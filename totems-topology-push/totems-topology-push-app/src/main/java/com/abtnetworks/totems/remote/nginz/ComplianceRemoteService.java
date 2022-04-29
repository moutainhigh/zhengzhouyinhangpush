package com.abtnetworks.totems.remote.nginz;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/17
 */
public interface ComplianceRemoteService {


    /***
     * 获取deny策略id
     * @param cmdDTO
     * @param actionParam
     * @return
     */
    List<DeviceFilterRuleListRO> getPolicyIdByDenyOrPermit(CmdDTO cmdDTO, String actionParam, String policyType,String ruleListUuid);
}
