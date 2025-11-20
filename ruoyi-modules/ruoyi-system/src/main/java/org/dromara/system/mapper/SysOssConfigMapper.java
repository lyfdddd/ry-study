// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：对象存储配置实体
// SysOssConfig是系统对象存储配置实体类，对应数据库的sys_oss_config表，存储OSS配置信息
import org.dromara.system.domain.SysOssConfig;
// 系统视图对象：对象存储配置视图对象
// SysOssConfigVo是对象存储配置的视图对象，用于返回给前端的OSS配置信息
import org.dromara.system.domain.vo.SysOssConfigVo;

/**
 * 对象存储配置Mapper接口
 * 继承BaseMapperPlus，提供对象存储配置表的CRUD操作和VO转换能力
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责OSS配置信息的增删改查操作，支持多种云存储配置
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
// 继承BaseMapperPlus，泛型参数：SysOssConfig表示实体类型，SysOssConfigVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysOssConfigMapper extends BaseMapperPlus<SysOssConfig, SysOssConfigVo> {

}
