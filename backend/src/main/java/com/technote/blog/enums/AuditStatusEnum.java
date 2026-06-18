package com.technote.blog.enums;

/**
 * 内容审核状态。
 */
public enum AuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "驳回");

    private final Integer code;

    private final String description;

    AuditStatusEnum(Integer code, String description) {
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
        for (AuditStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
