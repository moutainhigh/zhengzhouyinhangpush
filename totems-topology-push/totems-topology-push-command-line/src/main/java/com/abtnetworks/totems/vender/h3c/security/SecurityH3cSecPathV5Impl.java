package com.abtnetworks.totems.vender.h3c.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: WangCan
 * @Description h3cV5 命令行生成
 * @Date: 2021/4/27
 */
public class SecurityH3cSecPathV5Impl extends OverAllGeneratorAbstractBean {

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "system-view\n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "\n";
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
        for (String ip:singleIpArray) {
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                ipv4ArrayCommandLine.append(String.format(" undo host address %s \n",ip));
            }else{
                ipv4ArrayCommandLine.append(String.format(" host address %s \n",ip));
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
        for (String host:hosts) {
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                hostCommandLine.append(String.format(" undo host name %s \n",host));
            } else {
                hostCommandLine.append(String.format(" host name %s \n",host));
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
        for (IpAddressRangeDTO ipAddressRangeDTO:rangIpArray) {
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                rangeIpCl.append(String.format(" undo range %s %s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
            } else {
                rangeIpCl.append(String.format(" range %s %s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
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
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    subnetIpv4Cl.append(String.format(" undo subnet %s %s \n",ipAddressSubnetIntDTO.getIp(), TotemsIpUtils.getWildcardMaskMap(ipAddressSubnetIntDTO.getMask())));
                } else {
                    subnetIpv4Cl.append(String.format(" subnet %s %s \n",ipAddressSubnetIntDTO.getIp(), TotemsIpUtils.getWildcardMaskMap(ipAddressSubnetIntDTO.getMask())));
                }
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
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetIpArray) {
                int maskBit;
                String wildcardMask;
                if(MaskTypeEnum.mask.getType().equalsIgnoreCase(ipAddressSubnetStrDTO.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                    wildcardMask = TotemsIpUtils.getWildcardMaskMap(maskBit);
                } else {
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(ipAddressSubnetStrDTO.getMask());
                    wildcardMask = ipAddressSubnetStrDTO.getMask();
                }
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    subnetIpv4Cl.append(String.format(" undo subnet %s %s \n",ipAddressSubnetStrDTO.getIp(), wildcardMask));
                } else {
                    subnetIpv4Cl.append(String.format(" subnet %s %s \n",ipAddressSubnetStrDTO.getIp(), wildcardMask));
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
        return null;
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
        return null;
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
        return null;
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
        // 范围ip地址,子网ip地址,只要个数大于一,则生成对象组
        if(StringUtils.isNotBlank(name)
                && ((ArrayUtils.getLength(rangIpArray)+ArrayUtils.getLength(subnetIntIpArray)+ArrayUtils.getLength(subnetStrIpArray) > 1)
                || (ArrayUtils.isNotEmpty(singleIpArray) && (ArrayUtils.getLength(rangIpArray) + ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) + ArrayUtils.getLength(fqdnArray)  >=1)))){
            return this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,name,id,singleIpArray,rangIpArray,subnetIntIpArray,
                    subnetStrIpArray,interfaceArray,fqdnArray,objectNameRefArray,null,description,attachStr,delStr,map,args);
        }
        if(StringUtils.isNotBlank(name) && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray)
        && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) ){
            if(ArrayUtils.isNotEmpty(objectNameRefArray)){
                return generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,name,id,singleIpArray,rangIpArray,subnetIntIpArray,
                        subnetStrIpArray,interfaceArray,fqdnArray,objectNameRefArray,null,description,attachStr,delStr,map,args);
            }
            return String.format("object network host %s \n",name);
        }
        StringBuffer ipAddressCl = new StringBuffer();
        // ip地址对象
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            ipAddressCl.append(String.format("object network host %s \n", name));
            ipAddressCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,null,null));
        } else if(ArrayUtils.isNotEmpty(rangIpArray)){
            // ip范围地址对象
            ipAddressCl.append(String.format("object network range %s \n", name));
            ipAddressCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{rangIpArray[0]},null,null));
        }else if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            ipAddressCl.append(String.format("object network subnet %s \n",name));
            ipAddressCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{subnetIntIpArray[0]},null,null,null));

        } else if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            // Str子网地址对象
            ipAddressCl.append(String.format("object network subnet %s \n", name));
            ipAddressCl.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{subnetStrIpArray[0]},null,null,null));
            ipAddressCl.append(StringUtils.LF);
        } else if(ArrayUtils.isNotEmpty(fqdnArray)){
            ipAddressCl.append(String.format("object network host %s \n", name));
            ipAddressCl.append(this.generateHostCommandLine(statusTypeEnum,new String[]{fqdnArray[0]},null,null));
        }
        ipAddressCl.append("quit").append(StringUtils.LF);
        return ipAddressCl.toString();
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
        StringBuffer ipAddressGroupCl = new StringBuffer();
        // 生成地址对象命令行
        List<String> newIpAddressObjectNames = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            String singleIpArrayObjectName = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
            newIpAddressObjectNames.add(singleIpArrayObjectName);
            ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,singleIpArrayObjectName,null,singleIpArray,null,null,null,null,null,null,null,null,null,map,args));
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                String ipRangeObjectName = createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
                newIpAddressObjectNames.add(ipRangeObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,ipRangeObjectName,null,null,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,null,null,null,null,null,null,null,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                String ipMaskObjectName = createIpAddressObjectNameByIpMask(subnetIntDTO.getIp(), subnetIntDTO.getMask(), map, args);
                newIpAddressObjectNames.add(ipMaskObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,ipMaskObjectName,null,null,null,new IpAddressSubnetIntDTO[]{subnetIntDTO},null,null,null,null,null,null,null,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                int maskBit = TotemsIpUtils.getMaskBit(subnetStrDTO.getMask());
                String ipMaskObjectName = createIpAddressObjectNameByIpMask(subnetStrDTO.getIp(), maskBit, map, args);
                newIpAddressObjectNames.add(ipMaskObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,ipMaskObjectName,null,null,null,null,new IpAddressSubnetStrDTO[]{subnetStrDTO},null,null,null,null,null,null,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            String fqdnArrayObjectName = createIpAddressObjectNameBySingleIpArray(fqdnArray, map, args);
            newIpAddressObjectNames.add(fqdnArrayObjectName);
            ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,fqdnArrayObjectName,null,null,null,null,null,null,fqdnArray,null,null,null,null,map,args));
        }

        ipAddressGroupCl.append(String.format("object-group network %s \n",this.generateIpAddressObjectGroupName(ipTypeEnum,name,null,null)));
        if(StringUtils.isNotBlank(description)){
            ipAddressGroupCl.append(String.format(" description %s \n",description));
        }
        // 本次生成的地址对象
        if(CollectionUtils.isNotEmpty(newIpAddressObjectNames)){
            for (String objectName:newIpAddressObjectNames) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" undo network-object %s \n",objectName));
                }else{
                    ipAddressGroupCl.append(String.format(" network-object %s \n",objectName));
                }
            }
        }
        //引用地址对象
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName:objectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" undo network-object %s \n",objectName));
                }else{
                    ipAddressGroupCl.append(String.format(" network-object %s \n",objectName));
                }
            }
        }
        //引用地址组对象
        if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
            for (String objectGroupName:objectGroupNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" undo network-object %s \n",objectGroupName));
                }else{
                    ipAddressGroupCl.append(String.format(" network-object %s \n",objectGroupName));
                }
            }
        }
        ipAddressGroupCl.append(StringUtils.LF);
        return ipAddressGroupCl.toString();
    }

    /**
     * mac
     * @param macIpArray mac 集合
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateMacAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer macAddressCl = new StringBuffer();
        // mac地址对象
        macAddressCl.append(String.format("object mac mac_%s \n",name));
        if(ArrayUtils.isNotEmpty(macIpArray)){
            for (IpAddressMacDTO ipAddressMacDTO : macIpArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    macAddressCl.append(String.format(" undo mac-address %s \n",ipAddressMacDTO.getMacAddress()));
                } else {
                    macAddressCl.append(String.format(" mac-address %s \n",ipAddressMacDTO.getMacAddress()));
                }
            }
        }
        macAddressCl.append(StringUtils.LF);
        return macAddressCl.toString();
    }

    /**
     * mac地址组对象
     * @param macIpArray
     * @param macObjectNameRefArray
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateMacAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, String[] macObjectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer macGroupAddressCl = new StringBuffer();
        // mac地址对象
        macGroupAddressCl.append(String.format("object-group mac %s \n",name));
        if(ArrayUtils.isNotEmpty(macIpArray)){
            String macIpAddressNameByArray = this.createMacIpAddressNameByArray(macIpArray, map, args);
            this.generateMacAddressObjectCommandLine(statusTypeEnum,macIpAddressNameByArray,macIpArray,map,args);
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                macGroupAddressCl.append(String.format(" undo mac-object mac_%s \n",macIpAddressNameByArray));
            } else {
                macGroupAddressCl.append(String.format(" mac-object mac_%s \n",macIpAddressNameByArray));
            }
        }
        if(ArrayUtils.isNotEmpty(macObjectNameRefArray)){
            for (String macObjectName :macObjectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    macGroupAddressCl.append(String.format(" undo mac-object mac_%s \n",macObjectName));
                } else {
                    macGroupAddressCl.append(String.format(" mac-object mac_%s \n",macObjectName));
                }
            }
        }
        macGroupAddressCl.append(StringUtils.LF);
        return macGroupAddressCl.toString();
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("undo object-group network %s \n",groupName);
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("undo object network %s %s \n",delStr,name);
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

        if(ArrayUtils.isEmpty(protocolAttachTypeArray) || ArrayUtils.isEmpty(protocolAttachCodeArray)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            return String.format(" undo service icmp %s %s \n",protocolAttachTypeArray[0],protocolAttachCodeArray[0]);
        } else {
            return String.format(" service icmp %s %s \n",protocolAttachTypeArray[0],protocolAttachCodeArray[0]);
        }
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
     * @return service tcp  [ source-port source-port-start [ source-port-end ] ] [ destination-port destination-port-start [ destination-port-end ] ]
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
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format("%s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        }
        StringBuffer tcpCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            tcpCommandLine.append(" no");
        }
        tcpCommandLine.append(" service tcp  ");
        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            tcpCommandLine.append(String.format("source-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            tcpCommandLine.append(String.format("destination-port %s ",dstPortBuffer.toString()));
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
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format("%s %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        }
        StringBuffer udpCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            udpCommandLine.append(" no");
        }
        udpCommandLine.append(" service udp  ");
        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            udpCommandLine.append(String.format("source-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            udpCommandLine.append(String.format("destination-port %s ",dstPortBuffer.toString()));
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
            return " no service protocol \n";
        }
        return " service protocol \n";
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
        if(CollectionUtils.isEmpty(serviceParamDTOList)){
            return String.format("object service %s \n \n",name);
        }
        if(CollectionUtils.isNotEmpty(serviceParamDTOList) && serviceParamDTOList.size() > 1){
            return this.generateServiceObjectGroupCommandLine(statusTypeEnum,name,id,attachStr,serviceParamDTOList,description,null,null,map,args);
        }
        serviceObjectCl.append(String.format("object service %s \n",this.generateServiceObjectName(name,null,null)));
        if(StringUtils.isNotBlank(description)){
            serviceObjectCl.append(String.format(" description %s \n",description));
        }
        ServiceParamDTO serviceParamDTO = serviceParamDTOList.get(0);
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
        serviceObjectCl.append("quit").append(StringUtils.LF);
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
        //生成服务对象
        List<String> serviceObjectNameList = new ArrayList<String>();
        StringBuffer serviceObjectGroupCl = new StringBuffer();
        if(CollectionUtils.isNotEmpty(serviceParamDTOList)){
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                serviceObjectNameList.add(serviceObjectName);
                List<ServiceParamDTO> newServiceList = new ArrayList<>();
                newServiceList.add(serviceParamDTO);
                serviceObjectGroupCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newServiceList, null, null, null));
            }
        }
        serviceObjectGroupCl.append(String.format("object-group service %s \n",this.generateServiceObjectGroupName(name, null, null)));
        if(CollectionUtils.isNotEmpty(serviceObjectNameList)){
            for (String serviceObjectName:serviceObjectNameList) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    serviceObjectGroupCl.append(" no");
                }
                serviceObjectGroupCl.append(String.format(" service-object %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for (String serviceObjectName:serviceObjectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    serviceObjectGroupCl.append(" no");
                }
                serviceObjectGroupCl.append(String.format(" service-object %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            for (String serviceObjectGroupName:serviceObjectGroupNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    serviceObjectGroupCl.append(" no");
                }
                serviceObjectGroupCl.append(String.format(" service-object %s \n",serviceObjectGroupName));
            }
        }
        serviceObjectGroupCl.append("quit").append(StringUtils.LF);
        return serviceObjectGroupCl.toString();
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format(" undo object-group service %s \n",groupName);
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format(" undo object-group service %s \n",name);
    }

    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

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
        //处理时间对象(只允许指定一个时间对象)
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
        //生成src地址对象命令行
        if(ObjectUtils.isNotEmpty(srcIpDto)){
            if(ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())){
                String singleIpArrayObjectName = this.createIpAddressObjectNameBySingleIpArray(srcIpDto.getSingleIpArray(),map,args);
                newSrcIpAddressObjectNameList.add(singleIpArrayObjectName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),singleIpArrayObjectName,null,srcIpDto.getSingleIpArray(),null,null,null,
                        null,null,null,null,null,null,map,args));
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())){
                for (IpAddressRangeDTO ipAddressRangeDTO : srcIpDto.getRangIpArray()) {
                    String rangeIpObjectName = this.createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd(),map,args);
                    newSrcIpAddressObjectNameList.add(rangeIpObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),rangeIpObjectName,null,null,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,null,
                            null,null,null,null,null,null,map,args));
                }

            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())){
                for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : srcIpDto.getSubnetIntIpArray()) {
                    String subnetIntIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(),ipAddressSubnetIntDTO.getMask(),map,args);
                    newSrcIpAddressObjectNameList.add(subnetIntIpObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),subnetIntIpObjectName,null,null,null,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())){
                for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : srcIpDto.getSubnetStrIpArray()) {
                    String subnetStrIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetStrDTO.getIp(),TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()),map,args);
                    newSrcIpAddressObjectNameList.add(subnetStrIpObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),subnetStrIpObjectName,null,null,null,null,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(srcIpDto.getHosts())){
                String hostArrayObjectName = this.createIpAddressObjectNameBySingleIpArray(srcIpDto.getHosts(),map,args);
                newSrcIpAddressObjectNameList.add(hostArrayObjectName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),hostArrayObjectName,null,null,null,null,null,
                        null,srcIpDto.getHosts(),null,null,null,null,map,args));
            }
        }

        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        //生成dst地址对象命令行
        if(ObjectUtils.isNotEmpty(dstIpDto)){
            if(ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray())){
                String singleIpArrayObjectName = this.createIpAddressObjectNameBySingleIpArray(dstIpDto.getSingleIpArray(),map,args);
                newDstIpAddressObjectNameList.add(singleIpArrayObjectName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),singleIpArrayObjectName,null,dstIpDto.getSingleIpArray(),null,null,null,
                        null,null,null,null,null,null,map,args));
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray())){
                for (IpAddressRangeDTO ipAddressRangeDTO : dstIpDto.getRangIpArray()) {
                    String rangeIpObjectName = this.createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd(),map,args);
                    newDstIpAddressObjectNameList.add(rangeIpObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),rangeIpObjectName,null,null,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,null,
                            null,null,null,null,null,null,map,args));
                }

            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray())){
                for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : dstIpDto.getSubnetIntIpArray()) {
                    String subnetIntIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(),ipAddressSubnetIntDTO.getMask(),map,args);
                    newDstIpAddressObjectNameList.add(subnetIntIpObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),subnetIntIpObjectName,null,null,null,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray())){
                for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : dstIpDto.getSubnetStrIpArray()) {
                    String subnetStrIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetStrDTO.getIp(),TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()),map,args);
                    newDstIpAddressObjectNameList.add(subnetStrIpObjectName);
                    securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),subnetStrIpObjectName,null,null,null,null,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},
                            null,null,null,null,null,null,map,args));
                }
            }
            if(ArrayUtils.isNotEmpty(dstIpDto.getHosts())){
                String hostArrayObjectName = this.createIpAddressObjectNameBySingleIpArray(dstIpDto.getHosts(),map,args);
                newDstIpAddressObjectNameList.add(hostArrayObjectName);
                securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),hostArrayObjectName,null,null,null,null,null,
                        null,dstIpDto.getHosts(),null,null,null,null,map,args));
            }
        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(serviceParam)){
            for (ServiceParamDTO serviceParamDTO:serviceParam) {
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                List<ServiceParamDTO> newServiceList = new ArrayList<>();
                newServiceList.add(serviceParamDTO);
                securityPolicyCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newServiceList, null, map,args));
            }
        }

        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        securityPolicyCl.append(StringUtils.LF);

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
        // 策略命令行
        //interzone souce souce-zone-name destination destination-zone-nam
        securityPolicyCl.append("interzone ");
        if(StringUtils.isNotBlank(srcZoneName)){
            securityPolicyCl.append(String.format("source %s ",srcZoneName));
        } else {
            securityPolicyCl.append("source any ");
        }
        if(StringUtils.isNotBlank(dstZoneName)){
            securityPolicyCl.append(String.format("destination %s ",dstZoneName));
        } else {
            securityPolicyCl.append("destination any ");
        }
        securityPolicyCl.append(StringUtils.LF);
        //rule [ rule-id ] { deny | permit } [ content-filter policy-template-name | logging | time-range time-range-name ]
        securityPolicyCl.append("rule ");
        if(StringUtils.isNotBlank(id)){
            securityPolicyCl.append(id);
        }
//        securityPolicyCl.append(String.format(" %s \n",action));
        if (StringUtils.equals(action, "permit")) {
            securityPolicyCl.append(" permit \n");
        } else if (StringUtils.equalsIgnoreCase(action, "deny")){
            securityPolicyCl.append(" deny \n");
        }

        if(StringUtils.isNotBlank(description)){
            securityPolicyCl.append(String.format(" comment %s \n",description));
        }

        if(StringUtils.isNotBlank(newTimeObjectName)){
            securityPolicyCl.append(String.format(" time-range %s \n",newTimeObjectName));
        } else if(ArrayUtils.isNotEmpty(refTimeObject)){
            // 只能使用一个时间对象
            securityPolicyCl.append(String.format(" time-range %s \n",refTimeObject[0]));
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
        // 处理src-addr any问题
        if(ArrayUtils.isEmpty(srcRefIpAddressObject) && ArrayUtils.isEmpty(srcRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newSrcIpAddressObjectNameList)){
            securityPolicyCl.append(" source-ip any_ddress \n");
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
        // 处理dst-addr any问题
        if(ArrayUtils.isEmpty(dstRefIpAddressObject) && ArrayUtils.isEmpty(dstRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newDstIpAddressObjectNameList)){
            securityPolicyCl.append(" destination-ip any_ddress \n");
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
        // 处理service any的问题
        if(ArrayUtils.isEmpty(refServiceObject) && ArrayUtils.isEmpty(refServiceObjectGroup) && CollectionUtils.isEmpty(newServiceObjectNameList)){
            securityPolicyCl.append(" service any_service \n");
        }
        securityPolicyCl.append(" rule enable \n");
        securityPolicyCl.append("quit").append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        if(StringUtils.isNotBlank(id)){
            return String.format("undo rule id %s \n",id);
        }
        if(StringUtils.isNotBlank(name)){
            return String.format("undo rule name  %s \n",name);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 生成策略集合
     * @param policyGroupName
     * @param description
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generatePolicyGroupCommandLine(String policyGroupName,String description,Map<String, Object> map, String[] args){
        //TODO 生成华三v5策略集
        return StringUtils.LF;
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
}
