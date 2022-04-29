package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import lombok.Data;

@Data
public class IpAddressParamDTO {

    /**
     * 类型
     */
    RuleIPTypeEnum ipTypeEnum;

    private String name;

    /**
     * 地址对象名/系统名称/地址描述
     */
    private String ipSystem;

    /**
     * 单ip
     */
    private String[] singleIpArray;
    /**
     * 范围
     */
    private IpAddressRangeDTO[] rangIpArray;
    /**
     * 子网 掩码int
     */
    private IpAddressSubnetIntDTO[] subnetIntIpArray;
    /**
     * 子网 掩码str
     */
    private IpAddressSubnetStrDTO[] subnetStrIpArray;

    /**
     * 引用地址对象
     */
    String[] objectNameRefArray;

    /**
     * 引用地址组对象
     */
    String[] objectGroupNameRefArray;

    /**
     * 域名地址对象
     */
    String[] hosts;

    public IpAddressParamDTO() {
    }

    public IpAddressParamDTO(String name) {
        this.name = name;
    }

    public IpAddressParamDTO(RuleIPTypeEnum ipTypeEnum, String[] singleIpArray) {
        this.ipTypeEnum = ipTypeEnum;
        this.singleIpArray = singleIpArray;
    }

    public IpAddressParamDTO(RuleIPTypeEnum ipTypeEnum, IpAddressRangeDTO[] rangIpArray) {
        this.ipTypeEnum = ipTypeEnum;
        this.rangIpArray = rangIpArray;
    }

    public IpAddressParamDTO(RuleIPTypeEnum ipTypeEnum, IpAddressSubnetIntDTO[] subnetIntIpArray) {
        this.ipTypeEnum = ipTypeEnum;
        this.subnetIntIpArray = subnetIntIpArray;
    }

    public IpAddressParamDTO(RuleIPTypeEnum ipTypeEnum, IpAddressSubnetStrDTO[] subnetStrIpArray) {
        this.ipTypeEnum = ipTypeEnum;
        this.subnetStrIpArray = subnetStrIpArray;
    }
}
