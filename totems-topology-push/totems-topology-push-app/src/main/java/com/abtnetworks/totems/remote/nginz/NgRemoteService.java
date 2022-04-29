package com.abtnetworks.totems.remote.nginz;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.external.vo.NatRuleMatchFlowVO;
import com.abtnetworks.totems.external.vo.RuleMatchFlowVO;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/29
 */
public interface NgRemoteService {
    /**
     * 获取五元组规则匹配数据流
     *
     * @param cmdDTO
     * @return
     */
    List<CmdDTO> getRuleMatchFlow(CmdDTO cmdDTO);


    /**
     * 重载
     * @param policy
     * @param device
     * @return
     */
    RuleMatchFlowVO getRuleMatchFlow(PolicyDTO policy, DeviceDTO device);

    /**
     * 获取Nat策略建议的相关策略
     * @param policy
     * @param device
     * @param natType
     * @return
     */
    List<NatRuleMatchFlowVO> getNatRuleMatchFlow(PolicyDTO policy, DeviceDTO device, Integer natType);
}
