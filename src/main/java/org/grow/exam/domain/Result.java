package org.grow.exam.domain;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
**
* xieweig notes: 此类暂时看不见
*/
@Component
@Data
@Entity
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Result {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String resultCode;


    @Enumerated(EnumType.STRING)
    private ResultType resultType = ResultType.allright;
    public enum  ResultType {
        allright , frozen
    }

    private ClassInfo classInfo = new ClassInfo();

    private String memberCode;

    private String wrongs ;

    private Integer totalScore;

    @Transient
    private List<String> wrongsCode = new ArrayList<>();

}
