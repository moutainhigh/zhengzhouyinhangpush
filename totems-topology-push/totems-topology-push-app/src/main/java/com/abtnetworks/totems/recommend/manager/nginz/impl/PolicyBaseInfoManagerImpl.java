package com.abtnetworks.totems.recommend.manager.nginz.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.IpFormatChangeUtils;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.manager.nginz.PolicyBaseInfoManager;
import com.abtnetworks.totems.recommend.vo.SubnetSearchResultDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchSubnetDTO;
import com.abtnetworks.totems.whale.baseapi.ro.SubnetRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleSubnetObjectClient;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 青提接口文档上对应，策略基本信息类型的接口调用类
 * @date 2021/1/29
 */
@Service
@Slf4j
public class PolicyBaseInfoManagerImpl implements PolicyBaseInfoManager {

    @Autowired
    private WhaleSubnetObjectClient whaleSubnetObjectClient;

    @Override
    public List<SubnetSearchResultDTO> subnetSearchWith(SimulationTaskDTO taskDTO) {
        List<SubnetSearchResultDTO> subnetSearchResultDTOS = new ArrayList<>();
        try {
            SearchSubnetDTO searchSubnetDTO = new SearchSubnetDTO();
            String labelModel = StringUtils.isNotBlank(taskDTO.getLabelModel()) ? taskDTO.getLabelModel() : PolicyConstants.LABEL_MODEL_OR;
            String startLabel = taskDTO.getStartLabel();
            String srcIp = taskDTO.getSrcIp();
            if (StringUtils.isNotBlank(startLabel)) {
                List<String> startLabels = Arrays.asList(startLabel.split(","));
                searchSubnetDTO.setTags(startLabels);
                searchSubnetDTO.setTagsOp(labelModel);
            }else if(StringUtils.isAllEmpty(startLabel,srcIp)){
                //处理 针对api调用时，起点标签与源ip同时为空的情况，平台页面不存在该情况
                searchSubnetDTO.setTrustLevel("INTERNET");
            }

            searchSubnetDTO.setWithFlag(false);

            if (StringUtils.isNotBlank(srcIp)) {
                //输入源ip情况，只用标签查子网  除了外到内的开通需要输入的源地址要一起查，（业务开通，内到外）
                Map<String, SubnetSearchResultDTO> map = new HashMap<>(8);
                List<String> srcIps = Arrays.asList(srcIp.split(","));
                srcIps.forEach(p -> {
                    //这里是单ip，不是集合，入参需要，
                    SubnetSearchResultDTO subnetSearchResultDTO = new SubnetSearchResultDTO();
                    subnetSearchResultDTO.setSrcIp(p);
                    List<String> ips = new ArrayList<>();
                    ips.add(p);
                    if (taskDTO.getIpType() != null && taskDTO.getIpType() == 1) {
                        List<CommonRangeStringDTO> ip6AddressRanges = IpFormatChangeUtils.ip6AddressRangesChange(ips);
                        searchSubnetDTO.setIp6AddressRanges(ip6AddressRanges);
                    } else {
                        List<CommonRangeStringDTO> ip4AddressRanges = IpFormatChangeUtils.ip4AddressRangesChange(ips);
                        searchSubnetDTO.setAddressRanges(ip4AddressRanges);
                    }
                    log.info("查询青提【/v0/subnets/search-with搜索子网关联的设备、接口、域】接口START,入参{}", JSONObject.toJSONString(searchSubnetDTO));
                    ResultRO<List<SubnetRO>> listResultRO = whaleSubnetObjectClient.getSubnetList(searchSubnetDTO, null, null);
                    boolean isHaveSubnet = getMergeIpSubnet(listResultRO, map, subnetSearchResultDTO);
                    log.info("查询到的源子网{}，参数{}", isHaveSubnet, JSONObject.toJSONString(listResultRO));
                    if(!isHaveSubnet){
                        //无源子网的情况
                        subnetSearchResultDTOS.add(subnetSearchResultDTO);
                    }
                });
                if (MapUtils.isNotEmpty(map)) {
                    map.forEach((k, v) -> {
                        subnetSearchResultDTOS.add(v);
                    });
                }

            } else {
                //外到内
                // 处理互联网开通，查询源子网地址默认值
                dealDefaultAddress(taskDTO, searchSubnetDTO);
                log.info("查询青提【/v0/subnets/search-with搜索子网关联的设备、接口、域】接口START,入参{}", JSONObject.toJSONString(searchSubnetDTO));
                ResultRO<List<SubnetRO>> listResultRO = whaleSubnetObjectClient.getSubnetList(searchSubnetDTO, null, null);
                if (listResultRO != null && listResultRO.getSuccess()) {
                    SubnetSearchResultDTO subnetSearchResultDTO = new SubnetSearchResultDTO();
                    List<SubnetRO> subnetROS = listResultRO.getData();
                    subnetSearchResultDTO.setSrcSubnetRO(subnetROS);
                    log.info("查询青提【/v0/subnets/search-with搜索子网关联的设备、接口、域】接口END,返参{}", JSONObject.toJSONString(subnetSearchResultDTO));
                    subnetSearchResultDTOS.add(subnetSearchResultDTO);
                    return subnetSearchResultDTOS;
                } else {
                    return subnetSearchResultDTOS;
                }
            }

        } catch (Exception e) {
            log.error("查询青提【/v0/subnets/search-with搜索子网关联的设备、接口、域】接口 异常EDN", e);
            return subnetSearchResultDTOS;
        }
        return subnetSearchResultDTOS;
    }

