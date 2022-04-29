package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.network.TotemsIp6Utils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.EntityUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.generate.task.impl.CmdTaskServiceImpl;
import com.abtnetworks.totems.push.dao.mysql.PushRecommendTaskExpandMapper;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.dto.StaticRoutingDTO;
import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import com.abtnetworks.totems.push.service.PushTaskStaticRoutingService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.SNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PushTaskStaticRoutingServiceImpl implements PushTaskStaticRoutingService {

    @Autowired
    private PushRecommendTaskExpandMapper pushRecommendTaskExpandMapper;

    @Autowired
    RemoteBranchService remoteBranchService;

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;

    @Autowired
    private RecommendTaskManager taskService;

    @Autowired
    private CommandlineService commandlineService;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Autowired
    WhaleManager whaleManager;

    @Resource
    CmdTaskServiceImpl cmdTaskService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int createPushTaskStaticRouting(PushRecommendStaticRoutingDTO dto) {
        if (dto == null) {
            return ReturnCode.EMPTY_PARAMETERS;
        }
        log.info("新建静态路由策略任务入参:{}", JSONObject.toJSONString(dto));
        // 构建工单信息
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
        BeanUtils.copyProperties(dto, recommendTaskEntity);
        recommendTaskEntity.setSrcIp("");
        SNatAdditionalInfoEntity additionalInfoEntity = new SNatAdditionalInfoEntity();
        additionalInfoEntity.setDeviceUuid(dto.getDeviceUuid());
        recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));


        //数据校验
        int rc = vaildateParam(dto);
        if(rc != ReturnCode.POLICY_MSG_OK) {
            return rc;
        }

        //组装建议策略
        PushRecommendTaskExpandEntity taskEntity = new PushRecommendTaskExpandEntity();
        BeanUtils.copyProperties(dto, taskEntity);
        StaticRoutingDTO staticRoutingDTO = new StaticRoutingDTO();
        BeanUtils.copyProperties(dto, staticRoutingDTO);

        String staticRouting = JSONObject.toJSONString(staticRoutingDTO);
        taskEntity.setStaticRoutingInfo(staticRouting);
        taskEntity.setCreateTime(new Date());
        recommendTaskEntity.setTheme(dto.getTheme());
        recommendTaskEntity.setDescription(dto.getMark());

        UserInfoDTO userInfoDTO = remoteBranchService.findOne(dto.getCreateUser());
        if (userInfoDTO != null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
            recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            recommendTaskEntity.setBranchLevel("00");
        }

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber = "A" + simpleDateFormat.format(date);
        recommendTaskEntity.setOrderNumber(orderNumber);
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        recommendTaskEntity.setUserName(dto.getCreateUser());
        recommendTaskEntity.setCreateTime(new Date());

        Integer taskType = PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING;
        recommendTaskEntity.setTaskType(taskType);
        taskEntity.setTaskType(taskType);
        dto.setTaskType(taskType);

        List<RecommendTaskEntity> list = new ArrayList<>();
        list.add(recommendTaskEntity);
        taskService.insertRecommendTaskList(list);
        taskEntity.setTaskId(list.get(0).getId());
        dto.setTaskId(taskEntity.getTaskId());
        pushRecommendTaskExpandMapper.add(taskEntity);

        String message = String.format("新建工单策略%s成功", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        // 命令行数据生成
        int result = addCommandEditableEntityTask(taskEntity, recommendTaskEntity, staticRoutingDTO,taskType);
        return result;

    }

    @Override
    public PageInfo<PushRecommendStaticRoutingDTO> findPushTaskStaticRoutingPage(PushRecommendStaticRoutingDTO dto) {
        String branchLevel = remoteBranchService.likeBranch(dto.getCreateUser());
        // 查询列表
        String status = null == dto.getTaskStatus() ? null : String.valueOf(dto.getTaskStatus());
        List<RecommendTaskEntity> list = searchTaskList(dto.getTheme(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING,status,
                dto.getCurrentPage(), dto.getPageSize(), branchLevel);

        PageInfo<RecommendTaskEntity> originalPageInfo = new PageInfo<>(list);

        List<PushRecommendStaticRoutingDTO> staticRoutingDTOList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PushRecommendStaticRoutingDTO staticRoutingDTO = new PushRecommendStaticRoutingDTO();
            BeanUtils.copyProperties(entity, staticRoutingDTO);
            staticRoutingDTO.setCreateUser(entity.getUserName());
            staticRoutingDTO.setCreateTime(entity.getCreateTime());
            staticRoutingDTO.setTaskStatus(entity.getStatus());
            staticRoutingDTO.setTaskId(entity.getId());
            staticRoutingDTO.setIpType(entity.getIpType());
            staticRoutingDTO.setDstIp(entity.getDstIp());
            staticRoutingDTO.setTheme(entity.getTheme());
            PushRecommendTaskExpandEntity expandEntity = pushRecommendTaskExpandMapper.getByTaskId(entity.getId());
            if (null == expandEntity) {
                log.info("根据任务id:{}查询拓展数据为空", entity.getId());
                continue;
            }

            String deviceIp = "未知设备";
            //设备uuid
            String deviceUuid = expandEntity.getDeviceUuid();
            if (StringUtils.isNotBlank(deviceUuid)) {
                deviceIp = String.format("未知设备(%s)", deviceUuid);
                if (deviceUuid != null) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                    if (nodeEntity != null) {
                        deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }
                }
            }
            staticRoutingDTO.setDeviceUuid(expandEntity.getDeviceUuid());
            staticRoutingDTO.setDeviceName(deviceIp);
            staticRoutingDTO.setId(expandEntity.getId());
            staticRoutingDTO.setTaskType(expandEntity.getTaskType());
            staticRoutingDTO.setMark(expandEntity.getMark());

            if (StringUtils.isNotEmpty(expandEntity.getStaticRoutingInfo())){
                StaticRoutingDTO staticRoutingDTO1 = JSONObject.toJavaObject(JSONObject.parseObject(expandEntity.getStaticRoutingInfo()), StaticRoutingDTO.class);
                BeanUtils.copyProperties(staticRoutingDTO1, staticRoutingDTO);
            }
            staticRoutingDTOList.add(staticRoutingDTO);
        }

        PageInfo<PushRecommendStaticRoutingDTO> pageInfo = new PageInfo<>(staticRoutingDTOList);
        pageInfo.setTotal(originalPageInfo.getTotal());
        pageInfo.setStartRow(originalPageInfo.getStartRow());
        pageInfo.setEndRow(originalPageInfo.getEndRow());
        pageInfo.setPageSize(originalPageInfo.getPageSize());
        pageInfo.setPageNum(originalPageInfo.getPageNum());
        return pageInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePushTaskStaticRouting(String ids) {
        if(StringUtils.isBlank(ids)){
            return 0;
        }
        String[] strings = ids.split(",");
        List<String> taskIds = Arrays.asList(strings);
        List<Integer> taskIdList = taskIds.stream().map(p -> Integer.parseInt(p)).collect(Collectors.toList());
        // 删除工单表数据 和命令行生成表数据
        taskService.deleteTasks(taskIdList,0);
        // 策略工单拓展表数据
        return pushRecommendTaskExpandMapper.deleteByTaskId(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateStaticRoute(List<PushRecommendStaticRoutingDTO> dtos) {
        for (PushRecommendStaticRoutingDTO dto : dtos) {
            createPushTaskStaticRouting(dto);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public PushRecommendStaticRoutingDTO getStaticRoutingByTaskId(Integer taskId) {
        PushRecommendTaskExpandEntity expandEntity = pushRecommendTaskExpandMapper.getByTaskId(taskId);
        if (null == expandEntity) {
            return new PushRecommendStaticRoutingDTO();
        }
        StaticRoutingDTO staticRoutingDTO = JSONObject.toJavaObject(JSONObject.parseObject(expandEntity.getStaticRoutingInfo()), StaticRoutingDTO.class);
        PushRecommendStaticRoutingDTO dto = new PushRecommendStaticRoutingDTO();
        BeanUtils.copyProperties(expandEntity, dto);
        BeanUtils.copyProperties(staticRoutingDTO, dto);
        return dto;
    }

    /**
     * 新建命令行生成表 并生成命令行
     *
     * @param taskEntity
     * @return
     */
    public int addCommandEditableEntityTask(PushRecommendTaskExpandEntity taskEntity, RecommendTaskEntity recommendTaskEntity, StaticRoutingDTO staticRoutingDTO, Integer taskType) {
        PolicyEnum policyType = PolicyEnum.STRTIC_ROUTING;
        CommandTaskEditableEntity commandEntity = EntityUtils.createCommandTask(taskType,
                taskEntity.getTaskId(), taskEntity.getCreateUser(), recommendTaskEntity.getTheme(), taskEntity.getDeviceUuid());
        commandEntity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        // 新建命令行数据
        commandTaskManager.addCommandEditableEntityTask(commandEntity);

        boolean isVsys = false;
        String vsysName = "";
        NodeEntity node = taskService.getTheNodeByUuid(taskEntity.getDeviceUuid());
        if(node == null) {
            log.error("设备UUID:{}查询设备信息为空", taskEntity.getDeviceUuid());
            return ReturnCode.EMPTY_DEVICE_INFO;
        } else {
            DeviceRO device = whaleManager.getDeviceByUuid(taskEntity.getDeviceUuid());
            DeviceDataRO deviceData = device.getData().get(0);
            if(deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                vsysName = deviceData.getVsysName();
            }
        }

        // 组装cmdDto 并进行下发
        CmdDTO cmdDTO = EntityUtils.createCmdDTOStaticRouting(taskEntity, recommendTaskEntity, commandEntity, staticRoutingDTO, policyType,isVsys,vsysName);
        GenerateCommandDTO generateCommandDTO = commandlineService.generateCommandForFiveBalance(cmdDTO);

        // 更新命令行表
        commandlineService.updateCommandStatus(commandEntity.getId(), generateCommandDTO.getCommandline(), generateCommandDTO.getRollbackCommandline(),
                PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START, null, null);

        StringBuffer commandlineDisaster = new StringBuffer(), rollbackDisasterCommandline = new StringBuffer(),
                otherAclMsgDisaster = new StringBuffer();
        String ruleListNameDisaster = null, matchRuleIdDisaster = null;
        String commandline = "无法生成该设备的命令行[命令行生成数据对象不合法]";
        //nat的灾备设备命令行生成
        List<GeneratedDeviceDTO> devices = new ArrayList<>();
        List<NodeEntity> anotherDeviceByIpList = commandlineService.isDisasterRecoveryDevice(cmdDTO, null);
        if (CollectionUtils.isNotEmpty(anotherDeviceByIpList)) {
            for (NodeEntity nodeEntity : anotherDeviceByIpList) {
                GeneratedDeviceDTO generatedDeviceDTO = new GeneratedDeviceDTO();
                generatedDeviceDTO.setNodeEntity(nodeEntity);

                DeviceRO deviceRO = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
                DeviceDataRO deviceData = deviceRO.getData().get(0);
                DeviceDTO disasterRecoveryDevice = new DeviceDTO();

                if (deviceData.getIsVsys() != null) {
                    boolean isVsys1 = deviceData.getIsVsys();
                    String vsysName1 = deviceData.getVsysName();
                    disasterRecoveryDevice.setVsys(isVsys1);
                    disasterRecoveryDevice.setVsysName(vsysName1);
                }
                generatedDeviceDTO.setDisasterRecoveryDTO(disasterRecoveryDevice);
                devices.add(generatedDeviceDTO);
            }
        }
        List<NodeEntity> anotherDeviceByIpList1 = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(devices)){
            for (GeneratedDeviceDTO generatedDeviceDTO :devices){
                anotherDeviceByIpList1.add(generatedDeviceDTO.getNodeEntity());
            }
        }
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(cmdDTO.getTask().getUserName());
        TaskDTO task  = cmdDTO.getTask();
        task.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING);
        List<CommandTaskEditableEntity> entityDisasterList = commandlineService.saveDisasterDeviceCommandline(cmdDTO, anotherDeviceByIpList1, userInfoDTO);

        if (CollectionUtils.isNotEmpty(entityDisasterList)) {
            for (int i = 0; i < entityDisasterList.size(); i++) {
                GeneratedDeviceDTO generatedDeviceDTO = getGeneratedDeviceDTO(devices,entityDisasterList.get(i).getDeviceUuid());

                CmdDTO cmdDTODisaster = commandlineService.generateDisasterRecovery(cmdDTO, userInfoDTO, generatedDeviceDTO.getNodeEntity(), generatedDeviceDTO.getDisasterRecoveryDTO());
                CommandLineStaticRoutingInfoDTO commandLineStaticRoutingInfoDTO = new CommandLineStaticRoutingInfoDTO();
                BeanUtils.copyProperties(cmdDTO.getCommandLineStaticRoutingInfoDTO(), commandLineStaticRoutingInfoDTO);
                cmdDTODisaster.setCommandLineStaticRoutingInfoDTO(commandLineStaticRoutingInfoDTO);
                GenerateCommandDTO generateDisasterCommandDTO = commandlineService.generateCommandForFiveBalance(cmdDTODisaster);
                // 统计如果接口上没有绑定acl策略提示错误，不生成命令行
                if (null != cmdDTODisaster.getBusinessInfoDTO() && StringUtils.isNotBlank(cmdDTODisaster.getBusinessInfoDTO().getOtherErrorMsg())) {
                    otherAclMsgDisaster.append(cmdDTODisaster.getBusinessInfoDTO().getOtherErrorMsg()).append("\n");
                }
                if (generateDisasterCommandDTO != null && generateDisasterCommandDTO.getResultCode() == 0) {
                    commandlineDisaster.append(generateDisasterCommandDTO.getCommandline()).append(CommonConstants.LINE_BREAK);
                    rollbackDisasterCommandline.append(generateDisasterCommandDTO.getRollbackCommandline()).append(CommonConstants.LINE_BREAK);

                    if (StringUtils.isBlank(ruleListNameDisaster)) {
                        ruleListNameDisaster = cmdDTODisaster.getDevice().getRuleListName();
                        matchRuleIdDisaster = cmdDTODisaster.getDevice().getMatchRuleId();
                    }
                }
                // 更新单个备用设备
                String allCommandlineDisaster = commandlineDisaster.toString();
                String allRollbackDisasterCommandline = rollbackDisasterCommandline.toString();
                cmdDTODisaster.getTask().setId(entityDisasterList.get(i).getId());
                String otherAclMsgDisasterStr = otherAclMsgDisaster.toString();
                cmdDTODisaster.getDevice().setRuleListName(ruleListNameDisaster);
                cmdDTODisaster.getDevice().setMatchRuleId(matchRuleIdDisaster);
                cmdTaskService.updateCommandAndTask(allCommandlineDisaster, allRollbackDisasterCommandline, cmdDTODisaster,null,otherAclMsgDisasterStr);
                commandlineDisaster = new StringBuffer();
                rollbackDisasterCommandline = new StringBuffer();
                otherAclMsgDisaster = new StringBuffer();
            }
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    private int vaildateParam(PushRecommendStaticRoutingDTO dto){
        int rc;
        if(IpTypeEnum.IPV4.getCode() == dto.getIpType().intValue() ){
            if(!IpUtils.isIP(dto.getDstIp())){
                rc = ReturnCode.ADDRESS_NOT_IP_HOST;
            }else {
                rc = ReturnCode.POLICY_MSG_OK;
            }
            if (!InputValueUtils.validMask(String.valueOf(dto.getSubnetMask()), 0)) {
                rc = ReturnCode.SUBNET_MASK_ERROR;
            }
//            if(!TotemsIp4Utils.isIp4(dto.getDstIp())){
//                rc = ReturnCode.ADDRESS_NOT_IP_HOST;
//            }else {
//                rc = ReturnCode.POLICY_MSG_OK;
//            }
        } else {
            // 如果是ipv6,检查ipv6格式
            if(!IpUtils.isIPv6(dto.getDstIp())){
                rc = ReturnCode.ADDRESS_NOT_IP_HOST;
            }else {
                rc = ReturnCode.POLICY_MSG_OK;
            }
            if (!InputValueUtils.validMask(String.valueOf(dto.getSubnetMask()), 1)) {
                rc = ReturnCode.SUBNET_MASK_ERROR;
            }
//            if(!TotemsIp6Utils.isIp6(dto.getDstIp())){
//                rc = ReturnCode.ADDRESS_NOT_IP_HOST;
//            } else {
//                rc = ReturnCode.POLICY_MSG_OK;
//            }
        }

        // 如果出接口和下一跳同时不填则异常提示
        if (StringUtils.isBlank(dto.getNextHop()) && StringUtils.isBlank(dto.getOutInterface())) {
            rc = ReturnCode.OUTINTERFACE_AND_NEXTHOP_ALL_EMPTY;
        }
        return rc;
    }

    private List<RecommendTaskEntity> searchTaskList(String theme, Integer taskType, String status,int page, int psize, String branchLevel) {


        PageHelper.startPage(page, psize);
        Map<String, Object> params = new HashMap<>();
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("theme", theme);
        }
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if (!AliStringUtils.isEmpty(status)) {
            if (status.contains(",")) {
                status = status.split(",")[0];
            }
            params.put("status", String.valueOf(status));
        }
        if (null != taskType) {
            params.put("taskType", taskType);
        }

        List<RecommendTaskEntity> list = recommendTaskMapper.searchTask(params);
        return list;
    }

    /**
     * 获取设备信息(灾备和虚设备)
     * @param deviceDTOS
     * @param tergetDeviceUuid
     * @return
     */
    private GeneratedDeviceDTO getGeneratedDeviceDTO(List<GeneratedDeviceDTO> deviceDTOS,String tergetDeviceUuid){
        for (GeneratedDeviceDTO generatedDeviceDTO : deviceDTOS){
            if(tergetDeviceUuid.equals(generatedDeviceDTO.getNodeEntity().getUuid())){
                return generatedDeviceDTO;
            }
        }
        return null;
    }

}
