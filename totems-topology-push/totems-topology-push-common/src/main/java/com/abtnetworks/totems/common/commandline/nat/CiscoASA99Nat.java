package com.abtnetworks.totems.common.commandline.nat;


import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.ExistObjectRefDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc    思科nat命令行生成
 * @author liuchanghao
 * @date 2020-11-24 15:13
 */

@Slf4j
@Service(value = "Cisco ASA 9.9 NAT")
public class CiscoASA99Nat implements NatPolicyGenerator {

    public final int MAX_NAME_LENGTH = 65;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate cisco nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure terminal\n");

        if(policyDTO.getInsidePort().equals("any") && StringUtils.isNotEmpty(policyDTO.getExistInsidePort())){
            policyDTO.setExistInsidePort(null);
        }
        if(policyDTO.getGlobalPort().equals("any") && StringUtils.isNotEmpty(policyDTO.getExistGlobaPort())){
            policyDTO.setExistGlobaPort(null);
        }





        PolicyObjectDTO globalObject = getAddressObjectGroup(policyDTO.getGlobalAddress(), policyDTO.getTheme(), policyDTO.getGlobalAddressName(),null, true);

        if(!AliStringUtils.isEmpty(globalObject.getCommandLine())) {
            sb.append(globalObject.getCommandLine());
        }

        PolicyObjectDTO insideAddressObject = getAddressObjectGroup(policyDTO.getInsideAddress(), policyDTO.getTheme(), policyDTO.getInsideAddressName(),null, true);
        if(!AliStringUtils.isEmpty(insideAddressObject.getCommandLine())) {
            sb.append(insideAddressObject.getCommandLine());
        }

        List<String> addressObjectNameList = new ArrayList<>();
        List<String> addressObjectGroupNameList = new ArrayList<>();
        List<String> serviceObjectNameList = new ArrayList<>();
        List<String> serviceObjectGroupNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(addressObjectNameList, addressObjectGroupNameList, serviceObjectNameList,
            serviceObjectGroupNameList, globalObject, insideAddressObject,null, null, null, null);

