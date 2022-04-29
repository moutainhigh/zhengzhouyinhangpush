package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.entity.PathDeviceDetailEntity;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.ExternalManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.VerifyService;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.policy.dto.PathAnalyzeDTO;
import com.abtnetworks.totems.whale.policy.ro.DeviceDetailRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeDataRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import com.abtnetworks.totems.whale.policy.ro.PathDetailRO;
import com.abtnetworks.totems.whale.policy.ro.PathInfoRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.sleep;

@Service
public class VerifyServiceImpl implements VerifyService {

    private static Logger logger = LoggerFactory.getLogger(VerifyServiceImpl.class);

    private static final int MAX_ANALYSE_TIME=3*3600; //秒

    @Autowired
    RecommendTaskManager recommendTaskService;

    @Autowired
    ExternalManager externalService;

    @Autowired
    WhaleManager whaleService;


    @Override
    public int verifyTask(List<RecommendTaskEntity> taskList) {

        //设置任务为正在验证
        setTaskStatus(taskList, PolicyConstants.POLICY_INT_STATUS_VERIFYING);

        //根据任务获PathInfo的信息
        List<PathInfoTaskDTO> list = getPathInfoTaskDTOList(taskList);

        //重置每条PathInfo的状态
        resetPathInfoStatus(list);

        //汇总所有PathInfo上设备UUID信息
        List<String> deviceUuidList = getDeviceUuidListByPathInfo(list);

        //对所有设备进行采集，返回采集失败的设备列表
        List<String> invalidDeviceUuidList = gatherDevices(deviceUuidList);

        //根据采集失败的设备，找到具有采集失败的PathInfo的列表
        List<PathInfoTaskDTO> failedTaskList = getFailedPathInfoTaskList(list, invalidDeviceUuidList);

        //设置所有PathInfo采集状态
        setPathInfoTaskGatherStatus(list, failedTaskList);

        //依次验证所有PathInfo
        Map<Integer, Integer> resultMap = verifyAllPath(list, failedTaskList);

        //设置任务为验证完成
        setTaskStatus(taskList, resultMap);

        return ReturnCode.POLICY_MSG_OK;
    }

