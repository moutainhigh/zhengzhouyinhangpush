package com.abtnetworks.totems.vender.cisco.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * @Author: lb
 * @Description Cisco IOS 命令行
 * @Date: 2021/7/16
 */
public class SecurityCiscoIOSImpl extends OverAllGeneratorAbstractBean {
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
        return null;
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
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] service, AbsoluteTimeParamDTO absoluteTimeParamDTO, PeriodicTimeParamDTO periodicTimeParamDTO, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] refTimeObject, Map<String, Object> map, String[] args) throws Exception {
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
        return null;
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return null;
    }
}
