package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogLink;
import com.technote.blog.entity.BlogLinkApply;
import com.technote.blog.enums.AuditStatusEnum;
import com.technote.blog.enums.VisibleStatusEnum;
import com.technote.blog.mapper.BlogLinkApplyMapper;
import com.technote.blog.mapper.BlogLinkMapper;
import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.LinkApplyAuditReq;
import com.technote.blog.model.req.LinkApplySubmitReq;
import com.technote.blog.model.resp.LinkApplyResp;
import com.technote.blog.model.resp.PublicLinkApplyResp;
import com.technote.blog.service.LinkApplyService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 友链申请服务实现。
 */
@Service
@RequiredArgsConstructor
public class LinkApplyServiceImpl implements LinkApplyService {

    private final BlogLinkApplyMapper linkApplyMapper;

    private final BlogLinkMapper linkMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicLinkApplyResp submitLinkApply(LinkApplySubmitReq req) {
        String siteUrl = normalizeSiteUrl(req.getSiteUrl());
        validateOptionalIconUrl(req.getIconUrl());
        ensureNoExistingLink(siteUrl);
        Long pendingCount = linkApplyMapper.selectCount(new LambdaQueryWrapper<BlogLinkApply>()
                .eq(BlogLinkApply::getSiteUrl, siteUrl)
                .eq(BlogLinkApply::getStatus, AuditStatusEnum.PENDING.getCode()));
        if (pendingCount > 0) {
            throw new BaseException(409, "该网站已有待审核申请，请勿重复提交");
        }

        BlogLinkApply apply = new BlogLinkApply();
        apply.setSiteName(req.getSiteName().trim());
        apply.setSiteUrl(siteUrl);
        apply.setIconUrl(trimToNull(req.getIconUrl()));
        apply.setDescription(trimToNull(req.getDescription()));
        apply.setApplicantEmail(req.getApplicantEmail().trim());
        apply.setStatus(AuditStatusEnum.PENDING.getCode());
        try {
            linkApplyMapper.insert(apply);
        } catch (DuplicateKeyException e) {
            throw new BaseException(409, "该网站已有待审核申请，请勿重复提交");
        }
        return toPublicLinkApplyResp(apply);
    }

