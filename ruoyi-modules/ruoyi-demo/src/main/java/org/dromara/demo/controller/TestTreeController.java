// 测试树表Controller层
// 提供测试树表的增删改查、导出等RESTful API接口
// 树表结构支持无限级分类，常用于部门、分类等场景
package org.dromara.demo.controller;

// Sa-Token权限校验注解，用于接口级别的权限控制
import cn.dev33.satoken.annotation.SaCheckPermission;
// 统一响应结果封装类，提供标准化的API响应格式
import org.dromara.common.core.domain.R;
// 新增分组校验接口，用于区分新增场景的校验规则
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验接口，用于区分编辑场景的校验规则
import org.dromara.common.core.validate.EditGroup;
// 查询分组校验接口，用于区分查询场景的校验规则
import org.dromara.common.core.validate.QueryGroup;
// 基础Controller类，提供通用的响应封装方法如toAjax()
import org.dromara.common.web.core.BaseController;
// Excel工具类，提供导出功能
import org.dromara.common.excel.utils.ExcelUtil;
// 防重复提交注解，基于Redis实现幂等性控制
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，标记需要记录操作日志的方法
import org.dromara.common.log.annotation.Log;
// 业务类型枚举，定义操作类型（INSERT/UPDATE/DELETE/EXPORT/IMPORT等）
import org.dromara.common.log.enums.BusinessType;
// 测试树表业务对象，封装前端传入的参数
import org.dromara.demo.domain.bo.TestTreeBo;
// 测试树表视图对象，返回给前端的数据格式
import org.dromara.demo.domain.vo.TestTreeVo;
// 测试树表服务接口，定义业务逻辑方法
import org.dromara.demo.service.ITestTreeService;
// Lombok注解：生成包含所有final字段的构造方法
import lombok.RequiredArgsConstructor;
// Spring校验注解，启用方法参数校验功能
import org.springframework.validation.annotation.Validated;
// Spring Web注解：组合注解，包含@Controller和@ResponseBody
import org.springframework.web.bind.annotation.*;
// Jakarta Servlet HTTP响应接口，用于文件下载
import jakarta.servlet.http.HttpServletResponse;
// Jakarta校验注解：验证集合/数组不为空
import jakarta.validation.constraints.NotEmpty;
// Jakarta校验注解：验证对象不为null
import jakarta.validation.constraints.NotNull;
// Java集合工具类
import java.util.Arrays;
import java.util.List;

