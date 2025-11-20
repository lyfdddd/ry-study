package org.dromara.common.doc.handler;

// 导入Hutool的IO工具类
import cn.hutool.core.io.IoUtil;
// 导入Swagger的类型名称解析器
import io.swagger.v3.core.jackson.TypeNameResolver;
// 导入Swagger的注解工具类
import io.swagger.v3.core.util.AnnotationsUtils;
// 导入Swagger的标签注解
import io.swagger.v3.oas.annotations.tags.Tags;
// 导入OpenAPI组件模型
import io.swagger.v3.oas.models.Components;
// 导入OpenAPI模型
import io.swagger.v3.oas.models.OpenAPI;
// 导入操作模型
import io.swagger.v3.oas.models.Operation;
// 导入路径模型
import io.swagger.v3.oas.models.Paths;
// 导入标签模型
import io.swagger.v3.oas.models.tags.Tag;
// 导入Lombok的Slf4j注解，自动生成日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Apache Commons Lang3的字符串工具类
import org.apache.commons.lang3.StringUtils;
// 导入Stream工具类
import org.dromara.common.core.utils.StreamUtils;
// 导入OpenAPI构建自定义器
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
// 导入服务器基础URL自定义器
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
// 导入SpringDoc配置属性
import org.springdoc.core.properties.SpringDocConfigProperties;
// 导入JavaDoc提供者
import org.springdoc.core.providers.JavadocProvider;
// 导入OpenAPI服务
import org.springdoc.core.service.OpenAPIService;
// 导入安全服务
import org.springdoc.core.service.SecurityService;
// 导入属性解析工具类
import org.springdoc.core.utils.PropertyResolverUtils;
// 导入Spring应用上下文
import org.springframework.context.ApplicationContext;
// 导入注解元素工具类
import org.springframework.core.annotation.AnnotatedElementUtils;
// 导入集合工具类
import org.springframework.util.CollectionUtils;
// 导入处理器方法
import org.springframework.web.method.HandlerMethod;

// 导入字符串读取器
import java.io.StringReader;
// 导入方法反射类
import java.lang.reflect.Method;
// 导入ArrayList集合类
import java.util.ArrayList;
// 导入Collections工具类
import java.util.Collections;
// 导入HashMap集合类
import java.util.HashMap;
// 导入HashSet集合类
import java.util.HashSet;
// 导入List集合接口
import java.util.List;
// 导入Locale类
import java.util.Locale;
// 导入Map集合接口
import java.util.Map;
// 导入Optional类
import java.util.Optional;
// 导入Set集合接口
import java.util.Set;
// 导入Collectors收集器
import java.util.stream.Collectors;
// 导入Stream流
import java.util.stream.Stream;

/**
 * 自定义OpenAPI处理器
 * 继承OpenAPIService并对源码功能进行修改增强
 * 主要用于自定义API文档的生成逻辑
 */
// Lombok注解，自动生成日志对象
@Slf4j
// 抑制所有警告
@SuppressWarnings("all")
public class OpenApiHandler extends OpenAPIService {

    /**
     * 基础错误控制器类
     */
    private static Class<?> basicErrorController;

    /**
     * 安全解析器
     * 用于解析安全相关注解
     */
    private final SecurityService securityParser;

    /**
     * 映射Map
     * 存储URL路径与处理器的映射关系
     */
    private final Map<String, Object> mappingsMap = new HashMap<>();

    /**
     * SpringDoc标签Map
     * 存储处理器方法与标签的映射关系
     */
    private final Map<HandlerMethod, Tag> springdocTags = new HashMap<>();

    /**
     * OpenAPI构建自定义器列表
     * 用于自定义OpenAPI构建过程
     */
    private final Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers;

    /**
     * 服务器基础URL自定义器列表
     * 用于自定义服务器基础URL
     */
    private final Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers;

    /**
     * SpringDoc配置属性
     */
    private final SpringDocConfigProperties springDocConfigProperties;

    /**
     * 缓存的OpenAPI Map
     * 按分组缓存OpenAPI对象，提升性能
     */
    private final Map<String, OpenAPI> cachedOpenAPI = new HashMap<>();

    /**
     * 属性解析工具类
     * 用于解析配置文件中的占位符
     */
    private final PropertyResolverUtils propertyResolverUtils;

    /**
     * JavaDoc提供者
     * 用于获取类和方法的JavaDoc注释
     */
    private final Optional<JavadocProvider> javadocProvider;

    /**
     * Spring应用上下文
     */
    private ApplicationContext context;

    /**
     * OpenAPI对象
     */
    private OpenAPI openAPI;

    /**
     * 是否已配置服务器
     */
    private boolean isServersPresent;

    /**
     * 服务器基础URL
     */
    private String serverBaseUrl;

