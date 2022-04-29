package com.abtnetworks.totems.vender.leadSecPowerV.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SecurityLeadSecPowerV30Impl extends OverAllGeneratorAbstractBean {

    private static final String RANGE_PORT_TEMPLATE = "%s:%s";
    private static final String NAME_TEMPLATE = "\"%s\" ";


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
        StringBuilder securityPolicyCl = new StringBuilder();
        //处理地址对象
        String srcGroupName = disposeAddress(statusTypeEnum, srcIpDto, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, args, securityPolicyCl);
        String dstGroupName = disposeAddress(statusTypeEnum,dstIpDto,dstRefIpAddressObject,dstRefIpAddressObjectGroup,map,args,securityPolicyCl);
        //处理服务对象
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam,refServiceObject, refServiceObjectGroup, map, args, securityPolicyCl);
        //处理时间对象
        String newTimeObjectName = disposeTime(absoluteTimeParamDTO, periodicTimeParamDTO, refTimeObject, map, args, securityPolicyCl);
        //新建策略
        disposePolicy(name, action, description, moveSeatEnum, srcZone, dstZone, securityPolicyCl, srcGroupName, dstGroupName, serviceGroupName, newTimeObjectName);
        return securityPolicyCl.toString();
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return null;
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "newconfig save \n";
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(singleIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuilder ipv4ArrayCommandLine = new StringBuilder();
        if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

        } else {
            for (int i = 0; i < singleIpArray.length; i++) {
                ipv4ArrayCommandLine.append(String.format("%s", singleIpArray[i]));
            }
        }
        return ipv4ArrayCommandLine.toString();
    }


    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(rangIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuilder rangeIpCl = new StringBuilder();
        if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

        } else {
            for (int i = 0; i < rangIpArray.length; i++) {
                rangeIpCl.append(String.format("%s:%s", rangIpArray[i].getStart(), rangIpArray[i].getEnd()));
            }
        }
        return rangeIpCl.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(subnetIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuilder subnetIpv4Cl = new StringBuilder();
        if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

        } else {
            for (int i = 0; i < subnetIpArray.length; i++) {
                subnetIpv4Cl.append(String.format("%s/%s", subnetIpArray[i].getIp(), TotemsIpUtils.getMaskMap(String.valueOf(subnetIpArray[i].getMask()))));
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isEmpty(subnetIpArray)) {
            return StringUtils.EMPTY;
        }
        StringBuilder subnetIpv4Cl = new StringBuilder();
        if (ArrayUtils.isNotEmpty(subnetIpArray)) {
            for (int i = 0; i < subnetIpArray.length; i++) {
                IpAddressSubnetStrDTO ipAddressSubnetStrDTO = subnetIpArray[i];
                int maskBit;
                String wildcardMask;
                if (MaskTypeEnum.wildcard_mask.getType().equalsIgnoreCase(ipAddressSubnetStrDTO.getType().getType())) {
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                    wildcardMask = TotemsIpUtils.getWildcardMaskMap(maskBit);
                } else {
                    wildcardMask = ipAddressSubnetStrDTO.getMask();
                }
                if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

                } else {
                    subnetIpv4Cl.append(String.format("%s/%s ", ipAddressSubnetStrDTO.getIp(), wildcardMask)).append(StringUtils.LF);
                }
            }
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }


    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray,
                                                     IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                     String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr,
                                                     String delStr, Map<String, Object> map, String[] args) throws Exception {
        if (ArrayUtils.isNotEmpty(objectNameRefArray)) {
            return this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, ipTypeEnum, name, id, singleIpArray, rangIpArray, subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, objectNameRefArray, null, null, null, null, null, null);
        }
        if (ArrayUtils.getLength(singleIpArray) + ArrayUtils.getLength(rangIpArray) + ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) + ArrayUtils.getLength(fqdnArray) > 1) {
            return this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, ipTypeEnum, name, id, singleIpArray, rangIpArray, subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, objectNameRefArray, null, null, null, null, null, null);
        }
        StringBuilder ipAddressCl = new StringBuilder();
        String namePrefix = "address add name %s ip ";
        ipAddressCl.append(String.format(namePrefix, name));
        if (ArrayUtils.isNotEmpty(singleIpArray)) {
            // ip地址对象
            ipAddressCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum, singleIpArray, null, args));
        } else if (ArrayUtils.isNotEmpty(rangIpArray)) {
            // ip范围地址对象
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                ipAddressCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum, new IpAddressRangeDTO[]{ipAddressRangeDTO}, null, args));
            }
        } else if (ArrayUtils.isNotEmpty(subnetIntIpArray) || ArrayUtils.isNotEmpty(subnetStrIpArray)) {
            if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                // int子网地址对象
                for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                    if (RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())) {
                        ipAddressCl.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum, new IpAddressSubnetIntDTO[]{subnetIntDTO}, null, null, args));
                    } else {
                        ipAddressCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum, new IpAddressSubnetIntDTO[]{subnetIntDTO}, null, null, args));
                    }
                }
            }
            // Str子网地址对象
            if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                    ipAddressCl.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}, null, null, args));
                }
            }
        } else if (ArrayUtils.isNotEmpty(fqdnArray)) {
            ipAddressCl.append(this.generateHostCommandLine(statusTypeEnum, fqdnArray, map, args));
        }
        ipAddressCl.append(StringUtils.LF);
        return ipAddressCl.toString();
    }


    /**
     * 创建地址组对象
     *
     * @param statusTypeEnum          状态类型
     * @param ipTypeEnum              IP枚举类型
     * @param name                    地址对象名称
     * @param id                      地址对象id
     * @param singleIpArray           单个ip
     * @param rangIpArray             范围ip
     * @param subnetIntIpArray        int子网ip
     * @param subnetStrIpArray        Str子网ip
     * @param interfaceArray          接口集合
     * @param fqdnArray               域名集合
     * @param objectNameRefArray      引用对象名称集合
     * @param objectGroupNameRefArray 引用对象组名称集合
     * @param description             备注
     * @param attachStr               附加字符串
     * @param delStr                  删除，失效标记
     * @param map                     扩展参数 key-value String:Object类型
     * @param args                    扩展参数 String[] 类型
     * @return 深信服地址组允许包含地址值
     * @throws Exception
     */
    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {
        //address add name address1 ip 192.168.1.1
        //address add name address2 ip 192.168.2.1:192.168.2.100
        //address add name address3 ip 192.168.3.0/255.255.255.0
        //addrgrp add name addressgroup1 member address1
        //addrgrp set name addressgroup1 addmbr  address2 address3

        StringBuilder ipAddressCl = new StringBuilder();
        List<String> refNameList = new ArrayList<>();
        //多ip建地址组
        if (ArrayUtils.isNotEmpty(singleIpArray)) {
            for (String singleIp : singleIpArray) {
                String singleIpObjectName = this.createIpAddressObjectNameByIp(singleIp, map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, singleIpObjectName, null, new String[]{singleIp}, null, null, null, null, null, null, null, null, null, null, null));
                refNameList.add(singleIpObjectName);
            }
        }
        if (ArrayUtils.isNotEmpty(fqdnArray)) {
            for (String host : fqdnArray) {
                String hostIpObjectName = this.createIpAddressObjectNameByHost(host, map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, hostIpObjectName, null, null, null, null, null, null, new String[]{host}, null, null, null, null, null, null));
                refNameList.add(hostIpObjectName);
            }
        }
        if (ArrayUtils.isNotEmpty(rangIpArray)) {
            for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                String rangeIpObjectName = this.createIpAddressObjectNameByIpRange(rangeDTO.getStart(), rangeDTO.getEnd(), map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, rangeIpObjectName, null, null, new IpAddressRangeDTO[]{rangeDTO}, null, null, null, null, null, null, null, null, null, null));
                refNameList.add(rangeIpObjectName);
            }
        }
        if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                String subnetIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask(), map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIpObjectName, null, null, null, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, null, null, null, null, null, null, null, null, null));
                refNameList.add(subnetIpObjectName);
            }
        }
        if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                String subnetIpObjectName = this.createIpAddressObjectNameByIpMask(ipAddressSubnetStrDTO.getIp(), TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask()), map, args);
                ipAddressCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, subnetIpObjectName, null, null, null, null, new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO}, null, null, null, null, null, null, null, null));
                refNameList.add(subnetIpObjectName);
            }
        }
        //单个不建组
        if(refNameList.size()==1 && objectNameRefArray.length==0){
            args[0] = refNameList.get(0);
            return ipAddressCl.toString();
        }

        //离散复用
        Collections.addAll(refNameList, objectNameRefArray);

        for(int i = 0;i<refNameList.size();i++){
            if(i==0){
                //创建地址组，创建时需要添加一个地址
                ipAddressCl.append("addrgrp add name ").append(name).append(" member ").append(refNameList.get(i)).append(StringUtils.LF);
            }else {
                //地址组添加其他地址
                ipAddressCl.append("addrgrp set name ").append(name).append(" addmbr ").append(refNameList.get(i)).append(StringUtils.LF);
            }
        }

        ipAddressCl.append(StringUtils.LF);
        return ipAddressCl.toString();
    }

    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        String name = "";
        if(!ArrayUtils.isEmpty(args)){
            name = args[0];
        }
        StringBuilder srcPortBuffer = new StringBuilder();
        StringBuilder dstPortBuffer = new StringBuilder();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]).append(":").append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]).append(":").append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format(RANGE_PORT_TEMPLATE, srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]).append(":").append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]).append(":").append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format(RANGE_PORT_TEMPLATE, dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        }
        StringBuilder tcpCommandLine = new StringBuilder();
        tcpCommandLine.append(String.format("service add name \"%s\" ", name));
        if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

        } else {
            tcpCommandLine.append("protocol tcp");
        }
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            tcpCommandLine.append(String.format(" sp %s ", srcPortBuffer.toString()));
        } else {
            tcpCommandLine.append(" sp 1:65535");
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            tcpCommandLine.append(String.format(" dp %s ", dstPortBuffer.toString()));
        } else {
            tcpCommandLine.append(" dp 1:65535");
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        String name = "";
        if(!ArrayUtils.isEmpty(args)){
            name = args[0];
        }
        StringBuilder srcPortBuffer = new StringBuilder();
        StringBuilder dstPortBuffer = new StringBuilder();
        if (ArrayUtils.isNotEmpty(srcSinglePortArray)) {
            srcPortBuffer.append(srcSinglePortArray[0]).append(":").append(srcSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcSinglePortStrArray)) {
            srcPortBuffer.append(srcSinglePortStrArray[0]).append(":").append(srcSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(srcRangePortArray)) {
            srcPortBuffer.append(String.format(RANGE_PORT_TEMPLATE, srcRangePortArray[0].getStart(), srcRangePortArray[0].getEnd()));
        }

        if (ArrayUtils.isNotEmpty(dstSinglePortArray)) {
            dstPortBuffer.append(dstSinglePortArray[0]).append(":").append(dstSinglePortArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstSinglePortStrArray)) {
            dstPortBuffer.append(dstSinglePortStrArray[0]).append(":").append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)) {
            dstPortBuffer.append(String.format(RANGE_PORT_TEMPLATE, dstRangePortArray[0].getStart(), dstRangePortArray[0].getEnd()));
        }
        StringBuilder udpCommandLine = new StringBuilder();
        udpCommandLine.append(String.format("service add name \"%s\" ", name));
        if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

        } else {
            udpCommandLine.append(" protocol udp");
        }
        if (StringUtils.isNotBlank(srcPortBuffer.toString())) {
            udpCommandLine.append(String.format(" sp %s ", srcPortBuffer.toString()));
        } else {
            udpCommandLine.append(" sp 1:65535");
        }
        if (StringUtils.isNotBlank(dstPortBuffer.toString())) {
            udpCommandLine.append(String.format(" dp %s ", dstPortBuffer.toString()));
        } else {
            udpCommandLine.append(" dp 1:65535");
        }
        udpCommandLine.append(StringUtils.LF);
        return udpCommandLine.toString();
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                          Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                          Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                          String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        String name = "";
        if(!ArrayUtils.isEmpty(args)){
            name = args[0];
        }
        StringBuilder icmpCommandLine = new StringBuilder();
        icmpCommandLine.append(String.format("service add name \"%s\" ", name));
        icmpCommandLine.append(" protocol icmp ").append(StringUtils.LF);

        return icmpCommandLine.toString();
    }

    /**
     * 服务组对象
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
        List<String> serverObjectNameList = new ArrayList<>();
        if(serviceObjectNameRefArray!=null){
            //离散复用对象名
            serverObjectNameList = Arrays.asList(serviceObjectNameRefArray);
        }
        List<String> nameList = new ArrayList<>(serverObjectNameList);
        StringBuilder serviceGroupObjectCl = new StringBuilder();
        if (CollectionUtils.isEmpty(serviceParamDTOList)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum != null && StatusTypeEnum.DELETE.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus())) {

        }
        if(map.get("restServiceParamDTOS")!=null){
            serviceParamDTOList = Arrays.asList((ServiceParamDTO[]) map.get("restServiceParamDTOS"));
        }
        if (StatusTypeEnum.ADD.getStatus().equalsIgnoreCase(statusTypeEnum.getStatus()) && CollectionUtils.isNotEmpty(serviceParamDTOList) && serviceParamDTOList.size()>0) {
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                String serviceObjectName = createServiceObjectName(serviceParamDTO, null, null);
                String[] names = new String[]{serviceObjectName};
                String protocolPortCl = null;
                if (ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    protocolPortCl = this.generateTCPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, names);
                } else if (ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    protocolPortCl = this.generateUDPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, names);
                } else if (ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    protocolPortCl = this.generateICMPCommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, names);
                } else if (ProtocolTypeEnum.ICMP6.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    protocolPortCl = this.generateICMP6CommandLine(statusTypeEnum, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, names);
                }
                if (!StringUtils.isBlank(protocolPortCl)) {
                    serviceGroupObjectCl.append(protocolPortCl);
                }
                nameList.add(serviceObjectName);
            }
        }
        //服务组
        if(!CollectionUtils.isEmpty(nameList)){
            if(nameList.size()>1){
                serviceGroupObjectCl.append("servgrp add name \"").append(name).append("\"").append(StringUtils.LF);
                for (String na : nameList) {
                    serviceGroupObjectCl.append("servgrp set name \"").append(name).append("\"").append(" addmbr ").append(na).append(StringUtils.LF);
                }
            }
        }
        serviceGroupObjectCl.append(StringUtils.LF);
        return serviceGroupObjectCl.toString();
    }


    @Override
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO, Map<String, Object> map, String[] args) throws Exception {
        //time add name t1 type once start 2021/08/10 09:00:00 stop 2021/10/10 12:00:00
        if (StringUtils.isEmpty(absoluteTimeParamDTO.getStartTime()) || StringUtils.isEmpty(absoluteTimeParamDTO.getStartDate())
                || StringUtils.isEmpty(absoluteTimeParamDTO.getEndTime()) || StringUtils.isEmpty(absoluteTimeParamDTO.getEndDate())) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(name)) {
            name = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, map, args);
        }
        StringBuilder sb = new StringBuilder();
        String startDate = absoluteTimeParamDTO.getStartDate();
        String startTime = absoluteTimeParamDTO.getStartTime();
        String startTimeString = String.format("%s %s", startDate.replace("-", "/"), startTime);
        String endDate = absoluteTimeParamDTO.getEndDate();
        String endTime = absoluteTimeParamDTO.getEndTime();
        String endTimeString = String.format("%s %s", endDate.replace("-", "/"), endTime);
        sb.append("time add name ").append(name).append(" type once start ").append(startTimeString).append(" stop ").append(endTimeString).append(StringUtils.LF);
        return sb.toString();
    }



    private void disposePolicy(String name, String action, String description, MoveSeatEnum moveSeatEnum, ZoneParamDTO srcZone, ZoneParamDTO dstZone, StringBuilder securityPolicyCl, String srcGroupName, String dstGroupName, String serviceGroupName, String newTimeObjectName) {
        //rule add type permit name "policy1" sa address1 da address2 izone zone1 ozone zone2 service servicegroup1 time t1 comment “描述”
        securityPolicyCl.append("rule add type ")
                .append(action)
                .append(" name \"").append(name).append("\"");
        //移动,目前只支持置顶
        if(MoveSeatEnum.FIRST.equals(moveSeatEnum)){
            securityPolicyCl.append(" id 1 ");
        }
        String  srcZoneName = "any";
        if(!StringUtils.isBlank(srcZone.getName())){
            srcZoneName = srcZone.getName();
        }
        String  dstZoneName = "any";
        if(!StringUtils.isBlank(dstZone.getName())){
            dstZoneName = dstZone.getName();
        }
        securityPolicyCl.append(" sa ").append(srcGroupName)
                .append(" da ").append(dstGroupName)
                .append(" izone \"").append(srcZoneName).append("\"")
                .append(" ozone \"").append(dstZoneName).append("\"")
                .append(" service ").append(serviceGroupName);

        if(!StringUtils.isBlank(newTimeObjectName)){
            securityPolicyCl.append(" time \"").append(newTimeObjectName).append("\"");
        }
        if(!StringUtils.isBlank(description)){
            securityPolicyCl.append(" comment \"").append(description).append("\"");
        }

        securityPolicyCl.append(StringUtils.LF);
    }

    private String disposeTime(AbsoluteTimeParamDTO absoluteTimeParamDTO, PeriodicTimeParamDTO periodicTimeParamDTO, String[] refTimeObject, Map<String, Object> map, String[] args, StringBuilder securityPolicyCl) throws Exception {
        String newTimeObjectName = StringUtils.EMPTY;
        String newTimeCommandLine = null;
        if (ObjectUtils.isNotEmpty(absoluteTimeParamDTO) && !StringUtils.isEmpty(absoluteTimeParamDTO.getStartTime()) && !StringUtils.isEmpty(absoluteTimeParamDTO.getStartDate())
                && !StringUtils.isEmpty(absoluteTimeParamDTO.getEndTime()) && !StringUtils.isEmpty(absoluteTimeParamDTO.getEndDate())) {
            newTimeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, map, args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName, null, absoluteTimeParamDTO, map, null);
            newTimeObjectName = String.format("%s", newTimeObjectName);
        } else if (ObjectUtils.isNotEmpty(periodicTimeParamDTO)) {
            newTimeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO, map, args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(newTimeObjectName, null, periodicTimeParamDTO, map, null);
            newTimeObjectName = String.format("%s", newTimeObjectName);
        } else if (ArrayUtils.isNotEmpty(refTimeObject)) {
            newTimeObjectName = String.format("%s", refTimeObject[0]);
        }

        if (StringUtils.isNotBlank(newTimeCommandLine)) {
            securityPolicyCl.append(newTimeCommandLine);
        }
        securityPolicyCl.append(StringUtils.LF);
        return newTimeObjectName;
    }

    protected String disposeService(StatusTypeEnum statusTypeEnum, ServiceParamDTO[] serviceParam,String[] refServiceObject, String[] refServiceObjectGroup, Map<String, Object> map, String[] args, StringBuilder securityPolicyCl) throws Exception {
        String serviceGroupName = null;
        if (!ArrayUtils.isEmpty(serviceParam) &&
                (ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParam[0].getProtocol().getType()) ||
                        ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParam[0].getProtocol().getType()) ||
                        ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParam[0].getProtocol().getType()))) {
            if(!ArrayUtils.isEmpty(refServiceObjectGroup)){
                //整体复用
                serviceGroupName = refServiceObjectGroup[0];
                serviceGroupName = String.format(" \"%s\" ", serviceGroupName);
                return serviceGroupName;
            }else {
                List<ServiceParamDTO> serviceParamDTOList = Arrays.stream(serviceParam).collect(Collectors.toList());
                serviceGroupName = createServiceObjectName(serviceParamDTOList, null, null);
                securityPolicyCl.append(this.generateServiceObjectGroupCommandLine(statusTypeEnum, serviceGroupName, null, null, Arrays.asList(serviceParam), null, refServiceObject, null, map, null));
                if (serviceParam.length<1) {
                    serviceGroupName = "any";
                }
            }
            serviceGroupName = String.format(" \"%s\" ", serviceGroupName);
            return serviceGroupName;
        }else {
            return "\"any\"";
        }
    }

    protected String disposeAddress(StatusTypeEnum statusTypeEnum, IpAddressParamDTO srcIpDto, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder securityPolicyCl) throws Exception {
        String srcGroupName = "\"any\"";
        if(!ArrayUtils.isEmpty(srcRefIpAddressObjectGroup)){
            srcGroupName = String.format(NAME_TEMPLATE, srcRefIpAddressObjectGroup[0]);
        } else if (ObjectUtils.isNotEmpty(srcIpDto) && (ObjectUtils.isNotEmpty(srcIpDto.getIpTypeEnum()) || (ArrayUtils.getLength(srcRefIpAddressObject) > 1))) {
            if (srcIpDto.getObjectNameRefArray() == null) {
                srcIpDto.setObjectNameRefArray(new String[0]);
            }
            String[] allSrcRefObjectName = ArrayUtils.addAll(srcRefIpAddressObject, srcIpDto.getObjectNameRefArray());

            //生成地址组对象命令行
            String[] singleIpName = new String[1];
            srcGroupName = this.createIpAddressObjectGroupName(srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(),
                    null, srcIpDto.getHosts(), allSrcRefObjectName, null, map, args);
            securityPolicyCl.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum, srcIpDto.getIpTypeEnum(), srcGroupName, null, srcIpDto.getSingleIpArray(), srcIpDto.getRangIpArray(), srcIpDto.getSubnetIntIpArray(), srcIpDto.getSubnetStrIpArray(),
                    null, srcIpDto.getHosts(), allSrcRefObjectName, null, null, null, null, null, singleIpName));
            if(!StringUtils.isBlank(singleIpName[0]))
                srcGroupName = singleIpName[0];
            srcGroupName = String.format(NAME_TEMPLATE, srcGroupName);
            securityPolicyCl.append(StringUtils.LF);
        } else if (ArrayUtils.isNotEmpty(srcRefIpAddressObject)) {
            srcGroupName = String.format(NAME_TEMPLATE, srcRefIpAddressObject[0]);
        }
        return srcGroupName;
    }
}
