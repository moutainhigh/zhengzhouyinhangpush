package com.abtnetworks.totems.push.service.task.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedLatchRunnable;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.tools.queue.PushBlockingQueueTool;
import com.abtnetworks.totems.common.tools.vo.PushQueueVo;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.generate.task.CmdTaskService;
import com.abtnetworks.totems.issued.annotation.PushTimeLockCheck;
import com.abtnetworks.totems.push.dao.mysql.SystemParamMapper;
import com.abtnetworks.totems.push.dto.BatchCommandTaskDTO;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.dto.MailServerConfDTO;
import com.abtnetworks.totems.push.entity.PushTaskEntity;
import com.abtnetworks.totems.push.enums.PushStatusEnum;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.push.utils.CustomMailTool;
import com.abtnetworks.totems.push.vo.NewPolicyPushVO;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.PushAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.CommandlineManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.CommonConstants.*;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static java.lang.Thread.sleep;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/19 1:24
 */
@Service
public class PushTaskServiceImpl implements PushTaskService {

    private static Logger logger = LoggerFactory.getLogger(PushTaskServiceImpl.class);

    private static final String STOP_TYPE_INTERUPT = "interrupt";

    private static final String STOP_TYPE_STOP = "stop";

    private Integer WAIT_TIME = 60 * 1000; //ms

    private static CountDownLatch scheduleLatch;

    private static Map<String, String> scheduleMap = new LinkedHashMap<>();


    @Autowired
    private PushService pushService;

    @Autowired
    private PushTaskService pushTaskServiceImpl;

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    private CommandlineService commandlineService;

    @Autowired
    private CmdTaskService cmdTaskService;

    @Autowired
    private SystemParamMapper systemParamMapper;

    @Resource
    CommandTaskEdiableMapper commandTaskEditableMapper;

    @Resource
    RemoteBranchService remoteBranchService;

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    @Qualifier(value = "commandExecutor")
    private Executor pushExecutor;

    @Autowired
    @Qualifier(value = "pushScheduleExecutor")
    private Executor pushScheduleExecutor;

    @Autowired
    @Qualifier(value = "commandlineExecutor")
    private Executor commandlineExecutor;


    @Autowired
    @Qualifier(value = "prePushTaskExecutor")
    private Executor prePushTaskExecutor;

    @Autowired
    Map<String, CmdService> cmdServiceMap;

    @Autowired
    VendorManager vendorManager;

    @Autowired
    CommandlineManager commandlineManager;


    /**
     * ?????? Service
     */
    @Autowired
    public DisposalScenesService disposalScenesService;

    @Autowired
    PushBlockingQueueTool pushBlockingQueue;


    @Transactional(rollbackFor = Exception.class)
    @Override
    @PushTimeLockCheck
    public int addCommandTaskList(List<CommandTaskDTO> taskList,boolean doPush) {
        if(Boolean.FALSE.equals(doPush)){
            return ReturnCode.PUSH_TIME_LOCKED;
        }
        long start = System.currentTimeMillis();
        logger.info("???????????????????????????...,???????????????{}???", start);

        for (CommandTaskDTO commandTaskDTO : taskList) {
            //push??????????????????????????????PT???push task????????????????????????????????????????????????
            //@modify by zy 20200513 ????????????????????????????????????????????????????????????
            boolean revert =  commandTaskDTO.isRevert();
            if(revert) {
                recommendTaskService.updateCommandTaskRevertStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_QUEUED);
            }else{
                recommendTaskService.updateCommandTaskPushStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_QUEUED);
            }

            String id = "PT_" + commandTaskDTO.getTaskId();
            if (ExtendedExecutor.containsKey(id)){
                logger.info("??????????????????["+id+"]???????????????");
                continue;

            }
            logger.info("?????????????????????" + id);

            pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
                @Override
                protected void start() throws InterruptedException, Exception {

                    try {
                        //??????????????????
                        long start = System.currentTimeMillis();
                        logger.info("????????????...??????id???{}???,???????????????{}???",id,start);

                        pushService.pushCommand(commandTaskDTO);
                        long end = System.currentTimeMillis();
                        long consume = end - start;
                        logger.info("????????????...??????id???{}???,???????????????{}???,?????????{}?????????",id,end,consume);
                    } catch ( Exception e) {
                        logger.error(e.getClass().toString());
                        taskService.updateTaskStatus(commandTaskDTO.getTaskId(), PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                        taskService.updateCommandTaskStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                        throw e;
                    }
                }
            });

        }
        long end = System.currentTimeMillis();
        long consume = end - start;
        logger.info("???????????????????????????...,???????????????{}???,?????????{}?????????", end, consume);
        return ReturnCode.POLICY_MSG_OK;
    }


    @Override
    public int addCommandTaskListV2(List<BatchCommandTaskDTO> batchTaskLists,List<Integer> taskIds) throws Exception {
        long start = System.currentTimeMillis();
        logger.info("???????????????????????????...,???????????????{}???", start);

        // ???????????????????????????
        logger.info("??????????????????--????????????????????????????????????????????????:{}???", batchTaskLists.size());
        CountDownLatch latch = new CountDownLatch(batchTaskLists.size());
        for (BatchCommandTaskDTO batchCommandTaskDTO : batchTaskLists) {
            String deviceUuid = batchCommandTaskDTO.getDeviceUuid();
            NodeEntity nodeEntity = recommendTaskService.getTheNodeByUuid(deviceUuid);
            if (null == nodeEntity) {
                logger.info("????????????uuid:{}????????????????????????", deviceUuid);
                continue;
            }
            batchCommandTaskDTO.setModelNumber(nodeEntity.getModelNumber());

            String id = "batch_push_device_" + batchCommandTaskDTO.getDeviceUuid() + IdGen.getRandomNumberString();
            if (ExtendedExecutor.containsKey(id)) {
                //??????????????????
                logger.error(String.format("??????[%s]????????????????????????????????????????????????", nodeEntity.getModelNumber()));
                //?????????????????????latch??????????????????????????????????????????????????????????????????
                latch.countDown();
                continue;
            }
            // ????????????
            batchPushCommand(latch, batchCommandTaskDTO, id);
        }
        try {
            latch.await();
        } catch (Exception e) {
            logger.error("???????????????????????????,????????????:{}", e.getMessage());
            throw e;
        }

        //????????????????????????
        for (Integer taskId : taskIds) {
            List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskManager.getCommandTaskByTaskId(taskId);
            int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(taskEditableEntityList);
            int policyStatusByPushStatus = recommendTaskService.getPolicyStatusByPushStatus(pushStatusInTaskList);
            recommendTaskService.updateTaskStatus(taskId, policyStatusByPushStatus);
        }

        long end = System.currentTimeMillis();
        long consume = end - start;
        logger.info("???????????????????????????...,???????????????{}???,?????????{}?????????", end, consume);
        return ReturnCode.POLICY_MSG_OK;

    }

    @Override
    @PushTimeLockCheck
    public int preBatchPushTaskList(List<CommandTaskDTO> taskList, boolean doPush) {
        if(Boolean.FALSE.equals(doPush)){
            return ReturnCode.PUSH_TIME_LOCKED;
        }
        // ??????????????????????????????????????????
        List<BatchCommandTaskDTO> batchTaskLists = new ArrayList<>();

        boolean revert = taskList.get(0).isRevert();

        List<Integer> pramTaskIds = new ArrayList<>();
        Map<String,List<CommandTaskEditableEntity>> listMap = new HashMap<>();
        for (CommandTaskDTO taskDTO : taskList) {
            pramTaskIds.add(taskDTO.getTaskId());
            List<CommandTaskEditableEntity> commandList = taskDTO.getList();
            if(CollectionUtils.isEmpty(commandList)){
                continue;
            }
            for (CommandTaskEditableEntity commandEntity : commandList) {
                String rootDeviceUuid = pushService.getRootDeviceUuid(commandEntity.getDeviceUuid());

                if (listMap.containsKey(rootDeviceUuid)) {
                    List<CommandTaskEditableEntity> existCommands = listMap.get(rootDeviceUuid);
                    existCommands.add(commandEntity);
                } else {
                    List<CommandTaskEditableEntity> newCommandTask = new ArrayList<>();
                    newCommandTask.add(commandEntity);
                    listMap.put(rootDeviceUuid,newCommandTask);
                }
            }
        }

        for (String deviceUuid :listMap.keySet()){
            BatchCommandTaskDTO batchCommandTaskDTO = new BatchCommandTaskDTO();
            batchCommandTaskDTO.setDeviceUuid(deviceUuid);
            batchCommandTaskDTO.setList(listMap.get(deviceUuid));
            List<Integer> taskIds = listMap.get(deviceUuid).stream().map(CommandTaskEditableEntity::getTaskId).distinct()
                    .collect(Collectors.toList());
            batchCommandTaskDTO.setTaskIds(taskIds);
            batchCommandTaskDTO.setRevert(revert);
            batchTaskLists.add(batchCommandTaskDTO);
        }


        String singeId = "pre_push_task_" + DateUtil.uniqueCurrentTimeMS();
        if (ExtendedExecutor.containsKey(singeId)) {
            logger.info("????????????????????????[" + singeId + "]???????????????");
            return ReturnCode.TASK_ALREADY_EXIST;
        }
        for (BatchCommandTaskDTO batchCommandTaskDTO : batchTaskLists) {
            // ?????????????????????????????????????????????????????????
            recommendTaskService.updateCommandTaskPushOrRevertStatus(batchCommandTaskDTO.getList(), PushConstants.PUSH_INT_PUSH_QUEUED, revert);
        }


        prePushTaskExecutor.execute(new ExtendedRunnable(new ExecutorDto(singeId,"????????????????????????","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    PushQueueVo pushQueueVo = new PushQueueVo();
                    pushQueueVo.setBatchCommandTaskDTOS(batchTaskLists);
                    pushQueueVo.setTaskIds(pramTaskIds);
                    pushQueueVo.setRevert(revert);
                    pushBlockingQueue.addQueue(pushQueueVo);
                    pushBlockingQueue.execute();
                } catch (Exception e) {
                    logger.error("????????????????????????,????????????:{}",e);
                    throw e;
                }
            }
        });

        return ReturnCode.POLICY_MSG_OK;
    }



    @Transactional(rollbackFor = Exception.class,propagation= Propagation.SUPPORTS)
    public synchronized void batchPushCommand(CountDownLatch latch, BatchCommandTaskDTO batchCommandTaskDTO, String id) {
        pushExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "??????????????????", "", new Date()), latch) {
            @Override
            protected void start() throws InterruptedException, Exception {

                try {
                    long start = System.currentTimeMillis();
                    logger.info("????????????????????????:{},????????????{}???", batchCommandTaskDTO.getModelNumber(), start);

                    pushService.pushCommandV2(batchCommandTaskDTO);
                    long end = System.currentTimeMillis();
                    long consume = end - start;
                    logger.info("????????????????????????:{},???????????????{}???,?????????{}?????????", batchCommandTaskDTO.getModelNumber(), end, consume);
                } catch (Exception e) {
                    logger.error("??????:{}???????????????????????????,????????????:{}", batchCommandTaskDTO.getModelNumber(), e.getMessage());
                    // ????????????????????????????????????
                    for (Integer taskId : batchCommandTaskDTO.getTaskIds()) {
                        taskService.updateTaskStatus(taskId, PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                    }
                    // ???????????????????????????
                    taskService.updateCommandTaskStatus(batchCommandTaskDTO.getList(), PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                    throw e;
                }
            }
        });
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    @PushTimeLockCheck
    public int addDeviceCommandTaskList(CommandTaskDTO commandTaskDTO, boolean doPush) {
        if(!doPush){
            return ReturnCode.PUSH_TIME_LOCKED;
        }
        //push??????????????????????????????PT???push task????????????????????????????????????????????????
        //@modify by zy 20200513 ????????????????????????????????????????????????????????????
        //????????????????????????
        List<CommandTaskEditableEntity> list = commandTaskDTO.getList();
        if (ObjectUtils.isEmpty(list)) {
            logger.info("????????????????????????0?????????????????????");
            return ReturnCode.POLICY_PUSH_NODEVICE_TO_PUSH_COMMAND;
        }
        for (CommandTaskEditableEntity editableEntity : list) {
            recommendTaskService.updateCommandTaskStatusById(editableEntity.getId(),PushConstants.PUSH_INT_PUSH_QUEUED);
        }

        String id = "PTD_" + commandTaskDTO.getTaskId() + commandTaskDTO.getList().get(0).getId();
        if (ExtendedExecutor.containsKey(id)){
            logger.info("??????????????????["+id+"]???????????????");
            return ReturnCode.TASK_ALREADY_EXIST;
        }
        logger.info("?????????????????????" + id);
        pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    //??????????????????
                    long start = System.currentTimeMillis();
                    logger.info("????????????...??????id???{}???,???????????????{}???",id,start);

                    pushService.pushCommandDevice(commandTaskDTO);
                    long end = System.currentTimeMillis();
                    long consume = end - start;
                    logger.info("????????????...??????id???{}???,???????????????{}???,?????????{}?????????",id,end,consume);
                } catch ( Exception e) {
                    logger.error(e.getClass().toString());
                    taskService.updateTaskStatus(commandTaskDTO.getTaskId(), PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                    taskService.updateCommandTaskStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                    throw e;
                }
            }
        });

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public List<String> stopTaskList(List<String> taskList, Integer isRevert) {
        List<String> failedList = new ArrayList();
        for(String taskId:taskList) {
            String id = "PT_"  + taskId;
            if(ExtendedExecutor.containsKey(id)) {
                logger.info("????????????:" + id);
                List<CommandTaskEditableEntity> commandTaskEditableEntities = commandTaskManager.getCommandTaskByTaskId(Integer.parseInt(taskId));
                if(CollectionUtils.isNotEmpty(commandTaskEditableEntities)){
                    CommandTaskEditableEntity commandTaskEditableEntity =  commandTaskEditableEntities.get(0);
                    Integer pushStatus = commandTaskEditableEntity.getPushStatus();
                    Integer revertStatus = commandTaskEditableEntity.getRevertStatus();
                    if(pushStatus != PushConstants.PUSH_INT_PUSH_QUEUED  && isRevert == 0){
                        failedList.add(taskId);
                        continue;
                    }
                    if(revertStatus != PushConstants.PUSH_INT_PUSH_QUEUED  && isRevert == 1){
                        failedList.add(taskId);
                        continue;
                    }
                }
                boolean stopped = ExtendedExecutor.stop(id, STOP_TYPE_STOP);
                if (!stopped) {
                    failedList.add(taskId);
                } else if (ObjectUtils.isNotEmpty(scheduleLatch) && scheduleMap.containsKey(id)) {
                    logger.info("??????????????????????????????????????????????????????id???" + id);
                    scheduleLatch.countDown();
                    scheduleMap.remove(id);
                }
            } else {
                ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) pushExecutor;
                BlockingQueue<Runnable> queue = threadPoolTaskExecutor.getThreadPoolExecutor().getQueue();
//                logger.info("queue size="+queue.size());
                String stopIds = id;
                boolean rc = queue.removeIf(runnable -> {
                    ExtendedRunnable extendedRunnable = (ExtendedRunnable) runnable;
                    logger.info("????????????????????????" + extendedRunnable.getExecutorDto().toString()+"???????????????????????????"+stopIds);
                    return stopIds.equals(extendedRunnable.getExecutorDto().getId());
                });
                if(!rc) {
                    logger.info("?????????????????????" + id);
                    failedList.add(taskId);
                } else if (ObjectUtils.isNotEmpty(scheduleLatch) && scheduleMap.containsKey(id)) {
                    logger.info("??????????????????????????????????????????????????????id???" + id);
                    scheduleLatch.countDown();
                    scheduleMap.remove(id);
                }
            }
        }

        return failedList;
    }

    @Override
    public int stopAllTasks() {
        List<PushQueueVo> pushQueueVos = pushBlockingQueue.getAllDataFromQueue();
        pushBlockingQueue.stopPush();
        logger.info("?????????????????????????????????????????????:{}", pushQueueVos.size());
        if (CollectionUtils.isEmpty(pushQueueVos)) {
            return 0;
        }
        for (PushQueueVo pushQueueVo : pushQueueVos) {
            List<Integer> taskIds = pushQueueVo.getTaskIds();
            for (Integer taskId : taskIds) {
                List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(taskId);
                if (CollectionUtils.isEmpty(taskEntityList)) {
                    continue;
                }
                for (CommandTaskEditableEntity task : taskEntityList) {
                    if (PolicyConstants.PUSH_STATUS_PUSHING == task.getPushStatus()) {
                        //??????????????????--->????????????
                        recommendTaskService.updateCommandTaskStatusById(task.getId(), PolicyConstants.PUSH_STATUS_FAILED);
                        continue;
                    }
                    if (PushConstants.PUSH_INT_PUSH_QUEUED == task.getPushStatus()) {
                        //????????????????????????--->???????????????
                        recommendTaskService.updateCommandTaskStatusById(task.getId(), PolicyConstants.PUSH_STATUS_NOT_START);
                        continue;
                    }

                    if (PolicyConstants.REVERT_STATUS_REVERTING == task.getRevertStatus()) {
                        //??????????????????--->????????????
                        recommendTaskService.updateCommandTaskRevertStatusById(task.getId(), PolicyConstants.REVERT_STATUS_FAILED);
                        continue;

                    }

                    if (PushConstants.PUSH_INT_PUSH_QUEUED == task.getRevertStatus()) {
                        //????????????????????????--->???????????????
                        recommendTaskService.updateCommandTaskRevertStatusById(task.getId(), PolicyConstants.REVERT_STATUS_NOT_START);
                        continue;
                    }
                }
            }
        }
        return pushQueueVos.size();
    }


    @Override
    public boolean checkTaskRunning(int taskId) {

        return false;
    }

    @Override
    public int addGenerateCmdTask(CmdDTO cmdDTO) {
        TaskDTO task = cmdDTO.getTask();
        String id = "CMD_" + String.valueOf(task.getId());
        if (ExtendedExecutor.containsKey(id)){
            logger.warn(String.format("?????????????????????(%s)????????????????????????????????????", id));
            return ReturnCode.TASK_ALREADY_EXIST;
        }

        commandlineExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    logger.info("?????????????????????...");
                    commandlineService.generate(cmdDTO);
                } catch ( Exception e) {
                    logger.info("????????????:", e);
                    taskService.updateCommandTaskStatus(task.getId(), PushConstants.PUSH_INT_PUSH_GENERATING_ERROR);
                    throw e;
                }
            }
        });

        return 0;
    }

    @Override
    public int pushPeriod() {
        String id = "PUSH_PERIOD";
        if (ExtendedExecutor.containsKey(id)){
            return ReturnCode.TASK_ALREADY_EXIST;
        }

        pushScheduleExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                try {
                    logger.info("??????????????????????????????...");
                    checkScheduleAndPush();
                } catch ( Exception e) {
                    logger.error("????????????:", e);
                    throw e;
                }
            }
        });

        return 0;
    }

    @Override
    public int newPolicyPush(NewPolicyPushVO vo) throws Exception{
        if(vo == null) {
//            return new ResultRO(false, "????????????");
            return ReturnCode.EMPTY_PARAMETERS;
        }
        logger.info("???????????????" + JSONObject.toJSONString(vo));

        //????????????
        int rc = 0;
        if(vo.getIpType().intValue() == IpTypeEnum.IPV4.getCode()){
            rc = InputValueUtils.checkIp(vo.getSrcIp());
        } else if(vo.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
            // ?????????ipv6,??????ipv6??????
            rc = InputValueUtils.checkIpV6(vo.getSrcIp());
        } else {
            if(vo.getUrlType().intValue() == UrlTypeEnum.IPV4.getCode()){
                rc = InputValueUtils.checkIp(vo.getSrcIp());
            } else if(vo.getUrlType().intValue() == UrlTypeEnum.IPV6.getCode()){
                // ?????????ipv6,??????ipv6??????
                rc = InputValueUtils.checkIpV6(vo.getSrcIp());
            }
        }
        if(rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc!= ReturnCode.POLICY_MSG_EMPTY_VALUE) {
//            return new ResultRO(false, ReturnCode.getMsg(rc));
            return rc;
        }

        //??????IP????????????????????????????????????????????????????????????
        if(rc == ReturnCode.INVALID_IP_RANGE && vo.getIpType().intValue() == IpTypeEnum.IPV4.getCode()) {
            vo.setSrcIp(InputValueUtils.autoCorrect(vo.getSrcIp()));
        }

        if(vo.getIpType().intValue() == IpTypeEnum.IPV4.getCode()){
            rc = InputValueUtils.checkIp(vo.getDstIp());
        } else if(vo.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
            // ?????????ipv6,??????ipv6??????
            rc = InputValueUtils.checkIpV6(vo.getDstIp());
        } else {
            // url ?????????????????????
        }
        if(rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc!= ReturnCode.POLICY_MSG_EMPTY_VALUE) {
//            return new ResultRO(false, ReturnCode.getMsg(rc));
            return rc;
        }

        //??????IP????????????????????????????????????????????????????????????
        if(rc == ReturnCode.INVALID_IP_RANGE  && vo.getIpType().intValue() == IpTypeEnum.IPV4.getCode()) {
            vo.setDstIp(InputValueUtils.autoCorrect(vo.getDstIp()));
        }

        String userName = vo.getUserName();
        String theme = vo.getTheme();

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber= "A" + simpleDateFormat.format(date);

        //??????uuid
        String deviceUuid = vo.getDeviceUuid();
        //??????uuid
        String scenesUuid = vo.getScenesUuid();

        //???????????????????????????????????????????????????????????????
        if (StringUtils.isAllBlank(deviceUuid, scenesUuid) || StringUtils.isNoneBlank(deviceUuid, scenesUuid)) {
            return ReturnCode.EMPTY_DEVICE_INFO;
        }

        //????????????????????????
        List<DisposalScenesDTO> scenesDTOList = null;
        if (StringUtils.isNotBlank(scenesUuid)) {
            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(scenesUuid);
            if (scenesEntity == null) {
                logger.error(String.format("??????UUID???%s ????????????????????????", scenesUuid));
                return ReturnCode.EMPTY_DEVICE_INFO;
            } else {
                scenesDTOList = disposalScenesService.findByScenesUuid(scenesUuid);
                if (CollectionUtils.isEmpty(scenesDTOList)) {
                    logger.error(String.format("??????UUID???%s ???????????????????????????????????????", scenesUuid));
                    return ReturnCode.EMPTY_DEVICE_INFO;
                }
            }
        }

        if (StringUtils.isNotBlank(deviceUuid)) {
            NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
            if(node == null) {
                logger.error(String.format("??????UUID???%s ???????????????????????????", deviceUuid));
                return ReturnCode.EMPTY_DEVICE_INFO;
            }
        }

        //??????????????????
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
        BeanUtils.copyProperties(vo,recommendTaskEntity);
        //?????????????????????
        recommendTaskEntity.setSrcIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getSrcIpSystem()));
        recommendTaskEntity.setDstIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getDstIpSystem()));
        recommendTaskEntity.setOrderNumber(orderNumber);
        Integer ipType = ObjectUtils.isNotEmpty(vo.getIpType())?vo.getIpType(): IPV4.getCode();
        Integer urlType = ObjectUtils.isNotEmpty(vo.getUrlType())?vo.getUrlType(): IPV4.getCode();
        recommendTaskEntity.setIpType(ipType);
        recommendTaskEntity.setUrlType(urlType);
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        if(vo.getServiceList() == null || vo.getServiceList().size() == 0) {
            logger.info("????????????????????????");
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTOList.add(serviceDTO);
            recommendTaskEntity.setServiceList(JSONObject.toJSONString(serviceDTOList));
        } else {
            logger.info("???????????????????????????" + JSONObject.toJSONString(vo.getServiceList()));
            serviceDTOList = vo.getServiceList();
            for(ServiceDTO service : serviceDTOList) {
                if(!AliStringUtils.isEmpty(service.getDstPorts())) {
                    service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                }
            }
            recommendTaskEntity.setServiceList(JSONObject.toJSONString(vo.getServiceList()));
        }
        recommendTaskEntity.setCreateTime(date);
        if(vo.getStartTime() != null) {
            recommendTaskEntity.setStartTime(new Date(vo.getStartTime()));
        }
        if(vo.getEndTime() != null) {
            recommendTaskEntity.setEndTime(new Date(vo.getEndTime()));
        }
        recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        if(userInfoDTO!=null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())){
            recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        }else{
            recommendTaskEntity.setBranchLevel("00");
        }
        PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
        additionalInfoEntity.setScenesUuid(scenesUuid);
        additionalInfoEntity.setScenesDTOList(scenesDTOList);
        additionalInfoEntity.setDeviceUuid(deviceUuid);
        //?????????????????????????????????
        if (StringUtils.isNotBlank(deviceUuid)) {
            if(vo.getSrcZone() != null) {
                additionalInfoEntity.setSrcZone(vo.getSrcZone().equals("-1") ? null : vo.getSrcZone());
            }

            if(vo.getDstZone() != null) {
                additionalInfoEntity.setDstZone(vo.getDstZone().equals("-1") ? null : vo.getDstZone());
            }
            additionalInfoEntity.setInDevItf(vo.getInDevIf());
            additionalInfoEntity.setOutDevItf(vo.getOutDevIf());
            additionalInfoEntity.setInDevItfAlias(vo.getInDevItfAlias());
            additionalInfoEntity.setOutDevItfAlias(vo.getOutDevItfAlias());
        }
        additionalInfoEntity.setAction(vo.getAction());

        recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        Integer idleTimeout = vo.getIdleTimeout();
        if (ObjectUtils.isNotEmpty(idleTimeout)) {
            recommendTaskEntity.setIdleTimeout(vo.getIdleTimeout() * HOUR_SECOND);
        } else {
            recommendTaskEntity.setIdleTimeout(null);
        }

        List<RecommendTaskEntity> list = new ArrayList<>();
        list.add(recommendTaskEntity);
        taskService.insertRecommendTaskList(list);
        logger.info("????????????????????????" + JSONObject.toJSONString(recommendTaskEntity));
        String message = String.format("??????????????????%s??????", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        ActionEnum action = ActionEnum.PERMIT;
        if(!vo.getAction().equalsIgnoreCase(PolicyConstants.POLICY_STR_PERMISSION_PERMIT)) {
            action = ActionEnum.DENY;
        }
        MoveSeatEnum moveSeat = MoveSeatEnum.FIRST;

        //?????????????????????
        List<Integer> pushTaskIdList = new ArrayList<>();
        //??????
        if (scenesDTOList != null && scenesDTOList.size() > 0) {
            for (DisposalScenesDTO scenesDTO : scenesDTOList) {

                boolean isVsys = false;
                String rootDeviceUuid = "";
                String vsysName = "";

                NodeEntity node = null;
                DeviceDataRO deviceData = null;
                if (StringUtils.isNotBlank(scenesDTO.getDeviceUuid())) {
                    node = taskService.getTheNodeByUuid(scenesDTO.getDeviceUuid());
                    if(node == null) {
                        logger.error(String.format("?????????%s ??????UUID???%s ???????????????????????????", scenesDTO.getName(), scenesDTO.getDeviceUuid()));
                        return ReturnCode.EMPTY_DEVICE_INFO;
                    } else {
                        DeviceRO device = whaleManager.getDeviceByUuid(scenesDTO.getDeviceUuid());
                        deviceData = device.getData().get(0);
                        if(deviceData.getIsVsys() != null) {
                            isVsys = deviceData.getIsVsys();
                            rootDeviceUuid = deviceData.getRootDeviceUuid();
                            vsysName = deviceData.getVsysName();
                        }
                    }
                }
                String srcZone = null, dstZone = null;
                if(StringUtils.isNotEmpty(scenesDTO.getSrcZoneUuid())){
                    srcZone = scenesDTO.getSrcZoneUuid().equals("-1") ? null : scenesDTO.getSrcZoneName();
                }
                if(StringUtils.isNotEmpty(scenesDTO.getDstZoneUuid())){
                    dstZone = scenesDTO.getDstZoneUuid().equals("-1") ? null : scenesDTO.getDstZoneName();
                }
                int pushTaskId = this.addTaskToNewTable(recommendTaskEntity, node, deviceData, vo, userInfoDTO, userName,
                        theme, scenesDTO.getDeviceUuid(), action, isVsys, vsysName, srcZone, dstZone,
                        scenesDTO.getSrcItf(), scenesDTO.getDstItf(), scenesDTO.getSrcItfAlias(), scenesDTO.getDstItfAlias(), moveSeat);
                pushTaskIdList.add(pushTaskId);
            }
        }
        //?????????
        if (StringUtils.isNotBlank(deviceUuid)) {
            boolean isVsys = false;
            String rootDeviceUuid = "";
            String vsysName = "";

            NodeEntity node = null;
            DeviceDataRO deviceData = null;
            if (StringUtils.isNotBlank(deviceUuid)) {
                node = taskService.getTheNodeByUuid(deviceUuid);
                if(node == null) {
                    logger.error(String.format("??????UUID???%s ???????????????????????????", deviceUuid));
                    return ReturnCode.EMPTY_DEVICE_INFO;
                } else {
                    DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
                    deviceData = device.getData().get(0);
                    if(deviceData.getIsVsys() != null) {
                        isVsys = deviceData.getIsVsys();
                        rootDeviceUuid = deviceData.getRootDeviceUuid();
                        vsysName = deviceData.getVsysName();
                    }
                }
            }

            int pushTaskId = this.addTaskToNewTable(recommendTaskEntity, node, deviceData, vo, userInfoDTO, userName,
                    theme, deviceUuid, action, isVsys, vsysName, additionalInfoEntity.getSrcZone(), additionalInfoEntity.getDstZone(),
                    additionalInfoEntity.getInDevItf(), additionalInfoEntity.getOutDevItf(),
                    additionalInfoEntity.getInDevItfAlias(), additionalInfoEntity.getOutDevItfAlias(), moveSeat);
            pushTaskIdList.add(pushTaskId);
        }

        vo.setPushTaskId(pushTaskIdList);
        vo.setTaskId(recommendTaskEntity.getId());
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int newCustomizeCmd(NewPolicyPushVO vo) {
        if(vo == null) {
            return ReturnCode.EMPTY_PARAMETERS;
        }
        logger.info("???????????????" + JSONObject.toJSONString(vo));

        String userName = vo.getUserName();
        String theme = vo.getTheme();

        if (StringUtils.isBlank(vo.getCommandLine())) {
            logger.error(String.format("?????????????????????%s ?????????", theme));
            return ReturnCode.EMPTY_PARAMETERS;
        }

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber= "A" + simpleDateFormat.format(date);

        //??????uuid
        String deviceUuid = vo.getDeviceUuid();
        //??????uuid
        String scenesUuid = vo.getScenesUuid();

        //???????????????????????????????????????????????????????????????
        if (StringUtils.isAllBlank(deviceUuid, scenesUuid) || StringUtils.isNoneBlank(deviceUuid, scenesUuid)) {
            return ReturnCode.EMPTY_DEVICE_INFO;
        }

        PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
        //????????????????????????
        List<DisposalScenesDTO> scenesDTOList = null;
        if (StringUtils.isNotBlank(scenesUuid)) {
            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(scenesUuid);
            if (null == scenesEntity) {
                logger.error(String.format("??????UUID???%s ????????????????????????", scenesUuid));
                return ReturnCode.EMPTY_DEVICE_INFO;
            } else {
                scenesDTOList = disposalScenesService.findByScenesUuid(scenesUuid);
                if (CollectionUtils.isEmpty(scenesDTOList)) {
                    logger.error(String.format("??????UUID???%s ???????????????????????????????????????", scenesUuid));
                    return ReturnCode.EMPTY_DEVICE_INFO;
                }
                additionalInfoEntity.setScenesUuid(scenesUuid);
                additionalInfoEntity.setScenesDTOList(scenesDTOList);
            }
        }

        if (StringUtils.isNotBlank(deviceUuid)) {
            NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
            if(node == null) {
                logger.error(String.format("??????UUID???%s ???????????????????????????", deviceUuid));
                return ReturnCode.EMPTY_DEVICE_INFO;
            }
            additionalInfoEntity.setDeviceUuid(deviceUuid);
        }

        //??????????????????
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
        BeanUtils.copyProperties(vo,recommendTaskEntity);
        recommendTaskEntity.setOrderNumber(orderNumber);
        recommendTaskEntity.setCreateTime(date);
        recommendTaskEntity.setTaskType(PolicyConstants.CUSTOMIZE_CMD_PUSH);
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        if(userInfoDTO!=null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())){
            recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        }else{
            recommendTaskEntity.setBranchLevel("00");
        }
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        //????????????????????????????????????????????????
        recommendTaskEntity.setSrcIp(PolicyConstants.IPV4_ANY);
        recommendTaskEntity.setDstIp(PolicyConstants.IPV4_ANY);
        recommendTaskEntity.setIpType(IpTypeEnum.IPV4.getCode());
        List<RecommendTaskEntity> list = new ArrayList<>();
        list.add(recommendTaskEntity);
        taskService.insertRecommendTaskList(list);
        logger.info("????????????????????????" + JSONObject.toJSONString(recommendTaskEntity));
        String message = String.format("????????????????????????%s??????", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        //?????????????????????
        List<Integer> pushTaskIdList = new ArrayList<>();
        //??????
        if (scenesDTOList != null && scenesDTOList.size() > 0) {
            for (DisposalScenesDTO scenesDTO : scenesDTOList) {
                int pushTaskId = this.addTaskToNewCustomizeCmd(recommendTaskEntity, userName,theme, scenesDTO.getDeviceUuid(), vo.getCommandLine());
                pushTaskIdList.add(pushTaskId);
            }
        }
        //?????????
        if (StringUtils.isNotBlank(deviceUuid)) {
            int pushTaskId = this.addTaskToNewCustomizeCmd(recommendTaskEntity, userName,theme, deviceUuid, vo.getCommandLine());
            pushTaskIdList.add(pushTaskId);
        }

        vo.setPushTaskId(pushTaskIdList);
        vo.setTaskId(recommendTaskEntity.getId());
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int generateDeleteCommandLine(NodeEntity nodeEntity, Integer policyId, Integer ipType, String policyName, String srcZone, String dstZone, String userName) throws Exception {
        CmdDTO cmdDTO = EntityUtils.createDeleteCmdDTO(nodeEntity, policyId, ipType, policyName, srcZone, dstZone);
        DeviceRO device = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
        DeviceDataRO deviceData = device.getData().get(0);
        Boolean isVsys = false;
        String vsysName = "";
        if(deviceData.getIsVsys() != null) {
            isVsys = deviceData.getIsVsys();
            vsysName = deviceData.getVsysName();
        }
        cmdDTO.getDevice().setVsys(isVsys);
        cmdDTO.getDevice().setVsysName(vsysName);
        List<Integer> steps= cmdDTO.getProcedure().getSteps();
        if ( CollectionUtils.isEmpty(steps) ){
            steps = new ArrayList();
            steps.add(103);
        }
        for (int step : steps) {
            SubServiceEnum subService = SubServiceEnum.valueOf(step);
            String serviceName = NameUtils.getServiceDefaultName(subService.getServiceClass());
            CmdService service = cmdServiceMap.get(serviceName);
            service.modify(cmdDTO);
        }
        PolicyEnum policyType = cmdDTO.getPolicy().getType();
        DeviceDTO deviceDTO = cmdDTO.getDevice();

        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(nodeEntity.getModelNumber());
        if (DeviceModelNumberEnum.isRangeHillStoneCode(deviceModelNumberEnum.getCode())) {
            String paramValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE);
            if (StringUtils.isNotEmpty(paramValue)) {
                if (paramValue.equals("0")) {
                    cmdDTO.getSetting().setRollbackType(true);
                } else {
                    cmdDTO.getSetting().setRollbackType(false);
                }
            }
        }
        cmdDTO.getDevice().setModelNumber(deviceModelNumberEnum);
        vendorManager.getGenerator(policyType, deviceDTO, cmdDTO.getProcedure());
        // ??????????????????????????????????????? ?????????????????????????????????
        String rollbackCommandline = commandlineManager.generateRollback(cmdDTO);

        if (StringUtils.isNotEmpty(rollbackCommandline)){
            rollbackCommandline = specialCommandLine(deviceModelNumberEnum,rollbackCommandline,cmdDTO);
            PushTaskEntity pushTaskEntity = new PushTaskEntity();
            UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
            pushTaskEntity.setUserName(userName);
            pushTaskEntity.setOrderType(PolicyConstants.DOMAIN_COMPLIANCE);
            pushTaskEntity.setDeviceUuid(nodeEntity.getUuid());
            pushTaskEntity.setDeviceName(nodeEntity.getDeviceName());
            pushTaskEntity.setManageIp(nodeEntity.getIp());
            pushTaskEntity.setCommand(rollbackCommandline);
            List<CommandTaskEditableEntity> commandTaskEntityList = processTask(pushTaskEntity, userInfoDTO);
            for(CommandTaskEditableEntity entity: commandTaskEntityList) {
                if(userInfoDTO != null ){
                    entity.setBranchLevel(userInfoDTO.getBranchLevel());
                }else{
                    entity.setBranchLevel("00");
                }
                recommendTaskService.addCommandTaskEditableEntity(entity);
            }
        }
        return 0;
    }


    /**
     * ??????????????????:???????????????????????????????????????????????????????????????
     */
    List<CommandTaskEditableEntity> processTask(PushTaskEntity entity,UserInfoDTO userInfoDTO) {

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PushConstants.PUSH_ORDER_NO_TIME_FORMAT);
        String orderNo = "P" + simpleDateFormat.format(date) + PushConstants.COUNT;
        PushConstants.COUNT++;
        List<CommandTaskEditableEntity> commandTaskEntityList = new ArrayList<>();
        String branchLevel;
        if(userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())){
            branchLevel = userInfoDTO.getBranchLevel();
        }else{
            branchLevel  = "00";
        }
        String theme = orderNo;
        Date createDate = new Date();
        //?????????????????????task???????????????taskId??????????????????????????????????????????????????????????????????????????????????????????task????????????
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
        recommendTaskEntity.setTheme(theme);
        recommendTaskEntity.setOrderNumber(theme);
        recommendTaskEntity.setUserName(entity.getUserName());
        recommendTaskEntity.setSrcIp("255.255.255.255");
        recommendTaskEntity.setDstIp("255.255.255.255");
        recommendTaskEntity.setCreateTime(createDate);
        recommendTaskEntity.setBranchLevel(branchLevel);
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        recommendTaskEntity.setTaskType(entity.getOrderType());
        recommendTaskEntity.setIpType(0);
        List<RecommendTaskEntity> recommendTaskEntityListlist = new ArrayList<>();
        recommendTaskEntityListlist.add(recommendTaskEntity);
        recommendTaskService.insertRecommendTaskList(recommendTaskEntityListlist);

        CommandTaskEditableEntity commandTaskEntity = new CommandTaskEditableEntity();
        commandTaskEntity.setDeviceUuid(entity.getDeviceUuid());

        commandTaskEntity.setTaskType(entity.getOrderType());
        commandTaskEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);
        commandTaskEntity.setCommandline(entity.getCommand());
        commandTaskEntity.setTheme(theme);
        commandTaskEntity.setCreateTime(createDate);
        commandTaskEntity.setTaskId(recommendTaskEntity.getId());
        commandTaskEntity.setUserName(entity.getUserName());

        commandTaskEntityList.add(commandTaskEntity);
        return commandTaskEntityList;
    }

    /**
     * ?????????????????????
     * @param recommendTaskEntity
     * @param node
     * @param deviceData
     * @param vo
     * @param userInfoDTO
     * @param userName
     * @param theme
     * @param deviceUuid
     * @param action
     * @param isVsys
     * @param vsysName
     * @param srcZone
     * @param dstZone
     * @param inItf
     * @param outItf
     * @param inItfAlias
     * @param outItfAlias
     * @param moveSeat
     */
    public int addTaskToNewTable(RecommendTaskEntity recommendTaskEntity, NodeEntity node, DeviceDataRO deviceData, NewPolicyPushVO vo, UserInfoDTO userInfoDTO,
                                 String userName, String theme, String deviceUuid, ActionEnum action, boolean isVsys, String vsysName,
                                 String srcZone, String dstZone, String inItf, String outItf, String inItfAlias, String outItfAlias,
                                 MoveSeatEnum moveSeat) {
        //?????????????????????
        CommandTaskEditableEntity entity = EntityUtils.createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,
                recommendTaskEntity.getId(), userName, theme, deviceUuid);
        entity.setBranchLevel(recommendTaskEntity.getBranchLevel());

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        String startTimeString = recommendTaskEntity.getStartTime()==null?null:sdf.format(recommendTaskEntity.getStartTime());
        String endTimeString = recommendTaskEntity.getEndTime()==null?null:sdf.format(recommendTaskEntity.getEndTime());
        commandTaskManager.addCommandEditableEntityTask(entity);

        PolicyEnum type = null;
        String modelNumber = node.getModelNumber();
        if(!AliStringUtils.isEmpty(modelNumber) && DeviceTypeEnum.ROUTER.name().equalsIgnoreCase(deviceData.getDeviceType()) &&
                (modelNumber.equals("Cisco IOS") || modelNumber.equals("Cisco NX-OS") || modelNumber.equals("Ruijie RGOS")
                || modelNumber.equals(DeviceModelNumberEnum.SRX.getKey()) || modelNumber.equals(DeviceModelNumberEnum.SRX_NoCli.getKey()))
                || modelNumber.equalsIgnoreCase(DeviceModelNumberEnum.JUNIPER_ROUTER.getKey())) {
            // ????????????uuid
            type = PolicyEnum.ACL;
        }else{
            // ?????????????????????????????????????????????,????????????????????????(??????????????????????????????,??????????????????ACL)
            type = PolicyEnum.SECURITY;
        }
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(type, entity.getId(), entity.getTaskId(), deviceUuid, theme, userName,
                recommendTaskEntity.getSrcIp(), recommendTaskEntity.getDstIp(), null, null, vo.getServiceList(),
                null, srcZone, dstZone, inItf, outItf, inItfAlias, outItfAlias,
                startTimeString, endTimeString, recommendTaskEntity.getDescription(), action, isVsys, vsysName, moveSeat, null, null, recommendTaskEntity.getIdleTimeout(),
                recommendTaskEntity.getSrcIpSystem(), recommendTaskEntity.getDstIpSystem(), recommendTaskEntity.getIpType(),null,null,null,null,null,null,null);

        TaskDTO task = cmdDTO.getTask();
        task.setMergeCheck(recommendTaskEntity.getMergeCheck());
        task.setRangeFilter(recommendTaskEntity.getRangeFilter());
        task.setBeforeConflict(recommendTaskEntity.getBeforeConflict());
        task.setTaskTypeEnum(TaskTypeEnum.SECURITY_TYPE);
        cmdDTO.getDevice().setNodeEntity(node);
        cmdDTO.getPolicy().setPolicyUserNames(vo.getPolicyUserNames());
        cmdDTO.getPolicy().setUrlType(recommendTaskEntity.getUrlType());
        cmdDTO.getPolicy().setPolicyApplications(vo.getPolicyApplications());
        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        if (DeviceModelNumberEnum.isRangeHillStoneCode(deviceModelNumberEnum.getCode())) {
            String paramValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE);
            if (StringUtils.isNotEmpty(paramValue)) {
                if (paramValue.equals("0")) {
                    cmdDTO.getSetting().setRollbackType(true);
                } else {
                    cmdDTO.getSetting().setRollbackType(false);
                }
            }
        }
        cmdTaskService.getRuleMatchFlow2Generate(cmdDTO,userInfoDTO);

        return entity.getId();
    }

    /**
     * ?????????????????????
     * @param recommendTaskEntity
     * @param userName
     * @param theme
     * @param deviceUuid
     * @param commandLine
     */
    private int addTaskToNewCustomizeCmd(RecommendTaskEntity recommendTaskEntity, String userName, String theme, String deviceUuid, String commandLine) {
        //?????????????????????
        CommandTaskEditableEntity entity = EntityUtils.createCommandTask(PolicyConstants.CUSTOMIZE_CMD_PUSH,
                recommendTaskEntity.getId(), userName, theme, deviceUuid);
        entity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        entity.setCommandline(commandLine);
        entity.setStatus(PushStatusEnum.PUSH_NOT_START.getCode());
        commandTaskManager.addCommandEditableEntityTask(entity);

        return entity.getId();
    }


    @Override
    public void startCommandTaskEditableEmail(List<CommandTaskEditableEntity> taskList) {
        String toAddress = taskList.get(0).getReceiverEmail();
        Date pushSchedule = taskList.get(0).getPushSchedule();
        Date pushTime = taskList.get(0).getPushTime();//????????????????????????
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //????????????????????????????????? + ??????????????? + ????????????
        String subject = "????????????" + "-??????????????????" + sdf.format(pushSchedule) + "-???????????????" + String.valueOf(taskList.size());
        /*??????????????????:
        ?????????????????? ~ ??????????????????
        ?????????N??? ????????????N??? */
        //?????????????????????list
        List<CommandTaskEditableEntity> failList = taskList.stream().filter(task -> 3 == task.getPushStatus()).collect(Collectors.toList());

        //?????????????????????list
        List<CommandTaskEditableEntity> successList = taskList.stream().filter(task -> 2 == task.getPushStatus()).collect(Collectors.toList());
        String content = "?????????????????????" + sdf.format(pushSchedule) + "~?????????????????????" + sdf.format(pushTime) + "<br/>" + "?????????<font color=\"green\">" + String.valueOf(null == successList ? 0 : successList.size()) + "???</font> ????????????<font color=\"red\">" + String.valueOf(null == failList ? 0 : failList.size()) + "???</font>";
        try {
            //????????????
            if (StringUtils.isNotBlank(toAddress)) {
                logger.info("?????????????????????subject:{},toAddress:{}", subject, toAddress);
                MailServerConfDTO emialDTO = systemParamMapper.findEmailParam(PushConstants.EMAIL_PARAM_GROUP_NAME);
                if (emialDTO != null) {
                    CustomMailTool.sendEmail(emialDTO, toAddress, subject, content, null);
                } else {
                    logger.error("????????????????????????????????????");
                }
            }
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
        }
    }

    //???????????????????????????
    private List<CommandTaskEditableEntity> getSendEmailList(Date pushSchedule) {
        Map<String, Object> cond = new HashMap<String, Object>();
        cond.put("pushSchedule",pushSchedule);//??????????????????????????????????????????
        List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskEditableMapper.selectAllPushList(cond);
        List<CommandTaskEditableEntity> successList = taskEditableEntityList.stream().filter(task -> 2 == task.getPushStatus()).collect(Collectors.toList());
        List<CommandTaskEditableEntity> failList = taskEditableEntityList.stream().filter(task -> 3 == task.getPushStatus()).collect(Collectors.toList());
        if (taskEditableEntityList.size() == successList.size() + failList.size()) {//????????????????????????????????????????????????????????????????????????????????????
            return taskEditableEntityList;
        }
        return null;
    }

    @Transactional(propagation= Propagation.SUPPORTS)
    public void checkScheduleAndPush() throws Exception {
        while(true) {
            List<CommandTaskEditableEntity> list = commandTaskManager.getScheduledCommand();
            if(list == null || list.size() == 0) {
                try{
                    logger.info("??????...");
                    sleep(WAIT_TIME);
                } catch (Exception e) {
                    logger.error("???????????????", e);
                }
                continue;
            }

            Map<Integer, CommandTaskDTO> idTaskMap = new LinkedHashMap<>();
            for(CommandTaskEditableEntity entity : list) {
                Integer taskId = entity.getTaskId();
                if(idTaskMap.containsKey(taskId)) {
                    CommandTaskDTO dto = idTaskMap.get(taskId);
                    List<CommandTaskEditableEntity> taskList = dto.getList();
                    taskList.add(entity);
                } else {
                    CommandTaskDTO dto = new CommandTaskDTO();
                    dto.setRevert(false);
                    List<CommandTaskEditableEntity> taskList = new ArrayList<>();
                    taskList.add(entity);
                    dto.setList(taskList);
                    dto.setTaskId(taskId);
                    dto.setTheme(entity.getTheme());
                    idTaskMap.put(taskId, dto);
                }
            }

            List<CommandTaskDTO> commandTaskList = new ArrayList<>(idTaskMap.values());
//            pushTaskServiceImpl.addCommandTaskListForSchedule(commandTaskList, true);
            pushTaskServiceImpl.addCommandTaskListForScheduleV2(commandTaskList, true);
            try{
                logger.info("??????????????????...");
                sleep(WAIT_TIME);
            } catch (Exception e) {
                logger.error("???????????????", e);
            }
        }
    }

    /**
     * ????????????-?????????????????????????????????????????????
     * @param taskList
     * @param doPush
     */
    @Override
    @PushTimeLockCheck
    public void addCommandTaskListForSchedule(List<CommandTaskDTO> taskList, boolean doPush) {
        if(!doPush){
            logger.info("??????????????????????????????");
        }else {
            scheduleLatch = new CountDownLatch(taskList.size());
            for (CommandTaskDTO commandTaskDTO : taskList) {
                //push??????????????????????????????PT???push task????????????????????????????????????????????????
                //@modify by zy 20200513 ????????????????????????????????????????????????????????????
                boolean revert =  commandTaskDTO.isRevert();
                if(revert) {
                    recommendTaskService.updateCommandTaskRevertStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_QUEUED);
                }else{
                    recommendTaskService.updateCommandTaskPushStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_QUEUED);
                }

                String id = "PT_" + commandTaskDTO.getTaskId();
                scheduleMap.put(id,id);
                if (ExtendedExecutor.containsKey(id)){
                    logger.info("??????????????????["+id+"]???????????????");
                    scheduleLatch.countDown();
                    scheduleMap.remove(id);
                    continue;

                }
                logger.info("?????????????????????" + id);

                pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
                    @Override
                    protected void start() throws InterruptedException, Exception {

                        try {
                            //??????????????????
                            long start = System.currentTimeMillis();
                            logger.info("????????????...??????id???{}???,???????????????{}???",id,start);

                            pushService.pushCommand(commandTaskDTO);
                            long end = System.currentTimeMillis();
                            long consume = end - start;
                            logger.info("????????????...??????id???{}???,???????????????{}???,?????????{}?????????",id,end,consume);
                            scheduleLatch.countDown();
                            scheduleMap.remove(id);
                        } catch ( Exception e) {
                            logger.info(e.getClass().toString());
                            taskService.updateTaskStatus(commandTaskDTO.getTaskId(), PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                            taskService.updateCommandTaskStatus(commandTaskDTO.getTaskId(), PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                            throw e;
                        }
                    }
                });

            }

            try {
                scheduleLatch.await();
            } catch (Exception e) {
                logger.error(String.format("????????????????????????"),e);
            }

            //?????????????????????????????????????????????
            List<Integer> taskIds = taskList.stream().map(task -> task.getTaskId()).collect(Collectors.toList());
            Map<String, Object> cond = new HashMap<String, Object>();
            cond.put("enableEmail","true");//??????????????????????????????????????????
            List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskEditableMapper.selectAllPushList(cond);
            List<CommandTaskEditableEntity> taskEditableEntityListNew = new ArrayList<>();
            for (Integer id : taskIds) {
                for (CommandTaskEditableEntity task : taskEditableEntityList) {
                    if (id.equals(task.getTaskId())) {
                        taskEditableEntityListNew.add(task);
                    }
                }
            }
            if (null != taskEditableEntityListNew) {
                taskEditableEntityListNew = taskEditableEntityListNew.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(task -> task.getPushSchedule()))), ArrayList::new));
                List<Date> pushScheduleList = taskEditableEntityListNew.stream().map(task -> task.getPushSchedule()).collect(Collectors.toList());
                if (null != pushScheduleList) {
                    for (Date pushSchedule : pushScheduleList) {
                        List<CommandTaskEditableEntity> sendEmailList = getSendEmailList(pushSchedule);
                        if (null != sendEmailList) {
                            //????????????
                            startCommandTaskEditableEmail(sendEmailList);
                        }
                    }
                }
            }


        }
    }

    /**
     * ????????????-?????????????????????????????????????????????
     * @param taskList
     * @param doPush
     */
    @PushTimeLockCheck
    @Override
    public void addCommandTaskListForScheduleV2(List<CommandTaskDTO> taskList, boolean doPush) throws Exception {
        if(!doPush) {
            logger.info("??????????????????????????????");
            return;
        }
        if (0 != pushBlockingQueue.getQueueSize()) {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH_PUSH.getId(),
                    String.format("????????????????????????????????????????????????????????????????????????????????? ?????????????????????"));
            return;
        }
        long start = System.currentTimeMillis();
        logger.info("???????????????????????????...,???????????????{}???", start);

        // ??????????????????????????????????????????
        List<BatchCommandTaskDTO> batchTaskLists = new ArrayList<>();

        boolean isRever = taskList.get(0).isRevert();

        Map<String,List<CommandTaskEditableEntity>> listMap = new HashMap<>();
        for (CommandTaskDTO taskDTO : taskList) {
            List<CommandTaskEditableEntity> commandList = taskDTO.getList();
            if(CollectionUtils.isEmpty(commandList)){
                continue;
            }
            for (CommandTaskEditableEntity commandEntity : commandList) {
                if (listMap.containsKey(commandEntity.getDeviceUuid())) {
                    List<CommandTaskEditableEntity> existCommands = listMap.get(commandEntity.getDeviceUuid());
                    existCommands.add(commandEntity);
                } else {
                    List<CommandTaskEditableEntity> newCommandTask = new ArrayList<>();
                    newCommandTask.add(commandEntity);
                    listMap.put(commandEntity.getDeviceUuid(),newCommandTask);
                }
            }
        }

        for (String deviceUuid :listMap.keySet()){
            BatchCommandTaskDTO batchCommandTaskDTO = new BatchCommandTaskDTO();
            batchCommandTaskDTO.setDeviceUuid(deviceUuid);
            batchCommandTaskDTO.setList(listMap.get(deviceUuid));
            List<Integer> taskIds = listMap.get(deviceUuid).stream().map(CommandTaskEditableEntity::getTaskId).distinct()
                    .collect(Collectors.toList());
            batchCommandTaskDTO.setTaskIds(taskIds);
            batchCommandTaskDTO.setRevert(isRever);
            batchTaskLists.add(batchCommandTaskDTO);
        }
        logger.info("??????????????????--????????????????????????????????????????????????:{}???", batchTaskLists.size());
        CountDownLatch latch = new CountDownLatch(batchTaskLists.size());
        for (BatchCommandTaskDTO batchCommandTaskDTO : batchTaskLists) {
            String deviceUuid = batchCommandTaskDTO.getDeviceUuid();
            NodeEntity nodeEntity = recommendTaskService.getTheNodeByUuid(deviceUuid);
            if (null == nodeEntity) {
                logger.info("????????????uuid:{}????????????????????????", deviceUuid);
                continue;
            }
            batchCommandTaskDTO.setModelNumber(nodeEntity.getModelNumber());

            String id = "PT_" + batchCommandTaskDTO.getDeviceUuid();
            scheduleMap.put(id,id);
            if (ExtendedExecutor.containsKey(id)) {
                //??????????????????
                logger.warn(String.format("??????[%s]????????????????????????????????????????????????", nodeEntity.getModelNumber()));
                //?????????????????????latch??????????????????????????????????????????????????????????????????
                scheduleMap.remove(id);
                latch.countDown();
                continue;
            }
            boolean revert = batchCommandTaskDTO.isRevert();
            // ?????????????????????????????????????????????????????????
            recommendTaskService.updateCommandTaskPushOrRevertStatus(batchCommandTaskDTO.getList(), PushConstants.PUSH_INT_PUSH_QUEUED, revert);
            // ????????????
            batchPushCommand(latch, batchCommandTaskDTO, id);
        }
        try {
            latch.await();
        } catch (Exception e) {
            logger.error("???????????????????????????,????????????:{}", e.getMessage());
            throw e;
        }

        //????????????????????????
        for (CommandTaskDTO commandTaskDTO : taskList) {
            List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskManager.getCommandTaskByTaskId(commandTaskDTO.getTaskId());
            int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(taskEditableEntityList);
            int policyStatusByPushStatus = recommendTaskService.getPolicyStatusByPushStatus(pushStatusInTaskList);
            recommendTaskService.updateTaskStatus(commandTaskDTO.getTaskId(), policyStatusByPushStatus);
        }

        //?????????????????????????????????????????????
        List<Integer> taskIds = taskList.stream().map(task -> task.getTaskId()).collect(Collectors.toList());
        Map<String, Object> cond = new HashMap<String, Object>();
        //??????????????????????????????????????????
        cond.put("enableEmail","true");
        List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskEditableMapper.selectAllPushList(cond);
        List<CommandTaskEditableEntity> taskEditableEntityListNew = new ArrayList<>();
        for (Integer id : taskIds) {
            for (CommandTaskEditableEntity task : taskEditableEntityList) {
                if (id.equals(task.getTaskId())) {
                    taskEditableEntityListNew.add(task);
                }
            }
        }
        if (null != taskEditableEntityListNew) {
            taskEditableEntityListNew = taskEditableEntityListNew.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(task -> task.getPushSchedule()))), ArrayList::new));
            List<Date> pushScheduleList = taskEditableEntityListNew.stream().map(task -> task.getPushSchedule()).collect(Collectors.toList());
            if (null != pushScheduleList) {
                for (Date pushSchedule : pushScheduleList) {
                    List<CommandTaskEditableEntity> sendEmailList = getSendEmailList(pushSchedule);
                    if (null != sendEmailList) {
                        //????????????
                        startCommandTaskEditableEmail(sendEmailList);
                    }
                }
            }
        }

        long end = System.currentTimeMillis();
        long consume = end - start;
        logger.info("???????????????????????????...,???????????????{}???,?????????{}?????????", end, consume);
    }

    /**
     * ??????push????????????????????????--?????????????????????
     */
    @PostConstruct
    public void initPushTaskStatus () {
        logger.info("**********??????push??????????????????????????????????????????**********");
        List<CommandTaskEditableEntity> list = commandTaskManager.getExecuteTask();
        if (ObjectUtils.isEmpty(list)) {
            logger.info("**********???????????????????????????????????????**********");
            return;
        }
        /*list = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(task -> task.getTaskId()))),ArrayList::new));
        List<Integer> taskIdList = list.stream().map(task -> task.getTaskId()).collect(Collectors.toList());
        taskIdList.forEach(taskId -> {
            recommendTaskService.updateCommandTaskPushStatus(taskId,PolicyConstants.PUSH_STATUS_FAILED);
        });*/
        for (CommandTaskEditableEntity task : list) {
            if (PolicyConstants.PUSH_STATUS_PUSHING == task.getPushStatus()) {
                //??????????????????--->????????????
                recommendTaskService.updateCommandTaskStatusById(task.getId(), PolicyConstants.PUSH_STATUS_FAILED);
                continue;
            }
            if (PushConstants.PUSH_INT_PUSH_QUEUED == task.getPushStatus()) {
                //????????????????????????--->???????????????
                recommendTaskService.updateCommandTaskStatusById(task.getId(), PolicyConstants.PUSH_STATUS_NOT_START);
                continue;
            }

            if (PolicyConstants.REVERT_STATUS_REVERTING == task.getRevertStatus()) {
                //??????????????????--->????????????
                recommendTaskService.updateCommandTaskRevertStatusById(task.getId(), PolicyConstants.REVERT_STATUS_FAILED);
            }

            if (PushConstants.PUSH_INT_PUSH_QUEUED == task.getRevertStatus()) {
                //????????????????????????--->???????????????
                recommendTaskService.updateCommandTaskRevertStatusById(task.getId(), PolicyConstants.REVERT_STATUS_NOT_START);
            }
        }
        logger.info("**********??????push??????????????????????????????????????????**********");
    }

    private String specialCommandLine(DeviceModelNumberEnum modelNumber , String rollbackCommandline ,CmdDTO cmdDTO){
        switch (modelNumber) {
            case HILLSTONE:
            case HILLSTONE_R5:
            case HILLSTONE_V5:
                return rollbackCommandline.replace(PLACE_HOLDER,cmdDTO.getSetting().getPolicyId());
            case FORTINET:
            case FORTINET_V5:
            case FORTINET_V5_2:
                return rollbackCommandline.replace(FORTINET_PLACE_HOLDER,cmdDTO.getSetting().getPolicyId());
        }
        return rollbackCommandline;
    }
}
