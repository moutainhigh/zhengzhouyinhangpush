package com.abtnetworks.totems.vender.h3c.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.activemq.util.TimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: WangCan
 * @Description h3cV7 命令行生成
 * @Date: 2021/5/10
 */
public class SecurityH3cSecPathV7Impl extends OverAllGeneratorAbstractBean {

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuffer preCommandline = new StringBuffer();
        if (isVsys != null && isVsys && StringUtils.isNotBlank(vsysName)) {
            preCommandline.append("system-view\n");
            preCommandline.append("switchto context " + vsysName + "\n");
        }
        preCommandline.append("system-view\n");
        return preCommandline.toString();
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "return\n";
    }

    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return name;
    }

    /**
     * 生成绝对时间对象命令行
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
        String timeNameCommandLine = this.generateTimeObjectName(name, map, null);

        //绝对计划 time-range time-range-name { from time1 date1 [ to time2 date2 ] | to time2 date2 }
        String startDate = absoluteTimeParamDTO.getStartDate();
        String startTime = absoluteTimeParamDTO.getStartTime();
        String start = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(startTime)){
            start = String.format("%s %s",startDate,startTime);
        }

        String endDate = absoluteTimeParamDTO.getEndDate();
        String endTime = absoluteTimeParamDTO.getEndTime();
        String end = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(startTime)){
            end = String.format("%s %s",endDate,endTime);
        }
        if(StringUtils.isNotBlank(start) &&  StringUtils.isNotBlank(end)){
            String startTimeStr = TotemsTimeUtils.transformDateFormat(start, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.H3C_V7_FORMAT);
            String endTimeStr = TotemsTimeUtils.transformDateFormat(end, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.H3C_V7_FORMAT);
            return String.format("time-range %s from %s to %s \n",timeNameCommandLine,startTimeStr,endTimeStr);
        } else if(StringUtils.isNotBlank(start)){
            String startTimeStr = TotemsTimeUtils.transformDateFormat(start, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.H3C_V7_FORMAT);
            return String.format("time-range %s from %s \n",timeNameCommandLine,startTimeStr);
        } else {
            String endTimeStr = TotemsTimeUtils.transformDateFormat(end, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.H3C_V7_FORMAT);
            return String.format("time-range %s to %s \n",timeNameCommandLine,endTimeStr);
        }
    }

    /**
     * 生成周期时间对象命令行
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
        String timeNameCommandLine = this.generateTimeObjectName(name, map, null);
        // 周期计划 time-range time-range-name start-time to end-time days
        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("time-range %s ", timeNameCommandLine));
        String cycleStart = periodicTimeParamDTO.getCycleStart();
        if(StringUtils.isNotBlank(cycleStart)){
            timeCommandLineBuffer.append(cycleStart);
        }
        String cycleEnd = periodicTimeParamDTO.getCycleEnd();
        if(StringUtils.isNotBlank(cycleEnd)){
            timeCommandLineBuffer.append(String.format(" to %s ",cycleEnd));
        }
        String[] cycle = periodicTimeParamDTO.getCycle();
        if(ArrayUtils.isNotEmpty(cycle)){
            for (String date:cycle) {
                timeCommandLineBuffer.append(date).append(StringUtils.SPACE);
            }
        }
        timeCommandLineBuffer.append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }


    /**
     * ip地址对象命令行生成
     * 同一 IP 地址对象可以和多个主机 IP 地址绑定，多次执行 host address 命令即可
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
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (int i = 0; i < singleIpArray.length; i++) {
                ipv4ArrayCommandLine.append(String.format(" undo network host address %s \n",singleIpArray[i]));
            }
        } else {
            for (int i = 0; i < singleIpArray.length; i++) {
                ipv4ArrayCommandLine.append(String.format(" network host address %s \n",singleIpArray[i]));
            }
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum,String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(hosts)){
            return StringUtils.EMPTY;
        }
        StringBuffer hostCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (int i = 0; i < hosts.length; i++) {
                hostCommandLine.append(String.format(" undo network domain %s \n",hosts[i]));
            }
        } else {
            for (int i = 0; i < hosts.length; i++) {
                hostCommandLine.append(String.format(" network domain %s \n",hosts[i]));
            }
        }
        return hostCommandLine.toString();
    }

    /**
     * ip范围地址对象
     * 同一范围地址对象只能和一个范围地址绑定
     * @param rangIpArray
     * @param map
     * @param args
     * @return range ip-address-start ip-address-end
     * @throws Exception
     */
    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer rangeIpCl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (int i = 0; i < rangIpArray.length; i++) {
                rangeIpCl.append(String.format(" undo network range %s %s \n",rangIpArray[i].getStart(),rangIpArray[i].getEnd()));
            }
        } else {
            for (int i = 0; i < rangIpArray.length; i++) {
                rangeIpCl.append(String.format(" network range %s %s \n",rangIpArray[i].getStart(),rangIpArray[i].getEnd()));
            }
        }
        return rangeIpCl.toString();
    }

    /**
     * int子网地址对象
     * 同一子网地址对象只能和一个子网地址绑定
     * @param subnetIpArray ip掩码
     * @param map
     * @param args
     * @return  subnet net-address  wildcard-mask
     * @throws Exception
     */
    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(subnetIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            for (int i = 0; i < subnetIpArray.length; i++) {
                subnetIpv4Cl.append(String.format(" undo network subnet %s %s \n",subnetIpArray[i].getIp(), TotemsIpUtils.getMaskMap(String.valueOf(subnetIpArray[i].getMask()))));
            }
        } else {
            for (int i = 0; i < subnetIpArray.length; i++) {
                subnetIpv4Cl.append(String.format(" network subnet %s %s \n",subnetIpArray[i].getIp(), TotemsIpUtils.getMaskMap(String.valueOf(subnetIpArray[i].getMask()))));
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
     * @return subnet net-address  wildcard-mask
     * @throws Exception
     */
    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(subnetIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (int i = 0; i < subnetIpArray.length; i++) {
                IpAddressSubnetStrDTO ipAddressSubnetStrDTO = subnetIpArray[i];
                int maskBit;
                String wildcardMask;
                if(MaskTypeEnum.wildcard_mask.getType().equalsIgnoreCase(ipAddressSubnetStrDTO.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(ipAddressSubnetStrDTO.getMask());
                } else {
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                }
                wildcardMask = TotemsIpUtils.getMaskMap(String.valueOf(maskBit));
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    subnetIpv4Cl.append(String.format(" undo network subnet %s %s \n",ipAddressSubnetStrDTO.getIp(), wildcardMask));
                } else {
                    subnetIpv4Cl.append(String.format(" network subnet %s %s \n",ipAddressSubnetStrDTO.getIp(), wildcardMask));
                }
            }
        }
        return subnetIpv4Cl.toString();
    }
    /**
     * 单个 ip
     * ABCD:EF01:2345:6789:ABCD:EF01:2345:6789
     * @param singleIpArray
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception{
        return this.generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,map,args);
    }

    /**
     * Ip 范围
     * @param rangIpArray X:X:X:X:X:X:X:X - X:X:X:X:X:X:X:X 例如：ABCD:EF01:2345:6789:ABCD:EF01:2345:6789 - ADCD:EF01:2125:6189:ABCD:EF01:2345:6789
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception{
        return this.generateRangeIpV4CommandLine(statusTypeEnum,rangIpArray,map,args);
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
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception{
        StringBuffer ipv6CommandLine = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (int i = 0; i < subnetIpArray.length; i++) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipv6CommandLine.append(String.format(" undo network subnet %s %s \n",subnetIpArray[i].getIp(), subnetIpArray[i].getMask()));
                } else {
                    ipv6CommandLine.append(String.format(" network subnet %s %s \n",subnetIpArray[i].getIp(),subnetIpArray[i].getMask()));
                }
            }
        }
        return ipv6CommandLine.toString();
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum, String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 创建地址对象
     * @param statusTypeEnum 状态类型
     * @param ipTypeEnum IP枚举类型
     * @param name 地址对象名称
     * @param id 地址对象id
     * @param singleIpArray 单个ip
     * @param rangIpArray 范围ip  同一范围地址对象只能和一个范围地址绑定
     * @param subnetIntIpArray int子网ip 同一子网地址对象只能和一个子网地址绑定
     * @param subnetStrIpArray Str子网ip
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
                                                     String name, String id,
                                                     String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                     IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                     String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray,
                                                     String description, String attachStr, String delStr,
                                                     Map<String, Object> map, String[] args) throws Exception {
        StringBuffer ipAddressCl = new StringBuffer();
        String namePrefix = null;
        if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            namePrefix = "object-group ipv6 address %s \n";
        } else {
            namePrefix = "object-group ip address %s \n";
        }
        ipAddressCl.append(String.format(namePrefix,name));
        // ip地址对象
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            ipAddressCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,null,args));
        }
        // ip范围地址对象
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                ipAddressCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,args));
            }
        }
        // int子网地址对象
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
                    ipAddressCl.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{subnetIntDTO},null,null,args));
                } else {
                    ipAddressCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{subnetIntDTO},null,null,args));
                }
            }
        }
        // Str子网地址对象
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                ipAddressCl.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,null,args));
            }
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            ipAddressCl.append(this.generateHostCommandLine(statusTypeEnum,fqdnArray,map,args));
        }
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName : objectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressCl.append(String.format(" undo network group-object %s \n",objectName));
                } else {
                    ipAddressCl.append(String.format(" network group-object %s \n",objectName));
                }
            }
        }
        ipAddressCl.append("exit").append(StringUtils.LF);
        return ipAddressCl.toString();
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if(ipTypeEnum != null && RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("no object-group ipv6 address %s \n",groupName);
        }
        return String.format("no object-group ip address %s \n",groupName);
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return this.deleteIpAddressObjectGroupCommandLine(ipTypeEnum,delStr,name,map,args);
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("no object-group service %s \n",groupName);
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return this.deleteServiceObjectGroupCommandLine(delStr, attachStr, name, map, args);
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        return String.format("no time-range %s\n",name);
    }



    @Override
    public String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    /**
     * 创建地址组对象
     * @param statusTypeEnum 状态类型
     * @param ipTypeEnum IP枚举类型
     * @param name 地址对象名称
     * @param id 地址对象id
     * @param singleIpArray 单个ip
     * @param rangIpArray 范围ip
     * @param subnetIntIpArray int子网ip
     * @param subnetStrIpArray Str子网ip
     * @param interfaceArray 接口集合
     * @param fqdnArray 域名集合
     * @param objectNameRefArray 引用对象名称集合
     * @param objectGroupNameRefArray 引用对象组名称集合
     * @param description 备注
     * @param attachStr 附加字符串
     * @param delStr 删除，失效标记
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray,IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {
        return this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,name,id,singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,interfaceArray,fqdnArray
                ,ArrayUtils.addAll(objectNameRefArray,objectGroupNameRefArray),description,attachStr,delStr,map,args);
    }

    /**
     * mac地址对象
     * @param statusTypeEnum
     * @param name
     * @param macIpArray mac地址
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateMacAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer macAddressCl = new StringBuffer();
        // mac地址对象
        macAddressCl.append(String.format("object-group mac-address %s \n",name));
        if(ArrayUtils.isNotEmpty(macIpArray)){
            for (IpAddressMacDTO ipAddressMacDTO : macIpArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    macAddressCl.append(String.format(" undo mac %s \n",ipAddressMacDTO.getMacAddress()));
                } else {
                    macAddressCl.append(String.format(" mac %s \n",ipAddressMacDTO.getMacAddress()));
                }
            }
        }
        macAddressCl.append(StringUtils.LF);
        return macAddressCl.toString();
    }

    /**
     * mac
     * @param macIpArray mac 集合
     * @param macObjectNameRefArray 引用mac对象名字 集合
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateMacAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, String[] macObjectNameRefArray,Map<String, Object> map, String[] args) throws Exception {
        StringBuffer macGroupAddressCl = new StringBuffer();
        // mac地址对象
        macGroupAddressCl.append(String.format("object-group mac %s \n",name));
        if(ArrayUtils.isNotEmpty(macIpArray)){
            for (IpAddressMacDTO ipAddressMacDTO : macIpArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    macGroupAddressCl.append(String.format(" undo mac %s \n",ipAddressMacDTO.getMacAddress()));
                } else {
                    macGroupAddressCl.append(String.format(" mac %s \n",ipAddressMacDTO.getMacAddress()));
                }
            }

        }
        if(ArrayUtils.isNotEmpty(macObjectNameRefArray)){
            for (String macObjectName :macObjectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    macGroupAddressCl.append(String.format(" undo mac group-object %s \n",macObjectName));
                } else {
                    macGroupAddressCl.append(String.format(" mac group-object %s \n",macObjectName));
                }
            }
        }
        macGroupAddressCl.append(StringUtils.LF);
        return macGroupAddressCl.toString();
    }

    /**
     * icmp 服务对象
     * @param statusTypeEnum 状态类型枚举
     * @param protocolAttachTypeArray 协议附件type值
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
     * @return service icmp icmp-type icmp-code
     * @throws Exception
     */
    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                               Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                               Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                               String[] timeOutArray, String[] objectNameRefArray,
                               Map<String, Object> map, String[] args) throws Exception {
        String type = StringUtils.EMPTY;
        String code = StringUtils.EMPTY;
        if(ArrayUtils.isNotEmpty(protocolAttachTypeArray)){
            type = protocolAttachTypeArray[0];
        }
        if(ArrayUtils.isNotEmpty(protocolAttachCodeArray)){
            code = protocolAttachCodeArray[0];
        }
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            return String.format(" undo service icmp %s %s \n",type,code);
        } else {
            return String.format(" service icmp %s %s \n",type,code);
        }
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                           Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(protocolAttachTypeArray) || ArrayUtils.isEmpty(protocolAttachCodeArray)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            return String.format(" undo service icmpv6 %s %s \n",protocolAttachTypeArray[0],protocolAttachCodeArray[0]);
        } else {
            return String.format(" service icmpv6 %s %s \n",protocolAttachTypeArray[0],protocolAttachCodeArray[0]);
        }
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
     * @return
     * [ object-id ] service
     * { protocol [ {
     * source { { eq | lt | gt } port | range port1 port2 } |
     * destination { { eq | lt | gt | } port | range port1 port2 } } * |
     * icmp-type icmp-code |
     * icmpv6-type icmpv6-code ] |
     * group-object object-group-name }
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
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            srcPortBuffer.append(String.format(" eq %s ",srcSinglePortArray[0]));
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format(" range %s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(String.format(" eq %s ",dstSinglePortArray[0]));
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format(" range %s %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        }
        StringBuffer tcpCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            tcpCommandLine.append(" undo service tcp  ");
        } else {
            tcpCommandLine.append(" service tcp  ");
        }

        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            tcpCommandLine.append(String.format(" source %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            tcpCommandLine.append(String.format(" destination %s ",dstPortBuffer.toString()));
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
     * @return service tcp  [ source-port source-port-start [ source-port-end ] ] [ destination-port destination-port-start [ destination-port-end ] ]
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
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            srcPortBuffer.append(String.format(" eq %s ",srcSinglePortArray[0]));
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format(" range %s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(String.format(" eq %s ",dstSinglePortArray[0]));
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format(" range %s %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        }
        StringBuffer udpCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            udpCommandLine.append(" undo service udp  ");
        } else {
            udpCommandLine.append(" service udp  ");
        }
        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            udpCommandLine.append(String.format("source %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            udpCommandLine.append(String.format("destination %s ",dstPortBuffer.toString()));
        }
        udpCommandLine.append(StringUtils.LF);
        return udpCommandLine.toString();
    }

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
     * @return
     * @throws Exception
     */
    @Override
    public String generateOtherCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                           Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            return " undo service protocol \n";
        } else {
            return " service protocol \n";
        }
    }


    @Override
    public String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 生成服务对象命令行(会生成多个服务对象)
     * @param name 服务对象名称
     * @param id 服务对象id
     * @param attachStr 附加字符串
     * @param serviceParamDTOList
     * @param description
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
        if(CollectionUtils.isEmpty(serviceParamDTOList) && MapUtils.isEmpty(map)){
            return String.format("object-group service %s \n exit\n ",name);
        }
        serviceObjectCl.append(String.format("object-group service %s \n",this.generateServiceObjectName(name,null,null)));
        if(StringUtils.isNotBlank(description)){
            serviceObjectCl.append(String.format(" description %s \n",description));
        }
        for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
            if(ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateTCPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,args));
            } else if(ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateUDPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,args));
            } else if(ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateICMPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,args));
            } else {
                serviceObjectCl.append(this.generateOtherCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(),null,null,args));
            }
        }
        if(MapUtils.isNotEmpty(map)){
            if (map.get("serviceObjectNameRefArray") != null){
                String[] serviceObjectNameRefArray = (String[]) map.get("serviceObjectNameRefArray");
                for (String serviceObjectName:serviceObjectNameRefArray) {
                    if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                        serviceObjectCl.append(String.format(" undo service group-object %s \n",serviceObjectName));
                    } else {
                        serviceObjectCl.append(String.format(" service group-object %s \n",serviceObjectName));
                    }
                }
            }
            if (map.get("serviceObjectGroupNameRefArray") != null){
                String[] serviceObjectGroupNameRefArray = (String[]) map.get("serviceObjectGroupNameRefArray");
                for (String serviceObjectGroupName:serviceObjectGroupNameRefArray) {
                    if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                        serviceObjectCl.append(String.format(" undo service group-object %s \n",serviceObjectGroupName));
                    } else {
                        serviceObjectCl.append(String.format(" service group-object %s \n",serviceObjectGroupName));
                    }
                }
            }
        }
        serviceObjectCl.append("exit").append(StringUtils.LF);
        return serviceObjectCl.toString();
    }

    /**
     * 服务组对象名
     * @param groupName
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    /**
     * 服务组对象
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
        if(map == null){
            map = new HashMap<>();
        }
        map.put("serviceObjectNameRefArray",serviceObjectNameRefArray);
        map.put("serviceObjectGroupNameRefArray",serviceObjectGroupNameRefArray);
        return this.generateServiceObjectCommandLine(statusTypeEnum,name,id,attachStr,serviceParamDTOList,description,map,args);
    }

    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 生成安全策略
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
     * rule [ rule-id ]
     * { drop | pass | inspect app-profile-name }
     * [
     * [ source-ip { object-group-name | any } ]
     * [ destination-ip { object-group-name | any } ]
     * [ service { object-group-name | any } ]
     * [vrf vrf-name ]
     * [ application application-name ]
     * [ app-group app-group-name ]
     * [ counting ]
     * [ disable ]
     * [ logging ]
     * [ track [ negative ] track-entry-number ]
     * [ time-range time-range-name ]
     * ] *
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
        //处理时间对象(是允许指定一个时间对象)
        String newTimeObjectName = null;
        String newTimeCommandLine = null;
        if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName,null,absoluteTimeParamDTO,map,null);
        } else if(ObjectUtils.isNotEmpty(periodicTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(newTimeObjectName,null,periodicTimeParamDTO,map,null);
        }

        StringBuffer securityPolicyCl = new StringBuffer();
        // 处理地址对象
        List<String> newSrcIpAddressObjectNameList = new ArrayList<>();
        if(ObjectUtils.isNotEmpty(srcIpDto)){
            //生成src地址对象命令行
            if(ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())){
                String singleIpArrayName = this.createIpAddressObjectNameBySingleIpArray(srcIpDto.getSingleIpArray(),map,args);
                newSrcIpAddressObjectNameList.add(singleIpArrayName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),singleIpArrayName,null,srcIpDto.getSingleIpArray(),null,null,null,
                        null,null,null,null,null,null,map,args));
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())){
                for (IpAddressRangeDTO ipAddressRangeDTO : srcIpDto.getRangIpArray()) {
                    String ipRangeObjectName = this.createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
                    newSrcIpAddressObjectNameList.add(ipRangeObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),ipRangeObjectName,null,null,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,null,
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())){
                for (IpAddressSubnetIntDTO subnetIntDTO : srcIpDto.getSubnetIntIpArray()) {
                    String ipMaskObjectName = this.createIpAddressObjectNameByIpMask(subnetIntDTO.getIp(), subnetIntDTO.getMask(), map, args);
                    newSrcIpAddressObjectNameList.add(ipMaskObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),ipMaskObjectName,null,null,null,new IpAddressSubnetIntDTO[]{subnetIntDTO},null,
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())){
                for (IpAddressSubnetStrDTO subnetStrDTO : srcIpDto.getSubnetStrIpArray()) {
                    String ipMaskObjectName = this.createIpAddressObjectNameByIpMask(subnetStrDTO.getIp(), TotemsIpUtils.getMaskBit(subnetStrDTO.getMask()), map, args);
                    newSrcIpAddressObjectNameList.add(ipMaskObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),ipMaskObjectName,null,null,null,null, new IpAddressSubnetStrDTO[]{subnetStrDTO},
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getHosts())){
                String hostArrayName = this.createIpAddressObjectNameBySingleIpArray(srcIpDto.getHosts(),map,args);
                newSrcIpAddressObjectNameList.add(hostArrayName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),hostArrayName,null,srcIpDto.getSingleIpArray(),null,null,null,
                        null,srcIpDto.getHosts(),null,null,null,null,map,args));
            }
        }

        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        //生成dst地址对象命令行
        if(ObjectUtils.isNotEmpty(dstIpDto)){
            //生成src地址对象命令行
            if(ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray())){
                String singleIpArrayName = this.createIpAddressObjectNameBySingleIpArray(dstIpDto.getSingleIpArray(),map,args);
                newDstIpAddressObjectNameList.add(singleIpArrayName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),singleIpArrayName,null,dstIpDto.getSingleIpArray(),null,null,null,
                        null,null,null,null,null,null,map,args));
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray())){
                for (IpAddressRangeDTO ipAddressRangeDTO : dstIpDto.getRangIpArray()) {
                    String ipRangeObjectName = this.createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
                    newDstIpAddressObjectNameList.add(ipRangeObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),ipRangeObjectName,null,null,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,null,
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray())){
                for (IpAddressSubnetIntDTO subnetIntDTO : dstIpDto.getSubnetIntIpArray()) {
                    String ipMaskObjectName = this.createIpAddressObjectNameByIpMask(subnetIntDTO.getIp(), subnetIntDTO.getMask(), map, args);
                    newDstIpAddressObjectNameList.add(ipMaskObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),ipMaskObjectName,null,null,null,new IpAddressSubnetIntDTO[]{subnetIntDTO},null,
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray())){
                for (IpAddressSubnetStrDTO subnetStrDTO : dstIpDto.getSubnetStrIpArray()) {
                    String ipMaskObjectName = this.createIpAddressObjectNameByIpMask(subnetStrDTO.getIp(), TotemsIpUtils.getMaskBit(subnetStrDTO.getMask()), map, args);
                    newDstIpAddressObjectNameList.add(ipMaskObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),ipMaskObjectName,null,null,null,null, new IpAddressSubnetStrDTO[]{subnetStrDTO},
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getHosts())){
                String hostArrayName = this.createIpAddressObjectNameBySingleIpArray(dstIpDto.getHosts(),map,args);
                newDstIpAddressObjectNameList.add(hostArrayName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),hostArrayName,null,dstIpDto.getSingleIpArray(),null,null,null,
                        null,dstIpDto.getHosts(),null,null,null,null,map,args));
            }
        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(serviceParam)){
            for (ServiceParamDTO serviceParamDTO:serviceParam) {
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> serviceList = new ArrayList<>();
                serviceList.add(serviceParamDTO);
                securityPolicyCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, serviceList, null, map,args));
            }
        }

        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        securityPolicyCl.append(StringUtils.LF);
        securityPolicyCl.append("security-policy ip \n");
        securityPolicyCl.append("rule ");
        if(StringUtils.isNotBlank(id)){
            securityPolicyCl.append(id);
        }
        if(StringUtils.isNotBlank(name)){
            securityPolicyCl.append(String.format("name %s \n",name));
        }
        if(StringUtils.isNotBlank(description)){
            securityPolicyCl.append(String.format(" description %s \n",description));
        }
        if(StringUtils.isNotBlank(action)){
            if("permit".equalsIgnoreCase(action)){
                securityPolicyCl.append(String.format(" action %s \n","pass"));
            } else {
                securityPolicyCl.append(String.format(" action %s \n","drop"));
            }
        }
        securityPolicyCl.append(" logging enable \n");
        securityPolicyCl.append(" counting enable \n");
        if(StringUtils.isNotBlank(newTimeObjectName)){
            securityPolicyCl.append(String.format(" time-range %s \n",newTimeObjectName));
        } else if(ArrayUtils.isNotEmpty(refTimeObject)){
            // 只能使用一个时间对象
            securityPolicyCl.append(String.format(" time-range %s \n",refTimeObject[0]));
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
        if(StringUtils.isNotBlank(srcZoneName)){
            securityPolicyCl.append(String.format(" source-zone %s \n",srcZoneName));
        }
        if(StringUtils.isNotBlank(dstZoneName)){
            securityPolicyCl.append(String.format(" destination-zone %s \n",dstZoneName));
        }

        // 本次生成的源地址
        if(CollectionUtils.isNotEmpty(newSrcIpAddressObjectNameList)){
            for(String addressObjectName:newSrcIpAddressObjectNameList){
                securityPolicyCl.append(String.format(" source-ip %s \n", addressObjectName));
            }
        }
        //引用源地址对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            for (String srcRefIpAddressObjectName:srcRefIpAddressObject) {
                securityPolicyCl.append(String.format(" source-ip %s ", srcRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        //引用源地址组对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            for (String srcRefIpAddressObjectGroupName:srcRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" source-ip %s ", srcRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }

        //本次生成目的地址
        if(CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)){

            for(String addressObjectName:newDstIpAddressObjectNameList){
                securityPolicyCl.append(String.format(" destination-ip %s \n", addressObjectName));
            }
        }
        // 引用目的地址对象
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            for (String dstRefIpAddressObjectName:dstRefIpAddressObject) {
                securityPolicyCl.append(String.format(" destination-ip %s ", dstRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        // 引用目的地址组对象
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){

            for (String dstRefIpAddressObjectGroupName:dstRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" destination-ip %s ", dstRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }


        //本次生成的服务对象
        if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
            for(String serviceObjectName:newServiceObjectNameList){
                securityPolicyCl.append(String.format(" service %s \n", serviceObjectName));
            }
        }
        // 引用服务对象
        if(ArrayUtils.isNotEmpty(refServiceObject)){

            for (String serviceObjectName:refServiceObject) {
                securityPolicyCl.append(String.format(" service %s %s",serviceObjectName,StringUtils.LF));
            }
        }
        //引用服务组对象
        if(ArrayUtils.isNotEmpty(refServiceObjectGroup)){
            for (String serviceObjectGroupName:refServiceObjectGroup) {
                securityPolicyCl.append(String.format(" service %s %s",serviceObjectGroupName,StringUtils.LF));
            }
        }

        securityPolicyCl.append("exit");
        securityPolicyCl.append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        StringBuffer deleteStr = new StringBuffer();
        if(RuleIPTypeEnum.IP6.name().equalsIgnoreCase(ipTypeEnum.name())){
            deleteStr.append("security-policy ipv6\n");
        }else{
            deleteStr.append("security-policy ip\n");
        }
        if(StringUtils.isNotBlank(name)){
            deleteStr.append(String.format(" undo rule name %s \n",name));
        } else if(StringUtils.isNotBlank(id)){
            return String.format(" undo rule %s \n",id);
        }
        deleteStr.append("quit\n");
        return deleteStr.toString();
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("ip route-static  %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description,Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("undo ip route-static %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("ipv6 route-static %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("undo ipv6 route %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String generateManageIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                           IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                           String[] fqdnArray, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();

        if (StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

            sb.append("object-group ip address ");
            sb.append(name).append("\n");
            if (ArrayUtils.isNotEmpty(rangIpArray)) {
                for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                    String startIp = rangeDTO.getStart();
                    String endIp = rangeDTO.getEnd();
                    sb.append(String.format("network range %s %s\n", startIp, endIp));
                }
            }

            if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                for (IpAddressSubnetIntDTO subnetStrDTO : subnetIntIpArray) {
                    String ip = subnetStrDTO.getIp();
                    int maskBit = subnetStrDTO.getMask();
                    sb.append(String.format("network subnet %s %s\n", ip, maskBit));
                }
            }

            if (ArrayUtils.isNotEmpty(singleIpArray)) {
                for (String ip : singleIpArray) {
                    sb.append(String.format("network host address %s\n", ip));
                }
            }

            if (ArrayUtils.isNotEmpty(fqdnArray)) {
                for (String fqdn : fqdnArray) {
                    sb.append(String.format("network host name %s\n", fqdn));
                }
            }
        } else if (StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            sb.append("object-group ip address ");
            sb.append(name).append("\n");
            if (ArrayUtils.isNotEmpty(rangIpArray)) {
                for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                    String startIp = rangeDTO.getStart();
                    String endIp = rangeDTO.getEnd();
                    sb.append(String.format("undo network range %s %s\n", startIp, endIp));
                }
            }

            if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                    String ip = subnetStrDTO.getIp();
                    String maskBit = subnetStrDTO.getMask();
                    sb.append(String.format("undo network subnet %s %s\n", ip, maskBit));
                }
            }

            if (ArrayUtils.isNotEmpty(singleIpArray)) {
                for (String ip : singleIpArray) {
                    sb.append(String.format("undo network host address %s\n", ip));
                }
            }

            if (ArrayUtils.isNotEmpty(fqdnArray)) {
                for (String fqdn : fqdnArray) {
                    sb.append(String.format("undo network host address %s\n", fqdn));
                }
            }
        }

        sb.append("quit\n");
        return sb.toString();
    }

    @Override
    public String generateManageIpAddressGroupObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                                IpAddressRangeDTO[] rangIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                                String[] fqdnArray, String[] objectNameArray, String[] addObjectNameArray, String[] delObjectNameArray, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("object-group ip address ").append(name).append("\n");
        if(ArrayUtils.isNotEmpty(addObjectNameArray)){
            for (String addr : addObjectNameArray) {
                sb.append(String.format("network group-object %s\n",addr));
            }
        }
        if(ArrayUtils.isNotEmpty(delObjectNameArray)){
            for (String addr : delObjectNameArray) {
                sb.append(String.format("undo network group-object %s\n",addr));
            }
        }
        sb.append("quit\n");
        return sb.toString();
    }

}
