package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

@Data
public class PortParamDTO {

    /**
     * 范围
     */
    private PortRangeDTO rangeDTO;

    /**
     * 单个，多个端口
     */
    private PortListDTO listDTO;

    public PortParamDTO(PortRangeDTO rangeDTO) {
        this.rangeDTO = rangeDTO;
    }

    public PortParamDTO(PortListDTO listDTO) {
        this.listDTO = listDTO;
    }
}
