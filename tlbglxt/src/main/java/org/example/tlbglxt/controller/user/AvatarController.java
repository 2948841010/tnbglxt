package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.entity.FileRecord;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.exception.BusinessException;
import org.example.tlbglxt.service.FileUploadService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 头像管理控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "头像管理", description = "用户头像上传和管理接口")
@RestController
@RequestMapping("/api/v1/avatar")
public class AvatarController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 上传头像
     */
    @Operation(summary = "上传头像", description = "用户上传新头像")
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorization) {
        
        try {
            // 1. 验证授权令牌并获取用户ID
            Long userId = validateTokenAndGetUserId(authorization);
            
            // 2. 验证文件
            if (file == null || file.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请选择要上传的头像文件");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请上传图片格式的文件");
            }

            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "头像文件大小不能超过5MB");
            }

            // 3. 上传文件到OSS
            FileRecord fileRecord = fileUploadService.uploadFile(
                file, "avatar", userId, "user", userId
            );

            // 4. 更新用户头像字段
            Result<Void> updateResult = userService.updateUserAvatar(userId, fileRecord.getFileUrl());
            if (!updateResult.isSuccess()) {
                // 如果更新用户头像失败，可以考虑删除已上传的文件
                log.warn("更新用户头像字段失败，文件已上传：{}", fileRecord.getFileUrl());
            }

            // 5. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileRecord.getId());
            result.put("fileName", fileRecord.getFileName());
            result.put("fileUrl", fileRecord.getFileUrl());
            result.put("fileSize", fileRecord.getFileSize());

            log.info("用户头像上传成功，用户ID：{}，文件URL：{}", userId, fileRecord.getFileUrl());
            return Result.success("头像上传成功", result);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户头像信息
     */
    @Operation(summary = "获取头像信息", description = "获取当前用户的头像信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getAvatarInfo(
            @RequestHeader("Authorization") String authorization) {
        
        try {
            // 1. 验证授权令牌并获取用户ID
            Long userId = validateTokenAndGetUserId(authorization);
            
            // 2. 获取用户信息
            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            User user = userResult.getData();
            
            // 3. 返回头像信息
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("avatarUrl", user.getAvatar());
            result.put("hasAvatar", user.getAvatar() != null && !user.getAvatar().trim().isEmpty());

            return Result.success("获取头像信息成功", result);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取头像信息失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "获取头像信息失败");
        }
    }

    /**
     * 删除头像（重置为默认头像）
     */
    @Operation(summary = "删除头像", description = "删除当前头像，重置为默认头像")
    @DeleteMapping("/remove")
    public Result<Void> removeAvatar(
            @RequestHeader("Authorization") String authorization) {
        
        try {
            // 1. 验证授权令牌并获取用户ID
            Long userId = validateTokenAndGetUserId(authorization);
            
            // 2. 重置用户头像为空
            Result<Void> updateResult = userService.updateUserAvatar(userId, null);
            if (!updateResult.isSuccess()) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "删除头像失败");
            }

            log.info("用户头像删除成功，用户ID：{}", userId);
            return Result.success("头像删除成功");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除头像失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "删除头像失败");
        }
    }

    /**
     * 验证令牌并获取用户ID
     */
    private Long validateTokenAndGetUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未提供有效的授权令牌");
        }

        String token = authorization.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), "令牌已过期或无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从令牌中获取用户信息");
        }

        return userId;
    }
} 