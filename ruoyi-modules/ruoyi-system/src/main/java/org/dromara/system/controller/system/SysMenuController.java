// 菜单管理控制器，提供菜单的CRUD、路由构建、树形结构、角色和租户套餐菜单配置功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Sa-Token角色校验注解
import cn.dev33.satoken.annotation.SaCheckRole;
// Sa-Token校验模式枚举，OR表示满足任一角色即可
import cn.dev33.satoken.annotation.SaMode;
// Hutool树形结构工具类
import cn.hutool.core.lang.tree.Tree;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 租户常量定义
import org.dromara.common.core.constant.TenantConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 防重复提交注解
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// Sa-Token登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 菜单实体类
import org.dromara.system.domain.SysMenu;
// 菜单业务对象
import org.dromara.system.domain.bo.SysMenuBo;
// 路由视图对象
import org.dromara.system.domain.vo.RouterVo;
// 菜单视图对象
import org.dromara.system.domain.vo.SysMenuVo;
// 菜单服务接口
import org.dromara.system.service.ISysMenuService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单管理控制器
 * 提供菜单的CRUD、路由构建、树形结构、角色和租户套餐菜单配置功能
 * 继承BaseController获取通用响应方法
 *
 * @author Lion Li
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/system/menu
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {

    // 菜单服务接口，自动注入
    private final ISysMenuService menuService;

    /**
     * 获取路由信息
     * 根据当前登录用户ID获取其有权限访问的菜单，并构建为前端路由格式
     *
     * @return 路由信息列表
     */
    // GET请求映射，路径为/system/menu/getRouters
    @GetMapping("/getRouters")
    public R<List<RouterVo>> getRouters() {
        // 调用服务层根据用户ID查询菜单树
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(LoginHelper.getUserId());
        // 调用服务层将菜单列表构建为路由格式并返回
        return R.ok(menuService.buildMenus(menus));
    }

    /**
     * 获取菜单列表
     * 查询菜单列表，需要超级管理员或租户管理员角色
     */
    // Sa-Token角色校验，需要超级管理员或租户管理员角色，OR模式表示满足任一即可
    @SaCheckRole(value = {
        TenantConstants.SUPER_ADMIN_ROLE_KEY,
        TenantConstants.TENANT_ADMIN_ROLE_KEY
    }, mode = SaMode.OR)
    // Sa-Token权限校验，需要system:menu:list权限
    @SaCheckPermission("system:menu:list")
    // GET请求映射，路径为/system/menu/list
    @GetMapping("/list")
    public R<List<SysMenuVo>> list(SysMenuBo menu) {
        // 调用服务层查询菜单列表，传入当前用户ID进行数据权限过滤
        List<SysMenuVo> menus = menuService.selectMenuList(menu, LoginHelper.getUserId());
        // 返回菜单列表
        return R.ok(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     * 根据主键ID查询菜单详情，需要超级管理员或租户管理员角色
     *
     * @param menuId 菜单ID
     */
    // Sa-Token角色校验，需要超级管理员或租户管理员角色
    @SaCheckRole(value = {
        TenantConstants.SUPER_ADMIN_ROLE_KEY,
        TenantConstants.TENANT_ADMIN_ROLE_KEY
    }, mode = SaMode.OR)
    // Sa-Token权限校验，需要system:menu:query权限
    @SaCheckPermission("system:menu:query")
    // GET请求映射，路径为/system/menu/{menuId}
    @GetMapping(value = "/{menuId}")
    public R<SysMenuVo> getInfo(@PathVariable Long menuId) {
        // 调用服务层查询菜单详情并返回
        return R.ok(menuService.selectMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     * 查询菜单列表并构建为树形结构，用于前端下拉选择
     */
    // Sa-Token权限校验，需要system:menu:query权限
    @SaCheckPermission("system:menu:query")
    // GET请求映射，路径为/system/menu/treeselect
    @GetMapping("/treeselect")
    public R<List<Tree<Long>>> treeselect(SysMenuBo menu) {
        // 调用服务层查询菜单列表，传入当前用户ID进行数据权限过滤
        List<SysMenuVo> menus = menuService.selectMenuList(menu, LoginHelper.getUserId());
        // 调用服务层将菜单列表构建为树形选择结构并返回
        return R.ok(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     * 查询角色已分配的菜单和全部菜单树，用于角色菜单配置页面
     *
     * @param roleId 角色ID
     */
    // Sa-Token权限校验，需要system:menu:query权限
    @SaCheckPermission("system:menu:query")
    // GET请求映射，路径为/system/menu/roleMenuTreeselect/{roleId}
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public R<MenuTreeSelectVo> roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        // 调用服务层查询当前用户有权限的菜单列表
        List<SysMenuVo> menus = menuService.selectMenuList(LoginHelper.getUserId());
        // 构建角色菜单树选择VO，包含角色已选菜单和全部菜单树
        MenuTreeSelectVo selectVo = new MenuTreeSelectVo(
            // 查询角色已分配的菜单ID列表
            menuService.selectMenuListByRoleId(roleId),
            // 构建全部菜单树
            menuService.buildMenuTreeSelect(menus));
        // 返回角色菜单树选择信息
        return R.ok(selectVo);
    }

    /**
     * 加载对应租户套餐菜单列表树
     * 查询租户套餐已分配的菜单和全部菜单树（排除租户管理菜单），用于租户套餐配置页面
     *
     * @param packageId 租户套餐ID
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:menu:query权限
    @SaCheckPermission("system:menu:query")
    // GET请求映射，路径为/system/menu/tenantPackageMenuTreeselect/{packageId}
    @GetMapping(value = "/tenantPackageMenuTreeselect/{packageId}")
    public R<MenuTreeSelectVo> tenantPackageMenuTreeselect(@PathVariable("packageId") Long packageId) {
        // 调用服务层查询当前用户有权限的菜单列表
        List<SysMenuVo> menus = menuService.selectMenuList(LoginHelper.getUserId());
        // 调用服务层构建菜单树选择结构
        List<Tree<Long>> list = menuService.buildMenuTreeSelect(menus);
        // 删除租户管理菜单（ID为6），租户套餐不应包含租户管理功能
        list.removeIf(menu -> menu.getId() == 6L);
        // 创建套餐已选菜单ID列表
        List<Long> ids = new ArrayList<>();
        // 如果套餐ID大于0，查询该套餐已分配的菜单ID列表
        if (packageId > 0L) {
            ids = menuService.selectMenuListByPackageId(packageId);
        }
        // 构建租户套餐菜单树选择VO
        MenuTreeSelectVo selectVo = new MenuTreeSelectVo(ids, list);
        // 返回租户套餐菜单树选择信息
        return R.ok(selectVo);
    }

    /**
     * 新增菜单
     * 添加新的菜单，需要超级管理员角色，校验菜单名称唯一性和外链地址格式
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:menu:add权限
    @SaCheckPermission("system:menu:add")
    // 操作日志注解，记录业务操作，标题为"菜单管理"，类型为新增
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/menu
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysMenuBo menu) {
        // 校验菜单名称是否唯一，如果不唯一返回错误信息
        if (!menuService.checkMenuNameUnique(menu)) {
            return R.fail("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (SystemConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            // 如果是外链菜单，校验地址是否以http(s)://开头
            return R.fail("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        // 调用服务层新增菜单，并返回操作结果
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     * 更新菜单信息，需要超级管理员角色，校验菜单名称唯一性、外链地址格式和上级菜单不能是自己
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:menu:edit权限
    @SaCheckPermission("system:menu:edit")
    // 操作日志注解，记录业务操作，标题为"菜单管理"，类型为更新
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/menu
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysMenuBo menu) {
        // 校验菜单名称是否唯一（排除当前记录），如果不唯一返回错误信息
        if (!menuService.checkMenuNameUnique(menu)) {
            return R.fail("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (SystemConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            // 如果是外链菜单，校验地址是否以http(s)://开头
            return R.fail("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            // 校验上级菜单不能是自己
            return R.fail("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        // 调用服务层更新菜单信息，并返回操作结果
        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     * 删除菜单及其子菜单，需要超级管理员角色，检查是否存在子菜单和角色关联
     *
     * @param menuId 菜单ID
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:menu:remove权限
    @SaCheckPermission("system:menu:remove")
    // 操作日志注解，记录业务操作，标题为"菜单管理"，类型为删除
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/menu/{menuId}
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable("menuId") Long menuId) {
        // 检查是否存在子菜单
        if (menuService.hasChildByMenuId(menuId)) {
            return R.warn("存在子菜单,不允许删除");
        }
        // 检查菜单是否已分配给角色
        if (menuService.checkMenuExistRole(menuId)) {
            return R.warn("菜单已分配,不允许删除");
        }
        // 调用服务层删除菜单，并返回操作结果
        return toAjax(menuService.deleteMenuById(menuId));
    }

    /**
     * 角色菜单列表树信息VO
     * 使用Java Record定义不可变的数据传输对象
     * 包含已选菜单ID列表和菜单树结构
     *
     * @param checkedKeys 角色已选中的菜单ID列表
     * @param menus       菜单树结构列表，用于前端展示
     */
    // Java Record定义，自动生成构造函数、equals、hashCode、toString方法
    public record MenuTreeSelectVo(List<Long> checkedKeys, List<Tree<Long>> menus) {
    }

    /**
     * 批量级联删除菜单
     * 批量删除菜单及其子菜单，需要超级管理员角色，检查是否存在子菜单
     *
     * @param menuIds 菜单ID数组
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:menu:remove权限
    @SaCheckPermission("system:menu:remove")
    // 操作日志注解，记录业务操作，标题为"菜单管理"，类型为删除
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/menu/cascade/{menuIds}，支持多个id用逗号分隔
    @DeleteMapping("/cascade/{menuIds}")
    public R<Void> remove(@PathVariable("menuIds") Long[] menuIds) {
        // 将数组转换为列表
        List<Long> menuIdList = List.of(menuIds);
        // 检查是否存在子菜单
        if (menuService.hasChildByMenuId(menuIdList)) {
            return R.warn("存在子菜单,不允许删除");
        }
        // 调用服务层批量删除菜单
        menuService.deleteMenuById(menuIdList);
        // 返回成功响应
        return R.ok();
    }

}
