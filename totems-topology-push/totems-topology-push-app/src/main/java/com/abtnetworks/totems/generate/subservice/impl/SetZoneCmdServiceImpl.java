package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.generate.subservice.CmdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 该类用于获取设置域相关参数
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class SetZoneCmdServiceImpl implements CmdService {

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();
        PolicyDTO policyDTO = cmdDTO.getPolicy();

        NodeEntity nodeEntity = deviceDTO.getNodeEntity();

        if(advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE, deviceUuid)) {
            log.info(String.format("设备%s(%s)不指定域信息...命令行生成不指定域", nodeEntity.getDeviceName(), nodeEntity.getIp()));
            //源域目的域均设置为空
            policyDTO.setSrcZone(null);
            policyDTO.setDstZone(null);
        } else if(advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE, deviceUuid)) {
            log.info(String.format("设备%s(%s)指定源域信息...命令行生成指定源域", nodeEntity.getDeviceName(), nodeEntity.getIp()));
            //目的域设置为空
            policyDTO.setDstZone(null);
        } else if(advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE, deviceUuid)) {
            log.info(String.format("设备%s(%s)指定目的域信息...命令行生成指定目的域", nodeEntity.getDeviceName(), nodeEntity.getIp()));
            //源域设置为空
            policyDTO.setSrcZone(null);
        } else {
            log.info(String.format("设备%s(%s)使用默认方式设置域...命令行生成指定源域和目的域", nodeEntity.getDeviceName(), nodeEntity.getIp()));
        }
    }
}
