package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: WangCan
 * @Description common调用command-line 参数转换工具类
 * @Date: 2021/6/22
 */
@Slf4j
public class Param4CommandLineUtils {

    /**
     * 指定ip类型
     * @param ipAddress
     * @return
     */
    public static IpAddressParamDTO buildIpAddressParamDTO(String ipAddress) {
        IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
        if(StringUtils.isBlank(ipAddress)){
            return ipAddressParamDTO;
        }
        if(IP6Utils.isIPv6(ipAddress)){
            ipAddressParamDTO.setIpTypeEnum(RuleIPTypeEnum.IP6);
        }else{
            ipAddressParamDTO.setIpTypeEnum(RuleIPTypeEnum.IP4);
        }
        List<String> ipList = Arrays.asList(ipAddress.split(","));
        List<String> singleIpList = new ArrayList<>();
        List<IpAddressRangeDTO> rangIpList = new ArrayList<>();
        List<IpAddressSubnetIntDTO> subnetIntIpList = new ArrayList<>();
        List<String> hosts = new ArrayList<>();
        for (String address : ipList) {
            if(IpUtils.isIPRange(address)) {
                IpAddressRangeDTO ipAddressRangeDTO = new IpAddressRangeDTO();
                ipAddressRangeDTO.setStart(IpUtils.getStartIpFromRange(address));
                ipAddressRangeDTO.setEnd(IpUtils.getEndIpFromRange(address));
                rangIpList.add(ipAddressRangeDTO);
            } else if (IpUtils.isIPSegment(address)) {
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                ipAddressSubnetIntDTO.setIp(IpUtils.getIpFromIpSegment(address));
                ipAddressSubnetIntDTO.setMask(Integer.parseInt(IpUtils.getMaskBitFromIpSegment(address)));
                subnetIntIpList.add(ipAddressSubnetIntDTO);
            } else if (IpUtils.isIP(address)) {
                singleIpList.add(address);
            } else if (address.contains(":")) {
                if (address.contains("-")) {
                    IpAddressRangeDTO ipAddressRangeDTO = new IpAddressRangeDTO();
                    ipAddressRangeDTO.setStart(IpUtils.getStartIpFromRange(address));
                    ipAddressRangeDTO.setEnd(IpUtils.getEndIpFromRange(address));
                    rangIpList.add(ipAddressRangeDTO);
                } else if (address.contains("/")) {
                    IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                    ipAddressSubnetIntDTO.setIp(IpUtils.getIpFromIpSegment(address));
                    ipAddressSubnetIntDTO.setMask(Integer.parseInt(IpUtils.getMaskBitFromIpSegment(address)));
                    subnetIntIpList.add(ipAddressSubnetIntDTO);
                } else {
                    singleIpList.add(address);
                }
            } else {
                hosts.add(address);
//                log.error("错误的ip[{}]",address);
//                throw new IllegalArgumentException("ip格式不规范：" + address);
            }
        }
        ipAddressParamDTO.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
        ipAddressParamDTO.setRangIpArray(rangIpList.toArray(new IpAddressRangeDTO[rangIpList.size()]));
        ipAddressParamDTO.setSubnetIntIpArray(subnetIntIpList.toArray(new IpAddressSubnetIntDTO[subnetIntIpList.size()]));
        ipAddressParamDTO.setHosts(hosts.toArray(new String[0]));
        return ipAddressParamDTO;
    }

