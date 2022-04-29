package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5ForZXTX;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class HillstoneForZXTX implements NatPolicyGenerator {

    private static String SEPERATOR = ",";

    private static Set<Integer> allowType = new HashSet<>();

    private final int MAX_NAME_LENGTH = 95;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate hillstone nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src");
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst");
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "");
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), policyDTO.isCreateObjFlag());

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postAddressObject.getCommandLine())) {
            sb.append(postAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("nat\n");
        String postAddressObjectName = postAddressObject.getName();
        if(!IpUtils.isIP(postAddressObjectName) &&  !IpUtils.isIPSegment(postAddressObjectName)){
            postAddressObjectName = "address-book " + postAddressObjectName;
        }
        //源NAT策略生成页面，转换后地址不填时，就转换为出接口 20210312
        if (AliStringUtils.isEmpty(policyDTO.getPostIpAddress())) {
            sb.append(String.format("snatrule from %s to %s%s%s trans-to eif-ip mode dynamicport log\n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                    AliStringUtils.isEmpty(policyDTO.getDstItf()) ? (AliStringUtils.isEmpty(policyDTO.getDstZone()) ? "": " eif"):(" eif " + policyDTO.getDstItf())));
        }else {
            sb.append(String.format("snatrule from %s to %s%s%s trans-to %s mode dynamicport log\n", srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                    AliStringUtils.isEmpty(policyDTO.getDstItf()) ? (AliStringUtils.isEmpty(policyDTO.getDstZone()) ? "": " eif") : (" eif " + policyDTO.getDstItf()),
                    postAddressObjectName));
        }

        sb.append("exit\nend\n");
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src");
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst");
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "");
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), policyDTO.isCreateObjFlag());

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine()) ) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(postAddressObject.getCommandLine())) {
            sb.append(postAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
        if("any".equalsIgnoreCase(policyDTO.getPostPort())){
            postPortString = "";
        }
        sb.append(String.format("dnatrule %sfrom %s to %s%s trans-to %s%s log\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postAddressObject.getName(), postPortString));

        sb.append("exit\nend\n");
        return sb.toString();
    }


    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        //山石BOTH NAT命令行为生成一个SNAT再生成一个DNAT
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src");
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst");
        PolicyObjectDTO postSrcAddressObject = getAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), true, policyDTO.getPostSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "");
        PolicyObjectDTO postDstAddressObject = getAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), true,policyDTO.getPostDstAddressObjectName(), policyDTO.isCreateObjFlag(), "");
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), policyDTO.isCreateObjFlag());

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postSrcAddressObject.getCommandLine())) {
            sb.append(postSrcAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postDstAddressObject.getCommandLine())) {
            sb.append(postDstAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("nat\n");
        String postAddressObjectName = postSrcAddressObject.getName();
        if(!IpUtils.isIP(postAddressObjectName) &&  !IpUtils.isIPSegment(postAddressObjectName)){
            postAddressObjectName = "address-book " + postAddressObjectName;
        }
        sb.append(String.format("snatrule from %s to %s%s%s trans-to address-book %s mode dynamicport log\n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()), postAddressObjectName));

        sb.append("exit\n");

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
        if(CommonConstants.ANY.equalsIgnoreCase(policyDTO.getPostPort())){
            postPortString = "";
        }
        sb.append(String.format("dnatrule %sfrom %s to %s%s trans-to %s%s log\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postDstAddressObject.getName(), postPortString));

        sb.append("exit\n");

        sb.append("end\n");
        return sb.toString();
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, String addressObjectName, boolean isCreateObject, String prefix) {
        return getAddressObject(ipAddressString, theme, false, addressObjectName, isCreateObject, prefix);
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, boolean isSNatPostAddress, String addressObjectName, boolean isCreateObject, String prefix) {
        PolicyObjectDTO policyObject = new PolicyObjectDTO();
        policyObject.setName("");
        policyObject.setCommandLine("");
        //如果地址为空，则为any，不生成地址对象
        if(AliStringUtils.isEmpty(ipAddressString)) {
            policyObject.setName("any");
            return policyObject;
        }

        String[] ipAddresses = ipAddressString.split(",");
        //创建对象或者，ip地址多于一个都创建对象，否则直接引用内容
        if(isCreateObject == true || ipAddresses.length > 1) {

            SecurityHillStoneR5ForZXTX r5 = new SecurityHillStoneR5ForZXTX();
            PolicyObjectDTO dto = null;

            if (isSNatPostAddress) {
                dto = r5.generateAddressObjectForNat(ipAddressString, theme, prefix, true, addressObjectName,null);
                policyObject.setName(" " + dto.getName());
            } else {
                dto = r5.generateAddressObject(ipAddressString, theme, prefix, true, addressObjectName,null,0);
                policyObject.setName(" " + dto.getName());
            }
            policyObject.setCommandLine(dto.getCommandLine());

        } else {
            //若为单个ip范围，则建对象
            if(IpUtils.isIPRange(ipAddressString)) {
                SecurityHillStoneR5ForZXTX r5 = new SecurityHillStoneR5ForZXTX();
                PolicyObjectDTO dto = r5.generateAddressObject(ipAddressString, theme, "", true, addressObjectName,null,0);

                if (isSNatPostAddress) {
                    policyObject.setName(" " + dto.getName());
                } else {
                    policyObject.setName(" " + dto.getName());
                }
                policyObject.setCommandLine(dto.getCommandLine());
            } else {
                if(!AliStringUtils.isEmpty(addressObjectName)) {
                    policyObject.setName(String.format("%s", addressObjectName));
                } else {
                    policyObject.setName(String.format("%s", ipAddressString));
                }
                policyObject.setCommandLine("");
            }
        }
        return policyObject;
    }


    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, boolean isCreateObject) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(" service any");
        policyObjectDTO.setCommandLine("");

        SecurityHillStoneR5ForZXTX r5 = new SecurityHillStoneR5ForZXTX();
        HillstoneForZXTX h5 = new HillstoneForZXTX();

        if(serviceList == null || serviceList.size() == 0) {
            return policyObjectDTO;
        } else if(serviceList.size() == 1) {
            ServiceDTO service = serviceList.get(0);
            String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
            if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                policyObjectDTO.setName(" service any");
                return policyObjectDTO;
            }
            if(AliStringUtils.isEmpty(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                if(protocol.equalsIgnoreCase("ICMP")) {
                    policyObjectDTO.setName(" service icmp");
                } else {
                    policyObjectDTO.setName(String.format(" service %s-any", protocol.toLowerCase()));
                }
            } else {
                PolicyObjectDTO dto = h5.generateServiceObject(serviceList, isCreateObject, serviceObjectName);
                String commandline = dto.getCommandLine();
                String name = " " + dto.getJoin();
                policyObjectDTO.setName(name.replace("\n", ""));
                policyObjectDTO.setCommandLine(commandline);
            }
        } else {
            PolicyObjectDTO dto = h5.generateServiceObject(serviceList, isCreateObject, serviceObjectName);
            String commandline = dto.getCommandLine();
            String name = " " + dto.getJoin();
            policyObjectDTO.setName(name.replace("\n", ""));
            policyObjectDTO.setCommandLine(commandline);
        }

        return policyObjectDTO;
    }

    //从安全策略复制过来的公共方法

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin("service " + existsServiceName +"\n");
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        boolean groupFlag = false;
        //对象名称集合, 不一定会建组，建组条件：有2组及以上协议，其中有一个协议，不带端口
        List<String> serviceNameList = new ArrayList<>();

        //直接写内容，当端口是any时，可以直接写内容，但有具体端口时，就必须创建对象
        if (serviceDTOList != null && serviceDTOList.size() == 1) {
            //无端口时，有返回值，  有端口就需要建对象，是没有返回值的
            String command = getServiceNameByNoPort(serviceDTOList.get(0), 0);
            if (StringUtils.isNotBlank(command)) {
                dto.setObjectFlag(false);
                dto.setCommandLine(String.format("service %s\n", command));
                return dto;
            }
        }

        //多个服务，必须建对象或组
        createObjFlag = true;
        dto.setObjectFlag(createObjFlag);


        StringBuilder objNameSb = new StringBuilder();

        //多个，建对象
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toUpperCase();

            //字符串转换数组取首位
            char[] protocolchar = protocolString.toCharArray();
//            String protocols = String.valueOf(protocolchar[0]);

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("service Any \n");
                return dto;
            }


            String command = getServiceNameByNoPort(service, 0);
            if(StringUtils.isNotBlank(command)) {
                groupFlag = true;
                serviceNameList.add(command);
                continue;
            }

            objNameSb.append(getServiceName(service)+"_");

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)){

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                //源、目的都有具体的值，或源不为空，目的为空
                /*if(srcPorts != null && srcPorts.length >0) {
                    for(String srcPort: srcPorts) {
                        for(String dstPort: dstPorts) {
                            sb.append(String.format("%s dst-port %s src-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT), PortUtils.getPortString(srcPort, PortUtils.BLANK_FORMAT)));
                        }
                    }
                }*/
                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                for (String dstPort : dstPorts) {
//                    sb.append(String.format("dst-port %s\n", PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    sb.append(String.format("%s dst-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                }
            }else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %d %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
            }
        }

        //有对象
        if(createObjFlag && sb.toString().length() > 0) {
            String objName = objNameSb.toString();
            if (objName.substring(objName.length() - 1).equals("_")) {
                objName = objName.substring(0, objName.length() - 1);
            }

            //service name限制长度
            if(objName.length() > getMaxNameLength()) {
                String shortName = objName.substring(0, getMaxNameLength()-5);
                objName = String.format("%s_etcs", shortName.substring(0, shortName.lastIndexOf("_")));
            }

            dto.setName(objName);
            serviceNameList.add(objName);
            String tmp = sb.toString();
            StringBuilder tmpSb = new StringBuilder();
            tmpSb.append(String.format("service %s\n", objName));
            tmpSb.append(tmp);
            tmpSb.append("exit\n");
            sb.setLength(0);
            sb.append(tmpSb);
        }


        //要建组
        if(groupFlag){
            String groupName = getServiceName(serviceDTOList);
            sb.append(String.format("servgroup %s\n", groupName));
            for(String objName : serviceNameList){
                sb.append(String.format("service %s\n", objName));
            }
            sb.append("exit\n");
            dto.setJoin("service " + groupName +"\n");
            dto.setName(groupName);
        }else{
            dto.setJoin("service " + dto.getName() +"\n");
        }

        dto.setCommandLine(sb.toString());
        return dto;
    }

    //------
    private String getServiceNameByNoPort(ServiceDTO service, Integer ipType) {
        String command = "";
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            command = " any ";
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            if (StringUtils.isBlank(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                command =  protocolString + "-any ";
            }
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            if(ipType.intValue() == IpTypeEnum.IPV6.getCode()){
                // ipv6的 icmp
                command = protocolString + "v6 ";
            } else {
                command = protocolString + " ";
            }
        }
        return command;
    }

    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();

        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol())).toUpperCase();

        //字符串转换数组取首位
        char[] protocolchar = protocolString.toCharArray();
        String protocols = String.valueOf(protocolchar[0]);



