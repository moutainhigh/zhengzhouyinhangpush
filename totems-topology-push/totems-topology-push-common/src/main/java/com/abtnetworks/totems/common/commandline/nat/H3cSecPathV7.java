package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityH3cSecPathV7;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc 华3V7 NAT策略
 * @author zhoumuhua
 * @date 2021-6-17
 */
@Slf4j
@Service(value = "H3cSecPathV7 NAT")
public class H3cSecPathV7 extends SecurityPolicyGenerator implements NatPolicyGenerator {

    public final static int MAX_OBJECT_NAME_LENGTH = 31;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate H3cSecPathV7 nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        StringBuilder sb = new StringBuilder();

        String preCommand = generatePreCommandline(policyDTO.isVsys(), policyDTO.getVsysName());
        sb.append(preCommand);

        boolean isCreateObject = policyDTO.isCreateObject();
        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getInsideAddress(), policyDTO.getTheme(), policyDTO.getInsideAddressName(), "object-group", isCreateObject, "", true);
        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getGlobalAddress(), policyDTO.getTheme(), policyDTO.getGlobalAddressName(), "object-group", isCreateObject, "", true);

        List<String> addressObjectGroupNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(null, addressObjectGroupNameList, null,
                null, srcAddressObject, dstAddressObject,null, null, null, null);
        // 华三v7都是建组 所以这个地方set组属性
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);

        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }
        String srcJoin = srcAddressObject.getJoin().substring(0, srcAddressObject.getJoin().length()-1);
        String addressCmd = String.format("nat static outbound %s %s", srcJoin, dstAddressObject.getJoin());

        sb.append(addressCmd);

        sb.append("quit\n");

        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();

        String preCommand = generatePreCommandline(policyDTO.isVsys(), policyDTO.getVsysName());
        sb.append(preCommand);

        SecurityH3cSecPathV7 h3c = new SecurityH3cSecPathV7();
        boolean isCreateObject = policyDTO.isCreateObjFlag();

        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), "source-ip", isCreateObject, policyDTO.getSrcIpSystem(), false);
        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), "destination-ip", isCreateObject, policyDTO.getDstIpSystem(), false);


        if (StringUtils.isBlank(policyDTO.getServiceObjectName())) {
            isCreateObject = true;
            policyDTO.setIpType(IpTypeEnum.IPV4.getCode());
        }
        PolicyObjectDTO serviceObject = h3c.generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName(),isCreateObject,policyDTO.getIpType());


        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostIpAddress(), policyDTO.getCurrentAddressGroupId(),policyDTO.getPostAddressObjectName(), policyDTO.getPostSrcIpSystem());

        List<String> addressObjectGroupNameList = new ArrayList<>();
        List<String> serviceObjectGroupNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(null, addressObjectGroupNameList, null,
                serviceObjectGroupNameList, srcAddressObject, dstAddressObject,natObject, null, serviceObject, null);
        // 华三v7都是建组 所以这个地方set组属性
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);
        policyDTO.setServiceObjectGroupNameList(serviceObjectGroupNameList);


        if (natObject.isObjectFlag() && StringUtils.isNotBlank(natObject.getCommandLine())) {
            sb.append(natObject.getCommandLine());
            sb.append("\n");
        }
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
            sb.append("\n");
        }

        sb.append("nat global-policy\n");
        String randomNumberString = IdGen.getRandomNumberString();
        String ruleName = String.format("rule name %s\n", policyDTO.getTheme() + "_" + randomNumberString);
        sb.append(ruleName);
        policyDTO.setPolicyName(policyDTO.getTheme() + "_" + randomNumberString);
        String rulePath = String.format("action snat address-group %s", StringUtils.isEmpty(natObject.getName()) ? "\n" : natObject.getName());
        sb.append(rulePath);

        if (StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            String srcZone = String.format("source-zone %s\n", policyDTO.getSrcZone());
            sb.append(srcZone);
        }

        if (StringUtils.isNotBlank(policyDTO.getDstZone())) {
            String dstZone = String.format("destination-zone %s\n", policyDTO.getDstZone());
            sb.append(dstZone);
        }


        sb.append(srcAddressObject.getJoin());
        sb.append(dstAddressObject.getJoin());
        if(serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        }

        sb.append("quit\n");
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();

        String preCommand = generatePreCommandline(policyDTO.isVsys(), policyDTO.getVsysName());
        sb.append(preCommand);

        SecurityH3cSecPathV7 h3c = new SecurityH3cSecPathV7();
        boolean isCreateObject = policyDTO.isCreateObjFlag();


        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), "source-ip", isCreateObject, policyDTO.getSrcIpSystem(), false);
        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), "destination-ip", isCreateObject, policyDTO.getDstIpSystem(), false);


        if (StringUtils.isBlank(policyDTO.getServiceObjectName())) {
            isCreateObject = true;
            policyDTO.setIpType(IpTypeEnum.IPV4.getCode());
        }

        PolicyObjectDTO serviceObject = h3c.generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName(),isCreateObject,policyDTO.getIpType());

        List<String> addressObjectGroupNameList = new ArrayList<>();
        List<String> serviceObjectGroupNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(null, addressObjectGroupNameList, null,
                serviceObjectGroupNameList, srcAddressObject, dstAddressObject,null, null, serviceObject, null);
        // 华三v7都是建组 所以这个地方set组属性
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);
        policyDTO.setServiceObjectGroupNameList(serviceObjectGroupNameList);


        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
            sb.append("\n");
        }

        String postDstIp = policyDTO.getPostIpAddress();
        String[] dstSplit = postDstIp.split(PolicyConstants.ADDRESS_SEPERATOR);

        sb.append("nat global-policy\n");
        String randomNumberString = IdGen.getRandomNumberString();
        String ruleName = String.format("rule name %s\n", policyDTO.getTheme() + "_" + randomNumberString);
        sb.append(ruleName);
        policyDTO.setPolicyName(policyDTO.getTheme() + "_" + randomNumberString);
        if (StringUtils.isNotBlank(policyDTO.getPostPort()) && !PolicyConstants.POLICY_STR_VALUE_ANY.equals(policyDTO.getPostPort())) {
            String[] splitPort = policyDTO.getPostPort().split(PolicyConstants.ADDRESS_SEPERATOR);
            String rulePathPort = String.format("action dnat ip-address %s local-port %s\n", dstSplit[0], splitPort[0]);
            sb.append(rulePathPort);
        } else {
            String rulePath = String.format("action dnat ip-address %s\n", dstSplit[0]);
            sb.append(rulePath);
        }

        if (StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            String srcZone = String.format("source-zone %s\n", policyDTO.getSrcZone());
            sb.append(srcZone);
        }

        // 2022.01.13 华三V7目的nat不支持目的域输入，已注释  updated by liuchanghao
        /*if (StringUtils.isNotBlank(policyDTO.getDstZone())) {
            String dstZone = String.format("destination-zone %s\n", policyDTO.getDstZone());
            sb.append(dstZone);
        }*/


        sb.append(srcAddressObject.getJoin());
        sb.append(dstAddressObject.getJoin());
        if(serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        }

        sb.append("quit\n");
        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();

        String preCommand = generatePreCommandline(policyDTO.isVsys(), policyDTO.getVsysName());
        sb.append(preCommand);

        SecurityH3cSecPathV7 h3c = new SecurityH3cSecPathV7();
        boolean isCreateObject = policyDTO.isCreateObjFlag();

        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), "source-ip", isCreateObject, "", false);
        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), "destination-ip", isCreateObject, "", false);


        if (StringUtils.isBlank(policyDTO.getServiceObjectName())) {
            isCreateObject = true;
            policyDTO.setIpType(IpTypeEnum.IPV4.getCode());
        }
        PolicyObjectDTO serviceObject = h3c.generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName(),isCreateObject,policyDTO.getIpType());

        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostSrcIp(), policyDTO.getCurrentAddressGroupId(),policyDTO.getPostSrcAddressObjectName(), "");

        List<String> addressObjectGroupNameList = new ArrayList<>();
        List<String> serviceObjectGroupNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(null, addressObjectGroupNameList, null,
                serviceObjectGroupNameList, srcAddressObject, dstAddressObject,natObject, null, serviceObject, null);
        // 华三v7都是建组 所以这个地方set组属性
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);
        policyDTO.setServiceObjectGroupNameList(serviceObjectGroupNameList);


        if (natObject.isObjectFlag() && StringUtils.isNotBlank(natObject.getCommandLine())) {
            sb.append(natObject.getCommandLine());
            sb.append("\n");

        }
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
            sb.append("\n");
        }

        String postDstIp = policyDTO.getPostDstIp();
        String[] dstSplit = postDstIp.split(PolicyConstants.ADDRESS_SEPERATOR);
        sb.append("nat global-policy\n");
        String srcRuleName = String.format("rule name %s\n", policyDTO.getTheme());
        sb.append(srcRuleName);
        policyDTO.setPolicyName(srcRuleName);
        String srcRulePath = String.format("action snat address-group %s", StringUtils.isEmpty(natObject.getName()) ? "\n" : natObject.getName());
        sb.append(srcRulePath);


        if (StringUtils.isNotBlank(policyDTO.getPostPort()) && !PolicyConstants.POLICY_STR_VALUE_ANY.equals(policyDTO.getPostPort())) {
            String rulePathPort = String.format("action dnat ip-address %s local-port %s\n", dstSplit[0], policyDTO.getPostPort());
            sb.append(rulePathPort);
        } else {
            String dstRulePath = String.format("action dnat ip-address %s\n", dstSplit[0]);
            sb.append(dstRulePath);
        }

        if (StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            String srcZone = String.format("source-zone %s\n", policyDTO.getSrcZone());
            sb.append(srcZone);
        }

        if (StringUtils.isNotBlank(policyDTO.getDstZone())) {
            String dstZone = String.format("destination-zone %s\n", policyDTO.getDstZone());
            sb.append(dstZone);
        }


        sb.append(srcAddressObject.getJoin());
        sb.append(dstAddressObject.getJoin());
        if(serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        }

        sb.append("quit\n");
        return sb.toString();
    }


    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return null;
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        return null;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return null;
    }

    /**
     * 获取地址对象
     * @param ipAddress ip地址
     * @return 地址对象
     */
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String prefix,
                                                 boolean isCreateObject, String ipSystem, Boolean isStatic) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(prefix + " " + existsAddressName + "\n");
            return dto;
        }

        //若为IPv6地址，则必须创建对象
        isCreateObject = true;

        String join = "";
        String command = "";

        if(AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine(command);
            return dto;
        }
        List<String>  createGroupObjectName = new ArrayList<>();

        if(isCreateObject) {
            String name;
            if (isStatic) {
                //如果是静态nat,对象名为地址本身
                name = strSub(ipAddress, getMaxObejctNameLength(),"GB2312");
            } else {
                if(StringUtils.isNotEmpty(ipSystem)){
                    name = ipSystem;
                    // 对象名称长度限制，一个中文2个字符
                    name = strSub(name, getMaxObejctNameLength(),"GB2312");
                    // 对象名称长度限制
                    int len = 0;
                    try{
                        len = name.getBytes("GB2312").length;
                    }catch (Exception e) {
                        log.error("字符串长度计算异常");
                    }
                    if(len > getMaxObejctNameLength() -7 ) {
                        name = strSub(name, getMaxObejctNameLength() -7, "GB2312");
                    }
                    name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                } else {
                    name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
                }
            }
            name = containsQuotes(name);
            StringBuilder sb = new StringBuilder();
            sb.append("object-group ip address ");
            sb.append(name + "\n");
            createGroupObjectName.add(name);
            join = prefix + " " + name + "\n";

            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            int index = 0;
            boolean isIpv6 = false;
            for(String address : arr) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s network range %s %s\n", index, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s network subnet %s %s\n", index, ip, maskBit);
                } else if (IpUtils.isIP(address)){
                    addressCmd = String.format("%s network host address %s\n", index, address);
                } else if (address.contains(":")) {
                    isIpv6 = true;
                    //ipv6
                    if (address.contains("/")) {
                        String[] addrArray = address.split("/");
                        if (StringUtils.isNotEmpty(addrArray[0])) {
                            addressCmd = String.format("%s network subnet %s %s\n", index, addrArray[0].toLowerCase(), addrArray[1]);
                        }
                    } else if (address.contains("-")) {
                        String[] addrArray = address.split("-");
                        if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                            addressCmd = String.format("%s network range %s %s\n", index, addrArray[0].toLowerCase(), addrArray[1].toLowerCase());
                        }
                    } else {
                        addressCmd = String.format("%s network host address %s\n", index,  address.toLowerCase());
                    }
                }
                index++;
                sb.append(addressCmd);
            }

            sb.append("quit\n");

            command = sb.toString();
            if (isIpv6) {
                //ipv6时
                command = command.replace("object-group ip", "object-group ipv6");
            }
        } else {
            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            StringBuilder sb = new StringBuilder();
            for(String address : arr) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format( "%s range %s %s\n", prefix, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s subnet %s %s\n", prefix, ip, maskBit);
                } else if (IpUtils.isIP(address)){
                    addressCmd = String.format("%s host %s\n", prefix, address);
                }
                sb.append(addressCmd);
            }
            join = sb.toString();
        }

        dto.setJoin(join);
        dto.setCommandLine(command);
        dto.setObjectFlag(true);
        dto.setCreateGroupObjectName(createGroupObjectName);
        return dto;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }

    /**
     * 创建Nat地址组对象
     * @param addressString
     * @param addressGroupId
     * @param existAddressName
     * @return
     */
    public PolicyObjectDTO generateNatObject(String addressString, String addressGroupId, String existAddressName, String postSrcIpSystem) {
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(StringUtils.isNotBlank(existAddressName)){
            if(StringUtils.isNotEmpty(existAddressName) && existAddressName.contains(" ")){
                existAddressName = "\""+existAddressName+"\"";
            }
            dto.setName(existAddressName + "\n");
            dto.setJoin(existAddressName + "\n");
            dto.setObjectFlag(true);
            return dto;
        }

        String addressCmd = "";

        if(AliStringUtils.isEmpty(addressString)) {
            dto.setCommandLine(addressCmd);
            return dto;
        }
        List<String>  createGroupObjectNames = new ArrayList<>();
        if (StringUtils.isNotBlank(postSrcIpSystem)) {
            String groupName = String.format("nat address-group %s name %s\n", addressGroupId, postSrcIpSystem);
            sb.append(groupName);
            createGroupObjectNames.add(postSrcIpSystem);
        } else {
            sb.append("nat address-group ").append(addressGroupId).append("\n");
        }
        String[] ipArr = addressString.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String ipAddr : ipArr) {
            if(IpUtils.isIPRange(ipAddr)) {
                String startIp = IpUtils.getStartIpFromRange(ipAddr);
                String endIp = IpUtils.getEndIpFromRange(ipAddr);
                addressCmd = String.format("address %s %s\n", startIp, endIp);
            } else if (IpUtils.isIPSegment(ipAddr)) {
                String startIp = IpUtils.getStartIp(ipAddr);
                String endIp = IpUtils.getEndIp(ipAddr);
                addressCmd = String.format("address %s %s\n", startIp, endIp);
            } else if (IpUtils.isIP(ipAddr)){
                addressCmd = String.format("address %s %s\n", ipAddr, ipAddr);
            } else {
                addressCmd = "";
            }
            sb.append(addressCmd);
        }

        sb.append("quit\n");

        dto.setName(addressGroupId + "\n");
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        dto.setCreateGroupObjectName(createGroupObjectNames);
        return dto;
    }

    String generatePreCommandline(Boolean isVsys, String vsysName) {
        StringBuilder sb = new StringBuilder();
        if (isVsys) {
            sb.append("system-view\n");
            sb.append("switchto context " + vsysName + "\n");
        }
        sb.append("system-view\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        H3cSecPathV7 h3c = new H3cSecPathV7();

        System.out.println("--------------------------源nat生成命令行开始------------------------------------------");
        //源nat
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        sNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        sNatPolicyDTO.setSrcIpSystem("src51");
        sNatPolicyDTO.setDstIpSystem("dst61");

        sNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        //sNatPolicyDTO.setPostSrcIpSystem("post71");
        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setTheme("w1");

        sNatPolicyDTO.setCurrentAddressGroupId("10");
        //sNatPolicyDTO.setSrcAddressObjectName("srcadddress");
        //sNatPolicyDTO.setDstAddressObjectName("dstadddress");
        //sNatPolicyDTO.setServiceObjectName("serviceadddress");
        //sNatPolicyDTO.setPostAddressObjectName("zhangsan");
        sNatPolicyDTO.setVsys(true);
        sNatPolicyDTO.setVsysName("xuqiang");

        String snat = h3c.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------源nat生成命令行结束------------------------------------------");

        System.out.println("--------------------------目的nat生成命令行开始------------------------------------------");
        //源nat
        DNatPolicyDTO dNatPolicyDTO = new DNatPolicyDTO();
        dNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        dNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");

        dNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        dNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        dNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        dNatPolicyDTO.setTheme("w2");
        dNatPolicyDTO.setCurrentAddressGroupId("10");
        dNatPolicyDTO.setPostPort("9090,9091,9092");


        String dnat = h3c.generateDNatCommandLine(dNatPolicyDTO);
        System.out.println(dnat);
        System.out.println("--------------------------目的nat生成命令行结束------------------------------------------");


        System.out.println("--------------------------both nat生成命令行开始------------------------------------------");
        //源nat
        NatPolicyDTO natPolicyDTO = new NatPolicyDTO();
        natPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        natPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");

        natPolicyDTO.setPostSrcIp("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        natPolicyDTO.setPostDstIp("192.168.5.1,192.168.6.1");
        natPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        natPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        natPolicyDTO.setTheme("w3");
        natPolicyDTO.setCurrentAddressGroupId("10");
        natPolicyDTO.setPostPort("9090");


        String bothnat = h3c.generateBothNatCommandLine(natPolicyDTO);
        System.out.println(bothnat);
        System.out.println("--------------------------both nat生成命令行结束------------------------------------------");

        System.out.println("--------------------------静态nat生成命令行开始------------------------------------------");
        //源nat
        StaticNatTaskDTO staticNatPolicyDTO = new StaticNatTaskDTO();
        staticNatPolicyDTO.setGlobalAddress("192.168.2.1,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2");
        staticNatPolicyDTO.setInsideAddress("172.16.2.1,172.16.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2,192.168.2.2");

        staticNatPolicyDTO.setTheme("w4");


        String staticNat = h3c.generateStaticNatCommandLine(staticNatPolicyDTO);
        System.out.println(staticNat);
        System.out.println("--------------------------静态nat生成命令行结束------------------------------------------");
    }
}
