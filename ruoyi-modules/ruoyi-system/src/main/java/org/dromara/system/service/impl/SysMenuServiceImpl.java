// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

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
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：通用常量定义
import org.dromara.common.core.constant.Constants;
// 公共核心常量：系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共核心工具类：树构建工具
import org.dromara.common.core.utils.TreeBuildUtils;
// Sa-Token工具类：登录助手，提供获取当前登录用户信息的方法
import org.dromara.common.satoken.utils.LoginHelper;
// 系统领域模型：菜单实体类
import org.dromara.system.domain.SysMenu;
// 系统领域模型：角色实体类
import org.dromara.system.domain.SysRole;
// 系统领域模型：角色菜单关联实体类
import org.dromara.system.domain.SysRoleMenu;
// 系统领域模型：租户套餐实体类
import org.dromara.system.domain.SysTenantPackage;
// 系统业务对象：菜单业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysMenuBo;
// 系统视图对象：路由元信息视图对象
import org.dromara.system.domain.vo.MetaVo;
// 系统视图对象：路由视图对象
import org.dromara.system.domain.vo.RouterVo;
// 系统视图对象：菜单视图对象
import org.dromara.system.domain.vo.SysMenuVo;
// 系统Mapper接口：菜单Mapper
import org.dromara.system.mapper.SysMenuMapper;
// 系统Mapper接口：角色Mapper
import org.dromara.system.mapper.SysRoleMapper;
// 系统Mapper接口：角色菜单关联Mapper
import org.dromara.system.mapper.SysRoleMenuMapper;
// 系统Mapper接口：租户套餐Mapper
import org.dromara.system.mapper.SysTenantPackageMapper;
// 系统服务接口：菜单服务接口
import org.dromara.system.service.ISysMenuService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;

// Java集合工具类：提供集合操作
import java.util.*;

