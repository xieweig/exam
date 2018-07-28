package org.grow.exam.domain;

import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@Data
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StandardAnswer {

    private  Map<String,Integer> correctAnswers = new HashMap<>();



    private ClassInfo classInfo = new ClassInfo();
    /**
    **
    * xieweig notes: 该字段用来解释当未点击开放考试按钮时，即便导入了excel文件，学生端页看不到考试题目
    */
    private Boolean submitted = false;

    /**
    **
    * xieweig notes: 该字段用来解释当点击关闭考试按钮时候，系统仍然运行，学生可以看试题，但是不能继续提交答题卡。
     *
    */
    private Boolean examClosed = false;

    private LocalDateTime examStartTime;



}
