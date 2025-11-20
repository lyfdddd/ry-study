package org.dromara.generator.constant;

/**
 * 代码生成通用常量
 * 该接口定义了代码生成过程中使用的所有常量，包括模板类型、数据库类型映射、HTML控件类型等
 * 使用接口定义常量，方便其他类直接引用，无需实例化
 * 这是代码生成模块的核心常量定义，所有模板和工具类都依赖这些常量
 *
 * @author ruoyi
 */
public interface GenConstants {
    /**
     * 单表（增删改查）模板类型
     * 用于标识生成普通的CRUD代码
     * 值为"crud"，对应普通表格的增删改查操作
     */
    String TPL_CRUD = "crud";

    /**
     * 树表（增删改查）模板类型
     * 用于标识生成树形结构的表代码，支持层级关系
     * 值为"tree"，对应树形结构的增删改查操作，支持父子节点关系
     */
    String TPL_TREE = "tree";

    /**
     * 树编码字段名称
     * 在树形结构中，用于存储节点编码的字段名
     * 值为"treeCode"，在options配置中使用
     */
    String TREE_CODE = "treeCode";

    /**
     * 树父编码字段名称
     * 在树形结构中，用于存储父节点编码的字段名
     * 值为"treeParentCode"，在options配置中使用
     */
    String TREE_PARENT_CODE = "treeParentCode";

    /**
     * 树名称字段名称
     * 在树形结构中，用于存储节点名称的字段名
     * 值为"treeName"，在options配置中使用
     */
    String TREE_NAME = "treeName";

    /**
     * 上级菜单ID字段名称
     * 用于生成菜单时，指定上级菜单的ID字段
     * 值为"parentMenuId"，在options配置中使用
     */
    String PARENT_MENU_ID = "parentMenuId";

    /**
     * 上级菜单名称字段名称
     * 用于生成菜单时，指定上级菜单的名称字段
     * 值为"parentMenuName"，在options配置中使用
     */
    String PARENT_MENU_NAME = "parentMenuName";

    /**
     * 数据库字符串类型数组
     * 包含所有数据库字符串类型的关键字，用于类型映射
     * 支持多种数据库：MySQL、Oracle、PostgreSQL、SQL Server等
     * 当数据库字段类型包含这些关键字时，会映射为Java的String类型
     */
    String[] COLUMNTYPE_STR = {"char", "varchar", "enum", "set", "nchar", "nvarchar", "varchar2", "nvarchar2"};

    /**
     * 数据库文本类型数组
     * 包含所有数据库大文本类型的关键字，用于类型映射
     * 当字段长度超过500或属于文本类型时，会生成textarea控件
     * 支持多种数据库的文本类型：tinytext、text、mediumtext、longtext、blob等
     */
    String[] COLUMNTYPE_TEXT = {"tinytext", "text", "mediumtext", "longtext", "binary", "varbinary", "blob",
        "ntext", "image", "bytea"};

    /**
     * 数据库时间类型数组
     * 包含所有数据库时间类型的关键字，用于类型映射
     * 识别为时间类型后，会生成日期时间选择控件
     * 支持多种数据库的时间类型：datetime、timestamp、date、time等
     */
    String[] COLUMNTYPE_TIME = {"datetime", "time", "date", "timestamp", "year", "interval",
        "smalldatetime", "datetime2", "datetimeoffset", "timestamptz"};

    /**
     * 数据库数字类型数组
     * 包含所有数据库数字类型的关键字，用于类型映射
     * 识别为数字类型后，默认映射为Long类型，可在界面调整
     * 支持多种数据库的数字类型：int、bigint、decimal、numeric、float、double等
     */
    String[] COLUMNTYPE_NUMBER = {"tinyint", "smallint", "mediumint", "int", "int2", "int4", "int8", "number", "integer",
        "bit", "bigint", "float", "float4", "float8", "double", "decimal", "numeric", "real", "double precision",
        "smallserial", "serial", "bigserial", "money", "smallmoney"};

    /**
     * BO对象 不需要添加的字段数组
     * 这些字段是系统基础字段，在新增时不需要用户填写，由系统自动生成
     * 包括：创建部门、创建人、创建时间、删除标志、更新人、更新时间、版本号、租户ID
     */
    String[] COLUMNNAME_NOT_ADD = {"create_dept", "create_by", "create_time", "del_flag", "update_by",
        "update_time", "version", "tenant_id"};

    /**
     * BO对象 不需要编辑的字段数组
     * 这些字段是系统基础字段，在编辑时不允许修改，保持原始值
     * 包括：创建部门、创建人、创建时间、删除标志、更新人、更新时间、版本号、租户ID
     */
    String[] COLUMNNAME_NOT_EDIT = {"create_dept", "create_by", "create_time", "del_flag", "update_by",
        "update_time", "version", "tenant_id"};

