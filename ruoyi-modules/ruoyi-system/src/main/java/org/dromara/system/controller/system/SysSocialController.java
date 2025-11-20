// 社会化关系控制器，提供第三方社交账号绑定查询功能
package org.dromara.system.controller.system;

// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 登录助手，获取当前登录用户信息
import org.dromara.common.satoken.utils.LoginHelper;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 社会化关系视图对象
import org.dromara.system.domain.vo.SysSocialVo;
// 社会化关系服务接口
import org.dromara.system.service.ISysSocialService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Java集合类
import java.util.List;

/**
 * 社会化关系控制器
 * 提供第三方社交账号绑定查询功能
 * 继承BaseController获取通用响应方法
 *
 * @author thiszhc
 * @date 2023-06-16
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/system/social
@RequestMapping("/system/social")
public class SysSocialController extends BaseController {

    // 社会化关系服务接口，自动注入
    private final ISysSocialService socialUserService;

    /**
     * 查询社会化关系列表
     * 获取当前登录用户绑定的第三方社交账号列表
     */
    // GET请求映射，路径为/system/social/list
    @GetMapping("/list")
    public R<List<SysSocialVo>> list() {
        // 调用服务层查询当前登录用户的社会化关系列表并返回
        return R.ok(socialUserService.queryListByUserId(LoginHelper.getUserId()));
    }

}
