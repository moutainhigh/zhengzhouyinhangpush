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
 * @Description: 策略开通业务扩展类或者抽离增删改，避免上千行代码的维护问题(容易变动的或牵扯面少的先移动出来)
 * @date 2021/1/7
 */
@Slf4j
@Api(value = "策略开通扩展抽离页面增改查控制层")
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



    @ApiOperation("new 策略开通搜索列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "batchId", value = "批量任务id", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "orderNumber", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "userName", value = "用户名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "描述", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源地址", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的地址", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "protocol", value = "协议", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstPort", value = "目的端口", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "taskType", value = "类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "Integer")
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

    @ApiOperation("new 策略开通搜索批量管理列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "userName", value = "用户名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "策略开始时间", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "策略结束时间", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "String")
    })
    @PostMapping("task/searchbatchlist")
    public JSONObject getbatchList(String theme, String userName, Long taskStart, Long taskEnd, int page, int psize) {
        PageInfo<BatchTaskVO> pageInfo = policyRecommendTaskService.searchBatchTaskList(theme, userName, page, psize);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    /*****************************互联网开通***********************/
    @ApiOperation("new 添加互联网开通任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "申请描述", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "entrySubnet", value = "入口子网", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "exitSubnet", value = "出口子网", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "协议号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "策略开始时间", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "策略结束时间", required = false, dataType = "Long"),
    })
    @PostMapping("task/addinternat")
    public JSONObject addInternatRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        log.debug("新建互联网开通工单接口Start参数{}", JSONObject.toJSONString(entity));
        int rc = ReturnCode.POLICY_MSG_OK;
        try {
            //参数校验
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

            //添加数据检测
            BeanUtils.copyProperties(entity, recommendTaskEntity);
            //去掉没用的空格
            recommendTaskEntity.setSrcIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getSrcIpSystem()));
            recommendTaskEntity.setDstIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getDstIpSystem()));

            //服务对象转换成字符串保存数据库, 若服务为空，则为any
            recommendTaskEntity.setServiceList(entity.getServiceList() == null ? null : JSONObject.toJSONString(entity.getServiceList()));
            UserInfoDTO userInfoDTO = remoteBranchService.findOne(auth.getName());
            if (userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
                recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
            } else {
                recommendTaskEntity.setBranchLevel("00");
            }
            //设置创建时间和流水号（时间相关）
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "A" + simpleDateFormat.format(date);
            recommendTaskEntity.setCreateTime(date);
            recommendTaskEntity.setOrderNumber(orderNumber);

            //设置用户名
            recommendTaskEntity.setUserName(auth.getName());
            recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            if (entity.getIdleTimeout() != null) {
                recommendTaskEntity.setIdleTimeout(entity.getIdleTimeout() * HOUR_SECOND);
            } else {
                recommendTaskEntity.setIdleTimeout(null);
            }
            //设置状态和任务类型
            recommendTaskEntity.setTaskType(entity.getTaskType());
            recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);
            // 查询是否有关联nat
            int returnCode = recommendBussCommonService.checkPostRelevancyNat(recommendTaskEntity, auth);
            if (ReturnCode.POLICY_MSG_OK != returnCode) {
                return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(returnCode));
            }
            List<RecommendTaskEntity> list = new ArrayList<>();
            list.add(recommendTaskEntity);
            policyRecommendTaskService.insertRecommendTaskList(list);
            recommendBussCommonService.updateRelevanceNatTaskId(list);
            String message = String.format("新建互联网开通工单：%s 成功", recommendTaskEntity.getTheme());
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
            List<Integer> collect = list.stream().map(r -> r.getId()).collect(Collectors.toList());
            JSONObject returnJSON = getReturnJSON(rc, "");
            returnJSON.put("taskId",collect);
            return returnJSON;
        }catch (IllegalArgumentException e){
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            log.error("新建互联网开通工单接口End参数异常",e);
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
        log.debug("新建互联网开通工单接口Start参数{}",JSONObject.toJSONString(entity));
        int rc = ReturnCode.POLICY_MSG_OK;
        try{
            //参数校验
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

            //添加数据检测
            BeanUtils.copyProperties(entity, recommendTaskEntity);
            //去掉没用的空格
            recommendTaskEntity.setSrcIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getSrcIpSystem()));
            recommendTaskEntity.setDstIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getDstIpSystem()));

            //服务对象转换成字符串保存数据库, 若服务为空，则为any
            recommendTaskEntity.setServiceList(entity.getServiceList() == null ? null : JSONObject.toJSONString(entity.getServiceList()));
            UserInfoDTO userInfoDTO = remoteBranchService.findOne(auth.getName());
            if (userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
                recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
            } else {
                recommendTaskEntity.setBranchLevel("00");
            }
            //设置创建时间和流水号（时间相关）
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "A" + simpleDateFormat.format(date);
            recommendTaskEntity.setCreateTime(date);
            recommendTaskEntity.setOrderNumber(orderNumber);

            //设置用户名
            recommendTaskEntity.setUserName(auth.getName());
            recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            if (entity.getIdleTimeout() != null) {
                recommendTaskEntity.setIdleTimeout(entity.getIdleTimeout() * HOUR_SECOND);
            } else {
                recommendTaskEntity.setIdleTimeout(null);
            }
            //设置状态和任务类型
            recommendTaskEntity.setTaskType(entity.getTaskType());
            recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);
            // 查询是否有关联nat
            int returnCode = recommendBussCommonService.checkPostRelevancyNat(recommendTaskEntity, auth);
            if (ReturnCode.POLICY_MSG_OK != returnCode) {
                return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(returnCode));
            }
            //南北+东西
            globalRecommendService.addGlobalinternat(entity,recommendTaskEntity);
            String message = String.format("新建互联网开通工单：%s 成功", recommendTaskEntity.getTheme());
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
            JSONObject returnJSON = getReturnJSON(rc, "");
            returnJSON.put("taskId",Arrays.asList(recommendTaskEntity.getId()));
            return returnJSON;
        }catch (IllegalArgumentException e){
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            log.error("新建互联网开通工单接口End参数异常",e);
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


    @ApiOperation("new 批量删除任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "策略开通任务id数组", required = true, dataType = "String")
    })
    @PostMapping("task/deletetask")
    public JSONObject deleteTask(String ids) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            log.error("开始策略仿真任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            log.error("解析任务列表出错！", e);
        }

        //检测删除的任务是否有在运行中的，有的话则不进行删除。
        StringBuilder errMsg = new StringBuilder();
        int rc = ReturnCode.POLICY_MSG_OK;
        List<String> themeList = new ArrayList<>();
        int tasking = 0;
        List<Integer> weTaskIds = new ArrayList<>();
        for (int id : idList) {
            log.info(String.format("获取任务(%d)", id));
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
            logClientSimple.addBusinessLog(LogLevel.ERROR.getId(), BusinessLogType.POLICY_PUSH.getId(), String.format("删除工单: %s 失败",org.apache.commons.lang3.StringUtils.join(themeList, ",")));
            return getReturnJSON(ReturnCode.FAILED);
        }

        String message = String.format("删除策略开通工单：%s 成功", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }

    @ApiOperation("new 获取单个任务详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "策略开通任务id", required = true, dataType = "String")
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
    @ApiOperation("new 编辑策略开通任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "id", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "申请描述", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "协议号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "策略开始时间", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "策略结束时间", required = false, dataType = "Long"),
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
                    return getReturnJSON(ReturnCode.FAILED, "服务中同类型协议只能添加一条！");
                } else {
                    serviceSet.add(serviceDTO.getProtocol());
                }
            }
        }

        //添加数据检测
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();

        boolean invalidId = false;
        try {
            int taskId = Integer.valueOf(idString);
            recommendTaskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskId);
        } catch (Exception e) {
            logger.error("ID格式错误", e);
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

        //有一个不为空，则为互联网开通任务
        if (!AliStringUtils.isEmpty(entity.getEntrySubnet()) || !AliStringUtils.isEmpty(entity.getExitSubnet())) {
            InternetAdditionalInfoEntity additionalInfoEntity = new InternetAdditionalInfoEntity();
            JSONArray dstArray = JSONObject.parseArray(entity.getExitSubnet());
            List<SubnetEntity> exitSubnetList = dstArray.toJavaList(SubnetEntity.class);
            additionalInfoEntity.setExitSubnetList(exitSubnetList);
            recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        }

        //服务对象转换成字符串保存数据库, 若服务为空，则为any
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
        // 编辑的时候做nat映射匹配处理如果匹配不上 则直接报错
        int returnCode = recommendBussCommonService.checkPostRelevancyNat(recommendTaskEntity, auth);
        if (ReturnCode.POLICY_MSG_OK != returnCode) {
            return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(returnCode));
        }
        // KSH-5812  下发前的任务（仿真未开始和仿真完成未下发）支持编辑    如状态为下发完成/下发失败 则需要删除之前已经和源工单相关的数据
        if (recommendTaskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR ||
                recommendTaskEntity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE) {
            int type = 1;
            List<Integer> taskIds = new ArrayList<>();
            taskIds.add(recommendTaskEntity.getId());
            policyRecommendTaskService.deleteTasks(taskIds,type);
        }

        if (entity.getIpType() == 0) {
            if (vmwareInterfaceStatusConfig.isVmInterfaceAvailable()) {
                //编辑东西向工单内容
                try {
                    entity.setWeTaskId(recommendTaskEntity.getWeTaskId());
                    globalRecommendService.editWETask(entity);
                    recommendTaskEntity.setWeTaskId(entity.getWeTaskId());
                    recommendTaskEntity.setTaskType(entity.getTaskType());
                } catch (Exception e) {
                    return getReturnJSON(ReturnCode.FAILED, "东西向任务更新异常：" + e.getMessage());
                }
            }
        } else {
            //非ip4场景不支持云开通
            if(recommendTaskEntity.getWeTaskId()!=null){
                recommendTaskEntity.setWeTaskId(null);
                policyRecommendTaskService.updateWeTaskId(recommendTaskEntity);
            }
        }
        // 更新工单操作
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);
        policyRecommendTaskService.updateTaskById(recommendTaskEntity);
        // 构建处理关联关系
        buildRelevanceNat(recommendTaskEntity, originalRelevancyNat, newlRelevancyNat);
        int taskType = recommendTaskEntity.getTaskType().intValue();
        String taskTypeDesc = RecommendTypeEnum.getRecommendTypeByTypeCode(taskType).getDesc();
        String message = String.format("编辑%s工单: %s 成功", taskTypeDesc, recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }


    /**
     * 构建处理编辑策略的时候工单所关联的场景关系
     * @param recommendTaskEntity
     * @param originalRelevancyNat
     * @param newlRelevancyNat
     */
    private void buildRelevanceNat(RecommendTaskEntity recommendTaskEntity, String originalRelevancyNat, String newlRelevancyNat) {
        // 清除工单原有的关联场景的关联关系
        recommendTaskEntity.setRelevancyNat(originalRelevancyNat);
        recommendBussCommonService.updateRelevancyNatTaskId(recommendTaskEntity,true);
        // 关联现有工单上的关联场景的关联关系
        recommendTaskEntity.setRelevancyNat(newlRelevancyNat);
        recommendBussCommonService.updateRelevancyNatTaskId(recommendTaskEntity,false);
    }

    /**
     * 处理工单所关联的nat json串,
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
