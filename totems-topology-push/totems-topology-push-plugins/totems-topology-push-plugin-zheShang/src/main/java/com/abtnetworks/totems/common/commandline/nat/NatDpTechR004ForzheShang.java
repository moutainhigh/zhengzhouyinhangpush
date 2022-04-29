package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @date 2021/6/24 15:29
 */
@Slf4j
@Service
public class NatDpTechR004ForzheShang implements NatPolicyGenerator {

    public final int MAX_OBJECT_NAME_LENGTH = 44;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate dp R004 nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        return null;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("language-mode chinese\nconf-mode\n");

        //调用安全策略的处理对象的方法
        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), policyDTO.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), policyDTO.getDstIpSystem());
        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(policyDTO.getServiceList(), policyDTO.getServiceObjectName());

        //源地址 转换后， 生成地址池对象  每次都新建
        PolicyObjectDTO srcPostObject = generateAddressPool(policyDTO.getPostIpAddress(), policyDTO.getTheme(), null,policyDTO.getPostSrcIpSystem());

        //事先声明对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())
                && (StringUtils.isNotBlank(policyDTO.getSrcIp()) || StringUtils.isNotBlank(policyDTO.getSrcAddressObjectName()))) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())
                && (StringUtils.isNotBlank(policyDTO.getDstIp()) || StringUtils.isNotBlank(policyDTO.getDstAddressObjectName()))) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }
        if (serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
                    sb.append(serviceObject.getCommandLine());
                }
            }
            sb.append("\n");
        }

        if (StringUtils.isNotBlank(srcPostObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcPostObject.getCommandLine()));
        }


        String natPolicy = String.format("nat source-nat %s ", policyDTO.getTheme());
        //出接口
        if (StringUtils.isNotBlank(policyDTO.getDstItf())) {
            sb.append(natPolicy).append(String.format("interface %s \n", policyDTO.getDstItf()));
        } else {
            sb.append(natPolicy).append("interface any \n");
        }

        //源地址
        if (srcAddressObject.getCommandLine() != null && srcAddressObject.getCommandLine().contains("ipv6 address-object")) {
            sb.append(natPolicy).append(String.format("src-address ipv6 address-object %s\n", srcAddressObject.getJoin()));
        } else if (StringUtils.isNotBlank(srcAddressObject.getJoin()) && StringUtils.isNotBlank(policyDTO.getSrcIp())) {
            sb.append(natPolicy).append(String.format("src-address address-object %s\n", srcAddressObject.getJoin()));
        } else if (StringUtils.isAllBlank(policyDTO.getSrcIp(), policyDTO.getSrcAddressObjectName())) {
            sb.append(natPolicy).append("src-address any \n");
        }

        //目的地址
        if (dstAddressObject.getCommandLine() != null && dstAddressObject.getCommandLine().contains("ipv6 address-object")) {
            sb.append(natPolicy).append(String.format("dst-address ipv6 address-object %s\n", dstAddressObject.getJoin()));
        } else if (StringUtils.isNotBlank(dstAddressObject.getJoin()) && StringUtils.isNotBlank(policyDTO.getDstIp())) {
            sb.append(natPolicy).append(String.format("dst-address address-object %s\n", dstAddressObject.getJoin()));
        } else if (StringUtils.isAllBlank(policyDTO.getDstIp(), policyDTO.getDstAddressObjectName())) {
            sb.append(natPolicy).append("dst-address any \n");
        }

        //服务
        if (serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                sb.append(natPolicy).append(String.format("service service-object %s\n", serviceObject.getJoin()));
            }
        } else {
            sb.append(natPolicy).append("service any\n");
        }

        //源地址转换后 地址池引用
        if (StringUtils.isNotBlank(srcPostObject.getCommandLine())) {
            //KSH-4801 添加action
            sb.append(natPolicy).append(String.format("action address-pool %s\n", srcPostObject.getJoin()));
        }
        sb.append(StringUtils.LF);
        sb.append("end").append(StringUtils.LF);
        sb.append("write file").append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("language-mode chinese\nconf-mode\n");
        String command = dstIpTranslate(policyDTO.getTheme(), policyDTO.getServiceList(), policyDTO.getDstIp(),
                policyDTO.getPostIpAddress(), policyDTO.getPostPort(), policyDTO.getSrcItf());
        sb.append(command);

        sb.append(StringUtils.LF);
        sb.append("end").append(StringUtils.LF);
        sb.append("write file").append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("language-mode chinese\nconf-mode\n");

        //调用安全策略的处理对象的方法
        PolicyObjectDTO srcAddressObject = generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), policyDTO.getSrcIpSystem());
        PolicyObjectDTO srcPostAddressObject = generateAddressPool(policyDTO.getPostSrcIp(), policyDTO.getTheme(), null,policyDTO.getPostSrcIpSystem());

        //事先声明对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())
                && (StringUtils.isNotBlank(policyDTO.getSrcIp()) || StringUtils.isNotBlank(policyDTO.getSrcAddressObjectName()))) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }
        if (StringUtils.isNotBlank(srcPostAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcPostAddressObject.getCommandLine()));
        }

        String natPolicy = String.format("nat destination-nat %s ", policyDTO.getTheme());

        //目的地址转换前、后
        String dstCommand = dstIpTranslate(policyDTO.getTheme(), policyDTO.getServiceList(), policyDTO.getDstIp(),
                policyDTO.getPostDstIp(), policyDTO.getPostPort(), policyDTO.getSrcItf());
        sb.append(dstCommand);

        //源地址转换前
        if (StringUtils.isNotBlank(policyDTO.getSrcIp()) && srcAddressObject != null) {
            sb.append(natPolicy).append(String.format("src-address address-object %s\n", srcAddressObject.getJoin()));
        } else if (StringUtils.isBlank(policyDTO.getSrcIp())) {
            sb.append(natPolicy).append("src-address any \n");
        }

        //源地址转换后
        if (StringUtils.isNotBlank(policyDTO.getPostSrcIp()) && srcPostAddressObject != null) {
            sb.append(natPolicy).append(String.format("src-address-translate address-pool %s\n", srcPostAddressObject.getJoin()));
        }
        sb.append(StringUtils.LF);
        sb.append("end").append(StringUtils.LF);
        sb.append("write file").append(StringUtils.LF);
        return sb.toString();
    }

    /**
     * 新建地址池
     * @param ipAddress  地址池信息
     * @param ticket  工单名称
     * @param existsAddressName  已存在地址池名称
     * @return
     */
    public PolicyObjectDTO generateAddressPool(String ipAddress, String ticket, String existsAddressName,String postIpSystem){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            return dto;
        }

        String addressCmd = "";
        StringBuilder sb = new StringBuilder();
        String setName = null;
        if(StringUtils.isNotBlank(postIpSystem)){
            setName = dealIpSystemName(postIpSystem);
        } else {
            setName = String.format("%s_pool_%s", ticket, IdGen.getRandomNumberString());
        }

        String[] arr = ipAddress.split(",");

        for (String address : arr) {

            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                addressCmd = String.format("%s to %s\n", startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                //是子网段，转换成范围
                String ip = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                addressCmd = String.format("%s to %s\n", startIp, endIp);
            } else if (IpUtils.isIP(address)) {
                addressCmd = String.format("%s\n", address);
            } else if (address.contains(":")) {
                log.info("迪普R004 新建地址池，遇到IPv6, address:{}", address);
                //ipv6
                if (address.contains("/")) {
                    addressCmd = String.format("%s\n", address);
                } else if (address.contains("-")) {
                    String[] addrArray = address.split("-");
                    if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                        addressCmd = String.format("%s to %s\n", addrArray[0], addrArray[1]);
                    }
                } else {
                    addressCmd = String.format("%s/128\n", address);
                }
            }
            sb.append(String.format("address-pool %s address %s", setName, addressCmd));
        }

        String command  = sb.toString();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(command);

        return dto;

    }

    /**
     * 处理系统名称 作为地址对象名称去创建对象
     * @param ipSystem
     */
    private String dealIpSystemName(String ipSystem) {
        String setName = ipSystem;
        // 对象名称长度限制，一个中文2个字符
        setName = strSub(setName, getMaxObejctNameLength(), "GB2312");
        // 对象名称长度限制
        int len = 0;
        try {
            len = setName.getBytes("GB2312").length;
        } catch (Exception e) {
            log.error("字符串长度计算异常");
        }
        if (len > getMaxObejctNameLength() - 7) {
            setName = strSub(setName, getMaxObejctNameLength() - 7, "GB2312");
        }
        setName = String.format("%s_%s", setName, DateUtils.getDate().replace("-", "").substring(2));
        return setName;
    }


    /**
     * 目的IP转换命令
     * @return
     */
    public String dstIpTranslate(String theme, List<ServiceDTO> serviceList,
                                 String dstIp, String postDstIp, String postPort,
                                 String srcItf){
        StringBuilder sb = new StringBuilder();
        String natPolicy = String.format("nat destination-nat %s", theme);
        //必填项 入接口，目的地址转换前、 目的地址转换后

        //优先判断服务是否配置，若有配置，则生成服务范围
        String serviceCommand = generateServiceRange(serviceList);

        //目的地址转换前  global-address 取第一个，且只能为单个IP
        String globalAddress = getFirstIp(dstIp);


        //目的地址转换后 local-address 取第一个，可以是单IP、范围，遇到子网处理成范围
        String localAddress = getFirstIpAndRange(postDstIp);

        //转换后端口  仅支持1个，仅为单个端口， 为any时，就空着不写
        String postPortString = "";
        if (StringUtils.isNotBlank(postPort) && !postPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            //若转换后端口是范围，则取开始数字就好
            if (PortUtils.isPortRange(postPort)) {
                postPort = PortUtils.getStartPort(postPort);
            }
            postPortString = String.format(" local-port %s", postPort);
        }

        sb.append(natPolicy).append(String.format(" interface %s", StringUtils.isNotBlank(srcItf) ? String.format("%s ", srcItf) : srcItf))
                .append(String.format("global-address%s", StringUtils.isNotBlank(globalAddress) ? String.format(" %s", globalAddress) : globalAddress));
        if (StringUtils.isNotBlank(serviceCommand)) {
            sb.append(serviceCommand);
        }
        sb.append(String.format(" local-address%s", StringUtils.isNotBlank(localAddress) ? String.format(" %s", localAddress) : localAddress));
        if (StringUtils.isNotBlank(postPortString)) {
            sb.append(postPortString);
        }

        sb.append("\n");

        return sb.toString();
    }

    /**
     * 新建服务范围
     * @return
     */
    public String generateServiceRange(List<ServiceDTO> serviceList) {
        StringBuilder serviceSb = new StringBuilder();
        if (serviceList == null || serviceList.isEmpty()) {
            return "";
        }

        //若是1条服务，且是any的，则跳过生成
        if (serviceList.size() == 1 && serviceList.get(0).getProtocol().equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ANY)) {
            return "";
        }

        serviceSb.append(" service");
        for (ServiceDTO service : serviceList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                serviceSb.append(" icmp");
                continue;
            }
            String[] dstPorts = service.getDstPorts().split(",");
            for (String dstPort : dstPorts) {
                if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    serviceSb.append(String.format(" %s 0 to 65535", protocolString));
                } else if (PortUtils.isPortRange(dstPort)) {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
                    serviceSb.append(String.format(" %s %s to %s", protocolString, startPort, endPort));
                } else {
                    serviceSb.append(String.format(" %s %s to %s", protocolString, dstPort, dstPort));
                }
            }

        }
        return serviceSb.toString();
    }

    /**
     * 取第一个单IP，若是范围，则取开始IP
     * @return
     */
    public String getFirstIp(String ipStr) {
        String globalAddress = "";
        if (StringUtils.isBlank(ipStr)) {
            return globalAddress;
        }
        String[] dstIps = ipStr.split(",");
        for (String address : dstIps) {
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                globalAddress = startIp;
            } else if (IpUtils.isIPSegment(address)) {
                //是子网段，转换成范围
                String ip = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                globalAddress = startIp;
            } else if (IpUtils.isIP(address)) {
                globalAddress = address;
            }

            //只取第一个
            if (StringUtils.isNotBlank(globalAddress)) {
                break;
            }
        }
        return globalAddress;
    }

    /**
     * 取第一个IP，可以是单IP、范围，若遇到子网则转换成范围
     * @return
     */
    public String getFirstIpAndRange(String ipStr) {
        String localAddress = "";
        if (StringUtils.isBlank(ipStr)) {
            return localAddress;
        }

        String[] postDstIps = ipStr.split(",");
        for (String address : postDstIps) {
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                localAddress = String.format("%s to %s", startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                //是子网段，转换成范围
                String ip = IpUtils.getIpFromIpSegment(address);
                //获取网段数
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                long[] ipArr = IpUtils.getIpStartEndBySubnetMask(ip, maskBit);
                String startIp = IpUtils.IPv4NumToString(ipArr[0]);
                String endIp = IpUtils.IPv4NumToString(ipArr[1]);
                localAddress = String.format("%s to %s", startIp, endIp);
            } else if (IpUtils.isIP(address)) {
                localAddress = String.format("%s to %s", address, address);;
            }

            //只取第一个
            if (StringUtils.isNotBlank(localAddress)) {
                break;
            }
        }
        return localAddress;
    }

    /**
     * 获取地址对象
     * @param ipAddress ip地址
     * @return 地址对象
     */
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);

        if(AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin("any");
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            return dto;
        }

        String addressCmd = "";

        StringBuilder sb = new StringBuilder();

        String[] arr = ipAddress.split(",");
        boolean isIpv6 = false;
        String setName = null;
        if(StringUtils.isNotEmpty(ipSystem)){
            setName = dealIpSystemName(ipSystem);
        } else {
            setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }
        for (String address : arr) {
            sb.append(String.format("address-object %s ", setName));
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                addressCmd = String.format("range %s %s\n", startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                addressCmd = String.format("%s\n", address);
            } else if (IpUtils.isIP(address)) {
                addressCmd = String.format("%s\n", address + "/32");
            } else if (address.contains(":")) {
                isIpv6 = true;
                //ipv6
                if (address.contains("/")) {
                    addressCmd = String.format("%s\n", address);
                } else if (address.contains("-")) {
                    String[] addrArray = address.split("-");
                    if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                        addressCmd = String.format("range %s %s\n", addrArray[0], addrArray[1]);
                    }
                } else {
                    addressCmd = String.format("%s/128\n", address);
                }
            }

            sb.append(addressCmd);
        }

        String command = sb.toString();
        if (isIpv6) {
            command = command.replaceAll("address-object", "ipv6 address-object");
        }

        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(command);
        return dto;
    }

    /**
     * 获取服务集对象文本
     * @param serviceDTOList 协议
     * @return 服务集对象
     */
    public List<PolicyObjectDTO> generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName) {
        List<PolicyObjectDTO> policyObjectList = new ArrayList<>();
        if(StringUtils.isNotBlank(existsServiceName)){
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setObjectFlag(true);
            dto.setJoin(existsServiceName);
            policyObjectList.add(dto);
            return policyObjectList;
        }

        for(ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return null;
            }

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                StringBuilder sb = new StringBuilder();
                String setName = getServiceName(service);
                sb.append(String.format("service-object %s protocol icmp ", setName));

                if (StringUtils.isNotBlank(service.getType()) && StringUtils.isNotBlank(service.getCode())) {
                    sb.append(String.format("type %d code %d\n", Integer.valueOf(service.getType()), Integer.valueOf(service.getCode())));
                }
                sb.append("\n");
                PolicyObjectDTO dto = new PolicyObjectDTO();
                dto.setObjectFlag(true);
                dto.setName(setName);
                dto.setJoin(setName);
                dto.setCommandLine(sb.toString());
                policyObjectList.add(dto);
            } else {
                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");

                for (String srcPort : srcPorts) {
                    for (String dstPort : dstPorts) {
                        StringBuilder sb = new StringBuilder();
                        String setName = getServiceNameByOne(protocolString, dstPort);
                        sb.append(String.format("service-object %s protocol %s ", setName, protocolString));
                        if (!srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)
                                || !dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append("src-port 0 to 65535 ");
                            } else if (PortUtils.isPortRange(srcPort)) {
                                String startPort = PortUtils.getStartPort(srcPort);
                                String endPort = PortUtils.getEndPort(srcPort);
                                sb.append(String.format("src-port %s to %s ", startPort, endPort));
                            } else {
                                sb.append(String.format("src-port %s to %s", srcPort,srcPort));
                            }

                            if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append("dst-port 0 to 65535 ");
                            } else if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("dst-port %s to %s ", startPort, endPort));
                            } else {
                                sb.append(String.format("dst-port %s to %s", dstPort,dstPort));
                            }
                        }
                        sb.append("\n");
                        PolicyObjectDTO dto = new PolicyObjectDTO();
                        dto.setObjectFlag(true);
                        dto.setName(setName);
                        dto.setJoin(setName);
                        dto.setCommandLine(sb.toString());
                        policyObjectList.add(dto);
                    }
                }
            }
        }

        return policyObjectList;
    }

    /**
     * 判断传进来的字符串，是否
     * 大于指定的字节，如果大于递归调用
     * 直到小于指定字节数 ，一定要指定字符编码，因为各个系统字符编码都不一样，字节数也不一样
     * @param s
     *            原始字符串
     * @param num
     *            传进来指定字节数
     * @return String 截取后的字符串

     * @throws
     */
    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            log.error("字符串长度计算异常");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
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
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

    public String getServiceNameByOne(String protocolString, String dstPort) {
        if(dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
            return protocolString;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%s", protocolString, dstPort));
        return sb.toString().toLowerCase();
    }

    public static void main(String[] args) {
        NatDpTechR004ForzheShang r004 = new NatDpTechR004ForzheShang();
        System.out.println("--------------------------------------------------------------------------");
        //源nat
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
//        sNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        sNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        sNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());

        sNatPolicyDTO.setSrcZone("trust");
        sNatPolicyDTO.setDstZone("untrust");

        sNatPolicyDTO.setSrcItf("srcItf");
        sNatPolicyDTO.setDstItf("dstItf");

        sNatPolicyDTO.setTheme("w1");

     /*   sNatPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        sNatPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());

        sNatPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
        sNatPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
        sNatPolicyDTO.setPostAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());*/

        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");


        DNatPolicyDTO dnatPolicyDTO = new DNatPolicyDTO();

        dnatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        dnatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        dnatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        dnatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        dnatPolicyDTO.setPostPort("27");

        dnatPolicyDTO.setSrcZone("trust");
        dnatPolicyDTO.setDstZone("untrust");

        dnatPolicyDTO.setSrcItf("srcItf");
        dnatPolicyDTO.setDstItf("dstItf");

        dnatPolicyDTO.setTheme("w1");
        String dnat = r004.generateDNatCommandLine(dnatPolicyDTO);
//        System.out.println(dnat);

        System.out.println("--------------------------------------------------------------------------");
        NatPolicyDTO bothNatDTO = new NatPolicyDTO();
//        bothNatDTO.setSrcIp("192.168.2.1,192.168.2.2");
//        bothNatDTO.setDstIp("172.16.2.1,172.16.2.2");
        bothNatDTO.setSrcIp("");
        bothNatDTO.setDstIp("");
        bothNatDTO.setPostSrcIp("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        bothNatDTO.setPostDstIp("172.16.1.0/24,7.7.7.7,63.2.2.2-75.3.3.6");
        bothNatDTO.setServiceList(ServiceDTO.getServiceList());
        bothNatDTO.setPostPort("27");

        bothNatDTO.setSrcZone("trust");
        bothNatDTO.setDstZone("untrust");

        bothNatDTO.setSrcItf("srcItf");
        bothNatDTO.setDstItf("dstItf");

        bothNatDTO.setTheme("w1");
        String bothNat = r004.generateBothNatCommandLine(bothNatDTO);
//        System.out.println(bothNat);

    }

}
