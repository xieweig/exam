package org.grow.exam.infrastruture;

import org.apache.poi.ss.usermodel.Workbook;
import org.grow.exam.domain.Question;
import org.grow.exam.domain.StandardAnswer;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
public interface PoiQuestion{

    /**
    **
    * xieweig notes: poi为单例， workbook 非单例 所以每次使用一般方法前要set
    */

    void setWorkbook(Workbook workbook);

    List<Question> selectAll(int startRow , int size);

    Map<String,Integer> readStandardAnswer(int startRow , int size);

}
