package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.dto.SceneForFiveBalanceDTO;
import com.abtnetworks.totems.advanced.service.SceneForFiveBalanceService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.PoolDetailInfo;
import com.abtnetworks.totems.common.dto.commandline.PushPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.PushSnatPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.IPTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.enums.ProtocolEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.common.utils.excel.TotemsExcelImport;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.push.dao.mysql.PushRecommendTaskExpandMapper;
import com.abtnetworks.totems.push.dto.PushRecommendTaskExpandDTO;
import com.abtnetworks.totems.push.dto.PushTaskFiveBalanceImportDTO;
import com.abtnetworks.totems.push.dto.SceneForFiveBalanceImportDTO;
import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import com.abtnetworks.totems.push.enums.PushSnatType;
import com.abtnetworks.totems.push.service.PushTaskFiveBalanceService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author lifei
 * @desc ??????F5???????????????
 * @date 2021/8/2 17:42
 */
@Service
@Log4j2
public class PushTaskFiveBalanceServiceImpl implements PushTaskFiveBalanceService {

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
    private SceneForFiveBalanceService sceneForFiveBalanceService;

    @Autowired
    private RecommendTaskManager recommendTaskManager;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    @Qualifier(value = "batchImportExecutor")
    private Executor batchImportExecutor;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createPushTaskFiveBalance(PushRecommendTaskExpandDTO dto) {
        if (dto == null) {
            return ReturnCode.EMPTY_PARAMETERS;
        }
        log.info("??????f5??????????????????:{}", JSONObject.toJSONString(dto));

        // ??????????????????
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
        BeanUtils.copyProperties(dto, recommendTaskEntity);

        // ???????????????poolInfo,snatpool???serviceInfo
        int rc = vaildateAndBuildParam(dto, recommendTaskEntity);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return rc;
        }

        //??????????????????
        PushRecommendTaskExpandEntity taskEntity = new PushRecommendTaskExpandEntity();
        BeanUtils.copyProperties(dto, taskEntity);
        taskEntity.setCreateTime(new Date());
        taskEntity.setSnatPoolInfo(null == dto.getSnatPoolInfo() ? "" : JSONObject.toJSONString(dto.getSnatPoolInfo()));
        taskEntity.setPoolInfo(null == dto.getPoolInfo() ? "" : JSONObject.toJSONString(dto.getPoolInfo()));

        if(StringUtils.isBlank(recommendTaskEntity.getTheme())){
            String protocol = dto.getServiceInfo().getProtocol();
            String port = StringUtils.isNotBlank(dto.getServiceInfo().getDstPorts()) ? dto.getServiceInfo().getDstPorts() : "any";
            // ??????vs??????
            String protocolStr = StringUtils.isBlank(ProtocolEnum.getDescByCode(protocol)) ? "any":ProtocolEnum.getDescByCode(protocol).toLowerCase();
            recommendTaskEntity.setTheme(String.format("vs_%s_%s_%s", dto.getDstIp(), port, protocolStr));
        }

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

        // ????????????
        SceneForFiveBalanceDTO sceneDto = null;
        if(null == dto.getSceneForFiveBalanceDTO()){
            // ??????vs????????????(???????????????sceneuuid??????)
            SceneForFiveBalanceDTO sceneForFiveBalanceDTO = new SceneForFiveBalanceDTO();
            sceneForFiveBalanceDTO.setSceneUuid(taskEntity.getSceneUuid());
            sceneDto = sceneForFiveBalanceService.getSceneForFiveBalance(sceneForFiveBalanceDTO);
            if (null == sceneDto) {
                log.info("????????????uuid??????????????????,??????????????????");
                return ReturnCode.SCENE_NOT_EXIST;
            }
        }else{
            // ????????????dto?????? ??????????????????(??????????????????)
            // ?????????????????????????????????scene
            SceneForFiveBalanceDTO queryNameDto = new SceneForFiveBalanceDTO();
            queryNameDto.setSceneName(dto.getSceneForFiveBalanceDTO().getSceneName());
            SceneForFiveBalanceDTO resutlDTO = sceneForFiveBalanceService.getSceneForFiveBalance(queryNameDto);
            if(null != resutlDTO){
                sceneDto = resutlDTO;
                taskEntity.setSceneUuid(resutlDTO.getSceneUuid());
            }else{
                sceneForFiveBalanceService.createSceneForFiveBalance(dto.getSceneForFiveBalanceDTO());
                sceneDto = dto.getSceneForFiveBalanceDTO();
                taskEntity.setSceneUuid(dto.getSceneForFiveBalanceDTO().getSceneUuid());
            }
        }



