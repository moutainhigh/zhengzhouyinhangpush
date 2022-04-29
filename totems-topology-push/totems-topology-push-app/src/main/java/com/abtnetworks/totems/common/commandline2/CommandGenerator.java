package com.abtnetworks.totems.common.commandline2;

import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.commandline2.constant.CommandConstant;
import com.abtnetworks.totems.common.commandline2.constant.DeviceModelProperties;
import com.abtnetworks.totems.common.commandline2.dto.CommandDTO;
import com.abtnetworks.totems.common.commandline2.util.CommandUtils;
import com.abtnetworks.totems.common.commandline2.util.CustomStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.ACTION;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.ADDRESS_GROUP_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.ADDRESS_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.DESCRIPTION;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.DST_ADDRESS_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.INDEX;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.IPV4_HOST;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.IPV4_MASK;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.IPV4_RANGE;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.IPV6_HOST;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.MASK;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.POLICY_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.PORT;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.PORT_END;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.PORT_START;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.SERVICE_GROUP_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.SERVICE_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.SRC_ADDRESS_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.TIME_FROM;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.TIME_NAME;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.TIME_TO;
import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.VSYS_NAME;
import static org.apache.commons.lang3.StringUtils.LF;

/**
 * @author zc
 * @date 2020/01/07
 */
@Slf4j
@Component
public class CommandGenerator extends SecurityPolicyGenerator {

    @Resource
    protected DeviceModelProperties deviceModelProperties;

    protected DeviceModelProperties.CommandTemplate getCommandTemplate(String modelNumber) {
        Assert.assertNotNull(modelNumber);
        return deviceModelProperties.getModelMap().get(modelNumber);
    }

    /**
     * 前置命令行
     * @param commandDTO
     * @return
     */
    protected String generatePreCommandLine(CommandDTO commandDTO) {
        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        String cmd = "";
        if(commandDTO.getIsVsys() != null && commandDTO.getIsVsys()) {
            if (StringUtils.contains(commandTemplate.getPreVsysCommand(),VSYS_NAME)) {
                cmd = commandTemplate.getPreVsysCommand()
                        .replace(VSYS_NAME, commandDTO.getVsysName());
            }
        } else {
            cmd = StringUtils.defaultString(commandTemplate.getPreCommand(), cmd);
        }
        return cmd + LF;
    }

    /**
     * 后置命令行
     * @param commandDTO
     * @return
     */
    protected String generatePostCommandLine(CommandDTO commandDTO) {
        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        return StringUtils.defaultString(commandTemplate.getPostCommand() + LF, LF);
    }

    /**
     * 黑洞路由生成命令行
     * @param commandDTO
     * @return
     */
    public String routerCreateCommandLine(CommandDTO commandDTO) {
        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        DeviceModelProperties.RouterCommand routerCommand = commandTemplate.getRouterCommand();
        Assert.assertNotNull("型号[" + commandTemplate.getModelNumber() + "]的路由对象不能为空", routerCommand);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generatePreCommandLine(commandDTO));

        String ipAddr = commandDTO.getIpAddress();
        List<String> allIpList = CommandUtils.ipConvert(ipAddr);
        Map<String, List<String>> typeIpListMap = CommandUtils.groupByIpType(allIpList);

        if (typeIpListMap.containsKey(CommandConstant.IPV4_HOST)) {
            String routerIpv4Create = routerCommand.getIpv4Create();
            Assert.assertNotNull("routerIpv4Create字段不能为空",routerIpv4Create );
            typeIpListMap.get(CommandConstant.IPV4_HOST).forEach(ip -> stringBuilder.append(routerIpv4Create.replace(IPV4_HOST, ip)).append(LF));
        }
        if (typeIpListMap.containsKey(CommandConstant.IPV6_HOST)) {
            String routerIpv6Create = routerCommand.getIpv6Create();
            Assert.assertNotNull("routerIpv6Create字段不能为空",routerIpv6Create );
            typeIpListMap.get(CommandConstant.IPV4_HOST).forEach(ip -> stringBuilder.append(routerIpv6Create.replace(IPV6_HOST, ip)).append(LF));
        }

