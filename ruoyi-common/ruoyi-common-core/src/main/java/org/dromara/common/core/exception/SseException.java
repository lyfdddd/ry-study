package org.dromara.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * SSE（Server-Sent Events）异常类
 * 用于处理服务端推送相关的异常
 * 继承自RuntimeException，使用Lombok注解简化代码
 *
 * @author LionLi
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
public final class SseException extends RuntimeException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     * 业务异常的错误码，用于前端识别具体错误类型
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
     */
    private String detailMessage;

    /**
     * 构造方法：只传错误消息
     * 用于简单的SSE异常场景
     *
     * @param message 错误提示消息
     */
    public SseException(String message) {
        this.message = message;
    }

    /**
     * 构造方法：传错误消息和错误码
     * 用于需要指定错误码的SSE异常场景
     *
     * @param message 错误提示消息
     * @param code 错误码
     */
    public SseException(String message, Integer code) {
        this.message = message;
        this.code = code;
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
    public SseException setMessage(String message) {
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
    public SseException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}
