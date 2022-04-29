package com.abtnetworks.totems.vender.topsec.TOS_010;

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
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: zt
 * @Date: 2021/05/28/14:08
 * @Description:
 */
public class SecurityTopsec010Impl extends OverAllGeneratorAbstractBean {

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

    /**
     * 生成绝对计划时间对象命令行 define schedule add name ddd_TR_8661 cyctype yearcyc sdate 2021-04-21 stime 12:00:00 edate 2021-05-19 etime 12:00:00
     *
     * @param name                 时间标记字符串
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
        if (StringUtils.isEmpty(name)) {
            this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, map, args);
        }
        String timeNameCommandLine = generateTimeObjectName(name, map, null);
        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("define schedule add name %s ", timeNameCommandLine));
        String startTime = String.format("sdate %s stime %s", absoluteTimeParamDTO.getStartDate(), absoluteTimeParamDTO.getStartTime());
        String endTime = String.format("edate %s etime %s", absoluteTimeParamDTO.getEndDate(), absoluteTimeParamDTO.getEndTime());
        timeCommandLineBuffer.append(String.format("cyctype yearcyc %s %s ", startTime, endTime)).append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    /**
     * 生成周期计划时间对象命令行 define schedule add name zt_test1 cyctype weekcyc week 123 start 00:00 end 23:59
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
        if (StringUtils.isEmpty(name)) {
            this.createTimeObjectNameByPeriodic(periodicTimeParamDTO, map, args);
        }
        String timeNameCommandLine = generateTimeObjectName(name, map, null);

        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("define schedule add name %s cyctype", timeNameCommandLine));


        //周期计划类型 weekcyc  monthcyc
        if (periodicTimeParamDTO.getCycleType().equals("weekcyc")) {
            timeCommandLineBuffer.append(" weekcyc ");
        }

        if (ArrayUtils.isNotEmpty(periodicTimeParamDTO.getCycle())) {
            for (String date : periodicTimeParamDTO.getCycle()) {
                timeCommandLineBuffer.append(date);
            }
        }
        if (StringUtils.isNotBlank(periodicTimeParamDTO.getCycleStart())) {
            timeCommandLineBuffer.append(String.format(" start %s ", periodicTimeParamDTO.getCycleStart()));
        }
        if (StringUtils.isNotBlank(periodicTimeParamDTO.getCycleEnd())) {
            timeCommandLineBuffer.append(String.format(" end %s ", periodicTimeParamDTO.getCycleEnd()));
        }
        timeCommandLineBuffer.append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args){
        StringBuffer timeCommandLineBuffer = new StringBuffer("define schedule delete name "+timeFlag);
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
        if (ArrayUtils.isEmpty(singleIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer("define host");
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
            ipv4ArrayCommandLine.append(" add ");
        } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
            ipv4ArrayCommandLine.append(" modify ");
        }
        if (MapUtils.isEmpty(map)) {
            addressName = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
        } else {
            addressName = map.get("name").toString();
        }
        ipv4ArrayCommandLine.append("name " + addressName + " ipaddr ");
        for (String ip : singleIpArray) {
            ipv4ArrayCommandLine.append(ip);
        }
        ipv4ArrayCommandLine.append(StringUtils.LF);
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        if (ArrayUtils.isEmpty(rangIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuffer rangeIpCl = new StringBuffer();
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            if (MapUtils.isEmpty(map)) {
                addressName = createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
            } else {
                addressName = map.get("name").toString();
            }
            if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                rangeIpCl.append("define range add name " + addressName + " ip1 " + ipAddressRangeDTO.getStart() + " ip2 " + ipAddressRangeDTO.getEnd());
            } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                rangeIpCl.append("define range modify name " + addressName + " ip1 " + ipAddressRangeDTO.getStart() + " ip2 " + ipAddressRangeDTO.getEnd());
            }
        }
        rangeIpCl.append(StringUtils.LF);
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if (ArrayUtils.isNotEmpty(subnetIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                if (MapUtils.isEmpty(map)) {
                    addressName = createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args);
                } else {
                    addressName = map.get("name").toString();
                }
                if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                    subnetIpv4Cl.append("define subnet add name " + addressName + " ipaddr " + ipAddressSubnetIntDTO.getIp() + " mask " + TotemsIpUtils.getMaskMap(String.valueOf(ipAddressSubnetIntDTO.getMask())));
                } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                    subnetIpv4Cl.append("define subnet modify name " + addressName + " ipaddr " + ipAddressSubnetIntDTO.getIp() + " mask " + TotemsIpUtils.getMaskMap(String.valueOf(ipAddressSubnetIntDTO.getMask())));                }
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateMacAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, IpAddressMacDTO[] macIpArray, Map<String, Object> map, String[] args) throws Exception {
        String addressName = "";
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if (ArrayUtils.isNotEmpty(macIpArray)) {
            for (IpAddressMacDTO ipAddressMacDTO : macIpArray) {
                if (MapUtils.isEmpty(map)) {
                    addressName = createMacIpAddressName(ipAddressMacDTO, map, args);
                } else {
                    addressName = map.get("name").toString();
                }
                if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                    subnetIpv4Cl.append("define mac add name " + addressName + " macaddr" + ipAddressMacDTO.getMacAddress());
                } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                    subnetIpv4Cl.append("define mac modify name " + addressName + " macaddr" + ipAddressMacDTO.getMacAddress());
                }
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(singleIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer("define host");
        if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
            ipv4ArrayCommandLine.append(" add ");
        } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
            ipv4ArrayCommandLine.append(" modify ");
        }
        String ipAddressObjectNameBySingleIpArray = createIpAddressObjectNameBySingleIpArray(singleIpArray, map, args);
        ipv4ArrayCommandLine.append("name " + ipAddressObjectNameBySingleIpArray + " ipaddr '");
        for (String ip : singleIpArray) {
            ipv4ArrayCommandLine.append(ip + " ");
        }
        ipv4ArrayCommandLine.append("'").append(StringUtils.LF);
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(rangIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuilder rangeIpCl = new StringBuilder();
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            String ipAddressObjectNameByIpRange = createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args);
            if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                rangeIpCl.append("define range add name " + ipAddressObjectNameByIpRange + " ip1 " + ipAddressRangeDTO.getStart() + " ip2 " + ipAddressRangeDTO.getEnd());
            } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                rangeIpCl.append("define range modify name " + ipAddressObjectNameByIpRange + " ip1 " + ipAddressRangeDTO.getStart() + " ip2 " + ipAddressRangeDTO.getEnd());
            }
        }
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIpv6Cl = new StringBuilder();
        if (ArrayUtils.isNotEmpty(subnetIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIpArray) {
                String ipAddressObjectNameByIpMask = createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args);
                if (statusTypeEnum.getStatus().equals(StatusTypeEnum.ADD.getStatus())) {
                    subnetIpv6Cl.append("define subnet add name " + ipAddressObjectNameByIpMask + " ipaddr " + ipAddressSubnetIntDTO.getIp() + " mask " + ipAddressSubnetIntDTO.getMask());
                } else if (statusTypeEnum.getStatus().equals(StatusTypeEnum.MODIFY.getStatus())) {
                    subnetIpv6Cl.append("define subnet modify name " + ipAddressObjectNameByIpMask + " ipaddr " + ipAddressSubnetIntDTO.getIp() + " mask " + ipAddressSubnetIntDTO.getMask());
                }
            }
        }
        return subnetIpv6Cl.toString();
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum, String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("define group_address delete name %s", groupName);
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
        StringBuilder stringBuilder = new StringBuilder();
        if(null != ipTypeEnum && "2".equals(ipTypeEnum.getCode())){
            if (StringUtils.isBlank(delStr)) {
                stringBuilder.append("define ipv6_host delete name " + name).append(StringUtils.LF);
                return stringBuilder.toString();
            }
            // ipv6
            switch (delStr) {
                case "host":
                    stringBuilder.append("define ipv6_host delete name " + name).append(StringUtils.LF);
                    break;
                case "range":
                    stringBuilder.append("define ipv6_range delete name " + name).append(StringUtils.LF);
                    break;
                case "subnet":
                    stringBuilder.append("define ipv6_subnet delete name " + name).append(StringUtils.LF);
                    break;
                default:
                    stringBuilder.append("define ipv6_host delete name " + name).append(StringUtils.LF);
                    break;
            }
        }else{
            if (StringUtils.isBlank(delStr)) {
                stringBuilder.append("define host delete name " + name).append(StringUtils.LF);
                return stringBuilder.toString();
            }
            switch (delStr) {
                case "host":
                    stringBuilder.append("define host delete name " + name).append(StringUtils.LF);
                    break;
                case "range":
                    stringBuilder.append("define range delete name " + name).append(StringUtils.LF);
                    break;
                case "subnet":
                    stringBuilder.append("define subnet delete name " + name).append(StringUtils.LF);
                    break;
                default:
                    stringBuilder.append("define host delete name " + name).append(StringUtils.LF);
                    break;
            }
        }


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
        //单ip
        if (ArrayUtils.isNotEmpty(singleIpArray) && getArraysLength(singleIpArray,rangIpArray,subnetIntIpArray) <= 1) {
            if (StringUtils.isNotEmpty(name)) {
                map = new HashMap<>();
                map.put("name", name);
            }
            commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum, singleIpArray, map, null));
        }
        if (ArrayUtils.isNotEmpty(singleIpArray) && getArraysLength(singleIpArray,rangIpArray,subnetIntIpArray) > 1) {
            //生成地址组对象
            commandLine.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, ipTypeEnum, name, idStr, singleIpArray, rangIpArray,
                    subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, objectNameRefArray, null, description, attachStr, delStr, map, args));
        } else if (ArrayUtils.isEmpty(singleIpArray) && (getArraysLength(singleIpArray,rangIpArray,subnetIntIpArray) >= 2)) {
            //生成地址组对象
            commandLine.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, ipTypeEnum, name, idStr, singleIpArray, rangIpArray,
                    subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, objectNameRefArray, null, description, attachStr, delStr, map, args));
        } else {
            // ip范围地址对象
            if (ArrayUtils.isNotEmpty(rangIpArray)) {
                if (StringUtils.isNotEmpty(name)) {
                    map = new HashMap<>();
                    map.put("name", name);
                }
                for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                    commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum, new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }
            }
            // int子网地址对象
            if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                if (StringUtils.isNotEmpty(name)) {
                    map = new HashMap<>();
                    map.put("name", name);
                }
                for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                    commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, null, map, null));
                }
            }
            if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                if (StringUtils.isNotEmpty(name)) {
                    map = new HashMap<>();
                    map.put("name", name);
                }
                for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                    commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}, null, map, null));
                }
            }
        }
        commandLine.append(StringUtils.LF);
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
        ipAddressGroupCl.append("define group_address add name " + name + " member '");
        // 本次生成的地址对象
        if (CollectionUtils.isNotEmpty(newIpAddressObjectNameList)) {
            for (String objectName : newIpAddressObjectNameList) {
                ipAddressGroupCl.append(objectName + " ");
            }
        }
        //引用地址对象
        if (ArrayUtils.isNotEmpty(objectNameRefArray)) {
            for (String objectName : objectNameRefArray) {
                ipAddressGroupCl.append(objectName + " ");
            }
        }
        //引用地址组对象
        if (ArrayUtils.isNotEmpty(objectGroupNameRefArray)) {
            for (String objectGroupName : objectGroupNameRefArray) {
                ipAddressGroupCl.append(objectGroupName + " ");
            }
        }
        ipAddressGroupCl.deleteCharAt(ipAddressGroupCl.length()-1).toString();
        ipAddressGroupCl.append("'").append(StringUtils.LF);
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
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format("%s %s", srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format("%s %s", dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }

        StringBuffer tcpCommandLine = new StringBuffer("define service");
        if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())) {
            tcpCommandLine.append(" delete ");
        } else if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.MODIFY.getStatus())) {
            tcpCommandLine.append(" modify ");
        } else {
            tcpCommandLine.append(" add ");
        }
        tcpCommandLine.append("name "+map.get("name").toString()+" protocol 6");
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            tcpCommandLine.append(" port1 " + srcPortBuffer.toString());
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            if(dstPortBuffer.toString().contains(" ")){
                // 范围端口
                String[] dstPortBuffers =  dstPortBuffer.toString().split(" ");
                tcpCommandLine.append(String.format(" port %s port2 %s",dstPortBuffers[0],dstPortBuffers[1]));
            }else{
                // 单端口
                tcpCommandLine.append(String.format(" port %s",dstPortBuffer.toString()));
            }
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }

    /**
     * 生成 UDP 命令行  define service add name http8080 protocol 17 port1 8080
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
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format("%s %s", srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format("%s %s", dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }

        StringBuffer udpCommandLine = new StringBuffer("define service");
        if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())) {
            udpCommandLine.append(" delete ");
        } else if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.MODIFY.getStatus())) {
            udpCommandLine.append(" modify ");
        } else {
            udpCommandLine.append(" add ");
        }
        udpCommandLine.append("name "+map.get("name").toString()+" protocol 17");
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            udpCommandLine.append(" port1 " + srcPortBuffer.toString());
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            if(dstPortBuffer.toString().contains(" ")){
                // 范围端口
                String[] dstPortBuffers =  dstPortBuffer.toString().split(" ");
                udpCommandLine.append(String.format(" port %s port2 %s",dstPortBuffers[0],dstPortBuffers[1]));
            }else{
                // 单端口
                udpCommandLine.append(String.format(" port %s",dstPortBuffer.toString()));
            }
        }

        udpCommandLine.append(StringUtils.LF);
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
     * @param protocolAttachTypeArray 协议附件code值
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
        if (ArrayUtils.isEmpty(protocolAttachTypeArray)) {
            return StringUtils.EMPTY;
        }
        StringBuffer srcPortBuffer = new StringBuffer();
        StringBuffer dstPortBuffer = new StringBuffer();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format("%s %s", srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format("%s %s", dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        } else {
            return StringUtils.EMPTY;
        }

        StringBuffer icmpCommandLine = new StringBuffer("define service");
        if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())) {
            icmpCommandLine.append(" delete ");
        } else if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.MODIFY.getStatus())) {
            icmpCommandLine.append(" modify ");
        } else {
            icmpCommandLine.append(" add ");
        }
        icmpCommandLine.append("name "+map.get("name").toString()+" protocol 1");
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            icmpCommandLine.append(" port1 " + srcPortBuffer.toString());
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            if(dstPortBuffer.toString().contains(" ")){
                // 范围端口
                String[] dstPortBuffers =  dstPortBuffer.toString().split(" ");
                icmpCommandLine.append(String.format(" port %s port2 %s",dstPortBuffers[0],dstPortBuffers[1]));
            }else{
                // 单端口
                icmpCommandLine.append(String.format(" port %s",dstPortBuffer.toString()));
            }
        }
        icmpCommandLine.append(StringUtils.LF);
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
        if (map == null) {
            return StringUtils.EMPTY;
        }
        if (!map.containsKey("protocolNumber")) {
            return StringUtils.EMPTY;
        }

        StringBuffer otherCommandLine = new StringBuffer("define service");
        if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.DELETE.getStatus())) {
            otherCommandLine.append(" delete ");
        } else if (ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.MODIFY.getStatus())) {
            otherCommandLine.append(" modify ");
        } else {
            otherCommandLine.append(" add ");
        }
        otherCommandLine.append(" name "+map.get("name").toString()+" protocol " + map.get("protocolNumber"));
        otherCommandLine.append(StringUtils.LF);
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
        if (StringUtils.isNotBlank(name)) {
            return String.format("define service delete name %s %s", name, StringUtils.LF);
        } else {
            return null;
        }
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String name, Map<String, Object> map, String[] args) {
        if (StringUtils.isNotBlank(name)) {
            return String.format("define schedule delete name %s %s", name, StringUtils.LF);
        } else {
            return StringUtils.EMPTY;
        }
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
        if (CollectionUtils.isEmpty(serviceParamDTOList)) {
            return null;
        }
        map = new HashMap<>();
        if(StringUtils.isNotBlank(description)){
            map.put("description",description);
        }
        for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
            if(StringUtils.isEmpty(name)){
                name = this.createServiceObjectName(serviceParamDTO,map,args);
            }
            map.put("name",name);
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
        if (StringUtils.isNotBlank(name)) {
            serviceObjectCl.append(StringUtils.LF);
        }
        return serviceObjectCl.toString();
    }

    @Override
    public String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotBlank(groupName)) {
            return String.format("define group_service delete name %s %s", groupName, StringUtils.LF);
        } else {
            return null;
        }
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
        //生成服务对象
        List<String> newServiceObjectNameList = new ArrayList<String>();
        StringBuffer serviceObjectGroupCl = new StringBuffer();
        if (CollectionUtils.isNotEmpty(serviceParamDTOList)) {
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                String serviceObjectName = createServiceObjectName(serviceParamDTO, null, null);
                newServiceObjectNameList.add(serviceObjectName);
                ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                newService.add(serviceParamDTO);
                serviceObjectGroupCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null, null));
            }
        }
        serviceObjectGroupCl.append("define group_service add name ");
        if (StringUtils.isBlank(name)) {
            name = this.createServiceObjectGroupName(serviceParamDTOList, serviceObjectNameRefArray, serviceObjectGroupNameRefArray, null, null);
        }
        serviceObjectGroupCl.append(this.generateServiceObjectGroupName(name, null, null) + " member '");
        if (CollectionUtils.isNotEmpty(newServiceObjectNameList)) {
            for (String serviceObjectName : newServiceObjectNameList) {
                serviceObjectGroupCl.append(serviceObjectName+" ");
            }
        }
        if (ArrayUtils.isNotEmpty(serviceObjectNameRefArray)) {
            for (String serviceObjectName : serviceObjectNameRefArray) {
                serviceObjectGroupCl.append(serviceObjectName);
            }
        }
        if (ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)) {
            for (String serviceObjectGroupName : serviceObjectGroupNameRefArray) {
                serviceObjectGroupCl.append(serviceObjectGroupName);
            }
        }
        serviceObjectGroupCl.append("'").append(StringUtils.LF);
        return serviceObjectGroupCl.toString();
    }

    /*-------------------------------------------------------服务对象结束-----------------------------------------------------------------*/
    /*-------------------------------------------------------策略开始-----------------------------------------------------------------*/


    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("\"%s\"", name);
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
        List<String> newSrcIpAddressGroupNameList = new ArrayList<>();
        StringBuffer ipAddressGroupCl = new StringBuffer();
        if (ObjectUtils.isNotEmpty(srcIpDto)) {
            //生成src地址对象命令行
            if (ArrayUtils.isNotEmpty(srcIpDto.getSingleIpArray())) {
                if(srcIpDto.getSingleIpArray().length <= 1){
                    String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(srcIpDto.getSingleIpArray(), map, args);
                    newSrcIpAddressObjectNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), singleIpObjectName, null, srcIpDto.getSingleIpArray(), null, null, null, null,
                            null, null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newSrcIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), singleIpObjectName, null, srcIpDto.getSingleIpArray(), null, null, null, null,
                            null, null,null, null, null, null, map, args));
                }

            }
            if (ArrayUtils.isNotEmpty(srcIpDto.getRangIpArray())) {
                IpAddressRangeDTO[] rangIpArray = srcIpDto.getRangIpArray();
                if(rangIpArray.length <= 1){
                    for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                        newSrcIpAddressObjectNameList.add(createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(), ipAddressRangeDTO.getEnd(), map, args));
                    }
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, srcIpDto.getRangIpArray(), null, null, null, null,
                            null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newSrcIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, srcIpDto.getRangIpArray(), null, null, null, null,
                            null, null,null, null, null, map, args));
                }

            }
            if (ArrayUtils.isNotEmpty(srcIpDto.getSubnetIntIpArray())) {
                IpAddressSubnetIntDTO[] subnetIntIpArray = srcIpDto.getSubnetIntIpArray();
                if(subnetIntIpArray.length <= 1){
                    for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                        newSrcIpAddressObjectNameList.add(createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args));
                    }
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, null, srcIpDto.getSubnetIntIpArray(), null, null,
                            null, null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newSrcIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), null, null, null, null, srcIpDto.getSubnetIntIpArray(), null, null,
                            null, null,null, null, null, null, map, args));
                }

            }
            if (ArrayUtils.isNotEmpty(srcIpDto.getSubnetStrIpArray())) {
                if(srcIpDto.getSubnetStrIpArray().length <= 1){
                    String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, srcIpDto.getSubnetStrIpArray(), map, args);
                    newSrcIpAddressObjectNameList.add(subnetStrIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, srcIpDto.getSubnetStrIpArray(), null,
                            null, null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newSrcIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), singleIpObjectName, null, null, null, null, srcIpDto.getSubnetStrIpArray(), null,
                            null, null,null, null, null, null, map, args));
                }
            }
        }

        List<String> newDstIpAddressObjectNameList = new ArrayList<>();
        List<String> newDstIpAddressGroupNameList = new ArrayList<>();
        //生成dst地址对象命令行
        if (ObjectUtils.isNotEmpty(dstIpDto)) {
            //生成dst地址对象命令行
            if (ArrayUtils.isNotEmpty(dstIpDto.getSingleIpArray())) {
                if(dstIpDto.getSingleIpArray().length <= 1){
                    String singleIpObjectName = createIpAddressObjectNameBySingleIpArray(dstIpDto.getSingleIpArray(), map, args);
                    newDstIpAddressObjectNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, dstIpDto.getSingleIpArray(), null, null, null, null,
                            null, null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newDstIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, dstIpDto.getSingleIpArray(), null, null, null, null,
                            null, null,null, null, null, null, map, args));
                }

            }
            if (ArrayUtils.isNotEmpty(dstIpDto.getRangIpArray())) {
                if(dstIpDto.getRangIpArray().length <=1){
                    String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(dstIpDto.getRangIpArray(), map, args);
                    newDstIpAddressObjectNameList.add(rangeIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), rangeIpObjectName, null, null, dstIpDto.getRangIpArray(), null, null, null, null,
                            null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newDstIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, null, dstIpDto.getRangIpArray(), null, null, null, null,
                            null, null,null, null, null, map, args));
                }
            }
            if (ArrayUtils.isNotEmpty(dstIpDto.getSubnetIntIpArray())) {
                if(dstIpDto.getSubnetIntIpArray().length <= 1){
                    String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(dstIpDto.getSubnetIntIpArray(), null, map, args);
                    newDstIpAddressObjectNameList.add(subnetIntIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetIntIpObjectName, null, null, null, dstIpDto.getSubnetIntIpArray(), null, null,
                            null, null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newDstIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, null, null, dstIpDto.getSubnetIntIpArray(), null, null,
                            null, null,null, null, null, null, map, args));
                }

            }
            if (ArrayUtils.isNotEmpty(dstIpDto.getSubnetStrIpArray())) {
                if(dstIpDto.getSubnetStrIpArray().length <= 1){
                    String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null, dstIpDto.getSubnetStrIpArray(), map, args);
                    newDstIpAddressObjectNameList.add(subnetStrIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, dstIpDto.getSubnetStrIpArray(), null,
                            null, null, null, null, null, map, args));
                }else{
                    //临时获取地址对象组的名称 后期改
                    String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                    newDstIpAddressGroupNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), singleIpObjectName, null, null, null, null, dstIpDto.getSubnetStrIpArray(), null,
                            null, null, null,null, null, null, map, args));
                }
            }
        }

        // 处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        // 处理服务组对象
        List<String> newServiceGroupNameList = new ArrayList<>();
        String serviceObjectCommandLine = null;
        if (ArrayUtils.isNotEmpty(serviceParam)) {
            if(serviceParam.length <= 1){
                for (ServiceParamDTO serviceParamDTO : serviceParam) {
                    String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                    newServiceObjectNameList.add(serviceObjectName);
                    ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                    newService.add(serviceParamDTO);
                    serviceObjectCommandLine = this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, null, null);
                }
            }else{
                //临时获取地址对象组的名称 后期改
                String singleIpObjectName =String.valueOf(RandomUtils.nextInt());
                newServiceGroupNameList.add(singleIpObjectName);
                List<ServiceParamDTO> serviceParamDTOS = Arrays.asList(serviceParam);
                serviceObjectCommandLine = this.generateServiceObjectGroupCommandLine(statusTypeEnum, singleIpObjectName, null, null, serviceParamDTOS, null, null, null,null,null);
            }

        }
        StringBuffer securityPolicyCl = new StringBuffer();
        // 时间对象命令行
        if (StringUtils.isNotBlank(newTimeCommandLine)) {
            securityPolicyCl.append(newTimeCommandLine);
        }
        // 地址和服务对象命令行
        if (StringUtils.isNotBlank(ipAddressGroupCl.toString())) {
            securityPolicyCl.append(ipAddressGroupCl.toString());
        }
        if (StringUtils.isNotBlank(serviceObjectCommandLine)) {
            securityPolicyCl.append(serviceObjectCommandLine);
        }

        securityPolicyCl.append(StringUtils.LF);
        if (StringUtils.isNotBlank(id)) {
            securityPolicyCl.append("ID " + id);
        }
        // 策略命令行
        securityPolicyCl.append(" firewall policy add action " + action);

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
            securityPolicyCl.append(" srcarea " + srcZoneName);
        }
        if (StringUtils.isNotBlank(dstZoneName)) {
            securityPolicyCl.append(" dstarea " + dstZoneName);
        }

        if (StringUtils.isNotBlank(logFlag)) {
            securityPolicyCl.append(" log " + logFlag);
        }

        if(ifTimeObjectEmpty(newTimeObjectName,refTimeObject)){
            securityPolicyCl.append(" schedule '");
            // 本次生成的时间对象
            if (StringUtils.isNotBlank(newTimeObjectName)) {
                securityPolicyCl.append(newTimeObjectName);
            }
            //引用时间对象
            if (ArrayUtils.isNotEmpty(refTimeObject)) {
                for (String timeObjectName : refTimeObject) {
                    securityPolicyCl.append(timeObjectName);
                }
            }
            securityPolicyCl.append("'");
        }

        if(ifAllCollectionsEmpty(newSrcIpAddressObjectNameList,newSrcIpAddressGroupNameList,srcRefIpAddressObject,srcRefIpAddressObjectGroup)){
            securityPolicyCl.append(" src '");
            StringBuffer stringBufferSrc = new StringBuffer();
            // 本次生成的源地址
            if (CollectionUtils.isNotEmpty(newSrcIpAddressObjectNameList)) {
                for (String newSrcIpAddressObjectName : newSrcIpAddressObjectNameList) {
                    stringBufferSrc.append(newSrcIpAddressObjectName + " ");
                }
            }
            // 本次生成的源地址组
            if (CollectionUtils.isNotEmpty(newSrcIpAddressGroupNameList)) {
                for (String newSrcIpAddressObjectName : newSrcIpAddressGroupNameList) {
                    stringBufferSrc.append(newSrcIpAddressObjectName + " ");
                }
            }
            // 引用地址和地址组对象
            if (ArrayUtils.isNotEmpty(srcRefIpAddressObject)) {
                for (String srcRefIpAddressObjectName : srcRefIpAddressObject) {
                    stringBufferSrc.append(srcRefIpAddressObjectName + " ");
                }
            }
            if (ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)) {
                for (String srcRefIpAddressObjectGroupName : srcRefIpAddressObjectGroup) {
                    stringBufferSrc.append(srcRefIpAddressObjectGroupName + " ");
                }
            }
            securityPolicyCl.append(stringBufferSrc.toString().trim());
            securityPolicyCl.append("'");
        }
        if(ifAllCollectionsEmpty(newDstIpAddressObjectNameList,newDstIpAddressGroupNameList,dstRefIpAddressObject,dstRefIpAddressObjectGroup)){
            securityPolicyCl.append(" dst '");
            StringBuffer stringBufferDst = new StringBuffer();
            // 本地生成的目的地址
            if (CollectionUtils.isNotEmpty(newDstIpAddressObjectNameList)) {
                for (String newDstIpAddressObjectName : newDstIpAddressObjectNameList) {
                    stringBufferDst.append(newDstIpAddressObjectName + " ");
                }
            }
            // 本地生成的目的地址组
            if (CollectionUtils.isNotEmpty(newDstIpAddressGroupNameList)) {
                for (String newDstIpAddressObjectName : newDstIpAddressGroupNameList) {
                    stringBufferDst.append(newDstIpAddressObjectName + " ");
                }
            }
            if (ArrayUtils.isNotEmpty(dstRefIpAddressObject)) {
                for (String dstRefIpAddressObjectName : dstRefIpAddressObject) {
                    stringBufferDst.append(dstRefIpAddressObjectName + " ");
                }
            }
            if (ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)) {
                for (String dstRefIpAddressObjectGroupName : dstRefIpAddressObjectGroup) {
                    stringBufferDst.append(dstRefIpAddressObjectGroupName + " ");
                }
            }
            securityPolicyCl.append(stringBufferDst.toString().trim());
            securityPolicyCl.append("'");
        }
        if(ifAllCollectionsEmpty(newServiceObjectNameList,newServiceGroupNameList,refServiceObject,refServiceObjectGroup)){
            securityPolicyCl.append(" service '");
            StringBuffer stringBufferService = new StringBuffer();

            // 本次生成的服务对象
            if (CollectionUtils.isNotEmpty(newServiceObjectNameList)) {
                for (String newServiceObjectName : newServiceObjectNameList) {
                    stringBufferService.append(newServiceObjectName + " ");
                }
            }
            // 本次生成的服务组对象
            if (CollectionUtils.isNotEmpty(newServiceGroupNameList)) {
                for (String newServiceObjectName : newServiceGroupNameList) {
                    stringBufferService.append(newServiceObjectName + " ");
                }
            }
            //引用服务对象
            if (ArrayUtils.isNotEmpty(refServiceObject)) {
                for (String serviceObjectName : refServiceObject) {
                    stringBufferService.append(serviceObjectName + " ");
                }
            }
            //引用服务组对象
            if (ArrayUtils.isNotEmpty(refServiceObjectGroup)) {
                for (String serviceObjectGroupName : refServiceObjectGroup) {
                    stringBufferService.append(serviceObjectGroupName + " ");
                }
            }
            securityPolicyCl.append(stringBufferService.toString().trim());
            securityPolicyCl.append("'");
        }

        if(StringUtils.isNotBlank(groupName)){
            securityPolicyCl.append("group-name "+groupName);
        }
        securityPolicyCl.append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        StringBuffer stringBuffer = new StringBuffer("firewall policy delete id "+id);
        return stringBuffer.toString();
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
        StringBuilder stringBuilder = new StringBuilder("network route add");
        String family = "ipv4";
        if (StringUtils.isEmpty(ip)) {
            return StringUtils.EMPTY;
        }
        if(mask == null){
            mask = 32;
        }
        stringBuilder.append(" family "+family+" dst "+ip+" "+mask+" null 0");
        return stringBuilder.append(StringUtils.LF).toString();
    }

    /**
     * network route clean family ipv6
     * @param ip IP
     * @param mask 掩码
     * @param nextHop 下一跳
     * @param netDoor 出接口
     * @param distance 优先级
     * @param weight 权重
     * @param description 描述
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        if(mask == null){
            mask = 32;
        }
        if(StringUtils.isEmpty(ip)){
            stringBuilder.append("\"network route clean family ipv4\"\n");
        }else{
            stringBuilder.append("network route delete dst ").append(ip).append(" "+mask+" null 0\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder stringBuilder = new StringBuilder("network route add");
        String family = "ipv6";
        if (StringUtils.isEmpty(ip)) {
            return StringUtils.EMPTY;
        }
        if(mask == null){
            mask = 128;
        }
        stringBuilder.append(" family "+family+" dst "+ip+"/"+mask+" null 0");
        return stringBuilder.append(StringUtils.LF).toString();
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        if(mask == null){
            mask = 128;
        }
        if(StringUtils.isEmpty(ip)){
            stringBuilder.append("network route clean family ipv6\n");
        }else{
            stringBuilder.append("network route delete family ipv6 dst ").append(ip).append("/"+mask+" null 0\n");
        }
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

    private boolean ifTimeObjectEmpty(String string,String[] arr1){
        boolean flag = false;
        if(StringUtils.isNotBlank(string) || ArrayUtils.isNotEmpty(arr1)){
            flag = true;
        }
        return flag;
    }

    private boolean ifAllCollectionsEmpty(List<String> list1,List<String> list2,String[] arr1,String[] arr2){
        boolean flag = false;
        if(CollectionUtils.isNotEmpty(list1) || CollectionUtils.isNotEmpty(list2) || ArrayUtils.isNotEmpty(arr1) || ArrayUtils.isNotEmpty(arr2)){
            flag = true;
        }
        return flag;
    }


    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return StringUtils.EMPTY;
    }


    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return StringUtils.EMPTY;
    }


}
