package com.abtnetworks.totems.translation.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @Description 策略迁移域和接口映射表
 * @author: WangCan
 * @date: 2021/4/19
 */
@ApiModel("策略迁移域和接口映射Entity")
public class TranslationTaskMappingEntity extends BaseEntity {

    private static final long serialVersionUID = -400179216533194750L;

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
     * 策略迁移任务UUID
     */
    @ApiModelProperty(value = "策略迁移任务UUID")
    private String taskUuid;

    /**
     * 原设备的值
     */
    @ApiModelProperty(value = "原设备的值")
    private String sourceValue;

    @ApiModelProperty(value = "原设备值的类型 1:域 2:接口")
    private String sourceType;

    /**
     * 新设备的值
     */
    @ApiModelProperty(value = "新设备的值")
    private String targetValue;

    @ApiModelProperty(value = "目的设备值的类型 1:域 2:接口")
    private String targetType;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remarks;

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

    public String getTaskUuid() {
        return taskUuid;
    }

    public void setTaskUuid(String taskUuid) {
        this.taskUuid = taskUuid;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
