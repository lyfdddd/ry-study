// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// EasyExcel注解：忽略未标注@ExcelProperty的字段，不导出到Excel
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
// EasyExcel注解：Excel属性注解，用于定义Excel列
import cn.idev.excel.annotation.ExcelProperty;
// EasyExcel注解：字典格式注解，用于将字典值转换为标签
import org.dromara.common.excel.annotation.ExcelDictFormat;
// EasyExcel转换器：字典转换器，实现字典值到标签的转换
import org.dromara.common.excel.convert.ExcelDictConvert;
// MapStruct-plus注解：自动映射注解，用于自动生成对象转换代码
import io.github.linpeilie.annotations.AutoMapper;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// SpEL表达式实体类：目标实体类，用于MapStruct-plus自动映射
import org.dromara.workflow.domain.FlowSpel;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 流程SpEL表达式定义视图对象
 * 用于封装SpEL表达式查询结果，返回给前端展示和Excel导出
 * 实现Serializable接口，支持序列化传输
 * 使用MapStruct-plus自动映射到FlowSpel实体类
 * 使用EasyExcel注解支持Excel导出功能
 *
 * @author Michelle.Chung
 * @date 2025-07-04
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// EasyExcel注解：忽略未标注@ExcelProperty的字段
@ExcelIgnoreUnannotated
// MapStruct-plus注解：指定目标映射类为FlowSpel
@AutoMapper(target = FlowSpel.class)
public class FlowSpelVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * SpEL表达式定义的唯一标识
     * Excel导出列名为"主键id"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "主键id")
    private Long id;

    /**
     * 组件名称
     * Spring Bean的名称，用于定位要调用的组件
     * Excel导出列名为"组件名称"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "组件名称")
    private String componentName;

    /**
     * 方法名
     * 要调用的方法名称
     * Excel导出列名为"方法名"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "方法名")
    private String methodName;

    /**
     * 方法参数
     * 方法调用时传递的参数，支持SpEL表达式
     * Excel导出列名为"参数"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "参数")
    private String methodParams;

    /**
     * 预览SpEL值
     * 完整的SpEL表达式预览，用于调试和验证
     * Excel导出列名为"预览spel值"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "预览spel值")
    private String viewSpel;

    /**
     * 状态（0正常 1停用）
     * 表达式的启用状态，0表示正常可用，1表示已停用
     * Excel导出列名为"状态"，使用ExcelDictConvert转换器
     */
    // EasyExcel注解：定义Excel列，value指定列名，converter指定转换器
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    // EasyExcel字典格式注解：定义字典值到标签的映射关系
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 备注
     * 表达式的说明和备注信息
     * Excel导出列名为"备注"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     * 表达式的创建时间，记录创建的时间点
     * Excel导出列名为"创建时间"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
