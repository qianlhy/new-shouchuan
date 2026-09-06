package com.diy.controller.admin;

import com.diy.constant.MessageConstant;
import com.diy.entity.CustomerServiceQr;
import com.diy.mapper.CustomerServiceQrMapper;
import com.diy.result.Result;
import com.diy.utils.WxCloudStorageUtil;
import com.diy.utils.LocalFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private WxCloudStorageUtil wxCloudStorageUtil;

    @Autowired
    private CustomerServiceQrMapper customerServiceQrMapper;

    @Value("${local.file.base-path}")
    private String basePath;

    // 支持的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    /**
     * 文件上传（微信云托管对象存储）
     * @param file 文件
     * @param type 文件类型（可选）：product（商品图）、banner（轮播图）、diy（DIY设计图）、customer_service（客服二维码）等
     */
    @ApiOperation("文件上传")
    @PostMapping("/admin/common/upload")
    public Result<String> upload(MultipartFile file, 
                                 @RequestParam(value = "type", required = false, defaultValue = "other") String type) {
        log.info("文件上传: {}, 类型: {}", file, type);
        try {
            //获取原文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }
            
            // 根据文件类型确定文件夹
            String folder = "other";
            switch (type.toLowerCase()) {
                case "product":
                    folder = "products";
                    break;
                case "banner":
                    folder = "banners";
                    break;
                case "diy":
                case "diy_design":
                    folder = "diy_designs";
                    break;
                case "customer_service":
                case "qr":
                    folder = "customer_service";
                    break;
                default:
                    folder = "others";
                    break;
            }
            
            //获得原文件名后缀并拼接一个uuid
            String extension = getFileExtension(originalFilename);
            String fileName = folder + "/" + UUID.randomUUID().toString() + "." + extension;
            String filepath = wxCloudStorageUtil.upload(file.getBytes(), fileName);
            return Result.success(filepath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    /**
     * 用户端上传DIY截图
     * 场景：小程序DIY下单时上传截图留档
     */
    @ApiOperation("用户端上传DIY截图")
    @PostMapping("/user/common/upload")
    public Result<java.util.Map<String, String>> uploadDiyScreenshot(@RequestParam("file") MultipartFile file,
                                                                     @RequestParam(value = "scene", required = false) String scene) {
        log.info("用户端上传DIY截图: file={}, scene={}", file != null ? file.getOriginalFilename() : null, scene);
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        if (scene == null || scene.trim().isEmpty()) {
            scene = "diy_design";
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 使用统一的文件夹结构：diy_designs/场景名/文件名
            String folder = "diy_designs";
            String objectName = folder + "/" + scene + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
            String path = wxCloudStorageUtil.upload(file.getBytes(), objectName);
            java.util.Map<String, String> result = new java.util.HashMap<>();
            result.put("path", path);
            // 这里返回的url先与path保持一致,前端可按需拼接完整域名
            result.put("url", path);
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户端上传DIY截图失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
    /**
     * 本地文件上传
     *
     * @param file   文件
     * @param module 模块名称（banner、merchant、product）
     * @return 文件访问路径
     */
    @ApiOperation("本地文件上传")
    @PostMapping("/admin/common/upload/local")
    public Result<String> uploadLocal(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "module", required = false) String module) {
        log.info("本地文件上传: {}, module: {}", file.getOriginalFilename(), module);

        try {
            String filepath = LocalFileUtil.upload(file, basePath, module);
            return Result.success(filepath);
        } catch (Exception e) {
            log.error("本地文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    /**
     * 读取图片文件（返回 302 重定向,不占用后端带宽）
     * @param request HTTP请求
     * @return 302 重定向到对象存储
     */
    @ApiOperation("读取图片文件（302重定向）")
    @GetMapping("/admin/common/image/**")
    public ResponseEntity<?> getImageRedirect(HttpServletRequest request) {
        try {
            // 获取请求路径
            String requestURI = request.getRequestURI();
            String imagePath = requestURI.substring(requestURI.indexOf("/admin/common/image/") + 20);

            // 移除开头的斜杠
            String cleanPath = imagePath;
            if (cleanPath.startsWith("/") || cleanPath.startsWith("\\")) {
                cleanPath = cleanPath.substring(1);
            }
            // 统一使用正斜杠
            cleanPath = cleanPath.replace("\\", "/");

            // 安全检查：防止路径遍历攻击
            if (cleanPath.contains("../") || cleanPath.contains("..\\")) {
                log.warn("检测到路径遍历攻击尝试: {}", imagePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 检查文件扩展名
            String extension = getFileExtension(cleanPath).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                log.warn("不支持的文件类型: {}", extension);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 尝试获取 COS 预签名 URL 并返回 302 重定向
            try {
                String directUrl = wxCloudStorageUtil.getDirectUrl(cleanPath);
                log.info("图片302重定向: {} -> {}", cleanPath, directUrl);
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(directUrl)).build();
            } catch (RuntimeException e) {
                // 如果获取预签名 URL 失败（如本地开发无密钥），回退到直接下载方式
                log.warn("获取预签名 URL 失败，回退到直接下载: {}", e.getMessage());

                byte[] data = wxCloudStorageUtil.download(cleanPath);
                if (data == null || data.length == 0) {
                    log.warn("对象存储中未找到文件: {}", cleanPath);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                MediaType mediaType = getMediaType(extension);
                ByteArrayResource resource = new ByteArrayResource(data);
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(7)))
                        .body(resource);
            }

        } catch (Exception e) {
            log.error("图片访问失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
    
    /**
     * 根据扩展名获取MediaType
     * @param extension 文件扩展名
     * @return MediaType
     */
    private MediaType getMediaType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "bmp":
                return MediaType.valueOf("image/bmp");
            case "webp":
                return MediaType.valueOf("image/webp");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    
    /**
     * 上传客服二维码
     * @param file 二维码图片文件
     * @return 上传结果
     */
    @ApiOperation("上传客服二维码")
    @PostMapping("/admin/common/upload/customer-service-qr")
    public Result<String> uploadCustomerServiceQR(@RequestParam("file") MultipartFile file) {
        log.info("上传客服二维码: {}", file.getOriginalFilename());
        
        try {
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }
            
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return Result.error("只支持图片格式: " + String.join(", ", ALLOWED_EXTENSIONS));
            }
            
            // 验证文件大小（限制5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }
            
            // 在禁用旧记录之前,先尝试删除旧图片文件
            CustomerServiceQr oldRecord = customerServiceQrMapper.getActiveOne();
            if (oldRecord != null && oldRecord.getImagePath() != null && !oldRecord.getImagePath().isEmpty()) {
                log.info("准备删除旧客服二维码图片: {}", oldRecord.getImagePath());
                wxCloudStorageUtil.delete(oldRecord.getImagePath());
            }
            
            // 生成对象存储路径,如：customer_service/qr_code_xxx.png
            String objectName = "customer_service/qr_code_" + System.currentTimeMillis() + "." + extension;
            
            // 上传到对象存储（微信云托管COS）
            String cloudPath = wxCloudStorageUtil.upload(file.getBytes(), objectName);
            log.info("客服二维码上传成功到对象存储: {}", cloudPath);
            
            // 将之前启用的记录标记为禁用
            customerServiceQrMapper.disableAll();
            
            // 保存新的记录到数据库
            CustomerServiceQr record = CustomerServiceQr.builder()
                    .imagePath(cloudPath)
                    .status(1)
                    .build();
            customerServiceQrMapper.insert(record);
            
            // 返回云文件标识或访问路径
            return Result.success(cloudPath);
            
        } catch (Exception e) {
            log.error("客服二维码上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取客服二维码
     * @return 客服二维码信息
     */
    @ApiOperation("获取客服二维码 - 管理端")
    @GetMapping("/admin/common/customer-service-qr")
    public Result<String> getCustomerServiceQR() {
        try {
            CustomerServiceQr record = customerServiceQrMapper.getActiveOne();
            if (record == null) {
                return Result.error("客服二维码未上传");
            }
            log.info("获取客服二维码: {}", record.getImagePath());
            return Result.success(record.getImagePath());
        } catch (Exception e) {
            log.error("获取客服二维码失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除客服二维码
     * @return 删除结果
     */
    @ApiOperation("删除客服二维码")
    @DeleteMapping("/admin/common/customer-service-qr")
    public Result<String> deleteCustomerServiceQR() {
        try {
            // 查询当前启用的二维码记录
            CustomerServiceQr record = customerServiceQrMapper.getActiveOne();
            if (record != null && record.getImagePath() != null && !record.getImagePath().isEmpty()) {
                log.info("删除客服二维码图片文件: {}", record.getImagePath());
                wxCloudStorageUtil.delete(record.getImagePath());
            }
            
            // 将当前启用的记录全部标记为禁用
            customerServiceQrMapper.disableAll();
            log.info("已标记所有客服二维码记录为禁用");
            return Result.success("删除成功");
            
        } catch (Exception e) {
            log.error("删除客服二维码失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取客服二维码 - 用户端公开接口
     * @return 客服二维码信息
     */
    @ApiOperation("获取客服二维码 - 用户端")
    @GetMapping("/user/common/customer-service-qr")
    public Result<String> getUserCustomerServiceQR() {
        try {
            CustomerServiceQr record = customerServiceQrMapper.getActiveOne();
            if (record == null) {
                return Result.error("客服二维码未上传");
            }
            log.info("[用户端]获取客服二维码: {}", record.getImagePath());
            return Result.success(record.getImagePath());
            
        } catch (Exception e) {
            log.error("[用户端]获取客服二维码失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}