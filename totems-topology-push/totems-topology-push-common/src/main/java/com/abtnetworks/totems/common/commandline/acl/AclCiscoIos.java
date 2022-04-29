package com.abtnetworks.totems.common.commandline.acl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @desc 思科acl策略命令行生成规则
 * @date 2021/4/20 14:17
 */
@Service
@Log4j2
public class AclCiscoIos extends SecurityPolicyGenerator implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("Cisco_ACL cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
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
        dto.setCiscoInterfaceCreate(settingDTO.isCreateCiscoItfRuleList());
        dto.setCiscoInterfacePolicyName(settingDTO.getCiscoItfRuleListName());
        // 思科acl 默认不创建对象
        dto.setCreateObjFlag(false);
        dto.setOutBound(settingDTO.isOutBound());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        dto.setSpecialExistObject(cmdDTO.getSpecialExistObject());
        log.info("思科 ACL dto is" + JSONObject.toJSONString(dto, true));

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
        // 判断当前策略所需的策略id的个数是和可用的id个数做对比,如果所需的id个数大于可用的则直接提示,没有可用的ruledid

        //创建服务、地址对象
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName());

        String description = "";
        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            description = dto.getDescription();
        }
        return commonLine(srcAddress, dstAddress, dto, description);
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            List<String> list = new ArrayList<>();
            list.add("any");
            dto.setCommandLineList(list);
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
                String hostIp = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                //根据掩码获取反掩码的ip格式
                String ipDecimal = IpUtils.getInverseMaskIpByMaskBit(maskBit);
                //获取网段的ip
                String mask = IpUtils.getMaskByMaskBit(maskBit);
                //将ip和mask转二进制后，进行与计算，得到十进制的子网段ip地址
                hostIp = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(hostIp), IpUtils.getBinaryIp(mask));
                fullStr = String.format("%s %s ", hostIp, ipDecimal);
                list.add(fullStr);
            } else if (IpUtils.isIPRange(address)) {
                List<String> ips = IPUtil.convertRangeToSubnet(address);
                for (String item : ips) {
                    //获取ip
                    String hostIp = IpUtils.getIpFromIpSegment(item);
                    //获取网段数
                    String maskBit = IpUtils.getMaskBitFromIpSegment(item);
                    //根据掩码获取反掩码的ip格式
                    String ipDecimal = IpUtils.getInverseMaskIpByMaskBit(maskBit);
                    //获取网段的ip
                    String mask = IpUtils.getMaskByMaskBit(maskBit);
                    //将ip和mask转二进制后，进行与计算，得到十进制的子网段ip地址
                    hostIp = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(hostIp), IpUtils.getBinaryIp(mask));
                    fullStr = String.format("%s %s", hostIp, ipDecimal);
                    list.add(fullStr);
                }
            } else {
                fullStr = String.format("host %s", address);
                list.add(fullStr);
            }
        }
    }

    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress,
                             CommandlineDTO dto, String description) {

        List<String> srcAddressList = srcAddress.getCommandLineList();
        List<String> dstAddressList = dstAddress.getCommandLineList();

        String ticket = dto.getName();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        String ruleListName = dto.getRuleListName();

        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        StringBuilder sb = new StringBuilder();
        sb.append("configure terminal\n\n");

        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }
        boolean createRule = false;
        // 如果策略名称为空 则需要去新建策略集,新建策略集名称用工单号
        if (StringUtils.isBlank(ruleListName)) {
            createRule = true;
        }

        if (createRule) {
            sb.append(String.format("ip access-list extended %s", ticket));
        }else {
            sb.append(String.format("ip access-list extended %s", ruleListName));
        }

        sb.append("\n");
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
                                sb.append(String.format("%s %s %s %s %s %s %s", CommonConstants.POLICY_ID, dto.getAction().toLowerCase(), protocolString,
                                        srcAddressList.get(i), srcPortList.get(j), dstAddressList.get(m), dstPortList.get(n)));
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
                        sb.append(String.format("%s %s %s %s %s", CommonConstants.POLICY_ID, dto.getAction().toLowerCase(), protocolString,
                                srcAddressList.get(i), dstAddressList.get(m)));
                        if (time != null) {
                            sb.append(time.getJoin());
                        }
                        sb.append("\n");
                    }
                }
            }
        }

        if (!AliStringUtils.isEmpty(description)) {
            sb.append(String.format("remark %s", description));
            sb.append("\n");
        }
        sb.append("exit \n");
        if(createRule){
            boolean outBound = dto.isOutBound();
            if(outBound){
                sb.append(String.format("interface %s", dto.getDstItf())).append(StringUtils.LF);
                sb.append(String.format("ip access-group %s out",ticket)).append(StringUtils.LF);
            }else{
                sb.append(String.format("interface %s", dto.getSrcItf())).append(StringUtils.LF);
                sb.append(String.format("ip access-group %s in",ticket)).append(StringUtils.LF);
            }
        }

        sb.append("end\nwrite\n");

        return sb.toString();
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


    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }
}
