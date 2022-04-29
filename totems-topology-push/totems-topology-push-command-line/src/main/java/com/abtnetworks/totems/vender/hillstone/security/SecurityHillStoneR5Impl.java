package com.abtnetworks.totems.vender.hillstone.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Description
 * @Version 山石命令行实现类
 * @Created by hw on '2021/4/7 16:39'.
 */
public class SecurityHillStoneR5Impl extends OverAllGeneratorAbstractBean {

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuffer preCommandline = new StringBuffer();
        if (isVsys != null && isVsys && StringUtils.isNotBlank(vsysName)) {
            preCommandline.append("enter-vsys " + vsysName + "\n");
        }
        preCommandline.append("configure \n");
        return preCommandline.toString();
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end \n";
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
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO,
                                          Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isEmpty(name)){
            this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
        }
        if(ObjectUtils.isEmpty(absoluteTimeParamDTO)){
            return StringUtils.EMPTY;
        }
        String timeNameCommandLine = generateTimeObjectName(name, map, null);

        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("schedule %s \n",timeNameCommandLine));

        // 指定绝对计划 absolute {[start start-date start-time] [end end-date end-time]}
        String startTime = TotemsTimeUtils.transformDateFormat(String.format("%s %s",absoluteTimeParamDTO.getStartDate(),absoluteTimeParamDTO.getStartTime()), TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.HILLSTONE_TIME_FORMAT);
        String endTime = TotemsTimeUtils.transformDateFormat(String.format("%s %s",absoluteTimeParamDTO.getEndDate(),absoluteTimeParamDTO.getEndTime()), TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.HILLSTONE_TIME_FORMAT);
        timeCommandLineBuffer.append(String.format(" absolute start %s end %s ",startTime,endTime)).append(StringUtils.LF);
        timeCommandLineBuffer.append("exit").append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    /**
     * 生成周期计划时间对象命令行
     * @param name 时间标记字符串
     * @param attachStr 附加字符串
     * @param periodicTimeParamDTO 周期计划
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generatePeriodicTimeCommandLine(String name, String attachStr, PeriodicTimeParamDTO periodicTimeParamDTO,
                                          Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isEmpty(name)){
            this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
        }
        String timeNameCommandLine = generateTimeObjectName(name, map, null);

        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("schedule %s \n",timeNameCommandLine));

        //指定周期计划 periodic {daily | weekdays | weekend | [monday] […] [sunday]} starttime to end-time
        //周期计划
        timeCommandLineBuffer.append(" periodic ");
        if(ArrayUtils.isNotEmpty(periodicTimeParamDTO.getCycle())){
            for (String date:periodicTimeParamDTO.getCycle()) {
                timeCommandLineBuffer.append(date).append(StringUtils.SPACE);
            }
        }
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getCycleStart())){
            timeCommandLineBuffer.append(String.format(" %s ",periodicTimeParamDTO.getCycleStart()));
        }
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getCycleEnd())){
            timeCommandLineBuffer.append(String.format(" to %s ",periodicTimeParamDTO.getCycleEnd()));
        }
        timeCommandLineBuffer.append(StringUtils.LF);
        timeCommandLineBuffer.append("exit").append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args){
        return String.format("no schedule %s %s",timeFlag,StringUtils.LF);
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        if(StringUtils.isNotBlank(name) && name.contains("\"")){
            return String.format("no schedule %s %s", name,StringUtils.LF);
        }else {
            return String.format("no schedule \"%s\" %s", name,StringUtils.LF);
        }
    }

    /**
     * ip地址对象命令行生成
     * @param singleIpArray 单个ip 集合
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        for (String ip:singleIpArray) {
            ipv4ArrayCommandLine.append(deletePrefix).append(String.format(" ip %s/32\n",ip));
        }
        return ipv4ArrayCommandLine.toString();
    }

    /**
     * host地址对象命令行生成
     * @param hosts host 集合
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception{
        if(ArrayUtils.isNotEmpty(hosts)){
            String deletePrefix = StringUtils.EMPTY;
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                deletePrefix = " no";
            }
            StringBuffer hostCommandLine = new StringBuffer();
            for (String host : hosts) {
                hostCommandLine.append(deletePrefix).append(String.format(" host %s \n",host));
            }
            return hostCommandLine.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * ip范围地址对象
     * 同一范围地址对象只能和一个范围地址绑定
     * @param rangIpArray
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        StringBuffer rangeIpCl = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO:rangIpArray) {
            rangeIpCl.append(deletePrefix).append(String.format(" range %s %s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
        }
        return rangeIpCl.toString();
    }


    /**
     * int子网地址对象
     * @param subnetIpArray
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                subnetIpv4Cl.append(deletePrefix).append(String.format(" ip %s/%s \n",ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
            }
        }
        return subnetIpv4Cl.toString();
    }


    /**
     * String子网地址对象
     * 同一子网地址对象只能和一个子网地址绑定
     * @param subnetIpArray 子网ip 集合
     * @param sub sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args)  {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetIpArray) {
                int maskBit;
                String wildcardMask;
                if(MaskTypeEnum.mask.getType().equalsIgnoreCase(ipAddressSubnetStrDTO.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                } else {
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(ipAddressSubnetStrDTO.getMask());
                }
                subnetIpv4Cl.append(deletePrefix).append(String.format(" ip %s/%s \n",ipAddressSubnetStrDTO.getIp(), maskBit));
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        for (String ip:singleIpArray) {
            ipv4ArrayCommandLine.append(deletePrefix).append(String.format(" ip %s/128 \n",ip));
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        StringBuffer rangeIpCl = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO:rangIpArray) {
            rangeIpCl.append(deletePrefix).append(String.format(" range %s %s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
        }
        return rangeIpCl.toString();
    }

    /**
     * 子网 前缀
     * @param subnetIpArray 子网ip 集合
     * @param sub sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                subnetIpv4Cl.append(deletePrefix).append(String.format(" ip %s/%s \n",ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum,String name, Map<String, Object> map, String[] args) throws Exception {

        if (ipTypeEnum == null){
            return String.format("\"%s\" ",name);
        }else if (RuleIPTypeEnum.IP4.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("\"%s\" ",name);
        } else if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("\"%s\" ipv6 ",name);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 删除地址对象
     * @param delStr 删除 符号
     * @param name ip地址对象名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     */
    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum,String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotBlank(name) && name.contains("\"")) {
            return String.format("no address %s \n", name);
        } else {
            return String.format("no address \"%s\" \n", name);
        }
    }

    /**
     * 创建地址对象
     * @param statusTypeEnum 状态类型
     * @param ipTypeEnum IP枚举类型
     * @param name 地址对象名称
     * @param idStr 地址对象id
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
        public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                     String name, String idStr,
                                                     String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                     IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                     String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray,
                                                     String description, String attachStr, String delStr,
                                                     Map<String, Object> map, String[] args) throws Exception {
        StringBuffer commandLine = new StringBuffer();
        // 处理ANY
        if("any".equalsIgnoreCase(name) || "ipv6-any".equalsIgnoreCase(name)){
            commandLine.append("address ").append(name).append(StringUtils.SPACE);
            if(RuleIPTypeEnum.IP4.getName().equalsIgnoreCase(ipTypeEnum.getName())){
                commandLine.append("predefined").append(StringUtils.LF);
                commandLine.append(" ip 0.0.0.0/0").append(StringUtils.LF);
            } else {
                commandLine.append("ipv6 predefined").append(StringUtils.LF);
                commandLine.append(" ip ::/0").append(StringUtils.LF);

            }
            commandLine.append("exit").append(StringUtils.LF);
            return commandLine.toString();
        }
        Integer id = null;
        if(StringUtils.isNotBlank(idStr)){
            id = Integer.parseInt(idStr);
        }
        // ip地址对象
        commandLine.append("address ");
        if(id != null){
            commandLine.append(String.format("id %s ",++id));
        }
        commandLine.append(String.format("%s \n",generateIpAddressObjectName(ipTypeEnum,name,null,null)));

        if(StringUtils.isNotBlank(description)){
            commandLine.append(String.format(" description \"%s_%s\" \n",description,id));
        }

        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String singleIp : singleIpArray) {
                commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum,new String[]{singleIp},null,null));
            }
        }
        // ip范围地址对象
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO  ipAddressRangeDTO: rangIpArray) {
                commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,null));
            }
        }
        // int子网地址对象
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,null,null));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,null,null));
            }
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            if(ArrayUtils.isNotEmpty(fqdnArray)){
                commandLine.append(this.generateHostCommandLine(statusTypeEnum,fqdnArray,null,null));
            }
        }
        //引用地址对象 member address-entry
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectNameRef:objectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    commandLine.append(String.format(" no member \"%s\" \n",objectNameRef));
                }else{
                    commandLine.append(String.format(" member \"%s\" \n",objectNameRef));
                }
            }
        }
        commandLine.append("exit").append(StringUtils.LF);
        return commandLine.toString();
    }

    @Override
    public String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum,String groupName, Map<String, Object> map, String[] args) throws Exception {
        return this.generateIpAddressObjectName(ipTypeEnum,groupName,map,args);
    }

    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {
        StringBuffer ipAddressGroupCl = new StringBuffer();
        // 生成地址对象命令行
        List<String> newIpAddressObjectNameList = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
            newIpAddressObjectNameList.add(singleIpObjectName);
            ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, singleIpObjectName, null, singleIpArray, null, null, null, null,
                    null, null, null, null, null, map, args));
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(rangIpArray, map, args);
            newIpAddressObjectNameList.add(rangeIpObjectName);
            ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, rangeIpObjectName, null,null, rangIpArray, null, null, null, null,
                    null, null, null, null, map,args));
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(subnetIntIpArray, null, map, args);
            newIpAddressObjectNameList.add(subnetIntIpObjectName);
            ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIntIpObjectName, null, null, null, subnetIntIpArray, null, null,
                    null, null, null, null, null, map, args));
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null,subnetStrIpArray, map, args);
            newIpAddressObjectNameList.add(subnetStrIpObjectName);
            ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetStrIpObjectName, null, null, null, null, subnetStrIpArray, null,
                    null, null, null, null, null, map, args));
        }
        ipAddressGroupCl.append(String.format("address %s \n",this.generateIpAddressObjectGroupName(ipTypeEnum,name,null,null)));
        if(StringUtils.isNotBlank(description)){
            ipAddressGroupCl.append(String.format(" description %s \n",description));
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            if(ArrayUtils.isNotEmpty(fqdnArray)){
                ipAddressGroupCl.append(this.generateHostCommandLine(statusTypeEnum,fqdnArray,null,null));
            }
        }
        // 本次生成的地址对象
        if(CollectionUtils.isNotEmpty(newIpAddressObjectNameList)){
            for (String objectName:newIpAddressObjectNameList) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" no member %s \n",objectName));
                } else {
                    ipAddressGroupCl.append(String.format(" member %s \n",objectName));
                }
            }
        }
        //引用地址对象
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName:objectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" no member %s \n",objectName));
                } else {
                    ipAddressGroupCl.append(String.format(" member %s \n",objectName));
                }
            }
        }
        //引用地址组对象
        if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
            for (String objectGroupName:objectGroupNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" no member %s \n",objectGroupName));
                } else {
                    ipAddressGroupCl.append(String.format(" member %s \n",objectGroupName));
                }
            }
        }
        ipAddressGroupCl.append("exit").append(StringUtils.LF).append(StringUtils.LF);
        return ipAddressGroupCl.toString();
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isNotBlank(groupName) && groupName.contains("\"")){
            return String.format("no address %s \n", groupName);
        }else {
            return String.format("no address \"%s\" \n", groupName);
        }
    }

    /**
     * icmp 服务对象
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachTypeArray 协议附件code值
     * @param protocolAttachCodeArray 协议附件code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return icmp type type-value [code min-code [max-code]] [timeout time-outvalue | timeout-day time-out-value]
     * @throws Exception
     */
    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                          Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                          Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                          String[] timeOutArray, String[] objectNameRefArray,
                                          Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(protocolAttachTypeArray)){
            protocolAttachTypeArray = new String[]{"any"};
        }
        StringBuffer icmpCl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            icmpCl.append(" no");
        }
        icmpCl.append(String.format(" icmp type %s ", protocolAttachTypeArray[0]));
        if(ArrayUtils.isNotEmpty(protocolAttachCodeArray)){
            icmpCl.append(String.format("code %s ",protocolAttachCodeArray[0]));
            if(protocolAttachCodeArray.length > 1){
                icmpCl.append(protocolAttachCodeArray[1]);
            }
        }
        if(ArrayUtils.isNotEmpty(timeOutArray)){
            icmpCl.append(String.format(" timeout %s ",timeOutArray[0]));
            if(timeOutArray.length > 1){
                icmpCl.append(String.format(" timeout-day %s ",timeOutArray[1]));
            }
        }
        icmpCl.append(StringUtils.LF);
        return icmpCl.toString();
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                           Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * 生成 TCP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return {tcp | udp} dst-port min-port [max-port] [src-port min-port [maxport]] [timeout time-out-value | timeout-day time-out-value]
     * timeout  超时时间。 单位为秒， 取值范围1-65535
     * timeout-day 长连接的超时时间。 单位为天， 取值范围1-1000
     * @throws Exception
     */
    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray,
                                         Map<String, Object> map, String[] args) throws Exception {
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        StringBuffer timeoutBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format("%s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        if(ArrayUtils.isNotEmpty(timeOutArray)){
            timeoutBuffer.append(String.format(" timeout %s ",timeOutArray[0]));
            if(timeOutArray.length > 1){
                timeoutBuffer.append(String.format(" timeout-day %s ",timeOutArray[1]));
            }
        }
        StringBuffer tcpCommandLine = new StringBuffer();
        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())){
            tcpCommandLine.append(" no ");
        }
        tcpCommandLine.append(" tcp  ");
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            tcpCommandLine.append(String.format("dst-port %s ",dstPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            tcpCommandLine.append(String.format("src-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(timeoutBuffer.toString())){
            tcpCommandLine.append(timeoutBuffer.toString());
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }

    /**
     * 生成 UDP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray,
                                         Map<String, Object> map, String[] args) throws Exception {
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        StringBuffer timeoutBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format("%s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }
        if(ArrayUtils.isNotEmpty(timeOutArray)){
            timeoutBuffer.append(String.format(" timeout %s ",timeOutArray[0]));
            if(timeOutArray.length > 1){
                timeoutBuffer.append(String.format(" timeout-day %s ",timeOutArray[1]));
            }
        }
        StringBuffer udpCommandLine = new StringBuffer();
        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())){
            udpCommandLine.append(" no ");
        }
        udpCommandLine.append(" udp  ");
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            udpCommandLine.append(String.format("dst-port %s ",dstPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            udpCommandLine.append(String.format("src-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(timeoutBuffer.toString())){
            udpCommandLine.append(timeoutBuffer.toString());
        }
        udpCommandLine.append(StringUtils.LF);
        return udpCommandLine.toString();
    }

    /**
     * 生成 TCP_UDP 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateTCP_UDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                             Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                             Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                             String[] timeOutArray, String[] objectNameRefArray,
                                             Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * 生成 其他协议 命令行
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray 源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray 源端口： Str 单个端口类型
     * @param srcRangePortArray 源端口：数字 范围端口类型
     * @param dstSinglePortArray 目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray 目的端口： Str 单个端口类型
     * @param dstRangePortArray 目的端口：数字 范围端口类型
     * @param timeOutArray 超时时间
     * @param objectNameRefArray 引用对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return protocol protocol-number [timeout time-out-value | timeout-day timeout-value]
     * protocol-number – 指定自定义服务的协议号。 范围是1到255
     * @throws Exception
     */
    @Override
    public String generateOtherCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                           Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        StringBuffer otherCommandLine = new StringBuffer();
        if(map == null){
            return StringUtils.EMPTY;
        }
        if(!map.containsKey("protocolNumber")){
            return StringUtils.EMPTY;
        }
        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())){
            otherCommandLine.append(" no ");
        }
        otherCommandLine.append(String.format(" protocol %s ",map.get("protocolNumber")));
        if(ArrayUtils.isNotEmpty(timeOutArray)){
            otherCommandLine.append(String.format(" timeout %s ",timeOutArray[0]));
            if(timeOutArray.length > 1){
                otherCommandLine.append(String.format(" timeout-day %s ",timeOutArray[1]));
            }
        }
        otherCommandLine.append(StringUtils.LF);
        return otherCommandLine.toString();
    }


    @Override
    public String generatePortRefStrCommandLine(String[] strRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }


    @Override
    public String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("\"%s\"",name);
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isNotBlank(name) && name.contains("\"")){
            return String.format("no service %s %s",name,StringUtils.LF);
        }else {
            return String.format("no service \"%s\" %s",name,StringUtils.LF);
        }
    }

    @Override
    public String createServiceObjectName(ServiceParamDTO serviceParamDTO, Map<String, Object> map, String[] args) {
        if(serviceParamDTO == null || serviceParamDTO.getProtocol() == null){
            return StringUtils.EMPTY;
        }
        String type = serviceParamDTO.getProtocol().getType();
        if(ArrayUtils.isEmpty(serviceParamDTO.getSrcSinglePortArray()) && ArrayUtils.isEmpty(serviceParamDTO.getSrcSinglePortStrArray()) && ArrayUtils.isEmpty(serviceParamDTO.getSrcRangePortArray()) &&
                ArrayUtils.isEmpty(serviceParamDTO.getDstSinglePortArray()) && ArrayUtils.isEmpty(serviceParamDTO.getDstSinglePortStrArray()) && ArrayUtils.isEmpty(serviceParamDTO.getDstRangePortArray()) ){
            if(ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(type) || ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(type)){
                return type+"-any";
            } else if(ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(type) || ProtocolTypeEnum.ICMP6.getType().equalsIgnoreCase(type)){
                return type;
            }
        }
        return super.createServiceObjectName(serviceParamDTO, map, args);
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
        StringBuffer serviceObjectCl = new StringBuffer();
        if(CollectionUtils.isEmpty(serviceParamDTOList)){
            return String.format("service %s \nexit \n",name);
        }
        if("any".equalsIgnoreCase(name) || "tcp-any".equalsIgnoreCase(name) || "udp-any".equalsIgnoreCase(name) || "icmp".equalsIgnoreCase(name) || "icmp6".equalsIgnoreCase(name)){
            return StringUtils.EMPTY;
        }
        serviceObjectCl.append(String.format("service %s \n",this.generateServiceObjectName(name,null,null)));
        for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
            if(ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateTCPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,null));
            } else if(ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateUDPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,null));
            } else if(ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateICMPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,null));
            } else {
                serviceObjectCl.append(this.generateOtherCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,null));
            }
        }
        if(StringUtils.isNotBlank(name)){
            serviceObjectCl.append("exit").append(StringUtils.LF);
        }
        return serviceObjectCl.toString();
    }

    @Override
    public String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("\"%s\" ",groupName);
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isNotBlank(groupName) && groupName.contains("\"")) {
            return String.format("no servgroup %s %s",groupName,StringUtils.LF);
        }else {
            return String.format("no servgroup \"%s\" %s",groupName,StringUtils.LF);
        }
    }

    /**
     * 创建服务对象组 命令行
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
    public String generateServiceObjectGroupCommandLine(StatusTypeEnum statusTypeEnum,
                                                        String name, String id, String attachStr,
                                                        List<ServiceParamDTO> serviceParamDTOList,
                                                        String description,
                                                        String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray,
                                                        Map<String, Object> map, String[] args) throws Exception {
        //生成服务对象
        List<String> newServiceObjectNameList = new ArrayList<String>();
        StringBuffer serviceObjectGroupCl = new StringBuffer();
        if(CollectionUtils.isNotEmpty(serviceParamDTOList)){
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                newService.add(serviceParamDTO);
                serviceObjectGroupCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null, null));
            }
        }
        if(StringUtils.isBlank(name)){
            name = this.createServiceObjectGroupName(serviceParamDTOList,serviceObjectNameRefArray,serviceObjectGroupNameRefArray,null,null);
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " no";
        }
        serviceObjectGroupCl.append(String.format("servgroup %s \n",this.generateServiceObjectGroupName(name, null, null)));
        if(StringUtils.isNotBlank(description)){
            serviceObjectGroupCl.append(String.format(" description %s \n",description));
        }
        if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
            for (String serviceObjectName:newServiceObjectNameList) {
                serviceObjectGroupCl.append(String.format(" service %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for (String serviceObjectName:serviceObjectNameRefArray) {
                serviceObjectGroupCl.append(deletePrefix).append(String.format(" service %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            for (String serviceObjectGroupName:serviceObjectGroupNameRefArray) {
                serviceObjectGroupCl.append(deletePrefix).append(String.format(" service %s \n",serviceObjectGroupName));
            }
        }
        serviceObjectGroupCl.append("exit").append(StringUtils.LF);
        return serviceObjectGroupCl.toString();
    }

    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("\"%s\"",name);
    }

    /**
     * 生成安全策略 命令行
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
     * @param srcIpDto 源ip
     * @param dstIpDto 目的ip
     * @param serviceParam 服务（源端口，目的端口，协议）
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
        // 处理时间对象
        String newTimeObjectName = null;
        String newTimeCommandLine = null;
        if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName,null,absoluteTimeParamDTO,map,null);
        } else if(ObjectUtils.isNotEmpty(periodicTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(newTimeObjectName,null,periodicTimeParamDTO,map,null);
        }
        // 处理地址对象
        List<String> newSrcIpAddressObjectNameList = new ArrayList<>();
        StringBuffer ipAddressGroupCl = new StringBuffer();
        if(ObjectUtils.isNotEmpty(srcIpDto)){
            //生成src地址对象命令行
            if(ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())){
                String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(srcIpDto.getSingleIpArray(), map, args);
                newSrcIpAddressObjectNameList.add(singleIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), singleIpObjectName, null, srcIpDto.getSingleIpArray(), null, null, null, null,
                        null, null, null, null, null, map, args));
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())){
                String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(srcIpDto.getRangIpArray(), map, args);
                newSrcIpAddressObjectNameList.add(rangeIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), rangeIpObjectName, null,null, srcIpDto.getRangIpArray(), null, null, null, null,
                        null, null, null, null, map, args));
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())){
                String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(srcIpDto.getSubnetIntIpArray(), null, map, args);
                newSrcIpAddressObjectNameList.add(subnetIntIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), subnetIntIpObjectName, null, null, null, srcIpDto.getSubnetIntIpArray(), null, null,
                        null, null, null, null, null, map, args));
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())){
                String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null,srcIpDto.getSubnetStrIpArray(), map, args);
                newSrcIpAddressObjectNameList.add(subnetStrIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, srcIpDto.getSubnetStrIpArray(), null,
                        null, null, null, null, null, map, args));
            }
        }

        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        //生成dst地址对象命令行
        if(ObjectUtils.isNotEmpty(dstIpDto)){
            //生成dst地址对象命令行
            if(ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray())){
                String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(dstIpDto.getSingleIpArray(), map, args);
                newDstIpAddressObjectNameList.add(singleIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, dstIpDto.getSingleIpArray(), null, null, null, null,
                        null, null, null, null, null, map, args));
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray())){
                String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(dstIpDto.getRangIpArray(), map, args);
                newDstIpAddressObjectNameList.add(rangeIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), rangeIpObjectName, null,null, dstIpDto.getRangIpArray(), null, null, null, null,
                        null, null, null, null, map, args));
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray())){
                String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(dstIpDto.getSubnetIntIpArray(), null, map, args);
                newDstIpAddressObjectNameList.add(subnetIntIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetIntIpObjectName, null, null, null, dstIpDto.getSubnetIntIpArray(), null, null,
                        null, null, null, null, null, map, args));
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray())){
                String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null,dstIpDto.getSubnetStrIpArray(), map, args);
                newDstIpAddressObjectNameList.add(subnetStrIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, dstIpDto.getSubnetStrIpArray(), null,
                        null, null, null, null, null, map, args));
            }
        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        String serviceObjectCommandLine = "";
        if(ArrayUtils.isNotEmpty(serviceParam)){
            for (ServiceParamDTO serviceParamDTO:serviceParam) {
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                newService.add(serviceParamDTO);
                serviceObjectCommandLine += this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null, null);
            }
        }
        StringBuffer securityPolicyCl = new StringBuffer();
        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        // 地址和服务对象命令行
        if(StringUtils.isNotBlank(ipAddressGroupCl.toString())){
            securityPolicyCl.append(ipAddressGroupCl.toString());
        }
        if(StringUtils.isNotBlank(serviceObjectCommandLine)){
            securityPolicyCl.append(serviceObjectCommandLine);
        }

        securityPolicyCl.append(StringUtils.LF);
        // 策略命令行
        securityPolicyCl.append("rule ");
        if(StringUtils.isNotBlank(id) && StringUtils.isNumeric(id)){
            int ruleId = Integer.parseInt(id);
            // 山石策略id 的取值 范围是1-1256，不在该范围的不显示id
            if(ruleId >= 1 && ruleId <= 1256){
                securityPolicyCl.append(String.format("id %s ",ruleId));
            }
        }
        // 交换位置
        swapRuleNameId = StringUtils.isNotBlank(swapRuleNameId) ? swapRuleNameId : "";
        if(moveSeatEnum != null){
            if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() == moveSeatEnum.getCode()){
                securityPolicyCl.append(" top").append(StringUtils.LF);
            } else if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() != moveSeatEnum.getCode() && StringUtils.isNotBlank(swapRuleNameId)){
                securityPolicyCl.append(String.format("%s %s %s",moveSeatEnum.getKey(), swapRuleNameId,StringUtils.LF));
            } else {
                securityPolicyCl.append(StringUtils.LF);
            }
        }
        String ruleName = this.generateSecurityPolicyName(name, null, null);
        if(StringUtils.isNotBlank(name)){
            securityPolicyCl.append(" name ").append(ruleName).append(StringUtils.LF);
        }
        securityPolicyCl.append(String.format(" action %s",action)).append(StringUtils.LF);
        if(StringUtils.isNotBlank(description)){
            securityPolicyCl.append(String.format(" description \"%s\"",description)).append(StringUtils.LF);
        }
        if(StringUtils.isNotBlank(logFlag)){
            securityPolicyCl.append(String.format(" log \"%s\"",logFlag)).append(StringUtils.LF);

        }
        String srcZoneName = null;
        String dstZoneName = null;
        if(ObjectUtils.isNotEmpty(srcZone)){
            if(StringUtils.isNotBlank(srcZone.getName())){
                srcZoneName = srcZone.getName();
            } else if(ArrayUtils.isNotEmpty(srcZone.getNameArray())){
                srcZoneName = srcZone.getNameArray()[0];
            }
        }
        if(ObjectUtils.isNotEmpty(dstZone)){
            if(StringUtils.isNotBlank(dstZone.getName())){
                dstZoneName = dstZone.getName();
            } else if(ArrayUtils.isNotEmpty(dstZone.getNameArray())){
                dstZoneName = dstZone.getNameArray()[0];
            }
        }

        //源域目的域
        if(StringUtils.isNotBlank(srcZoneName)){
            securityPolicyCl.append(String.format(" src-zone \"%s\" ",srcZoneName)).append(StringUtils.LF);
        }
        if(StringUtils.isNotBlank(dstZoneName)){
            securityPolicyCl.append(String.format(" dst-zone \"%s\" ",dstZoneName)).append(StringUtils.LF);
        }

        // 本次生成的时间对象
        if(StringUtils.isNotBlank(newTimeObjectName)){
            securityPolicyCl.append(String.format(" schedule \"%s\" %s",newTimeObjectName,StringUtils.LF));
        }
        //引用时间对象
        if(ArrayUtils.isNotEmpty(refTimeObject)){
            for (String timeObjectName:refTimeObject) {
                securityPolicyCl.append(String.format(" schedule \"%s\" %s",timeObjectName,StringUtils.LF));
            }
        }

        // 本次生成的源地址
        if(CollectionUtils.isNotEmpty(newSrcIpAddressObjectNameList)){
            for (String newSrcIpAddressObjectName : newSrcIpAddressObjectNameList) {
                securityPolicyCl.append(String.format(" src-addr \"%s\" ", newSrcIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        // 引用地址和地址组对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            for (String srcRefIpAddressObjectName:srcRefIpAddressObject) {
                securityPolicyCl.append(String.format(" src-addr \"%s\" ", srcRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            for (String srcRefIpAddressObjectGroupName:srcRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" src-addr \"%s\" ", srcRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }
        // 处理src-addr any问题
        if(ArrayUtils.isEmpty(srcRefIpAddressObject) && ArrayUtils.isEmpty(srcRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newSrcIpAddressObjectNameList)){
            securityPolicyCl.append(" src-addr any").append(StringUtils.LF);
        }

        // 本地生成的目的地址
        if(CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)){
            for (String newDstIpAddressObjectName : newDstIpAddressObjectNameList) {
                securityPolicyCl.append(String.format(" dst-addr \"%s\" ", newDstIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            for (String dstRefIpAddressObjectName:dstRefIpAddressObject) {
                securityPolicyCl.append(String.format(" dst-addr \"%s\" ", dstRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){
            for (String dstRefIpAddressObjectGroupName:dstRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" dst-addr \"%s\" ", dstRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }
        // 处理dst-addr any问题
        if(ArrayUtils.isEmpty(dstRefIpAddressObject) && ArrayUtils.isEmpty(dstRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newDstIpAddressObjectNameList)){
            securityPolicyCl.append(" dst-addr any").append(StringUtils.LF);
        }

        // 本次生成的服务对象
        if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
            for (String newServiceObjectName : newServiceObjectNameList) {
                securityPolicyCl.append(String.format(" service \"%s\" %s",newServiceObjectName,StringUtils.LF));
            }
        }
        //引用服务对象
        if(ArrayUtils.isNotEmpty(refServiceObject)){
            for (String serviceObjectName:refServiceObject) {
                securityPolicyCl.append(String.format(" service \"%s\" %s",serviceObjectName,StringUtils.LF));
            }
        }
        //引用服务组对象
        if(ArrayUtils.isNotEmpty(refServiceObjectGroup)){
            for (String serviceObjectGroupName:refServiceObjectGroup) {
                securityPolicyCl.append(String.format(" service \"%s\" %s",serviceObjectGroupName,StringUtils.LF));
            }
        }
        // 处理service any的问题
        if(ArrayUtils.isEmpty(refServiceObject) && ArrayUtils.isEmpty(refServiceObjectGroup) && CollectionUtils.isEmpty(newServiceObjectNameList)){
            securityPolicyCl.append(" service Any").append(StringUtils.LF);
        }


        securityPolicyCl.append("exit").append(StringUtils.LF);

        return securityPolicyCl.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        Object rollbackType = null;
        if(map != null ){
            rollbackType = map.get("rollbackType");
        }
        if(StringUtils.isNotBlank(name) && (rollbackType == null || (Boolean)rollbackType)){
            return String.format("policy-global \n no rule name %s \n",name);
        }else {
            return String.format("policy-global \n no rule id %s \n",id);
        }
    }

    @Override
    public String generatePolicyGroupCommandLine(String policyGroupName,String description,Map<String, Object> map, String[] args){
        if(StringUtils.isBlank(policyGroupName)){
            return StringUtils.LF;
        }
        StringBuilder policyGroupCl = new StringBuilder();
        policyGroupCl.append("\n");
        policyGroupCl.append(String.format("policy-group \"%s\" \n",policyGroupName));
        policyGroupCl.append("exit\n");
        policyGroupCl.append(StringUtils.LF);
        return policyGroupCl.toString();
    }

    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "ip vrouter trust-vr \n";
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("ip route %s %s %s\n",ip, TotemsIpUtils.getMaskMap(String.valueOf(mask)),"null0");
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description,Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("no ip route %s %s %s\n",ip,TotemsIpUtils.getMaskMap(String.valueOf(mask)),"null0");
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("ipv6 route %s/%s %s\n",ip,mask,"null0");
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("no ipv6 route %s/%s %s\n",ip,mask,"null0");
    }

    @Override
    public String generateManageIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                           IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray,
                                                           IpAddressSubnetStrDTO[] subnetStrIpArray, String[] fqdnArray, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();

        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = "no ";
        }
        sb.append("configure\n");
        sb.append(String.format("address %s\n", name));

        if (ArrayUtils.isNotEmpty(rangIpArray)) {
            for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                String startIp = rangeDTO.getStart();
                String endIp = rangeDTO.getEnd();
                sb.append(deletePrefix).append(String.format("range %s %s\n", startIp, endIp));
            }
        }

        if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
            for (IpAddressSubnetIntDTO subnetStrDTO : subnetIntIpArray) {
                sb.append(deletePrefix).append(String.format("ip %s/%s\n",subnetStrDTO.getIp(), subnetStrDTO.getMask()));
            }
        }

        if (ArrayUtils.isNotEmpty(singleIpArray)) {
            for (String ip : singleIpArray) {
                sb.append(deletePrefix).append(String.format("ip %s/32\n", ip));
            }
        }

        if (ArrayUtils.isNotEmpty(fqdnArray)) {
            for (String fqdn : fqdnArray) {
                sb.append(deletePrefix).append(String.format("host %s\n", fqdn));
            }
        }
        sb.append("exit\n");

        sb.append("end\n");

        return sb.toString();

    }

    @Override
    public String generateManageIpAddressGroupObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                                IpAddressRangeDTO[] rangIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                                String[] fqdnArray, String[] objectNameArray, String[] addObjectNameArray, String[] delObjectNameArray, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure\n");
        sb.append(String.format("address %s\n", name));
        //新增地址对象
        if (ObjectUtils.isNotEmpty(addObjectNameArray)) {
            for (String addressAdd : addObjectNameArray) {
                sb.append(String.format("member %s\n", addressAdd));
            }
        }
        //删除地址对象
        if (ObjectUtils.isNotEmpty(delObjectNameArray)) {
            for (String addressDel : delObjectNameArray) {
                sb.append(String.format("no member %s\n", addressDel));
            }
        }

        sb.append("exit\n");
        return "";
    }

}
