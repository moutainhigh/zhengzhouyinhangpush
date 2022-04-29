package com.abtnetworks.totems.common.dto.manager;

import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/16
 */
@Data
public class MatchServiceValueDTO {

    private String type;

    private String protocolName;

    private String dstPortOp;

    private List<String> dstPortValues;
}
