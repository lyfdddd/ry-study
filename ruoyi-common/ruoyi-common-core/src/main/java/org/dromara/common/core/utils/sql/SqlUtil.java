// SQL操作工具类，提供SQL注入防护功能
package org.dromara.common.core.utils.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.core.utils.StringUtils;

/**
 * SQL操作工具类
 * 提供SQL注入防护功能，包括关键字过滤、排序字段验证等
 * 主要用于防止恶意用户通过输入参数进行SQL注入攻击
 * 支持常见的SQL注入关键字检测和ORDER BY语法验证
 * 提供统一的SQL注入防护入口，确保数据安全
 *
 * @author ruoyi
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlUtil {

    /**
     * SQL注入关键字正则表达式
     * 包含常见的SQL注入关键字和函数，用于检测潜在的SQL注入风险
     * 如：and、or、union、select、insert、update、delete、drop等
     * 还包含一些MySQL特有的函数，如extractvalue、updatexml、sleep等
     * 使用竖线|作为分隔符，便于后续拆分处理
     * 注意：\u000B是垂直制表符，有时被用于绕过过滤
     */
    public static String SQL_REGEX = "\u000B|and |extractvalue|updatexml|sleep|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";

    /**
     * ORDER BY字段验证正则表达式
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     * 用于验证排序字段的合法性，防止通过排序参数进行SQL注入
     * 例如：user_name ASC, create_time DESC 是合法的
     * 例如：user_name; DROP TABLE user; 是非法的
     * 正则表达式解释：[a-zA-Z0-9_\\ \\,\\.]+
     * - a-zA-Z：字母
     * - 0-9：数字
     * - _：下划线
     * - \\ ：空格（需要转义）
     * - \\,：逗号（需要转义）
     * - \\.：小数点（需要转义）
     */
    public static final String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * 转义并验证ORDER BY SQL语句
     * 检查排序参数是否合法，防止SQL注入攻击
     * 如果参数不合法，抛出IllegalArgumentException异常
     * 这是主要的入口方法，用于对外提供ORDER BY参数验证
     *
     * @param value 需要验证的排序参数字符串
     * @return 验证通过的原参数值
     * @throws IllegalArgumentException 如果参数不符合规范
     */
    public static String escapeOrderBySql(String value) {
        // 如果参数不为空且不符合验证规则，抛出异常
        // StringUtils.isNotEmpty会检查字符串是否为null和空字符串
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            // 抛出IllegalArgumentException异常，包含中文错误信息
            // 这个异常会被全局异常处理器捕获，返回友好的错误提示
            throw new IllegalArgumentException("参数不符合规范，不能进行查询");
        }
        // 验证通过，返回原值
        // 注意：这个方法不会修改输入值，只是验证其合法性
        return value;
    }

    /**
     * 验证ORDER BY语法是否符合规范
     * 使用正则表达式验证排序字段是否只包含允许的字符
     * 这是一个辅助方法，被escapeOrderBySql方法调用
     *
     * @param value 需要验证的排序参数字符串
     * @return true表示符合规范，false表示不符合规范
     */
    public static boolean isValidOrderBySql(String value) {
        // 使用预定义的正则表达式进行匹配
        // String.matches方法会编译正则表达式并进行匹配
        // 如果value为null，matches会返回false
        return value.matches(SQL_PATTERN);
    }

    /**
     * SQL关键字检查
     * 检查输入值是否包含SQL注入关键字，如果包含则抛出异常
     * 用于防止通过输入参数进行SQL注入攻击
     * 支持不区分大小写的关键字检测
     * 这是主要的入口方法，用于对外提供SQL关键字过滤功能
     *
     * @param value 需要检查的输入值
     * @throws IllegalArgumentException 如果检测到SQL注入关键字
     */
    public static void filterKeyword(String value) {
        // 如果输入值为空，直接返回，无需检查
        // StringUtils.isEmpty会检查字符串是否为null、空字符串和"null"字符串
        if (StringUtils.isEmpty(value)) {
            return;
        }
        // 使用竖线分隔符拆分SQL关键字数组
        // StringUtils.split方法会处理分隔符转义，返回字符串数组
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        // 遍历每个SQL关键字
        for (String sqlKeyword : sqlKeywords) {
            // 使用不区分大小写的方式检查是否包含SQL关键字
            // StringUtils.indexOfIgnoreCase方法会忽略大小写进行查找
            // 如果返回值大于-1，表示找到了匹配的关键字
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                // 如果检测到SQL关键字，抛出异常
                // 抛出IllegalArgumentException异常，包含中文错误信息
                // 这个异常会被全局异常处理器捕获，返回友好的错误提示
                throw new IllegalArgumentException("参数存在SQL注入风险");
            }
        }
    }
}