    private Map<Integer, Integer> verifyAllPath(List<PathInfoTaskDTO> list, List<PathInfoTaskDTO> failedTaskList) {
        Map<Integer, Integer> resultMap = new HashMap<>();
        for(PathInfoTaskDTO taskEntity: list) {
            if(failedTaskList.contains(taskEntity)) {
                logger.info("路径包含采集失败设备，不继续进行验证...");
                resultMap.put(taskEntity.getTaskId(), PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
                continue;
            }
            int rc = verifyByPath(taskEntity);
            if(!resultMap.containsKey(taskEntity.getTaskId())) {
                if(rc == ReturnCode.POLICY_MSG_OK) {
                    resultMap.put(taskEntity.getTaskId(), PolicyConstants.POLICY_INT_STATUS_VERIFY_DONE);
                } else {
                    resultMap.put(taskEntity.getTaskId(), PolicyConstants.POLICY_INT_STATUS_VERIFY_ERROR);
                }
            }
        }
        return resultMap;
    }

    /**
     * 更新所有任务状态为验证中
     * @param taskList 任务列表
     */
    void setTaskStatus(List<RecommendTaskEntity> taskList, Map<Integer, Integer> resultMap) {
        for(RecommendTaskEntity task:taskList) {
            Integer result = resultMap.get(task.getId());
            if(result == null) {
                result = PolicyConstants.POLICY_INT_STATUS_VERIFY_DONE;
            }
            recommendTaskService.updateTaskStatus(task.getId(), result);
        }
    }

    void setTaskStatus(List<RecommendTaskEntity> taskList, Integer status) {
        for(RecommendTaskEntity task:taskList) {
            recommendTaskService.updateTaskStatus(task.getId(), status);
        }
    }

    /**
     * 获取任务列表下所有PathInfo列表
     * @param taskList 任务列表
     * @return PathInfo列表
     */
    private List<PathInfoTaskDTO> getPathInfoTaskDTOList(List<RecommendTaskEntity> taskList) {
        logger.info("获取每个任务的路径信息...");
        List<PathInfoTaskDTO> list = new ArrayList<>();
        for(RecommendTaskEntity taskEntity : taskList) {
            List<PathInfoEntity> pathInfoEntityList = recommendTaskService.getPathInfoByTaskId(taskEntity.getId());
            for(PathInfoEntity pathInfoEntity : pathInfoEntityList) {
                //添加路径时跳过未启用的路径
                if(pathInfoEntity.getEnablePath() == PolicyConstants.PATH_ENABLE_DISABLE) {
                    continue;
                }
                PathInfoTaskDTO pathInfoTaskDTO = new PathInfoTaskDTO();
                BeanUtils.copyProperties(pathInfoEntity, pathInfoTaskDTO);
                pathInfoTaskDTO.setWhatIfCaseUuid(taskEntity.getWhatIfCase());
                pathInfoTaskDTO.setIpType(taskEntity.getIpType());
                if(!AliStringUtils.isEmpty(pathInfoEntity.getService())) {
                    JSONArray array = JSONArray.parseArray(pathInfoEntity.getService());
                    List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                    pathInfoTaskDTO.setServiceList(serviceList);
                }
                list.add(pathInfoTaskDTO);
            }
        }
        return list;
    }

    /**
     * 重置所有PathInfo采集和验证步骤状态
     * @param list PathInfo列表
     */
    void resetPathInfoStatus(List<PathInfoTaskDTO> list) {
        logger.info("重置所有PathInfo采集和验证状态...");
        for(PathInfoTaskDTO task : list) {
            setPathInfoStatus(task, PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED,
                    PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        }
    }

    /**
     * 获取设备UUID列表，过滤重复项
     * @param taskList 任务列表
     * @return 设备UUID列表
     */
    private List<String> getDeviceUuidListByPathInfo(List<PathInfoTaskDTO> taskList) {
        logger.info("获取所有PathInfo上的所有设备UUID...");
        Set<String> deviceSet = new HashSet<>();
        for(PathInfoTaskDTO taskDTO: taskList) {
            List<RecommendPolicyEntity> policyList = recommendTaskService.getPolicyListByPathInfoId(taskDTO.getId());
            for(RecommendPolicyEntity policyEntity: policyList) {
                String deviceUuid = policyEntity.getDeviceUuid();
                //策略相关设备UUID应该不为空，跳过添加到deviceSet中，以免后续过程出错，HashSet可以添加null值
                if(deviceUuid == null) {
                    logger.error(String.format("策略(%d)的设备UUID为空...", policyEntity.getId()));
                    continue;
                }
                deviceSet.add(deviceUuid);
            }
        }

        return new ArrayList<>(deviceSet);
    }

    /**
     * 开始采集设备
     * @param deviceUuidList 所有设备uuid
     * @return 采集失败设备列表，包括被删除设备，手动采集设备和采集失败设备
     */
    List<String> gatherDevices(List<String> deviceUuidList) {
        logger.info("开始采集设备...");
        //获取设备采集Id
        List<String> invalidDeviceUuidList = new ArrayList<>();             //用来保存节点被删除，手动导入和采集失败的设备
        List<String> gatherIdList = new ArrayList<>();
        List<String> gatherDeviceUuidList = new ArrayList<>();
        for(String deviceUuid: deviceUuidList) {
            NodeEntity nodeEntity = recommendTaskService.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.error(String.format("设备(%s)节点已被删除...", deviceUuid));
                invalidDeviceUuidList.add(deviceUuid);
                continue;
            }

            //设备来源：1手工导入2采集
            if(nodeEntity.getOrigin() == 1) {
                logger.error(String.format("设备(%s)[%s(%s)]的手动采集设备...", deviceUuid, nodeEntity.getDeviceName(), nodeEntity.getIp()));
                invalidDeviceUuidList.add(deviceUuid);
                continue;
            }
            //若设备为虚设备，则下发设备信息获取其主设备的
            DeviceRO deviceRO = whaleService.getDeviceByUuid(deviceUuid);
            int gatherId = nodeEntity.getId();
            if(deviceRO == null || deviceRO.getData() == null ||deviceRO.getData().size() ==0 ) {
                logger.error("设备信息为空，不查询是否为虚设备");

            }else{
                DeviceDataRO deviceData = deviceRO.getData().get(0);
                if (deviceData.getIsVsys() != null) {
                    //不等于null就是虚墙,获取主墙的设备uuid
                    String deviceUuidRoot =  deviceData.getRootDeviceUuid();
                    logger.info("采集时发现虚墙{}=设备{}uuid={}，就使用主墙的uuid={}",deviceData.getIsVsys(),deviceData.getVsysName(),deviceData.getUuid(),deviceUuidRoot);
                    NodeEntity  rootNode =  recommendTaskService.getTheNodeByUuid(deviceUuidRoot);
                    if(rootNode == null) {
                        logger.error(String.format("设备(%s)节点已被删除...", deviceUuidRoot));
                        invalidDeviceUuidList.add(deviceUuidRoot);
                        continue;
                    }
                    deviceUuid = deviceUuidRoot;
                    gatherId  = rootNode.getId();
                }
            }
            gatherIdList.add(String.valueOf(gatherId));
            if(StringUtils.isNotEmpty(deviceUuid)){
                gatherDeviceUuidList.add(deviceUuid);
            }

        }
        //去重，重复的id不能重复采集，避免虚墙和主墙存在重复
        if(CollectionUtils.isNotEmpty(gatherIdList)){
            gatherIdList.stream().distinct().forEach(p->{
                externalService.doGather(p);
            });
        }
        List<String> distinctDeviceUuidList = new ArrayList<>();
        gatherDeviceUuidList.stream().distinct().forEach(uuid->{
            distinctDeviceUuidList.add(uuid);
        });
        List<String> failedList = checkingGatherStatus(distinctDeviceUuidList);

        invalidDeviceUuidList.addAll(failedList);

        return invalidDeviceUuidList;
    }

    /**
     * 根据采集设备uuid，获取通过采集失败设备路径集的列表
     * @param list 路径任务列表
     * @param invalidDeviceUuidList 采集失败设备UUID列表
     * @return 采集失败路径任务列表
     */
    List<PathInfoTaskDTO> getFailedPathInfoTaskList(List<PathInfoTaskDTO> list, List<String> invalidDeviceUuidList) {
        List<PathInfoTaskDTO> failedTaskList = new ArrayList<>();
        for(PathInfoTaskDTO entity : list) {
            List<RecommendPolicyEntity> policyList = recommendTaskService.getPolicyListByPathInfoId(entity.getId());
            boolean failed = false;
            for(RecommendPolicyEntity policyEntity : policyList) {
                String uuid = policyEntity.getDeviceUuid();
                //有相关设备在采集失败列表中，则设置为采集失败,并将任务设置为验证失败
                if(invalidDeviceUuidList.contains(uuid)) {
                    failedTaskList.add(entity);
                    recommendTaskService.updatePathGatherStatus(entity.getId(), PolicyConstants.POLICY_INT_RECOMMEND_GATHER_FAILED);
                    failed = true;
                    break;
                }

            }
            //没有失败的任务则更新采集状态为完成
            if(!failed) {
                recommendTaskService.updatePathGatherStatus(entity.getId(), PolicyConstants.POLICY_INT_RECOMMEND_GATHER_SUCCESS);
            }
        }
        return failedTaskList;
    }

    /**
     * 设置PathInfo的采集状态
     * @param allList 所有PathInfo列表
     * @param failedList 采集失败的PathInfo列表
     */
    void setPathInfoTaskGatherStatus(List<PathInfoTaskDTO> allList, List<PathInfoTaskDTO> failedList) {
        for(PathInfoTaskDTO pathInfoTaskDTO : allList) {
            if(failedList.contains(pathInfoTaskDTO)) {
                setPathInfoGatherStatus(pathInfoTaskDTO, PolicyConstants.POLICY_INT_RECOMMEND_GATHER_FAILED);
            } else {
                setPathInfoGatherStatus(pathInfoTaskDTO, PolicyConstants.POLICY_INT_RECOMMEND_GATHER_SUCCESS);
            }
        }
    }

    /**
     * 验证路径状态
     * @param taskDTO
     * @return
     */
    private int verifyByPath(PathInfoTaskDTO taskDTO) {
        logger.info(String.format("%s[%s]任务路径验证...", taskDTO.getOrderNumber(), taskDTO.getTheme()));
        PathAnalyzeDTO pathAnalyzeDTO = whaleService.getAnylizePathDTO(taskDTO);

        String verificationDetailedPath = "";
        try {
            verificationDetailedPath = externalService.getDetailPath(pathAnalyzeDTO);
        } catch (Exception e) {
            logger.error("查询路径数据为空...");
            recommendTaskService.updatePathVerifyStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_RECOMMEND_VERIFY_ERROR);
            return ReturnCode.POLICY_VERIFY_ERROR;
        }
        JSONObject detailPathObject = JSONObject.parseObject(verificationDetailedPath);
        //解析路径详情数据为对象
        PathAnalyzeRO pathAnalyzeRO = detailPathObject.toJavaObject(PathAnalyzeRO.class);

        if(pathAnalyzeRO == null) {
            logger.error("查询路径数据为空...");
            recommendTaskService.updatePathVerifyStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_RECOMMEND_VERIFY_ERROR);
            return ReturnCode.POLICY_VERIFY_ERROR;
        }


