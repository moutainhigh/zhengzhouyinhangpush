package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.utils.AliStringUtils;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 该类用于将策略的源IP地址和目的IP地址由范围转换成为至多两个（理想情况下为1个）IP子网，
 *              用于处理不支持IP地址为地址范围的的设备的策略生成流程（当前主要用于Cisco)
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class ConvertIpRangeToSegmentCmdServiceImpl implements CmdService {

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        PolicyDTO policyDTO = cmdDTO.getPolicy();

        policyDTO.setSrcIp(convertIpRangeToSegment(policyDTO.getSrcIp()));

        policyDTO.setDstIp(convertIpRangeToSegment(policyDTO.getDstIp()));
    }

    private String convertIpRangeToSegment(String ipAddress) {
        if(AliStringUtils.isEmpty(ipAddress)) {
            return ipAddress;
        }
        List<String> ipList = new ArrayList<>();
        String[] ips = ipAddress.split(",");
        for(String ip:ips) {
            List<String> ipSegments = IPUtil.convertRangeToSubnet(ip);
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
}
