package org.grow.exam.infrastruture;

import org.grow.exam.domain.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Author : xieweig
 * Date : 18-7-21
 * <p>
 * Description:
 */
@Repository
public interface JpaResult extends JpaRepository<Result, Long>,JpaSpecificationExecutor<Result> {

    Result findByMemberCode(String memberCode);

    Result findByResultCode(String resultCode);
}
