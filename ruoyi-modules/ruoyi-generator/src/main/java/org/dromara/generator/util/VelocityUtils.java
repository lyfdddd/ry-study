package org.dromara.generator.util;

// 导入Hutool的集合工具类
import cn.hutool.core.collection.CollUtil;
// 导入Hutool的类型转换工具类
import cn.hutool.core.convert.Convert;
// 导入Hutool的字典类
import cn.hutool.core.lang.Dict;
// 导入数据库类型枚举
import org.dromara.common.mybatis.enums.DataBaseType;
// 导入代码生成常量类
import org.dromara.generator.constant.GenConstants;
// 导入日期工具类
import org.dromara.common.core.utils.DateUtils;
// 导入字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 导入JSON工具类
import org.dromara.common.json.utils.JsonUtils;
// 导入数据库助手类
import org.dromara.common.mybatis.helper.DataBaseHelper;
// 导入代码生成表实体类
import org.dromara.generator.domain.GenTable;
// 导入代码生成表字段实体类
import org.dromara.generator.domain.GenTableColumn;
// 导入Lombok的访问级别枚举
import lombok.AccessLevel;
// 导入Lombok的无参构造函数注解，并设置访问级别为PRIVATE，防止实例化
import lombok.NoArgsConstructor;
// 导入Velocity上下文类
import org.apache.velocity.VelocityContext;

import java.util.*;

