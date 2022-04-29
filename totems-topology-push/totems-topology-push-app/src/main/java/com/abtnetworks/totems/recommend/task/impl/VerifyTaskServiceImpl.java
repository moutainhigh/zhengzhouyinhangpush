package com.abtnetworks.totems.recommend.task.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.*;
import com.abtnetworks.totems.recommend.task.VerifyTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

@Service
public class VerifyTaskServiceImpl implements VerifyTaskService {
    private static Logger logger = LoggerFactory.getLogger(VerifyTaskServiceImpl.class);

    private static final String VERIFY_THREADS_NAME = "verify_thread";

    @Autowired
    private VerifyService verifyService;

    @Autowired
    RecommendTaskManager recommendTaskService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    @Qualifier(value = "pushExecutor")
    private Executor pushExecutor;

    @Override
    public int startVerify(List<RecommendTaskEntity> list) {
        logger.info("创建验证线程...");
        if(ExtendedExecutor.containsKey(VERIFY_THREADS_NAME)) {
            return ReturnCode.VERIFICATION_IS_RUNNING;
        }

        pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(VERIFY_THREADS_NAME,"","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                if(list == null) {
                    logger.error("验证任务队列为空");
                    return;
                }
                try {
                    logger.info("开始验证...");
                    verifyService.verifyTask(list);
                } catch (Exception e) {
                    logger.error("策略验证异常：",e);
                    for(RecommendTaskEntity entity: list) {
                        exceptionService.handleException(entity.getId(), PolicyConstants.POLICY_INT_TASK_VERIFY);
                    }
                    throw e;
                }
            }
        });


        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public boolean isVerifyRunning() {
        if(ExtendedExecutor.containsKey(VERIFY_THREADS_NAME)) {
            return true;
        }
        return false;
    }
}
