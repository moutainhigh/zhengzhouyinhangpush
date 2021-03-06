package com.abtnetworks.totems.recommend.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.config.VmwareInterfaceStatusConfig;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.RecommendTypeEnum;
import com.abtnetworks.totems.common.exception.RecommendArgumentException;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.StringUtils;
import com.abtnetworks.totems.common.vo.ResultResponseVO;
import com.abtnetworks.totems.recommend.dto.global.VmwareSdnBusinessDTO;
import com.abtnetworks.totems.recommend.dto.task.SearchRecommendTaskDTO;
import com.abtnetworks.totems.recommend.entity.*;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.GlobalRecommendService;
import com.abtnetworks.totems.recommend.service.RecommendBussCommonService;
import com.abtnetworks.totems.recommend.vo.BatchTaskVO;
import com.abtnetworks.totems.recommend.vo.PushTaskDetailVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.CommonConstants.HOUR_SECOND;

/**
 * @author Administrator
 * @Title:
 * @Description: ???????????????????????????????????????????????????????????????????????????????????????(????????????????????????????????????????????????)
 * @date 2021/1/7
 */
@Slf4j
@Api(value = "????????????????????????????????????????????????")
@RestController
@RequestMapping(value = "/recommend/")
public class RecommendBussExtendController extends BaseController {

