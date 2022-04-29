package com.abtnetworks.totems.vender.abt.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.UnknownHostException;
import java.util.Map;

@Slf4j
public class SecurityAbtBListImpl extends OverAllGeneratorAbstractBean {

    private static String groupMember = "member  %s";
    private static String deleteBList = "blist delete %s";
    private static String addBList = "blist add %s age forever enable";

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

        logger.info("开始生成 abt blist security命令行------------------");
        StringBuilder commandLine = new StringBuilder("");
        //地址服务对象
        //源地址对象
        String srcAddressName = null;
        String srcCommandline = null;
        if (ObjectUtils.isNotEmpty(srcIpDto)) {
            srcAddressName = this.createIpAddressObjectNameByParamDTO(srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), null, null, null, null);
            srcCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcAddressName, null, srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(), null,
                    null, null, null, null, null, map, args);
        }
        //目的地址对象
        String dstAddressName = null;
        String dstCommandline = null;
        if (ObjectUtils.isNotEmpty(dstIpDto)) {
            dstAddressName = this.createIpAddressObjectNameByParamDTO(dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), null, null, null, null);
            dstCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(), dstAddressName, null, dstIpDto.getSingleIpArray(), dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(), dstIpDto.getSubnetStrIpArray(), null,
                    null, null, null, null, null, map, args);
        }
        if (StringUtils.isNotBlank(srcCommandline)) {
            commandLine.append(srcCommandline);
        }
        if (StringUtils.isNotBlank(dstCommandline)) {
            commandLine.append(dstCommandline);
        }
        commandLine.append("quit\n");
        log.info("生成abt blist：{} {}", StringUtils.LF, commandLine.toString());
        return commandLine.toString();
    }

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray, IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr, String delStr, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(singleIpArray)
                && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(fqdnArray)) {
            return StringUtils.EMPTY;
        }
        StringBuilder commandLine = new StringBuilder("");

        if (ArrayUtils.isNotEmpty(rangIpArray)) {
            log.info("abtnetworks blist 设备ip范围不为空,开始生成范围命令行...");
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                if (RuleIPTypeEnum.IP6.equals(ipTypeEnum)) {
                    commandLine.append(this.generateRangeIpV6CommandLine(statusTypeEnum, new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                } else {
                    commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum, new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }
            }
            log.info("范围命令行为：\n{}", commandLine.toString());
        }
        if (ArrayUtils.isNotEmpty(singleIpArray)) {
            for (String ip : singleIpArray) {
                if (RuleIPTypeEnum.IP6.equals(ipTypeEnum)) {
                    commandLine.append(this.generateSingleIpV6CommandLine(statusTypeEnum, new String[]{ip}, map, null));
                } else {
                    commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum, new String[]{ip}, map, null));
                }
            }
            log.info("单ip命令行为：\n{}", commandLine.toString());
        }
        if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                if (RuleIPTypeEnum.IP6.equals(ipTypeEnum)) {
                    commandLine.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, null, map, null));
                } else {
                    commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, null, map, null));
                }
            }
            log.info("网段int命令行为：\n{}", commandLine.toString());
        }
        if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                if (RuleIPTypeEnum.IP6.equals(ipTypeEnum)) {
                    commandLine.append(this.generateSubnetStrIpV6CommandLine(statusTypeEnum, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}));
                } else {
                    commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}, null, map, null));
                }
            }
            log.info("网段str命令行为：\n{}", commandLine.toString());
        }
        if (ArrayUtils.isNotEmpty(fqdnArray)) {
            for (String fqdnDto : fqdnArray) {
                if (RuleIPTypeEnum.IP4.equals(ipTypeEnum)) {
                    commandLine.append(this.generateFqdnIpV4CommandLine(statusTypeEnum, new String[]{fqdnDto}));
                }
                //ipv6 不支持域名
            }
            log.info("域名命令行为：\n{}", commandLine.toString());
        }
        return commandLine.toString();
    }

    public String generateFqdnIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] fqdnArray) {
        StringBuilder ipArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (String ip : fqdnArray) {
            if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                ipArrayCommandLine.append(ipPre)
                        .append(String.format(deleteBList, ip))
                        .append(StringUtils.LF);
            } else {
                ipArrayCommandLine.append(ipPre)
                        .append(String.format(addBList, ip))
                        .append(StringUtils.LF);
            }
        }
        return ipArrayCommandLine.toString();
    }

    public String generateSubnetStrIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray) throws UnknownHostException {
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (IpAddressSubnetStrDTO dto : subnetIpArray) {
            String ipv6Subnet = dto.getIp() + "/" + dto.getMask();
            String[] ipv6toIpList = IPUtil.ipv46toIpList(ipv6Subnet);
            for (String ipv6 : ipv6toIpList) {
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                    subnetStrArrayCommandLine.append(ipPre)
                            .append(String.format(deleteBList, ipv6))
                            .append(StringUtils.LF);
                } else {
                    subnetStrArrayCommandLine.append(ipPre)
                            .append(String.format(addBList, ipv6))
                            .append(StringUtils.LF);
                }

            }
        }
        return subnetStrArrayCommandLine.toString();
    }

    /**
     * Ip 范围
     *
     * @param statusTypeEnum
     * @param rangIpArray    X:X:X:X:X:X:X:X - X:X:X:X:X:X:X:X 例如：ABCD:EF01:2345:6789:ABCD:EF01:2345:6789 - ADCD:EF01:2125:6189:ABCD:EF01:2345:6789
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipv6RangeArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (IpAddressRangeDTO ipAddress : rangIpArray) {
            String[] ipv46toIpList = IPUtil.ipv46toIpList(ipAddress.getStart() + "-" + ipAddress.getEnd());
            for (String ipv46 : ipv46toIpList) {
                String str = "";
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                    ipv6RangeArrayCommandLine.append(ipPre).append(String.format(deleteBList, ipv46)).append(StringUtils.LF);
                } else {
                    ipv6RangeArrayCommandLine.append(ipPre).append(String.format(addBList, ipv46)).append(StringUtils.LF);
                }
            }
        }
        return ipv6RangeArrayCommandLine.toString();
    }

    /**
     * 单个 Ip
     * 192.168.215.192
     *
     * @param statusTypeEnum
     * @param singleIpArray  单个ip 集合
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (String ip : singleIpArray) {
            if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                ipArrayCommandLine.append(ipPre).append(String.format(deleteBList, ip)).append(StringUtils.LF);
            } else {
                ipArrayCommandLine.append(ipPre).append(String.format(addBList, ip)).append(StringUtils.LF);
            }
        }
        return ipArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * 范围 Ip
     * 192.68.2.23-192.68.2.190
     *
     * @param statusTypeEnum
     * @param rangIpArray    范围ip 集合
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipRangeArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (IpAddressRangeDTO ipAddress : rangIpArray) {
            String[] ipv46toIpList = IPUtil.ipv46toIpList(ipAddress.getStart() + "-" + ipAddress.getEnd());
            for (String ipv46 : ipv46toIpList) {
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                    ipRangeArrayCommandLine.append(ipPre).append(String.format(deleteBList, ipv46)).append(StringUtils.LF);
                } else {
                    ipRangeArrayCommandLine.append(ipPre).append(String.format(addBList, ipv46)).append(StringUtils.LF);
                }
            }
        }
        return ipRangeArrayCommandLine.toString();
    }

    /**
     * 子网 IP mask 掩码 int 数字类型
     *
     * @param statusTypeEnum
     * @param subnetIpArray  子网ip 集合
     * @param sub            sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (IpAddressSubnetIntDTO dto : subnetIpArray) {
            String ipv4Subnet = dto.getIp() + "/" + dto.getMask();
            String[] ipv46toIpList = IPUtil.ipv46toIpList(ipv4Subnet);
            for (String ipv46 : ipv46toIpList) {
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                    subnetIntArrayCommandLine.append(ipPre)
                            .append(String.format(deleteBList, ipv46))
                            .append(StringUtils.LF);
                } else {
                    subnetIntArrayCommandLine.append(ipPre)
                            .append(String.format(addBList, ipv46))
                            .append(StringUtils.LF);
                }
            }
        }
        return subnetIntArrayCommandLine.toString();
    }

    /**
     * 子网 IP mask 掩码 str ip类型
     *
     * @param statusTypeEnum
     * @param subnetIpArray  子网ip 集合
     * @param sub            sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (IpAddressSubnetStrDTO dto : subnetIpArray) {
            String ipv4Subnet = dto.getIp() + "/" + dto.getMask();
            String[] ipv4toIpList = IPUtil.ipv46toIpList(ipv4Subnet);
            for (String ipv4 : ipv4toIpList) {
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                    subnetStrArrayCommandLine.append(ipPre)
                            .append(String.format(deleteBList, ipv4))
                            .append(StringUtils.LF);
                } else {
                    subnetStrArrayCommandLine.append(ipPre)
                            .append(String.format(addBList, ipv4))
                            .append(StringUtils.LF);
                }
            }
        }
        return subnetStrArrayCommandLine.toString();
    }

    /**
     * 单个 ip
     * ABCD:EF01:2345:6789:ABCD:EF01:2345:6789
     *
     * @param statusTypeEnum
     * @param singleIpArray
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipv6ArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (String ip : singleIpArray) {
            if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                ipv6ArrayCommandLine.append(ipPre).append(String.format(deleteBList, ip)).append(StringUtils.LF);
            } else {
                ipv6ArrayCommandLine.append(ipPre).append(String.format(addBList, ip)).append(StringUtils.LF);
            }
        }
        return ipv6ArrayCommandLine.toString();
    }

    /**
     * 子网 前缀
     *
     * @param statusTypeEnum
     * @param subnetIpArray  子网ip 集合
     * @param sub            sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        for (IpAddressSubnetIntDTO dto : subnetIpArray) {
            String ipSubnetIpv6 = dto.getIp() + "/" + dto.getMask();
            String[] ipv46toIpList = IPUtil.ipv46toIpList(ipSubnetIpv6);
            for (String ipv46 : ipv46toIpList) {
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
                    subnetIntArrayCommandLine.append(ipPre).append(String.format(deleteBList, ipv46)).append(StringUtils.LF);
                } else {
                    subnetIntArrayCommandLine.append(ipPre).append(String.format(addBList, ipv46)).append(StringUtils.LF);
                }
            }
        }
        return subnetIntArrayCommandLine.toString();
    }

    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {

        if(ArrayUtils.isEmpty(objectNameRefArray) && ArrayUtils.isEmpty(objectGroupNameRefArray)){
            return this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum,name,null,singleIpArray, rangIpArray, subnetIntIpArray, subnetStrIpArray,  interfaceArray, fqdnArray,
                    objectNameRefArray,description,attachStr, delStr, map, args);
        }

        String newName = this.createIpAddressObjectNameByParamDTO(singleIpArray, rangIpArray, subnetIntIpArray, subnetStrIpArray,null,null,null,null);
        String newCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum,newName,null,singleIpArray, rangIpArray, subnetIntIpArray, subnetStrIpArray,  interfaceArray, fqdnArray,
                objectNameRefArray,description,attachStr, delStr, map, args);

        StringBuilder commandLine=new StringBuilder("");
        if(StringUtils.isNotEmpty(newCommandline)){
            commandLine.append(newCommandline);
        }
        name = name.replace(":","_");
        commandLine.append(String.format("address-group  %s",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for(String nameref:objectNameRefArray){
                commandLine.append(String.format(groupMember,nameref)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
            for(String nameref:objectGroupNameRefArray){
                commandLine.append(String.format(groupMember,nameref)).append(StringUtils.LF);
            }
        }
        if(StringUtils.isNotEmpty(newCommandline)){
            commandLine.append(String.format(groupMember,newName)).append(StringUtils.LF);
        }
        commandLine.append("quit").append(StringUtils.LF);

        if(ArrayUtils.isEmpty(objectNameRefArray) && ArrayUtils.isEmpty(objectGroupNameRefArray) &&StringUtils.isEmpty(newCommandline)){
            return StringUtils.EMPTY;
        }

        return commandLine.toString();
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "enable\nconfigure terminal \n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end \nsave config\n";
    }

    public static void main(String[] args) {
        String str = "blist add %s forever enable";
        System.out.println(String.format(str, "1.1.1.1"));
    }
}