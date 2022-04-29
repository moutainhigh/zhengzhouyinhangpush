package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service(value = "HillstoneStoneOS NAT")
public class Hillstone implements NatPolicyGenerator {

    private static String SEPERATOR = ",";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate hillstone nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }
    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null);

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postAddressObject.getCommandLine())) {
            sb.append(postAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("nat\n");
        String postAddressObjectName = postAddressObject.getName();
        if(!IpUtils.isIP(postAddressObjectName) &&  !IpUtils.isIPSegment(postAddressObjectName)){
            postAddressObjectName = "address-book " + postAddressObjectName;
        }
        sb.append(String.format("snatrule from %s to %s%s%s trans-to %s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()),  postAddressObjectName));

        sb.append("exit\nend\n");

        List<String> createAddressObjectNames = getCreateAddressObjectNames(srcAddressObject, dstAddressObject, postAddressObject);

        if(CollectionUtils.isNotEmpty(createAddressObjectNames)){
            policyDTO.setAddressObjectNameList(createAddressObjectNames);
        }
        List<String> createServiceObjectName = serviceObject.getCreateServiceObjectName();
        List<String> createServiceGroupObjectName = serviceObject.getCreateServiceGroupObjectNames();
        if(CollectionUtils.isNotEmpty(createServiceObjectName)){
            policyDTO.setServiceObjectNameList(createServiceObjectName);
        }
        if(CollectionUtils.isNotEmpty(createServiceGroupObjectName)){
            policyDTO.setServiceObjectGroupNameList(createServiceGroupObjectName);
        }
        return sb.toString();
    }

    /**
     * 获取新建的地址对象
     * @param srcAddressObject
     * @param dstAddressObject
     * @param postAddressObject
     * @return
     */
    public List<String> getCreateAddressObjectNames(PolicyObjectDTO srcAddressObject,PolicyObjectDTO dstAddressObject,PolicyObjectDTO postAddressObject){
        List<String> createAddressObjectNames = new ArrayList<>();
        List<String> createSrcServiceObjectName = null, createDstServiceObjectName = null, createServiceObjectName1 = null;
        if (null != srcAddressObject) {
            createSrcServiceObjectName = srcAddressObject.getCreateObjectName();
        }
        if (null != dstAddressObject) {
            createDstServiceObjectName = dstAddressObject.getCreateObjectName();
        }
        if (null != postAddressObject) {
            createServiceObjectName1 = postAddressObject.getCreateObjectName();
        }
        if (CollectionUtils.isNotEmpty(createDstServiceObjectName)) {
            createAddressObjectNames.addAll(createDstServiceObjectName);
        }
        if (CollectionUtils.isNotEmpty(createSrcServiceObjectName)) {
            createAddressObjectNames.addAll(createSrcServiceObjectName);
        }
        if (CollectionUtils.isNotEmpty(createServiceObjectName1)) {
            createAddressObjectNames.addAll(createServiceObjectName1);
        }
        return createAddressObjectNames;
    }
    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",policyDTO.getSrcIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",policyDTO.getDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(),true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "",policyDTO.getPostDstIpSystem(),policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null);

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine()) ) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(postAddressObject.getCommandLine())) {
            sb.append(postAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
        if("any".equalsIgnoreCase(policyDTO.getPostPort())){
            postPortString = "";
        }
        sb.append(String.format("dnatrule %sfrom %s to %s%s trans-to %s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postAddressObject.getName(), postPortString));

        sb.append("exit\nend\n");
        List<String> createAddressObjectNames = getCreateAddressObjectNames(srcAddressObject, dstAddressObject, postAddressObject);

        if(CollectionUtils.isNotEmpty(createAddressObjectNames)){
            policyDTO.setAddressObjectNameList(createAddressObjectNames);
        }
        List<String> createServiceObjectName = serviceObject.getCreateServiceObjectName();
        if(CollectionUtils.isNotEmpty(createServiceObjectName)){
            policyDTO.setServiceObjectNameList(createServiceObjectName);
        }
        List<String> createServiceGroupObjectName = serviceObject.getCreateServiceGroupObjectNames();
        if(CollectionUtils.isNotEmpty(createServiceGroupObjectName)){
            policyDTO.setServiceObjectGroupNameList(createServiceGroupObjectName);
        }
        return sb.toString();
    }


    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        //山石BOTH NAT命令行为生成一个SNAT再生成一个DNAT
        StringBuilder sb = new StringBuilder();
        if (policyDTO.isVsys()) {
            sb.append("enter-vsys " + policyDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(),true, policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src",null,policyDTO.getIpType());
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(),true, policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst",null,policyDTO.getIpType());
        PolicyObjectDTO postSrcAddressObject = getAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), true, policyDTO.getPostSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "",null,policyDTO.getIpType());
        PolicyObjectDTO postDstAddressObject = getAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), true,policyDTO.getPostDstAddressObjectName(), policyDTO.isCreateObjFlag(), "",null,policyDTO.getIpType());
        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), null);

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postSrcAddressObject.getCommandLine())) {
            sb.append(postSrcAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postDstAddressObject.getCommandLine())) {
            sb.append(postDstAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        List<String> createAddressObjectNames = getCreateAddressObjectNames(srcAddressObject, dstAddressObject, postSrcAddressObject);

        if(CollectionUtils.isNotEmpty(createAddressObjectNames)){
            policyDTO.setAddressObjectNameList(createAddressObjectNames);
        }
        if(null != postDstAddressObject){
            List<String> createObjectName = postDstAddressObject.getCreateObjectName();
            if(CollectionUtils.isNotEmpty(createObjectName)){
                policyDTO.getAddressObjectNameList().addAll(createObjectName);
            }
        }
        List<String> createServiceObjectName = serviceObject.getCreateServiceObjectName();
        if(CollectionUtils.isNotEmpty(createServiceObjectName)){
            policyDTO.setServiceObjectNameList(createServiceObjectName);
        }
        List<String> createServiceGroupObjectName = serviceObject.getCreateServiceGroupObjectNames();
        if(CollectionUtils.isNotEmpty(createServiceGroupObjectName)){
            policyDTO.setServiceObjectGroupNameList(createServiceGroupObjectName);
        }

        sb.append("nat\n");
        String postAddressObjectName = postSrcAddressObject.getName();
        if(!IpUtils.isIP(postAddressObjectName) &&  !IpUtils.isIPSegment(postAddressObjectName)){
            postAddressObjectName = "address-book " + postAddressObjectName;
        }
        sb.append(String.format("snatrule from %s to %s%s%s trans-to %s mode dynamicport \n",srcAddressObject.getName(), dstAddressObject.getName(), serviceObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()), postAddressObjectName));

        sb.append("exit\n");

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
        if(CommonConstants.ANY.equalsIgnoreCase(policyDTO.getPostPort())){
            postPortString = "";
        }
        sb.append(String.format("dnatrule %sfrom %s to %s%s trans-to %s%s\n", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                serviceObject.getName(), postDstAddressObject.getName(), postPortString));

        sb.append("exit\n");

        sb.append("end\n");
        return sb.toString();
    }

