package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc    飞塔NAT策略
 * @author liuchanghao
 * @date 2021-06-29 15:13
 */

@Slf4j
@Service
public class FortinetNatForZXTX implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate cisco nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        String globalAddress = policyDTO.getGlobalAddress();
        String insideAddress = policyDTO.getInsideAddress();
        String name = String.format("mip_%s_%s ", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        sb.append("edit ").append(name).append("\n");
        setAddressCommandLine(globalAddress, sb, "extip ");
        sb.append("set extintf any\n");
        setAddressCommandLine(insideAddress, sb, "mappedip ");


        if((!policyDTO.getGlobalPort().equals("any")) || (!policyDTO.getInsidePort().equals("any"))){
            sb.append("set portforward enable\n");
            String protocol = ProtocolUtils.getProtocolByString(policyDTO.getProtocol());
            sb.append("set protocol ").append(protocol.toLowerCase()).append("\n");

            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(), PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set extport ").append(policyDTO.getGlobalPort()).append("\n");
                } else {
                    if(StringUtils.isNotEmpty(policyDTO.getProtocol())){
                        sb.append("set extport 1-65535").append("\n");
                    }
                }
                if(StringUtils.isNotEmpty(policyDTO.getInsidePort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getInsidePort(), PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set mappedport ").append(policyDTO.getInsidePort()).append("\n");
                }else {
                    if(StringUtils.isNotEmpty(policyDTO.getProtocol())){
                        sb.append("set mappedport 1-65535").append("\n");
                    }
                }
            }
        }else if(StringUtils.isNotEmpty(policyDTO.getProtocol())){
            sb.append("set portforward disable\n");
        }
        sb.append("next\n");
        sb.append("end\n\n");

        String serviceName = "";
        Boolean isExist = false;
        //是否复用
        if(StringUtils.isNotEmpty(policyDTO.getExistGlobaPort())){
            isExist=true;
            serviceName = policyDTO.getExistGlobaPort();
        }
        if(StringUtils.isNotEmpty(policyDTO.getProtocol()) && isExist==false){
            String protocol = ProtocolUtils.getProtocolByString(policyDTO.getProtocol());
            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(), PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("config firewall service custom\n");
                    if (PortUtils.isPortRange(policyDTO.getGlobalPort())) {
                        String startPort = PortUtils.getStartPort(policyDTO.getGlobalPort());
                        String endPort = PortUtils.getEndPort(policyDTO.getGlobalPort());
                        serviceName = String.format("\"%s%s-%s\"", protocol.substring(0, 1).toUpperCase(), startPort, endPort);
                    } else {
                        serviceName = String.format("\"%s%s\"", protocol.substring(0, 1).toUpperCase(), policyDTO.getGlobalPort());
                    }
                } else {
                    serviceName = "ALL_" + protocol.toUpperCase();
                }
            } else {
                serviceName = "ALL_ICMP";
            }

            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(), PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(policyDTO.getGlobalPort())) {
                        String startPort = PortUtils.getStartPort(policyDTO.getGlobalPort());
                        String endPort = PortUtils.getEndPort(policyDTO.getGlobalPort());
                        sb.append(String.format("set tcp-portrange %s %s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set tcp-portrange %s\n", policyDTO.getGlobalPort()));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }
            } else if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(), PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(policyDTO.getGlobalPort())) {
                        String startPort = PortUtils.getStartPort(policyDTO.getGlobalPort());
                        String endPort = PortUtils.getEndPort(policyDTO.getGlobalPort());
                        sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set udp-portrange %s\n", policyDTO.getGlobalPort()));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }

            }
        }


        sb.append("config firewall policy\n");
        sb.append("edit ").append(policyDTO.getCurrentId()).append("\n");
        sb.append("set name ").append(String.format("%s_AO_%s\n",policyDTO.getTheme(), IdGen.getRandomNumberString()));
        if(StringUtils.isNotEmpty(policyDTO.getFromZone())){
            sb.append("set srcintf ").append(policyDTO.getFromZone()).append("\n");
        }else if(StringUtils.isNotEmpty(policyDTO.getInDevItf())){
            sb.append("set srcintf ").append(policyDTO.getInDevItf()).append("\n");
        }
        if(StringUtils.isNotEmpty(policyDTO.getToZone())){
            sb.append("set dstintf ").append(policyDTO.getToZone()).append("\n");
        } else if(StringUtils.isNotEmpty(policyDTO.getOutDevItf())){
            sb.append("set dstintf ").append(policyDTO.getOutDevItf()).append("\n");
        } else {

        }
        sb.append("set srcaddr all").append("\n");
        sb.append("set dstaddr ").append(name).append("\n");
        sb.append("set action accept").append("\n");
        sb.append("set schedule always").append("\n");
        sb.append("set service ").append(serviceName).append("\n");
        sb.append("next\n");
        sb.append("end\n");
        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        List<String> restDstAddressList = policyDTO.getRestDstAddressList();

        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        List<String> existDstAddressList = policyDTO.getExistDstAddressList();

        List<ServiceDTO> serviceList = policyDTO.getRestServiceList();

        StringBuilder sb = new StringBuilder();

        if(restSrcAddressList.size()>0){
            for(String srcAddr : restSrcAddressList){
                sb.append(createAddressObject(srcAddr,existSrcAddressList,policyDTO.getTheme()));
            }
        }

        if(restDstAddressList.size()>0){
            for(String dstAddr : restDstAddressList){
                sb.append(createAddressObject(dstAddr,existDstAddressList,policyDTO.getTheme()));
            }
        }




        List<String> serviceNameList = new ArrayList<>();
        for (String existServiceName : policyDTO.getExistServiceNameList()) {
            serviceNameList.add(String.format("\"%s\"", existServiceName));
        }
        if (serviceList.size() > 0) {
            //服务为any时，service设置为ALL
            if (ProtocolUtils.getProtocolByString(serviceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                serviceNameList.add("ALL");
            } else {
                StringBuilder servicesb = new StringBuilder();
                servicesb.append(createServiceObject(serviceList, serviceNameList));
                if (servicesb.toString().trim().length() > 0) {
                    sb.append(servicesb.toString());
                }
            }
        }
        //源地址 转换后， 生成地址池对象  每次都新建
        PolicyObjectDTO srcPostObject = generateAddressPool(policyDTO.getPostIpAddress(), policyDTO.getTheme());
        if(srcPostObject != null && StringUtils.isNotEmpty(srcPostObject.getCommandLine())){
            sb.append(srcPostObject.getCommandLine()).append("\n");
        }
        sb.append(preCreatePolicy());
        List<String> srcItfList = new ArrayList<>();
        if (AliStringUtils.isEmpty(policyDTO.getSrcZone())) {
            if (StringUtils.isNotEmpty(policyDTO.getSrcItf())) {
                srcItfList.add(policyDTO.getSrcItf());
            }
        } else {
            srcItfList.add(policyDTO.getSrcZone());
        }

        List<String> dstItfList = new ArrayList<>();
        if (StringUtils.isEmpty(policyDTO.getDstZone())) {
            if (StringUtils.isNotEmpty(policyDTO.getDstItf())) {
                dstItfList.add(policyDTO.getDstItf());
            }
        } else {
            dstItfList.add(policyDTO.getDstZone());

        }
        sb.append(createPolicy(policyDTO.getTheme(),String.valueOf(policyDTO.getCurrentId()), srcItfList, dstItfList, serviceNameList, policyDTO.getDescription(), srcPostObject,existSrcAddressList,existDstAddressList));
        if (policyDTO.isVsys()) {
            sb.append("end\n");
        }
        return sb.toString();
    }

    /**
     * 创建静态nat地址命令行
     * @param address
     * @param sb
     * @param keyWords
     */
    private void setAddressCommandLine(String address, StringBuilder sb, String keyWords){
        if(StringUtils.isNotEmpty(address)){
            String[] globals = address.split(",");
            for(String globalIp : globals){
                if (IpUtils.isIPRange(globalIp)) {
                    String startIp = IpUtils.getStartIpFromRange(globalIp);
                    String endIp = IpUtils.getEndIpFromRange(globalIp);
                    sb.append("set ").append(keyWords).append(startIp).append("-").append(endIp).append("\n");
                    break;
                } else if (IpUtils.isIPSegment(globalIp)) {
                    //是子网段，转换成范围
                    String ip = IpUtils.getIpFromIpSegment(globalIp);
                    //获取网段数
                    String maskBit = IpUtils.getMaskBitFromIpSegment(globalIp);
                    long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                    String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                    String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                    sb.append("set ").append(keyWords).append(startIp).append("-").append(endIp).append("\n");
                    break;
                } else  {
                    sb.append("set ").append(keyWords).append(globalIp).append("\n");
                    break;
                }
            }
        }
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        String dstIp = policyDTO.getDstIp();
        String postIpAddress = policyDTO.getPostIpAddress();
        String dstItf = policyDTO.getDstItf();
        String dstZone = policyDTO.getDstZone();
        String srcItf = policyDTO.getSrcItf();
        String srcZone = policyDTO.getSrcZone();
        String name = String.format("mip_%s_%s ", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        sb.append("edit ").append(name).append("\n");
        setAddressCommandLine(dstIp, sb, "extip ");
        if(StringUtils.isNotEmpty(srcItf)){
            sb.append(String.format("set extintf \"%s\"",srcItf)).append("\n");
        }
        setAddressCommandLine(postIpAddress, sb, "mappedip ");


        String protocol = policyDTO.getServiceList().get(0).getProtocol();
        String dstPorts = policyDTO.getServiceList().get(0).getDstPorts();
        String postPort = policyDTO.getPostPort();


        if((!dstPorts.equals("any")) || (!postPort.equals("any"))){
            sb.append("set portforward enable\n");
            String protocolString = ProtocolUtils.getProtocolByString(protocol);
            sb.append("set protocol ").append(protocolString.toLowerCase()).append("\n");

            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set extport ").append(dstPorts).append("\n");
                } else {
                    if(StringUtils.isNotEmpty(protocol)){
                        sb.append("set extport 1-65535").append("\n");
                    }
                }
                if(StringUtils.isNotEmpty(postPort) && !StringUtils.equalsAnyIgnoreCase(postPort, PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set mappedport ").append(postPort).append("\n");
                }else {
                    if(StringUtils.isNotEmpty(protocol)){
                        sb.append("set mappedport 1-65535").append("\n");
                    }
                }
            }
        }else if(StringUtils.isNotEmpty(protocol)){
            sb.append("set portforward disable\n");
        }
        sb.append("next\n");
        sb.append("end\n\n");

        String serviceName = "";

        String timeObjectName = "always";
        if (policyDTO.getStartTime() != null) {
            sb.append(preCreateTimeObject());
            timeObjectName = String.format("\"to%s\"",
                    TimeUtils.transformDateFormat(policyDTO.getEndTime(), TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
            sb.append(createTimeObject(formatTimeString(policyDTO.getStartTime()), formatTimeString(policyDTO.getEndTime()), timeObjectName));
        }


        //是否复用
        Boolean isExist = false;
        if(CollectionUtils.isNotEmpty(policyDTO.getExistServiceNameList())){
            List<String> existServiceNameList = policyDTO.getExistServiceNameList();
            String existServiceName= existServiceNameList.get(0);
            serviceName = existServiceName;
            isExist=true;
        }

        //创建地址对象
        if(CollectionUtils.isNotEmpty(restSrcAddressList)){
            for(String srcAddress : restSrcAddressList){
                sb.append("config firewall address\n");
                String element = "";
                if(IpUtils.isIPRange(srcAddress)){
                    String[] range = srcAddress.split("-");
                    String start = range[0];
                    String end  = range[1];
                    element = String.format(" \"%s\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set type iprange\n"));
                    sb.append(String.format("set start-ip %s\n",start));
                    sb.append(String.format("set end-ip %s\n",end));
                }else if(IpUtils.isIPSegment(srcAddress)){
                    element = String.format(" \"%s\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set sunbet %s\n",srcAddress));
                }else{
                    element = String.format(" \"%s/32\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set subnet %s/32\n",srcAddress));
                }
                existSrcAddressList.add(element);
                sb.append("next\nend\n\n");
            }
        }
        if(StringUtils.isNotEmpty(protocol) && isExist==false){
            String protocolString = ProtocolUtils.getProtocolByString(protocol);
            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("config firewall service custom\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        serviceName = String.format("\"%s%s-%s\"", protocolString.substring(0, 1).toUpperCase(), startPort, endPort);
                    } else {
                        serviceName = String.format("\"%s%s\"", protocolString.substring(0, 1).toUpperCase(), dstPorts);
                    }
                } else {
                    serviceName = "ALL_" + protocolString.toUpperCase();
                }
            } else {
                serviceName = "ALL_ICMP";
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        sb.append(String.format("set tcp-portrange %s %s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set tcp-portrange %s\n", dstPorts));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set udp-portrange %s\n", dstPorts));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }

            }
        }


        sb.append("config firewall policy\n");
        sb.append("edit ").append(policyDTO.getCurrentId()).append("\n");
        sb.append("set name ").append(String.format("%s_AO_%s\n",policyDTO.getTheme(), IdGen.getRandomNumberString()));
        if(StringUtils.isNotEmpty(srcZone)){
            sb.append("set srcintf ").append(srcZone).append("\n");
        }else if(StringUtils.isNotEmpty(srcItf)){
            sb.append("set srcintf ").append(srcItf).append("\n");
        }
        if(StringUtils.isNotEmpty(dstZone)){
            sb.append("set dstintf ").append(dstZone).append("\n");
        } else if(StringUtils.isNotEmpty(dstItf)){
            sb.append("set dstintf ").append(dstItf).append("\n");
        }

        if(CollectionUtils.isNotEmpty(existSrcAddressList)){
            sb.append("set srcaddr ");
            for (String srcip : existSrcAddressList){
                sb.append(String.format("%s ",srcip));
            }
            sb.append("\n");
        }else {
            sb.append("set srcaddr all\n");
        }


        sb.append("set dstaddr ").append(name).append("\n");
        sb.append("set action accept").append("\n");
        sb.append(String.format("set schedule %s\n", timeObjectName));
        sb.append("set service ").append(serviceName).append("\n");
        sb.append("set logtraffic all\n");
        sb.append("next\n");
        sb.append("end\n");
        return sb.toString();
    }

    String preCreateTimeObject() {
        return "config firewall schedule onetime\n";
    }

    String createTimeObject(String startTime, String endTime, String timeObjectName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", timeObjectName));
        sb.append(String.format("set start %s\n", startTime));
        sb.append(String.format("set end %s\n", endTime));
        sb.append("next\nend\n\n");
        return sb.toString();
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.FORTINET_FORMAT);
    }

    private String getAddressObjectForObjectGroup(String ipAddress, String name){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("object network %s\n", name));
        if(IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            sb.append(String.format("range %s %s\n", start, end));
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            sb.append(String.format("subnet %s %s\n", ip, mask));
        } else {
            sb.append(String.format("host %s\n", ipAddress));
        }

        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        // TODO
        return DO_NOT_SUPPORT;
    }

    private String getAddressObject(String ipAddress, String name){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("object network %s\n", name));
        if(IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            sb.append(String.format("range %s %s\n", start, end));
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            sb.append(String.format("subnet %s %s\n", ip, mask));
        } else {
            sb.append(String.format("host %s\n", ipAddress));
        }

        return sb.toString();
    }

    private String preCreateServiceObject() {
        return "config firewall service custom\n";
    }

    private PolicyObjectDTO getAddressObjectGroup(String ipAddresses, String name, String addressObjectName, boolean createObject){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        //地址对象已存在，复用
        if(!AliStringUtils.isEmpty(addressObjectName)) {
            dto.setName(addressObjectName);
            dto.setCommandLine("");
            return dto;
        }

        //不强制创建对象的时候不对单个ip创建对象
        if(!createObject) {
            //若为单独IP地址，则不创建对象组
            if (IpUtils.isIP(ipAddresses)) {
                dto.setName(ipAddresses);
                dto.setCommandLine("");
                return dto;
            }
        }

        StringBuilder sb = new StringBuilder();
        String objectName = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
        String[] ipAddressList = ipAddresses.split(",");
        if(ipAddressList.length == 1) {
            sb.append(getAddressObject(ipAddressList[0], objectName));
        } else {
            List<String> objectJoinList = new ArrayList<>();
            for (String ipAddress : ipAddressList) {
                String join = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
                if(IpUtils.isIPSegment(ipAddress) || IpUtils.isIPRange(ipAddress)) {
                    sb.append(getAddressObjectForObjectGroup(ipAddress, join));
                    join = "object " + join;
                } else {
                    join = "host " + ipAddress;
                }
                objectJoinList.add(join);
            }

            sb.append(String.format("object-group network %s\n", objectName));
            for(String join:objectJoinList) {
                sb.append("network " + join + "\n");
            }
        }

        dto.setName(objectName);
        dto.setCommandLine(sb.toString());

        return dto;
    }

    private String preCreateAddressObject() {
        return "config firewall address\n";
    }

    private String createServiceObject(ServiceDTO service, List<String> serviceNameList) {
        StringBuilder sb = new StringBuilder();
        String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
        //若端口为空，则服务名称为TCP_ALL，ICMP_ALL，UDP_ALL等
        if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            serviceNameList.add(String.format("ALL_%s", protocol.toUpperCase()));
            return "";
        }

        String dstPort = service.getDstPorts();
        String[] dstPortList = dstPort.split(",");

        //同一个协议，对于每个port都生成一个服务
        for (String port : dstPortList) {
            sb.append(preCreateServiceObject());

            String serviceName = String.format("\"service_%s\"", protocol);
            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                serviceName = String.format("\"%s%s\"", protocol.substring(0, 1).toUpperCase(), service.getDstPorts());
                if(PortUtils.isPortRange(service.getDstPorts())) {
                    String start = PortUtils.getStartPort(service.getDstPorts());
                    String end = PortUtils.getEndPort(service.getDstPorts());
                    serviceName =  String.format("\"%s%s-%s\"", protocol.substring(0, 1).toUpperCase(), start, end);
                }
            }

            /*String name = String.format("\"%s%s\"", protocolString.substring(0, 1).toUpperCase(), service.getDstPorts());

            if(PortUtils.isPortRange(service.getDstPorts())) {
                String start = PortUtils.getStartPort(service.getDstPorts());
                String end = PortUtils.getEndPort(service.getDstPorts());
                name =  String.format("\"%s%s-%s\"", protocolString.substring(0, 1).toUpperCase(), start, end);

            }*/
            sb.append(String.format("edit %s\n", serviceName));
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set tcp-portrange %s %s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set tcp-portrange %s\n", port));
                }
            } else if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set udp-portrange %s %s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set udp-portrange %s\n", port));
                }
            } else {
                sb.append(String.format("set protocol %s\n", protocol.toUpperCase()));
            }
            serviceNameList.add(serviceName);
            sb.append("next\n");
            sb.append("end\n\n");
        }
        return sb.toString();
    }

    private String createServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList) {
        StringBuilder sb = new StringBuilder();

        for (ServiceDTO serviceDTO : serviceDTOList) {
            String command = createServiceObject(serviceDTO, existServiceNameList);
            sb.append(command);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String createAddressObject(String srcIp, List<String> addressNameList,String theme) {
        StringBuilder sb = new StringBuilder();
        String addressObjectName = "";
        sb.append(preCreateAddressObject());
        if (IpUtils.isIPRange(srcIp)) {
            String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
            String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
            addressObjectName = String.format("\"%s-%s\"", startIp, endIp);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append("set type iprange\n");
            sb.append(String.format("set start-ip %s\n", startIp));
            sb.append(String.format("set end-ip %s\n", endIp));
        } else if (IpUtils.isIPSegment(srcIp)) {
            String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
            String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp);
            addressObjectName = String.format("\"%s/%s\"", ipFromIpSegment, maskBitFromIpSegment);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append(String.format("set subnet %s/%s\n", ipFromIpSegment, maskBitFromIpSegment));
        } else if(IpUtils.isIP(srcIp)){
            addressObjectName = String.format("\"%s/32\"", srcIp);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append(String.format("set subnet %s/32\n", srcIp));
        }else if(ObjectUtils.notEqual(srcIp,"any")){
            addressObjectName = String.format("%s_AO_%s",theme, IdGen.getRandomNumberString());
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append("set type fqdn\n");
            sb.append(String.format("set fqdn %s\n",srcIp));
        }

        sb.append("next\n");
        sb.append("end\n\n");
        addressNameList.add(addressObjectName);
        return sb.toString();
    }

    String preCreatePolicy() {
        return "config firewall policy\n";
    }

    /**
     * 新建地址池
     * @param ipAddress  地址池信息
     * @param ticket  工单名称
     * @return
     */
    public PolicyObjectDTO generateAddressPool(String ipAddress, String ticket){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);

        StringBuilder sb = new StringBuilder();
        sb.append("config firewall ippool\n");
        String setName = String.format("%s_pool_%s ", ticket, IdGen.getRandomNumberString());
        sb.append("edit ").append(setName).append("\n");
        String[] arr = ipAddress.split(",");

        for (String address : arr) {

            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                sb.append("set startip ").append(startIp).append("\n");
                sb.append("set endip ").append(endIp).append("\n");
                sb.append("next\n");
                sb.append("end\n");
                // TODO 这里创建地址池的时候，只取了第一个IP，暂不支持IPV6
                break;
            } else if (IpUtils.isIPSegment(address)) {
                //是子网段，转换成范围
                String ip = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                sb.append("set startip ").append(startIp).append("\n");
                sb.append("set endip ").append(endIp).append("\n");
                sb.append("next\n");
                sb.append("end\n");
                // TODO 这里创建地址池的时候，只取了第一个IP，暂不支持IPV6
                break;
            } else  {
                sb.append("set startip ").append(address).append("\n");
                sb.append("set endip ").append(address).append("\n");
                sb.append("next\n");
                sb.append("end\n");
                // TODO 这里创建地址池的时候，只取了第一个IP，暂不支持IPV6
                break;
            }
        }

        String command  = sb.toString();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(command);
        return dto;

    }

    String createPolicy(String theme, String policyName, List<String> srcItfList, List<String> dstItfList,
                        List<String> serviceNameList, String description, PolicyObjectDTO srcPostObject,
                        List<String> existSrcAddressList, List<String> existDstAddressList) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", policyName));
        sb.append("set name ").append(String.format("%s_AO_%s\n",theme, IdGen.getRandomNumberString()));
        if (CollectionUtils.isNotEmpty(srcItfList)) {
            sb.append("set srcintf ");
            for (String itf : srcItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }

        if (CollectionUtils.isNotEmpty(dstItfList)) {
            sb.append("set dstintf ");
            for (String itf : dstItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }
        if (CollectionUtils.isNotEmpty(existSrcAddressList)) {
            sb.append("set srcaddr ");
            for (String addressObject : existSrcAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }else{
            sb.append("set srcaddr all\n");
        }

        if (CollectionUtils.isNotEmpty(existDstAddressList)) {
            sb.append("set dstaddr ");
            for (String addressObject : existDstAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }else{
            sb.append("set dstaddr all\n");
        }

        if (CollectionUtils.isNotEmpty(serviceNameList)) {
            sb.append("set service ");
            for (String service : serviceNameList) {
                sb.append(String.format("%s ", service));
            }
            sb.append("\n");
        }

        sb.append("set schedule always\n");
        sb.append("set action accept\n");

        sb.append("set nat enable\n");
        if (StringUtils.isNotBlank(srcPostObject.getCommandLine())) {
            sb.append("set ippool enable\n");
            sb.append("set poolname ").append(srcPostObject.getName()).append("\n");
        }
        if (!AliStringUtils.isEmpty(description)) {
            //排除命令行为空格
            if (description.trim().length() > 0) {
                sb.append(String.format("set comments %s\n", description));
            }
        }

        sb.append("set logtraffic all\n");
        sb.append("next\n");
        sb.append("\nend\n\n");

        return sb.toString();
    }

    public static void main(String[] args) {
        FortinetNatForZXTX fortinet = new FortinetNatForZXTX();


        DNatPolicyDTO dNatPolicyDTO = new DNatPolicyDTO();
        //dNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        List<String> a = new ArrayList<>();
        a.add("abc");
        List<String> b = new ArrayList<>();
        b.add("2.2.2.2");
        b.add("6.6.6.6/20");
        b.add("5.5.5.5-5.5.5.12");
        dNatPolicyDTO.setRestSrcAddressList(b);
        dNatPolicyDTO.setExistSrcAddressList(a);
        dNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        dNatPolicyDTO.setPostPort("any");
        dNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");



        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setDstPorts("80");
        serviceDTO.setProtocol("6");
        serviceDTOList.add(serviceDTO);

        dNatPolicyDTO.setServiceList(serviceDTOList);

        dNatPolicyDTO.setCreateObjFlag(true);
        dNatPolicyDTO.setSrcZone("trust");
        dNatPolicyDTO.setDstZone("untrust");

        dNatPolicyDTO.setSrcItf("srcItf");
        dNatPolicyDTO.setDstItf("dstItf");


        dNatPolicyDTO.setTheme("w1");
        dNatPolicyDTO.setCurrentId("test dnat");
        System.out.println(fortinet.generateDNatCommandLine(dNatPolicyDTO));


    }
}
