// 通知公告管理控制器，提供公告的CRUD和SSE实时推送功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 字典服务接口，用于获取字典标签
import org.dromara.common.core.service.DictService;
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
// SSE消息工具类，用于实时推送通知
import org.dromara.common.sse.utils.SseMessageUtils;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 通知公告业务对象
import org.dromara.system.domain.bo.SysNoticeBo;
// 通知公告视图对象
import org.dromara.system.domain.vo.SysNoticeVo;
// 通知公告服务接口
import org.dromara.system.service.ISysNoticeService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告管理控制器
 * 提供公告的CRUD和SSE实时推送功能
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
// 请求路径映射，所有接口前缀为/system/notice
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {

    // 通知公告服务接口，自动注入
    private final ISysNoticeService noticeService;
    // 字典服务接口，自动注入，用于获取公告类型标签
    private final DictService dictService;

    /**
     * 获取通知公告列表
     * 分页查询通知公告，支持条件筛选
     */
    // Sa-Token权限校验，需要system:notice:list权限
    @SaCheckPermission("system:notice:list")
    // GET请求映射，路径为/system/notice/list
    @GetMapping("/list")
    public TableDataInfo<SysNoticeVo> list(SysNoticeBo notice, PageQuery pageQuery) {
        // 调用服务层分页查询通知公告列表
        return noticeService.selectPageNoticeList(notice, pageQuery);
    }

    /**
     * 根据通知公告编号获取详细信息
     * 根据主键ID查询通知公告详情
     *
     * @param noticeId 公告ID
     */
    // Sa-Token权限校验，需要system:notice:query权限
    @SaCheckPermission("system:notice:query")
    // GET请求映射，路径为/system/notice/{noticeId}
    @GetMapping(value = "/{noticeId}")
    public R<SysNoticeVo> getInfo(@PathVariable Long noticeId) {
        // 调用服务层查询通知公告详情并返回
        return R.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     * 添加新的通知公告，并通过SSE实时推送给所有在线用户
     */
    // Sa-Token权限校验，需要system:notice:add权限
    @SaCheckPermission("system:notice:add")
    // 操作日志注解，记录业务操作，标题为"通知公告"，类型为新增
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/notice
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysNoticeBo notice) {
        // 调用服务层新增通知公告，获取影响行数
        int rows = noticeService.insertNotice(notice);
        // 如果影响行数小于等于0，表示新增失败
        if (rows <= 0) {
            return R.fail();
        }
        // 通过字典服务获取公告类型标签（如：通知、公告、预警）
        String type = dictService.getDictLabel("sys_notice_type", notice.getNoticeType());
        // 使用SSE推送通知给所有在线用户，格式为"[类型] 标题"
        SseMessageUtils.publishAll("[" + type + "] " + notice.getNoticeTitle());
        // 返回成功响应
        return R.ok();
    }

    /**
     * 修改通知公告
     * 更新通知公告信息
     */
    // Sa-Token权限校验，需要system:notice:edit权限
    @SaCheckPermission("system:notice:edit")
    // 操作日志注解，记录业务操作，标题为"通知公告"，类型为更新
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/notice
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysNoticeBo notice) {
        // 调用服务层更新通知公告，并返回操作结果
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     * 批量删除通知公告
     *
     * @param noticeIds 公告ID数组
     */
    // Sa-Token权限校验，需要system:notice:remove权限
    @SaCheckPermission("system:notice:remove")
    // 操作日志注解，记录业务操作，标题为"通知公告"，类型为删除
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/notice/{noticeIds}，支持多个id用逗号分隔
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable Long[] noticeIds) {
        // 调用服务层批量删除通知公告，并返回操作结果
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
