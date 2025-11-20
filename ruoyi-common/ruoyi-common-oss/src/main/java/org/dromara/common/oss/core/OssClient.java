// 定义OSS核心客户端类的包路径
package org.dromara.common.oss.core;

// Hutool IO工具类，用于流操作
import cn.hutool.core.io.IoUtil;
// Hutool ID生成工具类，用于生成UUID
import cn.hutool.core.util.IdUtil;
// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 系统常量类
import org.dromara.common.core.constant.Constants;
// 日期工具类
import org.dromara.common.core.utils.DateUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 文件工具类
import org.dromara.common.core.utils.file.FileUtils;
// OSS常量接口
import org.dromara.common.oss.constant.OssConstant;
// 上传结果实体类
import org.dromara.common.oss.entity.UploadResult;
// 访问策略枚举
import org.dromara.common.oss.enums.AccessPolicyType;
// OSS异常类
import org.dromara.common.oss.exception.OssException;
// OSS配置属性类
import org.dromara.common.oss.properties.OssProperties;
// AWS SDK基础认证类
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
// AWS SDK静态认证提供者
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
// AWS SDK异步请求和响应类
import software.amazon.awssdk.core.async.*;
// AWS SDK Netty HTTP客户端
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
// AWS SDK区域类
import software.amazon.awssdk.regions.Region;
// AWS SDK S3异步客户端
import software.amazon.awssdk.services.s3.S3AsyncClient;
// AWS SDK S3配置类
import software.amazon.awssdk.services.s3.S3Configuration;
// AWS SDK S3获取对象响应类
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
// AWS SDK S3预签名生成器
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
// AWS SDK S3传输管理器
import software.amazon.awssdk.transfer.s3.S3TransferManager;
// AWS SDK S3传输模型类
import software.amazon.awssdk.transfer.s3.model.*;
// AWS SDK S3传输监听器
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

// Java IO类
import java.io.*;
// Java网络URI类
import java.net.URI;
// Java网络URL类
import java.net.URL;
// Java NIO通道类
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
// Java NIO文件类
import java.nio.file.Files;
import java.nio.file.Path;
// Java时间Duration类
import java.time.Duration;
// Java Optional类
import java.util.Optional;
// Java函数式接口
import java.util.function.Consumer;

/**
 * OSS客户端类，基于AWS S3协议实现
 * 支持所有兼容S3协议的云厂商：阿里云OSS、腾讯云COS、七牛云Kodo、MinIO等
 * 提供文件上传、下载、删除、预签名URL生成等完整功能
 * 使用AWS SDK for Java 2.x，基于Netty实现异步IO，性能优异
 *
 * @author AprilWind
 */
// Lombok日志注解：自动生成slf4j日志对象log
@Slf4j
// OSS客户端类，封装S3操作
public class OssClient {

    /**
     * OSS配置键，标识当前客户端实例
     * 如：aliyun-oss、qcloud-cos等
     */
    private final String configKey;

    /**
     * OSS配置属性对象，包含endpoint、accessKey、secretKey等
     */
    private final OssProperties properties;

    /**
     * AWS S3异步客户端，用于执行S3操作
     * 基于Netty实现异步IO，性能优异
     */
    private final S3AsyncClient client;

    /**
     * S3传输管理器，用于管理文件上传下载的高级工具
     * 支持分片上传、断点续传等高级功能
     */
    private final S3TransferManager transferManager;

    /**
     * S3预签名URL生成器，用于生成临时访问链接
     * 支持私有桶文件的临时授权访问
     */
    private final S3Presigner presigner;

    /**
     * 构造方法，初始化OSS客户端
     * 创建AWS S3客户端、传输管理器和预签名生成器
     *
     * @param configKey     OSS配置键
     * @param ossProperties OSS配置属性
     */
    public OssClient(String configKey, OssProperties ossProperties) {
        // 保存配置键
        this.configKey = configKey;
        // 保存配置属性
        this.properties = ossProperties;
        try {
            // 创建AWS认证信息，使用accessKey和secretKey
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));

            // 判断是否为MinIO：MinIO使用HTTPS时限制使用域名访问，需要启用路径样式访问
            // 云服务商（阿里云、腾讯云等）使用虚拟托管样式访问
            boolean isStyle = !StringUtils.containsAny(properties.getEndpoint(), OssConstant.CLOUD_SERVICE);

