package com.abtnetworks.totems.vender.abt.security;

import com.abtnetworks.totems.command.line.dto.InterfaceParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.dto.ZoneParamDTO;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class NatAbtImpl extends AclAbtImpl{

    @Override
    public String generateNatPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                     String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                     String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress,
                                                     ServiceParamDTO[] insideServiceParam, ServiceParamDTO[] globalServiceParam,
                                                     ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                     InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                     String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup,
                                                     String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup,
                                                     Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,
                                                ServiceParamDTO[] serviceParam,IpAddressParamDTO postSrcIpAddress,
                                                ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,String eVr,
                                                String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                String[] refServiceObject, String[] refServiceObjectGroup,
                                                String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,
                                                ServiceParamDTO[] serviceParam,IpAddressParamDTO postDstIpAddress,ServiceParamDTO[] postServiceParam,
                                                ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                String[] refServiceObject, String[] refServiceObjectGroup,
                                                String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                   String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                   String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,ServiceParamDTO[] serviceParam,
                                                   IpAddressParamDTO postSrcIpAddress,IpAddressParamDTO postDstIpAddress,ServiceParamDTO[] postServiceParam,
                                                   ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                   InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,String eVr,
                                                   String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                   String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                   String[] refServiceObject, String[] refServiceObjectGroup,
                                                   String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup,
                                                   String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup,
                                                   Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

}
