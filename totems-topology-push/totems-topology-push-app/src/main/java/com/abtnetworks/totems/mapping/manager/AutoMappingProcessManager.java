package com.abtnetworks.totems.mapping.manager;

import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingTaskEntity;
import com.abtnetworks.totems.mapping.enums.CustomRuleTypeEnum;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import com.abtnetworks.totems.mapping.vo.OrderCheckVO;
import com.github.pagehelper.PageInfo;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @desc    地址映射自动匹配流程控制manager
 * @author liuchanghao
 * @date 2022-01-20 20:55
 */
public interface AutoMappingProcessManager {

    /**
     * 获取执行器
     * @param customRuleTypeEnum
     * @param ruleTypeTaskEnum
     * @return
     */
    String getExecutor(CustomRuleTypeEnum customRuleTypeEnum, RuleTypeTaskEnum ruleTypeTaskEnum);

    /**
     * 生成地址映射自动匹配工单数据流程
     * @param checkVO
     * @param pageInfo
     * @param sceneRuleEntity
     * @param auth
     * @param isAppointPostSrcIp
     * @return
     * @throws Exception
     */
    List<PushAutoMappingTaskEntity> generateAutoMappingProcessTask(OrderCheckVO checkVO, PageInfo<PushAutoMappingPoolEntity> pageInfo, PushAutoMappingSceneRuleEntity sceneRuleEntity,
                                                                   Authentication auth, boolean isAppointPostSrcIp) throws Exception;
    /**
     * 生成地址映射自动匹配工单数据
     * @param dto
     * @return
     */
    AutoMappingTaskResultDTO generateAutoMappingTask(RuleProcessDTO dto) throws Exception;
}
