// 定义工作流抄送人员的数据传输对象
package org.dromara.common.core.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 抄送
 * 用于工作流任务办理时的抄送功能，指定需要接收通知的用户
 * 封装了抄送用户的基本信息：用户ID和用户名称
 * 实现Serializable接口，支持分布式环境下的序列化传输
 * 通常作为CompleteTaskDTO的一部分，用于批量抄送通知
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class FlowCopyDTO implements Serializable {

    // Java序列化版本号，用于反序列化时的版本兼容性检查
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     * 被抄送用户的唯一标识，对应系统用户表的主键
     * 用于精确定位需要接收抄送通知的用户
     */
    private Long userId;

    /**
     * 用户名称
     * 被抄送用户的显示名称，通常是用户的中文名或昵称
     * 用于在通知中显示用户身份，提升用户体验
     * 可以是用户真实姓名，也可以是系统用户名
     */
    private String userName;

}
