package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

import java.util.List;

@Data
public class PortListDTO extends PortDTO {

    /**
     * 数字类型端口
     */
    private List<Integer> portList;

    /**
     * 字符串 非数字端口
     */
    private List<String> strList;

}
