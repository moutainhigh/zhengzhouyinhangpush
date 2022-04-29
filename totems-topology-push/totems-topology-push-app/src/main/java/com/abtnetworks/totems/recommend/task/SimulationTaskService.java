package com.abtnetworks.totems.recommend.task;

import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/7 15:02
 */
@Service
public interface SimulationTaskService {


    /**
     * 添加策略仿真任务到任务队列
     * @param list 策略仿真任务队列
     * @return
     */
    int addSimulationTaskList(List<SimulationTaskDTO> list, Authentication authentication);

    /**
     * 停止任务
     * @param taskList 任务队列id列表
     * @return 成功失败的任务队列结果
     */
    List<String> stopTaskList(List<String> taskList);





    int addReassembleCommandLineTask(SimulationTaskDTO task,Authentication authentication);




}
