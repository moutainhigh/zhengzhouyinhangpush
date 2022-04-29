package com.abtnetworks.totems.mapping.service;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.dto.PushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.dto.SearchPushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.github.pagehelper.PageInfo;

/**
 * @desc    地址映射池接口
 * @author liuchanghao
 * @date 2022-02-16 10:34
 */
public interface PushAutoMappingPoolService {
    
    /**
     * 保存关联信息映射信息
     * @param PushAutoMappingPoolDTO
     * @return
     */
    TotemsReturnT savePushAutoMappingPoolInfo(PushAutoMappingPoolDTO PushAutoMappingPoolDTO);

    /**
     * 删除
     * @param ids
     * @return
     */
    int deletePushAutoMappingPoolInfo(String ids);

    /**
     * 修改关联信息映射信息
     * @param PushAutoMappingPoolDTO
     * @return
     */
    TotemsReturnT updatePushAutoMappingPoolInfo(PushAutoMappingPoolDTO PushAutoMappingPoolDTO);

    /**
     * 更新下一个可用ip
     * @param pushAutoMappingPoolDTO
     * @returnp
     */
    int updateNextAvailableIp(PushAutoMappingPoolDTO pushAutoMappingPoolDTO);

    /**
     * 修改关联信息映射信息
     * @param searchPushAutoMappingPoolDTO
     * @return
     */
    PageInfo<PushAutoMappingPoolEntity> listPushAutoMappingPoolInfo(SearchPushAutoMappingPoolDTO searchPushAutoMappingPoolDTO);


}
