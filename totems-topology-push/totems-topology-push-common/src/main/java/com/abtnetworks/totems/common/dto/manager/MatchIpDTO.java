package com.abtnetworks.totems.common.dto.manager;

import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/16
 */
@Data
public class MatchIpDTO {

    private Integer type;

    private IpRangeDTO ip4Range;


}
