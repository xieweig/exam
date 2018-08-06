package org.grow.exam.controller;


import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.grow.exam.domain.*;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.JpaResult;
import org.grow.exam.infrastruture.PoiQuestion;
import org.grow.exam.infrastruture.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@Controller
public class ExamHandler {
    @Resource
    private PoiQuestion poiQuestion;
    @Resource
    private JpaQuestion jpaQuestion;

    @Resource
    private StandardAnswer standardAnswer;

    /**
    **
    * xieweig notes: 第一步教师通过提交excel文件 录入试题和正确答案
    */
    @PostMapping(value = "/trainer/uploadQuestions")
    public  ModelAndView upLoad(@RequestParam MultipartFile file,
                                 @RequestParam(required = false) int startRow,
                                 @RequestParam(required = false) int size){

        if (startRow == 0) startRow =1;
        if (size == 0) size =20;
        /**
        **
        * xieweig notes: 初始化，上传一次excel，代表要进行一场考试，进入考试准备阶段啊，此时暂时不允许学生访问试题库，此时同样不必允许学生提交考试答案
         * 读取正确答案保存在单例bean中
        */
        standardAnswer.setSubmitted(false);
        standardAnswer.setExamClosed(true);


        try(Workbook workbook = WorkbookFactory.create(file.getInputStream())){
            this.poiQuestion.setWorkbook(workbook);
            standardAnswer.setCorrectAnswers(poiQuestion.readStandardAnswer(startRow,size));

            /**
             **
             * xieweig notes: 读取考试试题的信息保存到数据库中并返回给前台跳转页面
             */
            List<Question> questions = this.cleanInsert(startRow, size);
            return new ModelAndView("confirm", new HashMap<String, Object>(){{
                put("questions", questions);
                put("standardAnswer", standardAnswer);
                put("grades",ClassInfo.Grade.values());
                put("subjects",ClassInfo.StudentSubject.values());
            }});

        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
            return new ModelAndView("error");
        }

    }
    @Transactional
    protected List<Question> cleanInsert(Integer startRow, Integer size ){
        jpaQuestion.deleteAll();
        return  poiQuestion.selectAll(startRow, size)
                .stream()
                .map(jpaQuestion::save)
                .collect(Collectors.toList());

    }
    /**
    **
    * xieweig notes: 第二步教师通过录入本次考试的相关信息，如考试科目和考试班级 第几次课堂测验 等信息
    */
    @ResponseBody
    @PostMapping(value = "/trainer/classInfoAndOpenTest")
    public ClassInfo saveClassInfo(@RequestBody ClassInfo classInfo){
        standardAnswer.setClassInfo(classInfo);

        /**
        **
        * xieweig notes: 此时进入考试阶段，考生可以看到考题，当然页可以访问,提交按钮有必要出来
        */
        if (standardAnswer.getSubmitted()) throw new RuntimeException("未知黑客破坏了游戏规则");
        standardAnswer.setSubmitted(true);
        standardAnswer.setExamClosed(false);

        standardAnswer.setExamStartTime(LocalDateTime.now());
        return standardAnswer.getClassInfo();
    }
    /**
    **
    * xieweig notes: 第三步，考生访问答题界面后，提交答题卡，后台接受答题卡并直接计算出正确答案，把每一份答案持久化到数据库
    */
    @Resource
    private JpaResult jpaResult;
    @Resource
    private RandomString randomString;
    @Resource
    private ApplicationContext applicationContext;

    @ResponseBody
    @PutMapping(value = "/student/finish")
    public Result submit(@RequestBody AnswerSheet answerSheet){

        if (standardAnswer.getExamClosed()) return new Result(){{
           setMessage("考试已经关闭，不允许再提交答案");
        }};
        /**
        **
        * xieweig notes: 加一个小拦截器，防止同一个学生提交两次
        */
        if (jpaResult.findByMemberCode(answerSheet.getMemberCode()) != null) return new Result(){{
            setMessage("该学号已经提交过一次，不允许再次提交，如有问题，请联系老师解决");
        }};
        /**
        **
        * xieweig notes: 让spring容器帮助new实例对象的好处是可以帮你装配上@Resource工具类，但如果用的不多的话可以建一个静态类更好。
         *
        */


        Result result = applicationContext.getBean(Result.class);

        /**
        **
        * xieweig notes: 比较答案
        */
        Map<String,Integer> answers = answerSheet.getAnswers();
        Map<String,Integer> correctAnswers = standardAnswer.getCorrectAnswers();
        for (String key : correctAnswers.keySet()) {
            /**
            **
            * xieweig notes: 这个if应对答题卡有没填写的情况 为什么不写在一起 防止思维混乱 也可以声明一个boolean存储
            */
            if (!answers.containsKey(key)){
                result.getWrongsList().add(key);
                continue;
            }
            /**
            **
            * xieweig notes: 如果不符合正确答案，那么就认为错误，此处只要求答题卡是单选题，正确答案可以是多个 例如答题卡选A正确答案是AC
            */
            if ((answers.get(key) & correctAnswers.get(key)) == 0){
                 result.getWrongsList().add(key);

            }

        }

        /**
        **
        * xieweig notes: 计算属性 正常应该写，利用钩子函数@prepersist 见result实体类里
        */
        //result.setWrongsString(result.getWrongsList().toString());
        //result.setTotalScore(100 - result.getWrongsList().size() *100 /correctAnswers.size());

        /**
        **
        * xieweig notes: 基本信息设置 分别从教师提交的信息standard和学生提交的信息answer_sheet综合获取
        */
        result.setClassInfo(standardAnswer.getClassInfo());
        result.setMemberCode(answerSheet.getMemberCode());
        result.setMessage(answerSheet.getName()+ "留言："+answerSheet.getRemarks());

        result.setResultCode(randomString.random5String("Result"));
       // System.out.println(result);
        return jpaResult.save(result);


    }
    /**
    **
    * xieweig notes: 关闭考试按钮
    */
    @ResponseBody
    @GetMapping("/trainer/closeExam")
    public boolean closeExam(){
        standardAnswer.setExamClosed(true);
        return true;
    }
    /**
    **
    * xieweig notes: 临时开启考试按钮，为了应对紧急情况
    */
    @ResponseBody
    @GetMapping("/trainer/tempOpenExam")
    public boolean openExam(){
        standardAnswer.setExamClosed(false);
        return false;
    }


    @GetMapping("/trainer/csvResults")
    public ResponseEntity<byte[]> exportAsCsv() throws IOException {
        Path workDir = Paths.get(System.getProperty("user.home")).resolve(".examApp");

        if (Files.notExists(workDir)) Files.createDirectory(workDir);

        Path file = workDir.resolve("results.csv");

        if (Files.notExists(file)) Files.createFile(file);


        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file)
        ){
            StatefulBeanToCsv<Result> beanToCsv = new StatefulBeanToCsvBuilder<Result>(bufferedWriter).build();
            beanToCsv.write(jpaResult.findAll(Sort.by(Sort.Direction.ASC,"memberCode")));
            bufferedWriter.flush();

        }catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(Files.readAllBytes(file),new HttpHeaders(){{
            setContentType(MediaType.APPLICATION_OCTET_STREAM);
            setContentDispositionFormData("attachment", standardAnswer.getClassInfo().toString()+".csv");
        }},HttpStatus.OK);


    }






}
