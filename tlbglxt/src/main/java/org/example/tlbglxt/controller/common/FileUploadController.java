package org.example.tlbglxt.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.entity.FileRecord;
import org.example.tlbglxt.service.FileUploadService;
import org.example.tlbglxt.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传单个文件
     *
     * @param file         文件
     * @param category     文件分类（可选）
     * @param businessId   业务ID（可选）
     * @param businessType 业务类型（可选）
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<FileRecord> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "general") String category,
            @RequestParam(value = "businessId", required = false) Long businessId,
            @RequestParam(value = "businessType", required = false) String businessType) {
        
        try {
            // 获取当前用户ID（这里需要根据实际的认证机制获取）
            Long userId = SecurityUtils.getCurrentUserId();
            
            FileRecord fileRecord = fileUploadService.uploadFile(
                file, category, businessId, businessType, userId
            );
            
            return Result.success(fileRecord);
            
        } catch (IllegalArgumentException e) {
            log.warn("文件上传参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     *
     * @param files        文件列表
     * @param category     文件分类（可选）
     * @param businessId   业务ID（可选）
     * @param businessType 业务类型（可选）
     * @return 上传结果
     */
    @PostMapping("/upload/batch")
    public Result<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false, defaultValue = "general") String category,
            @RequestParam(value = "businessId", required = false) Long businessId,
            @RequestParam(value = "businessType", required = false) String businessType) {
        
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            List<FileRecord> fileRecords = fileUploadService.uploadFiles(
                files, category, businessId, businessType, userId
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("files", fileRecords);
            result.put("total", files.length);
            result.put("success", fileRecords.size());
            result.put("failed", files.length - fileRecords.size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/upload/avatar")
    public Result<FileRecord> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            FileRecord fileRecord = fileUploadService.uploadFile(
                file, "avatar", userId, "user", userId
            );
            
            return Result.success(fileRecord);
            
        } catch (IllegalArgumentException e) {
            log.warn("头像上传参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            boolean success = fileUploadService.deleteFile(fileId, userId);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("文件删除失败");
            }
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/{fileId}")
    public Result<FileRecord> getFileInfo(@PathVariable Long fileId) {
        try {
            FileRecord fileRecord = fileUploadService.getFileRecord(fileId);
            
            if (fileRecord == null) {
                return Result.error("文件不存在");
            }
            
            return Result.success(fileRecord);
            
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据业务获取文件列表
     *
     * @param businessId   业务ID
     * @param businessType 业务类型
     * @return 文件列表
     */
    @GetMapping("/business/{businessId}/{businessType}")
    public Result<List<FileRecord>> getFilesByBusiness(
            @PathVariable Long businessId,
            @PathVariable String businessType) {
        try {
            List<FileRecord> files = fileUploadService.getFilesByBusiness(businessId, businessType);
            return Result.success(files);
            
        } catch (Exception e) {
            log.error("获取业务文件列表失败", e);
            return Result.error("获取业务文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取文件列表
     *
     * @param category   文件分类
     * @param businessId 业务ID（可选）
     * @return 文件列表
     */
    @GetMapping("/category/{category}")
    public Result<List<FileRecord>> getFilesByCategory(
            @PathVariable String category,
            @RequestParam(value = "businessId", required = false) Long businessId) {
        try {
            List<FileRecord> files = fileUploadService.getFilesByCategory(category, businessId);
            return Result.success(files);
            
        } catch (Exception e) {
            log.error("获取分类文件列表失败", e);
            return Result.error("获取分类文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 生成预签名URL
     *
     * @param fileId        文件ID
     * @param expireMinutes 过期时间（分钟，默认60分钟）
     * @return 预签名URL
     */
    @GetMapping("/{fileId}/presigned-url")
    public Result<Map<String, String>> generatePresignedUrl(
            @PathVariable Long fileId,
            @RequestParam(value = "expireMinutes", required = false, defaultValue = "60") int expireMinutes) {
        try {
            String presignedUrl = fileUploadService.generatePresignedUrl(fileId, expireMinutes);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", presignedUrl);
            result.put("expireMinutes", String.valueOf(expireMinutes));
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            return Result.error("生成预签名URL失败: " + e.getMessage());
        }
    }
} 