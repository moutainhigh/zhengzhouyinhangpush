package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/8/11
 */
@Slf4j
@Service
public class GetTopsecGroupNameServiceImpl implements CmdService {


    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        SettingDTO settingDTO = cmdDto.getSetting();

        String deviceUuid = cmdDto.getDevice().getDeviceUuid();
        log.info("天融信高级设置，根据设备查询分组名称,deviceUuid:{}", deviceUuid);
        DeviceDTO groupDevice = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME, deviceUuid);
        if (groupDevice != null) {
            if (groupDevice.getGroupName() != null) {
                settingDTO.setGroupName(groupDevice.getGroupName());
            } else {
                settingDTO.setGroupName("");
            }
            log.info("天融信，查询分组名称, deviceUuid:{},name:{}", deviceUuid, settingDTO.getGroupName());
        }
    }
}
