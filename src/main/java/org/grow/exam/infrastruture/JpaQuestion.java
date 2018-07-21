package org.grow.exam.infrastruture;

import org.grow.exam.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@Repository
public interface JpaQuestion extends JpaRepository<Question, Long>, JpaSpecificationExecutor<JpaQuestion> {

    Question findByQuestionCode(String questionCode);

}
