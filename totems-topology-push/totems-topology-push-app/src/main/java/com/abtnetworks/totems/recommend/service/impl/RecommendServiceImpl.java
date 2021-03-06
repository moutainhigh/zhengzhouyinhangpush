package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.collect.TotemsListUtils;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.external.vo.AllRuleMatchFlowVO;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.recommend.dto.task.DeviceNatInfoDTO;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.PathDeviceDetailEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.RecommendService;
import com.abtnetworks.totems.whale.baseapi.dto.RuleMatchFlowDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.policy.dto.FilterDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstIntegerDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policy.ro.*;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.abtnetworks.totems.whale.policy.service.impl.WhalePathAnalyzeClientImpl;
import com.abtnetworks.totems.whale.policybasic.ro.FilterListsRO;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS;

@Service
public class RecommendServiceImpl implements RecommendService {
    private static Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    public WhaleManager whaleService;

    @Autowired
    public VendorManager vendorManager;

    @Value("${push.whale:false}")
    private Boolean isNginZ;

    @Value("${push.open-acl:false}")
    private Boolean openAcl;

    @Value("${push.reverse-acl:false}")
    private Boolean reverseAcl;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    public WhalePathAnalyzeClientImpl whalePathAnalyzeClient;

    @Autowired
    private RecommendTaskManager recommendTaskManager;

    @Autowired
    WhalePathAnalyzeClient client;

