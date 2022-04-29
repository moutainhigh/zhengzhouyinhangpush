package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.dto.PushRecommendTaskExpandDTO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 静态路由service
 */
public interface PushTaskStaticRoutingService {
    /**
     * 单个增加(混合新增和命令行一起新增)
     *
     * @param dto
     * @return
     */
    int createPushTaskStaticRouting(PushRecommendStaticRoutingDTO dto) ;

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    PageInfo<PushRecommendStaticRoutingDTO> findPushTaskStaticRoutingPage(PushRecommendStaticRoutingDTO dto) ;

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    int deletePushTaskStaticRouting(String ids) ;

    /**
     * 批量新增静态路由数据（命令行生成混在一起新增）
     * @param dtos
     * @return
     */
    int  batchCreateStaticRoute(List<PushRecommendStaticRoutingDTO> dtos);


    /**
     * 根据任务id获取静态路由信息
     * @param taskId
     * @return
     */
    PushRecommendStaticRoutingDTO  getStaticRoutingByTaskId(Integer taskId);
}
