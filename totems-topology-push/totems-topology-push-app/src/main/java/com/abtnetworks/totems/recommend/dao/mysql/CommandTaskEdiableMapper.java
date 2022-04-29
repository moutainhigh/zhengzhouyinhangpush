package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.push.dto.PushStatus;
import com.abtnetworks.totems.push.vo.CheckRelevancyNatOrderVO;
import com.abtnetworks.totems.push.vo.PushTaskVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface CommandTaskEdiableMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CommandTaskEditableEntity record);

    int insertSelective(CommandTaskEditableEntity record);

    CommandTaskEditableEntity selectByPrimaryKey(Integer id);

    List<CommandTaskEditableEntity> selectByTaskId(Integer taskId);

    List<CommandTaskEditableEntity> selectByTaskIdList(Map<String, Object> cond);

    /**
     * key传taskIds，value传taskid的list
     * @param cond
     * @return
     */
    List<CommandTaskEditableEntity> selectPushStuasByTaskIdList(Map<String, Object> cond);

    List<CommandTaskEditableEntity> selectByTaskIdAndDeviceUuid(Map<String, String> params);

    /**
     * 根据主题、类型查询命令行信息
     * @param params key=theme、taskType
     * @return
     */
    List<CommandTaskEditableEntity> selectByThemeAndTaskType(Map<String, Object> params);

    int updateByPrimaryKeySelective(CommandTaskEditableEntity record);

    int updateByPrimaryKey(CommandTaskEditableEntity record);

    /**
     * 停止下发按钮更新时清空下发计划时间
     * @param params
     * @return
     */
    int updateForStopTask(Map<String, String> params);

    int deleteByTaskId(Integer taskId);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 获取命令行数据列表
     * @param params 查询参数
     * @return 命令行任务数据列表
     */
    List<PushTaskVO> getPushTaskList(Map<String, Object> params);

    int insertList(List<CommandTaskEditableEntity> list);

    List<PushStatus> getPushTaskStatusList(@Param("branchLevel") String  branchLevels);

    /**
     * 单独查总数
     * @return
     */
    int getPushTaskStatusListTotal(Map<String, String> params);

    /**
     * 查询执行中的策略下发数据
     * @return
     */
    List<CommandTaskEditableEntity> selectExecuteTask();

    List<CommandTaskEditableEntity> selectScheduledTask();

    int setPushSchedule(CommandTaskEditableEntity entity);

    /**
     * 检查关联nat的下发工单sql
     * @param ids
     * @return
     */
    List<CheckRelevancyNatOrderVO>  selectRecommendNatOrderByIds(@Param("ids") String ids);

    //查询所有已完成推送任务
    List<CommandTaskEditableEntity> selectAllPushList(Map<String, Object> params);

    /**
     * 根据任务ID和任务类型查询
     * @param taskId
     * @param taskType
     * @return
     */
    List<CommandTaskEditableEntity> selectByTaskIdAndType(Integer taskId, Integer taskType);
}