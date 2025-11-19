// 定义通用角色服务接口，提供角色相关的查询功能
package org.dromara.common.core.service;

import java.util.List;
import java.util.Map;

/**
 * 通用 角色服务
 * 该接口定义了角色相关的通用查询方法，主要用于工作流、权限等模块获取角色信息
 * 实现类通常在ruoyi-system模块中，通过Spring的@Service注解注入
 * 角色是权限系统的重要组成部分，用于控制用户访问权限
 *
 * @author AprilWind
 */
public interface RoleService {

    /**
     * 根据角色 ID 列表查询角色名称映射关系
     * 用于批量获取角色名称，避免循环查询数据库
     * 典型应用场景：用户详情展示、工作流任务分配等需要显示角色名称的地方
     * 返回Map结构便于通过角色ID快速查找对应的角色名称
     *
     * @param roleIds 角色 ID 列表
     * @return Map，其中 key 为角色 ID，value 为对应的角色名称
     */
    Map<Long, String> selectRoleNamesByIds(List<Long> roleIds);

}
