package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

@Data
public class PortDTO {

    private String type;

    //超时时间
    private Long idleTimeout;

}
