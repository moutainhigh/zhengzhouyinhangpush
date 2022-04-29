package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityUsg6000;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service(value = "USG6000 NAT")
public class U6000 implements NatPolicyGenerator {


    private final String SOURCE_ADDRESS = "source-address";

    private final String DESTINATION_ADDRESS = "destination-address";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate u6000 nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {

        StringBuilder sb = new StringBuilder();

        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }
        String name  = policyDTO.getTheme().replace("-","_");
        sb.append("nat server ");
        sb.append( name + " ");
        policyDTO.setPolicyName(name);
        if(!AliStringUtils.isEmpty(policyDTO.getToZone())) {
            sb.append(" zone ");
            sb.append(policyDTO.getToZone());
            sb.append(" ");
        }

        String protocol = ProtocolUtils.getProtocolByString(policyDTO.getProtocol()).toLowerCase();
        if(!AliStringUtils.isEmpty(protocol) && !protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            sb.append("protocol " + protocol.toLowerCase() + " ");
        }

        sb.append("global ");

        String insideAddress = getIpString(policyDTO.getInsideAddress());
        String globalAddress = getIpString(policyDTO.getGlobalAddress());

        //port字串会自带前面的空格，因为port有可能为空不填，若在后续组成协议中写空格，则会出现端口为空的时候多个空格的情况，不好看
        String globalPort = getPort(protocol, policyDTO.getGlobalPort());
        String insidePort = getPort(protocol, policyDTO.getInsidePort());

        sb.append(globalAddress + globalPort + " inside " + insideAddress + insidePort );

        sb.append("\n");
        sb.append("quit\nreturn\n");
        return sb.toString();
    }
    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        SecurityUsg6000 usg6000 = new SecurityUsg6000();
        //这里是整体复用所以只会有一个 ,这

        String existSrcAddressName = policyDTO.getSrcAddressObjectName();


        PolicyObjectDTO srcAddressObject = usg6000.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), SOURCE_ADDRESS, createObjFlag, existSrcAddressName, null,0);

        PolicyObjectDTO dstAddressObject = usg6000.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), DESTINATION_ADDRESS, createObjFlag, policyDTO.getDstAddressObjectName(),null,0);

        PolicyObjectDTO serviceObject = usg6000.generateServiceObject(policyDTO.getServiceList(), createObjFlag, policyDTO.getServiceObjectName(),0);
        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),policyDTO.getPostAddressObjectName());

        List<String> addressObjectNameList = new ArrayList<>();
        List<String> addressGroupObjectNameList = new ArrayList<>();
        List<String> serviceObjectNameList = new ArrayList<>();

        // 记录创建对象名称
        recordCreateObjectName(addressObjectNameList, addressGroupObjectNameList, serviceObjectNameList, null, srcAddressObject,
            dstAddressObject, null, null, serviceObject, natObject);


        policyDTO.setAddressObjectNameList(addressObjectNameList);
        policyDTO.setServiceObjectNameList(serviceObjectNameList);
        policyDTO.setAddressObjectGroupNameList(addressGroupObjectNameList);


        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        if (StringUtils.isNotBlank(natObject.getCommandLine())) {
            sb.append(natObject.getCommandLine());
        }


        sb.append("nat-policy\n");
        String name = policyDTO.getTheme().replace("-","_");
        sb.append(String.format("rule name %s\n", name));
        policyDTO.setPolicyName(name);

        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", policyDTO.getSrcZone()));
        }

        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", policyDTO.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
                sb.append(srcAddressObject.getCommandLine());
            }
        }

        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
                sb.append(dstAddressObject.getCommandLine());
            }
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("action source-nat address-group %s\n", natObject.getName()));
        //rule之后添加quit
        sb.append("quit\n");

        sb.append("return\n");

        return sb.toString();
    }
    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        SecurityUsg6000 usg6000 = new SecurityUsg6000();
        PolicyObjectDTO srcAddressObject = usg6000.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), SOURCE_ADDRESS, createObjFlag, policyDTO.getSrcAddressObjectName(), null,0);

        PolicyObjectDTO dstAddressObject = usg6000.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), DESTINATION_ADDRESS, createObjFlag, policyDTO.getDstAddressObjectName(),null,0);

        PolicyObjectDTO serviceObject = usg6000.generateServiceObject(policyDTO.getServiceList(), createObjFlag, policyDTO.getServiceObjectName(), 0);
