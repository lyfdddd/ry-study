// 正则字段校验器包声明，属于核心工具包
package org.dromara.common.core.utils.regex;

// 导入Hutool验证异常类，用于抛出验证失败异常
import cn.hutool.core.exceptions.ValidateException;
// 导入Hutool验证器基类，提供基础验证能力
import cn.hutool.core.lang.Validator;
// 导入正则表达式模式池工厂类，提供预定义的正则模式
import org.dromara.common.core.factory.RegexPatternPoolFactory;

// 导入Java正则表达式核心类
import java.util.regex.Pattern;

/**
 * 正则字段校验器
 * 主要验证字段非空、是否为满足指定格式等
 *
 * @author Feng
 */
// 继承Hutool的Validator，复用其基础验证能力
public class RegexValidator extends Validator {

    /**
     * 字典类型必须以字母开头，且只能为（小写字母，数字，下滑线）
     * 使用预编译的正则表达式模式，提升性能
     */
    public static final Pattern DICTIONARY_TYPE = RegexPatternPoolFactory.DICTIONARY_TYPE;

    /**
     * 身份证号码（后6位）
     * 用于验证身份证后6位格式
     */
    public static final Pattern ID_CARD_LAST_6 = RegexPatternPoolFactory.ID_CARD_LAST_6;

    /**
     * QQ号码
     * 用于验证QQ号码格式
     */
    public static final Pattern QQ_NUMBER = RegexPatternPoolFactory.QQ_NUMBER;

    /**
     * 邮政编码
     * 用于验证邮政编码格式
     */
    public static final Pattern POSTAL_CODE = RegexPatternPoolFactory.POSTAL_CODE;

    /**
     * 注册账号
     * 用于验证账号格式，通常要求字母开头，包含字母数字下划线
     */
    public static final Pattern ACCOUNT = RegexPatternPoolFactory.ACCOUNT;

    /**
     * 密码：包含至少8个字符，包括大写字母、小写字母、数字和特殊字符
     * 用于验证密码强度，确保密码安全性
     */
    public static final Pattern PASSWORD = RegexPatternPoolFactory.PASSWORD;

    /**
     * 通用状态（0表示正常，1表示停用）
     * 用于验证状态字段，通常用于数据库状态字段
     */
    public static final Pattern STATUS = RegexPatternPoolFactory.STATUS;


    /**
     * 检查输入的账号是否匹配预定义的规则
     * 使用预编译的正则表达式模式进行匹配，提升性能
     *
     * @param value 要验证的账号
     * @return 如果账号符合规则，返回 true；否则，返回 false。
     */
    // 静态方法，方便全局调用，无需创建对象
    public static boolean isAccount(CharSequence value) {
        // 调用父类的isMatchRegex方法，使用预编译模式匹配
        return isMatchRegex(ACCOUNT, value);
    }

    /**
     * 验证输入的账号是否符合规则，如果不符合，则抛出 ValidateException 异常
     * 提供强制验证能力，验证失败时抛出异常，便于统一异常处理
     *
     * @param value    要验证的账号
     * @param errorMsg 验证失败时抛出的异常消息
     * @param <T>      CharSequence 的子类型
     * @return 如果验证通过，返回输入的账号
     * @throws ValidateException 如果验证失败
     */
    // 泛型方法，支持任意CharSequence子类型
    public static <T extends CharSequence> T validateAccount(T value, String errorMsg) throws ValidateException {
        // 调用isAccount方法进行验证
        if (!isAccount(value)) {
            // 验证失败，抛出ValidateException异常
            throw new ValidateException(errorMsg);
        }
        // 验证通过，返回原值
        return value;
    }

    /**
     * 检查输入的状态是否匹配预定义的规则
     * 使用预编译的正则表达式模式进行匹配，提升性能
     *
     * @param value 要验证的状态
     * @return 如果状态符合规则，返回 true；否则，返回 false。
     */
    // 静态方法，方便全局调用
    public static boolean isStatus(CharSequence value) {
        // 调用父类的isMatchRegex方法，使用预编译模式匹配
        return isMatchRegex(STATUS, value);
    }

    /**
     * 验证输入的状态是否符合规则，如果不符合，则抛出 ValidateException 异常
     * 提供强制验证能力，验证失败时抛出异常，便于统一异常处理
     *
     * @param value    要验证的状态
     * @param errorMsg 验证失败时抛出的异常消息
     * @param <T>      CharSequence 的子类型
     * @return 如果验证通过，返回输入的状态
     * @throws ValidateException 如果验证失败
     */
    // 泛型方法，支持任意CharSequence子类型
    public static <T extends CharSequence> T validateStatus(T value, String errorMsg) throws ValidateException {
        // 调用isStatus方法进行验证
        if (!isStatus(value)) {
            // 验证失败，抛出ValidateException异常
            throw new ValidateException(errorMsg);
        }
        // 验证通过，返回原值
        return value;
    }

}
