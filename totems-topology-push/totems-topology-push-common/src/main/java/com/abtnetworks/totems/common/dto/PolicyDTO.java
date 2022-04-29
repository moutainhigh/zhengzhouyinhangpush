package com.abtnetworks.totems.common.dto;


import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@ApiModel("策略对象")
public class PolicyDTO {
    @ApiModelProperty("策略类型")
    PolicyEnum type;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("转换后源地址")
    String postSrcIp;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("转换后目的地址")
    String postDstIp;

    @ApiModelProperty("服务列表")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("转换后服务列表")
    List<ServiceDTO> postServiceList;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("入接口别名")
    String srcItfAlias;

    @ApiModelProperty("出接口别名")
    String dstItfAlias;

    @ApiModelProperty("开始时间")
    String startTime;

    @ApiModelProperty("结束时间")
    String endTime;

    @ApiModelProperty("描述")
    String description;

    @ApiModelProperty("行为")
    ActionEnum action;

    @ApiModelProperty("长连接")
    Integer idleTimeout;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("选择域名策略的ip类型用于生成命令行  0：ipv4; 1:ipv6; ")
    private Integer urlType;

    @ApiModelProperty("默认static，勾选后为dynamic")
    boolean dynamic = false;

    @ApiModelProperty("待合并的字段属性")
    private Integer mergeProperty;

    @ApiModelProperty("编辑策略（相关安全策略）名称")
    private String editPolicyName;

    @ApiModelProperty("合并策略的值")
    private String mergeValue;

    @ApiModelProperty("被合并的策略的数据信息")
    private PolicyRecommendSecurityPolicyDTO securityPolicy;

    @ApiModelProperty("策略生成来源，0为Ip,1为域名或域名解析后的ip地址")
    private String policySource;

    @ApiModelProperty("策略用户")
    private List<String> policyUserNames;
    @ApiModelProperty("策略应用")
    private List<String> policyApplications;

    @ApiModelProperty("转换前端口")
    private String prePort;

    @ApiModelProperty("转换后端口")
    private String postPort;

    @ApiModelProperty("原始源ip。解释：针对于F5策略生成 页面上填的源地址 不去创建对象也不复用")
    private String originalSrcIp;

    @ApiModelProperty("原始目的ip。 解释：针对于F5策略生成 页面上填的目的地址 不去创建对象也不复用")
    private String originalDstIp;

    @ApiModelProperty("复用查到的已经存在的虚拟ip名称  仅飞塔用到")
    String existVirtualIpName;

    @ApiModelProperty("是否存在dnat转换")
    Boolean existDnat = false;

    @ApiModelProperty("自动开通生成飞塔目的NAT处理")
    AutoRecommendFortinetDnatSpecialDTO fortinetDnatSpecialDTO;

    @ApiModelProperty("特殊：飞塔NAT场景仿真高级引用添加")
    SpecialNatDTO specialNatDTO = new SpecialNatDTO();

    @ApiModelProperty("复用查到的已经存在的acl策略名称  仅思科ASA用到")
    String existAclName;

    @ApiModelProperty("复用查到的已经存在的global 仅思科ASA用到")
    boolean existGlobal = false;
}
