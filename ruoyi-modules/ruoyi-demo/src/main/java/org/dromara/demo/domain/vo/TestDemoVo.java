// 测试单表视图对象层
// 返回给前端的测试单表数据格式，支持Excel导出、数据翻译等功能
package org.dromara.demo.domain.vo;

// EasyExcel注解，忽略未标注@ExcelProperty的字段
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
// EasyExcel注解，标记Excel列属性
import cn.idev.excel.annotation.ExcelProperty;
// EasyExcel日期时间格式注解，指定日期格式
import cn.idev.excel.annotation.format.DateTimeFormat;
// 自定义Excel注解，标记需要特殊处理的列
import org.dromara.common.excel.annotation.ExcelNotation;
// 自定义Excel注解，标记必填列
import org.dromara.common.excel.annotation.ExcelRequired;
// 翻译注解，实现字段级数据翻译
import org.dromara.common.translation.annotation.Translation;
// 翻译常量，定义翻译类型
import org.dromara.common.translation.constant.TransConstant;
// 测试单表实体类，用于MapStruct转换
import org.dromara.demo.domain.TestDemo;
// MapStruct-plus自动映射注解，实现VO与Entity之间的自动转换
import io.github.linpeilie.annotations.AutoMapper;
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

// 序列化版本号注解（JDK 14+）
import java.io.Serial;
// 序列化接口
import java.io.Serializable;
// 日期类
import java.util.Date;


/**
 * 测试单表视图对象 test_demo
 * 返回给前端的测试单表数据格式，支持Excel导出、数据翻译等功能
 * 实现Serializable接口支持序列化
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// EasyExcel注解，忽略未标注@ExcelProperty的字段
@ExcelIgnoreUnannotated
// MapStruct-plus自动映射注解，target指定目标类为TestDemo
@AutoMapper(target = TestDemo.class)
// 测试单表视图对象类，实现Serializable接口
public class TestDemoVo implements Serializable {

    // 序列化版本号，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * Excel导出时显示为"主键"列
     */
    // EasyExcel注解，value指定列标题
    @ExcelProperty(value = "主键")
    // 主键ID
    private Long id;

    /**
     * 部门ID
     * Excel导出时显示为"部门id"列，标记为必填
     */
    // 自定义Excel注解，标记为必填列
    @ExcelRequired
    // EasyExcel列属性
    @ExcelProperty(value = "部门id")
    // 部门ID
    private Long deptId;

    /**
     * 用户ID
     * Excel导出时显示为"用户id"列，索引为5，标记为必填
     */
    // 标记为必填列
    @ExcelRequired
    // EasyExcel列属性，index=5指定列索引
    @ExcelProperty(value = "用户id", index = 5)
    // 用户ID
    private Long userId;

    /**
     * 排序号
     * Excel导出时显示为"排序号"列，标记为必填
     */
    // 标记为必填列
    @ExcelRequired
    // EasyExcel列属性
    @ExcelProperty(value = "排序号")
    // 排序号
    private Integer orderNum;

    /**
     * key键
     * Excel导出时显示为"key键"列，添加备注"测试key"
     */
    // 自定义Excel注解，添加备注信息
    @ExcelNotation(value = "测试key")
    // EasyExcel列属性
    @ExcelProperty(value = "key键")
    // key键
    private String testKey;

    /**
     * 值
     * Excel导出时显示为"值"列，添加备注"测试value"
     */
    // 自定义Excel注解，添加备注信息
    @ExcelNotation(value = "测试value")
    // EasyExcel列属性
    @ExcelProperty(value = "值")
    // 值
    private String value;

    /**
     * 创建时间
     * Excel导出时显示为"创建时间"列，格式为yyyy-MM-dd HH:mm:ss，标记为必填
     */
    // 标记为必填列
    @ExcelRequired
    // EasyExcel日期格式注解，指定格式
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    // EasyExcel列属性
    @ExcelProperty(value = "创建时间")
    // 创建时间
    private Date createTime;

    /**
     * 创建人ID
     * Excel导出时显示为"创建人"列
     */
    // EasyExcel列属性
    @ExcelProperty(value = "创建人")
    // 创建人ID
    private Long createBy;

    /**
     * 创建人账号
     * 通过@Translation注解实现数据翻译，将userId翻译为用户名
     * Excel导出时显示为"创建人账号"列
     */
    // 翻译注解，type指定翻译类型为USER_ID_TO_NAME，mapper指定源字段为createBy
    @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "createBy")
    // EasyExcel列属性
    @ExcelProperty(value = "创建人账号")
    // 创建人账号（翻译后）
    private String createByName;

    /**
     * 更新时间
     * Excel导出时显示为"更新时间"列
     */
    // EasyExcel列属性
    @ExcelProperty(value = "更新时间")
    // 更新时间
    private Date updateTime;

    /**
     * 更新人ID
     * Excel导出时显示为"更新人"列
     */
    // EasyExcel列属性
    @ExcelProperty(value = "更新人")
    // 更新人ID
    private Long updateBy;

    /**
     * 更新人账号
     * 通过@Translation注解实现数据翻译，将userId翻译为用户名
     * Excel导出时显示为"更新人账号"列
     */
    // 翻译注解，type指定翻译类型，mapper指定源字段为updateBy
    @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "updateBy")
    // EasyExcel列属性
    @ExcelProperty(value = "更新人账号")
    // 更新人账号（翻译后）
    private String updateByName;

    /**
     * 版本号
     * 用于乐观锁控制
     */
    // 版本号
    private Long version;

}
