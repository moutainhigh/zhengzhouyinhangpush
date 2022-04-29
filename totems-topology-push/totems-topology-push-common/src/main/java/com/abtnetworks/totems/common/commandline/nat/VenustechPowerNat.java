package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityLeadSecPowerV;
import com.abtnetworks.totems.common.commandline.security.SecurityVenustechPowerV;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 * @Title:
 * @Description: 网御nat命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service()
public class VenustechPowerNat implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("VenustechPower nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    private void preCommand(StringBuffer sb, boolean isVs, String vName) {
        return;
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {

        StringBuffer sb = new StringBuffer();

        preCommand(sb, policyDTO.isVsys(), policyDTO.getVsysName());

        if (StringUtils.isEmpty(policyDTO.getInsideAddress()) || policyDTO.getInsideAddress().split(",").length != 1){
            return "内网地址不可为空，且只能为单ip";
        }
        if (StringUtils.isEmpty(policyDTO.getGlobalAddress()) || policyDTO.getGlobalAddress().split(",").length != 1){
            return "外网地址不可为空，且只能为单ip";
        }
        String insideAddress = policyDTO.getInsideAddress();
        String globalAddress = policyDTO.getGlobalAddress();
        SecurityVenustechPowerV securityLeadSecPowerV = new SecurityVenustechPowerV();
        PolicyObjectDTO insidePolicyObjectDTO = securityLeadSecPowerV.generateAddressObject(insideAddress, policyDTO.getTheme(), policyDTO.isCreateObject(), policyDTO.getInsideAddressName(),null);
//        PolicyObjectDTO globalAddressObjectDTO = generateAddressObject(globalAddress, policyDTO.getTheme(), false, policyDTO.getGlobalAddressName(),null);
        //定义对象
        if (insidePolicyObjectDTO.isObjectFlag() && StringUtils.isNotBlank(insidePolicyObjectDTO.getCommandLine())) {
            sb.append(String.format("%s\n", insidePolicyObjectDTO.getCommandLine()));
        }
//        if (StringUtils.isNotBlank(globalAddressObjectDTO.getCommandLine())) {
//            sb.append(String.format("%s\n", globalAddressObjectDTO.getCommandLine()));
//        }

        sb.append(String.format("rule add type ipmap name \"%s\" ", policyDTO.getTheme())).append(" pa ");

        if (StringUtils.isNotEmpty(globalAddress)) {
            String[] addresses = globalAddress.split(",");
            sb.append(addresses[0]);

        } else {
            sb.append(" any ");
        }

        sb.append(" ia ").append(insidePolicyObjectDTO.getJoin());
        if (StringUtils.isNotEmpty(policyDTO.getInDevItf())) {
            sb.append(" iif ").append(policyDTO.getInDevItf());
        }else {
            sb.append(" iif any ");
        }
        sb.append(CommonConstants.LINE_BREAK);
        sb.append("newconfig save\n");
        return sb.toString();
    }

    private void addressPool(String globalAddress, StringBuffer sb) {
        String[] addresses = globalAddress.split(",");

        if (IpUtils.isIPRange(addresses[0])) {
            String startIp = IpUtils.getStartIpFromRange(addresses[0]);
            String endIp = IpUtils.getEndIpFromRange(addresses[0]);
            sb.append(String.format(" %s:%s ", startIp, endIp));
        } else if (IpUtils.isIPSegment(addresses[0])) {
            String ipStart = IpUtils.getStartIp(addresses[0]);
            String ipEnd = IpUtils.getStartIp(addresses[0]);
            sb.append(String.format(" %s/%s ", ipStart, ipEnd));
        } else {
            sb.append(addresses[0]).append(" ");
        }

    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        SecurityVenustechPowerV securityLeadSecPowerV = new SecurityVenustechPowerV();
        PolicyObjectDTO srcAddressObject = securityLeadSecPowerV.generateAddressObject(StringUtils.join(policyDTO.getRestSrcAddressList().toArray(),","), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getSrcAddressObjectName(),policyDTO.getExistSrcAddressList());
        PolicyObjectDTO dstAddressObject = securityLeadSecPowerV.generateAddressObject(StringUtils.join(policyDTO.getRestDstAddressList().toArray(),","), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getDstAddressObjectName(),policyDTO.getExistDstAddressList());
        PolicyObjectDTO policyObjectDTO = securityLeadSecPowerV.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.isCreateObjFlag(), policyDTO.getExistServiceNameList());
        PolicyObjectDTO postSrcAddressObject = generatePostAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName());
        StringBuffer sb = new StringBuffer();
        preCommand(sb, policyDTO.isVsys(), policyDTO.getVsysName());
        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if (policyObjectDTO.isObjectFlag() && StringUtils.isNotBlank(policyObjectDTO.getCommandLine())) {
            sb.append(String.format("%s\n", policyObjectDTO.getCommandLine()));
        }
        if (StringUtils.isNotBlank(postSrcAddressObject.getCommandLine())) {
            sb.append(postSrcAddressObject.getCommandLine());
        }


        sb.append(String.format("rule add type nat name \"%s\" ", policyDTO.getTheme()));
        if (!CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin())) {
            sb.append(" sa ").append(srcAddressObject.getJoin());
        }
        sb.append(" sat ").append(postSrcAddressObject.getJoin());
        if (!CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())) {
            sb.append(" da ").append(dstAddressObject.getJoin());
        }
        if (StringUtils.isNotEmpty(policyDTO.getDstItf())) {
            sb.append(" oif ").append(policyDTO.getDstItf());
        }else {
            sb.append(" oif any ");
        }
        if (!CommonConstants.ANY.equalsIgnoreCase(policyObjectDTO.getJoin())) {
            sb.append(" service ").append(policyObjectDTO.getJoin());
        }else {
            sb.append(" oif any ");
        }
        sb.append(CommonConstants.LINE_BREAK);
        sb.append("newconfig save\n");
        return sb.toString();
    }

    public PolicyObjectDTO generatePostAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            return dto;
        }