    public static List<ServiceParamDTO> buildServiceParamDTO(ServiceDTO serviceDTO, Boolean needDstPortOnly) {
        ProtocolTypeEnum protocol = null;
        List<ServiceParamDTO> serviceDTOList = new ArrayList<>();

        List<String> protocolAttachTypeList = new ArrayList<>();
        List<String> protocolAttachCodeList = new ArrayList<>();
        List<Integer> srcSinglePortList = new ArrayList<>();
        List<String> srcSinglePortStrList = new ArrayList<>();
        List<PortRangeDTO>srcRangePortList = new ArrayList<>();
        List<Integer> dstSinglePortList = new ArrayList<>();
        List<String> dstSinglePortStrList = new ArrayList<>();
        List<PortRangeDTO> dstRangePortList = new ArrayList<>();

        int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            protocol = ProtocolTypeEnum.ANY;
            ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
            serviceParamDTO.setProtocol(protocol);
            serviceDTOList.add(serviceParamDTO);
            return serviceDTOList;
        }

        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            protocol = ProtocolTypeEnum.ICMP;
            if(StringUtils.isNotBlank(serviceDTO.getType())) {
                protocolAttachTypeList.add(serviceDTO.getType());
            }
            if (StringUtils.isNotBlank(serviceDTO.getCode())) {
                protocolAttachCodeList.add(serviceDTO.getCode());
            }
        } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                protocol = ProtocolTypeEnum.TCP;
            } else {
                protocol = ProtocolTypeEnum.UDP;
            }
            if (StringUtils.isNotBlank(serviceDTO.getSrcPorts()) && !serviceDTO.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String[] srcPorts = serviceDTO.getSrcPorts().split(",");
                //源端口有值，目的端口any，则仅显示源端口
                for (String srcPort : srcPorts) {
                    if (PortUtils.isPortRange(srcPort)) {
                        String startPort = PortUtils.getStartPort(srcPort);
                        String endPort = PortUtils.getEndPort(srcPort);
                        PortRangeDTO portRangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                        srcRangePortList.add(portRangeDTO);
                    } else {
                        if (StringUtils.isNumeric(srcPort)) {
                            srcSinglePortList.add(Integer.parseInt(srcPort));
                        } else {
                            srcSinglePortStrList.add(srcPort);
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(serviceDTO.getDstPorts()) && !serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String[] dstPorts = serviceDTO.getDstPorts().split(",");
                for (String dstPort : dstPorts) {
                    if (PortUtils.isPortRange(dstPort)) {
                        String startPort = PortUtils.getStartPort(dstPort);
                        String endPort = PortUtils.getEndPort(dstPort);
                        PortRangeDTO portRangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                        dstRangePortList.add(portRangeDTO);
                    } else {
                        if (StringUtils.isNumeric(dstPort)) {
                            dstSinglePortList.add(Integer.parseInt(dstPort));
                        } else {
                            dstSinglePortStrList.add(dstPort);
                        }
                    }
                }
            }
        } else {
            protocol = ProtocolTypeEnum.PROTOCOL;
        }

        if(needDstPortOnly != null && needDstPortOnly == true){
            if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                for (Integer dstPort : dstSinglePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                for (String dstPortStr : dstSinglePortStrList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstRangePortList)){
                for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isEmpty(dstSinglePortList) && CollectionUtils.isEmpty(dstRangePortList) && CollectionUtils.isEmpty(dstSinglePortStrList)){
                ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                serviceParamDTO.setProtocol(protocol);
                serviceDTOList.add(serviceParamDTO);
            }
            return serviceDTOList;
        }

        if(CollectionUtils.isNotEmpty(srcSinglePortList)){
            for (Integer srcPort : srcSinglePortList) {
                boolean hasDst = false;
                if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                    hasDst = true;
                    for (Integer dstPort : dstSinglePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                        serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                    hasDst = true;
                    for (String dstPortStr : dstSinglePortStrList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                        serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstRangePortList)){
                    hasDst = true;
                    for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                        serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if (!hasDst){
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(srcSinglePortStrList)){
            for (String srcPort : srcSinglePortStrList) {
                boolean hasDst = false;
                if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                    hasDst = true;
                    for (Integer dstPort : dstSinglePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                        serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                    hasDst = true;
                    for (String dstPortStr : dstSinglePortStrList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                        serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstRangePortList)){
                    hasDst = true;
                    for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                        serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if (!hasDst){
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(srcRangePortList)){
            for (PortRangeDTO srcPort : srcRangePortList) {
                boolean hasDst = false;
                if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                    hasDst = true;
                    for (Integer dstPort : dstSinglePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                        serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                    hasDst = true;
                    for (String dstPortStr : dstSinglePortStrList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                        serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstRangePortList)){
                    hasDst = true;
                    for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                        serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if (!hasDst){
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
        }
        if(CollectionUtils.isEmpty(srcSinglePortList) && CollectionUtils.isEmpty(srcSinglePortStrList) && CollectionUtils.isEmpty(srcRangePortList)){
            boolean hasDst = false;
            if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                hasDst = true;
                for (Integer dstPort : dstSinglePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                hasDst = true;
                for (String dstPortStr : dstSinglePortStrList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstRangePortList)){
                hasDst = true;
                for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if (!hasDst){
                ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                serviceParamDTO.setProtocol(protocol);
                serviceDTOList.add(serviceParamDTO);
            }
        }
        return serviceDTOList;
    }



    public static List<ServiceParamDTO> buildServiceParamDTO(ServiceDTO serviceDTO,Boolean needDstPortOnly,Map<String, Object> map) {
        ProtocolTypeEnum protocol = null;
        List<String> protocolAttachTypeList = new ArrayList<>();
        List<String> protocolAttachCodeList = new ArrayList<>();
        List<Integer> srcSinglePortList = new ArrayList<>();
        List<String> srcSinglePortStrList = new ArrayList<>();
        List<PortRangeDTO>srcRangePortList = new ArrayList<>();
        List<Integer> dstSinglePortList = new ArrayList<>();
        List<String> dstSinglePortStrList = new ArrayList<>();
        List<PortRangeDTO> dstRangePortList = new ArrayList<>();

        int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            protocol = ProtocolTypeEnum.ANY;

        }

        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            protocol = ProtocolTypeEnum.ICMP;
            if(StringUtils.isNotBlank(serviceDTO.getType())) {
                protocolAttachTypeList.add(serviceDTO.getType());
            }
            if (StringUtils.isNotBlank(serviceDTO.getCode())) {
                protocolAttachCodeList.add(serviceDTO.getCode());
            }
        } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                protocol = ProtocolTypeEnum.TCP;
            } else {
                protocol = ProtocolTypeEnum.UDP;
            }
            String[] srcPorts = serviceDTO.getSrcPorts().split(",");
            String[] dstPorts = serviceDTO.getDstPorts().split(",");
            if (StringUtils.isNotBlank(serviceDTO.getSrcPorts()) && !serviceDTO.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                //源端口有值，目的端口any，则仅显示源端口
                for (String srcPort : srcPorts) {
                    if (PortUtils.isPortRange(srcPort)) {
                        String startPort = PortUtils.getStartPort(srcPort);
                        String endPort = PortUtils.getEndPort(srcPort);
                        PortRangeDTO portRangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                        srcRangePortList.add(portRangeDTO);
                    } else {
                        if (StringUtils.isNumeric(srcPort)) {
                            srcSinglePortList.add(Integer.parseInt(srcPort));
                        } else {
                            srcSinglePortStrList.add(srcPort);
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(serviceDTO.getDstPorts()) && !serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                for (String dstPort : dstPorts) {
                    if (PortUtils.isPortRange(dstPort)) {
                        String startPort = PortUtils.getStartPort(dstPort);
                        String endPort = PortUtils.getEndPort(dstPort);
                        PortRangeDTO portRangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                        dstRangePortList.add(portRangeDTO);
                    } else {
                        if (StringUtils.isNumeric(dstPort)) {
                            dstSinglePortList.add(Integer.parseInt(dstPort));
                        } else {
                            dstSinglePortStrList.add(dstPort);
                        }
                    }
                }
            }
        } else {
            protocol = ProtocolTypeEnum.ANY;
            return new ArrayList<>();
        }
        List<ServiceParamDTO> serviceDTOList = new ArrayList<>();
        if(needDstPortOnly!= null && needDstPortOnly == true){
            if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                for (Integer dstPort : dstSinglePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                for (String dstPortStr : dstSinglePortStrList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstRangePortList)){
                for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isEmpty(dstSinglePortList) && CollectionUtils.isEmpty(dstRangePortList) && CollectionUtils.isEmpty(dstSinglePortStrList)){
                ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                serviceParamDTO.setProtocol(protocol);
                serviceDTOList.add(serviceParamDTO);
            }
            return serviceDTOList;
        }

        if(CollectionUtils.isNotEmpty(srcSinglePortList)){
            for (Integer srcPort : srcSinglePortList) {
                boolean hasDst = false;
                if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                    hasDst = true;
                    for (Integer dstPort : dstSinglePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                        serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                    hasDst = true;
                    for (String dstPortStr : dstSinglePortStrList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                        serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstRangePortList)){
                    hasDst = true;
                    for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                        serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if (!hasDst){
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setSrcSinglePortArray(new Integer[]{srcPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(srcSinglePortStrList)){
            for (String srcPort : srcSinglePortStrList) {
                boolean hasDst = false;
                if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                    hasDst = true;
                    for (Integer dstPort : dstSinglePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                        serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                    hasDst = true;
                    for (String dstPortStr : dstSinglePortStrList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                        serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstRangePortList)){
                    hasDst = true;
                    for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                        serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if (!hasDst){
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setSrcSinglePortStrArray(new String[]{srcPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(srcRangePortList)){
            for (PortRangeDTO srcPort : srcRangePortList) {
                boolean hasDst = false;
                if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                    hasDst = true;
                    for (Integer dstPort : dstSinglePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                        serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                    hasDst = true;
                    for (String dstPortStr : dstSinglePortStrList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                        serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if(CollectionUtils.isNotEmpty(dstRangePortList)){
                    hasDst = true;
                    for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(protocol);
                        serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                        serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                        serviceDTOList.add(serviceParamDTO);
                    }
                }
                if (!hasDst){
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setSrcRangePortArray(new PortRangeDTO[]{srcPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
        }
        if(CollectionUtils.isEmpty(srcSinglePortList) && CollectionUtils.isEmpty(srcSinglePortStrList) && CollectionUtils.isEmpty(srcRangePortList)){
            boolean hasDst = false;
            if(CollectionUtils.isNotEmpty(dstSinglePortList)){
                hasDst = true;
                for (Integer dstPort : dstSinglePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortArray(new Integer[]{dstPort});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstSinglePortStrList)){
                hasDst = true;
                for (String dstPortStr : dstSinglePortStrList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstSinglePortStrArray(new String[]{dstPortStr});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if(CollectionUtils.isNotEmpty(dstRangePortList)){
                hasDst = true;
                for (PortRangeDTO dstPortRangeDTO : dstRangePortList) {
                    ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                    serviceParamDTO.setProtocol(protocol);
                    serviceParamDTO.setDstRangePortArray(new PortRangeDTO[]{dstPortRangeDTO});
                    serviceDTOList.add(serviceParamDTO);
                }
            }
            if (!hasDst){
                ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                serviceParamDTO.setProtocol(protocol);
                serviceDTOList.add(serviceParamDTO);
            }
        }
        return serviceDTOList;
    }

    /**
     * 构建nat策略转换后源地址阐述
     * @param postAddressObjectName
     * @param postIpAddress
     * @param postSrcIpSystem
     * @param sb
     * @param natPolicyParamDTO
     * @param generatorBean
     */
    public static void buildNatParamPostSrcIp(String postAddressObjectName, String postIpAddress, String postSrcIpSystem, StringBuilder sb, NatPolicyParamDTO natPolicyParamDTO, OverAllGeneratorAbstractBean generatorBean,boolean createObjFlag) {
        if (StringUtils.isNotEmpty(postIpAddress) && createObjFlag){
            String postSrcIpName = null;
            IpAddressParamDTO postSrcIpParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postIpAddress);
            if(StringUtils.isNotBlank(postSrcIpSystem) && (postSrcIpParamDTO.getSingleIpArray().length+postSrcIpParamDTO.getRangIpArray().length)==1){
                postSrcIpName = postSrcIpSystem;
            } else {//创建的地址对象名称有可能一个也有可能多个
                postSrcIpName = generatorBean.createIpAddressObjectNameByParamDTO(postSrcIpParamDTO.getSingleIpArray(), postSrcIpParamDTO.getRangIpArray(), postSrcIpParamDTO.getSubnetIntIpArray(), postSrcIpParamDTO.getSubnetStrIpArray(), postSrcIpParamDTO.getHosts(), null, null,null);
            }


            if (StringUtils.isNotEmpty(postAddressObjectName)){
                postAddressObjectName = postSrcIpSystem + "," + postAddressObjectName;
            }

            String[] strings = postAddressObjectName.split(",");
            natPolicyParamDTO.setPostSrcIpAddress(postSrcIpParamDTO);

            natPolicyParamDTO.setSrcIp(postSrcIpParamDTO);
        }else {
        }

    }

    /**
     * 构建nat策略转换后目的地址阐述
     * @param postAddressObjectName
     * @param postIpAddress
     * @param postDstIpSystem
     * @param sb
     * @param natPolicyParamDTO
     * @param generatorBean
     */
    public static void buildNatParamPostDstIp(String postAddressObjectName,String postIpAddress,String postDstIpSystem, StringBuilder sb, NatPolicyParamDTO natPolicyParamDTO,OverAllGeneratorAbstractBean generatorBean) {
        if(StringUtils.isNotBlank(postAddressObjectName)){
            // 地址复用
            natPolicyParamDTO.setPostDstRefIpAddressObject(new String[]{postAddressObjectName});
        } else {
            String postDstIpName = null;
            IpAddressParamDTO postDstIpParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(postIpAddress);
            if(StringUtils.isNotBlank(postDstIpSystem)){
                // 使用页面输入的地址对象名生成地址对象
                postDstIpName = postDstIpSystem;
                natPolicyParamDTO.setPostDstRefIpAddressObject(new String[]{postDstIpName});
                RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                if(postIpAddress.contains(":")){
                    ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                }
                try {
                    sb.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,postDstIpName,null,postDstIpParamDTO.getSingleIpArray(),postDstIpParamDTO.getRangIpArray()
                            ,postDstIpParamDTO.getSubnetIntIpArray(),postDstIpParamDTO.getSubnetStrIpArray(),null,postDstIpParamDTO.getHosts(),null,null,null,null,null,null));
                } catch (Exception e) {
                    log.error("原子化命令行生成地址对象异常",e);
                }
            }
            if(StringUtils.isNotBlank(postIpAddress)){
                natPolicyParamDTO.setPostDstIpAddress(postDstIpParamDTO);
            }
        }
    }

    /**
     * 构建nat策略目的地址参数
     * @param dstAddressObjectName
     * @param dstIp
     * @param dstIpSystem
     * @param sb
     * @param natPolicyParamDTO
     * @param generatorBean
     */
    public static void buildNatParamDstIp(String dstAddressObjectName,String dstIp,String dstIpSystem, StringBuilder sb, NatPolicyParamDTO natPolicyParamDTO,OverAllGeneratorAbstractBean generatorBean,boolean createObjFlag) {
        List<String> refAddressObjectNames = new ArrayList<>();
        if (StringUtils.isNotEmpty(dstIp) && createObjFlag){
            String dstIpName = null;
            IpAddressParamDTO dstIpParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dstIp);
            if(StringUtils.isNotEmpty(dstIpSystem) && (dstIpParamDTO.getSingleIpArray().length+dstIpParamDTO.getRangIpArray().length)==1){
                refAddressObjectNames.add(dstIpSystem);
                dstIpName = dstIpSystem;
            } else {//创建的地址对象名称有可能一个也有可能多个
                dstIpName = generatorBean.createIpAddressObjectNameByParamDTO(dstIpParamDTO.getSingleIpArray(), dstIpParamDTO.getRangIpArray(), dstIpParamDTO.getSubnetIntIpArray(), dstIpParamDTO.getSubnetStrIpArray(), dstIpParamDTO.getHosts(), null, null,null);
            }


            if (StringUtils.isNotEmpty(dstAddressObjectName)){
                dstIpName = dstIpName + "," + dstAddressObjectName;
            }

            String[] strings = dstIpName.split(",");
            natPolicyParamDTO.setDstRefIpAddressObject(strings);

            RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
            if(dstIp.contains(":")){
                ruleIPTypeEnum = RuleIPTypeEnum.IP6;
            }
            try {
                sb.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,dstIpName,null,dstIpParamDTO.getSingleIpArray(),dstIpParamDTO.getRangIpArray()
                        ,dstIpParamDTO.getSubnetIntIpArray(),dstIpParamDTO.getSubnetStrIpArray(),null,dstIpParamDTO.getHosts(),null,null,null,null,null,null));
            } catch (Exception e) {
                log.error("原子化命令行生成地址对象异常",e);
            }
            natPolicyParamDTO.setDstIp(dstIpParamDTO);
        }else {
            if (StringUtils.isNotEmpty(dstAddressObjectName)){
                String[] strings = dstAddressObjectName.split(",");
                natPolicyParamDTO.setDstRefIpAddressObject(strings);
            }
        }

    }

    /**
     * 构建nat策略源地址参数
     * @param srcAddressObjectName
     * @param srcIp
     * @param srcIpSystem
     * @param sb
     * @param natPolicyParamDTO
     * @param generatorBean
     */
    public static List<String> buildNatParamSrcIp(String srcAddressObjectName,String srcIp,String srcIpSystem, StringBuilder sb, NatPolicyParamDTO natPolicyParamDTO,OverAllGeneratorAbstractBean generatorBean,boolean createObjFlag) {
        List<String> refAddressObjectNames = new ArrayList<>();
        if (StringUtils.isNotEmpty(srcIp) && createObjFlag){
            String srcIpName = null;
            IpAddressParamDTO srcIpParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(srcIp);
            if(StringUtils.isNotBlank(srcIpSystem) && (srcIpParamDTO.getSingleIpArray().length+srcIpParamDTO.getRangIpArray().length)==1){
                refAddressObjectNames.add(srcIpSystem);
                srcIpName = srcIpSystem;
            } else {//创建的地址对象名称有可能一个也有可能多个
                srcIpName = generatorBean.createIpAddressObjectNameByParamDTO(srcIpParamDTO.getSingleIpArray(), srcIpParamDTO.getRangIpArray(), srcIpParamDTO.getSubnetIntIpArray(), srcIpParamDTO.getSubnetStrIpArray(), srcIpParamDTO.getHosts(), null, null,null);
            }


            if (StringUtils.isNotEmpty(srcAddressObjectName)){
                srcIpName = srcIpName + "," + srcAddressObjectName;
            }

            String[] strings = srcIpName.split(",");
            natPolicyParamDTO.setSrcRefIpAddressObject(strings);

                RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                if(srcIp.contains(":")){
                    ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                }
                try {
                    sb.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,srcIpName,null,srcIpParamDTO.getSingleIpArray(),srcIpParamDTO.getRangIpArray()
                            ,srcIpParamDTO.getSubnetIntIpArray(),srcIpParamDTO.getSubnetStrIpArray(),null,srcIpParamDTO.getHosts(),null,null,null,null,null,null));
                } catch (Exception e) {
                    log.error("原子化命令行生成地址对象异常",e);
                }
                natPolicyParamDTO.setSrcIp(srcIpParamDTO);
        }else {
            if (StringUtils.isNotEmpty(srcAddressObjectName)){
                String[] strings = srcAddressObjectName.split(",");
                natPolicyParamDTO.setSrcRefIpAddressObject(strings);
            }
        }

        return refAddressObjectNames;
    }


    public static String getRefTimeName(String startTime, String endTime, StringBuilder sb,OverAllGeneratorAbstractBean generatorBean) {
        AbsoluteTimeParamDTO absoluteTimeParamDTO = new AbsoluteTimeParamDTO(startTime, endTime);
        String timeObjectName = generatorBean.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, null, null);
        try {
            sb.append(generatorBean.generateAbsoluteTimeCommandLine(timeObjectName,null,absoluteTimeParamDTO,null,null));
        } catch (Exception e) {
            log.error("原子化命令行创建时间对象异常",e);
        }
        return timeObjectName;
    }


    public static String getRefTimeName(String startTime, String endTime, StringBuilder sb, OverAllGeneratorAbstractBean generatorBean, Map map) {
        if(StringUtils.isEmpty(startTime)){
            return "always";
        }
        String timeObjectName = "always";
        AbsoluteTimeParamDTO absoluteTimeParamDTO = new AbsoluteTimeParamDTO(startTime, endTime);
        timeObjectName = generatorBean.createTimeObjectNameByAbsoluteParamDTO(absoluteTimeParamDTO,map);
        try {
            sb.append(generatorBean.generateAbsoluteTimeCommandLine(timeObjectName,null,absoluteTimeParamDTO,map,null));
            sb.append(StringUtils.LF);
        } catch (Exception e) {
            log.error("原子化命令行创建时间对象异常",e);
        }
        return timeObjectName;
    }

    public static List<String> getRefServiceNames( String exitServiceName, List<String> existServiceNameList, List<ServiceDTO> restServiceList,StringBuilder sb,OverAllGeneratorAbstractBean generatorBean) {
        List<String> serviceNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(exitServiceName)) {
            serviceNameList.add(exitServiceName);
        } else {
            if (CollectionUtils.isNotEmpty(existServiceNameList)) {
                serviceNameList.addAll(existServiceNameList);
            }
            if (CollectionUtils.isNotEmpty(restServiceList)) {
                List<ServiceParamDTO> serviceParamDTOS = new ArrayList<>();

                if (ProtocolUtils.getProtocolByString(restServiceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    serviceNameList.add("ALL");
                } else {

                    for (ServiceDTO serviceDTO : restServiceList) {
                        if (serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                            serviceNameList.add(String.format("ALL_%s", protocol.toUpperCase()));
                            continue;
                        }
                        serviceParamDTOS.addAll(Param4CommandLineUtils.buildServiceParamDTO(serviceDTO, false));
                    }
                    String serviceObjectName = generatorBean.createServiceObjectName(serviceParamDTOS, null, null);
                    if (StringUtils.isNotEmpty(serviceObjectName)) {
                        if (serviceObjectName.contains(",")) {
                            for (String tmpServiceObjectName : serviceObjectName.split(",")) {
                                serviceNameList.add(tmpServiceObjectName);
                            }
                        } else {
                            serviceNameList.add(serviceObjectName);
                        }
                    }
                    try {
                        sb.append(generatorBean.generateServiceObjectCommandLine(StatusTypeEnum.ADD, serviceObjectName, null, null, serviceParamDTOS, null, null, null));
                    } catch (Exception e) {
                        log.error("原子化命令行创建服务对象异常", e);
                    }
                }

            }
        }
        return serviceNameList;
    }



    public static List<String> getRefIpAddressNames(String exitAddressName,List<String> existAddressList,List<String> restAddresList, String ipSystem,StringBuilder sb, boolean createObjFlag,OverAllGeneratorAbstractBean generatorBean) {
        List<String> refAddressObjectNames = new ArrayList<>();
        if(StringUtils.isNotBlank(exitAddressName)){
            // 地址名整体复用
            refAddressObjectNames.add(exitAddressName);
        } else {
            if(CollectionUtils.isNotEmpty(existAddressList)){
                refAddressObjectNames.addAll(existAddressList);
            }
            if(CollectionUtils.isNotEmpty(restAddresList)){
                if(createObjFlag){
                    String restSrcIp = StringUtils.join(restAddresList, ",");
                    IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(restSrcIp);
                    RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                    if(restSrcIp.contains(":")){
                        ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                    }
                    String srcIpAddressName = null;
                    if(StringUtils.isNotBlank(ipSystem) && (srcIpAddressParamDTO.getSingleIpArray().length+srcIpAddressParamDTO.getRangIpArray().length)==1){
                        srcIpAddressName = ipSystem;
                        refAddressObjectNames.add(srcIpAddressName);
                    } else {//创建的地址对象名称有可能一个也有可能多个
                        srcIpAddressName = generatorBean.createIpAddressObjectNameByParamDTO(srcIpAddressParamDTO.getSingleIpArray(), srcIpAddressParamDTO.getRangIpArray(), srcIpAddressParamDTO.getSubnetIntIpArray(), srcIpAddressParamDTO.getSubnetStrIpArray(), srcIpAddressParamDTO.getHosts(), null, null,null);
                        if(srcIpAddressName!=null && srcIpAddressName.contains(",")){
                            String[] split = srcIpAddressName.split(",");
                            for(String tempSrcIpAddressName : split){
                                refAddressObjectNames.add(tempSrcIpAddressName);
                            }
                        }else{
                            refAddressObjectNames.add(srcIpAddressName);
                        }
                    }

                    try {
                        sb.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,srcIpAddressName,null,srcIpAddressParamDTO.getSingleIpArray(),srcIpAddressParamDTO.getRangIpArray()
                                ,srcIpAddressParamDTO.getSubnetIntIpArray(),srcIpAddressParamDTO.getSubnetStrIpArray(),null,srcIpAddressParamDTO.getHosts(),null,null,null,null,null,null));
                    } catch (Exception e) {
                        log.error("原子化命令行生成地址对象异常",e);
                    }
                } else{
                    // TODO 不创建对象,直接使用ip
                    refAddressObjectNames.addAll(restAddresList);
                }
            }
        }
        return refAddressObjectNames;
    }

    public static List<String> getAddressGroupName(String exitAddressName,List<String> existAddressList,List<String> restAddresList, String ipSystem,StringBuilder sb, boolean createObjFlag,OverAllGeneratorAbstractBean generatorBean){
        List<String> refAddressObjectNames = new ArrayList<>();
        List<String> refAddressGroupNames = new ArrayList<>();
        if(StringUtils.isNotBlank(exitAddressName)){
            // 地址名整体复用
            refAddressObjectNames.add(exitAddressName);
        } else {
            if(CollectionUtils.isNotEmpty(existAddressList)){
                refAddressObjectNames.addAll(existAddressList);
            }
            if(CollectionUtils.isNotEmpty(restAddresList)){
                if(createObjFlag){
                    String restSrcIp = StringUtils.join(restAddresList, ",");
                    IpAddressParamDTO srcIpAddressParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(restSrcIp);
                    RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                    if(restSrcIp.contains(":")){
                        ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                    }
                    String srcIpAddressName = null;
                    if(StringUtils.isNotBlank(ipSystem) && CollectionUtils.isEmpty(existAddressList)){
                        srcIpAddressName = ipSystem;
                        refAddressObjectNames.add(srcIpAddressName);
                    } else {//创建的地址对象名称有可能一个也有可能多个
                        srcIpAddressName = generatorBean.createIpAddressObjectNameByParamDTO(srcIpAddressParamDTO.getSingleIpArray(), srcIpAddressParamDTO.getRangIpArray(), srcIpAddressParamDTO.getSubnetIntIpArray(), srcIpAddressParamDTO.getSubnetStrIpArray(), srcIpAddressParamDTO.getHosts(), null, null,null);
                        if(srcIpAddressName!=null && srcIpAddressName.contains(",")){
                            String[] split = srcIpAddressName.split(",");
                            for(String tempSrcIpAddressName : split){
                                refAddressObjectNames.add(tempSrcIpAddressName);
                            }
                        }else{
                            refAddressObjectNames.add(srcIpAddressName);
                        }
                    }

                    try {
                        sb.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,srcIpAddressName,null,srcIpAddressParamDTO.getSingleIpArray(),srcIpAddressParamDTO.getRangIpArray()
                                ,srcIpAddressParamDTO.getSubnetIntIpArray(),srcIpAddressParamDTO.getSubnetStrIpArray(),null,srcIpAddressParamDTO.getHosts(),null,null,null,null,null,null));

                        if(refAddressObjectNames.size() > 1){
                            String groupName = null;
                            if(StringUtils.isNotBlank(ipSystem)){
                                groupName = ipSystem;
                            }else{
                                groupName = generatorBean.createIpAddressObjectGroupName(srcIpAddressParamDTO.getSingleIpArray(), srcIpAddressParamDTO.getRangIpArray(), srcIpAddressParamDTO.getSubnetIntIpArray(),
                                        srcIpAddressParamDTO.getSubnetStrIpArray(), null,srcIpAddressParamDTO.getHosts(), refAddressObjectNames.toArray(new String[refAddressObjectNames.size()]), null,null,null);
                            }
                            refAddressGroupNames.add(groupName);
                            sb.append(generatorBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,groupName,null,null,null
                                    ,null,null,null,null,refAddressObjectNames.toArray(new String[refAddressObjectNames.size()]),null,null,null,null,null,null));
                        }else{
                            refAddressGroupNames.addAll(refAddressObjectNames);
                        }
                    } catch (Exception e) {
                        log.error("原子化命令行生成地址对象异常",e);
                    }
                } else{
                    // TODO 不创建对象,直接使用ip
                    refAddressObjectNames.addAll(restAddresList);
                }
            }
        }

        return refAddressGroupNames;
    }

    public static List<String> getServiceGroup(List<String> refServiceNames, List<String> restAddressList, StringBuilder sb, SecurityFortinetImpl generatorBean) throws Exception{
        List<String> serviceObjectGroupNames = new ArrayList<>();
        String serviceObjectGroupName = generatorBean.createServiceObjectGroupName(null, refServiceNames.toArray(new String[0]), null, null, null);

        sb.append(generatorBean.generateServiceObjectGroupCommandLine(null, serviceObjectGroupName, null, null, null, null, refServiceNames.toArray(new String[0]), null, null, null));
        serviceObjectGroupNames.add(serviceObjectGroupName);
        return serviceObjectGroupNames;
    }

    /**
     * 创建nat地址命令行
     * @param address
     * @param sb
     * @param keyWords
     */
    public static void setAddressCommandLine(String address, StringBuilder sb, String keyWords){
        if(StringUtils.isNotEmpty(address)){
            String[] globals = address.split(",");
            for(String globalIp : globals){
                if (IpUtils.isIPRange(globalIp)) {
                    String startIp = IpUtils.getStartIpFromRange(globalIp);
                    String endIp = IpUtils.getEndIpFromRange(globalIp);
                    sb.append("set ").append(keyWords).append(startIp).append("-").append(endIp).append("\n");
                    break;
                } else if (IpUtils.isIPSegment(globalIp)) {
                    //是子网段，转换成范围
                    String ip = IpUtils.getIpFromIpSegment(globalIp);
                    //获取网段数
                    String maskBit = IpUtils.getMaskBitFromIpSegment(globalIp);
                    long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                    String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                    String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                    sb.append("set ").append(keyWords).append(startIp).append("-").append(endIp).append("\n");
                    break;
                } else  {
                    sb.append("set ").append(keyWords).append(globalIp).append("\n");
                    break;
                }
            }
        }
    }

}
