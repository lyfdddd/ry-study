// 测试数据脱敏Controller层
// 演示数据脱敏功能，根据用户角色和权限动态脱敏敏感信息
// 默认管理员不过滤，需自行根据业务重写SensitiveService实现
package org.dromara.demo.controller;

// 统一响应结果封装类，提供标准化的API响应格式
import org.dromara.common.core.domain.R;
// 基础Controller类，提供通用的响应封装方法
import org.dromara.common.web.core.BaseController;
// 敏感字段注解，标记需要脱敏的字段
import org.dromara.common.sensitive.annotation.Sensitive;
// 脱敏策略枚举，定义各种脱敏规则（身份证、手机号、地址等）
import org.dromara.common.sensitive.core.SensitiveStrategy;
// Lombok注解：生成getter、setter、toString等方法
import lombok.Data;
// 脱敏服务接口，定义脱敏逻辑
import org.dromara.common.sensitive.core.SensitiveService;
// Spring Web注解：指定GET请求方法
import org.springframework.web.bind.annotation.GetMapping;
// Spring Web注解：指定请求路径
import org.springframework.web.bind.annotation.RequestMapping;
// Spring Web注解：指定RESTful控制器
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试数据脱敏控制器
 * 演示数据脱敏功能，根据用户角色和权限动态脱敏敏感信息
 * <p>
 * 默认管理员不过滤（isSensitive方法返回false）
 * 需自行根据业务重写SensitiveService实现
 *
 * @author Lion Li
 * @version 3.6.0
 * @see SensitiveService
 */
// Spring注解：组合@Controller和@ResponseBody，表示RESTful控制器
@RestController
// Spring注解：指定请求路径前缀为/demo/sensitive
@RequestMapping("/demo/sensitive")
// 测试数据脱敏Controller，继承BaseController
public class TestSensitiveController extends BaseController {

    /**
     * 测试数据脱敏接口
     * 返回包含敏感信息的对象，Jackson序列化时自动脱敏
     * 脱敏规则根据当前用户的角色和权限动态判断
     *
     * @return 统一响应结果，包含脱敏后的数据
     */
    // Spring注解：指定GET请求方法，路径为/test
    @GetMapping("/test")
    // 测试数据脱敏
    public R<TestSensitive> test() {
        // 创建测试对象
        TestSensitive testSensitive = new TestSensitive();
        // 设置身份证信息（实际场景从数据库查询）
        testSensitive.setIdCard("210397198608215431");
        // 设置手机号信息
        testSensitive.setPhone("17640125371");
        // 设置地址信息
        testSensitive.setAddress("北京市朝阳区某某四合院1203室");
        // 设置邮箱信息
        testSensitive.setEmail("17640125371@163.com");
        // 设置银行卡信息
        testSensitive.setBankCard("6226456952351452853");
        // 返回R.ok()包装的结果，Jackson序列化时自动脱敏
        return R.ok(testSensitive);
    }

    /**
     * 测试敏感信息类
     * 包含各种敏感字段，演示不同脱敏策略
     */
    // Lombok注解：生成getter、setter、toString等方法
    @Data
    // 静态内部类，避免污染外部命名空间
    static class TestSensitive {

        /**
         * 身份证号
         * 使用ID_CARD脱敏策略：显示前6位和后4位，中间用*代替
         * 例如：210397********5431
         */
        // 敏感字段注解：指定脱敏策略为身份证号
        @Sensitive(strategy = SensitiveStrategy.ID_CARD)
        private String idCard;

        /**
         * 手机号
         * 使用PHONE脱敏策略：显示前3位和后4位，中间用*代替
         * 例如：176****5371
         * roleKey = "common"：指定角色key，用于SensitiveService判断是否需要脱敏
         */
        // 敏感字段注解：指定脱敏策略为手机号，角色key为common
        @Sensitive(strategy = SensitiveStrategy.PHONE, roleKey = "common")
        private String phone;

        /**
         * 地址
         * 使用ADDRESS脱敏策略：显示前6位，后面用*代替
         * 例如：北京市朝阳区****
         * perms = "system:user:query"：指定权限标识，用于SensitiveService判断是否需要脱敏
         */
        // 敏感字段注解：指定脱敏策略为地址，权限为system:user:query
        @Sensitive(strategy = SensitiveStrategy.ADDRESS, perms = "system:user:query")
        private String address;

        /**
         * 邮箱
         * 使用EMAIL脱敏策略：@前面保留第一位和最后一位，中间用*代替
         * 例如：1*************1@163.com
         * roleKey = "common"：指定角色key
         * perms = "system:user:query1"：指定权限标识
         * 同时指定roleKey和perms时，SensitiveService需要同时满足才脱敏（与逻辑）
         */
        // 敏感字段注解：指定脱敏策略为邮箱，角色key为common，权限为system:user:query1
        @Sensitive(strategy = SensitiveStrategy.EMAIL, roleKey = "common", perms = "system:user:query1")
        private String email;

        /**
         * 银行卡号
         * 使用BANK_CARD脱敏策略：显示前4位和后4位，中间用*代替
         * 例如：6226**********2853
         * roleKey = "common1"：指定角色key
         * perms = "system:user:query"：指定权限标识
         */
        // 敏感字段注解：指定脱敏策略为银行卡号，角色key为common1，权限为system:user:query
        @Sensitive(strategy = SensitiveStrategy.BANK_CARD, roleKey = "common1", perms = "system:user:query")
        private String bankCard;

    }

}