// 转换后源地址对象定义（地址池，不要复用）,目的nat要复用：
        if (StringUtils.isNotBlank(existsAddressName) && !createObjFlag) {
            dto.setJoin( existsAddressName);
            dto.setObjectFlag(true);
            dto.setName(existsAddressName);
            return dto;
        }
        String join = "";
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine("");
            return dto;
        }
        boolean isIpV6 = false;
        //若为IPv6地址，
        if (IpUtils.isIPv6(ipAddress)) {
            isIpV6 = true;
        }


        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        join = setName;
        String addressCmd = "";
        if (isIpV6) {

        } else {
            // 是创建对象
            AtomicInteger count = new AtomicInteger(0);

            StringBuffer addressObject = new StringBuffer();
            for (String address : arr) {
                join = setName;
                int mark = count.get();
                if (mark > 0) {
                    join = String.format("%s_%s",join,mark);
                }
                addressObject.append(join).append(" ");
                count.addAndGet(1);
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("sataddr add name %s ip %s:%s\n", join, startIp, endIp);
                    sb.append(addressCmd);

                } else if (IpUtils.isIPSegment(address)) {
                    String ipStart = IpUtils.getStartIp(address);
                    String ipEnd = IpUtils.getStartIp(address);
                    addressCmd = String.format("sataddr add name %s ip %s/%s\n", join, ipStart, ipEnd);
                    sb.append(addressCmd);

                } else {
                    sb.append("sataddr add name ").append(join)
                            .append(" ip ").append(address).append(CommonConstants.LINE_BREAK);
                }
            }
            if (arr.length > 1) {
                String groupName = String.format("%s_group_%s", ticket, IdGen.getRandomNumberString());
                sb.append(String.format("addrgrp add name %s \n", groupName));
                sb.append(String.format("addrgrp set name %s addmbr %s  \n", groupName, addressObject));
                join = groupName;
            }
        }
        sb.append("exit\n");
        dto.setCommandLine(sb.toString());
        dto.setJoin(join);
        return dto;
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        SecurityVenustechPowerV securityLeadSecPowerV = new SecurityVenustechPowerV();
        PolicyObjectDTO srcAddressObject = securityLeadSecPowerV.generateAddressObject(StringUtils.join(policyDTO.getRestSrcAddressList().toArray(),","), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getSrcAddressObjectName(),policyDTO.getExistSrcAddressList());
        PolicyObjectDTO policyObjectDTO = securityLeadSecPowerV.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.isCreateObjFlag(), policyDTO.getExistServiceNameList());
        PolicyObjectDTO postAddressObject = generatePostAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), false, policyDTO.getPostAddressObjectName());

        PolicyObjectDTO postPolicyObjectDTO = securityLeadSecPowerV.generateServiceObject(policyDTO.getRestPostServiceList(), policyDTO.isCreateObjFlag(), policyDTO.getExistPostServiceNameList());
        StringBuffer sb = new StringBuffer();

        preCommand(sb, policyDTO.isVsys(), policyDTO.getVsysName());
        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }


        if (policyObjectDTO.isObjectFlag() && StringUtils.isNotBlank(policyObjectDTO.getCommandLine())) {
            sb.append(String.format("%s\n", policyObjectDTO.getCommandLine()));
        }

        if (postPolicyObjectDTO.isObjectFlag() && StringUtils.isNotBlank(postPolicyObjectDTO.getCommandLine())) {
            sb.append(String.format("%s\n", postPolicyObjectDTO.getCommandLine()));
        }
        if(StringUtils.isNotBlank(postAddressObject.getCommandLine())){
            sb.append(postAddressObject.getCommandLine());
        }

        sb.append(String.format("rule add type portmap name \"%s\" ", policyDTO.getTheme()));
        if (!CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin())) {
            sb.append(" sa ").append(srcAddressObject.getJoin());
        }

        if (StringUtils.isNotEmpty(policyDTO.getDstIp())) {
            sb.append(" pa ");
            addressPool(policyDTO.getDstIp(), sb);
        } else {
            sb.append(CommonConstants.ANY);
        }
        sb.append(" ia ").append(postAddressObject.getJoin());

        if (StringUtils.isNotEmpty(policyDTO.getSrcItf())) {
            sb.append(" iif ").append(policyDTO.getSrcItf());
        }else {
            sb.append(" iif any ");
        }
        if (StringUtils.isNotEmpty(policyObjectDTO.getJoin())) {
            sb.append(" ps ").append(policyObjectDTO.getJoin());
        }else {
            sb.append(" ps any ");
        }
        if (StringUtils.isNotEmpty(postPolicyObjectDTO.getJoin())) {
            sb.append(" is ").append(postPolicyObjectDTO.getJoin());
        }else {
            sb.append(" is any ");
        }

        sb.append(CommonConstants.LINE_BREAK);
        sb.append("newconfig save\n");
        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    public static void main(String[] args) {
        VenustechPowerNat r004 = new VenustechPowerNat();
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
        sNatPolicyDTO.setCreateObjFlag(true);
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
        dnatPolicyDTO.setCreateObjFlag(true);
        dnatPolicyDTO.setSrcZone("trust");
        dnatPolicyDTO.setDstZone("untrust");

        dnatPolicyDTO.setSrcItf("srcItf");
        dnatPolicyDTO.setDstItf("dstItf");
        dnatPolicyDTO.setPostAddressObjectName("dst_post_object");
        dnatPolicyDTO.setTheme("w1");
        String dnat = r004.generateDNatCommandLine(dnatPolicyDTO);
        System.out.println(dnat);

        System.out.println("--------------------------------------------------------------------------");
        StaticNatTaskDTO bothNatDTO = new StaticNatTaskDTO();

//        bothNatDTO.setGlobalAddress("192.168.2.1,192.168.2.2");
        bothNatDTO.setInsideAddress("172.16.2.1,172.16.2.2");
        bothNatDTO.setCreateObject(true);

        bothNatDTO.setTheme("w1");
        String bothNat = r004.generateStaticNatCommandLine(bothNatDTO);
        System.out.println(bothNat);

    }
}
