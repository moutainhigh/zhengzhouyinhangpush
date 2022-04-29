package com.abtnetworks.totems.recommend.manager.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.config.VmwareInterfaceStatusConfig;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.constants.*;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.DNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.external.vo.DeviceDetailRunVO;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.push.dao.mysql.PushRecommendTaskExpandMapper;
import com.abtnetworks.totems.push.dto.PushStatus;
import com.abtnetworks.totems.push.dto.StaticRoutingDTO;
import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.push.vo.PushTaskVO;
import com.abtnetworks.totems.recommend.dao.mysql.CheckResultMapper;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.MergedPolicyMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.PathDetailMapper;
import com.abtnetworks.totems.recommend.dao.mysql.PathDeviceDetailMapper;
import com.abtnetworks.totems.recommend.dao.mysql.PathInfoMapper;
import com.abtnetworks.totems.recommend.dao.mysql.PolicyRecommendCredentialMapper;
import com.abtnetworks.totems.recommend.dao.mysql.PolicyRiskMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendPolicyMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskCheckMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RiskRuleInfoMapper;
import com.abtnetworks.totems.recommend.dto.global.VmwareSdnBusinessDTO;
import com.abtnetworks.totems.recommend.dto.push.TaskStatusBranchLevelsDTO;
import com.abtnetworks.totems.recommend.dto.recommend.IpTermsExtendDTO;
import com.abtnetworks.totems.recommend.dto.task.SearchRecommendTaskDTO;
import com.abtnetworks.totems.recommend.entity.CheckResultEntity;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.DNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.DeviceDimension;
import com.abtnetworks.totems.recommend.entity.NatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.PathDetailEntity;
import com.abtnetworks.totems.recommend.entity.PathDeviceDetailEntity;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.entity.PolicyRiskEntity;
import com.abtnetworks.totems.recommend.entity.PushAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskCheckEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.RiskRuleDetailEntity;
import com.abtnetworks.totems.recommend.entity.RiskRuleInfoEntity;
import com.abtnetworks.totems.recommend.entity.SNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.StaticNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.ExternalManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.GlobalRecommendService;
import com.abtnetworks.totems.recommend.vo.BatchTaskVO;
import com.abtnetworks.totems.recommend.vo.PathDetailVO;
import com.abtnetworks.totems.recommend.vo.PolicyRecommendSecurityPolicyVO;
import com.abtnetworks.totems.recommend.vo.PolicyTaskDetailVO;
import com.abtnetworks.totems.recommend.vo.PolicyVO;
import com.abtnetworks.totems.recommend.vo.RecommendPolicyVO;
import com.abtnetworks.totems.recommend.vo.TaskStatusVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.ro.ObjectDetailRO;
import com.abtnetworks.totems.whale.baseapi.service.IpServiceNameRefClient;
import com.abtnetworks.totems.whale.common.CommonRangeIntegerDTO;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.abtnetworks.totems.whale.model.ro.IPItemRO;
import com.abtnetworks.totems.whale.model.ro.NatClauseRO;
import com.abtnetworks.totems.whale.model.ro.PortSpecRO;
import com.abtnetworks.totems.whale.policy.dto.FilterListsRuleSearchDTO;
import com.abtnetworks.totems.whale.policy.dto.IpTermsDTO;
import com.abtnetworks.totems.whale.policy.dto.JsonQueryDTO;
import com.abtnetworks.totems.whale.policy.ro.DeviceDetailRO;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultDataRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultRO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static com.abtnetworks.totems.common.enums.PolicyTypeEnum.SYSTEM__NAT_LIST;

@Service
public class RecommendTaskManagerImpl implements RecommendTaskManager {

    private static Logger logger = Logger.getLogger(RecommendTaskManagerImpl.class);

    private static final String NO_CONTENT_MARK = "--";

    private static final String UNKNOWN_DEVICE = "未知设备";

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Autowired
    private PolicyRecommendCredentialMapper credentialMapper;

    @Autowired
    private RiskRuleInfoMapper riskMapper;

    ////////////////////////////新Mapper
    @Autowired
    private PathInfoMapper pathInfoMapper;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;



    @Autowired
    private PathDetailMapper pathDetailMapper;

    @Autowired
    private PathDeviceDetailMapper pathDeviceDetailMapper;

    @Autowired
    private PolicyRiskMapper policyRiskMapper;

    @Autowired
    private RecommendPolicyMapper recommendPolicyMapper;

    @Autowired
    private CheckResultMapper checkResultMapper;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEditableMapper;

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @Autowired
    private MergedPolicyMapper mergedPolicyMapper;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Autowired
    private RecommendTaskCheckMapper recommendTaskCheckMapper;

    @Autowired
    RemoteBranchService remoteBranchService;


    @Autowired
    private WhalePathAnalyzeClient client;

    @Value("${topology.whale-server-prefix}")
    private String whaleServerPrefix;

    @Autowired
    private IpServiceNameRefClient ipServiceNameRefClient;

    /**
     * 场景 Service
     */
    @Autowired
    public DisposalScenesService disposalScenesService;

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    CommandTaskManager commandTaskManager;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    PushTaskService pushTaskService;
    @Autowired
    GlobalRecommendService globalRecommendService;
    @Autowired
    VmwareInterfaceStatusConfig vmwareInterfaceStatusConfig;
    @Autowired
    private PushRecommendTaskExpandMapper pushRecommendTaskExpandMapper;

    @Autowired
    private ExternalManager externalManager;

    /**
     * 根据设备uuid获取设备采集状态
     *
     * @param uuid 设备uuid
     * @return 采集状态
     */
    @Override
    public int getGatherStateByDeviceUuid(String uuid) {
        return policyRecommendNodeMapper.getGatherStateByDeviceUuid(uuid);
    }




    @Override
    public NodeEntity getTheNodeByUuid(String deviceUuid) {
        return policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
    }



    @Override
    public String getDeviceModelNumber(String uuid) {
        return policyRecommendNodeMapper.getDeviceModelNumber(uuid);
    }



    @Override
    public String getDeviceName(String uuid) {
        return policyRecommendNodeMapper.getDeviceName(uuid);
    }

    @Override
    public Integer getDeviceGatherPort(String uuid) {
        return policyRecommendNodeMapper.getDeviceGatherPort(uuid);
    }


    @Override
    public List<RiskRuleInfoEntity> getRiskInfoBySecondSortId(int secondSortId) {
        return riskMapper.getRiskInfoBySecondSortId(secondSortId);
    }

    @Override
    public RiskRuleDetailEntity getRiskDetailEntityByRuleId(String ruleId) {
        List<RiskRuleDetailEntity> list = riskMapper.getRiskDetailByRuleId(ruleId);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<CommandTaskEditableEntity> listPolicyRecommendPolicyByTaskIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return null;
        }

