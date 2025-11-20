// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Sa-Token异常类：用于处理未登录异常
import cn.dev33.satoken.exception.NotLoginException;
// Sa-Token核心工具类：提供登录、权限、Session等操作
import cn.dev33.satoken.stp.StpUtil;
// Hutool工具类：Bean转换工具，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：查询条件包装器接口
import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
// 公共核心常量：租户相关常量
import org.dromara.common.core.constant.TenantConstants;
// 公共核心领域模型：登录用户对象，包含用户基本信息和权限信息
import org.dromara.common.core.domain.model.LoginUser;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心服务接口：通用角色服务接口
import org.dromara.common.core.service.RoleService;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Sa-Token工具类：登录助手，提供获取当前登录用户信息的方法
import org.dromara.common.satoken.utils.LoginHelper;
// 系统领域模型：角色实体类
import org.dromara.system.domain.SysRole;
// 系统领域模型：角色部门关联实体类，用于数据权限
import org.dromara.system.domain.SysRoleDept;
// 系统领域模型：角色菜单关联实体类，用于菜单授权
import org.dromara.system.domain.SysRoleMenu;
// 系统领域模型：用户角色关联实体类
import org.dromara.system.domain.SysUserRole;
// 系统业务对象：角色业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysRoleBo;
// 系统视图对象：角色视图对象，用于返回前端数据
import org.dromara.system.domain.vo.SysRoleVo;
// 系统Mapper接口：角色部门关联Mapper
import org.dromara.system.mapper.SysRoleDeptMapper;
// 系统Mapper接口：角色Mapper
import org.dromara.system.mapper.SysRoleMapper;
// 系统Mapper接口：角色菜单关联Mapper
import org.dromara.system.mapper.SysRoleMenuMapper;
// 系统Mapper接口：用户角色关联Mapper
import org.dromara.system.mapper.SysUserRoleMapper;
// 系统服务接口：角色服务接口
import org.dromara.system.service.ISysRoleService;
// Spring缓存注解：缓存清除，用于数据变更时清除缓存
import org.springframework.cache.annotation.CacheEvict;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;

// Java集合工具类：提供集合操作
import java.util.*;

