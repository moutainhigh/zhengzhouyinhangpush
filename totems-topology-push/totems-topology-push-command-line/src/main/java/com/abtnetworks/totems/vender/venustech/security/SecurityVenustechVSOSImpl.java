package com.abtnetworks.totems.vender.venustech.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.common.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class SecurityVenustechVSOSImpl extends OverAllGeneratorAbstractBean {

    private static final String POOL_ADDRESS_TEMPLATE = "ip address %s %s";
    private static final String EXIT = "exit";
    private static final String SRC_IP_SYSTEM = "srcIpSystem";
    private static final String DST_IP_SYSTEM = "dstIpSystem";
    private static final String POST_IP_SYSTEM = "postIpSystem";

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        {
            StringBuilder ipArrayCommandLine = new StringBuilder("");
            if(ObjectUtils.isEmpty(statusTypeEnum) || !statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.ADD.getStatus())){
                return null;
            }
            for (String ip:singleIpArray) {
                ipArrayCommandLine.append(String.format("host-address %s",ip)).append(StringUtils.LF);
            }
            return ipArrayCommandLine.toString();
        }
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipRangeArrayCommandLine = new StringBuilder("");
        if(ObjectUtils.isEmpty(statusTypeEnum) || !statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.ADD.getStatus())){
            return null;
        }
        for(IpAddressRangeDTO ipAddress: rangIpArray){
            ipRangeArrayCommandLine.append(String.format("range-address %s %s",ipAddress.getStart(),ipAddress.getEnd())).append(StringUtils.LF);
        }
        return ipRangeArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        {
            StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
            if(ObjectUtils.isEmpty(statusTypeEnum) || !statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.ADD.getStatus())){
                return null;
            }
            for(IpAddressSubnetIntDTO dto : subnetIpArray){
                subnetIntArrayCommandLine.append(String.format("net-address %s",dto.getIp()+"/"+dto.getMask())).append(StringUtils.LF);
            }
            return subnetIntArrayCommandLine.toString();
        }
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        if(ObjectUtils.isEmpty(statusTypeEnum) || !statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.ADD.getStatus())){
            return null;
        }
        for(IpAddressSubnetStrDTO dto : subnetIpArray){
            subnetStrArrayCommandLine.append(String.format("net-address %s",dto.getIp()+"/"+dto.getMask())).append(StringUtils.LF);
        }
        return subnetStrArrayCommandLine.toString();
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
    public String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateExcludeIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum, String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(singleIpArray)
                && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(fqdnArray)){
//            if(StringUtils.isNotEmpty(name))
//                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum))
//                    return "address6 " + name + "\nquit\n";
//                else
//                    return "address " + name + "\nquit\n";
            return StringUtils.EMPTY;
        }
        StringBuilder commandLine=new StringBuilder("");
//        name = name.replace(":","_");
//        if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
//            commandLine.append("address6").append(String.format(" %s ",name)).append(StringUtils.LF);
//        }else{
//            commandLine.append("address").append(String.format(" %s ",name)).append(StringUtils.LF);
//        }

        commandLine.append("address").append(String.format(" %s ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO: rangIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateRangeIpV6CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }else{
                    commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }
            }

        }
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String ip: singleIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateSingleIpV6CommandLine(statusTypeEnum,new String[]{ip},map, null));
                }else{
                    commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum,new String[]{ip},map, null));
                }
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,null));
                }else{
                    commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,null));
                }
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO: subnetStrIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
//                    commandLine.append(this.generateSubnetStrIpV6CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}));
                }else{
                    commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,map,null));
                }
            }
        }