//    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, String addressObjectName, boolean isCreateObject, String prefix) {
//        return getAddressObject(ipAddressString, theme, false, addressObjectName, isCreateObject, prefix,null);
//    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, boolean isSNatPostAddress, String addressObjectName, boolean isCreateObject, String prefix,String ipSystem,Integer ipType) {
        // 设置iptype的默认值
        if(null == ipType){
            ipType = 0;
        }
        List<String> createAddressObjectNames = new ArrayList<>();

        PolicyObjectDTO policyObject = new PolicyObjectDTO();
        policyObject.setName("");
        policyObject.setCommandLine("");
        //如果地址为空，则为any，不生成地址对象
        if(AliStringUtils.isEmpty(ipAddressString)) {
            policyObject.setName("any");
            return policyObject;
        }

        String[] ipAddresses = ipAddressString.split(",");
        //创建对象或者，ip地址多于一个都创建对象，否则直接引用内容
        if(isCreateObject == true || ipAddresses.length > 1) {

            SecurityHillStoneR5 r5 = new SecurityHillStoneR5();
            PolicyObjectDTO dto = null;

            if (isSNatPostAddress) {
                dto = r5.generateAddressObjectForNat(ipAddressString, theme, prefix, true, addressObjectName,ipSystem,ipType);
            } else {
                dto = r5.generateAddressObject(ipAddressString, theme, prefix, true, addressObjectName,ipSystem,ipType,null,0);
            }
            policyObject.setName(" " + dto.getName());
            policyObject.setCommandLine(dto.getCommandLine());
            if(CollectionUtils.isNotEmpty(dto.getCreateObjectName())){
                createAddressObjectNames.addAll(dto.getCreateObjectName());
            }

        } else {
            //若为单个ip范围，则建对象
            if(IpUtils.isIPRange(ipAddressString)) {
                SecurityHillStoneR5 r5 = new SecurityHillStoneR5();
                PolicyObjectDTO dto = r5.generateAddressObject(ipAddressString, theme, "", true, addressObjectName,ipSystem,ipType,null,0);

                policyObject.setName(" " + dto.getName());
                policyObject.setCommandLine(dto.getCommandLine());
                createAddressObjectNames.add(dto.getName());
            } else {
                if(!AliStringUtils.isEmpty(addressObjectName)) {
                    policyObject.setName(String.format("%s", addressObjectName));
                } else {
                    policyObject.setName(String.format("%s", ipAddressString));
                }
                policyObject.setCommandLine("");
            }
        }
        policyObject.setCreateObjectName(createAddressObjectNames);
        return policyObject;
    }


    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, Integer idleTimeout) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        List<String> createServiceObjectName = new ArrayList<>();
        List<String> createServiceGroupObjectName = new ArrayList<>();
        policyObjectDTO.setName(" service any");
        policyObjectDTO.setCommandLine("");

        SecurityHillStoneR5 r5 = new SecurityHillStoneR5();


        if(serviceList == null || serviceList.size() == 0) {
            return policyObjectDTO;
        } else if(serviceList.size() == 1) {
            ServiceDTO service = serviceList.get(0);
            String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
            if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                policyObjectDTO.setName(" service any");
                return policyObjectDTO;
            }
            if(AliStringUtils.isEmpty(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                if(protocol.equalsIgnoreCase("ICMP")) {
                    policyObjectDTO.setName(" service icmp");
                } else {
                    policyObjectDTO.setName(String.format(" service %s-any", protocol.toLowerCase()));
                }
            } else {
                PolicyObjectDTO dto = r5.generateServiceObject(serviceList, idleTimeout, serviceObjectName);
                String commandline = dto.getCommandLine();
                String name = " " + dto.getJoin();
                if(CollectionUtils.isNotEmpty(dto.getCreateServiceObjectName())) {
                    createServiceObjectName.addAll(dto.getCreateServiceObjectName());
                }
                if(CollectionUtils.isNotEmpty(dto.getCreateServiceGroupObjectNames())) {
                    createServiceGroupObjectName.addAll(dto.getCreateServiceGroupObjectNames());
                }
                policyObjectDTO.setName(name.replace("\n", ""));
                policyObjectDTO.setCommandLine(commandline);
            }
        } else {
            PolicyObjectDTO dto = r5.generateServiceObject(serviceList, idleTimeout, serviceObjectName);
            String commandline = dto.getCommandLine();
            String name = " " + dto.getJoin();
            if(CollectionUtils.isNotEmpty(dto.getCreateServiceObjectName())) {
                createServiceObjectName.addAll(dto.getCreateServiceObjectName());
            }
            if(CollectionUtils.isNotEmpty(dto.getCreateServiceGroupObjectNames())) {
                createServiceGroupObjectName.addAll(dto.getCreateServiceGroupObjectNames());
            }
            policyObjectDTO.setName(name.replace("\n", ""));
            policyObjectDTO.setCommandLine(commandline);
        }
        policyObjectDTO.setCreateServiceObjectName(createServiceObjectName);
        policyObjectDTO.setCreateServiceGroupObjectNames(createServiceGroupObjectName);
        return policyObjectDTO;
    }

    public void commonCreateService(PolicyObjectDTO dto,List<String> createServiceObjectName){
        List<String> createServiceObjectName1 = dto.getCreateServiceObjectName();
        if(CollectionUtils.isNotEmpty(createServiceObjectName1)){
            createServiceObjectName.addAll(createServiceObjectName1);
        }
    }

    String getAddressObjectString(String[] addressList, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("address %s\n", name));
        for(String ipAddress:addressList) {
            if(IpUtils.isIP(ipAddress)) {
                sb.append(String.format("ip %s/32", ipAddress));
            } else if(IpUtils.isIPSegment(ipAddress)) {
                sb.append("ip " + ipAddress + "\n");
            } else {
                String start = IpUtils.getStartIpFromIpAddress(ipAddress);
                String end = IpUtils.getEndIpFromIpAddress(ipAddress);
                sb.append(String.format("range %s %s\n", start, end));
            }
        }
        sb.append("exit\n\n");
        return sb.toString();
    }


    String getServiceObjectString(List<ServiceDTO> serviceList, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\n", name));
        for(ServiceDTO service: serviceList) {
            if(service.getDstPorts() != null) {
                String portString = service.getDstPorts();
                String[] ports = portString.split(SEPERATOR);
                for(String port:ports) {
                    sb.append(String.format("%s ", ProtocolUtils.getProtocolByString(service.getProtocol())));
                    sb.append("dst-port " + port + "\n");
                }
            } else {
                sb.append(String.format("%s \n", service.getProtocol()));
            }
        }
        sb.append("exit\n\n");
        return sb.toString();
    }
    public static void main(String[] args) {
        Hillstone r004 = new Hillstone();
        System.out.println("--------------------------------------------------------------------------");
        //源nat
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        sNatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
//        sNatPolicyDTO.setSrcIp("");
//        sNatPolicyDTO.setDstIp("");

        sNatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        sNatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        sNatPolicyDTO.setTheme("w1");


        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");
    }
}
