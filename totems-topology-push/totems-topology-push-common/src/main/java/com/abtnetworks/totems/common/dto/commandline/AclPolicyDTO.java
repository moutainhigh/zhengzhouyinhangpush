package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@ApiModel("Acl策略对象")
public class AclPolicyDTO {

    @ApiModelProperty("命令行下发任务主键id")
    Integer id;

    @ApiModelProperty("任务ID")
    Integer taskId;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("目的地址")
    String dstIp;


    @ApiModelProperty("服务")
    List<ServiceDTO> serviceList;


    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("现有服务对象名称")
    String serviceObjectName;

    @ApiModelProperty("现有源地址对象名称")
    String srcAddressObjectName;

    @ApiModelProperty("现有目的地址对象名称")
    String dstAddressObjectName;

    @ApiModelProperty("转换后地址对象名称")
    String postSrcAddressObjectName;

    @ApiModelProperty("转换后目的地址对象名称")
    String postDstAddressObjectName;

    @ApiModelProperty("转换后服务对象名称")
    String postServiceObjectName;

    @ApiModelProperty("是否为虚墙")
    boolean isVsys;

    @ApiModelProperty("虚墙名称")
    String vsysName;

    @ApiModelProperty("是否创建对象,true标识创建, false不创建，直接引用")
    boolean createObjFlag;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("交换位置的规则名或id")
    String swapRuleNameId;

    @ApiModelProperty("需要新建的服务列表")
    List<ServiceDTO> restServiceList = new ArrayList<>();

    @ApiModelProperty("已存在服务名称列表")
    List<String> existServiceNameList = new ArrayList<>();

    @ApiModelProperty("已存在转换后服务名称列表")
    List<String> existPostServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建转换后的服务列表")
    List<ServiceDTO> restPostServiceList = new ArrayList<>();
}
