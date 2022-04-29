package com.abtnetworks.totems.advanced.service;

import com.abtnetworks.totems.advanced.dto.PushMappingNatDTO;
import com.abtnetworks.totems.advanced.dto.SearchPushMappingNatDTO;
import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import com.github.pagehelper.PageInfo;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/8
 */
public interface PushMappingNatService {
    /**
     * 保存关联信息映射信息
     * @param pushMappingNatDTO
     * @return
     */
    int savePushMappingNatInfo(PushMappingNatDTO pushMappingNatDTO);

    /**
     * 删除
     * @param ids
     * @return
     */
    int deletePushMappingNatInfo(String ids);

    /**
     * 修改关联信息映射信息
     * @param pushMappingNatDTO
     * @return
     */
    int updatePushMappingNatInfo(PushMappingNatDTO pushMappingNatDTO);

    /**
     * 修改关联信息映射信息
     * @param searchPushMappingNatDTO
     * @return
     */
    PageInfo<PushMappingNatEntity> listPushMappingNatInfo(SearchPushMappingNatDTO searchPushMappingNatDTO);


}
