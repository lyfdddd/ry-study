// XSS防护校验器实现类，基于Hutool的HTML工具类
package org.dromara.common.core.xss;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 自定义XSS校验注解实现
 * 基于Hutool的HTML工具类，检测输入字符串中是否包含HTML标签
 * 用于防止跨站脚本攻击（XSS），确保用户输入的安全性
 * 实现Jakarta Bean Validation的ConstraintValidator接口
 * 支持在字段、方法参数等级别进行XSS校验
 *
 * @author Lion Li
 */
// XSS校验器实现类，实现ConstraintValidator接口
public class XssValidator implements ConstraintValidator<Xss, String> {

    /**
     * 校验输入字符串是否包含HTML标签
     * 使用Hutool的HtmlUtil.RE_HTML_MARK正则表达式检测HTML标记
     * 如果包含HTML标签，则认为存在XSS风险，返回false
     * 如果不包含HTML标签，则认为安全，返回true
     *
     * @param value 要校验的字符串值
     * @param constraintValidatorContext 校验上下文，可用于构建自定义错误信息
     * @return true表示不包含HTML标签（安全），false表示包含HTML标签（存在XSS风险）
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 使用Hutool的ReUtil工具类进行正则匹配
        // HtmlUtil.RE_HTML_MARK是Hutool预定义的HTML标签正则表达式
        // 如果value包含HTML标签，ReUtil.contains返回true，我们取反返回false
        // 如果value不包含HTML标签，ReUtil.contains返回false，我们取反返回true
        return !ReUtil.contains(HtmlUtil.RE_HTML_MARK, value);
    }

}
