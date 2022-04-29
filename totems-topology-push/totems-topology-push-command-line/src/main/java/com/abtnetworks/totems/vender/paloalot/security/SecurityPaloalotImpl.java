package com.abtnetworks.totems.vender.paloalot.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.utils.IPUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class SecurityPaloalotImpl extends OverAllGeneratorAbstractBean {

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray,
                                                     IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                     String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String description, String attachStr,
                                                     String delStr, Map<String, Object> map, String[] args) throws Exception {
        if (StringUtils.isBlank(name)) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            //生成新增策略命令行
            switch (ipTypeEnum) {
                case IP6:
                    if (!ArrayUtils.isEmpty(singleIpArray)) {
                        sb.append(generateSingleIpV6CommandLine(statusTypeEnum, singleIpArray, null, new String[]{name}));
                    }
                    if (!ArrayUtils.isEmpty(rangIpArray)) {
                        sb.append(generateRangeIpV6CommandLine(statusTypeEnum, rangIpArray, null, new String[]{name}));
                    }
                    if (!ArrayUtils.isEmpty(subnetIntIpArray)) {
                        sb.append(generateSubnetIntIpV6CommandLine(statusTypeEnum, subnetIntIpArray, null, null, new String[]{name}));
                    }
                    break;
                case IP4:
                    if (!ArrayUtils.isEmpty(singleIpArray)) {
                        sb.append(generateSingleIpV4CommandLine(statusTypeEnum, singleIpArray, null, new String[]{name}));
                    }
                    if (!ArrayUtils.isEmpty(rangIpArray)) {
                        sb.append(generateRangeIpV4CommandLine(statusTypeEnum, rangIpArray, null, new String[]{name}));
                    }
                    if (!ArrayUtils.isEmpty(subnetIntIpArray)) {
                        sb.append(generateSubnetIntIpV4CommandLine(statusTypeEnum, subnetIntIpArray, null, null, new String[]{name}));
                    }
                    if (!ArrayUtils.isEmpty(subnetStrIpArray)) {
                        sb.append(generateSubnetStrIpV4CommandLine(statusTypeEnum, subnetStrIpArray, null, null, new String[]{name}));
                    }
            }
            if (!ArrayUtils.isEmpty(fqdnArray)) {
                sb.append(generateHostCommandLine(statusTypeEnum, fqdnArray, null, new String[]{name}));
            }
        }
        return sb.toString();
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if (StringUtils.isBlank(objectName)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            StringBuilder sb = new StringBuilder();
            for (String ip : singleIpArray) {
                sb.append("set address ").append(objectName).append(" ip-netmask ").append(ip).append("/32").append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
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
            //set address name1 fqdn www.baidu.com
            StringBuilder sb = new StringBuilder();
            for (String host : hosts) {
                sb.append("set address ").append(objectName).append(" fqdn ").append(host).append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if (StringUtils.isBlank(objectName)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            StringBuilder sb = new StringBuilder();
            //set address test1 ip-netmask ff00::ff00/1
            for (int i = 0; i < subnetIpArray.length; i++) {
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIpArray[i];
                sb.append("set address ").append(objectName).append(" ip-netmask ").append(ipAddressSubnetIntDTO.getIp())
                        .append('/').append(ipAddressSubnetIntDTO.getMask()).append(StringUtils.LF);
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
        //默认有地址对象名
        String objectName = args[0];
        if (StringUtils.isBlank(objectName)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            //set address test1 ip-netmask ff00::ff00/128
            StringBuilder sb = new StringBuilder();
            for (String ip : singleIpArray) {
                sb.append("set address ").append(objectName).append(" ip-netmask ").append(ip).append("/128").append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        //默认有地址对象名
        String objectName = args[0];
        if (StringUtils.isBlank(objectName)) {
            return StringUtils.EMPTY;
        }
        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            StringBuilder sb = new StringBuilder();
            //set address ao2 ip-range 1.1.1.1-1.1.1.2
            for (int i = 0; i < rangIpArray.length; i++) {
                IpAddressRangeDTO ipAddressRangeDTO = rangIpArray[i];
                sb.append("set address ").append(objectName).append(" ip-range ").append(ipAddressRangeDTO.getStart())
                        .append('-').append(ipAddressRangeDTO.getEnd()).append(StringUtils.LF);
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        return generateSubnetIntIpV4CommandLine(statusTypeEnum, subnetIpArray, sub, map, args);
    }
}
