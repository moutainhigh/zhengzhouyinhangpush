package com.abtnetworks.totems.vender.abt.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.AbsoluteTimeParamDTO;
import com.abtnetworks.totems.command.line.dto.InterfaceParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.dto.PeriodicTimeParamDTO;
import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.dto.ZoneParamDTO;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class SecurityAbtImpl extends OverAllGeneratorAbstractBean {


    private static String defaultTcpPort = "tcp dst-port %s %s ";
    private static String defaultUdpPort = "udp dst-port %s %s ";
    private static String defaultPort = "src-port 0 65535";
    private static String groupMember = "member  %s";

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
        logger.info("开始生成 abt security命令行------------------");
        StringBuilder commandLine=new StringBuilder("");
        /*if(ObjectUtils.isEmpty(statusTypeEnum) || !statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.ADD.getStatus())){
            return null;
        }*/

        //地址服务对象
        //源地址对象
        String srcAddressName = null;
        String srcCommandline = null;
        if(ObjectUtils.isNotEmpty(srcIpDto)){
            srcAddressName = this.createIpAddressObjectNameByParamDTO(srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),null,null,null,null);
            srcCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(),srcAddressName,null, srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),null,
                    null, null, null, null, null, map, args);
        }
        //目的地址对象
        String dstAddressName = null;
        String dstCommandline = null;
        if(ObjectUtils.isNotEmpty(dstIpDto)){
            dstAddressName = this.createIpAddressObjectNameByParamDTO(dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),null,null,null,null);
            dstCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, dstIpDto.getIpTypeEnum(),dstAddressName,null, dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(), dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),null,
                    null, null, null, null, null, map, args);
        }
        //时间对象
        String newTimeObjectName = null;
        String newTimeCommandLine = null;
        if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            newTimeObjectName =createTimeObjectNameByAbsoluteParamDTO(absoluteTimeParamDTO,map);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName,null,absoluteTimeParamDTO,map,null);
        }

        //服务对象
        String serviceName =null;
        String serviceCommandline = null;
        if(ArrayUtils.isNotEmpty(serviceParam)){
            serviceName = this.createServiceObjectName( Arrays.asList(serviceParam),null,null);
            serviceCommandline = this.generateServiceObjectCommandLine(statusTypeEnum, serviceName, null, null, Arrays.asList(serviceParam), null, null,null);
        }

        //地址对象 组装为地址组对象
        String srcAddressGroupName = null;
        StringBuilder srcAddressGroupCommandline=new StringBuilder("");
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObject) || StringUtils.isNotEmpty(srcAddressName)){
            //只有一个地址对象时，直接引用不再创建地址组
            if(ArrayUtils.isNotEmpty(srcRefIpAddressObject) && srcRefIpAddressObject.length==1 &&  StringUtils.isEmpty(srcAddressName)){
                srcAddressGroupName=srcRefIpAddressObject[0].replace(":","_");
            }else if(ArrayUtils.isEmpty(srcRefIpAddressObject) && StringUtils.isNotEmpty(srcAddressName)){
                srcAddressGroupName=srcAddressName.replace(":","_");
            }else{
                if(srcIpDto!=null){
                    srcAddressGroupName = this.createIpAddressObjectGroupName(srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),null,srcIpDto.getHosts(),srcRefIpAddressObject,srcIpDto.getObjectGroupNameRefArray(),null,null);
                }else{
                    srcAddressGroupName = this.createIpAddressObjectGroupName(null,null,null,null,null,null,srcRefIpAddressObject,null,null,null);
                }
                srcAddressGroupCommandline.append(String.format("address-group %s",srcAddressGroupName)).append(StringUtils.LF);
                if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
                    for (String srcAddress : srcRefIpAddressObject){
                        srcAddressGroupCommandline.append(String.format(groupMember,srcAddress.replace(":","_"))).append(StringUtils.LF);
                    }
                }
                if(StringUtils.isNotEmpty(srcCommandline)){
                    srcAddressName = srcAddressName.replace(":","_");
                    srcAddressGroupCommandline.append(String.format(groupMember,srcAddressName)).append(StringUtils.LF);
                }
                srcAddressGroupCommandline.append("quit").append(StringUtils.LF);
            }
        }

        String dstAddressGroupName = null;
        StringBuilder dstAddressGroupCommandline=new StringBuilder("");
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObject) || ObjectUtils.isNotEmpty(dstAddressName)){
            if(ArrayUtils.isNotEmpty(dstRefIpAddressObject) && dstRefIpAddressObject.length==1 &&  StringUtils.isEmpty(dstAddressName)){
                dstAddressGroupName=dstRefIpAddressObject[0].replace(":","_");
            }else if(ArrayUtils.isEmpty(dstRefIpAddressObject) && StringUtils.isNotEmpty(dstAddressName)){
                dstAddressGroupName=dstAddressName.replace(":","_");
            }else {
                if(dstIpDto!=null){
                    dstAddressGroupName = this.createIpAddressObjectGroupName(dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),null,dstIpDto.getHosts(),dstRefIpAddressObject,dstIpDto.getObjectGroupNameRefArray(),null,null);
                }else{
                    dstAddressGroupName = this.createIpAddressObjectGroupName(null,null,null,null,null,null,dstRefIpAddressObject,null,null,null);
                }
                dstAddressGroupCommandline.append(String.format("address-group %s", dstAddressGroupName)).append(StringUtils.LF);
                if (ArrayUtils.isNotEmpty(dstRefIpAddressObject)) {
                    for (String dstAddress : dstRefIpAddressObject) {
                        dstAddressGroupCommandline.append(String.format(groupMember, dstAddress.replace(":","_"))).append(StringUtils.LF);
                    }
                }
                if (StringUtils.isNotEmpty(dstCommandline)) {
                    dstAddressName= dstAddressName.replace(":","_");
                    dstAddressGroupCommandline.append(String.format(groupMember, dstAddressName)).append(StringUtils.LF);
                }
                dstAddressGroupCommandline.append("quit").append(StringUtils.LF);
            }
        }

        //服务对象组装为组
        String serviceGroupName = null;
        StringBuilder serviceGroupCommandline=new StringBuilder("");
        if(ArrayUtils.isNotEmpty(refServiceObject) || ObjectUtils.isNotEmpty(serviceName)){
            if(ArrayUtils.isNotEmpty(refServiceObject) && refServiceObject.length==1 &&  StringUtils.isEmpty(serviceName)){
                serviceGroupName=refServiceObject[0];
            }else if(ArrayUtils.isEmpty(refServiceObject) && StringUtils.isNotEmpty(serviceName)){
                serviceGroupName=serviceName;
            }else {
                serviceGroupName = this.createServiceObjectGroupName(null, refServiceObject, refServiceObjectGroup, null, null);
                serviceGroupCommandline.append(String.format("service-group %s", serviceGroupName)).append(StringUtils.LF);
                if (ArrayUtils.isNotEmpty(refServiceObject)) {
                    for (String serivce : refServiceObject) {
                        serviceGroupCommandline.append(String.format(groupMember, serivce)).append(StringUtils.LF);
                    }
                }
                if (ObjectUtils.isNotEmpty(serviceName)) {
                    serviceGroupCommandline.append(String.format(groupMember, serviceName)).append(StringUtils.LF);
                }
                serviceGroupCommandline.append("quit").append(StringUtils.LF);
            }
        }

        //组装策略命令  及全部命令行
        StringBuilder policyCommandLine=new StringBuilder("");
        StringBuilder moveCommandLine=new StringBuilder("");
        if(statusTypeEnum.getRuleType().equals("ipv6")){
            policyCommandLine.append("policy6");
            moveCommandLine.append("policy6");
        }else{
            policyCommandLine.append("policy");
            moveCommandLine.append("policy");
        }

        if(ObjectUtils.isNotEmpty(statusTypeEnum) && statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.MODIFY.getStatus())){
            policyCommandLine.append(String.format(" %s\n",id));
            if(srcRefIpAddressObject != null && ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
                for (String s : srcRefIpAddressObject) {
                    policyCommandLine.append(String.format("source-address %s\n",s));
                }
            }
            if(dstRefIpAddressObject != null && ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
                for (String s : dstRefIpAddressObject) {
                    policyCommandLine.append(String.format("dest-address %s\n",s));
                }
            }
            commandLine.append(policyCommandLine.toString());
        }else{
            //源域 目的域
            if(srcZone != null && StringUtils.isNotBlank(srcZone.getName())){
                policyCommandLine.append(String.format(" %s",srcZone.getName()));
            }else{
                policyCommandLine.append(" any");
            }
            if(dstZone != null && StringUtils.isNotBlank(dstZone.getName())){
                policyCommandLine.append(String.format(" %s",dstZone.getName()));
            }else{
                policyCommandLine.append(" any");
            }

            //源地址 目的地址 服务 时间 原子化命令
            if(StringUtils.isNotEmpty(srcCommandline)){
                commandLine.append(srcCommandline).append(StringUtils.LF);
            }
            if(StringUtils.isNotEmpty(dstCommandline)){
                commandLine.append(dstCommandline).append(StringUtils.LF);
            }
            if(StringUtils.isNotEmpty(serviceCommandline)){
                commandLine.append(serviceCommandline).append(StringUtils.LF);
            }
            if(StringUtils.isNotEmpty(newTimeCommandLine)){
                commandLine.append(newTimeCommandLine).append(StringUtils.LF);
            }

            //源地址
            if(StringUtils.isNotEmpty(srcAddressGroupCommandline)) {
                commandLine.append(srcAddressGroupCommandline).append(StringUtils.LF);
            }
            if(StringUtils.isNotEmpty(srcAddressGroupName)){
                policyCommandLine.append(String.format(" %s",srcAddressGroupName));
            }else{
                policyCommandLine.append(" any");
            }
            //目的地址
            if(StringUtils.isNotEmpty(dstAddressGroupCommandline)) {
                commandLine.append(dstAddressGroupCommandline).append(StringUtils.LF);
            }
            if(StringUtils.isNotEmpty(dstAddressGroupName)){
                policyCommandLine.append(String.format(" %s", dstAddressGroupName));
            }else{
                policyCommandLine.append(" any");
            }
            //服务
            if(StringUtils.isNotEmpty(serviceGroupCommandline)) {
                commandLine.append(serviceGroupCommandline).append(StringUtils.LF);
            }
            if(StringUtils.isNotEmpty(serviceGroupName)){
                policyCommandLine.append(String.format(" %s",serviceGroupName));
            }else{
                policyCommandLine.append(" any");
            }
            //用户 应用
            policyCommandLine.append(" any").append(" any");
            //时间计划
            if(StringUtils.isNotEmpty(newTimeCommandLine)){
                policyCommandLine.append(String.format(" %s",newTimeObjectName));
            }else if(ArrayUtils.isNotEmpty(refTimeObject)&&StringUtils.isNotBlank(refTimeObject[0])){
                policyCommandLine.append(String.format(" %s",refTimeObject[0]));
            }else{
                policyCommandLine.append(" always");
            }
            //策略动作
            policyCommandLine.append(String.format(" %s",action));

            if(StringUtils.isNotEmpty(id)){
                policyCommandLine.append(String.format(" %s",id));
            }
            policyCommandLine.append(StringUtils.LF);
            //保存
            commandLine.append(policyCommandLine.toString());

            if(moveSeatEnum!=null && StringUtils.isNotEmpty(swapRuleNameId) && StringUtils.isNotEmpty(id)){
                if(moveSeatEnum==MoveSeatEnum.BEFORE || moveSeatEnum==MoveSeatEnum.AFTER){
                    moveCommandLine.append(String.format(" move %s %s %s",id,moveSeatEnum.getKey(),swapRuleNameId));
                    commandLine.append(moveCommandLine);
                }
                commandLine.append(StringUtils.LF);
            }
        }
        return commandLine.toString();
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for (String ip:singleIpArray) {
            ipArrayCommandLine.append(ipPre).append(String.format("ip address %s",ip)).append(StringUtils.LF);
        }
        return ipArrayCommandLine.toString();
    }

    public String generateFqdnIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] fqdnArray) {
        StringBuilder ipArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for (String ip:fqdnArray) {
            ipArrayCommandLine.append(ipPre).append(String.format("ip host %s",ip)).append(StringUtils.LF);
        }
        return ipArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipRangeArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for(IpAddressRangeDTO ipAddress: rangIpArray){
            ipRangeArrayCommandLine.append(ipPre).append(String.format("ip range %s %s",ipAddress.getStart(),ipAddress.getEnd())).append(StringUtils.LF);
        }
        return ipRangeArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for(IpAddressSubnetIntDTO dto : subnetIpArray){
            subnetIntArrayCommandLine.append(ipPre).append(String.format("ip subnet %s",dto.getIp()+"/"+dto.getMask())).append(StringUtils.LF);
        }
        return subnetIntArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for(IpAddressSubnetStrDTO dto : subnetIpArray){
            subnetStrArrayCommandLine.append(ipPre).append(String.format("ip subnet %s",dto.getIp()+"/"+dto.getMask())).append(StringUtils.LF);
        }
        return subnetStrArrayCommandLine.toString();
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
        if(CollectionUtils.isEmpty(serviceParamDTOList)){
            return StringUtils.EMPTY;
        }
        if(!StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {
            return null;
        }
        StringBuilder serviceObjectCommandLine = new StringBuilder("");
        serviceObjectCommandLine.append("service").append(String.format(" %s ",name)).append(StringUtils.LF);
        for(ServiceParamDTO serviceParamDTO : serviceParamDTOList){
            if(ProtocolTypeEnum.PROTOCOL.equals(serviceParamDTO.getProtocol())) {
                serviceObjectCommandLine.append("protocol 58").append(StringUtils.LF);
            }else if(ProtocolTypeEnum.ICMP.equals(serviceParamDTO.getProtocol())){
                serviceObjectCommandLine.append("icmp type 8 code 0 255").append(StringUtils.LF);
            }else if(ProtocolTypeEnum.TCP.equals(serviceParamDTO.getProtocol())){
                serviceObjectCommandLine.append(this.generateTCPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, null, null));
            }else if(ProtocolTypeEnum.UDP.equals(serviceParamDTO.getProtocol())){
                serviceObjectCommandLine.append(this.generateUDPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                        serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                        serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                        serviceParamDTO.getTimeOutArray(), null, null, null));
            }
        }
        serviceObjectCommandLine.append("quit").append(StringUtils.LF);
        return serviceObjectCommandLine.toString();
    }
    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder serviceTCPCommandLine = new StringBuilder("");
        if(ObjectUtils.isNotEmpty(dstRangePortArray)){
            for(PortRangeDTO dto :dstRangePortArray){
                serviceTCPCommandLine.append(String.format(defaultTcpPort,dto.getStart(),dto.getEnd())).append(defaultPort).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortStrArray)){
            for(String dto :dstSinglePortStrArray){
                serviceTCPCommandLine.append(String.format(defaultTcpPort,dto,dto)).append(defaultPort).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortArray)){
            for(Integer dto :dstSinglePortArray){
                serviceTCPCommandLine.append(String.format(defaultTcpPort,dto,dto)).append(defaultPort).append(StringUtils.LF);
            }
        }else{
            serviceTCPCommandLine.append(String.format(defaultTcpPort,"0","65535")).append(defaultPort).append(StringUtils.LF);
        }

        return serviceTCPCommandLine.toString();
    }
    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder serviceUDPCommandLine = new StringBuilder("");
        if(ObjectUtils.isNotEmpty(dstRangePortArray)){
            for(PortRangeDTO dto :dstRangePortArray){
                serviceUDPCommandLine.append(String.format(defaultUdpPort,dto.getStart(),dto.getEnd())).append(defaultPort).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortStrArray)){
            for(String dto :dstSinglePortStrArray){
                serviceUDPCommandLine.append(String.format(defaultUdpPort,dto,dto)).append(defaultPort).append(StringUtils.LF);
            }
        }else if(ObjectUtils.isNotEmpty(dstSinglePortArray)){
            for(Integer dto :dstSinglePortArray){
                serviceUDPCommandLine.append(String.format(defaultUdpPort,dto,dto)).append(defaultPort).append(StringUtils.LF);
            }
        }else{
            serviceUDPCommandLine.append(String.format(defaultUdpPort,"0","65535")).append(defaultPort).append(StringUtils.LF);
        }
        return serviceUDPCommandLine.toString();
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipv6ArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for (String ip:singleIpArray) {
            ipv6ArrayCommandLine.append(ipPre).append(String.format("ipv6 address %s",ip)).append(StringUtils.LF);
        }
        return ipv6ArrayCommandLine.toString();
    }

    public String generateFqdnIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] fqdnArray) {
        StringBuilder ipArrayCommandLine = new StringBuilder("");
        if(ObjectUtils.isEmpty(statusTypeEnum) || !statusTypeEnum.getStatus().equalsIgnoreCase(StatusTypeEnum.ADD.getStatus())){
            return null;
        }
        for (String ip:fqdnArray) {
            ipArrayCommandLine.append(String.format("ip host %s",ip)).append(StringUtils.LF);
        }
        return ipArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder ipv6RangeArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for(IpAddressRangeDTO ipAddress: rangIpArray){
            ipv6RangeArrayCommandLine.append(ipPre).append(String.format("ipv6 range %s %s",ipAddress.getStart(),ipAddress.getEnd())).append(StringUtils.LF);
        }
        return ipv6RangeArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder subnetIntArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for(IpAddressSubnetIntDTO dto : subnetIpArray){
            subnetIntArrayCommandLine.append(ipPre).append(String.format("ipv6 subnet %s",dto.getIp()+"/"+dto.getMask())).append(StringUtils.LF);
        }
        return subnetIntArrayCommandLine.toString();
    }

    public String generateSubnetStrIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray){
        StringBuilder subnetStrArrayCommandLine = new StringBuilder("");
        String ipPre = StringUtils.EMPTY;
        if(statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())){
            ipPre = "no ";
        }
        for(IpAddressSubnetStrDTO dto : subnetIpArray){
            subnetStrArrayCommandLine.append(ipPre).append(String.format("ipv6 subnet %s",dto.getIp()+"/"+dto.getMask())).append(StringUtils.LF);
        }
        return subnetStrArrayCommandLine.toString();
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
        if(StringUtils.isNotEmpty(startTime)&&StringUtils.isNotEmpty(startDate)&&StringUtils.isNotEmpty(endTime)&&StringUtils.isNotEmpty(endDate)){
            if(StringUtils.isEmpty(name)){
                name=this.createTimeObjectNameByAbsoluteParamDTO(absoluteTimeParamDTO,map);
            }
            if(StringUtils.isNotEmpty(name)){
                commandLine.append("schedule-once").append(String.format(" %s ",name)).append(StringUtils.LF);
            }
            startTime=startTime.substring(0,5);
            endTime=endTime.substring(0,5);
            commandLine.append(String.format("absolute start %s %s",startDate,startTime)).append(String.format(" end %s %s",endDate,endTime)).append(StringUtils.LF);
            commandLine.append("quit").append(StringUtils.LF);
        }
        return commandLine.toString();
    }

    /**
     * 创建地址对象
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
        if(ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(singleIpArray)
                && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(fqdnArray)){
            return StringUtils.EMPTY;
        }
        StringBuilder commandLine=new StringBuilder("");
        name = name.replace(":","_");
        if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
            commandLine.append("address6").append(String.format(" %s ",name)).append(StringUtils.LF);
        }else{
            commandLine.append("address").append(String.format(" %s ",name)).append(StringUtils.LF);
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO: rangIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateRangeIpV6CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }else{
                    commandLine.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{ipAddressRangeDTO}, map, null));
                }
            }

        }
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String ip: singleIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateSingleIpV6CommandLine(statusTypeEnum,new String[]{ip},map, null));
                }else{
                    commandLine.append(this.generateSingleIpV4CommandLine(statusTypeEnum,new String[]{ip},map, null));
                }
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,null));
                }else{
                    commandLine.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,null));
                }
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO: subnetStrIpArray) {
                if(RuleIPTypeEnum.IP6.equals(ipTypeEnum)){
                    commandLine.append(this.generateSubnetStrIpV6CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}));
                }else{
                    commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,map,null));
                }
            }
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            for (String fqdnDto : fqdnArray) {
                if(RuleIPTypeEnum.IP4.equals(ipTypeEnum)){
                    commandLine.append(this.generateFqdnIpV4CommandLine(statusTypeEnum,new String[]{fqdnDto}));
                }
                //ipv6 不支持域名
            }
        }
        commandLine.append("quit").append(StringUtils.LF);
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
        if(ArrayUtils.isEmpty(serviceObjectNameRefArray) && ArrayUtils.isEmpty(serviceObjectGroupNameRefArray)){
            return this.generateServiceObjectCommandLine(statusTypeEnum,name, id, attachStr,serviceParamDTOList,description,null, null);
        }
        String newName = this.createServiceObjectName( serviceParamDTOList,null,null);
        String newCommandline = this.generateServiceObjectCommandLine(statusTypeEnum,newName, id, attachStr,serviceParamDTOList,description,null, null);

        StringBuilder commandLine=new StringBuilder("");
        if(StringUtils.isNotEmpty(newCommandline)){
            commandLine.append(newCommandline);
        }
        commandLine.append(String.format("service-group  %s",name)).append(StringUtils.LF);
        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for(String nameref:serviceObjectNameRefArray){
                commandLine.append(String.format(groupMember,nameref)).append(StringUtils.LF);
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            for(String nameref:serviceObjectGroupNameRefArray){
                commandLine.append(String.format(groupMember,nameref)).append(StringUtils.LF);
            }
        }
        if(StringUtils.isNotEmpty(newCommandline)){
            commandLine.append(String.format(groupMember,newName)).append(StringUtils.LF);
        }
        commandLine.append("quit").append(StringUtils.LF);

        if(ArrayUtils.isEmpty(serviceObjectNameRefArray) && ArrayUtils.isEmpty(serviceObjectGroupNameRefArray) &&StringUtils.isEmpty(newCommandline)){
            return StringUtils.EMPTY;
        }

        return commandLine.toString();
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "enable \nconfigure terminal \n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "end\nsave config\n";
    }

}



