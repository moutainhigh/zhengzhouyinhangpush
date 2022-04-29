package com.abtnetworks.totems.mapping.rule.snatone2one;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.exception.BusinessException;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.mapping.common.AutoMappingExecutor;
import com.abtnetworks.totems.mapping.common.CommonExecutor;
import com.abtnetworks.totems.mapping.dto.AddressPoolDTO;
import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.CommonExecuteResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.enums.AutoMappingNatTypeEnum;
import com.abtnetworks.totems.mapping.utils.MappingUtils;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc    Snat一对一默认实现
 * @author liuchanghao
 * @date 2022-01-20 17:06
 */
@Service
@Log4j2
public class DefaultSnatOneToOne extends CommonExecutor implements AutoMappingExecutor {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoMappingTaskResultDTO matchAndGenerateAutoMappingTask(RuleProcessDTO dto) throws Exception {
        AutoMappingTaskResultDTO autoMappingTaskResultDTO = new AutoMappingTaskResultDTO();
        CommonExecuteResultDTO commonExecuteResultDTO = new CommonExecuteResultDTO();

        // 1.校验地址池，取出匹配上的地址池数据
        boolean validateResult = MappingUtils.validateOrderAndAddressPool(dto);
        if (!validateResult) {
            log.error("工单:{}填写的源ip地址,并没有完全被地址池数据包含,请检查配置",dto.getTheme());
            throw new BusinessException(SendErrorEnum.ADDRESS_NOT_FULLY_CONTAINED);
        }
        PushAutoMappingPoolEntity entity = dto.getAddressPoolDTO().getMatchPools().get(0);

        // 2.查复用。一对一时将IP拆成单IP（多对一时拿IP整体），遍历IP，去IP匹配表中查询该设备下的数据（先查whale，再查mysql）
        String[] srcIps = dto.getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        Map<String, String> resultNatMap = new LinkedHashMap<>();

        // 查询whale中的ip映射关系，整体查询
        AutoMappingIpSearchVO autoMappingIpSearchVO = new AutoMappingIpSearchVO();
        autoMappingIpSearchVO.setDeviceUuid(entity.getDeviceUuid());
        List<AutoMappingIpVO> autoMappingIpResultForQt = autoIpMappingManager.queryIpMappingFromQt(autoMappingIpSearchVO);

        // 查询push中的ip映射关系，整体查询
        List<AutoMappingIpVO> autoMappingIpResultForPush = autoIpMappingManager.queryIpMappingFromPush(autoMappingIpSearchVO);


        // 定义查询复用没有查询到的ip集合
        List<String> restIps = new ArrayList<>();
        for (String srcIp : srcIps) {
            if (!IpUtils.isValidIp(srcIp)) {
                break;
            }
            String[] ips = QuintupleUtils.ipv46toIpList(srcIp);
            for (String itemIp : ips) {
                super.queryIpMapping(resultNatMap, restIps, itemIp, autoMappingIpResultForQt, autoMappingIpResultForPush);
            }
        }
        // 3.没有复用的ip,需要去地址池去取ip然后再去查whale和mysql,然后再去添加到对应关系的Map中
        Map<String, String> createIpMappingMap = new LinkedHashMap<>();
        dto.setIpPoolPostIp(entity.getPostIp());
        dto.setCurrentIp(entity.getNextAvailableIp());
        for (String restIp : restIps) {
            String mappingIp = super.getIpMappingByNextAvailableIp(dto, entity, autoMappingIpResultForQt, autoMappingIpResultForPush);
            // 如果没有下一个可用ip了，则直接报错
            if (StringUtils.isBlank(mappingIp)) {
                log.error("工单:{}工单检测时,可用IP不足，请检查地址池配置",dto.getTheme());
                autoMappingIpResultForQt.clear();
                autoMappingIpResultForPush.clear();
                throw new BusinessException(SendErrorEnum.NO_NEXT_AVAILABLEIP);
            } else {
                createIpMappingMap.put(restIp, mappingIp);
            }
            String nextIp = this.getNextAvailableIp(dto);
            if(StringUtils.isBlank(nextIp)){
                dto.setCurrentIp(PolicyConstants.USE_UP);
            }else{
                dto.setCurrentIp(nextIp);
            }

        }
        // 4. 将对应关系insert到ip映射表
        dto.setNatMap(createIpMappingMap);
        super.insertIntoIpMatchTable(dto, commonExecuteResultDTO);
        // 合并查询到的复用的对应关系 和从地址池中取的需要新增的对应关系
        resultNatMap.putAll(createIpMappingMap);

        // 5. 更新匹配到的地址池中的下一个可用ip
        super.updateNextAvailableIp(dto);
        dto.setNatMap(resultNatMap);
        // 6. 合并nat数据，新增一条snat到工单表
        this.mergeAndGenerateNatScene(dto, commonExecuteResultDTO);
        // 7. 组装参数返回给调用方
        super.buildResultData(autoMappingTaskResultDTO, commonExecuteResultDTO, entity);
        autoMappingTaskResultDTO.setRuleTypeTaskEnum(dto.getRuleTypeTaskEnum());
        autoMappingIpResultForQt.clear();
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
        sNatPolicyDTO.setDstIp(dto.getDstIp());
        sNatPolicyDTO.setPostIpAddress(postIpStr);
        sNatPolicyDTO.setTheme(dto.getTheme() + "_" + AutoMappingNatTypeEnum.SNAT.getName());
        sNatPolicyDTO.setServiceList(dto.getServiceList());
        recommendTaskManager.insertSrcNatPolicy(sNatPolicyDTO, dto.getAuth());
        // 回设给外部调用方
        commonExecuteResultDTO.setNatId(sNatPolicyDTO.getTaskId());
        commonExecuteResultDTO.setNatTheme(sNatPolicyDTO.getTheme());
        commonExecuteResultDTO.setPostSrcIp(postIpStr);

    }
}
