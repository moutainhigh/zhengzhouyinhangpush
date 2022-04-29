package com.abtnetworks.totems.translation.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;

/**
 * @Description 策略迁移信息表
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
@ApiModel("策略迁移任务信息Entity")
public class TranslationTaskRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 6931018951680816826L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * UUID
     */
    @ApiModelProperty(value = "UUID")
    private String uuid;

    /**
     * 主题名称
     */
    @ApiModelProperty(value = "主题名称")
    private String titleName;

    /**
     * 设备uuid
     */
    @ApiModelProperty(value = "设备uuid")
    private String deviceUuid;

    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 设备厂商id
     */
    @ApiModelProperty(value = "设备厂商id")
    private String deviceVendorId;

    /**
     * 设备厂商名称
     */
    @ApiModelProperty(value = "设备厂商名称")
    private String deviceVendorName;

    /**
     * 设备厂商型号
     */
    @ApiModelProperty(value = "设备厂商型号")
    private String deviceModelNumber;

    /**
     * 设备uuid
     */
    @ApiModelProperty(value = "目标设备uuid")
    private String targetDeviceUuid;

    /**
     * 目标设备名称
     */
    @ApiModelProperty(value = "目标设备名称")
    private String targetDeviceName;

    /**
     * 目标设备厂商id
     */
    @ApiModelProperty(value = "目标设备厂商id")
    private String targetDeviceVendorId;

    /**
     * 目标设备厂商名称
     */
    @ApiModelProperty(value = "目标设备厂商名称")
    private String targetDeviceVendorName;

    /**
     * 目标设备厂商型号
     */
    @ApiModelProperty(value = "目标设备厂商型号")
    private String targetDeviceModelNumber;

    /**
     * 生成的命令行配置状态（是否有值）
     */
    @ApiModelProperty(value = "生成的命令行配置状态（是否有值）")
    private Object commandLineConfigStatus;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remarks;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "迁移类型 1:已纳管  2:未纳管")
    private Integer migrationType;

    /**
     * 告警信息
     */
    @ApiModelProperty(value = "告警信息")
    private String warning;

    /**
     * 创建用户
     */
    @ApiModelProperty(value = "创建用户")
    private String createUser;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    private List<TranslationTaskMappingEntity> mappingList;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
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

    public String getDeviceVendorId() {
        return deviceVendorId;
    }

    public void setDeviceVendorId(String deviceVendorId) {
        this.deviceVendorId = deviceVendorId;
    }

    public String getDeviceVendorName() {
        return deviceVendorName;
    }

    public void setDeviceVendorName(String deviceVendorName) {
        this.deviceVendorName = deviceVendorName;
    }

    public String getDeviceModelNumber() {
        return deviceModelNumber;
    }

    public void setDeviceModelNumber(String deviceModelNumber) {
        this.deviceModelNumber = deviceModelNumber;
    }

    public String getTargetDeviceUuid() {
        return targetDeviceUuid;
    }

    public void setTargetDeviceUuid(String targetDeviceUuid) {
        this.targetDeviceUuid = targetDeviceUuid;
    }

    public String getTargetDeviceName() {
        return targetDeviceName;
    }

    public void setTargetDeviceName(String targetDeviceName) {
        this.targetDeviceName = targetDeviceName;
    }

    public String getTargetDeviceVendorId() {
        return targetDeviceVendorId;
    }

    public void setTargetDeviceVendorId(String targetDeviceVendorId) {
        this.targetDeviceVendorId = targetDeviceVendorId;
    }

    public String getTargetDeviceVendorName() {
        return targetDeviceVendorName;
    }

    public void setTargetDeviceVendorName(String targetDeviceVendorName) {
        this.targetDeviceVendorName = targetDeviceVendorName;
    }

    public String getTargetDeviceModelNumber() {
        return targetDeviceModelNumber;
    }

    public void setTargetDeviceModelNumber(String targetDeviceModelNumber) {
        this.targetDeviceModelNumber = targetDeviceModelNumber;
    }

    public Object getCommandLineConfigStatus() {
        return commandLineConfigStatus;
    }

    public void setCommandLineConfigStatus(Object commandLineConfigStatus) {
        this.commandLineConfigStatus = commandLineConfigStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<TranslationTaskMappingEntity> getMappingList() {
        return mappingList;
    }

    public void setMappingList(List<TranslationTaskMappingEntity> mappingList) {
        this.mappingList = mappingList;
    }

    public Integer getMigrationType() {
        return migrationType;
    }

    public void setMigrationType(Integer migrationType) {
        this.migrationType = migrationType;
    }
}
