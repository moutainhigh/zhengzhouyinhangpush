package com.abtnetworks.totems.recommend.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.config.VmwareInterfaceStatusConfig;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.PolicyCheckTypeEnum;
import com.abtnetworks.totems.common.enums.RecommendTypeEnum;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.StringUtils;
import com.abtnetworks.totems.common.utils.TotemsIp6Utils;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.external.utils.PolicyCheckCommonUtil;
import com.abtnetworks.totems.external.vo.DeviceDetailRunVO;
import com.abtnetworks.totems.external.vo.PolicyCheckDetailVO;
import com.abtnetworks.totems.external.vo.PolicyCheckListVO;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.recommend.dto.recommend.EditCommandDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.*;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.GlobalRecommendService;
import com.abtnetworks.totems.recommend.service.RecommendBussCommonService;
import com.abtnetworks.totems.recommend.service.RecommendTaskHistoryService;
import com.abtnetworks.totems.recommend.service.WhatIfService;
import com.abtnetworks.totems.recommend.task.impl.SimulationTaskServiceImpl;
import com.abtnetworks.totems.recommend.task.impl.VerifyTaskServiceImpl;
import com.abtnetworks.totems.recommend.vo.*;
import com.abtnetworks.totems.remote.dto.DredgeServiceDTO;
import com.abtnetworks.totems.remote.dto.DredgeVerifyComplianceDTO;
import com.abtnetworks.totems.remote.policy.RiskRemoteCheckService;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import com.abtnetworks.totems.whale.baseapi.service.IpServiceNameRefClient;
import com.abtnetworks.totems.whale.policy.ro.DeviceDetailRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultDataRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.log4j.Logger;
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
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_ANY;
import static com.abtnetworks.totems.common.constants.ReturnCode.BIG_INTERNET_NAT_ERROR;

/**
 * @author Administrator
 * @Title:
 * @Description: ????????????????????????????????????????????????????????????????????????????????????
 * @date 2021/1/7
 */
@Api(value = "????????????????????????")
@RestController
@RequestMapping(value = "/recommend/")
public class PolicyRecommendController extends BaseController {

    private static Logger logger = Logger.getLogger(PolicyRecommendController.class);

    private final static String  SAME_SUBNET ="????????????????????????";
    @Autowired
    GlobalRecommendService globalRecommendService;
    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    SimulationTaskServiceImpl recommendTaskManager;

    @Autowired
    VerifyTaskServiceImpl verifyTaskManager;

    @Autowired
    WhalePathAnalyzeClient client;

    @Autowired
    private IpServiceNameRefClient ipServiceNameRefClient;

    @Autowired
    private WhaleManager whaleService;

    @Autowired
    WhatIfService whatIfService;

    @Autowired
    RecommendTaskHistoryService recommendTaskHistoryService;

    @Autowired
    ExcelParser excelParser;

    @Autowired
    LogClientSimple logClientSimple;

    @Resource
    RecommendBussCommonService recommendBussCommonService;

    @Autowired
    VmwareInterfaceStatusConfig vmwareInterfaceStatusConfig;

    @Resource
    RemoteBranchService remoteBranchService;

    @Resource
    RiskRemoteCheckService riskRemoteCheckService;

