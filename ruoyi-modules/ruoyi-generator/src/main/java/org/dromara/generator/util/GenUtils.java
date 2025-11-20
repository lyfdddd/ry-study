package org.dromara.generator.util;

// 导入Lombok的访问级别枚举
import lombok.AccessLevel;
// 导入Lombok的无参构造函数注解，并设置访问级别为PRIVATE，防止实例化
import lombok.NoArgsConstructor;
// 导入Apache Commons Lang3的正则表达式工具类
import org.apache.commons.lang3.RegExUtils;
// 导入字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 导入代码生成配置类
import org.dromara.generator.config.GenConfig;
// 导入代码生成常量类
import org.dromara.generator.constant.GenConstants;
// 导入代码生成表实体类
import org.dromara.generator.domain.GenTable;
// 导入代码生成表字段实体类
import org.dromara.generator.domain.GenTableColumn;

import java.util.Arrays;

/**
 * 代码生成器 工具类
 * 提供代码生成过程中的各种工具方法，包括表信息初始化、字段初始化、名称转换等
 * 使用私有构造函数防止实例化，符合工具类设计模式
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenUtils {

    /**
     * 初始化表信息
     * 根据表名和配置信息，初始化GenTable对象的各个属性
     * 包括类名、包路径、模块名、业务名、功能名、作者等
     *
     * @param genTable 代码生成表对象
     */
    public static void initTable(GenTable genTable) {
        // 将表名转换为Java类名（驼峰命名，首字母大写）
        genTable.setClassName(convertClassName(genTable.getTableName()));
        // 设置包路径，从配置文件中读取
        genTable.setPackageName(GenConfig.getPackageName());
        // 获取模块名（包路径的最后一部分）
        genTable.setModuleName(getModuleName(GenConfig.getPackageName()));
        // 获取业务名（表名去掉前缀后的部分）
        genTable.setBusinessName(getBusinessName(genTable.getTableName()));
        // 设置功能名（表注释，去掉"表"和"若依"关键字）
        genTable.setFunctionName(replaceText(genTable.getTableComment()));
        // 设置作者，从配置文件中读取
        genTable.setFunctionAuthor(GenConfig.getAuthor());
        // 清空创建时间，由数据库自动填充
        genTable.setCreateTime(null);
        // 清空更新时间，由数据库自动填充
        genTable.setUpdateTime(null);
    }

    /**
     * 初始化列属性字段
     * 根据数据库字段类型和名称，初始化GenTableColumn对象的各个属性
     * 包括Java字段名、Java类型、HTML控件类型、是否插入、是否编辑、是否列表、是否查询等
     *
     * @param column 代码生成表字段对象
     * @param table 代码生成表对象
     */
    public static void initColumnField(GenTableColumn column, GenTable table) {
        // 获取数据库字段类型（去掉长度部分，如varchar(50) -> varchar）
        String dataType = getDbType(column.getColumnType());
        // 统一转小写，避免有些数据库默认大写问题。如果需要特别书写方式，请在实体类增加注解标注别名
        String columnName = column.getColumnName().toLowerCase();
        // 设置归属表ID
        column.setTableId(table.getTableId());
        // 清空创建时间，由数据库自动填充
        column.setCreateTime(null);
        // 清空更新时间，由数据库自动填充
        column.setUpdateTime(null);
        // 设置java字段名（将下划线命名转换为驼峰命名）
        column.setJavaField(StringUtils.toCamelCase(columnName));
        // 设置默认Java类型为String
        column.setJavaType(GenConstants.TYPE_STRING);
        // 设置默认查询方式为相等（EQ）
        column.setQueryType(GenConstants.QUERY_EQ);

        // 判断是否为字符串类型或文本类型
        if (arraysContains(GenConstants.COLUMNTYPE_STR, dataType) || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType)) {
            // 字符串长度超过500设置为文本域（textarea），否则为输入框（input）
            Integer columnLength = getColumnLength(column.getColumnType());
            String htmlType = columnLength >= 500 || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType) ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT;
            column.setHtmlType(htmlType);
        } else if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType)) {
            // 如果是时间类型，设置Java类型为Date，HTML控件类型为日期时间选择器
            column.setJavaType(GenConstants.TYPE_DATE);
            column.setHtmlType(GenConstants.HTML_DATETIME);
        } else if (arraysContains(GenConstants.COLUMNTYPE_NUMBER, dataType)) {
            // 如果是数字类型，设置HTML控件类型为输入框
            column.setHtmlType(GenConstants.HTML_INPUT);
            // 数据库的数字字段与Java不匹配，且很多数据库的数字字段很模糊（例如Oracle只有number没有细分）
            // 所以默认数字类型全为Long，可在界面上自行编辑想要的类型，有什么特殊需求也可以在这里特殊处理
            column.setJavaType(GenConstants.TYPE_LONG);
        }

        // BO对象默认插入勾选（除了系统字段和主键）
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_ADD, columnName) && !column.isPk()) {
            column.setIsInsert(GenConstants.REQUIRE);
        }
        // BO对象默认编辑勾选（除了系统字段）
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName)) {
            column.setIsEdit(GenConstants.REQUIRE);
        }
        // VO对象默认返回勾选（除了系统字段）
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName)) {
            column.setIsList(GenConstants.REQUIRE);
        }
        // BO对象默认查询勾选（除了系统字段和主键）
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !column.isPk()) {
            column.setIsQuery(GenConstants.REQUIRE);
        }

        // 查询字段类型：如果字段名以"name"结尾，设置为模糊查询（LIKE）
        if (StringUtils.endsWithIgnoreCase(columnName, "name")) {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }
        // 状态字段设置单选框（radio）
        if (StringUtils.endsWithIgnoreCase(columnName, "status")) {
            column.setHtmlType(GenConstants.HTML_RADIO);
        }
        // 类型&性别字段设置下拉框（select）
        else if (StringUtils.endsWithIgnoreCase(columnName, "type")
            || StringUtils.endsWithIgnoreCase(columnName, "sex")) {
            column.setHtmlType(GenConstants.HTML_SELECT);
        }
        // 图片字段设置图片上传控件（imageUpload）
        else if (StringUtils.endsWithIgnoreCase(columnName, "image")) {
            column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
        }
        // 文件字段设置文件上传控件（fileUpload）
        else if (StringUtils.endsWithIgnoreCase(columnName, "file")) {
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
        }
        // 内容字段设置富文本控件（editor）
        else if (StringUtils.endsWithIgnoreCase(columnName, "content")) {
            column.setHtmlType(GenConstants.HTML_EDITOR);
        }
    }

    /**
     * 校验数组是否包含指定值
     * 使用Arrays.asList将数组转换为List，然后调用contains方法判断是否包含目标值
     *
     * @param arr         数组
     * @param targetValue 目标值
     * @return true包含，false不包含
     */
    public static boolean arraysContains(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

    /**
     * 获取模块名
     * 从包路径中提取模块名（包路径的最后一部分）
     * 例如：org.dromara.system -> system
     *
     * @param packageName 包名
     * @return 模块名
     */
    public static String getModuleName(String packageName) {
        // 查找最后一个点的位置
        int lastIndex = packageName.lastIndexOf(".");
        // 获取包名长度
        int nameLength = packageName.length();
        // 截取最后一个点之后的部分作为模块名
        return StringUtils.substring(packageName, lastIndex + 1, nameLength);
    }

    /**
     * 获取业务名
     * 从表名中提取业务名（去掉前缀后的部分，并转换为驼峰命名）
     * 例如：sys_user -> user
     *
     * @param tableName 表名
     * @return 业务名
     */
    public static String getBusinessName(String tableName) {
        // 查找第一个下划线的位置
        int firstIndex = tableName.indexOf("_");
        // 获取表名长度
        int nameLength = tableName.length();
        // 截取第一个下划线之后的部分
        String businessName = StringUtils.substring(tableName, firstIndex + 1, nameLength);
        // 转换为驼峰命名
        businessName = StringUtils.toCamelCase(businessName);
        return businessName;
    }

    /**
     * 表名转换成Java类名
     * 将数据库表名转换为Java类名（驼峰命名，首字母大写）
     * 如果配置了自动去除表前缀，会先去除前缀
     * 例如：sys_user -> SysUser
     *
     * @param tableName 表名称
     * @return 类名
     */
    public static String convertClassName(String tableName) {
        // 获取是否自动去除表前缀的配置
        boolean autoRemovePre = GenConfig.getAutoRemovePre();
        // 获取表前缀配置
        String tablePrefix = GenConfig.getTablePrefix();
        // 如果启用自动去除前缀且前缀不为空
        if (autoRemovePre && StringUtils.isNotEmpty(tablePrefix)) {
            // 将表前缀按分隔符拆分为数组
            String[] searchList = StringUtils.split(tablePrefix, StringUtils.SEPARATOR);
            // 去除表名中的前缀
            tableName = replaceFirst(tableName, searchList);
        }
        // 将表名转换为驼峰命名（首字母大写）
        return StringUtils.convertToCamelCase(tableName);
    }

    /**
     * 批量替换前缀
     * 从表名中去除指定的前缀（匹配第一个符合条件的前缀）
     *
     * @param replacementm 表名
     * @param searchList   前缀列表
     * @return 去除前缀后的表名
     */
    public static String replaceFirst(String replacementm, String[] searchList) {
        // 初始化返回值为原表名
        String text = replacementm;
        // 遍历前缀列表
        for (String searchString : searchList) {
            // 如果表名以该前缀开头
            if (replacementm.startsWith(searchString)) {
                // 去除该前缀
                text = replacementm.replaceFirst(searchString, StringUtils.EMPTY);
                // 只替换第一个匹配的前缀，跳出循环
                break;
            }
        }
        return text;
    }

    /**
     * 关键字替换
     * 从表注释中去除"表"和"若依"关键字，使功能名更简洁
     *
     * @param text 需要被替换的名字（表注释）
     * @return 替换后的名字（功能名）
     */
    public static String replaceText(String text) {
        // 使用正则表达式替换"表"和"若依"关键字为空字符串
        return RegExUtils.replaceAll(text, "(?:表|若依)", "");
    }

    /**
     * 获取数据库类型字段
     * 从完整的列类型中提取基础类型（去掉长度部分）
     * 例如：varchar(50) -> varchar，int -> int
     *
     * @param columnType 列类型（包含长度）
     * @return 基础列类型
     */
    public static String getDbType(String columnType) {
        // 如果包含左括号，说明有长度定义
        if (StringUtils.indexOf(columnType, "(") > 0) {
            // 截取左括号之前的部分作为基础类型
            return StringUtils.substringBefore(columnType, "(");
        } else {
            // 不包含括号，直接返回原类型
            return columnType;
        }
    }

    /**
     * 获取字段长度
     * 从列类型定义中提取长度值
     * 例如：varchar(50) -> 50，int -> 0
     *
     * @param columnType 列类型（包含长度）
     * @return 字段长度，如果没有长度定义返回0
     */
    public static Integer getColumnLength(String columnType) {
        // 如果包含左括号，说明有长度定义
        if (StringUtils.indexOf(columnType, "(") > 0) {
            // 提取括号中的长度值
            String length = StringUtils.substringBetween(columnType, "(", ")");
            // 转换为Integer类型返回
            return Integer.valueOf(length);
        } else {
            // 不包含括号，返回0
            return 0;
        }
    }
}
