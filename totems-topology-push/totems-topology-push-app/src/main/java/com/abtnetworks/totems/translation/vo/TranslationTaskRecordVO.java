package com.abtnetworks.totems.translation.vo;

import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.enums.CommandLineTranslationStatus;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/1/12 11:25'.
 */
@ApiModel("策略迁移信息 展示对象")
public class TranslationTaskRecordVO extends TranslationTaskRecordEntity {

    private boolean deviceIsDelete;

    private boolean targetDeviceIsDelete;

    @Override
    public String getStatus() {
        if (StringUtils.isNotBlank(super.getStatus())) {
            return CommandLineTranslationStatus.getStatusByCode(super.getStatus()).getName();
        } else {
            return CommandLineTranslationStatus.NOT_STARTED.getName();
        }
    }
    /**
     * 节点类型：0防火墙，1路由交换机，2负载均衡，3模拟网关
     **/
    private String deviceType;

    private String targetDeviceType;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getTargetDeviceType() {
        return targetDeviceType;
    }

    public void setTargetDeviceType(String targetDeviceType) {
        this.targetDeviceType = targetDeviceType;
    }

    public boolean isDeviceIsDelete() {
        return deviceIsDelete;
    }

    public void setDeviceIsDelete(boolean deviceIsDelete) {
        this.deviceIsDelete = deviceIsDelete;
    }

    public boolean isTargetDeviceIsDelete() {
        return targetDeviceIsDelete;
    }

    public void setTargetDeviceIsDelete(boolean targetDeviceIsDelete) {
        this.targetDeviceIsDelete = targetDeviceIsDelete;
    }
}
