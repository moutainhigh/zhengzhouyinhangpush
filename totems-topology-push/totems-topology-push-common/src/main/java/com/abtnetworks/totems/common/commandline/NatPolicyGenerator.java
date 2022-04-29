package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.commandline.nat.H3cSecPathV7;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public interface NatPolicyGenerator {

    String DO_NOT_SUPPORT = "暂不支持生成命令行！";

    default String generate(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        switch(policyDTO.getType()) {
            case STATIC:
                StaticNatTaskDTO staticNatTaskDTO = getStaticNatDTO(cmdDTO);
                String staticNatCommandLine = generateStaticNatCommandLine(staticNatTaskDTO);
                cmdDTO.getGeneratedObject().setAddressObjectNameList(staticNatTaskDTO.getAddressObjectNameList());
                cmdDTO.getGeneratedObject().setAddressObjectGroupNameList(staticNatTaskDTO.getAddressObjectGroupNameList());
                cmdDTO.getGeneratedObject().setServiceObjectNameList(staticNatTaskDTO.getServiceObjectNameList());
                cmdDTO.getGeneratedObject().setServiceObjectGroupNameList(staticNatTaskDTO.getServiceObjectGroupNameList());
                cmdDTO.getGeneratedObject().setRollbackCommandLine(staticNatTaskDTO.getRollbackCommandLine());
                cmdDTO.getGeneratedObject().setPolicyName(staticNatTaskDTO.getPolicyName());
                return staticNatCommandLine;
            case SNAT:
                SNatPolicyDTO sNatPolicyDTO = getSNatPolicyDTO(cmdDTO);
                String sNatCommandLine = generateSNatCommandLine(sNatPolicyDTO);
                cmdDTO.getGeneratedObject().setAddressObjectNameList(sNatPolicyDTO.getAddressObjectNameList());
                cmdDTO.getGeneratedObject().setAddressObjectGroupNameList(sNatPolicyDTO.getAddressObjectGroupNameList());
                cmdDTO.getGeneratedObject().setServiceObjectNameList(sNatPolicyDTO.getServiceObjectNameList());
                cmdDTO.getGeneratedObject().setServiceObjectGroupNameList(sNatPolicyDTO.getServiceObjectGroupNameList());
                cmdDTO.getGeneratedObject().setPolicyName(sNatPolicyDTO.getPolicyName());
                cmdDTO.getGeneratedObject().setRollbackShowCmd(sNatPolicyDTO.getRollbackShowCmd());
                cmdDTO.getGeneratedObject().setAddressTypeMap(sNatPolicyDTO.getAddressTypeMap());
                return sNatCommandLine;
            case DNAT:
                DNatPolicyDTO dNatPolicyDTO = getDNatPolicyDTO(cmdDTO);
                String dNatCommandLine = generateDNatCommandLine(dNatPolicyDTO);
                cmdDTO.getGeneratedObject().setAddressObjectNameList(dNatPolicyDTO.getAddressObjectNameList());
                cmdDTO.getGeneratedObject().setAddressObjectGroupNameList(dNatPolicyDTO.getAddressObjectGroupNameList());
                cmdDTO.getGeneratedObject().setServiceObjectNameList(dNatPolicyDTO.getServiceObjectNameList());
                cmdDTO.getGeneratedObject().setServiceObjectGroupNameList(dNatPolicyDTO.getServiceObjectGroupNameList());
                cmdDTO.getGeneratedObject().setPolicyName(dNatPolicyDTO.getPolicyName());
                cmdDTO.getGeneratedObject().setRollbackShowCmd(dNatPolicyDTO.getRollbackShowCmd());
                cmdDTO.getGeneratedObject().setAddressTypeMap(dNatPolicyDTO.getAddressTypeMap());
                return dNatCommandLine;
            case BOTH:
                NatPolicyDTO natPolicyDTO = getBothNatDTO(cmdDTO);
                String bothNatCommandLine = generateBothNatCommandLine(natPolicyDTO);
                cmdDTO.getGeneratedObject().setAddressObjectNameList(natPolicyDTO.getAddressObjectNameList());
                cmdDTO.getGeneratedObject().setAddressObjectGroupNameList(natPolicyDTO.getAddressObjectGroupNameList());
                cmdDTO.getGeneratedObject().setServiceObjectNameList(natPolicyDTO.getServiceObjectNameList());
                cmdDTO.getGeneratedObject().setServiceObjectGroupNameList(natPolicyDTO.getServiceObjectGroupNameList());
                cmdDTO.getGeneratedObject().setRollbackShowCmd(natPolicyDTO.getRollbackShowCmd());
                cmdDTO.getGeneratedObject().setAddressTypeMap(natPolicyDTO.getAddressTypeMap());
                cmdDTO.getGeneratedObject().setPolicyName(natPolicyDTO.getPolicyName());
                cmdDTO.getGeneratedObject().setRollbackCommandLine(natPolicyDTO.getRollbackCommandLine());
                return bothNatCommandLine;
            case F5_DNAT:
                F5DNatPolicyDTO f5DNatPolicyDTO = getF5DNatDTO(cmdDTO);
                return  generateF5DNatCommandLine(f5DNatPolicyDTO);
            case F5_BOTH_NAT:
                F5BothNatPolicyDTO f5BothNatPolicyDTO = getF5BothNatDTO(cmdDTO);
                return  generateF5BothNatCommandLine(f5BothNatPolicyDTO);
            default:
                return DO_NOT_SUPPORT;
        }
    }

    String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO);

    String generateSNatCommandLine(SNatPolicyDTO policyDTO);

    String generateDNatCommandLine(DNatPolicyDTO policyDTO);

    String generateBothNatCommandLine(NatPolicyDTO policyDTO);

    default String generateF5DNatCommandLine(F5DNatPolicyDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    default String generateF5BothNatCommandLine(F5BothNatPolicyDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    default StaticNatTaskDTO getStaticNatDTO(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        StaticNatTaskDTO staticNatTaskDTO = new StaticNatTaskDTO();
        staticNatTaskDTO.setDescription(policyDTO.getDescription());
        staticNatTaskDTO.setGlobalAddress(policyDTO.getDstIp());
        staticNatTaskDTO.setInsideAddress(policyDTO.getPostDstIp());
        if(ObjectUtils.isNotEmpty(cmdDTO.getExistObject())){
            staticNatTaskDTO.setExistGlobaPort(cmdDTO.getExistObject().getServiceObjectName());
            staticNatTaskDTO.setExistInsidePort(cmdDTO.getExistObject().getPostServiceObjectName());
        }
        List<ServiceDTO> serviceList = policyDTO.getServiceList();
        if(serviceList != null && serviceList.size() > 0) {
            ServiceDTO serviceDTO = serviceList.get(0);
            staticNatTaskDTO.setProtocol(serviceDTO.getProtocol());
            staticNatTaskDTO.setGlobalPort(serviceDTO.getDstPorts());
        }

        List<ServiceDTO> postServiceList = policyDTO.getPostServiceList();
        if(postServiceList != null && postServiceList.size() > 0) {
            ServiceDTO serviceDTO = postServiceList.get(0);
            staticNatTaskDTO.setInsidePort(serviceDTO.getDstPorts());
        }

        staticNatTaskDTO.setFromZone(policyDTO.getSrcZone());
        staticNatTaskDTO.setToZone(policyDTO.getDstZone());
        staticNatTaskDTO.setInDevItf(policyDTO.getSrcItf());
        staticNatTaskDTO.setOutDevItf(policyDTO.getDstItf());

        TaskDTO taskDTO = cmdDTO.getTask();
        staticNatTaskDTO.setTheme(taskDTO.getTheme());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        staticNatTaskDTO.setGlobalAddressName(existObjectDTO.getDstAddressObjectName());
        staticNatTaskDTO.setInsideAddressName(existObjectDTO.getPostDstAddressObjectName());
        staticNatTaskDTO.setExistGlobaPort(CollectionUtils.isNotEmpty(existObjectDTO.getExistServiceNameList())?existObjectDTO.getExistServiceNameList().get(0):null);
        staticNatTaskDTO.setExistInsidePort(CollectionUtils.isNotEmpty(existObjectDTO.getExistPostServiceNameList())?existObjectDTO.getExistPostServiceNameList().get(0):null);
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        staticNatTaskDTO.setVsys(deviceDTO.isVsys());
        staticNatTaskDTO.setVsysName(deviceDTO.getVsysName());
        staticNatTaskDTO.setHasVsys(deviceDTO.isHasVsys());
        //引用传递，后续回滚命令，还会用到
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(staticNatTaskDTO.getTheme());
        List<String> addressNameList = new ArrayList<>();
        addressNameList.add(H3cSecPathV7.strSub(staticNatTaskDTO.getInsideAddress(), H3cSecPathV7.MAX_OBJECT_NAME_LENGTH,"GB2312"));
        addressNameList.add(H3cSecPathV7.strSub(staticNatTaskDTO.getGlobalAddress(), H3cSecPathV7.MAX_OBJECT_NAME_LENGTH,"GB2312"));
        generatedDto.setAddressNameList(addressNameList);
        SettingDTO setting = cmdDTO.getSetting();
        staticNatTaskDTO.setCurrentId(setting.getPolicyId());
        staticNatTaskDTO.setMoveSeatEnum(setting.getMoveSeatEnum());
        staticNatTaskDTO.setSwapRuleNameId(setting.getSwapNameId());
        return staticNatTaskDTO;
    }

    default SNatPolicyDTO getSNatPolicyDTO(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
        sNatPolicyDTO.setDescription(policyDTO.getDescription());
        sNatPolicyDTO.setSrcIp(policyDTO.getSrcIp());
        sNatPolicyDTO.setDstIp(policyDTO.getDstIp());
        sNatPolicyDTO.setPostIpAddress(policyDTO.getPostSrcIp());
        sNatPolicyDTO.setServiceList(policyDTO.getServiceList());
        sNatPolicyDTO.setSrcIpSystem(policyDTO.getSrcIpSystem());
        sNatPolicyDTO.setDstIpSystem(policyDTO.getDstIpSystem());
        sNatPolicyDTO.setPostSrcIpSystem(policyDTO.getPostSrcIpSystem());
        sNatPolicyDTO.setSrcZone(policyDTO.getSrcZone());
        sNatPolicyDTO.setDstZone(policyDTO.getDstZone());

        sNatPolicyDTO.setSrcItf(policyDTO.getSrcItf());
        sNatPolicyDTO.setDstItf(policyDTO.getDstItf());
        sNatPolicyDTO.setIpType(policyDTO.getIpType());


        sNatPolicyDTO.setStartTime(policyDTO.getStartTime());
        sNatPolicyDTO.setEndTime(policyDTO.getEndTime());
        sNatPolicyDTO.setIpType(policyDTO.getIpType());
        sNatPolicyDTO.setExistAclName(policyDTO.getExistAclName());
        sNatPolicyDTO.setExistGlobal(policyDTO.isExistGlobal());

        TaskDTO taskDTO = cmdDTO.getTask();
        sNatPolicyDTO.setTheme(taskDTO.getTheme());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        sNatPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        sNatPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());
        sNatPolicyDTO.setServiceObjectName(existObjectDTO.getServiceObjectName());

        sNatPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
        sNatPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
        sNatPolicyDTO.setPostAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());
        sNatPolicyDTO.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        sNatPolicyDTO.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        sNatPolicyDTO.setExistPostSrcAddressList(existObjectDTO.getExistPostSrcAddressList());
        sNatPolicyDTO.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        sNatPolicyDTO.setRestPostSrcAddressList(existObjectDTO.getRestPostSrcAddressList());
        sNatPolicyDTO.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        sNatPolicyDTO.setExistDstAddressName(existObjectDTO.getExistDstAddressName());
        sNatPolicyDTO.setExistSrcAddressName(existObjectDTO.getExistSrcAddressName());
        sNatPolicyDTO.setExistPostSrcAddressName(existObjectDTO.getExistPostSrcAddressName());
        //引用传递，后续回滚命令，还会用到
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(sNatPolicyDTO.getTheme());
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        sNatPolicyDTO.setVsys(deviceDTO.isVsys());
        sNatPolicyDTO.setVsysName(deviceDTO.getVsysName());
        sNatPolicyDTO.setHasVsys(deviceDTO.isHasVsys());

        SettingDTO setting = cmdDTO.getSetting();
        sNatPolicyDTO.setCurrentId(setting.getPolicyId());
        sNatPolicyDTO.setCurrentAddressGroupId(setting.getH3v7addressGroupId());
        sNatPolicyDTO.setMoveSeatEnum(setting.getMoveSeatEnum());
        sNatPolicyDTO.setSwapRuleNameId(setting.getSwapNameId());
        return sNatPolicyDTO;
    }

    default DNatPolicyDTO getDNatPolicyDTO(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        DNatPolicyDTO dnatPolicyDTO = new DNatPolicyDTO();
        dnatPolicyDTO.setDescription(policyDTO.getDescription());
        dnatPolicyDTO.setSrcIpSystem(policyDTO.getSrcIpSystem());
        dnatPolicyDTO.setDstIpSystem(policyDTO.getDstIpSystem());
        dnatPolicyDTO.setPostDstIpSystem(policyDTO.getPostSrcIpSystem());
        dnatPolicyDTO.setSrcIp(policyDTO.getSrcIp());
        dnatPolicyDTO.setDstIp(policyDTO.getDstIp());
        dnatPolicyDTO.setPostIpAddress(policyDTO.getPostDstIp());
        dnatPolicyDTO.setServiceList(policyDTO.getServiceList());
        dnatPolicyDTO.setStartTime(policyDTO.getStartTime());
        dnatPolicyDTO.setEndTime(policyDTO.getEndTime());
        dnatPolicyDTO.setIpType(policyDTO.getIpType());
        dnatPolicyDTO.setFortinetDnatSpecialDTO(policyDTO.getFortinetDnatSpecialDTO());
        List<ServiceDTO> postServiceList = policyDTO.getPostServiceList();
        if(postServiceList != null && postServiceList.size() > 0) {
            ServiceDTO serviceDTO = postServiceList.get(0);
            dnatPolicyDTO.setPostPort(serviceDTO.getDstPorts());
        }

        dnatPolicyDTO.setSrcZone(policyDTO.getSrcZone());
        dnatPolicyDTO.setDstZone(policyDTO.getDstZone());

        dnatPolicyDTO.setSrcItf(policyDTO.getSrcItf());
        dnatPolicyDTO.setDstItf(policyDTO.getDstItf());
        dnatPolicyDTO.setIpType(policyDTO.getIpType());

        TaskDTO taskDTO = cmdDTO.getTask();
        dnatPolicyDTO.setTheme(taskDTO.getTheme());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dnatPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dnatPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());
        dnatPolicyDTO.setServiceObjectName(existObjectDTO.getServiceObjectName());

        dnatPolicyDTO.setPostServiceObjectName(existObjectDTO.getPostServiceObjectName());
        dnatPolicyDTO.setExistPostServiceNameList(existObjectDTO.getExistPostServiceNameList());
        dnatPolicyDTO.setRestPostServiceList(existObjectDTO.getRestPostServiceList());


        dnatPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
        dnatPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
        dnatPolicyDTO.setPostAddressObjectName(existObjectDTO.getPostDstAddressObjectName());

        dnatPolicyDTO.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dnatPolicyDTO.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dnatPolicyDTO.setExistPostSrcAddressList(existObjectDTO.getExistPostSrcAddressList());
        dnatPolicyDTO.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dnatPolicyDTO.setRestPostSrcAddressList(existObjectDTO.getRestPostSrcAddressList());
        dnatPolicyDTO.setRestDstAddressList(existObjectDTO.getRestDstAddressList());

        dnatPolicyDTO.setExistPostDstAddressList(existObjectDTO.getExistPostDstAddressList());
        dnatPolicyDTO.setRestPostDstAddressList(existObjectDTO.getRestPostDstAddressList());

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dnatPolicyDTO.getTheme());
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        dnatPolicyDTO.setVsys(deviceDTO.isVsys());
        dnatPolicyDTO.setVsysName(deviceDTO.getVsysName());
        dnatPolicyDTO.setHasVsys(deviceDTO.isHasVsys());

        SettingDTO setting = cmdDTO.getSetting();
        dnatPolicyDTO.setCurrentId(setting.getPolicyId());
        dnatPolicyDTO.setCurrentAddressGroupId(setting.getH3v7addressGroupId());
        dnatPolicyDTO.setMoveSeatEnum(setting.getMoveSeatEnum());
        dnatPolicyDTO.setSwapRuleNameId(setting.getSwapNameId());
        return dnatPolicyDTO;
    }

    default NatPolicyDTO getBothNatDTO(CmdDTO cmdDTO) {
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        ExistObjectRefDTO specialExistObject = cmdDTO.getSpecialExistObject();
        NatPolicyDTO natPolicyDTO = new NatPolicyDTO();
        natPolicyDTO.setDescription(policyDTO.getDescription());
        natPolicyDTO.setSpecialExistObject(specialExistObject);
        natPolicyDTO.setSrcIp(policyDTO.getSrcIp());
        natPolicyDTO.setDstIp(policyDTO.getDstIp());
        natPolicyDTO.setStartTime(policyDTO.getStartTime());
        natPolicyDTO.setEndTime(policyDTO.getEndTime());
        natPolicyDTO.setExistVirtualIpName(policyDTO.getExistVirtualIpName());

        natPolicyDTO.setPostSrcIp(policyDTO.getPostSrcIp());
        natPolicyDTO.setPostDstIp(policyDTO.getPostDstIp());

        natPolicyDTO.setServiceList(policyDTO.getServiceList());
        natPolicyDTO.setPostServiceList(policyDTO.getPostServiceList());

        List<ServiceDTO> postServiceList = policyDTO.getPostServiceList();
        if (postServiceList != null && postServiceList.size() > 0) {
            ServiceDTO serviceDTO = postServiceList.get(0);
            natPolicyDTO.setPostPort(serviceDTO.getDstPorts());
        }

        natPolicyDTO.setSrcZone(policyDTO.getSrcZone());
        natPolicyDTO.setDstZone(policyDTO.getDstZone());


        natPolicyDTO.setSrcItf(policyDTO.getSrcItf());
        natPolicyDTO.setDstItf(policyDTO.getDstItf());
        natPolicyDTO.setIpType(policyDTO.getIpType());


        TaskDTO taskDTO = cmdDTO.getTask();
        natPolicyDTO.setTheme(taskDTO.getTheme());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        natPolicyDTO.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        natPolicyDTO.setRestServiceList(existObjectDTO.getRestServiceList());
        natPolicyDTO.setServiceObjectName(existObjectDTO.getServiceObjectName());


        natPolicyDTO.setPostServiceObjectName(existObjectDTO.getPostServiceObjectName());
        natPolicyDTO.setExistPostServiceNameList(existObjectDTO.getExistPostServiceNameList());
        natPolicyDTO.setRestPostServiceList(existObjectDTO.getRestPostServiceList());

        natPolicyDTO.setSrcAddressObjectName(existObjectDTO.getSrcAddressObjectName());
        natPolicyDTO.setDstAddressObjectName(existObjectDTO.getDstAddressObjectName());
        natPolicyDTO.setPostSrcAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());
        natPolicyDTO.setPostDstAddressObjectName(existObjectDTO.getPostDstAddressObjectName());
        natPolicyDTO.setRestPostDstAddressList(existObjectDTO.getRestPostDstAddressList());
        natPolicyDTO.setExistPostDstAddressList(existObjectDTO.getExistPostDstAddressList());
        natPolicyDTO.setRestPostSrcAddressList(existObjectDTO.getRestPostSrcAddressList());
        natPolicyDTO.setExistPostSrcAddressList(existObjectDTO.getExistPostSrcAddressList());
        natPolicyDTO.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        natPolicyDTO.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        natPolicyDTO.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        natPolicyDTO.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        natPolicyDTO.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        natPolicyDTO.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(natPolicyDTO.getTheme());
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        natPolicyDTO.setVsys(deviceDTO.isVsys());
        natPolicyDTO.setVsysName(deviceDTO.getVsysName());
        natPolicyDTO.setHasVsys(deviceDTO.isHasVsys());
        natPolicyDTO.setDynamic(policyDTO.isDynamic());

        SettingDTO setting = cmdDTO.getSetting();
        natPolicyDTO.setCurrentId(setting.getPolicyId());
        natPolicyDTO.setCurrentAddressGroupId(setting.getH3v7addressGroupId());
        natPolicyDTO.setMoveSeatEnum(setting.getMoveSeatEnum());
        natPolicyDTO.setSwapRuleNameId(setting.getSwapNameId());
        return natPolicyDTO;
    }

    default F5BothNatPolicyDTO getF5BothNatDTO(CmdDTO cmdDTO) {
        F5BothNatPolicyDTO  f5bothNatPolicyDTO = new F5BothNatPolicyDTO();

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        f5bothNatPolicyDTO.setExistPostSrcAddressList(existObjectDTO.getExistPostSrcAddressList());
        f5bothNatPolicyDTO.setPostSrcAddressObjectName(existObjectDTO.getPostSrcAddressObjectName());

        PolicyDTO policyDTO = cmdDTO.getPolicy();
        if (StringUtils.isNotEmpty(policyDTO.getOriginalSrcIp())){
            f5bothNatPolicyDTO.setSrcIp(policyDTO.getOriginalSrcIp());
        }
        f5bothNatPolicyDTO.setDstIp(policyDTO.getOriginalDstIp());
        f5bothNatPolicyDTO.setDescription(policyDTO.getDescription());
        f5bothNatPolicyDTO.setServiceList(policyDTO.getServiceList());

        TaskDTO taskDTO = cmdDTO.getTask();
        f5bothNatPolicyDTO.setId(taskDTO.getId());
        f5bothNatPolicyDTO.setTaskId(taskDTO.getTaskId());
        f5bothNatPolicyDTO.setTheme(taskDTO.getTheme());

        CommandLineBalanceInfoDTO commandLineBalanceInfoDTO = cmdDTO.getCommandLineBalanceInfoDTO();
        f5bothNatPolicyDTO.setSnatPoolInfo(commandLineBalanceInfoDTO.getSnatPoolInfo());
        f5bothNatPolicyDTO.setPoolInfo(commandLineBalanceInfoDTO.getPoolInfo());
        f5bothNatPolicyDTO.setSceneName(commandLineBalanceInfoDTO.getSceneName());
        f5bothNatPolicyDTO.setApplyType(commandLineBalanceInfoDTO.getApplyType());
        f5bothNatPolicyDTO.setLoadBlanaceMode(commandLineBalanceInfoDTO.getLoadBlanaceMode());
        f5bothNatPolicyDTO.setPersist(commandLineBalanceInfoDTO.getPersist());
        f5bothNatPolicyDTO.setMonitor(commandLineBalanceInfoDTO.getMonitor());
        f5bothNatPolicyDTO.setHttpProfile(commandLineBalanceInfoDTO.getHttpProfile());
        f5bothNatPolicyDTO.setSslProfile(commandLineBalanceInfoDTO.getSslProfile());
        f5bothNatPolicyDTO.setSnatType(commandLineBalanceInfoDTO.getSnatType());
        if (StringUtils.isNotEmpty(commandLineBalanceInfoDTO.getHttpProfile())){
            f5bothNatPolicyDTO.setHttpProfile(commandLineBalanceInfoDTO.getHttpProfile());
        }
        if (StringUtils.isNotEmpty(commandLineBalanceInfoDTO.getSslProfile())){
            f5bothNatPolicyDTO.setSslProfile(commandLineBalanceInfoDTO.getSslProfile());
        }
        DeviceDTO device = cmdDTO.getDevice();
        f5bothNatPolicyDTO.setVsys(device.isVsys());
        if (StringUtils.isNotEmpty(device.getVsysName())){
            f5bothNatPolicyDTO.setVsysName(device.getVsysName());
        }

        return f5bothNatPolicyDTO;
    }

    default F5DNatPolicyDTO getF5DNatDTO(CmdDTO cmdDTO) {
        F5DNatPolicyDTO  f5DNatPolicyDTO = new F5DNatPolicyDTO();

        PolicyDTO policyDTO = cmdDTO.getPolicy();
        if (StringUtils.isNotEmpty(policyDTO.getOriginalSrcIp())){
            f5DNatPolicyDTO.setSrcIp(policyDTO.getOriginalSrcIp());
        }
        f5DNatPolicyDTO.setDstIp(policyDTO.getOriginalDstIp());
        f5DNatPolicyDTO.setDescription(policyDTO.getDescription());
        f5DNatPolicyDTO.setServiceList(policyDTO.getServiceList());

        TaskDTO taskDTO = cmdDTO.getTask();
        f5DNatPolicyDTO.setId(taskDTO.getId());
        f5DNatPolicyDTO.setTaskId(taskDTO.getTaskId());
        f5DNatPolicyDTO.setTheme(taskDTO.getTheme());

        CommandLineBalanceInfoDTO commandLineBalanceInfoDTO = cmdDTO.getCommandLineBalanceInfoDTO();
        f5DNatPolicyDTO.setPoolInfo(commandLineBalanceInfoDTO.getPoolInfo());
        f5DNatPolicyDTO.setSceneName(commandLineBalanceInfoDTO.getSceneName());
        f5DNatPolicyDTO.setApplyType(commandLineBalanceInfoDTO.getApplyType());
        f5DNatPolicyDTO.setLoadBlanaceMode(commandLineBalanceInfoDTO.getLoadBlanaceMode());
        f5DNatPolicyDTO.setPersist(commandLineBalanceInfoDTO.getPersist());
        f5DNatPolicyDTO.setMonitor(commandLineBalanceInfoDTO.getMonitor());
        f5DNatPolicyDTO.setSnatType(commandLineBalanceInfoDTO.getSnatType());
        if (StringUtils.isNotEmpty(commandLineBalanceInfoDTO.getHttpProfile())){
            f5DNatPolicyDTO.setHttpProfile(commandLineBalanceInfoDTO.getHttpProfile());
        }
        if (StringUtils.isNotEmpty(commandLineBalanceInfoDTO.getSslProfile())){
            f5DNatPolicyDTO.setSslProfile(commandLineBalanceInfoDTO.getSslProfile());
        }
        DeviceDTO device = cmdDTO.getDevice();
        f5DNatPolicyDTO.setVsys(device.isVsys());
        if (StringUtils.isNotEmpty(device.getVsysName())){
            f5DNatPolicyDTO.setVsysName(device.getVsysName());
        }
        return f5DNatPolicyDTO;
    }

        /**
         * 记录创建对象的名称
         *
         * @param srcAddressObject
         * @param dstAddressObject
         * @param serviceObject
         */
    default void recordCreateObjectName(List<String> addressObjectNameList, List<String> addressObjectGroupNameList,
        List<String> serviceObjectNameList, List<String> serviceObjectGroupNameList, PolicyObjectDTO srcAddressObject,
        PolicyObjectDTO dstAddressObject, PolicyObjectDTO postSrcAddressObject, PolicyObjectDTO postDstAddressObject,
        PolicyObjectDTO serviceObject, PolicyObjectDTO natObject) {

        if (null != srcAddressObject && CollectionUtils.isNotEmpty(srcAddressObject.getCreateGroupObjectName())) {
            addressObjectGroupNameList.addAll(srcAddressObject.getCreateGroupObjectName());
        }
        if (null != srcAddressObject && CollectionUtils.isNotEmpty(srcAddressObject.getCreateObjectName())) {
            addressObjectNameList.addAll(srcAddressObject.getCreateObjectName());
        }

        if (null != dstAddressObject && CollectionUtils.isNotEmpty(dstAddressObject.getCreateGroupObjectName())) {
            addressObjectGroupNameList.addAll(dstAddressObject.getCreateGroupObjectName());
        }
        if (null != dstAddressObject && CollectionUtils.isNotEmpty(dstAddressObject.getCreateObjectName())) {
            addressObjectNameList.addAll(dstAddressObject.getCreateObjectName());
        }

        if (null != postSrcAddressObject && CollectionUtils.isNotEmpty(postSrcAddressObject.getCreateGroupObjectName())) {
            addressObjectGroupNameList.addAll(postSrcAddressObject.getCreateGroupObjectName());
        }
        if (null != postSrcAddressObject && CollectionUtils.isNotEmpty(postSrcAddressObject.getCreateObjectName())) {
            addressObjectNameList.addAll(postSrcAddressObject.getCreateObjectName());
        }
        if (null != postDstAddressObject && CollectionUtils.isNotEmpty(postDstAddressObject.getCreateGroupObjectName())) {
            addressObjectGroupNameList.addAll(postDstAddressObject.getCreateGroupObjectName());
        }
        if (null != postDstAddressObject && CollectionUtils.isNotEmpty(postDstAddressObject.getCreateObjectName())) {
            addressObjectNameList.addAll(postDstAddressObject.getCreateObjectName());
        }

        if (null != natObject && CollectionUtils.isNotEmpty(natObject.getCreateGroupObjectName())) {
            addressObjectGroupNameList.addAll(natObject.getCreateGroupObjectName());
        }
        if (null != natObject && CollectionUtils.isNotEmpty(natObject.getCreateObjectName())) {
            addressObjectNameList.addAll(natObject.getCreateObjectName());
        }
        if (null != serviceObject && CollectionUtils.isNotEmpty(serviceObject.getCreateServiceGroupObjectNames())) {
            serviceObjectGroupNameList.addAll(serviceObject.getCreateServiceGroupObjectNames());
        }
        if (null != serviceObject && CollectionUtils.isNotEmpty(serviceObject.getCreateServiceObjectName())) {
            serviceObjectNameList.addAll(serviceObject.getCreateServiceObjectName());
        }
    }
}
