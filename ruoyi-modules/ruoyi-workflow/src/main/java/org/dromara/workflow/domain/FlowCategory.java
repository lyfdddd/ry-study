// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain 表示工作流模块领域模型层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain;

// MyBatis-Plus注解：表字段注解，用于标记非数据库字段
import com.baomidou.mybatisplus.annotation.TableField;
// MyBatis-Plus注解：表ID注解，标记主键字段
import com.baomidou.mybatisplus.annotation.TableId;
// MyBatis-Plus注解：逻辑删除注解，标记逻辑删除字段
import com.baomidou.mybatisplus.annotation.TableLogic;
// MyBatis-Plus注解：表名注解，指定对应的数据库表
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
import lombok.EqualsAndHashCode;
// 租户实体基类：提供租户相关字段（tenantId）
import org.dromara.common.tenant.core.TenantEntity;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java集合类：ArrayList实现类
import java.util.ArrayList;
// Java集合接口：List接口
import java.util.List;

/**
 * 流程分类实体类
 * 对应数据库表：flow_category（流程分类表）
 * 继承TenantEntity，获得租户ID字段
 * 用于存储流程分类信息，支持树形结构
 *
 * @author may
 * @date 2023-06-27
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定数据库表名为flow_category
@TableName("flow_category")
public class FlowCategory extends TenantEntity {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流程分类ID
     * 主键字段，对应数据库表category_id列
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名
    @TableId(value = "category_id")
    private Long categoryId;

    /**
     * 父流程分类ID
     * 用于构建树形结构，指向父分类的ID
     * 对应数据库表parent_id列
     */
    private Long parentId;

    /**
     * 祖级列表
     * 存储从根节点到当前节点的所有祖先ID路径
     * 格式：0,1,2（使用逗号分隔）
     * 对应数据库表ancestors列
     */
    private String ancestors;

    /**
     * 流程分类名称
     * 分类的显示名称，如"请假流程"、"报销流程"
     * 对应数据库表category_name列
     */
    private String categoryName;

    /**
     * 显示顺序
     * 用于分类的排序，数值越小越靠前
     * 对应数据库表order_num列
     */
    private Long orderNum;

    /**
     * 删除标志（0代表存在 1代表删除）
     * 逻辑删除字段，0表示正常，1表示已删除
     * 对应数据库表del_flag列
     */
    // MyBatis-Plus注解：标记为逻辑删除字段
    @TableLogic
    private String delFlag;

    /**
     * 子菜单列表
     * 非数据库字段，用于存储子分类列表
     * 在查询时动态填充，构建树形结构
     */
    // MyBatis-Plus注解：标记为非数据库字段，exist=false表示不对应数据库列
    @TableField(exist = false)
    // 初始化空列表，避免空指针异常
    private List<FlowCategory> children = new ArrayList<>();

}
