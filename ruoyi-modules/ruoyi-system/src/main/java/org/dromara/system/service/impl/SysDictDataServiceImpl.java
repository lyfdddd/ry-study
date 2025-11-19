// 字典数据服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Redis缓存工具类：提供缓存操作工具
import org.dromara.common.redis.utils.CacheUtils;
// 系统领域模型：字典数据实体类
import org.dromara.system.domain.SysDictData;
// 系统业务对象：字典数据业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysDictDataBo;
// 系统视图对象：字典数据视图对象，用于返回前端数据
import org.dromara.system.domain.vo.SysDictDataVo;
// 系统Mapper接口：字典数据Mapper
import org.dromara.system.mapper.SysDictDataMapper;
// 字典数据服务接口
import org.dromara.system.service.ISysDictDataService;
// Spring缓存注解：缓存更新，用于更新缓存
import org.springframework.cache.annotation.CachePut;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.List;

/**
 * 字典数据服务实现类
 * 实现字典数据管理的核心业务逻辑，包括CRUD、缓存管理等
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysDictDataServiceImpl implements ISysDictDataService {

    // 字典数据Mapper，用于字典数据数据的持久化操作
    private final SysDictDataMapper baseMapper;

    /**
     * 分页查询字典数据列表
     * 根据查询条件分页查询字典数据列表
     *
     * @param dictData  字典数据查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 字典数据分页结果
     */
    @Override
    public TableDataInfo<SysDictDataVo> selectPageDictDataList(SysDictDataBo dictData, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysDictData> lqw = buildQueryWrapper(dictData);
        // 调用Mapper执行分页查询
        Page<SysDictDataVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件查询字典数据
     * 根据查询条件查询字典数据列表，不分页
     *
     * @param dictData 字典数据查询条件
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictDataVo> selectDictDataList(SysDictDataBo dictData) {
        // 构建查询条件
        LambdaQueryWrapper<SysDictData> lqw = buildQueryWrapper(dictData);
        // 调用Mapper查询列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建字典数据查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo 字典数据查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<SysDictData> buildQueryWrapper(SysDictDataBo bo) {
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查
        LambdaQueryWrapper<SysDictData> lqw = Wrappers.lambdaQuery();
        // 精确查询字典排序
        lqw.eq(bo.getDictSort() != null, SysDictData::getDictSort, bo.getDictSort());
        // 模糊查询字典标签
        lqw.like(StringUtils.isNotBlank(bo.getDictLabel()), SysDictData::getDictLabel, bo.getDictLabel());
        // 精确查询字典类型
        lqw.eq(StringUtils.isNotBlank(bo.getDictType()), SysDictData::getDictType, bo.getDictType());
        // 按字典排序和字典编码升序排序
        lqw.orderByAsc(SysDictData::getDictSort, SysDictData::getDictCode);
        return lqw;
    }

    /**
     * 根据字典类型和字典键值查询字典标签
     * 根据字典类型和字典值查询对应的字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        // 查询字典数据，只查询字典标签字段
        return baseMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
                .select(SysDictData::getDictLabel) // 只查询字典标签
                .eq(SysDictData::getDictType, dictType) // 字典类型相同
                .eq(SysDictData::getDictValue, dictValue)) // 字典值相同
            .getDictLabel(); // 返回字典标签
    }

    /**
     * 根据字典数据ID查询信息
     * 根据字典数据ID查询字典数据详情
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    @Override
    public SysDictDataVo selectDictDataById(Long dictCode) {
        // 调用Mapper查询VO对象
        return baseMapper.selectVoById(dictCode);
    }

    /**
     * 批量删除字典数据信息
     * 批量删除字典数据，并清除缓存
     *
     * @param dictCodes 需要删除的字典数据ID列表
     */
    @Override
    public void deleteDictDataByIds(List<Long> dictCodes) {
        // 查询要删除的字典数据列表
        List<SysDictData> list = baseMapper.selectByIds(dictCodes);
        // 批量删除
        baseMapper.deleteByIds(dictCodes);
        // 清除缓存
        list.forEach(x -> CacheUtils.evict(CacheNames.SYS_DICT, x.getDictType()));
    }

    /**
     * 新增保存字典数据信息
     * 新增字典数据，并更新缓存
     *
     * @param bo 字典数据信息
     * @return 字典数据列表
     */
    // Spring缓存注解：更新缓存，key为字典类型
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    public List<SysDictDataVo> insertDictData(SysDictDataBo bo) {
        // 将BO转换为实体对象
        SysDictData data = MapstructUtils.convert(bo, SysDictData.class);
        // 插入数据
        int row = baseMapper.insert(data);
        // 如果插入成功，返回该字典类型下的所有字典数据
        if (row > 0) {
            return baseMapper.selectDictDataByType(data.getDictType());
        }
        // 插入失败，抛出业务异常
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存字典数据信息
     * 修改字典数据，并更新缓存
     *
     * @param bo 字典数据信息
     * @return 字典数据列表
     */
    // Spring缓存注解：更新缓存，key为字典类型
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    public List<SysDictDataVo> updateDictData(SysDictDataBo bo) {
        // 将BO转换为实体对象
        SysDictData data = MapstructUtils.convert(bo, SysDictData.class);
        // 更新数据
        int row = baseMapper.updateById(data);
        // 如果更新成功，返回该字典类型下的所有字典数据
        if (row > 0) {
            return baseMapper.selectDictDataByType(data.getDictType());
        }
        // 更新失败，抛出业务异常
        throw new ServiceException("操作失败");
    }

    /**
     * 校验字典键值是否唯一
     * 检查同一字典类型下字典值是否重复
     *
     * @param dict 字典数据信息
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkDictDataUnique(SysDictDataBo dict) {
        // 查询是否存在相同字典类型和字典值的记录
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysDictData>()
            // 字典类型相同
            .eq(SysDictData::getDictType, dict.getDictType())
            // 字典值相同
            .eq(SysDictData::getDictValue, dict.getDictValue())
            // 排除当前字典数据ID（编辑时）
            .ne(ObjectUtil.isNotNull(dict.getDictCode()), SysDictData::getDictCode, dict.getDictCode()));
        // 返回是否唯一（不存在重复）
        return !exist;
    }

}
