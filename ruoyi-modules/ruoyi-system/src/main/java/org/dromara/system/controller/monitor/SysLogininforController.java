// 系统登录日志监控控制器，提供登录日志的查询、导出、删除和清理功能
package org.dromara.system.controller.monitor;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Redisson分布式锁注解，防止并发操作
import com.baomidou.lock.annotation.Lock4j;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// Redis缓存常量定义
import org.dromara.common.core.constant.CacheConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// Excel导出工具类
import org.dromara.common.excel.utils.ExcelUtil;
// 防重复提交注解
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Redis操作工具类
import org.dromara.common.redis.utils.RedisUtils;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 登录日志业务对象
import org.dromara.system.domain.bo.SysLogininforBo;
// 登录日志视图对象
import org.dromara.system.domain.vo.SysLogininforVo;
// 登录日志服务接口
import org.dromara.system.service.ISysLogininforService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 系统登录日志监控控制器
 * 提供登录日志的查询、导出、删除、清理和账户解锁功能
 * 继承BaseController获取通用响应方法
 *
 * @author Lion Li
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/monitor/logininfor
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {

    // 登录日志服务接口，自动注入
    private final ISysLogininforService logininforService;

    /**
     * 获取系统访问记录列表
     * 分页查询登录日志，支持条件筛选
     */
    // Sa-Token权限校验，需要monitor:logininfor:list权限
    @SaCheckPermission("monitor:logininfor:list")
    // GET请求映射，路径为/monitor/logininfor/list
    @GetMapping("/list")
    public TableDataInfo<SysLogininforVo> list(SysLogininforBo logininfor, PageQuery pageQuery) {
        // 调用服务层分页查询登录日志列表
        return logininforService.selectPageLogininforList(logininfor, pageQuery);
    }

    /**
     * 导出系统访问记录列表
     * 将登录日志导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"登录日志"，类型为导出
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要monitor:logininfor:export权限
    @SaCheckPermission("monitor:logininfor:export")
    // POST请求映射，路径为/monitor/logininfor/export
    @PostMapping("/export")
    public void export(SysLogininforBo logininfor, HttpServletResponse response) {
        // 查询所有符合条件的登录日志列表
        List<SysLogininforVo> list = logininforService.selectLogininforList(logininfor);
        // 使用Excel工具类导出数据，指定文件名和响应对象
        ExcelUtil.exportExcel(list, "登录日志", SysLogininforVo.class, response);
    }

    /**
     * 批量删除登录日志
     * @param infoIds 日志ids数组
     */
    // Sa-Token权限校验，需要monitor:logininfor:remove权限
    @SaCheckPermission("monitor:logininfor:remove")
    // 操作日志注解，记录业务操作，标题为"登录日志"，类型为删除
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/monitor/logininfor/{infoIds}，支持多个id用逗号分隔
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable Long[] infoIds) {
        // 调用服务层批量删除登录日志，并返回操作结果
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    /**
     * 清理系统访问记录
     * 清空所有登录日志，需要分布式锁防止并发
     */
    // Sa-Token权限校验，需要monitor:logininfor:remove权限
    @SaCheckPermission("monitor:logininfor:remove")
    // 操作日志注解，记录业务操作，标题为"登录日志"，类型为清理
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    // Redisson分布式锁注解，防止并发清理操作
    @Lock4j
    // DELETE请求映射，路径为/monitor/logininfor/clean
    @DeleteMapping("/clean")
    public R<Void> clean() {
        // 调用服务层清理所有登录日志
        logininforService.cleanLogininfor();
        // 返回成功响应
        return R.ok();
    }

    /**
     * 解锁用户账户
     * 清除密码错误次数缓存，允许用户重新登录
     * @param userName 用户名
     */
    // Sa-Token权限校验，需要monitor:logininfor:unlock权限
    @SaCheckPermission("monitor:logininfor:unlock")
    // 操作日志注解，记录业务操作，标题为"账户解锁"，类型为其他
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    // 防重复提交注解，防止重复解锁
    @RepeatSubmit()
    // GET请求映射，路径为/monitor/logininfor/unlock/{userName}
    @GetMapping("/unlock/{userName}")
    public R<Void> unlock(@PathVariable("userName") String userName) {
        // 构造Redis缓存key，格式为pwd_err_cnt:username
        String loginName = CacheConstants.PWD_ERR_CNT_KEY + userName;
        // 如果缓存中存在该key
        if (RedisUtils.hasKey(loginName)) {
            // 删除密码错误次数缓存
            RedisUtils.deleteObject(loginName);
        }
        // 返回成功响应
        return R.ok();
    }

}