    /**
     * VO对象 不需要返回的字段数组
     * 这些字段是系统内部字段，不需要返回给前端展示
     * 包括：创建部门、创建人、创建时间、删除标志、更新人、更新时间、版本号、租户ID
     */
    String[] COLUMNNAME_NOT_LIST = {"create_dept", "create_by", "create_time", "del_flag", "update_by",
        "update_time", "version", "tenant_id"};

    /**
     * BO对象 不需要查询的字段数组
     * 这些字段不适合作为查询条件，如创建时间、更新时间等
     * 包括：ID、创建部门、创建人、创建时间、删除标志、更新人、更新时间、备注、版本号、租户ID
     */
    String[] COLUMNNAME_NOT_QUERY = {"id", "create_dept", "create_by", "create_time", "del_flag", "update_by",
        "update_time", "remark", "version", "tenant_id"};

    /**
     * Entity基类字段数组
     * 定义实体类的基础字段，这些字段在BaseEntity中已定义
     * 包括：创建部门、创建人、创建时间、更新人、更新时间、租户ID
     */
    String[] BASE_ENTITY = {"createDept", "createBy", "createTime", "updateBy", "updateTime", "tenantId"};

    /**
     * HTML文本框控件类型
     * 用于生成input输入框
     * 值为"input"，对应前端Element UI的el-input组件
     */
    String HTML_INPUT = "input";

    /**
     * HTML文本域控件类型
     * 用于生成textarea多行文本框
     * 值为"textarea"，对应前端Element UI的el-input type="textarea"组件
     */
    String HTML_TEXTAREA = "textarea";

    /**
     * HTML下拉框控件类型
     * 用于生成select下拉选择框
     * 值为"select"，对应前端Element UI的el-select组件
     */
    String HTML_SELECT = "select";

    /**
     * HTML单选框控件类型
     * 用于生成radio单选按钮组
     * 值为"radio"，对应前端Element UI的el-radio-group组件
     */
    String HTML_RADIO = "radio";

    /**
     * HTML复选框控件类型
     * 用于生成checkbox多选框
     * 值为"checkbox"，对应前端Element UI的el-checkbox-group组件
     */
    String HTML_CHECKBOX = "checkbox";

    /**
     * HTML日期时间控件类型
     * 用于生成日期时间选择器
     * 值为"datetime"，对应前端Element UI的el-date-picker组件
     */
    String HTML_DATETIME = "datetime";

    /**
     * HTML图片上传控件类型
     * 用于生成图片上传组件
     * 值为"imageUpload"，对应前端自定义的图片上传组件
     */
    String HTML_IMAGE_UPLOAD = "imageUpload";

    /**
     * HTML文件上传控件类型
     * 用于生成文件上传组件
     * 值为"fileUpload"，对应前端自定义的文件上传组件
     */
    String HTML_FILE_UPLOAD = "fileUpload";

    /**
     * HTML富文本控件类型
     * 用于生成富文本编辑器
     * 值为"editor"，对应前端的富文本编辑器组件
     */
    String HTML_EDITOR = "editor";

    /**
     * Java字符串类型
     * 对应数据库的字符串类型
     * 值为"String"，是Java的字符串类型
     */
    String TYPE_STRING = "String";

    /**
     * Java整型
     * 对应数据库的整数类型
     * 值为"Integer"，是Java的整数包装类型
     */
    String TYPE_INTEGER = "Integer";

    /**
     * Java长整型
     * 对应数据库的长整数类型，是数字类型的默认映射
     * 值为"Long"，是Java的长整数包装类型
     */
    String TYPE_LONG = "Long";

    /**
     * Java浮点型
     * 对应数据库的浮点数类型
     * 值为"Double"，是Java的双精度浮点数包装类型
     */
    String TYPE_DOUBLE = "Double";

    /**
     * Java高精度计算类型
     * 对应数据库的decimal、numeric等精确数值类型
     * 值为"BigDecimal"，用于金融计算等需要精确数值的场景
     */
    String TYPE_BIGDECIMAL = "BigDecimal";

    /**
     * Java时间类型
     * 对应数据库的日期时间类型
     * 值为"Date"，是Java的日期时间类型
     */
    String TYPE_DATE = "Date";

    /**
     * 模糊查询方式
     * 在SQL中使用LIKE进行模糊匹配
     * 值为"LIKE"，对应MyBatis-Plus的模糊查询条件构造器
     */
    String QUERY_LIKE = "LIKE";

    /**
     * 相等查询方式
     * 在SQL中使用=进行精确匹配
     * 值为"EQ"，对应MyBatis-Plus的相等查询条件构造器
     */
    String QUERY_EQ = "EQ";

    /**
     * 需要标识
     * 值为"1"，表示需要、启用或勾选状态
     * 在代码生成配置中，1表示启用，0表示禁用
     */
    String REQUIRE = "1";
}
