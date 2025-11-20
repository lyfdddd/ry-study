package org.dromara.common.sse.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息的dto
 *
 * @author zendwang
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
public class SseMessageDto implements Serializable {

    // 序列化版本号，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的用户ID列表，指定哪些用户接收消息
     */
    private List<Long> userIds;

    /**
     * 需要发送的消息内容
     */
    private String message;
}
