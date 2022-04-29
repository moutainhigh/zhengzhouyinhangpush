package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.*;

/**
 * @Description 该类用于获取当前策略集的RuleListUuid值
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetRuleListUuidCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    RecommendTaskManager taskManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        RuleIPTypeEnum type = RuleIPTypeEnum.IP4;
        //判断是ipv4还是ipv6
        PolicyDTO policy = cmdDTO.getPolicy();
        PolicyEnum policyType = policy.getType();
        if(policy!=null && policy.getIpType()!=null){
            if(policy.getIpType()==2){
                String srcIp = policy.getSrcIp();
                if(StringUtils.isNotEmpty(srcIp)){
                    List<String> ipList = Arrays.asList(srcIp.split(","));
                    for(int i=0;i<ipList.size();i++){
                        String address = ipList.get(i);
                        if(IpUtils.isIPRange(address) || IpUtils.isIPSegment(address) ||IpUtils.isIP(address) ){
                            type = RuleIPTypeEnum.IP4;
                            break;
                        }else if(address.contains(":")){
                            type = RuleIPTypeEnum.IP6;
                        }
                    }
                }
            }else if(policy.getIpType()==0){
                type = RuleIPTypeEnum.IP4;
            }else if(policy.getIpType()==1){
                type = RuleIPTypeEnum.IP6;
            }
        }
        String ruleListUuid = deviceDTO.getRuleListUuid();
        ruleListUuid = getRuleListUuid(ruleListUuid, deviceDTO.getNodeEntity(),type, policyType);
        deviceDTO.setRuleListUuid(ruleListUuid);
    }

    private String getRuleListUuid(String ruleListUuid, NodeEntity node, RuleIPTypeEnum type, PolicyEnum policyType) {
        //针对特殊设备获取其RuleListUUID
        String modelNumber = node.getModelNumber();
        if (!AliStringUtils.isEmpty(modelNumber)) {
            if (modelNumber.equals("USG6000")) {
                return  whaleManager.getHuaweiRuleListUuid(node.getUuid());
            } else if (modelNumber.equals("JuniperSRX") || modelNumber.equals(SRX_NoCli.getKey()) || modelNumber.equals(JUNIPER_ROUTER.getKey())) {
                return whaleManager.getJuniperSrxRuleListUuid(node.getUuid());
            } else if (modelNumber.contains("DPTech Firewall")) {
                return whaleManager.getDpTechRuleListUuid(node.getUuid());
            } else if (modelNumber.contains("Cisco ASA") ) {
                return whaleManager.getCiscoRuleListUuid(node.getUuid(), taskManager.getAclDirection(node.getUuid()));
            } else if (modelNumber.contains("Fortinet")) {
                return whaleManager.getFortinetTechRuleListUuid(node.getUuid());
            } else if (modelNumber.contains("abtnetworks") || modelNumber.contains("anheng")
                    || modelNumber.contains("MaipuMSGFirewall") || modelNumber.contains("V2.1.5i-s")
                    || modelNumber.contains("sdnware")) {
                if (type == RuleIPTypeEnum.IP4){
                    return whaleManager.getAbtnetworksTechRuleListUuid(node.getUuid(),policyType);
                }else{
                    return whaleManager.getAbtnetworksTechRule6ListUuid(node.getUuid(),policyType);
                }
            } else if (modelNumber.equals(TOPSEC_TOS_005.getKey())||modelNumber.equals(TOPSEC_TOS_010_020.getKey()) ||modelNumber.equals(TOPSEC_NG.getKey()) ||modelNumber.equals(TOPSEC_NG2.getKey()) ||
                    modelNumber.equals(TOPSEC_NG3.getKey())  || modelNumber.equals(TOPSEC_NG4.getKey()) ) {
                return whaleManager.getTopsecRuleListUuid(node.getUuid());
            } else if (modelNumber.equals("H3C SecPath V7")) {
                return whaleManager.getH3Cv7RuleListUuid(node.getUuid());
            } else if (modelNumber.equals("Venustech VSOS") || modelNumber.equals("V2.6.3+")) {
                return whaleManager.getVenusVSOSRuleListUuid(node.getUuid());
            }
        }
        //不为上面所有设备则返回从相关策略中查找到的RuleListUuid
        return ruleListUuid;
    }
}
