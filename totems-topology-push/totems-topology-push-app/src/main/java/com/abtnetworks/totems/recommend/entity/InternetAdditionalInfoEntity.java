package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("互联网开通补充数据")
@Data
public class InternetAdditionalInfoEntity {

    @ApiModelProperty("入口子网")
    List<SubnetEntity> entrySubnetList;

    @ApiModelProperty("出口子网")
    List<SubnetEntity> exitSubnetList;
}