    @Override
    public PageResp<LinkApplyResp> pageAdminLinkApplies(AuditPageQueryReq req) {
        validateQueryStatus(req.getStatus());
        IPage<BlogLinkApply> page = linkApplyMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                new LambdaQueryWrapper<BlogLinkApply>()
                        .eq(req.getStatus() != null, BlogLinkApply::getStatus, req.getStatus())
                        .orderByAsc(BlogLinkApply::getStatus)
                        .orderByDesc(BlogLinkApply::getCreateTime));
        List<LinkApplyResp> records = page.getRecords().stream().map(this::toLinkApplyResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditLinkApply(Long id, LinkApplyAuditReq req) {
        validateAudit(req);
        BlogLinkApply apply = getLinkApplyOrThrow(id);
        if (AuditStatusEnum.APPROVED.getCode().equals(req.getStatus())) {
            ensureNoExistingLink(apply.getSiteUrl());
        }

        Long userId = currentUserId();
        LambdaUpdateWrapper<BlogLinkApply> wrapper = new LambdaUpdateWrapper<BlogLinkApply>()
                .eq(BlogLinkApply::getId, apply.getId())
                .eq(BlogLinkApply::getStatus, AuditStatusEnum.PENDING.getCode())
                .set(BlogLinkApply::getStatus, req.getStatus())
                .set(BlogLinkApply::getAuditRemark, trimToNull(req.getAuditRemark()))
                .set(BlogLinkApply::getAuditBy, userId)
                .set(BlogLinkApply::getAuditTime, LocalDateTime.now());
        if (linkApplyMapper.update(null, wrapper) == 0) {
            throw new BaseException(409, "友链申请已审核，请刷新后重试");
        }

        if (AuditStatusEnum.APPROVED.getCode().equals(req.getStatus())) {
            BlogLink link = new BlogLink();
            link.setSiteName(apply.getSiteName());
            link.setSiteUrl(apply.getSiteUrl());
            link.setIconUrl(apply.getIconUrl());
            link.setDescription(apply.getDescription());
            link.setSortOrder(0);
            link.setStatus(VisibleStatusEnum.HIDDEN.getCode());
            link.setCreateBy(userId);
            link.setUpdateBy(userId);
            try {
                linkMapper.insert(link);
            } catch (DuplicateKeyException e) {
                throw new BaseException(409, "该网站已存在于友情链接中，请刷新后重试");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLinkApply(Long id) {
        getLinkApplyOrThrow(id);
        linkApplyMapper.deleteById(id);
    }

    private BlogLinkApply getLinkApplyOrThrow(Long id) {
        BlogLinkApply apply = linkApplyMapper.selectById(id);
        if (apply == null) {
            throw new BaseException(404, "友链申请不存在");
        }
        return apply;
    }

    private void validateQueryStatus(Integer status) {
        if (status != null && !AuditStatusEnum.contains(status)) {
            throw new BaseException(400, "申请状态不正确");
        }
    }

    private void validateAudit(LinkApplyAuditReq req) {
        if (!AuditStatusEnum.contains(req.getStatus()) || AuditStatusEnum.PENDING.getCode().equals(req.getStatus())) {
            throw new BaseException(400, "审核状态不正确");
        }
        if (AuditStatusEnum.REJECTED.getCode().equals(req.getStatus()) && StrUtil.isBlank(req.getAuditRemark())) {
            throw new BaseException(400, "驳回申请时请填写审核备注");
        }
    }

    private void ensureNoExistingLink(String siteUrl) {
        Long linkCount = linkMapper.selectCount(new LambdaQueryWrapper<BlogLink>()
                .eq(BlogLink::getSiteUrl, siteUrl));
        if (linkCount > 0) {
            throw new BaseException(409, "该网站已存在于友情链接中");
        }
    }

    private String normalizeSiteUrl(String siteUrl) {
        return validateHttpUrl(siteUrl, "网站地址");
    }

    private void validateOptionalIconUrl(String iconUrl) {
        if (StrUtil.isNotBlank(iconUrl)) {
            validateHttpUrl(iconUrl, "图标地址");
        }
    }

    private String validateHttpUrl(String value, String fieldName) {
        String url = value.trim();
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (StrUtil.isBlank(uri.getHost())
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new BaseException(400, fieldName + "必须是有效的 HTTP(S) 地址");
            }
        } catch (URISyntaxException e) {
            throw new BaseException(400, fieldName + "必须是有效的 HTTP(S) 地址");
        }
        return url;
    }

    private String trimToNull(String value) {
        return StrUtil.isBlank(value) ? null : value.trim();
    }

    private PublicLinkApplyResp toPublicLinkApplyResp(BlogLinkApply apply) {
        PublicLinkApplyResp resp = new PublicLinkApplyResp();
        resp.setId(apply.getId());
        resp.setSiteName(apply.getSiteName());
        resp.setSiteUrl(apply.getSiteUrl());
        resp.setIconUrl(apply.getIconUrl());
        resp.setDescription(apply.getDescription());
        resp.setCreateTime(apply.getCreateTime());
        return resp;
    }

    private LinkApplyResp toLinkApplyResp(BlogLinkApply apply) {
        LinkApplyResp resp = new LinkApplyResp();
        resp.setId(apply.getId());
        resp.setSiteName(apply.getSiteName());
        resp.setSiteUrl(apply.getSiteUrl());
        resp.setIconUrl(apply.getIconUrl());
        resp.setDescription(apply.getDescription());
        resp.setApplicantEmail(apply.getApplicantEmail());
        resp.setStatus(apply.getStatus());
        resp.setAuditRemark(apply.getAuditRemark());
        resp.setAuditBy(apply.getAuditBy());
        resp.setAuditTime(apply.getAuditTime());
        resp.setCreateTime(apply.getCreateTime());
        resp.setUpdateTime(apply.getUpdateTime());
        return resp;
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
