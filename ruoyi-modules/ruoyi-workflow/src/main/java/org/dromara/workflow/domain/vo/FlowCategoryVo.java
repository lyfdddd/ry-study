// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// EasyExcel注解：忽略未标注@ExcelProperty的字段，不导出到Excel
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
// EasyExcel注解：Excel属性注解，用于定义Excel列
import cn.idev.excel.annotation.ExcelProperty;
// MapStruct-plus注解：自动映射注解，用于自动生成对象转换代码
import io.github.linpeilie.annotations.AutoMapper;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 翻译注解：用于字段值翻译，将ID翻译为名称
import org.dromara.common.translation.annotation.Translation;
// 工作流常量：定义工作流相关的常量
import org.dromara.workflow.common.constant.FlowConstant;
// 流程分类实体类：目标实体类，用于MapStruct-plus自动映射
import org.dromara.workflow.domain.FlowCategory;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 流程分类视图对象
 * 用于封装流程分类查询结果，返回给前端展示和Excel导出
 * 实现Serializable接口，支持序列化传输
 * 使用MapStruct-plus自动映射到FlowCategory实体类
 * 使用EasyExcel注解支持Excel导出功能
 *
 * @author may
 * @date 2023-06-27
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// EasyExcel注解：忽略未标注@ExcelProperty的字段
@ExcelIgnoreUnannotated
// MapStruct-plus注解：指定目标映射类为FlowCategory
@AutoMapper(target = FlowCategory.class)
public class FlowCategoryVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流程分类ID
     * 流程分类的唯一标识
     * Excel导出列名为"流程分类ID"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "流程分类ID")
    private Long categoryId;

    /**
     * 父级分类ID
     * 父级分类的唯一标识，用于构建树形结构
     * 顶级分类的parentId通常为0
     */
    private Long parentId;

    /**
     * 父级分类名称
     * 父级分类的名称，用于前端展示
     * 使用@Translation注解将parentId翻译为名称
     */
    // 翻译注解：将category ID翻译为名称，使用FlowConstant.CATEGORY_ID_TO_NAME翻译器
    @Translation(type = FlowConstant.CATEGORY_ID_TO_NAME, mapper = "parentId")
    private String parentName;

    /**
     * 祖级列表
     * 从当前分类到顶级分类的所有祖先ID列表，用逗号分隔
     * 用于树形结构查询和权限控制
     */
    private String ancestors;

    /**
     * 流程分类名称
     * 流程分类的名称，如"人事流程"、"财务流程"
     * Excel导出列名为"流程分类名称"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "流程分类名称")
    private String categoryName;

    /**
     * 显示顺序
     * 分类在列表中的显示顺序，数值越小越靠前
     * Excel导出列名为"显示顺序"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "显示顺序")
    private Long orderNum;

    /**
     * 创建时间
     * 分类的创建时间，记录分类创建的时间点
     * Excel导出列名为"创建时间"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
