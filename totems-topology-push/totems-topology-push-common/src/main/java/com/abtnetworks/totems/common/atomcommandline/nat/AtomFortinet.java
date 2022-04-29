package com.abtnetworks.totems.common.atomcommandline.nat;


import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.vender.fortinet.security.NatFortinetImpl;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service(value = "Atom Fortinet NAT")
public class AtomFortinet implements NatPolicyGenerator {

    private NatFortinetImpl generatorBean;

    public AtomFortinet() {
        generatorBean = new NatFortinetImpl();
    }

    private static String SEPERATOR = ",";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate fortinet nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO){
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = new HashMap<>();

        map.put("hasVsys",policyDTO.isHasVsys());
        sb.append(generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),map,null));
        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();
        if (StringUtils.isNotEmpty(policyDTO.getFromZone())){
            natPolicyParamDTO.setSrcZone(new ZoneParamDTO(policyDTO.getFromZone()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getToZone())){
            natPolicyParamDTO.setDstZone(new ZoneParamDTO(policyDTO.getToZone()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getInDevItf())){
            natPolicyParamDTO.setInInterface(new InterfaceParamDTO(policyDTO.getInDevItf()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getOutDevItf())){
            natPolicyParamDTO.setOutInterface(new InterfaceParamDTO(policyDTO.getOutDevItf()));
        }

        String globalAddress = policyDTO.getGlobalAddress();
        String insideAddress = policyDTO.getInsideAddress();
        String name = String.format("mip_%s_%s ", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        sb.append("edit ").append(name).append("\n");
        Param4CommandLineUtils.setAddressCommandLine(globalAddress, sb, "extip ");
        sb.append("set extintf any\n");
        Param4CommandLineUtils.setAddressCommandLine(insideAddress, sb, "mappedip ");

        String globalPort = policyDTO.getGlobalPort();
        String insidePort = policyDTO.getInsidePort();
        String protocol = ProtocolUtils.getProtocolByString(policyDTO.getProtocol());

        sb.append(generatorBean.generateProtocolCommandline(policyDTO.getProtocol(),globalPort,insidePort,protocol));

        sb.append("next\n");
        sb.append("end\n\n");


        //是否复用
        String serviceName = "";
        Boolean isExist = false;
        if(StringUtils.isNotEmpty(policyDTO.getExistGlobaPort())){
            isExist=true;
            serviceName = policyDTO.getExistGlobaPort();
        }


        if(StringUtils.isNotEmpty(policyDTO.getProtocol()) && isExist==false){
            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(),PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("config firewall service custom\n");
                    if (PortUtils.isPortRange(policyDTO.getGlobalPort())) {
                        String startPort = PortUtils.getStartPort(policyDTO.getGlobalPort());
                        String endPort = PortUtils.getEndPort(policyDTO.getGlobalPort());
                        serviceName = String.format("\"%s%s-%s\"", protocol.substring(0, 1).toUpperCase(), startPort, endPort);
                    } else {
                        serviceName = String.format("\"%s%s\"", protocol.substring(0, 1).toUpperCase(), policyDTO.getGlobalPort());
                    }
                } else {
                    // 20211012 招商证券 修改如果服务不填的时候，修改命令行按照ALL去下发
                    serviceName = "ALL";
                }
            } else {
                serviceName = "ALL_ICMP";
            }

            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(),PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(policyDTO.getGlobalPort())) {
                        String startPort = PortUtils.getStartPort(policyDTO.getGlobalPort());
                        String endPort = PortUtils.getEndPort(policyDTO.getGlobalPort());
                        sb.append(String.format("set tcp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set tcp-portrange %s\n", policyDTO.getGlobalPort()));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }
            } else if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(),PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(policyDTO.getGlobalPort())) {
                        String startPort = PortUtils.getStartPort(policyDTO.getGlobalPort());
                        String endPort = PortUtils.getEndPort(policyDTO.getGlobalPort());
                        sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set udp-portrange %s\n", policyDTO.getGlobalPort()));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }

            }

        }
        natPolicyParamDTO.setRefServiceObject(new String[]{serviceName});

        natPolicyParamDTO.setDescription(policyDTO.getDescription());
        if(policyDTO.getMoveSeatEnum() != null){
            natPolicyParamDTO.setMoveSeatEnum(MoveSeatEnum.getByCode(policyDTO.getMoveSeatEnum().getCode()));
            natPolicyParamDTO.setSwapRuleNameId(policyDTO.getSwapRuleNameId());
        }
        Map nameHash = new HashMap();
        if(StringUtils.isNotBlank(policyDTO.getTheme())){
            nameHash.put("theme",policyDTO.getTheme());
        }
        nameHash.put("name",name);
        nameHash.put("serviceName",serviceName);
        try {
            sb.append(generatorBean.generateStaticNatPolicyCommandLine(StatusTypeEnum.ADD,natPolicyParamDTO,nameHash, null));
        } catch (Exception e) {
            log.error("原子化命令行生成飞塔DNAT策略异常",e);
            throw new RuntimeException(e.getMessage());
        }
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        log.info("飞塔的源nat的命令行的入参：{}", JSONObject.toJSONString(policyDTO));
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = new HashMap<>();
        map.put("hasVsys",policyDTO.isHasVsys());
        sb.append(generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),map,null));
        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();
        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        List<String> restDstAddressList = policyDTO.getRestDstAddressList();
        List<String> restPostSrcAddressList = policyDTO.getRestPostSrcAddressList();
        List<ServiceDTO> restServiceList = policyDTO.getRestServiceList();
        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        List<String> existDstAddressList = policyDTO.getExistDstAddressList();
        List<String> existPostSrcAddressList = policyDTO.getExistPostSrcAddressList();
        List<String> existServiceNameList = policyDTO.getExistServiceNameList();
        String srcAddressObjectName = policyDTO.getSrcAddressObjectName();
        String dstAddressObjectName = policyDTO.getDstAddressObjectName();
        String postAddressObjectName = policyDTO.getPostAddressObjectName();
        String serviceObjectName = policyDTO.getServiceObjectName();

        String src = "",dst = "",existSrc = "",existDst = "",postSrc = "",existPostSrc = "";
        if (CollectionUtils.isNotEmpty(restSrcAddressList)){
            String[] srcStringArray = (String[])(restSrcAddressList.toArray(new String[restSrcAddressList.size()]));
            src = String.join(",", srcStringArray);
        }
        if (CollectionUtils.isNotEmpty(restDstAddressList)){
            String[] dstStringArray = (String[])(restDstAddressList.toArray(new String[restSrcAddressList.size()]));
            dst = String.join(",", dstStringArray);
        }
        if (CollectionUtils.isNotEmpty(restPostSrcAddressList)){
            String[] postSrcStringArray = (String[])(restPostSrcAddressList.toArray(new String[restSrcAddressList.size()]));
            postSrc = String.join(",", postSrcStringArray);
        }
        if (CollectionUtils.isNotEmpty(existSrcAddressList)){
            String[] existSrcList = (String[])(existSrcAddressList.toArray(new String[restSrcAddressList.size()]));
            existSrc = String.join(",", existSrcList);
        }
        if (CollectionUtils.isNotEmpty(existDstAddressList)){
            String[] existDstList = (String[])(existDstAddressList.toArray(new String[restSrcAddressList.size()]));
            existDst = String.join(",", existDstList);
        }
        if (CollectionUtils.isNotEmpty(existPostSrcAddressList)){
            String[] existPostSrcList = (String[])(existPostSrcAddressList.toArray(new String[restSrcAddressList.size()]));
            existPostSrc = String.join(",", existPostSrcList);
        }
        if (StringUtils.isNotEmpty(srcAddressObjectName)){
            existSrc = srcAddressObjectName + ","+existSrc;
        }
        if (StringUtils.isNotEmpty(dstAddressObjectName)){
            existDst = dstAddressObjectName + ","+existDst;
        }
        if (StringUtils.isNotEmpty(postAddressObjectName)){
            existPostSrc = postAddressObjectName + ","+existPostSrc;
        }

        Param4CommandLineUtils.buildNatParamSrcIp(existSrc,src,policyDTO.getSrcIpSystem(), sb, natPolicyParamDTO,generatorBean,true);
        Param4CommandLineUtils.buildNatParamDstIp(existDst,dst,policyDTO.getDstIpSystem(), sb, natPolicyParamDTO,generatorBean,true);
        Param4CommandLineUtils.buildNatParamPostSrcIp(existPostSrc,postSrc,policyDTO.getPostSrcIpSystem(), sb, natPolicyParamDTO,generatorBean,true);

        List<String> serviceNameList = new ArrayList<>();
        if(StringUtils.isNotEmpty(serviceObjectName)){
            serviceNameList.add(serviceObjectName);
        } else {
            if (CollectionUtils.isNotEmpty(existServiceNameList)){
                serviceNameList.addAll(existDstAddressList);
            }
            if(CollectionUtils.isNotEmpty(restServiceList)){
                List<ServiceParamDTO> serviceParamDTOS = new ArrayList<>();
                if (ProtocolUtils.getProtocolByString(restServiceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    serviceNameList.add("ALL");
                }else {

                    for (ServiceDTO serviceDTO : restServiceList) {
                        if (serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                            serviceNameList.add(String.format("ALL_%s", protocol.toUpperCase()));
                            continue;
                        }
                        serviceParamDTOS.addAll(Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true));
                    }
                    String ObjectName = generatorBean.createServiceObjectName(serviceParamDTOS, null, null);
                    if (StringUtils.isNotEmpty(ObjectName)){
                        if(ObjectName.contains(",")){
                            for(String tmpServiceObjectName : ObjectName.split(",")){
                                serviceNameList.add(tmpServiceObjectName);
                            }
                        }else{
                            serviceNameList.add(ObjectName);
                        }
                    }
                    try {
                        sb.append(generatorBean.generateServiceObjectCommandLine(StatusTypeEnum.ADD,ObjectName,null,null,serviceParamDTOS,null,null,null));
                    } catch (Exception e) {
                        log.error("原子化命令行创建服务对象异常",e);
                    }

                }
            }
        }
        natPolicyParamDTO.setRefServiceObject(serviceNameList.toArray(new String[0]));
        natPolicyParamDTO.setDescription(policyDTO.getDescription());
        if (StringUtils.isNotEmpty(policyDTO.getSrcZone())){
            natPolicyParamDTO.setSrcZone(new ZoneParamDTO(policyDTO.getSrcZone()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getDstZone())){
            natPolicyParamDTO.setDstZone(new ZoneParamDTO(policyDTO.getDstZone()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getDstItf())){
            natPolicyParamDTO.setOutInterface(new InterfaceParamDTO(policyDTO.getDstItf()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getSrcItf())){
            natPolicyParamDTO.setInInterface(new InterfaceParamDTO(policyDTO.getSrcItf()));
        }
        if(policyDTO.getMoveSeatEnum() != null){
            natPolicyParamDTO.setMoveSeatEnum(MoveSeatEnum.getByCode(policyDTO.getMoveSeatEnum().getCode()));
            natPolicyParamDTO.setSwapRuleNameId(policyDTO.getSwapRuleNameId());
        }
        Map nameHash = new HashMap();
        if(StringUtils.isNotBlank(policyDTO.getTheme())){
            nameHash.put("theme",policyDTO.getTheme());
        }
        try {
            log.info("源nat-natPolicyParamDTO:{}",JSONObject.toJSONString(natPolicyParamDTO));
            String sNatCommnadLine= generatorBean.generateSNatPolicyCommandLine(StatusTypeEnum.ADD, natPolicyParamDTO, nameHash, null);
            log.info("生成sNat的策略：{}",sNatCommnadLine);
            sb.append(sNatCommnadLine);
        } catch (Exception e) {
            log.error("原子化命令行生成飞塔SNAT策略异常",e);
            throw new RuntimeException(e.getMessage());
        }
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();
        String dstIp = policyDTO.getDstIp();
        String srcItf = policyDTO.getSrcItf();
        String startTime = policyDTO.getStartTime();
        String endTime = policyDTO.getEndTime();
        String postIpAddress = policyDTO.getPostIpAddress();
        Map<String, Object> map = new HashMap<>();

        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        String srcAddressObjectName = policyDTO.getSrcAddressObjectName();

        String src = "",existSrc = "";
        if (CollectionUtils.isNotEmpty(restSrcAddressList)){
            String[] srcStringArray = (String[])(restSrcAddressList.toArray(new String[restSrcAddressList.size()]));
            src = String.join(",", srcStringArray);
        }
        if (CollectionUtils.isNotEmpty(existSrcAddressList)){
            String[] existSrcList = (String[])(existSrcAddressList.toArray(new String[restSrcAddressList.size()]));
            existSrc = String.join(",", existSrcList);
        }
        if (StringUtils.isNotEmpty(srcAddressObjectName)){
            existSrc = srcAddressObjectName + ","+existSrc;
        }

        map.put("hasVsys",policyDTO.isHasVsys());
        sb.append(generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),map,null));
        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();
        if (StringUtils.isNotEmpty(policyDTO.getSrcZone())){
            natPolicyParamDTO.setSrcZone(new ZoneParamDTO(policyDTO.getSrcZone()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getDstZone())){
            natPolicyParamDTO.setDstZone(new ZoneParamDTO(policyDTO.getDstZone()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getSrcItf())){
            natPolicyParamDTO.setInInterface(new InterfaceParamDTO(policyDTO.getSrcItf()));
        }
        if (StringUtils.isNotEmpty(policyDTO.getDstItf())){
            natPolicyParamDTO.setOutInterface(new InterfaceParamDTO(policyDTO.getDstItf()));
        }


        String name = String.format("mip_%s_%s ", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        sb.append("edit ").append(name).append("\n");
        Param4CommandLineUtils.setAddressCommandLine(dstIp, sb, "extip ");
        if(StringUtils.isNotEmpty(srcItf)){
            sb.append(String.format("set extintf \"%s\"",srcItf)).append("\n");
        }else {
            sb.append("set extintf any\n");
        }
        Param4CommandLineUtils.setAddressCommandLine(postIpAddress, sb, "mappedip ");

        String protocol = policyDTO.getServiceList().get(0).getProtocol();
        String dstPorts = policyDTO.getServiceList().get(0).getDstPorts();
        String postPort = policyDTO.getPostPort();
        String protocolString = ProtocolUtils.getProtocolByString(protocol);

        sb.append(generatorBean.generateProtocolCommandline(protocol,dstPorts,postPort,protocolString));

        sb.append("next\n");
        sb.append("end\n\n");

        String refTimeName = Param4CommandLineUtils.getRefTimeName(startTime, endTime, sb,generatorBean,null);

        //是否复用
        String serviceName = "";
        Boolean isExist = false;
        if(CollectionUtils.isNotEmpty(policyDTO.getExistServiceNameList())){
            List<String> existServiceNameList = policyDTO.getExistServiceNameList();
            String existServiceName= existServiceNameList.get(0);
            serviceName = existServiceName;
            isExist=true;
        }

        Param4CommandLineUtils.buildNatParamSrcIp(existSrc,src,policyDTO.getSrcIpSystem(), sb, natPolicyParamDTO,generatorBean,true);

        if(StringUtils.isNotEmpty(protocol) && isExist==false){
            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("config firewall service custom\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        serviceName = String.format("\"%s%s-%s\"", protocolString.substring(0, 1).toUpperCase(), startPort, endPort);
                    } else {
                        serviceName = String.format("\"%s%s\"", protocolString.substring(0, 1).toUpperCase(), dstPorts);
                    }
                } else {
                    // 20211012 招商证券 修改如果服务不填的时候，修改命令行按照ALL去下发
                    serviceName = "ALL";
                }
            } else {
                serviceName = "ALL_ICMP";
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        sb.append(String.format("set tcp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set tcp-portrange %s\n", dstPorts));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set udp-portrange %s\n", dstPorts));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }
            }
        }
        natPolicyParamDTO.setRefServiceObject(new String[]{serviceName});


        natPolicyParamDTO.setDescription(policyDTO.getDescription());
        if(policyDTO.getMoveSeatEnum() != null){
            natPolicyParamDTO.setMoveSeatEnum(MoveSeatEnum.getByCode(policyDTO.getMoveSeatEnum().getCode()));
            natPolicyParamDTO.setSwapRuleNameId(policyDTO.getSwapRuleNameId());
        }
        Map nameHash = new HashMap();
        if(StringUtils.isNotBlank(policyDTO.getTheme())){
            nameHash.put("theme",policyDTO.getTheme());
        }
        nameHash.put("name",name);
        nameHash.put("refTimeName",refTimeName);
        try {
            sb.append(generatorBean.generateDNatPolicyCommandLine(StatusTypeEnum.ADD,natPolicyParamDTO,nameHash, null));
        } catch (Exception e) {
            log.error("原子化命令行生成飞塔DNAT策略异常",e);
            throw new RuntimeException(e.getMessage());
        }
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }


    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        return StringUtils.EMPTY;
    }


    public static void main(String[] args) {


        //String a={"ciscoEnable":true,"createObjFlag":false,"currentId":"17","description":"","dstIp":"1.2.19.2/32","dstIpSystem":"3","dstItf":"","dstZone":"","existDstAddressList":[],"existDstAddressName":[],"existPostSrcAddressList":[],"existPostSrcAddressName":[],"existServiceNameList":[],"existSrcAddressList":[],"existSrcAddressName":[],"postIpAddress":"1.1.9.2/32","postSrcIpSystem":"2","restDstAddressList":["1.2.19.2/32"],"restPostSrcAddressList":["1.1.9.2/32"],"restServiceList":[{"dstPorts":"30","protocol":"6","srcPorts":"any"}],"restSrcAddressList":["1.1.1.2/24"],"serviceList":[{"dstPorts":"30","protocol":"6","srcPorts":"any"}],"srcIp":"1.1.1.2/24","srcIpSystem":"1","srcItf":"","srcZone":"","theme":"12","vsys":false,"vsysName":""}"
//        SNatPolicyDTO sNatPolicyDTO=new SNatPolicyDTO();
//        DNatPolicyDTO sNatPolicyDTO=new DNatPolicyDTO();
        StaticNatTaskDTO sNatPolicyDTO=new StaticNatTaskDTO();
        List<String> restSrcAddressList = new ArrayList<>();
        List<String> restDstAddressList = new ArrayList<>();
        List<String> restPostSrcAddressList = new ArrayList<>();
        List<ServiceDTO> restServiceList = new ArrayList<>();
        List<String> existSrcAddressList = new ArrayList<>();
        List<String> existDstAddressList = new ArrayList<>();
        List<String> existPostSrcAddressList = new ArrayList<>();
        List<String> existServiceNameList = new ArrayList<>();


//        restSrcAddressList.add("1.1.1.1/24");
//        sNatPolicyDTO.setRestSrcAddressList(restSrcAddressList);
//
//        restDstAddressList.add("2.2.2.2/24");
//        sNatPolicyDTO.setRestDstAddressList(restDstAddressList);
//
//        restPostSrcAddressList.add("3.3.3.3/24");
////        sNatPolicyDTO.setRestPostSrcAddressList(restPostSrcAddressList);
//        sNatPolicyDTO.setRestPostDstAddressList(restPostSrcAddressList);
//
//        ServiceDTO dto=new ServiceDTO();
//        dto.setProtocol("6");
//        dto.setSrcPorts("any");
//        dto.setDstPorts("80");
//        restServiceList.add(dto);
//        sNatPolicyDTO.setRestServiceList(restServiceList);
//        sNatPolicyDTO.setServiceList(restServiceList);
//
//        existSrcAddressList.add("444");
//        sNatPolicyDTO.setExistSrcAddressList(existSrcAddressList);
//
//        existDstAddressList.add("555");
//        sNatPolicyDTO.setExistDstAddressList(existDstAddressList);
//
//        existPostSrcAddressList.add("666");
////        sNatPolicyDTO.setExistPostSrcAddressList(existPostSrcAddressList);
//        sNatPolicyDTO.setExistPostDstAddressList(existPostSrcAddressList);
//
////        existServiceNameList.add("777");
//        sNatPolicyDTO.setExistServiceNameList(existServiceNameList);
//
//        sNatPolicyDTO.setSrcAddressObjectName("888");
//        sNatPolicyDTO.setDstAddressObjectName("999");
////        sNatPolicyDTO.setServiceObjectName("1010");
//
////        sNatPolicyDTO.setCiscoEnable(true);
//        sNatPolicyDTO.setTheme("主题");
//        sNatPolicyDTO.setCreateObjFlag(false);
//        sNatPolicyDTO.setCurrentId("17");
//        sNatPolicyDTO.setDescription("测试123");
//        sNatPolicyDTO.setDstIp("2.2.2.2/24");
////        sNatPolicyDTO.setDstIpSystem("2222");
//        sNatPolicyDTO.setSrcIp("1.1.1.1/24");
////        sNatPolicyDTO.setSrcIpSystem("1111");
//        sNatPolicyDTO.setPostIpAddress("3.3.3.3/24");
////        sNatPolicyDTO.setPostSrcIpSystem("3333");
//        sNatPolicyDTO.setCreateObjFlag(true);
//        List rest = new ArrayList();
//        rest.add("3.3.3.3/24");
//        sNatPolicyDTO.setRestPostSrcAddressList(rest);
//
//        List src = new ArrayList();
//        rest.add("1.1.1.1/24");
//        sNatPolicyDTO.setRestPostSrcAddressList(src);
//
//        List dst = new ArrayList();
//        rest.add("2.2.2.2/24");
//        sNatPolicyDTO.setRestPostSrcAddressList(dst);
//
//        List<ServiceDTO> services=new ArrayList<>();
//        ServiceDTO dto=new ServiceDTO();
//        dto.setProtocol("6");
//        dto.setSrcPorts("any");
//        dto.setDstPorts("80");
//        services.add(dto);
//        sNatPolicyDTO.setServiceList(services);
        sNatPolicyDTO.setMoveSeatEnum(com.abtnetworks.totems.common.enums.MoveSeatEnum.FIRST);
        sNatPolicyDTO.setCurrentId("test snat");


        sNatPolicyDTO.setGlobalPort("80");
        sNatPolicyDTO.setInsidePort("90");
        sNatPolicyDTO.setProtocol("6");
        sNatPolicyDTO.setGlobalAddress("1.1.1.1/24");
        sNatPolicyDTO.setInsideAddress("2.2.2.2/24");
        sNatPolicyDTO.setTheme("主题");
        sNatPolicyDTO.setCurrentId("17");
        sNatPolicyDTO.setDescription("测试123");

        AtomFortinet natFortinet=new AtomFortinet();
//        String sNatCommandLine = natFortinet.generateSNatCommandLine(sNatPolicyDTO);
//        String sNatCommandLine = natFortinet.generateDNatCommandLine(sNatPolicyDTO);
        String sNatCommandLine = natFortinet.generateStaticNatCommandLine(sNatPolicyDTO);
        System.out.println("sNatCommandLine:\n"+sNatCommandLine);

        //{"ciscoEnable":true,"createObjFlag":false,"currentId":"16","description":"","dstIp":"4.4.4.2","dstIpSystem":"3","dstItf":"","dstZone":"","existDstAddressList":[],"existDstAddressName":[],"existPostSrcAddressList":[],"existPostSrcAddressName":[],"existServiceNameList":[],"existSrcAddressList":[],"existSrcAddressName":[],"postIpAddress":"2.2.2.3","postSrcIpSystem":"2","restDstAddressList":["4.4.4.2"],"restPostSrcAddressList":["2.2.2.3"],"restServiceList":[{"dstPorts":"80","protocol":"6","srcPorts":"any"}],"restSrcAddressList":["1.1.1.3"],"serviceList":[{"dstPorts":"80","protocol":"6","srcPorts":"any"}],"srcIp":"1.1.1.3","srcIpSystem":"1","srcItf":"","srcZone":"","theme":"12","vsys":false,"vsysName":""}

    }

}
