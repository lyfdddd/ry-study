package org.dromara.common.encrypt.annotation;

// 导入加密算法枚举
import org.dromara.common.encrypt.enumd.AlgorithmType;
// 导入编码方式枚举
import org.dromara.common.encrypt.enumd.EncodeType;

// 导入Java语言注解相关类
import java.lang.annotation.*;

/**
 * 字段加密注解
 * 用于标记需要加密的字段，支持多种加密算法
 *
 * @author 老马
 */
// 表示该注解会被javadoc工具记录
@Documented
// 表示该注解可以被子类继承
@Inherited
// 指定注解作用目标为字段
@Target({ElementType.FIELD})
// 指定注解保留策略为运行时，可通过反射获取
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {

    /**
     * 加密算法类型
     * 默认为DEFAULT，支持AES、RSA、SM2、SM4等
     */
    AlgorithmType algorithm() default AlgorithmType.DEFAULT;

    /**
     * 加密密钥
     * AES、SM4对称加密算法需要配置此密钥
     */
    String password() default "";

    /**
     * 公钥
     * RSA、SM2非对称加密算法需要配置公钥
     */
    String publicKey() default "";

    /**
     * 私钥
     * RSA、SM2非对称加密算法需要配置私钥
     */
    String privateKey() default "";

    /**
     * 编码方式
     * 加密后的数据编码方式，支持BASE64、HEX等
     * 对加密算法为BASE64的不起作用
     */
    EncodeType encode() default EncodeType.DEFAULT;

}
