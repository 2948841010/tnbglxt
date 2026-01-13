package org.example.tlbglxt.controller.consultation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.consultation.SubmitRatingRequest;
import org.example.tlbglxt.service.ConsultationRatingService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 问诊评价控制器
 */
@Tag(name = "问诊评价管理", description = "问诊评价相关接口")
@RestController
@RequestMapping("/api/v1/consultation")
@RequiredArgsConstructor
@Slf4j
public class ConsultationRatingController {

    private final ConsultationRatingService consultationRatingService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "提交问诊评价")
    @PostMapping("/rating")
    public Result<Object> submitRating(
            @RequestHeader("Authorization") String token,
            @RequestBody SubmitRatingRequest request) {
        
        try {
            // 提取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            log.info("用户{}提交问诊评价，咨询编号：{}，评分：{}", userId, request.getConsultationNo(), request.getScore());
            
            return consultationRatingService.submitRating(userId, request);
            
        } catch (Exception e) {
            log.error("提交问诊评价失败", e);
            return Result.error("提交评价失败");
        }
    }

    @Operation(summary = "获取问诊评价")
    @GetMapping("/{consultationNo}/rating")
    public Result<Object> getRating(
            @RequestHeader("Authorization") String token,
            @PathVariable String consultationNo) {
        
        try {
            // 提取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            return consultationRatingService.getRating(consultationNo, userId);
            
        } catch (Exception e) {
            log.error("获取问诊评价失败", e);
            return Result.error("获取评价失败");
        }
    }
    
    @Operation(summary = "获取医生评价统计")
    @GetMapping("/doctor/{doctorId}/rating-stats")
    public Result<Object> getDoctorRatingStats(
            @PathVariable Long doctorId) {
        
        try {
            return consultationRatingService.getDoctorRatingStats(doctorId);
            
        } catch (Exception e) {
            log.error("获取医生评价统计失败", e);
            return Result.error("获取评价统计失败");
        }
    }
} 