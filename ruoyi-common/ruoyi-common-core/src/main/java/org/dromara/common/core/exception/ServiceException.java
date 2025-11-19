package org.dromara.common.core.exception;

import cn.hutool.core.text.StrFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 业务异常类（支持占位符 {} ）
 * 用于抛出业务逻辑相关的异常，支持字符串格式化占位符
 * 继承自RuntimeException，无需显式捕获
 * 使用Lombok注解简化代码
 *
 * @author ruoyi
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：重写父类的equals和hashCode方法
@EqualsAndHashCode(callSuper = true)
// Lombok注解：生成无参构造函数
@NoArgsConstructor
// Lombok注解：生成全参构造函数
@AllArgsConstructor
// final修饰：该类不可被继承
public final class ServiceException extends RuntimeException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     * 业务异常的错误码，用于前端识别具体错误类型
     * 例如：400-参数错误，401-未授权，403-无权限，404-资源不存在
     */
    private Integer code;

    /**
     * 错误提示消息
     * 给用户看的友好错误提示
     */
    private String message;

    /**
     * 错误明细消息
     * 内部调试使用的详细错误信息，不暴露给用户
     * 用于记录日志和排查问题
     */
    private String detailMessage;

    /**
     * 构造方法：只传错误消息
     * 用于简单的业务异常场景
     *
     * @param message 错误提示消息
     */
    public ServiceException(String message) {
        this.message = message;
    }

    /**
     * 构造方法：传错误消息和错误码
     * 用于需要指定错误码的业务异常场景
     *
     * @param message 错误提示消息
     * @param code 错误码
     */
    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     * 构造方法：支持字符串格式化占位符
     * 使用Hutool的StrFormatter格式化消息，支持{}占位符
     * 例如：new ServiceException("用户{}不存在", username)
     *
     * @param message 错误提示消息模板
     * @param args 格式化参数
     */
    public ServiceException(String message, Object... args) {
        this.message = StrFormatter.format(message, args);
    }

    /**
     * 重写父类的getMessage方法
     * 返回自定义的错误消息
     *
     * @return 错误提示消息
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息
     * 支持链式调用，方便在抛出异常前动态设置消息
     *
     * @param message 错误提示消息
     * @return 当前异常对象
     */
    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置详细错误消息
     * 支持链式调用，方便在抛出异常前动态设置详细消息
     *
     * @param detailMessage 详细错误消息
     * @return 当前异常对象
     */
    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}
