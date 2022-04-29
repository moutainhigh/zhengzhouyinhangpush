package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.PathDeviceDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface PathDeviceDetailMapper {
    /**
     * 根据路径信息id删除设备路径详情数据
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
     * 插入路径设备详情数据
     * @param record
     * @return
     */
    int insert(PathDeviceDetailEntity record);

    /**
     * 更新路径设备详情数据
     * @param record
     * @return
     */
    int update(PathDeviceDetailEntity record);

    /**
     * 获取路径设备详情数据
     * @param params 查询参数
     * @return
     */
    List<PathDeviceDetailEntity> selectPathDeviceDetail(Map<String, String> params);

    /**
     * 获取路径分析路径设备详情信息
     * @param params 查询参数
     * @return
     */
    List<PathDeviceDetailEntity> selectAnalyzePathDeviceDetail(Map<String, String> params);

    /**
     * 获取路经验证设备详情信息
     * @param params 查询参数
     * @return
     */
    List<PathDeviceDetailEntity> selectVerifyPathDeviceDetail(Map<String, String> params);

    /**
     * 批量插入设备详情列表
     * @param list 设备详情列表
     * @return 影响行数
     */
    int insertList(List<PathDeviceDetailEntity> list);
}