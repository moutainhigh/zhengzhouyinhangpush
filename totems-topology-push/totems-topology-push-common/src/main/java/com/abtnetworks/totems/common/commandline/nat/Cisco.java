package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityCiscoASA;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoASAImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("Cisco ASA NAT")
@Deprecated
public class Cisco implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate cisco nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
//        if(policyDTO.isCiscoEnable()) {
//            sb.append("enable\n\n\n");
//        }
        sb.append("configure terminal\n");

        PolicyObjectDTO globalObject = getAddressObjectGroup(policyDTO.getGlobalAddress(), policyDTO.getTheme(), policyDTO.getGlobalAddressName(), policyDTO.isCreateObject());

        String portString  = "";
        if(!AliStringUtils.isEmpty(policyDTO.getGlobalPort())) {
            //多个端口/端口范围只取第一个
            String port = policyDTO.getGlobalPort().split(",")[0];
            if(PortUtils.isPortRange(port)) {
                String start = PortUtils.getStartPort(port);
                String end  = PortUtils.getEndPort(port);
                portString = String.format("%s %s", start, end);
            } else {
                if(!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    portString = String.format("%s %s", port, port);
                }
            }
        }

        if(!AliStringUtils.isEmpty(globalObject.getCommandLine())) {
            sb.append(globalObject.getCommandLine());
            sb.append("\n");
        }
        String protocolString = PolicyConstants.POLICY_STR_VALUE_ANY;
        if(!AliStringUtils.isEmpty(policyDTO.getProtocol())) {
            protocolString = ProtocolUtils.getProtocolByString(policyDTO.getProtocol());
        }

        PolicyObjectDTO insideAddressObject = getAddressObjectGroup(policyDTO.getInsideAddress(), policyDTO.getTheme(), policyDTO.getInsideAddressName(), true);
        if(!AliStringUtils.isEmpty(insideAddressObject.getCommandLine())) {
            sb.append(insideAddressObject.getCommandLine());
            sb.append("\n");
        }

        List<String> addressObjectNameList = new ArrayList<>();
        List<String> addressObjectGroupNameList = new ArrayList<>();
        List<String> serviceObjectNameList = new ArrayList<>();
        List<String> serviceObjectGroupNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(addressObjectNameList, addressObjectGroupNameList, serviceObjectNameList,
                serviceObjectGroupNameList, globalObject, insideAddressObject,null, null, null, null);

        policyDTO.setAddressObjectNameList(addressObjectNameList);
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);


        sb.append(String.format("object network %s\n",insideAddressObject.getName()));

        String inDevItfAlia = StringUtils.isNotEmpty(policyDTO.getFromZone()) ? policyDTO.getFromZone() : policyDTO.getInDevItf();
        String outDevItfAlias = StringUtils.isNotEmpty(policyDTO.getToZone()) ? policyDTO.getToZone() : policyDTO.getOutDevItf();


        StringBuffer rollbackCommandLine = new StringBuffer();
        rollbackCommandLine.append("configure terminal\n");
        rollbackCommandLine.append(String.format("no nat (%s,%s) %s %s %s\n", inDevItfAlia, outDevItfAlias, AliStringUtils.isEmpty(globalObject.getCommandLine()) ? "static" : "dynamic", globalObject.getName(),
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "" : String.format(" service %s %s", protocolString.toLowerCase(), portString)));
        rollbackCommandLine.append("\nend\nwrite\n");
        rollbackCommandLine.append("\n");
        policyDTO.setRollbackCommandLine(rollbackCommandLine.toString());

        sb.append(String.format("nat (%s,%s) %s %s %s\n", inDevItfAlia, outDevItfAlias, AliStringUtils.isEmpty(globalObject.getCommandLine()) ? "static" : "dynamic", globalObject.getName(),
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "" : String.format(" service %s %s", protocolString.toLowerCase(), portString)));

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();


        if (policyDTO.isVsys()) {
            sb.append("changeto context system \n");
            sb.append("changeto context " + policyDTO.getVsysName() + "\n");
        }

        sb.append("configure terminal").append(StringUtils.LF);
        String srcObjName = "", dstObjName = "", serviceObjName = "";
        SecurityCiscoASA securityCiscoASA = new SecurityCiscoASA();
        PolicyObjectDTO srcObjectDTO = null;
        PolicyObjectDTO dstObjectDTO = null;
        PolicyObjectDTO serviceObjectDTO = null;


        String srcItf = StringUtils.isNotEmpty(policyDTO.getSrcZone()) ? policyDTO.getSrcZone() : policyDTO.getSrcItf();
        String dstItf = StringUtils.isNotEmpty(policyDTO.getDstZone()) ? policyDTO.getDstZone() : policyDTO.getDstItf();

        if (StringUtils.isNotEmpty(policyDTO.getSrcIp()) && policyDTO.getSrcIp().split(",").length == 1 &&
                TotemsIp4Utils.isIp4(policyDTO.getSrcIp().replace("/32",""))) {
        } else {
            srcObjectDTO = securityCiscoASA.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), true, policyDTO.getSrcAddressObjectName(), policyDTO.getSrcIpSystem());
        }

        if (StringUtils.isNotEmpty(policyDTO.getDstIp()) && policyDTO.getDstIp().split(",").length == 1 &&
                TotemsIp4Utils.isIp4(policyDTO.getDstIp().replace("/32",""))) {
        } else {
            dstObjectDTO = securityCiscoASA.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), true, policyDTO.getDstAddressObjectName(), policyDTO.getDstIpSystem());
        }

        if (CollectionUtils.isNotEmpty(policyDTO.getServiceList()) && policyDTO.getServiceList().size() == 1 &&
                !policyDTO.getServiceList().get(0).getProtocol().equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ANY) &&
                policyDTO.getServiceList().get(0).getDstPorts().split(",").length < 2) {
            if (policyDTO.getServiceList().get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                serviceObjName = ProtocolUtils.getProtocolByString(policyDTO.getServiceList().get(0).getProtocol()).toLowerCase();
            }
        } else {
            serviceObjectDTO = securityCiscoASA.generateServiceObject(policyDTO.getServiceList(), true, policyDTO.getServiceObjectName());
        }

        if (ObjectUtils.isNotEmpty(srcObjectDTO)) {
            if (StringUtils.isNotEmpty(srcObjectDTO.getCommandLine())){
                sb.append(srcObjectDTO.getCommandLine()).append(StringUtils.LF).append(StringUtils.LF);
            }
            if (StringUtils.isNotEmpty(srcObjectDTO.getName())){
                srcObjName = "object-group " + srcObjectDTO.getName();
            }else {
                srcObjName = srcObjectDTO.getJoin();
            }
        }
        if (ObjectUtils.isNotEmpty(dstObjectDTO)) {
            if (StringUtils.isNotEmpty(dstObjectDTO.getCommandLine())){
                sb.append(dstObjectDTO.getCommandLine()).append(StringUtils.LF).append(StringUtils.LF);
            }
            if (StringUtils.isNotEmpty(dstObjectDTO.getName())){
                dstObjName = "object-group " + dstObjectDTO.getName();
            }else {
                dstObjName = dstObjectDTO.getJoin();
            }
        }
        if (ObjectUtils.isNotEmpty(serviceObjectDTO)) {
            if (StringUtils.isNotEmpty(serviceObjectDTO.getCommandLine())){
                sb.append(serviceObjectDTO.getCommandLine()).append(StringUtils.LF).append(StringUtils.LF);
            }
            if (StringUtils.isNotEmpty(serviceObjectDTO.getName())){
                serviceObjName = "object-group " + serviceObjectDTO.getName();
            }else if (serviceObjectDTO.getJoin().contains("object-group")){
                serviceObjName = serviceObjectDTO.getJoin();
            }
            if (serviceObjectDTO.getJoin().equals("ip")) {
                serviceObjName = "ip";
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (StringUtils.isNotEmpty(policyDTO.getExistAclName())) {
            if (ObjectUtils.isEmpty(serviceObjectDTO) && !policyDTO.getServiceList().get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String protocolByString = ProtocolUtils.getProtocolByString(policyDTO.getServiceList().get(0).getProtocol());
                stringBuilder.append("access-list ").append(policyDTO.getExistAclName()).append(" extended permit ").append(protocolByString.toLowerCase()).append(" ")
                        .append(StringUtils.isNotEmpty(srcObjName) ? srcObjName : "host " + policyDTO.getSrcIp().replace("/32","")).append(" ")
                        .append(StringUtils.isNotEmpty(dstObjName) ? dstObjName : "host " + policyDTO.getDstIp().replace("/32",""))
                        .append(policyDTO.getServiceList().get(0).getDstPorts().contains("-") ? " range " + policyDTO.getServiceList().get(0).getDstPorts().replace("-", " ") : " eq " + policyDTO.getServiceList().get(0).getDstPorts())
                        .append(StringUtils.LF);

            } else {
                stringBuilder.append("access-list ").append(policyDTO.getExistAclName()).append(" extended permit ").append(serviceObjName).append(" ")
                        .append(StringUtils.isNotEmpty(srcObjName) ? srcObjName : "host " + policyDTO.getSrcIp().replace("/32","")).append(" ")
                        .append(StringUtils.isNotEmpty(dstObjName) ? dstObjName : "host " + policyDTO.getDstIp().replace("/32","")).append(StringUtils.LF);
            }
            sb.append(stringBuilder.toString());
        } else {
            String name = String.format("%s_AO_%s",policyDTO.getTheme(), IdGen.getRandomNumberString());
            if (!policyDTO.isExistGlobal()){
                if (StringUtils.isNotEmpty(policyDTO.getPostIpAddress()) && policyDTO.getPostIpAddress().split(",").length > 1){
                    String[] strings = policyDTO.getPostIpAddress().split(",");
                    for (String string : strings) {
                        if (TotemsIp4Utils.isIp4Mask(string.replace("/32",""))){
                            stringBuilder.append("global (").append(dstItf).append(") ").append(policyDTO.getCurrentId()).append(" ").append(IpUtils.getStartIpFromIpAddress(string)).append(" netmask ").append(IpUtils.getMaskMap(IpUtils.getMaskBitFromIpSegment(string))).append(StringUtils.LF);
                        }else {
                            stringBuilder.append("global (").append(dstItf).append(") ").append(policyDTO.getCurrentId()).append(" ").append(string.replace("/32","")).append(StringUtils.LF);
                        }
                    }
                }else {
                    if (StringUtils.isNotEmpty(policyDTO.getPostIpAddress()) && TotemsIp4Utils.isIp4Mask(policyDTO.getPostIpAddress().replace("/32",""))){
                        stringBuilder.append("global (").append(dstItf).append(") ").append(policyDTO.getCurrentId()).append(" ").append(IpUtils.getStartIpFromIpAddress(policyDTO.getPostIpAddress())).append(" netmask ").append(IpUtils.getMaskMap(IpUtils.getMaskBitFromIpSegment(policyDTO.getPostIpAddress()))).append(StringUtils.LF);
                    }else {
                        stringBuilder.append("global (").append(dstItf).append(") ").append(policyDTO.getCurrentId()).append(" ").append(StringUtils.isNotEmpty(policyDTO.getPostIpAddress()) ? policyDTO.getPostIpAddress().replace("/32","") : "interface").append(StringUtils.LF);
                    }
                }
            }
            if (ObjectUtils.isEmpty(serviceObjectDTO) && !policyDTO.getServiceList().get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String protocolByString = ProtocolUtils.getProtocolByString(policyDTO.getServiceList().get(0).getProtocol());
                stringBuilder.append("access-list ").append(name).append(" extended permit ").append(protocolByString.toLowerCase()).append(" ")
                        .append(StringUtils.isNotEmpty(srcObjName) ? srcObjName : "host " + policyDTO.getSrcIp().replace("/32","")).append(" ")
                        .append(StringUtils.isNotEmpty(dstObjName) ? dstObjName : "host " + policyDTO.getDstIp().replace("/32",""))
                        .append(policyDTO.getServiceList().get(0).getDstPorts().contains("-") ? " range " + policyDTO.getServiceList().get(0).getDstPorts().replace("-", " ") : " eq " + policyDTO.getServiceList().get(0).getDstPorts())
                        .append(StringUtils.LF);

            } else {
                stringBuilder.append("access-list ").append(name).append(" extended permit ").append(serviceObjName).append(" ")
                        .append(StringUtils.isNotEmpty(srcObjName) ? srcObjName : "host " + policyDTO.getSrcIp().replace("/32","")).append(" ")
                        .append(StringUtils.isNotEmpty(dstObjName) ? dstObjName : "host " + policyDTO.getDstIp().replace("/32","")).append(StringUtils.LF);
            }
            sb.append(stringBuilder.toString());
            sb.append("nat (").append(srcItf).append(") ").append(policyDTO.getCurrentId()).append(" access-list ").append(name).append(StringUtils.LF);
        }

        policyDTO.setRollbackShowCmd(stringBuilder.toString());

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
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

    private PolicyObjectDTO getAddressObjectGroup(String ipAddresses, String name, String addressObjectName) {
        return getAddressObjectGroup(ipAddresses, name, addressObjectName, false);
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
        List<String>  createObjectNames = new ArrayList<>();
        List<String>  createGroupObjectNames = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String objectName = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
        String[] ipAddressList = ipAddresses.split(",");
        if(ipAddressList.length == 1) {
            sb.append(getAddressObject(ipAddressList[0], objectName));
            createObjectNames.add(objectName);
            dto.setGroup(false);
        } else {
            List<String> objectJoinList = new ArrayList<>();
            for (String ipAddress : ipAddressList) {
                String join = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
                if(IpUtils.isIPSegment(ipAddress) || IpUtils.isIPRange(ipAddress)) {
                    createObjectNames.add(join);
                    sb.append(getAddressObjectForObjectGroup(ipAddress, join));
                    join = "object " + join;
                } else {
                    join = "host " + ipAddress;
                }
                objectJoinList.add(join);
            }

            sb.append(String.format("object-group network %s\n", objectName));
            createGroupObjectNames.add(objectName);
            for(String join:objectJoinList) {
                sb.append("network " + join + "\n");
            }
            dto.setGroup(true);
        }

        dto.setName(objectName);
        dto.setCommandLine(sb.toString());
        dto.setCreateObjectName(createObjectNames);
        dto.setCreateGroupObjectName(createGroupObjectNames);
        return dto;
    }

    //将IP中范围转换成为子网
    String formatIpAddresses(String ipAddresses) {
        if(AliStringUtils.isEmpty(ipAddresses)) {
            return "";
        }
        String[] ipAddressList = ipAddresses.split(",");
        StringBuilder sb = new StringBuilder();
        for(String ipAddress : ipAddressList) {
            sb.append(",");
            if(IpUtils.isIPRange(ipAddress)){
                sb.append(IpUtils.convertIpRangeToSegment(ipAddress));
            } else {
                sb.append(ipAddress);
            }
        }
        if(sb.length()>0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        StaticNatTaskDTO staticDto = new StaticNatTaskDTO();
        staticDto.setGlobalAddress("45.5.3.5,33.4.5.6/24,67.5.4.34-67.5.4.55");
        staticDto.setProtocol("6");
        staticDto.setGlobalPort("80-90,100");
        staticDto.setTheme("static001");
        staticDto.setInsideAddress("25.5.3.5,23.4.5.6/24,2.5.4.34-2.5.4.55");
//        staticDto.setInsideAddressName("inpolicy");
        staticDto.setInDevItf("inxq1");
        staticDto.setOutDevItf("outnana1");

        Cisco cisco = new Cisco();
        String  line = cisco.generateStaticNatCommandLine(staticDto);
        System.out.println(line);
        System.out.println("------------------------------------------------");

    }
}