//        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocols);
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        } else if (protocolString.equalsIgnoreCase("ICMP")) {
            return sb.toString();
        } else if (!dto.getDstPorts().equalsIgnoreCase("any") && !dto.getDstPorts().equals("0-65535")) {
            String[] dstPorts = dto.getDstPorts().split(",");
            String[] var5 = dstPorts;
            int var6 = dstPorts.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String dstPort = var5[var7];
                if (PortUtils.isPortRange(dstPort)) {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
//                    sb.append(String.format("_%s-%s", startPort, endPort));
                    sb.append(String.format("_%s-%s", startPort, endPort));
                } else {
                    if (var7 == 0) {
                        sb.append(String.format("%s", dstPort));
                    } else {
                        sb.append(String.format("_%s", dstPort));
                    }
                }
            }

            return sb.toString();
//            return sb.toString().toLowerCase();
        } else {
            return sb.toString();
        }

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
            String shortName = name.substring(0, getMaxNameLength()-6);
            name = String.format("%s_etcsg", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }



    String getAddressObjectString(String[] addressList, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("address %s\n", name));
        for(String ipAddress:addressList) {
            if(IpUtils.isIP(ipAddress)) {
                sb.append(String.format("ip %s/32", ipAddress));
            } else if(IpUtils.isIPSegment(ipAddress)) {
                sb.append("ip " + ipAddress + "\n");
            } else {
                String start = IpUtils.getStartIpFromIpAddress(ipAddress);
                String end = IpUtils.getEndIpFromIpAddress(ipAddress);
                sb.append(String.format("range %s %s\n", start, end));
            }
        }
        sb.append("exit\n\n");
        return sb.toString();
    }


    String getServiceObjectString(List<ServiceDTO> serviceList, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\n", name));
        for(ServiceDTO service: serviceList) {
            if(service.getDstPorts() != null) {
                String portString = service.getDstPorts();
                String[] ports = portString.split(SEPERATOR);
                for(String port:ports) {
                    sb.append(String.format("%s ", ProtocolUtils.getProtocolByString(service.getProtocol())));
                    sb.append("dst-port " + port + "\n");
                }
            } else {
                sb.append(String.format("%s \n", service.getProtocol()));
            }
        }
        sb.append("exit\n\n");
        return sb.toString();
    }
    public static void main(String[] args) {
        HillstoneForZXTX r004 = new HillstoneForZXTX();
        System.out.println("--------------------------------------------------------------------------");
        //源nat
        DNatPolicyDTO sNatPolicyDTO = new DNatPolicyDTO();
        sNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        sNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        sNatPolicyDTO.setSrcIp("");
        sNatPolicyDTO.setDstIp("2.2.2.2");
        sNatPolicyDTO.setPostIpAddress("1.1.1.1");

        sNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setTheme("w1");


        String snat = r004.generateDNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");
    }
}
