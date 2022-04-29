package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luwei
 * @date 2019-03-21
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.H3CV7, type = PolicyEnum.SECURITY)
public class SecurityH3cSecPathV7ForZZYH extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityH3cSecPathV7ForZZYH.class);

    public final int MAX_OBJECT_NAME_LENGTH = 31;

    @Override
    public String generate(CmdDTO cmdDTO) {
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        if(policyDTO.getAction().equals(ActionEnum.PERMIT)) {
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
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());

        AutoRecommendSpecialDTO autoRecommendSpecialDTO = cmdDTO.getAutoRecommendSpecialDTO();
        dto.setAutoRecommendSpecialDTO(autoRecommendSpecialDTO);

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        String commandLine = composite(dto);
        cmdDTO.getSetting().setRandomNumberString( dto.getRandomNumberString() );
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setAddressObjectGroupNameList(dto.getAddressObjectGroupNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setServiceObjectGroupNameList(dto.getServiceObjectGroupNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.isVsys()) {
            sb.append("system-view\n");
            sb.append("switchto context " + dto.getVsysName() + "\n");
        }
        sb.append("system-view\n");
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        boolean isIPv6 = false;
        boolean isCreateObject = dto.isCreateObjFlag();

        //源地址
        String srcIpString = dto.getSrcIp();

        boolean isSrcAutoRecommend = false,isDstAutoRecommend = false;
        AutoRecommendSpecialDTO autoRecommendSpecialDTO = dto.getAutoRecommendSpecialDTO();
        if (autoRecommendSpecialDTO != null && (CollectionUtils.isNotEmpty(autoRecommendSpecialDTO.getExistSrcAddressList()) || CollectionUtils.isNotEmpty(autoRecommendSpecialDTO.getRestSrcAddressList()))){
            isSrcAutoRecommend = true;
        }
        if (autoRecommendSpecialDTO != null && (CollectionUtils.isNotEmpty(autoRecommendSpecialDTO.getExistDstAddressList()) || CollectionUtils.isNotEmpty(autoRecommendSpecialDTO.getRestDstAddressList()))){
            isDstAutoRecommend = true;
        }
        PolicyObjectDTO srcAddressObject = null;
        PolicyObjectDTO dstAddressObject = null;
        PolicyObjectDTO domainAddressObject = null;
        if (isSrcAutoRecommend){
            srcAddressObject = generateAutoRecommendAddressObject(isCreateObject,autoRecommendSpecialDTO.getRestSrcAddressList(),autoRecommendSpecialDTO.getExistSrcAddressList(), "source-ip");
        }else {
            srcAddressObject = generateAddressObject(srcIpString, dto.getName(), dto.getSrcAddressName(), "source-ip", isCreateObject, dto.getSrcIpSystem(), dto.getDescription());
        }

        if (isDstAutoRecommend){
            dstAddressObject = generateAutoRecommendAddressObject(isCreateObject,autoRecommendSpecialDTO.getRestDstAddressList(),autoRecommendSpecialDTO.getExistDstAddressList(), "destination-ip");
        }else {
            //目的地址（包含域名）
            String dstIpString = dto.getDstIp();

            String domain = convertDomainToIp(dstIpString);
            if (StringUtils.isNotEmpty(domain)) {
                List<String> ipStrList = new ArrayList<>();
                List<String> domainStrList = new ArrayList<>();
                String[] dstInputIpStrs = dstIpString.split(PolicyConstants.ADDRESS_SEPERATOR);
                for (String str : dstInputIpStrs) {
                    if (isDomainForIp(str)) {
                        domainStrList.add(str);
                    } else {
                        ipStrList.add(str);
                    }
                }
                String ipStr = ipStrList.stream().collect(Collectors.joining(","));
                String domainStr = domainStrList.stream().collect(Collectors.joining(","));
                dstAddressObject = generateAddressObject(ipStr, dto.getName(), dto.getDstAddressName(), "destination-ip", isCreateObject, dto.getDstIpSystem(), dto.getDescription());
                domainAddressObject = generateAddressObject(domainStr, dto.getName(), "", "destination-ip", isCreateObject, dto.getDstIpSystem(), dto.getDescription());
            } else {
                dstAddressObject = generateAddressObject(dstIpString, dto.getName(), dto.getDstAddressName(), "destination-ip", isCreateObject, dto.getDstIpSystem(), dto.getDescription());
            }
        }

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getServiceName(),isCreateObject,dto.getIpType());

        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        // 记录华三V7创建命令行对象名称
        recordCreateAddrAndServiceObjectName(dto,srcAddressObject,dstAddressObject,null,null);
        recordCreateServiceObjectNames(dto,serviceObject);
        recordCreateTimeObjectName(dto,timeObject);
        //IPv6地址必须创建对象
        if(dto.getIpType()!=null&&dto.getIpType()==1) {
            isCreateObject = true;
            isIPv6 = true;
        }
        StringBuilder sb = new StringBuilder();


        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }


        if (ObjectUtils.isNotEmpty(dstAddressObject) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }


        if (ObjectUtils.isNotEmpty(domainAddressObject) && domainAddressObject.isObjectFlag() && StringUtils.isNotBlank(domainAddressObject.getCommandLine())) {
            sb.append(domainAddressObject.getCommandLine());
            sb.append("\n");
        }


        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
            sb.append("\n");
        }
        if (timeObject != null) {
            sb.append(timeObject.getCommandLine());
            sb.append("\n");
        }

        sb.append("security-policy ");
        if (isIPv6) {
            sb.append("ipv6\n");
        } else {
            sb.append("ip\n");
        }

        String randomNumberString = IdGen.getRandomNumberString();
        dto.setRandomNumberString( dto.getName() +"-"+ randomNumberString );
        logger.info( "-------------randomNumberString:"+ dto.getRandomNumberString() );

        if (StringUtils.isNotEmpty(dto.getSrcIpSystem()) && StringUtils.isNotEmpty(dto.getDstIpSystem()) && StringUtils.isNotEmpty(dto.getDescription())) {
            sb.append(String.format("rule name %s\n", "\"" + dto.getSrcIpSystem() + "-" + dto.getDstIpSystem() + "-" + dto.getDescription() + "\""));
        } else {
            sb.append(String.format("rule name %s\n", dto.getName()+"_"+ "AO" +"_"+ randomNumberString));
        }


        sb.append("counting enable\n");
        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append("description " + dto.getDescription() + "\n");
        }
        String action = dto.getAction();

        if (StringUtils.isNotBlank(dto.getSrcZone()) && !"any".equalsIgnoreCase(dto.getSrcZone().trim())) {
            String[] srcZones = dto.getSrcZone().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String zone : srcZones) {
                sb.append(String.format("source-zone %s\n", zone));
            }
        }
        if (StringUtils.isNotBlank(dto.getDstZone()) && !"any".equalsIgnoreCase(dto.getDstZone().trim())) {
            String[] dstZones = dto.getDstZone().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String zone : dstZones) {
                sb.append(String.format("destination-zone %s\n", zone));
            }
        }


        if (ObjectUtils.isNotEmpty(srcAddressObject)) {
            sb.append(srcAddressObject.getJoin());
        }


        if (ObjectUtils.isNotEmpty(dstAddressObject)) {
            sb.append(dstAddressObject.getJoin());
        }


        if (ObjectUtils.isNotEmpty(domainAddressObject)) {
            sb.append(domainAddressObject.getJoin());
        }


        if (serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        }


        if (timeObject != null) {
            sb.append(String.format("time-range %s\n", timeObject.getJoin()));
        }

        // move rule before xxx 会导致下发命令行失败，因为下发的时候存在move rule before xxx
