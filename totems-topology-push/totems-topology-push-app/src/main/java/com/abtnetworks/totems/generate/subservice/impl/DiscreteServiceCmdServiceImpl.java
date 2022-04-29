package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 拆分服务为离散服务对象
 */
@Slf4j
@Service
public class DiscreteServiceCmdServiceImpl implements CmdService {

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        log.info(String.format("高级设置不复用现有服务对象，获取离散服务对象......"));
        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        List<ServiceDTO> restServiceList = new ArrayList<>();
        descreteService(policyDTO.getServiceList(), restServiceList);
        existObjectDTO.setRestServiceList(restServiceList);
    }

    /**
     * 将已有服务拆分为离散服务
     * @param serviceList 服务列表
     * @param restServiceList 剩余为查询到已存在对象服务列表
     */
    private void descreteService(List<ServiceDTO> serviceList, List<ServiceDTO> restServiceList) {
        restServiceList.clear();
        log.info("源服务列表为" + JSONObject.toJSONString(serviceList, true));
        List<ServiceDTO> seprateServiceList = new ArrayList<>();
        for(ServiceDTO serviceDTO : serviceList) {
            if(serviceDTO.getDstPorts().equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                seprateServiceList.add(serviceDTO);
            } else {
                String dstPorts = serviceDTO.getDstPorts();
                String[] dstPortList = dstPorts.split(",");
                for(String dstPort:dstPortList) {
                    ServiceDTO newServiceDTO = new ServiceDTO();
                    newServiceDTO.setDstPorts(dstPort);
                    newServiceDTO.setProtocol(serviceDTO.getProtocol());
                    newServiceDTO.setSrcPorts(serviceDTO.getSrcPorts());
                    seprateServiceList.add(newServiceDTO);
                }
            }
        }

        restServiceList.addAll(seprateServiceList);
        log.info("离散服务列表为：" + JSONObject.toJSONString(seprateServiceList));
    }
}
