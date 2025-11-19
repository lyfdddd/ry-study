// 数据权限服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 系统领域模型：角色部门关联实体类
import org.dromara.system.domain.SysRoleDept;
// 系统Mapper接口：部门Mapper
import org.dromara.system.mapper.SysDeptMapper;
// 系统Mapper接口：角色部门关联Mapper
import org.dromara.system.mapper.SysRoleDeptMapper;
// 数据权限服务接口
import org.dromara.system.service.ISysDataScopeService;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.List;

/**
 * 数据权限服务实现类
 * <p>
 * 核心业务：角色数据权限、部门数据权限的查询与缓存管理
 * 重要约束：此Service内不允许调用标注`数据权限`注解的方法
 * 例如：deptMapper.selectList 此方法标注了`数据权限`注解会出现循环解析的问题
 * 实现接口：ISysDataScopeService（数据权限服务接口）
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
// 指定Bean名称为"sdss"，避免与其他服务冲突
@Service("sdss")
public class SysDataScopeServiceImpl implements ISysDataScopeService {

    // 角色部门关联Mapper，用于查询角色与部门的关联关系
    private final SysRoleDeptMapper roleDeptMapper;
    // 部门Mapper，用于部门数据查询
    private final SysDeptMapper deptMapper;

    /**
     * 获取角色自定义权限
     * 查询指定角色已分配的部门ID列表，使用Redis缓存提升性能
     *
     * @param roleId 角色Id
     * @return 部门Id组（逗号分隔的字符串，如"1,2,3"）
     */
    // Spring缓存注解：查询时缓存结果，key为角色ID，condition条件确保roleId不为null时才缓存
    @Cacheable(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#roleId", condition = "#roleId != null")
    @Override
    public String getRoleCustom(Long roleId) {
        // 如果角色ID为null，返回"-1"表示无权限
        if (ObjectUtil.isNull(roleId)) {
            return "-1";
        }
        // 查询角色部门关联列表，只查询部门ID字段
        List<SysRoleDept> list = roleDeptMapper.selectList(
            new LambdaQueryWrapper<SysRoleDept>()
                .select(SysRoleDept::getDeptId) // 只查询部门ID
                .eq(SysRoleDept::getRoleId, roleId)); // 条件：角色ID等于指定值
        // 如果查询结果不为空
        if (CollUtil.isNotEmpty(list)) {
            // 使用StreamUtils将部门ID列表转换为逗号分隔的字符串
            return StreamUtils.join(list, rd -> Convert.toStr(rd.getDeptId()));
        }
        // 如果查询结果为空，返回"-1"表示无权限
        return "-1";
    }

    /**
     * 获取部门及以下权限
     * 查询指定部门及其所有子部门的ID列表，使用Redis缓存提升性能
     *
     * @param deptId 部门Id
     * @return 部门Id组（逗号分隔的字符串，如"1,2,3"）
     */
    // Spring缓存注解：查询时缓存结果，key为部门ID，condition条件确保deptId不为null时才缓存
    @Cacheable(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, key = "#deptId", condition = "#deptId != null")
    @Override
    public String getDeptAndChild(Long deptId) {
        // 如果部门ID为null，返回"-1"表示无权限
        if (ObjectUtil.isNull(deptId)) {
            return "-1";
        }
        // 查询部门及其所有子部门的ID列表
        List<Long> deptIds = deptMapper.selectDeptAndChildById(deptId);
        // 如果查询结果不为空，使用StreamUtils将部门ID列表转换为逗号分隔的字符串
        // 否则返回"-1"表示无权限
        return CollUtil.isNotEmpty(deptIds) ? StreamUtils.join(deptIds, Convert::toStr) : "-1";
    }

}
