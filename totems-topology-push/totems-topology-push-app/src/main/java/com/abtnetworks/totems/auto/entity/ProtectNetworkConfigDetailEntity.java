package com.abtnetworks.totems.auto.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-10 15:52
 */
@Data
@ApiModel("防护网段配置明细实体类")
public class ProtectNetworkConfigDetailEntity extends BaseEntity {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("防护网段配置主键ID")
    private Long configId;

    @ApiModelProperty("IPV4防护网段IP起始数据")
    private Long ipv4Start;

    @ApiModelProperty("IPV4防护网段IP终止数据")
    private Long ipv4End;

    @ApiModelProperty("IPV6防护网段IP起始数据")
    private String ipv6Start;

    @ApiModelProperty("IPV6防护网段IP终止数据")
    private String ipv6End;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("创建时间")
    private Date createTime;

}