package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 对checkpoint进行layer和packageName 赋值
 * @date 2020/8/19
 */
@Slf4j
@Service
public class GetCheckPointLayerPackageServiceImpl implements CmdService {
    @Autowired
    AdvancedSettingService advancedSettingService;




    @Autowired
    private WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        SettingDTO settingDTO = cmdDto.getSetting();
        String deviceUuid = cmdDto.getDevice().getDeviceUuid();
        log.info("对checkpoint进行layer和packageName 赋值,deviceUuid:{}", deviceUuid);
        String policyPackage = "";
        String layerName = "";

        DeviceDTO deviceDTO = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT, deviceUuid);
        if (deviceDTO != null) {
            layerName = deviceDTO.getLayerName();
            policyPackage = deviceDTO.getPolicyPackage();
        }
        if (StringUtils.isNotEmpty(layerName)) {
            settingDTO.setLayerName(layerName);
        }else{
            DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
            if(deviceRO!=null){
                if(deviceRO == null || deviceRO.getData() == null ||deviceRO.getData().size() ==0 ) {
                    log.error("设备信息为空，不查询是否为虚设备");
                    settingDTO.setLayerName(CommonConstants.DEFAULT_LAYER_NAME);
                }else{
                    DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
                    settingDTO.setLayerName(deviceDataRO.getAccessLayersName());
                }

            }
        }

        if(StringUtils.isEmpty(policyPackage)){
            List<DeviceFilterlistRO> deviceFilterListROS = whaleManager.getDeviceFilterListRO(deviceUuid);
            if (CollectionUtils.isNotEmpty(deviceFilterListROS)) {
                DeviceFilterlistRO deviceFilterlistRO = deviceFilterListROS.get(0);
                policyPackage = deviceFilterlistRO.getName();
            }
        }
        settingDTO.setPolicyPackage(policyPackage);
        log.info("对checkpoint进行设备 = {} 和layerName = {} 赋值, package = {} 赋值", deviceUuid, settingDTO.getLayerName(),settingDTO.getPolicyPackage());
    }
}
