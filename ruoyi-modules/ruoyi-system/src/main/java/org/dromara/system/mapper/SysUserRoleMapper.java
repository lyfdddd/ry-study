// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
// LambdaQueryWrapper是MyBatis-Plus提供的Lambda表达式查询包装器，使用实体属性引用而非字符串，避免硬编码字段名，编译期检查类型安全
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：用户与角色关联实体
// SysUserRole是系统用户与角色关联实体类，对应数据库的sys_user_role表，存储用户和角色的关联关系
import org.dromara.system.domain.SysUserRole;

// Java列表接口
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;

/**
 * 用户与角色关联表 数据层
 * 继承BaseMapperPlus，提供用户与角色关联表的CRUD操作
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责用户和角色关联关系的增删改查操作，用于权限控制
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysUserRole表示实体类型，第二个SysUserRole表示视图对象类型（与实体相同）
// BaseMapperPlus提供基础的CRUD方法，简化数据访问层开发
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRole, SysUserRole> {

    /**
     * 根据角色ID查询关联的用户ID列表
     * 使用LambdaQueryWrapper构建查询条件，查询指定角色的用户ID
     * 使用Lambda表达式避免硬编码字段名，提升代码可维护性
     * 返回用户ID列表，用于查询角色下的所有用户
     *
     * @param roleId 角色ID
     * @return 关联到指定角色的用户ID列表
     */
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用select方法指定查询用户ID字段，eq方法添加角色ID等于条件
    default List<Long> selectUserIdsByRoleId(Long roleId) {
        // 创建LambdaQueryWrapper，指定查询用户ID字段，添加角色ID等于条件
        // 调用selectObjs方法返回对象列表，自动转换为Long类型
        return this.selectObjs(new LambdaQueryWrapper<SysUserRole>()
            .select(SysUserRole::getUserId).eq(SysUserRole::getRoleId, roleId)
        );
    }

}
