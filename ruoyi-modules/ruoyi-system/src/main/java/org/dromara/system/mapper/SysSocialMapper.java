// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：社会化关系实体
// SysSocial是系统社会化关系实体类，对应数据库的sys_social表，存储第三方社交账号绑定信息
import org.dromara.system.domain.SysSocial;
// 系统视图对象：社会化关系视图对象
// SysSocialVo是社会化关系的视图对象，用于返回给前端的社交账号绑定信息
import org.dromara.system.domain.vo.SysSocialVo;

/**
 * 社会化关系Mapper接口
 * 继承BaseMapperPlus，提供社会化关系表的CRUD操作和VO转换能力
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责第三方社交账号绑定信息的增删改查操作，支持多平台登录
 *
 * @author thiszhc
 */
// 继承BaseMapperPlus，泛型参数：SysSocial表示实体类型，SysSocialVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysSocialMapper extends BaseMapperPlus<SysSocial, SysSocialVo> {

}
