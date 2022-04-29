package com.abtnetworks.totems.common.atomcommandline.security;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.vender.leadSecPowerV.security.SecurityLeadSecPowerV30Impl;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @Title:
 * @Description: 网御安全策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
public class AtomSecurityLeadSecPowerV30 extends SecurityPolicyGenerator implements PolicyGenerator {
    private SecurityLeadSecPowerV30Impl generatorBean;

    public AtomSecurityLeadSecPowerV30() {
        generatorBean = new SecurityLeadSecPowerV30Impl();
    }


    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("原子化命令行 cmdDTO is " + cmdDTO);
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
        } else {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
        }

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        if(ObjectUtils.isNotEmpty(taskDTO.getTheme())){
            String theme=taskDTO.getTheme();
            String policyName=theme+"_AO_"+ RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
            dto.setBusinessName(policyName);
        }
        // ip类型默认为ipv4
        if (ObjectUtils.isEmpty(dto.getIpType())) {
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        generatedDto.setVsys(dto.isVsys());
        generatedDto.setVsysName(dto.getVsysName());
        generatedDto.setHasVsys(dto.isHasVsys());
        log.info("原子化命令行dto:{}", JSON.toJSONString(dto));
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        Map<String,Object> map = new HashMap<>();
        String cmd = "";
        String srcString = dto.getRestSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);
        String dstString = dto.getRestDstAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO dstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstString);
        //服务(所有)
        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
        List<ServiceDTO> serviceList = dto.getServiceList();
        for (ServiceDTO serviceDTO : serviceList) {
            List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
            serviceParamDTOList.addAll(c);
        }
        ServiceParamDTO[] serviceParamDTOS =serviceParamDTOList.toArray(new ServiceParamDTO[serviceParamDTOList.size()]);
        //服务（还需创建）
        List<ServiceParamDTO> restServiceParamDTOList = new ArrayList<>();
        List<ServiceDTO> restServiceList = dto.getRestServiceList();
        for (ServiceDTO serviceDTO : restServiceList) {
            List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
            restServiceParamDTOList.addAll(c);
        }
        ServiceParamDTO[] restServiceParamDTOS =restServiceParamDTOList.toArray(new ServiceParamDTO[restServiceParamDTOList.size()]);
        map.put("restServiceParamDTOS",restServiceParamDTOS);

        AbsoluteTimeParamDTO absoluteTimeParamDTO = new AbsoluteTimeParamDTO(dto.getStartTime(), dto.getEndTime());

        ZoneParamDTO srcZone = new ZoneParamDTO(dto.getSrcZone());
        ZoneParamDTO dstZone = new ZoneParamDTO(dto.getDstZone());

        InterfaceParamDTO srcIntf = new InterfaceParamDTO(dto.getSrcItf());
        InterfaceParamDTO dstIntf = new InterfaceParamDTO(dto.getDstItf());

        List<String> existSrcAddressList = dto.getExistSrcAddressList();
        String[] srcRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existSrcAddressList)){
            srcRefIpAddressObject =existSrcAddressList.toArray(new String[0]);
        }
        String[] srcRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(dto.getSrcAddressName())){
            srcRefIpAddressObjectGroup = new String[]{dto.getSrcAddressName()};
        }

        List<String> existDstAddressList = dto.getExistDstAddressList();
        String[] dstRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existDstAddressList)){
            dstRefIpAddressObject =  existDstAddressList.toArray(new String[0]);
        }
        String[] dstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(dto.getDstAddressName())){
            dstRefIpAddressObjectGroup = new String[]{dto.getDstAddressName()};
        }

        List<String> existServiceNameList = dto.getExistServiceNameList();
        String[]  refServiceObject = null;
        if(!CollectionUtils.isEmpty(existServiceNameList)){
            refServiceObject = existServiceNameList.toArray(new String[0]);
        }
        String[] refServiceObjectGroup = null;
        if(!StringUtils.isBlank(dto.getServiceName())){
            refServiceObjectGroup = new String[]{dto.getServiceName()};
        }
        List<String> timeObjectNameList = dto.getTimeObjectNameList();
        String[]  refTimeObject = null;
        if(!CollectionUtils.isEmpty(timeObjectNameList)){
            refTimeObject =  timeObjectNameList.toArray(new String[0]);
        }
        try {
            cmd = generatorBean.generateSecurityPolicyCommandLine(StatusTypeEnum.ADD,null,dto.getName(),dto.getPolicyId(),dto.getAction().toLowerCase(),
                    dto.getDescription(),null,null,null, MoveSeatEnum.getByCode(dto.getMoveSeatEnum().getCode())
                    ,dto.getSwapRuleNameId(),srcIpAddressParamDTO, dstIpAddressParamDTO,serviceParamDTOS,absoluteTimeParamDTO,null,
                    srcZone,dstZone,srcIntf, dstIntf,srcRefIpAddressObject,srcRefIpAddressObjectGroup,
                    dstRefIpAddressObject,dstRefIpAddressObjectGroup, refServiceObject,refServiceObjectGroup,
                    refTimeObject,map,null);
        } catch (Exception e) {
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return generatorBean.generatePostCommandline(null,null);
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        AtomSecurityLeadSecPowerV30 abtcomandEntity = new AtomSecurityLeadSecPowerV30();
        dto.setSrcIp("2.2.2.2/24,5.5.5.5-5.5.5.9");
        List<String> a = new ArrayList<>();
        a.add("1.1.1.1");
        a.add("2.2.2.2/24");
        a.add("6.6.6.6-6.6.6.9");
        List<String> n = new ArrayList<>();
        n.add("asdasdasd");
        dto.setRestSrcAddressList(a);
        dto.setRestDstAddressList(a);
        dto.setExistDstAddressList(n);


        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("43");
        serviceDTO.setSrcPorts("any");
        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setDstPorts("22");
        serviceDTO1.setSrcPorts("any");
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("6");
        serviceDTO2.setDstPorts("85");
        serviceDTO2.setSrcPorts("any");
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        //serviceDTOS.add(serviceDTO);
        serviceDTOS.add(serviceDTO1);
        serviceDTOS.add(serviceDTO2);
        //serviceDTOS.add(serviceDTO1);
        //dto.setRestServiceList(serviceDTOS);
       // dto.setExistServiceNameList(n);
        dto.setServiceList(serviceDTOS);

        List<ServiceDTO> serviceDTOS1 = new ArrayList<>();
        //serviceDTOS1.add(serviceDTO);
        dto.setRestServiceList(serviceDTOS1);

        List<String> names = new ArrayList<>();
        names.add("tcp22");
        names.add("tcp85");
        dto.setExistServiceNameList(names);
        dto.setMoveSeatEnum(com.abtnetworks.totems.common.enums.MoveSeatEnum.FIRST);
        dto.setAction("deny");
        String commandLine = abtcomandEntity.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
