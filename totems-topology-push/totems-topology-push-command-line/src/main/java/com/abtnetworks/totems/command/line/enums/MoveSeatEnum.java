package com.abtnetworks.totems.command.line.enums;

/**
 * @author luwei
 * @date 2019/5/27
 */
public enum MoveSeatEnum {

    FIRST(1, "最前面"),
    LAST(2,"最后面"),
    BEFORE(3, "在之前",  "before"),
    AFTER(4, "在之后", "after"),
    UNKNOWN(5,"不移动", "");
    ;


    private int code;

    private String desc;

    private String key;

    MoveSeatEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    MoveSeatEnum(int code, String desc, String key) {
        this.code = code;
        this.desc = desc;
        this.key = key;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }


    /**
     * 根据字符串数值获取
     * @param code
     * @return
     */
    public static MoveSeatEnum getByCode(Integer code) {
        if(code == null){
            return MoveSeatEnum.UNKNOWN;
        }
        for (MoveSeatEnum moveSeatEnum:MoveSeatEnum.values()) {
            if(moveSeatEnum.getCode() == code){
                return moveSeatEnum;
            }
        }
        return null;
    }
}
