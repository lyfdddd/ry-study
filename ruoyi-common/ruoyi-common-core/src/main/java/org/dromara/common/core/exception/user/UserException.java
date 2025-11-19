package org.dromara.common.core.exception.user;

import org.dromara.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 用户信息异常基类
 * 所有用户相关异常的父类，继承自BaseException
 * 指定模块为"user"，用于用户登录、注册、权限等场景
 *
 * @author ruoyi
 */
public class UserException extends BaseException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * 调用父类BaseException的构造方法，指定模块为"user"
     *
     * @param code 错误码，对应国际化资源文件中的key
     * @param args 错误码参数，用于替换国际化消息中的占位符，可变参数
     */
    public UserException(String code, Object... args) {
        // 调用父类构造方法，模块为"user"，错误码和参数由子类传入，默认消息为null
        // 国际化消息会从messages.properties文件中查找，key为code的值
        super("user", code, args, null);
    }
}
