package org.example.tlbglxt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件记录实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class FileRecord {

    /**
     * 文件记录ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 文件分类（avatar-头像, health-健康记录, document-文档等）
     */
    private String category;

    /**
     * 关联的业务ID（用户ID、记录ID等）
     */
    private Long businessId;

    /**
     * 关联的业务类型（user, health_record等）
     */
    private String businessType;

    /**
     * 上传状态（0-上传中, 1-上传成功, 2-上传失败）
     */
    private Integer status;

    /**
     * 是否已删除（0-未删除, 1-已删除）
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建者ID
     */
    private Long createBy;

    /**
     * 更新者ID
     */
    private Long updateBy;

    /**
     * 备注
     */
    private String remark;
} 