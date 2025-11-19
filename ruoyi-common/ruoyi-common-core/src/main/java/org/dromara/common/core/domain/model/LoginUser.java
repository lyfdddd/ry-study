// 定义登录用户模型所在的包路径，属于common-core模块的domain.model包
package org.dromara.common.core.domain.model;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Lombok注解：生成无参构造方法
import lombok.NoArgsConstructor;
// 岗位数据传输对象（DTO）
import org.dromara.common.core.domain.dto.PostDTO;
// 角色数据传输对象（DTO）
import org.dromara.common.core.domain.dto.RoleDTO;

// Java序列化版本号注解（JDK 14+）
import java.io.Serial;
// Java序列化接口
import java.io.Serializable;
// Java列表接口
import java.util.List;
// Java集合接口（Set用于存储权限字符串）
import java.util.Set;

/**
 * 登录用户身份权限模型
 * 存储在Sa-Token的Session中，贯穿整个请求生命周期
 * 包含用户基本信息、权限、角色、岗位等完整上下文
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造方法（用于JSON反序列化）
@NoArgsConstructor
public class LoginUser implements Serializable {

    // 序列化版本号（类结构变更时需更新，避免反序列化失败）
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     * 多租户隔离标识（如：000000表示默认租户）
     */
    private String tenantId;

    /**
     * 用户ID
     * 系统用户表主键（sys_user.user_id）
     */
    private Long userId;

    /**
     * 部门ID
     * 用户所属部门（sys_dept.dept_id）
     */
    private Long deptId;

    /**
     * 部门类别编码
     * 用于数据权限控制（如：company、dept、group等）
     */
    private String deptCategory;

    /**
     * 部门名称
     * 用户所属部门名称（冗余字段，避免重复查询）
     */
    private String deptName;

    /**
     * 用户唯一标识（Token）
     * Sa-Token生成的Token值（如：satoken:login:token:xxx）
     */
    private String token;

    /**
     * 用户类型
     * 区分不同用户体系（如：sys_user、member_user等）
     */
    private String userType;

    /**
     * 登录时间戳
     * 用户登录时的时间（毫秒级时间戳）
     */
    private Long loginTime;

    /**
     * Token过期时间戳
     * Token失效时间（毫秒级时间戳，用于前端判断是否需要续期）
     */
    private Long expireTime;

    /**
     * 登录IP地址
     * 用户登录时的IP（如：192.168.1.100）
     */
    private String ipaddr;

    /**
     * 登录地点
     * 根据IP解析的地理位置（如：北京市 北京市）
     */
    private String loginLocation;

    /**
     * 浏览器类型
     * 用户登录时的浏览器（如：Chrome、Firefox、Safari等）
     */
    private String browser;

    /**
     * 操作系统
     * 用户登录时的操作系统（如：Windows 10、Mac OS X等）
     */
    private String os;

    /**
     * 菜单权限集合
     * 用户拥有的菜单权限（用于前端按钮级控制，如：system:user:add）
     * 格式：system:模块:操作（与数据库sys_menu.perms字段对应）
     */
    private Set<String> menuPermission;

    /**
     * 角色权限集合
     * 用户拥有的角色标识（用于后端接口鉴权，如：admin、common）
     * 格式：角色key（与数据库sys_role.role_key字段对应）
     */
    private Set<String> rolePermission;

    /**
     * 用户名
     * 登录账号（sys_user.user_name）
     */
    private String username;

    /**
     * 用户昵称
     * 显示名称（sys_user.nick_name）
     */
    private String nickname;

    /**
     * 角色对象列表
     * 用户拥有的完整角色信息（包含角色ID、名称、权限等）
     * 用于前端展示用户角色、权限计算等
     */
    private List<RoleDTO> roles;

    /**
     * 岗位对象列表
     * 用户拥有的完整岗位信息（包含岗位ID、名称、排序等）
     * 用于前端展示用户岗位、流程审批等
     */
    private List<PostDTO> posts;

    /**
     * 数据权限：当前角色ID
     * 用户当前切换的角色（用于数据权限控制）
     * 如果为null，则使用用户默认角色
     */
    private Long roleId;

    /**
     * 客户端标识
     * 区分不同客户端（如：PC、APP、小程序等）
     * 用于多端登录管理、权限控制等
     */
    private String clientKey;

    /**
     * 设备类型
     * 用户登录设备类型（如：pc、mobile、tablet等）
     * 用于设备管理、登录限制等
     */
    private String deviceType;

    /**
     * 获取登录ID（Sa-Token唯一标识）
     * 格式：userType:userId（如：sys_user:1）
     * 用于Sa-Token的Session管理、权限认证等
     *
     * @return 登录ID字符串
     * @throws IllegalArgumentException 如果userType或userId为空
     */
    public String getLoginId() {
        // 校验用户类型不能为空（防止NPE）
        if (userType == null) {
            throw new IllegalArgumentException("用户类型不能为空");
        }
        // 校验用户ID不能为空（防止NPE）
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 返回格式化的登录ID（用于Sa-Token的Session key）
        return userType + ":" + userId;
    }

}
