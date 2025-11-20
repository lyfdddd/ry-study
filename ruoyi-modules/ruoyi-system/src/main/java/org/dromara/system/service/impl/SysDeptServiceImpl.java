// 部门管理服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：Bean拷贝工具，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：树结构工具，用于构建树形结构
import cn.hutool.core.lang.tree.Tree;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：Lambda更新包装器，支持类型安全更新
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心常量：系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 公共核心领域模型：部门DTO，用于跨服务数据传输
import org.dromara.common.core.domain.dto.DeptDTO;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心服务接口：通用部门服务接口
import org.dromara.common.core.service.DeptService;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共核心工具类：树构建工具
import org.dromara.common.core.utils.TreeBuildUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// MyBatis-Plus数据库助手：提供数据库相关辅助方法
import org.dromara.common.mybatis.helper.DataBaseHelper;
// Redis缓存工具类：提供缓存操作工具
import org.dromara.common.redis.utils.CacheUtils;
// Sa-Token工具类：登录助手，提供获取当前登录用户信息的方法
import org.dromara.common.satoken.utils.LoginHelper;
// 系统领域模型：部门实体类
import org.dromara.system.domain.SysDept;
// 系统领域模型：角色实体类
import org.dromara.system.domain.SysRole;
// 系统领域模型：用户实体类
import org.dromara.system.domain.SysUser;
// 系统业务对象：部门业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysDeptBo;
// 系统视图对象：部门视图对象，用于返回前端数据
import org.dromara.system.domain.vo.SysDeptVo;
// 系统Mapper接口：部门Mapper
import org.dromara.system.mapper.SysDeptMapper;
// 系统Mapper接口：角色Mapper
import org.dromara.system.mapper.SysRoleMapper;
// 系统Mapper接口：用户Mapper
import org.dromara.system.mapper.SysUserMapper;
// 系统服务接口：部门服务接口
import org.dromara.system.service.ISysDeptService;
// Spring缓存注解：缓存清除，用于数据变更时清除缓存
import org.springframework.cache.annotation.CacheEvict;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring缓存注解：组合缓存操作，支持多个缓存操作
import org.springframework.cache.annotation.Caching;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;
// 公共核心工具类：对象工具类，提供对象操作辅助方法
import org.dromara.common.core.utils.ObjectUtils;
// 公共核心工具类：Spring工具类，提供Spring上下文相关操作
import org.dromara.common.core.utils.SpringUtils;

// Java集合工具类：提供集合操作
import java.util.*;

