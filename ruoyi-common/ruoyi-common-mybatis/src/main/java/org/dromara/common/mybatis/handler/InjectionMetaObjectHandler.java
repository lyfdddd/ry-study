// 定义MyBatis-Plus元对象处理器包路径，用于自动填充实体字段
package org.dromara.common.mybatis.handler;

// Hutool对象工具类，用于判空操作
import cn.hutool.core.util.ObjectUtil;
// Hutool HTTP状态码常量
import cn.hutool.http.HttpStatus;
// MyBatis-Plus元对象处理器接口，用于在插入/更新时自动填充字段
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// MyBatis元对象，提供对原始对象的反射操作能力
import org.apache.ibatis.reflection.MetaObject;
// 登录用户对象，包含用户ID、部门ID等信息
import org.dromara.common.core.domain.model.LoginUser;
// 业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 对象工具类，提供对象默认值处理
import org.dromara.common.core.utils.ObjectUtils;
// 实体基类，包含createBy、createTime等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;
// 登录助手工具类，提供获取当前登录用户的方法
import org.dromara.common.satoken.utils.LoginHelper;

// Java日期类，用于记录创建时间和更新时间
import java.util.Date;

/**
 * MyBatis-Plus元对象字段自动填充处理器
 * 实现MetaObjectHandler接口，在插入和更新操作时自动填充公共字段（创建人、创建时间、更新人、更新时间）
 * 解决每个实体类手动设置这些字段的重复代码问题
 *
 * @author Lion Li
 * @date 2021/4/25
 */
// Lombok日志注解：自动生成slf4j日志对象log，用于打印日志
@Slf4j
// 实现MyBatis-Plus的MetaObjectHandler接口
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    /**
     * 默认用户ID，当用户未登录时填充-1代表无用户
     * 用于标识系统操作或定时任务等无登录用户的场景
     */
    private static final Long DEFAULT_USER_ID = -1L;

    /**
     * 插入填充方法，用于在插入数据时自动填充实体对象中的创建时间、更新时间、创建人、更新人等信息
     * 该方法在MyBatis-Plus执行insert操作前自动调用
     *
     * @param metaObject 元对象，用于获取原始对象并进行填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            // 判断元对象不为空且原始对象是BaseEntity类型（包含公共字段的实体）
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 获取当前时间作为创建时间和更新时间
                // 如果实体中createTime字段已经有值（手动设置），则使用已有值，否则使用当前时间
                // 这样可以支持导入历史数据时保留原始创建时间
                Date current = ObjectUtils.notNull(baseEntity.getCreateTime(), new Date());
                // 设置创建时间
                baseEntity.setCreateTime(current);
                // 设置更新时间（插入时创建时间和更新时间相同）
                baseEntity.setUpdateTime(current);

                // 判断创建人字段是否为空，如果为空则自动填充当前登录用户
                if (ObjectUtil.isNull(baseEntity.getCreateBy())) {
                    // 获取当前登录用户信息
                    LoginUser loginUser = getLoginUser();
                    // 如果用户已登录
                    if (ObjectUtil.isNotNull(loginUser)) {
                        // 获取用户ID
                        Long userId = loginUser.getUserId();
                        // 填充创建人、更新人信息（插入时创建人和更新人相同）
                        baseEntity.setCreateBy(userId);
                        baseEntity.setUpdateBy(userId);
                        // 填充创建部门信息，如果实体中已有createDept则保留，否则使用用户所属部门
                        baseEntity.setCreateDept(ObjectUtils.notNull(baseEntity.getCreateDept(), loginUser.getDeptId()));
                    } else {
                        // 用户未登录（如定时任务、系统初始化等场景），填充默认用户ID
                        baseEntity.setCreateBy(DEFAULT_USER_ID);
                        baseEntity.setUpdateBy(DEFAULT_USER_ID);
                        // 填充创建部门为默认ID
                        baseEntity.setCreateDept(ObjectUtils.notNull(baseEntity.getCreateDept(), DEFAULT_USER_ID));
                    }
                }
            } else {
                // 如果实体不是BaseEntity类型，使用MyBatis-Plus的严格填充模式
                // 这种方式需要字段上标注@TableField(fill = FieldFill.INSERT)
                Date date = new Date();
                // 严格填充createTime字段，如果字段已有值则不覆盖
                this.strictInsertFill(metaObject, "createTime", Date.class, date);
                // 严格填充updateTime字段
                this.strictInsertFill(metaObject, "updateTime", Date.class, date);
            }
        } catch (Exception e) {
            // 捕获异常并转换为业务异常，返回401未授权状态码
            // 通常是因为用户未登录导致无法获取用户信息
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 更新填充方法，用于在更新数据时自动填充实体对象中的更新时间和更新人信息
     * 该方法在MyBatis-Plus执行update操作前自动调用
     *
     * @param metaObject 元对象，用于获取原始对象并进行填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            // 判断元对象不为空且原始对象是BaseEntity类型
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 获取当前时间作为更新时间
                // 更新操作总是使用当前时间，覆盖原有值
                Date current = new Date();
                // 设置更新时间
                baseEntity.setUpdateTime(current);

                // 获取当前登录用户的ID
                Long userId = LoginHelper.getUserId();
                // 如果用户已登录，填充更新人
                if (ObjectUtil.isNotNull(userId)) {
                    baseEntity.setUpdateBy(userId);
                } else {
                    // 用户未登录，填充默认用户ID
                    baseEntity.setUpdateBy(DEFAULT_USER_ID);
                }
            } else {
                // 如果实体不是BaseEntity类型，使用MyBatis-Plus的严格填充模式
                // 严格填充updateTime字段
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        } catch (Exception e) {
            // 捕获异常并转换为业务异常
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 获取当前登录用户信息
     * 封装try-catch，防止用户未登录时抛出异常影响业务逻辑
     *
     * @return 当前登录用户的信息，如果用户未登录则返回 null
     */
    private LoginUser getLoginUser() {
        LoginUser loginUser;
        try {
            // 调用LoginHelper获取当前登录用户
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            // 用户未登录或Token失效，返回null
            return null;
        }
        return loginUser;
    }

}
