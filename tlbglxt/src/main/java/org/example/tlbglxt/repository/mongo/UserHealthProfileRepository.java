package org.example.tlbglxt.repository.mongo;

import org.example.tlbglxt.entity.health.UserHealthProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户健康档案Repository
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Repository
public interface UserHealthProfileRepository extends MongoRepository<UserHealthProfile, String> {

    /**
     * 根据用户ID查找健康档案
     *
     * @param userId 用户ID
     * @return 健康档案
     */
    Optional<UserHealthProfile> findByUserId(Long userId);

    /**
     * 检查用户是否存在健康档案
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(Long userId);

    /**
     * 根据用户ID删除健康档案
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
} 