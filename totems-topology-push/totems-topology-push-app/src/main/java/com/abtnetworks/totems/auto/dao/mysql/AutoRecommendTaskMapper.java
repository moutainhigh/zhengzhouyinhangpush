package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.credential.dto.UpdateCredentialDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Mapper
@Repository
public interface AutoRecommendTaskMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(AutoRecommendTaskEntity record);

    int insertSelective(AutoRecommendTaskEntity record);

    AutoRecommendTaskEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AutoRecommendTaskEntity record);

    int updateByPrimaryKey(AutoRecommendTaskEntity record);


    /**
     * 根据uuid获取自动开通任务
     * @param uuid
     * @return
     */
    AutoRecommendTaskEntity getByUuid(String uuid);

    /**
     * 查询防护网段列表
     * @param record
     * @return
     */
    List<AutoRecommendTaskEntity> findList(AutoRecommendTaskEntity record);

    /**
     * 获取下发成功的即将过期工单--定时发送至申请人及管理员
     * @return
     */
    List<AutoRecommendTaskEntity> getWillExpireTask();


    /**
     * 获取需要定时下发的任务
     * @return
     */
    List<AutoRecommendTaskEntity> getAutoPushTask();

    /**
     * 根据name获取自动开通任务
     * @param name
     * @return
     */
    AutoRecommendTaskEntity getByName(String name);

    /**
     * 查询所有自动开通工单数据
     * @return
     */
    List<AutoRecommendTaskEntity> findAll();

    /**
     * 查询不能重复创建相同五元组的自动开通工单数据
     * @return
     */
    List<AutoRecommendTaskEntity> findCannotCreateTaskByConditions(Integer accessType);

    /**
     * 批量修改
     * @param updateAutoRecommendTasks
     *
     * @return
     */
    int updateList(@Param("updateAutoRecommendTasks") List<AutoRecommendTaskEntity> updateAutoRecommendTasks);

}