    @Resource
    RemoteBranchService remoteBranchService;

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    LogClientSimple logClientSimple;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    RecommendBussCommonService recommendBussCommonService;



    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "batchId", value = "????????????id", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "orderNumber", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "userName", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "??????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "?????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "protocol", value = "??????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstPort", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "taskType", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "Integer")
    })
    @PostMapping("task/searchtasklist.action")
    public ResultResponseVO getTaskList(@RequestBody SearchRecommendTaskDTO searchRecommendTaskDTO, Authentication authentication) {
        String protocolString = searchRecommendTaskDTO.getProtocol();
        searchRecommendTaskDTO.setAuthentication(authentication);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(protocolString)) {
            if (!protocolString.equals("0")) {
                ServiceDTO serviceDTO = new ServiceDTO();
                serviceDTO.setProtocol(protocolString);
                if (org.apache.commons.lang3.StringUtils.isNotBlank(searchRecommendTaskDTO.getDstPort())) {
                    serviceDTO.setDstPorts(searchRecommendTaskDTO.getDstPort());
                }
                String jsonString = JSONObject.toJSONString(serviceDTO);
                protocolString = org.apache.commons.lang3.StringUtils.strip(jsonString, "{}");
                searchRecommendTaskDTO.setProtocol(protocolString);
            }
        }

        PageInfo<RecommendTaskEntity> pageInfo = policyRecommendTaskService.getTaskList(searchRecommendTaskDTO);

        return returnResponseSuccess(pageInfo);
    }

    @ApiOperation("new ????????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "userName", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "??????????????????", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "??????????????????", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "String")
    })
    @PostMapping("task/searchbatchlist")
    public JSONObject getbatchList(String theme, String userName, Long taskStart, Long taskEnd, int page, int psize) {
        PageInfo<BatchTaskVO> pageInfo = policyRecommendTaskService.searchBatchTaskList(theme, userName, page, psize);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    /*****************************???????????????***********************/
    @ApiOperation("new ???????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "???IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "??????IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "entrySubnet", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "exitSubnet", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "?????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "??????????????????", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "??????????????????", required = false, dataType = "Long"),
    })
    @PostMapping("task/addinternat")
    public JSONObject addInternatRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        log.debug("?????????????????????????????????Start??????{}", JSONObject.toJSONString(entity));
        int rc = ReturnCode.POLICY_MSG_OK;
        try {
            //????????????
            validatorParam(entity);

            List<ServiceDTO> serviceList = entity.getServiceList();
            if (CollectionUtils.isNotEmpty(serviceList)) {
                for (ServiceDTO service : serviceList) {
                    if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                        service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                    }
                }
            }
            RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
            InternetAdditionalInfoEntity additionalInfoEntity = new InternetAdditionalInfoEntity();

            //??????????????????
            BeanUtils.copyProperties(entity, recommendTaskEntity);
            //?????????????????????
            recommendTaskEntity.setSrcIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getSrcIpSystem()));
            recommendTaskEntity.setDstIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getDstIpSystem()));

            //?????????????????????????????????????????????, ????????????????????????any
            recommendTaskEntity.setServiceList(entity.getServiceList() == null ? null : JSONObject.toJSONString(entity.getServiceList()));
            UserInfoDTO userInfoDTO = remoteBranchService.findOne(auth.getName());
            if (userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
                recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
            } else {
                recommendTaskEntity.setBranchLevel("00");
            }
            //????????????????????????????????????????????????
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "A" + simpleDateFormat.format(date);
            recommendTaskEntity.setCreateTime(date);
            recommendTaskEntity.setOrderNumber(orderNumber);

            //???????????????
            recommendTaskEntity.setUserName(auth.getName());
            recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            if (entity.getIdleTimeout() != null) {
                recommendTaskEntity.setIdleTimeout(entity.getIdleTimeout() * HOUR_SECOND);
            } else {
                recommendTaskEntity.setIdleTimeout(null);
            }
            //???????????????????????????
            recommendTaskEntity.setTaskType(entity.getTaskType());
            recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);
            // ?????????????????????nat
            int returnCode = recommendBussCommonService.checkPostRelevancyNat(recommendTaskEntity, auth);
            if (ReturnCode.POLICY_MSG_OK != returnCode) {
                return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(returnCode));
            }
            List<RecommendTaskEntity> list = new ArrayList<>();
            list.add(recommendTaskEntity);
            policyRecommendTaskService.insertRecommendTaskList(list);
            recommendBussCommonService.updateRelevanceNatTaskId(list);
            String message = String.format("??????????????????????????????%s ??????", recommendTaskEntity.getTheme());
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
            List<Integer> collect = list.stream().map(r -> r.getId()).collect(Collectors.toList());
            JSONObject returnJSON = getReturnJSON(rc, "");
            returnJSON.put("taskId",collect);
            return returnJSON;
        }catch (IllegalArgumentException e){
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            log.error("?????????????????????????????????End????????????",e);
        }

        return getReturnJSON(rc);
    }
    @Autowired
    GlobalRecommendService globalRecommendService;
    @Autowired
    VmwareInterfaceStatusConfig vmwareInterfaceStatusConfig;

    @PostMapping("task/addGlobalinternat")
    public JSONObject addGlobalinternat(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        if(!vmwareInterfaceStatusConfig.checkVmInterfaceAvailableNow() ||StringUtils.isEmpty(entity.getSrcIp())||StringUtils.isEmpty(entity.getDstIp())){
            return addInternatRecommendTask(entity,auth);
        }
        log.debug("?????????????????????????????????Start??????{}",JSONObject.toJSONString(entity));
        int rc = ReturnCode.POLICY_MSG_OK;
        try{
            //????????????
            validatorParam(entity);
            List<ServiceDTO> serviceList = entity.getServiceList();
            if (CollectionUtils.isNotEmpty(serviceList)) {
                for (ServiceDTO service : serviceList) {
                    if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                        service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                    }
                }
            }
            RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
            InternetAdditionalInfoEntity additionalInfoEntity = new InternetAdditionalInfoEntity();

            //??????????????????
            BeanUtils.copyProperties(entity, recommendTaskEntity);
            //?????????????????????
            recommendTaskEntity.setSrcIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getSrcIpSystem()));
            recommendTaskEntity.setDstIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getDstIpSystem()));

            //?????????????????????????????????????????????, ????????????????????????any
            recommendTaskEntity.setServiceList(entity.getServiceList() == null ? null : JSONObject.toJSONString(entity.getServiceList()));
            UserInfoDTO userInfoDTO = remoteBranchService.findOne(auth.getName());
            if (userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
                recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
            } else {
                recommendTaskEntity.setBranchLevel("00");
            }
            //????????????????????????????????????????????????
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "A" + simpleDateFormat.format(date);
            recommendTaskEntity.setCreateTime(date);
            recommendTaskEntity.setOrderNumber(orderNumber);

            //???????????????
            recommendTaskEntity.setUserName(auth.getName());
            recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            if (entity.getIdleTimeout() != null) {
                recommendTaskEntity.setIdleTimeout(entity.getIdleTimeout() * HOUR_SECOND);
            } else {
                recommendTaskEntity.setIdleTimeout(null);
            }
            //???????????????????????????
            recommendTaskEntity.setTaskType(entity.getTaskType());
            recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);
            // ?????????????????????nat
            int returnCode = recommendBussCommonService.checkPostRelevancyNat(recommendTaskEntity, auth);
            if (ReturnCode.POLICY_MSG_OK != returnCode) {
                return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(returnCode));
            }
            //??????+??????
            globalRecommendService.addGlobalinternat(entity,recommendTaskEntity);
            String message = String.format("??????????????????????????????%s ??????", recommendTaskEntity.getTheme());
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
            JSONObject returnJSON = getReturnJSON(rc, "");
            returnJSON.put("taskId",Arrays.asList(recommendTaskEntity.getId()));
            return returnJSON;
        }catch (IllegalArgumentException e){
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            log.error("?????????????????????????????????End????????????",e);
        }catch (RecommendArgumentException e){
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            log.warn(e.getMessage());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status",-1);
            jsonObject.put("errcode",rc);
            jsonObject.put("errmsg",e.getMessage());
            return jsonObject;
        }
        return getReturnJSON(rc);
    }


    @ApiOperation("new ??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id??????", required = true, dataType = "String")
    })
    @PostMapping("task/deletetask")
    public JSONObject deleteTask(String ids) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            log.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            log.error("???????????????????????????", e);
        }

        //??????????????????????????????????????????????????????????????????????????????
        StringBuilder errMsg = new StringBuilder();
        int rc = ReturnCode.POLICY_MSG_OK;
        List<String> themeList = new ArrayList<>();
        int tasking = 0;
        List<Integer> weTaskIds = new ArrayList<>();
        for (int id : idList) {
            log.info(String.format("????????????(%d)", id));
            RecommendTaskEntity taskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            if(taskEntity==null){
                continue;
            }
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(id);
            //int taskStatus = taskEntity.getStatus();
            themeList.add(taskEntity.getTheme());
            if (taskEntityList != null && taskEntityList.size() > 0) {
                if (taskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE ||
                        taskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATING ||
                        taskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_VERIFYING ||
                        taskEntityList.get(0).getPushStatus() == PolicyConstants.PUSH_STATUS_PUSHING ||
                        taskEntityList.get(0).getRevertStatus() == PolicyConstants.REVERT_STATUS_REVERTING) {
                    tasking++;
                    rc = ReturnCode.CAN_NOT_DELETE_RUNNING_TASK;
                    errMsg.append(taskEntity.getTheme() + ",");
                }
            } else if (taskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE ||
                    taskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATING ||
                    taskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_VERIFYING) {
                tasking++;
                rc = ReturnCode.CAN_NOT_DELETE_RUNNING_TASK;
                errMsg.append(taskEntity.getTheme() + ",");
            }

            if (taskEntity.getWeTaskId() != null && vmwareInterfaceStatusConfig.isVmInterfaceAvailable()) {
                VmwareSdnBusinessDTO weTask = globalRecommendService.getWETaskByWETaskId(taskEntity.getWeTaskId());
                if (weTask != null) {
                    if (weTask.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATING || weTask.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE) {
                        tasking++;
                        rc = ReturnCode.CAN_NOT_DELETE_RUNNING_TASK;
                        errMsg.append(taskEntity.getTheme() + ",");
                    } else {
                        weTaskIds.add(weTask.getId());
                    }
                }
            }

        }

        if(tasking>0){
            errMsg.deleteCharAt(errMsg.length()-1);
            errMsg.append(":" + ReturnCode.getMsg(rc));
        }

        if (rc != ReturnCode.POLICY_MSG_OK) {
            return getReturnJSON(rc, errMsg.toString());
        }
        try{
            int type = 0;
            policyRecommendTaskService.deleteTasks(idList,type);
            if(vmwareInterfaceStatusConfig.isVmInterfaceAvailable()){
                globalRecommendService.deleteWeTasks(weTaskIds);
            }
        }catch (Exception e){
            logClientSimple.addBusinessLog(LogLevel.ERROR.getId(), BusinessLogType.POLICY_PUSH.getId(), String.format("????????????: %s ??????",org.apache.commons.lang3.StringUtils.join(themeList, ",")));
            return getReturnJSON(ReturnCode.FAILED);
        }

        String message = String.format("???????????????????????????%s ??????", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }

    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "??????????????????id", required = true, dataType = "String")
    })
    @PostMapping("task/gettaskbyid")
    public JSONObject getTaskById(String taskId) {
        int taskIdNum = Integer.valueOf(taskId);
        RecommendTaskEntity recommendTaskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskIdNum);
        PushTaskDetailVO pushTaskDetailVO = new PushTaskDetailVO();

        BeanUtils.copyProperties(recommendTaskEntity, pushTaskDetailVO);
        if (recommendTaskEntity.getTaskType() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED) {
            String additionInfo = recommendTaskEntity.getAdditionInfo();
            JSONObject object = JSONObject.parseObject(additionInfo);
            PushAdditionalInfoEntity pushAdditionalInfoEntity = object.toJavaObject(PushAdditionalInfoEntity.class);
            String deviceUuid = pushAdditionalInfoEntity.getDeviceUuid();
            if (deviceUuid != null) {
                NodeEntity nodeEntity = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
                if (nodeEntity != null) {
                    pushTaskDetailVO.setDeviceName(nodeEntity.getDeviceName());
                }
            }
            pushTaskDetailVO.setSrcZone(pushAdditionalInfoEntity.getSrcZone());
            pushTaskDetailVO.setDstZone(pushAdditionalInfoEntity.getDstZone());
            pushTaskDetailVO.setInDevItf(pushAdditionalInfoEntity.getInDevItf());
            pushTaskDetailVO.setOutDevItf(pushAdditionalInfoEntity.getOutDevItf());
            pushTaskDetailVO.setAction(pushAdditionalInfoEntity.getAction());
            pushTaskDetailVO.setCreateTime(recommendTaskEntity.getCreateTime());
        }

        String serviceListString = recommendTaskEntity.getServiceList();
        if (!AliStringUtils.isEmpty(serviceListString)) {
            JSONArray jsonArray = JSONArray.parseArray(serviceListString);
            List<ServiceDTO> serviceList = jsonArray.toJavaList(ServiceDTO.class);
            for (ServiceDTO serviceDTO : serviceList) {
                serviceDTO.setProtocol(ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                serviceDTO.setSrcPorts(null);
                if (serviceDTO.getDstPorts() != null) {
                    if (serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        serviceDTO.setDstPorts(null);
                    }
                }
            }
            pushTaskDetailVO.setServiceList(JSONObject.toJSONString(serviceList));
        }

        String jsonObjectString = JSONObject.toJSONString(pushTaskDetailVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }
    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "id", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "???IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "??????IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "?????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "??????????????????", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "??????????????????", required = false, dataType = "Long"),
    })
    @PostMapping("task/edit")
    public JSONObject editRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        String idString = entity.getId();
        if (AliStringUtils.isEmpty(idString)) {
            return getReturnJSON(ReturnCode.INVALID_ENTITY_ID);
        }

        List<ServiceDTO> serviceList = entity.getServiceList();

        if (serviceList != null) {
            Set<String> serviceSet = new HashSet<>();
            for (ServiceDTO serviceDTO : serviceList) {
                if (serviceSet.contains(serviceDTO.getProtocol())) {
                    return getReturnJSON(ReturnCode.FAILED, "?????????????????????????????????????????????");
                } else {
                    serviceSet.add(serviceDTO.getProtocol());
                }
            }
        }

        //??????????????????
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();

        boolean invalidId = false;
        try {
            int taskId = Integer.valueOf(idString);
            recommendTaskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskId);
        } catch (Exception e) {
            logger.error("ID????????????", e);
            invalidId = true;
        }

        if (invalidId) {
            return getReturnJSON(ReturnCode.INVALID_ENTITY_ID);
        }

        if (recommendTaskEntity == null) {
            return getReturnJSON(ReturnCode.TASK_IS_DELETED);
        }

        if (recommendTaskEntity.getStatus() != PolicyConstants.POLICY_INT_STATUS_INITIAL &&
                recommendTaskEntity.getStatus() != PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR &&
                recommendTaskEntity.getStatus() != PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE) {
            return getReturnJSON(ReturnCode.TASK_STATUS_ERROR);
        }

        if (!auth.getName().equals(recommendTaskEntity.getUserName())) {
            return getReturnJSON(ReturnCode.INVALID_USER);
        }


        if (serviceList != null) {
            for (ServiceDTO service : serviceList) {
                if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                    service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                }
            }
        }

        recommendTaskEntity.setTheme(entity.getTheme());
        recommendTaskEntity.setDescription(entity.getDescription());
        recommendTaskEntity.setRemarks(entity.getRemarks());

        recommendTaskEntity.setSrcIp(entity.getSrcIp());
        recommendTaskEntity.setDstIp(entity.getDstIp());

        //????????????????????????????????????????????????
        if (!AliStringUtils.isEmpty(entity.getEntrySubnet()) || !AliStringUtils.isEmpty(entity.getExitSubnet())) {
            InternetAdditionalInfoEntity additionalInfoEntity = new InternetAdditionalInfoEntity();
            JSONArray dstArray = JSONObject.parseArray(entity.getExitSubnet());
            List<SubnetEntity> exitSubnetList = dstArray.toJavaList(SubnetEntity.class);
            additionalInfoEntity.setExitSubnetList(exitSubnetList);
            recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        }

        //?????????????????????????????????????????????, ????????????????????????any
        recommendTaskEntity.setServiceList(entity.getServiceList() == null ? null : JSONObject.toJSONString(entity.getServiceList()));

        recommendTaskEntity.setStartTime(entity.getStartTime());
        recommendTaskEntity.setEndTime(entity.getEndTime());

        if (entity.getIdleTimeout() != null) {
            recommendTaskEntity.setIdleTimeout(entity.getIdleTimeout() * HOUR_SECOND);
        } else {
            recommendTaskEntity.setIdleTimeout(null);
        }
        recommendTaskEntity.setSrcIpSystem(entity.getSrcIpSystem());
        recommendTaskEntity.setDstIpSystem(entity.getDstIpSystem());
        recommendTaskEntity.setPostSrcIp(entity.getPostSrcIp());
        recommendTaskEntity.setPostDstIp(entity.getPostDstIp());
        recommendTaskEntity.setStartLabel(entity.getStartLabel());
        recommendTaskEntity.setLabelModel(entity.getLabelModel());
        recommendTaskEntity.setTaskType(entity.getTaskType());
        recommendTaskEntity.setIpType(entity.getIpType());
        String originalRelevancyNat  = recommendTaskEntity.getRelevancyNat();
        recommendTaskEntity.setRelevancyNat(buildRelevancyNat(entity.getRelevancyNat()));
        String newlRelevancyNat  = recommendTaskEntity.getRelevancyNat();
        recommendTaskEntity.setBeforeConflict(entity.isBeforeConflict());
        recommendTaskEntity.setMergeCheck(entity.isMergeCheck());
        // ??????????????????nat???????????????????????????????????? ???????????????
        int returnCode = recommendBussCommonService.checkPostRelevancyNat(recommendTaskEntity, auth);
        if (ReturnCode.POLICY_MSG_OK != returnCode) {
            return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(returnCode));
        }
        // KSH-5812  ???????????????????????????????????????????????????????????????????????????    ????????????????????????/???????????? ??????????????????????????????????????????????????????
        if (recommendTaskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR ||
                recommendTaskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE) {
            int type = 1;
            List<Integer> taskIds = new ArrayList<>();
            taskIds.add(recommendTaskEntity.getId());
            policyRecommendTaskService.deleteTasks(taskIds,type);
        }

        if (entity.getIpType() == 0) {
            if (vmwareInterfaceStatusConfig.isVmInterfaceAvailable()) {
                //???????????????????????????
                try {
                    entity.setWeTaskId(recommendTaskEntity.getWeTaskId());
                    globalRecommendService.editWETask(entity);
                    recommendTaskEntity.setWeTaskId(entity.getWeTaskId());
                    recommendTaskEntity.setTaskType(entity.getTaskType());
                } catch (Exception e) {
                    return getReturnJSON(ReturnCode.FAILED, "??????????????????????????????" + e.getMessage());
                }
            }
        } else {
            //???ip4????????????????????????
            if(recommendTaskEntity.getWeTaskId()!=null){
                recommendTaskEntity.setWeTaskId(null);
                policyRecommendTaskService.updateWeTaskId(recommendTaskEntity);
            }
        }
        // ??????????????????
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);
        policyRecommendTaskService.updateTaskById(recommendTaskEntity);
        // ????????????????????????
        buildRelevanceNat(recommendTaskEntity, originalRelevancyNat, newlRelevancyNat);
        int taskType = recommendTaskEntity.getTaskType().intValue();
        String taskTypeDesc = RecommendTypeEnum.getRecommendTypeByTypeCode(taskType).getDesc();
        String message = String.format("??????%s??????: %s ??????", taskTypeDesc, recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }


    /**
     * ???????????????????????????????????????????????????????????????
     * @param recommendTaskEntity
     * @param originalRelevancyNat
     * @param newlRelevancyNat
     */
    private void buildRelevanceNat(RecommendTaskEntity recommendTaskEntity, String originalRelevancyNat, String newlRelevancyNat) {
        // ????????????????????????????????????????????????
        recommendTaskEntity.setRelevancyNat(originalRelevancyNat);
        recommendBussCommonService.updateRelevancyNatTaskId(recommendTaskEntity,true);
        // ???????????????????????????????????????????????????
        recommendTaskEntity.setRelevancyNat(newlRelevancyNat);
        recommendBussCommonService.updateRelevancyNatTaskId(recommendTaskEntity,false);
    }

    /**
     * ????????????????????????nat json???,
     *
     * @param relevancyNat
     * @return
     */
    private static String buildRelevancyNat(String relevancyNat) {
        if (org.apache.commons.lang3.StringUtils.isBlank(relevancyNat)) {
            return null;
        }

        JSONArray jsonArray = JSONObject.parseArray(relevancyNat);
        if (jsonArray.size() == 0) {
            return null;
        }
        Iterator<Object> itemObj = jsonArray.iterator();
        while (itemObj.hasNext()) {
            JSONObject itemObject = (JSONObject)itemObj.next();
            if ("auto".equalsIgnoreCase(itemObject.getString("flag"))) {
                itemObj.remove();
            }
        }
        return jsonArray.toJSONString();
    }

}
