package org.grow.exam.controller;

import org.grow.exam.domain.Question;
import org.grow.exam.domain.Result;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.JpaResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author : xieweig
 * Date : 18-7-26
 * <p>
 * Description: 这部分接口是依据一般经验的增删改查等暴露出来的接口，主要方便开发，是写完实体类就拥有的，与业务逻辑无关
 *
 */
@RequestMapping(value = "/basic")
@RestController
public class BasicRest {
    @Resource
    private JpaQuestion jpaQuestion;
    /**
    **
    * xieweig notes: 针对Question
    */
    @GetMapping(value = "/questions")
    public List<Question> getAllQuestions(){
        return jpaQuestion.findAll();
    }

    @PostMapping(value = "/questions")
    public Page<Question> getPageQuestions(@RequestParam Integer page ,@RequestParam Integer size){
        Pageable pageable = PageRequest.of(page,size);
        return jpaQuestion.findAll(pageable);
    }

    @Transactional
    @DeleteMapping(value = "/questions" )
    public List<Question> deleteAllQuestions(){

        jpaQuestion.deleteAll();

        return jpaQuestion.findAll();
    }

    @Resource
    private JpaResult jpaResult;
    /**
    **
    * xieweig notes: 针对Result
    */
    @GetMapping(value = "/results")
    public List<Result> getAllResults(){
        return jpaResult.findAll();
    }
    @Transactional
    @DeleteMapping("/results")
    public List<Result> deleteAllResults(){

        jpaResult.deleteAll();

        return jpaResult.findAll();
    }

    @Transactional
    @DeleteMapping("/result/{memberCode}")
    public void deleteByMemberCode(@PathVariable String memberCode){
        jpaResult.deleteByMemberCode(memberCode);
    }
    @GetMapping("/result/{memberCode}")
    public Result getOne(@PathVariable String memberCode){
        return jpaResult.findByMemberCode(memberCode);
    }



}
