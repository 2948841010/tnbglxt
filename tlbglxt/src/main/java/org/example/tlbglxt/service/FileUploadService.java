package org.example.tlbglxt.service;

import org.example.tlbglxt.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     *
     * @param file         文件
     * @param category     文件分类
     * @param businessId   业务ID
     * @param businessType 业务类型
     * @param uploaderId   上传者ID
     * @return 文件记录
     */
    FileRecord uploadFile(MultipartFile file, String category, Long businessId, 
                         String businessType, Long uploaderId);

    /**
     * 批量上传文件
     *
     * @param files        文件列表
     * @param category     文件分类
     * @param businessId   业务ID
     * @param businessType 业务类型
     * @param uploaderId   上传者ID
     * @return 文件记录列表
     */
    List<FileRecord> uploadFiles(MultipartFile[] files, String category, Long businessId, 
                                String businessType, Long uploaderId);

    /**
     * 删除文件
     *
     * @param fileId   文件ID
     * @param userId   操作用户ID
     * @return 是否删除成功
     */
    boolean deleteFile(Long fileId, Long userId);

    /**
     * 获取文件记录
     *
     * @param fileId 文件ID
     * @return 文件记录
     */
    FileRecord getFileRecord(Long fileId);

    /**
     * 根据业务ID获取文件列表
     *
     * @param businessId   业务ID
     * @param businessType 业务类型
     * @return 文件记录列表
     */
    List<FileRecord> getFilesByBusiness(Long businessId, String businessType);

    /**
     * 根据分类获取文件列表
     *
     * @param category   文件分类
     * @param businessId 业务ID
     * @return 文件记录列表
     */
    List<FileRecord> getFilesByCategory(String category, Long businessId);

    /**
     * 生成预签名URL用于临时访问
     *
     * @param fileId       文件ID
     * @param expireMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    String generatePresignedUrl(Long fileId, int expireMinutes);
} 