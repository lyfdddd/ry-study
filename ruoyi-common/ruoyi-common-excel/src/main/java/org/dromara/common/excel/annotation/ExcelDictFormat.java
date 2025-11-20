package org.dromara.common.excel.annotation;

import org.dromara.common.core.utils.StringUtils;

import java.lang.annotation.*;

/**
 * Excel字典格式化注解
 * 用于将数据库中的字典值（如0、1、2）转换为可读文本（如男、女、未知）导出到Excel
 * 支持从系统字典表或自定义表达式进行值转换
 *
 * @author Lion Li
 */
// @Target指定注解作用目标为字段（FIELD），表示该注解只能用在类的属性上
@Target({ElementType.FIELD})
// @Retention指定注解保留策略为运行时（RUNTIME），确保在运行期可以通过反射读取
@Retention(RetentionPolicy.RUNTIME)
// @Inherited表示该注解具有继承性，子类会继承父类的该注解
@Inherited
public @interface ExcelDictFormat {

    /**
     * 字典类型，对应系统字典表的type值（如: sys_user_sex）
     * 框架会自动从字典表查询该类型下的键值对进行转换
     * @return 字典类型字符串
     */
    String dictType() default "";

    /**
     * 读取内容转换表达式，格式：键1=值1,键2=值2,键3=值3
     * 例如："0=男,1=女,2=未知"，将0转换为男，1转换为女
     * 当dictType为空时使用此表达式进行转换
     * @return 转换表达式字符串
     */
    String readConverterExp() default "";

    /**
     * 分隔符，用于读取字符串组内容时的分割
     * 默认使用StringUtils.SEPARATOR（逗号）
     * @return 分隔符字符串
     */
    String separator() default StringUtils.SEPARATOR;

}
