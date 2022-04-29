package com.abtnetworks.totems.vender.Juniper.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.FieldConstants;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IP6Utils;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: Yyh
 * @Description JuniperSrx 命令行生成
 * @Date: 2021/4/29
 */
public class SecurityJuniperSRXImpl extends OverAllGeneratorAbstractBean {
    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum,String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        String zone="";
        if(args==null) {
            zone = "any";
        }else {
            zone=args[0];
        }
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
            ipv4ArrayCommandLine.append(String.format("set security zones security-zone %s address-book address %s %s/32\n",zone,map.get("singleIpAddressObjectName"),singleIpArray[0]));
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if (StringUtils.isBlank(objectName)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            //set security zones security-zone trust address-book address cc dns-name dsd.com.cn
            StringBuilder sb = new StringBuilder();
            for (String host : hosts) {
                sb.append("set security zones security-zone ").append(map.get("zone"))
                        .append(" address-book address ").append(objectName).append(" dns-name ").append(host).append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        String zone="";
        if(args==null) {
            zone = "any";
        }else {
            zone=args[0];
        }
        if(ArrayUtils.isEmpty(rangIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer ipv4ArrayCommandLine = new StringBuffer();
        ipv4ArrayCommandLine.append(String.format("set security zones security-zone %s address-book %s range-address %s to %s\n",zone,map.get("rangeAddressObjectName"),
                rangIpArray[0].getStart(),rangIpArray[0].getEnd()));
        return ipv4ArrayCommandLine.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        String zone="";
        if(args==null) {
            zone = "any";
        }else {
            zone=args[0];
        }
        StringBuffer subnetIpv4Cl = new StringBuffer();
        if(ArrayUtils.isEmpty(subnetIpArray)){
            return StringUtils.EMPTY;
        }
        subnetIpv4Cl.append(String.format("set security zones security-zone %s address-book address %s %s/%s\n",zone,map.get("subnetIntAddressObjectName"),subnetIpArray[0].getIp(), subnetIpArray[0].getMask()));
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuffer subnetIpv4Cl = new StringBuffer();
        String zone="";
        if(args==null) {
            zone = "any";
        }else {
            zone=args[0];
        }
        if(ArrayUtils.isNotEmpty(subnetIpArray)){
                int maskBit;
                if(MaskTypeEnum.mask.getType().equalsIgnoreCase(subnetIpArray[0].getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBit(subnetIpArray[0].getMask());
                } else {
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(subnetIpArray[0].getMask());
                }
                subnetIpv4Cl.append(String.format("set security zones security-zone %s address-book address %s %s/%s\n",zone,map.get("subnetStrAddressObjectName"),subnetIpArray[0].getIp(), maskBit));
        }
        return subnetIpv4Cl.toString();
    }

    @Override
    public String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return this.generateIpAddressObjectName(ipTypeEnum,groupName,map,args);
    }


    @Override
    public String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                          String name, String id,
                                                          String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                          IpAddressSubnetIntDTO[] subnetIntIpArray,IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                          String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                          String description, String attachStr, String delStr,
                                                          Map<String, Object> map, String[] args) throws Exception {
        StringBuffer ipAddressGroupCl = new StringBuffer();
        attachStr="true";
        List<String> iparray = new ArrayList<>();
        iparray.add(attachStr);
        if(StringUtils.isEmpty(name)) {
            name = createIpAddressObjectGroupName(iparray.toArray(new String[0]), null, null, null,null,null,null,null,null,null);
        }
            if (null == map) {
                map = new HashMap<String, Object>();
            }
            map.put("ObjectGroupName",name);
            if (ArrayUtils.isNotEmpty(singleIpArray) || ArrayUtils.isNotEmpty(rangIpArray) || ArrayUtils.isNotEmpty(subnetIntIpArray) || ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                String addressObjectCommandLine = this.generateIpAddressObjectCommandLine(statusTypeEnum, ipTypeEnum, null, null, singleIpArray, rangIpArray,
                        subnetIntIpArray, subnetStrIpArray, interfaceArray, fqdnArray, null, null, null, null, map, args);
                ipAddressGroupCl.append(addressObjectCommandLine);

        }

        return ipAddressGroupCl.toString();
    }

    @Override
    public String generateIpAddressObjectName(RuleIPTypeEnum ipTypeEnum, String name, Map<String, Object> map, String[] args) throws Exception {
        if (ipTypeEnum == null){
            return String.format("%s ",name);
        }else if (RuleIPTypeEnum.IP4.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("%s ",name);
        } else if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
            return String.format("%s ipv6 ",name);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                     String name, String id,
                                                     String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                     IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                     String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray,
                                                     String description, String attachStr, String delStr,
                                                     Map<String, Object> map, String[] args) throws Exception {
        StringBuffer ipAddressCl = new StringBuffer();
        String zone="";
        if(args==null) {
            zone = "any";
        }else {
            zone=args[0];
        }
        // 处理ANY

        // ip地址对象
        if(ArrayUtils.isNotEmpty(singleIpArray)) {
            for (String singleIp : singleIpArray) {
                if (ArrayUtils.isNotEmpty(singleIpArray)) {
                        name = generateIpAddressObjectName(ipTypeEnum, createIpAddressObjectNameByIp(singleIp, null, null), null, null);

                    map.put("singleIpAddressObjectName", name);
                    if (RuleIPTypeEnum.IP4.getName().equalsIgnoreCase(ipTypeEnum.getName())){
                        ipAddressCl.append(this.generateSingleIpV4CommandLine(statusTypeEnum,new String[]{singleIp}, map, args));
                    } else if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
                        ipAddressCl.append(this.generateSingleIpV6CommandLine(statusTypeEnum,new String[]{singleIp}, map, args));
                    }
                }
                if (ObjectUtils.isNotEmpty(rangIpArray) || ObjectUtils.isNotEmpty(subnetIntIpArray) || ObjectUtils.isNotEmpty(subnetStrIpArray) || singleIpArray.length > 1) {
                    ipAddressCl.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone,
                            map.get("ObjectGroupName"), name));
                }
            }
        }
        // ip范围地址对象
        if(ArrayUtils.isNotEmpty(rangIpArray)) {
            for (IpAddressRangeDTO rangeIp : rangIpArray) {
                if (ArrayUtils.isNotEmpty(rangIpArray)) {
                        name = generateIpAddressObjectName(ipTypeEnum, createIpAddressObjectNameByRangIp(rangeIp, null, null),null,null);
                    map.put("rangeAddressObjectName", name);
                    //同一范围地址对象只能和一个范围地址绑定
                    ipAddressCl.append(this.generateRangeIpV4CommandLine(statusTypeEnum,new IpAddressRangeDTO[]{rangeIp}, map, args));
                }
                if (ObjectUtils.isNotEmpty(singleIpArray) || ObjectUtils.isNotEmpty(subnetIntIpArray) || ObjectUtils.isNotEmpty(subnetStrIpArray) || rangIpArray.length > 1) {
                    ipAddressCl.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone,
                            map.get("ObjectGroupName"), name));
                }
            }
        }
        // int子网地址对象
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)) {
            for (IpAddressSubnetIntDTO SubnetIntIp : subnetIntIpArray) {
                if (ArrayUtils.isNotEmpty(subnetIntIpArray)) {
                        name = generateIpAddressObjectName(ipTypeEnum, createIpAddressObjectNameByIpSubArray(subnetIntIpArray, subnetStrIpArray, null, null), null, null);

                    map.put("subnetIntAddressObjectName", name);
                    //同一子网地址对象只能和一个子网地址绑定

                    if (RuleIPTypeEnum.IP4.getName().equalsIgnoreCase(ipTypeEnum.getName())){
                        ipAddressCl.append(this.generateSubnetIntIpV4CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{SubnetIntIp}, null, map, args));
                    } else if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ipTypeEnum.getName())){
                        ipAddressCl.append(this.generateSubnetIntIpV6CommandLine(statusTypeEnum,new IpAddressSubnetIntDTO[]{SubnetIntIp}, null, map, args));
                    }
                }
                if (ObjectUtils.isNotEmpty(rangIpArray) || ObjectUtils.isNotEmpty(singleIpArray) || ObjectUtils.isNotEmpty(subnetStrIpArray) || subnetIntIpArray.length > 1) {
                    ipAddressCl.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone,
                            map.get("ObjectGroupName"), name));
                }
            }
        }
        // Str子网地址对象
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)) {
            for (IpAddressSubnetStrDTO SubnetStrIp : subnetStrIpArray) {
                if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {

                        name = generateIpAddressObjectName(ipTypeEnum, createIpAddressObjectNameByIpSubArray(subnetIntIpArray, subnetStrIpArray, null, null), null, null);


                    map.put("subnetStrAddressObjectName", name);
                    ipAddressCl.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{SubnetStrIp}, null, map, args));
                }
                if (ObjectUtils.isNotEmpty(rangIpArray) || ObjectUtils.isNotEmpty(subnetIntIpArray) || ObjectUtils.isNotEmpty(singleIpArray) || subnetStrIpArray.length > 1) {
                    ipAddressCl.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone,
                            map.get("ObjectGroupName"), name));
                }
            }
        }
        ipAddressCl.append(StringUtils.LF);
        return ipAddressCl.toString();
    }


    public String createIpAddressObjectNameByRangIp(IpAddressRangeDTO rangeIp, Map<String, Object> map, String[] args) {
        if (ObjectUtils.isEmpty(rangeIp)) {
            return StringUtils.EMPTY;
        }
        StringBuffer rangIpName = new StringBuffer("R_");
        rangIpName.append(String.format("%s-%s",rangeIp.getStart(),rangeIp.getEnd()));
        return rangIpName.toString();
    }

    @Override
    public String createIpAddressObjectNameByIpSubArray(IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, Map<String, Object> map, String[] args) {
        if(ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer ipSubName = new StringBuffer("SUB_");
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            ipSubName.append(String.format("%s/%s",subnetIntIpArray[0].getIp(),subnetIntIpArray[0].getMask()));
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            ipSubName.append(String.format("%s/%s",subnetStrIpArray[0].getIp(),subnetStrIpArray[0].getMask()));
        }
        return ipSubName.toString();
    }

    @Override
    public String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    private List<String> getIpAddressObjectName(String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                IpAddressSubnetIntDTO[] subnetIntIpArray,IpAddressSubnetStrDTO[] subnetStrIpArray){
        List<String> addressNameList = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            addressNameList.add(this.createIpAddressObjectNameBySingleIpArray(singleIpArray,null,null));
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO:rangIpArray) {
                addressNameList.add(this.createIpAddressObjectNameByIpRange(ipAddressRangeDTO.getStart(),ipAddressRangeDTO.getEnd(),null,null));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : subnetIntIpArray) {
                addressNameList.add(this.createIpAddressObjectNameByIpMask(ipAddressSubnetIntDTO.getIp(),ipAddressSubnetIntDTO.getMask(),null,null));
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO ipAddressSubnetStrDTO : subnetStrIpArray) {
                int maskBit;
                if(MaskTypeEnum.mask.getType().equalsIgnoreCase(ipAddressSubnetStrDTO.getType().getType())){
                    maskBit = TotemsIpUtils.getMaskBit(ipAddressSubnetStrDTO.getMask());
                } else {
                    maskBit = TotemsIpUtils.getMaskBitMapByInverseMask(ipAddressSubnetStrDTO.getMask());
                }
                addressNameList.add(createIpAddressObjectNameByIpMask(ipAddressSubnetStrDTO.getIp(),maskBit,null,null));
            }
        }
        return addressNameList;
    }

    /**
     *
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
     * @param srcIp 源ip
     * @param dstIp 目的ip
     * @param serviceParam 服务（源端口，目的端口，协议）
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
     * @throws Exception
     */
    @Override
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                    String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                    String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam,
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
//        命令行开始
        StringBuffer securityPolicyCl = new StringBuffer();
        securityPolicyCl.append("configure ");

        securityPolicyCl.append(StringUtils.LF);
        // 处理地址对象
        List<String> newSrcIpAddressObjectNameList = Lists.newArrayList();
        List<String> newDstIpAddressObjectNameList = Lists.newArrayList();
        String newDstIpAddressGroupObjectName=null;
        String newSrcIpAddressGroupObjectName=null;
        String srcAddressCommandLine = "";