        List<PathAnalyzeDataRO> pathAnalyzeDataList = pathAnalyzeRO.getData();
        if(pathAnalyzeDataList == null || pathAnalyzeDataList.size() == 0) {
            logger.error("路径分析数据列表为空...");
            recommendTaskService.updatePathVerifyStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_RECOMMEND_VERIFY_ERROR);
            return ReturnCode.POLICY_VERIFY_ERROR;
        }
        PathAnalyzeDataRO data = pathAnalyzeDataList.get(0);
        List<PathInfoRO> pathInfoList = data.getPathList();
        if (pathInfoList == null || pathInfoList.size() == 0) {
            logger.error("路径信息列表为空...");
            recommendTaskService.updatePathVerifyStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_RECOMMEND_VERIFY_ERROR);
            return ReturnCode.POLICY_VERIFY_ERROR;
        }

        //更新路径验证状态
        int taskStatus = PolicyConstants.POLICY_INT_VERIFY_STATUS_PATH_FULLY_OPEN;
        boolean hasClosePath = false;
        for(PathInfoRO pathInfoRO : pathInfoList) {
            if (pathInfoRO.getPathStatus().equals("FULLY_CLOSED")) {
                hasClosePath = true;
            }
        }
        if(hasClosePath) {
            taskStatus = PolicyConstants.POLICY_INT_VERIFY_STATUS_PATH_NOT_OPEN;
        }
        recommendTaskService.updatePathPathStatus(taskDTO.getId(), taskStatus);

        //保存设备信息
        int index = 0;
        for(PathInfoRO pathInfo : pathInfoList) {
            List<PathDetailRO> pathDetailList = pathInfo.getDeviceDetails();
            for(PathDetailRO pathDetail:pathDetailList) {
                String uuid = pathDetail.getDeviceUuid();
                logger.info(String.format("获取设备(%s)相关策略详情", uuid));
                DeviceDetailRO deviceDetail = null;
                try {
                    deviceDetail = whaleService.getDeviceDetail(pathDetail);
                } catch(Exception e) {
                    logger.error(String.format("任务(%d)[%s]路径%d验证获取设备详情出错", taskDTO.getTaskId(), taskDTO.getTheme(), taskDTO.getId()), e);
                }

                if(deviceDetail!= null) {
                    PathDeviceDetailEntity entity = new PathDeviceDetailEntity();
                    entity.setPathInfoId(taskDTO.getId());
                    entity.setDeviceUuid(uuid);
                    entity.setIsVerifyData(PolicyConstants.POLICY_INT_PATH_VERIFY_DATA);
                    entity.setDeviceDetail(JSONObject.toJSONString(deviceDetail));
                    entity.setPathIndex(index);
                    recommendTaskService.savePathDeviceDetail(entity);
                }
            }
            index ++ ;
        }

        recommendTaskService.updatePathVerifyStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_RECOMMEND_VERIFY_SUCESS);
        recommendTaskService.saveVerifyDeitailPath(taskDTO.getId(), verificationDetailedPath);
