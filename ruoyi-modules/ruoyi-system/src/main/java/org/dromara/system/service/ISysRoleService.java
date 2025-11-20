// 角色业务层接口，定义角色管理相关的业务方法
package org.dromara.system.service;

// MyBatis-Plus分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 用户角色关联实体
import org.dromara.system.domain.SysUserRole;
// 角色业务对象
import org.dromara.system.domain.bo.SysRoleBo;
// 角色视图对象
import org.dromara.system.domain.vo.SysRoleVo;

// Java集合类
import java.util.List;
import java.util.Set;

/**
 * 角色业务层接口
 * 定义角色管理相关的业务方法，包括角色查询、新增、修改、删除、授权、数据权限等操作
 *
 * @author Lion Li
 */
public interface ISysRoleService {

    /**
     * 分页查询角色列表
     * 支持按角色名称、角色权限、状态等条件筛选，并分页返回结果
     *
     * @param role      角色查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 角色分页列表
     */
    TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo role, PageQuery pageQuery);

    /**
     * 根据条件查询角色数据
     * 查询所有符合条件的角色列表，不分页
     *
     * @param role 角色信息查询条件
     * @return 角色数据集合信息
     */
    List<SysRoleVo> selectRoleList(SysRoleBo role);

    /**
     * 根据用户ID查询角色列表
     * 查询指定用户已分配的所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRoleVo> selectRolesByUserId(Long userId);

    /**
     * 根据用户ID查询角色列表(包含被授权状态)
     * 查询所有角色，并标记指定用户已授权的角色
     *
     * @param userId 用户ID
     * @return 角色列表（包含是否授权状态）
     */
    List<SysRoleVo> selectRolesAuthByUserId(Long userId);

    /**
     * 根据用户ID查询角色权限
     * 查询用户拥有的所有角色权限字符串（如：admin、common）
     *
     * @param userId 用户ID
     * @return 权限列表（角色权限字符串集合）
     */
    Set<String> selectRolePermissionByUserId(Long userId);

    /**
     * 查询所有角色
     * 查询系统中所有角色列表
     *
     * @return 角色列表
     */
    List<SysRoleVo> selectRoleAll();

    /**
     * 根据用户ID获取角色选择框列表
     * 查询指定用户已分配的角色ID列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    List<Long> selectRoleListByUserId(Long userId);

    /**
     * 通过角色ID查询角色
     * 根据主键ID查询角色详细信息
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    SysRoleVo selectRoleById(Long roleId);

    /**
     * 通过角色ID串查询角色
     * 根据角色ID列表查询多个角色信息
     *
     * @param roleIds 角色ID列表
     * @return 角色列表信息
     */
    List<SysRoleVo> selectRoleByIds(List<Long> roleIds);

    /**
     * 校验角色名称是否唯一
     * 检查角色名称在数据库中是否已存在，用于新增和修改时的唯一性校验
     *
     * @param role 角色信息（包含角色ID和角色名称）
     * @return true-唯一，false-不唯一
     */
    boolean checkRoleNameUnique(SysRoleBo role);

    /**
     * 校验角色权限是否唯一
     * 检查角色权限字符串在数据库中是否已存在，用于新增和修改时的唯一性校验
     *
     * @param role 角色信息（包含角色ID和角色权限字符串）
     * @return true-唯一，false-不唯一
     */
    boolean checkRoleKeyUnique(SysRoleBo role);

    /**
     * 校验角色是否允许操作
     * 检查是否为内置管理员角色（如admin），防止误删或修改系统内置角色
     *
     * @param role 角色信息
     */
    void checkRoleAllowed(SysRoleBo role);

    /**
     * 校验角色是否有数据权限
     * 检查当前登录用户是否有权限操作指定角色的数据（数据权限范围控制）
     *
     * @param roleId 角色id
     */
    void checkRoleDataScope(Long roleId);

    /**
     * 校验角色是否有数据权限
     * 批量检查多个角色的数据权限
     *
     * @param roleIds 角色ID列表（支持传单个ID）
     */
    void checkRoleDataScope(List<Long> roleIds);

    /**
     * 通过角色ID查询角色使用数量
     * 统计指定角色已分配的用户数量
     *
     * @param roleId 角色ID
     * @return 使用该角色的用户数量
     */
    long countUserRoleByRoleId(Long roleId);

    /**
     * 新增保存角色信息
     * 添加新角色到数据库，包含角色基本信息、菜单权限、数据权限
     *
     * @param bo 角色信息
     * @return 影响的行数
     */
    int insertRole(SysRoleBo bo);

    /**
     * 修改保存角色信息
     * 更新角色基本信息、菜单权限、数据权限
     *
     * @param bo 角色信息
     * @return 影响的行数
     */
    int updateRole(SysRoleBo bo);

    /**
     * 修改角色状态
     * 启用或禁用角色
     *
     * @param roleId 角色ID
     * @param status 角色状态（0-正常，1-停用）
     * @return 影响的行数
     */
    int updateRoleStatus(Long roleId, String status);

    /**
     * 修改数据权限信息
     * 配置角色的数据权限范围（全部、自定义、本部门、本部门及子部门、仅本人）
     *
     * @param bo 角色信息
     * @return 影响的行数
     */
    int authDataScope(SysRoleBo bo);

    /**
     * 通过角色ID删除角色
     * 删除单个角色及其关联的菜单、部门、用户关系
     *
     * @param roleId 角色ID
     * @return 影响的行数
     */
    int deleteRoleById(Long roleId);

    /**
     * 批量删除角色信息
     * 批量删除角色及其关联的菜单、部门、用户关系
     *
     * @param roleIds 需要删除的角色ID列表
     * @return 影响的行数
     */
    int deleteRoleByIds(List<Long> roleIds);

    /**
     * 取消授权用户角色
     * 取消单个用户的角色授权
     *
     * @param userRole 用户和角色关联信息（包含userId和roleId）
     * @return 影响的行数
     */
    int deleteAuthUser(SysUserRole userRole);

    /**
     * 批量取消授权用户角色
     * 批量取消多个用户的角色授权
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户ID数组
     * @return 影响的行数
     */
    int deleteAuthUsers(Long roleId, Long[] userIds);

    /**
     * 批量选择授权用户角色
     * 批量为多个用户分配角色
     *
     * @param roleId  角色ID
     * @param userIds 需要授权的用户ID数组
     * @return 影响的行数
     */
    int insertAuthUsers(Long roleId, Long[] userIds);

    /**
     * 根据角色ID清除该角色关联的所有在线用户的登录状态（踢出在线用户）
     *
     * <p>
     * 先判断角色是否绑定用户，若无绑定则直接返回
     * 然后遍历当前所有在线Token，查找拥有该角色的用户并强制登出
     * 注意：在线用户量过大时，操作可能导致 Redis 阻塞，需谨慎调用
     * </p>
     *
     * @param roleId 角色ID
     */
    void cleanOnlineUserByRole(Long roleId);

    /**
     * 根据用户ID列表清除对应在线用户的登录状态（踢出指定用户）
     *
     * <p>
     * 遍历当前所有在线Token，匹配用户ID列表中的用户，强制登出
     * 注意：在线用户量过大时，操作可能导致 Redis 阻塞，需谨慎调用
     * </p>
     *
     * @param userIds 需要清除的用户ID列表
     */
    void cleanOnlineUser(List<Long> userIds);

}
