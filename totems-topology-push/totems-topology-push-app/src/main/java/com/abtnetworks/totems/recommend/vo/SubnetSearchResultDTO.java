package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.whale.baseapi.ro.SubnetRO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/22
 */
@ApiModel("子网搜索结果接口")
@Data
public class SubnetSearchResultDTO {
    @ApiModelProperty("源地址关联子网")
    List<SubnetRO> srcSubnetRO;
    @ApiModelProperty("源地址")
    String srcIp;

}
