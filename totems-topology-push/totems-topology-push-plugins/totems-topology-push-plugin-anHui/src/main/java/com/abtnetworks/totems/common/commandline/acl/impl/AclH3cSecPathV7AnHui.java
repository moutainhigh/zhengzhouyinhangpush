package com.abtnetworks.totems.common.commandline.acl.impl;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.commandline.acl.AclH3cSecPathV7;
import com.abtnetworks.totems.common.commandline.acl.PolicyExtendGenerateService;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.remote.RemotePolicyService;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.abtnetworks.totems.whale.policy.dto.JsonQueryDTO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.abtnetworks.totems.common.enums.PolicyTypeEnum.SYSTEM__POLICY_1;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/15
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.H3CV7, type = PolicyEnum.ACL,classPoxy = AclH3cSecPathV7.class)
public class AclH3cSecPathV7AnHui extends SecurityPolicyGenerator implements PolicyExtendGenerateService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AclH3cSecPathV7AnHui.class);

    @Resource
    RemotePolicyService remotePolicyService;

    @Override
    public String getZonePairId(CmdDTO cmdDto) throws UnsupportedEncodingException {
        DeviceDTO deviceDTO = cmdDto.getDevice();
        PolicyDTO policyDTO = cmdDto.getPolicy();
        String srcZone = policyDTO.getSrcZone();
        String dstZone = policyDTO.getDstZone();
        PolicyEnum policyEnum = policyDTO.getType();
        String deviceUuid = deviceDTO.getDeviceUuid();
        String srcIp = policyDTO.getSrcIp();
        String dstIp = policyDTO.getDstIp();

        IpTermsExtendDTO ipTerms = new IpTermsExtendDTO();
        if (StringUtils.isNotEmpty(srcIp) && !CommonConstants.ANY.equalsIgnoreCase(srcIp)) {
            List<CommonRangeStringDTO> ip4SrcAddresses = checkConversionParam(srcIp);
            ipTerms.setIp4SrcAddresses(ip4SrcAddresses);
        }
        if (StringUtils.isNotEmpty(dstIp) && !CommonConstants.ANY.equalsIgnoreCase(dstIp)) {
            List<CommonRangeStringDTO> ip4DstAddresses = checkConversionParam(srcIp);
            ipTerms.setIp4DstAddresses(ip4DstAddresses);
        }

        ipTerms.setSkipAny(false);

        ipTerms.setPolicyType(SYSTEM__POLICY_1.getCode());
        if (!AliStringUtils.isEmpty(policyEnum.getKey())) {
            JsonQueryDTO jsonQuery = new JsonQueryDTO();
            /*filterListType: 策略集type。可能的值为：安全策略： SYSTEM__POLICY_1, SYSTEM__POLICY_2。NAT策略：
            SYSTEM__NAT_LIST。ACL策略： UNKNOWN_LIST_TYPE, SYSTEM__GENERIC_ACL*/
            String whalePolicyType = "SYSTEM__POLICY_1,SYSTEM__POLICY_2";
            Map<String, String[]> filterListType = new HashMap<>();
            filterListType.put("$in", whalePolicyType.split(","));
            jsonQuery.setFilterListType(filterListType);
            Map<String, String> dstZoneMap = new HashMap<>();
            dstZoneMap.put("$regex", dstZone);
            dstZoneMap.put("$options", "i");
            jsonQuery.setDstZone(dstZoneMap);
            Map<String, String> srcZoneMap = new HashMap<>();
            srcZoneMap.put("$regex", srcZone);
            srcZoneMap.put("$options", "i");
            jsonQuery.setSrcZone(srcZoneMap);
            ipTerms.setJsonQuery(jsonQuery);
        }
        String ipTerm = JSONObject.toJSONString(ipTerms);
        ipTerm = URLEncoder.encode(ipTerm, Charset.defaultCharset().name());
        Integer whalePolicyType = SYSTEM__POLICY_1.getCode();
        JSONArray jsonArray = remotePolicyService.remotePolicyDetailByIpTerms(ipTerm, null, deviceUuid, whalePolicyType);
        if (jsonArray != null && jsonArray.size() > 0) {
            List<PolicyDetailVO> policyDetailVOS = jsonArray.toJavaList(PolicyDetailVO.class);
            PolicyDetailVO policyDetailVO = policyDetailVOS.get(0);
            String policyName = policyDetailVO.getPolicyName();
            if (policyName.indexOf("_") > 0) {
                String zonePairId = policyName.substring(0, policyName.indexOf("_"));
                return zonePairId;
            }

        }
        return null;
    }

    private List<CommonRangeStringDTO> checkConversionParam(String ip) {
        String[] ip4Addresses = ip.split(",");
        List<CommonRangeStringDTO> ip4AddressList = new LinkedList<>();
        for (String ip4 : ip4Addresses) {

            CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();

            if (IpUtils.isIPRange(ip4)) {
                String[] ipSegment = ip4.trim().split("-");
                commonRangeStringDTO.setStart(ipSegment[0]);
                commonRangeStringDTO.setEnd(ipSegment[1]);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else if (IpUtils.isIPSegment(ip4)) {
                String startIp = IpUtils.getStartIp(ip4);
                String endIp = IpUtils.getEndIp(ip4);
                commonRangeStringDTO.setStart(startIp);
                commonRangeStringDTO.setEnd(endIp);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else if (IpUtils.isIP(ip4)) {
                commonRangeStringDTO.setStart(ip4);
                commonRangeStringDTO.setEnd(ip4);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else {
                LOGGER.error("不支持这种类型{}", ip4);
                throw new IllegalArgumentException("不支持其它类型的ip" + ip4 + "输入");
            }
        }
        return ip4AddressList;
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
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
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());

        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        try {
            String zonePairId = getZonePairId(cmdDTO);
            dto.setZonePairId(zonePairId);

        } catch (UnsupportedEncodingException e) {
            LOGGER.error("调用接口policy/rule-list-search是参数转码错误", e);
        }
        log.info("dto is" + JSONObject.toJSONString(dto, true));
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        String commandLine = composite(dto);


        generatedDto.setPolicyName(dto.getName());
        generatedDto.setAclPolicyCommand(dto.getAclPolicyCommand());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {

        return "";
    }

    /**
     * 单次进入都需要这行命令
     *
     * @param dto
     * @return
     */
    private String generatePrepareCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        if (dto.isVsys()) {
            sb.append("switchto context " + dto.getVsysName() + "\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        boolean isIPv6 = false;
        boolean isCreateObject = dto.isCreateObjFlag();
        //IPv6地址必须创建对象
        if (IpUtils.isIPv6(dto.getSrcIp()) || IpUtils.isIPv6(dto.getDstIp())) {
            isCreateObject = true;
            isIPv6 = true;
        }

        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), "source", isCreateObject);

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), "destination", isCreateObject);
        StringBuffer sb = new StringBuffer();
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            String preCommandline = generatePrepareCommandline(dto);
            sb.append(preCommandline);
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            String preCommandline = generatePrepareCommandline(dto);
            sb.append(preCommandline);
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }
        String preCommandline = generatePrepareCommandline(dto);
        sb.append(preCommandline);
        String zonePairId = dto.getZonePairId();
        String srcJoin = srcAddressObject.getJoin();
        String dstJoin = dstAddressObject.getJoin();
        StringBuffer aclRollbackPolicyCommand = new StringBuffer();

        if (StringUtils.isNotEmpty(zonePairId)) {
            aclRollbackPolicyCommand.append(String.format("acl advanced %s\n", zonePairId));
            sb.append(String.format("acl advanced %s\n", zonePairId));
        }
        List<ServiceDTO> serviceList = dto.getServiceList();

        if (CollectionUtils.isNotEmpty(serviceList)) {
            for (ServiceDTO serviceDTO : serviceList) {
                int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    String srcDstCmd = commonBufferIp(srcJoin, dstJoin);
                    aclRollbackPolicyCommand.append(String.format("undo rule permit ip %s\n", srcDstCmd));
                    sb.append(String.format("rule permit ip %s\n", srcDstCmd));

                    break;
                } else {
                    if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                        String srcDstCmd = commonBufferIp(srcJoin, dstJoin);
                        sb.append(String.format("rule permit %s %s\n", protocolString, srcDstCmd));
                        aclRollbackPolicyCommand.append(String.format("undo rule permit %s %s\n", protocolString, srcDstCmd));
                    } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                            protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                        String[] dstPorts = serviceDTO.getDstPorts().split(",");
                        //是TCP/UPD协议
                        //源为any，目的端口有值，则仅显示目的端口
                        if (StringUtils.isNotEmpty(serviceDTO.getDstPorts()) && !serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            for (String dstPort : dstPorts) {

                                String srcDstCmd = commonBufferIp(srcJoin, dstJoin);

                                if (PortUtils.isPortRange(dstPort)) {
                                    String startPort = PortUtils.getStartPort(dstPort);
                                    String endPort = PortUtils.getEndPort(dstPort);
                                    sb.append(String.format("rule permit %s %s destination range %s %s\n", protocolString, srcDstCmd, startPort, endPort));
                                    aclRollbackPolicyCommand.append(String.format("undo rule permit %s %s destination range %s %s\n", protocolString, srcDstCmd, startPort, endPort));
                                } else {
                                    sb.append(String.format("rule permit %s %s destination eq %s\n", protocolString, srcDstCmd, dstPort));
                                    aclRollbackPolicyCommand.append(String.format("undo rule permit %s %s destination eq %s\n", protocolString, srcDstCmd, dstPort));
                                }

                            }

                        }
                    }
                }
            }
        } else {
            String srcDstCmd = commonBufferIp(srcJoin, dstJoin);
            sb.append(String.format("rule permit ip %\n", srcDstCmd));
            aclRollbackPolicyCommand.append(String.format(String.format("undo rule permit ip %s\n", srcDstCmd)));


        }
        sb.append("quit\n");
        dto.setAclPolicyCommand(aclRollbackPolicyCommand.toString());

        return sb.toString();
    }

    private String commonBufferIp(String srcJoin, String dstJoin) {
        StringBuffer stringBuffer = new StringBuffer("");
        if (StringUtils.isNotEmpty(srcJoin)) {
            stringBuffer.append(srcJoin);
        }
        if (StringUtils.isNotEmpty(dstJoin)) {
            stringBuffer.append(dstJoin);
        }
        return stringBuffer.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    /**
     * 获取地址对象
     *
     * @param ipAddress ip地址
     * @return 地址对象
     */
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String prefix, boolean isCreateObject) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            dto.setJoin(prefix + " " + existsAddressName + " ");
            return dto;
        }

        //若为IPv6地址，则必须创建对象
        if (IpUtils.isIPv6(ipAddress)) {
            isCreateObject = true;
        }

        String join = "";
        String command = "";

        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine(command);
            return dto;
        }

        if (isCreateObject) {
            String name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            StringBuilder sb = new StringBuilder();
            sb.append("object-group ip address ");
            sb.append(name + "\n");
            join = prefix + " " + name + " ";

            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            int index = 0;
            boolean isIpv6 = false;
            for (String address : arr) {
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s network range %s %s\n", index, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s network subnet %s %s\n", index, ip, maskBit);
                } else if (IpUtils.isIP(address)) {
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
                        addressCmd = String.format("%s network host address %s\n", index, address.toLowerCase());
                    }
                }
                index++;
                sb.append(addressCmd);
            }

            sb.append("quit\n");

            command = sb.toString();
            if (isIpv6) {
                //ipv6时
                command = command.replace("object-group ip", "object-group ipv6");
            }
        } else {
            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            StringBuilder sb = new StringBuilder();
            for (String address : arr) {
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s-range %s %s\n", prefix, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s-subnet %s %s\n", prefix, ip, maskBit);
                } else if (IpUtils.isIP(address)) {
                    addressCmd = String.format("%s-host %s\n", prefix, address);
                }
                sb.append(addressCmd);
            }
            join = sb.toString();
        }

        dto.setJoin(join);
        dto.setCommandLine(command);
        dto.setObjectFlag(true);
        return dto;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();

        AclH3cSecPathV7AnHui checkPoint = new AclH3cSecPathV7AnHui();
        String commandLine = checkPoint.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
