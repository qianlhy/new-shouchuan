package com.diy.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LocalFileUtil {

    /**
     * 本地文件上传
     * @param file 文件
     * @param basePath 基础路径
     * @param module 模块名称（如：banner、product、merchant）
     * @return 文件访问路径
     */
    public static String upload(MultipartFile file, String basePath, String module) {
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("文件名不能为空");
            }
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + 
                originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 构建文件保存路径
            String modulePath = module != null ? module + "/" : "";
            String filePath = basePath + "/uploads/" + modulePath + fileName;
            
            // 创建目录
            File destFile = new File(filePath);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            
            // 保存文件
            file.transferTo(destFile);
            
            // 返回访问路径（包含完整的访问前缀）
            String accessPath = "/admin/common/image/uploads/" + modulePath + fileName;
            log.info("文件上传成功: {}", accessPath);
            return accessPath;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}