/**
 * 菜单管理服务实现类
 * 实现菜单管理的核心业务逻辑，包括菜单CRUD、权限校验、路由构建、树形结构等
 * 同时处理角色菜单关联、租户套餐菜单关联等复杂业务
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysMenuServiceImpl implements ISysMenuService {

    // 菜单Mapper，用于菜单数据的持久化操作
    private final SysMenuMapper baseMapper;
    // 角色Mapper，用于角色数据查询
    private final SysRoleMapper roleMapper;
    // 角色菜单关联Mapper，用于角色菜单关系操作
    private final SysRoleMenuMapper roleMenuMapper;
    // 租户套餐Mapper，用于租户套餐数据查询
    private final SysTenantPackageMapper tenantPackageMapper;

    /**
     * 根据用户ID查询系统菜单列表
     * 查询指定用户可见的所有菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuVo> selectMenuList(Long userId) {
        // 调用重载方法，传入空的查询条件
        return selectMenuList(new SysMenuBo(), userId);
    }

    /**
     * 查询系统菜单列表
     * 根据查询条件和用户ID查询菜单列表，支持权限过滤
     *
     * @param menu 菜单查询条件
     * @param userId 用户ID，用于权限过滤
     * @return 菜单列表
     */
    @Override
    public List<SysMenuVo> selectMenuList(SysMenuBo menu, Long userId) {
        // 菜单列表
        List<SysMenuVo> menuList;
        // 创建LambdaQueryWrapper，使用Lambda表达式，类型安全
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        // 管理员显示所有菜单信息，不是管理员按用户id过滤菜单
        if (!LoginHelper.isSuperAdmin(userId)) {
            // 通过用户id获取角色id，通过角色id获取菜单id，然后in菜单
            // 使用inSql方法，执行子查询：SELECT menu_id FROM sys_role_menu WHERE role_id IN (SELECT role_id FROM sys_user_role WHERE user_id = ?)
            wrapper.inSql(SysMenu::getMenuId, baseMapper.buildMenuByUserSql(userId));
        }
        // 调用Mapper查询菜单VO列表，构建查询条件
        menuList = baseMapper.selectVoList(
            wrapper.like(StringUtils.isNotBlank(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName()) // 模糊查询菜单名称
                .eq(StringUtils.isNotBlank(menu.getVisible()), SysMenu::getVisible, menu.getVisible()) // 精确查询可见性
                .eq(StringUtils.isNotBlank(menu.getStatus()), SysMenu::getStatus, menu.getStatus()) // 精确查询状态
                .eq(StringUtils.isNotBlank(menu.getMenuType()), SysMenu::getMenuType, menu.getMenuType()) // 精确查询菜单类型
                .eq(ObjectUtil.isNotNull(menu.getParentId()), SysMenu::getParentId, menu.getParentId()) // 精确查询父菜单ID
                .orderByAsc(SysMenu::getParentId) // 按父菜单ID升序排序
                .orderByAsc(SysMenu::getOrderNum)); // 按排序号升序排序
        return menuList;
    }

    /**
     * 根据用户ID查询权限
     * 查询指定用户拥有的所有菜单权限标识（如：system:user:add）
     *
     * @param userId 用户ID
     * @return 权限列表（Set集合，自动去重）
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        // 调用Mapper查询用户权限，返回权限标识集合
        return baseMapper.selectMenuPermsByUserId(userId);
    }

    /**
     * 根据角色ID查询权限
     * 查询指定角色拥有的所有菜单权限标识
     *
     * @param roleId 角色ID
     * @return 权限列表（Set集合，自动去重）
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId) {
        // 调用Mapper查询角色权限，返回权限标识集合
        return baseMapper.selectMenuPermsByRoleId(roleId);
    }

    /**
     * 根据用户ID查询菜单树
     * 查询指定用户可见的菜单树形结构，用于前端展示
     *
     * @param userId 用户ID
     * @return 菜单列表（树形结构）
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        // 菜单列表
        List<SysMenu> menus;
        // 如果是超级管理员，查询所有菜单
        if (LoginHelper.isSuperAdmin(userId)) {
            menus = baseMapper.selectMenuTreeAll();
        } else {
            // 非超级管理员，查询用户有权限的菜单
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            menus = baseMapper.selectList(
                wrapper.in(SysMenu::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU) // 只查询目录和菜单类型
                    .eq(SysMenu::getStatus, SystemConstants.NORMAL) // 状态为正常
                    .inSql(SysMenu::getMenuId, baseMapper.buildMenuByUserSql(userId)) // 在用户权限范围内
                    .orderByAsc(SysMenu::getParentId) // 按父菜单ID升序排序
                    .orderByAsc(SysMenu::getOrderNum)); // 按排序号升序排序
        }
        // 将菜单列表转换为树形结构，从根节点开始
        return getChildPerms(menus, Constants.TOP_PARENT_ID);
    }

    /**
     * 根据角色ID查询菜单树信息
     * 查询角色已分配的菜单ID列表，支持父子联动/不联动模式
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        // 查询角色信息
        SysRole role = roleMapper.selectById(roleId);
        // 调用Mapper查询角色已分配的菜单ID列表
        return baseMapper.selectMenuListByRoleId(roleId, role.getMenuCheckStrictly());
    }

    /**
     * 根据租户套餐ID查询菜单树信息
     * 查询租户套餐已分配的菜单ID列表，支持父子联动/不联动模式
     *
     * @param packageId 租户套餐ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByPackageId(Long packageId) {
        // 查询租户套餐信息
        SysTenantPackage tenantPackage = tenantPackageMapper.selectById(packageId);
        // 将逗号分隔的菜单ID字符串转换为Long列表
        List<Long> menuIds = StringUtils.splitTo(tenantPackage.getMenuIds(), Convert::toLong);
        // 如果菜单ID列表为空，返回空列表
        if (CollUtil.isEmpty(menuIds)) {
            return List.of();
        }
        // 父菜单ID列表
        List<Long> parentIds = null;
        // 如果开启父子联动，需要排除父菜单ID
        if (tenantPackage.getMenuCheckStrictly()) {
            // 查询所有菜单的父菜单ID
            parentIds = baseMapper.selectObjs(new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getParentId) // 只查询父菜单ID
                .in(SysMenu::getMenuId, menuIds), x -> { // 在指定菜单ID范围内
                return Convert.toLong(x); // 转换为Long类型
            });
        }
        // 查询最终的菜单ID列表，排除父菜单ID（如果开启父子联动）
        return baseMapper.selectObjs(new LambdaQueryWrapper<SysMenu>()
            .select(SysMenu::getMenuId) // 只查询菜单ID
            .in(SysMenu::getMenuId, menuIds) // 在指定菜单ID范围内
            .notIn(CollUtil.isNotEmpty(parentIds), SysMenu::getMenuId, parentIds), x -> { // 排除父菜单ID
            return Convert.toLong(x); // 转换为Long类型
        });
    }

    /**
     * 构建前端路由所需要的菜单
     * 将菜单列表转换为前端Vue路由格式，支持多种菜单类型（目录、菜单、按钮、外链、内链）
     * 路由name命名规则：path首字母转大写 + id
     *
     * @param menus 菜单列表（树形结构）
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        // 创建路由列表（LinkedList，支持高效插入）
        List<RouterVo> routers = new LinkedList<>();
        // 遍历菜单列表
        for (SysMenu menu : menus) {
            // 路由名称：路由名称 + 菜单ID（确保唯一性）
            String name = menu.getRouteName() + menu.getMenuId();
            // 创建路由对象
            RouterVo router = new RouterVo();
            // 设置是否隐藏（visible为1时隐藏）
            router.setHidden("1".equals(menu.getVisible()));
            // 设置路由名称
            router.setName(name);
            // 设置路由路径
            router.setPath(menu.getRouterPath());
            // 设置组件路径
            router.setComponent(menu.getComponentInfo());
            // 设置查询参数
            router.setQuery(menu.getQueryParam());
            // 设置路由元信息（标题、图标、是否缓存、路径、备注）
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath(), menu.getRemark()));
            // 获取子菜单列表
            List<SysMenu> cMenus = menu.getChildren();
            // 如果有子菜单且菜单类型为目录
            if (CollUtil.isNotEmpty(cMenus) && SystemConstants.TYPE_DIR.equals(menu.getMenuType())) {
                // 设置总是显示
                router.setAlwaysShow(true);
                // 设置重定向为noRedirect（不自动重定向）
                router.setRedirect("noRedirect");
                // 递归构建子路由
                router.setChildren(buildMenus(cMenus));
            } else if (menu.isMenuFrame()) { // 如果是菜单框架（外链）
                // 框架名称：path首字母大写 + 菜单ID
                String frameName = StringUtils.capitalize(menu.getPath()) + menu.getMenuId();
                // 设置元信息为null（框架菜单不显示元信息）
                router.setMeta(null);
                // 创建子路由列表
                List<RouterVo> childrenList = new ArrayList<>();
                // 创建子路由对象
                RouterVo children = new RouterVo();
                // 设置子路由路径
                children.setPath(menu.getPath());
                // 设置子路由组件
                children.setComponent(menu.getComponent());
                // 设置子路由名称
                children.setName(frameName);
                // 设置子路由元信息
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath(), menu.getRemark()));
                // 设置子路由查询参数
                children.setQuery(menu.getQueryParam());
                // 添加到子路由列表
                childrenList.add(children);
                // 设置子路由
                router.setChildren(childrenList);
            } else if (menu.getParentId().equals(Constants.TOP_PARENT_ID) && menu.isInnerLink()) { // 如果是内链且为顶级菜单
                // 设置元信息（标题、图标）
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                // 设置路径为根路径
                router.setPath("/");
                // 创建子路由列表
                List<RouterVo> childrenList = new ArrayList<>();
                // 创建子路由对象
                RouterVo children = new RouterVo();
                // 处理内链路径（替换特殊字符）
                String routerPath = SysMenu.innerLinkReplaceEach(menu.getPath());
                // 内链名称：path首字母大写 + 菜单ID
                String innerLinkName = StringUtils.capitalize(routerPath) + menu.getMenuId();
                // 设置子路由路径
                children.setPath(routerPath);
                // 设置子路由组件为内链组件
                children.setComponent(SystemConstants.INNER_LINK);
                // 设置子路由名称
                children.setName(innerLinkName);
                // 设置子路由元信息（标题、图标、原始路径）
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                // 添加到子路由列表
                childrenList.add(children);
                // 设置子路由
                router.setChildren(childrenList);
            }
            // 添加到路由列表
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildMenuTreeSelect(List<SysMenuVo> menus) {
        if (CollUtil.isEmpty(menus)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.build(menus, (menu, tree) -> {
            Tree<Long> menuTree = tree.setId(menu.getMenuId())
                .setParentId(menu.getParentId())
                .setName(menu.getMenuName())
                .setWeight(menu.getOrderNum());
            menuTree.put("menuType", menu.getMenuType());
            menuTree.put("icon", menu.getIcon());
            menuTree.put("visible", menu.getVisible());
            menuTree.put("status", menu.getStatus());
        });
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenuVo selectMenuById(Long menuId) {
        return baseMapper.selectVoById(menuId);
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return baseMapper.exists(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuIds 菜单ID串
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(List<Long> menuIds) {
        return baseMapper.exists(new LambdaQueryWrapper<SysMenu>().in(SysMenu::getParentId, menuIds).notIn(SysMenu::getMenuId, menuIds));
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        return roleMenuMapper.exists(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId));
    }

    /**
     * 新增保存菜单信息
     *
     * @param bo 菜单信息
     * @return 结果
     */
    @Override
    public int insertMenu(SysMenuBo bo) {
        SysMenu menu = MapstructUtils.convert(bo, SysMenu.class);
        return baseMapper.insert(menu);
    }

    /**
     * 修改保存菜单信息
     *
     * @param bo 菜单信息
     * @return 结果
     */
    @Override
    public int updateMenu(SysMenuBo bo) {
        SysMenu menu = MapstructUtils.convert(bo, SysMenu.class);
        return baseMapper.updateById(menu);
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public int deleteMenuById(Long menuId) {
        return baseMapper.deleteById(menuId);
    }

    /**
     * 批量删除菜单管理信息
     *
     * @param menuIds 菜单ID串
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuById(List<Long> menuIds) {
        baseMapper.deleteByIds(menuIds);
        roleMenuMapper.deleteByMenuIds(menuIds);
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenuBo menu) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getMenuName, menu.getMenuName())
            .eq(SysMenu::getParentId, menu.getParentId())
            .ne(ObjectUtil.isNotNull(menu.getMenuId()), SysMenu::getMenuId, menu.getMenuId()));
        return !exist;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    private List<SysMenu> getChildPerms(List<SysMenu> list, Long parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (SysMenu t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId().equals(parentId)) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = StreamUtils.filter(list, n -> n.getParentId().equals(t.getMenuId()));
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            // 判断是否有子节点
            if (list.stream().anyMatch(n -> n.getParentId().equals(tChild.getMenuId()))) {
                recursionFn(list, tChild);
            }
        }
    }

}
