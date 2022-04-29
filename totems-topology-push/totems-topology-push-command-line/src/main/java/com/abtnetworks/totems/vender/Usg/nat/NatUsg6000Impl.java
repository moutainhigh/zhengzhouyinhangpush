package com.abtnetworks.totems.vender.Usg.nat;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author lifei
 * @desc 华为6000 nat命令行生成
 * @date 2021/6/30 17:32
 */
public class NatUsg6000Impl extends SecurityUsg6000Impl {

    @Override
    public String generateNatPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress, ServiceParamDTO[] insideServiceParam, ServiceParamDTO[] globalServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup, String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postSrcIpAddress, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postDstIpAddress, ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postSrcIpAddress, IpAddressParamDTO postDstIpAddress, ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteNatPolicyByIdOrName(NatTypeEnum natTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        return null;
    }


    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuffer preCommandline = new StringBuffer();
        if (null != isVsys && isVsys && StringUtils.isNotBlank(vsysName)) {
            preCommandline.append("switch vsys " + vsysName + "\n");
        }
        preCommandline.append("system-view\n");
        return preCommandline.toString();
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "return\n";
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotBlank(name)) {
            return String.format("undo ip address-set %s %s", name, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isNotEmpty(groupName)){
            return String.format("undo nat address-group %s %s" ,groupName, StringUtils.LF);
        }else {
            return null;
        }
    }
}
