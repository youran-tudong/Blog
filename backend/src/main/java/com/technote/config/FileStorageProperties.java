package com.technote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 本地文件存储配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "technote.upload")
public class FileStorageProperties {

    /**
     * 文件保存根目录。
     */
    private String rootPath;

    /**
     * 对外访问前缀。
     */
    private String accessPrefix;
}