//        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostIpAddress(), policyDTO.getTheme());
        String dstNatAddress = policyDTO.getDstIp() == null?"":policyDTO.getPostIpAddress().split(",")[0];

        List<String> addressObjectNameList = new ArrayList<>();
        List<String> serviceObjectNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(addressObjectNameList, null, serviceObjectNameList, null, srcAddressObject,
            dstAddressObject,null, null, serviceObject, null);
        policyDTO.setAddressObjectNameList(addressObjectNameList);
        policyDTO.setServiceObjectNameList(serviceObjectNameList);

        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        sb.append("nat-policy\n");
        String name = policyDTO.getTheme().replace("-","_");
        sb.append(String.format("rule name %s\n", name));
        policyDTO.setPolicyName(name);

        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", policyDTO.getSrcZone()));
        }

        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", policyDTO.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
                sb.append(srcAddressObject.getCommandLine());
            }
        }

        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
                sb.append(dstAddressObject.getCommandLine());
            }
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("action destination-nat address %s\n", dstNatAddress));
        //rule之后添加quit
        sb.append("quit\n");

        sb.append("return\n");

        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        boolean createObjFlag = policyDTO.isCreateObjFlag();
        SecurityUsg6000 usg6000 = new SecurityUsg6000();
        PolicyObjectDTO srcAddressObject = usg6000.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), SOURCE_ADDRESS, createObjFlag, policyDTO.getSrcAddressObjectName(),null,0);

        PolicyObjectDTO dstAddressObject = usg6000.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), DESTINATION_ADDRESS, createObjFlag, policyDTO.getDstAddressObjectName(), null,0);

        PolicyObjectDTO serviceObject = usg6000.generateServiceObject(policyDTO.getServiceList(), createObjFlag, policyDTO.getServiceObjectName(), 0);
        PolicyObjectDTO natObject = generateNatObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(),policyDTO.getPostSrcAddressObjectName());
        String dstNatAddress = policyDTO.getDstIp() == null?"":policyDTO.getPostDstIp().split(",")[0];

        List<String> addressObjectNameList = new ArrayList<>();
        List<String> addressGroupObjectNameList = new ArrayList<>();
        List<String> serviceObjectNameList = new ArrayList<>();
        // 记录创建对象名称
        recordCreateObjectName(addressObjectNameList, addressGroupObjectNameList, serviceObjectNameList, null, srcAddressObject,
            dstAddressObject,null, null, serviceObject, natObject);
        policyDTO.setAddressObjectNameList(addressObjectNameList);
        policyDTO.setAddressObjectGroupNameList(addressGroupObjectNameList);
        policyDTO.setServiceObjectNameList(serviceObjectNameList);


        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");

        if(policyDTO.isVsys()) {
            sb.append("switch vsys " + policyDTO.getVsysName() + "\n");
            sb.append("system-view\n\n");
        }

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        if (StringUtils.isNotBlank(natObject.getCommandLine())) {
            sb.append(natObject.getCommandLine());
        }


        sb.append("nat-policy\n");
        String name = policyDTO.getTheme().replace("-","_");
        sb.append(String.format("rule name %s\n", name));
        policyDTO.setPolicyName(name);

        if(StringUtils.isNotBlank(policyDTO.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", policyDTO.getSrcZone()));
        }

        if(StringUtils.isNotBlank(policyDTO.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", policyDTO.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if(!AliStringUtils.isEmpty(policyDTO.getSrcIp())) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
                sb.append(srcAddressObject.getCommandLine());
            }
        }

        if(!AliStringUtils.isEmpty(policyDTO.getDstIp())) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
                sb.append(dstAddressObject.getCommandLine());
            }
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("action source-nat address-group %s\n", natObject.getName()));
        sb.append(String.format("action destination-nat static address-to-address address %s\n", dstNatAddress));
        //rule之后添加quit
        sb.append("quit\n");

        sb.append("return\n");

        return sb.toString();
    }

    private String getPort(String protocol, String port) {
        if (AliStringUtils.isEmpty(protocol) || protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            return "";
        }  else if (protocol.equalsIgnoreCase("ICMP")) {
            return "";
        } else if(AliStringUtils.isEmpty(port)) {
            return " any";
        } else if(PortUtils.isPortRange(port)) {
            String startPort = PortUtils.getStartPort(port);
            String endPort = PortUtils.getEndPort(port);
            return " " + startPort + " " + endPort;
        }
        return " " + port;
    }

    private String getIpString(String ipAddresses){
        if(AliStringUtils.isEmpty(ipAddresses)) {
            return "";
        }

        String[] ipAddressList = ipAddresses.split(",");
        String ipAddress = ipAddressList[0];

        if(IpUtils.isIP(ipAddress)) {
            return ipAddress;
        }
        String startIpAddress = IpUtils.getStartIpFromIpAddress(ipAddress);
        String endIpAddress = IpUtils.getEndIpFromIpAddress(ipAddress);

        return startIpAddress + " " + endIpAddress;
    }

    public PolicyObjectDTO generateNatObject(String addressString, String theme,String existAddressName) {
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO dto = new PolicyObjectDTO();
        String name = String.format("%s_NO_%s",theme, IdGen.getRandomNumberString());
        if(StringUtils.isNotBlank(existAddressName)){
            if(StringUtils.isNotEmpty(existAddressName) && existAddressName.contains(" ")){
                existAddressName = "\""+existAddressName+"\"";
            }
            dto.setName(existAddressName);
            dto.setJoin(existAddressName);
            return dto;
        }
        sb.append(String.format("nat address-group %s\n", name));
        List<String> createGroupObjectNames = new ArrayList<>();
        createGroupObjectNames.add(name);
        String[] addresses = addressString.split(",");

        for(String address: addresses) {
            if(IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromIpAddress(address);
                String endIp = IpUtils.getEndIpFromIpAddress(address);
                sb.append(String.format("section %s %s\n", startIp, endIp) );
            } else if (IpUtils.isIPSegment(address)) {
                String startIp = IpUtils.getStartIpFromIpAddress(address);
                String endIp = IpUtils.getEndIpFromIpAddress(address);
                sb.append(String.format("section %s %s\n", startIp, endIp) );
            } else {
                sb.append("section " + address + "\n");
            }
        }

        sb.append("quit\n\n");

        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setCreateGroupObjectName(createGroupObjectNames);
        return dto;
    }



}
