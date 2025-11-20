// 定义分页数据对象的包路径，属于MyBatis-Plus核心分页组件
package org.dromara.common.mybatis.core.page;

// Hutool集合工具类，提供集合操作便捷方法
import cn.hutool.core.collection.CollUtil;
// Hutool HTTP状态码常量
import cn.hutool.http.HttpStatus;
// MyBatis-Plus分页接口，包含分页数据和分页信息
import com.baomidou.mybatisplus.core.metadata.IPage;
// Lombok注解，自动生成getter、setter、toString等方法
import lombok.Data;
// Lombok注解，生成无参构造函数
import lombok.NoArgsConstructor;

// 序列化相关接口和类
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 * 统一后端分页返回格式，包含总记录数、当前页数据、状态码和消息
 * 支持泛型，可返回任意类型的数据列表
 * 与前端表格组件（如Element UI的el-table）配合使用
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
// 实现Serializable接口，支持对象序列化（用于缓存、分布式传输等场景）
public class TableDataInfo<T> implements Serializable {

    // 序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     * 符合查询条件的总记录数量，用于前端计算总页数
     */
    private long total;

    /**
     * 列表数据
     * 当前页的数据列表，类型为泛型T
     */
    private List<T> rows;

    /**
     * 消息状态码
     * HTTP状态码，如200表示成功
     */
    private int code;

    /**
     * 消息内容
     * 操作结果描述，如"查询成功"、"参数错误"等
     */
    private String msg;

    /**
     * 分页构造函数
     * 用于手动构建分页结果
     *
     * @param list  列表数据（当前页数据）
     * @param total 总记录数（符合查询条件的总数量）
     */
    public TableDataInfo(List<T> list, long total) {
        // 设置当前页数据列表
        this.rows = list;
        // 设置总记录数
        this.total = total;
        // 设置状态码为200（成功）
        this.code = HttpStatus.HTTP_OK;
        // 设置消息为"查询成功"
        this.msg = "查询成功";
    }

    /**
     * 根据MyBatis-Plus分页对象构建表格分页数据对象
     * 将MyBatis-Plus的IPage对象转换为统一的TableDataInfo格式
     *
     * @param page MyBatis-Plus分页对象，包含records和total
     * @param <T>  数据类型
     * @return 构造好的TableDataInfo对象
     */
    public static <T> TableDataInfo<T> build(IPage<T> page) {
        // 创建TableDataInfo对象
        TableDataInfo<T> rspData = new TableDataInfo<>();
        // 设置状态码为200（成功）
        rspData.setCode(HttpStatus.HTTP_OK);
        // 设置消息为"查询成功"
        rspData.setMsg("查询成功");
        // 设置当前页数据（从IPage获取）
        rspData.setRows(page.getRecords());
        // 设置总记录数（从IPage获取）
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    /**
     * 根据数据列表构建表格分页数据对象（不分页，返回全部数据）
     * 适用于数据量较小的场景，直接返回所有数据
     *
     * @param list 数据列表
     * @param <T>  数据类型
     * @return 构造好的TableDataInfo对象
     */
    public static <T> TableDataInfo<T> build(List<T> list) {
        // 创建TableDataInfo对象
        TableDataInfo<T> rspData = new TableDataInfo<>();
        // 设置状态码为200（成功）
        rspData.setCode(HttpStatus.HTTP_OK);
        // 设置消息为"查询成功"
        rspData.setMsg("查询成功");
        // 设置数据列表
        rspData.setRows(list);
        // 设置总记录数为列表大小（因为不分页，总记录数就是列表大小）
        rspData.setTotal(list.size());
        return rspData;
    }

    /**
     * 构建空的分页数据对象
     * 用于返回空数据场景
     *
     * @param <T> 数据类型
     * @return 构造好的空TableDataInfo对象
     */
    public static <T> TableDataInfo<T> build() {
        // 创建TableDataInfo对象
        TableDataInfo<T> rspData = new TableDataInfo<>();
        // 设置状态码为200（成功）
        rspData.setCode(HttpStatus.HTTP_OK);
        // 设置消息为"查询成功"
        rspData.setMsg("查询成功");
        // rows和total保持null/0，表示空数据
        return rspData;
    }

    /**
     * 根据原始数据列表和分页参数，构建表格分页数据对象（用于假分页）
     * 假分页：先查询出所有数据，然后在内存中进行分页
     * 适用于数据量较小（如几千条）的场景，减少数据库交互次数
     *
     * @param list 原始数据列表（全部数据）
     * @param page 分页参数对象（包含当前页码、每页大小等）
     * @param <T>  数据类型
     * @return 构造好的分页结果 TableDataInfo<T>
     */
    public static <T> TableDataInfo<T> build(List<T> list, IPage<T> page) {
        // 如果原始数据列表为空，返回空的分页对象
        if (CollUtil.isEmpty(list)) {
            return TableDataInfo.build();
        }
        // 使用Hutool的CollUtil.page方法进行内存分页
        // 参数：pageIndex（从0开始）、pageSize、数据列表
        List<T> pageList = CollUtil.page((int) page.getCurrent() - 1, (int) page.getSize(), list);
        // 返回分页结果，total为原始列表大小
        return new TableDataInfo<>(pageList, list.size());
    }

}
