package org.dromara.web.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

/**
 * 验证码信息视图对象
 * 用于封装验证码相关数据返回给前端
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class CaptchaVo {

    /**
     * 是否开启验证码
     * 默认为true，表示需要验证码验证
     * 如果系统配置关闭验证码，则返回false，前端可不显示验证码输入框
     */
    private Boolean captchaEnabled = true;

    /**
     * 验证码唯一标识（UUID）
     * 用于关联Redis中存储的验证码值
     * 前端验证时需要将此值连同用户输入的验证码一起提交
     */
    private String uuid;

    /**
     * 验证码图片Base64编码字符串
     * 前端可直接将此字符串作为图片源显示验证码图片
     * 格式：data:image/png;base64,...
     */
    private String img;

}
