// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// MyBatis-Plus核心组件：查询条件包装器，支持动态SQL构建
// Wrapper是MyBatis-Plus提供的查询条件抽象类，用于构建动态SQL查询条件
import com.baomidou.mybatisplus.core.conditions.Wrapper;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
// LambdaQueryWrapper是MyBatis-Plus提供的Lambda表达式查询包装器，使用实体属性引用而非字符串，避免硬编码字段名，编译期检查类型安全
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus分页插件：分页对象
// Page是MyBatis-Plus提供的分页插件核心类，支持物理分页查询，自动处理分页逻辑
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 公共核心工具类：Stream流操作工具
// StreamUtils是项目封装的Stream工具类，提供集合的流式处理增强方法
import org.dromara.common.core.utils.StreamUtils;
// 公共MyBatis注解：数据列注解，用于数据权限控制
// DataColumn是项目自定义的注解，用于标记数据权限控制的列信息
import org.dromara.common.mybatis.annotation.DataColumn;
// 公共MyBatis注解：数据权限注解，用于方法级别的数据权限控制
// DataPermission是项目自定义的注解，用于标记需要数据权限控制的方法
import org.dromara.common.mybatis.annotation.DataPermission;
// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 公共MyBatis辅助类：数据库辅助类，提供数据库相关工具方法
// DataBaseHelper是项目封装的数据库辅助类，提供findInSet等方法
import org.dromara.common.mybatis.helper.DataBaseHelper;
// 系统领域实体类：部门实体
// SysDept是系统部门实体类，对应数据库的sys_dept表
import org.dromara.system.domain.SysDept;
// 系统视图对象：部门视图对象
// SysDeptVo是部门的视图对象，用于返回给前端的部门信息
import org.dromara.system.domain.vo.SysDeptVo;

// Java列表接口
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;