//        域作为参数传入args
        if(args==null){
            args=new String[1];
        }
        String srcZoneName = StringUtils.EMPTY;
        String dstZoneName = StringUtils.EMPTY;
        if(ObjectUtils.isNotEmpty(srcZone)){
            if(StringUtils.isNotBlank(srcZone.getName())){
                srcZoneName = srcZone.getName();
            } else if(ArrayUtils.isNotEmpty(srcZone.getNameArray())){
                srcZoneName = srcZone.getNameArray()[0];
            }
        }
        if( StringUtils.isBlank( srcZoneName )  ){
            srcZoneName = "any";
        }
        if(ObjectUtils.isNotEmpty(dstZone)){
            if(StringUtils.isNotBlank(dstZone.getName())){
                dstZoneName = dstZone.getName();
            } else if(ArrayUtils.isNotEmpty(dstZone.getNameArray())){
                dstZoneName = dstZone.getNameArray()[0];
            }
        }
        if( StringUtils.isBlank( dstZoneName )  ){
            dstZoneName = "any";
        }
        if(ObjectUtils.isNotEmpty(srcIp)) {
            newSrcIpAddressObjectNameList = this.getIpAddressObjectName(srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray());
                args[0]=srcZoneName;
            if (ArrayUtils.getLength(srcIp.getRangIpArray()) + ArrayUtils.getLength(srcIp.getSingleIpArray()) + ArrayUtils.getLength(srcIp.getSubnetIntIpArray())
                    + ArrayUtils.getLength(srcIp.getSubnetStrIpArray()) > 1) {
                newSrcIpAddressGroupObjectName = createIpAddressObjectGroupName(srcIp.getSingleIpArray(),srcIp.getRangIpArray(),srcIp.getSubnetIntIpArray(),srcIp.getSubnetStrIpArray(), null,null,null,null,null, null);
                srcAddressCommandLine = this.generateIpAddressObjectGroupCommandLine(null, srcIp.getIpTypeEnum(), newSrcIpAddressGroupObjectName, null,
                        srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(),
                        null, null, null, null, null, null, null, map, args);
            } else {
                //生成src地址对象命令行
                srcAddressCommandLine = this.generateIpAddressObjectCommandLine(null, srcIp.getIpTypeEnum(), null, null,
                        srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(),
                        null, null, null, null, null, null, new HashMap<>(), args);
            }
        }

        if( ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup) && ArrayUtils.isNotEmpty( srcRefIpAddressObject ) && "zu".equals(srcRefIpAddressObjectGroup[0])){

            String nameStr = createIpAddressObjectGroupName(null,null,null,null, null,null,srcRefIpAddressObject,null,null, null);

            srcRefIpAddressObjectGroup = nameStr.split("  ");
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : newSrcIpAddressObjectNameList) {
                stringBuilder.append( "set applications application-set ").append( nameStr).append(" application ").append( s ).append( "\n" ) ;
            }
            newSrcIpAddressObjectNameList=new ArrayList<>();
            for (String s : srcRefIpAddressObject) {
                stringBuilder.append( "set applications application-set ").append(nameStr).append(" application ").append( s ).append( "\n" ) ;
            }
            srcRefIpAddressObject = null;
            srcAddressCommandLine += stringBuilder.toString();
        }else if ( ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup) &&  "zu".equals(srcRefIpAddressObjectGroup[0])) {
            srcRefIpAddressObjectGroup = null ;
        }


        //生成dst地址对象命令行
        String dstAddressCommandLine = null;
        if(ObjectUtils.isNotEmpty(dstIp)) {
                args[0]=dstZoneName;

            newDstIpAddressObjectNameList = getIpAddressObjectName(dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray());
            if (ArrayUtils.getLength(dstIp.getRangIpArray()) + ArrayUtils.getLength(dstIp.getSingleIpArray()) + ArrayUtils.getLength(dstIp.getSubnetIntIpArray())
                    + ArrayUtils.getLength(dstIp.getSubnetStrIpArray()) > 1) {
                newDstIpAddressGroupObjectName = createIpAddressObjectGroupName(dstIp.getSingleIpArray(),dstIp.getRangIpArray(),dstIp.getSubnetIntIpArray(),dstIp.getSubnetStrIpArray(), null,null,null,null,null,null);
                dstAddressCommandLine = this.generateIpAddressObjectGroupCommandLine(null, dstIp.getIpTypeEnum(), newDstIpAddressGroupObjectName, null,
                        dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray(),
                        null, null, null, null, null, null, null, map, args);
            } else {
                dstAddressCommandLine = this.generateIpAddressObjectCommandLine(null, dstIp.getIpTypeEnum(), null, null,
                        dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray(),
                        null, null, null, null, null, null, new HashMap<>(), args);
            }
        }

        if( ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup) && ArrayUtils.isNotEmpty( dstRefIpAddressObject ) && "zu".equals(dstRefIpAddressObjectGroup[0])){

            String nameStr = createIpAddressObjectGroupName(null,null,null,null, null,null,dstRefIpAddressObject,null,null, null);

            dstRefIpAddressObjectGroup = nameStr.split("  ");
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : newDstIpAddressObjectNameList) {
                stringBuilder.append( "set applications application-set ").append( nameStr).append(" application ").append( s ).append( "\n" ) ;
            }
            newDstIpAddressObjectNameList=new ArrayList<>();
            for (String s : srcRefIpAddressObject) {
                stringBuilder.append( "set applications application-set ").append(nameStr).append(" application ").append( s ).append( "\n" ) ;
            }
            dstRefIpAddressObject = null;
            dstAddressCommandLine += stringBuilder.toString();
        }else if ( ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup) &&  "zu".equals(dstRefIpAddressObjectGroup[0])) {
            dstRefIpAddressObjectGroup = null ;
        }


