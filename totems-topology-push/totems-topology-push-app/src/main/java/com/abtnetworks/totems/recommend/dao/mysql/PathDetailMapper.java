package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.PathDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface PathDetailMapper {
    /**
     * 根据路径信息id删除路径详情数据
     * @param pathInfoId 路径信息id
     * @return
     */
    int deleteByPathInfoId(Integer pathInfoId);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 插入路径详情数据
     * @param record 路径详情数据
     * @return
     */
    int insert(PathDetailEntity record);

    /**
     * 根据路径信息id获取路径详情数据
     * @param pathInfoId 路径信息Id
     * @return
     */
    List<PathDetailEntity> selectByPathInfoId(Integer pathInfoId);

    /**
     * 更新验证路径详情
     * @param params 路径验证详情参数Map
     * @return
     */
    int updateVerifyPath(Map<String, String> params);

    /**
     * 更新路径详情数据
     * @param record 路径详情数据
     * @return
     */
    int update(PathDetailEntity record);
}