package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;

import java.util.List;
import java.util.Map;

public interface MergeService {

    int loadAndMergePolicy(SimulationTaskDTO task);

    int mergePolicy(SimulationTaskDTO task);




    /**
     * 策略合并，七中六
     * @param map
     * @return
     */
    List<RecommendPolicyDTO> mergedPolicyMap(Map<String, List<RecommendPolicyDTO>> map);

    /**
     * 精确合并 - 策略合并，七中六
     * @param map
     * @return
     */
    List<RecommendPolicyDTO> accurateMergedPolicyMap(Map<String, List<RecommendPolicyDTO>> map);

    /**
     * 飞塔NAT - 策略合并，七中六,源nat当转换后的源IP为空或出接口时（转换后为IP时不合并）
     * @param map
     * @param fortinetNatType
     * @return
     */
    List<RecommendPolicyDTO> accurateMergedPolicyMapForFortinetNat(Map<String, List<RecommendPolicyDTO>> map, String fortinetNatType);

    /**
     * 飞塔NAT - 策略合并，五元组相同时合并VIP
     * @param map
     * @return
     */
    List<RecommendPolicyDTO> accurateMergedPolicyMapForFortinetVIP(Map<String, List<RecommendPolicyDTO>> map);

    /**
     * 比较源地址目的地址是否完全一致
     * @param srcOrDstIp
     * @param srcOrDstIp1
     * @return
     */
    boolean equalIpCollection(String srcOrDstIp,String srcOrDstIp1);

    /**
     * 比较服务是否可以合并
     * @param serviceList
     * @param serviceList1
     * @return
     */
    boolean equalServiceCollection(List<ServiceDTO> serviceList, List<ServiceDTO> serviceList1);
}
