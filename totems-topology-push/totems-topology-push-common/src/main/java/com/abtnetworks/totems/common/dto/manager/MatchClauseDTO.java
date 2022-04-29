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
public class MatchClauseDTO {

    List<MatchIpDTO> srcIp;

    List<MatchIpDTO> dstIp;

    List<MatchServiceValueDTO> services;

    Boolean srcIPNegate ;

    Boolean dstIPNegate;

}