/**
 * 测试树表Controller
 * 提供测试树表的增删改查、导出等RESTful API接口
 * 树表结构支持无限级分类，常用于部门、分类等场景
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Spring校验注解，启用方法参数校验功能
@Validated
// Lombok注解：生成包含所有final字段的构造方法，实现构造函数注入
@RequiredArgsConstructor
// Spring注解：组合@Controller和@ResponseBody，表示RESTful控制器
@RestController
// Spring注解：指定请求路径前缀为/demo/tree
@RequestMapping("/demo/tree")
// 测试树表Controller，继承BaseController
public class TestTreeController extends BaseController {

    // 测试树表服务接口，通过构造函数注入
    // final关键字确保依赖不可变，符合依赖注入最佳实践
    private final ITestTreeService testTreeService;

    /**
     * 查询测试树表列表
     * 返回树形结构数据，支持无限级分类
     * 需要demo:tree:list权限
     *
     * @param bo 查询条件业务对象
     * @return 统一响应结果，包含树形列表数据
     */
    // Sa-Token权限校验：需要demo:tree:list权限
    @SaCheckPermission("demo:tree:list")
    // Spring注解：指定GET请求方法，路径为/list
    @GetMapping("/list")
    // 查询测试树表列表，返回树形结构
    public R<List<TestTreeVo>> list(@Validated(QueryGroup.class) TestTreeBo bo) {
        // 调用服务层查询列表方法
        // 返回List<TestTreeVo>树形结构数据
        List<TestTreeVo> list = testTreeService.queryList(bo);
        // 使用R.ok()包装结果，标准化响应格式
        return R.ok(list);
    }

    /**
     * 导出测试树表列表
     * 将查询结果导出为Excel文件
     * 需要demo:tree:export权限，记录操作日志
     *
     * @param bo 查询条件业务对象
     * @param response HTTP响应对象，用于写入Excel文件
     */
    // Sa-Token权限校验：需要demo:tree:export权限
    @SaCheckPermission("demo:tree:export")
    // 操作日志注解：记录导出操作日志
    @Log(title = "测试树表", businessType = BusinessType.EXPORT)
    // Spring注解：指定GET请求方法，路径为/export
    @GetMapping("/export")
    // 导出测试树表列表到Excel
    public void export(@Validated TestTreeBo bo, HttpServletResponse response) {
        // 调用服务层查询列表数据
        List<TestTreeVo> list = testTreeService.queryList(bo);
        // 使用ExcelUtil导出Excel文件
        // 参数：数据列表、Sheet名称、目标类、响应对象
        ExcelUtil.exportExcel(list, "测试树表", TestTreeVo.class, response);
    }

    /**
     * 获取测试树表详细信息
     * 根据ID查询单条记录
     * 需要demo:tree:query权限
     *
     * @param id 主键ID
     * @return 统一响应结果，包含详细信息
     */
    // Sa-Token权限校验：需要demo:tree:query权限
    @SaCheckPermission("demo:tree:query")
    // Spring注解：指定GET请求方法，路径为/{id}
    @GetMapping("/{id}")
    // 获取测试树表详细信息
    public R<TestTreeVo> getInfo(@NotNull(message = "主键不能为空")
                                 @PathVariable("id") Long id) {
        // 调用服务层查询单条记录
        // 返回R.ok()包装的结果
        return R.ok(testTreeService.queryById(id));
    }

    /**
     * 新增测试树表
     * 添加一条新记录
     * 需要demo:tree:add权限，记录操作日志
     * 防重复提交控制
     *
     * @param bo 业务对象
     * @return 统一响应结果
     */
    // Sa-Token权限校验：需要demo:tree:add权限
    @SaCheckPermission("demo:tree:add")
    // 操作日志注解：记录新增操作日志
    @Log(title = "测试树表", businessType = BusinessType.INSERT)
    // 防重复提交注解：默认1秒内不允许重复提交
    @RepeatSubmit
    // Spring注解：指定POST请求方法，路径为空
    @PostMapping()
    // 新增测试树表
    public R<Void> add(@Validated(AddGroup.class) @RequestBody TestTreeBo bo) {
        // 调用服务层插入方法，返回影响行数
        // 使用AddGroup校验组验证新增参数
        // toAjax()将行数转换为R响应（>0为成功）
        return toAjax(testTreeService.insertByBo(bo));
    }

    /**
     * 修改测试树表
     * 更新已有记录
     * 需要demo:tree:edit权限，记录操作日志
     * 防重复提交控制
     *
     * @param bo 业务对象
     * @return 统一响应结果
     */
    // Sa-Token权限校验：需要demo:tree:edit权限
    @SaCheckPermission("demo:tree:edit")
    // 操作日志注解：记录修改操作日志
    @Log(title = "测试树表", businessType = BusinessType.UPDATE)
    // 防重复提交注解：默认1秒内不允许重复提交
    @RepeatSubmit
    // Spring注解：指定PUT请求方法，路径为空
    @PutMapping()
    // 修改测试树表
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody TestTreeBo bo) {
        // 调用服务层更新方法，返回影响行数
        // 使用EditGroup校验组验证编辑参数
        return toAjax(testTreeService.updateByBo(bo));
    }

    /**
     * 删除测试树表
     * 批量删除记录
     * 需要demo:tree:remove权限，记录操作日志
     *
     * @param ids 主键ID数组
     * @return 统一响应结果
     */
    // Sa-Token权限校验：需要demo:tree:remove权限
    @SaCheckPermission("demo:tree:remove")
    // 操作日志注解：记录删除操作日志
    @Log(title = "测试树表", businessType = BusinessType.DELETE)
    // Spring注解：指定DELETE请求方法，路径为/{ids}
    @DeleteMapping("/{ids}")
    // 删除测试树表
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        // 调用服务层删除方法，返回影响行数
        // Arrays.asList将数组转换为List
        // true表示进行逻辑删除校验
        return toAjax(testTreeService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
