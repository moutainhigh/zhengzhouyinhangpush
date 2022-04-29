package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/8/13
 */
@Slf4j
@Service
public class HillStoneNatForTACX implements  NatPolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate hillstone nat策略");
        return  NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        return null;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src");
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst");
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), true, policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "");
//        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), policyDTO.isCreateObjFlag());

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(postAddressObject.getCommandLine())) {
            sb.append(postAddressObject.getCommandLine());
        }

//        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
//            sb.append(serviceObject.getCommandLine());
//        }

        sb.append("nat\n");
        String keyWord = isManyAddress(policyDTO.getSrcIp());
        sb.append(String.format("snatrule from %s to %s service \"Any\"%s trans-to %s mode %s log ",srcAddressObject.getName(), dstAddressObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()),  postAddressObject.getName(),keyWord));
        if(StringUtils.isNotEmpty(policyDTO.getDescription())){
            sb.append(String.format("description \"%s\"",policyDTO.getDescription()));
        }
        sb.append("\n");
        sb.append("exit\nend\n");
        return sb.toString();
    }

    /**
     * 天安财险单个源地址还是保留static；多个源地址则修改为dynamicport
     * @param srcIp
     * @return
     */
    private String isManyAddress(String srcIp){
        String keyWord = "static";
        if(StringUtils.isNotEmpty(srcIp) ){
            int length = srcIp.split(",").length;
            if(length>1){
                keyWord = "dynamicport";
            }else if(length == 1 ){

                String startIpFromIpAddress = IPUtil.getStartIpFromIpAddress(srcIp);
                String endIpFromIpAddress = IPUtil.getEndIpFromIpAddress(srcIp);
                if(!startIpFromIpAddress.equals(endIpFromIpAddress)){
                    keyWord = "dynamicport";
                }

            }

        }
        return keyWord;
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {

        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src");
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst");
        PolicyObjectDTO postAddressObject = getAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), policyDTO.getPostAddressObjectName(), policyDTO.isCreateObjFlag(), "");
//        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), policyDTO.isCreateObjFlag());

        if(!AliStringUtils.isEmpty(srcAddressObject.getCommandLine()) ) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if(!AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }

        if(!AliStringUtils.isEmpty(postAddressObject.getCommandLine())) {
            sb.append(postAddressObject.getCommandLine());
        }

//        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
//            sb.append(serviceObject.getCommandLine());
//        }

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

//        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());
//        if("any".equalsIgnoreCase(policyDTO.getPostPort())){
//            postPortString = "";
//        }
        sb.append(String.format("dnatrule %sfrom %s to %s service \"Any\" trans-to %s log ", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                 postAddressObject.getName()));
        if(StringUtils.isNotEmpty(policyDTO.getDescription())){
            sb.append(String.format("description \"%s\"",policyDTO.getDescription()));
        }
        sb.append("\n");
        sb.append("exit\nend\n");
        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        //山石BOTH NAT命令行为生成一个SNAT再生成一个DNAT
        StringBuilder sb = new StringBuilder();

        sb.append("configure\n");

        PolicyObjectDTO srcAddressObject = getAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.getSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "src");
        PolicyObjectDTO dstAddressObject = getAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.getDstAddressObjectName(), policyDTO.isCreateObjFlag(), "dst");
        PolicyObjectDTO postSrcAddressObject = getAddressObject(policyDTO.getPostSrcIp(), policyDTO.getTheme(), true, policyDTO.getPostSrcAddressObjectName(), policyDTO.isCreateObjFlag(), "");
        PolicyObjectDTO postDstAddressObject = getAddressObject(policyDTO.getPostDstIp(), policyDTO.getTheme(), policyDTO.getPostDstAddressObjectName(), policyDTO.isCreateObjFlag(), "");
//        PolicyObjectDTO serviceObject = getServiceObject(policyDTO.getServiceList(), policyDTO.getTheme(), policyDTO.getServiceObjectName(), policyDTO.isCreateObjFlag());

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

//        if(!AliStringUtils.isEmpty(serviceObject.getCommandLine())) {
//            sb.append(serviceObject.getCommandLine());
//        }

        sb.append("nat\n");
        String keyWord = isManyAddress(policyDTO.getSrcIp());
        sb.append(String.format("snatrule from %s to %s service \"Any\"%s trans-to %s mode %s log ",srcAddressObject.getName(), dstAddressObject.getName(),
                AliStringUtils.isEmpty(policyDTO.getDstItf())?"":(" eif " + policyDTO.getDstItf()),  postSrcAddressObject.getName(),keyWord));
        if(StringUtils.isNotEmpty(policyDTO.getDescription())){
            sb.append(String.format("description \"%s\"",policyDTO.getDescription()));
        }
        sb.append("\n");
        sb.append("exit\n");

        sb.append("nat\n");

        String srcItf = AliStringUtils.isEmpty(policyDTO.getSrcItf())?"":String.format("ingress-interface %s ", policyDTO.getSrcItf());

