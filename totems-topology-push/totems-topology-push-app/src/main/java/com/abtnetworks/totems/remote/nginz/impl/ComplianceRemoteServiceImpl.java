package com.abtnetworks.totems.remote.nginz.impl;

import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PageDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IP6Utils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.external.utils.PolicyListCommonUtil;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.remote.nginz.ComplianceRemoteService;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.common.CommonRangeIntegerDTO;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.abtnetworks.totems.whale.policy.dto.FilterListsRuleSearchDTO;
import com.abtnetworks.totems.whale.policy.dto.IpTermsDTO;
import com.abtnetworks.totems.whale.policy.dto.JsonQueryDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: ???????????????
 * @date 2021/3/17
 */
@Slf4j
@Service
public class ComplianceRemoteServiceImpl implements ComplianceRemoteService {

    @Value("${topology.risk-prefix}")
    private String riskPrefix;



    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;





    @Override
    public List<DeviceFilterRuleListRO> getPolicyIdByDenyOrPermit(CmdDTO cmdDTO, String actionParam,String policyType,String ruleListUuid) {
        FilterListsRuleSearchDTO filterListsRuleSearchDTO = new FilterListsRuleSearchDTO();
        DeviceDTO device = cmdDTO.getDevice();
        String deviceUuid = device.getDeviceUuid();
        filterListsRuleSearchDTO.setDeviceUuid(deviceUuid);
        filterListsRuleSearchDTO.setRuleListUuid(ruleListUuid);
        // ??????IP??????????????????true??????????????????any????????????false?????????
        filterListsRuleSearchDTO.setSkipAny(false);
        PolicyDTO policy = cmdDTO.getPolicy();
        Integer ipType = policy.getIpType() != null ? policy.getIpType() : IpTypeEnum.IPV4.getCode();
        if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
            filterListsRuleSearchDTO.setIpType(RuleIPTypeEnum.IP6.getName());
        } else {
            filterListsRuleSearchDTO.setIpType(RuleIPTypeEnum.IP4.getName());
        }
        String srcIp = policy.getSrcIp();
        List<ServiceDTO> serviceList = policy.getServiceList();
        String dstIp = policy.getDstIp();

        String srcZone = policy.getSrcZone();
        String dstZone = policy.getDstZone();
        Integer idleTimeout = policy.getIdleTimeout();
        List<IpTermsDTO> ipTermsDTOS = new ArrayList<>();
        IpTermsDTO ipTermsDTO = new IpTermsDTO();

