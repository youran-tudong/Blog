package com.technote.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.technote.blog.entity.BlogSetting;
import com.technote.blog.mapper.BlogSettingMapper;
import com.technote.blog.model.req.SettingSaveReq;
import com.technote.blog.model.resp.PublicSettingResp;
import com.technote.blog.model.resp.SettingResp;
import com.technote.blog.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统设置服务实现。
 */
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final BlogSettingMapper settingMapper;

    @Override
    public SettingResp getSetting() {
        BlogSetting setting = getFirstSetting();
        if (setting == null) {
            setting = buildDefaultSetting();
        }
        return toSettingResp(setting);
    }

    @Override
    public PublicSettingResp getPublicSetting() {
        BlogSetting setting = getFirstSetting();
        if (setting == null) {
            setting = buildDefaultSetting();
        }
        return toPublicSettingResp(setting);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettingResp saveSetting(SettingSaveReq req) {
        BlogSetting setting = getFirstSetting();
        if (setting == null) {
            setting = new BlogSetting();
            fillSetting(setting, req);
            settingMapper.insert(setting);
            return toSettingResp(setting);
        }
        fillSetting(setting, req);
        settingMapper.updateById(setting);
        return toSettingResp(setting);
    }

    private BlogSetting getFirstSetting() {
        return settingMapper.selectOne(new LambdaQueryWrapper<BlogSetting>()
                .orderByAsc(BlogSetting::getId)
                .last("LIMIT 1"));
    }

    private BlogSetting buildDefaultSetting() {
        BlogSetting setting = new BlogSetting();
        setting.setSiteTitle("TechNote");
        setting.setSiteDescription("面向技术创作者的个人内容管理与分享系统");
        setting.setSiteKeywords("TechNote,技术博客,内容管理");
        setting.setAuthorName("TechNote Admin");
        setting.setAuthorProfile("专注技术沉淀与内容创作。");
        setting.setAboutContent("欢迎来到 TechNote。");
        return setting;
    }

    private void fillSetting(BlogSetting setting, SettingSaveReq req) {
        setting.setSiteTitle(req.getSiteTitle().trim());
        setting.setSiteDescription(StrUtil.blankToDefault(req.getSiteDescription(), null));
        setting.setSiteKeywords(StrUtil.blankToDefault(req.getSiteKeywords(), null));
        setting.setIcpNo(StrUtil.blankToDefault(req.getIcpNo(), null));
        setting.setAuthorName(StrUtil.blankToDefault(req.getAuthorName(), null));
        setting.setAuthorAvatar(StrUtil.blankToDefault(req.getAuthorAvatar(), null));
        setting.setAuthorProfile(StrUtil.blankToDefault(req.getAuthorProfile(), null));
        setting.setAnnouncement(StrUtil.blankToDefault(req.getAnnouncement(), null));
        setting.setAboutContent(StrUtil.blankToDefault(req.getAboutContent(), null));
    }

    private SettingResp toSettingResp(BlogSetting setting) {
        SettingResp resp = new SettingResp();
        resp.setId(setting.getId());
        resp.setSiteTitle(setting.getSiteTitle());
        resp.setSiteDescription(setting.getSiteDescription());
        resp.setSiteKeywords(setting.getSiteKeywords());
        resp.setIcpNo(setting.getIcpNo());
        resp.setAuthorName(setting.getAuthorName());
        resp.setAuthorAvatar(setting.getAuthorAvatar());
        resp.setAuthorProfile(setting.getAuthorProfile());
        resp.setAnnouncement(setting.getAnnouncement());
        resp.setAboutContent(setting.getAboutContent());
        resp.setCreateTime(setting.getCreateTime());
        resp.setUpdateTime(setting.getUpdateTime());
        return resp;
    }

    private PublicSettingResp toPublicSettingResp(BlogSetting setting) {
        PublicSettingResp resp = new PublicSettingResp();
        resp.setSiteTitle(setting.getSiteTitle());
        resp.setSiteDescription(setting.getSiteDescription());
        resp.setSiteKeywords(setting.getSiteKeywords());
        resp.setIcpNo(setting.getIcpNo());
        resp.setAuthorName(setting.getAuthorName());
        resp.setAuthorAvatar(setting.getAuthorAvatar());
        resp.setAuthorProfile(setting.getAuthorProfile());
        resp.setAnnouncement(setting.getAnnouncement());
        resp.setAboutContent(setting.getAboutContent());
        return resp;
    }
}
