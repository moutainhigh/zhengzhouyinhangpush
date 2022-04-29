package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import lombok.Data;

@Data
public class IpAddressDTO {

    /**
     * ip类型
     */
    private RuleIPTypeEnum ipTypeEnum;

}
