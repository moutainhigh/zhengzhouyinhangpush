package com.abtnetworks.totems.mapping.common;


import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.exception.BusinessException;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.CommonExecuteResultDTO;
import com.abtnetworks.totems.mapping.dto.PushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.enums.AutoMappingNatTypeEnum;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import com.abtnetworks.totems.mapping.manager.AutoIpMappingManager;
import com.abtnetworks.totems.mapping.service.AutoMappingIpService;
import com.abtnetworks.totems.mapping.service.PushAutoMappingPoolService;
import com.abtnetworks.totems.mapping.utils.MappingUtils;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpVO;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @desc    规则自动匹配公共方法抽象类
 * @author liuchanghao
 * @date 2022-01-20 20:42
 */
@Log4j2
public abstract class CommonExecutor {

    protected final String NAT_NAME_PRE = "AUTO_MAPPING_";

    @Autowired
    protected AutoIpMappingManager autoIpMappingManager;

    @Autowired
    protected RecommendTaskManager recommendTaskManager;

    @Autowired
    protected AutoMappingIpService autoMappingIpService;

    @Autowired
    protected PushAutoMappingPoolService pushAutoMappingPoolService;

    @Autowired
    protected NodeMapper policyRecommendNodeMapper;


    /**
     * 校验工单地址与地址池是否有交集
     *
     * @param dto
     * @param commonExecuteResultDTO
     * @return
     */
    public abstract boolean validateOrderAndAddressPool(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO);

    /**
     * 获取下一个可用IP
     *
     * @param commonExecuteResultDTO
     * @return
     */
    public abstract String getNextAvailableIp(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO);

    /**
     * 对Nat场景进行合并，保证IP映射关系顺序，生成Nat场景
     *
     * @param dto
     * @param commonExecuteResultDTO
     * @return
     */
    public abstract void mergeAndGenerateNatScene(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO);

    /**
     * 获取下一个可用ip
     *
     * @param dto
     * @return
     */
    protected String getNextAvailableIp(RuleProcessDTO dto) {
        String nextAvailableIp = MappingUtils.getNextAvailableIp(dto.getIpPoolPostIp(), dto.getCurrentIp());
        if (StringUtils.isBlank(nextAvailableIp)) {
            return null;
        }
        return nextAvailableIp;
    }

    /**
     * 更新下一个可用ip到地址池（将当前可用IP + 1）
     *
     * @param dto
     */
    protected void updateNextAvailableIp(RuleProcessDTO dto) {
        // 如果前面计算的最后一个可用ip为null，则不去更新地址池中的下一个可用ip
        if(StringUtils.isBlank(dto.getCurrentIp())){
            return;
        }
        // 2.当可新增的ip映射关系为空的时候直接跳出，不更新下一个可用ip
        if (ObjectUtils.isEmpty(dto.getNatMap())) {
            return;
        }
        PushAutoMappingPoolDTO pushAutoMappingPoolDTO = new PushAutoMappingPoolDTO();
        pushAutoMappingPoolDTO.setId(dto.getAddressPoolDTO().getMatchPools().get(0).getId());
//        String nextAvailableIp = getNextAvailableIp(dto);
//        if (StringUtils.isBlank(nextAvailableIp)) {
//            nextAvailableIp = PolicyConstants.USE_UP;
//        }
        pushAutoMappingPoolDTO.setNextAvailableIp(dto.getCurrentIp());
        pushAutoMappingPoolService.updateNextAvailableIp(pushAutoMappingPoolDTO);
    }


    /**
     * 获取下一个可用ip
     *
     * @param dto
     * @param entity
     * @param autoMappingIpResultForQt
     * @param autoMappingIpResultForPush
     * @return
     * @throws Exception
     */
    protected String getIpMappingByNextAvailableIp(RuleProcessDTO dto, PushAutoMappingPoolEntity entity,
                                                   List<AutoMappingIpVO> autoMappingIpResultForQt, List<AutoMappingIpVO> autoMappingIpResultForPush) throws Exception {
        boolean done = false;
        String availableIp = dto.getCurrentIp();

        if(PolicyConstants.USE_UP.equals(availableIp)){
            log.error("工单:{}工单检测时,由于前面已经使用完了可用IP,当前无可用IP,请检查地址池配置",dto.getTheme());
            throw new BusinessException(SendErrorEnum.NO_NEXT_AVAILABLEIP);
        }

        if (StringUtils.isBlank(availableIp)) {
            done = true;
        }else {
            done = this.queryIpMappingByNextAvailableIp(availableIp, autoMappingIpResultForQt, autoMappingIpResultForPush);
        }

        if (!done) {
            return availableIp;
        } else {
            availableIp = this.getNextAvailableIp(dto);
            if (StringUtils.isBlank(availableIp)) {
                log.error("工单:{}工单检测时,由于前面已经使用完了可用IP,当前无可用IP,请检查地址池配置", dto.getTheme());
                throw new BusinessException(SendErrorEnum.NO_NEXT_AVAILABLEIP);
            }

            dto.setIpPoolPostIp(entity.getPostIp());
            dto.setCurrentIp(availableIp);
            return this.getIpMappingByNextAvailableIp(dto, entity, autoMappingIpResultForQt, autoMappingIpResultForPush);
        }
    }


