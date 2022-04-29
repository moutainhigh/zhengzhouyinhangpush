package com.abtnetworks.totems.vender.leadSecPowerV.nat;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.vender.leadSecPowerV.security.SecurityLeadSecPowerV30Impl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class NatLeadSecPowerV30Impl extends SecurityLeadSecPowerV30Impl {

    private static final String CONFIG_SAVE = "newconfig save\n";


    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action,
                                                     String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                     String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress,
                                                     ServiceParamDTO[] insideServiceParam, ServiceParamDTO[] globalServiceParam, ZoneParamDTO srcZone,
                                                     ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                     String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup,
                                                     String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup,
                                                     Map<String, Object> map, String[] args) throws Exception {
        StringBuilder securityPolicyCl = new StringBuilder();
        //外网地址地址不建对象，只支持单ip
        String gloGroupName = "\"any\"";
        String[] dstIpSingleIpArray = globalAddress.getSingleIpArray();
        if (!ArrayUtils.isEmpty(dstIpSingleIpArray)) {
            gloGroupName = dstIpSingleIpArray[0];
        }
        //处理内网地址
        String serverGroupName = disposerServerAddr(insideAddress,insideRefIpAddressObjectGroup,map,args,securityPolicyCl);
        String intfName = "any";
        if (!StringUtils.isBlank(inInterface.getName())) {
            intfName = inInterface.getName();
        }
        String otfName = "any";
        if (!StringUtils.isBlank(outInterface.getName())) {
            otfName = outInterface.getName();
        }
        //添加类型为 IP  映射的安全规则：（不支持设置服务）
        //rule add type ipmap name ""主题"" pa 外网地址（不要建对象） ia 内网地址对象 [izone
        //源域] [ozone目的域]
        //rule add type ipmap name “rule6”  pa “192.168.1.1” ia “http_server” izone “any”ozone “any”
        securityPolicyCl.append("rule add type ipmap name \"").append(name).append("\"")
                .append(" pa ").append(gloGroupName)
                .append(" ia ").append(serverGroupName)
                .append(" izone \"").append(intfName).append("\"")
                .append(" ozone \"").append(otfName).append("\"")
                .append(StringUtils.LF);
        securityPolicyCl.append(CONFIG_SAVE);
        return securityPolicyCl.toString();
    }

    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId,
                                                IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam,
                                                IpAddressParamDTO postSrcIpAddress, ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject,
                                                String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                String[] refServiceObject, String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject,
                                                String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {

        StringBuilder securityPolicyCl = new StringBuilder();
        //处理地址对象
        String srcGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, args, securityPolicyCl);
        String dstGroupName = disposeAddress(statusTypeEnum, dstIp, dstRefIpAddressObject, dstRefIpAddressObjectGroup, map, args, securityPolicyCl);
        //处理转换后源地址对象(地址池不复用)
        String postGroupName = disposeSatAddr(postSrcIpAddress, null, null, null, securityPolicyCl);
        //处理服务对象
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam,refServiceObject, refServiceObjectGroup, map, args, securityPolicyCl);
        String otfName = "any";
        if (!StringUtils.isBlank(outInterface.getName())) {
            otfName = outInterface.getName();
        }
        //rule add type nat name "主题" [sa 源地址对象] sat 地址池 [da 目的地址对象] [oif 目的接口（不是目的域）] [service 转换前服务对象]"
        //rule add type nat name “rule4” sa “any” sat “sat1” da “a1” ozone “any”service “http”
        securityPolicyCl.append("rule add type nat name \"").append(name).append("\"")
                .append(" sa ").append(srcGroupName)
                .append(" sat ").append(postGroupName)
                .append(" da ").append(dstGroupName)
                .append(" ozone \"").append(otfName).append("\"")
                .append(" service ").append(serviceGroupName)
                .append(StringUtils.LF);
        securityPolicyCl.append(CONFIG_SAVE);
        return securityPolicyCl.toString();
    }


    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId,
                                                IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam,
                                                IpAddressParamDTO postDstIpAddress, ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone,
                                                ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject,
                                                String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup,
                                                String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map,
                                                String[] args) throws Exception {
        StringBuilder securityPolicyCl = new StringBuilder();
        //处理地址对象
        String srcGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, args, securityPolicyCl);
        //目的地址不建对象，只支持单ip
        String dstGroupName = "\"any\"";
        String[] dstIpSingleIpArray = dstIp.getSingleIpArray();
        if (!ArrayUtils.isEmpty(dstIpSingleIpArray)) {
            dstGroupName = dstIpSingleIpArray[0];
        }
        //处理转换后地址（服务器地址）
        String serverGroupName = disposerServerAddr(postDstIpAddress, postDstRefIpAddressObjectGroup, map, args, securityPolicyCl);
        //处理服务对象
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam, refServiceObject,refServiceObjectGroup, map, args, securityPolicyCl);
        String postServiceGroupName = disposeService(statusTypeEnum, postServiceParam, null,null, map, args, securityPolicyCl);

        //rule add type portmap name "主题" [sa 源地址对象] pa 目的地址（不要建对象，只支持单IP） ia 转换后地址对象 [izone源域] [ozone目的域] ps 转换前服务对象 is 转换后端口服务对象
        //rule add type portmap name “rule5” sa “any” pa 192.168.1.1 ia “http_server” izone “any”ozone “any”ps “http”is “http”
        String intfName = "any";
        if (!StringUtils.isBlank(inInterface.getName())) {
            intfName = inInterface.getName();
        }
        String otfName = "any";
        if (!StringUtils.isBlank(outInterface.getName())) {
            otfName = outInterface.getName();
        }
        securityPolicyCl.append("rule add type portmap name \"").append(name).append("\"")
                .append(" sa ").append(srcGroupName)
                .append(" pa ").append(dstGroupName)
                .append(" ia ").append(serverGroupName)
                .append(" izone \"").append(intfName).append("\"")
                .append(" ozone \"").append(otfName).append(" \"")
                .append(" ps ").append(serviceGroupName)
                .append(" is ").append(postServiceGroupName)
                .append(StringUtils.LF);
        securityPolicyCl.append(CONFIG_SAVE);
        return securityPolicyCl.toString();
    }

    private String disposerServerAddr(IpAddressParamDTO postDstIpAddress, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder securityPolicyCl) {
        String serverGroupName = "any";
        //serveraddr add name sa1 ip 192.168.0.1 ip 192.168.0.2
        if (!ArrayUtils.isEmpty(postDstRefIpAddressObjectGroup)) {
            serverGroupName = String.format("\"%s\"", postDstRefIpAddressObjectGroup[0]);
        } else if (ObjectUtils.isNotEmpty(postDstIpAddress) && ObjectUtils.isNotEmpty(postDstIpAddress.getSingleIpArray())) {
            if(postDstIpAddress.getSingleIpArray().length==1){
               serverGroupName = postDstIpAddress.getSingleIpArray()[0];
            }else {
                serverGroupName = this.createIpAddressObjectGroupName(postDstIpAddress.getSingleIpArray(), postDstIpAddress.getRangIpArray(), postDstIpAddress.getSubnetIntIpArray(), postDstIpAddress.getSubnetStrIpArray(),
                        null, postDstIpAddress.getHosts(), postDstRefIpAddressObjectGroup, null, map, args);
            }
            securityPolicyCl.append("serveraddr add name ").append(serverGroupName);
            for (String serverIp : postDstIpAddress.getSingleIpArray()) {
                securityPolicyCl.append(" ip ").append(serverIp);
            }
            securityPolicyCl.append(StringUtils.LF).append(StringUtils.LF);
            serverGroupName = String.format("\"%s\"", serverGroupName);
        }
        return serverGroupName;
    }

    private String disposeSatAddr(IpAddressParamDTO postDstIpAddress, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder securityPolicyCl) {
        String serverGroupName = "\"any\"";
        //serveraddr add name sa1 ip 192.168.0.1 ip 192.168.0.2
        if (!ArrayUtils.isEmpty(postDstRefIpAddressObjectGroup)) {
            serverGroupName = String.format("\"%s\"", postDstRefIpAddressObjectGroup[0]);
        } else if (ObjectUtils.isNotEmpty(postDstIpAddress) && ObjectUtils.isNotEmpty(postDstIpAddress.getRangIpArray())) {
            if(postDstIpAddress.getRangIpArray().length==1){
                serverGroupName = postDstIpAddress.getRangIpArray()[0].getStart()+"_"+postDstIpAddress.getRangIpArray()[0].getEnd();
            }else {
                serverGroupName = this.createIpAddressObjectGroupName(postDstIpAddress.getSingleIpArray(), postDstIpAddress.getRangIpArray(), postDstIpAddress.getSubnetIntIpArray(), postDstIpAddress.getSubnetStrIpArray(),
                        null, postDstIpAddress.getHosts(), postDstRefIpAddressObjectGroup, null, map, args);
            }
            securityPolicyCl.append("sataddr add name ").append(serverGroupName);
            for (IpAddressRangeDTO rangeDTO : postDstIpAddress.getRangIpArray()) {
                securityPolicyCl.append(" ip ").append(rangeDTO.getStart()).append(":").append(rangeDTO.getEnd());
            }
            securityPolicyCl.append(StringUtils.LF).append(StringUtils.LF);
            serverGroupName = String.format("\"%s\"", serverGroupName);
        }
        return serverGroupName;
    }


}
