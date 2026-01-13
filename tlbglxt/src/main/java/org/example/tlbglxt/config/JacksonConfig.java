package org.example.tlbglxt.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson配置类
 * 处理日期时间格式的序列化和反序列化
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册JavaTimeModule
        mapper.registerModule(new JavaTimeModule());
        
        // 创建自定义模块
        SimpleModule customModule = new SimpleModule();
        
        // 自定义LocalDateTime反序列化器（从JSON字符串 -> Java对象）
        customModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateTimeString = p.getValueAsString();
                
                if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
                    return null;
                }
                
                try {
                    // 处理ISO 8601格式 (例如: 2025-09-22T16:00:00.000Z 或 2025-09-22T16:00:00)
                    if (dateTimeString.contains("T")) {
                        if (dateTimeString.endsWith("Z")) {
                            return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
                        } else {
                            return LocalDateTime.parse(dateTimeString);
                        }
                    }
                    
                    // 处理标准格式 (例如: 2025-09-22 16:00:00)
                    if (dateTimeString.contains(" ")) {
                        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                    
                    // 处理日期格式 (例如: 2025-09-22)
                    if (dateTimeString.contains("-")) {
                        return LocalDateTime.parse(dateTimeString + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                    
                    // 最后尝试默认解析
                    return LocalDateTime.parse(dateTimeString);
                    
                } catch (DateTimeParseException e) {
                    throw new IOException("无法解析日期时间格式: " + dateTimeString, e);
                }
            }
        });
        
        // 自定义LocalDateTime序列化器（从Java对象 -> JSON字符串）
        // 统一输出为 "yyyy-MM-dd HH:mm:ss" 格式，前端dayjs可以解析
        customModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            }
        });
        
        mapper.registerModule(customModule);
        
        return mapper;
    }
} 