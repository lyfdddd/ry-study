// 定义包路径，将当前类归类到系统服务实现层
package org.dromara.system.service.impl;

// 引入Hutool的Bean工具类，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
// 引入Hutool的集合工具类，用于集合操作
import cn.hutool.core.collection.CollUtil;
// 引入Hutool的类型转换工具类
import cn.hutool.core.convert.Convert;
// 引入Hutool的对象工具类，用于判断对象是否为空
import cn.hutool.core.util.ObjectUtil;
// 引入Hutool的随机数工具类，用于生成随机租户ID
import cn.hutool.core.util.RandomUtil;
// 引入Hutool的BCrypt加密工具类，用于密码加密
import cn.hutool.crypto.digest.BCrypt;
// 引入MyBatis-Plus的Lambda查询包装器，支持类型安全的字段引用
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 引入MyBatis-Plus的Wrappers工具类，快速创建查询包装器
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// 引入MyBatis-Plus的分页插件，用于物理分页查询
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 引入Lombok的RequiredArgsConstructor注解，自动生成包含final字段的构造函数
import lombok.RequiredArgsConstructor;
// 引入缓存名称常量定义
import org.dromara.common.core.constant.CacheNames;
// 引入通用常量定义
import org.dromara.common.core.constant.Constants;
// 引入系统常量定义，包含状态值等常量
import org.dromara.common.core.constant.SystemConstants;
// 引入租户常量定义，包含默认租户ID等
import org.dromara.common.core.constant.TenantConstants;
// 引入服务异常类，用于抛出业务异常
import org.dromara.common.core.exception.ServiceException;
// 引入工作流服务接口，用于同步流程定义
import org.dromara.common.core.service.WorkflowService;
// 引入Mapstruct工具类，用于对象之间的转换
import org.dromara.common.core.utils.MapstructUtils;
// 引入Spring工具类，用于获取Bean和代理对象
import org.dromara.common.core.utils.SpringUtils;
// 引入Stream工具类，用于集合的流式处理
import org.dromara.common.core.utils.StreamUtils;
// 引入字符串工具类，用于字符串操作
import org.dromara.common.core.utils.StringUtils;
// 引入分页查询实体类，封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 引入表格数据信息类，封装分页查询结果
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 引入缓存工具类，用于操作Redis缓存
import org.dromara.common.redis.utils.CacheUtils;
// 引入租户实体基类，包含租户ID字段
import org.dromara.common.tenant.core.TenantEntity;
// 引入租户助手类，用于租户上下文切换和忽略租户过滤
import org.dromara.common.tenant.helper.TenantHelper;
// 引入系统所有实体类（使用通配符导入所有domain类）
import org.dromara.system.domain.*;
// 引入租户业务对象，用于接收前端传来的租户操作参数
import org.dromara.system.domain.bo.SysTenantBo;
// 引入租户视图对象，用于返回给前端的租户信息
import org.dromara.system.domain.vo.SysTenantVo;
// 引入系统所有Mapper接口（使用通配符导入所有mapper类）
import org.dromara.system.mapper.*;
// 引入租户服务接口，定义租户相关的业务方法
import org.dromara.system.service.ISysTenantService;
// 引入Spring的CacheEvict注解，用于清除缓存
import org.springframework.cache.annotation.CacheEvict;
// 引入Spring的Cacheable注解，用于缓存查询结果
import org.springframework.cache.annotation.Cacheable;
// 引入Spring的Service注解，标识该类为服务层组件
import org.springframework.stereotype.Service;
// 引入Spring的事务注解，确保方法在事务中执行
import org.springframework.transaction.annotation.Transactional;

// 引入Java集合工具类
import java.util.*;

