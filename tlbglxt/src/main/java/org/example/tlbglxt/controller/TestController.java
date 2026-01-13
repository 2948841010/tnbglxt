package org.example.tlbglxt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.util.DataMigrationUtil;
import org.example.tlbglxt.util.JwtUtil;
import org.example.tlbglxt.util.Md5Util;
import org.example.tlbglxt.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器
 */
@Slf4j
@Tag(name = "系统测试", description = "系统测试相关接口")
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DataMigrationUtil dataMigrationUtil;

    @Operation(summary = "测试接口", description = "测试系统是否正常运行")
    @GetMapping("/hello")
    public Result<String> hello() {
        log.info("测试接口被调用");
        return Result.success("Hello, 糖尿病智能服务管理系统!");
    }
    
    // 添加一个最简单的测试方法，不依赖任何自定义类
    @GetMapping("/simple")
    public String simple() {
        System.out.println("简单测试接口被调用");
        return "Simple test works!";
    }
    
    @Operation(summary = "测试MD5加密", description = "测试MD5密码加密功能")
    @GetMapping("/md5")
    public Result<String> testMd5(@RequestParam(defaultValue = "123456") String password) {
        String encrypted = Md5Util.encrypt(password);
        boolean matches = Md5Util.matches(password, encrypted);
        
        String result = String.format("原密码: %s\n加密后: %s\n验证结果: %s", 
                                     password, encrypted, matches);
        
        log.info("MD5测试 - 原密码: {}, 加密后: {}, 验证: {}", password, encrypted, matches);
        return Result.success("MD5测试成功", result);
    }
    
    @Operation(summary = "测试Redis连接", description = "测试Redis连接是否正常")
    @GetMapping("/redis")
    public Result<String> testRedis() {
        try {
            // 测试Redis连接
            String testKey = "test_connection";
            String testValue = "Redis connection works! " + System.currentTimeMillis();
            
            // 写入测试数据
            redisUtil.set(testKey, testValue, 60);
            
            // 读取测试数据
            Object result = redisUtil.get(testKey);
            
            if (result != null && testValue.equals(result.toString())) {
                // 清理测试数据
                redisUtil.del(testKey);
                
                log.info("Redis连接测试成功");
                return Result.success("Redis连接正常", "写入和读取测试数据成功");
            } else {
                return Result.error("Redis数据读取失败");
            }
            
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return Result.error("Redis连接失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "测试JWT令牌解析", description = "测试JWT令牌的生成和解析功能")
    @GetMapping("/jwt")
    public Result<String> testJwt(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                // 生成一个测试令牌
                String testToken = jwtUtil.generateAccessToken("testuser", 1L);
                String result = String.format("未提供令牌，生成测试令牌：\nBearer %s\n\n请在请求头中添加：\nAuthorization: Bearer %s", 
                                            testToken, testToken);
                return Result.success("JWT测试 - 生成测试令牌", result);
            } else {
                // 解析提供的令牌
                String token = authorization.substring(7);
                
                // 验证令牌
                boolean isValid = jwtUtil.validateToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                long remainingTime = jwtUtil.getRemainingTime(token);
                
                String result = String.format("令牌解析结果：\n有效性: %s\n用户名: %s\n用户ID: %s\n剩余时间: %d秒", 
                                             isValid, username, userId, remainingTime / 1000);
                
                log.info("JWT测试 - 令牌有效性: {}, 用户名: {}, 用户ID: {}", isValid, username, userId);
                return Result.success("JWT测试 - 令牌解析成功", result);
            }
            
        } catch (Exception e) {
            log.error("JWT测试失败", e);
            return Result.error("JWT测试失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("服务运行正常", "OK");
    }

    /**
     * 数据迁移接口 - 为现有记录添加ID
     */
    @Operation(summary = "数据迁移", description = "为现有健康记录添加ID字段")
    @PostMapping("/migrate")
    public Result<String> migrateData() {
        try {
            log.info("开始执行数据迁移...");
            dataMigrationUtil.migrateAllRecords();
            log.info("数据迁移完成");
            return Result.success("数据迁移成功", "已为所有现有记录添加ID字段");
        } catch (Exception e) {
            log.error("数据迁移失败", e);
            return Result.error("数据迁移失败：" + e.getMessage());
        }
    }

    @Operation(summary = "调试Redis在线用户", description = "查看Redis中当前的在线用户keys")
    @GetMapping("/debug-online-users")
    public Result<Object> debugOnlineUsers() {
        try {
            log.info("开始调试Redis在线用户keys");
            
            // 获取所有在线用户keys
            java.util.Set<String> onlineKeys = redisUtil.keys("user:online:*");
            
            java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
            debugInfo.put("totalKeys", onlineKeys != null ? onlineKeys.size() : 0);
            debugInfo.put("keys", onlineKeys);
            
            // 获取每个key的详细信息
            java.util.Map<String, Object> keyDetails = new java.util.HashMap<>();
            if (onlineKeys != null) {
                for (String key : onlineKeys) {
                    java.util.Map<String, Object> keyInfo = new java.util.HashMap<>();
                    keyInfo.put("exists", redisUtil.hasKey(key));
                    keyInfo.put("value", redisUtil.get(key));
                    keyInfo.put("ttl", redisUtil.getExpire(key));
                    keyDetails.put(key, keyInfo);
                }
            }
            debugInfo.put("keyDetails", keyDetails);
            
            log.info("Redis调试信息：{}", debugInfo);
            return Result.success("Redis在线用户调试信息", debugInfo);
            
        } catch (Exception e) {
            log.error("调试Redis在线用户失败", e);
            return Result.error("调试失败：" + e.getMessage());
        }
    }

    @Operation(summary = "清理所有在线用户Redis keys", description = "清理所有user:online:*的Redis keys")
    @PostMapping("/clear-online-users")
    public Result<String> clearOnlineUsers() {
        try {
            log.info("开始清理所有在线用户Redis keys");
            
            // 获取所有在线用户keys
            java.util.Set<String> onlineKeys = redisUtil.keys("user:online:*");
            int clearedCount = 0;
            
            if (onlineKeys != null && !onlineKeys.isEmpty()) {
                for (String key : onlineKeys) {
                    redisUtil.del(key);
                    clearedCount++;
                }
            }
            
            String message = String.format("已清理 %d 个在线用户Redis keys", clearedCount);
            log.info(message);
            return Result.success(message);
            
        } catch (Exception e) {
            log.error("清理在线用户Redis keys失败", e);
            return Result.error("清理失败：" + e.getMessage());
        }
    }
} 