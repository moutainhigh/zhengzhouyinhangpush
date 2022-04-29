package com.abtnetworks.totems.vender.longma.nat;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.vender.longma.security.SecurityLongMaImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NatLongMaImpl extends SecurityLongMaImpl {

    private static String groupMember = "member  %s";
    private static final String IS_POOL = "isPool";
    private static final String POOL_ADDRESS_TEMPLATE = "ip address %s %s";
    private static final String EXIT = "exit";
    private static final String SRC_IP_SYSTEM = "srcIpSystem";
    private static final String DST_IP_SYSTEM = "dstIpSystem";
    private static final String POST_IP_SYSTEM = "postIpSystem";

    /**
     * @return ip nat destination ge0 2.2.2.2 7.7.7.7 tcp60-80 5.4.6.1 1 description 地址映射方式
     * ip nat destination ge0 1.5.4.1-1.5.4.10 pool1 tcp60-80 192.168.215.2 service 50 2 description 端口映射方式
     */
    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId,
                                                IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postDstIpAddress,
                                                ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface,
                                                InterfaceParamDTO outInterface, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject,
                                                String[] refServiceObjectGroup, String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String srcAddressGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, new String[]{SRC_IP_SYSTEM}, sb);
        String dstAddressGroupName = disposeAddress(statusTypeEnum, postDstIpAddress, postDstRefIpAddressObject, postDstRefIpAddressObjectGroup, map, new String[]{DST_IP_SYSTEM}, sb);
        String addressPoolName = disposePool(statusTypeEnum, dstIp, dstRefIpAddressObject,dstRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}, sb);
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam, refServiceObject, refServiceObjectGroup, sb);
        sb.append("ip nat destination ").append(StringUtils.isBlank(inInterface.getName()) ? "any" : inInterface.getName()).append(StringUtils.SPACE)
                .append(srcAddressGroupName).append(StringUtils.SPACE)
                .append(addressPoolName).append(StringUtils.SPACE)
                .append(serviceGroupName).append(StringUtils.SPACE)
                .append(dstAddressGroupName).append(StringUtils.SPACE);
        if (map.get("postPort")!=null && StringUtils.isNotEmpty((CharSequence) map.get("postPort")) && !"any".equalsIgnoreCase((String) map.get("postPort")))
            sb.append("service ").append(map.get("postPort"));
        if (StringUtils.isNotEmpty(id))
            sb.append(StringUtils.SPACE).append(id);
        sb.append(StringUtils.LF);
        if(StringUtils.isNotEmpty(description)){
            sb.append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);
        return sb.toString();
    }


    /**
     * @return ip nat static ge0 6.66.6.6 5.4.6.101 1 description 静态nat模式
     */
    @Override
    public String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description, String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam, IpAddressParamDTO postSrcIpAddress, IpAddressParamDTO postDstIpAddress, ServiceParamDTO[] postServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup, String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject, String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteNatPolicyByIdOrName(NatTypeEnum natTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        return null;
    }


    /**
     * @return ip nat static ge0 6.66.6.6 5.4.6.101 1 description 静态nat模式
     */
    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                     String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId,
                                                     IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress, ServiceParamDTO[] insideServiceParam,
                                                     ServiceParamDTO[] globalServiceParam, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface,
                                                     InterfaceParamDTO outInterface, String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup,
                                                     String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String inIntName = "any";
        if (inInterface != null && StringUtils.isNotEmpty(inInterface.getName())) {
            inIntName = inInterface.getName();
        }
        String globalIp = "any";
        String insideIp = "any";
        if (globalAddress != null && globalAddress.getSingleIpArray() != null) {
            globalIp = globalAddress.getSingleIpArray()[0];
        }
        if (insideAddress != null && insideAddress.getSingleIpArray() != null) {
            insideIp = insideAddress.getSingleIpArray()[0];
        }
        sb.append("ip nat static ").append(inIntName).append(StringUtils.SPACE).append(globalIp).append(StringUtils.SPACE).append(insideIp);
        if (StringUtils.isNotEmpty(id))
            sb.append(StringUtils.SPACE).append(id);
        sb.append(StringUtils.LF);
        if(StringUtils.isNotEmpty(description)){
            sb.append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);
        return sb.toString();
    }

    /**
     * @return ip nat source ge0 1.1.1.1 2.2.2.2 tcp50 interface 2 description\n 出接口源Nat转换  ip nat source ge0 192.168.215.2 5.4.6.1 any pool1 3 description\n 源nat转换地址池方式
     */
    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum, String swapRuleNameId,
                                                IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] serviceParam,
                                                IpAddressParamDTO postSrcIpAddress, ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface,
                                                InterfaceParamDTO outInterface, String eVr, String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup, String[] refServiceObject,
                                                String[] refServiceObjectGroup, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String outIntName = "any";
        if (outInterface != null && StringUtils.isNotEmpty(outInterface.getName())) {
            outIntName = outInterface.getName();
        }
        String srcAddressGroupName = disposeAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup, map, new String[]{SRC_IP_SYSTEM}, sb);
        String dstAddressGroupName = disposeAddress(statusTypeEnum, dstIp, dstRefIpAddressObject, dstRefIpAddressObjectGroup, map, new String[]{DST_IP_SYSTEM}, sb);
        String addressPoolName = disposePool(statusTypeEnum, postSrcIpAddress, postSrcRefIpAddressObject, postSrcRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}, sb);
        String serviceGroupName = disposeService(statusTypeEnum, serviceParam, refServiceObject, refServiceObjectGroup, sb);
        sb.append("ip nat source ").append(outIntName).append(StringUtils.SPACE)
                .append(srcAddressGroupName).append(StringUtils.SPACE)
                .append(dstAddressGroupName).append(StringUtils.SPACE)
                .append(serviceGroupName).append(StringUtils.SPACE);
        if ("any".equals(addressPoolName) || StringUtils.isEmpty(addressPoolName)) {
            sb.append("interface");
        } else {
            sb.append(addressPoolName);
        }
        if (StringUtils.isNotEmpty(id))
            sb.append(StringUtils.SPACE).append(id);
        sb.append(StringUtils.LF);
        if(StringUtils.isNotEmpty(description)){
            sb.append("description ").append(description).append(StringUtils.LF);
        }
        sb.append(EXIT).append(StringUtils.LF);

        return sb.toString();
    }

    private String disposeService(StatusTypeEnum statusTypeEnum, ServiceParamDTO[] serviceParam, String[] refServiceObject, String[] refServiceObjectGroup, StringBuilder sb) throws Exception {
        if (!ArrayUtils.isEmpty(refServiceObjectGroup) && refServiceObjectGroup.length > 0) {
            return StringUtils.isBlank(refServiceObjectGroup[0]) ? "any" : refServiceObjectGroup[0];
        }
        String serviceGroupName = null;
        //判断是否为预定义服务
        String serviceName = null;
        String serviceCommandline = null;
        //服务对象
        if (ArrayUtils.isNotEmpty(serviceParam)) {
            serviceName = this.createServiceObjectName(Arrays.asList(serviceParam), null, null);
            serviceCommandline = this.generateServiceObjectCommandLine(statusTypeEnum, serviceName, null, null, Arrays.asList(serviceParam), null, null, null);
        }
        //服务对象组装为组
        StringBuilder serviceGroupCommandline = new StringBuilder("");
        if (ArrayUtils.isNotEmpty(refServiceObject) || ObjectUtils.isNotEmpty(serviceName)) {
            if (ArrayUtils.isNotEmpty(refServiceObject) && refServiceObject.length == 1 && StringUtils.isEmpty(serviceName)) {
                serviceGroupName = refServiceObject[0];
            } else if (ArrayUtils.isEmpty(refServiceObject) && StringUtils.isNotEmpty(serviceName)) {
                serviceGroupName = serviceName;
            } else {
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
        if (StringUtils.isNotEmpty(serviceCommandline)) {
            sb.append(serviceCommandline).append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(serviceGroupCommandline)) {
            sb.append(serviceGroupCommandline).append(StringUtils.LF);
        }
        return serviceGroupName == null ? "any" : serviceGroupName;
    }


    private String disposeAddress(StatusTypeEnum statusTypeEnum, IpAddressParamDTO srcIp, String[] srcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder sb) throws Exception {
        if (!ArrayUtils.isEmpty(postSrcRefIpAddressObjectGroup) && postSrcRefIpAddressObjectGroup.length > 0 && StringUtils.isNotEmpty(postSrcRefIpAddressObjectGroup[0])) {
            return postSrcRefIpAddressObjectGroup[0];
        }
        String srcAddressGroupName = null;
        String srcAddressName = null;
        String srcCommandline = null;
        String ipSystem = null;
        if (map != null && args != null && map.get(args[0]) != null && StringUtils.isNotEmpty((CharSequence) map.get(args[0]))) {
            ipSystem = (String) map.get(args[0]);
        }
        if (ObjectUtils.isNotEmpty(srcIp)) {
            if (ArrayUtils.isEmpty(srcRefIpAddressObject) && StringUtils.isNotEmpty(ipSystem)) {
                srcAddressName = ipSystem;
            } else {
                srcAddressName = this.createIpAddressObjectNameByParamDTO(srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null, null, null, null);
            }
            srcCommandline = this.generateIpAddressObjectCommandLine(statusTypeEnum, srcIp.getIpTypeEnum(), srcAddressName, null, srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null,
                    null, null, null, null, null, map, args);
        }
        //地址对象 组装为地址组对象
        StringBuilder srcAddressGroupCommandline = new StringBuilder("");
        if (ArrayUtils.isNotEmpty(srcRefIpAddressObject) || StringUtils.isNotEmpty(srcAddressName)) {
            //只有一个地址对象时，直接引用不再创建地址组
            if (ArrayUtils.isNotEmpty(srcRefIpAddressObject) && srcRefIpAddressObject.length == 1 && StringUtils.isEmpty(srcAddressName)) {
                srcAddressGroupName = srcRefIpAddressObject[0].replace(":", "_");
            } else if (ArrayUtils.isEmpty(srcRefIpAddressObject) && StringUtils.isNotEmpty(srcAddressName)) {
                srcAddressGroupName = srcAddressName.replace(":", "_");
            } else {
                if (ipSystem != null) {
                    srcAddressGroupName = ipSystem;
                } else if (srcIp != null) {
                    srcAddressGroupName = this.createIpAddressObjectGroupName(srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null, srcIp.getHosts(), srcRefIpAddressObject, srcIp.getObjectGroupNameRefArray(), null, null);
                } else {
                    srcAddressGroupName = this.createIpAddressObjectGroupName(null, null, null, null, null, null, srcRefIpAddressObject, null, null, null);
                }
                srcAddressGroupCommandline.append(String.format("address-group %s", srcAddressGroupName)).append(StringUtils.LF);
                if (ArrayUtils.isNotEmpty(srcRefIpAddressObject)) {
                    for (String srcAddress : srcRefIpAddressObject) {
                        srcAddressGroupCommandline.append(String.format(groupMember, srcAddress.replace(":", "_"))).append(StringUtils.LF);
                    }
                }
                if (StringUtils.isNotEmpty(srcCommandline)) {
                    srcAddressName = srcAddressName.replace(":", "_");
                    srcAddressGroupCommandline.append(String.format(groupMember, srcAddressName)).append(StringUtils.LF);
                }
                srcAddressGroupCommandline.append("quit").append(StringUtils.LF);
            }
        }
        if (StringUtils.isNotEmpty(srcCommandline)) {
            sb.append(srcCommandline).append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(srcAddressGroupCommandline)) {
            sb.append(srcAddressGroupCommandline).append(StringUtils.LF);
        }
        return StringUtils.isBlank(srcAddressGroupName) ? "any" : srcAddressGroupName;
    }


    /**
     * @return ip nat pool pool1
     * ip address 1.1.1.2 1.1.1.10
     * ip address 3.3.3.3 3.3.3.3
     * quit
     */
    private String disposePool(StatusTypeEnum statusTypeEnum, IpAddressParamDTO dstIp,String[] postSrcRefIpAddressObject, String[] dstRefIpAddressObjectGroup, Map<String, Object> map, String[] args, StringBuilder sb) {
        if (!ArrayUtils.isEmpty(dstRefIpAddressObjectGroup) && dstRefIpAddressObjectGroup.length>0 && StringUtils.isNotEmpty(dstRefIpAddressObjectGroup[0])) {
            return dstRefIpAddressObjectGroup[0];
        }
        if (!ArrayUtils.isEmpty(postSrcRefIpAddressObject) && postSrcRefIpAddressObject.length>0 && StringUtils.isNotEmpty(postSrcRefIpAddressObject[0])) {
            return postSrcRefIpAddressObject[0];
        }
        String ipSystem = null;
        if (map != null && args != null && map.get(args[0]) != null && StringUtils.isNotEmpty((CharSequence) map.get(args[0]))) {
            ipSystem = (String) map.get(args[0]);
        }
        String poolName = "";
        if (dstIp != null) {
            if (StringUtils.isNotEmpty(ipSystem)) {
                poolName = ipSystem;
            } else {
                poolName = this.createIpAddressObjectNameByParamDTO(dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray(), null, null, null, null);
            }
            sb.append(generateIpAddressPoolCommandLine(statusTypeEnum, poolName, dstIp, null, dstRefIpAddressObjectGroup, map, new String[]{POST_IP_SYSTEM}));
        }
        return StringUtils.isBlank(poolName) ? "any" : poolName;
    }

    /**
     * @return ip nat pool poolname
     * * ip address 1.1.1.2 1.1.1.10
     * * ip address 3.3.3.3 3.3.3.3
     * * quit
     */
    @Override
    public String generateIpAddressPoolCommandLine(StatusTypeEnum statusTypeEnum, String poolName, IpAddressParamDTO poolIp, String[] poolRefIpAddressObject, String[] poolRefIpAddressObjectGroup, Map<String, Object> map, String[] args) {
        StringBuilder poolCommand = new StringBuilder();
        StringBuilder elementCommand = new StringBuilder();
        if (!ArrayUtils.isEmpty(poolIp.getSingleIpArray())) {
            for (String singleIp : poolIp.getSingleIpArray()) {
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, singleIp, singleIp)).append(StringUtils.LF);
            }
        }
        if (!ArrayUtils.isEmpty(poolIp.getRangIpArray())) {
            for (IpAddressRangeDTO rangeDTO : poolIp.getRangIpArray()) {
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, rangeDTO.getStart(), rangeDTO.getEnd())).append(StringUtils.LF);
            }
        }
        if (!ArrayUtils.isEmpty(poolIp.getSubnetStrIpArray())) {
            for (IpAddressSubnetStrDTO subnetIp : poolIp.getSubnetStrIpArray()) {
                String Ip = subnetIp.getIp() + "/" + IPUtil.getMaskBitMapByInverseMask(subnetIp.getMask());
                String startIp = IPUtil.getStartIp(Ip);
                String endIp = IPUtil.getEndIp(Ip);
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, startIp, endIp)).append(StringUtils.LF);
            }
        }
        if (!ArrayUtils.isEmpty(poolIp.getSubnetIntIpArray())) {
            for (IpAddressSubnetIntDTO subnetIp : poolIp.getSubnetIntIpArray()) {
                String Ip = subnetIp.getIp() + "/" + subnetIp.getMask();
                String startIp = IPUtil.getStartIp(Ip);
                String endIp = IPUtil.getEndIp(Ip);
                elementCommand.append(String.format(POOL_ADDRESS_TEMPLATE, startIp, endIp)).append(StringUtils.LF);
            }
        }
        if (!StringUtils.isBlank(elementCommand)) {
            poolCommand.append("ip nat pool ").append(poolName).append(StringUtils.LF).append(elementCommand).append("quit").append(StringUtils.LF).append(StringUtils.LF);
        }
        return poolCommand.toString();
    }
}