//        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
//        int moveSeatCode = dto.getMoveSeatEnum().getCode();
//        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
//            if (!AliStringUtils.isEmpty(swapRuleNameId)) {
//                sb.append(String.format("move rule before %s\n", swapRuleNameId));
//            }
//        }
        if ("deny".equalsIgnoreCase(action)) {
            sb.append("action drop \n");
        } else {
            sb.append("action pass \n");
        }
        Integer idleTimeout = dto.getIdleTimeout();
        if(ObjectUtils.isNotEmpty(idleTimeout)){
            idleTimeout /= 3600;
            sb.append("session persistent aging-time ").append(idleTimeout).append(StringUtils.LF);
        }
        sb.append("quit\n");
        sb.append("return\n");
        String command = sb.toString();
        return command;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    /**
     * 获取地址对象
     * @param ipAddress ip地址
     * @return 地址对象
     */
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String prefix, boolean isCreateObject, String ipSystem, String description) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(prefix + " " + existsAddressName + "\n");
            return dto;
        }

        //若为IPv6地址，则必须创建对象
        if(IpUtils.isIPv6(ipAddress)) {
            isCreateObject = true;
        }

        String join = "";
        String command = "";

        if(AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine(command);
            return dto;
        }

        List<String> createGroupObjectNames = new ArrayList<>();
        if(isCreateObject) {
            String name;
            //判断地址中有没有域名，有域名则对象名称为域名本身
            String domain = convertDomainToIp(ipAddress);
            if (StringUtils.isNotEmpty(domain)) {
                name = String.format("%s", domain);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("object-group ip address ");
            boolean flag = false;
            if (IpUtils.isIP(ipAddress) || IpUtils.isIPSegment(ipAddress)) {
                flag = true;
                name = ipAddress;
            } else if (IpUtils.isIPRange(ipAddress)) {
                flag = true;
                String startIpFromRange = IpUtils.getStartIpFromRange(ipAddress);
                String endIpFromRange = IpUtils.getEndIpFromRange(ipAddress);
                StringBuilder rangeSB = new StringBuilder();
                rangeSB.append(startIpFromRange);
                rangeSB.append("-");
                String[] startIpFromRangeArr = startIpFromRange.split("[.]");
                String[] endIpFromRangeArr = endIpFromRange.split("[.]");
                boolean flag2 = false;
                for (int i = 0; i < 3; i++) {
                    if (!startIpFromRangeArr[i].equals(endIpFromRangeArr[i])) {
                        flag2 = true;
                        break;
                    }
                }
                if (flag2) {
                    rangeSB.append(endIpFromRange);
                } else {
                    rangeSB.append(endIpFromRange.substring(endIpFromRange.lastIndexOf(".")+1));
                }
                name = rangeSB.toString();
            }else {
                if (StringUtils.isEmpty(description)) {
                    name = LocalDate.now() + "_" + IdGen.getRandomNumberString();
                }else {
                    name = description;
                }
            }
            if (flag) {
                if ("source-ip".equals(prefix)) {
                    sb.append("S"+ name + "\n");
                    name = "S"+ name;
                }else if ("destination-ip".equals(prefix)) {
                    sb.append("D"+ name + "\n");
                    name = "D"+ name;
                }
            }else {
                if (StringUtils.isEmpty(ipSystem)) {
                    sb.append(name + "\n");
                } else {
                    if (StringUtils.isEmpty(description)) {
                        sb.append(ipSystem + "\n");
                        name = ipSystem;
                    }else {
                        sb.append(ipSystem + "_" + name + "\n");
                        name = ipSystem + "_" + name;
                    }
                }
            }

            join = prefix + " " + name + "\n";
            createGroupObjectNames.add(name);
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
                } else {
                    addressCmd = String.format("%s network host name %s\n", index,  address.toLowerCase());
                }
                index++;
                sb.append(addressCmd);
            }

            sb.append("quit\n");
            // 域名和ipv4 对象回滚的时候统一用ipv4对象回滚的命令行格式
            dto.setIpType(0);
            command = sb.toString();
            if (isIpv6) {
                //ipv6时
                command = command.replace("object-group ip", "object-group ipv6");
                dto.setIpType(1);
            }
            dto.setName(name);
        } else {
            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            StringBuilder sb = new StringBuilder();
            for(String address : arr) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format( "%s-range %s %s\n", prefix, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s-subnet %s %s\n", prefix, ip, maskBit);
                } else if (IpUtils.isIP(address)){
                    addressCmd = String.format("%s-host %s\n", prefix, address);
                } else {
                    addressCmd = String.format("%s-host %s\n", prefix, address);
                }
                sb.append(addressCmd);
            }
            join = sb.toString();
        }

        dto.setJoin(join);
        dto.setCommandLine(command);
        dto.setObjectFlag(true);
        dto.setCreateGroupObjectName(createGroupObjectNames);
        return dto;
    }

    /**
     * 获取地址对象
     *
     * @param restAddressList
     * @param existAddressList
     * @return 地址对象
     */
    public PolicyObjectDTO generateAutoRecommendAddressObject(boolean isCreateObject, List<AddressObjectInfoDTO> restAddressList,
                                                              List<AddressObjectInfoDTO> existAddressList, String prefix) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        //若为IPv6地址，则必须创建对象
        isCreateObject = true;

        StringBuilder join = new StringBuilder();
        StringBuilder command = new StringBuilder();

        if (CollectionUtils.isEmpty(restAddressList) && CollectionUtils.isEmpty(existAddressList)) {
            dto.setJoin(join.toString());
            dto.setCommandLine(command.toString());
            return dto;
        }

        boolean isIpv6 = false;
        if (isCreateObject) {
            String addressCmd = "";
            for (AddressObjectInfoDTO addressObjectInfoDTO : restAddressList) {
                String addressObjectName = addressObjectInfoDTO.getAddressObjectName();
                String addressObjectIP = addressObjectInfoDTO.getAddressObjectIP();
                if(StringUtils.isNotEmpty(addressObjectIP) && StringUtils.isNotEmpty(addressObjectName)){
                    String[] addresses = addressObjectIP.split(",");
                    command.append(String.format("object-group ip address %s\n", addressObjectName));
                    for (String address : addresses) {
                        if (IpUtils.isIPRange(address)) {
                            String startIp = IpUtils.getStartIpFromRange(address);
                            String endIp = IpUtils.getEndIpFromRange(address);
                            addressCmd = String.format("network range %s %s\n", startIp, endIp);
                        } else if (IpUtils.isIPSegment(address)) {
                            String ip = IpUtils.getIpFromIpSegment(address);
                            String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                            addressCmd = String.format("network subnet %s %s\n", ip, maskBit);
                        } else if (IpUtils.isIP(address)) {
                            addressCmd = String.format("network host address %s\n", address);
                        } else if (address.contains(":")) {
                            isIpv6 = true;
                            //ipv6
                            if (address.contains("/")) {
                                String[] addrArray = address.split("/");
                                if (StringUtils.isNotEmpty(addrArray[0])) {
                                    addressCmd = String.format("network subnet %s %s\n", addrArray[0].toLowerCase(), addrArray[1]);
                                }
                            } else if (address.contains("-")) {
                                String[] addrArray = address.split("-");
                                if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                                    addressCmd = String.format("network range %s %s\n", addrArray[0].toLowerCase(), addrArray[1].toLowerCase());
                                }
                            } else {
                                addressCmd = String.format("network host address %s\n", address.toLowerCase());
                            }
                        }
                        command.append(addressCmd);

                    }
                    if ("source-ip".equals(prefix)) {
                        join.append(String.format("%s %s\n", prefix, "S"+addressObjectIP));
                    } else if ("destination-ip".equals(prefix)) {
                        join.append(String.format("%s %s\n", prefix, "D"+addressObjectIP));
                    }

                    command.append("quit\n");
                }


            }
        }
        if (CollectionUtils.isNotEmpty(existAddressList)){
            for (AddressObjectInfoDTO addressObjectInfoDTO : existAddressList) {
                if (StringUtils.isNotEmpty(addressObjectInfoDTO.getAddressObjectName())){
                    if ("source-ip".equals(prefix)) {
                        join.append(String.format("%s %s\n", prefix, "S"+addressObjectInfoDTO.getAddressObjectIP()));
                    } else if ("destination-ip".equals(prefix)) {
                        join.append(String.format("%s %s\n", prefix, "D"+addressObjectInfoDTO.getAddressObjectIP()));
                    }
                }
            }
        }
        if (isIpv6) {
            //ipv6时
            dto.setCommandLine(command.toString().replace("object-group ip", "object-group ipv6"));
        } else {
            dto.setCommandLine(command.toString());
        }
        dto.setJoin(join.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }

    /**
     * 获取服务集对象文本
     * @param serviceDTOList 服务列表
     * @return 服务集对象
     */
    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName, boolean isCreateObject,Integer iptype) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setJoin("service" + " " + existsServiceName + "\n");
            return dto;
        }
        if(CollectionUtils.isEmpty(serviceDTOList)){
            dto.setJoin("service any\n");
            return dto;
        }

        List<String> createServiceGroupObjectName = new ArrayList<>();
        String join = "";
        String command = "";
        if(isCreateObject) {
            StringBuilder sb = new StringBuilder();
            String setName = generateServiceName(serviceDTOList);
            sb.append(String.format("object-group service %s\n", setName));
            createServiceGroupObjectName.add(setName);
            int index = 0;
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto = new PolicyObjectDTO();
                    dto.setJoin(join);
                    dto.setCommandLine(command);
                    return dto;
                }

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)||protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                    if(iptype==1){
                        sb.append(String.format("%d service icmpv6 ", index));
                    }else {
                        sb.append(String.format("%d service icmp ", index));
                    }
                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                    index++;
                } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    //是TCP/UPD协议
                    //源为any，目的端口有值，则仅显示目的端口
                    if (service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        for (String dstPort : dstPorts) {
                            sb.append(String.format(index + " service %s ", protocolString));
                            if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("destination range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("destination eq %s  \n", dstPort));
                            }
                            index++;
                        }

                    } else if (!service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        //源端口有值，目的端口any，则仅显示源端口
                        for (String srcPort : srcPorts) {
                            sb.append(String.format(index + " service %s ", protocolString));
                            if (PortUtils.isPortRange(srcPort)) {
                                String startPort = PortUtils.getStartPort(srcPort);
                                String endPort = PortUtils.getEndPort(srcPort);
                                sb.append(String.format("source range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("source eq %s \n", srcPort));
                            }
                            index++;
                        }
                    } else {
                        //源和目的端口都有具体的值、或者都为any
                        for (String srcPort : srcPorts) {
                            for (String dstPort : dstPorts) {
                                sb.append(String.format(index + " service %s ", protocolString));

                                if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(srcPort)) {
                                    String startPort = PortUtils.getStartPort(srcPort);
                                    String endPort = PortUtils.getEndPort(srcPort);
                                    sb.append(String.format("source range %s %s ", startPort, endPort));
                                } else {
                                    sb.append(String.format("source eq %s ", srcPort));
                                }

                                if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(dstPort)) {
                                    String startPort = PortUtils.getStartPort(dstPort);
                                    String endPort = PortUtils.getEndPort(dstPort);
                                    sb.append(String.format("destination range %s %s", startPort, endPort));
                                } else {
                                    sb.append(String.format("destination eq %s", dstPort));
                                }

                                sb.append("\n");
                                index++;
                            }
                        }
                    }
                }
            }
            dto.setName(setName);
            dto.setGroup(true);
            sb.append("quit\n");
            join = String.format("service %s\n", setName);
            command = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto = new PolicyObjectDTO();
                    dto.setJoin(join);
                    dto.setCommandLine(command);
                    return dto;
                }

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    sb.append("service-port icmp ");
                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    //是TCP/UPD协议
                    //源为any，目的端口有值，则仅显示目的端口
                    if (service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        for (String dstPort : dstPorts) {
                            sb.append(String.format("service-port %s ", protocolString));
                            if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("destination range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("destination eq %s  \n", dstPort));
                            }
                        }

                    } else if (!service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        //源端口有值，目的端口any，则仅显示源端口
                        for (String srcPort : srcPorts) {
                            sb.append(String.format("service-port %s ", protocolString));
                            if (PortUtils.isPortRange(srcPort)) {
                                String startPort = PortUtils.getStartPort(srcPort);
                                String endPort = PortUtils.getEndPort(srcPort);
                                sb.append(String.format("source range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("source eq %s \n", srcPort));
                            }
                        }
                    } else {
                        //源和目的端口都有具体的值、或者都为any
                        for (String srcPort : srcPorts) {
                            for (String dstPort : dstPorts) {
                                sb.append(String.format("service-port %s ", protocolString));

                                if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(srcPort)) {
                                    String startPort = PortUtils.getStartPort(srcPort);
                                    String endPort = PortUtils.getEndPort(srcPort);
                                    sb.append(String.format("source range %s %s ", startPort, endPort));
                                } else {
                                    sb.append(String.format("source eq %s ", srcPort));
                                }

                                if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(dstPort)) {
                                    String startPort = PortUtils.getStartPort(dstPort);
                                    String endPort = PortUtils.getEndPort(dstPort);
                                    sb.append(String.format("destination range %s %s", startPort, endPort));
                                } else {
                                    sb.append(String.format("destination eq %s", dstPort));
                                }

                                sb.append("\n");
                            }
                        }
                    }
                }
            }
            join = sb.toString();
        }

        dto.setJoin(join);
        dto.setCommandLine(command);
        dto.setCreateServiceGroupObjectNames(createServiceGroupObjectName);
        return dto;
    }

    public String generateServiceName(List<ServiceDTO> serviceDTOList) {
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(generateServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    private String generateServiceName(ServiceDTO dto) {
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

            sb.append(String.format("_%s", dstPort));
        }
        return sb.toString().toLowerCase();
    }

    static String formatTimeString(String timeString) {
        if (StringUtils.isBlank(timeString)) {
            return null;
        }

        String dateStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        SimpleDateFormat sdf2 = new SimpleDateFormat(TimeUtils.H3C_V7_FORMAT);
        try{
            Date date = sdf.parse(timeString);
            dateStr = sdf2.format(date);
        }catch (Exception e) {
            logger.error("时间转化异常", e);
        }

        return dateStr;
    }

    /**
     * 生成时间区间对象
     * @param startTimeString 开始时间字符串
     * @param endTimeString 结束时间字符串
     * @return 时间区间对象
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if(AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

        String setName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(String.format("time-range %s from %s to %s \n", setName, startTime, endTime));
        return dto;
    }

    private String convertDomainToIp (String dstIpStr) {
        String domain = "";
        String[] dstInputIpStrs = dstIpStr.split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String inputIp : dstInputIpStrs) {
            //内到外，源是明确的必填 目的可以不填
            //如果选择的是ipv4，则目的地址只能包含idv4或者域名，选择的是Ipv6，则目的地址只能包含ipv6和域名
            int rc,rc2 ;
            rc = InputValueUtils.checkIpV6(inputIp);
            Boolean isDomain = true;
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
                //ip校验不通过的默认为域名
                isDomain = false;
            } else {
                isDomain = true;
            }
            rc2 = InputValueUtils.checkIp(inputIp);

            //若出IP范围起始地址大于终止地址错误，则自动纠正
            if (rc2 == ReturnCode.INVALID_IP_RANGE) {
                inputIp= InputValueUtils.autoCorrect(inputIp);
                rc2 = ReturnCode.POLICY_MSG_OK;
            }

            if (rc2 != ReturnCode.POLICY_MSG_OK && rc2 != ReturnCode.INVALID_IP_RANGE) {
                isDomain = false;
            } else {
                isDomain = true;
            }
            if (!isDomain) {
                return inputIp;
            }
        }
        return null;
    }

    /**
     * 判断目的IP是否是域名
     * @param dstIp
     * @return
     */
    private Boolean isDomainForIp(String dstIp) {
        if (IpUtils.isIPSegment(dstIp) || IpUtils.isIPRange(dstIp) || IpUtils.isIPv6(dstIp) || IpUtils.isIP(dstIp)) {
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        SecurityH3cSecPathV7ForZZYH h3cv7 = new SecurityH3cSecPathV7ForZZYH();
        //h3cv7.convertDomainToIp("14.215.177.38,14.215.177.39,www.baidu.com");
        h3cv7.generateAddressObject("14.215.177.38,14.215.177.39,www.baidu.com","","","",true,"", "");
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO=new ServiceDTO();
        serviceDTO.setDstPorts("any");
        serviceDTO.setProtocol("58");
        srcport.add(serviceDTO);
        dto.setServiceList(srcport);
//
//        dto.setSrcIp("1111:a1a1::1111,1111:a1a1::1111-1111:a1a1::1112,1111:a1a1::1111/128");
//        dto.setDstIp("1111:a1a1::1121");
        dto.setIpType(1);
        //SecurityH3cSecPathV7 h3cv7 = new SecurityH3cSecPathV7();
        String commandLine = h3cv7.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
