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
     * ?????? Service
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
            errmsg = "????????????????????????????????????<br>";
            return errmsg;
        }
        try {
            int successNum = 0;
            int failureNum = 0;

            //?????????????????????
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
                        //modify by zy 20200721 ?????????????????????????????????
                        if (list.get(i) != null) {


                            ExcelSecurityTaskEntity entity = list.get(i);
                            DisposalScenesEntity disposalScenesEntity = null;
                            List<DisposalScenesDTO> scenesDTOList = null;

                            if (entity.isEmpty()) {
                                log.error(String.format("??????????????????%d??????", rowNum));
                                continue;
                            }

                            int rc = entity.validation();
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
                            String orderNO = "A" + simpleDateFormat.format(new Date()) + String.valueOf(index);
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
                                entity.setUser(userName);
                            }

                            //?????????????????????
                            entity.setSrcIpDescription(dealSpace(entity.getSrcIpDescription()));
                            entity.setDstIpDescription(dealSpace(entity.getDstIpDescription()));
                            entity.setName(dealSpace(entity.getName()));
                            if (!InputValueUtils.validOrderName(entity.getName())) {
                                failureMsg.append("????????????????????????" + rowNum + "????????????????????????????????????????????????????????????????????????64???????????????????????????????????????????????????(-)???????????????????????????<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            NodeEntity node = recommendTaskManager.getDeviceByManageIp(entity.getDeviceIp());
                            // 1.??????????????????ip
                            // 2.??????????????????ip
                            // 3.?????????????????????????????????????????????????????????
                            if (null == node) {
                                disposalScenesEntity = disposalScenesDao.getByScenesName(entity.getDeviceIp());
                                if (null == disposalScenesEntity) {
                                    failureMsg.append("????????????????????????" + rowNum + "?????????????????????IP?????????????????????????????????????????????????????????<br>");
                                    hasInvalid = true;
                                    failureNum++;
                                    continue;
                                } else {
                                    scenesDTOList = disposalScenesService.findByScenesUuid(disposalScenesEntity.getUuid());
                                    if (CollectionUtils.isEmpty(scenesDTOList)) {
                                        failureMsg.append("????????????????????????" + rowNum + "?????????????????????IP????????????????????????????????????????????????????????????????????????????????????<br>");
                                        hasInvalid = true;
                                        failureNum++;
                                        continue;
                                    }
                                }
                            }

                            if (null != node && AliStringUtils.isEmpty(node.getUuid())) {
                                failureMsg.append("????????????????????????" + rowNum + "?????????????????????IP?????????????????????UUID????????????<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (null != node) {
                                DeviceRO device = whaleManager.getDeviceByUuid(node.getUuid());
                                if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                                    failureMsg.append("????????????????????????" + rowNum + "???????????????????????????????????????????????????<br>");
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

                            // ????????????????????????????????????
                            if (disposalScenesEntity != null && StringUtils.isNotEmpty(disposalScenesEntity.getUuid()) && CollectionUtils.isNotEmpty(scenesDTOList)) {
                                additionalInfoEntity.setScenesUuid(disposalScenesEntity.getUuid());
                                additionalInfoEntity.setScenesDTOList(scenesDTOList);
                            }
                            if (AliStringUtils.isEmpty(entity.getAction()) || entity.getAction().equalsIgnoreCase("??????")) {
                                additionalInfoEntity.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
                            } else {
                                additionalInfoEntity.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
                            }
                            tmpEntity.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));

                            taskList.add(tmpEntity);
                        }
//                        else {
////                            failureMsg.append("????????????????????????" + rowNum + "??????????????????????????????????????????????????????<br>");
//                        }
                    } catch (Exception ex) {
                        log.error("????????????????????????", ex);
                        failureMsg.append(" ??????????????????: " + list.get(i).getOrderNO() + " ???????????????<br>");
                        failureNum++;
                    }
                    index++;
                }
            } else {
                failureMsg.append("???????????????????????????<br>");
            }

            //??????nat????????????????????????
            ImportExcel natExcel = new ImportExcel(file, 0, 1);
            List<ExcelTaskNatEntity> natExcelList = natExcel.getDataList(ExcelTaskNatEntity.class);
            String msg = checkExcelNatTaskValidation(natExcelList);
            List<RecommendTaskEntity> tmpList = getRecommendTaskEntity(natExcelList, null, userName, userInfoDTO);
            natTaskList.addAll(tmpList);
            if (!AliStringUtils.isEmpty(msg)) {
                errmsg = msg;
                return errmsg;
            }


            //????????????????????????????????????
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
                failureMsg.insert(0, " ?????? " + failureNum + "??????????????? ?????????????????????<br>");

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
            errmsg = "??????????????????????????????????????????????????????<br>";
            log.error("???????????????????????????", e);
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
     * ??????????????????DTO
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
                log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", entity.getDeviceIp()));
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
                log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", entity.getDeviceIp()));
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
     * ??????Nat??????????????????????????????
     *
     * @param natExcelList nat?????????????????????
     * @return ?????????????????????????????????????????????,????????????
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
                sb.append(String.format("Nat????????????%d????????????%s<br>", rowNum, ReturnCode.getMsg(rc)));
            }

            if (indexSet.contains(entity.getId())) {
                sb.append(String.format("Nat????????????%d???????????????????????????<br>", rowNum));
            } else {
                indexSet.add(entity.getId());
            }
