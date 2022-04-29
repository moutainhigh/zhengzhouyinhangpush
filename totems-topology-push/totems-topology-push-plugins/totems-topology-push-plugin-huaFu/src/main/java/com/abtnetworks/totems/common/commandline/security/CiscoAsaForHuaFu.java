package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/6/24
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO, type = PolicyEnum.SECURITY)
public class CiscoAsaForHuaFu extends SecurityPolicyGenerator implements PolicyGenerator {
    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA.class);

    private final String IN_KEY = "in";

    private final int MAX_NAME_LENGTH = 65;

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

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCiscoInterfaceCreate(settingDTO.isCreateCiscoItfRuleList());
        dto.setCiscoInterfacePolicyName(settingDTO.getCiscoItfRuleListName());
        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        log.info("dto is" + JSONObject.toJSONString(dto, true));
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();

        String srcItfAlias = dto.getSrcItfAlias();
        String dstItfAlias = dto.getDstItfAlias();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();

        String businessName = String.format("%s", dto.getBusinessName());

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());
        List<String> srcAddressList = srcAddress.getCommandLineList();
        List<String> dstAddressList = dstAddress.getCommandLineList();


        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        sb.append("configure terminal\n");
        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }


        String interfaceName = dto.getCiscoInterfacePolicyName();


        //思科新建策略，默认是置顶的、最前，不分前后
        String line1 = "";

        //存在接口信息，则就是编辑
        if (StringUtils.isNotBlank(interfaceName)) {
            businessName = interfaceName;
            String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
            int moveSeatCode = dto.getMoveSeatEnum().getCode();
            if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                line1 = String.format("line %s", swapRuleNameId);
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                int lineNum = -1;
                try {
                    lineNum = Integer.valueOf(swapRuleNameId);
                } catch (Exception e) {
                    logger.info("放在某条策略之后的名称应为数字ID！");
                }
                line1 = String.format("line %d", lineNum + 1);
            } else if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
                line1 = "line 1";
            }
        }


        //对象式命令行 或，服务、源、目的地址都是建对象或复用的
        if (createObjFlag || (serviceObject.isObjectFlag() && srcAddress.isObjectFlag() && dstAddress.isObjectFlag())) {
            List<ServiceDTO> serviceDTOList = dto.getServiceList();
            if (CollectionUtils.isNotEmpty(serviceDTOList)) {
                for (ServiceDTO serviceDTO : serviceDTOList) {
                    int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                    String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                    String serviceJoin = getServiceJoin(serviceDTO);
                    if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        protocolString = "ip";
                    }
                    sb.append(String.format("access-list %s %s extended  %s %s %s %s %s \n", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin(), serviceJoin));
                }

            }
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && !srcAddress.isObjectFlag() && !dstAddress.isObjectFlag()) {
            //不建对象，但服务对象是复用的，源、目的地址都是直接写内容
            List<ServiceDTO> serviceDTOList = dto.getServiceList();
            for (int i = 0; i < srcAddressList.size(); i++) {
                for (int j = 0; j < dstAddressList.size(); j++) {
                    for (ServiceDTO serviceDTO : serviceDTOList) {
                        int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

                        String serviceJoin = getServiceJoin(serviceDTO);
                        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            protocolString = "ip";
                        }
                        sb.append(String.format("access-list %s %s extended %s %s %s %s %s \n", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), dstAddressList.get(j), serviceJoin));
                    }
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
            //不建对象，服务、源地址是复用的，目的地址直接写内容
            List<ServiceDTO> serviceDTOList = dto.getServiceList();
            for (int j = 0; j < dstAddressList.size(); j++) {
                for (ServiceDTO serviceDTO : serviceDTOList) {
                    int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                    String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                    String serviceJoin = getServiceJoin(serviceDTO);
                    if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        protocolString = "ip";
                    }
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s \n", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddressList.get(j), serviceJoin));
                }
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，服务、目的地址是复用，源地址直接写内容
            List<ServiceDTO> serviceDTOList = dto.getServiceList();
            for (int j = 0; j < srcAddressList.size(); j++) {
                for (ServiceDTO serviceDTO : serviceDTOList) {
                    int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                    String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                    String serviceJoin = getServiceJoin(serviceDTO);
                    if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        protocolString = "ip";
                    }
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s \n", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(j), dstAddress.getJoin(), serviceJoin));
                }
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，但源、目的地址复用，服务直接写内容
            for (ServiceDTO service : dto.getServiceList()) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                String serviceJoin = getServiceJoin(service);
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                //服务的端口是any
                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s ", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin()));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    continue;
                }

                String[] dstPorts = service.getDstPorts().split(",");
                List<String> dstPortList = formatFullPort(dstPorts);
                for (int j = 0; j < dstPortList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin(), dstPortList.get(j), serviceObject.getJoin()));
                }
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }

        } else {
            //不建对象，做笛卡尔积处理， 下面的循环保留srcPort，以防止后期出现源端口
            for (ServiceDTO service : dto.getServiceList()) {
                //根据协议Id 取协议名称
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                List<String> srcPortList = new ArrayList<>();
                List<String> dstPortList = new ArrayList<>();

                // 多个端口
                if (!protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && !"ip".equals(protocolString)) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    srcPortList = formatFullPort(srcPorts);
                    dstPortList = formatFullPort(dstPorts);
                }
                //dstPortList 非空，则表示是：tcp、udp协议
                if (dstPortList != null && dstPortList.size() > 0) {
                    //非对象笛卡尔积
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int j = 0; j < srcPortList.size(); j++) {
                            for (int m = 0; m < dstAddressList.size(); m++) {
                                for (int n = 0; n < dstPortList.size(); n++) {
                                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), srcPortList.get(j), dstAddressList.get(m), dstPortList.get(n)));
                                    if (time != null) {
                                        sb.append(time.getJoin());
                                    }
                                    sb.append("\n");
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int m = 0; m < dstAddressList.size(); m++) {
                            sb.append(String.format("access-list %s %s extended %s %s %s %s",
                                    businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), dstAddressList.get(m)));

                            //icmp协议需要增加协议类型
                            if (protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && StringUtils.isNotBlank(service.getType())) {
                                sb.append(String.format(" %d", Integer.valueOf(service.getType())));
                            }

                            if (time != null) {
                                sb.append(time.getJoin());
                            }
                            sb.append("\n");
                        }
                    }
                }
            }
        }


        //接口为空时，需要新建
        if (dto.isCiscoInterfaceCreate()) {
            if (StringUtils.isNotBlank(srcItfAlias)) {
                sb.append(String.format("access-group %s in interface %s\n", businessName, srcItfAlias));
            } else if (StringUtils.isNotBlank(dstItfAlias)) {
                sb.append(String.format("access-group %s out interface %s\n", businessName, dstItfAlias));
            }
        }

        sb.append("end\nwrite\n");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * 服务关联
     *
     * @param serviceDTO
     * @return
     */
    private String getServiceJoin(ServiceDTO serviceDTO) {

        String serviceName = getServiceNameByServiceDto(serviceDTO);
        String serviceJoin = " ";
        if(StringUtils.isNotEmpty(serviceName)){
             serviceJoin = String.format("object-group %s ", serviceName);
        }

        return serviceJoin;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }


    public String generateDeleteCommandline(CommandlineDTO dto) {
        String generatedCommandLine = generateCommandline(dto);
        if (StringUtils.isEmpty(generatedCommandLine)) {
            logger.error("generatedCommandLine不能为空");
            return "";
        } else if (!generatedCommandLine.contains("access-list")) {
            logger.error("命令行[{}]没有关键字：access-list", generatedCommandLine);
            return "";
        } else {
            return generatedCommandLine.replaceAll("access-list", "no access-list");
        }
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin("object-group " + existsAddressName + " ");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        // 是创建对象
        if (createObjFlag) {
            String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s \n", objName));
            formatFullAddress(arr, list, sb);
            sb.append("exit\n");
            dto.setCommandLine(sb.toString());
            dto.setJoin("object-group " + dto.getName() + " ");
            dto.setObjectFlag(true);
        } else {
            //直接显示内容
            formatFullAddress(arr, list, sb);
            dto.setCommandLineList(list);
            dto.setObjectFlag(false);
        }

        return dto;
    }

    private void formatFullAddress(String[] arr, List<String> list, StringBuilder sb) {
        for (String address : arr) {
            String fullStr = "";
            if (IpUtils.isIPSegment(address)) {
                //获取ip
                String ip = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                //获取网段的ip
                String mask = IpUtils.getMaskByMaskBit(maskBit);
                //将ip和mask转二进制后，进行与计算，得到十进制的子网段ip地址
                String ipDecimal = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(ip), IpUtils.getBinaryIp(mask));
                fullStr = String.format(" %s %s ", ipDecimal, mask);
                sb.append(String.format("network-object %s \n", fullStr));
                list.add(fullStr);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                //取最后一个字符，循环中间值
                String[] startIpArr = startIp.split("\\.");
                String[] endIpArr = endIp.split("\\.");
                Integer startIp_lastNum = Integer.valueOf(startIpArr[3]);
                Integer endIp_lastNum = Integer.valueOf(endIpArr[3]);
                for (int i = startIp_lastNum; i <= endIp_lastNum; i++) {
                    fullStr = String.format(" host %s.%s.%s.%s", startIpArr[0], startIpArr[1], startIpArr[2], i);
                    sb.append(String.format("network-object %s \n", fullStr));
                    list.add(fullStr);
                }
            } else {
                fullStr = String.format(" host %s ", address);
                sb.append(String.format("network-object %s \n", fullStr));
                list.add(fullStr);
            }
        }
    }

    private List<String> formatFullPort(String[] arr) {
        List<String> list = new ArrayList<>();
        for (String srcPortString : arr) {
            if (PortUtils.isPortRange(srcPortString)) {
                list.add(String.format("range %s", PortUtils.getPortString(srcPortString, PortUtils.BLANK_FORMAT)));
            } else if (srcPortString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                //2019-03-18 修改：如果端口为any，显示保持为空
                list.add(" ");
            } else {
                list.add(String.format("eq %s", PortUtils.getPortString(srcPortString, PortUtils.BLANK_FORMAT)));
            }
        }
        return list;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

//        if (StringUtils.isNotBlank(existsServiceName)) {
//            dto.setObjectFlag(true);
//            dto.setJoin(String.format("object-group %s ", existsServiceName));
//            return dto;
//        }

        if (!createObjFlag) {
            return dto;
        }

        if (serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setObjectFlag(false);
            dto.setJoin("");
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        for (ServiceDTO service : serviceDTOList) {
            String nameService = getServiceNameByServiceDto(service);

            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                continue;
            } else {

                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    continue;
                }

                String[] ports = service.getDstPorts().split(",");
                sb.append(String.format("object-group service %s %s \n", nameService, protocolString));
                for (String port : ports) {

                    if (PortUtils.isPortRange(port)) {
                        sb.append(String.format("port-object range  %s \n", PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    } else if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        //2019-03-18 修改：如果端口为any，显示保持为空
                        sb.append(" ");
                    } else {
                        sb.append(String.format("port-object eq %s \n", PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    }
                }
                sb.append("exit\n");
            }
        }



        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    /***
     * 瓶装对象名称
     * @param serviceDTO
     * @return
     */
    private String getServiceNameByServiceDto(ServiceDTO serviceDTO) {
        StringBuffer nameSb = new StringBuffer();
        String dstPorts = serviceDTO.getDstPorts();
        int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
        if (StringUtils.isNotEmpty(dstPorts) && !dstPorts.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            String[] dstPortArray = dstPorts.split(",");
            nameSb.append(protocolString).append("_");
            String formatPort =  PortUtils.getPortString(dstPortArray[0], PortUtils.UNDERLINE_FORMAT);
            if (dstPortArray.length > 1) {
                nameSb.append(formatPort).append("_etc");
            } else {
                nameSb.append(formatPort);
            }
        }


        String name = nameSb.toString();

        return name;
    }

    /**
     * 生成时间对象
     *
     * @param startTime
     * @param endTime
     * @param ticket
     * @return
     */
    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (startTime == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

        sb.append(String.format("time-range %s \n", name));
        sb.append(String.format("absolute start %s end %s \n", formatTimeString(startTime), formatTimeString(endTime)));
        sb.append("exit\n");
        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setJoin(String.format(" time-range %s", name));
        return dto;
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.CISCO_ASA_TIME_FORMAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }


    public static void main(String[] args) {
        CiscoAsaForHuaFu ciscoASA = new CiscoAsaForHuaFu();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        String commandLine = ciscoASA.composite(dto);
        System.out.println("commandline:\n" + commandLine);


    }
}
