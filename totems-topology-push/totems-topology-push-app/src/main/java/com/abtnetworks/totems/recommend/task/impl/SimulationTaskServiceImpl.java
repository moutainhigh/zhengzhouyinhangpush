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
                logger.warn(String.format("策略仿真任务(%s)已经存在！任务不重复添加", id));
                continue;
            }
            pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "", "", new Date())) {
                @Override
                @TimeCounter
                protected void start() throws InterruptedException {
                    logger.info(String.format("任务(%d)[%s]开始仿真策略开通...", task.getId(), task.getTheme()));
                    recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATING);
                    List<PathInfoTaskDTO> pathInfoTaskDTOList ;
                    //明细开通,大网段开通，互联网开通
                    pathInfoTaskDTOList = pathService.qtBusinessFindPath(task);

                    int size = pathInfoTaskDTOList.size();
                    logger.info(String.format("任务(%d)[%s]查找路径完成，总共找到%d条路径...", task.getId(), task.getTheme(), size));

                    CountDownLatch latch = new CountDownLatch(size);
                    for (PathInfoTaskDTO pathInfoTaskDTO : pathInfoTaskDTOList) {
                        String id = "path_" + pathInfoTaskDTO.getId();
                        if (ExtendedExecutor.containsKey(id)) {
                            //一般不会出现
                            logger.warn(String.format("任务(%d)[%s]路径%d分析任务(%s)已经存在！任务不重复添加", task.getId(), task.getTheme(), pathInfoTaskDTO.getId(), id));
                            //添加失败需要将latch减少，以免计数器无法完成导致线程永远无法完成
                            latch.countDown();
                            continue;
                        }

                        analyzeExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "", "", new Date()), latch) {
                            @Override
                            protected void start() throws InterruptedException, Exception {
                                try {
                                    //路径查询
                                    if (analyzeService.analyzePathByPathInfo(pathInfoTaskDTO,task) != ReturnCode.POLICY_MSG_OK) {
                                        return;
                                    }
                                    //按照路径状态过滤成策略
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
                                            //同设备过滤添加策略
                                            commandSimulationCommonService.addPolicyToTask(task, pathInfoTaskDTO.getPolicyList());
                                        }
                                    } else {
                                        logger.warn(String.format("任务(%d)[%s]路径%d没有策略生成，不添加策略到任务", task.getId(), task.getTheme(), pathInfoTaskDTO.getId()));
                                    }
                                } catch (Exception e) {
                                    logger.error(String.format("任务(%d)[%s]路径%d策略仿真异常：", task.getId(), task.getTheme(), pathInfoTaskDTO.getId()), e);
                                } finally {

                                }
                            }
                        });
                    }

                    try {
                        latch.await();
                    } catch (Exception e) {
                        logger.error(String.format("任务(%d)[%s]路径路径分析异常",task.getId(), task.getTheme()), e);
                        throw e;
                    }

                    logger.info(String.format("任务(%d)[%s]所有路径分析线程已完成...",task.getId(), task.getTheme()));

                    if (task.getDevicePolicyMap() != null && task.getDevicePolicyMap().size() > 0) {
                        //合并策略
                        mergeService.mergePolicy(task);
                        //策略合并之后再进行策略检查
                        checkService.checkPolicyByPolicyTask(task);
                        //生成命令行
                        commandService.generateCommandline(task,userInfoDTO);

                    } else {
                        logger.info(String.format("任务(%d)[%s]无策略生成...",task.getId(), task.getTheme()));
                    }

                    recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE);
                }
            });

        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 停止任务
     *
     * @param taskList 任务队列
     * @return 停止任务队列结果
     */
    @Override
    public List<String> stopTaskList(List<String> taskList) {
        List<String> failedList = new ArrayList();
        for(String taskId:taskList) {
            String id = taskId;
            if(ExtendedExecutor.containsKey(id)) {
                logger.info("停止任务:" + id);
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
                    logger.info("队列中任务名称：" + extendedRunnable.getExecutorDto().toString()+"，要停止的线程名称"+stopIds);
                    return stopIds.equals(extendedRunnable.getExecutorDto().getId());
                });
                if(!rc) {
                    logger.info("停止任务失败：" + id);
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
            logger.warn(String.format("策略仿真任务( %d )已经存在！任务不重复添加", id));
            return ReturnCode.TASK_ALREADY_EXIST;
        }

        pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {


                logger.info(String.format("开始仿真策略任务(%d)%s重新生成命令行...", task.getId(), task.getTheme()));
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
                    //重新生成命令行
                    commandSimulationCommonService.reGenerateCommandLine(task,userInfoDTO);
                }
                recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE);
            }
        });

        return ReturnCode.POLICY_MSG_OK;
    }






    /**
     * 重启push重置策略开通状态--重置为仿真失败
     */
    @PostConstruct
    public void initRecommendStatus () {
        logger.info("**********重启push重置策略开通状态方法执行开始**********");
        List<RecommendTaskEntity> list = recommendTaskManager.selectExecuteRecommendTask();
        if (ObjectUtils.isEmpty(list)) {
            logger.info("**********策略仿真没有执行中状态数据**********");
            return;
        }
        for (RecommendTaskEntity task : list) {
            if (PolicyConstants.POLICY_INT_STATUS_SIMULATING == task.getStatus()) {
                //执行执行中--->仿真失败
                policyRecommendTaskService.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
            } else if (PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE == task.getStatus()) {
                //执行等待中--->仿真未开始
                policyRecommendTaskService.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_INITIAL);
            } else if (PolicyConstants.POLICY_INT_STATUS_VERIFYING == task.getStatus()) {
                //验证中--->验证失败
                policyRecommendTaskService.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
            }
        }
        logger.info("**********重启push重置策略开通状态方法执行结束**********");
    }
}
