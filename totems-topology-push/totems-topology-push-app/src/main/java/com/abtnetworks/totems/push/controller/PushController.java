package com.abtnetworks.totems.push.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.config.VmwareInterfaceStatusConfig;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.StringUtils;
import com.abtnetworks.totems.push.dao.mysql.PushPwdStrategyMapper;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.dto.PushStatus;
import com.abtnetworks.totems.push.entity.PushPwdStrategyEntity;
import com.abtnetworks.totems.push.entity.PushTaskEntity;
import com.abtnetworks.totems.push.manager.PushTaskManager;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.push.service.task.GlobalPushTaskService;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.push.vo.*;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dto.push.TaskStatusBranchLevelsDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/4 14:25
 */
@Api(tags="??????????????????")
@RestController
@RequestMapping(value="/task/")
public class PushController extends BaseController {

    private static Logger logger = Logger.getLogger(PushController.class);

    private static final String ORDER_SEPERATOR = ",";

    @Autowired
    private PushTaskService pushTaskService;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private PushTaskManager pushTaskManager;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    private LogClientSimple logClientSimple;
    @Autowired
    CommandTaskEdiableMapper commandTaskEdiableMapper;
    @Resource
    RemoteBranchService remoteBranchService;
    @Autowired
    private PushService pushService;
    @Autowired
    VmwareInterfaceStatusConfig vmwareInterfaceStatusConfig;

