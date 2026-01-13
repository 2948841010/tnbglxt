package org.example.tlbglxt.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.config.OssConfig;
import org.example.tlbglxt.entity.FileRecord;
import org.example.tlbglxt.mapper.FileRecordMapper;
import org.example.tlbglxt.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    @Autowired
    private FileRecordMapper fileRecordMapper;

    @Override
    public FileRecord uploadFile(MultipartFile file, String category, Long businessId, 
                                String businessType, Long uploaderId) {
        // 验证文件
        validateFile(file);

        // 生成文件名和路径
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String fileName = generateFileName(extension);
        String filePath = generateFilePath(category, fileName);

        // 创建文件记录
        FileRecord fileRecord = new FileRecord();
        
        try {
            fileRecord.setOriginalName(originalName);
            fileRecord.setFileName(fileName);
            fileRecord.setFilePath(filePath);
            fileRecord.setFileUrl(ossConfig.getBaseUrl() + "/" + filePath);
            fileRecord.setFileSize(file.getSize());
            fileRecord.setContentType(file.getContentType());
            fileRecord.setExtension(extension);
            fileRecord.setCategory(category);
            fileRecord.setBusinessId(businessId);
            fileRecord.setBusinessType(businessType);
            fileRecord.setStatus(0); // 上传中
            fileRecord.setIsDeleted(0);
            fileRecord.setCreateTime(LocalDateTime.now());
            fileRecord.setUpdateTime(LocalDateTime.now());
            fileRecord.setCreateBy(uploaderId);
            fileRecord.setUpdateBy(uploaderId);

            // 先保存记录到数据库
            fileRecordMapper.insert(fileRecord);

            // 上传文件到OSS
            PutObjectRequest putRequest = new PutObjectRequest(
                ossConfig.getBucketName(), 
                filePath, 
                file.getInputStream()
            );
            
            ossClient.putObject(putRequest);
            
            // 更新上传状态
            fileRecord.setStatus(1); // 上传成功
            fileRecord.setUpdateTime(LocalDateTime.now());
            fileRecordMapper.updateById(fileRecord);

            log.info("文件上传成功: {}", filePath);
            return fileRecord;

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            // 更新失败状态
            if (fileRecord.getId() != null) {
                fileRecord.setStatus(2); // 上传失败
                fileRecord.setUpdateTime(LocalDateTime.now());
                fileRecordMapper.updateById(fileRecord);
            }
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<FileRecord> uploadFiles(MultipartFile[] files, String category, Long businessId, 
                                       String businessType, Long uploaderId) {
        List<FileRecord> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                FileRecord record = uploadFile(file, category, businessId, businessType, uploaderId);
                results.add(record);
            } catch (Exception e) {
                log.error("批量上传中单个文件失败: {}", file.getOriginalFilename(), e);
                // 继续处理其他文件
            }
        }
        
        return results;
    }

    @Override
    public boolean deleteFile(Long fileId, Long userId) {
        try {
            FileRecord fileRecord = fileRecordMapper.selectById(fileId);
            if (fileRecord == null) {
                log.warn("文件记录不存在: {}", fileId);
                return false;
            }

            // 从OSS删除文件
            ossClient.deleteObject(ossConfig.getBucketName(), fileRecord.getFilePath());

            // 软删除数据库记录
            fileRecord.setIsDeleted(1);
            fileRecord.setUpdateTime(LocalDateTime.now());
            fileRecord.setUpdateBy(userId);
            fileRecordMapper.updateById(fileRecord);

            log.info("文件删除成功: {}", fileRecord.getFilePath());
            return true;

        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileRecord getFileRecord(Long fileId) {
        return fileRecordMapper.selectById(fileId);
    }

    @Override
    public List<FileRecord> getFilesByBusiness(Long businessId, String businessType) {
        return fileRecordMapper.selectByBusiness(businessId, businessType);
    }

    @Override
    public List<FileRecord> getFilesByCategory(String category, Long businessId) {
        return fileRecordMapper.selectByCategory(category, businessId);
    }

    @Override
    public String generatePresignedUrl(Long fileId, int expireMinutes) {
        try {
            FileRecord fileRecord = fileRecordMapper.selectById(fileId);
            if (fileRecord == null) {
                throw new IllegalArgumentException("文件记录不存在");
            }

            // 设置过期时间
            Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000);
            
            // 生成预签名URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                ossConfig.getBucketName(), 
                fileRecord.getFilePath()
            );
            request.setExpiration(expiration);
            
            return ossClient.generatePresignedUrl(request).toString();

        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > ossConfig.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("文件大小超过限制");
        }

        // 检查文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ossConfig.isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return StringUtils.hasText(extension) ? uuid + "." + extension : uuid;
    }

    /**
     * 生成文件路径
     */
    private String generateFilePath(String category, String fileName) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = String.format("%d/%02d/%02d", 
            now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        
        if (StringUtils.hasText(category)) {
            return String.format("%s/%s/%s", category, datePath, fileName);
        } else {
            return String.format("general/%s/%s", datePath, fileName);
        }
    }
} 