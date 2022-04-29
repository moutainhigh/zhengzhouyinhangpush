package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;

/**
 * @Author hw
 * @Description
 * @Date 11:13 2019/11/14
 */
public class DisposalScenesNodeEntity extends BaseEntity {

    private static final long serialVersionUID = -1283698783355954973L;

    /**
     * 主键id，自增
     */
    private Long id;

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
