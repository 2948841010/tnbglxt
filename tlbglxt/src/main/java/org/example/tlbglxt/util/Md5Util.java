package org.example.tlbglxt.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 *
 * @author 开发团队
 * @since 1.0.0
 */
public class Md5Util {

    private static final String SALT = "tlbglxt_salt_2024"; // 盐值，增加安全性

    /**
     * MD5加密（带盐值）
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            // 密码 + 盐值
            String saltedPassword = password + SALT;
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            
            // 转换为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 验证密码
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 数据库中的加密密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        String encryptedRaw = encrypt(rawPassword);
        return encryptedRaw.equals(encodedPassword);
    }

    /**
     * 生成随机盐值（可选使用）
     *
     * @return 随机盐值
     */
    public static String generateSalt() {
        return Long.toHexString(System.currentTimeMillis()) + 
               Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
} 