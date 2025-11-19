// 定义统一响应实体所在的包路径，属于common-core模块的domain包
package org.dromara.common.core.domain;

// HTTP状态码常量定义（200成功、500失败、601警告等）
import org.dromara.common.core.constant.HttpStatus;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Lombok注解：生成无参构造方法
import lombok.NoArgsConstructor;

// Java序列化版本号注解（JDK 14+）
import java.io.Serial;
// Java序列化接口
import java.io.Serializable;

/**
 * 统一响应信息主体（Result）
 * 所有Controller接口必须返回此对象，确保前端接收格式统一
 * 泛型T支持任意数据类型（列表、对象、分页等）
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造方法（用于JSON反序列化）
@NoArgsConstructor
public class R<T> implements Serializable {

    // 序列化版本号（类结构变更时需更新）
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码（HTTP 200）
     * 业务操作成功时返回
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码（HTTP 500）
     * 业务操作失败时返回
     */
    public static final int FAIL = 500;

    // 响应状态码（200/500/601等）
    private int code;

    // 响应消息（成功/失败的提示信息，支持国际化）
    private String msg;

    // 响应数据（泛型，可以是任意类型：List、Map、VO、分页对象等）
    private T data;

    /**
     * 返回成功响应（无数据）
     * 用于删除、更新等不需要返回数据的场景
     *
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> ok() {
        // 调用内部方法构建响应对象（code=200, msg="操作成功"）
        return restResult(null, SUCCESS, "操作成功");
    }

    /**
     * 返回成功响应（带数据）
     * 用于查询、详情等需要返回数据的场景
     *
     * @param data 响应数据
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> ok(T data) {
        // 调用内部方法构建响应对象（code=200, msg="操作成功"）
        return restResult(data, SUCCESS, "操作成功");
    }

    /**
     * 返回成功响应（自定义消息）
     * 用于需要自定义提示信息的场景
     *
     * @param msg 自定义消息
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> ok(String msg) {
        // 调用内部方法构建响应对象（code=200, data=null）
        return restResult(null, SUCCESS, msg);
    }

    /**
     * 返回成功响应（自定义消息+数据）
     * 用于需要自定义提示信息且返回数据的场景
     *
     * @param msg 自定义消息
     * @param data 响应数据
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> ok(String msg, T data) {
        // 调用内部方法构建响应对象（code=200）
        return restResult(data, SUCCESS, msg);
    }

    /**
     * 返回失败响应（默认消息）
     * 用于业务校验失败、异常等场景
     *
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> fail() {
        // 调用内部方法构建响应对象（code=500, msg="操作失败"）
        return restResult(null, FAIL, "操作失败");
    }

    /**
     * 返回失败响应（自定义消息）
     * 用于需要自定义错误提示的场景
     *
     * @param msg 错误消息
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> fail(String msg) {
        // 调用内部方法构建响应对象（code=500, data=null）
        return restResult(null, FAIL, msg);
    }

    /**
     * 返回失败响应（带数据）
     * 用于需要返回错误详情数据的场景（如校验错误列表）
     *
     * @param data 错误数据
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> fail(T data) {
        // 调用内部方法构建响应对象（code=500, msg="操作失败"）
        return restResult(data, FAIL, "操作失败");
    }

    /**
     * 返回失败响应（自定义消息+数据）
     * 用于需要自定义错误提示且返回错误数据的场景
     *
     * @param msg 错误消息
     * @param data 错误数据
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> fail(String msg, T data) {
        // 调用内部方法构建响应对象（code=500）
        return restResult(data, FAIL, msg);
    }

    /**
     * 返回失败响应（自定义状态码+消息）
     * 用于需要自定义HTTP状态码的场景（如401未授权、403禁止访问）
     *
     * @param code 自定义状态码
     * @param msg 错误消息
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    public static <T> R<T> fail(int code, String msg) {
        // 调用内部方法构建响应对象（data=null）
        return restResult(null, code, msg);
    }

    /**
     * 返回警告消息
     * 用于业务校验通过但需要提示的场景（如密码即将过期）
     *
     * @param msg 警告消息
     * @param <T> 泛型类型
     * @return 统一响应对象（code=601）
     */
    public static <T> R<T> warn(String msg) {
        // 调用内部方法构建响应对象（code=HttpStatus.WARN=601）
        return restResult(null, HttpStatus.WARN, msg);
    }

    /**
     * 返回警告消息（带数据）
     * 用于需要返回数据且附带警告提示的场景
     *
     * @param msg 警告消息
     * @param data 响应数据
     * @param <T> 泛型类型
     * @return 统一响应对象（code=601）
     */
    public static <T> R<T> warn(String msg, T data) {
        // 调用内部方法构建响应对象（code=HttpStatus.WARN=601）
        return restResult(data, HttpStatus.WARN, msg);
    }

    /**
     * 内部方法：构建响应对象
     * 私有静态方法，统一创建R对象实例
     *
     * @param data 响应数据
     * @param code 状态码
     * @param msg 消息内容
     * @param <T> 泛型类型
     * @return 统一响应对象
     */
    private static <T> R<T> restResult(T data, int code, String msg) {
        // 创建R对象实例
        R<T> r = new R<>();
        // 设置状态码
        r.setCode(code);
        // 设置数据
        r.setData(data);
        // 设置消息
        r.setMsg(msg);
        // 返回构建好的响应对象
        return r;
    }

    /**
     * 判断响应是否错误
     * 用于业务层或拦截器中判断接口调用结果
     *
     * @param ret 响应对象
     * @param <T> 泛型类型
     * @return true=错误, false=成功
     */
    public static <T> Boolean isError(R<T> ret) {
        // 取反isSuccess的结果
        return !isSuccess(ret);
    }

    /**
     * 判断响应是否成功
     * 用于业务层或拦截器中判断接口调用结果
     *
     * @param ret 响应对象
     * @param <T> 泛型类型
     * @return true=成功, false=错误
     */
    public static <T> Boolean isSuccess(R<T> ret) {
        // 判断code是否等于SUCCESS（200）
        return R.SUCCESS == ret.getCode();
    }
}
