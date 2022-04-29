package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.PortFormatUtil;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.StringGeneralUtil;
import com.abtnetworks.totems.whale.baseapi.ro.IcmpCodeRangeRO;
import com.abtnetworks.totems.whale.baseapi.ro.IncludeFilterServicesRO;
import com.abtnetworks.totems.whale.baseapi.ro.IncludeItemsRO;
import com.abtnetworks.totems.whale.baseapi.ro.Ip4RangeRO;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/1/18 10:55'.
 */
public class ObjectConversionTool {

    /**
     * IncludeItemsRO 对象转换 ip 字符串
     * 用于策略迁移功能生成地址对象命令行
     * @param ro 对象参数
     * @return
     */
    public static String IncludeItemsROtoIpString (IncludeItemsRO ro) {
        //获取类型
        String type = ro.getType();

        String formatIpStr = "";
        if(type.equals(Constants.ANY4) || type.equals(Constants.ANY)) {
            formatIpStr += "any";
        }else if (Constants.SUBNET.equals(type)) {
            String ip4Prefix = ro.getIp4Prefix();
            String ip4Length = ro.getIp4Length();
            if(StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)){
                formatIpStr += (ip4Prefix + "/" + ip4Length) + ";";
            }

            String ip6Prefix = ro.getIp6Prefix();
            String ip6Length = ro.getIp6Length();
            if(StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)){
                formatIpStr += (ip6Prefix + "/" + ip6Length) + ";";
            }
        }else if ("INTERFACE".equals(type)) {

        }else if (Constants.HOST_IP.equals(type)) {
            List<String> ip4Addresses = ro.getIp4Addresses();
            if (ip4Addresses != null && ip4Addresses.size() > 0) {
                formatIpStr += StringUtils.join(ip4Addresses, ",") + ";";
            }

            List<String> ip6Addresses = ro.getIp6Addresses();
            if (ip6Addresses != null && ip6Addresses.size() > 0) {
                formatIpStr += StringUtils.join(ip6Addresses, ",") + ";";
            }
        }else if (Constants.RANGE.equals(type)) {
            Ip4RangeRO ip4Range = ro.getIp4Range();
            Ip4RangeRO ip6Range = ro.getIp6Range();
            if (ip4Range != null) {
                formatIpStr += getStartEndByJsonObject(ip4Range);
            }
            if (ip6Range != null) {
                formatIpStr += getStartEndByJsonObject(ip6Range);
            }
        }else if (type.equals(Constants.FQDN)) {
            formatIpStr += "域名: " + ro.getFqdn() + ";";
        }else if (Constants.IP4WILDCARD.equals(type)) {
            String ip4WildCardMask = ro.getIp4WildCardMask();
            if (!StringUtils.isNumeric(ip4WildCardMask)) {
                ip4WildCardMask = String.valueOf(getNetmaskLength(ip4WildCardMask));
            }
            String ip4Base = ro.getIp4Base();
            formatIpStr += (ip4Base + "/" + ip4WildCardMask);
        }else if (Constants.OBJECT_GROUP.equals(type)) {
            formatIpStr += ro.getNameRef() + ";";
        }else if (type.equals("interface") || type.equals("acl")) {

        }else if (Constants.OBJECT.equals(type)) {
            formatIpStr += ro.getNameRef() + ";";
        }

        formatIpStr = StringGeneralUtil.removeLastSemicolon(formatIpStr);

        return formatIpStr;
    }

    private static String getStartEndByJsonObject(Ip4RangeRO rangeRO) {
        String formatIpStr = "";
        String start = rangeRO.getStart();
        String end = rangeRO.getEnd();
        if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
            formatIpStr += (start + "-" + end) + ";";
        } else if (StringUtils.isNotBlank(start)) {
            formatIpStr += start + ";";
        } else if (StringUtils.isNotBlank(end)) {
            formatIpStr += end + ";";
        }
        return formatIpStr;
    }

    /**
     * IncludeFilterServicesRO 对象转换 ServiceDTO
     * 用于策略迁移功能生成服务对象命令行
     * @param ro 对象参数
     * @return
     */
    public static ServiceDTO IncludeFilterServicesROtoServiceDTO(IncludeFilterServicesRO ro) {
        String type = ro.getType();
        ServiceDTO serviceDTO = new ServiceDTO();
        if (Constants.HOST_IP.equals(type)) {
            String content = "";
            List<String> ip4Addresses = ro.getIp4Addresses();
            if (ip4Addresses != null && ip4Addresses.size() > 0) {
                for (int i = 0; i < ip4Addresses.size(); i++) {
                    content += ip4Addresses.get(i) + ";";
                }
            }

            List<String> ip6Addresses = ro.getIp6Addresses();
            if (ip6Addresses != null && ip6Addresses.size() > 0) {
                for (int i = 0; i < ip6Addresses.size(); i++) {
                    content += ip6Addresses.get(i) + ";";
                }
            }

            return serviceDTO;
        }

        if (Constants.ANY4.equals(type)) {
            String content = "0.0.0.0/0" + ";";
            return serviceDTO;
        }

        if (Constants.SUBNET.equals(type)) {
            String content = "";
            String ip4Prefix = ro.getIp4Prefix();
            String ip4Length = ro.getIp4Length();
            if(StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)){
                content += (ip4Prefix + "/" + ip4Length) + ";";
            }

            String ip6Prefix = ro.getIp6Prefix();
            String ip6Length = ro.getIp6Length();
            if(StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)){
                content += (ip6Prefix + "/" + ip6Length) + ";";
            }

            return serviceDTO;
        }

        if (Constants.RANGE.equals(type)) {
            String content = "";
            Ip4RangeRO ip4RangeRO = ro.getIp4Range();
            content += getRangeValue(ip4RangeRO);

            Ip4RangeRO ip6RangeRO = ro.getIp6Range();
            content += getRangeValue(ip6RangeRO);
            return serviceDTO;
        }

        if (Constants.FQDN.equals(type)) {
            String content = ro.getFqdn() + ";";
            return serviceDTO;
        }

        if (Constants.IP4WILDCARD.equals(type)) {
            String ip4Base = ro.getIp4Base();
            String ip4WildCardMask = ro.getIp4WildCardMask();
            String content = (ip4Base + "/" + ip4WildCardMask) + ";";
            return serviceDTO;
        }
        if (Constants.OBJECT_GROUP.equals(type)) {
            String content = ro.getNameRef() + ";";
            return serviceDTO;
        }

        if (Constants.SERVICE_NAME.equals(type)) {
            String content = "";
            List<String> serviceNames = ro.getServiceNames();
            for (int i = 0; i < serviceNames.size(); i++) {
                content += serviceNames.get(i) + ";";
            }
            return serviceDTO;
        }

        if (Constants.SERVICE_OTHER.equals(type)) {
            String protocolName = ro.getProtocolName();
            String protocolNum = ro.getProtocolNum();
            if (StringUtils.isNotBlank(protocolName) && StringUtils.isNotBlank(protocolNum)) {
                //content += protocolName + ":" + protocolNum + " ";
            } else if (StringUtils.isBlank(protocolName) && StringUtils.isNotBlank(protocolNum)) {
                serviceDTO.setProtocol(protocolNum);
            } else if (StringUtils.isNotBlank(protocolName) && StringUtils.isBlank(protocolNum)) {
                //content += protocolName + " ";
            }

            //目的端口   华为、Juniper返回源、目的端口，srcPortOp=Range
            String dstPortOp = ro.getDstPortOp();
            List<String> dstPortValues = ro.getDstPortValues();
            String dstPorts = PortFormatUtil.getPortValueStr(dstPortOp, dstPortValues);

            //源端口
            String srcPortOp = ro.getSrcPortOp();
            List<String> srcPortValues = ro.getSrcPortValues();
            String srcPort = PortFormatUtil.getPortValueStr(srcPortOp, srcPortValues);

            if (StringUtils.isNotBlank(srcPortOp)) {
                serviceDTO.setSrcPorts(srcPort);
            }
            if (StringUtils.isNotBlank(dstPortOp)) {
                serviceDTO.setDstPorts(dstPorts);
            }
            return serviceDTO;
        }

        if (Constants.SERVICE_ICMP.equals(type)) {
            String icmpCode = ro.getIcmpCode();
            String protocolName = ro.getProtocolName();
            Integer icmpTypeNum = ro.getIcmpTypeNum();
            String icmpType = ro.getIcmpType();
            String icmpStr = ro.getIcmpStr();
            IcmpCodeRangeRO icmpCodeRangeRO = ro.getIcmpCodeRange();
            if (StringUtils.isNotBlank(protocolName)) {
                //content += protocolName;
            }
            if (icmpTypeNum != null) {
                //content += ":类型:" + icmpTypeNum;
            }
            if (StringUtils.isNotBlank(icmpType)) {
                //content += ":类型:" + icmpType;
            }
            if (StringUtils.isNotBlank(icmpCode)) {
                //content += " 编码:" + icmpCode;
            }
            if(StringUtils.isNotBlank(icmpStr)) {
                //content += ":" + icmpStr;
            }

            if (icmpCodeRangeRO != null && icmpCodeRangeRO.getStart() != null && icmpCodeRangeRO.getEnd() != null) {
                //content += " 协议范围:" + icmpCodeRangeRO.getStart() + "-" + icmpCodeRangeRO.getEnd();
            }

            return serviceDTO;
        }

        if (Constants.SERVICE_TCP_UDP.equals(type)) {
            String protocolName = ro.getProtocolName();
            serviceDTO.setProtocol(ProtocolUtils.getProtocolNumberByName(protocolName));

            //端口   中新网安响应的数据是端口，portOp=EQ
            String portOp = ro.getPortOp();
            List<String> portValues = ro.getPortValues();
            String ports = PortFormatUtil.getPortValueStr(portOp,portValues);

            //目的端口   华为返回源、目的端口，srcPortOp=Range
            String dstPortOp = ro.getDstPortOp();
            List<String> dstPortValues = ro.getDstPortValues();
            String dstPorts = PortFormatUtil.getPortValueStr(dstPortOp, dstPortValues);

            //源端口
            String srcPortOp = ro.getSrcPortOp();
            List<String> srcPortValues = ro.getSrcPortValues();
            String srcPort = PortFormatUtil.getPortValueStr(srcPortOp, srcPortValues);

            //源端口，可能没有

            if (StringUtils.isNotBlank(srcPortOp)) {
                serviceDTO.setSrcPorts(srcPort);
            }

            if (StringUtils.isNotBlank(dstPortOp)) {
                serviceDTO.setDstPorts(dstPorts);
            }

            if(StringUtils.isNotBlank(portOp) && StringUtils.isNotBlank(ports)) {
                //content += "端口:" + portOp + ":" + ports + " ";
                if(StringUtils.isNotBlank(serviceDTO.getDstPorts())){
                    serviceDTO.setDstPorts(serviceDTO.getDstPorts() + "," + ports);
                } else {
                    serviceDTO.setDstPorts(ports);
                }
            }

            return serviceDTO;
        }

        if (Constants.PORT_OBJECT.equals(type)) {
            String portOp = ro.getPortOp();
            String protocolName = ro.getProtocolName();

            List<String> portValues = ro.getPortValues();
            String ports = "";
            if (portValues != null && portValues.size() > 0) {
                for (int i = 0; i < portValues.size(); i++) {
                    ports += portValues.get(i) + ";";
                }
                ports = StringGeneralUtil.removeLastSemicolon(ports);
            }

            if (StringUtils.isNotBlank(protocolName)) {
                serviceDTO.setProtocol(ProtocolUtils.getProtocolNumberByName(protocolName));
            }
            if (StringUtils.isNotBlank(portOp)) {
                //content += portOp + ":";
            }
            if (StringUtils.isNotBlank(ports)) {
                //content += ports + ";";
            }
            
            return serviceDTO;
        }

        return null;
    }

    public static String getRangeValue(Ip4RangeRO ipRangeRO) {
        String content = "";
        if (ipRangeRO != null) {
            String start = ipRangeRO.getStart();
            String end = ipRangeRO.getEnd();
            if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
                content = (start + "-" + end) + ";";
            } else if (StringUtils.isNotBlank(start)) {
                content = start + ";";
            } else if (StringUtils.isNotBlank(end)) {
                content = end + ";";
            }
        }
        return content;
    }

    public static int getNetmaskLength(String ip)
    {
        long myInt = IpUtils.IPv4StringToNum(ip);
        int myResult = Long.bitCount(myInt);
        if (myResult == 0) {
            return 0;
        }
        if (Long.numberOfLeadingZeros(myInt) != 32) {
            throw new IllegalArgumentException("This is not a valid netmask:" + ip);
        }
        if (Long.numberOfTrailingZeros(myInt) + myResult != 32) {
            throw new IllegalArgumentException("This is not a valid netmask:" + ip);
        }
        return myResult;
    }

}
