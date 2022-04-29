package com.abtnetworks.totems.vender.Legendsec.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class SecurityLegendsecImpl extends OverAllGeneratorAbstractBean {

    private static final String RANGE_TEMPLATE = "range %s-%s";
    private static final String SINGLE_IP_TEMPLATE = "host %s";
    private static final String SUBNET_TEMPLATE = "network %s %s";
    private static final String IPV6_SUBNET_TEMPLATE = "network %s/%s";
    private static final String SRC_PORT_TEMPLATE = "src-port %s %s ";
    private static final String DST_PORT_TEMPLATE = "dst-port %s %s ";

    @Override
    public String generateIpAddressObjectCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum, String name, String id, String[] singleIpArray,
                                                     IpAddressRangeDTO[] rangIpArray, IpAddressSubnetIntDTO[] subnetIntIpArray,
                                                     IpAddressSubnetStrDTO[] subnetStrIpArray, String[] interfaceArray, String[] fqdnArray,
                                                     String[] objectNameRefArray, String description, String attachStr, String delStr,
                                                     Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(rangIpArray) && ArrayUtils.isEmpty(singleIpArray)
                && ArrayUtils.isEmpty(subnetIntIpArray) && ArrayUtils.isEmpty(subnetStrIpArray) && ArrayUtils.isEmpty(fqdnArray)){
            return StringUtils.EMPTY;
        }
        StringBuilder commandLine=new StringBuilder("");
        commandLine.append("object address ").append(name).append(StringUtils.LF);
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
                commandLine.append(this.generateSubnetStrIpV4CommandLine(statusTypeEnum,new IpAddressSubnetStrDTO[]{ipAddressSubnetStrDTO},null,map,null));
            }
        }
        if(ArrayUtils.isNotEmpty(fqdnArray)){
            // 不支持域名
        }
        if(StringUtils.isNotEmpty(commandLine)){
            commandLine.append("exit").append(StringUtils.LF);
        }
        return commandLine.toString();
    }

    @Override
    public String generateSingleIpV4CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(singleIpArray)){
            for (String singleIp : singleIpArray) {
                sb.append(String.format(SINGLE_IP_TEMPLATE,singleIp)).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateHostCommandLine(StatusTypeEnum statusTypeEnum, String[] hosts, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * @return range 192.168.1.1-192.168.1.10
     */
    @Override
    public String generateRangeIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(rangIpArray)){
            for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                sb.append(String.format(RANGE_TEMPLATE,rangeDTO.getStart(),rangeDTO.getEnd())).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateSubnetIntIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(subnetIpArray)){
            for (IpAddressSubnetIntDTO subnet : subnetIpArray) {
                sb.append(String.format(SUBNET_TEMPLATE,subnet.getIp(),subnet.getMask())).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateSubnetStrIpV4CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetStrDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(subnetIpArray)){
            for (IpAddressSubnetStrDTO subnet : subnetIpArray) {
                sb.append(String.format(SUBNET_TEMPLATE,subnet.getIp(), TotemsIpUtils.getMaskBit(subnet.getMask()))).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(singleIpArray)){
            for (String singleIp : singleIpArray) {
                sb.append(String.format(SINGLE_IP_TEMPLATE,singleIp)).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(rangIpArray)){
            for (IpAddressRangeDTO rangeDTO : rangIpArray) {
                sb.append(String.format(RANGE_TEMPLATE,rangeDTO.getStart(),rangeDTO.getEnd())).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(!ArrayUtils.isEmpty(subnetIpArray)){
            for (IpAddressSubnetIntDTO subnet : subnetIpArray) {
                sb.append(String.format(IPV6_SUBNET_TEMPLATE,subnet.getIp(),subnet.getMask())).append(StringUtils.LF);
            }
        }
        return sb.toString();
    }

    @Override
    public String generateICMPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateICMP6CommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    /**
     * @return service-item tcp src-port 1 65535 dst-port 44 44
     */
    @Override
    public String generateTCPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray,
                                         Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray,
                                         Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray,
                                         String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(srcSinglePortArray) && ArrayUtils.isEmpty(srcSinglePortStrArray) && ArrayUtils.isEmpty(srcRangePortArray) && ArrayUtils.isEmpty(dstSinglePortArray) && ArrayUtils.isEmpty(dstSinglePortStrArray) && ArrayUtils.isEmpty(dstRangePortArray)){
            return null;
        }
        StringBuilder allCmd = new StringBuilder();
        allCmd.append("service-item tcp ");
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            allCmd.append(String.format(SRC_PORT_TEMPLATE,srcSinglePortArray[0],srcSinglePortArray[0]));
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            allCmd.append(String.format(SRC_PORT_TEMPLATE,srcSinglePortStrArray[0],srcSinglePortStrArray[0]));
        } else if(ArrayUtils.isNotEmpty(srcRangePortArray)){
            allCmd.append(String.format(SRC_PORT_TEMPLATE,srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }else {
            allCmd.append("src-port 0 65535 ");
        }
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            allCmd.append(String.format(DST_PORT_TEMPLATE,dstSinglePortArray[0],dstSinglePortArray[0]));
        }else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            allCmd.append(String.format(DST_PORT_TEMPLATE,dstSinglePortStrArray[0],dstSinglePortStrArray[0]));
        }else if(ArrayUtils.isNotEmpty(dstRangePortArray)){
            allCmd.append(String.format(DST_PORT_TEMPLATE,dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        }
        return allCmd.toString();
    }

    @Override
    public String generateUDPCommandLine(StatusTypeEnum statusTypeEnum, String[] protocolAttachTypeArray, String[] protocolAttachCodeArray, Integer[] srcSinglePortArray, String[] srcSinglePortStrArray, PortRangeDTO[] srcRangePortArray, Integer[] dstSinglePortArray, String[] dstSinglePortStrArray, PortRangeDTO[] dstRangePortArray, String[] timeOutArray, String[] objectNameRefArray, Map<String, Object> map, String[] args) throws Exception {
        if(ArrayUtils.isEmpty(srcSinglePortArray) && ArrayUtils.isEmpty(srcSinglePortStrArray) && ArrayUtils.isEmpty(srcRangePortArray) && ArrayUtils.isEmpty(dstSinglePortArray) && ArrayUtils.isEmpty(dstSinglePortStrArray) && ArrayUtils.isEmpty(dstRangePortArray)){
            return null;
        }
        StringBuilder allCmd = new StringBuilder();
        allCmd.append("service-item udp ");
        if(ArrayUtils.isNotEmpty(srcSinglePortArray)){
            allCmd.append(String.format(SRC_PORT_TEMPLATE,srcSinglePortArray[0],srcSinglePortArray[0]));
        } else if(ArrayUtils.isNotEmpty(srcSinglePortStrArray)){
            allCmd.append(String.format(SRC_PORT_TEMPLATE,srcSinglePortStrArray[0],srcSinglePortStrArray[0]));
        } else if(ArrayUtils.isNotEmpty(srcRangePortArray)){
            allCmd.append(String.format(SRC_PORT_TEMPLATE,srcRangePortArray[0].getStart(),srcRangePortArray[0].getEnd()));
        }else {
            allCmd.append("src-port 0 65535 ");
        }
        if(ArrayUtils.isNotEmpty(dstSinglePortArray)){
            allCmd.append(String.format(DST_PORT_TEMPLATE,dstSinglePortArray[0],dstSinglePortArray[0]));
        }else if(ArrayUtils.isNotEmpty(dstSinglePortStrArray)){
            allCmd.append(String.format(DST_PORT_TEMPLATE,dstSinglePortStrArray[0],dstSinglePortStrArray[0]));
        }else if(ArrayUtils.isNotEmpty(dstRangePortArray)){
            allCmd.append(String.format(DST_PORT_TEMPLATE,dstRangePortArray[0].getStart(),dstRangePortArray[0].getEnd()));
        }
        return allCmd.toString();
    }
}
