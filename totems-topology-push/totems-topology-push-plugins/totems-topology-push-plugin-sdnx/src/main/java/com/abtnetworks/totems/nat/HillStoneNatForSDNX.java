package com.abtnetworks.totems.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.Hillstone;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author liuchanghao
 * @desc
 * @date 2020-11-19 15:13
 */
@Slf4j
@Service
public class HillStoneNatForSDNX implements NatPolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(HillStoneNatForSDNX.class);

    private final int DAY_SECOND = 24 * 60 * 60;

    private static Set<Integer> allowType = new HashSet<>();

    private final int MAX_NAME_LENGTH = 95;


    public HillStoneNatForSDNX() {
        init();
    }

    private static void init() {
        allowType.add(3);
        allowType.add(4);
        allowType.add(5);
        allowType.add(8);
        allowType.add(11);
        allowType.add(12);
        allowType.add(13);
        allowType.add(15);
    }

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
        sb.append(String.format("snatrule from %s to %s%s%s trans-to %s mode dynamicport log\n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()),  postAddressObjectName));

        sb.append("exit\nend\n");
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostDstIpSystem());
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

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",null);
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",null);
        PolicyObjectDTO postSrcAddressObject = getAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), true, policyDTO.getPostSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "",null);
        PolicyObjectDTO postDstAddressObject = getAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), true,policyDTO.getPostDstAddressObjectName(), policyDTO.isCreateObjFlag(), "",null);
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

            PolicyObjectDTO dto = null;

            if (isSNatPostAddress) {
                dto = generateAddressObjectForNat(ipAddressString, theme, prefix, true, addressObjectName,ipSystem);
                policyObject.setName(" " + dto.getName());
            } else {
                dto = generateAddressObject(ipAddressString, theme, prefix, true, addressObjectName,ipSystem,0);
                policyObject.setName(" " + dto.getName());
            }
            policyObject.setCommandLine(dto.getCommandLine());

        } else {
            //若为单个ip范围，则建对象
            if(IpUtils.isIPRange(ipAddressString)) {
                PolicyObjectDTO dto = generateAddressObject(ipAddressString, theme, "", true, addressObjectName,ipSystem,0);

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

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem, Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if(AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                dto.setJoin(ipPrefix + "-addr any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            } else {
                dto.setJoin(ipPrefix + "-addr IPv6-any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            }
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (String address : arr) {
            // 是创建对象
            if (createObjFlag) {
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket,ipSystem, arr.length, ipType, index);
                sb.append("exit\n");
                dto.setCommandLine(sb.toString());
            } else {
                //直接显示内容
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket,ipSystem, arr.length, ipType, index);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }
            index ++;
        }
        return dto;
    }



    public PolicyObjectDTO generateAddressObjectForNat(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName,String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if(AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setJoin(ipPrefix + "-addr any\n");
            dto.setName("any");
            dto.setObjectFlag(true);
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();

        String name;
        if(StringUtils.isNotEmpty(ipSystem)){
            name = ipSystem;
        } else {
            name = String.format("%s_AO_%s",ticket, IdGen.getRandomNumberString());
        }

        sb.append(String.format("address %s\n", name));
        if(arr.length>1){

            for (String address : arr) {
                String fullStr = "";
                if (IpUtils.isIPSegment(address)) {
                    fullStr = String.format("ip %s\n", address);
                } else if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s/32\n", address);
                }
                sb.append(fullStr);
            }

            sb.append("exit\n");
            dto.setName(name);
            dto.setCommandLine(sb.toString());
        }else{
            if(arr.length == 1){
                //直接显示内容
                formatFullAddress(arr[0], sb, ipPrefix, false, dto, ticket,ipSystem, arr.length, 0, 0);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }

        }
        return dto;
    }
    private void formatFullAddress(String address, StringBuilder sb, String ipPrefix, boolean createObjFlag, PolicyObjectDTO dto, String ticket,String ipSystem,int length, Integer ipType,
                                   int index) {
        String name;
        if(StringUtils.isNotEmpty(ipSystem)){
            name = ipSystem;
        } else {
            name = String.format("%s_AO_%s",ticket, IdGen.getRandomNumberString());
        }

        String fullStr = "";
        if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/32\n", address);
            }

            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        } else if(ipType.intValue() == IpTypeEnum.IPV6.getCode()){
            // ipv6
            if (address.contains("/")) {
                fullStr = String.format("ip %s\n", address);
            } else if(address.contains("-")){
                // 范围
                String startIp = IpUtils.getRangeStartIPv6(address);
                String endIp = IpUtils.getRangeEndIPv6(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/128\n", address);
            }
            name = String.format("\"%s\"", name);
            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        } else {
            // 目的地址是URL类型
            // ipv4
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else if(IpUtils.isIPv6(address)){
                // ipv6
                if (address.contains("/")) {
                    fullStr = String.format("ip %s\n", address);
                } else if(address.contains("-")){
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s/128\n", address);
                }
            } else if(IpUtils.isIP(address)){
                fullStr = String.format("ip %s/32\n", address);
            }else {
                // 域名
                fullStr = String.format("host %s\n", address);
            }
            name = String.format("\"%s\"", name);
            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        }

        if (createObjFlag) {
            sb.append(fullStr);
        } else {
            sb.append(ipPrefix + "-" + fullStr);
        }
    }

    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            logger.error("字符串长度计算异常");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }


    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, boolean isCreateObject) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(" service any");
        policyObjectDTO.setCommandLine("");

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
                PolicyObjectDTO dto = generateServiceObject(serviceList, null, serviceObjectName);
                String commandline = dto.getCommandLine();
                String name = " " + dto.getJoin();
                policyObjectDTO.setName(name.replace("\n", ""));
                policyObjectDTO.setCommandLine(commandline);
            }
        } else {
            PolicyObjectDTO dto = generateServiceObject(serviceList, null, serviceObjectName);
            String commandline = dto.getCommandLine();
            String name = " " + dto.getJoin();
            policyObjectDTO.setName(name.replace("\n", ""));
            policyObjectDTO.setCommandLine(commandline);
        }

        return policyObjectDTO;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, Integer idleTimeout, String existsServiceName) {
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

        dto.setObjectFlag(true);


        StringBuilder objNameSb = new StringBuilder();

        //多个，建对象
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

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

                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                for (String dstPort : dstPorts) {
                    sb.append(String.format("%s dst-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    if(idleTimeout != null) {
                        int day = idleTimeout / DAY_SECOND;
                        if((idleTimeout % DAY_SECOND) > 0) {
                            day = day + 1;
                        }
                        sb.append(String.format(" timeout-day %d\n", day));
                    }
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
        if( sb.toString().length() > 0) {
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

    //协议没有端口时，衔接
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

    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        Hillstone r004 = new Hillstone();
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
