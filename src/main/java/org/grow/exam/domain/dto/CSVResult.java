package org.grow.exam.domain.dto;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import org.grow.exam.domain.AnswerSheet;
import org.grow.exam.domain.ClassInfo;
import org.grow.exam.domain.Result;
import org.grow.exam.domain.StandardAnswer;

import java.time.LocalDateTime;

/**
 * Author : xieweig
 * Date : 18-8-6
 * <p>
 * Description:
 */
@Data
public class CSVResult{

    @CsvBindByName(column="memberCode")
    private String memberCode;

    @CsvBindByName
    private Integer totalScore;

    @CsvBindByName
    private String message;
    @CsvBindByName
    private ClassInfo.Grade grade = ClassInfo.Grade.java1;

    @CsvBindByName
    private ClassInfo.StudentSubject subject = ClassInfo.StudentSubject.spring;

    @CsvBindByName
    private Integer times = 1;

    @CsvBindByName
    private LocalDateTime examFinishTime;

    @CsvBindByName
    private String wrongsString;


    public static CSVResult getInstanceFrom(Result result, StandardAnswer standardAnswer){
        CSVResult csvResult = new CSVResult();
        csvResult.setMemberCode(result.getMemberCode());
        csvResult.setMessage(result.getMessage());
        csvResult.setTotalScore(result.getTotalScore());
        csvResult.setExamFinishTime(result.getExamFinishTime());
        csvResult.setWrongsString(result.getWrongsString());
        csvResult.setGrade(standardAnswer.getClassInfo().getGrade());
        csvResult.setSubject(standardAnswer.getClassInfo().getSubject());
        csvResult.setTimes(standardAnswer.getClassInfo().getTimes());
        return csvResult;
    }
}
