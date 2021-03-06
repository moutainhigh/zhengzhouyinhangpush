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

//????????????1
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
        logger.info("????????????{}??????????????????{}", path, JSONObject.toJSONString(obj));
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, obj, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("????????????%s?????????????????????", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("????????????{}????????????????????????{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("????????????%s????????????????????????%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("????????????{}??????????????????{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
            } else {
                logger.error("????????????{}????????????,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("????????????%s????????????errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
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
        logger.debug("????????????{}??????????????????{}", path, weTaskId);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, weTaskId, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("????????????%s?????????????????????,id:%s", path,weTaskId), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.warn("????????????{}????????????????????????{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("????????????%s????????????????????????%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.debug("????????????{}??????????????????{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                JSONObject obj = jsonObject.getJSONObject("data");
                if (obj != null) {
                    task = obj.toJavaObject(VmwareSdnBusinessDTO.class);
                    if (task == null) {
                        logger.warn("????????????ID:{}????????????????????????????????????", weTaskId);
                    }
                }
            } else {
                logger.warn("????????????{}????????????,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("????????????%s????????????errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }
        return task;
    }


    //??????srcip/dstip?????????????????????
    public CloudAddsContainResult checkCloudAddsContain(AddRecommendTaskEntity entity) {
        String path = vmwareServerPrefix + restPath_getCloudAddsContainResult + "?dstIps=" + entity.getDstIp() + "&srcIps=" + entity.getSrcIp();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dstIps", entity.getDstIp());
        jsonObject.put("srcIps", entity.getSrcIp());
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("????????????{}??????????????????{}", path, jsonObject);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, jsonObject, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("????????????%s?????????????????????", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("????????????{}????????????????????????{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("????????????%s????????????????????????%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("????????????{}??????????????????{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                CloudAddsContainResult result = jsonObject.getJSONObject("data").toJavaObject(CloudAddsContainResult.class);
                return result;
            } else {
                logger.error("????????????{}????????????,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("????????????%s????????????errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
            }
        }
    }

    private void generateWETask(AddRecommendTaskEntity entity) {
        boolean flag = true;
        CloudAddsContainResult containResult = checkCloudAddsContain(entity);
//        //???-->?????????????????????????????????
//        if (entity.getTaskType() == PolicyConstants.IN2OUT_INTERNET_RECOMMEND && !containResult.getSrcContainType().equals(ContainType.FULL_CONTAIN)) {
//            throw (new RecommendArgumentException("????????????????????????????????????????????????????????????."));
//        }
//        //???-->????????????????????????????????????
//        if (entity.getTaskType() == PolicyConstants.OUT2IN_INTERNET_RECOMMEND && !containResult.getDstContainType().equals(ContainType.FULL_CONTAIN)) {
//            throw (new RecommendArgumentException("???????????????????????????????????????????????????????????????."));
//        }
        //???????????????????????????????????????????????????????????????????????????
        if (containResult.getDstContainType().equals(ContainType.FULL_CONTAIN) && containResult.getSrcContainType().equals(ContainType.FULL_CONTAIN) && !containResult.isMultipleCloud()) {
            //???????????????????????????????????????
            callHttpApiCreateWeTask(entity, containResult);
            //????????????????????????????????????????????????
            entity.setTaskType(PolicyConstants.IN2IN_INTERNET_RECOMMEND);
            flag = false;
        } else {
            if (containResult.getDstContainType().equals(ContainType.PART_CONTAIN) || containResult.getDstContainType().equals(ContainType.PART_CONTAIN)) {
                //??????????????????????????????(??????+??????)????????????????????????????????????
                //????????????????????????????????????????????????????????????????????????????????????
                callHttpApiCreateWeTask(entity, containResult);
            } else if (containResult.getSrcContainType().equals(ContainType.EXCLUSIVENESS) && containResult.getDstContainType().equals(ContainType.EXCLUSIVENESS)) {
                //????????? ??????????????????
                entity.setWeTaskId(null);
            } else {
                //(??????+??????)?????????????????????????????????????????????????????????
                callHttpApiCreateWeTask(entity, containResult);
            }
        }

        //?????? ????????????????????? ???tasktype????????????16
        if(entity.getTaskType()==PolicyConstants.IN2IN_INTERNET_RECOMMEND && flag){
            //todo
            entity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
        }
    }

    public RecommendTaskEntity addGlobalRecommendTask(AddRecommendTaskEntity entity, Authentication auth) {
        //?????????????????????
        generateWETask(entity);
        //???????????????????????????????????????
        return recommendBussCommonService.addAutoNatGenerate(entity, auth);
    }

    public VmwareSdnBusinessDTO buildWETaskDTO(AddRecommendTaskEntity entity, CloudAddsContainResult containResult) {
        VmwareSdnBusinessDTO vmwareSdnTask = new VmwareSdnBusinessDTO();
        List<VmwareSdnPlan> planList = new ArrayList<>();
        // ?????????application
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
        logger.info("????????????{}??????????????????{}", path, jsonObject);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, jsonObject, JSONObject.class);
        } catch (Exception e) {
            logger.error(String.format("????????????%s?????????????????????", path), e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("????????????{}????????????????????????{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("????????????%s????????????????????????%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("????????????{}??????????????????{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
            } else {
                logger.error("????????????{}????????????,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("????????????%s????????????errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
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
            logger.error("???????????????????????????ids=" + ids, e);
            throw (e);
        }
        List<String> themeList = new ArrayList<>();
        List<RecommendTaskEntity> taskEntitylist = new ArrayList<>();
        List<Integer> weTaskIds = new ArrayList<>();
        for (int id : idList) {
            logger.info(String.format("????????????(%d)", id));
            RecommendTaskEntity entity = policyRecommendTaskService.getRecommendTaskByTaskId(id);
            if (entity == null) {
                logger.error(String.format("????????????(%d)??????, ???????????????, ?????????????????????...", id));
            } else if (entity.getTaskType() == PolicyConstants.IN2IN_INTERNET_RECOMMEND) {
                //?????????????????????????????????
                if (entity.getWeTaskId() != null) {
                    weTaskIds.add(entity.getWeTaskId());
                }
            } else if (entity.getStatus() > PolicyConstants.POLICY_INT_TASK_TYPE_FRESH) {
                logger.error(String.format("??????????????????(%s), ????????????????????????\n", entity.getOrderNumber()));
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
                logger.warn("?????????????????????????????????" + taskEntity.getRelevancyNat());
            }
            taskDtoList.add(taskDTO);
            policyRecommendTaskService.updateTaskStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE);
        }

        logger.info("taskDtoList.size:{};weTaskIds.size:{}", taskDtoList.size(), weTaskIds.size());

        //????????????
        if (!CollectionUtils.isEmpty(taskDtoList)) {
            recommendTaskManager.addSimulationTaskList(taskDtoList, authentication);
        }
        //?????????
        if (!CollectionUtils.isEmpty(weTaskIds)) {
            sendStartWERecommendTask(weTaskIds);
        }
        String message = String.format("?????????%s ????????????", org.apache.commons.lang3.StringUtils.join(themeList, ","));
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        String errmsg = String.format("%d???????????????????????????????????????????????????\n%d????????????????????????????????????????????????\n", taskEntitylist.size(), weTaskIds.size());
        if (taskEntitylist.size() == 0 && weTaskIds.size() == 0) {
            errmsg = "???????????????????????????????????????????????????";
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
                //?????????????????????????????????,???????????????????????? TCP/UDP/ICMP
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
        //?????????????????????????????????
        plan.setAction("pass");
        plan.setPlanName(entity.getTheme() + "-subTask");
        if (entity.getTaskType() == PolicyConstants.BIG_INTERNET_RECOMMEND) {
            //?????????
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
        logger.info("????????????{}??????????????????{}", path, JSONObject.toJSONString(entity));
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, entity, JSONObject.class);
        } catch (Exception e) {
            logger.error("????????????push/recommend/task/add?????????????????????", e);
            throw (e);
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.error("????????????{}????????????????????????{}", path, responseEntity.getStatusCodeValue());
            throw (new RuntimeException(String.format("????????????%s????????????????????????%s", path, responseEntity.getStatusCodeValue())));
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("????????????{}??????????????????{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                //????????????
                Integer weTaskId = jsonObject.getJSONObject("data").getInteger("id");
                task.setWeTaskId(weTaskId);
            } else {
                logger.error("????????????{}????????????,errorCode:{};errmsg:{}", path, jsonObject.get("code"), jsonObject.get("msg"));
                throw (new RuntimeException(String.format("????????????%s????????????errorCode:%s;errmsg:%s", path, jsonObject.get("code"), jsonObject.get("msg"))));
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
