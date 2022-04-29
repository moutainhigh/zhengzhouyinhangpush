package com.abtnetworks.totems.vender.Usg.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description 华为2100 命令行生成
 * @Date: 2021/7/16
 */
public class SecurityUsg2100Impl extends OverAllGeneratorAbstractBean {

    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return name;
    }


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
        timeCommandLineBuffer.append(String.format("time-range %s ",timeNameCommandLine));

        // 指定绝对计划
        String startTime = TotemsTimeUtils.transformDateFormat(String.format("%s %s",absoluteTimeParamDTO.getStartDate(),absoluteTimeParamDTO.getStartTime()), TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.HUAWEI_USG2000_FORMAT);
        String endTime = TotemsTimeUtils.transformDateFormat(String.format("%s %s",absoluteTimeParamDTO.getEndDate(),absoluteTimeParamDTO.getEndTime()), TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.HUAWEI_USG2000_FORMAT);
        timeCommandLineBuffer.append(String.format("from %s to %s ",startTime,endTime)).append(StringUtils.LF);
        timeCommandLineBuffer.append("quit").append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }
    @Override
    public String generatePeriodicTimeCommandLine(String name, String attachStr, PeriodicTimeParamDTO periodicTimeParamDTO,
                                                  Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isEmpty(name)){
            this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
        }
        String timeNameCommandLine = generateTimeObjectName(name, map, null);

        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("time-range %s ",timeNameCommandLine));

        //指定周期计划 periodic {daily | weekdays | weekend | [monday] […] [sunday]} starttime to end-time
        //周期计划
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getCycleStart())){
            timeCommandLineBuffer.append(String.format(" from %s ",periodicTimeParamDTO.getCycleStart()));
        }
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getCycleEnd())){
            timeCommandLineBuffer.append(String.format(" to %s ",periodicTimeParamDTO.getCycleEnd()));
        }
        if(ArrayUtils.isNotEmpty(periodicTimeParamDTO.getCycle())){
            for (String date:periodicTimeParamDTO.getCycle()) {
                timeCommandLineBuffer.append(date).append(StringUtils.SPACE);
            }
        }

        timeCommandLineBuffer.append(StringUtils.LF);
        timeCommandLineBuffer.append("quit").append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        return String.format("undo time-range %s \n",name);
    }

    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag, Map<String, Object> map, String[] args) {
        return String.format("undo time-range %s \n",timeFlag);
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " undo";
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        for (String singleIp : singleIpArray) {
            ipv4ArrayCommandLine.append(deletePrefix).append(String.format(" address %s 0 \n",singleIp));
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " undo";
        }
        StringBuffer rangeIpCl = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO:rangIpArray) {
            rangeIpCl.append(deletePrefix).append(String.format(" address range %s %s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
        }
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            String deletePrefix = StringUtils.EMPTY;
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                deletePrefix = " undo";
            }
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                subnetIpv4Cl.append(deletePrefix).append(String.format(" address %s mask %s \n",ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            String deletePrefix = StringUtils.EMPTY;
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                deletePrefix = " undo";
            }
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetIpArray) {
                int maskBit;
                if(ObjectUtils.isNotEmpty(ipAddressSubnetStrDTO.getType()) && MaskTypeEnum.wildcard_mask.getType().equalsIgnoreCase(ipAddressSubnetStrDTO.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(ipAddressSubnetStrDTO.getMask());
                } else {
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                }
                subnetIpv4Cl.append(deletePrefix).append(String.format(" address %s mask %s \n",ipAddressSubnetStrDTO.getIp(), maskBit));
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " undo";
        }
        for (String ip:singleIpArray) {
            ipv4ArrayCommandLine.append(deletePrefix).append(String.format(" address %s 128 \n",ip));
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer rangeIpCl = new StringBuffer();
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " undo";
        }
        for (IpAddressRangeDTO ipAddressRangeDTO:rangIpArray) {
            rangeIpCl.append(deletePrefix).append(String.format(" address range %s %s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
        }
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
            String deletePrefix = StringUtils.EMPTY;
            if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                deletePrefix = " undo";
            }
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                subnetIpv4Cl.append(deletePrefix).append(String.format(" address %s %s \n",ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum, String name, Map<String, Object> map, String[] args) throws Exception {
        if (ipTypeEnum == null){
            return String.format("%s",name);
        }else if (RuleIPTypeEnum.IP4.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("%s" ,name);
        } else if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("%s",name);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray,
                                                     IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray,
                                                     IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray,
                                                     String[] fqdnArray, String[] objectNameRefArray, String description,
                                                     String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        //引用地址对象
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            return this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,name,id,singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,interfaceArray,fqdnArray,objectNameRefArray,null,description,attachStr,delStr,map,args);
        }
        if("any".equals(name)){
            return StringUtils.EMPTY;
        }
        if(StringUtils.isEmpty(name)){
            name = this.createIpAddressObjectNameByParamDTO(singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,fqdnArray,objectNameRefArray,map,args);
        }
        StringBuffer ipAddressGroupCl = new StringBuffer(String.format("ip address-set %s type object  \n",name));

        if(ArrayUtils.isNotEmpty(singleIpArray)){
            if(ipTypeEnum != null && RuleIPTypeEnum.IP6.name().equals(ipTypeEnum.name())){
                ipAddressGroupCl.append(this.generateSingleIpV6CommandLine(statusTypeEnum,singleIpArray,map,args));
            } else {
                ipAddressGroupCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            if(ipTypeEnum != null && RuleIPTypeEnum.IP6.name().equals(ipTypeEnum.name())){
                ipAddressGroupCl.append(this.generateRangeIpV6CommandLine(statusTypeEnum,rangIpArray,map,args));
            } else {
                ipAddressGroupCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum,rangIpArray,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            if(ipTypeEnum != null && RuleIPTypeEnum.IP6.name().equals(ipTypeEnum.name())){
                ipAddressGroupCl.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,subnetIntIpArray,null,map,args));
            } else {
                ipAddressGroupCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,subnetIntIpArray,null,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            ipAddressGroupCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,subnetIntIpArray,null,map,args));
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            if(ArrayUtils.isNotEmpty(fqdnArray)){
                ipAddressGroupCl.append(this.generateHostCommandLine(statusTypeEnum,fqdnArray,null,null));
            }
        }
        if(StringUtils.isNotBlank(description)){
            ipAddressGroupCl.append(String.format(" description %s \n",description));
        }
        ipAddressGroupCl.append("quit").append(StringUtils.LF).append(StringUtils.LF);
        return ipAddressGroupCl.toString();
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name,
                                                   Map<String, Object> map, String[] args) throws Exception {
        return deleteIpAddressObjectGroupCommandLine(ipTypeEnum, delStr, name, map, args);
    }

    @Override
    public String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return this.generateIpAddressObjectName(ipTypeEnum,groupName,map,args);
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum,String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isNotEmpty(groupName)){
            return String.format("undo ip address-set %s %s" ,groupName, StringUtils.LF);
        }else {
            return null;
        }
    }

    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String groupName,
                                                          String id, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray,
                                                          IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray,
                                                          String[] fqdnArray, String[] objectNameRefArray,
                                                          String[] objectGroupNameRefArray, String description,
                                                          String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        if("any".equals(groupName)){
            return StringUtils.EMPTY;
        }
        if(StringUtils.isEmpty(groupName)){
            groupName = this.createIpAddressObjectGroupName(singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,null,fqdnArray,objectNameRefArray,objectGroupNameRefArray,map,args);
        }
        StringBuffer ipAddressGroupCl = new StringBuffer(String.format("ip address-set %s type group \n",groupName));

        if(ArrayUtils.isNotEmpty(singleIpArray)){
            if(ipTypeEnum != null && RuleIPTypeEnum.IP6.name().equals(ipTypeEnum.name())){
                ipAddressGroupCl.append(this.generateSingleIpV6CommandLine(statusTypeEnum,singleIpArray,map,args));
            } else {
                ipAddressGroupCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            if(ipTypeEnum != null && RuleIPTypeEnum.IP6.name().equals(ipTypeEnum.name())){
                ipAddressGroupCl.append(this.generateRangeIpV6CommandLine(statusTypeEnum,rangIpArray,map,args));
            } else {
                ipAddressGroupCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum,rangIpArray,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            if(ipTypeEnum != null && RuleIPTypeEnum.IP6.name().equals(ipTypeEnum.name())){
                ipAddressGroupCl.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,subnetIntIpArray,null,map,args));
            } else {
                ipAddressGroupCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,subnetIntIpArray,null,map,args));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            ipAddressGroupCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,subnetIntIpArray,null,map,args));
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            if(ArrayUtils.isNotEmpty(fqdnArray)){
                ipAddressGroupCl.append(this.generateHostCommandLine(statusTypeEnum,fqdnArray,null,null));
            }
        }
        //引用地址对象
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName:objectNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" undo address address-set  %s \n",objectName));
                } else {
                    ipAddressGroupCl.append(String.format(" address address-set  %s \n",objectName));
                }
            }
        }
        //引用地址组对象
        if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
            for (String objectGroupName:objectGroupNameRefArray) {
                if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
                    ipAddressGroupCl.append(String.format(" undo address address-set  %s \n",objectGroupName));
                } else {
                    ipAddressGroupCl.append(String.format(" address address-set  %s \n",objectGroupName));
                }
            }
        }
        if(StringUtils.isNotBlank(description)){
            ipAddressGroupCl.append(String.format(" description %s \n",description));
        }
        ipAddressGroupCl.append("quit").append(StringUtils.LF).append(StringUtils.LF);
        return ipAddressGroupCl.toString();
    }

    @Override
    public String generateExcludeIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateExcludeIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                          Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                          Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                          String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer icmpCl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            icmpCl.append(" undo");
        }
        icmpCl.append(String.format(" service protocol  icmp "));
        icmpCl.append(StringUtils.LF);
        return icmpCl.toString();
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray,
                                           String[] protocolAttachCodeArray, Integer[] srcSinglePortArray,
                                           String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray,
                                           PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray,
                                           Map<String, Object> map, String[] args) throws Exception {
        StringBuffer icmpCl = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            icmpCl.append(" undo");
        }
        icmpCl.append(String.format(" service protocol ICMPv6 "));
        icmpCl.append(StringUtils.LF);
        return icmpCl.toString();
    }

    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray,
                                         String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray,
                                         PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        StringBuffer timeoutBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s to %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format("%s to %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        StringBuffer tcpCommandLine = new StringBuffer();
        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())){
            tcpCommandLine.append(" undo ");
        }
        tcpCommandLine.append(" service protocol tcp ");
        if(StringUtils.isNotBlank(srcPortBuffer.toString())){
            tcpCommandLine.append(String.format("source-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            tcpCommandLine.append(String.format("destination-port %s ",dstPortBuffer.toString()));
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray,
                                         String[] protocolAttachCodeArray, Integer[] srcSinglePortArray,
                                         String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s to %s",dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            srcPortBuffer.append(String.format("%s to %s",srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }

        StringBuffer udpCommandLine = new StringBuffer();
        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())){
            udpCommandLine.append(" undo ");
        }
        udpCommandLine.append(" service protocol udp ");
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
    public String generateTCP_UDPCommandLine(StatusTypeEnum statusTypeEnum,
                                             String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                             Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                             Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                             String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateOtherCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray,
                                           String[] protocolAttachCodeArray, Integer[] srcSinglePortArray,
                                           String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                           Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                           String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return deleteServiceObjectGroupCommandLine(delStr,attachStr,name,map,args);
    }

    @Override
    public String generateServiceObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String id,
                                                   String attachStr, List<ServiceParamDTO> serviceParamDTOList,
                                                   String description, Map<String, Object> map, String[] args) throws Exception {
        if("all".equals(name) || "any".equals(name)){
            return StringUtils.EMPTY;
        }
        StringBuffer serviceObjectCl = new StringBuffer();
        if(CollectionUtils.isEmpty(serviceParamDTOList) && StringUtils.isNotBlank(name)){
            return String.format("ip service-set %s type object \nquit \n",name);
        }
        serviceObjectCl.append(String.format("ip service-set %s type object \n",name));
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
            serviceObjectCl.append("quit").append(StringUtils.LF);
        }
        return serviceObjectCl.toString();
    }

    @Override
    public String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(groupName)) {
            return String.format("undo ip service-set %s %s", groupName, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String generateServiceObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String id,
                                                        String attachStr, List<ServiceParamDTO> serviceParamDTOList,
                                                        String description, String[] serviceObjectNameRefArray,
                                                        String[] serviceObjectGroupNameRefArray, Map<String, Object> map,
                                                        String[] args) throws Exception {
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
        if(StringUtils.isBlank(groupName)){
            groupName = this.createServiceObjectGroupName(serviceParamDTOList,serviceObjectNameRefArray,serviceObjectGroupNameRefArray,null,null);
        }
        String deletePrefix = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            deletePrefix = " undo";
        }
        serviceObjectGroupCl.append(String.format("ip service-set %s type group \n",this.generateServiceObjectGroupName(groupName, null, null)));
        if(StringUtils.isNotBlank(description)){
            serviceObjectGroupCl.append(String.format(" description %s \n",description));
        }
        if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
            for (String serviceObjectName:newServiceObjectNameList) {
                serviceObjectGroupCl.append(String.format(" service service-set  %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for (String serviceObjectName:serviceObjectNameRefArray) {
                serviceObjectGroupCl.append(deletePrefix).append(String.format(" service service-set  %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            for (String serviceObjectGroupName:serviceObjectGroupNameRefArray) {
                serviceObjectGroupCl.append(deletePrefix).append(String.format(" service service-set %s \n",serviceObjectGroupName));
            }
        }
        serviceObjectGroupCl.append("quit").append(StringUtils.LF);
        return serviceObjectGroupCl.toString();
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuffer preCommandline = new StringBuffer();
        preCommandline.append("system-view\n");
        if (isVsys != null && isVsys && StringUtils.isNotBlank(vsysName)) {
            preCommandline.append(String.format("switch vsys %s \n",vsysName));
        }
        return preCommandline.toString();
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "return \n";
    }

    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 获取流量方向
     * @return {inbound|outbound}
     */
    private String getDirection(Map<String,Object> map) {
        /**
         * srcPriority 源域优先级
         * dstPriority 目的域优先级
         */
        if(map == null || !map.containsKey("srcPriority") || !map.containsKey("dstPriority")){
            return "inbound";
        }
        /**
         *  低->高 inbound，高->低 outbound
         */
        int srcPriority = (int) map.get("srcPriority");
        int dstPriority = (int) map.get("dstPriority");
        if (srcPriority > dstPriority) {
            return "inbound";
        }
        return "outbound";
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
        List<String> newSrcIpAddressObjectGroupNameList = new ArrayList<>();
        String sourceIp = null;
        StringBuffer ipAddressGroupCl = new StringBuffer();
        if(ObjectUtils.isNotEmpty(srcIpDto)) {
            if (ArrayUtils.getLength(srcIpDto.getSingleIpArray()) + ArrayUtils.getLength(srcIpDto.getRangIpArray()) + ArrayUtils.getLength(srcIpDto.getSubnetIntIpArray()) +
                    ArrayUtils.getLength(srcIpDto.getSubnetStrIpArray()) + ArrayUtils.getLength(srcIpDto.getHosts()) + ArrayUtils.getLength(srcIpDto.getObjectNameRefArray()) + ArrayUtils.getLength(srcIpDto.getObjectGroupNameRefArray()) > 1) {
                if (ArrayUtils.isNotEmpty(srcIpDto.getObjectGroupNameRefArray())) {
                    //创建地址组对象
                    String srcObjectGroupName = this.createIpAddressObjectGroupName(srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), null, srcIpDto.getHosts(),
                            srcIpDto.getObjectNameRefArray(), srcIpDto.getObjectGroupNameRefArray(), map, args);
                    newSrcIpAddressObjectGroupNameList.add(srcObjectGroupName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcObjectGroupName, null, srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), null, srcIpDto.getHosts(),
                            srcIpDto.getObjectNameRefArray(), srcIpDto.getObjectGroupNameRefArray(), null, null, null, null, null));
                } else {
                    // 创建地址对象
                    String srcObjectName = this.createIpAddressObjectNameByParamDTO(srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), srcIpDto.getHosts(),
                            srcIpDto.getObjectNameRefArray(), map, args);
                    newSrcIpAddressObjectNameList.add(srcObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcObjectName, null, srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), null, srcIpDto.getHosts(),
                            srcIpDto.getObjectNameRefArray(), null, null, null, null, null));
                }
            } else {
                //策略中直接使用
                if (ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())) {
                    String singleIp = srcIpDto.getSingleIpArray()[0];
                    sourceIp = String.format(" policy source %s %s \n", singleIp, 0);
                } else if (ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())) {
                    IpAddressRangeDTO rangeIp = srcIpDto.getRangIpArray()[0];
                    sourceIp = String.format(" policy source range %s %s \n", rangeIp.getStart(), rangeIp.getEnd());
                } else if (ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())) {
                    IpAddressSubnetIntDTO subnetIntDTO = srcIpDto.getSubnetIntIpArray()[0];
                    sourceIp = String.format(" policy source %s mask %s \n", subnetIntDTO.getIp(), subnetIntDTO.getMask());
                } else if (ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())) {
                    IpAddressSubnetStrDTO subnetStrDTO = srcIpDto.getSubnetStrIpArray()[0];
                    sourceIp = String.format(" policy source %s mask %s \n", subnetStrDTO.getIp(), TotemsIpUtils.getMaskBit(subnetStrDTO.getMask()));
                } else if (ArrayUtils.isNotEmpty(srcIpDto.getObjectNameRefArray())) {
                    sourceIp = String.format(" policy source address-set %s \n", srcIpDto.getObjectNameRefArray()[0]);
                } else if (ArrayUtils.isNotEmpty(srcIpDto.getObjectGroupNameRefArray())) {
                    sourceIp = String.format(" policy source address-set %s \n", srcIpDto.getObjectGroupNameRefArray()[0]);
                }
            }
        }
        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        List<String> newDstIpAddressObjectGroupNameList = new ArrayList<>();
        String destinationIp = null;
        if(ObjectUtils.isNotEmpty(dstIpDto)) {
            if (ArrayUtils.getLength(dstIpDto.getSingleIpArray()) + ArrayUtils.getLength(dstIpDto.getRangIpArray()) + ArrayUtils.getLength(dstIpDto.getSubnetIntIpArray()) +
                    ArrayUtils.getLength(dstIpDto.getSubnetStrIpArray()) + ArrayUtils.getLength(dstIpDto.getHosts()) + ArrayUtils.getLength(dstIpDto.getObjectNameRefArray()) + ArrayUtils.getLength(dstIpDto.getObjectGroupNameRefArray()) > 1) {
                if (ArrayUtils.isNotEmpty(dstIpDto.getObjectGroupNameRefArray())) {
                    //创建地址组对象
                    String dstObjectGroupName = this.createIpAddressObjectGroupName(dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), null, dstIpDto.getHosts(),
                            dstIpDto.getObjectNameRefArray(), dstIpDto.getObjectGroupNameRefArray(), map, args);
                    newDstIpAddressObjectGroupNameList.add(dstObjectGroupName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstObjectGroupName, null, dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), null, dstIpDto.getHosts(),
                            dstIpDto.getObjectNameRefArray(), dstIpDto.getObjectGroupNameRefArray(), null, null, null, null, null));
                } else {
                    // 创建地址对象
                    String dstObjectName = this.createIpAddressObjectNameByParamDTO(dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), dstIpDto.getHosts(),
                            dstIpDto.getObjectNameRefArray(), map, args);
                    newDstIpAddressObjectNameList.add(dstObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstObjectName, null, dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), null, dstIpDto.getHosts(),
                            dstIpDto.getObjectNameRefArray(), null, null, null, null, null));
                }
            } else {
                //策略中直接使用
                if (ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray())) {
                    String singleIp = dstIpDto.getSingleIpArray()[0];
                    destinationIp = String.format(" policy destination %s %s \n", singleIp, 0);
                } else if (ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray())) {
                    IpAddressRangeDTO rangeIp = dstIpDto.getRangIpArray()[0];
                    destinationIp = String.format(" policy destination range %s %s \n", rangeIp.getStart(), rangeIp.getEnd());
                } else if (ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray())) {
                    IpAddressSubnetIntDTO subnetIntDTO = dstIpDto.getSubnetIntIpArray()[0];
                    destinationIp = String.format(" policy destination %s mask %s \n", subnetIntDTO.getIp(), subnetIntDTO.getMask());
                } else if (ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray())) {
                    IpAddressSubnetStrDTO subnetStrDTO = dstIpDto.getSubnetStrIpArray()[0];
                    destinationIp = String.format(" policy destination %s mask %s \n", subnetStrDTO.getIp(), TotemsIpUtils.getMaskBit(subnetStrDTO.getMask()));
                } else if (ArrayUtils.isNotEmpty(dstIpDto.getObjectNameRefArray())) {
                    destinationIp = String.format(" policy destination address-set %s \n", dstIpDto.getObjectNameRefArray()[0]);
                } else if (ArrayUtils.isNotEmpty(dstIpDto.getObjectGroupNameRefArray())) {
                    destinationIp = String.format(" policy destination address-set %s \n", dstIpDto.getObjectGroupNameRefArray()[0]);
                }
            }
        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        String serviceObjectCommandLine = null;
        if(ArrayUtils.isNotEmpty(serviceParam)){
            for (ServiceParamDTO serviceParamDTO:serviceParam) {
                String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                newService.add(serviceParamDTO);
                serviceObjectCommandLine = this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null,null);
            }
        }
        StringBuffer securityPolicyCl = new StringBuffer();
        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        // 地址对象命令行
        if(StringUtils.isNotBlank(ipAddressGroupCl.toString())){
            securityPolicyCl.append(ipAddressGroupCl.toString());
        }
        //服务对象命令行
        if(StringUtils.isNotBlank(serviceObjectCommandLine)){
            securityPolicyCl.append(serviceObjectCommandLine);
        }

        securityPolicyCl.append(StringUtils.LF);
        // 策略命令行
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

        boolean isOneEmpty = (StringUtils.isEmpty(srcZoneName) && StringUtils.isNotEmpty(dstZoneName)) || (StringUtils.isEmpty(dstZoneName) && StringUtils.isNotEmpty(srcZoneName)) ;
        if(isOneEmpty){
            String zone;
            if(StringUtils.isNotEmpty(srcZoneName)){
                zone = srcZoneName;
            }else{
                zone = dstZoneName;
            }
            securityPolicyCl.append(String.format("policy zone %s\n", zone));
        }
        else  if(StringUtils.isNotEmpty(srcZoneName) && srcZoneName.equals(dstZoneName)) {
            securityPolicyCl.append(String.format("policy zone %s\n", srcZoneName));
        } else {
            securityPolicyCl.append(String.format("policy interzone %s %s %s\n", srcZoneName, dstZoneName, getDirection(map)));
        }
        securityPolicyCl.append(String.format("policy \n"));

        securityPolicyCl.append("policy logging\n");
        if(StringUtils.isNotBlank(description)){
            securityPolicyCl.append(String.format(" description %s ",description)).append(StringUtils.LF);
        }

        // 本次生成的时间对象
        if(StringUtils.isNotBlank(newTimeObjectName)){
            securityPolicyCl.append(String.format(" policy time-range  %s %s",newTimeObjectName,StringUtils.LF));
        } else if(ArrayUtils.isNotEmpty(refTimeObject)){
            //引用时间对象e
            for (String timeObjectName:refTimeObject) {
                securityPolicyCl.append(String.format(" policy time-range %s %s",timeObjectName,StringUtils.LF));
            }
        }

        // 本次生成的源地址
        if(StringUtils.isNotBlank(sourceIp)){
            securityPolicyCl.append(sourceIp);
        } else if(CollectionUtils.isNotEmpty(newSrcIpAddressObjectNameList)){
            for (String srcObjectName : newSrcIpAddressObjectNameList) {
                securityPolicyCl.append(String.format(" policy source-address  address-set %s %s",srcObjectName,StringUtils.LF));
            }
        } else if(CollectionUtils.isNotEmpty(newSrcIpAddressObjectGroupNameList)){
            for (String srcObjectGroupName : newSrcIpAddressObjectGroupNameList) {
                securityPolicyCl.append(String.format(" policy source-address  address-set %s %s",srcObjectGroupName,StringUtils.LF));
            }
        }

        // 引用地址和地址组对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            for (String srcRefIpAddressObjectName:srcRefIpAddressObject) {
                securityPolicyCl.append(String.format(" policy source-address  address-set %s ", srcRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            for (String srcRefIpAddressObjectGroupName:srcRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" policy source-address  address-set %s ", srcRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }
        // 处理src-addr any问题
        if(StringUtils.isBlank(sourceIp) && ArrayUtils.isEmpty(srcRefIpAddressObject) && ArrayUtils.isEmpty(srcRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newSrcIpAddressObjectNameList) && CollectionUtils.isEmpty(newSrcIpAddressObjectGroupNameList)){
            securityPolicyCl.append(" policy source-address any").append(StringUtils.LF);
        }

        // 本地生成的目的地址
        if(StringUtils.isNotBlank(destinationIp)){
            securityPolicyCl.append(destinationIp);
        } else if(CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)){
            for (String dstObjectName : newDstIpAddressObjectNameList) {
                securityPolicyCl.append(String.format(" policy destination-address  address-set %s %s",dstObjectName,StringUtils.LF));
            }
        } else if(CollectionUtils.isNotEmpty(newDstIpAddressObjectGroupNameList)){
            for (String dstObjectGroupName : newDstIpAddressObjectGroupNameList) {
                securityPolicyCl.append(String.format(" policy destination-address  address-set %s %s",dstObjectGroupName,StringUtils.LF));
            }
        }
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            for (String dstRefIpAddressObjectName:dstRefIpAddressObject) {
                securityPolicyCl.append(String.format(" policy destination-address  address-set %s ", dstRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){
            for (String dstRefIpAddressObjectGroupName:dstRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" policy destination-address address-set  %s ", dstRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }
        // 处理dst-addr any问题
        if(StringUtils.isBlank(destinationIp) && ArrayUtils.isEmpty(dstRefIpAddressObject) && ArrayUtils.isEmpty(dstRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newDstIpAddressObjectNameList) && CollectionUtils.isEmpty(newDstIpAddressObjectGroupNameList)){
            securityPolicyCl.append(" policy destination-address any").append(StringUtils.LF);
        }

        // 本次生成的服务对象
        if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
            for (String newServiceObjectName : newServiceObjectNameList) {
                securityPolicyCl.append(String.format(" policy service service-set %s %s",newServiceObjectName,StringUtils.LF));
            }
        }
        //引用服务对象
        if(ArrayUtils.isNotEmpty(refServiceObject)){
            for (String serviceObjectName:refServiceObject) {
                securityPolicyCl.append(String.format(" policy service service-set %s %s",serviceObjectName,StringUtils.LF));
            }
        }
        //引用服务组对象
        if(ArrayUtils.isNotEmpty(refServiceObjectGroup)){
            for (String serviceObjectGroupName:refServiceObjectGroup) {
                securityPolicyCl.append(String.format(" policy service service-set %s %s",serviceObjectGroupName,StringUtils.LF));
            }
        }
        // 处理service any的问题
        if(ArrayUtils.isEmpty(refServiceObject) && ArrayUtils.isEmpty(refServiceObjectGroup) && CollectionUtils.isEmpty(newServiceObjectNameList)){
            securityPolicyCl.append(" policy service any").append(StringUtils.LF);
        }
        securityPolicyCl.append(String.format(" action %s\n", action));
        securityPolicyCl.append("quit").append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("ip route-static  %s %s %s\n",ip,mask,"NULL 0");
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description,Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("undo ip route-static  %s %s %s\n",ip,mask,"NULL 0");
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("ipv6 route-static %s %s %s\n",ip,mask,"NULL 0");
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("undo ipv6 route-static %s %s %s\n",ip,mask,"NULL 0");
    }
}
