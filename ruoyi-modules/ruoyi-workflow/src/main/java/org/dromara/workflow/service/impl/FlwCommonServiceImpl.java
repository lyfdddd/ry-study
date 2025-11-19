// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
package org.dromara.workflow.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 公共核心领域模型：用户DTO，用于跨服务数据传输
import org.dromara.common.core.domain.dto.UserDTO;
// 公共核心工具类：Spring工具类，提供Spring上下文相关操作
import org.dromara.common.core.utils.SpringUtils;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共邮件工具类：邮件发送工具
import org.dromara.common.mail.utils.MailUtils;
// 公共SSE工具类：SSE消息DTO
import org.dromara.common.sse.dto.SseMessageDto;
// 公共SSE工具类：SSE消息发送工具
import org.dromara.common.sse.utils.SseMessageUtils;
// Warm-Flow流程引擎核心：流程引擎入口
import org.dromara.warm.flow.core.FlowEngine;
// Warm-Flow流程引擎核心实体：节点实体
import org.dromara.warm.flow.core.entity.Node;
// Warm-Flow流程引擎ORM实体：流程任务实体
import org.dromara.warm.flow.orm.entity.FlowTask;
// 工作流公共组件：条件启用注解（当工作流功能开启时才加载）
import org.dromara.workflow.common.ConditionalOnEnable;
// 工作流公共枚举：消息类型枚举
import org.dromara.workflow.common.enums.MessageTypeEnum;
// 工作流服务接口：工作流通用服务接口
import org.dromara.workflow.service.IFlwCommonService;
// 工作流服务接口：流程任务服务接口
import org.dromara.workflow.service.IFlwTaskService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * 工作流通用服务实现类
 * 核心业务：流程消息通知（系统消息、邮件、短信）、申请人节点查询
 * 实现接口：IFlwCommonService（工作流通用服务）
 *
 * @author LionLi
 */
// 条件启用注解：当工作流功能开启时才加载该服务
@ConditionalOnEnable
// Lombok注解：自动生成SLF4J日志对象
@Slf4j
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class FlwCommonServiceImpl implements IFlwCommonService {

    // 默认邮件标题
    private static final String DEFAULT_SUBJECT = "单据审批提醒";

    /**
     * 根据流程实例发送消息给当前处理人
     * 查询流程实例的当前待办任务，向所有办理人发送通知消息
     *
     * @param flowName    流程定义名称
     * @param instId      流程实例ID
     * @param messageType 消息类型列表（system:系统消息, email:邮件, sms:短信）
     * @param message     消息内容，为空则使用默认消息
     */
    @Override
    public void sendMessage(String flowName, Long instId, List<String> messageType, String message) {
        // 如果消息类型列表为空，直接返回
        if (CollUtil.isEmpty(messageType)) {
            return;
        }
        // 通过Spring工具类获取流程任务服务（解决循环依赖问题）
        IFlwTaskService flwTaskService = SpringUtils.getBean(IFlwTaskService.class);
        // 查询流程实例的当前待办任务列表
        List<FlowTask> list = flwTaskService.selectByInstId(instId);
        // 如果待办任务列表为空，直接返回
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 如果消息内容为空，使用默认消息模板
        if (StringUtils.isBlank(message)) {
            message = "有新的【" + flowName + "】单据已经提交至您，请您及时处理。";
        }
        // 查询所有待办任务的办理人列表
        List<UserDTO> userList = flwTaskService.currentTaskAllUser(StreamUtils.toList(list, FlowTask::getId));
        // 如果办理人列表为空，直接返回
        if (CollUtil.isEmpty(userList)) {
            return;
        }
        // 调用重载方法发送消息
        sendMessage(messageType, message, DEFAULT_SUBJECT, userList);
    }

    /**
     * 发送消息给指定用户列表
     * 根据消息类型分别发送系统消息、邮件或短信
     *
     * @param messageType 消息类型列表（system:系统消息, email:邮件, sms:短信）
     * @param message     消息内容
     * @param subject     邮件标题
     * @param userList    接收用户列表
     */
    @Override
    public void sendMessage(List<String> messageType, String message, String subject, List<UserDTO> userList) {
        // 如果消息类型列表或用户列表为空，直接返回
        if (CollUtil.isEmpty(messageType) || CollUtil.isEmpty(userList)) {
            return;
        }
        // 提取用户ID列表（去重）
        List<Long> userIds = new ArrayList<>(StreamUtils.toSet(userList, UserDTO::getUserId));
        // 提取用户邮箱集合（去重）
        Set<String> emails = StreamUtils.toSet(userList, UserDTO::getEmail);

        // 遍历消息类型列表
        for (String code : messageType) {
            // 根据编码获取消息类型枚举
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByCode(code);
            // 如果消息类型枚举为空，跳过
            if (ObjectUtil.isEmpty(messageTypeEnum)) {
                continue;
            }
            // 根据消息类型执行不同的发送逻辑
            switch (messageTypeEnum) {
                case SYSTEM_MESSAGE -> { // 系统消息（SSE推送）
                    // 创建SSE消息DTO
                    SseMessageDto dto = new SseMessageDto();
                    // 设置接收用户ID列表
                    dto.setUserIds(userIds);
                    // 设置消息内容
                    dto.setMessage(message);
                    // 发布SSE消息
                    SseMessageUtils.publishMessage(dto);
                }
                case EMAIL_MESSAGE -> // 邮件消息
                    // 调用邮件工具类发送文本邮件
                    MailUtils.sendText(emails, subject, message);
                case SMS_MESSAGE -> { // 短信消息
                    // TODO: 短信发送功能待实现
                }
                default -> // 未知消息类型，抛出异常
                    throw new IllegalStateException("Unexpected value: " + messageTypeEnum);
            }
        }
    }


    /**
     * 查询申请人节点编码
     * 获取流程定义的第一个审批节点编码，用于申请人提交流程
     *
     * @param definitionId 流程定义ID
     * @return 申请人节点编码
     */
    @Override
    public String applyNodeCode(Long definitionId) {
        // 调用Warm-Flow流程引擎的节点服务，获取第一个审批节点
        List<Node> firstBetweenNode = FlowEngine.nodeService().getFirstBetweenNode(definitionId, new HashMap<>());
        // 返回第一个节点的节点编码
        return firstBetweenNode.get(0).getNodeCode();
    }
}