/**
 * 部门管理服务实现类
 * 核心业务：部门管理、树形结构构建、数据权限校验、用户角色关联查询
 * 实现接口：ISysDeptService（系统部门服务）、DeptService（通用部门服务）
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class SysDeptServiceImpl implements ISysDeptService, DeptService {

    // 部门Mapper，继承BaseMapperPlus，提供部门表CRUD操作
    private final SysDeptMapper baseMapper;
    // 角色Mapper，用于查询角色信息
    private final SysRoleMapper roleMapper;
    // 用户Mapper，用于查询用户信息
    private final SysUserMapper userMapper;

    /**
     * 分页查询部门管理数据
     * 根据查询条件分页查询部门列表，返回分页结果
     *
     * @param dept      部门查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 部门信息分页结果
     */
    @Override
    public TableDataInfo<SysDeptVo> selectPageDeptList(SysDeptBo dept, PageQuery pageQuery) {
        // 调用Mapper执行分页查询，buildQueryWrapper构建查询条件
        Page<SysDeptVo> page = baseMapper.selectPageDeptList(pageQuery.build(), buildQueryWrapper(dept));
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 查询部门管理数据
     * 根据查询条件查询部门列表，不分页
     *
     * @param dept 部门查询条件
     * @return 部门信息集合
     */
    @Override
    public List<SysDeptVo> selectDeptList(SysDeptBo dept) {
        // 构建查询条件
        LambdaQueryWrapper<SysDept> lqw = buildQueryWrapper(dept);
        // 调用Mapper查询部门列表
        return baseMapper.selectDeptList(lqw);
    }

    /**
     * 查询部门树结构信息
     * 将部门列表转换为树形结构，用于前端下拉树组件
     *
     * @param bo 部门查询条件
     * @return 部门树信息集合
     */
    @Override
    public List<Tree<Long>> selectDeptTreeList(SysDeptBo bo) {
        // 构建查询条件
        LambdaQueryWrapper<SysDept> lqw = buildQueryWrapper(bo);
        // 查询部门列表
        List<SysDeptVo> depts = baseMapper.selectDeptList(lqw);
        // 将列表转换为树形结构
        return buildDeptTreeSelect(depts);
    }

    /**
     * 构建部门查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo 部门查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<SysDept> buildQueryWrapper(SysDeptBo bo) {
        // 获取查询参数Map，包含beginTime、endTime等
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查
        LambdaQueryWrapper<SysDept> lqw = Wrappers.lambdaQuery();
        // 删除标志为正常（未删除）
        lqw.eq(SysDept::getDelFlag, SystemConstants.NORMAL);
        // 精确查询部门ID
        lqw.eq(ObjectUtil.isNotNull(bo.getDeptId()), SysDept::getDeptId, bo.getDeptId());
        // 精确查询父部门ID
        lqw.eq(ObjectUtil.isNotNull(bo.getParentId()), SysDept::getParentId, bo.getParentId());
        // 模糊查询部门名称
        lqw.like(StringUtils.isNotBlank(bo.getDeptName()), SysDept::getDeptName, bo.getDeptName());
        // 模糊查询部门分类
        lqw.like(StringUtils.isNotBlank(bo.getDeptCategory()), SysDept::getDeptCategory, bo.getDeptCategory());
        // 精确查询状态
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysDept::getStatus, bo.getStatus());
        // 时间范围查询创建时间
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            SysDept::getCreateTime, params.get("beginTime"), params.get("endTime"));
        // 按祖级列表升序排序（保证父部门在前）
        lqw.orderByAsc(SysDept::getAncestors);
        // 按父部门ID升序排序
        lqw.orderByAsc(SysDept::getParentId);
        // 按排序号升序排序
        lqw.orderByAsc(SysDept::getOrderNum);
        // 按部门ID升序排序
        lqw.orderByAsc(SysDept::getDeptId);
        // 如果指定了所属部门ID，则查询该部门及其所有子部门
        if (ObjectUtil.isNotNull(bo.getBelongDeptId())) {
            //部门树搜索
            lqw.and(x -> {
                // 查询指定部门及其所有子部门ID
                List<Long> deptIds = baseMapper.selectDeptAndChildById(bo.getBelongDeptId());
                // 使用in条件查询这些部门
                x.in(SysDept::getDeptId, deptIds);
            });
        }
        return lqw;
    }

    /**
     * 构建前端所需要下拉树结构
     * 将部门列表转换为树形结构，用于前端下拉树组件
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildDeptTreeSelect(List<SysDeptVo> depts) {
        // 如果部门列表为空，返回空列表
        if (CollUtil.isEmpty(depts)) {
            return CollUtil.newArrayList();
        }
        // 使用TreeBuildUtils工具类构建多根节点树
        return TreeBuildUtils.buildMultiRoot(
            depts, // 部门列表
            SysDeptVo::getDeptId, // ID获取函数
            SysDeptVo::getParentId, // 父ID获取函数
            (node, treeNode) -> treeNode // 节点转换函数
                .setId(node.getDeptId()) // 设置节点ID
                .setParentId(node.getParentId()) // 设置父节点ID
                .setName(node.getDeptName()) // 设置节点名称
                .setWeight(node.getOrderNum()) // 设置排序权重
                .putExtra("disabled", SystemConstants.DISABLE.equals(node.getStatus())) // 添加额外属性：是否禁用（状态为停用）
        );
    }

    /**
     * 根据角色ID查询部门树信息
     * 查询角色已分配的部门ID列表，支持父子联动/不联动模式
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        // 查询角色信息
        SysRole role = roleMapper.selectById(roleId);
        // 调用Mapper查询角色已分配的部门ID列表
        return baseMapper.selectDeptListByRoleId(roleId, role.getDeptCheckStrictly());
    }

    /**
     * 根据部门ID查询信息
     * 查询部门详情，包含父部门名称，使用Redis缓存提升性能
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    // Spring缓存注解：查询时缓存结果，key为部门ID
    @Cacheable(cacheNames = CacheNames.SYS_DEPT, key = "#deptId")
    @Override
    public SysDeptVo selectDeptById(Long deptId) {
        // 查询部门VO对象
        SysDeptVo dept = baseMapper.selectVoById(deptId);
        // 如果部门不存在，返回null
        if (ObjectUtil.isNull(dept)) {
            return null;
        }
        // 查询父部门名称
        SysDeptVo parentDept = baseMapper.selectVoOne(new LambdaQueryWrapper<SysDept>()
            .select(SysDept::getDeptName).eq(SysDept::getDeptId, dept.getParentId()));
        // 设置父部门名称（如果父部门存在）
        dept.setParentName(ObjectUtils.notNullGetter(parentDept, SysDeptVo::getDeptName));
        return dept;
    }

    /**
     * 根据部门ID列表批量查询部门信息
     * 只查询部门ID、名称、负责人等必要字段
     *
     * @param deptIds 部门ID列表
     * @return 部门信息列表
     */
    @Override
    public List<SysDeptVo> selectDeptByIds(List<Long> deptIds) {
        // 使用LambdaQueryWrapper构建查询条件
        return baseMapper.selectDeptList(new LambdaQueryWrapper<SysDept>()
            // 只查询需要的字段：部门ID、部门名称、负责人
            .select(SysDept::getDeptId, SysDept::getDeptName, SysDept::getLeader)
            // 状态为正常（启用）
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            // 在指定部门ID列表中
            .in(CollUtil.isNotEmpty(deptIds), SysDept::getDeptId, deptIds));
    }

    /**
     * 通过部门ID查询部门名称
     * 将逗号分隔的部门ID字符串转换为逗号分隔的部门名称字符串
     *
     * @param deptIds 部门ID串逗号分隔
     * @return 部门名称串逗号分隔
     */
    @Override
    public String selectDeptNameByIds(String deptIds) {
        // 创建部门名称列表
        List<String> list = new ArrayList<>();
        // 将逗号分隔的部门ID字符串转换为Long列表并遍历
        for (Long id : StringUtils.splitTo(deptIds, Convert::toLong)) {
            // 使用AOP代理调用selectDeptById，确保缓存注解生效
            SysDeptVo vo = SpringUtils.getAopProxy(this).selectDeptById(id);
            // 如果部门存在，添加部门名称到列表
            if (ObjectUtil.isNotNull(vo)) {
                list.add(vo.getDeptName());
            }
        }
        // 将部门名称列表转换为逗号分隔字符串
        return StringUtils.joinComma(list);
    }

    /**
     * 根据部门ID查询部门负责人
     * 查询指定部门的负责人ID
     *
     * @param deptId 部门ID，用于指定需要查询的部门
     * @return 返回该部门的负责人ID
     */
    @Override
    public Long selectDeptLeaderById(Long deptId) {
        // 使用AOP代理调用selectDeptById，确保缓存注解生效
        SysDeptVo vo = SpringUtils.getAopProxy(this).selectDeptById(deptId);
        // 返回部门负责人ID
        return vo.getLeader();
    }

    /**
     * 查询所有正常状态的部门列表
     * 用于其他模块获取部门基础数据
     *
     * @return 部门DTO列表
     */
    @Override
    public List<DeptDTO> selectDeptsByList() {
        // 查询所有正常状态的部门，只查询必要字段
        List<SysDeptVo> list = baseMapper.selectDeptList(new LambdaQueryWrapper<SysDept>()
            .select(SysDept::getDeptId, SysDept::getDeptName, SysDept::getParentId)
            .eq(SysDept::getStatus, SystemConstants.NORMAL));
        // 使用Hutool的BeanUtil将SysDeptVo列表转换为DeptDTO列表
        return BeanUtil.copyToList(list, DeptDTO.class);
    }

    /**
     * 根据ID查询所有子部门数（正常状态）
     * 查询指定部门下所有正常状态的子部门数量
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public long selectNormalChildrenDeptById(Long deptId) {
        // 使用LambdaQueryWrapper构建查询条件
        return baseMapper.selectCount(new LambdaQueryWrapper<SysDept>()
            // 状态为正常（启用）
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            // 使用FIND_IN_SET函数查询ancestors字段包含deptId的记录
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
    }

    /**
     * 判断部门是否存在子节点
     * 查询指定部门是否有子部门
     *
     * @param deptId 部门ID
     * @return 结果 true存在 false不存在
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        // 使用exists方法判断是否存在子部门
        return baseMapper.exists(new LambdaQueryWrapper<SysDept>()
            // 父部门ID等于指定部门ID
            .eq(SysDept::getParentId, deptId));
    }

    /**
     * 查询部门是否存在用户
     * 判断指定部门下是否有用户，用于删除前的校验
     *
     * @param deptId 部门ID
     * @return 结果 true存在 false不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId) {
        // 使用exists方法判断部门下是否存在用户
        return userMapper.exists(new LambdaQueryWrapper<SysUser>()
            // 用户部门ID等于指定部门ID
            .eq(SysUser::getDeptId, deptId));
    }

    /**
     * 校验部门名称是否唯一
     * 检查同一父部门下部门名称是否重复
     *
     * @param dept 部门信息
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkDeptNameUnique(SysDeptBo dept) {
        // 查询是否存在同名部门（同一父部门下）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysDept>()
            // 部门名称相同
            .eq(SysDept::getDeptName, dept.getDeptName())
            // 父部门ID相同
            .eq(SysDept::getParentId, dept.getParentId())
            // 排除当前部门ID（编辑时）
            .ne(ObjectUtil.isNotNull(dept.getDeptId()), SysDept::getDeptId, dept.getDeptId()));
        // 返回是否唯一（不存在重复）
        return !exist;
    }

    /**
     * 校验部门是否有数据权限
     * 检查当前用户是否有权限访问指定部门数据
     *
     * @param deptId 部门id
     */
    @Override
    public void checkDeptDataScope(Long deptId) {
        // 如果部门ID为空，直接返回
        if (ObjectUtil.isNull(deptId)) {
            return;
        }
        // 如果是超级管理员，直接返回（超级管理员拥有所有权限）
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        // 查询当前用户是否有权限访问该部门
        if (baseMapper.countDeptById(deptId) == 0) {
            // 如果没有权限，抛出业务异常
            throw new ServiceException("没有权限访问部门数据！");
        }
    }

    /**
     * 新增保存部门信息
     * 新增部门，自动计算祖级列表，校验父部门状态
     *
     * @param bo 部门信息
     * @return 结果
     */
    // Spring缓存注解：删除部门及子部门缓存（全部条目）
    @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    @Override
    public int insertDept(SysDeptBo bo) {
        // 查询父部门信息
        SysDept info = baseMapper.selectById(bo.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!SystemConstants.NORMAL.equals(info.getStatus())) {
            // 抛出业务异常
            throw new ServiceException("部门停用，不允许新增");
        }
        // 将BO转换为实体对象
        SysDept dept = MapstructUtils.convert(bo, SysDept.class);
        // 设置祖级列表：父部门祖级列表 + 分隔符 + 父部门ID
        dept.setAncestors(info.getAncestors() + StringUtils.SEPARATOR + dept.getParentId());
        // 插入部门数据
        return baseMapper.insert(dept);
    }

    /**
     * 修改保存部门信息
     * 修改部门信息，支持父部门变更，自动更新子部门祖级列表
     *
     * @param bo 部门信息
     * @return 结果
     */
    // Spring缓存注解：组合缓存失效，删除当前部门缓存和部门及子部门缓存
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT, key = "#bo.deptId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    })
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int updateDept(SysDeptBo bo) {
        // 将BO转换为实体对象
        SysDept dept = MapstructUtils.convert(bo, SysDept.class);
        // 查询旧部门信息
        SysDept oldDept = baseMapper.selectById(dept.getDeptId());
        // 如果部门不存在，抛出业务异常
        if (ObjectUtil.isNull(oldDept)) {
            throw new ServiceException("部门不存在，无法修改");
        }
        // 如果父部门ID发生变化
        if (!oldDept.getParentId().equals(dept.getParentId())) {
            // 如果是新父部门 则校验是否具有新父部门权限 避免越权
            this.checkDeptDataScope(dept.getParentId());
            // 查询新父部门信息
            SysDept newParentDept = baseMapper.selectById(dept.getParentId());
            // 如果新父部门存在
            if (ObjectUtil.isNotNull(newParentDept)) {
                // 计算新的祖级列表：新父部门祖级列表 + 分隔符 + 新父部门ID
                String newAncestors = newParentDept.getAncestors() + StringUtils.SEPARATOR + newParentDept.getDeptId();
                // 获取旧的祖级列表
                String oldAncestors = oldDept.getAncestors();
                // 设置新的祖级列表
                dept.setAncestors(newAncestors);
                // 更新所有子部门的祖级列表
                updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
            }
        } else {
            // 父部门未变化，保持原祖级列表
            dept.setAncestors(oldDept.getAncestors());
        }
        // 更新部门信息
        int result = baseMapper.updateById(dept);
        // 如果部门状态为启用，且部门祖级列表不为空，且部门祖级列表不等于根部门祖级列表（如果部门祖级列表不等于根部门祖级列表，则说明存在上级部门）
        if (SystemConstants.NORMAL.equals(dept.getStatus())
            && StringUtils.isNotEmpty(dept.getAncestors())
            && !StringUtils.equals(SystemConstants.ROOT_DEPT_ANCESTORS, dept.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     * 将当前部门的所有上级部门状态设置为正常（启用）
     *
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDept dept) {
        // 获取祖级列表字符串
        String ancestors = dept.getAncestors();
        // 将祖级列表字符串转换为Long数组
        Long[] deptIds = Convert.toLongArray(ancestors);
        // 批量更新所有上级部门状态为正常
        baseMapper.update(null, new LambdaUpdateWrapper<SysDept>()
            // 设置状态为正常
            .set(SysDept::getStatus, SystemConstants.NORMAL)
            // 部门ID在祖级列表中
            .in(SysDept::getDeptId, Arrays.asList(deptIds)));
    }

    /**
     * 修改子元素关系
     * 当部门父节点变更时，更新所有子部门的祖级列表
     *
     * @param deptId       被修改的部门ID
     * @param newAncestors 新的祖级列表
     * @param oldAncestors 旧的祖级列表
     */
    private void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        // 查询所有子部门（ancestors字段包含deptId）
        List<SysDept> children = baseMapper.selectList(new LambdaQueryWrapper<SysDept>()
            // 使用FIND_IN_SET函数查询ancestors字段包含deptId的记录
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
        // 创建需要更新的部门列表
        List<SysDept> list = new ArrayList<>();
        // 遍历所有子部门
        for (SysDept child : children) {
            // 创建新的部门对象
            SysDept dept = new SysDept();
            // 设置部门ID
            dept.setDeptId(child.getDeptId());
            // 更新祖级列表：将旧的祖级列表替换为新的祖级列表
            dept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            // 添加到更新列表
            list.add(dept);
        }
        // 如果需要更新的列表不为空
        if (CollUtil.isNotEmpty(list)) {
            // 批量更新子部门
            if (baseMapper.updateBatchById(list)) {
                // 更新成功后，清除所有子部门的缓存
                list.forEach(dept -> CacheUtils.evict(CacheNames.SYS_DEPT, dept.getDeptId()));
            }
        }
    }

    /**
     * 删除部门管理信息
     * 删除指定部门，并清除相关缓存
     *
     * @param deptId 部门ID
     * @return 结果
     */
    // Spring缓存注解：组合缓存失效，删除当前部门缓存和部门及子部门缓存
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT, key = "#deptId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, key = "#deptId")
    })
    @Override
    public int deleteDeptById(Long deptId) {
        // 根据部门ID删除部门
        return baseMapper.deleteById(deptId);
    }

    /**
     * 根据部门ID列表查询部门名称映射关系
     * 批量查询部门ID和名称的映射关系，用于数据展示
     *
     * @param deptIds 部门ID列表
     * @return Map，其中key为部门ID，value为对应的部门名称
     */
    @Override
    public Map<Long, String> selectDeptNamesByIds(List<Long> deptIds) {
        // 如果部门ID列表为空，返回空Map
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyMap();
        }
        // 查询部门列表，只查询部门ID和名称
        List<SysDept> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysDept>()
                // 只查询部门ID和名称
                .select(SysDept::getDeptId, SysDept::getDeptName)
                // 在指定部门ID列表中
                .in(SysDept::getDeptId, deptIds)
        );
        // 使用StreamUtils将列表转换为Map，key为部门ID，value为部门名称
        return StreamUtils.toMap(list, SysDept::getDeptId, SysDept::getDeptName);
    }

}
