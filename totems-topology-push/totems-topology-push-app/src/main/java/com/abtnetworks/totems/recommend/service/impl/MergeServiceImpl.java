package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.auto.enums.PushNatTypeEnum;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.AutoRecommendFortinetDnatInfoDTO;
import com.abtnetworks.totems.common.dto.AutoRecommendFortinetDnatSpecialDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.PolicyServiceTupleDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedLatchRunnable;
import com.abtnetworks.totems.common.utils.*;


import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.push.service.PushTaskBusinessArithmeticService;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.DeviceDimension;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.CommandSimulationCommonService;
import com.abtnetworks.totems.recommend.service.MergeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.ADD_POLICY;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.UN_OPEN_GENERATE;


@Service
public class MergeServiceImpl implements MergeService {

    private static Logger logger = LoggerFactory.getLogger(MergeServiceImpl.class);

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Autowired
    @Qualifier(value = "mergeExecutor")
    private Executor mergeExecutor;


    @Autowired
    CommandSimulationCommonService commandSimulationCommonService;

    @Autowired
    PushTaskBusinessArithmeticService pushTaskBusinessArithmeticService;

    @Override
    public int loadAndMergePolicy(SimulationTaskDTO task) {
        logger.info(String.format("任务(%d)[%s]的策略...", task.getId(), task.getTheme()));

        List<DeviceDimension> deviceDimensionList = recommendTaskManager.searchDeviceDimensionByTaskId(task.getId());
        Integer listSize = deviceDimensionList.size();
        logger.debug("deviceDimensionList is " + JSONObject.toJSONString(deviceDimensionList));


        List<RecommendPolicyDTO> mergedList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(listSize);
        int index = 0;
        for(DeviceDimension deviceDimension:deviceDimensionList) {
            String id = deviceDimension.getDeviceUuid() + "_" + String.valueOf(task.getId())+ "_" + String.valueOf(index);

            if (ExtendedExecutor.containsKey(id)) {
                logger.warn(String.format("寻找路径任务(%s)已经存在！任务不重复添加", id));
                continue;
            }
            index++;
            mergeExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "", "", new Date()), latch) {
                @Override
                protected void start() throws InterruptedException, Exception {
                    List<RecommendPolicyEntity> policyEntityList = recommendTaskManager.selectByDeviceDimension(deviceDimension, task.getId());
                    logger.debug("合并前策略为：" + JSONObject.toJSONString(policyEntityList));
                    logger.info(String.format("任务(%d)[%s]:设备%s有%d条策略需要合并...", task.getId(), task.getTheme(), deviceDimension.getDeviceUuid(), policyEntityList.size()));
                    for(RecommendPolicyEntity entity : policyEntityList) {
                        int pathInfoId = entity.getPathInfoId();
                        PathInfoEntity pathInfoEntity = recommendTaskManager.getPathInfoByPathInfoId(pathInfoId);
                        if(pathInfoEntity.getEnablePath() == 0) {
                            continue;
                        }

                        RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
                        BeanUtils.copyProperties(entity, policyDTO);

                        String deviceUuid = deviceDimension.getDeviceUuid();
                        NodeEntity nodeEntity = recommendTaskManager.getTheNodeByUuid(deviceUuid);
                        if(nodeEntity != null) {
                            if(nodeEntity.getModelNumber()!= null) {
                                if(nodeEntity.getModelNumber().equals("Cisco ASA")) {
                                    policyDTO.setSrcIp(convertIpRangeToSegment(entity.getSrcIp(),task));
                                    policyDTO.setDstIp(convertIpRangeToSegment(entity.getDstIp(),task));
                                }
                            }
                        }

                        if(!AliStringUtils.isEmpty(entity.getService())) {
                            JSONArray array = JSONArray.parseArray(entity.getService());
                            List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                            policyDTO.setServiceList(serviceList);
                        } else {
                            policyDTO.setServiceList(null);
                        }

                        if(StringUtils.isNotBlank(entity.getMatchPreServices())){
                            JSONArray array = JSONArray.parseArray(entity.getMatchPreServices());
                            List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                            policyDTO.setMatchPreServices(serviceList);
                        }else{
                            policyDTO.setMatchPreServices(null);
                        }

                        if(StringUtils.isNotBlank(entity.getMatchPostServices())){
                            JSONArray array = JSONArray.parseArray(entity.getMatchPostServices());
                            List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                            policyDTO.setMatchPostServices(serviceList);
                        }else{
                            policyDTO.setMatchPostServices(null);
                        }


                        //判断是否为ipv4和ipv6
                        String dstIp = policyDTO.getDstIp();

                        if (StringUtils.isNotBlank(dstIp)) {
                            boolean isDomain = false;
                            String[] split = dstIp.split(PolicyConstants.ADDRESS_SEPERATOR);

                            for (String ip : split) {
                                Boolean ipv4Or6 = IPUtil.isIPV4Or6(ip);
                                if(ipv4Or6 != null && !ipv4Or6){
                                    isDomain = true;
                                    break;
                                }
                            }
                            if (isDomain) {
                                policyDTO.setPolicySource(CommonConstants.POLICY_SOURCE_DST_DOMAIN);
                            }
                        }
                        mergedList.add(policyDTO);
                    }
                }
            });
        }


        try{
            latch.await();
        } catch(Exception e) {
            logger.error("等待异常", e);
        }

        if (CollectionUtils.isNotEmpty(mergedList)) {
            commandSimulationCommonService.addPolicyToTask(task, mergedList);
        }
        if (task.getDevicePolicyMap() != null && task.getDevicePolicyMap().size() > 0) {
            //合并策略

            this.mergePolicy(task);
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int mergePolicy(SimulationTaskDTO task) {
        logger.info(String.format("任务(%d)[%s]开始合并策略...", task.getId(), task.getTheme()));
        logger.debug(String.format("任务(%d)[%s]合并前策略为", task.getId(), task.getTheme()) + JSONObject.toJSONString(task.getDevicePolicyMap()));

        List<RecommendPolicyDTO> mergedList = mergedPolicyMap(task.getDevicePolicyMap());

        logger.debug(String.format("任务(%d)[%s]合并后策略为", task.getId(), task.getTheme()) + JSONObject.toJSONString(mergedList));

        //保存已合并的策略
        saveMergedList(mergedList);

        //将DevicePolicyMap置空减少对象大小
        task.setDevicePolicyMap(null);

        //设置生成命令行策略DTO
        task.setPolicyList(mergedList);
        return ReturnCode.POLICY_MSG_OK;
    }

    private void saveMergedList(List<RecommendPolicyDTO> dtoList) {
        List<RecommendPolicyEntity> entityList = new ArrayList<>();
        for(RecommendPolicyDTO dto : dtoList) {

            RecommendPolicyEntity entity = new RecommendPolicyEntity();

            BeanUtils.copyProperties(dto, entity);
            if(CollectionUtils.isNotEmpty(dto.getServiceList())){

                entity.setService(ServiceDTOUtils.toString(dto.getServiceList()));
            }

            entityList.add(entity);

        }
        recommendTaskManager.addMergedPolicyList(entityList);

    }

    /**
     * 合并策略对象
     * @param map
     * @return
     */
    @Override
    public List<RecommendPolicyDTO> mergedPolicyMap(Map<String, List<RecommendPolicyDTO>> map) {
        List<RecommendPolicyDTO> mergedList = new ArrayList<>();
        if(map == null || map.size() == 0) {
            return mergedList;
        }

        for(String deviceUuid : map.keySet()) {
            List<RecommendPolicyDTO> list = new ArrayList<>();
            //不是同一设备的肯定不能合并，因此不用循环添加
            for(RecommendPolicyDTO policyDTO: map.get(deviceUuid)) {
                addPolicyToMergeList(list, policyDTO);
            }
            mergedList.addAll(list);
        }
        return mergedList;
    }

    /**
     * 合并算法，只能合并一层，合并之后不能再次合并
     * 7中6合并源ip 或目的ip 或服务
     * 相等（也不新增保持原样）或者不满足7中6的规则不能合并
     * @param mergeList
     * @param policy
     */
    private void addPolicyToMergeList(List<RecommendPolicyDTO> mergeList, RecommendPolicyDTO policy) {
        if(mergeList.size() == 0) {
            mergeList.add(policy);
            return;
        }

        boolean canMerge = false;
        for(RecommendPolicyDTO policyDTO: mergeList) {
            // 如果是关联且匹配上了飞塔NAT场景的策略建议，直接跳过，不参与合并
            if(StringUtils.isNotBlank(policyDTO.getMatchType())){
                break;
            }
            if(equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                    && !PolicyConstants.POLICY_DST_IS_DOMAIN.equals(policy.getPolicySource())
                    && !PolicyConstants.POLICY_DST_IS_DOMAIN.equals(policyDTO.getPolicySource())){
                if(notNullAndEquals(policyDTO.getSrcZone(), policy.getSrcZone())
                  && notNullAndEquals(policyDTO.getDstZone(), policy.getDstZone())){
                    // 源域和目的域 都不为空且都相等的时候  合并地址和服务 和接口
                    mergeAddressAndService(policy, policyDTO);

                    String inDevIf = policyDTO.getInDevIf();
                    String outDevIf = policyDTO.getOutDevIf();

                    String newInDevIf = policy.getInDevIf();
                    String newOutDevIf = policy.getOutDevIf();

                    inDevIf = mergeInterface(inDevIf,newInDevIf);
                    outDevIf = mergeInterface(outDevIf,newOutDevIf);
                    policyDTO.setInDevIf(inDevIf);
                    policyDTO.setOutDevIf(outDevIf);
                    canMerge = true;
                    break;
                }

                if(equals(policyDTO.getSrcZone(), policy.getSrcZone())
                        && equals(policyDTO.getDstZone(), policy.getDstZone())
                        && equals(policyDTO.getInDevIf(), policy.getInDevIf())
                        && equals(policyDTO.getOutDevIf(), policy.getOutDevIf())){
                    // 源域和目的域都为空 如果接口都相同则合并地址和服务
                    mergeAddressAndService(policy, policyDTO);
                    canMerge = true;
                    break;
                }
            }
        }

        if(!canMerge) {
            mergeList.add(policy);
        }

    }

    /**
     * 合并地址和服务
     * @param policy
     * @param policyDTO
     */
    private void mergeAddressAndService(RecommendPolicyDTO policy, RecommendPolicyDTO policyDTO) {
        String srcIp = policyDTO.getSrcIp();
        String dstIp = policyDTO.getDstIp();
        List<ServiceDTO> serviceList = policyDTO.getServiceList();

        String newSrcIp = policy.getSrcIp();
        String newDstIp = policy.getDstIp();
        List<ServiceDTO> newServiceList = policy.getServiceList();
        Integer ipType = policy.getIpType();
        srcIp = pushTaskBusinessArithmeticService.combineIp(srcIp, newSrcIp, ipType);
        policyDTO.setSrcIp(srcIp);

        dstIp = pushTaskBusinessArithmeticService.combineIp(dstIp, newDstIp, ipType);
        policyDTO.setDstIp(dstIp);

        serviceList = pushTaskBusinessArithmeticService.mergeServiceList(serviceList, newServiceList);

        policyDTO.setServiceList(serviceList);
    }

    private boolean equals(String srcString, String dstString) {
        if( StringUtils.isBlank(srcString) &&  StringUtils.isNotBlank(dstString)) {
            return false;
        }
        if(StringUtils.isNotBlank(srcString ) && StringUtils.isBlank( dstString )) {
            return false;
        }
        if(StringUtils.isBlank(srcString) && StringUtils.isBlank(dstString)) {
            return true;
        }
        if(!srcString.equals(dstString)) {
            return false;
        }
        return true;
    }

    private boolean notNullAndEquals(String srcString, String dstString) {
        if(StringUtils.isBlank(srcString)){
            return false;
        }
        if(StringUtils.isBlank(dstString)){
            return false;
        }
        if(!srcString.equals(dstString)) {
            return false;
        }
        return true;
    }

    private String mergeInterface(String srcString, String newString) {
        // 去重处理，如果接口重复就不去追加
        if (StringUtils.isNotBlank(srcString) && StringUtils.isNotBlank(newString)
                && srcString.contains(newString)) {
            return srcString;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtils.isNotBlank(srcString) ? PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(srcString) ? "" : srcString : "");
        sb.append(PolicyConstants.ADDRESS_SEPERATOR);
        sb.append(StringUtils.isNotBlank(newString) ? PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(newString) ? "" : newString : "");
        return sb.toString();
    }



    /**
     * 将IP地址中的IP范围转换成IP子网
     * @param ipAddress 原始IP地址字符串，可以包含多个IP地址，逗号分隔
     * @return 转换后ip地址字符串，可以包含多个IP地址，逗号分隔
     */
    private String convertIpRangeToSegment(String ipAddress,SimulationTaskDTO task) {
        List<String> ipList = new ArrayList<>();
        String[] ips = ipAddress.split(",");
        for(String ip:ips) {
            Integer ipType = task.getIpType();
            List<String> ipSegments;
            if(IpTypeEnum.IPV6.getCode().equals(ipType)){
                ipSegments = IP6Utils.convertRangeToSubnet(ip);
            }else{
                ipSegments = IPUtil.convertRangeToSubnet(ip);
            }

            ipList.addAll(ipSegments);
        }
        StringBuilder sb= new StringBuilder();
        for(String ip:ipList) {
            sb.append(",");
            sb.append(ip);
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * 合并策略对象
     * @param map
     * @return
     */
    @Override
    public List<RecommendPolicyDTO> accurateMergedPolicyMapForFortinetNat(Map<String, List<RecommendPolicyDTO>> map, String fortinetNatType) {
        List<RecommendPolicyDTO> mergedList = new ArrayList<>();
        if(map == null || map.size() == 0) {
            return mergedList;
        }

        for(String deviceUuid : map.keySet()) {

            //不是同一设备的肯定不能合并，因此不用循环添加
            List<RecommendPolicyDTO> recommendPolicyDTOS = map.get(deviceUuid);
            List<RecommendPolicyDTO> list = this.recursionMergeForFortinetNat(recommendPolicyDTOS, fortinetNatType);
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)){

                //由于这种外围合并的框架，不能递归合并时减去自己重复的，所以去重
                mergedList.addAll(list);

            }

        }
        return mergedList;
    }

    /**
     * 飞塔 合并算法
     * @param recommendPolicyDTOS
     */
    private List<RecommendPolicyDTO> recursionMergeForFortinetNat(List<RecommendPolicyDTO> recommendPolicyDTOS, String fortinetNatType){
        List<RecommendPolicyDTO> list = new ArrayList<>();
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(recommendPolicyDTOS)){
            AtomicInteger countMerge = new AtomicInteger(0);
            for(RecommendPolicyDTO policyDTO: recommendPolicyDTOS) {
                this.checkServiceBeyond2ProtocolAny(policyDTO);
                if(StringUtils.equalsAnyIgnoreCase(PushNatTypeEnum.NAT_TYPE_S.getCode() ,fortinetNatType)){
                    boolean isMerge = this.addPolicyToMergeListForFortinetSNat(list, policyDTO);
                    if(isMerge){
                        countMerge.addAndGet(1);
                    }
                } else {
                    boolean isMerge = this.addPolicyToMergeListForFortinetDNat(list, policyDTO);
                    if(isMerge){
                        countMerge.addAndGet(1);
                    }
                }
            }
            //如果不可合并 list 和 recommendPolicyDTOS 应该相等
            // 如果可合并 ，list 中的一个对象会将新的合并元素放入该对象中，list大小不会加1

            if(countMerge.get()<1){
                return list;
            }else{
                return this.recursionMergeForFortinetNat(list, fortinetNatType);
            }

        }
        return list;
    }

    /**
     * 只能合并一层，合并之后不能再次合并
     * 7中6合并源ip 或目的ip 或服务
     *  相等（也不新增保持原样）或者不满足7中6的规则不能合并
     *
     *  飞塔NAT合并算法，
     *
     *
     * @param mergeList
     * @param policy
     * @return
     */
    private boolean addPolicyToMergeListForFortinetSNat(List<RecommendPolicyDTO> mergeList, RecommendPolicyDTO policy) {
        if(mergeList.size() == 0) {
            mergeList.add(policy);
            return false;
        }
        boolean isMySelf = false;
        boolean canMerge = false;
        for(RecommendPolicyDTO policyDTO: mergeList) {

            if(equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                    && equals(policyDTO.getSrcZone(), policy.getSrcZone())
                    && equals(policyDTO.getDstZone(), policy.getDstZone())
                    && equals(policyDTO.getInDevIf(), policy.getInDevIf())
                    && equals(policyDTO.getOutDevIf(), policy.getOutDevIf())
            ) {
                // 如果是源nat合并
                if(StringUtils.isNotBlank(policyDTO.getPostSrcIp()) || StringUtils.isNotBlank(policy.getPostSrcIp())){
                    logger.info("飞塔源nat合并只合并转换后源IP为空的策略建议，当前:{}, {}不符合，跳过合并", policyDTO.getPostSrcIp(), policy.getPostSrcIp());
                    continue;
                }
                PolicyInfoDTO policyPushVO = new PolicyInfoDTO();
                BeanUtils.copyProperties(policyDTO,policyPushVO);
                PolicyInfoDTO policyVO = new PolicyInfoDTO();
                BeanUtils.copyProperties(policy,policyVO);
                PolicyMergePropertyEnum propertyEnum = pushTaskBusinessArithmeticService.mergeResult(policyPushVO,policyVO);
                // 这里判断如果同一个设备走了多条路，需要创建多条策略。如果acl先匹配的是默认策略 后匹配的非默认策略，则以非默认策略匹配为准(非默认策略没有matchRuleId)
                if (StringUtils.isBlank(policyDTO.getMatchRuleId()) && StringUtils.isNotBlank(policy.getMatchRuleId()) ){
                    policyDTO.setMatchRuleId(policy.getMatchRuleId());
                }
                if( propertyEnum.equals(UN_OPEN_GENERATE)){
                    //证明三个都相等不能合也不能加入新的list中
                    isMySelf = true;
                    continue;
                }
                if(!(propertyEnum.equals(ADD_POLICY) || propertyEnum.equals(UN_OPEN_GENERATE))){
                    canMerge = true;
                    policyDTO.setSrcIp(policyPushVO.getSrcIp());
                    policyDTO.setDstIp(policyPushVO.getDstIp());
                    policyDTO.setServiceList(policyPushVO.getServiceList());
                    break;
                }

            }
        }
        if(!canMerge && !isMySelf ) {
            mergeList.add(policy);
            return false;
        }else{
            return canMerge;
        }

    }


    /**
     * 判断转换后源IP是否是IP或子网或范围或为空
     * 以上情况不走源Nat合并逻辑
     * @param postSrcIp
     * @return
     */
    private boolean isIPOrSegmentOrRangeOrNull(String postSrcIp){
        if(StringUtils.isBlank(postSrcIp)){
            return true;
        }
        if(IpUtils.isIP(postSrcIp)){
            return true;
        }
        if(IpUtils.isIPSegment(postSrcIp)){
            return true;
        }
        if(IpUtils.isIPRange(postSrcIp)){
            return true;
        }
        return false;
    }

    /**
     * 只能合并一层，合并之后不能再次合并
     * 7中6合并源ip 或目的ip 或服务
     *  相等（也不新增保持原样）或者不满足7中6的规则不能合并
     *
     *  飞塔目的NAT合并算法，
     *
     *
     * @param mergeList
     * @param policy
     * @return
     */
    private boolean addPolicyToMergeListForFortinetDNat(List<RecommendPolicyDTO> mergeList, RecommendPolicyDTO policy) {
        if(mergeList.size() == 0) {
            mergeList.add(policy);
            return false;
        }
        boolean isMySelf = false;
        boolean canMerge = false;
        for(RecommendPolicyDTO policyDTO: mergeList) {

            if(equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                    && equals(policyDTO.getSrcZone(), policy.getSrcZone())
                    && equals(policyDTO.getDstZone(), policy.getDstZone())
                    && equals(policyDTO.getInDevIf(), policy.getInDevIf())
                    && equals(policyDTO.getOutDevIf(), policy.getOutDevIf())
            ) {
                // 飞塔目的nat合并
                if(!(equals(policyDTO.getPreDstIp(), policy.getPreDstIp()) && equals(policyDTO.getPostDstIp(), policy.getPostDstIp()))){
                    logger.info("飞塔目的nat可合并转换前后目的IP相同的策略建议，当前转换前：{}， {}，转换后：{}， {}不符合，跳过合并", policyDTO.getPreDstIp(), policy.getPreDstIp(), policyDTO.getPostDstIp(), policy.getPostDstIp());
                    continue;
                }
                PolicyInfoDTO policyPushVO = new PolicyInfoDTO();
                BeanUtils.copyProperties(policyDTO,policyPushVO);
                PolicyInfoDTO policyVO = new PolicyInfoDTO();
                BeanUtils.copyProperties(policy,policyVO);
                PolicyMergePropertyEnum propertyEnum = pushTaskBusinessArithmeticService.mergeResult(policyPushVO,policyVO);
                // 这里判断如果同一个设备走了多条路，需要创建多条策略。如果acl先匹配的是默认策略 后匹配的非默认策略，则以非默认策略匹配为准(非默认策略没有matchRuleId)
                if (StringUtils.isBlank(policyDTO.getMatchRuleId()) && StringUtils.isNotBlank(policy.getMatchRuleId()) ){
                    policyDTO.setMatchRuleId(policy.getMatchRuleId());
                }
                if( propertyEnum.equals(UN_OPEN_GENERATE)){
                    //证明三个都相等不能合也不能加入新的list中
                    isMySelf = true;
                    continue;
                }
                if(!(propertyEnum.equals(ADD_POLICY) || propertyEnum.equals(UN_OPEN_GENERATE))){
                    canMerge = true;
                    policyDTO.setSrcIp(policyPushVO.getSrcIp());
                    policyDTO.setDstIp(policyPushVO.getDstIp());
                    policyDTO.setServiceList(policyPushVO.getServiceList());
                    break;
                }

            }
        }
        if(!canMerge && !isMySelf ) {
            mergeList.add(policy);
            return false;
        }else{
            return canMerge;
        }

    }

    /**
     * 合并策略对象
     * @param map
     * @return
     */
    @Override
    public List<RecommendPolicyDTO> accurateMergedPolicyMapForFortinetVIP(Map<String, List<RecommendPolicyDTO>> map) {
        List<RecommendPolicyDTO> mergedList = new ArrayList<>();
        if(map == null || map.size() == 0) {
            return mergedList;
        }

        for(String deviceUuid : map.keySet()) {

            //不是同一设备的肯定不能合并，因此不用循环添加
            List<RecommendPolicyDTO> recommendPolicyDTOS = map.get(deviceUuid);
            List<RecommendPolicyDTO> list = this.recursionMergeForFortinetVIP(recommendPolicyDTOS);
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)){

                //由于这种外围合并的框架，不能递归合并时减去自己重复的，所以去重
                mergedList.addAll(list);

            }

        }
        return mergedList;
    }

    /**
     * 飞塔VIP 合并算法
     * @param recommendPolicyDTOS
     */
    private List<RecommendPolicyDTO> recursionMergeForFortinetVIP(List<RecommendPolicyDTO> recommendPolicyDTOS){
        List<RecommendPolicyDTO> list = new ArrayList<>();
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(recommendPolicyDTOS)){
            AtomicInteger countMerge = new AtomicInteger(0);
            for(RecommendPolicyDTO policyDTO: recommendPolicyDTOS) {
                this.checkServiceBeyond2ProtocolAny(policyDTO);
                boolean isMerge = this.addPolicyToMergeListForFortinetVIP(list, policyDTO);
                if(isMerge){
                    countMerge.addAndGet(1);
                }
            }
            //如果不可合并 list 和 recommendPolicyDTOS 应该相等
            // 如果可合并 ，list 中的一个对象会将新的合并元素放入该对象中，list大小不会加1

            if(countMerge.get()<1){
                return list;
            }else{
                return this.recursionMergeForFortinetVIP(list);
            }

        }
        return list;
    }

    /**
     *  飞塔VIP合并算法，五元组相同时，VIP不同，则合并VIP
     *
     * 只能合并一层，合并之后不能再次合并
     * 7中6合并源ip 或目的ip 或服务
     *  相等（也不新增保持原样）或者不满足7中6的规则不能合并
     *
     *
     *
     * @param mergeList
     * @param policy
     * @return
     */
    private boolean addPolicyToMergeListForFortinetVIP(List<RecommendPolicyDTO> mergeList, RecommendPolicyDTO policy) {
        if(mergeList.size() == 0) {
            mergeList.add(policy);
            return false;
        }
        boolean isMySelf = false;
        boolean canMerge = false;
        for(RecommendPolicyDTO policyDTO: mergeList) {

            if(equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                    && equals(policyDTO.getSrcZone(), policy.getSrcZone())
                    && equals(policyDTO.getDstZone(), policy.getDstZone())
                    && equals(policyDTO.getInDevIf(), policy.getInDevIf())
                    && equals(policyDTO.getOutDevIf(), policy.getOutDevIf())
                    && equalIpCollection(policyDTO.getSrcIp(),policy.getSrcIp())
                    && equalServiceCollection(policyDTO.getServiceList(),policy.getServiceList())
            ) {
                canMerge = true;

                // 如果可以合并，则处理飞塔VIP数据
                AutoRecommendFortinetDnatSpecialDTO policyDTODnatSpecialDTO = policyDTO.getFortinetDnatSpecialDTO();
                AutoRecommendFortinetDnatSpecialDTO policyDnatSpecialDTO = policy.getFortinetDnatSpecialDTO();
                if(CollectionUtils.isNotEmpty(policyDTODnatSpecialDTO.getExistVipList())){
                    // 如果一条策略建议的已存在VIP
                    List<AutoRecommendFortinetDnatInfoDTO> policyDTOExistVipList = policyDTODnatSpecialDTO.getExistVipList();
                    if(CollectionUtils.isNotEmpty(policyDnatSpecialDTO.getExistVipList())){
                        policyDTOExistVipList.addAll(policyDnatSpecialDTO.getExistVipList());
                        policyDTODnatSpecialDTO.setExistVipList(policyDTOExistVipList);
                    } else {
                        List<AutoRecommendFortinetDnatInfoDTO> policyDTORestVipList = policyDTODnatSpecialDTO.getRestVipList();
                        policyDTORestVipList.addAll(policyDnatSpecialDTO.getRestVipList());
                        policyDTODnatSpecialDTO.setRestVipList(policyDTORestVipList);
                    }
                } else {
                    // 如果是需要新建VIP
                    List<AutoRecommendFortinetDnatInfoDTO> policyDTORestVipList = policyDTODnatSpecialDTO.getRestVipList();
                    if(CollectionUtils.isNotEmpty(policyDnatSpecialDTO.getRestVipList())){
                        policyDTORestVipList.addAll(policyDnatSpecialDTO.getRestVipList());
                        policyDTODnatSpecialDTO.setRestVipList(policyDTORestVipList);
                    } else {
                        List<AutoRecommendFortinetDnatInfoDTO> policyDTOExistVipList = policyDTODnatSpecialDTO.getExistVipList();
                        policyDTOExistVipList.addAll(policyDnatSpecialDTO.getExistVipList());
                        policyDTODnatSpecialDTO.setExistVipList(policyDTOExistVipList);
                    }
                }
                policyDTO.setFortinetDnatSpecialDTO(policyDTODnatSpecialDTO);
                break;
            }
        }
        if(!canMerge && !isMySelf ) {
            mergeList.add(policy);
            return false;
        }else{
            return canMerge;
        }
    }

    /**
     * 比较源地址目的地址是否完全一致
     * @param srcOrDstIp
     * @param srcOrDstIp1
     * @return
     */
    @Override
    public boolean equalIpCollection(String srcOrDstIp,String srcOrDstIp1){
        boolean equalSrcOrDstIpCollection = false;
        if(!StringUtils.isAllBlank(srcOrDstIp,srcOrDstIp1)){
            List<String> srcIps = Arrays.asList(srcOrDstIp.split(","));
            List<String> srcIp1s = Arrays.asList(srcOrDstIp1.split(","));
            Collections.sort(srcIps);
            Collections.sort(srcIp1s);
            equalSrcOrDstIpCollection = CollectionUtils.isEqualCollection(srcIps, srcIp1s);
        }else if (StringUtils.isAllBlank(srcOrDstIp,srcOrDstIp1)){
            return true;
        }
        return equalSrcOrDstIpCollection;
    }

    /**
     * 比较服务是否可以合并
     * @param serviceList
     * @param serviceList1
     * @return
     */
    @Override
    public boolean equalServiceCollection(List<ServiceDTO> serviceList,List<ServiceDTO> serviceList1){


        if(CollectionUtils.isNotEmpty(serviceList) && CollectionUtils.isNotEmpty(serviceList1)){
            Set<PolicyServiceTupleDTO> editServiceTupleSet = pushTaskBusinessArithmeticService.setServiceFormatter(serviceList);
            Set<PolicyServiceTupleDTO> editServiceTupleSet1 = pushTaskBusinessArithmeticService.setServiceFormatter(serviceList1);
            boolean equalCollection = CollectionUtils.isEqualCollection(editServiceTupleSet, editServiceTupleSet1);
            return equalCollection;
        }else if(CollectionUtils.isEmpty(serviceList) && CollectionUtils.isEmpty(serviceList1)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 是否超过了支持的协议范围，超过了就默认是协议为0
     * @param policyDTO
     */
    public void checkServiceBeyond2ProtocolAny(RecommendPolicyDTO policyDTO){
        if(policyDTO.getServiceList() != null) {
            List<ServiceDTO> serviceDTOList = new LinkedList<>();
            for (ServiceDTO serviceDTO :policyDTO.getServiceList()) {
                String protocol = serviceDTO.getProtocol();
                if("0".equals(protocol) ){
                    serviceDTOList.clear();
                    serviceDTOList.add(serviceDTO);
                    break;
                }
                boolean  isBeyond = ProtocolUtils.isBeyondProtocol(protocol);
                if(isBeyond ){
                    serviceDTOList.clear();
                    serviceDTO.setProtocol("0");
                    serviceDTOList.add(serviceDTO);
                    break;

                }else{
                    serviceDTOList.add(serviceDTO);
                }
            }
            policyDTO.setServiceList(serviceDTOList);
        } else {

        }
    }

    private String mergeZone(String srcString, String newString) {
        // 去重处理，如果接口重复就不去追加
        if (StringUtils.isNotBlank(srcString) && StringUtils.isNotBlank(newString)
                && srcString.contains(newString)) {
            return srcString;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtils.isNotBlank(srcString) ? PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(srcString) ? "" : srcString : "");
        sb.append(PolicyConstants.ADDRESS_SEPERATOR);
        sb.append(StringUtils.isNotBlank(newString) ? PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(newString) ? "" : newString : "");
        return sb.toString();
    }

    /**
     * 自动开通合并算法，只能合并一层，合并之后不能再次合并
     * 7中6合并源ip 或目的ip 或服务
     * 相等（也不新增保持原样）或者不满足7中6的规则不能合并
     * @param mergeList
     * @param policy
     */
    private boolean addPolicyToMergeListForAuto(List<RecommendPolicyDTO> mergeList, RecommendPolicyDTO policy) {
        if(mergeList.size() == 0) {
            mergeList.add(policy);
            return false;
        }
        boolean isMySelf = false;
        boolean canMerge = false;
        for(RecommendPolicyDTO policyDTO: mergeList) {
            NodeEntity nodeEntity = policyDTO.getNode();
            if (null == nodeEntity) {
                continue;
            }
            PolicyInfoDTO policyPushVO = new PolicyInfoDTO();
            BeanUtils.copyProperties(policyDTO,policyPushVO);
            PolicyInfoDTO policyVO = new PolicyInfoDTO();
            BeanUtils.copyProperties(policy,policyVO);
            // 如果是华三设备，先判断是否满足，源ip和目的ip，服务，目的域都相等 则合并源域;源ip和目的ip，服务，源域都相等 则合并目的;若都不满足则还是走之前的逻辑
            if (DeviceModelNumberEnum.H3CV7.getKey().equalsIgnoreCase(nodeEntity.getModelNumber())) {
                if (equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                        && equalIpCollection(policyDTO.getSrcIp(), policy.getSrcIp())
                        && equalIpCollection(policyDTO.getDstIp(), policy.getDstIp())
                        && equalServiceCollection(policyPushVO.getServiceList(), policyVO.getServiceList())
                        && equals(policyDTO.getDstZone(), policy.getDstZone())) {
                    String srcZonePolicyDTO = policyDTO.getSrcZone();
                    String srcZonePolicy = policy.getSrcZone();

                    srcZonePolicyDTO = mergeZone(srcZonePolicyDTO, srcZonePolicy);
                    policyDTO.setSrcZone(srcZonePolicyDTO);
                    canMerge = true;
                    break;
                } else if (equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                        && equalIpCollection(policyDTO.getSrcIp(), policy.getSrcIp())
                        && equalIpCollection(policyDTO.getDstIp(), policy.getDstIp())
                        && equalServiceCollection(policyPushVO.getServiceList(), policyVO.getServiceList())
                        && equals(policyDTO.getSrcZone(), policy.getSrcZone())) {
                    String dstZonePolicyDTO = policyDTO.getDstZone();
                    String dstZonePolicy = policy.getDstZone();
                    dstZonePolicyDTO = mergeZone(dstZonePolicyDTO, dstZonePolicy);
                    policyDTO.setDstZone(dstZonePolicyDTO);
                    canMerge = true;
                    break;
                } else {
                    // 之前逻辑判断源域目的域是否一样，如果一样再去判断是去合源ip还是目的ip还是服务
                    PolicyMergePropertyEnum propertyEnum = mergePolicyByCondition(policy, policyDTO, policyPushVO, policyVO);
                    if(null == propertyEnum){
                        continue;
                    }
                    if (propertyEnum.equals(UN_OPEN_GENERATE)) {
                        //证明三个都相等不能合也不能加入新的list中
                        isMySelf = true;
                        continue;
                    }
                    if (!(propertyEnum.equals(ADD_POLICY) || propertyEnum.equals(UN_OPEN_GENERATE))) {
                        canMerge = true;
                        policyDTO.setSrcIp(policyPushVO.getSrcIp());
                        policyDTO.setDstIp(policyPushVO.getDstIp());
                        policyDTO.setServiceList(policyPushVO.getServiceList());
                        break;
                    }
                }
            } else {
                // 之前逻辑判断源域目的域是否一样，如果一样再去判断是去合源ip还是目的ip还是服务
                PolicyMergePropertyEnum propertyEnum = mergePolicyByCondition(policy, policyDTO, policyPushVO, policyVO);
                if(null == propertyEnum){
                    continue;
                }
                if (propertyEnum.equals(UN_OPEN_GENERATE)) {
                    //证明三个都相等不能合也不能加入新的list中
                    isMySelf = true;
                    continue;
                }
                if (!(propertyEnum.equals(ADD_POLICY) || propertyEnum.equals(UN_OPEN_GENERATE))) {
                    canMerge = true;
                    policyDTO.setSrcIp(policyPushVO.getSrcIp());
                    policyDTO.setDstIp(policyPushVO.getDstIp());
                    policyDTO.setServiceList(policyPushVO.getServiceList());
                    break;
                }
            }
        }

        if(!canMerge && !isMySelf ) {
            mergeList.add(policy);
            return false;
        }else{
            return canMerge;
        }

    }

    /**
     *
     * @param policy
     * @param policyDTO
     * @param policyPushVO
     * @param policyVO
     * @return
     */
    private PolicyMergePropertyEnum mergePolicyByCondition(RecommendPolicyDTO policy, RecommendPolicyDTO policyDTO, PolicyInfoDTO policyPushVO, PolicyInfoDTO policyVO) {
        PolicyMergePropertyEnum propertyEnum = null;
        if(equals(policyDTO.getDeviceUuid(), policy.getDeviceUuid())
                && equals(policyDTO.getSrcZone(), policy.getSrcZone())
                && equals(policyDTO.getDstZone(), policy.getDstZone())) {
            propertyEnum = pushTaskBusinessArithmeticService.mergeResult(policyPushVO,policyVO);
            // 这里判断如果同一个设备走了多条路，需要创建多条策略。如果acl先匹配的是默认策略 后匹配的非默认策略，则以非默认策略匹配为准(非默认策略没有matchRuleId)
            if (StringUtils.isBlank(policyDTO.getMatchRuleId()) && StringUtils.isNotBlank(policy.getMatchRuleId()) ){
                policyDTO.setMatchRuleId(policy.getMatchRuleId());
            }
        }
        return propertyEnum;
    }

    /**
     * 递归合并算法
     * @param recommendPolicyDTOS
     */
    private List<RecommendPolicyDTO> recursionMerge(List<RecommendPolicyDTO> recommendPolicyDTOS){
        List<RecommendPolicyDTO> list = new ArrayList<>();
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(recommendPolicyDTOS)){
            AtomicInteger countMerge = new AtomicInteger(0);
            for(RecommendPolicyDTO policyDTO: recommendPolicyDTOS) {
                this.checkServiceBeyond2ProtocolAny(policyDTO);
                boolean isMerge = this.addPolicyToMergeListForAuto(list, policyDTO);
                if(isMerge){
                    countMerge.addAndGet(1);
                }
            }
            //如果不可合并 list 和 recommendPolicyDTOS 应该相等
            // 如果可合并 ，list 中的一个对象会将新的合并元素放入该对象中，list大小不会加1

            if(countMerge.get()<1){
                return list;
            }else{
                return this.recursionMerge(list);
            }

        }
        return list;
    }

    /**
     * 合并策略对象
     * @param map
     * @return
     */
    @Override
    public List<RecommendPolicyDTO> accurateMergedPolicyMap(Map<String, List<RecommendPolicyDTO>> map) {
        List<RecommendPolicyDTO> mergedList = new ArrayList<>();
        if(map == null || map.size() == 0) {
            return mergedList;
        }

        for(String deviceUuid : map.keySet()) {

            //不是同一设备的肯定不能合并，因此不用循环添加
            List<RecommendPolicyDTO> recommendPolicyDTOS = map.get(deviceUuid);
            List<RecommendPolicyDTO> list = this.recursionMerge(recommendPolicyDTOS);
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)){

                //由于这种外围合并的框架，不能递归合并时减去自己重复的，所以去重
                mergedList.addAll(list);

            }

        }
        return mergedList;
    }

}
