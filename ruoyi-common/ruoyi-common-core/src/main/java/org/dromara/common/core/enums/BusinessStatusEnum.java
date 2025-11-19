package org.dromara.common.core.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务状态枚举
 * 定义业务流程中的各种状态，如草稿、待审核、已完成等
 * 用于工作流、审批流程等业务场景的状态管理
 *
 * @author may
 */
// Lombok注解：自动生成getter方法
@Getter
// Lombok注解：生成全参构造函数
@AllArgsConstructor
public enum BusinessStatusEnum {

    /**
     * 已撤销
     * 用户主动撤销申请，流程终止
     */
    CANCEL("cancel", "已撤销"),

    /**
     * 草稿
     * 用户保存但未提交的申请
     */
    DRAFT("draft", "草稿"),

    /**
     * 待审核
     * 已提交申请，等待审批人处理
     */
    WAITING("waiting", "待审核"),

    /**
     * 已完成
     * 审批流程全部通过，业务完成
     */
    FINISH("finish", "已完成"),

    /**
     * 已作废
     * 审批人作废申请，流程终止
     */
    INVALID("invalid", "已作废"),

    /**
     * 已退回
     * 审批人驳回申请，需重新提交
     */
    BACK("back", "已退回"),

    /**
     * 已终止
     * 系统或管理员终止流程
     */
    TERMINATION("termination", "已终止");

    /**
     * 状态码
     * 存储在数据库中的状态值
     */
    private final String status;

    /**
     * 状态描述
     * 前端展示的状态名称
     */
    private final String desc;

    // 静态Map缓存，用于快速根据状态码查找枚举
    // 使用ConcurrentMap保证线程安全
    private static final Map<String, BusinessStatusEnum> STATUS_MAP = Arrays.stream(BusinessStatusEnum.values())
        .collect(Collectors.toConcurrentMap(BusinessStatusEnum::getStatus, Function.identity()));

    /**
     * 根据状态获取对应的 BusinessStatusEnum 枚举
     *
     * @param status 业务状态码
     * @return 对应的 BusinessStatusEnum 枚举，如果找不到则返回 null
     */
    public static BusinessStatusEnum getByStatus(String status) {
        // 使用 STATUS_MAP 获取对应的枚举，若找不到则返回 null
        return STATUS_MAP.get(status);
    }

    /**
     * 根据状态获取对应的业务状态描述信息
     *
     * @param status 业务状态码
     * @return 返回业务状态描述，若状态码为空或未找到对应的枚举，返回空字符串
     */
    public static String findByStatus(String status) {
        // 判空，防止空指针异常
        if (StringUtils.isBlank(status)) {
            return StrUtil.EMPTY;
        }
        // 从缓存Map中获取枚举
        BusinessStatusEnum statusEnum = STATUS_MAP.get(status);
        // 如果找到返回描述，否则返回空字符串
        return (statusEnum != null) ? statusEnum.getDesc() : StrUtil.EMPTY;
    }

    /**
     * 判断是否为指定的状态之一：草稿、已撤销或已退回
     * 用于判断流程是否可以编辑或重新提交
     *
     * @param status 要检查的状态
     * @return 如果状态为草稿、已撤销或已退回之一，则返回 true；否则返回 false
     */
    public static boolean isDraftOrCancelOrBack(String status) {
        // 判断是否为草稿、已撤销或已退回状态
        return DRAFT.status.equals(status) || CANCEL.status.equals(status) || BACK.status.equals(status);
    }

    /**
     * 判断是否为撤销、退回、作废、终止状态
     * 用于判断流程是否已结束（非正常完成）
     *
     * @param status 要检查的状态
     * @return 如果是结束状态返回true，否则返回false
     */
    public static boolean initialState(String status) {
        // 判断是否为撤销、退回、作废、终止状态
        return CANCEL.status.equals(status) || BACK.status.equals(status) || INVALID.status.equals(status) || TERMINATION.status.equals(status);
    }

    /**
     * 获取运行中的实例状态列表
     * 用于查询待办事项、进行中流程
     *
     * @return 包含运行中实例状态的不可变列表
     * （包含 DRAFT、WAITING、BACK 和 CANCEL 状态）
     */
    public static List<String> runningStatus() {
        // 返回运行中状态列表，使用Arrays.asList创建不可变列表
        return Arrays.asList(DRAFT.status, WAITING.status, BACK.status, CANCEL.status);
    }

