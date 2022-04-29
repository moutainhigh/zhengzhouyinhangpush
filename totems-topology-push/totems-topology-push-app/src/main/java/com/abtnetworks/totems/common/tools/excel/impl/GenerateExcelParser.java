package com.abtnetworks.totems.common.tools.excel.impl;

import com.abtnetworks.totems.auto.dao.mysql.AutoRecommendTaskMapper;
import com.abtnetworks.totems.auto.dto.AddressManageDetailDTO;
import com.abtnetworks.totems.auto.dto.AutoRecommendTaskExcelDTO;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.enums.InputTypeEnum;
import com.abtnetworks.totems.auto.enums.PushAccessTypeEnum;
import com.abtnetworks.totems.auto.service.AddressManageDetailService;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.IPTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.enums.TaskTypeEnum;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.tools.excel.ImportExcel;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.EntityUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.credential.dao.mysql.CredentialMapper;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.credential.entity.ExcelCredentialEntity;
import com.abtnetworks.totems.credential.service.CredentialService;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesMapper;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.generate.task.CmdTaskService;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.DNatAdditionalInfoEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelSecurityTaskEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.recommend.entity.NatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.PushAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.SNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.StaticNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.CredentialResultDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.CredentialResultRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GenerateExcelParser implements ExcelParser {

    @Autowired
    private RecommendTaskManager recommendTaskManager;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    private CmdTaskService cmdTaskService;

    @Autowired
    private CredentialService service;

    @Autowired
    private CredentialMapper credentialMapper;

    @Resource
    RemoteBranchService remoteBranchService;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    PushTaskService pushTaskService;

    @Autowired
    private DisposalScenesMapper disposalScenesDao;

    /**
     * 场景 Service
     */
    @Autowired
    public DisposalScenesService disposalScenesService;

    @Autowired
    private PushAutoRecommendService pushAutoRecommendService;

    @Autowired
    private AutoRecommendTaskMapper autoRecommendTaskMapper;

    @Autowired
    private AddressManageDetailService addressManageDetailService;

    @Override
    public String parse(MultipartFile file, UserInfoDTO userInfoDTO, List<RecommendTaskEntity> taskList, List<RecommendTaskEntity> natTaskList, List<PushRecommendStaticRoutingDTO> routeList) {
        String userName = "";
        if (userInfoDTO != null) {
            userName = userInfoDTO.getId();
        }

        String errmsg = null;
        if (taskList == null) {
            errmsg = "解析失败，任务列表为空！<br>";
            return errmsg;
        }
        try {
            int successNum = 0;
            int failureNum = 0;

            //解析数据并校验
            StringBuilder failureMsg = new StringBuilder();
            ImportExcel ei = new ImportExcel(file, 0, 0);
            List<ExcelSecurityTaskEntity> list = ei.getDataList(ExcelSecurityTaskEntity.class);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "";

            int index = 0;
            boolean hasInvalid = false;
            Set<String> indexSet = new HashSet<>();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
//                    log.info("Excel value:" + list.get(i).toString());
                    try {
                        int rowNum = i + 2;
                        //modify by zy 20200721 允许导入为空的源和目的
                        if (list.get(i) != null) {


                            ExcelSecurityTaskEntity entity = list.get(i);
                            DisposalScenesEntity disposalScenesEntity = null;
                            List<DisposalScenesDTO> scenesDTOList = null;

                            if (entity.isEmpty()) {
                                log.error(String.format("跳过空数据第%d行！", rowNum));
                                continue;
                            }

                            int rc = entity.validation();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("安全策略模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (indexSet.contains(entity.getId())) {
                                failureMsg.append("安全策略模版页第" + rowNum + "行序号重复。<br>");
                                log.error("安全策略模版页第" + rowNum + "行序号重复。");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            } else {
                                indexSet.add(entity.getId());
                            }

                            // 流水号码需要自动生成
                            String orderNO = "A" + simpleDateFormat.format(new Date()) + String.valueOf(index);
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
                                entity.setUser(userName);
                            }

                            //去掉没用的空格
                            entity.setSrcIpDescription(dealSpace(entity.getSrcIpDescription()));
                            entity.setDstIpDescription(dealSpace(entity.getDstIpDescription()));
                            entity.setName(dealSpace(entity.getName()));
                            if (!InputValueUtils.validOrderName(entity.getName())) {
                                failureMsg.append("安全策略模版页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            NodeEntity node = recommendTaskManager.getDeviceByManageIp(entity.getDeviceIp());
                            // 1.先认为是设备ip
                            // 2.再认为是虚墙ip
                            // 3.最后认为是场景，都查不到认为是异常数据
                            if (null == node) {
                                disposalScenesEntity = disposalScenesDao.getByScenesName(entity.getDeviceIp());
                                if (null == disposalScenesEntity) {
                                    failureMsg.append("安全策略模版页第" + rowNum + "行主题（防火墙IP或者场景）不合法，设备或者场景不存在！<br>");
                                    hasInvalid = true;
                                    failureNum++;
                                    continue;
                                } else {
                                    scenesDTOList = disposalScenesService.findByScenesUuid(disposalScenesEntity.getUuid());
                                    if (CollectionUtils.isEmpty(scenesDTOList)) {
                                        failureMsg.append("安全策略模版页第" + rowNum + "行主题（防火墙IP或者场景）不合法，设备或者场景不存在，或者场景下无设备！<br>");
                                        hasInvalid = true;
                                        failureNum++;
                                        continue;
                                    }
                                }
                            }

                            if (null != node && AliStringUtils.isEmpty(node.getUuid())) {
                                failureMsg.append("安全策略模版页第" + rowNum + "行主题（防火墙IP）不合法，设备UUID不存在！<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (null != node) {
                                DeviceRO device = whaleManager.getDeviceByUuid(node.getUuid());
                                if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                                    failureMsg.append("安全策略模版页第" + rowNum + "行设备数据不正确，请重新采集设备！<br>");
                                    hasInvalid = true;
                                    failureNum++;
                                    continue;
                                }
                            }

                            RecommendTaskEntity tmpEntity = entity.toTaskEntity();
                            if (userInfoDTO != null) {
                                tmpEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                            } else {
                                tmpEntity.setBranchLevel("00");
                            }
                            tmpEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                            tmpEntity.setDeviceIp(entity.getDeviceIp());
                            String timeRange = entity.getTimeRange();
                            if (StringUtils.isNotEmpty(timeRange)) {
                                String[] timeRanges = timeRange.split("-");
                                String timeTemplate = "yyyy-MM-dd HH:mm";
                                SimpleDateFormat sdf = new SimpleDateFormat(timeTemplate);
                                String time = sdf.format(new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(timeRanges[0]));
                                Date startTime = DateUtil.stringToDate(time, timeTemplate);
                                String time1 = sdf.format(new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(timeRanges[1]));
                                Date endTime = DateUtil.stringToDate(time1, timeTemplate);

                                tmpEntity.setStartTime(startTime);
                                tmpEntity.setEndTime(endTime);
                            }

                            PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
                            if (null != node) {
                                additionalInfoEntity.setDeviceUuid(node.getUuid());
                            }
                            additionalInfoEntity.setSrcZone(entity.getSrcZone());
                            additionalInfoEntity.setDstZone(entity.getDstZone());
                            additionalInfoEntity.setOutDevItf(entity.getOutDevItf());
                            additionalInfoEntity.setInDevItf(entity.getInDevItf());
                            additionalInfoEntity.setOutDevItfAlias(entity.getOutDevItf());
                            additionalInfoEntity.setInDevItfAlias(entity.getInDevItf());

                            // 附件信息添加场景相关数据
                            if (disposalScenesEntity != null && StringUtils.isNotEmpty(disposalScenesEntity.getUuid()) && CollectionUtils.isNotEmpty(scenesDTOList)) {
                                additionalInfoEntity.setScenesUuid(disposalScenesEntity.getUuid());
                                additionalInfoEntity.setScenesDTOList(scenesDTOList);
                            }
                            if (AliStringUtils.isEmpty(entity.getAction()) || entity.getAction().equalsIgnoreCase("允许")) {
                                additionalInfoEntity.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
                            } else {
                                additionalInfoEntity.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
                            }
                            tmpEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));

                            taskList.add(tmpEntity);
                        }
//                        else {
////                            failureMsg.append("安全策略模版页第" + rowNum + "行，源地址，目的地址和服务不能为空！<br>");
//                        }
                    } catch (Exception ex) {
                        log.error("批量开通导入异常", ex);
                        failureMsg.append(" 策略仿真任务: " + list.get(i).getOrderNO() + " 导入失败：<br>");
                        failureNum++;
                    }
                    index++;
                }
            } else {
                failureMsg.append("导入文件内容为空！<br>");
            }

            //模拟nat开通工单在第二页
            ImportExcel natExcel = new ImportExcel(file, 0, 1);
            List<ExcelTaskNatEntity> natExcelList = natExcel.getDataList(ExcelTaskNatEntity.class);
            String msg = checkExcelNatTaskValidation(natExcelList);
            List<RecommendTaskEntity> tmpList = getRecommendTaskEntity(natExcelList, null, userName, userInfoDTO);
            natTaskList.addAll(tmpList);
            if (!AliStringUtils.isEmpty(msg)) {
                errmsg = msg;
                return errmsg;
            }


            //静态路由开通工单在第三页
            ImportExcel routeExcel = new ImportExcel(file, 0, 2);
            List<ExcelTaskStaticRouteEntity> routeExcelList = routeExcel.getDataList(ExcelTaskStaticRouteEntity.class);
            String routeExcelMsg = checkExcelRouteTaskValidation(routeExcelList);
            List<PushRecommendStaticRoutingDTO> routeTaskList = getRouteTaskEntity(routeExcelList, userName, userInfoDTO);
            routeList.addAll(routeTaskList);

            if (!AliStringUtils.isEmpty(routeExcelMsg)) {
                errmsg = routeExcelMsg;
                return errmsg;
            }

            if (hasInvalid) {
                failureMsg.insert(0, " 失败 " + failureNum + "条任务信息 导入信息如下：<br>");

                errmsg = failureMsg.toString();
                return errmsg;
            }

            if (!hasInvalid && taskList.size() > 0) {
                successNum = taskList.size();
                if (successNum > 0) {
                    recommendTaskManager.insertRecommendTaskList(taskList);
                }
            }

        } catch (Exception e) {
            errmsg = "导入失败，请确保文件格式和内容正确！<br>";
            log.error("导入任务列表失败：", e);
            return errmsg;
        }

        return errmsg;
    }

    private DeviceDataRO getDeviceDataRO(String deviceUuid) {
        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        if (device != null) {
            DeviceDataRO deviceData = device.getData().get(0);
            return deviceData;
        }
        return null;
    }

    /**
     * 获取静态路由DTO
     *
     * @param routeExcelList
     * @param userName
     * @param userInfoDTO
     * @return
     */
    @Override
    public List<PushRecommendStaticRoutingDTO> getRouteTaskEntity(List<ExcelTaskStaticRouteEntity> routeExcelList, String userName, UserInfoDTO userInfoDTO) {
        List<PushRecommendStaticRoutingDTO> resultList = new ArrayList<>();
        if (StringUtils.isBlank(userName) && userInfoDTO != null) {
            userName = userInfoDTO.getId();
        }
        for (ExcelTaskStaticRouteEntity entity : routeExcelList) {
            PushRecommendStaticRoutingDTO routeDTO = new PushRecommendStaticRoutingDTO();


            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(entity.getDeviceIp());
            if (nodeEntity == null) {
                log.error(String.format("设备%s不存在，无法获取设备UUID，跳过转换WhatIfNatDTO过程...", entity.getDeviceIp()));
                continue;
            }

            String themeString = String.format("%s-%s", entity.getTheme(), entity.getId());

            String deviceUuid = nodeEntity.getUuid();
            routeDTO.setDeviceUuid(deviceUuid);
            routeDTO.setDeviceName(nodeEntity.getDeviceName());
            routeDTO.setDeviceIp(entity.getDeviceIp());
            routeDTO.setTheme(themeString);

            Integer ipTypeNumber = 0;
            if (StringUtils.equalsAnyIgnoreCase(entity.getIpType(), IpTypeEnum.IPV4.getDesc())) {
                ipTypeNumber = IpTypeEnum.IPV4.getCode();
            } else if (StringUtils.equalsAnyIgnoreCase(entity.getIpType(), IpTypeEnum.IPV6.getDesc())) {
                ipTypeNumber = IpTypeEnum.IPV6.getCode();
            }
            routeDTO.setIpType(ipTypeNumber);
            routeDTO.setDstIp(entity.getDstIp());
            routeDTO.setSubnetMask(StringUtils.isBlank(entity.getSubnetMask()) ? 0 : Integer.valueOf(entity.getSubnetMask()));
            routeDTO.setSrcVirtualRouter(entity.getSrcVirtualRouter());
            routeDTO.setDstVirtualRouter(entity.getDstVirtualRouter());
            routeDTO.setOutInterface(entity.getOutInterface());
            routeDTO.setNextHop(entity.getNextHop());
            routeDTO.setPriority(entity.getPriority());
            routeDTO.setManagementDistance(entity.getManagementDistance());
            routeDTO.setCreateUser(userName);
            routeDTO.setMark(entity.getMark());
            resultList.add(routeDTO);
        }
        return resultList;
    }

    @Override
    public List<RecommendRelevanceSceneDTO> getSpecialNatDTO(List<ExcelTaskSpecialNatEntity> specialNatEntityList, String userName, UserInfoDTO userInfoDTO) {
        List<RecommendRelevanceSceneDTO> resultList = new ArrayList<>();
        if (StringUtils.isBlank(userName) && userInfoDTO != null) {
            userName = userInfoDTO.getId();
        }
        for (ExcelTaskSpecialNatEntity entity : specialNatEntityList) {
            RecommendRelevanceSceneDTO sceneDTO = new RecommendRelevanceSceneDTO();


            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(entity.getDeviceIp());
            if (nodeEntity == null) {
                log.error(String.format("设备%s不存在，无法获取设备UUID，跳过转换WhatIfNatDTO过程...", entity.getDeviceIp()));
                continue;
            }

            String themeString = String.format("%s-%s", entity.getName(), entity.getId());

            String deviceUuid = nodeEntity.getUuid();
            sceneDTO.setDeviceUuid(deviceUuid);
            sceneDTO.setDeviceIp(entity.getDeviceIp());
            sceneDTO.setDeviceName(nodeEntity.getDeviceName());
            sceneDTO.setName(themeString);

            Integer ipTypeNumber = 0;
            if (StringUtils.equalsAnyIgnoreCase(entity.getIpType(), IpTypeEnum.IPV4.getDesc())) {
                ipTypeNumber = IpTypeEnum.IPV4.getCode();
            } else if (StringUtils.equalsAnyIgnoreCase(entity.getIpType(), IpTypeEnum.IPV6.getDesc())) {
                ipTypeNumber = IpTypeEnum.IPV6.getCode();
            }
            sceneDTO.setIpType(ipTypeNumber);
            sceneDTO.setSrcIp(entity.getPreSrcAddress());
            sceneDTO.setDstIp(entity.getPreDstAddress());
            sceneDTO.setPostSrcIp(entity.getPostSrcAddress());
            sceneDTO.setPostDstIp(entity.getPostDstAddress());

            sceneDTO.setPostPort(entity.getPostDstPorts());
            sceneDTO.setSrcZone(entity.getSrcZone());
            sceneDTO.setDstZone(entity.getDstZone());
            sceneDTO.setSrcItf(entity.getInDevItf());
            sceneDTO.setDstItf(entity.getOutDevItf());

            List<ServiceDTO> serviceList = new ArrayList<>();
            if (StringUtils.isNotBlank(entity.getProtocol()) && StringUtils.isNotBlank(entity.getPreDstPorts())) {
                ServiceDTO serviceDTO = new ServiceDTO();

                String protocolString = entity.getProtocol();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ICMP);
                } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_TCP);
                } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_UDP);
                }

                if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    serviceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(entity.getPreDstPorts().trim()));
                }
                serviceList.add(serviceDTO);
            }
            sceneDTO.setServiceList(serviceList);

            sceneDTO.setCreateUser(userName);
            sceneDTO.setCreateTime(new Date());
            resultList.add(sceneDTO);
        }
        return resultList;
    }


    /**
     * 检测Nat模板页工单列表合法性
     *
     * @param natExcelList nat模板页工单列表
     * @return 错误提示，若为空字符串（“”）,则无错误
     */
    private String checkExcelNatTaskValidation(List<ExcelTaskNatEntity> natExcelList) {
        StringBuilder sb = new StringBuilder();
        int rowNum = 2;
        List<ExcelTaskNatEntity> emptyList = new ArrayList<>();
        Set<String> indexSet = new HashSet<>();
        for (ExcelTaskNatEntity entity : natExcelList) {
            if (entity.isEmpty()) {
                emptyList.add(entity);
                continue;
            }

            int rc = entity.validation();
            if (rc != ReturnCode.POLICY_MSG_OK) {
                sb.append(String.format("Nat模板页第%d行错误！%s<br>", rowNum, ReturnCode.getMsg(rc)));
            }

            if (indexSet.contains(entity.getId())) {
                sb.append(String.format("Nat模板页第%d行错误！序号重复。<br>", rowNum));
            } else {
                indexSet.add(entity.getId());
            }
//KSH-5542,KSH-5099
//            if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) && AliStringUtils.isEmpty(entity.getPostDstAddress())) {
//                sb.append(String.format("Nat模板页第%d行错误！转换后源/目的地址不能都为空<br>", rowNum));
//            }
            String deviceIp = entity.getDeviceIp();
            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("Nat模板页第%d行错误！设备%s不存在！<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            DeviceRO device = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
            if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                sb.append(String.format("Nat模板页第%d行错误！设备%s数据不正确，请重新采集设备！<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            if (!InputValueUtils.validOrderName(entity.getTheme())) {
                sb.append("Nat模板页第" + rowNum + "行主题（工单号）不合法，主题（工单号）长度不超过64个字符，只能包括数字，字母和短横线(-)，只能以字母开头！<br>");
                continue;
            }
            //KSH-5542
//            if (entity.getNatType().equalsIgnoreCase("STATIC")) {
//                if (nodeEntity.getModelNumber().equals("Cisco ASA")) {
//                    if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) || AliStringUtils.isEmpty(entity.getPreSrcAddress())) {
//                        sb.append(String.format("Nat模板页第%d行错误！Cisco设备静态Nat转换前/转换后源地址不能为空！<br>", rowNum));
//                    }
//                } else {
//                    if (AliStringUtils.isEmpty(entity.getPostDstAddress())) {
//                        sb.append(String.format("Nat模板页第%d行错误！静态Nat转换后目的地址不能为空！<br>", rowNum));
//                    }
//                }
//            }
            rowNum++;
        }

        natExcelList.removeAll(emptyList);

        return sb.toString();
    }

    /**
     * 检测静态路由sheet页工单列表合法性
     *
     * @param routeExcelList 静态路由工单列表
     * @return 错误提示，若为空字符串（“”）,则无错误
     */
    private String checkExcelRouteTaskValidation(List<ExcelTaskStaticRouteEntity> routeExcelList) {
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
            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("静态路由模板第%d行错误！设备%s不存在！<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            DeviceRO device = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
            if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                sb.append(String.format("静态路由模板第%d行错误！设备%s数据不正确，请重新采集设备！<br>", rowNum, deviceIp));
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

    /**
     * 将nat开通工单Excel数据转换成NAT开通工单
     *
     * @param list     nat开通工单Excel数据列表
     * @param theme    主题
     * @param userName 用户名
     * @return NAT开通工单名称
     */
    @Override
    public List<RecommendTaskEntity> getRecommendTaskEntity(List<ExcelTaskNatEntity> list, String theme, String userName, UserInfoDTO userInfoDTO) {
        List<RecommendTaskEntity> recommendTaskEntityList = new ArrayList<>();
        if (StringUtils.isBlank(userName) && userInfoDTO != null) {
            userName = userInfoDTO.getId();
        }
        int index = 1;
        for (ExcelTaskNatEntity entity : list) {
            RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();

            //配置工单号
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "N" + simpleDateFormat.format(new Date()) + index;
            Date currentDate = new Date(DateUtil.uniqueCurrentTimeMS());

            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(entity.getDeviceIp());
            if (nodeEntity == null) {
                log.error(String.format("设备%s不存在，无法获取设备UUID，跳过转换WhatIfNatDTO过程...", entity.getDeviceIp()));
                continue;
            }
            String deviceUuid = nodeEntity.getUuid();

            String themeString = String.format("%s-%s", entity.getTheme(), entity.getId());
            if (AliStringUtils.isEmpty(entity.getTheme())) {
                themeString = String.format("%s-%s", theme, entity.getId());
            }

            recommendTaskEntity.setDeviceIp(entity.getDeviceIp());
            recommendTaskEntity.setDescription(entity.getDescription());
            recommendTaskEntity.setRemarks(entity.getRemark());
            if(userInfoDTO!=null){
                recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
            } else {
                recommendTaskEntity.setBranchLevel("00");
            }

            recommendTaskEntity.setIpType(IpTypeEnum.getIpTypeByDesc(entity.getIpType()).getCode());
            //标记为静态则为静态NAT，否则查看转换后地址，若有转换后源地址则为源NAT，若有转换后目的地址则为目的NAT
            if (entity.getNatType().equalsIgnoreCase("STATIC")) {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT);
                recommendTaskEntity.setTheme(themeString);
                recommendTaskEntity.setOrderNumber(orderNumber);
                recommendTaskEntity.setUserName(userName);
                recommendTaskEntity.setSrcIp("255.255.255.255");
                recommendTaskEntity.setDstIp("255.255.255.255");
                recommendTaskEntity.setServiceList(null);
                recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);

                recommendTaskEntity.setCreateTime(currentDate);

                StaticNatAdditionalInfoEntity additionalInfoEntity = new StaticNatAdditionalInfoEntity();

                additionalInfoEntity.setDeviceUuid(deviceUuid);
                additionalInfoEntity.setFromZone(entity.getSrcZone());
                additionalInfoEntity.setInDevItf(entity.getInDevItf());
                additionalInfoEntity.setToZone(entity.getDstZone());
                additionalInfoEntity.setOutDevItf(entity.getOutDevItf());


                //普通设备静态NAT使用目的地址，因此global为转换前，inside为转换后
                additionalInfoEntity.setGlobalAddress(entity.getPreDstAddress());
                additionalInfoEntity.setInsideAddress(entity.getPostDstAddress());
                if (nodeEntity.getModelNumber() != null) {
                    if (nodeEntity.getModelNumber().equals("Cisco ASA")) {
                        //思科设备静态NAT使用源地址，因此inside是pre，global是post
                        additionalInfoEntity.setInsideAddress(entity.getPreSrcAddress());
                        additionalInfoEntity.setGlobalAddress(entity.getPostSrcAddress());
                    }
                }

                if (entity.getPreServiceList() != null && entity.getPreServiceList().size() > 0) {
                    additionalInfoEntity.setGlobalPort(entity.getPreServiceList().get(0).getDstPorts());
                    additionalInfoEntity.setProtocol(entity.getPreServiceList().get(0).getProtocol());
                    if (entity.getPostServiceList() != null && entity.getPostServiceList().size() > 0) {
                        additionalInfoEntity.setInsidePort(entity.getPostServiceList().get(0).getDstPorts());
                    }
                }
                recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            } else if (entity.getNatType().equalsIgnoreCase("SNAT")) {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT);
                recommendTaskEntity.setTheme(themeString);
                recommendTaskEntity.setOrderNumber(orderNumber);
                recommendTaskEntity.setUserName(userName);
                recommendTaskEntity.setSrcIp(entity.getPreSrcAddress());
                recommendTaskEntity.setDstIp(entity.getPreDstAddress());
                recommendTaskEntity.setPostSrcIpSystem(entity.getPostSrcIpSystem());
                List<ServiceDTO> serviceDTOList = entity.getPreServiceList();
                recommendTaskEntity.setCreateTime(currentDate);
                if (serviceDTOList != null) {
                    recommendTaskEntity.setServiceList(JSONObject.toJSONString(serviceDTOList));
                }
                recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
                recommendTaskEntity.setSrcIpSystem(
                        StringUtils.isBlank(entity.getSrcIpSystem()) ? null : entity.getSrcIpSystem().replace("\n", ""));
                recommendTaskEntity.setDstIpSystem(
                        StringUtils.isBlank(entity.getDstIpSystem()) ? null : entity.getDstIpSystem().replace("\n", ""));
                recommendTaskEntity.setPostSrcIpSystem(StringUtils.isBlank(entity.getPostSrcIpSystem()) ? null
                        : entity.getPostSrcIpSystem().replace("\n", ""));
                recommendTaskEntity.setPostSrcIp(StringUtils.isBlank(entity.getPostSrcAddress()) ? null
                        : entity.getPostSrcAddress().replace("\n", ""));

                SNatAdditionalInfoEntity additionalInfoEntity = new SNatAdditionalInfoEntity();
                additionalInfoEntity.setDeviceUuid(deviceUuid);
                additionalInfoEntity.setSrcZone(entity.getSrcZone());
                additionalInfoEntity.setSrcItf(entity.getInDevItf());
                additionalInfoEntity.setPostIpAddress(entity.getPostSrcAddress());
                additionalInfoEntity.setDstZone(entity.getDstZone());
                additionalInfoEntity.setDstItf(entity.getOutDevItf());
                recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            } else if (entity.getNatType().equalsIgnoreCase("DNAT")) {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT);
                recommendTaskEntity.setTheme(themeString);
                recommendTaskEntity.setOrderNumber(orderNumber);
                recommendTaskEntity.setUserName(userName);
                recommendTaskEntity.setSrcIp(entity.getPreSrcAddress());
                recommendTaskEntity.setDstIp(entity.getPreDstAddress());
                List<ServiceDTO> serviceDTOList = entity.getPreServiceList();
                recommendTaskEntity.setCreateTime(currentDate);
                if (serviceDTOList != null) {
                    recommendTaskEntity.setServiceList(JSONObject.toJSONString(serviceDTOList));
                }
                recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
                recommendTaskEntity.setSrcIpSystem(
                        StringUtils.isBlank(entity.getSrcIpSystem()) ? null : entity.getSrcIpSystem().replace("\n", ""));
                recommendTaskEntity.setDstIpSystem(
                        StringUtils.isBlank(entity.getDstIpSystem()) ? null : entity.getDstIpSystem().replace("\n", ""));
                recommendTaskEntity.setPostDstIpSystem(StringUtils.isBlank(entity.getPostDstIpSystem()) ? null
                        : entity.getPostDstIpSystem().replace("\n", ""));
                recommendTaskEntity.setPostDstIp(StringUtils.isBlank(entity.getPostDstAddress()) ? null
                        : entity.getPostDstAddress().replace("\n", ""));

                DNatAdditionalInfoEntity additionalInfoEntity = new DNatAdditionalInfoEntity();
                additionalInfoEntity.setDeviceUuid(deviceUuid);

                additionalInfoEntity.setSrcZone(entity.getSrcZone());
                additionalInfoEntity.setSrcItf(entity.getInDevItf());
                additionalInfoEntity.setDstZone(entity.getDstZone());
                additionalInfoEntity.setDstItf(entity.getOutDevItf());
                additionalInfoEntity.setPostIpAddress(entity.getPostDstAddress());
                if (entity.getPostServiceList() != null && entity.getPostServiceList().size() > 0) {
                    additionalInfoEntity.setPostPort(entity.getPostServiceList().get(0).getDstPorts());
                }
                recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            } else if (entity.getNatType().equalsIgnoreCase("BOTH")) {
                recommendTaskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT);
                recommendTaskEntity.setTheme(themeString);
                recommendTaskEntity.setOrderNumber(orderNumber);
                recommendTaskEntity.setUserName(userName);
                recommendTaskEntity.setSrcIp(entity.getPreSrcAddress());
                recommendTaskEntity.setDstIp(entity.getPreDstAddress());
                List<ServiceDTO> serviceDTOList = entity.getPreServiceList();
                recommendTaskEntity.setCreateTime(currentDate);
                if (serviceDTOList != null) {
                    recommendTaskEntity.setServiceList(JSONObject.toJSONString(serviceDTOList));
                }
                recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);

                NatAdditionalInfoEntity additionalInfoEntity = new NatAdditionalInfoEntity();
                additionalInfoEntity.setDeviceUuid(deviceUuid);

                additionalInfoEntity.setSrcZone(entity.getSrcZone());
                additionalInfoEntity.setSrcItf(entity.getInDevItf());
                additionalInfoEntity.setDstZone(entity.getDstZone());
                additionalInfoEntity.setDstItf(entity.getOutDevItf());
                additionalInfoEntity.setPostSrcIp(entity.getPostSrcAddress());
                additionalInfoEntity.setPostDstIp(entity.getPostDstAddress());
                if (entity.getPostServiceList() != null && entity.getPostServiceList().size() > 0) {
                    additionalInfoEntity.setPostPort(entity.getPostServiceList().get(0).getDstPorts());
                }
                recommendTaskEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
            } else {
                log.error("非法NAT类型：" + entity.getNatType());
            }

            log.info("recommendTaskEntity is " + JSONObject.toJSONString(recommendTaskEntity));
            recommendTaskEntityList.add(recommendTaskEntity);
            index++;
        }

        return recommendTaskEntityList;
    }

    @Override
    public void createNatCommandTask(List<RecommendTaskEntity> natTaskList, Authentication authentication) {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(authentication.getName());
        for (RecommendTaskEntity entity : natTaskList) {

            if (entity.getTaskType() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT) {
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                StaticNatAdditionalInfoEntity additionalInfo = object.toJavaObject(StaticNatAdditionalInfoEntity.class);

                String userName = entity.getUserName();
                String theme = entity.getTheme();
                String deviceUuid = additionalInfo.getDeviceUuid();

                CommandTaskEditableEntity commandTaskEntity = EntityUtils.createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT,
                        entity.getId(), userName, theme, deviceUuid);
                if (userInfoDTO != null) {
                    commandTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                } else {
                    commandTaskEntity.setBranchLevel("00");
                }
                commandTaskManager.insertCommandEditableEntityTask(commandTaskEntity);
                DeviceDataRO deviceDataRO = getDeviceDataRO(deviceUuid);
                boolean isVsys = false;
                String vsysName = "";
                if (deviceDataRO != null && deviceDataRO.getIsVsys() != null) {
                    isVsys = deviceDataRO.getIsVsys();
                    vsysName = deviceDataRO.getVsysName();
                }
                CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.STATIC, commandTaskEntity.getId(), commandTaskEntity.getTaskId(), deviceUuid, theme,
                        userName, null, additionalInfo.getGlobalAddress(), null,
                        additionalInfo.getInsideAddress(), EntityUtils.getServiceList(additionalInfo.getProtocol(), additionalInfo.getGlobalPort()),
                        EntityUtils.getServiceList(additionalInfo.getProtocol(), additionalInfo.getInsidePort()), additionalInfo.getFromZone(),
                        additionalInfo.getToZone(), additionalInfo.getInDevItf(), additionalInfo.getOutDevItf(), entity.getDescription(), isVsys, vsysName, null, null, null, null);
                cmdDTO.getPolicy().setIpType(entity.getIpType());
                cmdDTO.getTask().setTaskTypeEnum(TaskTypeEnum.STATIC_TYPE);
                cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT);
                pushTaskService.addGenerateCmdTask(cmdDTO);
            } else if (entity.getTaskType() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT) {
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                SNatAdditionalInfoEntity additionalInfo = object.toJavaObject(SNatAdditionalInfoEntity.class);
                String theme = entity.getTheme();
                String userName = entity.getUserName();
                String deviceUuid = additionalInfo.getDeviceUuid();

                List<ServiceDTO> serviceList = new ArrayList<>();
                if (!AliStringUtils.isEmpty(entity.getServiceList())) {
                    JSONArray array = JSONObject.parseArray(entity.getServiceList());
                    serviceList = array.toJavaList(ServiceDTO.class);
                }

                CommandTaskEditableEntity commandTaskEntity = EntityUtils.createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT,
                        entity.getId(), userName, theme, deviceUuid);
                if (userInfoDTO != null) {
                    commandTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                } else {
                    commandTaskEntity.setBranchLevel("00");
                }
                commandTaskManager.insertCommandEditableEntityTask(commandTaskEntity);
                DeviceDataRO deviceDataRO = getDeviceDataRO(deviceUuid);
                boolean isVsys = false;
                String vsysName = "";
                if (deviceDataRO != null && deviceDataRO.getIsVsys() != null) {
                    isVsys = deviceDataRO.getIsVsys();
                    vsysName = deviceDataRO.getVsysName();
                }
                CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.SNAT, commandTaskEntity.getId(), commandTaskEntity.getTaskId(),
                        deviceUuid, theme, userName, entity.getSrcIp(), entity.getDstIp(), additionalInfo.getPostIpAddress(),
                        null, serviceList, null, additionalInfo.getSrcZone(),
                        additionalInfo.getDstZone(), additionalInfo.getSrcItf(), additionalInfo.getDstItf(), entity.getDescription(), isVsys, vsysName,
                        entity.getSrcIpSystem(), entity.getDstIpSystem(), entity.getPostSrcIpSystem(), null);
                cmdDTO.getPolicy().setIpType(entity.getIpType());
                cmdDTO.getTask().setTaskTypeEnum(TaskTypeEnum.SNAT_TYPE);
                cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT);
                pushTaskService.addGenerateCmdTask(cmdDTO);
            } else if (entity.getTaskType() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT) {
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                DNatAdditionalInfoEntity additionalInfo = object.toJavaObject(DNatAdditionalInfoEntity.class);
                String theme = entity.getTheme();
                String userName = entity.getUserName();
                String deviceUuid = additionalInfo.getDeviceUuid();

                List<ServiceDTO> serviceList = new ArrayList<>();
                if (!AliStringUtils.isEmpty(entity.getServiceList())) {
                    JSONArray array = JSONObject.parseArray(entity.getServiceList());
                    serviceList = array.toJavaList(ServiceDTO.class);
                }

                List<ServiceDTO> postServiceList = EntityUtils.getPostServiceList(serviceList, additionalInfo.getPostPort());

                CommandTaskEditableEntity commandTaskEntity = EntityUtils.createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT,
                        entity.getId(), userName, theme, deviceUuid);
                if (userInfoDTO != null) {
                    commandTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                } else {
                    commandTaskEntity.setBranchLevel("00");
                }
                commandTaskManager.insertCommandEditableEntityTask(commandTaskEntity);
                DeviceDataRO deviceDataRO = getDeviceDataRO(deviceUuid);
                boolean isVsys = false;
                String vsysName = "";
                if (deviceDataRO != null && deviceDataRO.getIsVsys() != null) {
                    isVsys = deviceDataRO.getIsVsys();
                    vsysName = deviceDataRO.getVsysName();
                }
                CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.DNAT, commandTaskEntity.getId(), commandTaskEntity.getTaskId(),
                        deviceUuid, theme, userName, entity.getSrcIp(), entity.getDstIp(), null,
                        additionalInfo.getPostIpAddress(), serviceList, postServiceList, additionalInfo.getSrcZone(),
                        additionalInfo.getDstZone(), additionalInfo.getSrcItf(), additionalInfo.getDstItf(), entity.getDescription(), isVsys, vsysName,
                        entity.getSrcIpSystem(), entity.getDstIpSystem(), entity.getPostDstIpSystem(), null);
                cmdDTO.getPolicy().setIpType(entity.getIpType());
                cmdDTO.getTask().setTaskTypeEnum(TaskTypeEnum.DNAT_TYPE);
                cmdDTO.getPolicy().setPostDstIpSystem(entity.getPostDstIpSystem());
                cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT);
                pushTaskService.addGenerateCmdTask(cmdDTO);
            }
            if (entity.getTaskType() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT) {
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                NatAdditionalInfoEntity additionalInfo = object.toJavaObject(NatAdditionalInfoEntity.class);

                String theme = entity.getTheme();
                String userName = entity.getUserName();
                String deviceUuid = additionalInfo.getDeviceUuid();

                List<ServiceDTO> serviceList = new ArrayList<>();
                if (!AliStringUtils.isEmpty(entity.getServiceList())) {
                    JSONArray array = JSONObject.parseArray(entity.getServiceList());
                    serviceList = array.toJavaList(ServiceDTO.class);
                }

                List<ServiceDTO> postServiceList = EntityUtils.getPostServiceList(serviceList, additionalInfo.getPostPort());

                CommandTaskEditableEntity commandTaskEntity = EntityUtils.createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT,
                        entity.getId(), userName, theme, deviceUuid);
                if (userInfoDTO != null) {
                    commandTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
                } else {
                    commandTaskEntity.setBranchLevel("00");
                }
                commandTaskManager.insertCommandEditableEntityTask(commandTaskEntity);
                DeviceDataRO deviceDataRO = getDeviceDataRO(deviceUuid);
                boolean isVsys = false;
                String vsysName = "";
                if (deviceDataRO != null && deviceDataRO.getIsVsys() != null) {
                    isVsys = deviceDataRO.getIsVsys();
                    vsysName = deviceDataRO.getVsysName();
                }
                CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.BOTH, commandTaskEntity.getId(), commandTaskEntity.getTaskId(),
                        deviceUuid, theme, userName, entity.getSrcIp(), entity.getDstIp(), additionalInfo.getPostSrcIp(),
                        additionalInfo.getPostDstIp(), serviceList, postServiceList, additionalInfo.getSrcZone(),
                        additionalInfo.getDstZone(), additionalInfo.getSrcItf(), additionalInfo.getDstItf(), entity.getDescription(), isVsys, vsysName, null, null, null, null);
                cmdDTO.getPolicy().setIpType(entity.getIpType());
                cmdDTO.getTask().setTaskTypeEnum(TaskTypeEnum.BOTHNAT_TYPE);
                cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT);
                pushTaskService.addGenerateCmdTask(cmdDTO);
            }
        }
    }

    @Override
    public String parseCredentialExcel(MultipartFile file, String userName, List<ExcelCredentialEntity> taskList, Boolean encrypt) {
        String errMsg = null;
        if (taskList == null) {
            errMsg = "解析失败，任务列表为空！<br>";
            return errMsg;
        }
        try {
            int failureNum = 0;
            //解析数据并校验
            StringBuilder failureMsg = new StringBuilder();
            ImportExcel ei = new ImportExcel(file, 0, 0);
            List<ExcelCredentialEntity> list = ei.getDataList(ExcelCredentialEntity.class);

            boolean hasInvalid = false;
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    log.info("Excel value:" + list.get(i).toString());
                    try {
                        int rowNum = i + 1;
                        if (list.get(i) != null) {
                            ExcelCredentialEntity entity = list.get(i);

                            if (entity.isEmpty()) {
                                log.error(String.format("跳过空数据第%d行！", rowNum));
                                continue;
                            }

                            if (StringUtils.isEmpty(entity.getName())) {
                                failureMsg.append("凭据批量生成模板页第" + rowNum + "行凭据名为空！<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (StringUtils.isEmpty(entity.getLoginName())) {
                                failureMsg.append("凭据批量生成模板页第" + rowNum + "行用户名为空！<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (StringUtils.isEmpty(entity.getLoginPassword())) {
                                failureMsg.append("凭据批量生成模板页第" + rowNum + "行密码为空！<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }
                            taskList.add(entity);
                        }
                    } catch (Exception ex) {
                        log.error("批量导入凭据异常", ex);
                        failureMsg.append(" 批量导入凭据异常: " + list.get(i).getName() + " 导入失败：<br>");
                        failureNum++;
                    }
                }
            } else {
                failureMsg.append("导入文件内容为空！<br>");
            }

            if (hasInvalid) {
                failureMsg.insert(0, " 失败 " + failureNum + "条任务信息 导入信息如下：<br>");
                errMsg = failureMsg.toString();
                return errMsg;
            }

            if (!hasInvalid && taskList.size() > 0) {
                for (ExcelCredentialEntity credentialEntity : taskList) {
                    String loginPassword = credentialEntity.getLoginPassword();
                    String enableUserName = "";
                    if (StringUtils.isNotEmpty(credentialEntity.getEnableUserName())) {
                        enableUserName = credentialEntity.getEnableUserName();
                    }

                    // 检查凭据名称是否存在，存在则编辑
                    CredentialEntity credential = credentialMapper.getByName(credentialEntity.getName());
                    if (null != credential) {
                        // 为了取到编辑时的version，要再查一次凭据信息
                        CredentialResultRO creResult = service.get(credential.getUuid());
                        List<CredentialResultDataRO> data = creResult.getData();
                        if (CollectionUtils.isNotEmpty(data)) {
                            String version = data.get(0).getVersion();
                            service.modify(credential.getId(), credential.getUuid(), credentialEntity.getName(), "", credentialEntity.getLoginName(), loginPassword,
                                    enableUserName, credentialEntity.getEnablePassword(), version, userName, encrypt);
                        } else {
                            log.error("未查询到uuid为：{}的凭据", credential.getUuid());
                            throw new Exception("查询凭据异常！");
                        }
                    } else {
                        // 新建凭据
                        service.create(credentialEntity.getName(), "", credentialEntity.getLoginName(), loginPassword,
                                enableUserName, credentialEntity.getEnablePassword(), userName, encrypt);
                    }
                }
            }
        } catch (Exception e) {
            errMsg = "导入失败，请确保文件格式和内容正确！<br>";
            log.error("导入凭据失败：", e);
            return errMsg;
        }
        return errMsg;
    }

    @Override
    public String parseAutoRecommendExcel(MultipartFile file, String userName, List<AutoRecommendTaskVO> taskList) {
        String errMsg = null;
        if(taskList == null) {
            errMsg =  "解析失败，任务列表为空！<br>";
            return errMsg;
        }
        try {
            int failureNum = 0;
            //解析数据并校验
            StringBuilder failureMsg = new StringBuilder();
            ImportExcel ei = new ImportExcel(file, 0, 0);
            List<AutoRecommendTaskExcelDTO> list = ei.getDataList(AutoRecommendTaskExcelDTO.class);

            boolean hasInvalid = false;
            List<String> themeList = new ArrayList<>();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    log.info("Excel value:" + list.get(i).toString());
                    try {
                        int rowNum = i + 1;
                        if (list.get(i) != null) {
                            AutoRecommendTaskExcelDTO entity = list.get(i);

                            if(entity.isEmpty()) {
                                log.error(String.format("跳过空数据第%d行！", rowNum));
                                continue;
                            }

                            if(StringUtils.isEmpty(entity.getTheme())){
                                failureMsg.append("自动开通模板页第" + rowNum + "行主题/工单号为空！<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            if (StringUtils.isBlank(entity.getTaskType())) {
                                failureMsg.append("自动开通模板页第" + rowNum + "行访问类型为空！<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            //数据验证
                            int rc = entity.validation();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("自动开通模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            AutoRecommendTaskVO taskVO = new AutoRecommendTaskVO();
                            BeanUtils.copyProperties(entity, taskVO);

                            //设置生效时间
                            String timeRange = entity.getTimeRange();
                            if(StringUtils.isNotEmpty(timeRange)){
                                String[] timeRanges = timeRange.split("-");
                                String timeTemplate = "yyyy-MM-dd HH:mm";
                                SimpleDateFormat sdf = new SimpleDateFormat(timeTemplate);
                                String time = sdf.format(new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(timeRanges[0])) ;
                                Date startTime = DateUtil.stringToDate(time,timeTemplate);
                                String time1 = sdf.format(new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(timeRanges[1]));
                                Date endTime = DateUtil.stringToDate(time1,timeTemplate);

                                taskVO.setStartTime(startTime.getTime());
                                taskVO.setEndTime(endTime.getTime());
                            }

                            taskVO.setAccessType(PushAccessTypeEnum.getCodeByDesc(entity.getTaskType()));
                            taskVO.setUserName(userName);
                            String errorMsg = "";
                            if (PushAccessTypeEnum.INSIDE_TO_INSIDE.getCode().equals(taskVO.getAccessType()) && StringUtils.isEmpty(taskVO.getSrcIp()) && StringUtils.isEmpty(taskVO.getDstIp())) {
                                errorMsg = "内网访问，源地址、目的地址不能同时为空";
                            } else if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(taskVO.getAccessType()) && StringUtils.isEmpty(taskVO.getSrcIp())) {
                                errorMsg = "内网访问互联网，源地址不能为空";
                            } else if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(taskVO.getAccessType()) && StringUtils.isEmpty(taskVO.getDstIp())) {
                                errorMsg = "互联网访问内网，目的地址不能为空";
                            }

                            taskVO.setSrcInputType(InputTypeEnum.getCodeByDesc("源地址输入"+entity.getSrcInputType()));
                            taskVO.setDstInputType(InputTypeEnum.getCodeByDesc("目的地址输入"+entity.getDstInputType()));
                            if (InputTypeEnum.SRC_INPUT_TYPE_OBJECT.getCode().equals(taskVO.getSrcInputType()) && StringUtils.isNotEmpty(taskVO.getSrcIp())) {
                                rc = checkAddressObject(taskVO.getSrcIp());
                                if (rc != ReturnCode.POLICY_MSG_OK) {
                                    failureMsg.append("自动开通模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                    log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                    hasInvalid = true;
                                    failureNum ++;
                                    continue;
                                }
                                taskVO.setSrcAddressObjectName(taskVO.getSrcIp());
                                taskVO.setSrcIp("");
                            }
                            if (InputTypeEnum.DST_INPUT_TYPE_OBJECT.getCode().equals(taskVO.getDstInputType()) && StringUtils.isNotEmpty(taskVO.getDstIp())) {
                                rc = checkAddressObject(taskVO.getDstIp());
                                if (rc != ReturnCode.POLICY_MSG_OK) {
                                    failureMsg.append("自动开通模版页第" + rowNum + "行错误！" + ReturnCode.getMsg(rc) + "<br>");
                                    log.info("数据\n" + entity.toString() + "\n错误！" + ReturnCode.getMsg(rc));
                                    hasInvalid = true;
                                    failureNum ++;
                                    continue;
                                }
                                taskVO.setDstAddressObjectName(taskVO.getDstIp());
                                taskVO.setDstIp("");
                            }

                            if (StringUtils.isNotEmpty(errorMsg)) {
                                log.error("主题为：{} 的工单已存在有误，{}", entity.getTheme(), errorMsg);
                                failureMsg.append("自动开通模版页第" + rowNum + "行错误！" + errorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            AutoRecommendTaskEntity getByName = autoRecommendTaskMapper.getByName(entity.getTheme());
                            if( null != getByName ){
                                log.error("主题为：{} 的工单已存在！", entity.getTheme());
                                String srcErrorMsg = String.format("主题为：%s的工单已存在", entity.getTheme());
                                failureMsg.append("自动开通模版页第" + rowNum + "行错误！" + srcErrorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            if (themeList.contains(entity.getTheme())) {
                                log.error("主题为：{} 的工单重复！", entity.getTheme());
                                String srcErrorMsg = String.format("主题为：%s的工单重复", entity.getTheme());
                                failureMsg.append("自动开通模版页第" + rowNum + "行错误！" + srcErrorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }
                            // 检查防护网段配置，查询域和接口信息--这个步骤应该放在参数组装之前
                            taskList.add(taskVO);
                            themeList.add(entity.getTheme());
                        }
                    } catch (Exception ex) {
                        hasInvalid = true;
                        log.error("批量导入自动开通工单异常", ex);
                        failureMsg.append(" 批量导入自动开通异常: " + list.get(i).getTheme() + " 导入失败：<br>");
                        failureNum++;
                    }
                }
            } else {
                hasInvalid = true;
                failureMsg.append("导入文件内容为空！<br>");
            }

            if (hasInvalid) {
                failureMsg.insert(0, " 失败 " + failureNum + "条任务信息 导入信息如下：<br>");
                errMsg = failureMsg.toString();
                return errMsg;
            }

        } catch (Exception e) {
            errMsg = "导入失败，请确保文件格式和内容正确！<br>";
            log.error("导入自动开通失败：", e);
            return errMsg;
        }
        return errMsg;
    }

    private Integer checkAddressObject(String ipAddress){
        int rc = 0 ;
        List<AddressManageDetailDTO> rList = addressManageDetailService.findAddressByName(null);
        List<String> list = new ArrayList<>(Arrays.asList(ipAddress.split(",")));
        if (CollectionUtils.isNotEmpty(rList)){
            List<String> stringList = rList.stream().map(p -> p.getAddressName()).collect(Collectors.toList());
            list.removeAll(stringList);
            if (CollectionUtils.isNotEmpty(list)){
                log.error("地址对象名称中有不存在于地址对象列表的数据:ipAddressName:{}", list.toString());
                rc = ReturnCode.ADDRESS_NOT_EXIST;
                return rc;
            }
        }else {
            rc = ReturnCode.ADDRESS_LIST_EMPTY;
            return rc;
        }
        return rc;
    }

    /**
     * 处理数据空格
     *
     * @param str
     * @return
     */
    private String dealSpace(String str) {
        return StringUtils.isBlank(str) ? "" : str.trim();
    }
}
