package org.dromara.common.translation.constant;

/**
 * 翻译常量
 *
 * @author Lion Li
 */
// 翻译常量接口，定义各种翻译类型的常量值
public interface TransConstant {

    /**
     * 用户id转账号
     */
    // 用户ID转用户名的翻译类型常量，用于@Translation注解的type属性
    String USER_ID_TO_NAME = "user_id_to_name";

    /**
     * 用户id转用户名称
     */
    // 用户ID转用户昵称的翻译类型常量
    String USER_ID_TO_NICKNAME = "user_id_to_nickname";

    /**
     * 部门id转名称
     */
    // 部门ID转部门名称的翻译类型常量
    String DEPT_ID_TO_NAME = "dept_id_to_name";

    /**
     * 字典type转label
     */
    // 字典类型转字典标签的翻译类型常量
    String DICT_TYPE_TO_LABEL = "dict_type_to_label";

    /**
     * ossId转url
     */
    // OSS文件ID转URL地址的翻译类型常量
    String OSS_ID_TO_URL = "oss_id_to_url";

}
