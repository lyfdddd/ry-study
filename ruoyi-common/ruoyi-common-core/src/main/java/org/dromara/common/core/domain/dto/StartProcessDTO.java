// 定义工作流流程启动请求的数据传输对象
package org.dromara.common.core.domain.dto;


import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 启动流程对象
 * 用于工作流引擎中流程启动操作的参数封装
 * 包含业务ID、流程定义编码、办理人、流程变量等关键信息
 * 支持流程变量设置和业务扩展信息传递
 * 实现Serializable接口，支持分布式环境下的序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class StartProcessDTO implements Serializable {

    // Java序列化版本号，用于反序列化时的版本兼容性检查
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务唯一值id
     * 业务系统的主键，用于关联流程实例和业务数据
     * 必填字段，建立流程与业务的关联关系
     * 例如：请假单ID、采购单ID等业务主键
     */
    private String businessId;

    /**
     * 流程定义编码
     * 流程定义的标识符，用于指定要启动的流程模板
     * 必填字段，对应流程定义表中的流程编码
     * 例如："leave_process"、"purchase_process"等
     */
    private String flowCode;

    /**
     * 办理人(可不填 用于覆盖当前节点办理人)
     * 指定流程启动的办理人，如果不填则使用当前登录用户
     * 常用于管理员代提交、系统自动提交等场景
     * 格式为用户ID或用户账号，根据流程引擎配置决定
     */
    private String handler;

    /**
     * 流程变量，前端会提交一个元素{'entity': {业务详情数据对象}}
     * 用于在流程执行过程中传递业务数据
     * 典型的变量包括：业务实体、审批意见、金额、部门等
     * 这些变量可以在流程的条件表达式、任务分配等场景使用
     * 例如：{"entity": {"id": 1, "title": "请假申请"}, "amount": 1000, "dept": "技术部"}
     */
    private Map<String, Object> variables;

    /**
     * 流程业务扩展信息
     * 用于存储流程实例的扩展业务数据
     * 包含业务标题、紧急程度、分类等信息
     * 与variables不同，bizExt是结构化的业务扩展数据
     */
    private FlowInstanceBizExtDTO bizExt;

    /**
     * 获取流程变量
     * 如果variables为null，返回一个初始容量为16的空HashMap
     * 同时过滤掉值为null的条目，确保流程变量的有效性
     * 这是防御式编程，避免空指针异常
     *
     * @return 非空的流程变量Map，已过滤null值
     */
    public Map<String, Object> getVariables() {
        // 如果variables为null，创建一个初始容量为16的HashMap
        // 16是HashMap的默认初始容量，适合大多数场景
        if (variables == null) {
            return new HashMap<>(16);
        }
        // 使用Java 8的removeIf方法过滤掉值为null的条目
        // 避免流程变量中出现null值，影响流程引擎的判断
        variables.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        return variables;
    }

    /**
     * 获取业务扩展信息
     * 如果bizExt为null，创建一个新的FlowInstanceBizExtDTO对象
     * 这是防御式编程，避免空指针异常
     * 确保总是有可用的业务扩展对象
     *
     * @return 非空的FlowInstanceBizExtDTO对象
     */
    public FlowInstanceBizExtDTO getBizExt() {
        // 使用Hutool的ObjectUtil.isNull判断对象是否为null
        // 比直接==null更安全，可以处理一些特殊情况
        if (ObjectUtil.isNull(bizExt)) {
            // 创建新的业务扩展对象，使用默认构造方法
            bizExt = new FlowInstanceBizExtDTO();
        }
        return bizExt;
    }
}
