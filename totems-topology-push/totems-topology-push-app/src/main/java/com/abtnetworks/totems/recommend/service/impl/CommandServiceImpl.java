package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.auto.enums.PushNatTypeEnum;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendTaskPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.CommandService;
import com.abtnetworks.totems.recommend.service.RecommendRelevanceSceneService;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CommandServiceImpl implements CommandService {

    private static Logger logger = Logger.getLogger(CommandServiceImpl.class);

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    CommandTaskManager commandTaskManager;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    CommandlineService commandlineService;

    @Autowired
    VendorManager vendorManager;

    @Autowired
    Map<String, CmdService> cmdServiceMap;

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Autowired
    RecommendRelevanceSceneService recommendRelevanceSceneService;

    @Value("${push.open-convert-address:false}")
    private boolean open_convert_address;


    @Override
    public int generateCommandline(SimulationTaskDTO task, UserInfoDTO userInfoDTO){
        logger.info(String.format("任务(%d)[%s]开始生成命令行...", task.getId(), task.getTheme()));
        int taskId = task.getId();

        List<RecommendPolicyDTO> policyDTOList = task.getPolicyList();

        Set<String> deviceSet = new HashSet<>();
        Map<String, Integer> deviceIndexMap = new HashMap<>();
        Map<String, CommandTaskEditableEntity> deviceCommandMap = new HashMap<>();
        List<RecommendPolicyDTO> disasterRecoveryDTOList = new ArrayList<>();
        List<RecommendPolicyEntity> policyList = new ArrayList<>();
        for(RecommendPolicyDTO dto : policyDTOList) {

            //格式化serviceList，为生成命令行做准备
            dto.setServiceList(formatService(dto.getServiceList()));

            //统计设备生成策略数，多于一条的需要更在主题名上增加下划线和编号，以免多条策略名称重复
            //此处对于多余一条命令行生成的设备，生成一个设备，名称序号计数器map，用于后续对策略名称进行编号
            String deviceUuid = dto.getDeviceUuid();
            // 判断是否配置灾备
            List<RecommendPolicyDTO> disasterRecoveryDTOLists =  setDisasterRecovery(dto);
            if(CollectionUtils.isNotEmpty(disasterRecoveryDTOLists)){
                disasterRecoveryDTOList.addAll(disasterRecoveryDTOLists);
                for (RecommendPolicyDTO recoveryDTOList : disasterRecoveryDTOLists) {
                    RecommendPolicyEntity policyEntity = new RecommendPolicyEntity();
                    BeanUtils.copyProperties(recoveryDTOList, policyEntity);
                    policyList.add(policyEntity);
                }
            }

            if(deviceSet.contains(deviceUuid)) {
                deviceIndexMap.put(deviceUuid, 1);
            } else {
                deviceSet.add(deviceUuid);
            }
        }
        // 添加灾备设备策略
        if(CollectionUtils.isNotEmpty(disasterRecoveryDTOList)){
            policyDTOList.addAll(disasterRecoveryDTOList);
            policyRecommendTaskService.addMergedPolicyList(policyList);

        }
        Map<String,Map<Integer, AtomicInteger>> editPolicyTotalMap = new HashMap<>();

        String[] srcIps = org.apache.commons.lang3.StringUtils.isNotBlank(task.getSrcIp()) ? task.getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR) : null;
        String[] dstIps = org.apache.commons.lang3.StringUtils.isNotBlank(task.getDstIp()) ? task.getDstIp().split(PolicyConstants.ADDRESS_SEPERATOR) : null;

        for(RecommendPolicyDTO policyDTO : policyDTOList) {
            // 如果没有匹配到nat And开关打开 才会执行过滤流程
            if (!task.getMatchNat() && open_convert_address) {
                logger.info(String.format("工单原地址过数据流之前policys为:%s", JSONObject.toJSONString(policyDTO)));

                // 原始子网若未被刨除，需要还原成工单所填的源子网地址
                Map<String,String> filterMap = this.filterRuleDataByTaskAddress(srcIps, dstIps, policyDTO.getSrcIp(),policyDTO.getDstIp());
                policyDTO.setSrcIp(filterMap.get(PolicyConstants.SRC));
                policyDTO.setDstIp(filterMap.get(PolicyConstants.DST));
                logger.info(String.format("工单原地址过数据流之后policys为:%s", JSONObject.toJSONString(policyDTO)));
            }


            String deviceUuid = policyDTO.getDeviceUuid();
            String branchLevel;
            if(userInfoDTO != null ){
                branchLevel  = userInfoDTO.getBranchLevel();
            }else{
                branchLevel = "00";
            }
            String theme = task.getTheme();
            //策略名称添加序号，添加完成后序号+1
            if(deviceIndexMap.keySet().contains(deviceUuid)) {
                theme = String.format("%s_%d", task.getTheme(), deviceIndexMap.get(deviceUuid));
                deviceIndexMap.put(deviceUuid, deviceIndexMap.get(deviceUuid) + 1);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
            String startTimeString = policyDTO.getStartTime()==null?null:sdf.format(policyDTO.getStartTime());
            String endTimeString = policyDTO.getEndTime()==null?null:sdf.format(policyDTO.getEndTime());

            PolicyEnum type = policyDTO.getPolicyType();
            // 如果前面流程没有获取到策略类型,则默认走安全策略(前面有走路由交换设备,这里类型就是ACL)
            if(null == type){
                type = PolicyEnum.SECURITY;
            }

            CmdDTO cmdDTO = EntityUtils.createCmdDTO(type, null, policyDTO.getTaskId(), deviceUuid,
                    theme, task.getUserName(), policyDTO.getSrcIp(), policyDTO.getDstIp(), null, null, policyDTO.getServiceList(),
                    null, policyDTO.getSrcZone(), policyDTO.getDstZone(), policyDTO.getInDevIf(), policyDTO.getOutDevIf(),
                    policyDTO.getInDevIf(), policyDTO.getOutDevIf(), startTimeString, endTimeString, task.getDescription(),
                    ActionEnum.PERMIT, policyDTO.isVsys(), policyDTO.getVsysName(), MoveSeatEnum.FIRST, null, policyDTO.getRuleListUuid(),
                    policyDTO.getIdleTimeout(), task.getSrcIpSystem(), task.getDstIpSystem(),task.getIpType(),null,null,null,null,null,null,null);
            //设置是否含域名对象
            cmdDTO.getPolicy().setPolicySource(policyDTO.getPolicySource());
            TaskDTO taskDto = cmdDTO.getTask();
            taskDto.setTaskTypeEnum(TaskTypeEnum.SERVICE_TYPE);
            taskDto.setMergeCheck(task.getMergeCheck());
            taskDto.setBeforeConflict(task.getBeforeConflict());
            DeviceDTO device =cmdDTO.getDevice();
            device.setMatchRuleId(policyDTO.getMatchRuleId());
            device.setRuleListName(policyDTO.getRuleListName());

            String  modelNumber = policyDTO.getNode().getModelNumber();
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

            // 飞塔设备做特殊处理,如果径路计算需要开通飞塔设备的安全策略,且该工单关联了nat策略就不生成安全策略命令行(因为此时安全策略被nat策略覆盖 生成安全策略没意义,
            // 但是策略建议得生成 所以在生成命令行这个地方做处理)
            if (DeviceModelNumberEnum.isRangeFortCode(DeviceModelNumberEnum.fromString(policyDTO.getNode().getModelNumber()).getCode())) {
                specialDealForFortinet(task, policyDTO, cmdDTO);
            }

            GenerateCommandDTO simulationCmdDTO = commandlineService.generateCommand(cmdDTO);

            // 构建和统计可编辑信息(eg: {"srcIp":1,"dstIp":1})
            buildEditPolicyTotalMap(editPolicyTotalMap, deviceUuid, cmdDTO);
            // 统计错误信息(只针对思科的acl)
            String errorMsg = null == cmdDTO.getBusinessInfoDTO() ? "" : cmdDTO.getBusinessInfoDTO().getRuleIdNotEnoughMsg();
            //建立命令行生成对象
            buildDeviceCommandMap(task, taskId, deviceCommandMap, deviceUuid, branchLevel, device, simulationCmdDTO, errorMsg, cmdDTO);
        }

        //遍历设备命令行Map添 加合并信息
        buildMergeInfoToEntity(deviceCommandMap, editPolicyTotalMap);

        //合并所有命令行对象到列表并存储
        List<CommandTaskEditableEntity> commandList = new ArrayList<>(deviceCommandMap.values());

        //根据错误信息是否有值来变更任务状态,如果有错误信息拼接到命令行里面,让后面编辑命令行去修改(只针对思科acl,如果error有值则是acl所返回)
        for (CommandTaskEditableEntity commandTaskEditableEntity : commandList) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(commandTaskEditableEntity.getErrorMsg())) {
                // 将errorMsg 拼接到命令行里面
                StringBuffer  commandLindSb = new StringBuffer();
                commandLindSb.append(commandTaskEditableEntity.getErrorMsg()).append("\n\n");
                commandLindSb.append(commandTaskEditableEntity.getCommandline());
                commandTaskEditableEntity.setCommandline(commandLindSb.toString());

                StringBuffer  rollbackCommandLindSb = new StringBuffer();
                rollbackCommandLindSb.append(commandTaskEditableEntity.getErrorMsg()).append("\n\n");
                rollbackCommandLindSb.append(commandTaskEditableEntity.getCommandlineRevert());
                commandTaskEditableEntity.setCommandlineRevert(rollbackCommandLindSb.toString());
            }
        }
        commandTaskManager.insertCommandEditableEntityList(commandList);
        //更新系统状态
        policyRecommendTaskService.updatePathCmdStatusByTaskId(taskId, PolicyConstants.POLICY_INT_RECOMMEND_CMD_SUCCESS);

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 拆分之后的数据流去过原工单地址的子网（如果拆分之后的数据流和工单所填的子网完全相同，则命令行生成的时候用工单所填的子网）
     * @param srcIps
     * @param dstIps
     * @param policySrcIp
     * @param policyDstIp
     * @throws Exception
     */
    @Override
    public Map<String, String> filterRuleDataByTaskAddress(String[] srcIps, String[] dstIps, String policySrcIp, String policyDstIp) {
        Map<String,String> resultMap = new HashMap<>();
        try {
            String[] policySrcIps = policySrcIp.split(PolicyConstants.ADDRESS_SEPERATOR);

            String[] policyDstIps = policyDstIp.split(PolicyConstants.ADDRESS_SEPERATOR);
            // 将工单的每个源ip按照逗号拆分之后再去过拆过之后的数据流
            if(null != srcIps){

                for (String itemSrcIp : srcIps) {
                    List<String> targetIps = new ArrayList<>();
                    targetIps.add(itemSrcIp);

                    QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(targetIps, Arrays.asList(policySrcIps));
                    // 取交集
                    List<String> filterData = tmp.getFilterOutData();
                    // 如果没有交集 直接跳出 进行下一次
                    if (CollectionUtils.isEmpty(filterData)) {
                        continue;
                    }
                    // 如果有交集，判断交集是不是和当前工单的源地址中的其中一个地址相等
                    QuintupleUtils.Tmp<String> sameTmp = QuintupleUtils.ipListFilter(targetIps, filterData);
                    // 如果取交集之后的结果再去和源工单数据进行比较，如果有交集且没有剩余（也就是完全相等）这个时候将工单的地址完全替换到policy
                    if (CollectionUtils.isEmpty(sameTmp.getPostFilterData()) && CollectionUtils.isNotEmpty(sameTmp.getFilterOutData())) {
                        StringBuffer sb = new StringBuffer();
                        for (String itemData : filterData) {
                            String[] startEnd = QuintupleUtils.ipv46toIpStartEnd(itemData);
                            sb.append(startEnd[0]);
                            sb.append(PolicyConstants.VALUE_RANGE_SEPERATOR);
                            sb.append(startEnd[1]);
                        }
                        policySrcIp = policySrcIp.replace(sb.toString(), itemSrcIp);
                    }
                }
            }
            resultMap.put(PolicyConstants.SRC,policySrcIp);

            // 将工单的每个目的ip按照逗号拆分之后再去过拆过之后的数据流
            if(null != dstIps){

                for (String itemDstIp : dstIps) {
                    List<String> targetIps = new ArrayList<>();
                    targetIps.add(itemDstIp);

                    QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(targetIps, Arrays.asList(policyDstIps));
                    // 取交集
                    List<String> filterData = tmp.getFilterOutData();
                    // 如果没有交集 直接跳出 进行下一次
                    if (CollectionUtils.isEmpty(filterData)) {
                        continue;
                    }
                    // 如果有交集，判断交集是不是和当前工单的源地址中的其中一个地址相等
                    QuintupleUtils.Tmp<String> sameTmp = QuintupleUtils.ipListFilter(targetIps, filterData);
                    // 如果取交集之后的结果再去和源工单数据进行比较，如果有交集且没有剩余（也就是完全相等）这个时候将工单的地址完全替换到policy
                    if (CollectionUtils.isEmpty(sameTmp.getPostFilterData()) && CollectionUtils.isNotEmpty(sameTmp.getFilterOutData())) {
                        StringBuffer sb = new StringBuffer();
                        for (String itemData : filterData) {
                            String[] startEnd = QuintupleUtils.ipv46toIpStartEnd(itemData);
                            sb.append(startEnd[0]);
                            sb.append(PolicyConstants.VALUE_RANGE_SEPERATOR);
                            sb.append(startEnd[1]);
                        }
                        policyDstIp = policyDstIp.replace(sb.toString(), itemDstIp);
                    }
                }
            }
            resultMap.put(PolicyConstants.DST,policyDstIp);
        } catch (Exception e) {
            logger.error("数据流过滤原地址流程异常,异常原因:{}", e);
        }
        return resultMap;
    }

    /**
     * 飞塔关联nat特殊处理
     * @param task
     * @param policyDTO
     * @return
     */
    private void specialDealForFortinet(SimulationTaskDTO task, RecommendPolicyDTO policyDTO, CmdDTO cmdDTO) {
        logger.info(String.format("当前策略建议经过飞塔设备,且该策略建议所对应的工单关联了飞塔nat策略,构建匹配参数:%s", JSONObject.toJSONString(policyDTO)));
        // 根据关联的nat查询已经生成的nat的命令行和回滚命令行
        if(org.apache.commons.lang3.StringUtils.isNotBlank(policyDTO.getMatchType())) {
            // 都没有匹配上
            if (PolicyConstants.MATCH_NOTHING.equalsIgnoreCase(policyDTO.getMatchType())) {
                return;
            }
            // 匹配上存量nat
            if (PolicyConstants.MATCH_EXIST.equalsIgnoreCase(policyDTO.getMatchType())) {
                cmdDTO.getExistObject().setPostSrcAddressObjectName(policyDTO.getSnatName());
                cmdDTO.getPolicy().setExistVirtualIpName(policyDTO.getDnatName());
                cmdDTO.getPolicy().setExistDnat(org.apache.commons.lang3.StringUtils.isNotBlank(policyDTO.getDnatName()) ? true : false);
                if(org.apache.commons.lang3.StringUtils.isNotBlank(policyDTO.getDnatName())){
                    if(CollectionUtils.isNotEmpty(policyDTO.getMatchPreServices())){
                        cmdDTO.getPolicy().setServiceList(policyDTO.getMatchPreServices());
                    }
                    if(CollectionUtils.isNotEmpty(policyDTO.getMatchPostServices())){
                        cmdDTO.getPolicy().setPostServiceList(policyDTO.getMatchPostServices());
                    }
                }
                return;
            }
            // 匹配上whatIf中的nat 或者存量和whatIf都匹配上
            if (PolicyConstants.MATCH_WHATIF.equalsIgnoreCase(policyDTO.getMatchType()) || PolicyConstants.MATCH_EXIST_AND_WHATIF.equalsIgnoreCase(policyDTO.getMatchType())) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(policyDTO.getSnatName())) {
                    buildParam(task, cmdDTO, policyDTO.getSnatName(), policyDTO.getMatchType(),PushNatTypeEnum.NAT_TYPE_S.getCode(),policyDTO.getMatchPreServices(),policyDTO.getMatchPostServices());
                }
                if (org.apache.commons.lang3.StringUtils.isNotBlank(policyDTO.getDnatName())) {
                    buildParam(task, cmdDTO, policyDTO.getDnatName(), policyDTO.getMatchType(),PushNatTypeEnum.NAT_TYPE_D.getCode(),policyDTO.getMatchPreServices(),policyDTO.getMatchPostServices());
                }
            }
        }

    }

    /**
     * 构建 匹配上whatIf中的nat 或者存量和whatIf都匹配上 的转换关系参数
     * @param task
     * @param cmdDTO
     * @param targetName
     * @param matchType
     * @param natType
     */
    private void buildParam(SimulationTaskDTO task, CmdDTO cmdDTO,String targetName,String matchType,String natType,List<ServiceDTO> matchPreServices,List<ServiceDTO> matchPostServices) {
        PolicyDTO policy = cmdDTO.getPolicy();
        SpecialNatDTO specialNatDTO = policy.getSpecialNatDTO();

        if (PolicyConstants.MATCH_EXIST_AND_WHATIF.equals(matchType) && !targetName.startsWith("whatIf_")) {
            if (PushNatTypeEnum.NAT_TYPE_S.getCode().equals(natType)) {
                cmdDTO.getExistObject().setPostSrcAddressObjectName(targetName);
            } else if (PushNatTypeEnum.NAT_TYPE_D.getCode().equals(natType)) {
                cmdDTO.getPolicy().setExistVirtualIpName(targetName);
                cmdDTO.getPolicy().setExistDnat(org.apache.commons.lang3.StringUtils.isNotBlank(targetName) ? true : false);
                if (CollectionUtils.isNotEmpty(matchPreServices)) {
                    cmdDTO.getPolicy().setServiceList(matchPreServices);
                }
                if (CollectionUtils.isNotEmpty(matchPostServices)) {
                    cmdDTO.getPolicy().setPostServiceList(matchPostServices);
                }
            }
            return;
        }
        JSONArray whatIfCaseArray = JSONObject.parseArray(task.getRelevancyNat());
        if (whatIfCaseArray != null && whatIfCaseArray.size() > 0) {
            for (int i = 0; i < whatIfCaseArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) whatIfCaseArray.get(i);
                int relevancyNatTaskId = jsonObject.getIntValue("taskId");
                int specialRelevancyNat = jsonObject.getIntValue("type");
                if (PolicyConstants.POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT != specialRelevancyNat) {
                    // 如果仿真关联的nat场景类型不是飞塔nat场景，则不去查询
                    continue;
                }
                RecommendRelevanceSceneDTO relevanceSceneDTO = recommendRelevanceSceneService.queryById(relevancyNatTaskId);
                if (relevanceSceneDTO == null) {
                    continue;
                }

                if (targetName.contains(relevanceSceneDTO.getName()) && PushNatTypeEnum.NAT_TYPE_S.getCode().equals(natType)) {
                    policy.setPostSrcIp(relevanceSceneDTO.getPostSrcIp());
                    break;
                }
                if (targetName.contains(relevanceSceneDTO.getName()) && PushNatTypeEnum.NAT_TYPE_D.getCode().equals(natType)) {
                    cmdDTO.getPolicy().setExistDnat(org.apache.commons.lang3.StringUtils.isNotBlank(relevanceSceneDTO.getName()) ? true : false);

                    specialNatDTO.setDstIp(relevanceSceneDTO.getDstIp());
                    specialNatDTO.setPostDstIp(relevanceSceneDTO.getPostDstIp());
                    specialNatDTO.setSrcItf(relevanceSceneDTO.getSrcItf());
                    specialNatDTO.setDstItf(relevanceSceneDTO.getDstItf());
                    specialNatDTO.setPostPort(relevanceSceneDTO.getPostPort());
                    specialNatDTO.setServiceList(relevanceSceneDTO.getServiceList());

                    if (org.apache.commons.lang3.StringUtils.isNotBlank(relevanceSceneDTO.getPostService())) {
                        JSONArray array = JSONObject.parseArray(relevanceSceneDTO.getPostService());
                        List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                        if (CollectionUtils.isNotEmpty(serviceList)) {
                            specialNatDTO.setPostServiceList(serviceList);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void buildDeviceCommandMap(SimulationTaskDTO task, int taskId, Map<String, CommandTaskEditableEntity> deviceCommandMap, String deviceUuid, String branchLevel, DeviceDTO device, GenerateCommandDTO simulationCmdDTO, String errorMsg, CmdDTO cmdDTO) {
        if(deviceCommandMap.containsKey(deviceUuid)) {
            CommandTaskEditableEntity commandTaskEditableEntity = deviceCommandMap.get(deviceUuid);
            String commandLine = null == simulationCmdDTO ? "" : simulationCmdDTO.getCommandline();
            String command = commandTaskEditableEntity.getCommandline() + "\n\n"
                    + commandLine;
            commandTaskEditableEntity.setCommandline(command);
            String rollbackCommandLine = null == simulationCmdDTO ? "" : simulationCmdDTO.getRollbackCommandline();
            String commandRevert =  commandTaskEditableEntity.getCommandlineRevert()+ "\n\n"
                    + rollbackCommandLine;
            commandTaskEditableEntity.setCommandlineRevert(commandRevert);
            commandTaskEditableEntity.setBranchLevel(branchLevel);
            if(null != errorMsg){
                String errorMsgStr = org.apache.commons.lang3.StringUtils.isBlank(commandTaskEditableEntity.getErrorMsg()) ? errorMsg : commandTaskEditableEntity.getErrorMsg() + "\n\n" + errorMsg;
                commandTaskEditableEntity.setErrorMsg(errorMsgStr);
            }
        } else {
            CommandTaskEditableEntity commandTaskEditableEntity = getCommandTaskEditableEntity(taskId, task.getTheme(), task.getUserName(), simulationCmdDTO, deviceUuid,errorMsg, cmdDTO.getSetting().getSwapNameId());
            commandTaskEditableEntity.setBranchLevel(branchLevel);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(device.getRuleListName())
                    || org.apache.commons.lang3.StringUtils.isNotBlank(device.getMatchRuleId())) {
                JSONObject json = new JSONObject();
                json.put("ruleListName", org.apache.commons.lang3.StringUtils.isBlank(device.getRuleListName()) ? "" : device.getRuleListName());
                json.put("matchRuleId", org.apache.commons.lang3.StringUtils.isBlank(device.getMatchRuleId()) ? "" : device.getMatchRuleId());
                commandTaskEditableEntity.setMatchMsg(json.toJSONString());
            }
            deviceCommandMap.put(deviceUuid, commandTaskEditableEntity);
        }
    }

    /**
     * 构建编辑策略统计map
     * @param editPolicyTotalMap
     * @param deviceUuid
     * @param cmdDTO
     */
    private void buildEditPolicyTotalMap(Map<String, Map<Integer, AtomicInteger>> editPolicyTotalMap, String deviceUuid, CmdDTO cmdDTO) {
        // 如果返回的合并属性为空，则证明没有可合并的策略,不用添加到统计策略合并map
        if(null != cmdDTO.getPolicy().getMergeProperty()){
            if (editPolicyTotalMap.containsKey(deviceUuid)) {
                Map<Integer, AtomicInteger> detailMap = editPolicyTotalMap.get(deviceUuid);
                if(detailMap.containsKey(cmdDTO.getPolicy().getMergeProperty())){
                    detailMap.get(cmdDTO.getPolicy().getMergeProperty()).addAndGet(1);
                }else{
                    detailMap.put(cmdDTO.getPolicy().getMergeProperty(),new AtomicInteger(1));
                }
            } else {
                Map<Integer, AtomicInteger> detailMap = new HashMap<>();
                AtomicInteger countInt = new AtomicInteger(1);
                detailMap.put(cmdDTO.getPolicy().getMergeProperty(), countInt);
                editPolicyTotalMap.put(deviceUuid,detailMap);
            }
        }
    }

    /**
     * 构建合并信息到实体
     * @param deviceCommandMap
     * @param editPolicyTotalMap
     */
    private void buildMergeInfoToEntity(Map<String, CommandTaskEditableEntity> deviceCommandMap, Map<String, Map<Integer, AtomicInteger>> editPolicyTotalMap) {
        if(0 != editPolicyTotalMap.size()){
            for(String deviceUuid :deviceCommandMap.keySet()){
                if(editPolicyTotalMap.containsKey(deviceUuid)){
                    CommandTaskEditableEntity editableEntity = deviceCommandMap.get(deviceUuid);
                    Map<Integer,AtomicInteger> detailMap = editPolicyTotalMap.get(deviceUuid);
                    JSONObject jsonObject = new JSONObject();
                    for (Integer mergeProperty : detailMap.keySet()){
                        String property = null;
                        if(PolicyMergePropertyEnum.MERGE_SRC_IP.getCode().equals(mergeProperty)){
                            property = PolicyMergePropertyEnum.MERGE_SRC_IP.getKey();
                        }else if(PolicyMergePropertyEnum.MERGE_DST_IP.getCode().equals(mergeProperty)){
                            property = PolicyMergePropertyEnum.MERGE_DST_IP.getKey();
                        }else if(PolicyMergePropertyEnum.MERGE_SERVICE.getCode().equals(mergeProperty)){
                            property = PolicyMergePropertyEnum.MERGE_SERVICE.getKey();
                        }else{
                            continue;
                        }
                        jsonObject.put(property, detailMap.get(mergeProperty));
                    }
                    if (0 == jsonObject.size()) {
                        editableEntity.setMergeInfo(null);
                    }else{
                        editableEntity.setMergeInfo(jsonObject.toString());
                    }
                }
            }
        }
    }


    /**
     * 查询策略集下，符合条件的第一个策略集名称
     * @param deviceUuid  设备uuid
     * @param ruleListUuid 策略集uuid
     * @param srcZone  源域
     * @param dstZone  目的域
     * @return
     */
    protected String getFirstPolicyName(String deviceUuid, String ruleListUuid, String srcZone, String dstZone) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);

        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO rule : list) {
                if(rule.getInInterfaceGroupRefs() == null) {
                    logger.info("getInInterfaceGroupRefs为空");
                    continue;
                }
                if( rule.getOutInterfaceGroupRefs() == null) {
                    logger.info("getOutInterfaceGroupRefs为空");
                    continue;
                }
                if(rule.getInInterfaceGroupRefs().contains(srcZone) && rule.getOutInterfaceGroupRefs().contains(dstZone)) {
                    name = rule.getName();
                    break;
                }
            }
        }
        return name;
    }

    /**
     * 查询策略集下的第一个策略名称
     * @param deviceUuid 设备uuid
     * @param ruleListUuid 策略集uuid
     * @return
     */
    protected String getFirstPolicyName(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        logger.info(String.format("迪普设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + resultRO.toString()  + "\n-----------------------------------\n");

        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if(list != null && list.size() > 0) {
            int index = 0;
            for(DeviceFilterRuleListRO ruleListRO:list) {
                index++;
                if (ruleListRO.isImplicit() == true) {
                    logger.info(String.format("第(%d)条策略(%s)为默认策略，不返回策略名称...", index, ruleListRO.getName()));
                } else {
                    logger.info("找到策略:" + ruleListRO.getName());
                    name = ruleListRO.getName();
                    return name;
                }
            }
        }
        return name;
    }

    protected String getFirstPolicyId(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        logger.info(String.format("迪普设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + resultRO.toString()  + "\n-----------------------------------\n");

        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO ruleListRO:list) {
                logger.info("找到策略:" + ruleListRO.getRuleId());
                name = ruleListRO.getRuleId();
                return name;
            }
        }
        return name;
    }

    /**
     * 获取策略集最大的id
     * @param deviceUuid
     * @param ruleListUuid
     * @return
     */
    protected Integer getMaxPolicyId(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);

        List<DeviceFilterRuleListRO> list = resultRO.getData();
        Integer maxId = null;
        if(list != null && list.size() > 0) {
            maxId = list.stream()
                    .map(deviceFilterRuleListRO -> Integer.valueOf(deviceFilterRuleListRO.getRuleId()))
                    .max(Integer::compareTo)
                    .get();
        }
        return maxId;
    }

    Integer getCurrentPolicyId(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        logger.debug(String.format("设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSONObject.toJSONString(resultRO)  + "\n-----------------------------------");

        List<DeviceFilterRuleListRO> list = resultRO.getData();
        Integer id = 0;
        String name = null;
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO ruleListRO:list) {
                logger.info("找到策略:" + ruleListRO.getRuleId());
                name = ruleListRO.getRuleId();
                if(AliStringUtils.isEmpty(name)) {
                    logger.info("策略名为空！");
                    continue;
                }

                if(name.startsWith("Default")) {
                    logger.info("Default策略，跳过获取id");
                    continue;
                }

                try {
                    Integer policyId = Integer.valueOf(name.trim());
                    if(policyId>id) {
                        id = policyId;
                    }
                } catch (Exception e) {
                    logger.info("解析策略名称出错。。。", e);
                }
            }
        }

        return id;
    }


    private class Result {
        public int rc = ReturnCode.POLICY_MSG_OK;

        public String commandLine = "";

        public boolean hasCmdFlag = false;

        public boolean hasNoCmdFlag = false;
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

    @Override
    public void setAdvancedSetting(CmdDTO cmdDTO, String modelNumber ,String deviceUuid) {

        SettingDTO settingDTO = cmdDTO.getSetting();

        String isCreateObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT);

        //全局设置为不创建对象，则设置为不创建对象
        if (isCreateObject.equals(AdvancedSettingsConstants.IS_REFERENCE_CONTENT_VALUE)) {
            settingDTO.setCreateObject(false);
        } else {
            settingDTO.setCreateObject(true);
        }

        //根据高级设置，判断是否复用服务对象
        boolean reuseService = policyRecommendTaskService.isUseCurrentObject();
        //根据高级设置，判断是否复用地址对象
        boolean reuseAddress = policyRecommendTaskService.isUseCurrentAddressObject();

        logger.info("是否创建对象:" + settingDTO.isCreateObject() + "，是否复用服务:" + reuseService + ",是否复用地址:" + reuseAddress);

        settingDTO.setEnableServiceObjectSearch(reuseService);
        settingDTO.setEnableAddressObjectSearch(reuseAddress);

        logger.info("进行复用之前，cmdDto:" + JSONObject.toJSONString(cmdDTO));

        //根据产品型号，确定地址、服务复用
        DeviceModelNumberEnum modelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);

        try {

            //格式化服务对象
            CmdService serviceFormat = cmdServiceMap.get(NameUtils.getServiceDefaultName(SubServiceEnum.FORMAT_SERVICE_DTO.getServiceClass()));
            if (serviceFormat != null) {
                serviceFormat.modify(cmdDTO);
            }

            //服务对象复用
            int serviceCode = vendorManager.getSearchExistServiceProcedure(modelNumberEnum);
            SubServiceEnum serviceSubServiceEnum = SubServiceEnum.valueOf(serviceCode);
            CmdService serviceService = cmdServiceMap.get(NameUtils.getServiceDefaultName(serviceSubServiceEnum.getServiceClass()));
            if (serviceService != null) {
                serviceService.modify(cmdDTO);
            }

            //地址对象复用
            int addressCode = vendorManager.getSearchAddressObject(modelNumberEnum);
            SubServiceEnum addressSubServieEnum = SubServiceEnum.valueOf(addressCode);
            CmdService addressService = cmdServiceMap.get(NameUtils.getServiceDefaultName(addressSubServieEnum.getServiceClass()));
            if (addressService != null) {
                addressService.modify(cmdDTO);
            }

            logger.info("进行复用之后，cmdDto:" + JSONObject.toJSONString(cmdDTO));

        } catch (Exception e) {
            logger.error("复用对象异常", e);
        }

    }

    private RecommendTaskPolicyDTO setAdvanceSettings(SimulationTaskDTO task, RecommendPolicyDTO policyDTO, RecommendTaskPolicyDTO taskPolicyDTO) {
        //命令行生成设置域信息
        //根据高级设置决定策略检查是否需要设置源域和目的域。
        if(policyDTO.getSpecifyZone() == AdvancedSettingsConstants.PARAM_INT_SET_NO_ZONE) {
            logger.debug(String.format("任务(%d)[%s]设备(%s)不指定域信息...命令行生成不指定域", task.getId(), task.getTheme(), policyDTO.getDeviceUuid()));
            //源域目的域均设置为空
            taskPolicyDTO.setSrcZone(null);
            taskPolicyDTO.setDstZone(null);
        } else if(policyDTO.getSpecifyZone() == AdvancedSettingsConstants.PARAM_INT_SET_SRC_ZONE) {
            logger.debug(String.format("任务(%d)[%s]设备(%s)指定源域信息...命令行生成指定源域", task.getId(), task.getTheme(), policyDTO.getDeviceUuid()));
            //目的域设置为空
            taskPolicyDTO.setDstZone(null);
        } else if(policyDTO.getSpecifyZone() == AdvancedSettingsConstants.PARAM_INT_SET_DST_ZONE) {
            logger.debug(String.format("任务(%d)[%s]设备(%s)指定目的域信息...命令行生成指定目的域", task.getId(), task.getTheme(), policyDTO.getDeviceUuid()));
            //源域设置为空
            taskPolicyDTO.setSrcZone(null);
        } else {
            logger.debug(String.format("任务(%d)[%s]设备(%s)使用默认方式设置域...命令行生成指定源域和目的域", task.getId(), task.getTheme(), policyDTO.getDeviceUuid()));
        }

        //只有思科设备根据高级设置决定是ACL策略设置到入接口还是出接口
        NodeEntity nodeEntity = policyDTO.getNode();
        if(nodeEntity != null && !AliStringUtils.isEmpty(nodeEntity.getModelNumber()) && nodeEntity.getModelNumber().equals("Cisco ASA")) {
            if (policyDTO.getAclDirection() == AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION) {
                logger.debug(String.format("任务(%d)[%s]设备（%s）指定策略下发到出接口", task.getId(), task.getTheme(), taskPolicyDTO.getDeviceUuid()));
                taskPolicyDTO.setInDevIf(null);
                taskPolicyDTO.setInDevIfAlias(null);
            } else {
                logger.debug(String.format("任务(%d)[%s]设备（%s）指定策略下发到入接口", task.getId(), task.getTheme(), taskPolicyDTO.getDeviceUuid()));
                taskPolicyDTO.setOutDevIf(null);
                taskPolicyDTO.setOutDevIfAlias(null);
            }
        }

        //全局设置为合并策略，则设置为合并策略
        if(policyDTO.getCreatePolicy() == AdvancedSettingsConstants.PARAM_INT_MERGE_RULE) {
            logger.debug(String.format("任务(%d)[%s]生成命令行优先合并策略", task.getId(), task.getTheme()));
            taskPolicyDTO.setMustCreateFlag(false);
        } else {
            logger.debug(String.format("任务(%d)[%s]生成命令行优先创建策略", task.getId(), task.getTheme()));
        }

        //全局设置为优先引用内容还是创建对象
        if(policyDTO.getCreateObject() == AdvancedSettingsConstants.PARAM_INT_REFERENCE_CONTENT) {
            logger.debug(String.format("任务(%d)[%s]生成命令行优先引用内容", task.getId(), task.getTheme()));
            taskPolicyDTO.setCreateObjFlag(false);
        } else {
            logger.debug(String.format("任务(%d)[%s]生成命令行优先创建对象...", task.getId(), task.getTheme()));
        }

        //设置策略移动的位置
        taskPolicyDTO.setMoveSeatEnum(MoveSeatEnum.values()[policyDTO.getMovePolicy()]);
        taskPolicyDTO.setSwapRuleNameId(policyDTO.getSpecificPosition());

        return taskPolicyDTO;
    }

    CommandTaskEditableEntity getCommandTaskEditableEntity(Integer taskId, String theme, String userName, GenerateCommandDTO simulationCmdDTO, String deviceUuid,String errorMsg,String swapNameId) {
        CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
        entity.setTaskId(taskId);
        entity.setTheme(theme);
        entity.setUserName(userName);
        entity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
        entity.setCommandline(simulationCmdDTO.getCommandline());
        entity.setCommandlineRevert(simulationCmdDTO.getRollbackCommandline());
        entity.setCreateTime(new Date());
        entity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);
        entity.setDeviceUuid(deviceUuid);
        entity.setPushResult("");
        entity.setErrorMsg(errorMsg);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(swapNameId)) {
            entity.setMovePosition(swapNameId);
        }
        return entity;
    }

    @Override
    public void setPreSteps(String modelNumber, CommandlineDTO commandLineDTO) {
        if(commandLineDTO.getMoveSeatEnum() == MoveSeatEnum.FIRST) {
            if (modelNumber.toUpperCase().equals("JUNIPERSRX")) {
                logger.debug("JuniperSRX获取第一条策略名称");
                String firstPolicyName = getFirstPolicyName(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid(),
                        commandLineDTO.getSrcZone(), commandLineDTO.getDstZone());
                commandLineDTO.setFirstPolicyName(firstPolicyName);
            } else if (modelNumber.contains("DPTech Firewall")) {
                logger.debug("DPTech获取第一条策略名称");
                String firstPolicyName = getFirstPolicyName(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid());
                commandLineDTO.setFirstPolicyName(firstPolicyName);
            } else if(modelNumber.equals("Topsec TOS Firewall")) {
                String firstPolicyId = getFirstPolicyId(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid());
                commandLineDTO.setSwapRuleNameId(firstPolicyId);
            } else if ("Venustech VSOS".equals(modelNumber)) {
                logger.debug("Venustech VSOS的策略id得根据已有的策略id来累加,移动策略的获得同一个策略集的第一条策略");
                String firstPolicyId = getFirstPolicyName(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid(),
                        commandLineDTO.getSrcZone(), commandLineDTO.getDstZone());
                Integer maxId = getMaxPolicyId(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid());
                if (maxId != null) {
                    commandLineDTO.setFirstPolicyName(firstPolicyId);
                    int policyId = maxId + 1;
                    String name = commandLineDTO.getName();
                    if (name.contains("_")) {
                        String[] strings = name.split("_");
                        String nameIdString = strings[strings.length -1];
                        if (StringUtils.isNumeric(nameIdString)) {
                            int nameId = Integer.valueOf(nameIdString);
                            policyId += nameId;
                        }
                    }
                    commandLineDTO.setName(String.valueOf(policyId));
                } else {
                    throw new IllegalArgumentException("策略集最大id查询异常");
                }
            }
        }

        if(modelNumber.toUpperCase().contains("FORTINET")){
            int maxId = getCurrentPolicyId(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid());
            String firstPolicyId = getFirstPolicyId(commandLineDTO.getDeviceUuid(), commandLineDTO.getRuleListUuid());
            if (firstPolicyId != null) {
                if (firstPolicyId.startsWith("Default")) {
                    firstPolicyId = null;
                }
            }
            commandLineDTO.setSwapRuleNameId(firstPolicyId);
            synchronized (CommandServiceImpl.class) {
                int currentId = advancedSettingService.getFortinetPolicyId(commandLineDTO.getDeviceUuid());
                if (maxId < currentId) {
                    maxId = currentId;
                }
                commandLineDTO.setCurrentId(String.valueOf(maxId + 1));
                advancedSettingService.setFortinetPolicyId(commandLineDTO.getDeviceUuid(), maxId + 1);
            }
        }

        if(modelNumber.contains("Cisco")) {
            String interfaceAlias = commandLineDTO.getSrcItfAlias();
            boolean inbound = true;
            if(AliStringUtils.isEmpty(interfaceAlias)) {
                interfaceAlias = commandLineDTO.getDstItfAlias();
                inbound = false;
            }

            if(interfaceAlias != null) {
                logger.debug(String.format("思科设备(%s)接口别名为(%s)，查询现有策略集名称...", commandLineDTO.getDeviceUuid(), interfaceAlias));
                String policyName = whaleManager.getInterfacePolicyName(commandLineDTO.getDeviceUuid(), interfaceAlias, inbound);
                if(AliStringUtils.isEmpty(policyName)) {
                    policyName = String.format("%s_%s", interfaceAlias, inbound?"in":"out");
                    commandLineDTO.setCiscoInterfaceCreate(true);
                }
                commandLineDTO.setCiscoInterfacePolicyName(policyName);
            } else {
                logger.debug("接口别名为空...");
            }
        }
    }




    /**
     * 添加灾备设备
     * @param dto
     * @return
     */
    public List<RecommendPolicyDTO> setDisasterRecovery(RecommendPolicyDTO dto){
        List<RecommendPolicyDTO> recommendPolicyDTOList = new ArrayList<>();
        NodeEntity node = dto.getNode();
        logger.info("开始生成灾备墙命令行，当前主墙ip: " + node.getIp());
        List<NodeEntity> anotherDeviceList = advancedSettingService.getAnotherDeviceByIp(AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY, node.getIp());
        if (CollectionUtils.isNotEmpty(anotherDeviceList)) {
            for (NodeEntity anotherDevice : anotherDeviceList) {
                if(anotherDevice == null ){
                    logger.info("未查询到:"+ node.getIp() +"对应的灾备设备");
                    return null;
                }
                RecommendPolicyDTO disasterRecoveryDTO = new RecommendPolicyDTO();
                BeanUtils.copyProperties(dto , disasterRecoveryDTO);
                disasterRecoveryDTO.setDeviceUuid(anotherDevice.getUuid());
                disasterRecoveryDTO.setNode(anotherDevice);

                DeviceRO deviceRO = whaleManager.getDeviceByUuid(anotherDevice.getUuid());
                DeviceDataRO deviceData = deviceRO.getData().get(0);
                if (deviceData.getIsVsys() != null) {
                    boolean isVsys = deviceData.getIsVsys();
                    String vsysName = deviceData.getVsysName();
                    disasterRecoveryDTO.setVsysName(vsysName);
                    disasterRecoveryDTO.setVsys(isVsys);
                }
                recommendPolicyDTOList.add(disasterRecoveryDTO);
            }
        }
        return recommendPolicyDTOList;
    }

    /**
     * 检查设备是否已支持编辑策略建议
     * 已支持需添加设备型号
     * @param deviceUuid
     * @return
     */
    private boolean checkEditSecurityPolicy(String deviceUuid){
        NodeEntity nodeEntity = recommendTaskManager.getTheNodeByUuid(deviceUuid);
        DeviceModelNumberEnum modelNumber = DeviceModelNumberEnum.fromString(nodeEntity.getModelNumber());
        switch (modelNumber) {
            case CISCO:
            case CISCO_ASA_86:
            case USG6000:
            case DPTECHR003:
            case DPTECHR004:
            case SRX:
            case SSG:
            case USG6000_NO_TOP:
                return true;
            default:
                return false;
        }
    }

}
