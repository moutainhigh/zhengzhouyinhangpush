package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.vender.Juniper.security.SecurityJuniperSRXImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EditJuniperSrx extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditJuniperSrx.class);

    public final int MAX_NAME_LENGTH = 24;

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO dto = new EditCommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        //入接口出接口
        dto.setSrcZone(policyDTO.getSrcZone());
        dto.setDstZone(policyDTO.getDstZone());
        //获取策略名
        if(!AliStringUtils.isEmpty(policyDTO.getEditPolicyName())){
            dto.setName(policyDTO.getEditPolicyName());
        }
        //获取原策略数据
        dto.setSecurityPolicy(policyDTO.getSecurityPolicy());
        dto.setAddressType(cmdDTO.getSetting().getAddressType());
        //获取已存在和还需创建数据
        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        if(ObjectUtils.isNotEmpty(policyDTO.getMergeProperty())){
            if(policyDTO.getMergeProperty()==0){
                dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
                dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
            }
            if(policyDTO.getMergeProperty()==1){
                dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
                dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
            }
            if(policyDTO.getMergeProperty()==2){
                dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
                dto.setRestServiceList(existObjectDTO.getRestServiceList());
            }
            DeviceDTO device = cmdDTO.getDevice();
            if(device.isVsys()){
                dto.setVsys(true);
            }
        }else {
            logger.error("无需合并的值");
            return null;
        }
        return composite(dto);
    }

    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    public String createCommandLine(EditCommandlineDTO dto){
        //初始化
        String srcZone = dto.getSrcZone();
        String dstZone = dto.getDstZone();
        if(AliStringUtils.isEmpty(srcZone)){
            srcZone = "any";
        }
        if(AliStringUtils.isEmpty(dstZone)){
            dstZone = "any";
        }
        String name = dto.getName();
        List<String> restSrcAddressList = null;
        List<String> restDstAddressList =null;
        List<ServiceDTO> restServiceList =null;
        List<String> existSrcAddressList = new ArrayList<>();
        List<String> existDstAddressList = new ArrayList<>();
        List<String> existServiceNameList = new ArrayList<>();
        boolean addressType = dto.getAddressType();
        if(CollectionUtils.isNotEmpty( dto.getRestSrcAddressList())){
            restSrcAddressList = dto.getRestSrcAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getRestDstAddressList())){
            restDstAddressList = dto.getRestDstAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getRestServiceList())){
            restServiceList = dto.getRestServiceList();
        }
        if(CollectionUtils.isNotEmpty( dto.getExistSrcAddressList())){
            existSrcAddressList = dto.getExistSrcAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getExistDstAddressList())){
            existDstAddressList = dto.getExistDstAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getExistServiceNameList())){
            existServiceNameList = dto.getExistServiceNameList();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("configure\n");
        //创建地址对象
        sb.append(createAddressCommandline(name,srcZone,restSrcAddressList,existSrcAddressList,addressType));
        sb.append(createAddressCommandline(name,dstZone,restDstAddressList,existDstAddressList,addressType));
        //创建服务对象
        StringBuilder existServiceName = new StringBuilder();
        for(String serviceName : existServiceNameList){
            existServiceName.append(serviceName);
        }
        PolicyObjectDTO service = generateServiceObject(restServiceList,existServiceName.toString(),null);
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s", service.getCommandLine()));
        }
        //生成编辑策略元素
        String srcString = createPolicyElement(existSrcAddressList);
        String dstString = createPolicyElement(existDstAddressList);
        //生成策略
        if(CollectionUtils.isNotEmpty(existSrcAddressList)){
            sb.append(String.format("set security policies from-zone %s to-zone %s policy %s match source-address %s\n", srcZone, dstZone, name,srcString));
        }
        if(CollectionUtils.isNotEmpty(existDstAddressList)){
            sb.append(String.format("set security policies from-zone %s to-zone %s policy %s match destination-address %s\n", srcZone, dstZone, name,dstString));
        }
        if(!AliStringUtils.isEmpty(service.getJoin())){
            sb.append(String.format("set security policies from-zone %s to-zone %s policy %s match application %s\n", srcZone, dstZone, name,service.getJoin()));
        }
        sb.append("commit\n");
        sb.append("exit");
        return sb.toString();
    }


    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }

    public String createAddressCommandline(String name,String srcZone,List<String> restSrcAddressList,List<String> existSrcAddressList,boolean addressType){
        //创建新增IP,并加入复用列表
        //多个ip建地址组
        StringBuilder sb = new StringBuilder();
        SecurityJuniperSRXImpl securityJuniperSRX = new SecurityJuniperSRXImpl();
        String objectName = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
        int indexIpv6 = 0;
        if (addressType){
            //复用非复用混合
            if(CollectionUtils.isNotEmpty(restSrcAddressList) && CollectionUtils.isNotEmpty(existSrcAddressList) && existSrcAddressList.size()+restSrcAddressList.size()>1){
                for(String srcIpName : existSrcAddressList){
                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n",srcZone,objectName,srcIpName));
                }
                for(String srcIp : restSrcAddressList){
                    String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                    String element = "";
                    if(IPUtil.isIPRange(srcIp) || IPUtil.isIPv6Range(srcIp)){
                        if (IPUtil.isIPv6Range(srcIp)){
                            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(srcIp);
                            for (String subIpv6 : toSubnetList) {
                                element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",srcZone,element,subIpv6));
                                if(toSubnetList.size() > 1) {
                                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", srcZone, objectName, element));
                                }
                                indexIpv6++;
                            }
                            continue;
                        }else {
                            String[] split = srcIp.split("-");
                            String start = split[0];
                            String end = split[1];
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                            sb.append(String.format("set security zones security-zone %s address-book address %s range-address %s to %s\n",srcZone,element,start,end));
                        }

                    } else if(IpUtils.isIPSegment(srcIp) || IP6Utils.isIPv6Subnet(srcIp)){
                        element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());

                        String[] array = StringUtils.split(srcIp, "/");
                        if(IP6Utils.isIPv6Subnet(srcIp)){
                            // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                            ipAddressSubnetIntDTO.setIp(array[0]);
                            ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                            Map<String,Object> map = new HashMap<>();
                            map.put("subnetIntAddressObjectName",element);
                            try {
                                sb.append(securityJuniperSRX.generateSubnetIntIpV6CommandLine(StatusTypeEnum.ADD,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,new String[]{srcZone}));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else {
                            sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",srcZone,element,srcIp));
                        }

                    }else {
                        element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                        if(IPUtil.isIPv6(srcIp)){
                            sb.append(String.format("set security zones security-zone %s address-book address %s %s/128 \n", srcZone , element, srcIp));
                        }else {
                            sb.append(String.format("set security zones security-zone %s address-book address %s %s/32\n",srcZone,element,srcIp));
                        }
                    }
                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n",srcZone,objectName,element));
                }
                existSrcAddressList.clear();
                existSrcAddressList.add(objectName);
            }else {
                //只有复用或者只有非复用
                if(CollectionUtils.isNotEmpty(existSrcAddressList) && existSrcAddressList.size()>1){
                    for(String srcIpName : existSrcAddressList){
                        sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n",srcZone,objectName,srcIpName));
                    }
                    existSrcAddressList.clear();
                    existSrcAddressList.add(objectName);
                }
                if(CollectionUtils.isNotEmpty(restSrcAddressList) && restSrcAddressList.size()>1){
                    for(String srcIp : restSrcAddressList){
                        String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                        String element = "";
                        if(IPUtil.isIPRange(srcIp) || IPUtil.isIPv6Range(srcIp)){
                            if (IPUtil.isIPv6Range(srcIp)){
                                List<String> toSubnetList = IP6Utils.convertRangeToSubnet(srcIp);
                                for (String subIpv6 : toSubnetList) {
                                    element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                    sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",srcZone,name,subIpv6));
                                    if(toSubnetList.size() > 1) {
                                        sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", srcZone, objectName, element));
                                    }
                                    indexIpv6++;
                                }
                                continue;
                            }else {
                                String[] split = srcIp.split("-");
                                String start = split[0];
                                String end = split[1];
                                element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                sb.append(String.format("set security zones security-zone %s address-book address %s range-address %s to %s\n", srcZone, element, start, end));
                            }
                        } else if(IpUtils.isIPSegment(srcIp) || IP6Utils.isIPv6Subnet(srcIp)){
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());

                            String[] array = StringUtils.split(srcIp, "/");
                            if(IP6Utils.isIPv6Subnet(srcIp)){
                                // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                                ipAddressSubnetIntDTO.setIp(array[0]);
                                ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                                Map<String,Object> map = new HashMap<>();
                                map.put("subnetIntAddressObjectName",element);
                                try {
                                    sb.append(securityJuniperSRX.generateSubnetIntIpV6CommandLine(StatusTypeEnum.ADD,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,new String[]{srcZone}));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",srcZone,element,srcIp));
                            }

                        }else {
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                            if(IPUtil.isIPv6(srcIp)){
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s/128 \n", srcZone , element, srcIp));
                            }else {
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s/32\n",srcZone,element,srcIp));
                            }
                        }
                        sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n",srcZone,objectName,element));
                    }
                    existSrcAddressList.add(objectName);
                }else if(CollectionUtils.isNotEmpty(restSrcAddressList) && restSrcAddressList.size()==1){
                    //单个ip
                    //set security zones security-zone trust address-book address test12_AO_1906 192.168.10.1/32
                    for(String srcIp : restSrcAddressList){
                        String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                        String element = "";
                        if(IPUtil.isIPRange(srcIp) || IPUtil.isIPv6Range(srcIp)){
                            if (IPUtil.isIPv6Range(srcIp)){
                                List<String> toSubnetList = IP6Utils.convertRangeToSubnet(srcIp);
                                for (String subIpv6 : toSubnetList) {
                                    element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                    sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",srcZone,name,subIpv6));
                                    if(toSubnetList.size() > 1) {
                                        sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", srcZone, objectName, element));
                                    }
                                    indexIpv6++;
                                }
                                existSrcAddressList.add(objectName);
                            }else {
                                String[] split = srcIp.split("-");
                                String start = split[0];
                                String end = split[1];
                                element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                sb.append(String.format("set security zones security-zone %s address-book address %s range-address %s to %s\n", srcZone, element, start, end));
                                existSrcAddressList.add(element);
                            }
                        } else if(IpUtils.isIPSegment(srcIp) || IP6Utils.isIPv6Subnet(srcIp)){
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());

                            String[] array = StringUtils.split(srcIp, "/");
                            if(IP6Utils.isIPv6Subnet(srcIp)){
                                // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                                ipAddressSubnetIntDTO.setIp(array[0]);
                                ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                                Map<String,Object> map = new HashMap<>();
                                map.put("subnetIntAddressObjectName",element);
                                try {
                                    sb.append(securityJuniperSRX.generateSubnetIntIpV6CommandLine(StatusTypeEnum.ADD,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,new String[]{srcZone}));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",srcZone,element,srcIp));
                            }
                            existSrcAddressList.add(element);
                        }else {
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                            if(IPUtil.isIPv6(srcIp)){
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s/128 \n", srcZone , element, srcIp));
                            }else {
                                sb.append(String.format("set security zones security-zone %s address-book address %s %s/32\n",srcZone,element,srcIp));
                            }
                            existSrcAddressList.add(element);
                        }
                    }
                }
            }
        }else {
            //复用非复用混合
            if(CollectionUtils.isNotEmpty(restSrcAddressList) && CollectionUtils.isNotEmpty(existSrcAddressList) && existSrcAddressList.size()+restSrcAddressList.size()>1){
                for(String srcIpName : existSrcAddressList){
                    sb.append(String.format("set security address-book global address-set %s address %s \n",objectName,srcIpName));
                }
                for(String srcIp : restSrcAddressList){
                    String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                    String element = "";
                    if(IPUtil.isIPRange(srcIp) || IPUtil.isIPv6Range(srcIp)){
                        if (IPUtil.isIPv6Range(srcIp)){
                            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(srcIp);
                            for (String subIpv6 : toSubnetList) {
                                element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                sb.append(String.format("set security address-book global address %s %s\n",element,subIpv6));
                                if(toSubnetList.size() > 1) {
                                    sb.append(String.format("set security address-book global address-set %s address %s \n", objectName, element));
                                }
                                indexIpv6++;
                            }
                            continue;
                        }else {
                            String[] split = srcIp.split("-");
                            String start = split[0];
                            String end = split[1];
                            element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                            sb.append(String.format("set security address-book global address %s range-address %s to %s\n", element, start, end));
                        }
                    } else if(IpUtils.isIPSegment(srcIp) || IP6Utils.isIPv6Subnet(srcIp)){
                        element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());

                        if(IP6Utils.isIPv6Subnet(srcIp)){
                            // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                            sb.append(String.format("set security address-book global address %s %s \n", element , srcIp));
                        }else {
                            sb.append(String.format("set security address-book global address %s %s\n", element, srcIp));
                        }
                    }else {
                        element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                        if(IPUtil.isIPv6(srcIp)){
                            sb.append(String.format("set security address-book global address %s %s/128 \n", element, srcIp));
                        }else {
                            sb.append(String.format("set security address-book global address %s %s/32\n", element, srcIp));
                        }
                    }
                    sb.append(String.format("set security address-book global address-set %s address %s \n",objectName,element));
                }
                existSrcAddressList.clear();
                existSrcAddressList.add(objectName);
            }else {
                //只有复用或者只有非复用
                if(CollectionUtils.isNotEmpty(existSrcAddressList) && existSrcAddressList.size()>1){
                    for(String srcIpName : existSrcAddressList){
                        sb.append(String.format("set security address-book global address-set %s address %s \n",objectName,srcIpName));
                    }
                    existSrcAddressList.clear();
                    existSrcAddressList.add(objectName);
                }
                if(CollectionUtils.isNotEmpty(restSrcAddressList) && restSrcAddressList.size()>1){
                    for(String srcIp : restSrcAddressList){
                        String ipFromIpSegment = IpUtils.getIpFromIpSegment(srcIp);
                        String element = "";
                        if(IPUtil.isIPRange(srcIp) || IPUtil.isIPv6Range(srcIp)){
                            if (IPUtil.isIPv6Range(srcIp)){
                                List<String> toSubnetList = IP6Utils.convertRangeToSubnet(srcIp);
                                for (String subIpv6 : toSubnetList) {
                                    element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                    sb.append(String.format("set security address-book global address %s %s\n",element,subIpv6));
                                    if(toSubnetList.size() > 1) {
                                        sb.append(String.format("set security address-book global address-set %s address %s \n", objectName, element));
                                    }
                                    indexIpv6++;
                                }
                                continue;
                            }else {
                                String[] split = srcIp.split("-");
                                String start = split[0];
                                String end = split[1];
                                element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                sb.append(String.format("set security address-book global address %s range-address %s to %s\n", element, start, end));
                            }
                        } else if(IpUtils.isIPSegment(srcIp) || IP6Utils.isIPv6Subnet(srcIp)){
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());

                            if(IP6Utils.isIPv6Subnet(srcIp)){
                                // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                                sb.append(String.format("set security address-book global address %s %s \n", element , srcIp));
                            }else {
                                sb.append(String.format("set security address-book global address %s %s\n", element, srcIp));
                            }
                        }else {
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                            if(IPUtil.isIPv6(srcIp)){
                                sb.append(String.format("set security address-book global address %s %s/128 \n", element, srcIp));
                            }else {
                                sb.append(String.format("set security address-book global address %s %s/32\n", element, srcIp));
                            }
                        }
                        sb.append(String.format("set security address-book global address-set %s address %s \n",objectName,element));
                    }
                    existSrcAddressList.add(objectName);
                }else if(CollectionUtils.isNotEmpty(restSrcAddressList) && restSrcAddressList.size()==1){
                    //单个ip
                    //set security zones security-zone trust address-book address test12_AO_1906 192.168.10.1/32
                    for(String srcIp : restSrcAddressList){
                        String element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                        if(IPUtil.isIPRange(srcIp) || IPUtil.isIPv6Range(srcIp)){
                            if (IPUtil.isIPv6Range(srcIp)){
                                List<String> toSubnetList = IP6Utils.convertRangeToSubnet(srcIp);
                                for (String subIpv6 : toSubnetList) {
                                    element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                    sb.append(String.format("set security address-book global address %s %s\n",element,subIpv6));
                                    if(toSubnetList.size() > 1) {
                                        sb.append(String.format("set security address-book global address-set %s address %s \n", objectName, element));
                                    }
                                    indexIpv6++;
                                }
                                existSrcAddressList.add(objectName);
                            }else {
                                String[] split = srcIp.split("-");
                                String start = split[0];
                                String end = split[1];
                                element = String.format("%s_AO_%s", name, IdGen.getRandomNumberString());
                                sb.append(String.format("set security address-book global address %s range-address %s to %s\n", element, start, end));
                                existSrcAddressList.add(element);
                            }
                        } else if(IpUtils.isIPSegment(srcIp) || IP6Utils.isIPv6Subnet(srcIp)){
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());

                            if(IP6Utils.isIPv6Subnet(srcIp)){
                                // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                                sb.append(String.format("set security address-book global address %s %s \n", element , srcIp));
                            }else {
                                sb.append(String.format("set security address-book global address %s %s\n", element, srcIp));
                            }
                            existSrcAddressList.add(element);
                        }else {
                            element = String.format("%s_AO_%s",name, IdGen.getRandomNumberString());
                            if(IPUtil.isIPv6(srcIp)){
                                sb.append(String.format("set security address-book global address %s %s/128 \n", element, srcIp));
                            }else {
                                sb.append(String.format("set security address-book global address %s %s/32\n", element, srcIp));
                            }
                            existSrcAddressList.add(element);
                        }
                        existSrcAddressList.add(element);
                    }
                }
            }
        }

        return sb.toString();
    }

    public String createPolicyElement(List<String> existSrcAddressList){
        StringBuilder sb = new StringBuilder();
        for (String src : existSrcAddressList){
            if(!AliStringUtils.isEmpty(src)){
                sb.append(String.format("%s",src)+" ");
            }
        }
        return sb.toString();
    }

    /**
     * 生成服务对象
     * @param serviceDTOList 服务列表
     * @return 服务对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName, Integer idleTimeout) {
        if(CollectionUtils.isNotEmpty(serviceDTOList)){
            reformServiceDTOList(serviceDTOList);
        }
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (CollectionUtils.isEmpty(serviceDTOList) && StringUtils.isBlank(existsServiceName)) {
            return dto;
        }

        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setObjectFlag(true);
            dto.setJoin(existsServiceName);
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        //对象名称集合
        List<String> serviceNameList = new ArrayList<>();

        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("");
                dto.setJoin("any");
                return dto;
            }

            //只有1个服务，且端口是any ，或icmp type是空
            if (serviceDTOList.size() == 1) {
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    //icmp协议，icmpType和icmpCode都为空
                    if (StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                        dto.setJoin("junos-icmp-all");
                        dto.setName(dto.getJoin());
                        return dto;
                    }
                } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                    if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        dto.setCommandLine("junos-" + protocolString + "-any");
                        dto.setJoin(dto.getCommandLine());
                        return dto;
                    }
                }
            }

            //多个服务建对象
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                String objName = "";
                //只有ICMP，则不用建对象，直接添加到组
                if(StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                    objName = "junos-icmp-all";
                    if(ObjectUtils.isNotEmpty(idleTimeout)){
                        objName += "_L";
                    }
                }else{
                    objName = PolicyConstants.POLICY_STR_VALUE_ICMP.toLowerCase();
                    if(ObjectUtils.isNotEmpty(idleTimeout)){
                        objName += "_L";
                    }
                    sb.append(String.format("set applications application %s protocol icmp ", objName));

                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("icmp-type %d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("icmp-code %d", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                }
                serviceNameList.add(objName);
            } else {

                //tcp、udp协议， 但是端口是any，则直接添加到组即可，不用建对象
                if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                    String name = String.format("junos-%s-any", protocolString.toLowerCase());
                    if(ObjectUtils.isNotEmpty(idleTimeout)){
                        name += "_L";
                    }
                    serviceNameList.add(name);
                    continue;
                }

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                for(String srcPortStr: srcPorts) {
                    for(String dstPortStr: dstPorts) {

                        String objName = getServiceNameByOne(protocolString, dstPortStr);
                        if(ObjectUtils.isNotEmpty(idleTimeout)){
                            objName += "_L";
                        }
                        sb.append(String.format("set applications application %s protocol %s ", objName, protocolString));
                        //前面的判断已经过滤了srcPort和dstPort同时为any的情况，此时只有一个有值，仅显示有值的即可，若同时有值，则同时显示
                        if(!srcPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String srcPortString = PortUtils.getPortString(srcPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("source-port %s ", srcPortString));
                        }
                        if(!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("destination-port %s ", dstPortString));
                        }
                        sb.append("\n");
                        serviceNameList.add(objName);
                    }
                }
            }
        }

        //将对象添加到组  如果只是1个对象，则不用建组，多个对象，需要建组
        if(serviceNameList.size() == 1){
            dto.setName(serviceNameList.get(0));
            dto.setJoin(serviceNameList.get(0));
        }else if(serviceNameList.size() > 1){
            String groupName = getServiceName(serviceDTOList);
            dto.setName(groupName);
            dto.setJoin(groupName);
            for(String objName : serviceNameList){
                sb.append(String.format("set applications application-set %s application %s \n", groupName, objName));
            }
        }

        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    public void reformServiceDTOList(List<ServiceDTO> serviceDTOList) {
        StringBuilder tcpSb = new StringBuilder();
        StringBuilder udpSb = new StringBuilder();
        StringBuilder icmpSb = new StringBuilder();
        ServiceDTO tcpServiceDTO = new ServiceDTO();
        ServiceDTO udpServiceDTO = new ServiceDTO();
        ServiceDTO icmpServiceDTO = new ServiceDTO();
        for(ServiceDTO serviceDTO : serviceDTOList){
            if("6".equals(serviceDTO.getProtocol())){
                tcpSb.append(serviceDTO.getDstPorts()).append(",");
            }
            if("17".equals(serviceDTO.getProtocol())){
                udpSb.append(serviceDTO.getDstPorts()).append(",");
            }
            if("1".equals(serviceDTO.getProtocol())){
                icmpSb.append(serviceDTO.getDstPorts()).append(",");
            }
        }

        if(!AliStringUtils.isEmpty(tcpSb.toString())){
            tcpServiceDTO.setProtocol("6");
            tcpServiceDTO.setSrcPorts("any");
            tcpServiceDTO.setDstPorts(tcpSb.toString().substring(0,tcpSb.length()-1));
        }
        if(!AliStringUtils.isEmpty(udpSb.toString())){
            udpServiceDTO.setProtocol("17");
            udpServiceDTO.setSrcPorts("any");
            udpServiceDTO.setDstPorts(udpSb.toString().substring(0,udpSb.length()-1));
        }
        if(!AliStringUtils.isEmpty(icmpSb.toString())){
            icmpServiceDTO.setProtocol("1");
            icmpServiceDTO.setSrcPorts("any");
            icmpServiceDTO.setDstPorts(icmpSb.toString().substring(0,icmpSb.length()-1));
        }

        serviceDTOList.clear();

        if(!AliStringUtils.isEmpty(tcpSb.toString())){
            serviceDTOList.add(tcpServiceDTO);
        }
        if(!AliStringUtils.isEmpty(udpSb.toString())){
            serviceDTOList.add(udpServiceDTO);
        }
        if(!AliStringUtils.isEmpty(icmpSb.toString())){
            serviceDTOList.add(icmpServiceDTO);
        }
    }

    /**获取服务名称***/
    public String getServiceName(List<ServiceDTO> serviceDTOList){
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(getServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

    public String getServiceNameByOne(String protocolString, String dstPort) {
        if(dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
            return protocolString;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%s", protocolString, dstPort));
        return sb.toString().toLowerCase();
    }

    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }


    public static void main(String[] args) {
        CmdDTO cmdDTO = new CmdDTO();
        PolicyDTO policyDTO = new PolicyDTO();
        TaskDTO taskDTO = new TaskDTO();
        ExistObjectDTO existObjectDTO = new ExistObjectDTO();
        PolicyRecommendSecurityPolicyDTO policyRecommendSecurityPolicyDTO = new PolicyRecommendSecurityPolicyDTO();

        policyDTO.setSecurityPolicy(policyRecommendSecurityPolicyDTO);
        cmdDTO.setExistObject(existObjectDTO);
        cmdDTO.setPolicy(policyDTO);
        cmdDTO.setTask(taskDTO);

        policyDTO.setMergeProperty(0);
        policyDTO.setSrcZone("Trust");
        policyDTO.setDstZone("UnTrust");


        List<String> a = new ArrayList<>();
        a.add("5.5.5.5");
        a.add("6.6.6.6/20");
        //existObjectDTO.setRestSrcAddressList(a);
        existObjectDTO.setRestDstAddressList(a);

        List<ServiceDTO> b = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("23");
        b.add(serviceDTO);
        //existObjectDTO.setRestServiceList(b);
        //existObjectDTO.setExistServiceNameList(a);


    }

}
