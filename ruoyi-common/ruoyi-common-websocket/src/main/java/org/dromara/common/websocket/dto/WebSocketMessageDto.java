// 定义WebSocket消息DTO类所在的包路径
package org.dromara.common.websocket.dto;

// 导入Lombok的@Data注解，自动生成getter、setter、toString等方法
import lombok.Data;

// 导入序列化版本号注解，用于标识类的版本
import java.io.Serial;
// 导入序列化接口，使对象可以序列化传输
import java.io.Serializable;
// 导入List集合接口，用于存储多个会话key
import java.util.List;

/**
 * WebSocket消息传输对象（DTO）
 * 用于在系统内部传输WebSocket消息数据，实现Serializable接口支持序列化
 *
 * @author zendwang
 */
// Lombok注解，自动生成getter、setter、equals、hashCode、toString方法
@Data
// WebSocket消息DTO类，封装消息内容和目标会话列表
public class WebSocketMessageDto implements Serializable {

    // 序列化版本号，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的会话key列表（用户ID列表）
     * 如果为空表示群发消息，否则只发送给指定用户
     */
    private List<Long> sessionKeys;

    /**
     * 需要发送的消息内容
     * 可以是文本、JSON等格式的字符串
     */
    private String message;
}
