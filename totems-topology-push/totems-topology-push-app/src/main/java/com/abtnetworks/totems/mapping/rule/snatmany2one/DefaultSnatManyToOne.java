package com.abtnetworks.totems.mapping.rule.snatmany2one;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.exception.BusinessException;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.mapping.common.AutoMappingExecutor;
import com.abtnetworks.totems.mapping.common.CommonExecutor;
import com.abtnetworks.totems.mapping.dto.AddressPoolDTO;
import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.CommonExecuteResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.enums.AutoMappingNatTypeEnum;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import com.abtnetworks.totems.mapping.rule.snatone2one.DefaultSnatOneToOne;
import com.abtnetworks.totems.mapping.utils.MappingUtils;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author liuchanghao
 * @desc Snat 多对一 默认实现
 * @date 2022-01-20 17:06
 */
@Service
@Log4j2
public class DefaultSnatManyToOne extends CommonExecutor implements AutoMappingExecutor {
    @Autowired
    private DefaultSnatOneToOne defaultSnatOneToOne;

    /**
     * 查询标识：退化
     */
    private final String QUERY_FLAG_DEGENERATE = "Degenerate";

    /**
     * 询标识：复用
     */
    private final String QUERY_FLAG_REUSE = "Reuse";


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoMappingTaskResultDTO matchAndGenerateAutoMappingTask(RuleProcessDTO dto) throws Exception{
        AutoMappingTaskResultDTO autoMappingTaskResultDTO = new AutoMappingTaskResultDTO();
        CommonExecuteResultDTO commonExecuteResultDTO = new CommonExecuteResultDTO();

        // 1.校验地址池，取出匹配上的地址池数据
        boolean validateResult = MappingUtils.validateOrderAndAddressPool(dto);
        if (!validateResult) {
            log.error("工单:{}填写的源ip地址,并没有完全被地址池数据包含,请检查配置",dto.getTheme());
            throw new BusinessException(SendErrorEnum.ADDRESS_NOT_FULLY_CONTAINED);
        }
        PushAutoMappingPoolEntity entity = dto.getAddressPoolDTO().getMatchPools().get(0);

        // 2.查询是否退化(源的多对一的逻辑，如果根据所有的源地址和ip映射表的目的NAT的转换后去交，如果交到了就直接退化成1对1)
        Map<String, String> resultNatMap = new LinkedHashMap<>();
        this.queryDegenerateOrReuse(entity, dto.getSrcIp(), QUERY_FLAG_DEGENERATE, resultNatMap);
        if (resultNatMap.size() > 0) {
            // 如果退化成1对1则走一对一的逻辑
            // 3.查复用。一对一时将IP拆成单IP（多对一时拿IP整体），遍历IP，去IP匹配表中查询该设备下的数据（先查whale，再查mysql）
            dto.setRuleTypeTaskEnum(RuleTypeTaskEnum.SNAT_ONE_TO_ONE);
            return defaultSnatOneToOne.matchAndGenerateAutoMappingTask(dto);
        }
        // 3.查询复用
        this.queryDegenerateOrReuse(entity, dto.getSrcIp(), QUERY_FLAG_REUSE, resultNatMap);
        if (resultNatMap.size() > 0) {
            dto.setNatMap(resultNatMap);
            // 4. 合并nat数据，新增一条dnat到工单表
            this.mergeAndGenerateNatScene(dto, commonExecuteResultDTO);
            // 5. 组装参数返回给调用方
            String deviceUuid = entity.getDeviceUuid();

            if (deviceUuid != null) {
                NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                if (nodeEntity != null) {
                    commonExecuteResultDTO.setDeviceIp(nodeEntity.getIp());
                    commonExecuteResultDTO.setDeviceName(nodeEntity.getDeviceName());
                }
            }
            super.buildResultData(autoMappingTaskResultDTO, commonExecuteResultDTO, entity);
            autoMappingTaskResultDTO.setRuleTypeTaskEnum(dto.getRuleTypeTaskEnum());
            return autoMappingTaskResultDTO;
        }
        // 4.没有查询到复用，则去查询地址池获取下一个可用ip
        AutoMappingIpSearchVO autoMappingIpSearchVO = new AutoMappingIpSearchVO();
        autoMappingIpSearchVO.setDeviceUuid(entity.getDeviceUuid());
        List<AutoMappingIpVO> autoMappingIpResultForPush = autoIpMappingManager.queryIpMappingFromPush(autoMappingIpSearchVO);

        dto.setIpPoolPostIp(entity.getPostIp());
        dto.setCurrentIp(entity.getNextAvailableIp());
        String lastAvailableIp = getIpMappingByNextAvailableIp(dto, entity, autoMappingIpResultForPush);
        resultNatMap.put(dto.getSrcIp(), lastAvailableIp);

        dto.setNatMap(resultNatMap);
        // 5. 新增ip映射表数据
        super.insertIntoIpMatchTable(dto, commonExecuteResultDTO);

        dto.setCurrentIp(lastAvailableIp);
        String nextIp = this.getNextAvailableIp(dto);
        if(StringUtils.isBlank(nextIp)){
            dto.setCurrentIp(PolicyConstants.USE_UP);
        }else{
            dto.setCurrentIp(nextIp);
        }
        // 6. 更新匹配到的地址池中的下一个可用ip
        super.updateNextAvailableIp(dto);
        // 7. 合并nat数据，新增一条dnat到工单表
        this.mergeAndGenerateNatScene(dto, commonExecuteResultDTO);
        // 8. 组装返回数据给调用方
        super.buildResultData(autoMappingTaskResultDTO, commonExecuteResultDTO, entity);
        autoMappingTaskResultDTO.setRuleTypeTaskEnum(dto.getRuleTypeTaskEnum());

        autoMappingIpResultForPush.clear();
        return autoMappingTaskResultDTO;
    }


