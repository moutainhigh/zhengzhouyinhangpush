package com.abtnetworks.totems.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @desc    域名策略下的ip类型枚举
 * @author liuchanghao
 * @date 2020-11-16 10:42
 */
public enum UrlTypeEnum {

    IPV4(0,"IPV4"),
    IPV6(1, "IPV6")
    ;

    private Integer code;

    private String desc;


    UrlTypeEnum(Integer code, String desc) {
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
    public static UrlTypeEnum getIpTypeByDesc(String desc){
        for (UrlTypeEnum ipTypeEnum: UrlTypeEnum.values() ) {
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
        }
        return ipTypeNumber;
    }
}
