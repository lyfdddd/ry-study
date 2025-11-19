// OSS对象存储控制器，提供文件上传、下载、查询和删除功能
package org.dromara.system.controller.system;


// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 查询分组校验
import org.dromara.common.core.validate.QueryGroup;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// OSS对象存储业务对象
import org.dromara.system.domain.bo.SysOssBo;
// OSS上传响应视图对象
import org.dromara.system.domain.vo.SysOssUploadVo;
// OSS对象存储视图对象
import org.dromara.system.domain.vo.SysOssVo;
// OSS对象存储服务接口
import org.dromara.system.service.ISysOssService;
// Servlet响应对象，用于文件下载
import jakarta.servlet.http.HttpServletResponse;
// 非空校验注解，用于数组参数
import jakarta.validation.constraints.NotEmpty;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// HTTP媒体类型枚举
import org.springframework.http.MediaType;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;
// 文件上传对象
import org.springframework.web.multipart.MultipartFile;

// Java IO异常类
import java.io.IOException;
// Java数组工具类
import java.util.Arrays;
// Java集合类
import java.util.List;

/**
 * OSS对象存储控制器
 * 提供文件上传、下载、查询和删除功能
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
// 请求路径映射，所有接口前缀为/resource/oss
@RequestMapping("/resource/oss")
public class SysOssController extends BaseController {

    // OSS对象存储服务接口，自动注入
    private final ISysOssService ossService;

    /**
     * 查询OSS对象存储列表
     * 分页查询OSS对象存储记录，支持条件筛选
     */
    // Sa-Token权限校验，需要system:oss:list权限
    @SaCheckPermission("system:oss:list")
    // GET请求映射，路径为/resource/oss/list
    @GetMapping("/list")
    public TableDataInfo<SysOssVo> list(@Validated(QueryGroup.class) SysOssBo bo, PageQuery pageQuery) {
        // 调用服务层分页查询OSS对象存储列表
        return ossService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询OSS对象基于id串
     * 根据ID数组批量查询OSS对象存储信息
     *
     * @param ossIds OSS对象ID数组
     */
    // Sa-Token权限校验，需要system:oss:query权限
    @SaCheckPermission("system:oss:query")
    // GET请求映射，路径为/resource/oss/listByIds/{ossIds}
    @GetMapping("/listByIds/{ossIds}")
    public R<List<SysOssVo>> listByIds(@NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] ossIds) {
        // 调用服务层根据ID列表查询OSS对象存储信息
        List<SysOssVo> list = ossService.listByIds(Arrays.asList(ossIds));
        // 返回OSS对象存储列表
        return R.ok(list);
    }

    /**
     * 上传OSS对象存储
     * 上传文件到OSS对象存储，并返回上传结果
     *
     * @param file 上传的文件
     */
    // Sa-Token权限校验，需要system:oss:upload权限
    @SaCheckPermission("system:oss:upload")
    // 操作日志注解，记录业务操作，标题为"OSS对象存储"，类型为新增
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    // POST请求映射，路径为/resource/oss/upload，指定消费multipart/form-data类型数据
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file) {
        // 校验文件是否为空
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        // 调用服务层上传文件到OSS
        SysOssVo oss = ossService.upload(file);
        // 创建上传响应VO
        SysOssUploadVo uploadVo = new SysOssUploadVo();
        // 设置文件访问URL
        uploadVo.setUrl(oss.getUrl());
        // 设置原始文件名
        uploadVo.setFileName(oss.getOriginalName());
        // 设置OSS记录ID
        uploadVo.setOssId(oss.getOssId().toString());
        // 返回上传结果
        return R.ok(uploadVo);
    }

    /**
     * 下载OSS对象
     * 根据OSS对象ID下载文件
     *
     * @param ossId OSS对象ID
     */
    // Sa-Token权限校验，需要system:oss:download权限
    @SaCheckPermission("system:oss:download")
    // GET请求映射，路径为/resource/oss/download/{ossId}
    @GetMapping("/download/{ossId}")
    public void download(@PathVariable Long ossId, HttpServletResponse response) throws IOException {
        // 调用服务层下载OSS对象到响应流
        ossService.download(ossId, response);
    }

    /**
     * 删除OSS对象存储
     * 批量删除OSS对象存储记录和文件
     *
     * @param ossIds OSS对象ID数组
     */
    // Sa-Token权限校验，需要system:oss:remove权限
    @SaCheckPermission("system:oss:remove")
    // 操作日志注解，记录业务操作，标题为"OSS对象存储"，类型为删除
    @Log(title = "OSS对象存储", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/resource/oss/{ossIds}，支持多个id用逗号分隔
    @DeleteMapping("/{ossIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ossIds) {
        // 调用服务层批量删除OSS对象存储，并返回操作结果
        return toAjax(ossService.deleteWithValidByIds(List.of(ossIds), true));
    }

}
