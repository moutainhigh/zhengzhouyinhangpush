package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 该类用于获取高级配置相关选项（这里的高级设置需要在所有步骤前面）
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetSettingCmdServiceImpl implements CmdService {

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        SettingDTO settingDTO = cmdDTO.getSetting();

        String isCreateObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT);
        //全局设置为不创建对象，则设置为不创建对象
        if(isCreateObject.equals(AdvancedSettingsConstants.IS_REFERENCE_CONTENT_VALUE)) {
            settingDTO.setCreateObject(false);
        } else {
            settingDTO.setCreateObject(true);
        }

        String useCurrentAddressObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT);
        if(useCurrentAddressObject.equals(AdvancedSettingsConstants.IS_USE_CURRENT)) {
            settingDTO.setEnableAddressObjectSearch(true);
        }

        String useCurrentServiceObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_OBJECT);
        if(useCurrentServiceObject.equals(AdvancedSettingsConstants.IS_USE_CURRENT)) {
            settingDTO.setEnableServiceObjectSearch(true);
        }

        String addressType = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_GLOBLE_OR_SECURITY);
        if(StringUtils.isEmpty(addressType) || AdvancedSettingsConstants.IS_SECURITY.equals(addressType)) {
            settingDTO.setAddressType(true);
        }else {
            settingDTO.setAddressType(false);
        }

        log.info("cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
    }
}
