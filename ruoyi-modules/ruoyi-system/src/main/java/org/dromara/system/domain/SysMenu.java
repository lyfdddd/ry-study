// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，@TableField指定字段属性
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;
// 系统常量类：系统相关常量定义
// SystemConstants包含系统级别的常量，如菜单类型、是否框架等
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.SystemConstants;
// 字符串工具类：提供字符串操作工具方法
// StringUtils提供字符串判空、格式化、替换等工具方法
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus基础实体类：提供基础字段（createBy、createTime、updateBy、updateTime）
// BaseEntity是MyBatis-Plus提供的基础实体类，包含创建人、创建时间、更新人、更新时间等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;

// Java列表接口和实现类
// List是列表接口，ArrayList是列表实现类
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限表 sys_menu
 * 继承BaseEntity，继承基础字段（createBy、createTime、updateBy、updateTime）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_menu表，存储系统菜单和权限信息
 * 支持树形结构，通过parentId实现菜单层级关系
 * 包含路由、组件、权限等前端路由相关配置
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_menu
@TableName("sys_menu")
// 菜单实体类，继承基础实体类
public class SysMenu extends BaseEntity {

    /**
     * 菜单ID
     * 主键字段，使用@TableId注解标记
     * 对应数据库的menu_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为menu_id
    @TableId(value = "menu_id")
    // 菜单ID，Long类型
    private Long menuId;

    /**
     * 父菜单ID
     * 父菜单的外键，用于构建菜单树形结构
     * 顶级菜单的parentId为0
     * 对应数据库的parent_id字段
     */
    // 父菜单ID，Long类型
    private Long parentId;

    /**
     * 菜单名称
     * 菜单的显示名称，用于界面展示
     * 对应数据库的menu_name字段
     */
    // 菜单名称，String类型
    private String menuName;

    /**
     * 显示顺序
     * 菜单的排序号，用于界面显示顺序
     * 对应数据库的order_num字段
     */
    // 显示顺序，Integer类型
    private Integer orderNum;

    /**
     * 路由地址
     * 前端路由地址，对应Vue Router的path
     * 如：/system/user、/monitor/online
     * 对应数据库的path字段
     */
    // 路由地址，String类型
    private String path;

    /**
     * 组件路径
     * 前端组件路径，对应Vue组件的相对路径
     * 如：system/user/index、monitor/online/index
     * 对应数据库的component字段
     */
    // 组件路径，String类型
    private String component;

    /**
     * 路由参数
     * 路由参数，用于传递额外参数
     * 对应数据库的query_param字段
     */
    // 路由参数，String类型
    private String queryParam;

    /**
     * 是否为外链（0是 1否）
     * 是否外链标志，控制菜单是否在新窗口打开
     * 0表示是外链，1表示不是外链
     * 对应数据库的is_frame字段
     */
    // 是否为外链，String类型
    private String isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     * 是否缓存标志，控制路由组件是否缓存
     * 0表示缓存，1表示不缓存
     * 对应数据库的is_cache字段
     */
    // 是否缓存，String类型
    private String isCache;

    /**
     * 类型（M目录 C菜单 F按钮）
     * 菜单类型，区分目录、菜单、按钮
     * M表示目录（Directory），C表示菜单（Menu），F表示按钮（Button）
     * 对应数据库的menu_type字段
     */
    // 菜单类型，String类型
    private String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     * 显示状态，控制菜单是否在侧边栏显示
     * 0表示显示，1表示隐藏
     * 对应数据库的visible字段
     */
    // 显示状态，String类型
    private String visible;

    /**
     * 菜单状态（0正常 1停用）
     * 菜单状态，控制菜单是否可用
     * 0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 菜单状态，String类型
    private String status;

    /**
     * 权限字符串
     * 权限标识，用于后端接口权限控制
     * 格式如：system:user:list、monitor:online:query
     * 对应数据库的perms字段
     */
    // 权限字符串，String类型
    private String perms;

    /**
     * 菜单图标
     * 菜单图标，使用SVG图标名称
     * 对应数据库的icon字段
     */
    // 菜单图标，String类型
    private String icon;

    /**
     * 备注
     * 菜单备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

    /**
     * 父菜单名称
     * 父菜单的显示名称，用于树形结构展示
     * 使用@TableField(exist = false)标记为非数据库字段
     * 通过关联查询动态填充
     */
    // MyBatis-Plus注解：标记为非数据库字段，exist=false表示该字段不存在于数据库表中
    @TableField(exist = false)
    // 父菜单名称，String类型
    private String parentName;

