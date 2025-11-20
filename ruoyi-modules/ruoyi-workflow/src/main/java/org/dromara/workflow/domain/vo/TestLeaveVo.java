// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// EasyExcel注解：忽略未标注@ExcelProperty的字段，不导出到Excel
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
// EasyExcel注解：Excel属性注解，用于定义Excel列
import cn.idev.excel.annotation.ExcelProperty;
// Jackson注解：JSON格式注解，用于指定日期序列化格式
import com.fasterxml.jackson.annotation.JsonFormat;
// MapStruct-plus注解：自动映射注解，用于自动生成对象转换代码
import io.github.linpeilie.annotations.AutoMapper;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 请假实体类：目标实体类，用于MapStruct-plus自动映射
import org.dromara.workflow.domain.TestLeave;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 请假视图对象
 * 用于封装请假查询结果，返回给前端展示和Excel导出
 * 实现Serializable接口，支持序列化传输
 * 使用MapStruct-plus自动映射到TestLeave实体类
 * 使用EasyExcel注解支持Excel导出功能
 *
 * @author may
 * @date 2023-07-21
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// EasyExcel注解：忽略未标注@ExcelProperty的字段
@ExcelIgnoreUnannotated
// MapStruct-plus注解：指定目标映射类为TestLeave
@AutoMapper(target = TestLeave.class)
public class TestLeaveVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 请假申请的唯一标识
     * Excel导出列名为"主键"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 申请编号
     * 请假申请的唯一编号，如"LEAVE202311190001"
     * Excel导出列名为"申请编号"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "申请编号")
    private String applyCode;

    /**
     * 请假类型
     * 请假的类型，如"事假"、"病假"、"年假"等
     * Excel导出列名为"请假类型"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "请假类型")
    private String leaveType;

    /**
     * 开始时间
     * 请假的开始日期
     * Excel导出列名为"开始时间"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "开始时间")
    private Date startDate;

    /**
     * 结束时间
     * 请假的结束日期
     * Excel导出列名为"结束时间"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "结束时间")
    private Date endDate;

    /**
     * 请假天数
     * 请假的总天数
     * Excel导出列名为"请假天数"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "请假天数")
    private Integer leaveDays;

    /**
     * 请假原因
     * 请假的详细原因说明
     * Excel导出列名为"请假原因"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "请假原因")
    private String remark;

    /**
     * 状态
     * 请假申请的审批状态
     * Excel导出列名为"状态"
     */
    // EasyExcel注解：定义Excel列，value指定列名
    @ExcelProperty(value = "状态")
    private String status;

}
