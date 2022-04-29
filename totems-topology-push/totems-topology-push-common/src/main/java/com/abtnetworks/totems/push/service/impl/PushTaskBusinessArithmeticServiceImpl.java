package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.PolicyServiceTupleDTO;
import com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum;
import com.abtnetworks.totems.common.utils.MergeIpServiceUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.push.service.PushTaskBusinessArithmeticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.abtnetworks.totems.common.constants.CommonConstants.POLICY_SOURCE_DST_DOMAIN;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.ADD_POLICY;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.MERGE_DST_IP;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.MERGE_SERVICE;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.MERGE_SRC_IP;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.UN_OPEN_GENERATE;


/**
 * @author Administrator
 * @Title:
 * @Description: 工单业务算法层
 * @date 2021/4/27
 */
@Slf4j
@Service
public class PushTaskBusinessArithmeticServiceImpl implements PushTaskBusinessArithmeticService {






    @Override
    public List<PolicyInfoDTO> sameTaskMergeQuintuple(List<PolicyInfoDTO> quintetList) {
        List<PolicyInfoDTO> afterMergePolicyList = new ArrayList<>();
        int size = quintetList.size();
        if(CollectionUtils.isNotEmpty(quintetList)){
            for (int i = 0; i< quintetList.size() ; i++) {
                PolicyInfoDTO policyPushVO = quintetList.get(i);

                boolean isMerge = false;
                if(i < quintetList.size()){

                    for (int j = 0; j < quintetList.size(); j++) {
                        PolicyInfoDTO policyPushVO1 = quintetList.get(j);
                        if(policyPushVO.equals(policyPushVO1)){
                            //自己不能合
                            continue;
                        }
                        PolicyMergePropertyEnum propertyEnum = this.mergeResult( policyPushVO, policyPushVO1);
                        if(!(propertyEnum.equals(ADD_POLICY) || propertyEnum.equals(UN_OPEN_GENERATE))){
                            quintetList.remove(j);

                            afterMergePolicyList.add(policyPushVO);
                            quintetList.remove(i);
                            //表示每次从0开始
                            i=-1;
                            isMerge = true;
                            break;
                        }

                    }
                    if(!isMerge ){
                        //找了除了自己之外的所有五元组，没有可合并的就存入，并且只移除当前的存入的
                        afterMergePolicyList.add(policyPushVO);
                        quintetList.remove(i);
                        //表示每次从0开始
                        i=-1;
                    }else{
                        log.debug("当前循环没有匹配到,原始数组不变");
                    }
                }else{
                    afterMergePolicyList.add(policyPushVO);
                }

            }

        }

        if (afterMergePolicyList.size() == size) {
            //这里说明都没有合并的必要了就退出合并了
            return afterMergePolicyList;
        }else{
            return this.sameTaskMergeQuintuple(afterMergePolicyList);
        }


    }

    @Override
    public PolicyMergePropertyEnum mergeResult(PolicyInfoDTO policyPushVO, PolicyInfoDTO policyPushVO1){
        //前面需要判断比较相同之后才能进行下面的步骤
        String srcIp = policyPushVO.getSrcIp();
        String srcIp1 = policyPushVO1.getSrcIp();
        boolean equalSrcIpCollection = equalIpCollection(srcIp,srcIp1);
        String dstIp = policyPushVO.getDstIp();
        String dstIp1 = policyPushVO1.getDstIp();
        boolean equalDstIpCollection = equalIpCollection(dstIp,dstIp1);
        Integer ipType = policyPushVO.getIpType();
        //服务可合并
        boolean serviceMerge = equalServiceCollection(policyPushVO,policyPushVO1);
        if(equalSrcIpCollection && equalDstIpCollection && !serviceMerge){
            List<ServiceDTO> serviceList = policyPushVO.getServiceList();
            List<ServiceDTO> serviceList1 = policyPushVO1.getServiceList();
            if(CollectionUtils.isNotEmpty(serviceList) && CollectionUtils.isNotEmpty(serviceList1)){
                List<ServiceDTO> serviceDTOS = mergeServiceList(serviceList, serviceList1);
                policyPushVO.setServiceList(serviceDTOS);

            }
            return MERGE_SERVICE;

        }else if(equalSrcIpCollection && !equalDstIpCollection && serviceMerge){
            if(!POLICY_SOURCE_DST_DOMAIN.equals(policyPushVO.getPolicySource()) && !POLICY_SOURCE_DST_DOMAIN.equals(policyPushVO1.getPolicySource())){
                // 只有目的都不是域名时才能合并
                String newDstIp = combineIp(dstIp, dstIp1,ipType);
                policyPushVO.setDstIp(newDstIp);
                return MERGE_DST_IP;

            }else{
                return ADD_POLICY;
            }

        }else if(!equalSrcIpCollection && equalDstIpCollection && serviceMerge){
            String newSrcIp = combineIp(srcIp, srcIp1,ipType);
            policyPushVO.setSrcIp(newSrcIp);

            return MERGE_SRC_IP;

        }else if(equalSrcIpCollection && equalDstIpCollection && serviceMerge){
            //源域或目的域相同同但不满足三中二不能合
            return UN_OPEN_GENERATE;
        }else{
            return ADD_POLICY;
        }

    }

