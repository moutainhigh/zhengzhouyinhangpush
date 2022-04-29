package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecuritySangfor;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description 服务可以先整体再离散，地址只能整体
 * @Version
 * @Created by hw on '2020/8/11'.
 */
@Slf4j
@Service(value = "Sangfor Nat")
public class SangforNat implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate 深信服（sangfor） nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        SecuritySangfor sangfor = new SecuritySangfor();

        PolicyObjectDTO srcAddress = sangfor.generateAddressObjectForNat(policyDTO.getInsideAddress(), policyDTO.getTheme(), policyDTO.getInsideAddressName(),null);
        PolicyObjectDTO dstAddress = sangfor.generateAddressObjectForNat(policyDTO.getGlobalAddress(), policyDTO.getTheme(), policyDTO.getGlobalAddressName(),null);
        String insidePort = policyDTO.getInsidePort();
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol(policyDTO.getProtocol());
        serviceDTO.setDstPorts(insidePort);
        serviceDTOList.add(serviceDTO);
        //这种只能整体复用
        List<String> existsServiceName = new LinkedList<>();
        String serviceCommand = sangfor.generateServiceObject(serviceDTOList, existsServiceName);
//        PolicyObjectDTO postSrcAddress = sangfor.generateAddressObjectForNat(policyDTO.getGlobalAddress(), policyDTO.getTheme(), policyDTO.getGlobalAddressName());

        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        if (policyDTO.isVsys()) {
            sb.append("vsys change " + policyDTO.getVsysName() + "\n");
            sb.append("config\n");
        }

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if (StringUtils.isNotBlank(serviceCommand)) {
            sb.append(String.format("%s", serviceCommand));
        }

//        if(StringUtils.isNotBlank(postSrcAddress.getCommandLine())) {
//            sb.append(String.format("%s", postSrcAddress.getCommandLine()));
//        }

        sb.append(String.format("snat-rule %s top\n", policyDTO.getTheme()));
        sb.append("enable\n");

        if (StringUtils.isNotBlank(policyDTO.getToZone())) {
            sb.append(String.format("src-zones %s\n", policyDTO.getToZone()));
        }

        if (StringUtils.isNotBlank(srcAddress.getJoin())) {
            sb.append(String.format("src-ipgroups %s\n", srcAddress.getJoin()));
        }

        if (StringUtils.isNotBlank(dstAddress.getJoin())) {
            sb.append(String.format("dst-ipgroups %s\n", dstAddress.getJoin()));
        }

        if (CollectionUtils.isNotEmpty(existsServiceName)) {
            for (String serviceName : existsServiceName) {
                sb.append(String.format("service %s\n", serviceName));
            }

        }
        String globalIp = policyDTO.getGlobalAddress();
        if (StringUtils.isNotBlank(globalIp)) {
            globalIp = globalIp.split(",")[0];
            if (IpUtils.isIPSegment(globalIp)) {
                globalIp = IpUtils.getStartIp(globalIp) + "-" + IpUtils.getEndIp(globalIp);
            } else if (IpUtils.isIP(globalIp)) {
                globalIp = globalIp + "-" + globalIp;
            } else {

            }
            sb.append(String.format("transfer iprange %s static\n", globalIp));
        }

        sb.append("end\n");

        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {

        SecuritySangfor sangfor = new SecuritySangfor();
        PolicyObjectDTO srcAddress = sangfor.generateAddressObjectForNat(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(),null);
        PolicyObjectDTO dstAddress = sangfor.generateAddressObjectForNat(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(),null);
        List<String> existsServiceNames = policyDTO.getExistServiceNameList();
        String serviceCommand = sangfor.generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName(), policyDTO.getRestServiceList(), existsServiceNames);

//        PolicyObjectDTO postSrcAddress = sangfor.generateAddressObjectForNat(policyDTO.getPostIpAddress(), policyDTO.getTheme(), policyDTO.getPostAddressObjectName());

        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        if (policyDTO.isVsys()) {
            sb.append("vsys change " + policyDTO.getVsysName() + "\n");
            sb.append("config\n");
        }

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if (StringUtils.isNotBlank(serviceCommand)) {
            sb.append(String.format("%s", serviceCommand));
        }
