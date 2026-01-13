package org.example.tlbglxt.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息请求DTO
 */
@Data
public class SendMessageRequest {
    
    /**
     * 问诊编号
     */
    @NotBlank(message = "问诊编号不能为空")
    private String consultationNo;
    
    /**
     * 消息类型：text-文本，image-图片，voice-语音，video-视频，file-文件
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;
    
    /**
     * 消息内容（文本消息必填）
     */
    private String content;
    
    /**
     * 媒体文件URL（非文本消息必填）
     */
    private String mediaUrl;
    
    /**
     * 媒体文件大小
     */
    private Long mediaSize;
    
    /**
     * 文件名（文件类型消息）
     */
    private String fileName;
    
    /**
     * 文件大小（文件类型消息）
     */
    private Long fileSize;
    
    /**
     * 文件内容类型（文件类型消息）
     */
    private String contentType;
} 