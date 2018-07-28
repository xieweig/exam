package org.grow.exam.controller;


import org.grow.exam.domain.*;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.JpaResult;
import org.grow.exam.infrastruture.PoiQuestion;
import org.grow.exam.infrastruture.RandomString;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.IOException;

import java.io.InputStream;
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
                                 @RequestParam(required = false) int size) throws IOException {

        if (startRow == 0) startRow =1;
        if (size == 0) size =20;
        /**
        **
        * xieweig notes: 初始化，上传一次excel，代表要进行一场考试，进入考试准备阶段啊，此时暂时不允许学生访问试题库，此时允许学生提交考试答案
         * 读取正确答案保存在单例bean中
        */
        standardAnswer.setSubmitted(false);
        standardAnswer.setExamClosed(false);
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
    @PostMapping(value = "/trainer/classInfoAndOpenTest")
    public ClassInfo saveClassInfo(@RequestBody ClassInfo classInfo){
        standardAnswer.setClassInfo(classInfo);

        /**
        **
        * xieweig notes: 此时进入考试阶段，考生可以看到考题，当然页可以访问
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
        * xieweig notes: 计算属性 正常应该写，利用钩子函数@prepersist 见result实体类里
        */
        //result.setWrongs(result.getWrongsCode().toString());
        //result.setTotalScore(100 - result.getWrongsCode().size() *100 /correctAnswers.size());
        //result.setClassInfo(standardAnswer.getClassInfo());

        /**
        **
        * xieweig notes: 基本信息设置
        */
        result.setMemberCode(answerSheet.getMemberCode());
        result.setMessage(answerSheet.getName()+ "留言："+answerSheet.getRemarks());

        result.setResultCode(randomString.random5String("Result"));

        return jpaResult.save(result);


    }
    /**
    **
    * xieweig notes: 关闭考试按钮
     *
    */
    @GetMapping("/trainer/closeExam")
    public void closeExam(){
        standardAnswer.setExamClosed(true);
    }
    /**
    **
    * xieweig notes: 临时开启考试按钮，为了应对紧急情况
    */
    @GetMapping("/trainer/tempOpenExam")
    public void openExam(){
        standardAnswer.setExamClosed(false);
    }
    @GetMapping("/trainer/csvResults")
    public void exportAsCsv(){
        jpaResult.findAll(Sort.by(Sort.Direction.ASC,"memberCode"));
    }


}
