package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class NatDTO extends BasicNatInfo {

    @ApiModelProperty("NAT类型")
    PolicyEnum type;

    @ApiModelProperty("命令行下发任务主键id")
    Integer id;

    @ApiModelProperty("任务ID")
    Integer taskId;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    /****************************对象服用相关数据******************************/
    @ApiModelProperty("现有源地址对象（组）名称")
    String srcAddressObjectName;

    @ApiModelProperty("转换后地址对象（组）名称")
    String postSrcAddressObjectName;

    @ApiModelProperty("现有目的地址对象（组）名称")
    String dstAddressObjectName;

    @ApiModelProperty("转换后目的地址对象（组）名称")
    String postDstAddressObjectName;

    @ApiModelProperty("现有服务对象（组）名称")
    String serviceObjectName;

    @ApiModelProperty("转换后服务对象（组）名称")
    String postServiceObjectName;

    @ApiModelProperty("已存在源地址对象列表")
    List<String> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建源地址对象列表")
    List<String> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在")
    List<String> existPostSrcAddressList = new ArrayList<>();

    List<String> restPostAddressList = new ArrayList<>();

    List<String> existDstAddressList = new ArrayList<>();

    List<String> restDstAddressList = new ArrayList<>();

    List<String> existPostDstAddressList = new ArrayList<>();

    List<String> restPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("已存在服务名称列表")
    List<String> existServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建的服务列表")
    List<ServiceDTO> restServiceList = new ArrayList<>();


    @ApiModelProperty("是否创建对象,true标识创建, false不创建，直接引用")
    boolean createObjFlag;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("交换位置的规则名或id")
    String swapRuleNameId;
}