//        if(ArrayUtils.isNotEmpty(fqdnArray)){
//            for (String fqdnDto : fqdnArray) {
//                if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
//                    commandLine.append(this.generateFqdnIpV4CommandLine(statusTypeEnum,new String[]{fqdnDto}));
//                }
//                //ipv6 不支持域名
//            }
//        }
        commandLine.append("exit").append(StringUtils.LF);
        return commandLine.toString();
    }

    @Override
    public String generateExcludeIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateMacAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, String[] macObjectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateMacAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String getDomainAcl(String strName, String dstName) {
        return null;
    }

    @Override
    public String generateAclName(String aclType, String name, RuleIPTypeEnum ruleIPTypeEnum) {
        return null;
    }

    @Override
    public String generateAclDescription(String description) {
        return null;
    }

    @Override
    public String generateAclPost() {
        return null;
    }

    @Override
    public String generateAclPolicyCommandLine(StatusTypeEnum statusTypeEnum, String aclType, String name, RuleIPTypeEnum ipTypeEnum, String action, String description, IpAddressParamDTO srcIpDto, IpAddressParamDTO dstIpDto, ServiceParamDTO serviceParam, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateNatPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress, ServiceParamDTO[] insideServiceParam, ServiceParamDTO[] globalServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup, String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String outIntName = "any";
        if (outInterface != null && StringUtils.isNotEmpty(outInterface.getName())) {
            outIntName = outInterface.getName();
        }
        String globalIp = "any";
        String insideIp = "any";
        if (globalAddress != null && globalAddress.getSingleIpArray() != null) {
            globalIp = globalAddress.getSingleIpArray()[0];
        }
        if (insideAddress != null && insideAddress.getSingleIpArray() != null) {
            insideIp = insideAddress.getSingleIpArray()[0];
        }
        sb.append("ip nat static ").append(outIntName).append(StringUtils.SPACE).append(globalIp).append(StringUtils.SPACE).append(insideIp).append(StringUtils.LF);
        if(StringUtils.isNotEmpty(description) && StringUtils.isNotEmpty(id)){
            sb.append("ip nat ").append(id).append(StringUtils.SPACE).append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postSrcIpAddress, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String outIntName = "any";
        if (outInterface != null && StringUtils.isNotEmpty(outInterface.getName())) {
            outIntName = outInterface.getName();
        }
        String srcAddressGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, new String[]{SRC_IP_SYSTEM}, sb);
        String dstAddressGroupName = disposeAddress(statusTypeEnum, dstIp, dstRefIpAddressObject, dstRefIpAddressObjectGroup, map, new String[]{DST_IP_SYSTEM}, sb);
        String addressPoolName = disposePool(statusTypeEnum, postSrcIpAddress, postSrcRefIpAddressObject, postSrcRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}, sb);
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam, refServiceObject, refServiceObjectGroup, sb);
        sb.append("ip nat source ").append(outIntName).append(StringUtils.SPACE)
                .append(srcAddressGroupName).append(StringUtils.SPACE)
                .append(dstAddressGroupName).append(StringUtils.SPACE)
                .append(serviceGroupName).append(StringUtils.SPACE);
        if ("any".equals(addressPoolName) || StringUtils.isEmpty(addressPoolName)) {
            sb.append("interface").append(StringUtils.LF);
        } else {
            sb.append(addressPoolName).append(StringUtils.LF);
        }
        if(StringUtils.isNotEmpty(description) && StringUtils.isNotEmpty(id)){
            sb.append("ip nat ").append(id).append(StringUtils.SPACE).append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);

        return sb.toString();
    }

    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postDstIpAddress, ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String srcAddressGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, new String[]{SRC_IP_SYSTEM}, sb);
        String dstAddressGroupName = disposeAddress(statusTypeEnum, dstIp, dstRefIpAddressObject,dstRefIpAddressObjectGroup, map, new String[]{DST_IP_SYSTEM}, sb);
        String addressPoolName = disposePool(statusTypeEnum, postDstIpAddress, postDstRefIpAddressObject, postDstRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}, sb);
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam, refServiceObject, refServiceObjectGroup, sb);

        if ("any".equals(dstAddressGroupName) || StringUtils.isEmpty(dstAddressGroupName)) {
            dstAddressGroupName = "interface";
        }
        sb.append("ip nat destination ").append(StringUtils.isBlank(inInterface.getName()) ? "any" : inInterface.getName()).append(StringUtils.SPACE)
                .append(srcAddressGroupName).append(StringUtils.SPACE)
                .append(dstAddressGroupName).append(StringUtils.SPACE)
                .append(serviceGroupName).append(StringUtils.SPACE)
                .append(addressPoolName).append(StringUtils.SPACE);
        if (map.get("postPort")!=null && StringUtils.isNotEmpty((CharSequence) map.get("postPort")) && !"any".equalsIgnoreCase((String) map.get("postPort"))){
            sb.append("service ").append(map.get("postPort"));
        }
        sb.append(StringUtils.LF);
        if(StringUtils.isNotEmpty(description) && StringUtils.isNotEmpty(id)){
            sb.append("ip nat ").append(id).append(StringUtils.SPACE).append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postSrcIpAddress, IpAddressParamDTO postDstIpAddress, ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String srcAddressGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, new String[]{SRC_IP_SYSTEM}, sb);
        String dstAddressGroupName = disposeAddress(statusTypeEnum, dstIp, dstRefIpAddressObject,dstRefIpAddressObjectGroup, map, new String[]{DST_IP_SYSTEM}, sb);
        String addressSrcPoolName = disposePool(statusTypeEnum, postSrcIpAddress, postSrcRefIpAddressObject, postSrcRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}, sb);
        String addressDstPoolName = disposePool(statusTypeEnum, postDstIpAddress, postDstRefIpAddressObject, postDstRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}, sb);
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam, refServiceObject, refServiceObjectGroup, sb);

