package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

@Data
public class PortRangeDTO extends PortDTO {

    /**
     * 开始端口
     */
    private Integer start;

    /**
     * 结束端口
     */
    private Integer end;

    public PortRangeDTO() {
    }

    public PortRangeDTO(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }
}
