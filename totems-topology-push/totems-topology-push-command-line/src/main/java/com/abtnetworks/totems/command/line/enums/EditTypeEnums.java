package com.abtnetworks.totems.command.line.enums;

public enum EditTypeEnums {

    SRC_ZONE(1, "源域"),
    DST_ZONE(2,"目的域"),
    SRC_ADDRESS(3, "源IP"),
    DST_ADDRESS(4,"目的IP"),
    SERVICE(5,"服务");


    private int code;
    private String description;


    EditTypeEnums(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
