package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

@Data
public class IpAddressRangeDTO extends IpAddressDTO {

    /**
     * 开始ip
     */
    private String start;

    /**
     * 结束ip
     */
    private String end;

}
