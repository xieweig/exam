package org.grow.exam.domain;

import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

}