/**
 * 部门管理 数据层
 * 继承BaseMapperPlus，提供部门表的CRUD操作和VO转换能力
 * 使用@DataPermission注解实现数据权限控制，确保用户只能查看和操作有权限的部门数据
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysDept表示实体类型，SysDeptVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysDeptMapper extends BaseMapperPlus<SysDept, SysDeptVo> {

    /**
     * 构建角色对应的部门 SQL 查询语句
     * 生成SQL子查询语句，用于查询角色关联的部门ID
     * 通常配合inSql方法使用，实现子查询逻辑
     * 使用Java 15+的文本块语法（"""）构建SQL语句，提升可读性
     *
     * <p>该 SQL 用于查询某个角色关联的所有部门 ID，常用于数据权限控制</p>
     *
     * @param roleId 角色ID
     * @return 查询部门ID的 SQL 语句字符串
     */
    // 默认方法，使用formatted方法格式化字符串，将roleId插入SQL中
    default String buildDeptByRoleSql(Long roleId) {
        // 构建子查询SQL：从sys_role_dept表中查询指定角色的部门ID
        // 关联sys_role表，确保角色状态正常（status = '0'）
        return """
                select srd.dept_id from sys_role_dept srd
                    left join sys_role sr on sr.role_id = srd.role_id
                    where srd.role_id = %d and sr.status = '0'
            """.formatted(roleId);
    }

    /**
     * 构建 SQL 查询，用于获取当前角色拥有的部门中所有的父部门ID
     * 生成SQL子查询语句，用于查询部门的所有父部门ID
     * 用于deptCheckStrictly场景，排除非叶子节点（父节点）
     *
     * <p>
     * 该 SQL 用于 deptCheckStrictly 场景下，排除非叶子节点（父节点）用。
     * 通过查询部门表中，部门ID在角色部门关联表中的记录的父部门ID
     * </p>
     *
     * @param roleId 角色ID
     * @return SQL 语句字符串，查询角色下部门的所有父部门ID
     */
    // 默认方法，使用Java 15+的文本块语法构建嵌套子查询
    default String buildParentDeptByRoleSql(Long roleId) {
        // 构建嵌套子查询SQL：先查询角色关联的部门ID，再查询这些部门的父部门ID
        return """
                select parent_id from sys_dept where dept_id in (
                    select srd.dept_id from sys_role_dept srd
                        left join sys_role sr on sr.role_id = srd.role_id
                        where srd.role_id = %d and sr.status = '0'
                )
            """.formatted(roleId);
    }

    /**
     * 查询部门管理数据
     * 使用@DataPermission注解实现部门级的数据权限控制
     * 通过@DataColumn指定数据权限控制的列映射关系
     * 返回List集合，不分页
     *
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据
    // @DataColumn key="deptName"表示部门名称，value="dept_id"表示部门ID字段
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    // 默认方法，调用父类的selectVoList方法，自动将实体转换为VO对象
    default List<SysDeptVo> selectDeptList(Wrapper<SysDept> queryWrapper) {
        // 调用BaseMapperPlus的selectVoList方法，实现列表查询和VO转换
        return this.selectVoList(queryWrapper);
    }

    /**
     * 分页查询部门管理数据
     * 使用@DataPermission注解实现部门级的数据权限控制
     * 通过@DataColumn指定数据权限控制的列映射关系
     * 返回Page分页对象
     *
     * @param page         分页信息
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
    })
    // 默认方法，调用父类的selectVoPage方法，自动将实体转换为VO对象
    default Page<SysDeptVo> selectPageDeptList(Page<SysDept> page, Wrapper<SysDept> queryWrapper) {
        // 调用BaseMapperPlus的selectVoPage方法，实现分页查询和VO转换
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 统计指定部门ID的部门数量
     * 使用数据权限控制，确保只能统计有权限的部门
     *
     * @param deptId 部门ID
     * @return 该部门ID的部门数量
     */
    // 数据权限注解：控制用户只能统计有权限的部门数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用Lambda表达式避免硬编码字段名，提升代码可维护性
    default long countDeptById(Long deptId) {
        // 创建LambdaQueryWrapper，添加部门ID等于条件
        return this.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptId, deptId));
    }

    /**
     * 根据父部门ID查询其所有子部门的列表
     * 使用DataBaseHelper.findInSet方法查询祖先字段中包含父部门ID的记录
     * 只查询部门ID字段，提升查询性能
     *
     * @param parentId 父部门ID
     * @return 包含子部门的列表
     */
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用apply方法应用自定义SQL条件，findInSet用于查询祖先字段
    default List<SysDept> selectListByParentId(Long parentId) {
        // 创建LambdaQueryWrapper，指定查询部门ID字段
        // 使用apply方法添加findInSet条件，查询ancestors字段中包含parentId的记录
        return this.selectList(new LambdaQueryWrapper<SysDept>()
            .select(SysDept::getDeptId)
            .apply(DataBaseHelper.findInSet(parentId, "ancestors")));
    }

    /**
     * 查询某个部门及其所有子部门ID（含自身）
     * 先查询所有子部门ID，再添加自身ID到列表中
     * 使用StreamUtils进行集合转换，提升代码简洁性
     *
     * @param parentId 父部门ID
     * @return 部门ID集合
     */
    // 默认方法，组合查询子部门和自身
    // 使用StreamUtils.toList将部门列表转换为ID列表
    default List<Long> selectDeptAndChildById(Long parentId) {
        // 查询父部门的所有子部门列表
        List<SysDept> deptList = this.selectListByParentId(parentId);
        // 使用StreamUtils将部门列表转换为部门ID列表
        List<Long> deptIds = StreamUtils.toList(deptList, SysDept::getDeptId);
        // 添加父部门ID到列表中
        deptIds.add(parentId);
        // 返回包含自身和所有子部门的ID列表
        return deptIds;
    }

    /**
     * 根据角色ID查询部门树信息
     * 查询角色关联的部门列表，支持deptCheckStrictly模式
     * deptCheckStrictly为true时，排除父部门，只返回叶子节点
     * 按parentId和orderNum排序，保证部门树结构正确
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示（true：只返回叶子节点，false：返回所有节点）
     * @return 选中部门列表
     */
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用inSql方法添加子查询条件，查询角色关联的部门
    default List<Long> selectDeptListByRoleId(Long roleId, boolean deptCheckStrictly) {
        // 创建LambdaQueryWrapper，指定查询部门ID字段
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysDept::getDeptId)
            // 使用inSql添加子查询条件，查询角色关联的部门ID
            .inSql(SysDept::getDeptId, this.buildDeptByRoleSql(roleId))
            // 按父部门ID升序排序
            .orderByAsc(SysDept::getParentId)
            // 按排序号升序排序
            .orderByAsc(SysDept::getOrderNum);
        // 如果deptCheckStrictly为true，排除父部门（只返回叶子节点）
        if (deptCheckStrictly) {
            // 使用notInSql排除父部门ID
            wrapper.notInSql(SysDept::getDeptId, this.buildParentDeptByRoleSql(roleId));
        }
        // 调用selectObjs方法，返回部门ID列表
        return this.selectObjs(wrapper);
    }

}
