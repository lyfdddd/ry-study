// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain 表示工作流模块领域模型层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain;

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
// MyBatis-Plus基础实体类：提供创建人、创建时间、更新人、更新时间等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;

/**
 * 流程SpEL表达式定义实体类
 * 对应数据库表：flow_spel（流程SpEL表达式定义表）
 * 继承BaseEntity，获得创建人、创建时间、更新人、更新时间等公共字段
 * 用于存储流程中使用的SpEL表达式定义，支持动态方法调用和参数传递
 * SpEL（Spring Expression Language）是Spring框架的表达式语言，用于在运行时查询和操作对象
 *
 * @author Michelle.Chung
 * @date 2025-07-04
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定数据库表名为flow_spel
@TableName("flow_spel")
public class FlowSpel extends BaseEntity {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 唯一标识一条SpEL表达式定义记录
     * 对应数据库表id列
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名
    @TableId(value = "id")
    private Long id;

    /**
     * 组件名称
     * Spring Bean的名称，用于定位要调用的组件
     * 如"userService"、"deptService"等
     * 对应数据库表component_name列
     */
    private String componentName;

    /**
     * 方法名
     * 要调用的方法名称
     * 如"getUserById"、"findDeptList"等
     * 对应数据库表method_name列
     */
    private String methodName;

    /**
     * 方法参数
     * 方法调用时传递的参数，支持SpEL表达式
     * 如"#{userId}"、"#{deptId}"等
     * 对应数据库表method_params列
     */
    private String methodParams;

    /**
     * 预览SpEL表达式
     * 完整的SpEL表达式预览，用于调试和验证
     * 如"@userService.getUserById(#{userId})"
     * 对应数据库表view_spel列
     */
    private String viewSpel;

    /**
     * 状态（0正常 1停用）
     * 表达式的启用状态，0表示正常可用，1表示已停用
     * 用于控制表达式是否生效
     * 对应数据库表status列
     */
    private String status;

    /**
     * 备注
     * 表达式的说明和备注信息
     * 用于记录表达式的用途和注意事项
     * 对应数据库表remark列
     */
    private String remark;

    /**
     * 删除标志（0代表存在 1代表删除）
     * 逻辑删除字段，0表示正常，1表示已删除
     * 实现软删除功能，保留历史数据
     * 对应数据库表del_flag列
     */
    // MyBatis-Plus注解：标记为逻辑删除字段
    @TableLogic
    private String delFlag;

    // 继承自BaseEntity的创建人、创建时间、更新人、更新时间字段

}
