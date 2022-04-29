package com.abtnetworks.totems.mapping.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @desc    工单检测VO
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */
@Data
public class OrderCheckVO {

    @ApiModelProperty("主题/工单号")
    private String theme;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("源ip")
    private String srcIp;

    @ApiModelProperty("是否指定转换后源IP，0：不指定；1：指定")
    private Integer isAppointPostSrcIp;

    @ApiModelProperty("源地址转换后IP")
    private String appointPostSrcIp;

    @ApiModelProperty("目的ip")
    private String dstIp;

    @ApiModelProperty("协议")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("长连接")
    private Integer IdleTimeout;

    @ApiModelProperty("当前登录的用户名")
    private String userName;

    @ApiModelProperty("源地址所属系统")
    String srcIpSystem;

    @ApiModelProperty("目的地址所属系统")
    String dstIpSystem;


}
