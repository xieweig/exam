package org.grow.exam.controller;

import org.grow.exam.domain.ClassInfo;
import org.grow.exam.domain.Question;
import org.grow.exam.domain.StandardAnswer;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.PoiQuestion;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@RestController
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
    @GetMapping(value = "/configLoadQuestions")
    public List<Question> loadQuestions(@RequestParam int startRow , @RequestParam int size){
        /**
        **
        * xieweig notes: 如果未规定则读取第一行 读20道题
        */
        if (startRow == 0 || size == 0) { startRow= 1;  size=20 ;}

        poiQuestion.selectAll(startRow, size).stream().map(x -> jpaQuestion.save(x)).forEach(System.out::println);


        return jpaQuestion.findAll(Sort.by("questionCode"));


    }
    @Transactional
    @GetMapping(value = "/delete")
    public void deleteAll(){
        jpaQuestion.deleteAll();
    }


    @PostMapping(value = "/upLoadQuestions")
    public List<Question> upLoad(@RequestParam MultipartFile file, @RequestParam int startRow, @RequestParam int size) throws IOException {

        if (startRow == 0 || size == 0) { startRow= 1;  size=20 ;}

        standardAnswer.setCorrectAnswers(poiQuestion.readStandardAnswer(file.getInputStream(),startRow,size));
        System.err.println(standardAnswer.getCorrectAnswers());
        return poiQuestion.selectAll(file.getInputStream(),startRow, size)
                .stream()
                .map(jpaQuestion::save)
                .collect(Collectors.toList());


    }
    @PostMapping(value = "/classInfo")
    public void saveClassInfo(@RequestBody ClassInfo classInfo){

    }

}
