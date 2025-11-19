// 定义包路径，将当前类归类到系统服务实现层
package org.dromara.system.service.impl;

// 引入Hutool的集合工具类，用于判断集合是否为空等操作
import cn.hutool.core.collection.CollUtil;
// 引入Hutool的对象工具类，用于判断对象是否为空
import cn.hutool.core.util.ObjectUtil;
// 引入MyBatis-Plus的Lambda查询包装器，支持类型安全的字段引用
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 引入MyBatis-Plus的Wrappers工具类，快速创建查询包装器
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// 引入MyBatis-Plus的分页插件，用于物理分页查询
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 引入Lombok的RequiredArgsConstructor注解，自动生成包含final字段的构造函数
import lombok.RequiredArgsConstructor;
// 引入系统常量定义，包含状态值等常量
import org.dromara.common.core.constant.SystemConstants;
// 引入服务异常类，用于抛出业务异常
import org.dromara.common.core.exception.ServiceException;
// 引入Mapstruct工具类，用于对象之间的转换
import org.dromara.common.core.utils.MapstructUtils;
// 引入字符串工具类，用于字符串操作
import org.dromara.common.core.utils.StringUtils;
// 引入分页查询实体类，封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 引入表格数据信息类，封装分页查询结果
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 引入租户实体类
import org.dromara.system.domain.SysTenant;
// 引入租户套餐实体类
import org.dromara.system.domain.SysTenantPackage;
// 引入租户套餐业务对象，用于接收前端传来的套餐操作参数
import org.dromara.system.domain.bo.SysTenantPackageBo;
// 引入租户套餐视图对象，用于返回给前端的套餐信息
import org.dromara.system.domain.vo.SysTenantPackageVo;
// 引入租户Mapper，用于查询租户信息
import org.dromara.system.mapper.SysTenantMapper;
// 引入租户套餐Mapper，用于数据库操作
import org.dromara.system.mapper.SysTenantPackageMapper;
// 引入租户套餐服务接口，定义套餐相关的业务方法
import org.dromara.system.service.ISysTenantPackageService;
// 引入Spring的Service注解，标识该类为服务层组件
import org.springframework.stereotype.Service;
// 引入Spring的事务注解，确保方法在事务中执行
import org.springframework.transaction.annotation.Transactional;

// 引入Arrays工具类，用于数组操作
import java.util.Arrays;
// 引入Collection集合接口
import java.util.Collection;
// 引入List集合接口
import java.util.List;

