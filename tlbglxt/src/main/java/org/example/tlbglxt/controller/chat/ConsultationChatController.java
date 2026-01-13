package org.example.tlbglxt.controller.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.chat.CreateConsultationRequest;
import org.example.tlbglxt.dto.request.chat.SendMessageRequest;
import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.example.tlbglxt.entity.chat.ConsultationMessage;
import org.example.tlbglxt.entity.FileRecord;
import org.example.tlbglxt.service.ConsultationChatService;
import org.example.tlbglxt.service.FileUploadService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问诊聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "问诊聊天管理", description = "问诊聊天相关API")
public class ConsultationChatController {

    private final ConsultationChatService consultationChatService;
    private final FileUploadService fileUploadService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "创建问诊")
    @PostMapping("/consultation")
    public Result<ConsultationChat> createConsultation(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateConsultationRequest request) {
        
        // 提取患者ID
        String actualToken = token.replace("Bearer ", "");
        Long patientId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.createConsultation(patientId, request);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/message")
    public Result<ConsultationMessage> sendMessage(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody SendMessageRequest request) {
        
        // 提取发送者ID和用户类型
        String actualToken = token.replace("Bearer ", "");
        Long senderId = jwtUtil.getUserIdFromToken(actualToken);
        String userType = jwtUtil.getUserTypeFromToken(actualToken);
        
        // 根据用户类型确定发送者类型
        String senderType = "1".equals(userType) ? "doctor" : "patient";
        
        return consultationChatService.sendMessage(senderId, senderType, request);
    }

    @Operation(summary = "获取问诊详情")
    @GetMapping("/consultation/{consultationNo}")
    public Result<ConsultationChat> getConsultationDetail(
            @PathVariable String consultationNo) {
        
        return consultationChatService.getConsultationDetail(consultationNo);
    }

    @Operation(summary = "获取我的问诊列表")
    @GetMapping("/consultations/mine")
    public Result<List<ConsultationChat>> getMyConsultations(
            @RequestHeader("Authorization") String token) {
        
        // 提取用户ID和类型
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        String userType = jwtUtil.getUserTypeFromToken(actualToken);
        System.out.println("用户类型: " + userType);
        
        // 根据用户类型获取问诊列表
        if ("1".equals(userType)) {
            // 医生
            return consultationChatService.getDoctorConsultations(userId);
        } else {
            // 患者
            return consultationChatService.getPatientConsultations(userId);
        }
    }

    @Operation(summary = "分页获取我的问诊列表")
    @GetMapping("/consultations/mine/page")
    public Result<PageResult<ConsultationChat>> getMyConsultationsPage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        // 提取用户ID和类型
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        String userType = jwtUtil.getUserTypeFromToken(actualToken);
        
        // 根据用户类型获取问诊列表
        if ("1".equals(userType)) {
            // 医生 - 支持状态过滤
            if (status != null && !status.trim().isEmpty()) {
                return consultationChatService.getDoctorConsultationsPageByStatus(userId, page, size, status);
            } else {
                return consultationChatService.getDoctorConsultationsPage(userId, page, size);
            }
        } else {
            // 患者
            return consultationChatService.getPatientConsultationsPage(userId, page, size);
        }
    }

    @Operation(summary = "医生接诊")
    @PostMapping("/consultation/{consultationNo}/accept")
    public Result<Void> acceptConsultation(
            @RequestHeader("Authorization") String token,
            @PathVariable String consultationNo) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.acceptConsultation(doctorId, consultationNo);
    }

    @Operation(summary = "完成问诊")
    @PostMapping("/consultation/{consultationNo}/complete")
    public Result<Void> completeConsultation(
            @PathVariable String consultationNo) {
        
        return consultationChatService.completeConsultation(consultationNo);
    }

    @Operation(summary = "取消问诊")
    @PostMapping("/consultation/{consultationNo}/cancel")
    public Result<Void> cancelConsultation(
            @RequestHeader("Authorization") String token,
            @PathVariable String consultationNo) {
        
        // 提取用户ID
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.cancelConsultation(consultationNo, userId);
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/consultation/{consultationNo}/message/{messageId}/read")
    public Result<Void> markMessageAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable String consultationNo,
            @PathVariable String messageId) {
        
        // 提取用户ID
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.markMessageAsRead(consultationNo, messageId, userId);
    }

    @Operation(summary = "获取活跃问诊")
    @GetMapping("/consultations/active")
    public Result<List<ConsultationChat>> getActiveConsultations(
            @RequestHeader("Authorization") String token) {
        
        // 提取用户ID
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getActiveConsultations(userId);
    }

    @Operation(summary = "获取今日问诊数量")
    @GetMapping("/consultations/today/count")
    public Result<Long> getTodayConsultationCount(
            @RequestHeader("Authorization") String token) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getTodayConsultationCount(doctorId);
    }

    @Operation(summary = "获取待回复咨询数量")
    @GetMapping("/consultations/pending/count")
    public Result<Long> getPendingConsultationsCount(
            @RequestHeader("Authorization") String token) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getPendingConsultationsCount(doctorId);
    }

    @Operation(summary = "获取待接诊咨询数量")
    @GetMapping("/consultations/waiting/count")
    public Result<Long> getWaitingConsultationsCount(
            @RequestHeader("Authorization") String token) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getWaitingConsultationsCount(doctorId);
    }

    @Operation(summary = "获取正在接诊咨询数量")
    @GetMapping("/consultations/ongoing/count")
    public Result<Long> getOngoingConsultationsCount(
            @RequestHeader("Authorization") String token) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getOngoingConsultationsCount(doctorId);
    }

    @Operation(summary = "获取已完成咨询数量")
    @GetMapping("/consultations/completed/count")
    public Result<Long> getCompletedConsultationsCount(
            @RequestHeader("Authorization") String token) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getCompletedConsultationsCount(doctorId);
    }

    @Operation(summary = "获取最近咨询列表")
    @GetMapping("/consultations/recent")
    public Result<List<ConsultationChat>> getRecentConsultations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "5") int limit) {
        
        // 提取医生ID
        String actualToken = token.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(actualToken);
        
        return consultationChatService.getRecentConsultations(doctorId, limit);
    }

    @Operation(summary = "上传聊天图片")
    @PostMapping("/upload/image")
    public Result<Object> uploadChatImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("consultationNo") String consultationNo) {
        
        try {
            // 提取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            // 验证文件类型 - 只允许图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
            
            // 验证文件大小 - 最大10MB
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("图片大小不能超过10MB");
            }
            
            // 上传图片到OSS
            FileRecord fileRecord = fileUploadService.uploadFile(
                file, 
                "chat_image",  // 分类
                null,          // businessId (暂时不需要)
                "consultation", // businessType
                userId         // uploaderId
            );
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileRecord.getFileUrl());
            result.put("fileName", fileRecord.getOriginalName());
            result.put("fileSize", fileRecord.getFileSize());
            result.put("fileId", fileRecord.getId());
            
            log.info("聊天图片上传成功 - 用户ID: {}, 咨询编号: {}, 文件: {}, URL: {}", 
                    userId, consultationNo, fileRecord.getOriginalName(), fileRecord.getFileUrl());
            
            return Result.success(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("聊天图片上传参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("聊天图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传聊天文件（通用）")
    @PostMapping("/upload/file")
    public Result<Object> uploadChatFile(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("consultationNo") String consultationNo) {
        
        try {
            // 提取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            
            // 验证文件大小 - 最大50MB
            if (file.getSize() > 50 * 1024 * 1024) {
                return Result.error("文件大小不能超过50MB");
            }
            
            // 获取文件类型
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream"; // 默认类型
            }
            
            // 根据文件类型确定分类
            String category;
            if (contentType.startsWith("image/")) {
                category = "chat_image";
            } else if (contentType.startsWith("video/")) {
                category = "chat_video";
            } else if (contentType.startsWith("audio/")) {
                category = "chat_audio";
            } else if (contentType.contains("pdf")) {
                category = "chat_document";
            } else {
                category = "chat_file";
            }
            
            // 上传文件到OSS
            FileRecord fileRecord = fileUploadService.uploadFile(
                file, 
                category,      // 分类
                null,          // businessId (暂时不需要)
                "consultation", // businessType
                userId         // uploaderId
            );
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileRecord.getFileUrl());
            result.put("fileName", fileRecord.getOriginalName());
            result.put("fileSize", fileRecord.getFileSize());
            result.put("contentType", fileRecord.getContentType());
            result.put("fileId", fileRecord.getId());
            
            log.info("聊天文件上传成功 - 用户ID: {}, 咨询编号: {}, 文件: {}, 类型: {}, URL: {}", 
                    userId, consultationNo, fileRecord.getOriginalName(), contentType, fileRecord.getFileUrl());
            
            return Result.success(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("聊天文件上传参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("聊天文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    // WebSocket消息处理

    @MessageMapping("/chat/{consultationNo}")
    @SendTo("/topic/consultation/{consultationNo}")
    public ConsultationMessage handleChatMessage(
            @DestinationVariable String consultationNo,
            ConsultationMessage message) {
        
        log.info("收到WebSocket聊天消息 - 问诊编号：{}，消息类型：{}", 
                consultationNo, message.getMessageType());
        
        // 这里可以添加消息处理逻辑，比如持久化到数据库
        // 现在先简单返回消息
        return message;
    }
} 