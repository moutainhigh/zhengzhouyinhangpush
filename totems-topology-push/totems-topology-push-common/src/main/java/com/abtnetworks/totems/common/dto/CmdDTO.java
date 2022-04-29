package com.abtnetworks.totems.common.dto;


import com.abtnetworks.totems.common.dto.manager.DenyPolicyInfoDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("命令行生成数据对象")
public class CmdDTO {

    @ApiModelProperty("任务相关数据")
    TaskDTO task = new TaskDTO();

    @ApiModelProperty("设备信息")
    DeviceDTO device = new DeviceDTO();

    @ApiModelProperty("处理流程数据")
    ProcedureDTO procedure = new ProcedureDTO();

    @ApiModelProperty("策略数据")
    PolicyDTO policy = new PolicyDTO();

    @ApiModelProperty("已存在对象数据")
    ExistObjectDTO existObject = new ExistObjectDTO();

    @ApiModelProperty("特殊：已存在对象数据，当前仅思科8.6以上版本使用")
    ExistObjectRefDTO specialExistObject = new ExistObjectRefDTO();

    @ApiModelProperty("高级设置")
    SettingDTO setting = new SettingDTO();

    @ApiModelProperty("生成数据")
    GeneratedObjectDTO generatedObject = new GeneratedObjectDTO();

    @ApiModelProperty("命令行业务数据信息返回参数")
    CommandLineBusinessInfoDTO businessInfoDTO = new CommandLineBusinessInfoDTO();
    @ApiModelProperty("定位deny的策略")
    DenyPolicyInfoDTO policyIdByFirstDeny;

    @ApiModelProperty("负载均衡设备信息命令行所需数据")
    CommandLineBalanceInfoDTO commandLineBalanceInfoDTO = new CommandLineBalanceInfoDTO();

    @ApiModelProperty("静态路由信息命令行所需数据")
    CommandLineStaticRoutingInfoDTO commandLineStaticRoutingInfoDTO = new CommandLineStaticRoutingInfoDTO();

    @ApiModelProperty("自动开通适配地址对象特殊处理")
    AutoRecommendSpecialDTO autoRecommendSpecialDTO;

}
