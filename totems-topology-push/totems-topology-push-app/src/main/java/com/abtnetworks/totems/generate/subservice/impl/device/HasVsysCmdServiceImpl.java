package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查询飞塔设备是否包含虚墙的子服务，若为主墙，是否包含虚墙对命令行生成有区别
 */
@Slf4j
@Service
public class HasVsysCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO device = cmdDTO.getDevice();

        //不是虚墙才查，是虚墙则不查
        if(!device.isVsys()) {
            device.setHasVsys(hasVsys(device.getDeviceUuid()));
        } else {
            log.info("设备{}为虚墙，不查是否包含虚墙", device.getDeviceUuid());
        }
    }

    private boolean hasVsys(String deviceUuid) {
        log.info(String.format("查找%s是否有虚墙", deviceUuid));
        if(AliStringUtils.isEmpty(deviceUuid)) {
            return false;
        }

        List<NodeEntity> nodeEntityList = recommendTaskManager.getNodeList();
        for(NodeEntity nodeEntity: nodeEntityList) {
            if(nodeEntity.getModelNumber() != null && nodeEntity.getModelNumber().toUpperCase().contains("FORTINET")){
                String nodeEnetityUuid = nodeEntity.getUuid();
                if(!AliStringUtils.isEmpty(nodeEnetityUuid )) {
                    DeviceRO deviceRO = whaleManager.getDeviceByUuid(nodeEnetityUuid);
                    if(deviceRO != null && deviceRO.getData() != null && deviceRO.getData().size() > 0) {
                        DeviceDataRO deviceData = deviceRO.getData().get(0);
                        if (deviceData.getIsVsys() != null) {
                            String rootDeviceUuid = deviceData.getRootDeviceUuid();
                            log.info(String.format("设备%s的主墙deviceUuid为%s", nodeEnetityUuid, rootDeviceUuid));
                            if(rootDeviceUuid.equals(deviceUuid)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