/**
 * 模板处理工具类
 * 提供Velocity模板引擎的上下文准备、模板列表获取、文件名生成等功能
 * 使用私有构造函数防止实例化，符合工具类设计模式
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VelocityUtils {

    /**
     * 项目空间路径
     * Java源代码的根目录路径
     */
    private static final String PROJECT_PATH = "main/java";

    /**
     * mybatis空间路径
     * MyBatis Mapper XML文件的根目录路径
     */
    private static final String MYBATIS_PATH = "main/resources/mapper";

    /**
     * 默认上级菜单，系统工具
     * 生成菜单时的默认上级菜单ID
     */
    private static final String DEFAULT_PARENT_MENU_ID = "3";

    /**
     * 设置模板变量信息
     * 准备Velocity模板引擎需要的所有上下文变量
     * 包括表信息、包信息、作者信息、字段列表等
     *
     * @param genTable 代码生成表对象
     * @return VelocityContext 模板上下文对象
     */
    public static VelocityContext prepareContext(GenTable genTable) {
        // 获取模块名
        String moduleName = genTable.getModuleName();
        // 获取业务名
        String businessName = genTable.getBusinessName();
        // 获取包路径
        String packageName = genTable.getPackageName();
        // 获取模板类型
        String tplCategory = genTable.getTplCategory();
        // 获取功能名
        String functionName = genTable.getFunctionName();

        // 创建Velocity上下文对象
        VelocityContext velocityContext = new VelocityContext();
        // 将模板类型放入上下文
        velocityContext.put("tplCategory", genTable.getTplCategory());
        // 将表名放入上下文
        velocityContext.put("tableName", genTable.getTableName());
        // 将功能名放入上下文，如果为空则使用默认值
        velocityContext.put("functionName", StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
        // 将类名（首字母大写）放入上下文
        velocityContext.put("ClassName", genTable.getClassName());
        // 将类名（首字母小写）放入上下文
        velocityContext.put("className", StringUtils.uncapitalize(genTable.getClassName()));
        // 将模块名放入上下文
        velocityContext.put("moduleName", genTable.getModuleName());
        // 将业务名（首字母大写）放入上下文
        velocityContext.put("BusinessName", StringUtils.capitalize(genTable.getBusinessName()));
        // 将业务名放入上下文
        velocityContext.put("businessName", genTable.getBusinessName());
        // 将基础包路径（去掉最后一层）放入上下文
        velocityContext.put("basePackage", getPackagePrefix(packageName));
        // 将完整包路径放入上下文
        velocityContext.put("packageName", packageName);
        // 将作者信息放入上下文
        velocityContext.put("author", genTable.getFunctionAuthor());
        // 将当前日期放入上下文
        velocityContext.put("datetime", DateUtils.getDate());
        // 将主键列信息放入上下文
        velocityContext.put("pkColumn", genTable.getPkColumn());
        // 将需要导入的包列表放入上下文
        velocityContext.put("importList", getImportList(genTable));
        // 将权限前缀放入上下文
        velocityContext.put("permissionPrefix", getPermissionPrefix(moduleName, businessName));
        // 将字段列表放入上下文
        velocityContext.put("columns", genTable.getColumns());
        // 将表对象放入上下文
        velocityContext.put("table", genTable);
        // 将字典列表放入上下文
        velocityContext.put("dicts", getDicts(genTable));
        // 设置菜单相关的上下文变量
        setMenuVelocityContext(velocityContext, genTable);
        // 如果是树表模板，设置树相关的上下文变量
        if (GenConstants.TPL_TREE.equals(tplCategory)) {
            setTreeVelocityContext(velocityContext, genTable);
        }
        return velocityContext;
    }

    /**
     * 设置菜单相关的Velocity上下文变量
     * 从options配置中解析上级菜单ID
     *
     * @param context Velocity上下文
     * @param genTable 代码生成表对象
     */
    public static void setMenuVelocityContext(VelocityContext context, GenTable genTable) {
        // 获取options配置
        String options = genTable.getOptions();
        // 解析为Dict对象
        Dict paramsObj = JsonUtils.parseMap(options);
        // 获取上级菜单ID
        String parentMenuId = getParentMenuId(paramsObj);
        // 将上级菜单ID放入上下文
        context.put("parentMenuId", parentMenuId);
    }

    /**
     * 设置树表相关的Velocity上下文变量
     * 从options配置中解析树编码、树父编码、树名称等
     *
     * @param context Velocity上下文
     * @param genTable 代码生成表对象
     */
    public static void setTreeVelocityContext(VelocityContext context, GenTable genTable) {
        // 获取options配置
        String options = genTable.getOptions();
        // 解析为Dict对象
        Dict paramsObj = JsonUtils.parseMap(options);
        // 获取树编码
        String treeCode = getTreecode(paramsObj);
        // 获取树父编码
        String treeParentCode = getTreeParentCode(paramsObj);
        // 获取树名称
        String treeName = getTreeName(paramsObj);

        // 将树编码放入上下文
        context.put("treeCode", treeCode);
        // 将树父编码放入上下文
        context.put("treeParentCode", treeParentCode);
        // 将树名称放入上下文
        context.put("treeName", treeName);
        // 将展开列序号放入上下文
        context.put("expandColumn", getExpandColumn(genTable));
        // 如果配置中包含树父编码，将原始值放入上下文
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_CODE)) {
            context.put("tree_parent_code", paramsObj.get(GenConstants.TREE_PARENT_CODE));
        }
        // 如果配置中包含树名称，将原始值放入上下文
        if (paramsObj.containsKey(GenConstants.TREE_NAME)) {
            context.put("tree_name", paramsObj.get(GenConstants.TREE_NAME));
        }
    }

    /**
     * 获取模板信息
     * 根据模板类型返回需要渲染的模板文件列表
     * 包括Java类模板、Mapper模板、Service模板、Controller模板、Vue页面模板等
     *
     * @param tplCategory 模板类型（crud/tree）
     * @return 模板文件路径列表
     */
    public static List<String> getTemplateList(String tplCategory) {
        // 创建模板列表
        List<String> templates = new ArrayList<>();
        // 添加Java实体类模板
        templates.add("vm/java/domain.java.vm");
        // 添加VO类模板
        templates.add("vm/java/vo.java.vm");
        // 添加BO类模板
        templates.add("vm/java/bo.java.vm");
        // 添加Mapper接口模板
        templates.add("vm/java/mapper.java.vm");
        // 添加Service接口模板
        templates.add("vm/java/service.java.vm");
        // 添加Service实现类模板
        templates.add("vm/java/serviceImpl.java.vm");
        // 添加Controller类模板
        templates.add("vm/java/controller.java.vm");
        // 添加Mapper XML模板
        templates.add("vm/xml/mapper.xml.vm");
        // 获取当前数据库类型
        DataBaseType dataBaseType = DataBaseHelper.getDataBaseType();
        // 根据数据库类型添加对应的SQL脚本模板
        if (dataBaseType.isOracle()) {
            templates.add("vm/sql/oracle/sql.vm");
        } else if (dataBaseType.isPostgreSql()) {
            templates.add("vm/sql/postgres/sql.vm");
        } else if (dataBaseType.isSqlServer()) {
            templates.add("vm/sql/sqlserver/sql.vm");
        } else {
            templates.add("vm/sql/sql.vm");
        }
        // 添加TypeScript API模板
        templates.add("vm/ts/api.ts.vm");
        // 添加TypeScript类型定义模板
        templates.add("vm/ts/types.ts.vm");
        // 根据模板类型添加对应的Vue页面模板
        if (GenConstants.TPL_CRUD.equals(tplCategory)) {
            templates.add("vm/vue/index.vue.vm");
        } else if (GenConstants.TPL_TREE.equals(tplCategory)) {
            templates.add("vm/vue/index-tree.vue.vm");
        }
        return templates;
    }

    /**
     * 获取文件名
     * 根据模板类型和表信息生成对应的文件路径
     * 支持Java类文件、Mapper XML文件、SQL脚本文件、Vue文件等
     *
     * @param template 模板文件路径
     * @param genTable 代码生成表对象
     * @return 生成的文件路径
     */
    public static String getFileName(String template, GenTable genTable) {
        // 初始化文件名为空字符串
        String fileName = "";
        // 获取包路径
        String packageName = genTable.getPackageName();
        // 获取模块名
        String moduleName = genTable.getModuleName();
        // 获取类名（首字母大写）
        String className = genTable.getClassName();
        // 获取业务名
        String businessName = genTable.getBusinessName();

        // 构建Java源码路径
        String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/");
        // 构建MyBatis XML路径
        String mybatisPath = MYBATIS_PATH + "/" + moduleName;
        // Vue前端路径
        String vuePath = "vue";

        // 根据模板类型生成对应的文件路径
        if (template.contains("domain.java.vm")) {
            // Java实体类文件路径
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        }
        if (template.contains("vo.java.vm")) {
            // VO类文件路径
            fileName = StringUtils.format("{}/domain/vo/{}Vo.java", javaPath, className);
        }
        if (template.contains("bo.java.vm")) {
            // BO类文件路径
            fileName = StringUtils.format("{}/domain/bo/{}Bo.java", javaPath, className);
        }
        if (template.contains("mapper.java.vm")) {
            // Mapper接口文件路径
            fileName = StringUtils.format("{}/mapper/{}Mapper.java", javaPath, className);
        } else if (template.contains("service.java.vm")) {
            // Service接口文件路径
            fileName = StringUtils.format("{}/service/I{}Service.java", javaPath, className);
        } else if (template.contains("serviceImpl.java.vm")) {
            // Service实现类文件路径
            fileName = StringUtils.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        } else if (template.contains("controller.java.vm")) {
            // Controller类文件路径
            fileName = StringUtils.format("{}/controller/{}Controller.java", javaPath, className);
        } else if (template.contains("mapper.xml.vm")) {
            // Mapper XML文件路径
            fileName = StringUtils.format("{}/{}Mapper.xml", mybatisPath, className);
        } else if (template.contains("sql.vm")) {
            // SQL脚本文件路径
            fileName = businessName + "Menu.sql";
        } else if (template.contains("api.ts.vm")) {
            // TypeScript API文件路径
            fileName = StringUtils.format("{}/api/{}/{}/index.ts", vuePath, moduleName, businessName);
        } else if (template.contains("types.ts.vm")) {
            // TypeScript类型定义文件路径
            fileName = StringUtils.format("{}/api/{}/{}/types.ts", vuePath, moduleName, businessName);
        } else if (template.contains("index.vue.vm")) {
            // Vue页面文件路径
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        } else if (template.contains("index-tree.vue.vm")) {
            // Vue树表页面文件路径
            fileName = StringUtils.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        }
        return fileName;
    }

    /**
     * 获取包前缀
     * 从完整包路径中提取父级包路径（去掉最后一层）
     * 例如：org.dromara.system -> org.dromara
     *
     * @param packageName 包名称
     * @return 包前缀名称
     */
    public static String getPackagePrefix(String packageName) {
        // 查找最后一个点的位置
        int lastIndex = packageName.lastIndexOf(".");
        // 截取从开头到最后一个点之前的部分
        return StringUtils.substring(packageName, 0, lastIndex);
    }

    /**
     * 根据列类型获取导入包
     * 分析字段列表，确定需要导入的Java包
     * 包括Date类型需要的java.util.Date和@JsonFormat注解
     * BigDecimal类型需要的java.math.BigDecimal
     * 图片上传需要的翻译注解等
     *
     * @param genTable 业务表对象
     * @return 返回需要导入的包列表（使用HashSet去重）
     */
    public static HashSet<String> getImportList(GenTable genTable) {
        // 获取字段列表
        List<GenTableColumn> columns = genTable.getColumns();
        // 创建HashSet存储导入包（自动去重）
        HashSet<String> importList = new HashSet<>();
        // 遍历字段列表
        for (GenTableColumn column : columns) {
            // 如果不是父类字段且Java类型为Date
            if (!column.isSuperColumn() && GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                // 添加Date类导入
                importList.add("java.util.Date");
                // 添加JsonFormat注解导入
                importList.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if (!column.isSuperColumn() && GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                // 如果不是父类字段且Java类型为BigDecimal，添加BigDecimal类导入
                importList.add("java.math.BigDecimal");
            } else if (!column.isSuperColumn() && "imageUpload".equals(column.getHtmlType())) {
                // 如果不是父类字段且HTML类型为图片上传，添加翻译注解导入
                importList.add("org.dromara.common.translation.annotation.Translation");
                importList.add("org.dromara.common.translation.constant.TransConstant");
            }
        }
        return importList;
    }

    /**
     * 根据列类型获取字典组
     * 分析字段列表，提取需要使用的字典类型
     * 只包含使用select、radio、checkbox控件的字段
     *
     * @param genTable 业务表对象
     * @return 返回字典组字符串（逗号分隔）
     */
    public static String getDicts(GenTable genTable) {
        // 获取字段列表
        List<GenTableColumn> columns = genTable.getColumns();
        // 创建Set存储字典类型（自动去重）
        Set<String> dicts = new HashSet<>();
        // 添加字典到Set
        addDicts(dicts, columns);
        // 使用逗号和空格连接成字符串
        return StringUtils.join(dicts, ", ");
    }

    /**
     * 添加字典列表
     * 将字段中定义的字典类型添加到Set中
     * 只处理使用select、radio、checkbox控件的字段
     *
     * @param dicts 字典列表（Set集合，自动去重）
     * @param columns 列集合
     */
    public static void addDicts(Set<String> dicts, List<GenTableColumn> columns) {
        // 遍历字段列表
        for (GenTableColumn column : columns) {
            // 如果不是父类字段且字典类型不为空且HTML类型是select、radio或checkbox
            if (!column.isSuperColumn() && StringUtils.isNotEmpty(column.getDictType()) && StringUtils.equalsAny(
                column.getHtmlType(),
                new String[] { GenConstants.HTML_SELECT, GenConstants.HTML_RADIO, GenConstants.HTML_CHECKBOX })) {
                // 将字典类型添加到Set中（使用单引号包裹）
                dicts.add("'" + column.getDictType() + "'");
            }
        }
    }

    /**
     * 获取权限前缀
     * 生成权限字符串，格式为：模块名:业务名
     * 例如：system:user
     *
     * @param moduleName   模块名称
     * @param businessName 业务名称
     * @return 返回权限前缀
     */
    public static String getPermissionPrefix(String moduleName, String businessName) {
        // 使用StringUtils.format格式化字符串
        return StringUtils.format("{}:{}", moduleName, businessName);
    }

    /**
     * 获取上级菜单ID字段
     * 从配置参数中获取上级菜单ID，如果未配置则使用默认值
     *
     * @param paramsObj 生成其他选项（Dict对象）
     * @return 上级菜单ID字段
     */
    public static String getParentMenuId(Dict paramsObj) {
        // 如果参数对象不为空且包含上级菜单ID且值不为空
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.PARENT_MENU_ID)
            && StringUtils.isNotEmpty(paramsObj.getStr(GenConstants.PARENT_MENU_ID))) {
            // 返回配置的上级菜单ID
            return paramsObj.getStr(GenConstants.PARENT_MENU_ID);
        }
        // 返回默认上级菜单ID（系统工具）
        return DEFAULT_PARENT_MENU_ID;
    }

    /**
     * 获取树编码
     * 从配置参数中获取树编码字段，并转换为驼峰命名
     *
     * @param paramsObj 生成其他选项（Map对象）
     * @return 树编码（驼峰命名）
     */
    public static String getTreecode(Map<String, Object> paramsObj) {
        // 如果参数对象不为空且包含树编码
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_CODE)) {
            // 获取值并转换为字符串，然后转换为驼峰命名
            return StringUtils.toCamelCase(Convert.toStr(paramsObj.get(GenConstants.TREE_CODE)));
        }
        // 返回空字符串
        return StringUtils.EMPTY;
    }

    /**
     * 获取树父编码
     * 从配置参数中获取树父编码字段，并转换为驼峰命名
     *
     * @param paramsObj 生成其他选项（Dict对象）
     * @return 树父编码（驼峰命名）
     */
    public static String getTreeParentCode(Dict paramsObj) {
        // 如果参数对象不为空且包含树父编码
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_PARENT_CODE)) {
            // 获取值并转换为驼峰命名
            return StringUtils.toCamelCase(paramsObj.getStr(GenConstants.TREE_PARENT_CODE));
        }
        // 返回空字符串
        return StringUtils.EMPTY;
    }

    /**
     * 获取树名称
     * 从配置参数中获取树名称字段，并转换为驼峰命名
     *
     * @param paramsObj 生成其他选项（Dict对象）
     * @return 树名称（驼峰命名）
     */
    public static String getTreeName(Dict paramsObj) {
        // 如果参数对象不为空且包含树名称
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_NAME)) {
            // 获取值并转换为驼峰命名
            return StringUtils.toCamelCase(paramsObj.getStr(GenConstants.TREE_NAME));
        }
        // 返回空字符串
        return StringUtils.EMPTY;
    }

    /**
     * 获取需要在哪一列上面显示展开按钮
     * 计算树形表格中展开按钮应该显示在第几列
     * 遍历字段列表，找到树名称字段的位置
     *
     * @param genTable 业务表对象
     * @return 展开按钮列序号（从1开始）
     */
    public static int getExpandColumn(GenTable genTable) {
        // 获取options配置
        String options = genTable.getOptions();
        // 解析为Dict对象
        Dict paramsObj = JsonUtils.parseMap(options);
        // 获取树名称字段
        String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
        // 初始化列序号
        int num = 0;
        // 遍历字段列表
        for (GenTableColumn column : genTable.getColumns()) {
            // 如果是列表字段
            if (column.isList()) {
                // 列序号递增
                num++;
                // 获取字段名
                String columnName = column.getColumnName();
                // 如果当前字段是树名称字段
                if (columnName.equals(treeName)) {
                    // 跳出循环，返回当前列序号
                    break;
                }
            }
        }
        return num;
    }
}
