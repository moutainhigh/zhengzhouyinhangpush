package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ConnectTypeEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.credential.service.CredentialService;
import com.abtnetworks.totems.disposal.BaseService;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalCreateCommandLineRecordMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalRollbackMapper;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCredentialDTO;
import com.abtnetworks.totems.disposal.dto.DisposalRollbackOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalDeleteCommandLineRecordEntity;
import com.abtnetworks.totems.disposal.entity.DisposalRollbackEntity;
import com.abtnetworks.totems.disposal.service.DisposalDeleteCommandLineRecordService;
import com.abtnetworks.totems.disposal.service.DisposalOrderScenesService;
import com.abtnetworks.totems.disposal.service.DisposalRollbackService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dozermapper.core.Mapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @Author hw
 * @Description
 * @Date 17:22 2019/11/15
 */
@Service
public class DisposalRollbackServiceImpl extends BaseService implements DisposalRollbackService {

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    private Mapper dozerMapper;

    @Resource
    private DisposalRollbackMapper disposalRollbackDao;

    @Autowired
    private DisposalCreateCommandLineRecordMapper createCommandLineRecordDao;

    @Autowired
    private DisposalDeleteCommandLineRecordService deleteCommandLineRecordService;

    @Autowired
    private DisposalOrderScenesService disposalOrderScenesService;

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

    @Autowired
    CredentialService credentialService;

    @Resource
    RemoteBranchService remoteBranchService;

    /**
     * 开始回滚命令行下发
     * 0：未执行，1：下发完成；更新为下发完成；2：下发失败；5：下发过程中；6：正在生成命令行；7：生成命令行出错；
     * @param streamId
     * @param centerUuid
     * @param userName
     * @return
     */
    @Override
    public void startSendDeleteCommandTasks(String streamId, String centerUuid, String userName) {
        String logInfo = null;
        String errorMessage = null;
        DisposalRollbackOrderDTO rollbackOrderDTO = getByCenterUuid(centerUuid);
        ReturnT<String> rT1 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING, null);
        //获取命令行list
        List<DisposalNodeCommandLineRecordDTO> commandLineRecordDTOList = createCommandLineRecordDao
                .findListByCenterUuidOrOrderNo(centerUuid, null);
        if (commandLineRecordDTOList == null || commandLineRecordDTOList.size() == 0) {
            errorMessage = "命令行生成异常，回滚失败！";
            ReturnT<String> rT2 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_GENERATING_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号："+rollbackOrderDTO.getOrderNo()+errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }
        ReturnT<String> rT3 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RUNNING, null);
        logger.info("任务单号："+rollbackOrderDTO.getOrderNo()+"：开始回滚下发，状态更新为下发过程中！更新结果："+rT1.getMsg());
        Set<String> deviceUuidSet = new HashSet<>();
        for (DisposalNodeCommandLineRecordDTO commandLineRecordDTO : commandLineRecordDTOList) {
            if (commandLineRecordDTO != null && commandLineRecordDTO.getCommandlineRevert() != null) {
                deviceUuidSet.add(commandLineRecordDTO.getDeviceUuid());
            }
        }
        if (deviceUuidSet.size() == 0) {
            errorMessage = "无设备，回滚失败！";
            ReturnT<String> rT4 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号："+rollbackOrderDTO.getOrderNo()+errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            return;
        }
        String[] deviceUuidArray = deviceUuidSet.toArray(new String[0]);
        //查询下发设备凭证list
        List<DisposalNodeCredentialDTO> nodeCredentialList = disposalOrderScenesService.findNodeCredentialDtoList(deviceUuidArray);
        if (nodeCredentialList == null || nodeCredentialList.size() == 0) {
            errorMessage = "未找到回滚下发设备的ssh连接凭证！";
            ReturnT<String> rT5 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
            logInfo = userName + "应急封堵，任务单号："+rollbackOrderDTO.getOrderNo()+errorMessage;
            logger.error(logInfo);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
            nodeCredentialList = new ArrayList<>();
        }
        Map<String, DisposalNodeCredentialDTO> credentialDTOMap = new HashMap<>();
        for (DisposalNodeCredentialDTO nodeCredentialDTO : nodeCredentialList) {
            credentialDTOMap.put(nodeCredentialDTO.getDeviceUuid(), nodeCredentialDTO);
        }

        orderDisposalExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId,rollbackOrderDTO.getOrderName(),rollbackOrderDTO.getOrderNo(),new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                Map<String, Object> resultMap = new HashMap<>();
                Set<String> pushErrorDeviceSet = new HashSet<>();
                List<String> errList = new ArrayList<>();
                //开始下发
                logger.debug("任务单号："+rollbackOrderDTO.getOrderNo()+":orderDisposalExecutor 线程池开始执行------------------回滚-----------------------");
                for (DisposalNodeCommandLineRecordDTO commandDTO : commandLineRecordDTOList) {
                    if (commandDTO == null) {
                        String tmpErr = "命令行对象为null！";
                        logger.error(tmpErr);
                        continue;
                    }

                    DisposalNodeCredentialDTO nodeCredentialDTO = credentialDTOMap.get(commandDTO.getDeviceUuid());
                    PushResultDTO pushResultDTO = null;

                    CommandTaskEditableEntity taskEditableEntity = new CommandTaskEditableEntity();
                    taskEditableEntity.setUserName(userName);
                    taskEditableEntity.setTheme(rollbackOrderDTO.getOrderNo());
                    taskEditableEntity.setDeviceUuid(commandDTO.getDeviceUuid());
                    taskEditableEntity.setTaskId(0);
                    taskEditableEntity.setCommandline(String.valueOf(commandDTO.getCommandlineRevert()));
                    taskEditableEntity.setCreateTime(new Date());
                    taskEditableEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DELETE);

                    DisposalDeleteCommandLineRecordEntity nodeDeleteCommandLineRecordEntity = new DisposalDeleteCommandLineRecordEntity();
                    nodeDeleteCommandLineRecordEntity.setCreateUuid(commandDTO.getUuid());
                    nodeDeleteCommandLineRecordEntity.setUuid(IdGen.uuid());
                    nodeDeleteCommandLineRecordEntity.setOrderNo(rollbackOrderDTO.getOrderNo());
                    nodeDeleteCommandLineRecordEntity.setCenterUuid(rollbackOrderDTO.getCenterUuid());
                    nodeDeleteCommandLineRecordEntity.setType(2);
                    nodeDeleteCommandLineRecordEntity.setVsys(commandDTO.getVsys());
                    nodeDeleteCommandLineRecordEntity.setPDeviceUuid(commandDTO.getpDeviceUuid());
                    nodeDeleteCommandLineRecordEntity.setSrcIp(commandDTO.getSrcIp());
                    nodeDeleteCommandLineRecordEntity.setDstIp(commandDTO.getDstIp());
                    nodeDeleteCommandLineRecordEntity.setIpType(commandDTO.getIpType());
                    nodeDeleteCommandLineRecordEntity.setServiceList(commandDTO.getServiceList());
                    nodeDeleteCommandLineRecordEntity.setRoutingIp(commandDTO.getRoutingIp());

                    //判断凭证和命令行不为null
                    if (nodeCredentialDTO != null) {
                        PushCmdDTO cmdDTO = new PushCmdDTO();

                        // 获取下发凭据，如果不为空，则使用下发凭据，为空则使用采集凭据
                        if(StringUtils.isNotBlank(nodeCredentialDTO.getPushCredentialUuid())){
                            CredentialEntity credentialEntity = credentialService.getFromMysql(nodeCredentialDTO.getPushCredentialUuid());
                            if(credentialEntity != null ){
                                cmdDTO.setUsername(credentialEntity.getLoginName());
                                cmdDTO.setPassword(credentialEntity.getLoginPassword());
                                cmdDTO.setEnableUsername(credentialEntity.getEnableUserName());
                                cmdDTO.setEnablePassword(credentialEntity.getEnablePassword());
                                cmdDTO.setCredentialName(credentialEntity.getName());
                            }
                        }

                        dozerMapper.map(nodeCredentialDTO, cmdDTO);
                        cmdDTO.setCommandline(String.valueOf(commandDTO.getCommandlineRevert()));
                        cmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(commandDTO.getModelNumber()));

                        //去除虚墙的后缀，例如：192.168.215.32(2) 的(2)
                        if (cmdDTO.getDeviceManagerIp().contains("(")) {
                            String tmpIp = cmdDTO.getDeviceManagerIp().substring(0, cmdDTO.getDeviceManagerIp().indexOf("("));
                            cmdDTO.setDeviceManagerIp(tmpIp);
                        }
                        // 密码解密
                        cmdDTO.setPassword(Encodes.decodeBase64Key(cmdDTO.getPassword()));
                        cmdDTO.setEnablePassword(Encodes.decodeBase64Key(cmdDTO.getEnablePassword()));

