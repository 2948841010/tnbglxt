package org.example.tlbglxt.repository.mongo;

import org.example.tlbglxt.entity.health.BloodGlucoseRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 血糖记录Repository
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface BloodGlucoseRecordRepository extends MongoRepository<BloodGlucoseRecord, String> {

    /**
     * 根据用户ID查找血糖记录
     *
     * @param userId 用户ID
     * @return 血糖记录
     */
    Optional<BloodGlucoseRecord> findByUserId(Long userId);

    /**
     * 根据用户ID和时间范围查询血糖记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 血糖记录列表
     */
    @Query("{ 'userId': ?0, 'records.measureTime': { $gte: ?1, $lte: ?2 } }")
    List<BloodGlucoseRecord> findByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查用户是否存在血糖记录
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(Long userId);

    /**
     * 根据用户ID删除血糖记录
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
} 