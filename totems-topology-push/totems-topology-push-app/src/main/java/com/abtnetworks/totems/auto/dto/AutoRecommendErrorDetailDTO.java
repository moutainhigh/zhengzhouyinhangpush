package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @desc    自动开通任务错误信息详情DTO
 * @author liuchanghao
 * @date 2021-07-14 18:48
 */
@Data
public class AutoRecommendErrorDetailDTO {

    @ApiModelProperty("墙上已有的冲突策略")
    List<PolicyDetailVO> ruleList;

    @ApiModelProperty("冲突策略的防火墙IP")
    private String deviceIp;

    @ApiModelProperty("冲突策略的防火墙名称")
    private String deviceName;

    @ApiModelProperty("状态枚举")
    private Integer status;

    @ApiModelProperty("错误信息提示")
    private String errorMsg;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("入接口")
    private String inDevIf;

    @ApiModelProperty("出接口")
    private String outDevIf;

    @ApiModelProperty("访问类型（0：内网互访；1：内网访问互联网；2：互联网访问内网） ")
    private Integer accessType;

    @ApiModelProperty("转换后IP地址")
    private String postIpAddress;
}
