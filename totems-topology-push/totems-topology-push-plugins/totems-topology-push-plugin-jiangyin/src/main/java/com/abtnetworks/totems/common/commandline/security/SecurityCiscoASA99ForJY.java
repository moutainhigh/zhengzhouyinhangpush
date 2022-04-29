package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @desc    思科ASA9.9版本,支持域名
 * @author liuchanghao
 * @date 2020-11-30 9:26
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_99, type = PolicyEnum.SECURITY)
public class SecurityCiscoASA99ForJY extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA99ForJY.class);
    private final int MAX_NAME_LENGTH = 65;

    private final String srcAddressRef = "src-address-ref";

    private final String dstAddressRef = "dst-address-ref";

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

        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        int iptype=dto.getIpType();
        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();
        // 默认设置IPV4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();

        SecurityCiscoASA99ForJY securityCiscoASA = new SecurityCiscoASA99ForJY();

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getIpType(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getIpType(), dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName(),dto.getIpType());

        String commandLine = commonLine(srcAddress, dstAddress, serviceObject, dto);

        return commandLine;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO, Integer ipType, String ipSystem) {
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
        String objName ;

            objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());


        // 是创建对象
        if (createObjFlag) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1&&!arr[0].contains("-")&&!arr[0].contains("/")) {
                objName = "host"+" "+arr[0];
                dto.setCommandLine(objName);
                dto.setJoin(objName);
            }
            else if(arr.length == 1&&(arr[0].contains("-")||arr[0].contains("/"))){
                sb.append(getAddressObject(arr[0], objName, null,ipType));

                dto.setJoin("object " + objName);
            }
            else {
                //创建对象、对象组
                boolean containsUrl = false;
                List<String> objectJoinList = new ArrayList<>();
                List<String> urlJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        sb.append(getAddressObject(ip, refObjName, null,ipType));
                        objectJoinList.add("network object " + refObjName);
                    } else if(IpUtils.isIP(ip)){
                        objectJoinList.add("network host " + ip);
                    } else {
                        if(IpUtils.isIPv6Subnet(ip)||IpUtils.isIPv6Range(ip))
                        {
                            String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                            sb.append(getAddressObject(ip, refObjName, null,ipType));
                            objectJoinList.add("network object " + refObjName);
                        }
                        else if(IpUtils.isIPv6(ip)) {
                            objectJoinList.add("network host " + ip);
                        }
                        else {
                            containsUrl = true;
                            sb.append(getURLAddressObject(ip, ticket, urlJoinList));
                        }
                    }
                }

                //建地址组，去引用哪些对象
                if(containsUrl){
                    sb.append(String.format("object-group network %s\n", objName));
                } else {
                    sb.append(String.format("object-group network %s\n", objName));
                }
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }
                for (String joinStr : urlJoinList) {
                    sb.append(joinStr + "\n");
                }
                sb.append("exit\n");
                dto.setJoin("object-group " + objName + " ");
            }

            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s \n", objName));
            dto.setObjectFlag(true);
        } else {
            //直接显示内容
//            for (String ip : arr) {
//                getAddressObject(ip, "", list,ipType);
//            }
//            dto.setCommandLineList(list);
//            dto.setObjectFlag(false);
        }

        return dto;
    }


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
            String objectString ="";
            if(StringUtils.isNotEmpty(srcAddress.getJoin())){
                objectString = serviceObject.getJoin().replace(srcAddressRef,srcAddress.getJoin());
            }else{
                objectString = serviceObject.getJoin().replace(srcAddressRef,"");
            }
            if(StringUtils.isNotEmpty(dstAddress.getJoin())){
                objectString = objectString.replace(dstAddressRef,dstAddress.getJoin());
            }else{
                objectString = objectString.replace(dstAddressRef,"");
            }

            sb.append(String.format("access-list %s %s extended %s %s", businessName, line1, dto.getAction().toLowerCase(), objectString ));
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");

        }


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




    public String getAddressObject(String ipAddress, String name, List<String> list, Integer ipType) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(name)) {
            sb.append(String.format("object network %s\n", name));
        }

        String fullStr = "";
        if (IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            fullStr = String.format("range %s %s\n", start, end);
        } else if (IpUtils.isIPSegment(ipAddress)) {
            //获取ip
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            //获取网段数
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            //获取网段的ip
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            //将ip和mask转二进制后，进行与计算，得到十进制的子网段ip地址
            String ipDecimal = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(ip), IpUtils.getBinaryIp(mask));
            fullStr = String.format("subnet %s %s\n", ipDecimal, mask);
        } else if(IpUtils.isIP(ipAddress)){
            fullStr = String.format("host %s\n", ipAddress);
        } else {
                // ipv6
                if (ipAddress.contains("/")) {
                    String startIp=IpUtils.getIpSegmentStartIPv6(ipAddress);
                    String endIp=IpUtils.getIpSegmentMaskIPv6(ipAddress);
                    fullStr = String.format("subnet %s/%s\n", startIp, endIp);
                } else if(ipAddress.contains("-")){
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(ipAddress);
                    String endIp = IpUtils.getRangeEndIPv6(ipAddress);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s\n", ipAddress);
                }

            if(ipType.intValue() == IpTypeEnum.URL.getCode()){
                fullStr = String.format("fqdn %s", ipAddress);
            }
        }

        sb.append(fullStr);
        sb.append("exit\n");
        if (list != null) {
            list.add(fullStr);
        }

        return sb.toString();
    }


    public String getURLAddressObject(String ipAddress, String ticket, List<String> urlJoinList) {
        StringBuilder sb = new StringBuilder();
        String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        if (StringUtils.isNotBlank(objName)) {
            sb.append(String.format("object network %s\n", objName));
        }

        String fullStr = String.format("fqdn %s", ipAddress);
        String groupStr = String.format("network-object object %s", objName);
        sb.append(fullStr + "\n");
        sb.append("exit\n");
        if (urlJoinList != null) {
            urlJoinList.add(groupStr);
        }
        return sb.toString();
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, RefObjectDTO refObjectDTO ,int iptype) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (refObjectDTO != null) {
            dto.setObjectFlag(true);
            if (refObjectDTO.getObjectTypeEnum().equals(DeviceObjectTypeEnum.SERVICE_GROUP_OBJECT)) {
                dto.setJoin(String.format("object-group %s %s %s", refObjectDTO.getRefName(),srcAddressRef,dstAddressRef));
            } else {
                dto.setJoin(String.format("object %s %s %s", refObjectDTO.getRefName(),srcAddressRef,dstAddressRef));
            }
            return dto;
        }


        if (!createObjFlag) {
            return dto;
        }

        boolean singledstport = true;
        String str = ProtocolUtils.getProtocolByValue(Integer.valueOf(serviceDTOList.get(0).getProtocol()));
        if (serviceDTOList.get(0).getDstPorts().contains(",") || serviceDTOList.get(0).getDstPorts().contains("-")) {
            singledstport = false;
        }
        if (serviceDTOList.size() == 1 && singledstport == true) {
            dto.setObjectFlag(false);
            String protocolS = ProtocolUtils.getProtocolByValue(Integer.valueOf(serviceDTOList.get(0).getProtocol())).toLowerCase();
            if (protocolS.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(iptype == IpTypeEnum.IPV4.getCode()) {
                    dto.setJoin(String.format("%s %s %s ", str.toLowerCase(), srcAddressRef, dstAddressRef, serviceDTOList.get(0).getDstPorts()));
                }
                else {
                    dto.setJoin(String.format("%s6 %s %s ", str.toLowerCase(), srcAddressRef, dstAddressRef, serviceDTOList.get(0).getDstPorts()));
                }
            } else {
                if (StringUtils.isNotEmpty(serviceDTOList.get(0).getDstPorts()) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(serviceDTOList.get(0).getDstPorts())) {
                    dto.setJoin(String.format("%s %s %s eq %s", str.toLowerCase(), srcAddressRef, dstAddressRef, serviceDTOList.get(0).getDstPorts()));
                } else {
                    dto.setJoin(String.format("%s %s %s ", str.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "ip" : str.toLowerCase(), srcAddressRef, dstAddressRef));
                }
            }
            return dto;
        }
        boolean isAnyFirst = serviceDTOList.size() == 1 && (StringUtils.isEmpty(serviceDTOList.get(0).getDstPorts())||PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(serviceDTOList.get(0).getDstPorts()));
        if(isAnyFirst){
            dto.setJoin(String.format("%s %s %s", "ip", srcAddressRef, dstAddressRef));
         return dto;
        }


            StringBuilder sb = new StringBuilder();

            String name = getServiceName(serviceDTOList);
            dto.setName(name);
            dto.setJoin(String.format("object-group %s %s %s ", name, srcAddressRef, dstAddressRef));
            sb.append(String.format("object-group service %s \n", name));

            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    if(iptype == IpTypeEnum.IPV4.getCode()){
                        sb.append(String.format("service-object %s ", protocolString));
                    } else {
                        sb.append(String.format("service-object %s6 ", protocolString));
                    }

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
                            sb.append(String.format("service-object %s destination range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                        } else if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
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
    public String getServiceName(List<ServiceDTO> serviceDTOList) {
        StringBuilder nameSb = new StringBuilder();
        int number = 0;

        for(Iterator var4 = serviceDTOList.iterator(); var4.hasNext(); ++number) {
            ServiceDTO dto = (ServiceDTO)var4.next();
            if (number != 0) {
                nameSb.append("-");
            }
            nameSb.append(this.getServiceName(dto));
        }



        String name = nameSb.toString();
        if (name.length() > this.getMaxNameLength()) {
            String shortName = name.substring(0, this.getMaxNameLength() - 4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("-")));
        }

        return name;
    }
    @Override
    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toUpperCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        } else if (protocolString.equalsIgnoreCase("ICMP")) {
            return sb.toString();
        } else if (!dto.getDstPorts().equalsIgnoreCase("any") && !dto.getDstPorts().equals("0-65535")) {
            String[] dstPorts = dto.getDstPorts().split(",");
            String[] var5 = dstPorts;
            int var6 = dstPorts.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String dstPort = var5[var7];
                if (PortUtils.isPortRange(dstPort)) {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
                    sb.append(String.format("-%s-%s", startPort, endPort));
                } else {
                    sb.append(String.format("-%s", dstPort));
                }
            }

            return sb.toString().toUpperCase();
        } else {
            return sb.toString();
        }
    }
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
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityCiscoASA99ForJY cisco = new SecurityCiscoASA99ForJY();

        dto.setSrcIp("230E:360:4A02:32EE:848B:CFF5:CD04:7AFC,240E::3601-240E::36ff,240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100 ");
        dto.setDstIp("240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100,240E::3601-240E::36ff");
        dto.setStartTime("2019-12-3 12:2:0");
        dto.setEndTime("2019-12-3 12:2:32");

        List<ServiceDTO> srcport = new ArrayList<>();
        ServiceDTO service = new ServiceDTO();
//        service.setProtocol("6");
//        service.setDstPorts("");
        dto.setIpType(0);

        dto.setSrcIpSystem("sfs");
//
//        service.setDstPorts("25,21");
        ServiceDTO service1 = new ServiceDTO();
//        ServiceDTO service2 = new ServiceDTO();
        service1.setProtocol("1");
//        service2.setProtocol("17");
//        service1.setDstPorts("323");
//        service2.setDstPorts("");

//        service.setProtocol("6");
        service1.setDstPorts("any");
//        dto.setServiceName("");
//        service2.setDstPorts("67,84");
//        ServiceDTO service2 = new ServiceDTO();
//        service2.setProtocol("1");
//        srcport.add(service);
        srcport.add(service1);
//        srcport.add(service2);

        dto.setServiceList(srcport);
        String commandLine = cisco.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
