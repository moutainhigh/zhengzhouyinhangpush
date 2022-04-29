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
 * @Description: 策略开通业务早期类（已经业务类型分开成两个独立的控制层）
 * @date 2021/1/7
 */
@Api(value = "策略开通任务列表")
@RestController
@RequestMapping(value = "/recommend/")
public class PolicyRecommendController extends BaseController {

    private static Logger logger = Logger.getLogger(PolicyRecommendController.class);

    private final static String  SAME_SUBNET ="来源于同一个子网";
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
            logger.info("新建开通工单接口Start参数{}" + JSONObject.toJSONString(entity));
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
                String message = String.format("新建开通工单：%s 成功", recommendTaskEntity.getTheme());
                logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
                List<Integer> collect = list.stream().map(r -> r.getId()).collect(Collectors.toList());
                taskId = collect.get(0);
                returnJSON = getReturnJSON(rc, "");
                returnJSON.put("taskId", taskId);
            } catch (IllegalArgumentException e) {
                rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
                logger.error("新建开通工单接口End参数异常", e);
                return getReturnJSON(rc);
            }
        } else {
            logger.info("新建业务开通工单接口Start参数{}" + JSONObject.toJSONString(entity));
            returnJSON = addRecommendTask(entity, auth);
            taskId = (Integer) returnJSON.get("taskId");
        }
        //仿真
        if(taskId != null){
            logger.info("======================仿真开始============================");
            String id = String.valueOf(taskId);
            startRecommendTaskList(id,auth);
            logger.info("======================仿真结束============================");
        }
        return returnJSON;
    }

    @ApiOperation("new 获取任执行务状态列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "策略开通任务id", required = true, dataType = "Integer")
    })
    @PostMapping("task/gettaskstatus")
    public JSONObject getTaskStatus(int taskId) {

        TaskStatusVO taskStatusVO = policyRecommendTaskService.getTaskStatusByTaskId(taskId);

        String jsonObjectString = JSONObject.toJSONString(taskStatusVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("获取合规检测信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "策略开通任务id", required = true, dataType = "String")
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
                        //不传值,就是为any
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
            logger.error("工单合规检测信息异常,异常原因:{}", e);
            return new TotemsReturnT("-1", e.getMessage());
        }

    }

    @ApiOperation("new 获取路径分析状态列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "策略开通任务id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "Integer")
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

    @ApiOperation("new 获取路径验证信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "策略开通任务id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "psize", value = "每页条数", required = true, dataType = "Integer")
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

    @ApiOperation("new 获取路径信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "路径信息id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "isVerifyData", value = "是否为验证路径数据", required = true, dataType = "Boolean")
    })
    @PostMapping("task/pathdetail")
    public JSONObject getPathDetail(int pathInfoId, boolean isVerifyData) {

        PathDetailVO pathInfoVO = policyRecommendTaskService.getPathDetail(pathInfoId, isVerifyData);

        String jsonObjectString = JSONObject.toJSONString(pathInfoVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("new 获取路径设备详情")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "路径信息id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "isVerifyData", value = "是否为验证路径数据", required = true, dataType = "Boolean"),
            @ApiImplicitParam(paramType = "query", name = "pathIndex", value = "路径序号", required = true, dataType = "String")
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
            logger.error(String.format("数据库中没有设备详情数据...路径：%d, 设备：%s", pathInfoId, deviceUuid));
            isEmptyData = true;
        } else {
            deviceDetail = entity.getDeviceDetail();
            if (deviceDetail == null) {
                logger.error(String.format("设备详情数据中路径数据为空...路径：%d，设备：%s", pathInfoId, deviceUuid));
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

    @ApiOperation("new 获取策略风险分析结果")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "路径信息id", required = true, dataType = "Integer")
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

    @ApiOperation("new 获取策略生成结果")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "路径信息id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getpolicy")
    public JSONObject getPolicyByPathInfoId(int pathInfoId) {
        List<RecommendPolicyVO> policyVOList = policyRecommendTaskService.getPolicyByPathInfoId(pathInfoId);

        String jsonObjectString = JSONObject.toJSONString(policyVOList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }

    @ApiOperation("new 获取合并策略结果")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoId", value = "路径信息id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getmergedpolicy")
    public JSONObject getMergedPolicyByTaskId(int taskId) {
        List<RecommendPolicyVO> policyVOList = policyRecommendTaskService.getMergedPolicyByTaskId(taskId);

        String jsonObjectString = JSONObject.toJSONString(policyVOList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }


    @ApiOperation("new 获取策略检查结果列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "策略id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getcheckresult")
    public JSONObject getCheckResultByPolicyId(@Param("policyId") int taskId) {

        //策略检查更改到策略下发处显示，查询策略检查对象时应通过taskId查询
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
                logger.info(String.format("策略(%d)数据对象不存在...", entity.getPolicyId()));
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

    @ApiOperation("new 获取生成命令行")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "策略开通任务id", required = true, dataType = "Integer")
    })
    @PostMapping("task/getcommand")
    public JSONObject getCommand(int taskId) {
        List<CommandVO> commandVOList = commandTaskManager.getCommandByTaskId(taskId);

        String jsonObjectString = JSONObject.toJSONString(commandVOList);
        JSONArray jsonArray = JSONArray.parseArray(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonArray);
    }

    @ApiOperation("new 编辑生成命令行")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "策略开通任务id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "deviceUuid", value = "设备uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "command", value = "命令行", required = true, dataType = "String")
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

    @ApiOperation("开始全网策略仿真")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "ids", value = "策略开通任务id列表", required = true, dataType = "String")
    })
    @PostMapping("recommend/startGlobalRecommendTaskList")
    public JSONObject startGlobalRecommendTaskList(String ids,Authentication authentication) {
        if(!vmwareInterfaceStatusConfig.isVmInterfaceAvailable()){
            return startRecommendTaskList(ids,authentication);
        }
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("开始策略仿真任务为空！");
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

    @ApiOperation("开始策略仿真")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "策略开通任务id列表", required = true, dataType = "String")
    })
    @PostMapping("recommend/start")
    public JSONObject startRecommendTaskList(String ids,Authentication authentication) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("开始策略仿真任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("解析任务列表出错！", e);
        }

        List<String> themeList = new ArrayList<>();
        List<RecommendTaskEntity> taskEntitylist = new ArrayList<>();
        for (int id : idList) {
            logger.info(String.format("获取任务(%d)", id));
            RecommendTaskEntity entity = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            if (entity == null) {
                logger.error(String.format("获取任务(%d)失败, 任务不存在, 继续查找下一个...", id));
            }else if (entity.getTaskType()==PolicyConstants.IN2IN_INTERNET_RECOMMEND) {
                //空任务，啥也不做
                return getReturnJSON(ReturnCode.POLICY_MSG_OK, "东西向仿真开始");
            }
            else if (entity.getStatus() > PolicyConstants.POLICY_INT_TASK_TYPE_FRESH) {
                logger.error(String.format("无法开始任务(%s), 任务已完成仿真！\n", entity.getOrderNumber()));
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

            //设置服务对象
            if (taskEntity.getServiceList() == null) {
                taskDTO.setServiceList(null);
            } else {
                JSONArray array = JSONArray.parseArray(taskEntity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                taskDTO.setServiceList(serviceList);
            }
            WhatIfRO whatIf = recommendBussCommonService.createWhatIfCaseUuid(taskEntity);
            if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
                logger.info("创建模拟开通环境UUID为:" + whatIf.getUuid());
                taskDTO.setWhatIfCaseUuid(whatIf.getUuid());
                taskDTO.setDeviceWhatifs(whatIf.getDeviceWhatifs());
            } else {
                logger.error("创建模拟开通数据失败！" + taskEntity.getRelevancyNat());
            }
            taskDtoList.add(taskDTO);
            policyRecommendTaskService.updateTaskStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE);
        }

        recommendTaskManager.addSimulationTaskList(taskDtoList, authentication);
        String message = String.format("工单：%s 进行仿真", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        String errmsg = String.format("%d个任务已加入策略仿真任务队列。\n", taskEntitylist.size());
        if (taskEntitylist.size() == 0) {
            errmsg = "没有策略仿真任务加入策略仿真队列。";
        }

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, errmsg);
    }

    @ApiOperation("重新开始策略仿真")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "策略开通任务id列表", required = true, dataType = "String")
    })
    @PostMapping("recommend/restart")
    public JSONObject restartRecommendTaskList(String ids,Authentication authentication) {
        String idString = ids;
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("开始策略仿真任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("解析任务列表出错！", e);
        }

        //检测重新仿真的任务中是否有在运行中的，有的话则不进行重新仿真。
        StringBuilder errMsg = new StringBuilder();
        int rc = ReturnCode.POLICY_MSG_OK;
        List<String> themeList = new ArrayList<>();
        for (int id : idList) {
            logger.info(String.format("获取任务(%d)", id));
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
        //删除工单历史数据
        policyRecommendTaskService.deleteTasks(idList,type);
        //开始仿真
        JSONObject jsonObject = startRecommendTaskList(idString, authentication);

        return jsonObject;

    }

    @ApiOperation("添加策略开通任务并开始")
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
            //vmware接口不可用时走老逻辑
            return addRecommendTask(entity, auth);
        }
        // 参数校验
        entity.setDstIp(InputValueUtils.formatIpAddress(entity.getDstIp()));
        int rc = recommendBussCommonService.checkParamForSrcAddress(entity);
        List<ServiceDTO> serviceList = entity.getServiceList();
        Set<String> serviceSet = new HashSet<>();
        for (ServiceDTO serviceDTO : serviceList) {
            if (serviceSet.contains(serviceDTO.getProtocol())) {
                return getReturnJSON(ReturnCode.FAILED, "服务中同类型协议只能添加一条！");
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
            //东西/南北
            recommendTaskEntity = globalRecommendService.addGlobalRecommendTask(entity, auth);
        } catch (IssuedExecutorException e) {
            return getReturnJSON(ReturnCode.FAILED, e.getMessage());
        }
        String message = String.format("新建业务开通工单：%s 成功", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        JSONObject rs = getReturnJSON(rc, "");
        rs.put("taskId", recommendTaskEntity.getId());
        return rs;
    }

    @ApiOperation("new 添加策略开通任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "theme", value = "策略主题", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "description", value = "申请描述", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "srcIp", value = "源IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "dstIp", value = "目的IP", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceList", value = "协议号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "策略开始时间", required = false, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "策略结束时间", required = false, dataType = "Long"),
    })
    @PostMapping("task/add")
    public JSONObject addRecommendTask(@RequestBody AddRecommendTaskEntity entity, Authentication auth) {
        //若出IP范围起始地址大于终止地址错误，则自动纠正 新增ipv6之后这里的校验就不能使用了，
        //int  rc =  recommendBussCommonService.checkParamForDstAddress(entity);
        //格式化目的地址，目的地址会包括域名，此处不做校验
        entity.setDstIp(InputValueUtils.formatIpAddress(entity.getDstIp()));
        int rc = recommendBussCommonService.checkParamForSrcAddress(entity);
        List<ServiceDTO> serviceList = entity.getServiceList();
        Set<String> serviceSet = new HashSet<>();
        for (ServiceDTO serviceDTO : serviceList) {
            if (serviceSet.contains(serviceDTO.getProtocol())) {
                return getReturnJSON(ReturnCode.FAILED, "服务中同类型协议只能添加一条！");
            } else {
                serviceSet.add(serviceDTO.getProtocol());
            }
        }
        logger.info("新建任务的whatIfCases is " + entity.getWhatIfCases());


        for (ServiceDTO service : serviceList) {
            if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
            }
        }
        //添加数据检测
        RecommendTaskEntity recommendTaskEntity = null;
        try {
            recommendTaskEntity = recommendBussCommonService.addAutoNatGenerate(entity, auth);
        } catch (IssuedExecutorException e) {
            return getReturnJSON(ReturnCode.FAILED, e.getMessage());
        }
        String message = String.format("新建业务开通工单：%s 成功", recommendTaskEntity.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        JSONObject rs = getReturnJSON(rc, "");
        rs.put("taskId", recommendTaskEntity.getId());
        return rs;
    }



    @ApiOperation("停止策略仿真")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "策略开通任务id列表", required = true, dataType = "String")
    })
    @PostMapping("recommend/stop")
    public JSONObject stopRecommendTaskList(String ids) {
        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("开始策略仿真任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("解析任务列表出错！", e);
        }

        Map<String, String> themeMap = new HashMap<>();
        List<String> taskList = new ArrayList<>();
        StringBuilder errMsg = new StringBuilder();

        boolean hasFailed = false;
        for (int id : idList) {
            logger.info(String.format("获取任务(%d)", id));
            RecommendTaskEntity entity = policyRecommendTaskService.getRecommendTaskByTaskId(id);

            if (entity == null) {
                logger.error(String.format("获取任务(%d)失败, 任务不存在, 继续查找下一个...", id));
                hasFailed = true;
            } else if (entity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE ||
                    entity.getStatus() == PolicyConstants.POLICY_INT_STATUS_SIMULATING) {
                errMsg.append(String.format("无法停止任务(%s), 任务未开始或者已完成仿真！\n", entity.getTheme()));
                hasFailed = true;
            } else {
                taskList.add(String.valueOf(id));
                themeMap.put(String.valueOf(id), entity.getTheme());
            }
        }

        if (taskList.size() == 0) {
            logger.error("没有任务可以停止！");
            errMsg.insert(0, "没有任务可以停止！");
            return getReturnJSON(ReturnCode.NO_RECOMMEND_TASK_ENTITY_GET, "没有任务可以停止！");
        }

        String message = String.format("工单：%s 停止仿真", org.apache.commons.lang3.StringUtils.join(themeMap.values(), ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        List<String> failedList = recommendTaskManager.stopTaskList(taskList);

        for (String id : taskList) {
            if (failedList.contains(id)) {
                logger.info(String.format("停止任务（%s）失败", id));
                errMsg.append(String.format("停止任务（%s）失败", id));
                hasFailed = true;
                logClientSimple.addBusinessLog(LogLevel.ERROR.getId(), BusinessLogType.POLICY_PUSH.getId(), String.format("停止工单: %s 失败", themeMap.get(id)));
                continue;
            }
            Integer idNum = 0;
            try {
                idNum = Integer.valueOf(id);
            } catch (Exception e) {
                logger.error(String.format("转换任务id类型失败！id为(%s)", id));
            }
            policyRecommendTaskService.updateTaskStatus(idNum, PolicyConstants.POLICY_INT_STATUS_STOPPED);
        }
        String msg = "停止策略下发任务成功！";
        int code = ReturnCode.POLICY_MSG_OK;
        if (hasFailed) {
            errMsg.insert(0, "停止策略下发任务完成！");
            code = ReturnCode.INVALID_TASK_ID;
            msg = errMsg.toString();
        }

        return getReturnJSON(code, msg);
    }

    @ApiOperation("批量开始验证任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "ids", value = "策略开通任务id", required = true, dataType = "String")
    })
    @PostMapping("verify/startverify")
    public JSONObject startVerify(String ids) {
        logger.info(String.format("开始批量验证[%s]... ", ids));

        if (verifyTaskManager.isVerifyRunning()) {
            return getReturnJSON(ReturnCode.VERIFY_TASK_IS_RUNNING);
        }

        ids = String.format("[%s]", ids);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("添加到下发列表任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }
        StringBuilder sb = new StringBuilder();

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("解析任务列表出错！", e);
        }

        List<String> themeList = new ArrayList<>();
        List<RecommendTaskEntity> taskList = new ArrayList<>();
        for (Integer id : idList) {
            RecommendTaskEntity task = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            WhatIfRO whatIf = recommendBussCommonService.createWhatIfCaseUuid(task);
            if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
                logger.info("创建模拟开通环境UUID为:" + whatIf.getUuid());
                task.setWhatIfCase(whatIf.getUuid());
            } else {
                logger.error("创建模拟开通数据失败！" + task.getRelevancyNat());
            }
            taskList.add(task);
            sb.append(String.format(",%s[%s]开始验证！\n", task.getTheme(), task.getOrderNumber()));
            themeList.add(task.getTheme());
        }
        String msg = sb.toString().replaceFirst(",", "");

        logger.info(msg);
        int rc = verifyTaskManager.startVerify(taskList);
        String message = String.format("工单：%s 开始验证", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return getReturnJSON(rc, "开始验证");
    }



    @ApiOperation("new 设置路径有效")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pathInfoIds", value = "路径信息id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "enable", value = "是否启用(0:diable,1:enable)", required = true, dataType = "String")
    })
    @PostMapping("task/enablepath")
    public JSONObject enablePath(String pathInfoIds, String enable,Authentication authentication) {
        pathInfoIds = String.format("[%s]", pathInfoIds);
        JSONArray jsonArray = JSONArray.parseArray(pathInfoIds);
        if (jsonArray == null || jsonArray.size() == 0) {
            logger.error("开始策略仿真任务为空！");
            return getReturnJSON(ReturnCode.NO_TASK_ID_SELECTED_TO_START);
        }

        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(pathInfoIds);
        } catch (Exception e) {
            logger.error("解析任务列表出错！", e);
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

        String message = String.format("修改工单：%s ，路径id: %s，将路径设置为 %s", taskEntity.getTheme(), pathInfoIds, enable.equals("0") ? "无效" : "有效");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        SimulationTaskDTO task = new SimulationTaskDTO();
        BeanUtils.copyProperties(taskEntity, task);
        //设置服务对象
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



    @ApiOperation("new 获取单个任务详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "策略开通任务id", required = true, dataType = "String")
    })
    @PostMapping("task/getpathstatic")
    public JSONObject getPathStaticByTaskId(String taskId) {
        int taskIdNum = 0;
        try {
            taskIdNum = Integer.valueOf(taskId);
        } catch (Exception e) {
            getReturnJSON(ReturnCode.FAILED, "工单号不正确!");
        }

        RecommendTaskEntity taskEntity = policyRecommendTaskService.getRecommendTaskByTaskId(taskIdNum);
        if (taskEntity.getStatus().equals(PolicyConstants.POLICY_INT_STATUS_SIMULATION_NOT_STARTED)) {
            return getReturnJSON(ReturnCode.POLICY_MSG_OK, "模拟仿真未开始");
        }
        JSONObject object = new JSONObject();
        List<PathInfoEntity> list = policyRecommendTaskService.getPathInfoByTaskId(taskIdNum);
        if (list.size() == 0) {
            //KSH-5006
            object.put("danger", "无路径生成");
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
            // 如果是原目的子网相同，则需要去查询路径详情获取错误message
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
            object.put("info", String.format("未开始（未开始 %d 条路径）", notStart));
        }
        if (success > 0) {
            object.put("success", String.format("开通成功（开通 %d 条路径）", success));
        }
        if (access > 0 && srcDstHasSameSubnet > 0) {
            object.put("access", String.format("无需开通（已有通路 %d 条;%s %d 条）", access, ReturnCode.getMsg(ReturnCode.SRC_DST_FROM_SAME_SUBNET), srcDstHasSameSubnet));
        } else if (access > 0) {
            object.put("access", String.format("无需开通（已有通路 %d 条）", access));
        } else if (srcDstHasSameSubnet > 0){
            object.put("access", String.format("无需开通（%s %d 条）", ReturnCode.getMsg(ReturnCode.SRC_DST_FROM_SAME_SUBNET),srcDstHasSameSubnet));
        }
        if (failed > 0) {
            StringBuilder sb = new StringBuilder();
            if (error > 0) {
                sb.append(String.format(";系统执行异常 %d 条", error));
            }
            if (noSrcSubnet > 0) {
                sb.append(String.format(";源地址无对应子网 %d 条", noSrcSubnet));
            }
            if (noDstSubnet > 0) {
                sb.append(String.format(";未找到可达路径 %d 条", noDstSubnet));
            }
            if (pathNoExists > 0) {
                sb.append(String.format("; %d 条路径不存在", pathNoExists));
            }
            if (bigInternetNatExits > 0) {
                sb.append(String.format("; 大网段有 %d 条路径经过NAT,仿真失败", bigInternetNatExits));
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            object.put("danger", String.format("开通失败（%s）", sb.toString()));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(message)) {
            object.put("message", message);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }




    String getSubnetDeviceList(String subnetUuid) {
        String devices = new String();
        if (AliStringUtils.isEmpty(subnetUuid)) {
            logger.info("子网uuid为空，没有关联设备");
            return devices;
        }

        List<String> deviceUuidList = whaleService.getSubnetDeviceUuidList(subnetUuid);
        if (deviceUuidList == null) {
            logger.error("获取子网关联设备uuidList失败");
            return devices;
        }

        StringBuilder sb = new StringBuilder();
        for (String deviceUuid : deviceUuidList) {
            NodeEntity node = policyRecommendTaskService.getTheNodeByUuid(deviceUuid);
            if (node == null) {
                logger.error(String.format("设备(%s)不存在...", deviceUuid));
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
     * 分页查询
     */
    @ApiOperation("仿真导入历史列表，查询传参JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "page", value = "页数",  dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "limit", value = "每页条数", dataType = "Integer")
    })
    @PostMapping("/pageList")
    public ReturnT pageList(@RequestBody PushRecommendTaskHistoryEntity historyEntity) {
        try {
            PageInfo<PushRecommendTaskHistoryEntity> pageInfoList = recommendTaskHistoryService.findList(historyEntity, historyEntity.getPage(), historyEntity.getLimit());
            return new ReturnT(pageInfoList);
        } catch (Exception e) {
            logger.error("分页查询仿真导入历史列表异常", e);
            return ReturnT.FAIL;
        }

    }


}
