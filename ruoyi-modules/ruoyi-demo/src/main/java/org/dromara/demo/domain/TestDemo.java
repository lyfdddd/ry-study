// 测试单表实体类
// 对应数据库test_demo表，继承TenantEntity实现多租户支持
package org.dromara.demo.domain;

// MyBatis-Plus注解，用于标识主键、版本号、逻辑删除等
import com.baomidou.mybatisplus.annotation.*;
// 多租户实体基类，提供租户ID字段
import org.dromara.common.tenant.core.TenantEntity;
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Lombok注解，自动生成equals和hashCode方法，callSuper=true表示包含父类字段
import lombok.EqualsAndHashCode;

// 序列化版本号注解（JDK 14+）
import java.io.Serial;

/**
 * 测试单表对象 test_demo
 * 对应数据库test_demo表，用于演示单表CRUD操作、数据权限、多租户等功能
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解，自动生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus表名注解，指定对应的数据库表名
@TableName("test_demo")
// 测试单表实体类，继承TenantEntity实现多租户支持
public class TestDemo extends TenantEntity {

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
     * 排序号
     * 使用@OrderBy注解指定默认排序规则：降序、排序优先级为1
     */
    // MyBatis-Plus排序注解，asc=false表示降序，sort=1表示排序优先级
    @OrderBy(asc = false, sort = 1)
    // 排序号
    private Integer orderNum;

    /**
     * key键
     * 业务键值，可用于字典或配置项
     */
    // key键
    private String testKey;

    /**
     * 值
     * 业务值，与testKey对应
     */
    // 值
    private String value;

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
