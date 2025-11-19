// 定义通用用户服务接口，提供用户相关的查询功能
package org.dromara.common.core.service;

import org.dromara.common.core.domain.dto.UserDTO;

import java.util.List;
import java.util.Map;

/**
 * 通用 用户服务
 * 该接口定义了用户相关的通用查询方法，是系统中最基础的服务接口之一
 * 主要用于工作流、权限、消息通知等模块获取用户信息
 * 实现类通常在ruoyi-system模块中，通过Spring的@Service注解注入
 * 用户是权限系统的核心实体，与角色、部门、岗位等实体关联
 *
 * @author Lion Li
 */
public interface UserService {

    /**
     * 通过用户ID查询用户账户
     * 获取用户的登录账号（用户名），用于显示和日志记录
     * 用户账号是用户的唯一标识，通常用于登录系统
     *
     * @param userId 用户ID
     * @return 用户账户（登录用户名）
     */
    String selectUserNameById(Long userId);

    /**
     * 通过用户ID查询用户账户
     * 获取用户的昵称（显示名称），用于界面展示
     * 昵称通常是用户的中文名或自定义名称，比账号更友好
     *
     * @param userId 用户ID
     * @return 用户名称（昵称）
     */
    String selectNicknameById(Long userId);

    /**
     * 通过用户ID查询用户账户
     * 批量获取多个用户的昵称，ID用逗号分隔
     * 用于需要显示多个用户名称的场景，如流程审批历史
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户名称（昵称列表，逗号分隔）
     */
    String selectNicknameByIds(String userIds);

    /**
     * 通过用户ID查询用户手机号
     * 获取用户的手机号码，用于短信通知、密码找回等功能
     * 手机号是重要的用户联系方式，需要脱敏显示
     *
     * @param userId 用户id
     * @return 用户手机号
     */
    String selectPhonenumberById(Long userId);

    /**
     * 通过用户ID查询用户邮箱
     * 获取用户的邮箱地址，用于邮件通知、密码找回等功能
     * 邮箱是重要的用户联系方式，需要脱敏显示
     *
     * @param userId 用户id
     * @return 用户邮箱
     */
    String selectEmailById(Long userId);

    /**
     * 通过用户ID查询用户列表
     * 批量获取多个用户的详细信息，返回UserDTO对象列表
     * 用于需要展示用户详细信息的场景，如用户选择器
     *
     * @param userIds 用户ids
     * @return 用户列表（包含用户详细信息的DTO对象）
     */
    List<UserDTO> selectListByIds(List<Long> userIds);

    /**
     * 通过角色ID查询用户ID
     * 获取指定角色下的所有用户ID列表
     * 用于权限控制、消息推送等需要按角色筛选用户的场景
     *
     * @param roleIds 角色ids
     * @return 用户ids（属于这些角色的用户ID列表）
     */
    List<Long> selectUserIdsByRoleIds(List<Long> roleIds);

    /**
     * 通过角色ID查询用户
     * 获取指定角色下的所有用户详细信息
     * 用于需要展示角色成员列表的场景，如角色管理
     *
     * @param roleIds 角色ids
     * @return 用户（包含用户详细信息的DTO对象列表）
     */
    List<UserDTO> selectUsersByRoleIds(List<Long> roleIds);

    /**
     * 通过部门ID查询用户
     * 获取指定部门下的所有用户详细信息
     * 用于组织架构管理、部门人员展示等场景
     *
     * @param deptIds 部门ids
     * @return 用户（包含用户详细信息的DTO对象列表）
     */
    List<UserDTO> selectUsersByDeptIds(List<Long> deptIds);

    /**
     * 通过岗位ID查询用户
     * 获取指定岗位下的所有用户详细信息
     * 岗位是用户的职位信息，用于按职位筛选用户
     *
     * @param postIds 岗位ids
     * @return 用户（包含用户详细信息的DTO对象列表）
     */
    List<UserDTO> selectUsersByPostIds(List<Long> postIds);

    /**
     * 根据用户 ID 列表查询用户名称映射关系
     * 批量获取用户名称，返回Map结构便于通过用户ID快速查找
     * 用于需要频繁通过用户ID获取用户名称的场景，避免循环查询数据库
     * Map的key为用户ID，value为用户名称（昵称）
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户名称
     */
    Map<Long, String> selectUserNamesByIds(List<Long> userIds);

}
