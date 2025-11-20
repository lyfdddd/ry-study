// 包声明：定义当前服务接口所在的包路径，org.dromara.workflow.service 表示工作流模块服务层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service;

// 用户数据传输对象：封装用户基本信息
import org.dromara.common.core.domain.dto.UserDTO;

// List集合接口：用于存储列表数据
import java.util.List;

/**
 * 流程设计器-获取办理人
 * 定义流程设计器中办理人相关的业务逻辑方法
 * 用于解析存储标识符并获取用户列表
 *
 * @author AprilWind
 */

// 流程任务办理人服务接口
public interface IFlwTaskAssigneeService {

    /**
     * 批量解析多个存储标识符（storageIds），按类型分类并合并查询用户列表
     * 输入格式支持多个以逗号分隔的标识（如 "user:123,role:456,789"）
     * 会自动去重返回结果，非法格式的标识将被忽略
     *
     * @param storageIds 多个存储标识符字符串（逗号分隔）
     * @return 合并后的用户列表，去重后返回，非法格式的标识将被跳过
     */
    // 根据存储标识符获取用户列表
    List<UserDTO> fetchUsersByStorageIds(String storageIds);

}
