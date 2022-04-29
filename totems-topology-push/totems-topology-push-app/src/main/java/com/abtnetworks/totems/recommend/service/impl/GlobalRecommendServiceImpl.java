package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ContainType;
import com.abtnetworks.totems.common.exception.RecommendArgumentException;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.StringUtils;
import com.abtnetworks.totems.recommend.dto.global.PlanServiceVO;
import com.abtnetworks.totems.recommend.dto.global.CloudAddsContainResult;
import com.abtnetworks.totems.recommend.dto.global.VmwareSdnBusinessDTO;
import com.abtnetworks.totems.recommend.dto.global.VmwareSdnPlan;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.AddRecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.GlobalRecommendService;
import com.abtnetworks.totems.recommend.service.RecommendBussCommonService;
import com.abtnetworks.totems.recommend.task.impl.SimulationTaskServiceImpl;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

//全网仿真1
@Component
public class GlobalRecommendServiceImpl implements GlobalRecommendService {
    private final static Logger logger = LoggerFactory.getLogger(GlobalRecommendServiceImpl.class);
    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    private OAuth2RestTemplate OAuth2RestTemplate;
    @Value("${topology.vmsdn-server-prefix:http://${service_connection_url}:8085/}")
    private String vmwareServerPrefix;
    @Value("${topology.restpath.we-recommend-task-add:vmsdn/vmware_sdn_business/addBusiness}")
    private String restPath_weRecommendTaskAdd;
    @Value("${topology.restpath.getCloudAddsContainResult:vmsdn/cloudAdds/getCloudAddsContainResult}")
    private String restPath_getCloudAddsContainResult;
    @Value("${topology.restpath.startWeRecommend:vmsdn/vmware_sdn_business/buildPolicy}")
    private String startWeRecommend;
    @Value("${topology.restpath.searchWETaskById:vmsdn/vmware_sdn_business/getTaskById}")
    private String searchWETaskById;
    @Value("${topology.restpath.deleteBusiness:vmsdn/vmware_sdn_business/deleteBusiness}")
    private String deleteBusiness;
    @Value("${topology.restpath.searchWETaskById:vmsdn/vmware_sdn_business/updateBusiness}")
    private String editBusiness;
    @Resource
    RecommendBussCommonService recommendBussCommonService;
    @Autowired
    RecommendTaskManager policyRecommendTaskService;
    public void editWETask(AddRecommendTaskEntity entity) {
        //delete wetask
        if(entity.getWeTaskId()!=null){
            deleteWETask(entity.getWeTaskId());
        }
        //add wetask
        generateWETask(entity);
    }

