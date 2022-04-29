package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.recommend.vo.PolicyRecommendSecurityPolicyVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Data
@ApiModel("策略建议数据")
public class RecommendPolicyEntity {
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
    private String service;

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
    private String mergePolicyName;

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

    @ApiModelProperty("策略类型")
    PolicyEnum policyType;

    @ApiModelProperty("策略生成来源，0为Ip,1为域名或域名解析后的ip地址")
    private String policySource;

    @ApiModelProperty("匹配到的DNAT名称")
    private String dnatName;

    @ApiModelProperty("匹配到的SNAT名称")
    private String snatName;

    @ApiModelProperty("whatIf场景Nat匹配类型 0.未匹配到 1.存量NAT匹配 2.whatIf Nat 匹配 3.存量和whatIf都存在，根据name前缀是否是whatIf_进行区分")
    private String matchType;

    @ApiModelProperty("匹配到的Nat策略的转换前的服务")
    private String matchPreServices;

    @ApiModelProperty("匹配到的Nat策略的转换后的服务")
    private String matchPostServices;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecommendPolicyEntity that = (RecommendPolicyEntity) o;
        return isVsys == that.isVsys &&
                Objects.equals(id, that.id) &&
                Objects.equals(taskId, that.taskId) &&
                Objects.equals(pathInfoId, that.pathInfoId) &&
                Objects.equals(srcIp, that.srcIp) &&
                Objects.equals(dstIp, that.dstIp) &&
                Objects.equals(service, that.service) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(deviceUuid, that.deviceUuid) &&
                Objects.equals(ruleListUuid, that.ruleListUuid) &&
                Objects.equals(srcZone, that.srcZone) &&
                Objects.equals(dstZone, that.dstZone) &&
                Objects.equals(inDevIf, that.inDevIf) &&
                Objects.equals(outDevIf, that.outDevIf) &&
                Objects.equals(createObject, that.createObject) &&
                Objects.equals(createPolicy, that.createPolicy) &&
                Objects.equals(mergePolicyName, that.mergePolicyName) &&
                Objects.equals(movePolicy, that.movePolicy) &&
                Objects.equals(specificPosition, that.specificPosition) &&
                Objects.equals(specifyZone, that.specifyZone) &&
                Objects.equals(aclDirection, that.aclDirection) &&
                Objects.equals(rootDeviceUuid, that.rootDeviceUuid) &&
                Objects.equals(vsysName, that.vsysName) &&
                Objects.equals(idleTimeout, that.idleTimeout);
    }

    @Override
    public int hashCode() {
        int result = 13;

        result = result + (id != null ? id.hashCode() : 0);
        result = result + (taskId != null ? taskId.hashCode() : 0);
        result = result + (pathInfoId != null ? pathInfoId.hashCode() : 0);
        result = result + (srcIp != null ? srcIp.hashCode() : 0);
        result = result + (dstIp != null ? dstIp.hashCode() : 0);
        result = result + (service != null ? service.hashCode() : 0);
        result = result + (startTime != null ? startTime.hashCode() : 0);
        result = result + (endTime != null ? endTime.hashCode() : 0);
        result = result + (deviceUuid != null ? deviceUuid.hashCode() : 0);
        result = result + (ruleListUuid != null ? ruleListUuid.hashCode() : 0);
        result = result + (srcZone != null ? srcZone.hashCode() : 0);
        result = result + (dstZone != null ? dstZone.hashCode() : 0);
        result = result + (inDevIf != null ? inDevIf.hashCode() : 0);
        result = result + (outDevIf != null ? outDevIf.hashCode() : 0);
        result = result + (createObject != null ? createObject.hashCode() : 0);
        result = result + (createPolicy != null ? createPolicy.hashCode() : 0);
        result = result + (mergePolicyName != null ? mergePolicyName.hashCode() : 0);
        result = result + (movePolicy != null ? movePolicy.hashCode() : 0);
        result = result + (specificPosition != null ? specificPosition.hashCode() : 0);
        result = result + (specifyZone != null ? specifyZone.hashCode() : 0);
        result = result + (aclDirection != null ? aclDirection.hashCode() : 0);
        result = result + (isVsys ? 1231 : 1237);
        result = result + (rootDeviceUuid != null ? rootDeviceUuid.hashCode() : 0);
        result = result + (vsysName != null ? vsysName.hashCode() : 0);
        result = result + (idleTimeout != null ? idleTimeout.hashCode() : 0);
        return result;
    }
}