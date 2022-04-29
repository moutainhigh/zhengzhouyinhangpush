package com.abtnetworks.totems.command.line.abs.policy;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.policy.NatPolicyInterface;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:09'.
 */
public abstract class GenericNatPolicy extends GenericACLPolicy implements NatPolicyInterface {

    /**
     * 生成nat策略 命令行
     * @param statusTypeEnum 状态类型
     * @param natPolicyParamDTO static nat参数
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     */

    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, NatPolicyParamDTO natPolicyParamDTO,Map<String, Object> map, String[] args) throws Exception {
        return this.generateStaticNatPolicyCommandLine(statusTypeEnum,natPolicyParamDTO.getGroupName(),natPolicyParamDTO.getName(), natPolicyParamDTO.getId(),
                natPolicyParamDTO.getAction(), natPolicyParamDTO.getDescription(), natPolicyParamDTO.getLogFlag(), natPolicyParamDTO.getAgeingTime(),
                natPolicyParamDTO.getRefVirusLibrary(), natPolicyParamDTO.getMoveSeatEnum(), natPolicyParamDTO.getSwapRuleNameId(),
                natPolicyParamDTO.getInsideAddress(), natPolicyParamDTO.getGlobalAddress(), natPolicyParamDTO.getInsideServiceParam(),
                natPolicyParamDTO.getGlobalServiceParam(), natPolicyParamDTO.getSrcZone(), natPolicyParamDTO.getDstZone(),
                natPolicyParamDTO.getInInterface(), natPolicyParamDTO.getOutInterface(),
                natPolicyParamDTO.getInsideRefIpAddressObject(), natPolicyParamDTO.getInsideRefIpAddressObjectGroup(),
                natPolicyParamDTO.getGlobalRefIpAddressObject(), natPolicyParamDTO.getGlobalRefIpAddressObjectGroup(),
                map,args);
    }

    /**
     * 生成源NAT策略命令行
     * @param statusTypeEnum
     * @param sNatPolicyParamDTO
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, NatPolicyParamDTO sNatPolicyParamDTO,
                                                 Map<String, Object> map, String[] args) throws Exception {
        return this.generateSNatPolicyCommandLine(statusTypeEnum,sNatPolicyParamDTO.getGroupName(),sNatPolicyParamDTO.getName(),sNatPolicyParamDTO.getId(),
                sNatPolicyParamDTO.getAction(),sNatPolicyParamDTO.getDescription(),sNatPolicyParamDTO.getLogFlag(),sNatPolicyParamDTO.getAgeingTime(),
                sNatPolicyParamDTO.getRefVirusLibrary(),sNatPolicyParamDTO.getMoveSeatEnum(),sNatPolicyParamDTO.getSwapRuleNameId(),
                sNatPolicyParamDTO.getSrcIp(),sNatPolicyParamDTO.getDstIp(),sNatPolicyParamDTO.getServiceParam(),sNatPolicyParamDTO.getPostSrcIpAddress(),
                sNatPolicyParamDTO.getSrcZone(),sNatPolicyParamDTO.getDstZone(),sNatPolicyParamDTO.getInInterface(),sNatPolicyParamDTO.getOutInterface(),sNatPolicyParamDTO.getEVr(),
                sNatPolicyParamDTO.getSrcRefIpAddressObject(),sNatPolicyParamDTO.getSrcRefIpAddressObjectGroup(),
                sNatPolicyParamDTO.getDstRefIpAddressObject(),sNatPolicyParamDTO.getDstRefIpAddressObjectGroup(),
                sNatPolicyParamDTO.getRefServiceObject(),sNatPolicyParamDTO.getRefServiceObjectGroup(),
                sNatPolicyParamDTO.getPostSrcRefIpAddressObject(),sNatPolicyParamDTO.getPostSrcRefIpAddressObjectGroup(),map,args);
    }

    /**
     * 生成目的NAT策略命令行
     * @param statusTypeEnum
     * @param dNatPolicyParamDTO
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, NatPolicyParamDTO dNatPolicyParamDTO,
                                                 Map<String, Object> map, String[] args) throws Exception {
        return this.generateDNatPolicyCommandLine(statusTypeEnum,dNatPolicyParamDTO.getGroupName(),dNatPolicyParamDTO.getName(),dNatPolicyParamDTO.getId(),
                dNatPolicyParamDTO.getAction(),dNatPolicyParamDTO.getDescription(),dNatPolicyParamDTO.getLogFlag(),dNatPolicyParamDTO.getAgeingTime(),
                dNatPolicyParamDTO.getRefVirusLibrary(),dNatPolicyParamDTO.getMoveSeatEnum(),dNatPolicyParamDTO.getSwapRuleNameId(),
                dNatPolicyParamDTO.getSrcIp(),dNatPolicyParamDTO.getDstIp(),dNatPolicyParamDTO.getServiceParam(),
                dNatPolicyParamDTO.getPostDstIpAddress(),dNatPolicyParamDTO.getPostServiceParam(),
                dNatPolicyParamDTO.getSrcZone(),dNatPolicyParamDTO.getDstZone(),
                dNatPolicyParamDTO.getInInterface(),dNatPolicyParamDTO.getOutInterface(),
                dNatPolicyParamDTO.getSrcRefIpAddressObject(),dNatPolicyParamDTO.getSrcRefIpAddressObjectGroup(),
                dNatPolicyParamDTO.getDstRefIpAddressObject(),dNatPolicyParamDTO.getDstRefIpAddressObjectGroup(),
                dNatPolicyParamDTO.getRefServiceObject(),dNatPolicyParamDTO.getRefServiceObjectGroup(),
                dNatPolicyParamDTO.getPostDstRefIpAddressObject(),dNatPolicyParamDTO.getPostDstRefIpAddressObjectGroup(),
                map,args);
    }

    /**
     * 生成bothNAT策略命令行
     * @param statusTypeEnum
     * @param bothNatPolicyParamDTO
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, NatPolicyParamDTO bothNatPolicyParamDTO,
                                                    Map<String, Object> map, String[] args) throws Exception {
        return this.generateBothNatPolicyCommandLine(statusTypeEnum,bothNatPolicyParamDTO.getGroupName(),bothNatPolicyParamDTO.getName(),bothNatPolicyParamDTO.getId(),bothNatPolicyParamDTO.getAction(),
                bothNatPolicyParamDTO.getDescription(),bothNatPolicyParamDTO.getLogFlag(),bothNatPolicyParamDTO.getAgeingTime(),
                bothNatPolicyParamDTO.getRefVirusLibrary(),bothNatPolicyParamDTO.getMoveSeatEnum(),bothNatPolicyParamDTO.getSwapRuleNameId(),
                bothNatPolicyParamDTO.getSrcIp(),bothNatPolicyParamDTO.getDstIp(),bothNatPolicyParamDTO.getServiceParam(),
                bothNatPolicyParamDTO.getPostSrcIpAddress(),bothNatPolicyParamDTO.getPostDstIpAddress(),bothNatPolicyParamDTO.getPostServiceParam(),
                bothNatPolicyParamDTO.getSrcZone(),bothNatPolicyParamDTO.getDstZone(),bothNatPolicyParamDTO.getInInterface(),bothNatPolicyParamDTO.getOutInterface(),bothNatPolicyParamDTO.getEVr(),
                bothNatPolicyParamDTO.getSrcRefIpAddressObject(),bothNatPolicyParamDTO.getSrcRefIpAddressObjectGroup(),
                bothNatPolicyParamDTO.getDstRefIpAddressObject(),bothNatPolicyParamDTO.getDstRefIpAddressObjectGroup(),
                bothNatPolicyParamDTO.getRefServiceObject(),bothNatPolicyParamDTO.getRefServiceObjectGroup(),
                bothNatPolicyParamDTO.getPostSrcRefIpAddressObject(),bothNatPolicyParamDTO.getPostSrcRefIpAddressObjectGroup(),
                bothNatPolicyParamDTO.getPostDstRefIpAddressObject(),bothNatPolicyParamDTO.getPostDstRefIpAddressObjectGroup(),
                map,args);
    }

}
