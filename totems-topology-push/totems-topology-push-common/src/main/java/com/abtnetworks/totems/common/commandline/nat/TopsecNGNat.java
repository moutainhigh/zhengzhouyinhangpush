package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityTopsec;
import com.abtnetworks.totems.common.commandline.security.SecurityTopsecNG;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service(value = "Topsec NG NAT")
public class TopsecNGNat implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate topsec 010-020 nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        SecurityTopsec topsec = new SecurityTopsec();
        SecurityTopsecNG topsecNG = new SecurityTopsecNG();
        PolicyObjectDTO srcAddress = topsec.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), null,null);
        PolicyObjectDTO dstAddress = topsec.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), null,null);
        PolicyObjectDTO service = topsecNG.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.getExistServiceNameList(),null);
        PolicyObjectDTO postSrcAddress = topsec.generateAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), policyDTO.getPostAddressObjectName(), null,null);

        // 设置对象名称到大对象中
        policyDTO.setAddressTypeMap(srcAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(dstAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(postSrcAddress.getAddressTypeMap());
        policyDTO.setServiceObjectNameList(service.getCreateServiceObjectName());

        String srcZoneJoin = StringUtils.isNotBlank(policyDTO.getSrcZone()) ? "srcarea '" + policyDTO.getSrcZone() + "'" : "";
        String dstZoneJoin = StringUtils.isNotBlank(policyDTO.getDstZone()) ? "dstarea '" + policyDTO.getDstZone() + "'" : "";

        StringBuilder sb = new StringBuilder();
        StringBuilder define = new StringBuilder();

        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())){
            define.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())){
            define.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if(StringUtils.isNotBlank(service.getCommandLine())) {
            define.append(String.format("%s", service.getCommandLine()));
        }

        if(StringUtils.isNotBlank(postSrcAddress.getCommandLine())) {
            define.append(String.format("%s", postSrcAddress.getCommandLine()));
        }

        if(define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }

        String serviceString = "";
        if(!AliStringUtils.isEmpty(service.getJoin())) {
            serviceString = String.format("orig-service \'%s\' ", service.getJoin());
        }

        sb.append("nat\n");
        sb.append("policy add ");
        if(StringUtils.isNotBlank(srcZoneJoin)){
            sb.append(srcZoneJoin).append(" ");
        }
        if(StringUtils.isNotBlank(dstZoneJoin)){
            sb.append(dstZoneJoin).append(" ");
        }
        if(StringUtils.isNotBlank(srcAddress.getJoin())){
            sb.append(String.format("orig-src \'%s\' ", srcAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(dstAddress.getJoin())){
            sb.append(String.format("orig-dst \'%s\' ", dstAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(serviceString)) {
            sb.append(serviceString);
        }

        if(StringUtils.isNotBlank(postSrcAddress.getJoin())){
            sb.append(String.format("trans-src %s ", postSrcAddress.getJoin()));
        }
        sb.append("enable yes\n");

        sb.append("end\n");

        StringBuffer rollbackShowCmd = new StringBuffer();
        rollbackShowCmd.append(srcZoneJoin).append(dstZoneJoin);
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getFirstIpNameJoin())){
            String rollbackSrcIp = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("orig-src \'%s\' ", srcAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackSrcIp);
        }
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getFirstIpNameJoin())){
            String rollbackDstIp = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("orig-dst \'%s\' ", dstAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackDstIp);
        }
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(service.getFirstServiceJoin())){
            String rollbackService = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : String.format("orig-service \'%s\' ", service.getFirstServiceJoin());
            rollbackShowCmd.append(rollbackService);
        }
        policyDTO.setRollbackShowCmd(rollbackShowCmd.toString());

        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        SecurityTopsec topsec = new SecurityTopsec();
        SecurityTopsecNG topsecNG = new SecurityTopsecNG();
        PolicyObjectDTO srcAddress = topsec.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), null,null);
        PolicyObjectDTO dstAddress = topsec.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), null,null);
        PolicyObjectDTO service = topsecNG.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.getExistServiceNameList(),null);
        PolicyObjectDTO postSrcAddress = topsec.generateAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), policyDTO.getPostAddressObjectName(), null,null);
        PolicyObjectDTO postService = topsecNG.generateServiceObject(policyDTO.getRestPostServiceList(), policyDTO.getExistPostServiceNameList(),null);

        // 设置对象名称到大对象中
        policyDTO.setAddressTypeMap(srcAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(dstAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(postSrcAddress.getAddressTypeMap());

        policyDTO.setServiceObjectNameList(service.getCreateServiceObjectName());
        policyDTO.getServiceObjectNameList().addAll(postService.getCreateServiceObjectName());


        String srcZoneJoin = StringUtils.isNotBlank(policyDTO.getSrcZone()) ? "srcarea '" + policyDTO.getSrcZone() + "'" : "";
        String dstZoneJoin = StringUtils.isNotBlank(policyDTO.getDstZone()) ? "dstarea '" + policyDTO.getDstZone() + "'" : "";

        StringBuilder sb = new StringBuilder();
        StringBuilder define = new StringBuilder();

        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())){
            define.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())){
            define.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if(StringUtils.isNotBlank(service.getCommandLine())) {
            define.append(String.format("%s", service.getCommandLine()));
        }

        if(StringUtils.isNotBlank(postService.getCommandLine())) {
            define.append(String.format("%s", postService.getCommandLine()));
        }

        if(StringUtils.isNotBlank(postSrcAddress.getCommandLine())) {
            define.append(String.format("%s", postSrcAddress.getCommandLine()));
        }

        if(define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }

        String serviceString = "";
        if(!AliStringUtils.isEmpty(service.getJoin())) {
            serviceString = String.format("orig-service \'%s\' ", service.getJoin());
        }

        sb.append("nat\n");
        sb.append("policy add ");
        if(StringUtils.isNotBlank(srcZoneJoin)){
            sb.append(srcZoneJoin).append(" ");
        }
        if(StringUtils.isNotBlank(dstZoneJoin)){
            sb.append(dstZoneJoin).append(" ");
        }
        if(StringUtils.isNotBlank(srcAddress.getJoin())){
            sb.append(String.format("orig-src \'%s\' ", srcAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(dstAddress.getJoin())){
            sb.append(String.format("orig-dst \'%s\' ", dstAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(serviceString)){
            sb.append(serviceString);
        }
        if(StringUtils.isNotBlank(postSrcAddress.getJoin())){
            sb.append(String.format("trans-dst %s ", postSrcAddress.getJoin()));
        }

        if(StringUtils.isNotBlank(postService.getJoin())){
            sb.append(String.format("trans-service %s ", postService.getJoin()));
        }
        sb.append("enable yes\n");
        sb.append("end\n");

        StringBuffer rollbackShowCmd = new StringBuffer();
        rollbackShowCmd.append(srcZoneJoin).append(dstZoneJoin);
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getFirstIpNameJoin())){
            String rollbackSrcIp = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("orig-src \'%s\' ", srcAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackSrcIp);
        }
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getFirstIpNameJoin())){
            String rollbackDstIp = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("orig-dst \'%s\' ", dstAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackDstIp);
        }
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(service.getFirstServiceJoin())){
            String rollbackService = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : String.format("orig-service \'%s\' ", service.getFirstServiceJoin());
            rollbackShowCmd.append(rollbackService);
        }
        policyDTO.setRollbackShowCmd(rollbackShowCmd.toString());

        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        SecurityTopsec topsec = new SecurityTopsec();
        SecurityTopsecNG topsecNG = new SecurityTopsecNG();
        PolicyObjectDTO srcAddress = topsec.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), null,null);
        PolicyObjectDTO dstAddress = topsec.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), null,null);
        PolicyObjectDTO service = topsecNG.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.getExistServiceNameList(),null);
        PolicyObjectDTO postSrcAddress = topsec.generateAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), policyDTO.getPostSrcAddressObjectName(), null,null);
        PolicyObjectDTO postDstAddress = topsec.generateAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), policyDTO.getPostDstAddressObjectName(), null,null);

        PolicyObjectDTO postService = topsecNG.generateServiceObject(policyDTO.getRestPostServiceList(), policyDTO.getExistPostServiceNameList(),null);

        // 设置对象名称到大对象中
        policyDTO.setAddressTypeMap(srcAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(dstAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(postSrcAddress.getAddressTypeMap());
        policyDTO.getAddressTypeMap().putAll(postDstAddress.getAddressTypeMap());

        policyDTO.setServiceObjectNameList(service.getCreateServiceObjectName());
        policyDTO.getServiceObjectNameList().addAll(postService.getCreateServiceObjectName());


        String srcZoneJoin = StringUtils.isNotBlank(policyDTO.getSrcZone()) ? "srcarea '" + policyDTO.getSrcZone() + "'" : "";
        String dstZoneJoin = StringUtils.isNotBlank(policyDTO.getDstZone()) ? "dstarea '" + policyDTO.getDstZone() + "'" : "";


        StringBuilder sb = new StringBuilder();
        StringBuilder define = new StringBuilder();

        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())){
            define.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())){
            define.append(String.format("%s", dstAddress.getCommandLine()));
        }

        if(StringUtils.isNotBlank(service.getCommandLine())) {
            define.append(String.format("%s", service.getCommandLine()));
        }

        if(StringUtils.isNotBlank(postService.getCommandLine())) {
            define.append(String.format("%s", postService.getCommandLine()));
        }

        if(postSrcAddress.isObjectFlag() && StringUtils.isNotBlank(postSrcAddress.getCommandLine())){
            define.append(String.format("%s", postSrcAddress.getCommandLine()));
        }
        if(postDstAddress.isObjectFlag() && StringUtils.isNotBlank(postDstAddress.getCommandLine())){
            define.append(String.format("%s", postDstAddress.getCommandLine()));
        }

        if(define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }

        String serviceString = "";
        if(!AliStringUtils.isEmpty(service.getJoin())) {
            serviceString = String.format("orig-service \'%s\' ", service.getJoin());
        }

        sb.append("nat\n");
        sb.append("policy add ");
        if(StringUtils.isNotBlank(srcZoneJoin)){
            sb.append(srcZoneJoin).append(" ");
        }
        if(StringUtils.isNotBlank(dstZoneJoin)){
            sb.append(dstZoneJoin).append(" ");
        }
        if(StringUtils.isNotBlank(srcAddress.getJoin())){
            sb.append(String.format("orig-src \'%s\' ", srcAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(dstAddress.getJoin())){
            sb.append(String.format("orig-dst \'%s\' ", dstAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(serviceString)){
            sb.append(serviceString);
        }
        if(StringUtils.isNotBlank(postSrcAddress.getJoin())){
            sb.append(String.format("trans-src %s ", postSrcAddress.getJoin()));
        }
        if(StringUtils.isNotBlank(postDstAddress.getJoin())){
            sb.append(String.format("trans-dst %s ", postDstAddress.getJoin()));
        }

        if(StringUtils.isNotBlank(postService.getJoin())){
            sb.append(String.format("trans-service %s ", postService.getJoin()));
        }
        
        sb.append("\n");
        sb.append("end\n");

        StringBuffer rollbackShowCmd = new StringBuffer();
        rollbackShowCmd.append(srcZoneJoin).append(dstZoneJoin);
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getFirstIpNameJoin())){
            String rollbackSrcIp = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("orig-src \'%s\' ", srcAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackSrcIp);
        }
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getFirstIpNameJoin())){
            String rollbackDstIp = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("orig-dst \'%s\' ", dstAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackDstIp);
        }
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(service.getFirstServiceJoin())){
            String rollbackService = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : String.format("orig-service \'%s\' ", service.getFirstServiceJoin());
            rollbackShowCmd.append(rollbackService);
        }
        policyDTO.setRollbackShowCmd(rollbackShowCmd.toString());

        return sb.toString();
    }



    public static void main(String[] args) {
        TopsecNGNat r004 = new TopsecNGNat();
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

        dnatPolicyDTO.setTheme("w1");
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

        bothNatDTO.setTheme("w1");
        String bothNat = r004.generateBothNatCommandLine(bothNatDTO);
        System.out.println(bothNat);

    }


}
