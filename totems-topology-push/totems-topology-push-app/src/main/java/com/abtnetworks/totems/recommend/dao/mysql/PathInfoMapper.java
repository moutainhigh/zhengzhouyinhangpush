package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface PathInfoMapper {
    /**
     * 根据任务id删除路径信息数据
     * @param taskId 任务id
     * @return
     */
    int deleteByTaskId(Integer taskId);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 插入路径信息数据
     * @param record 路径信息数据
     * @return
     */
    int insert(PathInfoEntity record);

    /**
     * 根据任务id获取路径信息数据列表
     * @param taskId 路径信息数据
     * @return
     */
    List<PathInfoEntity> selectByTaskId(Integer taskId);

    /**
     * 根据任务id获取路径信息数据列表
     * @param id 路径信息数据
     * @return
     */
    List<PathInfoEntity> selectById(Integer id);

    /**
     * 根据任务idList获取路径信息数据列表
     * @param cond 路径信息数据
     * @return
     */
    List<PathInfoEntity> selectByIdList(Map<String, Object> cond);

    /**
     * 更新路径信息状态信息
     * @param record 路径信息数据
     * @return
     */
    int updateStatusById(PathInfoEntity record);

    /**
     * 更新路径信息数据
     * @param record 路径信息数据
     * @return
     */
    int update(PathInfoEntity record);

    /**
     * 更新路径状态信息
     * @param params
     * @return
     */
    int updateStatusByPathInfoId(Map<String, String> params);

    int updateStatusByTaskId(Map<String, String> params);

    /**
     * 设置路径enable或者disable
     * @param params
     * @return
     */
    int enablePath(Map<String,String> params);

    int addPathInfoList(List<PathInfoEntity> list);
}