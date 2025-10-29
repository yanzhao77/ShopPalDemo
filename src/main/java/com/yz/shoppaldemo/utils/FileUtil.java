package com.yz.shoppaldemo.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    /**
     * 获取或创建Lucene索引文件夹
     *
     * @return 索引文件夹路径
     * @throws IOException 路径创建失败时抛出异常
     */
    public static Path getOrCreateIndexDirectory(String filePath) throws IOException {
        // 1. 尝试从类路径资源获取（开发环境）
        ClassPathResource classPathResource = new ClassPathResource(filePath);

        try {
            if (classPathResource.exists()) {
                return Paths.get(classPathResource.getURI());
            }
        } catch (IOException e) {
            // 类路径资源不存在或无法访问，转为处理文件系统路径
        }

        // 2. 从文件系统获取（生产环境或JAR外部）
        // 注意：生产环境建议使用外部配置路径，避免写入JAR内部
        Path externalPath = Paths.get(System.getProperty("user.dir"), filePath);

        // 3. 确保目录存在，不存在则递归创建
        if (!Files.exists(externalPath)) {
            Files.createDirectories(externalPath);
        }

        return externalPath;
    }
}