/**
 * 租户套餐Service业务层处理
 * 负责管理系统中租户套餐的CRUD操作、状态管理、唯一性校验等业务逻辑
 * 租户套餐定义了不同租户可使用的菜单权限集合，实现SaaS平台的功能分级
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring注解：标识该类为服务层组件，交由Spring容器管理
@Service
public class SysTenantPackageServiceImpl implements ISysTenantPackageService {

    // 租户套餐Mapper，用于数据库CRUD操作
    private final SysTenantPackageMapper baseMapper;
    // 租户Mapper，用于查询租户信息，校验套餐是否被使用
    private final SysTenantMapper tenantMapper;

    /**
     * 根据套餐ID查询租户套餐详情
     * 使用MyBatis-Plus的selectVoById方法，自动将实体转换为VO对象
     *
     * @param packageId 套餐ID
     * @return 租户套餐视图对象
     */
    @Override
    public SysTenantPackageVo queryById(Long packageId){
        // 调用Mapper的selectVoById方法，查询并转换结果
        return baseMapper.selectVoById(packageId);
    }

    /**
     * 分页查询租户套餐列表
     * 支持动态条件查询和分页，返回TableDataInfo格式数据
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据包装对象
     */
    @Override
    public TableDataInfo<SysTenantPackageVo> queryPageList(SysTenantPackageBo bo, PageQuery pageQuery) {
        // 构建查询条件包装器
        LambdaQueryWrapper<SysTenantPackage> lqw = buildQueryWrapper(bo);
        // 执行分页查询，pageQuery.build()创建Page对象，selectVoPage自动转换VO
        Page<SysTenantPackageVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将Page对象转换为TableDataInfo，统一响应格式
        return TableDataInfo.build(result);
    }

    /**
     * 查询所有启用的租户套餐列表
     * 用于前端下拉框等场景，只返回状态正常的套餐
     *
     * @return 租户套餐视图对象列表
     */
    @Override
    public List<SysTenantPackageVo> selectList() {
        // 使用LambdaQueryWrapper链式调用，查询状态为正常的套餐
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysTenantPackage>()
                .eq(SysTenantPackage::getStatus, SystemConstants.NORMAL));
    }

    /**
     * 查询租户套餐列表（不分页）
     * 根据条件查询所有匹配的套餐数据
     *
     * @param bo 查询条件业务对象
     * @return 租户套餐视图对象列表
     */
    @Override
    public List<SysTenantPackageVo> queryList(SysTenantPackageBo bo) {
        // 构建查询条件
        LambdaQueryWrapper<SysTenantPackage> lqw = buildQueryWrapper(bo);
        // 执行查询并返回列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建查询条件包装器
     * 根据业务对象动态生成查询条件，支持模糊查询和精确查询
     *
     * @param bo 查询条件业务对象
     * @return LambdaQueryWrapper查询包装器
     */
    private LambdaQueryWrapper<SysTenantPackage> buildQueryWrapper(SysTenantPackageBo bo) {
        // 创建LambdaQueryWrapper实例，使用Wrappers工具类
        LambdaQueryWrapper<SysTenantPackage> lqw = Wrappers.lambdaQuery();
        // 套餐名称模糊查询，StringUtils.isNotBlank确保参数不为空
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), SysTenantPackage::getPackageName, bo.getPackageName());
        // 状态精确查询
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysTenantPackage::getStatus, bo.getStatus());
        // 按packageId升序排序
        lqw.orderByAsc(SysTenantPackage::getPackageId);
        // 返回构建好的查询包装器
        return lqw;
    }

    /**
     * 新增租户套餐
     * 将菜单ID数组转换为逗号分隔字符串存储，保证事务一致性
     *
     * @param bo 租户套餐业务对象
     * @return 是否新增成功
     */
    @Override
    // Spring事务注解：确保方法在事务中执行，发生异常时回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SysTenantPackageBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysTenantPackage add = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id：将前端传来的菜单ID数组转换为List
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        // 使用Hutool的CollUtil判断集合是否为空，不为空则转换为逗号分隔字符串
        add.setMenuIds(CollUtil.isNotEmpty(menuIds) ? StringUtils.joinComma(menuIds) : "");
        // 执行插入操作，返回影响的行数
        boolean flag = baseMapper.insert(add) > 0;
        // 如果插入成功，将生成的ID回写到业务对象
        if (flag) {
            bo.setPackageId(add.getPackageId());
        }
        // 返回操作结果
        return flag;
    }

    /**
     * 修改租户套餐
     * 更新套餐信息，包括菜单ID的重新设置
     *
     * @param bo 租户套餐业务对象
     * @return 是否修改成功
     */
    @Override
    // Spring事务注解：确保方法在事务中执行，发生异常时回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SysTenantPackageBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysTenantPackage update = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id：将前端传来的菜单ID数组转换为List
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        // 使用Hutool的CollUtil判断集合是否为空，不为空则转换为逗号分隔字符串
        update.setMenuIds(CollUtil.isNotEmpty(menuIds) ? StringUtils.joinComma(menuIds) : "");
        // 执行更新操作，返回影响的行数
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 校验套餐名称是否唯一
     * 用于新增和修改时的重复性校验，排除当前记录
     *
     * @param bo 租户套餐业务对象
     * @return 是否唯一（true：唯一，false：重复）
     */
    @Override
    public boolean checkPackageNameUnique(SysTenantPackageBo bo) {
        // 查询是否存在同名套餐，使用exists方法提高性能
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysTenantPackage>()
            .eq(SysTenantPackage::getPackageName, bo.getPackageName())
            // 如果packageId不为空，则排除当前记录（修改场景）
            .ne(ObjectUtil.isNotNull(bo.getPackageId()), SysTenantPackage::getPackageId, bo.getPackageId()));
        // 返回是否唯一（取反）
        return !exist;
    }

    /**
     * 修改套餐状态
     * 单独更新套餐状态字段
     *
     * @param bo 套餐信息
     * @return 影响的行数
     */
    @Override
    public int updatePackageStatus(SysTenantPackageBo bo) {
        // 使用Mapstruct将BO转换为实体对象
        SysTenantPackage tenantPackage = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 执行更新操作，返回影响的行数
        return baseMapper.updateById(tenantPackage);
    }

    /**
     * 批量删除租户套餐
     * 删除前校验套餐是否被租户使用，保证数据完整性
     *
     * @param ids 需要删除的套餐ID集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    // Spring事务注解：确保方法在事务中执行，发生异常时回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 如果需要进行有效性校验
        if(isValid){
            // 查询是否有租户正在使用这些套餐
            boolean exists = tenantMapper.exists(new LambdaQueryWrapper<SysTenant>().in(SysTenant::getPackageId, ids));
            // 如果存在使用中的套餐，抛出业务异常
            if (exists) {
                throw new ServiceException("租户套餐已被使用");
            }
        }
        // 执行批量删除操作，返回影响的行数
        return baseMapper.deleteByIds(ids) > 0;
    }
}
