package com.abtnetworks.totems.command.line.abs.ip;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.inf.ip.IpAddressObjectGroupInterface;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:10'.
 */
public abstract class GenericIpAddressObjectGroup extends GenericIpAddressObject implements IpAddressObjectGroupInterface {

    /**
     * 创建ip地址组名称
     * @param args 扩展参数 String[] 类型
     * @return
     */
    public String createIpAddressObjectGroupName(String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                 IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                 String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,Map<String, Object> map, String[] args) {
        if(ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray)&& ArrayUtils.isEmpty(subnetIntIpArray)&& ArrayUtils.isEmpty(subnetStrIpArray)
                && ArrayUtils.isEmpty(interfaceArray) && ArrayUtils.isEmpty(fqdnArray) && ArrayUtils.isEmpty(objectNameRefArray) && ArrayUtils.isEmpty(objectGroupNameRefArray)){
            return StringUtils.EMPTY;
        } else {
            if(ArrayUtils.isEmpty(singleIpArray) && ArrayUtils.isEmpty(rangIpArray)&& ArrayUtils.isEmpty(subnetIntIpArray)&& ArrayUtils.isEmpty(subnetStrIpArray)
                    && ArrayUtils.isEmpty(interfaceArray) && ArrayUtils.isEmpty(fqdnArray) && (ArrayUtils.isNotEmpty(objectNameRefArray) || ArrayUtils.isNotEmpty(objectGroupNameRefArray)) ){
                if(ArrayUtils.isNotEmpty(objectNameRefArray) && ArrayUtils.isEmpty(objectGroupNameRefArray)){
                    if(ArrayUtils.getLength(objectNameRefArray) == 1){
                        return objectNameRefArray[0];
                    } else {
                        int num = 0;
                        for (String objectName : objectNameRefArray) {
                            num += objectName.hashCode();
                        }
                        return "addg_"+Math.abs(num);
                    }
                } else if(ArrayUtils.isNotEmpty(objectGroupNameRefArray) && ArrayUtils.isEmpty(objectNameRefArray)){
                    if(ArrayUtils.getLength(objectGroupNameRefArray) == 1){
                        return objectGroupNameRefArray[0];
                    } else {
                        int num = 0;
                        for (String objectGroupName : objectGroupNameRefArray) {
                            num += objectGroupName.hashCode();
                        }
                        return "addg_"+Math.abs(num);
                    }
                } else {
                    int addgNum = 0;
                    if(ArrayUtils.isNotEmpty(objectNameRefArray)){
                        for (String objectName : objectNameRefArray) {
                            addgNum += objectName.hashCode();
                        }
                        addgNum += Arrays.hashCode(objectNameRefArray);
                    }
                    if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
                        for (String objectGroupName : objectGroupNameRefArray) {
                            addgNum += objectGroupName.hashCode();
                        }
                    }
                    return "addg_"+ Math.abs(addgNum);
                }

            } else {
                int addgNum = 0;
                if(ArrayUtils.isNotEmpty(singleIpArray)){
                    for (String singeIp : singleIpArray) {
                        addgNum += singeIp.hashCode();
                    }
                }
                if(ArrayUtils.isNotEmpty(rangIpArray)){
                    for (IpAddressRangeDTO ipAddressRangeDTO : rangIpArray) {
                        addgNum += (ipAddressRangeDTO.getStart().hashCode() + ipAddressRangeDTO.getEnd().hashCode());
                    }
                }
                if(ArrayUtils.isNotEmpty(subnetIntIpArray)){
                    for (IpAddressSubnetIntDTO subnetIntDTO : subnetIntIpArray) {
                        addgNum +=(subnetIntDTO.getIp().hashCode() + subnetIntDTO.getMask());
                    }
                }
                if(ArrayUtils.isNotEmpty(subnetStrIpArray)){
                    for (IpAddressSubnetStrDTO subnetStrDTO : subnetStrIpArray) {
                        addgNum +=(subnetStrDTO.getIp().hashCode() + subnetStrDTO.getMask().hashCode());
                    }
                }
                if(ArrayUtils.isNotEmpty(objectNameRefArray)){
                    for (String objectName : objectNameRefArray) {
                        addgNum += objectName.hashCode();
                    }
                }
                if(ArrayUtils.isNotEmpty(fqdnArray)){
                    for (String host : fqdnArray) {
                        addgNum += host.hashCode();
                    }
                }
                if(ArrayUtils.isNotEmpty(objectGroupNameRefArray)){
                    for (String objectGroupName : objectGroupNameRefArray) {
                        addgNum += objectGroupName.hashCode();
                    }
                }
                return "addg_"+ Math.abs(addgNum);
            }
        }
    }
}