        // --????????????any????????? ??????setSrcAddressAny(true)????????? ????????????
        if(StringUtils.isNotBlank(srcIp)){
            if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
                List<CommonRangeStringDTO> ip6SrcAddresses = getAddressIP6DO(srcIp);
                ipTermsDTO.setIp6SrcAddresses(ip6SrcAddresses);
            } else {
                List<CommonRangeStringDTO> ip4SrcAddresses = getAddressDO(srcIp);
                ipTermsDTO.setIp4SrcAddresses(ip4SrcAddresses);
            }
        }
        // --???????????????any????????? ??????setSrcAddressAny(true)????????? ????????????
        if(StringUtils.isNotBlank(dstIp)){
            if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
                List<CommonRangeStringDTO> ip6DstAddresses = getAddressIP6DO(dstIp);
                ipTermsDTO.setIp6DstAddresses(ip6DstAddresses);
            } else {
                List<CommonRangeStringDTO> ip4DstAddresses = getAddressDO(dstIp);
                ipTermsDTO.setIp4DstAddresses(ip4DstAddresses);
            }
        }
        List<CommonRangeIntegerDTO> protocols = new ArrayList<>();
        List<CommonRangeIntegerDTO> dstPort = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(serviceList)) {
            for (ServiceDTO serviceDTO : serviceList) {
                Integer protocol = Integer.parseInt(serviceDTO.getProtocol());
                // ??????????????????0?????????????????????????????????
                if (0 != protocol) {
                    CommonRangeIntegerDTO commonRangeStringDTO = new CommonRangeIntegerDTO(protocol, protocol);
                    protocols.add(commonRangeStringDTO);
                }
                String dstPorts = serviceDTO.getDstPorts();

                if (StringUtils.isNotBlank(dstPorts) && !CommonConstants.ANY.equalsIgnoreCase(dstPorts)) {
                    String[] dstPortArray = StringUtils.split(dstPorts, ",");
                    for (String port : dstPortArray) {
                        if (PortUtils.isPortRange(port)) {
                            String[] ports = port.split("-");
                            CommonRangeIntegerDTO commonRangeIntegerDTO = new CommonRangeIntegerDTO(Integer.parseInt(ports[0]), Integer.parseInt(ports[1]));
                            dstPort.add(commonRangeIntegerDTO);

                        } else {
                            CommonRangeIntegerDTO commonRangeIntegerDTO = new CommonRangeIntegerDTO(Integer.parseInt(port), Integer.parseInt(port));
                            dstPort.add(commonRangeIntegerDTO);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(protocols)) {
                ipTermsDTO.setProtocols(protocols);

            }
            if (CollectionUtils.isNotEmpty(dstPort)) {
                ipTermsDTO.setDstPorts(dstPort);

            }
        }
        if (ipTermsDTO != null) {
            ipTermsDTOS.add(ipTermsDTO);
        }

        filterListsRuleSearchDTO.setIpTerms(ipTermsDTOS);

        if (!AliStringUtils.isEmpty(policyType)) {
            JsonQueryDTO jsonQueryDTO = new JsonQueryDTO();
            /*filterListType: ?????????type???????????????????????????????????? SYSTEM__POLICY_1, SYSTEM__POLICY_2???NAT?????????
            SYSTEM__NAT_LIST???ACL????????? UNKNOWN_LIST_TYPE, SYSTEM__GENERIC_ACL*/
            Map<String, String[]> filterListType = new HashMap<>();
            filterListType.put("$in", policyType.split(","));
            jsonQueryDTO.setFilterListType(filterListType);
            /*action??????????????????????????????????????? DENY, PERMIT*/
            // a)????????????????????????: ???????????????+???????????????????????????????????????
            // b)??????????????????????????????????????????+??????deny???????????????????????????
            if(null == idleTimeout || 0 == idleTimeout){
                Map<String, String[]> action = new HashMap<>();
                action.put("$in", new String[]{actionParam});
                jsonQueryDTO.setAction(action);
            }
            // ??????????????????
            Map<String, Boolean> activeMap = new HashMap<>();
            activeMap.put("$ne", true);
            jsonQueryDTO.setInactive(activeMap);

            filterListsRuleSearchDTO.setJsonQuery(jsonQueryDTO);
        }
        // ????????????50??? ??????????????????????????????
        PageDTO pageDTO = new PageDTO();
        pageDTO.setPage(1);
        pageDTO.setPsize(50);
        log.info("???????????????????????????????????????{}", JSONObject.toJSONString(filterListsRuleSearchDTO));
        ResultRO<List<DeviceFilterRuleListRO>> totalDataResultRO = whaleDevicePolicyClient.getFilterRuleListSearch(filterListsRuleSearchDTO, pageDTO);
        log.debug("??????????????????????????????????????????{}", JSONObject.toJSONString(totalDataResultRO));

        if (totalDataResultRO == null || !totalDataResultRO.getSuccess() || totalDataResultRO.getData() == null) {
            return null;
        }

        return filterRuleListROS(srcZone, dstZone, totalDataResultRO);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param srcZone
     * @param dstZone
     * @param totalDataResultRO
     * @return
     */
    private List<DeviceFilterRuleListRO> filterRuleListROS(String srcZone, String dstZone, ResultRO<List<DeviceFilterRuleListRO>> totalDataResultRO) {
        List<DeviceFilterRuleListRO> ruleListROS = totalDataResultRO.getData();
        List<DeviceFilterRuleListRO> resultRuleList = new ArrayList<>();
        for (DeviceFilterRuleListRO deviceFilterRuleListRO : ruleListROS) {
            if (CollectionUtils.isNotEmpty(deviceFilterRuleListRO.getInInterfaceGroupRefs()) && CollectionUtils.isNotEmpty(deviceFilterRuleListRO.getOutInterfaceGroupRefs())) {
                if (deviceFilterRuleListRO.getInInterfaceGroupRefs().contains(srcZone) && deviceFilterRuleListRO.getOutInterfaceGroupRefs().contains(dstZone)) {
                    resultRuleList.add(deviceFilterRuleListRO);
                    break;
                }
            } else if (CollectionUtils.isEmpty(deviceFilterRuleListRO.getInInterfaceGroupRefs()) && CollectionUtils.isEmpty(deviceFilterRuleListRO.getOutInterfaceGroupRefs())) {
                resultRuleList.add(deviceFilterRuleListRO);
                break;
            } else if (CollectionUtils.isNotEmpty(deviceFilterRuleListRO.getInInterfaceGroupRefs()) && CollectionUtils.isEmpty(deviceFilterRuleListRO.getOutInterfaceGroupRefs())) {
                if (deviceFilterRuleListRO.getInInterfaceGroupRefs().contains(srcZone)) {
                    resultRuleList.add(deviceFilterRuleListRO);
                    break;
                }
            } else if(CollectionUtils.isEmpty(deviceFilterRuleListRO.getInInterfaceGroupRefs()) && CollectionUtils.isNotEmpty(deviceFilterRuleListRO.getOutInterfaceGroupRefs())){
                if (deviceFilterRuleListRO.getOutInterfaceGroupRefs().contains(dstZone)) {
                    resultRuleList.add(deviceFilterRuleListRO);
                    break;
                }
            }
        }
        log.info("???????????????????????????:{},?????????:{},???????????????????????????????????????:{}",srcZone,dstZone,JSONObject.toJSONString(resultRuleList));
        return resultRuleList;
    }

    private List<CommonRangeStringDTO> getAddressDO(String dstOrSrcIp) {
        List<CommonRangeStringDTO> ip4SrcAddresses = new ArrayList<>();
        if (StringUtils.isNotBlank(dstOrSrcIp)) {
            String[] srcAddresses = dstOrSrcIp.split(",");
            for (String ip : srcAddresses) {

                CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();

                if (IpUtils.isIPRange(ip)) {
                    String[] ipSegment = ip.trim().split("-");
                    commonRangeStringDTO.setStart(ipSegment[0]);
                    commonRangeStringDTO.setEnd(ipSegment[1]);
                    ip4SrcAddresses.add(commonRangeStringDTO);
                    continue;
                } else if (IpUtils.isIPSegment(ip)) {
                    String startIp = IpUtils.getStartIp(ip);
                    String endIp = IpUtils.getEndIp(ip);
                    commonRangeStringDTO.setStart(startIp);
                    commonRangeStringDTO.setEnd(endIp);
                    ip4SrcAddresses.add(commonRangeStringDTO);
                    continue;
                } else if (IpUtils.isIP(ip)) {
                    commonRangeStringDTO.setStart(ip);
                    commonRangeStringDTO.setEnd(ip);
                    ip4SrcAddresses.add(commonRangeStringDTO);
                    continue;
                } else {
                    log.error("?????????????????????{}", dstOrSrcIp);
                    throw new IllegalArgumentException("????????????????????????ip" + dstOrSrcIp + "??????");
                }

            }
        }
        return ip4SrcAddresses;
    }

    private List<CommonRangeStringDTO> getAddressIP6DO(String dstOrSrcIp) {
        List<CommonRangeStringDTO> ip6SrcAddresses = new ArrayList<>();
        if (StringUtils.isNotBlank(dstOrSrcIp)) {
            String[] srcAddresses = dstOrSrcIp.split(",");
            for (String ip : srcAddresses) {

                CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();

                if (IpUtils.isIPv6Range(ip)) {
                    String[] ipSegment = ip.trim().split("-");
                    commonRangeStringDTO.setStart(ipSegment[0]);
                    commonRangeStringDTO.setEnd(ipSegment[1]);
                    ip6SrcAddresses.add(commonRangeStringDTO);
                    continue;
                } else if (IpUtils.isIPv6Subnet(ip)) {
                    CommonRangeStringDTO startEndIpFromIpv6Address = IP6Utils.getStartEndIpFromIpv6Address(ip);
                    String startIp = startEndIpFromIpv6Address.getStart();
                    String endIp = startEndIpFromIpv6Address.getEnd();
                    commonRangeStringDTO.setStart(startIp);
                    commonRangeStringDTO.setEnd(endIp);
                    ip6SrcAddresses.add(commonRangeStringDTO);
                    continue;
                } else if (IpUtils.isIPv6(ip)) {
                    commonRangeStringDTO.setStart(ip);
                    commonRangeStringDTO.setEnd(ip);
                    ip6SrcAddresses.add(commonRangeStringDTO);
                    continue;
                } else {
                    log.error("?????????????????????{}", dstOrSrcIp);
                    throw new IllegalArgumentException("????????????????????????ip" + dstOrSrcIp + "??????");
                }

            }
        }
        return ip6SrcAddresses;
    }

    public List<PolicyDetailVO> parseRuleListRO(ResultRO<List<DeviceFilterRuleListRO>> dataResultRO, PolicyTypeEnum typeEnum, String deviceUuid, DeviceRO deviceRO) {
        int total = dataResultRO.getTotal();
        // ???????????????????????????
        List<DeviceFilterRuleListRO> roList = new ArrayList<>();
        if (typeEnum.getCode() == PolicyTypeEnum.SYSTEM__GENERIC_ACL.getCode() || typeEnum.getCode() == PolicyTypeEnum.SYSTEM__POLICY_1.getCode()) {
            roList = processVsysName(dataResultRO.getData(), total, deviceUuid, deviceRO);
        } else {
            roList = dataResultRO.getData();
        }
        List<PolicyDetailVO> voList = new ArrayList<>();
        if (typeEnum.getCode() == PolicyTypeEnum.SYSTEM__GENERIC_ACL.getCode() || typeEnum.getCode() == PolicyTypeEnum.SYSTEM__POLICY_1.getCode()
                || typeEnum.getCode() == PolicyTypeEnum.SYSTEM__NAT_LIST.getCode() || typeEnum.getCode() == PolicyTypeEnum.SYSTEM__POLICY_ROUTING.getCode()) {
            voList = PolicyListCommonUtil.getPageShowData(roList);
        }
        return voList;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    private List<DeviceFilterRuleListRO> processVsysName(List<DeviceFilterRuleListRO> list, Integer total, String deviceUuid, DeviceRO deviceRO) {
        List<DeviceFilterRuleListRO> resultList = new ArrayList<>();
        if (list == null || list.size() == 0) {
            return resultList;
        }

        if (deviceRO == null || deviceRO.getData() == null || deviceRO.getData().size() == 0) {
            log.error("????????????????????????,deviceUuid:{}", deviceUuid);
            return resultList;
        }
        DeviceDataRO deviceObj = deviceRO.getData().get(0);
        Boolean isVsys = deviceObj.getIsVsys();

        String vsysName = deviceObj.getVsysName();
        String rootDeviceUuid = deviceObj.getRootDeviceUuid();

        //??????
        for (DeviceFilterRuleListRO ro : list) {
            if (!StringUtils.isEmpty(vsysName)) {
                ro.setVsysName(vsysName);
            }
            if (isVsys != null) {
                ro.setIsVsys(isVsys);
            }
            if (!StringUtils.isEmpty(rootDeviceUuid)) {
                ro.setRootDeviceUuid(rootDeviceUuid);
            }

            resultList.add(ro);
        }
        return resultList;
    }

}
