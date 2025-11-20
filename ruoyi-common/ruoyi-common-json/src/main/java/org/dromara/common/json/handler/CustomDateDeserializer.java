package org.dromara.common.json.handler;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.dromara.common.core.utils.ObjectUtils;

import java.io.IOException;
import java.util.Date;

/**
 * 自定义 Date 类型反序列化处理器（支持多种格式）
 * 使用Hutool的DateUtil解析多种日期格式字符串
 *
 * @author AprilWind
 */
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    /**
     * 反序列化逻辑：将字符串转换为 Date 对象
     * 支持多种日期格式，如yyyy-MM-dd、yyyy-MM-dd HH:mm:ss等
     *
     * @param p    JSON 解析器，用于获取字符串值
     * @param ctxt 上下文环境（可用于获取更多配置）
     * @return 转换后的 Date 对象，若为空字符串返回 null
     * @throws IOException 当字符串格式非法或转换失败时抛出
     */
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 使用Hutool的DateUtil解析日期字符串，自动识别多种格式
        DateTime parse = DateUtil.parse(p.getText());
        // 如果解析结果为null，返回null
        if (ObjectUtils.isNull(parse)) {
            return null;
        }
        // 将Hutool的DateTime转换为JDK的Date对象
        return parse.toJdkDate();
    }

}