//         处理服务对象
        List<String> newServiceObjectNameList = new ArrayList<>();
        String serviceObjectCommandLine = "";
        String newServiceGroupObjectName = null;

        if(ArrayUtils.isNotEmpty(serviceParam)) {
            for (ServiceParamDTO serviceParamDTO : serviceParam) {
                newServiceObjectNameList.add(this.createServiceObjectName(serviceParamDTO, null, null));
            }

            if (serviceParam.length > 1) {
                newServiceGroupObjectName=createServiceObjectGroupName(Arrays.asList(serviceParam),null,null
                        ,null,null);
                Map m = new HashMap<String, Object>();
                serviceObjectCommandLine = this.generateServiceObjectGroupCommandLine(null, newServiceGroupObjectName, null, null, Arrays.asList(serviceParam),
                        null, null, null, m, null);


                StringBuilder stringBuilder = new StringBuilder();

                if (  ArrayUtils.isNotEmpty(refServiceObject) ){
                    for (String s : refServiceObject) {
                        stringBuilder.append( "set applications application-set ").append(m.get("ObjectGroupName")).append(" application ").append( s ).append( "\n" ) ;
                    }
                    refServiceObject = null;
                }
                serviceObjectCommandLine += stringBuilder.toString();

            } else {
                serviceObjectCommandLine = this.generateServiceObjectCommandLine(statusTypeEnum, null, null, null, Arrays.asList(serviceParam), null, new HashMap<>(), null);
            }
        }
        if( ArrayUtils.isNotEmpty(refServiceObjectGroup) &&  CollectionUtils.isNotEmpty( newServiceObjectNameList ) &&   ArrayUtils.isNotEmpty( refServiceObject ) && "zu".equals(refServiceObjectGroup[0])  ){

            String serviceObjectGroupName = this.createServiceObjectGroupName(Arrays.asList(serviceParam), refServiceObject, null, null, args);
            refServiceObjectGroup = serviceObjectGroupName.split("  ");
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : newServiceObjectNameList) {
                stringBuilder.append( "set applications application-set ").append( serviceObjectGroupName).append(" application ").append( s ).append( "\n" ) ;
            }
            newServiceObjectNameList=new ArrayList<>();
            for (String s : refServiceObject) {
                stringBuilder.append( "set applications application-set ").append(serviceObjectGroupName).append(" application ").append( s ).append( "\n" ) ;
            }
            refServiceObject = null;
            serviceObjectCommandLine += stringBuilder.toString();
        }else if ( ArrayUtils.isNotEmpty(refServiceObjectGroup) &&  "zu".equals(refServiceObjectGroup[0])) {
            refServiceObjectGroup = null ;
        }

        // 处理时间对象
        if(ArrayUtils.isNotEmpty(refTimeObject)) {
            if ( StringUtils.isBlank( srcZoneName ) ){
                srcZoneName = "any";
            }
            if ( StringUtils.isBlank( dstZoneName ) ){
                dstZoneName = "any";
            }
            for (String s : refTimeObject) {
                newTimeCommandLine ="set security policies from-zone "+ srcZoneName + " to-zone "+ dstZoneName +" policy "+ name +" scheduler-name " + s +"\n" ;
            }
        }

        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        // 地址和服务对象命令行
        if(StringUtils.isNotBlank(srcAddressCommandLine)){
            securityPolicyCl.append(srcAddressCommandLine);
        }
        if(StringUtils.isNotBlank(dstAddressCommandLine)){
            securityPolicyCl.append(dstAddressCommandLine);
        }
        if(StringUtils.isNotBlank(serviceObjectCommandLine)){
            securityPolicyCl.append(serviceObjectCommandLine);
        }
        securityPolicyCl.append(StringUtils.LF);

        // 策略命令行
        //域拼接
        String srcZoneJoin = "";
        String dstZoneJoin = "";
        srcZoneJoin = String.format("from-zone %s", AliStringUtils.isEmpty(srcZoneName)?"any":srcZoneName);
        dstZoneJoin = String.format("to-zone %s", AliStringUtils.isEmpty(dstZoneName)?"any":dstZoneName);
        if(!AliStringUtils.isEmpty(description)){
            securityPolicyCl.append(String.format("set security policies %s %s policy %s description %s \n", srcZoneJoin, dstZoneJoin, name, description));
        }
        // 本次生成的源地址和目的地址
        // 处理src-addr any问题
        if(ArrayUtils.isEmpty(srcRefIpAddressObject) && ArrayUtils.isEmpty(srcRefIpAddressObjectGroup) &&
                CollectionUtils.isEmpty(newSrcIpAddressObjectNameList)){
            securityPolicyCl.append(String.format("set security policies %s %s policy %s match source-address any \n", srcZoneJoin, dstZoneJoin, name));
        }
        if(newSrcIpAddressObjectNameList.size()==1){
            securityPolicyCl.append(String.format("set security policies %s %s policy %s match source-address %s \n", srcZoneJoin, dstZoneJoin, name,newSrcIpAddressObjectNameList.get(0)));
        }else {
            if( StringUtils.isNotBlank( newSrcIpAddressGroupObjectName )  ){
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match source-address %s \n", srcZoneJoin, dstZoneJoin, name,newSrcIpAddressGroupObjectName));
            }
        }
        //复用源地址对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObject)){
            for (String srcRefIpAddressObjectName:srcRefIpAddressObject) {
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match source-address %s \n", srcZoneJoin, dstZoneJoin, name,srcRefIpAddressObjectName));
            }
        }
        //复用源地址组对象
        if(ArrayUtils.isNotEmpty(srcRefIpAddressObjectGroup)){
            for (String srcRefIpAddressObjectGroupName:srcRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match source-address %s \n", srcZoneJoin, dstZoneJoin, name,srcRefIpAddressObjectGroupName));
            }
        }
        // 处理dst-addr any问题
        if(ArrayUtils.isEmpty(dstRefIpAddressObject) && ArrayUtils.isEmpty(dstRefIpAddressObjectGroup) && CollectionUtils.isEmpty(newDstIpAddressObjectNameList)){
            securityPolicyCl.append(String.format("set security policies %s %s policy %s match destination-address any \n", srcZoneJoin, dstZoneJoin, name));
        }
        if(newDstIpAddressObjectNameList.size()==1) {
            securityPolicyCl.append(String.format("set security policies %s %s policy %s match destination-address %s \n", srcZoneJoin, dstZoneJoin, name, newDstIpAddressObjectNameList.get(0)));
        }else {
            if( StringUtils.isNotBlank( newDstIpAddressGroupObjectName )  ){
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match destination-address %s \n", srcZoneJoin, dstZoneJoin, name, newDstIpAddressGroupObjectName));
            }
        }
        // 复用目的地址对象
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObject)){
            for (String dstRefIpAddressObjectName:dstRefIpAddressObject) {
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match destination-address %s \n", srcZoneJoin, dstZoneJoin, name,dstRefIpAddressObjectName));
            }
        }
        // 复用目的地址组对象
        if(ArrayUtils.isNotEmpty(dstRefIpAddressObjectGroup)){

            for (String dstRefIpAddressObjectGroupName:dstRefIpAddressObjectGroup) {
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match destination-address %s \n", srcZoneJoin, dstZoneJoin, name,dstRefIpAddressObjectGroupName));
            }
        }

        // 处理service any的问题
        if(ArrayUtils.isEmpty(refServiceObject) && ArrayUtils.isEmpty(refServiceObjectGroup) && CollectionUtils.isEmpty(newServiceObjectNameList)){
            securityPolicyCl.append(String.format("set security policies %s %s policy %s match application any \n", srcZoneJoin, dstZoneJoin, name));
        }
