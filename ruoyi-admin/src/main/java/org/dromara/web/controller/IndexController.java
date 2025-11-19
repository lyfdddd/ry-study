package org.dromara.web.controller;

// Sa-Token注解：标记此接口无需认证即可访问
import cn.dev33.satoken.annotation.SaIgnore;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Spring工具类，用于获取应用信息
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// Spring Web注解：GET请求映射
import org.springframework.web.bind.annotation.GetMapping;
// Spring Web注解：REST控制器
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author Lion Li
 */
// Sa-Token注解：标记此控制器所有接口无需认证即可访问
@SaIgnore
// Lombok注解：自动生成final字段的构造方法
@RequiredArgsConstructor
// Spring注解：标记为REST控制器，返回JSON数据
@RestController
public class IndexController {

    /**
     * 访问首页，提示语
     */
    // GET请求映射：根路径/
    @GetMapping("/")
    public String index() {
        // 返回欢迎信息，包含应用名称
        // 提示用户需要通过前端地址访问系统
        return StringUtils.format("欢迎使用{}后台管理框架，请通过前端地址访问。", SpringUtils.getApplicationName());
    }

}
