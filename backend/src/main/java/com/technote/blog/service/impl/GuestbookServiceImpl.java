package com.technote.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.blog.entity.BlogGuestbook;
import com.technote.blog.enums.AuditStatusEnum;
import com.technote.blog.mapper.BlogGuestbookMapper;
import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.AuditReq;
import com.technote.blog.model.req.GuestbookSubmitReq;
import com.technote.blog.model.resp.GuestbookResp;
import com.technote.blog.model.resp.PublicGuestbookResp;
import com.technote.blog.service.GuestbookService;
import com.technote.common.exception.BaseException;
import com.technote.common.model.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言服务实现。
 */
@Service
@RequiredArgsConstructor
public class GuestbookServiceImpl implements GuestbookService {

    private final BlogGuestbookMapper guestbookMapper;

    @Override
    public List<PublicGuestbookResp> listApprovedGuestbooks() {
        return guestbookMapper.selectList(new LambdaQueryWrapper<BlogGuestbook>()
                        .eq(BlogGuestbook::getStatus, AuditStatusEnum.APPROVED.getCode())
                        .orderByDesc(BlogGuestbook::getCreateTime))
                .stream()
                .map(this::toPublicGuestbookResp)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicGuestbookResp submitGuestbook(GuestbookSubmitReq req) {
        BlogGuestbook guestbook = new BlogGuestbook();
        guestbook.setNickname(req.getNickname().trim());
        guestbook.setEmail(StrUtil.blankToDefault(req.getEmail(), null));
        guestbook.setContent(req.getContent().trim());
        guestbook.setStatus(AuditStatusEnum.PENDING.getCode());
        guestbookMapper.insert(guestbook);
        return toPublicGuestbookResp(guestbook);
    }

    @Override
    public PageResp<GuestbookResp> pageAdminGuestbooks(AuditPageQueryReq req) {
        IPage<BlogGuestbook> page = guestbookMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                new LambdaQueryWrapper<BlogGuestbook>()
                        .eq(req.getStatus() != null, BlogGuestbook::getStatus, req.getStatus())
                        .orderByAsc(BlogGuestbook::getStatus)
                        .orderByDesc(BlogGuestbook::getCreateTime));
        List<GuestbookResp> records = page.getRecords().stream().map(this::toGuestbookResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditGuestbook(Long id, AuditReq req) {
        validateAuditStatus(req.getStatus());
        BlogGuestbook guestbook = getGuestbookOrThrow(id);
        LambdaUpdateWrapper<BlogGuestbook> wrapper = new LambdaUpdateWrapper<BlogGuestbook>()
                .eq(BlogGuestbook::getId, guestbook.getId())
                .eq(BlogGuestbook::getStatus, AuditStatusEnum.PENDING.getCode())
                .set(BlogGuestbook::getStatus, req.getStatus())
                .set(BlogGuestbook::getReplyContent, StrUtil.blankToDefault(req.getReplyContent(), null))
                .set(BlogGuestbook::getAuditBy, currentUserId())
                .set(BlogGuestbook::getAuditTime, LocalDateTime.now());
        if (guestbookMapper.update(null, wrapper) == 0) {
            throw new BaseException(409, "留言已审核，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGuestbook(Long id) {
        getGuestbookOrThrow(id);
        guestbookMapper.deleteById(id);
    }

    private BlogGuestbook getGuestbookOrThrow(Long id) {
        BlogGuestbook guestbook = guestbookMapper.selectById(id);
        if (guestbook == null) {
            throw new BaseException(404, "留言不存在");
        }
        return guestbook;
    }

    private void validateAuditStatus(Integer status) {
        if (!AuditStatusEnum.contains(status) || AuditStatusEnum.PENDING.getCode().equals(status)) {
            throw new BaseException(400, "审核状态不正确");
        }
    }

    private PublicGuestbookResp toPublicGuestbookResp(BlogGuestbook guestbook) {
        PublicGuestbookResp resp = new PublicGuestbookResp();
        resp.setId(guestbook.getId());
        resp.setNickname(guestbook.getNickname());
        resp.setContent(guestbook.getContent());
        resp.setReplyContent(guestbook.getReplyContent());
        resp.setCreateTime(guestbook.getCreateTime());
        return resp;
    }

    private GuestbookResp toGuestbookResp(BlogGuestbook guestbook) {
        GuestbookResp resp = new GuestbookResp();
        resp.setId(guestbook.getId());
        resp.setNickname(guestbook.getNickname());
        resp.setEmail(guestbook.getEmail());
        resp.setContent(guestbook.getContent());
        resp.setStatus(guestbook.getStatus());
        resp.setReplyContent(guestbook.getReplyContent());
        resp.setAuditBy(guestbook.getAuditBy());
        resp.setAuditTime(guestbook.getAuditTime());
        resp.setCreateTime(guestbook.getCreateTime());
        resp.setUpdateTime(guestbook.getUpdateTime());
        return resp;
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }
}