/**
 * 租户Service业务层处理
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
public class SysTenantServiceImpl implements ISysTenantService {

    private final SysTenantMapper baseMapper;
    private final SysTenantPackageMapper tenantPackageMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final SysConfigMapper configMapper;

    /**
     * 查询租户
     */
    @Override
    public SysTenantVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 基于租户ID查询租户
     */
    @Cacheable(cacheNames = CacheNames.SYS_TENANT, key = "#tenantId")
    @Override
    public SysTenantVo queryByTenantId(String tenantId) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysTenant>().eq(SysTenant::getTenantId, tenantId));
    }

    /**
     * 查询租户列表
     */
    @Override
    public TableDataInfo<SysTenantVo> queryPageList(SysTenantBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysTenant> lqw = buildQueryWrapper(bo);
        Page<SysTenantVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询租户列表
     */
    @Override
    public List<SysTenantVo> queryList(SysTenantBo bo) {
        LambdaQueryWrapper<SysTenant> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysTenant> buildQueryWrapper(SysTenantBo bo) {
        LambdaQueryWrapper<SysTenant> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getTenantId()), SysTenant::getTenantId, bo.getTenantId());
        lqw.like(StringUtils.isNotBlank(bo.getContactUserName()), SysTenant::getContactUserName, bo.getContactUserName());
        lqw.eq(StringUtils.isNotBlank(bo.getContactPhone()), SysTenant::getContactPhone, bo.getContactPhone());
        lqw.like(StringUtils.isNotBlank(bo.getCompanyName()), SysTenant::getCompanyName, bo.getCompanyName());
        lqw.eq(StringUtils.isNotBlank(bo.getLicenseNumber()), SysTenant::getLicenseNumber, bo.getLicenseNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress()), SysTenant::getAddress, bo.getAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getIntro()), SysTenant::getIntro, bo.getIntro());
        lqw.like(StringUtils.isNotBlank(bo.getDomain()), SysTenant::getDomain, bo.getDomain());
        lqw.eq(bo.getPackageId() != null, SysTenant::getPackageId, bo.getPackageId());
        lqw.eq(bo.getExpireTime() != null, SysTenant::getExpireTime, bo.getExpireTime());
        lqw.eq(bo.getAccountCount() != null, SysTenant::getAccountCount, bo.getAccountCount());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysTenant::getStatus, bo.getStatus());
        lqw.orderByAsc(SysTenant::getId);
        return lqw;
    }

    /**
     * 新增租户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SysTenantBo bo) {
        SysTenant add = MapstructUtils.convert(bo, SysTenant.class);

        // 获取所有租户编号
        List<String> tenantIds = baseMapper.selectObjs(
            new LambdaQueryWrapper<SysTenant>().select(SysTenant::getTenantId), x -> {
                return Convert.toStr(x);
            });
        String tenantId = generateTenantId(tenantIds);
        add.setTenantId(tenantId);
        boolean flag = baseMapper.insert(add) > 0;
        if (!flag) {
            throw new ServiceException("创建租户失败");
        }
        bo.setId(add.getId());

        // 根据套餐创建角色
        Long roleId = createTenantRole(tenantId, bo.getPackageId());

        // 创建部门: 公司名是部门名称
        SysDept dept = new SysDept();
        dept.setTenantId(tenantId);
        dept.setDeptName(bo.getCompanyName());
        dept.setParentId(Constants.TOP_PARENT_ID);
        dept.setAncestors(Constants.TOP_PARENT_ID.toString());
        deptMapper.insert(dept);
        Long deptId = dept.getDeptId();

        // 角色和部门关联表
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setRoleId(roleId);
        roleDept.setDeptId(deptId);
        roleDeptMapper.insert(roleDept);

        // 创建系统用户
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUserName(bo.getUsername());
        user.setNickName(bo.getUsername());
        user.setPassword(BCrypt.hashpw(bo.getPassword()));
        user.setDeptId(deptId);
        userMapper.insert(user);
        //新增系统用户后，默认当前用户为部门的负责人
        SysDept sd = new SysDept();
        sd.setLeader(user.getUserId());
        sd.setDeptId(deptId);
        deptMapper.updateById(sd);

        // 用户和角色关联表
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);

        String defaultTenantId = TenantConstants.DEFAULT_TENANT_ID;
        List<SysDictType> dictTypeList = dictTypeMapper.selectList(
            new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getTenantId, defaultTenantId));
        List<SysDictData> dictDataList = dictDataMapper.selectList(
            new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getTenantId, defaultTenantId));
        for (SysDictType dictType : dictTypeList) {
            dictType.setDictId(null);
            dictType.setTenantId(tenantId);
            dictType.setCreateDept(null);
            dictType.setCreateBy(null);
            dictType.setCreateTime(null);
            dictType.setUpdateBy(null);
            dictType.setUpdateTime(null);
        }
        for (SysDictData dictData : dictDataList) {
            dictData.setDictCode(null);
            dictData.setTenantId(tenantId);
            dictData.setCreateDept(null);
            dictData.setCreateBy(null);
            dictData.setCreateTime(null);
            dictData.setUpdateBy(null);
            dictData.setUpdateTime(null);
        }
        dictTypeMapper.insertBatch(dictTypeList);
        dictDataMapper.insertBatch(dictDataList);

        List<SysConfig> sysConfigList = configMapper.selectList(
            new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getTenantId, defaultTenantId));
        for (SysConfig config : sysConfigList) {
            config.setConfigId(null);
            config.setTenantId(tenantId);
            config.setCreateDept(null);
            config.setCreateBy(null);
            config.setCreateTime(null);
            config.setUpdateBy(null);
            config.setUpdateTime(null);
        }
        configMapper.insertBatch(sysConfigList);

        // 未开启工作流不执行下方操作
        if (SpringUtils.getProperty("warm-flow.enabled", Boolean.class, false)) {
            WorkflowService workflowService = SpringUtils.getBean(WorkflowService.class);
            // 新增租户流程定义
            workflowService.syncDef(tenantId);
        }
        return true;
    }

    /**
     * 生成租户id
     *
     * @param tenantIds 已有租户id列表
     * @return 租户id
     */
    private String generateTenantId(List<String> tenantIds) {
        // 随机生成6位
        String numbers = RandomUtil.randomNumbers(6);
        // 判断是否存在，如果存在则重新生成
        if (tenantIds.contains(numbers)) {
            return generateTenantId(tenantIds);
        }
        return numbers;
    }

    /**
     * 根据租户菜单创建租户角色
     *
     * @param tenantId  租户编号
     * @param packageId 租户套餐id
     * @return 角色id
     */
    private Long createTenantRole(String tenantId, Long packageId) {
        // 获取租户套餐
        SysTenantPackage tenantPackage = tenantPackageMapper.selectById(packageId);
        if (ObjectUtil.isNull(tenantPackage)) {
            throw new ServiceException("套餐不存在");
        }
        // 获取套餐菜单id
        List<Long> menuIds = StringUtils.splitTo(tenantPackage.getMenuIds(), Convert::toLong);

        // 创建角色
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setRoleName(TenantConstants.TENANT_ADMIN_ROLE_NAME);
        role.setRoleKey(TenantConstants.TENANT_ADMIN_ROLE_KEY);
        role.setRoleSort(1);
        role.setStatus(SystemConstants.NORMAL);
        roleMapper.insert(role);
        Long roleId = role.getRoleId();

        // 创建角色菜单
        List<SysRoleMenu> roleMenus = new ArrayList<>(menuIds.size());
        menuIds.forEach(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        });
        roleMenuMapper.insertBatch(roleMenus);

        return roleId;
    }

    /**
     * 修改租户
     * 更新租户基本信息，清除缓存保证数据一致性
     * 不允许修改租户编号和套餐ID，防止破坏数据完整性
     *
     * @param bo 租户业务对象
     * @return 是否修改成功
     */
    // Spring缓存注解：清除CacheNames.SYS_TENANT中key为tenantId的缓存
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "#bo.tenantId")
    @Override
    public Boolean updateByBo(SysTenantBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysTenant tenant = MapstructUtils.convert(bo, SysTenant.class);
        // 将租户编号设为null，防止误修改
        tenant.setTenantId(null);
        // 将套餐ID设为null，防止误修改
        tenant.setPackageId(null);
        // 执行更新操作，返回影响的行数
        return baseMapper.updateById(tenant) > 0;
    }

    /**
     * 修改租户状态
     * 单独更新租户状态字段，清除缓存保证数据一致性
     *
     * @param bo 租户信息
     * @return 影响的行数
     */
    // Spring缓存注解：清除CacheNames.SYS_TENANT中key为tenantId的缓存
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "#bo.tenantId")
    @Override
    public int updateTenantStatus(SysTenantBo bo) {
        // 创建租户实体对象，只设置需要更新的字段
        SysTenant tenant = new SysTenant();
        // 设置租户ID
        tenant.setId(bo.getId());
        // 设置状态
        tenant.setStatus(bo.getStatus());
        // 执行更新操作，返回影响的行数
        return baseMapper.updateById(tenant);
    }

    /**
     * 校验租户是否允许操作
     * 防止对系统默认租户进行非法操作，保护系统安全
     *
     * @param tenantId 租户ID
     */
    @Override
    public void checkTenantAllowed(String tenantId) {
        // 如果租户ID不为空且为默认租户ID，则抛出异常
        if (ObjectUtil.isNotNull(tenantId) && TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            throw new ServiceException("不允许操作管理租户");
        }
    }

    /**
     * 批量删除租户
     * 删除前进行业务校验，清除所有租户缓存
     *
     * @param ids 需要删除的租户ID集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    // Spring缓存注解：清除CacheNames.SYS_TENANT中的所有缓存条目
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, allEntries = true)
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 如果需要进行有效性校验
        if (isValid) {
            // 校验是否包含超管租户ID，防止误删
            if (ids.contains(TenantConstants.SUPER_ADMIN_ID)) {
                throw new ServiceException("超管租户不能删除");
            }
        }
        // 执行批量删除操作，返回影响的行数
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 校验企业名称是否唯一
     * 用于新增和修改时的重复性校验，排除当前记录
     *
     * @param bo 租户业务对象
     * @return 是否唯一（true：唯一，false：重复）
     */
    @Override
    public boolean checkCompanyNameUnique(SysTenantBo bo) {
        // 查询是否存在同名企业，使用exists方法提高性能
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysTenant>()
            .eq(SysTenant::getCompanyName, bo.getCompanyName())
            // 如果tenantId不为空，则排除当前记录（修改场景）
            .ne(ObjectUtil.isNotNull(bo.getTenantId()), SysTenant::getTenantId, bo.getTenantId()));
        // 返回是否唯一（取反）
        return !exist;
    }

    /**
     * 校验账号余额
     * 检查租户是否还有可用账号名额，防止超额创建用户
     * 使用AOP代理调用，确保缓存注解生效
     *
     * @param tenantId 租户ID
     * @return 是否还有余额
     */
    @Override
    public boolean checkAccountBalance(String tenantId) {
        // 使用AOP代理调用queryByTenantId，确保@Cacheable生效
        SysTenantVo tenant = SpringUtils.getAopProxy(this).queryByTenantId(tenantId);
        // 如果余额为-1代表不限制，直接返回true
        if (tenant.getAccountCount() == -1) {
            return true;
        }
        // 查询当前租户下的用户数量
        Long userNumber = userMapper.selectCount(new LambdaQueryWrapper<>());
        // 如果余额大于0代表还有可用名额，返回true
        return tenant.getAccountCount() - userNumber > 0;
    }

    /**
     * 校验有效期
     * 检查租户是否还在有效期内，防止过期租户继续使用
     * 使用AOP代理调用，确保缓存注解生效
     *
     * @param tenantId 租户ID
     * @return 是否还在有效期内
     */
    @Override
    public boolean checkExpireTime(String tenantId) {
        // 使用AOP代理调用queryByTenantId，确保@Cacheable生效
        SysTenantVo tenant = SpringUtils.getAopProxy(this).queryByTenantId(tenantId);
        // 如果未设置过期时间代表不限制，直接返回true
        if (ObjectUtil.isNull(tenant.getExpireTime())) {
            return true;
        }
        // 如果当前时间在过期时间之前则通过，返回true
        return new Date().before(tenant.getExpireTime());
    }

    /**
     * 同步租户套餐
     * 根据新的套餐更新租户角色的菜单权限
     * 管理员角色更新为新套餐的菜单，其他角色删除不在新套餐中的菜单
     *
     * @param tenantId 租户ID
     * @param packageId 套餐ID
     * @return 是否同步成功
     */
    @Override
    // Spring事务注解：确保方法在事务中执行，发生异常时回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncTenantPackage(String tenantId, Long packageId) {
        // 查询套餐信息
        SysTenantPackage tenantPackage = tenantPackageMapper.selectById(packageId);
        // 查询租户下的所有角色
        List<SysRole> roles = roleMapper.selectList(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getTenantId, tenantId));
        // 初始化其他角色ID列表（非管理员角色）
        List<Long> roleIds = new ArrayList<>(roles.size() - 1);
        // 将套餐菜单ID字符串转换为Long列表
        List<Long> menuIds = StringUtils.splitTo(tenantPackage.getMenuIds(), Convert::toLong);
        // 遍历角色列表
        roles.forEach(item -> {
            // 如果是租户管理员角色
            if (TenantConstants.TENANT_ADMIN_ROLE_KEY.equals(item.getRoleKey())) {
                // 创建新的角色菜单关联列表
                List<SysRoleMenu> roleMenus = new ArrayList<>(menuIds.size());
                // 遍历菜单ID，创建角色菜单关联对象
                menuIds.forEach(menuId -> {
                    SysRoleMenu roleMenu = new SysRoleMenu();
                    // 设置角色ID
                    roleMenu.setRoleId(item.getRoleId());
                    // 设置菜单ID
                    roleMenu.setMenuId(menuId);
                    // 添加到列表
                    roleMenus.add(roleMenu);
                });
                // 删除该角色的所有旧菜单权限
                roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, item.getRoleId()));
                // 批量插入新的角色菜单关联记录
                roleMenuMapper.insertBatch(roleMenus);
            } else {
                // 如果是其他角色，收集角色ID
                roleIds.add(item.getRoleId());
            }
        });
        // 如果存在其他角色
        if (!roleIds.isEmpty()) {
            // 删除这些角色不在新套餐中的菜单权限
            roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds).notIn(!menuIds.isEmpty(), SysRoleMenu::getMenuId, menuIds));
        }
        // 返回同步成功
        return true;
    }

    /**
     * 同步租户字典
     * 从默认租户同步字典类型和数据到所有租户
     * 使用TenantHelper.ignore忽略租户过滤，查询所有数据
     * 使用批量插入提高性能，最后清除缓存保证数据一致性
     */
    // Spring事务注解：确保方法在事务中执行，发生异常时回滚
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncTenantDict() {
        // 查询超管所有字典数据：使用TenantHelper.ignore忽略租户过滤
        List<SysDictType> dictTypeList = new ArrayList<>();
        List<SysDictData> dictDataList = new ArrayList<>();
        // 在忽略租户上下文中查询所有字典类型和数据
        TenantHelper.ignore(() -> {
            // 查询所有字典类型
            dictTypeList.addAll(dictTypeMapper.selectList());
            // 查询所有字典数据
            dictDataList.addAll(dictDataMapper.selectList());
        });
        // 所有租户字典类型：按租户ID分组
        Map<String, List<SysDictType>> dictTypeMap = StreamUtils.groupByKey(dictTypeList, TenantEntity::getTenantId);
        // 所有租户字典数据：按租户ID和字典类型分组
        Map<String, Map<String, List<SysDictData>>> dictDataMap = StreamUtils.groupBy2Key(dictDataList, TenantEntity::getTenantId, SysDictData::getDictType);

        // 默认租户字典类型列表
        List<SysDictType> defaultDictTypeList = dictTypeMap.get(TenantConstants.DEFAULT_TENANT_ID);
        // 默认租户字典数据
        Map<String, List<SysDictData>> defaultDictDataMap = dictDataMap.get(TenantConstants.DEFAULT_TENANT_ID);

        // 获取所有租户编号：只查询状态正常的租户
        List<String> tenantIds = baseMapper.selectObjs(
            new LambdaQueryWrapper<SysTenant>().select(SysTenant::getTenantId)
                .eq(SysTenant::getStatus, SystemConstants.NORMAL), x -> {
                // 使用Hutool的Convert将Object转换为String
                return Convert.toStr(x);
            });
        // 待入库的字典类型和字典数据
        List<SysDictType> saveTypeList = new ArrayList<>();
        List<SysDictData> saveDataList = new ArrayList<>();
        // 待同步的租户编号（用于清除对应租户的字典缓存）
        Set<String> syncTenantIds = new HashSet<>();
        // 循环所有租户，处理需要同步的数据
        for (String tenantId : tenantIds) {
            // 排除默认租户，不处理默认租户
            if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
                continue;
            }
            // 根据默认租户的字典类型进行数据同步
            for (SysDictType dictType : defaultDictTypeList) {
                // 获取当前租户的字典类型列表
                List<String> typeList = StreamUtils.toList(dictTypeMap.get(tenantId), SysDictType::getDictType);
                // 根据字典类型获取默认租户的字典数据
                List<SysDictData> defaultDictDataList = defaultDictDataMap.get(dictType.getDictType());
                // 排除不需要同步的字典数据：记录已存在的字典值
                Set<String> excludeDictDataSet = CollUtil.newHashSet();
                // 处理存在type不存在data的情况
                if (typeList.contains(dictType.getDictType())) {
                    // 获取租户字典数据
                    Optional.ofNullable(dictDataMap.get(tenantId))
                        // 获取租户当前字典类型的字典数据
                        .map(tenantDictDataMap -> tenantDictDataMap.get(dictType.getDictType()))
                        // 保存字典数据项的字典键值，用于判断数据是否需要同步
                        .map(data -> StreamUtils.toSet(data, SysDictData::getDictValue))
                        // 添加到排除集合中
                        .ifPresent(excludeDictDataSet::addAll);
                } else {
                    // 同步字典类型：当前租户没有该字典类型，需要新增
                    // 使用Hutool的BeanUtil复制对象属性
                    SysDictType type = BeanUtil.toBean(dictType, SysDictType.class);
                    // 设置字典ID为null，让数据库自动生成
                    type.setDictId(null);
                    // 设置新的租户ID
                    type.setTenantId(tenantId);
                    // 清空创建时间，由系统自动填充
                    type.setCreateTime(null);
                    // 清空更新时间
                    type.setUpdateTime(null);
                    // 记录需要清除缓存的租户
                    syncTenantIds.add(tenantId);
                    // 添加到待插入列表
                    saveTypeList.add(type);
                }

                // 默认租户字典数据不为空再去处理
                if (CollUtil.isNotEmpty(defaultDictDataList)) {
                    // 提前优化排除判断if条件语句，对于 && 并联条件，该优化可以避免不必要的 excludeDictDataSet.contains() 函数调用
                    boolean isExclude = CollUtil.isNotEmpty(excludeDictDataSet);
                    // 筛选出 dictType 对应的 data
                    for (SysDictData dictData : defaultDictDataList) {
                        // 排除不需要同步的字典数据：如果已存在则跳过
                        if (isExclude && excludeDictDataSet.contains(dictData.getDictValue())) {
                            continue;
                        }
                        // 复制字典数据对象
                        SysDictData data = BeanUtil.toBean(dictData, SysDictData.class);
                        // 设置字典编码为 null，让数据库自动生成
                        data.setDictCode(null);
                        // 设置新的租户ID
                        data.setTenantId(tenantId);
                        // 清空创建时间
                        data.setCreateTime(null);
                        // 清空更新时间
                        data.setUpdateTime(null);
                        // 清空创建部门
                        data.setCreateDept(null);
                        // 清空创建人
                        data.setCreateBy(null);
                        // 清空更新人
                        data.setUpdateBy(null);
                        // 记录需要清除缓存的租户
                        syncTenantIds.add(tenantId);
                        // 添加到待插入列表
                        saveDataList.add(data);
                    }
                }
            }
        }
        // 在忽略租户上下文中批量插入数据
        TenantHelper.ignore(() -> {
            // 批量插入字典类型
            if (CollUtil.isNotEmpty(saveTypeList)) {
                dictTypeMapper.insertBatch(saveTypeList);
            }
            // 批量插入字典数据
            if (CollUtil.isNotEmpty(saveDataList)) {
                dictDataMapper.insertBatch(saveDataList);
            }
        });
        // 清除所有受影响租户的字典缓存
        for (String tenantId : syncTenantIds) {
            // 在指定租户上下文中清除缓存
            TenantHelper.dynamic(tenantId, () -> CacheUtils.clear(CacheNames.SYS_DICT));
        }
    }

    /**
     * 同步租户参数配置
     * 从默认租户同步参数配置到所有租户
     * 使用TenantHelper.ignore忽略租户过滤，查询所有数据
     * 使用批量插入提高性能，最后清除缓存保证数据一致性
     */
    // Spring事务注解：确保方法在事务中执行，发生异常时回滚
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncTenantConfig() {
        // 查询超管所有参数配置：使用TenantHelper.ignore忽略租户过滤
        List<SysConfig> configList = TenantHelper.ignore(() -> configMapper.selectList());

        // 所有租户参数配置：按租户ID分组
        Map<String, List<SysConfig>> configMap = StreamUtils.groupByKey(configList, TenantEntity::getTenantId);

        // 默认租户参数配置列表
        List<SysConfig> defaultConfigList = configMap.get(TenantConstants.DEFAULT_TENANT_ID);

        // 获取所有租户编号：只查询状态正常的租户
        List<String> tenantIds = baseMapper.selectObjs(
            new LambdaQueryWrapper<SysTenant>().select(SysTenant::getTenantId)
                .eq(SysTenant::getStatus, SystemConstants.NORMAL), x -> {
                // 使用Hutool的Convert将Object转换为String
                return Convert.toStr(x);
            });
        // 待入库的参数配置
        List<SysConfig> saveConfigList = new ArrayList<>();
        // 待同步的租户编号（用于清除对应租户的配置缓存）
        Set<String> syncTenantIds = new HashSet<>();
        // 循环所有租户，处理需要同步的数据
        for (String tenantId : tenantIds) {
            // 排除默认租户，不处理默认租户
            if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
                continue;
            }
            // 根据默认租户的参数配置进行数据同步
            for (SysConfig config : defaultConfigList) {
                // 获取当前租户的参数配置键列表
                List<String> typeList = StreamUtils.toList(configMap.get(tenantId), SysConfig::getConfigKey);
                // 如果当前租户没有该配置键，则需要同步
                if (!typeList.contains(config.getConfigKey())) {
                    // 复制配置对象
                    SysConfig type = BeanUtil.toBean(config, SysConfig.class);
                    // 设置配置ID为null，让数据库自动生成
                    type.setConfigId(null);
                    // 设置新的租户ID
                    type.setTenantId(tenantId);
                    // 清空创建时间，由系统自动填充
                    type.setCreateTime(null);
                    // 清空更新时间
                    type.setUpdateTime(null);
                    // 记录需要清除缓存的租户
                    syncTenantIds.add(tenantId);
                    // 添加到待插入列表
                    saveConfigList.add(type);
                }
            }
        }
        // 在忽略租户上下文中批量插入数据
        TenantHelper.ignore(() -> {
            // 批量插入参数配置
            if (CollUtil.isNotEmpty(saveConfigList)) {
                configMapper.insertBatch(saveConfigList);
            }
        });
        // 清除所有受影响租户的配置缓存
        for (String tenantId : syncTenantIds) {
            // 在指定租户上下文中清除缓存
            TenantHelper.dynamic(tenantId, () -> CacheUtils.clear(CacheNames.SYS_CONFIG));
        }
    }

}
