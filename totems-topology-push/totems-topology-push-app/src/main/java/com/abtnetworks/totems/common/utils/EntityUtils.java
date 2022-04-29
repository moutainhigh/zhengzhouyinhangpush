package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.advanced.dto.SceneForFiveBalanceDTO;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.PushPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.PushSnatPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.push.dto.StaticRoutingDTO;
import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import com.abtnetworks.totems.push.enums.PushSnatType;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.vo.PolicyRecommendSecurityPolicyVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntityUtils {

    public static CommandTaskEditableEntity createCommandTask(Integer taskType, Integer taskId, String userName, String theme, String deviceUuid) {
        CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
        entity.setCreateTime(new Date());
        entity.setStatus(PushConstants.PUSH_INT_PUSH_GENERATING);
        entity.setUserName(userName);
        entity.setTheme(theme);
        entity.setDeviceUuid(deviceUuid);
        entity.setTaskId(taskId);
        entity.setTaskType(taskType);
        return entity;
    }

    public static RecommendTaskEntity createRecommendTask(String theme, String userName, String srcIp, String dstIp, String serviceList,
                                                    Integer taskType, Integer status, String additionalInfo,String srcIpSystem, String dstIpSystem,
                                                          String postSrcIpAddressSystem,String postDstIpAddressSystem,Integer ipType){
        RecommendTaskEntity entity = new RecommendTaskEntity();
        entity.setTheme(theme);
        entity.setOrderNumber(getOrderNumber());
        entity.setUserName(userName);
        entity.setSrcIp(srcIp);
        entity.setDstIp(dstIp);
        entity.setServiceList(serviceList);
        entity.setTaskType(taskType);
        entity.setStatus(status);
        entity.setCreateTime(new Date());
        entity.setAdditionInfo(additionalInfo);
        entity.setSrcIpSystem(srcIpSystem);
        entity.setDstIpSystem(dstIpSystem);
        entity.setPostSrcIpSystem(postSrcIpAddressSystem);
        entity.setPostDstIpSystem(postDstIpAddressSystem);
        entity.setIpType(ipType);
        return entity;
    }

    public static CmdDTO createCmdDTO(PolicyEnum type, Integer id, Integer taskId, String deviceUuid, String theme, String userName,
                                      String srcIp, String dstIp, String postSrcIp, String postDstIp, List<ServiceDTO> serviceList,
                                      List<ServiceDTO> postServiceList, String srcZone, String dstZone, String inItf, String outItf, String inItfAlias, String outItfAlias,String description,Boolean isVsys, String vsysName,
                                      String srcIpSystem,String dstIpSystem,String postSrcIpSystem) {
        return createCmdDTO(type, id, taskId, deviceUuid, theme, userName, srcIp, dstIp, postSrcIp, postDstIp, serviceList,
                postServiceList, srcZone, dstZone, inItf, outItf, inItfAlias, outItfAlias, null, null, description, ActionEnum.PERMIT,
                isVsys, vsysName, MoveSeatEnum.FIRST, null, null, null, srcIpSystem, dstIpSystem,0,postSrcIpSystem, null, null, null, null, null, null);
    }

    public static CmdDTO createCmdDTO(PolicyEnum type, Integer id, Integer taskId, String deviceUuid, String theme, String userName,
                                      String srcIp, String dstIp, String postSrcIp, String postDstIp, List<ServiceDTO> serviceList,
                                      List<ServiceDTO> postServiceList, String srcZone, String dstZone, String inItf, String outItf,String description,Boolean isVsys, String vsysName,
                                        String srcIpSystem,String dstIpSystem,String postSrcIpSystem, String postPort) {
        return createCmdDTO(type, id, taskId, deviceUuid, theme, userName, srcIp, dstIp, postSrcIp, postDstIp, serviceList,
                postServiceList, srcZone, dstZone, inItf, outItf, null, null, null, null, description, ActionEnum.PERMIT,
                isVsys, vsysName, MoveSeatEnum.FIRST, null, null, null, srcIpSystem, dstIpSystem,0,postSrcIpSystem, null, null, null, null,postPort, null);
    }
    public static CmdDTO createCmdDTO(PolicyEnum type, Integer id, Integer taskId, String deviceUuid, String theme, String userName,
                                      String srcIp, String dstIp, String postSrcIp, String postDstIp, List<ServiceDTO> serviceList,
                                      List<ServiceDTO> postServiceList, String srcZone, String dstZone, String inItf, String outItf, String inItfAlias, String outItfAlias, String description,Boolean isVsys,
                                      String vsysName,String startTime,String endTime, String srcIpSystem, String dstIpSystem, String postSrcIpSystem, String postPort) {
        return createCmdDTO(type, id, taskId, deviceUuid, theme, userName, srcIp, dstIp, postSrcIp, postDstIp, serviceList,
                postServiceList, srcZone, dstZone, inItf, outItf, inItfAlias, outItfAlias, startTime, endTime, description, ActionEnum.PERMIT,
                isVsys, vsysName, MoveSeatEnum.FIRST, null, null, null, srcIpSystem, dstIpSystem,0,postSrcIpSystem, null, null, null, null,postPort, null);
    }
    public static CmdDTO createCmdDTO(PolicyEnum type, Integer id, Integer taskId, String deviceUuid, String theme, String userName,
                                String srcIp, String dstIp, String postSrcIp, String postDstIp, List<ServiceDTO> serviceList,
                                List<ServiceDTO> postServiceList, String srcZone, String dstZone, String inItf, String outItf,
                                String inItfAlias, String outItfAlias, String startTime, String endTime, String description,
                                ActionEnum action,Boolean isVsys, String vsysName, MoveSeatEnum moveSeat, String swapNameId,
                                String ruleListUuid, Integer idleTimeout,String srcIpSystem,String dstIpSystem,Integer ipType,
                                String postSrcIpSystem, Integer mergeProperty, String mergeValue, String editPolicyName,
                                      PolicyRecommendSecurityPolicyVO securityPolicyVO,String postPort, String vipName) {
        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO device = cmdDTO.getDevice();
        device.setDeviceUuid(deviceUuid);
        device.setVsys(isVsys);
        device.setVsysName(vsysName);
        device.setRuleListUuid(ruleListUuid);

        TaskDTO taskDTO = cmdDTO.getTask();
        taskDTO.setId(id);
        taskDTO.setTaskId(taskId);
        taskDTO.setTheme(theme);
        taskDTO.setUserName(userName);

        SettingDTO settingDTO = cmdDTO.getSetting();
        settingDTO.setMoveSeatEnum(moveSeat);
        settingDTO.setSwapNameId(swapNameId);

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setType(type);
        policyDTO.setSrcIp(srcIp);
        policyDTO.setPostSrcIp(postSrcIp);
        policyDTO.setDstIp(dstIp);
        policyDTO.setPostDstIp(postDstIp);
        policyDTO.setServiceList(serviceList);
        policyDTO.setPostServiceList(postServiceList);
        policyDTO.setSrcZone(srcZone);
        policyDTO.setDstZone(dstZone);
        policyDTO.setSrcItf(inItf);
        policyDTO.setDstItf(outItf);
        policyDTO.setSrcItfAlias(inItfAlias);
        policyDTO.setDstItfAlias(outItfAlias);
        policyDTO.setStartTime(startTime);
        policyDTO.setEndTime(endTime);
        policyDTO.setDescription(description);
        policyDTO.setAction(action);
        policyDTO.setIdleTimeout(idleTimeout);
        policyDTO.setSrcIpSystem(srcIpSystem);
        policyDTO.setDstIpSystem(dstIpSystem);
        policyDTO.setIpType(ipType);
        policyDTO.setPostSrcIpSystem(postSrcIpSystem);
        policyDTO.setPostPort(postPort);
        policyDTO.setExistVirtualIpName(vipName);

        // 设备编辑策略字段属性
        policyDTO.setMergeProperty(mergeProperty);
        policyDTO.setMergeValue(mergeValue);
        policyDTO.setEditPolicyName(editPolicyName);
        PolicyRecommendSecurityPolicyDTO securityPolicyDTO = new PolicyRecommendSecurityPolicyDTO();
        if(securityPolicyVO != null){
            BeanUtils.copyProperties(securityPolicyVO, securityPolicyDTO);
        }
        policyDTO.setSecurityPolicy(securityPolicyDTO);
        cmdDTO.setPolicy(policyDTO);

        return cmdDTO;
    }

    public static CmdDTO createCmdDTOFiveBalance(PushRecommendTaskExpandEntity tasEntity,RecommendTaskEntity recommendTaskEntity, CommandTaskEditableEntity commandEntity, SceneForFiveBalanceDTO sceneDto, PolicyEnum policyType,boolean isVsys,String vsysName){
        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO device = cmdDTO.getDevice();
        device.setDeviceUuid(tasEntity.getDeviceUuid());
        device.setVsys(isVsys);
        device.setVsysName(vsysName);

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setType(policyType);

        // 对于F5而言 需要去复用的地址为snatpool的内容，即为postSrc
        if (StringUtils.isNotBlank(tasEntity.getSnatPoolInfo())) {
            PushSnatPoolInfo snatPoolInfo = JSONObject.toJavaObject(JSONObject.parseObject(tasEntity.getSnatPoolInfo()), PushSnatPoolInfo.class);
            // 判断如果不是引用的，就需要创建，需要创建的地址需要校验
            if (!snatPoolInfo.isQuote()) {
                policyDTO.setPostSrcIp(snatPoolInfo.getSnatPoolIp());
            }
        }
        policyDTO.setOriginalSrcIp(recommendTaskEntity.getSrcIp());
        policyDTO.setOriginalDstIp(recommendTaskEntity.getDstIp());
        policyDTO.setDescription(recommendTaskEntity.getDescription());
        if(StringUtils.isNotBlank(recommendTaskEntity.getServiceList())) {
            JSONArray array = JSONObject.parseArray(recommendTaskEntity.getServiceList());
            List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
            if (serviceList.size() > 0) {
                policyDTO.setServiceList(serviceList);
            }
        }
        cmdDTO.setPolicy(policyDTO);


        TaskDTO taskDTO = cmdDTO.getTask();
        taskDTO.setId(commandEntity.getId());
        taskDTO.setTaskId(commandEntity.getTaskId());
        taskDTO.setTheme(commandEntity.getTheme());
        taskDTO.setUserName(commandEntity.getUserName());

        CommandLineBalanceInfoDTO balanceInfoDTO = cmdDTO.getCommandLineBalanceInfoDTO();
        balanceInfoDTO.setSceneName(sceneDto.getSceneName());
        balanceInfoDTO.setApplyType(sceneDto.getApplyType());
        balanceInfoDTO.setLoadBlanaceMode(sceneDto.getLoadBlanaceMode());
        balanceInfoDTO.setPersist(sceneDto.getPersist());
        balanceInfoDTO.setMonitor(sceneDto.getMonitor());
        balanceInfoDTO.setHttpProfile(tasEntity.getHttpProfile());
        balanceInfoDTO.setSslProfile(tasEntity.getSslProfile());
        balanceInfoDTO.setSnatType(PushSnatType.getDescByCode(tasEntity.getSnatType()));
        if (StringUtils.isNotBlank(tasEntity.getPoolInfo())) {
            PushPoolInfo pushPoolInfo = JSONObject.toJavaObject(JSONObject.parseObject(tasEntity.getPoolInfo()), PushPoolInfo.class);
            balanceInfoDTO.setPoolInfo(pushPoolInfo);
        }
        if (StringUtils.isNotBlank(tasEntity.getSnatPoolInfo())) {
            PushSnatPoolInfo snatPushPoolInfo = JSONObject.toJavaObject(JSONObject.parseObject(tasEntity.getSnatPoolInfo()), PushSnatPoolInfo.class);
            balanceInfoDTO.setSnatPoolInfo(snatPushPoolInfo);
        }
        return cmdDTO;
    }

    public static CmdDTO createCmdDTOStaticRouting(PushRecommendTaskExpandEntity tasEntity, RecommendTaskEntity recommendTaskEntity, CommandTaskEditableEntity commandEntity, StaticRoutingDTO staticRoutingDTO, PolicyEnum policyType, boolean isVsys, String vsysName){
        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO device = cmdDTO.getDevice();
        device.setDeviceUuid(tasEntity.getDeviceUuid());
        device.setVsys(isVsys);
        device.setVsysName(vsysName);

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setType(policyType);
        policyDTO.setIpType(recommendTaskEntity.getIpType());

        policyDTO.setDstIp(recommendTaskEntity.getDstIp());
        policyDTO.setDescription(recommendTaskEntity.getDescription());
        cmdDTO.setPolicy(policyDTO);


        TaskDTO taskDTO = cmdDTO.getTask();
        taskDTO.setId(commandEntity.getId());
        taskDTO.setTaskId(commandEntity.getTaskId());
        taskDTO.setTheme(commandEntity.getTheme());
        taskDTO.setUserName(commandEntity.getUserName());

        CommandLineStaticRoutingInfoDTO staticRoutingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        staticRoutingInfoDTO.setDstVirtualRouter(staticRoutingDTO.getDstVirtualRouter());
        staticRoutingInfoDTO.setNextHop(staticRoutingDTO.getNextHop());
        staticRoutingInfoDTO.setOutInterface(staticRoutingDTO.getOutInterface());
        staticRoutingInfoDTO.setPriority(staticRoutingDTO.getPriority());
        staticRoutingInfoDTO.setSrcVirtualRouter(staticRoutingDTO.getSrcVirtualRouter());
        staticRoutingInfoDTO.setSubnetMask(staticRoutingDTO.getSubnetMask());
        staticRoutingInfoDTO.setManagementDistance(staticRoutingDTO.getManagementDistance());
        return cmdDTO;
    }

    public static CmdDTO createDeleteCmdDTO(NodeEntity nodeEntity, Integer policyId, Integer ipType, String policyName, String srcZone, String dstZone){
        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO device = new DeviceDTO();
        device.setDeviceUuid(nodeEntity.getUuid());
        cmdDTO.setDevice(device);

        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setType(PolicyEnum.SECURITY);
        policyDTO.setIpType(ipType);

        SettingDTO setting = new SettingDTO();
        if (StringUtils.isNotEmpty(policyName)){
            setting.setPolicyName(policyName);
            setting.setRandomNumberString(policyName);
        }
        if (null != policyId){
            setting.setPolicyId(String.valueOf(policyId));
        }
        cmdDTO.setSetting(setting);
        if (StringUtils.isNotEmpty(srcZone)){
            policyDTO.setSrcZone(srcZone);
        }
        if (StringUtils.isNotEmpty(dstZone)){
            policyDTO.setDstZone(dstZone);
        }
        cmdDTO.setPolicy(policyDTO);

        GeneratedObjectDTO generatedDto = new GeneratedObjectDTO();
        generatedDto.setPolicyName(policyName);
        generatedDto.setIpType(ipType);
        cmdDTO.setGeneratedObject(generatedDto);
        return cmdDTO;
    }


    public static List<ServiceDTO> getServiceList(String protocol, String dstPort) {
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol(protocol);
        serviceDTO.setDstPorts(dstPort);
        List<ServiceDTO> serviceList = new ArrayList<>();
        serviceList.add(serviceDTO);
        return serviceList;
    }

    public static List<ServiceDTO> getPostServiceList(List<ServiceDTO> serviceList, String postPort) {
        List<ServiceDTO> postServiceList = new ArrayList<>();
        for(ServiceDTO serviceDTO: serviceList) {
            ServiceDTO postService = new ServiceDTO();
            postService.setProtocol(serviceDTO.getProtocol());
            if(!postService.getProtocol().equals(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                postService.setDstPorts(postPort);
            }
            postServiceList.add(postService);
        }
        return postServiceList;
    }

    private static String getOrderNumber() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeConstants.ORDER_TIME_TAG_FORMAT);
        return "A" + simpleDateFormat.format(date);
    }
}
