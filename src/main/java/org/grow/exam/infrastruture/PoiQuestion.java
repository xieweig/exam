package org.grow.exam.infrastruture;

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
public interface PoiQuestion extends Closeable {
    List<Question> selectAll( int startRow, int size);

    List<Question> selectAll(InputStream inputStream , int startRow , int size);

    Map<String,Integer> readStandardAnswer(InputStream inputStream, int startRow , int size);
}
