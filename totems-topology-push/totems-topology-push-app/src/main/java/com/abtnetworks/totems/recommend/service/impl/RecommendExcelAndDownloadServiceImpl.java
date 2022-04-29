package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.RecommendTypeEnum;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.FileUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.recommend.dao.mysql.PushRecommendTaskHistoryMapper;
import com.abtnetworks.totems.recommend.dto.excel.DataRecommendForExcelDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelBigInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.verify.VerifyBussExcelDTO;
import com.abtnetworks.totems.recommend.entity.PushRecommendTaskHistoryEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.RecommendBussCommonService;
import com.abtnetworks.totems.recommend.service.RecommendExcelAndDownloadService;
import com.abtnetworks.totems.recommend.service.VerifyBusinessService;
import com.abtnetworks.totems.recommend.vo.excel.ExcelImportResultVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.abtnetworks.totems.common.constants.CommonConstants.HOUR_SECOND;
import static com.abtnetworks.totems.common.constants.PolicyConstants.BIG_INTERNET_RECOMMEND;
import static com.abtnetworks.totems.common.constants.PolicyConstants.LABEL_MODEL_OR;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;

/**
 * @author Administrator
 * @Title:
 * @Description: 这里分开写之后，还没完全解耦，下次有遇到这样业务改动，可继续修改
 * @date 2021/1/14
 */
@Slf4j
@Service
public class RecommendExcelAndDownloadServiceImpl implements RecommendExcelAndDownloadService {


    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    VerifyBusinessService verifyBusinessService;

    @Resource
    RemoteBranchService remoteBranchService;

    @Resource
    RecommendBussCommonService recommendBussCommonService;

    @Autowired
    PushRecommendTaskHistoryMapper pushRecommendTaskHistoryMapper;

    @Value("${push.download-file}")
    private String downloadFileDir;

    private static final String RECOMMEND_HISTORY = "recommendHistory";