        String[] arr = ids.split(",");
        List<Integer> taskIdList = new ArrayList<>();
        for (String id : arr) {
            taskIdList.add(Integer.valueOf(id));
        }
        List<CommandTaskEditableEntity> list = new ArrayList<>();
        for (Integer id : taskIdList) {
            List<CommandTaskEditableEntity> taskEntityList = commandTaskEditableMapper.selectByTaskId(id);
            list.addAll(taskEntityList);
        }
        return list;
    }

    @Override
    public String getRecommendZip(String ids, String pathPrefix) {
        //获取命令行
        List<CommandTaskEditableEntity> list = listPolicyRecommendPolicyByTaskIds(ids);
        if (list == null || list.size() == 0) {
            return null;
        }
        Map<String, List<String>> map = new LinkedHashMap<>();
        boolean hasFile = false;
        for (CommandTaskEditableEntity entity : list) {
            if (StringUtils.isBlank(entity.getCommandline())) {
                continue;
            }
            String deviceUuid = entity.getDeviceUuid();
            NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
            if (nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除，不下载该设备命令行...", deviceUuid));
                continue;
            }
            hasFile = true;
            String key = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());

            List<String> deviceList = map.get(key);
            if (deviceList == null) {
                deviceList = new ArrayList<>();
            }
            deviceList.add(entity.getCommandline());
            map.put(key, deviceList);
        }

        if (!hasFile) {
            return null;
        }

        //名称：时间戳 + 随机数
        Timestamp currnetTime = DateUtil.getCurrentTimestamp();
        String standardDateTime = DateUtil.getTimeStamp(currnetTime);

        //判断目录是否存在
        String destDirName = pathPrefix + "/" + CommonConstants.PUSH_COMMAND_FILE_DOWNLOAD_FOLDER;
        if (!new File(destDirName).exists()) {
            FileUtils.createDir(destDirName);
        }

        List<String> fileList = new ArrayList<>();

        //根据品牌，生成命令行文件，1个品牌，一个命令行
        for (Map.Entry entry : map.entrySet()) {
            String key = entry.getKey().toString();
            List<String> deviceList = (List<String>) entry.getValue();
            String filePath = destDirName + "/" + key + "_" + standardDateTime + ".txt";
            File file = new File(filePath);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException ex) {
                logger.error("文件创建异常", ex);
            }

            FileWriter fw = null;
            BufferedWriter bw = null;
            try {

                fw = new FileWriter(file, false);
                bw = new BufferedWriter(fw);
                for (String device : deviceList) {
                    //windows部分系统编译\n时，不能正确换行，用\r\n
                    if (device.contains("\n")) {
                        device = device.replace("\n", "\r\n");
                    }
                    bw.write(device);
                    //换行，换十行
                    bw.write("\r\n");
                    bw.write("\r\n");
                    bw.write("\r\n");
//                    bw.write("\r\n");
//                    bw.write("\r\n");
//                    bw.write("\r\n");
//                    bw.write("\r\n");
//                    bw.write("\r\n");
//                    bw.write("\r\n");
//                    bw.write("\r\n");
                }

            } catch (Exception e) {
                logger.error("写入txt文件异常", e);
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (Exception e) {
                    logger.error("流关闭异常", e);
                }

            }
            fileList.add(filePath);
        }

        if (!hasFile) {
            String filePath = destDirName + "/暂无记录.txt";
            File file = new File(filePath);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException ex) {
                logger.error("文件创建异常", ex);
            }
            fileList.add(filePath);
        }

        //将文件打成压缩包
        String zipFilePath = destDirName + "/策略命令行" + "_" + standardDateTime;
        try {
            ZipUtil.writeZip(fileList, zipFilePath);
        } catch (IOException ex) {
            logger.error("打压缩包异常: ", ex);
        }

        return zipFilePath + ".zip";
    }

    @Override
    public PageInfo<PathInfoEntity> getAnalyzePathInfoVOList(int taskId, int page, int psize) {
        PageHelper.startPage(page, psize);
        List<PathInfoEntity> list = pathInfoMapper.selectByTaskId(taskId);

        for (PathInfoEntity entity : list) {
            if (StringUtils.isNotBlank(entity.getService())) {
                JSONArray array = JSONArray.parseArray(entity.getService());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                for (ServiceDTO service : serviceList) {
                    service.setSrcPorts(AliStringUtils.isEmpty(service.getSrcPorts()) ? null : service.getSrcPorts());
                    service.setDstPorts(AliStringUtils.isEmpty(service.getDstPorts()) ? null : service.getDstPorts());
                    service.setProtocol(ProtocolUtils.getProtocolByString(service.getProtocol()));
                }
                entity.setService(JSONObject.toJSONString(serviceList));
            }
        }

        PageInfo<PathInfoEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PageInfo<PathInfoEntity> getVerifyPathInfoVOList(int taskId, int page, int psize) {
        return getAnalyzePathInfoVOList(taskId, page, psize);
    }

    @Override
    public PageInfo<RecommendTaskEntity> getTaskList(SearchRecommendTaskDTO searchRecommendTaskDTO) {
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getProtocol()) && "0".equals(searchRecommendTaskDTO.getProtocol())) {
            searchRecommendTaskDTO.setIsServiceAny(true);
        } else {
            searchRecommendTaskDTO.setIsServiceAny(false);
        }
        List<RecommendTaskEntity> list = searchRecommendTaskList(searchRecommendTaskDTO);
        List<PathInfoEntity> pathInfoEntityList = new ArrayList<>();

        if (ObjectUtils.isNotEmpty(list)) {
            List<Integer> taskIdList = list.stream().map(task -> task.getId()).collect(Collectors.toList());
            Map<String, Object> cond = new HashMap<>();
            cond.put("ids",taskIdList);
            pathInfoEntityList = pathInfoMapper.selectByIdList(cond);
        }

        for (RecommendTaskEntity entity : list) {
            if (!AliStringUtils.isEmpty(entity.getServiceList())) {
                JSONArray jsonArray = JSONArray.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = jsonArray.toJavaList(ServiceDTO.class);
                for (ServiceDTO service : serviceList) {
                    service.setProtocol(ProtocolUtils.getProtocolByString(service.getProtocol()));
                    service.setSrcPorts(AliStringUtils.isEmpty(service.getSrcPorts()) ? null : service.getSrcPorts());
                    service.setDstPorts(AliStringUtils.isEmpty(service.getDstPorts()) ? null : service.getDstPorts());
                }
                entity.setServiceList(JSONObject.toJSONString(serviceList));
            }

            if (ObjectUtils.isNotEmpty(pathInfoEntityList)) {
                String pathAnalyzeStatus = getTaskPathAnalyzeStatusByTaskId(entity.getId(), pathInfoEntityList);
                if (entity.getStatus() >= PolicyConstants.POLICY_INT_STATUS_VERIFYING) {
                    pathAnalyzeStatus = getTaskVerifyPathStatusByTaskId(entity.getId(), pathInfoEntityList);
                }
                if (StringUtils.isEmpty(pathAnalyzeStatus) && PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED != entity.getStatus()) {
                    //当仿真完成，且无路径时，列表显示无路径 返回前端格式 11(路径分析状态):1(数量)
                    entity.setPathAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_NONE + ":" + "1");
                } else {
                    entity.setPathAnalyzeStatus(pathAnalyzeStatus);
                }

            }
            //物理工单的状态，用于界面判断验证按钮是否支持点击
            entity.setNsStatus(entity.getStatus());
        }
        //云、物理工单状态合并
        if(vmwareInterfaceStatusConfig.isVmInterfaceAvailable()){
            mergeESWNTaskStatus(list);
        }
        PageInfo<RecommendTaskEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    //合并物理、云仿真任务状态
    void mergeESWNTaskStatus(List<RecommendTaskEntity> list){
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        for(RecommendTaskEntity entity:list) {
            if(entity.getWeTaskId()!=null){
                try{
                    VmwareSdnBusinessDTO vmwareSdnBusinessDTO = globalRecommendService.getWETaskByWETaskId(entity.getWeTaskId());
                    if(vmwareSdnBusinessDTO!=null) {
                        entity.setWeStatus(vmwareSdnBusinessDTO.getStatus());
                        if("7".equals(vmwareSdnBusinessDTO.getRemark())){
                            //云策略无需下发
                            entity.setWeStatus(7);
                        }
                        int weTaskStatus = vmwareSdnBusinessDTO.getStatus();
                        int nsTaskStatus = entity.getStatus();
                        int status = 0;
                        //纯东西向
                        if (entity.getTaskType() == PolicyConstants.IN2IN_INTERNET_RECOMMEND) {
                            status = weTaskStatus;
                        }else if (weTaskStatus == PolicyConstants.POLICY_INT_STATUS_PUSH_FINISHED && isAllPathExists(entity)) {
                            // 云下发成功，物理仿真成功(已开通)，总状态为下发成功
                            status = PolicyConstants.POLICY_INT_STATUS_PUSH_FINISHED;
                        }else if((weTaskStatus == PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE && "7".equals(vmwareSdnBusinessDTO.getRemark()))
                                || weTaskStatus == PolicyConstants.POLICY_INT_STATUS_PUSH_FINISHED){
                            //云下发完成或仿真完成无需下发，取物理状态
                            status = nsTaskStatus;
                        }else if(weTaskStatus == PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR || nsTaskStatus == PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR){
                            status = PolicyConstants.POLICY_INT_STATUS_SIMULATION_ERROR;
                        }else if(weTaskStatus == PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR || nsTaskStatus == PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR){
                            status = PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR;
                        } else {
                            //两边状态不一致时取较小的
                            status = weTaskStatus < nsTaskStatus ? weTaskStatus : nsTaskStatus;
                        }
                        entity.setStatus(status);
                    }
                }catch (Exception e){
                    logger.error(String.format("merge task status error,nsTaskId:%s,weTaskId:%s",entity.getId(),entity.getWeTaskId()));
                }
            }
        }
    }


    boolean isAllPathExists(RecommendTaskEntity entity){
        boolean flag = true;
        try {
            String pathAnalyzeStatus = entity.getPathAnalyzeStatus();
            //是否所有的路径都是已开通
            if (StringUtils.isNotEmpty(pathAnalyzeStatus)) {
                String[] args = pathAnalyzeStatus.split(",");
                for (String arg : args) {
                    String pathState = arg.split(":")[0];
                    if (PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS != Integer.parseInt(pathState)) {
                        flag = false;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("物理/云状态合并失败,nsTask:"+JSON.toJSONString(entity));
            return false;
        }
        return flag;
    }








    @Override
    public PageInfo<PolicyTaskDetailVO> getNatPolicyTaskList(String theme, String type, int page, int psize, String taskIds, Integer id, String userName,String deviceUUID,String status , Authentication authentication) {
        String branchLevel = remoteBranchService.likeBranch(authentication.getName());
        List<RecommendTaskEntity> list = searchNatTaskList(theme, null, userName, null, null, null, null, null, status,
                type, page, psize, taskIds, id,branchLevel,deviceUUID);
        PageInfo<RecommendTaskEntity> originalPageInfo = new PageInfo<>(list);
        List<PolicyTaskDetailVO> policyList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PolicyTaskDetailVO policyDetailVO = new PolicyTaskDetailVO();
            policyDetailVO.setUserName(entity.getUserName());
            policyDetailVO.setCreateTime(entity.getCreateTime());
            policyDetailVO.setTaskId(entity.getId());
            policyDetailVO.setPolicyName(entity.getTheme());
            policyDetailVO.setDeviceName("Unknown Device");
            policyDetailVO.setId(entity.getId());
            policyDetailVO.setPostSrcIpSystem(entity.getPostSrcIpSystem());
            policyDetailVO.setPostDstIpSystem(entity.getPostDstIpSystem());
            policyDetailVO.setPushStatus(entity.getStatus());
            if (StringUtils.isNotBlank(entity.getServiceList())) {
                JSONArray array = JSONObject.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                if (serviceList.size() > 0) {
                    for (ServiceDTO serviceDTO : serviceList) {
                        serviceDTO.setProtocol(ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                    }
                    policyDetailVO.setService(JSONObject.toJSONString(serviceList));
                } else {
                    ServiceDTO postService = new ServiceDTO();
                    postService.setProtocol("any");
                    serviceList.add(postService);
                    policyDetailVO.setService(JSONObject.toJSONString(serviceList));
                }
            } else {
                ServiceDTO postService = new ServiceDTO();
                postService.setProtocol("any");
                List<ServiceDTO> serviceList = new ArrayList<>();
                serviceList.add(postService);
                policyDetailVO.setService(JSONObject.toJSONString(serviceList));
            }
            if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                logger.debug("static nat additionalInfo is " + JSONObject.toJSONString(entity.getAdditionInfo()));
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    StaticNatAdditionalInfoEntity staticNatAdditionalInfoEntity = object.toJavaObject(StaticNatAdditionalInfoEntity.class);
                    policyDetailVO.setPublicAddress(staticNatAdditionalInfoEntity.getGlobalAddress());
                    policyDetailVO.setPrivateAddress(staticNatAdditionalInfoEntity.getInsideAddress());
                    policyDetailVO.setPublicPort(staticNatAdditionalInfoEntity.getGlobalPort());
                    policyDetailVO.setPrivatePort(staticNatAdditionalInfoEntity.getInsidePort());
                    String protocol = staticNatAdditionalInfoEntity.getProtocol();
                    String deviceUuid = staticNatAdditionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);
                    if (AliStringUtils.isEmpty(protocol)) {
                        policyDetailVO.setProtocol("any");
                    } else {
                        policyDetailVO.setProtocol(ProtocolUtils.getProtocolByString(protocol));
                    }

                    policyDetailVO.setSrcDomain(formatZoneItfString(staticNatAdditionalInfoEntity.getFromZone(), staticNatAdditionalInfoEntity.getInDevItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(staticNatAdditionalInfoEntity.getToZone(), staticNatAdditionalInfoEntity.getOutDevItf()));
                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                logger.debug("snat additionalInfo is " + JSONObject.toJSONString(entity.getAdditionInfo()));
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    SNatAdditionalInfoEntity sNatAdditionalInfoEntity = object.toJavaObject(SNatAdditionalInfoEntity.class);

                    policyDetailVO.setSrcDomain(formatZoneItfString(sNatAdditionalInfoEntity.getSrcZone(), sNatAdditionalInfoEntity.getSrcItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(sNatAdditionalInfoEntity.getDstZone(), sNatAdditionalInfoEntity.getDstItf()));
                    policyDetailVO.setPreSrcIp(entity.getSrcIp());
                    policyDetailVO.setPostSrcIp(sNatAdditionalInfoEntity.getPostIpAddress());
                    String deviceUuid = sNatAdditionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);
                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    DNatAdditionalInfoEntity dnatAdditionalInfoEntity = object.toJavaObject(DNatAdditionalInfoEntity.class);

                    policyDetailVO.setSrcDomain(formatZoneItfString(dnatAdditionalInfoEntity.getSrcZone(), dnatAdditionalInfoEntity.getSrcItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(dnatAdditionalInfoEntity.getDstZone(), dnatAdditionalInfoEntity.getDstItf()));
                    policyDetailVO.setPreDstIp(entity.getDstIp());
                    policyDetailVO.setPostDstIp(dnatAdditionalInfoEntity.getPostIpAddress());
                    String deviceUuid = dnatAdditionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);

                    List<ServiceDTO> postServiceList = new ArrayList<>();
                    if (StringUtils.isNotBlank(entity.getServiceList())) {
                        JSONArray array = JSONObject.parseArray(entity.getServiceList());
                        List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                        if (serviceList.size() > 0) {
                            for (ServiceDTO serviceDTO : serviceList) {
                                ServiceDTO postService = new ServiceDTO();
                                String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
//                                postService.setProtocol(protocol);
                                if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                                    postService.setDstPorts(dnatAdditionalInfoEntity.getPostPort());
                                }
                                postServiceList.add(postService);
                            }
                        } else {
                            ServiceDTO postService = new ServiceDTO();
                            postService.setProtocol("any");
                            postService.setDstPorts(dnatAdditionalInfoEntity.getPostPort());
                            postServiceList.add(postService);
                        }
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    } else {
                        ServiceDTO postService = new ServiceDTO();
                        postService.setProtocol("any");
                        postService.setDstPorts(dnatAdditionalInfoEntity.getPostPort());
                        postServiceList.add(postService);
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    }

                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    NatAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(NatAdditionalInfoEntity.class);

                    policyDetailVO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getSrcItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getDstItf()));

                    policyDetailVO.setPreSrcIp(entity.getSrcIp());
                    policyDetailVO.setPostSrcIp(additionalInfoEntity.getPostSrcIp());

                    policyDetailVO.setPreDstIp(entity.getDstIp());
                    policyDetailVO.setPostDstIp(additionalInfoEntity.getPostDstIp());

                    String deviceUuid = additionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);

                    List<ServiceDTO> postServiceList = new ArrayList<>();
                    if (StringUtils.isNotBlank(entity.getServiceList())) {
                        JSONArray array = JSONObject.parseArray(entity.getServiceList());
                        List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                        if (serviceList.size() > 0) {
                            for (ServiceDTO serviceDTO : serviceList) {
                                ServiceDTO postService = new ServiceDTO();
                                String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                                postService.setProtocol(protocol);
                                if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                                    postService.setDstPorts(additionalInfoEntity.getPostPort());
                                }
                                postServiceList.add(postService);
                            }
                        } else {
                            ServiceDTO postService = new ServiceDTO();
                            postService.setProtocol("any");
                            postService.setDstPorts(additionalInfoEntity.getPostPort());
                            postServiceList.add(postService);
                        }
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    } else {
                        ServiceDTO postService = new ServiceDTO();
                        postService.setProtocol("any");
                        postService.setDstPorts(additionalInfoEntity.getPostPort());
                        postServiceList.add(postService);
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    }

                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                policyDetailVO.setUserName(entity.getUserName());
                PushRecommendTaskExpandEntity expandEntity = pushRecommendTaskExpandMapper.getByTaskId(entity.getId());
                if (null == expandEntity) {
                    logger.info(String.format("根据任务id:%d查询拓展数据为空", entity.getId()));
                    continue;
                }
                String deviceIp = "未知设备";
                //设备uuid
                String deviceUuid = expandEntity.getDeviceUuid();
                policyDetailVO.setDescription(entity.getDescription());
                if (StringUtils.isNotBlank(deviceUuid)) {
                    deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                            policyDetailVO.setDeviceName(nodeEntity.getDeviceName());
                        }
                    }
                }
                policyDetailVO.setDeviceIp(deviceIp);
                if (StringUtils.isNotEmpty(expandEntity.getStaticRoutingInfo())) {
                    StaticRoutingDTO staticRoutingDTO = JSONObject.toJavaObject(JSONObject.parseObject(expandEntity.getStaticRoutingInfo()), StaticRoutingDTO.class);
                    policyDetailVO.setSrcVirtualRouter(staticRoutingDTO.getSrcVirtualRouter());
                    policyDetailVO.setDstVirtualRouter(staticRoutingDTO.getDstVirtualRouter());
                    policyDetailVO.setNextHop(staticRoutingDTO.getNextHop());
                    policyDetailVO.setSubnetMask(staticRoutingDTO.getSubnetMask());
                    policyDetailVO.setOutInterface(staticRoutingDTO.getOutInterface());
                    policyDetailVO.setPriority(staticRoutingDTO.getPriority());
                    policyDetailVO.setManagementDistance(staticRoutingDTO.getManagementDistance());
                }
            }
            policyList.add(policyDetailVO);
        }
        PageInfo<PolicyTaskDetailVO> pageInfo = new PageInfo<>(policyList);
        pageInfo.setTotal(originalPageInfo.getTotal());
        pageInfo.setStartRow(originalPageInfo.getStartRow());
        pageInfo.setEndRow(originalPageInfo.getEndRow());
        pageInfo.setPageSize(originalPageInfo.getPageSize());
        pageInfo.setPageNum(originalPageInfo.getPageNum());
        return pageInfo;
    }

    @Override
    public List<PolicyTaskDetailVO> getNatTaskList(String theme, String type, String taskIds, Integer id, String userName,String deviceUUID,String startTime, String endTime, Authentication authentication) {
        String branchLevel = remoteBranchService.likeBranch(authentication.getName());
        List<RecommendTaskEntity> list = searchNatPolicyTaskList(theme, null, userName, null, null, null, null, null, null,
                startTime, endTime, type, taskIds, id,branchLevel,deviceUUID);
        List<PolicyTaskDetailVO> policyList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PolicyTaskDetailVO policyDetailVO = new PolicyTaskDetailVO();
            policyDetailVO.setUserName(entity.getUserName());
            policyDetailVO.setCreateTime(entity.getCreateTime());
            policyDetailVO.setTaskId(entity.getId());
            policyDetailVO.setPolicyName(entity.getTheme());
            policyDetailVO.setDeviceName("Unknown Device");
            policyDetailVO.setId(entity.getId());
            policyDetailVO.setPostSrcIpSystem(entity.getPostSrcIpSystem());
            policyDetailVO.setPostDstIpSystem(entity.getPostDstIpSystem());
            policyDetailVO.setPushStatus(entity.getStatus());
            if (StringUtils.isNotBlank(entity.getServiceList())) {
                JSONArray array = JSONObject.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                if (serviceList.size() > 0) {
                    for (ServiceDTO serviceDTO : serviceList) {
                        serviceDTO.setProtocol(ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                    }
                    policyDetailVO.setService(JSONObject.toJSONString(serviceList));
                } else {
                    ServiceDTO postService = new ServiceDTO();
                    postService.setProtocol("any");
                    serviceList.add(postService);
                    policyDetailVO.setService(JSONObject.toJSONString(serviceList));
                }
            } else {
                ServiceDTO postService = new ServiceDTO();
                postService.setProtocol("any");
                List<ServiceDTO> serviceList = new ArrayList<>();
                serviceList.add(postService);
                policyDetailVO.setService(JSONObject.toJSONString(serviceList));
            }
            if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                logger.debug("static nat additionalInfo is " + JSONObject.toJSONString(entity.getAdditionInfo()));
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    StaticNatAdditionalInfoEntity staticNatAdditionalInfoEntity = object.toJavaObject(StaticNatAdditionalInfoEntity.class);
                    policyDetailVO.setPublicAddress(staticNatAdditionalInfoEntity.getGlobalAddress());
                    policyDetailVO.setPrivateAddress(staticNatAdditionalInfoEntity.getInsideAddress());
                    policyDetailVO.setPublicPort(staticNatAdditionalInfoEntity.getGlobalPort());
                    policyDetailVO.setPrivatePort(staticNatAdditionalInfoEntity.getInsidePort());
                    String protocol = staticNatAdditionalInfoEntity.getProtocol();
                    String deviceUuid = staticNatAdditionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);
                    if (AliStringUtils.isEmpty(protocol)) {
                        policyDetailVO.setProtocol("any");
                    } else {
                        policyDetailVO.setProtocol(ProtocolUtils.getProtocolByString(protocol));
                    }

                    policyDetailVO.setSrcDomain(formatZoneItfString(staticNatAdditionalInfoEntity.getFromZone(), staticNatAdditionalInfoEntity.getInDevItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(staticNatAdditionalInfoEntity.getToZone(), staticNatAdditionalInfoEntity.getOutDevItf()));
                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                logger.debug("snat additionalInfo is " + JSONObject.toJSONString(entity.getAdditionInfo()));
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    SNatAdditionalInfoEntity sNatAdditionalInfoEntity = object.toJavaObject(SNatAdditionalInfoEntity.class);

                    policyDetailVO.setSrcDomain(formatZoneItfString(sNatAdditionalInfoEntity.getSrcZone(), sNatAdditionalInfoEntity.getSrcItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(sNatAdditionalInfoEntity.getDstZone(), sNatAdditionalInfoEntity.getDstItf()));
                    policyDetailVO.setPreSrcIp(entity.getSrcIp());
                    policyDetailVO.setPostSrcIp(sNatAdditionalInfoEntity.getPostIpAddress());
                    String deviceUuid = sNatAdditionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);
                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    DNatAdditionalInfoEntity dnatAdditionalInfoEntity = object.toJavaObject(DNatAdditionalInfoEntity.class);

                    policyDetailVO.setSrcDomain(formatZoneItfString(dnatAdditionalInfoEntity.getSrcZone(), dnatAdditionalInfoEntity.getSrcItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(dnatAdditionalInfoEntity.getDstZone(), dnatAdditionalInfoEntity.getDstItf()));
                    policyDetailVO.setPreDstIp(entity.getDstIp());
                    policyDetailVO.setPostDstIp(dnatAdditionalInfoEntity.getPostIpAddress());
                    String deviceUuid = dnatAdditionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);

                    List<ServiceDTO> postServiceList = new ArrayList<>();
                    if (StringUtils.isNotBlank(entity.getServiceList())) {
                        JSONArray array = JSONObject.parseArray(entity.getServiceList());
                        List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                        if (serviceList.size() > 0) {
                            for (ServiceDTO serviceDTO : serviceList) {
                                ServiceDTO postService = new ServiceDTO();
                                String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
//                                postService.setProtocol(protocol);
                                if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                                    postService.setDstPorts(dnatAdditionalInfoEntity.getPostPort());
                                }
                                postServiceList.add(postService);
                            }
                        } else {
                            ServiceDTO postService = new ServiceDTO();
                            postService.setProtocol("any");
                            postService.setDstPorts(dnatAdditionalInfoEntity.getPostPort());
                            postServiceList.add(postService);
                        }
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    } else {
                        ServiceDTO postService = new ServiceDTO();
                        postService.setProtocol("any");
                        postService.setDstPorts(dnatAdditionalInfoEntity.getPostPort());
                        postServiceList.add(postService);
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    }

                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
                if (object != null) {
                    NatAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(NatAdditionalInfoEntity.class);

                    policyDetailVO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getSrcItf()));
                    policyDetailVO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getDstItf()));

                    policyDetailVO.setPreSrcIp(entity.getSrcIp());
                    policyDetailVO.setPostSrcIp(additionalInfoEntity.getPostSrcIp());

                    policyDetailVO.setPreDstIp(entity.getDstIp());
                    policyDetailVO.setPostDstIp(additionalInfoEntity.getPostDstIp());

                    String deviceUuid = additionalInfoEntity.getDeviceUuid();
                    String deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                        }
                    }
                    policyDetailVO.setDeviceIp(deviceIp);

                    List<ServiceDTO> postServiceList = new ArrayList<>();
                    if (StringUtils.isNotBlank(entity.getServiceList())) {
                        JSONArray array = JSONObject.parseArray(entity.getServiceList());
                        List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                        if (serviceList.size() > 0) {
                            for (ServiceDTO serviceDTO : serviceList) {
                                ServiceDTO postService = new ServiceDTO();
                                String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                                postService.setProtocol(protocol);
                                if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                                    postService.setDstPorts(additionalInfoEntity.getPostPort());
                                }
                                postServiceList.add(postService);
                            }
                        } else {
                            ServiceDTO postService = new ServiceDTO();
                            postService.setProtocol("any");
                            postService.setDstPorts(additionalInfoEntity.getPostPort());
                            postServiceList.add(postService);
                        }
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    } else {
                        ServiceDTO postService = new ServiceDTO();
                        postService.setProtocol("any");
                        postService.setDstPorts(additionalInfoEntity.getPostPort());
                        postServiceList.add(postService);
                        policyDetailVO.setPostService(JSONObject.toJSONString(postServiceList));
                    }

                }
            } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING)) {
                BeanUtils.copyProperties(entity, policyDetailVO);
                policyDetailVO.setPushStatus(entity.getStatus());
                policyDetailVO.setUserName(entity.getUserName());
                PushRecommendTaskExpandEntity expandEntity = pushRecommendTaskExpandMapper.getByTaskId(entity.getId());
                if (null == expandEntity) {
                    logger.info(String.format("根据任务id:%d查询拓展数据为空", entity.getId()));
                    continue;
                }
                String deviceIp = "未知设备";
                //设备uuid
                String deviceUuid = expandEntity.getDeviceUuid();
                policyDetailVO.setDescription(entity.getDescription());
                if (StringUtils.isNotBlank(deviceUuid)) {
                    deviceIp = String.format("未知设备(%s)", deviceUuid);
                    if (deviceUuid != null) {
                        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                        if (nodeEntity != null) {
                            deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                            policyDetailVO.setDeviceName(nodeEntity.getDeviceName());
                        }
                    }
                }
                policyDetailVO.setDeviceIp(deviceIp);
                if (StringUtils.isNotEmpty(expandEntity.getStaticRoutingInfo())) {
                    StaticRoutingDTO staticRoutingDTO = JSONObject.toJavaObject(JSONObject.parseObject(expandEntity.getStaticRoutingInfo()), StaticRoutingDTO.class);
                    policyDetailVO.setSrcVirtualRouter(staticRoutingDTO.getSrcVirtualRouter());
                    policyDetailVO.setDstVirtualRouter(staticRoutingDTO.getDstVirtualRouter());
                    policyDetailVO.setNextHop(staticRoutingDTO.getNextHop());
                    policyDetailVO.setSubnetMask(staticRoutingDTO.getSubnetMask());
                    policyDetailVO.setOutInterface(staticRoutingDTO.getOutInterface());
                    policyDetailVO.setPriority(staticRoutingDTO.getPriority());
                    policyDetailVO.setManagementDistance(staticRoutingDTO.getManagementDistance());
                }
            }
            policyList.add(policyDetailVO);
        }
        return policyList;
    }


    @Override
    public PageInfo<PolicyTaskDetailVO> getSecurityPolicyTaskList(String theme, int page, int psize, String userName,String deviceUUID,String status, Authentication authentication) {
        String branchLevel = remoteBranchService.likeBranch(authentication.getName());
        List<RecommendTaskEntity> list = searchTaskList(theme, null, userName, null, null, null, null, null, status,
                String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED), page, psize,branchLevel,deviceUUID);
        PageInfo<RecommendTaskEntity> originalPageInfo = new PageInfo<>(list);
        List<PolicyTaskDetailVO> policyList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PolicyTaskDetailVO policyDetailVO = new PolicyTaskDetailVO();
            String additionalInfo = entity.getAdditionInfo();
            PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
            if (additionalInfo != null) {
                JSONObject object = JSONObject.parseObject(additionalInfo);
                additionalInfoEntity = JSONObject.toJavaObject(object, PushAdditionalInfoEntity.class);
            }

            String deviceIp = "未知设备";
            //设备uuid
            String deviceUuid = additionalInfoEntity.getDeviceUuid();
            if (StringUtils.isNotBlank(deviceUuid)) {
                deviceIp = String.format("未知设备(%s)", deviceUuid);
                if (deviceUuid != null) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                    if (nodeEntity != null) {
                        deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }
                }
            } else {
                //场景uuid
                String scenesUuid = additionalInfoEntity.getScenesUuid();
                if (StringUtils.isNotBlank(scenesUuid)) {
                    DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(scenesUuid);
                    if (scenesEntity == null) {
                        logger.error(String.format("场景UUID：%s 查询场景不存在。", scenesUuid));
                    } else {
                        policyDetailVO.setScenesUuid(scenesUuid);
                        policyDetailVO.setScenesName(scenesEntity.getName());
                        deviceIp = String.format("场景：%s", scenesEntity.getName());
                    }
                }
            }

            policyDetailVO.setTaskId(entity.getId());
            policyDetailVO.setCreateTime(entity.getCreateTime());
            policyDetailVO.setUserName(entity.getUserName());
            policyDetailVO.setDeviceIp(deviceIp);

            policyDetailVO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getInDevItf()));
            policyDetailVO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getOutDevItf()));
            policyDetailVO.setAction(additionalInfoEntity.getAction());
            policyDetailVO.setSrcIp(entity.getSrcIp());
            policyDetailVO.setDstIp(entity.getDstIp());
            policyDetailVO.setPolicyName(entity.getTheme());
            policyDetailVO.setSrcIpSystem(entity.getSrcIpSystem());
            policyDetailVO.setDstIpSystem(entity.getDstIpSystem());
            policyDetailVO.setPushStatus(entity.getStatus());
            if (entity.getStatus() != null && entity.getEndTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                policyDetailVO.setTime(String.format("%s - %s", sdf.format(entity.getStartTime()), sdf.format(entity.getEndTime())));
            }
            policyDetailVO.setDescription(entity.getDescription());
            policyDetailVO.setRemarks(entity.getRemarks());
            policyDetailVO.setIdleTimeout(null == entity.getIdleTimeout() ? null : String.valueOf(entity.getIdleTimeout()));
            if (StringUtils.isNotBlank(entity.getServiceList())) {
                JSONArray array = JSONObject.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                for (ServiceDTO serviceDTO : serviceList) {
                    serviceDTO.setProtocol(ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                }
                policyDetailVO.setService(JSONObject.toJSONString(serviceList));
            }
            policyList.add(policyDetailVO);
        }
        PageInfo<PolicyTaskDetailVO> pageInfo = new PageInfo<>(policyList);
        pageInfo.setTotal(originalPageInfo.getTotal());
        pageInfo.setStartRow(originalPageInfo.getStartRow());
        pageInfo.setEndRow(originalPageInfo.getEndRow());
        pageInfo.setPageSize(originalPageInfo.getPageSize());
        pageInfo.setPageNum(originalPageInfo.getPageNum());
        return pageInfo;
    }

    @Override
    public PageInfo<PolicyTaskDetailVO> getCustomizeCmdTaskList(String theme, int page, int psize, String userName,String deviceUUID,Integer status, Authentication authentication) {
        String branchLevel = remoteBranchService.likeBranch(authentication.getName());
        List<RecommendTaskEntity> list = searchTaskList(theme, null, userName, null, null, null, null, null, null,
                String.valueOf(PolicyConstants.CUSTOMIZE_CMD_PUSH), page, psize,branchLevel,deviceUUID);
        List<PolicyTaskDetailVO> policyList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PolicyTaskDetailVO policyDetailVO = new PolicyTaskDetailVO();
            String additionalInfo = entity.getAdditionInfo();
            PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
            if (additionalInfo != null) {
                JSONObject object = JSONObject.parseObject(additionalInfo);
                additionalInfoEntity = JSONObject.toJavaObject(object, PushAdditionalInfoEntity.class);
            }

            String deviceIp = "未知设备";
            //设备uuid
            String deviceUuid = additionalInfoEntity.getDeviceUuid();
            if (StringUtils.isNotBlank(deviceUuid)) {
                deviceIp = String.format("未知设备(%s)", deviceUuid);
                if (deviceUuid != null) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                    if (nodeEntity != null) {
                        deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }
                }
            } else {
                //场景uuid
                String scenesUuid = additionalInfoEntity.getScenesUuid();
                if (StringUtils.isNotBlank(scenesUuid)) {
                    DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(scenesUuid);
                    if (scenesEntity == null) {
                        logger.error(String.format("场景UUID：%s 查询场景不存在。", scenesUuid));
                    } else {
                        policyDetailVO.setScenesUuid(scenesUuid);
                        policyDetailVO.setScenesName(scenesEntity.getName());
                        deviceIp = String.format("场景：%s", scenesEntity.getName());
                    }
                }
            }

            policyDetailVO.setTaskId(entity.getId());
            policyDetailVO.setCreateTime(entity.getCreateTime());
            policyDetailVO.setUserName(entity.getUserName());
            policyDetailVO.setDeviceIp(deviceIp);

            policyDetailVO.setPolicyName(entity.getTheme());

            List<CommandTaskEditableEntity> taskCollect = commandTaskEditableMapper.selectByTaskId(entity.getId());
            int pushStatusInTaskList = getPushStatusInTaskList(taskCollect);
            policyDetailVO.setPushStatus(pushStatusInTaskList);
            if (null != status) {
                if (PushStatusConstans.PUSH_STATUS_NOT_START == status && PushStatusConstans.PUSH_STATUS_NOT_START == pushStatusInTaskList) {
                    policyList.add(policyDetailVO);
                } else if (PushStatusConstans.PUSH_STATUS_PUSHING == status &&
                        (PushStatusConstans.PUSH_STATUS_PUSHING == pushStatusInTaskList || PushStatusConstans.PUSH_INT_PUSH_QUEUED == pushStatusInTaskList)) {
                    policyList.add(policyDetailVO);
                } else if (PushStatusConstans.PUSH_STATUS_FINISHED == status
                        && (PushStatusConstans.PUSH_STATUS_FINISHED == pushStatusInTaskList || PushStatusConstans.PUSH_STATUS_FAILED == pushStatusInTaskList || PushStatusConstans.PUSH_STATUS_PART_FINISHED == pushStatusInTaskList)) {
                    policyList.add(policyDetailVO);
                }
            } else {
                policyList.add(policyDetailVO);
            }
        }

        PageInfo<PolicyTaskDetailVO> pageInfo = new PageInfo<>(policyList);
        return pageInfo;
    }

    @Override
    public List<PolicyTaskDetailVO> getSecurityTaskList(String theme, String userName,String deviceUUID, String startTime, String endTime, Authentication authentication) {
        String branchLevel = remoteBranchService.likeBranch(authentication.getName());
        List<RecommendTaskEntity> list = searchPolicyTaskList(theme, null, userName, null, null, null, null, null, null,
                String.valueOf(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED), startTime, endTime, branchLevel,deviceUUID);
        List<PolicyTaskDetailVO> policyList = new ArrayList<>();
        for (RecommendTaskEntity entity : list) {
            PolicyTaskDetailVO policyDetailVO = new PolicyTaskDetailVO();
            String additionalInfo = entity.getAdditionInfo();
            PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
            if (additionalInfo != null) {
                JSONObject object = JSONObject.parseObject(additionalInfo);
                additionalInfoEntity = JSONObject.toJavaObject(object, PushAdditionalInfoEntity.class);
            }

            String deviceIp = "未知设备";
            //设备uuid
            String deviceUuid = additionalInfoEntity.getDeviceUuid();
            if (StringUtils.isNotBlank(deviceUuid)) {
                deviceIp = String.format("未知设备(%s)", deviceUuid);
                if (deviceUuid != null) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                    if (nodeEntity != null) {
                        deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }
                }
            } else {
                //场景uuid
                String scenesUuid = additionalInfoEntity.getScenesUuid();
                if (StringUtils.isNotBlank(scenesUuid)) {
                    DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(scenesUuid);
                    if (scenesEntity == null) {
                        logger.error(String.format("场景UUID：%s 查询场景不存在。", scenesUuid));
                    } else {
                        policyDetailVO.setScenesUuid(scenesUuid);
                        policyDetailVO.setScenesName(scenesEntity.getName());
                        deviceIp = String.format("场景：%s", scenesEntity.getName());
                    }
                }
            }

            policyDetailVO.setTaskId(entity.getId());
            policyDetailVO.setCreateTime(entity.getCreateTime());
            policyDetailVO.setUserName(entity.getUserName());
            policyDetailVO.setDeviceIp(deviceIp);

            policyDetailVO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getInDevItf()));
            policyDetailVO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getOutDevItf()));
            policyDetailVO.setAction(additionalInfoEntity.getAction());
            policyDetailVO.setSrcIp(entity.getSrcIp());
            policyDetailVO.setDstIp(entity.getDstIp());
            policyDetailVO.setPolicyName(entity.getTheme());
            policyDetailVO.setSrcIpSystem(entity.getSrcIpSystem());
            policyDetailVO.setDstIpSystem(entity.getDstIpSystem());
            policyDetailVO.setPushStatus(entity.getStatus());
            if (entity.getStatus() != null && entity.getEndTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                policyDetailVO.setTime(String.format("%s - %s", sdf.format(entity.getStartTime()), sdf.format(entity.getEndTime())));
            }
            policyDetailVO.setDescription(entity.getDescription());
            policyDetailVO.setRemarks(entity.getRemarks());
            if (StringUtils.isNotBlank(entity.getServiceList())) {
                JSONArray array = JSONObject.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                for (ServiceDTO serviceDTO : serviceList) {
                    serviceDTO.setProtocol(ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                }
                policyDetailVO.setService(JSONObject.toJSONString(serviceList));
            }
            policyList.add(policyDetailVO);
        }
        return policyList;
    }


    @Override
    public TaskStatusVO getTaskStatusByTaskId(int taskId) {
        List<PathInfoEntity> list = pathInfoMapper.selectByTaskId(taskId);
        TaskStatusVO taskStatusVO = new TaskStatusVO();
        for (PathInfoEntity pathInfoEntity : list) {
            //若为空，则说明遍历第一个对象，需要将得到的值赋值到VO
            if (taskStatusVO.getAnalyzeStatus() == null) {
                BeanUtils.copyProperties(pathInfoEntity, taskStatusVO);
            } else {
                //比较每条路径分析的状态结果，获取最坏的结果进行展示
                if (taskStatusVO.getAccessAnalyzeStatus() < pathInfoEntity.getAccessAnalyzeStatus()) {
                    taskStatusVO.setAccessAnalyzeStatus(pathInfoEntity.getAccessAnalyzeStatus());
                }
                if (taskStatusVO.getAdviceStatus() < pathInfoEntity.getAdviceStatus()) {
                    taskStatusVO.setAdviceStatus(pathInfoEntity.getAdviceStatus());
                }
                if (taskStatusVO.getCheckStatus() < pathInfoEntity.getCheckStatus()) {
                    taskStatusVO.setCheckStatus(pathInfoEntity.getCheckStatus());
                }
                if (taskStatusVO.getRiskStatus() < pathInfoEntity.getRiskStatus()) {
                    taskStatusVO.setRiskStatus(pathInfoEntity.getRiskStatus());
                }
                if (taskStatusVO.getCmdStatus() < pathInfoEntity.getCmdStatus()) {
                    taskStatusVO.setCmdStatus(pathInfoEntity.getCmdStatus());
                }
                if (taskStatusVO.getPushStatus() < pathInfoEntity.getPushStatus()) {
                    taskStatusVO.setPushStatus(pathInfoEntity.getPushStatus());
                }
                if (taskStatusVO.getGatherStatus() < pathInfoEntity.getGatherStatus()) {
                    taskStatusVO.setGatherStatus(pathInfoEntity.getGatherStatus());
                }
                if (taskStatusVO.getAccessAnalyzeStatus() < pathInfoEntity.getAccessAnalyzeStatus()) {
                    taskStatusVO.setAccessAnalyzeStatus(pathInfoEntity.getAccessAnalyzeStatus());
                }
                if (taskStatusVO.getVerifyStatus() < pathInfoEntity.getVerifyStatus()) {
                    taskStatusVO.setVerifyStatus(pathInfoEntity.getVerifyStatus());
                }
            }
        }
        return taskStatusVO;
    }

    @Override
    public PathDetailVO getPathDetail(int pathInfoId, boolean isVerifyData) {
        List<PathDetailEntity> entityList = pathDetailMapper.selectByPathInfoId(pathInfoId);
        if (entityList == null || entityList.size() == 0) {
            return null;
        } else if (entityList.size() > 1) {
            logger.warn(String.format("Size of path detail entity of path(%d) is %d", pathInfoId, entityList.size()));
        }
        PathDetailEntity entity = entityList.get(0);
        PathDetailVO pathDetailVO = new PathDetailVO();

        pathDetailVO.setPathInfoId(pathInfoId);
        if (isVerifyData) {
            pathDetailVO.setDetailPath(JSONObject.parseObject(entity.getVerifyPath()));
        } else {
            pathDetailVO.setDetailPath(JSONObject.parseObject(entity.getAnalyzePath()));
        }

        return pathDetailVO;
    }

    @Override
    public PathDeviceDetailEntity getDevieceDetail(int pathInfoId, String deviceUuid, boolean isVerifyData, String index) {
        Map<String, String> params = new HashMap<>();
        params.put("pathInfoId", String.valueOf(pathInfoId));
        params.put("deviceUuid", deviceUuid);
        params.put("isVerifyData", isVerifyData ? String.valueOf(PolicyConstants.POLICY_INT_PATH_VERIFY_DATA) : String.valueOf(PolicyConstants.POLICY_INT_PATH_ANALYZE_DATA));
        params.put("pathIndex", index);
        List<PathDeviceDetailEntity> list = pathDeviceDetailMapper.selectPathDeviceDetail(params);
        if (list == null || list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            logger.warn(String.format("Size of device detail entity of path(%d) device(%s) is %d.", pathInfoId, deviceUuid, list.size()));
        }

        return list.get(0);
    }

    @Override
    public List<PolicyRiskEntity> getRiskByPathInfoId(int pathInfoId) {
        return policyRiskMapper.selectByPathInfoId(pathInfoId);
    }

    @Override
    public List<RecommendPolicyVO> getPolicyByPathInfoId(int pathInfoId) {
        List<RecommendPolicyEntity> list = recommendPolicyMapper.selectByPathInfoId(pathInfoId);
        List<RecommendPolicyVO> recommendPolicyVOList = new ArrayList<>();
        for (RecommendPolicyEntity entity : list) {
            PolicyVO policyVO = new PolicyVO();
            BeanUtils.copyProperties(entity, policyVO);
            //设置服务
            if (!AliStringUtils.isEmpty(entity.getService())) {
                JSONArray array = JSONArray.parseArray(entity.getService());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                for (ServiceDTO service : serviceList) {
                    service.setProtocol(ProtocolUtils.getProtocolByString(service.getProtocol()));
                }
                policyVO.setService(serviceList);
            }
            //设置设备名称
            NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(entity.getDeviceUuid());

            String name = UNKNOWN_DEVICE;
            if (nodeEntity != null) {
                name = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
            }
            //policyVO.setName(name);
            //policyVOList.add(policyVO);
            boolean hasVO = false;
            for (RecommendPolicyVO recommendPolicyVO : recommendPolicyVOList) {
                if (recommendPolicyVO.getName().equals(name)) {
                    List<PolicyVO> policyVOList = recommendPolicyVO.getList();
                    policyVOList.add(policyVO);
                    hasVO = true;
                }
            }

            if (!hasVO) {
                RecommendPolicyVO recommendPolicyVO = new RecommendPolicyVO();
                recommendPolicyVO.setName(name);
                recommendPolicyVO.setDeviceUuid(entity.getDeviceUuid());
                List<PolicyVO> policyVOList = new ArrayList<>();
                policyVOList.add(policyVO);
                recommendPolicyVO.setList(policyVOList);
                recommendPolicyVOList.add(recommendPolicyVO);

            }

        }
        return recommendPolicyVOList;
    }

    @Override
    public List<RecommendPolicyVO> getMergedPolicyByTaskId(int taskId) {
        List<RecommendPolicyEntity> list = mergedPolicyMapper.selectByTaskId(taskId);
        List<RecommendPolicyVO> recommendPolicyVOList = new ArrayList<>();
        for (RecommendPolicyEntity entity : list) {
            PolicyVO policyVO = new PolicyVO();
            BeanUtils.copyProperties(entity, policyVO);
            //设置服务
            if (!AliStringUtils.isEmpty(entity.getService())) {
                JSONArray array = JSONArray.parseArray(entity.getService());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                for (ServiceDTO service : serviceList) {
                    service.setProtocol(ProtocolUtils.getProtocolByString(service.getProtocol()));
                }
                policyVO.setService(serviceList);
            }
            //设置设备名称
            NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(entity.getDeviceUuid());

            String name = UNKNOWN_DEVICE;
            if (nodeEntity != null) {
                name = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
            } else {
                name = String.format("未知设备(%s)", entity.getDeviceUuid());
            }

            boolean hasVO = false;
            for (RecommendPolicyVO recommendPolicyVO : recommendPolicyVOList) {
                if (recommendPolicyVO.getName().equals(name)) {
                    List<PolicyVO> policyVOList = recommendPolicyVO.getList();
                    policyVOList.add(policyVO);
                    hasVO = true;
                }
            }

            if (!hasVO) {
                RecommendPolicyVO recommendPolicyVO = new RecommendPolicyVO();
                recommendPolicyVO.setName(name);
                recommendPolicyVO.setDeviceUuid(entity.getDeviceUuid());
                List<PolicyVO> policyVOList = new ArrayList<>();
                policyVOList.add(policyVO);
                recommendPolicyVO.setList(policyVOList);
                recommendPolicyVOList.add(recommendPolicyVO);

            }

        }
        return recommendPolicyVOList;
    }




    @Override
    public List<CheckResultEntity> getCheckResultByPolicyId(int taskId) {
        List<RecommendPolicyEntity> policyEntityList = recommendPolicyMapper.selectByTaskId(taskId);
        List<CheckResultEntity> checkResultEntityList = new ArrayList<>();
        if (policyEntityList == null || policyEntityList.size() == 0) {
            return checkResultEntityList;
        }

        for (RecommendPolicyEntity policyEntity : policyEntityList) {
            Integer policyId = policyEntity.getId();
            List<CheckResultEntity> list = checkResultMapper.selectByPolicyId(policyId);
            if (list == null || list.size() == 0) {
                continue;
            } else if (list.size() > 1) {
                logger.warn(String.format("策略(%d)检查结果数据大于一，为(%d)", policyId, list.size()));
            }
            CheckResultEntity entity = list.get(0);
            if (AliStringUtils.isEmpty(entity.getCheckResult())) {
                logger.error("策略(%d)检查结果字段为空...");
                continue;
            }

            String checkResult = entity.getCheckResult();
            JSONObject checkResultObject = JSONObject.parseObject(checkResult);
            RuleCheckResultRO checkResultRO = checkResultObject.toJavaObject(RuleCheckResultRO.class);
            List<RuleCheckResultDataRO> ruleCheckResultDataROList = checkResultRO.getData();

            if (ruleCheckResultDataROList == null) {
                logger.error("策略(%d)检查结果数据为空..." + checkResultRO.toString());
                continue;
            }

            checkResultEntityList.add(list.get(0));
        }
        return checkResultEntityList;
    }

    @Override
    public int insertSrcNatPolicy(SNatPolicyDTO sNatPolicyDTO, Authentication auth) {
        // 检测设备是否存在
        String deviceUuid = sNatPolicyDTO.getDeviceUuid();

        NodeEntity node = this.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return ReturnCode.DEVICE_NOT_EXIST;
        }

        // 获取用户名
        String userName = auth.getName();

        // 格式化域信息
        sNatPolicyDTO.setSrcZone(getZone(sNatPolicyDTO.getSrcZone()));
        sNatPolicyDTO.setDstZone(getZone(sNatPolicyDTO.getDstZone()));

        // 创建源NAT附加信息对象
        SNatAdditionalInfoEntity additionalInfoEntity = new SNatAdditionalInfoEntity(deviceUuid,
                sNatPolicyDTO.getPostIpAddress(), sNatPolicyDTO.getSrcZone(), sNatPolicyDTO.getSrcItf(),
                sNatPolicyDTO.getDstZone(), sNatPolicyDTO.getDstItf(), sNatPolicyDTO.getMode(),sNatPolicyDTO.getInDevItfAlias(),sNatPolicyDTO.getOutDevItfAlias());
        Integer ipType = ObjectUtils.isNotEmpty(sNatPolicyDTO.getIpType())?sNatPolicyDTO.getIpType(): IPV4.getCode();
        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(sNatPolicyDTO.getTheme(), userName,
                sNatPolicyDTO.getSrcIp(), sNatPolicyDTO.getDstIp(), JSONObject.toJSONString(sNatPolicyDTO.getServiceList()),
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, JSONObject.toJSONString(additionalInfoEntity),
                sNatPolicyDTO.getSrcIpSystem(), sNatPolicyDTO.getDstIpSystem(), sNatPolicyDTO.getPostSrcIpSystem(), null,ipType);
        getBranch(userName, recommendTaskEntity);
        recommendTaskEntity.setRemarks(sNatPolicyDTO.getRemarks());
        recommendTaskEntity.setDescription(sNatPolicyDTO.getDescription());
        // 添加任务数据到策略下发任务列表
        addRecommendTask(recommendTaskEntity);

        // 添加命令行生成任务到新表
        CommandTaskEditableEntity entity =
                createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT,
                        recommendTaskEntity.getId(), userName, sNatPolicyDTO.getTheme(), sNatPolicyDTO.getDeviceUuid());

        entity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        commandTaskManager.addCommandEditableEntityTask(entity);
        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = device.getData().get(0);
        boolean isVsys = false;
        String vsysName = "";
        if (deviceData.getIsVsys() != null) {
            isVsys = deviceData.getIsVsys();
            vsysName = deviceData.getVsysName();
        }
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.SNAT, entity.getId(), entity.getTaskId(), deviceUuid,
                sNatPolicyDTO.getTheme(), userName, recommendTaskEntity.getSrcIp(), recommendTaskEntity.getDstIp(),
                additionalInfoEntity.getPostIpAddress(), null, sNatPolicyDTO.getServiceList(), null,
                sNatPolicyDTO.getSrcZone(), sNatPolicyDTO.getDstZone(), sNatPolicyDTO.getSrcItf(),
                sNatPolicyDTO.getDstItf(),sNatPolicyDTO.getInDevItfAlias(),sNatPolicyDTO.getOutDevItfAlias(), "", isVsys, vsysName, sNatPolicyDTO.getSrcIpSystem(),
                sNatPolicyDTO.getDstIpSystem(), sNatPolicyDTO.getPostSrcIpSystem());
        TaskDTO taskDTO = cmdDTO.getTask();
        taskDTO.setTaskTypeEnum(TaskTypeEnum.SNAT_TYPE);
        // 设置ip类型
        cmdDTO.getPolicy().setIpType(sNatPolicyDTO.getIpType());
        logger.info("命令行生成任务为:" + JSONObject.toJSONString(cmdDTO));
        cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT);
        pushTaskService.addGenerateCmdTask(cmdDTO);

        sNatPolicyDTO.setId(entity.getId());
        sNatPolicyDTO.setTaskId(entity.getTaskId());
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int insertDstNatPolicy(DNatPolicyDTO policyDTO, Authentication auth) {
        // 检测设备对象是否存在
        String deviceUuid = policyDTO.getDeviceUuid();
        NodeEntity node = this.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return ReturnCode.DEVICE_NOT_EXIST;
        }

        // 获取用户名
        String userName = auth.getName();

        // 格式化域信息
        policyDTO.setSrcZone(getZone(policyDTO.getSrcZone()));
        policyDTO.setDstZone(getZone(policyDTO.getDstZone()));

        // 创建目的NAT附加信息数据对象
        DNatAdditionalInfoEntity additionalInfoEntity = new DNatAdditionalInfoEntity();
        additionalInfoEntity.setDeviceUuid(deviceUuid);
        additionalInfoEntity.setPostIpAddress(policyDTO.getPostIpAddress());
        additionalInfoEntity.setPostPort(policyDTO.getPostPort());
        additionalInfoEntity.setSrcZone(policyDTO.getSrcZone());
        additionalInfoEntity.setSrcItf(policyDTO.getSrcItf());
        additionalInfoEntity.setDstZone(policyDTO.getDstZone());
        additionalInfoEntity.setDstItf(policyDTO.getDstItf());
        additionalInfoEntity.setInDevItfAlias(policyDTO.getInDevItfAlias());
        additionalInfoEntity.setOutDevItfAlias(policyDTO.getOutDevItfAlias());

        Integer ipType = ObjectUtils.isNotEmpty(policyDTO.getIpType())?policyDTO.getIpType(): IPV4.getCode();
        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(policyDTO.getTheme(), userName,
                policyDTO.getSrcIp(), policyDTO.getDstIp(), JSONObject.toJSONString(policyDTO.getServiceList()),
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, JSONObject.toJSONString(additionalInfoEntity),
                policyDTO.getSrcIpSystem(), policyDTO.getDstIpSystem(), null, policyDTO.getPostDstIpSystem(),ipType);
        getBranch(userName, recommendTaskEntity);
        recommendTaskEntity.setRemarks(policyDTO.getRemarks());
        recommendTaskEntity.setDescription(policyDTO.getDescription());
        addRecommendTask(recommendTaskEntity);

        // 添加任务到新表
        CommandTaskEditableEntity entity =
                createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT,
                        recommendTaskEntity.getId(), userName, policyDTO.getTheme(), policyDTO.getDeviceUuid());

        entity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        commandTaskManager.addCommandEditableEntityTask(entity);

        List<ServiceDTO> postServiceList =
                EntityUtils.getPostServiceList(policyDTO.getServiceList(), policyDTO.getPostPort());
        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = device.getData().get(0);
        boolean isVsys = false;
        String rootDeviceUuid = "";
        String vsysName = "";
        if (deviceData.getIsVsys() != null) {
            isVsys = deviceData.getIsVsys();
            rootDeviceUuid = deviceData.getRootDeviceUuid();
            vsysName = deviceData.getVsysName();
        }

        String startTimeString = null;
        String endTimeString = null;
        if (AliStringUtils.areNotEmpty(policyDTO.getStartTime(), policyDTO.getEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
            Long startTime = Long.parseLong(policyDTO.getStartTime());
            Long endTime = Long.parseLong(policyDTO.getEndTime());
            startTimeString = sdf.format(startTime);
            endTimeString = sdf.format(endTime);
        }
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.DNAT, entity.getId(), entity.getTaskId(), deviceUuid,
                policyDTO.getTheme(), userName, policyDTO.getSrcIp(), policyDTO.getDstIp(), null,
                policyDTO.getPostIpAddress(), policyDTO.getServiceList(), postServiceList, policyDTO.getSrcZone(),
                policyDTO.getDstZone(), policyDTO.getSrcItf(), policyDTO.getDstItf(), policyDTO.getInDevItfAlias(),policyDTO.getOutDevItfAlias(),"", isVsys, vsysName, startTimeString,
                endTimeString, policyDTO.getSrcIpSystem(), policyDTO.getDstIpSystem(), policyDTO.getPostDstIpSystem(), policyDTO.getPostPort() );
        TaskDTO taskDTO = cmdDTO.getTask();
        taskDTO.setTaskTypeEnum(TaskTypeEnum.DNAT_TYPE);
        cmdDTO.getPolicy().setIpType(policyDTO.getIpType());
        cmdDTO.getPolicy().setPostDstIpSystem(policyDTO.getPostDstIpSystem());
        logger.info("命令行生成任务为:" + JSONObject.toJSONString(cmdDTO));
        cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT);
        pushTaskService.addGenerateCmdTask(cmdDTO);

        policyDTO.setId(entity.getId());
        policyDTO.setTaskId(entity.getTaskId());
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int insertBothNatPolicy(NatPolicyDTO policyDTO, Authentication auth) {
        //检测设备对象是否存在
        String deviceUuid = policyDTO.getDeviceUuid();
        NodeEntity node = this.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return ReturnCode.DEVICE_NOT_EXIST;
        }

        //获取用户名
        String userName = auth.getName();

        //格式化域信息
        policyDTO.setSrcZone(getZone(policyDTO.getSrcZone()));
        policyDTO.setDstZone(getZone(policyDTO.getDstZone()));

        //创建双向NAT附加信息数据对象
        NatAdditionalInfoEntity additionalInfoEntity = new NatAdditionalInfoEntity(null, policyDTO.getPostSrcIp(),
                policyDTO.getPostDstIp(), policyDTO.getPostPort(), deviceUuid, policyDTO.getSrcZone(), policyDTO.getDstZone(),
                policyDTO.getDstItf(),policyDTO.getSrcItf(), policyDTO.isDynamic(),policyDTO.getInDevItfAlias(),policyDTO.getOutDevItfAlias());
        Integer ipType = ObjectUtils.isNotEmpty(policyDTO.getIpType())?policyDTO.getIpType(): IPV4.getCode();
        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(policyDTO.getTheme(), userName, policyDTO.getSrcIp(),
                policyDTO.getDstIp(), JSONObject.toJSONString(policyDTO.getServiceList()), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, JSONObject.toJSONString(additionalInfoEntity), null, null, null, null,ipType);
        getBranch(userName, recommendTaskEntity);
        recommendTaskEntity.setRemarks(policyDTO.getRemarks());
        recommendTaskEntity.setDescription(policyDTO.getDescription());
        addRecommendTask(recommendTaskEntity);

        //添加任务到新表
        CommandTaskEditableEntity entity = createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT,
                recommendTaskEntity.getId(), userName, policyDTO.getTheme(), policyDTO.getDeviceUuid());
        entity.setBranchLevel(recommendTaskEntity.getBranchLevel());
        commandTaskManager.addCommandEditableEntityTask(entity);

        List<ServiceDTO> postServiceList = EntityUtils.getPostServiceList(policyDTO.getServiceList(), policyDTO.getPostPort());
        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = device.getData().get(0);
        boolean isVsys = false;
        String vsysName = "";
        if (deviceData.getIsVsys() != null) {
            isVsys = deviceData.getIsVsys();
            vsysName = deviceData.getVsysName();
        }
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.BOTH, entity.getId(), entity.getTaskId(), deviceUuid, policyDTO.getTheme(),
                userName, policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getPostSrcIp(), policyDTO.getPostDstIp(),
                policyDTO.getServiceList(), postServiceList, policyDTO.getSrcZone(), policyDTO.getDstZone(), policyDTO.getSrcItf(),
                policyDTO.getDstItf(),policyDTO.getInDevItfAlias(),policyDTO.getOutDevItfAlias(), "", isVsys, vsysName, policyDTO.getStartTime(), policyDTO.getEndTime(), null , null, null, policyDTO.getPostPort());


        PolicyDTO policy = cmdDTO.getPolicy();
        policy.setDynamic(policyDTO.isDynamic());
        policy.setIpType(policyDTO.getIpType());
        cmdDTO.setPolicy(policy);
        logger.info("命令行生成任务为:" + JSONObject.toJSONString(cmdDTO));
        cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT);
        pushTaskService.addGenerateCmdTask(cmdDTO);

        policyDTO.setId(entity.getId());
        policyDTO.setTaskId(entity.getTaskId());
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int insertRecommendTaskList(List<RecommendTaskEntity> list) {
        return recommendTaskMapper.addRecommendTaskList(list);
    }

    @Override
    public RecommendTaskEntity getRecommendTaskByTaskId(int taskId) {
        List<RecommendTaskEntity> list = recommendTaskMapper.selectByTaskId(taskId);
        if (list == null || list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            logger.warn(String.format("策略开通任务(%d)数据大于1", taskId));
        }
        return list.get(0);
    }

    @Override
    public CommandTaskEditableEntity getRecommendTaskById(int Id) {
        List<CommandTaskEditableEntity> list = commandTaskEdiableMapper.selectByTaskId(Id);
        if (list == null || list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            logger.warn(String.format("策略下发任务(%d)数据大于1", Id));
        }
        return list.get(0);
    }

    @Override
    public int addPathInfo(PathInfoEntity entity) {
        return pathInfoMapper.insert(entity);
    }

    @Override
    public int addRecommendPolicyList(List<RecommendPolicyEntity> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return ReturnCode.SAVE_EMPTY_LIST;
        }

        int rc = recommendPolicyMapper.insertRecommendPolicyList(entityList);
        if (rc != entityList.size()) {
            logger.error(String.format("任务(%d)存储错误，SQL影响行数与存储行数不一致！%d:%d", entityList.get(0).getTaskId(), entityList.size(), rc));
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int addCheckResult(CheckResultEntity entity) {
        return checkResultMapper.insert(entity);
    }

    @Override
    public int addCommandTaskEditableEntity(CommandTaskEditableEntity entity) {
        return commandTaskEdiableMapper.insert(entity);
    }

    @Override
    public List<PathInfoEntity> getPathInfoByTaskId(int taskId) {
        return pathInfoMapper.selectByTaskId(taskId);
    }

    @Override
    public PathInfoEntity getPathInfoByPathInfoId(int taskId) {
        List<PathInfoEntity> list = pathInfoMapper.selectById(taskId);
        if (list == null || list.size() == 0) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public List<RecommendPolicyEntity> getPolicyListByPathInfoId(int pathInfoId) {
        return recommendPolicyMapper.selectByPathInfoId(pathInfoId);
    }


    @Override
    public int savePathDeviceDetail(PathDeviceDetailEntity entity) {
        Map<String, String> params = new HashMap<>();
        params.put("pathInfoId", String.valueOf(entity.getPathInfoId()));
        params.put("deviceUuid", entity.getDeviceUuid());
        params.put("isVerifyData", String.valueOf(entity.getIsVerifyData()));
        params.put("pathIndex", String.valueOf(entity.getPathIndex()));

        List<PathDeviceDetailEntity> list = pathDeviceDetailMapper.selectPathDeviceDetail(params);
        int rc = 0;
        if (list == null || list.size() == 0) {
            rc = pathDeviceDetailMapper.insert(entity);
        } else {
            rc = pathDeviceDetailMapper.update(entity);
        }
        return rc;
    }


    @Override
    public int insertpathDeviceDetailList(List<PathDeviceDetailEntity> list) {
        if (list == null || list.size() == 0) {
            return ReturnCode.SAVE_EMPTY_LIST;
        }

        int rc = pathDeviceDetailMapper.insertList(list);
        if (rc != list.size()) {
            logger.error(String.format("路径( %d )设备%s详情存储错误，SQL影响行数与存储行数不一致！ %d:%d", list.get(0).getPathInfoId(), list.size(), rc));
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int saveVerifyDeitailPath(int pathInfoId, String detailPath) {
        Map<String, String> params = new HashMap<>();
        params.put("pathInfoId", String.valueOf(pathInfoId));
        params.put("verifyPath", detailPath);
        return pathDetailMapper.updateVerifyPath(params);
    }

    @Override
    public void saveAnalyzeDetailPath(int pathInfoId, String detailPath) {
        PathDetailEntity entity = new PathDetailEntity();
        entity.setPathInfoId(pathInfoId);
        entity.setAnalyzePath(detailPath);
        int rc = pathDetailMapper.insert(entity);
        if (rc != 1) {
            logger.error(String.format("路径(%d)详情存储出错，SQL执行影响行数不正确:%d", pathInfoId, rc));
        }
    }

    @Override
    public int updatePathStatus(PathInfoEntity entity) {
        return pathInfoMapper.updateStatusById(entity);
    }

    @Override
    public void updateTaskStatus(int taskId, int status) {
        RecommendTaskEntity entity = new RecommendTaskEntity();
        entity.setId(taskId);
        entity.setStatus(status);
        if (status == PolicyConstants.POLICY_INT_STATUS_SIMULATING) {
            entity.setTaskStart(new Date());
        } else if (status == PolicyConstants.POLICY_INT_STATUS_SIMULATION_DONE) {
            entity.setTaskEnd(new Date());
        }
        int rc = recommendTaskMapper.updateByPrimaryKeySelective(entity);
        if (rc != 1) {
            logger.error(String.format("任务(%d)更新任务状态出错，SQL执行影响行数不正确:%d", taskId, rc));
        }
    }

    @Override
    public void updateTaskByEntity(RecommendTaskEntity entity) {
        int rc = recommendTaskMapper.updateByPrimaryKeySelective(entity);
        if (rc != 1) {
            logger.error(String.format("任务(%d)更新任务出错，SQL执行影响行数不正确:%d", entity.getId(), rc));
        }
    }

    @Override
    public void updatePathPathStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("pathStatus", String.valueOf(status));
        int rc = pathInfoMapper.updateStatusByPathInfoId(params);
        if (rc != 1) {
            logger.error(String.format("路径(%d)更新路径路径状态出错，SQL执行影响行数不正确:%d", pathInfoId, rc));
        }
    }

    @Override
    public void updatePathAnalyzeStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("analyzeStatus", String.valueOf(status));
        int rc = pathInfoMapper.updateStatusByPathInfoId(params);
        if (rc != 1) {
            logger.error(String.format("路径(%d)更新路径分析状态出错，SQL执行影响行数不正确:%d", pathInfoId, rc));
        }
    }

    @Override
    public void updatePathAdviceStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("adviceStatus", String.valueOf(status));
        int rc = pathInfoMapper.updateStatusByPathInfoId(params);
        if (rc != 1) {
            logger.error(String.format("路径(%d)更新路径策略建议状态出错，SQL执行影响行数不正确:%d", pathInfoId, rc));
        }
    }

    @Override
    public int updatePathCheckStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("checkStatus", String.valueOf(status));
        return pathInfoMapper.updateStatusByPathInfoId(params);
    }

    @Override
    public void updatePathRiskStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("riskStatus", String.valueOf(status));
        int rc = pathInfoMapper.updateStatusByPathInfoId(params);
        if (rc == 0) {
            logger.error(String.format("路径(%d)更新风险分析状态出错，SQL执行影响行数为0", pathInfoId));
        }
    }

    @Override
    public void updatePathCmdStatusByTaskId(int taskId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        params.put("cmdStatus", String.valueOf(status));
        int rc = pathInfoMapper.updateStatusByTaskId(params);
        //根据taskId批量处理，不知道具体影响行数，只判断影响行数大于0即可
        if (rc == 0) {
            logger.error(String.format("任务(%d)更新路径分析状态出错，SQL执行影响行数不正确:%d", taskId, rc));
        }
    }

    @Override
    public int updatePathPushStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("pushStatus", String.valueOf(status));
        return pathInfoMapper.updateStatusByPathInfoId(params);
    }

    @Override
    public int updatePathGatherStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("gatherStatus", String.valueOf(status));
        return pathInfoMapper.updateStatusByPathInfoId(params);
    }



    @Override
    public int updatePathVerifyStatus(int pathInfoId, int status) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("verifyStatus", String.valueOf(status));
        return pathInfoMapper.updateStatusByPathInfoId(params);
    }


    /**
     * 设置生成策略高级选项
     *
     * @param entity
     */
    @Override
    public void getAdvancedSettings(RecommendPolicyEntity entity) {
        String uuid = entity.getDeviceUuid();
        // 1.标识长链接 2.标识短链接
        Integer longConnect = null == entity.getIdleTimeout() ? ConnectTypeEnum.SHORT_CONNECT.getCode() : ConnectTypeEnum.LONG_CONNECT.getCode();
        String connectType = longConnect.toString();

        //命令行生成设置域信息
        //根据高级设置决定策略检查是否需要设置源域和目的域。
        int zoneSettings = AdvancedSettingsConstants.PARAM_INT_SET_BOTH_ZONE;
        if (advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE, uuid)) {
            logger.debug(String.format("设备(%s)不指定域信息...命令行生成不指定域", uuid));
            //源域目的域均设置为空
            zoneSettings = AdvancedSettingsConstants.PARAM_INT_SET_NO_ZONE;
        } else if (advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE, uuid)) {
            logger.debug(String.format("设备(%s)指定源域信息...命令行生成指定源域", uuid));
            //目的域设置为空
            zoneSettings = AdvancedSettingsConstants.PARAM_INT_SET_SRC_ZONE;
        } else if (advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE, uuid)) {
            logger.debug(String.format("设备(%s)指定目的域信息...命令行生成指定目的域", uuid));
            //源域设置为空
            zoneSettings = AdvancedSettingsConstants.PARAM_INT_SET_DST_ZONE;
        } else {
            logger.debug(String.format("设备(%s)使用默认方式设置域...命令行生成指定源域和目的域", uuid));
        }
        entity.setSpecifyZone(zoneSettings);

        //思科设备根据高级设置决定是ACL策略设置到入接口还是出接口
        int aclDirection = AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_IN_DIRECTION;
        if (advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE, uuid)) {
            logger.debug(String.format("设备（%s）指定策略下发到出接口", uuid));
            aclDirection = AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION;
        }
        entity.setAclDirection(aclDirection);

        String isCreateRule = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_RULES);
        //全局设置为合并策略，则设置为合并策略
        int createRule = AdvancedSettingsConstants.PARAM_INT_CREATE_RULE;
        if (isCreateRule.equals(AdvancedSettingsConstants.IS_MERGE_RULE_VALUE)) {
            logger.debug("该策略生成命令行时生成命令行优先合并策略");
            createRule = AdvancedSettingsConstants.PARAM_INT_MERGE_RULE;
        }
        entity.setCreatePolicy(createRule);

        String isCreateObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT);
        //全局设置为不创建对象，则设置为不创建对象
        int createObject = AdvancedSettingsConstants.PARAM_INT_CREATE_OBJECT;
        if (isCreateObject.equals(AdvancedSettingsConstants.IS_REFERENCE_CONTENT_VALUE)) {
            createObject = AdvancedSettingsConstants.PARAM_INT_REFERENCE_CONTENT;
        }
        entity.setCreateObject(createObject);

        //设置策略移动的位置
        DeviceDTO beforeDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE, uuid,connectType);
        DeviceDTO afterDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER, uuid,connectType);
        DeviceDTO topDevice = advancedSettingService.getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP, uuid,connectType);

        int rulePosition = AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_FIRST;
        String relatedRule = "";
        if (null != topDevice ) {
            rulePosition = AdvancedSettingsConstants.PARAM_INT_NOT_MOVE_POLICY;
            logger.debug("该条生成策略不移动...");
        } else if (beforeDevice != null) {
            rulePosition = AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_BEFORE;
            relatedRule = beforeDevice.getRelatedRule() == null ? "" : beforeDevice.getRelatedRule().trim();
            logger.debug(String.format("该条生成策移动到策略[%s]之前...", relatedRule));
        } else if (afterDevice != null) {
            rulePosition = AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_AFTER;
            relatedRule = afterDevice.getRelatedRule() == null ? "" : afterDevice.getRelatedRule().trim();
            logger.debug(String.format("该条生成策移动到策略[%s]之前...", relatedRule));
        } else {
            logger.debug("该条生成策略需要移动到最前...");
        }
        entity.setMovePolicy(rulePosition);
        entity.setSpecificPosition(relatedRule);
    }

    @Override
    public PageInfo<PushTaskVO> getPushTaskList(String taskId, String theme, String taskType, String status, String pushStatus,
                                                String revertStatus, int page, int psize, String userName,String branchLevel) {


        Map<String, Object> params = new HashMap();
        if (!AliStringUtils.isEmpty(taskId)) {
            params.put("taskId", taskId);
        }
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("theme", theme);
        }
        if (!AliStringUtils.isEmpty(taskType)) {
            params.put("taskType", taskType);
        }
        if (!AliStringUtils.isEmpty(status)) {
            params.put("status", status);
        }
        if (!AliStringUtils.isEmpty(revertStatus)) {
            params.put("revertStatus", revertStatus);
        }
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if(!AliStringUtils.isEmpty(userName)) {
            params.put("userName", userName);
        }
        PageHelper.startPage(page, psize);
        List<PushTaskVO> list = commandTaskEditableMapper.getPushTaskList(params);

        if (ObjectUtils.isNotEmpty(list)) {
            List<Integer> taskIds = list.stream().map(task -> task.getTaskId()).distinct().collect(Collectors.toList());
            Map<String, Object> cond = new HashMap<>();
            cond.put("taskIds", taskIds);
            List<CommandTaskEditableEntity> taskEditableEntityList = commandTaskEditableMapper.selectPushStuasByTaskIdList(cond);
            for (PushTaskVO pushTaskVO : list) {
                List<CommandTaskEditableEntity> taskCollect = taskEditableEntityList.stream().filter(task -> pushTaskVO.getTaskId().equals(task.getTaskId())).collect(Collectors.toList());
                int pushStatusInTaskList = getPushStatusInTaskList(taskCollect);
                pushTaskVO.setPushStatus(pushStatusInTaskList);
            }
            if (!AliStringUtils.isEmpty(pushStatus)) {
                list = list.stream().filter(s -> pushStatus.equals(String.valueOf(s.getPushStatus()))).collect(Collectors.toList());
            }
        }
        PageInfo<PushTaskVO> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public int updateCommandTaskStatus(int taskId, int status) {
        List<CommandTaskEditableEntity> list = commandTaskEditableMapper.selectByTaskId(taskId);
        if (list == null || list.size() == 0) {
            return ReturnCode.FAILED;
        }
        for (CommandTaskEditableEntity entity : list) {
            CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
            newEntity.setId(entity.getId());
            newEntity.setStatus(status);
            commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public  int updateCommandTaskStatus(List<CommandTaskEditableEntity> list,int status){
        if(CollectionUtils.isEmpty(list)){
            return ReturnCode.FAILED;
        }
        for (CommandTaskEditableEntity entity : list) {
            CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
            newEntity.setId(entity.getId());
            newEntity.setStatus(status);
            commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateCommandTaskPushStatus(int taskId, int status) {
        List<CommandTaskEditableEntity> list = commandTaskEditableMapper.selectByTaskId(taskId);
        if (list == null || list.size() == 0) {
            return ReturnCode.FAILED;
        }
        for (CommandTaskEditableEntity entity : list) {
            CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
            newEntity.setId(entity.getId());
            newEntity.setPushStatus(status);
            newEntity.setRevertStatus(null);
            commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateCommandTaskRevertStatus(int taskId, int status) {
        List<CommandTaskEditableEntity> list = commandTaskEditableMapper.selectByTaskId(taskId);
        if (list == null || list.size() == 0) {
            return ReturnCode.FAILED;
        }
        for (CommandTaskEditableEntity entity : list) {
            CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
            newEntity.setId(entity.getId());
            newEntity.setRevertStatus(status);
            newEntity.setPushStatus(null);
            commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateCommandTaskPushOrRevertStatus(List<CommandTaskEditableEntity> list, int status, boolean isRever) {
        if (CollectionUtils.isEmpty(list)) {
            return ReturnCode.FAILED;
        }
        for (CommandTaskEditableEntity entity : list) {
            CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
            newEntity.setId(entity.getId());
            if (isRever) {
                newEntity.setRevertStatus(status);
                newEntity.setPushStatus(null);
            } else {
                newEntity.setPushStatus(status);
                newEntity.setRevertStatus(null);
            }
            commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateCommandTaskStatusById(int id, int status) {
        CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
        newEntity.setId(id);
        newEntity.setPushStatus(status);
        newEntity.setRevertStatus(null);
        commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateCommandTaskRevertStatusById(int id, int status) {
        CommandTaskEditableEntity newEntity = new CommandTaskEditableEntity();
        newEntity.setId(id);
        newEntity.setRevertStatus(status);
        newEntity.setPushStatus(null);
        commandTaskEditableMapper.updateByPrimaryKeySelective(newEntity);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateStopTaskPushStatus(int taskId, int status) {
        List<CommandTaskEditableEntity> list = commandTaskEditableMapper.selectByTaskId(taskId);
        if (list == null || list.size() == 0) {
            return ReturnCode.FAILED;
        }
        for (CommandTaskEditableEntity entity : list) {
            Map<String, String> parmas = new HashMap<>();
            parmas.put("id",String.valueOf(entity.getId()));
            parmas.put("pushStatus", String.valueOf(status));
            parmas.put("pushScheduleInit","Y");
            parmas.put("enableEmail","false");
            parmas.put("receiverEmailInit","Y");

            commandTaskEditableMapper.updateForStopTask(parmas);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public void addTaskRisk(int pathInfoId, String riskId) {
        PolicyRiskEntity riskEntity = new PolicyRiskEntity();
        riskEntity.setPathInfoId(pathInfoId);
        riskEntity.setRuleId(riskId);
        int rc = policyRiskMapper.insert(riskEntity);
        if (rc == 0) {
            logger.error(String.format("路径(%d)增加风险数据出错，SQL执行影响行数为0", pathInfoId));
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removePolicyTasks(List<Integer> list) {
        for (int taskId : list) {
            logger.info(String.format("删除任务[%d]任务生成数据信息...", taskId));
            recommendTaskMapper.deleteByTaskId(taskId);

            // 删除工单下已经使用的ruleId
            List<CommandTaskEditableEntity> editableList = commandTaskEdiableMapper.selectByTaskId(taskId);
            if (!CollectionUtils.isEmpty(editableList)) {
                int status = getPushStatusInTaskList(editableList);
                // 判断下发状态如果是下发失败和下发未开始则 删除工单的时候删除对应的taskId
                if (PushStatusConstans.PUSH_STATUS_FAILED == status || PushStatusConstans.PUSH_STATUS_NOT_START == status) {
                    logger.info(String.format("删除任务(%d)所有关联的高级设置ruleId...", taskId));
                    advancedSettingService.removeRuleIdByTaskId(taskId);
                }
            }

            logger.info(String.format("删除任务(%d)任务下发数据信息...", taskId));
            commandTaskEdiableMapper.deleteByTaskId(taskId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTasks(List<Integer> list ,int type) {
        Map<String, Object> cond = new HashMap<>();
        cond.put("ids",list);
        logger.info(String.format("删除任务[%s]路径详情...", list.toString()));
        //push_path_detail
        pathDetailMapper.deleteByTaskList(cond);
        logger.info(String.format("删除任务[%s]路径风险分析结果...", list.toString()));
        //push_policy_risk
        policyRiskMapper.deleteByTaskList(cond);
        logger.info(String.format("删除任务[%s]路径策略检查结果...", list.toString()));
        //push_policy_check_result
        checkResultMapper.deleteByTaskList(cond);
        logger.info(String.format("删除任务[%s]路径开通策略...", list.toString()));
        //push_recommend_policy
        recommendPolicyMapper.deleteByTaskList(cond);
        logger.info(String.format("删除任务[%s]路径设备详情...", list.toString()));
        pathDeviceDetailMapper.deleteByTaskList(cond);//push_path_device_detail
        logger.info(String.format("删除任务[%s]策略建议数据...", list.toString()));
        mergedPolicyMapper.deleteByTaskList(cond);//push_merged_policy

        // 删除工单下已经使用的ruleId
        List<CommandTaskEditableEntity> editableList = commandTaskEdiableMapper.selectByTaskIdList(cond);
        if (!CollectionUtils.isEmpty(editableList)) {
            int mergStatus = getPushStatusInTaskList(editableList);
            // 判断下发状态如果是下发失败和下发未开始则 删除工单的时候删除对应的taskId
            if (PushStatusConstans.PUSH_STATUS_FAILED == mergStatus
                    || PushStatusConstans.PUSH_STATUS_NOT_START == mergStatus) {
                for (CommandTaskEditableEntity entity : editableList){
                    advancedSettingService.removeRuleIdByTaskId(entity.getTaskId());

                }
            }
        }

        logger.info(String.format("删除任务[%s]命令行...", list.toString()));
        commandTaskEdiableMapper.deleteByTaskList(cond);//push_command_task_editable
        logger.info(String.format("删除任务[%s]路径信息...", list.toString()));
        pathInfoMapper.deleteByTaskList(cond);//push_path_info
        if (type == 0) {
            logger.info(String.format("删除任务[%s]任务信息...", list.toString()));
            recommendTaskMapper.deleteByTaskList(cond);//push_recommend_task
        }
        if (type == 1) {
            logger.info(String.format("更改任务[%s]状态为0仿真未开始...", list.toString()));
            recommendTaskMapper.updateByTaskList(cond);
        }
    }

    @Override
    public RecommendPolicyEntity getPolicyByPolicyId(Integer policyId) {
        List<RecommendPolicyEntity> list = recommendPolicyMapper.selectById(policyId);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public int setPathEnable(Integer pathInfoId, String enable) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(pathInfoId));
        params.put("enablePath", enable);

        return pathInfoMapper.enablePath(params);
    }


    @Override
    public boolean isCheckRule() {
        String rc = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CHECK_RULE);
        if (rc.equals(AdvancedSettingsConstants.IS_SKIP_CHECK_RULE)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCheckRisk() {
        String rc = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CHECK_RISK);
        if (rc.equals(AdvancedSettingsConstants.IS_SKIP_CHECK_RISK)) {
            return false;
        }
        return true;
    }

    @Override
    public List<RecommendPolicyEntity> getMergedPolicyList(int taskId) {
        return mergedPolicyMapper.selectByTaskId(taskId);
    }

    @Override
    public int addMergedPolicyList(List<RecommendPolicyEntity> list) {
        return mergedPolicyMapper.insertRecommendPolicyList(list);
    }

    @Override
    public int addMergedPolicy(RecommendPolicyEntity entity) {
        return mergedPolicyMapper.insert(entity);
    }

    @Override
    public boolean isUseCurrentObject() {
        String rc = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_OBJECT);
        if (rc.equals(AdvancedSettingsConstants.IS_CREATE_NEW)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isUseCurrentAddressObject() {
        String rc = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT);
        if (rc.equals(AdvancedSettingsConstants.IS_CREATE_NEW)) {
            return false;
        }
        return true;
    }

    @Override
    public void removeCommandsByTask(int taskId) {
        logger.info(String.format("删除任务[%d]命令行...", taskId));
        commandTaskEditableMapper.deleteByTaskId(taskId);
        logger.info(String.format("删除任务[%d]合并后策略...", taskId));
        mergedPolicyMapper.deleteByTaskId(taskId);
    }

    @Override
    public void updateTaskById(RecommendTaskEntity entity) {
        recommendTaskMapper.updateByPrimaryKey(entity);
    }

    @Override
    public List<DeviceDimension> searchDeviceDimensionByTaskId(Integer taskId) {
        return recommendPolicyMapper.selectDeviceDimensionByTaskId(taskId);
    }

    @Override
    public List<RecommendPolicyEntity> selectByDeviceDimension(DeviceDimension deviceDimension, Integer taskId) {
        Map<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        params.put("deviceUuid", deviceDimension.getDeviceUuid());
        params.put("srcZone", deviceDimension.getSrcZone());
        params.put("dstZone", deviceDimension.getDstZone());
        params.put("inDevItf", deviceDimension.getInDevItf());
        params.put("outDevItf", deviceDimension.getOutDevItf());
        return recommendPolicyMapper.selectByDeviceDimension(params);
    }

    @Override
    public int addPathInfoList(List<PathInfoEntity> list) {
        if (list == null || list.size() == 0) {
            return ReturnCode.SAVE_EMPTY_LIST;
        }

        int rc = pathInfoMapper.addPathInfoList(list);
        if (rc != list.size()) {
            logger.error(String.format("任务(%d)存储错误，SQL影响行数与存储行数不一致！%d:%d", list.get(0).getTaskId(), list.size(), rc));
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int getAclDirection(String deviceUuid) {
        int aclDirection = AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_IN_DIRECTION;
        if (advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE, deviceUuid)) {
            logger.debug(String.format("设备（%s）指定策略下发到出接口", deviceUuid));
            aclDirection = AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION;
        }
        return aclDirection;
    }

    @Override
    public NodeEntity getDeviceByManageIp(String deviceIp) {
        return policyRecommendNodeMapper.getTheNodeByIp(deviceIp);
    }

    private List<RecommendTaskEntity> searchTaskList(String theme, String orderNumber, String userName, String description, String srcIp, String dstIp,
                                                     String protocol, String dstPort, String status, String taskType, int page, int psize, String branchLevel,String deviceUuid) {


        PageHelper.startPage(page, psize);
        Map<String, Object> params = new HashMap<>();
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("theme", theme);
        }
        if (!AliStringUtils.isEmpty(orderNumber)) {
            params.put("orderNumber", orderNumber);
        }
        if(!AliStringUtils.isEmpty(userName) ) {
            params.put("userName", userName);
        }
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if (!AliStringUtils.isEmpty(description)) {
            params.put("description", description);
        }
        if (!AliStringUtils.isEmpty(srcIp)) {
            params.put("srcIp", srcIp);
        }
        if (!AliStringUtils.isEmpty(dstIp)) {
            params.put("dstIp", dstIp);
        }
        if (!AliStringUtils.isEmpty(dstPort)) {
            params.put("dstPort", dstPort);
        }
        if (!AliStringUtils.isEmpty(protocol)) {
            params.put("protocol", protocol);
        }
        if (!AliStringUtils.isEmpty(status)) {
            if (status.contains(",")){
                status = status.split(",")[0];
            }
            params.put("status", String.valueOf(status));
        }
        if (!AliStringUtils.isEmpty(taskType)) {
            params.put("taskType", taskType);
        }
        if (!AliStringUtils.isEmpty(deviceUuid)) {
            params.put("deviceUuid", deviceUuid);
        }

        List<RecommendTaskEntity> list = recommendTaskMapper.searchTask(params);
        return list;
    }

    private List<RecommendTaskEntity> searchPolicyTaskList(String theme, String orderNumber, String userName, String description, String srcIp, String dstIp,
                                                           String protocol, String dstPort, String status, String taskType,  String startTime, String endTime, String branchLevel,String deviceUuid) {


        Map<String, Object> params = new HashMap<>();
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("theme", theme);
        }
        if (!AliStringUtils.isEmpty(orderNumber)) {
            params.put("orderNumber", orderNumber);
        }
        if(!AliStringUtils.isEmpty(userName) ) {
            params.put("userName", userName);
        }
        if (!AliStringUtils.isEmpty(startTime)) {
            params.put("startTime", startTime);
        }
        if (!AliStringUtils.isEmpty(endTime)) {
            params.put("endTime", endTime);
        }
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if (!AliStringUtils.isEmpty(description)) {
            params.put("description", description);
        }
        if (!AliStringUtils.isEmpty(srcIp)) {
            params.put("srcIp", srcIp);
        }
        if (!AliStringUtils.isEmpty(dstIp)) {
            params.put("dstIp", dstIp);
        }
        if (!AliStringUtils.isEmpty(dstPort)) {
            params.put("dstPort", dstPort);
        }
        if (!AliStringUtils.isEmpty(protocol)) {
            params.put("protocol", protocol);
        }
        if (!AliStringUtils.isEmpty(status)) {
            if (status.contains(",")){
                status = status.split(",")[0];
            }
            params.put("status", String.valueOf(status));
        }
        if (!AliStringUtils.isEmpty(taskType)) {
            params.put("taskType", taskType);
        }
        if (!AliStringUtils.isEmpty(deviceUuid)) {
            params.put("deviceUuid", deviceUuid);
        }
        List<RecommendTaskEntity> list = recommendTaskMapper.searchPolicyTask(params);
        return list;
    }


    private List<RecommendTaskEntity> searchNatTaskList(String theme, String orderNumber, String userName, String description, String srcIp, String dstIp,
                                                        String protocol, String dstPort, String status, String taskType, int page, int psize, String taskIds, Integer id, String branchLevel,String deviceUuid) {
        PageHelper.startPage(page, psize);
        Map<String, Object> params = new HashMap<>();
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("theme", theme);
        }
        if (!AliStringUtils.isEmpty(orderNumber)) {
            params.put("orderNumber", orderNumber);
        }
        if(!AliStringUtils.isEmpty(userName) ) {
            params.put("userName", userName);
        }
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if (!AliStringUtils.isEmpty(description)) {
            params.put("description", description);
        }
        if (!AliStringUtils.isEmpty(srcIp)) {
            params.put("srcIp", srcIp);
        }
        if (!AliStringUtils.isEmpty(dstIp)) {
            params.put("dstIp", dstIp);
        }
        if (!AliStringUtils.isEmpty(dstPort)) {
            params.put("dstPort", dstPort);
        }
        if (!AliStringUtils.isEmpty(protocol)) {
            params.put("protocol", protocol);
        }
        if (!AliStringUtils.isEmpty(status)) {
            if (status.contains(",")){
                status = status.split(",")[0];
            }
            params.put("status", String.valueOf(status));
        }
        if (!AliStringUtils.isEmpty(taskType)) {
            params.put("taskType", taskType);
        }
        if (!AliStringUtils.isEmpty(deviceUuid)) {
            params.put("deviceUuid", deviceUuid);
        }
        if (!AliStringUtils.isEmpty(taskIds)){
            params.put("taskIds", taskIds);
        }

        params.put("id", id == null ? "" : String.valueOf(id));
        List<RecommendTaskEntity> list = recommendTaskMapper.searchNatTask(params);
        return list;
    }

    private List<RecommendTaskEntity> searchNatPolicyTaskList(String theme, String orderNumber, String userName, String description, String srcIp, String dstIp,
                                                              String protocol, String dstPort, String status, String startTime, String endTime, String taskType, String taskIds, Integer id,String branchLevel,String deviceUuid) {
        Map<String, Object> params = new HashMap<>();
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("theme", theme);
        }
        if (!AliStringUtils.isEmpty(orderNumber)) {
            params.put("orderNumber", orderNumber);
        }
        if(!AliStringUtils.isEmpty(userName) ) {
            params.put("userName", userName);
        }
        if (!AliStringUtils.isEmpty(startTime)) {
            params.put("startTime", startTime);
        }
        if (!AliStringUtils.isEmpty(endTime)) {
            params.put("endTime", endTime);
        }
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if (!AliStringUtils.isEmpty(description)) {
            params.put("description", description);
        }
        if (!AliStringUtils.isEmpty(srcIp)) {
            params.put("srcIp", srcIp);
        }
        if (!AliStringUtils.isEmpty(dstIp)) {
            params.put("dstIp", dstIp);
        }
        if (!AliStringUtils.isEmpty(dstPort)) {
            params.put("dstPort", dstPort);
        }
        if (!AliStringUtils.isEmpty(protocol)) {
            params.put("protocol", protocol);
        }
        if (!AliStringUtils.isEmpty(status)) {
            if (status.contains(",")){
                status = status.split(",")[0];
            }
            params.put("status", String.valueOf(status));
        }
        if (!AliStringUtils.isEmpty(taskType)) {
            params.put("taskType", taskType);
        }
        if (!AliStringUtils.isEmpty(deviceUuid)) {
            params.put("deviceUuid", deviceUuid);
        }
        params.put("taskIds", taskIds);
        params.put("id", id == null ? "" : String.valueOf(id));
        List<RecommendTaskEntity> list = recommendTaskMapper.searchNatPolicyTask(params);
        return list;
    }


    private List<RecommendTaskEntity> searchRecommendTaskList(SearchRecommendTaskDTO searchRecommendTaskDTO) {
        PageHelper.startPage(searchRecommendTaskDTO.getPage(), searchRecommendTaskDTO.getPSize());
        Map<String, Object> params = new HashMap<>();

        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getBatchId())) {
            params.put("batchId", searchRecommendTaskDTO.getBatchId());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getId())) {
            params.put("id", searchRecommendTaskDTO.getId());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getTheme())) {
            params.put("theme", searchRecommendTaskDTO.getTheme());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getOrderNumber())) {
            params.put("orderNumber", searchRecommendTaskDTO.getOrderNumber());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getUserName())) {
            params.put("userName", searchRecommendTaskDTO.getUserName());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getDescription())) {
            params.put("description", searchRecommendTaskDTO.getDescription());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getRemarks())) {
            params.put("remarks", searchRecommendTaskDTO.getRemarks());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getSrcIp())) {
            params.put("srcIp", searchRecommendTaskDTO.getSrcIp());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getDstIp())) {
            params.put("dstIp", searchRecommendTaskDTO.getDstIp());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getDstPort())) {
            params.put("dstPort", searchRecommendTaskDTO.getDstPort());
        }
        //服务不为any并且服务不为空才查询
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getProtocol()) && !searchRecommendTaskDTO.getIsServiceAny()) {
            params.put("protocol", searchRecommendTaskDTO.getProtocol());
        }
        if (!AliStringUtils.isEmpty(searchRecommendTaskDTO.getStatus())) {
            if (searchRecommendTaskDTO.getStatus().equals(String.valueOf(PolicyConstants.POLICY_INT_TASK_TYPE_ALL))) {

            } else if (searchRecommendTaskDTO.getStatus().equals(String.valueOf(PolicyConstants.POLICY_INT_TASK_TYPE_FRESH))) {
                params.put("status", String.valueOf(PolicyConstants.POLICY_INT_STATUS_INITIAL));
            } else {
                // 2: 1~10;
                // 3: 11~20;
                // 4: 21~30;
                String min = "1";
                String max = "10";
                switch (searchRecommendTaskDTO.getStatus()) {
                    case "2":
                        min = "1";
                        max = "10";
                        break;
                    case "3":
                        min = "11";
                        max = "20";
                        break;
                    case "4":
                        min = "21";
                        max = "31";
                        break;
                }
                params.put("min", min);
                params.put("max", max);
            }
        }

        String branchLevel = remoteBranchService.likeBranch(searchRecommendTaskDTO.getAuthentication().getName());
        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }

        if(searchRecommendTaskDTO.getTaskType() != null){
            params.put("taskType",searchRecommendTaskDTO.getTaskType());
        }
        List<RecommendTaskEntity> list = new ArrayList<>();
        if (searchRecommendTaskDTO.getIsServiceAny()) {
            list = recommendTaskMapper.searchRecommendTaskWithServiceAny(params);
        } else {
            list = recommendTaskMapper.searchRecommendTask(params);
        }
        return list;
    }

    @Override
    public PageInfo<BatchTaskVO> searchBatchTaskList(String theme, String userName, int page, int psize) {
        PageHelper.startPage(page, psize);
        Map<String, Object> params = new HashMap<>();
        if (!AliStringUtils.isEmpty(theme)) {
            params.put("orderNumber", theme);
        }

        if (!AliStringUtils.isEmpty(userName)) {
            params.put("userName", userName);
        }

        List<RecommendTaskCheckEntity> list = recommendTaskCheckMapper.searchTask(params);
        PageInfo<RecommendTaskCheckEntity> tmpPageInfo = new PageInfo<>(list);
        List<BatchTaskVO> batchTaskVOList = new ArrayList<>();
        for (RecommendTaskCheckEntity entity : list) {
            BatchTaskVO vo = new BatchTaskVO();
            vo.setId(entity.getId());
            params = new HashMap<>();
            params.put("batchId", String.valueOf(entity.getId()));
            List<RecommendTaskEntity> entityList = recommendTaskMapper.searchRecommendTask(params);
            vo.setCount(entityList.size());
            setStatistics(vo, entityList);
            vo.setTaskIds(entity.getTaskId());
            vo.setCreateTime(entity.getCreateTime());
            vo.setUserName(entity.getUserName());
            vo.setTheme(entity.getOrderNumber());
            vo.setType(entity.getBatchType());
            vo.setStatus(entity.getStatus());
            vo.setResult(entity.getResult());
            batchTaskVOList.add(vo);

        }
        PageInfo<BatchTaskVO> pageInfo = new PageInfo<>();
        BeanUtils.copyProperties(tmpPageInfo, pageInfo);
        pageInfo.setList(batchTaskVOList);
        return pageInfo;
    }

    @Override
    public RecommendTaskCheckEntity selectBatchTaskById(Integer id) {
        return recommendTaskCheckMapper.selectByPrimaryKey(id);
    }

    @Override
    public void addBatchTask(RecommendTaskCheckEntity entity) {
        recommendTaskCheckMapper.insert(entity);
    }

    @Override
    public void updateBatchTask(RecommendTaskCheckEntity entity) {
        recommendTaskCheckMapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public RiskRuleInfoEntity getRiskInfoByRuleId(String ruleId) {
        List<RiskRuleInfoEntity> list = riskMapper.getRiskInfoByRuleId(ruleId);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<NodeEntity> getNodeList() {
        return policyRecommendNodeMapper.getNodeList();
    }

    @Override
    public TaskStatusBranchLevelsDTO getPushTaskStatusList(String userName) {
        TaskStatusBranchLevelsDTO statusBranchLevelsDTO = new TaskStatusBranchLevelsDTO();
        String branchLevel = remoteBranchService.likeBranch(userName);
        statusBranchLevelsDTO.setBranchLevel(branchLevel);
        List<PushStatus> pushTaskStatusList = commandTaskEditableMapper.getPushTaskStatusList(branchLevel);
        statusBranchLevelsDTO.setPushStatuses(pushTaskStatusList);
        return statusBranchLevelsDTO;
    }


    private void setStatistics(BatchTaskVO vo, List<RecommendTaskEntity> list) {
        int notStart = 0;
        int waiting = 0;
        int running = 0;
        int analyzed = 0;
        Date start = null;
        Date end = null;
        Boolean hasUnfinished = false;
        for (RecommendTaskEntity entity : list) {
            switch (entity.getStatus()) {
                case PolicyConstants.POLICY_INT_STATUS_INITIAL:
                    notStart++;
                    break;
                case PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE:
                    waiting++;
                    break;
                case PolicyConstants.POLICY_INT_STATUS_SIMULATING:
                    running++;
                    break;
                default:
                    analyzed++;
                    break;
            }

            if (entity.getTaskStart() != null) {
                if (start == null) {
                    start = entity.getTaskStart();
                } else {
                    if (start.after(entity.getTaskStart())) {
                        start = entity.getTaskStart();
                    }
                }
            }

            if (entity.getTaskEnd() != null) {
                if (end == null) {
                    end = entity.getTaskEnd();
                } else {
                    if (end.before(entity.getTaskEnd())) {
                        end = entity.getTaskEnd();
                    }
                }
            } else {
                hasUnfinished = true;
            }
        }

        if (hasUnfinished) {
            vo.setTaskEnd(null);
            end = new Date();
        } else {
            vo.setTaskEnd(end);
        }

        vo.setTaskStart(start);
        if (start != null) {
            Long duration = end.getTime() - start.getTime();
            vo.setDuration(formatTime(duration));
        }

        vo.setNotStart(notStart);
        vo.setRunning(running);
        vo.setWaiting(waiting);
        vo.setAnalyzed(analyzed);
    }

    String formatTime(Long duration) {
        Long seconds = duration / 1000;
        if (seconds < 60) {
            return String.format("%d秒", seconds);
        } else if (seconds < 60 * 60) {
            return String.format("%d分%d秒", seconds / 60, seconds % 60);
        } else if (seconds < 60 * 60 * 24) {
            return String.format("%d小时%d分%d秒", seconds / (60 * 60), (seconds % (60 * 60)) / 60, seconds % 60);
        } else if (seconds < 7 * 60 * 60 * 24) {
            return String.format("%d天%d小时%d分%d秒", seconds / (60 * 60 * 24), (seconds % (60 * 60 * 24)) / (60 * 60), (seconds % (60 * 60)) / 60, seconds % 60);
        } else {
            return "大于一周";
        }
    }

    String formatZoneItfString(String zone, String itf) {
        if (AliStringUtils.isEmpty(zone)) {
            return AliStringUtils.isEmpty(itf) ? "" : itf;
        } else {
            return AliStringUtils.isEmpty(itf) ? zone : zone + ", " + itf;
        }
    }

    @Override
    public List<RecommendTaskEntity> getTaskListByTime(String startTime, String endTime, Authentication authentication) {
        List<RecommendTaskEntity> list = searchRecommendTaskListByTime(startTime, endTime, authentication);

        List<PathInfoEntity> pathInfoEntityList = new ArrayList<>();

        if (ObjectUtils.isNotEmpty(list)) {
            List<Integer> taskIdList = list.stream().map(task -> task.getId()).collect(Collectors.toList());
            Map<String, Object> cond = new HashMap<>();
            cond.put("ids",taskIdList);
            pathInfoEntityList = pathInfoMapper.selectByIdList(cond);
        }

        for (RecommendTaskEntity entity : list) {
            if (!AliStringUtils.isEmpty(entity.getServiceList())) {
                JSONArray jsonArray = JSONArray.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = jsonArray.toJavaList(ServiceDTO.class);
                for (ServiceDTO service : serviceList) {
                    service.setProtocol(ProtocolUtils.getProtocolByString(service.getProtocol()));
                    service.setSrcPorts(AliStringUtils.isEmpty(service.getSrcPorts()) ? null : service.getSrcPorts());
                    service.setDstPorts(AliStringUtils.isEmpty(service.getDstPorts()) ? null : service.getDstPorts());
                }
                entity.setServiceList(JSONObject.toJSONString(serviceList));
            }
            if (ObjectUtils.isNotEmpty(pathInfoEntityList)) {
                String pathAnalyzeStatus = getTaskPathAnalyzeStatusByTaskId(entity.getId(), pathInfoEntityList);
                if (entity.getStatus() >= PolicyConstants.POLICY_INT_STATUS_VERIFYING) {
                    pathAnalyzeStatus = getTaskVerifyPathStatusByTaskId(entity.getId(), pathInfoEntityList);
                }
                if (StringUtils.isEmpty(pathAnalyzeStatus) && PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED != entity.getStatus()) {
                    //当仿真完成，且无路径时，列表显示无路径 返回前端格式 11(路径分析状态):1(数量)
                    entity.setPathAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_NONE + ":" + "1");
                } else {
                    entity.setPathAnalyzeStatus(pathAnalyzeStatus);
                }

            }
        }
        return list;
    }

    /**
     * 获取当前仿真开通的路径分析信息
     * @param taskId
     * @param pathList
     * @return
     */
    private String getTaskPathAnalyzeStatusByTaskId(Integer taskId, List<PathInfoEntity> pathList) {
        StringBuilder sb = new StringBuilder();
        List<PathInfoEntity> pathInfos = pathList.stream().filter(path -> taskId.equals(path.getTaskId())).collect(Collectors.toList());
        List<Integer> ayalyzeStatusList = pathInfos.stream().map(path -> path.getAnalyzeStatus()).distinct().collect(Collectors.toList());

        ayalyzeStatusList.forEach(status -> {
            Long statusNum = pathInfos.stream().filter(path -> path.getAnalyzeStatus().equals(status)).count();
            sb.append(status).append(":").append(statusNum).append(",");
        });

        if (sb.length() > 1) {
            return sb.deleteCharAt(sb.length()-1).toString();
        }

        return sb.toString();
    }

    /**
     * 获取当前仿真开通的验证路径信息
     * @param taskId
     * @param pathList
     * @return
     */
    private String getTaskVerifyPathStatusByTaskId(Integer taskId, List<PathInfoEntity> pathList) {
        StringBuilder sb = new StringBuilder();
        List<PathInfoEntity> pathInfos = pathList.stream().filter(path -> taskId.equals(path.getTaskId())).collect(Collectors.toList());
        List<Integer> verifyStatusList = pathInfos.stream().map(path -> path.getPathStatus()).distinct().collect(Collectors.toList());

        verifyStatusList.forEach(status -> {
            Long statusNum = pathInfos.stream().filter(path -> path.getPathStatus().equals(status)).count();

            int recommendStatus = status;
            if (status.equals(PolicyConstants.POLICY_INT_VERIFY_STATUS_PATH_FULLY_OPEN)) {
                recommendStatus = PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS;
            } else if (status.equals(PolicyConstants.POLICY_INT_VERIFY_STATUS_PATH_NOT_OPEN)) {
                recommendStatus = PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FINISHED;
            }
            sb.append(recommendStatus).append(":").append(statusNum).append(",");
        });

        if (sb.length() > 1) {
            return sb.deleteCharAt(sb.length()-1).toString();
        }

        return sb.toString();
    }

    @Override
    public List<RecommendTaskEntity> selectExecuteRecommendTask() {
        return recommendTaskMapper.selectExecuteRecommendTask();
    }

    private List<RecommendTaskEntity> searchRecommendTaskListByTime(String startTime, String endTime, Authentication authentication) {
        String branchLevel = remoteBranchService.likeBranch(authentication.getName());
        Map<String, Object> params = new HashMap<>();
        if (startTime != null) {
            params.put("startTime", startTime);
        }
        if (endTime != null) {
            params.put("endTime", endTime);
        }

        if (StringUtils.isNotEmpty(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }


        List<RecommendTaskEntity> list = recommendTaskMapper.searchRecommendTask(params);
        return list;
    }


    @Override
    public int getPushStatusInTaskList(List<CommandTaskEditableEntity> entityList) {
        List<Integer> pushStatusCollect = entityList.stream().map(task -> task.getPushStatus()).distinct().collect(Collectors.toList());
        if (pushStatusCollect.size() == 1) {
            //如果只有一种状态则下发状态为该状态
            return pushStatusCollect.get(0);
        }
        Boolean hasRun = false;
        Boolean hasUnStart = false;
        Boolean hasFail = false;
        for (Integer temp : pushStatusCollect) {
            if (PushStatusConstans.PUSH_STATUS_PUSHING == temp) {
                hasRun = true;
                break;
            }
            if (PushStatusConstans.PUSH_STATUS_NOT_START == temp) {
                hasUnStart = true;
            }
            if (PushStatusConstans.PUSH_STATUS_FAILED == temp) {
                hasFail = true;
            }
        }
        if (hasRun) {
            //有一个执行中，即为执行中
            return PushStatusConstans.PUSH_STATUS_PUSHING;
        }
        if (hasUnStart && hasFail) {
            //存在一个设备状态为未开始，一个为失败，即为失败
            return PushStatusConstans.PUSH_STATUS_FAILED;
        }
        //以上三种情况都不是则为部分成功
        return PushStatusConstans.PUSH_STATUS_PART_FINISHED;
    }

    @Override
    public int getPolicyStatusByPushStatus (int pushStatus) {
        if (PushStatusConstans.PUSH_STATUS_FINISHED == pushStatus) {
            //下发状态为成功--->仿真状态为下发成功
            return PolicyConstants.POLICY_INT_STATUS_PUSH_FINISHED;
        } else if (PushStatusConstans.PUSH_STATUS_FAILED == pushStatus) {
            //下发状态为失败--->仿真状态为下发失败
            return PolicyConstants.POLICY_INT_STATUS_PUSH_ERROR;
        }
        //其他都为下发部分成功
        return PolicyConstants.POLICY_INT_STATUS_PUSH_PART_FINISHED;
    }

    @Override
    public void setPathInfoStatus(Integer id, Integer gatherStatus, Integer verifyStatus, Integer pathStatus, Integer pushStatus) {
        PathInfoEntity pathInfoEntity = new PathInfoEntity();
        pathInfoEntity.setId(id);
        pathInfoEntity.setGatherStatus(gatherStatus);
        pathInfoEntity.setVerifyStatus(verifyStatus);
        pathInfoEntity.setPathStatus(pathStatus);
        pathInfoEntity.setPushStatus(pushStatus);
        //设置为空因为这些属性默认值不为空
        pathInfoEntity.setAccessAnalyzeStatus(null);
        pathInfoEntity.setAnalyzeStatus(null);
        pathInfoEntity.setAdviceStatus(null);
        pathInfoEntity.setCheckStatus(null);
        pathInfoEntity.setRiskStatus(null);
        pathInfoEntity.setCmdStatus(null);
        updatePathStatus(pathInfoEntity);
        //清空路径详情状态
        saveVerifyDeitailPath(id,"");
    }



    /**
     * 获取路径详情中的防火墙安全策略信息
     * @param deviceDetailList
     * @return
     */
    private List<PolicyRecommendSecurityPolicyVO> getSecurityPolicyList(List<PathDeviceDetailEntity> deviceDetailList){
        List<PolicyRecommendSecurityPolicyVO> securityPolicyList = new ArrayList<>();
        if(CollectionUtils.isEmpty(deviceDetailList)){
            return securityPolicyList;
        }
        for(PathDeviceDetailEntity deviceDetailEntity : deviceDetailList){
            if(StringUtils.isEmpty(deviceDetailEntity.getDeviceDetail()) || StringUtils.isEmpty(deviceDetailEntity.getDeviceUuid())){
                logger.info("设备uuid或设备详情不存在，忽略合并");
                continue;
            }
            NodeEntity nodeEntity = getTheNodeByUuid(deviceDetailEntity.getDeviceUuid());
            if(nodeEntity == null || !StringUtils.equals(nodeEntity.getType(), "0")){
                logger.info("未找到设备或设备类型不是防火墙，忽略合并");
                continue;
            }
            String deviceDetail = deviceDetailEntity.getDeviceDetail();
            JSONObject deviceDetailObject = JSONObject.parseObject(deviceDetail);
            DeviceDetailRO detailDeviceRO = deviceDetailObject.toJavaObject(DeviceDetailRO.class);
            DeviceDetailRunVO deviceDetailRunVO = client.parseDetailRunRO(detailDeviceRO);
            if(deviceDetailRunVO != null  && CollectionUtils.isNotEmpty(deviceDetailRunVO.getSafeList())){
                List<PolicyDetailVO> safeListDetail = deviceDetailRunVO.getSafeList();

                List<PolicyRecommendSecurityPolicyVO> securityPolicyVOList = new ArrayList<>();
                for (PolicyDetailVO vo : safeListDetail) {
                    PolicyRecommendSecurityPolicyVO policyVO = new PolicyRecommendSecurityPolicyVO();
                    BeanUtils.copyProperties(vo, policyVO);
                    policyVO.setDeviceUuid(deviceDetailEntity.getDeviceUuid());
                    policyVO.setIsAble(vo.getIsAble());
                    policyVO.setDescription(vo.getDescription());
                    securityPolicyVOList.add(policyVO);
                }
                securityPolicyList.addAll(securityPolicyVOList);
            }
        }
        return securityPolicyList;
    }





    /**
     * 判断长连接是否相同
     * @param newPolicy
     * @param securityPolicy
     * @return
     */
    private boolean isSameIdleTimeout(RecommendPolicyEntity newPolicy,PolicyRecommendSecurityPolicyVO securityPolicy){
        if(ObjectUtils.isEmpty(newPolicy.getIdleTimeout()) && ObjectUtils.isEmpty(securityPolicy.getIdleTimeout())){
            return true;
        }
        String securityPolicyIdleTimeout = "";
        if(ObjectUtils.isNotEmpty(newPolicy.getIdleTimeout()) && ObjectUtils.isNotEmpty(securityPolicy.getIdleTimeout())){
            // 去除相关策略中长连接的单位秒
            if(StringUtils.containsIgnoreCase(securityPolicy.getIdleTimeout(),"s")){
                securityPolicyIdleTimeout = securityPolicy.getIdleTimeout().replace("s","");
            }
            if(newPolicy.getIdleTimeout().intValue() == Integer.parseInt(securityPolicyIdleTimeout)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断时间对象是否一致
     * @param newPolicy
     * @param securityPolicy
     * @return
     */
    private boolean isSameTime(RecommendPolicyEntity newPolicy,PolicyRecommendSecurityPolicyVO securityPolicy){
        if(ObjectUtils.isEmpty(newPolicy.getStartTime()) && ObjectUtils.isEmpty(newPolicy.getEndTime()) && StringUtils.isEmpty(securityPolicy.getTime())){
            return true;
        }
        try{
            if(StringUtils.isNotEmpty(securityPolicy.getTime())){
                // 获取时间对象的值
                ResultRO<List<ObjectDetailRO>> timeResult = queryObjectDetail(newPolicy.getDeviceUuid(),securityPolicy.getTime(),3);
                if(ObjectUtils.isNotEmpty(newPolicy.getStartTime()) && ObjectUtils.isNotEmpty(newPolicy.getEndTime()) && ObjectUtils.isEmpty(timeResult)){
                    return false;
                }
                if(ObjectUtils.isNotEmpty(timeResult)){
                    List<ObjectDetailRO> resultList = timeResult.getData();
                    for(ObjectDetailRO object : resultList){
                        // 只用判断单次时间的对象
                        if(StringUtils.startsWith(object.getValue(), "单次时间")){
                            String timeStr = object.getValue();
                            // 截取开始时间和结束时间
                            String startTimeStr =timeStr.substring(5,15) +" " + timeStr.substring(16,24);
                            String endTimeStr =timeStr.substring(36,46) +" " + timeStr.substring(47,55);
                            if(ObjectUtils.isEmpty(newPolicy.getStartTime()) || ObjectUtils.isEmpty(newPolicy.getEndTime())){
                                return false;
                            }
                            String startTime = DateUtils.formatDateTime(newPolicy.getStartTime());
                            String endTime = DateUtils.formatDateTime(newPolicy.getEndTime());
                            // 只有当开始时间和结束时间完全一致，才相等
                            if(StringUtils.equals(startTimeStr,startTime) && StringUtils.equals(endTimeStr,endTime)){
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取时间对象异常");
        }
        return false;
    }


    /**
     * 将原策略值传递，生成命令行需要
     * @param newPolicy
     * @param securityPolicy
     * @param mergeProperty
     * @param mergePolicyName
     * @param mergeValue
     */
    private void buildEditPolicyData(RecommendPolicyEntity newPolicy, PolicyRecommendSecurityPolicyVO securityPolicy,Integer mergeProperty, String mergePolicyName, String mergeValue){
        newPolicy.setMergeProperty(mergeProperty);
        newPolicy.setEditPolicyName(mergePolicyName);
        newPolicy.setMergeValue(mergeValue);

        newPolicy.setSecurityPolicy(securityPolicy);
    }

    /**
     * 获取对象的值
     * @param deviceUuid
     * @param name
     * @param type
     * @return
     * @throws Exception
     */
    public ResultRO<List<ObjectDetailRO>> queryObjectDetail(String deviceUuid, String name, int type) throws Exception{
        ResultRO<List<ObjectDetailRO>> resultRO = new ResultRO<>(true);
        List<ObjectDetailRO> list = new ArrayList<>();
        //解码,得到格式: A();b;c;D()
        name =  URLDecoder.decode(name, "UTF-8");
        String[] arr = name.split(";");
        int number = 1;
        for (String str : arr) {
            int splitIndex = str.indexOf("()");
            //非对象，直接填充内容
            if (splitIndex == -1) {
                ObjectDetailRO obj = new ObjectDetailRO();
                obj.setValue(str);
                obj.setNumber(number);
                list.add(obj);
            } else {
                String nameRef = str.substring(0, splitIndex);
                ObjectDetailRO obj = null;
                //对象，需要查询
                if(type == 1) {
                    obj = ipServiceNameRefClient.queryIpByName(deviceUuid, nameRef);
                }else if(type == 2) {
                    obj = ipServiceNameRefClient.queryServiceByName(deviceUuid, nameRef);
                }else if(type == 3){
                    obj = ipServiceNameRefClient.queryTimeByName(deviceUuid, nameRef);
                }
                if(obj != null){
                    obj.setNumber(number);
                    list.add(obj);
                }
            }
            number++;
        }

        resultRO.setData(list);
        return resultRO;
    }

    /**
     * 创建公共任务
     * @param taskType
     * @param id
     * @param userName
     * @param theme
     * @param deviceUuid
     * @return
     */
    protected CommandTaskEditableEntity createCommandTask(Integer taskType, Integer id, String userName, String theme, String deviceUuid) {
        CommandTaskEditableEntity entity = new CommandTaskEditableEntity();
        entity.setCreateTime(new Date());
        entity.setStatus(PushConstants.PUSH_INT_PUSH_GENERATING);
        entity.setUserName(userName);
        entity.setTheme(theme);
        entity.setDeviceUuid(deviceUuid);
        entity.setTaskId(id);
        entity.setTaskType(taskType);
        return entity;
    }

    /**
     * 处理域
     * @param zone
     * @return
     */
    protected String getZone(String zone) {
        if (zone == null) {
            return "";
        }
        return zone.equals("-1") ? "" : zone;
    }

    /**
     * 处理分支
     * @param userName
     * @param recommendTaskEntity
     */
    private void getBranch(String userName, RecommendTaskEntity recommendTaskEntity) {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        if (userInfoDTO != null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
            recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            recommendTaskEntity.setBranchLevel("00");
        }
    }

    /**
     * 新增仿真任务
     * @param entity
     */
    protected void addRecommendTask(RecommendTaskEntity entity) {
        logger.info("策略下发新增任务:" + JSONObject.toJSONString(entity));
        List<RecommendTaskEntity> list = new ArrayList<>();
        list.add(entity);
        int count = this.insertRecommendTaskList(list);
        String policyTypeDesc = "";
        if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT) {
            policyTypeDesc = "静态Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT) {
            policyTypeDesc = "源Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT) {
            policyTypeDesc = "目的Nat";
        } else if (entity.getTaskType().intValue() == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT) {
            policyTypeDesc = "Both Nat";
        } else {
            policyTypeDesc = "未知";
        }
        String message = String.format("新建%s策略%s%s", policyTypeDesc, entity.getTheme(), count > 0 ? "成功" : "失败");
        //添加操作日志
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
    }

    @Override
    public void updateWeTaskId(RecommendTaskEntity entity) {
        recommendTaskMapper.updateWeTaskId(entity);
    }
}