//
//        if(StringUtils.isNotBlank(postSrcAddress.getCommandLine())) {
//            sb.append(String.format("%s", postSrcAddress.getCommandLine()));
//        }

        sb.append(String.format("snat-rule %s top\n", policyDTO.getTheme()));
        sb.append("enable\n");

        if (StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("src-zones %s\n", policyDTO.getSrcZone()));
        }

        if (StringUtils.isNotBlank(srcAddress.getJoin())) {
            sb.append(String.format("src-ipgroups %s\n", srcAddress.getJoin()));
        }

        if (StringUtils.isNotBlank(dstAddress.getJoin())) {
            sb.append(String.format("dst-ipgroups %s\n", dstAddress.getJoin()));
        }

        if (CollectionUtils.isNotEmpty(existsServiceNames)) {
            for (String serviceName : existsServiceNames) {
                sb.append(String.format("service %s\n", serviceName));
            }

        }
        String postIp = policyDTO.getPostIpAddress();
        if (StringUtils.isNotBlank(postIp)) {
            postIp = postIp.split(",")[0];
            if (IpUtils.isIPSegment(postIp)) {
                postIp = IpUtils.getStartIp(postIp) + "-" + IpUtils.getEndIp(postIp);
            } else if (IpUtils.isIP(postIp)) {
                postIp = postIp + "-" + postIp;
            } else {

            }
            sb.append(String.format("transfer iprange %s dynamic\n", postIp));
        }

        sb.append("end\n");


        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        SecuritySangfor sangfor = new SecuritySangfor();
        PolicyObjectDTO srcAddress = sangfor.generateAddressObjectForNat(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(),null);
        PolicyObjectDTO dstAddress = sangfor.generateAddressObjectForNat(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(),null);
        List<String> existsServiceNames = policyDTO.getExistServiceNameList();
        String serviceCommand = sangfor.generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName(), policyDTO.getRestServiceList(), existsServiceNames);

        PolicyObjectDTO postSrcAddress = sangfor.generateAddressObjectForNat(policyDTO.getPostIpAddress(), policyDTO.getTheme(), policyDTO.getPostAddressObjectName(),null);


        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        if (policyDTO.isVsys()) {
            sb.append("vsys change " + policyDTO.getVsysName() + "\n");
            sb.append("config\n");
        }

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if (StringUtils.isNotBlank(serviceCommand)) {
            sb.append(String.format("%s", serviceCommand));
        }

        if (StringUtils.isNotBlank(postSrcAddress.getCommandLine())) {
            sb.append(String.format("%s", postSrcAddress.getCommandLine()));
        }

        sb.append(String.format("dnat-rule %s top\n", policyDTO.getTheme()));
        sb.append("enable\n");

        if (StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("src-zones %s\n", policyDTO.getSrcZone()));
        }

        if (StringUtils.isNotBlank(srcAddress.getJoin())) {
            sb.append(String.format("src-ipgroups %s\n", srcAddress.getJoin()));
        }

        if (StringUtils.isNotBlank(dstAddress.getJoin())) {
            sb.append(String.format("dst-ipgroups %s\n", dstAddress.getJoin()));
        }
        if (CollectionUtils.isNotEmpty(existsServiceNames)) {
            for (String serviceName : existsServiceNames) {
                sb.append(String.format("service %s\n", serviceName));
            }

        }

        if (StringUtils.isNotBlank(policyDTO.getPostPort()) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(policyDTO.getPostPort())) {
            if (StringUtils.isNotBlank(postSrcAddress.getJoin())) {
                sb.append(String.format("transfer ipgroup %s port %s\n", postSrcAddress.getJoin(), policyDTO.getPostPort()));
            }
        } else {
            if (StringUtils.isNotBlank(postSrcAddress.getJoin())) {
                sb.append(String.format("transfer ipgroup %s\n", postSrcAddress.getJoin()));
            }
        }

        sb.append("end\n");
        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        SecuritySangfor sangfor = new SecuritySangfor();
        PolicyObjectDTO srcAddress = sangfor.generateAddressObjectForNat(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(),null);
        PolicyObjectDTO dstAddress = sangfor.generateAddressObjectForNat(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(),null);
        List<String> existsServiceNames = policyDTO.getExistServiceNameList();
        String serviceCommand = sangfor.generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName(), policyDTO.getRestServiceList(), existsServiceNames);

        PolicyObjectDTO postSrcAddress = sangfor.generateAddressObjectForNat(policyDTO.getPostSrcIp(), policyDTO.getTheme(), policyDTO.getPostSrcAddressObjectName(),null);
        PolicyObjectDTO postDstAddress = sangfor.generateAddressObjectForNat(policyDTO.getPostDstIp(), policyDTO.getTheme(), policyDTO.getPostDstAddressObjectName(),null);
        List<String> existsPostServiceNames = policyDTO.getExistPostServiceNameList();
//        String servicePostCommand =  sangfor.generateServiceObject( policyDTO.getPostServiceList(),policyDTO.getPostServiceObjectName() , policyDTO.getRestPostServiceList(), existsPostServiceNames);


        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        if (policyDTO.isVsys()) {
            sb.append("vsys change " + policyDTO.getVsysName() + "\n");
            sb.append("config\n");
        }

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if (StringUtils.isNotBlank(serviceCommand)) {
            sb.append(String.format("%s", serviceCommand));
        }
