package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/6/29
 */
public interface CommandSimulationCommonService {

    /**
     * 生成任务命令行
     * @param task
     * @return
     */
    int getPushCommandTextByTask(SimulationTaskDTO task, UserInfoDTO userInfoDTO);


    /**
     * 重新生成命令行
     * @param task
     * @param userInfoDTO
     * @return
     */
    int reGenerateCommandLine(SimulationTaskDTO task, UserInfoDTO userInfoDTO);

    /**
     * 添加策略到任务列表中
     * @param task
     * @param policyList
     */
    void addPolicyToTask(SimulationTaskDTO task, List<RecommendPolicyDTO> policyList);

    /**
     * 对大网段开通进行替换
     * @param task
     * @param policyList
     * @param map
     */
    void bigNetRangeReplace(SimulationTaskDTO task, List<RecommendPolicyDTO> policyList, Map<String, List<RecommendPolicyDTO>> map);

    /**
     * 域名解析
     * @param task
     *
     * @return
     */
    Boolean convertDomainToIp (SimulationTaskDTO task,List<String> dscIpList, Map<String,String> domainMap,StringBuilder isConvertFail);
}