    public void deleteWETask(int weTaskId) {
        JSONObject obj = new JSONObject();
        obj.put("taskId", weTaskId);
        String path = vmwareServerPrefix + deleteBusiness;
        JSONObject jsonObject = null;
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口请求参数{}", path, JSONObject.toJSONString(obj));
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, obj, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("远程调用%s接口服务端异常", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用%s接口响应状态码：%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
            } else {
                logger.error("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("远程调用%s接口响应errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }

    }

    public void addGlobalinternat(AddRecommendTaskEntity entity, RecommendTaskEntity recommendTaskEntity) {
        generateWETask(entity);
        List<RecommendTaskEntity> list = new ArrayList<>();
        recommendTaskEntity.setWeTaskId(entity.getWeTaskId());
        list.add(recommendTaskEntity);
        policyRecommendTaskService.insertRecommendTaskList(list);
        recommendBussCommonService.updateRelevanceNatTaskId(list);
    }

    public VmwareSdnBusinessDTO getWETaskByWETaskId(Integer weTaskId) {
        VmwareSdnBusinessDTO task = null;
        String path = vmwareServerPrefix + searchWETaskById + "?id=" + weTaskId;
        JSONObject jsonObject = new JSONObject();
        ResponseEntity<JSONObject> responseEntity = null;
        logger.debug("远程调用{}接口请求参数{}", path, weTaskId);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, weTaskId, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("远程调用%s接口服务端异常,id:%s", path,weTaskId), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.warn("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用%s接口响应状态码：%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.debug("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                JSONObject obj = jsonObject.getJSONObject("data");
                if (obj != null) {
                    task = obj.toJavaObject(VmwareSdnBusinessDTO.class);
                    if (task == null) {
                        logger.warn("根据任务ID:{}无法查询到东西向仿真工单", weTaskId);
                    }
                }
            } else {
                logger.warn("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("远程调用%s接口响应errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }
        return task;
    }


    //检查srcip/dstip在云内是否存在
    public CloudAddsContainResult checkCloudAddsContain(AddRecommendTaskEntity entity) {
        String path = vmwareServerPrefix + restPath_getCloudAddsContainResult + "?dstIps=" + entity.getDstIp() + "&srcIps=" + entity.getSrcIp();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dstIps", entity.getDstIp());
        jsonObject.put("srcIps", entity.getSrcIp());
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口请求参数{}", path, jsonObject);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, jsonObject, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("远程调用%s接口服务端异常", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用%s接口响应状态码：%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                CloudAddsContainResult result = jsonObject.getJSONObject("data").toJavaObject(CloudAddsContainResult.class);
                return result;
            } else {
                logger.error("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("远程调用%s接口响应errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }
    }

    private void generateWETask(AddRecommendTaskEntity entity) {
        boolean flag = true;
        CloudAddsContainResult containResult = checkCloudAddsContain(entity);
//        //内-->外，则源必须完全在云内
//        if (entity.getTaskType() == PolicyConstants.IN2OUT_INTERNET_RECOMMEND && !containResult.getSrcContainType().equals(ContainType.FULL_CONTAIN)) {
//            throw (new RecommendArgumentException("访问类型为由内到外，源地址必须完全在云内."));
//        }
//        //外-->内，则目的必须完全在云内
//        if (entity.getTaskType() == PolicyConstants.OUT2IN_INTERNET_RECOMMEND && !containResult.getDstContainType().equals(ContainType.FULL_CONTAIN)) {
//            throw (new RecommendArgumentException("访问类型为由外到内，目的地址必须完全在云内."));
//        }
        //源、目的都在云内，并且是同一个云，只需要开通东西向
        if (containResult.getDstContainType().equals(ContainType.FULL_CONTAIN) && containResult.getSrcContainType().equals(ContainType.FULL_CONTAIN) && !containResult.isMultipleCloud()) {
            //纯东西，生成一个东西向工单
            callHttpApiCreateWeTask(entity, containResult);
            //标识这是一条空任务，不走物理仿真
            entity.setTaskType(PolicyConstants.IN2IN_INTERNET_RECOMMEND);
            flag = false;
        } else {
            if (containResult.getDstContainType().equals(ContainType.PART_CONTAIN) || containResult.getDstContainType().equals(ContainType.PART_CONTAIN)) {
                //目的或源部分在云内，(南北+东西)拆分工单，对东西进行拆分
                //拆分原则：去除笛卡尔积中纯物理的部分，剩下的进行东西仿真
                callHttpApiCreateWeTask(entity, containResult);
            } else if (containResult.getSrcContainType().equals(ContainType.EXCLUSIVENESS) && containResult.getDstContainType().equals(ContainType.EXCLUSIVENESS)) {
                //纯物理 这里啥也不做
                entity.setWeTaskId(null);
            } else {
                //(南北+东西)不需要拆分工单，这里生成一个东西向工单
                callHttpApiCreateWeTask(entity, containResult);
            }
        }

        //从纯 云变成其他模式 ，tasktype不能再是16
        if(entity.getTaskType()==PolicyConstants.IN2IN_INTERNET_RECOMMEND && flag){
            //todo
            entity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
        }
    }

    public RecommendTaskEntity addGlobalRecommendTask(AddRecommendTaskEntity entity, Authentication auth) {
        //创建东西的部分
        generateWETask(entity);
        //创建南北的部分，走原有逻辑
        return recommendBussCommonService.addAutoNatGenerate(entity, auth);
    }

    public VmwareSdnBusinessDTO buildWETaskDTO(AddRecommendTaskEntity entity, CloudAddsContainResult containResult) {
        VmwareSdnBusinessDTO vmwareSdnTask = new VmwareSdnBusinessDTO();
        List<VmwareSdnPlan> planList = new ArrayList<>();
        // 先默认application
        vmwareSdnTask.setCategory("Application");
        vmwareSdnTask.setRemark(entity.getDescription());
        vmwareSdnTask.setTheme(entity.getTheme());
        //
        List<String> srcIncludeIps = containResult.getSrcIncludeIps();
        List<String> srcExcludeIps = containResult.getSrcExcludeIps();
        List<String> dstIncludeIps = containResult.getDstIncludeIps();
        List<String> dstExcludeIps = containResult.getDstExcludeIps();

        //split task
        if (!CollectionUtils.isEmpty(srcIncludeIps)) {
            VmwareSdnPlan plan = createSdnPlanByTask(entity, list2String(srcIncludeIps), entity.getDstIp());
            planList.add(plan);
            if (!CollectionUtils.isEmpty(srcExcludeIps) && !CollectionUtils.isEmpty(dstIncludeIps)) {
                VmwareSdnPlan plan2 = createSdnPlanByTask(entity, list2String(srcExcludeIps), list2String(dstIncludeIps));
                if (planList.size() > 0) {
                    plan2.setPlanName(entity.getTheme() + "-subTask" + planList.size());
                }
                planList.add(plan2);
            }
        } else {
            if (!CollectionUtils.isEmpty(srcExcludeIps) && !CollectionUtils.isEmpty(dstIncludeIps)) {
                VmwareSdnPlan plan = createSdnPlanByTask(entity, list2String(srcExcludeIps), list2String(dstIncludeIps));
                planList.add(plan);
            }
        }
        vmwareSdnTask.setVmwareSdnPlanList(planList);
        return vmwareSdnTask;
    }

    @Override
    public void deleteWeTasks(List<Integer> weTaskIds) {
        if(CollectionUtils.isEmpty(weTaskIds)){
            return;
        }
        for(Integer weTaskId:weTaskIds){
            logger.info("deleteWETask,id:{}",weTaskId);
            deleteWETask(weTaskId);
        }
    }

    String list2String(List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (!CollectionUtils.isEmpty(list)) {
            boolean flag = false;
            for (String item : list) {
                if (flag) {
                    sb.append(PolicyConstants.ADDRESS_SEPERATOR);
                }
                sb.append(item);
                flag = true;
            }
        }
        return sb.toString();
    }

    void sendStartWERecommendTask(List<Integer> taskIds) {
        String path = vmwareServerPrefix + startWeRecommend;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("taskIds", taskIds);
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口请求参数{}", path, jsonObject);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, jsonObject, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("远程调用%s接口服务端异常", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用%s接口响应状态码：%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
            } else {
                logger.error("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("远程调用%s接口响应errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }
    }

    @Autowired
    SimulationTaskServiceImpl recommendTaskManager;
    @Autowired
    LogClientSimple logClientSimple;

    public String startGlobalRecommendTaskList(String ids, Authentication authentication) throws Exception {
        List<Integer> idList = null;
        try {
            idList = StringUtils.parseIntArrayList(ids);
        } catch (Exception e) {
            logger.error("解析任务列表出错！ids=" + ids, e);
            throw (e);
        }
        List<String> themeList = new ArrayList<>();
        List<RecommendTaskEntity> taskEntitylist = new ArrayList<>();
        List<Integer> weTaskIds = new ArrayList<>();
        for (int id : idList) {
            logger.info(String.format("获取任务(%d)", id));
            RecommendTaskEntity entity = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            if (entity == null) {
                logger.error(String.format("获取任务(%d)失败, 任务不存在, 继续查找下一个...", id));
            } else if (entity.getTaskType() == PolicyConstants.IN2IN_INTERNET_RECOMMEND) {
                //空任务，只进行东西仿真
                if (entity.getWeTaskId() != null) {
                    weTaskIds.add(entity.getWeTaskId());
                }
            } else if (entity.getStatus() > PolicyConstants.POLICY_INT_TASK_TYPE_FRESH) {
                logger.error(String.format("无法开始任务(%s), 任务已完成仿真！\n", entity.getOrderNumber()));
            } else {
                if (entity.getWeTaskId() != null) {
                    weTaskIds.add(entity.getWeTaskId());
                }
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
                logger.warn("创建模拟开通数据失败！" + taskEntity.getRelevancyNat());
            }
            taskDtoList.add(taskDTO);
            policyRecommendTaskService.updateTaskStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE);
        }

        logger.info("taskDtoList.size:{};weTaskIds.size:{}", taskDtoList.size(), weTaskIds.size());

        //物理仿真
        if (!CollectionUtils.isEmpty(taskDtoList)) {
            recommendTaskManager.addSimulationTaskList(taskDtoList, authentication);
        }
        //云仿真
        if (!CollectionUtils.isEmpty(weTaskIds)) {
            sendStartWERecommendTask(weTaskIds);
        }
        String message = String.format("工单：%s 进行仿真", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        String errmsg = String.format("%d个任务已加入物理策略仿真任务队列。\n%d个任务已加入云策略仿真任务队列。\n", taskEntitylist.size(), weTaskIds.size());
        if (taskEntitylist.size() == 0 && weTaskIds.size() == 0) {
            errmsg = "没有策略仿真任务加入策略仿真队列。";
        }
        return errmsg;
    }


    VmwareSdnPlan createSdnPlanByTask(AddRecommendTaskEntity entity, String srcIps, String dstIps) {
        VmwareSdnPlan plan = new VmwareSdnPlan();
        plan.setSrcIp(srcIps);
        plan.setDstIp(dstIps);
        List<PlanServiceVO> serviceVOList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(entity.getServiceList())){
            for (ServiceDTO service : entity.getServiceList()) {
                PlanServiceVO planService = new PlanServiceVO();
                //这里前端传过来的是数字,东西向表里需要存 TCP/UDP/ICMP
                planService.setProtocol(getProtocolByProtocolRangePO(service.getProtocol()));
                planService.setSrcPort(service.getSrcPorts());
                planService.setDstPort(service.getDstPorts());
                serviceVOList.add(planService);
            }
        }else{
            PlanServiceVO planService = new PlanServiceVO();
            planService.setProtocol(PolicyConstants.POLICY_STR_VALUE_ANY.toUpperCase());
            serviceVOList.add(planService);
        }
        plan.setServiceList(serviceVOList);
//        plan.setService(JSON.toJSONString(serviceVOList));
        //界面没输入项，默认允许
        plan.setAction("pass");
        plan.setPlanName(entity.getTheme() + "-subTask");
        if (entity.getTaskType() == PolicyConstants.BIG_INTERNET_RECOMMEND) {
            //大网段
            plan.setIsIpSegment(1);
        } else {
            plan.setIsIpSegment(0);
        }
        return plan;
    }

    public void callHttpApiCreateWeTask(AddRecommendTaskEntity task, CloudAddsContainResult containResult) {
        VmwareSdnBusinessDTO entity = buildWETaskDTO(task, containResult);
        if (entity == null || CollectionUtils.isEmpty(entity.getVmwareSdnPlanList())) {
            return;
        }
        String path = vmwareServerPrefix + restPath_weRecommendTaskAdd;
        JSONObject jsonObject = null;
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口请求参数{}", path, JSONObject.toJSONString(entity));
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, entity, JSONObject.class);
        } catch (Exception e) {
            logger.error("远程调用push/recommend/task/add接口服务端异常", e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("远程调用%s接口响应状态码：%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                //新增成功
                Integer weTaskId = jsonObject.getJSONObject("data").getInteger("id");
                task.setWeTaskId(weTaskId);
            } else {
                logger.error("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("远程调用%s接口响应errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }
    }

    public String getProtocolByProtocolRangePO(String protocol) {
        switch (protocol) {
            case PolicyConstants.POLICY_NUM_VALUE_ICMP:
                return PolicyConstants.POLICY_STR_VALUE_ICMP.toUpperCase();
            case PolicyConstants.POLICY_NUM_VALUE_TCP:
                return PolicyConstants.POLICY_STR_VALUE_TCP.toUpperCase();
            case PolicyConstants.POLICY_NUM_VALUE_UDP:
                return PolicyConstants.POLICY_STR_VALUE_UDP.toUpperCase();
            default:
                return PolicyConstants.POLICY_STR_VALUE_ANY.toUpperCase();
        }
    }
}
