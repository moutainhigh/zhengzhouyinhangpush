package com.abtnetworks.totems.common.commandline.security.translation;

import com.abtnetworks.totems.common.commandline.TranslationPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.ObjectConversionTool;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: hw
 * @Date: 2021-1-19 10:57
 */
@Service
public class SecurityHillStoneR5Extended extends SecurityHillStoneR5 implements TranslationPolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityHillStoneR5Extended.class);

    @Override
    public String generateAddressObjectByTranslation(ResultRO<List<NetWorkGroupObjectRO>> netWorkObjectList, Map<String, Object> map) throws Exception {

        StringBuilder sb_network = new StringBuilder();

        if (netWorkObjectList != null && netWorkObjectList.getData() != null && netWorkObjectList.getData().size() != 0) {
            for (NetWorkGroupObjectRO netWork : netWorkObjectList.getData()) {
                if (netWork == null || netWork.getIncludeItems() == null) {
                    continue;
                }

                String ip = "";
                Integer ipType = 0;
                for (IncludeItemsRO includeItem : netWork.getIncludeItems()) {
                    ip += "," + ObjectConversionTool.IncludeItemsROtoIpString(includeItem);
                }
                if (StringUtils.isNotBlank(ip) && ip.startsWith(",")) {
                    ip = ip.substring(1);
                }

                PolicyObjectDTO command_line = generateAddressObject(ip, netWork.getName(),"",true,null, netWork.getName(), ipType,null,0);
                if (command_line == null) {
                    continue;
                }
                sb_network.append(command_line.getCommandLine());
            }
        }

        return sb_network.toString();
    }

    @Override
    public String generateAddressGroupObjectByTranslation(ResultRO<List<NetWorkGroupObjectRO>> netWorkGroupObjectList, Map<String, Object> map) throws Exception {

        StringBuilder sb_networkGroup = new StringBuilder();

        if (netWorkGroupObjectList != null && netWorkGroupObjectList.getData() != null && netWorkGroupObjectList.getData().size() != 0) {
            for (NetWorkGroupObjectRO netWorkGroup : netWorkGroupObjectList.getData()) {
                if (netWorkGroup == null || netWorkGroup.getIncludeGroupNames() == null) {
                    continue;
                }

                String command_line = createAddressGroup(netWorkGroup.getName(), netWorkGroup.getIncludeGroupNames());
                if (StringUtils.isBlank(command_line)) {
                    continue;
                }
                sb_networkGroup.append(command_line);

            }
        }

        return sb_networkGroup.toString();
    }

    @Override
    public String generateTimeObjectByTranslation(ResultRO<List<TimeObjectRO>> timeObjectList, Map<String, Object> map) throws Exception {
        StringBuilder sb_time = new StringBuilder();
        if (timeObjectList != null && timeObjectList.getData() != null && timeObjectList.getData().size() != 0) {
            for (TimeObjectRO timeObjectRO : timeObjectList.getData()) {

                if (timeObjectRO == null || timeObjectRO.getTimeItems() == null) {
                    continue;
                }

                String start = null;
                String end = null;
                for (TimeItemsRO timeItem : timeObjectRO.getTimeItems()) {
                    if (StringUtils.isNoneBlank(timeItem.getStart(), timeItem.getEnd())) {
                        start = TimeUtils.dealDateFormat(timeItem.getStart());
                        end = TimeUtils.dealDateFormat(timeItem.getEnd());
                    }
                }

                if (StringUtils.isNoneBlank(start, end)) {
                    PolicyObjectDTO command_line = generateTimeObject(start, end, timeObjectRO.getName());
                    if (command_line == null) {
                        continue;
                    }
                    sb_time.append(command_line.getCommandLine());
                }
            }
        }

        return sb_time.toString();
    }

    @Override
    public String generateServiceObjectByTranslation(ResultRO<List<ServiceGroupObjectRO>> serviceObjectList, Map<String, Object> map) throws Exception {
        StringBuilder sb_service = new StringBuilder();
        if (serviceObjectList != null && serviceObjectList.getData() != null && serviceObjectList.getData().size() != 0) {

            for (ServiceGroupObjectRO serviceGroupObjectRO : serviceObjectList.getData()) {

                if (serviceGroupObjectRO == null || serviceGroupObjectRO.getIncludeFilterServices() == null) {
                    continue;
                }

                List<ServiceDTO> serviceDTOList = new ArrayList<>();
                for (IncludeFilterServicesRO includeFilterService : serviceGroupObjectRO.getIncludeFilterServices()) {
                    ServiceDTO serviceDTO = ObjectConversionTool.IncludeFilterServicesROtoServiceDTO(includeFilterService);
                    if (serviceDTO == null || serviceDTO.getProtocol() == null) {
                        continue;
                    }
                    serviceDTOList.add(serviceDTO);
                }

                if (serviceDTOList.size() == 0) {
                    continue;
                }

                PolicyObjectDTO command_line = createServiceObject(serviceGroupObjectRO.getName(), serviceDTOList, null);
                if (command_line == null) {
                    continue;
                }
                sb_service.append(command_line.getCommandLine());
            }

        }

        return sb_service.toString();
    }

    @Override
    public String generateServiceGroupObjectByTranslation(ResultRO<List<ServiceGroupObjectRO>> serviceGroupObjectList, Map<String, Object> map) throws Exception {

        StringBuilder sb_serviceGroup = new StringBuilder();
        if (serviceGroupObjectList != null && serviceGroupObjectList.getData() != null && serviceGroupObjectList.getData().size() != 0) {
            for (ServiceGroupObjectRO serviceGroupObjectRO : serviceGroupObjectList.getData()) {

                if (serviceGroupObjectRO == null || serviceGroupObjectRO.getIncludeFilterServiceGroupNames() == null) {
                    continue;
                }

                PolicyObjectDTO command_line = createServiceObjectGroup(serviceGroupObjectRO.getName(), serviceGroupObjectRO.getIncludeFilterServiceGroupNames());
                if (command_line == null) {
                    continue;
                }
                sb_serviceGroup.append(command_line.getCommandLine());

            }
        }

        return sb_serviceGroup.toString();
    }

    private final static String DEFAULT_FILTERLIST_NAME = "Firewall Policy";
    @Override
    public String generateFilterListAndRuleListByTranslation(String deviceUuid, DeviceFilterlistRO deviceFilterlistRO,
                                                             ResultRO<List<DeviceFilterRuleListRO>> listResultRO, Map<String, Object> map) throws Exception {

        if (PolicyTypeEnum.SYSTEM__IMPORTED_ROUTING_TABLES.name().equals(deviceFilterlistRO.getRuleListType()) ||
                PolicyTypeEnum.SYSTEM__ROUTING_TABLES.name().equals(deviceFilterlistRO.getRuleListType()) ||
                PolicyTypeEnum.SYSTEM__POLICY_ROUTING.name().equals(deviceFilterlistRO.getRuleListType()) ||
                PolicyTypeEnum.SYSTEM__GENERIC_ACL.name().equals(deviceFilterlistRO.getRuleListType())) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        StringBuilder sb_FilterList = new StringBuilder();
        //山石生成策略集
        if (deviceFilterlistRO != null && !DEFAULT_FILTERLIST_NAME.equals(deviceFilterlistRO.getName())) {
            String command_line = String.format("policy-group %s\n", deviceFilterlistRO.getName());
            sb_FilterList.append("configure\n");
            sb_FilterList.append(command_line);
            sb_FilterList.append("exit\n");
            sb_FilterList.append("\n");
            sb.append(sb_FilterList.toString());
        }

        StringBuilder sb_RuleList = new StringBuilder();
        if (listResultRO != null && listResultRO.getData() != null && listResultRO.getData().size() != 0) {

            for (DeviceFilterRuleListRO ruleListRO : listResultRO.getData()) {
                if (ruleListRO == null) {
                    continue;
                }

                CommandlineDTO dto = new CommandlineDTO();
                if (ruleListRO.getInInterfaceGroupRefs() != null && ruleListRO.getInInterfaceGroupRefs().size() > 0) {
                    dto.setSrcZone(ruleListRO.getInInterfaceGroupRefs().get(0));
                }
                dto.setSrcItf("");
                if (ruleListRO.getOutInterfaceGroupRefs() != null && ruleListRO.getOutInterfaceGroupRefs().size() > 0) {
                    dto.setDstZone(ruleListRO.getOutInterfaceGroupRefs().get(0));
                }
                dto.setDstItf("");

                String name = StringUtils.isBlank(ruleListRO.getName())?ruleListRO.getRuleId():ruleListRO.getName();
                dto.setName(name);
                dto.setBusinessName(name);
                dto.setAction(StringUtils.isBlank(ruleListRO.getAction())?"permit":ruleListRO.getAction());
                dto.setDescription(ruleListRO.getDescription());

                dto.setCreateObjFlag(false);
                dto.setMoveSeatEnum(MoveSeatEnum.AFTER);

                //ip类型  0：ipv4; 1:ipv6; 2: url
                if ("IP4".equals(ruleListRO.getIpType())) {
                    dto.setIpType(0);
                } else if ("IP6".equals(ruleListRO.getIpType())) {
                    dto.setIpType(1);
                } else {
                    dto.setIpType(2);
                }

                JSONObject matchClause = ruleListRO.getMatchClause();
                if (matchClause != null) {
                    if (matchClause.containsKey("services")) {
                        JSONArray services = matchClause.getJSONArray("services");
                        List<String> existServiceNameList = new ArrayList<>();
                        for (int i = 0; i < services.size(); i++) {
                            JSONObject service = services.getJSONObject(i);

                            if (service.containsKey("nameRef")) {
                                String nameRef = service.getString("nameRef");
                                nameRef = nameRef.substring(0, nameRef.length()-2);
                                existServiceNameList.add(nameRef);
                            }

                        }
                        dto.setExistServiceNameList(existServiceNameList);
                    }

                    if (matchClause.containsKey("srcIp")) {
                        JSONArray srcIpArray = matchClause.getJSONArray("srcIp");
                        List<String> existSrcAddressList = new ArrayList<>();
                        for (int i = 0; i < srcIpArray.size(); i++) {
                            JSONObject srcIp = srcIpArray.getJSONObject(i);

                            if (srcIp.containsKey("nameRef")) {
                                String nameRef = srcIp.getString("nameRef");
                                nameRef = nameRef.substring(0, nameRef.length()-2);
                                existSrcAddressList.add(nameRef);
                            }

                        }
                        dto.setExistSrcAddressList(existSrcAddressList);
                    }

                    if (matchClause.containsKey("dstIp")) {
                        JSONArray dstIpArray = matchClause.getJSONArray("dstIp");
                        List<String> existDstAddressList = new ArrayList<>();
                        for (int i = 0; i < dstIpArray.size(); i++) {
                            JSONObject dstIp = dstIpArray.getJSONObject(i);

                            if (dstIp.containsKey("nameRef")) {
                                String nameRef = dstIp.getString("nameRef");
                                nameRef = nameRef.substring(0, nameRef.length()-2);
                                existDstAddressList.add(nameRef);
                            }

                        }
                        dto.setExistDstAddressList(existDstAddressList);
                    }
                }

                dto.setDeviceUuid(ruleListRO.getDeviceUuid());

                String command_line = createCommandLine(dto);
                if (StringUtils.isBlank(command_line)) {
                    continue;
                }
                sb_RuleList.append(command_line);
            }

        }
        sb.append(sb_RuleList.toString());
        return sb.toString();

    }

    private String createAddressGroup(String addressGroupName, List<String> addressObjectNameList) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("address \"%s\"\n", addressGroupName));
        for (String name : addressObjectNameList) {
            sb.append(String.format(" member %s \n", name));
        }
        sb.append("exit\n");
        return sb.toString();
    }

    private static Set<Integer> allowType = new HashSet<>();
    private final int MAX_NAME_LENGTH = 95;
    private final int DAY_SECOND = 24 * 60 * 60;
    private PolicyObjectDTO createServiceObject(String name, List<ServiceDTO> serviceDTOList, Integer idleTimeout) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        //对象名称集合, 不一定会建组，建组条件：有2组及以上协议，其中有一个协议，不带端口
        List<String> serviceNameList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        //多个，建对象
        for(ServiceDTO service : serviceDTOList){
            if (!StringUtils.isNumeric(service.getProtocol())) {
                return null;
            }
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("service Any \n");
                return dto;
            }


            String command = getServiceNameByNoPort(service, 0);
            if(StringUtils.isNotBlank(command)) {
                serviceNameList.add(command);
                continue;
            }

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)){

                String[] srcPorts = null;
                if (StringUtils.isNotBlank(service.getSrcPorts())) {
                    srcPorts = service.getSrcPorts().split(",");
                }
                String[] dstPorts = service.getDstPorts().split(",");

                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                for (String dstPort : dstPorts) {
                    sb.append(String.format("%s dst-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    if(idleTimeout != null) {
                        int day = idleTimeout / DAY_SECOND;
                        if((idleTimeout % DAY_SECOND) > 0) {
                            day = day + 1;
                        }
                        sb.append(String.format(" timeout-day %d", day));
                    }
                }
            }else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %d %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
            }
        }

        //有对象
        if( sb.toString().length() > 0) {
            String objName = name;

            //service name限制长度
            if(objName.length() > getMaxNameLength()) {
                String shortName = objName.substring(0, getMaxNameLength()-5);
                objName = String.format("%s_etcs", shortName.substring(0, shortName.lastIndexOf("_")));
            }

            dto.setName(objName);
            serviceNameList.add(objName);
            String tmp = sb.toString();
            StringBuilder tmpSb = new StringBuilder();
            tmpSb.append(String.format("service %s\n", objName));
            tmpSb.append(tmp);
            tmpSb.append("exit\n");
            sb.setLength(0);
            sb.append(tmpSb);
        }

        dto.setCommandLine(sb.toString());
        return dto;
    }

    private PolicyObjectDTO createServiceObjectGroup(String name, List<String> serviceNameList) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        //要建组
        String groupName = name;
        sb.append(String.format("servgroup %s\n", groupName));
        for(String objName : serviceNameList){
            sb.append(String.format("service %s\n", objName));
        }
        sb.append("exit\n");
        dto.setJoin("service " + groupName +"\n");
        dto.setName(groupName);

        dto.setCommandLine(sb.toString());
        return dto;
    }

}
