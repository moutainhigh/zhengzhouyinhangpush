package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

@Data
public class ZoneParamDTO {

    /**
     * 域名称 单个
     */
    private String name;

    /**
     * 域名称 多个
     */
    private String[] nameArray;

    public ZoneParamDTO(String name) {
        this.name = name;
    }

    public ZoneParamDTO(String[] nameArray) {
        this.nameArray = nameArray;
    }

    public ZoneParamDTO(String name,String[] nameArray) {
        this.name = name;
        this.nameArray = nameArray;
    }


}
