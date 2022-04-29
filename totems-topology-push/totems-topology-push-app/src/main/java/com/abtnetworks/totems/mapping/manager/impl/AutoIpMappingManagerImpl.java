package com.abtnetworks.totems.mapping.manager.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import com.abtnetworks.totems.mapping.enums.AutoMappingNatTypeEnum;
import com.abtnetworks.totems.mapping.manager.AutoIpMappingManager;
import com.abtnetworks.totems.mapping.service.AutoMappingIpService;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpVO;
import com.abtnetworks.totems.whale.baseapi.dto.NatTransSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.IpAddressStringRangeRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.policy.ro.NatTransRelationRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lifei
 * @desc IP映射管理类
 * @date 2022/2/10 18:48
 */
@Service
@Log4j2
public class AutoIpMappingManagerImpl implements AutoIpMappingManager {

    @Autowired
    private AutoMappingIpService  autoMappingIpService;

    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;


    @Override
    public List<AutoMappingIpVO> queryIpMappingFromQt(AutoMappingIpSearchVO autoMappingIpSearchVO) throws UnknownHostException {
        // 查询whale那边IP映射关系，根据目标ip去查询(不区分NAT类型和转换前后)
        NatTransSearchDTO searchDTO = new NatTransSearchDTO();
        searchDTO.setDeviceUuid(autoMappingIpSearchVO.getDeviceUuid());
        if (null != autoMappingIpSearchVO.getNatType()) {
            searchDTO.setTransDirection(AutoMappingNatTypeEnum.SNAT.getCode().equals(autoMappingIpSearchVO.getNatType()) ? PolicyConstants.SRC :
                    AutoMappingNatTypeEnum.DNAT.getCode().equals(autoMappingIpSearchVO.getNatType()) ? PolicyConstants.DST : null);
        }
        List<IpAddressStringRangeRO> preIp = new ArrayList<>();

        if (StringUtils.isNotBlank(autoMappingIpSearchVO.getPreIp())) {
            String[] ips = autoMappingIpSearchVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String ip : ips) {
                String[] ipStartEnd = QuintupleUtils.ipv46toIpStartEnd(ip);
                IpAddressStringRangeRO rangeRO = new IpAddressStringRangeRO();
                rangeRO.setStartIp(ipStartEnd[0]);
                rangeRO.setEndIp(ipStartEnd[1]);
                preIp.add(rangeRO);
            }
        }

        List<IpAddressStringRangeRO> postIp = new ArrayList<>();
        if (StringUtils.isNotBlank(autoMappingIpSearchVO.getPostIp())) {
            String[] ips = autoMappingIpSearchVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String ip : ips) {
                String[] ipStartEnd = QuintupleUtils.ipv46toIpStartEnd(ip);
                IpAddressStringRangeRO rangeRO = new IpAddressStringRangeRO();
                rangeRO.setStartIp(ipStartEnd[0]);
                rangeRO.setEndIp(ipStartEnd[1]);
                postIp.add(rangeRO);
            }
        }
        searchDTO.setPreIp(preIp.size() > 0 ? preIp : null);
        searchDTO.setPostIp(postIp.size() > 0 ? postIp : null);
        log.info("查询青提ip映射表入参:{}", JSONObject.toJSONString(searchDTO));
        ResultRO<List<NatTransRelationRO>> resultROs = whaleDevicePolicyClient.searchNatTrans(searchDTO);
        log.info("查询青提ip映射表出参:{}", JSONObject.toJSONString(resultROs));

        if (!resultROs.getSuccess() || 0 == resultROs.getData().size()) {
            return new ArrayList<>();
        }
        List<NatTransRelationRO> natTrans = resultROs.getData();
        List<AutoMappingIpVO> resultVos = new ArrayList<>();
        for (NatTransRelationRO natTransRelationRO : natTrans) {
            if (null == natTransRelationRO.getPreTranIps() || 0 == natTransRelationRO.getPreTranIps().length) {
                continue;
            }
            if (null == natTransRelationRO.getPostTransIps() || 0 == natTransRelationRO.getPostTransIps().length) {
                continue;
            }
            AutoMappingIpVO autoMappingIpVO = new AutoMappingIpVO();
            String[] preTransIps = natTransRelationRO.getPreTranIps();
            String[] postTransIps = natTransRelationRO.getPostTransIps();
            autoMappingIpVO.setPreIp(StringUtils.join(Arrays.asList(preTransIps), ","));
            autoMappingIpVO.setPostIp(StringUtils.join(Arrays.asList(postTransIps), ","));
            resultVos.add(autoMappingIpVO);
        }
        return resultVos;
    }

    @Override
    public List<AutoMappingIpVO> queryIpMappingFromQtAll(AutoMappingIpSearchVO autoMappingIpSearchVO) {

        return null;
    }

    @Override
    public List<AutoMappingIpVO> queryIpMappingFromPush(AutoMappingIpSearchVO autoMappingIpSearchVO) {
        // 查询mysql这边的IP映射关系
        PushAutoMappingIpEntity record = new PushAutoMappingIpEntity();
        BeanUtils.copyProperties(autoMappingIpSearchVO, record);
        List<PushAutoMappingIpEntity> ipMappingList = autoMappingIpService.findIpMappingByEntityAll(record);
        if (CollectionUtils.isEmpty(ipMappingList)) {
            return new ArrayList<>();
        }
        List<AutoMappingIpVO> resultVo = new ArrayList<>();
        for (PushAutoMappingIpEntity entity : ipMappingList) {
            AutoMappingIpVO autoMappingIpVO = new AutoMappingIpVO();
            BeanUtils.copyProperties(entity, autoMappingIpVO);
            resultVo.add(autoMappingIpVO);
        }
        return resultVo;
    }
}
