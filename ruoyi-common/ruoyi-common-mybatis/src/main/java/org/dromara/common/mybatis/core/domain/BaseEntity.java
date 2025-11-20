// 定义实体类的基础包路径，所有实体类都在此包下
package org.dromara.common.mybatis.core.domain;

// MyBatis-Plus字段填充策略注解，用于自动填充创建时间、更新时间等
import com.baomidou.mybatisplus.annotation.FieldFill;
// MyBatis-Plus表字段注解，标记字段是否存在于数据库表中
import com.baomidou.mybatisplus.annotation.TableField;
// Jackson注解，标记字段在序列化时忽略（不返回给前端）
import com.fasterxml.jackson.annotation.JsonIgnore;
// Jackson注解，控制字段序列化时的包含策略
import com.fasterxml.jackson.annotation.JsonInclude;
// Lombok注解，自动生成getter、setter、toString等方法
import lombok.Data;

// 序列化相关接口和类
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity基类
 * 所有数据库实体类的父类，提供公共字段（创建人、创建时间、更新人、更新时间等）
 * 使用MyBatis-Plus的字段自动填充功能，减少重复代码
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
// 减少样板代码，提升开发效率
@Data
// 实现Serializable接口，支持对象序列化（用于缓存、分布式传输等场景）
public class BaseEntity implements Serializable {

    // 序列化版本UID，用于反序列化时验证版本一致性
    // 如果类结构发生变化（增删字段），需要修改此值
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 搜索值
     * 前端传递的模糊搜索关键字，不持久化到数据库
     */
    // Jackson注解：序列化时忽略此字段，不返回给前端
    @JsonIgnore
    // MyBatis-Plus注解：标记此字段在数据库表中不存在，仅为查询参数
    @TableField(exist = false)
    private String searchValue;

    /**
     * 创建部门
     * 记录创建该数据的部门ID，用于数据权限隔离
     * 使用MyBatis-Plus自动填充，插入时自动设置当前用户部门
     */
    // MyBatis-Plus注解：插入时自动填充（通过InjectionMetaObjectHandler实现）
    @TableField(fill = FieldFill.INSERT)
    private Long createDept;

    /**
     * 创建者
     * 记录创建该数据的用户ID，用于审计追踪
     * 使用MyBatis-Plus自动填充，插入时自动设置当前用户ID
     */
    // MyBatis-Plus注解：插入时自动填充
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间
     * 记录数据创建时间，用于审计追踪
     * 使用MyBatis-Plus自动填充，插入时自动设置当前时间
     */
    // MyBatis-Plus注解：插入时自动填充
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新者
     * 记录最后更新该数据的用户ID，用于审计追踪
     * 使用MyBatis-Plus自动填充，插入和更新时自动设置当前用户ID
     */
    // MyBatis-Plus注解：插入和更新时自动填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     * 记录数据最后更新时间，用于审计追踪
     * 使用MyBatis-Plus自动填充，插入和更新时自动设置当前时间
     */
    // MyBatis-Plus注解：插入和更新时自动填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 请求参数
     * 存储前端传递的额外查询参数，如日期范围、自定义条件等
     * 不持久化到数据库，仅用于查询场景
     */
    // Jackson注解：当字段为空（null或空集合）时不序列化，减少传输数据量
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    // MyBatis-Plus注解：标记此字段在数据库表中不存在
    @TableField(exist = false)
    // 使用HashMap存储请求参数，支持动态扩展
    private Map<String, Object> params = new HashMap<>();

}
