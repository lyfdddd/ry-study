package org.dromara.web.controller;

// Sa-Token注解：标记此接口无需认证即可访问，用于验证码等公共接口
import cn.dev33.satoken.annotation.SaIgnore;
// Hutool验证码抽象类，提供验证码生成基础能力
import cn.hutool.captcha.AbstractCaptcha;
// Hutool验证码生成器接口，定义验证码生成规则
import cn.hutool.captcha.generator.CodeGenerator;
// Hutool ID生成工具，用于生成唯一标识
import cn.hutool.core.util.IdUtil;
// Hutool随机数工具，用于生成随机验证码
import cn.hutool.core.util.RandomUtil;
// Jakarta验证注解，用于参数非空校验
import jakarta.validation.constraints.NotBlank;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 系统常量定义
import org.dromara.common.core.constant.Constants;
// 全局常量定义
import org.dromara.common.core.constant.GlobalConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 业务异常类
import org.dromara.common.core.exception.ServiceException;
// Spring工具类，用于获取Bean和ApplicationContext
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 反射工具类，用于动态创建对象
import org.dromara.common.core.utils.reflect.ReflectUtils;
// 邮件配置属性类
import org.dromara.common.mail.config.properties.MailProperties;
// 邮件发送工具类
import org.dromara.common.mail.utils.MailUtils;
// 限流注解：限制接口访问频率
import org.dromara.common.ratelimiter.annotation.RateLimiter;
// 限流类型枚举
import org.dromara.common.ratelimiter.enums.LimitType;
// Redis操作工具类
import org.dromara.common.redis.utils.RedisUtils;
// 验证码配置属性类
import org.dromara.common.web.config.properties.CaptchaProperties;
// 验证码类型枚举
import org.dromara.common.web.enums.CaptchaType;
// SMS4J短信接口
import org.dromara.sms4j.api.SmsBlend;
// SMS4J短信响应实体
import org.dromara.sms4j.api.entity.SmsResponse;
// SMS4J短信工厂类，用于获取短信实例
import org.dromara.sms4j.core.factory.SmsFactory;
// 验证码视图对象
import org.dromara.web.domain.vo.CaptchaVo;
// Spring表达式接口
import org.springframework.expression.Expression;
// Spring表达式解析器
import org.springframework.expression.ExpressionParser;
// Spring标准表达式解析器实现
import org.springframework.expression.spel.standard.SpelExpressionParser;
// Spring验证注解
import org.springframework.validation.annotation.Validated;
// Spring Web注解：GET请求映射
import org.springframework.web.bind.annotation.GetMapping;
// Spring Web注解：REST控制器
import org.springframework.web.bind.annotation.RestController;

// Java时间Duration类，用于设置过期时间
import java.time.Duration;
// LinkedHashMap保证插入顺序
import java.util.LinkedHashMap;

/**
 * 验证码操作处理
 *
 * @author Lion Li
 */
// Sa-Token注解：标记此控制器所有接口无需认证即可访问
@SaIgnore
// Lombok注解：自动生成slf4j日志对象
@Slf4j
// Spring验证注解：启用方法参数验证
@Validated
// Lombok注解：自动生成final字段的构造方法
@RequiredArgsConstructor
// Spring注解：标记为REST控制器，返回JSON数据
@RestController
public class CaptchaController {

    // 注入验证码配置属性对象，用于获取验证码相关配置
    private final CaptchaProperties captchaProperties;
    // 注入邮件配置属性对象，用于获取邮件功能开关状态
    private final MailProperties mailProperties;

    /**
     * 短信验证码
     *
     * @param phonenumber 用户手机号
     */
    // 限流注解：基于手机号限流，60秒内只能请求1次，防止短信轰炸
    @RateLimiter(key = "#phonenumber", time = 60, count = 1)
    // GET请求映射：/resource/sms/code
    @GetMapping("/resource/sms/code")
    public R<Void> smsCode(@NotBlank(message = "{user.phonenumber.not.blank}") String phonenumber) {
        // 构建Redis缓存key，格式：captcha_codes:手机号
        String key = GlobalConstants.CAPTCHA_CODE_KEY + phonenumber;
        // 生成4位数字随机验证码
        String code = RandomUtil.randomNumbers(4);
        // 将验证码存入Redis，设置过期时间（默认5分钟）
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        // 验证码模板id，需要自行处理（可以从数据库查询或写死）
        String templateId = "";
        // 创建有序Map，用于存储短信模板变量
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        // 将验证码放入模板变量
        map.put("code", code);
        // 通过SMS4J工厂获取短信发送实例（config1为配置标识）
        SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");
        // 发送短信验证码
        SmsResponse smsResponse = smsBlend.sendMessage(phonenumber, templateId, map);
        // 判断短信发送是否成功
        if (!smsResponse.isSuccess()) {
            // 记录错误日志，包含完整响应信息
            log.error("验证码短信发送异常 => {}", smsResponse);
            // 返回失败响应，包含错误信息
            return R.fail(smsResponse.getData().toString());
        }
        // 返回成功响应
        return R.ok();
    }