    /**
     * 获取结束实例的状态列表
     * 用于查询已办事项、历史流程
     *
     * @return 包含结束实例状态的不可变列表
     * （包含 FINISH、INVALID 和 TERMINATION 状态）
     */
    public static List<String> finishStatus() {
        // 返回结束状态列表，使用Arrays.asList创建不可变列表
        return Arrays.asList(FINISH.status, INVALID.status, TERMINATION.status);
    }

    /**
     * 启动流程校验
     * 检查当前状态是否允许启动流程
     * 如果已处于流程中，抛出异常阻止重复提交
     *
     * @param status 当前状态
     */
    public static void checkStartStatus(String status) {
        // 如果状态为待审核，说明已提交过申请
        if (WAITING.getStatus().equals(status)) {
            throw new ServiceException("该单据已提交过申请,正在审批中！");
        // 如果状态为已完成，说明流程已结束
        } else if (FINISH.getStatus().equals(status)) {
            throw new ServiceException("该单据已完成申请！");
        // 如果状态为已作废，说明单据已失效
        } else if (INVALID.getStatus().equals(status)) {
            throw new ServiceException("该单据已作废！");
        // 如果状态为已终止，说明流程被终止
        } else if (TERMINATION.getStatus().equals(status)) {
            throw new ServiceException("该单据已终止！");
        // 如果状态为空，说明数据异常
        } else if (StringUtils.isBlank(status)) {
            throw new ServiceException("流程状态为空！");
        }
    }

    /**
     * 撤销流程校验
     * 检查当前状态是否允许撤销
     * 如果已处于结束状态，不允许撤销
     *
     * @param status 当前状态
     */
    public static void checkCancelStatus(String status) {
        // 如果状态为已撤销，说明已操作过
        if (CANCEL.getStatus().equals(status)) {
            throw new ServiceException("该单据已撤销！");
        // 如果状态为已完成，说明流程已结束
        } else if (FINISH.getStatus().equals(status)) {
            throw new ServiceException("该单据已完成申请！");
        // 如果状态为已作废，说明单据已失效
        } else if (INVALID.getStatus().equals(status)) {
            throw new ServiceException("该单据已作废！");
        // 如果状态为已终止，说明流程被终止
        } else if (TERMINATION.getStatus().equals(status)) {
            throw new ServiceException("该单据已终止！");
        // 如果状态为已退回，说明需要重新提交
        } else if (BACK.getStatus().equals(status)) {
            throw new ServiceException("该单据已退回！");
        // 如果状态为空，说明数据异常
        } else if (StringUtils.isBlank(status)) {
            throw new ServiceException("流程状态为空！");
        }
    }

    /**
     * 驳回流程校验
     * 检查当前状态是否允许驳回
     * 如果已处于结束状态，不允许驳回
     *
     * @param status 当前状态
     */
    public static void checkBackStatus(String status) {
        // 如果状态为已退回，说明已操作过
        if (BACK.getStatus().equals(status)) {
            throw new ServiceException("该单据已退回！");
        // 如果状态为已完成，说明流程已结束
        } else if (FINISH.getStatus().equals(status)) {
            throw new ServiceException("该单据已完成申请！");
        // 如果状态为已作废，说明单据已失效
        } else if (INVALID.getStatus().equals(status)) {
            throw new ServiceException("该单据已作废！");
        // 如果状态为已终止，说明流程被终止
        } else if (TERMINATION.getStatus().equals(status)) {
            throw new ServiceException("该单据已终止！");
        // 如果状态为已撤销，说明用户已撤销
        } else if (CANCEL.getStatus().equals(status)) {
            throw new ServiceException("该单据已撤销！");
        // 如果状态为空，说明数据异常
        } else if (StringUtils.isBlank(status)) {
            throw new ServiceException("流程状态为空！");
        }
    }

    /**
     * 作废、终止流程校验
     * 检查当前状态是否允许作废或终止
     * 如果已处于结束状态，不允许再次操作
     *
     * @param status 当前状态
     */
    public static void checkInvalidStatus(String status) {
        // 如果状态为已完成，说明流程已结束
        if (FINISH.getStatus().equals(status)) {
            throw new ServiceException("该单据已完成申请！");
        // 如果状态为已作废，说明已操作过
        } else if (INVALID.getStatus().equals(status)) {
            throw new ServiceException("该单据已作废！");
        // 如果状态为已终止，说明已操作过
        } else if (TERMINATION.getStatus().equals(status)) {
            throw new ServiceException("该单据已终止！");
        // 如果状态为空，说明数据异常
        } else if (StringUtils.isBlank(status)) {
            throw new ServiceException("流程状态为空！");
        }
    }

}
