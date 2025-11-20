// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Hutool工具类：Bean转换工具，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：数组操作工具，提供数组判空、转换等方法
import cn.hutool.core.util.ArrayUtil;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：查询条件包装器接口
import com.baomidou.mybatisplus.core.conditions.Wrapper;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询包装器
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// MyBatis-Plus核心组件：Lambda更新包装器，支持类型安全更新
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok日志注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心常量：系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 公共核心领域模型：用户DTO，用于跨服务数据传输
import org.dromara.common.core.domain.dto.UserDTO;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心服务接口：通用用户服务接口
import org.dromara.common.core.service.UserService;
// 公共核心工具类：包含所有工具类的静态导入
import org.dromara.common.core.utils.*;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Sa-Token工具类：登录助手，提供获取当前登录用户信息的方法
import org.dromara.common.satoken.utils.LoginHelper;
// 系统领域模型：用户实体类
import org.dromara.system.domain.SysUser;
// 系统领域模型：用户岗位关联实体类
import org.dromara.system.domain.SysUserPost;
// 系统领域模型：用户角色关联实体类
import org.dromara.system.domain.SysUserRole;
// 系统业务对象：用户业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysUserBo;
// 系统视图对象：岗位视图对象
import org.dromara.system.domain.vo.SysPostVo;
// 系统视图对象：角色视图对象
import org.dromara.system.domain.vo.SysRoleVo;
// 系统视图对象：用户导出视图对象
import org.dromara.system.domain.vo.SysUserExportVo;
// 系统视图对象：用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// 系统Mapper接口：用户Mapper
import org.dromara.system.mapper.*;
// 系统服务接口：用户服务接口
import org.dromara.system.service.ISysUserService;
// Spring缓存注解：缓存清除，用于数据变更时清除缓存
import org.springframework.cache.annotation.CacheEvict;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;

// Java集合工具类：提供集合操作
import java.util.*;

/**
 * 用户 业务层处理
 * 核心业务：用户管理、角色分配、岗位分配、数据权限校验
 * 实现接口：ISysUserService（系统用户服务）、UserService（通用用户服务）
 *
 * @author Lion Li
 */
