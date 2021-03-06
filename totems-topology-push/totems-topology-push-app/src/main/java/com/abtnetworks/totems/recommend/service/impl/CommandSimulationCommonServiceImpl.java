package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.constants.*;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendTaskPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.CommandTaskEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.CommandService;
import com.abtnetworks.totems.recommend.service.CommandSimulationCommonService;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;



/**
 * @author Administrator
 * @Title:
 * @Description: ????????????????????????????????????
 * @date 2020/6/29
 */
@Service
public class CommandSimulationCommonServiceImpl implements CommandSimulationCommonService {
    private final static Logger logger = LoggerFactory.getLogger(CommandSimulationCommonServiceImpl.class);

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    CommandTaskManager commandTaskManager;

    @Value("${ciscoEnable}")
    private Boolean ciscoEnable = true;


    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    AdvancedSettingService advancedSettingService;


    @Autowired
    CommandlineService commandlineService;

    @Autowired
    CommandService commandService;

    @Override
    @Deprecated
    public int getPushCommandTextByTask(SimulationTaskDTO task, UserInfoDTO userInfoDTO) {
        logger.info(String.format("??????(%d)[%s]:?????????????????????...", task.getId(), task.getTheme()));
        int taskId = task.getId();

        List<RecommendPolicyEntity> policyEntityList = policyRecommendTaskService.getMergedPolicyList(taskId);

        List<RecommendPolicyDTO> policyDTOList = new ArrayList<>();

        Map<String, Integer> deviceCountMap = new HashMap<>();
        for (RecommendPolicyEntity entity : policyEntityList) {
            RecommendPolicyDTO recommendPolicyDTO = new RecommendPolicyDTO();
            BeanUtils.copyProperties(entity, recommendPolicyDTO);
            // ???????????????????????????
            formatService(entity, recommendPolicyDTO);
            policyDTOList.add(recommendPolicyDTO);

            //???????????????????????????
            String deviceUuid = entity.getDeviceUuid();
            if (deviceUuid != null) {
                Integer count = deviceCountMap.get(entity.getDeviceUuid());
                if (count != null) {
                    count = count + 1;
                    deviceCountMap.put(deviceUuid, count);
                } else {
                    count = new Integer(1);
                    deviceCountMap.put(deviceUuid, count);
                }
            } else {
                logger.error("????????????UUID??????..." + JSONObject.toJSONString(entity));
            }
        }

        //??????????????????1?????????????????????????????????
        Map<String, Integer> deviceIndexMap = new HashMap<>();
        Set<String> keySet = deviceCountMap.keySet();
        for (String deviceUuid : keySet) {
            Integer count = deviceCountMap.get(deviceUuid);
            if (count > 1) {
                Integer index = 1;
                deviceIndexMap.put(deviceUuid, index);
            }
        }

        List<CommandTaskEntity> commandTaskEntityList = new ArrayList<>();
        for (RecommendPolicyDTO policyDTO : policyDTOList) {
            String deviceUuid = policyDTO.getDeviceUuid();
            NodeEntity nodeEntity = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);

            RecommendTaskPolicyDTO taskPolicyDTO = new RecommendTaskPolicyDTO();
            BeanUtils.copyProperties(policyDTO, taskPolicyDTO);

            taskPolicyDTO.setOrderNo(task.getTheme());
            Integer index = deviceIndexMap.get(deviceUuid);

            //???????????????????????????????????????
            DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
            if (deviceRO != null && deviceRO.getData() != null && deviceRO.getData().size() > 0) {
                DeviceDataRO deviceData = deviceRO.getData().get(0);
                if (deviceData.getIsVsys() != null) {
                    taskPolicyDTO.setVsys(deviceData.getIsVsys());
                    taskPolicyDTO.setVsysName(deviceData.getVsysName());
                }
            }

            //????????????????????????????????????????????????+1
            if (index != null) {
                taskPolicyDTO.setName(String.format("%s_%d", task.getTheme(), index));
                index = index + 1;
                deviceIndexMap.put(deviceUuid, index);
            } else {
                taskPolicyDTO.setName(task.getTheme());
            }
//            taskPolicyDTO.setSrcPort(policyDTO.getService().getSrcPorts());
//            taskPolicyDTO.setDstPort(policyDTO.getService().getDstPorts());
//            taskPolicyDTO.setProtocol(policyDTO.getService().getProtocol());
            taskPolicyDTO.setServiceList(policyDTO.getServiceList());
            taskPolicyDTO.setInDevIfAlias(policyDTO.getInDevIf());
            taskPolicyDTO.setOutDevIfAlias(policyDTO.getOutDevIf());
            taskPolicyDTO.setTaskId(task.getId());
            taskPolicyDTO.setDeviceUuid(policyDTO.getDeviceUuid());
            taskPolicyDTO.setStartTime(task.getStartTime());
            taskPolicyDTO.setEndTime(task.getEndTime());
            taskPolicyDTO.setDescription(task.getDescription());
            taskPolicyDTO.setRuleListUuid(policyDTO.getRuleListUuid());
            taskPolicyDTO.setIdleTimeout(policyDTO.getIdleTimeout());
            taskPolicyDTO.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);

