package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.entity.RecommendRelevanceSceneEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
* 仿真关联场景实体服务service
*
* @author Administrator
* @since 2021年12月24日
*/
public interface RecommendRelevanceSceneService {

    /**
     * 单个新增
     * 
     * @param dto
     * @return
     */
    int createRecommendRelevanceScene(RecommendRelevanceSceneDTO dto);


    /**
     * 单个更新
     * 
     * @param dto
     * @return
     */
    int updateRecommendRelevanceScene(RecommendRelevanceSceneDTO dto);

    /**
     * 更新关联关系
     * @param sceneDTO
     * @return
     */
    int updateRelevanceSceneTaskId(RecommendRelevanceSceneDTO sceneDTO);


    /**
     * 查询所有
     * 
     * @param taskId
     * @return
     */
    PageInfo<RecommendRelevanceSceneDTO> getRecommendRelevanceScene(int page, int psize, String taskId, String name, String deviceUuid, String userName);


    /**
     * 根据id查询关联关系
     * @param id
     * @return
     */
    RecommendRelevanceSceneDTO queryById(Integer id);

    /**
     * 删除关联场景
     * @param ids
     * @return
     */
    int deleteRecommendRelevanceScene(String ids);
}