        policyDTO.setAddressObjectNameList(addressObjectNameList);
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);


        String protocolString = PolicyConstants.POLICY_STR_VALUE_ANY;
        String serviceCmd = "";
        List<String> createServiceNames = new ArrayList<>();
        if(!AliStringUtils.isEmpty(policyDTO.getProtocol())) {
            protocolString = ProtocolUtils.getProtocolByString(policyDTO.getProtocol());

            String portString  = "";

            if(!AliStringUtils.isEmpty(policyDTO.getExistGlobaPort())){
                String serviceName = policyDTO.getExistGlobaPort();
                if(StringUtils.equals(policyDTO.getInsidePort(),"any")){
                    serviceCmd = "service " + serviceName + " " + serviceName;
                }else{
                    serviceCmd =  "service " + serviceName + " ";
                }

            }else if(!StringUtils.equals(policyDTO.getGlobalPort(),"any")) {
                    //多个端口/端口范围只取第一个
                StringBuilder serviceSb = new StringBuilder();
                String port = policyDTO.getGlobalPort().split(",")[0];
                if(PortUtils.isPortRange(port)) {
                    String start = PortUtils.getStartPort(port);
                    String end  = PortUtils.getEndPort(port);
                    portString = String.format("%s-%s", start, end);
                    serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination range ").append(start).append(" ").append(end).append("\n");
                } else {
                    if(!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        portString = String.format("%s", port);
                        serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination eq ").append(port).append("\n");
                    }
                }
                String serviceName = protocolString.toLowerCase()+"_" + portString + " ";
                sb.append("object service ").append(serviceName).append("\n");
                sb.append(serviceSb).append("exit\n");
                createServiceNames.add(serviceName);
                if(StringUtils.equals(policyDTO.getInsidePort(), PolicyConstants.POLICY_STR_VALUE_ANY)){
                    serviceCmd  += "service "+ serviceName + serviceName;
                } else {
                    serviceCmd  += "service "+ serviceName;
                }

            }

            if(!AliStringUtils.isEmpty(policyDTO.getExistInsidePort())){
                String serviceName = policyDTO.getExistInsidePort();
                if(StringUtils.isNotEmpty(serviceCmd)){
                    serviceCmd += serviceName;
                } else {
                    serviceCmd  += "service any "+ serviceName;
                }
            }else if(!policyDTO.getInsidePort().equals("any")) {
                //多个端口/端口范围只取第一个
                StringBuilder serviceSb = new StringBuilder();
                String port = policyDTO.getInsidePort().split(",")[0];
                if(PortUtils.isPortRange(port)) {
                    String start = PortUtils.getStartPort(port);
                    String end  = PortUtils.getEndPort(port);
                    portString = String.format("%s-%s", start, end);
                    serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination range ").append(start).append(" ").append(end).append("\n");
                } else {
                    if(!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        portString = String.format("%s", port);
                        serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination eq ").append(port).append("\n");
                    }
                }
                String serviceName = protocolString.toLowerCase()+"_" + portString;
                if(StringUtils.isNotEmpty(serviceCmd)){
                    serviceCmd += serviceName;
                } else {
                    serviceCmd  += "service any "+ serviceName;
                }
                sb.append("object service ").append(serviceName).append("\n");
                sb.append(serviceSb).append("exit\n");

                createServiceNames.add(serviceName);
            }

            if((!StringUtils.equals(protocolString,"any")) && StringUtils.isEmpty(serviceCmd)){
                serviceCmd += "service " + protocolString.toLowerCase() + "_all" + " " + protocolString.toLowerCase() + "_all";
                sb.append("object service ").append(protocolString.toLowerCase() + "_all").append("\n");
                sb.append("service "+protocolString.toLowerCase()+"\nexit\n");

                createServiceNames.add(protocolString.toLowerCase() + "_all");
            }


        }

            policyDTO.setServiceObjectNameList(createServiceNames);

            StringBuilder rollbacksb = new StringBuilder();
            rollbacksb.append("configure terminal\n");
            rollbacksb.append(String.format("no nat (%s,%s) source %s %s %s %s\n",
                    StringUtils.isBlank(policyDTO.getFromZone()) ? StringUtils.isBlank(policyDTO.getInDevItf()) ? "any" : policyDTO.getInDevItf() : policyDTO.getFromZone(),
                    StringUtils.isBlank(policyDTO.getToZone()) ? StringUtils.isBlank(policyDTO.getOutDevItf()) ? "any" : policyDTO.getOutDevItf() : policyDTO.getToZone(),
                    "static",
                    globalObject.getName(),
                    insideAddressObject.getName(),
                    protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "" : serviceCmd));
            rollbacksb.append("end\nwrite\n");
            policyDTO.setRollbackCommandLine(rollbacksb.toString());

            sb.append(String.format("nat (%s,%s) source %s %s %s %s\n",
                StringUtils.isBlank(policyDTO.getFromZone()) ? StringUtils.isBlank(policyDTO.getInDevItf()) ? "any" : policyDTO.getInDevItf()  : policyDTO.getFromZone(),
                StringUtils.isBlank(policyDTO.getToZone()) ? StringUtils.isBlank(policyDTO.getOutDevItf()) ? "any" : policyDTO.getOutDevItf() : policyDTO.getToZone(),
                "static",
                globalObject.getName(),
                insideAddressObject.getName(),
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "" : serviceCmd));

            sb.append("\nend\nwrite\n");
            sb.append("\n");
            return sb.toString();

    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    private String getAddressObjectForObjectGroup(String ipAddress, String name){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("object network %s\n", name));
        if(IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            sb.append(String.format("range %s %s\n", start, end));
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            sb.append(String.format("subnet %s %s\n", ip, mask));
        } else {
            sb.append(String.format("host %s\n", ipAddress));
        }

        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {

        ExistObjectRefDTO specialObject = policyDTO.getSpecialExistObject();

        StringBuilder sb = new StringBuilder();
        sb.append("configure terminal\n");

        // 源地址

        PolicyObjectDTO srcObject= new PolicyObjectDTO();
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())){
            srcObject = getAddressObjectGroup(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(),policyDTO.getSrcIpSystem(), true);

            if(!AliStringUtils.isEmpty(srcObject.getCommandLine())) {
                sb.append(srcObject.getCommandLine());
            }
        }


        // 目的地址
        PolicyObjectDTO dstObject = new PolicyObjectDTO();
        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())){
            dstObject=getAddressObjectGroup(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(),policyDTO.getDstIpSystem(), true);

            if(!AliStringUtils.isEmpty(dstObject.getCommandLine())) {
                sb.append(dstObject.getCommandLine());
            }
        }

        // 转换后源地址
        PolicyObjectDTO postSrcIp = new PolicyObjectDTO();
        if (!AliStringUtils.isEmpty(policyDTO.getPostSrcIp())) {
            postSrcIp = getAddressObjectGroup(policyDTO.getPostSrcIp(), policyDTO.getTheme(), policyDTO.getPostSrcAddressObjectName(), policyDTO.getPostSrcIpSystem(), true);

            if (!AliStringUtils.isEmpty(postSrcIp.getCommandLine())) {
                sb.append(postSrcIp.getCommandLine());
            }
        }


        // 转换后目的地址
        PolicyObjectDTO postDstIp = new PolicyObjectDTO();
        if (!AliStringUtils.isEmpty(policyDTO.getPostDstIp())) {
            postDstIp = getAddressObjectGroup(policyDTO.getPostDstIp(), policyDTO.getTheme(), policyDTO.getPostDstAddressObjectName(), policyDTO.getPostDstIpSystem(), true);

            if (!AliStringUtils.isEmpty(postDstIp.getCommandLine())) {
                sb.append(postDstIp.getCommandLine());
            }
        }

        // 记录创建对象名称集合
        List<String> addressObjectNameList = new ArrayList<>();
        List<String> addressObjectGroupNameList = new ArrayList<>();

        recordCreateObjectName(addressObjectNameList, addressObjectGroupNameList, null, null, srcObject, dstObject,
            postSrcIp, postDstIp, null, null);
        policyDTO.setAddressObjectNameList(addressObjectNameList);
        policyDTO.setAddressObjectGroupNameList(addressObjectGroupNameList);


        String protocolString = PolicyConstants.POLICY_STR_VALUE_ANY;
        String serviceCmd = "";

        Boolean isExist = false;


        if(policyDTO.getExistServiceNameList().size()>0 && policyDTO.getServiceList().get(0).getDstPorts().equals("any")){
            policyDTO.setExistServiceNameList(null);
        }
        if(policyDTO.getExistPostServiceNameList().size()>0 && policyDTO.getPostServiceList().get(0).getDstPorts().equals("any"))  {
            policyDTO.setExistPostServiceNameList(null);
        }


        List<String> createServiceNames = new ArrayList<>();

        //处理转换前服务
        if(CollectionUtils.isNotEmpty(policyDTO.getServiceList())){


            //是否复用转换前端口
            if(CollectionUtils.isNotEmpty(policyDTO.getExistServiceNameList())){
                isExist = true;
                List<String> existServiceNameList = policyDTO.getExistServiceNameList();
                serviceCmd = "service ";
                for(String existServiceName : existServiceNameList){
                    if(!policyDTO.getPostPort().equals("any")){
                        serviceCmd += existServiceName ;
                    }else {
                        serviceCmd += existServiceName +" "+ existServiceName;
                    }
                }
                //处理转换后端口
                //是否复用转换后端口
                if(CollectionUtils.isNotEmpty(policyDTO.getExistPostServiceNameList())){
                    List<String> existPostServiceNameList = policyDTO.getExistPostServiceNameList();
                    for(String existPostServiceName : existPostServiceNameList){
                        if(StringUtils.isNotEmpty(serviceCmd)){
                            serviceCmd += " " + existPostServiceName;
                        } else {
                            serviceCmd  += "service any "+ existPostServiceName;
                        }
                    }
                }else if(!policyDTO.getPostPort().equals("any")) {
                    ServiceDTO serviceDTO = policyDTO.getServiceList().get(0);
                    protocolString = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                    String portString  = "";
                    //多个端口/端口范围只取第一个
                    StringBuilder serviceSb = new StringBuilder();
                    String port = policyDTO.getPostPort().split(",")[0];
                    if(PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end  = PortUtils.getEndPort(port);
                        portString = String.format("%s-%s", start, end);
                        serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination range ").append(start).append(" ").append(end).append("\n");
                    } else {
                        if(!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            portString = String.format("%s", port);
                            serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination eq ").append(port).append("\n");
                        }
                    }
                    String serviceName = protocolString.toLowerCase()+"_" + portString;
                    if(StringUtils.isNotEmpty(serviceCmd)){
                        serviceCmd += " " + serviceName;
                    } else {
                        serviceCmd  += "service any "+ serviceName;
                    }
                    sb.append("object service ").append(serviceName).append("\n");
                    sb.append(serviceSb).append("exit\n");
                    createServiceNames.add(serviceName);
                }
            }
            if(isExist==false){
                if(!policyDTO.getServiceList().get(0).getProtocol().equals("any")){
                    // 只取第一个
                    ServiceDTO serviceDTO = policyDTO.getServiceList().get(0);
                    if(!AliStringUtils.isEmpty(serviceDTO.getProtocol())) {
                        protocolString = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                        String portString  = "";
                        //处理转换前端口
                        if(!serviceDTO.getDstPorts().equals("any")) {
                            //多个端口/端口范围只取第一个
                            StringBuilder serviceSb = new StringBuilder();
                            String port = serviceDTO.getDstPorts().split(",")[0];
                            if(PortUtils.isPortRange(port)) {
                                String start = PortUtils.getStartPort(port);
                                String end  = PortUtils.getEndPort(port);
                                portString = String.format("%s-%s", start, end);
                                serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination range ").append(start).append(" ").append(end).append("\n");
                            } else {
                                if(!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    portString = String.format("%s", port);
                                    serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination eq ").append(port).append("\n");
                                }
                            }
                            String serviceName = protocolString.toLowerCase()+"_" + portString + " ";
                            sb.append("object service ").append(serviceName).append("\n");
                            sb.append(serviceSb).append("exit\n");
                            createServiceNames.add(serviceName);

                            if(!StringUtils.equals(policyDTO.getPostPort(), "any")){
                                serviceCmd  += "service "+ serviceName;
                            }else {
                                serviceCmd  += "service "+ serviceName +serviceName;
                            }

                        }
                        //处理转换后端口
                        //是否复用转换后服务
                        if(CollectionUtils.isNotEmpty(policyDTO.getExistPostServiceNameList())){
                            List<String> existPostServiceNameList = policyDTO.getExistPostServiceNameList();
                            for(String existPostServiceName : existPostServiceNameList){
                                if(StringUtils.isNotEmpty(serviceCmd)){
                                    serviceCmd += " " + existPostServiceName;
                                } else {
                                    serviceCmd  += "service any "+ existPostServiceName;
                                }
                            }
                        }else if(!StringUtils.equals(policyDTO.getPostPort(), "any")) {
                            //多个端口/端口范围只取第一个
                            StringBuilder serviceSb = new StringBuilder();
                            String port = policyDTO.getPostPort().split(",")[0];
                            if(PortUtils.isPortRange(port)) {
                                String start = PortUtils.getStartPort(port);
                                String end  = PortUtils.getEndPort(port);
                                portString = String.format("%s-%s", start, end);
                                serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination range ").append(start).append(" ").append(end).append("\n");
                            } else {
                                if(!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    portString = String.format("%s", port);
                                    serviceSb.append("service ").append(protocolString.toLowerCase()).append(" destination eq ").append(port).append("\n");
                                }
                            }
                            String serviceName = protocolString.toLowerCase()+"_" + portString;
                            if(StringUtils.isNotEmpty(serviceCmd)){
                                serviceCmd += serviceName;
                            } else {
                                serviceCmd  += "service any "+ serviceName;
                            }
                            sb.append("object service ").append(serviceName).append("\n");
                            sb.append(serviceSb).append("exit\n");
                            createServiceNames.add(serviceName);
                        }

                    }
                }
                if((!protocolString.equals("any")) && StringUtils.isEmpty(serviceCmd)){
                    serviceCmd += "service " + protocolString.toLowerCase() + "_all" + " " + protocolString.toLowerCase() + "_all";
                    sb.append("object service ").append(protocolString.toLowerCase() + "_all").append("\n");
                    sb.append("service "+protocolString.toLowerCase()+"\nexit\n");
                    createServiceNames.add(protocolString.toLowerCase() + "_all");
                }
            }


        }

        policyDTO.setServiceObjectNameList(createServiceNames);

        StringBuilder rollbacksb = new StringBuilder();
        rollbacksb.append("configure terminal\n");
        rollbacksb.append(String.format("no nat (%s,%s) source %s %s %s destination static %s %s %s\n",StringUtils.isBlank(policyDTO.getSrcZone()) ? StringUtils.isBlank(policyDTO.getSrcItf()) ? "any" : policyDTO.getSrcItf() : policyDTO.getSrcZone(),
                StringUtils.isBlank(policyDTO.getDstZone()) ? StringUtils.isBlank(policyDTO.getDstItf()) ? "any" : policyDTO.getDstItf() : policyDTO.getDstZone(),
                policyDTO.isDynamic() ? "dynamic": "static",
                AliStringUtils.isEmpty(srcObject.getName()) ? "any" : srcObject.getName(),
                AliStringUtils.isEmpty(postSrcIp.getName()) ? "any" : postSrcIp.getName(),
                AliStringUtils.isEmpty(dstObject.getName()) ? "any" : dstObject.getName(),
                AliStringUtils.isEmpty(postDstIp.getName()) ? "any" : postDstIp.getName(),
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && isExist==false? "" : serviceCmd));
        rollbacksb.append("end\nwrite\n");
        policyDTO.setRollbackCommandLine(rollbacksb.toString());

        sb.append(String.format("nat (%s,%s) source %s %s %s destination static %s %s %s\n", StringUtils.isBlank(policyDTO.getSrcZone()) ? StringUtils.isBlank(policyDTO.getSrcItf()) ? "any" : policyDTO.getSrcItf() : policyDTO.getSrcZone(),
                StringUtils.isBlank(policyDTO.getDstZone()) ? StringUtils.isBlank(policyDTO.getDstItf()) ? "any" : policyDTO.getDstItf() : policyDTO.getDstZone(),
                policyDTO.isDynamic() ? "dynamic": "static",
                AliStringUtils.isEmpty(srcObject.getName()) ? "any" : srcObject.getName(),
                AliStringUtils.isEmpty(postSrcIp.getName()) ? "any" : postSrcIp.getName(),
                AliStringUtils.isEmpty(dstObject.getName()) ? "any" : dstObject.getName(),
                AliStringUtils.isEmpty(postDstIp.getName()) ? "any" : postDstIp.getName(),
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && isExist==false? "" : serviceCmd));
        sb.append("\nend\nwrite\n");
        sb.append("\n");
        return sb.toString();


    }

    private String getAddressObject(String ipAddress, String name){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("object network %s\n", name));
        if(IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            sb.append(String.format("range %s %s\n", start, end));
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            sb.append(String.format("subnet %s %s\n", ip, mask));
        } else {
            sb.append(String.format("host %s\n", ipAddress));
        }

        return sb.toString();
    }

    private PolicyObjectDTO getAddressObjectGroup(String ipAddresses, String name, String addressObjectName,String ipSystem, boolean createObject){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        //地址对象已存在，复用
        if(!AliStringUtils.isEmpty(addressObjectName)) {
            dto.setName(addressObjectName);
            dto.setCommandLine("");
            return dto;
        }

        //不强制创建对象的时候不对单个ip创建对象
        if(!createObject) {
            //若为单独IP地址，则不创建对象组
            if (IpUtils.isIP(ipAddresses)) {
                dto.setName(ipAddresses);
                dto.setCommandLine("");
                return dto;
            }
        }

        List<String>  createObjectNames = new ArrayList<>();
        List<String>  createGroupObjectNames = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String objectName = null;
        if (StringUtils.isNotBlank(ipSystem)) {
            objectName = dealIpSystemName(ipSystem);
        } else {
            objectName = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
        }
        String[] ipAddressList = ipAddresses.split(",");
        if(ipAddressList.length == 1) {
            sb.append(getAddressObject(ipAddressList[0], objectName));
            createObjectNames.add(objectName);
            sb.append("exit\n");
            dto.setGroup(false);
        } else {
            List<String> objectJoinList = new ArrayList<>();
            for (String ipAddress : ipAddressList) {
                String join = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
                if(IpUtils.isIPSegment(ipAddress) || IpUtils.isIPRange(ipAddress)) {
                    createObjectNames.add(join);
                    sb.append(getAddressObjectForObjectGroup(ipAddress, join));
                    join = "object " + join;
                } else {
                    join = "host " + ipAddress;
                }
                objectJoinList.add(join);
            }

            sb.append(String.format("object-group network %s\n", objectName));
            for(String join:objectJoinList) {
                sb.append("network " + join + "\n");
            }
            createGroupObjectNames.add(objectName);
            sb.append("exit\n");
            dto.setGroup(true);
        }
        dto.setObjectFlag(createObject);
        dto.setName(objectName);
        dto.setCommandLine(sb.toString());
        dto.setCreateObjectName(createObjectNames);
        dto.setCreateGroupObjectName(createGroupObjectNames);
        return dto;
    }

    /**
     * 处理系统名称 作为地址对象名称去创建对象
     * @param ipSystem
     */
    private String dealIpSystemName(String ipSystem) {
        String setName = ipSystem;
        // 对象名称长度限制，一个中文2个字符
        setName = strSub(setName, getMaxObejctNameLength(), "GB2312");
        // 对象名称长度限制
        int len = 0;
        try {
            len = setName.getBytes("GB2312").length;
        } catch (Exception e) {
            log.error("字符串长度计算异常");
        }
        if (len > getMaxObejctNameLength() - 7) {
            setName = strSub(setName, getMaxObejctNameLength() - 7, "GB2312");
        }
        setName = String.format("%s_%s", setName, DateUtils.getDate().replace("-", "").substring(2));
        return setName;
    }

    /**
     * 判断传进来的字符串，是否
     * 大于指定的字节，如果大于递归调用
     * 直到小于指定字节数 ，一定要指定字符编码，因为各个系统字符编码都不一样，字节数也不一样
     * @param s
     *            原始字符串
     * @param num
     *            传进来指定字节数
     * @return String 截取后的字符串

     * @throws
     */
    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            log.error("字符串长度计算异常");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }

    public int getMaxObejctNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
      CiscoASA99Nat cisco = new CiscoASA99Nat();
  //    StaticNatTaskDTO staticDto = new StaticNatTaskDTO();
  //    staticDto.setGlobalAddress("5.5.5.5");
  //    staticDto.setProtocol("6");
  //    staticDto.setInsidePort("20");
  //    staticDto.setTheme("static001");
  //    staticDto.setInsideAddress("any");
  //    staticDto.setInsidePort("any");
  //    staticDto.setGlobalPort("20");







     // System.out.println(cisco.generateStaticNatCommandLine(staticDto));

