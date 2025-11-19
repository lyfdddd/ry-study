// 分页查询参数实体类，封装前端分页、排序参数
// 提供build()方法构建MyBatis-Plus的Page对象，支持多字段排序
package org.dromara.common.mybatis.core.page;

// Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// Hutool对象工具类，用于判空和默认值处理
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus排序项
import com.baomidou.mybatisplus.core.metadata.OrderItem;
// MyBatis-Plus分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Jackson注解，标记字段不序列化
import com.fasterxml.jackson.annotation.JsonIgnore;
// Lombok注解，自动生成getter、setter、toString等方法
import lombok.Data;
// 业务异常类
import org.dromara.common.core.exception.ServiceException;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// SQL工具类，用于SQL注入防护
import org.dromara.common.core.utils.sql.SqlUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询实体类
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// 实现Serializable接口，支持序列化
public class PageQuery implements Serializable {

    // 序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页大小，每页显示记录数
     */
    private Integer pageSize;

    /**
     * 当前页数，从1开始
     */
    private Integer pageNum;

    /**
     * 排序列，支持多个字段，用逗号分隔
     * 如：id,createTime
     */
    private String orderByColumn;

    /**
     * 排序方向，desc或asc
     * 支持单个值或多个值，用逗号分隔
     * 如：asc 或 asc,desc
     */
    private String isAsc;

    /**
     * 当前记录起始索引默认值，第一页
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 每页显示记录数默认值，默认查全部（Integer.MAX_VALUE）
     */
    public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;

    /**
     * 构建分页对象
     */
    // 构建MyBatis-Plus的Page对象，设置分页参数和排序规则
    public <T> Page<T> build() {
        // 获取页码，如果为null使用默认值1
        Integer pageNum = ObjectUtil.defaultIfNull(getPageNum(), DEFAULT_PAGE_NUM);
        // 获取每页大小，如果为null使用默认值Integer.MAX_VALUE（查询全部）
        Integer pageSize = ObjectUtil.defaultIfNull(getPageSize(), DEFAULT_PAGE_SIZE);
        // 页码不能小于等于0，否则设置为默认值1
        if (pageNum <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        // 创建Page对象，设置当前页和每页大小
        Page<T> page = new Page<>(pageNum, pageSize);
        // 构建排序规则
        List<OrderItem> orderItems = buildOrderItem();
        // 如果排序规则不为空，添加到Page对象
        if (CollUtil.isNotEmpty(orderItems)) {
            page.addOrder(orderItems);
        }
        return page;
    }

    /**
     * 构建排序
     *
     * 支持的用法如下:
     * {isAsc:"asc",orderByColumn:"id"} order by id asc
     * {isAsc:"asc",orderByColumn:"id,createTime"} order by id asc,create_time asc
     * {isAsc:"desc",orderByColumn:"id,createTime"} order by id desc,create_time desc
     * {isAsc:"asc,desc",orderByColumn:"id,createTime"} order by id asc,create_time desc
     */
    // 私有方法：构建排序规则列表
    // 支持单字段排序和多字段混合排序
    private List<OrderItem> buildOrderItem() {
        // 如果排序列为空或排序方向为空，返回null
        if (StringUtils.isBlank(orderByColumn) || StringUtils.isBlank(isAsc)) {
            return null;
        }
        // SQL注入防护：对排序列进行转义，防止恶意SQL注入
        String orderBy = SqlUtil.escapeOrderBySql(orderByColumn);
        // 将驼峰命名转换为下划线命名，适配数据库字段
        orderBy = StringUtils.toUnderScoreCase(orderBy);

        // 兼容前端排序类型：将ascending/descending转换为asc/desc
        isAsc = StringUtils.replaceEach(isAsc, new String[]{"ascending", "descending"}, new String[]{"asc", "desc"});

        // 按逗号分割排序列
        String[] orderByArr = orderBy.split(StringUtils.SEPARATOR);
        // 按逗号分割排序方向
        String[] isAscArr = isAsc.split(StringUtils.SEPARATOR);
        // 校验参数：如果排序方向不是1个，且数量与排序列不匹配，抛出异常
        if (isAscArr.length != 1 && isAscArr.length != orderByArr.length) {
            throw new ServiceException("排序参数有误");
        }

        // 创建排序规则列表
        List<OrderItem> list = new ArrayList<>();
        // 遍历每个字段，构建排序规则
        for (int i = 0; i < orderByArr.length; i++) {
            String orderByStr = orderByArr[i];
            // 如果只有一个排序方向，所有字段使用同一个方向；否则每个字段使用对应的方向
            String isAscStr = isAscArr.length == 1 ? isAscArr[0] : isAscArr[i];
            // 根据排序方向创建升序或降序排序项
            if ("asc".equals(isAscStr)) {
                list.add(OrderItem.asc(orderByStr));
            } else if ("desc".equals(isAscStr)) {
                list.add(OrderItem.desc(orderByStr));
            } else {
                // 排序方向不是asc或desc，抛出异常
                throw new ServiceException("排序参数有误");
            }
        }
        return list;
    }

    // Jackson注解：序列化时忽略此字段
    @JsonIgnore
    // 获取当前页的起始记录索引，用于手动分页
    public Integer getFirstNum() {
        // 计算起始索引：(当前页-1) * 每页大小
        return (pageNum - 1) * pageSize;
    }

    // 构造函数：创建分页查询对象
    public PageQuery(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

}
