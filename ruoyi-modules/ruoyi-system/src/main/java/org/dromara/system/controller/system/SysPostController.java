// 岗位信息操作控制器，提供岗位的CRUD、导出、选择框列表和部门树查询功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Hutool树形结构工具类
import cn.hutool.core.lang.tree.Tree;
// Hutool对象工具类，用于对象非空判断
import cn.hutool.core.util.ObjectUtil;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 系统常量类，包含状态码等常量
import org.dromara.common.core.constant.SystemConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// Excel工具类，用于数据导出
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
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 部门业务对象
import org.dromara.system.domain.bo.SysDeptBo;
// 岗位业务对象
import org.dromara.system.domain.bo.SysPostBo;
// 岗位视图对象
import org.dromara.system.domain.vo.SysPostVo;
// 部门服务接口
import org.dromara.system.service.ISysDeptService;
// 岗位服务接口
import org.dromara.system.service.ISysPostService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 岗位信息操作处理控制器
 * 提供岗位的CRUD、导出、选择框列表和部门树查询功能
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
// 请求路径映射，所有接口前缀为/system/post
@RequestMapping("/system/post")
public class SysPostController extends BaseController {

    // 岗位服务接口，自动注入
    private final ISysPostService postService;
    // 部门服务接口，自动注入
    private final ISysDeptService deptService;

    /**
     * 获取岗位列表
     * 分页查询岗位信息，支持条件筛选
     */
    // Sa-Token权限校验，需要system:post:list权限
    @SaCheckPermission("system:post:list")
    // GET请求映射，路径为/system/post/list
    @GetMapping("/list")
    public TableDataInfo<SysPostVo> list(SysPostBo post, PageQuery pageQuery) {
        // 调用服务层分页查询岗位列表
        return postService.selectPagePostList(post, pageQuery);
    }

    /**
     * 导出岗位列表
     * 将岗位数据导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"岗位管理"，类型为导出
    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要system:post:export权限
    @SaCheckPermission("system:post:export")
    // POST请求映射，路径为/system/post/export
    @PostMapping("/export")
    public void export(SysPostBo post, HttpServletResponse response) {
        // 调用服务层查询所有岗位列表
        List<SysPostVo> list = postService.selectPostList(post);
        // 使用Excel工具类导出数据到响应流
        ExcelUtil.exportExcel(list, "岗位数据", SysPostVo.class, response);
    }

    /**
     * 根据岗位编号获取详细信息
     * 根据主键ID查询岗位详情
     *
     * @param postId 岗位ID
     */
    // Sa-Token权限校验，需要system:post:query权限
    @SaCheckPermission("system:post:query")
    // GET请求映射，路径为/system/post/{postId}
    @GetMapping(value = "/{postId}")
    public R<SysPostVo> getInfo(@PathVariable Long postId) {
        // 调用服务层查询岗位详情并返回
        return R.ok(postService.selectPostById(postId));
    }

    /**
     * 新增岗位
     * 添加新的岗位信息
     */
    // Sa-Token权限校验，需要system:post:add权限
    @SaCheckPermission("system:post:add")
    // 操作日志注解，记录业务操作，标题为"岗位管理"，类型为新增
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/post
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysPostBo post) {
        // 校验岗位名称唯一性
        if (!postService.checkPostNameUnique(post)) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        // 校验岗位编码唯一性
        } else if (!postService.checkPostCodeUnique(post)) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        // 调用服务层新增岗位，并返回操作结果
        return toAjax(postService.insertPost(post));
    }

    /**
     * 修改岗位
     * 更新岗位信息
     */
    // Sa-Token权限校验，需要system:post:edit权限
    @SaCheckPermission("system:post:edit")
    // 操作日志注解，记录业务操作，标题为"岗位管理"，类型为更新
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/post
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysPostBo post) {
        // 校验岗位名称唯一性
        if (!postService.checkPostNameUnique(post)) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        // 校验岗位编码唯一性
        } else if (!postService.checkPostCodeUnique(post)) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        // 如果状态为禁用且岗位下有用户，则不允许禁用
        } else if (SystemConstants.DISABLE.equals(post.getStatus())
            && postService.countUserPostById(post.getPostId()) > 0) {
            return R.fail("该岗位下存在已分配用户，不能禁用!");
        }
        // 调用服务层更新岗位，并返回操作结果
        return toAjax(postService.updatePost(post));
    }

    /**
     * 删除岗位
     * 批量删除岗位信息
     *
     * @param postIds 岗位ID数组
     */
    // Sa-Token权限校验，需要system:post:remove权限
    @SaCheckPermission("system:post:remove")
    // 操作日志注解，记录业务操作，标题为"岗位管理"，类型为删除
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/post/{postIds}，支持多个id用逗号分隔
    @DeleteMapping("/{postIds}")
    public R<Void> remove(@PathVariable Long[] postIds) {
        // 调用服务层批量删除岗位，并返回操作结果
        return toAjax(postService.deletePostByIds(Arrays.asList(postIds)));
    }

    /**
     * 获取岗位选择框列表
     * 根据部门ID或岗位ID数组查询岗位列表，用于下拉选择框
     *
     * @param postIds 岗位ID数组
     * @param deptId  部门id
     */
    // Sa-Token权限校验，需要system:post:query权限
    @SaCheckPermission("system:post:query")
    // GET请求映射，路径为/system/post/optionselect
    @GetMapping("/optionselect")
    public R<List<SysPostVo>> optionselect(@RequestParam(required = false) Long[] postIds, @RequestParam(required = false) Long deptId) {
        // 初始化岗位列表为空
        List<SysPostVo> list = new ArrayList<>();
        // 如果部门ID不为空，则查询该部门下的所有岗位
        if (ObjectUtil.isNotNull(deptId)) {
            SysPostBo post = new SysPostBo();
            post.setDeptId(deptId);
            list = postService.selectPostList(post);
        // 如果岗位ID数组不为空，则查询指定ID的岗位列表
        } else if (postIds != null) {
            list = postService.selectPostByIds(List.of(postIds));
        }
        // 返回岗位列表
        return R.ok(list);
    }

    /**
     * 获取部门树列表
     * 查询部门树形结构，用于岗位分配
     */
    // Sa-Token权限校验，需要system:post:list权限
    @SaCheckPermission("system:post:list")
    // GET请求映射，路径为/system/post/deptTree
    @GetMapping("/deptTree")
    public R<List<Tree<Long>>> deptTree(SysDeptBo dept) {
        // 调用部门服务查询部门树列表并返回
        return R.ok(deptService.selectDeptTreeList(dept));
    }


}