    /**
     * 取下一个可用ip之后再去查询ip映射关系，查询到了return true，否则return false
     *
     * @param nextAvailableIp
     * @param autoMappingIpResultForQt
     * @param autoMappingIpResultForPush
     */
    private boolean queryIpMappingByNextAvailableIp(String nextAvailableIp, List<AutoMappingIpVO> autoMappingIpResultForQt, List<AutoMappingIpVO> autoMappingIpResultForPush) throws Exception {
        if (CollectionUtils.isEmpty(autoMappingIpResultForQt)) {
            if (CollectionUtils.isEmpty(autoMappingIpResultForPush)) {
                return false;
            } else {
                for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResultForPush) {
                    // 如果是匹配到映射关系相同，且是ip一对一的，则直接确定对应关系
                    if (nextAvailableIp.equals(autoMappingIpVO.getPreIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPostIp())
                            && autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                        return true;
                    }
                    if (nextAvailableIp.equals(autoMappingIpVO.getPostIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPreIp())
                            && autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            // 先匹配青提的数据
            for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResultForQt) {
                // 如果是匹配到映射关系相同，且是ip一对一的，则直接确定对应关系
                if (nextAvailableIp.equals(autoMappingIpVO.getPreIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPostIp())
                        && autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                    return true;
                }
                if (nextAvailableIp.equals(autoMappingIpVO.getPostIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPreIp())
                        && autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                    return true;
                }
            }

            // 再匹配push的数据
            if (CollectionUtils.isEmpty(autoMappingIpResultForPush)) {
                return false;
            } else {
                for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResultForPush) {
                    // 如果是匹配到映射关系相同，且是ip一对一的，则直接确定对应关系
                    if (nextAvailableIp.equals(autoMappingIpVO.getPreIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPostIp())
                            && autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                        return true;
                    }
                    if (nextAvailableIp.equals(autoMappingIpVO.getPostIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPreIp())
                            && autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                        return true;
                    }
                }
                return false;
            }
        }
    }


    /**
     * 查询ip映射关系
     *
     * @param resultNatMap
     * @param restIps
     * @param srcIp
     */
    protected void queryIpMapping(Map<String, String> resultNatMap, List<String> restIps, String srcIp, List<AutoMappingIpVO> autoMappingIpResultForQt, List<AutoMappingIpVO> autoMappingIpResultForPush) {
        if (CollectionUtils.isEmpty(autoMappingIpResultForQt) && CollectionUtils.isEmpty(autoMappingIpResultForPush)) {
            restIps.add(srcIp);
            return;
        } else {
            getIpMapping(srcIp, autoMappingIpResultForQt, autoMappingIpResultForPush, resultNatMap, restIps);
        }
    }

    /**
     * 获取Ip映射对应关系
     *
     * @param srcIp
     * @param autoMappingIpResultForQt
     * @param natMap
     */
    private void getIpMapping(String srcIp, List<AutoMappingIpVO> autoMappingIpResultForQt, List<AutoMappingIpVO> autoMappingIpResultForPush, Map<String, String> natMap, List<String> restIps) {
        boolean match = false;
        // 对比whale的ip映射表
        if(CollectionUtils.isNotEmpty(autoMappingIpResultForQt)) {
            for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResultForQt) {
                // 如果是匹配到映射关系相同，且是ip一对一的，则直接确定对应关系
                if (srcIp.equals(autoMappingIpVO.getPreIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPostIp())
                        && autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                    natMap.put(srcIp, autoMappingIpVO.getPostIp());
                    match = true;
                    break;
                }
                if (srcIp.equals(autoMappingIpVO.getPostIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPreIp())
                        && autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                    natMap.put(srcIp, autoMappingIpVO.getPreIp());
                    match = true;
                    break;
                }
            }
        }

        if (match) {
            return;
        }