    /**
     * 邮箱验证码
     *
     * @param email 邮箱
     */
    // GET请求映射：/resource/email/code
    @GetMapping("/resource/email/code")
    public R<Void> emailCode(@NotBlank(message = "{user.email.not.blank}") String email) {
        // 检查系统是否开启邮箱功能
        if (!mailProperties.getEnabled()) {
            // 邮箱功能未开启，返回错误提示
            return R.fail("当前系统没有开启邮箱功能！");
        }
        // 通过AOP代理调用实现方法，确保限流注解生效
        // 如果直接调用this.emailCodeImpl()，限流注解会失效（因为绕过了代理）
        SpringUtils.getAopProxy(this).emailCodeImpl(email);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 邮箱验证码
     * 独立方法避免验证码关闭之后仍然走限流
     */
    // 限流注解：基于邮箱限流，60秒内只能请求1次
    @RateLimiter(key = "#email", time = 60, count = 1)
    public void emailCodeImpl(String email) {
        // 构建Redis缓存key，格式：captcha_codes:邮箱
        String key = GlobalConstants.CAPTCHA_CODE_KEY + email;
        // 生成4位数字随机验证码
        String code = RandomUtil.randomNumbers(4);
        // 将验证码存入Redis，设置过期时间（默认5分钟）
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        try {
            // 发送文本邮件，包含验证码和有效期信息
            MailUtils.sendText(email, "登录验证码", "您本次验证码为：" + code + "，有效性为" + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。");
        } catch (Exception e) {
            // 记录错误日志
            log.error("验证码短信发送异常 => {}", e.getMessage());
            // 抛出业务异常
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 生成验证码
     */
    // GET请求映射：/auth/code
    @GetMapping("/auth/code")
    public R<CaptchaVo> getCode() {
        // 从配置中获取验证码开关状态
        boolean captchaEnabled = captchaProperties.getEnable();
        // 如果验证码功能关闭
        if (!captchaEnabled) {
            // 创建验证码视图对象
            CaptchaVo captchaVo = new CaptchaVo();
            // 设置验证码未启用标记
            captchaVo.setCaptchaEnabled(false);
            // 返回响应，前端可根据此标记不显示验证码
            return R.ok(captchaVo);
        }
        // 通过AOP代理调用实现方法，确保限流注解生效
        return R.ok(SpringUtils.getAopProxy(this).getCodeImpl());
    }

    /**
     * 生成验证码
     * 独立方法避免验证码关闭之后仍然走限流
     */
    // 限流注解：基于IP限流，60秒内最多请求10次
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    public CaptchaVo getCodeImpl() {
        // 保存验证码信息
        // 生成UUID作为验证码唯一标识
        String uuid = IdUtil.simpleUUID();
        // 构建Redis缓存key，格式：captcha_codes:UUID
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + uuid;
        // 生成验证码
        // 从配置中获取验证码类型（数学、字符等）
        CaptchaType captchaType = captchaProperties.getType();
        // 验证码生成器
        CodeGenerator codeGenerator;
        // 判断是否为数学验证码
        if (CaptchaType.MATH == captchaType) {
            // 创建数学验证码生成器，指定数字长度，false表示不生成负数
            codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), captchaProperties.getNumberLength(), false);
        } else {
            // 创建字符验证码生成器，指定字符长度
            codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), captchaProperties.getCharLength());
        }
        // 从Spring容器获取验证码实现类实例（如LineCaptcha、CircleCaptcha等）
        AbstractCaptcha captcha = SpringUtils.getBean(captchaProperties.getCategory().getClazz());
        // 设置验证码生成器
        captcha.setGenerator(codeGenerator);
        // 生成验证码内容
        captcha.createCode();
        // 如果是数学验证码，使用SpEL表达式处理验证码结果
        // 获取验证码文本（如"1+2=?"）
        String code = captcha.getCode();
        // 判断是否为数学验证码
        if (CaptchaType.MATH == captchaType) {
            // 创建SpEL表达式解析器
            ExpressionParser parser = new SpelExpressionParser();
            // 解析数学表达式（移除等号，如"1+2"）
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            // 计算表达式结果（如"3"）
            code = exp.getValue(String.class);
        }
        // 将验证码结果存入Redis，设置过期时间
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        // 创建验证码视图对象
        CaptchaVo captchaVo = new CaptchaVo();
        // 设置UUID，用于后续验证时匹配Redis中的验证码
        captchaVo.setUuid(uuid);
        // 设置验证码图片Base64编码字符串，前端可直接显示
        captchaVo.setImg(captcha.getImageBase64());
        // 返回验证码视图对象
        return captchaVo;
    }

}