//        if ("any".equals(dstAddressGroupName) || StringUtils.isEmpty(dstAddressGroupName)) {
//            dstAddressGroupName = "interface";
//        }
        sb.append("ip nat destination ").append(StringUtils.isBlank(inInterface.getName()) ? "any" : inInterface.getName()).append(StringUtils.SPACE)
                .append(srcAddressGroupName).append(StringUtils.SPACE)
                .append(dstAddressGroupName).append(StringUtils.SPACE)
                .append(serviceGroupName).append(StringUtils.SPACE)
                .append(addressDstPoolName).append(StringUtils.SPACE);
        if (map.get("postPort")!=null && StringUtils.isNotEmpty((CharSequence) map.get("postPort")) && !"any".equalsIgnoreCase((String) map.get("postPort"))){
            sb.append("service ").append(map.get("postPort")).append(StringUtils.SPACE);
        }
        sb.append("src-translate-to ").append(addressSrcPoolName).append(StringUtils.LF);
        if(StringUtils.isNotEmpty(description) && StringUtils.isNotEmpty(id)){
            sb.append("ip nat ").append(id).append(StringUtils.SPACE).append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String deleteNatPolicyByIdOrName(NatTypeEnum natTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generateIpAddressPoolCommandLine(StatusTypeEnum statusTypeEnum, String poolName, IpAddressParamDTO poolIp, String[] poolRefIpAddressObject, String[] poolRefIpAddressObjectGroup, Map<String, Object> map, String[] args) {
        StringBuilder poolCommand = new StringBuilder();
        StringBuilder elementCommand = new StringBuilder();
        if (!ArrayUtils.isEmpty(poolIp.getSingleIpArray())) {
            for (String singleIp : poolIp.getSingleIpArray()) {
                elementCommand.append(String.format("ip address %s %s", singleIp, singleIp)).append(StringUtils.LF);
            }
        }
        if (!ArrayUtils.isEmpty(poolIp.getRangIpArray())) {
            for (IpAddressRangeDTO rangeDTO : poolIp.getRangIpArray()) {
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, rangeDTO.getStart(), rangeDTO.getEnd())).append(StringUtils.LF);
            }
        }
        if (!ArrayUtils.isEmpty(poolIp.getSubnetStrIpArray())) {
            for (IpAddressSubnetStrDTO subnetIp : poolIp.getSubnetStrIpArray()) {
                String Ip = subnetIp.getIp() + "/" + IPUtil.getMaskBitMapByInverseMask(subnetIp.getMask());
                String startIp = IPUtil.getStartIp(Ip);
                String endIp = IPUtil.getEndIp(Ip);
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, startIp, endIp)).append(StringUtils.LF);
            }
        }
        if (!ArrayUtils.isEmpty(poolIp.getSubnetIntIpArray())) {
            for (IpAddressSubnetIntDTO subnetIp : poolIp.getSubnetIntIpArray()) {
                String Ip = subnetIp.getIp() + "/" + subnetIp.getMask();
                String startIp = IPUtil.getStartIp(Ip);
                String endIp = IPUtil.getEndIp(Ip);
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, startIp, endIp)).append(StringUtils.LF);
            }
        }
        if (!StringUtils.isBlank(elementCommand)) {
            poolCommand.append("ip nat pool ").append(poolName).append(StringUtils.LF).append(elementCommand).append("exit").append(StringUtils.LF).append(StringUtils.LF);
        }
        return poolCommand.toString();
    }

    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] service, AbsoluteTimeParamDTO absoluteTimeParamDTO, PeriodicTimeParamDTO periodicTimeParamDTO, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] refTimeObject, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSecurityPolicyModifyCommandLine(StatusTypeEnum statusTypeEnum, EditTypeEnums editType, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] service, AbsoluteTimeParamDTO absoluteTimeParamDTO, PeriodicTimeParamDTO periodicTimeParamDTO, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] refTimeObject, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generatePortRefStrCommandLine(String[] strRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder serviceTCPCommandLine = new StringBuilder("");
//        if(ObjectUtils.isNotEmpty(srcRangePortArray)){
//            defaultPort = String.format("src-port %s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd());
//        }else if(ObjectUtils.isNotEmpty(srcSinglePortArray)){
//            defaultPort = String.format("src-port %s %s",srcSinglePortArray[0],srcSinglePortArray[0]);
//        }else if(ObjectUtils.isNotEmpty(srcSinglePortStrArray)){
//            defaultPort = String.format("src-port %s %s",srcSinglePortStrArray[0],srcSinglePortStrArray[0]);
//        }

        if(ObjectUtils.isNotEmpty(dstRangePortArray)){
            for(PortRangeDTO dto :dstRangePortArray){
                serviceTCPCommandLine.append(String.format("tcp dest %s %s ",dto.getStart(),dto.getEnd())).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortStrArray)){
            for(String dto :dstSinglePortStrArray){
                serviceTCPCommandLine.append(String.format("tcp dest %s ",dto)).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortArray)){
            for(Integer dto :dstSinglePortArray){
                serviceTCPCommandLine.append(String.format("tcp dest %s ",dto)).append(StringUtils.LF);
            }
        }
        if (ObjectUtils.isEmpty(dstRangePortArray) && ObjectUtils.isNotEmpty(dstSinglePortStrArray) &&
                ObjectUtils.isNotEmpty(dstSinglePortArray)){
            serviceTCPCommandLine.append("tcp dest 1 65535");
        }

        return serviceTCPCommandLine.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder serviceUDPCommandLine = new StringBuilder("");
//        if(ObjectUtils.isNotEmpty(srcRangePortArray)){
//            defaultPort = String.format("src-port %s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd());
//        }else if(ObjectUtils.isNotEmpty(srcSinglePortArray)){
//            defaultPort = String.format("src-port %s %s",srcSinglePortArray[0],srcSinglePortArray[0]);
//        }else if(ObjectUtils.isNotEmpty(srcSinglePortStrArray)){
//            defaultPort = String.format("src-port %s %s",srcSinglePortStrArray[0],srcSinglePortStrArray[0]);
//        }

        if(ObjectUtils.isNotEmpty(dstRangePortArray)){
            for(PortRangeDTO dto :dstRangePortArray){
                serviceUDPCommandLine.append(String.format("udp dest %s %s ",dto.getStart(),dto.getEnd())).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortStrArray)){
            for(String dto :dstSinglePortStrArray){
                serviceUDPCommandLine.append(String.format("udp dest %s ",dto)).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortArray)){
            for(Integer dto :dstSinglePortArray){
                serviceUDPCommandLine.append(String.format("udp dest %s ",dto)).append(StringUtils.LF);
            }
        }
        if (ObjectUtils.isEmpty(dstRangePortArray) && ObjectUtils.isNotEmpty(dstSinglePortStrArray) &&
                ObjectUtils.isNotEmpty(dstSinglePortArray)){
            serviceUDPCommandLine.append("udp dest 1 65535");
        }
        return serviceUDPCommandLine.toString();
    }

    @Override
    public String generateTCP_UDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateOtherCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateServiceObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, String id, String attachStr, List<ServiceParamDTO> serviceParamDTOList, String description, String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateServiceObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String id, String attachStr, List<ServiceParamDTO> serviceParamDTOList, String description, Map<String, Object> map, String[] args) throws Exception {
        if(CollectionUtils.isEmpty(serviceParamDTOList)){
            return StringUtils.EMPTY;
        }
        if(!StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            return null;
        }
        StringBuilder serviceObjectCommandLine = new StringBuilder("");
        if (!"any".equalsIgnoreCase(name)){
            serviceObjectCommandLine.append("service").append(String.format(" %s ",name)).append(StringUtils.LF);
        }
        for(ServiceParamDTO serviceParamDTO : serviceParamDTOList){
            if(ProtocolTypeEnum.PROTOCOL.equals(serviceParamDTO.getProtocol())) {
//                serviceObjectCommandLine.append("protocol 58").append(StringUtils.LF);
            }else if(ProtocolTypeEnum.ICMP.equals(serviceParamDTO.getProtocol())){
                serviceObjectCommandLine.append("icmp 8").append(StringUtils.LF);
            }else if(ProtocolTypeEnum.TCP.equals(serviceParamDTO.getProtocol())){
                serviceObjectCommandLine.append(this.generateTCPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, null, null));
            }else if(ProtocolTypeEnum.UDP.equals(serviceParamDTO.getProtocol())){
                serviceObjectCommandLine.append(this.generateUDPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, null, null));
            }
        }
        if (!"any".equalsIgnoreCase(name)){
            serviceObjectCommandLine.append("exit").append(StringUtils.LF);
        }
        return serviceObjectCommandLine.toString();
    }

    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generatePeriodicTimeCommandLine(String name, String attachStr, PeriodicTimeParamDTO periodicTimeParamDTO, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "enable\nconfigure terminal\n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("end").append(StringUtils.LF);
        sb.append("write file").append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String createIpAddressObjectNameByParamDTO(String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] hosts, String[] objectNameRefArray, Map<String, Object> map, String[] args) {
        if(ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(hosts) && ArrayUtils.isEmpty(objectNameRefArray)){
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.isNotEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(hosts) && ArrayUtils.isEmpty(objectNameRefArray)){
            if(singleIpArray.length == 1){
                return String.format("add_%s",singleIpArray[0]);
            } else {
                int hashNum = 0;
                for (String singleIp : singleIpArray) {
                    hashNum += singleIp.hashCode();
                }
                return String.format("add_%s",Math.abs(hashNum));
            }
        }
        if(ArrayUtils.isNotEmpty(hosts) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(objectNameRefArray)){
            if(hosts.length == 1){
                return String.format("add_%s",hosts[0]);
            } else {
                int hashNum = 0;
                for (String host : hosts) {
                    hashNum += host.hashCode();
                }
                return String.format("add_%s",Math.abs(hashNum));
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray) && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray)  && ArrayUtils.isEmpty(hosts) && ArrayUtils.isEmpty(objectNameRefArray)){
            if(rangIpArray.length == 1){
                if(rangIpArray[0].getEnd().contains(":")){
                    String end = rangIpArray[0].getEnd();
                    end = end.substring(end.indexOf(":")+1);
                    return String.format("add_%s_%s",rangIpArray[0].getStart(),end);
                }
                return String.format("add_%s_%s",rangIpArray[0].getStart(),rangIpArray[0].getEnd());
            } else {
                int hashNum = 0;
                for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                    hashNum += (ipAddressRangeDTO.getStart().hashCode() + ipAddressRangeDTO.getEnd().hashCode());
                }
                return String.format("add_%s",Math.abs(hashNum));
            }
        }
        if((ArrayUtils.isNotEmpty(subnetIntIpArray) || ArrayUtils.isNotEmpty(subnetStrIpArray))
                && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(objectNameRefArray) && ArrayUtils.isEmpty(hosts) ){
            if(ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) == 1){
                if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
                    return String.format("add_%s_%s",subnetIntIpArray[0].getIp(),subnetIntIpArray[0].getMask());
                } else {
                    return String.format("add_%s_%s",subnetStrIpArray[0].getIp(), TotemsIpUtils.getMaskBit(subnetStrIpArray[0].getMask()));
                }
            }
            int hashNum = 0;
            if(subnetIntIpArray != null){
                for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                    hashNum +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
                }
            }
            if(subnetStrIpArray != null){
                for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                    hashNum +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
                }
            }
            return String.format("add_%s",Math.abs(hashNum));
        }

        StringBuffer ipAddressName = new StringBuffer("add_");
        int num = 0;
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String singeIp : singleIpArray) {
                num += singeIp.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                num += (ipAddressRangeDTO.getStart().hashCode() + ipAddressRangeDTO.getEnd().hashCode());
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                num +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                num +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
            }
        }
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName : objectNameRefArray) {
                num += objectName.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(hosts)){
            for (String host : hosts) {
                num += host.hashCode();
            }
        }
        ipAddressName.append(Math.abs(num));
        return ipAddressName.toString();
    }


    private String disposeAddress(StatusTypeEnum statusTypeEnum, IpAddressParamDTO srcIp, String[] srcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder sb) throws Exception {
        if (!ArrayUtils.isEmpty(postSrcRefIpAddressObjectGroup) && postSrcRefIpAddressObjectGroup.length > 0 && StringUtils.isNotEmpty(postSrcRefIpAddressObjectGroup[0])) {
            return postSrcRefIpAddressObjectGroup[0];
        }
//        if (!ArrayUtils.isEmpty(srcRefIpAddressObject) && srcRefIpAddressObject.length>0 && StringUtils.isNotEmpty(srcRefIpAddressObject[0])) {
//            return srcRefIpAddressObject[0];
//        }
        String srcAddressGroupName = null;
        String srcAddressName = null;
        String srcCommandline = null;
        String ipSystem = null;
        if (map != null && args != null && map.get(args[0]) != null && StringUtils.isNotEmpty((CharSequence) map.get(args[0]))) {
            ipSystem = (String) map.get(args[0]);
        }
        if (ObjectUtils.isNotEmpty(srcIp)) {
            if (ArrayUtils.isEmpty(srcRefIpAddressObject) && StringUtils.isNotEmpty(ipSystem)) {
                srcAddressName = ipSystem;
            } else {
                srcAddressName = this.createIpAddressObjectNameByParamDTO(srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null, null, null, null);
            }
            srcCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIp.getIpTypeEnum(), srcAddressName, null, srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null,
                    null, null, null, null, null, map, args);
        }
        //地址对象 组装为地址组对象
        StringBuilder srcAddressGroupCommandline = new StringBuilder("");

        if (ArrayUtils.isNotEmpty(srcRefIpAddressObject) || StringUtils.isNotEmpty(srcAddressName)) {
            //只有一个地址对象时，直接引用不再创建地址组
            if (ArrayUtils.isNotEmpty(srcRefIpAddressObject) && srcRefIpAddressObject.length == 1 && StringUtils.isEmpty(srcAddressName)) {
                srcAddressGroupName = srcRefIpAddressObject[0].replace(":", "_");
            } else if (ArrayUtils.isEmpty(srcRefIpAddressObject) && StringUtils.isNotEmpty(srcAddressName)) {
                srcAddressGroupName = srcAddressName.replace(":", "_");
            } else {
                if (ipSystem != null) {
                    srcAddressGroupName = ipSystem;
                } else if (srcIp != null) {
                    srcAddressGroupName = this.createIpAddressObjectGroupName(srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null, srcIp.getHosts(), srcRefIpAddressObject, srcIp.getObjectGroupNameRefArray(), null, null);
                } else {
                    srcAddressGroupName = this.createIpAddressObjectGroupName(null, null, null, null, null, null, srcRefIpAddressObject, null, null, null);
                }
                srcAddressGroupCommandline.append(String.format("address-group %s", srcAddressGroupName)).append(StringUtils.LF);
                if (ArrayUtils.isNotEmpty(srcRefIpAddressObject)) {
                    for (String srcAddress : srcRefIpAddressObject) {
                        srcAddressGroupCommandline.append(String.format("address-object %s", srcAddress.replace(":", "_"))).append(StringUtils.LF);
                    }
                }
                if (StringUtils.isNotEmpty(srcCommandline)) {
                    srcAddressName = srcAddressName.replace(":", "_");
                    srcAddressGroupCommandline.append(String.format("address-object %s", srcAddressName)).append(StringUtils.LF);
                }
                srcAddressGroupCommandline.append("exit").append(StringUtils.LF);
            }
        }

        if (StringUtils.isNotEmpty(srcCommandline)) {
            sb.append(srcCommandline).append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(srcAddressGroupCommandline)) {
            sb.append(srcAddressGroupCommandline).append(StringUtils.LF);
        }
        return StringUtils.isBlank(srcAddressGroupName) ? "any" : srcAddressGroupName;

    }

    /**
     * @return ip nat pool pool1
     * ip address 1.1.1.2 1.1.1.10
     * ip address 3.3.3.3 3.3.3.3
     * quit
     */
    private String disposePool(StatusTypeEnum statusTypeEnum, IpAddressParamDTO dstIp,String[] postSrcRefIpAddressObject, String[] dstRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder sb) {
        if (!ArrayUtils.isEmpty(dstRefIpAddressObjectGroup) && dstRefIpAddressObjectGroup.length>0 && StringUtils.isNotEmpty(dstRefIpAddressObjectGroup[0])) {
            return dstRefIpAddressObjectGroup[0];
        }
        if (!ArrayUtils.isEmpty(postSrcRefIpAddressObject) && postSrcRefIpAddressObject.length>0 && StringUtils.isNotEmpty(postSrcRefIpAddressObject[0])) {
            return postSrcRefIpAddressObject[0];
        }
        String ipSystem = null;
        if (map != null && args != null && map.get(args[0]) != null && StringUtils.isNotEmpty((CharSequence) map.get(args[0]))) {
            ipSystem = (String) map.get(args[0]);
        }
        String poolName = "";
        if (dstIp != null) {
            if (StringUtils.isNotEmpty(ipSystem)) {
                poolName = ipSystem;
            } else {
                poolName = this.createIpAddressObjectNameByParamDTO(dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray(), null, null, null, null);
            }
            sb.append(generateIpAddressPoolCommandLine(statusTypeEnum, poolName, dstIp, null, dstRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}));
        }
        return StringUtils.isBlank(poolName) ? "any" : poolName;
    }

    private String disposeService(StatusTypeEnum statusTypeEnum, ServiceParamDTO[] serviceParam, String[] refServiceObject, String[] refServiceObjectGroup, StringBuilder sb) throws Exception {
        if (!ArrayUtils.isEmpty(refServiceObjectGroup) && refServiceObjectGroup.length > 0) {
            return StringUtils.isBlank(refServiceObjectGroup[0]) ? "any" : refServiceObjectGroup[0];
        }
//        if (!ArrayUtils.isEmpty(refServiceObject) && refServiceObject.length>0 && StringUtils.isNotEmpty(refServiceObject[0])) {
//            return refServiceObject[0];
//        }
        String serviceGroupName = null;
        //判断是否为预定义服务
        String serviceName = null;
        String serviceCommandline = null;
        //服务对象
        if (ArrayUtils.isNotEmpty(serviceParam)) {
            serviceName = this.createServiceObjectName(Arrays.asList(serviceParam), null, null);
            serviceCommandline = this.generateServiceObjectCommandLine(statusTypeEnum, serviceName, null, null, Arrays.asList(serviceParam), null, null, null);
        }
        //服务对象组装为组
        StringBuilder serviceGroupCommandline = new StringBuilder("");
        if (ArrayUtils.isNotEmpty(refServiceObject) || ObjectUtils.isNotEmpty(serviceName)) {
            if (ArrayUtils.isNotEmpty(refServiceObject) && refServiceObject.length == 1 && StringUtils.isEmpty(serviceName)) {
                serviceGroupName = refServiceObject[0];
            } else if (ArrayUtils.isEmpty(refServiceObject) && StringUtils.isNotEmpty(serviceName)) {
                serviceGroupName = serviceName;
            } else {
                serviceGroupName = this.createServiceObjectGroupName(null, refServiceObject, refServiceObjectGroup, null, null);
                serviceGroupCommandline.append(String.format("service-group %s", serviceGroupName)).append(StringUtils.LF);
                if (ArrayUtils.isNotEmpty(refServiceObject)) {
                    for (String serivce : refServiceObject) {
                        serviceGroupCommandline.append(String.format("service-object %s", serivce)).append(StringUtils.LF);
                    }
                }
                if (ObjectUtils.isNotEmpty(serviceName)) {
                    serviceGroupCommandline.append(String.format("service-object %s", serviceName)).append(StringUtils.LF);
                }
                serviceGroupCommandline.append("exit").append(StringUtils.LF);
            }
        }
        if (StringUtils.isNotEmpty(serviceCommandline)) {
            sb.append(serviceCommandline).append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(serviceGroupCommandline)) {
            sb.append(serviceGroupCommandline).append(StringUtils.LF);
        }
        return serviceGroupName == null ? "any" : serviceGroupName;
    }

}