    @Override
    public boolean validateOrderAndAddressPool(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        return false;
    }

    @Override
    public String getNextAvailableIp(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        return null;
    }

    @Override
    public void mergeAndGenerateNatScene(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        List<String> preIps = new ArrayList<>();
        List<String> postIps = new ArrayList<>();

        for (String key : dto.getNatMap().keySet()) {
            preIps.add(key);
            postIps.add(dto.getNatMap().get(key));
        }
        String preIpStr = StringUtils.join(preIps, PolicyConstants.ADDRESS_SEPERATOR);
        String postIpStr = StringUtils.join(postIps, PolicyConstants.ADDRESS_SEPERATOR);

        AddressPoolDTO poolDTO = dto.getAddressPoolDTO();
        PushAutoMappingPoolEntity natEntity = poolDTO.getMatchPools().get(0);

        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setDeviceUuid(natEntity.getDeviceUuid());

        if(DeviceModelNumberEnum.isRangeCiscoCode(DeviceModelNumberEnum.fromString(commonExecuteResultDTO.getModelNumber()).getCode())){
            sNatPolicyDTO.setSrcZone(StringUtils.isNotBlank(natEntity.getSrcZone()) ? natEntity.getSrcZone() : natEntity.getInDevItfAlias());
            sNatPolicyDTO.setDstZone(StringUtils.isNotBlank(natEntity.getDstZone()) ? natEntity.getDstZone() : natEntity.getOutDevItfAlias());
        } else {
            sNatPolicyDTO.setSrcZone(StringUtils.isNotBlank(natEntity.getSrcZone()) ? natEntity.getSrcZone() : null);
            sNatPolicyDTO.setDstZone(StringUtils.isNotBlank(natEntity.getDstZone()) ? natEntity.getDstZone() : null);
        }

        sNatPolicyDTO.setSrcItf(natEntity.getInDevIf());
        sNatPolicyDTO.setDstItf(natEntity.getOutDevIf());
        sNatPolicyDTO.setInDevItfAlias(natEntity.getInDevItfAlias());
        sNatPolicyDTO.setOutDevItfAlias(natEntity.getOutDevItfAlias());
        sNatPolicyDTO.setSrcIp(preIpStr);
        sNatPolicyDTO.setPostIpAddress(postIpStr);
        sNatPolicyDTO.setDstIp(dto.getDstIp());
        sNatPolicyDTO.setTheme(dto.getTheme() + "_" + AutoMappingNatTypeEnum.SNAT.getName());
        sNatPolicyDTO.setServiceList(dto.getServiceList());
        recommendTaskManager.insertSrcNatPolicy(sNatPolicyDTO, dto.getAuth());
        // 回设给外部调用方
        commonExecuteResultDTO.setNatId(sNatPolicyDTO.getTaskId());
        commonExecuteResultDTO.setNatTheme(sNatPolicyDTO.getTheme());
        commonExecuteResultDTO.setPostSrcIp(postIpStr);

    }