                        if(nodeCredentialDTO.getControllerId().contains("ssh")) {
                            cmdDTO.setExecutorType("ssh");
                        } else {
                            cmdDTO.setExecutorType("telnet");
                        }
                        logger.info("下发器类型为：" + cmdDTO.getExecutorType());
                        cmdDTO.setRevert(true);
                        MoveParamDTO moveParamDTO = advancedSettingService.getMoveByDeviceUuidAndParam(commandDTO.getDeviceUuid(), ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
                        cmdDTO.setMoveParamDTO(moveParamDTO);
                        //执行回滚时，将策略名传递
                        cmdDTO.setPolicyFlag(rollbackOrderDTO.getOrderNo());
                        NodeEntity node = nodeDao.getTheNodeByUuid(commandDTO.getDeviceUuid());
                        cmdDTO.setCharset(node.getCharset());
                        logger.info("回滚前，传递策略名称，cmdDto:{}", JSONObject.toJSONString(cmdDTO));
                        pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(cmdDTO);

                        //任务状态
                        int status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_DONE;
                        if(pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
                            status = PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR;
                            pushErrorDeviceSet.add(commandDTO.getDeviceUuid());
                        }

                        if(pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
                            StringBuffer stringBuffer = new StringBuffer(pushResultDTO.getCmdEcho())
                                    .append(SendCommandStaticAndConstants.LINE_BREAK).append(pushResultDTO.getSendErrorEnum().getMessage());
                            taskEditableEntity.setCommandlineEcho(stringBuffer.toString());
                        } else {
                            taskEditableEntity.setCommandlineEcho(pushResultDTO.getCmdEcho());
                        }
                        resultMap.put("设备IP："+nodeCredentialDTO.getDeviceManagerIp(), "下发结果:"+ pushResultDTO.getResult()
                                +";命令行回显:    "+taskEditableEntity.getCommandlineEcho());
                        taskEditableEntity.setStatus(status);
                        taskEditableEntity.setPushResult(String.valueOf(pushResultDTO.getResult()));
                        taskEditableEntity.setPushTime(new Date());
                    } else {
                        String deviceManageIp = nodeDao.getDeviceManageIp(commandDTO.getDeviceUuid());
                        String tmpErr = "设备ip：" + deviceManageIp + "：设备ssh连接凭证为null！";
                        logger.error(tmpErr);
                        errList.add(tmpErr);
                        taskEditableEntity.setDeviceUuid(commandDTO.getDeviceUuid());
                        taskEditableEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                        pushErrorDeviceSet.add(commandDTO.getDeviceUuid());
                    }
                    UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
                    if(userInfoDTO != null ){
                        taskEditableEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                    }else{
                        taskEditableEntity.setBranchLevel("00");
                    }
                    commandTaskEdiableDao.insert(taskEditableEntity);
                    nodeDeleteCommandLineRecordEntity.setTaskEditableId(taskEditableEntity.getId());
                    deleteCommandLineRecordService.insert(nodeDeleteCommandLineRecordEntity);
                }
                //异常error集合
                resultMap.put("连接凭证异常列表" , errList);

                resultMap.put("任务单号", rollbackOrderDTO.getOrderNo());
                resultMap.put("pushErrorDeviceSet", JSONObject.toJSONString(pushErrorDeviceSet));
                String errorMessage = JSONObject.toJSONString(resultMap, SerializerFeature.PrettyFormat).replace("\\r\\n","\n");
                logger.debug("工单名称："+rollbackOrderDTO.getOrderName()+">>回滚结果:"+errorMessage);

                if (pushErrorDeviceSet.size() > 0) {
                    ReturnT<String> rT6 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR, errorMessage);
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            userName + "应急封堵，工单名称："+rollbackOrderDTO.getOrderName()+">>回滚失败");
                } else {
                    ReturnT<String> rT7 = updateStatusByCenterUuid(centerUuid, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_DONE, null);
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            userName + "应急封堵，工单名称："+rollbackOrderDTO.getOrderName()+">>回滚完成");
                }
            }
        });
    }

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalRollbackEntity disposalRollback) {

        // valid
        if (disposalRollback == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalRollbackDao.insert(disposalRollback);
        return ReturnT.SUCCESS;
    }

    /**
     * 回滚插入 方式：INSERT INTO SELECT
     * @param disposalRollback
     * @return
     */
    @Override
    public ReturnT<String> insertBySelectOrder(DisposalRollbackEntity disposalRollback) {
        disposalRollbackDao.insertBySelectOrder(disposalRollback);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalRollbackDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalRollbackEntity disposalRollback) {
        int ret = disposalRollbackDao.update(disposalRollback);
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
        int ret = disposalRollbackDao.updateStatusByCenterUuid(centerUuid, status, errorMessage);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalRollbackEntity getById(int id) {
        return disposalRollbackDao.getById(id);
    }

    @Override
    public DisposalRollbackEntity getRollbackEntity(String pCenterUuid, String centerUuid) {
        return disposalRollbackDao.getRollbackEntity(pCenterUuid, centerUuid);
    }

    /**
     * 查询 get Dto By centerUuid
     * @param centerUuid
     * @return
     */
    @Override
    public DisposalRollbackOrderDTO getByCenterUuid(String centerUuid) {
        return disposalRollbackDao.getByCenterUuid(centerUuid);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalRollbackOrderDTO> findDtoList(DisposalRollbackOrderDTO disposalRollbackOrderDTO, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalRollbackOrderDTO> list = disposalRollbackDao.findDtoList(disposalRollbackOrderDTO);
        PageInfo<DisposalRollbackOrderDTO> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

}

