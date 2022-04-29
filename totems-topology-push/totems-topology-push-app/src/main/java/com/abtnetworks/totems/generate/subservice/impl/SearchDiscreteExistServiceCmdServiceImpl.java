package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 该类用于整体查找当前已存在服务对象
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class SearchDiscreteExistServiceCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        log.info(String.format("高级设置复用现有服务对象，获取离散服务对象......"));
        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        PolicyDTO policyDTO = cmdDTO.getPolicy();

        //转换前
        List<String> existServiceList = new ArrayList<>();
        List<ServiceDTO> restServiceList = new ArrayList<>();
        getExistServiceName(deviceDTO.getDeviceUuid(), policyDTO.getServiceList(), existServiceList, restServiceList, cmdDTO.getPolicy().getIdleTimeout(),deviceDTO.getModelNumber(),policyDTO.getType());
        existObjectDTO.setExistServiceNameList(existServiceList);
        existObjectDTO.setRestServiceList(restServiceList);

        //转换后
        if (policyDTO.getPostServiceList() != null && !policyDTO.getPostServiceList().isEmpty()) {
            List<String> existPostServiceList = new ArrayList<>();
            List<ServiceDTO> restPostServiceList = new ArrayList<>();
            getExistServiceName(deviceDTO.getDeviceUuid(), policyDTO.getPostServiceList(), existPostServiceList, restPostServiceList, cmdDTO.getPolicy().getIdleTimeout(),deviceDTO.getModelNumber(),policyDTO.getType());
            existObjectDTO.setExistPostServiceNameList(existPostServiceList);
            existObjectDTO.setRestPostServiceList(restPostServiceList);
        }

    }

    /**
     * 将已有服务拆分为离散服务，针对每个离散服务查询现有服务，将查询到已有服务对象名称保存到一个列表中，将未查到已有服务对象的服务保存到一个list中后续生成对象
     * @param deviceUuid 设备UUID
     * @param serviceList 服务列表
     * @param existServiceList 已存在对象列表
     * @param restServiceList 剩余为查询到已存在对象服务列表
     */
    private void getExistServiceName(String deviceUuid, List<ServiceDTO> serviceList, List<String> existServiceList, List<ServiceDTO> restServiceList, Integer idleTimeout, DeviceModelNumberEnum modelNumberEnum, PolicyEnum type) {
        existServiceList.clear();
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
        log.info("离散服务列表为：" + JSONObject.toJSONString(seprateServiceList));
        DeviceForExistObjDTO deviceForExistObjDTO =  new DeviceForExistObjDTO();
        deviceForExistObjDTO.setModelNumber(modelNumberEnum);
        deviceForExistObjDTO.setDeviceUuid(deviceUuid);
        deviceForExistObjDTO.setPolicyType(type);
        for(ServiceDTO serviceDTO : seprateServiceList) {
            List<ServiceDTO> array = new ArrayList<>();
            //网神特殊处理，给源端口加到1-65535
            if(DeviceModelNumberEnum.LEGEND_SEC_NSG.getKey().equals(modelNumberEnum.getKey()) || DeviceModelNumberEnum.LEGEND_SEC_GATE.getKey().equals(modelNumberEnum.getKey()) ){
                serviceDTO.setSrcPorts("1-65535");
            }
            array.add(serviceDTO);

            String serviceName = whaleManager.getCurrentServiceObjectName(array, deviceForExistObjDTO,idleTimeout);
            if(AliStringUtils.isEmpty(serviceName)) {
                restServiceList.add(serviceDTO);
            }  else {
                existServiceList.add(serviceName);
            }
        }

        log.info("已查询到服务对象列表为：" + JSONObject.toJSONString(existServiceList));
        log.info("未查询到服务对象列表为：" + JSONObject.toJSON(restServiceList));
    }
}
