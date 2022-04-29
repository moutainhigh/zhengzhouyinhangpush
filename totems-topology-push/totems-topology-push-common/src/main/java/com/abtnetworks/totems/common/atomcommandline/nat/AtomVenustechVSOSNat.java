package com.abtnetworks.totems.common.atomcommandline.nat;

import com.abtnetworks.totems.command.line.dto.InterfaceParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.dto.ZoneParamDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.vender.venustech.nat.NatVenustechVSOSImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service(value = "Atom VenustechVSOS NAT")
public class AtomVenustechVSOSNat implements NatPolicyGenerator {

    private NatVenustechVSOSImpl generatorBean;

    public AtomVenustechVSOSNat() {
        generatorBean = new NatVenustechVSOSImpl();
    }

    private static String SEPERATOR = ",";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate VenustechVSOS nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        String cmd = "";
        String srcString = policyDTO.getGlobalAddress();
        if(StringUtils.isNotEmpty(srcString) &&( srcString.split(",").length>1 || IPUtil.isIPRange(srcString) || IPUtil.isIPSegment(srcString))){
            return "外网地址只支持单IP！";
        }
        IpAddressParamDTO globalIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);
        String dstString = policyDTO.getInsideAddress();
        if(StringUtils.isNotEmpty(dstString) && (dstString.split(",").length>1 || IPUtil.isIPRange(dstString) || IPUtil.isIPSegment(dstString))){
            return "内网地址只支持单IP！";
        }
        IpAddressParamDTO insideIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstString);
        InterfaceParamDTO inIntf = new InterfaceParamDTO(policyDTO.getInDevItf());
        InterfaceParamDTO outIntf = new InterfaceParamDTO(policyDTO.getOutDevItf());
        ZoneParamDTO srcZone = new ZoneParamDTO(policyDTO.getFromZone());
        ZoneParamDTO dstZone = new ZoneParamDTO(policyDTO.getToZone());
        try {
            cmd = generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null)
                    +generatorBean.generateStaticNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),policyDTO.getCurrentId(),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,null,insideIpAddressParamDTO,globalIpAddressParamDTO,null,null,srcZone,dstZone,inIntf,outIntf,
                    null,null,null,null,null,null)
                    +generatorBean.generatePostCommandline(null,null);
        } catch (Exception e) {
            cmd = "启明星辰staticNat生成错误";
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        Map<String,Object> map = new HashMap<>();
        map.put("srcIpSystem",policyDTO.getSrcIpSystem());
        map.put("dstIpSystem",policyDTO.getDstIpSystem());
        map.put("postIpSystem",policyDTO.getPostSrcIpSystem());
        String cmd = "";
        String srcString = policyDTO.getRestSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);
        String dstString = policyDTO.getRestDstAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO dstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstString);

        String postSrcString = policyDTO.getRestPostSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO postSrcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postSrcString);
        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();

        List<ServiceDTO> serviceList = policyDTO.getRestServiceList();
        if(!CollectionUtils.isEmpty(serviceList)){
            for (ServiceDTO serviceDTO : serviceList) {
                List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                serviceParamDTOList.addAll(c);
            }
        }
        ServiceParamDTO[] serviceParamDTOS =serviceParamDTOList.toArray(new ServiceParamDTO[serviceParamDTOList.size()]);

        ZoneParamDTO srcZone = new ZoneParamDTO(policyDTO.getSrcZone());
        ZoneParamDTO dstZone = new ZoneParamDTO(policyDTO.getDstZone());

        InterfaceParamDTO srcIntf = new InterfaceParamDTO(policyDTO.getSrcItf());
        InterfaceParamDTO dstIntf = new InterfaceParamDTO(policyDTO.getDstItf());
        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        String[] srcRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existSrcAddressList)){
            srcRefIpAddressObject =existSrcAddressList.toArray(new String[0]);
        }
        String[] srcRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getSrcAddressObjectName())){
            srcRefIpAddressObjectGroup = new String[]{policyDTO.getSrcAddressObjectName()};
        }
        List<String> existDstAddressList = policyDTO.getExistDstAddressList();
        String[] dstRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existDstAddressList)){
            dstRefIpAddressObject =  existDstAddressList.toArray(new String[0]);
        }
        String[] dstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getDstAddressObjectName())){
            dstRefIpAddressObjectGroup = new String[]{policyDTO.getDstAddressObjectName()};
        }
        List<String> existPostSrcAddressList = policyDTO.getExistPostSrcAddressList();
        String[] postSrcRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existPostSrcAddressList)){
            postSrcRefIpAddressObject =existPostSrcAddressList.toArray(new String[0]);
        }
        String[] postSrcRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getPostAddressObjectName())){
            postSrcRefIpAddressObjectGroup = new String[]{policyDTO.getPostAddressObjectName()};
        }
        List<String> existServiceNameList = policyDTO.getExistServiceNameList();
        String[]  refServiceObject = null;
        if(!CollectionUtils.isEmpty(existServiceNameList)){
            refServiceObject = existServiceNameList.toArray(new String[0]);
        }
        String[] refServiceObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getServiceObjectName())){
            refServiceObjectGroup = new String[]{policyDTO.getServiceObjectName()};
        }

        try {
            cmd = generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null)
                    +generatorBean.generateSNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),policyDTO.getCurrentId(),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,policyDTO.getSwapRuleNameId(),srcIpAddressParamDTO, dstIpAddressParamDTO,serviceParamDTOS,postSrcIpAddressParamDTO,
                    srcZone,dstZone,srcIntf, dstIntf,null,srcRefIpAddressObject,srcRefIpAddressObjectGroup,
                    dstRefIpAddressObject,dstRefIpAddressObjectGroup, refServiceObject,refServiceObjectGroup,postSrcRefIpAddressObject,postSrcRefIpAddressObjectGroup,
                    map,null)
                    +generatorBean.generatePostCommandline(null,null);
        } catch (Exception e) {
            cmd = "启明星辰Snat生成错误";
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        Map<String,Object> map = new HashMap();
        map.put("srcIpSystem",policyDTO.getSrcIpSystem());
        map.put("dstIpSystem",policyDTO.getDstIpSystem());
        map.put("postIpSystem",policyDTO.getPostDstIpSystem());
        String cmd = "";
        String srcString = policyDTO.getRestSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);

        String dstString = policyDTO.getRestDstAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO dstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstString);

        String postDstString = policyDTO.getRestPostDstAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO postDstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postDstString);

        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
        List<ServiceDTO> serviceList = policyDTO.getRestServiceList();
        if(!CollectionUtils.isEmpty(serviceList)){
            for (ServiceDTO serviceDTO : serviceList) {
                List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                serviceParamDTOList.addAll(c);
            }
        }
        ServiceParamDTO[] serviceParamDTOS =serviceParamDTOList.toArray(new ServiceParamDTO[serviceParamDTOList.size()]);
        String postPort = policyDTO.getPostPort();
        if(!StringUtils.isBlank(postPort)){
            String[] split = postPort.split(",");
            if(split[0].contains("-")){
                split[0] = String.format("%s %s",split[0].split("-")[0],split[0].split("-")[1]);
            }
            map.put("postPort",split[0]);
        }

        InterfaceParamDTO srcIntf = new InterfaceParamDTO(policyDTO.getSrcItf());
        InterfaceParamDTO dstIntf = new InterfaceParamDTO(policyDTO.getDstItf());

        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        String[] srcRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existSrcAddressList)){
            srcRefIpAddressObject =existSrcAddressList.toArray(new String[0]);
        }
        String[] srcRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getSrcAddressObjectName())){
            srcRefIpAddressObjectGroup = new String[]{policyDTO.getSrcAddressObjectName()};
        }
        List<String> postExistDstAddressList = policyDTO.getExistPostDstAddressList();
        String[] postDstRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(postExistDstAddressList)){
            postDstRefIpAddressObject =  postExistDstAddressList.toArray(new String[0]);
        }
        String[] postDstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getPostAddressObjectName())){
            postDstRefIpAddressObjectGroup = new String[]{policyDTO.getPostAddressObjectName()};
        }

        String[] dstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getDstAddressObjectName())){
            dstRefIpAddressObjectGroup = new String[]{policyDTO.getDstAddressObjectName()};
        }
        List<String> existServiceNameList = policyDTO.getExistServiceNameList();
        String[]  refServiceObject = null;
        if(!CollectionUtils.isEmpty(existServiceNameList)){
            refServiceObject = existServiceNameList.toArray(new String[0]);
        }
        String[] refServiceObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getServiceObjectName())){
            refServiceObjectGroup = new String[]{policyDTO.getServiceObjectName()};
        }
        try {
            cmd = generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null)
                    +generatorBean.generateDNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),policyDTO.getCurrentId(),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,null,srcIpAddressParamDTO,dstIpAddressParamDTO,serviceParamDTOS,postDstIpAddressParamDTO,null,null,null,
                    srcIntf,dstIntf,srcRefIpAddressObject,srcRefIpAddressObjectGroup,null,dstRefIpAddressObjectGroup,
                    refServiceObject,refServiceObjectGroup,postDstRefIpAddressObject,postDstRefIpAddressObjectGroup,map,null)
                    +generatorBean.generatePostCommandline(null,null);
        } catch (Exception e) {
            cmd = "启明星辰dnat生成错误";
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        Map<String,Object> map = new HashMap();
        map.put("srcIpSystem",policyDTO.getSrcIpSystem());
        map.put("dstIpSystem",policyDTO.getDstIpSystem());
        map.put("postIpSystem",policyDTO.getPostDstIpSystem());
        String cmd = "";
        String srcString = policyDTO.getRestSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);

        String dstString = policyDTO.getRestDstAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO dstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstString);

        String postDstString = policyDTO.getRestPostDstAddressList().stream().collect(Collectors.joining(","));
        String postSrcString = policyDTO.getRestPostSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO postSrcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postSrcString);
        IpAddressParamDTO postDstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postDstString);

        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
        List<ServiceDTO> serviceList = policyDTO.getRestServiceList();
        if(!CollectionUtils.isEmpty(serviceList)){
            for (ServiceDTO serviceDTO : serviceList) {
                List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                serviceParamDTOList.addAll(c);
            }
        }
        ServiceParamDTO[] serviceParamDTOS =serviceParamDTOList.toArray(new ServiceParamDTO[serviceParamDTOList.size()]);
        String postPort = policyDTO.getPostPort();
        if(!StringUtils.isBlank(postPort)){
            String[] split = postPort.split(",");
            if(split[0].contains("-")){
                split[0] = String.format("%s %s",split[0].split("-")[0],split[0].split("-")[1]);
            }
            map.put("postPort",split[0]);
        }

        InterfaceParamDTO srcIntf = new InterfaceParamDTO(policyDTO.getSrcItf());
        InterfaceParamDTO dstIntf = new InterfaceParamDTO(policyDTO.getDstItf());

        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        String[] srcRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existSrcAddressList)){
            srcRefIpAddressObject =existSrcAddressList.toArray(new String[0]);
        }
        String[] srcRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getSrcAddressObjectName())){
            srcRefIpAddressObjectGroup = new String[]{policyDTO.getSrcAddressObjectName()};
        }

        List<String> postExistSrcAddressList = policyDTO.getExistPostSrcAddressList();
        String[] postSrcRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(postExistSrcAddressList)){
            postSrcRefIpAddressObject =  postExistSrcAddressList.toArray(new String[0]);
        }
        String[] postSrcRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getPostDstAddressObjectName())){
            postSrcRefIpAddressObjectGroup = new String[]{policyDTO.getPostDstAddressObjectName()};
        }

        List<String> postExistDstAddressList = policyDTO.getExistPostDstAddressList();
        String[] postDstRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(postExistDstAddressList)){
            postDstRefIpAddressObject =  postExistDstAddressList.toArray(new String[0]);
        }
        String[] postDstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getPostDstAddressObjectName())){
            postDstRefIpAddressObjectGroup = new String[]{policyDTO.getPostDstAddressObjectName()};
        }

        String[] dstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getDstAddressObjectName())){
            dstRefIpAddressObjectGroup = new String[]{policyDTO.getDstAddressObjectName()};
        }
        List<String> existServiceNameList = policyDTO.getExistServiceNameList();
        String[]  refServiceObject = null;
        if(!CollectionUtils.isEmpty(existServiceNameList)){
            refServiceObject = existServiceNameList.toArray(new String[0]);
        }
        String[] refServiceObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getServiceObjectName())){
            refServiceObjectGroup = new String[]{policyDTO.getServiceObjectName()};
        }
        try {
            cmd = generatorBean.generatePreCommandline(policyDTO.isVsys(),policyDTO.getVsysName(),null,null)
                    +generatorBean.generateBothNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),policyDTO.getCurrentId(),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,null,srcIpAddressParamDTO,dstIpAddressParamDTO,serviceParamDTOS,postSrcIpAddressParamDTO,postDstIpAddressParamDTO,null,null,null,
                    srcIntf,dstIntf,null,srcRefIpAddressObject,srcRefIpAddressObjectGroup,null,dstRefIpAddressObjectGroup,
                    refServiceObject,refServiceObjectGroup,postSrcRefIpAddressObject,postSrcRefIpAddressObjectGroup,postDstRefIpAddressObject,postDstRefIpAddressObjectGroup,map,null)
                    +generatorBean.generatePostCommandline(null,null);
        } catch (Exception e) {
            cmd = "启明星辰dnat生成错误";
            log.error("",e);
        }
        return cmd;
    }
}
