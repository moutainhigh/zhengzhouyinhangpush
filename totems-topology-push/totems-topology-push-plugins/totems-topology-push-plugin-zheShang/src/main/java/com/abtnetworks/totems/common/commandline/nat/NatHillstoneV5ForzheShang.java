package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5ForzheShang;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneV5ForzheShang;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IP6Utils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NatHillstoneV5ForzheShang implements NatPolicyGenerator {

    private static String SEPERATOR = ",";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate hillstone5.5 nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null,policyDTO.getIpType());

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
        // 2021.6.15 按照需求修改,分三种情况。1.如果转换后的地址不填，trans-to eif-ip mode dynamicport。eif-ip后面不接any 2.如果转换后地址为单ip/子网，则trans-to 单ip mode dynamicport
        // 3. 如果转换后的地址为地址组对象/ip范围 则trans-to address-book "地址对象名称" mode dynamicport
        String dealPostAddressObjectName = StringUtils.isNotBlank(postAddressObjectName) ? postAddressObjectName.trim() :postAddressObjectName ;
        if(IpTypeEnum.IPV6.getCode().equals(policyDTO.getIpType())){
            if (" IPv6-any".equals(postAddressObjectName)) {
                postAddressObjectName = " eif-ip";
            } else if (IpUtils.isIPv6Range(dealPostAddressObjectName)){
                // ipv6 范围
                postAddressObjectName = " address-book" + postAddressObjectName;
            } else if (IpUtils.isIPv6(dealPostAddressObjectName) || IpUtils.isIPv6Subnet(dealPostAddressObjectName)) {
                postAddressObjectName = postAddressObjectName;
            } else {
                // ip范围  或 地址组对象 走else
                postAddressObjectName = " address-book" + postAddressObjectName;
            }
            sb.append(String.format("snatrule from%s to%s%s%s trans-to%s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                    AliStringUtils.isEmpty(policyDTO.getDstItf()) ? "" : (" eif " + policyDTO.getDstItf()), postAddressObjectName));
        }else{
            if (" any".equals(postAddressObjectName)) {
                postAddressObjectName = " eif-ip";
            } else if (IpUtils.isIP(dealPostAddressObjectName) || IpUtils.isIPSegment(dealPostAddressObjectName)) {
                postAddressObjectName = postAddressObjectName;
            } else {
                // ip范围  或 地址组对象 走else
                postAddressObjectName = " address-book" + postAddressObjectName;
            }
            sb.append(String.format("snatrule from%s to%s%s%s trans-to%s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                    AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()),  postAddressObjectName));
        }


        sb.append("exit\nend\n");
        sb.append(StringUtils.LF)
                .append("save").append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null,policyDTO.getIpType());

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
        String postAddressObjectName = postAddressObject.getName();

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf()) ? "" : String.format("ingress-interface %s ", policyDTO.getSrcItf());
        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort()) ? "" : String.format(" port %s", policyDTO.getPostPort());
        if ("any".equalsIgnoreCase(policyDTO.getPostPort())) {
            postPortString = "";
        }

        // ipv4 和ipv6共用一套dnat命令行
        sb.append(String.format("dnatrule %sfrom%s to%s%s trans-to%s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postAddressObject.getName(), postPortString));

        sb.append("exit\nend\n");
        sb.append(StringUtils.LF)
                .append("save").append(StringUtils.LF);
        return sb.toString();
    }


    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        //山石BOTH NAT命令行为生成一个SNAT再生成一个DNAT
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postSrcAddressObject = getAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), true, policyDTO.getPostSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postDstAddressObject = getAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), true,policyDTO.getPostDstAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null,policyDTO.getIpType());

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
        String dealPostAddressObjectName = StringUtils.isNotBlank(postAddressObjectName) ? postAddressObjectName.trim() :postAddressObjectName ;

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf()) ? "" : String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort()) ? "" : String.format(" port %s", policyDTO.getPostPort());
        if (CommonConstants.ANY.equalsIgnoreCase(policyDTO.getPostPort())) {
            postPortString = "";
        }

        if (IpTypeEnum.IPV6.getCode().equals(policyDTO.getIpType())) {
            if (" IPv6-any".equals(postAddressObjectName)) {
                postAddressObjectName = " eif-ip";
            } else if (IpUtils.isIPv6Range(dealPostAddressObjectName)) {
                // ipv6 范围
                postAddressObjectName = " address-book" + postAddressObjectName;
            } else if(IpUtils.isIPv6(dealPostAddressObjectName) || IpUtils.isIPv6Subnet(dealPostAddressObjectName)) {
                postAddressObjectName = postAddressObjectName;
            } else {
                // ip范围  或 地址组对象 走else
                postAddressObjectName = " address-book" + postAddressObjectName;
            }
            sb.append(String.format("snatrule from%s to%s%s%s trans-to%s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                    AliStringUtils.isEmpty(policyDTO.getDstItf()) ? "" : (" eif " + policyDTO.getDstItf()), postAddressObjectName));

            sb.append("exit\n");
            sb.append("nat\n");

            sb.append(String.format("dnatrule %sfrom%s to%s%s trans-to%s%s\n", srcItf,srcAddressObject.getName(), dstAddressObject.getName(),
                    serviceObject.getName(), postDstAddressObject.getName(),postPortString));
        } else {
            if (" any".equals(postAddressObjectName)) {
                postAddressObjectName = " eif-ip";
            } else if (IpUtils.isIP(dealPostAddressObjectName) || IpUtils.isIPSegment(dealPostAddressObjectName)) {
                postAddressObjectName = postAddressObjectName;
            } else {
                // ip范围  或 地址组对象 走else
                postAddressObjectName = " address-book" + postAddressObjectName;
            }
            // ipv4 逻辑
            sb.append(String.format("snatrule from%s to%s%s%s trans-to%s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                    AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()), postAddressObjectName));

            sb.append("exit\n");
            sb.append("nat\n");

            sb.append(String.format("dnatrule %sfrom%s to%s%s trans-to%s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                    serviceObject.getName(), postDstAddressObject.getName(), postPortString));
        }

        sb.append("exit\n");

        sb.append("end\n");
        sb.append(StringUtils.LF)
                .append("save").append(StringUtils.LF);
        return sb.toString();
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, String addressObjectName, boolean isCreateObject, String prefix) {
        return getAddressObject(ipAddressString, theme, false, addressObjectName, isCreateObject, prefix,null,null);
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, boolean isSNatPostAddress, String addressObjectName, boolean isCreateObject, String prefix, String ipSystem,Integer ipType) {
        PolicyObjectDTO policyObject = new PolicyObjectDTO();
        policyObject.setName("");
        policyObject.setCommandLine("");
        //如果地址为空，则为any，不生成地址对象
        if(AliStringUtils.isEmpty(ipAddressString) || ipAddressString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if(IpTypeEnum.IPV6.getCode().equals(ipType) ){
                policyObject.setName(" IPv6-any");
            }else {
                policyObject.setName(" any");
            }
            return policyObject;
        }

        String[] ipAddresses = ipAddressString.split(",");
        //创建对象或者，ip地址多于一个都创建对象，否则直接引用内容
        if(isCreateObject == true || ipAddresses.length > 1) {

            SecurityHillStoneV5ForzheShang r5 = new SecurityHillStoneV5ForzheShang();
            PolicyObjectDTO dto = null;

            if (isSNatPostAddress) {
                dto = r5.generateAddressObjectForNat(ipAddressString, theme, prefix, true, addressObjectName,ipSystem,ipType);
                policyObject.setName(" " + dto.getName());
            } else {
                dto = r5.generateAddressObject(ipAddressString, theme, prefix, true, addressObjectName,ipSystem,ipType);
                policyObject.setName(" " + dto.getName());
            }
            policyObject.setCommandLine(dto.getCommandLine());

        } else {

            if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
                //若为单个ip范围，则建对象
                if (IpUtils.isIPv6Range(ipAddressString)) {
                    SecurityHillStoneV5ForzheShang r5 = new SecurityHillStoneV5ForzheShang();
                    PolicyObjectDTO dto = r5.generateAddressObjectForNat(ipAddressString, theme, "", true, addressObjectName, ipSystem,ipType);

                    if (isSNatPostAddress) {
                        policyObject.setName(" " + dto.getName());
                    } else {
                        policyObject.setName(" " + dto.getName());
                    }
                    policyObject.setCommandLine(dto.getCommandLine());
                } else {
                    if (!AliStringUtils.isEmpty(addressObjectName)) {
                        policyObject.setName(String.format(" %s", addressObjectName));
                    } else {
                        policyObject.setName(String.format(" %s", ipAddressString));
                    }
                    policyObject.setCommandLine("");
                }
            } else {
                //若为单个ip范围，则建对象
                if (IpUtils.isIPRange(ipAddressString)) {
                    SecurityHillStoneV5ForzheShang r5 = new SecurityHillStoneV5ForzheShang();
                    PolicyObjectDTO dto = r5.generateAddressObjectForNat(ipAddressString, theme, "", true, addressObjectName, ipSystem, 0);

                    if (isSNatPostAddress) {
                        policyObject.setName(" " + dto.getName());
                    } else {
                        policyObject.setName(" " + dto.getName());
                    }
                    policyObject.setCommandLine(dto.getCommandLine());
                } else {
                    if (!AliStringUtils.isEmpty(addressObjectName)) {
                        policyObject.setName(String.format(" %s", addressObjectName));
                    } else {
                        policyObject.setName(String.format(" %s", ipAddressString));
                    }
                    policyObject.setCommandLine("");
                }
            }
        }
        return policyObject;
    }


    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, Integer idleTimeout,Integer ipType) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(" service any");
        policyObjectDTO.setCommandLine("");

        SecurityHillStoneV5ForzheShang r5 = new SecurityHillStoneV5ForzheShang();


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
                    if (IpTypeEnum.IPV6.getCode().equals(ipType)){
                        policyObjectDTO.setName(" service icmpv6");
                    }else{
                        policyObjectDTO.setName(" service icmp");
                    }
                } else if (protocol.equalsIgnoreCase("ICMPv6")){
                    policyObjectDTO.setName(" service icmpv6");
                } else {
                    policyObjectDTO.setName(String.format(" service %s-any", protocol.toLowerCase()));
                }
            } else {
                PolicyObjectDTO dto = r5.generateServiceObject(serviceList, idleTimeout, serviceObjectName);
                String commandline = dto.getCommandLine();
                String name = " " + dto.getJoin();
                policyObjectDTO.setName(name.replace("\n", ""));
                policyObjectDTO.setCommandLine(commandline);
            }
        } else {
            PolicyObjectDTO dto = r5.generateServiceObject(serviceList, idleTimeout, serviceObjectName);
            String commandline = dto.getCommandLine();
            String name = " " + dto.getJoin();
            policyObjectDTO.setName(name.replace("\n", ""));
            policyObjectDTO.setCommandLine(commandline);
        }

        return policyObjectDTO;
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
        NatHillstoneV5ForzheShang r004 = new NatHillstoneV5ForzheShang();
        System.out.println("--------------------------------------------------------------------------");
        //源nat
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setSrcIp("123::4-123::44");
        sNatPolicyDTO.setDstIp("44::43");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        sNatPolicyDTO.setPostIpAddress("322::4-32::41");