    @Value("${resourceHandler}")
    private String resourceHandler;
    /**
     * 检测Nat模板页工单列表合法性
     *
     * @param natExcelList nat模板页工单列表
     * @return 错误提示，若为空字符串（“”）,则无错误
     */
    @Override
    public String checkExcelNatTaskValidation(List<ExcelTaskNatEntity> natExcelList,String filename) {
        StringBuilder sb = new StringBuilder();
        int rowNum = 2;
        List<ExcelTaskNatEntity> emptyList = new ArrayList<>();
        Set<String> indexSet = new HashSet<>();
        for (ExcelTaskNatEntity entity : natExcelList) {
            if (entity.isEmpty()) {
                entity.setSrcIpSystem(TotemsStringUtils.trim2(entity.getSrcIpSystem()));
                entity.setDstIpSystem(TotemsStringUtils.trim2(entity.getDstIpSystem()));
                entity.setPostSrcIpSystem(TotemsStringUtils.trim2(entity.getPostSrcIpSystem()));
                entity.setPostDstIpSystem(TotemsStringUtils.trim2(entity.getPostDstIpSystem()));
                emptyList.add(entity);
                continue;
            }
            if(StringUtils.isNotBlank(filename)){
                //Api提交的工单号由url中filename生成
                entity.setTheme(filename);
            }
            int rc = entity.validation();
            if (rc != ReturnCode.POLICY_MSG_OK) {
                sb.append(String.format("Nat模板页第%d行错误！%s\n", rowNum, ReturnCode.getMsg(rc)));
            }

            if (indexSet.contains(entity.getId())) {
                sb.append(String.format("Nat模板页第%d行错误！序号重复。\n", rowNum));
            } else {
                indexSet.add(entity.getId());
            }

            if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) && AliStringUtils.isEmpty(entity.getPostDstAddress())) {
                sb.append(String.format("Nat模板页第%d行错误！转换后源/目的地址不能都为空", rowNum));
            }
            String deviceIp = entity.getDeviceIp();
            NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("Nat模板页第%d行错误！设备%s不存在！\n", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            if (!InputValueUtils.validOrderName(entity.getTheme())) {
                sb.append("Nat模板页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
                continue;
            }

            if (entity.getNatType().equalsIgnoreCase("STATIC")) {
                if (nodeEntity.getModelNumber().equals("Cisco ASA")) {
                    if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) || AliStringUtils.isEmpty(entity.getPreSrcAddress())) {
                        sb.append(String.format("Nat模板页第%d行错误！Cisco设备静态Nat转换前/转换后源地址不能为空！", rowNum));
                    }
                } else {
                    if (AliStringUtils.isEmpty(entity.getPostDstAddress())) {
                        sb.append(String.format("Nat模板页第%d行错误！静态Nat转换后源的地址不能为空！", rowNum));
                    }
                }
            }
            rowNum++;
        }

        natExcelList.removeAll(emptyList);
        return sb.toString();
    }


    /**
     * 检测特殊nat场景列表合法性
     *
     * @param specialNatExcelList nat模板页工单列表
     * @return 错误提示，若为空字符串（“”）,则无错误
     */
    @Override
    public String checkExcelSpecialNatValidation(List<ExcelTaskSpecialNatEntity> specialNatExcelList) {
        StringBuilder sb = new StringBuilder();
        int rowNum = 2;
        List<ExcelTaskSpecialNatEntity> emptyList = new ArrayList<>();
        Set<String> indexSet = new HashSet<>();
        for (ExcelTaskSpecialNatEntity entity : specialNatExcelList) {
            if (entity.isEmpty()) {
                emptyList.add(entity);
                continue;
            }
            int rc = entity.validation();
            if (rc != ReturnCode.POLICY_MSG_OK) {
                sb.append(String.format("飞塔Nat模板页第%d行错误！%s\n", rowNum, ReturnCode.getMsg(rc)));
            }

            if (indexSet.contains(entity.getId())) {
                sb.append(String.format("飞塔Nat模板页第%d行错误！序号重复。\n", rowNum));
            } else {
                indexSet.add(entity.getId());
            }

            if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) && AliStringUtils.isEmpty(entity.getPostDstAddress())) {
                sb.append(String.format("飞塔Nat模板页第%d行错误！转换后源/目的地址不能都为空", rowNum));
            }
            String deviceIp = entity.getDeviceIp();
            NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("飞塔Nat模板页第%d行错误！设备%s不存在！\n", rowNum, deviceIp));
                rowNum++;
                continue;
            }
            // 判断是不是飞塔设备
            DeviceModelNumberEnum deviceModelNumberEnum =  DeviceModelNumberEnum.fromString(nodeEntity.getModelNumber());
            boolean isFort = DeviceModelNumberEnum.isRangeFortCode(deviceModelNumberEnum.getCode());
            if (!isFort) {
                sb.append(String.format("飞塔Nat模板页第%d行错误！设备%s不是飞塔设备！\n", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            if (!InputValueUtils.validOrderName(entity.getName())) {
                sb.append("飞塔Nat模板页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
                continue;
            }

            rowNum++;
        }

        specialNatExcelList.removeAll(emptyList);
        return sb.toString();
    }






    /**
     * 检测静态路由sheet页工单列表合法性
     *
     * @param routeExcelList 静态路由工单列表
     * @return 错误提示，若为空字符串（“”）,则无错误
     */
    @Override
    public String checkExcelRouteTaskValidation(List<ExcelTaskStaticRouteEntity> routeExcelList) {
        StringBuilder sb = new StringBuilder();
        int rowNum = 2;
        List<ExcelTaskStaticRouteEntity> emptyList = new ArrayList<>();
        Set<String> indexSet = new HashSet<>();
        for (ExcelTaskStaticRouteEntity entity : routeExcelList) {
            if (entity.isEmpty()) {
                emptyList.add(entity);
                continue;
            }

            int rc = entity.validation();
            if (rc != ReturnCode.POLICY_MSG_OK) {
                sb.append(String.format("静态路由模板页第%d行错误！%s<br>", rowNum, ReturnCode.getMsg(rc)));
            }

            if (indexSet.contains(entity.getId())) {
                sb.append(String.format("静态路由模板第%d行错误！序号重复。<br>", rowNum));
            } else {
                indexSet.add(entity.getId());
            }
            String deviceIp = entity.getDeviceIp();
            NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("静态路由模板第%d行错误！设备%s不存在！<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            if (!InputValueUtils.validOrderName(entity.getTheme())) {
                sb.append("静态路由模板第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
                continue;
            }

            entity.setOutInterface(dealSpace(entity.getOutInterface()));
            entity.setNextHop(dealSpace(entity.getNextHop()));
            entity.setSrcVirtualRouter(dealSpace(entity.getSrcVirtualRouter()));
            entity.setDstVirtualRouter(dealSpace(entity.getDstVirtualRouter()));
            rowNum++;
        }

        routeExcelList.removeAll(emptyList);

        return sb.toString();
    }

    /***
     * 策略开通下载
     */
    @Override
    public void downLoadPolicyAdd(HttpServletResponse response, String fileExcitPath) {
        File src = new File(fileExcitPath);
        FileUtils.downloadOverView(src, response);
    }

    @Override
    public ExcelImportResultVO savePolicyDataForExcel(DataRecommendForExcelDTO dataRecommendForExcelDTO, SimpleDateFormat simpleDateFormat,Authentication auth) {
        ExcelImportResultVO excelImportResultVO = new ExcelImportResultVO();
        List<ExcelRecommendInternetTaskDTO> excelRecommendInternetTaskDTOS = dataRecommendForExcelDTO.getInternetExcelDataList();
        StringBuilder failureMsg = dataRecommendForExcelDTO.getFailureMsg();
        List<RecommendTaskEntity> tmpList = dataRecommendForExcelDTO.getTmpList();
        boolean hasInvalid = false;
        int index = 0;
        Set<String> indexSet = new HashSet<>();
        String filename = dataRecommendForExcelDTO.getFilename();
        String creator = dataRecommendForExcelDTO.getCreator();
        int failureNum = dataRecommendForExcelDTO.getFailureNum();
        String orderNumber = "";
        if (CollectionUtils.isNotEmpty(excelRecommendInternetTaskDTOS)) {

            for (int i = 0; i < excelRecommendInternetTaskDTOS.size(); i++) {
                log.info("Excel value:" + excelRecommendInternetTaskDTOS.get(i).toString());
                ExcelRecommendInternetTaskDTO entity = excelRecommendInternetTaskDTOS.get(i);
                String taskType = entity.getTaskType();
                // 流水号码需要自动生成
                String orderNO = "A" + simpleDateFormat.format(new Date()) + index;
                entity.setOrderNO(orderNO);
                String themName = generateOrderNum(entity.getName(), entity.getId(), orderNO);
                //第一条工单的主题作为批量管理的工单号
                if (AliStringUtils.isEmpty(orderNumber)) {
                    orderNumber = themName;
                }
                entity.setName(themName);
                entity.setSrcIpDescription(entity.getSrcIpDescription().trim());
                entity.setDstIpDescription(entity.getDstIpDescription().trim());
                try {
                    int rowNum = i + 2;
                    boolean emptyParam = excelRecommendInternetTaskDTOS.get(i) != null
                            && (!AliStringUtils.isEmpty(excelRecommendInternetTaskDTOS.get(i).getSrcIp())
                            || !AliStringUtils.isEmpty(excelRecommendInternetTaskDTOS.get(i).getDstIp()));
                    if (emptyParam) {
                        if (entity.isEmpty()) {
                            log.error(String.format("%s模版工单[%s]用户[%s]跳过空数据第%d行！", taskType, filename, creator, rowNum));
                            continue;
                        }
                        VerifyBussExcelDTO verifyBussExcelDTO = verifyBusinessService.verifyRecommendInternetExcel(entity);
                        if (ReturnCode.POLICY_MSG_OK != verifyBussExcelDTO.getResultCode()) {
                            failureMsg.append("互联网开通模板页第" + rowNum + "行错误！"
                                    + ReturnCode.getMsg(verifyBussExcelDTO.getResultCode()) + "<br>");
                            log.error("数据\n" + entity.toString() + "\n错误！"
                                    + ReturnCode.getMsg(verifyBussExcelDTO.getResultCode()));
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        }
                        excelImportResultVO.setResultCode(verifyBussExcelDTO.getResultCode());
                        //则设置申请人为当前登录用户
                        if (AliStringUtils.isEmpty(entity.getUser())) {
                            entity.setUser(dataRecommendForExcelDTO.getUserInfoDTO().getId());
                        }
                        if (indexSet.contains(entity.getId())) {
                            failureMsg.append(String.format("%s模版页第" + rowNum + "行序号重复。<br>", taskType));
                            log.error(String.format("%s模版页第" + rowNum + "行序号重复。", taskType));
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        } else {
                            indexSet.add(entity.getId());
                        }
                        if (!InputValueUtils.validOrderName(entity.getName().trim())) {
                            failureMsg.append(String.format("%s模版页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>", taskType));
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        }
                        //复制转化的公共部分
                        RecommendTypeEnum recommendTypeEnum = RecommendTypeEnum.getRecommendTypeByDesc(entity.getTaskType());
                        RecommendTaskEntity tmpEntity = toCommonTaskEntity(null, entity, recommendTypeEnum.getTypeCode(), dataRecommendForExcelDTO);
                        // 查询是否有关联nat
                        int returnCode = recommendBussCommonService.checkPostRelevancyNat(tmpEntity, auth);
                        // 如果填写了源转或者目的转 没有匹配到映射关系数据 则记录粗错误信息
                        if (ReturnCode.POLICY_MSG_OK != returnCode) {
                            failureMsg.append(
                                    String.format("%s模版页第%d行错误!%s<br>", taskType, rowNum, ReturnCode.getMsg(returnCode)));
                            log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(returnCode));
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        }
                        tmpList.add(tmpEntity);
                    } else {
                        log.info(String.format("%s模板", StringUtils.isNotBlank(taskType) ? taskType : "互联网开通模板"));
                    }
                } catch (Exception ex) {
                    log.error("批量开通导入异常", ex);
                    failureMsg.append(" 策略仿真任务: " + orderNO + " 导入失败：");
                    failureNum++;
                }
                index++;
            }
        }
        dataRecommendForExcelDTO.setHasInvalid(hasInvalid);
        dataRecommendForExcelDTO.setFailureMsg(failureMsg);
        dataRecommendForExcelDTO.setTmpList(tmpList);
        dataRecommendForExcelDTO.setFailureNum(failureNum);
        excelImportResultVO.setOrderNumber(orderNumber);
        return excelImportResultVO;
    }


    @Override
    public ExcelImportResultVO saveBigInternetPolicyDataForExcel(DataRecommendForExcelDTO dataRecommendForExcelDTO, SimpleDateFormat simpleDateFormat) {
        ExcelImportResultVO excelImportResultVO = new ExcelImportResultVO();
        StringBuilder failureMsg = dataRecommendForExcelDTO.getFailureMsg();
        List<ExcelBigInternetTaskDTO> excelRecommendInternetTaskDTOS = dataRecommendForExcelDTO.getExcelBigInternetTaskDTOS();
        String orderNumber ="";
        List<RecommendTaskEntity> tmpList = dataRecommendForExcelDTO.getTmpList();
        boolean hasInvalid = dataRecommendForExcelDTO.getHasInvalid();
        int index = 0;
        Set<String> indexSet = new HashSet<>();
        String filename = dataRecommendForExcelDTO.getFilename();
        String creator = dataRecommendForExcelDTO.getCreator();
        int failureNum = dataRecommendForExcelDTO.getFailureNum();
        if (CollectionUtils.isNotEmpty(excelRecommendInternetTaskDTOS)) {
            for (int i = 0; i < excelRecommendInternetTaskDTOS.size(); i++) {
                log.info("Excel value:" + excelRecommendInternetTaskDTOS.get(i).toString());
                ExcelBigInternetTaskDTO entity = excelRecommendInternetTaskDTOS.get(i);
                // 流水号码需要自动生成
                String orderNO = "A" + simpleDateFormat.format(new Date()) + index;
                entity.setOrderNO(orderNO);
                String themName = generateOrderNum(entity.getName(), entity.getId(), orderNO);

                entity.setName(themName);
                entity.setSrcIpDescription(entity.getSrcIpDescription().trim());
                entity.setDstIpDescription(entity.getDstIpDescription().trim());
                //第一条工单的主题作为批量管理的工单号
                if (StringUtils.isBlank(orderNumber)) {
                    orderNumber = themName;
                }
                try {
                    int rowNum = i + 2;
                    boolean emptyParam = excelRecommendInternetTaskDTOS.get(i) != null
                            && !AliStringUtils.isEmpty(excelRecommendInternetTaskDTOS.get(i).getSrcIp())
                            && !AliStringUtils.isEmpty(excelRecommendInternetTaskDTOS.get(i).getDstIp());
                    if (emptyParam) {
                        boolean checkItem = AliStringUtils.isEmpty(entity.getId()) && AliStringUtils.isEmpty(entity.getSrcIp()) && AliStringUtils.isEmpty(entity.getDstIp()) && AliStringUtils.isEmpty(entity.getService());
                        if (checkItem) {
                            log.error(String.format("大网段开通模版工单[%s]用户[%s]跳过空数据第%d行！", filename, creator, rowNum));
                            continue;
                        }
                        VerifyBussExcelDTO verifyBussExcelDTO = verifyBusinessService.verifyRecommendBigInternetExcel(entity);
                        if (ReturnCode.POLICY_MSG_OK != verifyBussExcelDTO.getResultCode()) {
                            failureMsg.append("大网段开通模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(verifyBussExcelDTO.getResultCode()) + "<br>");
                            log.error("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(verifyBussExcelDTO.getResultCode()));
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        }
                        excelImportResultVO.setResultCode(verifyBussExcelDTO.getResultCode());
                        if (indexSet.contains(entity.getId())) {
                            failureMsg.append("大网段开通模版页第" + rowNum + "行序号重复。<br>");
                            log.error("大网段开通模版页第" + rowNum + "行序号重复。");
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        } else {
                            indexSet.add(entity.getId());
                        }
                        if (!InputValueUtils.validOrderName(entity.getName().trim())) {
                            failureMsg.append("大网段开通模版页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
                            hasInvalid = true;
                            failureNum++;
                            continue;
                        }
                        //则设置申请人为当前登录用户
                        if (AliStringUtils.isEmpty(entity.getUser())) {
                            entity.setUser(dataRecommendForExcelDTO.getUserInfoDTO().getId());
                        }
                        //去掉没用的空格
                        entity.setName(entity.getName().trim());
                        RecommendTaskEntity tmpEntity = toCommonTaskEntity(entity, null, BIG_INTERNET_RECOMMEND, dataRecommendForExcelDTO);
                        tmpList.add(tmpEntity);
                    } else {
                        log.error(String.format("大网段开通模版工单[%s]用户[%s]跳过空数据第%d行！", filename, creator, rowNum));
                        continue;
                    }
                } catch (Exception ex) {
                    log.error("批量开通导入异常", ex);
                    failureMsg.append(" 策略仿真任务: " + excelRecommendInternetTaskDTOS.get(i).getOrderNO() + " 导入失败：");
                    failureNum++;
                }
                index++;
            }
        }
        dataRecommendForExcelDTO.setHasInvalid(hasInvalid);
        dataRecommendForExcelDTO.setFailureMsg(failureMsg);
        dataRecommendForExcelDTO.setFailureNum(failureNum);
        dataRecommendForExcelDTO.setTmpList(tmpList);
        excelImportResultVO.setOrderNumber(orderNumber);
        return excelImportResultVO;
    }

    /**
     * 生成orderNo
     *
     * @param name
     * @param id
     * @return
     */
    private String generateOrderNum(String name, String id, String orderNO) {
        //主题为空，则自动生成一个
        if (AliStringUtils.isEmpty(name)) {
            return orderNO;
        } else {
            //将序号和名称拼接起来
            if (!AliStringUtils.isEmpty(id)) {
                name = String.format("%s_%s", name, id);
                return name;
            }
        }
        return orderNO;
    }

    /***
     * 将不同类型的开通从excel中解析完成，之后转成实体
     * @param entity
     * @param excelRecommendInternetTaskDTO
     * @param taskType
     * @return
     */
    @Override
    public RecommendTaskEntity toCommonTaskEntity(ExcelBigInternetTaskDTO entity, ExcelRecommendInternetTaskDTO excelRecommendInternetTaskDTO, Integer taskType, DataRecommendForExcelDTO dataRecommendForExcelDTO) throws ParseException {
        RecommendTaskEntity taskEntity = new RecommendTaskEntity();
        Date date = new Date();
        taskEntity.setCreateTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String name, orderNO, user, description,remarks, srcIpDescription, dstIpDescription,  idleTimeout, labelModel, startLabel,timeRange
                ,mergeCheck,beforeConflict;
        List<ServiceDTO> serviceDTOS;
        int ipType;
        String postSrcIp = null, postDstIp = null;
        if (taskType == BIG_INTERNET_RECOMMEND) {
            BeanUtils.copyProperties(entity, taskEntity);
            name = entity.getName();
            orderNO = entity.getOrderNO();
            user = entity.getUser();
            description = entity.getDescription();
            remarks = entity.getRemark();
            srcIpDescription = entity.getSrcIpDescription();
            dstIpDescription = entity.getDstIpDescription();
            timeRange = entity.getTimeRange();
            idleTimeout = entity.getIdleTimeout();
            labelModel = entity.getLabelModel();
            startLabel = entity.getStartLabel();
            if (StringUtils.isNotEmpty(startLabel) && StringUtils.isEmpty(labelModel)){
                labelModel = "OR";
            }
            ipType = IpTypeEnum.getIpTypeByDesc(entity.getIpType()).getCode();
            serviceDTOS =  entity.getServiceList();
            mergeCheck = entity.getMergeCheck();
            beforeConflict = entity.getBeforeConflict();
        } else {
            BeanUtils.copyProperties(excelRecommendInternetTaskDTO, taskEntity);
            name = excelRecommendInternetTaskDTO.getName();
            orderNO = excelRecommendInternetTaskDTO.getOrderNO();
            user = excelRecommendInternetTaskDTO.getUser();
            description = excelRecommendInternetTaskDTO.getDescription();
            remarks = excelRecommendInternetTaskDTO.getRemark();
            srcIpDescription = excelRecommendInternetTaskDTO.getSrcIpDescription();
            dstIpDescription = excelRecommendInternetTaskDTO.getDstIpDescription();
            idleTimeout = excelRecommendInternetTaskDTO.getIdleTimeout();
            labelModel = StringUtils.isNotBlank(excelRecommendInternetTaskDTO.getLabelModel())?excelRecommendInternetTaskDTO.getLabelModel():LABEL_MODEL_OR;
            startLabel = excelRecommendInternetTaskDTO.getStartLabel();
            ipType = IpTypeEnum.getIpTypeByDesc(excelRecommendInternetTaskDTO.getIpType()).getCode();

            timeRange = excelRecommendInternetTaskDTO.getTimeRange();
            serviceDTOS =  excelRecommendInternetTaskDTO.getServiceList();
            mergeCheck = excelRecommendInternetTaskDTO.getMergeCheck();
            beforeConflict = excelRecommendInternetTaskDTO.getBeforeConflict();
            postSrcIp = excelRecommendInternetTaskDTO.getPostSrcIp();
            postDstIp = excelRecommendInternetTaskDTO.getPostDstIp();
        }
        if(StringUtils.isNotBlank(timeRange)){
            String[] timeRanges = timeRange.split("-");
            if(timeRanges.length > 1){
                String startTime = timeRanges[0] ;
                String endTime = timeRanges[1];
                taskEntity.setStartTime(sdf.parse(startTime));
                taskEntity.setEndTime(sdf.parse(endTime));
            }
        }
        if(CollectionUtils.isNotEmpty(serviceDTOS)){
            String serviceList = JSONObject.toJSONString(serviceDTOS);
            taskEntity.setServiceList(serviceList);
        }
        taskEntity.setIpType(ipType);
        taskEntity.setTheme(name);
        taskEntity.setOrderNumber(orderNO);
        taskEntity.setUserName(user);
        taskEntity.setDescription(description);
        taskEntity.setRemarks(remarks);
        // 批量导入描述字段与新增的2个字段对应
        taskEntity.setSrcIpSystem(StringUtils.isBlank(srcIpDescription) ? null : srcIpDescription.replace("\n", ""));
        taskEntity.setDstIpSystem(StringUtils.isBlank(dstIpDescription) ? null : dstIpDescription.replace("\n", ""));
        WhatIfRO whatIf = dataRecommendForExcelDTO.getWhatIf();
        if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
            taskEntity.setWhatIfCase(whatIf.getUuid());
            taskEntity.setDeviceWhatifs(whatIf.getDeviceWhatifs());
        }
        if (!AliStringUtils.isEmpty(idleTimeout)) {
            Integer idleTimeoutInteger = Integer.valueOf(idleTimeout);
            taskEntity.setIdleTimeout(idleTimeoutInteger * HOUR_SECOND);
        }
        UserInfoDTO userInfoDTO = dataRecommendForExcelDTO.getUserInfoDTO();
        if (userInfoDTO != null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
            taskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            taskEntity.setBranchLevel("00");
        }

        if (StringUtils.isNotBlank(labelModel)  ) {
            taskEntity.setLabelModel(labelModel);

        }
        if(StringUtils.isNotBlank(startLabel)){
            taskEntity.setStartLabel(startLabel);
        }
        taskEntity.setStatus(0);
        taskEntity.setTaskType(taskType);
        taskEntity.setMergeCheck(StringUtils.isEmpty(mergeCheck)?false:Boolean.parseBoolean(mergeCheck));
        taskEntity.setBeforeConflict(StringUtils.isEmpty(beforeConflict)?false:Boolean.parseBoolean(beforeConflict));
        taskEntity.setPostSrcIp(postSrcIp);
        taskEntity.setPostDstIp(postDstIp);
        return taskEntity;
    }

    @Override
    public UserInfoDTO getUserInfo(String creator, Authentication auth) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        if(StringUtils.isBlank(creator)){
            UserInfoDTO userInfoDTO1 = remoteBranchService.findOne(auth.getName());
            BeanUtils.copyProperties(userInfoDTO1,userInfoDTO);
        }else{
            userInfoDTO.setBranchLevel("00");
            userInfoDTO.setId(creator);
        }
        return userInfoDTO;
    }

    @Override
    public ReturnResult<String> recodeFileToHistory(MultipartFile file, String fileName, String createdUser, Date date){
        FileOutputStream fos = null;
        InputStream inputStream = null;
        try {
            log.info("开始导入至仿真历史列表");
            // 保存excel到服务器上,先创建目录
            String recommendDir = downloadFileDir + "/" + RECOMMEND_HISTORY;
            if(!new File(recommendDir).exists()){
                FileUtils.createDir(recommendDir);
            }
            inputStream = file.getInputStream();
            byte[] getData = readInputStream(inputStream);
            String filePath = recommendDir + File.separator + fileName;
            File rfile = new File(filePath);
            fos = new FileOutputStream(rfile);
            fos.write(getData);

            PushRecommendTaskHistoryEntity historyEntity = new PushRecommendTaskHistoryEntity();
            String resourceFile = resourceHandler.replace("**", "") + fileName;
            historyEntity.setFileUrl(resourceFile);
            historyEntity.setFileName(fileName);
            historyEntity.setImportDate(date);
            historyEntity.setImportUser(createdUser);

            pushRecommendTaskHistoryMapper.insert(historyEntity);
        }catch (Exception e){
            log.error("仿真列表导入历史列表异常:{}",e);
            return new ReturnResult<>(ReturnResult.FAIL_CODE,"导入历史仿真列表异常");
        }finally {
            try {
                if(null != fos){
                  fos.close();
                }
                if(null != inputStream){
                    inputStream.close();
                }
            } catch (IOException e){
                log.error("仿真列表导入历史列表异常:{}",e);
            }
        }
        return ReturnResult.SUCCESS;
    }

    /**
     * 读取文件流
     * @param inputStream
     * @return
     */
    public static byte[] readInputStream(InputStream inputStream){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1){
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException ioe) {
            log.error("读取流异常,异常原因：", ioe);
        } finally {
            try{
                if(bos != null){
                    bos.close();
                }
            }catch (IOException ioe) {
                log.error("关闭流异常");
            }
        }
        return bos.toByteArray();
    }


    /**
     * 处理数据空格
     * @param str
     * @return
     */
    private String dealSpace(String str){
        return StringUtils.isBlank(str) ? "" : str.trim();
    }
}
