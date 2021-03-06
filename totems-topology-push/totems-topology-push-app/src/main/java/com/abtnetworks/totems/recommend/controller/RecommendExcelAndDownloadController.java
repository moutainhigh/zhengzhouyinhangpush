package com.abtnetworks.totems.recommend.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.executor.PolicyAddThread;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.tools.excel.ImportExcel;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.FileUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.service.PushTaskStaticRoutingService;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.recommend.dto.excel.DataRecommendForExcelDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelBigInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendTaskDTO;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.PushRecommendTaskHistoryEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskCheckEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.*;
import com.abtnetworks.totems.recommend.task.impl.SimulationTaskServiceImpl;
import com.abtnetworks.totems.recommend.vo.excel.ExcelImportResultVO;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.CommonConstants.HOUR_SECOND;
import static com.abtnetworks.totems.common.constants.PolicyConstants.BIG_INTERNET_RECOMMEND;
import static com.abtnetworks.totems.common.constants.PolicyConstants.LABEL_MODEL_OR;

/**
 * @author Administrator
 * @Title:
 * @Description: ???????????????????????????????????????????????????????????????
 * @date 2021/1/7
 */
@Slf4j
@Api(value = "?????????????????????????????????")
@RestController
@RequestMapping(value = "/recommend/")
public class RecommendExcelAndDownloadController extends BaseController {

    @Resource
    RecommendExcelAndDownloadService recommendExcelAndDownloadService;

    @Resource
    RecommendBussCommonService recommendBussCommonService;

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    private PushTaskService pushTaskService;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    ExcelParser excelParser;

    @Autowired
    LogClientSimple logClientSimple;

    @Autowired
    WhatIfService whatIfService;

    @Autowired
    SimulationTaskServiceImpl recommendTaskManager;

    @Value("${resourceHandler}")
    String resourceHandler;

    @Value("${importHostExcelFileName}")
    String importHostExcelFileName;

    @Value("${push.download-file}")
    String dirPath;

    @Autowired
    VerifyBusinessService verifyBusinessService;

    @Autowired
    PushTaskStaticRoutingService pushTaskStaticRoutingService;