//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setTheme("test_1");
        sNatPolicyDTO.setIpType(1);
        sNatPolicyDTO.setDstItf("cellular0/0");
        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO serviceDTO =new ServiceDTO();
        serviceDTO.setProtocol("1");
        serviceList.add(serviceDTO);
        sNatPolicyDTO.setServiceList(serviceList);

        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");

        // 目的nat
        DNatPolicyDTO dNatPolicyDTO = new DNatPolicyDTO();
        dNatPolicyDTO.setSrcIp("421::1-421::1,421::3");
        dNatPolicyDTO.setDstIp("421::2");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        dNatPolicyDTO.setPostIpAddress("421::3");
//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        dNatPolicyDTO.setTheme("test_2");
        dNatPolicyDTO.setIpType(1);

        String dnat = r004.generateDNatCommandLine(dNatPolicyDTO);
        System.out.println(dnat);
        System.out.println("--------------------------------------------------------------------------");


        // 目的nat
        NatPolicyDTO natPolicyDTO = new NatPolicyDTO();
        natPolicyDTO.setSrcIp("2001:2:3:4:5:6:ffff:f,2001:2:3:4:5:6:ffff:19");
        natPolicyDTO.setDstIp("2002:2:3:4:5:6:ffff:19");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

//        natPolicyDTO.setPostSrcIp("2003:2:3:4:5:6:ffff:19");
        natPolicyDTO.setPostDstIp("2004:2:3:4:5:6:ffff:19");

//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        natPolicyDTO.setTheme("test_3");
        natPolicyDTO.setIpType(1);

        String nat = r004.generateBothNatCommandLine(natPolicyDTO);
        System.out.println(nat);
        System.out.println("--------------------------------------------------------------------------");


    }
}
