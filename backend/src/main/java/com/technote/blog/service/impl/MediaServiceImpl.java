package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogMedia;
import com.technote.blog.mapper.BlogMediaMapper;
import com.technote.blog.model.req.MediaPageQueryReq;
import com.technote.blog.model.resp.MediaResp;
import com.technote.blog.service.MediaService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import com.technote.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 媒体文件服务实现。
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");

    private final BlogMediaMapper mediaMapper;

    private final FileStorageProperties storageProperties;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaResp uploadImage(MultipartFile file) {
        validateImage(file);
        String originalName = StringUtils.cleanPath(StrUtil.blankToDefault(file.getOriginalFilename(), ""));
        String fileExt = resolveExtension(originalName);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExt;
        LocalDate today = LocalDate.now();
        String relativeDir = today.getYear() + "/" + String.format("%02d", today.getMonthValue());

        Path rootPath = Paths.get(storageProperties.getRootPath()).toAbsolutePath().normalize();
        Path targetDir = rootPath.resolve(relativeDir).normalize();
        Path targetFile = targetDir.resolve(fileName).normalize();
        if (!targetFile.startsWith(rootPath)) {
            throw new BaseException(400, "文件保存路径不合法");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new BaseException(500, "文件保存失败");
        }

        try {
            BlogMedia media = new BlogMedia();
            media.setOriginalName(originalName);
            media.setFileName(fileName);
            media.setFilePath(buildAccessPath(relativeDir + "/" + fileName));
            media.setFileSize(file.getSize());
            media.setMimeType(file.getContentType());
            media.setFileExt(fileExt);
            media.setQuoteCount(0);
            media.setUploadBy(currentUserId());
            mediaMapper.insert(media);
            return toMediaResp(media);
        } catch (RuntimeException ex) {
            deleteSavedFileQuietly(targetFile);
            throw ex;
        }
    }

    @Override
    public PageResp<MediaResp> pageMedia(MediaPageQueryReq req) {
        IPage<BlogMedia> page = mediaMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                new LambdaQueryWrapper<BlogMedia>()
                        .like(StrUtil.isNotBlank(req.getKeyword()), BlogMedia::getOriginalName, req.getKeyword())
                        .orderByDesc(BlogMedia::getCreateTime));
        List<MediaResp> records = page.getRecords().stream().map(this::toMediaResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMedia(Long id) {
        BlogMedia media = mediaMapper.selectById(id);
        if (media == null) {
            throw new BaseException(404, "媒体文件不存在");
        }
        if (media.getQuoteCount() != null && media.getQuoteCount() > 0) {
            throw new BaseException(400, "媒体文件已被引用，不能删除");
        }
        Path targetFile = resolveSavedFilePath(media.getFilePath());
        if (mediaMapper.markDeletedIfUnquoted(id) == 0) {
            throw new BaseException(400, "媒体文件已被引用，不能删除");
        }
        deleteSavedFileQuietly(targetFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshArticleMediaQuotes(String oldCoverUrl, String oldContent, String newCoverUrl, String newContent) {
        Set<String> oldPaths = collectLocalMediaPaths(oldCoverUrl, oldContent);
        Set<String> newPaths = collectLocalMediaPaths(newCoverUrl, newContent);

        Set<String> removedPaths = new HashSet<>(oldPaths);
        removedPaths.removeAll(newPaths);
        for (String filePath : removedPaths) {
            changeQuoteCount(filePath, -1);
        }

        Set<String> addedPaths = new HashSet<>(newPaths);
        addedPaths.removeAll(oldPaths);
        for (String filePath : addedPaths) {
            changeQuoteCount(filePath, 1);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BaseException(400, "图片不能超过10MB");
        }
        String originalName = StringUtils.cleanPath(StrUtil.blankToDefault(file.getOriginalFilename(), ""));
        String fileExt = resolveExtension(originalName);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(fileExt)) {
            throw new BaseException(400, "只支持 jpg、jpeg、png、webp、gif 图片");
        }
        String contentType = StrUtil.blankToDefault(file.getContentType(), "").toLowerCase();
        if (!ALLOWED_IMAGE_MIME_TYPES.contains(contentType)) {
            throw new BaseException(400, "图片类型不正确");
        }
    }

    private String resolveExtension(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            throw new BaseException(400, "文件名不能为空");
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new BaseException(400, "文件扩展名不能为空");
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private void deleteSavedFileQuietly(Path targetFile) {
        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException ignored) {
            // 删除失败不覆盖原始数据库异常，后续可通过媒体清理任务处理。
        }
    }

    private String buildAccessPath(String relativePath) {
        String normalizedPrefix = storageProperties.getAccessPrefix();
        if (!normalizedPrefix.startsWith("/")) {
            normalizedPrefix = "/" + normalizedPrefix;
        }
        String normalizedContextPath = StrUtil.blankToDefault(contextPath, "");
        return normalizedContextPath + normalizedPrefix + "/" + relativePath.replace("\\", "/");
    }

    private Path resolveSavedFilePath(String filePath) {
        String normalizedContextPath = StrUtil.blankToDefault(contextPath, "");
        String normalizedPrefix = storageProperties.getAccessPrefix();
        if (!normalizedPrefix.startsWith("/")) {
            normalizedPrefix = "/" + normalizedPrefix;
        }
        String expectedPrefix = normalizedContextPath + normalizedPrefix + "/";
        if (StrUtil.isBlank(filePath) || !filePath.startsWith(expectedPrefix)) {
            throw new BaseException(400, "媒体文件路径不合法");
        }
        String relativePath = filePath.substring(expectedPrefix.length());
        Path rootPath = Paths.get(storageProperties.getRootPath()).toAbsolutePath().normalize();
        Path targetFile = rootPath.resolve(relativePath).normalize();
        if (!targetFile.startsWith(rootPath)) {
            throw new BaseException(400, "媒体文件路径不合法");
        }
        return targetFile;
    }

    private Set<String> collectLocalMediaPaths(String coverUrl, String content) {
        Set<String> paths = new HashSet<>();
        addLocalMediaPath(paths, coverUrl);
        if (StrUtil.isBlank(content)) {
            return paths;
        }
        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
        while (matcher.find()) {
            addLocalMediaPath(paths, matcher.group(1));
        }
        return paths;
    }

    private void addLocalMediaPath(Set<String> paths, String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return;
        }
        String trimmed = filePath.trim();
        String normalizedContextPath = StrUtil.blankToDefault(contextPath, "");
        String normalizedPrefix = storageProperties.getAccessPrefix();
        if (!normalizedPrefix.startsWith("/")) {
            normalizedPrefix = "/" + normalizedPrefix;
        }
        String expectedPrefix = normalizedContextPath + normalizedPrefix + "/";
        if (trimmed.startsWith(expectedPrefix)) {
            paths.add(trimmed);
        }
    }

    private void changeQuoteCount(String filePath, int delta) {
        mediaMapper.changeQuoteCount(filePath, delta);
    }

    private MediaResp toMediaResp(BlogMedia media) {
        MediaResp resp = new MediaResp();
        resp.setId(media.getId());
        resp.setOriginalName(media.getOriginalName());
        resp.setFileName(media.getFileName());
        resp.setFilePath(media.getFilePath());
        resp.setFileSize(media.getFileSize());
        resp.setMimeType(media.getMimeType());
        resp.setFileExt(media.getFileExt());
        resp.setQuoteCount(media.getQuoteCount());
        resp.setUploadBy(media.getUploadBy());
        resp.setCreateTime(media.getCreateTime());
        return resp;
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
