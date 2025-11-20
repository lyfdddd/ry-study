// 定义MyBatis异常处理器包路径，统一处理MyBatis相关异常
package org.dromara.common.mybatis.handler;

// Sa-Token未登录异常，用于处理认证失败场景
import cn.dev33.satoken.exception.NotLoginException;
// Hutool HTTP状态码常量
import cn.hutool.http.HttpStatus;
// 动态数据源异常，当找不到指定数据源时抛出
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
// Jakarta Servlet请求对象，用于获取请求信息
import jakarta.servlet.http.HttpServletRequest;
// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 统一响应结果类
import org.dromara.common.core.domain.R;
// MyBatis系统异常，MyBatis操作失败时抛出
import org.mybatis.spring.MyBatisSystemException;
// Spring数据访问异常，主键重复时抛出
import org.springframework.dao.DuplicateKeyException;
// Spring异常处理注解，标记方法处理指定异常
import org.springframework.web.bind.annotation.ExceptionHandler;
// Spring全局异常处理注解，标记类为全局异常处理器
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * MyBatis全局异常处理器
 * 统一处理MyBatis相关的异常，如主键重复、数据源不存在、未登录等
 * 使用@RestControllerAdvice注解，捕获所有Controller抛出的异常
 *
 * @author Lion Li
 */
// Lombok日志注解：自动生成slf4j日志对象log
@Slf4j
// Spring全局异常处理注解：捕获所有Controller抛出的异常
@RestControllerAdvice
public class MybatisExceptionHandler {

    /**
     * 处理主键或UNIQUE索引数据重复异常
     * 当插入或更新数据时，如果违反主键或唯一索引约束，抛出此异常
     *
     * @param e       DuplicateKeyException异常对象
     * @param request HTTP请求对象，用于获取请求URI
     * @return 统一响应结果，状态码为409（冲突）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        // 获取请求URI，用于日志记录
        String requestURI = request.getRequestURI();
        // 记录错误日志，包含请求地址和异常信息
        log.error("请求地址'{}',数据库中已存在记录'{}'", requestURI, e.getMessage());
        // 返回失败响应，状态码409（冲突），提示用户记录已存在
        return R.fail(HttpStatus.HTTP_CONFLICT, "数据库中已存在该记录，请联系管理员确认");
    }

    /**
     * 处理MyBatis系统异常
     * 通用处理MyBatis操作失败的各种场景，如未登录、数据源不存在等
     *
     * @param e       MyBatisSystemException异常对象
     * @param request HTTP请求对象，用于获取请求URI
     * @return 统一响应结果，根据异常类型返回不同状态码
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public R<Void> handleCannotFindDataSourceException(MyBatisSystemException e, HttpServletRequest request) {
        // 获取请求URI
        String requestURI = request.getRequestURI();
        // 获取异常的根本原因
        Throwable root = getRootCause(e);
        
        // 判断根本原因是否为未登录异常
        if (root instanceof NotLoginException) {
            // 记录认证失败日志
            log.error("请求地址'{}',认证失败'{}',无法访问系统资源", requestURI, root.getMessage());
            // 返回未授权响应，状态码401
            return R.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源");
        }
        
        // 判断根本原因是否为数据源不存在异常
        if (root instanceof CannotFindDataSourceException) {
            // 记录数据源未找到日志
            log.error("请求地址'{}', 未找到数据源", requestURI);
            // 返回服务器错误响应，状态码500
            return R.fail(HttpStatus.HTTP_INTERNAL_ERROR, "未找到数据源，请联系管理员确认");
        }
        
        // 其他MyBatis系统异常，记录详细错误日志
        log.error("请求地址'{}', Mybatis系统异常", requestURI, e);
        // 返回服务器错误响应，包含异常消息
        return R.fail(HttpStatus.HTTP_INTERNAL_ERROR, e.getMessage());
    }

    /**
     * 获取异常的根因（递归查找最底层的cause）
     * 用于追溯异常链，找到最初抛出异常的原因
     *
     * @param e 当前异常
     * @return 根因异常（最底层的 cause）
     * <p>
     * 逻辑说明：
     * 1. 如果 e 没有 cause，说明 e 本身就是根因，直接返回
     * 2. 如果 e 的 cause 和自身相同（防止循环引用），也返回 e
     * 3. 否则递归调用，继续向下寻找最底层的 cause
     */
    public static Throwable getRootCause(Throwable e) {
        // 获取当前异常的cause
        Throwable cause = e.getCause();
        // 如果cause为null或与自身相同（防止循环引用），返回当前异常
        if (cause == null || cause == e) {
            return e;
        }
        // 递归调用，继续查找更深层的cause
        return getRootCause(cause);
    }

    /**
     * 在异常链中查找指定类型的异常
     * 用于判断异常链中是否包含特定类型的异常
     *
     * @param e     当前异常
     * @param clazz 目标异常类
     * @return 找到的指定类型异常，如果没有找到返回 null
     */
    public static Throwable findCause(Throwable e, Class<? extends Throwable> clazz) {
        // 从当前异常开始遍历
        Throwable t = e;
        // 循环直到t为null或t的cause与自身相同（防止无限循环）
        while (t != null && t != t.getCause()) {
            // 判断当前异常是否为目标类型的实例
            if (clazz.isInstance(t)) {
                // 找到目标异常，返回
                return t;
            }
            // 移动到下一个cause
            t = t.getCause();
        }
        // 未找到目标异常，返回null
        return null;
    }

}
