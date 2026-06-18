package com.technote.blog.enums;

/**
 * 文章可见性。
 */
public enum ArticleVisibilityEnum {

    PRIVATE(0, "私密"),
    PUBLIC(1, "公开");

    private final Integer code;

    private final String description;

    ArticleVisibilityEnum(Integer code, String description) {
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
        for (ArticleVisibilityEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

