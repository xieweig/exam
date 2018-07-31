package org.grow.exam.domain;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    private ClassInfo classInfo = new ClassInfo();
    /**
    **
    * xieweig notes: 可以通过column来指定csv文件的列别名
    */
    @CsvBindByName(column="memberCode")
    private String memberCode;

    @CsvBindByName
    private Integer totalScore;
    /**
    **
    * xieweig notes: json绑定用，不方便存入关系型数据库，方便存入nosql
    */
    @Transient
    private List<String> wrongsList = new ArrayList<>();
    /**
    **
    * xieweig notes: 方便存入关系型数据库，不方便直接面向对象，不方便json绑定
    */
    @JsonIgnore
    @CsvBindByName
    private String wrongsString;
    /**
    **
    * xieweig notes: 引用一个工具类的数据，交给spring管理的单例bean，不映射json，不持久化到数据库。
    */
    @Resource
    @Transient
    @JsonIgnore
    private StandardAnswer standardAnswer;
    /**
    **
    * xieweig notes: 钩子函数保存更新前 计算属性 派生属性
     * Callback methods annotated on the bean class must return void and take no arguments
    */

    @PrePersist
    @PreUpdate
    public void beforeWrite(){
        this.wrongsString = StringUtils.arrayToCommaDelimitedString(this.wrongsList.toArray());

        this.setExamFinishTime(LocalDateTime.now());
        this.setTotalScore(100 - this.getWrongsList().size() *100 /standardAnswer.getCorrectAnswers().size());

    }
    /**
     **
     * xieweig notes: 钩子函数查询后 计算属性 派生属性
     * Callback methods annotated on the bean class must return void and take no arguments
     */
    @PostLoad
    public void AfterRead(){
        this.wrongsList = Arrays.asList(StringUtils.commaDelimitedListToStringArray(this.wrongsString));
    }
    /**
    **
    * xieweig notes:  记录学生中文名字英文名字等乱七八糟的信息
    */
    @CsvBindByName
    private String message;

    @CsvBindByName
    private LocalDateTime examFinishTime;
}
