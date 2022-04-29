package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 该类用于获取当前策略集上从域A到域B的第一条策略的ID，用于生成移动策略到第一条的语句。增加了对源域和目的域的判断。
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetFirstPolicyNameInZoneCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();

        PolicyDTO policyDTO = cmdDTO.getPolicy();
        String srcZone = policyDTO.getSrcZone();
        String dstZone = policyDTO.getDstZone();

        String ruleListUuid = deviceDTO.getRuleListUuid();


        String firstPolicyName = getFirstPolicyName(deviceUuid, ruleListUuid, srcZone, dstZone);

        SettingDTO settingDTO = cmdDTO.getSetting();
        settingDTO.setSwapNameId(firstPolicyName);
    }

    /**
     * 查询策略集下，符合条件的第一个策略集名称
     * @param deviceUuid  设备uuid
     * @param ruleListUuid 策略集uuid
     * @param srcZone  源域
     * @param dstZone  目的域
     * @return
     */
    protected String getFirstPolicyName(String deviceUuid, String ruleListUuid, String srcZone, String dstZone) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        if(resultRO == null) {
            return null;
        }
        if(StringUtils.isEmpty(srcZone)){
            srcZone = CommonConstants.ANY;
        }
        if(StringUtils.isEmpty(dstZone)){
            dstZone = CommonConstants.ANY;
        }
        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO ruleListRO : list) {
                List<String> inInterfaceGroupRefs = ruleListRO.getInInterfaceGroupRefs();
                List<String> outInterfaceGroupRefs = ruleListRO.getOutInterfaceGroupRefs();
                //2个域都 非空
                if (StringUtils.isNoneBlank(srcZone, dstZone) && inInterfaceGroupRefs != null && outInterfaceGroupRefs != null
                        && inInterfaceGroupRefs.contains(srcZone) && outInterfaceGroupRefs.contains(dstZone)) {
                    name = ruleListRO.getName();
                    break;
                }
                //2个域都 空
                if (StringUtils.isAllBlank(srcZone, dstZone)
                        && (inInterfaceGroupRefs == null || inInterfaceGroupRefs.isEmpty())
                        && (outInterfaceGroupRefs == null || outInterfaceGroupRefs.isEmpty())) {
                    name = ruleListRO.getName();
                    break;
                }
                //源域非空 ， 目的域空
                if (StringUtils.isNotBlank(srcZone) && StringUtils.isBlank(dstZone)
                        && inInterfaceGroupRefs != null && !inInterfaceGroupRefs.isEmpty()
                        && inInterfaceGroupRefs.contains(srcZone)
                        && (outInterfaceGroupRefs == null || outInterfaceGroupRefs.isEmpty())) {
                    name = ruleListRO.getName();
                    break;
                }

                //源域空  ，  目的域非空
                if (StringUtils.isBlank(srcZone) && StringUtils.isNotBlank(dstZone)
                        && (inInterfaceGroupRefs == null || inInterfaceGroupRefs.isEmpty())
                        && outInterfaceGroupRefs != null && !outInterfaceGroupRefs.isEmpty()
                        && outInterfaceGroupRefs.contains(dstZone)) {
                    name = ruleListRO.getName();
                    break;
                }
            }
        }
        return name;
    }
}