//        本次生成的服务
                if(CollectionUtils.isNotEmpty(newServiceObjectNameList)){
                 if(newServiceObjectNameList.size()==1){
                        if (newServiceObjectNameList.get(0).equalsIgnoreCase("icmp")) {
                            securityPolicyCl.append(String.format("set security policies %s %s policy %s match application junos-icmp-all\n", srcZoneJoin, dstZoneJoin, name));
                        }
                    securityPolicyCl.append(String.format("set security policies %s %s policy %s match application %s\n", srcZoneJoin, dstZoneJoin, name, newServiceObjectNameList.get(0)));
                 }
                else {
                    securityPolicyCl.append(String.format("set security policies %s %s policy %s match application %s\n", srcZoneJoin, dstZoneJoin, name, newServiceGroupObjectName));
            }
        }
        // 复用服务对象
        if(ArrayUtils.isNotEmpty(refServiceObject)){

            for (String serviceObjectName:refServiceObject) {
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match application %s\n", srcZoneJoin, dstZoneJoin, name, serviceObjectName));
            }
        }
        //复用服务组对象
        if(ArrayUtils.isNotEmpty(refServiceObjectGroup)){
            for (String serviceObjectGroupName:refServiceObjectGroup) {
                securityPolicyCl.append(String.format("set security policies %s %s policy %s match application %s\n", srcZoneJoin, dstZoneJoin, name, serviceObjectGroupName));
            }
        }
