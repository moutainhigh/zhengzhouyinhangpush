package com.abtnetworks.totems.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityUsg6000;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Deprecated
public class U6000ForSDNX implements NatPolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(U6000ForSDNX.class);

    private final String SOURCE_ADDRESS = "source-address";

    private final String DESTINATION_ADDRESS = "destination-address";

    private final int V500_MAX_NAME_LENGTH = 63;

    private final int MAX_NAME_LENGTH = 63;

    private static Integer index = 0;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate u6000 nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {

        StringBuilder sb = new StringBuilder();

        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        sb.append("nat server ");
        sb.append(policyDTO.getTheme() + " ");

        if(!AliStringUtils.isEmpty(policyDTO.getToZone())) {
            sb.append(" zone ");
            sb.append(policyDTO.getToZone());
            sb.append(" ");
        }

        String protocol = ProtocolUtils.getProtocolByString(policyDTO.getProtocol()).toLowerCase();
        if(!AliStringUtils.isEmpty(protocol) && !protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            sb.append("protocol " + protocol.toLowerCase() + " ");
        }

        sb.append("global ");

        String insideAddress = getIpString(policyDTO.getInsideAddress());
        String globalAddress = getIpString(policyDTO.getGlobalAddress());

        //port字串会自带前面的空格，因为port有可能为空不填，若在后续组成协议中写空格，则会出现端口为空的时候多个空格的情况，不好看
        String globalPort = getPort(protocol, policyDTO.getGlobalPort());
        String insidePort = getPort(protocol, policyDTO.getInsidePort());

        sb.append(globalAddress + globalPort + " inside " + insideAddress + insidePort );

        sb.append("\n");
        sb.append("quit\nreturn\n");
        return sb.toString();
    }

    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), SOURCE_ADDRESS, createObjFlag, policyDTO.getSrcAddressObjectName(), policyDTO.getSrcIpSystem(),0);
        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), DESTINATION_ADDRESS, createObjFlag, policyDTO.getDstAddressObjectName(),policyDTO.getDstIpSystem(),0);
        PolicyObjectDTO serviceObject = generateServiceObject(policyDTO.getServiceList(), createObjFlag, policyDTO.getServiceObjectName(),0);
        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),policyDTO.getPostSrcIpSystem(),policyDTO.getPostAddressObjectName());


        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        sb.append(natObject.getCommandLine());


        sb.append("nat-policy\n");
        String name = policyDTO.getTheme();
        sb.append(String.format("rule name %s\n", name));

        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", policyDTO.getSrcZone()));
        }

        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", policyDTO.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
                sb.append(srcAddressObject.getCommandLine());
            }
        }

        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
                sb.append(dstAddressObject.getCommandLine());
            }
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("action source-nat address-group %s\n", natObject.getName()));
        //rule之后添加quit
        sb.append("quit\n");

        sb.append("return\n");

        return sb.toString();
    }

    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), SOURCE_ADDRESS, createObjFlag, policyDTO.getSrcAddressObjectName(), policyDTO.getSrcIpSystem(),0);

        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), DESTINATION_ADDRESS, createObjFlag, policyDTO.getDstAddressObjectName(),policyDTO.getDstIpSystem(),0);

        PolicyObjectDTO serviceObject = generateServiceObject(policyDTO.getServiceList(), createObjFlag, policyDTO.getServiceObjectName(), 0);
