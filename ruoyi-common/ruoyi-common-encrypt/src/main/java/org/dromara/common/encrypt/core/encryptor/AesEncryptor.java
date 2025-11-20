package org.dromara.common.encrypt.core.encryptor;

// 导入加密上下文类
import org.dromara.common.encrypt.core.EncryptContext;
// 导入算法类型枚举
import org.dromara.common.encrypt.enumd.AlgorithmType;
// 导入编码类型枚举
import org.dromara.common.encrypt.enumd.EncodeType;
// 导入加密工具类
import org.dromara.common.encrypt.utils.EncryptUtils;

/**
 * AES加密器实现类
 * 实现AES对称加密算法，支持BASE64和HEX编码
 *
 * @author 老马
 * @version 4.6.0
 */
public class AesEncryptor extends AbstractEncryptor {

    /**
     * 加密上下文
     * 存储AES加密所需的密钥等配置信息
     */
    private final EncryptContext context;

    /**
     * 构造函数
     * 初始化AES加密器
     *
     * @param context 加密上下文，包含密钥等配置
     */
    public AesEncryptor(EncryptContext context) {
        // 调用父类构造函数
        super(context);
        // 保存加密上下文
        this.context = context;
    }

    /**
     * 获取当前算法类型
     * 返回AES算法类型
     */
    @Override
    public AlgorithmType algorithm() {
        // 返回AES算法枚举
        return AlgorithmType.AES;
    }

    /**
     * 加密方法
     * 使用AES算法加密字符串
     *
     * @param value      待加明文字符串
     * @param encodeType 加密后的编码格式，支持BASE64和HEX
     * @return 加密后的字符串
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        // 如果编码类型为HEX
        if (encodeType == EncodeType.HEX) {
            // 使用HEX编码方式加密
            return EncryptUtils.encryptByAesHex(value, context.getPassword());
        } else {
            // 使用BASE64编码方式加密
            return EncryptUtils.encryptByAes(value, context.getPassword());
        }
    }

    /**
     * 解密方法
     * 使用AES算法解密字符串
     *
     * @param value 待解密密文字符串
     * @return 解密后的明文字符串
     */
    @Override
    public String decrypt(String value) {
        // 调用加密工具类解密，使用上下文中的密钥
        return EncryptUtils.decryptByAes(value, context.getPassword());
    }
}