    @Autowired
    RecommendRelevanceSceneService recommendRelevanceSceneService;

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "file", value = "?????????Excel??????", required = false, dataType = "String")
    })
    @PostMapping("task/import")
    public JSONObject importPolicyRecommendTaskList(MultipartFile file, String filename, String creator,Boolean autoStartRecommend, Authentication auth) {
        log.info("??????????????????");
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        UserInfoDTO userInfoDTO = recommendExcelAndDownloadService.getUserInfo(creator, auth);
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        String orderNumber = "";
        try {
            int successNum = 0;
            int failureNum = 0;
            StringBuilder failureMsg = new StringBuilder();
            //???????????????????????? ????????????????????????????????????
            ImportExcel ei = new ImportExcel(file, 0, 0);

            // ????????????????????????
            ImportExcel internetExcel = new ImportExcel(file, 0, 1);
            // ??????????????????????????????
            ImportExcel bigInternetExcel = new ImportExcel(file, 0, 2);
            // ??????nat????????????????????????
            ImportExcel natExcel = new ImportExcel(file, 0, 3);
            // ????????????????????????????????????
            ImportExcel routeExcel = new ImportExcel(file, 0, 4);
            // ??????NAT??????
            ImportExcel specialNatExcel = new ImportExcel(file, 0, 5);



            List<RecommendTaskEntity> tmpList = new ArrayList<>();
            List<RecommendTaskEntity> natTaskList = new ArrayList<>();
            List<PushRecommendStaticRoutingDTO> routeTaskList = new ArrayList<>();
            List<RecommendRelevanceSceneDTO> specialNatList = new ArrayList<>();

            //????????????
            List<ExcelRecommendTaskDTO> list = ei.getDataList(ExcelRecommendTaskDTO.class);
            //nat??????
            List<ExcelTaskNatEntity> natExcelList = natExcel.getDataList(ExcelTaskNatEntity.class);
            //??????????????????????????????
            List<ExcelRecommendInternetTaskDTO> internetExcelDataList = internetExcel.getDataList(ExcelRecommendInternetTaskDTO.class);
            //???????????????
            List<ExcelBigInternetTaskDTO> excelBigInternetTaskDTOS = bigInternetExcel.getDataList(ExcelBigInternetTaskDTO.class);
            //??????????????????
            List<ExcelTaskStaticRouteEntity> routeExcelList = routeExcel.getDataList(ExcelTaskStaticRouteEntity.class);
            // ??????Nat(?????????????????????)
            List<ExcelTaskSpecialNatEntity> specialNatExcelList = specialNatExcel.getDataList(ExcelTaskSpecialNatEntity.class);

            String msg = recommendExcelAndDownloadService.checkExcelNatTaskValidation(natExcelList, filename);
            if (!AliStringUtils.isEmpty(msg)) {
                errmsg = msg;
                return returnJSON(status, jsonObject, errcode, errmsg);
            }

            // ??????????????????
            String routeErrorMsg = recommendExcelAndDownloadService.checkExcelRouteTaskValidation(routeExcelList);
            if (!AliStringUtils.isEmpty(routeErrorMsg)) {
                errmsg = routeErrorMsg;
                return returnJSON(status, jsonObject, errcode, errmsg);
            }
            routeTaskList = recommendBussCommonService.getRouteExcelDTO(routeExcelList,creator,userInfoDTO);

            // ????????????nat??????
            String specialNatErrorMsg = recommendExcelAndDownloadService.checkExcelSpecialNatValidation(specialNatExcelList);
            if (!AliStringUtils.isEmpty(specialNatErrorMsg)) {
                errmsg = specialNatErrorMsg;
                return returnJSON(status, jsonObject, errcode, errmsg);
            }
            specialNatList = recommendBussCommonService.getSpecialNatExcelDTO(specialNatExcelList,creator,userInfoDTO);

            String whatIfCaseName = String.format("A%s", String.valueOf(System.currentTimeMillis()));
            //??????NAT?????????????????????????????????????????????????????????
            WhatIfRO whatIf = recommendBussCommonService.createWhatIfCaseUuid(natExcelList, natTaskList,routeTaskList,specialNatList, whatIfCaseName, creator, userInfoDTO);


            int index = 0;
            boolean hasInvalid = false;
            Set<String> indexSet = new HashSet<>();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    log.info("Excel value:" + list.get(i).toString());
                    try {
                        int rowNum = i + 2;
                        if (list.get(i) != null
                                && !AliStringUtils.isEmpty(list.get(i).getSrcIp())
                                && !AliStringUtils.isEmpty(list.get(i).getDstIp())
                        ) {


                            ExcelRecommendTaskDTO entity = list.get(i);

                            if (entity.isEmpty()) {
                                log.error(String.format("??????????????????%d??????", rowNum));
                                continue;
                            }
                            int rc = verifyBusinessService.verifyRecommendBussExcel(entity).getResultCode();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("????????????????????????" + rowNum + "????????????" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("??????\n" + entity.toString() + "\n?????????" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (indexSet.contains(entity.getId())) {
                                failureMsg.append("????????????????????????" + rowNum + "??????????????????<br>");
                                log.error("????????????????????????" + rowNum + "??????????????????");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            } else {
                                indexSet.add(entity.getId());
                            }

                            // ??????????????????????????????
                            String orderNO = "A" + simpleDateFormat.format(new Date()) + index;
                            entity.setOrderNO(orderNO);

                            //????????????????????????????????????
                            if (AliStringUtils.isEmpty(entity.getName())) {
                                entity.setName(orderNO);
                            } else {
                                //??????????????????????????????
                                if (!AliStringUtils.isEmpty(entity.getId())) {
                                    String name = String.format("%s_%s", entity.getName(), entity.getId());
                                    //??????????????????????????????????????????????????????
                                    if (AliStringUtils.isEmpty(orderNumber)) {
                                        orderNumber = entity.getName();
                                    }
                                    entity.setName(name);
                                }
                            }
                            //????????????????????????????????????????????????????????????????????????
                            if (AliStringUtils.isEmpty(entity.getUser())) {
                                entity.setUser(userInfoDTO.getId());
                            }

                            //?????????????????????
                            entity.setSrcIpDescription(entity.getSrcIpDescription().trim());
                            entity.setDstIpDescription(entity.getDstIpDescription().trim());
                            entity.setName(entity.getName().trim());
                            if (!InputValueUtils.validOrderName(entity.getName())) {
                                failureMsg.append("????????????????????????" + rowNum + "????????????????????????????????????????????????????????????????????????64???????????????????????????????????????????????????(-)???????????????????????????<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            RecommendTaskEntity tmpEntity = entity.toTaskEntity();
                            tmpEntity.setStartLabel(entity.getStartLabel());
                            tmpEntity.setLabelModel(StringUtils.isNotBlank(entity.getLabelModel()) ? entity.getLabelModel() : LABEL_MODEL_OR);
                            tmpEntity.setIpType(IpTypeEnum.getIpTypeByDesc(entity.getIpType()).getCode());
                            if (userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
                                tmpEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                            } else {
                                tmpEntity.setBranchLevel("00");
                            }
                            if (whatIf != null) {
                                tmpEntity.setWhatIfCase(whatIf.getUuid());
                            }
                            log.info("idle timeout is " + entity.getIdleTimeout());
                            if (!AliStringUtils.isEmpty(entity.getIdleTimeout())) {
                                Integer idleTimeout = Integer.valueOf(entity.getIdleTimeout());
                                tmpEntity.setIdleTimeout(idleTimeout * HOUR_SECOND);
                            }

                            //??????????????????????????????????????????
                            tmpEntity.setSrcIpSystem(StringUtils.isBlank(tmpEntity.getSrcIpSystem()) ? null
                                    : tmpEntity.getSrcIpSystem().replace(" ", ""));
                            tmpEntity.setDstIpSystem(StringUtils.isBlank(tmpEntity.getDstIpSystem()) ? null
                                    : tmpEntity.getDstIpSystem().replace(" ", ""));
                            // ?????????????????????nat
                            int returnCode = recommendBussCommonService.checkPostRelevancyNat(tmpEntity, auth);
                            // ???????????????????????????????????? ????????????????????????????????? ?????????????????????????????????????????????
                            if (ReturnCode.POLICY_MSG_OK != returnCode) {
                                failureMsg.append("????????????????????????" + rowNum + "????????????" + ReturnCode.getMsg(returnCode) + "<br>");
                                log.info("??????\n" + entity.toString() + "\n?????????" + ReturnCode.getMsg(returnCode));
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }
                            tmpList.add(tmpEntity);
                        } else {
                            log.error(String.format("??????????????????%d??????", rowNum));
                            continue;
                        }
                    } catch (Exception ex) {
                        log.error("????????????????????????,????????????:{}", ex);
                        failureMsg.append(" ??????????????????: " + list.get(i).getOrderNO() + " ???????????????");
                        failureNum++;
                    }
                    index++;
                }

            } else {
                log.info("???????????????????????????????????????");
            }

            if (hasInvalid && StringUtils.isNotBlank(failureMsg)) {
                failureMsg.insert(0, " ?????? " + failureNum + "??????????????? ?????????????????????");
                errmsg = failureMsg.toString();
                return returnJSON(status, jsonObject, errcode, errmsg);
            }
            DataRecommendForExcelDTO dataRecommendForExcelDTO = new DataRecommendForExcelDTO();
            dataRecommendForExcelDTO.setFailureNum(failureNum);
            dataRecommendForExcelDTO.setFailureMsg(failureMsg);
            dataRecommendForExcelDTO.setHasInvalid(hasInvalid);

            dataRecommendForExcelDTO.setInternetExcelDataList(internetExcelDataList);
            dataRecommendForExcelDTO.setTmpList(tmpList);
            dataRecommendForExcelDTO.setUserInfoDTO(userInfoDTO);
            dataRecommendForExcelDTO.setWhatIf(whatIf);
            //???????????????
            ExcelImportResultVO excelImportResultInternetVO = recommendExcelAndDownloadService.savePolicyDataForExcel(dataRecommendForExcelDTO, simpleDateFormat,auth);
            //???????????????
            dataRecommendForExcelDTO.setExcelBigInternetTaskDTOS(excelBigInternetTaskDTOS);
            dataRecommendForExcelDTO.setTaskType(BIG_INTERNET_RECOMMEND);
            ExcelImportResultVO excelImportResultBigNetVO = recommendExcelAndDownloadService.saveBigInternetPolicyDataForExcel(dataRecommendForExcelDTO, simpleDateFormat);

            failureMsg = dataRecommendForExcelDTO.getFailureMsg();
            if (CollectionUtils.isEmpty(list) && CollectionUtils.isEmpty(internetExcelDataList) && CollectionUtils.isEmpty(excelBigInternetTaskDTOS)) {
                failureMsg.append("???????????????????????????");
            }
            if (dataRecommendForExcelDTO.getFailureNum() > 0) {
                failureMsg.insert(0, " ?????? " + dataRecommendForExcelDTO.getFailureNum() + "??????????????? ?????????????????????");
            }
            tmpList = dataRecommendForExcelDTO.getTmpList();
            StringBuilder sb = new StringBuilder();
            if (!dataRecommendForExcelDTO.getHasInvalid() && tmpList.size() > 0) {

                RecommendTaskCheckEntity checkEntity = new RecommendTaskCheckEntity();
                checkEntity.setBatchType(1);
                Date date = new Date();

                checkEntity.setCreateTime(date);
                //?????????????????????????????????????????????????????????
                if (StringUtils.isBlank(orderNumber)) {
                    if (StringUtils.isBlank(excelImportResultInternetVO.getOrderNumber())) {
                        if (StringUtils.isBlank(excelImportResultBigNetVO.getOrderNumber())) {
                            log.error("??????????????????????????????????????????");
                        } else {
                            checkEntity.setOrderNumber(excelImportResultBigNetVO.getOrderNumber());
                        }
                    } else {
                        checkEntity.setOrderNumber(excelImportResultInternetVO.getOrderNumber());
                    }
                } else {
                    if (StringUtils.isNotBlank(filename)) {
                        //api?????????????????????????????????
                        checkEntity.setOrderNumber(filename);
                    } else {
                        checkEntity.setOrderNumber(orderNumber);
                    }
                }

                checkEntity.setUserName(userInfoDTO.getId());
                checkEntity.setStatus(0);
                policyRecommendTaskService.addBatchTask(checkEntity);


                JSONArray relevancyNat = new JSONArray();
                // ??????nat??????
                if (natTaskList.size() > 0) {

                    for (RecommendTaskEntity entity : natTaskList) {
                        entity.setBatchId(checkEntity.getId());
                    }

                    policyRecommendTaskService.insertRecommendTaskList(natTaskList);

                    for (int i = 0; i < natTaskList.size(); i++) {
                        sb.append(",");
                        sb.append(natTaskList.get(i).getId());

                        NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(natTaskList.get(i).getDeviceIp());
                        // ??????relevancyNat
                        JSONObject tmp = new JSONObject();
                        tmp.put("index", i + 1);
                        tmp.put("name", natTaskList.get(i).getTheme() + "(" + nodeEntity.getDeviceName() + "(" + nodeEntity.getIp() + "))");
                        tmp.put("id", natTaskList.get(i).getId());
                        tmp.put("taskId", natTaskList.get(i).getId());
                        tmp.put("type", natTaskList.get(i).getTaskType());
                        relevancyNat.add(tmp);
                    }

                    excelParser.createNatCommandTask(natTaskList, auth);
                }

                int existNatSize = relevancyNat.size();

                // ????????????NAT????????????
                if(specialNatList.size() > 0){
                    for (int i = 0; i < specialNatList.size(); i++) {
                        recommendRelevanceSceneService.createRecommendRelevanceScene(specialNatList.get(i));
                        // ??????relevancyNat
                        JSONObject tmp = new JSONObject();
                        tmp.put("index", existNatSize + i + 1);
                        tmp.put("name", specialNatList.get(i).getName() + "(" + specialNatList.get(i).getDeviceName() + "(" + specialNatList.get(i).getDeviceIp() + "))");
                        tmp.put("id", specialNatList.get(i).getTaskId());
                        tmp.put("taskId", specialNatList.get(i).getTaskId());
                        tmp.put("type", PolicyConstants.POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT);
                        relevancyNat.add(tmp);
                    }
                }
                existNatSize = relevancyNat.size();

                // ????????????????????????
                if (routeTaskList.size() > 0) {
                    for (PushRecommendStaticRoutingDTO dto : routeTaskList) {
                        dto.setBatchId(checkEntity.getId());
                    }

                    pushTaskStaticRoutingService.batchCreateStaticRoute(routeTaskList);

                    for (int i = 0; i < routeTaskList.size(); i++) {
                        sb.append(",");
                        sb.append(routeTaskList.get(i).getTaskId());

                        NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(routeTaskList.get(i).getDeviceIp());
                        // ??????relevancyNat
                        JSONObject tmp = new JSONObject();
                        tmp.put("index", existNatSize + i + 1);
                        tmp.put("name", routeTaskList.get(i).getTheme() + "(" + nodeEntity.getDeviceName() + "(" + nodeEntity.getIp() + "))");
                        tmp.put("id", routeTaskList.get(i).getTaskId());
                        tmp.put("taskId", routeTaskList.get(i).getTaskId());
                        tmp.put("type", routeTaskList.get(i).getTaskType());
                        relevancyNat.add(tmp);
                    }

                }

                successNum = tmpList.size();
                if (successNum > 0) {

                    //??????relevancyNat?????????????????????relevancyNat
                    if (relevancyNat != null && relevancyNat.size() > 0) {
                        for (RecommendTaskEntity entity : tmpList) {
                            entity.setBatchId(checkEntity.getId());
                            // ??????????????????nat
                            if (entity.getTaskType() != null && BIG_INTERNET_RECOMMEND != entity.getTaskType()) {
                                // ???????????????????????????????????????nat??????????????????????????????,?????????????????????index??????
                                if (StringUtils.isBlank(entity.getRelevancyNat())
                                        || 0 == JSONObject.parseArray(entity.getRelevancyNat()).size()) {
                                    entity.setRelevancyNat(relevancyNat.toJSONString());
                                } else {
                                    JSONArray existRelevancyNat = JSONObject.parseArray(entity.getRelevancyNat());
                                    int existRelevancyNatSize = existRelevancyNat.size();
                                    int newIndex = 1;
                                    for (Object object : relevancyNat) {
                                        JSONObject itemJsonObject = (JSONObject)object;
                                        itemJsonObject.put("index", existRelevancyNatSize + newIndex);
                                        existRelevancyNat.add(itemJsonObject);
                                        newIndex++;
                                    }
                                    entity.setRelevancyNat(existRelevancyNat.toJSONString());
                                }
                            }

                        }
                    } else {
                        for (RecommendTaskEntity entity : tmpList) {
                            entity.setBatchId(checkEntity.getId());
                        }
                    }

                    policyRecommendTaskService.insertRecommendTaskList(tmpList);
                    recommendBussCommonService.updateRelevanceNatTaskId(tmpList);
                    Boolean aBoolean = recommendBussCommonService.autoStartRecommend(autoStartRecommend, tmpList, auth);
                    log.info("?????????????????????????????????????????????????????????{}",aBoolean);
                    for (RecommendTaskEntity entity : tmpList) {
                        sb.append(",");
                        sb.append(entity.getId());
                    }
                }

                if (sb.length() > 0) {
                    sb.deleteCharAt(0);
                }
                String taskIds = sb.toString();
                Integer batchId = checkEntity.getId();

                checkEntity.setId(batchId);
                checkEntity.setTaskId(taskIds);
                policyRecommendTaskService.updateBatchTask(checkEntity);
                jsonObject.put("batchId", batchId);
            }


            String message = String.format("?????????????????????????????????????????????%s???", successNum);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

            // errmsg = failureMsg.toString().replaceFirst("???","");
            errmsg = failureMsg.toString();
            if (AliStringUtils.isEmpty(errmsg)) {
                status = "0";
                errmsg = "????????????" + successNum + "??????";
                //?????????????????????
                recommendExcelAndDownloadService.recodeFileToHistory(file,file.getOriginalFilename(),userInfoDTO.getId(),new Date());
            }
            jsonObject.put("successNum", successNum);
            jsonObject.put("taskList", sb.toString());
        } catch (Exception e) {
            errmsg = "??????????????????????????????????????????????????????";
            log.error("???????????????????????????", e);

            String message = String.format("????????????????????????????????????");
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        }

        return returnJSON(status, jsonObject, errcode, errmsg);
    }


    @ApiOperation("????????????Excel????????????")
    @PostMapping("task/downloadTemplate")
    public JSONObject downloadHostTemplate() {
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", resourceHandler.replace("**", "") + importHostExcelFileName);
            status = "0";
        } catch (Exception e) {
            errmsg += e;
            log.error("downloadHostTemplate???" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg);
    }

    @ApiOperation(value = "????????????excel??????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "??????????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "????????????", required = false, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "")})
    @RequestMapping(value = "task/export", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload, String startTime, String endTime, Authentication authentication) throws Exception {

        List<RecommendTaskEntity> taskList = policyRecommendTaskService.getTaskListByTime(startTime, endTime, authentication);

        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "??????????????????";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("??????????????????????????????????????????", e1);
        }

        String destDirName = dirPath + "/policyAddExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "policyAdddoing.temp";

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
                    new PolicyAddThread(filePath, doingFile, taskList,authentication,policyRecommendTaskService,recommendRelevanceSceneService).start();
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
                new PolicyAddThread(filePath, doingFile, taskList,authentication,policyRecommendTaskService,recommendRelevanceSceneService).start();
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
            log.error("??????????????????Excel????????????:", e);
            resultRO.setMessage("??????????????????");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }

    @ApiOperation("?????????????????????")
    @GetMapping("download")
    public JSONObject download(@ApiParam(name = "ids", value = "??????id", required = true) @RequestParam String ids,
                               HttpServletResponse response) {
        File file = null;
        InputStream fin = null;
        ServletOutputStream out = null;
        String zipPath = policyRecommendTaskService.getRecommendZip(ids, dirPath);
        if (zipPath == null) {
            return getReturnJSON(ReturnCode.NO_COMMAND_TO_DOWNLOAD);
        }
        try {
            file = new File(zipPath);
            fin = new FileInputStream(file);

            String fileName = file.getName();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/msword");
            // ???????????????????????????????????????????????????
            response.setHeader("Content-Disposition", "attachment;filename="
                    .concat(String.valueOf(URLEncoder.encode(fileName, "UTF-8"))));
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
                if (file != null) {
                    file.delete();
                }
                ;
            } catch (IOException e) {
                logger.error("io?????????");
            }
        }

        //????????????????????????????????????????????????return????????????????????????
        return null;
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="id", value="??????id??????", required=true, dataType="String")
    })
    @PostMapping("startbatchpushtasks")
    public JSONObject startBatchCommandTask (@RequestParam String id) {
        StringBuilder errMsg = new StringBuilder();

        RecommendTaskCheckEntity recommendTaskCheckEntity = policyRecommendTaskService.selectBatchTaskById(Integer.valueOf(id));
        String ids = recommendTaskCheckEntity.getTaskId();
        if (StringUtils.isEmpty(ids)) {
            logger.error("???????????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        String[] arr = ids.split(",");
        List<Integer> idList = new ArrayList<>();
        for (String taskId : arr) {
            idList.add(Integer.valueOf(taskId));
        }

        List<String> themeList = new ArrayList<>();
        List<CommandTaskDTO> taskDTOList = new ArrayList<>();
        for(Integer taskId: idList) {
            logger.info(String.format("????????????(%d)", taskId));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(taskId);
            if(taskEntityList.size() == 0) {
                logger.error(String.format("????????????(%d)???????????????????????????????????????...", taskId));
                continue;
            }
            List<CommandTaskEditableEntity> validTaskEntityList = new ArrayList<>();
            for(CommandTaskEditableEntity taskEntity: taskEntityList) {
                boolean ignore = false;
                String command = taskEntity.getCommandline();
                if(org.apache.commons.lang3.StringUtils.isNotBlank(command) && command.startsWith("?????????????????????????????????")) {
                    errMsg.append(String.format("[%s]?????????????????????????????????????????????????????????", taskEntity.getTheme()));
                    ignore = true;
                }

                if (!ignore && PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START == taskEntity.getPushStatus()) {
                    validTaskEntityList.add(taskEntity);
                }
            }

            CommandTaskDTO taskDTO = new CommandTaskDTO();
            taskDTO.setList(validTaskEntityList);
            taskDTO.setRevert(false);

            RecommendTaskEntity taskEntity = recommendTaskService.getRecommendTaskByTaskId(taskId);
            taskDTO.setTaskId(taskEntity.getId());
            taskDTO.setTheme(taskEntity.getTheme());

            if (validTaskEntityList.size()>0) {
                taskDTOList.add(taskDTO);

                themeList.add(taskEntity.getTheme());
            }
        }

        if(taskDTOList.size() == 0) {
            logger.error("???????????????????????????????????????");
            errMsg.append(String.format("?????????????????????????????????????????????????????????"));
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }

        String message = String.format("?????????%s ?????????%s", org.apache.commons.lang3.StringUtils.join(themeList, ","), "??????");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        int returnCode = 0;
        try {
            returnCode = pushTaskService.preBatchPushTaskList(taskDTOList, true);
        } catch (Exception e) {
            logger.error("??????????????????,????????????:{}", e);
            return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(ReturnCode.PUSH_TASK_ERROR));
        }
        if (ReturnCode.PUSH_TIME_LOCKED == returnCode) {
            logger.info("??????????????????????????????");
            errMsg.append("??????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }

        logger.info(errMsg.toString());

        errMsg.insert(0, "???????????????");

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errMsg.toString());
    }

    @ApiOperation("??????????????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="id", value="??????id??????", required=true, dataType="String")
    })
    @PostMapping("getbatchdevicenum")
    public JSONObject getBatchDeviceNum(@RequestParam String id) {
        StringBuilder returnMsg = new StringBuilder();
        returnMsg.append("???????????????????????????");

        RecommendTaskCheckEntity recommendTaskCheckEntity = policyRecommendTaskService.selectBatchTaskById(Integer.valueOf(id));
        String ids = recommendTaskCheckEntity.getTaskId();
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if(jsonArray == null || jsonArray.size() == 0){
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }
        List<Integer> idList = null;
        try {
            idList = com.abtnetworks.totems.common.utils.StringUtils.parseIntArrayList(ids);
        } catch(Exception e) {
            logger.error("???????????????????????????", e);
        }

        List<CommandTaskEditableEntity> taskList = new ArrayList<>();
        for(Integer taskId: idList) {
            logger.info(String.format("????????????(%d)", taskId));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(taskId);
            taskEntityList.stream().filter(task -> PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START == task.getPushStatus() || PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR == task.getPushStatus()).collect(Collectors.toList());
            taskList.addAll(taskEntityList);
        }

        if(taskList.size() == 0) {
            logger.error("???????????????????????????????????????");
            return getReturnJSON(ReturnCode.FAILED, returnMsg.toString());
        }

        List<String> deviceUuidList = taskList.stream().map(task -> task.getDeviceUuid()).distinct().collect(Collectors.toList());
        returnMsg.append(deviceUuidList.size());

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, returnMsg.toString());
    }
}
