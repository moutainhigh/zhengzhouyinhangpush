package com.abtnetworks.totems.generate.controller;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.service.PushTaskStaticRoutingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.executor.PolicyGenerateThread;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.generate.service.CommandlineService;
import com.abtnetworks.totems.generate.task.CmdTaskService;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.push.vo.NewPolicyPushVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.PushAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.StaticNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.RecommendExcelAndDownloadService;
import com.abtnetworks.totems.recommend.vo.PolicyTaskDetailVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.*;

import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/4 14:25
 */
@Api(tags = "?????????????????????")
@RestController
@RequestMapping(value = "/recommend/")
public class PolicyGenerateController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(PolicyGenerateController.class);

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    CommandTaskManager commandTaskManager;

    @Autowired
    PushTaskService pushTaskService;

    @Autowired
    ExcelParser generateExcelParser;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    CommandlineService commandlineService;

    @Resource
    RecommendExcelAndDownloadService recommendExcelAndDownloadService;

    @Autowired
    PushTaskStaticRoutingService pushTaskStaticRoutingService;

    @Autowired
    Map<String, CmdService> cmdServiceMap;

    @Autowired
    AdvancedSettingService advancedSettingService;


    @Value("${resourceHandler}")
    private String resourceHandler;

    @Value("${importSecurityExcelFile}")
    private String securityExcelFile;

    @Value("${server.root.basedir}")
    private String serverRootBasedir;

    @Value("${push.download-file}")
    String dirPath;

    @Autowired
    private LogClientSimple logClientSimple;

    @Resource
    RemoteBranchService remoteBranchService;

    @Autowired
    CmdTaskService cmdTaskService;

    @Autowired
    @Qualifier(value = "generateExecutor")
    private Executor generateExecutor;

    @Autowired
    @Qualifier(value = "batchImportExecutor")
    private Executor batchImportExecutor;


    @ApiOperation("NAT??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ids", value = "id??????", required = false, dataType = "String")
    })
    @PostMapping("task/searchnatpolicytasklist")
    public JSONObject getNatPolicyTaskList(String theme, String type, int page, int psize, String ids, Integer id, String userName, String deviceUuid,String pushStatus , Authentication authentication) {

        PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getNatPolicyTaskList(theme, type, page, psize, ids, id, userName, deviceUuid,pushStatus,  authentication);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }


    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "Integer")
    })
    @PostMapping("task/searchsecuritypolicytasklist")
    public JSONObject getSecurityPolicyTaskList(String theme, int page, int psize, String userName, String deviceUuid,String pushStatus , Authentication authentication) {

        PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getSecurityPolicyTaskList(theme, page, psize, userName, deviceUuid,pushStatus,  authentication);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "String")
    })
    @PostMapping("task/searchcustomizecmdtasklist")
    public ReturnT getCustomizeCmdTaskList(String theme, int page, int psize, String userName, String deviceUuid, Integer pushStatus , Authentication authentication) {

        PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getCustomizeCmdTaskList(theme, page, psize, userName, deviceUuid,pushStatus,  authentication);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return new ReturnT(jsonObject);
    }

    @ApiOperation(value = "????????????excel??????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "??????????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "????????????", required = false, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "")})
    @RequestMapping(value = "task/exportTask", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload, String startTime, String endTime, Authentication authentication) throws Exception {

        List<PolicyTaskDetailVO> list = policyRecommendTaskService.getNatTaskList(null, "5", null, null, null, null, startTime, endTime,  authentication);
        List<PolicyTaskDetailVO> list1 = policyRecommendTaskService.getNatTaskList(null, "6", null, null, null, null, startTime, endTime,  authentication);
        List<PolicyTaskDetailVO> list2 = policyRecommendTaskService.getNatTaskList(null, "7", null, null, null, null, startTime, endTime,  authentication);
        List<PolicyTaskDetailVO> list3 = policyRecommendTaskService.getNatTaskList(null, "9", null, null, null, null, startTime, endTime,  authentication);

        List<PolicyTaskDetailVO> list4 = policyRecommendTaskService.getSecurityTaskList(null, null, null, startTime, endTime, authentication);
        List<PolicyTaskDetailVO> list5 = policyRecommendTaskService.getNatTaskList(null, "20", null, null, null, null, startTime, endTime, authentication);


        List<PolicyTaskDetailVO> taskArrayList[] = new ArrayList[6];
        taskArrayList[0] = list4;
        taskArrayList[1] = list1;
        taskArrayList[2] = list2;
        taskArrayList[3] = list;
        taskArrayList[4] = list3;
        taskArrayList[5] = list5;

        List<RecommendTaskEntity> taskList = policyRecommendTaskService.getTaskListByTime(startTime, endTime, authentication);

        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "??????????????????";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            logger.error("??????????????????????????????????????????", e1);
        }

        String destDirName = dirPath + "/policyGenerateExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "policyGeneratedoing.temp";

        try {
            // ???????????????????????????
            if (!new File(destDirName).exists()) {
                FileUtils.createDir(destDirName);
            }

            String fileIsExistsName = FileUtils.isDirExistFile(destDirName);
            boolean doingFileTempIsExists = FileUtils.fileIsExists(doingFileTemp);
            boolean fileIsExists = FileUtils.fileIsExists(destDirName + "/" + fileIsExistsName);
            if (null == isReload) {
                if (fileIsExists && doingFileTempIsExists == false) {
                    resultRO.setMessage("??????????????????");
                    jsonObject.put("filePath", fileIsExistsName);
                    jsonObject.put("status", 1);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists) {
                    // ??????????????????????????????
                    resultRO.setMessage("???????????????");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists == false && fileIsExists == false) {
                    // ??????????????????
                    File doingFile = new File(doingFileTemp);
                    doingFile.createNewFile();
                    resultRO.setMessage("????????????");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    new PolicyGenerateThread(filePath, doingFile, taskArrayList).start();
                    resultRO.setData(jsonObject);
                    return resultRO;
                }
            }
            if ("true".equals(isReload)) {
                // ??????????????????
                FileUtils.deleteFileByPath(destDirName + "/" + fileIsExistsName);
                // ?????????????????????????????????
                // ??????????????????
                File doingFile = new File(doingFileTemp);
                doingFile.createNewFile();
                new PolicyGenerateThread(filePath, doingFile, taskArrayList).start();
                resultRO.setMessage("??????????????????");
                jsonObject.put("filePath", preFilename + ".xlsx");
                jsonObject.put("status", 2);
                resultRO.setData(jsonObject);
                return resultRO;
            } else {
                recommendExcelAndDownloadService.downLoadPolicyAdd(response, destDirName + "/" + fileIsExistsName);
                return null;
            }
        } catch (Exception e) {
            File doingFile = new File(doingFileTemp);
            doingFile.delete();
            logger.error("??????????????????Excel????????????:", e);
            resultRO.setMessage("??????????????????");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }


    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id??????", required = true, dataType = "String")
    })
    @PostMapping("task/deletesecuritypolicytasklist")
    public JSONObject deletePolicyTaskList(@RequestParam String ids) {

        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("???????????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_DELETE);
        }
        List<Integer> idList = null;
        try {
            idList = com.abtnetworks.totems.common.utils.StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
        }

        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        StringBuilder errMsg = new StringBuilder();
        int rc = ReturnCode.POLICY_MSG_OK;
        List<String> themeList = new ArrayList<>();
        int tasking = 0;
        for (int id : idList) {
            logger.info(String.format("????????????(%d)", id));
            CommandTaskEditableEntity taskEntity = policyRecommendTaskService.getRecommendTaskById(id);
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (null == taskEntity) {
                continue;
            }
            themeList.add(taskEntity.getTheme());
            if (taskEntity.getPushStatus() == PolicyConstants.PUSH_STATUS_PUSHING ||
                    taskEntity.getRevertStatus() == PolicyConstants.REVERT_STATUS_REVERTING) {
                tasking++;
                rc = ReturnCode.CAN_NOT_DELETE_RUNNING_TASK;
                errMsg.append(taskEntity.getTheme() + ",");
            }
        }
        if(tasking>0){
            errMsg.deleteCharAt(errMsg.length()-1);
            errMsg.append(":" + ReturnCode.getMsg(rc));
        }

        if (rc != ReturnCode.POLICY_MSG_OK) {
            return getReturnJSON(rc, errMsg.toString());
        }

        policyRecommendTaskService.removePolicyTasks(idList);

        String message = String.format("????????????????????????????????????%s ??????", StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "Integer")
    })
    @PostMapping("task/addstaticnatpolicy")
    public JSONObject addStaticNatPolicyPolicy(@RequestBody StaticNatPolicyDTO staticNatPolicyDTO, Authentication auth) {
        logger.info("????????????nat??????" + JSONObject.toJSONString(staticNatPolicyDTO));
        if (staticNatPolicyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }

        //????????????????????????
        String deviceUuid = staticNatPolicyDTO.getDeviceUuid();
        NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return getReturnJSON(ReturnCode.DEVICE_NOT_EXIST);
        }

        //???????????????
        String userName = auth.getName();

        //??????????????????
        staticNatPolicyDTO.setSrcZone(getZone(staticNatPolicyDTO.getSrcZone()));
        staticNatPolicyDTO.setDstZone(getZone(staticNatPolicyDTO.getDstZone()));

        //????????????NAT????????????????????????
        StaticNatAdditionalInfoEntity additionalInfoEntity = new StaticNatAdditionalInfoEntity(deviceUuid,
                staticNatPolicyDTO.getSrcZone(), staticNatPolicyDTO.getDstZone(), staticNatPolicyDTO.getInDevItf(),
                staticNatPolicyDTO.getOutDevItf(), staticNatPolicyDTO.getPreIpAddress(), staticNatPolicyDTO.getPostIpAddress(),
                staticNatPolicyDTO.getPrePort(), staticNatPolicyDTO.getPostPort(), staticNatPolicyDTO.getProtocol(),staticNatPolicyDTO.getInDevItfAlias(),staticNatPolicyDTO.getOutDevItfAlias());
        Integer ipType = ObjectUtils.isNotEmpty(staticNatPolicyDTO.getIpType())?staticNatPolicyDTO.getIpType(): IPV4.getCode();

        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(staticNatPolicyDTO.getTheme(), userName,
                "255.255.255.255", "255.255.255.255", null, PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, JSONObject.toJSONString(additionalInfoEntity), null, null, null, null,ipType);
        getBranch(userName, recommendTaskEntity);
        recommendTaskEntity.setRemarks(staticNatPolicyDTO.getRemarks());
        recommendTaskEntity.setDescription(staticNatPolicyDTO.getDescription());
        addRecommendTask(recommendTaskEntity);


        //?????????????????????
        CommandTaskEditableEntity entity = createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT,
                recommendTaskEntity.getId(), userName, staticNatPolicyDTO.getTheme(), staticNatPolicyDTO.getDeviceUuid());
        BeanUtils.copyProperties(staticNatPolicyDTO, entity);
        entity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        commandTaskManager.addCommandEditableEntityTask(entity);
        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        boolean isVsys = false;
        String rootDeviceUuid = "";
        String vsysName = "";
        if (device != null) {
            DeviceDataRO deviceData = device.getData().get(0);
            if (deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                rootDeviceUuid = deviceData.getRootDeviceUuid();
                vsysName = deviceData.getVsysName();
            }
        }

        CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.STATIC, entity.getId(), entity.getTaskId(), deviceUuid, staticNatPolicyDTO.getTheme(),
                userName, null, additionalInfoEntity.getGlobalAddress(), null,
                additionalInfoEntity.getInsideAddress(), EntityUtils.getServiceList(additionalInfoEntity.getProtocol(), additionalInfoEntity.getGlobalPort()),
                EntityUtils.getServiceList(additionalInfoEntity.getProtocol(), additionalInfoEntity.getInsidePort()), staticNatPolicyDTO.getSrcZone(),
                staticNatPolicyDTO.getDstZone(), staticNatPolicyDTO.getInDevItf(), staticNatPolicyDTO.getOutDevItf(),staticNatPolicyDTO.getInDevItfAlias(),staticNatPolicyDTO.getOutDevItfAlias(), "", isVsys, vsysName, null, null, null);




        logger.info("????????????????????????:" + JSONObject.toJSONString(cmdDTO));
        cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT);
        pushTaskService.addGenerateCmdTask(cmdDTO);

        JSONObject rs = getReturnJSON(ReturnCode.POLICY_MSG_OK);
        rs.put("taskId", entity.getTaskId());
        rs.put("pushTaskId", entity.getId());
        return rs;
    }

    @ApiOperation("???Nat????????????")
    @ApiImplicitParams({
    })
    @PostMapping("task/addsrcnatpolicy")
    public JSONObject addSrcNatPolicyPolicy(@RequestBody SNatPolicyDTO sNatPolicyDTO, Authentication auth) {
        logger.info("??????sNat??????" + JSONObject.toJSONString(sNatPolicyDTO));
        if (sNatPolicyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }
        int rc = taskService.insertSrcNatPolicy(sNatPolicyDTO,auth);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", sNatPolicyDTO.getTaskId());
        rs.put("pushTaskId", sNatPolicyDTO.getId());
        return rs;
    }

    @ApiOperation("??????Nat????????????")
    @ApiImplicitParams({
    })
    @PostMapping("task/adddstnatpolicy")
    public JSONObject addDstNatPolicyPolicy(@RequestBody DNatPolicyDTO policyDTO, Authentication auth) {
        logger.info("??????dnat??????" + JSONObject.toJSONString(policyDTO));
        if (policyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }
        int rc = taskService.insertDstNatPolicy(policyDTO,auth);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", policyDTO.getTaskId());
        rs.put("pushTaskId", policyDTO.getId());
        return rs;
    }

    private void getBranch(String userName, RecommendTaskEntity recommendTaskEntity) {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        if (userInfoDTO != null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
            recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            recommendTaskEntity.setBranchLevel("00");
        }
    }

    @ApiOperation("??????Nat????????????")
    @PostMapping("task/addbothnatpolicy")
    public JSONObject addBothNatPolicy(@RequestBody NatPolicyDTO policyDTO, Authentication auth) {
        logger.info("????????????nat??????" + JSONObject.toJSONString(policyDTO));
        if (policyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }
        int rc = taskService.insertBothNatPolicy(policyDTO, auth);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", policyDTO.getTaskId());
        rs.put("pushTaskId", policyDTO.getId());
        return rs;
    }


    @ApiOperation(value = "????????????", httpMethod = "POST", notes = "????????????????????????????????????????????????????????????????????????")
    @RequestMapping(value = "task/new-policy-push", method = RequestMethod.POST)
    public JSONObject newPolicyPush(@ApiParam(name = "newPolicyPushVO", value = "????????????", required = true) @RequestBody NewPolicyPushVO newPolicyPushVO, Authentication auth) throws Exception {
        newPolicyPushVO.setUserName("");
        int rc = pushTaskService.newPolicyPush(newPolicyPushVO);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", newPolicyPushVO.getTaskId());
        rs.put("pushTaskId", newPolicyPushVO.getTaskId());
        return getReturnJSON(rc);
    }

    @ApiOperation(value = "????????????????????????", httpMethod = "POST", notes = "???????????????????????????????????????????????????????????????")
    @RequestMapping(value = "task/customizecmd", method = RequestMethod.POST)
    public ReturnT newCustomizeCmd(@ApiParam(name = "newPolicyPushVO", value = "????????????", required = true) @RequestBody NewPolicyPushVO newPolicyPushVO, Authentication auth) throws Exception {
        newPolicyPushVO.setUserName(auth.getName());
        int rc = pushTaskService.newCustomizeCmd(newPolicyPushVO);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", newPolicyPushVO.getTaskId());
        rs.put("pushTaskId", newPolicyPushVO.getTaskId());
        return new ReturnT(rs);
    }

    @ApiOperation(value = "??????????????????", httpMethod = "POST", notes = "????????????Excel???????????????????????????")
    @PostMapping("task/generatesecurity")
    public JSONObject importPolicyGenerate(MultipartFile file, Authentication auth, HttpServletResponse response) {
        logger.info("??????????????????????????????");
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        String userName = auth.getName();
        JSONObject jsonObject = new JSONObject();
        List<RecommendTaskEntity> tmpList = new ArrayList<>();
        List<RecommendTaskEntity> tmpNatList = new ArrayList<>();
        List<PushRecommendStaticRoutingDTO> tmpRouteList = new ArrayList<>();
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        errmsg = generateExcelParser.parse(file, userInfoDTO, tmpList, tmpNatList,tmpRouteList);
        if (!AliStringUtils.isEmpty(errmsg)) {
            return returnJSON(status, jsonObject, errcode, errmsg);
        }
        String  id = "batch_import_" + DateUtil.getTimeStamp();
        batchImportExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"??????????????????","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                //????????????????????????????????????
                for (RecommendTaskEntity entity : tmpList) {

                    String id = "generate_" + entity.getId();
                    if (ExtendedExecutor.containsKey(id)) {
                        logger.warn(String.format("??????????????????(%s)????????????????????????????????????", id));
                        continue;
                    }

                    // ???????????????????????????????????????
                    generateExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "????????????????????????", "", new Date())) {
                        @Override
                        protected void start() throws InterruptedException, Exception {
                            String additioncalInfoString = entity.getAdditionInfo();
                            JSONObject object = JSONObject.parseObject(additioncalInfoString);
                            PushAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(PushAdditionalInfoEntity.class);
                            if(CollectionUtils.isNotEmpty(additionalInfoEntity.getScenesDTOList())){
                                List<DisposalScenesDTO> scenesDTOList = additionalInfoEntity.getScenesDTOList();
                                for (DisposalScenesDTO disposalScenesDTO : scenesDTOList){
                                    NodeEntity node = taskService.getTheNodeByUuid(disposalScenesDTO.getDeviceUuid());
                                    generateCommandLineTask(entity, additionalInfoEntity, userInfoDTO, node, userName,disposalScenesDTO.getSrcZoneName(),disposalScenesDTO.getDstZoneName());
                                }
                            } else {
                                NodeEntity node = policyRecommendTaskService.getDeviceByManageIp(entity.getDeviceIp());
                                generateCommandLineTask(entity, additionalInfoEntity, userInfoDTO, node, userName,additionalInfoEntity.getSrcZone(),additionalInfoEntity.getDstZone());
                            }
                        }
                    });
                }
            }
        });

        if (tmpNatList.size() > 0) {
            logger.info("??????NAT??????...");

            policyRecommendTaskService.insertRecommendTaskList(tmpNatList);

            generateExcelParser.createNatCommandTask(tmpNatList, auth);
        }

        if (tmpRouteList.size() > 0) {
            logger.info("????????????????????????...");
            for (PushRecommendStaticRoutingDTO routeDTO : tmpRouteList) {
                pushTaskStaticRoutingService.createPushTaskStaticRouting(routeDTO);
            }
        }

        String message = String.format("?????????????????????????????????????????????%s???", tmpNatList.size() + tmpList.size());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);


        status = "0";
        return returnJSON(status, jsonObject, errcode, errmsg);
    }

    public static void main(String[] args) {
        JSONObject object = JSONObject.parseObject("");
        int a = object.size();
        JSONObject object1 = JSONObject.parseObject("{\"key\":12}");
        int b = object1.size();
        PushAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(PushAdditionalInfoEntity.class);
        if(additionalInfoEntity != null && CollectionUtils.isNotEmpty(additionalInfoEntity.getScenesDTOList())){
            System.out.println("aa");
        } else {
            System.out.println("ss");
        }
    }
    /**
     * ??????????????????????????????
     *
     * @param response response??????
     * @return
     */
    @RequestMapping("task/generatesecurityresult")
    public JSONObject importPolicyGenerateResult(HttpServletResponse response) {
        logger.info("??????????????????");
        String status = "-1";
        String errcode = "";
        String errmsg = "";

        JSONObject jsonObject = new JSONObject();

        String fileUrl = serverRootBasedir + "/service/push/cmd/????????????????????????.xls";
        File resultFile = new File(fileUrl);

        InputStream fin = null;
        ServletOutputStream out = null;
        try {
            fin = new FileInputStream(resultFile);

            response.setCharacterEncoding("utf-8");
            response.setContentType("application/msword");
            // ???????????????????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename="
                    .concat(String.valueOf(URLEncoder.encode("????????????????????????.xls", "UTF-8"))));
            response.setCharacterEncoding("utf-8");
            //??????????????????????????????,??????????????????????????????????????????
            out = response.getOutputStream();
            byte[] buffer = new byte[512];  // ?????????
            int bytesToRead = -1;
            // ????????????????????????Word????????????????????????????????????
            while ((bytesToRead = fin.read(buffer)) != -1) {
                out.write(buffer, 0, bytesToRead);
            }
        } catch (Exception e) {
            logger.error("?????????????????????", e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
                ;
                if (out != null) {
                    out.close();
                }
                ;
                if (resultFile != null) {
                    resultFile.delete();
                }
                ;
            } catch (IOException e) {
                logger.error("io?????????");
            }
        }

        //????????????????????????????????????????????????return????????????????????????
        return null;
    }

    @ApiOperation("????????????????????????Excel????????????")
    @PostMapping("task/downloadsecuritytemplate")
    public JSONObject downloadHostTemplate() {
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", resourceHandler.replace("**", "") + securityExcelFile);
            status = "0";
        } catch (Exception e) {
            errmsg += e;
            logger.error("downloadHostTemplate???" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg);
    }


    @ApiOperation("????????????")
    @PostMapping("task/objectReuse")
    public TotemsReturnT objectReuse(@RequestBody  CmdDTO cmdDTO) throws Exception{
        List<Integer> steps= cmdDTO.getProcedure().getSteps();
        if ( CollectionUtils.isEmpty(steps) ){
            steps = new ArrayList();
            steps.add(13);
            steps.add(14);
        }
        for (int step : steps) {
            SubServiceEnum subService = SubServiceEnum.valueOf(step);
            String serviceName = NameUtils.getServiceDefaultName(subService.getServiceClass());
            CmdService service = cmdServiceMap.get(serviceName);
            service.modify(cmdDTO);
        }
        return new TotemsReturnT(cmdDTO);
    }

    @ApiOperation(value = "????????????id??????????????????????????????", httpMethod = "POST")
    @RequestMapping(value = "/task/generateDeleteCommandLine",  method = RequestMethod.POST)
    public TotemsReturnT generateDeleteCommandLine(@ApiParam(name = "deviceUuid", value = "??????uuid", required = true)  @RequestParam(required = true) String deviceUuid,
                                                       @ApiParam(name = "policyId", value = "??????id", required = false) @RequestParam(required = false) Integer policyId,
                                                       @ApiParam(name = "ipType", value = "ip??????", required = true) @RequestParam(required = true) Integer ipType,
                                                       @ApiParam(name = "policyName", value = "????????????", required = false)@RequestParam(required = false)  String policyName,
                                                       @ApiParam(name = "srcZone", value = "??????", required = false) @RequestParam(required = false) String srcZone,
                                                       @ApiParam(name = "dstZone", value = "?????????", required = false) @RequestParam(required = false) String dstZone,
                                                       @ApiParam(name = "userName", value = "?????????", required = false) @RequestParam(required = false) String userName) throws Exception{

        NodeEntity nodeEntity = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
        if (nodeEntity == null){
            return new TotemsReturnT(-1,"?????????????????????");
        }
        int i = pushTaskService.generateDeleteCommandLine(nodeEntity, policyId, ipType, policyName, srcZone, dstZone,userName);
        return new TotemsReturnT(i);
    }


    protected String getZone(String zone) {
        if (zone == null) {
            return "";
        }
        return zone.equals("-1") ? "" : zone;
    }


    protected void addRecommendTask(RecommendTaskEntity entity) {
        logger.info("????????????????????????:" + JSONObject.toJSONString(entity));
        List<RecommendTaskEntity> list = new ArrayList<>();
        list.add(entity);
        int count = taskService.insertRecommendTaskList(list);
        String policyTypeDesc = "";
        if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT) {
            policyTypeDesc = "??????Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT) {
            policyTypeDesc = "???Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT) {
            policyTypeDesc = "??????Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT) {
            policyTypeDesc = "Both Nat";
        } else {
            policyTypeDesc = "??????";
        }

        String message = String.format("??????%s??????%s%s", policyTypeDesc, entity.getTheme(), count > 0 ? "??????" : "??????");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
    }

    protected CommandTaskEditableEntity createCommandTask(Integer taskType, Integer id, String userName, String theme, String deviceUuid) {
        CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
        entity.setCreateTime(new Date());
        entity.setStatus(PushConstants.PUSH_INT_PUSH_GENERATING);
        entity.setUserName(userName);
        entity.setTheme(theme);
        entity.setDeviceUuid(deviceUuid);
        entity.setTaskId(id);
        entity.setTaskType(taskType);
        return entity;
    }

    /**
     * ???????????????IP???????????????????????????????????????
     * updated by liuchanghao
     * @param entity
     * @param additionalInfoEntity
     * @param userInfoDTO
     * @param node
     * @param userName
     * @param srcZone
     * @param dstZone
     */
    private void generateCommandLineTask(RecommendTaskEntity entity, PushAdditionalInfoEntity additionalInfoEntity, UserInfoDTO userInfoDTO, NodeEntity node, String userName,
                                         String srcZone, String dstZone){
        //??????????????????
        List<ServiceDTO> serviceList = new ArrayList<>();
        if (entity.getServiceList() == null) {
            logger.info("????????????????????????");
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceList.add(serviceDTO);

        } else {
            JSONArray array = JSONArray.parseArray(entity.getServiceList());
            serviceList = array.toJavaList(ServiceDTO.class);
        }

        String deviceUuid = node.getUuid();

        //?????????????????????
        CommandTaskEditableEntity commandTaskEntity = createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,
                entity.getId(), entity.getUserName(), entity.getTheme(), deviceUuid);
        commandTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        commandTaskManager.addCommandEditableEntityTask(commandTaskEntity);

        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = device.getData().get(0);
        boolean isVsys = false;
        String vsysName = "";
        if (deviceData.getIsVsys() != null) {
            isVsys = deviceData.getIsVsys();
            vsysName = deviceData.getVsysName();
        }
        ActionEnum action = ActionEnum.PERMIT;
        if (additionalInfoEntity.getAction().equalsIgnoreCase(PolicyConstants.POLICY_STR_PERMISSION_DENY)) {
            action = ActionEnum.DENY;
        }
        MoveSeatEnum moveSeat = MoveSeatEnum.FIRST;
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        String startTimeString = entity.getStartTime() == null ? null : sdf.format(entity.getStartTime());
        String endTimeString = entity.getEndTime() == null ? null : sdf.format(entity.getEndTime());

        PolicyEnum type = null;
        String modelNumber = node.getModelNumber();
        if(!AliStringUtils.isEmpty(modelNumber) && DeviceTypeEnum.ROUTER.name().equalsIgnoreCase(deviceData.getDeviceType()) &&
                (modelNumber.equals("Cisco IOS") || modelNumber.equals("Cisco NX-OS"))) {
            type = PolicyEnum.ACL;
        }else{
            // ?????????????????????????????????????????????,????????????????????????(??????????????????????????????,??????????????????ACL)
            type = PolicyEnum.SECURITY;
        }
        String inItfAlias = additionalInfoEntity.getInDevItfAlias();
        String outItfAlias = additionalInfoEntity.getOutDevItfAlias();
        if (StringUtils.isNotEmpty(modelNumber) && DeviceModelNumberEnum.CISCO.getKey().equals(modelNumber)) {
            //KSH-5412 ???cisco???Excel?????????????????????????????????nat???????????????????????????????????????????????????????????????????????????????????????????????????
            inItfAlias = StringUtils.isNotEmpty(additionalInfoEntity.getInDevItf()) ? additionalInfoEntity.getInDevItf() : additionalInfoEntity.getSrcZone();
            outItfAlias = StringUtils.isNotEmpty(additionalInfoEntity.getOutDevItf()) ? additionalInfoEntity.getOutDevItf() : additionalInfoEntity.getDstZone();
        }
        //?????????????????????
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(type, commandTaskEntity.getId(), commandTaskEntity.getTaskId(), node.getUuid(), entity.getTheme(), userName,
                entity.getSrcIp(), entity.getDstIp(), null, null, serviceList,
                null, srcZone, dstZone, additionalInfoEntity.getInDevItf(), additionalInfoEntity.getOutDevItf(), inItfAlias, outItfAlias,
                startTimeString, endTimeString, entity.getDescription(), action, isVsys, vsysName, moveSeat, null, null, entity.getIdleTimeout(), entity.getSrcIpSystem(), entity.getDstIpSystem(), entity.getIpType(), entity.getPostSrcIpSystem(),
                null, null, null, null, null,null);
        cmdDTO.getTask().setBeforeConflict(entity.getBeforeConflict());
        cmdDTO.getTask().setMergeCheck(entity.getMergeCheck());
        cmdDTO.getTask().setRangeFilter(entity.getRangeFilter());
        cmdDTO.getTask()
            .setTaskTypeEnum(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED == entity.getTaskType().intValue()
                ? TaskTypeEnum.SECURITY_TYPE : null);
        cmdDTO.getDevice().setNodeEntity(node);
        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        if (DeviceModelNumberEnum.isRangeHillStoneCode(deviceModelNumberEnum.getCode())) {
            String paramValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE);
            if (StringUtils.isNotEmpty(paramValue)) {
                if (paramValue.equals("0")) {
                    cmdDTO.getSetting().setRollbackType(true);
                } else {
                    cmdDTO.getSetting().setRollbackType(false);
                }
            }
        }
        logger.info("????????????????????????:" + JSONObject.toJSONString(cmdDTO));
        cmdTaskService.getRuleMatchFlow2Generate(cmdDTO, userInfoDTO);
    }



}


