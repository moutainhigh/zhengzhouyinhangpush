package com.abtnetworks.totems.vender.fortinet.security;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class NatFortinetImpl extends AclFortinetImpl {

    @Override
    public String generateNatPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                     String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                     String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress,
                                                     ServiceParamDTO[] insideServiceParam, ServiceParamDTO[] globalServiceParam,
                                                     ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                     InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                     String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup,
                                                     String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup,
                                                     Map<String, Object> map, String[] args) throws Exception {
        StringBuffer staticNatPolicyCl = new StringBuffer();
        logger.info("开始生成staticNat命令行");
        if(ObjectUtils.isEmpty(map)){
            map=new HashMap<>();
            map.put("theme",ObjectUtils.isEmpty(name)?"":name);
        }
        staticNatPolicyCl.append("config firewall policy").append(StringUtils.LF);
        staticNatPolicyCl.append("edit ").append("0").append(StringUtils.LF);
        if(ObjectUtils.isEmpty(name)){
            name= (String) map.get("theme");
            name+="_AO_"+RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
            //paramDTO.setName(name);
        }
        if(ObjectUtils.isNotEmpty(name)) {
            staticNatPolicyCl.append(String.format("set name %s",name)).append(StringUtils.LF);
        }

        String src="";
        if (ObjectUtils.isNotEmpty(srcZone)){
            String[] zoneArray = srcZone.getNameArray();
            String   srcZoneName= srcZone.getName();
            if(ObjectUtils.isNotEmpty(zoneArray)) {
                src += String.join(" ", zoneArray);
            }
            if(ObjectUtils.isNotEmpty(srcZoneName)) {
                src+=srcZoneName;
            }
        }else if (ObjectUtils.isNotEmpty(inInterface)){
            String[] interfaceNameArray =inInterface.getNameArray();
            String inInterfaceName = inInterface.getName();
            if(ObjectUtils.isNotEmpty(interfaceNameArray)) {
                src += String.join(" ", interfaceNameArray);
            }
            if(ObjectUtils.isNotEmpty(inInterfaceName)) {
                src+=inInterfaceName;
            }
        }else {
            src = "\"any\"";
        }
        staticNatPolicyCl.append(String.format("set srcintf %s ",src)).append(StringUtils.LF);

        String dst="";
        if (ObjectUtils.isNotEmpty(dstZone)){
            String[] zoneArray = dstZone.getNameArray();
            String dstZoneName = dstZone.getName();
            if(ObjectUtils.isNotEmpty(zoneArray)) {
                dst+= String.join(" ", zoneArray);
            }
            if(ObjectUtils.isNotEmpty(dstZoneName)) {
                dst+=dstZoneName;
            }
        }else if (ObjectUtils.isNotEmpty(outInterface)){
            String[] outfaceNameArray =outInterface.getNameArray();
            String outInterfaceName = outInterface.getName();
            if(ObjectUtils.isNotEmpty(outfaceNameArray)) {
                dst += String.join(" ", outfaceNameArray);
            }
            if(ObjectUtils.isNotEmpty(outInterfaceName)) {
                dst+=outInterfaceName;
            }
        }else {
            dst = "\"any\"";
        }
        staticNatPolicyCl.append(String.format("set dstintf %s ",dst)).append(StringUtils.LF);

        staticNatPolicyCl.append("set srcaddr all").append("\n");
        String dstName = (String) map.get("name");
        String serviceName = (String) map.get("serviceName");
        staticNatPolicyCl.append("set dstaddr ").append(dstName).append("\n");

        staticNatPolicyCl.append("set action accept").append(StringUtils.LF);
        staticNatPolicyCl.append("set schedule always").append(StringUtils.LF);
        staticNatPolicyCl.append("set service ").append(serviceName).append(StringUtils.LF);
        staticNatPolicyCl.append("show").append(StringUtils.LF);
        staticNatPolicyCl.append("next").append(StringUtils.LF);
        if (ObjectUtils.isNotEmpty(moveSeatEnum)) {
            staticNatPolicyCl.append(generateChangePolicyPriorityCommandLine("#1",moveSeatEnum,swapRuleNameId,null,null));
        }
        return staticNatPolicyCl.toString();
    }

    /**
     * 生成源NAT策略命令行
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
     * @param srcIp 源ip ，需生成命令行
     * @param dstIp 目的ip 需生成命令行
     * @param serviceParam 服务（端口和协议），需要生成命令行
     * @param postSrcIpAddress 转换后源地址
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param eVr 下一跳VRouter
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用 服务对象
     * @param refServiceObjectGroup 引用 服务组对象
     * @param postSrcRefIpAddressObject 转换后源地址对象名
     * @param postSrcRefIpAddressObjectGroup 转换后源地址对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,
                                                ServiceParamDTO[] serviceParam,IpAddressParamDTO postSrcIpAddress,
                                                ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,String eVr,
                                                String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                String[] refServiceObject, String[] refServiceObjectGroup,
                                                String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        logger.info("开始生成sNat命令行");
        StringBuffer sNatPolicyCl = new StringBuffer();
        if(ObjectUtils.isEmpty(map)){
            map=new HashMap<>();
            map.put("theme",ObjectUtils.isEmpty(name)?"":name);
        }
        Map<String, String> postSrcMap = buildNatPoolAddress(statusTypeEnum, postSrcIpAddress, postSrcRefIpAddressObject, postSrcRefIpAddressObjectGroup,map);
        String poolName = postSrcMap.get("poolName");
        String poolCl= postSrcMap.get("poolCl");
        if(StringUtils.isNotEmpty(poolCl)){
            sNatPolicyCl.append(poolCl);
            sNatPolicyCl.append(StringUtils.LF);
        }
        sNatPolicyCl.append("config firewall policy").append(StringUtils.LF);
        sNatPolicyCl.append(String.format("edit %s\n", "0"));
        if(ObjectUtils.isEmpty(name)){
            name= (String) map.get("theme");
            name+="_AO_"+RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
            //paramDTO.setName(name);
        }
        if(ObjectUtils.isNotEmpty(name)) {
            sNatPolicyCl.append(String.format("set name %s",name)).append(StringUtils.LF);
        }


        String src="";
        if (ObjectUtils.isNotEmpty(srcZone)){
            String[] zoneArray = srcZone.getNameArray();
            String   srcZoneName= srcZone.getName();
            if(ObjectUtils.isNotEmpty(zoneArray)) {
                src += String.join(" ", zoneArray);
            }
            if(ObjectUtils.isNotEmpty(srcZoneName)) {
                src+=srcZoneName;
            }
        }else if (ObjectUtils.isNotEmpty(inInterface)){
            String[] interfaceNameArray =inInterface.getNameArray();
            String inInterfaceName = inInterface.getName();
            if(ObjectUtils.isNotEmpty(interfaceNameArray)) {
                src += String.join(" ", interfaceNameArray);
            }
            if(ObjectUtils.isNotEmpty(inInterfaceName)) {
                src+=inInterfaceName;
            }
        }else {
            src = "\"any\"";
        }
        sNatPolicyCl.append(String.format("set srcintf %s ",src)).append(StringUtils.LF);

        String dst="";
        if (ObjectUtils.isNotEmpty(dstZone)){
            String[] zoneArray = dstZone.getNameArray();
            String dstZoneName = dstZone.getName();
            if(ObjectUtils.isNotEmpty(zoneArray)) {
                dst+= String.join(" ", zoneArray);
            }
            if(ObjectUtils.isNotEmpty(dstZoneName)) {
                dst+=dstZoneName;
            }
        }else if (ObjectUtils.isNotEmpty(outInterface)){
            String[] outfaceNameArray =outInterface.getNameArray();
            String outInterfaceName = outInterface.getName();
            if(ObjectUtils.isNotEmpty(outfaceNameArray)) {
                dst += String.join(" ", outfaceNameArray);
            }
            if(ObjectUtils.isNotEmpty(outInterfaceName)) {
                dst+=outInterfaceName;
            }
        }else {
            dst = "\"any\"";
        }
        sNatPolicyCl.append(String.format("set dstintf %s ",dst)).append(StringUtils.LF);


        if (CollectionUtils.isNotEmpty(Arrays.asList(srcRefIpAddressObject))){
            sNatPolicyCl.append("set srcaddr ");
            for (String addressObject : srcRefIpAddressObject) {
                if (addressObject.contains(",")){
                    String[] strings = addressObject.split(",");
                    for (String string : strings) {
                        sNatPolicyCl.append(String.format("%s ", string));
                    }
                }else {
                    sNatPolicyCl.append(String.format("%s ", addressObject));
                }
            }
            sNatPolicyCl.append("\n");
        }else {
            sNatPolicyCl.append("set srcaddr all\n");
        }

        if (CollectionUtils.isNotEmpty(Arrays.asList(dstRefIpAddressObject))){
            sNatPolicyCl.append("set dstaddr ");
            for (String addressObject : dstRefIpAddressObject) {
                if (addressObject.contains(",")){
                    String[] strings = addressObject.split(",");
                    for (String string : strings) {
                        sNatPolicyCl.append(String.format("%s ", string));
                    }
                }else {
                    sNatPolicyCl.append(String.format("%s ", addressObject));
                }
            }
            sNatPolicyCl.append("\n");
        }else {
            sNatPolicyCl.append("set dstaddr all\n");
        }

        if (CollectionUtils.isNotEmpty(Arrays.asList(refServiceObject))){
            sNatPolicyCl.append("set service ");
            for (String service : refServiceObject) {
                if (service.contains(",")){
                    String[] strings = service.split(",");
                    for (String string : strings) {
                        sNatPolicyCl.append(String.format("%s ", string));
                    }
                }else {
                    sNatPolicyCl.append(String.format("%s ", service));
                }
            }
            sNatPolicyCl.append("\n");
        }


        sNatPolicyCl.append("set schedule always").append(StringUtils.LF);
        sNatPolicyCl.append("set action accept").append(StringUtils.LF);
        sNatPolicyCl.append("set nat enable").append(StringUtils.LF);
        if(StringUtils.isNotBlank(poolName)){
            sNatPolicyCl.append("set ippool enable").append(StringUtils.LF);
            sNatPolicyCl.append(String.format("set poolname %s",poolName)).append(StringUtils.LF);
        }
        if(StringUtils.isNotEmpty(description)){
            //排除命令行为空格
            if (description.trim().length() > 0) {
                sNatPolicyCl.append(String.format("set comments  %s ",description)).append(StringUtils.LF);;
            }
        }
        sNatPolicyCl.append("set match-vip disable").append(StringUtils.LF);
        sNatPolicyCl.append("show").append(StringUtils.LF);
        sNatPolicyCl.append("next").append(StringUtils.LF);
        if (ObjectUtils.isNotEmpty(moveSeatEnum)) {
            sNatPolicyCl.append(generateChangePolicyPriorityCommandLine("#1",moveSeatEnum,swapRuleNameId,null,null));
        }
        sNatPolicyCl.append(StringUtils.LF);
        logger.info("结束生成sNat命令行");
        return sNatPolicyCl.toString();
    }

    /**
     * 生成目的NAT策略命令行
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
     * @param serviceParam 服务
     * @param postDstIpAddress  转换后目的地址
     * @param postServiceParam 转换后服务
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用 服务对象
     * @param refServiceObjectGroup 引用 服务组对象
     * @param postDstRefIpAddressObject 转换后目的地址对象名
     * @param postDstRefIpAddressObjectGroup 转换后目的地址组对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,
                                                ServiceParamDTO[] serviceParam,IpAddressParamDTO postDstIpAddress,ServiceParamDTO[] postServiceParam,
                                                ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                String[] refServiceObject, String[] refServiceObjectGroup,
                                                String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        StringBuffer dNatPolicyCl = new StringBuffer();
        logger.info("开始生成dNat命令行");
        if(ObjectUtils.isEmpty(map)){
          map=new HashMap<>();
          map.put("theme",ObjectUtils.isEmpty(name)?"":name);
        }
        dNatPolicyCl.append("config firewall policy").append(StringUtils.LF);
        dNatPolicyCl.append("edit ").append("0").append(StringUtils.LF);
        if(ObjectUtils.isEmpty(name)){
            name= (String) map.get("theme");
            name+="_AO_"+RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
            //paramDTO.setName(name);
        }
        if(ObjectUtils.isNotEmpty(name)) {
            dNatPolicyCl.append(String.format("set name %s",name)).append(StringUtils.LF);
        }

        String src="";
        if (ObjectUtils.isNotEmpty(srcZone)){
            String[] zoneArray = srcZone.getNameArray();
            String   srcZoneName= srcZone.getName();
            if(ObjectUtils.isNotEmpty(zoneArray)) {
                src += String.join(" ", zoneArray);
            }
            if(ObjectUtils.isNotEmpty(srcZoneName)) {
                src+=srcZoneName;
            }
        }else if (ObjectUtils.isNotEmpty(inInterface)){
            String[] interfaceNameArray =inInterface.getNameArray();
            String inInterfaceName = inInterface.getName();
            if(ObjectUtils.isNotEmpty(interfaceNameArray)) {
                src += String.join(" ", interfaceNameArray);
            }
            if(ObjectUtils.isNotEmpty(inInterfaceName)) {
                src+=inInterfaceName;
            }
        }else {
            src = "\"any\"";
        }
        dNatPolicyCl.append(String.format("set srcintf %s ",src)).append(StringUtils.LF);

        String dst="";
        if (ObjectUtils.isNotEmpty(dstZone)){
            String[] zoneArray = dstZone.getNameArray();
            String dstZoneName = dstZone.getName();
            if(ObjectUtils.isNotEmpty(zoneArray)) {
                dst+= String.join(" ", zoneArray);
            }
            if(ObjectUtils.isNotEmpty(dstZoneName)) {
                dst+=dstZoneName;
            }
        }else if (ObjectUtils.isNotEmpty(outInterface)){
            String[] outfaceNameArray =outInterface.getNameArray();
            String outInterfaceName = outInterface.getName();
            if(ObjectUtils.isNotEmpty(outfaceNameArray)) {
                dst += String.join(" ", outfaceNameArray);
            }
            if(ObjectUtils.isNotEmpty(outInterfaceName)) {
                dst+=outInterfaceName;
            }
        }else {
            dst = "\"any\"";
        }
        dNatPolicyCl.append(String.format("set dstintf %s ",dst)).append(StringUtils.LF);

        if (CollectionUtils.isNotEmpty(Arrays.asList(srcRefIpAddressObject))){
            dNatPolicyCl.append("set srcaddr ");
            for (String addressObject : srcRefIpAddressObject) {
                dNatPolicyCl.append(String.format("%s ", addressObject));
            }
            dNatPolicyCl.append("\n");
        }else {
            dNatPolicyCl.append("set srcaddr all\n");
        }


        String dstName = (String) map.get("name");
        String refTimeName = (String)map.get("refTimeName");
        dNatPolicyCl.append("set dstaddr ").append(dstName).append("\n");

        dNatPolicyCl.append("set action accept").append(StringUtils.LF);
        dNatPolicyCl.append(String.format("set schedule %s",refTimeName)).append(StringUtils.LF);
        if (CollectionUtils.isNotEmpty(Arrays.asList(refServiceObject))){
            dNatPolicyCl.append("set service ");
            for (String service : refServiceObject) {
                dNatPolicyCl.append(String.format("%s ", service));
            }
            dNatPolicyCl.append("\n");
        }
        dNatPolicyCl.append("set match-vip enable").append(StringUtils.LF);
        dNatPolicyCl.append("show").append(StringUtils.LF);
        dNatPolicyCl.append("next").append(StringUtils.LF);
        if (ObjectUtils.isNotEmpty(moveSeatEnum)) {
            dNatPolicyCl.append(generateChangePolicyPriorityCommandLine("#1",moveSeatEnum,swapRuleNameId,null,null));
        }
        logger.info("生成完成dNat命令行");
        return dNatPolicyCl.toString();
    }

    private Map<String,String> buildNatIpAddress(StatusTypeEnum statusTypeEnum, IpAddressParamDTO ipAddressParamDTO, String[] refIpAddressObject, String[] refIpAddressObjectGroup) throws Exception {
        //String addressName =  "any";
        StringBuffer ipAddressGroupCl = new StringBuffer();
        List<String> newSrcIpAddressObjectNameList = new ArrayList<>();
        if(ipAddressParamDTO  != null){
            //StringBuffer ipAddressGroupCl = new StringBuffer();
            if(ObjectUtils.isNotEmpty(ipAddressParamDTO)){
                //生成src地址对象命令行
                if(ArrayUtils.isNotEmpty(ipAddressParamDTO.getSingleIpArray())){
                    String singleIpObjectName=createIpAddressObjectNameBySingleIpArray(ipAddressParamDTO.getSingleIpArray(),new HashMap<>(),null);;
                    if(StringUtils.isNotBlank(ipAddressParamDTO.getName())) {
                        singleIpObjectName=ipAddressParamDTO.getName();
                    }
                    newSrcIpAddressObjectNameList.add(singleIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipAddressParamDTO.getIpTypeEnum(), singleIpObjectName, null, ipAddressParamDTO.getSingleIpArray(), null, null, null, null,
                            null, null, null, null, null, new HashMap<>(), null));
                }
                if(ArrayUtils.isNotEmpty(ipAddressParamDTO.getRangIpArray())){
                    String rangeIpObjectName = createIpAddressObjectNameByRangIpArray(ipAddressParamDTO.getRangIpArray(), new HashMap<>(), null);
                    if(StringUtils.isNotBlank(ipAddressParamDTO.getName())) {
                        rangeIpObjectName=ipAddressParamDTO.getName();
                    }
                    newSrcIpAddressObjectNameList.add(rangeIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipAddressParamDTO.getIpTypeEnum(), rangeIpObjectName, null,null, ipAddressParamDTO.getRangIpArray(), null, null, null, null,
                            null, null, null, null, new HashMap<>(), null));
                }
                if(ArrayUtils.isNotEmpty(ipAddressParamDTO.getSubnetIntIpArray())){
                    String subnetIntIpObjectName = createIpAddressObjectNameByIpSubArray(ipAddressParamDTO.getSubnetIntIpArray(), null,Boolean.TRUE, new HashMap<>(), null);
                    if(StringUtils.isNotBlank(ipAddressParamDTO.getName())) {
                        subnetIntIpObjectName=ipAddressParamDTO.getName();
                    }
                    newSrcIpAddressObjectNameList.add(subnetIntIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipAddressParamDTO.getIpTypeEnum(), subnetIntIpObjectName, null, null, null, ipAddressParamDTO.getSubnetIntIpArray(), null, null,
                            null, null, null, null, null, new HashMap<>(), null));
                }
                if(ArrayUtils.isNotEmpty(ipAddressParamDTO.getSubnetStrIpArray())){
                    String subnetStrIpObjectName = createIpAddressObjectNameByIpSubArray(null,ipAddressParamDTO.getSubnetStrIpArray(),Boolean.TRUE, new HashMap<>(), null);
                    if(StringUtils.isNotBlank(ipAddressParamDTO.getName())) {
                        subnetStrIpObjectName=ipAddressParamDTO.getName();
                    }
                    newSrcIpAddressObjectNameList.add(subnetStrIpObjectName);
                    ipAddressGroupCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum, ipAddressParamDTO.getIpTypeEnum(), subnetStrIpObjectName, null, null, null, null, ipAddressParamDTO.getSubnetStrIpArray(), null,
                            null, null, null, null, null, new HashMap<>(), null));
                }
            }
        }
        Map<String,String> map = new HashMap<>();
        if(ObjectUtils.isEmpty(newSrcIpAddressObjectNameList)){
            newSrcIpAddressObjectNameList.add("");
        }
        map.put("addressName",newSrcIpAddressObjectNameList.get(0));
        map.put("addressCl",ipAddressGroupCl.toString());
        return map;
    }
    

    private Map<String,String> buildNatService(StatusTypeEnum statusTypeEnum, ServiceParamDTO[] serviceParam, String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray) throws Exception {
        String serviceObjectCommandLine ="";
        List<String> newServiceObjectNameList = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(serviceParam)){
            //ServiceParamDTO[] serviceParam= paramDTO.getServiceParam();
            if(ArrayUtils.isNotEmpty(serviceParam)){
                for (ServiceParamDTO serviceParamDTO:serviceParam) {
                    Map theme=new HashMap();
                    String serviceObjectName = this.createServiceObjectName(serviceParamDTO, null, null);
                    theme.put("name",serviceObjectName);
                    newServiceObjectNameList.add(serviceObjectName);
                    ArrayList<ServiceParamDTO> newService = new ArrayList<>();
                    newService.add(serviceParamDTO);
                    serviceObjectCommandLine+=this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, newService, null, theme,null)+"\n";
                }
            }
        }
        Map<String,String> map = new HashMap<>();
        map.put("serviceName",String.join(" ", newServiceObjectNameList));
        map.put("serviceCl",serviceObjectCommandLine.toString());
        return map;
    }



    String createPoolObjectName(Map<String, Object> map ,String[] args){
        String name =(String) map.get("name");
        if(ObjectUtils.isEmpty(name)){
            name= (String) map.get("theme");
            name+="_pool_"+RandomStringUtils.random(4, new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8','9'});
        }
        return name;
    }


    private Map<String, String> buildNatPoolAddress(StatusTypeEnum statusTypeEnum, IpAddressParamDTO postSrcIpAddress, String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup, Map<String, Object> map) {
        Map poolMap = new HashMap();
        StringBuilder commandLine = new StringBuilder();
        commandLine.append("config firewall ippool").append(StringUtils.LF);;
        String poolObjectName = createPoolObjectName(map, null);
        commandLine.append(String.format("edit %s",poolObjectName)).append(StringUtils.LF);
        IpAddressRangeDTO[] rangIpArray = postSrcIpAddress.getRangIpArray();
        String[] singleIpArray = postSrcIpAddress.getSingleIpArray();
        IpAddressSubnetIntDTO[] subnetIntIpArray = postSrcIpAddress.getSubnetIntIpArray();
        if(ObjectUtils.isNotEmpty(rangIpArray)){
            IpAddressRangeDTO dto = rangIpArray[0];
            commandLine.append(String.format("set startip %s",dto.getStart())).append(StringUtils.LF);
            commandLine.append(String.format("set endip %s",dto.getEnd())).append(StringUtils.LF);
        }else if(ObjectUtils.isNotEmpty(subnetIntIpArray)){
            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIntIpArray[0];
            long[] ipStartEnd= TotemsIpUtils.getIpStartEndBySubnetMask(ipAddressSubnetIntDTO.getIp(), String.valueOf(ipAddressSubnetIntDTO.getMask()));
            String startIp =TotemsIpUtils.IPv4NumToString(ipStartEnd[0]);
            String endIp =TotemsIpUtils.IPv4NumToString(ipStartEnd[1]);
            commandLine.append(String.format("set startip %s",startIp)).append(StringUtils.LF);
            commandLine.append(String.format("set endip %s",endIp)).append(StringUtils.LF);
        }else if(ObjectUtils.isNotEmpty(singleIpArray)){
            String ip = singleIpArray[0];
            commandLine.append(String.format("set startip %s",ip)).append(StringUtils.LF);
            commandLine.append(String.format("set endip %s",ip)).append(StringUtils.LF);
        }
        commandLine.append("next").append(StringUtils.LF);;
        commandLine.append("end").append(StringUtils.LF);;
        poolMap.put("poolName",poolObjectName);
        poolMap.put("poolCl",commandLine.toString());
        return poolMap;
    }

}