//KSH-5542,KSH-5099
//            if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) && AliStringUtils.isEmpty(entity.getPostDstAddress())) {
//                sb.append(String.format("Nat????????????%d????????????????????????/???????????????????????????<br>", rowNum));
//            }
            String deviceIp = entity.getDeviceIp();
            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("Nat????????????%d??????????????????%s????????????<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            DeviceRO device = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
            if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                sb.append(String.format("Nat????????????%d??????????????????%s??????????????????????????????????????????<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            if (!InputValueUtils.validOrderName(entity.getTheme())) {
                sb.append("Nat????????????" + rowNum + "????????????????????????????????????????????????????????????????????????64???????????????????????????????????????????????????(-)???????????????????????????<br>");
                continue;
            }
            //KSH-5542
//            if (entity.getNatType().equalsIgnoreCase("STATIC")) {
//                if (nodeEntity.getModelNumber().equals("Cisco ASA")) {
//                    if (AliStringUtils.isEmpty(entity.getPostSrcAddress()) || AliStringUtils.isEmpty(entity.getPreSrcAddress())) {
//                        sb.append(String.format("Nat????????????%d????????????Cisco????????????Nat?????????/?????????????????????????????????<br>", rowNum));
//                    }
//                } else {
//                    if (AliStringUtils.isEmpty(entity.getPostDstAddress())) {
//                        sb.append(String.format("Nat????????????%d??????????????????Nat????????????????????????????????????<br>", rowNum));
//                    }
//                }
//            }
            rowNum++;
        }

        natExcelList.removeAll(emptyList);

        return sb.toString();
    }

    /**
     * ??????????????????sheet????????????????????????
     *
     * @param routeExcelList ????????????????????????
     * @return ?????????????????????????????????????????????,????????????
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
                sb.append(String.format("????????????????????????%d????????????%s<br>", rowNum, ReturnCode.getMsg(rc)));
            }

            if (indexSet.contains(entity.getId())) {
                sb.append(String.format("?????????????????????%d???????????????????????????<br>", rowNum));
            } else {
                indexSet.add(entity.getId());
            }
            String deviceIp = entity.getDeviceIp();
            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(deviceIp);
            if (nodeEntity == null) {
                sb.append(String.format("?????????????????????%d??????????????????%s????????????<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            DeviceRO device = whaleManager.getDeviceByUuid(nodeEntity.getUuid());
            if (ObjectUtils.isEmpty(device) || ObjectUtils.isEmpty(device.getData())) {
                sb.append(String.format("?????????????????????%d??????????????????%s??????????????????????????????????????????<br>", rowNum, deviceIp));
                rowNum++;
                continue;
            }

            if (!InputValueUtils.validOrderName(entity.getTheme())) {
                sb.append("?????????????????????" + rowNum + "????????????????????????????????????????????????????????????????????????64???????????????????????????????????????????????????(-)???????????????????????????<br>");
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
     * ???nat????????????Excel???????????????NAT????????????
     *
     * @param list     nat????????????Excel????????????
     * @param theme    ??????
     * @param userName ?????????
     * @return NAT??????????????????
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

            //???????????????
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNumber = "N" + simpleDateFormat.format(new Date()) + index;
            Date currentDate = new Date(DateUtil.uniqueCurrentTimeMS());

            NodeEntity nodeEntity = recommendTaskManager.getDeviceByManageIp(entity.getDeviceIp());
            if (nodeEntity == null) {
                log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", entity.getDeviceIp()));
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
            //???????????????????????????NAT??????????????????????????????????????????????????????????????????NAT??????????????????????????????????????????NAT
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


                //??????????????????NAT???????????????????????????global???????????????inside????????????
                additionalInfoEntity.setGlobalAddress(entity.getPreDstAddress());
                additionalInfoEntity.setInsideAddress(entity.getPostDstAddress());
                if (nodeEntity.getModelNumber() != null) {
                    if (nodeEntity.getModelNumber().equals("Cisco ASA")) {
                        //??????????????????NAT????????????????????????inside???pre???global???post
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
                log.error("??????NAT?????????" + entity.getNatType());
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
            errMsg = "????????????????????????????????????<br>";
            return errMsg;
        }
        try {
            int failureNum = 0;
            //?????????????????????
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
                                log.error(String.format("??????????????????%d??????", rowNum));
                                continue;
                            }

                            if (StringUtils.isEmpty(entity.getName())) {
                                failureMsg.append("??????????????????????????????" + rowNum + "?????????????????????<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (StringUtils.isEmpty(entity.getLoginName())) {
                                failureMsg.append("??????????????????????????????" + rowNum + "?????????????????????<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }

                            if (StringUtils.isEmpty(entity.getLoginPassword())) {
                                failureMsg.append("??????????????????????????????" + rowNum + "??????????????????<br>");
                                hasInvalid = true;
                                failureNum++;
                                continue;
                            }
                            taskList.add(entity);
                        }
                    } catch (Exception ex) {
                        log.error("????????????????????????", ex);
                        failureMsg.append(" ????????????????????????: " + list.get(i).getName() + " ???????????????<br>");
                        failureNum++;
                    }
                }
            } else {
                failureMsg.append("???????????????????????????<br>");
            }

            if (hasInvalid) {
                failureMsg.insert(0, " ?????? " + failureNum + "??????????????? ?????????????????????<br>");
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

                    // ????????????????????????????????????????????????
                    CredentialEntity credential = credentialMapper.getByName(credentialEntity.getName());
                    if (null != credential) {
                        // ????????????????????????version??????????????????????????????
                        CredentialResultRO creResult = service.get(credential.getUuid());
                        List<CredentialResultDataRO> data = creResult.getData();
                        if (CollectionUtils.isNotEmpty(data)) {
                            String version = data.get(0).getVersion();
                            service.modify(credential.getId(), credential.getUuid(), credentialEntity.getName(), "", credentialEntity.getLoginName(), loginPassword,
                                    enableUserName, credentialEntity.getEnablePassword(), version, userName, encrypt);
                        } else {
                            log.error("????????????uuid??????{}?????????", credential.getUuid());
                            throw new Exception("?????????????????????");
                        }
                    } else {
                        // ????????????
                        service.create(credentialEntity.getName(), "", credentialEntity.getLoginName(), loginPassword,
                                enableUserName, credentialEntity.getEnablePassword(), userName, encrypt);
                    }
                }
            }
        } catch (Exception e) {
            errMsg = "??????????????????????????????????????????????????????<br>";
            log.error("?????????????????????", e);
            return errMsg;
        }
        return errMsg;
    }

    @Override
    public String parseAutoRecommendExcel(MultipartFile file, String userName, List<AutoRecommendTaskVO> taskList) {
        String errMsg = null;
        if(taskList == null) {
            errMsg =  "????????????????????????????????????<br>";
            return errMsg;
        }
        try {
            int failureNum = 0;
            //?????????????????????
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
                                log.error(String.format("??????????????????%d??????", rowNum));
                                continue;
                            }

                            if(StringUtils.isEmpty(entity.getTheme())){
                                failureMsg.append("????????????????????????" + rowNum + "?????????/??????????????????<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            if (StringUtils.isBlank(entity.getTaskType())) {
                                failureMsg.append("????????????????????????" + rowNum + "????????????????????????<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            //????????????
                            int rc = entity.validation();
                            if (rc != ReturnCode.POLICY_MSG_OK) {
                                failureMsg.append("????????????????????????" + rowNum + "????????????" + ReturnCode.getMsg(rc) + "<br>");
                                log.info("??????\n" + entity.toString() + "\n?????????" + ReturnCode.getMsg(rc));
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            AutoRecommendTaskVO taskVO = new AutoRecommendTaskVO();
                            BeanUtils.copyProperties(entity, taskVO);

                            //??????????????????
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
                                errorMsg = "?????????????????????????????????????????????????????????";
                            } else if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(taskVO.getAccessType()) && StringUtils.isEmpty(taskVO.getSrcIp())) {
                                errorMsg = "?????????????????????????????????????????????";
                            } else if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(taskVO.getAccessType()) && StringUtils.isEmpty(taskVO.getDstIp())) {
                                errorMsg = "????????????????????????????????????????????????";
                            }

                            taskVO.setSrcInputType(InputTypeEnum.getCodeByDesc("???????????????"+entity.getSrcInputType()));
                            taskVO.setDstInputType(InputTypeEnum.getCodeByDesc("??????????????????"+entity.getDstInputType()));
                            if (InputTypeEnum.SRC_INPUT_TYPE_OBJECT.getCode().equals(taskVO.getSrcInputType()) && StringUtils.isNotEmpty(taskVO.getSrcIp())) {
                                rc = checkAddressObject(taskVO.getSrcIp());
                                if (rc != ReturnCode.POLICY_MSG_OK) {
                                    failureMsg.append("????????????????????????" + rowNum + "????????????" + ReturnCode.getMsg(rc) + "<br>");
                                    log.info("??????\n" + entity.toString() + "\n?????????" + ReturnCode.getMsg(rc));
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
                                    failureMsg.append("????????????????????????" + rowNum + "????????????" + ReturnCode.getMsg(rc) + "<br>");
                                    log.info("??????\n" + entity.toString() + "\n?????????" + ReturnCode.getMsg(rc));
                                    hasInvalid = true;
                                    failureNum ++;
                                    continue;
                                }
                                taskVO.setDstAddressObjectName(taskVO.getDstIp());
                                taskVO.setDstIp("");
                            }

                            if (StringUtils.isNotEmpty(errorMsg)) {
                                log.error("????????????{} ???????????????????????????{}", entity.getTheme(), errorMsg);
                                failureMsg.append("????????????????????????" + rowNum + "????????????" + errorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            AutoRecommendTaskEntity getByName = autoRecommendTaskMapper.getByName(entity.getTheme());
                            if( null != getByName ){
                                log.error("????????????{} ?????????????????????", entity.getTheme());
                                String srcErrorMsg = String.format("????????????%s??????????????????", entity.getTheme());
                                failureMsg.append("????????????????????????" + rowNum + "????????????" + srcErrorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }

                            if (themeList.contains(entity.getTheme())) {
                                log.error("????????????{} ??????????????????", entity.getTheme());
                                String srcErrorMsg = String.format("????????????%s???????????????", entity.getTheme());
                                failureMsg.append("????????????????????????" + rowNum + "????????????" + srcErrorMsg + "<br>");
                                hasInvalid = true;
                                failureNum ++;
                                continue;
                            }
                            // ???????????????????????????????????????????????????--??????????????????????????????????????????
                            taskList.add(taskVO);
                            themeList.add(entity.getTheme());
                        }
                    } catch (Exception ex) {
                        hasInvalid = true;
                        log.error("????????????????????????????????????", ex);
                        failureMsg.append(" ??????????????????????????????: " + list.get(i).getTheme() + " ???????????????<br>");
                        failureNum++;
                    }
                }
            } else {
                hasInvalid = true;
                failureMsg.append("???????????????????????????<br>");
            }

            if (hasInvalid) {
                failureMsg.insert(0, " ?????? " + failureNum + "??????????????? ?????????????????????<br>");
                errMsg = failureMsg.toString();
                return errMsg;
            }

        } catch (Exception e) {
            errMsg = "??????????????????????????????????????????????????????<br>";
            log.error("???????????????????????????", e);
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
                log.error("???????????????????????????????????????????????????????????????:ipAddressName:{}", list.toString());
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
     * ??????????????????
     *
     * @param str
     * @return
     */
    private String dealSpace(String str) {
        return StringUtils.isBlank(str) ? "" : str.trim();
    }
}