        Integer taskType = 0;
        if (null == taskEntity.getSnatType() || !PushSnatType.SNAT.getCode().equals(taskEntity.getSnatType())) {
            taskType = PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT;
        } else {
            taskType = PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT;
        }
        recommendTaskEntity.setTaskType(taskType);
        taskEntity.setTaskType(taskType);

        List<RecommendTaskEntity> list = new ArrayList<>();
        // ??????F5?????????ipv4???ip??????
        recommendTaskEntity.setIpType(IpTypeEnum.IPV4.getCode());
        list.add(recommendTaskEntity);
        taskService.insertRecommendTaskList(list);
        taskEntity.setTaskId(list.get(0).getId());
        pushRecommendTaskExpandMapper.add(taskEntity);

        String message = String.format("??????????????????%s??????", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        // ?????????????????????
        int result = addCommandEditableEntityTask(taskEntity, recommendTaskEntity, sceneDto,taskType);
        return result;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePushTaskFiveBalance(String ids) {
        if(StringUtils.isBlank(ids)){
            return 0;
        }
        // ???????????????????????????id????????????id
        List<Integer> taskIds = pushRecommendTaskExpandMapper.getTaskIdByIds(ids);
        if(CollectionUtils.isEmpty(taskIds)){
            return 0;
        }
        // ????????????????????? ???????????????????????????
        taskService.deleteTasks(taskIds,0);
        // ???????????????????????????
        return pushRecommendTaskExpandMapper.delete(ids);
    }

    @Override
    public int updatePushTaskFiveBalance(PushRecommendTaskExpandDTO dto) {
        return 0;
    }

    @Override
    public PushRecommendTaskExpandDTO getPushTaskFiveBalance(PushRecommendTaskExpandDTO dto) {
        return null;
    }

    @Override
    public PageInfo<PushRecommendTaskExpandDTO> findPushTaskFiveBalancePage(PushRecommendTaskExpandDTO dto) {
        String branchLevel = remoteBranchService.likeBranch(dto.getCreateUser());
        // ????????????
        String status = null == dto.getTaskStatus() ? null : String.valueOf(dto.getTaskStatus());
        List<RecommendTaskEntity> list = searchTaskList(dto.getTheme(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT, PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT, status,
                dto.getCurrentPage(), dto.getPageSize(), branchLevel);

        PageInfo<RecommendTaskEntity> originalPageInfo = new PageInfo<>(list);

        List<PushRecommendTaskExpandDTO> expandDTOList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PushRecommendTaskExpandDTO expandDTO = new PushRecommendTaskExpandDTO();
            BeanUtils.copyProperties(entity, expandDTO);
            // ??????????????????
            if (StringUtils.isNotBlank(entity.getServiceList())) {
                JSONArray array = JSONObject.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                if (serviceList.size() > 0) {
                    ServiceDTO serviceDTO = serviceList.get(0);
                    expandDTO.setServiceInfo(serviceDTO);
                }
            }
            expandDTO.setCreateUser(entity.getUserName());
            expandDTO.setCreateTime(entity.getCreateTime());
            expandDTO.setTaskStatus(entity.getStatus());
            expandDTO.setTaskId(entity.getId());
            PushRecommendTaskExpandEntity expandEntity = pushRecommendTaskExpandMapper.getByTaskId(entity.getId());
            if (null == expandEntity) {
                log.info("????????????id:{}????????????????????????", entity.getId());
                continue;
            }

            String deviceIp = "????????????";
            //??????uuid
            String deviceUuid = expandEntity.getDeviceUuid();
            if (StringUtils.isNotBlank(deviceUuid)) {
                deviceIp = String.format("????????????(%s)", deviceUuid);
                if (deviceUuid != null) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                    if (nodeEntity != null) {
                        deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }
                }
            }
            expandDTO.setDeviceUuid(expandEntity.getDeviceUuid());
            expandDTO.setDeviceName(deviceIp);
            expandDTO.setId(expandEntity.getId());
            expandDTO.setSceneUuid(expandEntity.getSceneUuid());
            expandDTO.setSceneName(expandEntity.getSceneName());
            expandDTO.setTaskType(expandEntity.getTaskType());
            expandDTO.setSnatType(expandEntity.getSnatType());
            expandDTO.setHttpProfile(expandEntity.getHttpProfile());
            expandDTO.setSslProfile(expandEntity.getSslProfile());
            expandDTO.setMark(expandEntity.getMark());
            if (StringUtils.isNotBlank(expandEntity.getSnatPoolInfo())) {
                expandDTO.setSnatPoolInfo(JSONObject.toJavaObject(JSONObject.parseObject(expandEntity.getSnatPoolInfo()), PushSnatPoolInfo.class));
            }
            if (StringUtils.isNotBlank(expandEntity.getPoolInfo())) {
                expandDTO.setPoolInfo(JSONObject.toJavaObject(JSONObject.parseObject(expandEntity.getPoolInfo()), PushPoolInfo.class));
            }
            expandDTOList.add(expandDTO);
        }

        PageInfo<PushRecommendTaskExpandDTO> pageInfo = new PageInfo<>(expandDTOList);
        pageInfo.setTotal(originalPageInfo.getTotal());
        pageInfo.setStartRow(originalPageInfo.getStartRow());
        pageInfo.setEndRow(originalPageInfo.getEndRow());
        pageInfo.setPageSize(originalPageInfo.getPageSize());
        pageInfo.setPageNum(originalPageInfo.getPageNum());
        return pageInfo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchImportFivePolicy(MultipartFile file, Authentication auth) {
        int failureNum = 0;
        int rowNum = 1;
        StringBuilder failureMsg = new StringBuilder();
        String errmsg = "??????????????????";
        List<PushRecommendTaskExpandDTO> taskList = new ArrayList<>();
        List<SceneForFiveBalanceDTO> sceneList = new ArrayList<>();
        try (TotemsExcelImport policySheet = new TotemsExcelImport(file, 1, 0);
             TotemsExcelImport sceneSheet = new TotemsExcelImport(file, 1, 1)) {
            // 1.??????F5????????????sheet???
            List<PushTaskFiveBalanceImportDTO> list = policySheet.getDataList(PushTaskFiveBalanceImportDTO.class);
            // ??????????????????
            failureNum = validateImportDTO(failureNum, rowNum, failureMsg, list);
            if (failureMsg.length() > 1) {
                failureMsg.insert(0, "????????????F5???????????? " + failureNum + "??????????????? ?????????????????????<br>");
                return failureMsg.toString();
            }
            for (PushTaskFiveBalanceImportDTO dto : list) {
                PushRecommendTaskExpandDTO taskFiveBalanceDTO = new PushRecommendTaskExpandDTO();
                taskFiveBalanceDTO.setDeviceUuid(dto.getDeviceUuid());
                taskFiveBalanceDTO.setDeviceName(dto.getDeviceName());
                taskFiveBalanceDTO.setDstIp(dto.getDstIp());

                String protocol = StringUtils.isNotBlank(dto.getPreProtocol()) ? dto.getPreProtocol().toLowerCase() : "any";

                String port = StringUtils.isNotBlank(dto.getPrePort()) ? dto.getPrePort() : "any";
                ServiceDTO serviceDTO = new ServiceDTO();
                if (PolicyConstants.POLICY_STR_VALUE_TCP.equalsIgnoreCase(protocol)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_TCP);
                } else if (PolicyConstants.POLICY_STR_VALUE_UDP.equalsIgnoreCase(protocol)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_UDP);
                } else {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
                }
                serviceDTO.setDstPorts(port);
                taskFiveBalanceDTO.setServiceInfo(serviceDTO);

                if (StringUtils.isNotBlank(dto.getPreProtocol())) {
                    // ??????vs??????
                    taskFiveBalanceDTO.setTheme(String.format("vs_%s_%s_%s", dto.getDstIp(), port, protocol));
                } else {
                    taskFiveBalanceDTO.setTheme(String.format("vs_%s_any", dto.getDstIp()));
                }

                // ??????pool??????
                PushPoolInfo pushPoolInfo = new PushPoolInfo();
                List<PoolDetailInfo> poolDetailInfos = new ArrayList<>();
                // ???????????????pool??????
                buildImportPoolInfo(dto, pushPoolInfo, poolDetailInfos);
                pushPoolInfo.setPoolDetailInfos(poolDetailInfos);
                // ????????????????????????????????????
                pushPoolInfo.setGroupPriorityType("Disabled");
                // ??????????????????snattype???2 ??????None
                taskFiveBalanceDTO.setSnatType(2);
                taskFiveBalanceDTO.setSceneName(dto.getApplySystemName());
                taskFiveBalanceDTO.setPoolInfo(pushPoolInfo);
                taskFiveBalanceDTO.setCreateUser(auth.getName());

                taskList.add(taskFiveBalanceDTO);
            }

            // 2.??????F5??????sheet???
            List<SceneForFiveBalanceImportDTO> sceneImportList = sceneSheet.getDataList(SceneForFiveBalanceImportDTO.class);
            // ?????????????????????
            for (SceneForFiveBalanceImportDTO dto : sceneImportList) {
                // ????????????
                dto.dealData();
                SceneForFiveBalanceDTO balanceDTO = new SceneForFiveBalanceDTO();
                BeanUtils.copyProperties(dto, balanceDTO);
                balanceDTO.setSceneName(dto.getApplySystemName());
                balanceDTO.setCreateUser(auth.getName());
                sceneList.add(balanceDTO);
            }
            if (failureMsg.length() > 1) {
                failureMsg.insert(0, "????????????F5???????????? " + failureNum + "??????????????? ?????????????????????<br>");
                return failureMsg.toString();
            }
            // 3.?????????????????????????????????
            saveSceneDataAndTaskData(sceneList, taskList);
        } catch (Exception e) {
            log.error("??????F5????????????????????????,????????????:{}", e);
            errmsg = e.getMessage();
        }
        return errmsg;
    }


    /**
     * ???????????????poolInfo??????
     * @param dto
     * @param pushPoolInfo
     * @param poolDetailInfos
     */
    private void buildImportPoolInfo(PushTaskFiveBalanceImportDTO dto, PushPoolInfo pushPoolInfo, List<PoolDetailInfo> poolDetailInfos) {
        String[] postDstIpAndPorts = dto.getPostDstIpAndPort().split("\n");
        String postDstIpAndPort = postDstIpAndPorts[0];
        String[] itemsDstIpAndPort = postDstIpAndPort.split(":");
        pushPoolInfo.setName(String.format("pool_member-%s_%s",itemsDstIpAndPort[0] , itemsDstIpAndPort.length == 2 ?  itemsDstIpAndPort[1]: "any"));

        for (String str : postDstIpAndPorts) {
            String[] items = str.split(":");
            PoolDetailInfo poolDetailInfo = new PoolDetailInfo();
            if(items.length ==2){
                poolDetailInfo.setPort(items[1]);
            }
            poolDetailInfo.setIp(items[0]);
            poolDetailInfos.add(poolDetailInfo);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveSceneDataAndTaskData(List<SceneForFiveBalanceDTO> sceneList, List<PushRecommendTaskExpandDTO> taskList) {

        String id = "batch_import_" + DateUtil.getTimeStamp();
        batchImportExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "????????????F5??????", "", new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                for (PushRecommendTaskExpandDTO taskFiveBalanceDTO : taskList) {
                    // ????????????
                    for (SceneForFiveBalanceDTO sceneForFiveBalanceDTO : sceneList) {
                        if(taskFiveBalanceDTO.getSceneName().equals(sceneForFiveBalanceDTO.getSceneName())){
                            // ??????F5????????????
                            taskFiveBalanceDTO.setSceneForFiveBalanceDTO(sceneForFiveBalanceDTO);
                            createPushTaskFiveBalance(taskFiveBalanceDTO);
                            break;
                        }
                    }

                }
            }
        });
    }


    /**
     * ??????F5????????????DTO
     *
     * @param failureNum
     * @param rowNum
     * @param failureMsg
     * @param list
     * @return
     */
    private int validateImportDTO(int failureNum, int rowNum, StringBuilder failureMsg, List<PushTaskFiveBalanceImportDTO> list) {
        for (PushTaskFiveBalanceImportDTO dto : list) {
            try {
                // ????????????
                int rc = dto.validation();
                rowNum++;
                if (ReturnCode.POLICY_MSG_OK != rc) {
                    failureMsg.append(String.format("F5 NAT??????????????????%d?????????,????????????????????? %s", rowNum, ReturnCode.getMsg(rc)));
                    failureNum++;
                    continue;
                }

                // ??????????????????ip????????????
                NodeEntity node = recommendTaskManager.getDeviceByManageIp(dto.getDeviceIp());
                if (null == node) {
                    failureMsg.append(String.format("F5 NAT??????????????????%d?????????????????????IP????????????<br>", rowNum));
                    failureNum++;
                    continue;
                }
                // ?????????????????????????????????
                if (null != node) {
                    DeviceRO device = whaleManager.getDeviceByUuid(node.getUuid());
                    if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                        failureMsg.append(String.format("F5 NAT??????????????????%d???????????????????????????????????????????????????<br>", rowNum));
                        failureNum++;
                        continue;
                    }
                    dto.setDeviceUuid(node.getUuid());
                    dto.setDeviceName(node.getDeviceName());
                }
                if(StringUtils.isBlank(dto.getApplySystemName())){
                    failureMsg.append(String.format("F5 NAT??????????????????%d?????????????????????,??????????????????", rowNum));
                    failureNum++;
                    continue;
                }else{
                    dto.setApplySystemName(dto.getApplySystemName().trim());
                }

            } catch (Exception e) {
                log.error("????????????F5 NAT????????????", e);
                failureMsg.append(String.format("F5 NAT?????????%d??????????????????<br>", rowNum));
                failureNum++;
            }
        }
        return failureNum;
    }


    /**
     * ???????????????????????? ??????????????????
     *
     * @param taskEntity
     * @return
     */
    public int addCommandEditableEntityTask(PushRecommendTaskExpandEntity taskEntity, RecommendTaskEntity recommendTaskEntity, SceneForFiveBalanceDTO sceneDto,Integer taskType) {
        // ????????????nat,??????????????????taskType???Dnat,??????????????????bothNat
        PolicyEnum policyType = null;
        if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT == taskType) {
            policyType = PolicyEnum.F5_BOTH_NAT;
        } else {
            policyType = PolicyEnum.F5_DNAT;
        }
        CommandTaskEditableEntity commandEntity = EntityUtils.createCommandTask(taskType,
                taskEntity.getTaskId(), taskEntity.getCreateUser(), recommendTaskEntity.getTheme(), taskEntity.getDeviceUuid());
        commandEntity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        // ?????????????????????
        commandTaskManager.addCommandEditableEntityTask(commandEntity);

        boolean isVsys = false;
        String vsysName = "";
        NodeEntity node = taskService.getTheNodeByUuid(taskEntity.getDeviceUuid());
        if(node == null) {
            log.error("??????UUID:{}????????????????????????", taskEntity.getDeviceUuid());
            return ReturnCode.EMPTY_DEVICE_INFO;
        } else {
            DeviceRO device = whaleManager.getDeviceByUuid(taskEntity.getDeviceUuid());
            DeviceDataRO deviceData = device.getData().get(0);
            if(deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                vsysName = deviceData.getVsysName();
            }
        }

        // ??????cmdDto ???????????????
        CmdDTO cmdDTO = EntityUtils.createCmdDTOFiveBalance(taskEntity, recommendTaskEntity, commandEntity, sceneDto, policyType,isVsys,vsysName);
        GenerateCommandDTO generateCommandDTO = commandlineService.generateCommandForFiveBalance(cmdDTO);

        // ??????????????????
        commandlineService.updateCommandStatus(commandEntity.getId(), generateCommandDTO.getCommandline(), generateCommandDTO.getRollbackCommandline(),
                PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START, null, null);
        return ReturnCode.POLICY_MSG_OK;
    }


    /**
     * ?????????????????????
     *
     * @param dto
     * @param recommendTaskEntity
     */
    private int vaildateAndBuildParam(PushRecommendTaskExpandDTO dto, RecommendTaskEntity recommendTaskEntity) {

        //????????????
        int rc = 0;
        if (StringUtils.isBlank(dto.getSrcIp())) {
            recommendTaskEntity.setSrcIp(PolicyConstants.IPV4_ANY);
        }
        // ???ip????????????ip????????????
        if (StringUtils.isNotBlank(dto.getSrcIp()) && !IpUtils.isIP(dto.getSrcIp()) && !IpUtils.isIPSegment(dto.getSrcIp())) {
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
        }
        // ??????ip????????????
        if(StringUtils.isBlank(dto.getDstIp())){
            rc = ReturnCode.POLICY_MSG_EMPTY_VALUE;
        }
        // ??????ip????????????ip
        if (StringUtils.isNotBlank(dto.getDstIp()) && !IpUtils.isIP(dto.getDstIp())) {
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
        }
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return rc;
        }

        // 1. pool??????
        PushPoolInfo pushPoolInfo = dto.getPoolInfo();
        if (null != dto.getPoolInfo()) {
            List<PoolDetailInfo> poolDetailInfos = pushPoolInfo.getPoolDetailInfos();
            if (CollectionUtils.isEmpty(poolDetailInfos)) {
                log.info("???????????????pool???????????????");
                return ReturnCode.EMPTY_PARAMETERS;
            }
            // ???????????????pool??????
            for (PoolDetailInfo poolDetailInfo : poolDetailInfos) {
                // ????????????ip
                if (StringUtils.isNotBlank(poolDetailInfo.getIp()) && !IpUtils.isIP(poolDetailInfo.getIp())) {
                    rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
                }
                // ??????????????????
                if (StringUtils.isNotBlank(poolDetailInfo.getPort()) && !PortUtils.isValidPort(poolDetailInfo.getPort())) {
                    rc = ReturnCode.POLICY_MSG_INVALID_POST_VALUE;
                }
                if (ReturnCode.POLICY_MSG_OK != rc) {
                    return rc;
                }
                // ????????????????????????????????????any
                if (StringUtils.isBlank(poolDetailInfo.getPort())){
                    poolDetailInfo.setPort(PolicyConstants.POLICY_STR_VALUE_ANY);
                }
            }
        }

        // 2. snatPool??????
        PushSnatPoolInfo snatPoolInfo = dto.getSnatPoolInfo();
        if (null != snatPoolInfo) {
            // ?????????????????????????????????????????????????????????????????????????????????
            if (!snatPoolInfo.isQuote()) {
                String poolIps = snatPoolInfo.getSnatPoolIp();
                if (StringUtils.isBlank(poolIps)) {
                    log.info("???????????????Snatpool???????????????");
                    return ReturnCode.EMPTY_PARAMETERS;
                }
                String[] ips = poolIps.split(",");
                for (String ip : ips) {
                    // ????????????ip
                    if (StringUtils.isNotBlank(ip) && !IpUtils.isIP(ip)) {
                        return ReturnCode.POLICY_MSG_INVALID_FORMAT;
                    }
                }
            }
        }

        // 3.??????????????????
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        ServiceDTO serviceDTO = dto.getServiceInfo();
        if (null != serviceDTO) {
            serviceDTO.setProtocol(serviceDTO.getProtocol());
            serviceDTO.setDstPorts(serviceDTO.getDstPorts());
            serviceDTOList.add(serviceDTO);
        } else {
            ServiceDTO serviceDTO1 = new ServiceDTO();
            serviceDTO1.setProtocol(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO1.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTOList.add(serviceDTO1);
        }
        recommendTaskEntity.setServiceList(JSONObject.toJSONString(serviceDTOList));
        return ReturnCode.POLICY_MSG_OK;
    }

    private List<RecommendTaskEntity> searchTaskList(String theme, Integer minTaskType, Integer maxTaskType, String status, int page, int psize, String branchLevel) {


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
        if (null != maxTaskType) {
            params.put("maxTaskType", maxTaskType);
        }
        if (null != minTaskType) {
            params.put("minTaskType", minTaskType);
        }

        List<RecommendTaskEntity> list = recommendTaskMapper.searchTask(params);
        return list;
    }
}
