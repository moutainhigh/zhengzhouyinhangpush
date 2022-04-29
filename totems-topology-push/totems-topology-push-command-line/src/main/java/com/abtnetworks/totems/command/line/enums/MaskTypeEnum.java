package com.abtnetworks.totems.command.line.enums;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/4/23
 */
public enum MaskTypeEnum {

    mask(1,"mask", "掩码"),
    wildcard_mask(2,"wildcard_mask", "反掩码");

    private int code;
    private String type;
    private String description;

    MaskTypeEnum(int code, String type, String description) {
        this.code = code;
        this.type = type;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static MaskTypeEnum getByCode(Integer code){
        if(code == null){
            return null;
        }
        for (MaskTypeEnum maskTypeEnum:MaskTypeEnum.values()) {
            if(maskTypeEnum.getCode() == code.intValue()){
                return maskTypeEnum;
            }
        }
        return null;
    }
}
