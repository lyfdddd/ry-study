package org.dromara.common.log.aspect;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessStatus;
import org.dromara.common.log.event.OperLogEvent;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 操作日志记录处理
 *
 * @author Lion Li
 */
@Slf4j
@Aspect
@AutoConfiguration
public class LogAspect {

    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };


    /**
     * 计时 key
     */
    private static final ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, Log controllerLog) {
        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {

            // *========数据库日志=========*//
            OperLogEvent operLog = new OperLogEvent();
            operLog.setTenantId(LoginHelper.getTenantId());
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = ServletUtils.getClientIP();
            operLog.setOperIp(ip);
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            LoginUser loginUser = LoginHelper.getLoginUser();
            operLog.setOperName(loginUser.getUsername());
            operLog.setDeptName(loginUser.getDeptName());

            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 3800));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 设置消耗时间
            StopWatch stopWatch = KEY_CACHE.get();
            stopWatch.stop();
            operLog.setCostTime(stopWatch.getDuration().toMillis());
            // 发布事件保存数据库
            SpringUtils.context().publishEvent(operLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息
     * 从@Log注解中提取配置信息并设置到操作日志对象中
     *
     * @param joinPoint 切点对象
     * @param log @Log注解实例
     * @param operLog 操作日志事件对象
     * @param jsonResult 方法返回的JSON结果
     * @throws Exception 可能抛出的异常
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperLogEvent operLog, Object jsonResult) throws Exception {
        // 设置业务类型（从枚举转换为整数）
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置模块标题
        operLog.setTitle(log.title());
        // 设置操作人类别（从枚举转换为整数）
        operLog.setOperatorType(log.operatorType().ordinal());
        // 判断是否需要保存请求参数
        if (log.isSaveRequestData()) {
            // 获取请求参数信息并设置到操作日志中
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 判断是否需要保存响应参数且返回结果不为null
        if (log.isSaveResponseData() && ObjectUtil.isNotNull(jsonResult)) {
            // 将返回结果序列化为JSON字符串并截取前3800个字符
            operLog.setJsonResult(StringUtils.substring(JsonUtils.toJsonString(jsonResult), 0, 3800));
        }
    }

    /**
     * 获取请求的参数并设置到操作日志中
     * 根据请求方式不同，采用不同的参数获取策略
     *
     * @param joinPoint 切点对象
     * @param operLog 操作日志事件对象
     * @param excludeParamNames 需要排除的参数名数组
     * @throws Exception 可能抛出的异常
     */
    private void setRequestValue(JoinPoint joinPoint, OperLogEvent operLog, String[] excludeParamNames) throws Exception {
        // 获取请求参数Map（URL参数）
        Map<String, String> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        // 获取HTTP请求方式
        String requestMethod = operLog.getRequestMethod();
        // 如果URL参数为空且是PUT/POST/DELETE请求，从方法参数中获取
        if (MapUtil.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {
            // 将方法参数数组转换为JSON字符串
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            // 截取前3800个字符并设置到操作日志
            operLog.setOperParam(StringUtils.substring(params, 0, 3800));
        } else {
            // 移除敏感属性字段
            MapUtil.removeAny(paramsMap, EXCLUDE_PROPERTIES);
            // 移除自定义排除字段
            MapUtil.removeAny(paramsMap, excludeParamNames);
            // 将参数Map序列化为JSON字符串并截取前3800个字符
            operLog.setOperParam(StringUtils.substring(JsonUtils.toJsonString(paramsMap), 0, 3800));
        }
    }

    /**
     * 将参数数组转换为字符串
     * 处理不同类型的参数对象，过滤敏感信息
     *
     * @param paramsArray 参数对象数组
     * @param excludeParamNames 需要排除的参数名数组
     * @return 处理后的参数字符串
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        // 使用StringJoiner拼接参数字符串，以空格分隔
        StringJoiner params = new StringJoiner(" ");
        // 如果参数数组为空，返回空字符串
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        // 合并默认排除字段和自定义排除字段
        String[] exclude = ArrayUtil.addAll(excludeParamNames, EXCLUDE_PROPERTIES);
        // 遍历参数数组
        for (Object o : paramsArray) {
            // 如果参数不为null且不是需要过滤的对象
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                String str = "";
                // 如果参数是List类型
                if (o instanceof List<?> list) {
                    // 创建List用于存储处理后的Dict对象
                    List<Dict> list1 = new ArrayList<>();
                    // 遍历List中的每个元素
                    for (Object obj : list) {
                        // 将对象序列化为JSON字符串
                        String str1 = JsonUtils.toJsonString(obj);
                        // 将JSON字符串解析为Dict对象
                        Dict dict = JsonUtils.parseMap(str1);
                        // 如果Dict不为空，移除敏感字段
                        if (MapUtil.isNotEmpty(dict)) {
                            MapUtil.removeAny(dict, exclude);
                            list1.add(dict);
                        }
                    }
                    // 将处理后的List序列化为JSON字符串
                    str = JsonUtils.toJsonString(list1);
                } else {
                    // 将对象序列化为JSON字符串
                    str = JsonUtils.toJsonString(o);
                    // 将JSON字符串解析为Dict对象
                    Dict dict = JsonUtils.parseMap(str);
                    // 如果Dict不为空，移除敏感字段
                    if (MapUtil.isNotEmpty(dict)) {
                        MapUtil.removeAny(dict, exclude);
                        // 将处理后的Dict序列化为JSON字符串
                        str = JsonUtils.toJsonString(dict);
                    }
                }
                // 将处理后的参数字符串添加到StringJoiner
                params.add(str);
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象
     * 过滤MultipartFile、HttpServletRequest等不需要记录的对象
     *
     * @param o 待判断的对象
     * @return 如果需要过滤返回true，否则返回false
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        // 获取对象的Class
        Class<?> clazz = o.getClass();
        // 如果是数组类型，判断数组元素类型是否为MultipartFile
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            // 如果是Collection类型，判断集合元素是否为MultipartFile
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            // 如果是Map类型，判断值是否为MultipartFile
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        // 判断对象本身是否为MultipartFile、HttpServletRequest、HttpServletResponse或BindingResult
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
               || o instanceof BindingResult;
    }
}
