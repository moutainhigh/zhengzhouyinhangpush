package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ConnectTypeEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.BaseService;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderCenterMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderScenesMapper;
import com.abtnetworks.totems.disposal.dto.*;
import com.abtnetworks.totems.disposal.entity.DisposalCreateCommandLineRecordEntity;
import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import com.abtnetworks.totems.disposal.entity.DisposalOrderEntity;
import com.abtnetworks.totems.disposal.service.DisposalCommandService;
import com.abtnetworks.totems.disposal.service.DisposalCreateCommandLineRecordService;
import com.abtnetworks.totems.disposal.service.DisposalOrderScenesService;
import com.abtnetworks.totems.disposal.service.DisposalOrderService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.SpecialParamDTO;
import com.abtnetworks.totems.issued.send.IssuedApiParamService;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dozermapper.core.Mapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Author hw
 * @Description
 * @Date 18:48 2019/11/11
 */
@Service
public class DisposalOrderServiceImpl extends BaseService implements DisposalOrderService {

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    private Mapper dozerMapper;

    @Autowired
    private DisposalOrderMapper disposalOrderDao;

    @Autowired
    private DisposalOrderCenterMapper disposalOrderCenterDao;

    @Autowired
    private DisposalOrderScenesMapper disposalOrderScenesDao;

    @Autowired
    private DisposalCommandService disposalCommandService;

    @Autowired
    private DisposalOrderScenesService disposalOrderScenesService;

    @Autowired
    private DisposalCreateCommandLineRecordService createCommandLineRecordService;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEdiableDao;

    @Autowired
    private NodeMapper nodeDao;

    @Qualifier("orderDisposalExecutor")
    @Autowired
    private Executor orderDisposalExecutor;
    /**??????????????????**/
    @Autowired
    SendCommandService sendCommandService;

    @Autowired
    AdvancedSettingService advancedSettingService;

    private final static String STR_DENY = "deny";
    private final static String STR_PERMIT = "permit";

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    /**???????????????????????????????????????*/
    @Autowired
    IssuedApiParamService issuedApiParamService;

    @Autowired
    private WhaleManager whaleManager;

    @Resource
    RemoteBranchService remoteBranchService;

    @Value("${disposal.executor.pool.size:16}")
    private Integer disposalExecutorPoolSize;

    @Autowired
    @Qualifier(value = "commandlineExecutor")
    private Executor commandlineExecutor;