    @Override
    public int recommendPolicyByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO) {
        int pathId = task.getId();
        int taskId = task.getTaskId();
        String theme = task.getTheme();
        logger.info(String.format("??????(%d)[%s]??????%d????????????????????????...", taskId, theme, pathId));

        PathAnalyzeRO pathAnalyzeRO = task.getPathAnalyzeRO();
        List<PathAnalyzeDataRO> list = pathAnalyzeRO.getData();

        if(list == null || list.size() == 0) {
            //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????...
            logger.warn(String.format("??????(%d)[%s]??????%d????????????????????????????????????...",taskId, theme, pathId));
            return ReturnCode.FAILED;
        }

        //????????????????????????????????????????????????
        List<RecommendPolicyEntity> policyEntityList = new ArrayList<>();

        //???????????????????????????????????????????????????????????????????????????????????????MySQL??????
        List<PathDeviceDetailEntity> deviceDetailList = new ArrayList<>();

        boolean updateAnalyseStatus =false;

        for(PathAnalyzeDataRO pathAnalyzeData: list) {
            List<PathInfoRO> pathInfoList = pathAnalyzeData.getPathList();
            if (pathInfoList == null || pathInfoList.size() == 0) {
                logger.warn(String.format("??????(%d)[%s]??????%d????????????????????????...", taskId, theme, pathId));
                continue;
            }
            logger.debug(String.format("??????(%d)[%s]??????%d????????????????????????...", taskId, theme, pathId));


//            //????????????DTO?????????????????????????????????????????????????????????
//            List<RecommendPolicyDTO> recommendPolicyDTOList = new ArrayList<>();
            for (PathInfoRO pathInfoRO : pathInfoList) {
                logger.debug(String.format("??????(%d)[%s]:??????(%d)????????????:%s", taskId, theme, pathId, JSONObject.toJSONString(pathInfoRO)));
                List<PathDetailRO> pathDetailList = pathInfoRO.getDeviceDetails();
                if(pathDetailList == null || pathDetailList.size() == 0) {
                    logger.error(String.format("??????(%d)[%s]:??????(%d)??????????????????????????????!", taskId, theme, pathId));
                    continue;
                }

                int index = 0;
                for (PathDetailRO pathDetailRO : pathDetailList) {
                    String deviceUuid = pathDetailRO.getDeviceUuid();
                    NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
                    if (node == null) {
                        logger.error(String.format("??????(%d)[%s]:??????(%d)????????????(%s)???????????????????????????...", taskId, theme, pathId, deviceUuid));
                        continue;
                    }
                    logger.debug(String.format("??????(%d)[%s]:??????(%d)????????????%s(%s)????????????...", taskId, theme, pathId, node.getDeviceName(), node.getIp()));

                    //??????????????????
                    DeviceDetailRO deviceDetailRO = null;
                    logger.info(String.format("??????(%d)[%s]:??????(%d)????????????????????????:%s",taskId, theme, pathId,JSONObject.toJSONString(pathDetailRO)));
                    try {
                        //???????????????whale?????????????????????
                        if (isNginZ == null || !isNginZ) {
                            //?????????whale
                            deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO, task.getWhatIfCaseUuid());
                        }else{
                            //???????????????????????????
                            deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO,null);
                        }
                    } catch(Exception e) {
                        logger.error(String.format("??????(%d)[%s]??????%d??????????????????????????????...", taskId, task.getTheme(), task.getId()), e);
                    }

                    if (deviceDetailRO == null) {
                        //???????????????????????????????????????????????????????????????flow?????????
                        logger.warn(String.format("??????(%d)[%s]??????%d??????%s(%s)?????????????????????:%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), pathDetailRO));
                    } else {
                        logger.debug(String.format("??????(%d)[%s]??????%d??????%s(%s)???????????????:%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), deviceDetailRO));
                    }

                    //??????????????????
                    PathDeviceDetailEntity deviceDetailEntity = saveDeviceDetail(pathId, deviceUuid, index, deviceDetailRO);
                    index++;
                    deviceDetailList.add(deviceDetailEntity);

                    String currentStatus = pathDetailRO.getCurrDevicePathStatus();
                    //???????????????????????????????????????????????????????????????????????????
                    String longLinkPathStatus = pathDetailRO.getLongLinkPathStatus();
                    List<PolicyInfoDTO> policyInfoList = new ArrayList<>();

                    //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    if (null != currentStatus && currentStatus.endsWith("FULLY_OPEN") &&
                            (AliStringUtils.isEmpty(longLinkPathStatus) || longLinkPathStatus.endsWith("FULLY_OPEN"))) {
                        if (isNginZ == null || !isNginZ) {
                            //?????????whale
                            logger.debug(String.format("??????(%d)[%s]??????%d??????%s(%s)?????????????????????????????????", taskId, theme, pathId, node.getDeviceName(), node.getIp()));

                            logger.info(String.format("??????(%d)[%s]??????%d??????%s(%s)???????????????currDevicePathStatus(%s),longLinkePathStatus(%s)??????????????????",
                                    taskId, theme, pathId, node.getDeviceName(), node.getIp(), currentStatus, longLinkPathStatus));
                            continue;
                        }else{
//                            if (simulationTaskDTO.getTaskType() == POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND || simulationTaskDTO.getTaskType() == BIG_INTERNET_RECOMMEND ) {
                                logger.debug(String.format("??????(%d)[%s]??????%d??????%s(%s)?????????????????????????????????", taskId, theme, pathId, node.getDeviceName(), node.getIp()));

                                logger.info(String.format("??????(%d)[%s]??????%d??????%s(%s)???????????????currDevicePathStatus(%s),longLinkePathStatus(%s)??????????????????",
                                        taskId, theme, pathId, node.getDeviceName(), node.getIp(), currentStatus, longLinkPathStatus));
                                continue;
//                            }else{
                                //???????????????  20210203  KSH-4959??????????????? ??????????????????????????????????????????
//                                List<DeviceFlowsRO> deviceFlowsROList  = pathDetailRO.getDeviceFlows();
//                                policyInfoList = getPolicyList(deviceFlowsROList, node, task);
//                            }
                        }

                    } else if(null != currentStatus && currentStatus.endsWith("FULLY_OPEN")) {
                        //????????????????????????????????????????????????????????????
                        List<PathFlowRO> pathFlowROList = pathDetailRO.getLongLinkDenyTraffic();
                        List<PolicyInfoDTO> policyList = getPolicyInfoListFromPathFlow(pathFlowROList, task, pathDetailRO,false);
                        // ???????????????policyList
                        buildPolicyDtoToList(task, simulationTaskDTO, policyInfoList, policyList);
                    } else {
                        if("NO_PATH".equalsIgnoreCase(pathInfoRO.getPathType())){
                            logger.error(String.format("??????(%d)[%s]:??????(%d)??????%s(%s)????????????????????????????????????????????????%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), pathDetailRO));
                            continue;
                        }
                        //???????????????????????????
                        List<DeviceFlowsRO> deviceFlowsROList = pathDetailRO.getDeviceFlows();
                        if (deviceFlowsROList == null || deviceFlowsROList.size() == 0) {
                            logger.error(String.format("??????(%d)[%s]:??????(%d)??????%s(%s)?????????????????????????????????%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), pathDetailRO));
                            continue;
                        }
                        List<PolicyInfoDTO> policyList = getPolicyList(deviceFlowsROList, node, task,pathDetailRO,deviceDetailRO);
                        // ???????????????policyList
                        buildPolicyDtoToList(task, simulationTaskDTO, policyInfoList, policyList);
                    }
                    // ???????????????nat??????
                    simulationTaskDTO.setMatchNat(task.getMatchNat());

                    //?????????????????????????????????????????????
                    policyInfoList = mergePolicyInfoList(policyInfoList);

                    //?????????????????????????????????????????????UUID
                    String ruleListUuid = getRuleListUuid(deviceDetailRO);
                    ruleListUuid = getRuleListUuid(ruleListUuid, node);


                    //?????????????????????????????????UUID
                    String inIfId = pathDetailRO.getInIfId();
                    String outIfId = pathDetailRO.getOutIfId();

                    //??????????????????
                    DeviceRO device = null;
                    try {
                        device = whaleService.getDeviceByUuid(deviceUuid);
                    } catch (Exception e) {
                        logger.error(String.format("??????(%d)[%s]??????%d????????????%s????????????", task.getTaskId(), task.getTheme(), task.getId(), deviceUuid), e);
                    }

                    if(device == null || device.getData() == null || device.getData().size() == 0) {
                        logger.warn(String.format("??????(%d)[%s]??????%d????????????%s????????????, ???????????????...", task.getTaskId(), task.getTheme(), task.getId(), deviceUuid));
                        continue;
                    }



                    //??????????????????????????????????????????????????????
                    DeviceDataRO deviceData = device.getData().get(0);
                    boolean isVsys = false;
                    String rootDeviceUuid = "";
                    String vsysName = "";
                    if(deviceData.getIsVsys() != null) {
                        isVsys = deviceData.getIsVsys();
                        rootDeviceUuid = deviceData.getRootDeviceUuid();
                        vsysName = deviceData.getVsysName();
                    }


                    // ???????????????
                    String inAclRuleListName = null;
                    String outAclRuleListName = null;
                    //?????????????????????????????????????????????UUID
                    String inRuleListUuid = null;
                    String outRuleListUuid = null;
                    // ???????????????id
                    String inAclRuleIds = null;
                    String outAclRuleIds = null;

                    // ???????????????/?????????????????????????????????????????????????????????????????????uuid??????????????????id
                    String modelNumber = node.getModelNumber();
                    PolicyEnum policyType = null;

                    // ??????????????????acl?????????
                    if (!AliStringUtils.isEmpty(modelNumber) && DeviceTypeEnum.ROUTER.name().equalsIgnoreCase(deviceData.getDeviceType()) &&
                            (modelNumber.equals("Cisco IOS") || modelNumber.equals("Cisco NX-OS") || modelNumber.equals("Ruijie RGOS")
                            || modelNumber.equals(DeviceModelNumberEnum.SRX.getKey()) || modelNumber.equals(DeviceModelNumberEnum.SRX_NoCli.getKey())
                            || modelNumber.equalsIgnoreCase(DeviceModelNumberEnum.JUNIPER_ROUTER.getKey()))) {
                        //?????????????????????????????????????????????UUID
                        if (!openAcl) {
                            logger.info(String.format("????????????:%s?????????acl??????,???????????????????????????", modelNumber));
                            continue;
                        }
                        Map<String, List<String>> reACLRuleMap = getACLRuleListUuid(deviceDetailRO);
                        if (reACLRuleMap == null || reACLRuleMap.size() < 1) {
                            logger.info("??????????????????acl???????????????????????????uuid??????,???????????????");
                            continue;
                        }
                        if (!TotemsListUtils.isEmpty(reACLRuleMap.get("in"))) {
                            List<String> inRulList = reACLRuleMap.get("in");
                            inRuleListUuid = inRulList.get(0);
                            inAclRuleIds = inRulList.get(1);
                            inAclRuleListName = getAclRuleListName(inRuleListUuid);
                        }

                        if (!TotemsListUtils.isEmpty(reACLRuleMap.get("out"))) {
                            List<String> outRulList = reACLRuleMap.get("out");
                            outRuleListUuid = outRulList.get(0);
                            outAclRuleIds = outRulList.get(1);
                            outAclRuleListName = getAclRuleListName(outRuleListUuid);
                        }
                        if (StringUtils.isBlank(inAclRuleListName) && StringUtils.isBlank(outAclRuleListName)) {
                            continue;
                        }
                        policyType = PolicyEnum.ACL;
                    }


                    //??????????????????????????????????????????
                    List<String> srcIntf = InterfaceNameUtils.getInterfaceName(deviceData, inIfId);
                    List<String> dstIntf = InterfaceNameUtils.getInterfaceName(deviceData, outIfId);
                    String srcInterface = srcIntf.get(0);
                    String dstInterface = dstIntf.get(0);

                    //??????????????????Zone??????
                    ZoneRO zone = null;
                    try {
                        zone = whaleService.getDeviceZone(deviceUuid);
                    } catch(Exception e) {
                        logger.error(String.format("??????(%d)[%s]??????%d?????????????????????", task.getTaskId(), task.getTheme(), task.getId()), e);
                    }

                    //?????????????????????Zone?????????????????????????????????
                    ZoneDataRO srcZoneData = whaleService.getZoneData(zone, srcInterface);
                    ZoneDataRO dstZoneData = whaleService.getZoneData(zone, dstInterface);

                    // ??????????????????acl ????????????????????????
                    if (StringUtils.isNotBlank(inAclRuleListName)) {
                        buildPolicyEntity(task, simulationTaskDTO, policyEntityList, node, policyInfoList, inRuleListUuid, isVsys,
                                rootDeviceUuid, vsysName, inAclRuleListName, inAclRuleIds, policyType, srcIntf, dstIntf, srcZoneData, dstZoneData);
                    }

                    // ??????????????????acl ????????????????????????
                    if (StringUtils.isNotBlank(outAclRuleListName)) {
                        buildPolicyEntity(task, simulationTaskDTO, policyEntityList, node, policyInfoList, outRuleListUuid, isVsys,
                                rootDeviceUuid, vsysName, outAclRuleListName, outAclRuleIds, policyType, srcIntf, dstIntf, srcZoneData, dstZoneData);
                    }

                    //  ?????????????????????????????????save??????(????????????)
                    if(StringUtils.isBlank(inAclRuleListName) && StringUtils.isBlank(outAclRuleListName)){
                        buildPolicyEntity(task, simulationTaskDTO, policyEntityList, node, policyInfoList, ruleListUuid, isVsys,
                                rootDeviceUuid, vsysName, null, null, policyType, srcIntf, dstIntf, srcZoneData, dstZoneData);
                    }

                }

                if (reverseAcl) {
                    // ????????????acl ????????????
                    boolean isReverseAcl = reverseAclPolicyCreate(task, simulationTaskDTO, taskId, policyEntityList, pathDetailList);
                    if (isReverseAcl) {
                        updateAnalyseStatus = true;
                    }
                }
            }
        }

        //  ?????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(simulationTaskDTO.getPathAnalyzeStatus()) && String.valueOf(POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS).equals(simulationTaskDTO.getPathAnalyzeStatus()) && updateAnalyseStatus) {
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FINISHED);
        }

        //2020-06-19 luwei ???????????????????????????bug=4377?????????????????????
        if (isNginZ != null && isNginZ) {
            logger.info("??????????????????????????????????????????,size:{}", policyEntityList.size());
            policyEntityList = policyEntityList.stream().distinct().collect(Collectors.toList());
            logger.info("??????????????????????????????????????????size:{}", policyEntityList.size());
        }



        int rc = taskService.addRecommendPolicyList(policyEntityList);
        if(rc != ReturnCode.POLICY_MSG_OK){
            logger.warn(String.format("??????(%d)[%s]??????%d??????????????????",taskId, theme, pathId) + ReturnCode.getMsg(rc));
        }

        rc = taskService.insertpathDeviceDetailList(deviceDetailList);
        if(rc != ReturnCode.POLICY_MSG_OK){
            logger.warn(String.format("??????(%d)[%s]??????%d??????????????????",taskId, theme, pathId) + ReturnCode.getMsg(rc));
        }

        //????????????????????????PolicyDTO???????????????????????????????????????
        task.setPolicyList(getPolicyDTOList(policyEntityList));

        //???????????????????????????????????????????????????????????????????????????????????????
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????
        if(task.getPolicyList().size() > 0) {
            taskService.updatePathAdviceStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_ADVICE_FINISHED);
            logger.info(String.format("??????(%d)[%s]??????%d???????????????????????????%d?????????????????????...",taskId, theme, pathId, task.getPolicyList().size()));
            return ReturnCode.POLICY_MSG_OK;
        }

        logger.info(String.format("??????(%d)[%s]??????%d?????????????????????????????????????????????...",taskId, theme, pathId));
        return ReturnCode.NO_RECOMMEND_POLICY;
    }

    private void buildPolicyDtoToList(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO, List<PolicyInfoDTO> policyInfoList,
                                      List<PolicyInfoDTO> policyList) {
        Map<String, String> domainMap = simulationTaskDTO.getDomainConvertIp();
        if (null != domainMap && !domainMap.isEmpty() && StringUtils.isNotEmpty(domainMap.get(task.getDstIp()))) {
            for (PolicyInfoDTO policy : policyList) {
                //??????????????????????????????ip?????????????????????
                policy.setPolicySource(CommonConstants.POLICY_SOURCE_DST_DOMAIN);
                StringBuffer dstIp = new StringBuffer(policy.getDstIp()) ;
                String domain = domainMap.get(task.getDstIp());
                dstIp.append(",").append(domain);
                policy.setDstIp(dstIp.toString());

            }
        }
        policyInfoList.addAll(policyList);
    }

    /**
     * ????????????????????????
     * @param task
     * @param simulationTaskDTO
     * @param policyEntityList
     * @param node
     * @param policyInfoList
     * @param ruleListUuid
     * @param isVsys
     * @param rootDeviceUuid
     * @param vsysName
     * @param inAclRuleListName
     * @param inAclRuleIds
     * @param policyType
     * @param srcIntf
     * @param dstIntf
     * @param srcZoneData
     * @param dstZoneData
     */
    private void buildPolicyEntity(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO, List<RecommendPolicyEntity> policyEntityList, NodeEntity node, List<PolicyInfoDTO> policyInfoList, String ruleListUuid, boolean isVsys, String rootDeviceUuid, String vsysName, String inAclRuleListName, String inAclRuleIds, PolicyEnum policyType, List<String> srcIntf, List<String> dstIntf, ZoneDataRO srcZoneData, ZoneDataRO dstZoneData) {
        //????????????????????????
        for (PolicyInfoDTO policyInfo : policyInfoList) {

            RecommendPolicyEntity policyEntity = getPolicyEntity(task, policyInfo, node, ruleListUuid,
                    srcZoneData, dstZoneData, srcIntf, dstIntf, isVsys, rootDeviceUuid, vsysName, task.getIdleTimeout(), simulationTaskDTO, policyType, inAclRuleListName, inAclRuleIds);

            //??????????????????
            taskService.getAdvancedSettings(policyEntity);

            //?????????????????????????????????
            policyEntityList.add(policyEntity);
        }
    }

    /**
     * ????????????cisco acl????????????
     * @param task
     * @param simulationTaskDTO
     * @param taskId
     * @param policyEntityList
     * @param pathDetailList
     */
    private boolean reverseAclPolicyCreate(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO, int taskId, List<RecommendPolicyEntity> policyEntityList,
                                        List<PathDetailRO> pathDetailList) {
        boolean updateAnalyseStatus = false;
        for (PathDetailRO pathDetailRO : pathDetailList) {
            String deviceUuid = pathDetailRO.getDeviceUuid();

            NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
            //0???????????????1?????????/?????????2??????????????? 3: ????????????
            if (node == null || !"1".equals(node.getType())) {
                continue;
            }
            String modelNumber = node.getModelNumber();
            // ??????????????????ios?????????nx_os??????acl????????????
            if (!AliStringUtils.isEmpty(modelNumber) && !(modelNumber.equals(DeviceModelNumberEnum.CISCO_IOS.getKey())
                    || modelNumber.equals(DeviceModelNumberEnum.CISCO_NX_OS.getKey()))) {
                continue;
            }
            logger.info("??????cisco ??????acl????????????...");
            String tempId = pathDetailRO.getInIfId();
            pathDetailRO.setInIfId(pathDetailRO.getOutIfId());
            pathDetailRO.setOutIfId(tempId);
            List<DeviceFlowsRO> deviceFlows = pathDetailRO.getDeviceFlows();
            for (DeviceFlowsRO deviceFlow : deviceFlows) {
                List<FlowListRO> flowList = deviceFlow.getFlowList();
                for (FlowListRO flowListRO : flowList) {
                    List<PathFlowRO> traffic = flowListRO.getTraffic();
                    for (PathFlowRO pathFlowRO : traffic) {
                        List<SrcDstIntegerDTO> srcPorts = pathFlowRO.getSrcPorts();
                        pathFlowRO.setSrcPorts(pathFlowRO.getDstPorts());
                        pathFlowRO.setDstPorts(srcPorts);
                        List<SrcDstStringDTO> ip4SrcAddresses = pathFlowRO.getIp4SrcAddresses();
                        pathFlowRO.setIp4SrcAddresses(pathFlowRO.getIp4DstAddresses());
                        pathFlowRO.setIp4DstAddresses(ip4SrcAddresses);
                        List<SrcDstStringDTO> ip6SrcAddresses = pathFlowRO.getIp6SrcAddresses();
                        pathFlowRO.setIp6SrcAddresses(pathFlowRO.getIp6DstAddresses());
                        pathFlowRO.setIp6DstAddresses(ip6SrcAddresses);
                    }
                }
            }
            //??????????????????
            DeviceDetailRO deviceDetailRO = null;
            try {
                //???????????????whale?????????????????????
                if (isNginZ == null || !isNginZ) {
                    //?????????whale
                    deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO, task.getWhatIfCaseUuid());
                } else {
                    //???????????????????????????
                    deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO, null);
                }
            } catch (Exception e) {
                logger.error(String.format("??????????????????(%d)[%s]??????%d??????????????????????????????...", taskId, task.getTheme(),
                        task.getId()), e);
            }

            if (deviceDetailRO == null) {
                deviceDetailRO = new DeviceDetailRO();
            }

            //?????????????????????????????????UUID
            String inIfId = pathDetailRO.getInIfId();
            String outIfId = pathDetailRO.getOutIfId();

            //??????????????????
            DeviceRO device = null;
            try {
                device = whaleService.getDeviceByUuid(deviceUuid);
            } catch (Exception e) {
                logger.error(String.format("??????????????????(%d)[%s]??????%d????????????%s????????????", task.getTaskId(), task.getTheme(),
                        task.getId(), deviceUuid), e);
            }

            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.warn(String.format("??????????????????(%d)[%s]??????%d????????????%s????????????, ???????????????...", task.getTaskId(),
                        task.getTheme(), task.getId(), deviceUuid));
                continue;
            }

            //??????????????????????????????????????????????????????
            DeviceDataRO deviceData = device.getData().get(0);

            boolean isVsys = false;
            String rootDeviceUuid = "";
            String vsysName = "";
            if (deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                rootDeviceUuid = deviceData.getRootDeviceUuid();
                vsysName = deviceData.getVsysName();
            }

            // ???????????????
            String inAclRuleListName = null;
            String outAclRuleListName = null;
            //?????????????????????????????????????????????UUID
            String inRuleListUuid = null;
            String outRuleListUuid = null;
            // ???????????????id
            String inAclRuleIds = null;
            String outAclRuleIds = null;
            // ??????????????????????????????????????????????????????????????????????????????uuid??????????????????id
            PolicyEnum policyType = null;

            //?????????????????????????????????????????????UUID
            Map<String, List<String>> reACLRuleMap = getACLRuleListUuid(deviceDetailRO);
            if (reACLRuleMap == null || reACLRuleMap.size() < 1) {
                logger.info("??????????????????acl???????????????????????????uuid??????,???????????????");
                continue;
            }
            if (!TotemsListUtils.isEmpty(reACLRuleMap.get("in"))) {
                List<String> inRulList = reACLRuleMap.get("in");
                inRuleListUuid = inRulList.get(0);
                inAclRuleIds = inRulList.get(1);
                inAclRuleListName = getAclRuleListName(inRuleListUuid);
            }

            if (!TotemsListUtils.isEmpty(reACLRuleMap.get("out") )) {
                List<String> outRulList = reACLRuleMap.get("out");
                outRuleListUuid = outRulList.get(0);
                outAclRuleIds = outRulList.get(1);
                outAclRuleListName = getAclRuleListName(outRuleListUuid);
            }

            if (StringUtils.isBlank(inAclRuleListName) && StringUtils.isBlank(outAclRuleListName)) {
                continue;
            }
            policyType = PolicyEnum.ACL;

            //??????????????????????????????????????????
            List<String> srcIntf = InterfaceNameUtils.getInterfaceName(deviceData, inIfId);
            List<String> dstIntf = InterfaceNameUtils.getInterfaceName(deviceData, outIfId);
            String srcInterface = srcIntf.get(0);
            String dstInterface = dstIntf.get(0);

            RuleMatchFlowDTO ruleMatchFlowDTO = new RuleMatchFlowDTO();

            ruleMatchFlowDTO.setBeginFlow(createBeginFlow(task));
            ruleMatchFlowDTO.setInInterfaceName(srcInterface);
            ruleMatchFlowDTO.setOutInterfaceName(dstInterface);
            ruleMatchFlowDTO.setInIfId(inIfId);
            ruleMatchFlowDTO.setOutIfId(outIfId);
            ruleMatchFlowDTO.setEstablish(true);
            ruleMatchFlowDTO.setDeviceUuid(deviceUuid);

            AllRuleMatchFlowVO allRuleMatchFlowVO = null;
            try {
                //?????????whale
                allRuleMatchFlowVO = whalePathAnalyzeClient.getAllRuleMatchFlow(ruleMatchFlowDTO);
            } catch (Exception e) {
                logger.error(String.format("??????????????????(%d)[%s]??????%d??????(?????????)??????????????????????????????[%s]...", taskId, task.getTheme(),
                        task.getId(), e.getMessage()));
            }

            logger.info(String.format("??????????????????(%d)[%s]??????%d??????(?????????)???????????????????????????:[%s]",taskId, task.getTheme(), task.getId(), JSONObject.toJSONString(allRuleMatchFlowVO)));
            if (allRuleMatchFlowVO == null) {
                continue;
            }

            if (TotemsListUtils.isEmpty(allRuleMatchFlowVO.getInDeny())
                    && TotemsListUtils.isEmpty(allRuleMatchFlowVO.getOutDeny())) {
                continue;
            }

            if (StringUtils.isNotBlank(inAclRuleListName) && !TotemsListUtils.isEmpty(allRuleMatchFlowVO.getInDeny())) {
                List<PathFlowRO> inDenyList = allRuleMatchFlowVO.getInDeny();

                getPolicyList(task, simulationTaskDTO, policyEntityList, node, inRuleListUuid, isVsys,
                        rootDeviceUuid, vsysName, inAclRuleListName, inAclRuleIds, policyType, srcIntf,
                        dstIntf, inDenyList);
            }

            if (StringUtils.isNotBlank(outAclRuleListName) && !TotemsListUtils.isEmpty(allRuleMatchFlowVO.getOutDeny())) {
                List<PathFlowRO> outDenyList = allRuleMatchFlowVO.getOutDeny();

                getPolicyList(task, simulationTaskDTO, policyEntityList, node, outRuleListUuid, isVsys,
                        rootDeviceUuid, vsysName, outAclRuleListName, outAclRuleIds, policyType, srcIntf,
                        dstIntf, outDenyList);
            }
            updateAnalyseStatus = true;
            logger.info("??????cisco ??????acl????????????...");
        }
        return updateAnalyseStatus;
    }


    /**
     * ???????????????uuid?????????????????????
     * @param ruleListUuid
     * @return
     */
    private String getAclRuleListName(String ruleListUuid) {
        FilterListsRO dataPO = null;
        try {
            dataPO = whaleService.getFilterListsByUuid(ruleListUuid);
        } catch (Exception e) {
            logger.error(String.format("???????????????uuid:[%s]???????????????????????????...", ruleListUuid), e);
        }
        if (dataPO == null || null == dataPO.getData()) {
            logger.warn(String.format("???????????????uuid:[%s]???????????????????????????, ???????????????...", ruleListUuid));
            return null;
        }
        String aclRuleListName = dataPO.getData().get(0).getName();
        return aclRuleListName;
    }


    /**
     * ??????begin?????????
     * @param task
     * @return
     */
    private List<FilterDTO> createBeginFlow(PathInfoTaskDTO task) {

        FilterDTO filterDTO = new FilterDTO();
        //?????????ip
        List<SrcDstStringDTO> srcIpList = new ArrayList<>();
        //?????????ip
        String[] srcIpStrs = task.getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String srcIpStr : srcIpStrs) {
            String startIp = IpUtils.getStartIpFromIpAddress(srcIpStr);
            String endIp = IpUtils.getEndIpFromIpAddress(srcIpStr);
            srcIpList.add(WhaleDoUtils.getSrcDstStringDTO(startIp, endIp));
        }
        filterDTO.setIp4DstAddresses(srcIpList);

        //????????????ip
        List<SrcDstStringDTO> dstIpList = new ArrayList<>();
        String[] dstIpStrs = task.getDstIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String strIpStr : dstIpStrs) {
            String startIp = IpUtils.getStartIpFromIpAddress(strIpStr);
            String endIp = IpUtils.getEndIpFromIpAddress(strIpStr);
            dstIpList.add(WhaleDoUtils.getSrcDstStringDTO(startIp, endIp));
        }
        filterDTO.setIp4SrcAddresses(dstIpList);

        List<SrcDstIntegerDTO> protocolList = new ArrayList<>();
        StringBuffer srcPorts = new StringBuffer();
        for (ServiceDTO serviceDTO : task.getServiceList()) {

            SrcDstIntegerDTO protocolDTO = new SrcDstIntegerDTO();
            int protocolNum = serviceDTO.getProtocol() == null ? -1 :
                    Integer.parseInt(serviceDTO.getProtocol());
            if (protocolNum == -1) {
                protocolDTO.setStart(0);
                protocolDTO.setEnd(255);
            } else {
                protocolDTO.setStart(protocolNum);
                protocolDTO.setEnd(protocolNum);
            }
            protocolList.add(protocolDTO);

            srcPorts = srcPorts.append(StringUtils.isNotBlank(serviceDTO.getDstPorts()) ? serviceDTO.getDstPorts() + "," : "");
        }
        if (srcPorts.lastIndexOf(",") > 0) {
            srcPorts.deleteCharAt(srcPorts.lastIndexOf(","));
        }
        filterDTO.setProtocols(protocolList);

        //????????????
        filterDTO.setSrcPorts(WhaleUtils.getSrcDstIntegerDTOList(srcPorts.toString()));
        filterDTO.setDstPorts(WhaleUtils.getSrcDstIntegerDTOList("any"));

        List<FilterDTO> beginFlow = new ArrayList<>();
        beginFlow.add(filterDTO);

        return beginFlow;
    }

    private void getPolicyList(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO,
                               List<RecommendPolicyEntity> policyEntityList, NodeEntity node, String ruleListUuid,
                               boolean isVsys, String rootDeviceUuid, String vsysName, String aclRuleListName,
                               String aclRuleIds, PolicyEnum policyType, List<String> srcIntf, List<String> dstIntf,
                               List<PathFlowRO> denyList) {
        //==
        List<PolicyInfoDTO> policyList = getPolicyInfoListFromPathFlow(denyList, task,null,true);
        Map<String, String> domainMap = simulationTaskDTO.getDomainConvertIp();
        if (null != domainMap && !domainMap.isEmpty() && StringUtils.isNotEmpty(domainMap.get(task.getDstIp()))) {
            //????????????
            for (PolicyInfoDTO policy : policyList) {
                policy.setPolicySource(CommonConstants.POLICY_SOURCE_DST_DOMAIN);
                StringBuilder dstIp = new StringBuilder(policy.getDstIp());
                String domain = domainMap.get(task.getDstIp());
                dstIp.append(",").append(domain);
                policy.setDstIp(dstIp.toString());
            }
        }
        List<PolicyInfoDTO> policyInfoList = new ArrayList<>(policyList);
        //==

        //?????????????????????????????????????????????
        policyInfoList = mergePolicyInfoList(policyInfoList);

        //????????????????????????
        for (PolicyInfoDTO policyInfo : policyInfoList) {

            RecommendPolicyEntity policyEntity = getPolicyEntity(task, policyInfo, node, ruleListUuid,
                    null, null, srcIntf, dstIntf, isVsys, rootDeviceUuid, vsysName,
                    task.getIdleTimeout(), simulationTaskDTO, policyType, aclRuleListName, aclRuleIds);

            //??????????????????
            taskService.getAdvancedSettings(policyEntity);

            //?????????????????????????????????
            policyEntityList.add(policyEntity);
        }
    }

    /**
     * ??????????????????
     * @param pathId pathId
     * @param deviceUuid ??????UUid
     * @param index ????????????
     * @param deviceDetailRO ??????????????????
     */
    private PathDeviceDetailEntity saveDeviceDetail(int pathId, String deviceUuid, int index, DeviceDetailRO deviceDetailRO) {
        PathDeviceDetailEntity entity = new PathDeviceDetailEntity();
        entity.setPathInfoId(pathId);
        entity.setDeviceUuid(deviceUuid);
        entity.setIsVerifyData(PolicyConstants.POLICY_INT_PATH_ANALYZE_DATA);
        entity.setDeviceDetail(JSONObject.toJSONString(deviceDetailRO));
        entity.setPathIndex(index);
        return entity;
    }

    /**
     * ?????????????????????????????????RuleListUuid
     * @param deviceDetailRO ????????????
     * @return ?????????RuleListUuid
     */
    private String getRuleListUuid(DeviceDetailRO deviceDetailRO) {
        List<DeviceDetailDataRO> deviceDetailDataList = deviceDetailRO.getData();
        if(deviceDetailDataList == null || deviceDetailDataList.size() == 0) {
            return null;
        }
        for (DeviceDetailDataRO deviceDetailDataVO : deviceDetailDataList) {

            DeviceRulesRO deviceRules = deviceDetailDataVO.getDeviceRules();
            List<DeviceFilterRuleListRO> deviceRulesList = deviceRules.getSYSTEM__POLICY_1();
            if(deviceRulesList == null) {
                deviceRulesList = deviceRules.getSYSTEM__POLICY_2();
            }

            if(deviceRulesList == null) {
                logger.error("????????????????????????...");
                continue;
            }

            //????????????????????????????????????UUID
            for (DeviceFilterRuleListRO deviceRulesVO : deviceRulesList) {
                String ruleListUuid = deviceRulesVO.getRuleListUuid();
                if(!AliStringUtils.isEmpty(ruleListUuid)) {
                    return ruleListUuid;
                }
            }
        }
        return null;
    }


    /**
     * ?????????????????????????????????RuleListUuid???????????????rule id
     * @param deviceDetailRO ????????????
     * @return ?????????RuleListUuid
     */
    private Map<String,List<String>> getACLRuleListUuid(DeviceDetailRO deviceDetailRO) {
        logger.info("????????????acl??????????????????:{}",JSONObject.toJSONString(deviceDetailRO));
        Map<String,List<String>> aclRuleMap = new HashMap<>();
        List<DeviceDetailDataRO> deviceDetailDataList = deviceDetailRO.getData();
        if (CollectionUtils.isEmpty(deviceDetailDataList)) {
            return null;
        }
        for (DeviceDetailDataRO deviceDetailDataVO : deviceDetailDataList) {
            DeviceRulesRO deviceRules = deviceDetailDataVO.getDeviceRules();
            List<DeviceFilterRuleListRO>  inDeviceRulesList = deviceRules.getSYSTEM__INBOUND_INTERFACE_ACL();
            List<DeviceFilterRuleListRO>  outDeviceRulesList = deviceRules.getSYSTEM__OUTBOUND_INTERFACE_ACL();

            if(CollectionUtils.isEmpty(inDeviceRulesList) && CollectionUtils.isEmpty(outDeviceRulesList)){
                logger.error("??????ACL?????????????????????...");
                return null;
            }
            // ?????????????????????,??????????????????????????????deny????????????????????????????????????????????????????????????????????????deny????????????????????????null
            List<String> inRuleList = new ArrayList<>();
            getRuleListUuid(inRuleList, inDeviceRulesList);
            if(CollectionUtils.isNotEmpty(inRuleList)){
                aclRuleMap.put("in",inRuleList);
            }
            List<String> outRuleList = new ArrayList<>();
            getRuleListUuid(outRuleList, outDeviceRulesList);
            if(CollectionUtils.isNotEmpty(outRuleList)){
                aclRuleMap.put("out",outRuleList);
            }
        }
        return aclRuleMap;
    }



    /**
     * ???????????????uuid
     * @param ruleList
     * @param deviceRulesList
     */
    private void getRuleListUuid(List<String> ruleList, List<DeviceFilterRuleListRO> deviceRulesList) {
        // ?????????????????????
        if (CollectionUtils.isNotEmpty(deviceRulesList)) {
            for (DeviceFilterRuleListRO deviceFilterRuleListRO : deviceRulesList) {
                if (ActionEnum.DENY.getKey().equalsIgnoreCase(deviceFilterRuleListRO.getAction())) {
                    String ruleListUuid = deviceFilterRuleListRO.getRuleListUuid();
                    if (!AliStringUtils.isEmpty(ruleListUuid)) {
                        ruleList.add(ruleListUuid);
                        ruleList.add(deviceFilterRuleListRO.getRuleId());
                        break;
                    }
                }
            }
        }
    }

    private List<PolicyInfoDTO> getPolicyList(List<DeviceFlowsRO> deviceFlowsROList, NodeEntity node,PathInfoTaskDTO task,PathDetailRO pathDetailRO,DeviceDetailRO deviceDetailRO) {
        List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
        for(DeviceFlowsRO deviceFlowsRO : deviceFlowsROList) {
            List<FlowListRO> deviceFlowList = deviceFlowsRO.getFlowList();
            if (deviceFlowList == null || deviceFlowList.size() == 0) {
                logger.info("???????????????...DeviceFlowsRO" + JSONObject.toJSONString(deviceFlowsRO));
                continue;
            }

            //???post???post?????????post?????????ruleListType????????????????????????DNAT???SNAT??????????????????????????????????????????????????????
            FlowListRO effectFlowListRO = deviceFlowList.get(0);
            if (isNginZ != null && isNginZ) {
                // ???????????????ruleListType??????????????????????????????????????????DNAT???SNAT???????????????????????????????????????SYSTEM__PRE_ROUTING_NAT
                if (!AliStringUtils.isEmpty(node.getModelNumber()) && node.getModelNumber().contains("Hillstone")) {
                    for (FlowListRO flowListRO : deviceFlowList) {
                        if ("SYSTEM__PRE_ROUTING_NAT".equals(flowListRO.getRuleListType())) {
                            effectFlowListRO = flowListRO;
                            break;
                        }
                    }
                }
            }else {
                if (!AliStringUtils.isEmpty(node.getModelNumber())) {
                    if (node.getModelNumber().contains("Hillstone")) {
                        //?????????????????????NAT???????????????NAT??????????????????
                    } else {
                        for (FlowListRO flowListRO : deviceFlowList) {
                            if (flowListRO.getRuleListType() == null) {
                                effectFlowListRO = flowListRO;
                            } else if (flowListRO.getRuleListType().equals("SYSTEM__POST_ROUTING_NAT")) {
                                effectFlowListRO = flowListRO;
                                break;
                            }
                        }
                    }
                } else {
                    for (FlowListRO flowListRO : deviceFlowList) {
                        if (flowListRO.getRuleListType() == null) {
                            effectFlowListRO = flowListRO;
                        } else if (flowListRO.getRuleListType().equals("SYSTEM__POST_ROUTING_NAT")) {
                            effectFlowListRO = flowListRO;
                            break;
                        }
                    }
                }
            }

            //???????????????nat??????
            for (FlowListRO flowListRO : deviceFlowList) {
                if (StringUtils.isNotBlank(flowListRO.getRuleListType()) && (flowListRO.getRuleListType().equals("SYSTEM__POST_ROUTING_NAT") || flowListRO.getRuleListType().equals("SYSTEM__PRE_ROUTING_NAT"))) {
                    task.setMatchNat(true);
                    break;
                }
            }

            List<PathFlowRO> pathFlowROList = effectFlowListRO.getTraffic();
            List<PolicyInfoDTO> list = getPolicyInfoListFromPathFlow(pathFlowROList, task,pathDetailRO,false);
            policyInfoList.addAll(list);
        }

        return policyInfoList;
    }

    List<PolicyInfoDTO> getPolicyInfoListFromPathFlow(List<PathFlowRO> pathFlowROList, PathInfoTaskDTO task,PathDetailRO pathDetailRO,boolean isRevert) {
        List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
        Integer ipType = task.getIpType();
        for(PathFlowRO pathFlowRO : pathFlowROList) {
            String srcIp,dstIp;
            if(ipType != null && IpTypeEnum.IPV6.getCode().equals(ipType)) {
                if (isRevert) {
                    srcIp = getIpList(pathFlowRO.getIp6SrcAddresses(), task.getDstIp());
                    dstIp = getIpList(pathFlowRO.getIp6DstAddresses(), task.getSrcIp());
                } else {
                    srcIp = getIpList(pathFlowRO.getIp6SrcAddresses(), task.getSrcIp());
                    dstIp = getIpList(pathFlowRO.getIp6DstAddresses(), task.getDstIp());
                }
            } else {
                if (isRevert) {
                    srcIp = getIpList(pathFlowRO.getIp4SrcAddresses(), task.getDstIp());
                    dstIp = getIpList(pathFlowRO.getIp4DstAddresses(), task.getSrcIp());
                } else {
                    srcIp = getIpList(pathFlowRO.getIp4SrcAddresses(), task.getSrcIp());
                    dstIp = getIpList(pathFlowRO.getIp4DstAddresses(), task.getDstIp());
                }

            }

            String protocolString = pathFlowRO.getProtocols() == null ? "0" : getSrcDstInteger(pathFlowRO.getProtocols());
            String dstPort = pathFlowRO.getDstPorts() == null ? null : getSrcDstInteger(pathFlowRO.getDstPorts());
            String srcPort = pathFlowRO.getSrcPorts() == null ? null : getSrcDstInteger(pathFlowRO.getSrcPorts());
            String[] protocols = protocolString.split(PolicyConstants.ADDRESS_SEPERATOR);

            for(String protocol: protocols) {
                List<ServiceDTO> serviceList = new ArrayList();
                ServiceDTO serviceDTO = new ServiceDTO();
                serviceDTO.setProtocol(protocol);
                serviceDTO.setDstPorts(dstPort);
                serviceDTO.setSrcPorts(srcPort);
                serviceList.add(serviceDTO);
                PolicyInfoDTO policyInfo = new PolicyInfoDTO();
                policyInfo.setSrcIp(srcIp);
                policyInfo.setDstIp(dstIp);
                policyInfo.setServiceList(serviceList);
                if(null != pathDetailRO){
                    policyInfo.setSnatName(pathDetailRO.getWhatIfSNatName());
                    policyInfo.setDnatName(pathDetailRO.getWhatIfDNatName());
                    policyInfo.setMatchType(null == pathDetailRO.getWhatIfMathType() ? null : pathDetailRO.getWhatIfMathType().toString());

                    List<ServiceDTO> matchPostServiceList = new ArrayList();
                    matchPostServiceList.add(serviceDTO);
                    policyInfo.setMatchPostServices(matchPostServiceList);
                }
                policyInfoList.add(policyInfo);
            }
        }
        return policyInfoList;
    }

    /**
     * ?????????????????????uuid
     * @param ruleListUuid ?????????RuleListUuid
     * @param node ????????????
     * @return ?????????RuleListUuid
     */
    private String getRuleListUuid(String ruleListUuid, NodeEntity node) {
        //???????????????????????????RuleListUUID
        String modelNumber = node.getModelNumber();

        //???????????????????????????????????????ruleListUuid
        if (AliStringUtils.isEmpty(modelNumber)) {
            return ruleListUuid;
        }

        if (modelNumber.equals("USG6000")) {
            return  whaleService.getHuaweiRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("JuniperSRX")) {
            return whaleService.getJuniperSrxRuleListUuid(node.getUuid());
        } else if (modelNumber.contains("DPTech Firewall")) {
            return whaleService.getDpTechRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("Cisco ASA")) {
            return whaleService.getCiscoRuleListUuid(node.getUuid(), taskService.getAclDirection(node.getUuid()));
        } else if (modelNumber.contains("Fortinet")) {
            return whaleService.getFortinetTechRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("Topsec TOS Firewall")) {
            return whaleService.getTopsecRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("H3C SecPath V7")) {
            return whaleService.getH3Cv7RuleListUuid(node.getUuid());
        }

        //???????????????????????????????????????????????????????????????RuleListUuid
        return ruleListUuid;
    }

    /**
     * ????????????
     * @param zoneRO ???????????????
     * @param interfaceName ????????????
     * @return ?????????
     */
    private String getZoneName(ZoneRO zoneRO, String interfaceName) {
        String zoneName = "";

        if(zoneRO == null) {
            return zoneName;
        }

        if(zoneRO.getData() == null) {
            return zoneName;
        }

        List<ZoneDataRO> zoneList = zoneRO.getData();

        if(zoneList.size() == 0 ) {
            return zoneName;
        }

        for(ZoneDataRO zoneDataRO: zoneList) {
            List<String> interfaceNameList = zoneDataRO.getInterfaceNames();

            if(interfaceNameList == null || interfaceNameList.size() == 0) {
                continue;
            }

            for(String name:interfaceNameList) {
                if(name.equals(interfaceName)) {
                    zoneName = zoneDataRO.getName();
                    return zoneName;
                }
            }
        }
        return zoneName;
    }


    /**
     * ????????????NAT??????
     * @param deviceDetailRO ??????????????????
     * @return ??????NAT??????
     */
    private DeviceNatInfoDTO getDeivceNatInfo(DeviceDetailRO deviceDetailRO, String deviceUuid){
        DeviceNatInfoDTO deviceNatInfoDTO = new DeviceNatInfoDTO();
        return deviceNatInfoDTO;
    }

    /**
     * ???whale?????????IP???????????????srcDstStringDTO?????????????????????????????????
     * @param ipList
     * @param srcIpStr
     * @return
     */
    private String getIpList(List<SrcDstStringDTO> ipList, String srcIpStr) {


        StringBuilder ipSb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(ipList)) {
            for (SrcDstStringDTO ipDTO : ipList) {
                ipSb.append(",");
                String startIp = ipDTO.getStart();
                String endIp = ipDTO.getEnd();
                if (startIp.equals(endIp)) {
                    ipSb.append(startIp);
                } else {
                    String originalIp = null;
                    if (StringUtils.isNotBlank(srcIpStr)) {
                        String[] ips = srcIpStr.split(",");

                        for (String ip : ips) {
                            if (IpUtils.isIPSegment(ip)) {
                                String start = IpUtils.getStartIpFromIpAddress(ip);
                                String end = IpUtils.getEndIpFromIpAddress(ip);
                                if (start.equals(startIp) && end.equals(endIp)) {
                                    originalIp = ip;
                                }
                            }
                        }
                    }

                    if (originalIp == null) {
                        ipSb.append(String.format("%s-%s", startIp, endIp));
                    } else {
                        ipSb.append(originalIp);
                    }
                }
            }
        }
        if (ipSb.length() > 1) {
            ipSb.deleteCharAt(0);
        }

        return ipSb.toString();
    }

    String getSrcDstInteger(List<SrcDstIntegerDTO> list) {
        StringBuilder sb = new StringBuilder();
        for (SrcDstIntegerDTO srcDstIntegerDTO : list) {
            sb.append(",");
            Integer start = srcDstIntegerDTO.getStart();
            Integer end = srcDstIntegerDTO.getEnd();
            if(start.equals(end)) {
                sb.append(String.valueOf(start));
            }else {
                sb.append(String.format("%s-%s", String.valueOf(start), String.valueOf(end)));
            }
        }
        if(sb.length() > 1) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }
    @Override
    public List<PolicyInfoDTO> mergePolicyInfoList(List<PolicyInfoDTO> policyInfoList) {
        List<PolicyInfoDTO> list = new ArrayList<>();
        for(PolicyInfoDTO newPolicyInfo : policyInfoList) {
            boolean canMerge = false;
            for(PolicyInfoDTO policyInfo : list) {
                if(policyInfo.getSrcIp().equals(newPolicyInfo.getSrcIp()) &&
                policyInfo.getDstIp().equals(newPolicyInfo.getDstIp())) {
                    List<ServiceDTO> serviceList = policyInfo.getServiceList();
                    List<ServiceDTO> newServiceList = newPolicyInfo.getServiceList();
                    serviceList = mergeServiceList(serviceList, newServiceList);
                    policyInfo.setServiceList(serviceList);
                    canMerge = true;
                }
            }

            if(!canMerge) {
                list.add(newPolicyInfo);
            }
        }
        return list;
    }


    String mergePort(String portListString, String newPortListString) {
        //?????????????????????null?????????????????????any????????????????????????
        if(portListString == null || newPortListString == null) {
            return null;
        }

        String[] portStrings = portListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        String[] newPortStrings = newPortListString.split(PolicyConstants.ADDRESS_SEPERATOR);

        List<PortValue> portValueList = new ArrayList<>();
        List<PortValue> newPortValueList = new ArrayList<>();

        for(String portString : portStrings) {
            PortValue portValue = new PortValue(portString);
            portValueList.add(portValue);
        }

        for(String newPortString: newPortStrings) {
            PortValue portValue = new PortValue(newPortString);
            newPortValueList.add(portValue);
        }

        for(PortValue newPortValue : newPortValueList) {
            List<PortValue> mergedPortList = new ArrayList<>();
            for(PortValue portValue: portValueList) {
                if(newPortValue.getEnd() < (portValue.getStart() - 1)) {
                    //????????????????????????????????????????????????????????????
                } else if ((newPortValue.getStart() - 1) > portValue.getEnd()) {
                    //????????????????????????????????????????????????????????????
                } else {
                    //????????????

                    //?????????????????????
                    if(portValue.getStart() < newPortValue.getStart()) {
                        newPortValue.setStart(portValue.getStart());
                    }

                    //?????????????????????
                    if(portValue.getEnd() > newPortValue.getEnd()) {
                        newPortValue.setEnd(portValue.getEnd());
                    }

                    //?????????????????????????????????
                    mergedPortList.add(portValue);
                }
            }
            //???????????????????????????
            portValueList.removeAll(mergedPortList);
            //???????????????
            portValueList.add(newPortValue);
        }

        StringBuilder sb = new StringBuilder();
        for(PortValue port : portValueList) {
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
            sb.append(port.toString());
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    @Data
    class PortValue {
        int start;
        int end;

        public String toString() {
            if(start != end) {
                return String.format("%d-%d", start, end);
            } else {
                return String.format("%d", end);
            }
        }

        PortValue(String port) {
            if(PortUtils.isPortRange(port)) {
                String startString = PortUtils.getStartPort(port);
                String endString = PortUtils.getEndPort(port);
                start = Integer.valueOf(startString);
                end = Integer.valueOf(endString);
            } else {
                start = Integer.valueOf(port);
                end = Integer.valueOf(port);
            }
        }
    }

    List<ServiceDTO> mergeServiceList(List<ServiceDTO> serviceList, List<ServiceDTO> newServiceList) {
        for(ServiceDTO newService: newServiceList) {
            boolean hasSameProtocol = false;
            for(ServiceDTO service: serviceList) {
                //?????????????????????????????????
                if(service.getProtocol().trim().equals(newService.getProtocol().trim())) {
                    hasSameProtocol = true;
                    //??????icmp??????????????????????????????
                    if(service.getProtocol().equals(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                        break;
                    }
                    String dstPorts = service.getDstPorts();
                    String newDstPorts = newService.getDstPorts();
                    dstPorts = mergePort(dstPorts, newDstPorts);
                    service.setDstPorts(dstPorts);
                }
            }
            if(hasSameProtocol== false){
                serviceList.add(newService);
            }
        }

        return serviceList;
    }

    @Data
    class IpValue {
        long start;
        long end;
        String ipString;

        public String toString() {
            if(start != end) {
                return String.format("%s-%s", IpUtils.IPv4NumToString(start), IpUtils.IPv4NumToString(end));
            } else {
                return String.format("%s", IpUtils.IPv4NumToString(start));
            }
        }

        IpValue() {}

        IpValue(String ip) {
            ipString = ip;
            String startIp = IpUtils.getStartIpFromIpAddress(ip);
            String endIp = IpUtils.getEndIpFromIpAddress(ip);
            start = IpUtils.IPv4StringToNum(startIp);
            end = IpUtils.IPv4StringToNum(endIp);
        }
    }

    String getAddresses(String ipString, String originalIpString) {
        String[] ips = ipString.split(",");
        String[] originalIps = originalIpString.split(",");

        List<String> ipList = new ArrayList<>();
        List<String> originalList = new ArrayList<>();

        for(String ip:ips) {
            ipList.add(ip);
        }

        for(String ip:originalIps) {
            originalList.add(ip);
        }

        List<IpValue> originalIpValueList = new ArrayList<>();
        List<IpValue> ipValueList = new ArrayList<>();
        for(String ip: ipList) {
            IpValue ipValue = new IpValue(ip);
            ipValueList.add(ipValue);
        }

        for(String ip: originalList) {
            IpValue ipValue = new IpValue(ip);
            originalIpValueList.add(ipValue);
        }

        List<IpValue> total = new ArrayList<>();
        for(IpValue originalIpValue: originalIpValueList) {
            for(IpValue ipValue : ipValueList ) {
                if(ipValue.start <= originalIpValue.start && originalIpValue.end <= ipValue.end) {
                    List<IpValue> newIpList = new ArrayList<>();
                    if(ipValue.start < originalIpValue.start) {
                        String newIp = String.format("%s-%s", IpUtils.IPv4NumToString(ipValue.start), IpUtils.IPv4NumToString(originalIpValue.start-1));
//                        newIpList.add(newIp);
                        if(originalIpValue.start - 1 == ipValue.start) {
                            newIp = String.format("%s", IpUtils.IPv4NumToString(ipValue.start));
                        }
                        IpValue newIpValue = new IpValue();
                        newIpValue.setStart(ipValue.start);
                        newIpValue.setEnd(originalIpValue.start-1);
                        newIpValue.setIpString(newIp);
                        newIpList.add(newIpValue);
                    }

                    if(ipValue.end > originalIpValue.end) {
                        String newIp = String.format("%s-%s", IpUtils.IPv4NumToString(originalIpValue.end+1), IpUtils.IPv4NumToString(ipValue.end));

                        if(originalIpValue.end+1 == ipValue.end) {
                            newIp = String.format("%s", IpUtils.IPv4NumToString(ipValue.end));
                        }
//                        newIpList.add(newIp);
                        IpValue newIpValue = new IpValue();
                        newIpValue.setStart(originalIpValue.end+1);
                        newIpValue.setEnd(ipValue.end);
                        newIpValue.setIpString(newIp);
                        newIpList.add(newIpValue);
                    }
                    total.add(originalIpValue);

                    //????????????????????????????????????
                    ipValueList.remove(ipValue);
                    ipValueList.addAll(newIpList);
                    break;
                }
            }
        }

        //???????????????
        total.addAll(ipValueList);

        StringBuilder sb = new StringBuilder();
        for(IpValue ipValue: total) {
            sb.append(",");
            sb.append(ipValue.getIpString());
        }

        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }

        logger.debug("??????IP????????????" + ipString);
        logger.debug("??????IP????????????" + originalIpString);
        logger.debug("?????????IP????????????" + sb.toString());

        return sb.toString();
    }


    public void test() {
        ServiceDTO serviceDTO1= new ServiceDTO();
        serviceDTO1.setDstPorts("30002");
        serviceDTO1.setProtocol("6");
        List<ServiceDTO> serviceList1 = new ArrayList<>();
        serviceList1.add(serviceDTO1);
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setDstPorts("30002");
        serviceDTO2.setProtocol("6");
        List<ServiceDTO> serviceList2 = new ArrayList<>();
        serviceList2.add(serviceDTO2);

        PolicyInfoDTO policyInfo1 = new PolicyInfoDTO();
        policyInfo1.setSrcIp("192.168.1.1");
        policyInfo1.setDstIp("192.168.2.1");
        policyInfo1.setServiceList(serviceList1);
        PolicyInfoDTO policyInfo2 = new PolicyInfoDTO();
        policyInfo2.setSrcIp("192.168.1.1");
        policyInfo2.setDstIp("192.168.2.1");
        policyInfo2.setServiceList(serviceList2);

        List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
        policyInfoList.add(policyInfo1);
        policyInfoList.add(policyInfo2);
        System.out.println(JSONObject.toJSONString(policyInfoList));
        policyInfoList = mergePolicyInfoList(policyInfoList);
        System.out.println(JSONObject.toJSONString(policyInfoList));
    }



    RecommendPolicyEntity getPolicyEntity(PathInfoTaskDTO pathInfo, PolicyInfoDTO policyInfo, NodeEntity node, String ruleListUuid,
                                          ZoneDataRO srcZone, ZoneDataRO dstZone, List<String> srcItf, List<String> dstItf,
                                          boolean isVsys, String rootDeviceUuid, String vsysName, Integer idleTimeout,SimulationTaskDTO simulationTaskDTO,
                                          PolicyEnum policyType,String aclRuleListName,String aclRuleIds) {
        RecommendPolicyEntity policyEntity = new RecommendPolicyEntity();

        //????????????????????????????????????ID???task???PathInfoTaskDTO?????????id???pathInfoId??????taskId???taskId
        policyEntity.setTaskId(pathInfo.getTaskId());
        policyEntity.setPathInfoId(pathInfo.getId());

        //???????????????????????????????????????????????????
        policyEntity.setNode(node);
        policyEntity.setDeviceUuid(node.getUuid());

        //?????????????????????
        policyEntity.setSrcIp(policyInfo.getSrcIp());
        String dstIp;
        if (simulationTaskDTO.getTaskType() == PolicyConstants.IN2OUT_INTERNET_RECOMMEND && StringUtils.isBlank(simulationTaskDTO.getDstIp())) {
            dstIp = "";
        } else {
            dstIp = policyInfo.getDstIp();
        }
        policyEntity.setDstIp(dstIp);
        policyEntity.setService(ServiceDTOUtils.toString(policyInfo.getServiceList()));
        policyEntity.setPolicySource(policyInfo.getPolicySource());

        //?????????????????????????????????
        policyEntity.setStartTime(pathInfo.getStartTime());
        policyEntity.setEndTime(pathInfo.getEndTime());

        //???????????????RuleListUuid
        policyEntity.setRuleListUuid(ruleListUuid);
        //???????????????RuleListName
        policyEntity.setRuleListName(aclRuleListName);
        // ????????????ruleId
        policyEntity.setMatchRuleId(aclRuleIds);

        //????????????????????????
        policyEntity.setSrcZone(srcZone == null ? "" : srcZone.getName());
        policyEntity.setDstZone(dstZone == null ? "" : dstZone.getName());

        //??????????????????
        String srcInterface = srcItf.get(0);
        String dstInterface = dstItf.get(0);
        String srcInterfaceAlias = srcItf.get(1);
        String dstInterfaceAlias = dstItf.get(1);

        String modelNumber = node.getModelNumber();
        policyEntity.setInDevIf(srcInterface);
        policyEntity.setOutDevIf(dstInterface);
        //????????????/??????8.4????????????????????????????????????????????????????????????????????????
        if(!AliStringUtils.isEmpty(modelNumber) && (modelNumber.equals("Cisco ASA") || modelNumber.equals("Cisco ASA 8.4")  || modelNumber.equals("Cisco ASA 9.9"))) {
            policyEntity.setInDevIf(AliStringUtils.isEmpty(srcInterfaceAlias) ? srcInterface : srcInterfaceAlias);
            policyEntity.setOutDevIf(AliStringUtils.isEmpty(dstInterfaceAlias) ? dstInterface : dstInterfaceAlias);
        }

        //?????????????????????????????????????????????????????????UUID??????????????????
        policyEntity.setVsys(isVsys);
        policyEntity.setRootDeviceUuid(rootDeviceUuid);
        policyEntity.setVsysName(vsysName);

        policyEntity.setIdleTimeout(idleTimeout);
        policyEntity.setPolicyType(policyType);
        policyEntity.setSnatName(policyInfo.getSnatName());
        policyEntity.setDnatName(policyInfo.getDnatName());
        policyEntity.setMatchType(policyInfo.getMatchType());
        policyEntity.setMatchPreServices(CollectionUtils.isNotEmpty(policyInfo.getMatchPreServices()) ? JSONObject.toJSONString(policyInfo.getMatchPreServices()) : null);
        policyEntity.setMatchPostServices(CollectionUtils.isNotEmpty(policyInfo.getMatchPostServices()) ? JSONObject.toJSONString(policyInfo.getMatchPostServices()) : null);
        return policyEntity;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     * @param entityList ??????????????????
     * @return ??????????????????????????????
     */
    List<RecommendPolicyDTO> getPolicyDTOList(List<RecommendPolicyEntity> entityList) {
        List<RecommendPolicyDTO> dtoList = new ArrayList<>();
        for(RecommendPolicyEntity entity:entityList) {
            RecommendPolicyDTO dto = new RecommendPolicyDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setServiceList(ServiceDTOUtils.toList(entity.getService()));
            dto.setMatchPreServices(ServiceDTOUtils.toList(entity.getMatchPreServices()));
            dto.setMatchPostServices(ServiceDTOUtils.toList(entity.getMatchPostServices()));
            dtoList.add(dto);
        }
        return dtoList;
    }
}
