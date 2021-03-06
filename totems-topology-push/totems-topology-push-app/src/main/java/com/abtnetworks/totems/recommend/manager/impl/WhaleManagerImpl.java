package com.abtnetworks.totems.recommend.manager.impl;

import com.abtnetworks.totems.common.config.ProtocolMapConfig;
import com.abtnetworks.totems.common.constants.*;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PageDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.external.utils.PolicyListCommonUtil;
import com.abtnetworks.totems.external.vo.DeviceDetailRunVO;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.recommend.annotation.TimeCounter;
import com.abtnetworks.totems.recommend.dto.recommend.IpTermsExtendDTO;
import com.abtnetworks.totems.recommend.dto.risk.DeviceInterfaceDto;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.dto.task.PackFilterDTO;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.dto.*;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.*;
import com.abtnetworks.totems.whale.common.CommonRangeIntegerDTO;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.abtnetworks.totems.whale.policy.dto.*;
import com.abtnetworks.totems.whale.policy.ro.*;
import com.abtnetworks.totems.whale.policy.service.WhaleAnalysisClient;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.abtnetworks.totems.whale.policybasic.ro.FilterListsRO;
import com.abtnetworks.totems.whale.policybasic.service.WhaleFilterListClient;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckPolicyRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultRO;
import com.abtnetworks.totems.whale.policyoptimize.service.WhaleRuleCheckClient;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.*;

import static com.abtnetworks.totems.common.constants.GenerateConstants.PORT_SERVICE_TYPE;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4;
import static com.abtnetworks.totems.common.enums.AddressPropertyEnum.*;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.isRangeCiscoCode;
import static com.abtnetworks.totems.common.enums.DeviceNetworkTypeEnum.*;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV6;
import static com.abtnetworks.totems.common.enums.PolicyTypeEnum.SYSTEM__NAT_LIST;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/13 16:59
 */
@Service
public class WhaleManagerImpl implements WhaleManager {
    private static Logger logger = LoggerFactory.getLogger(WhaleManagerImpl.class);

    @Autowired
    private WhaleSubnetObjectClient whaleSubnetObjectClient;

    @Autowired
    private WhalePathAnalyzeClient whalePathAnalyzeClient;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    private WhaleAnalysisClient whaleAnalysisClient;

    @Autowired
    private WhaleFolderClient whaleFolderClient;

    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    private IpServiceNameRefClient ipServiceNameRefClient;

    @Autowired
    private WhaleFilterListClient whaleFilterListClient;

    @Autowired
    private WhaleRuleCheckClient whaleRuleCheckClient;

    @Autowired
    private WhaleWhatIfClient whaleWhatIfClient;

    @Autowired
    ProtocolMapConfig protocolMapConfig;

    @Value("${push.reuse-src-port:false}")
    private boolean reuse_src_port;


    @Override
    public List<String> getSubnetUuidList(String ipAddress) {

        SubnetUuidListRO uuidList = whaleSubnetObjectClient.getSubnetUuidList(ipAddress);
        if (uuidList == null) {
            return null;
        }
        return uuidList.getData();
    }

    @Override
    public PathAnalyzeRO queryPath(PathAnalyzeDTO pathAnalyzeDTO) {
        logger.info("??????????????????...");
        PathAnalyzeRO pathAnalyzeRO = null;
        try {
            pathAnalyzeRO = whalePathAnalyzeClient.getDetailPathObject(pathAnalyzeDTO);
        } catch (Exception e) {
            logger.error("Whale??????????????????????????????", e);
        }
        return pathAnalyzeRO;
    }

    @Override
    @TimeCounter
    public DeviceRO getDeviceByUuid(String deviceUuid) {
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);

