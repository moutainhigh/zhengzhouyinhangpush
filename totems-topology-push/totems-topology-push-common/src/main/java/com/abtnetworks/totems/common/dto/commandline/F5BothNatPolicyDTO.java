package com.abtnetworks.totems.common.dto.commandline;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @desc F5 Both Nat数据实体
 * @date 2021/8/3 14:36
 */
@Data
@ApiModel("F5 Both Nat数据实体")
public class F5BothNatPolicyDTO {

    @ApiModelProperty("命令行下发任务主键id")
    Integer id;

    @ApiModelProperty("任务ID")
    Integer taskId;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("服务")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("策略描述")
    String description;

    @ApiModelProperty("snat类型 None不转换 snat的信息；Auto Map:转换为出接口 没有snat的信息； SNAT：转换为snat模式 有snatpool信息")
    private String snatType;

    @ApiModelProperty("snatPool信息")
    private PushSnatPoolInfo snatPoolInfo;

    @ApiModelProperty("pool信息")
    private PushPoolInfo poolInfo;

    @ApiModelProperty("场景名称")
    private String sceneName;

    @ApiModelProperty("应用发布类型")
    private String applyType;

    @ApiModelProperty("节点负载模式")
    private String loadBlanaceMode;

    @ApiModelProperty("节点回话保持")
    private String persist;

    @ApiModelProperty("健康检查")
    private String monitor;

    @ApiModelProperty("http_profile")
    private String httpProfile;

    @ApiModelProperty("证书名称")
    private String sslProfile;

    @ApiModelProperty("已存在源地址对象列表")
    List<String> existPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("当前已经存在的对象名称")
    private String postSrcAddressObjectName;

    @ApiModelProperty("是否为虚墙")
    boolean isVsys;

    @ApiModelProperty("虚墙名称")
    String vsysName;
}
