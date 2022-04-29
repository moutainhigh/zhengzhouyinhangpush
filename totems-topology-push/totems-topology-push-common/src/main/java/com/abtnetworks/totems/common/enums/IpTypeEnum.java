package com.abtnetworks.totems.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @desc    ip类型枚举
 * @author liuchanghao
 * @date 2020-11-16 10:42
 */
public enum IpTypeEnum {

    IPV4(0,"IPV4"),
    IPV6(1, "IPV6"),
    URL(2, "URL")
    ;

    private Integer code;

    private String desc;


    IpTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据描述查枚举
     * @param desc
     * @return
     */
    public static IpTypeEnum  getIpTypeByDesc(String desc){
        for (IpTypeEnum ipTypeEnum: IpTypeEnum.values() ) {
            if(ipTypeEnum.getDesc().equalsIgnoreCase(desc)){
                return ipTypeEnum;
            }
        }
        return IPV4;
    }
    /**
     * 将字符串描述转成整型
     * @param ipType
     * @return
     */
    public static Integer covertString2Int(String ipType){
        Integer ipTypeNumber = IPV4.getCode();


        if(StringUtils.equalsAnyIgnoreCase(ipType, IPV4.getDesc())){
            ipTypeNumber = IPV4.getCode();
        } else if(StringUtils.equalsAnyIgnoreCase(ipType, IPV6.getDesc())){
            ipTypeNumber = IPV6.getCode();
        } else {
            ipTypeNumber = URL.getCode();
        }
        return ipTypeNumber;
    }
}
