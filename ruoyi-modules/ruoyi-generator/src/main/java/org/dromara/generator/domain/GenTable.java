package org.dromara.generator.domain;

// 导入MyBatis-Plus字段策略注解，用于控制更新策略
// FieldStrategy枚举定义了字段更新策略：ALWAYS（总是更新）、NOT_NULL（非空更新）、NOT_EMPTY（非空字符串更新）
import com.baomidou.mybatisplus.annotation.FieldStrategy;
// 导入MyBatis-Plus表字段注解，用于标记非主键字段
import com.baomidou.mybatisplus.annotation.TableField;
// 导入MyBatis-Plus主键注解，用于标记主键字段
import com.baomidou.mybatisplus.annotation.TableId;
// 导入MyBatis-Plus表名注解，用于指定数据库表名
import com.baomidou.mybatisplus.annotation.TableName;
// 导入字符串工具类，提供字符串操作相关方法
import org.dromara.common.core.utils.StringUtils;
// 导入基础实体类，包含创建人、创建时间、更新人、更新时间等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;
// 导入代码生成常量类，包含模板类型、字段类型等常量定义
import org.dromara.generator.constant.GenConstants;
// 导入参数校验注解，用于验证请求参数
import jakarta.validation.Valid;
// 导入参数校验注解，用于验证字符串不能为空
import jakarta.validation.constraints.NotBlank;
// 导入Lombok的Data注解，自动生成getter/setter、toString、equals、hashCode等方法
import lombok.Data;
// 导入Lombok的EqualsAndHashCode注解，用于生成equals和hashCode方法
import lombok.EqualsAndHashCode;

// 导入List接口，用于存储字段列表
import java.util.List;

/**
 * 业务表 gen_table
 * 代码生成配置实体类，存储需要生成代码的表配置信息
 * 继承BaseEntity获得基础字段（创建人、创建时间、更新人、更新时间等）
 * 使用@Data注解简化POJO开发，自动生成getter/setter等方法
 * 使用@EqualsAndHashCode注解生成equals和hashCode方法，callSuper=true表示包含父类字段
 * 使用@TableName注解指定数据库表名为gen_table
 *
 * @author Lion Li
 */

// 使用@Data注解，自动生成getter、setter、toString、equals、hashCode等方法
@Data
// 使用@EqualsAndHashCode注解，生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// 使用@TableName注解指定数据库表名为gen_table
@TableName("gen_table")
public class GenTable extends BaseEntity {

    /**
     * 编号
     * 主键ID，对应数据库表gen_table的table_id字段
     * 使用@TableId注解标记为主键，value="table_id"指定数据库字段名
     */
    // 使用@TableId注解标记为主键，value="table_id"指定数据库字段名
    @TableId(value = "table_id")
    private Long tableId;

    /**
     * 数据源名称
     * 指定使用哪个数据源，对应动态数据源的key（如master、slave）
     * 不能为空，因为代码生成需要知道从哪个数据库读取表结构
     * 使用@NotBlank注解进行参数校验，message指定校验失败时的提示信息
     */
    // 使用@NotBlank注解进行参数校验，确保数据源名称不为空
    @NotBlank(message = "数据源名称不能为空")
    private String dataName;

    /**
     * 表名称
     * 数据库中的表名，如sys_user、gen_table
     * 不能为空，是代码生成的基础
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保表名称不为空
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 表描述
     * 表的注释信息，用于生成功能名称和页面标题
     * 不能为空，影响生成代码的质量
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保表描述不为空
    @NotBlank(message = "表描述不能为空")
    private String tableComment;

    /**
     * 关联父表的表名
     * 用于主子表模板，指定父表名称
     * 当生成主子表代码时，需要指定关联的父表
     */
    // 主子表关联的父表名称
    private String subTableName;

    /**
     * 本表关联父表的外键名
     * 用于主子表模板，指定关联外键字段
     * 当生成主子表代码时，需要指定关联的外键字段名
     */
    // 主子表关联的外键字段名
    private String subTableFkName;

    /**
     * 实体类名称(首字母大写)
     * 生成的Java类名，如SysUser、GenTable
     * 不能为空，是代码生成的核心配置
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保实体类名称不为空
    @NotBlank(message = "实体类名称不能为空")
    private String className;

    /**
     * 使用的模板（crud单表操作 tree树表操作 sub主子表操作）
     * 指定使用哪种代码生成模板，影响生成的代码结构
     * 可选值：crud（单表）、tree（树表）、sub（主子表）
     */
    // 代码生成模板类型：crud、tree、sub
    private String tplCategory;

    /**
     * 生成包路径
     * 生成的Java代码包路径，如org.dromara.system
     * 不能为空，决定代码的目录结构
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保生成包路径不为空
    @NotBlank(message = "生成包路径不能为空")
    private String packageName;

    /**
     * 生成模块名
     * 模块名称，如system、demo
     * 不能为空，用于组织代码和生成权限前缀
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保生成模块名不为空
    @NotBlank(message = "生成模块名不能为空")
    private String moduleName;

    /**
     * 生成业务名
     * 业务名称，如user、config
     * 不能为空，用于生成文件名和请求路径
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保生成业务名不为空
    @NotBlank(message = "生成业务名不能为空")
    private String businessName;

    /**
     * 生成功能名
     * 功能描述，如用户管理、参数设置
     * 不能为空，用于生成页面标题和菜单名称
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保生成功能名不为空
    @NotBlank(message = "生成功能名不能为空")
    private String functionName;

    /**
     * 生成作者
     * 代码作者，写入类注释中
     * 不能为空，标识代码创建者
     * 使用@NotBlank注解进行参数校验
     */
    // 使用@NotBlank注解进行参数校验，确保作者不为空
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;

