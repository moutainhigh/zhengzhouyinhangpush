package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.NetWorkGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 该类用于获取h3V7设备接口上现有地址池中当前生成地址所使用的ID。
 *              当前地址池所使用的ID为当前地址池上最大的ID和数据库中存储的ID较大的那个+1，以避免多次生成策略时造成的ID重复
 *              生成地址池才去设置值
 * @Author zhoumuhua
 */
@Slf4j
@Service
public class GetH3cAddressGroupIdServiceImpl implements CmdService {

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();
        ExistObjectDTO existObject = cmdDTO.getExistObject();

        PolicyDTO policy = cmdDTO.getPolicy();
        if (StringUtils.isNotBlank(policy.getPostSrcIp()) && StringUtils.isBlank(existObject.getPostSrcAddressObjectName())) {
            //转换后源地址不为空才生成地址池对象
            SettingDTO settingDTO = cmdDTO.getSetting();

            int maxId = getCurrentAddressGroupId(deviceUuid);
            synchronized (GetH3cAddressGroupIdServiceImpl.class) {
                int currentId = advancedSettingService.getH3cAddressGroupId(deviceUuid);

                if (maxId < currentId) {
                    maxId = currentId;
                }
                int newMaxId = maxId + 1;
                settingDTO.setH3v7addressGroupId(String.valueOf(newMaxId));

                advancedSettingService.setH3cAddressGroupId(deviceUuid, newMaxId);

            }
        }
    }

    Integer getCurrentAddressGroupId(String deviceUuid) {
        DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();
        searchDTO.setDeviceUuid(deviceUuid);
        ResultRO<List<NetWorkGroupObjectRO>> resultRO = whaleDeviceObjectClient.getNetWorkGroupObject(searchDTO);
        log.debug(String.format("设备(%s)相关地址池数据为：\n-----------------------------------\n", deviceUuid)
                + JSONObject.toJSONString(resultRO)  + "\n-----------------------------------");
        if(resultRO == null) {
            return 0;
        }
        List<NetWorkGroupObjectRO> list = resultRO.getData();
        Integer id = 0;
        String name = null;
        if(list != null && list.size() > 0) {
            for(NetWorkGroupObjectRO ruleListRO : list) {
                try {
                    Integer addressId = Integer.valueOf(ruleListRO.getRealName());
                    if(addressId > id) {
                        id = addressId;
                    }
                } catch (Exception e) {
                    log.debug("解析地址池id出错。。。", e);
                }
            }
        }

        return id;
    }
}
