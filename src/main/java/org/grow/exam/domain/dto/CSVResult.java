package org.grow.exam.domain.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import org.grow.exam.domain.AnswerSheet;
import org.grow.exam.domain.ClassInfo;
import org.grow.exam.domain.Result;

/**
 * Author : xieweig
 * Date : 18-8-6
 * <p>
 * Description:
 */
@Data
public class CSVResult  extends Result {

    private ClassInfo classInfo;

    @CsvBindByName
    private ClassInfo.Grade grade;
}
