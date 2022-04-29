package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import lombok.Data;

@Data
public class IpAddressSubnetStrDTO extends IpAddressDTO {

    /**
     * ip
     */
    private String ip;

    /**
     * 掩码 ip类型
     */
    private String mask;

    /**
     * 掩码，反掩码
     */
    private MaskTypeEnum type;

}
