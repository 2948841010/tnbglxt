package org.example.tlbglxt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 基于Jackson提供JSON序列化和反序列化功能
 */
@Component
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置ObjectMapper
        // 忽略未知属性
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略空值
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * 对象转JSON字符串
     *
     * @param object 待转换对象
     * @return JSON字符串
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON字符串失败", e);
        }
    }

    /**
     * 对象转格式化的JSON字符串（美化输出）
     *
     * @param object 待转换对象
     * @return 格式化的JSON字符串
     */
    public static String toPrettyJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转格式化JSON字符串失败", e);
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param json  JSON字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转对象失败", e);
        }
    }

    /**
     * JSON字符串转对象（复杂类型）
     *
     * @param json          JSON字符串
     * @param typeReference 类型引用
     * @param <T>           泛型类型
     * @return 转换后的对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转对象失败", e);
        }
    }

    /**
     * JSON字符串转List
     *
     * @param json  JSON字符串
     * @param clazz 列表元素类型
     * @param <T>   泛型类型
     * @return 转换后的List
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType listType = typeFactory.constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转List失败", e);
        }
    }

    /**
     * JSON字符串转Map
     *
     * @param json JSON字符串
     * @return 转换后的Map
     */
    public static Map<String, Object> parseMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转Map失败", e);
        }
    }

    /**
     * JSON字符串转Map（指定值类型）
     *
     * @param json       JSON字符串
     * @param valueClass 值类型
     * @param <T>        值泛型类型
     * @return 转换后的Map
     */
    public static <T> Map<String, T> parseMap(String json, Class<T> valueClass) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            return objectMapper.readValue(json, typeFactory.constructMapType(Map.class, String.class, valueClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转Map失败", e);
        }
    }

    /**
     * 解析JSON字符串为JsonNode
     *
     * @param json JSON字符串
     * @return JsonNode对象
     */
    public static JsonNode parseTree(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析JSON树失败", e);
        }
    }

    /**
     * 对象转Map
     *
     * @param object 待转换对象
     * @return Map对象
     */
    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Map转对象
     *
     * @param map   Map对象
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return objectMapper.convertValue(map, clazz);
    }

    /**
     * 对象深拷贝
     *
     * @param object 源对象
     * @param clazz  目标类型
     * @param <T>    泛型类型
     * @return 深拷贝后的对象
     */
    public static <T> T deepCopy(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象深拷贝失败", e);
        }
    }

    /**
     * 验证JSON字符串格式是否正确
     *
     * @param json JSON字符串
     * @return 是否为有效的JSON格式
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 从JSON字符串中提取指定路径的值
     *
     * @param json JSON字符串
     * @param path 路径（用.分隔，如"user.name"）
     * @return 提取的值
     */
    public static Object extractValue(String json, String path) {
        if (json == null || json.isEmpty() || path == null || path.isEmpty()) {
            return null;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            String[] pathParts = path.split("\\.");
            JsonNode currentNode = rootNode;
            
            for (String part : pathParts) {
                if (currentNode == null) {
                    return null;
                }
                currentNode = currentNode.get(part);
            }
            
            return currentNode != null ? currentNode.asText() : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("提取JSON值失败", e);
        }
    }

    /**
     * 合并两个JSON对象
     *
     * @param json1 第一个JSON字符串
     * @param json2 第二个JSON字符串
     * @return 合并后的JSON字符串
     */
    public static String mergeJson(String json1, String json2) {
        if (json1 == null || json1.isEmpty()) {
            return json2;
        }
        if (json2 == null || json2.isEmpty()) {
            return json1;
        }
        try {
            JsonNode node1 = objectMapper.readTree(json1);
            JsonNode node2 = objectMapper.readTree(json2);
            
            if (node1.isObject() && node2.isObject()) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) node1).setAll((com.fasterxml.jackson.databind.node.ObjectNode) node2);
                return objectMapper.writeValueAsString(node1);
            }
            
            return json2; // 如果不是对象类型，返回第二个JSON
        } catch (JsonProcessingException e) {
            throw new RuntimeException("合并JSON失败", e);
        }
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
} 