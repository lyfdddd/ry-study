// 系统操作日志监控控制器，提供操作日志的查询、导出、删除和清理功能
package org.dromara.system.controller.monitor;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Redisson分布式锁注解，防止并发操作
import com.baomidou.lock.annotation.Lock4j;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// Excel导出工具类
import org.dromara.common.excel.utils.ExcelUtil;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 操作日志业务对象
import org.dromara.system.domain.bo.SysOperLogBo;
// 操作日志视图对象
import org.dromara.system.domain.vo.SysOperLogVo;
// 操作日志服务接口
import org.dromara.system.service.ISysOperLogService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 系统操作日志监控控制器
 * 提供操作日志的查询、导出、删除和清理功能
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
// 请求路径映射，所有接口前缀为/monitor/operlog
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {

    // 操作日志服务接口，自动注入
    private final ISysOperLogService operLogService;

    /**
     * 获取操作日志记录列表
     * 分页查询操作日志，支持条件筛选
     */
    // Sa-Token权限校验，需要monitor:operlog:list权限
    @SaCheckPermission("monitor:operlog:list")
    // GET请求映射，路径为/monitor/operlog/list
    @GetMapping("/list")
    public TableDataInfo<SysOperLogVo> list(SysOperLogBo operLog, PageQuery pageQuery) {
        // 调用服务层分页查询操作日志列表
        return operLogService.selectPageOperLogList(operLog, pageQuery);
    }

    /**
     * 导出操作日志记录列表
     * 将操作日志导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"操作日志"，类型为导出
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要monitor:operlog:export权限
    @SaCheckPermission("monitor:operlog:export")
    // POST请求映射，路径为/monitor/operlog/export
    @PostMapping("/export")
    public void export(SysOperLogBo operLog, HttpServletResponse response) {
        // 查询所有符合条件的操作日志列表
        List<SysOperLogVo> list = operLogService.selectOperLogList(operLog);
        // 使用Excel工具类导出数据，指定文件名和响应对象
        ExcelUtil.exportExcel(list, "操作日志", SysOperLogVo.class, response);
    }

    /**
     * 批量删除操作日志记录
     * @param operIds 日志ids数组
     */
    // 操作日志注解，记录业务操作，标题为"操作日志"，类型为删除
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    // Sa-Token权限校验，需要monitor:operlog:remove权限
    @SaCheckPermission("monitor:operlog:remove")
    // DELETE请求映射，路径为/monitor/operlog/{operIds}，支持多个id用逗号分隔
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable Long[] operIds) {
        // 调用服务层批量删除操作日志，并返回操作结果
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    /**
     * 清理操作日志记录
     * 清空所有操作日志，需要分布式锁防止并发
     */
    // 操作日志注解，记录业务操作，标题为"操作日志"，类型为清理
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    // Sa-Token权限校验，需要monitor:operlog:remove权限
    @SaCheckPermission("monitor:operlog:remove")
    // Redisson分布式锁注解，防止并发清理操作
    @Lock4j
    // DELETE请求映射，路径为/monitor/operlog/clean
    @DeleteMapping("/clean")
    public R<Void> clean() {
        // 调用服务层清理所有操作日志
        operLogService.cleanOperLog();
        // 返回成功响应
        return R.ok();
    }
}
