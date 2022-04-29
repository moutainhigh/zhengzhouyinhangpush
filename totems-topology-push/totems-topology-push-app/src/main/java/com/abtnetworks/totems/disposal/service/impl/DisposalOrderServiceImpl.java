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
    /**下发执行服务**/
    @Autowired
    SendCommandService sendCommandService;

    @Autowired
    AdvancedSettingService advancedSettingService;

    private final static String STR_DENY = "deny";
    private final static String STR_PERMIT = "permit";

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    /**查询工单服务给下发组装参数*/
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
     * 启用线程池，调用命令行生成
     */
    @Override
    public int threadGenerateCommand(String userName, String centerUuid) {
        String id = "CMD_" + centerUuid;
        if (ExtendedExecutor.containsKey(id)) {
            logger.warn(String.format("命令行生成任务(%d)已经存在！任务不重复添加", id));
            return ReturnCode.TASK_ALREADY_EXIST;
        }

        commandlineExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "", "", new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    generateCommand(userName, centerUuid);
                } catch (Exception e) {
                    logger.error("未知错误:", e);
                    throw e;
                }
            }
        });

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 根据封堵工单信息，生成命令行
     */
    private void generateCommand(String userName, String centerUuid) {
        DisposalOrderDTO orderDTO = getByCenterUuid(centerUuid);
        if (orderDTO == null) {
            logger.error("根据Uuid查询封堵工单为空,uuid:{}", centerUuid);
            updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, "查工单返回空，无法生成命令行");
            return;
        }
        //修改工单状态：正在生成命令行
        ReturnT<String> rT1 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING, null);
        //调用接口，正式生成
        String errorMessage = "";
        String logInfo = "";
        List<DisposalCommandDTO> commandDTOList = disposalCommandService.generateCommand(centerUuid);
        if (commandDTOList == null) {
            errorMessage = "命令行生成异常";
            ReturnT<String> rT2 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号：" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        } else if (commandDTOList.isEmpty()) {
//            errorMessage = "工单已存在，无需生成命令行！";
            errorMessage = "设备已删除，无法生成命令行！";
            ReturnT<String> rT2 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号：" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }
        logger.info("命令行生成成功，开始进行同设备的合并操作");

        //根据命令行，得到本次进行下发的设备(唯一、去重)
        Set<String> deviceUuidSet = new HashSet<>();
        //设备、命令行键值对，一个设备，可能对应多条命令行，DTO中进行字符拼接
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

        //无设备
        if (deviceUuidSet.isEmpty()) {
            errorMessage = "命令行暂未适配！";
            ReturnT<String> rT4 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号：" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }

        //将命令行进行合并
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
            //再重新覆盖进去
            distinctCommandDTOMap.put(deviceUuid, newCommandDTO);

        }
        distinctCommandDTOMap.forEach((strKey, disposalCommandDTO) -> {
            newCommandDTOList.add(disposalCommandDTO);
        });
        logger.info("命令行合并完成，开始保存入库");
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
            //标记任务类型：封堵、解封
            if (STR_DENY.equals(orderDTO.getAction())) {
                taskEditableEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY);
            } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                taskEditableEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT);
            }

            //未下发，所以，给初始状态
            taskEditableEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);

            commandTaskEdiableDao.insert(taskEditableEntity);
        }

        logger.info("命令保存入库结束");
        updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_SUCCESS, "");
        //输出生成的命令行数据
        String commandDTOListMessage = JSONObject.toJSONString(commandDTOList, SerializerFeature.PrettyFormat).replace("\\r\\n", "\n");
        logger.info("工单号：" + orderDTO.getOrderNo() + ">>命令行:" + commandDTOListMessage);

    }

    /**
     * 开始封堵命令行下发
     * 0：未执行，1：下发完成；更新为下发完成；2：下发失败；5：下发过程中；6：正在生成命令行；7：生成命令行出错；
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
                userName + "执行封堵工单号：" + orderDTO.getOrderNo() + "，工单名称：" + orderDTO.getOrderName() + " 开始下发");

        //修改状态为：下发中
        ReturnT<String> rT3 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RUNNING, null);
        logger.info("任务单号：" + orderDTO.getOrderNo() + "：开始下发，状态更新为下发过程中！更新结果：" + rT3.getMsg());

        //获取命令行list
        Map<String, Object> ediableMap = new HashMap<>();
        ediableMap.put("theme", orderDTO.getOrderNo());
        if (STR_DENY.equals(orderDTO.getAction())) {
            ediableMap.put("taskType", PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY);
        } else if (STR_PERMIT.equals(orderDTO.getAction())) {
            ediableMap.put("taskType", PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT);
        }
        List<CommandTaskEditableEntity> newCommandDTOList = commandTaskEdiableDao.selectByThemeAndTaskType(ediableMap);

        if (newCommandDTOList == null || newCommandDTOList.isEmpty()) {
            errorMessage = "命令行暂未适配！";
            //修改状态为：下发失败
            ReturnT<String> rT4 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号：" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }

        //将命令行以设备维度进行分组  key=deviceUuid value=命令行
        Map<String, List<CommandTaskEditableEntity>> commandTaskGroupMap = newCommandDTOList.stream().collect(Collectors.groupingBy(CommandTaskEditableEntity::getDeviceUuid));

        //根据本次下发的命令行取下发设备uuid
        Set<String> deviceUuidSet = new HashSet<>();
        for (CommandTaskEditableEntity entity : newCommandDTOList) {
            deviceUuidSet.add(entity.getDeviceUuid());
        }

        String[] deviceUuidArray = deviceUuidSet.toArray(new String[0]);
        logger.info("本次下发，设备数量 count:{}", deviceUuidArray.length);
        //查询下发设备凭证list
        List<DisposalNodeCredentialDTO> nodeCredentialList = disposalOrderScenesService.findNodeCredentialDtoList(deviceUuidArray);
        if (nodeCredentialList == null || nodeCredentialList.size() == 0) {
            errorMessage = "未找到下发设备的ssh连接凭证！";
            ReturnT<String> rT5 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号：" + orderDTO.getOrderNo() + errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            nodeCredentialList = new ArrayList<>();
        }
        //下发凭证map key=deviceUuid value=凭证
        Map<String, DisposalNodeCredentialDTO> credentialDTOMap = new HashMap<>();
        for (DisposalNodeCredentialDTO nodeCredentialDTO : nodeCredentialList) {
            credentialDTOMap.put(nodeCredentialDTO.getDeviceUuid(), nodeCredentialDTO);
        }

        //虚墙不能分组，必须和主墙在一起，视为1个设备
        Map<String, NodeEntity> nodeMap = new HashMap<>();
        Map<String, DeviceRO> deviceMap = new HashMap<>();
        for (String deviceUuid : deviceUuidSet) {
            //查询设备
            NodeEntity node = nodeDao.getTheNodeByUuid(deviceUuid);
            if (node == null) {
                logger.error("设备已删除，查询失败! deviceUuid=" + deviceUuid);
                continue;
            }
            nodeMap.put(deviceUuid, node);
            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
            if (deviceRO == null || deviceRO.getData() == null || deviceRO.getData().isEmpty()) {
                logger.error("从mongodb中查询设备为空! deviceUuid=" + deviceUuid);
                continue;
            }
            deviceMap.put(deviceUuid, deviceRO);
            //操作commandTaskGroupMap，移动虚墙
            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            Boolean isVsys = deviceDataRO.getIsVsys();
            if (isVsys != null && isVsys) {
                String rootDeviceUuid = deviceDataRO.getRootDeviceUuid();
                List<CommandTaskEditableEntity> rootCommList = commandTaskGroupMap.get(rootDeviceUuid);
                List<CommandTaskEditableEntity> childrenCommList = commandTaskGroupMap.get(deviceUuid);
                //虚墙没有命令行跳过
                if (childrenCommList == null || childrenCommList.isEmpty()) {
                    continue;
                }

                //合并
                if (rootCommList == null || rootCommList.isEmpty()) {
                    rootCommList = childrenCommList;
                } else {
                    rootCommList.addAll(childrenCommList);
                }
                commandTaskGroupMap.put(rootDeviceUuid, rootCommList);
                commandTaskGroupMap.remove(deviceUuid);
            }
        }

        logger.info("原始命令行大小 before:{},按设备分组后 after:{}", newCommandDTOList.size(), commandTaskGroupMap.size());


        //本次工单的源地址IP类型
        final Integer checkIpv4 = InputValueUtils.checkIp(orderDTO.getSrcIp()) != 0 ? 1 : 0;

        //order 工单线程数为1，串行的
        orderDisposalExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, orderDTO.getOrderName(), orderDTO.getOrderNo(), new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                Map<String, Object> resultMap = new HashMap<>();
                Set<String> pushErrorDeviceSet = new HashSet<>();
                List<String> errList = new ArrayList<>();
                //开始下发
                logger.info("任务单号：" + orderDTO.getOrderNo() + ":orderDisposalExecutor 线程池开始执行------------------下发-----------------------");
                long startTime = System.currentTimeMillis();
                //建立固定大小的线程池
                int maximumPoolSize = 2 * disposalExecutorPoolSize;
                ExecutorService cachedThreadPool = new ThreadPoolExecutor(disposalExecutorPoolSize, maximumPoolSize,
                        60L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());
                for (Map.Entry<String, List<CommandTaskEditableEntity>> entry : commandTaskGroupMap.entrySet()) {
                    String rootDeviceUuid = entry.getKey();
                    List<CommandTaskEditableEntity> commandTaskList = entry.getValue();
                    //基础信息判断
                    if (commandTaskList == null || commandTaskList.isEmpty()) {
                        logger.error("命令行对象为null！,deviceUuid=" + rootDeviceUuid);
                        continue;
                    }

                    //针对每个设备，并行，同一个设备多个命令行的串行
                    cachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            //线程池里面，进行for，即：单一设备命令行串行
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
                                logger.info("设备ip:{}，正在运行第 i:{}个命令行,共 total:{} 个", node.getIp(), deviceIndex, commandTaskList.size());

                                //取出该设备的凭证
                                DisposalNodeCredentialDTO nodeCredentialDTO = credentialDTOMap.get(deviceUuid);
                                PushResultDTO pushResultDTO = null;

                                //创建下发记录
                                DisposalCreateCommandLineRecordEntity nodeCreateCommandLineRecordEntity = new DisposalCreateCommandLineRecordEntity();
                                nodeCreateCommandLineRecordEntity.setUuid(IdGen.uuid());
                                nodeCreateCommandLineRecordEntity.setOrderNo(orderDTO.getOrderNo());
                                nodeCreateCommandLineRecordEntity.setCenterUuid(orderDTO.getCenterUuid());
                                if (STR_DENY.equals(orderDTO.getAction())) {
                                    nodeCreateCommandLineRecordEntity.setType(0);
                                } else if (STR_PERMIT.equals(orderDTO.getAction())) {
                                    nodeCreateCommandLineRecordEntity.setType(1);
                                }

                                //判断虚墙信息
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

                                //判断凭证和命令行不为null
                                if (nodeCredentialDTO != null) {
                                    PushCmdDTO cmdDTO = new PushCmdDTO();
                                    dozerMapper.map(nodeCredentialDTO, cmdDTO);
                                    cmdDTO.setCommandline(editableEntity.getCommandline());
                                    cmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(node.getModelNumber()));
                                    cmdDTO.setEnableUsername(nodeCredentialDTO.getEnableUserName());
                                    //去除虚墙的后缀，例如：192.168.215.32(2) 的(2)
                                    if (cmdDTO.getDeviceManagerIp().contains("(")) {
                                        String tmpIp = cmdDTO.getDeviceManagerIp().substring(0, cmdDTO.getDeviceManagerIp().indexOf("("));
                                        cmdDTO.setDeviceManagerIp(tmpIp);
                                    }
                                    cmdDTO.setIsVSys(isVsys);
                                    cmdDTO.setVSysName(deviceDataRO.getVsysName());
                                    // 密码解密
                                    cmdDTO.setPassword(Encodes.decodeBase64Key(cmdDTO.getPassword()));
                                    cmdDTO.setEnablePassword(Encodes.decodeBase64Key(cmdDTO.getEnablePassword()));

                                    if (nodeCredentialDTO.getControllerId().contains("ssh")) {
                                        cmdDTO.setExecutorType("ssh");
                                    } else {
                                        cmdDTO.setExecutorType("telnet");
                                    }
                                    cmdDTO.setCharset(node.getCharset());
                                    logger.info("下发器类型为：" + cmdDTO.getExecutorType());
                                    //特殊处理：高级设置，移动位置
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
                                        logger.error("封堵命令行下发异常",e);
                                        if(pushResultDTO == null){
                                            pushResultDTO = new PushResultDTO();
                                            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
                                            pushResultDTO.setCmdEcho("下发报错："+e.getMessage());
                                            pushResultDTO.setSendErrorEnum(SendErrorEnum.SYSTEM_ERROR);
                                        }
                                    }

                                    //任务状态
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

                                    resultMap.put("设备IP：" + nodeCredentialDTO.getDeviceManagerIp(), "下发结果:" + pushResultDTO.getResult()
                                            + ";命令行回显:    " + editableEntity.getCommandlineEcho());
                                    editableEntity.setStatus(status);
                                    editableEntity.setPushResult(String.valueOf(pushResultDTO.getResult()));
                                    editableEntity.setPushTime(new Date());
                                } else {
                                    String tmpErr = "设备ip：" + node.getIp() + "：设备ssh连接凭证为null！";
                                    logger.error(tmpErr);
                                    errList.add(tmpErr);
                                    editableEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                                    pushErrorDeviceSet.add(deviceUuid);
                                }

                                //更新命令行表 的状态及下发回显等信息
                                commandTaskEdiableDao.updateByPrimaryKeySelective(editableEntity);
                                logger.info("下发后，更新命令行状态及回显信息结束");
                                nodeCreateCommandLineRecordEntity.setTaskEditableId(editableEntity.getId());
                                createCommandLineRecordService.insert(nodeCreateCommandLineRecordEntity);
                                logger.info("下发后，保存下发记录到库结束");
                            }
                        }
                    });
                }

                //待上述的线程池，都执行完成后，父线程才能结束, 并添加日志
                cachedThreadPool.shutdown();
                while (true) {
                    //当调用shutdown()方法后，并且所有提交的任务完成后返回为true;
                    if (cachedThreadPool.isTerminated()) {
                        break;
                    }
                }

                long endTime = System.currentTimeMillis();
                logger.info("工单 name:{} ,total:{} 个设备 下发执行完成,耗时 ms:{}", orderDTO.getOrderName(), commandTaskGroupMap.size(), endTime - startTime);

                //异常error集合
                resultMap.put("连接凭证异常列表", errList);
                resultMap.put("任务单号", orderDTO.getOrderNo());
                resultMap.put("pushErrorDeviceSet", JSONObject.toJSONString(pushErrorDeviceSet));
                String errorMessage = JSONObject.toJSONString(resultMap, SerializerFeature.PrettyFormat).replace("\\r\\n", "\n");
                logger.debug("工单名称：" + orderDTO.getOrderName() + ">>下发结果:" + errorMessage);

                if (pushErrorDeviceSet.size() > 0) {
                    ReturnT<String> rT6 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            userName + "应急封堵，工单名称：" + orderDTO.getOrderName() + ">>下发失败");
                } else {
                    ReturnT<String> rT7 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_DONE, null);
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            userName + "应急封堵，工单名称：" + orderDTO.getOrderName() + ">>下发完成");
                }
            }
        });
    }

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalOrderEntity disposalOrder) {

        // valid
        if (disposalOrder == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalOrderDao.insert(disposalOrder);
        return ReturnT.SUCCESS;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<Map<String, String>> saveOrUpdate(DisposalOrderDTO orderDTO) {
        DisposalOrderCenterEntity orderCenterEntity = null;
        try {
            //验证非空参数
            if (!AliStringUtils.areNotEmpty(orderDTO.getOrderName(), orderDTO.getAction())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            DisposalOrderEntity orderEntity = dozerMapper.map(orderDTO, DisposalOrderEntity.class);
            orderCenterEntity = dozerMapper.map(orderDTO, DisposalOrderCenterEntity.class);
            //新增 赋值UUID
            if (AliStringUtils.isEmpty(orderDTO.getOrderNo())) {
                String tmpPrefix = "";
                //0：策略，1：路由
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
                //工单号
                String orderNo = tmpPrefix + DateUtils.formatDate(new Date(), "yyyyMMdd") + "_" + IdGen.getRandomNumberString();
                orderCenterEntity.setOrderNo(orderNo);
                orderCenterEntity.setUuid(IdGen.uuid());
                orderEntity.setCenterUuid(orderCenterEntity.getUuid());
                orderEntity.setCreateTime(new Date());
                //0：未执行，1：下发完成
                orderEntity.setStatus(0);
                //0：本地，1：上级派发
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
            logger.error("新增封堵工单异常", e);
            throw new RuntimeException("新增封堵工单异常", e);
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                orderDTO.getCreateUser() + " 新增封堵工单 " + orderDTO.getOrderName() + "：成功。");
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
            return new ReturnT(ReturnT.FAIL_CODE, "数据为空！");
        }
        List<String> centerUuidList = new ArrayList<>();
        try {
            for (DisposalOrderDTO orderDTO : list) {

                //验证非空参数
                if (!AliStringUtils.areNotEmpty(orderDTO.getOrderName(), orderDTO.getAction())) {
                    return new ReturnT(ReturnT.FAIL_CODE, "必要参数缺失");
                }
                DisposalOrderEntity orderEntity = dozerMapper.map(orderDTO, DisposalOrderEntity.class);
                DisposalOrderCenterEntity orderCenterEntity = dozerMapper.map(orderDTO, DisposalOrderCenterEntity.class);
                //新增 赋值UUID
                String tmpPrefix = "";
                //0：策略，1：路由
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
                //工单号
                String orderNo = tmpPrefix + DateUtils.formatDate(new Date(), "yyyyMMdd") + "-" + IdGen.getRandomNumberString();
                orderCenterEntity.setOrderNo(orderNo);
                orderCenterEntity.setUuid(IdGen.uuid());
                orderEntity.setCenterUuid(orderCenterEntity.getUuid());
                orderEntity.setCreateTime(new Date());
                //0：未执行，1：下发完成
                orderEntity.setStatus(0);
                //0：本地，1：上级派发
                orderCenterEntity.setSourceClassification(0);

                disposalOrderDao.insert(orderEntity);
                disposalOrderCenterDao.insert(orderCenterEntity);
                if (orderDTO.getScenesUuidArray() != null && orderDTO.getScenesUuidArray().length > 0) {
                    disposalOrderScenesDao.bulkInsert(orderDTO.getScenesUuidArray(), orderCenterEntity.getUuid());
                }
                centerUuidList.add(orderCenterEntity.getUuid());
            }
        } catch (Exception e) {
            logger.error("批量保存封堵工单异常", e);
            throw new RuntimeException("批量保存封堵工单异常", e);
        }

        ReturnT success = ReturnT.SUCCESS;
        success.setData(centerUuidList);
        return success;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalOrderDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalOrderEntity disposalOrder) {
        int ret = disposalOrderDao.update(disposalOrder);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新状态status By 工单内容UUID centerUuid
     * @param centerUuid
     * @param status 0：未执行，1：下发完成；更新为下发完成；2：下发失败；5：下发过程中；6：正在生成命令行；7：生成命令行出错；
     * @param errorMessage 错误异常信息
     * @return
     */
    @Override
    public ReturnT<String> updateStatusByCenterUuid(String centerUuid, Integer status, String errorMessage) {
        int ret = disposalOrderDao.updateStatusByCenterUuid(centerUuid, status, errorMessage);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
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
     * 查询 get Dto By centerUuid
     * @param centerUuid
     * @return
     */
    @Override
    public DisposalOrderDTO getByCenterUuid(String centerUuid) {
        return disposalOrderDao.getByCenterUuid(centerUuid);
    }

    /**
     * 分页查询
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
     * 攻击链使用：查询过滤 封堵工单list AttackChainDisposalOrderDTO
     * @param queryOrderDTO
     * @return
     */
    @Override
    public List<AttackChainDisposalOrderDTO> findAttackChainDtoList(AttackChainDisposalQueryOrderDTO queryOrderDTO) {
        List<AttackChainDisposalOrderDTO> list = new ArrayList<>();
        try {
            //查询受关联的工单记录
            List<AttackChainDisposalOrderDTO> originalList = disposalOrderDao.findAttackChainDtoList(queryOrderDTO);
            //过滤五元组条件
            List<QuintupleUtils.Quintuple> filterQuintupleList = serviceJsonHandle(queryOrderDTO.getSrcIp(), queryOrderDTO.getDstIp(),
                    queryOrderDTO.getServiceList());
            //循环匹配
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
            logger.error("攻击链通过五元组查询关联的封堵工单", e);
        }
        return list;
    }

    /**
     * 处理service Json格式的数据处理成标准的五元组
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

