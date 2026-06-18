package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.technote.blog.entity.BlogLink;
import com.technote.blog.enums.VisibleStatusEnum;
import com.technote.blog.mapper.BlogLinkMapper;
import com.technote.blog.model.req.LinkSaveReq;
import com.technote.blog.model.resp.LinkResp;
import com.technote.blog.service.LinkService;
import com.technote.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 友情链接服务实现。
 */
@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final BlogLinkMapper linkMapper;

    @Override
    public List<LinkResp> listAdminLinks() {
        return linkMapper.selectList(new LambdaQueryWrapper<BlogLink>()
                        .orderByAsc(BlogLink::getSortOrder)
                        .orderByDesc(BlogLink::getCreateTime))
                .stream()
                .map(this::toLinkResp)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkResp createLink(LinkSaveReq req) {
        validateLink(req);
        ensureNoDuplicateSiteUrl(req.getSiteUrl().trim(), null);
        BlogLink link = new BlogLink();
        fillLink(link, req);
        link.setCreateBy(currentUserId());
        link.setUpdateBy(currentUserId());
        try {
            linkMapper.insert(link);
        } catch (DuplicateKeyException e) {
            throw new BaseException(409, "该网站已存在于友情链接中");
        }
        return toLinkResp(link);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkResp updateLink(Long id, LinkSaveReq req) {
        validateLink(req);
        BlogLink link = getLinkOrThrow(id);
        ensureNoDuplicateSiteUrl(req.getSiteUrl().trim(), id);
        fillLink(link, req);
        link.setUpdateBy(currentUserId());
        try {
            linkMapper.updateById(link);
        } catch (DuplicateKeyException e) {
            throw new BaseException(409, "该网站已存在于友情链接中");
        }
        return toLinkResp(link);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLink(Long id) {
        getLinkOrThrow(id);
        linkMapper.deleteById(id);
    }

    @Override
    public List<LinkResp> listVisibleLinks() {
        return linkMapper.selectList(new LambdaQueryWrapper<BlogLink>()
                        .eq(BlogLink::getStatus, VisibleStatusEnum.VISIBLE.getCode())
                        .orderByAsc(BlogLink::getSortOrder)
                        .orderByDesc(BlogLink::getCreateTime))
                .stream()
                .map(this::toLinkResp)
                .toList();
    }

    private BlogLink getLinkOrThrow(Long id) {
        BlogLink link = linkMapper.selectById(id);
        if (link == null) {
            throw new BaseException(404, "友链不存在");
        }
        return link;
    }

    private void validateLink(LinkSaveReq req) {
        if (!VisibleStatusEnum.contains(req.getStatus())) {
            throw new BaseException(400, "友链状态不正确");
        }
        validateHttpUrl(req.getSiteUrl(), "网站地址");
        if (StrUtil.isNotBlank(req.getIconUrl())) {
            validateHttpUrl(req.getIconUrl(), "图标地址");
        }
    }

    private void validateHttpUrl(String value, String fieldName) {
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            if (StrUtil.isBlank(uri.getHost())
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new BaseException(400, fieldName + "必须是有效的 HTTP(S) 地址");
            }
        } catch (URISyntaxException e) {
            throw new BaseException(400, fieldName + "必须是有效的 HTTP(S) 地址");
        }
    }

    private void ensureNoDuplicateSiteUrl(String siteUrl, Long excludeId) {
        Long count = linkMapper.selectCount(new LambdaQueryWrapper<BlogLink>()
                .eq(BlogLink::getSiteUrl, siteUrl)
                .ne(excludeId != null, BlogLink::getId, excludeId));
        if (count > 0) {
            throw new BaseException(409, "该网站已存在于友情链接中");
        }
    }

    private void fillLink(BlogLink link, LinkSaveReq req) {
        link.setSiteName(req.getSiteName().trim());
        link.setSiteUrl(req.getSiteUrl().trim());
        link.setIconUrl(StrUtil.blankToDefault(req.getIconUrl(), null));
        link.setDescription(StrUtil.blankToDefault(req.getDescription(), null));
        link.setSortOrder(req.getSortOrder());
        link.setStatus(req.getStatus());
    }

    private LinkResp toLinkResp(BlogLink link) {
        LinkResp resp = new LinkResp();
        resp.setId(link.getId());
        resp.setSiteName(link.getSiteName());
        resp.setSiteUrl(link.getSiteUrl());
        resp.setIconUrl(link.getIconUrl());
        resp.setDescription(link.getDescription());
        resp.setSortOrder(link.getSortOrder());
        resp.setStatus(link.getStatus());
        resp.setCreateTime(link.getCreateTime());
        resp.setUpdateTime(link.getUpdateTime());
        return resp;
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
