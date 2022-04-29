package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineBusinessInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.manager.DenyPolicyInfoDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyTypeEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.remote.nginz.ComplianceRemoteService;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @desc 思科路由交换设备 根据设备uuid和五元组信息获取策略uuid和匹配到的deny策略
 * @date 2021/4/29 16:26
 */
@Slf4j
@Service
public class GetAclRuleListUuidServiceImpl implements CmdService {

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    ComplianceRemoteService complianceRemoteService;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        DeviceDTO deviceDTO = cmdDto.getDevice();
        PolicyDTO policy = cmdDto.getPolicy();
        List<InterfacesRO> interfacesROS = whaleDeviceObjectClient.getDeviceInterfacesRO(deviceDTO.getDeviceUuid());
        log.info("根据设备uuid:{}查询该设备接口信息结果为:{}",deviceDTO.getDeviceUuid(),JSONObject.toJSONString(interfacesROS));
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceDTO.getDeviceUuid());
        List<InterfacesRO> juniperInterfaces = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(deviceRO.getData())) {
            for (DeviceDataRO datum : deviceRO.getData()) {
                juniperInterfaces.addAll(datum.getInterfaces());
            }
            log.info("juniper设备，设备UUID：{}查询接口信息结果条数为：{}", deviceDTO.getDeviceUuid(),deviceRO.getData().size());
        }
        if (CollectionUtils.isEmpty(interfacesROS) || CollectionUtils.isEmpty(juniperInterfaces)) {
            log.error("根据设备uuid:{}查询设备接口信息为空!",deviceDTO.getDeviceUuid());
            return;
        }
        String srcItf = policy.getSrcItf();
        String dstItf = policy.getDstItf();
        // 格式为:["out_100","out_200"]
        CommandLineBusinessInfoDTO businessInfoDTO = new CommandLineBusinessInfoDTO();
        DeviceModelNumberEnum modelNumber = deviceDTO.getModelNumber();
        String ruleListName = "";
        switch (modelNumber) {
            case CISCO_IOS:
            case CISCO_NX_OS:
            case RUIJIE:
            case H3CV7:
                ruleListName = getRuleListName(deviceDTO, interfacesROS, srcItf, dstItf);
                break;
            case JUNIPER_ROUTER:
                // juniper没有源接口和目的接口概念，默认源接口
                ruleListName = getJuniperRuleListName(juniperInterfaces, srcItf);
                break;
            default:
                break;
        }
        // String ruleListName = getRuleListName(deviceDTO, interfacesROS, srcItf, dstItf);
        if(StringUtils.isBlank(ruleListName)){
            log.info("获取设备上的接口绑定的acl策略名称为空,进去新建策略集");
            return;
//            businessInfoDTO.setOtherErrorMsg(SendErrorEnum.ITF_ACL_LIST_NOT_HAVE.getMessage());
//            cmdDto.setBusinessInfoDTO(businessInfoDTO);
//            throw new IssuedExecutorException(SendErrorEnum.ITF_ACL_LIST_NOT_HAVE);
        }
        ruleListName = ruleListName.replace("[", "").replace("]", "").replace("\"", "");
        String actualRuleListName = ruleListName.split(",")[0];
        // 根据策略集名称获取策略集uuid
        // 首先根据设备uuid获取设备下面的策略集
        ResultRO<List<DeviceFilterlistRO>> reos = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceDTO.getDeviceUuid());
        log.info("根据设备uuid:{}获取设备下面的策略集结果为:{}",deviceDTO.getDeviceUuid(),JSONObject.toJSONString(reos));
        List<DeviceFilterlistRO> filterlistROS = reos.getData();
        if (CollectionUtils.isEmpty(filterlistROS)) {
            log.error("根据设备uuid:{}查询设备所有的策略集合为空!",deviceDTO.getDeviceUuid());
            return;
        }
        String ruleListUuid = null;
        for (DeviceFilterlistRO deviceFilterlistRO : filterlistROS) {
            if (actualRuleListName.equalsIgnoreCase(deviceFilterlistRO.getName())) {
                ruleListUuid = deviceFilterlistRO.getUuid();
                break;
            }
        }
        if(StringUtils.isBlank(ruleListUuid)){
            log.error("根据设备uuid:{}和策略集名称:{} 没有匹配到对应的策略集名称所对应的策略集uuid",deviceDTO.getDeviceUuid(),actualRuleListName);
            // 这种情况默认使用接口上挂载的acl名称即为空的acl策略名称，直接下发到该策略集，不用后续的策略uuid和匹配
            businessInfoDTO.setOtherErrorMsg(SendErrorEnum.RULE_LIST_NOT_EXIST.getMessage());
            cmdDto.setBusinessInfoDTO(businessInfoDTO);
            throw new IssuedExecutorException(SendErrorEnum.RULE_LIST_NOT_EXIST);
        }
        // 根据设备uuid和策略uuid和五元组信息查询匹配deny策略
        String aclDefualPolicyType = PolicyTypeEnum.SYSTEM__GENERIC_ACL.getRuleListType();
        aclDefualPolicyType = aclDefualPolicyType.replace("/", ",");
        // 取动作,如果是允许，则去根据五元组去查询deny策略,如果是拒绝，则去查询permit策略
        String actionParam = null;
        if(ActionEnum.PERMIT.equals(policy.getAction())){
            actionParam = ActionEnum.DENY.getKey().toUpperCase();
        }else {
            actionParam = ActionEnum.PERMIT.getKey().toUpperCase();
        }
        log.info("根据设备uuid:[{}]根据策略集:[{}]去查询:{}策略----", cmdDto.getDevice().getDeviceUuid(), ruleListUuid, actionParam);
        List<DeviceFilterRuleListRO> resultRO =
            complianceRemoteService.getPolicyIdByDenyOrPermit(cmdDto, actionParam, aclDefualPolicyType, ruleListUuid);
        log.info("根据设备uuid:[{}]根据策略集:[{}],查询{}策略返回有{}", cmdDto.getDevice().getDeviceUuid(), ruleListUuid, actionParam,
            JSONObject.toJSONString(resultRO));
        if (CollectionUtils.isEmpty(resultRO)) {
            log.info("查询到deny策略返回为空，默认匹配到默认deny策略");
        }
        for (DeviceFilterRuleListRO deviceFilterRuleListRO : resultRO) {
            String policyId = deviceFilterRuleListRO.getRuleId();
            String queryResultRuleListUuid = deviceFilterRuleListRO.getRuleListUuid();
            if (StringUtils.isNotBlank(policyId) && ruleListUuid.equalsIgnoreCase(queryResultRuleListUuid)) {
                deviceDTO.setMatchRuleId(policyId);
                break;
            }
        }
        log.info("获取到策略集uuid为:{}策略集名称:{}匹配到的deny策略id为{}", ruleListUuid, actualRuleListName, deviceDTO.getMatchRuleId());
        deviceDTO.setRuleListUuid(ruleListUuid);
        deviceDTO.setRuleListName(actualRuleListName);
    }

    private String getJuniperRuleListName(List<InterfacesRO> juniperInterfaces, String srcItf) {
        for (InterfacesRO interfacesRO : juniperInterfaces) {
            // 接口为空取第一个in方向挂载acl策略集来
            if (StringUtils.isBlank(srcItf)) {
                if (StringUtils.isNotBlank(interfacesRO.getInboundRuleListRefs())) {
                    return interfacesRO.getInboundRuleListRefs();
                }
            } else {
                // 接口不为空取第一个接口名称匹配的接口来
                if (srcItf.equalsIgnoreCase(interfacesRO.getName()) && StringUtils.isNotBlank(interfacesRO.getInboundRuleListRefs())) {
                    return interfacesRO.getInboundRuleListRefs();
                }
            }
        }
        return null;
    }


    /**
     * 获取策略集名称
     *
     * @param deviceDTO
     * @param interfacesROS
     * @param srcItf
     * @param dstItf
     */
    private String getRuleListName(DeviceDTO deviceDTO, List<InterfacesRO> interfacesROS, String srcItf, String dstItf) {
        // 查询高级设置接口acl策略挂载方向0:in  1:out
        Integer aclDirection = recommendTaskManager.getAclDirection(deviceDTO.getDeviceUuid());
        for (InterfacesRO interfacesRO : interfacesROS) {
            if (0 == aclDirection) {
                // 接口为空取第一个in方向挂载acl策略集来
                if (StringUtils.isBlank(srcItf)) {
                    if (StringUtils.isNotBlank(interfacesRO.getInboundRuleListRefs())) {
                        return interfacesRO.getInboundRuleListRefs();
                    }
                } else {
                    // 接口不为空取第一个接口名称匹配的接口来
                    if (srcItf.equalsIgnoreCase(interfacesRO.getName())) {
                        return interfacesRO.getInboundRuleListRefs();
                    }
                }
            } else {
                // 接口为空取第一个out方向挂载acl策略集来
                if (StringUtils.isBlank(dstItf)) {
                    if (StringUtils.isNotBlank(interfacesRO.getOutboundRuleListRefs())) {
                        return interfacesRO.getOutboundRuleListRefs();
                    }
                } else {
                    // 接口不为空取第一个接口名称匹配的接口来
                    if (dstItf.equalsIgnoreCase(interfacesRO.getName())) {
                        return interfacesRO.getOutboundRuleListRefs();
                    }
                }
            }
        }
        return null;
    }
}
