package com.abtnetworks.totems.advanced.service;

import com.abtnetworks.totems.advanced.dto.SceneForFiveBalanceDTO;
import com.abtnetworks.totems.advanced.entity.SceneForFiveBalanceEntity;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
* 策略生成F5负载场景实体类服务service
*
* @author Administrator
* @since 2021年07月30日
*/
public interface SceneForFiveBalanceService {

    /**
     * 单个新增
     * 
     * @param dto
     * @return
     */
    int createSceneForFiveBalance(SceneForFiveBalanceDTO dto) throws IssuedExecutorException;

    /**
     * 单个删除
     * 
     * @param ids
     * @return
     */
    int deleteById(String ids);

    /**
     * 单个更新
     * 
     * @param dto
     * @return
     */
    int updateSceneForFiveBalance(SceneForFiveBalanceDTO dto);
    /**
     * 单个查询
     * 
     * @param dto
     * @return
     */
    SceneForFiveBalanceDTO getSceneForFiveBalance(SceneForFiveBalanceDTO dto);

    /**
     * 查询所有
     * 
     * @param dto
     * @return
     */
    PageInfo<SceneForFiveBalanceEntity> querySceneForFiveBalanceList(SceneForFiveBalanceDTO dto);
}
