package com.abtnetworks.totems.mapping.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @desc    地址映射自动匹配工具类
 * @author liuchanghao
 * @date 2022-01-25 15:31
 */
public class MappingUtils {

    /**
     * 根据当前输入的IP计算地址池内下一个可用IP
     * 如果当前IP为null,默认第一次使用，则取地址池内第一个IP作为下一个可用IP
     * 如果最终结果返回NULL，则地址池内可用IP已用完，无可用IP
     * @param ipPool
     * @param currentIp
     * @return
     */
    public static  synchronized String getNextAvailableIp(String ipPool,String currentIp){
        if(StringUtils.isBlank(ipPool)){
            return null;
        }

        if(!IpUtils.isValidIp(ipPool)){
            return null;
        }

        if(StringUtils.isNotBlank(currentIp) && !IpUtils.checkIpRange(currentIp, ipPool)){
            return null;
        }

        if(StringUtils.isBlank(currentIp)){
            String startIp = IpUtils.getStartIpFromIpAddress(ipPool);
            return startIp;
        }

        if(!IpUtils.isValidIp(currentIp)){
            return null;
        }


        if(StringUtils.isNotBlank(ipPool) && StringUtils.isNotBlank(currentIp)){
            Long currentIpValue = IpUtils.IPv4StringToNum(currentIp);
            String availableIp = IpUtils.IPv4NumToString(currentIpValue + 1);
            if(!IpUtils.isValidIp(availableIp)){
                return null;
            }
            if(IpUtils.checkIpRange(availableIp, ipPool)){
                return availableIp;
            }
        }
        return null;

    }

    /**
     * 匹配地址池
     *
     * @param dto
     */
    public static boolean validateOrderAndAddressPool(RuleProcessDTO dto) throws Exception {
        List<PushAutoMappingPoolEntity> natEntities = dto.getAddressPoolDTO().getIpPoolList();
        if (CollectionUtils.isEmpty(natEntities)) {
            return false;
        }
        boolean allMatch = false;
        List<PushAutoMappingPoolEntity> matchPools = new ArrayList<>();
        for (PushAutoMappingPoolEntity pushMappingNatEntity : natEntities) {
            String[] poolPreIps = pushMappingNatEntity.getPreIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            String[] targetIps = null;
            if (RuleTypeTaskEnum.SNAT_MANT_TO_ONE.getCode() == dto.getRuleTypeTaskEnum().getCode() ||
                    RuleTypeTaskEnum.SNAT_ONE_TO_ONE.getCode() == dto.getRuleTypeTaskEnum().getCode()) {
                targetIps = dto.getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            } else if (RuleTypeTaskEnum.DNAT_ONE_TO_ONE.getCode() == dto.getRuleTypeTaskEnum().getCode()) {
                targetIps = dto.getDstIp().split(PolicyConstants.ADDRESS_SEPERATOR);
            }
            QuintupleUtils.Tmp<String> tmp = QuintupleUtils.ipListFilter(Arrays.asList(targetIps), Arrays.asList(poolPreIps));

            // 如果地址池转换前的数据有匹配上，且有剩余,直接退出，给出错误提示
            if (CollectionUtils.isNotEmpty(tmp.getFilterOutData()) && CollectionUtils.isNotEmpty(tmp.getPostFilterData())) {
                break;
            }

            // 如果地址池转换前的数据有匹配上，且没有剩余，直接算匹配上。进入后面流程
            if (CollectionUtils.isNotEmpty(tmp.getFilterOutData()) && CollectionUtils.isEmpty(tmp.getPostFilterData())) {
                matchPools.add(pushMappingNatEntity);
                dto.getAddressPoolDTO().setMatchPools(matchPools);
                allMatch = true;
                break;
            }
        }
        // 如果所有都过完 还是没有匹配上，给出错误提示
        if (!allMatch) {
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        String s = getNextAvailableIp("1.1.1.0/24", "2.1.1.10");
        System.out.println(s);
    }

}
