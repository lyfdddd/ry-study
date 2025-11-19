package org.dromara.web.service;


// 业务异常类
import org.dromara.common.core.exception.ServiceException;
// Spring工具类
import org.dromara.common.core.utils.SpringUtils;
// 系统客户端实体类
import org.dromara.system.domain.SysClient;
// 客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 登录视图对象
import org.dromara.web.domain.vo.LoginVo;

/**
 * 授权策略
 *
 * @author Michelle.Chung
 */
public interface IAuthStrategy {

    // 策略Bean名称基础后缀，用于Spring容器查找
    String BASE_NAME = "AuthStrategy";

    /**
     * 登录
     *
     * @param body      登录对象
     * @param client    授权管理视图对象
     * @param grantType 授权类型
     * @return 登录验证信息
     */
    // 静态方法：根据授权类型获取对应的策略实现并执行登录
    static LoginVo login(String body, SysClientVo client, String grantType) {
        // 构建策略Bean名称，格式：grantType + AuthStrategy
        // 例如：passwordAuthStrategy、emailAuthStrategy
        String beanName = grantType + BASE_NAME;
        // 检查Spring容器中是否存在该Bean
        if (!SpringUtils.containsBean(beanName)) {
            // 策略实现不存在，抛出业务异常
            throw new ServiceException("授权类型不正确!");
        }
        // 从Spring容器获取策略实现实例
        IAuthStrategy instance = SpringUtils.getBean(beanName);
        // 调用策略的登录方法
        return instance.login(body, client);
    }

    /**
     * 登录
     *
     * @param body   登录对象
     * @param client 授权管理视图对象
     * @return 登录验证信息
     */
    // 抽象方法：具体策略实现类需要实现此方法
    LoginVo login(String body, SysClientVo client);

}
