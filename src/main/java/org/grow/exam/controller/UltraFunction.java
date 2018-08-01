package org.grow.exam.controller;


import org.grow.exam.domain.Result;
import org.grow.exam.infrastruture.JpaResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
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
        IntSummaryStatistics summary = results.stream().collect(Collectors.summarizingInt(Result::getTotalScore));

        return new ModelAndView("analysis.html",new HashMap<String, Object>(){{
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

        }});
    }
    @PostMapping("/trainer/emailResults")
    @ResponseBody
    public String email(){
        return  "-----";
    }
}
