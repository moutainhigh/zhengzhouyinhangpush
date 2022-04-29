package com.abtnetworks.totems.mapping.service;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import com.abtnetworks.totems.mapping.vo.AutoMappingSceneRuleVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @desc    地址映射自动匹配接口
 * @author liuchanghao
 * @date 2022-01-21 10:34
 */
public interface AutoMappingSceneRuleService {

    /**
     * 进行工单检测
     * @param checkVO
     * @return
     * @throws Exception
     */
    TotemsReturnT addOrUpdate(AutoMappingSceneRuleVO checkVO) throws Exception;

    /**
     * 分页查询
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushAutoMappingSceneRuleEntity> findList(AutoMappingSceneRuleVO vo, int pageNum, int pageSize);

    /**
     * 删除
     * @param idList
     * @return
     * @throws Exception
     */
    int deleteIdList(List<Integer> idList) throws Exception;

    /**
     * 根据id查询
     * @param id
     * @return
     */
    PushAutoMappingSceneRuleEntity selectById(int id) ;


}
