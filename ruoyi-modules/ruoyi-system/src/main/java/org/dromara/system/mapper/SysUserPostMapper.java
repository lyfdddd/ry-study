// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：用户与岗位关联实体
// SysUserPost是系统用户与岗位关联实体类，对应数据库的sys_user_post表，存储用户和岗位的关联关系
import org.dromara.system.domain.SysUserPost;

/**
 * 用户与岗位关联表 数据层
 * 继承BaseMapperPlus，提供用户与岗位关联表的CRUD操作
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责用户和岗位关联关系的增删改查操作，用于用户职位管理
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysUserPost表示实体类型，第二个SysUserPost表示视图对象类型（与实体相同）
// BaseMapperPlus提供基础的CRUD方法，简化数据访问层开发
public interface SysUserPostMapper extends BaseMapperPlus<SysUserPost, SysUserPost> {

}
