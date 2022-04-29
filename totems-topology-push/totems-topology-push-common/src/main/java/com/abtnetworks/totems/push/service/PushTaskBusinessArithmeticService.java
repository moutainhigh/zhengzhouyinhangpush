package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.PolicyServiceTupleDTO;
import com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum;
import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;

import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @Title:
 * @Description: 生成时需要的业务算法类
 * @date 2021/4/27
 */
public interface PushTaskBusinessArithmeticService {


    /**
     * 同工单多五元组合并算法
     * @param policyInfoList
     * @return
     */
    List<PolicyInfoDTO> sameTaskMergeQuintuple(List<PolicyInfoDTO> policyInfoList);

    /**
     * 服务转化
     * @param serviceList
     * @return
     */
    Set<PolicyServiceTupleDTO> setServiceFormatter(List<ServiceDTO> serviceList);

    /**
     * 合并比较结果是合了还是没合
     * @param policyPushVO
     * @param policyPushVO1
     * @return
     */
    PolicyMergePropertyEnum mergeResult(PolicyInfoDTO policyPushVO, PolicyInfoDTO policyPushVO1);


    /**
     * 合并ip
     * @param ipListString
     * @param newIpListString
     * @return
     */
    String combineIp(String ipListString, String newIpListString,Integer taskType);



    /**
     * 合并服务
     * @param serviceList
     * @param newServiceList
     * @return
     */
    List<ServiceDTO> mergeServiceList(List<ServiceDTO> serviceList, List<ServiceDTO> newServiceList);
}
