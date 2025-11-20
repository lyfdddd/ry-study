// 测试树表实体类
// 对应数据库test_tree表，继承TenantEntity实现多租户支持，用于演示树形结构操作
package org.dromara.demo.domain;

// MyBatis-Plus主键注解
import com.baomidou.mybatisplus.annotation.TableId;
// MyBatis-Plus逻辑删除注解
import com.baomidou.mybatisplus.annotation.TableLogic;
// MyBatis-Plus表名注解
import com.baomidou.mybatisplus.annotation.TableName;
// MyBatis-Plus版本号注解
import com.baomidou.mybatisplus.annotation.Version;
// 多租户实体基类，提供租户ID字段
import org.dromara.common.tenant.core.TenantEntity;
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Lombok注解，自动生成equals和hashCode方法，callSuper=true表示包含父类字段
import lombok.EqualsAndHashCode;

// 序列化版本号注解（JDK 14+）
import java.io.Serial;

/**
 * 测试树表对象 test_tree
 * 对应数据库test_tree表，用于演示树形结构操作、数据权限、多租户等功能
 * 继承TenantEntity实现多租户支持
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解，自动生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus表名注解，指定对应的数据库表名
@TableName("test_tree")
// 测试树表实体类，继承TenantEntity实现多租户支持
public class TestTree extends TenantEntity {

    // 序列化版本号，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用MyBatis-Plus的@TableId注解标识为主键
     */
    // MyBatis-Plus主键注解，value指定数据库字段名
    @TableId(value = "id")
    // 主键ID
    private Long id;

    /**
     * 父节点ID
     * 用于构建树形结构，顶级节点的parentId为0
     */
    // 父节点ID
    private Long parentId;

    /**
     * 部门ID
     * 关联sys_dept表，用于数据权限控制
     */
    // 部门ID
    private Long deptId;

    /**
     * 用户ID
     * 关联sys_user表，用于数据权限控制
     */
    // 用户ID
    private Long userId;

    /**
     * 树节点名称
     * 树形结构显示的节点名称
     */
    // 树节点名称
    private String treeName;

    /**
     * 版本号
     * 使用@Version注解启用乐观锁，防止并发更新冲突
     */
    // MyBatis-Plus乐观锁注解，用于版本控制
    @Version
    // 版本号
    private Long version;

    /**
     * 删除标志
     * 使用@TableLogic注解启用逻辑删除，0-未删除，1-已删除
     */
    // MyBatis-Plus逻辑删除注解，启用逻辑删除功能
    @TableLogic
    // 删除标志
    private Long delFlag;

}
