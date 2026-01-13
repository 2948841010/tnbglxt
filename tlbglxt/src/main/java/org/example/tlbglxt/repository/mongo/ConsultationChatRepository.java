package org.example.tlbglxt.repository.mongo;

import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 问诊聊天MongoDB Repository
 */
@Repository
public interface ConsultationChatRepository extends MongoRepository<ConsultationChat, String> {
    
    /**
     * 根据问诊编号查找
     */
    Optional<ConsultationChat> findByConsultationNo(String consultationNo);
    
    /**
     * 根据患者ID查找问诊记录，按创建时间倒序
     */
    List<ConsultationChat> findByPatientIdOrderByCreateTimeDesc(Long patientId);
    
    /**
     * 根据医生ID查找问诊记录，按创建时间倒序
     */
    List<ConsultationChat> findByDoctorIdOrderByCreateTimeDesc(Long doctorId);
    
    /**
     * 根据患者ID和状态查找问诊记录
     */
    List<ConsultationChat> findByPatientIdAndStatusOrderByCreateTimeDesc(Long patientId, Integer status);
    
    /**
     * 根据医生ID和状态查找问诊记录
     */
    List<ConsultationChat> findByDoctorIdAndStatusOrderByCreateTimeDesc(Long doctorId, Integer status);
    
    /**
     * 分页查询患者问诊记录
     */
    Page<ConsultationChat> findByPatientId(Long patientId, Pageable pageable);
    
    /**
     * 分页查询医生问诊记录
     */
    Page<ConsultationChat> findByDoctorId(Long doctorId, Pageable pageable);
    
    /**
     * 分页查询医生指定状态的问诊记录
     */
    @Query("{'doctorId': ?0, 'status': {'$in': ?1}}")
    Page<ConsultationChat> findByDoctorIdAndStatusIn(Long doctorId, List<Integer> statusList, Pageable pageable);
    
    /**
     * 查找正在进行的问诊（状态为1或2）
     */
    @Query("{'$and': [{'$or': [{'patientId': ?0}, {'doctorId': ?0}]}, {'status': {'$in': [1, 2]}}]}")
    List<ConsultationChat> findActiveConsultationsByUserId(Long userId);
    
    /**
     * 统计医生今日问诊数量
     */
    @Query(value = "{'doctorId': ?0, 'createTime': {'$gte': ?1, '$lt': ?2}}", count = true)
    long countTodayConsultationsByDoctorId(Long doctorId, java.time.LocalDateTime startOfDay, java.time.LocalDateTime endOfDay);
    
    /**
     * 统计患者问诊总数
     */
    long countByPatientId(Long patientId);
    
    /**
     * 统计医生问诊总数
     */
    long countByDoctorId(Long doctorId);
    
    /**
     * 统计医生待回复咨询数量（状态为1或2）
     */
    @Query(value = "{'doctorId': ?0, 'status': {'$in': [1, 2]}}", count = true)
    long countPendingConsultationsByDoctorId(Long doctorId);
    
    /**
     * 统计医生指定状态的咨询数量
     */
    @Query(value = "{'doctorId': ?0, 'status': ?1}", count = true)
    long countConsultationsByDoctorIdAndStatus(Long doctorId, Integer status);
    
    /**
     * 获取医生最近咨询列表
     */
    @Query("{'doctorId': ?0}")
    List<ConsultationChat> findRecentByDoctorId(Long doctorId, Pageable pageable);
    
    /**
     * 查找医生已完成且有评价的问诊记录
     */
    @Query("{'doctorId': ?0, 'status': ?1, 'rating': {'$exists': true, '$ne': null}}")
    List<ConsultationChat> findByDoctorIdAndStatusAndRatingIsNotNull(Long doctorId, Integer status);

    // ======================= 管理员统计相关方法 =======================

    /**
     * 统计指定时间范围内的咨询数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定状态的咨询数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定状态和结束时间范围的咨询数量
     */
    @Query(value = "{'status': ?0, 'endTime': {'$gte': ?1, '$lt': ?2}}", count = true)
    long countByStatusAndEndTimeBetween(Integer status, LocalDateTime startTime, LocalDateTime endTime);
} 