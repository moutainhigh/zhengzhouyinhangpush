package com.abtnetworks.totems.vender.cisco.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: WangCan
 * @Description Cisco ASA 命令行
 * @Date: 2021/7/16
 */
public class SecurityCiscoASAImpl extends OverAllGeneratorAbstractBean {


    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "configure terminal\n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end \n";
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
        StringBuffer timeCl = new StringBuffer();
        timeCl.append(String.format("time-range %s \n",timeNameCommandLine));
        String endDate = absoluteTimeParamDTO.getEndDate();
        String endTime = absoluteTimeParamDTO.getEndTime();
        String end = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(startTime)){
            end = String.format("%s %s",endDate,endTime);
        }
        if(StringUtils.isNotBlank(start) &&  StringUtils.isNotBlank(end)){
            String startTimeStr = TotemsTimeUtils.transformDateFormat(start, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.CISCO_ASA_TIME_FORMAT);
            String endTimeStr = TotemsTimeUtils.transformDateFormat(end, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.CISCO_ASA_TIME_FORMAT);
            timeCl.append(String.format("absolute start %s end %s \n",startTimeStr,endTimeStr));
        } else if(StringUtils.isNotBlank(start)){
            String startTimeStr = TotemsTimeUtils.transformDateFormat(start, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.CISCO_ASA_TIME_FORMAT);
            timeCl.append(String.format("absolute start %s \n",startTimeStr));
        } else {
            String endTimeStr = TotemsTimeUtils.transformDateFormat(end, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.CISCO_ASA_TIME_FORMAT);
            timeCl.append(String.format("absolute end %s \n",endTimeStr));
        }
        return timeCl.toString();
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
        timeCommandLineBuffer.append(String.format("time-range %s \n",timeNameCommandLine));
        timeCommandLineBuffer.append(" periodic ");
        String[] cycle = periodicTimeParamDTO.getCycle();

        if(ArrayUtils.isNotEmpty(cycle)){
            for (String date:cycle) {
                timeCommandLineBuffer.append(date).append(StringUtils.SPACE);
            }
        }
        String cycleStart = periodicTimeParamDTO.getCycleStart();
        if(StringUtils.isNotBlank(cycleStart)){
            timeCommandLineBuffer.append(cycleStart);
        }
        String cycleEnd = periodicTimeParamDTO.getCycleEnd();
        if(StringUtils.isNotBlank(cycleEnd)){
            timeCommandLineBuffer.append(String.format(" to %s ",cycleEnd));
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
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            //TODO ciscoASA 编辑地址对象
        } else {
            for (int i = 0; i < singleIpArray.length; i++) {
                ipv4ArrayCommandLine.append(String.format(" host %s \n",singleIpArray[i]));
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
            //TODO ciscoASA 编辑地址对象
        } else {
            for (int i = 0; i < hosts.length; i++) {
                hostCommandLine.append(String.format(" fqdn %s \n",hosts[i]));
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
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer rangeIpCl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            //TODO ciscoASA 编辑地址对象
        } else {
            for (int i = 0; i < rangIpArray.length; i++) {
                rangeIpCl.append(String.format(" range %s %s \n",rangIpArray[i].getStart(),rangIpArray[i].getEnd()));
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
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(subnetIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            //TODO ciscoASA 编辑地址对象
        } else {
            for (int i = 0; i < subnetIpArray.length; i++) {
                subnetIpv4Cl.append(String.format(" subnet %s %s \n",subnetIpArray[i].getIp(), TotemsIpUtils.getMaskMap(String.valueOf(subnetIpArray[i].getMask()))));
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
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
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
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                    wildcardMask = TotemsIpUtils.getWildcardMaskMap(maskBit);
                } else {
                    wildcardMask = ipAddressSubnetStrDTO.getMask();
                }
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    //TODO ciscoASA 编辑地址对象
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
                    //TODO ciscoASA 编辑地址对象
                } else {
                    ipv6CommandLine.append(String.format(" subnet %s %s \n",args[i],subnetIpArray[i].getIp(),subnetIpArray[i].getMask()));
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
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            return this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,name,id,singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,interfaceArray,fqdnArray,objectNameRefArray,null,null,null,null,null,null);
        }
        if(ArrayUtils.getLength(singleIpArray) + ArrayUtils.getLength(rangIpArray) + ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) + ArrayUtils.getLength(fqdnArray) > 1 ){
            return this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,name,id,singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,interfaceArray,fqdnArray,objectNameRefArray,null,null,null,null,null,null);
        }
        StringBuffer ipAddressCl = new StringBuffer();
        String namePrefix = "object network %s \n";
        ipAddressCl.append(String.format(namePrefix,name));
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            // ip地址对象
            ipAddressCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,null,args));
        } else if(ArrayUtils.isNotEmpty(rangIpArray)){
            // ip范围地址对象
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                ipAddressCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO},null,args));
            }
        } else if(ArrayUtils.isNotEmpty(subnetIntIpArray) || ArrayUtils.isNotEmpty(subnetStrIpArray)){
            if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
                // int子网地址对象
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
        } else if(ArrayUtils.isNotEmpty(fqdnArray)){
            ipAddressCl.append(this.generateHostCommandLine(statusTypeEnum,fqdnArray,map,args));
        }
        ipAddressCl.append("exit").append(StringUtils.LF);
        return ipAddressCl.toString();
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
     * @return 深信服地址组允许包含地址值
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
        StringBuffer ipAddressCl = new StringBuffer();
        List<String> refNameList = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String singleIp : singleIpArray) {
                String singleIpObjectName = this.createIpAddressObjectNameByIp(singleIp, map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, singleIpObjectName, null, new String[]{singleIp}, null, null, null, null, null, null, null, null, null, null, null));
                refNameList.add(singleIpObjectName);
            }
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            for (String host : fqdnArray) {
                String hostIpObjectName = this.createIpAddressObjectNameByHost(host, map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, hostIpObjectName, null, null, null, null, null, null, new String[]{host}, null, null, null, null, null, null));
                refNameList.add(hostIpObjectName);
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                String rangeIpObjectName = this.createIpAddressObjectNameByIpRange(rangeDTO.getStart(),rangeDTO.getEnd(), map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, rangeIpObjectName, null,null, new IpAddressRangeDTO[]{rangeDTO},  null, null, null, null, null, null, null, null, null, null));
                refNameList.add(rangeIpObjectName);
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                String subnetIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(),ipAddressSubnetIntDTO.getMask(), map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIpObjectName, null,null,  null, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null, null, null, null, null, null, null, null, null));
                refNameList.add(subnetIpObjectName);
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                String subnetIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetStrDTO.getIp(),TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()), map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIpObjectName, null,null,  null, null, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null, null, null, null, null, null, null, null));
                refNameList.add(subnetIpObjectName);
            }
        }
        String namePrefix = "object-group network %s \n";
        ipAddressCl.append(String.format(namePrefix,name));
        if(StringUtils.isNotBlank(description)){
            ipAddressCl.append(String.format(" description \"%s\" \n",description));
        }
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            // TODO ciscoASA 编辑地址对象
            // ipPre = " no";
        }
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName : objectNameRefArray) {
                ipAddressCl.append(ipPre).append(String.format(" network-object object %s \n",objectName));
            }
        }
        if(CollectionUtils.isNotEmpty(refNameList)){
            for (String refName : refNameList) {
                ipAddressCl.append(ipPre).append(String.format(" network-object object %s \n",refName));
            }
        }
        if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
            for (String objectGroupName : objectGroupNameRefArray) {
                ipAddressCl.append(ipPre).append(String.format(" group-object %s \n",objectGroupName));
            }
        }
        ipAddressCl.append("exit \n");
        return ipAddressCl.toString();
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
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
            // TODO ciscoASA 编辑地址对象
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

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
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
            // TODO ciscoASA 编辑地址对象
        } else {
            tcpCommandLine.append(" service udp  ");
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
            return String.format("object service %s \n exit\n ",name);
        }
        if("any".equalsIgnoreCase(name) || "icmp".equalsIgnoreCase(name)){
            return StringUtils.EMPTY;
        }
        serviceObjectCl.append(String.format("object service %s \n",this.generateServiceObjectName(name,null,null)));
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
            } else if(ProtocolTypeEnum.ICMP6.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                serviceObjectCl.append(this.generateICMP6CommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
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
        StringBuffer serviceGroupObjectCl = new StringBuffer();

        if(CollectionUtils.isEmpty(serviceParamDTOList) && MapUtils.isEmpty(map)){
            return String.format("object-group service %s \n exit\n ",name);
        }
        serviceGroupObjectCl.append(String.format("object-group service %s \n",this.generateServiceObjectGroupName(name,null,null)));
        if(StringUtils.isNotBlank(description)){
            serviceGroupObjectCl.append(String.format(" description %s \n",description));
        }
        String servicePre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            servicePre = " no";
        }
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for (String serviceObjectName : serviceObjectNameRefArray) {
                serviceGroupObjectCl.append(servicePre).append(String.format(" service-object object %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            for (String serviceObjectGroupName : serviceObjectGroupNameRefArray) {
                serviceGroupObjectCl.append(servicePre).append(String.format(" group-object %s \n",serviceObjectGroupName));
            }
        }
        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus()) && CollectionUtils.isNotEmpty(serviceParamDTOList)){
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                String protocolPortCl = null;
                if(ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                    protocolPortCl = this.generateTCPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, args);
                } else if(ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                    protocolPortCl = this.generateUDPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(),null,null,args);
                } else if(ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                    protocolPortCl = this.generateICMPCommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(),null,null,args);
                } else if(ProtocolTypeEnum.ICMP6.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())){
                    protocolPortCl = this.generateICMP6CommandLine(statusTypeEnum,serviceParamDTO.getProtocolAttachTypeArray(),serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(),serviceParamDTO.getSrcSinglePortStrArray(),serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(),serviceParamDTO.getDstSinglePortStrArray(),serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(),null,null,args);
                }
                if(protocolPortCl != null){
                    protocolPortCl = protocolPortCl.replace("service","service-object");
                    serviceGroupObjectCl.append(protocolPortCl);
                }
            }
        }
        serviceGroupObjectCl.append("exit \n");
        return serviceGroupObjectCl.toString();
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
     * access-list access_list_name
     * [line line_number]
     * extended {deny | permit}
     * protocol_argument
     * source_address_argument
     * dest_address_argument
     * [log [[level] [interval secs] | disable | default]]
     * [inactive | time-range time_range_name]
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
        String newTimeObjectName = StringUtils.EMPTY;
        String newTimeCommandLine = null;
        if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName,null,absoluteTimeParamDTO,map,null);
            newTimeObjectName = String.format(" time-range %s ",newTimeObjectName);
        } else if(ObjectUtils.isNotEmpty(periodicTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(newTimeObjectName,null,periodicTimeParamDTO,map,null);
            newTimeObjectName = String.format(" time-range %s ",newTimeObjectName);
        } else if(ArrayUtils.isNotEmpty(refTimeObject)){
            newTimeObjectName = String.format(" time-range %s ",refTimeObject[0]);
        }

        StringBuffer securityPolicyCl = new StringBuffer();
        // 处理地址对象
        String srcGroupName = null;
        if(ObjectUtils.isNotEmpty(srcIpDto) || (ArrayUtils.getLength(srcRefIpAddressObject) + ArrayUtils.getLength(srcRefIpAddressObjectGroup) > 1)){
            if(srcIpDto == null){
                srcIpDto = new IpAddressParamDTO();
            }
            if(srcIpDto.getObjectNameRefArray() == null){
                srcIpDto.setObjectNameRefArray(new String[0]);
            }
            if(srcIpDto.getObjectGroupNameRefArray() == null){
                srcIpDto.setObjectGroupNameRefArray(new String[0]);
            }
            String[] allSrcRefObjectName = (String[]) ArrayUtils.addAll(srcRefIpAddressObject, srcIpDto.getObjectNameRefArray());
            String[] allSrcRefObjectGroupName = (String[]) ArrayUtils.addAll(srcRefIpAddressObjectGroup, srcIpDto.getObjectGroupNameRefArray());

            //生成src地址组对象命令行
            srcGroupName = this.createIpAddressObjectGroupName(srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),
                    null,srcIpDto.getHosts(),allSrcRefObjectName,allSrcRefObjectGroupName,map,args);
            securityPolicyCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),srcGroupName,null,srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),
                    null,srcIpDto.getHosts(),allSrcRefObjectName,allSrcRefObjectGroupName,null,null,null,null,null));
            srcGroupName = String.format(" object-group %s ",srcGroupName);
        } else if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            srcGroupName = String.format(" object %s ",srcRefIpAddressObject[0]);
        } else if(ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            srcGroupName = String.format(" object-group %s ",srcRefIpAddressObjectGroup[0]);
        } else {
            srcGroupName = "any";
        }

        String dstGroupName = null;
        //生成dst地址对象命令行
        if(ObjectUtils.isNotEmpty(dstIpDto) || (ArrayUtils.getLength(dstRefIpAddressObject) + ArrayUtils.getLength(dstRefIpAddressObjectGroup) > 1)){
            if(dstIpDto == null){
                dstIpDto = new IpAddressParamDTO();
            }
            if(dstIpDto.getObjectNameRefArray() == null){
                dstIpDto.setObjectNameRefArray(new String[0]);
            }
            if(dstIpDto.getObjectGroupNameRefArray() == null){
                dstIpDto.setObjectGroupNameRefArray(new String[0]);
            }
            String[] allDstRefObjectName = (String[]) ArrayUtils.addAll(dstRefIpAddressObject, dstIpDto.getObjectNameRefArray());
            String[] allDstRefObjectGroupName = (String[]) ArrayUtils.addAll(dstRefIpAddressObjectGroup, dstIpDto.getObjectGroupNameRefArray());
            //生成dst地址组对象命令行
            dstGroupName = this.createIpAddressObjectGroupName(dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),
                    null,dstIpDto.getHosts(),allDstRefObjectName,allDstRefObjectGroupName,map,args);
            securityPolicyCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),dstGroupName,null,dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),
                    null,dstIpDto.getHosts(),allDstRefObjectName,allDstRefObjectGroupName,null,null,null,null,null));
            dstGroupName = String.format(" object-group %s ",dstGroupName);
        } else if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            dstGroupName = String.format(" object %s ",dstRefIpAddressObject[0]);
        } else if(ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){
            dstGroupName = String.format(" object-group %s ",dstRefIpAddressObjectGroup[0]);
        } else {
            dstGroupName = "any";
        }

        // 处理服务对象
        String serviceGroupName = null;
        if(ArrayUtils.getLength(serviceParam) > 1 || (ArrayUtils.getLength(serviceParam) == 1 && (ArrayUtils.getLength(serviceParam[0].getDstRangePortArray()) +
                ArrayUtils.getLength(serviceParam[0].getDstRangePortArray()) + ArrayUtils.getLength(serviceParam[0].getDstRangePortArray()) > 1)) || (ArrayUtils.getLength(refServiceObject) + ArrayUtils.getLength(refServiceObjectGroup)) > 1){
            if(serviceParam == null){
                serviceParam = new ServiceParamDTO[]{};
            }
            List<ServiceParamDTO> serviceParamDTOList = Arrays.stream(serviceParam).collect(Collectors.toList());
            serviceGroupName = this.createServiceObjectGroupName(serviceParamDTOList, refServiceObject, refServiceObjectGroup, map, args);
            securityPolicyCl.append(this.generateServiceObjectGroupCommandLine(statusTypeEnum,serviceGroupName,null,null, Arrays.asList(serviceParam),null,refServiceObject,refServiceObjectGroup,null,null));
            serviceGroupName = String.format(" object-group %s ",serviceGroupName);
        } else if (ArrayUtils.isNotEmpty(refServiceObject)){
            serviceGroupName = String.format(" object %s ",refServiceObject[0]);
        } else if (ArrayUtils.isNotEmpty(refServiceObjectGroup)){
            serviceGroupName = String.format(" object-group %s ",refServiceObjectGroup[0]);
        }

        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        securityPolicyCl.append(StringUtils.LF);
        if(map != null){
            name = (String) map.get("ciscoInterfacePolicyName");
        }
        //思科新建策略，默认是置顶的、最前，不分前后
        String line = "";
        if(StringUtils.isNotBlank(name)){
            swapRuleNameId = StringUtils.isNotBlank(swapRuleNameId) ? swapRuleNameId : "";
            if(moveSeatEnum != null){
                int moveSeatCode = moveSeatEnum.getCode();
                if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                    line = String.format("line %s", swapRuleNameId);
                } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                    int lineNum = -1;
                    try {
                        lineNum = Integer.valueOf(swapRuleNameId);
                    } catch (Exception e) {
                        logger.info("放在某条策略之后的名称应为数字ID！");
                    }
                    line = String.format("line %d", lineNum + 1);
                } else if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
                    line = "line 1";
                }
            }
        }
        String serviceAddressLine = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(serviceGroupName)){
            serviceAddressLine = String.format(" %s %s %s ",serviceGroupName,srcGroupName,dstGroupName);
        } else if(ArrayUtils.isNotEmpty(serviceParam)){
            ServiceParamDTO serviceParamDTO = serviceParam[0];
            String protocolType = serviceParamDTO.getProtocol().getType();
            if(protocolType.contains("icmp")){
                serviceAddressLine = String.format(" %s %s %s ",protocolType,srcGroupName,dstGroupName);
            } else {
                Integer port = serviceParamDTO.getDstSinglePortArray()[0];
                serviceAddressLine = String.format(" %s %s %s eq %s ",protocolType,srcGroupName,dstGroupName,port);
            }
        } else {
            serviceAddressLine = String.format(" %s %s %s ","ip",srcGroupName,dstGroupName);
        }
        securityPolicyCl.append("access-list ");
        securityPolicyCl.append(String.format(" %s ",name));
        securityPolicyCl.append(line);
        if("permit".equalsIgnoreCase(action)){
            securityPolicyCl.append(String.format(" extended %s ","permit"));
        } else {
            securityPolicyCl.append(String.format(" extended %s ","deny"));
        }
        securityPolicyCl.append(serviceAddressLine);
        securityPolicyCl.append(newTimeObjectName);

        securityPolicyCl.append(StringUtils.LF);

        String srcZoneName = StringUtils.EMPTY;
        String dstZoneName = StringUtils.EMPTY;
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

        //接口为空时，需要新建
        if (map != null && map.containsKey("isCiscoInterfaceCreate") && (boolean)map.get("isCiscoInterfaceCreate")) {
            if (map.containsKey("outBound") && (boolean)map.get("outBound")) {
                securityPolicyCl.append(String.format("access-group %s out interface %s\n", name, dstZoneName));
            } else {
                securityPolicyCl.append(String.format("access-group %s in interface %s\n", name, srcZoneName));
            }
        }
        securityPolicyCl.append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("route %s %s %s  \n","Null0",ip, TotemsIpUtils.getMaskMap(String.valueOf(mask)));
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description,Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("no route %s %s %s  \n","Null0",ip,TotemsIpUtils.getMaskMap(String.valueOf(mask)));
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("ipv6 route %s %s/%s  \n","null0",ip,mask);
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("no ipv6 route %s %s/%s  \n","null0",ip,mask);
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(groupName)) {
            return String.format("no object-group network %s %s", groupName, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(name)) {
            return String.format("no object network %s %s", name, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(groupName)) {
            return String.format("no object-group service %s %s", groupName, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(name)) {
            return String.format("no object service %s %s", name, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        if (StringUtils.isNotEmpty(name)) {
            return String.format("no time-range %s", name, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        StringBuffer stringBuffer = new StringBuffer();
        String ruleText = map.get("ruleText").toString();
        //针对ruleText 进行处理
        int hitCntIndex = ruleText.indexOf("(hitcnt=");
        if(hitCntIndex != -1) {
            ruleText = ruleText.substring(0, hitCntIndex).trim();
        }

        //获取最后一个单词，判断hash，如果里面带name hash则正常，反之就截取
        int lastEmptyIndex = ruleText.lastIndexOf(" ");
        String lastWord = ruleText.substring(lastEmptyIndex);
        if(StringUtils.isNoneBlank(lastWord) && lastWord.trim().startsWith("0x") && ruleText.indexOf("name hash") == -1){
            ruleText = ruleText.substring(0, lastEmptyIndex);
        }

        List<String> strList = Arrays.asList(ruleText.replace("\\r\\n", "?").replace("\n", "?").split("\\?"));
        for (String row : strList) {
            stringBuffer.append(String.format("no  %s \n", row));
        }
        return stringBuffer.toString();
    }
}
