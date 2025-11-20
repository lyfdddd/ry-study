// 测试单表服务接口层
// 定义测试单表的业务逻辑方法，包括增删改查、分页查询、批量操作等
package org.dromara.demo.service;

// MyBatis-Plus分页查询对象，封装分页参数（页码、每页条数）
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类，包含total、rows等分页信息
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 测试单表实体类，对应数据库表
import org.dromara.demo.domain.TestDemo;
// 测试单表业务对象，封装前端传入的参数
import org.dromara.demo.domain.bo.TestDemoBo;
// 测试单表视图对象，返回给前端的数据格式
import org.dromara.demo.domain.vo.TestDemoVo;

// Java集合接口，表示集合类型
import java.util.Collection;
// Java List接口
import java.util.List;

/**
 * 测试单表Service接口
 * 定义测试单表的业务逻辑方法，包括增删改查、分页查询、批量操作等
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// 测试单表服务接口
public interface ITestDemoService {

    /**
     * 根据ID查询单个测试单表记录
     * 返回视图对象，隐藏敏感字段
     *
     * @param id 主键ID
     * @return 测试单表视图对象
     */
    // 根据ID查询单个记录
    TestDemoVo queryById(Long id);

    /**
     * 分页查询测试单表列表
     * 根据业务对象条件进行分页查询
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据表格信息
     */
    // 分页查询列表
    TableDataInfo<TestDemoVo> queryPageList(TestDemoBo bo, PageQuery pageQuery);

    /**
     * 自定义分页查询
     * 使用自定义SQL实现分页查询，演示自定义Mapper方法
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据表格信息
     */
    // 自定义分页查询
    TableDataInfo<TestDemoVo> customPageList(TestDemoBo bo, PageQuery pageQuery);

    /**
     * 查询测试单表列表（不分页）
     * 根据业务对象条件查询所有匹配记录
     *
     * @param bo 查询条件业务对象
     * @return 测试单表视图对象列表
     */
    // 查询列表（不分页）
    List<TestDemoVo> queryList(TestDemoBo bo);

    /**
     * 根据新增业务对象插入测试单表
     * 将业务对象转换为实体类并插入数据库
     *
     * @param bo 测试单表新增业务对象
     * @return 是否插入成功（true-成功，false-失败）
     */
    // 根据业务对象插入记录
    Boolean insertByBo(TestDemoBo bo);

    /**
     * 根据编辑业务对象修改测试单表
     * 将业务对象转换为实体类并更新数据库
     *
     * @param bo 测试单表编辑业务对象
     * @return 是否更新成功（true-成功，false-失败）
     */
    // 根据业务对象更新记录
    Boolean updateByBo(TestDemoBo bo);

    /**
     * 校验并删除数据
     * 根据isValid参数决定是否进行删除前校验
     *
     * @param ids     主键集合
     * @param isValid 是否校验,true-删除前校验,false-不校验
     * @return 是否删除成功（true-成功，false-失败）
     */
    // 校验并删除数据
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 批量保存测试单表记录
     * 使用MyBatis-Plus的批量插入功能
     *
     * @param list 实体类列表
     * @return 是否保存成功（true-成功，false-失败）
     */
    // 批量保存
    Boolean saveBatch(List<TestDemo> list);
}
