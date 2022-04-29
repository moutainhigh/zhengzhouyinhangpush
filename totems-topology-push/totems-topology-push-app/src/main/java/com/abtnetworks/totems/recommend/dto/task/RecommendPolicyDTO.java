package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.dto.AutoRecommendFortinetDnatSpecialDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.recommend.vo.PolicyRecommendSecurityPolicyVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@ApiModel("开通策略对象数据")
public class RecommendPolicyDTO {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务Id")
    private Integer taskId;

    @ApiModelProperty("路径信息id")
    private Integer pathInfoId;

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

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("相关策略集UUID")
    private String ruleListUuid;

    @ApiModelProperty("相关策略集名称")
    private String ruleListName;

    @ApiModelProperty("匹配到的ruleId")
    private String matchRuleId;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("入接口")
    private String inDevIf;

    @ApiModelProperty("出接口")
    private String outDevIf;

    @ApiModelProperty("是否创建对象")
    private Integer createObject;

    @ApiModelProperty("是否新建策略")
    private Integer createPolicy;

    @ApiModelProperty("合并策略名称")
    private PolicyMergeDTO mergeDTO;

    @ApiModelProperty("是否移动策略")
    private Integer movePolicy;

    @ApiModelProperty("指定策略位置")
    private String specificPosition;

    @ApiModelProperty("指定域信息")
    private Integer specifyZone;

    @ApiModelProperty("ACL策略挂载方向")
    private Integer aclDirection;

    @ApiModelProperty("是否为虚设备")
    boolean isVsys;

    @ApiModelProperty("主设备UUID")
    String rootDeviceUuid;

    @ApiModelProperty("虚设备名称")
    String vsysName;

    @ApiModelProperty("设备信息")
    NodeEntity node;

    @ApiModelProperty("长链接超时时间")
    Integer idleTimeout;

    @ApiModelProperty("待合并的字段属性")
    private Integer mergeProperty;

    @ApiModelProperty("编辑策略（相关安全策略）名称")
    private String editPolicyName;

    @ApiModelProperty("合并策略的值")
    private String mergeValue;

    @ApiModelProperty("被合并的策略数据信息")
    private PolicyRecommendSecurityPolicyVO securityPolicy;
    @ApiModelProperty("ip类型")
    private Integer IpType;

    @ApiModelProperty("策略类型")
    PolicyEnum policyType;

    @ApiModelProperty("策略生成来源，0为Ip,1为域名或域名解析后的ip地址")
    private String policySource;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")
    private String outDevItfAlias;

    @ApiModelProperty("目的IP转换后")
    private String postDstIp;

    @ApiModelProperty("转换后端口")
    private String postPorts;

    @ApiModelProperty("转换前协议")
    private String preProtocol;

    @ApiModelProperty("目的IP转换前")
    private String preDstIp;

    @ApiModelProperty("转换前端口")
    private String prePorts;

    @ApiModelProperty("转换后协议")
    private String postProtocol;

    @ApiModelProperty("源IP转换前")
    private String preSrcIp;

    @ApiModelProperty("源IP转换后")
    private String postSrcIp;

    @ApiModelProperty("是否存在VIP名称")
    private boolean existVipName;

    @ApiModelProperty("源地址对象名称")
    private String srcAddressObjectName;

    @ApiModelProperty("目的地址对象名称")
    private String dstAddressObjectName;

    @ApiModelProperty("vip名称")
    private String vipName;

    @ApiModelProperty("算路匹配到的DNAT名称")
    private String dnatName;

    @ApiModelProperty("算路匹配到的SNAT名称")
    private String snatName;

    @ApiModelProperty("whatIf场景Nat匹配类型 0.未匹配到 1.存量NAT匹配 2.whatIf Nat 匹配 3.存量和whatIf都存在，根据name前缀是否是whatIf_进行区分")
    private String matchType;

    @ApiModelProperty("匹配到的Nat策略的转换前的服务")
    private List<ServiceDTO> matchPreServices;

    @ApiModelProperty("匹配到的Nat策略的转换后的服务")
    private List<ServiceDTO> matchPostServices;

    @ApiModelProperty("自动开通生成飞塔目的NAT处理")
    AutoRecommendFortinetDnatSpecialDTO fortinetDnatSpecialDTO;


}