    @PostMapping("task/addAll")
    public JSONObject addRecommendTaskAll(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        int rc = ReturnCode.POLICY_MSG_OK;
        JSONObject returnJSON = new JSONObject();
        Integer  taskId;
        if (RecommendTypeEnum.IN_2OUT_RECOMMEND.getTypeCode() == entity.getTaskType() || RecommendTypeEnum.OUT_2IN_RECOMMEND.getTypeCode() == entity.getTaskType()) {
            logger.info("????????????????????????Start??????{}" + JSONObject.toJSONString(entity));
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
                String message = String.format("?????????????????????%s ??????", recommendTaskEntity.getTheme());
                logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
                List<Integer> collect = list.stream().map(r -> r.getId()).collect(Collectors.toList());
                taskId = collect.get(0);
                returnJSON = getReturnJSON(rc, "");
                returnJSON.put("taskId", taskId);
            } catch (IllegalArgumentException e) {
                rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
                logger.error("????????????????????????End????????????", e);
                return getReturnJSON(rc);
            }
        } else {
            logger.info("??????????????????????????????Start??????{}" + JSONObject.toJSONString(entity));
            returnJSON = addRecommendTask(entity, auth);
            taskId = (Integer) returnJSON.get("taskId");
        }
        //??????
        if(taskId != null){
            logger.info("======================????????????============================");
            String id = String.valueOf(taskId);
            startRecommendTaskList(id,auth);
            logger.info("======================????????????============================");
        }
        return returnJSON;
    }

    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "??????????????????id", required = true, dataType = "Integer")
    })
    @PostMapping("task/gettaskstatus")
    public JSONObject getTaskStatus(int taskId) {

        TaskStatusVO taskStatusVO = policyRecommendTaskService.getTaskStatusByTaskId(taskId);

        String jsonObjectString = JSONObject.toJSONString(taskStatusVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "??????????????????id", required = true, dataType = "String")
    })
    @PostMapping("task/getcompliance")
    public TotemsReturnT getTaskCompliance(String taskId) {
        int taskIdNum = 0;
        try {
            taskIdNum = Integer.valueOf(taskId);
            RecommendTaskEntity recommendTaskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskIdNum);
            DredgeVerifyComplianceDTO dto = new DredgeVerifyComplianceDTO();
            BeanUtils.copyProperties(recommendTaskEntity,dto);
            List<ComplianceRulesMatrixVO> complianceRulesMatrixVOS = new ArrayList<>();
            if (TotemsIp6Utils.isIp6(recommendTaskEntity.getSrcIp()) || TotemsIp6Utils.isIp6Range(recommendTaskEntity.getSrcIp()) || TotemsIp6Utils.isIp6Mask(recommendTaskEntity.getSrcIp()) ||
                    TotemsIp6Utils.isIp6(recommendTaskEntity.getDstIp()) || TotemsIp6Utils.isIp6Range(recommendTaskEntity.getDstIp()) || TotemsIp6Utils.isIp6Mask(recommendTaskEntity.getDstIp())){
                return new TotemsReturnT(complianceRulesMatrixVOS);
            }

            List<ServiceDTO> serviceDTOS = (List<ServiceDTO>)JSONObject.parseArray(recommendTaskEntity.getServiceList(), ServiceDTO.class);
            List<DredgeServiceDTO> serviceList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(serviceDTOS)){
                for (ServiceDTO serviceDTO : serviceDTOS) {
                    DredgeServiceDTO dredgeServiceDTO = new DredgeServiceDTO();
                    if (POLICY_NUM_VALUE_ANY.equals(dredgeServiceDTO.getProtocol())){
                        //?????????,?????????any
                    }else {
                        dredgeServiceDTO.setProtocol(serviceDTO.getProtocol().equals("58") ? "1" : serviceDTO.getProtocol());
                        dredgeServiceDTO.setDstPorts(serviceDTO.getDstPorts());
                    }
                    serviceList.add(dredgeServiceDTO);
                }
            }
            dto.setServiceList(serviceList);

            complianceRulesMatrixVOS = riskRemoteCheckService.remoteRiskVerifyCompliance(dto);
            return new TotemsReturnT(complianceRulesMatrixVOS);

        } catch (Exception e) {
            logger.error("??????????????????????????????,????????????:{}", e);
            return new TotemsReturnT("-1", e.getMessage());
        }

    }

    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "??????????????????id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "Integer")
    })
    @PostMapping("task/analyzepathinfolist")
    public JSONObject getAnalyzePathInfoList(int taskId, int page, int psize) {

        PageInfo<PathInfoEntity> pageInfo = policyRecommendTaskService.getAnalyzePathInfoVOList(taskId, page, psize);

        List<PathInfoEntity> list = pageInfo.getList();
        for (PathInfoEntity entity : list) {
            String srcSubnetDevices = getSubnetDeviceList(entity.getSrcNodeUuid());
            entity.setSrcSubnetDevices(srcSubnetDevices);
            String dstSubnetDevices = getSubnetDeviceList(entity.getDstNodeUuid());
            entity.setDstSubnetDevices(dstSubnetDevices);
        }

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "??????????????????id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "??????", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "????????????", required = true, dataType = "Integer")
    })
    @PostMapping("task/verifypathinfolist")
    public JSONObject getVerifyPathInfoList(int taskId, int page, int psize) {

        PageInfo<PathInfoEntity> pageInfo = policyRecommendTaskService.getVerifyPathInfoVOList(taskId, page, psize);

        List<PathInfoEntity> list = pageInfo.getList();
        for (PathInfoEntity entity : list) {
            String srcSubnetDevices = getSubnetDeviceList(entity.getSrcNodeUuid());
            entity.setSrcSubnetDevices(srcSubnetDevices);
            String dstSubnetDevices = getSubnetDeviceList(entity.getDstNodeUuid());
            entity.setDstSubnetDevices(dstSubnetDevices);
        }

        String jsonObjectString = JSONObject.toJSONString(pageInfo);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "????????????id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "isVerifyData", value = "???????????????????????????", required = true, dataType = "Boolean")
    })
    @PostMapping("task/pathdetail")
    public JSONObject getPathDetail(int pathInfoId, boolean isVerifyData) {

        PathDetailVO pathInfoVO = policyRecommendTaskService.getPathDetail(pathInfoId, isVerifyData);

        String jsonObjectString = JSONObject.toJSONString(pathInfoVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "????????????id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "??????uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "isVerifyData", value = "???????????????????????????", required = true, dataType = "Boolean"),
            @ApiImplicitParam(paramType = "query", name = "pathIndex", value = "????????????", required = true, dataType = "String")
    })
    @PostMapping("task/devicedetail")
    public JSONObject getDeviceDetail(int pathInfoId, String deviceUuid, boolean isVerifyData, String pathIndex) {
        DevicePolicyVO devicePolicyVO = new DevicePolicyVO();
        if (pathIndex == null) {
            pathIndex = "0";
        }
        PathDeviceDetailEntity entity = policyRecommendTaskService.getDevieceDetail(pathInfoId, deviceUuid, isVerifyData, pathIndex);

        boolean isEmptyData = false;
        String deviceDetail = null;
        if (entity == null) {
            logger.error(String.format("????????????????????????????????????...?????????%d, ?????????%s", pathInfoId, deviceUuid));
            isEmptyData = true;
        } else {
            deviceDetail = entity.getDeviceDetail();
            if (deviceDetail == null) {
                logger.error(String.format("???????????????????????????????????????...?????????%d????????????%s", pathInfoId, deviceUuid));
                isEmptyData = true;
            }
        }

        if (isEmptyData) {
            String jsonObjectString = JSONObject.toJSONString(devicePolicyVO);
            JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);
            return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
        }

        JSONObject deviceDetailObject = JSONObject.parseObject(deviceDetail);
        DeviceDetailRO detailDeviceRO = deviceDetailObject.toJavaObject(DeviceDetailRO.class);
        DeviceDetailRunVO deviceDetailRunVO = client.parseDetailRunRO(detailDeviceRO);

        List<PolicyDetailVO> safeListDetail = deviceDetailRunVO.getSafeList();
        List<PolicyDetailVO> natListDetail = deviceDetailRunVO.getNatList();
        List<PolicyDetailVO> routTableListDetail = deviceDetailRunVO.getRoutList();

        devicePolicyVO.setAclList(deviceDetailRunVO.getAclList());
        devicePolicyVO.setPolicyRoutList(deviceDetailRunVO.getPolicyRoutList());

        if (safeListDetail != null) {
            List<PolicyRecommendSecurityPolicyVO> securityPolicyVOList = new ArrayList<>();
            for (PolicyDetailVO vo : safeListDetail) {
                PolicyRecommendSecurityPolicyVO policyVO = new PolicyRecommendSecurityPolicyVO();
                BeanUtils.copyProperties(vo, policyVO);
                policyVO.setIsAble(vo.getIsAble());
                policyVO.setDescription(vo.getDescription());
                securityPolicyVOList.add(policyVO);
            }
            devicePolicyVO.setSecurityPolicyList(securityPolicyVOList);
        }

        if (natListDetail != null) {
            List<PolicyRecommendNatPolicyVO> natPolicyVOlist = new ArrayList<>();
            for (PolicyDetailVO vo : natListDetail) {
                PolicyRecommendNatPolicyVO natPolicyVO = new PolicyRecommendNatPolicyVO();
                BeanUtils.copyProperties(vo, natPolicyVO);
                natPolicyVO.setDescription(vo.getDescription());
                natPolicyVO.setIsAble(vo.getIsAble());
                natPolicyVOlist.add(natPolicyVO);
            }
            devicePolicyVO.setNatPolicList(natPolicyVOlist);
        }

        if (routTableListDetail != null) {
            List<PolicyRecommendPolicyRouterVO> routerVOList = new ArrayList<>();
            for (PolicyDetailVO vo : routTableListDetail) {
                PolicyRecommendPolicyRouterVO routerVO = new PolicyRecommendPolicyRouterVO();
                routerVO.setNumber(vo.getNumber());
                routerVO.setSrcIp(vo.getSrcIp());
                routerVO.setMask(vo.getMask());
                routerVO.setNextHop(vo.getNextStep());
                routerVO.setNetDoor(vo.getNetDoor());
                routerVO.setDistance(vo.getDistance());
                routerVO.setWeight(vo.getWeight());
                routerVO.setProtocol(vo.getProtocol());
                routerVO.setDescription(vo.getDescription());
                routerVOList.add(routerVO);
            }
            devicePolicyVO.setRouterList(routerVOList);
        }

        String jsonObjectString = JSONObject.toJSONString(devicePolicyVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "????????????id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getrisk")
    public JSONObject getRiskByPathInfoId(int pathInfoId) {
        List<PolicyRiskEntity> riskEntityList = policyRecommendTaskService.getRiskByPathInfoId(pathInfoId);

        List<RiskRuleInfoEntity> riskInfoList = new ArrayList<>();
        for (PolicyRiskEntity riskEntity : riskEntityList) {
            RiskRuleInfoEntity riskInfo = policyRecommendTaskService.getRiskInfoByRuleId(riskEntity.getRuleId());
            riskInfoList.add(riskInfo);
        }
        String jsonObjectString = JSONObject.toJSONString(riskInfoList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }

    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "????????????id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getpolicy")
    public JSONObject getPolicyByPathInfoId(int pathInfoId) {
        List<RecommendPolicyVO> policyVOList = policyRecommendTaskService.getPolicyByPathInfoId(pathInfoId);

        String jsonObjectString = JSONObject.toJSONString(policyVOList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }

    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "????????????id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getmergedpolicy")
    public JSONObject getMergedPolicyByTaskId(int taskId) {
        List<RecommendPolicyVO> policyVOList = policyRecommendTaskService.getMergedPolicyByTaskId(taskId);

        String jsonObjectString = JSONObject.toJSONString(policyVOList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }


    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "??????id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getcheckresult")
    public JSONObject getCheckResultByPolicyId(@Param("policyId") int taskId) {

        //?????????????????????????????????????????????????????????????????????????????????taskId??????
        List<CheckResultEntity> entityList = policyRecommendTaskService.getCheckResultByPolicyId(taskId);

        if (entityList.size() == 0) {
            return getReturnJSON(ReturnCode.POLICY_MSG_OK);
        }

        Map<String, List<RuleCheckResultDataRO>> deviceMergeListMap = new HashMap<>();
        Map<String, List<RuleCheckResultDataRO>> deviceHideListMap = new HashMap<>();
        Map<String, List<RuleCheckResultDataRO>> deviceRedundancyListMap = new HashMap<>();

        for (CheckResultEntity entity : entityList) {
            RecommendPolicyEntity recommendPolicyEntity = policyRecommendTaskService.getPolicyByPolicyId(entity.getPolicyId());

            if (recommendPolicyEntity == null) {
                logger.info(String.format("??????(%d)?????????????????????...", entity.getPolicyId()));
                continue;
            }
            String deviceUuid = recommendPolicyEntity.getDeviceUuid();

            String checkResult = entity.getCheckResult();
            JSONObject checkResultObject = JSONObject.parseObject(checkResult);
            RuleCheckResultRO checkResultRO = checkResultObject.toJavaObject(RuleCheckResultRO.class);
            List<RuleCheckResultDataRO> ruleCheckResultDataROList = checkResultRO.getData();

            for (RuleCheckResultDataRO data : ruleCheckResultDataROList) {
                if (data.getBpcCode().equals(PolicyConstants.POLICY_STRING_HIDDEN_POLICY) || data.getBpcCode().equals(PolicyConstants.POLICY_STRING_HIDDEN_POLICY_RC_HIDDEN_SAME) || data.getBpcCode().equals(PolicyConstants.POLICY_STRING_HIDDEN_POLICY_RC_HIDDEN_CONFLICT)) {
                    ipServiceNameRefClient.packRuleObject(deviceUuid, data.getPrimaryRule(), data.getOtherPrimaryRules(), data.getRelatedRules());
                    List<RuleCheckResultDataRO> hideList = deviceHideListMap.get(deviceUuid);
                    if (hideList == null) {
                        hideList = new ArrayList<>();
                        deviceHideListMap.put(deviceUuid, hideList);
                    }
                    hideList.add(data);
                } else if (data.getBpcCode().equals(PolicyConstants.POLICY_STRING_MERGE_POLICY)) {
                    ipServiceNameRefClient.packRuleObject(deviceUuid, data.getPrimaryRule(), data.getOtherPrimaryRules(), data.getRelatedRules());
                    List<RuleCheckResultDataRO> mergeList = deviceMergeListMap.get(deviceUuid);
                    if (mergeList == null) {
                        mergeList = new ArrayList<>();
                        deviceMergeListMap.put(deviceUuid, mergeList);
                    }
                    mergeList.add(data);
                } else if (data.getBpcCode().equals(PolicyConstants.POLICY_STRING_REDUNDANCY_POLICY)) {
                    ipServiceNameRefClient.packRuleObject(deviceUuid, data.getPrimaryRule(), data.getOtherPrimaryRules(), data.getRelatedRules());
                    List<RuleCheckResultDataRO> redundancyList = deviceRedundancyListMap.get(deviceUuid);
                    if (redundancyList == null) {
                        redundancyList = new ArrayList<>();
                        deviceRedundancyListMap.put(deviceUuid, redundancyList);
                    }
                    redundancyList.add(data);
                } else {
                    logger.error("invalid data bpc type: " + data.getBpcCode());
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        Set<String> deviceSet = new HashSet<>();
        Set<String> hideDeviceSet = deviceHideListMap.keySet();
        Set<String> mergeDeviceSet = deviceMergeListMap.keySet();
        Set<String> redundancyDeviceSet = deviceRedundancyListMap.keySet();

        deviceSet.addAll(hideDeviceSet);
        deviceSet.addAll(mergeDeviceSet);
        deviceSet.addAll(redundancyDeviceSet);
        for (String deviceUuid : deviceSet) {
            List<RuleCheckResultDataRO> hideList = deviceHideListMap.get(deviceUuid);
            if (hideList == null) {
                hideList = new ArrayList<>();
            }
            PolicyCheckVO policyResultList = new PolicyCheckVO();
            ResultRO<List<RuleCheckResultDataRO>> hideCheckResultRO = new ResultRO<>();
            hideCheckResultRO.setData(hideList);
            logger.debug("hideCheckResultRO.setData:" + hideList.toString());

            List<PolicyCheckListVO> checkListVO = PolicyCheckCommonUtil.getCheckList(hideCheckResultRO, PolicyCheckTypeEnum.HIDDEN);
            if (checkListVO != null && checkListVO.size() > 0) {
                policyResultList.setHiddenPolicy(checkListVO);

                /*List<PolicyCheckDetailVO> hideDetailList = new ArrayList<>();
                for (PolicyCheckListVO policyCheckListVO : checkListVO) {
                    hideDetailList.addAll(policyCheckListVO.getDetailList());
                }

                if (hideDetailList != null && hideDetailList.size() != 0) {
                    List<PolicyRecommendHiddenSecurityPolicyVO> hiddenPolicyList = new ArrayList<>();
                    for (PolicyCheckDetailVO vo : hideDetailList) {
                        PolicyRecommendHiddenSecurityPolicyVO hidden = new PolicyRecommendHiddenSecurityPolicyVO();
                        BeanUtils.copyProperties(vo, hidden);
                        hidden.setPolicyId(vo.getPolicyId());
                        hidden.setLineNumber(vo.getLineNum());
                        hidden.setSaveIdleTimeout(vo.getIdleTimeout());
                        hiddenPolicyList.add(hidden);
                    }
                    policyResultList.setHiddenPolicy(hiddenPolicyList);
                }*/
            }

            List<RuleCheckResultDataRO> mergeList = deviceMergeListMap.get(deviceUuid);
            if (mergeList == null) {
                mergeList = new ArrayList<>();
            }
            ResultRO<List<RuleCheckResultDataRO>> mergeCheckResultRO = new ResultRO<>();
            mergeCheckResultRO.setData(mergeList);
            checkListVO = PolicyCheckCommonUtil.getCheckList(mergeCheckResultRO, PolicyCheckTypeEnum.MERGE);
            if (checkListVO != null && checkListVO.size() > 0) {
                policyResultList.setMergePolicy(checkListVO);
                /*List<PolicyCheckDetailVO> mergeDetailList = checkListVO.get(0).getDetailList();

                if (mergeDetailList != null && mergeDetailList.size() != 0) {
                    List<PolicyRecommendMergePolicyVO> mergePolicyList = new ArrayList<>();
                    for (PolicyCheckDetailVO vo : mergeDetailList) {
                        PolicyRecommendMergePolicyVO merge = new PolicyRecommendMergePolicyVO();
                        BeanUtils.copyProperties(vo, merge);
                        merge.setPolicyId(vo.getPolicyId());
                        merge.setLineNumber(vo.getLineNum());
                        merge.setSaveIdleTimeout(vo.getIdleTimeout());
                        mergePolicyList.add(merge);
                    }
                    policyResultList.setMergePolicy(mergePolicyList);
                }*/
            }

            List<RuleCheckResultDataRO> redundancyList = deviceRedundancyListMap.get(deviceUuid);
            if (redundancyList == null) {
                redundancyList = new ArrayList<>();
            }
            ResultRO<List<RuleCheckResultDataRO>> redundancyCheckResultRO = new ResultRO<>();
            redundancyCheckResultRO.setData(redundancyList);
            logger.debug("hideCheckResultRO.setData:" + redundancyList.toString());

            checkListVO = PolicyCheckCommonUtil.getCheckList(redundancyCheckResultRO, PolicyCheckTypeEnum.REDUNDANCY);
            if (checkListVO != null && checkListVO.size() > 0) {
                policyResultList.setRedundancyPolicy(checkListVO);

                /*List<PolicyCheckDetailVO> redundancyDetailList = new ArrayList<>();
                for (PolicyCheckListVO policyCheckListVO : checkListVO) {
                    redundancyDetailList.addAll(policyCheckListVO.getDetailList());
                }

                if (redundancyDetailList != null && redundancyDetailList.size() != 0) {
                    List<PolicyRecommendHiddenSecurityPolicyVO> redundancyPolicyList = new ArrayList<>();
                    for (PolicyCheckDetailVO vo : redundancyDetailList) {
                        PolicyRecommendHiddenSecurityPolicyVO redundancy = new PolicyRecommendHiddenSecurityPolicyVO();
                        BeanUtils.copyProperties(vo, redundancy);
                        redundancy.setPolicyId(vo.getPolicyId());
                        redundancy.setLineNumber(vo.getLineNum());
                        redundancy.setSaveIdleTimeout(vo.getIdleTimeout());
                        redundancyPolicyList.add(redundancy);
                    }
                    policyResultList.setRedundancyPolicy(redundancyPolicyList);
                }*/
            }

            String jsonObjectString = JSONObject.toJSONString(policyResultList);
            JSONObject object = JSONObject.parseObject(jsonObjectString);
            jsonObject.put(deviceUuid, object);
        }

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("new ?????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "??????????????????id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getcommand")
    public JSONObject getCommand(int taskId) {
        List<CommandVO> commandVOList = commandTaskManager.getCommandByTaskId(taskId);

        String jsonObjectString = JSONObject.toJSONString(commandVOList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }

    @ApiOperation("new ?????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "??????????????????id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "??????uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "command", value = "?????????", required = true, dataType = "String")
    })
    @PostMapping("task/editcommand.action")
    public JSONObject editCommand(@RequestBody EditCommandDTO editCommandDTO, Authentication auth) {
        List<CommandTaskEditableEntity> entityList = commandTaskManager.getCommandTaskByTaskId(editCommandDTO.getTaskId());

        if (entityList == null || entityList.size() == 0) {
            return getReturnJSON(ReturnCode.TASK_IS_DELETED);
        }

        if (entityList.get(0).getStatus() > PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START) {
            return getReturnJSON(ReturnCode.INVALID_COMMANDLINE_STATUS);
        }

        int rc = commandTaskManager.editCommandEditableEntity(editCommandDTO, auth.getName());
        CommandTaskEditableEntity entity = commandTaskManager.getCommandEditableEntityByTaskIdAndDeviceUuid(editCommandDTO.getTaskId(), editCommandDTO.getDeviceUuid());

        String jsonObjectString = JSONObject.toJSONString(entity);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(rc, jsonObject);
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id??????", required = true, dataType = "String")
    })
    @PostMapping("recommend/startGlobalRecommendTaskList")
    public JSONObject startGlobalRecommendTaskList(String ids,Authentication authentication) {
        if(!vmwareInterfaceStatusConfig.isVmInterfaceAvailable()){
            return startRecommendTaskList(ids,authentication);
        }
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }
        String errmsg = "";
        try{
            errmsg = globalRecommendService.startGlobalRecommendTaskList(ids,authentication);
            return getReturnJSON(ReturnCode.POLICY_MSG_OK, errmsg);
        }catch (Exception e){
            return getReturnJSON(ReturnCode.FAILED, e.getMessage());
        }
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id??????", required = true, dataType = "String")
    })
    @PostMapping("recommend/start")
    public JSONObject startRecommendTaskList(String ids,Authentication authentication) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
        }

        List<String> themeList = new ArrayList<>();
        List<RecommendTaskEntity> taskEntitylist = new ArrayList<>();
        for (int id : idList) {
            logger.info(String.format("????????????(%d)", id));
            RecommendTaskEntity entity = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            if (entity == null) {
                logger.error(String.format("????????????(%d)??????, ???????????????, ?????????????????????...", id));
            }else if (entity.getTaskType()==PolicyConstants.IN2IN_INTERNET_RECOMMEND) {
                //????????????????????????
                return getReturnJSON(ReturnCode.POLICY_MSG_OK, "?????????????????????");
            }
            else if (entity.getStatus() > PolicyConstants.POLICY_INT_TASK_TYPE_FRESH) {
                logger.error(String.format("??????????????????(%s), ????????????????????????\n", entity.getOrderNumber()));
            } else {
                taskEntitylist.add(entity);
                themeList.add(entity.getTheme());
            }
        }

        List<SimulationTaskDTO> taskDtoList = new ArrayList<>();
        for (RecommendTaskEntity taskEntity : taskEntitylist) {
            SimulationTaskDTO taskDTO = new SimulationTaskDTO();
            BeanUtils.copyProperties(taskEntity, taskDTO);
            taskDTO.setWhatIfCaseUuid(taskEntity.getWhatIfCase());

            //??????????????????
            if (taskEntity.getServiceList() == null) {
                taskDTO.setServiceList(null);
            } else {
                JSONArray array = JSONArray.parseArray(taskEntity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                taskDTO.setServiceList(serviceList);
            }
            WhatIfRO whatIf = recommendBussCommonService.createWhatIfCaseUuid(taskEntity);
            if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
                logger.info("????????????????????????UUID???:" + whatIf.getUuid());
                taskDTO.setWhatIfCaseUuid(whatIf.getUuid());
                taskDTO.setDeviceWhatifs(whatIf.getDeviceWhatifs());
            } else {
                logger.error("?????????????????????????????????" + taskEntity.getRelevancyNat());
            }
            taskDtoList.add(taskDTO);
            policyRecommendTaskService.updateTaskStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE);
        }

        recommendTaskManager.addSimulationTaskList(taskDtoList, authentication);
        String message = String.format("?????????%s ????????????", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        String errmsg = String.format("%d?????????????????????????????????????????????\n", taskEntitylist.size());
        if (taskEntitylist.size() == 0) {
            errmsg = "???????????????????????????????????????????????????";
        }

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errmsg);
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id??????", required = true, dataType = "String")
    })
    @PostMapping("recommend/restart")
    public JSONObject restartRecommendTaskList(String ids,Authentication authentication) {
        String idString = ids;
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
        }

        //?????????????????????????????????????????????????????????????????????????????????????????????
        StringBuilder errMsg = new StringBuilder();
        int rc = ReturnCode.POLICY_MSG_OK;
        List<String> themeList = new ArrayList<>();
        for (int id : idList) {
            logger.info(String.format("????????????(%d)", id));
            RecommendTaskEntity taskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            List<CommandTaskEditableEntity> taskEntityList = commandTaskManager.getCommandTaskByTaskId(id);
            int taskStatus = taskEntity.getStatus();
            if (taskEntityList != null && taskEntityList.size() > 0){
                themeList.add(taskEntity.getTheme());
                if (taskStatus == PolicyConstants.POLICY_INT_STATUS_INITIAL ||
                        taskStatus == PolicyConstants.POLICY_INT_STATUS_SIMULATING ||
                        taskStatus == PolicyConstants.POLICY_INT_STATUS_VERIFYING ||
                        taskEntityList.get(0).getPushStatus() == PolicyConstants.PUSH_STATUS_PUSHING ||
                        taskEntityList.get(0).getRevertStatus() == PolicyConstants.REVERT_STATUS_REVERTING) {
                    rc = ReturnCode.RESTART_PUSH_OR_REVERT_RUNNING;
                    errMsg.append(taskEntity.getTheme() + ":"+ReturnCode.getMsg(rc));
                }
            }else {
                themeList.add(taskEntity.getTheme());
                if (taskStatus == PolicyConstants.POLICY_INT_STATUS_INITIAL ||
                        taskStatus == PolicyConstants.POLICY_INT_STATUS_SIMULATING ||
                        taskStatus == PolicyConstants.POLICY_INT_STATUS_VERIFYING) {
                    rc = ReturnCode.RESTART_PUSH_OR_REVERT_RUNNING;
                    errMsg.append(taskEntity.getTheme() + ":" + ReturnCode.getMsg(rc));
                }
            }
        }
        if (rc != ReturnCode.POLICY_MSG_OK) {
            return getReturnJSON(rc, errMsg.toString());
        }
        int type = 1;
        //????????????????????????
        policyRecommendTaskService.deleteTasks(idList,type);
        //????????????
        JSONObject jsonObject = startRecommendTaskList(idString, authentication);

        return jsonObject;

    }

    @ApiOperation("?????????????????????????????????")
    @PostMapping("task/newstart")
    public JSONObject addAndStartRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        JSONObject jsonObject = addRecommendTask(entity, auth);

        if (jsonObject.getString("status").equals("0")) {
            String taskId = jsonObject.getString("errmsg");
            startRecommendTaskList(taskId, auth);
        }

        return jsonObject;
    }


    @PostMapping("task/addGlobalRecommendTask")
    public JSONObject addGlobalRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        if (!vmwareInterfaceStatusConfig.checkVmInterfaceAvailableNow() || entity.getIpType() != 0) {
            //vmware??????????????????????????????
            return addRecommendTask(entity, auth);
        }
        // ????????????
        entity.setDstIp(InputValueUtils.formatIpAddress(entity.getDstIp()));
        int rc = recommendBussCommonService.checkParamForSrcAddress(entity);
        List<ServiceDTO> serviceList = entity.getServiceList();
        Set<String> serviceSet = new HashSet<>();
        for (ServiceDTO serviceDTO : serviceList) {
            if (serviceSet.contains(serviceDTO.getProtocol())) {
                return getReturnJSON(ReturnCode.FAILED, "?????????????????????????????????????????????");
            } else {
                serviceSet.add(serviceDTO.getProtocol());
            }
        }

        for (ServiceDTO service : serviceList) {
            if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
            }
        }
        RecommendTaskEntity recommendTaskEntity = null;
        try {
            //??????/??????
            recommendTaskEntity = globalRecommendService.addGlobalRecommendTask(entity, auth);
        } catch (IssuedExecutorException e) {
            return getReturnJSON(ReturnCode.FAILED, e.getMessage());
        }
        String message = String.format("???????????????????????????%s ??????", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        JSONObject rs = getReturnJSON(rc, "");
        rs.put("taskId", recommendTaskEntity.getId());
        return rs;
    }

    @ApiOperation("new ????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "???IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "??????IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "?????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "??????????????????", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "??????????????????", required = false, dataType = "Long"),
    })
    @PostMapping("task/add")
    public JSONObject addRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        //??????IP???????????????????????????????????????????????????????????? ??????ipv6??????????????????????????????????????????
        //int  rc =  recommendBussCommonService.checkParamForDstAddress(entity);
        //????????????????????????????????????????????????????????????????????????
        entity.setDstIp(InputValueUtils.formatIpAddress(entity.getDstIp()));
        int rc = recommendBussCommonService.checkParamForSrcAddress(entity);
        List<ServiceDTO> serviceList = entity.getServiceList();
        Set<String> serviceSet = new HashSet<>();
        for (ServiceDTO serviceDTO : serviceList) {
            if (serviceSet.contains(serviceDTO.getProtocol())) {
                return getReturnJSON(ReturnCode.FAILED, "?????????????????????????????????????????????");
            } else {
                serviceSet.add(serviceDTO.getProtocol());
            }
        }
        logger.info("???????????????whatIfCases is " + entity.getWhatIfCases());


        for (ServiceDTO service : serviceList) {
            if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
            }
        }
        //??????????????????
        RecommendTaskEntity recommendTaskEntity = null;
        try {
            recommendTaskEntity = recommendBussCommonService.addAutoNatGenerate(entity, auth);
        } catch (IssuedExecutorException e) {
            return getReturnJSON(ReturnCode.FAILED, e.getMessage());
        }
        String message = String.format("???????????????????????????%s ??????", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        JSONObject rs = getReturnJSON(rc, "");
        rs.put("taskId", recommendTaskEntity.getId());
        return rs;
    }



    @ApiOperation("??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id??????", required = true, dataType = "String")
    })
    @PostMapping("recommend/stop")
    public JSONObject stopRecommendTaskList(String ids) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
        }

        Map<String, String> themeMap = new HashMap<>();
        List<String> taskList = new ArrayList<>();
        StringBuilder errMsg = new StringBuilder();

        boolean hasFailed = false;
        for (int id : idList) {
            logger.info(String.format("????????????(%d)", id));
            RecommendTaskEntity entity = policyRecommendTaskService.getRecommendTaskByTaskId(id);

            if (entity == null) {
                logger.error(String.format("????????????(%d)??????, ???????????????, ?????????????????????...", id));
                hasFailed = true;
            } else if (entity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE ||
                    entity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATING) {
                errMsg.append(String.format("??????????????????(%s), ???????????????????????????????????????\n", entity.getTheme()));
                hasFailed = true;
            } else {
                taskList.add(String.valueOf(id));
                themeMap.put(String.valueOf(id), entity.getTheme());
            }
        }

        if (taskList.size() == 0) {
            logger.error("???????????????????????????");
            errMsg.insert(0, "???????????????????????????");
            return getReturnJSON(ReturnCode.NO_RECOMMEND_TASK_ENTITY_GET, "???????????????????????????");
        }

        String message = String.format("?????????%s ????????????", org.apache.commons.lang3.StringUtils.join(themeMap.values(), ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        List<String> failedList = recommendTaskManager.stopTaskList(taskList);

        for (String id : taskList) {
            if (failedList.contains(id)) {
                logger.info(String.format("???????????????%s?????????", id));
                errMsg.append(String.format("???????????????%s?????????", id));
                hasFailed = true;
                logClientSimple.addBusinessLog(LogLevel.ERROR.getId(), BusinessLogType.POLICY_PUSH.getId(), String.format("????????????: %s ??????", themeMap.get(id)));
                continue;
            }
            Integer idNum = 0;
            try {
                idNum = Integer.valueOf(id);
            } catch (Exception e) {
                logger.error(String.format("????????????id???????????????id???(%s)", id));
            }
            policyRecommendTaskService.updateTaskStatus(idNum, PolicyConstants.POLICY_INT_STATUS_STOPPED);
        }
        String msg = "?????????????????????????????????";
        int code = ReturnCode.POLICY_MSG_OK;
        if (hasFailed) {
            errMsg.insert(0, "?????????????????????????????????");
            code = ReturnCode.INVALID_TASK_ID;
            msg = errMsg.toString();
        }

        return getReturnJSON(code, msg);
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "??????????????????id", required = true, dataType = "String")
    })
    @PostMapping("verify/startverify")
    public JSONObject startVerify(String ids) {
        logger.info(String.format("??????????????????[%s]... ", ids));

        if (verifyTaskManager.isVerifyRunning()) {
            return getReturnJSON(ReturnCode.VERIFY_TASK_IS_RUNNING);
        }

        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("????????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }
        StringBuilder sb = new StringBuilder();

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
        }

        List<String> themeList = new ArrayList<>();
        List<RecommendTaskEntity> taskList = new ArrayList<>();
        for (Integer id : idList) {
            RecommendTaskEntity task = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            WhatIfRO whatIf = recommendBussCommonService.createWhatIfCaseUuid(task);
            if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
                logger.info("????????????????????????UUID???:" + whatIf.getUuid());
                task.setWhatIfCase(whatIf.getUuid());
            } else {
                logger.error("?????????????????????????????????" + task.getRelevancyNat());
            }
            taskList.add(task);
            sb.append(String.format(",%s[%s]???????????????\n", task.getTheme(), task.getOrderNumber()));
            themeList.add(task.getTheme());
        }
        String msg = sb.toString().replaceFirst(",", "");

        logger.info(msg);
        int rc = verifyTaskManager.startVerify(taskList);
        String message = String.format("?????????%s ????????????", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return getReturnJSON(rc, "????????????");
    }



    @ApiOperation("new ??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoIds", value = "????????????id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "enable", value = "????????????(0:diable,1:enable)", required = true, dataType = "String")
    })
    @PostMapping("task/enablepath")
    public JSONObject enablePath(String pathInfoIds, String enable,Authentication authentication) {
        pathInfoIds = String.format("[%s]", pathInfoIds);
        JSONArray jsonArray = JSONArray.parseArray(pathInfoIds);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("?????????????????????????????????");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(pathInfoIds);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);
        }

        PathInfoEntity entity = policyRecommendTaskService.getPathInfoByPathInfoId(idList.get(0));

        int taskId = entity.getTaskId();
        RecommendTaskEntity taskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskId);
        if (taskEntity.getStatus() > PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE) {
            return getReturnJSON(ReturnCode.INVALID_TASK_STATUS);
        }

        for (Integer pathInfoId : idList) {
            policyRecommendTaskService.setPathEnable(pathInfoId, enable);
        }

        String message = String.format("???????????????%s ?????????id: %s????????????????????? %s", taskEntity.getTheme(), pathInfoIds, enable.equals("0") ? "??????" : "??????");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        SimulationTaskDTO task = new SimulationTaskDTO();
        BeanUtils.copyProperties(taskEntity, task);
        //??????????????????
        if (taskEntity.getServiceList() == null) {
            task.setServiceList(null);
        } else {
            JSONArray array = JSONArray.parseArray(taskEntity.getServiceList());
            List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
            task.setServiceList(serviceList);
        }

        int rc = recommendTaskManager.addReassembleCommandLineTask(task,authentication);

        return getReturnJSON(rc);
    }



    @ApiOperation("new ??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "??????????????????id", required = true, dataType = "String")
    })
    @PostMapping("task/getpathstatic")
    public JSONObject getPathStaticByTaskId(String taskId) {
        int taskIdNum = 0;
        try {
            taskIdNum = Integer.valueOf(taskId);
        } catch (Exception e) {
            getReturnJSON(ReturnCode.FAILED, "??????????????????!");
        }

        RecommendTaskEntity taskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskIdNum);
        if (taskEntity.getStatus().equals(PolicyConstants.POLICY_INT_STATUS_SIMULATION_NOT_STARTED)) {
            return getReturnJSON(ReturnCode.POLICY_MSG_OK, "?????????????????????");
        }
        JSONObject object = new JSONObject();
        List<PathInfoEntity> list = policyRecommendTaskService.getPathInfoByTaskId(taskIdNum);
        if (list.size() == 0) {
            //KSH-5006
            object.put("danger", "???????????????");
            getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
        }

        int success = 0;
        int failed = 0;
        int notStart = 0;
        int error = 0;
        int access = 0;
        int noSrcSubnet = 0;
        int noDstSubnet = 0;
        int pathNoExists = 0;
        int srcDstHasSameSubnet = 0;
        int bigInternetNatExits = 0;
        String message = "";
        for (PathInfoEntity entity : list) {
            // ???????????????????????????????????????????????????????????????????????????message
            PathDetailVO pathDetailVO = policyRecommendTaskService.getPathDetail(entity.getId(),false);
            if (null != pathDetailVO) {
                JSONObject detailPathObject = pathDetailVO.getDetailPath();
                PathAnalyzeRO pathAnalyzeRO = detailPathObject.toJavaObject(PathAnalyzeRO.class);
                message = message + " " + pathAnalyzeRO.getMessage();
            }

            switch (entity.getAnalyzeStatus()) {
                case PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED:
                    notStart++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FINISHED:
                    success++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR:
                    error++;
                    failed++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS:
                    access++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SRC_ADDRESS_HAS_NO_SUBNET:
                    noSrcSubnet++;
                    failed++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_DST_ADDRESS_HAS_NO_SUBNET:
                    noDstSubnet++;
                    failed++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SRC_DST_HAS_SAME_SUBNET:
                    srcDstHasSameSubnet++;
                    break;
                case PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_NO_ACCESS:
                    pathNoExists++;
                    failed++;
                    break;
                case BIG_INTERNET_NAT_ERROR:
                    bigInternetNatExits++;
                    failed++;
                    break;
                default:
                    break;

            }
        }



        if (notStart > 0) {
            object.put("info", String.format("????????????????????? %d ????????????", notStart));
        }
        if (success > 0) {
            object.put("success", String.format("????????????????????? %d ????????????", success));
        }
        if (access > 0 && srcDstHasSameSubnet > 0) {
            object.put("access", String.format("??????????????????????????? %d ???;%s %d ??????", access, ReturnCode.getMsg(ReturnCode.SRC_DST_FROM_SAME_SUBNET), srcDstHasSameSubnet));
        } else if (access > 0) {
            object.put("access", String.format("??????????????????????????? %d ??????", access));
        } else if (srcDstHasSameSubnet > 0){
            object.put("access", String.format("???????????????%s %d ??????", ReturnCode.getMsg(ReturnCode.SRC_DST_FROM_SAME_SUBNET),srcDstHasSameSubnet));
        }
        if (failed > 0) {
            StringBuilder sb = new StringBuilder();
            if (error > 0) {
                sb.append(String.format(";?????????????????? %d ???", error));
            }
            if (noSrcSubnet > 0) {
                sb.append(String.format(";???????????????????????? %d ???", noSrcSubnet));
            }
            if (noDstSubnet > 0) {
                sb.append(String.format(";????????????????????? %d ???", noDstSubnet));
            }
            if (pathNoExists > 0) {
                sb.append(String.format("; %d ??????????????????", pathNoExists));
            }
            if (bigInternetNatExits > 0) {
                sb.append(String.format("; ???????????? %d ???????????????NAT,????????????", bigInternetNatExits));
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            object.put("danger", String.format("???????????????%s???", sb.toString()));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(message)) {
            object.put("message", message);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }




    String getSubnetDeviceList(String subnetUuid) {
        String devices = new String();
        if (AliStringUtils.isEmpty(subnetUuid)) {
            logger.info("??????uuid???????????????????????????");
            return devices;
        }

        List<String> deviceUuidList = whaleService.getSubnetDeviceUuidList(subnetUuid);
        if (deviceUuidList == null) {
            logger.error("????????????????????????uuidList??????");
            return devices;
        }

        StringBuilder sb = new StringBuilder();
        for (String deviceUuid : deviceUuidList) {
            NodeEntity node = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
            if (node == null) {
                logger.error(String.format("??????(%s)?????????...", deviceUuid));
                continue;
            }
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
            sb.append(String.format("%s(%s)", node.getDeviceName(), node.getIp()));
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }

        return sb.toString();
    }

    /**
     * ????????????
     */
    @ApiOperation("???????????????????????????????????????JSON??????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "page", value = "??????",  dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "limit", value = "????????????", dataType = "Integer")
    })
    @PostMapping("/pageList")
    public ReturnT pageList(@RequestBody PushRecommendTaskHistoryEntity historyEntity) {
        try {
            PageInfo<PushRecommendTaskHistoryEntity> pageInfoList = recommendTaskHistoryService.findList(historyEntity, historyEntity.getPage(), historyEntity.getLimit());
            return new ReturnT(pageInfoList);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return ReturnT.FAIL;
        }

    }


}
