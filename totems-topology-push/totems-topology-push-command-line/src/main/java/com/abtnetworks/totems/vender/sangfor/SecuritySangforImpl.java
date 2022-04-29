package com.abtnetworks.totems.vender.sangfor;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description 深信服命令行
 * @Date: 2021/6/7
 */
public class SecuritySangforImpl extends OverAllGeneratorAbstractBean {

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "config\n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end\n";
    }

    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return String.format("\"%s\"",name);
    }

    @Override
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isEmpty(name)){
            this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
        }
        String timeNameCommandLine = this.generateTimeObjectName(name, map, null);
        StringBuffer timeCommandLine = new StringBuffer();
        timeCommandLine.append(String.format("single-schedule %s \n",timeNameCommandLine));

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
            timeCommandLine.append(String.format(" absolute start \"%s\" end \"%s\" \n",timeNameCommandLine,start,end));
        } else {
            throw new RuntimeException("开始时间和结束时间不能为空");
        }
        timeCommandLine.append("exit \n");
        return timeCommandLine.toString();
    }

    @Override
    public String generatePeriodicTimeCommandLine(String name, String attachStr, PeriodicTimeParamDTO periodicTimeParamDTO, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isEmpty(name)){
            this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
        }
        String timeNameCommandLine = this.generateTimeObjectName(name, map, null);
        StringBuffer timeCommandLine = new StringBuffer();
        timeCommandLine.append(String.format("schedule %s \n",timeNameCommandLine));
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getAbsoluteStart()) && StringUtils.isNotBlank(periodicTimeParamDTO.getAbsoluteEnd())){
            timeCommandLine.append(String.format(" absolute start %s end %s \n",periodicTimeParamDTO.getAbsoluteStart(),periodicTimeParamDTO.getAbsoluteEnd()));
        }

        if(ArrayUtils.isNotEmpty(periodicTimeParamDTO.getCycle()) && StringUtils.isNotBlank(periodicTimeParamDTO.getCycleStart()) && StringUtils.isNotBlank(periodicTimeParamDTO.getCycleEnd())){
            timeCommandLine.append(" periodic ");
            for (String cycleStr : periodicTimeParamDTO.getCycle()) {
                timeCommandLine.append(cycleStr).append(" ");
            }
            timeCommandLine.append(String.format(" start %s end %s",periodicTimeParamDTO.getCycleStart(),periodicTimeParamDTO.getCycleEnd()));
        }
        timeCommandLine.append("exit \n");
        return timeCommandLine.toString();
    }

    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args) {
        return String.format("no schedule %s \n ",timeFlag);
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args){
        return String.format("no single-schedule %s \n ",timeFlag);
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (String ip : singleIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s \n",ip));
        }
        return ipCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s-%s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
        }
        return ipCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s/%s \n",ipAddressSubnetIntDTO.getIp(),ipAddressSubnetIntDTO.getMask()));
        }
        return ipCommandLine.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s/%s \n",ipAddressSubnetStrDTO.getIp(),ipAddressSubnetStrDTO.getMask()));
        }
        return ipCommandLine.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (String ip : singleIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s \n",ip));
        }
        return ipCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s-%s \n",ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd()));
        }
        return ipCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        StringBuffer ipCommandLine = new StringBuffer();
        for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
            ipCommandLine.append(ipPre).append(String.format(" ipentry %s/%s \n",ipAddressSubnetIntDTO.getIp(),ipAddressSubnetIntDTO.getMask()));
        }
        return ipCommandLine.toString();
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

        StringBuffer ipAddressCl = new StringBuffer();
        String namePrefix = null;
        if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            namePrefix = "ipgroup %s ipv6 \n";
        } else {
            namePrefix = "ipgroup %s \n";
        }
        ipAddressCl.append(String.format(namePrefix,name));
        if(StringUtils.isNotBlank(description)){
            ipAddressCl.append(String.format(" description \"%s\" \n",description));
        }
        ipAddressCl.append(" type ip \n");
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
        ipAddressCl.append("exit").append(StringUtils.LF);
        return ipAddressCl.toString();
    }



    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("no ipgroup %s \n",name);
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum,String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return this.deleteIpAddressObjectCommandLine(ipTypeEnum,delStr,groupName,map,args);
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
        String subIpAddressName = null;
        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus()) && (ArrayUtils.getLength(singleIpArray) + ArrayUtils.getLength(rangIpArray) + ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) + ArrayUtils.getLength(fqdnArray) > 1)){
            subIpAddressName = String.format("%s_sub",name);
            ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipTypeEnum,subIpAddressName,null,singleIpArray,rangIpArray,subnetIntIpArray,subnetStrIpArray,null,fqdnArray,null,null,null,null,null,null));
        }

        String namePrefix = null;
        if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            namePrefix = "ipgroup %s ipv6 \n";
        } else {
            namePrefix = "ipgroup %s \n";
        }
        ipAddressCl.append(String.format(namePrefix,name));
        if(StringUtils.isNotBlank(description)){
            ipAddressCl.append(String.format(" description \"%s\" \n",description));
        }

        ipAddressCl.append(" type addrgroup \n");
        if(StringUtils.isNotBlank(subIpAddressName)){
            ipAddressCl.append(String.format(" member %s \n",subIpAddressName));
        }
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = " no";
        }
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName : objectNameRefArray) {
                ipAddressCl.append(ipPre).append(String.format(" member %s \n",objectName));
            }
        }
        if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
            for (String objectGroupName : objectGroupNameRefArray) {
                ipAddressCl.append(ipPre).append(String.format(" member %s \n",objectGroupName));
            }
        }
        ipAddressCl.append("exit \n");
        return ipAddressCl.toString();
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        String type = StringUtils.EMPTY;
        String code = StringUtils.EMPTY;
        if(ArrayUtils.isNotEmpty(protocolAttachTypeArray)){
            type = protocolAttachTypeArray[0];
        }
        if(ArrayUtils.isNotEmpty(protocolAttachCodeArray)){
            code = protocolAttachCodeArray[0];
        }
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            return String.format(" no icmp type %s code %s \n",type,code);
        } else {
            return String.format(" icmp type %s code %s \n",type,code);
        }
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        String type = StringUtils.EMPTY;
        String code = StringUtils.EMPTY;
        if(ArrayUtils.isNotEmpty(protocolAttachTypeArray)){
            type = protocolAttachTypeArray[0];
        }
        if(ArrayUtils.isNotEmpty(protocolAttachCodeArray)){
            code = protocolAttachCodeArray[0];
        }
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            return String.format(" no icmpv6 type %s code %s \n",type,code);
        } else {
            return String.format(" icmpv6 type %s code %s \n",type,code);
        }
    }

    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            for (Integer port : srcSinglePortArray) {
                srcPortBuffer.append(String.format("%s,",port));
            }
        }
        if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            for (String port : srcSinglePortStrArray) {
                srcPortBuffer.append(String.format("%s,",port));
            }
        }
        if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            for (PortRangeDTO portRangeDTO : srcRangePortArray) {
                srcPortBuffer.append(String.format("%s-%s,",portRangeDTO.getStart(),portRangeDTO.getEnd()));
            }
        }
        String srcPortString = srcPortBuffer.toString();
        if(srcPortString.endsWith(SymbolsEnum.COMMA.getValue())){
            srcPortString = srcPortString.substring(0,srcPortString.length()-1);
        }

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            for (Integer port : dstSinglePortArray) {
                dstPortBuffer.append(String.format("%s,",port));
            }
        }
        if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            for (String port : dstSinglePortStrArray) {
                dstPortBuffer.append(String.format("%s,",port));
            }
        }
        if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            for (PortRangeDTO portRangeDTO : dstRangePortArray) {
                dstPortBuffer.append(String.format("%s-%s,",portRangeDTO.getStart(),portRangeDTO.getEnd()));
            }
        }
        String dstPortString = dstPortBuffer.toString();
        if(dstPortString.endsWith(SymbolsEnum.COMMA.getValue())){
            dstPortString = dstPortString.substring(0,dstPortString.length()-1);
        }

        StringBuffer tcpCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            tcpCommandLine.append(" no tcp  ");
        } else {
            tcpCommandLine.append(" tcp  ");
        }

        if(StringUtils.isNotBlank(srcPortString)){
            tcpCommandLine.append(String.format(" src-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortString)){
            tcpCommandLine.append(String.format(" dst-port %s ",dstPortString));
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            for (Integer port : srcSinglePortArray) {
                srcPortBuffer.append(String.format("%s,",port));
            }
        }
        if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            for (String port : srcSinglePortStrArray) {
                srcPortBuffer.append(String.format("%s,",port));
            }
        }
        if (ArrayUtils.isNotEmpty(srcRangePortArray)){
            for (PortRangeDTO portRangeDTO : srcRangePortArray) {
                srcPortBuffer.append(String.format("%s-%s,",portRangeDTO.getStart(),portRangeDTO.getEnd()));
            }
        }
        String srcPortString = srcPortBuffer.toString();
        if(srcPortString.endsWith(SymbolsEnum.COMMA.getValue())){
            srcPortString = srcPortString.substring(0,srcPortString.length()-1);
        }

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            for (Integer port : dstSinglePortArray) {
                dstPortBuffer.append(String.format("%s,",port));
            }
        }
        if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            for (String port : dstSinglePortStrArray) {
                dstPortBuffer.append(String.format("%s,",port));
            }
        }
        if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            for (PortRangeDTO portRangeDTO : dstRangePortArray) {
                dstPortBuffer.append(String.format("%s-%s,",portRangeDTO.getStart(),portRangeDTO.getEnd()));
            }
        }
        String dstPortString = dstPortBuffer.toString();
        if(dstPortString.endsWith(SymbolsEnum.COMMA.getValue())){
            dstPortString = dstPortString.substring(0,dstPortString.length()-1);
        }

        StringBuffer udpCommandLine = new StringBuffer();
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            udpCommandLine.append(" no udp  ");
        } else {
            udpCommandLine.append(" udp  ");
        }

        if(StringUtils.isNotBlank(srcPortString)){
            udpCommandLine.append(String.format(" src-port %s ",srcPortBuffer.toString()));
        }
        if(StringUtils.isNotBlank(dstPortString)){
            udpCommandLine.append(String.format(" dst-port %s ",dstPortString));
        }
        udpCommandLine.append(StringUtils.LF);
        return udpCommandLine.toString();
    }

    @Override
    public String generateTCP_UDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateOtherCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
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
            return String.format("service %s \n exit\n ",name);
        }
        serviceObjectCl.append(String.format("service %s \n",this.generateServiceObjectName(name,null,null)));
        if(StringUtils.isNotBlank(description)){
            serviceObjectCl.append(String.format(" description \"%s\" \n",description));
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

        serviceObjectCl.append("exit").append(StringUtils.LF);
        return serviceObjectCl.toString();
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
        String subServiceObjectName = null;
        if(StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus()) && CollectionUtils.isNotEmpty(serviceParamDTOList)){
            subServiceObjectName = String.format("%s_sub",name);
            serviceGroupObjectCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, subServiceObjectName, null, null, serviceParamDTOList, null, null, null));
        }

        if(CollectionUtils.isEmpty(serviceParamDTOList) && MapUtils.isEmpty(map)){
            return String.format("servgroup %s \n exit\n ",name);
        }
        serviceGroupObjectCl.append(String.format("servgroup %s \n",this.generateServiceObjectGroupName(name,null,null)));
        if(StringUtils.isNotBlank(description)){
            serviceGroupObjectCl.append(String.format(" description \"%s\" \n",description));
        }
        if(StringUtils.isNotBlank(subServiceObjectName)){
            serviceGroupObjectCl.append(String.format(" service %s \n",subServiceObjectName));
        }
        String servicePre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            servicePre = " no";
        }
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for (String serviceObjectName : serviceObjectNameRefArray) {
                serviceGroupObjectCl.append(servicePre).append(String.format(" service %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            logger.error("深信服不支持服务组嵌套:{}",name);
        }
        serviceGroupObjectCl.append("exit \n");
        return serviceGroupObjectCl.toString();
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("no servgroup %s \n",groupName);
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("no service %s \n",name);
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
        securityPolicyCl.append(String.format("acl-policy %s \n",name));
        if(moveSeatEnum != null){
            int moveSeatCode = moveSeatEnum.getCode();
            if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                if (StringUtils.isNotBlank(swapRuleNameId)) {
                    securityPolicyCl.append(String.format("move before %s\n", swapRuleNameId));
                }
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                if (StringUtils.isNotBlank(swapRuleNameId)) {
                    securityPolicyCl.append(String.format("move after %s\n", swapRuleNameId));
                }
            } else if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
                securityPolicyCl.append("move top\n");
            }
        }
        if (StringUtils.isNotBlank(description)) {
            securityPolicyCl.append(String.format(" description \"%s\"\n", description));
        }
        if(StringUtils.isNotBlank(logFlag) && "enable".equalsIgnoreCase(logFlag)){
            securityPolicyCl.append(" logging enable \n");
        }
        if(StringUtils.isNotBlank(newTimeObjectName)){
            securityPolicyCl.append(String.format(" schedule %s \n",newTimeObjectName));
        } else if(ArrayUtils.isNotEmpty(refTimeObject)){
            // 只能使用一个时间对象
            securityPolicyCl.append(String.format(" schedule  %s \n",refTimeObject[0]));
        } else {
            securityPolicyCl.append(String.format("schedule 全天\n"));
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
            securityPolicyCl.append(String.format(" src-zone %s \n",srcZoneName));
        }
        if(StringUtils.isNotBlank(dstZoneName)){
            securityPolicyCl.append(String.format(" dst-zone %s \n",dstZoneName));
        }

        // 本次生成的源地址
        if(CollectionUtils.isNotEmpty(newSrcIpAddressObjectNameList)){
            for(String addressObjectName:newSrcIpAddressObjectNameList){
                securityPolicyCl.append(String.format(" src-ipgroup %s \n", addressObjectName));
            }
        }
        //引用源地址对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            for (String srcRefIpAddressObjectName:srcRefIpAddressObject) {
                securityPolicyCl.append(String.format(" src-ipgroup %s ", srcRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        //引用源地址组对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            for (String srcRefIpAddressObjectGroupName:srcRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" src-ipgroup %s ", srcRefIpAddressObjectGroupName)).append(StringUtils.LF);
            }
        }

        //本次生成目的地址
        if(CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)){
            for(String addressObjectName:newDstIpAddressObjectNameList){
                securityPolicyCl.append(String.format(" dst-ipgroup %s \n", addressObjectName));
            }
        }
        // 引用目的地址对象
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            for (String dstRefIpAddressObjectName:dstRefIpAddressObject) {
                securityPolicyCl.append(String.format(" dst-ipgroup %s ", dstRefIpAddressObjectName)).append(StringUtils.LF);
            }
        }
        // 引用目的地址组对象
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){
            for (String dstRefIpAddressObjectGroupName:dstRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format(" dst-ipgroup %s ", dstRefIpAddressObjectGroupName)).append(StringUtils.LF);
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
        if(StringUtils.isNotBlank(action)){
            if("permit".equalsIgnoreCase(action)){
                securityPolicyCl.append(String.format(" action %s \n","pass"));
            } else {
                securityPolicyCl.append(String.format(" action %s \n","deny"));
            }
        }
        securityPolicyCl.append("exit");
        securityPolicyCl.append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("ip route %s/%s %s\n",ip,mask,"disable");
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description,Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("no ip route %s/%s %s\n",ip,mask,"disable");
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("ipv6 route %s/%s %s\n",ip,mask,"disable");
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("no ipv6 route %s/%s %s\n",ip,mask,"disable");
    }
}
