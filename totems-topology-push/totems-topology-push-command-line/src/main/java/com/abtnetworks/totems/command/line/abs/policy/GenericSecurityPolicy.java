package com.abtnetworks.totems.command.line.abs.policy;

import com.abtnetworks.totems.command.line.dto.PolicyEditParamDTO;
import com.abtnetworks.totems.command.line.dto.PolicyParamDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.policy.SecurityPolicyInterface;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @Version
 * @Description
 * @Created by hw on '2021/4/16 14:09'.
 */
public abstract class GenericSecurityPolicy extends GenericFilterListBean implements SecurityPolicyInterface {

    public String createSecurityPolicyName(List<String> list, Map<String, Object> map, String[] args) {
        String name = null;
        if (list != null && list.size() > 1) {

        } else {
            logger.error("createSecurityPolicyName()：对象名参数为空");
        }
        return null;
    }

    /**
     * 生成安全策略 命令行 入口 映射 方法
     * @param statusTypeEnum 状态类型
     * @param paramDTO 入参
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, PolicyParamDTO paramDTO, Map<String, Object> map, String[] args) throws Exception {
        return this.generateSecurityPolicyCommandLine(statusTypeEnum, paramDTO.getGroupName(), paramDTO.getName(), paramDTO.getId(), paramDTO.getAction(),
                paramDTO.getDescription(), paramDTO.getLogFlag(), paramDTO.getAgeingTime(), paramDTO.getRefVirusLibrary(), paramDTO.getMoveSeatEnum(), paramDTO.getSwapRuleNameId(),
                paramDTO.getSrcIp(), paramDTO.getDstIp(), paramDTO.getServiceParam(), paramDTO.getAbsoluteTimeParamDTO(),paramDTO.getPeriodicTimeParamDTO(), paramDTO.getSrcZone(), paramDTO.getDstZone(), paramDTO.getInInterface(), paramDTO.getOutInterface(),
                paramDTO.getSrcRefIpAddressObject(), paramDTO.getSrcRefIpAddressObjectGroup(), paramDTO.getDstRefIpAddressObject(), paramDTO.getDstRefIpAddressObjectGroup(),
                paramDTO.getRefServiceObject(), paramDTO.getRefServiceObjectGroup(), paramDTO.getRefTimeObject(), map, args);
    }

    /**
     * 生成编辑安全策略 命令行 入口 映射 方法
     * @param statusTypeEnum 状态类型
     * @param paramDTO 入参
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    public String generateSecurityPolicyModifyCommandLine(StatusTypeEnum statusTypeEnum, PolicyEditParamDTO paramDTO, Map<String, Object> map, String[] args) throws Exception {
        return this.generateSecurityPolicyModifyCommandLine(statusTypeEnum,paramDTO.getEditTypeEnums(), paramDTO.getGroupName(), paramDTO.getName(), paramDTO.getId(), paramDTO.getAction(),
                paramDTO.getDescription(), paramDTO.getLogFlag(), paramDTO.getAgeingTime(), paramDTO.getRefVirusLibrary(), paramDTO.getMoveSeatEnum(), paramDTO.getSwapRuleNameId(),
                paramDTO.getSrcIp(), paramDTO.getDstIp(), paramDTO.getServiceParam(), paramDTO.getAbsoluteTimeParamDTO(),paramDTO.getPeriodicTimeParamDTO(), paramDTO.getSrcZone(), paramDTO.getDstZone(), paramDTO.getInInterface(), paramDTO.getOutInterface(),
                paramDTO.getSrcRefIpAddressObject(), paramDTO.getSrcRefIpAddressObjectGroup(), paramDTO.getDstRefIpAddressObject(), paramDTO.getDstRefIpAddressObjectGroup(),
                paramDTO.getRefServiceObject(), paramDTO.getRefServiceObjectGroup(), paramDTO.getRefTimeObject(), map, args);
    }

    /**
     * 生成策略集命令行
     * @param policyGroupName
     * @param description
     * @param map
     * @param args
     * @return
     */
    public String generatePolicyGroupCommandLine(String policyGroupName,String description,Map<String, Object> map, String[] args){
        return StringUtils.LF;
    }

}
