package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5ForzheShang;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.IPTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NatHillstoneForzheShang implements NatPolicyGenerator {

    private static String SEPERATOR = ",";

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
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null);

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
        if(IpTypeEnum.IPV6.getCode().equals(policyDTO.getIpType())){
            // 对浙商而言,老的山石设备不支持ip类型为ipv6，故这里直接返回(本不该将业务写在这，考虑到主线上山石劳设备可能需要生成ipv6的nat命令行)
            return "命令行生成器[HillstoneStoneOS]暂不支持ipv6的NAT命令行生成";
        }else{
            String dealPostAddressObjectName = StringUtils.isNotBlank(postAddressObjectName) ? postAddressObjectName.trim() :postAddressObjectName ;

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
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null);

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

        if (IpTypeEnum.IPV6.getCode().equals(policyDTO.getIpType())) {
            return "命令行生成器[HillstoneStoneOS]暂不支持ipv6的NAT命令行生成";
        } else {
            String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf()) ? "" : String.format("ingress-interface %s ", policyDTO.getSrcItf());

            String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort()) ? "" : String.format(" port %s", policyDTO.getPostPort());
            if ("any".equalsIgnoreCase(policyDTO.getPostPort())) {
                postPortString = "";
            }
            sb.append(String.format("dnatrule %sfrom%s to%s%s trans-to%s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                    serviceObject.getName(), postAddressObject.getName(), postPortString));
        }

        sb.append("exit\nend\n");
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
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null);

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
        if (IpTypeEnum.IPV6.getCode().equals(policyDTO.getIpType())) {
            return "命令行生成器[HillstoneStoneOS]暂不支持ipv6的NAT命令行生成";
        } else {
            String dealPostAddressObjectName = StringUtils.isNotBlank(postAddressObjectName) ? postAddressObjectName.trim() :postAddressObjectName ;

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

            String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

            String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
            if(CommonConstants.ANY.equalsIgnoreCase(policyDTO.getPostPort())){
                postPortString = "";
            }
            sb.append(String.format("dnatrule %sfrom%s to%s%s trans-to%s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                    serviceObject.getName(), postDstAddressObject.getName(), postPortString));
        }

        sb.append("exit\n");

        sb.append("end\n");
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

            SecurityHillStoneR5ForzheShang r5 = new SecurityHillStoneR5ForzheShang();
            PolicyObjectDTO dto = null;

            if (isSNatPostAddress) {
                dto = r5.generateAddressObjectForNat(ipAddressString, theme, prefix, true, addressObjectName,ipSystem);
                policyObject.setName(" " + dto.getName());
            } else {
                dto = r5.generateAddressObject(ipAddressString, theme, prefix, true, addressObjectName,ipSystem,0);
                policyObject.setName(" " + dto.getName());
            }
            policyObject.setCommandLine(dto.getCommandLine());

        } else {
            //若为单个ip范围，则建对象
            if(IpUtils.isIPRange(ipAddressString)) {
                SecurityHillStoneR5ForzheShang r5 = new SecurityHillStoneR5ForzheShang();
                PolicyObjectDTO dto = r5.generateAddressObject(ipAddressString, theme, "", true, addressObjectName,ipSystem,0);

                if (isSNatPostAddress) {
                    policyObject.setName(" " + dto.getName());
                } else {
                    policyObject.setName(" " + dto.getName());
                }
                policyObject.setCommandLine(dto.getCommandLine());
            } else {
                if(!AliStringUtils.isEmpty(addressObjectName)) {
                    policyObject.setName(String.format(" %s", addressObjectName));
                } else {
                    policyObject.setName(String.format(" %s", ipAddressString));
                }
                policyObject.setCommandLine("");
            }
        }
        return policyObject;
    }


    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, Integer idleTimeout) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(" service any");
        policyObjectDTO.setCommandLine("");

        SecurityHillStoneR5ForzheShang r5 = new SecurityHillStoneR5ForzheShang();


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
        NatHillstoneForzheShang r004 = new NatHillstoneForzheShang();
        System.out.println("--------------------------------------------------------------------------");
        //源nat
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setSrcIp("1.1.1.1-1.1.1.2");
        sNatPolicyDTO.setDstIp("2.2.2.2-1.1.1.1");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        sNatPolicyDTO.setPostIpAddress("22.2.2.2-1.1.1.1");
//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setTheme("test_1");
        sNatPolicyDTO.setIpType(0);
        sNatPolicyDTO.setDstItf("cellular0/0");

        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");

        // 目的nat
        DNatPolicyDTO dNatPolicyDTO = new DNatPolicyDTO();
        dNatPolicyDTO.setSrcIp("1.1.1.1,1.1.1.2");
        dNatPolicyDTO.setDstIp("2.2.2.2,1.1.1.1");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        dNatPolicyDTO.setPostIpAddress("2.2.2.1");
//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        dNatPolicyDTO.setTheme("test_2");
        dNatPolicyDTO.setIpType(0);

        String dnat = r004.generateDNatCommandLine(dNatPolicyDTO);
        System.out.println(dnat);
        System.out.println("--------------------------------------------------------------------------");


        // 目的nat
        NatPolicyDTO natPolicyDTO = new NatPolicyDTO();
        natPolicyDTO.setSrcIp("1.1.1.1,1.1.1.2");
        natPolicyDTO.setDstIp("2.2.2.2,1.1.1.1");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        natPolicyDTO.setPostSrcIp("1.2.1.1,1.1.1.2");
        natPolicyDTO.setPostDstIp("2.1.2.2,1.1.1.1");

//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        natPolicyDTO.setTheme("test_3");
        natPolicyDTO.setIpType(0);

        String nat = r004.generateBothNatCommandLine(natPolicyDTO);
        System.out.println(nat);
        System.out.println("--------------------------------------------------------------------------");


    }
}
