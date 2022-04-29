package com.abtnetworks.totems.commandLine.vo;

import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>子网IP(int子网)")
@Data
public class IpAddressSubnetIntVO {

    /**
     * ip
     */
    private String ip;

    /**
     * 掩码 int类型 | 前缀
     */
    private int mask;

    /**
     * 掩码，反掩码，int类型不区分正反掩码
     */
    private Integer MaskTypeEnumCode;
}