    /**
     * 查询是否退化 queryFlag = QUERY_FLAG_DEGENERATE
     * 查询目的NAT的转换后，看是否相交，如果有相交则退化成1对1，如果没有相交则还是走后面的流程
     * <p>
     * 查询是否复用 queryFlag = QUERY_FLAG_REUSE
     * 查询源NAT的转换前，看是否相交，如果有相交则直接复用上,新增到映射关系的map中
     *
     * @param entity
     * @param targetIp
     */
    private void queryDegenerateOrReuse(PushAutoMappingPoolEntity entity, String targetIp, String queryFlag, Map<String, String> resultNatMap) throws Exception {
        // 源NAT的多对一的场景，拿所有的源地址去整体交
        AutoMappingIpSearchVO autoMappingIpSearchVO = new AutoMappingIpSearchVO();
        autoMappingIpSearchVO.setDeviceUuid(entity.getDeviceUuid());

        if (QUERY_FLAG_DEGENERATE.equals(queryFlag)) {
            autoMappingIpSearchVO.setNatType(AutoMappingNatTypeEnum.DNAT.getCode());
            autoMappingIpSearchVO.setPostIp(targetIp);
        }
        if (QUERY_FLAG_REUSE.equals(queryFlag)) {
            autoMappingIpSearchVO.setNatType(AutoMappingNatTypeEnum.SNAT.getCode());
            autoMappingIpSearchVO.setPreIp(targetIp);
        }

        List<AutoMappingIpVO> autoMappingIpResultForQt = autoIpMappingManager.queryIpMappingFromQt(autoMappingIpSearchVO);

        if (CollectionUtils.isEmpty(autoMappingIpResultForQt)) {
            List<AutoMappingIpVO> autoMappingIpResultForPush = autoIpMappingManager.queryIpMappingFromPush(autoMappingIpSearchVO);

            if (CollectionUtils.isEmpty(autoMappingIpResultForPush)) {
                return;
            } else {
                queryDegenerateBylocalIpMapping(targetIp, autoMappingIpResultForPush, queryFlag, resultNatMap);
            }
        } else {
            // 从qt查询结果中获取相交的部分
            getIntersectingData(targetIp, resultNatMap, autoMappingIpResultForQt, queryFlag);
        }
    }

    /**
     * 对比mysql中的ip映射表数据，如果有相交的，直接返回true
     *
     * @param targetIp
     * @param autoMappingIpResult
     */
    private void queryDegenerateBylocalIpMapping(String targetIp, List<AutoMappingIpVO> autoMappingIpResult, String queryFlag, Map<String, String> resultNatMap) throws Exception {
        if (CollectionUtils.isEmpty(autoMappingIpResult)) {
            return;
        }
        getIntersectingData(targetIp, resultNatMap, autoMappingIpResult, queryFlag);
    }

