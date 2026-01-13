package org.example.tlbglxt.repository.mongo;

import org.example.tlbglxt.entity.health.WeightRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 体重记录Repository接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface WeightRecordRepository extends MongoRepository<WeightRecord, String> {

    /**
     * 根据用户ID查找体重记录
     *
     * @param userId 用户ID
     * @return 体重记录
     */
    Optional<WeightRecord> findByUserId(Long userId);

    /**
     * 检查用户是否存在体重记录
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(Long userId);
} 