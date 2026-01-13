package org.example.tlbglxt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.dto.request.consultation.SubmitRatingRequest;
import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.example.tlbglxt.entity.chat.ConsultationRating;
import org.example.tlbglxt.repository.mongo.ConsultationChatRepository;
import org.example.tlbglxt.service.ConsultationRatingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 问诊评价服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationRatingServiceImpl implements ConsultationRatingService {

    private final ConsultationChatRepository consultationChatRepository;

    @Override
    public Result<Object> submitRating(Long userId, SubmitRatingRequest request) {
        try {
            // 查找问诊记录
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(request.getConsultationNo());
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 验证权限：只有患者可以评价
            if (!userId.equals(consultation.getPatientId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限评价此问诊");
            }

            // 验证问诊状态：只有已完成的问诊可以评价
            if (consultation.getStatus() != 3) {
                return Result.error(ResultCode.PARAM_ERROR, "只有已完成的问诊才能评价");
            }

            // 检查是否已经评价过
            if (consultation.getRating() != null) {
                return Result.error(ResultCode.DATA_ALREADY_EXIST, "该问诊已经评价过了");
            }

            // 创建评价对象
            ConsultationRating rating = new ConsultationRating();
            rating.setScore(request.getScore());
            rating.setComment(request.getComment() != null ? request.getComment().trim() : "");
            rating.setRatingTime(LocalDateTime.now());

            // 更新问诊记录
            consultation.setRating(rating);
            consultation.setUpdateTime(LocalDateTime.now());

            // 保存更新
            consultationChatRepository.save(consultation);

            log.info("问诊评价提交成功，咨询编号：{}，患者ID：{}，评分：{}", 
                    request.getConsultationNo(), userId, request.getScore());

            return Result.success("评价提交成功");

        } catch (Exception e) {
            log.error("提交问诊评价失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "评价提交失败");
        }
    }

    @Override
    public Result<Object> getRating(String consultationNo, Long userId) {
        try {
            // 查找问诊记录
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(consultationNo);
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 验证权限：患者和医生都可以查看评价
            if (!userId.equals(consultation.getPatientId()) && !userId.equals(consultation.getDoctorId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限查看此问诊评价");
            }

            return Result.success(consultation.getRating());

        } catch (Exception e) {
            log.error("获取问诊评价失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "获取评价失败");
        }
    }

    @Override
    public Result<Object> getDoctorRatingStats(Long doctorId) {
        try {
            // 查找该医生的所有已完成且有评价的问诊
            List<ConsultationChat> consultations = consultationChatRepository.findByDoctorIdAndStatusAndRatingIsNotNull(doctorId, 3);

            if (consultations.isEmpty()) {
                // 返回默认统计数据
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalRatings", 0);
                stats.put("averageScore", 0.0);
                stats.put("scoreDistribution", new HashMap<String, Integer>() {{
                    put("5", 0); put("4", 0); put("3", 0); put("2", 0); put("1", 0);
                }});
                return Result.success(stats);
            }

            // 统计评分分布
            Map<Integer, Integer> scoreCount = new HashMap<>();
            double totalScore = 0.0;
            int totalRatings = 0;

            for (ConsultationChat consultation : consultations) {
                ConsultationRating rating = consultation.getRating();
                if (rating != null && rating.getScore() != null) {
                    Integer score = rating.getScore();
                    scoreCount.put(score, scoreCount.getOrDefault(score, 0) + 1);
                    totalScore += score;
                    totalRatings++;
                }
            }

            // 构建返回数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRatings", totalRatings);
            stats.put("averageScore", totalRatings > 0 ? Math.round(totalScore / totalRatings * 10.0) / 10.0 : 0.0);
            
            Map<String, Integer> scoreDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                scoreDistribution.put(String.valueOf(i), scoreCount.getOrDefault(i, 0));
            }
            stats.put("scoreDistribution", scoreDistribution);

            return Result.success(stats);

        } catch (Exception e) {
            log.error("获取医生评价统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "获取评价统计失败");
        }
    }
} 