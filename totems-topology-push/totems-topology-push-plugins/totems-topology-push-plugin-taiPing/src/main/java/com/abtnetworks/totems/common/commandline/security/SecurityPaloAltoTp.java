package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: paloalto策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service

@CustomCli(value = DeviceModelNumberEnum.PALO_ALTO, type = PolicyEnum.SECURITY)
public class SecurityPaloAltoTp extends SecurityPolicyGenerator implements PolicyGenerator {
    private static Logger logger = LoggerFactory.getLogger(SecurityPaloAltoTp.class);

    private final String IN_KEY = "in";

    private final String BASE_COMMANDLINE = "set rulebase security rules ";

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

        log.info("dto is" + JSONObject.toJSONString(dto, true));
        String commandLine = composite(dto);
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(dto.getServiceList()) && dto.getServiceList().size() == 1 ){
            int protocolNum = Integer.valueOf(dto.getServiceList().get(0).getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            // TODO icmp不生成命令行，待确认格式后再调整
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                return sb.toString();
            }
        }

        if (dto.isVsys()) {
            sb.append("set system setting target-vsys " + dto.getVsysName() + "\n");
        }
        sb.append("configure\n");

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();

        //创建服务、地址对象
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getServiceName(), dto.getRestServiceList(), dto.getExistServiceNameList());
        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());
        //先定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s", srcAddressObject.getCommandLine()));
        }

        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s", dstAddressObject.getCommandLine()));
        }

        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s", serviceObject.getCommandLine()));
        }
        if (timeObject != null) {
            sb.append(timeObject.getCommandLine());
        }

        String srcZone = StringUtils.isEmpty(dto.getSrcZone())? PolicyConstants.POLICY_STR_VALUE_ANY : dto.getSrcZone();
        String dstZone = StringUtils.isEmpty(dto.getDstZone())? PolicyConstants.POLICY_STR_VALUE_ANY : dto.getDstZone();

        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" ").append("from ").append(srcZone).append(CommonConstants.LINE_BREAK);
        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" ").append("to ").append(dstZone).append(CommonConstants.LINE_BREAK);

        // 源地址对象
        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" source ");
        if (!CollectionUtils.isEmpty(srcAddressObject.getExistAddressNameList())) {
            if(srcAddressObject.getExistAddressNameList().size() == 1){
                sb.append(srcAddressObject.getExistAddressNameList().get(0)).append(" ");
            } else {
                sb.append("[ ");
                for (String srcName : srcAddressObject.getExistAddressNameList()) {
                    sb.append(srcName).append(" ");
                }
                sb.append("]");
            }
        } else {
            sb.append(PolicyConstants.POLICY_STR_VALUE_ANY);
        }
        sb.append(CommonConstants.LINE_BREAK);

        // 目的地址对象
        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" destination ");
        if (!CollectionUtils.isEmpty(dstAddressObject.getExistAddressNameList())) {
            if(dstAddressObject.getExistAddressNameList().size() == 1){
                sb.append(dstAddressObject.getExistAddressNameList().get(0)).append(" ");
            } else {
                sb.append("[ ");
                for (String dstName : dstAddressObject.getExistAddressNameList()) {
                    sb.append(dstName).append(" ");
                }
                sb.append("]");
            }
        } else {
            sb.append(PolicyConstants.POLICY_STR_VALUE_ANY);
        }
        sb.append(CommonConstants.LINE_BREAK);

        // 服务对象
        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" service ");
        if (!CollectionUtils.isEmpty(serviceObject.getExistServiceNameList())) {
            if(serviceObject.getExistServiceNameList().size() == 1){
                sb.append(serviceObject.getExistServiceNameList().get(0)).append(" ");
            } else {
                sb.append("[ ");
                for (String serviceName : serviceObject.getExistServiceNameList()) {
                    String[] array = StringUtils.split(serviceName, SYMBOL1);
                    for (String serviceArr: array) {
                        sb.append(serviceArr).append(" ");
                    }
                }
                sb.append("]");
            }
        } else {
            sb.append(PolicyConstants.POLICY_STR_VALUE_ANY);
        }
        sb.append(CommonConstants.LINE_BREAK);

        //时间对象
        if (timeObject != null && StringUtils.isNotBlank(timeObject.getJoin())) {
            sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" schedule ");
            sb.append(timeObject.getJoin());
            sb.append(CommonConstants.LINE_BREAK);
        }

        List<String> policyApplications = dto.getPolicyApplications();
        List<String> policyUserNames = dto.getPolicyUserNames();
        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName());
        sb.append(" application ");
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(policyApplications)){

            if(policyApplications.size()>1){
                sb.append("[ ");
                for (String policyApplication : policyApplications) {
                    sb.append(policyApplication).append(" ");
                }
                sb.append("]");
            }else{
                sb.append(policyApplications.get(0));
            }

            sb.append(CommonConstants.LINE_BREAK);
        }else{
            sb.append("any\n");
        }

        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(policyUserNames)){
            sb.append(BASE_COMMANDLINE).append(dto.getBusinessName());
            sb.append(" source-user ");
            if(policyUserNames.size()>1) {
                sb.append("[ ");
                for (String policyUserName : policyUserNames) {
                    sb.append(policyUserName).append(" ");
                }
                sb.append("]");
            }else{
                sb.append(policyUserNames.get(0));
            }
            sb.append(CommonConstants.LINE_BREAK);
        }
        if (StringUtils.isNotBlank(dto.getAction())) {
            if (PolicyConstants.POLICY_STR_PERMISSION_PERMIT.equalsIgnoreCase(dto.getAction())) {
                sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" ").append("action allow");
            } else {
                sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" ").append("action deny");
            }
        }
        sb.append(CommonConstants.LINE_BREAK);

        sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" ").append("log-start yes").append(CommonConstants.LINE_BREAK);
        if(StringUtils.isNotBlank(dto.getDescription())){
            sb.append(BASE_COMMANDLINE).append(dto.getBusinessName()).append(" description ").append(dto.getDescription()).append(CommonConstants.LINE_BREAK);
        }
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if (StringUtils.isNotBlank(swapRuleNameId)) {
                sb.append("move rulebase security rules ").append(dto.getBusinessName()).append(" before ").append(swapRuleNameId).append(CommonConstants.LINE_BREAK);
            }
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            sb.append("move rulebase security rules ").append(dto.getBusinessName()).append(" after ").append(swapRuleNameId).append(CommonConstants.LINE_BREAK);
            //暂无
        } else if (MoveSeatEnum.FIRST.getCode() == moveSeatCode) {
            sb.append("move rulebase security rules ").append(dto.getBusinessName()).append(" top").append(CommonConstants.LINE_BREAK);
        }
        return sb.toString();
    }




    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }


    /**
     * 地址整体和离散复用前置判读
     *
     * @param ipAddress
     * @param ticket
     * @param existsAddressName
     * @return
     */
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, List<String> exitAddressNameList, List<String> restAddressList) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(existsAddressName) && CollectionUtils.isEmpty(exitAddressNameList)) {
            exitAddressNameList.add(existsAddressName);
        } else {
            for (String srcAddress : restAddressList) {
                sb.append(generateAddressObject(srcAddress, ticket, exitAddressNameList));
            }
        }
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setExistAddressNameList(exitAddressNameList);
        policyObjectDTO.setObjectFlag(true);
        policyObjectDTO.setCommandLine(sb.toString());
        return policyObjectDTO;
    }
    private final static String SYMBOL1 = ",";
    private final static String SYMBOL2 = "-";
    private final static String SYMBOL3 = "/";
    /**
     * 生成地址对象
     *
     * @param ipAddress         ip地址
     * @param ticket            主题名称
     * @param existsAddressName 已存在的地址对象
     * @return 地址对象
     */
    private String generateAddressObject(String ipAddress, String ticket, List<String> existsAddressName) {
        //判断 已存在的地址对象 是否为空

        StringBuilder sb = new StringBuilder();
        StringBuilder nameSB = new StringBuilder();

        //循环获取ip
        String[] ipArray = StringUtils.split(ipAddress, SYMBOL1);
        for (String ip : ipArray) {
            String ipName;
            if (ip.contains(SYMBOL2)) {
                //判断 ip段
                ipName = String.format("%s",getUrlAddressPrefix(ip));
//                ipName = String.format("%s_ao2_%s", ticket, IdGen.getRandomNumberString());
                String line = String.format("edit address %s\nset ip-range %s\n", ipName, ip);
                sb.append(line);
            } else if (ip.contains(SYMBOL3)) {
                //判断是 子网
//                ipName = String.format("%s_ao3_%s", ticket, IdGen.getRandomNumberString());
                ipName = String.format("%s",getUrlAddressPrefix(ip));
                sb.append(String.format("edit address %s\nset ip-netmask %s\n", ipName, ip));
            } else {
                //单个ip
//                ipName = String.format("%s_ao1_%s", ticket, IdGen.getRandomNumberString());
                ipName = String.format("%s",getUrlAddressPrefix(ip));
                sb.append(String.format("edit address %s\nset ip-netmask %s/32\n", ipName, ip));
            }
            sb.append("top\n");
            if (StringUtils.isBlank(nameSB.toString())) {
                nameSB.append(String.format("%s", ipName));
            } else {
                nameSB.append(String.format(",%s", ipName));
            }

        }
        existsAddressName.add(nameSB.toString());

        return sb.toString();
    }

    public String getUrlAddressPrefix(String ip){
        /*根据IP获取前缀：
         * 1、单个IP前缀默认为 “H” 10.10.1.1
         * 2、IP范围前缀默认为 “R” 10.10.1.1-10.10.1.4
         * 3、IP子网前缀默认为 “H” 10.10.1.1/32
         * */
        StringBuilder name = new StringBuilder();

        if(StringUtils.isNotBlank(ip)){
            String[] rangeIp = ip.split("-");
            String[] subnetIp = ip.split("/");
            String prefix = "";
            if(rangeIp.length > 1){
                //根据 "-" 分割，如果长度大于1表示传递IP为范围IP
                prefix = "R";
                name.append(String.format("%s%s",prefix,ip));
            } else if(subnetIp.length > 1){
                //根据 “/” 分割，如果长度大于1表示传递IP为IP子网
                prefix = "H";
                name.append(String.format("%s%s_%s",prefix,subnetIp[0],subnetIp[1]));
            } else {
                prefix = "H";
                name.append(String.format("%s%s",prefix,ip));
            }
        }

        return name.toString();
    }

    /**
     * 整体到离散复用
     *
     * @param serviceDTOList
     * @param serviceName
     * @param restServiceList
     * @param existsServiceName
     * @return
     */
    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String serviceName, List<ServiceDTO> restServiceList, List<String> existsServiceName) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(serviceName) && CollectionUtils.isEmpty(existsServiceName)) {
            existsServiceName.add(serviceName);
        } else {
            sb.append(generateServiceObject(restServiceList, existsServiceName));

        }
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        dto.setExistServiceNameList(existsServiceName);
        return dto;
    }

    /**
     * 生成服务对象
     *
     * @param serviceList       服务列表
     * @param existsServiceName 已存在的服务对象
     * @return 服务对象
     */
    public String generateServiceObject(List<ServiceDTO> serviceList, List<String> existsServiceName) {

        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isEmpty(serviceList)) {
            return "";
        }

        for (ServiceDTO service : serviceList) {
            String setName = getServiceName(service);

            int protocolNum = Integer.valueOf(service.getProtocol());

            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                continue;
            }

            //建对象
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                // icmp直接跳过，暂不考虑
                continue;
            } else {
                String[] dstPorts = StringUtils.split(service.getDstPorts(), ",");
                //协议相同，端口逗号分开
                if (dstPorts != null && dstPorts.length > 0) {
                    for (String dstPort : dstPorts) {
                        String[] split = dstPort.split("-");
                        if(split.length > 1){
                            sb.append(String.format("edit service %s%s protocol %s\nset port %s\n",protocolString,dstPort,protocolString,dstPort));
                        }else{
                            if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append(String.format("set service %s protocol %s\nset port %s-%s\n",setName,protocolString,"0","65535"));
                            } else if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("edit service %s%s protocol %s\nset port %s-%s\n",protocolString,setName,protocolString,startPort,endPort));
                            } else {
                                sb.append(String.format("edit service %s%s protocol %s\nset port %s\n",protocolString,dstPort,protocolString,dstPort));
                            }
                        }
                        sb.append("top\n");
                    }
                    if (sb.lastIndexOf(",") > 0) {
                        sb = sb.deleteCharAt(sb.lastIndexOf(","));
                    }
                    sb.append("\n");
                } else {
                    sb.append(String.format("%s dst-port 0-65535\n", protocolString));
                    sb.append("top\n");
                }

            }
            existsServiceName.add(setName);
        }

        return sb.toString();
    }

    public String getServiceName(ServiceDTO dto) {
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
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("%s-%s", startPort, endPort));
            } else {
                sb.append(String.format("%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
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

        String startDate = formatDateString(startTime, TimeUtils.OPPO_TIME_FORMAT);
        String endDate = formatDateString(endTime, TimeUtils.OPPO_TIME_FORMAT);
        sb.append("set schedule ").append(name).append(" schedule-type non-recurring ").append(startDate).append("-").append(endDate).append(CommonConstants.LINE_BREAK);
        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setJoin(name);
        return dto;
    }

    private String formatDateString(String timeString, String format) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, format);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityPaloAltoTp securityPaloaltoTp = new SecurityPaloAltoTp();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<String> restSrc = new ArrayList<>();
        restSrc.add("1.3.2.5-1.3.2.9");
        //restSrc.add("1.3.2.55");
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("1.3.2.56");
//        restDst.add("10.80.32.14-10.80.32.23");
        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceList = new ArrayList<>();
        /*ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("8888-8890");
        serviceDTO.setSrcPorts("");
        serviceList.add(serviceDTO);*/
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
//        serviceDTO2.setDstPorts("");
        serviceList.add(serviceDTO2);
        dto.setRestServiceList(serviceList);
        dto.setServiceList(null);
        dto.setDescription("dfd");
        String commandLine = securityPaloaltoTp.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}