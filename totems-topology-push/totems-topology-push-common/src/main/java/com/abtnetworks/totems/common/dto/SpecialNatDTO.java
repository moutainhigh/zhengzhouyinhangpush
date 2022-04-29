package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @desc 飞塔特殊NAT场景
 * @date 2021/12/29 11:39
 */
@Data
public class SpecialNatDTO {

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("转换后源地址")
    String postSrcIp;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("转换后目的地址")
    String postDstIp;

    @ApiModelProperty("转换后目的端口")
    String postPort;

    @ApiModelProperty("服务列表")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("转换后服务列表")
    List<ServiceDTO> postServiceList;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("复用查到的已经存在的poolName  仅飞塔用到")
    String existPoolName;

    @ApiModelProperty("复用查到的已经存在的虚拟ip名称  仅飞塔用到")
    String existVipName;

    @ApiModelProperty("snat 转出接口标识")
    Boolean convertOutItf = false;
}