//        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostIpAddress(), policyDTO.getTheme());
        String dstNatAddress = "";
        if(AliStringUtils.isEmpty(policyDTO.getPostAddressObjectName())){
         dstNatAddress  = policyDTO.getDstIp() == null?"":policyDTO.getPostIpAddress().split(",")[0];
        }else {
            dstNatAddress = policyDTO.getPostAddressObjectName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        sb.append("nat-policy\n");
        String name = policyDTO.getTheme();
        sb.append(String.format("rule name %s\n", name));

        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", policyDTO.getSrcZone()));
        }

        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", policyDTO.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
                sb.append(srcAddressObject.getCommandLine());
            }
        }

        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
                sb.append(dstAddressObject.getCommandLine());
            }
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("action destination-nat address %s\n", dstNatAddress));
        //rule之后添加quit
        sb.append("quit\n");

        sb.append("return\n");

        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        SecurityUsg6000 usg6000 = new SecurityUsg6000();
        PolicyObjectDTO srcAddressObject = usg6000.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), SOURCE_ADDRESS, createObjFlag, policyDTO.getSrcAddressObjectName(),null,0);

        PolicyObjectDTO dstAddressObject = usg6000.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), DESTINATION_ADDRESS, createObjFlag, policyDTO.getDstAddressObjectName(), null,0);

        PolicyObjectDTO serviceObject = usg6000.generateServiceObject(policyDTO.getServiceList(), createObjFlag, policyDTO.getServiceObjectName(), 0);
        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(),null,null);
        String dstNatAddress = policyDTO.getDstIp() == null?"":policyDTO.getPostDstIp().split(",")[0];

        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        sb.append(natObject.getCommandLine());


        sb.append("nat-policy\n");
        String name = policyDTO.getTheme();
        sb.append(String.format("rule name %s\n", name));

        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", policyDTO.getSrcZone()));
        }

        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", policyDTO.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
                sb.append(srcAddressObject.getCommandLine());
            }
        }

        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
                sb.append(dstAddressObject.getCommandLine());
            }
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("action source-nat address-group %s\n", natObject.getName()));
        sb.append(String.format("action destination-nat static dress-to-dress address %s\n", dstNatAddress));
        //rule之后添加quit
        sb.append("quit\n");

        sb.append("return\n");

        return sb.toString();
    }

    private String getPort(String protocol, String port) {
        if (AliStringUtils.isEmpty(protocol) || protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            return "";
        }  else if (protocol.equalsIgnoreCase("ICMP")) {
            return "";
        } else if(AliStringUtils.isEmpty(port)) {
            return " any";
        } else if(PortUtils.isPortRange(port)) {
            String startPort = PortUtils.getStartPort(port);
            String endPort = PortUtils.getEndPort(port);
            return " " + startPort + " " + endPort;
        }
        return " " + port;
    }

    private String getIpString(String ipAddresses){
        if(AliStringUtils.isEmpty(ipAddresses)) {
            return "";
        }

        String[] ipAddressList = ipAddresses.split(",");
        String ipAddress = ipAddressList[0];

        if(IpUtils.isIP(ipAddress)) {
            return ipAddress;
        }
        String startIpAddress = IpUtils.getStartIpFromIpAddress(ipAddress);
        String endIpAddress = IpUtils.getEndIpFromIpAddress(ipAddress);

        return startIpAddress + " " + endIpAddress;
    }

    PolicyObjectDTO generateNatObject(String addressString, String theme,String ipSystem,String postIpAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        String name = "";
        if(AliStringUtils.isEmpty(postIpAddressName)){
            if(AliStringUtils.isEmpty(ipSystem)){
                name = String.format("%s_NO_%s",theme, IdGen.getRandomNumberString());
            }else {
                name = ipSystem;
            }


            sb.append(String.format("nat address-group %s\n", name));
            sb.append("mode pat\n");

            String[] addresses = addressString.split(",");

            for(String address: addresses) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromIpAddress(address);
                    String endIp = IpUtils.getEndIpFromIpAddress(address);
                    sb.append(String.format("section %s %s\n", startIp, endIp) );
                } else if (IpUtils.isIPSegment(address)) {
                    String startIp = IpUtils.getStartIpFromIpAddress(address);
                    String endIp = IpUtils.getEndIpFromIpAddress(address);
                    sb.append(String.format("section %s %s\n", startIp, endIp) );
                } else {
                    sb.append("section " + address + "\n");
                }
            }

            sb.append("quit\n\n");
        }else {
            name = postIpAddressName;
        }
        dto.setName(name);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem,
                                                 Integer ipType) {
        if(!AliStringUtils.isEmpty(ipAddress)){
            if(IpUtils.isIPRange(ipAddress) || ipAddress.split(",").length>1){
                createObjFlag = true;
            }
        }

        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin("");
            dto.setCommandLine("");
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(ipPrefix + " address-set " + existsAddressName + " \n");
            return dto;
        }

        int maxObjectNameLength = V500_MAX_NAME_LENGTH;

        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");
        // 是创建对象
        if (createObjFlag) {
            StringBuilder sb = new StringBuilder();
            sb.append("ip address-set ");
            String objName = "";
            if(StringUtils.isNotEmpty(ipSystem)){
                objName = ipSystem;
            } else {
                objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            }
            objName = containsQuotes(objName);
            dto.setName(objName);
            sb.append(objName);
            sb.append(" type object\n");
            int index = 0;
            for (String address : arr) {
                String addressCmd ;
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        addressCmd = String.format("address " + index + " range %s %s\n", startIp, endIp);
                    } else if (IpUtils.isIPSegment(address)) {
                        String ip = IpUtils.getIpFromIpSegment(address);
                        String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                        addressCmd = String.format("address " + index + " %s mask %s\n", ip, maskBit);
                    } else {
                        addressCmd = String.format("address " + index + " %s 0\n", address);
                    }
                } else {
                    // IPV6
                    if(address.contains("-")){
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(address);
                        String endIp = IpUtils.getRangeEndIPv6(address);
                        addressCmd = String.format("address " + index + " range %s %s\n", startIp, endIp);
                    } else if (address.contains("/")) {
                        // 子网
                        String ip = IpUtils.getIpSegmentStartIPv6(address);
                        String maskBit = IpUtils.getIpSegmentMaskIPv6(address);
                        addressCmd = String.format("address " + index + " %s %s\n", ip, maskBit);
                    } else {
                        addressCmd = String.format("address " + index + " %s 128\n", address);
                    }
                }
                index++;
                sb.append(addressCmd);
            }
            sb.append("quit\n");
            dto.setCommandLine(sb.toString());
            dto.setJoin(ipPrefix + " address-set " + dto.getName() + " \n");
        } else {
            //直接显示内容
            StringBuilder sb = new StringBuilder();
            for (String address : arr) {
                String addressCmd = "";
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        addressCmd = ipPrefix + String.format(" range %s %s\n", startIp, endIp);
                    } else if (IpUtils.isIPSegment(address)) {
                        if (!address.equals(PolicyConstants.IPV4_ANY)) {
                            String ip = IpUtils.getIpFromIpSegment(address);
                            String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                            addressCmd = ipPrefix + String.format(" %s %s\n", ip, maskBit);
                        }
                    } else if (IpUtils.isIP(address)) {
                        addressCmd = ipPrefix + String.format(" %s 32\n", address);
                    } else if (address.contains(":")) {
                        if (address.contains("/")) {
                            String[] addrArray = address.split("/");
                            if (StringUtils.isNotEmpty(addrArray[0])) {
                                addressCmd = ipPrefix + String.format(" %s %s\n", addrArray[0].toUpperCase(), addrArray[1]);
                            }
                        } else if (address.contains("-")) {
                            String[] addrArray = address.split("-");
                            if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                                addressCmd = ipPrefix + String.format(" range %s %s\n", addrArray[0].toUpperCase(), addrArray[1].toUpperCase());
                            }
                        } else {
                            addressCmd = ipPrefix + String.format(" %s 128\n", address.toUpperCase());
                        }
                    }
                } else {
                    // ipv6
                    if (address.contains("/")) {
                        String[] addrArray = address.split("/");
                        if (StringUtils.isNotEmpty(addrArray[0])) {
                            addressCmd = ipPrefix + String.format(" %s %s\n", addrArray[0].toUpperCase(), addrArray[1]);
                        }
                    } else if (address.contains("-")) {
                        String[] addrArray = address.split("-");
                        if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                            addressCmd = ipPrefix + String.format(" range %s %s\n", addrArray[0].toUpperCase(), addrArray[1].toUpperCase());
                        }
                    } else {
                        addressCmd = ipPrefix + String.format(" %s 128\n", address.toUpperCase());
                    }
                }
                sb.append(addressCmd);
            }
            dto.setCommandLine(sb.toString());
        }
        return dto;
    }

    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            logger.error("字符串长度计算异常");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }
    protected String containsQuotes(String objectName){
        if(StringUtils.isNotEmpty(objectName) && objectName.contains(" ")){
            objectName = "\""+objectName+"\"";
        }
        return objectName;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, String existsServiceName, Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if ((serviceDTOList == null || serviceDTOList.isEmpty()) && StringUtils.isBlank(existsServiceName)) {
            return dto;
        }

        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin(String.format("service %s \n", existsServiceName));
            return dto;
        }

        if (serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            createObjFlag = false;
        }

        dto.setObjectFlag(createObjFlag);

        StringBuilder sb = new StringBuilder();
        if (createObjFlag) {
            String objName = getServiceName(serviceDTOList);
            dto.setName(objName);
            sb.append(String.format("ip service-set %s type object\n", objName));
        }

        index = 0;
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            //当协议为any，则策略中不填service即可
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return dto;
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                Integer type = StringUtils.isNotBlank(service.getType()) ? Integer.valueOf(service.getType()) : null;
                Integer code = StringUtils.isNotBlank(service.getCode()) ? Integer.valueOf(service.getCode()) : 0;
                sb.append(createIcmpProtocol(type, code, createObjFlag, index, ipType));
            } else {
                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                sb.append(createOtherProtocol(protocolString, srcPorts, dstPorts, createObjFlag, index));
            }
        }

        if (createObjFlag) {
            sb.append("quit\n");
            dto.setJoin(String.format("service %s \n", dto.getName()));
        }
        dto.setCommandLine(sb.toString());
        return dto;
    }

    public String getServiceName(List<ServiceDTO> serviceDTOList){
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(getServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    private String createIcmpProtocol(Integer icmpType, Integer icmpCode, boolean createObjFlag, Integer num,Integer ipType) {
        StringBuffer sb = new StringBuffer();
        sb.append("service");
        if (createObjFlag) {
            //创建对象时，有需要序号
            sb.append(" " + num);
            num++;
            index = num;
        }
        if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
            sb.append(" icmp ");
        } else {
            sb.append(" icmpv6 ");
        }
        if (icmpType != null) {
            sb.append(String.format("icmp-type %d %d", icmpType, icmpCode));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String createOtherProtocol(String protocolString, String[] srcPorts, String[] dstPorts, boolean createObjFlag, Integer num) {
        StringBuffer sb = new StringBuffer();
        for (String srcPort : srcPorts) {
            for (String dstPort : dstPorts) {
                sb.append("service");
                if (createObjFlag) {
                    //创建对象时，有需要序号
                    sb.append(" " + num);
                    num++;
                }
                sb.append(String.format(" protocol %s ", protocolString));

                if (srcPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                    if(createObjFlag){
//                        sb.append("source-port 0 to 65535 ");
//                    }
                } else if (PortUtils.isPortRange(srcPort)) {
                    String startPort = PortUtils.getStartPort(srcPort);
                    String endPort = PortUtils.getEndPort(srcPort);
                    sb.append(String.format("source-port %s to %s ", startPort, endPort));
                } else {
                    sb.append(String.format("source-port %s ", srcPort));
                }

                if (dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                    if(createObjFlag) {
//                        sb.append("destination-port 0 to 65535 ");
//                    }
                } else if (PortUtils.isPortRange(dstPort)) {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
                    sb.append(String.format("destination-port %s to %s ", startPort, endPort));
                } else {
                    sb.append(String.format("destination-port %s  ", dstPort));
                }

                sb.append("\n");
                index++;
            }
        }
        index = num;
        return sb.toString();
    }




    public static void main(String[] args) {
       String ipaddr = "192.168.1.1,172.16.1.1";
       U6000ForSDNX u6000 = new U6000ForSDNX();
       PolicyObjectDTO dto = u6000.generateNatObject(ipaddr, "test",null,null);
       System.out.println(dto.getName());
       System.out.println(dto.getCommandLine());

    }
}
