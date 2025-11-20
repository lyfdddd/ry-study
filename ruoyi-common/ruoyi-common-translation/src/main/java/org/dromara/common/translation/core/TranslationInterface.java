package org.dromara.common.translation.core;

import org.dromara.common.translation.annotation.TranslationType;

/**
 * 翻译接口 (实现类需标注 {@link TranslationType} 注解标明翻译类型)
 *
 * @author Lion Li
 */
// 泛型翻译接口，定义翻译规范，所有翻译实现类都需要实现此接口
public interface TranslationInterface<T> {

    /**
     * 翻译
     *
     * @param key   需要被翻译的键(不为空)
     * @param other 其他参数
     * @return 返回键对应的值
     */
    // 翻译方法，将key转换为对应的翻译值
    // key: 需要翻译的原始值，如用户ID、部门ID等
    // other: 其他附加参数，如字典类型等
    // 返回: 翻译后的结果，如用户名、部门名称等
    T translation(Object key, String other);
}
