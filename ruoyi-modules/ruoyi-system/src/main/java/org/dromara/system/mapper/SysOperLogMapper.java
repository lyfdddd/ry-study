// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：操作日志实体
// SysOperLog是系统操作日志实体类，对应数据库的sys_oper_log表，存储用户操作日志信息
import org.dromara.system.domain.SysOperLog;
// 系统视图对象：操作日志视图对象
// SysOperLogVo是操作日志的视图对象，用于返回给前端的操作日志信息
import org.dromara.system.domain.vo.SysOperLogVo;

/**
 * 操作日志 数据层
 * 继承BaseMapperPlus，提供操作日志表的CRUD操作和VO转换能力
 * 使用BaseMapperPlus提供的增强方法，简化数据访问层开发
 * 主要负责用户操作日志的增删改查操作，用于审计和安全分析
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysOperLog表示实体类型，SysOperLogVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysOperLogMapper extends BaseMapperPlus<SysOperLog, SysOperLogVo> {

}
