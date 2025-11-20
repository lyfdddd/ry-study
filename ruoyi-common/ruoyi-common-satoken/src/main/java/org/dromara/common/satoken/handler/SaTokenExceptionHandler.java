package org.dromara.common.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SaToken异常处理器
 * 统一处理Sa-Token框架抛出的权限和认证异常
 *
 * @author Lion Li
 */
// 使用@Slf4j注解自动生成日志记录器
// @RestControllerAdvice注解表示这是一个全局异常处理器，处理所有Controller抛出的异常
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 权限码异常处理
     * @param e NotPermissionException异常对象
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        // 获取请求URI，用于日志记录
        String requestURI = request.getRequestURI();
        // 记录错误日志，包含请求地址和异常信息
        log.error("请求地址'{}',权限码校验失败'{}'", requestURI, e.getMessage());
        // 返回403状态码，提示用户没有访问权限
        return R.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系管理员授权");
    }

    /**
     * 角色权限异常处理
     * @param e NotRoleException异常对象
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NotRoleException.class)
    public R<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        // 获取请求URI，用于日志记录
        String requestURI = request.getRequestURI();
        // 记录错误日志，包含请求地址和异常信息
        log.error("请求地址'{}',角色权限校验失败'{}'", requestURI, e.getMessage());
        // 返回403状态码，提示用户没有访问权限
        return R.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系管理员授权");
    }

    /**
     * 认证失败异常处理
     * @param e NotLoginException异常对象
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        // 获取请求URI，用于日志记录
        String requestURI = request.getRequestURI();
        // 记录错误日志，包含请求地址和异常信息
        log.error("请求地址'{}',认证失败'{}',无法访问系统资源", requestURI, e.getMessage());
        // 返回401状态码，提示用户认证失败
        return R.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源");
    }

}