//        if(StringUtils.isNotBlank(servicePostCommand)) {
//            sb.append(String.format("%s", servicePostCommand));
//        }
        if (postSrcAddress.isObjectFlag() && StringUtils.isNotBlank(postSrcAddress.getCommandLine())) {
            sb.append(String.format("%s", postSrcAddress.getCommandLine()));
        }
        if (postDstAddress.isObjectFlag() && StringUtils.isNotBlank(postDstAddress.getCommandLine())) {
            sb.append(String.format("%s", postDstAddress.getCommandLine()));
        }

        sb.append(String.format("bnat-rule %s top\n", policyDTO.getTheme()));
        sb.append("enable\n");

        if (StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("src-zones %s\n", policyDTO.getSrcZone()));
        }

        if (StringUtils.isNotBlank(srcAddress.getJoin())) {
            sb.append(String.format("src-ipgroups %s\n", srcAddress.getJoin()));
        }

        if (StringUtils.isNotBlank(dstAddress.getJoin())) {
            sb.append(String.format("dst-ipgroups %s\n", dstAddress.getJoin()));
        }

        if (CollectionUtils.isNotEmpty(existsServiceNames)) {
            for (String existsServiceName : existsServiceNames) {
                sb.append(String.format("service %s\n", existsServiceName));
            }

        }


        if (StringUtils.isNotBlank(postSrcAddress.getJoin())) {
            sb.append(String.format("transfer-src ipgroup %s\n", postSrcAddress.getJoin()));
        }

        if (StringUtils.isNotBlank(policyDTO.getPostPort()) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(policyDTO.getPostPort())) {
            if (StringUtils.isNotBlank(postDstAddress.getJoin())) {
                sb.append(String.format("transfer-dst ipgroup %s port %s\n", postDstAddress.getJoin(), policyDTO.getPostPort()));
            }
        } else {
            if (StringUtils.isNotBlank(postDstAddress.getJoin())) {
                sb.append(String.format("transfer-dst ipgroup %s\n", postDstAddress.getJoin()));
            }
        }

        sb.append("end\n");

        return sb.toString();

    }

    public static void main(String[] args) {
        SangforNat r004 = new SangforNat();
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

       /* sNatPolicyDTO.setSrcZone("trust");
        sNatPolicyDTO.setDstZone("untrust");

        sNatPolicyDTO.setSrcItf("srcItf");
        sNatPolicyDTO.setDstItf("dstItf");*/

        sNatPolicyDTO.setTheme("w1");

     /*   sNatPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        sNatPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());

        sNatPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
        sNatPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
        sNatPolicyDTO.setPostAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());*/

        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");


        DNatPolicyDTO dnatPolicyDTO = new DNatPolicyDTO();

        dnatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        dnatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        dnatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        dnatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        dnatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        dnatPolicyDTO.setPostPort("27");

        dnatPolicyDTO.setSrcZone("trust");
        dnatPolicyDTO.setDstZone("untrust");

        dnatPolicyDTO.setSrcItf("srcItf");
        dnatPolicyDTO.setDstItf("dstItf");

        dnatPolicyDTO.setTheme("w2");
        String dnat = r004.generateDNatCommandLine(dnatPolicyDTO);
        System.out.println(dnat);

        System.out.println("--------------------------------------------------------------------------");
        NatPolicyDTO bothNatDTO = new NatPolicyDTO();
        bothNatDTO.setSrcIp("192.168.2.1,192.168.2.2");
        bothNatDTO.setDstIp("172.16.2.1,172.16.2.2");
//        bothNatDTO.setSrcIp("");
//        bothNatDTO.setDstIp("");
        bothNatDTO.setPostSrcIp("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        bothNatDTO.setPostDstIp("172.16.1.0/24,7.7.7.7,63.2.2.2-75.3.3.6");
        bothNatDTO.setServiceList(ServiceDTO.getServiceList());
        bothNatDTO.setRestServiceList(ServiceDTO.getServiceList());
        bothNatDTO.setPostPort("27");
        bothNatDTO.setRestPostServiceList(ServiceDTO.getServiceList());

       /* bothNatDTO.setSrcZone("trust");
        bothNatDTO.setDstZone("untrust");

        bothNatDTO.setSrcItf("srcItf");
        bothNatDTO.setDstItf("dstItf");*/

        bothNatDTO.setTheme("w3");
        String bothNat = r004.generateBothNatCommandLine(bothNatDTO);
        System.out.println(bothNat);

    }
}
