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
 * @Description: 这里只做一些仿真中的导入和导出功能的控制层
 * @date 2021/1/7
 */
@Slf4j
@Api(value = "策略开通扩展抽离控制层")
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

    @ApiOperation("批量导入任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "file", value = "上传的Excel文件", required = false, dataType = "String")
    })
    @PostMapping("task/import")
    public JSONObject importPolicyRecommendTaskList(MultipartFile file, String filename, String creator,Boolean autoStartRecommend, Authentication auth) {
        log.info("批量添加任务");
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
            //导入工单在第一页 包含明细开通和大网段开通
            ImportExcel ei = new ImportExcel(file, 0, 0);

            // 互联网开通第二页
            ImportExcel internetExcel = new ImportExcel(file, 0, 1);
            // 大网段开通开通第三页
            ImportExcel bigInternetExcel = new ImportExcel(file, 0, 2);
            // 模拟nat开通工单在第四页
            ImportExcel natExcel = new ImportExcel(file, 0, 3);
            // 静态路由开通工单在第四页
            ImportExcel routeExcel = new ImportExcel(file, 0, 4);
            // 飞塔NAT关联
            ImportExcel specialNatExcel = new ImportExcel(file, 0, 5);



            List<RecommendTaskEntity> tmpList = new ArrayList<>();
            List<RecommendTaskEntity> natTaskList = new ArrayList<>();
            List<PushRecommendStaticRoutingDTO> routeTaskList = new ArrayList<>();
            List<RecommendRelevanceSceneDTO> specialNatList = new ArrayList<>();

            //业务开通
            List<ExcelRecommendTaskDTO> list = ei.getDataList(ExcelRecommendTaskDTO.class);
            //nat策略
            List<ExcelTaskNatEntity> natExcelList = natExcel.getDataList(ExcelTaskNatEntity.class);
            //互联网开通内外，外内
            List<ExcelRecommendInternetTaskDTO> internetExcelDataList = internetExcel.getDataList(ExcelRecommendInternetTaskDTO.class);
            //大网段开通
            List<ExcelBigInternetTaskDTO> excelBigInternetTaskDTOS = bigInternetExcel.getDataList(ExcelBigInternetTaskDTO.class);
            //静态路由策略
            List<ExcelTaskStaticRouteEntity> routeExcelList = routeExcel.getDataList(ExcelTaskStaticRouteEntity.class);
            // 特殊Nat(场景关联不入库)
            List<ExcelTaskSpecialNatEntity> specialNatExcelList = specialNatExcel.getDataList(ExcelTaskSpecialNatEntity.class);

            String msg = recommendExcelAndDownloadService.checkExcelNatTaskValidation(natExcelList, filename);
            if (!AliStringUtils.isEmpty(msg)) {
                errmsg = msg;
                return returnJSON(status, jsonObject, errcode, errmsg);
            }

            // 校验静态路由
            String routeErrorMsg = recommendExcelAndDownloadService.checkExcelRouteTaskValidation(routeExcelList);
            if (!AliStringUtils.isEmpty(routeErrorMsg)) {
                errmsg = routeErrorMsg;
                return returnJSON(status, jsonObject, errcode, errmsg);
            }
            routeTaskList = recommendBussCommonService.getRouteExcelDTO(routeExcelList,creator,userInfoDTO);

            // 校验特殊nat场景
            String specialNatErrorMsg = recommendExcelAndDownloadService.checkExcelSpecialNatValidation(specialNatExcelList);
            if (!AliStringUtils.isEmpty(specialNatErrorMsg)) {
                errmsg = specialNatErrorMsg;
                return returnJSON(status, jsonObject, errcode, errmsg);
            }
            specialNatList = recommendBussCommonService.getSpecialNatExcelDTO(specialNatExcelList,creator,userInfoDTO);

            String whatIfCaseName = String.format("A%s", String.valueOf(System.currentTimeMillis()));
            //模拟NAT开通有数据，既需要进行模拟变更场景设置
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
                                log.error(String.format("跳过空数据第%d行！", rowNum));
                                continue;
                            }
                            int rc = verifyBusinessService.verifyRecommendBussExcel(entity).getResultCode();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("业务开通模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (indexSet.contains(entity.getId())) {
                                failureMsg.append("业务开通模版页第" + rowNum + "行序号重复。<br>");
                                log.error("业务开通模版页第" + rowNum + "行序号重复。");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            } else {
                                indexSet.add(entity.getId());
                            }

                            // 流水号码需要自动生成
                            String orderNO = "A" + simpleDateFormat.format(new Date()) + index;
                            entity.setOrderNO(orderNO);

                            //主题为空，则自动生成一个
                            if (AliStringUtils.isEmpty(entity.getName())) {
                                entity.setName(orderNO);
                            } else {
                                //将序号和名称拼接起来
                                if (!AliStringUtils.isEmpty(entity.getId())) {
                                    String name = String.format("%s_%s", entity.getName(), entity.getId());
                                    //第一条工单的主题作为批量管理的工单号
                                    if (AliStringUtils.isEmpty(orderNumber)) {
                                        orderNumber = entity.getName();
                                    }
                                    entity.setName(name);
                                }
                            }
                            //若导入工单申请人为空，则设置申请人为当前登录用户
                            if (AliStringUtils.isEmpty(entity.getUser())) {
                                entity.setUser(userInfoDTO.getId());
                            }

                            //去掉没用的空格
                            entity.setSrcIpDescription(entity.getSrcIpDescription().trim());
                            entity.setDstIpDescription(entity.getDstIpDescription().trim());
                            entity.setName(entity.getName().trim());
                            if (!InputValueUtils.validOrderName(entity.getName())) {
                                failureMsg.append("业务开通模版页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
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

                            //去掉业务开通中的对象名称空格
                            tmpEntity.setSrcIpSystem(StringUtils.isBlank(tmpEntity.getSrcIpSystem()) ? null
                                    : tmpEntity.getSrcIpSystem().replace(" ", ""));
                            tmpEntity.setDstIpSystem(StringUtils.isBlank(tmpEntity.getDstIpSystem()) ? null
                                    : tmpEntity.getDstIpSystem().replace(" ", ""));
                            // 查询是否有关联nat
                            int returnCode = recommendBussCommonService.checkPostRelevancyNat(tmpEntity, auth);
                            // 如果填写了源转或者目的转 没有匹配到映射关系数据 则抛异常，外层捕获记录错误条数
                            if (ReturnCode.POLICY_MSG_OK != returnCode) {
                                failureMsg.append("业务开通模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(returnCode) + "<br>");
                                log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(returnCode));
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }
                            tmpList.add(tmpEntity);
                        } else {
                            log.error(String.format("跳过空数据第%d行！", rowNum));
                            continue;
                        }
                    } catch (Exception ex) {
                        log.error("批量开通导入异常,异常原因:{}", ex);
                        failureMsg.append(" 策略仿真任务: " + list.get(i).getOrderNO() + " 导入失败：");
                        failureNum++;
                    }
                    index++;
                }

            } else {
                log.info("业务开通导入文件内容为空！");
            }

            if (hasInvalid && StringUtils.isNotBlank(failureMsg)) {
                failureMsg.insert(0, " 失败 " + failureNum + "条任务信息 导入信息如下：");
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
            //互联网开通
            ExcelImportResultVO excelImportResultInternetVO = recommendExcelAndDownloadService.savePolicyDataForExcel(dataRecommendForExcelDTO, simpleDateFormat,auth);
            //大网段开通
            dataRecommendForExcelDTO.setExcelBigInternetTaskDTOS(excelBigInternetTaskDTOS);
            dataRecommendForExcelDTO.setTaskType(BIG_INTERNET_RECOMMEND);
            ExcelImportResultVO excelImportResultBigNetVO = recommendExcelAndDownloadService.saveBigInternetPolicyDataForExcel(dataRecommendForExcelDTO, simpleDateFormat);

            failureMsg = dataRecommendForExcelDTO.getFailureMsg();
            if (CollectionUtils.isEmpty(list) && CollectionUtils.isEmpty(internetExcelDataList) && CollectionUtils.isEmpty(excelBigInternetTaskDTOS)) {
                failureMsg.append("导入文件内容为空！");
            }
            if (dataRecommendForExcelDTO.getFailureNum() > 0) {
                failureMsg.insert(0, " 失败 " + dataRecommendForExcelDTO.getFailureNum() + "条任务信息 导入信息如下：");
            }
            tmpList = dataRecommendForExcelDTO.getTmpList();
            StringBuilder sb = new StringBuilder();
            if (!dataRecommendForExcelDTO.getHasInvalid() && tmpList.size() > 0) {

                RecommendTaskCheckEntity checkEntity = new RecommendTaskCheckEntity();
                checkEntity.setBatchType(1);
                Date date = new Date();

                checkEntity.setCreateTime(date);
                //设置第一条任务的主题为批量管理工单主题
                if (StringUtils.isBlank(orderNumber)) {
                    if (StringUtils.isBlank(excelImportResultInternetVO.getOrderNumber())) {
                        if (StringUtils.isBlank(excelImportResultBigNetVO.getOrderNumber())) {
                            log.error("不可能同时多个工单都为空工单");
                        } else {
                            checkEntity.setOrderNumber(excelImportResultBigNetVO.getOrderNumber());
                        }
                    } else {
                        checkEntity.setOrderNumber(excelImportResultInternetVO.getOrderNumber());
                    }
                } else {
                    if (StringUtils.isNotBlank(filename)) {
                        //api导入时需要注意用文件名
                        checkEntity.setOrderNumber(filename);
                    } else {
                        checkEntity.setOrderNumber(orderNumber);
                    }
                }

                checkEntity.setUserName(userInfoDTO.getId());
                checkEntity.setStatus(0);
                policyRecommendTaskService.addBatchTask(checkEntity);


                JSONArray relevancyNat = new JSONArray();
                // 处理nat数据
                if (natTaskList.size() > 0) {

                    for (RecommendTaskEntity entity : natTaskList) {
                        entity.setBatchId(checkEntity.getId());
                    }

                    policyRecommendTaskService.insertRecommendTaskList(natTaskList);

                    for (int i = 0; i < natTaskList.size(); i++) {
                        sb.append(",");
                        sb.append(natTaskList.get(i).getId());

                        NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(natTaskList.get(i).getDeviceIp());
                        // 拼接relevancyNat
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

                // 处理飞塔NAT特殊场景
                if(specialNatList.size() > 0){
                    for (int i = 0; i < specialNatList.size(); i++) {
                        recommendRelevanceSceneService.createRecommendRelevanceScene(specialNatList.get(i));
                        // 拼接relevancyNat
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

                // 处理静态路由数据
                if (routeTaskList.size() > 0) {
                    for (PushRecommendStaticRoutingDTO dto : routeTaskList) {
                        dto.setBatchId(checkEntity.getId());
                    }

                    pushTaskStaticRoutingService.batchCreateStaticRoute(routeTaskList);

                    for (int i = 0; i < routeTaskList.size(); i++) {
                        sb.append(",");
                        sb.append(routeTaskList.get(i).getTaskId());

                        NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(routeTaskList.get(i).getDeviceIp());
                        // 拼接relevancyNat
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

                    //如果relevancyNat不等于空，添加relevancyNat
                    if (relevancyNat != null && relevancyNat.size() > 0) {
                        for (RecommendTaskEntity entity : tmpList) {
                            entity.setBatchId(checkEntity.getId());
                            // 大网段不关联nat
                            if (entity.getTaskType() != null && BIG_INTERNET_RECOMMEND != entity.getTaskType()) {
                                // 判断之前流程有没有自动创建nat。如果没有则直接添加,如果有自动更新index序号
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
                    log.info("此次导入的工单是否参与自动完成仿真功能{}",aBoolean);
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


            String message = String.format("批量导入策略开通数据成功，共计%s条", successNum);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

            // errmsg = failureMsg.toString().replaceFirst("，","");
            errmsg = failureMsg.toString();
            if (AliStringUtils.isEmpty(errmsg)) {
                status = "0";
                errmsg = "导入成功" + successNum + "条！";
                //保存至历史列表
                recommendExcelAndDownloadService.recodeFileToHistory(file,file.getOriginalFilename(),userInfoDTO.getId(),new Date());
            }
            jsonObject.put("successNum", successNum);
            jsonObject.put("taskList", sb.toString());
        } catch (Exception e) {
            errmsg = "导入失败，请确保文件格式和内容正确！";
            log.error("导入任务列表失败：", e);

            String message = String.format("批量导入策略开通数据失败");
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        }

        return returnJSON(status, jsonObject, errcode, errmsg);
    }


    @ApiOperation("下载导入Excel任务模板")
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
            log.error("downloadHostTemplate：" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg);
    }

    @ApiOperation(value = "策略开通excel导出")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "是否重新下载", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "")})
    @RequestMapping(value = "task/export", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload, String startTime, String endTime, Authentication authentication) throws Exception {

        List<RecommendTaskEntity> taskList = policyRecommendTaskService.getTaskListByTime(startTime, endTime, authentication);

        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "策略开通导出";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("生成策略开通报表文件名称异常", e1);
        }

        String destDirName = dirPath + "/policyAddExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "policyAdddoing.temp";

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
                    new PolicyAddThread(filePath, doingFile, taskList,authentication,policyRecommendTaskService,recommendRelevanceSceneService).start();
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
                new PolicyAddThread(filePath, doingFile, taskList,authentication,policyRecommendTaskService,recommendRelevanceSceneService).start();
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
            log.error("下载策略概览Excel表格失败:", e);
            resultRO.setMessage("数据导出失败");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }

    @ApiOperation("获取策略命令行")
    @GetMapping("download")
    public JSONObject download(@ApiParam(name = "ids", value = "任务id", required = true) @RequestParam String ids,
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
            // 设置浏览器以下载的方式处理该文件名
            response.setHeader("Content-Disposition", "attachment;filename="
                    .concat(String.valueOf(URLEncoder.encode(fileName, "UTF-8"))));
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
                if (file != null) {
                    file.delete();
                }
                ;
            } catch (IOException e) {
                logger.error("io流异常");
            }
        }

        //上面流已经返回了，后面不能有新的return值，否则会抛异常
        return null;
    }

    @ApiOperation("导入批量下发任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="id", value="任务id列表", required=true, dataType="String")
    })
    @PostMapping("startbatchpushtasks")
    public JSONObject startBatchCommandTask (@RequestParam String id) {
        StringBuilder errMsg = new StringBuilder();

        RecommendTaskCheckEntity recommendTaskCheckEntity = policyRecommendTaskService.selectBatchTaskById(Integer.valueOf(id));
        String ids = recommendTaskCheckEntity.getTaskId();
        if (StringUtils.isEmpty(ids)) {
            logger.error("批量开始策略仿真任务为空！");
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
            logger.info(String.format("获取任务(%d)", taskId));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(taskId);
            if(taskEntityList.size() == 0) {
                logger.error(String.format("获取任务(%d)失败，任务下没有命令行数据...", taskId));
                continue;
            }
            List<CommandTaskEditableEntity> validTaskEntityList = new ArrayList<>();
            for(CommandTaskEditableEntity taskEntity: taskEntityList) {
                boolean ignore = false;
                String command = taskEntity.getCommandline();
                if(org.apache.commons.lang3.StringUtils.isNotBlank(command) && command.startsWith("无法生成该设备的命令行")) {
                    errMsg.append(String.format("[%s]开始下发失败，存在未生成命令行的设备！", taskEntity.getTheme()));
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
            logger.error("没有可开始的策略下发任务！");
            errMsg.append(String.format("当前批量任务没有可开始的策略下发任务！"));
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }

        String message = String.format("工单：%s ，开始%s", org.apache.commons.lang3.StringUtils.join(themeList, ","), "下发");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        int returnCode = 0;
        try {
            returnCode = pushTaskService.preBatchPushTaskList(taskDTOList, true);
        } catch (Exception e) {
            logger.error("批量下发报错,报错原因:{}", e);
            return getReturnJSON(ReturnCode.FAILED, ReturnCode.getMsg(ReturnCode.PUSH_TASK_ERROR));
        }
        if (ReturnCode.PUSH_TIME_LOCKED == returnCode) {
            logger.info("当前时间不允许下发！");
            errMsg.append("当前时间不允许下发！");
            return getReturnJSON(ReturnCode.FAILED, errMsg.toString());
        }

        logger.info(errMsg.toString());

        errMsg.insert(0, "开始下发！");

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errMsg.toString());
    }

    @ApiOperation("导入批量下发提示涉及多少设备")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="id", value="任务id列表", required=true, dataType="String")
    })
    @PostMapping("getbatchdevicenum")
    public JSONObject getBatchDeviceNum(@RequestParam String id) {
        StringBuilder returnMsg = new StringBuilder();
        returnMsg.append("本次下发设备数量：");

        RecommendTaskCheckEntity recommendTaskCheckEntity = policyRecommendTaskService.selectBatchTaskById(Integer.valueOf(id));
        String ids = recommendTaskCheckEntity.getTaskId();
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if(jsonArray == null || jsonArray.size() == 0){
            logger.error("开始策略仿真任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }
        List<Integer> idList = null;
        try {
            idList = com.abtnetworks.totems.common.utils.StringUtils.parseIntArrayList(ids);
        } catch(Exception e) {
            logger.error("解析任务列表出错！", e);
        }

        List<CommandTaskEditableEntity> taskList = new ArrayList<>();
        for(Integer taskId: idList) {
            logger.info(String.format("获取任务(%d)", taskId));
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(taskId);
            taskEntityList.stream().filter(task -> PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START == task.getPushStatus() || PushConstants.PUSH_INT_PUSH_RESULT_STATUS_ERROR == task.getPushStatus()).collect(Collectors.toList());
            taskList.addAll(taskEntityList);
        }

        if(taskList.size() == 0) {
            logger.error("没有可开始的策略下发任务！");
            return getReturnJSON(ReturnCode.FAILED, returnMsg.toString());
        }

        List<String> deviceUuidList = taskList.stream().map(task -> task.getDeviceUuid()).distinct().collect(Collectors.toList());
        returnMsg.append(deviceUuidList.size());

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, returnMsg.toString());
    }
}