    /**
     * 比较源地址目的地址是否完全一致
     * @param srcOrDstIp
     * @param srcOrDstIp1
     * @return
     */
    private boolean equalIpCollection(String srcOrDstIp,String srcOrDstIp1){
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
     * @param policyPushVO
     * @param policyPushVO1
     * @return
     */
    private boolean equalServiceCollection(PolicyInfoDTO policyPushVO,PolicyInfoDTO policyPushVO1){


        List<ServiceDTO> serviceList = policyPushVO.getServiceList();
        List<ServiceDTO> serviceList1 = policyPushVO1.getServiceList();
        if(CollectionUtils.isNotEmpty(serviceList) && CollectionUtils.isNotEmpty(serviceList1)){
            Set<PolicyServiceTupleDTO> editServiceTupleSet = this.setServiceFormatter(serviceList);
            Set<PolicyServiceTupleDTO> editServiceTupleSet1 = this.setServiceFormatter(serviceList1);
            boolean equalCollection = CollectionUtils.isEqualCollection(editServiceTupleSet, editServiceTupleSet1);
            return equalCollection;
        }else if(CollectionUtils.isEmpty(serviceList) && CollectionUtils.isEmpty(serviceList1)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 服务转化
     *
     * @param serviceList
     */
    @Override
    public Set<PolicyServiceTupleDTO> setServiceFormatter(List<ServiceDTO> serviceList) {
        Set<PolicyServiceTupleDTO> newServiceTupleSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(serviceList)) {
            Set<String> newProtocolSet = new HashSet<>();

            Set<String> newDstPortSet = new HashSet<>();
            for (ServiceDTO service : serviceList) {
                PolicyServiceTupleDTO newServiceTuple = new PolicyServiceTupleDTO();


                if (StringUtils.isNotEmpty(service.getDstPorts())) {
                    List<String> dstPortsList = Arrays.asList(service.getDstPorts().split(","));
                    for (String dstPort : dstPortsList) {
                        if (PortUtils.isPortRange(dstPort)) {

                            Set<String> singlePortByRange = PortUtils.getSinglePortByRange(dstPort);
                            newDstPortSet.addAll(singlePortByRange);
                        } else {
                            newDstPortSet.add(dstPort);
                        }
                    }

                }

                if (StringUtils.isNotEmpty(service.getProtocol())) {
                    newProtocolSet.add(service.getProtocol());
                }
                newServiceTuple.setDstPortSet(newDstPortSet);
                newServiceTuple.setProtocolSet(newProtocolSet);
                newServiceTupleSet.add(newServiceTuple);
            }
        }
        return newServiceTupleSet;
    }

    @Override
    public String combineIp(String ipListString, String newIpListString,Integer taskType){
        Set<String> ipSet = new HashSet<>();
        String[] ipStringList = ipListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        String[] newIpStringList = newIpListString.split(PolicyConstants.ADDRESS_SEPERATOR);

        for(String ip:ipStringList) {
            if(StringUtils.isBlank(ip)){
                continue;
            }
            ipSet.add(ip);
        }

        for(String ip: newIpStringList) {
            if(StringUtils.isBlank(ip)){
                continue;
            }
            ipSet.add(ip);
        }
        ipSet = MergeIpServiceUtils.mergeIp(ipSet,taskType);
        StringBuilder sb = new StringBuilder();
        for(String ip:ipSet) {
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
            sb.append(ip);
        }

        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }


    @Override
    public List<ServiceDTO> mergeServiceList(List<ServiceDTO> serviceList, List<ServiceDTO> newServiceList) {
        if(newServiceList == null) {
            return serviceList;
        }
        for(ServiceDTO newService: newServiceList) {
            boolean hasSameProtocol = false;
            if(PolicyConstants.POLICY_NUM_VALUE_ANY.equalsIgnoreCase(newService.getProtocol().trim())){
                if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(newServiceList)){
                    newServiceList.clear();
                    newServiceList.add(newService);
                    return newServiceList;
                }
            }
            for(ServiceDTO service: serviceList) {
                //如果服务的协议为0 - any 那么久不用合并了
                if(PolicyConstants.POLICY_NUM_VALUE_ANY.equalsIgnoreCase(service.getProtocol().trim())){
                    if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(newServiceList)){
                        newServiceList.clear();
                        newServiceList.add(service);
                        return newServiceList;
                    }
                }
                //找到相同服务则合并端口
                if(service.getProtocol().trim().equals(newService.getProtocol().trim())) {
                    hasSameProtocol = true;
                    //若为icmp协议，则不用合并端口
                    if(service.getProtocol().equals(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                        break;
                    }
                    String dstPorts = service.getDstPorts();
                    String newDstPorts = newService.getDstPorts();
                    dstPorts = MergeIpServiceUtils.mergePort(dstPorts, newDstPorts);
                    service.setDstPorts(dstPorts);
                }
            }
            if(hasSameProtocol== false){
                serviceList.add(newService);
            }
        }

        return serviceList;
    }
}
