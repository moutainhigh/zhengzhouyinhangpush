package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.common.constants.GenerateConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.enums.SearchRangeOpEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.whale.baseapi.dto.SearchAddressDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchServiceDTO;
import com.abtnetworks.totems.whale.baseapi.dto.ServiceConditionDTO;
import com.abtnetworks.totems.whale.baseapi.ro.NetWorkGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.ro.ServiceGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.common.CommonRangeIntegerDTO;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.abtnetworks.totems.common.constants.GenerateConstants.PORT_SERVICE_TYPE;

/**
 * @author luwei
 * @date 2020/7/21
 */
@Slf4j
@Service
public class GetCiscoSpecialObjectRefServiceImpl implements CmdService {

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        log.info("思科8.6版本，对象引用，特殊处理");
        DeviceDTO deviceDTO = cmdDto.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();

        PolicyDTO policyDTO = cmdDto.getPolicy();

        ExistObjectRefDTO specialObject = cmdDto.getSpecialExistObject();

        SettingDTO settingDTO = cmdDto.getSetting();
        if (settingDTO.isEnableAddressObjectSearch()) {
            specialObject.setSrcAddressObjectName(getCurrentAddressObjectName(deviceDTO.getDeviceUuid(), policyDTO.getSrcIp()));
            specialObject.setDstAddressObjectName(getCurrentAddressObjectName(deviceDTO.getDeviceUuid(), policyDTO.getDstIp()));
            specialObject.setPostSrcAddressObjectName(getCurrentAddressObjectName(deviceDTO.getDeviceUuid(), policyDTO.getPostSrcIp()));
            specialObject.setPostDstAddressObjectName(getCurrentAddressObjectName(deviceDTO.getDeviceUuid(), policyDTO.getPostDstIp()));

            specialObject.setServiceObjectName(getCurrentServiceObjectName(policyDTO.getServiceList(), deviceUuid, null));
            if (policyDTO.getPostServiceList() != null && !policyDTO.getPostServiceList().isEmpty()) {
                specialObject.setPostServiceObjectName(getCurrentServiceObjectName(policyDTO.getPostServiceList(), deviceUuid, null));
            }

        } else {
            log.info("不进行整体地址对象/对象组查询...");
        }



    }

    //查询当前使用的地址对象
    private RefObjectDTO getCurrentAddressObjectName(String deviceUuid, String ipAddress) {
        if(AliStringUtils.isEmpty(ipAddress)) {
            log.info("查询现有地址对象，地址为空，不进行查询...");
            return null;
        }
        log.info(String.format("设备%s，地址为%s", deviceUuid, ipAddress));
        RefObjectDTO refObjectDTO = null;

        String[] ipAddresses = ipAddress.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<CommonRangeStringDTO> addressList = new ArrayList<>();
        boolean isIPv6 = ipAddress.contains(":");

        for(String ip: ipAddresses) {
            CommonRangeStringDTO dto = new CommonRangeStringDTO();
            String start = null;
            String end = null;
            try {
                String[] startEndIpStringArray = QuintupleUtils.ipv46toIpStartEnd(ip);
                start = startEndIpStringArray[0];
                end = startEndIpStringArray[1];
            } catch (UnknownHostException e) {
                log.error(String.format("获取IP地址起始地址异常！[{}]", ip), e);
            }
            dto.setStart(start);
            dto.setEnd(end);
            addressList.add(dto);
        }
        SearchAddressDTO addressDTO = new SearchAddressDTO();

        addressDTO.setDeviceUuid(deviceUuid);
        if (isIPv6) {
            addressDTO.setSearchType("IP6");
            addressDTO.setIp6RangeOp(SearchRangeOpEnum.EQUAL.getCode());
            addressDTO.setIp6AddressRanges(addressList);
        } else {
            addressDTO.setSearchType("IP");
            addressDTO.setRangeOp(SearchRangeOpEnum.EQUAL.getCode());
            addressDTO.setAddressRanges(addressList);
        }
        ResultRO<List<NetWorkGroupObjectRO>> resultRO = whaleDeviceObjectClient.searchAddress(addressDTO, null);
        if (resultRO == null || !resultRO.getSuccess() || resultRO.getData() == null || resultRO.getData().isEmpty()) {
            return refObjectDTO;
        }

        NetWorkGroupObjectRO groupObject = resultRO.getData().get(0);
        refObjectDTO = new RefObjectDTO();
        refObjectDTO.setRefName(groupObject.getName());
        refObjectDTO.setObjectTypeEnum(DeviceObjectTypeEnum.getDeviceObjectTypeByCode(groupObject.getDeviceObjectType()));

        return refObjectDTO;
    }

    //查询当前使用的服务对象
    public RefObjectDTO getCurrentServiceObjectName(List<ServiceDTO> serviceList, String deviceUuid, Integer idleTimeout){
        SearchServiceDTO serviceDTO = new SearchServiceDTO();
        List<ServiceConditionDTO> list = new ArrayList<>();
        for(ServiceDTO service: serviceList) {
            ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
            String dstPorts = service.getDstPorts();
            if(!AliStringUtils.isEmpty(dstPorts) && !dstPorts.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String[] dstPortArray = dstPorts.split(PolicyConstants.ADDRESS_SEPERATOR);
                List<CommonRangeIntegerDTO> dstPortRangeList = new ArrayList<>();
                for (String dstPort : dstPortArray) {
                    CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
                    if (PortUtils.isPortRange(dstPort)) {
                        String start = PortUtils.getStartPort(dstPort);
                        String end = PortUtils.getEndPort(dstPort);
                        rangeIntegerDTO.setStart(Integer.valueOf(start));
                        rangeIntegerDTO.setEnd(Integer.valueOf(end));
                    } else {
                        rangeIntegerDTO.setStart(Integer.valueOf(dstPort));
                        rangeIntegerDTO.setEnd(Integer.valueOf(dstPort));
                    }
                    dstPortRangeList.add(rangeIntegerDTO);
                }
                serviceConditionDTO.setDstPorts(dstPortRangeList);
            }

            String protocol = service.getProtocol();
            String[] protocolArray = protocol.split(PolicyConstants.ADDRESS_SEPERATOR);
            List<CommonRangeIntegerDTO> protocolRangeList = new ArrayList<>();
            for(String protocolString: protocolArray) {
                if(AliStringUtils.isEmpty(protocolString) || protocolString.equalsIgnoreCase("any")) {
                    CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
                    rangeIntegerDTO.setStart(0);
                    rangeIntegerDTO.setEnd(255);
                    protocolRangeList.add(rangeIntegerDTO);
                } else {
                    CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
                    rangeIntegerDTO.setStart(Integer.valueOf(protocolString));
                    rangeIntegerDTO.setEnd(Integer.valueOf(protocolString));
                    protocolRangeList.add(rangeIntegerDTO);
                }
            }
            serviceConditionDTO.setProtocols(protocolRangeList);
            list.add(serviceConditionDTO);
        }
        serviceDTO.setServices(list);
        serviceDTO.setDeviceUuid(deviceUuid);
        serviceDTO.setSearchType("SERVICE");
        serviceDTO.setRangeOp("EQUAL");
        //带上ObjectType查询，不会查找预定义对象，但需要查找两次，SERVICE_OBJECT和SERVICE_GROUP_OBJECT
        serviceDTO.setObjectType("SERVICE_OBJECT");
        log.debug("查询现有服务对象...查询参数对象：" + JSONObject.toJSONString(serviceDTO));

        ResultRO<List<ServiceGroupObjectRO>> resultRO = whaleDeviceObjectClient.searchService(serviceDTO, null);
        ServiceGroupObjectRO groupObject = null;

        if (resultRO == null || !resultRO.getSuccess() || resultRO.getData() == null || resultRO.getData().isEmpty()) {
            groupObject = null;
        } else {
            groupObject = resultRO.getData().get(0);
        }
        RefObjectDTO refObjectDTO = null;
        //log，则查询SERVICE_GROUP_OBJECT
        if(groupObject != null) {
            refObjectDTO = new RefObjectDTO();
            refObjectDTO.setRefName(groupObject.getName());
            refObjectDTO.setObjectTypeEnum(DeviceObjectTypeEnum.getDeviceObjectTypeByCode(groupObject.getDeviceObjectType()));
            return refObjectDTO;
        }

        serviceDTO.setObjectType("SERVICE_GROUP_OBJECT");
        resultRO = whaleDeviceObjectClient.searchService(serviceDTO, null);
        if(resultRO == null || !resultRO.getSuccess() || resultRO.getData() == null || resultRO.getData().isEmpty()) {
            return  null;
        }

        groupObject = resultRO.getData().get(0);
        // #bug KSH-5790 思科墙服务复用查询到port-object时过滤掉，修改为只支持复用service-object
        if (GenerateConstants.SERVICE_GROUP_OBJECT_TYPE.equals(groupObject.getDeviceObjectType())) {
            // 对于这个类型的数据忽略
            if (null != groupObject.getCiscoRefServiceTypeEnum() && PORT_SERVICE_TYPE.equalsIgnoreCase(groupObject.getCiscoRefServiceTypeEnum().name())) {
                return null;
            }
        }
        refObjectDTO = new RefObjectDTO();
        refObjectDTO.setRefName(groupObject.getName());
        refObjectDTO.setObjectTypeEnum(DeviceObjectTypeEnum.getDeviceObjectTypeByCode(groupObject.getDeviceObjectType()));
        return refObjectDTO;
    }
}