//        //源nat
//        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
//        sNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
//        sNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
////        sNatPolicyDTO.setSrcIp("");
////        sNatPolicyDTO.setDstIp("");
//
//        sNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
//        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
//        sNatPolicyDTO.setDescription("snatDesc");
//       sNatPolicyDTO.setSrcZone("trust");
//        sNatPolicyDTO.setDstZone("untrust");
//
//        sNatPolicyDTO.setSrcItf("srcItf");
//        sNatPolicyDTO.setDstItf("dstItf");
//
//        sNatPolicyDTO.setTheme("a1");
//
//     /*   sNatPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
//        sNatPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());
//
//        sNatPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
//        sNatPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
//        sNatPolicyDTO.setPostAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());*/
//
//        String snat = cisco.generateSNatCommandLine(sNatPolicyDTO);
//        System.out.println(snat);
//        System.out.println("--------------------------------------------------------------------------");
//
//          DNatPolicyDTO dnatPolicyDTO = new DNatPolicyDTO();
//
//        dnatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
//        dnatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
//        dnatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
//        dnatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        dnatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
//        dnatPolicyDTO.setPostPort("27");
//
//        dnatPolicyDTO.setSrcZone("trust");
//        dnatPolicyDTO.setDstZone("untrust");
//
//        dnatPolicyDTO.setSrcItf("srcItf");
//        dnatPolicyDTO.setDstItf("dstItf");
//        dnatPolicyDTO.setDescription("dnatDesc");
//        dnatPolicyDTO.setTheme("w1");
//        String dnat = cisco.generateDNatCommandLine(dnatPolicyDTO);
//        System.out.println(dnat);
//
      System.out.println("--------------------------------------------------------------------------");
      NatPolicyDTO bothNatDTO = new NatPolicyDTO();


      bothNatDTO.setPostSrcIp("1.1.1.1");
      bothNatDTO.setPostDstIp("2.2.2.2");

      bothNatDTO.setPostPort("27");

     ServiceDTO serviceDTO2 = new ServiceDTO();
     serviceDTO2.setProtocol("6");
     serviceDTO2.setDstPorts("any");

     List<ServiceDTO> serviceDTOList2 = new ArrayList<>();
     serviceDTOList2.add(serviceDTO2);
     bothNatDTO.setServiceList(serviceDTOList2);
     bothNatDTO.setRestPostServiceList(ServiceDTO.getServiceList());
       bothNatDTO.setTheme("w1");
       System.out.println(cisco.generateBothNatCommandLine(bothNatDTO));




    }
}
