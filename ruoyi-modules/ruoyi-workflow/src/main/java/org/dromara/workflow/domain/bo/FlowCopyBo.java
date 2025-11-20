// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;

/**
 * 抄送业务对象
 * 用于封装抄送人员信息
 * 实现Serializable接口，支持序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowCopyBo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 被抄送人员的唯一标识
     * 用于定位抄送对象
     */
    private Long userId;

    /**
     * 用户名称
     * 被抄送人员的姓名
     * 用于显示抄送对象
     */
    private String userName;

}
