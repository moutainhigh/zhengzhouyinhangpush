package com.abtnetworks.totems.common.atomcommandline.nat;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.ArrayUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.vender.leadSecPowerV.nat.NatLeadSecPowerV30Impl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AtomLeadSecPowerV30Nat implements NatPolicyGenerator {
    private NatLeadSecPowerV30Impl generatorBean;

    public AtomLeadSecPowerV30Nat() {
        generatorBean = new NatLeadSecPowerV30Impl();
    }
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        String globalAddress = policyDTO.getGlobalAddress();
        if(globalAddress.contains(",") || !IpUtils.isIP(globalAddress)){
            return "网御V3.0外网地址仅支持单IP";
        }
        String cmd = "";
        IpAddressParamDTO globalIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(globalAddress);
        IpAddressParamDTO insideIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(policyDTO.getInsideAddress());
        if(ObjectUtils.isNotEmpty(insideIpAddressParamDTO)){
            if((insideIpAddressParamDTO.getRangIpArray()!=null && insideIpAddressParamDTO.getRangIpArray().length>0) ||
                    (insideIpAddressParamDTO.getSubnetIntIpArray()!=null && insideIpAddressParamDTO.getSubnetIntIpArray().length>0) ||
                    (insideIpAddressParamDTO.getSubnetStrIpArray()!=null && insideIpAddressParamDTO.getSubnetStrIpArray().length>0) ||
                    (insideIpAddressParamDTO.getHosts()!=null && insideIpAddressParamDTO.getHosts().length>0)){
                return "转换后地址必须为服务器地址，由诺干单IP组成";
            }
        }
        String[] insideRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getInsideAddressName())){
                insideRefIpAddressObjectGroup = new String[]{policyDTO.getInsideAddressName()};
        }
        InterfaceParamDTO inIntf = new InterfaceParamDTO(policyDTO.getInDevItf());
        InterfaceParamDTO outIntf = new InterfaceParamDTO(policyDTO.getOutDevItf());
        ZoneParamDTO srcZone = new ZoneParamDTO(policyDTO.getFromZone());
        ZoneParamDTO dstZone = new ZoneParamDTO(policyDTO.getToZone());

        try {
            cmd = generatorBean.generateStaticNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),String.valueOf(policyDTO.getTaskId()),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,null,insideIpAddressParamDTO,globalIpAddressParamDTO,null,null,srcZone,dstZone,inIntf,outIntf,
                    null,insideRefIpAddressObjectGroup,null,null,null,null);
        } catch (Exception e) {
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        Map<String,Object> map = new HashMap<>();
        String cmd = "";
        String srcString = policyDTO.getRestSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);
        String dstString = policyDTO.getRestDstAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO dstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstString);
        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
        //服务
        List<ServiceDTO> serviceList = policyDTO.getServiceList();
        if(!CollectionUtils.isEmpty(serviceList)){
            for (ServiceDTO serviceDTO : serviceList) {
                List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                serviceParamDTOList.addAll(c);
            }
        }
        ServiceParamDTO[] serviceParamDTOS =serviceParamDTOList.toArray(new ServiceParamDTO[serviceParamDTOList.size()]);

        //服务（还需创建）
        List<ServiceParamDTO> restServiceParamDTOList = new ArrayList<>();
        List<ServiceDTO> restServiceList = policyDTO.getRestServiceList();
        for (ServiceDTO serviceDTO : restServiceList) {
            List<ServiceParamDTO> c = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
            restServiceParamDTOList.addAll(c);
        }
        ServiceParamDTO[] restServiceParamDTOS =restServiceParamDTOList.toArray(new ServiceParamDTO[restServiceParamDTOList.size()]);
        map.put("restServiceParamDTOS",restServiceParamDTOS);

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

        List<String> existServiceNameList = policyDTO.getExistServiceNameList();
        String[]  refServiceObject = null;
        if(!CollectionUtils.isEmpty(existServiceNameList)){
            refServiceObject = existServiceNameList.toArray(new String[0]);
        }
        String[] refServiceObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getServiceObjectName())){
            refServiceObjectGroup = new String[]{policyDTO.getServiceObjectName()};
        }
        //网域Snat转换后源地址对象定义（地址池，不要复用，仅能使用 IP 地址段）
        String postIpAddress = policyDTO.getPostIpAddress();
        IpAddressParamDTO postIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postIpAddress);
        if(ObjectUtils.isNotEmpty(postIpAddressParamDTO)){
            if((postIpAddressParamDTO.getSingleIpArray()!=null && postIpAddressParamDTO.getSingleIpArray().length>0) ||
                    (postIpAddressParamDTO.getSubnetIntIpArray()!=null && postIpAddressParamDTO.getSubnetIntIpArray().length>0) ||
                    (postIpAddressParamDTO.getSubnetStrIpArray()!=null && postIpAddressParamDTO.getSubnetStrIpArray().length>0) ||
                    (postIpAddressParamDTO.getHosts()!=null && postIpAddressParamDTO.getHosts().length>0)){
                return "转换后源地址必须为IP范围";
            }
        }
        try {
            cmd = generatorBean.generateSNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),String.valueOf(policyDTO.getTaskId()),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,policyDTO.getSwapRuleNameId(),srcIpAddressParamDTO, dstIpAddressParamDTO,serviceParamDTOS,postIpAddressParamDTO,
                    srcZone,dstZone,srcIntf, dstIntf,null,srcRefIpAddressObject,srcRefIpAddressObjectGroup,
                    dstRefIpAddressObject,dstRefIpAddressObjectGroup, refServiceObject,refServiceObjectGroup,null,null,
                    map,null);
        } catch (Exception e) {
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        Map<String,Object> map = new HashMap<>();
        String cmd = "";
        String srcString = policyDTO.getRestSrcAddressList().stream().collect(Collectors.joining(","));
        IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcString);
        //目的ip仅支持单ip
        IpAddressParamDTO dstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(policyDTO.getDstIp());
        if(ObjectUtils.isNotEmpty(dstIpAddressParamDTO)){
            if((dstIpAddressParamDTO.getRangIpArray()!=null && dstIpAddressParamDTO.getRangIpArray().length>0) ||
                    (dstIpAddressParamDTO.getSubnetIntIpArray()!=null && dstIpAddressParamDTO.getSubnetIntIpArray().length>0) ||
                    (dstIpAddressParamDTO.getSubnetStrIpArray()!=null && dstIpAddressParamDTO.getSubnetStrIpArray().length>0) ||
                    (dstIpAddressParamDTO.getHosts()!=null && dstIpAddressParamDTO.getHosts().length>0) ||
            (dstIpAddressParamDTO.getSingleIpArray()!=null && dstIpAddressParamDTO.getSingleIpArray().length>1)){
                return "网御DNAT 目的IP只支持 单IP";
            }
        }
        //网御转换后只能是 服务器地址（诺干单ip组成）
        String postDstString = policyDTO.getPostIpAddress();
        IpAddressParamDTO postDstIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postDstString);
        if(ObjectUtils.isNotEmpty(postDstIpAddressParamDTO)){
            if((postDstIpAddressParamDTO.getRangIpArray()!=null && postDstIpAddressParamDTO.getRangIpArray().length>0) ||
                    (postDstIpAddressParamDTO.getSubnetIntIpArray()!=null && postDstIpAddressParamDTO.getSubnetIntIpArray().length>0) ||
                    (postDstIpAddressParamDTO.getSubnetStrIpArray()!=null && postDstIpAddressParamDTO.getSubnetStrIpArray().length>0) ||
                    (postDstIpAddressParamDTO.getHosts()!=null && postDstIpAddressParamDTO.getHosts().length>0)){
                return "转换后地址必须为服务器地址，由诺干单IP组成";
            }
        }
        //服务暂时不离散
        //转换前服务
        boolean noPostService = true;
        String[] postPort = null;
        if(!StringUtils.isBlank(policyDTO.getPostPort()) && !"any".equalsIgnoreCase(policyDTO.getPostPort())){
             postPort = policyDTO.getPostPort().split(",");
             noPostService = false;
        }
        int n =0;
        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
        List<ServiceParamDTO> postServiceParamDTOList = new ArrayList<>();
        List<ServiceDTO> serviceList = policyDTO.getServiceList();
        if(!CollectionUtils.isEmpty(serviceList)){
            for (ServiceDTO serviceDTO : serviceList) {
                List<ServiceParamDTO> service = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                serviceParamDTOList.addAll(service);
                if(!noPostService){
                    serviceDTO.setDstPorts(postPort[n]);
                    n++;
                    List<ServiceParamDTO> postService = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                    postServiceParamDTOList.addAll(postService);
                }
            }
        }
        ServiceParamDTO[] serviceParamDTOS =serviceParamDTOList.toArray(new ServiceParamDTO[serviceParamDTOList.size()]);
        ServiceParamDTO[] postServiceParamDTOS =postServiceParamDTOList.toArray(new ServiceParamDTO[postServiceParamDTOList.size()]);


        List<ServiceParamDTO> restServiceParamDTOList = new ArrayList<>();
        List<ServiceDTO> restServiceList = policyDTO.getRestServiceList();
        if(!CollectionUtils.isEmpty(restServiceList)){
            for (ServiceDTO serviceDTO : restServiceList) {
                List<ServiceParamDTO> service = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, true);
                restServiceParamDTOList.addAll(service);
            }
        }
        ServiceParamDTO[] restServiceParamDTOS =restServiceParamDTOList.toArray(new ServiceParamDTO[restServiceParamDTOList.size()]);
        map.put("restServiceParamDTOS",restServiceParamDTOS);

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


        //转换后
        List<String> existPostDstAddressList = policyDTO.getExistPostDstAddressList();
        String[] postDstRefIpAddressObject = null;
        if(!CollectionUtils.isEmpty(existPostDstAddressList)){
            postDstRefIpAddressObject =  existPostDstAddressList.toArray(new String[0]);
        }
        String[] postDstRefIpAddressObjectGroup = null;
        if(!StringUtils.isBlank(policyDTO.getPostAddressObjectName())){
            postDstRefIpAddressObjectGroup = new String[]{policyDTO.getPostAddressObjectName()};
        }

        //转换前服务对象
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
            cmd = generatorBean.generateDNatPolicyCommandLine(StatusTypeEnum.ADD,null,policyDTO.getTheme(),String.valueOf(policyDTO.getTaskId()),null,
                    policyDTO.getDescription(),null,null,null, null
                    ,null,srcIpAddressParamDTO,dstIpAddressParamDTO,serviceParamDTOS,postDstIpAddressParamDTO,postServiceParamDTOS,srcZone,dstZone,
                    srcIntf,dstIntf,srcRefIpAddressObject,srcRefIpAddressObjectGroup,null,null,
                    refServiceObject,refServiceObjectGroup,postDstRefIpAddressObject,postDstRefIpAddressObjectGroup,map,null);
        } catch (Exception e) {
            log.error("",e);
        }
        return cmd;
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        return null;
    }

    public static void main(String[] args) {
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        List<String> n = new ArrayList<>();
        n.add("aaaaaaaaaa");
        sNatPolicyDTO.setExistSrcAddressList(n);
        sNatPolicyDTO.setExistDstAddressList(n);
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("90-99");
        serviceDTO.setSrcPorts("any");

        sNatPolicyDTO.setServiceObjectName("serviceName1");

        sNatPolicyDTO.setTheme("test");
        sNatPolicyDTO.setPostIpAddress("5.5.5.5-5.5.5.10");
        sNatPolicyDTO.setSrcItf("接口");
        AtomLeadSecPowerV30Nat leadSecPowerV30Nat = new AtomLeadSecPowerV30Nat();
        System.out.println(leadSecPowerV30Nat.generateSNatCommandLine(sNatPolicyDTO));
    }
}
