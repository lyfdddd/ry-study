// 测试单表服务实现层
// 实现ITestDemoService接口，提供测试单表的业务逻辑处理
package org.dromara.demo.service.impl;

// MyBatis-Plus Lambda查询包装器，用于构建类型安全的查询条件
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus查询包装器工具类，提供快速创建LambdaQueryWrapper的方法
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件，提供分页查询能力
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解，自动生成构造函数（基于final字段）
import lombok.RequiredArgsConstructor;
// 服务异常类，用于抛出业务异常
import org.dromara.common.core.exception.ServiceException;
// MapStruct对象转换工具，实现BO与Entity之间的转换
import org.dromara.common.core.utils.MapstructUtils;
// 字符串工具类，提供字符串判空、比较等操作
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页查询对象，封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类，包含total、rows等分页信息
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 测试单表实体类，对应数据库表
import org.dromara.demo.domain.TestDemo;
// 测试单表业务对象，封装前端传入的参数
import org.dromara.demo.domain.bo.TestDemoBo;
// 测试单表视图对象，返回给前端的数据格式
import org.dromara.demo.domain.vo.TestDemoVo;
// 测试单表Mapper接口，提供数据库操作能力
import org.dromara.demo.mapper.TestDemoMapper;
// 测试单表服务接口
import org.dromara.demo.service.ITestDemoService;
// Spring服务注解，标记为服务层组件
import org.springframework.stereotype.Service;

// Java集合接口，表示集合类型
import java.util.Collection;
// Java List接口
import java.util.List;
// Java Map接口
import java.util.Map;

