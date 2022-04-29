package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfNatDTO;
import com.abtnetworks.totems.recommend.entity.AddRecommendTaskEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 公共业务接口
 * @date 2021/1/14
 */
public interface RecommendBussCommonService {
    /**
     * 创建模拟变更
     * @param taskEntity
     * @return
     */
    WhatIfRO createWhatIfCaseUuid(RecommendTaskEntity taskEntity);

    /**
     * 创建模拟变更
     * @param natExcelList
     * @param natTaskList
     * @param whatIfCaseName
     * @param user
     * @param userInfoDTO
     * @return
     */
    WhatIfRO createWhatIfCaseUuid(List<ExcelTaskNatEntity> natExcelList, List<RecommendTaskEntity> natTaskList, List<PushRecommendStaticRoutingDTO> routeTaskList, List<RecommendRelevanceSceneDTO> specialNatList, String whatIfCaseName, String user, UserInfoDTO userInfoDTO);

    /**
     * 将Nat开通工单Excel数据转化为WhatIfNatDTO列表
     * @param natExcelList
     * @return
     */
    List<WhatIfNatDTO> getWhatIfNatDTOList(List<ExcelTaskNatEntity> natExcelList);

    /**
     * 将飞塔NAT场景Excel数据转化为WhatIfNatDTO列表
     * @param specialNatList
     * @return
     */
    List<WhatIfNatDTO> getWhatIfSpecialNatDTOList(List<RecommendRelevanceSceneDTO> specialNatList);

    /**
     * 检查ipv4 和 ipv 6 目的地址
     * @param entity
     * @return
     */
    int checkParamForDstAddress(AddRecommendTaskEntity entity);
    /**
     * 检查ipv4 和 ipv 6 源地址
     * @param entity
     * @return
     */
    int checkParamForSrcAddress(AddRecommendTaskEntity entity);

    /**
     * 自动关联nat还是高级设置选择
     * @param entity
     * @param auth
     */
    RecommendTaskEntity addAutoNatGenerate( AddRecommendTaskEntity entity, Authentication auth) throws IssuedExecutorException;


    /**
     * 查询批量导入数据是否关联nat策略
     * @param recommendTaskEntity
     * @param auth
     */
    int checkPostRelevancyNat(RecommendTaskEntity recommendTaskEntity, Authentication auth);


   /**
    * excel 导入是否需要自动仿真，api导入需要自动仿真，页面导入不需要自动仿真
    * @param autoStartRecommend
    * @param recommendTaskEntityList
    * @param auth
    * @return
    */
    Boolean autoStartRecommend(Boolean autoStartRecommend,List<RecommendTaskEntity> recommendTaskEntityList, Authentication auth);

    /**
     * 获取静态路由DTO
     * @param routeExcelList
     * @param user
     * @param userInfoDTO
     * @return
     */
    List<PushRecommendStaticRoutingDTO> getRouteExcelDTO(List<ExcelTaskStaticRouteEntity> routeExcelList, String user, UserInfoDTO userInfoDTO);


    /**
     * 获取静态路由DTO
     * @param specialNatEntityList
     * @param user
     * @param userInfoDTO
     * @return
     */
    List<RecommendRelevanceSceneDTO> getSpecialNatExcelDTO(List<ExcelTaskSpecialNatEntity> specialNatEntityList, String user, UserInfoDTO userInfoDTO);

    /**
     * 批量更新关联NAT的任务id
     * @param tmpList
     */
    void updateRelevanceNatTaskId(List<RecommendTaskEntity> tmpList);

    /**
     * 更新关联NAT的任务id
     * @param recommendTaskEntity
     */
    void updateRelevancyNatTaskId(RecommendTaskEntity recommendTaskEntity,boolean cleanTaskId);

}
