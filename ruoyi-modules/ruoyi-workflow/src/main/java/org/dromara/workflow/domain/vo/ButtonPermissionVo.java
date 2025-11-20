// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;

/**
 * 按钮权限视图对象
 * 用于封装流程节点上的按钮权限信息，返回给前端展示
 * 包含按钮的唯一编码、显示值和是否显示标志
 * 实现Serializable接口，支持序列化传输
 *
 * @author may
 * @date 2025-02-28
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class ButtonPermissionVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 唯一编码
     * 按钮的唯一标识，如"agree"、"reject"、"transfer"等
     */
    private String code;

    /**
     * 选项值
     * 按钮的显示文本，如"同意"、"拒绝"、"转办"等
     */
    private String value;

    /**
     * 是否显示
     * 控制按钮是否在前端显示，true表示显示，false表示隐藏
     */
    private Boolean show;

    /**
     * 无参构造方法
     * 用于创建空的按钮权限对象
     */
    public ButtonPermissionVo() {
    }

    /**
     * 带参构造方法
     * 根据按钮编码和显示标志创建按钮权限对象
     * @param code 按钮唯一编码
     * @param show 是否显示标志
     */
    public ButtonPermissionVo(String code, Boolean show) {
        // 初始化code属性
        this.code = code;
        // 初始化show属性
        this.show = show;
    }

}
