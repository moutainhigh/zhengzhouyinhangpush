package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

@Data
public class InterfaceParamDTO {

    /**
     * 接口名称 单个
     */
    private String name;

    /**
     * 接口名称 多个
     */
    private String[] nameArray;

    public InterfaceParamDTO(String name) {
        this.name = name;
    }

    public InterfaceParamDTO(String[] nameArray) {
        this.nameArray = nameArray;
    }
}
