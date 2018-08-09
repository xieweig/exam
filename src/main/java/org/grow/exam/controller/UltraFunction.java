package org.grow.exam.controller;


import org.grow.exam.domain.GlobalVar;
import org.grow.exam.domain.Result;
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
import java.io.IOException;
import java.nio.file.DirectoryStream;
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
    @GetMapping("/trainer/analysis")
    public ModelAndView analysis(){

        /**
        **
        * xieweig notes: 统计成绩平均分 各个分数段人数 错的最多的题目前五个
        */
        List<Result> results = jpaResult.findAll();
        results.sort(Comparator.comparing(Result::getTotalScore));
        IntSummaryStatistics summary = results.stream().collect(Collectors.summarizingInt(Result::getTotalScore));

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
    public ResponseEntity<byte[]> download(@PathVariable String fileName) throws IOException {

        if (!globalVar.getEnableDownload()) return  new ResponseEntity<>(HttpStatus.FORBIDDEN);

        Path base = Paths.get(System.getProperty("user.home"));
        if (StringUtils.isEmpty(fileName)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Path file = base.resolve(fileName);
        if (Files.notExists(file)) return new ResponseEntity<>(HttpStatus.NOT_FOUND);


        return new ResponseEntity<>(Files.readAllBytes(file),new HttpHeaders(){{
            setContentType(MediaType.APPLICATION_OCTET_STREAM);
            setContentDispositionFormData("attachment",fileName);
        }}, HttpStatus.OK);
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
        Path template =  new ClassPathResource("static/test_content.xlsx").getFile().toPath();
        //System.out.println(template.toString());
        return new ResponseEntity<>(Files.readAllBytes(template),new HttpHeaders(){{
            setContentType(MediaType.APPLICATION_OCTET_STREAM);
            setContentDispositionFormData("attachment",template.getFileName().toString());
        }},HttpStatus.OK);
    }

}