    /**
     * 构造函数
     * 初始化OpenAPI处理器
     *
     * @param openAPI                   OpenAPI对象
     * @param securityParser            安全解析器
     * @param springDocConfigProperties SpringDoc配置属性
     * @param propertyResolverUtils     属性解析工具类
     * @param openApiBuilderCustomizers OpenAPI构建自定义器列表
     * @param serverBaseUrlCustomizers  服务器基础URL自定义器列表
     * @param javadocProvider           JavaDoc提供者
     */
    public OpenApiHandler(Optional<OpenAPI> openAPI, SecurityService securityParser,
                          SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils,
                          Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers,
                          Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers,
                          Optional<JavadocProvider> javadocProvider) {
        // 调用父类构造函数
        super(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils, openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);
        // 如果OpenAPI对象存在
        if (openAPI.isPresent()) {
            // 获取OpenAPI对象
            this.openAPI = openAPI.get();
            // 如果组件为空，创建新的组件
            if (this.openAPI.getComponents() == null)
                this.openAPI.setComponents(new Components());
            // 如果路径为空，创建新的路径
            if (this.openAPI.getPaths() == null)
                this.openAPI.setPaths(new Paths());
            // 如果服务器列表不为空，标记为已配置服务器
            if (!CollectionUtils.isEmpty(this.openAPI.getServers()))
                this.isServersPresent = true;
        }
        // 初始化属性解析工具类
        this.propertyResolverUtils = propertyResolverUtils;
        // 初始化安全解析器
        this.securityParser = securityParser;
        // 初始化SpringDoc配置属性
        this.springDocConfigProperties = springDocConfigProperties;
        // 初始化OpenAPI构建自定义器
        this.openApiBuilderCustomisers = openApiBuilderCustomizers;
        // 初始化服务器基础URL自定义器
        this.serverBaseUrlCustomizers = serverBaseUrlCustomizers;
        // 初始化JavaDoc提供者
        this.javadocProvider = javadocProvider;
        // 如果配置使用完全限定名
        if (springDocConfigProperties.isUseFqn())
            // 设置类型名称解析器使用完全限定名
            TypeNameResolver.std.setUseFqn(true);
    }

    /**
     * 构建标签
     * 从处理器方法中提取标签信息并设置到操作中
     *
     * @param handlerMethod 处理器方法
     * @param operation     操作对象
     * @param openAPI       OpenAPI对象
     * @param locale        本地化信息
     * @return 操作对象
     */
    @Override
    public Operation buildTags(HandlerMethod handlerMethod, Operation operation, OpenAPI openAPI, Locale locale) {
        // 创建标签集合
        Set<Tag> tags = new HashSet<>();
        // 创建标签字符串集合
        Set<String> tagsStr = new HashSet<>();

        // 从方法构建标签
        buildTagsFromMethod(handlerMethod.getMethod(), tags, tagsStr, locale);
        // 从类构建标签
        buildTagsFromClass(handlerMethod.getBeanType(), tags, tagsStr, locale);

        // 如果标签字符串集合不为空
        if (!CollectionUtils.isEmpty(tagsStr))
            // 解析占位符并收集到集合中
            tagsStr = tagsStr.stream()
                .map(str -> propertyResolverUtils.resolve(str, locale))
                .collect(Collectors.toSet());

        // 如果springdocTags中包含该处理器方法
        if (springdocTags.containsKey(handlerMethod)) {
            // 获取标签
            io.swagger.v3.oas.models.tags.Tag tag = springdocTags.get(handlerMethod);
            // 添加标签名称
            tagsStr.add(tag.getName());
            // 如果OpenAPI的标签列表为空或不包含该标签
            if (openAPI.getTags() == null || !openAPI.getTags().contains(tag)) {
                // 添加标签到OpenAPI
                openAPI.addTagsItem(tag);
            }
        }

        // 如果标签字符串集合不为空
        if (!CollectionUtils.isEmpty(tagsStr)) {
            // 如果操作的标签为空
            if (CollectionUtils.isEmpty(operation.getTags()))
                // 设置操作的标签
                operation.setTags(new ArrayList<>(tagsStr));
            else {
                // 创建操作标签集合
                Set<String> operationTagsSet = new HashSet<>(operation.getTags());
                // 添加所有标签
                operationTagsSet.addAll(tagsStr);
                // 清空操作的标签
                operation.getTags().clear();
                // 添加合并后的标签
                operation.getTags().addAll(operationTagsSet);
            }
        }

        // 如果是自动标签类
        if (isAutoTagClasses(operation)) {
            // 如果JavaDoc提供者存在
            if (javadocProvider.isPresent()) {
                // 获取类的JavaDoc注释
                String description = javadocProvider.get().getClassJavadoc(handlerMethod.getBeanType());
                // 如果注释不为空
                if (StringUtils.isNotBlank(description)) {
                    // 创建标签
                    io.swagger.v3.oas.models.tags.Tag tag = new io.swagger.v3.oas.models.tags.Tag();

                    // 自定义部分：修改使用java注释当tag名
                    // 读取注释的第一行作为标签名
                    List<String> list = IoUtil.readLines(new StringReader(description), new ArrayList<>());
                    // 设置标签名为注释的第一行
                    tag.setName(list.get(0));
                    // 添加标签到操作
                    operation.addTagsItem(list.get(0));

                    // 设置标签描述
                    tag.setDescription(description);
                    // 如果OpenAPI的标签列表为空或不包含该标签
                    if (openAPI.getTags() == null || !openAPI.getTags().contains(tag)) {
                        // 添加标签到OpenAPI
                        openAPI.addTagsItem(tag);
                    }
                }
            } else {
                // 否则使用类名作为标签名
                String tagAutoName = splitCamelCase(handlerMethod.getBeanType().getSimpleName());
                // 添加标签到操作
                operation.addTagsItem(tagAutoName);
            }
        }

        // 如果标签集合不为空
        if (!CollectionUtils.isEmpty(tags)) {
            // 获取现有的标签
            List<io.swagger.v3.oas.models.tags.Tag> openApiTags = openAPI.getTags();
            // 如果现有的标签不为空
            if (!CollectionUtils.isEmpty(openApiTags))
                // 添加所有标签
                tags.addAll(openApiTags);
            // 设置OpenAPI的标签
            openAPI.setTags(new ArrayList<>(tags));
        }

        // 处理操作级别的安全要求
        io.swagger.v3.oas.annotations.security.SecurityRequirement[] securityRequirements = securityParser
            .getSecurityRequirements(handlerMethod);
        // 如果安全要求不为空
        if (securityRequirements != null) {
            // 如果安全要求长度为0
            if (securityRequirements.length == 0)
                // 设置操作为空安全列表
                operation.setSecurity(Collections.emptyList());
            else
                // 构建安全要求
                securityParser.buildSecurityRequirement(securityRequirements, operation);
        }

        // 返回操作对象
        return operation;
    }