    /**
     * ???????????????????????????????????????
     */
    @Override
    public int threadGenerateCommand(String userName, String centerUuid) {
        String id = "CMD_" + centerUuid;
        if (ExtendedExecutor.containsKey(id)) {
            logger.warn(String.format("?????????????????????(%d)????????????????????????????????????", id));
            return ReturnCode.TASK_ALREADY_EXIST;
        }

        commandlineExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "", "", new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    generateCommand(userName, centerUuid);
                } catch (Exception e) {
                    logger.error("????????????:", e);
                    throw e;
                }
            }
        });

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * ??????????????????????????????????????????
     */
    private void generateCommand(String userName, String centerUuid) {
        DisposalOrderDTO orderDTO = getByCenterUuid(centerUuid);
        if (orderDTO == null) {
            logger.error("??????Uuid????????????????????????,uuid:{}", centerUuid);
            updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, "??????????????????????????????????????????");
            return;
        }
        //??????????????????????????????????????????
        ReturnT<String> rT1 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING, null);
        //???????????????????????????
        String errorMessage = "";
        String logInfo = "";
        List<DisposalCommandDTO> commandDTOList = disposalCommandService.generateCommand(centerUuid);
        if (commandDTOList == null) {
            errorMessage = "?????????????????????";
            ReturnT<String> rT2 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "??????????????????????????????" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        } else if (commandDTOList.isEmpty()) {
//            errorMessage = "??????????????????????????????????????????";
            errorMessage = "??????????????????????????????????????????";
            ReturnT<String> rT2 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "??????????????????????????????" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }
        logger.info("????????????????????????????????????????????????????????????");

        //???????????????????????????????????????????????????(???????????????)
        Set<String> deviceUuidSet = new HashSet<>();
        //???????????????????????????????????????????????????????????????????????????DTO?????????????????????
        Map<String, DisposalCommandDTO> distinctCommandDTOMap = new HashMap<>();
        for (DisposalCommandDTO commandDTO : commandDTOList) {
            if (commandDTO == null) {
                continue;
            }
            if (!AliStringUtils.isEmpty(commandDTO.getCommandLine())) {
                deviceUuidSet.add(commandDTO.getDeviceUuid());
            }
            if (!distinctCommandDTOMap.containsKey(commandDTO.getDeviceUuid())) {
                distinctCommandDTOMap.put(commandDTO.getDeviceUuid(), null);
            }
        }

        //?????????
        if (deviceUuidSet.isEmpty()) {
            errorMessage = "????????????????????????";
            ReturnT<String> rT4 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "??????????????????????????????" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }

        //????????????????????????
        List<DisposalCommandDTO> newCommandDTOList = new ArrayList<>();
        for (DisposalCommandDTO tmpCommandDTO : commandDTOList) {
            if (tmpCommandDTO == null || StringUtils.isBlank(tmpCommandDTO.getDeviceUuid())) {
                continue;
            }

            String deviceUuid = tmpCommandDTO.getDeviceUuid();
            DisposalCommandDTO newCommandDTO = distinctCommandDTOMap.get(deviceUuid);
            if (newCommandDTO == null) {
                newCommandDTO = new DisposalCommandDTO();
                dozerMapper.map(tmpCommandDTO, newCommandDTO);
            } else {
                newCommandDTO.setCommandLine(newCommandDTO.getCommandLine() + "\n" + tmpCommandDTO.getCommandLine());
                newCommandDTO.setDeleteCommandLine(newCommandDTO.getDeleteCommandLine() + "\n" + tmpCommandDTO.getDeleteCommandLine());
            }
            //?????????????????????
            distinctCommandDTOMap.put(deviceUuid, newCommandDTO);

        }
        distinctCommandDTOMap.forEach((strKey, disposalCommandDTO) -> {
            newCommandDTOList.add(disposalCommandDTO);
        });
        logger.info("??????????????????????????????????????????");
        for (DisposalCommandDTO commandDTO : newCommandDTOList) {
            CommandTaskEditableEntity taskEditableEntity = new CommandTaskEditableEntity();
            taskEditableEntity.setUserName(userName);
            UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
            if(userInfoDTO != null ){
                taskEditableEntity.setBranchLevel(userInfoDTO.getBranchLevel());
            }else{
                taskEditableEntity.setBranchLevel("00");
            }
            taskEditableEntity.setTheme(orderDTO.getOrderNo());
            taskEditableEntity.setDeviceUuid(commandDTO.getDeviceUuid());
            taskEditableEntity.setTaskId(0);
            taskEditableEntity.setCommandline(commandDTO.getCommandLine());
            taskEditableEntity.setCommandlineRevert(commandDTO.getDeleteCommandLine());
            taskEditableEntity.setCreateTime(new Date());
            //????????????????????????????????????
            if (STR_DENY.equals(orderDTO.getAction())) {
                taskEditableEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY);
            } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                taskEditableEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT);
            }

            //????????????????????????????????????
            taskEditableEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);

            commandTaskEdiableDao.insert(taskEditableEntity);
        }

        logger.info("????????????????????????");
        updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_SUCCESS, "");
        //??????????????????????????????
        String commandDTOListMessage = JSONObject.toJSONString(commandDTOList, SerializerFeature.PrettyFormat).replace("\\r\\n", "\n");
        logger.info("????????????" + orderDTO.getOrderNo() + ">>?????????:" + commandDTOListMessage);

    }

    /**
     * ???????????????????????????
     * 0???????????????1??????????????????????????????????????????2??????????????????5?????????????????????6???????????????????????????7???????????????????????????
     * @param streamId
     * @param centerUuid
     * @param userName
     */
    @Override
    public void startSendCommandTasks(String streamId, String centerUuid, String userName) {
        String logInfo = null;
        String errorMessage = null;
        DisposalOrderDTO orderDTO = getByCenterUuid(centerUuid);
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                userName + "????????????????????????" + orderDTO.getOrderNo() + "??????????????????" + orderDTO.getOrderName() + " ????????????");

        //???????????????????????????
        ReturnT<String> rT3 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RUNNING, null);
        logger.info("???????????????" + orderDTO.getOrderNo() + "??????????????????????????????????????????????????????????????????" + rT3.getMsg());

        //???????????????list
        Map<String, Object> ediableMap = new HashMap<>();
        ediableMap.put("theme", orderDTO.getOrderNo());
        if (STR_DENY.equals(orderDTO.getAction())) {
            ediableMap.put("taskType", PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY);
        } else if (STR_PERMIT.equals(orderDTO.getAction())) {
            ediableMap.put("taskType", PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT);
        }
        List<CommandTaskEditableEntity> newCommandDTOList = commandTaskEdiableDao.selectByThemeAndTaskType(ediableMap);

        if (newCommandDTOList == null || newCommandDTOList.isEmpty()) {
            errorMessage = "????????????????????????";
            //??????????????????????????????
            ReturnT<String> rT4 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
            logInfo = userName + "??????????????????????????????" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }

        //???????????????????????????????????????  key=deviceUuid value=?????????
        Map<String, List<CommandTaskEditableEntity>> commandTaskGroupMap = newCommandDTOList.stream().collect(Collectors.groupingBy(CommandTaskEditableEntity::getDeviceUuid));

        //?????????????????????????????????????????????uuid
        Set<String> deviceUuidSet = new HashSet<>();
        for (CommandTaskEditableEntity entity : newCommandDTOList) {
            deviceUuidSet.add(entity.getDeviceUuid());
        }

        String[] deviceUuidArray = deviceUuidSet.toArray(new String[0]);
        logger.info("??????????????????????????? count:{}", deviceUuidArray.length);
        //????????????????????????list
        List<DisposalNodeCredentialDTO> nodeCredentialList = disposalOrderScenesService.findNodeCredentialDtoList(deviceUuidArray);
        if (nodeCredentialList == null || nodeCredentialList.size() == 0) {
            errorMessage = "????????????????????????ssh???????????????";
            ReturnT<String> rT5 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
            logInfo = userName + "??????????????????????????????" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            nodeCredentialList = new ArrayList<>();
        }
        //????????????map key=deviceUuid value=??????
        Map<String, DisposalNodeCredentialDTO> credentialDTOMap = new HashMap<>();
        for (DisposalNodeCredentialDTO nodeCredentialDTO : nodeCredentialList) {
            credentialDTOMap.put(nodeCredentialDTO.getDeviceUuid(), nodeCredentialDTO);
        }

        //??????????????????????????????????????????????????????1?????????
        Map<String, NodeEntity> nodeMap = new HashMap<>();
        Map<String, DeviceRO> deviceMap = new HashMap<>();
        for (String deviceUuid : deviceUuidSet) {
            //????????????
            NodeEntity node = nodeDao.getTheNodeByUuid(deviceUuid);
            if (node == null) {
                logger.error("??????????????????????????????! deviceUuid=" + deviceUuid);
                continue;
            }
            nodeMap.put(deviceUuid, node);
            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
            if (deviceRO == null || deviceRO.getData() == null || deviceRO.getData().isEmpty()) {
                logger.error("???mongodb?????????????????????! deviceUuid=" + deviceUuid);
                continue;
            }
            deviceMap.put(deviceUuid, deviceRO);
            //??????commandTaskGroupMap???????????????
            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            Boolean isVsys = deviceDataRO.getIsVsys();
            if (isVsys != null && isVsys) {
                String rootDeviceUuid = deviceDataRO.getRootDeviceUuid();
                List<CommandTaskEditableEntity> rootCommList = commandTaskGroupMap.get(rootDeviceUuid);
                List<CommandTaskEditableEntity> childrenCommList = commandTaskGroupMap.get(deviceUuid);
                //???????????????????????????
                if (childrenCommList == null || childrenCommList.isEmpty()) {
                    continue;
                }

                //??????
                if (rootCommList == null || rootCommList.isEmpty()) {
                    rootCommList = childrenCommList;
                } else {
                    rootCommList.addAll(childrenCommList);
                }
                commandTaskGroupMap.put(rootDeviceUuid, rootCommList);
                commandTaskGroupMap.remove(deviceUuid);
            }
        }

        logger.info("????????????????????? before:{},?????????????????? after:{}", newCommandDTOList.size(), commandTaskGroupMap.size());


        //????????????????????????IP??????
        final Integer checkIpv4 = InputValueUtils.checkIp(orderDTO.getSrcIp()) != 0 ? 1 : 0;

        //order ??????????????????1????????????
        orderDisposalExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, orderDTO.getOrderName(), orderDTO.getOrderNo(), new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                Map<String, Object> resultMap = new HashMap<>();
                Set<String> pushErrorDeviceSet = new HashSet<>();
                List<String> errList = new ArrayList<>();
                //????????????
                logger.info("???????????????" + orderDTO.getOrderNo() + ":orderDisposalExecutor ?????????????????????------------------??????-----------------------");
                long startTime = System.currentTimeMillis();
                //??????????????????????????????
                int maximumPoolSize = 2 * disposalExecutorPoolSize;
                ExecutorService cachedThreadPool = new ThreadPoolExecutor(disposalExecutorPoolSize, maximumPoolSize,
                        60L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());
                for (Map.Entry<String, List<CommandTaskEditableEntity>> entry : commandTaskGroupMap.entrySet()) {
                    String rootDeviceUuid = entry.getKey();
                    List<CommandTaskEditableEntity> commandTaskList = entry.getValue();
                    //??????????????????
                    if (commandTaskList == null || commandTaskList.isEmpty()) {
                        logger.error("??????????????????null???,deviceUuid=" + rootDeviceUuid);
                        continue;
                    }

                    //?????????????????????????????????????????????????????????????????????
                    cachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            //????????????????????????for????????????????????????????????????
                            int deviceIndex = 0;
                            for (CommandTaskEditableEntity editableEntity : commandTaskList) {
                                String deviceUuid = editableEntity.getDeviceUuid();
                                NodeEntity node = nodeMap.get(deviceUuid);
                                if (node == null) {
                                    continue;
                                }
                                DeviceRO deviceRO = deviceMap.get(deviceUuid);
                                if (deviceRO == null) {
                                    continue;
                                }

                                DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
                                deviceIndex++;
                                logger.info("??????ip:{}?????????????????? i:{}????????????,??? total:{} ???", node.getIp(), deviceIndex, commandTaskList.size());

                                //????????????????????????
                                DisposalNodeCredentialDTO nodeCredentialDTO = credentialDTOMap.get(deviceUuid);
                                PushResultDTO pushResultDTO = null;

                                //??????????????????
                                DisposalCreateCommandLineRecordEntity nodeCreateCommandLineRecordEntity = new DisposalCreateCommandLineRecordEntity();
                                nodeCreateCommandLineRecordEntity.setUuid(IdGen.uuid());
                                nodeCreateCommandLineRecordEntity.setOrderNo(orderDTO.getOrderNo());
                                nodeCreateCommandLineRecordEntity.setCenterUuid(orderDTO.getCenterUuid());
                                if (STR_DENY.equals(orderDTO.getAction())) {
                                    nodeCreateCommandLineRecordEntity.setType(0);
                                } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                                    nodeCreateCommandLineRecordEntity.setType(1);
                                }

                                //??????????????????
                                Boolean isVsys = deviceDataRO.getIsVsys();
                                if (isVsys == null) {
                                    isVsys = false;
                                }

                                nodeCreateCommandLineRecordEntity.setVsys(isVsys ? 1 : 0);
                                nodeCreateCommandLineRecordEntity.setPDeviceUuid(deviceDataRO.getRootDeviceUuid());
                                nodeCreateCommandLineRecordEntity.setIpType(0);
                    /*nodeCreateCommandLineRecordEntity.setSrcIp(commandDTO.getSrcIp());
                    nodeCreateCommandLineRecordEntity.setDstIp(commandDTO.getDstIp());
                    nodeCreateCommandLineRecordEntity.setServiceList(commandDTO.getServiceList());
                    nodeCreateCommandLineRecordEntity.setRoutingIp(commandDTO.getRoutingIp());*/

                                //??????????????????????????????null
                                if (nodeCredentialDTO != null) {
                                    PushCmdDTO cmdDTO = new PushCmdDTO();
                                    dozerMapper.map(nodeCredentialDTO, cmdDTO);
                                    cmdDTO.setCommandline(editableEntity.getCommandline());
                                    cmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(node.getModelNumber()));
                                    cmdDTO.setEnableUsername(nodeCredentialDTO.getEnableUserName());
                                    //?????????????????????????????????192.168.215.32(2) ???(2)
                                    if (cmdDTO.getDeviceManagerIp().contains("(")) {
                                        String tmpIp = cmdDTO.getDeviceManagerIp().substring(0, cmdDTO.getDeviceManagerIp().indexOf("("));
                                        cmdDTO.setDeviceManagerIp(tmpIp);
                                    }
                                    cmdDTO.setIsVSys(isVsys);
                                    cmdDTO.setVSysName(deviceDataRO.getVsysName());
                                    // ????????????
                                    cmdDTO.setPassword(Encodes.decodeBase64Key(cmdDTO.getPassword()));
                                    cmdDTO.setEnablePassword(Encodes.decodeBase64Key(cmdDTO.getEnablePassword()));

                                    if (nodeCredentialDTO.getControllerId().contains("ssh")) {
                                        cmdDTO.setExecutorType("ssh");
                                    } else {
                                        cmdDTO.setExecutorType("telnet");
                                    }
                                    cmdDTO.setCharset(node.getCharset());
                                    logger.info("?????????????????????" + cmdDTO.getExecutorType());
                                    //??????????????????????????????????????????
                                    MoveParamDTO moveParamDTO = advancedSettingService.getMoveByDeviceUuidAndParam(deviceUuid, ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
                                    DeviceDTO deviceDTO = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME, deviceUuid);
                                    if (deviceDTO != null) {
                                        moveParamDTO.setGroupName(deviceDTO.getGroupName());
                                    }
                                    moveParamDTO.setIpType(checkIpv4);
                                    cmdDTO.setMoveParamDTO(moveParamDTO);
                                    if (DeviceModelNumberEnum.CHECK_POINT.getKey().equalsIgnoreCase(node.getModelNumber())) {
                                        DeviceDTO deviceCheckPointDTO = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT, node.getUuid());
                                        SpecialParamDTO specialParamDTO = new SpecialParamDTO();
                                        if (deviceCheckPointDTO != null) {
                                            BeanUtils.copyProperties(deviceCheckPointDTO, specialParamDTO);
                                        }

                                        if (StringUtils.isEmpty(specialParamDTO.getPolicyPackage())) {
                                            List<DeviceFilterlistRO> deviceFilterListROS = whaleManager.getDeviceFilterListRO(node.getUuid());
                                            if (CollectionUtils.isNotEmpty(deviceFilterListROS)) {
                                                DeviceFilterlistRO deviceFilterlistRO = deviceFilterListROS.get(0);
                                                specialParamDTO.setPolicyPackage(deviceFilterlistRO.getName());
                                            }
                                        }
                                        specialParamDTO.setWebUrl(node.getWebUrl());
                                        specialParamDTO.setCpMiGatewayClusterName(deviceDataRO.getCpmiGatewayClusterName());
                                        cmdDTO.setSpecialParamDTO(specialParamDTO);
                                    }
                                    cmdDTO.setCharset(node.getCharset());
                                    cmdDTO = issuedApiParamService.disposalToIssued(cmdDTO, node.getUuid(), orderDTO);
                                    try {
                                        // KSH-5180
                                        if (STR_DENY.equals(orderDTO.getAction())) {
                                            cmdDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY);
                                        } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                                            cmdDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT);
                                        }
                                        pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(cmdDTO);
                                    } catch (Exception e) {
                                        logger.error("???????????????????????????",e);
                                        if(pushResultDTO == null){
                                            pushResultDTO = new PushResultDTO();
                                            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
                                            pushResultDTO.setCmdEcho("???????????????"+e.getMessage());
                                            pushResultDTO.setSendErrorEnum(SendErrorEnum.SYSTEM_ERROR);
                                        }
                                    }

                                    //????????????
                                    int status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_DONE;
                                    if (pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
                                        status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR;
                                        pushErrorDeviceSet.add(deviceUuid);
                                    }

                                    if (pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
                                        StringBuffer stringBuffer = new StringBuffer(pushResultDTO.getCmdEcho())
                                                .append(SendCommandStaticAndConstants.LINE_BREAK).append(pushResultDTO.getSendErrorEnum().getMessage());
                                        editableEntity.setCommandlineEcho(stringBuffer.toString());
                                    } else {
                                        editableEntity.setCommandlineEcho(pushResultDTO.getCmdEcho());
                                    }

                                    resultMap.put("??????IP???" + nodeCredentialDTO.getDeviceManagerIp(), "????????????:" + pushResultDTO.getResult()
                                            + ";???????????????:    " + editableEntity.getCommandlineEcho());
                                    editableEntity.setStatus(status);
                                    editableEntity.setPushResult(String.valueOf(pushResultDTO.getResult()));
                                    editableEntity.setPushTime(new Date());
                                } else {
                                    String tmpErr = "??????ip???" + node.getIp() + "?????????ssh???????????????null???";
                                    logger.error(tmpErr);
                                    errList.add(tmpErr);
                                    editableEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                                    pushErrorDeviceSet.add(deviceUuid);
                                }

                                //?????????????????? ?????????????????????????????????
                                commandTaskEdiableDao.updateByPrimaryKeySelective(editableEntity);
                                logger.info("??????????????????????????????????????????????????????");
                                nodeCreateCommandLineRecordEntity.setTaskEditableId(editableEntity.getId());
                                createCommandLineRecordService.insert(nodeCreateCommandLineRecordEntity);
                                logger.info("??????????????????????????????????????????");
                            }
                        }
                    });
                }

                //??????????????????????????????????????????????????????????????????, ???????????????
                cachedThreadPool.shutdown();
                while (true) {
                    //?????????shutdown()?????????????????????????????????????????????????????????true;
                    if (cachedThreadPool.isTerminated()) {
                        break;
                    }
                }

                long endTime = System.currentTimeMillis();
                logger.info("?????? name:{} ,total:{} ????????? ??????????????????,?????? ms:{}", orderDTO.getOrderName(), commandTaskGroupMap.size(), endTime - startTime);

                //??????error??????
                resultMap.put("????????????????????????", errList);
                resultMap.put("????????????", orderDTO.getOrderNo());
                resultMap.put("pushErrorDeviceSet", JSONObject.toJSONString(pushErrorDeviceSet));
                String errorMessage = JSONObject.toJSONString(resultMap, SerializerFeature.PrettyFormat).replace("\\r\\n", "\n");
                logger.debug("???????????????" + orderDTO.getOrderName() + ">>????????????:" + errorMessage);

                if (pushErrorDeviceSet.size() > 0) {
                    ReturnT<String> rT6 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            userName + "??????????????????????????????" + orderDTO.getOrderName() + ">>????????????");
                } else {
                    ReturnT<String> rT7 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_DONE, null);
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            userName + "??????????????????????????????" + orderDTO.getOrderName() + ">>????????????");
                }
            }
        });
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> insert(DisposalOrderEntity disposalOrder) {

        // valid
        if (disposalOrder == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "??????????????????");
        }

        disposalOrderDao.insert(disposalOrder);
        return ReturnT.SUCCESS;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<Map<String, String>> saveOrUpdate(DisposalOrderDTO orderDTO) {
        DisposalOrderCenterEntity orderCenterEntity = null;
        try {
            //??????????????????
            if (!AliStringUtils.areNotEmpty(orderDTO.getOrderName(), orderDTO.getAction())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "??????????????????");
            }
            DisposalOrderEntity orderEntity = dozerMapper.map(orderDTO, DisposalOrderEntity.class);
            orderCenterEntity = dozerMapper.map(orderDTO, DisposalOrderCenterEntity.class);
            //?????? ??????UUID
            if (AliStringUtils.isEmpty(orderDTO.getOrderNo())) {
                String tmpPrefix = "";
                //0????????????1?????????
                if (orderDTO.getCategory() == 0) {
                    tmpPrefix = "P";
                } else if (orderDTO.getCategory() == 1) {
                    tmpPrefix = "R";
                }
                if (STR_DENY.equals(orderDTO.getAction())) {
                    tmpPrefix += "D";
                } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                    tmpPrefix += "A";
                }
                //?????????
                String orderNo = tmpPrefix + DateUtils.formatDate(new Date(), "yyyyMMdd") + "_" + IdGen.getRandomNumberString();
                orderCenterEntity.setOrderNo(orderNo);
                orderCenterEntity.setUuid(IdGen.uuid());
                orderEntity.setCenterUuid(orderCenterEntity.getUuid());
                orderEntity.setCreateTime(new Date());
                //0???????????????1???????????????
                orderEntity.setStatus(0);
                //0????????????1???????????????
                orderCenterEntity.setSourceClassification(0);

                disposalOrderDao.insert(orderEntity);
                disposalOrderCenterDao.insert(orderCenterEntity);
            } else {
                disposalOrderDao.update(orderEntity);
                disposalOrderCenterDao.update(orderCenterEntity);
                disposalOrderScenesDao.deleteByCenterUuid(orderCenterEntity.getUuid());
            }
            if (orderDTO.getScenesUuidArray() != null && orderDTO.getScenesUuidArray().length > 0) {
                disposalOrderScenesDao.bulkInsert(orderDTO.getScenesUuidArray(), orderCenterEntity.getUuid());
            }
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            throw new RuntimeException("????????????????????????", e);
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                orderDTO.getCreateUser() + " ?????????????????? " + orderDTO.getOrderName() + "????????????");
        Map map = new HashMap();
        if (orderCenterEntity != null) {
            map.put("uuid", orderCenterEntity.getUuid());
        }

        return new ReturnT(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<List<String>> batchSave(List<DisposalOrderDTO> list,String userName) {
        if (list == null || list.size() == 0) {
            return new ReturnT(ReturnT.FAIL_CODE, "???????????????");
        }
        List<String> centerUuidList = new ArrayList<>();
        try {
            for (DisposalOrderDTO orderDTO : list) {

                //??????????????????
                if (!AliStringUtils.areNotEmpty(orderDTO.getOrderName(), orderDTO.getAction())) {
                    return new ReturnT(ReturnT.FAIL_CODE, "??????????????????");
                }
                DisposalOrderEntity orderEntity = dozerMapper.map(orderDTO, DisposalOrderEntity.class);
                DisposalOrderCenterEntity orderCenterEntity = dozerMapper.map(orderDTO, DisposalOrderCenterEntity.class);
                //?????? ??????UUID
                String tmpPrefix = "";
                //0????????????1?????????
                if (orderDTO.getCategory() == 0) {
                    tmpPrefix = "P";
                } else if (orderDTO.getCategory() == 1) {
                    tmpPrefix = "R";
                }
                if (STR_DENY.equals(orderDTO.getAction())) {
                    tmpPrefix += "D";
                } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                    tmpPrefix += "A";
                }
                //?????????
                String orderNo = tmpPrefix + DateUtils.formatDate(new Date(), "yyyyMMdd") + "-" + IdGen.getRandomNumberString();
                orderCenterEntity.setOrderNo(orderNo);
                orderCenterEntity.setUuid(IdGen.uuid());
                orderEntity.setCenterUuid(orderCenterEntity.getUuid());
                orderEntity.setCreateTime(new Date());
                //0???????????????1???????????????
                orderEntity.setStatus(0);
                //0????????????1???????????????
                orderCenterEntity.setSourceClassification(0);

                disposalOrderDao.insert(orderEntity);
                disposalOrderCenterDao.insert(orderCenterEntity);
                if (orderDTO.getScenesUuidArray() != null && orderDTO.getScenesUuidArray().length > 0) {
                    disposalOrderScenesDao.bulkInsert(orderDTO.getScenesUuidArray(), orderCenterEntity.getUuid());
                }
                centerUuidList.add(orderCenterEntity.getUuid());
            }
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            throw new RuntimeException("??????????????????????????????", e);
        }

        ReturnT success = ReturnT.SUCCESS;
        success.setData(centerUuidList);
        return success;
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalOrderDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> update(DisposalOrderEntity disposalOrder) {
        int ret = disposalOrderDao.update(disposalOrder);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ????????????status By ????????????UUID centerUuid
     * @param centerUuid
     * @param status 0???????????????1??????????????????????????????????????????2??????????????????5?????????????????????6???????????????????????????7???????????????????????????
     * @param errorMessage ??????????????????
     * @return
     */
    @Override
    public ReturnT<String> updateStatusByCenterUuid(String centerUuid, Integer status, String errorMessage) {
        int ret = disposalOrderDao.updateStatusByCenterUuid(centerUuid, status, errorMessage);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ?????? get By Id
     */
    @Override
    public DisposalOrderEntity getById(int id) {
        return disposalOrderDao.getById(id);
    }

    @Override
    public DisposalOrderEntity getOrderEntityByCenterUuid(String centerUuid) {
        return disposalOrderDao.getOrderEntityByCenterUuid(centerUuid);
    }

    /**
     * ?????? get Dto By centerUuid
     * @param centerUuid
     * @return
     */
    @Override
    public DisposalOrderDTO getByCenterUuid(String centerUuid) {
        return disposalOrderDao.getByCenterUuid(centerUuid);
    }

    /**
     * ????????????
     */
    @Override
    public PageInfo<DisposalOrderDTO> findDtoList(DisposalOrderDTO disposalOrderDTO, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalOrderDTO> list = disposalOrderDao.findDtoList(disposalOrderDTO);
        PageInfo<DisposalOrderDTO> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * ?????????????????????????????? ????????????list AttackChainDisposalOrderDTO
     * @param queryOrderDTO
     * @return
     */
    @Override
    public List<AttackChainDisposalOrderDTO> findAttackChainDtoList(AttackChainDisposalQueryOrderDTO queryOrderDTO) {
        List<AttackChainDisposalOrderDTO> list = new ArrayList<>();
        try {
            //??????????????????????????????
            List<AttackChainDisposalOrderDTO> originalList = disposalOrderDao.findAttackChainDtoList(queryOrderDTO);
            //?????????????????????
            List<QuintupleUtils.Quintuple> filterQuintupleList = serviceJsonHandle(queryOrderDTO.getSrcIp(), queryOrderDTO.getDstIp(),
                    queryOrderDTO.getServiceList());
            //????????????
            for (AttackChainDisposalOrderDTO orderDTO : originalList) {
                List<QuintupleUtils.Quintuple> quintupleList = serviceJsonHandle(orderDTO.getSrcIp(), orderDTO.getDstIp(), orderDTO.getServiceList());

                List<QuintupleUtils.Quintuple> resultList = QuintupleUtils.quintupleFilter(quintupleList, filterQuintupleList);
                if (resultList.size() == 0) {
                    list.add(orderDTO);
                } else {
                    if (!quintupleList.equals(resultList)) {
                        list.add(orderDTO);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????", e);
        }
        return list;
    }

    /**
     * ??????service Json??????????????????????????????????????????
     * @return
     */
    private List<QuintupleUtils.Quintuple> serviceJsonHandle(String srcIps, String dstIps, String serviceJson) {
        List<QuintupleUtils.Quintuple> quintupleList = new ArrayList<>();
        List<ServiceDTO> serviceDTOS = ServiceDTOUtils.toList(serviceJson);
        if (serviceDTOS != null && serviceDTOS.size() != 0) {
            serviceDTOS.forEach(serviceDTO -> quintupleList.add(
                    QuintupleUtils.convertQuintuple(
                            srcIps, dstIps, serviceDTO.getProtocol(), serviceDTO.getSrcPorts(),serviceDTO.getDstPorts()
                    )
            ));
        } else {
            quintupleList.add(QuintupleUtils.convertQuintuple(srcIps, dstIps, null,null,null));
        }
        return quintupleList;
    }

}

