package com.abtnetworks.totems.push.service.task;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.push.dto.BatchCommandTaskDTO;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.vo.NewPolicyPushVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/19 1:18
 */
@Service
public interface PushTaskService {


    /**
     * 批量添加策略下发任务
     * @param taskList
     * @return
     */
    int addCommandTaskList(List<CommandTaskDTO> taskList,boolean doPush);

    /**
     * 批量添加策略下发任务V2
     * @param batchTaskLists
     * @return
     */
    int addCommandTaskListV2(List<BatchCommandTaskDTO> batchTaskLists,List<Integer> taskIds) throws Exception;

    /**
     * 批量下发任务
     * @param taskList
     * @param doPush
     * @return
     */
    int preBatchPushTaskList(List<CommandTaskDTO> taskList,boolean doPush);

    /**
     * 单设备添加策略下发任务
     * @param commandTaskDTO
     * @param doPush
     * @return
     */
    int addDeviceCommandTaskList(CommandTaskDTO commandTaskDTO,boolean doPush);
    /**
     * 批量停止任务
     * @param taskList 任务队列id列表
     * @return 停止失败的任务结果
     */
    List<String> stopTaskList(List<String> taskList, Integer isRevert);

    /**
     * 停止所有下发任务
     * @return 停止的任务列表
     */
    int stopAllTasks();

    /**
     * 检测任务是否正在运行
     * @param taskId 任务id
     * @return 任务正在运行返回true，否则返回false
     */
    boolean checkTaskRunning(int taskId);

    /**新建策略，生成建议策略，生成命令行**/
    int newPolicyPush(NewPolicyPushVO vo) throws Exception;

    /**
     * 新建自定义命令行
     **/
    int newCustomizeCmd(NewPolicyPushVO vo);

    /**
     * 新建删除命令行
     **/
    int generateDeleteCommandLine(NodeEntity nodeEntity, Integer policyId, Integer ipType, String policyName, String srcZone, String dstZone, String userName) throws Exception;

    int addGenerateCmdTask(CmdDTO cmdDTO);

    int pushPeriod();

    /**
     * 下发结果邮件告警
     * @param taskList 任务List
     */
    void startCommandTaskEditableEmail(List<CommandTaskEditableEntity> taskList);

    void addCommandTaskListForSchedule(List<CommandTaskDTO> taskList,boolean doPush);

    void addCommandTaskListForScheduleV2(List<CommandTaskDTO> taskList,boolean doPush) throws Exception;

}
