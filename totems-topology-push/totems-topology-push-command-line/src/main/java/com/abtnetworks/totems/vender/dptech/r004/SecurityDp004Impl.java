package com.abtnetworks.totems.vender.dptech.r004;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: zt
 * @Date: 2021/06/04/10:09
 * @Description:
 */
public class SecurityDp004Impl extends OverAllGeneratorAbstractBean {


    @Override
    public String generatePreCommandline(Boolean isVsys,String vsysName, Map<String,Object> map , String[] args) {
        return "language-mode chinese \n conf-mode\n";
    }
    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "exit\n";
    }

    /*-------------------------------------------------------时间对象开始-----------------------------------------------------------------*/

    /**
     * 生成时间对象名称
     *
     * @param name
     * @return
     */
    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return name;
    }

    @Override
    public String createTimeObjectNameByAbsolute(AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String, Object> map, String[] args) {
        return "A_" + absoluteTimeParamDTO.hashCode();
    }

    /**
     * 生成绝对计划时间对象命令行
     *
     * @param name                 时间标记字符串  # HH:MM:SS  YYYY-MM-DD
     * @param attachStr            附加字符串
     * @param absoluteTimeParamDTO 绝对计划
     * @param map                  扩展参数 key-value String:Object类型
     * @param args                 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO,
                                                  Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isEmpty(absoluteTimeParamDTO.getStartTime()) || StringUtils.isEmpty(absoluteTimeParamDTO.getStartDate())
                || StringUtils.isEmpty(absoluteTimeParamDTO.getEndTime()) || StringUtils.isEmpty(absoluteTimeParamDTO.getEndDate())) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isEmpty(name)) {
            name = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, map, args);
        }
        StringBuffer timeCommandLineBuffer = new StringBuffer("time-object " + name );
        timeCommandLineBuffer.append("absolute ");
        timeCommandLineBuffer.append(" start " + absoluteTimeParamDTO.getStartTime() + " " + absoluteTimeParamDTO.getStartDate());
        timeCommandLineBuffer.append(" end " + absoluteTimeParamDTO.getEndTime() + " " + absoluteTimeParamDTO.getEndDate());
        timeCommandLineBuffer.append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    /**
     * 生成周期计划时间对象命令行
     *
     * @param name                 时间标记字符串
     * @param attachStr            附加字符串
     * @param periodicTimeParamDTO 周期计划  week格式为：1234567  start、end格式为：HH:MM（小时:分种）
     * @param map                  扩展参数 key-value String:Object类型
     * @param args                 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generatePeriodicTimeCommandLine(String name, String attachStr, PeriodicTimeParamDTO periodicTimeParamDTO,
                                                  Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    /**
     * 删除指定时间表 删除一个时间对象。如果该对象被规则引用，则无法删除。 define schedule delete name
     *
     * @param timeFlag
     * @return
     */
    @Override
    public String deleteAbsoluteTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args) {
        StringBuffer timeCommandLineBuffer = new StringBuffer("no time-object " + timeFlag);
        return timeCommandLineBuffer.toString();
    }
    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args) {
        StringBuffer timeCommandLineBuffer = new StringBuffer("no time-object " + timeFlag);
        return timeCommandLineBuffer.toString();
    }

    /*-------------------------------------------------------时间对象结束-----------------------------------------------------------------*/
    /*-------------------------------------------------------地址对象开始-----------------------------------------------------------------*/

    /**
     * ip地址对象命令行生成
     *
     * @param singleIpArray 单个ip 集合
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        if (ArrayUtils.isEmpty(singleIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        if (MapUtils.isEmpty(map)) {
            addressName = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
        } else {
            addressName = map.get("name").toString();
        }
        if(map.containsKey("description")){
            description = map.get("description").toString();
        }
        for (String ip : singleIpArray) {
            ipv4ArrayCommandLine.append(deleteFlag).append(String.format("address-object %s %s/32 \n" , addressName,ip));
        }
        if(StringUtils.isNotBlank(description)){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag).append("address-object " + addressName);
            descriptionCl.append(" description "+descriptionCl).append(StringUtils.LF);
            ipv4ArrayCommandLine.append(descriptionCl.toString());
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        if (ArrayUtils.isEmpty(rangIpArray)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        StringBuffer rangeIpCl = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            if (MapUtils.isEmpty(map)) {
                addressName = createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
            } else {
                addressName = map.get("name").toString();
            }
            rangeIpCl.append(deleteFlag).append(String.format("address-object %s " , addressName));
            rangeIpCl.append(" range " + ipAddressRangeDTO.getStart() + " " + ipAddressRangeDTO.getEnd()).append(StringUtils.LF);
            if(map.containsKey("description")){
                description = map.get("description").toString();
                StringBuilder descriptionCl = new StringBuilder(deleteFlag+"address-object " + addressName);
                descriptionCl.append(" description "+description).append(StringUtils.LF);
                rangeIpCl.append(descriptionCl.toString());
            }
        }
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        if (ArrayUtils.isNotEmpty(subnetIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                if (MapUtils.isEmpty(map)) {
                    addressName = createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args);
                } else {
                    addressName = map.get("name").toString();
                }
                subnetIpv4Cl.append(deleteFlag).append(String.format("address-object %s " ,addressName));
                if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                    subnetIpv4Cl.append(ipAddressSubnetIntDTO.getIp() + "/" + ipAddressSubnetIntDTO.getMask());
                } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                    subnetIpv4Cl.append("ip-exception " + ipAddressSubnetIntDTO.getIp() + "/" + ipAddressSubnetIntDTO.getMask());
                }
                subnetIpv4Cl.append(StringUtils.LF);
                if(map.containsKey("description")){
                    description = map.get("description").toString();
                    StringBuilder descriptionCl = new StringBuilder(deleteFlag+"address-object " + addressName);
                    descriptionCl.append(" description "+description).append(StringUtils.LF);
                    subnetIpv4Cl.append(descriptionCl.toString());
                }
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateMacAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        if (ArrayUtils.isNotEmpty(subnetIpArray)) {
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetIpArray) {
                if (MapUtils.isEmpty(map)) {
                    addressName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetStrDTO.getIp(), TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()), map, args);
                } else {
                    addressName = map.get("name").toString();
                }
                subnetIpv4Cl.append(deleteFlag).append(String.format("address-object %s " ,addressName));
                if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                    subnetIpv4Cl.append("ip-exception " + ipAddressSubnetStrDTO.getIp() + "/" + TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()));
                } else {
                    subnetIpv4Cl.append(ipAddressSubnetStrDTO.getIp() + "/" + TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()));
                }
                subnetIpv4Cl.append(StringUtils.LF);
                if(map.containsKey("description")){
                    description = map.get("description").toString();
                    StringBuilder descriptionCl = new StringBuilder(deleteFlag+"address-object " + addressName);
                    descriptionCl.append(" description "+description).append(StringUtils.LF);
                    subnetIpv4Cl.append(descriptionCl.toString());
                }
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        if (ArrayUtils.isEmpty(singleIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        if (MapUtils.isEmpty(map)) {
            addressName = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
        } else {
            addressName = map.get("name").toString();
        }
        if(map.containsKey("description")){
            description = map.get("description").toString();
        }
        ipv4ArrayCommandLine.append(deleteFlag).append("ipv6 address-object " + addressName);
        for (String ip : singleIpArray) {
            ipv4ArrayCommandLine.append(" ip-range start-ip " + ip + " end-ip " + ip);
        }
        if(StringUtils.isNotBlank(description)){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag).append("address-object " + addressName);
            descriptionCl.append(" description "+descriptionCl).append(StringUtils.LF);
            ipv4ArrayCommandLine.append(descriptionCl.toString());
        }
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        if (ArrayUtils.isEmpty(rangIpArray)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        StringBuffer rangeIpCl = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            if (MapUtils.isEmpty(map)) {
                addressName = createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
            } else {
                addressName = map.get("name").toString();
            }
            rangeIpCl.append(deleteFlag).append("ipv6 address-object " + addressName);
            rangeIpCl.append(" range " + ipAddressRangeDTO.getStart() + " " + ipAddressRangeDTO.getEnd());
            if(map.containsKey("description")){
                description = map.get("description").toString();
                StringBuilder descriptionCl = new StringBuilder(deleteFlag+"address-object " + addressName);
                descriptionCl.append(" description "+description).append(StringUtils.LF);
                rangeIpCl.append(descriptionCl.toString());
            }
        }
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        String description = "";
        String deleteFlag = StringUtils.EMPTY;
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        if (ArrayUtils.isNotEmpty(subnetIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                if (MapUtils.isEmpty(map)) {
                    addressName = createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args);
                } else {
                    addressName = map.get("name").toString();
                }
                subnetIpv4Cl.append(deleteFlag).append("ipv6 address-object " + addressName);
                if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                    subnetIpv4Cl.append("wildcard " + ipAddressSubnetIntDTO.getIp() + " " + ipAddressSubnetIntDTO.getMask());
                } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                    subnetIpv4Cl.append("ip-exception " + ipAddressSubnetIntDTO.getIp() + "/" + ipAddressSubnetIntDTO.getMask());
                }
                subnetIpv4Cl.append(StringUtils.LF);
                if(map.containsKey("description")){
                    description = map.get("description").toString();
                    StringBuilder descriptionCl = new StringBuilder(deleteFlag+"address-object " + addressName);
                    descriptionCl.append(" description "+description).append(StringUtils.LF);
                    subnetIpv4Cl.append(descriptionCl.toString());
                }
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum, String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 删除地址对象
     *
     * @param delStr 删除 符号
     * @param name   ip地址对象名称
     * @param map    扩展参数 key-value String:Object类型
     * @param args   扩展参数 String[] 类型
     * @return
     */
    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder stringBuilder = new StringBuilder("no address-object " + name);
        return stringBuilder.toString();
    }

    /**
     * 创建地址对象
     *
     * @param statusTypeEnum     状态类型
     * @param ipTypeEnum         IP枚举类型
     * @param name               地址对象名称
     * @param idStr              地址对象id
     * @param singleIpArray      单个ip
     * @param rangIpArray        范围ip
     * @param subnetIntIpArray   子网ip 掩码int
     * @param subnetStrIpArray   子网ip 掩码str
     * @param interfaceArray     接口集合
     * @param fqdnArray          域名集合
     * @param objectNameRefArray 引用对象名称集合
     * @param description        备注
     * @param attachStr          附加字符串
     * @param delStr             删除，失效标记
     * @param map                扩展参数 key-value String:Object类型
     * @param args               扩展参数 String[] 类型
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
        if (StringUtils.isNotEmpty(name)) {
            map = new HashMap<>();
            map.put("name", name);
        }
        if(StringUtils.isNotEmpty(description)){
            map.put("description", description);
        }
//        //单ip
//        if (ArrayUtils.isNotEmpty(singleIpArray) && getArraysLength(singleIpArray,rangIpArray,subnetIntIpArray) <= 1)  {
//            commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum, singleIpArray, map, null));
//            commandLine.append(StringUtils.LF);
//        }if (ArrayUtils.isNotEmpty(singleIpArray) && getArraysLength(singleIpArray,rangIpArray,subnetIntIpArray) > 1) {
//            //生成地址组对象
//            commandLine.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, ipTypeEnum, name, idStr, singleIpArray, rangIpArray,
//                    subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, objectNameRefArray, null, description, attachStr, delStr, map, args));
//        } else if (ArrayUtils.isEmpty(singleIpArray) && (getArraysLength(singleIpArray,rangIpArray,subnetIntIpArray) >= 2)) {
//            //生成地址组对象
//            commandLine.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, ipTypeEnum, name, idStr, singleIpArray, rangIpArray,
//                    subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, objectNameRefArray, null, description, attachStr, delStr, map, args));
//        } else {
//
//        }

        //生成地址对象
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum, singleIpArray, map, null));
        }
        // ip范围地址对象
        if (ArrayUtils.isNotEmpty(rangIpArray)) {
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum, new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                commandLine.append(StringUtils.LF);
            }
        }
        // int子网地址对象
        if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, null, map, null));
                commandLine.append(StringUtils.LF);
            }
        }
        if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}, null, map, null));
                commandLine.append(StringUtils.LF);
            }
        }

        return commandLine.toString();
    }

    @Override
    public String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return this.generateIpAddressObjectName(ipTypeEnum, groupName, map, args);
    }

    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isEmpty(name)) {
            name = this.generateIpAddressObjectGroupName(ipTypeEnum, name, map, args);
        }
        StringBuffer ipAddressGroupCl = new StringBuffer();
        // 生成地址对象命令行
        List<String> newIpAddressObjectNameList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(singleIpArray)) {
            if(singleIpArray.length <= 1){
                String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
                newIpAddressObjectNameList.add(singleIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, singleIpObjectName, null, singleIpArray, null, null, null, null,
                        null, null, null, null, null, map, args));
            }else{
                for (int i = 0; i < singleIpArray.length; i++) {
                    String[] strings = Arrays.copyOfRange(singleIpArray, i, i + 1);
                    String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(strings, map, args);
                    newIpAddressObjectNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, singleIpObjectName, null, strings, null, null, null, null,
                            null, null, null, null, null, map, args));
                }
            }
        }
        if (ArrayUtils.isNotEmpty(rangIpArray)) {
            if(rangIpArray.length <= 1){
                String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(rangIpArray, map, args);
                newIpAddressObjectNameList.add(rangeIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, rangeIpObjectName, null, null, rangIpArray, null, null, null, null,
                        null, null, null, null, map, args));
            }else{
                for (int i = 0; i < rangIpArray.length; i++) {
                    IpAddressRangeDTO[] ipAddressRangeDTOS = Arrays.copyOfRange(rangIpArray, i, i + 1);
                    String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(ipAddressRangeDTOS, map, args);
                    newIpAddressObjectNameList.add(rangeIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, rangeIpObjectName, null, null, ipAddressRangeDTOS, null, null, null, null,
                            null, null, null, null, map, args));
                }
            }

        }
        if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
            if(subnetIntIpArray.length <= 1){
                String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(subnetIntIpArray, null, map, args);
                newIpAddressObjectNameList.add(subnetIntIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIntIpObjectName, null, null, null, subnetIntIpArray, null, null,
                        null, null, null, null, null, map, args));
            }else{
                for (int i = 0; i < subnetIntIpArray.length; i++) {
                    IpAddressSubnetIntDTO[] ipAddressSubnetIntDTOS = Arrays.copyOfRange(subnetIntIpArray, i, i + 1);
                    String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(ipAddressSubnetIntDTOS, null, map, args);
                    newIpAddressObjectNameList.add(subnetIntIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIntIpObjectName, null, null, null, ipAddressSubnetIntDTOS, null, null,
                            null, null, null, null, null, map, args));
                }
            }
        }
        if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
            if(subnetStrIpArray.length <= 1){
                String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, subnetStrIpArray, map, args);
                newIpAddressObjectNameList.add(subnetStrIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetStrIpObjectName, null, null, null, null, subnetStrIpArray, null,
                        null, null, null, null, null, map, args));
            }else{
                for (int i = 0; i < subnetStrIpArray.length; i++) {
                    IpAddressSubnetStrDTO[] ipAddressSubnetStrDTOS = Arrays.copyOfRange(subnetStrIpArray, i, i + 1);
                    String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, ipAddressSubnetStrDTOS, map, args);
                    newIpAddressObjectNameList.add(subnetStrIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetStrIpObjectName, null, null, null, null, ipAddressSubnetStrDTOS, null,
                            null, null, null, null, null, map, args));
                }
            }
        }
        // 本次生成的地址对象
        if (CollectionUtils.isNotEmpty(newIpAddressObjectNameList)) {
            for (String objectName : newIpAddressObjectNameList) {
                ipAddressGroupCl.append("address-group " + name).append(" address-object " + objectName + StringUtils.LF);
            }
        }
        //引用地址对象
        if (ArrayUtils.isNotEmpty(objectNameRefArray)) {
            for (String objectName : objectNameRefArray) {
                ipAddressGroupCl.append("address-group " + name).append(" address-object " + objectName + StringUtils.LF);
            }
        }
        //引用地址组对象
        if (ArrayUtils.isNotEmpty(objectGroupNameRefArray)) {
            for (String objectGroupName : objectGroupNameRefArray) {
                ipAddressGroupCl.append("address-group " + name).append(" address-object " + objectGroupName + StringUtils.LF);
            }
        }
        //描述
        if(StringUtils.isNotBlank(description)){
            StringBuffer descriptionCl = new StringBuffer();
            descriptionCl.append("address-group " + name).append(" description "+descriptionCl);
            ipAddressGroupCl.append(descriptionCl.toString()+StringUtils.LF);
        }
        return ipAddressGroupCl.toString();
    }


    /*-------------------------------------------------------地址对象结束-----------------------------------------------------------------*/
    /*-------------------------------------------------------服务对象开始-----------------------------------------------------------------*/

    /**
     * 生成 TCP 命令行  define service add name http8080 protocol 6 port1 8080
     *
     * @param statusTypeEnum          状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray      源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray   源端口： Str 单个端口类型
     * @param srcRangePortArray       源端口：数字 范围端口类型
     * @param dstSinglePortArray      目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray   目的端口： Str 单个端口类型
     * @param dstRangePortArray       目的端口：数字 范围端口类型
     * @param timeOutArray            超时时间
     * @param objectNameRefArray      引用对象名称集合
     * @param map                     扩展参数 key-value String:Object类型
     * @param args                    扩展参数 String[] 类型
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
        String deleteFlag = StringUtils.EMPTY;
        if (MapUtils.isEmpty(map)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format("%s to %s", srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format("%s to %s", dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }
        StringBuffer tcpCommandLine = new StringBuffer(deleteFlag+"service-object " + map.get("name").toString() + " tcp ");
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            tcpCommandLine.append(" src-port " + srcPortBuffer.toString());
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            tcpCommandLine.append(" dst-port " + dstPortBuffer.toString());
        }
        tcpCommandLine.append(StringUtils.LF);
        if(map.containsKey("description")){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag).append("service-object " + map.get("name").toString()).append(" description "+map.get("description").toString());
            tcpCommandLine.append(descriptionCl.toString()).append(StringUtils.LF);
        }
        return tcpCommandLine.toString();
    }

    /**
     * 生成 UDP 命令行
     *
     * @param statusTypeEnum          状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray      源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray   源端口： Str 单个端口类型
     * @param srcRangePortArray       源端口：数字 范围端口类型
     * @param dstSinglePortArray      目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray   目的端口： Str 单个端口类型
     * @param dstRangePortArray       目的端口：数字 范围端口类型
     * @param timeOutArray            超时时间
     * @param objectNameRefArray      引用对象名称集合
     * @param map                     扩展参数 key-value String:Object类型
     * @param args                    扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray,
                                         Map<String, Object> map, String[] args) throws Exception {
        String deleteFlag = StringUtils.EMPTY;
        if (MapUtils.isEmpty(map)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }

        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format("%s to %s", srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format("%s to %s", dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }
        StringBuffer udpCommandLine = new StringBuffer(deleteFlag+"service-object " + map.get("name").toString() + " udp ");
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            udpCommandLine.append(" src-port " + srcPortBuffer.toString());
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            udpCommandLine.append(" dst-port " + dstPortBuffer.toString());
        }
        udpCommandLine.append(StringUtils.LF);
        if(map.containsKey("description")){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag).append("service-object " + map.get("name").toString()).append(" description "+map.get("description").toString());
            udpCommandLine.append(descriptionCl.toString()).append(StringUtils.LF);
        }
        return udpCommandLine.toString();
    }

    /**
     * 生成 TCP_UDP 命令行
     *
     * @param statusTypeEnum          状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray      源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray   源端口： Str 单个端口类型
     * @param srcRangePortArray       源端口：数字 范围端口类型
     * @param dstSinglePortArray      目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray   目的端口： Str 单个端口类型
     * @param dstRangePortArray       目的端口：数字 范围端口类型
     * @param timeOutArray            超时时间
     * @param objectNameRefArray      引用对象名称集合
     * @param map                     扩展参数 key-value String:Object类型
     * @param args                    扩展参数 String[] 类型
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
     * icmp 服务对象 define service add name http8080 protocol 1 port1 8080
     *
     * @param statusTypeEnum          状态类型枚举
     * @param protocolAttachTypeArray 协议附件type值
     * @param protocolAttachCodeArray 协议附件code值
     * @param srcSinglePortArray      源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray   源端口： Str 单个端口类型
     * @param srcRangePortArray       源端口：数字 范围端口类型
     * @param dstSinglePortArray      目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray   目的端口： Str 单个端口类型
     * @param dstRangePortArray       目的端口：数字 范围端口类型
     * @param timeOutArray            超时时间
     * @param objectNameRefArray      引用对象名称集合
     * @param map                     扩展参数 key-value String:Object类型
     * @param args                    扩展参数 String[] 类型
     * @return icmp type type-value [code min-code [max-code]] [timeout time-outvalue | timeout-day time-out-value]
     * @throws Exception
     */
    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                          Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                          Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                          String[] timeOutArray, String[] objectNameRefArray,
                                          Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(protocolAttachTypeArray) || ArrayUtils.isEmpty(protocolAttachCodeArray) || MapUtils.isEmpty(map)) {
            return StringUtils.EMPTY;
        }
        String deleteFlag = StringUtils.EMPTY;
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        String startType = protocolAttachTypeArray[0];
        String startCode = protocolAttachCodeArray[0];
        StringBuilder icmpCommandLine = new StringBuilder(deleteFlag+"service-object " + map.get("name").toString() + " icmp ");
        icmpCommandLine.append("type " + startType);
        icmpCommandLine.append(" code " + startCode);
        icmpCommandLine.append(StringUtils.LF);
        if(map.containsKey("description")){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag).append("service-object " + map.get("name").toString()).append(" description "+map.get("description").toString());
            icmpCommandLine.append(descriptionCl.toString()).append(StringUtils.LF);
        }
        return icmpCommandLine.toString();
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
     * 生成 其他协议 命令行
     *
     * @param statusTypeEnum          状态类型枚举
     * @param protocolAttachCodeArray 协议附加type值
     * @param protocolAttachCodeArray 协议附加code值
     * @param srcSinglePortArray      源端口： 数字 单个端口类型
     * @param srcSinglePortStrArray   源端口： Str 单个端口类型
     * @param srcRangePortArray       源端口：数字 范围端口类型
     * @param dstSinglePortArray      目的端口： 数字 单个端口类型
     * @param dstSinglePortStrArray   目的端口： Str 单个端口类型
     * @param dstRangePortArray       目的端口：数字 范围端口类型
     * @param timeOutArray            超时时间
     * @param objectNameRefArray      引用对象名称集合
     * @param map                     扩展参数 key-value String:Object类型
     * @param args                    扩展参数 String[] 类型
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
        if (map == null || !map.containsKey("name") || srcSinglePortArray.length <= 0) {
            return StringUtils.EMPTY;
        }
        String deleteFlag = StringUtils.EMPTY;
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        StringBuffer otherCommandLine = new StringBuffer(deleteFlag+"service-object " + map.get("name").toString() + " protocol ");
        otherCommandLine.append(srcSinglePortArray[0]);
        otherCommandLine.append(StringUtils.LF);
        if(map.containsKey("description")){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag).append("service-object " + map.get("name").toString()).append(" description "+map.get("description").toString());
            otherCommandLine.append(descriptionCl.toString()).append(StringUtils.LF);
        }
        return otherCommandLine.toString();
    }


    @Override
    public String generatePortRefStrCommandLine(String[] strRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }


    @Override
    public String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("\"%s\"", name);
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * 创建服务对象
     *
     * @param statusTypeEnum      状态类型
     * @param name                服务对象名称
     * @param id                  服务对象id
     * @param attachStr           附加字符串
     * @param serviceParamDTOList
     * @param map                 扩展参数 key-value String:Object类型
     * @param args                扩展参数 String[] 类型
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
        if (CollectionUtils.isEmpty(serviceParamDTOList) || StringUtils.isEmpty(name)) {
            return null;
        }
        map = new HashMap<>();
        if(StringUtils.isNotBlank(description)){
            map.put("description",description);
        }
        for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
            if(StringUtils.isEmpty(name)){
                name = this.createServiceObjectName(serviceParamDTO,map,args);
                map.put("name",name);
            }
            if (ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                serviceObjectCl.append(this.generateTCPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, map, null));
            } else if (ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                serviceObjectCl.append(this.generateUDPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, map, null));
            } else if (ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                serviceObjectCl.append(this.generateICMPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, map, null));
            } else {
                serviceObjectCl.append(this.generateOtherCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, map, null));
            }
        }
        return serviceObjectCl.toString();
    }

    @Override
    public String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * 创建服务对象组 命令行
     *
     * @param statusTypeEnum                 状态类型
     * @param name                           服务组名称
     * @param id                             服务组id
     * @param attachStr                      附加Str字符串
     * @param serviceParamDTOList            服务DTO
     * @param description                    备注
     * @param serviceObjectNameRefArray      引用服务对象名称
     * @param serviceObjectGroupNameRefArray 引用服务组对象名称
     * @param map                            扩展参数 key-value String:Object类型
     * @param args                           扩展参数 String[] 类型
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
        //生成服务组对象
        List<String> newServiceObjectNameList = new ArrayList<String>();
        if (StringUtils.isBlank(name)) {
            name = this.createServiceObjectGroupName(serviceParamDTOList, serviceObjectNameRefArray, serviceObjectGroupNameRefArray, null, null);
        }
        String deleteFlag = StringUtils.EMPTY;
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.DELETE.getStatus())) {
            deleteFlag = "no ";
        }
        StringBuffer serviceObjectGroupCl = new StringBuffer();
        if (CollectionUtils.isNotEmpty(serviceParamDTOList)) {
            if(serviceParamDTOList.size()<=1){
                ServiceParamDTO serviceParamDTO = serviceParamDTOList.get(0);
                String serviceObjectName = createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                newService.add(serviceParamDTO);
                serviceObjectGroupCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null, null));
            }else{
                for (int i = 0; i < serviceParamDTOList.size(); i++) {
                    ServiceParamDTO serviceParamDTO = serviceParamDTOList.get(i);
                    String serviceObjectName = createServiceObjectName(serviceParamDTO, null, null);
                    newServiceObjectNameList.add(serviceObjectName);
                    ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                    newService.add(serviceParamDTO);
                    serviceObjectGroupCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null, null));
                }
            }

        }
        if (CollectionUtils.isNotEmpty(newServiceObjectNameList)) {
            for (String serviceObjectName : newServiceObjectNameList) {
                serviceObjectGroupCl.append(deleteFlag+"group-service " + name ).append(" usr-service " + serviceObjectName+StringUtils.LF);
            }
        }
        if (ArrayUtils.isNotEmpty(serviceObjectNameRefArray)) {
            for (String serviceObjectName : serviceObjectNameRefArray) {
                serviceObjectGroupCl.append(deleteFlag+"group-service " + name ).append(" usr-service " + serviceObjectName+StringUtils.LF);
            }
        }
        if (ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)) {
            for (String serviceObjectGroupName : serviceObjectGroupNameRefArray) {
                serviceObjectGroupCl.append(deleteFlag+"group-service " + name ).append(" usr-service " + serviceObjectGroupName+StringUtils.LF);
            }
        }
        serviceObjectGroupCl.append(deleteFlag+"service-group " + name + StringUtils.LF);
        if(StringUtils.isNotEmpty(description)){
            StringBuilder descriptionCl = new StringBuilder();
            descriptionCl.append(deleteFlag+"service-group "+name + " description "+ description);
            serviceObjectGroupCl.append(descriptionCl.toString());
        }
        return serviceObjectGroupCl.toString();
    }

    /*-------------------------------------------------------服务对象结束-----------------------------------------------------------------*/
    /*-------------------------------------------------------策略开始-----------------------------------------------------------------*/


    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    @Override
    public String createSecurityPolicyName(List<String> list, Map<String, Object> map, String[] args) {
        return super.createSecurityPolicyName(list, map, args);
    }

    /**
     * 生成安全策略 命令行
     *
     * @param statusTypeEnum             状态类型
     * @param groupName                  策略集
     * @param name                       策略名称
     * @param id                         策略id
     * @param action                     动作
     * @param description                备注说明
     * @param logFlag                    开启日志
     * @param ageingTime                 老化时间
     * @param refVirusLibrary            引用病毒库
     * @param moveSeatEnum               移动位置
     * @param swapRuleNameId             交换位置的规则名或id
     * @param srcIpDto                   源ip
     * @param dstIpDto                   目的ip
     * @param serviceParam               服务（源端口，目的端口，协议）
     * @param absoluteTimeParamDTO       绝对时间对象
     * @param periodicTimeParamDTO       周期时间对象
     * @param srcZone                    源域
     * @param dstZone                    目的域
     * @param inInterface                进接口
     * @param outInterface               出接口
     * @param srcRefIpAddressObject      引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject      引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject           引用服务对象
     * @param refServiceObjectGroup      引用服务组对象
     * @param refTimeObject              引用时间对象
     * @param map                        扩展参数 key-value String:Object类型
     * @param args                       扩展参数 String[] 类型
     * @return
     */
    @Override
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                    String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                    String swapRuleNameId, IpAddressParamDTO srcIpDto, IpAddressParamDTO dstIpDto, ServiceParamDTO[] serviceParam,
                                                    AbsoluteTimeParamDTO absoluteTimeParamDTO, PeriodicTimeParamDTO periodicTimeParamDTO,
                                                    ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                    String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                    String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                    String[] refServiceObject, String[] refServiceObjectGroup,
                                                    String[] refTimeObject,
                                                    Map<String, Object> map, String[] args) throws Exception {
        // 处理时间对象
        String newTimeObjectName = null;
        String newTimeCommandLine = null;
        if (ObjectUtils.isNotEmpty(absoluteTimeParamDTO)) {
            newTimeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, map, args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName, null, absoluteTimeParamDTO, map, null);
        } else if (ObjectUtils.isNotEmpty(periodicTimeParamDTO)) {
            newTimeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO, map, args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(newTimeObjectName, null, periodicTimeParamDTO, map, null);
        }
        // 处理地址对象
        List<String> newSrcIpAddressObjectNameList = new ArrayList<>();
        // 处理地址组对象
        List<String> newSrcIpAddressGroupNameList = new ArrayList<>();
        StringBuffer ipAddressGroupCl = new StringBuffer();
        if (ObjectUtils.isNotEmpty(srcIpDto)) {
            //生成src地址对象命令行
            if (ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())) {
                if(srcIpDto.getSingleIpArray().length > 1){
                    String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(srcIpDto.getSingleIpArray(), map, args);
                    newSrcIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), singleIpObjectName, null, srcIpDto.getSingleIpArray(), null, null, null, null,
                            null, null, null, null, null, map, args));
                }else{
                    String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(srcIpDto.getSingleIpArray(), map, args);
                    newSrcIpAddressObjectNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), singleIpObjectName, null, srcIpDto.getSingleIpArray(), null, null, null, null,
                            null, null, null, null, null, map, args));
                }
            }
            if (ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())) {
                if(srcIpDto.getRangIpArray().length > 1){
                    for (IpAddressRangeDTO ipAddressRangeDTO : srcIpDto.getRangIpArray()) {
                        newSrcIpAddressGroupNameList.add(createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args));
                    }
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, srcIpDto.getRangIpArray(), null, null, null, null,
                            null, null, null, null, map, args));
                }else{
                    for (IpAddressRangeDTO ipAddressRangeDTO : srcIpDto.getRangIpArray()) {
                        newSrcIpAddressObjectNameList.add(createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args));
                    }
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, srcIpDto.getRangIpArray(), null, null, null, null,
                            null, null, null, null, map, args));
                }
            }
            if (ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())) {
                if(srcIpDto.getSubnetIntIpArray().length > 1){
                    for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : srcIpDto.getSubnetIntIpArray()) {
                        newSrcIpAddressGroupNameList.add(createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args));
                    }
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, null, srcIpDto.getSubnetIntIpArray(), null, null,
                            null, null, null, null, null, map, args));
                }else{
                    for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : srcIpDto.getSubnetIntIpArray()) {
                        newSrcIpAddressObjectNameList.add(createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args));
                    }
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, null, srcIpDto.getSubnetIntIpArray(), null, null,
                            null, null, null, null, null, map, args));
                }
            }
            if (ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())) {
                if(srcIpDto.getSubnetStrIpArray().length > 1){
                    String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, srcIpDto.getSubnetStrIpArray(), map, args);
                    newSrcIpAddressGroupNameList.add(subnetStrIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, srcIpDto.getSubnetStrIpArray(), null,
                            null, null, null, null, null, map, args));
                }else{
                    String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, srcIpDto.getSubnetStrIpArray(), map, args);
                    newSrcIpAddressObjectNameList.add(subnetStrIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, srcIpDto.getSubnetStrIpArray(), null,
                            null, null, null, null, null, map, args));
                }
            }
        }
        //地址对象
        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        //地址组对象
        List<String> newDstIpAddressObjectGroupNameList = new ArrayList<>();
        //生成dst地址对象命令行
        if (ObjectUtils.isNotEmpty(dstIpDto)) {
            //生成dst地址对象命令行
            if (ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray()) &&dstIpDto.getSingleIpArray().length <= 1 ) {
                String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(dstIpDto.getSingleIpArray(), map, args);
                newDstIpAddressObjectNameList.add(singleIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, dstIpDto.getSingleIpArray(), null, null, null, null,
                        null, null, null, null, null, map, args));
            }else{
                String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(dstIpDto.getSingleIpArray(), map, args);
                newDstIpAddressObjectGroupNameList.add(singleIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, dstIpDto.getSingleIpArray(), null, null, null, null,
                        null, null, null, null, null, map, args));
            }
            if (ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray()) && dstIpDto.getRangIpArray().length <= 1) {
                String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(dstIpDto.getRangIpArray(), map, args);
                newDstIpAddressObjectNameList.add(rangeIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), rangeIpObjectName, null, null, dstIpDto.getRangIpArray(), null, null, null, null,
                        null, null, null, null, map, args));
            }else{
                String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(dstIpDto.getRangIpArray(), map, args);
                newDstIpAddressObjectGroupNameList.add(rangeIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), rangeIpObjectName, null, null, dstIpDto.getRangIpArray(), null, null, null, null,
                        null, null, null, null, map, args));
            }
            if (ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray()) && dstIpDto.getSubnetIntIpArray().length <= 1) {
                String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(dstIpDto.getSubnetIntIpArray(), null, map, args);
                newDstIpAddressObjectNameList.add(subnetIntIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetIntIpObjectName, null, null, null, dstIpDto.getSubnetIntIpArray(), null, null,
                        null, null, null, null, null, map, args));
            }else{
                String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(dstIpDto.getSubnetIntIpArray(), null, map, args);
                newDstIpAddressObjectGroupNameList.add(subnetIntIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetIntIpObjectName, null, null, null, dstIpDto.getSubnetIntIpArray(), null, null,
                        null, null, null, null, null, map, args));
            }
            if (ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray()) && dstIpDto.getSubnetStrIpArray().length <= 1) {
                String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, dstIpDto.getSubnetStrIpArray(), map, args);
                newDstIpAddressObjectNameList.add(subnetStrIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, dstIpDto.getSubnetStrIpArray(), null,
                        null, null, null, null, null, map, args));
            }else{
                String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, dstIpDto.getSubnetStrIpArray(), map, args);
                newDstIpAddressObjectGroupNameList.add(subnetStrIpObjectName);
                ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, dstIpDto.getSubnetStrIpArray(), null,
                        null, null, null, null, null, map, args));
            }
        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        // 处理服务组对象
        List<String> newServiceGroupNameList = new ArrayList<>();
        String serviceObjectCommandLine = null;
        if (ArrayUtils.isNotEmpty(serviceParam)) {
            List<ServiceParamDTO> serviceParamDTOS = Arrays.asList(serviceParam);
            if(serviceParam.length > 1){
                //服务组
                String serviceObjectGroupName = this.createServiceObjectGroupName(serviceParamDTOS, refServiceObject, refServiceObjectGroup, map, args);
                newServiceGroupNameList.add(serviceObjectGroupName);
                serviceObjectCommandLine = this.generateServiceObjectGroupCommandLine(statusTypeEnum, serviceObjectGroupName, null, null, serviceParamDTOS, null, null, null,map,args);
            }else{
                //服务
                String serviceObjectName = this.createServiceObjectName(serviceParamDTOS.get(0), null, null);
                newServiceObjectNameList.add(serviceObjectName);
                serviceObjectCommandLine = this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, serviceParamDTOS, null, null, null);
            }
        }
        StringBuffer zoneCl = new StringBuffer();
        StringBuffer finalSecurityPolicyCl = new StringBuffer();
        // 时间对象命令行
        if (StringUtils.isNotBlank(newTimeCommandLine)) {
            finalSecurityPolicyCl.append(newTimeCommandLine);
        }
        // 地址和服务对象命令行
        if (StringUtils.isNotBlank(ipAddressGroupCl.toString())) {
            finalSecurityPolicyCl.append(ipAddressGroupCl.toString());
        }
        if (StringUtils.isNotBlank(serviceObjectCommandLine)) {
            finalSecurityPolicyCl.append(serviceObjectCommandLine);
        }

        finalSecurityPolicyCl.append(StringUtils.LF);

        //开始拼接策略命令行
        if (StringUtils.isEmpty(name)) {
            //暂时空
            name = this.createSecurityPolicyName(null,map,args);
        }

        zoneCl.append("security-policy " + name+" ");


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
        if (StringUtils.isNotBlank(srcZoneName)) {
            finalSecurityPolicyCl.append(zoneCl.toString()).append("src-zone " + srcZoneName).append(StringUtils.LF);
        }
        if (StringUtils.isNotBlank(dstZoneName)) {
            finalSecurityPolicyCl.append(zoneCl.toString()).append("dst-zone " + dstZoneName).append(StringUtils.LF);
        }

        // 本次生成的源地址对象
        if (CollectionUtils.isNotEmpty(newSrcIpAddressObjectNameList)) {
            for (String newSrcIpAddressObjectName : newSrcIpAddressObjectNameList) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("src-ip " + newSrcIpAddressObjectName + StringUtils.LF);
            }
        }
        // 本次生成的源地址组对象
        if (CollectionUtils.isNotEmpty(newSrcIpAddressGroupNameList)) {
            for (String newSrcIpAddressObjectName : newSrcIpAddressGroupNameList) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("src-ip-group " + newSrcIpAddressObjectName + StringUtils.LF);
            }
        }
        // 引用地址对象
        if (ArrayUtils.isNotEmpty(srcRefIpAddressObject)) {
            for (String srcRefIpAddressObjectName : srcRefIpAddressObject) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("src-ip " + srcRefIpAddressObjectName + StringUtils.LF);
            }
        }
        // 引用地址组对象
        if (ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)) {
            for (String srcRefIpAddressObjectGroupName : srcRefIpAddressObjectGroup) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("src-ip-group " + srcRefIpAddressObjectGroupName + StringUtils.LF);
            }
        }

        // 本地生成的目的地址
        if (CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)) {
            for (String newDstIpAddressObjectName : newDstIpAddressObjectNameList) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("dst-ip " + newDstIpAddressObjectName + StringUtils.LF);
            }
        }
        // 本地生成的目的地址组
        if (CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)) {
            for (String newDstIpAddressObjectName : newDstIpAddressObjectNameList) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("dst-ip-group " + newDstIpAddressObjectName + StringUtils.LF);
            }
        }
        if (ArrayUtils.isNotEmpty(dstRefIpAddressObject)) {
            for (String dstRefIpAddressObjectName : dstRefIpAddressObject) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("dst-ip " + dstRefIpAddressObjectName + StringUtils.LF);
            }
        }
        if (ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)) {
            for (String dstRefIpAddressObjectGroupName : dstRefIpAddressObjectGroup) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("dst-ip-group " + dstRefIpAddressObjectGroupName + StringUtils.LF);
            }
        }

        // 本次生成的服务对象
        if (CollectionUtils.isNotEmpty(newServiceObjectNameList)) {
            for (String newServiceObjectName : newServiceObjectNameList) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("usr-service " + newServiceObjectName + StringUtils.LF);
            }
        }

        // 本次生成的服务组对象
        if (CollectionUtils.isNotEmpty(newServiceObjectNameList)) {
            for (String newServiceObjectName : newServiceObjectNameList) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("group-service " + newServiceObjectName + StringUtils.LF);
            }
        }
        //引用服务对象
        if (ArrayUtils.isNotEmpty(refServiceObject)) {
            for (String serviceObjectName : refServiceObject) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("usr-service " + serviceObjectName + StringUtils.LF);
            }
        }
        //引用服务组对象
        if (ArrayUtils.isNotEmpty(refServiceObjectGroup)) {
            for (String serviceObjectGroupName : refServiceObjectGroup) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("group-service " + serviceObjectGroupName + StringUtils.LF);
            }
        }

        // 本次生成的时间对象
        if (StringUtils.isNotBlank(newTimeObjectName)) {
            finalSecurityPolicyCl.append(zoneCl.toString()).append("time-name " + newTimeObjectName + StringUtils.LF);
        }
        //引用时间对象
        if (ArrayUtils.isNotEmpty(refTimeObject)) {
            for (String timeObjectName : refTimeObject) {
                finalSecurityPolicyCl.append(zoneCl.toString()).append("time-name " + timeObjectName + StringUtils.LF);
            }
        }
        //动作
        if(StringUtils.isNotEmpty(action)){
            finalSecurityPolicyCl.append(zoneCl.toString()).append("action " + action + StringUtils.LF);
        }
        return finalSecurityPolicyCl.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        if (StringUtils.isBlank(name)){
            return StringUtils.EMPTY;
        }
        StringBuffer command = new StringBuffer();
        command.append(String.format("no pf-policy %s \n", name));
        return command.toString();
    }

    @Override
    public String generatePolicyGroupCommandLine(String policyGroupName, String description, Map<String, Object> map, String[] args) {
        if (StringUtils.isBlank(policyGroupName)) {
            return StringUtils.LF;
        }
        StringBuilder policyGroupCl = new StringBuilder("firewall group-policy add name " + policyGroupName);
        policyGroupCl.append(StringUtils.LF);
        return policyGroupCl.toString();
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isEmpty(ip)) {
            return StringUtils.EMPTY;
        }
        if(mask == null){
            mask = 32;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ip route "+ip+"/"+mask+" null0 \n");
        return stringBuilder.toString();
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isEmpty(ip)) {
            return StringUtils.EMPTY;
        }
        if(mask == null){
            mask = 32;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("no ip route ").append(ip).append("/"+mask+" null0\n");
        return stringBuilder.toString();
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isEmpty(ip)) {
            return StringUtils.EMPTY;
        }
        if(mask == null){
            mask = 128;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ipv6 route ").append(ip).append("/"+mask+" null0\n");
        return stringBuilder.toString();
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isEmpty(ip)) {
            return StringUtils.EMPTY;
        }
        if(mask == null){
            mask = 128;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("no ipv6 route ").append(ip).append("/"+mask+" null0\n");
        return stringBuilder.toString();
    }

    public int getArraysLength( String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                IpAddressSubnetIntDTO[] subnetIntIpArray){
        int single = 0;
        int range = 0;
        int subnet = 0;
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            single = singleIpArray.length;
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            range = rangIpArray.length;
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            subnet = subnetIntIpArray.length;
        }
        return single+range+subnet;
    }

}