//        recommendTaskService.updateTaskStatus(taskDTO.getTaskId(), PolicyConstants.POLICY_INT_STATUS_VERIFY_DONE);

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 检测采集状态
     * @param deviceUuidList
     * @return
     */
    List<String> checkingGatherStatus(List<String> deviceUuidList) {
        try{
            sleep(1000);
        } catch (Exception e) {
            logger.error("未知异常：", e);
        }
        boolean gathered = false;
        List<String> failedList = null;
        while(!gathered) {
            //先等待，再检测
            try {
                sleep (5000);
            } catch(Exception e) {
                logger.error("未知异常！", e);
            }
            failedList = this.getGatherFailedDevices(deviceUuidList);
            //失败列表
            if(failedList != null) {
                gathered = true;
            }
        }
        return failedList;
    }

    /**
     * 检测设备状态
     * @param deviceUuidList 设备UUID列表
     * @return 采集失败列表
     */
    @Override
    public List<String> getGatherFailedDevices(List<String> deviceUuidList) {
        logger.info("检测设备采集状态...");
        List<String> failedDeviceList = new ArrayList<>();

        boolean finished = true;
        for(String deviceUuid: deviceUuidList) {
            int status = recommendTaskService.getGatherStateByDeviceUuid(deviceUuid);
            logger.info(String.format("设备(%s)状态为:%d...", deviceUuid, status));
            if(status < 0) {
                finished = false;
            } else if (status == 2) {
                logger.info("设备采集失败...");
                failedDeviceList.add(deviceUuid);
            }
        }

        //没有完成，则返回null
        if(!finished) {
            return null;
        }

        return failedDeviceList;
    }

    @Override
    public boolean hasGatheringDevices(List<String> deviceUuidList) {
        logger.info("检测设备采集状态...");

        boolean finished = true;
        for (String deviceUuid : deviceUuidList) {
            int status = recommendTaskService.getGatherStateByDeviceUuid(deviceUuid);
            logger.info(String.format("设备(%s)状态为:%d...", deviceUuid, status));
            if (status < 0) {
                finished = false;
            }
        }

        // 如果存在采集中的 就返回true
        if (!finished) {
            return true;
        }
        return false;
    }



    private void setPathInfoStatus(PathInfoTaskDTO task, Integer gatherStatus, Integer verifyStatus, Integer pathStatus) {
        PathInfoEntity pathInfoEntity = new PathInfoEntity();
        pathInfoEntity.setId(task.getId());
        pathInfoEntity.setGatherStatus(gatherStatus);
        pathInfoEntity.setVerifyStatus(verifyStatus);
        pathInfoEntity.setPathStatus(pathStatus);
        //设置为空因为这些属性默认值不为空
        pathInfoEntity.setAccessAnalyzeStatus(null);
        pathInfoEntity.setAnalyzeStatus(null);
        pathInfoEntity.setAdviceStatus(null);
        pathInfoEntity.setCheckStatus(null);
        pathInfoEntity.setRiskStatus(null);
        pathInfoEntity.setCmdStatus(null);
        pathInfoEntity.setPushStatus(null);
        recommendTaskService.updatePathStatus(pathInfoEntity);
    }

    /**
     * 设置路径任务采集状态
     * @param task 路径任务
     * @param gatherStatus 采集状态
     */
    private void setPathInfoGatherStatus(PathInfoTaskDTO task, Integer gatherStatus) {
        setPathInfoStatus(task, gatherStatus, null, null);
    }

    /**
     * 设置路径任务验证状态
     * @param task 路径任务
     * @param verifyStatus
     */
    private void setPathInfoVerifyStatus(PathInfoTaskDTO task, Integer verifyStatus) {
        setPathInfoStatus(task, null, verifyStatus, null);
    }
}
