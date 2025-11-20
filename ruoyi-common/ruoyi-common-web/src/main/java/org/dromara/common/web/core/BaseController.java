package org.dromara.common.web.core;

// 导入统一响应结果类，包含成功/失败状态、消息和数据
// R是系统统一的API响应封装类，提供ok()和fail()等便捷方法
import org.dromara.common.core.domain.R;
// 导入字符串工具类，用于字符串格式化
// StringUtils提供字符串操作工具方法，如format、isEmpty等
import org.dromara.common.core.utils.StringUtils;

/**
 * Web层通用数据处理基类
 * 提供Controller层通用的响应处理和页面跳转方法
 * 其他Controller可以继承此类，复用通用的响应处理方法
 * 封装了数据库操作结果判断、业务操作结果判断、重定向等常用功能
 *
 * @author Lion Li
 */
// BaseController作为基类，不提供任何注解，由子类继承使用
// 子类Controller继承后可以直接调用toAjax和redirect方法
public class BaseController {

    /**
     * 根据数据库操作影响行数返回响应结果
     * 用于判断数据库增删改操作是否成功
     * 封装了常见的数据库操作结果判断逻辑，简化Controller代码
     *
     * @param rows 影响行数（数据库操作返回的影响记录数）
     * @return 操作结果，rows>0返回成功响应，否则返回失败响应
     */
    // protected修饰符：子类可以访问，外部类无法直接调用
    // 返回R<Void>类型：统一响应格式，Void表示无数据
    protected R<Void> toAjax(int rows) {
        // 使用三元运算符判断影响行数
        // 如果影响行数大于0表示操作成功，返回R.ok()成功响应
        // 否则表示操作失败，返回R.fail()失败响应
        // 简化Controller中的重复判断逻辑
        return rows > 0 ? R.ok() : R.fail();
    }

    /**
     * 根据布尔结果返回响应结果
     * 用于判断业务操作是否成功
     * 封装了常见的业务操作结果判断逻辑，简化Controller代码
     *
     * @param result 布尔结果（true表示成功，false表示失败）
     * @return 操作结果，result为true返回成功响应，否则返回失败响应
     */
    // protected修饰符：子类可以访问，外部类无法直接调用
    // 返回R<Void>类型：统一响应格式，Void表示无数据
    protected R<Void> toAjax(boolean result) {
        // 使用三元运算符判断布尔结果
        // 如果结果为true表示操作成功，返回R.ok()成功响应
        // 否则表示操作失败，返回R.fail()失败响应
        // 简化Controller中的重复判断逻辑
        return result ? R.ok() : R.fail();
    }

    /**
     * 构建重定向URL字符串
     * 用于Controller方法返回视图名称时实现页面跳转
     * Spring MVC会根据redirect:前缀识别为重定向操作
     * 支持redirect:/path、redirect:http://example.com等格式
     *
     * @param url 目标URL路径
     * @return 重定向字符串，格式：redirect:{url}
     */
    // public修饰符：子类和外部类都可以调用
    // 返回String类型：重定向字符串，Spring MVC特殊处理
    public String redirect(String url) {
        // 使用StringUtils.format方法格式化字符串，添加redirect:前缀
        // 格式：redirect:{url}，Spring MVC会根据redirect:前缀执行重定向操作
        // 而不是视图解析，实现页面跳转
        return StringUtils.format("redirect:{}", url);
    }

}
