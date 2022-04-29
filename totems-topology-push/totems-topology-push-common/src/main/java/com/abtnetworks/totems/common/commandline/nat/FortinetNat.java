package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.DNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.commandline.StaticNatTaskDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc    飞塔NAT策略
 * @author liuchanghao
 * @date 2020-11-24 15:13
 */

@Slf4j
@Service(value = "Fortinet NAT")
public class FortinetNat implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate cisco nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            if(StringUtils.isNotEmpty(policyDTO.getVsysName())){
                sb.append("config vdom\n");
                sb.append("edit " + policyDTO.getVsysName() + "\n");
            }
        }else {
            if (policyDTO.isHasVsys()){
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        List<String> createAddressObjectNames = new ArrayList<>();
        List<String> createServiceObjectNames = new ArrayList<>();

        String globalAddress = policyDTO.getGlobalAddress();
        String insideAddress = policyDTO.getInsideAddress();
        String name = String.format("mip_%s_%s ", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        sb.append("edit ").append(name).append("\n");
        setAddressCommandLine(globalAddress, sb, "extip ");
        sb.append("set extintf any\n");
        setAddressCommandLine(insideAddress, sb, "mappedip ");
        createAddressObjectNames.add(name);

        if((!policyDTO.getGlobalPort().equals("any")) || (!policyDTO.getInsidePort().equals("any"))){
            sb.append("set portforward enable\n");
            String protocol = ProtocolUtils.getProtocolByString(policyDTO.getProtocol());
            sb.append("set protocol ").append(protocol.toLowerCase()).append("\n");

            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(policyDTO.getGlobalPort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getGlobalPort(),PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set extport ").append(policyDTO.getGlobalPort()).append("\n");
                } else {
                    if(StringUtils.isNotEmpty(policyDTO.getProtocol())){
                        sb.append("set extport 1-65535").append("\n");
                    }
                }
                if(StringUtils.isNotEmpty(policyDTO.getInsidePort()) && !StringUtils.equalsAnyIgnoreCase(policyDTO.getInsidePort(),PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set mappedport ").append(policyDTO.getInsidePort()).append("\n");
                }else {
                    if(StringUtils.isNotEmpty(policyDTO.getProtocol())){
                        sb.append("set mappedport 1-65535").append("\n");
                    }
                }
            }
        }else if(StringUtils.isNotEmpty(policyDTO.getProtocol())){
            sb.append("set portforward disable\n");
        }
        sb.append("next\n");
        sb.append("end\n\n");

        String serviceName = "";
        Boolean isExist = false;
        //是否复用
        if(StringUtils.isNotEmpty(policyDTO.getExistGlobaPort())){
            isExist=true;
            serviceName = policyDTO.getExistGlobaPort();
        }
        serviceName = buildServiceCommandLine(sb, policyDTO.getProtocol(), policyDTO.getGlobalPort(), serviceName, isExist, createServiceObjectNames);
        policyDTO.setAddressObjectNameList(createAddressObjectNames);
        policyDTO.setServiceObjectNameList(createServiceObjectNames);


        sb.append("config firewall policy\n");
        sb.append("edit ").append("0").append("\n");
        String policyName = String.format("%s_AO_%s",policyDTO.getTheme(), IdGen.getRandomNumberString());
        policyDTO.setPolicyName(policyName);
        sb.append("set name ").append(policyName).append(StringUtils.LF);
        if(StringUtils.isNotEmpty(policyDTO.getFromZone())){
            sb.append("set srcintf ").append(policyDTO.getFromZone()).append("\n");
        }else if(StringUtils.isNotEmpty(policyDTO.getInDevItf())){
            sb.append("set srcintf ").append(policyDTO.getInDevItf()).append("\n");
        }else{
            sb.append("set srcintf \"any\"").append("\n");
        }
        if(StringUtils.isNotEmpty(policyDTO.getToZone())){
            sb.append("set dstintf ").append(policyDTO.getToZone()).append("\n");
        } else if(StringUtils.isNotEmpty(policyDTO.getOutDevItf())){
            sb.append("set dstintf ").append(policyDTO.getOutDevItf()).append("\n");
        } else {
            sb.append("set dstintf \"any\"").append("\n");
        }
        sb.append("set srcaddr all").append("\n");
        sb.append("set dstaddr ").append(name).append("\n");
        sb.append("set action accept").append("\n");
        sb.append("set schedule always").append("\n");
        sb.append("set service ").append(serviceName).append("\n");
        sb.append("show\n");
        sb.append("next\n");

        if(null != policyDTO.getMoveSeatEnum()){
            int moveSeatCode = policyDTO.getMoveSeatEnum().getCode();
            if (moveSeatCode == MoveSeatEnum.FIRST.getCode() || moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                sb.append(String.format("move %s before %s\n", "#1", policyDTO.getSwapRuleNameId()));
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                sb.append(String.format("move %s after %s\n", "#1", policyDTO.getSwapRuleNameId()));
            }
        }

        sb.append("end\n");
        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        List<String> restDstAddressList = policyDTO.getRestDstAddressList();

        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        List<String> existDstAddressList = policyDTO.getExistDstAddressList();

        List<ServiceDTO> serviceList = policyDTO.getRestServiceList();

        String postAddressObjectName = policyDTO.getPostAddressObjectName();
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            if(StringUtils.isNotEmpty(policyDTO.getVsysName())){
                sb.append("config vdom\n");
                sb.append("edit " + policyDTO.getVsysName() + "\n");
            }
        }else {
            if (policyDTO.isHasVsys()){
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        // 定义创建对象集合
        List<String> createAddressObjectNames = new ArrayList<>();
        if(restSrcAddressList.size()>0){
            for(String srcAddr : restSrcAddressList){
                sb.append(createAddressObject(srcAddr,existSrcAddressList,policyDTO.getTheme(),createAddressObjectNames));
            }
        }

        if(restDstAddressList.size()>0){
            for(String dstAddr : restDstAddressList){
                sb.append(createAddressObject(dstAddr,existDstAddressList,policyDTO.getTheme(),createAddressObjectNames));
            }
        }




        List<String> serviceNameList = new ArrayList<>();

        // 定义创建服务对象集合
        List<String> createServiceObjectNames = new ArrayList<>();

        for (String existServiceName : policyDTO.getExistServiceNameList()) {
            serviceNameList.add(String.format("\"%s\"", existServiceName));
        }
        if (serviceList.size() > 0) {
            //服务为any时，service设置为ALL
            if (ProtocolUtils.getProtocolByString(serviceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                serviceNameList.add("ALL");
            } else {
                StringBuilder servicesb = new StringBuilder();
                servicesb.append(createServiceObject(serviceList, serviceNameList,createServiceObjectNames));
                if (servicesb.toString().trim().length() > 0) {
                    sb.append(servicesb.toString());
                }
            }
        }
        // todo 转换后的没写
        //源地址 转换后， 生成地址池对象  每次都新建
        PolicyObjectDTO srcPostObject = generateAddressPool(policyDTO.getPostIpAddress(),postAddressObjectName, policyDTO.getTheme());
        if(null != srcPostObject && srcPostObject.isObjectFlag() && StringUtils.isNotBlank(srcPostObject.getName())){
            createAddressObjectNames.add(srcPostObject.getName());
        }
        policyDTO.setAddressObjectNameList(createAddressObjectNames);
        policyDTO.setServiceObjectNameList(createServiceObjectNames);

        if(srcPostObject != null && StringUtils.isNotEmpty(srcPostObject.getCommandLine())){
            sb.append(srcPostObject.getCommandLine()).append("\n");
        }
        sb.append(preCreatePolicy());
        List<String> srcItfList = new ArrayList<>();
        if (AliStringUtils.isEmpty(policyDTO.getSrcZone())) {
            if (StringUtils.isNotEmpty(policyDTO.getSrcItf())) {
                srcItfList.add(policyDTO.getSrcItf());
            }
        } else {
            srcItfList.add(policyDTO.getSrcZone());
        }

        List<String> dstItfList = new ArrayList<>();
        if (StringUtils.isEmpty(policyDTO.getDstZone())) {
            if (StringUtils.isNotEmpty(policyDTO.getDstItf())) {
                dstItfList.add(policyDTO.getDstItf());
            }
        } else {
            dstItfList.add(policyDTO.getDstZone());

        }

        String moveBefore = null;
        String moveAfter = null;
        if(null != policyDTO.getMoveSeatEnum()){
            int moveSeatCode = policyDTO.getMoveSeatEnum().getCode();
            if (moveSeatCode == MoveSeatEnum.FIRST.getCode() || moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                moveBefore = policyDTO.getSwapRuleNameId();
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                moveAfter = policyDTO.getSwapRuleNameId();
            }
        }

        String policyName = String.format("%s_AO_%s",policyDTO.getTheme(), IdGen.getRandomNumberString());
        policyDTO.setPolicyName(policyName);
        sb.append(createPolicy(policyDTO.getTheme(),policyName, srcItfList, dstItfList, serviceNameList, policyDTO.getDescription(), srcPostObject,existSrcAddressList,existDstAddressList, moveBefore, moveAfter));
        if (policyDTO.isVsys()) {
            sb.append("end\n");
        }
        return sb.toString();
    }

    /**
     * 创建静态nat地址命令行
     * @param address
     * @param sb
     * @param keyWords
     */
    private void setAddressCommandLine(String address, StringBuilder sb, String keyWords){
        if(StringUtils.isNotEmpty(address)){
            String[] globals = address.split(",");
            for(String globalIp : globals){
                if (IpUtils.isIPRange(globalIp)) {
                    String startIp = IpUtils.getStartIpFromRange(globalIp);
                    String endIp = IpUtils.getEndIpFromRange(globalIp);
                    sb.append("set ").append(keyWords).append(startIp).append("-").append(endIp).append("\n");
                    break;
                } else if (IpUtils.isIPSegment(globalIp)) {
                    //是子网段，转换成范围
                    String ip = IpUtils.getIpFromIpSegment(globalIp);
                    //获取网段数
                    String maskBit = IpUtils.getMaskBitFromIpSegment(globalIp);
                    long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                    String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                    String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                    sb.append("set ").append(keyWords).append(startIp).append("-").append(endIp).append("\n");
                    break;
                } else  {
                    sb.append("set ").append(keyWords).append(globalIp).append("\n");
                    break;
                }
            }
        }
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            if(StringUtils.isNotEmpty(policyDTO.getVsysName())){
                sb.append("config vdom\n");
                sb.append("edit " + policyDTO.getVsysName() + "\n");
            }
        }else {
            if (policyDTO.isHasVsys()){
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        String dstIp = policyDTO.getDstIp();
        String postIpAddress = policyDTO.getPostIpAddress();
        String dstItf = policyDTO.getDstItf();
        String dstZone = policyDTO.getDstZone();
        String srcItf = policyDTO.getSrcItf();
        String srcZone = policyDTO.getSrcZone();
        String name = String.format("mip_%s_%s", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        name = String.format("\"%s\"",name);
        sb.append("edit ").append(name).append("\n");
        setAddressCommandLine(dstIp, sb, "extip ");
        if(StringUtils.isNotEmpty(srcItf)){
            sb.append(String.format("set extintf \"%s\"",srcItf)).append("\n");
        }else {
            sb.append("set extintf any\n");
        }
        setAddressCommandLine(postIpAddress, sb, "mappedip ");


        String protocol = policyDTO.getServiceList().get(0).getProtocol();
        String dstPorts = policyDTO.getServiceList().get(0).getDstPorts();
        String postPort = policyDTO.getPostPort();


        if((!dstPorts.equals("any")) || (!postPort.equals("any"))){
            sb.append("set portforward enable\n");
            String protocolString = ProtocolUtils.getProtocolByString(protocol);
            sb.append("set protocol ").append(protocolString.toLowerCase()).append("\n");

            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set extport ").append(dstPorts).append("\n");
                } else {
                    if(StringUtils.isNotEmpty(protocol)){
                        sb.append("set extport 1-65535").append("\n");
                    }
                }
                if(StringUtils.isNotEmpty(postPort) && !StringUtils.equalsAnyIgnoreCase(postPort,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set mappedport ").append(postPort).append("\n");
                }else {
                    if(StringUtils.isNotEmpty(protocol)){
                        sb.append("set mappedport 1-65535").append("\n");
                    }
                }
            }
        }else if(StringUtils.isNotEmpty(protocol)){
            sb.append("set portforward disable\n");
        }

        sb.append("next\n");
        sb.append("end\n\n");

        String serviceName = "";

        String timeObjectName = "always";
        if (StringUtils.isNotBlank(policyDTO.getStartTime())) {
            sb.append(preCreateTimeObject());
            timeObjectName = String.format("\"to%s\"",
                    TimeUtils.transformDateFormat(policyDTO.getEndTime(), TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
            sb.append(createTimeObject(formatTimeString(policyDTO.getStartTime()), formatTimeString(policyDTO.getEndTime()), timeObjectName));
        }



        List<String> createAddressObjectName = new ArrayList<>();

        // 创建源地址对象
        if(CollectionUtils.isNotEmpty(restSrcAddressList)){
            for(String srcAddress : restSrcAddressList){
                sb.append("config firewall address\n");
                String element = "";
                if(IpUtils.isIPRange(srcAddress)){
                    String[] range = srcAddress.split("-");
                    String start = range[0];
                    String end  = range[1];
                    element = String.format(" \"%s\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set type iprange\n"));
                    sb.append(String.format("set start-ip %s\n",start));
                    sb.append(String.format("set end-ip %s\n",end));
                }else if(IpUtils.isIPSegment(srcAddress)){
                    element = String.format(" \"%s\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set sunbet %s\n",srcAddress));
                }else{
                    element = String.format(" \"%s/32\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set subnet %s/32\n",srcAddress));
                }
                existSrcAddressList.add(element);
                createAddressObjectName.add(element);
                sb.append("next\nend\n\n");
            }
        }
        List<String> createServiceObjectName = new ArrayList<>();


        // 构建服务命令行
        Boolean isExist = false;
        if(CollectionUtils.isNotEmpty(policyDTO.getExistServiceNameList())){
            List<String> existServiceNameList = policyDTO.getExistServiceNameList();
            String existServiceName= existServiceNameList.get(0);
            serviceName = existServiceName;
            isExist=true;
        }
        serviceName = buildServiceCommandLine(sb, protocol, dstPorts, serviceName, isExist, createServiceObjectName);



        policyDTO.setAddressObjectNameList(createAddressObjectName);
        policyDTO.setServiceObjectNameList(createServiceObjectName);

        // 构建策略命令行
        sb.append("config firewall policy\n");
        sb.append("edit ").append("0").append("\n");

        String policyName = String.format("%s_AO_%s",policyDTO.getTheme(), IdGen.getRandomNumberString());
        policyDTO.setPolicyName(policyName);
        sb.append("set name ").append(policyName).append(StringUtils.LF);
        if(StringUtils.isNotEmpty(srcZone)){
            sb.append("set srcintf ").append(srcZone).append("\n");
        }else if(StringUtils.isNotEmpty(srcItf)){
            sb.append("set srcintf ").append(srcItf).append("\n");
        }else{
            sb.append("set srcintf \"any\"").append("\n");
        }
        if(StringUtils.isNotEmpty(dstZone)){
            sb.append("set dstintf ").append(dstZone).append("\n");
        } else if(StringUtils.isNotEmpty(dstItf)){
            sb.append("set dstintf ").append(dstItf).append("\n");
        }else{
            sb.append("set dstintf \"any\"").append("\n");
        }

        if(CollectionUtils.isNotEmpty(existSrcAddressList)){
            sb.append("set srcaddr ");
            for (String srcip : existSrcAddressList){
                sb.append(String.format("%s ",srcip));
            }
            sb.append("\n");
        }else {
            sb.append("set srcaddr all\n");
        }


        sb.append("set dstaddr ").append(name).append("\n");
        sb.append("set action accept").append("\n");
        sb.append(String.format("set schedule %s\n", timeObjectName));
        sb.append("set service ").append(serviceName).append("\n");
        sb.append("show\n");
        sb.append("next\n");

        String moveBefore = null;
        String moveAfter = null;
        if(null != policyDTO.getMoveSeatEnum()){
            int moveSeatCode = policyDTO.getMoveSeatEnum().getCode();
            if (moveSeatCode == MoveSeatEnum.FIRST.getCode() || moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                moveBefore = policyDTO.getSwapRuleNameId();
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                moveAfter = policyDTO.getSwapRuleNameId();
            }
        }

        if(null != policyDTO.getMoveSeatEnum()){
            if (StringUtils.isNotBlank(moveBefore)) {
                sb.append(String.format("move %s before %s\n", "#1", moveBefore));
            } else if (StringUtils.isNotBlank(moveAfter)) {
                sb.append(String.format("move %s after %s\n", "#1", moveAfter));
            }
        }

        sb.append("end\n");
        return sb.toString();
    }

    String preCreateTimeObject() {
        return "config firewall schedule onetime\n";
    }

    String createTimeObject(String startTime, String endTime, String timeObjectName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", timeObjectName));
        sb.append(String.format("set start %s\n", startTime));
        sb.append(String.format("set end %s\n", endTime));
        sb.append("next\nend\n\n");
        return sb.toString();
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.FORTINET_FORMAT);
    }

    private String getAddressObjectForObjectGroup(String ipAddress, String name){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("object network %s\n", name));
        if(IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            sb.append(String.format("range %s %s\n", start, end));
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            sb.append(String.format("subnet %s %s\n", ip, mask));
        } else {
            sb.append(String.format("host %s\n", ipAddress));
        }

        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            if(StringUtils.isNotEmpty(policyDTO.getVsysName())){
                sb.append("config vdom\n");
                sb.append("edit " + policyDTO.getVsysName() + "\n");
            }
        }else {
            if (policyDTO.isHasVsys()){
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        List<String> existSrcAddressList = policyDTO.getExistSrcAddressList();
        List<String> restSrcAddressList = policyDTO.getRestSrcAddressList();
        String dstIp = policyDTO.getDstIp();
        String postDstIpAddress = policyDTO.getPostDstIp();
        String postSrcIpAddress = policyDTO.getPostSrcIp();

        String postSrcAddressName = policyDTO.getPostSrcAddressObjectName();


        String dstItf = policyDTO.getDstItf();
        String dstZone = policyDTO.getDstZone();
        String srcItf = policyDTO.getSrcItf();
        String srcZone = policyDTO.getSrcZone();

        String protocol = policyDTO.getServiceList().get(0).getProtocol();
        String dstPorts = policyDTO.getServiceList().get(0).getDstPorts();
        String postPort = policyDTO.getPostPort();

        String postProtocol = null;
        String postDstPorts = null;
        if (StringUtils.isNotBlank(postPort) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(postPort)) {
            postProtocol = policyDTO.getPostServiceList().get(0).getProtocol();
            postDstPorts = policyDTO.getPostServiceList().get(0).getDstPorts();
        }

        String dstAddressName ="";

        // 判断如果vip可以复用 则直接复用vip
        if (StringUtils.isNotBlank(policyDTO.getExistVirtualIpName())) {
            dstAddressName = policyDTO.getExistVirtualIpName();
        } else {
            if (StringUtils.isNotBlank(postDstIpAddress) && StringUtils.isNotBlank(dstIp)) {
                // 构建vip 命令行
                dstAddressName = buildVipCommandLine(policyDTO, sb, dstIp, postDstIpAddress, srcItf, protocol, dstPorts, postPort);
            }
        }

        List<String> createAddressObjectName = new ArrayList<>();

        //源地址 转换后， 生成地址池对象  每次都新建
        PolicyObjectDTO srcPostObject = generateAddressPool(postSrcIpAddress,postSrcAddressName, policyDTO.getTheme());
        if(null != srcPostObject && srcPostObject.isObjectFlag() && StringUtils.isNotBlank(srcPostObject.getName())){
            createAddressObjectName.add(srcPostObject.getName());
        }

        if(srcPostObject != null && StringUtils.isNotEmpty(srcPostObject.getCommandLine())){
            sb.append(srcPostObject.getCommandLine()).append("\n");
        }

        // 对于新建的vip要可以删除
        if(StringUtils.isBlank(policyDTO.getExistVirtualIpName()) && StringUtils.isNotBlank(dstAddressName)){
            createAddressObjectName.add(String.format("%s_vip",dstAddressName));
        }

        String timeObjectName = "always";
        if (StringUtils.isNotBlank(policyDTO.getStartTime())) {
            sb.append(preCreateTimeObject());
            timeObjectName = String.format("\"to%s\"",
                    TimeUtils.transformDateFormat(policyDTO.getEndTime(), TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
            sb.append(createTimeObject(formatTimeString(policyDTO.getStartTime()), formatTimeString(policyDTO.getEndTime()), timeObjectName));
        }


        //创建地址对象
        if(CollectionUtils.isNotEmpty(restSrcAddressList)){
            for(String srcAddress : restSrcAddressList){
                sb.append("config firewall address\n");
                String element = "";
                if(IpUtils.isIPRange(srcAddress)){
                    String[] range = srcAddress.split("-");
                    String start = range[0];
                    String end  = range[1];
                    element = String.format(" \"%s\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set type iprange\n"));
                    sb.append(String.format("set start-ip %s\n",start));
                    sb.append(String.format("set end-ip %s\n",end));
                }else if(IpUtils.isIPSegment(srcAddress)){
                    element = String.format(" \"%s\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set sunbet %s\n",srcAddress));
                }else{
                    element = String.format(" \"%s/32\"",srcAddress);
                    sb.append(String.format("edit %s\n",element));
                    sb.append(String.format("set subnet %s/32\n",srcAddress));
                }
                existSrcAddressList.add(element);
                createAddressObjectName.add(element);
                sb.append("next\nend\n\n");
            }
        }

        // 构建服务对象
        List<String> createServiceObjectName = new ArrayList<>();
        Boolean isExist = false;
        String serviceName = "";
        // 目的端口不为空，且不为any的时候
        if (StringUtils.isNotBlank(postPort) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(postPort)) {
            if (CollectionUtils.isNotEmpty(policyDTO.getExistPostServiceNameList())) {
                List<String> existPostServiceNameList = policyDTO.getExistPostServiceNameList();
                String existPostServiceName = existPostServiceNameList.get(0);
                serviceName = existPostServiceName;
                isExist = true;
            }
            serviceName = buildServiceCommandLine(sb, postProtocol, postDstPorts, serviceName, isExist, createServiceObjectName);
        } else {
            if (CollectionUtils.isNotEmpty(policyDTO.getExistServiceNameList())) {
                List<String> existServiceNameList = policyDTO.getExistServiceNameList();
                String existServiceName = existServiceNameList.get(0);
                serviceName = existServiceName;
                isExist = true;
            }
            serviceName = buildServiceCommandLine(sb, protocol, dstPorts, serviceName, isExist, createServiceObjectName);
        }





        policyDTO.setAddressObjectNameList(createAddressObjectName);
        policyDTO.setServiceObjectNameList(createServiceObjectName);


        // 构建策略
        sb.append("config firewall policy\n");
        sb.append("edit ").append("0").append("\n");

        String policyName = String.format("%s_AO_%s",policyDTO.getTheme(), IdGen.getRandomNumberString());
        policyDTO.setPolicyName(policyName);
        sb.append("set name ").append(policyName).append(StringUtils.LF);
        if(StringUtils.isNotEmpty(srcZone)){
            sb.append("set srcintf ").append(srcZone).append("\n");
        }else if(StringUtils.isNotEmpty(srcItf)){
            sb.append("set srcintf ").append(srcItf).append("\n");
        }else{
            sb.append("set srcintf \"any\"").append("\n");
        }
        if(StringUtils.isNotEmpty(dstZone)){
            sb.append("set dstintf ").append(dstZone).append("\n");
        } else if(StringUtils.isNotEmpty(dstItf)){
            sb.append("set dstintf ").append(dstItf).append("\n");
        }else{
            sb.append("set dstintf \"any\"").append("\n");
        }

        if(CollectionUtils.isNotEmpty(existSrcAddressList)){
            sb.append("set srcaddr ");
            for (String srcip : existSrcAddressList){
                sb.append(String.format("%s ",srcip));
            }
            sb.append("\n");
        }else {
            sb.append("set srcaddr all\n");
        }

        if(StringUtils.isNotBlank(dstAddressName)){
            sb.append("set dstaddr ").append(dstAddressName).append("\n");
        }
        sb.append("set action accept").append("\n");
        sb.append(String.format("set schedule %s\n", timeObjectName));
        sb.append("set service ").append(serviceName).append("\n");

        if (null != srcPostObject && StringUtils.isNotBlank(srcPostObject.getName())) {
            sb.append("set nat enable\n");
            sb.append("set ippool enable\n");
            sb.append(String.format("set poolname %s\n",srcPostObject.getName()));
        }
        sb.append("show\n");
        sb.append("next\n");

        if(null != policyDTO.getMoveSeatEnum()){
            int moveSeatCode = policyDTO.getMoveSeatEnum().getCode();
            if (moveSeatCode == MoveSeatEnum.FIRST.getCode() || moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                sb.append(String.format("move %s before %s\n", "#1", policyDTO.getSwapRuleNameId()));
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                sb.append(String.format("move %s after %s\n", "#1", policyDTO.getSwapRuleNameId()));
            }
        }

        sb.append("end\n");
        return sb.toString();



    }

    private String buildServiceCommandLine(StringBuilder sb, String protocol, String dstPorts, String serviceName, Boolean isExist, List<String> createServiceObjectName) {
        if (StringUtils.isNotEmpty(protocol) && isExist == false) {
            String protocolString = ProtocolUtils.getProtocolByString(protocol);
            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if (StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append("config firewall service custom\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        serviceName = String.format("\"%s%s-%s\"", protocolString.substring(0, 1).toUpperCase(), startPort, endPort);
                    } else {
                        serviceName = String.format("\"%s%s\"", protocolString.substring(0, 1).toUpperCase(), dstPorts);
                    }
                } else {
                    // 20211012 招商证券 修改如果服务不填的时候，修改命令行按照ALL去下发
                    serviceName = "ALL";
                }
            } else {
                serviceName = "ALL_ICMP";
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if (StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        sb.append(String.format("set tcp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set tcp-portrange %s\n", dstPorts));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if (StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts, PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append("edit ").append(serviceName).append("\n");
                    if (PortUtils.isPortRange(dstPorts)) {
                        String startPort = PortUtils.getStartPort(dstPorts);
                        String endPort = PortUtils.getEndPort(dstPorts);
                        sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                    } else {
                        sb.append(String.format("set udp-portrange %s\n", dstPorts));
                    }
                    sb.append("next\n");
                    sb.append("end\n\n");
                }

            }
            if (StringUtils.isNotBlank(serviceName) && !serviceName.equalsIgnoreCase("ALL") && !serviceName.equalsIgnoreCase("ALL_ICMP")) {
                createServiceObjectName.add(serviceName);
            }
        }
        return serviceName;
    }

    private String buildVipCommandLine(NatPolicyDTO policyDTO, StringBuilder sb, String dstIp, String postDstIpAddress, String srcItf, String protocol, String dstPorts, String postPort) {
        String name = String.format("mip_%s_%s", policyDTO.getTheme(), IdGen.getRandomNumberString());
        sb.append("config firewall vip\n");
        name = String.format("\"%s\"", name);
        sb.append("edit ").append(name).append("\n");
        setAddressCommandLine(dstIp, sb, "extip ");
        if(StringUtils.isNotEmpty(srcItf)){
            sb.append(String.format("set extintf \"%s\"",srcItf)).append("\n");
        }else {
            sb.append("set extintf any\n");
        }
        setAddressCommandLine(postDstIpAddress, sb, "mappedip ");


        if((!dstPorts.equals("any")) || (!postPort.equals("any"))){
            sb.append("set portforward enable\n");
            String protocolString = ProtocolUtils.getProtocolByString(protocol);
            sb.append("set protocol ").append(protocolString.toLowerCase()).append("\n");

            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set extport ").append(dstPorts).append("\n");
                } else {
                    if(StringUtils.isNotEmpty(protocol)){
                        sb.append("set extport 1-65535").append("\n");
                    }
                }
                if(StringUtils.isNotEmpty(postPort) && !StringUtils.equalsAnyIgnoreCase(postPort,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append("set mappedport ").append(postPort).append("\n");
                }else {
                    if(StringUtils.isNotEmpty(protocol)){
                        sb.append("set mappedport 1-65535").append("\n");
                    }
                }
            }
        }else if(StringUtils.isNotEmpty(protocol)){
            sb.append("set portforward disable\n");
        }

        sb.append("next\n");
        sb.append("end\n\n");
        return name;
    }

    private String getAddressObject(String ipAddress, String name){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("object network %s\n", name));
        if(IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            sb.append(String.format("range %s %s\n", start, end));
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            sb.append(String.format("subnet %s %s\n", ip, mask));
        } else {
            sb.append(String.format("host %s\n", ipAddress));
        }

        return sb.toString();
    }

    private String preCreateServiceObject() {
        return "config firewall service custom\n";
    }

    private PolicyObjectDTO getAddressObjectGroup(String ipAddresses, String name, String addressObjectName, boolean createObject){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        //地址对象已存在，复用
        if(!AliStringUtils.isEmpty(addressObjectName)) {
            dto.setName(addressObjectName);
            dto.setCommandLine("");
            return dto;
        }

        //不强制创建对象的时候不对单个ip创建对象
        if(!createObject) {
            //若为单独IP地址，则不创建对象组
            if (IpUtils.isIP(ipAddresses)) {
                dto.setName(ipAddresses);
                dto.setCommandLine("");
                return dto;
            }
        }

        StringBuilder sb = new StringBuilder();
        String objectName = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
        String[] ipAddressList = ipAddresses.split(",");
        if(ipAddressList.length == 1) {
            sb.append(getAddressObject(ipAddressList[0], objectName));
        } else {
            List<String> objectJoinList = new ArrayList<>();
            for (String ipAddress : ipAddressList) {
                String join = String.format("%s_AG_%s", name, IdGen.getRandomNumberString());
                if(IpUtils.isIPSegment(ipAddress) || IpUtils.isIPRange(ipAddress)) {
                    sb.append(getAddressObjectForObjectGroup(ipAddress, join));
                    join = "object " + join;
                } else {
                    join = "host " + ipAddress;
                }
                objectJoinList.add(join);
            }

            sb.append(String.format("object-group network %s\n", objectName));
            for(String join:objectJoinList) {
                sb.append("network " + join + "\n");
            }
        }

        dto.setName(objectName);
        dto.setCommandLine(sb.toString());

        return dto;
    }

    private String preCreateAddressObject() {
        return "config firewall address\n";
    }

    private String createServiceObject(ServiceDTO service, List<String> serviceNameList,List<String> createServiceObjectNames) {
        StringBuilder sb = new StringBuilder();
        String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
        //若端口为空，则服务名称为TCP_ALL，ICMP_ALL，UDP_ALL等
        if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            serviceNameList.add(String.format("ALL_%s", protocol.toUpperCase()));
            return "";
        }

        String dstPort = service.getDstPorts();
        String[] dstPortList = dstPort.split(",");

        //同一个协议，对于每个port都生成一个服务
        for (String port : dstPortList) {
            sb.append(preCreateServiceObject());
            String serviceName = String.format("\"service_%s\"", protocol);
            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    serviceName = String.format("\"%s_%s-%s\"", protocol.toLowerCase(), startPort, endPort);
                } else {
                    serviceName = String.format("\"%s_%s\"", protocol.toLowerCase(), port);
                }
            }

            sb.append(String.format("edit %s\n", serviceName));
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set tcp-portrange %s %s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set tcp-portrange %s\n", port));
                }
            } else if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set udp-portrange %s\n", port));
                }
            } else {
                sb.append(String.format("set protocol %s\n", protocol.toUpperCase()));
            }
            serviceNameList.add(serviceName);
            createServiceObjectNames.add(serviceName);
            sb.append("next\n");
            sb.append("end\n\n");
        }
        return sb.toString();
    }

    private String createServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList,List<String> createServiceObjectNames) {
        StringBuilder sb = new StringBuilder();

        for (ServiceDTO serviceDTO : serviceDTOList) {
            String command = createServiceObject(serviceDTO, existServiceNameList,createServiceObjectNames);
            sb.append(command);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String createAddressObject(String srcIp, List<String> addressNameList,String theme,List<String> createAddressObjectNames) {
        StringBuilder sb = new StringBuilder();
        String addressObjectName = "";
        sb.append(preCreateAddressObject());
        if (IpUtils.isIPRange(srcIp)) {
            String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
            String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
            addressObjectName = String.format("\"%s-%s\"", startIp, endIp);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append("set type iprange\n");
            sb.append(String.format("set start-ip %s\n", startIp));
            sb.append(String.format("set end-ip %s\n", endIp));
        } else if (IpUtils.isIPSegment(srcIp)) {
            String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
            String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp);
            addressObjectName = String.format("\"%s/%s\"", ipFromIpSegment, maskBitFromIpSegment);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append(String.format("set subnet %s/%s\n", ipFromIpSegment, maskBitFromIpSegment));
        } else if(IpUtils.isIP(srcIp)){
            addressObjectName = String.format("\"%s/32\"", srcIp);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append(String.format("set subnet %s/32\n", srcIp));
        }else if(ObjectUtils.notEqual(srcIp,"any")){
            addressObjectName = String.format("%s_AO_%s",theme, IdGen.getRandomNumberString());
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append("set type fqdn\n");
            sb.append(String.format("set fqdn %s\n",srcIp));
        }

        sb.append("next\n");
        sb.append("end\n\n");
        addressNameList.add(addressObjectName);
        createAddressObjectNames.add(addressObjectName);
        return sb.toString();
    }

    String preCreatePolicy() {
        return "config firewall policy\n";
    }

    /**
     * 新建地址池
     * @param ipAddress  地址池信息
     * @param ticket  工单名称
     * @return
     */
    public PolicyObjectDTO generateAddressPool(String ipAddress,String postSrcAddressName, String ticket){
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (StringUtils.isNotBlank(postSrcAddressName)) {
            dto.setObjectFlag(false);
            dto.setName(postSrcAddressName);
            return dto;
        }

        dto.setObjectFlag(true);
        if(StringUtils.isBlank(ipAddress)){
            return dto;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("config firewall ippool\n");
        String setName = String.format("%s_pool_%s", ticket, IdGen.getRandomNumberString());
        setName = String.format("\"%s\"", setName);
        sb.append("edit ").append(setName).append("\n");
        String[] arr = ipAddress.split(",");

        for (String address : arr) {

            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                sb.append("set startip ").append(startIp).append("\n");
                sb.append("set endip ").append(endIp).append("\n");
                sb.append("next\n");
                sb.append("end\n");
                // TODO 这里创建地址池的时候，只取了第一个IP，暂不支持IPV6
                break;
            } else if (IpUtils.isIPSegment(address)) {
                //是子网段，转换成范围
                String ip = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                sb.append("set startip ").append(startIp).append("\n");
                sb.append("set endip ").append(endIp).append("\n");
                sb.append("next\n");
                sb.append("end\n");
                // TODO 这里创建地址池的时候，只取了第一个IP，暂不支持IPV6
                break;
            } else  {
                sb.append("set startip ").append(address).append("\n");
                sb.append("set endip ").append(address).append("\n");
                sb.append("next\n");
                sb.append("end\n");
                // TODO 这里创建地址池的时候，只取了第一个IP，暂不支持IPV6
                break;
            }
        }

        String command  = sb.toString();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(command);
        return dto;

    }

    String createPolicy(String theme,String policyName, List<String> srcItfList, List<String> dstItfList,
                        List<String> serviceNameList, String description,PolicyObjectDTO srcPostObject,
                        List<String> existSrcAddressList,List<String> existDstAddressList, String moveBefore, String moveAfter) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", "0"));
        sb.append("set name ").append(policyName).append(StringUtils.LF);
        if (CollectionUtils.isNotEmpty(srcItfList)) {
            sb.append("set srcintf ");
            for (String itf : srcItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }else{
            sb.append("set srcintf \"any\"").append("\n");
        }

        if (CollectionUtils.isNotEmpty(dstItfList)) {
            sb.append("set dstintf ");
            for (String itf : dstItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }else{
            sb.append("set dstintf \"any\"").append("\n");
        }
        if (CollectionUtils.isNotEmpty(existSrcAddressList)) {
            sb.append("set srcaddr ");
                for (String addressObject : existSrcAddressList) {
                    sb.append(String.format("%s ", addressObject));
                }
            sb.append("\n");
        }else{
            sb.append("set srcaddr all\n");
        }

        if (CollectionUtils.isNotEmpty(existDstAddressList)) {
            sb.append("set dstaddr ");
            for (String addressObject : existDstAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }else{
            sb.append("set dstaddr all\n");
        }

        if (CollectionUtils.isNotEmpty(serviceNameList)) {
            sb.append("set service ");
            for (String service : serviceNameList) {
                sb.append(String.format("%s ", service));
            }
            sb.append("\n");
        }

        sb.append("set schedule always\n");
        sb.append("set action accept\n");

        sb.append("set nat enable\n");
        if (null != srcPostObject && StringUtils.isNotBlank(srcPostObject.getName())) {
            sb.append("set ippool enable\n");
            sb.append(String.format("set poolname %s\n",srcPostObject.getName()));
        }
        if (!AliStringUtils.isEmpty(description)) {
            //排除命令行为空格
            if (description.trim().length() > 0) {
                sb.append(String.format("set comments %s\n", description));
            }
        }
        sb.append("show\n");
        sb.append("next\n");

        policyName = "#1";
        if (!AliStringUtils.isEmpty(moveBefore)) {
            sb.append(String.format("move %s before %s\n", policyName, moveBefore));
        } else if (!AliStringUtils.isEmpty(moveAfter)) {
            sb.append(String.format("move %s after %s\n", policyName, moveAfter));
        }
        sb.append("\nend\n\n");

        return sb.toString();
    }

    public static void main(String[] args) {
//       FortinetNat fortinet = new FortinetNat();
//
//       // StaticNatTaskDTO staticDto = new StaticNatTaskDTO();
//
//       // staticDto.setGlobalAddress("192.168.1.1,192.168.0.1/25,192.168.2.13-192.168.2.15");
//       // staticDto.setInsideAddress("2.2.2.2");
//       // staticDto.setProtocol("6");
//       // staticDto.setGlobalPort("any");
//       // staticDto.setInsidePort("any");
//       // staticDto.setTheme("static001");
//       // staticDto.setInsideAddress("1.2.3.4,4.3.2.1/24");
//       // staticDto.setInsideAddressName("inpolicy");
//       // staticDto.setInDevItf("inxq1");
//       // staticDto.setOutDevItf("outnana1");
//       // staticDto.setCurrentId("21");
//       // System.out.println(fortinet.generateStaticNatCommandLine(staticDto));
////
//        //源nat
//
//        SNatPolicyDTO dNatPolicyDTO = new SNatPolicyDTO();
//        dNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
//        dNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
//
//        dNatPolicyDTO.setPostIpAddress("192.168.1.0/24");
//        dNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
//        dNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
//        dNatPolicyDTO.setCreateObjFlag(true);
//        dNatPolicyDTO.setSrcZone("trust");
//        dNatPolicyDTO.setDstZone("untrust");
//
//        dNatPolicyDTO.setSrcItf("srcItf");
//        dNatPolicyDTO.setDstItf("dstItf");
//
//        dNatPolicyDTO.setTheme("w1");
//        dNatPolicyDTO.setCurrentId("test snat");
//
//
//        List<String> a = new ArrayList<>();
//        a.add("www.baidu.com");
//        a.add("2.2.2.2/20");
//        dNatPolicyDTO.setExistDstAddressList(a);
//
//        List<String> b = new ArrayList<>();
//        b.add("5.5.5.5");
//        b.add("6.6.6.6");
//        b.add("www.4399.com");
//        dNatPolicyDTO.setRestDstAddressList(b);
//        dNatPolicyDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
//        System.out.println(fortinet.generateSNatCommandLine(dNatPolicyDTO));

//        DNatPolicyDTO dNatPolicyDTO = new DNatPolicyDTO();
//        //dNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
//        List<String> a = new ArrayList<>();
//        a.add("abc");
//        List<String> b = new ArrayList<>();
//        b.add("2.2.2.2");
//        b.add("6.6.6.6/20");
//        b.add("5.5.5.5-5.5.5.12");
//        dNatPolicyDTO.setRestSrcAddressList(b);
//        dNatPolicyDTO.setExistSrcAddressList(a);
//        dNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
//        dNatPolicyDTO.setPostPort("any");
//        dNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
//        List<ServiceDTO> serviceList;
//
//
//
//        List<ServiceDTO> serviceDTOList = new ArrayList<>();
//        ServiceDTO serviceDTO = new ServiceDTO();
//        serviceDTO.setDstPorts("80");
//        serviceDTO.setProtocol("6");
//        serviceDTOList.add(serviceDTO);
//
//        dNatPolicyDTO.setServiceList(serviceDTOList);
//
//        dNatPolicyDTO.setCreateObjFlag(true);
//        dNatPolicyDTO.setSrcZone("trust");
//        dNatPolicyDTO.setDstZone("untrust");
//
//        dNatPolicyDTO.setSrcItf("srcItf");
//        dNatPolicyDTO.setDstItf("dstItf");
//
//
//        dNatPolicyDTO.setTheme("w1");
//        dNatPolicyDTO.setCurrentId("test dnat");
//        System.out.println(fortinet.generateDNatCommandLine(dNatPolicyDTO));
    }
}
