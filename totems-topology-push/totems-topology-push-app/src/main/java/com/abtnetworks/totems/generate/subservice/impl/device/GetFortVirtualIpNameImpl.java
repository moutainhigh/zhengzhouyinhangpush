package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SpecialNatDTO;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.model.ro.IPItemRO;
import com.abtnetworks.totems.whale.model.ro.NatClauseRO;
import com.abtnetworks.totems.whale.model.ro.PortSpecRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;

/**
 * @author lifei
 * @desc 获取飞塔虚拟ip 能否被复用
 * @date 2021/12/2 16:37
 */
@Slf4j
@Service
public class GetFortVirtualIpNameImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        PolicyDTO policyDTO = cmdDto.getPolicy();
        SpecialNatDTO specialNatDTO = policyDTO.getSpecialNatDTO();

        DeviceDTO device = cmdDto.getDevice();

        if (StringUtils.isBlank(specialNatDTO.getPostDstIp()) || StringUtils.isBlank(specialNatDTO.getDstIp())) {
            log.info("飞塔查询vip跳过，存在工单里面的转换前的目的ip/转换后的目的ip为空的情况,不去查询vip复用");
            return;
        }

        if(null != policyDTO.getIpType() && !IPV4.getCode().equals(policyDTO.getIpType())){
            log.info("当前nat工单不是ipv4类型的，不进行查询vip复用");
            return;
        }

        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleListSearch(device.getDeviceUuid(),specialNatDTO.getDstIp(),specialNatDTO.getServiceList());
        if(null == resultRO){
            log.info("当前工单根据设备uuid:{},转换前的目的ip:{},转换前的服务:{}查询虚拟ip对象为空,直接新建nat和虚拟ip", device.getDeviceUuid(), specialNatDTO.getDstIp(), JSONObject.toJSONString(specialNatDTO.getServiceList()));
            return;
        }
        String postDstIp = specialNatDTO.getPostDstIp();
        String postPort = specialNatDTO.getPostPort();

        for (DeviceFilterRuleListRO deviceFilterRuleListRO : resultRO.getData()) {
            if (null == deviceFilterRuleListRO.getNatClause() || 0 == deviceFilterRuleListRO.getNatClause().size()) {
                continue;
            }
            NatClauseRO natClauseRO = JSONObject.toJavaObject(deviceFilterRuleListRO.getNatClause(), NatClauseRO.class);
            String[] ip4Addresses = postDstIp.split(",");

            String startIp = "";
            String endIp = "";
            // 目前只考虑前端页面转换后的地址只填一个地址的情况：可以为单ip也可以为范围
            for (String ip4 : ip4Addresses) {
                if (IpUtils.isIPRange(ip4)) {
                    String[] ipSegment = ip4.trim().split("-");
                    startIp = ipSegment[0];
                    endIp = ipSegment[1];
                    break;
                } else if (IpUtils.isIPSegment(ip4)) {
                    startIp = IpUtils.getStartIp(ip4);
                    endIp = IpUtils.getEndIp(ip4);
                    break;
                } else if (IpUtils.isIP(ip4)) {
                    startIp = ip4;
                    endIp = ip4;
                    break;
                }
            }
            List<IPItemRO> ipItemROS = natClauseRO.getPostDstIPItems();
            if (CollectionUtils.isEmpty(ipItemROS) || null == ipItemROS.get(0)) {
                continue;
            }
            IPItemRO ipItemRO = ipItemROS.get(0);

            boolean samePostDst = false;
            if (Constants.HOST_IP.equalsIgnoreCase(ipItemRO.getType())) {
                if (ipItemRO.getIp4Addresses().contains(startIp) && ipItemRO.getIp4Addresses().contains(endIp)) {
                    samePostDst = true;
                }
            } else if (Constants.RANGE.equalsIgnoreCase(ipItemRO.getType())) {
                if (ipItemRO.getIp4Range().getStart().equalsIgnoreCase(startIp) &&
                        ipItemRO.getIp4Range().getEnd().equalsIgnoreCase(endIp)) {
                    samePostDst = true;
                }
            }

            boolean samePostPort = false;

            if (StringUtils.isBlank(postPort) && CollectionUtils.isEmpty(natClauseRO.getPostDstPortSpec())) {
                samePostPort = true;
            } else if ((StringUtils.isNotBlank(postPort) && CollectionUtils.isEmpty(natClauseRO.getPostDstPortSpec())) ||
                    (StringUtils.isBlank(postPort) && CollectionUtils.isNotEmpty(natClauseRO.getPostDstPortSpec()))) {
                samePostPort = false;
            } else {
                for (PortSpecRO portSpecRO : natClauseRO.getPostDstPortSpec()) {
                    if (CollectionUtils.isNotEmpty(portSpecRO.getPortValues()) && portSpecRO.getPortValues().contains(postPort)) {
                        samePostPort = true;
                    } else {
                        samePostPort = false;
                    }
                }
            }
            if (samePostDst && samePostPort) {
                log.info(String.format("匹配到策略和当前工单目的转换前后和服务装换前后完全一致,复用的vip名称为:%s", deviceFilterRuleListRO.getName()));
                policyDTO.setExistVirtualIpName(deviceFilterRuleListRO.getName());
                return;
            }
        }
        log.info("当前工单匹配转换后的目的地址和转换后的服务没有匹配上,无法生成命令行");
        throw new IssuedExecutorException(SendErrorEnum.FORTINET_VIRTUALIP_EXIST);
    }
}
