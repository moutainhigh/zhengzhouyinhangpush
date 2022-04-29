package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.dto.AutoRecommendTaskNatInfoDTO;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.vo.*;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @desc    自动开通工单接口
 * @author liuchanghao
 * @date 2021-06-09 14:03
 */
public interface PushAutoRecommendService {

    /**
     * 添加自动开通工单任务
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT<Integer> addTask(AutoRecommendTaskVO vo) throws Exception;

    /**
     * 远程调用获取路由表
     * @param deviceUuid
     * @param policyListUuid
     * @param content
     * @return
     */
    ReturnT<List<String>> remoteGetRoutTableRuleList(String deviceUuid, String policyListUuid, String content) throws Exception;

    /**
     * 内网互访类型下-远程调用获取路由表
     * @param deviceUuid
     * @param policyListUuid
     * @param anyIpFindZoneInfo
     * @return
     */
    ReturnT<List<String>> remoteGetRoutTableRuleListByInside2Inside(String deviceUuid, String policyListUuid, String anyIpFindZoneInfo) throws Exception;

    /**
     * 添加自动开通工单任务
     * @param vo
     * @return
     * @throws Exception
     */
    ReturnT delete(AutoRecommendTaskVO vo) throws Exception;

    /**
     * 分页查询
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<AutoRecommendTaskEntity> findList(AutoRecommendTaskSearchVO vo, int pageNum, int pageSize);

    /**
     * 根据任务id获取nat信息
     * @param getInfoVO
     * @return
     */
    AutoRecommendTaskNatInfoDTO getNatInfo(AutoRecommendTaskGetInfoVO getInfoVO);

    /**
     * 自动任务下发
     * @param vo
     * @return
     */
    ReturnT autoPush(AutoRecommendTaskVO vo);


    /**
     * 获取报告页详情
     * @param vo
     * @return
     */
    ReturnT getResult(AutoRecommendTaskVO vo);

    /**
     * 获取报告页详情
     * @param conflictVo
     * @return
     */
    ReturnT getByDevice(AutoRecommendConflictVo conflictVo);

    /**
     * 获取自动开通工单状态及策略日志信息
     * @param conflictVo
     * @return
     */
    ReturnT getStatusInfo(AutoRecommendConflictVo conflictVo);

    /**
     * 查询
     * @param vo    查询条件
     * @return      数据集合
     */
    List<AutoRecommendTaskEntity> selectList(AutoRecommendTaskSearchVO vo);
}
