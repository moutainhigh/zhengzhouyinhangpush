package com.abtnetworks.totems.commandLine.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>子网IP(String子网)")
@Data
public class IpAddressSubnetStrVO {

    /**
     * ip
     */
    private String ip;

    /**
     * 掩码 ip类型
     */
    private String mask;

    /**
     * 掩码，反掩码，int类型不区分正反掩码
     */
    private Integer MaskTypeEnumCode;
}
