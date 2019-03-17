package org.grow.exam.domain;

/**
 * Author : xieweig
 * Date : 18-7-18
 * <p>
 * Description:
 */
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Component
@Data
@Entity
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Question {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String questionCode;
    @Column(length = 1024)
    private String title;
    @Column(length = 1024)
    private String optionA;
    @Column(length = 1024)
    private String optionB;
    @Column(length = 1024)
    private String optionC;
    @Column(length = 1024)
    private String optionD;

    private String remarks;

}
