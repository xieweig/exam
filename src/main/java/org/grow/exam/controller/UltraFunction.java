package org.grow.exam.controller;



import org.grow.exam.domain.GlobalVar;
import org.grow.exam.domain.Result;
import org.grow.exam.domain.StandardAnswer;
import org.grow.exam.infrastruture.JpaResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author : xieweig
 * Date : 18-8-1
 * <p>
 * Description: 额外的功能 如考试成绩分析，考试结果发送到邮箱
 */
@Controller
public class UltraFunction {

    @Resource
    private JpaResult jpaResult;

    @Resource
    private StandardAnswer standardAnswer;

    @GetMapping("/trainer/analysis")
    public ModelAndView analysis(){

        /**
        **
        * xieweig notes: 统计成绩平均分 各个分数段人数 错的最多的题目前五个
        */
        List<Result> results = jpaResult.findAll();
        results.sort(Comparator.comparing(Result::getTotalScore));
        IntSummaryStatistics summary = results.stream().collect(Collectors.summarizingInt(Result::getTotalScore));
        /**
        **
        * xieweig notes: 根据题目code数查找出 错误人数，这里有三种解决方案
         * 第一种 着眼于每个questioncode 如同班级中统计 第一题错的举手，依次count++ 第二题错的举手，依次count++
         * 这种思路是最正常的，但是如果没有自动缓存 io次数将会是 题目数 × 学生数 如20*50 =1000，性能浪费
         * 第二种 讲桌上有个题目的统计表 每一个学生走上前，错误的贡献一笔，画正号统计 io次数 = 学生数
         * 第三种 学生提交答案的时候就直接贡献一笔，错题画正号统计，这种性能最好，io次数 = 0;，但是耦合度过高，一旦有同学重新考试提交，那么重新考试接口 不仅要delete result 还要去掉正号一笔 其他开发人员难懂
         * 所以这里采用第二种方案 当然本例子是内存数据库，其实差别没那么验证
        */

        Map<String, Integer> counts = standardAnswer.getCorrectAnswers().keySet()
                .stream()
                .collect(Collectors.toMap(x -> x,x ->0,(x,y)->x,LinkedHashMap::new));
       // counts.put("code",counts.get("code")+1); 核心语句
        for(Result  result : results){
            for (String wrongCode : result.getWrongsList()){
                counts.put( wrongCode , counts.get(wrongCode)+1);
            }
        }
        Map<String, Integer> finalMap = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEachOrdered(x -> finalMap.put((x.getKey()),x.getValue()));

        return new ModelAndView("analysis",new HashMap<String, Object>(){{
            put("reduce", new TreeMap<String ,Object>(){{
                put("01 考试人数 number",summary.getCount());
                put("02 平均分 average",summary.getAverage());
                put("03 最低分 min",summary.getMin());
                put("04 最高分 max",summary.getMax());
                put("10 90分以上 above90",results.stream().filter(x-> x.getTotalScore()>=90).count());
                put("11 80分以上 above80",results.stream().filter(x-> x.getTotalScore()>=80).count());
                put("12 60分以上 above60",results.stream().filter(x-> x.getTotalScore()>=60).count());
                put("13 60分以下 below60",results.stream().filter(x-> x.getTotalScore()<60).count());

                put("14 错题排名",finalMap);

            }});
            put("results",results);

        }});
    }



    @PostMapping("/trainer/emailResults")
    @ResponseBody
    public String email(){
        return  "-----";
    }
    /**
    **
    * xieweig notes: 学生下载某个文件
    */
    @Resource
    private GlobalVar globalVar;
    @ResponseBody
    @PostMapping("/admin/switchDownload")
    private Boolean switchDownload(){
        globalVar.setEnableDownload(!globalVar.getEnableDownload());
        return globalVar.getEnableDownload();
    }

    @GetMapping("/student/download/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable String fileName, HttpServletResponse response) throws IOException {

        if (!globalVar.getEnableDownload()) return  new ResponseEntity<>(HttpStatus.FORBIDDEN);

        Path base = Paths.get(System.getProperty("user.home"));
        if (StringUtils.isEmpty(fileName)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Path file = base.resolve(fileName);
        if (Files.notExists(file)) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        //如果小于50M的文文件，存入缓存中下载
        if (Files.size(file) <= 50*1024*1024) {
            if (!fileName.equals(globalVar.getTempFileName())) {
                globalVar.setTempFile(Files.readAllBytes(file));
                globalVar.setTempFileName(fileName);
            }

            return new ResponseEntity<>(globalVar.getTempFile(), new HttpHeaders() {{
                setContentType(MediaType.APPLICATION_OCTET_STREAM);
                setContentDispositionFormData("attachment", fileName);
            }}, HttpStatus.OK);
        }
        //如果大于50M的文件，通过buffer下载
        response.reset();
        response.setContentType("application/x-download");
        response.addHeader("Content-Length",""+Files.size(file));
        response.addHeader("Content-Disposition","attachment:filename="+file.getFileName());
        response.setContentType("application/octet-stream");


        byte[] buffer = new byte[20*1024*1024];
        try(BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file));
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        ){
                        int i= -1;
                        while((i=in.read(buffer)) != -1){
                            out.write(buffer,0,i);
                        }
                        out.flush();
                        response.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;


    }

    @GetMapping("/trainer/template")
    public ResponseEntity<byte[]> getExcelTemplate() throws IOException {
        /**
        **
        * xieweig notes: 路径这种东西页很麻烦 下面的一个是正确的 classpath 先转file 再转path 可以自己去掉注释看一看
         * static/test_content.xlsx ===》》 temp
         * /home/xieweig/Downloads/horrible/exam/target/classes/static/test_content.xlsx ===》》template
        */
/*      Path temp = Paths.get(new ClassPathResource("static/test_content.xlsx").getPath());
        System.out.println(temp.toString());*/
        InputStream template =  new ClassPathResource("static/test_content.xlsx").getInputStream();
        //System.out.println(template.toString());
        byte[] e = new byte[template.available()];
        template.read(e);
        return new ResponseEntity<>(e,new HttpHeaders(){{
            setContentType(MediaType.APPLICATION_OCTET_STREAM);
            setContentDispositionFormData("attachment","test_content.xlsx");
        }},HttpStatus.OK);
    }

}
