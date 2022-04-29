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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:38
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO, type = PolicyEnum.SECURITY)
public class SecurityCiscoASAForHuaRui extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASAForHuaRui.class);

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
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCiscoInterfaceCreate(settingDTO.isCreateCiscoItfRuleList());
        dto.setCiscoInterfacePolicyName(settingDTO.getCiscoItfRuleListName());
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setOutBound(settingDTO.isOutBound());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        log.info("dto is" + JSONObject.toJSONString(dto, true));
        String commandLine = composite(dto);

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        //思科特殊处理，在进行回滚时，使用的是整个策略，而不是名称
        generatedDto.setPolicyName(commandLine);

        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();

        //创建服务、地址对象
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());

        return commonLine(srcAddress, dstAddress, serviceObject, dto);
    }

    public String generateCommandline(CommandlineDTO dto,Integer mergeProperty) {
        String ticket = dto.getName();
        boolean createObjFlag = dto.isCreateObjFlag();
        //创建服务、地址对象
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());

        return commonLine(srcAddress, dstAddress, serviceObject, dto,mergeProperty);
    }




    //提取公共, 供思科 ASA 2个版本使用
    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                             CommandlineDTO dto) {
        List<String> srcAddressList = srcAddress.getCommandLineList();
        List<String> dstAddressList = dstAddress.getCommandLineList();

        String ticket = dto.getName();
        String srcItf = dto.getSrcItf();
        String srcItfAlias = dto.getSrcItfAlias();
        String dstItfAlias = dto.getDstItfAlias();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();

        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        StringBuilder sb = new StringBuilder();
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

        String businessName = String.format("%s", dto.getBusinessName());

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
            sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && !srcAddress.isObjectFlag() && !dstAddress.isObjectFlag()) {
            //不建对象，但服务对象是复用的，源、目的地址都是直接写内容
            for (int i = 0; i < srcAddressList.size(); i++) {
                for (int j = 0; j < dstAddressList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(i), dstAddressList.get(j)));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
            //不建对象，服务、源地址是复用的，目的地址直接写内容
            for (int j = 0; j < dstAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddressList.get(j)));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，服务、目的地址是复用，源地址直接写内容
            for (int j = 0; j < srcAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(j), dstAddress.getJoin()));
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
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                //服务的端口是any
                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin()));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    continue;
                }

                String[] dstPorts = service.getDstPorts().split(",");
                List<String> dstPortList = formatFullPort(dstPorts);
                for (int j = 0; j < dstPortList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin(), dstPortList.get(j)));
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
        //长链接命令行生成
        String idleTimeoutCmd = getIdleTimeoutCmd(dto,businessName);
        sb.append(idleTimeoutCmd);
        //接口为空时，需要新建
        if (dto.isCiscoInterfaceCreate()) {
            if (dto.isOutBound()) {
                sb.append(String.format("access-group %s out interface %s\n", businessName, dstItfAlias));
            } else {
                sb.append(String.format("access-group %s in interface %s\n", businessName, srcItfAlias));
            }
        }

        sb.append("end\nwrite\n");
        sb.append("\n");

        return sb.toString();
    }

    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject, CommandlineDTO dto, Integer mergeProperty) {
        StringBuilder sb = new StringBuilder();
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
        String businessName = String.format("%s", dto.getBusinessName());
        //思科新建策略，默认是置顶的、最前，不分前后
        sb.append(String.format("access-list %s extended %s %s %s %s\n", businessName, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
        sb.append("end\nwrite\n");
        sb.append("\n");

        return sb.toString();
    }

    public String getIdleTimeoutCmd(CommandlineDTO dto, String businessName) {
        if (dto.getIdleTimeout() != null) {
            StringBuffer stringBuffer = new StringBuffer();
            String ticket = dto.getName();

            String objName1 = String.format("%s_T_%s", ticket, IdGen.getRandomNumberString());
            stringBuffer.append(String.format("class-map %s \nmatch access-list %s\nexit\n", objName1, businessName));

            int timeStamp = dto.getIdleTimeout();
            int second = timeStamp % 60;
            int minuteTemp = timeStamp / 60;
            String object ;
            if (minuteTemp > 0) {
                int minute = minuteTemp % 60;
                int hour = minuteTemp / 60;
                if (hour > 0) {
                    object = (hour >= 10 ? (hour + "") : (hour)) + ":" + (minute >= 10 ? (minute + "") : (minute))
                            + ":" + (second >= 10 ? (second + "") : (second));
                } else {
                    object = "0:" + (minute >= 10 ? (minute + "") : (minute)) + ":"
                            + (second >= 10 ? (second + "") : (second));
                }
            } else {
                object = "0:0:" + (second >= 10 ? (second + "") : (second));
            }
            String objName2 = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            stringBuffer.append(String.format("policy-map %s \nclass %s \nset connection timeout idle %s\nexit\nexit\n", objName2, objName1, object));
            return stringBuffer.toString();
        } else {
            return "";
        }
    }


    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
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

    public void formatFullAddress(String[] arr, List<String> list, StringBuilder sb) {
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

        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin(String.format("object-group %s ", existsServiceName));
            return dto;
        }

        if (!createObjFlag) {
            return dto;
        }

        if (serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setObjectFlag(false);
            String str = ProtocolUtils.getProtocolByValue(Integer.valueOf(serviceDTOList.get(0).getProtocol()));
            dto.setJoin(str.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "ip" : str);
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        String name = getServiceName(serviceDTOList);
        dto.setName(name);
        dto.setJoin(String.format("object-group %s ", name));
        sb.append(String.format("object-group service %s \n", name));

        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                sb.append(String.format("service-object %s ", protocolString));
                if (StringUtils.isNotBlank(service.getType())) {
                    sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                }
                sb.append("\n");
            } else {

                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append(String.format("service-object %s\n", protocolString));
                    continue;
                }

                String[] ports = service.getDstPorts().split(",");
                for (String port : ports) {
                    if (PortUtils.isPortRange(port)) {
                        sb.append(String.format("service-object %s range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    } else if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        //2019-03-18 修改：如果端口为any，显示保持为空
                        sb.append(" ");
                    } else {
                        sb.append(String.format("service-object %s %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    }
                }
            }
        }
        sb.append("exit\n");
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = sdf.parse(startTime);
            endDate = sdf.parse(endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("yyyyMMddHHmm");
        String finstartTime=dst.format(startDate).substring(0,4);
        String finendTime=dst.format(endDate).substring(0,4);
        String finstartTimemonth=dst.format(startDate).substring(4,6);
        String finendTimemonth=dst.format(endDate).substring(4,6);

        String name=finstartTime+"_"+finstartTimemonth+"-"+finendTime+"_"+finendTimemonth;



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

    public static void main(String[] args) throws UnknownHostException {
        SecurityCiscoASAForHuaRui ciscoASA = new SecurityCiscoASAForHuaRui();
       /* PolicyObjectDTO addressDTO = ciscoASA.generateAddressObject("192.168.201.112,192.168.201.113-192.168.115.115,172.16.11.0/24", "A20190428001",true);
        System.out.println("创建地址对象: \n" + addressDTO.getCommandLine() + " \n 衔接内容：" +addressDTO.getJoin());

        PolicyObjectDTO addressDTO1 = ciscoASA.generateAddressObject("192.168.201.112,192.168.201.113-192.168.115.115,172.16.11.0/24", "A20190428001", false);
        System.out.println("直接引用地址: \n");
        for(String str : addressDTO1.getCommandLineList()) {
            System.out.println(str);
        }*/


        /*CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        dto.setIdleTimeout(2);
        String commandLine = ciscoASA.composite(dto);
        System.out.println("commandline:\n" + commandLine);*/
        InetAddress local = InetAddress.getLocalHost();
        InetAddress remote = InetAddress.getByName("www.itcast.cn");
        System.out.println("本机的IP地址：" + local.getHostAddress());
        System.out.println("itcast的IP地址：" + remote.getHostAddress());
        System.out.println("itcast的主机名为：" + remote.getHostName());

    }
}
