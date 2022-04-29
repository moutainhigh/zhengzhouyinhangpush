package com.abtnetworks.totems.push.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/2/24
 */
@Data
@ApiModel("策略下发前检查关联nat的值")
public class CheckRelevancyNatOrderVO {

    @ApiModelProperty("主键流水ID")
    private Integer id;

    @ApiModelProperty("主键流水ID")
    private Integer taskId;

}