    @Autowired
    private PushPwdStrategyMapper pushPwdStrategyMapper;

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="orderNo",value="?????????", required=false, dataType="String"),
            @ApiImplicitParam(paramType="query", name="type", value="??????", required=false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="status", value="??????", required=false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="page", value="??????", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="????????????", required=true, dataType = "Integer")
    })
    @PostMapping("pushtasklist_old")
    public JSONObject list(String orderNo, String type, String status, int page, int psize) {
        PushTaskPageVO pageVO = pushTaskManager.getPushTaskList(orderNo, type, status, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation(value = "????????????", httpMethod = "POST", notes = "????????????????????????????????????????????????????????????????????????", nickname = "??????")
    @RequestMapping(value = "/new-policy-push", method = RequestMethod.POST)
    public JSONObject newPolicyPush(@ApiParam(name = "newPolicyPushVO", value = "????????????", required = true) @RequestBody NewPolicyPushVO newPolicyPushVO, Authentication auth) throws Exception {
        newPolicyPushVO.setUserName(auth.getName());
        int rc = pushTaskService.newPolicyPush(newPolicyPushVO);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId",newPolicyPushVO.getTaskId());
        rs.put("pushTaskId",newPolicyPushVO.getPushTaskId());
        return rs;
    }


    @ApiOperation("????????????????????????????????????")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name="tasks",value="????????????????????????", required=true, dataType="String"),
    })
    @PostMapping("addpushtasks")
    public JSONObject addPushTask(String tasks, Authentication auth)  {
        logger.info("???????????????????????????"+ tasks);
        JSONArray jsonArray = JSONArray.parseArray(tasks);
        if(jsonArray == null || jsonArray.size() == 0){
            logger.error("???????????????????????????????????????\n" + tasks );
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_DELETE);
        }
        List<PushTaskEntity> list = parseTaskEntity(tasks);
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(auth.getName());
        for(PushTaskEntity entity : list) {
            entity.setUserName(auth.getName());
        }

        List<CommandTaskEditableEntity> commandTaskEntityList = processTaskList(list,userInfoDTO);


        for(CommandTaskEditableEntity entity: commandTaskEntityList) {
            if(userInfoDTO != null ){
                entity.setBranchLevel(userInfoDTO.getBranchLevel());
            }else{
                entity.setBranchLevel("00");
            }
            recommendTaskService.addCommandTaskEditableEntity(entity);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name="ids", value="??????id??????", required=true, dataType="String")
    })
    @PostMapping("delpushtasks")
    public JSONObject deletePushTask(@RequestParam String ids) {
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if(jsonArray == null || jsonArray.size() == 0){
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_DELETE);
        }

        //TODO: ??????????????????????????????????????????

        List<Integer> list = null;
        try {
            list =StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("????????????id????????????", e);
        }

        if(list == null) {
            return getReturnJSON(ReturnCode.PARSE_ID_LIST_FAIL);
        }

        for(int id :list) {
            pushTaskManager.deletePushTask(id);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }

    ////////////////////////////////////????????????????????????????????????///////////////////////////////////
    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="orderNo",value="?????????", required=false, dataType="String"),
            @ApiImplicitParam(paramType="query", name="type", value="??????", required=false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="status", value="??????", required=false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="pushStatus", value="????????????", required=false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="revertStatus", value="????????????", required=false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="page", value="??????", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="????????????", required=true, dataType = "Integer")
    })
    @PostMapping("pushtasklist")
    public JSONObject getlist(String taskId, String orderNo, String type, String status, String pushStatus, String revertStatus, int page, int psize,String userName,Authentication authentication) {
        String theme = orderNo;
        String taskType = type;
        String branchLevel;
        if(org.apache.commons.lang3.StringUtils.isBlank(userName)){
            branchLevel =  remoteBranchService.likeBranch( authentication.getName());
        }else{
            branchLevel =  remoteBranchService.likeBranch( userName);
        }
        PageInfo<PushTaskVO> pageVO = recommendTaskService.getPushTaskList(taskId, theme, taskType, status, pushStatus, revertStatus, page, psize,userName,branchLevel);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @Autowired
    GlobalPushTaskService globalPushTaskService;

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="ids", value="??????id??????", required=true, dataType="String")
    })
    @PostMapping("startGlobalPushTask")
    public JSONObject startGlobalPushTask(@RequestParam String ids, String isRevert) {
        if(!vmwareInterfaceStatusConfig.isVmInterfaceAvailable()){
            return startCommandTask(ids,isRevert);
        }
        List<Integer> idList = null;
        try {
            if (StringUtils.isEmpty(ids)) {
                logger.error("?????????????????????????????????");
                return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
            }
            ids = String.format("[%s]", ids);
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("???????????????????????????ids=" + ids, e);
            return getReturnJSON(ReturnCode.FAILED);
        }
        boolean revert = false;
        if (isRevert.equalsIgnoreCase("true")) {
            revert = true;
        }
        StringBuilder errMsg = new StringBuilder();
        List<String> themeList = new ArrayList<>();
        List<Integer> weTaskIds = new ArrayList<>();
        List<CommandTaskDTO> taskDTOList = globalPushTaskService.getCommandTaskDTOListByTaskid(idList, revert, errMsg, themeList, weTaskIds);
        int wePushTaskCnt = 0;
        if (!CollectionUtils.isEmpty(weTaskIds)) {
            wePushTaskCnt = globalPushTaskService.getWePushTaskListByTaskId(weTaskIds);
        }
        if (taskDTOList.size() == 0 && wePushTaskCnt == 0) {
            logger.error("???????????????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }
        // ?????????????????????/??????
        String message = String.format("?????????%s ?????????%s", org.apache.commons.lang3.StringUtils.join(themeList, ","), revert ? "??????" : "??????");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        if (!CollectionUtils.isEmpty(taskDTOList)) {
            //????????????
            logger.info(String.format("%d?????????????????????????????????",taskDTOList.size()));
            int returnCode = 0;
            try {
                returnCode = pushTaskService.preBatchPushTaskList(taskDTOList,true);
            } catch (Exception e) {
                logger.error("??????????????????,????????????:{}", e);
                return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(ReturnCode.PUSH_TASK_ERROR));
            }
            if(ReturnCode.PUSH_TIME_LOCKED==returnCode){
                logger.info("??????????????????????????????");
                errMsg.append("??????????????????????????????");
                return getReturnJSON(ReturnCode.FAILED,errMsg.toString());
            }
        }
        if (wePushTaskCnt > 0) {
            //???????????????
            logger.info(String.format("%d??????????????????????????????",wePushTaskCnt));
            globalPushTaskService.pushWeTask(weTaskIds, revert);
        }
        logger.info(errMsg);
        if (revert) {
            errMsg.insert(0, String.format("????????????,%d??????????????????%d????????????", taskDTOList.size(), wePushTaskCnt));
        } else {
            errMsg.insert(0, String.format("????????????,%d??????????????????%d????????????", taskDTOList.size(), wePushTaskCnt));
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errMsg.toString());
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "ids", value = "??????id??????", required = true, dataType = "String")})
    @PostMapping("startpushtasks")
    public JSONObject startCommandTask(@RequestParam String ids, String isRevert) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if(jsonArray == null || jsonArray.size() == 0){
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        boolean revert = false;
        if(isRevert.equalsIgnoreCase("true")) {
            revert = true;
        }
        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch(Exception e) {
            logger.error("???????????????????????????", e);
        }

        StringBuilder errMsg = new StringBuilder();
        List<String> themeList = new ArrayList<>();
        List<CommandTaskDTO> taskDTOList = new ArrayList<>();
        for(Integer id: idList) {
            logger.info(String.format("????????????(%d)", id));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(id);
            if(taskEntityList.size() == 0) {
                logger.error(String.format("????????????(%d)???????????????????????????????????????...", id));
                continue;
            }
            boolean ignore = false;

            for(CommandTaskEditableEntity taskEntity: taskEntityList) {
                String command = taskEntity.getCommandline();
                if(org.apache.commons.lang3.StringUtils.isNotBlank(command) && command.startsWith("?????????????????????????????????")) {
                    errMsg.append(String.format("[%s]?????????????????????????????????????????????????????????", taskEntity.getTheme()));
                    ignore = true;
                    break;
                }
            }
            if(ignore) {
                continue;
            }

            CommandTaskDTO taskDTO = new CommandTaskDTO();
            taskDTO.setList(taskEntityList);
            taskDTO.setRevert(revert);

            RecommendTaskEntity taskEntity = recommendTaskService.getRecommendTaskByTaskId(id);
            taskDTO.setTaskId(taskEntity.getId());
            taskDTO.setTheme(taskEntity.getTheme());

            taskDTOList.add(taskDTO);

            themeList.add(taskEntity.getTheme());
        }

        if(taskDTOList.size() == 0) {
            logger.error("???????????????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }


//        for(CommandTaskDTO commandTaskDTO:taskDTOList) {
//            if(revert) {
//                recommendTaskService.updateCommandTaskRevertStatus(commandTaskDTO.getTaskId(), PolicyConstants.REVERT_STATUS_REVERTING);
//            } else {
//                recommendTaskService.updateCommandTaskPushStatus(commandTaskDTO.getTaskId(), PolicyConstants.PUSH_STATUS_PUSHING);
//            }
//        }


        String message = String.format("?????????%s ?????????%s", org.apache.commons.lang3.StringUtils.join(themeList, ","), revert ? "??????" : "??????");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        //??????????????????
        int returnCode = 0;
        try {
            returnCode = pushTaskService.preBatchPushTaskList(taskDTOList,true);
        } catch (Exception e) {
            logger.error("??????????????????,????????????:{}", e);
            return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(ReturnCode.PUSH_TASK_ERROR));
        }
        if(ReturnCode.PUSH_TIME_LOCKED==returnCode){
            logger.info("??????????????????????????????");
            errMsg.append("??????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED,errMsg.toString());
        }

        logger.info(errMsg);

        if(revert) {
            errMsg.insert(0, "???????????????");
        } else {
            errMsg.insert(0, "???????????????");
        }

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errMsg.toString());
    }

    @ApiOperation("????????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="ids", value="??????id??????", required=true, dataType="String")
    })
    @PostMapping("getdevicenum")
    public JSONObject getDeviceNum(@RequestParam String ids) {
        StringBuilder returnMsg = new StringBuilder();
        returnMsg.append("???????????????????????????");
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if(jsonArray == null || jsonArray.size() == 0){
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }
        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch(Exception e) {
            logger.error("???????????????????????????", e);
        }

        List<CommandTaskEditableEntity> taskList = new ArrayList<>();
        for(Integer id: idList) {
            logger.info(String.format("????????????(%d)", id));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(id);
            taskList.addAll(taskEntityList);
        }

        if(taskList.size() == 0) {
            logger.error("???????????????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED, returnMsg.toString());
        }

        List<String> deviceUuidList = taskList.stream().map(task -> task.getDeviceUuid()).distinct().collect(Collectors.toList());
        Set<String> rootDeviceUuids = new HashSet<>();
        for (String deviceUuid : deviceUuidList) {
            rootDeviceUuids.add(pushService.getRootDeviceUuid(deviceUuid));
        }

        returnMsg.append(rootDeviceUuids.size());

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, returnMsg.toString());
    }

    @ApiOperation("?????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="taskId", value="??????taskId", required=true, dataType="String"),
            @ApiImplicitParam(paramType="query", name="ids", value="??????id??????", required=true, dataType="String")
    })
    @PostMapping("startdevicepushtasks")
    public JSONObject startCommandTaskDevice(@RequestParam String taskId, String ids) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(taskId) || org.apache.commons.lang3.StringUtils.isEmpty(ids)){
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        StringBuilder errMsg = new StringBuilder();
        logger.info(String.format("????????????(%s)", ids));
        List<String> list = new ArrayList<>();
        String[] idArray = ids.split(ORDER_SEPERATOR);
        for(String id:idArray) {
            list.add(id);
        }

        List<CommandTaskEditableEntity> taskEditableEntityList = new ArrayList<>();
        for (String id : list) {
            CommandTaskEditableEntity taskEditableEntity = commandTaskManager.selectByPrimaryKey(Integer.parseInt(id));
            String command = taskEditableEntity.getCommandline();
            if(org.apache.commons.lang3.StringUtils.isNotBlank(command) && command.startsWith("?????????????????????????????????")) {
                errMsg.append(String.format("[%s]?????????????????????????????????????????????????????????", taskEditableEntity.getTheme()));
                continue;
            }
            taskEditableEntityList.add(taskEditableEntity);
        }

        if(ObjectUtils.isEmpty(taskEditableEntityList)) {
            logger.error(String.format("????????????(%s)???????????????????????????????????????...", ids));
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }
        boolean ignore = false;

        CommandTaskDTO taskDTO = new CommandTaskDTO();
        taskDTO.setList(taskEditableEntityList);
        taskDTO.setRevert(false);

        RecommendTaskEntity taskEntity = recommendTaskService.getRecommendTaskByTaskId(Integer.parseInt(taskId));
        taskDTO.setTaskId(taskEntity.getId());
        taskDTO.setTheme(taskEntity.getTheme());

        String message = String.format("?????????%s ?????????%s", taskEntity.getTheme(), "??????");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        int returnCode = pushTaskService.addDeviceCommandTaskList(taskDTO, true);
        if(ReturnCode.PUSH_TIME_LOCKED==returnCode){
            logger.info("??????????????????????????????");
            errMsg.append("??????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED,errMsg.toString());
        }

        logger.info(errMsg);

        errMsg.insert(0, "???????????????");

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errMsg.toString());
    }

    @ApiOperation("??????????????????")
    @PostMapping("stoppushtasks")
    public JSONObject stopPushTask(){

        int stopList = pushTaskService.stopAllTasks();
        String msg = "?????????????????????????????????";
        int code = ReturnCode.POLICY_MSG_OK;
        if(0 == stopList) {
            code = ReturnCode.STOP_TASK_FAILED;
            msg = ReturnCode.getMsg(ReturnCode.STOP_TASK_FAILED);
        }
        return getReturnJSON(code, msg);
    }

    @PostMapping("pushtaskstatuslist")
    public JSONObject getstatuslist(Authentication authentication) {
        List<PushStatusVO> pushStatusVOList = new LinkedList<>();
        String  userName = authentication.getName();
        TaskStatusBranchLevelsDTO taskStatusBranchLevelsDTO = recommendTaskService.getPushTaskStatusList(userName);

        getStatistic(pushStatusVOList, taskStatusBranchLevelsDTO);

        String jsonObjectString = JSONObject.toJSONString(pushStatusVOList);
        JSONArray jsonArray = JSONObject.parseArray(jsonObjectString);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }
    @ApiOperation("??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="ids", value="??????id??????", required=true, dataType="String"),
            @ApiImplicitParam(paramType="query", name="schedule", value="??????", required=true, dataType="String"),
            @ApiImplicitParam(paramType="query", name="enableEmail", value="??????????????????", required=true, dataType="String"),
            @ApiImplicitParam(paramType="query", name="receiverEmail", value="???????????????", required=true, dataType="String")
    })
    @PostMapping("setschedule")
    public JSONObject setSchedule(String ids, String schedule, String enableEmail, String receiverEmail) {
        logger.info(String.format("%s. %s", ids, schedule));
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if(jsonArray == null || jsonArray.size() == 0){
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch(Exception e) {
            logger.error("???????????????????????????", e);
        }

        Date scheduleTime = null;
        if(!AliStringUtils.isEmpty(schedule) && !schedule.equalsIgnoreCase("NaN")) {
            try {
                Long time = Long.valueOf(schedule);
                scheduleTime = new Date(time);
            } catch (Exception e) {
                return getReturnJSON(ReturnCode.FAILED);
            }
        }

        if (AliStringUtils.isEmpty(enableEmail) && (!"true".equals(enableEmail) || !"false".equals(enableEmail))) {
            return getReturnJSON(ReturnCode.FAILED);
        }


        List<CommandTaskEditableEntity> list = new ArrayList<>();
        for(Integer id: idList) {
            logger.info(String.format("????????????(%d)", id));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(id);
            if(taskEntityList.size() == 0) {
                logger.error(String.format("????????????(%d)???????????????????????????????????????...", id));
                continue;
            }

            for(CommandTaskEditableEntity entity: taskEntityList) {
                CommandTaskEditableEntity taskEntity = new CommandTaskEditableEntity();
                taskEntity.setId(entity.getId());
                taskEntity.setPushSchedule(scheduleTime);
                taskEntity.setEnableEmail(enableEmail);
                taskEntity.setReceiverEmail(receiverEmail);
                commandTaskManager.setPushSchedule(taskEntity);
            }
        }

        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }


    void getStatistic(List<PushStatusVO> pushStatusVOList, TaskStatusBranchLevelsDTO taskStatusBranchLevelsDTO) {
        PushStatusVO recommendStatusVO = new PushStatusVO();
        PushStatusVO checkStatusVO = new PushStatusVO();
        PushStatusVO securityStatusVO = new PushStatusVO();
        PushStatusVO staticNatStatusVO = new PushStatusVO();
        PushStatusVO srcNatStaticVO = new PushStatusVO();
        PushStatusVO dstNatStaticVO = new PushStatusVO();
        PushStatusVO bothNatStaticVO = new PushStatusVO();
        PushStatusVO f5DstNatStaticVO = new PushStatusVO();
        PushStatusVO f5BothNatStaticVO = new PushStatusVO();
        PushStatusVO staticRoutingVO = new PushStatusVO();
        PushStatusVO domainComplianceVO = new PushStatusVO();
        PushStatusVO optimizeVO = new PushStatusVO();
        PushStatusVO convergenceVO = new PushStatusVO();
        recommendStatusVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
        recommendStatusVO.setName("????????????");
        checkStatusVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK);
        checkStatusVO.setName("??????????????????");
        securityStatusVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
        securityStatusVO.setName("????????????-????????????");
        staticNatStatusVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT);
        staticNatStatusVO.setName("????????????-??????NAT");
        srcNatStaticVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT);
        srcNatStaticVO.setName("????????????-???NAT");
        dstNatStaticVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT);
        dstNatStaticVO.setName("????????????-??????NAT");
        bothNatStaticVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT);
        bothNatStaticVO.setName("????????????-BothNAT");
        optimizeVO.setType(PolicyConstants.POLICY_OPTIMIZE);
        optimizeVO.setName("??????????????????");
        f5DstNatStaticVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT);
        f5DstNatStaticVO.setName("F5??????-??????NAT");
        f5BothNatStaticVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT);
        f5BothNatStaticVO.setName("F5??????-BothNAT");
        staticRoutingVO.setType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING);
        staticRoutingVO.setName("????????????");
        domainComplianceVO.setType(PolicyConstants.DOMAIN_COMPLIANCE);
        domainComplianceVO.setName("????????????");
        convergenceVO.setType(PolicyConstants.POLICY_INT_PUSH_CONVERGENCE);
        convergenceVO.setName("????????????");
        List<PushStatus>   pushStatusList = taskStatusBranchLevelsDTO.getPushStatuses();

        for(PushStatus pushStatus: pushStatusList) {
            switch (pushStatus.getTaskType()) {
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND:
                case PolicyConstants.IN2OUT_INTERNET_RECOMMEND:
                case PolicyConstants.OUT2IN_INTERNET_RECOMMEND:
                case PolicyConstants.BIG_INTERNET_RECOMMEND:
                    statistic(recommendStatusVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK:
                    statistic(checkStatusVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED:
                    statistic(securityStatusVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT:
                    statistic(staticNatStatusVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT:
                    statistic(srcNatStaticVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT:
                    statistic(dstNatStaticVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT:
                    statistic(bothNatStaticVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_OPTIMIZE:
                    statistic(optimizeVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT:
                    statistic(f5DstNatStaticVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT:
                    statistic(f5BothNatStaticVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING:
                    statistic(staticRoutingVO, pushStatus);
                    break;
                case PolicyConstants.DOMAIN_COMPLIANCE:
                    statistic(domainComplianceVO, pushStatus);
                    break;
                case PolicyConstants.POLICY_INT_PUSH_CONVERGENCE:
                    statistic(convergenceVO, pushStatus);
                    break;
            }
        }

        String param = PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND +","+PolicyConstants.IN2OUT_INTERNET_RECOMMEND+","+PolicyConstants.OUT2IN_INTERNET_RECOMMEND+","+PolicyConstants.BIG_INTERNET_RECOMMEND;
        String  branchLevel = taskStatusBranchLevelsDTO.getBranchLevel();
        recommendStatusVO.setTotal(countTotal(param,branchLevel));
        pushStatusVOList.add(recommendStatusVO);
        securityStatusVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED),  branchLevel));
        pushStatusVOList.add(securityStatusVO);
        staticNatStatusVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT),branchLevel));
        pushStatusVOList.add(staticNatStatusVO);
        dstNatStaticVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT),branchLevel));
        pushStatusVOList.add(dstNatStaticVO);
        srcNatStaticVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT),branchLevel));
        pushStatusVOList.add(srcNatStaticVO);
        bothNatStaticVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT),branchLevel));
        pushStatusVOList.add(bothNatStaticVO);
        checkStatusVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK),branchLevel));
        pushStatusVOList.add(checkStatusVO);
        optimizeVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_OPTIMIZE),branchLevel));
        pushStatusVOList.add(optimizeVO);
        f5DstNatStaticVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT),branchLevel));
        pushStatusVOList.add(f5DstNatStaticVO);
        f5BothNatStaticVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT),branchLevel));
        pushStatusVOList.add(f5BothNatStaticVO);
        staticRoutingVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING),branchLevel));
        pushStatusVOList.add(staticRoutingVO);
        domainComplianceVO.setTotal(countTotal(String.valueOf(PolicyConstants.DOMAIN_COMPLIANCE),branchLevel));
        pushStatusVOList.add(domainComplianceVO);
        convergenceVO.setTotal(countTotal(String.valueOf(PolicyConstants.POLICY_INT_PUSH_CONVERGENCE),branchLevel));
        pushStatusVOList.add(convergenceVO);


    }

    private int countTotal(String param,String  branchLevel){

        Map<String,String> params = new HashMap(2);

        params.put("taskType", param);
        params.put("branchLevel", branchLevel);
        Integer listCount = commandTaskEdiableMapper.getPushTaskStatusListTotal(params);
        if(listCount!=null){
            return  listCount;
        }else{
            return 0;
        }
    }

    void statistic(PushStatusVO pushStatusVO, PushStatus pushStatus) {
        Integer total = pushStatusVO.getTotal();
        total = total + pushStatus.getCount();
        pushStatusVO.setTotal(total);

        switch(pushStatus.getPushStatus()) {
            case PolicyConstants.PUSH_STATUS_NOT_START: {
                int count = pushStatusVO.getNotStart();
                count = count + pushStatus.getCount();
                pushStatusVO.setNotStart(count);
                break;
            }
            case PolicyConstants.PUSH_STATUS_FINISHED: {
                int count = pushStatusVO.getFinished();
                count = count + pushStatus.getCount();
                pushStatusVO.setFinished(count);
                break;
            }
            case PolicyConstants.PUSH_STATUS_FAILED: {
                int count = pushStatusVO.getFailed();
                count = count + pushStatus.getCount();
                pushStatusVO.setFailed(count);
                break;
            }
        }

        switch(pushStatus.getRevertStatus()) {
            case PolicyConstants.REVERT_STATUS_FINISHED: {
                int count = pushStatusVO.getReverted();
                count = count + pushStatus.getCount();
                pushStatusVO.setReverted(count);
                break;
            }
            case PolicyConstants.REVERT_STATUS_FAILED: {
                int count = pushStatusVO.getRevertFailed();
                count = count + pushStatus.getCount();
                pushStatusVO.setRevertFailed(count);
                break;
            }
        }
    }

    /**
     * ????????????JSON?????????????????????????????????
     * @param taskString ????????????JSON???
     * @return ????????????????????????
     */
    private List<PushTaskEntity> parseTaskEntity(String taskString) {
        JSONArray jsonArray = JSONArray.parseArray(taskString);
        List<PushTaskEntity> list = new ArrayList<PushTaskEntity>();
        try {
            for (int index = 0; index < jsonArray.size(); index++) {
                PushTaskEntity entity = new PushTaskEntity();
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                entity.setOrderType(jsonObject.getInteger(PushConstants.PUSH_STR_TASK_ORDER_TYPE));

                //?????????????????????????????????????????????id
                if(entity.getOrderType() == PushConstants.PUSH_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND) {
                    entity.setPolicyId(jsonObject.getInteger(PushConstants.PUSH_STR_TASK_POLICY_ID));
                } else {
                    entity.setPolicyId(PushConstants.PUSH_INT_PUSH_TASK_NO_POLICY_ID);
                }
                entity.setOrderNo(jsonObject.getString(PushConstants.PUSH_STR_TASK_ORDER_NO));
                entity.setDeviceUuid(jsonObject.getString(PushConstants.PUSH_STR_TASK_DEVICE_UUID));
                entity.setDeviceName(jsonObject.getString(PushConstants.PUSH_STR_TASK_DEVICE_NAME));
                entity.setManageIp(jsonObject.getString(PushConstants.PUSH_STR_TASK_MANAGE_IP));
                entity.setUserName(jsonObject.getString(PushConstants.PUSH_STR_TASK_USER_NAME));
                entity.setCommand(jsonObject.getString(PushConstants.PUSH_STR_TASK_COMMAND));
                list.add(entity);
            }
        } catch(Exception e) {
            logger.error("???????????????????????????\n" + taskString);
        }
        return list;
    }

    /**
     * ??????????????????:???????????????????????????????????????????????????????????????
     * @param list ????????????????????????
     */
    List<CommandTaskEditableEntity> processTaskList(List<PushTaskEntity> list,UserInfoDTO userInfoDTO) {

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PushConstants.PUSH_ORDER_NO_TIME_FORMAT);
        String orderNo = "P" + simpleDateFormat.format(date);
        int index = 1;
        List<CommandTaskEditableEntity> commandTaskEntityList = new ArrayList<>();
        String branchLevel;
        if(userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())){
            branchLevel = userInfoDTO.getBranchLevel();
        }else{
            branchLevel  = "00";
        }
        for(PushTaskEntity entity: list) {
            String theme = String.format("%s%d", orderNo, index);
            Date createDate = new Date();
            //?????????????????????task???????????????taskId??????????????????????????????????????????????????????????????????????????????????????????task????????????
            RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
            recommendTaskEntity.setTheme(theme);
            recommendTaskEntity.setOrderNumber(theme);
            recommendTaskEntity.setUserName(entity.getUserName());
            recommendTaskEntity.setSrcIp("255.255.255.255");
            recommendTaskEntity.setDstIp("255.255.255.255");
            recommendTaskEntity.setCreateTime(createDate);
            recommendTaskEntity.setBranchLevel(branchLevel);
            recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
            recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK);
            if (entity.getOrderType() == PolicyConstants.POLICY_OPTIMIZE) {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_OPTIMIZE);
            } else if (entity.getOrderType() == PolicyConstants.POLICY_INT_PUSH_CONVERGENCE) {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_CONVERGENCE);
            } else {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK);
            }
            recommendTaskEntity.setIpType(0);
            List<RecommendTaskEntity> recommendTaskEntityListlist = new ArrayList<>();
            recommendTaskEntityListlist.add(recommendTaskEntity);
            recommendTaskService.insertRecommendTaskList(recommendTaskEntityListlist);

            CommandTaskEditableEntity commandTaskEntity = new CommandTaskEditableEntity();
            commandTaskEntity.setDeviceUuid(entity.getDeviceUuid());
            String model = recommendTaskService.getDeviceModelNumber(entity.getDeviceUuid());

            if (entity.getOrderType() == PolicyConstants.POLICY_OPTIMIZE) {
                commandTaskEntity.setTaskType(PolicyConstants.POLICY_OPTIMIZE);
            } else if (entity.getOrderType() == PolicyConstants.POLICY_INT_PUSH_CONVERGENCE) {
                commandTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_CONVERGENCE);
            } else {
                commandTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK);
            }
            commandTaskEntity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);
            commandTaskEntity.setCommandline(entity.getCommand());
            commandTaskEntity.setTheme(theme);
            commandTaskEntity.setCreateTime(createDate);
            commandTaskEntity.setTaskId(recommendTaskEntity.getId());
            commandTaskEntity.setUserName(entity.getUserName());

            commandTaskEntityList.add(commandTaskEntity);
            index = index++;
        }
        return commandTaskEntityList;
    }


    @ApiOperation("?????????????????????")
    @PostMapping("/pwdStrategyOperation")
    public JSONObject pwdStrategyOperation(@RequestBody PushPwdStrategyVO pwdStrategyVO){
        if (TotemsStringUtils.equals( "0",pwdStrategyVO.getPwdEnable()   ) ){
            pwdStrategyVO.setPwdDefaultMinLengthType("NOT_CHECK");
            pwdStrategyVO.setPwdDaysType("NOT_CHECK");
            pwdStrategyVO.setPwdContainType("NOT_CHECK");
        }else{
            pwdStrategyVO.setPwdDefaultMinLengthType("CHECK");
            pwdStrategyVO.setPwdDaysType("CHECK");
            pwdStrategyVO.setPwdContainType("CHECK");
        }
        int cr = pushService.pwdStrategyOperation(pwdStrategyVO);
        return getReturnJSON(cr);
    }

    @ApiOperation("??????????????????")
    @PostMapping("/searchCpwdStrategy")
    public JSONObject searchCmdDevicelist(HttpServletRequest request){
        PushPwdStrategyEntity pushPwdStrategyEntity = pushService.searchCmdDevicelist();
        String jsonObjectString = JSONObject.toJSONString(pushPwdStrategyEntity);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

}
