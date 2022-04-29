package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NatCiscoASAForzheShang implements NatPolicyGenerator {

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
        sb.append(String.format("object network %s\n",insideAddressObject.getName()));
        sb.append(String.format("nat (%s,%s) %s %s %s\n", policyDTO.getInDevItf(), policyDTO.getOutDevItf(), AliStringUtils.isEmpty(globalObject.getCommandLine())?"static":"dynamic", globalObject.getName(),
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "" : String.format(" service %s %s", protocolString.toLowerCase(), portString)));

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
//        if(policyDTO.isCiscoEnable()) {
//            sb.append("enable\n\n\n");
//        }
        sb.append("configure terminal\n");

        sb.append(String.format("nat (%s,%s) source dynamic PAT %s", policyDTO.getSrcItf(), policyDTO.getDstItf(), policyDTO.getPostIpAddress()));

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
        staticDto.setGlobalAddress("192.168.1.1,192.168.0.1/25,192.168.2.13-192.168.2.15");
        staticDto.setProtocol("6");
        staticDto.setGlobalPort("80-90,100");
        staticDto.setTheme("static001");
        staticDto.setInsideAddress("1.2.3.4,4.3.2.1/24");
        staticDto.setInsideAddressName("inpolicy");
        staticDto.setInDevItf("inxq1");
        staticDto.setOutDevItf("outnana1");

        NatCiscoASAForzheShang cisco = new NatCiscoASAForzheShang();
        System.out.println(cisco.generateStaticNatCommandLine(staticDto));

    }
}
