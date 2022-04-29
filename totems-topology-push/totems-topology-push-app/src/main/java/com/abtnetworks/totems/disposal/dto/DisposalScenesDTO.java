package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.BaseDto;

import java.util.Date;
import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 17:14 2019/11/14
 */
public class DisposalScenesDTO extends BaseDto {

    private static final long serialVersionUID = -6049354830694402699L;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 场景表的UUID
     */
    private String scenesUuid;

    /**
     * 设备UUID
     */
    private String deviceUuid;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备IP
     * （sql关联查询获取）
     */
    private String deviceIp;

    /**
     * 厂商英文名称
     * （sql关联查询获取）
     */
    private String vendorId;

    /**
     * 源域
     */
    private String srcZoneName;

    /**
     * 源域uuid
     */
    private String srcZoneUuid;

    /**
     * 源接口
     */
    private String srcItf;

    /**
     * 源接口别名
     */
    private String srcItfAlias;

    /**
     * 目的域
     */
    private String dstZoneName;

    /**
     * 目的域uuid
     */
    private String dstZoneUuid;

    /**
     * 目的接口
     */
    private String dstItf;

    /**
     * 目的接口别名
     */
    private String dstItfAlias;

    /**
     * 节点类型：0防火墙，1路由交换机，2负载均衡，3模拟网关*
     * （sql关联查询获取）
     */
    private String type;

    /**
     * node表 条件查询
     * 节点类型：0防火墙，1路由交换机，2负载均衡，3模拟网关
     */
    private List<String> queryTypeList;

    public DisposalScenesDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getScenesUuid() {
        return scenesUuid;
    }

    public void setScenesUuid(String scenesUuid) {
        this.scenesUuid = scenesUuid;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getSrcZoneName() {
        return srcZoneName;
    }

    public void setSrcZoneName(String srcZoneName) {
        this.srcZoneName = srcZoneName;
    }

    public String getSrcZoneUuid() {
        return srcZoneUuid;
    }

    public void setSrcZoneUuid(String srcZoneUuid) {
        this.srcZoneUuid = srcZoneUuid;
    }

    public String getDstZoneName() {
        return dstZoneName;
    }

    public void setDstZoneName(String dstZoneName) {
        this.dstZoneName = dstZoneName;
    }

    public String getDstZoneUuid() {
        return dstZoneUuid;
    }

    public void setDstZoneUuid(String dstZoneUuid) {
        this.dstZoneUuid = dstZoneUuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getQueryTypeList() {
        return queryTypeList;
    }

    public void setQueryTypeList(List<String> queryTypeList) {
        this.queryTypeList = queryTypeList;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getSrcItf() {
        return srcItf;
    }

    public void setSrcItf(String srcItf) {
        this.srcItf = srcItf;
    }

    public String getSrcItfAlias() {
        return srcItfAlias;
    }

    public void setSrcItfAlias(String srcItfAlias) {
        this.srcItfAlias = srcItfAlias;
    }

    public String getDstItf() {
        return dstItf;
    }

    public void setDstItf(String dstItf) {
        this.dstItf = dstItf;
    }

    public String getDstItfAlias() {
        return dstItfAlias;
    }

    public void setDstItfAlias(String dstItfAlias) {
        this.dstItfAlias = dstItfAlias;
    }
}