    /**
     * 子菜单
     * 子菜单列表，用于树形结构展示
     * 使用@TableField(exist = false)标记为非数据库字段
     * 通过关联查询动态填充
     */
    // MyBatis-Plus注解：标记为非数据库字段，exist=false表示该字段不存在于数据库表中
    @TableField(exist = false)
    // 子菜单列表，List类型，初始化为空列表
    private List<SysMenu> children = new ArrayList<>();

    /**
     * 获取路由名称
     * 根据路径生成路由名称，首字母大写
     * 如果是菜单内部跳转，返回空字符串
     *
     * @return 路由名称
     */
    // 获取路由名称方法
    public String getRouteName() {
        // 将路径首字母大写作为路由名称
        String routerName = StringUtils.capitalize(path);
        // 非外链并且是一级目录（类型为目录）
        // 如果是菜单内部跳转，返回空字符串
        if (isMenuFrame()) {
            routerName = StringUtils.EMPTY;
        }
        // 返回路由名称
        return routerName;
    }

    /**
     * 获取路由地址
     * 根据菜单类型和路径生成路由地址
     * 处理内链、目录、菜单等不同情况
     *
     * @return 路由地址
     */
    // 获取路由地址方法
    public String getRouterPath() {
        // 默认路由地址为当前路径
        String routerPath = this.path;
        // 内链打开外网方式
        // 如果是子菜单且是内链，替换特殊字符
        if (getParentId() != 0L && isInnerLink()) {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        // 如果是顶级目录且类型为目录且不是外链，添加/前缀
        if (0L == getParentId() && SystemConstants.TYPE_DIR.equals(getMenuType())
            && SystemConstants.NO_FRAME.equals(getIsFrame())) {
            routerPath = "/" + this.path;
        }
        // 非外链并且是一级目录（类型为菜单）
        // 如果是菜单内部跳转，路由地址为/
        else if (isMenuFrame()) {
            routerPath = "/";
        }
        // 返回路由地址
        return routerPath;
    }

    /**
     * 获取组件信息
     * 根据菜单类型和配置生成组件路径
     * 处理布局、内链、父视图等不同情况
     *
     * @return 组件路径
     */
    // 获取组件信息方法
    public String getComponentInfo() {
        // 默认组件为LAYOUT布局组件
        String component = SystemConstants.LAYOUT;
        // 如果组件路径不为空且不是菜单内部跳转，使用配置的组件路径
        if (StringUtils.isNotEmpty(this.component) && !isMenuFrame()) {
            component = this.component;
        }
        // 如果组件路径为空且是子菜单且是内链，使用INNER_LINK内链组件
        else if (StringUtils.isEmpty(this.component) && getParentId() != 0L && isInnerLink()) {
            component = SystemConstants.INNER_LINK;
        }
        // 如果组件路径为空且是父视图，使用PARENT_VIEW父视图组件
        else if (StringUtils.isEmpty(this.component) && isParentView()) {
            component = SystemConstants.PARENT_VIEW;
        }
        // 返回组件路径
        return component;
    }

    /**
     * 是否为菜单内部跳转
     * 判断条件：顶级菜单、类型为菜单、不是外链
     *
     * @return 如果是菜单内部跳转返回true，否则返回false
     */
    // 判断是否为菜单内部跳转
    public boolean isMenuFrame() {
        // 判断条件：父菜单ID为0（顶级菜单）且类型为菜单且不是外链
        return getParentId() == 0L && SystemConstants.TYPE_MENU.equals(menuType) && isFrame.equals(SystemConstants.NO_FRAME);
    }

    /**
     * 是否为内链组件
     * 判断条件：不是外链且路径是http或https开头
     *
     * @return 如果是内链组件返回true，否则返回false
     */
    // 判断是否为内链组件
    public boolean isInnerLink() {
        // 判断条件：不是外链且路径是http或https开头
        return isFrame.equals(SystemConstants.NO_FRAME) && StringUtils.ishttp(path);
    }

    /**
     * 是否为parent_view组件
     * 判断条件：子菜单且类型为目录
     *
     * @return 如果是parent_view组件返回true，否则返回false
     */
    // 判断是否为parent_view组件
    public boolean isParentView() {
        // 判断条件：父菜单ID不为0（子菜单）且类型为目录
        return getParentId() != 0L && SystemConstants.TYPE_DIR.equals(menuType);
    }

    /**
     * 内链域名特殊字符替换
     * 将http、https、www、.、:等特殊字符替换为空字符串或/
     * 用于处理内链地址，使其符合路由规范
     *
     * @param path 原始路径
     * @return 替换后的路径
     */
    // 静态方法：内链域名特殊字符替换
    public static String innerLinkReplaceEach(String path) {
        // 使用StringUtils的replaceEach方法批量替换特殊字符
        // 将http、https、www替换为空字符串，将.和:替换为/
        return StringUtils.replaceEach(path, new String[]{Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
            new String[]{"", "", "", "/", "/"});
    }
}
