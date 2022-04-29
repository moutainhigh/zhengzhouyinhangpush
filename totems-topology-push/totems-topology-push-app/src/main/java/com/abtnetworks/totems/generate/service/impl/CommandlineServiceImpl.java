package com.abtnetworks.totems.generate.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.*;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.dto.manager.DenyPolicyInfoDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.exception.UnInterruptException;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.generate.task.impl.CmdTaskServiceImpl;
import com.abtnetworks.totems.push.manager.PolicyMergeNewTaskService;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.CommandlineManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommandlineServiceImpl implements CommandlineService {

    @Autowired
    CommandlineManager commandlineManager;

    @Autowired
    CommandTaskManager commandTaskManager;

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    VendorManager vendorManager;

    @Autowired
    Map<String, CmdService> cmdServiceMap;

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @Autowired
    public RecommendTaskManager recommendTaskManager;

    @Autowired
    CommandlineService commandlineService;

    public final static String ERROR_PREFIX = "[无法生成命令行]:";

    public final static String ERROR_ROLLBACK_PREFIX = "[无法生成回滚命令行]:";

    private final static String NOT_SUPPORT = "暂不支持";

    @Autowired
    PolicyMergeNewTaskService policyMergeNewTaskService;

    @Autowired
    private WhaleManager whaleManager;

    @Resource
    RemoteBranchService remoteBranchService;

    @Resource
    CmdTaskServiceImpl cmdTaskService;



    @Override
    public String generate(CmdDTO cmdDTO) {
        TaskDTO task = cmdDTO.getTask();
        log.debug(String.format("任务(%d)工单号[%s]开始生成命令行...\n", task.getId(), task.getTheme()) + JSONObject.toJSONString(cmdDTO, true));
        log.info(String.format("任务(%d)工单号[%s]开始生成命令行...", task.getId(), task.getTheme()));
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        StringBuffer commandlineDisaster = new StringBuffer(), rollbackDisasterCommandline = new StringBuffer(),
                otherAclMsgDisaster = new StringBuffer();
        String ruleListNameDisaster = null, matchRuleIdDisaster = null;
        String commandline = "无法生成该设备的命令行[命令行生成数据对象不合法]";
        //nat的灾备设备命令行生成
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
        List<NodeEntity> anotherDeviceByIpList1 = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(devices)){
            for (GeneratedDeviceDTO generatedDeviceDTO :devices){
                anotherDeviceByIpList1.add(generatedDeviceDTO.getNodeEntity());
            }
        }
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(cmdDTO.getTask().getUserName());
        List<CommandTaskEditableEntity> entityDisasterList = commandlineService.saveDisasterDeviceCommandline(cmdDTO, anotherDeviceByIpList1, userInfoDTO);

        if (CollectionUtils.isNotEmpty(entityDisasterList)) {
            for (int i = 0; i < entityDisasterList.size(); i++) {
                GeneratedDeviceDTO generatedDeviceDTO = getGeneratedDeviceDTO(devices,entityDisasterList.get(i).getDeviceUuid());

                CmdDTO cmdDTODisaster = commandlineService.generateDisasterRecovery(cmdDTO, userInfoDTO, generatedDeviceDTO.getNodeEntity(), generatedDeviceDTO.getDisasterRecoveryDTO());
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
                cmdTaskService.updateCommandAndTask(allCommandlineDisaster, allRollbackDisasterCommandline, cmdDTODisaster,null,otherAclMsgDisasterStr);
                commandlineDisaster = new StringBuffer();
                rollbackDisasterCommandline = new StringBuffer();
                otherAclMsgDisaster = new StringBuffer();
            }
        }

        log.info(String.format("任务(%d)工单号[%s]获取品牌相关数据...", task.getId(), task.getTheme()));
        vendorManager.getVendorInfo(cmdDTO);
        commandline = String.format("无法生成该设备的命令行[设备类型(%s)不支持]", deviceDTO.getModelNumber().getKey());

        boolean done = generateCommon(cmdDTO);
        if (!done) {
            if (null != cmdDTO.getBusinessInfoDTO() && StringUtils.isNotBlank(cmdDTO.getBusinessInfoDTO().getOtherErrorMsg())) {
                commandline = cmdDTO.getBusinessInfoDTO().getOtherErrorMsg();
            } else {
                commandline = "无法生成该设备的命令行[命令行生成处理异常]";

            }
            updateCommandStatus(task.getId(), commandline, ERROR_ROLLBACK_PREFIX + "回滚命令行生成处理异常！", PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, null, null);
            updateTaskStatus(task.getTaskId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
            return commandline;
        }

        PolicyEnum policyType = cmdDTO.getPolicy().getType();
        vendorManager.getGenerator(policyType, deviceDTO, cmdDTO.getProcedure());
        log.info("处理后数据为{}", JSONObject.toJSONString(cmdDTO, true));
        commandline = commandlineManager.generate(cmdDTO);
        log.info(String.format("任务(%d)工单号[%s]命令行生成结果为:%s", task.getId(), task.getTheme(), commandline));

        String rollbackCommandline = commandlineManager.generateRollback(cmdDTO);
        log.info(String.format("任务(%d)工单号[%s]回滚命令行生成结果为:%s", task.getId(), task.getTheme(), rollbackCommandline));

        updateCommandStatus(task.getId(), commandline, rollbackCommandline, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START,null,null);
        updateTaskStatus(task.getTaskId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE);
        return commandline;
    }

    @Override
    public GenerateCommandDTO generateCommand(CmdDTO cmdDTO) {
        String commandline = "无法生成该设备的命令行[命令行生成数据对象不合法]";
        String rollbackCommandline = "无法生成该设备的命令行[命令行生成数据对象不合法]";
        vendorManager.getVendorInfo(cmdDTO);
        this.findDenyAndDeviceMatchEdit(cmdDTO);
        boolean done = generateCommon(cmdDTO);
        GenerateCommandDTO generateCommandDTO = new GenerateCommandDTO();
        if (!done) {
            commandline = ERROR_PREFIX + "命令行生成处理异常！";
            rollbackCommandline = ERROR_ROLLBACK_PREFIX;
            generateCommandDTO.setCommandline(commandline);
            generateCommandDTO.setRollbackCommandline(rollbackCommandline);
            generateCommandDTO.setResultCode(ReturnCode.FAILED);
            return generateCommandDTO;
        }

        PolicyEnum policyType = cmdDTO.getPolicy().getType();
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        vendorManager.getGenerator(policyType, deviceDTO, cmdDTO.getProcedure());
        commandline = commandlineManager.generate(cmdDTO);
        // 如果生成命令行已经不支持了 就不能生成回滚命令行了
        if(commandline.contains(NOT_SUPPORT)){
            generateCommandDTO.setCommandline(commandline);
            generateCommandDTO.setRollbackCommandline(rollbackCommandline);
            generateCommandDTO.setResultCode(ReturnCode.POLICY_MSG_OK);
            return generateCommandDTO;
        }
        rollbackCommandline = commandlineManager.generateRollback(cmdDTO);
        generateCommandDTO.setCommandline(commandline);
        generateCommandDTO.setRollbackCommandline(rollbackCommandline);
        generateCommandDTO.setResultCode(ReturnCode.POLICY_MSG_OK);
        return generateCommandDTO;
    }

    @Override
    public GenerateCommandDTO generateCommandForFiveBalance(CmdDTO cmdDTO) {
        String commandline = "无法生成该设备的命令行[命令行生成数据对象不合法]";
        String rollbackCommandline = "无法生成该设备的命令行[命令行生成数据对象不合法]";
        vendorManager.getVendorInfo(cmdDTO);
        boolean done = generateCommon(cmdDTO);

        GenerateCommandDTO generateCommandDTO = new GenerateCommandDTO();
        if (!done || StringUtils.isNotEmpty(cmdDTO.getBusinessInfoDTO().getOtherErrorMsg())) {
            commandline = ERROR_PREFIX + "命令行生成处理异常！";
            rollbackCommandline = ERROR_ROLLBACK_PREFIX;
            if (StringUtils.isNotEmpty(cmdDTO.getBusinessInfoDTO().getOtherErrorMsg())){
                commandline = cmdDTO.getBusinessInfoDTO().getOtherErrorMsg();
            }
            generateCommandDTO.setCommandline(commandline);
            generateCommandDTO.setRollbackCommandline(rollbackCommandline);
            generateCommandDTO.setResultCode(ReturnCode.FAILED);
            return generateCommandDTO;
        }

        PolicyEnum policyType = cmdDTO.getPolicy().getType();
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        vendorManager.getGenerator(policyType, deviceDTO, cmdDTO.getProcedure());
        log.info("F5设备生成命令行参数为:{}",JSONObject.toJSONString(cmdDTO));
        commandline = commandlineManager.generate(cmdDTO);
        rollbackCommandline = commandlineManager.generateRollback(cmdDTO);
        generateCommandDTO.setCommandline(commandline);
        generateCommandDTO.setRollbackCommandline(rollbackCommandline);
        generateCommandDTO.setResultCode(ReturnCode.POLICY_MSG_OK);
        return generateCommandDTO;
    }

    /**
     * 定位deny是主流程 ,设备支持的可编辑策略有限
     * @param cmdDTO
     * @return
     */
    private boolean findDenyAndDeviceMatchEdit(CmdDTO cmdDTO){
        //查询deny策略
        DenyPolicyInfoDTO policyIdByFirstDeny = policyMergeNewTaskService.getPolicyIdByFirstDeny(cmdDTO,false);
        cmdDTO.setPolicyIdByFirstDeny(policyIdByFirstDeny);
        DeviceModelNumberEnum modelNumber = cmdDTO.getDevice().getModelNumber();
        boolean isEdit = false;
        switch (modelNumber){
            case SSG:
            case USG6000:
            case CISCO:
            case CISCO_ASA_86:
            case DPTECHR004:
            case DPTECHR003:
            case SRX:
            case SRX_NoCli:
            case FORTINET:
            case FORTINET_V5:
            case FORTINET_V5_2:
            case HILLSTONE_R5:
            case HILLSTONE:
            case H3CV5:
            case H3CV7:
            case TOPSEC_NG:
            case TOPSEC_NG2:
            case TOPSEC_TOS_005:
            case TOPSEC_TOS_010_020:
            case TOPSEC_NG3:
            case TOPSEC_NG4:
            case PALO_ALTO:
            case SANG_FOR_IMAGE:
            case VENUSTECHVSOS_V263:
                List<Integer> steps = cmdDTO.getProcedure().getSteps();
                steps.add(SubServiceEnum.EDIT_POLICY.getCode());
                cmdDTO.getProcedure().setSteps(steps);
                isEdit = true;
                break;
            default:
                policyMergeNewTaskService.setPolicyId2SwapId(policyIdByFirstDeny,cmdDTO);
                break;
        }
        return isEdit;
    }

    /**
     * 仿真和生成的公共方法
     *
     * @param cmdDTO
     * @return
     */
    @Override
    public boolean generateCommon(CmdDTO cmdDTO) {
        boolean done = true;
        ProcedureDTO procedure = cmdDTO.getProcedure();
        CommandLineBusinessInfoDTO businessInfoDTO = cmdDTO.getBusinessInfoDTO();
        List<Integer> steps = procedure.getSteps();
        log.info("生成命令行数据为{}", JSONObject.toJSONString(cmdDTO, true));
        for (Integer step : steps) {
            log.info(String.format("命令行生成进行%s步骤", step));
            try {
                SubServiceEnum subService = SubServiceEnum.valueOf(step);

                String serviceName = NameUtils.getServiceDefaultName(subService.getServiceClass());
                log.info("开始进行{},调用{}服务", subService.getDesc(), serviceName);
                if (cmdServiceMap.containsKey(serviceName)) {
                    CmdService service = cmdServiceMap.get(serviceName);
                    if (service != null) {
                        service.modify(cmdDTO);
                    } else {
                        log.error("查找到服务{}为空！已注册子服务为{}", serviceName, JSONObject.toJSONString(cmdServiceMap, true));
                    }
                } else {
                    log.error("查找不到服务{}!已注册子服务为{}", serviceName, JSONObject.toJSONString(cmdServiceMap, true));
                }
            } catch (UnInterruptException e) {
                log.error("命令行对象修饰服务异常！", e);

                //非阻塞异常，命令行生成步骤继续进行
                //continue;
            } catch (Exception e) {
                log.error("命令行对象修饰出错！", e);
                businessInfoDTO.setOtherErrorMsg(e.getMessage());
                //阻塞异常，命令行生成过程跳出
                done = false;
                break;
            }
        }

        return done;
    }


    @Override
    public CmdDTO generateDisasterRecovery(CmdDTO cmdDTO, UserInfoDTO userInfoDTO, NodeEntity anotherDeviceByIp, DeviceDTO disasterRecoveryDevice) {
        CmdDTO disasterCmdDTO = new CmdDTO();
        if (ObjectUtils.isNotEmpty(anotherDeviceByIp)) {
            log.info("-------------------检测到灾备设备：{} ，开始生成灾备设备命令行--------------------", anotherDeviceByIp.getIp());

            // 生成灾备设备的cmdDTO


            SettingDTO setting = new SettingDTO();
            BeanUtils.copyProperties(cmdDTO.getSetting(), setting);
            disasterCmdDTO.setSetting(setting);
            PolicyDTO policy = new PolicyDTO();
            BeanUtils.copyProperties(cmdDTO.getPolicy(), policy);
            disasterCmdDTO.setPolicy(policy);
            disasterRecoveryDevice.setDeviceUuid(anotherDeviceByIp.getUuid());
            disasterRecoveryDevice.setNodeEntity(anotherDeviceByIp);
            DeviceModelNumberEnum modelNumber = DeviceModelNumberEnum.fromString(anotherDeviceByIp.getModelNumber());
            disasterRecoveryDevice.setModelNumber(modelNumber);
            disasterCmdDTO.setDevice(disasterRecoveryDevice);
            TaskDTO task = cmdDTO.getTask();
            TaskDTO disasterTask = new TaskDTO();
            BeanUtils.copyProperties(task, disasterTask);
            disasterCmdDTO.setTask(disasterTask);
            disasterCmdDTO.getDevice().setIsDisasterDevice(true);
        }

        return disasterCmdDTO;
    }

    /**
     * 是否为灾备设备
     *
     * @param cmdDTO
     * @return
     */
    @Override
    public List<NodeEntity> isDisasterRecoveryDevice(CmdDTO cmdDTO, DeviceDTO disasterRecoveryDevice) {
        DeviceDTO device = cmdDTO.getDevice();
        NodeEntity node = recommendTaskManager.getTheNodeByUuid(device.getDeviceUuid());
        if (node == null) {
            log.info("未查询到设备：{}", device.getDeviceUuid());
            return null;
        }
        List<NodeEntity> anotherDeviceByIp = advancedSettingService.getAnotherDeviceByIp(AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY, node.getIp());

        return anotherDeviceByIp;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<CommandTaskEditableEntity> saveDisasterDeviceCommandline(CmdDTO cmdDTO, List<NodeEntity> anotherDeviceByIpList, UserInfoDTO userInfoDTO) {
        List<CommandTaskEditableEntity> nodeEntityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(anotherDeviceByIpList)) {
            for (NodeEntity anotherDeviceByIp : anotherDeviceByIpList) {
                TaskDTO task  = cmdDTO.getTask();
                CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
                entity.setCreateTime(new Date());
                entity.setStatus(PushConstants.PUSH_INT_PUSH_GENERATING);
                entity.setUserName(task.getUserName());
                entity.setTheme(task.getTheme());
                entity.setDeviceUuid(anotherDeviceByIp.getUuid());
                entity.setTaskId(task.getTaskId());
                entity.setTaskType(null == cmdDTO.getTask().getTaskType() ? PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED : cmdDTO.getTask().getTaskType());
                entity.setBranchLevel(userInfoDTO.getBranchLevel());
                entity.setUserName(userInfoDTO.getId());
                commandTaskManager.addCommandEditableEntityTask(entity);
                nodeEntityList.add(entity);
            }
            return nodeEntityList;
        }
        return Collections.emptyList();
    }

    /**
     * 修改命令行对象
     *
     * @param id
     * @param commandLine
     * @param revertCommandline
     * @param status
     */
    @Override
    public void updateCommandStatus(Integer id, String commandLine, String revertCommandline, Integer status,String editPolicyTotalInfo,String errorMsg) {
        CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
        entity.setId(id);
        entity.setCommandline(commandLine);
        entity.setCommandlineRevert(revertCommandline);
        entity.setStatus(status);
        entity.setMergeInfo(editPolicyTotalInfo);
        entity.setErrorMsg(errorMsg);
        commandTaskManager.update(entity);
    }

    @Override
    public void updateCommandByEntity(CommandTaskEditableEntity entity) {
        commandTaskManager.update(entity);
    }

    /**
     * 修改任务的状态
     *
     * @param taskId
     * @param status
     */
    @Override
    public void updateTaskStatus(Integer taskId, Integer status) {
        policyRecommendTaskService.updateTaskStatus(taskId, status);
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
}
