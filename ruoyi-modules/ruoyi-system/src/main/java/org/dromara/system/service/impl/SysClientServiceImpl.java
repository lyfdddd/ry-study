// 客户端管理服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// Hutool工具类：加密工具，提供MD5、SHA等加密算法
import cn.hutool.crypto.SecureUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：Lambda更新包装器，支持类型安全更新
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok日志注解：自动生成slf4j的日志对象log
import lombok.extern.slf4j.Slf4j;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 系统领域模型：客户端实体类
import org.dromara.system.domain.SysClient;
// 系统业务对象：客户端业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysClientBo;
// 系统视图对象：客户端视图对象，用于返回前端数据
import org.dromara.system.domain.vo.SysClientVo;
// 系统Mapper接口：客户端Mapper
import org.dromara.system.mapper.SysClientMapper;
// 客户端服务接口
import org.dromara.system.service.ISysClientService;
// Spring缓存注解：缓存删除，用于删除缓存
import org.springframework.cache.annotation.CacheEvict;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.Collection;
// Java集合工具类：提供列表操作
import java.util.List;

/**
 * 客户端管理Service业务层处理
 * 负责管理系统中OAuth2客户端的注册、认证、授权类型配置等业务逻辑
 * 客户端是第三方应用接入系统的凭证，包含clientId、clientSecret等关键信息
 *
 * @author Michelle.Chung
 * @date 2023-06-18
 */
// Lombok日志注解：自动生成slf4j的日志对象log
@Slf4j
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring注解：标识该类为服务层组件，交由Spring容器管理
@Service
public class SysClientServiceImpl implements ISysClientService {

    // 客户端Mapper，用于数据库CRUD操作
    private final SysClientMapper baseMapper;

    /**
     * 根据ID查询客户端详情
     * 将数据库中存储的grantType字符串转换为List返回给前端
     *
     * @param id 客户端ID
     * @return 客户端视图对象
     */
    @Override
    public SysClientVo queryById(Long id) {
        // 调用Mapper的selectVoById方法查询并转换结果
        SysClientVo vo = baseMapper.selectVoById(id);
        // 将grantType字符串按逗号分割为List，方便前端展示
        vo.setGrantTypeList(StringUtils.splitList(vo.getGrantType()));
        // 返回视图对象
        return vo;
    }

