package org.dromara.common.mail.utils;

// Hutool集合工具类，提供集合操作增强方法
import cn.hutool.core.collection.CollUtil;
// Hutool IO工具类，提供流操作和关闭方法
import cn.hutool.core.io.IoUtil;
// Hutool Map工具类，提供Map操作增强方法
import cn.hutool.core.map.MapUtil;
// Hutool字符工具类，提供字符常量
import cn.hutool.core.util.CharUtil;
// Hutool字符串工具类，提供字符串操作增强方法
import cn.hutool.core.util.StrUtil;
// Hutool JakartaMail封装类，简化JavaMail操作
import cn.hutool.extra.mail.JakartaMail;
// Hutool认证器实现，用于SMTP身份验证
import cn.hutool.extra.mail.JakartaUserPassAuthenticator;
// Hutool邮件账户类，封装SMTP配置
import cn.hutool.extra.mail.MailAccount;
// Jakarta Mail认证器接口
import jakarta.mail.Authenticator;
// Jakarta Mail会话类
import jakarta.mail.Session;
// Lombok访问级别注解
import lombok.AccessLevel;
// Lombok无参构造函数注解，私有化构造函数防止实例化
import lombok.NoArgsConstructor;
// Spring工具类，用于获取Spring容器中的Bean
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类，提供字符串操作方法
import org.dromara.common.core.utils.StringUtils;

// 文件类
import java.io.File;
// 输入流类
import java.io.InputStream;
// 集合接口
import java.util.Collection;
// 列表接口
import java.util.List;
// Map接口
import java.util.Map;
// Map条目接口
import java.util.Map.Entry;

/**
 * 邮件工具类
 * 提供静态方法发送邮件，支持文本/HTML格式、附件、抄送/密送、内嵌图片等功能
 * 基于Hutool的JakartaMail封装，简化JavaMail的复杂API
 * 使用Lombok的@NoArgsConstructor(access = AccessLevel.PRIVATE)防止实例化
 */
