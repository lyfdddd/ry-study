// 字符串工具类，继承Apache Commons Lang3的StringUtils
// 提供字符串格式化、转换、验证、拼接等增强功能
package org.dromara.common.core.utils;

// Hutool集合工具类（用于判断集合是否为空）
import cn.hutool.core.collection.CollUtil;
// Hutool类型转换工具类（用于对象转换）
import cn.hutool.core.convert.Convert;
// Hutool验证器（用于URL、邮箱等格式验证）
import cn.hutool.core.lang.Validator;
// Hutool字符串工具类（提供基础字符串操作）
import cn.hutool.core.util.StrUtil;
// Spring Ant路径匹配器（用于URL模式匹配）
import org.springframework.util.AntPathMatcher;

// Java字符集（用于字符串编码转换）
import java.nio.charset.Charset;
// Java集合接口
import java.util.*;
// Java函数式接口（用于自定义转换）
import java.util.function.Function;
// Java Stream API（用于集合流式处理）
import java.util.stream.Collectors;

/**
 * 字符串工具类
 * 继承Apache Commons Lang3的StringUtils，提供增强的字符串操作能力
 * 包括格式化、转换、验证、拼接、路径匹配等功能
 *
 * @author Lion Li
 */
// 继承Apache Commons Lang3的StringUtils，复用其字符串操作方法
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 默认分隔符：逗号
     * 用于splitList等方法的默认分隔符
     */
    public static final String SEPARATOR = ",";
    
    /**
     * 斜杠分隔符
     * 用于URL、路径等场景
     */
    public static final String SLASH = "/";

    /**
     * 私有构造方法（已废弃）
     * @deprecated 工具类不应被实例化，所有方法均为静态方法
     */
    @Deprecated
    private StringUtils() {
        // 工具类私有化构造方法，防止被实例化
    }

    /**
     * 获取参数不为空值
     * 如果字符串为null或空白字符串，返回默认值
     *
     * @param str 要判断的value
     * @param defaultValue 默认值
     * @return value（如果不为空）或defaultValue（如果为空）
     */
    public static String blankToDefault(String str, String defaultValue) {
        // 调用Hutool的StrUtil.blankToDefault方法
        // blankToDefault判断字符串是否为null或空白（包括空格、制表符等）
        return StrUtil.blankToDefault(str, defaultValue);
    }

    /**
     * 判断一个字符串是否为空串
     * 空串包括：null、""、空白字符串（只包含空格、制表符等）
     *
     * @param str 字符串
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str) {
        // 调用Hutool的StrUtil.isEmpty方法
        // isEmpty判断字符串是否为null或空字符串（不判断空白）
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断一个字符串是否为非空串
     * 与isEmpty相反，判断字符串不为null且不为空字符串
     *
     * @param str 字符串
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str) {
        // 调用isEmpty()并取反
        return !isEmpty(str);
    }

    /**
     * 去除字符串首尾空格
     * 如果字符串为null，返回null
     *
     * @param str 字符串
     * @return 去除首尾空格后的字符串
     */
    public static String trim(String str) {
        // 调用Hutool的StrUtil.trim方法
        // trim去除字符串首尾的空白字符（空格、制表符、换行符等）
        return StrUtil.trim(str);
    }

    /**
     * 截取字符串（从指定位置到末尾）
     * 支持负数索引（-1表示最后一个字符）
     *
     * @param str 字符串
     * @param start 开始位置（从0开始，支持负数）
     * @return 截取后的字符串
     */
    public static String substring(final String str, int start) {
        // 调用重载方法，结束位置为字符串长度
        return substring(str, start, str.length());
    }

    /**
     * 截取字符串（从指定位置到指定位置）
     * 支持负数索引，自动处理越界情况
     *
     * @param str 字符串
     * @param start 开始位置（从0开始，支持负数）
     * @param end 结束位置（不包含，支持负数）
     * @return 截取后的字符串
     */
    public static String substring(final String str, int start, int end) {
        // 调用Hutool的StrUtil.sub方法
        // sub方法支持负数索引，-1表示最后一个字符，-2表示倒数第二个，以此类推
        return StrUtil.sub(str, start, end);
    }

    /**
     * 格式化文本，{}表示占位符
     * 此方法只是简单将占位符{}按照顺序替换为参数
     * 转义规则：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") -> this is {} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template 文本模板，被替换的部分用{}表示
     * @param params 参数值（可变参数）
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params) {
        // 调用Hutool的StrUtil.format方法
        // format使用{}作为占位符，按顺序替换为参数值
        return StrUtil.format(template, params);
    }

    /**
     * 判断字符串是否为http(s)://开头
     * 验证URL格式是否合法
     *
     * @param link 链接字符串
     * @return true：是URL格式 false：不是URL格式
     */
    public static boolean ishttp(String link) {
        // 调用Hutool的Validator.isUrl方法
        // isUrl使用正则表达式验证URL格式（支持http、https、ftp等）
        return Validator.isUrl(link);
    }

    /**
     * 字符串转Set集合
     * 使用指定分隔符分割字符串，去重后返回Set
     *
     * @param str 字符串
     * @param sep 分隔符
     * @return Set集合（自动去重）
     */
    public static Set<String> str2Set(String str, String sep) {
        // 调用str2List转换为List，再创建HashSet去重
        return new HashSet<>(str2List(str, sep, true, false));
    }

    /**
     * 字符串转List集合
     * 使用指定分隔符分割字符串，支持过滤空白和去除首尾空格
     *
     * @param str 字符串
     * @param sep 分隔符
     * @param filterBlank 是否过滤纯空白字符串
     * @param trim 是否去除首尾空格
     * @return List集合
     */
    public static List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        // 创建ArrayList存储结果
        List<String> list = new ArrayList<>();
        // 如果字符串为空，返回空List
        if (isEmpty(str)) {
            return list;
        }

        // 如果filterBlank为true且字符串是空白字符串，返回空List
        if (filterBlank && isBlank(str)) {
            return list;
        }
        // 使用split方法分割字符串
        String[] split = str.split(sep);
        // 遍历分割后的数组
        for (String string : split) {
            // 如果filterBlank为true且当前元素是空白字符串，跳过
            if (filterBlank && isBlank(string)) {
                continue;
            }
            // 如果trim为true，去除首尾空格
            if (trim) {
                string = trim(string);
            }
            // 添加到List
            list.add(string);
        }

        // 返回处理后的List
        return list;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串（忽略大小写）
     * 用于权限验证、角色判断等场景
     *
     * @param cs 指定字符串
     * @param searchCharSequences 需要检查的字符串数组
     * @return true：包含任意一个字符串 false：不包含
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences) {
        // 调用Hutool的StrUtil.containsAnyIgnoreCase方法
        // containsAnyIgnoreCase忽略大小写判断是否包含任意一个字符串
        return StrUtil.containsAnyIgnoreCase(cs, searchCharSequences);
    }

    /**
     * 驼峰命名转下划线命名
     * 例如：userName -> user_name
     *
     * @param str 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public static String toUnderScoreCase(String str) {
        // 调用Hutool的StrUtil.toUnderlineCase方法
        // toUnderlineCase将驼峰命名转换为下划线命名
        return StrUtil.toUnderlineCase(str);
    }

    /**
     * 判断字符串是否包含在指定字符串列表中（忽略大小写）
     * 用于权限验证、角色判断等场景
     *
     * @param str 验证字符串
     * @param strs 字符串组
     * @return true：包含 false：不包含
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        // 调用Hutool的StrUtil.equalsAnyIgnoreCase方法
        // equalsAnyIgnoreCase忽略大小写判断字符串是否等于任意一个
        return StrUtil.equalsAnyIgnoreCase(str, strs);
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式
     * 例如：HELLO_WORLD -> HelloWorld
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        // 先调用StrUtil.toCamelCase转换为驼峰命名（helloWorld）
        // 再调用StrUtil.upperFirst将首字母大写（HelloWorld）
        return StrUtil.upperFirst(StrUtil.toCamelCase(name));
    }

    /**
     * 驼峰式命名法
     * 例如：user_name -> userName
     *
     * @param s 下划线命名字符串
     * @return 驼峰命名字符串
     */
    public static String toCamelCase(String s) {
        // 调用Hutool的StrUtil.toCamelCase方法
        // toCamelCase将下划线命名转换为驼峰命名
        return StrUtil.toCamelCase(s);
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     * 使用AntPathMatcher进行模式匹配，支持通配符
     *
     * @param str 指定字符串
     * @param strs 需要检查的字符串数组（支持Ant路径匹配规则）
     * @return true：匹配 false：不匹配
     */
    public static boolean matches(String str, List<String> strs) {
        // 如果字符串为空或字符串列表为空，返回false
        if (isEmpty(str) || CollUtil.isEmpty(strs)) {
            return false;
        }
        // 遍历字符串列表
        for (String pattern : strs) {
            // 调用isMatch方法进行模式匹配
            if (isMatch(pattern, str)) {
                // 匹配成功，返回true
                return true;
            }
        }
        // 全部不匹配，返回false
        return false;
    }

    /**
     * 判断URL是否与规则配置匹配
     * 支持Ant风格的路径匹配规则：
     * ? 表示单个字符
     * * 表示一层路径内的任意字符串，不可跨层级
     * ** 表示任意层路径
     * 例如：
     * /user/* 匹配 /user/1、/user/abc，不匹配 /user/1/2
     * /user/** 匹配 /user/1、/user/1/2、/user/a/b/c
     *
     * @param pattern 匹配规则
     * @param url 需要匹配的URL
     * @return true：匹配 false：不匹配
     */
    public static boolean isMatch(String pattern, String url) {
        // 创建AntPathMatcher对象（Spring提供的路径匹配器）
        AntPathMatcher matcher = new AntPathMatcher();
        // 调用match方法进行模式匹配
        return matcher.match(pattern, url);
    }

    /**
     * 数字左边补齐0，使之达到指定长度
     * 注意：如果数字转换为字符串后，长度大于size，则只保留最后size个字符
     * 例如：padl(123, 5) -> "00123"
     *
     * @param num 数字对象
     * @param size 字符串指定长度
     * @return 返回数字的字符串格式，该字符串为指定长度
     */
    public static String padl(final Number num, final int size) {
        // 调用重载方法，传入数字的字符串形式
        return padl(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐
     * 如果原始字符串s长度大于size，则只保留最后size个字符
     * 例如：padl("123", 5, '0') -> "00123"
     *
     * @param s 原始字符串
     * @param size 字符串指定长度
     * @param c 用于补齐的字符
     * @return 返回指定长度的字符串，由原字符串左补齐或截取得到
     */
    public static String padl(final String s, final int size, final char c) {
        // 创建StringBuilder，容量为指定长度
        final StringBuilder sb = new StringBuilder(size);
        // 如果字符串不为null
        if (s != null) {
            // 获取字符串长度
            final int len = s.length();
            // 如果字符串长度小于等于指定长度
            if (s.length() <= size) {
                // 计算需要补齐的字符数
                // 使用Convert.toStr将字符转换为字符串，repeat重复指定次数
                sb.append(Convert.toStr(c).repeat(size - len));
                // 追加原始字符串
                sb.append(s);
            } else {
                // 如果字符串长度大于指定长度，截取最后size个字符
                return s.substring(len - size, len);
            }
        } else {
            // 如果字符串为null，全部用补齐字符填充
            sb.append(Convert.toStr(c).repeat(Math.max(0, size)));
        }
        // 返回补齐后的字符串
        return sb.toString();
    }

    /**
     * 切分字符串（分隔符默认逗号）
     * 将逗号分隔的字符串转换为List<String>
     *
     * @param str 被切分的字符串
     * @return 分割后的数据列表
     */
    public static List<String> splitList(String str) {
        // 调用splitTo方法，使用默认分隔符SEPARATOR（逗号）
        // Convert::toStr是方法引用，将元素转换为String
        return splitTo(str, Convert::toStr);
    }

    /**
     * 切分字符串（指定分隔符）
     * 将指定分隔符分割的字符串转换为List<String>
     *
     * @param str 被切分的字符串
     * @param separator 分隔符
     * @return 分割后的数据列表
     */
    public static List<String> splitList(String str, String separator) {
        // 调用splitTo方法，指定分隔符
        return splitTo(str, separator, Convert::toStr);
    }

    /**
     * 切分字符串并自定义转换（分隔符默认逗号）
     * 将逗号分隔的字符串转换为指定类型的List
     *
     * @param str 被切分的字符串
     * @param mapper 自定义转换函数
     * @return 分割后的数据列表
     */
    public static <T> List<T> splitTo(String str, Function<? super Object, T> mapper) {
        // 调用重载方法，使用默认分隔符SEPARATOR（逗号）
        return splitTo(str, SEPARATOR, mapper);
    }

    /**
     * 切分字符串并自定义转换
     * 将指定分隔符分割的字符串转换为指定类型的List
     * 支持自定义转换逻辑，如String转Long、String转Integer等
     *
     * @param str 被切分的字符串
     * @param separator 分隔符
     * @param mapper 自定义转换函数（如Convert::toLong）
     * @return 分割后的数据列表
     */
    public static <T> List<T> splitTo(String str, String separator, Function<? super Object, T> mapper) {
        // 如果字符串为null或空白，返回空List（容量为0）
        if (isBlank(str)) {
            return new ArrayList<>(0);
        }
        // 使用Hutool的StrUtil.split分割字符串
        // stream()转换为Stream，filter过滤null元素
        // map应用自定义转换函数，再次filter过滤转换后的null元素
        // collect收集为List
        return StrUtil.split(str, separator)
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 不区分大小写检查CharSequence是否以指定的前缀开头
     * 用于URL前缀判断、文件类型判断等场景
     *
     * @param str 要检查的CharSequence（可能为null）
     * @param prefixs 要查找的前缀（可能为null）
     * @return true：包含 false：不包含
     */
    public static boolean startWithAnyIgnoreCase(CharSequence str, CharSequence... prefixs) {
        // 遍历前缀数组
        for (CharSequence prefix : prefixs) {
            // 调用StringUtils.startsWithIgnoreCase判断前缀（忽略大小写）
            if (StringUtils.startsWithIgnoreCase(str, prefix)) {
                // 匹配成功，返回true
                return true;
            }
        }
        // 全部不匹配，返回false
        return false;
    }

    /**
     * 将字符串从源字符集转换为目标字符集
     * 用于处理编码转换问题，如GBK转UTF-8
     *
     * @param input 原始字符串
     * @param fromCharset 源字符集
     * @param toCharset 目标字符集
     * @return 转换后的字符串（如果转换失败返回原始字符串）
     */
    public static String convert(String input, Charset fromCharset, Charset toCharset) {
        // 如果输入为null或空白，直接返回
        if (isBlank(input)) {
            return input;
        }
        try {
            // 从源字符集获取字节数组
            byte[] bytes = input.getBytes(fromCharset);
            // 使用目标字符集解码为字符串
            return new String(bytes, toCharset);
        } catch (Exception e) {
            // 转换失败（如字符集不支持），返回原始字符串
            return input;
        }
    }

    /**
     * 将可迭代对象中的元素使用逗号拼接成字符串
     * 例如：List.of("a", "b", "c") -> "a,b,c"
     *
     * @param iterable 可迭代对象，如List、Set等
     * @return 拼接后的字符串
     */
    public static String joinComma(Iterable<?> iterable) {
        // 调用StringUtils.join方法，使用SEPARATOR（逗号）连接
        return StringUtils.join(iterable, SEPARATOR);
    }

    /**
     * 将数组中的元素使用逗号拼接成字符串
     * 例如：new String[]{"a", "b", "c"} -> "a,b,c"
     *
     * @param array 任意类型的数组
     * @return 拼接后的字符串
     */
    public static String joinComma(Object[] array) {
        // 调用StringUtils.join方法，使用SEPARATOR（逗号）连接
        return StringUtils.join(array, SEPARATOR);
    }

}
