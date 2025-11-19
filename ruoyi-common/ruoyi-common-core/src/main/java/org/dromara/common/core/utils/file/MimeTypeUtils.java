// 定义文件MIME类型常量，用于文件上传和类型检查
package org.dromara.common.core.utils.file;

/**
 * 媒体类型工具类
 * 提供常用的MIME类型常量和文件扩展名数组
 * 用于文件上传时的类型检查、Content-Type设置等场景
 * 包含图片、文档、视频、音频等常见文件类型的定义
 * 所有常量都是public static final，可以直接通过类名访问
 * 扩展名数组用于文件类型白名单验证，防止上传恶意文件
 * 提供统一的文件类型定义，确保整个系统使用一致的文件类型标准
 *
 * @author ruoyi
 */
// 工具类，提供文件MIME类型常量和扩展名数组
public class MimeTypeUtils {
    
    /**
     * 图片类型MIME常量
     * 用于设置HTTP响应的Content-Type头
     * 支持常见的图片格式：PNG、JPG、JPEG、BMP、GIF
     * 这些常量可以直接用于设置HTTP响应头
     */
    // PNG图片格式的MIME类型，支持透明背景
    public static final String IMAGE_PNG = "image/png";
    
    // JPG图片格式的MIME类型，有损压缩格式
    public static final String IMAGE_JPG = "image/jpg";
    
    // JPEG图片格式的MIME类型，JPG的另一种写法
    public static final String IMAGE_JPEG = "image/jpeg";
    
    // BMP图片格式的MIME类型，无压缩格式，文件较大
    public static final String IMAGE_BMP = "image/bmp";
    
    // GIF图片格式的MIME类型，支持动画和透明
    public static final String IMAGE_GIF = "image/gif";

    /**
     * 图片文件扩展名数组
     * 用于文件上传时的类型白名单验证
     * 包含常见的图片格式：bmp、gif、jpg、jpeg、png
     * 这个数组常用于文件上传前的类型检查，防止上传非图片文件
     */
    // 图片文件扩展名白名单，用于文件上传验证
    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png"};

    /**
     * Flash文件扩展名数组
     * 用于验证Flash动画文件
     * 包含：swf（Flash动画）、flv（Flash视频）
     * 注意：Flash技术已逐渐被淘汰，这些格式主要用于兼容旧系统
     * 新项目不建议再支持Flash格式
     */
    // Flash文件扩展名，已逐渐被淘汰
    public static final String[] FLASH_EXTENSION = {"swf", "flv"};

    /**
     * 媒体文件扩展名数组
     * 包含音频和视频格式的扩展名
     * 用于文件上传时的媒体类型验证
     * 包含：swf、flv、mp3、wav、wma、wmv、mid、avi、mpg、asf、rm、rmvb
     * 涵盖常见的音频和视频格式
     */
    // 媒体文件扩展名，包含音频和视频
    public static final String[] MEDIA_EXTENSION = {"swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
        "asf", "rm", "rmvb"};

    /**
     * 视频文件扩展名数组
     * 专门用于视频文件的类型验证
     * 包含：mp4（最常见）、avi（较老格式）、rmvb（Real格式）
     * 可以根据需要扩展支持更多视频格式
     * 这个数组比MEDIA_EXTENSION更专注于视频格式
     */
    // 视频文件扩展名，专注于视频格式
    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb"};

    /**
     * 默认允许上传的文件扩展名数组
     * 系统默认的白名单，包含常见的安全文件类型
     * 用于文件上传时的基础安全验证，防止上传可执行文件等危险类型
     *
     * 包含的文件类型：
     * - 图片：bmp、gif、jpg、jpeg、png
     * - 文档：doc、docx、xls、xlsx、ppt、pptx、html、htm、txt
     * - 压缩文件：rar、zip、gz、bz2
     * - 视频：mp4、avi、rmvb
     * - PDF：pdf
     *
     * 这个白名单涵盖了办公和多媒体文件的常见格式
     * 不包括可执行文件（exe、dll、bat等）和脚本文件（js、vbs等）
     */
    // 默认允许上传的文件扩展名白名单
    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
        // 图片格式，最常用的文件类型
        "bmp", "gif", "jpg", "jpeg", "png",
        // word excel powerpoint 文档格式
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
        // 压缩文件格式
        "rar", "zip", "gz", "bz2",
        // 视频格式
        "mp4", "avi", "rmvb",
        // pdf文档
        "pdf"};

    /**
     * 私有构造方法
     * 防止实例化工具类，所有方法都应该是静态的
     * 使用AssertionError而不是私有构造方法，提供更明确的错误信息
     * 这是工具类的标准做法，防止通过反射实例化
     */
    // 私有构造方法，防止工具类被实例化
    private MimeTypeUtils() {
        // 抛出AssertionError，提供更明确的错误信息
        // 这比私有构造方法更明确，表示这个类不应该被实例化
        throw new AssertionError("Cannot instantiate utility class");
    }

}
