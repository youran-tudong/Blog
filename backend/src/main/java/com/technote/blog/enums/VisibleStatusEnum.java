package com.technote.blog.enums;

/**
 * 前台可见状态。
 */
public enum VisibleStatusEnum {

    HIDDEN(0, "隐藏"),
    VISIBLE(1, "显示");

    private final Integer code;

    private final String description;

    VisibleStatusEnum(Integer code, String description) {
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
        for (VisibleStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

