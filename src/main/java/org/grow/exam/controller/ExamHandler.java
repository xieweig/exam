package org.grow.exam.controller;


import org.grow.exam.domain.*;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.JpaResult;
import org.grow.exam.infrastruture.PoiQuestion;
import org.grow.exam.infrastruture.RandomString;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    * xieweig notes: 初始化装载试题
    */
    @ResponseBody
    @GetMapping(value = "/configLoadQuestions")
    public List<Question> loadQuestions(@RequestParam int startRow , @RequestParam int size){
        /**
        **
        * xieweig notes: 如果未规定则读取第一行 读20道题
        */

        poiQuestion.selectAll(startRow, size).stream().map(x -> jpaQuestion.save(x)).forEach(System.out::println);


        return jpaQuestion.findAll(Sort.by("questionCode"));


    }

    /**
    **
    * xieweig notes: 第一步教师通过提交excel文件 录入试题和正确答案
    */
    @PostMapping(value = "/uploadQuestions")
    public  ModelAndView upLoad(@RequestParam MultipartFile file,
                                 @RequestParam(required = false) int startRow,
                                 @RequestParam(required = false) int size) throws IOException {

        if (startRow == 0) startRow =1;
        if (size == 0) size =20;
        /**
        **
        * xieweig notes: 读取正确答案保存在单例bean中
        */
        standardAnswer.setSubmitted(false);
        standardAnswer.setCorrectAnswers(poiQuestion.readStandardAnswer(file.getInputStream(),startRow,size));
        /**
        **
        * xieweig notes: 读取考试试题的信息保存到数据库中并返回给前台跳转页面
        */
        List<Question> questions = this.cleanInsert(file.getInputStream(), startRow, size);


         return new ModelAndView("confirm", new HashMap<String, Object>(){{
             put("questions", questions);

             put("standardAnswer", standardAnswer);
             put("grades",ClassInfo.Grade.values());
             put("subjects",ClassInfo.StudentSubject.values());
         }});

    }
    @Transactional
    protected List<Question> cleanInsert(InputStream in, Integer startRow, Integer size ){
        jpaQuestion.deleteAll();
        return  poiQuestion.selectAll(in,startRow, size)
                .stream()
                .map(jpaQuestion::save)
                .collect(Collectors.toList());

    }
    /**
    **
    * xieweig notes: 第二步教师通过录入本次考试的相关信息，如考试科目和考试班级 第几次课堂测验 等信息
    */
    @ResponseBody
    @PostMapping(value = "/classInfoAndOpenTest")
    public ClassInfo saveClassInfo(@RequestBody ClassInfo classInfo){
        standardAnswer.setClassInfo(classInfo);
        standardAnswer.setSubmitted(true);
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

    @ResponseBody
    @PutMapping(value = "/student/finish")
    public Result submit(@RequestBody AnswerSheet answerSheet){


        Result result = new Result();

        /**
        **
        * xieweig notes: 比较答案
        */
        Map<String,Integer> answers = answerSheet.getAnswers();
        Map<String,Integer> correctAnswers = standardAnswer.getCorrectAnswers();
        for (String key : answers.keySet()) {
            System.out.println(answers.get(key));
            /**
            **
            * xieweig notes: 如果不符合正确答案，那么就认为错误，此处只要求答题卡是单选题，正确答案可以是多个 例如答题卡选A正确答案是AC
            */
            if ((answers.get(key) & correctAnswers.get(key)) == 0){
                 result.getWrongsCode().add(key);

            }

        }
        /**
        **
        * xieweig notes: 计算属性
        */
        result.setWrongs(result.getWrongsCode().toString());
        result.setTotalScore(100 - result.getWrongsCode().size() *100 /correctAnswers.size());
        
        /**
        **
        * xieweig notes: 基本信息设置
        */
        result.setClassInfo(standardAnswer.getClassInfo());
        result.setMemberCode(answerSheet.getMemberCode());
        result.setResultCode(randomString.random5String("Result"));
        result.setResultType(Result.ResultType.allright);

        return jpaResult.save(result);


    }
    @ResponseBody
    @GetMapping("/result/{memberCode}")
    public Result getOne(@PathVariable String memberCode){
        return jpaResult.findByMemberCode(memberCode);
    }
/*    @ResponseBody
    @DeleteMapping("/results")
    @Transactional
    public void deleteResults(){
        jpaResult.deleteAll();
    }
    @ResponseBody
    @DeleteMapping("/questions")
    @Transactional
    public void deleteQuestions(){
        jpaQuestion.deleteAll();
    }*/
}
