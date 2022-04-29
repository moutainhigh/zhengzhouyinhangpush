package com.abtnetworks.totems.vender.checkpoint.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.utils.IPUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.util.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class SecurityCheckpointImpl extends OverAllGeneratorAbstractBean {

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray,
                                                     IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                     String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr,
                                                     String delStr, Map<String, Object> map, String[] args) throws Exception {
        if(StringUtils.isBlank(name)){
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            //生成新增策略命令行
            switch (ipTypeEnum){
                case IP6:
                    if(!ArrayUtils.isEmpty(singleIpArray)){
                        sb.append(generateSingleIpV6CommandLine(statusTypeEnum,singleIpArray,null,new String[]{name}));
                    }
                    if(!ArrayUtils.isEmpty(rangIpArray)){
                        sb.append(generateRangeIpV6CommandLine(statusTypeEnum,rangIpArray,null,new String[]{name}));
                    }
                    if(!ArrayUtils.isEmpty(subnetIntIpArray)){
                        sb.append(generateSubnetIntIpV6CommandLine(statusTypeEnum,subnetIntIpArray,null, null,new String[]{name}));
                    }
                    break;
                case IP4:
                    if(!ArrayUtils.isEmpty(singleIpArray)){
                        sb.append(generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,null,new String[]{name}));
                    }
                    if(!ArrayUtils.isEmpty(rangIpArray)){
                        sb.append(generateRangeIpV4CommandLine(statusTypeEnum,rangIpArray,null,new String[]{name}));
                    }
                    if(!ArrayUtils.isEmpty(subnetIntIpArray)){
                        sb.append(generateSubnetIntIpV4CommandLine(statusTypeEnum,subnetIntIpArray,null,null,new String[]{name}));
                    }
                    if(!ArrayUtils.isEmpty(subnetStrIpArray)){
                        sb.append(generateSubnetStrIpV4CommandLine(statusTypeEnum,subnetStrIpArray, null,null,new String[]{name}));
                    }
            }
            if(!ArrayUtils.isEmpty(fqdnArray)){
                sb.append(generateHostCommandLine(statusTypeEnum,fqdnArray,null,null));
            }
        }
        return sb.toString();
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if(StringUtils.isBlank(objectName)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuilder sb = new StringBuilder();
            for (String ip:singleIpArray){
                sb.append("mgmt add host name \"").append(objectName).append("\" ip-address \"").append(ip).append('"').append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuilder sb = new StringBuilder();
            //mgmt add dns-domain name ".www.example.com" is-sub-domain false
            //	参数 name  DNS域名应始终以 '.' 开头 （必填）
            //	is-sub-domain 是否匹配除域本身之外的子域（必填）
            for (String host:hosts){
                sb.append("mgmt add dns-domain name \".").append(host).append("\"  is-sub-domain false").append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if(StringUtils.isBlank(objectName)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuilder sb = new StringBuilder();
            //mgmt add address-range name "Range1" ip-address-first "1.1.1.1" ip-address-last "1.1.1.10"
            for(int i = 0;i<rangIpArray.length;i++){
                IpAddressRangeDTO ipAddressRangeDTO = rangIpArray[i];
                sb.append("mgmt add address-range name \"").append(objectName).append("\" ip-address-first \"").
                        append(ipAddressRangeDTO.getStart()).append("\" ip-address-last \"").append(ipAddressRangeDTO.getEnd()).
                        append('"').append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if(StringUtils.isBlank(objectName)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuilder sb = new StringBuilder();
            //mgmt add network name "Subnet1" subnet "1.1.1.0" subnet-mask "255.255.255.0"
            for(int i = 0;i<subnetIpArray.length;i++){
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIpArray[i];
                sb.append("mgmt add network name \"").append(objectName).append("\" subnet \"")
                        .append(ipAddressSubnetIntDTO.getIp()).append("\" subnet-mask \"").
                        append(IPUtil.getMaskByMaskBit(String.valueOf(ipAddressSubnetIntDTO.getMask()))).
                        append('"').append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        return generateSingleIpV4CommandLine(statusTypeEnum,singleIpArray,map,args);
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return generateRangeIpV4CommandLine(statusTypeEnum,rangIpArray,map,args);
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if(StringUtils.isBlank(objectName)){
            return StringUtils.EMPTY;
        }
        if(statusTypeEnum.equals(StatusTypeEnum.MODIFY) ||statusTypeEnum.equals(StatusTypeEnum.DELETE)){

        }else {
            StringBuilder sb = new StringBuilder();
            // mgmt add network name "Subnet1" subnet "ff00::ff00" mask-length "120"
            for(int i = 0;i<subnetIpArray.length;i++){
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIpArray[i];
                sb.append("mgmt add network name \"").append(objectName).append("\" subnet \"")
                        .append(ipAddressSubnetIntDTO.getIp()).append("\" mask-length \"").
                        append(ipAddressSubnetIntDTO.getMask()).
                        append('"').append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }
}