    /**
     * 处理互联网开通，查询源子网地址默认值
     * @param taskDTO
     * @param searchSubnetDTO
     */
    private void dealDefaultAddress(SimulationTaskDTO taskDTO, SearchSubnetDTO searchSubnetDTO) {
        if (taskDTO.getIpType() != null && taskDTO.getIpType() == 1) {
            List<CommonRangeStringDTO> ip6AddressRanges = new ArrayList<>();
            CommonRangeStringDTO commonIPV6RangeStringDTO = new CommonRangeStringDTO();
            commonIPV6RangeStringDTO.setStart("::0");
            commonIPV6RangeStringDTO.setEnd("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fff");
            ip6AddressRanges.add(commonIPV6RangeStringDTO);
            searchSubnetDTO.setIp6AddressRanges(ip6AddressRanges);
        } else {
            List<CommonRangeStringDTO> ip4AddressRanges = new ArrayList<>();
            CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();
            commonRangeStringDTO.setStart("0.0.0.0");
            commonRangeStringDTO.setEnd("255.255.255.255");
            ip4AddressRanges.add(commonRangeStringDTO);
            searchSubnetDTO.setAddressRanges(ip4AddressRanges);
        }
    }

    /**
     * 匹配到多个子网时先拆分后合并
     *
     * @param listResultRO
     * @param map
     * @param subnetSearchResultDTO
     * @return true 有源子网
     */
    private boolean getMergeIpSubnet(ResultRO<List<SubnetRO>> listResultRO, Map<String, SubnetSearchResultDTO> map, SubnetSearchResultDTO subnetSearchResultDTO) {

        if (listResultRO != null && listResultRO.getSuccess()) {
            List<SubnetRO> subnetROS = listResultRO.getData();
            if (CollectionUtils.isNotEmpty(subnetROS)) {
                String srcIp1 = subnetSearchResultDTO.getSrcIp();
                subnetROS.forEach(s -> {
                    String uuid = s.getUuid();
                    SubnetSearchResultDTO subnetSearchResultDTO1 = map.get(uuid);
                    if (ObjectUtils.isNotEmpty(subnetSearchResultDTO1)) {
                        String srcIp = subnetSearchResultDTO1.getSrcIp();
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(srcIp).append(",").append(srcIp1);
                        subnetSearchResultDTO1.setSrcIp(stringBuffer.toString());
                        map.put(uuid, subnetSearchResultDTO1);
                    } else {
                        List<SubnetRO> arrayList = new ArrayList<>();
                        arrayList.add(s);
                        SubnetSearchResultDTO subnetSearchResultDTO2 = new SubnetSearchResultDTO();
                        subnetSearchResultDTO2.setSrcSubnetRO(arrayList);
                        subnetSearchResultDTO2.setSrcIp(srcIp1);
                        map.put(uuid, subnetSearchResultDTO2);
                    }

                });
                return true;
            } else {
                log.info("没有查询源子网的参数");
                return false;
            }
        } else {
            log.info("没有查询源子网的参数，请检查子网接口");
            return false;
        }
    }
}
