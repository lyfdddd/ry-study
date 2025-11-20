package org.dromara.generator.domain;

// 导入MyBatis-Plus字段策略注解，用于控制更新策略
import com.baomidou.mybatisplus.annotation.FieldStrategy;
// 导入MyBatis-Plus表字段注解
import com.baomidou.mybatisplus.annotation.TableField;
// 导入MyBatis-Plus主键注解
import com.baomidou.mybatisplus.annotation.TableId;
// 导入MyBatis-Plus表名注解
import com.baomidou.mybatisplus.annotation.TableName;
// 导入字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 导入基础实体类
import org.dromara.common.mybatis.core.domain.BaseEntity;
// 导入Lombok的Data注解，自动生成getter/setter等方法
import lombok.Data;
// 导入Lombok的EqualsAndHashCode注解
import lombok.EqualsAndHashCode;
// 导入MyBatis的JDBC类型枚举
import org.apache.ibatis.type.JdbcType;

// 导入参数校验注解
import jakarta.validation.constraints.NotBlank;

/**
 * 代码生成业务字段表 gen_table_column
 * 存储代码生成配置中每个字段的详细信息
 * 继承BaseEntity获得基础字段（创建人、创建时间、更新人、更新时间等）
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gen_table_column")
public class GenTableColumn extends BaseEntity {

    /**
     * 编号
     * 主键ID，对应数据库表gen_table_column的column_id字段
     */
    @TableId(value = "column_id")
    private Long columnId;

    /**
     * 归属表编号
     * 外键，关联gen_table表的table_id字段
     */
    private Long tableId;

    /**
     * 列名称
     * 数据库字段名，如user_name、create_time
     */
    private String columnName;

    /**
     * 列描述
     * 数据库字段注释，用于生成字段说明
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String columnComment;

    /**
     * 列类型
     * 数据库字段类型，如varchar(50)、int(11)、datetime
     */
    private String columnType;

    /**
     * JAVA类型
     * 对应的Java类型，如String、Integer、Date
     */
    private String javaType;

    /**
     * JAVA字段名
     * Java对象的属性名，如userName、createTime
     * 不能为空，是代码生成的核心配置
     */
    @NotBlank(message = "Java属性不能为空")
    private String javaField;

    /**
     * 是否主键（1是）
     * 标记该字段是否为主键
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isPk;

    /**
     * 是否自增（1是）
     * 标记该字段是否为自增主键
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isIncrement;

    /**
     * 是否必填（1是）
     * 标记该字段在表单中是否为必填项
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isRequired;

    /**
     * 是否为插入字段（1是）
     * 标记该字段是否在插入时包含
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isInsert;

    /**
     * 是否编辑字段（1是）
     * 标记该字段是否在编辑时包含
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isEdit;

    /**
     * 是否列表字段（1是）
     * 标记该字段是否在列表页面显示
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isList;

    /**
     * 是否查询字段（1是）
     * 标记该字段是否作为查询条件
     * 更新策略为ALWAYS，表示每次更新都包含此字段
     * JDBC类型为VARCHAR
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isQuery;

    /**
     * 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围）
     * 指定该字段的查询方式，影响生成的查询条件
     */
    private String queryType;

    /**
     * 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、image图片上传控件、upload文件上传控件、editor富文本控件）
     * 指定该字段在页面上的显示控件类型
     */
    private String htmlType;

    /**
     * 字典类型
     * 当htmlType为select、radio、checkbox时，指定使用的字典类型
     */
    private String dictType;

    /**
     * 排序
     * 字段在列表中的显示顺序
     */
    private Integer sort;

    /**
     * 获取首字母大写的Java字段名
     * 用于生成getter/setter方法名
     * @return 首字母大写的字段名
     */
    public String getCapJavaField() {
        return StringUtils.capitalize(javaField);
    }

    /**
     * 判断当前字段是否为主键
     * @return true是主键，false不是主键
     */
    public boolean isPk() {
        return isPk(this.isPk);
    }

    /**
     * 静态方法判断字段是否为主键
     * @param isPk 是否主键标记（1是）
     * @return true是主键，false不是主键
     */
    public boolean isPk(String isPk) {
        return isPk != null && StringUtils.equals("1", isPk);
    }

    /**
     * 判断当前字段是否为自增
     * @return true是自增，false不是自增
     */
    public boolean isIncrement() {
        return isIncrement(this.isIncrement);
    }

    /**
     * 静态方法判断字段是否为自增
     * @param isIncrement 是否自增标记（1是）
     * @return true是自增，false不是自增
     */
    public boolean isIncrement(String isIncrement) {
        return isIncrement != null && StringUtils.equals("1", isIncrement);
    }

    /**
     * 判断当前字段是否为必填
     * @return true是必填，false不是必填
     */
    public boolean isRequired() {
        return isRequired(this.isRequired);
    }

    /**
     * 静态方法判断字段是否为必填
     * @param isRequired 是否必填标记（1是）
     * @return true是必填，false不是必填
     */
    public boolean isRequired(String isRequired) {
        return isRequired != null && StringUtils.equals("1", isRequired);
    }

    /**
     * 判断当前字段是否为插入字段
     * @return true是插入字段，false不是插入字段
     */
    public boolean isInsert() {
        return isInsert(this.isInsert);
    }

    /**
     * 静态方法判断字段是否为插入字段
     * @param isInsert 是否插入标记（1是）
     * @return true是插入字段，false不是插入字段
     */
    public boolean isInsert(String isInsert) {
        return isInsert != null && StringUtils.equals("1", isInsert);
    }

    /**
     * 判断当前字段是否为编辑字段
     * @return true是编辑字段，false不是编辑字段
     */
    public boolean isEdit() {
        return isEdit(this.isEdit);
    }

    /**
     * 静态方法判断字段是否为编辑字段
     * @param isEdit 是否编辑标记（1是）
     * @return true是编辑字段，false不是编辑字段
     */
    public boolean isEdit(String isEdit) {
        return isEdit != null && StringUtils.equals("1", isEdit);
    }

    /**
     * 判断当前字段是否为列表字段
     * @return true是列表字段，false不是列表字段
     */
    public boolean isList() {
        return isList(this.isList);
    }

    /**
     * 静态方法判断字段是否为列表字段
     * @param isList 是否列表标记（1是）
     * @return true是列表字段，false不是列表字段
     */
    public boolean isList(String isList) {
        return isList != null && StringUtils.equals("1", isList);
    }

    /**
     * 判断当前字段是否为查询字段
     * @return true是查询字段，false不是查询字段
     */
    public boolean isQuery() {
        return isQuery(this.isQuery);
    }

    /**
     * 静态方法判断字段是否为查询字段
     * @param isQuery 是否查询标记（1是）
     * @return true是查询字段，false不是查询字段
     */
    public boolean isQuery(String isQuery) {
        return isQuery != null && StringUtils.equals("1", isQuery);
    }

    /**
     * 判断当前字段是否为父类字段
     * 父类字段在BaseEntity中已定义，不需要重复生成
     * @return true是父类字段，false不是父类字段
     */
    public boolean isSuperColumn() {
        return isSuperColumn(this.javaField);
    }

    /**
     * 静态方法判断字段是否为父类字段
     * @param javaField Java字段名
     * @return true是父类字段，false不是父类字段
     */
    public static boolean isSuperColumn(String javaField) {
        return StringUtils.equalsAnyIgnoreCase(javaField,
            // BaseEntity中的基础字段
            "createBy", "createTime", "updateBy", "updateTime",
            // TreeEntity中的树形字段
            "parentName", "parentId");
    }

    /**
     * 判断当前字段是否为可用字段
     * 某些字段虽然在父类中定义，但在生成页面时需要用到，不能忽略
     * @return true是可用字段，false不是可用字段
     */
    public boolean isUsableColumn() {
        return isUsableColumn(javaField);
    }

    /**
     * 静态方法判断字段是否为可用字段
     * isSuperColumn()中的名单用于避免生成多余Domain属性，若某些属性在生成页面时需要用到不能忽略，则放在此处白名单
     * @param javaField Java字段名
     * @return true是可用字段，false不是可用字段
     */
    public static boolean isUsableColumn(String javaField) {
        // isSuperColumn()中的名单用于避免生成多余Domain属性，若某些属性在生成页面时需要用到不能忽略，则放在此处白名单
        return StringUtils.equalsAnyIgnoreCase(javaField, "parentId", "orderNum", "remark");
    }

    /**
     * 从列注释中提取字典转换表达式
     * 例如：列注释为"状态（0=正常 1=停用）"，提取出"0=正常,1=停用"
     * @return 字典转换表达式
     */
    public String readConverterExp() {
        // 使用StringUtils.substringBetween提取括号内的内容
        String remarks = StringUtils.substringBetween(this.columnComment, "（", "）");
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotEmpty(remarks)) {
            // 按空格分割，处理每个键值对
            for (String value : remarks.split(" ")) {
                if (StringUtils.isNotEmpty(value)) {
                    // 提取第一个字符作为key，剩余部分作为value
                    Object startStr = value.subSequence(0, 1);
                    String endStr = value.substring(1);
                    // 拼接成key=value格式，使用SEPARATOR分隔
                    sb.append(StringUtils.EMPTY).append(startStr).append("=").append(endStr).append(StringUtils.SEPARATOR);
                }
            }
            // 删除最后一个分隔符
            return sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            // 如果没有括号，返回原始注释
            return this.columnComment;
        }
    }
}