    /**
     * 从方法构建标签
     * 提取方法上的标签注解并添加到集合中
     *
     * @param method   方法
     * @param tags     标签集合
     * @param tagsStr  标签字符串集合
     * @param locale   本地化信息
     */
    private void buildTagsFromMethod(Method method, Set<io.swagger.v3.oas.models.tags.Tag> tags, Set<String> tagsStr, Locale locale) {
        // 查找方法上的所有Tags注解
        Set<Tags> tagsSet = AnnotatedElementUtils
            .findAllMergedAnnotations(method, Tags.class);
        // 提取标签注解
        Set<io.swagger.v3.oas.annotations.tags.Tag> methodTags = tagsSet.stream()
            .flatMap(x -> Stream.of(x.value())).collect(Collectors.toSet());
        // 添加所有标签注解
        methodTags.addAll(AnnotatedElementUtils.findAllMergedAnnotations(method, io.swagger.v3.oas.annotations.tags.Tag.class));
        // 如果方法标签不为空
        if (!CollectionUtils.isEmpty(methodTags)) {
            // 添加标签名称到字符串集合
            tagsStr.addAll(StreamUtils.toSet(methodTags, tag -> propertyResolverUtils.resolve(tag.name(), locale)));
            // 创建标签列表
            List<io.swagger.v3.oas.annotations.tags.Tag> allTags = new ArrayList<>(methodTags);
            // 添加标签到集合
            addTags(allTags, tags, locale);
        }
    }

    /**
     * 添加标签
     * 将注解标签转换为模型标签并添加到集合中
     *
     * @param sourceTags 源标签列表
     * @param tags       目标标签集合
     * @param locale     本地化信息
     */
    private void addTags(List<io.swagger.v3.oas.annotations.tags.Tag> sourceTags, Set<io.swagger.v3.oas.models.tags.Tag> tags, Locale locale) {
        // 获取标签集合
        Optional<Set<io.swagger.v3.oas.models.tags.Tag>> optionalTagSet = AnnotationsUtils
            .getTags(sourceTags.toArray(new io.swagger.v3.oas.annotations.tags.Tag[0]), true);
        // 如果标签集合存在
        optionalTagSet.ifPresent(tagsSet -> {
            // 遍历标签
            tagsSet.forEach(tag -> {
                // 解析标签名称
                tag.name(propertyResolverUtils.resolve(tag.getName(), locale));
                // 解析标签描述
                tag.description(propertyResolverUtils.resolve(tag.getDescription(), locale));
                // 如果标签集合中不存在该名称的标签
                if (tags.stream().noneMatch(t -> t.getName().equals(tag.getName())))
                    // 添加标签到集合
                    tags.add(tag);
            });
        });
    }

}
