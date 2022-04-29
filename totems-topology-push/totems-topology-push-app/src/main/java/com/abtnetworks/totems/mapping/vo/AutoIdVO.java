package com.abtnetworks.totems.mapping.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc    工单检测VO
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */
@Data
public class AutoIdVO {

    @ApiModelProperty("id集合")
    private List<Integer> idList;

    @ApiModelProperty("Nat或路由id")
    private Integer natOrRouteId;


}