// Lombok注解：生成私有访问级别的无参构造函数
// 防止工具类被实例化，符合工具类设计规范
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailUtils {

    /**
     * 静态常量：从Spring容器中获取MailAccount Bean
     * 在类加载时通过SpringUtils获取配置好的邮件账户对象
     * 该对象包含了SMTP服务器的所有配置信息（主机、端口、用户名、密码等）
     */
    private static final MailAccount ACCOUNT = SpringUtils.getBean(MailAccount.class);

    /**
     * 获取邮件发送实例
     * 返回从Spring容器中获取的MailAccount对象
     *
     * @return MailAccount对象，包含SMTP配置信息
     */
    public static MailAccount getMailAccount() {
        // 直接返回静态常量ACCOUNT，避免重复从Spring容器获取
        return ACCOUNT;
    }

    /**
     * 获取邮件发送实例（自定义发送人以及授权码）
     * 允许临时修改发件人、用户名和密码
     * 修改后的配置会影响后续所有邮件发送，谨慎使用
     *
     * @param from 自定义发件人地址，如果为空则使用默认配置
     * @param user 自定义SMTP用户名，如果为空则使用默认配置
     * @param pass 自定义SMTP授权码，如果为空则使用默认配置
     * @return 修改后的MailAccount对象
     */
    public static MailAccount getMailAccount(String from, String user, String pass) {
        // 使用StringUtils.blankToDefault处理空值，如果参数为空则使用默认值
        // 修改ACCOUNT对象的属性，影响全局配置
        ACCOUNT.setFrom(StringUtils.blankToDefault(from, ACCOUNT.getFrom()));
        ACCOUNT.setUser(StringUtils.blankToDefault(user, ACCOUNT.getUser()));
        ACCOUNT.setPass(StringUtils.blankToDefault(pass, ACCOUNT.getPass()));
        // 返回修改后的ACCOUNT对象
        return ACCOUNT;
    }

    /**
     * 使用配置文件中设置的账户发送文本邮件，发送给单个或多个收件人<br>
     * 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人邮箱地址，支持单个或多个（用逗号或分号分隔）
     * @param subject 邮件标题
     * @param content 邮件正文（纯文本格式）
     * @param files   附件文件列表，可变参数
     * @return message-id 邮件的唯一标识符，可用于追踪邮件状态
     * @since 3.2.0
     */
    public static String sendText(String to, String subject, String content, File... files) {
        // 调用send方法，isHtml参数设为false表示发送纯文本邮件
        return send(to, subject, content, false, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给单个或多个收件人<br>
     * 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人邮箱地址，支持单个或多个（用逗号或分号分隔）
     * @param subject 邮件标题
     * @param content 邮件正文（HTML格式，支持HTML标签）
     * @param files   附件文件列表，可变参数
     * @return message-id 邮件的唯一标识符
     * @since 3.2.0
     */
    public static String sendHtml(String to, String subject, String content, File... files) {
        // 调用send方法，isHtml参数设为true表示发送HTML邮件
        return send(to, subject, content, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人<br>
     * 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件标题
     * @param content 邮件正文
     * @param isHtml  是否为HTML格式，true表示HTML，false表示纯文本
     * @param files   附件文件列表
     * @return message-id 邮件的唯一标识符
     */
    public static String send(String to, String subject, String content, boolean isHtml, File... files) {
        // 调用splitAddress方法将收件人字符串转换为列表
        // 然后调用重载的send方法发送邮件
        return send(splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人<br>
     * 多个收件人、抄送人、密送人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param cc      抄送人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param bcc     密送人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param subject 邮件标题
     * @param content 邮件正文
     * @param isHtml  是否为HTML格式
     * @param files   附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.0.3
     */
    public static String send(String to, String cc, String bcc, String subject, String content, boolean isHtml, File... files) {
        // 分别调用splitAddress方法处理收件人、抄送人、密送人字符串
        // 转换为列表后调用重载的send方法
        return send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送文本邮件，发送给多人
     *
     * @param tos     收件人邮箱地址列表
     * @param subject 邮件标题
     * @param content 邮件正文（纯文本格式）
     * @param files   附件文件列表
     * @return message-id 邮件的唯一标识符
     */
    public static String sendText(Collection<String> tos, String subject, String content, File... files) {
        // 调用send方法，isHtml参数设为false
        return send(tos, subject, content, false, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给多人
     *
     * @param tos     收件人邮箱地址列表
     * @param subject 邮件标题
     * @param content 邮件正文（HTML格式）
     * @param files   附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 3.2.0
     */
    public static String sendHtml(Collection<String> tos, String subject, String content, File... files) {
        // 调用send方法，isHtml参数设为true
        return send(tos, subject, content, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos     收件人邮箱地址列表
     * @param subject 邮件标题
     * @param content 邮件正文
     * @param isHtml  是否为HTML格式
     * @param files   附件文件列表
     * @return message-id 邮件的唯一标识符
     */
    public static String send(Collection<String> tos, String subject, String content, boolean isHtml, File... files) {
        // 抄送人和密送人设为null，调用重载方法
        return send(tos, null, null, subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos     收件人邮箱地址列表
     * @param ccs     抄送人邮箱地址列表，可以为null或空
     * @param bccs    密送人邮箱地址列表，可以为null或空
     * @param subject 邮件标题
     * @param content 邮件正文
     * @param isHtml  是否为HTML格式
     * @param files   附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.0.3
     */
    public static String send(Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content, boolean isHtml, File... files) {
        // 调用私有send方法，useGlobalSession设为true表示使用全局共享Session
        // 这样可以复用Session连接，提升性能
        return send(getMailAccount(), true, tos, ccs, bccs, subject, content, null, isHtml, files);
    }

    // ------------------------------------------------------------------------------------------------------------------------------- Custom MailAccount
    // 自定义MailAccount的分隔线
    // 以下方法允许传入自定义的MailAccount对象，而不是使用默认的ACCOUNT
    // 适用于需要使用不同邮件账户发送邮件的场景

    /**
     * 发送邮件给多人（自定义MailAccount）
     *
     * @param mailAccount 自定义的邮件账户对象
     * @param to          收件人，多个收件人用逗号或分号分隔
     * @param subject     邮件标题
     * @param content     邮件正文
     * @param isHtml      是否为HTML格式
     * @param files       附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 3.2.0
     */
    public static String send(MailAccount mailAccount, String to, String subject, String content, boolean isHtml, File... files) {
        // 将收件人字符串转换为列表，然后调用重载方法
        return send(mailAccount, splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * 发送邮件给多人（自定义MailAccount）
     *
     * @param mailAccount 自定义的邮件账户对象
     * @param tos         收件人邮箱地址列表
     * @param subject     邮件标题
     * @param content     邮件正文
     * @param isHtml      是否为HTML格式
     * @param files       附件文件列表
     * @return message-id 邮件的唯一标识符
     */
    public static String send(MailAccount mailAccount, Collection<String> tos, String subject, String content, boolean isHtml, File... files) {
        // 抄送人和密送人设为null
        return send(mailAccount, tos, null, null, subject, content, isHtml, files);
    }

    /**
     * 发送邮件给多人（自定义MailAccount）
     *
     * @param mailAccount 自定义的邮件账户对象
     * @param tos         收件人邮箱地址列表
     * @param ccs         抄送人邮箱地址列表，可以为null或空
     * @param bccs        密送人邮箱地址列表，可以为null或空
     * @param subject     邮件标题
     * @param content     邮件正文
     * @param isHtml      是否为HTML格式
     * @param files       附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.0.3
     */
    public static String send(MailAccount mailAccount, Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content, boolean isHtml, File... files) {
        // useGlobalSession设为false，不使用全局共享Session
        return send(mailAccount, false, tos, ccs, bccs, subject, content, null, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给单个或多个收件人<br>
     * 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to       收件人邮箱地址
     * @param subject  邮件标题
     * @param content  邮件正文（HTML格式）
     * @param imageMap 图片与占位符映射，占位符格式为cid:$IMAGE_PLACEHOLDER
     *                 例如：map.put("logo", inputStream)，在HTML中使用<img src="cid:logo"/>
     * @param files    附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 3.2.0
     */
    public static String sendHtml(String to, String subject, String content, Map<String, InputStream> imageMap, File... files) {
        // 调用send方法，isHtml参数设为true
        return send(to, subject, content, imageMap, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人<br>
     * 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to       收件人邮箱地址
     * @param subject  邮件标题
     * @param content  邮件正文
     * @param imageMap 图片与占位符映射，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml   是否为HTML格式
     * @param files    附件文件列表
     * @return message-id 邮件的唯一标识符
     */
    public static String send(String to, String subject, String content, Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 将收件人字符串转换为列表
        return send(splitAddress(to), subject, content, imageMap, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人<br>
     * 多个收件人、抄送人、密送人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to       收件人邮箱地址
     * @param cc       抄送人邮箱地址
     * @param bcc      密送人邮箱地址
     * @param subject  邮件标题
     * @param content  邮件正文
     * @param imageMap 图片与占位符映射
     * @param isHtml   是否为HTML格式
     * @param files    附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.0.3
     */
    public static String send(String to, String cc, String bcc, String subject, String content, Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 分别处理收件人、抄送人、密送人字符串
        return send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, imageMap, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给多人
     *
     * @param tos      收件人邮箱地址列表
     * @param subject  邮件标题
     * @param content  邮件正文（HTML格式）
     * @param imageMap 图片与占位符映射
     * @param files    附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 3.2.0
     */
    public static String sendHtml(Collection<String> tos, String subject, String content, Map<String, InputStream> imageMap, File... files) {
        // 调用send方法，isHtml参数设为true
        return send(tos, subject, content, imageMap, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos      收件人邮箱地址列表
     * @param subject  邮件标题
     * @param content  邮件正文
     * @param imageMap 图片与占位符映射
     * @param isHtml   是否为HTML格式
     * @param files    附件文件列表
     * @return message-id 邮件的唯一标识符
     */
    public static String send(Collection<String> tos, String subject, String content, Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 抄送人和密送人设为null
        return send(tos, null, null, subject, content, imageMap, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos      收件人邮箱地址列表
     * @param ccs      抄送人邮箱地址列表，可以为null或空
     * @param bccs     密送人邮箱地址列表，可以为null或空
     * @param subject  邮件标题
     * @param content  邮件正文
     * @param imageMap 图片与占位符映射
     * @param isHtml   是否为HTML格式
     * @param files    附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.0.3
     */
    public static String send(Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content, Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 调用私有send方法，useGlobalSession设为true
        return send(getMailAccount(), true, tos, ccs, bccs, subject, content, imageMap, isHtml, files);
    }

    // ------------------------------------------------------------------------------------------------------------------------------- Custom MailAccount
    // 自定义MailAccount的分隔线
    // 以下方法允许传入自定义的MailAccount对象，而不是使用默认的ACCOUNT
    // 适用于需要使用不同邮件账户发送邮件的场景

    /**
     * 发送邮件给多人（自定义MailAccount，支持内嵌图片）
     *
     * @param mailAccount 自定义的邮件账户对象
     * @param to          收件人，多个收件人用逗号或分号分隔
     * @param subject     邮件标题
     * @param content     邮件正文
     * @param imageMap    图片与占位符映射
     * @param isHtml      是否为HTML格式
     * @param files       附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 3.2.0
     */
    public static String send(MailAccount mailAccount, String to, String subject, String content, Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 将收件人字符串转换为列表
        return send(mailAccount, splitAddress(to), subject, content, imageMap, isHtml, files);
    }

    /**
     * 发送邮件给多人（自定义MailAccount，支持内嵌图片）
     *
     * @param mailAccount 自定义的邮件账户对象
     * @param tos         收件人邮箱地址列表
     * @param subject     邮件标题
     * @param content     邮件正文
     * @param imageMap    图片与占位符映射
     * @param isHtml      是否为HTML格式
     * @param files       附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.6.3
     */
    public static String send(MailAccount mailAccount, Collection<String> tos, String subject, String content, Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 抄送人和密送人设为null
        return send(mailAccount, tos, null, null, subject, content, imageMap, isHtml, files);
    }

    /**
     * 发送邮件给多人（自定义MailAccount，支持内嵌图片）
     *
     * @param mailAccount 自定义的邮件账户对象
     * @param tos         收件人邮箱地址列表
     * @param ccs         抄送人邮箱地址列表，可以为null或空
     * @param bccs        密送人邮箱地址列表，可以为null或空
     * @param subject     邮件标题
     * @param content     邮件正文
     * @param imageMap    图片与占位符映射
     * @param isHtml      是否为HTML格式
     * @param files       附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.6.3
     */
    public static String send(MailAccount mailAccount, Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content, Map<String, InputStream> imageMap,
                              boolean isHtml, File... files) {
        // useGlobalSession设为false，不使用全局共享Session
        return send(mailAccount, false, tos, ccs, bccs, subject, content, imageMap, isHtml, files);
    }

    /**
     * 根据配置文件，获取邮件客户端会话
     *
     * @param mailAccount 邮件账户配置对象
     * @param isSingleton 是否单例（全局共享会话）
     *                    true: 使用Session.getDefaultInstance()获取全局共享会话
     *                    false: 使用Session.getInstance()创建独立会话
     * @return Session对象，Jakarta Mail的会话实例
     * @since 5.5.7
     */
    public static Session getSession(MailAccount mailAccount, boolean isSingleton) {
        // 初始化认证器为null
        Authenticator authenticator = null;
        
        // 如果需要身份验证，创建JakartaUserPassAuthenticator
        // 传入用户名和密码用于SMTP认证
        if (mailAccount.isAuth()) {
            authenticator = new JakartaUserPassAuthenticator(mailAccount.getUser(), mailAccount.getPass());
        }

        // 根据isSingleton参数选择获取Session的方式
        // 使用三元运算符简化代码
        // isSingleton为true时使用全局共享会话，false时创建独立会话
        return isSingleton ? Session.getDefaultInstance(mailAccount.getSmtpProps(), authenticator) //
            : Session.getInstance(mailAccount.getSmtpProps(), authenticator);
    }

    // ------------------------------------------------------------------------------------------------------------------------ Private method start
    // 私有方法区域开始
    // 以下方法是内部实现细节，不对外暴露

    /**
     * 发送邮件的核心私有方法（支持内嵌图片）
     * 所有公共send方法最终都会调用此方法
     *
     * @param mailAccount      邮件账户配置对象
     * @param useGlobalSession 是否使用全局共享Session
     * @param tos              收件人邮箱地址列表
     * @param ccs              抄送人邮箱地址列表，可以为null或空
     * @param bccs             密送人邮箱地址列表，可以为null或空
     * @param subject          邮件标题
     * @param content          邮件正文
     * @param imageMap         图片与占位符映射，key为占位符名称，value为图片输入流
     * @param isHtml           是否为HTML格式
     * @param files            附件文件列表
     * @return message-id 邮件的唯一标识符
     * @since 4.6.3
     */
    private static String send(MailAccount mailAccount, boolean useGlobalSession, Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content,
                               Map<String, InputStream> imageMap, boolean isHtml, File... files) {
        // 创建JakartaMail对象，Hutool对JavaMail的封装
        // setUseGlobalSession设置是否使用全局共享Session
        final JakartaMail mail = JakartaMail.create(mailAccount).setUseGlobalSession(useGlobalSession);

        // 可选抄送人：如果抄送人列表不为空，则设置到mail对象
        // 使用CollUtil.isNotEmpty判断集合非空，避免空指针异常
        if (CollUtil.isNotEmpty(ccs)) {
            // 将List转换为数组，JakartaMail需要数组类型
            mail.setCcs(ccs.toArray(new String[0]));
        }
        
        // 可选密送人：如果密送人列表不为空，则设置到mail对象
        if (CollUtil.isNotEmpty(bccs)) {
            mail.setBccs(bccs.toArray(new String[0]));
        }

        // 设置收件人（必填项）
        mail.setTos(tos.toArray(new String[0]));
        
        // 设置邮件标题
        mail.setTitle(subject);
        
        // 设置邮件正文
        mail.setContent(content);
        
        // 设置是否为HTML格式
        mail.setHtml(isHtml);
        
        // 设置附件文件列表
        mail.setFiles(files);

        // 处理内嵌图片：如果imageMap不为空，则添加图片到邮件
        if (MapUtil.isNotEmpty(imageMap)) {
            // 遍历imageMap的每个条目
            for (Entry<String, InputStream> entry : imageMap.entrySet()) {
                // 添加图片到邮件，key作为占位符名称
                mail.addImage(entry.getKey(), entry.getValue());
                // 关闭输入流，防止资源泄漏
                // 使用IoUtil.close自动处理null和异常
                IoUtil.close(entry.getValue());
            }
        }

        // 发送邮件并返回message-id
        // message-id是邮件的唯一标识符，可用于追踪邮件状态
        return mail.send();
    }

    /**
     * 将收件人字符串转换为列表
     * 支持逗号和分号两种分隔符
     *
     * @param addresses 收件人字符串，多个收件人用逗号或分号分隔
     *                  例如："user1@qq.com,user2@qq.com" 或 "user1@qq.com;user2@qq.com"
     * @return 收件人邮箱地址列表，如果输入为空则返回null
     */
    private static List<String> splitAddress(String addresses) {
        // 使用StrUtil.isBlank判断字符串是否为空或空白
        if (StrUtil.isBlank(addresses)) {
            // 如果为空，返回null
            return null;
        }

        // 定义结果列表
        List<String> result;
        
        // 判断字符串是否包含逗号分隔符
        if (StrUtil.contains(addresses, CharUtil.COMMA)) {
            // 使用逗号分隔，并去除每个元素的前后空白
            result = StrUtil.splitTrim(addresses, CharUtil.COMMA);
        }
        // 判断字符串是否包含分号分隔符
        else if (StrUtil.contains(addresses, ';')) {
            // 使用分号分隔，并去除每个元素的前后空白
            result = StrUtil.splitTrim(addresses, ';');
        }
        // 如果不包含分隔符，认为是单个邮箱地址
        else {
            // 创建只包含一个元素的列表
            result = CollUtil.newArrayList(addresses);
        }
        
        // 返回处理后的收件人列表
        return result;
    }
    // ------------------------------------------------------------------------------------------------------------------------ Private method end
    // 私有方法区域结束
}
