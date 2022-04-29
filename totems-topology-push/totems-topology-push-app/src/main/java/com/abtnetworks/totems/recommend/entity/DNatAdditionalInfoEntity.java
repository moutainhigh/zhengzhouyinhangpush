package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.common.dto.AutoRecommendFortinetDnatSpecialDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@ApiModel("DNat补充数据")
public class DNatAdditionalInfoEntity {

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("转换后地址")
    String postIpAddress;

    @ApiModelProperty("转换后端口")
    String postPort;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")
    private String outDevItfAlias;

    @ApiModelProperty("转换后服务列表")
    private List<ServiceDTO> postServiceList;

    @ApiModelProperty("转换前服务")
    private List<ServiceDTO> preServiceDTOList;

    @ApiModelProperty("自动开通生成飞塔目的NAT处理")
    AutoRecommendFortinetDnatSpecialDTO fortinetDnatSpecialDTO;
}