//        String postPortString = AliStringUtils.isEmpty(policyDTO.getPostPort())?"":String.format(" port %s", policyDTO.getPostPort());

        sb.append(String.format("dnatrule %sfrom %s to %s service \"Any\" trans-to %s log ", srcItf, srcAddressObject.getName(), dstAddressObject.getName(),
                 postDstAddressObject.getName()));
        if(StringUtils.isNotEmpty(policyDTO.getDescription())){
            sb.append(String.format("description \"%s\"",policyDTO.getDescription()));
        }
        sb.append("\n");
        sb.append("exit\n");

        sb.append("end\n");
        return sb.toString();
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, String addressObjectName, boolean isCreateObject, String prefix) {
        return getAddressObject(ipAddressString, theme, false, addressObjectName, isCreateObject, prefix);
    }

    PolicyObjectDTO getAddressObject(String ipAddressString, String theme, boolean isSNatPostAddress, String addressObjectName, boolean isCreateObject, String prefix) {
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


            PolicyObjectDTO dto = generateAddressObject(ipAddressString, theme, prefix, true, addressObjectName,null);

            if (isSNatPostAddress) {
                policyObject.setName(" address-book " + dto.getName());
            } else {
                policyObject.setName(" " + dto.getName());
            }
            policyObject.setCommandLine(dto.getCommandLine());

        } else {
            //若为单个ip范围，则建对象
            if(IpUtils.isIPRange(ipAddressString)) {

                PolicyObjectDTO dto = generateAddressObject(ipAddressString, theme, "", true, addressObjectName,null);

                if (isSNatPostAddress) {
                    policyObject.setName(" address-book " + dto.getName());
                } else {
                    policyObject.setName(" " + dto.getName());
                }
                policyObject.setCommandLine(dto.getCommandLine());
            } else {
                if(!AliStringUtils.isEmpty(addressObjectName)) {
                    policyObject.setName(String.format("%s", addressObjectName));
                } else {
                    policyObject.setName(String.format("%s", ipAddressString));
                }
                policyObject.setCommandLine("");
            }
        }
        return policyObject;
    }
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if(AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setJoin(ipPrefix + "-addr any\n");
            dto.setName("any");
            dto.setObjectFlag(true);
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        for (String address : arr) {
            // 是创建对象
            if (createObjFlag) {
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket,ipSystem, arr.length);
                sb.append("exit\n");
                dto.setCommandLine(sb.toString());
            } else {
                //直接显示内容
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket,ipSystem, arr.length);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }
        }
        return dto;
    }
    private void formatFullAddress(String address, StringBuilder sb, String ipPrefix, boolean createObjFlag, PolicyObjectDTO dto, String ticket,String ipSystem,int length) {
        String name;
        if(StringUtils.isNotEmpty(ipSystem)){
            if(length == 1){
                name = ipSystem;
            } else {
                name = ipSystem +"_" + address;
            }
        } else {
            name = String.format("%s_AO_%s",ticket, IdGen.getRandomNumberString());
        }

        String fullStr = "";
        if (IpUtils.isIPSegment(address)) {
            fullStr = String.format("ip %s\n", address);
        } else if (IpUtils.isIPRange(address)) {
            String startIp = IpUtils.getStartIpFromRange(address);
            String endIp = IpUtils.getEndIpFromRange(address);
            fullStr = String.format("range %s %s\n", startIp, endIp);
        } else {
            fullStr = String.format("ip %s/32\n", address);
        }
        dto.setName(name);
        sb.append(String.format("address %s\n", name));
        if (dto.getJoin() != null) {
            dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
        } else {
            dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
        }

        if (createObjFlag) {
            sb.append(fullStr);
        } else {
            sb.append(ipPrefix + "-" + fullStr);
        }
    }
    PolicyObjectDTO getServiceObject(List<ServiceDTO> serviceList, String theme, String serviceObjectName, boolean isCreateObject) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName(" service \"Any\"");
        policyObjectDTO.setCommandLine("");

        SecurityHillStoneR5 r5 = new SecurityHillStoneR5();


        if(serviceList == null || serviceList.size() == 0) {
            return policyObjectDTO;
        } else if(serviceList.size() == 1) {
            ServiceDTO service = serviceList.get(0);
            String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
            if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                policyObjectDTO.setName(" service \"Any\"");
                return policyObjectDTO;
            }
            if(AliStringUtils.isEmpty(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                if(protocol.equalsIgnoreCase("ICMP")) {
                    policyObjectDTO.setName(" service icmp");
                } else {
                    policyObjectDTO.setName(String.format(" service %s-any", protocol.toLowerCase()));
                }
            } else {
                PolicyObjectDTO dto = r5.generateServiceObject(serviceList, 1, serviceObjectName);
                String commandline = dto.getCommandLine();
                String name = " " + dto.getJoin();
                policyObjectDTO.setName(name.replace("\n", ""));
                policyObjectDTO.setCommandLine(commandline);
            }
        } else {
            PolicyObjectDTO dto = r5.generateServiceObject(serviceList, 1, serviceObjectName);
            String commandline = dto.getCommandLine();
            String name = " " + dto.getJoin();
            policyObjectDTO.setName(name.replace("\n", ""));
            policyObjectDTO.setCommandLine(commandline);
        }

        return policyObjectDTO;
    }

    public static void main(String[] args) {
        HillStoneNatForTACX r004 = new HillStoneNatForTACX();
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
        sNatPolicyDTO.setDescription("snatDesc");
       /* sNatPolicyDTO.setSrcZone("trust");
        sNatPolicyDTO.setDstZone("untrust");

        sNatPolicyDTO.setSrcItf("srcItf");
        sNatPolicyDTO.setDstItf("dstItf");*/

        sNatPolicyDTO.setTheme("a1");

     /*   sNatPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        sNatPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());

        sNatPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
        sNatPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
        sNatPolicyDTO.setPostAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());*/

        String snat = r004.generateSNatCommandLine(sNatPolicyDTO);
        System.out.println(snat);
        System.out.println("--------------------------------------------------------------------------");


        DNatPolicyDTO dnatPolicyDTO = new DNatPolicyDTO();

        dnatPolicyDTO.setSrcIp("192.168.2.1,192.168.2.2");
        dnatPolicyDTO.setDstIp("172.16.2.1,172.16.2.2");
        dnatPolicyDTO.setPostIpAddress("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        dnatPolicyDTO.setServiceList(ServiceDTO.getServiceList());
        dnatPolicyDTO.setRestServiceList(ServiceDTO.getServiceList());
        dnatPolicyDTO.setPostPort("27");

        dnatPolicyDTO.setSrcZone("trust");
        dnatPolicyDTO.setDstZone("untrust");

        dnatPolicyDTO.setSrcItf("srcItf");
        dnatPolicyDTO.setDstItf("dstItf");
        dnatPolicyDTO.setDescription("dnatDesc");
        dnatPolicyDTO.setTheme("w1");
        String dnat = r004.generateDNatCommandLine(dnatPolicyDTO);
        System.out.println(dnat);

        System.out.println("--------------------------------------------------------------------------");
        NatPolicyDTO bothNatDTO = new NatPolicyDTO();
        bothNatDTO.setSrcIp("192.168.2.1,192.168.2.2");
        bothNatDTO.setDstIp("172.16.2.1,172.16.2.2");
//        bothNatDTO.setSrcIp("");
//        bothNatDTO.setDstIp("");
        bothNatDTO.setPostSrcIp("192.168.1.0/24,1.1.1.1,2.2.2.2-3.3.3.3");
        bothNatDTO.setPostDstIp("172.16.1.0/24,7.7.7.7,63.2.2.2-75.3.3.6");
        bothNatDTO.setServiceList(ServiceDTO.getServiceList());
        bothNatDTO.setRestServiceList(ServiceDTO.getServiceList());
        bothNatDTO.setPostPort("27");
        bothNatDTO.setDescription("bothDesc");
        bothNatDTO.setRestPostServiceList(ServiceDTO.getServiceList());

       /* bothNatDTO.setSrcZone("trust");
        bothNatDTO.setDstZone("untrust");

        bothNatDTO.setSrcItf("srcItf");
        bothNatDTO.setDstItf("dstItf");*/

        bothNatDTO.setTheme("w1");
        String bothNat = r004.generateBothNatCommandLine(bothNatDTO);
        System.out.println(bothNat);

    }
}
