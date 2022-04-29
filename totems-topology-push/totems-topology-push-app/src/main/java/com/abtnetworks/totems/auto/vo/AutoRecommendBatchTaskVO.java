package com.abtnetworks.totems.auto.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @desc    自动开通工单对接批量需求VO
 * @author liuchanghao
 * @date 2021-08-16 14:01
 */
@ApiModel("新建自动开通工单时，页面传递到后台")
@Data
public class AutoRecommendBatchTaskVO {

    @ApiModelProperty("需求id")
    private String id;

    @ApiModelProperty("访问类型（0：内网互访；1：内网访问互联网；2：互联网访问内网） ")
    @NotNull(message = "访问类型不能为空（0：内网互访；1：内网访问互联网；2：互联网访问内网）")
    private Integer accessType;

    @ApiModelProperty("开始时间-时间戳格式")
    private Long startTime;

    @ApiModelProperty("结束时间-时间戳格式")
    private Long endTime;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("源ip")
    private String srcIp;

    @ApiModelProperty("目的ip")
    private String dstIp;

    @ApiModelProperty("协议")
    private List<ServiceDTO> serviceList;

}
