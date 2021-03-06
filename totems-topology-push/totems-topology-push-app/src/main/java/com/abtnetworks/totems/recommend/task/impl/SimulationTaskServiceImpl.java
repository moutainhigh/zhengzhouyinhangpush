package com.abtnetworks.totems.recommend.task.impl;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedLatchRunnable;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.recommend.annotation.TimeCounter;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.AnalyzeService;
import com.abtnetworks.totems.recommend.service.CheckService;
import com.abtnetworks.totems.recommend.service.CommandService;
import com.abtnetworks.totems.recommend.service.CommandSimulationCommonService;
import com.abtnetworks.totems.recommend.service.ExceptionService;
import com.abtnetworks.totems.recommend.service.MergeService;
import com.abtnetworks.totems.recommend.service.PathService;
import com.abtnetworks.totems.recommend.service.RecommendService;
import com.abtnetworks.totems.recommend.service.RiskService;
import com.abtnetworks.totems.recommend.task.SimulationTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/7 15:05
 */
@Service
public class SimulationTaskServiceImpl implements SimulationTaskService {

    private static Logger logger = LoggerFactory.getLogger(SimulationTaskServiceImpl.class);

    private static final String STOP_TYPE_INTERUPT = "interrupt";

    private static final String STOP_TYPE_STOP = "stop";

    @Autowired
    private AnalyzeService analyzeService;

    @Autowired
    private RecommendService recommendServiceImpl;

    @Autowired
    private CheckService checkService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private CommandService commandService;

    @Autowired
    ExceptionService exceptionService;

    @Autowired
    private PathService pathService;

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Autowired
    private MergeService mergeService;

    @Autowired
    private PushService pushService;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Resource
    CommandSimulationCommonService commandSimulationCommonService;

    @Value("${push.whale:false}")
    private Boolean isNginZ;

    @Resource
    RemoteBranchService remoteBranchService;

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    @Qualifier(value = "analyzeExecutor")
    private Executor analyzeExecutor;

    @Autowired
    @Qualifier(value = "pushExecutor")
    private Executor pushExecutor;

