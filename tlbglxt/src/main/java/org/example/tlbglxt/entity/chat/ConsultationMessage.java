package org.example.tlbglxt.entity.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问诊聊天消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationMessage {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者类型：patient-患者，doctor-医生
     */
    private String senderType;
    
    /**
     * 消息类型：text-文本，image-图片，voice-语音，video-视频，file-文件
     */
    private String messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 媒体文件URL（图片、语音、视频、文件）
     */
    private String mediaUrl;
    
    /**
     * 媒体文件大小（字节）
     */
    private Long mediaSize;
    
    /**
     * 文件名（文件类型消息）
     */
    private String fileName;
    
    /**
     * 文件大小（字节，文件类型消息）
     */
    private Long fileSize;
    
    /**
     * 文件内容类型（文件类型消息）
     */
    private String contentType;
    
    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;
    
    /**
     * 是否已读
     */
    private Boolean isRead = false;
} 