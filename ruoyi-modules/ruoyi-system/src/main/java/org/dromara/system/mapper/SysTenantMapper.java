// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// 系统领域实体类：租户实体
// SysTenant是系统租户实体类，对应数据库的sys_tenant表，存储租户信息
import org.dromara.system.domain.SysTenant;
// 系统视图对象：租户视图对象
// SysTenantVo是租户的视图对象，用于返回给前端的租户信息
import org.dromara.system.domain.vo.SysTenantVo;
// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 租户Mapper接口
 * 继承BaseMapperPlus，提供租户表的CRUD操作和VO转换能力
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责租户信息的增删改查操作，用于多租户管理
 *
 * @author Michelle.Chung
 */
// 继承BaseMapperPlus，泛型参数：SysTenant表示实体类型，SysTenantVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysTenantMapper extends BaseMapperPlus<SysTenant, SysTenantVo> {

}