            //??????????????????????????????
            //???????????????????????????????????????????????????????????????????????????
            if (AdvancedSettingsConstants.PARAM_INT_SET_NO_ZONE.equals(policyDTO.getSpecifyZone())) {
                logger.info(String.format("??????(%s)??????????????????...???????????????????????????", deviceUuid));
                //??????????????????????????????
                taskPolicyDTO.setSrcZone(null);
                taskPolicyDTO.setDstZone(null);
            } else if (AdvancedSettingsConstants.PARAM_INT_SET_SRC_ZONE.equals(policyDTO.getSpecifyZone())  ) {
                logger.info(String.format("??????(%s)??????????????????...???????????????????????????", deviceUuid));
                //?????????????????????
                taskPolicyDTO.setDstZone(null);
            } else if (AdvancedSettingsConstants.PARAM_INT_SET_DST_ZONE.equals(policyDTO.getSpecifyZone() ) ) {
                logger.info(String.format("??????(%s)?????????????????????...??????????????????????????????", deviceUuid));
                //??????????????????
                taskPolicyDTO.setSrcZone(null);
            } else {
                logger.info(String.format("??????(%s)???????????????????????????...???????????????????????????????????????", deviceUuid));
            }

            //?????????????????????????????????????????????ACL???????????????????????????????????????
            if (nodeEntity != null) {
                if (!AliStringUtils.isEmpty(nodeEntity.getModelNumber())) {
                    if (nodeEntity.getModelNumber().contains("Cisco")) {
                        if (AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION.equals(policyDTO.getAclDirection() )) {
                            logger.info(String.format("?????????%s?????????????????????????????????", taskPolicyDTO.getDeviceUuid()));
                            taskPolicyDTO.setInDevIf(null);
                            taskPolicyDTO.setInDevIfAlias(null);
                        } else {
                            logger.info(String.format("?????????%s?????????????????????????????????", taskPolicyDTO.getDeviceUuid()));
                            taskPolicyDTO.setOutDevIf(null);
                            taskPolicyDTO.setOutDevIfAlias(null);
                        }
                    }
                }
            }

            //??????????????????????????????????????????????????????
            if (AdvancedSettingsConstants.PARAM_INT_MERGE_RULE.equals(policyDTO.getCreatePolicy()) ) {
                logger.info("?????????????????????????????????");
                taskPolicyDTO.setMustCreateFlag(false);
            } else {
                logger.info("?????????????????????????????????");
            }

            //???????????????????????????????????????????????????
            if (AdvancedSettingsConstants.PARAM_INT_REFERENCE_CONTENT.equals(policyDTO.getCreateObject()) ) {
                logger.info("?????????????????????????????????");
                taskPolicyDTO.setCreateObjFlag(false);
            } else {
                logger.info("?????????????????????????????????...");
            }

            //???????????????????????????
            taskPolicyDTO.setMoveSeatEnum(MoveSeatEnum.values()[policyDTO.getMovePolicy()]);
            taskPolicyDTO.setSwapRuleNameId(policyDTO.getSpecificPosition());

            GenerateCommandDTO resultCommandLine = composeCommandline(taskPolicyDTO);
            String commandLine;
            String rollbackCommandLine;

            if (resultCommandLine != null && resultCommandLine.getResultCode() == 0) {
                commandLine = null == resultCommandLine ? "" : resultCommandLine.getCommandline();
                rollbackCommandLine = null == resultCommandLine ? "" : resultCommandLine.getRollbackCommandline();
            } else {
                commandLine = String.format("?????????????????????????????????");
                rollbackCommandLine = String.format("???????????????????????????????????????");
            }

