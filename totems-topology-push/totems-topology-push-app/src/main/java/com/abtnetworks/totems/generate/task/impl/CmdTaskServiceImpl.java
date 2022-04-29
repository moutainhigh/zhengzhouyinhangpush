package com.abtnetworks.totems.generate.task.impl;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.GenerateEnum;
import com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.generate.task.CmdTaskService;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.CommandService;
import com.abtnetworks.totems.remote.nginz.NgRemoteService;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.abtnetworks.totems.common.constants.GenerateConstants.OPENED_POLICY_NOT_GENERATE;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.UN_OPEN_GENERATE;
import static com.abtnetworks.totems.generate.service.impl.CommandlineServiceImpl.ERROR_PREFIX;
import static com.abtnetworks.totems.generate.service.impl.CommandlineServiceImpl.ERROR_ROLLBACK_PREFIX;

/**
 * @Description 该类用于生成命令行生成任务线程，处理命令行生成任务
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class CmdTaskServiceImpl implements CmdTaskService {

    @Autowired
    CommandlineService commandlineService;

    @Autowired
    @Qualifier(value = "commandlineExecutor")
    private Executor commandlineExecutor;

    @Resource
    NgRemoteService ngRemoteService;

    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    public RecommendTaskManager recommendTaskManager;

    @Autowired
    CommandService commandService;

    @Value("${push.open-convert-address:false}")
    private boolean open_convert_address;

    @Override
    public void getRuleMatchFlow2Generate(CmdDTO cmdDTO, UserInfoDTO userInfoDTO) {
        List<CmdDTO> ruleMatchFlow = ngRemoteService.getRuleMatchFlow(cmdDTO);
        // 过滤原地址子网，如果拆分之后的地址和源地址一样 需要把拆分之后的地址替换成工单填写的地址（主要针对于工单填写的子网，但是拆分之后没有刨除地址，且地址被转换成了范围）
        if (open_convert_address) {
            filterRuleDataByTaskAddress(cmdDTO, ruleMatchFlow);
        }
        TaskDTO task = cmdDTO.getTask();
        if (CollectionUtils.isNotEmpty(ruleMatchFlow)) {
            log.info("命令行生成任务为:" + JSONObject.toJSONString(ruleMatchFlow, false));
            //灾备设备需要移动到命令中去执行
            List<GeneratedDeviceDTO> devices = new ArrayList<>();
            List<NodeEntity> anotherDeviceByIpList = commandlineService.isDisasterRecoveryDevice(cmdDTO, null);
            if (CollectionUtils.isNotEmpty(anotherDeviceByIpList)) {
                for (NodeEntity nodeEntity : anotherDeviceByIpList) {
                    GeneratedDeviceDTO generatedDeviceDTO = new GeneratedDeviceDTO();
                    generatedDeviceDTO.setNodeEntity(nodeEntity);

                    DeviceRO deviceRO = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
                    DeviceDataRO deviceData = deviceRO.getData().get(0);
                    DeviceDTO disasterRecoveryDevice = new DeviceDTO();

                    if (deviceData.getIsVsys() != null) {
                        boolean isVsys = deviceData.getIsVsys();
                        String vsysName = deviceData.getVsysName();
                        disasterRecoveryDevice.setVsys(isVsys);
                        disasterRecoveryDevice.setVsysName(vsysName);
                    }
                    generatedDeviceDTO.setDisasterRecoveryDTO(disasterRecoveryDevice);
                    devices.add(generatedDeviceDTO);
                }
            }
            this.addGenerateCmdTask(ruleMatchFlow, userInfoDTO, cmdDTO, devices);
        } else {
            commandlineService.updateCommandStatus(task.getId(), OPENED_POLICY_NOT_GENERATE, OPENED_POLICY_NOT_GENERATE, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START,null,null);
            commandlineService.updateTaskStatus(task.getTaskId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE);
        }
    }

    /**
     * 根据工单地址 过滤数据源（若拆分后的范围与拆分前的子网是一致的（即未刨除已开通地址），则需要将范围还原成工单所填的子网）
     * @param cmdDTO
     * @param ruleMatchFlow
     */
    private void filterRuleDataByTaskAddress(CmdDTO cmdDTO, List<CmdDTO> ruleMatchFlow) {
        // 处理拆分之后数据流判断 如果拆分后的范围与拆分前的子网是一致的（即未刨除已开通地址），则需要将范围还原成子网
        String[] srcIps = (StringUtils.isBlank(cmdDTO.getPolicy().getSrcIp()) || PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(cmdDTO.getPolicy().getSrcIp())) ?
                null : cmdDTO.getPolicy().getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        String[] dstIps = (StringUtils.isBlank(cmdDTO.getPolicy().getDstIp()) || PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(cmdDTO.getPolicy().getDstIp())) ?
                null : cmdDTO.getPolicy().getDstIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        // 获取策略集合
        List<PolicyDTO> policyDTOS = getPolicyDTOS(ruleMatchFlow);
        log.info("工单原地址过数据流之前policys为:{}", JSONObject.toJSONString(policyDTOS));

        for (CmdDTO itemDTO : ruleMatchFlow) {
            PolicyDTO policyDTO = itemDTO.getPolicy();
            Map<String,String> filterMap = commandService.filterRuleDataByTaskAddress(srcIps, dstIps, policyDTO.getSrcIp(), policyDTO.getDstIp());
            policyDTO.setSrcIp(filterMap.get(PolicyConstants.SRC));
            policyDTO.setDstIp(filterMap.get(PolicyConstants.DST));
        }
        List<PolicyDTO> afterPolicyDTOS = getPolicyDTOS(ruleMatchFlow);
        log.info("工单原地址过数据流之后,替换的地址为:{}", JSONObject.toJSONString(afterPolicyDTOS));
    }

    /**
     * 获取策略dtos
     * @param ruleMatchFlow
     * @return
     */
    private List<PolicyDTO> getPolicyDTOS(List<CmdDTO> ruleMatchFlow) {
        List<PolicyDTO> policyDTOS = new ArrayList<>();
        for (CmdDTO itemCmdDTO : ruleMatchFlow){
            policyDTOS.add(itemCmdDTO.getPolicy());
        }
        return policyDTOS;
    }


    /**
     * ps 目前该方法只适用安全策略
     *
     * @param cmdDTOList  命令行生成数据
     * @param userInfoDTO
     * @return
     */
    @Override
    public int addGenerateCmdTask(List<CmdDTO> cmdDTOList, UserInfoDTO userInfoDTO, CmdDTO cmdDTO,
                                  List<GeneratedDeviceDTO> deviceDTOS) {
        TaskDTO task = cmdDTO.getTask();
        String id = "CMD_" + task.getId() + IdGen.getRandomNumberString();

        if (ExtendedExecutor.containsKey(id)) {
            log.warn(String.format("命令行生成任务(%s)已经存在！任务不重复添加", id));
            return ReturnCode.TASK_ALREADY_EXIST;
        }
        List<NodeEntity> anotherDeviceByIpList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(deviceDTOS)){
            for (GeneratedDeviceDTO generatedDeviceDTO :deviceDTOS){
                anotherDeviceByIpList.add(generatedDeviceDTO.getNodeEntity());
            }
        }
        List<CommandTaskEditableEntity> entityDisasterList = commandlineService.saveDisasterDeviceCommandline(cmdDTO, anotherDeviceByIpList, userInfoDTO);

        commandlineExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "安全策略（主/灾备）生成命令行", "", new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    log.info("开始生成命令行...");
                    StringBuffer commandline = new StringBuffer(), rollbackCommandline = new StringBuffer(),
                            commandlineDisaster = new StringBuffer(), rollbackDisasterCommandline = new StringBuffer(),
                            otherAclMsg = new StringBuffer(), otherAclMsgDisaster = new StringBuffer();
                    String ruleListName = null,matchRuleId = null, ruleListNameDisaster = null,
                            matchRuleIdDisaster = null;
                    Map<Integer,AtomicInteger> editPolicyTotalMap = new HashMap<>(16);
                    for (CmdDTO cmdNewDTO : cmdDTOList) {

                        Integer mergeProperty = cmdNewDTO.getPolicy().getMergeProperty();
                        if (mergeProperty != null && UN_OPEN_GENERATE.getCode().equals(mergeProperty)) {

                            log.info("如果拆分的数据流中有的已经不需要开通了，过滤掉这部分");
                        } else {
                            GenerateCommandDTO generateCommandDTO = commandlineService.generateCommand(cmdNewDTO);
                            // 统计如果接口上没有绑定acl策略提示错误，不生成命令行
                            if (null != cmdNewDTO.getBusinessInfoDTO() && StringUtils.isNotBlank(cmdNewDTO.getBusinessInfoDTO().getOtherErrorMsg())) {
                                otherAclMsg.append(cmdNewDTO.getBusinessInfoDTO().getOtherErrorMsg()).append("\n");
                            }
                            // 如果返回的合并属性为空，则证明没有可合并的策略,不用添加到统计策略合并map
                            if (null != cmdDTO.getPolicy().getMergeProperty()) {
                                if (editPolicyTotalMap.containsKey(cmdDTO.getPolicy().getMergeProperty())) {
                                    editPolicyTotalMap.get(cmdDTO.getPolicy().getMergeProperty()).addAndGet(1);
                                } else {
                                    editPolicyTotalMap.put(cmdDTO.getPolicy().getMergeProperty(), new AtomicInteger(1));
                                }
                            }

                            if (null != generateCommandDTO && 0 == generateCommandDTO.getResultCode()) {
                                commandline.append(generateCommandDTO.getCommandline()).append(CommonConstants.LINE_BREAK);
                                rollbackCommandline.append(generateCommandDTO.getRollbackCommandline()).append(CommonConstants.LINE_BREAK);
                                // 这里策略名称的优先级要高于匹配到策略id
                                if (StringUtils.isBlank(ruleListName)) {
                                    ruleListName = cmdNewDTO.getDevice().getRuleListName();
                                    matchRuleId = cmdNewDTO.getDevice().getMatchRuleId();

                                }

                                if (CollectionUtils.isNotEmpty(entityDisasterList)) {
                                    for (int i = 0; i < entityDisasterList.size(); i++) {
                                        GeneratedDeviceDTO generatedDeviceDTO = getGeneratedDeviceDTO(deviceDTOS,entityDisasterList.get(i).getDeviceUuid());
                                        if(ObjectUtils.isEmpty(generateCommandDTO)){
                                            continue;
                                        }

                                        CmdDTO cmdDTODisaster = commandlineService.generateDisasterRecovery(cmdNewDTO, userInfoDTO, generatedDeviceDTO.getNodeEntity(), generatedDeviceDTO.getDisasterRecoveryDTO());
                                        GenerateCommandDTO generateDisasterCommandDTO = commandlineService.generateCommand(cmdDTODisaster);
                                        // 统计如果接口上没有绑定acl策略提示错误，不生成命令行
                                        if (null != cmdDTODisaster.getBusinessInfoDTO() && StringUtils.isNotBlank(cmdDTODisaster.getBusinessInfoDTO().getOtherErrorMsg())) {
                                            otherAclMsgDisaster.append(cmdDTODisaster.getBusinessInfoDTO().getOtherErrorMsg()).append("\n");
                                        }
                                        if (generateDisasterCommandDTO != null && generateDisasterCommandDTO.getResultCode() == 0) {
                                            commandlineDisaster.append(generateDisasterCommandDTO.getCommandline()).append(CommonConstants.LINE_BREAK);
                                            rollbackDisasterCommandline.append(generateDisasterCommandDTO.getRollbackCommandline()).append(CommonConstants.LINE_BREAK);

                                            if (StringUtils.isBlank(ruleListNameDisaster)) {
                                                ruleListNameDisaster = cmdDTODisaster.getDevice().getRuleListName();
                                                matchRuleIdDisaster = cmdDTODisaster.getDevice().getMatchRuleId();
                                            }
                                        }
                                        // 更新单个备用设备
                                        String allCommandlineDisaster = commandlineDisaster.toString();
                                        String allRollbackDisasterCommandline = rollbackDisasterCommandline.toString();
                                        cmdDTODisaster.getTask().setId(entityDisasterList.get(i).getId());
                                        String otherAclMsgDisasterStr = otherAclMsgDisaster.toString();
                                        cmdDTODisaster.getDevice().setRuleListName(ruleListNameDisaster);
                                        cmdDTODisaster.getDevice().setMatchRuleId(matchRuleIdDisaster);
                                        updateCommandAndTask(allCommandlineDisaster, allRollbackDisasterCommandline, cmdDTODisaster,null,otherAclMsgDisasterStr);
                                        commandlineDisaster = new StringBuffer();
                                        rollbackDisasterCommandline = new StringBuffer();
                                        otherAclMsgDisaster = new StringBuffer();
                                    }
                                }
                            } else {
                                log.info("在生成命令行的禁止数据流时错误{},错误信息:{}", JSONObject.toJSONString(cmdNewDTO));
                            }

                        }
                    }
                    // 更新单个主设备命令行信息
                    String editPolicyTotalInfo = buildMergeInfoToEntity(editPolicyTotalMap);
                    //这里无论是主墙还是灾备设备都需要合成一条命令行存入
                    String allCommandline = commandline.toString();
                    String allRollbackCommandline = rollbackCommandline.toString();
                    String otherAclMsgStr = otherAclMsg.toString();
                    cmdDTO.getDevice().setRuleListName(ruleListName);
                    cmdDTO.getDevice().setMatchRuleId(matchRuleId);
                    updateCommandAndTask(allCommandline, allRollbackCommandline, cmdDTO, editPolicyTotalInfo, otherAclMsgStr);

                } catch (Exception e) {
                    log.error("未知错误:", e);
                    throw e;
                }
            }
        });


        return ReturnCode.POLICY_MSG_OK;
    }


    /**
     * 获取设备信息(灾备和虚设备)
     * @param deviceDTOS
     * @param tergetDeviceUuid
     * @return
     */
    private GeneratedDeviceDTO getGeneratedDeviceDTO(List<GeneratedDeviceDTO> deviceDTOS,String tergetDeviceUuid){
        for (GeneratedDeviceDTO generatedDeviceDTO : deviceDTOS){
            if(tergetDeviceUuid.equals(generatedDeviceDTO.getNodeEntity().getUuid())){
                return generatedDeviceDTO;
            }
        }
        return null;
    }

    /**
     * 构建合并信息到实体
     * @param editPolicyTotalMap
     */
    private String buildMergeInfoToEntity(Map<Integer, AtomicInteger> editPolicyTotalMap) {
        if (0 != editPolicyTotalMap.size()) {
            JSONObject jsonObject = new JSONObject();
            for (Integer mergeProperty : editPolicyTotalMap.keySet()) {
                String property = null;
                if ( PolicyMergePropertyEnum.MERGE_SRC_IP.getCode().equals(mergeProperty) ) {
                    property = PolicyMergePropertyEnum.MERGE_SRC_IP.getKey();
                } else if (PolicyMergePropertyEnum.MERGE_DST_IP.getCode().equals(mergeProperty)  ) {
                    property = PolicyMergePropertyEnum.MERGE_DST_IP.getKey();
                } else if (PolicyMergePropertyEnum.MERGE_SERVICE.getCode().equals(mergeProperty)  ) {
                    property = PolicyMergePropertyEnum.MERGE_SERVICE.getKey();
                } else {
                    continue;
                }
                jsonObject.put(property, editPolicyTotalMap.get(mergeProperty));
            }
            if (0 == jsonObject.size()) {
                return null;
            }
            return jsonObject.toString();
        }
        return null;
    }

    /**
     * 保存主设备和灾备设备的命令行
     * @param allCommandline
     * @param rollbackCommandline
     * @param cmdDTO
     */
    public void updateCommandAndTask(String allCommandline, String rollbackCommandline, CmdDTO cmdDTO,String editPolicyTotalInfo,String otherAclMsg) {
        TaskDTO task = cmdDTO.getTask();
        DeviceDTO device = cmdDTO.getDevice();
        Boolean isDisasterDevice = device.getIsDisasterDevice();
        if (StringUtils.isNotBlank(otherAclMsg)){
            commandlineService.updateCommandStatus(task.getId(), otherAclMsg, otherAclMsg, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START, editPolicyTotalInfo, null);
        } else if(StringUtils.isBlank(allCommandline)) {
            String existPolicyCommandline = GenerateEnum.CMD_EXIST_ERROR.getMessage();
            String existPolicyRollbackCommandline = GenerateEnum.CMD_EXIST_ERROR.getMessage();
            commandlineService.updateCommandStatus(task.getId(), existPolicyCommandline, existPolicyRollbackCommandline, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START, editPolicyTotalInfo, null);
        }else {
            allCommandline = StringUtils.isNotBlank(allCommandline) ? allCommandline : ERROR_PREFIX + "命令行生成处理异常！";
            rollbackCommandline = StringUtils.isNotBlank(rollbackCommandline) ? rollbackCommandline : ERROR_ROLLBACK_PREFIX + "回滚命令行生成处理异常！";

            CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
            entity.setId(task.getId());
            entity.setCommandline(allCommandline);
            entity.setCommandlineRevert(rollbackCommandline);
            entity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);
            entity.setMergeInfo(editPolicyTotalInfo);
            if (StringUtils.isNotBlank(device.getRuleListName())
                    || StringUtils.isNotBlank(device.getMatchRuleId())) {
                JSONObject json = new JSONObject();
                json.put("ruleListName", StringUtils.isBlank(device.getRuleListName()) ? "" : device.getRuleListName());
                json.put("matchRuleId", StringUtils.isBlank(device.getMatchRuleId()) ? "" : device.getMatchRuleId());
                entity.setMatchMsg(JSONObject.toJSONString(json));
            }
            commandlineService.updateCommandByEntity(entity);
        }
        if (isDisasterDevice != null && isDisasterDevice) {
            return;
        }
        commandlineService.updateTaskStatus(task.getTaskId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
    }

}
