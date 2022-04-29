package com.abtnetworks.totems.common.atomcommandline.nat;

import com.abtnetworks.totems.command.line.dto.InterfaceParamDTO;
import com.abtnetworks.totems.command.line.dto.NatPolicyParamDTO;
import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.vender.hillstone.nat.NatHillStoneR5Impl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service(value = "Atom HillstoneStoneOS NAT")
public class AtomHillstone implements NatPolicyGenerator {

    private NatHillStoneR5Impl generatorBean;

    public AtomHillstone() {
        generatorBean = new NatHillStoneR5Impl();
    }

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
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        sb.append(generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null));
        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();
        Param4CommandLineUtils.buildNatParamSrcIp(policyDTO.getSrcAddressObjectName(),policyDTO.getSrcIp(),policyDTO.getSrcIpSystem(), sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamDstIp(policyDTO.getDstAddressObjectName(),policyDTO.getDstIp(),policyDTO.getDstIpSystem(), sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamPostSrcIp(policyDTO.getPostAddressObjectName(),policyDTO.getPostIpAddress(),policyDTO.getPostSrcIpSystem(), sb, natPolicyParamDTO,generatorBean,createObjFlag);
        if(StringUtils.isNotBlank(policyDTO.getServiceObjectName())){
            natPolicyParamDTO.setRefServiceObject(new String[]{policyDTO.getServiceObjectName()});
        } else {
            if(CollectionUtils.isNotEmpty(policyDTO.getServiceList())){
                List<ServiceParamDTO> serviceParamDTOS = new ArrayList<>();
                for (ServiceDTO serviceDTO : policyDTO.getServiceList()) {
                    serviceParamDTOS.addAll(Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true));
                }
                natPolicyParamDTO.setServiceParam(serviceParamDTOS.toArray(new ServiceParamDTO[0]));
            }
        }
        natPolicyParamDTO.setDescription(policyDTO.getDescription());
        natPolicyParamDTO.setOutInterface(new InterfaceParamDTO(policyDTO.getDstItf()));
        if(policyDTO.getMoveSeatEnum() != null){
            natPolicyParamDTO.setMoveSeatEnum(MoveSeatEnum.getByCode(policyDTO.getMoveSeatEnum().getCode()));
            natPolicyParamDTO.setSwapRuleNameId(policyDTO.getSwapRuleNameId());
        }
        try {
            sb.append(generatorBean.generateSNatPolicyCommandLine(StatusTypeEnum.ADD,natPolicyParamDTO,null,null));
        } catch (Exception e) {
            log.error("原子化命令行生成山石SNAT策略异常",e);
            throw new RuntimeException(e.getMessage());
        }
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){

        StringBuilder sb = new StringBuilder();
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        sb.append(generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null));
        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();
        Param4CommandLineUtils.buildNatParamSrcIp(policyDTO.getSrcAddressObjectName(),policyDTO.getSrcIp(),policyDTO.getSrcIpSystem(), sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamDstIp(policyDTO.getDstAddressObjectName(),policyDTO.getDstIp(),policyDTO.getDstIpSystem(), sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamPostDstIp(policyDTO.getPostAddressObjectName(),policyDTO.getPostIpAddress(),policyDTO.getPostDstIpSystem(), sb, natPolicyParamDTO,generatorBean);
        if(StringUtils.isNotBlank(policyDTO.getServiceObjectName())){
            natPolicyParamDTO.setRefServiceObject(new String[]{policyDTO.getServiceObjectName()});
        } else {
            if(CollectionUtils.isNotEmpty(policyDTO.getServiceList())){
                List<ServiceParamDTO> serviceParamDTOS = new ArrayList<>();
                for (ServiceDTO serviceDTO : policyDTO.getServiceList()) {
                    serviceParamDTOS.addAll(Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true));
                }
                natPolicyParamDTO.setServiceParam(serviceParamDTOS.toArray(new ServiceParamDTO[0]));
            }
        }
        natPolicyParamDTO.setDescription(policyDTO.getDescription());
        natPolicyParamDTO.setInInterface(new InterfaceParamDTO(policyDTO.getSrcItf()));
        if(policyDTO.getMoveSeatEnum() != null){
            natPolicyParamDTO.setMoveSeatEnum(MoveSeatEnum.getByCode(policyDTO.getMoveSeatEnum().getCode()));
            natPolicyParamDTO.setSwapRuleNameId(policyDTO.getSwapRuleNameId());
        }
        if(StringUtils.isNotBlank(policyDTO.getPostPort())){
            List<Integer> dstSinglePortList = new ArrayList<>();
            List<String> dstSinglePortStrList = new ArrayList<>();
            List<PortRangeDTO> dstRangePortList = new ArrayList<>();
            for (String portStr : policyDTO.getPostPort().split(",")) {
                if (PortUtils.isPortRange(portStr)) {
                    String startPort = PortUtils.getStartPort(portStr);
                    String endPort = PortUtils.getEndPort(portStr);
                    PortRangeDTO portRangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                    dstRangePortList.add(portRangeDTO);
                } else {
                    if (StringUtils.isNumeric(portStr)) {
                        dstSinglePortList.add(Integer.parseInt(portStr));
                    } else {
                        dstSinglePortStrList.add(portStr);
                    }
                }
            }
            ServiceParamDTO postServiceParam = new ServiceParamDTO();
            postServiceParam.setDstSinglePortArray(dstSinglePortList.toArray(new Integer[0]));
            postServiceParam.setDstSinglePortStrArray(dstSinglePortStrList.toArray(new String[0]));
            postServiceParam.setDstRangePortArray(dstRangePortList.toArray(new PortRangeDTO[0]));
            natPolicyParamDTO.setPostServiceParam(new ServiceParamDTO[]{postServiceParam});
        }
        try {
            sb.append(generatorBean.generateDNatPolicyCommandLine(StatusTypeEnum.ADD,natPolicyParamDTO,null,null));
        } catch (Exception e) {
            log.error("原子化命令行生成山石DNAT策略异常",e);
            throw new RuntimeException(e.getMessage());
        }
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }


    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {

        StringBuilder sb = new StringBuilder();
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        sb.append(generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null));
        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();
        Param4CommandLineUtils.buildNatParamSrcIp(policyDTO.getSrcAddressObjectName(),policyDTO.getSrcIp(),null, sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamDstIp(policyDTO.getDstAddressObjectName(),policyDTO.getDstIp(),null, sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamPostSrcIp(policyDTO.getPostSrcAddressObjectName(),policyDTO.getPostSrcIp(),null, sb, natPolicyParamDTO,generatorBean,createObjFlag);
        Param4CommandLineUtils.buildNatParamPostDstIp(policyDTO.getPostDstAddressObjectName(),policyDTO.getPostDstIp(),null, sb, natPolicyParamDTO,generatorBean);
        if(StringUtils.isNotBlank(policyDTO.getServiceObjectName())){
            natPolicyParamDTO.setRefServiceObject(new String[]{policyDTO.getServiceObjectName()});
        } else {
            if(CollectionUtils.isNotEmpty(policyDTO.getServiceList())){
                List<ServiceParamDTO> serviceParamDTOS = new ArrayList<>();
                for (ServiceDTO serviceDTO : policyDTO.getServiceList()) {
                    serviceParamDTOS.addAll(Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true));
                }
                natPolicyParamDTO.setServiceParam(serviceParamDTOS.toArray(new ServiceParamDTO[0]));
            }
        }
        natPolicyParamDTO.setDescription(policyDTO.getDescription());
        natPolicyParamDTO.setOutInterface(new InterfaceParamDTO(policyDTO.getDstItf()));
        natPolicyParamDTO.setInInterface(new InterfaceParamDTO(policyDTO.getSrcItf()));
        if(policyDTO.getMoveSeatEnum() != null){
            natPolicyParamDTO.setMoveSeatEnum(MoveSeatEnum.getByCode(policyDTO.getMoveSeatEnum().getCode()));
            natPolicyParamDTO.setSwapRuleNameId(policyDTO.getSwapRuleNameId());
        }
        if(StringUtils.isNotBlank(policyDTO.getPostPort())){
            List<Integer> dstSinglePortList = new ArrayList<>();
            List<String> dstSinglePortStrList = new ArrayList<>();
            List<PortRangeDTO> dstRangePortList = new ArrayList<>();
            for (String portStr : policyDTO.getPostPort().split(",")) {
                if (PortUtils.isPortRange(portStr)) {
                    String startPort = PortUtils.getStartPort(portStr);
                    String endPort = PortUtils.getEndPort(portStr);
                    PortRangeDTO portRangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                    dstRangePortList.add(portRangeDTO);
                } else {
                    if (StringUtils.isNumeric(portStr)) {
                        dstSinglePortList.add(Integer.parseInt(portStr));
                    } else {
                        dstSinglePortStrList.add(portStr);
                    }
                }
            }
            ServiceParamDTO postServiceParam = new ServiceParamDTO();
            postServiceParam.setDstSinglePortArray(dstSinglePortList.toArray(new Integer[0]));
            postServiceParam.setDstSinglePortStrArray(dstSinglePortStrList.toArray(new String[0]));
            postServiceParam.setDstRangePortArray(dstRangePortList.toArray(new PortRangeDTO[0]));
            natPolicyParamDTO.setPostServiceParam(new ServiceParamDTO[]{postServiceParam});
        }
        try {
            sb.append(generatorBean.generateBothNatPolicyCommandLine(StatusTypeEnum.ADD,natPolicyParamDTO,null,null));
        } catch (Exception e) {
            log.error("原子化命令行生成山石BothNAT策略异常",e);
            throw new RuntimeException(e.getMessage());
        }
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }

}