/**
 * 角色 业务层处理
 * 核心业务：角色管理、菜单授权、数据权限、用户关联
 * 实现接口：ISysRoleService（系统角色服务）、RoleService（通用角色服务）
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class SysRoleServiceImpl implements ISysRoleService, RoleService {

    // 角色Mapper，继承BaseMapperPlus，提供角色表CRUD操作
    private final SysRoleMapper baseMapper;
    // 角色菜单关联Mapper，管理角色与菜单的多对多关系
    private final SysRoleMenuMapper roleMenuMapper;
    // 用户角色关联Mapper，管理用户与角色的多对多关系
    private final SysUserRoleMapper userRoleMapper;
    // 角色部门关联Mapper，管理角色与部门的数据权限关系
    private final SysRoleDeptMapper roleDeptMapper;

    /**
     * 分页查询角色列表（带数据权限）
     * 根据查询条件分页查询角色数据，自动注入数据权限过滤条件
     *
     * @param role 查询条件，包含角色名称、状态、权限字符等
     * @param pageQuery 分页参数，页码、每页大小、排序规则
     * @return 角色分页列表，包含总记录数和当前页数据
     */
    @Override
    public TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo role, PageQuery pageQuery) {
        // 构建分页对象和查询条件，调用Mapper查询
        // 数据权限由MyBatis-Plus拦截器自动注入
        Page<SysRoleVo> page = baseMapper.selectPageRoleList(pageQuery.build(), this.buildQueryWrapper(role));
        // 将Page对象转换为TableDataInfo，适配前端表格组件
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件查询角色数据
     * 根据查询条件查询角色列表，不分页
     *
     * @param role 角色信息，包含角色名称、状态、权限字符等查询条件
     * @return 角色数据集合信息
     */
    @Override
    public List<SysRoleVo> selectRoleList(SysRoleBo role) {
        // 调用Mapper查询角色列表，传入查询条件包装器
        return baseMapper.selectRoleList(this.buildQueryWrapper(role));
    }

    /**
     * 构建角色查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     * 使用MyBatis-Plus的LambdaQueryWrapper实现类型安全查询，避免硬编码字段名
     *
     * @param bo 查询条件对象，包含角色名称、状态、权限字符等
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private Wrapper<SysRole> buildQueryWrapper(SysRoleBo bo) {
        // 获取查询参数Map，包含beginTime、endTime等时间范围参数
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查，避免运行时错误
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        // 链式调用构建查询条件：
        // 1. 精确查询角色ID（当角色ID不为空时）
        wrapper.eq(ObjectUtil.isNotNull(bo.getRoleId()), SysRole::getRoleId, bo.getRoleId())
            // 2. 模糊查询角色名称（当角色名称不为空时）
            .like(StringUtils.isNotBlank(bo.getRoleName()), SysRole::getRoleName, bo.getRoleName())
            // 3. 精确查询状态（当状态不为空时）
            .eq(StringUtils.isNotBlank(bo.getStatus()), SysRole::getStatus, bo.getStatus())
            // 4. 模糊查询权限字符（当权限字符不为空时）
            .like(StringUtils.isNotBlank(bo.getRoleKey()), SysRole::getRoleKey, bo.getRoleKey())
            // 5. 时间范围查询创建时间（当开始时间和结束时间都不为空时）
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysRole::getCreateTime, params.get("beginTime"), params.get("endTime"))
            // 6. 按角色排序字段升序排列（数字越小越靠前）
            .orderByAsc(SysRole::getRoleSort)
            // 7. 按创建时间升序排列（创建时间越早越靠前）
            .orderByAsc(SysRole::getCreateTime);
        // 返回构建好的查询包装器
        return wrapper;
    }

    /**
     * 根据用户ID查询角色
     * 根据用户ID查询该用户关联的所有角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRoleVo> selectRolesByUserId(Long userId) {
        // 调用Mapper的自定义查询方法，通过用户ID关联查询角色表
        return baseMapper.selectRolesByUserId(userId);
    }

    /**
     * 根据用户ID查询角色列表(包含被授权状态)
     * 查询所有角色，并标记用户已授权的角色
     * 用于角色授权界面，展示所有角色并标识已选中的角色
     *
     * @param userId 用户ID
     * @return 角色列表，包含授权状态（flag为true表示已授权）
     */
    @Override
    public List<SysRoleVo> selectRolesAuthByUserId(Long userId) {
        // 查询用户已关联的角色列表
        List<SysRoleVo> userRoles = baseMapper.selectRolesByUserId(userId);
        // 查询所有角色列表
        List<SysRoleVo> roles = selectRoleAll();
        // 使用HashSet提高查找效率，将用户角色ID转换为Set
        Set<Long> userRoleIds = StreamUtils.toSet(userRoles, SysRoleVo::getRoleId);
        // 遍历所有角色，如果角色ID在用户角色ID集合中，设置flag为true
        for (SysRoleVo role : roles) {
            if (userRoleIds.contains(role.getRoleId())) {
                // 设置授权状态为true
                role.setFlag(true);
            }
        }
        // 返回包含授权状态的所有角色列表
        return roles;
    }

    /**
     * 根据用户ID查询权限
     * 根据用户ID查询该用户拥有的所有角色权限标识（如：admin、common）
     * 用于Sa-Token权限认证
     *
     * @param userId 用户ID
     * @return 权限列表，Set集合自动去重
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        // 查询用户关联的角色列表
        List<SysRoleVo> perms = baseMapper.selectRolesByUserId(userId);
        // 创建权限集合（Set自动去重）
        Set<String> permsSet = new HashSet<>();
        // 遍历角色列表
        for (SysRoleVo perm : perms) {
            // 如果角色不为空
            if (ObjectUtil.isNotNull(perm)) {
                // 将角色权限字符按逗号分割并添加到权限集合
                permsSet.addAll(StringUtils.splitList(perm.getRoleKey().trim()));
            }
        }
        // 返回权限集合
        return permsSet;
    }

    /**
     * 查询所有角色
     * 查询系统中所有角色列表，不分页
     *
     * @return 角色列表
     */
    @Override
    public List<SysRoleVo> selectRoleAll() {
        // 调用selectRoleList方法，传入空的查询条件（查询所有）
        return this.selectRoleList(new SysRoleBo());
    }

    /**
     * 根据用户ID获取角色选择框列表
     * 根据用户ID查询该用户关联的角色ID列表
     * 用于角色授权界面，展示已选中的角色ID
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        // 查询用户关联的角色列表
        List<SysRoleVo> list = baseMapper.selectRolesByUserId(userId);
        // 使用StreamUtils提取角色ID列表
        return StreamUtils.toList(list, SysRoleVo::getRoleId);
    }

    /**
     * 通过角色ID查询角色
     * 根据角色ID查询角色详情，包含关联的菜单和部门信息
     *
     * @param roleId 角色ID
     * @return 角色对象信息，包含角色基本信息、菜单列表、部门列表
     */
    @Override
    public SysRoleVo selectRoleById(Long roleId) {
        // 调用Mapper的自定义查询方法，查询角色详情及关联信息
        return baseMapper.selectRoleById(roleId);
    }

    /**
     * 通过角色ID串查询角色
     * 根据角色ID列表查询角色列表，只返回正常状态的角色
     *
     * @param roleIds 角色ID串
     * @return 角色列表信息
     */
    @Override
    public List<SysRoleVo> selectRoleByIds(List<Long> roleIds) {
        // 使用LambdaQueryWrapper构建查询条件：状态为正常、在角色ID列表中
        return baseMapper.selectRoleList(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getStatus, SystemConstants.NORMAL)
            .in(CollUtil.isNotEmpty(roleIds), SysRole::getRoleId, roleIds));
    }

    /**
     * 校验角色名称是否唯一
     * 检查角色名称是否重复，排除当前角色ID（编辑时）
     * 使用MyBatis-Plus的exists方法判断记录是否存在
     *
     * @param role 角色信息，包含角色名称和角色ID（编辑时）
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkRoleNameUnique(SysRoleBo role) {
        // 使用LambdaQueryWrapper构建查询条件：角色名称相同且角色ID不等于当前角色ID（编辑时）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleName, role.getRoleName())
            .ne(ObjectUtil.isNotNull(role.getRoleId()), SysRole::getRoleId, role.getRoleId()));
        // 返回是否唯一（不存在重复记录）
        return !exist;
    }

    /**
     * 校验角色权限是否唯一
     * 检查角色权限字符是否重复，排除当前角色ID（编辑时）
     * 使用MyBatis-Plus的exists方法判断记录是否存在
     *
     * @param role 角色信息，包含角色权限字符和角色ID（编辑时）
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkRoleKeyUnique(SysRoleBo role) {
        // 使用LambdaQueryWrapper构建查询条件：角色权限字符相同且角色ID不等于当前角色ID（编辑时）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleKey, role.getRoleKey())
            .ne(ObjectUtil.isNotNull(role.getRoleId()), SysRole::getRoleId, role.getRoleId()));
        // 返回是否唯一（不存在重复记录）
        return !exist;
    }

    /**
     * 校验角色是否允许操作
     * 检查是否允许操作指定角色，禁止操作超级管理员角色
     * 禁止新增或修改使用系统内置管理员角色标识符
     *
     * @param role 角色信息，包含角色ID、角色权限字符等
     */
    @Override
    public void checkRoleAllowed(SysRoleBo role) {
        // 如果角色ID不为空且是超级管理员角色ID，抛出业务异常
        if (ObjectUtil.isNotNull(role.getRoleId()) && LoginHelper.isSuperAdmin(role.getRoleId())) {
            throw new ServiceException("不允许操作超级管理员角色");
        }
        // 定义系统内置管理员角色标识符数组
        String[] keys = new String[]{TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY};
        // 新增不允许使用管理员标识符
        // 如果是新增角色（角色ID为空）且角色权限字符是系统内置管理员标识符，抛出业务异常
        if (ObjectUtil.isNull(role.getRoleId())
            && StringUtils.equalsAny(role.getRoleKey(), keys)) {
            throw new ServiceException("不允许使用系统内置管理员角色标识符!");
        }
        // 修改不允许修改管理员标识符
        // 如果是修改角色（角色ID不为空）
        if (ObjectUtil.isNotNull(role.getRoleId())) {
            // 查询旧角色信息
            SysRole sysRole = baseMapper.selectById(role.getRoleId());
            // 如果标识符不相等，判断为修改了管理员标识符
            if (!StringUtils.equals(sysRole.getRoleKey(), role.getRoleKey())) {
                // 如果旧标识符是系统内置管理员标识符，抛出业务异常（不允许修改）
                if (StringUtils.equalsAny(sysRole.getRoleKey(), keys)) {
                    throw new ServiceException("不允许修改系统内置管理员角色标识符!");
                } else if (StringUtils.equalsAny(role.getRoleKey(), keys)) {
                    // 如果新标识符是系统内置管理员标识符，抛出业务异常（不允许使用）
                    throw new ServiceException("不允许使用系统内置管理员角色标识符!");
                }
            }
        }
    }

    /**
     * 校验角色是否有数据权限
     * 检查当前用户是否有权限访问指定角色数据
     * 超级管理员拥有所有权限，普通用户受数据权限控制
     *
     * @param roleId 角色id
     */
    @Override
    public void checkRoleDataScope(Long roleId) {
        // 如果角色ID为空，直接返回
        if (ObjectUtil.isNull(roleId)) {
            return;
        }
        // 调用重载方法，将单个ID转换为列表
        this.checkRoleDataScope(Collections.singletonList(roleId));
    }

    /**
     * 校验角色是否有数据权限
     * 检查当前用户是否有权限访问指定角色列表数据
     * 超级管理员拥有所有权限，普通用户受数据权限控制
     *
     * @param roleIds 角色ID列表（支持传单个ID）
     */
    @Override
    public void checkRoleDataScope(List<Long> roleIds) {
        // 如果角色ID列表为空或当前用户是超级管理员，直接返回
        if (CollUtil.isEmpty(roleIds) || LoginHelper.isSuperAdmin()) {
            return;
        }
        // 调用Mapper查询当前用户有权限访问的角色数量
        long count = baseMapper.selectRoleCount(roleIds);
        // 如果有权限访问的角色数量不等于传入的角色数量，说明有部分角色无权限访问
        if (count != roleIds.size()) {
            // 抛出业务异常
            throw new ServiceException("没有权限访问部分角色数据！");
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     * 查询指定角色被多少个用户关联使用
     *
     * @param roleId 角色ID
     * @return 结果 关联的用户数量
     */
    @Override
    public long countUserRoleByRoleId(Long roleId) {
        // 使用LambdaQueryWrapper构建查询条件：角色ID匹配
        // 调用selectCount查询关联的用户数量
        return userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
    }

    /**
     * 新增保存角色信息
     * 新增角色，包含角色基本信息和菜单授权
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param bo 角色信息，包含角色名称、权限字符、菜单ID数组等
     * @return 结果 影响的行数
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRoleBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        // 新增角色信息，返回影响的行数
        baseMapper.insert(role);
        // 设置角色ID到BO对象，供后续关联操作使用
        bo.setRoleId(role.getRoleId());
        // 调用insertRoleMenu插入角色菜单关联，返回影响的行数
        return insertRoleMenu(bo);
    }

    /**
     * 修改保存角色信息
     * 修改角色基本信息和菜单授权
     * 如果角色已分配给用户，不允许禁用
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param bo 角色信息，包含角色ID、角色名称、权限字符、菜单ID数组等
     * @return 结果 影响的行数
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRoleBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysRole role = MapstructUtils.convert(bo, SysRole.class);

        // 如果角色状态为禁用且角色已分配给用户，抛出业务异常
        if (SystemConstants.DISABLE.equals(role.getStatus()) && this.countUserRoleByRoleId(role.getRoleId()) > 0) {
            throw new ServiceException("角色已分配，不能禁用!");
        }
        // 修改角色信息，返回影响的行数
        baseMapper.updateById(role);
        // 删除角色与菜单关联（先清除旧关联）
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getRoleId()));
        // 调用insertRoleMenu插入新的角色菜单关联，返回影响的行数
        return insertRoleMenu(bo);
    }

    /**
     * 修改角色状态
     * 修改角色状态（正常/停用）
     * 如果角色已分配给用户，不允许禁用
     * 使用LambdaUpdateWrapper构建更新条件，类型安全
     *
     * @param roleId 角色ID
     * @param status 角色状态（0正常 1停用）
     * @return 结果 影响的行数
     */
    @Override
    public int updateRoleStatus(Long roleId, String status) {
        // 如果角色状态为禁用且角色已分配给用户，抛出业务异常
        if (SystemConstants.DISABLE.equals(status) && this.countUserRoleByRoleId(roleId) > 0) {
            throw new ServiceException("角色已分配，不能禁用!");
        }
        // 使用LambdaUpdateWrapper构建更新条件：设置状态为指定值，角色ID匹配
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysRole>()
                .set(SysRole::getStatus, status)
                .eq(SysRole::getRoleId, roleId));
    }

    /**
     * 修改数据权限信息
     * 修改角色的数据权限，包含部门范围
     * 清除角色自定义缓存，保证缓存一致性
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param bo 角色信息，包含角色ID、数据权限类型、部门ID数组等
     * @return 结果 影响的行数
     */
    // Spring缓存注解：清除角色自定义缓存，key为角色ID
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#bo.roleId")
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRoleBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        // 修改角色信息，返回影响的行数
        baseMapper.updateById(role);
        // 删除角色与部门关联（先清除旧关联）
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, role.getRoleId()));
        // 调用insertRoleDept插入新的角色部门关联（数据权限），返回影响的行数
        return insertRoleDept(bo);
    }

    /**
     * 新增角色菜单信息
     * 私有方法，为角色分配菜单
     * 构建角色菜单关联列表并批量插入
     *
     * @param role 角色对象，包含角色ID和菜单ID数组
     * @return 结果 影响的行数
     */
    private int insertRoleMenu(SysRoleBo role) {
        // 初始化影响的行数为1（默认成功）
        int rows = 1;
        // 创建角色菜单关联列表
        List<SysRoleMenu> list = new ArrayList<>();
        // 遍历菜单ID数组
        for (Long menuId : role.getMenuIds()) {
            // 创建角色菜单关联实体
            SysRoleMenu rm = new SysRoleMenu();
            // 设置角色ID
            rm.setRoleId(role.getRoleId());
            // 设置菜单ID
            rm.setMenuId(menuId);
            // 添加到列表
            list.add(rm);
        }
        // 如果列表不为空
        if (CollUtil.isNotEmpty(list)) {
            // 批量插入角色菜单关联数据，返回影响的行数
            rows = roleMenuMapper.insertBatch(list) ? list.size() : 0;
        }
        // 返回影响的行数
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     * 私有方法，为角色分配数据权限部门
     * 构建角色部门关联列表并批量插入
     *
     * @param role 角色对象，包含角色ID和部门ID数组
     * @return 结果 影响的行数
     */
    private int insertRoleDept(SysRoleBo role) {
        // 初始化影响的行数为1（默认成功）
        int rows = 1;
        // 创建角色部门关联列表
        List<SysRoleDept> list = new ArrayList<>();
        // 遍历部门ID数组
        for (Long deptId : role.getDeptIds()) {
            // 创建角色部门关联实体
            SysRoleDept rd = new SysRoleDept();
            // 设置角色ID
            rd.setRoleId(role.getRoleId());
            // 设置部门ID
            rd.setDeptId(deptId);
            // 添加到列表
            list.add(rd);
        }
        // 如果列表不为空
        if (CollUtil.isNotEmpty(list)) {
            // 批量插入角色部门关联数据，返回影响的行数
            rows = roleDeptMapper.insertBatch(list) ? list.size() : 0;
        }
        // 返回影响的行数
        return rows;
    }

    /**
     * 通过角色ID删除角色
     * 删除角色，同时删除角色与菜单、部门的关联关系
     * 清除角色自定义缓存，保证缓存一致性
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param roleId 角色ID
     * @return 结果 影响的行数
     */
    // Spring缓存注解：清除角色自定义缓存，key为角色ID
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#roleId")
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Long roleId) {
        // 删除角色与菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        // 删除角色与部门关联
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        // 删除角色基本信息
        return baseMapper.deleteById(roleId);
    }

    /**
     * 批量删除角色信息
     * 批量删除多个角色，同时删除角色与菜单、部门的关联关系
     * 校验数据权限、角色是否允许操作、角色是否已分配给用户
     * 清除所有角色自定义缓存，保证缓存一致性
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param roleIds 需要删除的角色ID列表
     * @return 结果 影响的行数
     */
    // Spring缓存注解：清除所有角色自定义缓存（全部条目）
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, allEntries = true)
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(List<Long> roleIds) {
        // 校验数据权限
        this.checkRoleDataScope(roleIds);
        // 查询角色列表
        List<SysRole> roles = baseMapper.selectByIds(roleIds);
        // 遍历角色列表
        for (SysRole role : roles) {
            // 校验角色是否允许操作（非超级管理员）
            checkRoleAllowed(BeanUtil.toBean(role, SysRoleBo.class));
            // 如果角色已分配给用户，抛出业务异常
            if (countUserRoleByRoleId(role.getRoleId()) > 0) {
                throw new ServiceException(String.format("%1$s已分配，不能删除!", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        // 删除角色与部门关联
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds));
        // 批量删除角色基本信息
        return baseMapper.deleteByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     * 取消指定用户的角色授权，同时踢出该用户的在线状态
     * 不允许修改当前登录用户的角色
     *
     * @param userRole 用户和角色关联信息，包含用户ID和角色ID
     * @return 结果 影响的行数
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole) {
        // 如果尝试修改当前登录用户的角色，抛出业务异常
        if (LoginHelper.getUserId().equals(userRole.getUserId())) {
            throw new ServiceException("不允许修改当前用户角色!");
        }
        // 删除用户角色关联
        int rows = userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, userRole.getRoleId())
            .eq(SysUserRole::getUserId, userRole.getUserId()));
        // 如果删除成功，踢出该用户的在线状态
        if (rows > 0) {
            cleanOnlineUser(List.of(userRole.getUserId()));
        }
        // 返回影响的行数
        return rows;
    }

    /**
     * 批量取消授权用户角色
     * 批量取消多个用户的角色授权，同时踢出这些用户的在线状态
     * 不允许修改当前登录用户的角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID数组
     * @return 结果 影响的行数
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        // 将数组转换为List
        List<Long> ids = List.of(userIds);
        // 如果包含当前登录用户的ID，抛出业务异常
        if (ids.contains(LoginHelper.getUserId())) {
            throw new ServiceException("不允许修改当前用户角色!");
        }
        // 批量删除用户角色关联
        int rows = userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, roleId)
            .in(SysUserRole::getUserId, ids));
        // 如果删除成功，踢出这些用户的在线状态
        if (rows > 0) {
            cleanOnlineUser(ids);
        }
        // 返回影响的行数
        return rows;
    }

    /**
     * 批量选择授权用户角色
     * 批量为多个用户授权角色，同时踢出这些用户的在线状态
     * 不允许修改当前登录用户的角色
     *
     * @param roleId  角色ID
     * @param userIds 需要授权的用户数据ID数组
     * @return 结果 影响的行数
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        // 初始化影响的行数为1（默认成功）
        int rows = 1;
        // 将数组转换为List
        List<Long> ids = List.of(userIds);
        // 如果包含当前登录用户的ID，抛出业务异常
        if (ids.contains(LoginHelper.getUserId())) {
            throw new ServiceException("不允许修改当前用户角色!");
        }
        // 使用StreamUtils将用户ID列表转换为用户角色关联实体列表
        List<SysUserRole> list = StreamUtils.toList(ids, userId -> {
            // 创建用户角色关联实体
            SysUserRole ur = new SysUserRole();
            // 设置用户ID
            ur.setUserId(userId);
            // 设置角色ID
            ur.setRoleId(roleId);
            return ur;
        });
        // 如果列表不为空
        if (CollUtil.isNotEmpty(list)) {
            // 批量插入用户角色关联数据，返回影响的行数
            rows = userRoleMapper.insertBatch(list) ? list.size() : 0;
        }
        // 如果插入成功，踢出这些用户的在线状态
        if (rows > 0) {
            cleanOnlineUser(ids);
        }
        // 返回影响的行数
        return rows;
    }

    /**
     * 根据角色ID清除该角色关联的所有在线用户的登录状态（踢出在线用户）
     * 当角色权限变更时，需要踢出拥有该角色的所有在线用户，使其重新登录获取最新权限
     *
     * <p>
     * 先判断角色是否绑定用户，若无绑定则直接返回
     * 然后遍历当前所有在线Token，查找拥有该角色的用户并强制登出
     * 注意：在线用户量过大时，操作可能导致 Redis 阻塞，需谨慎调用
     * </p>
     *
     * @param roleId 角色ID
     */
    @Override
    public void cleanOnlineUserByRole(Long roleId) {
        // 查询角色关联的用户数量
        Long num = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        // 如果角色未绑定用户，直接返回
        if (num == 0) {
            return;
        }
        // 搜索所有在线Token的key
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        // 如果在线Token列表为空，直接返回
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        // 角色关联的在线用户量过大会导致redis阻塞卡顿，谨慎操作
        // 使用并行流提高处理效率
        keys.parallelStream().forEach(key -> {
            // 从key中提取token（截取最后一个:后面的部分）
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果token已经过期（剩余时间小于-1），跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }
            // 获取登录用户信息
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            // 如果登录用户为空或角色列表为空，跳过
            if (ObjectUtil.isNull(loginUser) || CollUtil.isEmpty(loginUser.getRoles())) {
                return;
            }
            // 如果用户拥有该角色，强制登出
            if (loginUser.getRoles().stream().anyMatch(r -> r.getRoleId().equals(roleId))) {
                try {
                    // 强制登出
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                    // 忽略未登录异常（可能已经被登出）
                }
            }
        });
    }

    /**
     * 根据用户ID列表清除对应在线用户的登录状态（踢出指定用户）
     * 当用户权限变更时，需要踢出指定用户的在线状态，使其重新登录获取最新权限
     *
     * <p>
     * 遍历当前所有在线Token，匹配用户ID列表中的用户，强制登出
     * 注意：在线用户量过大时，操作可能导致 Redis 阻塞，需谨慎调用
     * </p>
     *
     * @param userIds 需要清除的用户ID列表
     */
    @Override
    public void cleanOnlineUser(List<Long> userIds) {
        // 搜索所有在线Token的key
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        // 如果在线Token列表为空，直接返回
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        // 角色关联的在线用户量过大会导致redis阻塞卡顿，谨慎操作
        // 使用并行流提高处理效率
        keys.parallelStream().forEach(key -> {
            // 从key中提取token（截取最后一个:后面的部分）
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果token已经过期（剩余时间小于-1），跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }
            // 获取登录用户信息
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            // 如果登录用户为空，跳过
            if (ObjectUtil.isNull(loginUser)) {
                return;
            }
            // 如果用户ID在需要清除的列表中，强制登出
            if (userIds.contains(loginUser.getUserId())) {
                try {
                    // 强制登出
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                    // 忽略未登录异常（可能已经被登出）
                }
            }
        });
    }

    /**
     * 根据角色 ID 列表查询角色名称映射关系
     * 批量查询角色ID和名称的映射关系，用于数据展示
     *
     * @param roleIds 角色 ID 列表
     * @return Map，其中 key 为角色 ID，value 为对应的角色名称
     */
    @Override
    public Map<Long, String> selectRoleNamesByIds(List<Long> roleIds) {
        // 如果角色ID列表为空，返回空Map
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        // 使用LambdaQueryWrapper构建查询条件，只查询角色ID和名称
        List<SysRole> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysRole>()
                .select(SysRole::getRoleId, SysRole::getRoleName)
                .in(SysRole::getRoleId, roleIds)
        );
        // 使用StreamUtils将列表转换为Map，key为角色ID，value为角色名称
        return StreamUtils.toMap(list, SysRole::getRoleId, SysRole::getRoleName);
    }

}
