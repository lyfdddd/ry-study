package org.dromara.common.json.handler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;

/**
 * 超出 JS 最大最小值 处理
 *
 * @author Lion Li
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {

    /**
     * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
     * JavaScript中Number类型的最大安全整数，超过此值会丢失精度
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;
    /**
     * JavaScript中Number类型的最小安全整数，低于此值会丢失精度
     */
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    /**
     * 提供实例，使用单例模式避免重复创建对象
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    /**
     * 构造函数，调用父类构造函数
     * @param rawType 要序列化的数字类型
     */
    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    /**
     * 序列化方法，判断数字是否在JS安全范围内
     * @param value 要序列化的数字值
     * @param gen JSON生成器，用于写入JSON数据
     * @param provider 序列化提供者，提供序列化上下文
     * @throws IOException 当写入JSON数据时发生IO异常
     */
    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 判断数字值是否在JS安全整数范围内
        if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
            // 在安全范围内，按正常数字序列化
            super.serialize(value, gen, provider);
        } else {
            // 超出安全范围，序列化为字符串，防止JavaScript精度丢失
            gen.writeString(value.toString());
        }
    }
}
