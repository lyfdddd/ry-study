package org.dromara.common.json.utils;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 工具类
 * 提供JSON序列化、反序列化、格式校验等功能
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    /**
     * 静态ObjectMapper实例，从Spring容器中获取
     * 使用SpringUtils.getBean获取，确保使用Spring配置的ObjectMapper
     */
    private static final ObjectMapper OBJECT_MAPPER = SpringUtils.getBean(ObjectMapper.class);

    /**
     * 获取ObjectMapper实例
     * @return ObjectMapper对象
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象转换为JSON格式的字符串
     *
     * @param object 要转换的对象
     * @return JSON格式的字符串，如果对象为null，则返回null
     * @throws RuntimeException 如果转换过程中发生JSON处理异常，则抛出运行时异常
     */
    public static String toJsonString(Object object) {
        // 判断对象是否为null，如果是则返回null
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        try {
            // 使用ObjectMapper将对象序列化为JSON字符串
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // 捕获JSON处理异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象
     *
     * @param text  JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @param <T>   目标对象的泛型类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        // 判断字符串是否为空，如果是则返回null
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            // 使用ObjectMapper将JSON字符串反序列化为指定类型的对象
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            // 捕获IO异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字节数组转换为指定类型的对象
     *
     * @param bytes 字节数组
     * @param clazz 要转换的目标对象类型
     * @param <T>   目标对象的泛型类型
     * @return 转换后的对象，如果字节数组为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        // 判断字节数组是否为空，如果是则返回null
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            // 使用ObjectMapper将字节数组反序列化为指定类型的对象
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            // 捕获IO异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象，支持复杂类型
     *
     * @param text          JSON格式的字符串
     * @param typeReference 指定类型的TypeReference对象，用于处理泛型
     * @param <T>           目标对象的泛型类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        // 判断字符串是否为空，如果是则返回null
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            // 使用ObjectMapper将JSON字符串反序列化为指定类型的对象
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (IOException e) {
            // 捕获IO异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为Dict对象
     * Dict是Hutool提供的类似Map的数据结构
     *
     * @param text JSON格式的字符串
     * @return 转换后的Dict对象，如果字符串为空或者不是JSON格式则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static Dict parseMap(String text) {
        // 判断字符串是否为空，如果是则返回null
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            // 使用ObjectMapper将JSON字符串反序列化为Dict对象
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructType(Dict.class));
        } catch (MismatchedInputException e) {
            // 类型不匹配说明不是json，返回null
            return null;
        } catch (IOException e) {
            // 捕获IO异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为Dict对象的列表
     *
     * @param text JSON格式的字符串
     * @return 转换后的Dict对象的列表，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static List<Dict> parseArrayMap(String text) {
        // 判断字符串是否为空，如果是则返回null
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            // 使用ObjectMapper将JSON字符串反序列化为Dict对象列表
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Dict.class));
        } catch (IOException e) {
            // 捕获IO异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型对象的列表
     *
     * @param text  JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @param <T>   目标对象的泛型类型
     * @return 转换后的对象的列表，如果字符串为空则返回空列表
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        // 判断字符串是否为空，如果是则返回空列表
        if (StringUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            // 使用ObjectMapper将JSON字符串反序列化为指定类型的对象列表
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            // 捕获IO异常并转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断字符串是否为合法 JSON（对象或数组）
     *
     * @param str 待校验字符串
     * @return true = 合法 JSON，false = 非法或空
     */
    public static boolean isJson(String str) {
        // 判断字符串是否为空，如果是则返回false
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            // 尝试将字符串解析为JSON树结构
            OBJECT_MAPPER.readTree(str);
            return true;
        } catch (Exception e) {
            // 解析失败返回false
            return false;
        }
    }

    /**
     * 判断字符串是否为 JSON 对象（{}）
     *
     * @param str 待校验字符串
     * @return true = JSON 对象
     */
    public static boolean isJsonObject(String str) {
        // 判断字符串是否为空，如果是则返回false
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            // 尝试将字符串解析为JSON节点
            JsonNode node = OBJECT_MAPPER.readTree(str);
            // 判断节点是否为对象类型
            return node.isObject();
        } catch (Exception e) {
            // 解析失败返回false
            return false;
        }
    }

    /**
     * 判断字符串是否为 JSON 数组（[]）
     *
     * @param str 待校验字符串
     * @return true = JSON 数组
     */
    public static boolean isJsonArray(String str) {
        // 判断字符串是否为空，如果是则返回false
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            // 尝试将字符串解析为JSON节点
            JsonNode node = OBJECT_MAPPER.readTree(str);
            // 判断节点是否为数组类型
            return node.isArray();
        } catch (Exception e) {
            // 解析失败返回false
            return false;
        }
    }

}
