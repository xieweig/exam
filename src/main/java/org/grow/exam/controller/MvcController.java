package org.grow.exam.controller;

import org.grow.exam.domain.Question;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.PoiQuestion;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author : xieweig
 * Date : 18-7-18
 * <p>
 * Description:
 */
@Controller
public class MvcController {
    @Resource
    private JpaQuestion jpaQuestion;

    @Resource
    private PoiQuestion poiQuestion;

    @RequestMapping(value = "/testing", method = RequestMethod.GET)
    public ModelAndView testing(){

        List<Question> questions = jpaQuestion.findAll(Sort.by("questionCode"));
        return new ModelAndView("testing", new HashMap<String,Object>(){{
            put("questions", questions);
        }});

    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/trainer")
    public String user() {
        return "import";
    }

    @GetMapping("/student")
    public String about() {
        return "testing";
    }


    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/403")
    public String error403() {
        return "error";
    }

}
