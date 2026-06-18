package com.technote.blog.enums;

/**
 * 回收站资源类型。
 */
public enum RecycleResourceTypeEnum {

    ARTICLE("ARTICLE", "文章");

    private final String code;

    private final String description;

    RecycleResourceTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(String code) {
        if (code == null) {
            return false;
        }
        for (RecycleResourceTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
