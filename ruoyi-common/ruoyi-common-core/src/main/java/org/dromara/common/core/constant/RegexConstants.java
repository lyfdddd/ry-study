package org.dromara.common.core.constant;

import cn.hutool.core.lang.RegexPool;

/**
 * 常用正则表达式字符串
 * <p>
 * 常用正则表达式集合，更多正则见: https://any86.github.io/any-rule/
 * 继承Hutool的RegexPool，复用常用正则表达式
 *
 * @author Feng
 */
// 继承Hutool的RegexPool，复用邮箱、手机号等常用正则
public interface RegexConstants extends RegexPool {

    /**
     * 字典类型必须以字母开头，且只能为（小写字母，数字，下滑线）
     * 示例：sys_user、order_detail、dict_type
     * 用于校验数据字典类型的合法性
     */
    String DICTIONARY_TYPE = "^[a-z][a-z0-9_]*$";

    /**
     * 权限标识必须符合以下格式：
     * 1. 标准格式：xxx:yyy:zzz
     * - 第一部分（xxx）：只能包含字母、数字和下划线（_），不能使用 `*`
     * - 第二部分（yyy）：可以包含字母、数字、下划线（_）和 `*`
     * - 第三部分（zzz）：可以包含字母、数字、下划线（_）和 `*`
     * 2. 允许空字符串（""），表示没有权限标识
     * 示例：system:user:add、monitor:*:export、（空字符串）
     * 用于校验权限字符串的格式合法性
     */
    String PERMISSION_STRING = "^$|^[a-zA-Z0-9_]+:[a-zA-Z0-9_*]+:[a-zA-Z0-9_*]+$";

    /**
     * 身份证号码（后6位）
     * 格式：3位地区码 + 3位顺序码 + 1位校验码（数字或X）
     * 示例：1234567890X
     * 用于身份证后6位脱敏显示
     */
    String ID_CARD_LAST_6 = "^(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

    /**
     * QQ号码
     * 格式：5-11位数字，首位不能为0
     * 示例：123456、9876543210
     * 用于校验QQ号码的合法性
     */
    String QQ_NUMBER = "^[1-9][0-9]\\d{4,9}$";

    /**
     * 邮政编码
     * 格式：6位数字，首位不能为0
     * 示例：100000、200030
     * 用于校验中国邮政编码
     */
    String POSTAL_CODE = "^[1-9]\\d{5}$";

    /**
     * 注册账号
     * 格式：字母开头，5-16位，只能包含字母、数字、下划线
     * 示例：user123、admin_001
     * 用于用户注册时的账号格式校验
     */
    String ACCOUNT = "^[a-zA-Z][a-zA-Z0-9_]{4,15}$";

    /**
     * 密码强度校验：包含至少8个字符，包括大写字母、小写字母、数字和特殊字符
     * 格式：8位以上，必须包含大小写字母、数字、特殊字符
     * 示例：Abc@1234、Passw0rd!
     * 用于密码强度校验，提升账户安全性
     */
    String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    /**
     * 通用状态（0表示正常，1表示停用）
     * 格式：单个数字0或1
     * 用于校验状态字段的合法性
     */
    String STATUS = "^[01]$";

}
