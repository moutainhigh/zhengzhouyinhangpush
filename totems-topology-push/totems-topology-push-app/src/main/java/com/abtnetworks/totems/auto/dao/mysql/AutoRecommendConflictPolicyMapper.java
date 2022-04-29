package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.AutoRecommendConflictPolicyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AutoRecommendConflictPolicyMapper {
    /**
     * 根据taskId和deviceUuid查询数据
     * @param taskId        任务id
     * @param deviceUuid    设备uuid
     * @return              List
     */
    List<AutoRecommendConflictPolicyEntity> queryTaskIdAndUuid(@Param("taskId") Integer taskId, @Param("deviceUuid") String deviceUuid, @Param("policyType") String policyType);

    /**
     * 批量新增
     * @param conflictPolicyList    添加的数据
     * @return                      是否成功
     */
    int batchInsert(@Param("conflictPolicyList") List<AutoRecommendConflictPolicyEntity> conflictPolicyList);
}