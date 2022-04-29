package com.abtnetworks.totems.push.manager;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.manager.DenyPolicyInfoDTO;
import com.abtnetworks.totems.common.dto.manager.MatchClauseDTO;
import com.abtnetworks.totems.whale.policyoptimize.ro.CheckRuleRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultRO;

/**
 * @author Administrator
 * @Title:
 * @Description: 策略合并成新工单
 * @date 2021/3/15
 */
public interface PolicyMergeNewTaskService {
    /**
     * 给生成的参数做合并
     * @param cmdDTO
     */
    void mergePolicyForGenerateParam(CmdDTO cmdDTO);

    /**
     * 交行对查到的合并策略需要过滤
     * ①否为同一个应用系统
     * ②两者的端口是否同为高危端口或非高危端口
     * @param cmdDTO
     * @param ruleCheckResultRO
     * @param denyPolicyInfoDTO
     */
    void filterForGenerateParam(CmdDTO cmdDTO, RuleCheckResultRO ruleCheckResultRO, DenyPolicyInfoDTO denyPolicyInfoDTO);

    /**
     * 构件数据
     * @param mergeProperty
     * @param mergeValue
     * @param cmdDTO
     * @param primaryRule
     * @return
     */
    void buildData(Integer mergeProperty, String mergeValue, CmdDTO cmdDTO,
                                            CheckRuleRO primaryRule);

    /**
     * 获取第一个deny策略id
     * @param cmdDTO
     * @param advancedSettingOpen
     * @return
     */
    DenyPolicyInfoDTO getPolicyIdByFirstDeny(CmdDTO cmdDTO,boolean advancedSettingOpen);


    /**
     * 设置可移动位置
     * @param policyIdByFirstDeny
     * @param cmdDTO
     */
    void setPolicyId2SwapId(DenyPolicyInfoDTO policyIdByFirstDeny, CmdDTO cmdDTO);



}
