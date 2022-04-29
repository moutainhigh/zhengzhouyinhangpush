package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.push.entity.PushTaskEntity;
import com.abtnetworks.totems.push.manager.PushTaskManager;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.service.ExceptionService;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExceptionServiceImpl implements ExceptionService {

    private static Logger logger = LoggerFactory.getLogger(ExceptionServiceImpl.class);

    @Autowired
    RecommendTaskManager taskService;

    @Autowired
    PushTaskManager pushTaskService;

    @Override
    public void handleException(int taskId, int statusType) {
        logger.info(String.format("处理任务%d异常,状态%d", taskId, statusType));
        switch(statusType) {
            case PolicyConstants.POLICY_INT_TASK_ANALYZE:
                logger.info("PolicyConstants.POLICY_INT_TASK_ANALYZE");
//                taskService.updateAnalyzeStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_RECOMMEND:
//                taskService.updateAdviceStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_ADVICE_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_CHECK:
//                taskService.updateCheckStatus(taskId,PolicyConstants.POLICY_INT_RECOMMENT_CHECK_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_RISK:
//                taskService.updateRiskStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_RISK_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_CMD:
//                taskService.updateCmdStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_CMD_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_ADD_PUSH_LIST:
//                taskService.updateAddPushTaskStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_ADD_PUSH_TASK_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_PUSH:
//                taskService.updatePushStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_PUSH_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                RecommendTaskEntity taskEntity = taskService.getRecommendTaskByTaskId(taskId);
                if(taskEntity == null) {
                    break;
                }

                List<PushTaskEntity> list = pushTaskService.getPolicyRecommendTaskListByOrderNo(taskEntity.getOrderNumber());
                for(PushTaskEntity entity:list) {
                    pushTaskService.updatePushStatus(entity.getId(), PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                }
                break;
            case PolicyConstants.POLICY_INT_TASK_GATHER:
//                taskService.updateGatherStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_GATHER_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_ACCESS_ANALYZE:
//                taskService.updateAccessAnalyzeStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_ACCESS_ANALYZE_FAILED);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
                break;
            case PolicyConstants.POLICY_INT_TASK_VERIFY:
//                taskService.updateVerifyStatus(taskId,PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR);
//                taskService.updateStatus(taskId, PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
                break;

        }
    }

    @Override
    public void handleException(int taskId) {
        taskService.updateTaskStatus(taskId, PolicyConstants.POLICY_INT_TASK_TYPE_ERROR);
    }
}
