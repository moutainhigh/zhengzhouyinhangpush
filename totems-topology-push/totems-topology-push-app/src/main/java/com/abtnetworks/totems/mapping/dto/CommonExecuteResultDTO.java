package com.abtnetworks.totems.mapping.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    规则流程步骤返回值DTO
 *          注意：本类中的结果返回值只针对CommonGenerator抽象类中不同操作步骤的返回值，每一个方法请设置各自对应的子DTO，共用数据请抽离到本类中
 * @author liuchanghao
 * @date 2022-01-20 19:55
 */
@Data
public class CommonExecuteResultDTO {

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("下一个可用IP")
    private String nextAvailableIp;

    @ApiModelProperty("nat策略的主题工单号")
    private String natTheme;

    @ApiModelProperty("nat策略的id")
    private Integer natId;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换前目的地址")
    private String preDstIp;

    @ApiModelProperty("设备IP")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("设备型号")
    private String modelNumber;

}
