// 定义通用岗位服务接口，提供岗位相关的查询功能
package org.dromara.common.core.service;

import java.util.List;
import java.util.Map;

/**
 * 通用 岗位服务
 * 该接口定义了岗位相关的通用查询方法，主要用于工作流、权限等模块获取岗位信息
 * 实现类通常在ruoyi-system模块中，通过Spring的@Service注解注入
 *
 * @author AprilWind
 */
public interface PostService {

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     * 用于批量获取岗位名称，避免循环查询数据库
     * 典型应用场景：用户详情展示、工作流任务分配等需要显示岗位名称的地方
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    Map<Long, String> selectPostNamesByIds(List<Long> postIds);

}
