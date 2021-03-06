package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.auto.service.PushVerifyService;
import com.abtnetworks.totems.common.config.ShowStandbyCommandConfig;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ConnectTypeEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.PythonPushDTO;
import com.abtnetworks.totems.issued.dto.SpecialParamDTO;
import com.abtnetworks.totems.issued.dto.StandbyDeviceInfoDTO;
import com.abtnetworks.totems.issued.send.IssuedApiParamService;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dao.mysql.PushPwdStrategyMapper;
import com.abtnetworks.totems.push.dto.BatchCommandTaskDTO;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.entity.PushPwdStrategyEntity;
import com.abtnetworks.totems.push.enums.CommonEnum;
import com.abtnetworks.totems.push.manager.impl.PushTaskManagerImpl;
import com.abtnetworks.totems.push.service.ParticularParamCommonService;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.push.service.executor.Executor;
import com.abtnetworks.totems.push.service.executor.impl.*;
import com.abtnetworks.totems.push.vo.PushPwdStrategyVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.vo.RecommendPolicyVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class PushServiceImpl implements PushService {
    private static Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    @Value("${python-directory.fileDir}")
    private String pyFileBasedir;

    @Autowired
    private PushTaskManagerImpl pushTaskService;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    private WhaleManager whaleManager;

    /**??????????????????**/
    @Autowired
    SendCommandService sendCommandService;
    /**????????????**/
    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;
    /**???????????????????????????????????????*/
    @Autowired
    IssuedApiParamService issuedApiParamService;
    @Autowired
    private PushPwdStrategyMapper pushPwdStrategyMapper;
    @Autowired
    ParticularParamCommonService particularParamCommonService;

    @Autowired
    PushVerifyService pushVerifyService;

    @Autowired
    ShowStandbyCommandConfig showStandbyCommandConfig;


    @Override
    public int pushCommand(CommandTaskDTO commandTaskDTO) {
        //??????????????????
        long start = System.currentTimeMillis();
        logger.info("????????????...,???????????????{}???",start);
        String order = commandTaskDTO.getTheme();
        logger.info("???????????????????????????" + order);
        List<CommandTaskEditableEntity> commandTaskList = commandTaskDTO.getList();
        logger.info("??????????????????????????????" + commandTaskList.size());
        if(commandTaskList.size() == 0) {
            logger.info("????????????????????????0?????????????????????");
            return ReturnCode.POLICY_PUSH_NODEVICE_TO_PUSH_COMMAND;
        }
        boolean revert = commandTaskDTO.isRevert();


        Set<String> pushErrorDeviceSet = new HashSet<>();
        for(CommandTaskEditableEntity commandTaskEntity : commandTaskList) {
            pushCommandDetail(commandTaskEntity, pushErrorDeviceSet, revert);
        }
        int taskId = commandTaskDTO.getTaskId();

        List<PathInfoEntity> pathInfoEntityList = recommendTaskService.getPathInfoByTaskId(taskId);
        for(PathInfoEntity entity: pathInfoEntityList) {
            int pathInfoId = entity.getId();
//          KSH-4456  ???????????????????????????????????????????????????????????????????????????????????? 20200708
//            if(entity.getEnablePath().equals(PolicyConstants.PATH_ENABLE_DISABLE)) {
//                logger.info(String.format("??????(%d)??????????????????????????????...", pathInfoId));
//                continue;
//            }
            boolean pushError = false;
            List<RecommendPolicyVO> policyVOList = recommendTaskService.getPolicyByPathInfoId(pathInfoId);
            for(RecommendPolicyVO policyVO : policyVOList) {
                if(pushErrorDeviceSet.contains(policyVO.getDeviceUuid())) {
                    pushError = true;
                    break;
                }
            }

            if(pushError) {
                recommendTaskService.updatePathPushStatus(entity.getId(), PolicyConstants.POLICY_INT_RECOMMEND_PUSH_FAILED);
            } else {
                recommendTaskService.updatePathPushStatus(entity.getId(), PolicyConstants.POLICY_INT_RECOMMEND_PUSH_SUCCESS);
            }
        }

        //????????????????????????
        List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskManager.getCommandTaskByTaskId(commandTaskDTO.getTaskId());
        int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(taskEditableEntityList);
        int policyStatusByPushStatus = recommendTaskService.getPolicyStatusByPushStatus(pushStatusInTaskList);
        recommendTaskService.updateTaskStatus(commandTaskDTO.getTaskId(), policyStatusByPushStatus);

        long end = System.currentTimeMillis();
        long consume = end - start;
        logger.info("????????????...,???????????????{}???,?????????{}?????????",end,consume);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int pushCommandV2(BatchCommandTaskDTO batchCommandTaskDTO) {

        List<CommandTaskEditableEntity> commandTaskList = batchCommandTaskDTO.getList();
        logger.info("??????????????????????????????" + commandTaskList.size());
        if (commandTaskList.size() == 0) {
            logger.info("????????????????????????0?????????????????????");
            return ReturnCode.POLICY_PUSH_NODEVICE_TO_PUSH_COMMAND;
        }
        boolean revert = batchCommandTaskDTO.isRevert();

        Set<String> pushErrorDeviceSet = new HashSet<>();
        for (CommandTaskEditableEntity commandTaskEntity : commandTaskList) {
            pushCommandDetail(commandTaskEntity, pushErrorDeviceSet, revert);
        }


        // ?????????????????????????????? ??????????????????????????????
        List<Integer> taskIds = batchCommandTaskDTO.getTaskIds();

        for (Integer taskId : taskIds){

            List<PathInfoEntity> pathInfoEntityList = recommendTaskService.getPathInfoByTaskId(taskId);
            for(PathInfoEntity entity: pathInfoEntityList) {
                int pathInfoId = entity.getId();
                boolean pushError = false;
                List<RecommendPolicyVO> policyVOList = recommendTaskService.getPolicyByPathInfoId(pathInfoId);
                for(RecommendPolicyVO policyVO : policyVOList) {
                    if(pushErrorDeviceSet.contains(policyVO.getDeviceUuid())) {
                        pushError = true;
                        break;
                    }
                }

                if(pushError) {
                    recommendTaskService.updatePathPushStatus(entity.getId(), PolicyConstants.POLICY_INT_RECOMMEND_PUSH_FAILED);
                } else {
                    recommendTaskService.updatePathPushStatus(entity.getId(), PolicyConstants.POLICY_INT_RECOMMEND_PUSH_SUCCESS);
                }
            }
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int pushCommandDevice(CommandTaskDTO commandTaskDTO) {
        //??????????????????
        long start = System.currentTimeMillis();
        logger.info("????????????...,???????????????{}???",start);
        String order = commandTaskDTO.getTheme();
        logger.info("???????????????????????????" + order);
        List<CommandTaskEditableEntity> commandTaskList = commandTaskDTO.getList();
        logger.info("??????????????????????????????" + commandTaskList.size());
        if(commandTaskList.size() == 0) {
            logger.info("????????????????????????0?????????????????????");
            return ReturnCode.POLICY_PUSH_NODEVICE_TO_PUSH_COMMAND;
        }

        Integer id = commandTaskList.get(0).getId();
        boolean revert = commandTaskDTO.isRevert();

        Set<String> pushErrorDeviceSet = new HashSet<>();
        for(CommandTaskEditableEntity commandTaskEntity : commandTaskList) {
            pushCommandDetail(commandTaskEntity, pushErrorDeviceSet, revert);
        }

        int taskId = commandTaskDTO.getTaskId();

        List<PathInfoEntity> pathInfoEntityList = recommendTaskService.getPathInfoByTaskId(taskId);
        for(PathInfoEntity entity: pathInfoEntityList) {
            int pathInfoId = entity.getId();
            boolean pushError = false;
            List<RecommendPolicyVO> policyVOList = recommendTaskService.getPolicyByPathInfoId(pathInfoId);
            for(RecommendPolicyVO policyVO : policyVOList) {
                if(pushErrorDeviceSet.contains(policyVO.getDeviceUuid())) {
                    pushError = true;
                    break;
                }
            }

            //????????????????????????--->???????????????????????????????????????????????????
            if(pushError) {
                recommendTaskService.setPathInfoStatus(entity.getId(),PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED,PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED
                        ,PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED,PolicyConstants.POLICY_INT_RECOMMEND_PUSH_FAILED);
            } else {
                recommendTaskService.setPathInfoStatus(entity.getId(),PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED,PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED,
                        PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED,PolicyConstants.POLICY_INT_RECOMMEND_PUSH_SUCCESS);
            }

        }

        //????????????????????????
        List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskManager.getCommandTaskByTaskId(commandTaskDTO.getTaskId());
        int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(taskEditableEntityList);
        int policyStatusByPushStatus = recommendTaskService.getPolicyStatusByPushStatus(pushStatusInTaskList);
        recommendTaskService.updateTaskStatus(commandTaskDTO.getTaskId(), policyStatusByPushStatus);

        long end = System.currentTimeMillis();
        long consume = end - start;
        logger.info("????????????...,???????????????{}???,?????????{}?????????",end,consume);
        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * ?????????????????????????????????
     */
    private void pushCommandDetail (CommandTaskEditableEntity commandTaskEntity, Set<String> pushErrorDeviceSet, boolean revert) {
        Boolean pushFail = false;
        String deviceUuid = commandTaskEntity.getDeviceUuid();
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO = issuedApiParamService.recommendTaskManagerToIssued(pushCmdDTO,commandTaskEntity);
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = null;
        SpecialParamDTO specialParamDTO = new SpecialParamDTO();
        if(deviceRO == null || deviceRO.getData() == null ||deviceRO.getData().size() ==0 ) {
            logger.error("????????????????????????????????????????????????");
        } else {
            deviceData = deviceRO.getData().get(0);
            DeviceDTO deviceCheckPointDTO ;

            Integer ipType = null == pushCmdDTO.getMoveParamDTO() ? null : pushCmdDTO.getMoveParamDTO().getIpType();
            MoveParamDTO moveParamDTO ;

            Integer idleTime = null == pushCmdDTO.getRecommendTask2IssuedDTO() ? null :
                    null == pushCmdDTO.getRecommendTask2IssuedDTO().getIdleTime() ? null: Integer.valueOf(pushCmdDTO.getRecommendTask2IssuedDTO().getIdleTime());
            // 1.??????????????? 2.???????????????
            Integer longConnect = null == idleTime ? ConnectTypeEnum.SHORT_CONNECT.getCode() : ConnectTypeEnum.LONG_CONNECT.getCode();
            String connectType = longConnect.toString();

            if (deviceData.getIsVsys() != null) {
                deviceUuid = deviceData.getRootDeviceUuid();
                pushCmdDTO.setIsVSys(deviceData.getIsVsys());
                pushCmdDTO.setVSysName(deviceData.getVsysName());
                moveParamDTO = advancedSettingService.getMoveByDeviceUuidAndParam(deviceData.getUuid(),connectType);

                DeviceDTO deviceDTO = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants
                        .PARAM_NAME_TOPSEC_GROUP_NAME,deviceData.getUuid());
                if(deviceDTO!=null){
                    moveParamDTO.setGroupName(deviceDTO.getGroupName());
                }
                deviceCheckPointDTO =  advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT,deviceData.getUuid());

            }else{
                moveParamDTO = advancedSettingService.getMoveByDeviceUuidAndParam(deviceUuid,connectType);
                DeviceDTO deviceDTO = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME,deviceUuid);
                if(deviceDTO!=null){
                    moveParamDTO.setGroupName(deviceDTO.getGroupName());
                }

                deviceCheckPointDTO =  advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT,deviceUuid);

            }
            String movePosition = commandTaskEntity.getMovePosition();
            if(StringUtils.isNotBlank(movePosition)){
                moveParamDTO.setRelatedRule(movePosition);
                moveParamDTO.setRelatedName(moveParamDTO.getRelatedName());
            }
            moveParamDTO.setIpType(ipType);
            pushCmdDTO.setMoveParamDTO(moveParamDTO);
            if(deviceCheckPointDTO != null){
                BeanUtils.copyProperties(deviceCheckPointDTO,specialParamDTO);
            }
            if(StringUtils.isEmpty(specialParamDTO.getPolicyPackage())){
                List<DeviceFilterlistRO> deviceFilterListROS = whaleManager.getDeviceFilterListRO(deviceUuid);
                if (CollectionUtils.isNotEmpty(deviceFilterListROS)) {
                    DeviceFilterlistRO deviceFilterlistRO = deviceFilterListROS.get(0);
                    specialParamDTO.setPolicyPackage(deviceFilterlistRO.getName());
                }
            }
        }

        NodeEntity node = recommendTaskService.getTheNodeByUuid(deviceUuid);
        if(node == null) {
            logger.info(String.format("?????????%s???????????????????????????????????????...", deviceUuid));
            int status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR;
//                commandTaskEntity.setCommandlineEcho("???????????????????????????????????????");
//                commandTaskEntity.setStatus(status);
            if(revert) {
                commandTaskEntity.setRevertStatus(PolicyConstants.REVERT_STATUS_FAILED);
                commandTaskEntity.setCommandlineRevertEcho("????????????????????????????????????");
            } else {
                commandTaskEntity.setPushStatus(PolicyConstants.PUSH_STATUS_FAILED);
                commandTaskEntity.setCommandlineEcho("????????????????????????????????????");
            }
            commandTaskEntity.setPushTime(new Date());
            commandTaskManager.update(commandTaskEntity);
            pushErrorDeviceSet.add(deviceUuid);
            return;
        }

        //???????????????1????????????2??????
        if(node.getOrigin() == 1) {
            logger.info(String.format("?????????%s??????????????????????????????????????????...", deviceUuid));
            int status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR;
//                commandTaskEntity.setCommandlineEcho("??????????????????????????????????????????...");
//                commandTaskEntity.setStatus(status);
            if(revert) {
                commandTaskEntity.setRevertStatus(PolicyConstants.REVERT_STATUS_FAILED);
                commandTaskEntity.setCommandlineRevertEcho("??????????????????????????????????????????...");
            } else {
                commandTaskEntity.setPushStatus(PolicyConstants.PUSH_STATUS_FAILED);
                commandTaskEntity.setCommandlineEcho("??????????????????????????????????????????...");
            }
            commandTaskEntity.setPushTime(new Date());
            commandTaskManager.update(commandTaskEntity);
            pushErrorDeviceSet.add(deviceUuid);
            return;
        }


        logger.info(String.format("???????????????????????????%s(%s)", node.getDeviceName(), node.getIp()));


        if(node.getControllerId().contains("ssh") || node.getControllerId().contains("api")) {
            pushCmdDTO.setExecutorType("ssh");
        } else {
            pushCmdDTO.setExecutorType("telnet");
        }
        logger.info("?????????????????????" + pushCmdDTO.getExecutorType());


        //????????????????????????u6000???hillstone?????????????????????saveyy
        if(AliStringUtils.isEmpty(commandTaskEntity.getEditUserName())) {
            pushCmdDTO.setUserEdit(false);
        } else {
            pushCmdDTO.setUserEdit(true);
        }
        pushCmdDTO.setDeviceManagerIp(node.getIp());
        pushCmdDTO.setDeviceName(node.getDeviceName());
        specialParamDTO.setWebUrl(node.getWebUrl());
        pushCmdDTO.setSpecialParamDTO(specialParamDTO);
        String modelNumber = pushTaskService.getDeviceModelNumber(deviceUuid);
        // ???????????????????????????????????????????????????????????????????????????????????????
        String credentialUuid =  StringUtils.isBlank(node.getPushCredentialUuid()) ? node.getCredentialUuid() : node.getPushCredentialUuid();
//            String credentialUuid = pushTaskService.getCredentialUuid(deviceUuid);
        Integer port = recommendTaskService.getDeviceGatherPort(deviceUuid);
        logger.info(String.format("????????????[%s]?????????[%s]?????????[%d]",modelNumber,credentialUuid, port));

        CredentialEntity entity = pushTaskService.getCredentialEntity(credentialUuid);
        if (entity == null) {
            logger.info("???????????????");
            int status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR;
//                commandTaskEntity.setCommandlineEcho("????????????????????????????????????!");
//                commandTaskEntity.setStatus(status);
            if(revert) {
                commandTaskEntity.setRevertStatus(PolicyConstants.REVERT_STATUS_FAILED);
                commandTaskEntity.setCommandlineRevertEcho("????????????????????????????????????!");
            } else {
                commandTaskEntity.setPushStatus(PolicyConstants.PUSH_STATUS_FAILED);
                commandTaskEntity.setCommandlineEcho("????????????????????????????????????!");
            }
            commandTaskEntity.setPushTime(new Date());
            commandTaskManager.update(commandTaskEntity);
            pushErrorDeviceSet.add(deviceUuid);
            return;
        }
        pushCmdDTO.setCredentialName(entity.getName());
        pushCmdDTO.setUsername(entity.getLoginName());
        pushCmdDTO.setPassword(entity.getLoginPassword());
        pushCmdDTO.setEnableUsername(entity.getEnableUserName());
        pushCmdDTO.setEnablePassword(entity.getEnablePassword());
        pushCmdDTO.setPort(port);
        pushCmdDTO.setRevert(revert);
        pushCmdDTO.setCharset(node.getCharset());
        pushCmdDTO.setPolicyFlag(commandTaskEntity.getTheme());
        pushCmdDTO.setTaskType(commandTaskEntity.getTaskType());
        if(commandTaskEntity.getMergeInfo()!=null){
            PolicyMergePropertyEnum[] values = PolicyMergePropertyEnum.values();
            for(PolicyMergePropertyEnum policyMergePropertyEnum : values){
                if(commandTaskEntity.getMergeInfo().contains(policyMergePropertyEnum.getKey())){
                    pushCmdDTO.setMergeProperty(policyMergePropertyEnum.getCode());
                    logger.info("setMergeProperty:"+policyMergePropertyEnum.getCode());
                    break;
                }
            }
        }
        logger.info("???????????????" + modelNumber);
        if(deviceData != null && DeviceModelNumberEnum.CHECK_POINT.getKey().equalsIgnoreCase(modelNumber)){

            if (StringUtils.isEmpty(deviceData.getCpmiGatewayClusterName())) {
                //??????whale
                pushCmdDTO.getSpecialParamDTO().setCpMiGatewayClusterName(deviceData.getName());
            }else{
                //????????????
                pushCmdDTO.getSpecialParamDTO().setCpMiGatewayClusterName(deviceData.getCpmiGatewayClusterName());
            }
        }
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(modelNumber));
        particularParamCommonService.getPolicyIdForRollback(commandTaskEntity,pushCmdDTO);
        if(revert) {
            pushCmdDTO.setCommandline(commandTaskEntity.getCommandlineRevert());
            recommendTaskService.updateCommandTaskRevertStatusById(commandTaskEntity.getId(),PolicyConstants.REVERT_STATUS_REVERTING);
        } else {
            pushCmdDTO.setCommandline(commandTaskEntity.getCommandline());
            recommendTaskService.updateCommandTaskStatusById(commandTaskEntity.getId(), PolicyConstants.PUSH_STATUS_PUSHING);
        }
        pushCmdDTO.setCommandlineRevert(commandTaskEntity.getCommandlineRevert());
        // ??????????????????
        dealStandbyInfo(pushCmdDTO);
        // ???????????????????????????,?????????????????????py??????
        dealPushSpecial(pushCmdDTO);
        // ????????????
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);


        int status = PolicyConstants.PUSH_STATUS_FINISHED;
        if(pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
            status = PolicyConstants.PUSH_STATUS_FAILED;
            pushErrorDeviceSet.add(deviceUuid);
            pushFail = true;
        }

        //???????????? ?????????????????????
        logger.info("???????????????????????????");
        if(pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
            StringBuffer stringBuffer = new StringBuffer(pushResultDTO.getCmdEcho())
                    .append(SendCommandStaticAndConstants.LINE_BREAK).append(pushResultDTO.getSendErrorEnum().getMessage());
            if(revert) {
                commandTaskEntity.setCommandlineRevertEcho(stringBuffer.toString());
                commandTaskEntity.setRevertTime(new Date());
            } else {
                commandTaskEntity.setCommandlineEcho(stringBuffer.toString());
                commandTaskEntity.setPushTime(new Date());
            }
        } else {
            if(revert) {
                commandTaskEntity.setCommandlineRevertEcho(pushResultDTO.getCmdEcho());
                commandTaskEntity.setRevertTime(new Date());
                //??????????????????,???????????????????????????ruleId
                advancedSettingService.removeRuleIdByTaskId(commandTaskEntity.getTaskId());
            } else {
                commandTaskEntity.setCommandlineEcho(pushResultDTO.getCmdEcho());
                commandTaskEntity.setPushTime(new Date());
            }
        }

        //?????????????????????????????????????????????
        if(pushFail) {
            if(revert) {
                commandTaskEntity.setRevertStatus(PolicyConstants.REVERT_STATUS_FAILED);
            } else {
                commandTaskEntity.setPushStatus(PolicyConstants.PUSH_STATUS_FAILED);
            }
        } else {
            if(revert) {
                commandTaskEntity.setRevertStatus(PolicyConstants.REVERT_STATUS_FINISHED);
            } else {
                commandTaskEntity.setPushStatus(PolicyConstants.PUSH_STATUS_FINISHED);
            }
        }

        if (!pushFail && !revert && PolicyConstants.CUSTOMIZE_CMD_PUSH != pushCmdDTO.getTaskType()) {
            //?????????????????????????????????--?????????????????????????????????????????????????????????
            pushVerify(pushCmdDTO, pushResultDTO, commandTaskEntity);
        }

        logger.info(JSONObject.toJSONString(commandTaskEntity));
        commandTaskManager.update(commandTaskEntity);
    }

    /**
     * ?????????????????????
     *
     * @param pushCmdDTO
     */
    private void dealPushSpecial(PushCmdDTO pushCmdDTO) {
        String currentIp = pushCmdDTO.getDeviceManagerIp();
        String pyFileName = advancedSettingService.getPythonFileName(AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD, currentIp);
        pushCmdDTO.setPushByPython(StringUtils.isNotBlank(pyFileName) ? true : false);
        pushCmdDTO.getPythonPushDTO().setFilePath(pyFileBasedir);
        pushCmdDTO.getPythonPushDTO().setPythonFileName(pyFileName);
    }

    /**
     * ?????????????????????
     *
     * @param pushCmdDTO
     */
    private void dealStandbyInfo(PushCmdDTO pushCmdDTO) {
        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        String currentIp = pushCmdDTO.getDeviceManagerIp();
        List<NodeEntity> nodeEntityList = advancedSettingService.getAnotherDeviceByIp(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY, currentIp);
        if (CollectionUtils.isNotEmpty(nodeEntityList)) {
            for (NodeEntity nodeEntity : nodeEntityList) {
                if (nodeEntity != null && nodeEntity.getOrigin() != 1) {
                    switch (modelNumberEnum) {
                        case USG6000:
                        case USG6000_NO_TOP:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckHuaWeiCommandline());
                            break;
                        case CISCO:
                        case CISCO_ASA_86:
                        case CISCO_ASA_99:
                            StringBuffer sb = new StringBuffer();
                            sb.append(pushCmdDTO.getEnableUsername()).append(StringUtils.LF);
                            sb.append(pushCmdDTO.getEnablePassword()).append(StringUtils.LF);
                            sb.append(showStandbyCommandConfig.getCheckCiscoCommandline());
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, sb.toString());
                            break;
                        case HILLSTONE:
                        case HILLSTONE_V5:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckHillstoneCommandline());
                            break;
                        case DPTECHR003:
                        case DPTECHR004:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckDptechCommandline());
                            break;
                        case SRX:
                        case SRX_NoCli:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckJuniperSrxCommandline());
                            break;
                        case SSG:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckJuniperSsgCommandline());
                            break;
                        case TOPSEC_TOS_005:
                        case TOPSEC_TOS_010_020:
                        case TOPSEC_NG:
                        case TOPSEC_NG2:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckTopsecCommandline());
                            break;
                        case FORTINET:
                        case FORTINET_V5_2:
                            setQueryBeforeCommandLine(pushCmdDTO, nodeEntity, showStandbyCommandConfig.getCheckFortinetCommandline());
                            break;
                        default:
                            logger.info("????????????:{}??????????????????????????????,??????????????????????????????,??????????????????????????????!", modelNumberEnum.getKey());
                    }
                } else {
                    logger.info("????????????:{}??????ip:{}??????????????????????????????,??????????????????", modelNumberEnum.getKey(), currentIp);
                }
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param pushCmdDTO
     * @param param
     */
    private void setQueryBeforeCommandLine(PushCmdDTO pushCmdDTO, NodeEntity node, String param) {
        pushCmdDTO.setQueryBeforeCommandLine(param);
        pushCmdDTO.setNeedJudgeStandby(true);
        PythonPushDTO pythonPushDTO = pushCmdDTO.getPythonPushDTO();
        StandbyDeviceInfoDTO standbyDeviceInfoDTO = new StandbyDeviceInfoDTO();
        // ?????????????????????
        standbyDeviceInfoDTO.setDeviceManagerIp(node.getIp());
        standbyDeviceInfoDTO.setDeviceName(node.getDeviceName());
        Integer port = recommendTaskService.getDeviceGatherPort(node.getUuid());
        // ???????????????????????????????????????????????????????????????????????????????????????
        String credentialUuid = StringUtils.isBlank(node.getPushCredentialUuid()) ? node.getCredentialUuid() : node.getPushCredentialUuid();
        CredentialEntity entity = pushTaskService.getCredentialEntity(credentialUuid);
        if (entity == null) {
            logger.error(String.format("?????????%s???????????????????????????????????????...", standbyDeviceInfoDTO.getDeviceManagerIp()));
            return;
        }
        standbyDeviceInfoDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(node.getModelNumber()));
        standbyDeviceInfoDTO.setPort(port);
        standbyDeviceInfoDTO.setUsername(entity.getLoginName());
        standbyDeviceInfoDTO.setPassword(entity.getLoginPassword());
        standbyDeviceInfoDTO.setEnableUsername(entity.getEnableUserName());
        standbyDeviceInfoDTO.setEnablePassword(entity.getEnablePassword());
        standbyDeviceInfoDTO.setCharset(node.getCharset());
        standbyDeviceInfoDTO.setCredentialName(entity.getName());
        pythonPushDTO.setStandbyDeviceInfoDTO(standbyDeviceInfoDTO);
    }


    /**
     * ???????????????
     * @param pushCmdDTO ??????????????????
     * @return ???????????????
     */
    @Override
    public Executor getExecutor(PushCmdDTO pushCmdDTO) {
        switch(pushCmdDTO.getDeviceModelNumberEnum()) {
            case SRX:
                logger.info("??????Juniper Srx????????????????????????...");
                return new JuniperSRXExec();
            case SSG:
                return new JuniperSsgExec();
            case HILLSTONE:
            case HILLSTONE_R5:
                logger.info("????????????HillStoneR5????????????????????????...");
                return new HillStoneExec();
            case CISCO:
            case CISCO_S:
                logger.info("??????CiscoASA????????????????????????...");
                return new CiscoASAExec();
            case USG6000:
                logger.info("??????USG6000????????????????????????...");
                return new U6000Exec();
            case USG2000:
                logger.info("??????Usg2100????????????????????????...");
                return new Usg2100Exec();
            case PALO_ALTO:
                logger.info("??????Palo Alto Firewall????????????????????????...");
                return new PaloAltoExec();
            default:
                logger.info("??????????????????????????????...");
                return new CmdExec();
        }
    }


    /***
     * ??????PushCmdDTO
     */
    @Override
    public PushCmdDTO buildPushCmdDTO(String deviceUuid,String commandLines) {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = null;
        //SpecialParamDTO specialParamDTO = new SpecialParamDTO();
        if (deviceRO == null || deviceRO.getData() == null || deviceRO.getData().size() == 0) {
            logger.error("????????????????????????????????????????????????");
        } else {
            deviceData = deviceRO.getData().get(0);
            DeviceDTO deviceCheckPointDTO;
            //Integer ipType = pushCmdDTO.getMoveParamDTO().getIpType();
            //MoveParamDTO moveParamDTO;
            if (deviceData.getIsVsys() != null) {
                deviceUuid = deviceData.getRootDeviceUuid();
                pushCmdDTO.setIsVSys(deviceData.getIsVsys());
                pushCmdDTO.setVSysName(deviceData.getVsysName());
            }

            NodeEntity node = recommendTaskService.getTheNodeByUuid(deviceUuid);
            logger.info(String.format("???????????????????????????%s(%s)", node.getDeviceName(), node.getIp()));
            if (node.getControllerId().contains("ssh")) {
                pushCmdDTO.setExecutorType("ssh");
            } else {
                pushCmdDTO.setExecutorType("telnet");
            }
            logger.info("?????????????????????" + pushCmdDTO.getExecutorType());

            pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(node.getModelNumber()));
            pushCmdDTO.setDeviceManagerIp(node.getIp());
            pushCmdDTO.setDeviceName(node.getDeviceName());
            String modelNumber = pushTaskService.getDeviceModelNumber(deviceUuid);
            // ???????????????????????????????????????????????????????????????????????????????????????
            String credentialUuid = StringUtils.isBlank(node.getPushCredentialUuid()) ? node.getCredentialUuid() : node.getPushCredentialUuid();
//            String credentialUuid = pushTaskService.getCredentialUuid(deviceUuid);
            int port = recommendTaskService.getDeviceGatherPort(deviceUuid);
            logger.info(String.format("????????????[%s]?????????[%s]?????????[%d]", modelNumber, credentialUuid, port));

            CredentialEntity entity = pushTaskService.getCredentialEntity(credentialUuid);

            pushCmdDTO.setUsername(entity.getLoginName());
            pushCmdDTO.setPassword(entity.getLoginPassword());
            pushCmdDTO.setEnableUsername(entity.getEnableUserName());
            pushCmdDTO.setEnablePassword(entity.getEnablePassword());
            pushCmdDTO.setPort(port);
            //pushCmdDTO.setRevert(revert);
            pushCmdDTO.setCharset(node.getCharset());
              /* pushCmdDTO.setPolicyFlag(commandTaskEntity.getTheme());
               pushCmdDTO.setTaskType(commandTaskEntity.getTaskType());*/
            pushCmdDTO.setCommandline(commandLines);
            logger.info("???????????????" + modelNumber);
            //pushCmdDTOList.add(pushCmdDTO);
        }
        return pushCmdDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pwdStrategyOperation(PushPwdStrategyVO pwdStrategyVO) {

        /**
         * ??????
         */
        if(StringUtils.equalsIgnoreCase(pwdStrategyVO.getFlag(), CommonEnum.OperationFlag.AUPT.name())){
            PushPwdStrategyEntity pwdEntity =  pushPwdStrategyMapper.fingCmdbDeviceById(pwdStrategyVO.getId());
            if(pwdEntity !=null){
                BeanUtils.copyProperties(pwdStrategyVO,pwdEntity);
                pwdEntity.setCreateTime(new Date());
                pushPwdStrategyMapper.upadtePushDeviceById(pwdEntity);
            }
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public PushPwdStrategyEntity searchCmdDevicelist() {
        return pushPwdStrategyMapper.fingPwdStrategy();
    }

    @Override
    public String getRootDeviceUuid(String deviceUuid) {
        DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);

        if(deviceRO == null || deviceRO.getData() == null ||deviceRO.getData().size() ==0 ) {
            logger.error("????????????????????????????????????????????????");
        } else {
            DeviceDataRO deviceData = deviceRO.getData().get(0);
            if (deviceData.getIsVsys() != null) {
                deviceUuid = deviceData.getRootDeviceUuid();
            }
        }
        return  deviceUuid;
    }

    /**
     * ????????????????????????
     * @param pushCmdDTO
     * @param pushResultDTO
     * @param commandTaskEntity
     */
    private void pushVerify(PushCmdDTO pushCmdDTO, PushResultDTO pushResultDTO, CommandTaskEditableEntity commandTaskEntity) {

        try {
            String verifyCmd = pushVerifyService.getVerifyCmd(pushCmdDTO, pushResultDTO);
            DeviceModelNumberEnum deviceModelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
            if (StringUtils.isBlank(verifyCmd)) {
                logger.info(String.format("????????????:%s?????????????????? ??????...", deviceModelNumberEnum.getKey()));
                return;
            }
            int taskType = pushCmdDTO.getTaskType();
            if (StringUtils.isNotEmpty(verifyCmd)) {
                //???????????????????????????????????????
                pushCmdDTO.setCommandline(verifyCmd);
                pushCmdDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType
                        || PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType) {
                    //??????????????????????????????
                    pushCmdDTO.setTaskType(PolicyConstants.CUSTOMIZE_CMD_PUSH);
                }
                //????????????????????????
                pushCmdDTO.setBeforeCmdEcho(null);
                //??????????????????
                pushCmdDTO.setVerifyFlag(true);
                //????????????????????????
                PushResultDTO verifyResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);

                logger.info( "-----?????????????????????------:{}", JSONObject.toJSON( verifyResultDTO ) );

                if (verifyResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
                    StringBuffer stringBuffer = new StringBuffer(verifyResultDTO.getCmdEcho())
                            .append(SendCommandStaticAndConstants.LINE_BREAK).append(verifyResultDTO.getSendErrorEnum().getMessage());
                    commandTaskEntity.setVerifyEcho(stringBuffer.toString());
                } else {
                    commandTaskEntity.setVerifyEcho(verifyResultDTO.getCmdEcho());
                }

                String cmdEcho = commandTaskEntity.getVerifyEcho();
                if (TotemsStringUtils.isNotBlank(cmdEcho)) {
                    String[] rule_s = cmdEcho.split("rule ");
                    if (rule_s.length > 1) {
                        commandTaskEntity.setVerifyEcho(rule_s[0]+"rule "+rule_s[1]);
                    } else {
                        commandTaskEntity.setVerifyEcho(cmdEcho);
                    }

                }
            } else {
                commandTaskEntity.setVerifyEcho("????????????!");
            }
            logger.info( "-----??????????????????????????????------:{}", commandTaskEntity.getVerifyEcho()  );
        } catch (Exception e) {
            logger.error("????????????????????????,???????????????",e);
        }
    }

}
