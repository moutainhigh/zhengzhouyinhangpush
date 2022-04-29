package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class EditPolicyGenerator {

    public abstract String generateCommandline(EditCommandlineDTO dto);

    public final String composite(EditCommandlineDTO dto){
        StringBuilder sb = new StringBuilder();

        String cmd = generateCommandline(dto);
        sb.append(cmd);

        return sb.toString();
    }

    /**
     * 创建源目服务都有的编辑实体类
     * @param cmdDTO
     * @return
     */
    public EditCommandlineDTO createAllEditDTO(CmdDTO cmdDTO){
        EditCommandlineDTO dto = new EditCommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
        } else {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
        }
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());

        dto.setBusinessName(policyDTO.getEditPolicyName());
        dto.setName(policyDTO.getEditPolicyName());

        dto.setCiscoInterfaceCreate(settingDTO.isCreateCiscoItfRuleList());
        dto.setCiscoInterfacePolicyName(settingDTO.getCiscoItfRuleListName());
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setOutBound(settingDTO.isOutBound());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        if(deviceDTO.getModelNumber().equals(DeviceModelNumberEnum.CISCO_ASA_86)){
            dto.setSpecialExistObject(cmdDTO.getSpecialExistObject());
        }
        dto.setStartTime(null);
        dto.setIdleTimeout(null);
        dto.setCiscoInterfaceCreate(false);
        log.info("编辑策略构建完毕 dto is" + JSONObject.toJSONString(dto, false));
        return dto;
    }

    /**
     * 根据需合并数据创建编辑实体类
     * @param cmdDTO
     * @return
     */
    public EditCommandlineDTO createPartEditDTO(CmdDTO cmdDTO){
        EditCommandlineDTO dto = new EditCommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        dto.setIpType(policyDTO.getIpType());
        Integer mergeProperty = policyDTO.getMergeProperty();
        //获取策略名
        if (!AliStringUtils.isEmpty(policyDTO.getEditPolicyName())) {
            dto.setBusinessName(policyDTO.getEditPolicyName());
            dto.setName(policyDTO.getEditPolicyName());
            dto.setCurrentId(policyDTO.getEditPolicyName());
        }
        //获取原策略数据
        dto.setSecurityPolicy(policyDTO.getSecurityPolicy());
        //获取注释
        dto.setDescription(policyDTO.getDescription());
        //获取已存在和还需创建数据
        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        if (mergeProperty!=3) {
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol("0");
            serviceDTO.setDstPorts("any");
            serviceDTO.setSrcPorts("any");
            if (mergeProperty == 0) {
                dto.setSrcIpSystem(policyDTO.getSrcIpSystem());
                dto.setSrcIp(policyDTO.getSrcIp());
                dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
                dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
                List<ServiceDTO> serviceDTOList = new ArrayList<>();
                serviceDTOList.add(serviceDTO);
                dto.setServiceList(serviceDTOList);
                dto.setRestServiceList(serviceDTOList);
            }
            if (mergeProperty == 1) {
                dto.setDstIpSystem(policyDTO.getDstIpSystem());
                dto.setDstIp(policyDTO.getDstIp());
                dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
                dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
                List<ServiceDTO> serviceDTOList = new ArrayList<>();
                serviceDTOList.add(serviceDTO);
                dto.setServiceList(serviceDTOList);
                dto.setRestServiceList(serviceDTOList);
            }
            if (mergeProperty == 2) {
                dto.setServiceList(policyDTO.getServiceList());
                dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
                dto.setRestServiceList(existObjectDTO.getRestServiceList());
            }
            DeviceDTO device = cmdDTO.getDevice();
            dto.setModelNumber(device.getModelNumber());
            //入接口出接口
            String srcZone = policyDTO.getSrcZone();
            String dstZone = policyDTO.getDstZone();
            dto.setSrcZone(srcZone);
            dto.setDstZone(dstZone);
            if(DeviceModelNumberEnum.USG6000.equals(device.getModelNumber())){
                dto.setSrcZone("");
                dto.setDstZone("");
            }
            if (device.isVsys()) {
                dto.setVsys(true);
                dto.setVsysName(device.getVsysName());
            }
            dto.setHasVsys(device.isHasVsys());
            if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
                dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
            } else {
                dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
            }

            SettingDTO setting = cmdDTO.getSetting();
            dto.setMoveSeatEnum(setting.getMoveSeatEnum());
            dto.setCreateObjFlag(setting.isCreateObject());
            dto.setMergeProperty(policyDTO.getMergeProperty());
            dto.setStartTime(null);
            dto.setIdleTimeout(null);
            dto.setCiscoInterfaceCreate(false);
            //飞塔,天融信需要获取需要合并的策略对应值，不然会覆盖原策略
            if(DeviceModelNumberEnum.FORTINET.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.FORTINET_V5_2.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.TOPSEC_TOS_005.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.TOPSEC_TOS_010_020.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.TOPSEC_NG.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.TOPSEC_NG2.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.TOPSEC_NG3.equals(device.getModelNumber())
                    ||DeviceModelNumberEnum.TOPSEC_NG4.equals(device.getModelNumber())){
                List<String> specialVale = getSpecialVale(policyDTO.getSecurityPolicy(), policyDTO.getMergeProperty());
                switch (mergeProperty){
                    case 0:dto.getExistSrcAddressList().addAll(specialVale);break;
                    case 1:dto.getExistDstAddressList().addAll(specialVale);break;
                    case 2:dto.getExistServiceNameList().addAll(specialVale);
                }
                dto.setCurrentId(policyDTO.getSecurityPolicy().getRuleId());
            }
        } else {
            log.error("无需合并的值");
            return null;
        }
        return dto;
    }

    private List<String> getSpecialVale(PolicyRecommendSecurityPolicyDTO securityPolicy,Integer mergeProperty){
        //飞塔需要获取合并前的值
        List<String> exist = new ArrayList<>();
        String theKey = "";
        if(ObjectUtils.isNotEmpty(securityPolicy)){
            JSONObject matchClause = securityPolicy.getMatchClause();
            switch (mergeProperty){
                case 0:theKey = "srcIp";break;
                case 1:theKey = "dstIp";break;
                case 2:theKey = "services";
            }
            String srcIpString = matchClause.getString(theKey);
            JSONArray srcArray = JSON.parseArray(srcIpString);
            for(int i = 0;i<srcArray.size();i++){
                JSONObject srcObject = srcArray.getJSONObject(i);
                String nameRef = srcObject.getString("nameRef");
                exist.add(nameRef);
                log.info("合并时获取到原策略属性为：{},name:{}",theKey,nameRef);
            }
        }
        return exist;
    }
}
