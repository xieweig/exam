package org.grow.exam.domain;


import lombok.Data;

import java.util.*;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:学生提交上来的vo 经过与正确答案比较计算后生成待持久化的实体信息，也就是
 */
@Data
public class AnswerSheet {



    private AnswerSheetType answerSheetType = AnswerSheetType.studentAnswer;

    private enum AnswerSheetType {
        standardAnswer, studentAnswer
    }
    /**
    **
    * xieweig notes: 提交的答案应该是questionCode=8，4，2，1等键值对，共同构成一个动态map
    */
    private Map<String,Integer> answers= new HashMap<>();
    /**
    **
    * xieweig notes: 学生的信息memberCode是主要的，name方便学生查找，至于班级科目这些信息应该是由老师填写在正确答案信息类中
    */
    private String memberCode;

    private String name;

    private String remarks;



}