    /**
     * 根据clientId查询客户端详情
     * 使用Spring Cache缓存查询结果，提高性能
     *
     * @param clientId 客户端ID
     * @return 客户端视图对象
     */
    // Spring缓存注解：将查询结果缓存到CacheNames.SYS_CLIENT中，key为clientId
    @Cacheable(cacheNames = CacheNames.SYS_CLIENT, key = "#clientId")
    @Override
    public SysClientVo queryByClientId(String clientId) {
        // 使用LambdaQueryWrapper构建查询条件，查询clientId匹配的客户端
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysClient>().eq(SysClient::getClientId, clientId));
    }

    /**
     * 分页查询客户端列表
     * 支持动态条件查询和分页，将grantType字符串转换为List
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据包装对象
     */
    @Override
    public TableDataInfo<SysClientVo> queryPageList(SysClientBo bo, PageQuery pageQuery) {
        // 构建查询条件包装器
        LambdaQueryWrapper<SysClient> lqw = buildQueryWrapper(bo);
        // 执行分页查询，pageQuery.build()创建Page对象，selectVoPage自动转换VO
        Page<SysClientVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 遍历结果集，将每个记录的grantType字符串转换为List
        result.getRecords().forEach(r -> r.setGrantTypeList(StringUtils.splitList(r.getGrantType())));
        // 将Page对象转换为TableDataInfo，统一响应格式
        return TableDataInfo.build(result);
    }

    /**
     * 查询客户端列表（不分页）
     * 根据条件查询所有匹配的客户端数据
     *
     * @param bo 查询条件业务对象
     * @return 客户端视图对象列表
     */
    @Override
    public List<SysClientVo> queryList(SysClientBo bo) {
        // 构建查询条件
        LambdaQueryWrapper<SysClient> lqw = buildQueryWrapper(bo);
        // 执行查询并返回列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建查询条件包装器
     * 根据业务对象动态生成查询条件，支持精确查询
     *
     * @param bo 查询条件业务对象
     * @return LambdaQueryWrapper查询包装器
     */
    private LambdaQueryWrapper<SysClient> buildQueryWrapper(SysClientBo bo) {
        // 创建LambdaQueryWrapper实例，使用Wrappers工具类
        LambdaQueryWrapper<SysClient> lqw = Wrappers.lambdaQuery();
        // clientId精确查询，StringUtils.isNotBlank确保参数不为空
        lqw.eq(StringUtils.isNotBlank(bo.getClientId()), SysClient::getClientId, bo.getClientId());
        // clientKey精确查询
        lqw.eq(StringUtils.isNotBlank(bo.getClientKey()), SysClient::getClientKey, bo.getClientKey());
        // clientSecret精确查询
        lqw.eq(StringUtils.isNotBlank(bo.getClientSecret()), SysClient::getClientSecret, bo.getClientSecret());
        // 状态精确查询
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysClient::getStatus, bo.getStatus());
        // 按ID升序排序
        lqw.orderByAsc(SysClient::getId);
        // 返回构建好的查询包装器
        return lqw;
    }

    /**
     * 新增客户端
     * 将授权类型List转换为逗号分隔字符串存储
     * 使用MD5算法生成clientId，确保唯一性
     *
     * @param bo 客户端业务对象
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SysClientBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysClient add = MapstructUtils.convert(bo, SysClient.class);
        // 将授权类型List转换为逗号分隔字符串，使用Hutool的CollUtil.join方法
        add.setGrantType(CollUtil.join(bo.getGrantTypeList(), StringUtils.SEPARATOR));
        // 生成clientId：使用Hutool的SecureUtil.md5对clientKey和clientSecret进行MD5加密
        String clientKey = bo.getClientKey();
        String clientSecret = bo.getClientSecret();
        add.setClientId(SecureUtil.md5(clientKey + clientSecret));
        // 执行插入操作，返回影响的行数
        boolean flag = baseMapper.insert(add) > 0;
        // 如果插入成功，将生成的ID回写到业务对象
        if (flag) {
            bo.setId(add.getId());
        }
        // 返回操作结果
        return flag;
    }

    /**
     * 修改客户端
     * 更新客户端信息，清除缓存保证数据一致性
     * 将授权类型List转换为逗号分隔字符串存储
     *
     * @param bo 客户端业务对象
     * @return 是否修改成功
     */
    // Spring缓存注解：清除CacheNames.SYS_CLIENT中key为clientId的缓存
    @CacheEvict(cacheNames = CacheNames.SYS_CLIENT, key = "#bo.clientId")
    @Override
    public Boolean updateByBo(SysClientBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysClient update = MapstructUtils.convert(bo, SysClient.class);
        // 将授权类型List转换为逗号分隔字符串
        update.setGrantType(StringUtils.joinComma(bo.getGrantTypeList()));
        // 执行更新操作，返回影响的行数
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 修改客户端状态
     * 单独更新状态字段，清除缓存保证数据一致性
     *
     * @param clientId 客户端ID
     * @param status 状态值
     * @return 影响的行数
     */
    // Spring缓存注解：清除CacheNames.SYS_CLIENT中key为clientId的缓存
    @CacheEvict(cacheNames = CacheNames.SYS_CLIENT, key = "#clientId")
    @Override
    public int updateClientStatus(String clientId, String status) {
        // 使用LambdaUpdateWrapper构建更新条件，只更新status字段
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysClient>()
                // 设置status字段值
                .set(SysClient::getStatus, status)
                // 匹配clientId
                .eq(SysClient::getClientId, clientId));
    }

    /**
     * 批量删除客户端
     * 清除所有客户端缓存，保证数据一致性
     *
     * @param ids 需要删除的客户端ID集合
     * @param isValid 是否进行有效性校验（预留参数）
     * @return 是否删除成功
     */
    // Spring缓存注解：清除CacheNames.SYS_CLIENT中的所有缓存条目
    @CacheEvict(cacheNames = CacheNames.SYS_CLIENT, allEntries = true)
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 执行批量删除操作，返回影响的行数
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 校验客户端key是否唯一
     * 用于新增和修改时的重复性校验，排除当前记录
     *
     * @param client 客户端业务对象
     * @return 是否唯一（true：唯一，false：重复）
     */
    @Override
    public boolean checkClickKeyUnique(SysClientBo client) {
        // 查询是否存在相同clientKey的客户端，使用exists方法提高性能
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysClient>()
            // 匹配clientKey
            .eq(SysClient::getClientKey, client.getClientKey())
            // 如果ID不为空，则排除当前记录（修改场景）
            .ne(ObjectUtil.isNotNull(client.getId()), SysClient::getId, client.getId()));
        // 返回是否唯一（取反）
        return !exist;
    }

}
