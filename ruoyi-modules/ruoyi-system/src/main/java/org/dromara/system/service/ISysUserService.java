// 用户业务层接口，定义用户管理相关的业务方法
package org.dromara.system.service;

// MyBatis-Plus分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 用户业务对象
import org.dromara.system.domain.bo.SysUserBo;
// 用户导出视图对象
import org.dromara.system.domain.vo.SysUserExportVo;
// 用户视图对象
import org.dromara.system.domain.vo.SysUserVo;

// Java集合类
import java.util.List;

/**
 * 用户业务层接口
 * 定义用户管理相关的业务方法，包括用户查询、新增、修改、删除、授权等操作
 *
 * @author Lion Li
 */
public interface ISysUserService {


    /**
     * 根据条件分页查询用户列表
     * 支持按用户名、手机号、邮箱、部门等条件筛选，并分页返回结果
     *
     * @param user      用户信息查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 分页后的用户信息列表
     */
    TableDataInfo<SysUserVo> selectPageUserList(SysUserBo user, PageQuery pageQuery);

    /**
     * 导出用户列表
     * 根据查询条件导出所有符合条件的用户数据，用于Excel导出
     *
     * @param user 用户信息查询条件
     * @return 用户导出信息集合
     */
    List<SysUserExportVo> selectUserExportList(SysUserBo user);

    /**
     * 根据条件分页查询已分配用户角色列表
     * 查询已分配指定角色的用户列表，支持分页和条件筛选
     *
     * @param user      用户信息查询条件
     * @param pageQuery 分页参数
     * @return 已分配角色的用户信息集合
     */
    TableDataInfo<SysUserVo> selectAllocatedList(SysUserBo user, PageQuery pageQuery);

    /**
     * 根据条件分页查询未分配用户角色列表
     * 查询未分配指定角色的用户列表，支持分页和条件筛选
     *
     * @param user      用户信息查询条件
     * @param pageQuery 分页参数
     * @return 未分配角色的用户信息集合
     */
    TableDataInfo<SysUserVo> selectUnallocatedList(SysUserBo user, PageQuery pageQuery);

    /**
     * 通过用户名查询用户
     * 根据登录账号查询用户详细信息，用于登录验证
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUserVo selectUserByUserName(String userName);

    /**
     * 通过手机号查询用户
     * 根据手机号查询用户详细信息，用于手机号登录和唯一性校验
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    SysUserVo selectUserByPhonenumber(String phonenumber);

    /**
     * 通过用户ID查询用户
     * 根据主键ID查询用户详细信息
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUserVo selectUserById(Long userId);

    /**
     * 通过用户ID串查询用户
     * 根据用户ID列表查询用户基本信息，支持按部门筛选
     *
     * @param userIds 用户ID列表
     * @param deptId  部门id，用于筛选指定部门的用户
     * @return 用户列表信息
     */
    List<SysUserVo> selectUserByIds(List<Long> userIds, Long deptId);

    /**
     * 根据用户ID查询用户所属角色组
     * 查询用户分配的所有角色名称，用逗号分隔拼接成字符串
     *
     * @param userId 用户ID
     * @return 角色名称组字符串
     */
    String selectUserRoleGroup(Long userId);

    /**
     * 根据用户ID查询用户所属岗位组
     * 查询用户分配的所有岗位名称，用逗号分隔拼接成字符串
     *
     * @param userId 用户ID
     * @return 岗位名称组字符串
     */
    String selectUserPostGroup(Long userId);

    /**
     * 校验用户名称是否唯一
     * 检查用户名在数据库中是否已存在，用于新增和修改时的唯一性校验
     *
     * @param user 用户信息（包含用户ID和用户名）
     * @return true-唯一，false-不唯一
     */
    boolean checkUserNameUnique(SysUserBo user);

    /**
     * 校验手机号码是否唯一
     * 检查手机号在数据库中是否已存在，用于新增和修改时的唯一性校验
     *
     * @param user 用户信息（包含用户ID和手机号）
     * @return true-唯一，false-不唯一
     */
    boolean checkPhoneUnique(SysUserBo user);

    /**
     * 校验email是否唯一
     * 检查邮箱在数据库中是否已存在，用于新增和修改时的唯一性校验
     *
     * @param user 用户信息（包含用户ID和邮箱）
     * @return true-唯一，false-不唯一
     */
    boolean checkEmailUnique(SysUserBo user);

    /**
     * 校验用户是否允许操作
     * 检查是否为内置管理员用户（如admin），防止误删或修改系统内置用户
     *
     * @param userId 用户ID
     */
    void checkUserAllowed(Long userId);

    /**
     * 校验用户是否有数据权限
     * 检查当前登录用户是否有权限操作指定用户的数据（数据权限范围控制）
     *
     * @param userId 用户id
     */
    void checkUserDataScope(Long userId);

    /**
     * 新增用户信息
     * 添加新用户到数据库，包含用户基本信息、角色分配、岗位分配
     *
     * @param user 用户信息（包含密码、角色、岗位等）
     * @return 影响的行数
     */
    int insertUser(SysUserBo user);

    /**
     * 注册用户信息
     * 用户自主注册，通常用于前端注册页面，会校验租户用户名额
     *
     * @param user     用户信息
     * @param tenantId 租户ID（多租户模式下使用）
     * @return true-注册成功，false-注册失败
     */
    boolean registerUser(SysUserBo user, String tenantId);

    /**
     * 修改用户信息
     * 更新用户基本信息、角色分配、岗位分配
     *
     * @param user 用户信息
     * @return 影响的行数
     */
    int updateUser(SysUserBo user);

    /**
     * 用户授权角色
     * 为用户分配角色，先删除原有角色关系，再插入新的角色关系
     *
     * @param userId  用户ID
     * @param roleIds 角色ID数组
     */
    void insertUserAuth(Long userId, Long[] roleIds);

    /**
     * 修改用户状态
     * 启用或禁用用户账号
     *
     * @param userId 用户ID
     * @param status 帐号状态（0-正常，1-停用）
     * @return 影响的行数
     */
    int updateUserStatus(Long userId, String status);

    /**
     * 修改用户基本信息
     * 更新用户个人资料（昵称、手机号、邮箱等），不更新密码和状态
     *
     * @param user 用户信息
     * @return 影响的行数
     */
    int updateUserProfile(SysUserBo user);

    /**
     * 修改用户头像
     * 更新用户头像OSS文件ID
     *
     * @param userId 用户ID
     * @param avatar 头像OSS文件ID
     * @return true-更新成功，false-更新失败
     */
    boolean updateUserAvatar(Long userId, Long avatar);

    /**
     * 重置用户密码
     * 更新用户登录密码（已BCrypt加密）
     *
     * @param userId   用户ID
     * @param password 密码（BCrypt加密后的）
     * @return 影响的行数
     */
    int resetUserPwd(Long userId, String password);

    /**
     * 通过用户ID删除用户
     * 删除单个用户及其关联的角色、岗位关系
     *
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     * 批量删除用户及其关联的角色、岗位关系
     *
     * @param userIds 需要删除的用户ID数组
     * @return 影响的行数
     */
    int deleteUserByIds(Long[] userIds);

    /**
     * 通过部门id查询当前部门所有用户
     * 查询指定部门下的所有用户列表
     *
     * @param deptId 部门id
     * @return 用户列表
     */
    List<SysUserVo> selectUserListByDept(Long deptId);
}
