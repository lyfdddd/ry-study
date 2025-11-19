package org.dromara.common.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务受让人数据传输对象（DTO）
 * 用于工作流任务分配场景，封装任务处理人信息和总数
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class TaskAssigneeDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     * 任务处理人列表的总数量
     */
    private Long total = 0L;

    /**
     * 任务处理人列表
     * 存储TaskHandler对象的列表
     */
    private List<TaskHandler> list;

    /**
     * 构造方法
     * 创建TaskAssigneeDTO对象并初始化total和list
     *
     * @param total 总记录数
     * @param list 任务处理人列表
     */
    public TaskAssigneeDTO(Long total, List<TaskHandler> list) {
        this.total = total;
        this.list = list;
    }

    /**
     * 将源列表转换为TaskHandler列表
     * 通用转换方法，支持从任意类型的源列表中提取字段构建TaskHandler
     *
     * @param <T> 源列表元素的通用类型
     * @param sourceList 待转换的源列表
     * @param storageId 提取storageId的函数
     * @param handlerCode 提取handlerCode的函数
     * @param handlerName 提取handlerName的函数
     * @param groupName 提取groupName的函数
     * @param createTimeMapper 提取createTime的函数
     * @return 转换后的TaskHandler列表
     */
    public static <T> List<TaskHandler> convertToHandlerList(
        List<T> sourceList,
        Function<T, String> storageId,
        Function<T, String> handlerCode,
        Function<T, String> handlerName,
        Function<T, String> groupName,
        Function<T, Date> createTimeMapper) {
        // 使用Stream API将源列表转换为TaskHandler列表
        return sourceList.stream()
            .map(item -> new TaskHandler(
                storageId.apply(item),
                handlerCode.apply(item),
                handlerName.apply(item),
                groupName.apply(item),
                createTimeMapper.apply(item)
            )).collect(Collectors.toList());
    }

    /**
     * 任务处理人内部静态类
     * 封装单个任务处理人的详细信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskHandler {

        /**
         * 主键
         * 任务处理人的唯一标识
         */
        private String storageId;

        /**
         * 权限编码
         * 任务处理人的权限编码
         */
        private String handlerCode;

        /**
         * 权限名称
         * 任务处理人的名称
         */
        private String handlerName;

        /**
         * 权限分组
         * 任务处理人所属的分组
         */
        private String groupName;

        /**
         * 创建时间
         * 任务处理人的创建时间
         */
        private Date createTime;
    }

}
