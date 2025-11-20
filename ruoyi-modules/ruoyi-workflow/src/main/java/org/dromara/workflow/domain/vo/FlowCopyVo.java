// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 翻译注解：用于字段值翻译，将ID翻译为名称
import org.dromara.common.translation.annotation.Translation;
// 翻译常量：定义翻译类型
import org.dromara.common.translation.constant.TransConstant;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;

/**
 * 抄送对象视图
 * 用于封装抄送人员信息，返回给前端展示
 * 实现Serializable接口，支持序列化传输
 * 包含用户ID和用户名称，名称通过翻译注解自动转换
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowCopyVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 被抄送人员的唯一标识
     */
    private Long userId;

    /**
     * 用户名称
     * 被抄送人员的姓名，用于前端展示
     * 使用@Translation注解将userId翻译为昵称
     */
    // 翻译注解：将用户ID翻译为昵称，mapper指定字段名
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "userId")
    private String userName;

    /**
     * 构造方法
     * 根据用户ID创建抄送对象
     * @param userId 用户ID
     */
    public FlowCopyVo(Long userId) {
        // 初始化userId属性
        this.userId = userId;
    }

}