        return deviceRO;
    }

    @Override
    public ZoneRO getDeviceZoneVO(String deviceUuid) {
        ZoneRO zone = null;
        try {
            zone = whaleDeviceObjectClient.getDeviceZoneRO(deviceUuid);
        } catch (Exception e) {
            logger.error("Whale???????????????????????????????????????", e);
        }
        return zone;
    }

    @Override
    @TimeCounter
    public ZoneRO getDeviceZone(String deviceUuid) throws Exception {
        ZoneRO zone = whaleDeviceObjectClient.getDeviceZoneRO(deviceUuid);
        return zone;
    }

    @Override
    public DeviceDetailRO getDeviceDetail(PathDetailRO pathDetail) throws Exception {
        return getDeviceDetail(pathDetail, null);
    }

    @Override
    @TimeCounter
    public DeviceDetailRO getDeviceDetail(PathDetailRO pathDetail, String whatIfCaseUuid) throws Exception {
        return whalePathAnalyzeClient.getDeviceDetail(pathDetail, whatIfCaseUuid);
    }

    @Override
    public String startAnalysisAccess() {
        AnalysisStartRO analysisStartRO = whaleAnalysisClient.analysisAccessStart();
        AnalysisStartDataRO data = analysisStartRO.getData().get(0);
        return data.getId();
    }

    @Override
    public String checkAnalysisAccessTask(String id) {
        AnalysisStartRO analysisStartRO = whaleAnalysisClient.analysisAccessStatus(id);

        AnalysisStartDataRO data = analysisStartRO.getData().get(0);

        String result = data.getStatus();
        logger.info("????????????????????????" + result);

        return result;
    }

    @Override
    public PathAnalyzeDTO getAnylizePathDTO(PathInfoTaskDTO taskDTO) {
        String srcIpListString = taskDTO.getSrcIp(),  dstIpListString = taskDTO.getDstIp(), whatIfCaseUuid = taskDTO.getWhatIfCaseUuid()
                 ,srcNodeUuid = taskDTO.getSrcNodeUuid(),  dstNodeUuid =  taskDTO.getDstNodeUuid();
        List<ServiceDTO> serviceList =  taskDTO.getServiceList();
        //????????????????????????????????????????????????ipType,????????????????????????????????????????????????
        logger.debug(String.format("??????????????????????????????(%s, %s, %s, %s, %s, %s)...", srcIpListString, dstIpListString, JSONObject.toJSONString(serviceList), srcNodeUuid, dstNodeUuid, whatIfCaseUuid));
        int ipTypeInt = ObjectUtils.isEmpty(taskDTO.getIpType())?0:taskDTO.getIpType();
        //????????????????????????
        PathAnalyzeDTO pathAnalyzeDTO = new PathAnalyzeDTO();
        pathAnalyzeDTO.setIsPathOnly(true);
        pathAnalyzeDTO.setRequestType("REAL_TIME");
        pathAnalyzeDTO.setSrcNodeId(srcNodeUuid);
        pathAnalyzeDTO.setDstNodeId(dstNodeUuid);
        String ipTypeParam = IPV4.getCode().equals(ipTypeInt)  ? PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4 : PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6;
        pathAnalyzeDTO.setIpType(ipTypeParam);
        if (!AliStringUtils.isEmpty(whatIfCaseUuid)) {
            pathAnalyzeDTO.setWhatifCaseUuid(whatIfCaseUuid);
        }
        List<String> srcIpList = null;
        if (!AliStringUtils.isEmpty(srcIpListString)) {
            srcIpList = new ArrayList<>();
            String[] srcIpStrings = srcIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String srcIp : srcIpStrings) {
                srcIpList.add(srcIp);
            }
        }
        List<String> dstIpList = null;
        if (!AliStringUtils.isEmpty(dstIpListString)) {
            dstIpList = new ArrayList<>();
            String[] dstIpStrings = dstIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String dstIp : dstIpStrings) {
                dstIpList.add(dstIp);
            }
        }

        //2?????????????????????
        pathAnalyzeDTO.setOpt(2);
        PackFilterDTO packFilterDTO = new PackFilterDTO();
        packFilterDTO.setSrcIpList(srcIpList);
        packFilterDTO.setDstIpList(dstIpList);

        packFilterDTO.setIpType(ipTypeInt);
        packFilterDTO.setServiceList(serviceList);
        List<FilterDTO> beginFlow = packFilterDTO(packFilterDTO);

        pathAnalyzeDTO.setBeginFlow(beginFlow);

        return pathAnalyzeDTO;
    }

    @Override
    public PathAnalyzeDTO getAnylizePathDTO(String srcIp, String srcPort, String dstIp, String dstPort, String protocol) {
        logger.info(String.format("??????????????????????????????(%s, %s, %s, %s, %s)...", srcIp, srcPort, dstIp, dstPort, protocol));

        //String startSrcIp = IpUtils.getStartIpFromIpAddress(srcIp);
        //String endSrcIp = IpUtils.getEndIpFromIpAddress(srcIp);
        String startSrcIp = IpUtils.getStartIpFromIpAddresses(srcIp);

        //String startDstIp = IpUtils.getStartIpFromIpAddress(dstIp);
        //String endDstIp = IpUtils.getEndIpFromIpAddress(dstIp);
        String startDstIp = IpUtils.getStartIpFromIpAddresses(dstIp);

        //??????????????????UUID?????????????????????????????????????????????????????????????????????UUID
        List<String> srcNodeUuidList = getNodeUuidList(startSrcIp);
        List<String> dstNodeUuidList = getNodeUuidList(startDstIp);

        if (srcNodeUuidList.size() != 1 || dstNodeUuidList.size() != 1) {
            logger.info("????????????????????????????????????");
            return null;
        }

        //????????????
        int protocolNum = WhaleUtils.getProtocolNum(protocol);
        if (protocolNum == -2) {
            logger.info("????????????????????????");
            return null;
        }

        logger.info(String.format("?????????(%s)????????????UUID???(%s), ????????????(%s)????????????UUID???(%s)",
                srcIp, srcNodeUuidList.get(0), dstIp, dstNodeUuidList.get(0)));

        //????????????????????????
        PathAnalyzeDTO pathAnalyzeDTO = new PathAnalyzeDTO();
        pathAnalyzeDTO.setIsPathOnly(true);
        pathAnalyzeDTO.setRequestType("REAL_TIME");
        pathAnalyzeDTO.setSrcNodeId(srcNodeUuidList.get(0));
        pathAnalyzeDTO.setDstNodeId(dstNodeUuidList.get(0));
        pathAnalyzeDTO.setIpType(PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4);

        FilterDTO filterDTO = new FilterDTO();

        //?????????ip
        List<SrcDstStringDTO> srcIpList = new ArrayList<>();
        //?????????ip
        String[] srcIpStrs = srcIp.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String srcIpStr : srcIpStrs) {
            String startIp = IpUtils.getStartIpFromIpAddress(srcIpStr);
            String endIp = IpUtils.getEndIpFromIpAddress(srcIpStr);
            srcIpList.add(WhaleDoUtils.getSrcDstStringDTO(startIp, endIp));
        }
        filterDTO.setIp4SrcAddresses(srcIpList);

        //????????????ip
        List<SrcDstStringDTO> dstIpList = new ArrayList<>();
        String[] dstIpStrs = dstIp.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String strIpStr : dstIpStrs) {
            String startIp = IpUtils.getStartIpFromIpAddress(strIpStr);
            String endIp = IpUtils.getEndIpFromIpAddress(strIpStr);
            dstIpList.add(WhaleDoUtils.getSrcDstStringDTO(startIp, endIp));
        }
        filterDTO.setIp4DstAddresses(dstIpList);

        //????????????
        filterDTO.setSrcPorts(WhaleUtils.getSrcDstIntegerDTOList(srcPort));
        filterDTO.setDstPorts(WhaleUtils.getSrcDstIntegerDTOList(dstPort));

        List<SrcDstIntegerDTO> protocolList = new ArrayList<>();
        SrcDstIntegerDTO srcDstIntegerDTO = new SrcDstIntegerDTO();
        if (protocolNum == -1) {
            srcDstIntegerDTO.setStart(0);
            srcDstIntegerDTO.setEnd(255);
        } else {
            srcDstIntegerDTO.setStart(protocolNum);
            srcDstIntegerDTO.setEnd(protocolNum);
        }
        protocolList.add(srcDstIntegerDTO);
        filterDTO.setProtocols(protocolList);

        List<FilterDTO> beginFlow = new ArrayList<>();
        beginFlow.add(filterDTO);
        pathAnalyzeDTO.setBeginFlow(beginFlow);

        return pathAnalyzeDTO;
    }



    @Override
    public PathAnalyzeDTO getAnylizePathDTO(String srcIpListString, String dstIpListString, List<ServiceDTO> serviceList,
                                            String srcNodeUuid, String dstNodeUuid, String whatIfCaseUuid) {
        return getAnylizePathDTO(srcIpListString, dstIpListString, serviceList, srcNodeUuid, dstNodeUuid, whatIfCaseUuid, null);
    }

    @Override
    public PathAnalyzeDTO getAnylizePathDTO(String srcIpListString, String dstIpListString, List<ServiceDTO> serviceList,
                                            String srcNodeUuid, String dstNodeUuid, String whatIfCaseUuid, Integer idleTimeout) {
        logger.debug(String.format("??????????????????????????????(%s, %s, %s, %s, %s, %s)...", srcIpListString, dstIpListString, JSONObject.toJSONString(serviceList), srcNodeUuid, dstNodeUuid, whatIfCaseUuid));

        //????????????????????????
        PathAnalyzeDTO pathAnalyzeDTO = new PathAnalyzeDTO();
        pathAnalyzeDTO.setIsPathOnly(true);
        pathAnalyzeDTO.setRequestType("REAL_TIME");
        pathAnalyzeDTO.setSrcNodeId(srcNodeUuid);
        pathAnalyzeDTO.setDstNodeId(dstNodeUuid);
        pathAnalyzeDTO.setIpType(PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4);
        pathAnalyzeDTO.setIdleTimeout(idleTimeout);
        if (!AliStringUtils.isEmpty(whatIfCaseUuid)) {
            pathAnalyzeDTO.setWhatifCaseUuid(whatIfCaseUuid);
        }

        List<String> srcIpList = null;
        if (!AliStringUtils.isEmpty(srcIpListString)) {
            srcIpList = new ArrayList<>();
            String[] srcIpStrings = srcIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String srcIp : srcIpStrings) {
                srcIpList.add(srcIp);
            }
        }

        List<String> dstIpList = null;
        if (!AliStringUtils.isEmpty(dstIpListString)) {
            dstIpList = new ArrayList<>();
            String[] dstIpStrings = dstIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String dstIp : dstIpStrings) {
                dstIpList.add(dstIp);
            }
        }
        //???????????????????????????ipv4??????
        PackFilterDTO packFilterDTO = new PackFilterDTO();
        packFilterDTO.setSrcIpList(srcIpList);
        packFilterDTO.setDstIpList(dstIpList);
        packFilterDTO.setIpType(0);
        packFilterDTO.setServiceList(serviceList);
        List<FilterDTO> beginFlow = packFilterDTO(packFilterDTO);

        pathAnalyzeDTO.setBeginFlow(beginFlow);

        return pathAnalyzeDTO;
    }

    @Override
    public PathAnalyzeDTO getAnylizePathDTOByQT(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO) {

        String srcIpListString = task.getSrcIp(), dstIpListString = task.getDstIp(), srcNodeUuid = task.getSrcNodeUuid(), dstNodeUuid = task.getDstNodeUuid();
        List<ServiceDTO> serviceList = task.getServiceList();
        JSONObject deviceWhatifs = task.getDeviceWhatifs();
        Integer idleTimeout = task.getIdleTimeout();
        Integer ipType = simulationTaskDTO.getIpType() == null ? 0 : simulationTaskDTO.getIpType();
        logger.debug(String.format("??????????????????????????????(%s, %s, %s, %s, %s, %s, ip?????? %s)...", srcIpListString, dstIpListString, JSONObject.toJSONString(serviceList), srcNodeUuid, dstNodeUuid, deviceWhatifs, ipType));

        //????????????????????????
        PathAnalyzeDTO pathAnalyzeDTO = new PathAnalyzeDTO();
        pathAnalyzeDTO.setIsPathOnly(true);
        pathAnalyzeDTO.setRequestType("REAL_TIME");
        pathAnalyzeDTO.setSrcNodeId(srcNodeUuid);
        pathAnalyzeDTO.setDstNodeId(dstNodeUuid);
        String ipTypeParam = IPV4.getCode().equals(ipType) ? PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4 : PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6;
        pathAnalyzeDTO.setIpType(ipTypeParam);
        pathAnalyzeDTO.setIdleTimeout(idleTimeout);
        if (deviceWhatifs != null) {
            pathAnalyzeDTO.setDeviceWhatifs(deviceWhatifs);
        }
        pathAnalyzeDTO.setRuleStart(simulationTaskDTO.getStartTime());
        pathAnalyzeDTO.setRuleEnd(simulationTaskDTO.getEndTime());
        List<String> srcIpList = null;
        if (!AliStringUtils.isEmpty(srcIpListString)) {
            srcIpList = new ArrayList<>();
            String[] srcIpStrings = srcIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String srcIp : srcIpStrings) {
                srcIpList.add(srcIp);
            }
        }

        List<String> dstIpList = null;
        if (!AliStringUtils.isEmpty(dstIpListString)) {
            dstIpList = new ArrayList<>();
            String[] dstIpStrings = dstIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String dstIp : dstIpStrings) {
                dstIpList.add(dstIp);
            }
        } else {
            // ?????????????????????????????????????????????????????????
//            pathAnalyzeDTO.setOpt(2);
        }
        //2?????????????????????
        pathAnalyzeDTO.setOpt(2);
        //??????ipv4???ipv6??????????????????
        PackFilterDTO packFilterDTO = new PackFilterDTO();
        packFilterDTO.setSrcIpList(srcIpList);
        packFilterDTO.setDstIpList(dstIpList);
        packFilterDTO.setIpType(ipType);
        packFilterDTO.setServiceList(serviceList);
        List<FilterDTO> beginFlow = packFilterDTO(packFilterDTO);

        pathAnalyzeDTO.setBeginFlow(beginFlow);

        return pathAnalyzeDTO;
    }

    /**
     * ??????IP??????????????????????????????UUID
     *
     * @param ipAddress ip??????
     * @return ??????UUID???????????????????????????????????????1????????????null
     */
    @Override
    public String getNodeUuid(String ipAddress) {
        logger.info(String.format("??????[%s]??????????????????UUID....", ipAddress));
        List<String> nodeUuidList = getSubnetUuidList(ipAddress);

        if (nodeUuidList == null) {
            logger.info("????????????????????????????????????");
            return null;
        }

        int dstNodeUuidSize = nodeUuidList.size();
        if (dstNodeUuidSize != 1) {

            //????????????log????????????   start
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("IP?????????????????????UUID????????????1??????????????????uuid?????????(%d)???{", dstNodeUuidSize));
            for (String dstNodeUuid : nodeUuidList) {
                sb.append(dstNodeUuid);
                sb.append(",");
            }
            sb.append("}");
            logger.info(sb.toString());
            //????????????log????????????   end

            return null;
        }

        return nodeUuidList.get(0);
    }

    /**
     * ?????????????????????UUID
     *
     * @param subnetUuid
     * @return
     */
    @Override
    public String getZoneUuid(String subnetUuid) {
        FolderUuidRO srcUuidRO = whaleFolderClient.getWhaleFolder(subnetUuid);
        List<FolderUuidDataRO> data = srcUuidRO.getData();
        if (data == null || data.size() == 0) {
            logger.debug("No zone for the subnet(" + subnetUuid + ")");
            return null;
        }
        return data.get(0).getId();
    }

    /**
     * ??????IP????????????
     * ?????????
     *
     * @param ipAddress ????????????
     * @return ????????????UUID??????
     */
    @Override
    public List<String> getNodeUuidList(String ipAddress) {
        logger.info("??????????????????");
        List<String> uuidList = new ArrayList<>();
        String startIp = IpUtils.getStartIpFromIpAddress(ipAddress);
        String endIp = IpUtils.getEndIpFromIpAddress(ipAddress);

        List<String> startIpNodeUuidList = getSubnetUuidList(ipAddress);
        List<String> endIpNodeUuidList = getSubnetUuidList(endIp);

        //?????????????????????????????????????????????????????????????????????
        if (startIpNodeUuidList == null) {
            logger.info("?????????????????????uuid??????????????????(null)");
            return uuidList;
        }

        if (endIpNodeUuidList == null) {
            logger.info("????????????????????????uuid??????????????????(null)");
            return uuidList;
        }

        if (startIpNodeUuidList.size() == 0) {
            logger.info("???????????????????????????");
            return uuidList;
        }

        if (endIpNodeUuidList.size() == 0) {
            logger.info("?????????????????????????????????");
            return uuidList;
        }

        //??????????????????????????????????????????????????????????????????
        for (String startIpNodeUuid : startIpNodeUuidList) {
            for (String endIpNodeUuid : endIpNodeUuidList) {
                if (startIpNodeUuid.equals(endIpNodeUuid)) {
                    logger.info("?????????uuid: " + startIpNodeUuid);
                    uuidList.add(startIpNodeUuid);
                }
            }
        }

        return uuidList;
    }

    @Override
    public List<SubnetRO> getSubnetROList(List<String> subnetUuidList) {
        List<SubnetRO> list = new ArrayList<>();
        for (String subnetUuid : subnetUuidList) {
            SubnetListRO subnetListRO = whaleSubnetObjectClient.getSubnetObject(subnetUuid);
            List<SubnetRO> data = subnetListRO.getData();
            if (data.size() > 0) {
                list.add(data.get(0));
            }
        }
        return list;
    }

    @Override
    @TimeCounter
    public String getHuaweiRuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("security-policy")) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    @TimeCounter
    public String getJuniperSrxRuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("Firewall Policy")) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    @TimeCounter
    public String getH3Cv7RuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("Firewall Policy")) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    public String getVenusVSOSRuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("Policy") || filterlistRO.getName().equals("Firewall Policy")) {
                logger.info(String.format("??????????????????(%s)????????????UUID???:%s", deviceUuid, filterlistRO.getUuid()));
                return filterlistRO.getUuid();
            } else {
                logger.info(String.format("??????????????????(%s)??????????????????:%s", deviceUuid, filterlistRO.getName()));
            }
        }
        logger.info(String.format("??????????????????(%s)??????????????????Policy????????????UUID", deviceUuid));
        return null;
    }

    @Override
    public List<String> getMultiAddressNodeUuid(String ipAddress) {
        String startIp = IpUtils.getStartIpFromIpAddresses(ipAddress);

        return getNodeUuidList(startIp);
    }

    @Override
    @TimeCounter
    public String getDpTechRuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("Firewall Policy")) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    @TimeCounter
    public String getFortinetTechRuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("Firewall Policy")) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    @TimeCounter
    public String getAbtnetworksTechRuleListUuid(String deviceUuid, PolicyEnum policyType) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        if(PolicyEnum.SECURITY.equals(policyType)){
            for (DeviceFilterlistRO filterlistRO : list) {
                if (filterlistRO.getName().equals("policy")) {
                    return filterlistRO.getUuid();
                }
            }
        }else{
            for (DeviceFilterlistRO filterlistRO : list) {
                if (filterlistRO.getName().equals("__NAT_LIST1")) {
                    return filterlistRO.getUuid();
                }
            }
        }
        return null;
    }

    @Override
    @TimeCounter
    public String getAbtnetworksTechRule6ListUuid(String deviceUuid, PolicyEnum policyType) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        if(PolicyEnum.SECURITY.equals(policyType)){
            for (DeviceFilterlistRO filterlistRO : list) {
                if (filterlistRO.getName().equals("policy6")) {
                    return filterlistRO.getUuid();
                }
            }
        }else{
            for (DeviceFilterlistRO filterlistRO : list) {
                if (filterlistRO.getName().equals("__NAT_LIST1")) {
                    return filterlistRO.getUuid();
                }
            }
        }
        return null;
    }

    @Override
    public String getTopsecRuleListUuid(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals("Firewall Rules")) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    @TimeCounter
    public String getCiscoRuleListUuid(String deviceUuid, Integer aclDirection) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        String direction = "Inbound";
        if (aclDirection == AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION) {
            direction = "Outbound";
        }
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().trim().startsWith(direction)) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    public int checkSubnet(String srcIp, String dstIp) {
        List<String> srcSubnetUuidList = getMultiAddressNodeUuid(srcIp);
        List<String> dstSubnetUuidList = getMultiAddressNodeUuid(dstIp);

        if (srcSubnetUuidList.size() == 0 || dstSubnetUuidList.size() == 0) {
            return ReturnCode.SUBNET_UUID_IS_EMPTY;
        }

        String srcSubnetUuid = srcSubnetUuidList.get(0);
        String dstSubnetUuid = dstSubnetUuidList.get(0);
        if (srcSubnetUuid.equals(dstSubnetUuid)) {
            return ReturnCode.SUBNET_CANNOT_BE_THE_SAME;
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public ZoneDataRO getZoneData(ZoneRO zoneRO, String interfaceName) {
        String zoneName = "";
        if (zoneRO == null) {
            return null;
        }

        if (zoneRO.getData() == null) {
            return null;
        }

        List<ZoneDataRO> zoneList = zoneRO.getData();

        if (zoneList.size() == 0) {
            return null;
        }

        for (ZoneDataRO zoneDataRO : zoneList) {
            List<String> interfaceNameList = zoneDataRO.getInterfaceNames();

            if (interfaceNameList == null || interfaceNameList.size() == 0) {
                continue;
            }

            //?????? ??????
            boolean isImplicit = false;
            if (zoneDataRO.getIsImplicit() != null) {
                isImplicit = zoneDataRO.getIsImplicit();
            }

            for (String name : interfaceNameList) {
                if (name.equals(interfaceName) && !isImplicit) {
                    return zoneDataRO;
                }
            }
        }
        return null;
    }

    @Override
    public ObjectDetailRO queryIpByName(String deviceUuid, String name) {
        return ipServiceNameRefClient.queryIpByName(deviceUuid, name);
    }

    @Override
    public String getRuleListUuidByRef(String deviceUuid, String ref) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        for (DeviceFilterlistRO filterlistRO : list) {
            if (filterlistRO.getName().equals(ref)) {
                return filterlistRO.getUuid();
            }
        }
        return null;
    }

    @Override
    public FilterListsRO getFilterListsByUuid(String uuid) {
        return whaleFilterListClient.getFilterListsByUuid(uuid);
    }

    @Override
    public List<String> getInterfaces(String deviceUuid, String zone) {
        ZoneRO zoneRO = getDeviceZoneVO(deviceUuid);
        if (zoneRO == null) {
            return null;
        }
        List<ZoneDataRO> zoneDataROList = zoneRO.getData();
        if (zoneDataROList != null && zoneDataROList.size() > 0) {
            for (ZoneDataRO zoneDataRo : zoneDataROList) {
                if (zoneDataRo.getName().equals(zone)) {
                    return zoneDataRo.getInterfaceNames();
                }
            }
        }
        return null;
    }

    @Override
    public RuleCheckResultRO getRuleCheckResult(RuleCheckPolicyRO ruleCheckPolicyRO, String deviceUuid, String ruleListUuid) {
        return whaleRuleCheckClient.getRuleCheckResult(ruleCheckPolicyRO, deviceUuid, ruleListUuid);
    }

    @Override
    public List<String> getDetailPathSubnet(DetailPathSubnetDTO detailPathSubnetDTO) throws Exception {
        logger.debug("????????????????????????..." + JSONObject.toJSONString(detailPathSubnetDTO));
        RealSubnetRO realSubnetRO = whalePathAnalyzeClient.getDetailPathSubnet(detailPathSubnetDTO);

        if (realSubnetRO == null || realSubnetRO.getData() == null || realSubnetRO.getData().size() == 0) {
            logger.debug("??????????????????...realSubnetRO:" + JSONObject.toJSONString(realSubnetRO));
            return null;
        }

        List<SubnetRO> subnetROList = realSubnetRO.getData().get(0).getSubnets();
        if (subnetROList == null || subnetROList.size() == 0) {
            logger.debug("????????????????????????...realSubnetRO:" + JSONObject.toJSONString(realSubnetRO));
            return null;
        }
        List<String> nodeUuidList = new ArrayList<>();
        for (SubnetRO subnetRO : subnetROList) {
            String subnetUuid = subnetRO.getUuid();
            nodeUuidList.add(subnetUuid);
        }

        return nodeUuidList;
    }

    @Override
    public List<String> getDetailPathSubnetList(String srcNodeUuid, String srcIpListString, String dstIpListString, List<ServiceDTO> serviceList, String whatIfCaseUuid) throws Exception {
        logger.debug(String.format("????????????????????????...(%s, %s, %s, %s, %s)", srcNodeUuid, srcIpListString, dstIpListString, JSONObject.toJSONString(serviceList), whatIfCaseUuid));
        List<String> uuidList = new ArrayList<>();

        String[] srcIpStrings = srcIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<String> srcIpList = new ArrayList<>();
        for (String srcIpString : srcIpStrings) {
            srcIpList.add(srcIpString);
        }

        String[] dstIpStrings = dstIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<String> dstIpList = new ArrayList<>();
        for (String dstIpString : dstIpStrings) {
            dstIpList.add(dstIpString);
        }

        List<PathFlowRO> pathFlowList = packPathFlowRO(srcIpList, dstIpList, serviceList);
        DetailPathSubnetDTO detailPathSubnetDTO = new DetailPathSubnetDTO();
        detailPathSubnetDTO.setSrcNodeUuid(srcNodeUuid);
        if (!AliStringUtils.isEmpty(whatIfCaseUuid)) {
            detailPathSubnetDTO.setWhatifCaseUuid(whatIfCaseUuid);
        }
        detailPathSubnetDTO.setBeginFlow(pathFlowList);
        List<String> subnetUuidList = getDetailPathSubnet(detailPathSubnetDTO);

        //?????????????????????????????????????????????????????????????????????
        if (subnetUuidList == null) {
            logger.debug("??????????????????????????????uuid??????????????????...");
            return uuidList;
        }

        return subnetUuidList;
    }

    @Override
    public List<String> getSubnetDeviceUuidList(String subnetUuid) {
//        logger.info(String.format("????????????(%s)?????????????????????...", subnetUuid));
        SubnetLinkedDeviceRO subnetLinkedDevice = whaleSubnetObjectClient.getSubnetLinkedDevice(subnetUuid);
        if (subnetLinkedDevice == null) {
            logger.info("??????????????????????????????...");
            return null;
        }
        List<DeviceSummaryDataRO> deviceSummaryDataROList = subnetLinkedDevice.getData();
        if (deviceSummaryDataROList == null || deviceSummaryDataROList.size() == 0) {
            logger.info("??????????????????????????????????????????...SubnetLinkedDeviceRO:" + JSONObject.toJSONString(subnetLinkedDevice));
            return null;
        }
        List<String> deviceUuidList = new ArrayList<>();
        for (DeviceSummaryDataRO deviceSummaryDataRO : deviceSummaryDataROList) {
            String deviceUuid = deviceSummaryDataRO.getUuid();
            deviceUuidList.add(deviceUuid);
        }
        return deviceUuidList;
    }

    @Override
    public String getSubnetDeviceList(String subnetUuid) {
        String devices = new String();
        if (AliStringUtils.isEmpty(subnetUuid)) {
            logger.info("??????uuid???????????????????????????");
            return devices;
        }

        List<String> deviceUuidList = getSubnetDeviceUuidList(subnetUuid);
        if (deviceUuidList == null) {
            logger.error("????????????????????????uuidList??????");
            return devices;
        }

        StringBuilder sb = new StringBuilder();
        for (String deviceUuid : deviceUuidList) {
            DeviceRO deviceRO = getDeviceByUuid(deviceUuid);
            if (deviceRO == null) {
                logger.error(String.format("????????????(%s)????????????...", deviceUuid));
                continue;
            }
            List<DeviceDataRO> deviceDataROList = deviceRO.getData();
            if (deviceDataROList == null || deviceDataROList.size() == 0) {
                logger.error(String.format("????????????(%s)??????????????????...", deviceUuid));
                continue;
            }
            DeviceDataRO deviceDataRO = deviceDataROList.get(0);
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
            sb.append(deviceDataRO.getName());
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }

        return sb.toString();
    }

    @Override
    public String getSubnetStringByUuid(String uuid) {
        SubnetListRO subnetListRO = whaleSubnetObjectClient.getSubnetObject(uuid);
        List<SubnetRO> data = subnetListRO.getData();
        SubnetRO newSubnetRO = null;
        if (data != null && data.size() > 0) {
            newSubnetRO = data.get(0);
        } else {
            logger.warn(String.format("??????UUID(%s)???????????????????????????", uuid));
            return "";
        }
        String ipType = newSubnetRO.getIpType();
        StringBuffer stringBuffer = new StringBuffer();
        if (StringUtils.isNotBlank(ipType) && POLICY_STR_NETWORK_TYPE_IP4.equalsIgnoreCase(ipType)) {
            stringBuffer.append(newSubnetRO.getIp4BaseAddress()).append("/").append(newSubnetRO.getIp4MaskLength());
        } else {
            stringBuffer.append(newSubnetRO.getIp6BaseAddress()).append("/").append(newSubnetRO.getIp6MaskLength());
        }
        return stringBuffer.toString();
    }


    @Override
    public DeviceDetailRunVO parseDeviceDetail(DeviceDetailRO deviceDetailRO) {
        return whalePathAnalyzeClient.parseDetailRunRO(deviceDetailRO);
    }

    @Override
    public List<PathFlowRO> packPathFlowRO(List<String> srcIpList, List<String> dstIpList, List<ServiceDTO> serviceList) {
        List<PathFlowRO> pathFlowList = new ArrayList<>();


        //???????????????????????????????????????????????????????????????????????????filter?????????????????????????????????????????????
        if (serviceList != null) {
            for (ServiceDTO service : serviceList) {
                //???????????????
                List<SrcDstStringDTO> srcAddressList = getSrcDstStringDTO(srcIpList);
                List<SrcDstStringDTO> dstAddressList = getSrcDstStringDTO(dstIpList);
                PathFlowRO filter = new PathFlowRO();
                String protocolString = service.getProtocol();
                if (protocolString == null) {
                    logger.error("??????????????????,??????????????????..." + JSONObject.toJSONString(service));
                    continue;
                }
                List<SrcDstIntegerDTO> protocolList = getSrcDstIntegerDTOList(protocolString);
                filter.setProtocols(protocolList);

                String srcPortString = service.getSrcPorts();
                if (srcPortString != null && !srcPortString.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    List<SrcDstIntegerDTO> srcPortList = getSrcDstIntegerDTOList(srcPortString);
                    filter.setSrcPorts(srcPortList);
                }

                String dstPortString = service.getDstPorts();
                if (dstPortString != null && !dstPortString.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    List<SrcDstIntegerDTO> dstPortList = getSrcDstIntegerDTOList(dstPortString);
                    filter.setDstPorts(dstPortList);
                }

                filter.setIp4SrcAddresses(srcAddressList);
                filter.setIp4DstAddresses(dstAddressList);

                pathFlowList.add(filter);
            }
        } else {
            PathFlowRO filter = new PathFlowRO();
            //???????????????
            List<SrcDstStringDTO> srcAddressList = getSrcDstStringDTO(srcIpList);
            List<SrcDstStringDTO> dstAddressList = getSrcDstStringDTO(dstIpList);
            filter.setIp4SrcAddresses(srcAddressList);
            filter.setIp4DstAddresses(dstAddressList);
            pathFlowList.add(filter);
        }

        return pathFlowList;
    }

    @Override
    public List<FilterDTO> packFilterDTO(PackFilterDTO packFilterDTO) {
        List<FilterDTO> filterList = new ArrayList<>();
        List<ServiceDTO> serviceList = packFilterDTO.getServiceList();
        //???????????????
        Integer ipType = packFilterDTO.getIpType();
        List<SrcDstStringDTO> srcAddressList = getSrcDstStringDTO(packFilterDTO.getSrcIpList(), ipType);
        List<SrcDstStringDTO> dstAddressList = getSrcDstStringDTO(packFilterDTO.getDstIpList(), ipType);
        //???????????????????????????????????????????????????????????????????????????filter?????????????????????????????????????????????
        if (CollectionUtils.isNotEmpty(serviceList)) {

            for (ServiceDTO service : serviceList) {

                FilterDTO filter = new FilterDTO();
                String protocolString = service.getProtocol();
                if (StringUtils.isBlank(protocolString)) {
                    logger.error("??????????????????,??????????????????..." + JSONObject.toJSONString(service));
                    continue;
                }
                List<SrcDstIntegerDTO> protocolList = getSrcDstIntegerDTOList(protocolString);
                filter.setProtocols(protocolList);

                String srcPortString = service.getSrcPorts();
                if (srcPortString != null && !srcPortString.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    List<SrcDstIntegerDTO> srcPortList = getSrcDstIntegerDTOList(srcPortString);
                    filter.setSrcPorts(srcPortList);
                }

                String dstPortString = service.getDstPorts();
                if (dstPortString != null && !dstPortString.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    List<SrcDstIntegerDTO> dstPortList = getSrcDstIntegerDTOList(dstPortString);
                    filter.setDstPorts(dstPortList);
                }
                setFilterIp4Or6(ipType, filter, srcAddressList, dstAddressList);
                filterList.add(filter);
            }
        } else {
            FilterDTO filter = new FilterDTO();
            //???????????????
            setFilterIp4Or6(ipType, filter, srcAddressList, dstAddressList);
            filterList.add(filter);
        }

        return filterList;
    }

    /**
     * ????????????????????????filter??????
     *
     * @param ipType
     * @param filter
     * @param srcAddressList
     * @param dstAddressList
     */
    private void setFilterIp4Or6(Integer ipType, FilterDTO filter, List<SrcDstStringDTO> srcAddressList, List<SrcDstStringDTO> dstAddressList) {
        if (IPV4.getCode().equals(ipType)) {
            filter.setIpType(PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4);
            filter.setIp4SrcAddresses(srcAddressList);
            filter.setIp4DstAddresses(dstAddressList);
        } else if (IPV6.getCode().equals(ipType)) {
            filter.setIpType(PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6);
            filter.setIp6SrcAddresses(srcAddressList);
            filter.setIp6DstAddresses(dstAddressList);
        } else {
            //????????????
        }
    }

    @Override
    public List<SrcDstStringDTO> getSrcDstStringDTO(String ipListString) {
        if (AliStringUtils.isEmpty(ipListString)) {
            return null;
        }
        String[] ipStrings = ipListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<String> ipList = new ArrayList<>();
        for (String ipString : ipStrings) {
            ipList.add(ipString);
        }
        return getSrcDstStringDTO(ipList);
    }

    @Override
    public List<SrcDstStringDTO> getSrcDstStringDTO(List<String> ipStringList) {
        if (ipStringList == null) {
            return null;
        }
        List<SrcDstStringDTO> list = new ArrayList<>();
        for (String ipString : ipStringList) {
            SrcDstStringDTO dto = new SrcDstStringDTO();
            dto.setStart(IpUtils.getStartIpFromIpAddress(ipString));
            dto.setEnd(IpUtils.getEndIpFromIpAddress(ipString));
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<SrcDstStringDTO> getSrcDstStringDTO(List<String> ipStringList, Integer ipType) {
        if (CollectionUtils.isEmpty(ipStringList)) {
            return null;
        }
        List<SrcDstStringDTO> list = new ArrayList<>();
        if (IPV4.getCode().equals(ipType)) {
            for (String ipString : ipStringList) {
                SrcDstStringDTO dto = new SrcDstStringDTO();
                dto.setStart(IpUtils.getStartIpFromIpAddress(ipString));
                dto.setEnd(IpUtils.getEndIpFromIpAddress(ipString));
                list.add(dto);
            }
        } else if (IPV6.getCode().equals(ipType)) {
            for (String ipString : ipStringList) {
                SrcDstStringDTO dto = new SrcDstStringDTO();
                CommonRangeStringDTO startEndIpFromIpv6Address = IP6Utils.getStartEndIpFromIpv6Address(ipString);
                dto.setStart(startEndIpFromIpv6Address.getStart());
                dto.setEnd(startEndIpFromIpv6Address.getEnd());
                list.add(dto);
            }
        } else {
            //????????????
        }
        return list;
    }

    @Override
    public List<SrcDstIntegerDTO> getSrcDstIntegerDTOList(String portListString) {
        if (AliStringUtils.isEmpty(portListString)) {
            return null;
        }
        String[] portStrings = portListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<String> portList = new ArrayList<>();
        for (String portString : portStrings) {
            portList.add(portString);
        }
        return getSrcDstIntegerDTOList(portList);
    }

    @Override
    public List<SrcDstIntegerDTO> getSrcDstIntegerDTOList(List<String> portStringList) {
        List<SrcDstIntegerDTO> list = new ArrayList<>();

        for (String portString : portStringList) {
            SrcDstIntegerDTO dto = new SrcDstIntegerDTO();
            try {
                String[] ports = portString.split(PolicyConstants.VALUE_RANGE_SEPERATOR);
                if (ports.length == 2) {
                    int start = Integer.parseInt(ports[0]);
                    int end = Integer.parseInt(ports[1]);
                    dto.setStart(start);
                    dto.setEnd(end);
                    list.add(dto);
                } else if (ports.length == 1) {
                    int start = Integer.parseInt(ports[0]);
                    dto.setStart(start);
                    dto.setEnd(start);
                    list.add(dto);
                } else {
                    logger.error(String.format("??????(%s)????????????...", ports));
                    continue;
                }
            } catch (Exception e) {
                logger.info("????????????????????????");
            }
        }

        return list;
    }

    @Override
    public SrcDstIntegerDTO getSrcDstIntegerDTO(String port) {
        SrcDstIntegerDTO dto = new SrcDstIntegerDTO();
        try {
            String[] ports = port.split(PolicyConstants.VALUE_RANGE_SEPERATOR);
            if (ports.length == 2) {
                int start = Integer.parseInt(ports[0]);
                int end = Integer.parseInt(ports[1]);
                dto.setStart(start);
                dto.setEnd(end);
            } else if (ports.length == 1) {
                int start = Integer.parseInt(ports[0]);
                dto.setStart(start);
                dto.setEnd(start);
            } else {
                logger.error(String.format("??????(%s)????????????...", ports));
            }
        } catch (Exception e) {
            logger.info("????????????????????????");
        }
        return dto;
    }

    @Override
    public ExistAddressObjectDTO getCurrentAddressObjectName(String ipAddress, DeviceDTO deviceDTO, AddressPropertyEnum addressPropertyEnum, PolicyEnum policyType, SettingDTO settingDTO, PolicyDTO policyDTO) {
        if (AliStringUtils.isEmpty(ipAddress)) {
            return null;
        }
        String[] ipAddresses = ipAddress.split(PolicyConstants.ADDRESS_SEPERATOR);
        DeviceModelNumberEnum modelNumber = deviceDTO.getModelNumber();
        List<CommonRangeStringDTO> addressList = new ArrayList<>();
        boolean isIPv6 = ipAddress.contains(":");

        for (String ip : ipAddresses) {
            CommonRangeStringDTO dto = new CommonRangeStringDTO();
            String start = null;
            String end = null;
            try {
                String[] startEndIpStringArray = QuintupleUtils.ipv46toIpStartEnd(ip);
                start = startEndIpStringArray[0];
                end = startEndIpStringArray[1];
            } catch (UnknownHostException e) {
                logger.error(String.format("??????IP???????????????????????????[{}]", ip), e);
            }
            dto.setStart(start);
            dto.setEnd(end);
            addressList.add(dto);
        }
        SearchAddressDTO addressDTO = new SearchAddressDTO();

        addressDTO.setDeviceUuid(deviceDTO.getDeviceUuid());
        if (isIPv6) {
            addressDTO.setSearchType("IP6");
            addressDTO.setIp6RangeOp(SearchRangeOpEnum.EQUAL.getCode());
            addressDTO.setIp6AddressRanges(addressList);
        } else {
            addressDTO.setSearchType("IP");
            addressDTO.setRangeOp(SearchRangeOpEnum.EQUAL.getCode());
            addressDTO.setAddressRanges(addressList);
        }
        switch (modelNumber){
            case SRX:
            case SRX_NoCli:
            case H3CV7:
                if(POST_SRC.equals(addressPropertyEnum)){
                    addressDTO.setNetworkType(SRC_POOL);
                }else if(POST_DST.equals(addressPropertyEnum)){
                    addressDTO.setNetworkType(DST_POOL);
                }
                break;
            case F5:
                if(POST_SRC.equals(addressPropertyEnum)){
                    addressDTO.setNetworkType(POOL_GROUP);
                }
                break;
            case DPTECHR004:
                if(POST_SRC.equals(addressPropertyEnum)){
                    addressDTO.setNetworkType(POOL);
                }
                break;
            default:
                break;
        }
        logger.info("??????????????????????????????{}",JSONObject.toJSONString(addressDTO));
        ResultRO<List<NetWorkGroupObjectRO>> result = whaleDeviceObjectClient.searchAddress(addressDTO,null);
        logger.info("??????????????????????????????{}",JSONObject.toJSONString(result));
        if (ObjectUtils.isNotEmpty(result)  && result.getSuccess() == true) {
            List<NetWorkGroupObjectRO> netWorkGroupObjectROS = result.getData();
            ExistAddressObjectDTO existAddressObjectDTO = getFinalObjectAddress(netWorkGroupObjectROS, deviceDTO, addressPropertyEnum, policyType, settingDTO, policyDTO);
            if (existAddressObjectDTO != null && StringUtils.isEmpty(existAddressObjectDTO.getExistName())) {
                return null;
            }
            return existAddressObjectDTO;
        } else {
            return null;
        }
    }

    /**
     * ?????????????????????????????????
     * @param netWorkGroupObjectROS
     * @return
     */
    private ExistAddressObjectDTO getFinalObjectAddress(List<NetWorkGroupObjectRO> netWorkGroupObjectROS, DeviceDTO deviceDTO, AddressPropertyEnum addressPropertyEnum, PolicyEnum policyType, SettingDTO settingDTO, PolicyDTO policyDTO) {
        if (CollectionUtils.isNotEmpty(netWorkGroupObjectROS)) {
            ExistAddressObjectDTO existAddressObjectDTO = new ExistAddressObjectDTO();
            NetWorkGroupObjectRO netWorkGroupObjectRO =  netWorkGroupObjectROS.get(0);
            String deviceObjectType = netWorkGroupObjectRO.getDeviceObjectType();
            DeviceNetworkTypeEnum deviceNetworkTypeEnum = netWorkGroupObjectRO.getDeviceNetworkType();
            DeviceModelNumberEnum deviceModelNumberEnum = deviceDTO.getModelNumber();
            existAddressObjectDTO.setDeviceNetworkTypeEnum(deviceNetworkTypeEnum);
            existAddressObjectDTO.setDeviceObjectType(deviceObjectType);
            String realName = netWorkGroupObjectRO.getRealName();
            String name = netWorkGroupObjectRO.getName();
            String finalName = "";
            if(StringUtils.isNotBlank(realName)){
                finalName = realName;

            }else if (StringUtils.isNotBlank(name)){
                finalName = name;
            }else{

            }
            // ??????????????????????????????????????????????????????
            switch (deviceModelNumberEnum) {
                case SRX:
                case SRX_NoCli:
                    //1.?????????JuniperSRX??????????????????????????????????????????????????????????????????
                    if (settingDTO != null && policyDTO != null && settingDTO.getAddressType()) {
                        finalName = getSrxFinalAddressName(netWorkGroupObjectROS, addressPropertyEnum, policyDTO, finalName);
                    }
                    break;
                case FORTINET:
                case FORTINET_V5:
                case FORTINET_V5_2:
                    finalName = getFortinetFinalAddressName(netWorkGroupObjectROS, addressPropertyEnum, policyDTO, finalName);
                default:
                    break;
            }

            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            switch (deviceModelNumberEnum) {
                case USG6000:
                case USG6000_NO_TOP:
                case DPTECHR004:
                case F5:
                case FORTINET:
                case FORTINET_V5_2:
                    if(addressPropertyEnum.equals(POST_SRC)){
                        //????????????????????????????????????????????????
                        if(deviceNetworkTypeEnum != null ) {
                            //?????????????????????finalName
                        }else{
                            finalName = "";
                        }
                    }else{
                        //???????????????????????????????????????????????????????????????
                        if(deviceNetworkTypeEnum != null ){
                            finalName = "";
                        }
                    }
                    break;
                case SRX:
                case SRX_NoCli:
                case H3CV7:
                    if(addressPropertyEnum.equals(POST_SRC) || addressPropertyEnum.equals(POST_DST)){
                        //?????????????????????????????????????????????SRX??????????????????
                        if(deviceNetworkTypeEnum != null ) {
                            //?????????????????????finalName
                        }else{
                            finalName = "";
                        }
                    }else{
                        //???????????????????????????????????????????????????????????????
                        if(deviceNetworkTypeEnum != null ){
                            finalName = "";
                        }
                    }
                    break;
                case WESTONE:
                    if((addressPropertyEnum.equals(POST_SRC) && PolicyEnum.SNAT.equals(policyType))|| (addressPropertyEnum.equals(DST) && PolicyEnum.DNAT.equals(policyType))){
                        if(deviceNetworkTypeEnum != null ) {

                        }else{
                            return null;
                        }
                    }else {
                        if(deviceNetworkTypeEnum != null ){
                            return null;
                        }
                    }
                    break;
                default:
                    if(deviceNetworkTypeEnum != null ){
                        //???????????????????????????????????????
                        finalName = "";
                    }
                    break;
            }
            existAddressObjectDTO.setExistName(finalName);
            return existAddressObjectDTO;
        }
        return null;
    }

    /**
     * ??????srx????????????????????????
     * @param netWorkGroupObjectROS
     * @param addressPropertyEnum
     * @param policyDTO
     * @param finalName
     * @return
     */
    private String getSrxFinalAddressName(List<NetWorkGroupObjectRO> netWorkGroupObjectROS, AddressPropertyEnum addressPropertyEnum, PolicyDTO policyDTO, String finalName) {
        String zoneName = "";
        //2.???????????????????????????????????????????????????????????????????????????
        if (addressPropertyEnum.equals(SRC)) {
            zoneName = policyDTO.getSrcZone();
        } else if (addressPropertyEnum.equals(DST)) {
            zoneName = policyDTO.getDstZone();
        }
        //??????????????????????????????any??????????????????????????????
        if (StringUtils.isEmpty(zoneName)) {
            zoneName = "";
        }
        boolean exist = false;
        //3.netWorkGroupObjectROS???????????????????????????????????????????????????trust???????????????????????????
        for (NetWorkGroupObjectRO workGroupObjectRO : netWorkGroupObjectROS) {
            String zone = workGroupObjectRO.getZoneName();
            String name1 = workGroupObjectRO.getName();

            if (StringUtils.isEmpty(zoneName) && StringUtils.isNotEmpty(name1) && StringUtils.isEmpty(zone)){
                exist = true;
                finalName = name1;
                break;
            }

            if (zoneName.equalsIgnoreCase(zone) && StringUtils.isNotEmpty(name1) && StringUtils.isNotEmpty(zoneName)){
                exist = true;
                finalName = name1;
                break;
            }

        }
        if (!exist) {
            return null;
        }
        return finalName;
    }

    /**
     * ????????????????????????????????????
     * @param netWorkGroupObjectROS
     * @param addressPropertyEnum
     * @param policyDTO
     * @param finalName
     * @return
     */
    private String getFortinetFinalAddressName(List<NetWorkGroupObjectRO> netWorkGroupObjectROS, AddressPropertyEnum addressPropertyEnum, PolicyDTO policyDTO, String finalName) {
        String zoneName = "";
        String interfaceName = "";
        //2.???????????????????????????????????????????????????????????????????????????
        if (addressPropertyEnum.equals(SRC)) {
            zoneName = policyDTO.getSrcZone();
            interfaceName = policyDTO.getSrcItf();
        } else if (addressPropertyEnum.equals(DST)) {
            zoneName = policyDTO.getDstZone();
            interfaceName = policyDTO.getDstItf();
        }

        boolean exist = false;
        boolean tempExist =false;
        //3.netWorkGroupObjectROS???????????????????????????????????????????????????trust???????????????????????????
        for (NetWorkGroupObjectRO workGroupObjectRO : netWorkGroupObjectROS) {
            String zone = workGroupObjectRO.getZoneName();
            String existInterfaceName = workGroupObjectRO.getInterfaceName();

            String reuseAddressName = workGroupObjectRO.getName();


            // 1.????????????????????????
            if(StringUtils.isNotBlank(zoneName) && StringUtils.isNotBlank(interfaceName)){
                if(StringUtils.isBlank(zone) && StringUtils.isBlank(existInterfaceName)){
                    if(!tempExist && !exist){
                        finalName = reuseAddressName;
                        tempExist = true;
                        exist = true;
                        continue;
                    }
                }

                if(zoneName.equalsIgnoreCase(zone)){
                    exist = true;
                    finalName = reuseAddressName;
                    break;
                }
            }


            // 2.???????????????????????????????????????????????????????????????????????????
            if (StringUtils.isBlank(zoneName) && StringUtils.isNotEmpty(reuseAddressName) && StringUtils.isBlank(zone)
                    && StringUtils.isBlank(interfaceName) && StringUtils.isBlank(existInterfaceName)) {
                exist = true;
                finalName = reuseAddressName;
                break;
            }


            // 3.????????????????????????????????????????????????????????????????????????any?????????????????????????????????????????????????????????????????????????????????(???????????? ???????????????????????????????????????????????????????????? ???????????????????????????????????????)
            if(StringUtils.isNotBlank(zoneName) && StringUtils.isBlank(zone) && StringUtils.isBlank(existInterfaceName) && StringUtils.isNotBlank(reuseAddressName)){
                if(!tempExist && !exist){
                    finalName = reuseAddressName;
                    tempExist = true;
                    exist = true;
                    continue;
                }
            }

            // 4.????????????????????????????????????????????????????????????)
            if (StringUtils.isNotBlank(zoneName) && zoneName.equalsIgnoreCase(zone) && StringUtils.isBlank(existInterfaceName)) {
                exist = true;
                finalName = reuseAddressName;
                break;
            }


            // 5.??????????????????????????????????????? ???????????? ????????? and ??????????????????????????????
            if(StringUtils.isNotBlank(interfaceName) && StringUtils.isBlank(zoneName) && StringUtils.isBlank(zone) &&
                    StringUtils.isBlank(existInterfaceName) && StringUtils.isNotBlank(reuseAddressName)){
                if(!tempExist && !exist){
                    finalName = reuseAddressName;
                    tempExist = true;
                    exist = true;
                    continue;
                }
            }

            // 6.??????????????????????????????????????? ???????????????????????????????????????????????????
            if (StringUtils.isNotBlank(interfaceName) && StringUtils.isBlank(zoneName) &&
                    interfaceName.equalsIgnoreCase(existInterfaceName)) {
                exist = true;
                finalName = reuseAddressName;
                break;
            }
        }
        if(!exist){
            return null;
        }
        return finalName;
    }

    @Override
    public String getCurrentServiceObjectName(List<ServiceDTO> serviceList, DeviceForExistObjDTO deviceDTO) {
        return getCurrentServiceObjectName(serviceList, deviceDTO, null);
    }

    @Override
    public String getCurrentServiceObjectName(List<ServiceDTO> serviceList, DeviceForExistObjDTO deviceDTO, Integer idleTimeout) {
        SearchServiceDTO serviceDTO = new SearchServiceDTO();
        List<ServiceConditionDTO> list = new ArrayList<>();
        String deviceUuid = deviceDTO.getDeviceUuid();

        for (ServiceDTO service : serviceList) {

            String dstPorts = service.getDstPorts();
            String protocolString = service.getProtocol();
            List<CommonRangeIntegerDTO> protocolRangeList = new ArrayList<>();
            if (AliStringUtils.isEmpty(protocolString) || protocolString.equalsIgnoreCase("any")
                    || protocolString.equalsIgnoreCase("0")) {
                CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
                rangeIntegerDTO.setStart(0);
                rangeIntegerDTO.setEnd(255);
                protocolRangeList.add(rangeIntegerDTO);
            } else if(StringUtils.isNumeric(protocolString)){
                CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
                rangeIntegerDTO.setStart(Integer.valueOf(protocolString));
                rangeIntegerDTO.setEnd(Integer.valueOf(protocolString));
                protocolRangeList.add(rangeIntegerDTO);
            } else {
                String protocolNum = protocolMapConfig.getStrMap().get(protocolString);
                if(StringUtils.isNotBlank(protocolNum)){
                    CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
                    rangeIntegerDTO.setStart(Integer.valueOf(protocolNum));
                    rangeIntegerDTO.setEnd(Integer.valueOf(protocolNum));
                    protocolRangeList.add(rangeIntegerDTO);
                }
            }
            if (!AliStringUtils.isEmpty(dstPorts) && !dstPorts.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String[] dstPortArray = dstPorts.split(PolicyConstants.ADDRESS_SEPERATOR);


                for (String dstPort : dstPortArray) {
                    List<CommonRangeIntegerDTO> dstPortRangeList = new ArrayList<>();
                    ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
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
                    serviceConditionDTO.setDstPorts(dstPortRangeList);
                    serviceConditionDTO.setProtocols(protocolRangeList);
                    list.add(serviceConditionDTO);
                }

            } else {
                ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
                serviceConditionDTO.setProtocols(protocolRangeList);
                list.add(serviceConditionDTO);
            }
            String srcPorts = service.getSrcPorts();
            if (!AliStringUtils.isEmpty(srcPorts) && !srcPorts.equals(PolicyConstants.POLICY_STR_VALUE_ANY) &&
                    !srcPorts.equals(PolicyConstants.POLICY_NUM_VALUE_ANY)) {
                String[] srcPortArray = srcPorts.split(PolicyConstants.ADDRESS_SEPERATOR);

                for (String srcPort : srcPortArray) {
                    List<CommonRangeIntegerDTO> srcPortRangeList = new ArrayList<>();
                    ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
                    CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();

                    if (PortUtils.isPortRange(srcPort)) {
                        String start = PortUtils.getStartPort(srcPort);
                        String end = PortUtils.getEndPort(srcPort);
                        rangeIntegerDTO.setStart(Integer.valueOf(start));
                        rangeIntegerDTO.setEnd(Integer.valueOf(end));
                    } else {
                        rangeIntegerDTO.setStart(Integer.valueOf(srcPort));
                        rangeIntegerDTO.setEnd(Integer.valueOf(srcPort));
                    }
                    srcPortRangeList.add(rangeIntegerDTO);
                    serviceConditionDTO.setSrcPorts(srcPortRangeList);
                    serviceConditionDTO.setProtocols(protocolRangeList);
                    list.add(serviceConditionDTO);
                }

            }else{
//                ??????????????????????????????????????????KSH-5525
//                DeviceModelNumberEnum deviceModelNumberEnum = deviceDTO.getModelNumber();
//                switch (deviceModelNumberEnum){
//                    case SRX:
//                    case SRX_NoCli:
//                        break;
//                    default:
//                        List<CommonRangeIntegerDTO> srcPortRangeList = new ArrayList<>();
//                        ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
//                        CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();
//                        rangeIntegerDTO.setStart(0);
//                        rangeIntegerDTO.setEnd(65535);
//                        srcPortRangeList.add(rangeIntegerDTO);
//                        serviceConditionDTO.setSrcPorts(srcPortRangeList);
//                        serviceConditionDTO.setProtocols(protocolRangeList);
//                        list.add(serviceConditionDTO);
//                        break;
//                }
            }

        }
        String name ;
        if(CollectionUtils.isNotEmpty(list)){
            serviceDTO.setServices(list);
            serviceDTO.setDeviceUuid(deviceUuid);
            serviceDTO.setSearchType("SERVICE");
            serviceDTO.setRangeOp("EQUAL");
            logger.debug("????????????????????????...?????????????????????" + JSONObject.toJSONString(serviceDTO));
            name = getFirstNameBySearchService(serviceDTO, deviceDTO, idleTimeout);
        }else{
            DeviceModelNumberEnum deviceModelNumberEnum = deviceDTO.getModelNumber();
            switch (deviceModelNumberEnum){
                default:
                    name = "";
                    break;
            }

        }



        return name;
    }

    @Override
    public String getCurrentServiceObjectNameForNsfocus(List<ServiceDTO> serviceList, DeviceForExistObjDTO deviceDTO, Integer idleTimeout) {
        SearchServiceDTO serviceDTO = new SearchServiceDTO();
        List<ServiceConditionDTO> list = new ArrayList<>();
        String deviceUuid = deviceDTO.getDeviceUuid();

        for (ServiceDTO service : serviceList) {

            String dstPorts = service.getDstPorts();
            String protocolString = service.getProtocol();
            List<CommonRangeIntegerDTO> protocolRangeList = new ArrayList<>();
            if (AliStringUtils.isEmpty(protocolString) || protocolString.equalsIgnoreCase("any")
                || protocolString.equalsIgnoreCase("0")) {
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
            if (!AliStringUtils.isEmpty(dstPorts) && !dstPorts.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String[] dstPortArray = dstPorts.split(PolicyConstants.ADDRESS_SEPERATOR);

                List<CommonRangeIntegerDTO> dstPortRangeList = new ArrayList<>();
                ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();

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
                serviceConditionDTO.setProtocols(protocolRangeList);
                list.add(serviceConditionDTO);
            } else {
                ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
                serviceConditionDTO.setProtocols(protocolRangeList);
                list.add(serviceConditionDTO);
            }
            String srcPorts = service.getSrcPorts();
            if (!AliStringUtils.isEmpty(srcPorts) && !srcPorts.equals(PolicyConstants.POLICY_STR_VALUE_ANY)
                    && !srcPorts.equals(PolicyConstants.POLICY_NUM_VALUE_ANY)) {
                String[] srcPortArray = srcPorts.split(PolicyConstants.ADDRESS_SEPERATOR);

                List<CommonRangeIntegerDTO> srcPortRangeList = new ArrayList<>();
                ServiceConditionDTO serviceConditionDTO = new ServiceConditionDTO();
                for (String srcPort : srcPortArray) {
                    CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO();

                    if (PortUtils.isPortRange(srcPort)) {
                        String start = PortUtils.getStartPort(srcPort);
                        String end = PortUtils.getEndPort(srcPort);
                        rangeIntegerDTO.setStart(Integer.valueOf(start));
                        rangeIntegerDTO.setEnd(Integer.valueOf(end));
                    } else {
                        rangeIntegerDTO.setStart(Integer.valueOf(srcPort));
                        rangeIntegerDTO.setEnd(Integer.valueOf(srcPort));
                    }
                    srcPortRangeList.add(rangeIntegerDTO);
                }
                serviceConditionDTO.setSrcPorts(srcPortRangeList);
                serviceConditionDTO.setProtocols(protocolRangeList);
                list.add(serviceConditionDTO);

            }

        }
        String name;
        if (CollectionUtils.isNotEmpty(list)) {
            serviceDTO.setServices(list);
            serviceDTO.setDeviceUuid(deviceUuid);
            serviceDTO.setSearchType("SERVICE");
            serviceDTO.setRangeOp("EQUAL");
            logger.debug("????????????????????????...?????????????????????" + JSONObject.toJSONString(serviceDTO));
            name = getFirstNameBySearchService(serviceDTO, deviceDTO, idleTimeout);
        } else {
            DeviceModelNumberEnum deviceModelNumberEnum = deviceDTO.getModelNumber();
            switch (deviceModelNumberEnum) {
                default:
                    name = "";
                    break;
            }

        }
        return name;
    }

    @Override
    public String getInterfacePolicyName(String deviceUuid, String interfaceAlias) {
        if (AliStringUtils.isEmpty(interfaceAlias)) {
            logger.info("?????????????????????");
            return null;
        }
        DeviceRO device = getDeviceByUuid(deviceUuid);
        if (device == null) {
            logger.info("????????????device == null");
            return null;
        }
        if (device.getData() == null || device.getData().size() == 0) {
            logger.info("????????????device.getData() == null || device.getData().size() == 0..." + JSONObject.toJSONString(device));
            return null;
        }
        DeviceDataRO deviceData = device.getData().get(0);
        List<InterfacesRO> interfaceList = deviceData.getInterfaces();
        if (interfaceList == null) {
            logger.info("??????????????????????????????\n" + JSONObject.toJSONString(device));
            return null;
        }

        List<DeviceInterfaceRO> deviceInterfaceList = deviceData.getDeviceInterfaces();
        logger.info("??????????????????????????????\n" + JSONObject.toJSONString(deviceInterfaceList));

        List<String> list = new ArrayList<>();
        for (DeviceInterfaceRO deviceInterfaceRO : deviceInterfaceList) {
            if (deviceInterfaceRO.getAlias().equals(interfaceAlias)) {
                list = deviceInterfaceRO.getInboundRuleListRefs();
                break;
            }
        }

        if (list == null || list.size() == 0) {
            logger.info(String.format("??????(%s)????????????????????????", interfaceAlias));
            return null;
        } else if (list.size() > 1) {
            logger.info(String.format("??????(%s)??????????????????????????????1...???????????????%s", interfaceAlias, JSONObject.toJSONString(list)));
        }

        return list.get(0);
    }

    @Override
    public String getInterfacePolicyName(String deviceUuid, String interfaceAlias, Boolean isInbound) {
        logger.info(String.format("????????????(%s)????????????(%s)??????%s?????????????????????????????????...", deviceUuid, interfaceAlias, isInbound ? "In" : "Out"));
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        String ruleListUuid = null;
        for (DeviceFilterlistRO deviceFilterlistRO : list) {
            if (isInbound) {
                if (deviceFilterlistRO.getName().equals("Inbound Filter Rules")) {
                    ruleListUuid = deviceFilterlistRO.getUuid();
                    break;
                }
            } else {
                if (deviceFilterlistRO.getName().equals("Outbound Filter Rules")) {
                    ruleListUuid = deviceFilterlistRO.getUuid();
                    break;
                }
            }
        }
        if (ruleListUuid == null) {
            logger.error(String.format("%s??????ruleListUuid??????...", isInbound ? "Inbound" : "Outbound"));
            return null;
        }

        FilterRuleListSearchDTO searchDTO = new FilterRuleListSearchDTO();
        searchDTO.setDeviceUuid(deviceUuid);
        searchDTO.setFilterRuleListUuid(ruleListUuid);
        ResultRO<List<DeviceFilterRuleListRO>> resultROTmp = whaleDevicePolicyClient.getFilterRuleList(searchDTO);
        logger.info(String.format("whale????????????%s??????????????????(%s)???  ", isInbound ? "Inbound Filter Rules" : "Outbound Filter Rules", ruleListUuid));
//        logger.info(JSONObject.toJSONString(resultROTmp));
        List<PolicyDetailVO> voList = new ArrayList<>();
        voList = PolicyListCommonUtil.getPageShowData(resultROTmp.getData());


        String aclName = null;
        for (PolicyDetailVO deviceDetail : voList) {
//            logger.info("???????????????" + JSONObject.toJSONString(deviceDetail));
            if (isInbound) {
                String srcDomain = deviceDetail.getSrcDomain();
                if (srcDomain == null) {
                    continue;
                }
                if (srcDomain.contains(interfaceAlias)) {
                    if (AliStringUtils.isEmpty(deviceDetail.getDescription())) {
                        continue;
                    }
                    String description = deviceDetail.getDescription();
                    String[] tmp = description.split(":");
                    if (tmp.length > 1) {
                        aclName = tmp[1].trim();
                        logger.info("??????In??????????????????????????????...");
                        break;
                    }
                }
            } else {
                String dstDomain = deviceDetail.getDstDomain();
                if (dstDomain == null) {
                    continue;
                }
                if (dstDomain.contains(interfaceAlias)) {
                    if (AliStringUtils.isEmpty(deviceDetail.getDescription())) {
                        continue;
                    }
                    String description = deviceDetail.getDescription();
                    String[] tmp = description.split(":");
                    if (tmp.length > 1) {
                        aclName = tmp[1].trim();
                        logger.info("??????Out??????????????????????????????...");
                        break;
                    }
                }
            }
        }

        logger.info(String.format("??????%s??????(%s)???ACL???????????????%s", isInbound ? "In" : "Out", interfaceAlias, aclName));
        return aclName;
    }

    @Override
    public WhatIfRO addWhatIfCase(WhatIfRO whatIfRO) {
        return whaleWhatIfClient.addWhatIfRO(whatIfRO);
    }

    private String getFirstNameBySearchService(SearchServiceDTO searchServiceDTO, DeviceForExistObjDTO deviceDTO, Integer idleTimeout) {
        ResultRO<String> resultRO = new ResultRO(true);
        logger.info("????????????????????????:" + JSONObject.toJSONString((searchServiceDTO)));
        ResultRO<List<ServiceGroupObjectRO>> objectResult = whaleDeviceObjectClient.searchService(searchServiceDTO, (PageDTO) null);
        logger.info("?????????????????????objectResult  is " + JSONObject.toJSONString(objectResult));
        Map<String, String> objectNameMap = new HashMap<>();
        if (objectResult != null && objectResult.getSuccess() && CollectionUtils.isNotEmpty(objectResult.getData())) {
            for (ServiceGroupObjectRO ro : objectResult.getData()) {
                String deviceObjectType = ro.getDeviceObjectType();
                String isExitObjectTyeValue = objectNameMap.get(deviceObjectType);
                if (StringUtils.isNotEmpty(isExitObjectTyeValue)) {
                    //?????????????????????????????????????????????????????????
                    continue;
                }

                if (null != deviceDTO.getPolicyType() && PolicyEnum.ACL.getKey().equalsIgnoreCase(deviceDTO.getPolicyType().getKey())) {
                    // ??????????????????????????????acl?????? ???????????????????????????acl?????????????????????????????? ??????????????????
                    if (!PolicyEnum.ACL.getKey().equalsIgnoreCase(ro.getPreDefineServiceRefType())) {
                        continue;
                    }
                } else {
                    // ?????????????????????acl???????????????????????????????????????????????????????????????acl??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????Security???acl???
                    if (PolicyEnum.ACL.getKey().equalsIgnoreCase(ro.getPreDefineServiceRefType())) {
                        continue;
                    }
                }
                String name = ro.getName();
                // #bug KSH-5790 ??????????????????????????????port-object???????????????????????????????????????service-object
                if(GenerateConstants.SERVICE_GROUP_OBJECT_TYPE.equals(deviceObjectType) && isRangeCiscoCode(deviceDTO.getModelNumber().getCode())){
                    // ?????????????????????????????????
                    if(null != ro.getCiscoRefServiceTypeEnum() && PORT_SERVICE_TYPE.equalsIgnoreCase(ro.getCiscoRefServiceTypeEnum().name())){
                        continue;
                    }
                }


                if (ro.getIncludeFilterServices() == null || ro.getIncludeFilterServices().size() == 0) {

                    objectNameMap.put(deviceObjectType, name);
                    continue;
                }

                for (IncludeFilterServicesRO serviceRO : ro.getIncludeFilterServices()) {
                    String idleTimeoutString = serviceRO.getIdleTimeout();
                    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    if (!reuse_src_port) {
                        // ???????????????????????????????????????0-65535????????????????????????????????????????????????
                        List<String> srcPortValues = serviceRO.getSrcPortValues();
                        if (CollectionUtils.isNotEmpty(srcPortValues) && "RANGE".equalsIgnoreCase(serviceRO.getSrcPortOp())) {
                            if (!("0".equalsIgnoreCase(srcPortValues.get(0)) && "65535".equalsIgnoreCase(srcPortValues.get(1)))) {
                                continue;
                            }
                        } else if (CollectionUtils.isNotEmpty(srcPortValues) && "EQ".equalsIgnoreCase(serviceRO.getSrcPortOp())) {
                            if (!PolicyConstants.PORT_ANY.equalsIgnoreCase(srcPortValues.get(0))) {
                                continue;
                            }
                        }
                    }

                    if (StringUtils.isBlank(idleTimeoutString) && null == idleTimeout) {
                        objectNameMap.put(deviceObjectType, name);
                        continue;
                    } else if(StringUtils.isNotBlank(idleTimeoutString) && null != idleTimeout){
                        try {
                            Integer timeout = Integer.valueOf(idleTimeoutString);
                            if (timeout.equals(idleTimeout)) {
                                objectNameMap.put(deviceObjectType, name);
                                break;
                            }
                        } catch (Exception e) {
                            logger.error("???????????????????????????????????????" + idleTimeoutString, e);
                        }
                        logger.info(String.format("???????????????????????????  %s:%s", idleTimeout, idleTimeoutString));
                    }
                }

            }
        }
        String objectServiceName = getFinalObjectService(objectNameMap, deviceDTO);
        return objectServiceName;
    }

    /***
     * ????????????-checkpoint: ???????????????-???????????????-??????????????????
     * ????????????-??????????????????????????????-??????????????????
     * @param objectNameMap
     * @param deviceDTO
     * @return
     */
    private String getFinalObjectService(Map<String, String> objectNameMap, DeviceForExistObjDTO deviceDTO) {
        DeviceModelNumberEnum deviceModelNumberEnum = deviceDTO.getModelNumber();
        switch (deviceModelNumberEnum) {
            case ABTNETWORKS:
            case SDNWARE:
            case WESTONE:
            case LEGEND_SEC_NSG_V40:
            case CHECK_POINT:
            case NSFOCUS:
            case TOPSEC_TOS_005:
            case TOPSEC_NG:
            case TOPSEC_NG2:
            case TOPSEC_NG3:
            case TOPSEC_TOS_010_020:
            case TOPSEC_NG4:
            case SRX:
            case SRX_NoCli:
                //????????????-checkpoint/nsfocus: ???????????????-???????????????-??????????????????
                String preDefinedService = objectNameMap.get(GenerateConstants.PREDEFINED_SERVICE_OBJECT_TYPE);
                if (StringUtils.isNotEmpty(preDefinedService)) {
                    return preDefinedService;
                }
                break;
            default:
                //????????????-??????????????????????????????-??????????????????
                break;
        }
        String serverObjectType = objectNameMap.get(GenerateConstants.SERVER_OBJECT_TYPE);
        String serviceGroupObject = objectNameMap.get(GenerateConstants.SERVICE_GROUP_OBJECT_TYPE);
        if (StringUtils.isNotEmpty(serverObjectType)) {
            return serverObjectType;
        } else if (StringUtils.isNotEmpty(serviceGroupObject)) {
            return serviceGroupObject;
        }
        return null;
    }

    @Override
    public ResultRO<List<DeviceFilterRuleListRO>> getFilterRuleList(String deviceUuid, String ruleListUuid) {
        if (AliStringUtils.isEmpty(deviceUuid) || AliStringUtils.isEmpty(ruleListUuid)) {
            return null;
        }
        return whaleDevicePolicyClient.getFilterRuleList(deviceUuid, ruleListUuid);
    }

    @Override
    public List<String> getSubnetListByUntrusted() {
        String trustLevel = "UNTRUSTED_SPOOF_SRC";
        ResultRO<List<SubnetRO>> resultRO = whaleSubnetObjectClient.getSubnetListByTrustLevel(trustLevel);
        if (resultRO == null || resultRO.getData() == null || resultRO.getData().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (SubnetRO subnet : resultRO.getData()) {
            if (AliStringUtils.isEmpty(subnet.getUuid())) {
                continue;
            }
            list.add(subnet.getUuid());
        }

        return list;
    }

    @Override
    public List<DeviceFilterlistRO> getDeviceFilterListRO(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        List<DeviceFilterlistRO> list = resultRO.getData();
        return list;
    }
    @Override
    public List<RoutingtableRO> getRoutTable(String deviceUuid) {
        ResultRO<List<RoutingtableRO>> resultRO = whaleDevicePolicyClient.getRoutingTable(deviceUuid);
        if (resultRO == null || resultRO.getData() == null || resultRO.getData().isEmpty()) {
            return Collections.emptyList();
        }
        List<RoutingtableRO> resultRouteTables = new ArrayList<>();
        // ??????????????????????????????
        for (RoutingtableRO routingtableRO : resultRO.getData()) {
            if (null == routingtableRO) {
                continue;
            }
            resultRouteTables.add(routingtableRO);
        }
        return resultRouteTables;
    }

    @Override
    public List<DeviceInterfaceDto> getDeviceInterfaces(String deviceUuid) {
        // ???whale????????????????????????????????????
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
        List<DeviceInterfaceDto> subnetDtoList = new ArrayList<>();
        if (deviceRO != null && !CollectionUtils.isEmpty(deviceRO.getData()) && !CollectionUtils.isEmpty(deviceRO.getData().get(0).getInterfaces())) {
            for (InterfacesRO interfaces : deviceRO.getData().get(0).getInterfaces()) {
                if (StringUtils.isNotBlank(interfaces.getIp4SubnetUuid())) {
                    SubnetListRO subnetListRO = whaleSubnetObjectClient.getSubnetObject(interfaces.getIp4SubnetUuid());
                    if (subnetListRO != null && !CollectionUtils.isEmpty(subnetListRO.getData())) {
                        SubnetRO subnetRO = subnetListRO.getData().get(0);
                        //??????????????????ipAddress???maskLength????????????
                        if (StringUtils.isAnyBlank(subnetRO.getIp4BaseAddress(), subnetRO.getIp4MaskLength())) {
                            continue;
                        }
                        DeviceInterfaceDto subnetDto = new DeviceInterfaceDto();
                        subnetDto.setSubnetUuid(interfaces.getIp4SubnetUuid());
                        subnetDto.setIpAddress(interfaces.getIp4Address());
                        subnetDto.setMaskLength(interfaces.getIp4MaskLength());
                        subnetDto.setInterfaceName(interfaces.getDeviceInterfaceName());
                        subnetDtoList.add(subnetDto);
                    }
                } else if (AliStringUtils.areNotEmpty(interfaces.getIp6SubnetUuid())) {
                    SubnetListRO subnetListRO = whaleSubnetObjectClient.getSubnetObject(interfaces.getIp6SubnetUuid());
                    if (subnetListRO != null && !CollectionUtils.isEmpty(subnetListRO.getData())) {
                        SubnetRO subnetRO = subnetListRO.getData().get(0);
                        //??????????????????ipAddress???maskLength????????????
                        if (StringUtils.isAnyBlank(subnetRO.getIp6BaseAddress(), subnetRO.getIp6MaskLength())) {
                            continue;
                        }
                        DeviceInterfaceDto subnetDto = new DeviceInterfaceDto();
                        subnetDto.setSubnetUuid(interfaces.getIp6SubnetUuid());
                        subnetDto.setIpAddress(interfaces.getIp6Address());
                        subnetDto.setMaskLength(interfaces.getIp6MaskLength());
                        subnetDto.setInterfaceName(interfaces.getDeviceInterfaceName());
                        subnetDtoList.add(subnetDto);
                    }
                } else {
                    DeviceInterfaceDto subnetDto = new DeviceInterfaceDto();
                    subnetDto.setInterfaceName(interfaces.getDeviceInterfaceName());
                    subnetDtoList.add(subnetDto);
                }
            }
        }
        return subnetDtoList;
    }

    @Override
    public boolean queryIpSystemHasExist(String deviceUuid, String ipSystem) {
        boolean exist = false;
        SearchAddressDTO searchAddressDTO = new SearchAddressDTO();
        searchAddressDTO.setDeviceUuid(deviceUuid);
        searchAddressDTO.setRangeOp(SearchRangeOpEnum.EQUAL.getCode());
        searchAddressDTO.setSearchType(ObjectSearchTypeEnum.TEXT.getCode());
        searchAddressDTO.setQuickSearchText(ipSystem);
        searchAddressDTO.setObjectType(DeviceObjectTypeEnum.NETWORK_OBJECT.getCode());
        PageDTO pageDTO = new PageDTO();
        pageDTO.setPage(1);
        pageDTO.setPsize(20);
        // ???????????????????????????????????? ????????????
        ResultRO<List<NetWorkGroupObjectRO>> dataResultRO = whaleDeviceObjectClient.searchAddress(searchAddressDTO, pageDTO);

        if (dataResultRO != null && org.apache.commons.collections.CollectionUtils.isNotEmpty(dataResultRO.getData()) && dataResultRO.getSuccess()) {
            List<NetWorkGroupObjectShowVO> voList = NetWorkGroupObjectShowVO.formatByList(dataResultRO.getData());
            if (CollectionUtils.isNotEmpty(voList)) {
                for (NetWorkGroupObjectShowVO netWorkGroupObjectShowVO : voList) {
                    if (ipSystem.equalsIgnoreCase(netWorkGroupObjectShowVO.getName())) {
                        return true;
                    }
                }
            }
        }

        // ????????????????????????????????????????????????????????????
        if (!exist) {
            searchAddressDTO.setObjectType(DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT.getCode());
            // ??????????????????????????????????????? ????????????
            ResultRO<List<NetWorkGroupObjectRO>> groupDataResultRO = whaleDeviceObjectClient.searchAddress(searchAddressDTO, pageDTO);
            if (groupDataResultRO == null || org.apache.commons.collections.CollectionUtils.isEmpty(groupDataResultRO.getData()) || !groupDataResultRO.getSuccess()) {
                return false;
            }
            List<NetWorkGroupObjectShowVO> groupVoList = NetWorkGroupObjectShowVO.formatByList(groupDataResultRO.getData());
            if (CollectionUtils.isNotEmpty(groupVoList)) {
                for (NetWorkGroupObjectShowVO netWorkGroupObjectShowVO : groupVoList) {
                    if (ipSystem.equalsIgnoreCase(netWorkGroupObjectShowVO.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public ResultRO<List<DeviceFilterRuleListRO>> getFilterRuleListSearch(String deviceUuid,String dstIp,List<ServiceDTO> serviceList) {
        FilterListsRuleSearchDTO searchDTO = new FilterListsRuleSearchDTO();

        searchDTO.setDeviceUuid(deviceUuid);

        IpTermsExtendDTO ipTerms = new IpTermsExtendDTO();
        if (StringUtils.isNotBlank(dstIp) && !CommonConstants.ANY.equalsIgnoreCase(dstIp)) {
            List<CommonRangeStringDTO> ip4DstAddresses = checkConversionParam(dstIp);
            ipTerms.setIp4DstAddresses(ip4DstAddresses);
            ipTerms.setDstAddressOp(SearchRangeOpEnum.EQUAL);
        }

        JsonQueryDTO jsonQuery = new JsonQueryDTO();
        String whalePolicyType = SYSTEM__NAT_LIST.getRuleListType();
        Map<String, String[]> filterListType = new HashMap<>();
        filterListType.put("$in", whalePolicyType.split(","));
        jsonQuery.setFilterListType(filterListType);

        // ?????????????????????????????????vip
        searchDTO.setSkipImplicit(false);
        searchDTO.setJsonQuery(jsonQuery);


        List<IpTermsDTO> ipTermsList = new ArrayList<>();
        if(null != serviceList && serviceList.size() > 0){
            //???????????????
            for (ServiceDTO serviceDTO :serviceList) {
                IpTermsDTO termsCp = new IpTermsDTO();
                BeanUtils.copyProperties(ipTerms, termsCp);
                if (null == serviceDTO.getProtocol() || PolicyConstants.POLICY_NUM_VALUE_ANY.equals(serviceDTO.getProtocol())) {
                    continue;
                }
                //??????
                if (null != serviceDTO.getProtocol()) {
                    List<CommonRangeIntegerDTO> protocols = new ArrayList<>();
                    CommonRangeIntegerDTO commonRangeIntegerDTO = new CommonRangeIntegerDTO();
                    commonRangeIntegerDTO.setStart(Integer.valueOf(serviceDTO.getProtocol()));
                    commonRangeIntegerDTO.setEnd(Integer.valueOf(serviceDTO.getProtocol()));
                    protocols.add(commonRangeIntegerDTO);
                    termsCp.setProtocols(protocols);
                    if(PolicyConstants.POLICY_NUM_VALUE_ICMP.equals(serviceDTO.getProtocol())){
                        ipTermsList.add(termsCp);
                        continue;
                    }
                }
                //????????????
                if (StringUtils.isNotBlank(serviceDTO.getDstPorts()) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(serviceDTO.getDstPorts())) {
                    String[] dstPorts = serviceDTO.getDstPorts().split(PolicyConstants.ADDRESS_SEPERATOR);
                    for (String itemPort : dstPorts) {

                        List<CommonRangeIntegerDTO> dstPort = new ArrayList<>();
                        if (PortUtils.isPortRange(itemPort)) {
                            String start = PortUtils.getStartPort(itemPort);
                            String end = PortUtils.getEndPort(itemPort);
                            CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO(Integer.valueOf(start), Integer.valueOf(end));
                            dstPort.add(rangeIntegerDTO);
                        } else {
                            CommonRangeIntegerDTO rangeIntegerDTO = new CommonRangeIntegerDTO(Integer.valueOf(itemPort), Integer.valueOf(itemPort));
                            dstPort.add(rangeIntegerDTO);
                        }
                        termsCp.setDstPorts(dstPort);
                        ipTermsList.add(termsCp);
                    }
                } else {
                    if (!JSON.toJSONString(termsCp).equals("{}")) {
                        ipTermsList.add(termsCp);
                    }
                }
            }
            //???????????????????????????????????????????????????1??????????????????????????????
            if (0 == ipTermsList.size() && !JSON.toJSONString(ipTerms).equals("{}")) {
                ipTermsList.add(ipTerms);
            }
        } else {
            //???????????????????????????????????????????????????1??????????????????????????????
            if (!JSON.toJSONString(ipTerms).equals("{}")) {
                ipTermsList.add(ipTerms);
            }
        }

        if (0 == ipTermsList.size() && CollectionUtils.isEmpty(ipTerms.getIp4DstAddresses())) {
            logger.info("????????????vip??????????????????????????????????????????!");
            return null;
        }

        searchDTO.setIpTerms(ipTermsList);

        logger.info("????????????vip??????:{}",JSONObject.toJSONString(searchDTO));
        ResultRO<List<DeviceFilterRuleListRO>> resultROs = whaleDevicePolicyClient.getFilterRuleListSearch(searchDTO, null);
        logger.info("????????????vip????????????:{}",JSONObject.toJSONString(resultROs));

        if (null == resultROs || CollectionUtils.isEmpty(resultROs.getData()) || !resultROs.getSuccess()) {
            return null;
        }
        return resultROs;
    }

    /**
     * ????????????
     * @param ip
     * @return
     */
    private List<CommonRangeStringDTO> checkConversionParam(String ip) {
        String[] ip4Addresses = ip.split(",");
        List<CommonRangeStringDTO> ip4AddressList = new LinkedList<>();
        for (String ip4 : ip4Addresses) {

            CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();

            if (IpUtils.isIPRange(ip4)) {
                String[] ipSegment = ip4.trim().split("-");
                commonRangeStringDTO.setStart(ipSegment[0]);
                commonRangeStringDTO.setEnd(ipSegment[1]);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else if (IpUtils.isIPSegment(ip4)) {
                String startIp = IpUtils.getStartIp(ip4);
                String endIp = IpUtils.getEndIp(ip4);
                commonRangeStringDTO.setStart(startIp);
                commonRangeStringDTO.setEnd(endIp);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else if (IpUtils.isIP(ip4)) {
                commonRangeStringDTO.setStart(ip4);
                commonRangeStringDTO.setEnd(ip4);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else {
                logger.error(String.format("?????????????????????%s", ip4));
                throw new IllegalArgumentException("????????????????????????ip" + ip4 + "??????");
            }
        }
        return ip4AddressList;
    }
}
