package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.BaseDto;

import java.util.Date;
import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 17:56 2019/11/12
 */
public class DisposalOrderDTO extends BaseDto {

    private static final long serialVersionUID = -815586422687827332L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * 工单内容UUID
     */
    private String centerUuid;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 错误异常信息
     */
    private String errorMessage;

    /**
     * 创建人员
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 工单名称，用户自定义
     */
    private String orderName;

    /**
     * 工单号，系统生成
     */
    private String orderNo;

    /**
     * 分类：策略，路由
     */
    private Integer category;

    /**
     * 工单类型：1手动、2黑IP、3路径
     */
    private Integer type;

    /**
     * 派发审核类型：0手动审核、1自动
     */
    private Integer sendType;

    /**
     * 源IP
     */
    private String srcIp;

    /**
     * 目的ip
     */
    private String dstIp;

    /**
     * 服务组
     */
    private String serviceList;

    /**
     * 黑洞路由IP
     */
    private String routingIp;

    /**
     * 动作
     */
    private String action;

    /**
     * 需求来源描述
     */
    private String origin;

    /**
     * 原因
     */
    private String reason;

    /**
     * 来源分类，是否上级派发
     */
    private Integer sourceClassification;

    /**
     * 回滚表的 `p_center_uuid` AS callbackCenterUuid
     */
    private String callbackCenterUuid;

    /**
     * 场景uuid 集合（页面JSON传参接收）
     */
    private String[] scenesUuidArray;

    /**
     * 场景uuids 集合（后台sql返回接收）
     */
    private String scenesUuids;

    /**
     * 场景names 名称集合（后台sql返回接收）
     */
    private String scenesNames;

    /**
     * 下级单位uuids 集合（后台sql返回接收）
     */
    private String branchUuids;

    /**
     * 下级单位名称names 名称集合（后台sql返回接收）
     */
    private String branchNames;

    /**
     * 派发下级单位单位，返回的处理状态
     */
    private String branchNamesHandleStatus;

    /**
     * 命令行下发详情，分隔符：@@@ 示例如下：
     * 武汉出口2(192.168.215.18)：失败@@@武汉出口1(192.168.215.17)：失败@@@外联边界防火墙_cisco(1.1.1.10)：失败
     */
    private String implDetails;

    /**
     * 是否回滚标记
     */
    private Boolean callbackFlag;

    /**
     * 是否 ipv6
     */
    private boolean ipv6;

    /**
     * 厂商英文名称
     * （sql关联查询获取）
     */
    private String vendorId;

    public DisposalOrderDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCenterUuid() {
        return centerUuid;
    }

    public void setCenterUuid(String centerUuid) {
        this.centerUuid = centerUuid;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSendType() {
        return sendType;
    }

    public void setSendType(Integer sendType) {
        this.sendType = sendType;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public String getServiceList() {
        return serviceList;
    }

    public void setServiceList(String serviceList) {
        this.serviceList = serviceList;
    }

    public String getRoutingIp() {
        return routingIp;
    }

    public void setRoutingIp(String routingIp) {
        this.routingIp = routingIp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getSourceClassification() {
        return sourceClassification;
    }

    public void setSourceClassification(Integer sourceClassification) {
        this.sourceClassification = sourceClassification;
    }

    public String getCallbackCenterUuid() {
        return callbackCenterUuid;
    }

    public void setCallbackCenterUuid(String callbackCenterUuid) {
        this.callbackCenterUuid = callbackCenterUuid;
    }

    public String[] getScenesUuidArray() {
        return scenesUuidArray;
    }

    public void setScenesUuidArray(String[] scenesUuidArray) {
        this.scenesUuidArray = scenesUuidArray;
    }

    public String getScenesUuids() {
        return scenesUuids;
    }

    public void setScenesUuids(String scenesUuids) {
        this.scenesUuids = scenesUuids;
    }

    public String getScenesNames() {
        return scenesNames;
    }

    public void setScenesNames(String scenesNames) {
        this.scenesNames = scenesNames;
    }

    public String getBranchUuids() {
        return branchUuids;
    }

    public void setBranchUuids(String branchUuids) {
        this.branchUuids = branchUuids;
    }

    public String getBranchNames() {
        return branchNames;
    }

    public void setBranchNames(String branchNames) {
        this.branchNames = branchNames;
    }

    public String getBranchNamesHandleStatus() {
        return branchNamesHandleStatus;
    }

    public void setBranchNamesHandleStatus(String branchNamesHandleStatus) {
        this.branchNamesHandleStatus = branchNamesHandleStatus;
    }

    public String getImplDetails() {
        return implDetails;
    }

    public void setImplDetails(String implDetails) {
        this.implDetails = implDetails;
    }

    public Boolean getCallbackFlag() {
        return callbackFlag;
    }

    public void setCallbackFlag(Boolean callbackFlag) {
        this.callbackFlag = callbackFlag;
    }

    public boolean getIpv6() {
        return ipv6;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }
}
