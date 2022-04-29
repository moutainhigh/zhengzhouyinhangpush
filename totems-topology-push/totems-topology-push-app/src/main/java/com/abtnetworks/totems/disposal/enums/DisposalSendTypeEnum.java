package com.abtnetworks.totems.disposal.enums;

/**
 * 派发类型
 * @author luwei
 * @date 2019/11/12
 */
public enum DisposalSendTypeEnum {

    AUDIT(0,"人工审核"),
    AUTO(1, "自动"),
    ;

    private Integer code;

    private String desc;

    DisposalSendTypeEnum() {
    }

    DisposalSendTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }



}
