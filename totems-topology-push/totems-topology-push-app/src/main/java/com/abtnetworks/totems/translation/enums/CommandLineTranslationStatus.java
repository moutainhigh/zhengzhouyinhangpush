package com.abtnetworks.totems.translation.enums;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/2/2 16:51'.
 */
public enum CommandLineTranslationStatus {

    NOT_SETTING("10", "待翻译映射", ""),
    NOT_STARTED("20", "待转换", ""),
    CONVERTING("30", "转换中", ""),
    SUCCESS("40", "成功", ""),
    FAIL("50", "失败", "");

    CommandLineTranslationStatus(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    private String code;
    private String name;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static CommandLineTranslationStatus getStatusByCode(String code) {
        for (CommandLineTranslationStatus status : CommandLineTranslationStatus.values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
