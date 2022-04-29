package com.abtnetworks.totems.common.enums;

import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_ICMP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_TCP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_UDP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_STR_VALUE_ICMP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_STR_VALUE_ICMPV6;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_STR_VALUE_TCP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_STR_VALUE_UDP;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/2/24
 */
public enum ProtocolEnum {
    TCP(POLICY_NUM_VALUE_TCP,POLICY_STR_VALUE_TCP),
    ICMP(POLICY_NUM_VALUE_ICMP, POLICY_STR_VALUE_ICMP),
    ICMPV6("58", POLICY_STR_VALUE_ICMPV6),
    UDP(POLICY_NUM_VALUE_UDP,POLICY_STR_VALUE_UDP)
    ;

    private String code;

    private String desc;




    ProtocolEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getDescByCode(String code){
        for (ProtocolEnum protocolEnum: ProtocolEnum.values() ) {
            if(protocolEnum.getCode().equalsIgnoreCase(code)){
                return protocolEnum.getDesc();
            }
        }
        return null;
    }

}
