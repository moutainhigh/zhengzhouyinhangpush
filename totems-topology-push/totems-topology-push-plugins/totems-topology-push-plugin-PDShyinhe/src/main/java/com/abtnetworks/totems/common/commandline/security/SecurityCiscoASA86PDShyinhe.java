package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.commandline.util.LineNameUtils;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.utils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:38
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_86, type = PolicyEnum.SECURITY)
public class SecurityCiscoASA86PDShyinhe extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA86PDShyinhe.class);

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

        dto.setSpecialExistObject(cmdDTO.getSpecialExistObject());
        log.info("思科 ASA 8.6 dto is" + JSONObject.toJSONString(dto, true));

        String commandLine = composite(dto);

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        //思科特殊处理，在进行回滚时，使用的是整个策略，而不是名称
        generatedDto.setPolicyName(commandLine);
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
            sb.append("changeto context system \n");
            sb.append("changeto context " + dto.getVsysName() + "\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();

        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();


        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());

        // 记录创建对象名称
        recordCreateAddrAndServiceObjectName(dto, srcAddress, dstAddress, serviceObject);

        String description = "";
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            description = dto.getDescription();
        }

        String commandLine = commonLine(srcAddress, dstAddress, serviceObject, dto,description);

        return commandLine;
    }


    //提取公共, 供思科 ASA 2个版本使用
    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                             CommandlineDTO dto,String description) {

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
        // 记录时间对象
        recordCreateTimeObjectName(dto, time);

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
        //是否置顶
        Boolean isSetTop = false;
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
                isSetTop = true;
            }
        }


        //对象式命令行 或，服务、源、目的地址都是建对象或复用的
        if (createObjFlag || (serviceObject.isObjectFlag() && srcAddress.isObjectFlag() && dstAddress.isObjectFlag())) {
            if(!isSetTop && !AliStringUtils.isEmpty(description)){
                sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
            }
            sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");
            if(isSetTop && !AliStringUtils.isEmpty(description)){
                sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && !srcAddress.isObjectFlag() && !dstAddress.isObjectFlag()) {
            //不建对象，但服务对象是复用的，源、目的地址都是直接写内容
            for (int i = 0; i < srcAddressList.size(); i++) {
                for (int j = 0; j < dstAddressList.size(); j++) {
                    if(!isSetTop && !AliStringUtils.isEmpty(description)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                    sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(i), dstAddressList.get(j)));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    if(isSetTop && !AliStringUtils.isEmpty(description)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
            //不建对象，服务、源地址是复用的，目的地址直接写内容
            for (int j = 0; j < dstAddressList.size(); j++) {
                if(!isSetTop && !AliStringUtils.isEmpty(description)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
                sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddressList.get(j)));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
                if(isSetTop && !AliStringUtils.isEmpty(description)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，服务、目的地址是复用，源地址直接写内容
            for (int j = 0; j < srcAddressList.size(); j++) {
                if(!isSetTop && !AliStringUtils.isEmpty(description)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
                sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(j), dstAddress.getJoin()));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
                if(isSetTop && !AliStringUtils.isEmpty(description)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
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
                    if(!isSetTop && !AliStringUtils.isEmpty(description)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                    sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin()));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    if(isSetTop && !AliStringUtils.isEmpty(description)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                    continue;
                }

                String[] dstPorts = service.getDstPorts().split(",");
                List<String> dstPortList = formatFullPort(dstPorts);
                for (int j = 0; j < dstPortList.size(); j++) {
                    if(!isSetTop && !AliStringUtils.isEmpty(description)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin(), dstPortList.get(j)));
                    if(isSetTop && !AliStringUtils.isEmpty(description)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
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
                                    if(!isSetTop && !AliStringUtils.isEmpty(description)){
                                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                                    }
                                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), srcPortList.get(j), dstAddressList.get(m), dstPortList.get(n)));
                                    if (time != null) {
                                        sb.append(time.getJoin());
                                    }
                                    sb.append("\n");
                                    if(isSetTop && !AliStringUtils.isEmpty(description)){
                                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int m = 0; m < dstAddressList.size(); m++) {
                            if(!isSetTop && !AliStringUtils.isEmpty(description)){
                                sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                            }
                            sb.append(String.format("access-list %s %s extended %s %s %s %s",
                                    businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), dstAddressList.get(m)));
                            if(isSetTop && !AliStringUtils.isEmpty(description)){
                                sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                            }

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

    /**
     * 记录创建对象的名称
     *
     * @param dto
     * @param srcAddressObject
     * @param dstAddressObject
     * @param serviceObject
     */
    protected void recordCreateAddrAndServiceObjectName(CommandlineDTO dto, PolicyObjectDTO srcAddressObject,
                                                        PolicyObjectDTO dstAddressObject, PolicyObjectDTO serviceObject) {
        List<String> addressObjectNameList = new ArrayList<>();
        List<String> addressObjectGroupNameList = new ArrayList<>();
        List<String> serviceObjectNameList = new ArrayList<>();
        List<String> serviceObjectGroupNameList = new ArrayList<>();

        if (null != srcAddressObject && srcAddressObject.isObjectFlag() && srcAddressObject.isGroup() && StringUtils.isNotBlank(srcAddressObject.getName())) {
            addressObjectGroupNameList.add(srcAddressObject.getName());
        }else if (null != srcAddressObject && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getName())){
            addressObjectNameList.add(srcAddressObject.getName());
        }
        if (null != dstAddressObject && dstAddressObject.isObjectFlag() && dstAddressObject.isGroup() && StringUtils.isNotBlank(dstAddressObject.getName())) {
            addressObjectNameList.add(dstAddressObject.getName());
        }else if (null != dstAddressObject && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getName())){
            addressObjectNameList.add(dstAddressObject.getName());
        }

        if (null != serviceObject && serviceObject.isObjectFlag() && serviceObject.isGroup() && StringUtils.isNotBlank(serviceObject.getName())) {
            serviceObjectGroupNameList.add(serviceObject.getName());
        }else if (null != serviceObject && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getName())){
            serviceObjectNameList.add(serviceObject.getName());
        }

        dto.setAddressObjectNameList(addressObjectNameList);
        dto.setAddressObjectGroupNameList(addressObjectGroupNameList);
        dto.setServiceObjectNameList(serviceObjectNameList);
        dto.setServiceObjectGroupNameList(serviceObjectGroupNameList);
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        //地址为空，表示any
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            return dto;
        }

        //复用对象非空，则直接复用
        if (refObjectDTO != null) {
            if (refObjectDTO.getObjectTypeEnum().equals(DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT)) {
                dto.setJoin("object-group " + refObjectDTO.getRefName() + " ");
            } else {
                dto.setJoin("object " + refObjectDTO.getRefName() + " ");
            }

            dto.setName(refObjectDTO.getRefName());
            return dto;
        }

        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        // 是创建对象
        if (createObjFlag) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1) {
                objName = LineNameUtils.getIPNameKevin(0, arr[0], true, null, null, 1, 1);
                sb.append(getAddressObject(arr[0], objName, null));
                dto.setJoin("object " + objName);
                sb.substring(0,sb.length()-1);
                dto.setGroup(false);
            } else {
                //创建对象、对象组
                List<String> objectJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
                        String refObjName = LineNameUtils.getIPNameKevin(0, ip, true, null, null, 1, 1);
//                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        sb.append(getAddressObject(ip, refObjName, null));
                        objectJoinList.add("network object " + refObjName);
                    } else {
                        objectJoinList.add("network host " + ip);
                    }
                }

                //建地址组，去引用哪些对象
                sb.append(String.format("object-group network %s\n", objName));
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }
                dto.setJoin("object-group " + objName + " ");
                dto.setGroup(true);
                sb.substring(0,sb.length()-1);
                sb.append("exit\n");
            }

            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s \n", objName));
            dto.setObjectFlag(true);
        } else {
            //直接显示内容
            for (String ip : arr) {
                getAddressObject(ip, "", list);
            }
            dto.setCommandLineList(list);
            dto.setObjectFlag(false);
        }

        return dto;
    }

    public String getAddressObject(String ipAddress, String name, List<String> list) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(name)) {
            sb.append(String.format("object network %s\n", name));
        }

        String fullStr = "";
        if (IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            fullStr = String.format("range %s %s", start, end);
        } else if (IpUtils.isIPSegment(ipAddress)) {
            //获取ip
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            //获取网段数
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            //获取网段的ip
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            //将ip和mask转二进制后，进行与计算，得到十进制的子网段ip地址
            String ipDecimal = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(ip), IpUtils.getBinaryIp(mask));
            fullStr = String.format("subnet %s %s", ipDecimal, mask);
        } else {
            fullStr = String.format("host %s", ipAddress);
        }

        sb.append(fullStr + "\n\n");

        if (list != null) {
            list.add(fullStr);
        }

        return sb.toString();
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, RefObjectDTO refObjectDTO ){
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (refObjectDTO != null) {
            dto.setObjectFlag(true);
            if (refObjectDTO.getObjectTypeEnum().equals(DeviceObjectTypeEnum.SERVICE_GROUP_OBJECT)) {
                dto.setJoin(String.format("object-group %s ", refObjectDTO.getRefName()));
            } else {
                dto.setJoin(String.format("object %s ", refObjectDTO.getRefName()));
            }
            return dto;
        }

        if(!createObjFlag){
            return dto;
        }

        if(serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setObjectFlag(false);
            String str = ProtocolUtils.getProtocolByValue(Integer.parseInt(serviceDTOList.get(0).getProtocol()));
            dto.setJoin(str.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"ip":str);
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        StringBuilder nameSb = new StringBuilder();
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.parseInt(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            String setName = LineNameUtils.getServiceNameKevin(null, protocolString, service.getDstPorts(), null);
            nameSb.append("_").append(setName);
        }
        if (nameSb.length()>0){
            nameSb.deleteCharAt(0);
        }

//        String name = getServiceName(serviceDTOList);
        String name = nameSb.toString().replaceAll("\"","");
        dto.setName(name);
        dto.setJoin(String.format("object-group %s ", name));
        // 设置当前创建的为服务组
        dto.setGroup(true);
        sb.append(String.format("object-group service %s \n", name));

        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.parseInt(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                sb.append(String.format("service-object %s ", protocolString));
                if(StringUtils.isNotBlank(service.getType())){
                    sb.append(String.format("%d ", Integer.parseInt(service.getType())));
                }
                sb.append("\n");
            }else{

                if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append(String.format("service-object %s\n", protocolString));
                    continue;
                }

                String[] ports = service.getDstPorts().split(",");
                for(String port : ports){
                    if(PortUtils.isPortRange(port)) {
                        sb.append(String.format("service-object %s destination range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    } else if(port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                        //2019-03-18 修改：如果端口为any，显示保持为空
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
        return dto;
    }


    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityCiscoASA86PDShyinhe ciscoASA = new SecurityCiscoASA86PDShyinhe();
        String jsonStr = "{\n" +
                "\t\"action\":\"PERMIT\",\n" +
                "\t\"businessName\":\"cisco4\",\n" +
                "\t\"ciscoEnable\":false,\n" +
                "\t\"ciscoInterfaceCreate\":true,\n" +
                "\t\"ciscoInterfacePolicyName\":\"DaiWaiMange_in\",\n" +
                "\t\"createObjFlag\":true,\n" +
                "\t\"description\":\"fa\",\n" +
                "\t\"deviceUuid\":\"27d387ec3f354107ae23598e62fe248d\",\n" +
                "\t\"dstIp\":\"164.12.36.3\",\n" +
                "\t\"dstItf\":\"KaiFaRenYuan\",\n" +
                "\t\"dstItfAlias\":\"KaiFaRenYuan\",\n" +
                "\t\"dstZonePriority\":0,\n" +
                "\t\"existDstAddressList\":[],\n" +
                "\t\"existServiceNameList\":[],\n" +
                "\t\"existSrcAddressList\":[],\n" +
                "\t\"hasVsys\":false,\n" +
                "\t\"moveSeatEnum\":\"FIRST\",\n" +
                "\t\"mustCreateFlag\":false,\n" +
                "\t\"name\":\"cisco4\",\n" +
                "\t\"restDstAddressList\":[],\n" +
                "\t\"restServiceList\":[],\n" +
                "\t\"restSrcAddressList\":[],\n" +
                "\t\"serviceList\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"dstPorts\":\"8080\",\n" +
                "\t\t\t\"protocol\":\"6\",\n" +
                "\t\t\t\"srcPorts\":\"any\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dstPorts\":\"4-55\",\n" +
                "\t\t\t\"protocol\":\"17\",\n" +
                "\t\t\t\"srcPorts\":\"any\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"specialExistObject\":{\n" +
                "\t\t\"existDstAddressList\":[],\n" +
                "\t\t\"existPostDstAddressList\":[],\n" +
                "\t\t\"existPostServiceNameList\":[],\n" +
                "\t\t\"existPostSrcAddressList\":[],\n" +
                "\t\t\"existServiceNameList\":[],\n" +
                "\t\t\"existSrcAddressList\":[],\n" +
                "\t\t\"restDstAddressList\":[],\n" +
                "\t\t\"restPostDstAddressList\":[],\n" +
                "\t\t\"restPostServiceList\":[],\n" +
                "\t\t\"restPostSrcAddressList\":[],\n" +
                "\t\t\"restServiceList\":[],\n" +
                "\t\t\"restSrcAddressList\":[]\n" +
                "\t},\n" +
                "\t\"srcIp\":\"1.1.1.1-1.1.1.10,192.168.23.6,172.16.1.36-172.16.1.105,161.24.2.0/24\",\n" +
                "\t\"srcItf\":\"DaiWaiMange\",\n" +
                "\t\"srcItfAlias\":\"DaiWaiMange\",\n" +
                "\t\"srcZonePriority\":0,\n" +
                "\t\"topFlag\":false,\n" +
                "\t\"vsys\":false,\n" +
                "\t\"vsysName\":\"\"\n" +
                "}\n";
        CommandlineDTO cmdDTO = JSONObject.parseObject(jsonStr, CommandlineDTO.class);

        String cmd = ciscoASA.generateCommandline(cmdDTO);
        System.out.println(cmd);

    }
}
