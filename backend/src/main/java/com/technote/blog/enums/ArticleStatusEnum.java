package com.technote.blog.enums;

/**
 * 文章状态。
 */
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "发布"),
    AUTO_DRAFT(2, "自动草稿");

    private final Integer code;

    private final String description;

    ArticleStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(Integer code) {
        if (code == null) {
            return false;
        }
        for (ArticleStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