            logger.debug(String.format("??????(%d)??????????????????:\n%s", taskPolicyDTO.getId(), resultCommandLine));
            CommandTaskEntity commandTaskEntity = new CommandTaskEntity();
            commandTaskEntity.setCommandline(commandLine);
            commandTaskEntity.setCommandlineRevert(rollbackCommandLine);

//            commandTaskEntity.setPolicyId(policyDTO.getId());
            commandTaskEntity.setDeviceUuid(policyDTO.getDeviceUuid());
            commandTaskEntity.setTaskId(policyDTO.getTaskId());
            commandTaskEntity.setTheme(task.getTheme());
            commandTaskEntity.setCreateTime(new Date());
            commandTaskEntity.setStatus(0);
            commandTaskEntity.setUserName(task.getUserName());
            commandTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
            if(StringUtils.isNotBlank(taskPolicyDTO.getRuleListName())
                    || StringUtils.isNotBlank(taskPolicyDTO.getMatchRuleId())){
                JSONObject json = new JSONObject();
                json.put("ruleListName", org.apache.commons.lang3.StringUtils.isBlank(taskPolicyDTO.getRuleListName()) ? "" : taskPolicyDTO.getRuleListName());
                json.put("matchRuleId", org.apache.commons.lang3.StringUtils.isBlank(taskPolicyDTO.getMatchRuleId()) ? "" : taskPolicyDTO.getMatchRuleId());
                commandTaskEntity.setMatchMsg(json.toJSONString());
            }
            commandTaskEntityList.add(commandTaskEntity);
        }

        //?????????????????????????????????
        Map<String, CommandTaskEditableEntity> deviceCommandMap = new HashMap<>();
        for (CommandTaskEntity entity : commandTaskEntityList) {
            String deviceUuid = entity.getDeviceUuid();
            if (deviceCommandMap.containsKey(deviceUuid)) {
                CommandTaskEditableEntity commandTaskEditableEntity = deviceCommandMap.get(deviceUuid);
                String command = commandTaskEditableEntity.getCommandline() + "\n\n"
                        + entity.getCommandline();
                String revertCommand = commandTaskEditableEntity.getCommandlineRevert() + "\n\n"
                        + entity.getCommandlineRevert();
                commandTaskEditableEntity.setCommandline(command);
                commandTaskEditableEntity.setCommandlineRevert(revertCommand);
            } else {
                CommandTaskEditableEntity commandTaskEditableEntity = new CommandTaskEditableEntity();
                commandTaskEditableEntity.setTaskId(taskId);
                commandTaskEditableEntity.setTheme(entity.getTheme());
                commandTaskEditableEntity.setUserName(entity.getUserName());
                commandTaskEditableEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
                commandTaskEditableEntity.setCommandline(entity.getCommandline());
                commandTaskEditableEntity.setCommandlineRevert(entity.getCommandlineRevert());
                commandTaskEditableEntity.setCreateTime(new Date());
                commandTaskEditableEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);
                commandTaskEditableEntity.setDeviceUuid(deviceUuid);
                commandTaskEditableEntity.setPushResult(entity.getPushResult() == null ? "" : entity.getPushResult());
                commandTaskEditableEntity.setMatchMsg(entity.getMatchMsg());
                if (userInfoDTO != null) {
                    commandTaskEditableEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                } else {
                    commandTaskEditableEntity.setBranchLevel("00");
                }
                deviceCommandMap.put(deviceUuid, commandTaskEditableEntity);
            }
        }

        List<CommandTaskEditableEntity> commandList = new ArrayList<>(deviceCommandMap.values());
        commandTaskManager.insertCommandEditableEntityList(commandList);

        policyRecommendTaskService.updatePathCmdStatusByTaskId(taskId, PolicyConstants.POLICY_INT_RECOMMEND_CMD_SUCCESS);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int reGenerateCommandLine(SimulationTaskDTO task, UserInfoDTO userInfoDTO) {
        logger.info(String.format("??????(%d)[%s]:???????????????????????????...", task.getId(), task.getTheme()));

        List<RecommendPolicyDTO> policyDTOList = task.getPolicyList();
        if (CollectionUtils.isEmpty(policyDTOList)) {
            return ReturnCode.POLICY_MSG_OK;
        }
        for (RecommendPolicyDTO policyDTO : policyDTOList) {
            // 1.????????????????????????
            NodeEntity node = policyRecommendTaskService.getTheNodeByUuid(policyDTO.getDeviceUuid());
            if (node == null) {
                logger.error("????????????uuid????????????????????????,uuid:" + policyDTO.getDeviceUuid());
                continue;
            }
            policyDTO.setNode(node);
            // 2.????????????????????????
            DeviceRO deviceRO = whaleManager.getDeviceByUuid(policyDTO.getDeviceUuid());
            DeviceDataRO deviceData = deviceRO.getData().get(0);
            if (deviceData.getIsVsys() != null) {
                policyDTO.setVsysName(deviceData.getVsysName());
                policyDTO.setVsys(deviceData.getIsVsys());
            }
            PolicyEnum type = null;
            DeviceModelNumberEnum modelNumber = DeviceModelNumberEnum.fromString(node.getModelNumber());
            switch (modelNumber) {
                case CISCO_IOS:
                case CISCO_NX_OS:
                case RUIJIE:
                case SRX:
                case SRX_NoCli:
                case JUNIPER_ROUTER:
                    type = PolicyEnum.ACL;
                    break;
                default:
                    type = PolicyEnum.SECURITY;
                    break;
            }
            policyDTO.setPolicyType(type);

        }
        task.setPolicyList(policyDTOList);
        return commandService.generateCommandline(task, userInfoDTO);
    }


    /**
     * ??????????????????????????????????????????????????????????????????
     * @param serviceList
     * @return
     */
    private List<ServiceDTO> formatService(List<ServiceDTO> serviceList) {
        if(serviceList != null) {
            for(ServiceDTO service : serviceList) {
                if(AliStringUtils.isEmpty(service.getSrcPorts())){
                    service.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
                }
                if(AliStringUtils.isEmpty(service.getDstPorts())) {
                    service.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
                }
            }
        } else {
            serviceList = new ArrayList<>();
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol("0");
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceList.add(serviceDTO);
        }

        return serviceList;
    }

    private void formatService(RecommendPolicyEntity entity, RecommendPolicyDTO recommendPolicyDTO) {
        if (StringUtils.isNotBlank(entity.getService())) {
            JSONArray array = JSONObject.parseArray(entity.getService());
            List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
            for (ServiceDTO service : serviceList) {
                if (AliStringUtils.isEmpty(service.getSrcPorts())) {
                    service.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
                }
                if (AliStringUtils.isEmpty(service.getDstPorts())) {
                    service.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
                }

            }
            recommendPolicyDTO.setServiceList(serviceList);
        } else {
            List<ServiceDTO> serviceList = new ArrayList<>();
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol("0");
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceList.add(serviceDTO);
            recommendPolicyDTO.setServiceList(serviceList);
        }
    }


    private GenerateCommandDTO composeCommandline(RecommendTaskPolicyDTO taskPolicyDTO) {
        logger.debug("???????????????????????????\n" + taskPolicyDTO.toString());

        String deviceUuid = taskPolicyDTO.getDeviceUuid();

        NodeEntity node = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            logger.error("????????????uuid????????????????????????,uuid:" + deviceUuid);
            return new GenerateCommandDTO();
        }
        logger.info("?????????????????????,?????????????????????????????????:" + JSON.toJSONString(node));
        String modelNumber = node.getModelNumber();
        DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
        deviceForExistObjDTO.setModelNumber(DeviceModelNumberEnum.fromString(modelNumber));
        deviceForExistObjDTO.setDeviceUuid(node.getUuid());

        logger.info(String.format("??????(%d)?????????[%s]????????????????????????...", taskPolicyDTO.getId(), taskPolicyDTO.getName()));

        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        deviceDTO.setVsys(taskPolicyDTO.isVsys());
        deviceDTO.setVsysName(taskPolicyDTO.getVsysName());
        deviceDTO.setModelNumber(DeviceModelNumberEnum.fromString(modelNumber));
        deviceDTO.setNodeEntity(node);

        deviceDTO.setDeviceUuid(taskPolicyDTO.getDeviceUuid());
        deviceDTO.setRuleListUuid(taskPolicyDTO.getRuleListUuid());
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        PolicyEnum type = null;
        if(!AliStringUtils.isEmpty(modelNumber) &&
                (modelNumber.equals("Cisco IOS") || modelNumber.equals("Cisco NX-OS") || modelNumber.equals("Ruijie RGOS"))) {
            // ????????????uuid
            type = PolicyEnum.ACL;
        }else{
            // ?????????????????????????????????????????????,????????????????????????(??????????????????????????????,??????????????????ACL)
            type = PolicyEnum.SECURITY;
        }
        policyDTO.setType(type);
        policyDTO.setDstIp(taskPolicyDTO.getDstIp());
        policyDTO.setSrcIp(taskPolicyDTO.getSrcIp());
        policyDTO.setDstItf(taskPolicyDTO.getOutDevIf());
        policyDTO.setDstZone(taskPolicyDTO.getDstZone());
        policyDTO.setIdleTimeout(taskPolicyDTO.getIdleTimeout());
        policyDTO.setServiceList(taskPolicyDTO.getServiceList());
        policyDTO.setSrcZone(taskPolicyDTO.getSrcZone());
        policyDTO.setSrcItf(taskPolicyDTO.getInDevIf());
        policyDTO.setDstItfAlias(taskPolicyDTO.getOutDevIfAlias());
        policyDTO.setSrcItfAlias(taskPolicyDTO.getInDevIfAlias());
        policyDTO.setDescription(taskPolicyDTO.getDescription());
        PolicyMergeDTO policyMergeDTO = taskPolicyDTO.getMergeDTO();
        if (policyMergeDTO != null) {
            policyDTO.setMergeValue(policyMergeDTO.getMergeField());
        }
        policyDTO.setAction(ActionEnum.PERMIT);
        TaskDTO task = cmdDTO.getTask();
        task.setTheme(taskPolicyDTO.getName());
        task.setTaskId(taskPolicyDTO.getTaskId());
        task.setId(taskPolicyDTO.getId());

        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        if (DeviceModelNumberEnum.isRangeHillStoneCode(deviceModelNumberEnum.getCode())) {
            String paramValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(paramValue)) {
                if (paramValue.equals("0")) {
                    cmdDTO.getSetting().setRollbackType(true);
                } else {
                    cmdDTO.getSetting().setRollbackType(false);
                }
            }
        }
        GenerateCommandDTO generateCommandDTO = commandlineService.generateCommand(cmdDTO);
        taskPolicyDTO.setRuleListUuid(cmdDTO.getDevice().getRuleListUuid());
        taskPolicyDTO.setRuleListName(cmdDTO.getDevice().getRuleListName());
        return generateCommandDTO;
    }


    @Override
    public void addPolicyToTask(SimulationTaskDTO task, List<RecommendPolicyDTO> policyList) {
        logger.info(String.format("??????(%d)[%s]??????%d:???????????????????????????...", task.getId(), task.getTheme(), policyList.get(0).getPathInfoId()));
        Map<String, List<RecommendPolicyDTO>> map = task.getDevicePolicyMap();
        if (map == null) {
            map = new HashMap<>();
        }
        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        if (task.getTaskType() == PolicyConstants.BIG_INTERNET_RECOMMEND) {
            this.bigNetRangeReplace(task, policyList, map);
        } else {
            // ???????????????????????????
            List<ServiceDTO> taskServiceList = task.getServiceList();
            for (RecommendPolicyDTO policyDTO : policyList) {
                List<ServiceDTO> serviceDTOList = new LinkedList<>();
                AtomicInteger isBeyondCount = new AtomicInteger(0);
                policyDTO.setIpType(task.getIpType());
                // ?????????any?????????????????????????????????????????????any
                if (CollectionUtils.isEmpty(taskServiceList)) {
                    policyDTO.setServiceList(taskServiceList);
                } else {
                    for (ServiceDTO serviceDTO : policyDTO.getServiceList()) {
                        String protocol = serviceDTO.getProtocol();
                        boolean isBeyond = ProtocolUtils.isBeyondProtocol(protocol);
                        if (isBeyond) {
                            if (isBeyondCount.get() == 0) {
                                serviceDTO.setProtocol("0");
                                isBeyondCount.addAndGet(1);
                                serviceDTOList.add(serviceDTO);
                            }
                        } else {
                            serviceDTOList.add(serviceDTO);
                        }
                    }
                    policyDTO.setServiceList(serviceDTOList);
                }
                if (map.containsKey(policyDTO.getDeviceUuid())) {

                    map.get(policyDTO.getDeviceUuid()).add(policyDTO);
                } else {
                    List<RecommendPolicyDTO> list = new ArrayList<>();

                    list.add(policyDTO);
                    map.put(policyDTO.getDeviceUuid(), list);
                }
            }
        }
        task.setDevicePolicyMap(map);
    }

    /**
     * ????????????????????????????????????????????????????????????
     * @param task
     * @param policyList
     * @param map
     */
    @Override
    public void bigNetRangeReplace(SimulationTaskDTO task, List<RecommendPolicyDTO> policyList, Map<String, List<RecommendPolicyDTO>> map){
        List<ServiceDTO> taskServiceList = task.getServiceList();
        String taskSrcIp = task.getSrcIp();
        String taskDstIp = task.getDstIp();

        // ??????????????????????????????????????????
        for(RecommendPolicyDTO policyDTO:policyList) {

            if (CollectionUtils.isNotEmpty(taskServiceList)) {
                policyDTO.setServiceList(taskServiceList);
            }else{
                //???????????????????????????
            }
            if(StringUtils.isNotBlank(taskSrcIp)){
                policyDTO.setSrcIp(taskSrcIp);
            }else{
                // ?????????ip???????????????
            }

            if(StringUtils.isNotBlank(taskDstIp)){
                policyDTO.setDstIp(taskDstIp);
            }else{
                //?????????ip???????????????
            }
            //????????????+????????????????????????????????????????????????
            if (map.containsKey(policyDTO.getDeviceUuid())) {

                map.get(policyDTO.getDeviceUuid()).add(policyDTO);
            } else {
                List<RecommendPolicyDTO> list = new ArrayList<>();
                StringBuffer stringBuffer = new StringBuffer(policyDTO.getDeviceUuid())
                        .append(policyDTO.getSrcZone()).append(policyDTO.getInDevIf())
                        .append(policyDTO.getDstZone()).append(policyDTO.getOutDevIf());

                list.add(policyDTO);
                map.put(stringBuffer.toString(), list);
            }
        }
    }

    @Override
    public Boolean convertDomainToIp (SimulationTaskDTO task,List<String> dscIpList, Map<String,String> domainMap,StringBuilder isConvertFail) {
        String dstIpStr = task.getDstIp();
        //????????????ip??? ???????????????????????????

        Integer ipType = task.getIpType();

        Boolean hasDomain = false;
        if (StringUtils.isEmpty(dstIpStr)) {
            return hasDomain;
        }
        String[] dstInputIpStrs = dstIpStr.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String inputIp : dstInputIpStrs) {
            //????????????????????????????????? ??????????????????
            //??????????????????ipv4??????????????????????????????idv4???????????????????????????Ipv6??????????????????????????????ipv6?????????
            int rc ;
            if(ipType != null && ipType == 1){
                //ipv6
                rc = InputValueUtils.checkIpV6(inputIp);


                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
                    //ip?????????????????????????????????
                    List<String> ipStrs = IPUtil.converDomainToIp(inputIp, IPTypeEnum.IP6);
                    if (null == ipStrs || ipStrs.size() ==0) {
                        logger.error("???????????????" + inputIp + "??????????????????IP?????????");
                        isConvertFail.append("fail");
                    } else {
                        String domainDstStr = ipStrs.stream().collect(Collectors.joining(","));
                        domainMap.put(domainDstStr, inputIp);
                        hasDomain = true;
                    }
                } else {
                    dscIpList.add(inputIp);
                }
            } else {
                rc = InputValueUtils.checkIp(inputIp);
                //??????IP????????????????????????????????????????????????????????????
                if (rc == ReturnCode.INVALID_IP_RANGE) {
                    inputIp= InputValueUtils.autoCorrect(inputIp);
                    rc = ReturnCode.POLICY_MSG_OK;
                }

                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
                    //ip?????????????????????????????????
                    List<String> ipStrs = IPUtil.converDomainToIp(inputIp, IPTypeEnum.IP4);
                    if (null == ipStrs || ipStrs.size() ==0) {
                        logger.error("???????????????" + inputIp + "??????????????????IP?????????");
                        isConvertFail.append("fail");
                    } else {
                        String domainDstStr = ipStrs.stream().collect(Collectors.joining(","));
                        domainMap.put(domainDstStr, inputIp);
                        hasDomain = true;
                    }
                } else {
                    dscIpList.add(inputIp);
                }
            }

        }
        return hasDomain;
    }





}
