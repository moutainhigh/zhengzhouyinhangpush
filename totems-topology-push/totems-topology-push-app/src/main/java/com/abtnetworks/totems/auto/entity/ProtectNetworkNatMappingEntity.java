package com.abtnetworks.totems.auto.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-10 15:52
 */
@Data
@ApiModel("防护网段配置Nat映射实体类")
public class ProtectNetworkNatMappingEntity extends BaseEntity {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("防护网段配置主键ID")
    private Long configId;

    @ApiModelProperty("Nat类型（N：无Nat；S:源Nat；D:目的Nat）")
    private String natType;

    @ApiModelProperty("内网端口")
    private String insideIp;

    @ApiModelProperty("内网协议")
    private String insideProtocol;

    @ApiModelProperty("内网端口")
    private String insidePorts;

    @ApiModelProperty("外网IP")
    private String outsideIp;

    @ApiModelProperty("外网协议")
    private String outsideProtocol;

    @ApiModelProperty("外网端口")
    private String outsidePorts;

}