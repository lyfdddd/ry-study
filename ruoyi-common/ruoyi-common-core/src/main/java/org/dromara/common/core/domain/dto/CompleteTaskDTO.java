// 定义工作流任务办理请求的数据传输对象
package org.dromara.common.core.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 办理任务请求对象
 * 用于工作流引擎中任务办理操作的参数封装
 * 包含任务ID、办理意见、流程变量、抄送信息等关键数据
 * 支持附件上传、消息通知、流程变量设置等高级功能
 * 实现Serializable接口，支持分布式环境下的序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class CompleteTaskDTO implements Serializable {

    // Java序列化版本号，用于反序列化时的版本兼容性检查
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     * 工作流任务表中的主键，标识要办理的具体任务
     * 必填字段，用于定位具体的流程任务
     */
    private Long taskId;

    /**
     * 附件id
     * 上传附件的OSS文件ID，支持任务办理时上传相关附件
     * 多个附件ID可以用逗号分隔，如："file1,file2,file3"
     * 附件信息会存储到流程历史中，便于后续查看
     */
    private String fileId;

    /**
     * 抄送人员
     * 任务办理时需要抄送的用户列表
     * 使用FlowCopyDTO对象封装用户ID和用户名
     * 抄送人员会收到任务办理完成的通知
     */
    private List<FlowCopyDTO> flowCopyList;

    /**
     * 消息类型
     * 通知消息的类型列表，如："email"、"sms"、"system"等
     * 用于控制任务办理完成后发送通知的方式
     * 支持多种通知方式同时发送
     */
    private List<String> messageType;

    /**
     * 办理意见
     * 用户对任务的处理意见或备注信息
     * 如："同意"、"驳回"、"请修改后重新提交"等
     * 会记录到流程历史中，作为审批痕迹
     */
    private String message;

    /**
     * 消息通知
     * 额外的消息通知内容，可以是富文本
     * 用于发送给相关人员的消息内容
     * 支持HTML格式，可以包含链接、样式等
     */
    private String notice;

    /**
     * 办理人(可不填 用于覆盖当前节点办理人)
     * 当需要指定特定人员办理任务时使用
     * 如果不填，则使用流程引擎默认的办理人逻辑
     * 常用于管理员代办理、系统自动办理等场景
     */
    private String handler;

    /**
     * 流程变量
     * 任务办理时设置的流程变量，影响流程走向
     * 如：{"approve": true, "amount": 1000, "dept": "技术部"}
     * 这些变量可以在流程的条件表达式中使用
     */
    private Map<String, Object> variables;

    /**
     * 扩展变量(此处为逗号分隔的ossId)
     * 用于存储额外的扩展信息
     * 当前设计为存储OSS文件ID列表，用逗号分隔
     * 如："file1,file2,file3" 表示多个附件
     */
    private String ext;

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

}
