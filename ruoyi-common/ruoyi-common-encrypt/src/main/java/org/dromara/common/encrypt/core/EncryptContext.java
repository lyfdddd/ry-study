package org.dromara.common.encrypt.core;

// 导入加密算法枚举
import org.dromara.common.encrypt.enumd.AlgorithmType;
// 导入编码方式枚举
import org.dromara.common.encrypt.enumd.EncodeType;
// 导入Lombok的Data注解，自动生成getter、setter等方法
import lombok.Data;

/**
 * 加密上下文类
 * 用于在加密器之间传递必要的加密参数
 *
 * @author 老马
 * @version 4.6.0
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
public class EncryptContext {

    /**
     * 加密算法类型
     * 指定使用的加密算法，如AES、RSA、SM2、SM4等
     */
    private AlgorithmType algorithm;

    /**
     * 安全密钥
     * 对称加密算法（AES、SM4）使用的密钥
     */
    private String password;

    /**
     * 公钥
     * 非对称加密算法（RSA、SM2）使用的公钥
     */
    private String publicKey;

    /**
     * 私钥
     * 非对称加密算法（RSA、SM2）使用的私钥
     */
    private String privateKey;

    /**
     * 编码方式
     * 加密后的数据编码方式，支持BASE64、HEX等
     */
    private EncodeType encode;

}
