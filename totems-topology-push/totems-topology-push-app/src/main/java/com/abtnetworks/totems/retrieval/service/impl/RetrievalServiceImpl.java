package com.abtnetworks.totems.retrieval.service.impl;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.dto.PolicyEditParamDTO;
import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.enums.DeviceModelNumberEnumExtended;
import com.abtnetworks.totems.command.line.enums.EditTypeEnums;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.common.dto.TwoMemberObject;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.DeviceObjectContentUtil;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.ObjectCompareUtils;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.retrieval.dto.RetrievalAddressDto;
import com.abtnetworks.totems.retrieval.dto.RetrievalParamDto;
import com.abtnetworks.totems.retrieval.dto.RetrievalPolicyDto;
import com.abtnetworks.totems.retrieval.dto.RetrievalServiceDto;
import com.abtnetworks.totems.retrieval.service.RetrievalService;
import com.abtnetworks.totems.whale.baseapi.dto.BatchDeviceDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchAddressDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchServiceDTO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.FilterRuleInternalRO;
import com.abtnetworks.totems.whale.baseapi.ro.IncludeFilterServicesRO;
import com.abtnetworks.totems.whale.baseapi.ro.IncludeItemsRO;
import com.abtnetworks.totems.whale.baseapi.ro.Ip4RangeRO;
import com.abtnetworks.totems.whale.baseapi.ro.NetWorkGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.ro.ObjectInternalRO;
import com.abtnetworks.totems.whale.baseapi.ro.ServiceGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.github.pagehelper.util.StringUtil;
import com.microsoft.schemas.vml.STTrueFalse;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RetrievalServiceImpl implements RetrievalService {

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    private static final String groupName = "NETWORK_GROUP_OBJECT";
    private static final String addressName = "NETWORK_OBJECT";

    public void main(){
        RetrievalParamDto retrievalParamDto = new RetrievalParamDto();
        RetrievalAddressDto addressDto = new RetrievalAddressDto();
        addressDto.setDeviceUuid("71b0f1d41b3a4007be838b4cd6391a3f");
        addressDto.setAddressName("aaaa");
        addressDto.setAddressContent("1.1.1.1.1.1.12");

        RetrievalPolicyDto policyDto = new RetrievalPolicyDto();
        policyDto.setDeviceUuid("093fa6db2c7d4188bff8cc5a948f5604");
        policyDto.setPolicyName("物资域移动应用V2.0系统");
        policyDto.setDeviceUuid("71b0f1d41b3a4007be838b4cd6391a3f");
        policyDto.setDstIp("PMIS-IDC2-HOST-150.22.60();aaaa()");


        retrievalParamDto.addAddressDto(addressDto);
        retrievalParamDto.addPolicyDto(policyDto);


        DeviceModelNumberEnumExtended modelNumber = DeviceModelNumberEnumExtended.USG6000;
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(modelNumber.getSecurityClass());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        List<RetrievalAddressDto> list = new ArrayList<>();
        list.add(addressDto);
        String com = this.getAddressObjectCommandline(overAllGeneratorAbstractBean,"71b0f1d41b3a4007be838b4cd6391a3f",list);
        System.out.println(com);


    }

    @Override
    public ReturnResult getCommandline2(RetrievalParamDto retrievalParamDto){

        ReturnResult returnResult = new ReturnResult();

        StringBuffer allCommend = new StringBuffer();
        //设备信息处理   获得设备UUID
        Map<String,RetrievalParamDto> dtoToUuid = splitUiudToMap(retrievalParamDto);
        if(dtoToUuid.size() !=1 ){
            allCommend.append("参数不合法，请联系工作人员");
            returnResult.setContent(allCommend.toString());
            returnResult.setCode(ReturnResult.FAIL_CODE);
            return returnResult;
        }
        String deviceUuid = null;
        for (Map.Entry<String,RetrievalParamDto> paramMap : dtoToUuid.entrySet()) {
            deviceUuid = paramMap.getKey();
        }

        //查询设备基本信息
        BatchDeviceDTO batchDeviceDTO =new BatchDeviceDTO();
        batchDeviceDTO.setDeviceUuids(Collections.singletonList(deviceUuid));
        ResultRO<List<DeviceDataRO>> ro =  whaleDeviceObjectClient.getDeviceROByUuidList(batchDeviceDTO);
        if(!ro.getSuccess() || ro.getData()==null || ro.getData().size()==0){
            allCommend.append(String.format("设备UUID：%s 查询设备信息为空。", deviceUuid));
            returnResult.setContent(allCommend.toString());
            returnResult.setCode(ReturnResult.FAIL_CODE);
            return returnResult;
        }
        //获得设备型号
        DeviceModelNumberEnumExtended modelNumber = null;
        try{
            NodeEntity node = null;
            if (StringUtils.isNotBlank(deviceUuid)) {
                node = taskService.getTheNodeByUuid(deviceUuid);
                if(node == null) {
                    log.error(String.format("设备UUID：%s 查询设备型号为空。", deviceUuid));
                }
            }
            modelNumber = DeviceModelNumberEnumExtended.fromString(node.getModelNumber());
//            modelNumber = DeviceModelNumberEnumExtended.USG6000;
        }catch (Exception e){
            allCommend.append(String.format("设备UUID：%s 查询设备型号为空。", deviceUuid));
            returnResult.setContent(allCommend.toString());
            returnResult.setCode(ReturnResult.FAIL_CODE);
            return returnResult;
        }

        //检测设备是否能生成命令行
        String checkresult = checkCommandline(modelNumber);
        if(checkresult!=null){
            allCommend.append(checkresult);
            returnResult.setContent(allCommend.toString());
            returnResult.setCode(ReturnResult.SUCCESS_CODE);
            return returnResult;
        }
        //处理参数
        LinkedList<PolicyEditParamDTO> addressParam = dealWithAddressParam(retrievalParamDto.getAddress());
        LinkedList<PolicyEditParamDTO> policyParam = dealWithPolicyParam(retrievalParamDto.getPolicy());

        //获得生成器
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(modelNumber.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            allCommend.append("构造对象异常");
            returnResult.setContent(allCommend.toString());
            returnResult.setCode(ReturnResult.FAIL_CODE);
            return returnResult;
        }

        try {
            //前置命令行
            allCommend.append(overAllGeneratorAbstractBean.generatePreCommandline(ro.getData().get(0).getIsVsys(),ro.getData().get(0).getVsysName(),null,null));
            //生成命令行
            allCommend.append(generaAddressCommendline(overAllGeneratorAbstractBean,addressParam));
//            allCommend.append(generaServiceCommendline(overAllGeneratorAbstractBean,addressParam));
            allCommend.append(generaPolicyCommendline(overAllGeneratorAbstractBean,policyParam));
            //后置命令行
            allCommend.append(overAllGeneratorAbstractBean.generatePostCommandline(null,null));
        } catch (Exception e) {
            log.error("生成策略命令行出错:",e);
            allCommend.append("生成策略命令行出错");
            returnResult.setContent(allCommend.toString());
            returnResult.setCode(ReturnResult.FAIL_CODE);
            return returnResult;
        }

        returnResult.setContent(allCommend.toString());
        returnResult.setCode(ReturnResult.SUCCESS_CODE);
        return returnResult;
    }

    public String generaAddressCommendline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean,LinkedList<PolicyEditParamDTO> paramDTOList){
        //参数调用命令行生成器，生成最终命令行
        if(paramDTOList==null || paramDTOList.size()==0){
            return "";
        }
        StringBuffer commandLine = new StringBuffer();
        for(PolicyEditParamDTO policyEditParamDTO : paramDTOList){
            try {
                if(policyEditParamDTO.getGroupName() != null){
                    commandLine.append(overAllGeneratorAbstractBean.generateIpAddressObjectGroupCommandLine(policyEditParamDTO.getStatusTypeEnum(),RuleIPTypeEnum.IP4,policyEditParamDTO.getGroupName(),policyEditParamDTO.getId(),
                            policyEditParamDTO.getSrcIp().getSingleIpArray(),policyEditParamDTO.getSrcIp().getRangIpArray(),policyEditParamDTO.getSrcIp().getSubnetIntIpArray(),policyEditParamDTO.getSrcIp().getSubnetStrIpArray(),
                            null,policyEditParamDTO.getSrcIp().getHosts(),policyEditParamDTO.getSrcIp().getObjectNameRefArray(),policyEditParamDTO.getSrcIp().getObjectGroupNameRefArray(),null,
                            null,null,null,null));
                }else{
                    commandLine.append(overAllGeneratorAbstractBean.generateIpAddressObjectCommandLine(policyEditParamDTO.getStatusTypeEnum(),RuleIPTypeEnum.IP4,policyEditParamDTO.getName(),policyEditParamDTO.getId(),
                            policyEditParamDTO.getSrcIp().getSingleIpArray(),policyEditParamDTO.getSrcIp().getRangIpArray(),policyEditParamDTO.getSrcIp().getSubnetIntIpArray(),policyEditParamDTO.getSrcIp().getSubnetStrIpArray(),
                            null,policyEditParamDTO.getSrcIp().getHosts(),policyEditParamDTO.getSrcIp().getObjectNameRefArray(),null,
                            null,null,null,null));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return commandLine.toString();
    }

    public String generaPolicyCommendline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean,LinkedList<PolicyEditParamDTO> paramDTOList){
        //参数调用命令行生成器，生成最终命令行
        if(paramDTOList==null || paramDTOList.size()==0){
            return "";
        }
        StringBuffer commandLine = new StringBuffer();
        for(PolicyEditParamDTO policyEditParamDTO : paramDTOList){
            try {
                commandLine.append(overAllGeneratorAbstractBean.generateSecurityPolicyModifyCommandLine(StatusTypeEnum.MODIFY,policyEditParamDTO,null,null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return commandLine.toString();
    }


    public LinkedList<PolicyEditParamDTO> dealWithAddressParam(List<RetrievalAddressDto> addressList){
        if(addressList==null || addressList.size()==0){
            return null;
        }
        LinkedList<PolicyEditParamDTO> addResult = new LinkedList<>();
        LinkedList<PolicyEditParamDTO> modefyResult = new LinkedList<>();
        for(RetrievalAddressDto addressDto:addressList){
            PolicyEditParamDTO dto = new PolicyEditParamDTO();

            if(addressDto.getAddressType() == null){
                dto.setStatusTypeEnum(StatusTypeEnum.ADD);
                dto.setName(addressDto.getAddressName());
            }else{
                dto.setStatusTypeEnum(StatusTypeEnum.MODIFY);
                if(groupName.equals(addressDto.getAddressType())){
                    dto.setGroupName(addressDto.getAddressName());
                }else if(addressName.equals(addressDto.getAddressType())){
                    dto.setName(addressDto.getAddressName());
                }
            }

            IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
            String content = addressDto.getAddressContent();
            if(content != null){
                String[] contents = content.split(";");
                for(String address : contents){
                    dealWithAddress(ipAddressParamDTO,address);
                }
            }
            dto.setSrcIp(ipAddressParamDTO);
            if(dto.getStatusTypeEnum() == StatusTypeEnum.ADD){
                addResult.add(dto);
            }else{
                modefyResult.add(dto);
            }
        }
        addResult.addAll(modefyResult);
        return addResult;
    }

    public LinkedList<PolicyEditParamDTO> dealWithPolicyParam(List<RetrievalPolicyDto> policyDtoList){
        if(policyDtoList==null || policyDtoList.size()==0){
            return null;
        }
        LinkedList<PolicyEditParamDTO> result = new LinkedList<>();
        for(RetrievalPolicyDto policyDto : policyDtoList){

            if(policyDto.getSrcIp() != null){
                PolicyEditParamDTO paramDTO = new PolicyEditParamDTO();
                paramDTO.setName(policyDto.getPolicyName());
                paramDTO.setEditTypeEnums(EditTypeEnums.SRC_ADDRESS);
                String[] values = policyDto.getSrcIp().split(";");
                for(String val : values){
                    if(StringUtil.isEmpty(val)){
                        continue;
                    }
                    if(paramDTO.getSrcRefIpAddressObject() == null){
                        paramDTO.setSrcRefIpAddressObject(new String[]{getObjectname(val)});
                    }else{
                        List<String> dtoList = Arrays.asList(paramDTO.getSrcRefIpAddressObject());
                        List<String> dtoLists = new ArrayList<>(dtoList);
                        dtoLists.add(getObjectname(val));
                        String[] param = new String[dtoLists.size()];
                        paramDTO.setSrcRefIpAddressObject(dtoLists.toArray(param));
                    }
                }
                result.add(paramDTO);
            }
            if(policyDto.getDstIp() != null){
                PolicyEditParamDTO paramDTO = new PolicyEditParamDTO();
                paramDTO.setName(policyDto.getPolicyName());
                paramDTO.setEditTypeEnums(EditTypeEnums.DST_ADDRESS);
                String[] values = policyDto.getDstIp().split(";");
                for(String val : values){
                    if(StringUtil.isEmpty(val)){
                        continue;
                    }
                    if(paramDTO.getDstRefIpAddressObject() == null){
                        paramDTO.setDstRefIpAddressObject(new String[]{getObjectname(val)});
                    }else{
                        List<String> dtoList = Arrays.asList(paramDTO.getDstRefIpAddressObject());
                        List<String> dtoLists = new ArrayList<>(dtoList);
                        dtoLists.add(getObjectname(val));
                        String[] param = new String[dtoLists.size()];
                        paramDTO.setDstRefIpAddressObject(dtoLists.toArray(param));
                    }
                }
                result.add(paramDTO);
            }
            if(policyDto.getService() != null){

            }
        }
        return result;
    }

    @Override
    public Map<String,String> getCommandline(RetrievalParamDto retrievalParamDto){

        Map<String,RetrievalParamDto> dtoToUuid = splitUiudToMap(retrievalParamDto);

        Map<String,String> allCommend = new HashMap<>();
        if(dtoToUuid!=null){
            for (Map.Entry<String,RetrievalParamDto> paramMap : dtoToUuid.entrySet()) {
                StringBuffer commandLineSb = new StringBuffer();
                String deviceUuid = paramMap.getKey();
                RetrievalParamDto dto = paramMap.getValue();

                //查询设备基本信息
                BatchDeviceDTO batchDeviceDTO =new BatchDeviceDTO();
                batchDeviceDTO.setDeviceUuids(Collections.singletonList(deviceUuid));
                ResultRO<List<DeviceDataRO>> ro =  whaleDeviceObjectClient.getDeviceROByUuidList(batchDeviceDTO);
                if(!ro.getSuccess() || ro.getData()==null || ro.getData().size()==0){
                    allCommend.put(deviceUuid,String.format("设备UUID：%s 查询设备信息为空。", deviceUuid));
                    continue;
                }
                //设备信息处理
                DeviceModelNumberEnumExtended modelNumber = null;
                try{
                    NodeEntity node = null;
                    if (StringUtils.isNotBlank(deviceUuid)) {
                        node = taskService.getTheNodeByUuid(deviceUuid);
                        if(node == null) {
                            log.error(String.format("设备UUID：%s 查询设备信息为空。", deviceUuid));
                        }
                    }
                    modelNumber = DeviceModelNumberEnumExtended.fromString(node.getModelNumber());
//                    modelNumber = DeviceModelNumberEnumExtended.USG6000;
                }catch (Exception e){
                    allCommend.put(deviceUuid,String.format("设备UUID：%s 查询设备信息为空。", deviceUuid));
                    continue;
                }
                //检测设备是否能生成命令行
                String checkresult = checkCommandline(modelNumber);
                if(checkresult!=null){
                    allCommend.put(deviceUuid,checkresult);
                    continue;
                }

                OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
                try {
                        overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(modelNumber.getSecurityClass());
                } catch (Exception e) {
                    log.error("构造对象异常:",e);
                    allCommend.put(deviceUuid,"构造对象异常");
                }

                try {
                    //前置命令行
                    commandLineSb.append(overAllGeneratorAbstractBean.generatePreCommandline(ro.getData().get(0).getIsVsys(),ro.getData().get(0).getVsysName(),null,null));
                    commandLineSb.append(getAddressObjectCommandline(overAllGeneratorAbstractBean,deviceUuid,dto.getAddress()));
                    commandLineSb.append(getServiceObjectCommandline(overAllGeneratorAbstractBean,deviceUuid,dto.getService()));
                    commandLineSb.append(getPolicyObjectCommandline(overAllGeneratorAbstractBean, deviceUuid, dto.getPolicy()));
                    //后置命令行
                    commandLineSb.append(overAllGeneratorAbstractBean.generatePostCommandline(null,null));
                } catch (Exception e) {
                    log.error("生成策略命令行出错:",e);
                    allCommend.put(deviceUuid,"生成策略命令行出错");
                }

                allCommend.put(deviceUuid,commandLineSb.toString());
            }

        }
        return allCommend;
    }

    //将前端传入的多个修改内容，按照设备拆分为多个map
    public  Map<String,RetrievalParamDto> splitUiudToMap(RetrievalParamDto retrievalParamDto){
        Map<String,RetrievalParamDto> dtoToUuid = new HashMap<>();
        if(retrievalParamDto.getAddress()!=null && retrievalParamDto.getAddress().size()>0){
            for(RetrievalAddressDto addressDto : retrievalParamDto.getAddress()){
                if(dtoToUuid.get(addressDto.getDeviceUuid())==null){
                    RetrievalParamDto dto = new RetrievalParamDto();
                    dto.addAddressDto(addressDto);
                    dtoToUuid.put(addressDto.getDeviceUuid(),dto);
                }else{
                    dtoToUuid.get(addressDto.getDeviceUuid()).addAddressDto(addressDto);
                }
            }
        }
        if(retrievalParamDto.getService()!=null && retrievalParamDto.getService().size()>0){
            for(RetrievalServiceDto serviceDto : retrievalParamDto.getService()){
                if(dtoToUuid.get(serviceDto.getDeviceUuid())==null){
                    RetrievalParamDto dto = new RetrievalParamDto();
                    dto.addServiceDto(serviceDto);
                    dtoToUuid.put(serviceDto.getDeviceUuid(),dto);
                }else{
                    dtoToUuid.get(serviceDto.getDeviceUuid()).addServiceDto(serviceDto);
                }
            }
        }
        if(retrievalParamDto.getPolicy()!=null && retrievalParamDto.getPolicy().size()>0){
            for(RetrievalPolicyDto policyDto : retrievalParamDto.getPolicy()){
                if(dtoToUuid.get(policyDto.getDeviceUuid())==null){
                    RetrievalParamDto dto = new RetrievalParamDto();
                    dto.addPolicyDto(policyDto);
                    dtoToUuid.put(policyDto.getDeviceUuid(),dto);
                }else{
                    dtoToUuid.get(policyDto.getDeviceUuid()).addPolicyDto(policyDto);
                }
            }

        }
        return dtoToUuid;
    }

    //枚举判断 当前型号是否支持
    private String checkCommandline(DeviceModelNumberEnumExtended modelNumber){
        String commandLine = "命令行生成器暂不支持";
        if(modelNumber==null){
            return commandLine;
        }
        switch (modelNumber) {
            case USG6000:
            case USG6000_NO_TOP:
                return null;
            default:
                return commandLine;
        }
    }

    public String getAddressObjectCommandline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean,String uuid,List<RetrievalAddressDto> retrievalaAddressDto){

        //修改后的数据组装成 Map<String,List<String>>
        Map<String,List<String>> currentMap = new HashMap<>();
        if(retrievalaAddressDto != null){
            for(RetrievalAddressDto dto : retrievalaAddressDto){
                if(dto.getAddressName()==null || StringUtils.isBlank(dto.getAddressName())){
                    continue;
                }
                if(dto.getAddressContent()!=null){
                    String[] addressArray = dto.getAddressContent().split(";");
                    currentMap.put(dto.getAddressName(), Arrays.asList(addressArray));
                }else{
                    currentMap.put(dto.getAddressName(),new ArrayList<>());
                }
            }
        }
        // 根据对象和uuid  查询修改前的数据
        SearchAddressDTO searchAddressDTO = new SearchAddressDTO();
        searchAddressDTO.setDeviceUuid(uuid);
        ResultRO<List<NetWorkGroupObjectRO>> resultRO =  whaleDeviceObjectClient.searchAddressGlobalSearch(searchAddressDTO,null);
        if(resultRO==null || !resultRO.getSuccess()){
            return String.format("地址对象内容查询失败,失败UUID：%s",uuid);
        }
        //修改前的数据需要 组装为 Map<String,List<String>>
        Map<String,List<String>> originalMap = new HashMap<>();
        if(resultRO.getData()!=null){
            for(NetWorkGroupObjectRO ro : resultRO.getData()){
                String name = ro.getName() + "()";
                if(currentMap.containsKey(name)){
                    originalMap.put(name,new ArrayList<>());
                    if(ro.getIncludeItems() != null && ro.getIncludeItems().size()>0){
                        for(IncludeItemsRO item : ro.getIncludeItems()){
                            if(item.getType().equals("SUBNET")){
                                originalMap.get(name).add(item.getIp4Prefix() + "/" + item.getIp4Length());
                            }else if(item.getType().equals("HOST_IP")){
                                if(item.getIp4Addresses() != null){
                                    originalMap.get(name).addAll(item.getIp4Addresses());
                                }
                            }else if(item.getType().equals("RANG")){
                                Ip4RangeRO rang = item.getIp4Range();
                                originalMap.get(name).add(rang.getStart() + "-" + rang.getEnd());
                            }
                        }
                    }
                    if(ro.getIncludeItemNames() != null && ro.getIncludeItemNames().size()>0 ){
                        for(String itemName : ro.getIncludeItemNames() ){
                            originalMap.get(name).add(itemName + "()");
                        }
                    }
                    if(ro.getIncludeGroupNames() != null && ro.getIncludeGroupNames().size()>0 ){
                        for(String itemName : ro.getIncludeGroupNames() ){
                            originalMap.get(name).add(itemName + "()");
                        }
                    }
                }
            }
        }

        //调用木华的工具进行 差异对比
        Map<String, Map<String, List<String>>> compareObjectMap = ObjectCompareUtils.compareObjectMapList(originalMap,currentMap);

        //对比结果进行处理
        LinkedList<PolicyEditParamDTO> paramDTOList = dealWithAddressParam(compareObjectMap,currentMap,originalMap);



        //参数调用命令行生成器，生成最终命令行
        StringBuffer commandLine = new StringBuffer();
        for(PolicyEditParamDTO policyEditParamDTO : paramDTOList){
            try {
                commandLine.append(overAllGeneratorAbstractBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.MODIFY,RuleIPTypeEnum.IP4,policyEditParamDTO.getGroupName(),policyEditParamDTO.getId(),
                        policyEditParamDTO.getSrcIp().getSingleIpArray(),policyEditParamDTO.getSrcIp().getRangIpArray(),policyEditParamDTO.getSrcIp().getSubnetIntIpArray(),policyEditParamDTO.getSrcIp().getSubnetStrIpArray(),
                        null,policyEditParamDTO.getSrcIp().getHosts(),policyEditParamDTO.getSrcIp().getObjectNameRefArray(),policyEditParamDTO.getSrcIp().getObjectGroupNameRefArray(),null,
                        null,null,null,null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return commandLine.toString();
    }

    public String getServiceObjectCommandline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean,String uuid,List<RetrievalServiceDto> retrievalServiceDto){
        //修改后的数据组装成 Map<String,List<String>>
        Map<String,List<String>> currentMap = new HashMap<>();
        if(retrievalServiceDto != null){
            for(RetrievalServiceDto dto : retrievalServiceDto){
                if(dto.getServiceName()==null || StringUtils.isBlank(dto.getServiceName())){
                    continue;
                }
                if(dto.getServiceContent()!=null){
                    String[] serviceArray = dto.getServiceContent().split(";");
                    currentMap.put(dto.getServiceName(), Arrays.asList(serviceArray));
                }else{
                    currentMap.put(dto.getServiceName(),new ArrayList<>());
                }
            }
        }

        // 根据对象和uuid  查询修改前的数据
        SearchServiceDTO searchServiceDTO = new SearchServiceDTO();
        searchServiceDTO.setDeviceUuid(uuid);
        ResultRO<List<ServiceGroupObjectRO>> dataResultRO = whaleDeviceObjectClient.searchServiceForGlobalSearch(searchServiceDTO, null);

        if(dataResultRO==null || !dataResultRO.getSuccess()){
            return String.format("地址对象内容查询失败,失败UUID：%s",uuid);
        }
        //修改前的数据需要 组装为 Map<String,List<String>>
        Map<String,List<String>> originalMap = new HashMap<>();
        if(dataResultRO.getData()!=null){
            for(ServiceGroupObjectRO ro : dataResultRO.getData()){
                String name = ro.getName() + "()";
                if(currentMap.containsKey(name)){
                    originalMap.put(name,new ArrayList<>());
                    if(ro.getIncludeFilterServices() != null && ro.getIncludeFilterServices().size()>0){
                        for(IncludeFilterServicesRO item : ro.getIncludeFilterServices()){
                            String serviceContent = DeviceObjectContentUtil.packServiceContent(item);
                            if(serviceContent != null){
                                originalMap.get(name).add(serviceContent);
                            }
                        }
                    }
                    if(ro.getIncludeFilterServiceNames() !=null && ro.getIncludeFilterServiceNames().size()>0){
                        for(String serviceName : ro.getIncludeFilterServiceNames()){
                            originalMap.get(name).add(serviceName+ "()");
                        }
                    }
                    if(ro.getIncludeFilterServiceGroupNames() !=null && ro.getIncludeFilterServiceGroupNames().size()>0){
                        for(String serviceName : ro.getIncludeFilterServiceGroupNames()){
                            originalMap.get(name).add(serviceName+ "()");
                        }
                    }
                }
            }
        }

        //调用木华的工具进行 差异对比
        Map<String, Map<String, List<String>>> compareObjectMap = ObjectCompareUtils.compareObjectMapList(originalMap,currentMap);

        //对比结果进行处理
        LinkedList<PolicyEditParamDTO> paramDTOList = dealWithServiceParam(compareObjectMap,currentMap,originalMap);

        //参数调用命令行生成器，生成最终命令行
        StringBuffer commandLine = new StringBuffer();
        for(PolicyEditParamDTO policyEditParamDTO : paramDTOList){
            try {
                commandLine.append(overAllGeneratorAbstractBean.generateServiceObjectGroupCommandLine(StatusTypeEnum.MODIFY,policyEditParamDTO.getGroupName(),policyEditParamDTO.getId(),
                        null, policyEditParamDTO.getServiceParam() == null ?null : Arrays.asList(policyEditParamDTO.getServiceParam()), null,policyEditParamDTO.getRefServiceObject(),
                        policyEditParamDTO.getRefServiceObjectGroup(),null,null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return commandLine.toString();
    }

    public String getPolicyObjectCommandline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean,String uuid,List<RetrievalPolicyDto> retrievalPolicyDto){
        List<String> policyNameList = new ArrayList<>();
        //修改后的数据组装成 Map<String,List<String>>
        Map<String,List<String>> currentSrcMap = new HashMap<>();
        Map<String,List<String>> currentDstMap = new HashMap<>();
        Map<String,List<String>> currentServiceMap = new HashMap<>();
        if(retrievalPolicyDto != null){
            for(RetrievalPolicyDto dto : retrievalPolicyDto){
                if(dto.getPolicyName()==null || StringUtils.isBlank(dto.getPolicyName())){
                    continue;
                }
                policyNameList.add(dto.getPolicyName());
                if(dto.getSrcIp()!=null){
                    String[] srcAddressArray = dto.getSrcIp().split(";");
                    currentSrcMap.put(dto.getPolicyName(), Arrays.asList(srcAddressArray));
                }
                if(dto.getDstIp()!=null){
                    String[] dstAddressArray = dto.getDstIp().split(";");
                    currentDstMap.put(dto.getPolicyName(), Arrays.asList(dstAddressArray));
                }
                if(dto.getService()!=null){
                    String[] serviceAddressArray = dto.getService().split(";");
                    currentServiceMap.put(dto.getPolicyName(), Arrays.asList(serviceAddressArray));
                }
            }
        }

        // 根据对象和uuid  查询修改前的数据
        Map<String,List<String>> uuidmap = new HashMap<>();
        uuidmap.put("deviceUuids", Arrays.asList(new String[]{uuid}));
        ResultRO<List<FilterRuleInternalRO>> dataResultRO =  whaleDevicePolicyClient.getFilterRuleListByUuid(uuidmap);

        if(dataResultRO==null || !dataResultRO.getSuccess()){
            return String.format("策略信息查询失败,失败UUID：%s",uuid);
        }
        //修改前的数据需要 组装为 Map<String,List<String>>
        Map<String,List<String>> originalSrcMap = new HashMap<>();
        Map<String,List<String>> originalDstMap = new HashMap<>();
        Map<String,List<String>> originalServiceMap = new HashMap<>();
        if(dataResultRO.getData()!=null){
            for(FilterRuleInternalRO ruleInternalRO : dataResultRO.getData()){
                String policyName = ruleInternalRO.getName();
                if(currentSrcMap.containsKey(policyName)){
                    originalSrcMap.put(policyName,new ArrayList<>());
                    if(ruleInternalRO.getSrcIp()!=null){
                        for(ObjectInternalRO ro :ruleInternalRO.getSrcIp()){
                            originalSrcMap.get(policyName).add(ro.getName() + "()");
                        }
                    }
                }
                if(currentDstMap.containsKey(policyName)){
                    originalDstMap.put(policyName,new ArrayList<>());
                    if(ruleInternalRO.getDstIp()!=null){
                        for(ObjectInternalRO ro : ruleInternalRO.getDstIp()){
                            originalDstMap.get(policyName).add(ro.getName() + "()");
                        }
                    }
                }
                if(currentServiceMap.containsKey(policyName)){
                    originalServiceMap.put(policyName,new ArrayList<>());
                    if(ruleInternalRO.getService()!=null) {
                        for (ObjectInternalRO ro : ruleInternalRO.getService()) {
                            originalServiceMap.get(policyName).add(ro.getName() + "()");
                        }
                    }
                }
            }
        }
        //调用工具进行 差异对比
        Map<String, Map<String, List<String>>> compareObjectSrcMap = ObjectCompareUtils.compareObjectMapList(originalSrcMap,currentSrcMap);
        Map<String, Map<String, List<String>>> compareObjectDstMap = ObjectCompareUtils.compareObjectMapList(originalDstMap,currentDstMap);
        Map<String, Map<String, List<String>>> compareObjectServiceMap = ObjectCompareUtils.compareObjectMapList(originalServiceMap,currentServiceMap);

        List<PolicyEditParamDTO> paramDTOList = new ArrayList<>();
        for(String policyname : policyNameList){
            if(compareObjectSrcMap != null && compareObjectSrcMap.size()>0){
                PolicyEditParamDTO paramDTO = new PolicyEditParamDTO();
                paramDTO.setName(policyname);
                paramDTO.setEditTypeEnums(EditTypeEnums.SRC_ADDRESS);
                if(compareObjectSrcMap.get("add") != null && compareObjectSrcMap.get("add").get(policyname) != null){
                    if(compareObjectSrcMap.get("del") == null || compareObjectSrcMap.get("del").get(policyname) == null){
                        List<String> value = originalSrcMap.get(policyname);
                        List<String> addValue = compareObjectSrcMap.get("add").get(policyname);
                        if(value != null){
                            for(String val : value){
                                if(paramDTO.getSrcRefIpAddressObject() == null){
                                    paramDTO.setSrcRefIpAddressObject(new String[]{getObjectname(val)});

                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getSrcRefIpAddressObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setSrcRefIpAddressObject(dtoLists.toArray(param));
                                }
                            }
                        }
                        if(addValue != null){
                            for(String val : addValue){
                                if(paramDTO.getSrcRefIpAddressObject() == null){
                                    paramDTO.setSrcRefIpAddressObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getSrcRefIpAddressObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setSrcRefIpAddressObject(dtoLists.toArray(param));
                                }
                            }
                        }
                    }
                }
                if(compareObjectSrcMap.get("del") != null && compareObjectSrcMap.get("del").get(policyname) != null){
                    List<String> delList = compareObjectSrcMap.get("del").get(policyname);
                    if(compareObjectSrcMap.get("add") != null && compareObjectSrcMap.get("add").get(policyname) != null){
                        if(originalSrcMap.get(policyname) != null){
                            originalSrcMap.get(policyname).addAll(compareObjectSrcMap.get("add").get(policyname));
                        }else{
                            originalSrcMap.put(policyname,compareObjectSrcMap.get("add").get(policyname));
                        }
                    }
                    if(originalSrcMap.get(policyname) != null){
                        for(String val : originalSrcMap.get(policyname)){
                            if(!delList.contains(val)){
                                if(paramDTO.getSrcRefIpAddressObject() == null){
                                    paramDTO.setSrcRefIpAddressObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getSrcRefIpAddressObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setSrcRefIpAddressObject(dtoLists.toArray(param));
                                }
                            }
                        }
                    }
                }
                paramDTOList.add(paramDTO);
            }
            if(compareObjectDstMap != null && compareObjectDstMap.size()>0){
                PolicyEditParamDTO paramDTO = new PolicyEditParamDTO();
                paramDTO.setName(policyname);
                paramDTO.setEditTypeEnums(EditTypeEnums.DST_ADDRESS);
                if(compareObjectDstMap.get("add") != null && compareObjectDstMap.get("add").get(policyname) != null){
                    if(compareObjectDstMap.get("del") == null || compareObjectDstMap.get("del").get(policyname) == null){
                        List<String> value = originalDstMap.get(policyname);
                        List<String> addValue = compareObjectDstMap.get("add").get(policyname);
                        if(value != null){
                            for(String val : value){
                                if(paramDTO.getDstRefIpAddressObject() == null){
                                    paramDTO.setDstRefIpAddressObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getDstRefIpAddressObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setDstRefIpAddressObject(dtoLists.toArray(param));
                                }
                            }
                        }
                        if(addValue != null){
                            for(String val : addValue){
                                if(paramDTO.getDstRefIpAddressObject() == null){
                                    paramDTO.setDstRefIpAddressObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getDstRefIpAddressObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setDstRefIpAddressObject(dtoLists.toArray(param));
                                }
                            }
                        }
                    }
                }
                if(compareObjectDstMap.get("del") != null && compareObjectDstMap.get("del").get(policyname) != null){
                    List<String> delList = compareObjectDstMap.get("del").get(policyname);
                    if(compareObjectDstMap.get("add") != null && compareObjectDstMap.get("add").get(policyname) != null){
                        if(originalDstMap.get(policyname) != null){
                            originalDstMap.get(policyname).addAll(compareObjectDstMap.get("add").get(policyname));
                        }else{
                            originalDstMap.put(policyname,compareObjectDstMap.get("add").get(policyname));
                        }
                    }
                    if(originalDstMap.get(policyname) != null){
                        for(String val : originalDstMap.get(policyname)){
                            if(!delList.contains(val)){
                                if(paramDTO.getDstRefIpAddressObject() == null){
                                    paramDTO.setDstRefIpAddressObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getDstRefIpAddressObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setDstRefIpAddressObject(dtoLists.toArray(param));
                                }
                            }
                        }
                    }
                }
                paramDTOList.add(paramDTO);
            }
            if(compareObjectServiceMap != null && compareObjectServiceMap.size()>0){
                PolicyEditParamDTO paramDTO = new PolicyEditParamDTO();
                paramDTO.setName(policyname);
                paramDTO.setEditTypeEnums(EditTypeEnums.SERVICE);
                if(compareObjectServiceMap.get("add") != null && compareObjectServiceMap.get("add").get(policyname) != null){
                    if(compareObjectServiceMap.get("del") == null || compareObjectServiceMap.get("del").get(policyname) == null){
                        List<String> value = originalServiceMap.get(policyname);
                        List<String> addValue = compareObjectServiceMap.get("add").get(policyname);
                        if(value != null){
                            for(String val : value){
                                if(paramDTO.getRefServiceObject() == null){
                                    paramDTO.setRefServiceObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getRefServiceObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setRefServiceObject(dtoLists.toArray(param));
                                }
                            }
                        }
                        if(addValue != null){
                            for(String val : addValue){
                                if(paramDTO.getRefServiceObject() == null){
                                    paramDTO.setRefServiceObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getRefServiceObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setRefServiceObject(dtoLists.toArray(param));
                                }
                            }
                        }
                    }
                }
                if(compareObjectServiceMap.get("del") != null && compareObjectServiceMap.get("del").get(policyname) != null){
                    List<String> delList = compareObjectServiceMap.get("del").get(policyname);
                    if(compareObjectServiceMap.get("add") != null && compareObjectServiceMap.get("add").get(policyname) != null){
                        if(originalServiceMap.get(policyname) != null){
                            originalServiceMap.get(policyname).addAll(compareObjectServiceMap.get("add").get(policyname));
                        }else{
                            originalServiceMap.put(policyname,compareObjectServiceMap.get("add").get(policyname));
                        }
                    }
                    if(originalServiceMap.get(policyname) != null){
                        for(String val : originalServiceMap.get(policyname)){
                            if(!delList.contains(val)){
                                if(paramDTO.getRefServiceObject() == null){
                                    paramDTO.setRefServiceObject(new String[]{getObjectname(val)});
                                }else{
                                    List<String> dtoList = Arrays.asList(paramDTO.getRefServiceObject());
                                    List<String> dtoLists = new ArrayList<>(dtoList);
                                    dtoLists.add(getObjectname(val));
                                    String[] param = new String[dtoLists.size()];
                                    paramDTO.setRefServiceObject(dtoLists.toArray(param));
                                }
                            }
                        }
                    }
                }
                paramDTOList.add(paramDTO);
            }
        }
        //参数调用命令行生成器，生成最终命令行
        StringBuffer commandLine = new StringBuffer();
        for(PolicyEditParamDTO policyEditParamDTO : paramDTOList){
            try {
                commandLine.append(overAllGeneratorAbstractBean.generateSecurityPolicyModifyCommandLine(StatusTypeEnum.MODIFY,policyEditParamDTO,null,null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return commandLine.toString();
    }

    public LinkedList<PolicyEditParamDTO> dealWithAddressParam(Map<String, Map<String, List<String>>> compareObjectMap,Map<String,List<String>> currentMap,Map<String,List<String>> originalMap){
        LinkedList<PolicyEditParamDTO> paramDTOList = new LinkedList<>();
        if(compareObjectMap!=null){
            //有新增对象需要进行排序和参数组装
            if(compareObjectMap.get("add") != null){
                //组装参数
                Map<String ,List<String>> addMap = compareObjectMap.get("add");
                //对比结果需要进行排序，排序方法
                LinkedHashMap<String,List<String>> addMapSort = sortMap(addMap);

                for(Map.Entry<String,List<String>> paramMap : addMapSort.entrySet()){
                    String key = paramMap.getKey();
                    if(compareObjectMap.get("del") == null || compareObjectMap.get("del").get(key) == null){
                        PolicyEditParamDTO addressEditParamDTO = new PolicyEditParamDTO();
                        addressEditParamDTO.setGroupName(getObjectname(key));
                        IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
                        List<String> value = paramMap.getValue();
                        if(value != null){
                            for(String val : value){
                                dealWithAddress(ipAddressParamDTO,val);
                            }
                        }
                        List<String> originalValue = originalMap.get(key);
                        if(originalValue != null){
                            for(String val : originalValue){
                                dealWithAddress(ipAddressParamDTO,val);
                            }
                        }
                        addressEditParamDTO.setSrcIp(ipAddressParamDTO);
                        paramDTOList.add(addressEditParamDTO);
                    }
                }
            }
            //参数组装
            if(compareObjectMap.get("del") != null){
                Map<String ,List<String>> delMap = compareObjectMap.get("del");
                for(Map.Entry<String,List<String>> paramMap : delMap.entrySet()){
                    PolicyEditParamDTO addressEditParamDTO = new PolicyEditParamDTO();
                    String key = paramMap.getKey();
                    addressEditParamDTO.setGroupName(getObjectname(key));

                    IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
                    List<String> value = paramMap.getValue();
                    List<String> originalValue = originalMap.get(key);
                    //当前删除之前 存在新增 则将新增的放到原始Map中
                    if(compareObjectMap.get("add") != null && compareObjectMap.get("add").get(key) !=null){
                        originalValue.addAll(compareObjectMap.get("add").get(key));
                    }
                    // 对原始Map 进行命令行生成参数新增。val在删除中则不生成
                    for(String val : originalValue){
                        if(!value.contains(val)){
                            dealWithAddress(ipAddressParamDTO,val);
                        }
                    }
                    addressEditParamDTO.setSrcIp(ipAddressParamDTO);
                    paramDTOList.add(addressEditParamDTO);
                }
            }
        }
        return paramDTOList;
    }

    public LinkedList<PolicyEditParamDTO> dealWithServiceParam(Map<String, Map<String, List<String>>> compareObjectMap,Map<String,List<String>> currentMap,Map<String,List<String>> originalMap){
        LinkedList<PolicyEditParamDTO> paramDTOList = new LinkedList<>();
        if(compareObjectMap!=null){
            //有新增对象需要进行排序和参数组装
            if(compareObjectMap.get("add") != null) {
                //组装参数
                Map<String, List<String>> addMap = compareObjectMap.get("add");
                //对比结果需要进行排序，排序方法
                LinkedHashMap<String,List<String>> addMapSort = sortMap(addMap);

                for (Map.Entry<String, List<String>> paramMap : addMapSort.entrySet()) {
                    PolicyEditParamDTO serviceEditParamDTO = new PolicyEditParamDTO();
                    String key = paramMap.getKey();
                    serviceEditParamDTO.setGroupName(getObjectname(key));

                    List<String> value = originalMap.get(key);
                    List<String> addService = paramMap.getValue();

                    if(compareObjectMap.get("del") == null || compareObjectMap.get("del").get(key)==null){
                        if(value != null){
                            for(String val : value){
                                dealWithService(serviceEditParamDTO,val);
                            }
                        }
                        if(addService != null){
                            for(String val : addService){
                                dealWithService(serviceEditParamDTO,val);
                            }
                        }
                        paramDTOList.add(serviceEditParamDTO);
                    }
                }
            }
            //参数组装
            if(compareObjectMap.get("del") != null) {
                Map<String, List<String>> delMap = compareObjectMap.get("del");
                for(Map.Entry<String,List<String>> paramMap : delMap.entrySet()){
                    PolicyEditParamDTO serviceEditParamDTO = new PolicyEditParamDTO();
                    String key = paramMap.getKey();
                    serviceEditParamDTO.setGroupName(key);

                    List<String> value = paramMap.getValue();
                    List<String> originalValue = originalMap.get(key);
                    //当前删除之前 存在新增 则将新增的放到原始Map中
                    if(compareObjectMap.get("add") != null && compareObjectMap.get("add").get(key) !=null){
                        originalValue.addAll(compareObjectMap.get("add").get(key));
                    }
                    // 对原始Map 进行命令行生成参数新增。val在删除中则不生成
                    for(String val : originalValue){
                        if(!value.contains(val)){
                            dealWithService(serviceEditParamDTO,val);
                        }
                    }
                    paramDTOList.add(serviceEditParamDTO);
                }
            }

        }
        return paramDTOList;
    }

    public void dealWithAddress(IpAddressParamDTO ipAddressParamDTO,String val){
        //对象
        if(isObject(val)){
            String name = getObjectname(val);
            if(name != null){
                if(ipAddressParamDTO.getObjectNameRefArray() == null){
                    String[] objectName={ name };
                    ipAddressParamDTO.setObjectNameRefArray(objectName);
                }else{
                    String[] nameArray = ipAddressParamDTO.getObjectNameRefArray();
                    List<String> dtoList = Arrays.asList(nameArray);
                    List<String> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(name);
                    String[] newNameArray = new String[dtoLists.size()];
                    ipAddressParamDTO.setObjectNameRefArray(dtoLists.toArray(newNameArray));
                }
            }
        }else{
        //内容
            if(IPUtil.isIPRange(val)){
                IpAddressRangeDTO rangeDto = new IpAddressRangeDTO();
                String startIp = IPUtil.getStartIpFromRange(val);
                String endIp = IPUtil.getEndIpFromRange(val);
                rangeDto.setStart(startIp);
                rangeDto.setEnd(endIp);
                //是否已存在
                if(ipAddressParamDTO.getRangIpArray()!=null){
                    IpAddressRangeDTO[] rangeIpArray = ipAddressParamDTO.getRangIpArray();
                    List<IpAddressRangeDTO> dtoList = Arrays.asList(rangeIpArray);
                    List<IpAddressRangeDTO> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(rangeDto);
                    IpAddressRangeDTO[] newRangeIpArray = new IpAddressRangeDTO[dtoLists.size()];
                    ipAddressParamDTO.setRangIpArray(dtoLists.toArray(newRangeIpArray));
                }else{
                    IpAddressRangeDTO[] rangeIpArray={ rangeDto };
                    ipAddressParamDTO.setRangIpArray(rangeIpArray);
                }
            } else if (IPUtil.isIPSegment(val)) {
                IpAddressSubnetStrDTO subnetStrDto = new IpAddressSubnetStrDTO();
                String ipAddress = val.split("/")[0];
                String mask = val.split("/")[1];
                String maskIp = TotemsIp4Utils.getMaskIpByMask(mask);
                subnetStrDto.setIp(ipAddress);
                subnetStrDto.setMask(maskIp);
                //是否已存在
                if(ipAddressParamDTO.getSubnetStrIpArray() != null){
                    IpAddressSubnetStrDTO[] subnetIpArray = ipAddressParamDTO.getSubnetStrIpArray();
                    List<IpAddressSubnetStrDTO> dtoList = Arrays.asList(subnetIpArray);
                    List<IpAddressSubnetStrDTO> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(subnetStrDto);
                    IpAddressSubnetStrDTO[] newRangeIpArray = new IpAddressSubnetStrDTO[dtoLists.size()];
                    ipAddressParamDTO.setSubnetStrIpArray(dtoLists.toArray(newRangeIpArray));
                } else {
                    IpAddressSubnetStrDTO[] subnetStrIpArray={subnetStrDto};
                    ipAddressParamDTO.setSubnetStrIpArray(subnetStrIpArray);
                }
            } else if (IPUtil.isIP(val)) {
                if(ipAddressParamDTO.getSingleIpArray() != null){
                    String[] ipArray = ipAddressParamDTO.getSingleIpArray();
                    List<String> dtoList = Arrays.asList(ipArray);
                    List<String> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(val);
                    String[] newIpArray = new String[dtoLists.size()];
                    ipAddressParamDTO.setSingleIpArray(dtoLists.toArray(newIpArray));
                }else{
                    String[] singleIpArray={ val };
                    ipAddressParamDTO.setSingleIpArray(singleIpArray);
                }
            } else{
                if(ipAddressParamDTO.getHosts() != null){
                    String[] hostArray = ipAddressParamDTO.getHosts();
                    List<String> dtoList = Arrays.asList(hostArray);
                    List<String> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(val);
                    String[] newHostArray = new String[dtoLists.size()];
                    ipAddressParamDTO.setHosts(dtoLists.toArray(newHostArray));
                }else{
                    String[] hostArray={ val };
                    ipAddressParamDTO.setHosts(hostArray);
                }
            }
        }
    }

    public void dealWithService(PolicyEditParamDTO serviceEditParamDTO,String val){
        //对象
        if(isObject(val)){
            String name = getObjectname(val);
            if(name != null){
                if(serviceEditParamDTO.getRefServiceObject() == null){
                    String[] objectName={ name };
                    serviceEditParamDTO.setRefServiceObject(objectName);
                }else{
                    String[] nameArray = serviceEditParamDTO.getRefServiceObject();
                    List<String> dtoList = Arrays.asList(nameArray);
                    List<String> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(name);
                    String[] newNameArray = new String[dtoLists.size()];
                    serviceEditParamDTO.setRefServiceObject(dtoLists.toArray(newNameArray));
                }
            }
        }else{
            if(StringUtils.isNotBlank(val)){
                ServiceParamDTO paramDTO = null;
                if(val.indexOf("源端口:RANGE:")>-1 && val.indexOf("目的端口:RANGE")>-1) {
                    String name = val.substring(0, val.indexOf(":源端口:RANGE:")).trim();
                    String start = val.substring(val.indexOf(":源端口:RANGE:") + 11, val.indexOf("目的端口:RANGE:")).trim();
                    String end = val.substring(val.indexOf("目的端口:RANGE:") + 11).trim();

                    ProtocolTypeEnum type = ProtocolTypeEnum.getByType(name);
                    TwoMemberObject<Integer,Integer> srcPort = getRangPort(start);
                    TwoMemberObject<Integer,Integer> dstPort = getRangPort(end);

                    if(type != null && srcPort != null && dstPort != null){
                        paramDTO = new ServiceParamDTO();
                        paramDTO.setProtocol(type);
                        paramDTO.setSrcRangePortArray(new PortRangeDTO[]{new PortRangeDTO(srcPort.getFirstValue(),srcPort.getSecondValue())});
                        paramDTO.setDstRangePortArray(new PortRangeDTO[]{new PortRangeDTO(dstPort.getFirstValue(),dstPort.getSecondValue())});
                    }
                }else if(val.indexOf("源端口:RANGE:")>-1 && val.indexOf("目的端口:EQ:")>-1) {
                    String name = val.substring(0, val.indexOf(":源端口:RANGE:")).trim();
                    String start = val.substring(val.indexOf(":源端口:RANGE:") + 11, val.indexOf("目的端口:EQ:")).trim();
                    String end = val.substring(val.indexOf("目的端口:EQ:") + 8).trim();

                    ProtocolTypeEnum type = ProtocolTypeEnum.getByType(name);
                    TwoMemberObject<Integer, Integer> srcPort = getRangPort(start);
                    Integer dstPort = getSinglePort(end);

                    if (type != null && srcPort != null && dstPort != null) {
                        paramDTO = new ServiceParamDTO();
                        paramDTO.setProtocol(type);
                        paramDTO.setSrcRangePortArray(new PortRangeDTO[]{new PortRangeDTO(srcPort.getFirstValue(), srcPort.getSecondValue())});
                        paramDTO.setDstSinglePortArray(new Integer[]{dstPort});
                    }
                }else if(val.indexOf(":目的端口:RANGE:")>-1){
                    String name = val.substring(0, val.indexOf(":目的端口:RANGE:")).trim();
                    String port = val.substring(val.indexOf(":目的端口:RANGE:") + 9).trim();

                    ProtocolTypeEnum type = ProtocolTypeEnum.getByType(name);
                    TwoMemberObject<Integer, Integer> dstPort = getRangPort(port);
                    if (type != null && dstPort != null) {
                        paramDTO = new ServiceParamDTO();
                        paramDTO.setProtocol(type);
                        paramDTO.setDstRangePortArray(new PortRangeDTO[]{new PortRangeDTO(dstPort.getFirstValue(),dstPort.getSecondValue())});
                    }
                }else if(val.indexOf(":目的端口:EQ:")>-1) {
                    String name = val.substring(0, val.indexOf(":目的端口:EQ:")).trim();
                    String port = val.substring(val.indexOf(":目的端口:EQ:") + 9).trim();

                    ProtocolTypeEnum type = ProtocolTypeEnum.getByType(name);
                    Integer dstPort = getSinglePort(port);

                    if (type != null && dstPort != null) {
                        paramDTO = new ServiceParamDTO();
                        paramDTO.setProtocol(type);
                        paramDTO.setDstSinglePortArray(new Integer[]{dstPort});
                    }
                }else if(val.indexOf("ICMP") == 0){
                    ProtocolTypeEnum type = ProtocolTypeEnum.ICMP;
                    paramDTO = new ServiceParamDTO();
                    paramDTO.setProtocol(type);
    //            }else if(val.indexOf("类型:")>-1 && val.indexOf("协议范围:")>-1){
    //                String name = val.substring(0,val.indexOf(":类型:")).trim();
    //                String type = val.substring(val.indexOf(":类型:") + 4,val.indexOf("协议范围:")).trim();
    //                String rang = val.substring(val.indexOf("协议范围:") + 5).trim();
    //
    //                System.out.println(" ---------   " + name +"  "+ type +"  "+ rang);
    //            }else if(val.indexOf("类型:")>-1 && val.indexOf("编码:")>-1){
    //                String name = val.substring(0,val.indexOf(":类型:")).trim();
    //                String type = val.substring(val.indexOf(":类型:") + 4,val.indexOf("编码:")).trim();
    //                String code = val.substring(val.indexOf("编码:") + 3).trim();
    //
    //                System.out.println(" ---------  " + name +"  "+ type +"  "+ code);
                }else if(val.indexOf("协议号:")>-1){
                    String port = val.substring(val.indexOf("协议号:") + 4);
                    System.out.println(" ---------  " + port);
                }

                if(paramDTO != null){
                    if(serviceEditParamDTO.getServiceParam()==null){
                        ServiceParamDTO[] params = { paramDTO };
                        serviceEditParamDTO.setServiceParam(params);
                    }else{
                        ServiceParamDTO[] paramArray = serviceEditParamDTO.getServiceParam();
                        List<ServiceParamDTO> paramDTOList = Arrays.asList(paramArray);
                        List<ServiceParamDTO> dtoLists = new ArrayList<>(paramDTOList);
                        dtoLists.add(paramDTO);
                        ServiceParamDTO[] param = new ServiceParamDTO[dtoLists.size()];
                        serviceEditParamDTO.setServiceParam(dtoLists.toArray(param));
                    }
                }else{
//                    paramDTO = new ServiceParamDTO();

                    //未匹配到对应服务
                }

            }
        }

    }

    public TwoMemberObject getRangPort(String value){
        if(value !=null ){
            try{
                String[] splitValue = value.trim().split("-");
                Integer start = Integer.valueOf(splitValue[0]);
                Integer end = Integer.valueOf(splitValue[1]);
                TwoMemberObject result = new TwoMemberObject(start,end);
                return result;
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

    public Integer getSinglePort(String value){
        if(value !=null ){
            try{
                Integer port = Integer.valueOf(value.trim());
                return port;
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

    public boolean isObject(String name){
        if(name == null){
            return false;
        }
        if(name.length()>2 && name.substring(name.length()-2).equals("()")){
            return true;
        }
        return false;
    }

    public String getObjectname(String name){
        if(name == null){
            return null;
        }
        if(name.length()>2 && name.substring(name.length()-2).equals("()")){
            return name.substring(0,name.length()-2);
        }
        return name;
    }

    public LinkedHashMap<String,List<String>> sortMap(Map<String,List<String>> map){
        LinkedHashMap<String,List<String>> newMap = new LinkedHashMap<>();
        int size = map.size();
        if(map == null){
            return null;
        }

        while (newMap.size()<size){
            for(Map.Entry<String,List<String>> paramMap : map.entrySet()){
                String key = paramMap.getKey();
                List<String> value = paramMap.getValue();
                if(!newMap.containsKey(key)){
                    boolean isLeaf = true;
                    for(String val : value){
                        if(map.containsKey(val) && !newMap.containsKey(val)){
                            isLeaf = false;
                        }
                    }
                    if(isLeaf){
                        newMap.put(key,value);
                    }
                }
            }
        }
        return newMap;
    }
}