        stringBuilder.append(generatePostCommandLine(commandDTO));
        return stringBuilder.toString();
    }

    /**
     * 黑洞路由删除命令行
     * @param commandDTO
     * @return
     */
    public String routerDeleteCommandLine(CommandDTO commandDTO) {
        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        DeviceModelProperties.RouterCommand routerCommand = commandTemplate.getRouterCommand();
        Assert.assertNotNull("型号[" + commandTemplate.getModelNumber() + "]的路由对象不能为空", routerCommand);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generatePreCommandLine(commandDTO));

        String ipAddr = commandDTO.getIpAddress();
        List<String> allIpList = CommandUtils.ipConvert(ipAddr);
        Map<String, List<String>> typeIpListMap = CommandUtils.groupByIpType(allIpList);

        if (typeIpListMap.containsKey(CommandConstant.IPV4_HOST)) {
            String routerIpv4Delete = routerCommand.getIpv4Delete();
            Assert.assertNotNull("routerIpv4Delete字段不能为空",routerIpv4Delete );
            typeIpListMap.get(CommandConstant.IPV4_HOST).forEach(ip -> stringBuilder.append(routerIpv4Delete.replace(IPV4_HOST, ip)).append(LF));
        }
        if (typeIpListMap.containsKey(CommandConstant.IPV6_HOST)) {
            String routerIpv6Delete = routerCommand.getIpv6Delete();
            Assert.assertNotNull("routerIpv6Delete字段不能为空",routerIpv6Delete );
            typeIpListMap.get(CommandConstant.IPV4_HOST).forEach(ip -> stringBuilder.append(routerIpv6Delete.replace(IPV6_HOST, ip)).append(LF));
        }

        stringBuilder.append(generatePostCommandLine(commandDTO));
        return stringBuilder.toString();
    }


