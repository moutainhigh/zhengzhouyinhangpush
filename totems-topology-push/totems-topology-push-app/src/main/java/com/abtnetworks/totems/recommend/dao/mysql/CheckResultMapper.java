package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.CheckResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface CheckResultMapper {

    /**
     * 根据策略id删除策略检查结果
     * @param policyId 策略id
     * @return
     */
    int deleteByPolicyId(Integer policyId);

    /**
     * 根据路径id删除策略检查结果
     * @param pathInfoId 策略id
     * @return
     */
    int deleteByPathInfoIdIds(@Param("pathInfoId") Integer pathInfoId);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 插入策略检查结果
     * @param record 策略检查结果数据
     * @return
     */
    int insert(CheckResultEntity record);

    /**
     * 根据策略id查询策略检查结果
     * @param policyId 策略id
     * @return 策略检查结果
     */
    List<CheckResultEntity> selectByPolicyId(Integer policyId);
}