/**
 * 测试单表Service业务层处理
 * 实现ITestDemoService接口，提供测试单表的业务逻辑处理
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Lombok注解，自动生成构造函数（基于final字段）
@RequiredArgsConstructor
// Spring服务注解，标记为服务层组件
@Service
// 测试单表服务实现类
public class TestDemoServiceImpl implements ITestDemoService {

    // 测试单表Mapper接口，提供数据库操作能力
    // 使用final修饰，由Lombok自动生成构造函数注入
    private final TestDemoMapper baseMapper;

    /**
     * 根据ID查询单个测试单表记录
     * 调用Mapper的selectVoById方法，返回视图对象
     *
     * @param id 主键ID
     * @return 测试单表视图对象
     */
    @Override
    public TestDemoVo queryById(Long id) {
        // 调用Mapper的selectVoById方法，返回视图对象
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询测试单表列表
     * 根据业务对象条件构建查询包装器，调用Mapper的分页查询方法
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据表格信息
     */
    @Override
    public TableDataInfo<TestDemoVo> queryPageList(TestDemoBo bo, PageQuery pageQuery) {
        // 构建查询条件包装器
        LambdaQueryWrapper<TestDemo> lqw = buildQueryWrapper(bo);
        // 调用Mapper的selectVoPage方法进行分页查询
        Page<TestDemoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将分页结果转换为TableDataInfo对象
        return TableDataInfo.build(result);
    }

    /**
     * 自定义分页查询
     * 使用自定义SQL实现分页查询，演示自定义Mapper方法
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据表格信息
     */
    @Override
    public TableDataInfo<TestDemoVo> customPageList(TestDemoBo bo, PageQuery pageQuery) {
        // 构建查询条件包装器
        LambdaQueryWrapper<TestDemo> lqw = buildQueryWrapper(bo);
        // 调用Mapper的自定义分页查询方法
        Page<TestDemoVo> result = baseMapper.customPageList(pageQuery.build(), lqw);
        // 将分页结果转换为TableDataInfo对象
        return TableDataInfo.build(result);
    }

    /**
     * 查询测试单表列表（不分页）
     * 根据业务对象条件查询所有匹配记录
     *
     * @param bo 查询条件业务对象
     * @return 测试单表视图对象列表
     */
    @Override
    public List<TestDemoVo> queryList(TestDemoBo bo) {
        // 调用Mapper的selectVoList方法查询列表
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    /**
     * 构建查询条件包装器
     * 根据业务对象的条件动态构建LambdaQueryWrapper
     *
     * @param bo 查询条件业务对象
     * @return LambdaQueryWrapper对象
     */
    private LambdaQueryWrapper<TestDemo> buildQueryWrapper(TestDemoBo bo) {
        // 获取业务对象中的params参数（用于时间范围查询）
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper对象
        LambdaQueryWrapper<TestDemo> lqw = Wrappers.lambdaQuery();
        // 模糊查询testKey字段（非空时生效）
        lqw.like(StringUtils.isNotBlank(bo.getTestKey()), TestDemo::getTestKey, bo.getTestKey());
        // 精确查询value字段（非空时生效）
        lqw.eq(StringUtils.isNotBlank(bo.getValue()), TestDemo::getValue, bo.getValue());
        // 时间范围查询createTime字段（beginCreateTime和endCreateTime都不为空时生效）
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            TestDemo::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        // 按id字段升序排序
        lqw.orderByAsc(TestDemo::getId);
        // 返回构建好的查询包装器
        return lqw;
    }

    /**
     * 根据业务对象插入测试单表记录
     * 将业务对象转换为实体类，校验后插入数据库
     *
     * @param bo 测试单表业务对象
     * @return 是否插入成功（true-成功，false-失败）
     */
    @Override
    public Boolean insertByBo(TestDemoBo bo) {
        // 使用MapStruct将业务对象转换为实体类
        TestDemo add = MapstructUtils.convert(bo, TestDemo.class);
        // 保存前数据校验
        validEntityBeforeSave(add);
        // 调用Mapper的insert方法插入数据
        boolean flag = baseMapper.insert(add) > 0;
        // 如果插入成功，将生成的主键ID回写到业务对象
        if (flag) {
            bo.setId(add.getId());
        }
        // 返回插入结果
        return flag;
    }

    /**
     * 根据业务对象更新测试单表记录
     * 将业务对象转换为实体类，校验后更新数据库
     *
     * @param bo 测试单表业务对象
     * @return 是否更新成功（true-成功，false-失败）
     */
    @Override
    public Boolean updateByBo(TestDemoBo bo) {
        // 使用MapStruct将业务对象转换为实体类
        TestDemo update = MapstructUtils.convert(bo, TestDemo.class);
        // 保存前数据校验
        validEntityBeforeSave(update);
        // 调用Mapper的updateById方法更新数据
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     * 预留方法，用于添加唯一约束等业务校验逻辑
     *
     * @param entity 实体类数据
     */
    private void validEntityBeforeSave(TestDemo entity) {
        // TODO 做一些数据校验,如唯一约束
        // 例如：检查testKey是否唯一
        // LambdaQueryWrapper<TestDemo> lqw = Wrappers.lambdaQuery();
        // lqw.eq(TestDemo::getTestKey, entity.getTestKey());
        // if (baseMapper.exists(lqw)) {
        //     throw new ServiceException("testKey已存在");
        // }
    }

    /**
     * 校验并删除数据
     * 根据isValid参数决定是否进行删除前校验
     *
     * @param ids     主键集合
     * @param isValid 是否校验,true-删除前校验,false-不校验
     * @return 是否删除成功（true-成功，false-失败）
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 如果需要校验
        if (isValid) {
            // 查询要删除的记录
            List<TestDemo> list = baseMapper.selectByIds(ids);
            // 如果查询到的记录数与传入的ID数不一致，说明部分记录不存在或无权访问
            if (list.size() != ids.size()) {
                // 抛出服务异常，提示无删除权限
                throw new ServiceException("您没有删除权限!");
            }
        }
        // 调用Mapper的deleteByIds方法批量删除
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 批量保存测试单表记录
     * 使用MyBatis-Plus的批量插入功能
     *
     * @param list 实体类列表
     * @return 是否保存成功（true-成功，false-失败）
     */
    @Override
    public Boolean saveBatch(List<TestDemo> list) {
        // 调用Mapper的insertBatch方法批量插入
        return baseMapper.insertBatch(list);
    }
}
