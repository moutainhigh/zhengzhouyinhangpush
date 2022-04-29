package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import lombok.Data;

@Data
public class IpAddressSubnetIntDTO extends IpAddressDTO {

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
    private MaskTypeEnum type;

}
