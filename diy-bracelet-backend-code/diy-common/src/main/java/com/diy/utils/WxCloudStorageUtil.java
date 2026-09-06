package com.diy.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.http.HttpMethodName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 腾讯云 COS 对象存储工具类
 * 支持两种认证模式：
 *   1. 永久密钥（SecretId + SecretKey）—— 自建服务器部署
 *   2. 微信云托管临时密钥 —— 微信云托管容器内部署
 */
@Data
@Slf4j
public class WxCloudStorageUtil {

    private String bucketName;
    private String region;
    private String secretId;
    private String secretKey;

    /** 是否使用永久密钥模式 */
    private final boolean usePermanentKey;

    // 微信云托管开放接口地址（仅容器内可访问）
    private static final String GET_AUTH_URL = "http://api.weixin.qq.com/_/cos/getauth";
    private static final String GET_METADATA_URL = "http://api.weixin.qq.com/_/cos/metaid/encode";

    public WxCloudStorageUtil(String bucketName, String region, String secretId, String secretKey) {
        this.bucketName = bucketName;
        this.region = region;
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.usePermanentKey = secretId != null && !secretId.isEmpty()
                            && secretKey != null && !secretKey.isEmpty();
        log.info("COS 初始化完成: bucket={}, region={}, 模式={}",
                bucketName, region, usePermanentKey ? "永久密钥" : "微信云临时密钥");
    }

    /** 兼容旧的两参数构造（微信云模式） */
    public WxCloudStorageUtil(String bucketName, String region) {
        this(bucketName, region, null, null);
    }

    /**
     * 文件上传
     */
    public String upload(byte[] bytes, String objectName) {
        COSClient cosClient = null;
        try {
            cosClient = createCOSClient();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(bytes.length);

            // 仅微信云模式需要写入 fileid 元数据
            if (!usePermanentKey) {
                String metadata = getFileMetadata(objectName);
                if (metadata != null && !metadata.isEmpty()) {
                    objectMetadata.setHeader("x-cos-meta-fileid", metadata);
                }
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, objectName,
                new ByteArrayInputStream(bytes), objectMetadata
            );
            cosClient.putObject(putObjectRequest);

            String fileUrl = "/admin/common/image/" + objectName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) cosClient.shutdown();
        }
    }

    /**
     * 从对象存储下载文件
     */
    public byte[] download(String objectName) {
        COSClient cosClient = null;
        try {
            cosClient = createCOSClient();

            COSObject cosObject = cosClient.getObject(bucketName, objectName);
            if (cosObject == null) {
                log.warn("对象存储中未找到文件: {}", objectName);
                return null;
            }

            try (InputStream inputStream = cosObject.getObjectContent();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] data = outputStream.toByteArray();
                log.info("从对象存储下载文件成功: {},大小={}字节", objectName, data.length);
                return data;
            }
        } catch (Exception e) {
            log.error("从对象存储下载文件失败: {}", objectName, e);
            return null;
        } finally {
            if (cosClient != null) cosClient.shutdown();
        }
    }

    /**
     * 从对象存储删除文件
     */
    public void delete(String fileUrlOrObjectName) {
        if (fileUrlOrObjectName == null || fileUrlOrObjectName.trim().isEmpty()) return;

        String objectName = fileUrlOrObjectName.trim();
        String prefix = "/admin/common/image/";
        if (objectName.startsWith(prefix)) {
            objectName = objectName.substring(prefix.length());
        }
        if (objectName.startsWith("/")) {
            objectName = objectName.substring(1);
        }

        COSClient cosClient = null;
        try {
            cosClient = createCOSClient();
            cosClient.deleteObject(bucketName, objectName);
            log.info("从对象存储删除文件成功: {}", objectName);
        } catch (Exception e) {
            log.error("从对象存储删除文件失败: {}", objectName, e);
        } finally {
            if (cosClient != null) cosClient.shutdown();
        }
    }

    /**
     * 获取对象存储的预签名访问 URL（用于 302 重定向）
     * 生成的 URL 有效期为 1 小时
     */
    public String getDirectUrl(String objectName) {
        // 标准化对象名称
        String normalized = objectName.trim();
        String prefix = "/admin/common/image/";
        if (normalized.startsWith(prefix)) {
            normalized = normalized.substring(prefix.length());
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        COSClient cosClient = null;
        try {
            cosClient = createCOSClient();

            // 生成预签名 URL,有效期 1 小时
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                bucketName,
                normalized,
                HttpMethodName.GET
            );
            // 设置过期时间为 1 小时（3600 秒）
            request.setExpiration(java.util.Date.from(
                java.time.Instant.now().plusSeconds(3600)
            ));

            java.net.URL url = cosClient.generatePresignedUrl(request);
            log.info("生成预签名 URL: {}", normalized);
            return url.toString();

        } catch (Exception e) {
            log.error("生成预签名 URL 失败: {}", normalized, e);
            // 抛出异常让调用者知道预签名 URL 生成失败，可以回退到直接下载方式
            throw new RuntimeException("生成预签名 URL 失败: " + e.getMessage(), e);
        } finally {
            if (cosClient != null) cosClient.shutdown();
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 创建 COS 客户端（根据认证模式自动选择）
     */
    private COSClient createCOSClient() {
        COSCredentials credentials;
        if (usePermanentKey) {
            credentials = new BasicCOSCredentials(secretId, secretKey);
        } else {
            TempCredentials temp = getTempCredentials();
            credentials = new BasicSessionCredentials(temp.tmpSecretId, temp.tmpSecretKey, temp.token);
        }
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(credentials, clientConfig);
    }

    private static class TempCredentials {
        String tmpSecretId;
        String tmpSecretKey;
        String token;
    }

    /**
     * 获取微信云临时密钥（仅容器内可用）
     */
    private TempCredentials getTempCredentials() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.exchange(
                GET_AUTH_URL, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                TempCredentials c = new TempCredentials();
                c.tmpSecretId = (String) body.get("TmpSecretId");
                c.tmpSecretKey = (String) body.get("TmpSecretKey");
                c.token = (String) body.get("Token");
                return c;
            }
            throw new RuntimeException("获取临时密钥失败");
        } catch (Exception e) {
            throw new RuntimeException("获取临时密钥异常（请确认是否在微信云托管容器内运行）: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件元数据（仅微信云模式需要）
     */
    private String getFileMetadata(String objectPath) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("openid", "");
            requestBody.put("bucket", bucketName);
            requestBody.put("paths", Collections.singletonList("/" + objectPath));

            ResponseEntity<Map> response = restTemplate.exchange(
                GET_METADATA_URL, HttpMethod.POST,
                new HttpEntity<>(requestBody, headers), Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Integer errcode = (Integer) body.get("errcode");
                if (errcode != null && errcode == 0) {
                    Map<String, Object> respdata = (Map<String, Object>) body.get("respdata");
                    if (respdata != null) {
                        List<String> list = (List<String>) respdata.get("x_cos_meta_field_strs");
                        if (list != null && !list.isEmpty()) return list.get(0);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("获取文件元数据异常,继续上传: {}", e.getMessage());
            return null;
        }
    }
}