    @Override
    public int addSimulationTaskList(List<SimulationTaskDTO> list, Authentication authentication) {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(authentication.getName());
        for (SimulationTaskDTO task : list) {
            String id = "sim_" + task.getId();
            if (ExtendedExecutor.containsKey(id)) {
                logger.warn(String.format("??????????????????(%s)????????????????????????????????????", id));
                continue;
            }
            pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "", "", new Date())) {
                @Override
                @TimeCounter
                protected void start() throws InterruptedException {
                    logger.info(String.format("??????(%d)[%s]????????????????????????...", task.getId(), task.getTheme()));
                    recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATING);
                    List<PathInfoTaskDTO> pathInfoTaskDTOList ;
                    //????????????,?????????????????????????????????
                    pathInfoTaskDTOList = pathService.qtBusinessFindPath(task);

                    int size = pathInfoTaskDTOList.size();
                    logger.info(String.format("??????(%d)[%s]?????????????????????????????????%d?????????...", task.getId(), task.getTheme(), size));

                    CountDownLatch latch = new CountDownLatch(size);
                    for (PathInfoTaskDTO pathInfoTaskDTO : pathInfoTaskDTOList) {
                        String id = "path_" + pathInfoTaskDTO.getId();
                        if (ExtendedExecutor.containsKey(id)) {
                            //??????????????????
                            logger.warn(String.format("??????(%d)[%s]??????%d????????????(%s)????????????????????????????????????", task.getId(), task.getTheme(), pathInfoTaskDTO.getId(), id));
                            //?????????????????????latch??????????????????????????????????????????????????????????????????
                            latch.countDown();
                            continue;
                        }

                        analyzeExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "", "", new Date()), latch) {
                            @Override
                            protected void start() throws InterruptedException, Exception {
                                try {
                                    //????????????
                                    if (analyzeService.analyzePathByPathInfo(pathInfoTaskDTO,task) != ReturnCode.POLICY_MSG_OK) {
                                        return;
                                    }
                                    //?????????????????????????????????
                                    if (recommendServiceImpl.recommendPolicyByPathInfo(pathInfoTaskDTO,task) != ReturnCode.POLICY_MSG_OK) {
                                        return;
                                    }

                                    /*if (checkService.checkPolicyByPathInfo(pathInfoTaskDTO,task) != ReturnCode.POLICY_MSG_OK) {
                                        return;
                                    }*/

                                    if (riskService.checkPolicyRecommendRiskByPathInfo(pathInfoTaskDTO) != ReturnCode.POLICY_MSG_OK) {
                                        return;
                                    }

                                    if(pathInfoTaskDTO.getPolicyList() != null && pathInfoTaskDTO.getPolicyList().size() > 0) {
                                        synchronized (task) {
                                            //???????????????????????????
                                            commandSimulationCommonService.addPolicyToTask(task, pathInfoTaskDTO.getPolicyList());
                                        }
                                    } else {
                                        logger.warn(String.format("??????(%d)[%s]??????%d?????????????????????????????????????????????", task.getId(), task.getTheme(), pathInfoTaskDTO.getId()));
                                    }
                                } catch (Exception e) {
                                    logger.error(String.format("??????(%d)[%s]??????%d?????????????????????", task.getId(), task.getTheme(), pathInfoTaskDTO.getId()), e);
                                } finally {

                                }
                            }
                        });
                    }

                    try {
                        latch.await();
                    } catch (Exception e) {
                        logger.error(String.format("??????(%d)[%s]????????????????????????",task.getId(), task.getTheme()), e);
                        throw e;
                    }

                    logger.info(String.format("??????(%d)[%s]?????????????????????????????????...",task.getId(), task.getTheme()));

                    if (task.getDevicePolicyMap() != null && task.getDevicePolicyMap().size() > 0) {
                        //????????????
                        mergeService.mergePolicy(task);
                        //???????????????????????????????????????
                        checkService.checkPolicyByPolicyTask(task);
                        //???????????????
                        commandService.generateCommandline(task,userInfoDTO);

                    } else {
                        logger.info(String.format("??????(%d)[%s]???????????????...",task.getId(), task.getTheme()));
                    }

                    recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE);
                }
            });

        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * ????????????
     *
     * @param taskList ????????????
     * @return ????????????????????????
     */
    @Override
    public List<String> stopTaskList(List<String> taskList) {
        List<String> failedList = new ArrayList();
        for(String taskId:taskList) {
            String id = taskId;
            if(ExtendedExecutor.containsKey(id)) {
                logger.info("????????????:" + id);
                boolean stopped = ExtendedExecutor.stop(id, STOP_TYPE_STOP);
                if (!stopped) {
                    failedList.add(taskId);
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
                }
            }
        }
        return failedList;
    }



    @Override
    public int  addReassembleCommandLineTask(SimulationTaskDTO task, Authentication authentication) {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(authentication.getName());
        String id = String.valueOf(task.getId());
        if (ExtendedExecutor.containsKey(id)){
            logger.warn(String.format("??????????????????( %d )????????????????????????????????????", id));
            return ReturnCode.TASK_ALREADY_EXIST;
        }

        pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {


                logger.info(String.format("????????????????????????(%d)%s?????????????????????...", task.getId(), task.getTheme()));
                recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATING);

                recommendTaskManager.removeCommandsByTask(task.getId());

                boolean hasPath = false;
                List<PathInfoEntity> pathInfoTaskList = recommendTaskManager.getPathInfoByTaskId(task.getId());
                for (PathInfoEntity entity : pathInfoTaskList) {
                    if (PolicyConstants.PATH_ENABLE_ENABLE.equals(entity.getEnablePath())) {
                        hasPath = true;
                    }
                }

                if(hasPath) {
                    mergeService.loadAndMergePolicy(task);
                    //?????????????????????
                    commandSimulationCommonService.reGenerateCommandLine(task,userInfoDTO);
                }
                recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE);
            }
        });

        return ReturnCode.POLICY_MSG_OK;
    }






    /**
     * ??????push????????????????????????--?????????????????????
     */
    @PostConstruct
    public void initRecommendStatus () {
        logger.info("**********??????push??????????????????????????????????????????**********");
        List<RecommendTaskEntity> list = recommendTaskManager.selectExecuteRecommendTask();
        if (ObjectUtils.isEmpty(list)) {
            logger.info("**********???????????????????????????????????????**********");
            return;
        }
        for (RecommendTaskEntity task : list) {
            if (PolicyConstants.POLICY_INT_STATUS_SIMULATING == task.getStatus()) {
                //???????????????--->????????????
                policyRecommendTaskService.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
            } else if (PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE == task.getStatus()) {
                //???????????????--->???????????????
                policyRecommendTaskService.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_INITIAL);
            } else if (PolicyConstants.POLICY_INT_STATUS_VERIFYING == task.getStatus()) {
                //?????????--->????????????
                policyRecommendTaskService.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
            }
        }
        logger.info("**********??????push??????????????????????????????????????????**********");
    }
}
