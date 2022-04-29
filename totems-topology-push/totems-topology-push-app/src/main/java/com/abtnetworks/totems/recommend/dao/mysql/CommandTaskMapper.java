package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.push.vo.PushTaskVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface CommandTaskMapper {


    /**
     * 根据策略id获取命令行数据
     * @param policyId 策略id
     * @return
     */
    List<CommandTaskEntity> selectByPolicyId(Integer policyId);

    /**
     * 根据策略id获取命令行数据
     * @param taskId 任务id
     * @return
     */
    List<CommandTaskEntity> selectByTaskId(Integer taskId);

    /**
     * 根据路径信息id获取命令行数据
     * @param pathInfoId 路径信息id
     * @return
     */
    List<CommandTaskEntity> selectByPathInfoId(Integer pathInfoId);

    /**
     * 根据策略id删除命令行
     * @param policyId
     * @return
     */
    int deleteByPolicyId(Integer policyId);

    /**
     * 根据任务号删除命令行
     * @param taskId
     * @return
     */
    int deleteByTaskId(Integer taskId);

    /**
     * 插入命令行下发任务
     * @param record 命令行下发任务数据
     * @return
     */
    int insert(CommandTaskEntity record);


    /**
     * 更新下发结果
     * @param record 命令行任务数据
     * @return
     */
    int updateDevicePushResultByTaskId(CommandTaskEntity record);

    /**
     * 更新下发结果
     * @param record
     * @return
     */
    int updateDevicePushResultByPathInfoId(CommandTaskEntity record);

    /**
     * 更新命令行下发任务数据
     * @param record 命令行任务数据
     * @return
     */
    int update(CommandTaskEntity record);

    /**
     * 更新命令行下发任务状态
     * @param record 命令行任务数据
     * @return
     */
    int updateStatus(CommandTaskEntity record);

    /**
     * 根据任务id和设备UUID获取命令行数据
     * @param params 查询参数
     * @return
     */
    List<CommandTaskEntity> selectByTaskIdAndDeviceUuid(Map<String, String> params);

    /**
     * 根据任务id和设备UUID获取命令行数据
     * @param params 查询参数
     * @return
     */
    List<CommandTaskEntity> selectByPathInfoIdAndDeviceUuid(Map<String, String> params);

    /**
     * 获取命令行任务数据列表
     * @param params 查询参数
     * @return 命令行任务数据列表
     */
    List<CommandTaskEntity> searchPushTaskList(Map<String, String> params);

    /**
     * 获取命令行数据列表
     * @param params 查询参数
     * @return 命令行任务数据列表
     */
    List<PushTaskVO> getPushTaskList(Map<String, String> params);

    /**
     * 更新命令行下发任务数据
     * @param record 命令行任务数据
     * @return
     */
    int updateSelective(CommandTaskEntity record);
}