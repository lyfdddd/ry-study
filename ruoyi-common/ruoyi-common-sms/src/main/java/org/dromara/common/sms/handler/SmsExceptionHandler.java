// 定义短信异常处理器类的包路径
package org.dromara.common.sms.handler;

// 引入Hutool的HTTP状态码常量
import cn.hutool.http.HttpStatus;
// 引入Jakarta Servlet请求接口
import jakarta.servlet.http.HttpServletRequest;
// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 引入统一返回结果类
import org.dromara.common.core.domain.R;
// 引入Sms4j框架的短信混合异常类
import org.dromara.sms4j.comm.exception.SmsBlendException;
// Spring Web异常处理器注解
import org.springframework.web.bind.annotation.ExceptionHandler;
// Spring Web全局异常处理注解
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SMS短信异常处理器
 * 全局捕获和处理短信发送异常，返回友好的错误提示
 *
 * @author AprilWind
 */
// Lombok日志注解，自动生成slf4j日志对象
@Slf4j
// Spring Web全局异常处理注解，捕获所有Controller抛出的异常
@RestControllerAdvice
// SMS异常处理器类
public class SmsExceptionHandler {

    /**
     * 处理SmsBlendException短信异常
     * 捕获短信发送过程中抛出的SmsBlendException异常
     * 记录错误日志并返回统一的错误响应
     *
     * @param e 短信异常对象
     * @param request HTTP请求对象
     * @return 统一返回结果，包含错误信息
     */
    @ExceptionHandler(SmsBlendException.class)
    public R<Void> handleSmsBlendException(SmsBlendException e, HttpServletRequest request) {
        // 获取当前请求的URI路径
        String requestURI = request.getRequestURI();
        // 记录错误日志，包含请求地址和异常堆栈
        log.error("请求地址'{}',发生sms短信异常.", requestURI, e);
        // 返回失败响应，状态码为500内部服务器错误，提示短信发送失败
        return R.fail(HttpStatus.HTTP_INTERNAL_ERROR, "短信发送失败，请稍后再试...");
    }

}
