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
 * Base64加密器实现类
 * 实现Base64编码算法，注意Base64不是真正的加密算法，只是编码方式
 *
 * @author 老马
 * @version 4.6.0
 */
public class Base64Encryptor extends AbstractEncryptor {

    /**
     * 构造函数
     * 初始化Base64加密器
     *
     * @param context 加密上下文
     */
    public Base64Encryptor(EncryptContext context) {
        // 调用父类构造函数
        super(context);
    }

    /**
     * 获取当前算法类型
     * 返回BASE64算法类型
     */
    @Override
    public AlgorithmType algorithm() {
        // 返回BASE64算法枚举
        return AlgorithmType.BASE64;
    }

    /**
     * 加密方法
     * 使用Base64编码字符串
     *
     * @param value      待编码字符串
     * @param encodeType 编码格式（Base64忽略此参数）
     * @return Base64编码后的字符串
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        // 调用加密工具类进行Base64编码
        return EncryptUtils.encryptByBase64(value);
    }

    /**
     * 解密方法
     * 解码Base64字符串
     *
     * @param value 待解码的Base64字符串
     * @return 解码后的原始字符串
     */
    @Override
    public String decrypt(String value) {
        // 调用加密工具类解码Base64
        return EncryptUtils.decryptByBase64(value);
    }
}
