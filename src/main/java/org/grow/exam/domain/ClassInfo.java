package org.grow.exam.domain;

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
    private Grade grade = Grade.java1;

    private StudentSubject subject = StudentSubject.spring;


    public enum  Grade {
        java1, java2, java3, java4
    }

    public enum  StudentSubject {
        uml, junit, spring
    }

    private Integer times = 1;

}
