package org.dromara.common.encrypt.core.encryptor;

// 导入字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 导入加密上下文类
import org.dromara.common.encrypt.core.EncryptContext;
// 导入算法类型枚举
import org.dromara.common.encrypt.enumd.AlgorithmType;
// 导入编码类型枚举
import org.dromara.common.encrypt.enumd.EncodeType;
// 导入加密工具类
import org.dromara.common.encrypt.utils.EncryptUtils;

/**
 * SM2加密器实现类
 * 实现SM2国密非对称加密算法，使用公钥加密、私钥解密
 *
 * @author 老马
 * @version 4.6.0
 */
public class Sm2Encryptor extends AbstractEncryptor {

    /**
     * 加密上下文
     * 存储SM2加密所需的公钥、私钥等配置信息
     */
    private final EncryptContext context;

    /**
     * 构造函数
     * 初始化SM2加密器，验证公私钥配置
     *
     * @param context 加密上下文，包含公钥和私钥
     */
    public Sm2Encryptor(EncryptContext context) {
        // 调用父类构造函数
        super(context);
        // 获取私钥
        String privateKey = context.getPrivateKey();
        // 获取公钥
        String publicKey = context.getPublicKey();
        // 如果公钥或私钥为空，抛出异常
        if (StringUtils.isAnyEmpty(privateKey, publicKey)) {
            throw new IllegalArgumentException("SM2公私钥均需要提供，公钥加密，私钥解密。");
        }
        // 保存加密上下文
        this.context = context;
    }

    /**
     * 获取当前算法类型
     * 返回SM2算法类型
     */
    @Override
    public AlgorithmType algorithm() {
        // 返回SM2算法枚举
        return AlgorithmType.SM2;
    }

    /**
     * 加密方法
     * 使用SM2公钥加密字符串
     *
     * @param value      待加密的明文字符串
     * @param encodeType 加密后的编码格式，支持BASE64和HEX
     * @return 加密后的字符串
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        // 如果编码类型为HEX
        if (encodeType == EncodeType.HEX) {
            // 使用HEX编码方式加密
            return EncryptUtils.encryptBySm2Hex(value, context.getPublicKey());
        } else {
            // 使用BASE64编码方式加密
            return EncryptUtils.encryptBySm2(value, context.getPublicKey());
        }
    }

    /**
     * 解密方法
     * 使用SM2私钥解密密文
     *
     * @param value 待解密的密文字符串
     * @return 解密后的明文字符串
     */
    @Override
    public String decrypt(String value) {
        // 调用加密工具类解密，使用上下文中的私钥
        return EncryptUtils.decryptBySm2(value, context.getPrivateKey());
    }
}
