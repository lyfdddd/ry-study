// 在线用户监控控制器，提供在线用户查询、强退和设备管理功能
package org.dromara.system.controller.monitor;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Sa-Token未登录异常
import cn.dev33.satoken.exception.NotLoginException;
// Sa-Token工具类，提供登录、踢出等操作
import cn.dev33.satoken.stp.StpUtil;
// Hutool Bean工具类，提供对象复制功能
import cn.hutool.core.bean.BeanUtil;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// Redis缓存常量定义
import org.dromara.common.core.constant.CacheConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 用户在线数据传输对象
import org.dromara.common.core.domain.dto.UserOnlineDTO;
// Stream工具类，提供集合过滤功能
import org.dromara.common.core.utils.StreamUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 防重复提交注解
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Redis操作工具类
import org.dromara.common.redis.utils.RedisUtils;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 系统在线用户实体类
import org.dromara.system.domain.SysUserOnline;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线用户监控控制器
 * 提供在线用户查询、强退和设备管理功能
 * 继承BaseController获取通用响应方法
 *
 * @author Lion Li
 */
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/monitor/online
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController {

    /**
     * 获取在线用户监控列表
     * 从Redis获取所有在线用户，支持按IP地址和用户名筛选
     *
     * @param ipaddr   IP地址，用于筛选
     * @param userName 用户名，用于筛选
     */
    // Sa-Token权限校验，需要monitor:online:list权限
    @SaCheckPermission("monitor:online:list")
    // GET请求映射，路径为/monitor/online/list
    @GetMapping("/list")
    public TableDataInfo<SysUserOnline> list(String ipaddr, String userName) {
        // 从Redis获取所有未过期的token key，格式为online_tokens:token
        Collection<String> keys = RedisUtils.keys(CacheConstants.ONLINE_TOKEN_KEY + "*");
        // 创建在线用户DTO列表
        List<UserOnlineDTO> userOnlineDTOList = new ArrayList<>();
        // 遍历所有token key
        for (String key : keys) {
            // 从key中提取token值，取最后一个冒号后的内容
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果token已经过期则跳过，getTokenActiveTimeoutByToken返回-2表示已过期
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                continue;
            }
            // 从Redis获取在线用户信息并添加到列表
            userOnlineDTOList.add(RedisUtils.getCacheObject(CacheConstants.ONLINE_TOKEN_KEY + token));
        }
        // 如果IP地址和用户名都不为空，同时按两个条件筛选
        if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
            // 使用StreamUtils过滤，同时匹配IP地址和用户名
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(ipaddr, userOnline.getIpaddr()) &&
                    StringUtils.equals(userName, userOnline.getUserName())
            );
        } else if (StringUtils.isNotEmpty(ipaddr)) {
            // 如果只有IP地址不为空，按IP地址筛选
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(ipaddr, userOnline.getIpaddr())
            );
        } else if (StringUtils.isNotEmpty(userName)) {
            // 如果只有用户名不为空，按用户名筛选
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(userName, userOnline.getUserName())
            );
        }
        // 反转列表，使最新登录的用户排在前面
        Collections.reverse(userOnlineDTOList);
        // 移除列表中的null元素，防止空指针异常
        userOnlineDTOList.removeAll(Collections.singleton(null));
        // 使用Hutool将DTO列表复制为实体类列表
        List<SysUserOnline> userOnlineList = BeanUtil.copyToList(userOnlineDTOList, SysUserOnline.class);
        // 构建表格数据响应
        return TableDataInfo.build(userOnlineList);
    }

    /**
     * 强退用户
     * 强制指定token的用户下线
     *
     * @param tokenId token值
     */
    // Sa-Token权限校验，需要monitor:online:forceLogout权限
    @SaCheckPermission("monitor:online:forceLogout")
    // 操作日志注解，记录业务操作，标题为"在线用户"，类型为强制退出
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    // 防重复提交注解，防止重复强退
    @RepeatSubmit()
    // DELETE请求映射，路径为/monitor/online/{tokenId}
    @DeleteMapping("/{tokenId}")
    public R<Void> forceLogout(@PathVariable String tokenId) {
        try {
            // 调用Sa-Token踢出指定token的用户
            StpUtil.kickoutByTokenValue(tokenId);
        } catch (NotLoginException ignored) {
            // 如果用户已经下线，忽略异常
        }
        // 返回成功响应
        return R.ok();
    }

    /**
     * 获取当前用户登录在线设备
     * 查询当前登录用户的所有在线设备信息
     */
    // GET请求映射，路径为/monitor/online
    @GetMapping()
    public TableDataInfo<SysUserOnline> getInfo() {
        // 获取当前登录用户的所有token值列表
        List<String> tokenIds = StpUtil.getTokenValueListByLoginId(StpUtil.getLoginIdAsString());
        // 使用Stream API处理token列表
        List<UserOnlineDTO> userOnlineDTOList = tokenIds.stream()
            // 过滤未过期的token
            .filter(token -> StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) >= -1)
            // 从Redis获取在线用户信息
            .map(token -> (UserOnlineDTO) RedisUtils.getCacheObject(CacheConstants.ONLINE_TOKEN_KEY + token))
            // 收集为列表
            .collect(Collectors.toList());
        // 反转列表，使最新设备排在前面
        Collections.reverse(userOnlineDTOList);
        // 移除列表中的null元素
        userOnlineDTOList.removeAll(Collections.singleton(null));
        // 使用Hutool将DTO列表复制为实体类列表
        List<SysUserOnline> userOnlineList = BeanUtil.copyToList(userOnlineDTOList, SysUserOnline.class);
        // 构建表格数据响应
        return TableDataInfo.build(userOnlineList);
    }

    /**
     * 强退当前在线设备
     * 用户主动下线自己的某个设备
     *
     * @param tokenId token值
     */
    // 操作日志注解，记录业务操作，标题为"在线设备"，类型为强制退出
    @Log(title = "在线设备", businessType = BusinessType.FORCE)
    // 防重复提交注解，防止重复强退
    @RepeatSubmit()
    // DELETE请求映射，路径为/monitor/online/myself/{tokenId}
    @DeleteMapping("/myself/{tokenId}")
    public R<Void> remove(@PathVariable("tokenId") String tokenId) {
        try {
            // 获取当前登录用户的所有token值列表
            List<String> keys = StpUtil.getTokenValueListByLoginId(StpUtil.getLoginIdAsString());
            // 使用Stream API查找匹配的token并踢出
            keys.stream()
                // 过滤与传入tokenId相同的key
                .filter(key -> key.equals(tokenId))
                // 查找第一个匹配项
                .findFirst()
                // 如果找到则踢出该token
                .ifPresent(key -> StpUtil.kickoutByTokenValue(tokenId));
        } catch (NotLoginException ignored) {
            // 如果用户已经下线，忽略异常
        }
        // 返回成功响应
        return R.ok();
    }

}
