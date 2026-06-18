package com.technote.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 上传文件静态资源映射。
 */
@Configuration
@RequiredArgsConstructor
public class UploadResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String accessPrefix = storageProperties.getAccessPrefix();
        if (!accessPrefix.startsWith("/")) {
            accessPrefix = "/" + accessPrefix;
        }
        if (accessPrefix.endsWith("/")) {
            accessPrefix = accessPrefix.substring(0, accessPrefix.length() - 1);
        }
        Path rootPath = Paths.get(storageProperties.getRootPath()).toAbsolutePath().normalize();
        String resourceLocation = rootPath.toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation = resourceLocation + "/";
        }
        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations(resourceLocation);
    }
}
