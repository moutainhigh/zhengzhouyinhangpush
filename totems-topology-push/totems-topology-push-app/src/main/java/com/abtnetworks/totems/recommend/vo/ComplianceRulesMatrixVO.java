package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.remote.dto.DredgeServiceDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

/**
 * @Author: Administrator
 * @Date: 2020/5/11
 * @desc: 请写类注释
 */
@ApiModel("api接口返回")
public class ComplianceRulesMatrixVO {

    @ApiModelProperty("横坐标（区域）")
    private String districtNameX;
    @ApiModelProperty("纵坐标（区域）")
    private String districtNameY;
    @ApiModelProperty("源区域id")
    private String srcDistrictId;
    @ApiModelProperty("目的区域id")
    private String dstDistrictId;
    @ApiModelProperty("规则类型")
    private Integer ruleType;
    @ApiModelProperty("规则id")
    private String ruleId;
    @ApiModelProperty("源地址对象名称")
    private String srcAddress;
    @ApiModelProperty("目的地址对象名称")
    private String dstAddress;
    @ApiModelProperty("服务")
    private String services;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("规则级别：1拒绝访问 2 审批后开通 3按需开通")
    private Integer ruleLevel;
    @ApiModelProperty("源检查类型")
    private Integer srcCheckType;

    @ApiModelProperty("目的检查类型")
    private Integer dstCheckType;

    @ApiModelProperty("服务检查类型")
    private Integer serviceCheckType;


    @ApiModelProperty("规则排除地址")
    private String excludeSrcAddress;

    @ApiModelProperty("规则排除地址")
    private String excludeDstAddress;

    @ApiModelProperty("规则排除地址")
    private String excludeService;

    @ApiModelProperty("源子网掩码大小")
    private Integer srcMask;

    @ApiModelProperty("目的子网掩码大小")
    private Integer dstMask;

    @ApiModelProperty("端口数量")
    private Integer portRangeSize;

    @ApiModelProperty("违规的源地址")
    private String illegalSrcIp;

    @ApiModelProperty("违规的目的地址")
    private String illegalDstIp;

    @ApiModelProperty("违规的服务")
    private List<DredgeServiceDTO> illegalServices;

    public String getIllegalSrcIp() {
        return illegalSrcIp;
    }

    public void setIllegalSrcIp(String illegalSrcIp) {
        this.illegalSrcIp = illegalSrcIp;
    }

    public String getIllegalDstIp() {
        return illegalDstIp;
    }

    public void setIllegalDstIp(String illegalDstIp) {
        this.illegalDstIp = illegalDstIp;
    }

    public List<DredgeServiceDTO> getIllegalServices() {
        return illegalServices;
    }

    public void setIllegalServices(List<DredgeServiceDTO> illegalServices) {
        this.illegalServices = illegalServices;
    }

    public Integer getPortRangeSize() {
        return portRangeSize;
    }

    public void setPortRangeSize(Integer portRangeSize) {
        this.portRangeSize = portRangeSize;
    }

    public Integer getSrcMask() {
        return srcMask;
    }

    public void setSrcMask(Integer srcMask) {
        this.srcMask = srcMask;
    }

    public Integer getDstMask() {
        return dstMask;
    }

    public void setDstMask(Integer dstMask) {
        this.dstMask = dstMask;
    }

    public String getExcludeSrcAddress() {
        return excludeSrcAddress;
    }

    public void setExcludeSrcAddress(String excludeSrcAddress) {
        this.excludeSrcAddress = excludeSrcAddress;
    }

    public String getExcludeDstAddress() {
        return excludeDstAddress;
    }

    public void setExcludeDstAddress(String excludeDstAddress) {
        this.excludeDstAddress = excludeDstAddress;
    }

    public String getExcludeService() {
        return excludeService;
    }

    public void setExcludeService(String excludeService) {
        this.excludeService = excludeService;
    }

    public Integer getSrcCheckType() {
        return srcCheckType;
    }

    public void setSrcCheckType(Integer srcCheckType) {
        this.srcCheckType = srcCheckType;
    }

    public Integer getDstCheckType() {
        return dstCheckType;
    }

    public void setDstCheckType(Integer dstCheckType) {
        this.dstCheckType = dstCheckType;
    }

    public Integer getServiceCheckType() {
        return serviceCheckType;
    }

    public void setServiceCheckType(Integer serviceCheckType) {
        this.serviceCheckType = serviceCheckType;
    }

    public String getSrcDistrictId() {
        return srcDistrictId;
    }

    public void setSrcDistrictId(String srcDistrictId) {
        this.srcDistrictId = srcDistrictId;
    }

    public String getDstDistrictId() {
        return dstDistrictId;
    }

    public void setDstDistrictId(String dstDistrictId) {
        this.dstDistrictId = dstDistrictId;
    }

    public Integer getRuleLevel() {
        return ruleLevel;
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public String getDstAddress() {
        return dstAddress;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setRuleLevel(Integer ruleLevel) {
        this.ruleLevel = ruleLevel;
    }

    public String getDistrictNameX() {
        return districtNameX;
    }

    public void setDistrictNameX(String districtNameX) {
        this.districtNameX = districtNameX;
    }

    public String getDistrictNameY() {
        return districtNameY;
    }

    public void setDistrictNameY(String districtNameY) {
        this.districtNameY = districtNameY;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }


    @Override
    public String toString() {
        return super.toString();
    }
}