package com.abtnetworks.totems.command.line.enums;


/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/4/23
 */
public enum ProtocolTypeEnum {

    ANY(0,"any"),
    ICMP(1, "icmp"),
    TCP(6, "tcp"),
    UDP(17, "udp"),
    ICMP6(58, "icmp6"),
    PROTOCOL(200, "protocol")
    ;

    private int code;
    private String type;

    ProtocolTypeEnum(int code, String type) {
        this.code = code;
        this.type = type;
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

    public static ProtocolTypeEnum getByType(String type){
        for (ProtocolTypeEnum typeEnum:ProtocolTypeEnum.values()) {
            if(typeEnum.getType().equalsIgnoreCase(type)){
                return typeEnum;
            }
        }
        return null;
    }

    /**
     * 根据字符串数值获取协议名称
     * @param code
     * @return 协议名称
     */
    public static ProtocolTypeEnum getByCode(Integer code) {
        if(code == null){
            return null;
        }
        for (ProtocolTypeEnum typeEnum:ProtocolTypeEnum.values()) {
            if(typeEnum.getCode() == code.intValue()){
                return typeEnum;
            }
        }
        return null;
    }
}