            // 创建AWS S3异步客户端，基于Netty实现
            this.client = S3AsyncClient.builder()
                // 设置认证提供者
                .credentialsProvider(credentialsProvider)
                // 设置终端节点（endpoint）
                .endpointOverride(URI.create(getEndpoint()))
                // 设置AWS区域
                .region(of())
                // 强制路径样式（MinIO需要）
                .forcePathStyle(isStyle)
                // 设置HTTP客户端，配置连接超时60秒
                .httpClient(NettyNioAsyncHttpClient.builder()
                    .connectionTimeout(Duration.ofSeconds(60)).build())
                .build();

            // 创建S3传输管理器，用于高级文件传输操作
            this.transferManager = S3TransferManager.builder().s3Client(this.client).build();

            // 创建S3配置对象，禁用分块编码，启用路径样式访问
            S3Configuration config = S3Configuration.builder().chunkedEncodingEnabled(false)
                .pathStyleAccessEnabled(isStyle).build();

            // 创建S3预签名URL生成器，用于生成临时访问链接
            this.presigner = S3Presigner.builder()
                // 设置区域
                .region(of())
                // 设置认证提供者
                .credentialsProvider(credentialsProvider)
                // 设置域名终端节点
                .endpointOverride(URI.create(getDomain()))
                // 设置S3配置
                .serviceConfiguration(config)
                .build();

        } catch (Exception e) {
            // 如果是OssException，直接抛出
            if (e instanceof OssException) {
                throw e;
            }
            // 包装其他异常为OssException，提示配置错误
            throw new OssException("配置错误! 请检查系统配置:[" + e.getMessage() + "]");
        }
    }

    /**
     * 上传本地文件到OSS
     * 支持设置MD5校验和、内容类型等参数
     *
     * @param filePath    本地文件路径
     * @param key         OSS对象键（文件路径）
     * @param md5Digest   文件MD5哈希值（可选，用于校验）
     * @param contentType 文件内容类型（MIME类型）
     * @return UploadResult 上传结果，包含文件URL、文件名、ETag
     * @throws OssException 上传失败时抛出异常
     */
    public UploadResult upload(Path filePath, String key, String md5Digest, String contentType) {
        try {
            // 构建文件上传请求，使用传输管理器上传
            FileUpload fileUpload = transferManager.uploadFile(
                x -> x.putObjectRequest(
                        // 构建PutObject请求
                        y -> y.bucket(properties.getBucketName()) // 设置存储桶名称
                            .key(key) // 设置对象键（文件路径）
                            .contentMD5(StringUtils.isNotEmpty(md5Digest) ? md5Digest : null) // 设置MD5校验和（可选）
                            .contentType(contentType) // 设置内容类型
                            // 用于设置对象的访问控制列表（ACL）
                            // 不同云厂商对ACL的支持不同，阿里云支持，腾讯云不支持
                            // 默认注释掉，需要时自行开启
                            //.acl(getAccessPolicy().getObjectCannedACL())
                            .build())
                    // 添加传输监听器，打印上传进度日志
                    .addTransferListener(LoggingTransferListener.create())
                    // 设置源文件路径
                    .source(filePath).build());

            // 等待上传完成并获取结果
            CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
            // 获取ETag（实体标签，用于校验文件完整性）
            String eTag = uploadResult.response().eTag();

            // 构建上传结果对象，包含文件URL、文件名、ETag
            return UploadResult.builder().url(getUrl() + StringUtils.SLASH + key).filename(key).eTag(eTag).build();
        } catch (Exception e) {
            // 捕获异常并抛出自定义OssException
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        } finally {
            // 无论上传是否成功，最终删除临时文件
            // 防止临时文件占用磁盘空间
            FileUtils.del(filePath);
        }
    }

    /**
     * 上传输入流到OSS
     * 自动将非ByteArrayInputStream转换为ByteArrayInputStream
     *
     * @param inputStream 输入流
     * @param key         OSS对象键（文件路径）
     * @param length      输入流长度
     * @param contentType 文件内容类型（MIME类型）
     * @return UploadResult 上传结果
     * @throws OssException 上传失败时抛出异常
     */
    public UploadResult upload(InputStream inputStream, String key, Long length, String contentType) {
        // 如果输入流不是ByteArrayInputStream，先读取为字节数组再创建ByteArrayInputStream
        // 这是因为AWS SDK需要可重复读取的流
        if (!(inputStream instanceof ByteArrayInputStream)) {
            inputStream = new ByteArrayInputStream(IoUtil.readBytes(inputStream));
        }
        try {
            // 创建阻塞式输入流异步请求体，用于上传流数据
            // length如果为空会报错，必须指定内容长度
            BlockingInputStreamAsyncRequestBody body = BlockingInputStreamAsyncRequestBody.builder()
                .contentLength(length) // 设置内容长度
                .subscribeTimeout(Duration.ofSeconds(120)) // 设置订阅超时120秒
                .build();

            // 使用传输管理器上传流
            Upload upload = transferManager.upload(
                x -> x.requestBody(body) // 设置请求体
                    .addTransferListener(LoggingTransferListener.create()) // 添加传输监听器
                    .putObjectRequest(
                        y -> y.bucket(properties.getBucketName()) // 设置存储桶
                            .key(key) // 设置对象键
                            .contentType(contentType) // 设置内容类型
                            // ACL配置，默认注释
                            //.acl(getAccessPolicy().getObjectCannedACL())
                            .build())
                    .build());

            // 将输入流写入请求体
            body.writeInputStream(inputStream);

            // 等待上传完成
            CompletedUpload uploadResult = upload.completionFuture().join();
            // 获取ETag
            String eTag = uploadResult.response().eTag();

            // 构建上传结果对象
            return UploadResult.builder().url(getUrl() + StringUtils.SLASH + key).filename(key).eTag(eTag).build();
        } catch (Exception e) {
            // 抛出上传失败异常
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 从OSS下载文件到本地临时目录
     * 返回临时文件路径，调用方需要负责删除临时文件
     *
     * @param path OSS对象键（文件路径）
     * @return 临时文件路径
     * @throws OssException 下载失败时抛出异常
     */
    public Path fileDownload(String path) {
        // 创建临时文件，用于存储下载的文件
        Path tempFilePath = FileUtils.createTempFile().toPath();
        // 使用传输管理器下载文件
        FileDownload downloadFile = transferManager.downloadFile(
            x -> x.getObjectRequest(
                    // 构建GetObject请求
                    y -> y.bucket(properties.getBucketName()) // 设置存储桶
                        .key(removeBaseUrl(path)) // 移除基础URL获取相对路径
                        .build())
                // 添加传输监听器
                .addTransferListener(LoggingTransferListener.create())
                // 设置目标路径
                .destination(tempFilePath)
                .build());
        // 等待下载完成
        downloadFile.completionFuture().join();
        // 返回临时文件路径
        return tempFilePath;
    }

    /**
     * 从OSS下载文件到输出流
     * 提供消费者函数处理文件大小
     *
     * @param key OSS对象键（文件路径）
     * @param out 输出流
     * @param consumer 文件大小消费者函数
     * @throws OssException 下载失败时抛出异常
     */
    public void download(String key, OutputStream out, Consumer<Long> consumer) {
        try {
            // 调用重载方法获取写出订阅器，并写入输出流
            this.download(key, consumer).writeTo(out);
        } catch (Exception e) {
            // 抛出下载失败异常
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 从OSS下载文件到输出流
     * 返回写出订阅器，支持异步写出
     *
     * @param key OSS对象键（文件路径）
     * @param contentLengthConsumer 文件大小消费者函数
     * @return WriteOutSubscriber写出订阅器
     * @throws OssException 下载失败时抛出异常
     */
    public WriteOutSubscriber<OutputStream> download(String key, Consumer<Long> contentLengthConsumer) {
        try {
            // 构建下载请求，使用发布订阅模式
            DownloadRequest<ResponsePublisher<GetObjectResponse>> publisherDownloadRequest = DownloadRequest.builder()
                // 设置GetObject请求
                .getObjectRequest(y -> y.bucket(properties.getBucketName())
                    .key(key) // 设置对象键
                    .build())
                // 添加传输监听器
                .addTransferListener(LoggingTransferListener.create())
                // 使用发布订阅转换器
                .responseTransformer(AsyncResponseTransformer.toPublisher())
                .build();

            // 使用传输管理器下载文件，返回发布者
            Download<ResponsePublisher<GetObjectResponse>> publisherDownload = transferManager.download(publisherDownloadRequest);
            // 获取下载发布者
            ResponsePublisher<GetObjectResponse> publisher = publisherDownload.completionFuture().join().result();
            // 如果提供了文件大小消费者函数，执行它
            Optional.ofNullable(contentLengthConsumer)
                .ifPresent(lengthConsumer -> lengthConsumer.accept(publisher.response().contentLength()));

            // 构建写出订阅器对象
            return out -> {
                // 创建可写入的字节通道
                try(WritableByteChannel channel = Channels.newChannel(out)){
                    // 订阅数据并写入通道
                    publisher.subscribe(byteBuffer -> {
                        // 循环直到ByteBuffer数据全部写入
                        while (byteBuffer.hasRemaining()) {
                            try {
                                // 写入数据到通道
                                channel.write(byteBuffer);
                            } catch (IOException e) {
                                // 抛出运行时异常
                                throw new RuntimeException(e);
                            }
                        }
                    }).join(); // 等待完成
                }
            };
        } catch (Exception e) {
            // 抛出下载失败异常
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 删除OSS中的文件
     *
     * @param path OSS对象键（文件路径）
     * @throws OssException 删除失败时抛出异常
     */
    public void delete(String path) {
        try {
            // 调用S3客户端删除对象
            client.deleteObject(
                x -> x.bucket(properties.getBucketName()) // 设置存储桶
                    .key(removeBaseUrl(path)) // 移除基础URL获取相对路径
                    .build());
        } catch (Exception e) {
            // 抛出删除失败异常
            throw new OssException("删除文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 获取私有桶文件的临时访问URL（预签名URL）
     * 用于临时授权访问私有桶中的文件
     *
     * @param objectKey   OSS对象键（文件路径）
     * @param expiredTime URL过期时间
     * @return 预签名URL
     */
    public String getPrivateUrl(String objectKey, Duration expiredTime) {
        // 使用S3预签名生成器获取对象的预签名URL
        URL url = presigner.presignGetObject(
                x -> x.signatureDuration(expiredTime) // 设置签名持续时间
                    .getObjectRequest(
                        y -> y.bucket(properties.getBucketName()) // 设置存储桶
                            .key(objectKey) // 设置对象键
                            .build())
                    .build())
            .url(); // 获取URL对象
        // 返回URL字符串
        return url.toString();
    }

    /**
     * 上传字节数组到OSS，使用指定后缀构造对象键
     * 自动生成文件路径：前缀/日期路径/UUID.后缀
     *
     * @param data        字节数组
     * @param suffix      文件后缀（如.jpg）
     * @param contentType 内容类型
     * @return UploadResult 上传结果
     * @throws OssException 上传失败时抛出异常
     */
    public UploadResult uploadSuffix(byte[] data, String suffix, String contentType) {
        // 调用upload方法上传，自动生成路径
        return upload(new ByteArrayInputStream(data), getPath(properties.getPrefix(), suffix), Long.valueOf(data.length), contentType);
    }

    /**
     * 上传输入流到OSS，使用指定后缀构造对象键
     * 自动生成文件路径：前缀/日期路径/UUID.后缀
     *
     * @param inputStream 输入流
     * @param suffix      文件后缀
     * @param length      流长度
     * @param contentType 内容类型
     * @return UploadResult 上传结果
     * @throws OssException 上传失败时抛出异常
     */
    public UploadResult uploadSuffix(InputStream inputStream, String suffix, Long length, String contentType) {
        // 调用upload方法上传，自动生成路径
        return upload(inputStream, getPath(properties.getPrefix(), suffix), length, contentType);
    }

    /**
     * 上传文件到OSS，使用指定后缀构造对象键
     * 自动生成文件路径：前缀/日期路径/UUID.后缀
     *
     * @param file   文件对象
     * @param suffix 文件后缀
     * @return UploadResult 上传结果
     * @throws OssException 上传失败时抛出异常
     */
    public UploadResult uploadSuffix(File file, String suffix) {
        // 调用upload方法上传，自动生成路径，自动检测MIME类型
        return upload(file.toPath(), getPath(properties.getPrefix(), suffix), null, FileUtils.getMimeType(suffix));
    }

    /**
     * 获取文件输入流
     * 先下载到临时文件，再创建输入流，最后删除临时文件
     *
     * @param path OSS对象键（文件路径）
     * @return 输入流
     * @throws IOException IO异常
     */
    public InputStream getObjectContent(String path) throws IOException {
        // 下载文件到临时目录
        Path tempFilePath = fileDownload(path);
        // 创建输入流
        InputStream inputStream = Files.newInputStream(tempFilePath);
        // 删除临时文件
        FileUtils.del(tempFilePath);
        // 返回对象内容的输入流
        return inputStream;
    }

    /**
     * 获取S3客户端的终端节点URL
     * 根据isHttps配置返回http或https协议
     *
     * @return 终端节点URL
     */
    public String getEndpoint() {
        // 根据配置文件中的是否使用HTTPS，设置协议头部
        String header = getIsHttps();
        // 拼接协议头部和终端点，得到完整的终端点URL
        return header + properties.getEndpoint();
    }

    /**
     * 获取S3客户端的终端节点URL（自定义域名版本）
     * 处理云服务商和MinIO的不同域名格式
     *
     * @return 终端节点URL
     */
    public String getDomain() {
        // 从配置中获取域名、终端点、是否使用HTTPS等信息
        String domain = properties.getDomain();
        String endpoint = properties.getEndpoint();
        String header = getIsHttps();

        // 如果是云服务商（阿里云、腾讯云等），直接返回域名或终端点
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            return StringUtils.isNotEmpty(domain) ? header + domain : header + endpoint;
        }

        // 如果是MinIO，处理域名并返回
        if (StringUtils.isNotEmpty(domain)) {
            // 如果域名以"https://"或"http://"开头，直接返回；否则添加协议头部
            return domain.startsWith(Constants.HTTPS) || domain.startsWith(Constants.HTTP) ? domain : header + domain;
        }

        // 没有域名配置，返回终端点
        return header + endpoint;
    }

    /**
     * 根据配置的region返回相应的AWS区域对象
     * 如果region为空，返回默认的us-east-1区域
     *
     * @return AWS区域对象
     */
    public Region of() {
        // 获取配置的AWS区域字符串
        String region = properties.getRegion();
        // 如果region非空，使用Region.of创建对应区域对象；否则返回默认的us-east-1区域
        return StringUtils.isNotEmpty(region) ? Region.of(region) : Region.US_EAST_1;
    }

    /**
     * 获取云存储服务的访问URL
     * 处理云服务商和MinIO的不同URL格式
     *
     * @return 文件访问URL
     */
    public String getUrl() {
        // 获取域名、终端点和协议头部
        String domain = properties.getDomain();
        String endpoint = properties.getEndpoint();
        String header = getIsHttps();
        
        // 如果是云服务商，直接返回域名或桶域名
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            // 如果有自定义域名，返回自定义域名；否则返回桶域名（bucket.endpoint）
            return header + (StringUtils.isNotEmpty(domain) ? domain : properties.getBucketName() + "." + endpoint);
        }
        
        // MinIO单独处理
        if (StringUtils.isNotEmpty(domain)) {
            // 如果域名以"https://"或"http://"开头，直接返回；否则添加协议头部和桶路径
            return (domain.startsWith(Constants.HTTPS) || domain.startsWith(Constants.HTTP)) ?
                domain + StringUtils.SLASH + properties.getBucketName() : header + domain + StringUtils.SLASH + properties.getBucketName();
        }
        
        // 没有域名，返回终端点+桶路径
        return header + endpoint + StringUtils.SLASH + properties.getBucketName();
    }

    /**
     * 生成唯一的文件路径
     * 使用日期、UUID、前缀和后缀组合，确保路径唯一性
     * 格式：前缀/yyyy/MM/dd/UUID.后缀 或 yyyy/MM/dd/UUID.后缀
     *
     * @param prefix 路径前缀
     * @param suffix 文件后缀
     * @return 完整文件路径
     */
    public String getPath(String prefix, String suffix) {
        // 生成UUID（无横线的简单格式）
        String uuid = IdUtil.fastSimpleUUID();
        // 生成日期路径（yyyy/MM/dd格式）
        String datePath = DateUtils.datePath();
        // 拼接路径：前缀/日期路径/UUID.后缀
        String path = StringUtils.isNotEmpty(prefix) ?
            prefix + StringUtils.SLASH + datePath + StringUtils.SLASH + uuid : datePath + StringUtils.SLASH + uuid;
        // 添加文件后缀
        return path + suffix;
    }

    /**
     * 移除路径中的基础URL部分，得到相对路径
     * 用于从完整URL中提取对象键
     *
     * @param path 完整路径（包含基础URL）
     * @return 相对路径（对象键）
     */
    public String removeBaseUrl(String path) {
        // 将基础URL+斜杠替换为空字符串，得到相对路径
        return path.replace(getUrl() + StringUtils.SLASH, "");
    }

    /**
     * 获取配置键
     *
     * @return OSS配置键
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 获取是否使用HTTPS的配置，返回相应的协议头部
     *
     * @return 协议头部，"https://"或"http://"
     */
    public String getIsHttps() {
        // 如果配置为Y，返回https://；否则返回http://
        return OssConstant.IS_HTTPS.equals(properties.getIsHttps()) ? Constants.HTTPS : Constants.HTTP;
    }

    /**
     * 检查配置是否相同
     * 用于判断是否需要重新创建客户端实例
     *
     * @param properties 新的配置属性
     * @return 是否相同
     */
    public boolean checkPropertiesSame(OssProperties properties) {
        // 比较配置对象的相等性
        return this.properties.equals(properties);
    }

    /**
     * 获取当前桶的访问策略类型
     *
     * @return 访问策略枚举
     */
    public AccessPolicyType getAccessPolicy() {
        // 根据配置中的accessPolicy字符串获取枚举值
        return AccessPolicyType.getByType(properties.getAccessPolicy());
    }

}