//                动作
        if(StringUtils.isNotEmpty(action)) {
            securityPolicyCl.append(String.format("set security policies %s %s %s then %s",srcZoneJoin, dstZoneJoin, name,action)).append(StringUtils.LF);
        }
//        策略移动
        int moveSeatCode=moveSeatEnum.getCode();
        if (ObjectUtils.isNotEmpty(moveSeatEnum)) {
            if (moveSeatCode == MoveSeatEnum.FIRST.getCode() && StringUtils.isNotBlank(swapRuleNameId)) {
                securityPolicyCl.append(String.format("insert security policies %s %s policy %s before policy %s", srcZoneJoin, dstZoneJoin, name, moveSeatEnum.getKey())).append(StringUtils.LF);
            } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                securityPolicyCl.append(String.format("insert security policies %s %s policy %s %s policy %s\n", srcZoneJoin, dstZoneJoin, name, moveSeatEnum.getKey(), swapRuleNameId));
            }
        }

        securityPolicyCl.append("commit \n");
        securityPolicyCl.append("exit \n");
        securityPolicyCl.append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String generatePortRefStrCommandLine(String[] strRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                          Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray,
                                          String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray,
                                         String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray,
                                         PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {

        StringBuffer dstPortBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s%s%s",dstRangePortArray[0].getStart(),SymbolsEnum.MINUS.getValue(),dstRangePortArray[0].getEnd()));
        }else {
            dstPortBuffer.append(String.format("junos-tcp-any"));
        }
        StringBuffer tcpCommandLine = new StringBuffer();
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            tcpCommandLine.append(String.format("protocol tcp destination-port %s ",dstPortBuffer.toString()));
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {

        StringBuffer dstPortBuffer = new StringBuffer();

        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            dstPortBuffer.append(dstSinglePortArray[0]);
        } else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            dstPortBuffer.append(dstSinglePortStrArray[0]);
        } else if (ArrayUtils.isNotEmpty(dstRangePortArray)){
            dstPortBuffer.append(String.format("%s%s%s",dstRangePortArray[0].getStart(),SymbolsEnum.MINUS.getValue(),dstRangePortArray[0].getEnd()));
        }else {
            dstPortBuffer.append(String.format("junos-tcp-any"));
        }
        StringBuffer tcpCommandLine = new StringBuffer();
        if(StringUtils.isNotBlank(dstPortBuffer.toString())){
            tcpCommandLine.append(String.format("protocol udp destination-port %s ",dstPortBuffer.toString()));
        }
        tcpCommandLine.append(StringUtils.LF);
        return tcpCommandLine.toString();
    }


    @Override
    public String generateServiceObjectGroupName(String name, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isNotEmpty(name)) {
            String groupName = String.format("SG%s%s%s%s", SymbolsEnum.UNDERLINE.getValue(),name,SymbolsEnum.UNDERLINE.getValue(),name.hashCode());
            return groupName;
        }
        return null;
    }

    @Override
    public String generateServiceObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, String name, String id, String attachStr, List<ServiceParamDTO> serviceParamDTOList,
                                                        String description, String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray, Map<String, Object> map, String[] args) throws Exception {

            //生成服务对象

        StringBuffer serviceObjectGroupCl = new StringBuffer();
        String zone="any";
        attachStr="true";
        List<String> iparray = new ArrayList<>();
        iparray.add(attachStr);
        if(CollectionUtils.isEmpty(serviceParamDTOList)){
            return null;
        }
        if(StringUtils.isBlank(name)){
            name = this.createServiceObjectGroupName(serviceParamDTOList,serviceObjectNameRefArray,serviceObjectGroupNameRefArray,null,null);
        }
        if (null == map) {
            map = new HashMap<String, Object>();
        }
        map.put("ObjectGroupName",name);
        if (CollectionUtils.isNotEmpty(serviceParamDTOList) ||serviceParamDTOList.size()>1) {
            String serviceObjectCommandLine = this.generateServiceObjectCommandLine(statusTypeEnum,null,null,null,serviceParamDTOList,null,map,null);
            serviceObjectGroupCl.append(serviceObjectCommandLine);
        }

        if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
            for (String serviceObjectName:serviceObjectNameRefArray) {
                serviceObjectGroupCl.append(String.format(" service %s \n",serviceObjectName));
            }
        }
        if(ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray)){
            for (String serviceObjectGroupName:serviceObjectGroupNameRefArray) {
                serviceObjectGroupCl.append(String.format(" service %s \n",serviceObjectGroupName));
            }
        }
        return serviceObjectGroupCl.toString();
    }

    @Override
    public String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateServiceObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String id, String attachStr, List<ServiceParamDTO> serviceParamDTOList,
                                                   String description, Map<String, Object> map, String[] args) throws Exception {
            StringBuffer serviceObjectCl = new StringBuffer();
            String serviceName="";
            if (CollectionUtils.isEmpty(serviceParamDTOList)) {
                return null;
            }
            if (StringUtils.isNotBlank(name)) {
                serviceName=this.generateServiceObjectName(name, null, null);
                serviceObjectCl.append(String.format("set applications application %s ", serviceName));
                if (StringUtils.isNotBlank(description)) {
                    serviceObjectCl.append(String.format(" description %s \n", description));
                }
            }
            for (ServiceParamDTO serviceParamDTO : serviceParamDTOList) {
                if (ProtocolTypeEnum.TCP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    serviceName=this.createServiceObjectName(serviceParamDTO, null, null);
                    serviceObjectCl.append(String.format("set applications application %s ", serviceName));
                    serviceObjectCl.append(this.generateTCPCommandLine(null, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                    if (serviceParamDTOList.size()>1) {
                        serviceObjectCl.append(String.format("set applications application-set %s application %s  \n", map.get("ObjectGroupName"),
                                this.createServiceObjectName(serviceParamDTO, null, null)));
                    }
                } else if (ProtocolTypeEnum.UDP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    serviceName=createServiceObjectName(serviceParamDTO, null, null);
                    serviceObjectCl.append(String.format("set applications application %s ",serviceName));
                    serviceObjectCl.append(this.generateUDPCommandLine(null, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                    if (serviceParamDTOList.size()>1) {
                        serviceObjectCl.append(String.format("set applications application-set %s application %s  \n", map.get("ObjectGroupName"),
                                this.createServiceObjectName(serviceParamDTO, null, null)));
                    }
                } else if (ProtocolTypeEnum.ICMP.getType().equalsIgnoreCase(serviceParamDTO.getProtocol().getType())) {
                    serviceName=createServiceObjectName(serviceParamDTO, null, null);
                    serviceObjectCl.append(String.format("set applications application %s ",serviceName));
                    serviceObjectCl.append(this.generateICMPCommandLine(null, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                    if (serviceParamDTOList.size()>1) {
                        serviceObjectCl.append(String.format("set applications application-set %s application %s  \n", map.get("ObjectGroupName"),
                                this.createServiceObjectName(serviceParamDTO, null, null)));
                    }
                } else {
                    serviceName=createServiceObjectName(serviceParamDTO, null, null);
                    serviceObjectCl.append(String.format("set applications application %s ",serviceName));
                    serviceObjectCl.append(this.generateOtherCommandLine(null, serviceParamDTO.getProtocolAttachTypeArray(), serviceParamDTO.getProtocolAttachCodeArray(),
                            serviceParamDTO.getSrcSinglePortArray(), serviceParamDTO.getSrcSinglePortStrArray(), serviceParamDTO.getSrcRangePortArray(),
                            serviceParamDTO.getDstSinglePortArray(), serviceParamDTO.getDstSinglePortStrArray(), serviceParamDTO.getDstRangePortArray(),
                            serviceParamDTO.getTimeOutArray(), null, null, null));
                    if (serviceParamDTOList.size()>1) {
                        serviceObjectCl.append(String.format("set applications application-set %s application %s  \n", map.get("ObjectGroupName"),
                                this.createServiceObjectName(serviceParamDTO, null, null)));
                    }
                }
            }
            return serviceObjectCl.toString();

    }


    private String getTimeObjectName(String endTime,Map<String,Object> map){
        if(StringUtils.isBlank((String) map.get("cycleStart"))){
            String endTimeStr = TotemsTimeUtils.transformDateFormat(endTime, TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.COMMON_TIME_DAY_FORMAT);
            return String.format("to%s",endTimeStr);
        }else{
            return String.format("%s_%s",map.get("cycleStart"),map.get("cycleEnd"));
        }
    }

    @Override
    public String generateAbsoluteTimeCommandLine(String name, String attachStr, AbsoluteTimeParamDTO absoluteTimeParamDTO,
                                                  Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isEmpty(name)){
            this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
        }
        String timeNameCommandLine = generateTimeObjectName(name, map, null);

        StringBuffer timeCommandLineBuffer = new StringBuffer();
        timeCommandLineBuffer.append(String.format("set schedulers scheduler %s",timeNameCommandLine));

        // 指定绝对计划 absolute {[start start-date start-time] [end end-date end-time]}
        String startTime = TotemsTimeUtils.transformDateFormat(String.format("%s %s",absoluteTimeParamDTO.getStartDate(),absoluteTimeParamDTO.getStartTime()), TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.JUNIPER_SRX_TIME_FORMAT);
        String endTime = TotemsTimeUtils.transformDateFormat(String.format("%s %s",absoluteTimeParamDTO.getEndDate(),absoluteTimeParamDTO.getEndTime()), TimeConstants.EUROPEAN_TIME_FORMAT, TimeConstants.JUNIPER_SRX_TIME_FORMAT);
        timeCommandLineBuffer.append(String.format(" start-time %s stop-time %s  %s end %s ",startTime,endTime,startTime,endTime)).append(StringUtils.LF);
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
        timeCommandLineBuffer.append(String.format("schedule %s \n",timeNameCommandLine));

        //指定周期计划 periodic {daily | weekdays | weekend | [monday] […] [sunday]} starttime to end-time
        //周期计划
        timeCommandLineBuffer.append(" periodic ");
        if(ArrayUtils.isNotEmpty(periodicTimeParamDTO.getCycle())){
            for (String date:periodicTimeParamDTO.getCycle()) {
                timeCommandLineBuffer.append(date).append(StringUtils.SPACE);
            }
        }
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getCycleStart())){
            timeCommandLineBuffer.append(String.format(" %s ",periodicTimeParamDTO.getCycleStart()));
        }
        if(StringUtils.isNotBlank(periodicTimeParamDTO.getCycleEnd())){
            timeCommandLineBuffer.append(String.format(" to %s ",periodicTimeParamDTO.getCycleEnd()));
        }
        timeCommandLineBuffer.append(StringUtils.LF);
        timeCommandLineBuffer.append("exit").append(StringUtils.LF);
        return timeCommandLineBuffer.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        String zone = "";
        if(args == null) {
            zone = "any";
        }else {
            zone = args[0];
        }
        if(ArrayUtils.isEmpty(singleIpArray)){
            return StringUtils.EMPTY;
        }
        StringBuffer ipv6ArrayCommandLine = new StringBuffer();
        ipv6ArrayCommandLine.append(String.format("set security zones security-zone %s address-book address %s %s/128\n",zone,map.get("singleIpAddressObjectName"),singleIpArray[0]));
        return ipv6ArrayCommandLine.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args)  throws Exception {
        String rangeIpV6 = (String) map.get("RangeIpV6");
        //默认有地址对象名
        String zone = args[0];
        if(StringUtils.isBlank(zone)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuffer ipv6SubnetList = new StringBuffer();
            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(rangeIpV6);

            for (String subIpv6 : toSubnetList) {
                ipv6SubnetList.append(String.format("set security zones security-zone %s address-book address %s %s\n",zone,map.get("subnetIntAddressObjectName"),subIpv6));
            }

            return ipv6SubnetList.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception{
        //默认有地址对象名
        String zone = args[0];
        if(StringUtils.isBlank(zone)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuffer subnetIpv6Cl = new StringBuffer();
            if(ArrayUtils.isEmpty(subnetIpArray)){
                return StringUtils.EMPTY;
            }
            for(int i = 0;i<subnetIpArray.length;i++){
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIpArray[i];
                subnetIpv6Cl.append(String.format("set security zones security-zone %s address-book address %s %s/%s\n",zone,map.get("subnetIntAddressObjectName"),ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
            }
            return subnetIpv6Cl.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "configure\n";
    }

    @Override
    public String generatePostCommandline(Map<String, Object> map, String[] args) {
        return "commit\nexit\n";
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("set routing-options static route %s/%s reject\n",ip,mask);
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("delete set routing-options static route %s/%s reject\n",ip,mask);
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("set routing-options static route %s/%s reject\n",ip,mask);
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("delete set routing-options static route %s/%s reject\n",ip,mask);
    }

    @Override
    public String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if (ObjectUtils.isNotEmpty(map)) {
            if(ObjectUtils.isNotEmpty(map.get("from")) && "objectCheck".equals(map.get("from"))){
                //来自对象检查的调用
                if(StringUtils.isNotEmpty(groupName) && groupName.contains("_zone_")){
                    String zone = groupName.substring(0,groupName.indexOf("_"));
                    return String.format("delete security zones security-zone %s address-book address-set %s\n",zone,groupName);
                }else {
                    return null;
                }
            }else{
                return String.format("delete security zones security-zone %s address-book address-set %s address %s \n",
                        map.get(FieldConstants.JUNIPER_ZONE_NAME), map.get(FieldConstants.JUNIPER_OBJECT_GROUP_NAME), map.get(FieldConstants.JUNIPER_OBJECT_NAME));
            }
        } else {
            return null;
        }
    }

    @Override
    public String deleteIpAddressObjectCommandLine(RuleIPTypeEnum ipTypeEnum, String delStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if (ObjectUtils.isNotEmpty(map)) {
            if(ObjectUtils.isNotEmpty(map.get("from")) && "objectCheck".equals(map.get("from"))){
             //来自对象检查的调用
                if(StringUtils.isNotEmpty(name) && name.contains("_zone_")){
                    String zone = name.substring(0,name.indexOf("_"));
                    return String.format("delete security zones security-zone %s address-book address %s\n",zone,name);
                }else {
                    return null;
                }
            }else {
                return String.format("delete security zones security-zone %s address-book address %s %s \n",
                        map.get(FieldConstants.JUNIPER_ZONE_NAME), map.get(FieldConstants.JUNIPER_OBJECT_NAME), map.get(FieldConstants.JUNIPER_IP_NAME));
            }
        } else {
            return null;
        }
    }

    @Override
    public String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(groupName)) {
            return String.format("delete applications application %s \n", groupName);
        } else {
            return null;
        }
    }

    @Override
    public String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isNotEmpty(name)) {
            return String.format("delete applications application %s \n", name);
        } else {
            return null;
        }
    }

    @Override
    public String generateTimeObjectName(String name, Map<String, Object> map, String[] args) {
        return name;
    }

    @Override
    public String deleteAbsoluteTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args) {
        return String.format("delete schedulers scheduler %s\n",timeFlag);
    }

    @Override
    public String deletePeriodicTimeCommandLine(String timeFlag,Map<String, Object> map, String[] args) {
        return String.format("delete schedulers scheduler %s\n",timeFlag);
    }

    /**
     * ip范围生成地址对象名
     * @param ipStart
     * @param ipEnd
     * @param map
     * @param args
     * @return
     */
    public String createIpAddressObjectNameByIpRange(String ipStart,String ipEnd, Map<String, Object> map, String[] args) {
        RuleIPTypeEnum ipType = RuleIPTypeEnum.IP4;
        if (ipStart.contains(":") || ipEnd.contains(":")) {
            ipType = RuleIPTypeEnum.IP6;
        }
        if (RuleIPTypeEnum.IP6.equals(ipType)) {
            ipEnd = ipEnd.substring(ipEnd.lastIndexOf(":") + 1);
        }
        return StringUtils.join("R_", ipStart, SymbolsEnum.MINUS.getValue(), ipEnd);
    }

    /**
     * ip子网生成地址对象名
     * @param ip
     * @param netmask
     * @param map
     * @param args
     * @return
     */
    public String createIpAddressObjectNameByIpMask(String ip, int netmask, Map<String, Object> map, String[] args) {
        return StringUtils.join("SUB_",ip, SymbolsEnum.VIRGULE.getValue(),netmask);
    }

    public String createIpAddressObjectNameBySingleIpArray(String[] singleIpArray, Map<String, Object> map, String[] args) {
        if(singleIpArray == null){
            return StringUtils.EMPTY;
        } else {
            if(singleIpArray.length == 1){
                return singleIpArray[0];
            }
            int hashNum = 0;
            for (String singleIp : singleIpArray) {
                hashNum += singleIp.hashCode();
            }
            return "addg_"+ Math.abs(hashNum);
        }
    }
}
