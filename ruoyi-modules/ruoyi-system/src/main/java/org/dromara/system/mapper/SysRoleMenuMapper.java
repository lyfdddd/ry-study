// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// MyBatis-Plus核心组件：Lambda更新包装器，支持类型安全更新
// LambdaUpdateWrapper是MyBatis-Plus提供的Lambda表达式更新包装器，使用实体属性引用而非字符串，避免硬编码字段名，编译期检查类型安全
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：角色与菜单关联实体
// SysRoleMenu是系统角色与菜单关联实体类，对应数据库的sys_role_menu表，存储角色和菜单的关联关系
import org.dromara.system.domain.SysRoleMenu;

// Java列表接口
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;

/**
 * 角色与菜单关联表 数据层
 * 继承BaseMapperPlus，提供角色与菜单关联表的CRUD操作
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责角色和菜单关联关系的增删改查操作，用于权限控制
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysRoleMenu表示实体类型，第二个SysRoleMenu表示视图对象类型（与实体相同）
// BaseMapperPlus提供基础的CRUD方法，简化数据访问层开发
public interface SysRoleMenuMapper extends BaseMapperPlus<SysRoleMenu, SysRoleMenu> {

    /**
     * 根据菜单ID串删除关联关系
     * 使用LambdaUpdateWrapper构建删除条件，批量删除指定菜单ID的关联记录
     * 使用Lambda表达式避免硬编码字段名，提升代码可维护性
     *
     * @param menuIds 菜单ID串
     * @return 结果
     */
    // 默认方法，使用LambdaUpdateWrapper构建删除条件
    // 使用in方法批量匹配菜单ID
    default int deleteByMenuIds(List<Long> menuIds) {
        // 创建LambdaUpdateWrapper，添加菜单ID在指定列表中的条件
        // 调用delete方法批量删除符合条件的记录
        return this.delete(new LambdaUpdateWrapper<SysRoleMenu>().in(SysRoleMenu::getMenuId, menuIds));
    }

}
