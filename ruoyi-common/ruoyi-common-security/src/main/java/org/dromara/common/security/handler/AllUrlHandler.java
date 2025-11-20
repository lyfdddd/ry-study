// 定义URL处理器类的包路径
package org.dromara.common.security.handler;

// Hutool正则表达式工具类
import cn.hutool.core.util.ReUtil;
// Spring工具类
import org.dromara.common.core.utils.SpringUtils;
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Spring初始化Bean接口
import org.springframework.beans.factory.InitializingBean;
// Spring MVC方法处理器
import org.springframework.web.method.HandlerMethod;
// Spring MVC请求映射信息
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
// Spring MVC请求映射处理器
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

// Java集合类
import java.util.*;
// Java正则表达式类
import java.util.regex.Pattern;

/**
 * 获取所有URL配置处理器
 * 实现InitializingBean接口，在Bean初始化完成后自动执行
 * 用于收集系统中所有的API接口URL，用于权限配置和路由匹配
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// URL处理器类，实现InitializingBean接口
public class AllUrlHandler implements InitializingBean {

    /**
     * 路径变量正则表达式模式
     * 匹配URL中的路径变量，如：/user/{id}、/order/{orderId}
     */
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    /**
     * URL列表
     * 存储处理后的所有URL路径，路径变量被替换为*
     */
    private List<String> urls = new ArrayList<>();

    /**
     * Bean初始化完成后自动调用的方法
     * 从Spring MVC的RequestMappingHandlerMapping中获取所有映射的URL
     * 将路径变量替换为*，便于路由匹配和权限控制
     */
    @Override
    public void afterPropertiesSet() {
        // 使用HashSet去重，避免重复URL
        Set<String> set = new HashSet<>();
        // 从Spring容器中获取RequestMappingHandlerMapping Bean
        RequestMappingHandlerMapping mapping = SpringUtils.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        // 获取所有请求映射信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        // 遍历所有映射信息
        map.keySet().forEach(info -> {
            // 获取注解上的path，将路径变量替换为*
            Objects.requireNonNull(info.getPathPatternsCondition().getPatterns())
                    .forEach(url -> set.add(ReUtil.replaceAll(url.getPatternString(), PATTERN, "*")));
        });
        // 将去重后的URL添加到列表
        urls.addAll(set);
    }

}
