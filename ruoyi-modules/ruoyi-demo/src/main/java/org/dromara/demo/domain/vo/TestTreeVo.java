// 测试树表视图对象层
// 返回给前端的测试树表数据格式，支持Excel导出功能
package org.dromara.demo.domain.vo;

// EasyExcel注解，忽略未标注@ExcelProperty的字段
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
// EasyExcel注解，标记Excel列属性
import cn.idev.excel.annotation.ExcelProperty;
// 测试树表实体类，用于MapStruct转换
import org.dromara.demo.domain.TestTree;
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
 * 测试树表视图对象 test_tree
 * 返回给前端的测试树表数据格式，支持Excel导出功能
 * 实现Serializable接口支持序列化
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// EasyExcel注解，忽略未标注@ExcelProperty的字段
@ExcelIgnoreUnannotated
// MapStruct-plus自动映射注解，target指定目标类为TestTree
@AutoMapper(target = TestTree.class)
// 测试树表视图对象类，实现Serializable接口
public class TestTreeVo implements Serializable {

    // 序列化版本号，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * Excel导出时显示为"主键"列
     */
    // 主键ID
    private Long id;

    /**
     * 父节点ID
     * Excel导出时显示为"父id"列
     * 用于构建树形结构，顶级节点的parentId为0
     */
    // EasyExcel列属性
    @ExcelProperty(value = "父id")
    // 父节点ID
    private Long parentId;

    /**
     * 部门ID
     * Excel导出时显示为"部门id"列
     * 关联sys_dept表，用于数据权限控制
     */
    // EasyExcel列属性
    @ExcelProperty(value = "部门id")
    // 部门ID
    private Long deptId;

    /**
     * 用户ID
     * Excel导出时显示为"用户id"列
     * 关联sys_user表，用于数据权限控制
     */
    // EasyExcel列属性
    @ExcelProperty(value = "用户id")
    // 用户ID
    private Long userId;

    /**
     * 树节点名称
     * Excel导出时显示为"树节点名"列
     * 树形结构显示的节点名称
     */
    // EasyExcel列属性
    @ExcelProperty(value = "树节点名")
    // 树节点名称
    private String treeName;

    /**
     * 创建时间
     * Excel导出时显示为"创建时间"列
     */
    // EasyExcel列属性
    @ExcelProperty(value = "创建时间")
    // 创建时间
    private Date createTime;


}