    /**
     * 获取相交的数据存入到map
     *
     * @param targetIp
     * @param resultNatMap
     * @param autoMappingIpResult
     * @param queryFlag
     * @throws Exception
     */
    private void getIntersectingData(String targetIp, Map<String, String> resultNatMap, List<AutoMappingIpVO> autoMappingIpResult, String queryFlag) throws Exception {
        for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResult) {
            if (QUERY_FLAG_DEGENERATE.equals(queryFlag)) {
                if (StringUtils.isBlank(autoMappingIpVO.getPostIp())) {
                    continue;
                }
                String[] mappingPostIps = autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR);
                String[] targetIps = targetIp.split(PolicyConstants.ADDRESS_SEPERATOR);
                QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(Arrays.asList(targetIps), Arrays.asList(mappingPostIps));
                if (null != tmp && CollectionUtils.isNotEmpty(tmp.getFilterOutData())) {
                    resultNatMap.put(targetIp, autoMappingIpVO.getPreIp());
                    break;
                }
            } else if (QUERY_FLAG_REUSE.equals(queryFlag)) {
                // 如果是查询是否复用，直接对比源NAT转换前的地址看是否有交集
                if (StringUtils.isBlank(autoMappingIpVO.getPreIp())) {
                    continue;
                }
                String[] mappingPreIps = autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR);
                String[] targetIps = targetIp.split(PolicyConstants.ADDRESS_SEPERATOR);
                QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(Arrays.asList(targetIps), Arrays.asList(mappingPreIps));
                if (null != tmp && CollectionUtils.isNotEmpty(tmp.getFilterOutData())) {
                    resultNatMap.put(targetIp, autoMappingIpVO.getPostIp());
                    break;
                }
            }

        }
    }

    /**
     * 获取Ip映射对应关系
     *
     * @param srcDstIp
     * @param autoMappingIpResult
     * @param natMap
     */
    private void getIpMapping(String srcDstIp, List<AutoMappingIpVO> autoMappingIpResult, Map<String, String> natMap) {
        if (CollectionUtils.isEmpty(autoMappingIpResult)) {
            return;
        }
        for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResult) {
            // 如果是匹配到映射关系相同，且是ip一对一的，则直接确定对应关系
            if (srcDstIp.equals(autoMappingIpVO.getPreIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPostIp())
                    && autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                natMap.put(srcDstIp, autoMappingIpVO.getPostIp());
                break;
            }
            if (srcDstIp.equals(autoMappingIpVO.getPostIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPreIp())
                    && autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                natMap.put(srcDstIp, autoMappingIpVO.getPreIp());
                break;
            }
        }
    }

    /**
     * 取下一个可用ip之后再去查询ip映射关系，查询到了return true，否则return false
     *
     * @param entity
     * @param nextAvailableIp
     */
    private boolean queryIpMappingByNextAvailableIp(PushAutoMappingPoolEntity entity, String nextAvailableIp, List<AutoMappingIpVO> autoMappingIpResultForPush) throws Exception {
        AutoMappingIpSearchVO autoMappingIpSearchVO = new AutoMappingIpSearchVO();
        autoMappingIpSearchVO.setDeviceUuid(entity.getDeviceUuid());
        autoMappingIpSearchVO.setPreIp(nextAvailableIp);
        List<AutoMappingIpVO> autoMappingIpResultForQt = autoIpMappingManager.queryIpMappingFromQt(autoMappingIpSearchVO);
        if (CollectionUtils.isEmpty(autoMappingIpResultForQt)) {
            // 如果查转换前的地址查询不到则再去查询转换后的地址
            autoMappingIpSearchVO.setPostIp(nextAvailableIp);
            autoMappingIpSearchVO.setPreIp(null);
            autoMappingIpResultForQt = autoIpMappingManager.queryIpMappingFromQt(autoMappingIpSearchVO);
            if (CollectionUtils.isEmpty(autoMappingIpResultForQt)) {
                if (CollectionUtils.isEmpty(autoMappingIpResultForPush)) {
                    return false;
                } else {
                    for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResultForPush) {
                        if (StringUtils.isNotBlank(autoMappingIpVO.getPreIp())) {
                            String[] mappingPreIps = autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR);
                            String[] targetIps = nextAvailableIp.split(PolicyConstants.ADDRESS_SEPERATOR);
                            QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(Arrays.asList(targetIps), Arrays.asList(mappingPreIps));
                            // 如果判断下一个可用ip和当前ip映射表有交集则直接推出
                            if (null != tmp && CollectionUtils.isNotEmpty(tmp.getFilterOutData())) {
                                return true;
                            }
                        }
                        if (StringUtils.isNotBlank(autoMappingIpVO.getPostIp())) {
                            String[] mappingPostIps = autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR);
                            String[] targetIps = nextAvailableIp.split(PolicyConstants.ADDRESS_SEPERATOR);
                            QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(Arrays.asList(targetIps), Arrays.asList(mappingPostIps));
                            // 如果判断下一个可用ip和当前ip映射表有交集则直接推出
                            if (null != tmp && CollectionUtils.isNotEmpty(tmp.getFilterOutData())) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    /**
     * 获取下一个可以ip然后查询IP映射表
     *
     * @param dto
     */
    private String getIpMappingByNextAvailableIp(RuleProcessDTO dto, PushAutoMappingPoolEntity entity, List<AutoMappingIpVO> autoMappingIpResultForPush) throws Exception {
        boolean done = false;

        String availableIp = dto.getCurrentIp();
        if(PolicyConstants.USE_UP.equals(availableIp)){
            log.error("工单:{}工单检测时,由于前面已经使用完了可用IP,当前无可用IP,请检查地址池配置",dto.getTheme());
            throw new BusinessException(SendErrorEnum.NO_NEXT_AVAILABLEIP);
        }
        if (StringUtils.isBlank(availableIp)) {
            done = true;
        }else {
            done = queryIpMappingByNextAvailableIp(entity, availableIp, autoMappingIpResultForPush);
        }

        if (!done) {
            return availableIp;
        } else {
            availableIp = super.getNextAvailableIp(dto);
            if (StringUtils.isBlank(availableIp)) {
                log.error("工单:{}工单检测时,由于前面已经使用完了可用IP,当前无可用IP,请检查地址池配置", dto.getTheme());
                throw new BusinessException(SendErrorEnum.NO_NEXT_AVAILABLEIP);
            }
            dto.setIpPoolPostIp(entity.getPostIp());
            dto.setCurrentIp(availableIp);
            return this.getIpMappingByNextAvailableIp(dto, entity, autoMappingIpResultForPush);
        }
    }
}
