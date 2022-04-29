package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface CommandService {

    /**
     * 模拟仿真任务生成命令行
     * @param task 模拟仿真任务
     * @return 执行结果
     */
    int generateCommandline(SimulationTaskDTO task, UserInfoDTO userInfoDTO);


    /**
     * 部分厂商需要设置前置步骤
     * @param modelNumber 型号
     * @param commandLineDTO 命令行对象
     */
    void setPreSteps(String modelNumber, CommandlineDTO commandLineDTO);

    void setAdvancedSetting(CmdDTO cmdDTO, String modelNumber, String deviceUuid);

    /**
     * 过滤数据流之后对比拆分之后的地地址和工单原地址进行比较，如果完全一样就替换成共工单填写的地址去生成命令行（主要是子网拆成了范围，但是过数据流也没有刨除的情况下，还原成原子网）
     * @param srcIps
     * @param dstIps
     * @param policySrcIp
     * @param policyDstIp
     * @return
     */
    Map<String, String> filterRuleDataByTaskAddress(String[] srcIps, String[] dstIps, String policySrcIp, String policyDstIp);

}
