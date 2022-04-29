package com.abtnetworks.totems.vender.fortinet.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class SecurityFortinetImpl extends OverAllGeneratorAbstractBean {

    public static final String IP_ADDRESS_OBJECT_NAME_LIST = "ipAddressObjectNameList";

    public static final String DELETE_STR = "delete";

    public static final String EDIT_STR = "edit %s";

    public static final String SET_SERVICE_STR = "set service ";

    public static final String POLICY_STR_VALUE_ANY = "any";

    public static final String POLICY_STR_VALUE_ICMP = "ICMP";

    public String generatePreSecurityCommandLine(){
        StringBuilder preIpAddressObjectCommandLine = new StringBuilder("");
        preIpAddressObjectCommandLine.append("config firewall policy").append(StringUtils.LF);
        return preIpAddressObjectCommandLine.toString();
    }

    public String generatePreSecurityCommandLine6(){
        StringBuilder preIpAddressObjectCommandLine = new StringBuilder("");
        preIpAddressObjectCommandLine.append("config firewall policy6").append(StringUtils.LF);
        return preIpAddressObjectCommandLine.toString();
    }

    public String generatePreIpAddressObjectCommandLine(){
        StringBuilder preIpAddressObjectCommandLine = new StringBuilder("");
        preIpAddressObjectCommandLine.append("config firewall address").append(StringUtils.LF);
        return preIpAddressObjectCommandLine.toString();
    }
    public String generatePreIpAddressObjectCommandLine6(){
        StringBuilder preIpAddressObjectCommandLine = new StringBuilder("");
        preIpAddressObjectCommandLine.append("config firewall address6").append(StringUtils.LF);
        return preIpAddressObjectCommandLine.toString();
    }

    public String generatePreIpPoolObjectCommandLine(){
        StringBuilder preIpPoolObjectCommandLine = new StringBuilder("");
        preIpPoolObjectCommandLine.append("config firewall ippool").append(StringUtils.LF);
        return preIpPoolObjectCommandLine.toString();
    }

    public String generatePreIpVipObjectCommandLine(){
        StringBuilder preIpVipObjectCommandLine = new StringBuilder("");
        preIpVipObjectCommandLine.append("config firewall vip").append(StringUtils.LF);
        return preIpVipObjectCommandLine.toString();
    }

    public String generatePreIpAddressObjectGroupCommandLine(){
        StringBuilder preIpAddressObjectGroupCommandLine = new StringBuilder("");
        preIpAddressObjectGroupCommandLine.append("config firewall addrgrp").append(StringUtils.LF);
        return preIpAddressObjectGroupCommandLine.toString();
    }

    public String generatePreIpAddressObjectGroupCommandLine6(){
        StringBuilder preIpAddressObjectGroupCommandLine = new StringBuilder("");
        preIpAddressObjectGroupCommandLine.append("config firewall addrgrp6").append(StringUtils.LF);
        return preIpAddressObjectGroupCommandLine.toString();
    }


    public String generatePreServiceObjectCommandLine(){
        StringBuilder preServiceObjectCommandLine = new StringBuilder("");
        preServiceObjectCommandLine.append("config firewall service custom").append(StringUtils.LF);
        return preServiceObjectCommandLine.toString();
    }


    private String generatePreAbsoluteTimeObjectCommandLine() {
        StringBuilder preAbsoluteTimeObjectCommandLine = new StringBuilder("");
        preAbsoluteTimeObjectCommandLine.append("config firewall schedule onetime ").append("\n");
        return preAbsoluteTimeObjectCommandLine.toString();
    }

    private String generatePrePeriodicTimeObjectCommandLine() {
        StringBuilder prePeriodicTimeObjectCommandLine = new StringBuilder("");
        prePeriodicTimeObjectCommandLine.append("config firewall schedule recurring ").append("\n");
        return prePeriodicTimeObjectCommandLine.toString();
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipv4ArrayCommandLine = new StringBuilder("");
        //ipv4ArrayCommandLine.append(generatePreIpAddressObjectCommandLine(map, args)).append(StringUtils.LF);
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        ipv4ArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String ip:singleIpArray) {
                ipv4ArrayCommandLine.append(String.format("set subnet %s/32",ip)).append(StringUtils.LF);
            }
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder hostCommandLine = new StringBuilder("");
        //ipv4ArrayCommandLine.append(generatePreIpAddressObjectCommandLine(map, args)).append(StringUtils.LF);
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        hostCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(hosts)){
            for (String host:hosts) {
                hostCommandLine.append("set type fqdn\n");
                hostCommandLine.append(String.format("set fqdn %s",host)).append(StringUtils.LF);
            }
        }
        return hostCommandLine.toString();
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipvRangeArrayCommandLine = new StringBuilder("");
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        ipvRangeArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(rangIpArray)&&statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (IpAddressRangeDTO dto:rangIpArray) {
                ipvRangeArrayCommandLine.append("set type iprange").append(StringUtils.LF);
                ipvRangeArrayCommandLine.append(String.format("set start-ip %s",dto.getStart())).append(StringUtils.LF);
                ipvRangeArrayCommandLine.append(String.format("set end-ip %s",dto.getEnd())).append(StringUtils.LF);
            }
        }
        return ipvRangeArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        subnetIntArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(subnetIpArray)&&statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (IpAddressSubnetIntDTO dto:subnetIpArray) {
                subnetIntArrayCommandLine.append(String.format("set subnet %s/%s",dto.getIp(),dto.getMask())).append(StringUtils.LF);
            }
        }
        return subnetIntArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        subnetStrArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(subnetIpArray)&&statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (IpAddressSubnetStrDTO dto:subnetIpArray) {
                int maskBit;
                String wildcardMask;
                if(MaskTypeEnum.mask.getType().equalsIgnoreCase(dto.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBit(dto.getMask());
                    wildcardMask = TotemsIpUtils.getWildcardMaskMap(maskBit);
                } else {
//                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(dto.getMask());
                    wildcardMask = dto.getMask();
                }
                subnetStrArrayCommandLine.append(String.format("set subnet %s/%s",dto.getIp(), wildcardMask)).append(StringUtils.LF);;
            }
        }
        return subnetStrArrayCommandLine.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipv4ArrayCommandLine = new StringBuilder("");
        //ipv4ArrayCommandLine.append(generatePreIpAddressObjectCommandLine(map, args)).append(StringUtils.LF);
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        ipv4ArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String ip:singleIpArray) {
                ipv4ArrayCommandLine.append(String.format("set ip6 %s/128",ip)).append(StringUtils.LF);
            }
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipvRangeArrayCommandLine = new StringBuilder("");
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        ipvRangeArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(rangIpArray)&&statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (IpAddressRangeDTO dto:rangIpArray) {
                ipvRangeArrayCommandLine.append("set type iprange").append(StringUtils.LF);
                ipvRangeArrayCommandLine.append(String.format("set start-ip %s",dto.getStart())).append(StringUtils.LF);
                ipvRangeArrayCommandLine.append(String.format("set end-ip %s",dto.getEnd())).append(StringUtils.LF);
            }
        }
        return ipvRangeArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        subnetIntArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(subnetIpArray)&&statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (IpAddressSubnetIntDTO dto:subnetIpArray) {
                subnetIntArrayCommandLine.append(String.format("set ip6 %s/%s",dto.getIp(),dto.getMask())).append(StringUtils.LF);
            }
        }
        return subnetIntArrayCommandLine.toString();
    }

    public String generateSubnetStrIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = DELETE_STR;
        }
        String name = (String) map.get("name");
        subnetStrArrayCommandLine.append(deletePrefix).append(String.format(" \"%s\" ",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(subnetIpArray)&&statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (IpAddressSubnetStrDTO dto:subnetIpArray) {
                int maskBit;
                String wildcardMask;
                if(MaskTypeEnum.mask.getType().equalsIgnoreCase(dto.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBit(dto.getMask());
                    wildcardMask = TotemsIpUtils.getWildcardMaskMap(maskBit);
                } else {
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(dto.getMask());
                    wildcardMask = dto.getMask();
                }
                subnetStrArrayCommandLine.append(String.format("set ip6 %s/%s",dto.getIp(), wildcardMask)).append(StringUtils.LF);;
            }
        }
        return subnetStrArrayCommandLine.toString();
    }



    /***
     *
     * @param statusTypeEnum 状态类型
     * @param ipTypeEnum IP枚举类型
     * @param name 地址对象名称
     * @param id 地址对象id
     * @param singleIpArray 单个ip
     * @param rangIpArray 范围ip
     * @param subnetIntIpArray 子网ip 掩码int
     * @param subnetStrIpArray 子网ip 掩码str
     * @param interfaceArray 接口集合
     * @param fqdnArray 域名集合
     * @param objectNameRefArray 引用对象名称集合
     * @param description 备注
     * @param attachStr 附加字符串
     * @param delStr 删除，失效标记
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder commandLine=new StringBuilder("");
        // ip范围地址对象
        int i=0;
        boolean nameArray = name.contains(",");
        String tmpName = null;
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO  ipAddressRangeDTO: rangIpArray) {
                if(nameArray){
                    tmpName = name.split(",")[i];
                }else{
                    tmpName = name;
                }
                if(map==null) {
                    map=new HashMap<>();
                }
                map.put("name", tmpName);

                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(generatePreIpAddressObjectCommandLine6());
                    commandLine.append(this.generateRangeIpV6CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }else if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                    commandLine.append(generatePreIpAddressObjectCommandLine());
                    commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }

                commandLine.append("next").append(StringUtils.LF);
                commandLine.append("end").append(StringUtils.LF);
                commandLine.append(StringUtils.LF);
                i++;
            }
        }

        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String ip: singleIpArray) {
                if(nameArray){
                    tmpName = name.split(",")[i];
                }else{
                    tmpName = name;
                }
                if(map==null) {
                    map=new HashMap<>();
                }
                map.put("name", tmpName);
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(generatePreIpAddressObjectCommandLine6());
                    commandLine.append(this.generateSingleIpV6CommandLine(statusTypeEnum,new String[]{ip}, map, null));
                }else if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                    commandLine.append(generatePreIpAddressObjectCommandLine());
                    commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum,new String[]{ip}, map, null));
                }
                commandLine.append("next").append(StringUtils.LF);
                commandLine.append("end").append(StringUtils.LF);
                commandLine.append(StringUtils.LF);
                i++;
            }
        }


        if(ArrayUtils.isNotEmpty(subnetIntIpArray)||ArrayUtils.isNotEmpty(subnetStrIpArray)){
            if(ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                    if(nameArray){
                        tmpName = name.split(",")[i];
                    }else{
                        tmpName = name;
                    }
                    if(map==null) {
                        map=new HashMap<>();
                    }
                    map.put("name", tmpName);
                    if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                        commandLine.append(generatePreIpAddressObjectCommandLine6());
                        commandLine.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,null));
                    }else if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                        commandLine.append(generatePreIpAddressObjectCommandLine());
                        commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,null));
                    }
                    commandLine.append("next").append(StringUtils.LF);
                    commandLine.append("end").append(StringUtils.LF);
                    commandLine.append(StringUtils.LF);
                    i++;
                }
            }
            if(ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO: subnetStrIpArray) {
                    if(nameArray){
                        tmpName = name.split(",")[i];
                    }else{
                        tmpName = name;
                    }
                    if(map==null) {
                        map=new HashMap<>();
                    }
                    map.put("name", tmpName);
                    if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                        commandLine.append(generatePreIpAddressObjectCommandLine6());
                        commandLine.append(this.generateSubnetStrIpV6CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,map,null));
                    }else if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                        commandLine.append(generatePreIpAddressObjectCommandLine());
                        commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,map,null));
                    }
                    commandLine.append("next").append(StringUtils.LF);
                    commandLine.append("end").append(StringUtils.LF);
                    commandLine.append(StringUtils.LF);
                    i++;
                }
            }
        }

        if(ArrayUtils.isNotEmpty(fqdnArray)){
            for (String host: fqdnArray) {
                if(nameArray){
                    tmpName = name.split(",")[i];
                }else{
                    tmpName = name;
                }
                if(map==null) {
                    map=new HashMap<>();
                }
                map.put("name", tmpName);
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(generatePreIpAddressObjectCommandLine6());
                    commandLine.append(this.generateHostCommandLine(statusTypeEnum,new String[]{host},map,null));
                }else if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                    commandLine.append(generatePreIpAddressObjectCommandLine());
                    commandLine.append(this.generateHostCommandLine(statusTypeEnum,new String[]{host},map,null));
                }
                commandLine.append("next").append(StringUtils.LF);
                commandLine.append("end").append(StringUtils.LF);
                commandLine.append(StringUtils.LF);
                i++;
            }
        }

        return commandLine.toString();
    }


    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {
        if (!ArrayUtils.isNotEmpty(objectNameRefArray)) {
            return "";
        }
        StringBuilder commandLine=new StringBuilder("");
        if(statusTypeEnum != null && StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                commandLine.append(generatePreIpAddressObjectGroupCommandLine6());
            }else if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                commandLine.append(generatePreIpAddressObjectGroupCommandLine());
            }

            commandLine.append(String.format("edit \"%s\"", name)).append(StringUtils.LF);

            if (ArrayUtils.isNotEmpty(objectNameRefArray)) {
                commandLine.append("set member ");
                for (String members : objectNameRefArray) {
                        commandLine.append(String.format("\"%s\" ",members));
                }
                commandLine.append(StringUtils.LF);
            }
            commandLine.append("next").append(StringUtils.LF);
            commandLine.append("end").append(StringUtils.LF);
            commandLine.append(StringUtils.LF);
        }
        return commandLine.toString();
    }

    @Override
    public String createIpAddressObjectNameByParamDTO(String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] hosts, String[] objectNameRefArray, Map<String, Object> map, String[] args) {
        List<String> addressNames = new ArrayList<>();
        if(map==null){
            map = new HashMap<>();
        }
        // ip范围地址对象
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            IpAddressRangeDTO[] ipAddressDto = new IpAddressRangeDTO[1];
            for (IpAddressRangeDTO  ipAddressRangeDTO: rangIpArray) {
                RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                if(ipAddressRangeDTO.getStart().contains(":")){
                    ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                }
                map.put("ipType",ruleIPTypeEnum);
                ipAddressDto[0] = ipAddressRangeDTO;
                String singleIpObjectName=createIpAddressObjectNameByRangIpArray(ipAddressDto,map,args);
                String objectName= null;
                try {
                    objectName = generateIpAddressObjectName(null,singleIpObjectName,null,null);
                } catch (Exception e) {
                    logger.error("SecurityFortinetImpl.createIpAddressObjectNameByParamDTO() 1",e);
                }
                addressNames.add(objectName);
            }
        }

        if(ArrayUtils.isNotEmpty(singleIpArray)){
            String[] ips = new String[1];
            for (String ip: singleIpArray) {
                RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                if(ip.contains(":")){
                    ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                }
                map.put("ipType",ruleIPTypeEnum);
                ips[0] = ip;
                String singleIpObjectName=createIpAddressObjectNameBySingleIpArray(ips, map, args);;
                String objectName= null;
                try {
                    objectName = generateIpAddressObjectName(null,singleIpObjectName,null,null);
                } catch (Exception e) {
                    logger.error("SecurityFortinetImpl.createIpAddressObjectNameByParamDTO() 2",e);
                }
                addressNames.add(objectName);
            }
        }


        if(ArrayUtils.isNotEmpty(subnetIntIpArray)||ArrayUtils.isNotEmpty(subnetStrIpArray)){
            if(ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                IpAddressSubnetIntDTO[] subnetIntDTOS = new IpAddressSubnetIntDTO[1];
                for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                    RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                    if(ipAddressSubnetIntDTO.getIp().contains(":")){
                        ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                    }
                    map.put("ipType",ruleIPTypeEnum);
                    subnetIntDTOS[0] = ipAddressSubnetIntDTO;
                    String singleIpObjectName = createIpAddressObjectNameByIpSubArray(subnetIntDTOS, null, map, args);
                    String objectName= null;
                    try {
                        objectName = generateIpAddressObjectName(null,singleIpObjectName,null,null);
                    } catch (Exception e) {
                        logger.error("SecurityFortinetImpl.createIpAddressObjectNameByParamDTO() 3",e);
                    }
                    addressNames.add(objectName);
                }
            }
            if(ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                IpAddressSubnetStrDTO[] ipAddressSubnetStrDTOS = new IpAddressSubnetStrDTO[1];
                for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO: subnetStrIpArray) {
                    RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
                    if(ipAddressSubnetStrDTO.getIp().contains(":")){
                        ruleIPTypeEnum = RuleIPTypeEnum.IP6;
                    }
                    map.put("ipType",ruleIPTypeEnum);
                    ipAddressSubnetStrDTOS[0] = ipAddressSubnetStrDTO;
                    String singleIpObjectName = createIpAddressObjectNameByIpSubArray(null,ipAddressSubnetStrDTOS,map,args);
                    String objectName= null;
                    try {
                        objectName = generateIpAddressObjectName(null,singleIpObjectName,null,null);
                    } catch (Exception e) {
                        logger.error("SecurityFortinetImpl.createIpAddressObjectNameByParamDTO() 4",e);
                    }
                    addressNames.add(objectName);
                }
            }
        }
        if (ArrayUtils.isNotEmpty(hosts)){
            String[] ips = new String[1];
            for (String host: hosts) {
                String hostName=createIpAddressObjectNameByHost(host, map, args);;
                String objectName= null;
                try {
                    objectName = generateIpAddressObjectName(null,hostName,null,null);
                } catch (Exception e) {
                    logger.error("SecurityFortinetImpl.createIpAddressObjectNameByParamDTO() 2",e);
                }
                addressNames.add(objectName);
            }
        }
        return StringUtils.join(addressNames.iterator(),",");
    }


    @Override
    public String createServiceObjectGroupName(List<ServiceParamDTO> serviceParamList, String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray, Map<String, Object> map, String[] args) {
        if(CollectionUtils.isEmpty(serviceParamList)){
            if(ArrayUtils.getLength(serviceObjectNameRefArray) + ArrayUtils.getLength(serviceObjectGroupNameRefArray) == 1){
                if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
                    return "sg_"+serviceObjectNameRefArray[0];
                } else {
                    return "sg_"+serviceObjectGroupNameRefArray[0];
                }
            }
        }
        StringBuffer serviceGroupName = new StringBuffer("sg_");
        int num = 0;
        if(serviceParamList != null){
            num += serviceParamList.hashCode();
        }
        if(serviceObjectNameRefArray != null){
            num += Arrays.hashCode(serviceObjectNameRefArray);
        }
        if(serviceObjectGroupNameRefArray != null){
            num += Arrays.hashCode(serviceObjectGroupNameRefArray);
        }
        return "sg_"+Math.abs(num);
    }

    @Override
    public String createServiceObjectName(List<ServiceParamDTO> serviceParamList, Map<String, Object> map, String[] args) {
        if(CollectionUtils.isEmpty(serviceParamList)){
            return StringUtils.EMPTY;
        }
        List<String> serviceObjectNames = new ArrayList<>();
        for (ServiceParamDTO serviceParamDTO : serviceParamList) {
            String serviceObjectName = createServiceObjectName(serviceParamDTO, map, args);
            serviceObjectNames.add(serviceObjectName);
        }
        return StringUtils.join(serviceObjectNames.iterator(),",");
    }

    /**
     * 创建服务对象
     * @param statusTypeEnum  状态类型
     * @param name 服务对象名称
     * @param id 服务对象id
     * @param attachStr 附加字符串
     * @param serviceParamDTOList
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateServiceObjectCommandLine(StatusTypeEnum statusTypeEnum,
                                                   String name, String id, String attachStr,
                                                   List<ServiceParamDTO> serviceParamDTOList,
                                                   String description,
                                                   Map<String, Object> map, String[] args) throws Exception {
        StringBuilder serviceObjectCl = new StringBuilder();
        if(CollectionUtils.isEmpty(serviceParamDTOList)){
            return StringUtils.EMPTY;
        }

        String deletePrefix ="edit";
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            serviceObjectCl.append(generatePreServiceObjectCommandLine());
            deletePrefix = DELETE_STR;
            serviceObjectCl.append(deletePrefix).append(String.format(" %s ",name)).append(StringUtils.LF);;
            return  serviceObjectCl.toString();
        }

        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

            int i = 0;
            boolean nameArray = name.contains(",");
            String serviceObjectGroupName = "";
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                if(nameArray){
                    serviceObjectGroupName = name.split(",")[i];
                }else{
                    serviceObjectGroupName = name;
                }
                serviceObjectCl.append(generatePreServiceObjectCommandLine());
                serviceObjectCl.append(deletePrefix).append(String.format(" \"%s\" ",serviceObjectGroupName)).append(StringUtils.LF);;

                if (ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    serviceObjectCl.append(this.generateTCPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                } else if (ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    serviceObjectCl.append(this.generateUDPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                } else if (ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    serviceObjectCl.append(this.generateICMPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                }
                serviceObjectCl.append("next").append(StringUtils.LF);
                serviceObjectCl.append("end").append(StringUtils.LF);
                serviceObjectCl.append(StringUtils.LF);
                i++;
            }
        }
        return serviceObjectCl.toString();
    }




    /***
     *
     * @param statusTypeEnum 状态类型
     * @param name 服务组名称
     * @param id 服务组id
     * @param attachStr 附加Str字符串
     * @param serviceParamDTOList 服务DTO
     * @param description 备注
     * @param serviceObjectNameRefArray 引用服务对象名称
     * @param serviceObjectGroupNameRefArray 引用服务组对象名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateServiceObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, String id, String attachStr, List<ServiceParamDTO> serviceParamDTOList, String description, String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder commandLine=new StringBuilder();
        commandLine.append("config firewall service group").append(StringUtils.LF);
        if(StringUtils.isNotBlank(name)){
        commandLine.append(String.format(EDIT_STR,name)).append(StringUtils.LF);
        }
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            String serviceNames = String.join(" ", serviceObjectNameRefArray);
            commandLine.append(String.format("set member %s ",serviceNames)).append(StringUtils.LF);
        }
        commandLine.append("next").append(StringUtils.LF);
        commandLine.append("end").append(StringUtils.LF);
        commandLine.append(StringUtils.LF);
        return commandLine.toString();
    }

    /**
     * 生成时间对象名称
     * @param name
     * @return
     */
    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return String.format("\"%s\"",name);
    }

    /**
     * 生成绝对计划时间对象命令行 HILLSTONE_TIME_FORMAT = "MM/dd/yyyy HH:mm"
     * @param name 时间标记字符串
     * @param attachStr 附加字符串
     * @param absoluteTimeParamDTO 绝对计划
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String, Object> map, String[] args) throws Exception {
        if(ObjectUtils.isEmpty(absoluteTimeParamDTO)){
            return StringUtils.EMPTY;
        }
        StringBuilder commandLine=new StringBuilder("");
        String startTime = absoluteTimeParamDTO.getStartTime();
        String startDate = absoluteTimeParamDTO.getStartDate();
        String endTime = absoluteTimeParamDTO.getEndTime();
        String endDate = absoluteTimeParamDTO.getEndDate();
        if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(startDate)&&StringUtils.isNotBlank(endTime)&&StringUtils.isNotBlank(endDate)){
            if(StringUtils.isEmpty(name)){
                name=this.createTimeObjectNameByAbsoluteParamDTO(absoluteTimeParamDTO,map);
            }
            name= generateTimeObjectName(name, map,null);
            if(StringUtils.isNotBlank(name)){
                commandLine.append("config firewall schedule onetime").append(StringUtils.LF);
                commandLine.append(String.format(EDIT_STR,name)).append(StringUtils.LF);
            }
            startTime=startTime.substring(0,5);
            endTime=endTime.substring(0,5);
            startDate=startDate.replaceAll("-","/");
            endDate=endDate.replaceAll("-","/");
            commandLine.append(String.format("set start %s %s",startTime,startDate)).append(StringUtils.LF);
            commandLine.append(String.format("set end %s %s",endTime,endDate)).append(StringUtils.LF);
            commandLine.append("next").append(StringUtils.LF);;
            commandLine.append("end").append(StringUtils.LF);;
        }
        return commandLine.toString();
    }


    /***
     * 
     * @param statusTypeEnum 状态类型
     * @param groupName 策略集
     * @param name 策略名称
     * @param id 策略id
     * @param action 动作
     * @param description 备注说明
     * @param logFlag 开启日志
     * @param ageingTime 老化时间
     * @param refVirusLibrary 引用病毒库
     * @param moveSeatEnum 移动位置
     * @param swapRuleNameId 交换位置的规则名或id
     * @param srcIpDto
     * @param dstIpDto
     * @param serviceParam
     * @param absoluteTimeParamDTO 绝对时间对象
     * @param periodicTimeParamDTO 周期时间对象
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用服务对象
     * @param refServiceObjectGroup 引用服务组对象
     * @param refTimeObject 引用时间对象
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                    String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                    String swapRuleNameId, IpAddressParamDTO srcIpDto, IpAddressParamDTO dstIpDto, ServiceParamDTO[] serviceParam,
                                                    AbsoluteTimeParamDTO absoluteTimeParamDTO,PeriodicTimeParamDTO periodicTimeParamDTO,
                                                    ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                    String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                    String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                    String[] refServiceObject, String[] refServiceObjectGroup,
                                                    String[] refTimeObject,
                                                    Map<String, Object> map, String[] args) throws Exception {
        StringBuilder commandLine=new StringBuilder();
        String status = "edit";
        String lastLine="next";
        String end="end";
        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())){
            status = DELETE_STR;
            if(ObjectUtils.isNotEmpty(id)) {
                commandLine.append(generatePreServiceObjectCommandLine()).append(StringUtils.LF);
                commandLine.append(status).append(String.format(" %s ", id)).append(StringUtils.LF);
                commandLine.append(lastLine).append(StringUtils.LF);
                commandLine.append(end).append(StringUtils.LF);
                return commandLine.toString();
            }
        }

        // 处理时间对象
        String newTimeObjectName = null;
        String newTimeCommandLine = null;
        if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            newTimeObjectName =createTimeObjectNameByAbsoluteParamDTO(absoluteTimeParamDTO,map);
            log.info("生成时间对象名称:{}",newTimeObjectName);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName,null,absoluteTimeParamDTO,map,null);
        }

        //源地址对象
//        List<String> newSrcIpAddressObjectNameList = new ArrayList<>();
        StringBuilder ipAddressGroupCl = new StringBuilder();
        if(ObjectUtils.isNotEmpty(srcIpDto)){
            if (StringUtils.isEmpty(srcIpDto.getName())){
                String srcAddressName = this.createIpAddressObjectNameByParamDTO(srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), srcIpDto.getHosts(), null, null,null);

                srcRefIpAddressObject = srcAddressName.split(",");
                srcIpDto.setName(srcAddressName);
            }
            //生成src地址对象命令行
            if(ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcIpDto.getName(), null, srcIpDto.getSingleIpArray(), null, null, null, null,
                        null, null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newSrcIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcIpDto.getName(), null,null, srcIpDto.getRangIpArray(), null, null, null, null,
                        null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newSrcIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcIpDto.getName(), null, null, null, srcIpDto.getSubnetIntIpArray(), null, null,
                        null, null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newSrcIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcIpDto.getName(), null, null, null, null, srcIpDto.getSubnetStrIpArray(), null,
                        null, null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newSrcIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
        }
        
//        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        //生成dst地址对象命令行
        if(ObjectUtils.isNotEmpty(dstIpDto)){
            if (StringUtils.isEmpty(dstIpDto.getName())){
                String dstAddressName = this.createIpAddressObjectNameByParamDTO(dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), dstIpDto.getHosts(), null, null,null);

                dstRefIpAddressObject = dstAddressName.split(",");
                dstIpDto.setName(dstAddressName);
            }
            //生成dst地址对象命令行
            if(ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstIpDto.getName(), null, dstIpDto.getSingleIpArray(), null, null, null, null,
                        null, null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newDstIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstIpDto.getName(), null,null, dstIpDto.getRangIpArray(), null, null, null, null,
                        null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newDstIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstIpDto.getName(), null, null, null, dstIpDto.getSubnetIntIpArray(), null, null,
                        null, null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newDstIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray())){
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstIpDto.getName(), null, null, null, null, dstIpDto.getSubnetStrIpArray(), null,
                        null, null, null, null, null, map, args));
//                ArrayList<String> ipAddressObjectNameList = (ArrayList<String>)map.get(IP_ADDRESS_OBJECT_NAME_LIST);
//                newDstIpAddressObjectNameList.addAll(ipAddressObjectNameList);
            }
        }
        // 处理地址组对象
        String srcGroupName = null;
        String dstGroupName = null;
        if(ObjectUtils.isNotEmpty(srcIpDto) && (ArrayUtils.getLength(srcIpDto.getObjectNameRefArray()) + ArrayUtils.getLength(srcIpDto.getObjectGroupNameRefArray()) > 1)){
            if(srcIpDto == null){
                srcIpDto = new IpAddressParamDTO();
            }
            //生成src地址组对象命令行
            srcGroupName = this.createIpAddressObjectGroupName(srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),
                    null,srcIpDto.getHosts(),srcIpDto.getObjectNameRefArray(),srcIpDto.getObjectGroupNameRefArray(),map,args);
            ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),srcGroupName,null,srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),
                    null,srcIpDto.getHosts(),srcIpDto.getObjectNameRefArray(),srcIpDto.getObjectGroupNameRefArray(),null,null,null,null,null));
        }

        //生成dst地址组对象命令行
        if(ObjectUtils.isNotEmpty(dstIpDto) && (ArrayUtils.getLength(dstIpDto.getObjectNameRefArray()) + ArrayUtils.getLength(dstIpDto.getObjectGroupNameRefArray()) > 1)){
            if(dstIpDto == null){
                dstIpDto = new IpAddressParamDTO();
            }
            //生成dst地址组对象命令行
            dstGroupName = this.createIpAddressObjectGroupName(dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),
                    null,dstIpDto.getHosts(),dstIpDto.getObjectNameRefArray(),dstIpDto.getObjectGroupNameRefArray(),map,args);
            ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),dstGroupName,null,dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),
                    null,dstIpDto.getHosts(),dstIpDto.getObjectNameRefArray(),dstIpDto.getObjectGroupNameRefArray(),null,null,null,null,null));

        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        ArrayList<ServiceParamDTO> serviceParamList = new ArrayList<>();
        String serviceObjectCommandLine ="";
        if(ArrayUtils.isNotEmpty(serviceParam)){
            for (ServiceParamDTO serviceParamDTO:serviceParam) {
                Map theme=new HashMap();
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                log.info("生成服务对象名称:{}",serviceObjectName);
                theme.put("name",serviceObjectName);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                newService.add(serviceParamDTO);
                serviceParamList.add(serviceParamDTO);
                serviceObjectCommandLine+=this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, theme,null)+"\n";
            }
        }


        StringBuilder  allServiceCl = new StringBuilder();
        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            allServiceCl.append(newTimeCommandLine).append(StringUtils.LF);;
        }
        // 地址和服务对象命令行
        if(StringUtils.isNotBlank(ipAddressGroupCl.toString())){
            allServiceCl.append(ipAddressGroupCl.toString()).append(StringUtils.LF);
        }
        if(StringUtils.isNotBlank(serviceObjectCommandLine)){
            allServiceCl.append(serviceObjectCommandLine).append(StringUtils.LF);;
        }
           allServiceCl.append(StringUtils.LF);

        // 是否新建vip和pool
        String poolName = ObjectUtils.isEmpty(map.get("poolName")) ? null : (String) map.get("poolName");
        String createPoolCommandLine = ObjectUtils.isEmpty(map.get("createPoolCommandLine")) ? null : (String) map.get("createPoolCommandLine");

        String createVipCommandLine = ObjectUtils.isEmpty(map.get("createVipCommandLine")) ? null :  (String) map.get("createVipCommandLine");

        boolean convertOutItf = ObjectUtils.isEmpty(map.get("convertOutItf")) ? false :  (Boolean) map.get("convertOutItf");


        if(StringUtils.isNotBlank(createVipCommandLine)){
            commandLine.append(createVipCommandLine);
        }

        if(StringUtils.isNotBlank(createPoolCommandLine)){
            commandLine.append(createPoolCommandLine);
        }


        if(StringUtils.isNotBlank(allServiceCl)){
             commandLine.append(allServiceCl);
             commandLine.append(StringUtils.LF);
        }
        if("ipv6".equals(statusTypeEnum.getRuleType())){
            commandLine.append(generatePreSecurityCommandLine6());
        }else{
            commandLine.append(generatePreSecurityCommandLine());
        }
        commandLine.append(status).append(String.format(" %s ", "0")).append(StringUtils.LF);

        if(ObjectUtils.isEmpty(name)) {
            logger.error("SecurityFortinetImpl.generateSecurityPolicyCommandLine() name为空");
        }else{
            commandLine.append(String.format("set name %s", name)).append(StringUtils.LF);
        }

        if(ObjectUtils.isNotEmpty(inInterface)||ObjectUtils.isNotEmpty(srcZone)){
            String src="";

            if(null != srcZone && ObjectUtils.isNotEmpty(srcZone)){
                String[] zoneArray = srcZone.getNameArray();
                String   srcZoneName= srcZone.getName();
//                if(ArrayUtils.isNotEmpty(zoneArray)) {
//                    src += String.join(" ", zoneArray);
//                }
                if(ObjectUtils.isNotEmpty(srcZoneName)) {
                    src+=srcZoneName;
                }
            }
            if(null != inInterface && ObjectUtils.isNotEmpty(inInterface)){
                String[] interfaceNameArray =inInterface.getNameArray();
                String inInterfaceName = inInterface.getName();
//                if(ArrayUtils.isNotEmpty(interfaceNameArray)) {
//                    src += String.join(" ", interfaceNameArray);
//                }
                if(StringUtils.isBlank(src) && ObjectUtils.isNotEmpty(inInterfaceName)) {
                    src+=inInterfaceName;
                }
            }
            if(StringUtils.isNotBlank(src)) {
                commandLine.append(String.format("set srcintf %s ", src)).append(StringUtils.LF);
            }
        }else {
            commandLine.append("set srcintf \"any\" ").append(StringUtils.LF);
        }
     
        if(ObjectUtils.isNotEmpty(outInterface)||ObjectUtils.isNotEmpty(dstZone)){
            String dst="";
            if(null != dstZone && ObjectUtils.isNotEmpty(dstZone)){
                String[] zoneArray = dstZone.getNameArray();
                String dstZoneName = dstZone.getName();
//                if(ObjectUtils.isNotEmpty(zoneArray)) {
//                    dst+= String.join(" ", zoneArray);
//                }
                if(ObjectUtils.isNotEmpty(dstZoneName)) {
                    dst+=dstZoneName;
                }
            }

            if(null != outInterface && ObjectUtils.isNotEmpty(outInterface)){
                String[] interfaceNameArray =outInterface.getNameArray();
                String outInterfaceName = outInterface.getName();
//                if(ObjectUtils.isNotEmpty(interfaceNameArray)) {
//                    dst += String.join(" ", interfaceNameArray);
//                }
                if(StringUtils.isBlank(dst) && ObjectUtils.isNotEmpty(outInterfaceName)) {
                    dst+=outInterfaceName;
                }
            }

            if(StringUtils.isNotBlank(dst)) {
                commandLine.append(String.format("set dstintf %s ", dst)).append(StringUtils.LF);
            }
        }else {
            commandLine.append("set dstintf \"any\" ").append(StringUtils.LF);
        }

        if (ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            commandLine.append("set srcaddr ");
            for (String str : srcRefIpAddressObjectGroup) {
                commandLine.append(String.format("\"%s\" ",str));
            }
            commandLine.append(StringUtils.LF);
        }else if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            commandLine.append("set srcaddr ");
            for (String str : srcRefIpAddressObject) {
                commandLine.append(String.format("\"%s\" ",str));
            }
            commandLine.append(StringUtils.LF);
        }else {
            commandLine.append("set srcaddr \"all\" ").append(StringUtils.LF);
        }


        if (ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){
            commandLine.append("set dstaddr ");
            for (String str : dstRefIpAddressObjectGroup) {
                commandLine.append(String.format("\"%s\" ",str));
            }
            commandLine.append(StringUtils.LF);
        }else if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            commandLine.append("set dstaddr ");
            for (String str : dstRefIpAddressObject) {
                commandLine.append(String.format("\"%s\" ",str));
            }
            commandLine.append(StringUtils.LF);
        }else {
            commandLine.append("set dstaddr \"all\" ").append(StringUtils.LF);
        }

        if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
            commandLine.append(SET_SERVICE_STR);
            for (String newServiceObjectName : newServiceObjectNameList) {
                commandLine.append(String.format(" \"%s\" ",newServiceObjectName));
            }
            commandLine.append(StringUtils.LF);
        }
        //引用服务对象
        if(ArrayUtils.isNotEmpty(refServiceObject)){
            commandLine.append(SET_SERVICE_STR);
            for (String serviceObjectName:refServiceObject) {
                if("any".equalsIgnoreCase(serviceObjectName)) {
                    serviceObjectName="ALL";
                }
                commandLine.append(String.format(" \"%s\" ", serviceObjectName));
            }
            commandLine.append(StringUtils.LF);
        }
        //引用服务组对象
        if(ArrayUtils.isNotEmpty(refServiceObjectGroup)){
            commandLine.append(SET_SERVICE_STR);
            for (String serviceObjectGroupName:refServiceObjectGroup) {
                commandLine.append(String.format(" \"%s\" ",serviceObjectGroupName));
            }
            commandLine.append(StringUtils.LF);
        }

        if (CollectionUtils.isEmpty(newServiceObjectNameList) && ArrayUtils.isEmpty(refServiceObject) && ArrayUtils.isEmpty(refServiceObjectGroup)){
            commandLine.append(SET_SERVICE_STR + "\"ALL\"").append(StringUtils.LF);
        }

        // 本次生成的时间对象
        if(StringUtils.isNotBlank(newTimeObjectName)){
            if("any".equalsIgnoreCase(newTimeObjectName)){
                newTimeObjectName="always";
            }
            commandLine.append(String.format("set schedule \"%s\" %s",newTimeObjectName,StringUtils.LF));
        }
        //引用时间对象
        if(ArrayUtils.isNotEmpty(refTimeObject)){
            for (String objectName:refTimeObject) {
                if(StringUtils.isNotBlank(objectName)) {
                    if ("any".equalsIgnoreCase(objectName)) {
                        objectName = "always";
                    }
                    commandLine.append(String.format("set schedule \"%s\"", objectName)).append(StringUtils.LF);
                }
            }
        }
        if (StringUtils.isEmpty(newTimeObjectName) && ArrayUtils.isEmpty(refTimeObject)){
            commandLine.append("set schedule \"always\"").append(StringUtils.LF);
        }
        //动作
        if(StringUtils.isNotBlank(action)){
            action = action.toLowerCase();
            if ("permit".equals(action)) {
                action = "accept";
            }
            commandLine.append(String.format("set action %s",action)).append(StringUtils.LF);
        }
        commandLine.append("set logtraffic all").append(StringUtils.LF);

        //String description = paramDTO.getDescription();
        if(StringUtils.isNotBlank(description) && description.trim().length() > 0){
            commandLine.append(String.format("set comments %s", description)).append(StringUtils.LF);
        }
        if (StringUtils.isNotBlank(poolName)) {
            commandLine.append("set nat enable").append(StringUtils.LF);
            commandLine.append("set ippool enable").append(StringUtils.LF);
            commandLine.append(String.format("set poolname \"%s\"", poolName)).append(StringUtils.LF);
        } else if (convertOutItf) {
            commandLine.append("set nat enable").append(StringUtils.LF);
        }

        commandLine.append("show").append(StringUtils.LF);
        commandLine.append(lastLine).append(StringUtils.LF);
        if (ObjectUtils.isNotEmpty(moveSeatEnum)) {
            commandLine.append(generateChangePolicyPriorityCommandLine(id,moveSeatEnum,swapRuleNameId,null,null));
        }
        return commandLine.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        if(StringUtils.isBlank(id)){
            return StringUtils.EMPTY;
        }
        StringBuffer deleteSecurityCl = new StringBuffer();
        deleteSecurityCl.append("config firewall policy \n");
        deleteSecurityCl.append(String.format(" delete %s \n",id));
        deleteSecurityCl.append("next \n");
        return deleteSecurityCl.toString();
    }

    /**
     *
     * @param moveSeatEnum
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateChangePolicyPriorityCommandLine(String id,MoveSeatEnum moveSeatEnum,String swapRuleNameId, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder commandLine=new StringBuilder("");
        if(ObjectUtils.isNotEmpty(moveSeatEnum)){
            swapRuleNameId = StringUtils.isNotBlank(swapRuleNameId) ? swapRuleNameId : "";
            if(StringUtils.isNotBlank(id)&&(moveSeatEnum.getCode() == (MoveSeatEnum.FIRST.getCode())||moveSeatEnum.getCode() == (MoveSeatEnum.BEFORE.getCode()))){
                commandLine.append("move ").append(id).append(" before ").append(swapRuleNameId).append(StringUtils.LF);
            }else if (moveSeatEnum.getCode() == (MoveSeatEnum.AFTER.getCode())){
                commandLine.append("move ").append(id).append(" after ").append(swapRuleNameId).append(StringUtils.LF);
            }
        }
        return commandLine.toString();
    }


    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder tcpCommandLine = new StringBuilder("");
        tcpCommandLine.append(String.format("set protocol TCP/UDP/SCTP")).append(StringUtils.LF);
        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            if(ObjectUtils.isNotEmpty(dstSinglePortArray)){
                Integer dstPort= dstSinglePortArray[0];
                tcpCommandLine.append(String.format("set tcp-portrange %s",dstPort)).append(StringUtils.LF);
            }
            if(ObjectUtils.isNotEmpty(dstRangePortArray)){
                PortRangeDTO dstPortRangeDTO= dstRangePortArray[0];
                tcpCommandLine.append(String.format("set tcp-portrange %s-%s",dstPortRangeDTO.getStart(),dstPortRangeDTO.getEnd())).append(StringUtils.LF);
            }
        }
        return tcpCommandLine.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder udpCommandLine = new StringBuilder("");
        udpCommandLine.append("set protocol TCP/UDP/SCTP").append(StringUtils.LF);
        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
                Integer port = dstSinglePortArray[0];
                udpCommandLine.append(String.format("set udp-portrange %s", port)).append(StringUtils.LF);
            }
            if(ObjectUtils.isNotEmpty(dstRangePortArray)){
                PortRangeDTO dstPortRangeDTO= dstRangePortArray[0];
                udpCommandLine.append(String.format("set udp-portrange %s-%s",dstPortRangeDTO.getStart(),dstPortRangeDTO.getEnd())).append(StringUtils.LF);
            }
        }
        return udpCommandLine.toString();
    }


    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder icmpCommandLine = new StringBuilder("");
        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            icmpCommandLine.append("set protocol ICMP").append(StringUtils.LF);
        }
        return icmpCommandLine.toString();
    }


    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuilder commandLine=new StringBuilder("");
        if (isVsys) {
            commandLine.append("config vdom ").append(StringUtils.LF);
            commandLine.append(String.format(EDIT_STR,vsysName)).append(StringUtils.LF);
        }else{
            if(ObjectUtils.isNotEmpty(map)) {
                boolean hasVsys = (boolean) map.get("hasVsys");
                if (hasVsys) {
                    commandLine.append("config vdom ").append(StringUtils.LF);
                    vsysName = "root";
                    commandLine.append(String.format(EDIT_STR, vsysName)).append(StringUtils.LF);
                }
            }
        }
           return commandLine.toString();
    }



    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (RuleIPTypeEnum.IP4.getCode() == ipTypeEnum.getCode()) {
            sb.append(this.generatePreIpAddressObjectGroupCommandLine());
        } else if (RuleIPTypeEnum.IP6.getCode() == ipTypeEnum.getCode()) {
            sb.append(this.generatePreIpAddressObjectGroupCommandLine6());
        }

        sb.append(String.format("delete %s", groupName));
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotBlank(delStr) && delStr.contains("pool")) {
            sb.append(this.generatePreIpPoolObjectCommandLine());
        } else if (StringUtils.isNotBlank(delStr) && delStr.endsWith("vip")) {
            sb.append(this.generatePreIpVipObjectCommandLine());
            name = name.substring(0, name.lastIndexOf("_vip"));
        } else if (null != ipTypeEnum && RuleIPTypeEnum.IP4.getCode().equals(ipTypeEnum.getCode())) {
            sb.append(this.generatePreIpAddressObjectCommandLine());
        } else if (null != ipTypeEnum && RuleIPTypeEnum.IP6.getCode().equals(ipTypeEnum.getCode())) {
            sb.append(this.generatePreIpAddressObjectCommandLine6());
        }
        sb.append(String.format("delete %s", name));
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return this.deleteServiceObjectCommandLine(delStr, attachStr, groupName, map, args);
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer sb = new StringBuffer();
        if(map!=null && ObjectUtils.isNotEmpty(map.get("objectType")) && DeviceObjectTypeEnum.SERVICE_GROUP_OBJECT.getCode().equalsIgnoreCase((String) map.get("objectType"))){
            sb.append("config firewall service group").append(StringUtils.LF);
        }else if(map!=null && ObjectUtils.isNotEmpty(map.get("objectType")) && DeviceObjectTypeEnum.SERVICE_OBJECT.getCode().equalsIgnoreCase((String) map.get("objectType"))){
            sb.append(this.generatePreServiceObjectCommandLine());
        }
        sb.append(String.format("delete %s", name));
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        StringBuffer sb = new StringBuffer();
        String content = null;
        if(map!=null && map.get("delStr")!=null){
            content = (String) map.get("delStr");
        }
        if(content!=null && !content.startsWith("单次")){
            sb.append(generatePreTimeGroupObjectCommandLine());
        }else {
            sb.append(this.generatePreAbsoluteTimeObjectCommandLine());
        }
        sb.append(String.format("delete %s", name));
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String deletePeriodicTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        StringBuffer sb = new StringBuffer();
        String content = null;
        if(map!=null && map.get("delStr")!=null){
            content = (String) map.get("delStr");
        }
        if(content!=null && !content.startsWith("周期")){
            sb.append(generatePreTimeGroupObjectCommandLine());
        }else {
            sb.append(this.generatePrePeriodicTimeObjectCommandLine());
        }
        sb.append(String.format("delete %s", name));
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    private String generatePreTimeGroupObjectCommandLine() {
        StringBuilder preTimeGroupObjectCommandLine = new StringBuilder("");
        preTimeGroupObjectCommandLine.append("config firewall schedule group ").append("\n");
        return preTimeGroupObjectCommandLine.toString();
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end\n";
    }

    public String generateProtocolCommandline(String protocol,String dstPorts,String postPort,String protocolString){
        StringBuffer sb = new StringBuffer();
        if((StringUtils.isNotBlank(dstPorts) && !dstPorts.equals("any")) || (StringUtils.isNotBlank(postPort) && !postPort.equals("any"))){
            sb.append("set portforward enable\n");
            sb.append("set protocol ").append(protocolString.toLowerCase()).append("\n");

            if (!protocolString.equalsIgnoreCase("ICMP")) {
                if(StringUtils.isNotEmpty(dstPorts) && !StringUtils.equalsAnyIgnoreCase(dstPorts,POLICY_STR_VALUE_ANY)){
                    sb.append("set extport ").append(dstPorts).append("\n");
                } else {
//                    if(StringUtils.isNotEmpty(protocol)){
//                        sb.append("set extport 1-65535").append("\n");
//                    }
                }
                if(StringUtils.isNotEmpty(postPort) && !StringUtils.equalsAnyIgnoreCase(postPort,POLICY_STR_VALUE_ANY)){
                    sb.append("set mappedport ").append(postPort).append("\n");
                }else {
//                    if(StringUtils.isNotEmpty(protocol)){
//                        sb.append("set mappedport 1-65535").append("\n");
//                    }
                }
            }
        }else if(StringUtils.isNotEmpty(protocol)){
            sb.append("set portforward disable\n");
        }
        return sb.toString();
    }

    @Override
    public String generateManageIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                           IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                           String[] fqdnArray, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();

        if (StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            sb.append(generatePreIpAddressObjectCommandLine());
            sb.append(String.format("edit %s\n", name));

            if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                for (IpAddressSubnetIntDTO subnetStrDTO : subnetIntIpArray) {
                    sb.append(String.format("set subnet %s/%s\n", subnetStrDTO.getIp(), subnetStrDTO.getMask()));
                }
            }

            if (ArrayUtils.isNotEmpty(singleIpArray)) {
                for (String ip : singleIpArray) {
                    sb.append(String.format("set subnet %s/32\n", ip));
                }
            }

            if (ArrayUtils.isNotEmpty(rangIpArray)) {
                for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                    sb.append("set type iprange\n");
                    sb.append(String.format("set start-ip %s\n", rangeDTO.getStart()));
                    sb.append(String.format("set end-ip %s\n", rangeDTO.getEnd()));
                }
            }

            if (ArrayUtils.isNotEmpty(fqdnArray)) {
                for (String fqdn : fqdnArray) {
                    sb.append("set type fqdn\n");
                    sb.append(String.format("set fqdn %s\n", fqdn));
                }
            }
            sb.append("next\n");
            sb.append("end\n");
        } else if (StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            //不用删除对象
            //sb.append(String.format("delete %s\n\n", address));
        }

        return sb.toString();
    }

    @Override
    public String generateManageIpAddressGroupObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                                IpAddressRangeDTO[] rangIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                                String[] fqdnArray, String[] objectNameArray, String[] addObjectNameArray, String[] delObjectNameArray, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(generatePreIpAddressObjectGroupCommandLine());
        sb.append(String.format("edit %s\n", name));
        sb.append("set member ");
        for (String objectName : objectNameArray) {
            sb.append(objectName + " ");
        }
        sb.append("\n");
        sb.append("next\n");
        return sb.toString();
    }

}