    /**
     * 生成代码方式（0zip压缩包 1自定义路径）
     * 0表示下载zip包，1表示生成到项目指定路径
     * 默认值为0，通过界面选择
     */
    // 生成代码方式：0-zip压缩包，1-自定义路径
    private String genType;

    /**
     * 生成路径（不填默认项目路径）
     * 当genType为1时，指定代码生成的目标路径
     * 更新策略为NOT_EMPTY，只在非空时更新
     * 使用@TableField注解指定更新策略，避免更新空字符串覆盖原有值
     */
    // 使用@TableField注解指定更新策略为NOT_EMPTY，只在非空时更新
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String genPath;

    /**
     * 主键信息
     * 主键字段信息，非数据库字段，用于代码生成
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private GenTableColumn pkColumn;

    /**
     * 表列信息
     * 字段列表，非数据库字段，用于存储关联的字段配置
     * @Valid注解表示级联校验字段列表中的对象
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@Valid注解进行级联校验，确保字段列表中的对象也经过校验
    @Valid
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private List<GenTableColumn> columns;

    /**
     * 其它生成选项
     * JSON格式的扩展配置，存储树形结构、上级菜单等额外信息
     * 例如：{"parentMenuId": "3", "treeCode": "deptId", "treeParentCode": "parentId", "treeName": "deptName"}
     */
    // JSON格式的扩展配置
    private String options;

    /**
     * 备注
     * 代码生成配置的备注信息
     * 用于记录额外的说明或注意事项
     */
    // 备注信息
    private String remark;

    /**
     * 树编码字段
     * 树形结构时使用的编码字段，非数据库字段
     * 从options配置中解析得到，用于树表模板
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private String treeCode;

    /**
     * 树父编码字段
     * 树形结构时使用的父编码字段，非数据库字段
     * 从options配置中解析得到，用于树表模板
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private String treeParentCode;

    /**
     * 树名称字段
     * 树形结构时使用的名称字段，非数据库字段
     * 从options配置中解析得到，用于树表模板
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private String treeName;

    /*
     * 菜单id列表
     * 生成的菜单ID列表，非数据库字段
     * 用于记录生成的菜单ID，方便后续管理
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private List<Long> menuIds;

    /**
     * 上级菜单ID字段
     * 指定生成菜单的上级菜单ID，非数据库字段
     * 从options配置中解析得到，用于生成菜单时指定上级菜单
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private Long parentMenuId;

    /**
     * 上级菜单名称字段
     * 指定生成菜单的上级菜单名称，非数据库字段
     * 从options配置中解析得到，用于显示上级菜单名称
     * 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
     */
    // 使用@TableField(exist = false)注解表示该字段不存在于数据库表中
    @TableField(exist = false)
    private String parentMenuName;

    /**
     * 判断是否为树表模板
     * 根据tplCategory字段判断当前配置是否为树表模板
     * @return true是树表，false不是树表
     */
    // 实例方法，调用静态方法isTree进行判断
    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    /**
     * 静态方法判断模板类型是否为树表
     * 使用StringUtils.equals方法比较，避免空指针异常
     * @param tplCategory 模板类型
     * @return true是树表，false不是树表
     */
    // 静态方法，使用StringUtils.equals比较模板类型是否为tree
    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }

    /**
     * 判断是否为单表CRUD模板
     * 根据tplCategory字段判断当前配置是否为单表CRUD模板
     * @return true是单表，false不是单表
     */
    // 实例方法，调用静态方法isCrud进行判断
    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    /**
     * 静态方法判断模板类型是否为单表CRUD
     * 使用StringUtils.equals方法比较，避免空指针异常
     * @param tplCategory 模板类型
     * @return true是单表，false不是单表
     */
    // 静态方法，使用StringUtils.equals比较模板类型是否为crud
    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
    }

    /**
     * 判断是否为父类字段
     * 实例方法，调用静态方法isSuperColumn进行判断
     * @param javaField Java字段名
     * @return true是父类字段，false不是父类字段
     */
    // 实例方法，调用静态方法isSuperColumn进行判断
    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }

    /**
     * 静态方法判断字段是否为父类字段
     * 父类字段在BaseEntity中已定义，不需要重复生成
     * 使用StringUtils.equalsAnyIgnoreCase方法进行不区分大小写的比较
     * @param tplCategory 模板类型
     * @param javaField Java字段名
     * @return true是父类字段，false不是父类字段
     */
    // 静态方法，使用StringUtils.equalsAnyIgnoreCase比较字段是否在BASE_ENTITY数组中
    public static boolean isSuperColumn(String tplCategory, String javaField) {
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }
}
