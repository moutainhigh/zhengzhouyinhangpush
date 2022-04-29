package com.abtnetworks.totems.common.tools.queue;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.tools.vo.PushQueueVo;
import com.abtnetworks.totems.push.dto.BatchCommandTaskDTO;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author lifei
 * @desc 内存队列工具类
 * @date 2021/11/30 13:45
 */
@Slf4j
@Component
public class PushBlockingQueueTool {

    @Resource
    PushTaskService pushTaskService;
    @Resource
    public RecommendTaskManager taskService;

    /**
     * 定义内存队列
     */
    private static BlockingQueue<PushQueueVo> blockQueue = new ArrayBlockingQueue<>(20);


    /**
     * 获取队列的长度
     */
    public int getQueueSize() {
        return blockQueue.size();
    }

    /**
     * 获取队列种所有的数据
     */
    public List<PushQueueVo> getAllDataFromQueue() {
        Iterator<PushQueueVo> iterator = blockQueue.iterator();
        return IteratorUtils.toList(iterator);
    }

    /**
     * 添加信息至队列中
     *
     * @param pushQueueVo
     */
    public void addQueue(PushQueueVo pushQueueVo) throws InterruptedException {
        try {
            blockQueue.put(pushQueueVo);
        } catch (InterruptedException e) {
            log.error("添加数据到内存队列异常,异常原因:{}",e);
            throw e;
        }
    }

    /**
     * 如果任务停住,只能停队列中还没有执行的批次
     */
    public void stopPush() {
        blockQueue.clear();
    }


    public void execute() throws Exception {
            synchronized (PushBlockingQueueTool.class) {
                long start = System.currentTimeMillis();
                PushQueueVo pushQueueVo = null;
                while (blockQueue.size() > 0) {
                    try {
                        pushQueueVo = blockQueue.take();
                        if (null == pushQueueVo) {
                            continue;
                        }
                        log.info("当前消费工单任务id为：{}", pushQueueVo.getTaskIds());
                        pushTaskService.addCommandTaskListV2(pushQueueVo.getBatchCommandTaskDTOS(), pushQueueVo.getTaskIds());
                    } catch (Exception e) {
                        log.error("批量下发工单异常,异常原因:{}", e);
                        if (null == pushQueueVo) {
                            continue;
                        }
                        // 更新工单表为下发失败
                        for (Integer taskId : pushQueueVo.getTaskIds()) {
                            taskService.updateTaskStatus(taskId, PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR);
                        }
                        // 更新命令行表的下发状态
                        List<BatchCommandTaskDTO> batchCommandTaskDTOList = pushQueueVo.getBatchCommandTaskDTOS();
                        List<CommandTaskEditableEntity> list = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(batchCommandTaskDTOList)) {
                            for (BatchCommandTaskDTO taskDTO : batchCommandTaskDTOList) {
                                list.addAll(taskDTO.getList());
                            }
                        }
                        // 更新命令行表下发状态为异常
                        taskService.updateCommandTaskStatus(list, PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR);
                    }
                }
                long end = System.currentTimeMillis();
                long consume = end - start;
                log.info("队列从开始消费到消费完一共,耗时【{}】毫秒", consume);
            }
    }

}
