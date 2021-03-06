package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:38
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO, type = PolicyEnum.SECURITY)
public class SecurityCiscoASAForzheShang extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASAForzheShang.class);

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
        //????????????????????????????????????????????????????????????????????????????????????
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

        //???????????????????????????
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName(),dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName(),dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());

        return commonLine(srcAddress, dstAddress, serviceObject, dto);
    }


    //????????????, ????????? ASA 2???????????????
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
        //????????????
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

        //???????????????????????????????????????????????????????????????
        String line1 = "";

        //????????????????????????????????????
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
                    logger.info("?????????????????????????????????????????????ID???");
                }
                line1 = String.format("line %d", lineNum + 1);
            } else if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
                line1 = "line 1";
            }
        }


        //?????????????????? ????????????????????????????????????????????????????????????
        if (createObjFlag || (serviceObject.isObjectFlag() && srcAddress.isObjectFlag() && dstAddress.isObjectFlag())) {
            sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && !srcAddress.isObjectFlag() && !dstAddress.isObjectFlag()) {
            //????????????????????????????????????????????????????????????????????????????????????
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
            //???????????????????????????????????????????????????????????????????????????
            for (int j = 0; j < dstAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddressList.get(j)));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //????????????????????????????????????????????????????????????????????????
            for (int j = 0; j < srcAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(j), dstAddress.getJoin()));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //??????????????????????????????????????????????????????????????????
            for (ServiceDTO service : dto.getServiceList()) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                //??????????????????any
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
            //??????????????????????????????????????? ?????????????????????srcPort?????????????????????????????????
            for (ServiceDTO service : dto.getServiceList()) {
                //????????????Id ???????????????
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                List<String> srcPortList = new ArrayList<>();
                List<String> dstPortList = new ArrayList<>();

                // ????????????
                if (!protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && !"ip".equals(protocolString)) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    srcPortList = formatFullPort(srcPorts);
                    dstPortList = formatFullPort(dstPorts);
                }
                //dstPortList ????????????????????????tcp???udp??????
                if (dstPortList != null && dstPortList.size() > 0) {
                    //?????????????????????
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

                            //icmp??????????????????????????????
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


        //??????????????????????????????
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

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName,String ipSystem) {
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


        // ???????????????
        if (createObjFlag) {
            String objName = null;
            if (StringUtils.isNotBlank(ipSystem)) {
                objName = dealIpSystemName(ipSystem);
            } else {
                objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            }
            dto.setName(objName);
            if (ipAddress.contains(",")) {
                sb.append(String.format("object-group network %s \n", objName));
                formatFullAddress(arr, list, sb);
                sb.append("exit\n");
            }

            dto.setCommandLine(sb.toString());
            dto.setJoin("object-group " + dto.getName() + " ");
            dto.setObjectFlag(true);

        } else {
            //??????????????????
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
                //??????ip
                String ip = IpUtils.getIpFromIpSegment(address);
                //???????????????
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                //???????????????ip
                String mask = IpUtils.getMaskByMaskBit(maskBit);
                //???ip???mask???????????????????????????????????????????????????????????????ip??????
                String ipDecimal = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(ip), IpUtils.getBinaryIp(mask));
                fullStr = String.format(" %s %s ", ipDecimal, mask);
                sb.append(String.format("network-object %s \n", fullStr));
                list.add(fullStr);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                //???????????????????????????????????????
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
                //2019-03-18 ????????????????????????any?????????????????????
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


        StringBuilder sb = new StringBuilder();

        String name = getServiceName(serviceDTOList);
        dto.setName(name);

        dto.setJoin(String.format("object-group %s ", name));

        if (serviceDTOList.size() == 1) { }
        else {
            sb.append(String.format("object-group service %s \n", name));


            for (ServiceDTO service : serviceDTOList) {

                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    sb.append(String.format("service-object %s destination eq ", protocolString));
                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                    }
                    sb.append("\n");
                } else {

                    if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        sb.append(String.format("service-object %s destination eq\n", protocolString));
                        continue;
                    }

                    String[] ports = service.getDstPorts().split(",");
                    for (String port : ports) {
                        if (PortUtils.isPortRange(port)) {
                            sb.append(String.format("service-object %s destination eq range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                        } else if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            //2019-03-18 ????????????????????????any?????????????????????
                            sb.append(" ");
                        } else {
                            sb.append(String.format("service-object %s destination eq %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                        }
                    }
                }
            }


            sb.append("exit\n");
            dto.setCommandLine(sb.toString());
            dto.setObjectFlag(true);

        }
        return dto;
    }


    /**
     * ??????????????????
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


    /**
     * ?????????????????? ???????????????????????????????????????
     * @param ipSystem
     */
    private String dealIpSystemName(String ipSystem) {
        String setName = ipSystem;
        // ???????????????????????????????????????2?????????
        setName = strSub(setName, getMaxNameLength(), "GB2312");
        // ????????????????????????
        int len = 0;
        try {
            len = setName.getBytes("GB2312").length;
        } catch (Exception e) {
            log.error("???????????????????????????");
        }
        if (len > getMaxNameLength() - 7) {
            setName = strSub(setName, getMaxNameLength() - 7, "GB2312");
        }
        setName = String.format("%s_%s", setName, DateUtils.getDate().replace("-", "").substring(2));
        return setName;
    }

    /**
     * ????????????????????????????????????
     * ????????????????????????????????????????????????
     * ??????????????????????????? ???????????????????????????????????????????????????????????????????????????????????????????????????
     * @param s
     *            ???????????????
     * @param num
     *            ????????????????????????
     * @return String ?????????????????????

     * @throws
     */
    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            log.error("???????????????????????????");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityCiscoASAForzheShang ciscoASA = new SecurityCiscoASAForzheShang();
//       PolicyObjectDTO addressDTO = ciscoASA.generateAddressObject("192.168.201.112,192.168.201.113-192.168.115.115,172.16.11.0/24", "A20190428001",true);
//        System.out.println("??????????????????: \n" + addressDTO.getCommandLine() + " \n ???????????????" +addressDTO.getJoin());
//
//        PolicyObjectDTO addressDTO1 = ciscoASA.generateAddressObject("192.168.201.112,192.168.201.113-192.168.115.115,172.16.11.0/24", "A20190428001", false);
//        System.out.println("??????????????????: \n");
//        for(String str : addressDTO1.getCommandLineList()) {
//            System.out.println(str);
//        }

        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();


        String commandLine = ciscoASA.composite(dto);


        dto.setSrcIp("12.3.4.7,1.4.5.2");
        dto.setDstIp("2.567.24.8,4.2.4.2");
        dto.setStartTime("2019-12-3 12:2:0");
        dto.setEndTime("2019-12-3 12:2:32");

        List<ServiceDTO> srcport = new ArrayList<>();
//        ServiceDTO service = new ServiceDTO();
//        service.setProtocol("6");
//
//        service.setDstPorts("25,21");
        ServiceDTO service1 = new ServiceDTO();
        service1.setProtocol("17");
//        service.setProtocol("6");
        service1.setDstPorts("23");
        dto.setServiceName("");
//        ServiceDTO service2 = new ServiceDTO();
//        service2.setProtocol("1");
//        srcport.add(service);
        srcport.add(service1);
//        srcport.add(service2);
        dto.setServiceList(srcport);
        System.out.println("commandline:\n" + commandLine);


    }
}
