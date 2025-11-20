// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Hutool工具类：对象工具类，用于判断对象是否为空
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus条件构造器：LambdaQueryWrapper提供类型安全的查询条件构建
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心工具类：Mapstruct对象转换工具，用于DTO/VO/Entity之间的转换
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 系统领域实体类：社会化关系实体
import org.dromara.system.domain.SysSocial;
// 系统业务对象类：社会化关系业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysSocialBo;
// 系统视图对象类：社会化关系视图对象，用于返回前端数据
import org.dromara.system.domain.vo.SysSocialVo;
// 系统数据访问层：社会化关系Mapper接口
import org.dromara.system.mapper.SysSocialMapper;
// 系统服务接口：社会化关系服务接口
import org.dromara.system.service.ISysSocialService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合类：List集合
import java.util.List;

/**
 * 社会化关系Service业务层处理
 * 负责处理用户与第三方平台（微信、QQ、微博等）的授权绑定关系
 * 实现第三方登录账号的增删改查操作
 *
 * @author thiszhc
 * @date 2023-06-12
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysSocialServiceImpl implements ISysSocialService {

    // 社会化关系Mapper接口，用于数据库操作
    // 使用final修饰，通过构造函数注入（Lombok自动生成）
    private final SysSocialMapper baseMapper;


    /**
     * 查询社会化关系
     * 根据ID查询单条社会化关系记录
     *
     * @param id 主键ID
     * @return 社会化关系视图对象
     */
    @Override
    public SysSocialVo queryById(String id) {
        // 调用Mapper的selectVoById方法，自动将实体转换为VO对象返回
        // MyBatis-Plus Plus支持Vo转换，避免手动转换
        return baseMapper.selectVoById(id);
    }

    /**
     * 授权列表
     * 根据查询条件查询社会化关系列表
     *
     * @param bo 查询条件业务对象
     * @return 社会化关系视图对象列表
     */
    @Override
    public List<SysSocialVo> queryList(SysSocialBo bo) {
        // 构建LambdaQueryWrapper，使用Lambda表达式避免硬编码字段名
        // 条件1：如果userId不为空，则添加userId等于条件
        // 条件2：如果authId不为空，则添加authId等于条件
        // 条件3：如果source不为空，则添加source等于条件
        LambdaQueryWrapper<SysSocial> lqw = new LambdaQueryWrapper<SysSocial>()
            .eq(ObjectUtil.isNotNull(bo.getUserId()), SysSocial::getUserId, bo.getUserId())
            .eq(StringUtils.isNotBlank(bo.getAuthId()), SysSocial::getAuthId, bo.getAuthId())
            .eq(StringUtils.isNotBlank(bo.getSource()), SysSocial::getSource, bo.getSource());
        // 调用Mapper的selectVoList方法，自动将实体列表转换为VO列表返回
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 根据用户ID查询社会化关系列表
     * 查询指定用户的所有第三方授权信息
     *
     * @param userId 用户ID
     * @return 社会化关系视图对象列表
     */
    @Override
    public List<SysSocialVo> queryListByUserId(Long userId) {
        // 构建LambdaQueryWrapper，添加userId等于条件
        // 调用Mapper的selectVoList方法查询并转换结果
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysSocial>().eq(SysSocial::getUserId, userId));
    }


    /**
     * 新增社会化关系
     * 添加用户与第三方平台的授权绑定关系
     *
     * @param bo 社会化关系业务对象
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SysSocialBo bo) {
        // 使用Mapstruct将BO对象转换为实体对象
        // Mapstruct在编译期生成转换代码，性能优于BeanUtils
        SysSocial add = MapstructUtils.convert(bo, SysSocial.class);
        // 保存前数据校验
        validEntityBeforeSave(add);
        // 调用Mapper的insert方法插入数据，返回影响的行数
        // 判断插入是否成功（影响行数大于0）
        boolean flag = baseMapper.insert(add) > 0;
        // 如果插入成功
        if (flag) {
            // 检查实体对象是否不为空
            if (add != null) {
                // 将生成的ID回写到BO对象，供后续使用
                bo.setId(add.getId());
            } else {
                // 如果实体为空，返回false表示失败
                return false;
            }
        }
        // 返回插入结果
        return flag;
    }

    /**
     * 更新社会化关系
     * 修改用户与第三方平台的授权绑定关系
     *
     * @param bo 社会化关系业务对象
     * @return 是否更新成功
     */
    @Override
    public Boolean updateByBo(SysSocialBo bo) {
        // 使用Mapstruct将BO对象转换为实体对象
        SysSocial update = MapstructUtils.convert(bo, SysSocial.class);
        // 保存前数据校验
        validEntityBeforeSave(update);
        // 调用Mapper的updateById方法根据ID更新数据，返回影响的行数
        // 判断更新是否成功（影响行数大于0）
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     * 在插入或更新前进行数据合法性校验
     *
     * @param entity 社会化关系实体
     */
    private void validEntityBeforeSave(SysSocial entity) {
        // TODO 预留方法，后续可添加唯一约束校验等逻辑
        // 例如：校验同一用户同一平台不能重复绑定
    }


    /**
     * 删除社会化关系
     * 删除用户与第三方平台的授权绑定关系
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidById(Long id) {
        // 调用Mapper的deleteById方法根据ID删除数据，返回影响的行数
        // 判断删除是否成功（影响行数大于0）
        return baseMapper.deleteById(id) > 0;
    }


    /**
     * 根据 authId 查询用户信息
     * 根据第三方平台的认证ID查询授权信息
     *
     * @param authId 认证id
     * @return 授权信息
     */
    @Override
    public List<SysSocialVo> selectByAuthId(String authId) {
        // 构建LambdaQueryWrapper，添加authId等于条件
        // 调用Mapper的selectVoList方法查询并转换结果
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysSocial>().eq(SysSocial::getAuthId, authId));
    }

}
