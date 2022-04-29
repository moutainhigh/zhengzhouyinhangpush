package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.generate.subservice.CmdService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 该类用于离散查找当前已存在服务对象
 * @Author Wen Jiachang
 */
@Service
public class SearchUnitaryExistServiceCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        if (deviceDTO == null) {
            throw new Exception("设备数据为空。");
        }

        PolicyDTO policyDTO = cmdDTO.getPolicy();
        if (policyDTO == null) {
            throw new Exception("策略数据为空。");
        }

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        if (existObjectDTO == null) {
            existObjectDTO = new ExistObjectDTO();
            cmdDTO.setExistObject(existObjectDTO);
        }

        String serviceName = getObjectName(policyDTO.getServiceList(),policyDTO.getType(), deviceDTO,policyDTO.getIdleTimeout());
        existObjectDTO.setServiceObjectName(serviceName);

        //对转换后服务，做整体复用查询
        if (policyDTO.getPostServiceList() != null && !policyDTO.getPostServiceList().isEmpty()) {
            String postServiceName = getObjectName(policyDTO.getPostServiceList(),policyDTO.getType(), deviceDTO,policyDTO.getIdleTimeout());
            existObjectDTO.setPostServiceObjectName(postServiceName);
        }
    }

    private String getObjectName(List<ServiceDTO> serviceDTOS , PolicyEnum policyType,  DeviceDTO deviceDTO , Integer idleTimeout) {
        DeviceModelNumberEnum modelNumberEnum = deviceDTO.getModelNumber();
        DeviceForExistObjDTO deviceForExistObjDTO =  new DeviceForExistObjDTO();
        deviceForExistObjDTO.setModelNumber(modelNumberEnum);
        deviceForExistObjDTO.setDeviceUuid(deviceDTO.getDeviceUuid());
        deviceForExistObjDTO.setPolicyType(policyType);
        //网神特殊处理，给源端口加到1-65535
        if (DeviceModelNumberEnum.LEGEND_SEC_NSG.getKey().equals(modelNumberEnum.getKey()) || DeviceModelNumberEnum.LEGEND_SEC_GATE.getKey().equals(modelNumberEnum.getKey())) {
            for (ServiceDTO serviceDTO : serviceDTOS) {
                serviceDTO.setSrcPorts("1-65535");
            }
        }
        String serviceName ;
        if(CollectionUtils.isNotEmpty(serviceDTOS)){
            serviceName = whaleManager.getCurrentServiceObjectName(serviceDTOS, deviceForExistObjDTO,idleTimeout);
        }else{
            serviceName = "";
        }

        return serviceName;
    }
}
