package com.abtnetworks.totems.common.enums;

public enum ApplyTypeEnum {

    FASTL4("Performance(Layer 4)","fastL4"),
    FASTHTTP("Performance(HTTP)","fasthttp"),
    TCP("Standard","tcp"),
    IPFORWARD("Ip-forward","Ip-forward"),
    I2FORWARD("I2-forward","I2-forward"),
    INTERNAL("Internal","Internal");
    ;


    private String key;

    private String name;

    public String getKey() {
        return key;
    }


    public String getName() {
        return name;
    }

    ApplyTypeEnum(String key,String name){
        this.key = key;
        this.name = name;
    }

    public static String getNameByKey(String key){
        for (ApplyTypeEnum ApplyTypeEnum: ApplyTypeEnum.values() ) {
            if(ApplyTypeEnum.getKey().equalsIgnoreCase(key)){
                return ApplyTypeEnum.getName();
            }
        }
        return null;
    }
}
