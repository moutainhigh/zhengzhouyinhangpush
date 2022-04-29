package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 华为u6000策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.USG6000, type = PolicyEnum.SECURITY)
public class SecurityUsg6000Tp extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityUsg6000Tp.class);

    private final String SOURCE_ADDRESS = " source-address";

    private final String DESTINATION_ADDRESS = " destination-address";

    private static Integer index = 0;

    private final int MAX_NAME_LENGTH = 63;

    private final int HOUR_SECOND = 60 * 60;

    @Override
    public String generate(CmdDTO cmdDTO) {
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
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
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
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        if (dto.isVsys()) {
            sb.append("switch vsys " + dto.getVsysName() + "\n");
            sb.append("system-view\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    private String createCommandLine(CommandlineDTO dto) {
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getRestSrcAddressList(), dto.getName(), SOURCE_ADDRESS, createObjFlag, dto.getExistSrcAddressList());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getRestDstAddressList(), dto.getName(), DESTINATION_ADDRESS, createObjFlag, dto.getExistDstAddressList());
        List<String> existServiceNames = dto.getExistServiceNameList();
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), createObjFlag, existServiceNames);

        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();
        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (timeObject != null) {
            sb.append(String.format("%s\n", timeObject.getCommandLine()));
        }

        sb.append("security-policy\n");
        String name = dto.getBusinessName();
        sb.append(String.format("rule name %s\n", name));

        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append(String.format(" description %s\n", dto.getDescription()));
        }

        if (StringUtils.isNotBlank(dto.getSrcZone())) {
            sb.append(String.format(" source-zone %s\n", dto.getSrcZone()));
        }
        if (StringUtils.isNotBlank(dto.getDstZone())) {
            sb.append(String.format(" destination-zone %s\n", dto.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
            sb.append(srcAddressObject.getJoin());
        } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
            sb.append(dstAddressObject.getJoin());
        } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (CollectionUtils.isNotEmpty(existServiceNames)) {
            for (String existServiceName : existServiceNames) {
                sb.append(String.format(" service %s\n", existServiceName));
            }

        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        if (dto.getIdleTimeout() != null) {
            sb.append("long-link enable\n");
            sb.append(String.format("long-link aging-time %d\n", dto.getIdleTimeout() / HOUR_SECOND));

        }
        if (timeObject != null) {
            sb.append(timeObject.getJoin());
        }
        sb.append(String.format(" action %s\n", dto.getAction().toLowerCase()));

        //rule之后添加quit
        sb.append("quit\n");

        //移动位置， 默认在最后 modify by zy 20200618 转向下发那边直接移动，不要命令行中拼接

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            if (DeviceModelNumberEnum.USG6000.getKey().equalsIgnoreCase(dto.getModelNumber().getKey())) {
                sb.append(String.format("rule move %s top\n", name));
            }
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)){
                sb.append(String.format("rule move %s %s %s\n", name, dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }else{
                sb.append("\n");
            }

        }


        sb.append("return\n");

        return sb.toString();
    }

    private String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleName()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleName、mergeField 有为空的");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField = mergeDTO.getMergeField();

        //正式开始编辑
        StringBuilder sb = new StringBuilder();

        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getRestSrcAddressList(), dto.getName(), SOURCE_ADDRESS, createObjFlag, dto.getExistSrcAddressList());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getRestDstAddressList(), dto.getName(), DESTINATION_ADDRESS, createObjFlag, dto.getExistDstAddressList());

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), createObjFlag, dto.getExistServiceNameList());


        if (mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("security-policy\n");
        sb.append(String.format("rule name %s\n", ruleName));
        if (mergeField.equals(PolicyConstants.SRC)) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else {
                sb.append(srcAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.DST)) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else {
                sb.append(dstAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.SERVICE)) {
            sb.append(serviceObject.getCommandLine());
        }
        sb.append("quit\n");
        sb.append("return\n");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(List<String> ipAddress, String ticket, String ipPrefix, boolean createObjFlag, List<String> existsAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (CollectionUtils.isEmpty(ipAddress) && CollectionUtils.isEmpty(existsAddressName)) {
            dto.setJoin("");
            dto.setCommandLine("");
            return dto;
        }
        StringBuffer nameBuffer = new StringBuffer();
        if (CollectionUtils.isEmpty(ipAddress) && CollectionUtils.isNotEmpty(existsAddressName)) {
            for (String name : existsAddressName) {
                nameBuffer.append(ipPrefix + " address-set " + name + " \n");
            }
            dto.setJoin(nameBuffer.toString());
            dto.setCommandLine("");
            return dto;
        }

        dto.setObjectFlag(true);

        // 是创建对象
        if (true) {
            StringBuilder sb = new StringBuilder();

            int index = 0;
            for (String address : ipAddress) {
                sb.append("ip address-set ");
                String ipObject = "";
                String addressCmd = "";
                if (IpUtils.isIPRange(address)) {
                    ipObject = String.format("R%s", address);
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("address " + index + " range %s %s\n", startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {

                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    ipObject = String.format("N%s_%s", ip, maskBit);
                    addressCmd = String.format("address " + index + " %s mask %s\n", ip, maskBit);
                } else {
                    addressCmd = String.format("address " + index + " %s mask 32\n", address);
                    ipObject = String.format("H%s", address);
                }
                existsAddressName.add(ipObject);
                sb.append(ipObject).append(" type object\n");
                sb.append(" ").append(addressCmd);
                sb.append("quit\n");
            }

            dto.setCommandLine(sb.toString());

            for (String name : existsAddressName) {
                nameBuffer.append(ipPrefix + " address-set " + name + " \n");
            }
            dto.setJoin(nameBuffer.toString());
        }
        return dto;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, List<String> existsServiceNames) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (CollectionUtils.isEmpty(serviceDTOList) && CollectionUtils.isEmpty(existsServiceNames)) {
            return dto;
        }

//        if (StringUtils.isNotBlank(existsServiceName)) {
//            dto.setObjectFlag(true);
//            dto.setJoin(String.format("service %s \n", existsServiceName));
//            return dto;
//        }

//        if (serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//            createObjFlag = false;
//        }

        dto.setObjectFlag(createObjFlag);

        StringBuilder sb = new StringBuilder();
        if (createObjFlag) {
            List<String> createName = new LinkedList<>();
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
                //当协议为any，则策略中不填service即可
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    return dto;
                }
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    existsServiceNames.add(protocolString.toLowerCase());
                } else {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    sb.append(createOtherProtocol(protocolString, srcPorts, dstPorts, createObjFlag, existsServiceNames));

                }
            }

        }

        for (String existsServiceName: existsServiceNames) {
            dto.setJoin(dto.getJoin()+String.format(" service %s \n", existsServiceName));
        }

        dto.setCommandLine(sb.toString());
        return dto;
    }


    /**
     * 创建协议对象| 内容
     *
     * @param protocolString 协议：TCP、UDP、ANY
     * @param srcPorts       源端口数组
     * @param dstPorts       目的端口数组
     * @return
     * @description 创建对象时，端口为any，也需要显示具体的数字0-65535，不创建对象时，可省略any的端口信息
     */
    private String createOtherProtocol(String protocolString, String[] srcPorts, String[] dstPorts, boolean createObjFlag, List<String> createNames) {
        StringBuffer sb = new StringBuffer();
        if (dstPorts.length > 0) {
            for (String dstPort : dstPorts) {
                String name;
                if (StringUtils.isEmpty(dstPort) || dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    name = protocolString.toLowerCase();
                } else if (PortUtils.isPortRange(dstPort)) {
                    name = String.format("%s:%s", protocolString.toUpperCase(), dstPort);
                    sb.append(String.format("ip service-set %s type object\n", name));
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);

                    sb.append(String.format("service 0 protocol %s destination-port %s to %s \n", protocolString.toLowerCase(), startPort, endPort));
                    sb.append("quit\n");
                } else {
                    name = String.format("%s:%s", protocolString.toUpperCase(), dstPort);
                    sb.append(String.format("ip service-set %s type object\n", name));
                    sb.append(String.format("service 0 protocol %s destination-port %s  \n", protocolString.toLowerCase(), dstPort));
                    sb.append("quit\n");
                }
                createNames.add(name);


            }

        } else {
            createNames.add(protocolString.toLowerCase());
        }


        return sb.toString();
    }

    /**
     * 创建imcp协议
     **/
    private String createIcmpProtocol(Integer icmpType, Integer icmpCode, boolean createObjFlag) {
        StringBuffer sb = new StringBuffer();
        sb.append("service");
        if (createObjFlag) {
            //创建对象时，有需要序号
            sb.append(" " + 0);
        }
        sb.append(" protocol icmp ");
        if (icmpType != null) {
            sb.append(String.format("icmp-type %d %d ", icmpType, icmpCode));
        }
        sb.append("\nquit\n");
        return sb.toString();
    }

    /**
     * 生成时间区间对象
     *
     * @param startTimeString 开始时间字符串
     * @param endTimeString   结束时间字符串
     * @return 时间区间对象
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if (AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

        String objName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(objName);
        dto.setCommandLine(String.format("time-range %s \nabsolute-range %s to %s \nquit\n", objName, startTime, endTime));
        dto.setJoin(String.format(" time-range %s\n", objName));
        return dto;
    }


    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HUAWEI_TIME_FORMAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<String> srcList = new LinkedList<>();
        srcList.add("1.3.2.5");
        dto.setRestSrcAddressList(srcList);
        List<String> dstList = new LinkedList<>();
        dstList.add("1.5.3.5");
        dto.setRestSrcAddressList(srcList);
        dto.setRestDstAddressList(dstList);
        SecurityUsg6000Tp usg6000 = new SecurityUsg6000Tp();
        String commandLine = usg6000.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
