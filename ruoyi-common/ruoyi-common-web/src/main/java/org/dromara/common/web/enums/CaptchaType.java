package org.dromara.common.web.enums;

// 导入Hutool验证码生成器接口，定义验证码生成规范
// CodeGenerator是Hutool定义的验证码生成器接口，所有生成器都实现此接口
import cn.hutool.captcha.generator.CodeGenerator;
// 导入Hutool数学验证码生成器，用于生成数学表达式验证码（如1+2=?）
// MathGenerator继承自AbstractGenerator，生成数学表达式如"1+2=?"，用户需要计算结果
import cn.hutool.captcha.generator.MathGenerator;
// 导入Hutool随机字符验证码生成器，用于生成随机字符串验证码
// RandomGenerator继承自AbstractGenerator，生成随机字符串如"AbC12"，用户直接输入
import cn.hutool.captcha.generator.RandomGenerator;
// 导入Lombok全参构造函数注解，自动生成包含所有字段的构造函数
// @AllArgsConstructor自动生成包含所有字段的构造函数，参数顺序与字段定义顺序一致
import lombok.AllArgsConstructor;
// 导入Lombok Getter注解，自动生成字段的getter方法
// @Getter自动生成字段的public getter方法，方便获取枚举值
import lombok.Getter;

/**
 * 验证码类型枚举类
 * 定义系统支持的验证码类型，用于在生成验证码时选择不同的生成策略
 * 通过枚举方式管理验证码类型，避免硬编码字符串，提升代码可维护性和类型安全
 *
 * @author Lion Li
 */
// @Getter注解：Lombok自动生成getter方法，方便获取枚举值的clazz属性
// 生成的getter方法为：public Class<? extends CodeGenerator> getClazz() { return clazz; }
@Getter
// @AllArgsConstructor注解：Lombok自动生成全参构造函数，参数为clazz字段
// 生成的构造函数为：CaptchaType(Class<? extends CodeGenerator> clazz) { this.clazz = clazz; }
@AllArgsConstructor
// 验证码类型枚举，定义系统支持的不同验证码生成策略
public enum CaptchaType {

    /**
     * 数学验证码类型
     * 使用MathGenerator生成数学表达式验证码，如"1+2=?"，用户需要计算结果
     * 数学验证码增加了破解难度，因为需要解析表达式并计算结果
     * 适合对安全性要求较高的场景
     */
    // MATH枚举常量，关联MathGenerator.class
    // 当使用MATH类型时，会通过反射创建MathGenerator实例生成验证码
    MATH(MathGenerator.class),

    /**
     * 字符验证码类型
     * 使用RandomGenerator生成随机字符串验证码，如"AbC12"，用户直接输入
     * 字符验证码简单直观，用户输入方便
     * 适合对用户体验要求较高的场景
     */
    // CHAR枚举常量，关联RandomGenerator.class
    // 当使用CHAR类型时，会通过反射创建RandomGenerator实例生成验证码
    CHAR(RandomGenerator.class);

    /**
     * 验证码生成器类类型
     * 存储对应验证码生成器的Class对象，用于通过反射创建实例
     * 使用Class<? extends CodeGenerator>类型约束，确保只能是CodeGenerator的子类
     */
    // final修饰符：确保clazz字段在构造函数初始化后不可修改，保证枚举的不可变性
    // private修饰符：封装字段，通过Lombok生成的getter方法访问
    private final Class<? extends CodeGenerator> clazz;
}