//---------------------------------------- router handle end -------------------------------------------
//---------------------------------------- address handle start -------------------------------------------

    /**
     * 地址对象生成
     * @param commandDTO
     * @return 地址对象
     */
    protected List<PolicyObjectDTO> addressObjectListCreate(CommandDTO commandDTO) {
        if (StringUtils.isNotEmpty(commandDTO.getExistedAddressName())) {
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setName(commandDTO.getExistedAddressName());
            return Collections.singletonList(dto);
        }
        if (StringUtils.isEmpty(commandDTO.getIpAddress())) {
            return Collections.emptyList();
        }
        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        DeviceModelProperties.AddressCommand addressCommand = commandTemplate.getAddressCommand();
        Assert.assertNotNull("型号[" + commandTemplate.getModelNumber() + "]的地址对象不能为空", addressCommand);

        List<PolicyObjectDTO> policyObjectDTOS = addressObjectListCreate(commandDTO, addressCommand);

        if (StringUtils.isNotEmpty(addressCommand.getAddressGroup()) && policyObjectDTOS.size() > 1) {
            log.debug("存在地址组命令行且地址对象为多个时");
            return addressGroupCreate(policyObjectDTOS, addressCommand);
        }
        return policyObjectDTOS;

    }

    /**
     * 生成地址对象列表和对象对应的命令行列表
     * @param commandDTO
     * @param addressCommand
     * @return
     */
    protected List<PolicyObjectDTO> addressObjectListCreate(CommandDTO commandDTO, DeviceModelProperties.AddressCommand addressCommand) {
        List<String> allIpList = Arrays.asList(commandDTO.getIpAddress().split(","));
        List<Pair<String, String>> pairList = CommandUtils.assignIpType(allIpList);
        return pairList.stream()
                .map(pair -> {
                    String setName = String.format("%s_AO_%s", commandDTO.getTicket(), IdGen.getRandomNumberString());
                    String cmd = addressObjectSingleHandle(addressCommand, pair, setName);
                    PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
                    policyObjectDTO.setName(setName);
                    policyObjectDTO.setCommandLine(cmd);
                    return policyObjectDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 单个地址对象的命令行处理
     * @param addressCommand
     * @param typeAndIp
     * @param setName
     * @return
     */
    protected String addressObjectSingleHandle(DeviceModelProperties.AddressCommand addressCommand,
                                             Pair<String, String> typeAndIp,
                                             String setName) {
        String command;
        String result;
        String address = typeAndIp.getValue();
        switch (typeAndIp.getKey()) {
            case IPV4_HOST:
                command = addressCommand.getIpv4Host();
                if (StringUtils.isEmpty(command)) {
                    throw new IllegalArgumentException("不支持IP类型:" + typeAndIp.getKey());
                }
                result = command.replace(ADDRESS_NAME,setName).replace(IPV4_HOST, address) + LF;
                break;
            case IPV4_RANGE:
                command = addressCommand.getIpv4Range();
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                result = StringUtils.replaceOnce(command, IPV4_HOST, startIp).replace(ADDRESS_NAME,setName).replace(IPV4_HOST, endIp) + LF;
                break;
            case IPV4_MASK:
                command = addressCommand.getIpv4Mask();
                String ip = IpUtils.getIpFromIpSegment(address);
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                result = command.replace(ADDRESS_NAME,setName).replace(IPV4_HOST, ip).replace(MASK, maskBit) + LF;
                break;
            default:
                throw new IllegalArgumentException("不支持的类型:" + typeAndIp.getKey());
        }
        return result;
    }

    /**
     * 地址对象组的创建
     * @param policyObjectDTOS
     * @return
     */
    protected List<PolicyObjectDTO> addressGroupCreate(List<PolicyObjectDTO> policyObjectDTOS, DeviceModelProperties.AddressCommand addressCommand) {
        String groupCmd = addressCommand.getAddressGroup();
        String groupName = StringUtils.join(policyObjectDTOS.stream().map(PolicyObjectDTO::getName).collect(Collectors.toList()), "_");
        String command = StringUtils.join(policyObjectDTOS.stream().map(PolicyObjectDTO::getCommandLine).collect(Collectors.toList()), "");

        groupCmd = customKeyWordConsumer(groupCmd, ADDRESS_GROUP_NAME, groupName);
        groupCmd = customKeyWordConsumer(groupCmd, ADDRESS_NAME, policyObjectDTOS);
        groupCmd = CustomStringUtils.clearSpecialText(groupCmd);

        groupCmd = customIndexConsumer(groupCmd);

        command += groupCmd;
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setCommandLine(command);
        policyObjectDTO.setName(groupName);

        return Collections.singletonList(policyObjectDTO);
    }

//---------------------------------------- address handle end -------------------------------------------
//---------------------------------------- service handle start -------------------------------------------

    /**
     * 服务对象生成
     * @param commandDTO
     * @return
     */
    protected List<PolicyObjectDTO> serviceObjectListCreate(CommandDTO commandDTO) {
        if (StringUtils.isNotEmpty(commandDTO.getExistedServiceName())) {
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setName(commandDTO.getExistedServiceName());
            return Collections.singletonList(dto);
        }
        List<ServiceDTO> serviceList = commandDTO.getServiceList();
        if (serviceList == null || serviceList.size() == 0) {
            return Collections.emptyList();
        }

        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        DeviceModelProperties.ServiceCommand serviceCommand = commandTemplate.getServiceCommand();
        Assert.assertNotNull("型号[" + commandTemplate.getModelNumber() + "]的服务对象不能为空", serviceCommand);

        List<PolicyObjectDTO> policyObjectDTOS = serviceObjectListCreate(commandDTO, serviceCommand);

        if (StringUtils.isNotEmpty(serviceCommand.getServiceGroup()) && policyObjectDTOS.size() > 1) {
            log.debug("存在服务组命令行且服务对象为多个时");
            return serviceGroupCreate(policyObjectDTOS, serviceCommand);
        }
        return policyObjectDTOS;
    }

    /**
     * 处理服务对象列表
     * @param commandDTO
     * @param serviceCommand
     * @return
     */
    protected List<PolicyObjectDTO> serviceObjectListCreate(CommandDTO commandDTO, DeviceModelProperties.ServiceCommand serviceCommand) {
        //todo 后续的优化   (xxx)+
//        List<ServiceDTO> serviceDTOList = commandDTO.getServiceList();
//        for (ServiceDTO serviceDTO : serviceDTOList) {
//            String dstPorts = serviceDTO.getDstPorts();
//            String protocol = serviceDTO.getProtocol();
//            String cmd = "";
//            switch (protocol) {
//                case "0":
//                    log.debug("不做处理");
//                    break;
//                case "1":
//                    log.debug("不做处理");
//                    break;
//                case "6":
//                    cmd = serviceCommand.getTcpPortRange();
//                    CustomStringUtils.KeyWordFrequency frequencyPort = CustomStringUtils.keyWordDetect(cmd, PORT);
//                    CustomStringUtils.KeyWordFrequency frequencyPortRange = CustomStringUtils.keyWordDetect(cmd, PORT_START);
//
//                    break;
//                default:
//                    log.error("未知的协议[{}]", protocol);
//                    break;
//            }
//        }
        return commandDTO.getServiceList().stream()
                .map(serviceDTO -> {
                    List<ServiceDTO> serviceDTOS = new ArrayList<>();
                    String dstPorts = serviceDTO.getDstPorts();
                    String protocol = serviceDTO.getProtocol();
                    if (StringUtils.contains(dstPorts,",")) {
                        log.debug("逗号分隔的端口拆分");
                        Arrays.stream(dstPorts.split(","))
                                .forEach(ports -> {
                                    ServiceDTO service = new ServiceDTO();
                                    service.setProtocol(protocol);
                                    service.setDstPorts(ports);
                                    serviceDTOS.add(service);
                                });
                    } else {
                        serviceDTOS.add(serviceDTO);
                    }
                    return serviceDTOS;
                })
                .flatMap(Collection::stream)
                .map(serviceDTO -> {
                    PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
                    String setName = CommandUtils.getServiceName(serviceDTO);
                    policyObjectDTO.setName(setName);
                    if (setName .contains("_")) {
                        log.debug("代表有端口");
                        String cmd = serviceObjectSingleHandle(serviceCommand,
                                Pair.of(serviceDTO.getProtocol(),serviceDTO.getDstPorts()),
                                setName);
                        policyObjectDTO.setCommandLine(cmd);
                    }
                    return policyObjectDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 单个服务对象的处理
     * @param serviceCommand
     * @param typeAndPorts
     * @param setName
     * @return
     */
    protected String serviceObjectSingleHandle(DeviceModelProperties.ServiceCommand serviceCommand,
                                               Pair<String, String> typeAndPorts,
                                               String setName) {
        String type = typeAndPorts.getKey();
        String ports = typeAndPorts.getValue();
        String[] portArray = ports.split("-");
        String cmd = "";
        switch (type) {
            case "0":
                log.debug("不做处理");
                break;
            case "1":
                log.debug("不做处理");
                break;
            case "6":
                cmd = serviceCommand.getTcpPortRange();
                cmd = customKeyWordConsumer(cmd, SERVICE_NAME, setName);
                if (portArray.length == 1) {
                    cmd = customKeyWordConsumer(cmd, PORT, portArray[0]);
                } else if (portArray.length == 2) {
                    cmd = CustomStringUtils.biKeyWordConsumer(cmd, PORT_START,portArray[0],PORT_END,portArray[0]);
                }
                break;
            case "17":
                cmd = serviceCommand.getUdpPortRange();
                cmd = customKeyWordConsumer(cmd, SERVICE_NAME, setName);
                if (portArray.length == 1) {
                    cmd = customKeyWordConsumer(cmd, PORT, portArray[0]);
                } else if (portArray.length == 2) {
                    cmd = CustomStringUtils.biKeyWordConsumer(cmd, PORT_START,portArray[0],PORT_END,portArray[0]);
                }
                break;
            default:
                log.error("未知的协议[{}]", type);
                break;
        }
        return cmd;
    }

    /**
     * 服务组的处理
     * @param policyObjectDTOS
     * @param serviceCommand
     * @return
     */
    protected List<PolicyObjectDTO> serviceGroupCreate(List<PolicyObjectDTO> policyObjectDTOS, DeviceModelProperties.ServiceCommand serviceCommand) {
        String groupCmd = serviceCommand.getServiceGroup();
        String groupName = StringUtils.join(policyObjectDTOS.stream().map(PolicyObjectDTO::getName).collect(Collectors.toList()), "_");
        String command = StringUtils.join(policyObjectDTOS.stream().map(PolicyObjectDTO::getCommandLine).collect(Collectors.toList()), "");

        groupCmd = customKeyWordConsumer(groupCmd, SERVICE_GROUP_NAME, groupName);
        groupCmd = customKeyWordConsumer(groupCmd, SERVICE_NAME, policyObjectDTOS);
        groupCmd = CustomStringUtils.clearSpecialText(groupCmd);
        groupCmd = customIndexConsumer(groupCmd);

        command += groupCmd;
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setCommandLine(command);
        policyObjectDTO.setName(groupName);

        return Collections.singletonList(policyObjectDTO);
    }

//---------------------------------------- service handle end -------------------------------------------
//---------------------------------------- timer handle start -------------------------------------------

    /**
     * 创建时间对象
     * @param commandDTO
     * @return
     */
    protected List<PolicyObjectDTO> timeObjectListCreate(CommandDTO commandDTO) {
        String startTime = commandDTO.getStartTime();
        String endTime = commandDTO.getEndTime();
        if (StringUtils.isAnyEmpty(startTime, endTime)) {
            return Collections.emptyList();
        }

        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        DeviceModelProperties.TimeCommand timeCommand = commandTemplate.getTimeCommand();
        Assert.assertNotNull("型号[" + commandTemplate.getModelNumber() + "]的服务对象不能为空", timeCommand);

        String command = timeCommand.getAbsoluteRange();
        String timeFormat = timeCommand.getAbsoluteTimeFormat();
        String setName = String.format("%s_TR_%s", commandDTO.getTicket(), IdGen.getRandomNumberString());
        String timeFrom = TimeUtils.transformDateFormat(startTime, TimeUtils.EUROPEAN_TIME_FORMAT, timeFormat);
        String timeTo = TimeUtils.transformDateFormat(endTime, TimeUtils.EUROPEAN_TIME_FORMAT, timeFormat);
        if (StringUtils.isAnyEmpty(timeFrom, timeTo)) {
            throw new IllegalArgumentException("时间转换异常，可能是时间格式不正确");
        }
        String replacedCmd = command.replace(TIME_NAME,setName).replace(TIME_FROM, timeFrom).replace(TIME_TO, timeTo);

        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(setName);
        policyObjectDTO.setCommandLine(replacedCmd);
        return Collections.singletonList(policyObjectDTO);
    }

//---------------------------------------- timer handle end -------------------------------------------
//---------------------------------------- security policy handle start -------------------------------------------

    public String secPolicyCommandLine(CommandDTO commandDTO) {

        DeviceModelProperties.CommandTemplate commandTemplate = getCommandTemplate(commandDTO.getModelNumber());
        if (commandTemplate == null) {
            log.error("型号[{}]的命令行不存在", commandDTO.getModelNumber());
            return null;
        }

        log.debug("创建源地址对象、目的地址对象、服务对象、时间对象");
        commandDTO.setIpAddress(commandDTO.getSrcIp());
        List<PolicyObjectDTO> srcAddressList = addressObjectListCreate(commandDTO);
        commandDTO.setIpAddress(commandDTO.getDstIp());
        List<PolicyObjectDTO> dstAddressList = addressObjectListCreate(commandDTO);
        commandDTO.setIpAddress(null);
        List<PolicyObjectDTO> serviceList = serviceObjectListCreate(commandDTO);
        List<PolicyObjectDTO> timeList = timeObjectListCreate(commandDTO);

        log.debug("拼接对象生成时的命令行");
        StringBuilder sb = new StringBuilder();
        srcAddressList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        dstAddressList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        serviceList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        timeList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));

        log.debug("开始安全策略的命令行生成");
        String policyCommand = commandTemplate.getSecurityPolicyCommand();

        policyCommand = customKeyWordConsumer(policyCommand, POLICY_NAME, commandDTO.getName());
        policyCommand = customKeyWordConsumer(policyCommand, ACTION, commandDTO.getAction());
        policyCommand = customKeyWordConsumer(policyCommand, DESCRIPTION, commandDTO.getDescription());

        policyCommand = customKeyWordConsumer(policyCommand, SRC_ADDRESS_NAME, srcAddressList);
        policyCommand = customKeyWordConsumer(policyCommand, DST_ADDRESS_NAME, dstAddressList);
        policyCommand = customKeyWordConsumer(policyCommand, SERVICE_NAME, serviceList);
        policyCommand = customKeyWordConsumer(policyCommand, TIME_NAME, timeList);
        policyCommand = CustomStringUtils.clearSpecialText(policyCommand);

        sb.append(policyCommand);
        return sb.toString();
    }

//---------------------------------------- security policy handle end -------------------------------------------

    /**
     * 单个关键字的消费
     * @param command
     * @param keyWord
     * @param filler
     * @return
     */
    private static String customKeyWordConsumer(String command, String keyWord, String filler) {
        if (command.contains(keyWord)) {
            command = CustomStringUtils.keyWordConsumer(command, keyWord, filler);
        } else {
            log.error("命令行[{}]未找到关键字[{}]", command, keyWord);
        }
        return command;
    }

    /**
     * 对象名的消费
     * @param command
     * @param keyWord
     * @param policyObjectDTOS
     * @return
     */
    private static String customKeyWordConsumer(String command, String keyWord, List<PolicyObjectDTO> policyObjectDTOS) {
        if (policyObjectDTOS.size() > 0) {
            List<String> nameList = policyObjectDTOS.stream().map(PolicyObjectDTO::getName)
                    .collect(Collectors.toList());
            if (command.contains(keyWord)) {
                command = CustomStringUtils.keyWordConsumer(command, keyWord, (ArrayList<String>) nameList);
                if (nameList.size() > 0) {
                    throw new IllegalArgumentException("对象名没有消费完全，命令行不匹配");
                }
            } else {
                log.error("命令行[{}]未找到关键字[{}]", command, keyWord);
            }
        }
        return command;
    }

    /**
     * 关键字的特殊处理，例如有索引的需要自生成的
     * @param command
     * @return
     */
    private static String customIndexConsumer(String command) {
        if (command.contains(INDEX)) {
            int count = StringUtils.countMatches(command, INDEX);
            List<String> intString = IntStream.rangeClosed(0, count-1).boxed().map(String::valueOf).collect(Collectors.toList());
            command = CustomStringUtils.keyWordConsumer(command, INDEX, (ArrayList<String>) intString);
        }
        return command;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return null;
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        CommandDTO commandDTO = new CommandDTO();
        BeanUtils.copyProperties(dto, commandDTO);
        commandDTO.setModelNumber("USG6000");
        commandDTO.setTicket(dto.getName());
        return secPolicyCommandLine(commandDTO);
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return null;
    }
}
