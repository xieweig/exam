package org.grow.exam.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import javax.persistence.Embeddable;

/**
 * Author : xieweig
 * Date : 18-7-20
 * <p>
 * Description:
 */
@Embeddable
@Data
public class ClassInfo {

    @CsvBindByName
    private Grade grade = Grade.java1;

    @CsvBindByName
    private StudentSubject subject = StudentSubject.spring;


    public enum  Grade {
        java1, java2, java3, java4,java5,java6,java7, java8,
        os1,os2,os3,os4,os5,os6,
        dotNet1,dotNet2,dotNet3
    }

    public enum  StudentSubject {
        uml, junit, spring, html,javase1,javase2,php,hibernate
    }
    @CsvBindByName
    private Integer times = 1;

}
