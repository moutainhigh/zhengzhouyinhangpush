package com.abtnetworks.totems.command.line.abs.ip;

import com.abtnetworks.totems.command.line.abs.GenericPolicyGeneratorBean;
import com.abtnetworks.totems.command.line.dto.IpAddressMacDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.SymbolsEnum;
import com.abtnetworks.totems.command.line.inf.ip.*;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:09'.
 */
public abstract class GenericIpAddressObject extends GenericPolicyGeneratorBean
        implements IpAddressV4Interface, IpAddressV6Interface, IpAddressObjectInterface, MacAddressObjectInterface, MacAddressObjectGroupInterface {

    /**
     * 单个ip生成地址对象名
     * @param ip
     * @param map
     * @param args
     * @return
     */
    public String createIpAddressObjectNameByIp(String ip, Map<String, Object> map, String[] args) {
        if(StringUtils.isBlank(ip)){
            return null;
        } else {
            return ip;
        }
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
            return "add_"+ Math.abs(hashNum);
        }
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
        if(ipStart.contains(":") || ipEnd.contains(":")){
            ipType = RuleIPTypeEnum.IP6;
        }
        if(RuleIPTypeEnum.IP6.equals(ipType)){
            ipEnd = ipEnd.substring(ipEnd.lastIndexOf(":")+1);
        }
        return StringUtils.join(ipStart, SymbolsEnum.UNDERLINE.getValue(),ipEnd);
    }

    public String createIpAddressObjectNameByRangIpArray(IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) {
        if(rangIpArray == null){
            return StringUtils.EMPTY;
        }
        if(rangIpArray.length == 1){
            return createIpAddressObjectNameByIpRange(rangIpArray[0].getStart(),rangIpArray[0].getEnd(),map,args);
        }
        int hashNum = 0;
        for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
            hashNum += (ipAddressRangeDTO.getStart().hashCode()+ipAddressRangeDTO.getEnd().hashCode());
        }
        return String.format("add_%s",Math.abs(hashNum));
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
        return StringUtils.join(ip, SymbolsEnum.UNDERLINE.getValue(),netmask);
    }

    public String createIpAddressObjectNameByIpSubArray(IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray, Map<String, Object> map, String[] args) {
        if(subnetIntIpArray == null && subnetStrIpArray == null){
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) == 1){
            if(ArrayUtils.isNotEmpty(subnetIntIpArray) && StringUtils.isNotBlank(subnetIntIpArray[0].getIp())){
                return String.format("%s_%s",subnetIntIpArray[0].getIp(),subnetIntIpArray[0].getMask());
            }
            if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
                return String.format("%s_%s",subnetStrIpArray[0].getIp(),TotemsIpUtils.getMaskBit(subnetStrIpArray[0].getMask()));
            }
        }
        int hashNum = 0;
        if(subnetIntIpArray != null){
            for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                hashNum +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
            }
        }
        if(subnetStrIpArray != null){
            for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                hashNum +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
            }
        }
        return "add_"+Math.abs(hashNum);
    }


    public String createIpAddressObjectNameByIpSubArray(IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,boolean flag, Map<String, Object> map, String[] args) {
        if(subnetIntIpArray == null && subnetStrIpArray == null){
            return StringUtils.EMPTY;
        }
        if(flag) {
            if (ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) == 1) {
                if (ArrayUtils.isNotEmpty(subnetIntIpArray) && StringUtils.isNotBlank(subnetIntIpArray[0].getIp())) {
                    return String.format("%s/%s", subnetIntIpArray[0].getIp(), subnetIntIpArray[0].getMask());
                }
                if (ArrayUtils.isNotEmpty(subnetStrIpArray)) {
                    return String.format("%s/%s", subnetStrIpArray[0].getIp(), TotemsIpUtils.getMaskBit(subnetStrIpArray[0].getMask()));
                }
            }
        }
        int hashNum = 0;
        if(subnetIntIpArray != null){
            for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                hashNum +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
            }
        }
        if(subnetStrIpArray != null){
            for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                hashNum +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
            }
        }
        return "add_"+Math.abs(hashNum);
    }

    /**
     * mac地址对象生成对象名称
     */
    public String createMacIpAddressName(IpAddressMacDTO ipAddressMacDTO,Map<String, Object> map, String[] args){
        if(StringUtils.isEmpty(ipAddressMacDTO.getMacAddress())){
            return StringUtils.EMPTY;
        }else{
            return ipAddressMacDTO.getMacAddress();
        }
    }
    /**
     * mac地址对象生成对象名称
     */
    public String createMacIpAddressNameByArray(IpAddressMacDTO[] ipAddressMacDTOArr,Map<String, Object> map, String[] args){
        if(ArrayUtils.isEmpty(ipAddressMacDTOArr)){
            return StringUtils.EMPTY;
        }else{
            int hashNum = 0;
            for (IpAddressMacDTO ipAddressMacDTO : ipAddressMacDTOArr) {
                hashNum += ipAddressMacDTO.getMacAddress().hashCode();
            }
            return "add_"+Math.abs(hashNum);
        }
    }

    public String createIpAddressObjectNameByHost(String host, Map<String, Object> map, String[] args) {
        return StringUtils.join("HOST_",host);
    }

    /**
     * any
     */
    public String generateIpV4CommandLineByAny() {
        return "any";
    }

    public String createIpAddressObjectNameByParamDTO(String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                      IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,String[] hosts,
                                                      String[] objectNameRefArray,
                                                      Map<String, Object> map, String[] args) {
        if(ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(hosts) && ArrayUtils.isEmpty(objectNameRefArray)){
            return StringUtils.EMPTY;
        }
        if(ArrayUtils.isNotEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(hosts) && ArrayUtils.isEmpty(objectNameRefArray)){
            if(singleIpArray.length == 1){
                return singleIpArray[0];
            } else {
                int hashNum = 0;
                for (String singleIp : singleIpArray) {
                    hashNum += singleIp.hashCode();
                }
                return String.format("add_%s",Math.abs(hashNum));
            }
        }
        if(ArrayUtils.isNotEmpty(hosts) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(objectNameRefArray)){
            if(hosts.length == 1){
                return hosts[0];
            } else {
                int hashNum = 0;
                for (String host : hosts) {
                    hashNum += host.hashCode();
                }
                return String.format("add_%s",Math.abs(hashNum));
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray) && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray)  && ArrayUtils.isEmpty(hosts) && ArrayUtils.isEmpty(objectNameRefArray)){
            if(rangIpArray.length == 1){
                if(rangIpArray[0].getEnd().contains(":")){
                    String end = rangIpArray[0].getEnd();
                    end = end.substring(end.indexOf(":")+1);
                    return String.format("%s_%s",rangIpArray[0].getStart(),end);
                }
                return String.format("%s_%s",rangIpArray[0].getStart(),rangIpArray[0].getEnd());
            } else {
                int hashNum = 0;
                for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                    hashNum += (ipAddressRangeDTO.getStart().hashCode() + ipAddressRangeDTO.getEnd().hashCode());
                }
                return String.format("add_%s",Math.abs(hashNum));
            }
        }
        if((ArrayUtils.isNotEmpty(subnetIntIpArray) || ArrayUtils.isNotEmpty(subnetStrIpArray))
                && ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(objectNameRefArray) && ArrayUtils.isEmpty(hosts) ){
            if(ArrayUtils.getLength(subnetIntIpArray) + ArrayUtils.getLength(subnetStrIpArray) == 1){
                if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
                    return String.format("%s_%s",subnetIntIpArray[0].getIp(),subnetIntIpArray[0].getMask());
                } else {
                    return String.format("%s_%s",subnetStrIpArray[0].getIp(), TotemsIpUtils.getMaskBit(subnetStrIpArray[0].getMask()));
                }
            }
            int hashNum = 0;
            if(subnetIntIpArray != null){
                for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                    hashNum +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
                }
            }
            if(subnetStrIpArray != null){
                for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                    hashNum +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
                }
            }
            return String.format("add_%s",Math.abs(hashNum));
        }

        StringBuffer ipAddressName = new StringBuffer("add_");
        int num = 0;
        if(ArrayUtils.isNotEmpty(singleIpArray)){
            for (String singeIp : singleIpArray) {
                num += singeIp.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(rangIpArray)){
            for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                num += (ipAddressRangeDTO.getStart().hashCode() + ipAddressRangeDTO.getEnd().hashCode());
            }
        }
        if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
            for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                num +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
            }
        }
        if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
            for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                num +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
            }
        }
        if(ArrayUtils.isNotEmpty(objectNameRefArray)){
            for (String objectName : objectNameRefArray) {
                num += objectName.hashCode();
            }
        }
        if(ArrayUtils.isNotEmpty(hosts)){
            for (String host : hosts) {
                num += host.hashCode();
            }
        }
        ipAddressName.append(Math.abs(num));
        return ipAddressName.toString();
    }
}
