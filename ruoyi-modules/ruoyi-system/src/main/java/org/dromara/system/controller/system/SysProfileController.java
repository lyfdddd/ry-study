// 个人信息控制器，提供用户个人信息查询、修改、密码重置和头像上传功能
package org.dromara.system.controller.system;

// Hutool Bean工具类，用于对象转换
import cn.hutool.core.bean.BeanUtil;
// Hutool文件工具类，用于获取文件扩展名
import cn.hutool.core.io.FileUtil;
// Hutool BCrypt加密工具类，用于密码加密和验证
import cn.hutool.crypto.digest.BCrypt;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 文件MIME类型工具类
import org.dromara.common.core.utils.file.MimeTypeUtils;
// API加密注解，对请求响应进行加解密
import org.dromara.common.encrypt.annotation.ApiEncrypt;
// 防重复提交注解
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 数据权限助手，用于临时忽略数据权限
import org.dromara.common.mybatis.helper.DataPermissionHelper;
// 登录助手，获取当前登录用户信息
import org.dromara.common.satoken.utils.LoginHelper;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 用户业务对象
import org.dromara.system.domain.bo.SysUserBo;
// 用户密码业务对象
import org.dromara.system.domain.bo.SysUserPasswordBo;
// 用户个人信息业务对象
import org.dromara.system.domain.bo.SysUserProfileBo;
// 用户个人信息视图对象
import org.dromara.system.domain.vo.ProfileUserVo;
// OSS对象存储视图对象
import org.dromara.system.domain.vo.SysOssVo;
// 用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// OSS服务接口
import org.dromara.system.service.ISysOssService;
// 用户服务接口
import org.dromara.system.service.ISysUserService;
// Spring HTTP媒体类型
import org.springframework.http.MediaType;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;
// 文件上传对象
import org.springframework.web.multipart.MultipartFile;

// Java数组工具类
import java.util.Arrays;

/**
 * 个人信息业务处理控制器
 * 提供用户个人信息查询、修改、密码重置和头像上传功能
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
// 请求路径映射，所有接口前缀为/system/user/profile
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {

    // 用户服务接口，自动注入
    private final ISysUserService userService;
    // OSS服务接口，自动注入
    private final ISysOssService ossService;

    /**
     * 个人信息查询
     * 获取当前登录用户的详细信息，包括角色组和岗位组
     */
    // GET请求映射，路径为/system/user/profile
    @GetMapping
    public R<ProfileVo> profile() {
        // 查询当前登录用户的详细信息
        SysUserVo user = userService.selectUserById(LoginHelper.getUserId());
        // 查询用户所属角色组（多个角色用逗号分隔）
        String roleGroup = userService.selectUserRoleGroup(user.getUserId());
        // 查询用户所属岗位组（多个岗位用逗号分隔）
        String postGroup = userService.selectUserPostGroup(user.getUserId());
        // 单独做一个vo专门给个人中心用，避免数据被脱敏（如手机号、邮箱等）
        ProfileUserVo profileUser = BeanUtil.toBean(user, ProfileUserVo.class);
        // 封装个人信息视图对象
        ProfileVo profileVo = new ProfileVo(profileUser, roleGroup, postGroup);
        // 返回个人信息
        return R.ok(profileVo);
    }

    /**
     * 修改用户信息
     * 更新当前登录用户的个人信息（手机号、邮箱等）
     */
    // 防重复提交注解，防止重复修改
    @RepeatSubmit
    // 操作日志注解，记录业务操作，标题为"个人信息"，类型为更新
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    // PUT请求映射，路径为/system/user/profile
    @PutMapping
    public R<Void> updateProfile(@Validated @RequestBody SysUserProfileBo profile) {
        // 将ProfileBo转换为UserBo
        SysUserBo user = BeanUtil.toBean(profile, SysUserBo.class);
        // 设置用户ID为当前登录用户ID
        user.setUserId(LoginHelper.getUserId());
        // 获取当前登录用户名
        String username = LoginHelper.getUsername();
        // 校验手机号唯一性
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + username + "'失败，手机号码已存在");
        }
        // 校验邮箱唯一性
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + username + "'失败，邮箱账号已存在");
        }
        // 临时忽略数据权限更新用户个人信息（个人中心允许修改自己的信息）
        int rows = DataPermissionHelper.ignore(() -> userService.updateUserProfile(user));
        // 如果更新成功返回成功
        if (rows > 0) {
            return R.ok();
        }
        // 更新失败返回错误信息
        return R.fail("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     * 修改当前登录用户的登录密码
     *
     * @param bo 新旧密码
     */
    // 防重复提交注解，防止重复修改
    @RepeatSubmit
    // API加密注解，对请求响应进行加解密，保护密码安全
    @ApiEncrypt
    // 操作日志注解，记录业务操作，标题为"个人信息"，类型为更新
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    // PUT请求映射，路径为/system/user/profile/updatePwd
    @PutMapping("/updatePwd")
    public R<Void> updatePwd(@Validated @RequestBody SysUserPasswordBo bo) {
        // 查询当前登录用户的详细信息
        SysUserVo user = userService.selectUserById(LoginHelper.getUserId());
        // 获取用户当前密码（BCrypt加密后的）
        String password = user.getPassword();
        // 验证旧密码是否正确
        if (!BCrypt.checkpw(bo.getOldPassword(), password)) {
            return R.fail("修改密码失败，旧密码错误");
        }
        // 验证新密码不能与旧密码相同
        if (BCrypt.checkpw(bo.getNewPassword(), password)) {
            return R.fail("新密码不能与旧密码相同");
        }
        // 临时忽略数据权限重置用户密码（个人中心允许修改自己的密码）
        int rows = DataPermissionHelper.ignore(() -> userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(bo.getNewPassword())));
        // 如果更新成功返回成功
        if (rows > 0) {
            return R.ok();
        }
        // 更新失败返回错误信息
        return R.fail("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     * 上传并更新用户头像
     *
     * @param avatarfile 用户头像文件
     */
    // 防重复提交注解，防止重复上传
    @RepeatSubmit
    // 操作日志注解，记录业务操作，标题为"用户头像"，类型为更新
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    // POST请求映射，路径为/system/user/profile/avatar，指定消费multipart/form-data类型
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<AvatarVo> avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        // 如果文件不为空
        if (!avatarfile.isEmpty()) {
            // 获取文件扩展名
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            // 校验文件格式是否为图片
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
                return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
            }
            // 调用OSS服务上传头像文件
            SysOssVo oss = ossService.upload(avatarfile);
            // 获取头像URL
            String avatar = oss.getUrl();
            // 临时忽略数据权限更新用户头像（个人中心允许修改自己的头像）
            boolean updateSuccess = DataPermissionHelper.ignore(() -> userService.updateUserAvatar(LoginHelper.getUserId(), oss.getOssId()));
            // 如果更新成功返回头像URL
            if (updateSuccess) {
                return R.ok(new AvatarVo(avatar));
            }
        }
        // 上传失败返回错误信息
        return R.fail("上传图片异常，请联系管理员");
    }

    /**
     * 用户头像信息记录类
     * 封装用户头像URL信息
     *
     * @param imgUrl 头像地址
     */
    public record AvatarVo(String imgUrl) {}

    /**
     * 用户个人信息记录类
     * 封装用户个人信息、角色组和岗位组
     *
     * @param user      用户信息
     * @param roleGroup 用户所属角色组
     * @param postGroup 用户所属岗位组
     */
    public record ProfileVo(ProfileUserVo user, String roleGroup, String postGroup) {}

}
