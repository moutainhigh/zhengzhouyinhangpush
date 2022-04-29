package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.ProtocolEnum;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service(value = "Juniper NAT")
public class JuniperNat implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate Juniper nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    // 静态
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    // 源
    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure\n");

        // 策略名称
        String natName = String.format("%s_AG_%s", policyDTO.getTheme(), IdGen.getRandomNumberString());

        //  1)地址池
        // 转换后地址
        String postAddressObjectName = policyDTO.getPostAddressObjectName();
        String poolName="";
        if (StringUtils.isNotBlank( postAddressObjectName )) {
            // 可整体复用 无需再创建地址池
            poolName=postAddressObjectName;
        }else{
            // 不可复用 取值创建
            String postIpAddress = policyDTO.getPostIpAddress();
            // 当地址any时，在Nat策略中无需输入对应的命令行
            if ( StringUtils.isNotBlank( postIpAddress ) && !StringUtils.equalsIgnoreCase( "any",postIpAddress ) ){
                // 取第一个poolName
                String[] split = postIpAddress.split(",");
                String srcIp_0 = split[0];
                if (IpUtils.isIPRange(srcIp_0)) {
                    // 范围
                    String startIp = IpUtils.getStartIpFromIpAddress(srcIp_0);
                    String endIp = IpUtils.getEndIpFromIpAddress(srcIp_0);
                    int index=endIp.lastIndexOf(".");
                    String substring = endIp.substring(index+1, endIp.length());
                    String nIp = startIp+"_"+substring;
                    poolName = nIp.replace(".", "-");
                } else if (IpUtils.isIPSegment(srcIp_0)) {
                    // 网段
                    String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp_0);
                    String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp_0);
                    String ip = ipFromIpSegment + "/" + maskBitFromIpSegment;
                    poolName = ip.replace(".", "-");
                } else if (IpUtils.isIP(srcIp_0)) {
                    // 单ip
                    String ip = String.format("%s", srcIp_0);
                    poolName = ip.replace(".", "-");
                }
                for (String srcIp : split) {
                    if (IpUtils.isIPRange(srcIp)) {
                        // 范围
                        String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
                        String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
                        int index=endIp.lastIndexOf(".");
                        String substring = endIp.substring(index+1, endIp.length());
                        String nIp = startIp+"_"+substring;
                        sb.append(String.format("set security nat source pool %s address %s to %s\n", poolName, startIp, endIp));
                    } else if (IpUtils.isIPSegment(srcIp)) {
                        // 网段
                        String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                        String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp);
                        String ip = ipFromIpSegment + "/" + maskBitFromIpSegment;
                        sb.append(String.format("set security nat source pool %s address %s\n", poolName, ip));
                    } else if (IpUtils.isIP(srcIp)) {
                        // 单ip
                        String ip = String.format("%s/32", srcIp);
                        sb.append(String.format("set security nat source pool %s address %s\n", poolName, ip));
                    }
                }

            }
        }

        // 2）创建策略集
        String srcZone = StringUtils.isBlank( policyDTO.getSrcZone() ) ? "any": policyDTO.getSrcZone();
        String dstZone = StringUtils.isBlank( policyDTO.getDstZone() ) ? "any": policyDTO.getDstZone();
        String policySetName = srcZone + "_to_" + dstZone;
        sb.append(String.format("set security nat source rule-set %s from zone %s\n", policySetName, srcZone));
        sb.append(String.format("set security nat source rule-set %s to zone %s\n", policySetName, dstZone));

        // 3）创建策略
        // 转换前的源地址
        String srcIp = policyDTO.getSrcIp();
        if ( StringUtils.isNotBlank( srcIp ) && !StringUtils.equals( "any",srcIp ) ){
            String[] split = srcIp.split(",");
            for (String s : split) {
                if (IpUtils.isIPRange(s)) {
                    // 范围
       //             String startIp = IpUtils.getStartIpFromIpAddress(s);
                    String ips = IpUtils.convertIpRangeToSegment(s);
                    String[] ipArr = ips.split(",");
                    for (String ip : ipArr) {
                        sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s\n", policySetName, natName, ip));
                    }
                 //   sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s/32\n", policySetName, natName, startIp));
                } else if (IpUtils.isIPSegment(s)){
                    // 网段
                    sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s\n", policySetName, natName, s));
                }else if (IpUtils.isIP(s)){
                    // 单ip
                    sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s/32\n", policySetName, natName, s));
                }

            }
        }
        // 转换前目的地址
        String dstIp = policyDTO.getDstIp();
        if ( StringUtils.isNotBlank( dstIp ) && !StringUtils.equals( "any",dstIp ) ){
            String[] split = dstIp.split(",");
            for (String s : split) {
                if (IpUtils.isIPRange(s)) {
                    // 范围
           //         String startIp = IpUtils.getStartIpFromIpAddress(s);
                    String ips = IpUtils.convertIpRangeToSegment(s);
                    String[] ipArr = ips.split(",");
                    for (String ip : ipArr) {
                        sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s\n", policySetName, natName, ip));
                    }
         //           sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, startIp));
                } else if (IpUtils.isIPSegment(s)) {
                    // 网段
                    sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s\n", policySetName, natName, s));
                } else if (IpUtils.isIP(s)) {
                    // 单ip
                    sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, s));
                }
            }
        }
        // 服务 用户在页面填写多个协议时命令行都生成，但端口只取第一个协议的单端口或单端口范围
        List<ServiceDTO> serviceList = policyDTO.getServiceList();
        if ( CollectionUtils.isNotEmpty( serviceList ) ){
            for (ServiceDTO serviceDTO : serviceList) {
                // 协议
                String protocol = serviceDTO.getProtocol();
                if (  StringUtils.isNotBlank( protocol ) && !StringUtils.equalsIgnoreCase( "any", protocol ) && !StringUtils.equalsIgnoreCase( "0", protocol)){
                    String desc = ProtocolEnum.getDescByCode(protocol);
                    sb.append(String.format("set security nat source rule-set %s rule %s match protocol %s\n", policySetName, natName, desc.toLowerCase()));
                }
            }
            // 端口-只取第一个协议的单端口或单端口范围(服务对象为多个，第一个服务没端口，依次往下寻找端口，直到找到一个端口为止)
            List<String> list = serviceList.stream().filter(t-> !StringUtils.equalsIgnoreCase("any", t.getDstPorts())).map(ServiceDTO::getDstPorts).collect(Collectors.toList());
            if ( CollectionUtils.isNotEmpty( list ) ){
                String dstPorts = list.get(0);
                String[] port = dstPorts.split(",");
                if ( port[0].contains("-") ){
                    String[] split = port[0].split("-");
                    sb.append(String.format("set security nat source rule-set %s rule %s match destination-port %s to %s\n", policySetName, natName, split[0], split[1]));
                }else{
                    if (!StringUtils.equalsIgnoreCase( "any", port[0]) ){
                        sb.append(String.format("set security nat source rule-set %s rule %s match destination-port %s\n", policySetName, natName, port[0]) );
                    }
                }
            }
        }
        // 转
        if ( StringUtils.isNotBlank( poolName ) ){
            sb.append(String.format("set security nat source rule-set %s rule %s then source-nat pool %s\n", policySetName, natName, poolName ));
        }

        sb.append("commit\n");
        sb.append("exit");

        return sb.toString();
    }


    // 目的
    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure\n");

        // 策略名称
        String natName = String.format("%s_AG_%s", policyDTO.getTheme(), IdGen.getRandomNumberString());

        // 1）创建转换后的地址池与端口
        // 转换后地址
        String postAddressObjectName = policyDTO.getPostAddressObjectName();
        String poolName="";
        if (StringUtils.isNotBlank( postAddressObjectName )) {
            // 可整体复用 无需再创建地址池
            poolName=postAddressObjectName;
        }else{
            // 不可复用 取值创建
            String postIpAddress = policyDTO.getPostIpAddress();
            // 当地址any时，在Nat策略中无需输入对应的命令行
            if ( StringUtils.isNotBlank( postIpAddress ) && !StringUtils.equalsIgnoreCase( "any",postIpAddress ) ){
                String[] split = postIpAddress.split(",");
                String srcIp = split[0];
                String postPort = policyDTO.getPostPort();
                if (IpUtils.isIPRange(srcIp)) {
                    // 范围
                    String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
                    String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
                    int index=endIp.lastIndexOf(".");
                    String substring = endIp.substring(index+1, endIp.length());
                    String nIp = startIp+"_"+substring;
                    poolName = nIp.replace(".", "-");
                    sb.append(String.format("set security nat destination pool %s address %s to %s\n", poolName, startIp, endIp));
                    if ( StringUtils.isBlank( postPort ) || StringUtils.equalsIgnoreCase( "any",postPort ) ){
                        // 无端口就只创建IP范围的命令行
                    }else {
                        // 端口命令行
                        String[] p = postPort.split(",");
                        String[] s = p[0].split("-");
                        sb.append(String.format("set security nat destination pool %s address port %s\n", poolName, s[0] ));
                    }
                } else if (IpUtils.isIPSegment(srcIp)) {
                    // 网段
                    String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                    String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp);
                    String ip = ipFromIpSegment + "/" + maskBitFromIpSegment;
                    poolName = ip.replace(".", "-");
                    sb.append(String.format("set security nat destination pool %s address %s\n", poolName, ip));
                    if ( StringUtils.isNotBlank( postPort ) && !StringUtils.equalsIgnoreCase( "any",postPort ) ){
                        // 有端口，再取第一个端口值（填多个端口或端口范围，也只取第一个端口）
                        String[] p = postPort.split(",");
                        String[] s = p[0].split("-");
                        sb.append(String.format("set security nat destination pool %s address port %s\n", poolName, s[0] ));
                    }
                } else if (IpUtils.isIP(srcIp)) {
                    // 单ip
                    poolName = srcIp.replace(".", "-");
                    sb.append(String.format("set security nat destination pool %s address %s/32\n", poolName, srcIp));
                    if ( StringUtils.isNotBlank( postPort ) && !StringUtils.equalsIgnoreCase( "any",postPort ) ){
                        // 有端口，再取第一个端口值（填多个端口或端口范围，也只取第一个端口）
                        String[] p = postPort.split(",");
                        String[] s = p[0].split("-");
                        sb.append(String.format("set security nat destination pool %s address port %s\n", poolName, s[0] ));
                    }
                }
            }
        }
        // 2）创建策略集
        String srcZone = StringUtils.isBlank( policyDTO.getSrcZone() ) ? "any": policyDTO.getSrcZone();
        String policySetName = srcZone;
        sb.append(String.format("set security nat destination rule-set %s from zone %s\n", policySetName, srcZone));

        // 3）创建策略
            // 转换前源地址
        String srcIp = policyDTO.getSrcIp();
        if ( StringUtils.isNotBlank( srcIp ) && !StringUtils.equals( "any",srcIp ) ){
            String[] split = srcIp.split(",");
            for (String s : split) {
                if (IpUtils.isIPRange(s)) {
                    // 范围
               //     String startIp = IpUtils.getStartIpFromIpAddress(s);
                    String ips = IpUtils.convertIpRangeToSegment(s);
                    String[] ipArr = ips.split(",");
                    for (String ip : ipArr) {
                        sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s\n", policySetName, natName, ip));
                    }
              //      sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s/32\n", policySetName, natName, startIp));
                } else if (IpUtils.isIPSegment(s)){
                    // 网段
                    sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s\n", policySetName, natName, s));
                }else if (IpUtils.isIP(s)){
                    // 单ip
                    sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s/32\n", policySetName, natName, s));
                }
            }
        }
            // 转换前目的地址
        String dstIp = policyDTO.getDstIp();
        if ( StringUtils.isNotBlank( dstIp ) && !StringUtils.equals( "any",dstIp ) ){
            String[] split = dstIp.split(",");
            String split_0 = split[0];
            if (IpUtils.isIPRange(split_0)) {
                // 范围
        //        String startIp = IpUtils.getStartIpFromIpAddress(split_0);
                String ips = IpUtils.convertIpRangeToSegment(split_0);
                String[] ipArr = ips.split(",");
                for (String ip : ipArr) {
                    sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s\n", policySetName, natName, ip));
                }
        //        sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, startIp));
            } else if (IpUtils.isIPSegment(split_0)) {
                // 网段
                sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s\n", policySetName, natName, split_0));
            } else if (IpUtils.isIP(split_0)) {
                // 单ip
                sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, split_0));
            }
        }
        // 协议-协议可以填写多个，但端口只能填一个，并且只能填单端口，不支持填单端口范围；（用户在页面填写多个协议时命令行都生成，但端口只取第一个协议的单端口）
        List<ServiceDTO> serviceList = policyDTO.getServiceList();
        if ( CollectionUtils.isNotEmpty( serviceList ) ){
            for (ServiceDTO serviceDTO : serviceList) {
                // 协议
                String protocol = serviceDTO.getProtocol();
                if (  StringUtils.isNotBlank( protocol ) && !StringUtils.equalsIgnoreCase( "any", protocol ) && !StringUtils.equalsIgnoreCase( "0", protocol) ){
                    String desc = ProtocolEnum.getDescByCode(protocol);
                    sb.append(String.format("set security nat destination rule-set %s rule %s match protocol %s\n", policySetName, natName ,desc.toLowerCase()));
                }
            }
            // 端口-只取第一个协议的单端口(服务对象为多个，第一个服务没端口，依次往下寻找端口，直到找到一个端口为止)
            List<String> list = serviceList.stream().filter(t-> !StringUtils.equalsIgnoreCase("any", t.getDstPorts())).map(ServiceDTO::getDstPorts).collect(Collectors.toList());
            if ( CollectionUtils.isNotEmpty( list ) ){
                String dstPorts = list.get(0);
                String[] port = dstPorts.split(",");
                if ( port[0].contains("-") ){
                    String[] split = port[0].split("-");
                    sb.append(String.format("set security nat destination rule-set %s rule %s match destination-port %s\n", policySetName, natName, split[0] ));
                }else{
                    if (!StringUtils.equalsIgnoreCase( "any", port[0]) ){
                        sb.append(String.format("set security nat destination rule-set %s rule %s match destination-port %s\n", policySetName, natName, port[0]) );
                    }
                }
            }

        }
        // 转
        if ( StringUtils.isNotBlank( poolName ) ){
            sb.append(String.format("set security nat destination rule-set %s rule %s then destination-nat pool %s\n", policySetName, natName, poolName ));
        }


        sb.append("commit\n");
        sb.append("exit");

        return sb.toString();
    }


    // both
    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {

        StringBuilder sb = new StringBuilder();
        sb.append("configure\n");

        // 策略名称
        String natName = String.format("%s_AG_%s", policyDTO.getTheme(), IdGen.getRandomNumberString());

        // 1）创建源地址转换后的地址池
        String postSrcAddressObjectName = policyDTO.getPostSrcAddressObjectName();
        String sPoolName="";
        if (StringUtils.isNotBlank( postSrcAddressObjectName )) {
            // 可整体复用 无需再创建地址池
            sPoolName=postSrcAddressObjectName;
        }else{
            // 不可复用 取值创建
            String postSrcIp = policyDTO.getPostSrcIp();
            // 当地址any时，在Nat策略中无需输入对应的命令行
            if ( StringUtils.isNotBlank( postSrcIp ) && !StringUtils.equalsIgnoreCase( "any",postSrcIp ) ){
                // 取第一个poolName
                String[] split = postSrcIp.split(",");
                String srcIp_0 = split[0];
                if (IpUtils.isIPRange(srcIp_0)) {
                    // 范围
                    String startIp = IpUtils.getStartIpFromIpAddress(srcIp_0);
                    String endIp = IpUtils.getEndIpFromIpAddress(srcIp_0);
                    int index=endIp.lastIndexOf(".");
                    String substring = endIp.substring(index+1, endIp.length());
                    String nIp = startIp+"_"+substring;
                    sPoolName = nIp.replace(".", "-");
                } else if (IpUtils.isIPSegment(srcIp_0)) {
                    // 网段
                    String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp_0);
                    String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp_0);
                    String ip = ipFromIpSegment + "/" + maskBitFromIpSegment;
                    sPoolName = ip.replace(".", "-");
                } else if (IpUtils.isIP(srcIp_0)) {
                    // 单ip
                    sPoolName = srcIp_0.replace(".", "-");
                }
                for (String srcIp : split) {
                    if (IpUtils.isIPRange(srcIp)) {
                        // 范围
                        String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
                        String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
                        int index=endIp.lastIndexOf(".");
                        String substring = endIp.substring(index+1, endIp.length());
                        String nIp = startIp+"_"+substring;
                        //      poolName = nIp.replace(".", "-");
                        sb.append(String.format("set security nat source pool %s address %s to %s\n", sPoolName, startIp, endIp));
                    } else if (IpUtils.isIPSegment(srcIp)) {
                        // 网段
                        String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                        String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp);
                        String ip = ipFromIpSegment + "/" + maskBitFromIpSegment;
                        //       poolName = ip.replace(".", "-");
                        sb.append(String.format("set security nat source pool %s address %s\n", sPoolName, ip));
                    } else if (IpUtils.isIP(srcIp)) {
                        // 单ip
                        //      poolName = ip.replace(".", "-");
                        sb.append(String.format("set security nat source pool %s address %s/32\n", sPoolName, srcIp));
                    }
                }

            }
        }
        // 2）创建目的地址转换后的地址池与端口
        // 转换后地址
        String postDstAddressObjectName = policyDTO.getPostDstAddressObjectName();
        String dPoolName="";
        if (StringUtils.isNotBlank( postDstAddressObjectName )) {
            // 可整体复用 无需再创建地址池
            dPoolName=postDstAddressObjectName;
        }else{
            // 不可复用 取值创建
            String postDstIp = policyDTO.getPostDstIp();
            // 当地址any时，在Nat策略中无需输入对应的命令行
            if ( StringUtils.isNotBlank( postDstIp ) && !StringUtils.equalsIgnoreCase( "any",postDstIp ) ){
                String[] split = postDstIp.split(",");
                String srcIp = split[0];
                String postPort = policyDTO.getPostPort();
                if (IpUtils.isIPRange(srcIp)) {
                    // 范围
                    String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
                    String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
                    int index=endIp.lastIndexOf(".");
                    String substring = endIp.substring(index+1, endIp.length());
                    String nIp = startIp+"_"+substring;
                    dPoolName = nIp.replace(".", "-");
                    sb.append(String.format("set security nat destination pool %s address %s to %s\n", dPoolName, startIp, endIp));
                    if ( StringUtils.isBlank( postPort ) || StringUtils.equalsIgnoreCase( "any",postPort ) ){
                        // 无端口就只创建IP范围的命令行
                    }else {
                        // 端口命令行
                        String[] p = postPort.split(",");
                        String[] s = p[0].split("-");
                        sb.append(String.format("set security nat destination pool %s address port %s\n", dPoolName, s[0] ));
                    }
                } else if (IpUtils.isIPSegment(srcIp)) {
                    // 网段
                    String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                    String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(srcIp);
                    String ip = ipFromIpSegment + "/" + maskBitFromIpSegment;
                    dPoolName = ip.replace(".", "-");
                    sb.append(String.format("set security nat destination pool %s address %s\n", dPoolName, ip));
                    if ( StringUtils.isNotBlank( postPort ) && !StringUtils.equalsIgnoreCase( "any",postPort ) ){
                        // 有端口，再取第一个端口值（填多个端口或端口范围，也只取第一个端口）
                        String[] p = postPort.split(",");
                        String[] s = p[0].split("-");
                        sb.append(String.format("set security nat destination pool %s address port %s\n", dPoolName, s[0] ));
                    }
                } else if (IpUtils.isIP(srcIp)) {
                    // 单ip
                    dPoolName = srcIp.replace(".", "-");
                    sb.append(String.format("set security nat destination pool %s address %s/32\n", dPoolName, srcIp));
                    if ( StringUtils.isNotBlank( postPort ) && !StringUtils.equalsIgnoreCase( "any",postPort ) ){
                        // 有端口，再取第一个端口值（填多个端口或端口范围，也只取第一个端口）
                        String[] p = postPort.split(",");
                        String[] s = p[0].split("-");
                        sb.append(String.format("set security nat destination pool %s address port %s\n", dPoolName, s[0] ));
                    }
                }
            }
        }
        // 3）创建策略集
        String srcZone = StringUtils.isBlank( policyDTO.getSrcZone() ) ? "any": policyDTO.getSrcZone();
        String dstZone = StringUtils.isBlank( policyDTO.getDstZone() ) ? "any": policyDTO.getDstZone();
                // 源策略集
        String policySetName = srcZone + "_to_" + dstZone;
        sb.append(String.format("set security nat source rule-set %s from zone %s\n", policySetName, srcZone));
        sb.append(String.format("set security nat source rule-set %s to zone %s\n", policySetName, dstZone));
                // 目的策略集
        sb.append(String.format("set security nat destination rule-set %s from zone %s\n", policySetName, srcZone));

        // 4）创建BothNat策略
            // 转换前的源地址
        String srcIp = policyDTO.getSrcIp();
        if ( StringUtils.isNotBlank( srcIp ) && !StringUtils.equals( "any",srcIp ) ){
            String[] split = srcIp.split(",");
            for (String s : split) {
                if (IpUtils.isIPRange(s)) {
                    // 范围
             //       String startIp = IpUtils.getStartIpFromIpAddress(s);
                    String ips = IpUtils.convertIpRangeToSegment(s);
                    String[] ipArr = ips.split(",");
                    for (String ip : ipArr) {
                        sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s\n", policySetName, natName, ip));
                    }
       //             sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s/32\n", policySetName, natName, startIp));
                } else if (IpUtils.isIPSegment(s)){
                    // 网段
                    sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s\n", policySetName, natName, s));
                }else if (IpUtils.isIP(s)){
                    // 单ip
                    sb.append(String.format("set security nat source rule-set %s rule %s match source-address %s/32\n", policySetName, natName, s));
                }

            }
        }
        // 转换前目的地址
        String dstIp = policyDTO.getDstIp();
        if ( StringUtils.isNotBlank( dstIp ) && !StringUtils.equals( "any",dstIp ) ){
            String[] split = dstIp.split(",");
            for (String s : split) {
                if (IpUtils.isIPRange(s)) {
                    // 范围
               //     String startIp = IpUtils.getStartIpFromIpAddress(s);
                    String ips = IpUtils.convertIpRangeToSegment(s);
                    String[] ipArr = ips.split(",");
                    for (String ip : ipArr) {
                        sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s\n", policySetName, natName, ip));
                    }
           //         sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, startIp));
                } else if (IpUtils.isIPSegment(s)) {
                    // 网段
                    sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s\n", policySetName, natName, s));
                } else if (IpUtils.isIP(s)) {
                    // 单ip
                    sb.append(String.format("set security nat source rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, s));
                }
            }
        }
        // 服务 用户在页面填写多个协议时命令行都生成，但端口只取第一个协议的单端口或单端口范围
        List<ServiceDTO> serviceList = policyDTO.getServiceList();
        if ( CollectionUtils.isNotEmpty( serviceList ) ){
            for (ServiceDTO serviceDTO : serviceList) {
                // 协议
                String protocol = serviceDTO.getProtocol();
                if (  StringUtils.isNotBlank( protocol ) && !StringUtils.equalsIgnoreCase( "any", protocol ) && !StringUtils.equalsIgnoreCase( "0", protocol)){
                    String desc = ProtocolEnum.getDescByCode(protocol);
                    sb.append(String.format("set security nat source rule-set %s rule %s match protocol %s\n", policySetName, natName, desc.toLowerCase()));
                }
            }
            // 端口-只取第一个协议的单端口或单端口范围(服务对象为多个，第一个服务没端口，依次往下寻找端口，直到找到一个端口为止)
            List<String> list = serviceList.stream().filter(t-> !StringUtils.equalsIgnoreCase("any", t.getDstPorts())).map(ServiceDTO::getDstPorts).collect(Collectors.toList());
            if ( CollectionUtils.isNotEmpty( list ) ){
                String dstPorts = list.get(0);
                String[] port = dstPorts.split(",");
                if ( port[0].contains("-") ){
                    String[] split = port[0].split("-");
                    sb.append(String.format("set security nat source rule-set %s rule %s match destination-port %s to %s\n", policySetName, natName, split[0], split[1]));
                }else{
                    if (!StringUtils.equalsIgnoreCase( "any", port[0]) ){
                        sb.append(String.format("set security nat source rule-set %s rule %s match destination-port %s\n", policySetName, natName, port[0]) );
                    }
                }
            }
        }
        // 转
        if ( StringUtils.isNotBlank( sPoolName ) ){
            sb.append(String.format("set security nat source rule-set %s rule %s then source-nat pool %s\n", policySetName, natName, sPoolName ));
        }

        // 源地址
        if ( StringUtils.isNotBlank( srcIp ) && !StringUtils.equals( "any",srcIp ) ){
            String[] split = srcIp.split(",");
            for (String s : split) {
                if (IpUtils.isIPRange(s)) {
                    // 范围
                //    String startIp = IpUtils.getStartIpFromIpAddress(s);
                    String ips = IpUtils.convertIpRangeToSegment(s);
                    String[] ipArr = ips.split(",");
                    for (String ip : ipArr) {
                        sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s\n", policySetName, natName, ip));
                    }
              //      sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s/32\n", policySetName, natName, startIp));
                } else if (IpUtils.isIPSegment(s)){
                    // 网段
                    sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s\n", policySetName, natName, s));
                }else if (IpUtils.isIP(s)){
                    // 单ip
                    sb.append(String.format("set security nat destination rule-set %s rule %s match source-address %s/32\n", policySetName, natName, s));
                }
            }
        }
        // 转换前目的地址
        if ( StringUtils.isNotBlank( dstIp ) && !StringUtils.equals( "any",dstIp ) ){
            String[] split = dstIp.split(",");
            String split_0 = split[0];
            if (IpUtils.isIPRange(split_0)) {
                // 范围
           //     String startIp = IpUtils.getStartIpFromIpAddress(split_0);
                String ips = IpUtils.convertIpRangeToSegment(split_0);
                String[] ipArr = ips.split(",");
                for (String ip : ipArr) {
                    sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s\n", policySetName, natName, ip));
                }
         //       sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, startIp));
            } else if (IpUtils.isIPSegment(split_0)) {
                // 网段
                sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s\n", policySetName, natName, split_0));
            } else if (IpUtils.isIP(split_0)) {
                // 单ip
                sb.append(String.format("set security nat destination rule-set %s rule %s match destination-address %s/32\n", policySetName, natName, split_0));
            }
        }
        // 协议-协议可以填写多个，但端口只能填一个，并且只能填单端口，不支持填单端口范围；（用户在页面填写多个协议时命令行都生成，但端口只取第一个协议的单端口）
        if ( CollectionUtils.isNotEmpty( serviceList ) ){
            for (ServiceDTO serviceDTO : serviceList) {
                // 协议
                String protocol = serviceDTO.getProtocol();
                if (  StringUtils.isNotBlank( protocol ) && !StringUtils.equalsIgnoreCase( "any", protocol ) && !StringUtils.equalsIgnoreCase( "0", protocol) ){
                    String desc = ProtocolEnum.getDescByCode(protocol);
                    sb.append(String.format("set security nat destination rule-set %s rule %s match protocol %s\n", policySetName, natName ,desc.toLowerCase()));
                }
            }
            // 端口-只取第一个协议的单端口(服务对象为多个，第一个服务没端口，依次往下寻找端口，直到找到一个端口为止)
            List<String> list = serviceList.stream().filter(t-> !StringUtils.equalsIgnoreCase("any", t.getDstPorts())).map(ServiceDTO::getDstPorts).collect(Collectors.toList());
            if ( CollectionUtils.isNotEmpty( list ) ){
                String dstPorts = list.get(0);
                String[] port = dstPorts.split(",");
                if ( port[0].contains("-") ){
                    String[] split = port[0].split("-");
                    sb.append(String.format("set security nat destination rule-set %s rule %s match destination-port %s\n", policySetName, natName, split[0] ));
                }else{
                    if (!StringUtils.equalsIgnoreCase( "any", port[0]) ){
                        sb.append(String.format("set security nat destination rule-set %s rule %s match destination-port %s\n", policySetName, natName, port[0]) );
                    }
                }
            }
        }
        // 转
        if ( StringUtils.isNotBlank( dPoolName ) ){
            sb.append(String.format("set security nat destination rule-set %s rule %s then destination-nat pool %s\n", policySetName, natName, dPoolName ));
        }

        sb.append("commit\n");
        sb.append("exit");

        return sb.toString();
    }



    public static void main(String[] args) {
        JuniperNat juniperNat = new JuniperNat();

        String a="{" +
                "    \"existDstAddressList\": []," +
                "    \"description\": \"\"," +
                "    \"createObjFlag\": false," +
                "    \"existSrcAddressName\": []," +
                "    \"restSrcAddressList\": [" +
                "        \"1.1.1.1-1.1.1.2\"" +
                "    ]," +
                "    \"theme\": \"s001\"," +
                "    \"dstIpSystem\": \"\"," +
                "    \"existPostSrcAddressList\": []," +
                "    \"vsys\": false," +
                "    \"restDstAddressList\": [" +
                "        \"2.2.2.2\"" +
                "    ]," +
                "    \"restPostSrcAddressList\": [" +
                "        \"3.3.3.3\"" +
                "    ]," +
                "    \"srcIp\": \"1.1.1.1-1.1.1.2\"," +
                "    \"srcIpSystem\": \"\"," +
                "    \"srcZone\": \"trust\"," +
                "    \"srcItf\": \"reth1.0\"," +
                "    \"dstZone\": \"untrust\"," +
                "    \"vsysName\": \"\"," +
                "    \"existPostSrcAddressName\": []," +
                "    \"ciscoEnable\": true," +
                "    \"existServiceNameList\": []," +
                "    \"serviceList\": [" +
                "        {" +
                "            \"dstPorts\": \"1-2\"," +
                "            \"protocol\": \"6\"," +
                "            \"srcPorts\": \"any\"" +
                "        }" +
                "    ]," +
                "    \"dstItf\": \"reth0.0\"," +
                "    \"postIpAddress\": \"3.3.3.3\"," +
                "    \"restServiceList\": [" +
                "        {" +
                "            \"dstPorts\": \"1-2\"," +
                "            \"protocol\": \"6\"," +
                "            \"srcPorts\": \"any\"" +
                "        }" +
                "    ]," +
                "    \"dstIp\": \"2.2.2.2\"," +
                "    \"postSrcIpSystem\": \"\"," +
                "    \"existDstAddressName\": []," +
                "    \"existSrcAddressList\": []" +
                "}";
        SNatPolicyDTO s = JSON.parseObject(a,SNatPolicyDTO.class);
        juniperNat.generateSNatCommandLine( s );


        String b="{" +
                "\"existDstAddressList\": []," +
                "\"description\": \"\"," +
                "\"createObjFlag\": false," +
                "\"restSrcAddressList\": [\"1.1.1.1-1.1.1.2\"]," +
                "\"theme\": \"d001\"," +
                "\"postPort\": \"3\"," +
                "\"existPostSrcAddressList\": []," +
                "\"restPostServiceList\": [{" +
                "\"dstPorts\": \"3\"," +
                "\"protocol\": \"6\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"existPostServiceNameList\": []," +
                "\"vsys\": false," +
                "\"restDstAddressList\": [\"2.2.2.2-2.2.2.3\"]," +
                "\"restPostSrcAddressList\": []," +
                "\"srcIp\": \"1.1.1.1-1.1.1.2\"," +
                "\"srcZone\": \"trust\"," +
                "\"srcItf\": \"reth1.0\"," +
                "\"dstZone\": \"\"," +
                "\"vsysName\": \"\"," +
                "\"existPostDstAddressList\": []," +
                "\"restPostDstAddressList\": [\"4.4.4.4\"]," +
                "\"existServiceNameList\": []," +
                "\"serviceList\": [{" +
                "\"dstPorts\": \"1-2\"," +
                "\"protocol\": \"6\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"dstItf\": \"\"," +
                "\"postIpAddress\": \"4.4.4.4\"," +
                "\"restServiceList\": [{" +
                "\"dstPorts\": \"1-2\"," +
                "\"protocol\": \"6\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"dstIp\": \"2.2.2.2-2.2.2.3\"," +
                "\"existSrcAddressList\": []" +
                "}";

        DNatPolicyDTO d = JSON.parseObject(b,DNatPolicyDTO.class);

        juniperNat.generateDNatCommandLine( d );

        String n="{" +
                "\"description\": \"\"," +
                "\"createObjFlag\": false," +
                "\"postServiceList\": [{" +
                "\"dstPorts\": \"3-8\"," +
                "\"protocol\": \"17\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"specialExistObject\": {" +
                "\"existPostServiceNameList\": []," +
                "\"restDstAddressList\": []," +
                "\"restPostSrcAddressList\": []," +
                "\"existDstAddressList\": []," +
                "\"existPostDstAddressList\": []," +
                "\"restPostDstAddressList\": []," +
                "\"restSrcAddressList\": []," +
                "\"existServiceNameList\": []," +
                "\"restServiceList\": []," +
                "\"existPostSrcAddressList\": []," +
                "\"existSrcAddressList\": []," +
                "\"restPostServiceList\": []" +
                "}," +
                "\"postSrcIp\": \"3.3.3.3-3.3.3.4\"," +
                "\"dynamic\": false," +
                "\"theme\": \"b001\"," +
                "\"postPort\": \"3-8\"," +
                "\"existPostSrcAddressList\": []," +
                "\"restPostServiceList\": [{" +
                "\"dstPorts\": \"3-8\"," +
                "\"protocol\": \"17\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"existPostServiceNameList\": []," +
                "\"rollback\": false," +
                "\"vsys\": false," +
                "\"restPostSrcAddressList\": [\"3.3.3.3-3.3.3.4\"]," +
                "\"srcIp\": \"1.1.1.1-1.1.1.2\"," +
                "\"srcZone\": \"trust\"," +
                "\"srcItf\": \"reth1.0\"," +
                "\"dstZone\": \"untrust\"," +
                "\"vsysName\": \"\"," +
                "\"postDstIp\": \"4.4.4.4-4.4.4.5\"," +
                "\"existPostDstAddressList\": []," +
                "\"restPostDstAddressList\": [\"4.4.4.4-4.4.4.5\"]," +
                "\"existServiceNameList\": []," +
                "\"serviceList\": [{" +
                "\"dstPorts\": \"1-2\"," +
                "\"protocol\": \"17\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"dstItf\": \"reth0.0\"," +
                "\"restServiceList\": [{" +
                "\"dstPorts\": \"1-2\"," +
                "\"protocol\": \"17\"," +
                "\"srcPorts\": \"any\"" +
                "}]," +
                "\"dstIp\": \"2.2.2.2-2.2.2.3\"" +
                "}";
        NatPolicyDTO c = JSON.parseObject(n,NatPolicyDTO.class);
        juniperNat.generateBothNatCommandLine( c );

    }



}
