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
 * @Description: 抽离仿真中使用的公共方法
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
        logger.info(String.format("任务(%d)[%s]:开始生成命令行...", task.getId(), task.getTheme()));
        int taskId = task.getId();

        List<RecommendPolicyEntity> policyEntityList = policyRecommendTaskService.getMergedPolicyList(taskId);

        List<RecommendPolicyDTO> policyDTOList = new ArrayList<>();

        Map<String, Integer> deviceCountMap = new HashMap<>();
        for (RecommendPolicyEntity entity : policyEntityList) {
            RecommendPolicyDTO recommendPolicyDTO = new RecommendPolicyDTO();
            BeanUtils.copyProperties(entity, recommendPolicyDTO);
            // 为命令行生成做准备
            formatService(entity, recommendPolicyDTO);
            policyDTOList.add(recommendPolicyDTO);

            //统计设备生成策略数
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
                logger.error("生成策略UUID为空..." + JSONObject.toJSONString(entity));
            }
        }

        //策略数量大于1的设备才策略名才有序号
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

            //若为虚设备，设置虚设备信息
            DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
            if (deviceRO != null && deviceRO.getData() != null && deviceRO.getData().size() > 0) {
                DeviceDataRO deviceData = deviceRO.getData().get(0);
                if (deviceData.getIsVsys() != null) {
                    taskPolicyDTO.setVsys(deviceData.getIsVsys());
                    taskPolicyDTO.setVsysName(deviceData.getVsysName());
                }
            }

            //策略名称添加序号，添加完成后序号+1
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

            //命令行生成设置域信息
            //根据高级设置决定策略检查是否需要设置源域和目的域。
            if (AdvancedSettingsConstants.PARAM_INT_SET_NO_ZONE.equals(policyDTO.getSpecifyZone())) {
                logger.info(String.format("设备(%s)不指定域信息...命令行生成不指定域", deviceUuid));
                //源域目的域均设置为空
                taskPolicyDTO.setSrcZone(null);
                taskPolicyDTO.setDstZone(null);
            } else if (AdvancedSettingsConstants.PARAM_INT_SET_SRC_ZONE.equals(policyDTO.getSpecifyZone())  ) {
                logger.info(String.format("设备(%s)指定源域信息...命令行生成指定源域", deviceUuid));
                //目的域设置为空
                taskPolicyDTO.setDstZone(null);
            } else if (AdvancedSettingsConstants.PARAM_INT_SET_DST_ZONE.equals(policyDTO.getSpecifyZone() ) ) {
                logger.info(String.format("设备(%s)指定目的域信息...命令行生成指定目的域", deviceUuid));
                //源域设置为空
                taskPolicyDTO.setSrcZone(null);
            } else {
                logger.info(String.format("设备(%s)使用默认方式设置域...命令行生成指定源域和目的域", deviceUuid));
            }

            //只有思科设备根据高级设置决定是ACL策略设置到入接口还是出接口
            if (nodeEntity != null) {
                if (!AliStringUtils.isEmpty(nodeEntity.getModelNumber())) {
                    if (nodeEntity.getModelNumber().contains("Cisco")) {
                        if (AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION.equals(policyDTO.getAclDirection() )) {
                            logger.info(String.format("设备（%s）指定策略下发到出接口", taskPolicyDTO.getDeviceUuid()));
                            taskPolicyDTO.setInDevIf(null);
                            taskPolicyDTO.setInDevIfAlias(null);
                        } else {
                            logger.info(String.format("设备（%s）指定策略下发到入接口", taskPolicyDTO.getDeviceUuid()));
                            taskPolicyDTO.setOutDevIf(null);
                            taskPolicyDTO.setOutDevIfAlias(null);
                        }
                    }
                }
            }

            //全局设置为合并策略，则设置为合并策略
            if (AdvancedSettingsConstants.PARAM_INT_MERGE_RULE.equals(policyDTO.getCreatePolicy()) ) {
                logger.info("生成命令行优先合并策略");
                taskPolicyDTO.setMustCreateFlag(false);
            } else {
                logger.info("生成命令行优先创建策略");
            }

            //全局设置为优先引用内容还是创建对象
            if (AdvancedSettingsConstants.PARAM_INT_REFERENCE_CONTENT.equals(policyDTO.getCreateObject()) ) {
                logger.info("生成命令行优先引用内容");
                taskPolicyDTO.setCreateObjFlag(false);
            } else {
                logger.info("生成命令行优先创建对象...");
            }

            //设置策略移动的位置
            taskPolicyDTO.setMoveSeatEnum(MoveSeatEnum.values()[policyDTO.getMovePolicy()]);
            taskPolicyDTO.setSwapRuleNameId(policyDTO.getSpecificPosition());

            GenerateCommandDTO resultCommandLine = composeCommandline(taskPolicyDTO);
            String commandLine;
            String rollbackCommandLine;

            if (resultCommandLine != null && resultCommandLine.getResultCode() == 0) {
                commandLine = null == resultCommandLine ? "" : resultCommandLine.getCommandline();
                rollbackCommandLine = null == resultCommandLine ? "" : resultCommandLine.getRollbackCommandline();
            } else {
                commandLine = String.format("无法生成该设备的命令行");
                rollbackCommandLine = String.format("无法生成该设备的回滚命令行");
            }

            logger.debug(String.format("策略(%d)生成命令行为:\n%s", taskPolicyDTO.getId(), resultCommandLine));
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

        //合并命令行后保存到新表
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
        logger.info(String.format("任务(%d)[%s]:重新开始生成命令行...", task.getId(), task.getTheme()));

        List<RecommendPolicyDTO> policyDTOList = task.getPolicyList();
        if (CollectionUtils.isEmpty(policyDTOList)) {
            return ReturnCode.POLICY_MSG_OK;
        }
        for (RecommendPolicyDTO policyDTO : policyDTOList) {
            // 1.查询设备是否存在
            NodeEntity node = policyRecommendTaskService.getTheNodeByUuid(policyDTO.getDeviceUuid());
            if (node == null) {
                logger.error("根据设备uuid查询不到基础信息,uuid:" + policyDTO.getDeviceUuid());
                continue;
            }
            policyDTO.setNode(node);
            // 2.查询是否存在虚墙
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
     * 格式化服务对象列表，使得其符合命令行生成需求
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
        logger.debug("策略开通任务数据：\n" + taskPolicyDTO.toString());

        String deviceUuid = taskPolicyDTO.getDeviceUuid();

        NodeEntity node = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            logger.error("根据设备uuid查询不到基础信息,uuid:" + deviceUuid);
            return new GenerateCommandDTO();
        }
        logger.info("生成命令行之前,打印查询出来的设备信息:" + JSON.toJSONString(node));
        String modelNumber = node.getModelNumber();
        DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
        deviceForExistObjDTO.setModelNumber(DeviceModelNumberEnum.fromString(modelNumber));
        deviceForExistObjDTO.setDeviceUuid(node.getUuid());

        logger.info(String.format("任务(%d)工单号[%s]获取品牌相关数据...", taskPolicyDTO.getId(), taskPolicyDTO.getName()));

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
            // 查询设备uuid
            type = PolicyEnum.ACL;
        }else{
            // 如果前面流程没有获取到策略类型,则默认走安全策略(前面有走路由交换设备,这里类型就是ACL)
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
        logger.info(String.format("任务(%d)[%s]路径%d:添加策略到任务对象...", task.getId(), task.getTheme(), policyList.get(0).getPathInfoId()));
        Map<String, List<RecommendPolicyDTO>> map = task.getDevicePolicyMap();
        if (map == null) {
            map = new HashMap<>();
        }
        // 如果是大网段开通就需要将策略中五元组替换成页面输入的，放弃细节原则
        if (task.getTaskType() == PolicyConstants.BIG_INTERNET_RECOMMEND) {
            this.bigNetRangeReplace(task, policyList, map);
        } else {
            // 取出工单里面的服务
            List<ServiceDTO> taskServiceList = task.getServiceList();
            for (RecommendPolicyDTO policyDTO : policyList) {
                List<ServiceDTO> serviceDTOList = new LinkedList<>();
                AtomicInteger isBeyondCount = new AtomicInteger(0);
                policyDTO.setIpType(task.getIpType());
                // 服务为any时在生成的策略建议中都被替换为any
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
     * 大网段开通替换每一个设备的策略五元组参数
     * @param task
     * @param policyList
     * @param map
     */
    @Override
    public void bigNetRangeReplace(SimulationTaskDTO task, List<RecommendPolicyDTO> policyList, Map<String, List<RecommendPolicyDTO>> map){
        List<ServiceDTO> taskServiceList = task.getServiceList();
        String taskSrcIp = task.getSrcIp();
        String taskDstIp = task.getDstIp();

        // 对每个设备的参数都改成大范围
        for(RecommendPolicyDTO policyDTO:policyList) {

            if (CollectionUtils.isNotEmpty(taskServiceList)) {
                policyDTO.setServiceList(taskServiceList);
            }else{
                //没有服务该页面校验
            }
            if(StringUtils.isNotBlank(taskSrcIp)){
                policyDTO.setSrcIp(taskSrcIp);
            }else{
                // 没有源ip该页面校验
            }

            if(StringUtils.isNotBlank(taskDstIp)){
                policyDTO.setDstIp(taskDstIp);
            }else{
                //没有源ip该页面校验
            }
            //以（设备+进出域接口）维度进行设备策略合并
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
        //目的地址ip组 重新解析域名的情况

        Integer ipType = task.getIpType();

        Boolean hasDomain = false;
        if (StringUtils.isEmpty(dstIpStr)) {
            return hasDomain;
        }
        String[] dstInputIpStrs = dstIpStr.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String inputIp : dstInputIpStrs) {
            //内到外，源是明确的必填 目的可以不填
            //如果选择的是ipv4，则目的地址只能包含idv4或者域名，选择的是Ipv6，则目的地址只能包含ipv6和域名
            int rc ;
            if(ipType != null && ipType == 1){
                //ipv6
                rc = InputValueUtils.checkIpV6(inputIp);


                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
                    //ip校验不通过的默认为域名
                    List<String> ipStrs = IPUtil.converDomainToIp(inputIp, IPTypeEnum.IP6);
                    if (null == ipStrs || ipStrs.size() ==0) {
                        logger.error("输入的域名" + inputIp + "解析不到对应IP地址！");
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
                //若出IP范围起始地址大于终止地址错误，则自动纠正
                if (rc == ReturnCode.INVALID_IP_RANGE) {
                    inputIp= InputValueUtils.autoCorrect(inputIp);
                    rc = ReturnCode.POLICY_MSG_OK;
                }

                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
                    //ip校验不通过的默认为域名
                    List<String> ipStrs = IPUtil.converDomainToIp(inputIp, IPTypeEnum.IP4);
                    if (null == ipStrs || ipStrs.size() ==0) {
                        logger.error("输入的域名" + inputIp + "解析不到对应IP地址！");
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
