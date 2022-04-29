package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.entity.AutoRecommendConflictPolicyEntity;
import com.abtnetworks.totems.auto.vo.AutoRecommendConflictPolicyVo;
import com.abtnetworks.totems.auto.vo.AutoRecommendConflictVo;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface PushAutoRecommendConflictPolicyService {

    /**
     * 批量新增
     * @param vo    批量新增数据
     * @return      ReturnT
     */
    ReturnT batchInsert(List<AutoRecommendConflictPolicyVo> vo);

    /**
     * 分页查询
     * @param vo    查询条件
     * @return      pageInfo
     */
    PageInfo<AutoRecommendConflictPolicyEntity> getBytaskIdAndDeviceUuid(AutoRecommendConflictVo vo);
}