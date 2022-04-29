package com.abtnetworks.totems.common.commandline.nat;


import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5ForShanShi;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class HillstoneForShanShi implements NatPolicyGenerator {

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

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostSrcIpSystem());
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
        if(!IpUtils.isIP(postAddressObjectName) &&  !IpUtils.isIPSegment(postAddressObjectName)){
            postAddressObjectName = "address-book " + postAddressObjectName;
        }
        sb.append(String.format("snatrule from %s to %s%s%s trans-to %s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()),  postAddressObjectName));

        sb.append("exit\nend\n");
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getPostDstIpSystem());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostDstIpSystem());
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

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
        if("any".equalsIgnoreCase(policyDTO.getPostPort())){
            postPortString = "";
        }
        sb.append(String.format("dnatrule %sfrom %s to %s%s trans-to %s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postAddressObject.getName(), postPortString));

        sb.append("exit\nend\n");
        return sb.toString();
    }


    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        //山石BOTH NAT命令行为生成一个SNAT再生成一个DNAT
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",null);
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",null);
        PolicyObjectDTO postSrcAddressObject = getAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), true, policyDTO.getPostSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "",null);
        PolicyObjectDTO postDstAddressObject = getAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), true,policyDTO.getPostDstAddressObjectName(), policyDTO.isCreateObjFlag(), "",null);
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
        if(!IpUtils.isIP(postAddressObjectName) &&  !IpUtils.isIPSegment(postAddressObjectName)){
            postAddressObjectName = "address-book " + postAddressObjectName;
        }
        sb.append(String.format("snatrule from %s to %s%s%s trans-to address-book %s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()), postAddressObjectName));

        sb.append("exit\n");

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
        if(CommonConstants.ANY.equalsIgnoreCase(policyDTO.getPostPort())){
            postPortString = "";
        }
        sb.append(String.format("dnatrule %sfrom %s to %s%s trans-to %s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postDstAddressObject.getName(), postPortString));

        sb.append("exit\n");

        sb.append("end\n");
        return sb.toString();
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, String addressObjectName, boolean isCreateObject, String prefix) {
        return getAddressObject(ipAddressString, theme, false, addressObjectName, isCreateObject, prefix,null);
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, boolean isSNatPostAddress, String addressObjectName, boolean isCreateObject, String prefix,String ipSystem) {
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

            SecurityHillStoneR5ForShanShi r5 = new SecurityHillStoneR5ForShanShi();
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
                SecurityHillStoneR5ForShanShi r5 = new SecurityHillStoneR5ForShanShi();
                PolicyObjectDTO dto = r5.generateAddressObject(ipAddressString, theme, "", true, addressObjectName,ipSystem,0);

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


    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, Integer idleTimeout) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(" service any");
        policyObjectDTO.setCommandLine("");

        SecurityHillStoneR5ForShanShi r5 = new SecurityHillStoneR5ForShanShi();


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
        HillstoneForShanShi r004 = new HillstoneForShanShi();
        System.out.println("--------------------------------------------------------------------------");
        //源nat
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        sNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        sNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setTheme("w1");


        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");
    }
}