        if(CollectionUtils.isEmpty(autoMappingIpResultForPush)){
            restIps.add(srcIp);
            return;
        }
        // 对比mysql的ip映射表
        for (AutoMappingIpVO autoMappingIpVO : autoMappingIpResultForPush) {
            // 如果是匹配到映射关系相同，且是ip一对一的，则直接确定对应关系
            if (srcIp.equals(autoMappingIpVO.getPreIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPostIp())
                    && autoMappingIpVO.getPostIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                natMap.put(srcIp, autoMappingIpVO.getPostIp());
                match = true;
                break;
            }
            if (srcIp.equals(autoMappingIpVO.getPostIp()) && StringUtils.isNotBlank(autoMappingIpVO.getPreIp())
                    && autoMappingIpVO.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR).length == 1) {
                natMap.put(srcIp, autoMappingIpVO.getPreIp());
                match = true;
                break;
            }
        }
        if (!match) {
            restIps.add(srcIp);
        }
    }

    /**
     * 往IP匹配表中添加数据
     * @param dto
     * @param commonExecuteResultDTO
     */
    protected void insertIntoIpMatchTable(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        List<PushAutoMappingIpEntity> entitys = new ArrayList<>();
        List<PushAutoMappingPoolEntity> matchNats = dto.getAddressPoolDTO().getMatchPools();
        PushAutoMappingPoolEntity natEntity = matchNats.get(0);
        String deviceUuid = natEntity.getDeviceUuid();
        String deviceIp = null;
        String deviceName = null;
        String modelNumber = null;


        if (deviceUuid != null) {
            NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
            if (nodeEntity != null) {
                deviceIp = nodeEntity.getIp();
                deviceName = nodeEntity.getDeviceName();
                modelNumber = nodeEntity.getModelNumber();
            }
        }
        // 1.优先将设备ip和设备名称返回
        commonExecuteResultDTO.setDeviceIp(deviceIp);
        commonExecuteResultDTO.setDeviceName(deviceName);
        commonExecuteResultDTO.setModelNumber(modelNumber);

        // 2.当可新增的ip映射关系为空的时候直接跳出
        if (ObjectUtils.isEmpty(dto.getNatMap())) {
            return;
        }

        for (String key : dto.getNatMap().keySet()) {
            PushAutoMappingIpEntity entity = new PushAutoMappingIpEntity();
            entity.setPreIp(key);
            Integer code = dto.getRuleTypeTaskEnum().getCode();
            Integer natType = null;
            if (RuleTypeTaskEnum.DNAT_ONE_TO_ONE.getCode() == code) {
                natType = AutoMappingNatTypeEnum.DNAT.getCode();
            } else if (RuleTypeTaskEnum.SNAT_MANT_TO_ONE.getCode() == code ||
                    RuleTypeTaskEnum.SNAT_ONE_TO_ONE.getCode() == code) {
                natType = AutoMappingNatTypeEnum.SNAT.getCode();
            }
            entity.setNatType(natType);
            entity.setPostIp(dto.getNatMap().get(key));
            entity.setDeviceUuid(natEntity.getDeviceUuid());
            entity.setDeviceIp(deviceIp);
            entity.setDeviceName(deviceName);
            entity.setMappingNatId(natEntity.getId());
            entity.setCreateUser(dto.getAuth().getName());
            entity.setUuid(IdGen.uuid());
            entitys.add(entity);
        }
        autoMappingIpService.batchInsert(entitys);
    }

    /**
     * 构建返回数据
     * @param autoMappingTaskResultDTO
     * @param commonExecuteResultDTO
     * @param entity
     */
    protected void buildResultData(AutoMappingTaskResultDTO autoMappingTaskResultDTO, CommonExecuteResultDTO commonExecuteResultDTO, PushAutoMappingPoolEntity entity) {
        autoMappingTaskResultDTO.setStatus(ReturnCode.POLICY_MSG_OK);
        autoMappingTaskResultDTO.setNatId(commonExecuteResultDTO.getNatId());
        autoMappingTaskResultDTO.setNatTheme(commonExecuteResultDTO.getNatTheme());
        autoMappingTaskResultDTO.setDeviceIp(commonExecuteResultDTO.getDeviceIp());
        autoMappingTaskResultDTO.setDeviceName(commonExecuteResultDTO.getDeviceName());
        autoMappingTaskResultDTO.setPostSrcIp(commonExecuteResultDTO.getPostSrcIp());
        autoMappingTaskResultDTO.setPreDstIp(commonExecuteResultDTO.getPreDstIp());
    }

}
