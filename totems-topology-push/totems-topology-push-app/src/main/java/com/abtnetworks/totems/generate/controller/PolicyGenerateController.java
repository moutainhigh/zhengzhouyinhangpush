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
@Api(tags = "命令行生成列表")
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


    @ApiOperation("NAT策略生成列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ids", value = "id集合", required = false, dataType = "String")
    })
    @PostMapping("task/searchnatpolicytasklist")
    public JSONObject getNatPolicyTaskList(String theme, String type, int page, int psize, String ids, Integer id, String userName, String deviceUuid,String pushStatus , Authentication authentication) {

        PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getNatPolicyTaskList(theme, type, page, psize, ids, id, userName, deviceUuid,pushStatus,  authentication);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }


    @ApiOperation("安全策略生成列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "Integer")
    })
    @PostMapping("task/searchsecuritypolicytasklist")
    public JSONObject getSecurityPolicyTaskList(String theme, int page, int psize, String userName, String deviceUuid,String pushStatus , Authentication authentication) {

        PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getSecurityPolicyTaskList(theme, page, psize, userName, deviceUuid,pushStatus,  authentication);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("自定义命令行列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "String")
    })
    @PostMapping("task/searchcustomizecmdtasklist")
    public ReturnT getCustomizeCmdTaskList(String theme, int page, int psize, String userName, String deviceUuid, Integer pushStatus , Authentication authentication) {

        PageInfo<PolicyTaskDetailVO> pageInfo = policyRecommendTaskService.getCustomizeCmdTaskList(theme, page, psize, userName, deviceUuid,pushStatus,  authentication);

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return new ReturnT(jsonObject);
    }

    @ApiOperation(value = "策略生成excel导出")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "是否重新下载", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String")
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
        String preFilename = "策略生成导出";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            logger.error("生成策略生成报表文件名称异常", e1);
        }

        String destDirName = dirPath + "/policyGenerateExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "policyGeneratedoing.temp";

        try {
            // 生成策略开通文件夹
            if (!new File(destDirName).exists()) {
                FileUtils.createDir(destDirName);
            }

            String fileIsExistsName = FileUtils.isDirExistFile(destDirName);
            boolean doingFileTempIsExists = FileUtils.fileIsExists(doingFileTemp);
            boolean fileIsExists = FileUtils.fileIsExists(destDirName + "/" + fileIsExistsName);
            if (null == isReload) {
                if (fileIsExists && doingFileTempIsExists == false) {
                    resultRO.setMessage("文件生成成功");
                    jsonObject.put("filePath", fileIsExistsName);
                    jsonObject.put("status", 1);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists) {
                    // 有正在生成的临时文件
                    resultRO.setMessage("文件生成中");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists == false && fileIsExists == false) {
                    // 生成临时文件
                    File doingFile = new File(doingFileTemp);
                    doingFile.createNewFile();
                    resultRO.setMessage("生成成功");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    new PolicyGenerateThread(filePath, doingFile, taskArrayList).start();
                    resultRO.setData(jsonObject);
                    return resultRO;
                }
            }
            if ("true".equals(isReload)) {
                // 生成临时文件
                FileUtils.deleteFileByPath(destDirName + "/" + fileIsExistsName);
                // 不存在获取数据从新下载
                // 生成临时文件
                File doingFile = new File(doingFileTemp);
                doingFile.createNewFile();
                new PolicyGenerateThread(filePath, doingFile, taskArrayList).start();
                resultRO.setMessage("正在生成文件");
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
            logger.error("下载策略生成Excel表格失败:", e);
            resultRO.setMessage("数据导出失败");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }


    @ApiOperation("策略生成批量删除")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "策略生成任务id数组", required = true, dataType = "String")
    })
    @PostMapping("task/deletesecuritypolicytasklist")
    public JSONObject deletePolicyTaskList(@RequestParam String ids) {

        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("要删除的下发策略任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_DELETE);
        }
        List<Integer> idList = null;
        try {
            idList = com.abtnetworks.totems.common.utils.StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("解析下发策略任务出错！", e);
        }

        //检测选中列表删除的任务中是否有在下发中或者回滚中的，有的话则不进行删除。
        StringBuilder errMsg = new StringBuilder();
        int rc = ReturnCode.POLICY_MSG_OK;
        List<String> themeList = new ArrayList<>();
        int tasking = 0;
        for (int id : idList) {
            logger.info(String.format("获取任务(%d)", id));
            CommandTaskEditableEntity taskEntity = policyRecommendTaskService.getRecommendTaskById(id);
            // 如果删除策略生成工单表数据的时候还未生成命令行数据，或者生成报错了没有命令行数据，则直接删除工单表，跳过状态判断
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

        String message = String.format("删除策略生成和下发工单：%s 成功", StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK);
    }

    @ApiOperation("静态策略生成")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "Integer")
    })
    @PostMapping("task/addstaticnatpolicy")
    public JSONObject addStaticNatPolicyPolicy(@RequestBody StaticNatPolicyDTO staticNatPolicyDTO, Authentication auth) {
        logger.info("添加静态nat策略" + JSONObject.toJSONString(staticNatPolicyDTO));
        if (staticNatPolicyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }

        //检测设备是否存在
        String deviceUuid = staticNatPolicyDTO.getDeviceUuid();
        NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return getReturnJSON(ReturnCode.DEVICE_NOT_EXIST);
        }

        //获取用户名
        String userName = auth.getName();

        //格式化域信息
        staticNatPolicyDTO.setSrcZone(getZone(staticNatPolicyDTO.getSrcZone()));
        staticNatPolicyDTO.setDstZone(getZone(staticNatPolicyDTO.getDstZone()));

        //创建静态NAT附加信息数据对象
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


        //添加任务到新表
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




        logger.info("命令行生成任务为:" + JSONObject.toJSONString(cmdDTO));
        cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT);
        pushTaskService.addGenerateCmdTask(cmdDTO);

        JSONObject rs = getReturnJSON(ReturnCode.POLICY_MSG_OK);
        rs.put("taskId", entity.getTaskId());
        rs.put("pushTaskId", entity.getId());
        return rs;
    }

    @ApiOperation("源Nat策略生成")
    @ApiImplicitParams({
    })
    @PostMapping("task/addsrcnatpolicy")
    public JSONObject addSrcNatPolicyPolicy(@RequestBody SNatPolicyDTO sNatPolicyDTO, Authentication auth) {
        logger.info("添加sNat策略" + JSONObject.toJSONString(sNatPolicyDTO));
        if (sNatPolicyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }
        int rc = taskService.insertSrcNatPolicy(sNatPolicyDTO,auth);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", sNatPolicyDTO.getTaskId());
        rs.put("pushTaskId", sNatPolicyDTO.getId());
        return rs;
    }

    @ApiOperation("目的Nat策略生成")
    @ApiImplicitParams({
    })
    @PostMapping("task/adddstnatpolicy")
    public JSONObject addDstNatPolicyPolicy(@RequestBody DNatPolicyDTO policyDTO, Authentication auth) {
        logger.info("添加dnat策略" + JSONObject.toJSONString(policyDTO));
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

    @ApiOperation("双向Nat策略生成")
    @PostMapping("task/addbothnatpolicy")
    public JSONObject addBothNatPolicy(@RequestBody NatPolicyDTO policyDTO, Authentication auth) {
        logger.info("添加双向nat策略" + JSONObject.toJSONString(policyDTO));
        if (policyDTO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }
        int rc = taskService.insertBothNatPolicy(policyDTO, auth);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", policyDTO.getTaskId());
        rs.put("pushTaskId", policyDTO.getId());
        return rs;
    }


    @ApiOperation(value = "新建策略", httpMethod = "POST", notes = "新建策略，根据策略参数生成建议策略，并生成命令行")
    @RequestMapping(value = "task/new-policy-push", method = RequestMethod.POST)
    public JSONObject newPolicyPush(@ApiParam(name = "newPolicyPushVO", value = "新建策略", required = true) @RequestBody NewPolicyPushVO newPolicyPushVO, Authentication auth) throws Exception {
        newPolicyPushVO.setUserName("");
        int rc = pushTaskService.newPolicyPush(newPolicyPushVO);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", newPolicyPushVO.getTaskId());
        rs.put("pushTaskId", newPolicyPushVO.getTaskId());
        return getReturnJSON(rc);
    }

    @ApiOperation(value = "新建自定义命令行", httpMethod = "POST", notes = "新建自定义命令行，用户可自由输入下发命令行")
    @RequestMapping(value = "task/customizecmd", method = RequestMethod.POST)
    public ReturnT newCustomizeCmd(@ApiParam(name = "newPolicyPushVO", value = "新建策略", required = true) @RequestBody NewPolicyPushVO newPolicyPushVO, Authentication auth) throws Exception {
        newPolicyPushVO.setUserName(auth.getName());
        int rc = pushTaskService.newCustomizeCmd(newPolicyPushVO);
        JSONObject rs = getReturnJSON(rc);
        rs.put("taskId", newPolicyPushVO.getTaskId());
        rs.put("pushTaskId", newPolicyPushVO.getTaskId());
        return new ReturnT(rs);
    }

    @ApiOperation(value = "批量策略生成", httpMethod = "POST", notes = "根据导入Excel表，批量生成命令行")
    @PostMapping("task/generatesecurity")
    public JSONObject importPolicyGenerate(MultipartFile file, Authentication auth, HttpServletResponse response) {
        logger.info("批量添加策略生成任务");
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
        batchImportExecutor.execute(new ExtendedRunnable(new ExecutorDto(id,"批量新增策略","",new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {
                //记录到数据库并生成命令行
                for (RecommendTaskEntity entity : tmpList) {

                    String id = "generate_" + entity.getId();
                    if (ExtendedExecutor.containsKey(id)) {
                        logger.warn(String.format("策略仿真任务(%s)已经存在！任务不重复添加", id));
                        continue;
                    }

                    // 异步处理命令行生成入库流程
                    generateExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "批量新增策略生成", "", new Date())) {
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
            logger.info("添加NAT策略...");

            policyRecommendTaskService.insertRecommendTaskList(tmpNatList);

            generateExcelParser.createNatCommandTask(tmpNatList, auth);
        }

        if (tmpRouteList.size() > 0) {
            logger.info("添加静态路由策略...");
            for (PushRecommendStaticRoutingDTO routeDTO : tmpRouteList) {
                pushTaskStaticRoutingService.createPushTaskStaticRouting(routeDTO);
            }
        }

        String message = String.format("批量导入策略生成数据成功，共计%s条", tmpNatList.size() + tmpList.size());
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
     * 下载批量策略生成文件
     *
     * @param response response数据
     * @return
     */
    @RequestMapping("task/generatesecurityresult")
    public JSONObject importPolicyGenerateResult(HttpServletResponse response) {
        logger.info("批量添加任务");
        String status = "-1";
        String errcode = "";
        String errmsg = "";

        JSONObject jsonObject = new JSONObject();

        String fileUrl = serverRootBasedir + "/service/push/cmd/策略批量生成结果.xls";
        File resultFile = new File(fileUrl);

        InputStream fin = null;
        ServletOutputStream out = null;
        try {
            fin = new FileInputStream(resultFile);

            response.setCharacterEncoding("utf-8");
            response.setContentType("application/msword");
            // 设置浏览器以下载的方式处理该文件名
            response.setHeader("Content-Disposition", "attachment;filename="
                    .concat(String.valueOf(URLEncoder.encode("策略批量生成结果.xls", "UTF-8"))));
            response.setCharacterEncoding("utf-8");
            //您在这里稍微注意一下,中文在火狐下会出现乱码的现象
            out = response.getOutputStream();
            byte[] buffer = new byte[512];  // 缓冲区
            int bytesToRead = -1;
            // 通过循环将读入的Word文件的内容输出到浏览器中
            while ((bytesToRead = fin.read(buffer)) != -1) {
                out.write(buffer, 0, bytesToRead);
            }
        } catch (Exception e) {
            logger.error("压缩包下载异常", e);
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
                logger.error("io流异常");
            }
        }

        //上面流已经返回了，后面不能有新的return值，否则会抛异常
        return null;
    }

    @ApiOperation("下载批量策略生成Excel任务模板")
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
            logger.error("downloadHostTemplate：" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg);
    }


    @ApiOperation("对象复用")
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

    @ApiOperation(value = "根据策略id或名称生成删除命令行", httpMethod = "POST")
    @RequestMapping(value = "/task/generateDeleteCommandLine",  method = RequestMethod.POST)
    public TotemsReturnT generateDeleteCommandLine(@ApiParam(name = "deviceUuid", value = "设备uuid", required = true)  @RequestParam(required = true) String deviceUuid,
                                                       @ApiParam(name = "policyId", value = "策略id", required = false) @RequestParam(required = false) Integer policyId,
                                                       @ApiParam(name = "ipType", value = "ip类型", required = true) @RequestParam(required = true) Integer ipType,
                                                       @ApiParam(name = "policyName", value = "策略名称", required = false)@RequestParam(required = false)  String policyName,
                                                       @ApiParam(name = "srcZone", value = "源域", required = false) @RequestParam(required = false) String srcZone,
                                                       @ApiParam(name = "dstZone", value = "目的域", required = false) @RequestParam(required = false) String dstZone,
                                                       @ApiParam(name = "userName", value = "用户名", required = false) @RequestParam(required = false) String userName) throws Exception{

        NodeEntity nodeEntity = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
        if (nodeEntity == null){
            return new TotemsReturnT(-1,"该设备不存在！");
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
        logger.info("策略下发新增任务:" + JSONObject.toJSONString(entity));
        List<RecommendTaskEntity> list = new ArrayList<>();
        list.add(entity);
        int count = taskService.insertRecommendTaskList(list);
        String policyTypeDesc = "";
        if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT) {
            policyTypeDesc = "静态Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT) {
            policyTypeDesc = "源Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT) {
            policyTypeDesc = "目的Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT) {
            policyTypeDesc = "Both Nat";
        } else {
            policyTypeDesc = "未知";
        }

        String message = String.format("新建%s策略%s%s", policyTypeDesc, entity.getTheme(), count > 0 ? "成功" : "失败");
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
     * 抽离出导入IP和场景生成命令行的公共方法
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
        //设置服务对象
        List<ServiceDTO> serviceList = new ArrayList<>();
        if (entity.getServiceList() == null) {
            logger.info("新建策略服务为空");
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

        //添加任务到新表
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
            // 如果前面流程没有获取到策略类型,则默认走安全策略(前面有走路由交换设备,这里类型就是ACL)
            type = PolicyEnum.SECURITY;
        }
        String inItfAlias = additionalInfoEntity.getInDevItfAlias();
        String outItfAlias = additionalInfoEntity.getOutDevItfAlias();
        if (StringUtils.isNotEmpty(modelNumber) && DeviceModelNumberEnum.CISCO.getKey().equals(modelNumber)) {
            //KSH-5412 ：cisco从Excel模板导入生成安全策略和nat策略获取接口别名时，优先获取表格中的进出接口，若无接口再获取安全域
            inItfAlias = StringUtils.isNotEmpty(additionalInfoEntity.getInDevItf()) ? additionalInfoEntity.getInDevItf() : additionalInfoEntity.getSrcZone();
            outItfAlias = StringUtils.isNotEmpty(additionalInfoEntity.getOutDevItf()) ? additionalInfoEntity.getOutDevItf() : additionalInfoEntity.getDstZone();
        }
        //添加任务到新表
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
        logger.info("命令行生成任务为:" + JSONObject.toJSONString(cmdDTO));
        cmdTaskService.getRuleMatchFlow2Generate(cmdDTO, userInfoDTO);
    }



}