// Lombok日志注解：自动生成slf4j日志对象
@Slf4j
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class SysUserServiceImpl implements ISysUserService, UserService {

    // 用户Mapper，继承BaseMapperPlus，提供用户表CRUD操作
    private final SysUserMapper baseMapper;
    // 部门Mapper，用于查询部门及子部门
    private final SysDeptMapper deptMapper;
    // 角色Mapper，用于查询角色信息和角色数量校验
    private final SysRoleMapper roleMapper;
    // 岗位Mapper，用于查询岗位信息和岗位数量校验
    private final SysPostMapper postMapper;
    // 用户角色关联Mapper，管理用户与角色的多对多关系
    private final SysUserRoleMapper userRoleMapper;
    // 用户岗位关联Mapper，管理用户与岗位的多对多关系
    private final SysUserPostMapper userPostMapper;

    /**
     * 分页查询用户列表（带数据权限）
     * 根据查询条件分页查询用户数据，自动注入数据权限过滤条件
     *
     * @param user 查询条件，包含用户名、状态、部门等
     * @param pageQuery 分页参数，页码、每页大小、排序规则
     * @return 分页用户列表，包含总记录数和当前页数据
     */
    @Override
    public TableDataInfo<SysUserVo> selectPageUserList(SysUserBo user, PageQuery pageQuery) {
        // 构建分页对象和查询条件，调用Mapper查询
        // 数据权限由MyBatis-Plus拦截器自动注入
        Page<SysUserVo> page = baseMapper.selectPageUserList(pageQuery.build(), this.buildQueryWrapper(user));
        // 将Page对象转换为TableDataInfo，适配前端表格组件
        return TableDataInfo.build(page);
    }

    /**
     * 导出用户列表（Excel导出）
     * 根据条件查询用户数据，转换为导出VO格式
     * 支持复杂查询条件：用户名、昵称、手机号、状态、时间范围、部门及子部门
     *
     * @param user 查询条件，包含用户名、状态、部门、时间范围等
     * @return 用户导出VO列表，包含所有需要导出的字段
     */
    @Override
    public List<SysUserExportVo> selectUserExportList(SysUserBo user) {
        // 获取查询参数Map，包含beginTime、endTime等时间范围参数
        Map<String, Object> params = user.getParams();
        // 创建QueryWrapper，使用字符串形式指定表别名（u），便于多表关联查询
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        // 构建查询条件：删除标志为正常、模糊查询用户名、昵称、手机号
        // 时间范围查询、部门及子部门查询、按用户ID升序排序
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getNickName()), "u.nick_name", user.getNickName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "u.create_time", params.get("beginTime"), params.get("endTime"))
            .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                // 查询指定部门及其所有子部门ID
                List<Long> deptIds = deptMapper.selectDeptAndChildById(user.getDeptId());
                w.in("u.dept_id", deptIds);
            }).orderByAsc("u.user_id");
        // 调用Mapper查询并转换为导出VO列表
        return baseMapper.selectUserExportList(wrapper);
    }

    /**
     * 构建用户查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     * 使用MyBatis-Plus的LambdaQueryWrapper实现类型安全查询，避免硬编码字段名
     *
     * @param user 查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private Wrapper<SysUser> buildQueryWrapper(SysUserBo user) {
        // 获取查询参数Map，包含beginTime、endTime等时间范围参数
        Map<String, Object> params = user.getParams();
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查，避免运行时错误
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        // 构建查询条件：删除标志为正常（未删除）
        wrapper.eq(SysUser::getDelFlag, SystemConstants.NORMAL)
            // 精确查询用户ID（当用户ID不为空时）
            .eq(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId())
            // 批量查询用户ID列表，支持逗号分隔字符串，使用StringUtils.splitTo转换为Long列表
            .in(StringUtils.isNotBlank(user.getUserIds()), SysUser::getUserId, StringUtils.splitTo(user.getUserIds(), Convert::toLong))
            // 模糊查询用户名（当用户名不为空时）
            .like(StringUtils.isNotBlank(user.getUserName()), SysUser::getUserName, user.getUserName())
            // 模糊查询昵称（当昵称不为空时）
            .like(StringUtils.isNotBlank(user.getNickName()), SysUser::getNickName, user.getNickName())
            // 精确查询状态（当状态不为空时）
            .eq(StringUtils.isNotBlank(user.getStatus()), SysUser::getStatus, user.getStatus())
            // 模糊查询手机号（当手机号不为空时）
            .like(StringUtils.isNotBlank(user.getPhonenumber()), SysUser::getPhonenumber, user.getPhonenumber())
            // 时间范围查询创建时间（当开始时间和结束时间都不为空时）
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysUser::getCreateTime, params.get("beginTime"), params.get("endTime"))
            // 部门及子部门查询，使用子查询，当部门ID不为空时
            .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                // 查询部门及其所有子部门ID
                List<Long> ids = deptMapper.selectDeptAndChildById(user.getDeptId());
                // 使用in条件查询这些部门下的用户
                w.in(SysUser::getDeptId, ids);
            }).orderByAsc(SysUser::getUserId); // 按用户ID升序排序
        // 排除指定用户ID，支持逗号分隔字符串，当排除用户ID不为空时
        if (StringUtils.isNotBlank(user.getExcludeUserIds())) {
            wrapper.notIn(SysUser::getUserId, StringUtils.splitTo(user.getExcludeUserIds(), Convert::toLong));
        }
        // 返回构建好的查询包装器
        return wrapper;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     * 查询已分配给指定角色的用户列表，支持用户名、状态、手机号等条件筛选
     *
     * @param user 用户信息，包含角色ID、用户名、状态、手机号等查询条件
     * @param pageQuery 分页参数，页码、每页大小、排序规则
     * @return 用户信息集合信息，包含分页数据
     */
    @Override
    public TableDataInfo<SysUserVo> selectAllocatedList(SysUserBo user, PageQuery pageQuery) {
        // 创建QueryWrapper，使用字符串形式指定表别名（u、r）
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        // 构建查询条件：删除标志为正常、精确查询角色ID、模糊查询用户名、精确查询状态、模糊查询手机号、按用户ID升序排序
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(user.getRoleId()), "r.role_id", user.getRoleId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .orderByAsc("u.user_id");
        // 调用Mapper执行分页查询，传入分页对象和查询条件
        Page<SysUserVo> page = baseMapper.selectAllocatedList(pageQuery.build(), wrapper);
        // 将Page对象转换为TableDataInfo，适配前端表格组件
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     * 查询未分配给指定角色的用户列表，支持用户名、手机号等条件筛选
     *
     * @param user 用户信息，包含角色ID、用户名、手机号等查询条件
     * @param pageQuery 分页参数，页码、每页大小、排序规则
     * @return 用户信息集合信息，包含分页数据
     */
    @Override
    public TableDataInfo<SysUserVo> selectUnallocatedList(SysUserBo user, PageQuery pageQuery) {
        // 先查询已分配给该角色的所有用户ID列表
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        // 创建QueryWrapper，使用字符串形式指定表别名（u、r）
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        // 构建查询条件：删除标志为正常、未关联指定角色（或角色为空）、排除已分配的用户ID、模糊查询用户名和手机号、按用户ID升序排序
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .and(w -> w.ne("r.role_id", user.getRoleId()).or().isNull("r.role_id"))
            .notIn(CollUtil.isNotEmpty(userIds), "u.user_id", userIds)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .orderByAsc("u.user_id");
        // 调用Mapper执行分页查询，传入分页对象和查询条件
        Page<SysUserVo> page = baseMapper.selectUnallocatedList(pageQuery.build(), wrapper);
        // 将Page对象转换为TableDataInfo，适配前端表格组件
        return TableDataInfo.build(page);
    }

    /**
     * 通过用户名查询用户
     * 根据用户名精确查询用户详情，返回视图对象
     *
     * @param userName 用户名
     * @return 用户对象信息，包含用户基本信息、部门、角色等
     */
    @Override
    public SysUserVo selectUserByUserName(String userName) {
        // 使用LambdaQueryWrapper构建查询条件，精确匹配用户名
        // selectVoOne方法返回视图对象，自动转换实体为VO
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, userName));
    }

    /**
     * 通过手机号查询用户
     * 根据手机号精确查询用户详情，返回视图对象
     *
     * @param phonenumber 手机号
     * @return 用户对象信息，包含用户基本信息、部门、角色等
     */
    @Override
    public SysUserVo selectUserByPhonenumber(String phonenumber) {
        // 使用LambdaQueryWrapper构建查询条件，精确匹配手机号
        // selectVoOne方法返回视图对象，自动转换实体为VO
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhonenumber, phonenumber));
    }

    /**
     * 通过用户ID查询用户
     * 根据用户ID查询用户详情，包含关联的角色信息
     *
     * @param userId 用户ID
     * @return 用户对象信息，包含用户基本信息和角色列表
     */
    @Override
    public SysUserVo selectUserById(Long userId) {
        // 使用selectVoById查询用户视图对象
        SysUserVo user = baseMapper.selectVoById(userId);
        // 如果用户不存在，直接返回null
        if (ObjectUtil.isNull(user)) {
            return user;
        }
        // 查询并设置用户的角色列表
        user.setRoles(roleMapper.selectRolesByUserId(user.getUserId()));
        // 返回包含角色信息的用户对象
        return user;
    }

    /**
     * 通过用户ID串查询用户
     * 根据用户ID列表和部门ID查询用户列表，只返回必要字段
     *
     * @param userIds 用户ID串，多个ID组成的列表
     * @param deptId  部门id，用于部门筛选
     * @return 用户列表信息，包含用户ID、用户名、昵称
     */
    @Override
    public List<SysUserVo> selectUserByIds(List<Long> userIds, Long deptId) {
        // 使用LambdaQueryWrapper构建查询条件，只查询必要字段（用户ID、用户名、昵称）
        // 状态为正常、部门ID匹配（如果指定）、在用户ID列表中
        return baseMapper.selectUserList(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName)
            .eq(SysUser::getStatus, SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(deptId), SysUser::getDeptId, deptId)
            .in(CollUtil.isNotEmpty(userIds), SysUser::getUserId, userIds));
    }

    /**
     * 查询用户所属角色组
     * 根据用户ID查询该用户关联的所有角色名称，用逗号拼接返回
     *
     * @param userId 用户ID
     * @return 角色名称字符串，多个角色用逗号分隔
     */
    @Override
    public String selectUserRoleGroup(Long userId) {
        // 调用角色Mapper查询用户关联的角色列表
        List<SysRoleVo> list = roleMapper.selectRolesByUserId(userId);
        // 如果角色列表为空，返回空字符串
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        // 使用StreamUtils将角色列表中的角色名称用逗号拼接
        return StreamUtils.join(list, SysRoleVo::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     * 根据用户ID查询该用户关联的所有岗位名称，用逗号拼接返回
     *
     * @param userId 用户ID
     * @return 岗位名称字符串，多个岗位用逗号分隔
     */
    @Override
    public String selectUserPostGroup(Long userId) {
        // 调用岗位Mapper查询用户关联的岗位列表
        List<SysPostVo> list = postMapper.selectPostsByUserId(userId);
        // 如果岗位列表为空，返回空字符串
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        // 使用StreamUtils将岗位列表中的岗位名称用逗号拼接
        return StreamUtils.join(list, SysPostVo::getPostName);
    }

    /**
     * 校验用户名称是否唯一
     * 检查用户名是否重复，排除当前用户ID（编辑时）
     * 使用MyBatis-Plus的exists方法判断记录是否存在
     *
     * @param user 用户信息，包含用户名和用户ID（编辑时）
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkUserNameUnique(SysUserBo user) {
        // 使用LambdaQueryWrapper构建查询条件：用户名相同且用户ID不等于当前用户ID（编辑时）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, user.getUserName())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        // 返回是否唯一（不存在重复记录）
        return !exist;
    }

    /**
     * 校验手机号码是否唯一
     * 检查手机号是否重复，排除当前用户ID（编辑时）
     * 使用MyBatis-Plus的exists方法判断记录是否存在
     *
     * @param user 用户信息，包含手机号和用户ID（编辑时）
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkPhoneUnique(SysUserBo user) {
        // 使用LambdaQueryWrapper构建查询条件：手机号相同且用户ID不等于当前用户ID（编辑时）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhonenumber, user.getPhonenumber())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        // 返回是否唯一（不存在重复记录）
        return !exist;
    }

    /**
     * 校验email是否唯一
     * 检查邮箱是否重复，排除当前用户ID（编辑时）
     * 使用MyBatis-Plus的exists方法判断记录是否存在
     *
     * @param user 用户信息，包含邮箱和用户ID（编辑时）
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkEmailUnique(SysUserBo user) {
        // 使用LambdaQueryWrapper构建查询条件：邮箱相同且用户ID不等于当前用户ID（编辑时）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, user.getEmail())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        // 返回是否唯一（不存在重复记录）
        return !exist;
    }

    /**
     * 校验用户是否允许操作
     * 检查是否允许操作指定用户，禁止操作超级管理员
     * 使用LoginHelper判断是否为超级管理员
     *
     * @param userId 用户ID
     */
    @Override
    public void checkUserAllowed(Long userId) {
        // 如果用户ID不为空且是超级管理员，抛出业务异常
        if (ObjectUtil.isNotNull(userId) && LoginHelper.isSuperAdmin(userId)) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     * 检查当前用户是否有权限访问指定用户数据
     * 超级管理员拥有所有权限，普通用户受数据权限控制
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        // 如果用户ID为空，直接返回
        if (ObjectUtil.isNull(userId)) {
            return;
        }
        // 如果是超级管理员，直接返回（超级管理员拥有所有数据权限）
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        // 调用Mapper查询当前用户是否有权限访问该用户数据
        if (baseMapper.countUserById(userId) == 0) {
            // 如果没有权限，抛出业务异常
            throw new ServiceException("没有权限访问用户数据！");
        }
    }

    /**
     * 新增保存用户信息
     * 新增用户，包含用户基本信息、岗位关联、角色关联
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param user 用户信息，包含用户基本信息、岗位ID数组、角色ID数组
     * @return 结果 影响的行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUserBo user) {
        // 使用Mapstruct将BO转换为实体对象
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 新增用户信息，返回影响的行数
        int rows = baseMapper.insert(sysUser);
        // 设置用户ID到BO对象，供后续关联操作使用
        user.setUserId(sysUser.getUserId());
        // 新增用户岗位关联（不清除旧关联，因为是新增用户）
        insertUserPost(user, false);
        // 新增用户与角色管理（不清除旧关联，因为是新增用户）
        insertUserRole(user, false);
        // 返回影响的行数
        return rows;
    }

    /**
     * 注册用户信息
     * 用户注册接口，设置创建人和更新人为系统用户（0），并设置租户ID
     *
     * @param user 用户信息，包含用户名、密码、昵称等
     * @param tenantId 租户ID，用于多租户隔离
     * @return 结果 true成功 false失败
     */
    @Override
    public boolean registerUser(SysUserBo user, String tenantId) {
        // 设置创建人为系统用户（0），表示系统自动创建
        user.setCreateBy(0L);
        // 设置更新人为系统用户（0），表示系统自动更新
        user.setUpdateBy(0L);
        // 使用Mapstruct将BO转换为实体对象
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 设置租户ID，实现多租户数据隔离
        sysUser.setTenantId(tenantId);
        // 插入用户数据，返回是否成功（影响行数大于0）
        return baseMapper.insert(sysUser) > 0;
    }

    /**
     * 修改保存用户信息
     * 修改用户基本信息、角色关联、岗位关联
     * 清除用户昵称缓存，保证缓存一致性
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param user 用户信息，包含用户基本信息、岗位ID数组、角色ID数组
     * @return 结果 影响的行数
     */
    @Override
    // Spring缓存注解：清除用户昵称缓存，key为用户ID
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUserBo user) {
        // 新增用户与角色管理（先清除旧关联，再插入新关联）
        insertUserRole(user, true);
        // 新增用户与岗位管理（先清除旧关联，再插入新关联）
        insertUserPost(user, true);
        // 使用Mapstruct将BO转换为实体对象
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 更新用户信息，返回影响的行数
        int flag = baseMapper.updateById(sysUser);
        // 如果更新失败（影响行数小于1），抛出业务异常
        if (flag < 1) {
            throw new ServiceException("修改用户{}信息失败", user.getUserName());
        }
        // 返回影响的行数
        return flag;
    }

    /**
     * 用户授权角色
     * 为用户分配角色，先清除旧的角色关联，再插入新的角色关联
     * 使用事务保证数据一致性
     *
     * @param userId  用户ID
     * @param roleIds 角色组，角色ID数组
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        // 调用私有方法插入用户角色关联（清除旧关联）
        insertUserRole(userId, roleIds, true);
    }

    /**
     * 修改用户状态
     * 修改用户账号状态（正常/停用）
     * 使用LambdaUpdateWrapper构建更新条件，类型安全
     *
     * @param userId 用户ID
     * @param status 帐号状态（0正常 1停用）
     * @return 结果 影响的行数
     */
    @Override
    public int updateUserStatus(Long userId, String status) {
        // 使用LambdaUpdateWrapper构建更新条件：设置状态为指定值，用户ID匹配
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getStatus, status)
                .eq(SysUser::getUserId, userId));
    }

    /**
     * 修改用户基本信息
     * 修改用户个人资料：昵称、手机号、邮箱、性别
     * 清除用户昵称缓存，保证缓存一致性
     *
     * @param user 用户信息，包含用户ID、昵称、手机号、邮箱、性别
     * @return 结果 影响的行数
     */
    // Spring缓存注解：清除用户昵称缓存，key为用户ID
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    @Override
    public int updateUserProfile(SysUserBo user) {
        // 使用LambdaUpdateWrapper构建更新条件：动态设置昵称（如果不为空）、手机号、邮箱、性别
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(ObjectUtil.isNotNull(user.getNickName()), SysUser::getNickName, user.getNickName())
                .set(SysUser::getPhonenumber, user.getPhonenumber())
                .set(SysUser::getEmail, user.getEmail())
                .set(SysUser::getSex, user.getSex())
                .eq(SysUser::getUserId, user.getUserId()));
    }

    /**
     * 修改用户头像
     * 修改用户头像ID，关联文件表
     *
     * @param userId 用户ID
     * @param avatar 头像地址（文件ID）
     * @return 结果 true成功 false失败
     */
    @Override
    public boolean updateUserAvatar(Long userId, Long avatar) {
        // 使用LambdaUpdateWrapper构建更新条件：设置头像ID，用户ID匹配
        // 返回是否成功（影响行数大于0）
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getAvatar, avatar)
                .eq(SysUser::getUserId, userId)) > 0;
    }

    /**
     * 重置用户密码
     * 重置用户密码为指定值（已加密）
     *
     * @param userId   用户ID
     * @param password 密码（已加密）
     * @return 结果 影响的行数
     */
    @Override
    public int resetUserPwd(Long userId, String password) {
        // 使用LambdaUpdateWrapper构建更新条件：设置密码，用户ID匹配
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, password)
                .eq(SysUser::getUserId, userId));
    }

    /**
     * 新增用户角色信息
     * 私有方法，为用户分配角色
     * 调用重载方法，传入用户ID和角色ID数组
     *
     * @param user  用户对象，包含用户ID和角色ID数组
     * @param clear 是否清除已存在的关联数据（true清除后插入，false直接插入）
     */
    private void insertUserRole(SysUserBo user, boolean clear) {
        // 调用重载方法，传入用户ID和角色ID数组
        this.insertUserRole(user.getUserId(), user.getRoleIds(), clear);
    }

    /**
     * 新增用户岗位信息
     * 私有方法，为用户分配岗位
     * 包含权限校验、清除旧关联、批量插入新关联
     *
     * @param user  用户对象，包含用户ID和岗位ID数组
     * @param clear 是否清除已存在的关联数据（true清除后插入，false直接插入）
     */
    private void insertUserPost(SysUserBo user, boolean clear) {
        // 获取岗位ID数组
        Long[] postIdArr = user.getPostIds();
        // 如果岗位ID数组为空，直接返回
        if (ArrayUtil.isEmpty(postIdArr)) {
            return;
        }
        // 将数组转换为List
        List<Long> postIds = Arrays.asList(postIdArr);

        // 校验是否有权限操作这些岗位（含数据权限控制）
        // 查询岗位数量，如果查询结果数量不等于传入的岗位数量，说明有部分岗位无权限访问
        if (postMapper.selectPostCount(postIds) != postIds.size()) {
            throw new ServiceException("没有权限访问岗位的数据");
        }

        // 是否清除旧的用户岗位绑定（当clear为true时）
        if (clear) {
            // 删除该用户的所有岗位关联
            userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, user.getUserId()));
        }

        // 构建用户岗位关联列表并批量插入
        // 使用StreamUtils.toList将岗位ID列表转换为用户岗位关联实体列表
        List<SysUserPost> list = StreamUtils.toList(postIds,
            postId -> {
                // 创建用户岗位关联实体
                SysUserPost up = new SysUserPost();
                // 设置用户ID
                up.setUserId(user.getUserId());
                // 设置岗位ID
                up.setPostId(postId);
                return up;
            });
        // 批量插入用户岗位关联数据
        userPostMapper.insertBatch(list);
    }

    /**
     * 新增用户角色信息
     * 私有方法，为用户分配角色
     * 包含超级管理员角色过滤、权限校验、清除旧关联、批量插入新关联
     *
     * @param userId  用户ID
     * @param roleIds 角色组，角色ID数组
     * @param clear   是否清除已存在的关联数据（true清除后插入，false直接插入）
     */
    private void insertUserRole(Long userId, Long[] roleIds, boolean clear) {
        // 如果角色ID数组为空，直接返回
        if (ArrayUtil.isEmpty(roleIds)) {
            return;
        }

        // 将数组转换为ArrayList，支持删除操作
        List<Long> roleList = new ArrayList<>(Arrays.asList(roleIds));

        // 非超级管理员，禁止包含超级管理员角色
        // 如果不是超级管理员且角色列表包含超级管理员角色ID，则移除
        if (!LoginHelper.isSuperAdmin(userId)) {
            roleList.remove(SystemConstants.SUPER_ADMIN_ID);
        }

        // 校验是否有权限访问这些角色（含数据权限控制）
        // 查询角色数量，如果查询结果数量不等于传入的角色数量，说明有部分角色无权限访问
        if (roleMapper.selectRoleCount(roleList) != roleList.size()) {
            throw new ServiceException("没有权限访问角色的数据");
        }

        // 是否清除原有绑定（当clear为true时）
        if (clear) {
            // 删除该用户的所有角色关联
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        }

        // 批量插入用户-角色关联
        // 使用StreamUtils.toList将角色ID列表转换为用户角色关联实体列表
        List<SysUserRole> list = StreamUtils.toList(roleList,
            roleId -> {
                // 创建用户角色关联实体
                SysUserRole ur = new SysUserRole();
                // 设置用户ID
                ur.setUserId(userId);
                // 设置角色ID
                ur.setRoleId(roleId);
                return ur;
            });
        // 批量插入用户角色关联数据
        userRoleMapper.insertBatch(list);
    }

    /**
     * 通过用户ID删除用户
     * 删除用户，同时删除用户与角色、岗位的关联关系
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param userId 用户ID
     * @return 结果 影响的行数
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 删除用户与岗位关联
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        // 删除用户基本信息，防止更新失败导致的数据误删除
        int flag = baseMapper.deleteById(userId);
        // 如果删除失败（影响行数小于1），抛出业务异常
        if (flag < 1) {
            throw new ServiceException("删除用户失败!");
        }
        // 返回影响的行数
        return flag;
    }

    /**
     * 批量删除用户信息
     * 批量删除多个用户，同时删除用户与角色、岗位的关联关系
     * 校验用户是否允许操作（非超级管理员）和数据权限
     * 使用事务保证数据一致性，任何异常都会回滚
     *
     * @param userIds 需要删除的用户ID数组
     * @return 结果 影响的行数
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        // 遍历用户ID数组，校验每个用户是否允许操作和数据权限
        for (Long userId : userIds) {
            // 校验是否允许操作（非超级管理员）
            checkUserAllowed(userId);
            // 校验数据权限
            checkUserDataScope(userId);
        }
        // 将数组转换为List
        List<Long> ids = List.of(userIds);
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        // 删除用户与岗位关联
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, ids));
        // 批量删除用户基本信息，防止更新失败导致的数据误删除
        int flag = baseMapper.deleteByIds(ids);
        // 如果删除失败（影响行数小于1），抛出业务异常
        if (flag < 1) {
            throw new ServiceException("删除用户失败!");
        }
        // 返回影响的行数
        return flag;
    }

    /**
     * 通过部门id查询当前部门所有用户
     * 查询指定部门下的所有用户列表，按用户ID升序排序
     *
     * @param deptId 部门ID
     * @return 用户信息集合信息，包含用户基本信息
     */
    @Override
    public List<SysUserVo> selectUserListByDept(Long deptId) {
        // 创建LambdaQueryWrapper
        LambdaQueryWrapper<SysUser> lqw = Wrappers.lambdaQuery();
        // 精确查询部门ID
        lqw.eq(SysUser::getDeptId, deptId);
        // 按用户ID升序排序
        lqw.orderByAsc(SysUser::getUserId);
        // 查询用户视图对象列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 通过用户ID查询用户账户
     * 根据用户ID查询用户名，使用Redis缓存提升性能
     * 缓存key为"sys_user_name:{userId}"
     *
     * @param userId 用户ID
     * @return 用户账户（用户名）
     */
    // Spring缓存注解：查询时缓存结果，key为用户ID
    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    @Override
    public String selectUserNameById(Long userId) {
        // 使用LambdaQueryWrapper构建查询条件，只查询用户名字段
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserName).eq(SysUser::getUserId, userId));
        // 使用ObjectUtils.notNullGetter安全获取用户名（防止NPE）
        return ObjectUtils.notNullGetter(sysUser, SysUser::getUserName);
    }

    /**
     * 通过用户ID查询用户账户
     * 根据用户ID查询用户昵称，使用Redis缓存提升性能
     * 缓存key为"sys_nickname:{userId}"
     *
     * @param userId 用户ID
     * @return 用户账户（昵称）
     */
    @Override
    // Spring缓存注解：查询时缓存结果，key为用户ID
    @Cacheable(cacheNames = CacheNames.SYS_NICKNAME, key = "#userId")
    public String selectNicknameById(Long userId) {
        // 使用LambdaQueryWrapper构建查询条件，只查询昵称字段
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getNickName).eq(SysUser::getUserId, userId));
        // 使用ObjectUtils.notNullGetter安全获取昵称（防止NPE）
        return ObjectUtils.notNullGetter(sysUser, SysUser::getNickName);
    }

    /**
     * 通过用户ID查询用户账户
     * 根据逗号分隔的用户ID字符串查询多个用户的昵称，用逗号拼接返回
     * 使用AOP代理调用，确保缓存注解生效
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户账户（昵称），多个昵称用逗号分隔
     */
    @Override
    public String selectNicknameByIds(String userIds) {
        // 创建昵称列表
        List<String> list = new ArrayList<>();
        // 将逗号分隔的用户ID字符串转换为Long列表并遍历
        for (Long id : StringUtils.splitTo(userIds, Convert::toLong)) {
            // 使用AOP代理调用selectNicknameById，确保@Cacheable注解生效（同类调用注解失效问题）
            String nickname = SpringUtils.getAopProxy(this).selectNicknameById(id);
            // 如果昵称不为空，添加到列表
            if (StringUtils.isNotBlank(nickname)) {
                list.add(nickname);
            }
        }
        // 将昵称列表用逗号拼接返回
        return StringUtils.joinComma(list);
    }

    /**
     * 通过用户ID查询用户手机号
     * 根据用户ID查询用户手机号
     *
     * @param userId 用户id
     * @return 用户手机号
     */
    @Override
    public String selectPhonenumberById(Long userId) {
        // 使用LambdaQueryWrapper构建查询条件，只查询手机号字段
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getPhonenumber).eq(SysUser::getUserId, userId));
        // 使用ObjectUtils.notNullGetter安全获取手机号（防止NPE）
        return ObjectUtils.notNullGetter(sysUser, SysUser::getPhonenumber);
    }

    /**
     * 通过用户ID查询用户邮箱
     * 根据用户ID查询用户邮箱
     *
     * @param userId 用户id
     * @return 用户邮箱
     */
    @Override
    public String selectEmailById(Long userId) {
        // 使用LambdaQueryWrapper构建查询条件，只查询邮箱字段
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getEmail).eq(SysUser::getUserId, userId));
        // 使用ObjectUtils.notNullGetter安全获取邮箱（防止NPE）
        return ObjectUtils.notNullGetter(sysUser, SysUser::getEmail);
    }

    /**
     * 通过用户ID查询用户列表
     * 根据用户ID列表查询用户基本信息，转换为UserDTO
     * 只查询正常状态的用户
     *
     * @param userIds 用户ids
     * @return 用户列表，UserDTO对象列表
     */
    @Override
    public List<UserDTO> selectListByIds(List<Long> userIds) {
        // 如果用户ID列表为空，返回空列表
        if (CollUtil.isEmpty(userIds)) {
            return List.of();
        }
        // 使用LambdaQueryWrapper构建查询条件，只查询必要字段
        List<SysUserVo> list = baseMapper.selectVoList(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserId, SysUser::getDeptId, SysUser::getUserName,
                SysUser::getNickName, SysUser::getUserType, SysUser::getEmail,
                SysUser::getPhonenumber, SysUser::getSex, SysUser::getStatus,
                SysUser::getCreateTime)
            .eq(SysUser::getStatus, SystemConstants.NORMAL)
            .in(SysUser::getUserId, userIds));
        // 使用Hutool的BeanUtil将SysUserVo列表转换为UserDTO列表
        return BeanUtil.copyToList(list, UserDTO.class);
    }

    /**
     * 通过角色ID查询用户ID
     * 根据角色ID列表查询关联的用户ID列表
     *
     * @param roleIds 角色ids
     * @return 用户ids
     */
    @Override
    public List<Long> selectUserIdsByRoleIds(List<Long> roleIds) {
        // 如果角色ID列表为空，返回空列表
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }
        // 查询用户角色关联表，获取用户角色关联列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));
        // 使用StreamUtils提取用户ID列表
        return StreamUtils.toList(userRoles, SysUserRole::getUserId);
    }

    /**
     * 通过角色ID查询用户
     * 根据角色ID列表查询关联的用户列表，转换为UserDTO
     *
     * @param roleIds 角色ids
     * @return 用户，UserDTO对象列表
     */
    @Override
    public List<UserDTO> selectUsersByRoleIds(List<Long> roleIds) {
        // 如果角色ID列表为空，返回空列表
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }

        // 通过角色ID获取用户角色信息
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));

        // 使用StreamUtils提取用户ID列表，转换为Set去重
        Set<Long> userIds = StreamUtils.toSet(userRoles, SysUserRole::getUserId);

        // 调用selectListByIds查询用户列表
        return this.selectListByIds(new ArrayList<>(userIds));
    }

    /**
     * 通过部门ID查询用户
     * 根据部门ID列表查询用户列表，转换为UserDTO
     * 只查询正常状态的用户
     *
     * @param deptIds 部门ids
     * @return 用户，UserDTO对象列表
     */
    @Override
    public List<UserDTO> selectUsersByDeptIds(List<Long> deptIds) {
        // 如果部门ID列表为空，返回空列表
        if (CollUtil.isEmpty(deptIds)) {
            return List.of();
        }
        // 使用LambdaQueryWrapper构建查询条件，只查询必要字段
        List<SysUserVo> list = baseMapper.selectVoList(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName, SysUser::getEmail, SysUser::getPhonenumber)
            .eq(SysUser::getStatus, SystemConstants.NORMAL)
            .in(SysUser::getDeptId, deptIds));
        // 使用Hutool的BeanUtil将SysUserVo列表转换为UserDTO列表
        return BeanUtil.copyToList(list, UserDTO.class);
    }

    /**
     * 通过岗位ID查询用户
     * 根据岗位ID列表查询关联的用户列表，转换为UserDTO
     *
     * @param postIds 岗位ids
     * @return 用户，UserDTO对象列表
     */
    @Override
    public List<UserDTO> selectUsersByPostIds(List<Long> postIds) {
        // 如果岗位ID列表为空，返回空列表
        if (CollUtil.isEmpty(postIds)) {
            return List.of();
        }

        // 通过岗位ID获取用户岗位信息
        List<SysUserPost> userPosts = userPostMapper.selectList(
            new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getPostId, postIds));

        // 使用StreamUtils提取用户ID列表，转换为Set去重
        Set<Long> userIds = StreamUtils.toSet(userPosts, SysUserPost::getUserId);

        // 调用selectListByIds查询用户列表
        return this.selectListByIds(new ArrayList<>(userIds));
    }

    /**
     * 根据用户 ID 列表查询用户名称映射关系
     * 批量查询用户ID和昵称的映射关系，用于数据展示
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户名称（昵称）
     */
    @Override
    public Map<Long, String> selectUserNamesByIds(List<Long> userIds) {
        // 如果用户ID列表为空，返回空Map
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        // 使用LambdaQueryWrapper构建查询条件，只查询用户ID和昵称
        List<SysUser> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getNickName)
                .in(SysUser::getUserId, userIds)
        );
        // 使用StreamUtils将列表转换为Map，key为用户ID，value为昵称
        return StreamUtils.toMap(list, SysUser::getUserId, SysUser::getNickName);
    }

}
