package org.grow.exam.controller;

import org.grow.exam.domain.Question;
import org.grow.exam.domain.StandardAnswer;
import org.grow.exam.infrastruture.JpaQuestion;
import org.grow.exam.infrastruture.PoiQuestion;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Resource
    private StandardAnswer standardAnswer;

    @RequestMapping(value = "/student/start", method = RequestMethod.GET)
    public ModelAndView testing(){

        if (standardAnswer.getSubmitted() == false) return new ModelAndView("testing",new HashMap<String,Object>(){{
            put("submitted",standardAnswer.getSubmitted());
        }});

        //List<Question> questions = jpaQuestion.findAll(Sort.by("questionCode"));

        return new ModelAndView("testing", new HashMap<String,Object>(){{
            put("questions", jpaQuestion.findAll(Sort.by("questionCode")));
            put("examClosed",standardAnswer.getExamClosed());
        }});

    }

    /**
    **
    * xieweig notes: 以下是路由 不是请求，对于属性 分清楚什么是引用什么是字段，对于url分清楚什么是路由什么是请求
    */
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



    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/403")
    public String error403() {
        return "error";
    }
    @GetMapping("/student")
    public ModelAndView listDir(){
        Path base = Paths.get(System.getProperty("user.home"));

        List<String> files = new LinkedList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(base)) {
            for (Path file : directoryStream) {
                if (Files.isRegularFile(file))
                files.add(file.getFileName().toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(files);
        return new ModelAndView("download",new HashMap<String, Object>(){{
            put("files",files);
            put("userHome",base.toString());
        }});

    }

}
