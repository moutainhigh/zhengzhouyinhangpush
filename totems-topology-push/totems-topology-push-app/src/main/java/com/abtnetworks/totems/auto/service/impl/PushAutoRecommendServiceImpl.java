package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.auto.dao.mysql.AutoRecommendTaskMapper;
import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkConfigMapper;
import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkNatMappingMapper;
import com.abtnetworks.totems.auto.dao.mysql.PushZoneLimitConfigMapper;
import com.abtnetworks.totems.auto.dto.*;
import com.abtnetworks.totems.auto.entity.*;
import com.abtnetworks.totems.auto.enums.*;
import com.abtnetworks.totems.auto.manager.AddressManageTaskManager;
import com.abtnetworks.totems.auto.manager.AutoRecommendTaskManager;
import com.abtnetworks.totems.auto.service.*;
import com.abtnetworks.totems.auto.utils.IpAddress;
import com.abtnetworks.totems.auto.utils.ObjectSearhUtil;
import com.abtnetworks.totems.auto.vo.*;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushStatusConstans;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.external.vo.NatRuleMatchFlowVO;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.external.vo.RuleMatchFlowVO;
import com.abtnetworks.totems.generate.task.CmdTaskService;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.entity.*;
import com.abtnetworks.totems.recommend.manager.CommandTaskManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.MergeService;
import com.abtnetworks.totems.remote.nginz.NgRemoteService;
import com.abtnetworks.totems.whale.baseapi.dto.RoutingTableSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.abtnetworks.totems.whale.policy.dto.JsonQueryDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policy.ro.PathFlowRO;
import com.abtnetworks.totems.whale.policy.ro.RoutingEntriesRO;
import com.abtnetworks.totems.whale.policy.ro.RoutingMatchFlowRO;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.abtnetworks.totems.whale.policy.service.WhalePolicyClient;
import com.abtnetworks.totems.whale.policybasic.ro.NextHopRO;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.enums.PolicyTypeEnum.SYSTEM__NAT_LIST;

/**
 * @author liuchanghao
 * @desc 自动开通工单接口实现类
 * @date 2021-06-09 14:05
 */
@Service
public class PushAutoRecommendServiceImpl implements PushAutoRecommendService {

    private static Logger logger = LoggerFactory.getLogger(PushAutoRecommendServiceImpl.class);

    @Autowired
    private PushProtectNetworkConfigService pushProtectNetworkConfigService;

    @Autowired
    private ProtectNetworkNatMappingMapper protectNetworkNatMappingMapper;

    @Autowired
    private ProtectNetworkConfigMapper protectNetworkConfigMapper;

    @Autowired
    private AutoRecommendTaskManager autoRecommendTaskManager;

    @Autowired
    private AutoRecommendTaskMapper autoRecommendTaskMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private RemoteBranchService remoteBranchService;

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    private CommandTaskManager commandTaskManager;

    @Autowired
    private CmdTaskService cmdTaskService;

    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    WhalePathAnalyzeClient whalePathAnalyzeClient;

    @Autowired
    MergeService mergeService;

    @Autowired
    WhalePolicyClient whalePolicyClient;

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;
    /**
     * rest policy
     **/
    @Value("${topology.policy-server-prefix}")
    private String policyServerPrefix;

    @Autowired
    NgRemoteService ngRemoteService;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEdiableMapper;

    /**
     * 源nat mode
     */
    private static final String SNAT_MODE_STRING = "dynamicport";

    /**
     * 山石型号关键字
     */
    private static final String HILLSTONE_MODELNUMBER_KEY = "HILLSTONE";

    /**
     * 飞塔型号关键字
     */
    private static final String FORTINET_MODELNUMBER_KEY = "FORTINET";

    /**
     * 山石待处理域名__VROUTER_IF_GROUP 包含时需要去掉
     */
    private static final String HILLSTONE_ZONE_VROUTER = "__VROUTER_IF_GROUP";

    /**
     * 山石待处理域名__VSWITCH_IF_GROUP 包含时需要去掉
     */
    private static final String HILLSTONE_ZONE_VSWITCH = "__VSWITCH_IF_GROUP";

    /**
     * 飞塔型号关键字
     */
    private static final String INTERFACE_NAME_SPLIT_BY = "_";

    /**
     * any对应的域名
     */
    private static final String ANY_ZONE = "trust";

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    @Qualifier(value = "autoTaskPushExecutor")
    private Executor autoTaskPushExecutor;

    @Autowired
    private PushService pushService;

    @Autowired
    private PushAutoTaskEmailService pushAutoTaskEmailService;

    @Autowired
    private RemotePolicyService remotePolicyService;

    @Autowired
    private AddressManageDetailService addressManageDetailService;

    @Autowired
    private AddressManageTaskManager addressManageTaskManager;

    @Autowired
    private PushZoneLimitConfigMapper pushZoneLimitConfigMapper;

    @Autowired
    private PushAutoRecommendConflictPolicyService pushAutoRecommendConflictPolicyService;

    /**
     * IP为空时，根据此IP去查域和接口信息
     */
    private static final String ANY_IP = "114.114.114.114";

    /**
     * IP和子网相同时的掩码
     */
    private static final String IPV4_MASK = "/32";

    /**
     * 获取接口和域信息的默认路由的查询IP
     */
    private static final String GET_DEFAULT_INTERFACE_IP = "0.0.0.0";

    /**
     * 飞塔获取mip名称正则匹配字符串
     */
    private static final String GET_DNAT_MIP_NAME_STR = "set dstaddr ";

    /**
     * 获取接口和域信息的默认路由的子网
     */
    private static final Integer GET_DEFAULT_INTERFACE_MASK = 0;

    /**
     * IP为空时，根据此IP去查域和接口信息
     */
    @Value("${inside-to-inside.any-find-zone-info}")
    private String anyIpFindZoneInfo;

    @Value("${push.token}")
    private String token;

    @Value("${push.project-ip}")
    private String projectIp;

    @Autowired
    @Qualifier(value = "autoTaskGenerateCommandlineExecutor")
    private Executor autoTaskGenerateCommandlineExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<Integer> addTask(AutoRecommendTaskVO vo) throws Exception {
        AutoRecommendTaskEntity record = new AutoRecommendTaskEntity();
        try {
            if (vo == null || StringUtils.isBlank(vo.getTheme())) {
                return new ReturnT(ReturnT.FAIL_CODE, "参数为空");
            }
            AutoRecommendTaskEntity getByName = autoRecommendTaskMapper.getByName(vo.getTheme());
            if( null != getByName ){
                logger.error("主题为：{} 的工单已存在！", vo.getTheme());
                return new ReturnT(ReturnT.FAIL_CODE, "工单号：" + vo.getTheme() + "已存在！");
            }
            logger.info("------------开始新建自动开通策略为：{}------------", JSONObject.toJSONString(vo));

            // 数据校验
            if(vo.getIpType() == null ){
                vo.setIpType(IpTypeEnum.IPV4.getCode());
            }
            int rc;
            if(InputTypeEnum.SRC_INPUT_TYPE_IP.getCode().equals(vo.getSrcInputType())){
                rc = InputValueUtils.checkIp(vo.getSrcIp());
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                    return new ReturnT(ReturnT.FAIL_CODE, "源IP地址格式错误");
                }

                //若出IP范围起始地址大于终止地址错误，则自动纠正
                if (rc == ReturnCode.INVALID_IP_RANGE && IpTypeEnum.IPV4.getCode().equals(vo.getIpType())) {
                    vo.setSrcIp(InputValueUtils.autoCorrect(vo.getSrcIp()));
                }
            }

            if(InputTypeEnum.DST_INPUT_TYPE_IP.getCode().equals(vo.getSrcInputType())){
                rc = InputValueUtils.checkIp(vo.getDstIp());
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                    return new ReturnT(ReturnT.FAIL_CODE, "目的IP地址格式错误");
                }

                //若出IP范围起始地址大于终止地址错误，则自动纠正
                if (rc == ReturnCode.INVALID_IP_RANGE && IpTypeEnum.IPV4.getCode().equals(vo.getIpType())) {
                    vo.setDstIp(InputValueUtils.autoCorrect(vo.getDstIp()));
                }
            }

            if (StringUtils.isEmpty(vo.getSrcIp()) && StringUtils.isEmpty(vo.getDstIp()) && StringUtils.isEmpty(vo.getSrcAddressObjectName()) && StringUtils.isEmpty(vo.getDstAddressObjectName())) {
                String msg = "源、目的IP均为空，不生成自动开通工单！";
                logger.error(msg);
                return new ReturnT(ReturnT.FAIL_CODE, msg);
            }

            // 客户提出需求，已下发的五元组不能重复再下发 updated on 2021-10-14
            // 已注释—用户又觉得很不方便。。。updated on 2021-11-10
            /*List<AutoRecommendTaskEntity> allTask = autoRecommendTaskMapper.findCannotCreateTaskByConditions(vo.getAccessType());
            if(CollectionUtils.isNotEmpty(allTask)){
                String currentTaskHashcode = this.getHashcode(vo.getSrcIp(), vo.getDstIp(), vo.getServiceList());
                for (AutoRecommendTaskEntity historyTask : allTask ){
                    List<ServiceDTO> serviceList = ServiceDTOUtils.toList(historyTask.getServiceList());
                    String historyTaskHashcode = this.getHashcode(historyTask.getSrcIp(), historyTask.getDstIp(), serviceList);
                    if(StringUtils.equals(currentTaskHashcode, historyTaskHashcode)){
                        logger.error("当前工单五元组信息已存在历史工单，请确认！历史工单信息：{}", JSON.toJSONString(historyTask));
                        String msg = "当前工单五元组信息与历史工单:["+ historyTask.getTheme() +"]相同，请确认";
                        return new ReturnT(ReturnT.FAIL_CODE, msg);
                    }
                }
            }*/

            Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap = new HashMap<>();
            Map<String, Set<AddAutoRecommendTaskDTO>> resultMap = new HashMap<>();
            Map<String, AutoRecommendConflictPolicyDTO> conflictPolicyMap = new HashMap<>();
            Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet= new HashSet<>();

            String streamId = com.abtnetworks.totems.common.utils.DateUtils.formatDate(new Date(), "yyyyMMddHHmmss") + "-" + IdGen.randomBase62(6);
            // 添加工单任务
            record = this.buildAutoRecommendTask(vo, record, errorDetailDTOSet);
            record.setSrcIp(StringUtils.isBlank(vo.getSrcAddressObjectName()) ? vo.getSrcIp() : null);
            record.setDstIp(StringUtils.isEmpty(vo.getDstAddressObjectName()) ? vo.getDstIp() : null);
            autoRecommendTaskManager.insert(record);
            AutoRecommendTaskEntity finalRecord = record;
            autoTaskGenerateCommandlineExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, finalRecord.getTheme(), finalRecord.getTheme(), new Date())) {
                @Override
                protected void start() throws Exception {
                    if(InputTypeEnum.SRC_INPUT_TYPE_IP.getCode().equals(vo.getSrcInputType()) && InputTypeEnum.DST_INPUT_TYPE_IP.getCode().equals(vo.getDstInputType())){
                        // 源和目的都是IP类型，走自动开通-IP 的命令行生成逻辑
                        generateAutoRecommendForIp(vo, policyInfoMap, resultMap, errorDetailDTOSet, finalRecord, conflictPolicyMap);
                    } else {
                        // 源和目的有地址对象类型，走自动开通-地址对象 的命令行生成逻辑
                        generateAutoRecommendForObject(vo, policyInfoMap, resultMap, errorDetailDTOSet, finalRecord, conflictPolicyMap);
                    }
                }
            });

            logger.info("------------新建自动开通策略结束------------");
        } catch (Exception e) {
            logger.error("新增自动开通工单任务异常，异常原因：", e);
            record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            autoRecommendTaskMapper.updateByPrimaryKey(record);
            throw e;
        }
        return new ReturnT(record.getId());

    }

    /**
     * 获取五元组的hashcode值
     * @param srcIp
     * @param dstIp
     * @param serviceList
     * @return
     */
    private String getHashcode(String srcIp, String dstIp, List<ServiceDTO> serviceList){
        String srcHashcode;
        String dstHashcode;
        if(StringUtils.isBlank(srcIp)){
            srcHashcode = "null";
        } else {
            Set<String> srcSet = new HashSet<>(Arrays.asList(srcIp.split(",")));
            srcHashcode = String.valueOf(srcSet.hashCode());
        }
        if(StringUtils.isBlank(dstIp)){
            dstHashcode = "null";
        } else {
            Set<String> dstSet = new HashSet<>(Arrays.asList(dstIp.split(",")));
            dstHashcode = String.valueOf(dstSet.hashCode());
        }
        String serviceHashcode = getServiceListCode(serviceList);
        return srcHashcode + dstHashcode + serviceHashcode;
    }

    /**
     * 获取serviceList的hashcode值
     * @param serviceList
     * @return
     */
    private String getServiceListCode(List<ServiceDTO> serviceList){
        String hashCode ="";
        if(CollectionUtils.isEmpty(serviceList)){
            return "null";
        }
        for(ServiceDTO service : serviceList){
            if(StringUtils.isNotBlank(service.getProtocol())){
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
                if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    return "null";
                }
            }
        }
        serviceList.sort(Comparator.comparing(ServiceDTO::getProtocol));

        for (ServiceDTO service : serviceList){
            String protocol = service.getProtocol();
            String dstPorts = service.getDstPorts();
            Set<String> dstPortsSet = new HashSet<>();
            dstPortsSet.addAll(Arrays.asList(dstPorts.split(",")));
            hashCode += protocol.hashCode();
            hashCode += dstPortsSet.hashCode();
        }

        return hashCode;
    }

    /**
     * 关联策略生成里面的数据（安全策略、源Nat、目的Nat）
     * @param entity
     * @param deviceUuid
     * @param taskId
     * @param jsonArray
     */
    private void buildRelevancyNat(RecommendTaskEntity entity, String deviceUuid, Integer taskId, Integer taskType, JSONArray jsonArray){
        NodeEntity node = nodeMapper.getTheNodeByUuid(deviceUuid);
        JSONObject jsonObject = new JSONObject();
        String name = String.format("%s(%s(%s))", entity.getTheme(), node.getDeviceName(), node.getIp());
        jsonObject.put("name", name);
        jsonObject.put("id", entity.getId());
        jsonObject.put("taskId", taskId);
        jsonObject.put("taskType", taskType);
        jsonArray.add(jsonObject);
    }

    /**
     * 创建安全策略
     * @param taskDTO
     * @param samePartDTO
     */
    private RecommendTaskEntity createSecurityTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO){
        RecommendTaskEntity record = new RecommendTaskEntity();
        BeanUtils.copyProperties(taskDTO, record);
        record.setTheme(samePartDTO.getTheme());
        record.setTaskType(samePartDTO.getTaskType());
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber= "A" + simpleDateFormat.format(date);
        record.setOrderNumber(orderNumber);
        record.setUserName(samePartDTO.getUserName());
        record.setBranchLevel(samePartDTO.getUserInfoDTO() == null ? "00" : samePartDTO.getUserInfoDTO().getBranchLevel());

        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(taskDTO.getServiceList())) {
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTOList.add(serviceDTO);
            record.setServiceList(JSONObject.toJSONString(serviceDTOList));
        } else {
            serviceDTOList = taskDTO.getServiceList();
            for (ServiceDTO service : serviceDTOList) {
                if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                    service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                }
            }
            record.setServiceList(JSONObject.toJSONString(taskDTO.getServiceList()));
        }

        PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
        additionalInfoEntity.setDeviceUuid(taskDTO.getDeviceUuid());
        additionalInfoEntity.setSrcZone(taskDTO.getSrcZone());
        additionalInfoEntity.setDstZone(taskDTO.getDstZone());
        additionalInfoEntity.setOutDevItf(taskDTO.getOutDevIf());
        additionalInfoEntity.setAction("PERMIT");
        additionalInfoEntity.setInDevItf(taskDTO.getInDevIf());
        additionalInfoEntity.setInDevItfAlias(taskDTO.getInDevItfAlias());
        additionalInfoEntity.setOutDevItfAlias(taskDTO.getOutDevItfAlias());

        record.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));

        record.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        record.setCreateTime(new Date());
        record.setSrcAddressObjectName(taskDTO.getSrcAddressObjectName());
        record.setDstAddressObjectName(taskDTO.getDstAddressObjectName());
        record.setSrcIp(StringUtils.isBlank(taskDTO.getSrcAddressObjectName()) ? taskDTO.getSrcIp() : "");
        record.setDstIp(StringUtils.isEmpty(taskDTO.getDstAddressObjectName()) ? taskDTO.getDstIp() : "");
        record.setIpType(samePartDTO.getIpType());

        recommendTaskMapper.insert(record);
        return record;
    }

    /**
     * 创建源Nat任务
     * @param taskDTO
     * @param samePartDTO
     */
    private RecommendTaskEntity createSnatTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO, AutoRecommendTaskEntity record){
        // 创建源NAT附加信息对象
        NodeEntity nodeEntity = taskDTO.getNode();
        String dstIp = taskDTO.getDstIp();
        // 如果转换是源nat，并且转换后的源IP为空，则使用出接口
        String postSrcIp = taskDTO.getPostSrcIp();
        List<ServiceDTO> serviceList = taskDTO.getServiceList();
        if(StringUtils.isBlank(postSrcIp)){
            if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                postSrcIp = "";
                dstIp = "";
                serviceList = new ArrayList<>();
            } else {
                postSrcIp = taskDTO.getOutDevIf();
            }
        }

        SNatAdditionalInfoEntity additionalInfoEntity = new SNatAdditionalInfoEntity(taskDTO.getDeviceUuid(),
                postSrcIp, taskDTO.getSrcZone(), taskDTO.getInDevIf(),
                taskDTO.getDstZone(), taskDTO.getOutDevIf(), SNAT_MODE_STRING, taskDTO.getInDevItfAlias(),taskDTO.getOutDevItfAlias());

        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(samePartDTO.getTheme(), samePartDTO.getUserName(),
                taskDTO.getSrcIp(), dstIp, JSONObject.toJSONString(serviceList),
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, JSONObject.toJSONString(additionalInfoEntity),
                null, null, null, null, samePartDTO.getIpType());
        recommendTaskEntity.setBranchLevel(samePartDTO.getUserInfoDTO() == null ? "00" : samePartDTO.getUserInfoDTO().getBranchLevel());
        recommendTaskEntity.setStartTime(record.getStartTime());
        recommendTaskEntity.setEndTime(record.getEndTime());
        // 添加任务数据到策略下发任务列表
        recommendTaskMapper.insert(recommendTaskEntity);
        return recommendTaskEntity;
    }

    /**
     *  构建服务对象
     * @param taskDTO
     * @return
     */
    private List<ServiceDTO> createPreServiceList(RecommendPolicyDTO taskDTO){
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        String postProtocol = taskDTO.getPostProtocol();
        // 存在nat映射关系的协议和端口映射
        if(StringUtils.isNotBlank(postProtocol) && StringUtils.isNotBlank(taskDTO.getPrePorts())){
            String protocol = ProtocolUtils.getProtocolNumberByName(postProtocol);
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol(protocol);

            NodeEntity nodeEntity = taskDTO.getNode();
            String dstPorts = InputValueUtils.autoCorrectPorts(taskDTO.getPrePorts());
            if(null != nodeEntity){
                if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                    dstPorts = InputValueUtils.autoCorrectPorts(taskDTO.getPostPorts());
                }
            }
            serviceDTO.setDstPorts(dstPorts);
            serviceDTOList.add(serviceDTO);
        }
        return serviceDTOList;
    }
    /**
     *  构建服务对象
     * @param taskDTO
     * @return
     */
    private List<ServiceDTO> createPostServiceList(RecommendPolicyDTO taskDTO){
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        String postProtocol = taskDTO.getPostProtocol();
        // 存在nat映射关系的协议和端口映射
        if(StringUtils.isNotBlank(postProtocol) && StringUtils.isNotBlank(taskDTO.getPostPorts())){
            String protocol = ProtocolUtils.getProtocolNumberByName(postProtocol);
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol(protocol);

            NodeEntity nodeEntity = taskDTO.getNode();
            String dstPorts = InputValueUtils.autoCorrectPorts(taskDTO.getPostPorts());
            if(null != nodeEntity){
                if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                    dstPorts = InputValueUtils.autoCorrectPorts(taskDTO.getPrePorts());
                }
            }
            serviceDTO.setDstPorts(dstPorts);
            serviceDTOList.add(serviceDTO);
        } else {
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTOList.add(serviceDTO);
        }
        return serviceDTOList;
    }

    /**
     * 创建目的Nat任务
     * @param taskDTO
     * @param samePartDTO
     */
    private RecommendTaskEntity createDnatTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO, AutoRecommendTaskEntity record){
        // 创建目的NAT附加信息数据对象
        List<ServiceDTO> postServiceDTOList = this.createPostServiceList(taskDTO);
        List<ServiceDTO> preServiceDTOList = this.createPreServiceList(taskDTO);
        NodeEntity nodeEntity = taskDTO.getNode();
        String postPorts = taskDTO.getPostPorts();
        if(ObjectUtils.isNotEmpty(nodeEntity)){
            if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                postPorts = taskDTO.getPrePorts();
            }
        }

        DNatAdditionalInfoEntity additionalInfoEntity = new DNatAdditionalInfoEntity();
        additionalInfoEntity.setDeviceUuid(taskDTO.getDeviceUuid());
        additionalInfoEntity.setPostIpAddress(taskDTO.getPostDstIp());
        additionalInfoEntity.setPostPort(postPorts);
        additionalInfoEntity.setSrcZone(taskDTO.getSrcZone());
        additionalInfoEntity.setSrcItf(taskDTO.getInDevIf());
        additionalInfoEntity.setDstZone(taskDTO.getDstZone());
        additionalInfoEntity.setDstItf(taskDTO.getOutDevIf());
        additionalInfoEntity.setInDevItfAlias(taskDTO.getInDevItfAlias());
        additionalInfoEntity.setOutDevItfAlias(taskDTO.getOutDevItfAlias());
        additionalInfoEntity.setPostServiceList(postServiceDTOList);
        additionalInfoEntity.setPreServiceDTOList(preServiceDTOList);
        if(ObjectUtils.isNotEmpty(taskDTO.getFortinetDnatSpecialDTO())){
            additionalInfoEntity.setFortinetDnatSpecialDTO(taskDTO.getFortinetDnatSpecialDTO());
        }

        String dstIp = taskDTO.getDstIp();
        if(ObjectUtils.isNotEmpty(nodeEntity)){
            if (!StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                dstIp = taskDTO.getPreDstIp();
            }
        }

        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(samePartDTO.getTheme(), samePartDTO.getUserName(),
                taskDTO.getSrcIp(), dstIp, JSONObject.toJSONString(taskDTO.getServiceList()),
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, JSONObject.toJSONString(additionalInfoEntity),
                null, null, null, null,samePartDTO.getIpType());
        recommendTaskEntity.setBranchLevel(samePartDTO.getUserInfoDTO() == null ? "00" : samePartDTO.getUserInfoDTO().getBranchLevel());
        recommendTaskEntity.setEndTime(record.getEndTime());
        // 添加任务数据到策略下发任务列表
        recommendTaskMapper.insert(recommendTaskEntity);
        return recommendTaskEntity;
    }

    /**
     * 根据禁止数据流过滤策略开通建议
     * @param policyDTOSet
     * @param deviceUuid
     * @param ipType
     * @param errorDetailDTOSet
     * @param record
     * @param conflictPolicyMap
     * @return
     */
    private List<RecommendPolicyDTO> checkDataFlow(Set<RecommendPolicyDTO> policyDTOSet, String deviceUuid, Integer ipType, Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet,
                                                   AutoRecommendTaskEntity record, Map<String, AutoRecommendConflictPolicyDTO> conflictPolicyMap) {
        List<RecommendPolicyDTO> filterPolicyDTOList = new ArrayList<>();
        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
        for (RecommendPolicyDTO policyDTO : policyDTOSet) {
            RecommendPolicyDTO filterPolicyDTO = new RecommendPolicyDTO();

            PolicyDTO policy = new PolicyDTO();
            DeviceDTO device = new DeviceDTO();
            device.setDeviceUuid(deviceUuid);
            BeanUtils.copyProperties(policyDTO, policy);
            policy.setIpType(ipType);
            logger.info("根据策略：{} 和设备UUID：{}查询数据流", JSON.toJSONString(policy), device.getDeviceUuid());
            RuleMatchFlowVO ruleMatchFlow = ngRemoteService.getRuleMatchFlow(policy, device);
            logger.info("查询数据流响应数据:{}", JSON.toJSONString(ruleMatchFlow));
            if (ObjectUtils.isNotEmpty(ruleMatchFlow)) {
                List<PathFlowRO> deny = ruleMatchFlow.getDeny();
                if (CollectionUtils.isEmpty(deny)) {
                    logger.error("查询到禁止数据流为空，安全策略：{} 已开通，不生成自动开通策略建议", JSON.toJSONString(policyDTO));
                    this.setErrorMsg(nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）安全策略已开通 [Security policy already exists]", policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getServiceList(), policyDTO.getStartTime(), policyDTO.getEndTime(),
                            AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode(),errorDetailDTOSet,ruleMatchFlow.getRuleList(), record.getAccessType(),nodeEntity.getUuid(),policyDTO.getSrcZone(),policyDTO.getDstZone(),null);
                    continue;
                }
                // 添加到自动开通冲突策略表
                this.batchInsert(ruleMatchFlow.getRuleList(),record.getId(), record.getTheme(), "0");

                BeanUtils.copyProperties(policyDTO, filterPolicyDTO);
                filterPolicyDTOList.add(filterPolicyDTO);
                // 添加安全策略的冲突策略
                if(conflictPolicyMap.containsKey(deviceUuid)){
                    AutoRecommendConflictPolicyDTO autoRecommendConflictPolicyDTO = conflictPolicyMap.get(deviceUuid);
                    Set<PolicyDetailVO> securityConflictPolicyDTOSet = autoRecommendConflictPolicyDTO.getSecurityConflictPolicyDTOSet();
                    securityConflictPolicyDTOSet.addAll(ruleMatchFlow.getRuleList());
                } else {
                    AutoRecommendConflictPolicyDTO autoRecommendConflictPolicyDTO = new AutoRecommendConflictPolicyDTO();
                    Set<PolicyDetailVO> securityConflictPolicyDTOSet = new HashSet<>();
                    securityConflictPolicyDTOSet.addAll(ruleMatchFlow.getRuleList());
                    autoRecommendConflictPolicyDTO.setSecurityConflictPolicyDTOSet(securityConflictPolicyDTOSet);
                    conflictPolicyMap.put(deviceUuid, autoRecommendConflictPolicyDTO);
                }
            }
        }

        return filterPolicyDTOList;
    }

    @Override
    public ReturnT<List<String>> remoteGetRoutTableRuleList(String deviceUuid, String policyListUuid, String content) throws Exception{
        try{
            RoutingTableSearchDTO searchDTO = buildSearchDTO(deviceUuid, policyListUuid, content);
            if(null == searchDTO ){
                return new ReturnT(ReturnT.FAIL_CODE, "请输入一个正确的ip、子网或范围！");
            }

            ResultRO<List<RoutingMatchFlowRO>> dataResultRO = whalePathAnalyzeClient.getRoutingMatchFlow(searchDTO);
            if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                return new ReturnT(ReturnT.FAIL_CODE, "未查询到路由表数据信息");
            }

            List<String> interfaceNameList = new ArrayList<>();
            List<RoutingMatchFlowRO> routData = dataResultRO.getData();
            // 当前不处理返回的 匹配数据流集合
            for (RoutingMatchFlowRO routingMatchFlowRO : routData ){
                RoutingEntriesRO routingEntry = routingMatchFlowRO.getRoutingEntry();
                if ( null == routingEntry ){
                    logger.error("获取到的路由信息为空，不生成策略信息");
                    continue;
                }
                NextHopRO nextHop = routingEntry.getNextHop();
                if (null == nextHop ) {
                    logger.error("获取到的下一跳信息为空，不生成策略信息");
                    continue;
                }
                if (StringUtils.isNotEmpty(nextHop.getInterfaceName())){
                    interfaceNameList.add(nextHop.getInterfaceName());
                }
            }
            logger.info("根据IP：{} 获取到出接口为：{}", content, interfaceNameList);
            return new ReturnT(interfaceNameList);
        } catch (Exception e ) {
            logger.error("获取路由表出接口异常，异常原因：", e);
            throw e;
        }
    }

    /**
     * 此方法逻辑：优先拿配置的IP精确查找路由，所有的IP都未找到精确路由后，
     * 每个IP分别去找默认接口（0.0.0.0/0对应的接口）
     * 当前方法只适用于IPV4
     * @param deviceUuid
     * @param policyListUuid
     * @param anyIpFindZoneInfo
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<List<String>> remoteGetRoutTableRuleListByInside2Inside(String deviceUuid, String policyListUuid, String anyIpFindZoneInfo) throws Exception{
        List<String> firstNameList = new ArrayList<>();
        List<String> secondNameList = new ArrayList<>();
        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
        if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "H3C")){
            firstNameList.add(PolicyConstants.POLICY_STR_VALUE_ANY);
            return new ReturnT(firstNameList);
        }else {
            try {
                String[] anyIpFindZoneInfos = anyIpFindZoneInfo.split(",");
                for (String content : anyIpFindZoneInfos) {
                    String configIp = content.split("/")[0];
                    String configMask = content.split("/")[1];
                    RoutingTableSearchDTO searchDTO = this.buildSearchDTO(deviceUuid, policyListUuid, content);
                    if(null == searchDTO ){
                        logger.error("当前输入的IP、子网或范围:{}格式不正确", content);
                        continue;
                    }
                    ResultRO<List<RoutingEntriesRO>> dataResultRO = whaleDevicePolicyClient.searchRout(searchDTO);
                    if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                        logger.error("根据配置IP：{} 未查询到路由表数据信息，跳过，开始查询下一配置IP", content);
                        continue;
                    }

                    List<RoutingEntriesRO> routData = dataResultRO.getData();
                    // 当前不处理返回的 匹配数据流集合
                    for (RoutingEntriesRO routingEntriesRO : routData ){
                        NextHopRO nextHop = routingEntriesRO.getNextHop();
                        if (null == nextHop ) {
                            logger.error("获取到的下一跳信息为空，不生成策略信息");
                            continue;
                        }

                        String ip4Prefix = routingEntriesRO.getIp4Prefix();
                        Integer mask = routingEntriesRO.getMaskLength();
                        if(StringUtils.equals(configIp, ip4Prefix) && Integer.parseInt(configMask) == mask ){
                            if (StringUtils.isNotEmpty(nextHop.getInterfaceName())){
                                logger.info("根据IP：{} 获取到出接口为：{}", content, firstNameList.toString());
                                this.handleHillstoneZone(nextHop, firstNameList);
                                return new ReturnT(firstNameList);
                            }
                        } else {
                            if(StringUtils.equals(GET_DEFAULT_INTERFACE_IP, ip4Prefix) && GET_DEFAULT_INTERFACE_MASK.equals(mask) ){
                                logger.info("根据IP：{} 获取到默认出接口为：{}", content, firstNameList.toString());
                                this.handleHillstoneZone(nextHop, secondNameList);
                            }
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(firstNameList)){
                    return new ReturnT(firstNameList);
                } else if(CollectionUtils.isNotEmpty(secondNameList)){
                    return new ReturnT(secondNameList);
                } else {
                    return new ReturnT(ReturnT.FAIL_CODE, "未查询到配置的路由接口或默认接口");
                }

            } catch (Exception e ) {
                logger.error("获取路由表出接口异常，异常原因：", e);
                throw e;
            }
        }
    }

    /**
     * 判断（山石）域信息是否包含关键字，包含则截取处理
     * @param nextHop
     * @param zoneNameList
     */
    private void handleHillstoneZone(NextHopRO nextHop, List<String> zoneNameList){
        if(StringUtils.contains(nextHop.getInterfaceName(),HILLSTONE_ZONE_VROUTER)){
            String zoneName = nextHop.getInterfaceName().substring(0, nextHop.getInterfaceName().lastIndexOf(HILLSTONE_ZONE_VROUTER));
            zoneNameList.add(zoneName);
        } else if(StringUtils.contains(nextHop.getInterfaceName(),HILLSTONE_ZONE_VSWITCH)){
            String zoneName = nextHop.getInterfaceName().substring(0, nextHop.getInterfaceName().lastIndexOf(HILLSTONE_ZONE_VSWITCH));
            zoneNameList.add(zoneName);
        } else {
            zoneNameList.add(nextHop.getInterfaceName());
        }
    }

    /**
     * 构建SearchDTO
     * @param deviceUuid
     * @param policyListUuid
     * @param content
     * @return
     */
    private RoutingTableSearchDTO buildSearchDTO(String deviceUuid, String policyListUuid, String content){
        RoutingTableSearchDTO searchDTO = new RoutingTableSearchDTO();

        searchDTO.setDeviceUuid(deviceUuid);
        searchDTO.setRoutingTableUuid(policyListUuid);

        IPTypeEnum addressIpType = null;
        if (StringUtils.isNotBlank(content)) {
            //判断是IPv4，还是IPv6
            if (IPUtil.isIP(content) || IPUtil.isIPRange(content) || IPUtil.isIPSegment(content)) {
                addressIpType = IPTypeEnum.IP4;
                List<CommonRangeStringDTO> addressRanges = ObjectSearhUtil.getRangeByContent(content);
                searchDTO.setSearchType("DEST_IP");
                searchDTO.setAddressRanges(addressRanges);
            } else if (IPUtil.isIPv6(content) || IPUtil.isIPv6Range(content) || IPUtil.isIPv6Segment(content)) {
                addressIpType = IPTypeEnum.IP6;
                List<CommonRangeStringDTO> addressRanges = ObjectSearhUtil.getIP6RangeByContent(content);
                searchDTO.setSearchType("DEST_IP6");
                searchDTO.setIp6AddressRanges(addressRanges);
            }

            if (addressIpType == null) {
                return null;
            }
        }
        return searchDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnT delete(AutoRecommendTaskVO vo) throws Exception {
        if (CollectionUtils.isNotEmpty(vo.getIdList())) {
            for (Integer id : vo.getIdList()) {
                AutoRecommendTaskEntity autoRecommendTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(id);
                if (StringUtils.isNotEmpty(autoRecommendTaskEntity.getRelevancyNat())) {
                    String relevancyNat = autoRecommendTaskEntity.getRelevancyNat();
                    JSONArray jsonArray = JSONArray.parseArray(relevancyNat);

                    for (int index = 0; index < jsonArray.size(); index++) {
                        logger.info("开始删除关联策略生成工单{}...", autoRecommendTaskEntity.getTheme());
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        Integer natId = jsonObject.getInteger("id");
                        commandTaskEdiableMapper.deleteByTaskId(natId);
                        recommendTaskMapper.deleteByTaskId(natId);
                    }
                }
                logger.info("开始删除自动开通任务工单:[]...", autoRecommendTaskEntity.getTheme());
                autoRecommendTaskMapper.deleteByPrimaryKey(id);
            }
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public PageInfo<AutoRecommendTaskEntity> findList(AutoRecommendTaskSearchVO vo, int pageNum, int pageSize) {
        AutoRecommendTaskEntity record = new AutoRecommendTaskEntity();
        BeanUtils.copyProperties(vo, record);
        List<AutoRecommendTaskEntity> taskEntityList = new ArrayList<>();
        List<AutoRecommendTaskEntity> taskEntities = autoRecommendTaskManager.findList(record);
        if (CollectionUtils.isNotEmpty(taskEntities)){
            List<CommandTaskEditableEntity> taskEditableEntityList = new ArrayList<>();
            List<AutoRecommendTaskEntity> finalTaskEntityList = taskEntityList;
            taskEntities.forEach(p -> {
                if(StringUtils.isNotEmpty(p.getRelevancyNat())){
                    String relevancyNat = p.getRelevancyNat();
                    JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
                    if (null != jsonArray && jsonArray.size() > 0){
                        for (int index = 0; index < jsonArray.size(); index++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(index);
                            Integer natId = jsonObject.getInteger("id");
                            List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(natId);
                            if (CollectionUtils.isNotEmpty(commandTaskEditableList)) {
                                // 更新下发状态
                                taskEditableEntityList.addAll(commandTaskEditableList);
                            }
                        }
                        int pushStatus;
                        if (CollectionUtils.isNotEmpty(taskEditableEntityList)){
                            int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(taskEditableEntityList);
                            if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_NOT_START) {
                                pushStatus = AutoRecommendStatusEnum.PUSH_NOT_START.getCode();
                            } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_PUSHING) {
                                pushStatus = AutoRecommendStatusEnum.PUSHING.getCode();
                            } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_FINISHED) {
                                pushStatus = AutoRecommendStatusEnum.PUSH_SUCCESS.getCode();
                            } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_FAILED) {
                                pushStatus = AutoRecommendStatusEnum.PUSH_FAIL.getCode();
                            } else if (pushStatusInTaskList == PushStatusConstans.PUSH_INT_PUSH_QUEUED) {
                                pushStatus = AutoRecommendStatusEnum.PUSH_WAITING.getCode();
                            } else {
                                pushStatus = AutoRecommendStatusEnum.PUSH_SUCCESS_PARTS.getCode();
                            }

                            if (p.getStatus() != pushStatus){
                                p.setStatus(pushStatus);
                                finalTaskEntityList.add(p);
                            }
                        }
                        taskEditableEntityList.clear();
                    }
                }
            });

            if (CollectionUtils.isNotEmpty(finalTaskEntityList)){
                autoRecommendTaskMapper.updateList(finalTaskEntityList);
            }
        }
        taskEntityList.clear();
        List<AutoRecommendTaskEntity> taskEntities1 = autoRecommendTaskManager.findList(record);
        taskEntityList = taskEntities1.stream().filter(p -> p.getStatus().intValue() == AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS.getCode() ||
                p.getStatus().intValue() == AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS.getCode()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            taskEntityList.stream().forEach(p -> {
                if (isPartSuccess(p)){
                    p.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS_PARTS.getCode());
                }else {
                    p.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS.getCode());
                }
            });
            if (CollectionUtils.isNotEmpty(taskEntityList)){
                autoRecommendTaskMapper.updateList(taskEntityList);
            }
        }

        PageHelper.startPage(pageNum, pageSize);
        List<AutoRecommendTaskEntity> taskList = autoRecommendTaskManager.findList(record);
        PageInfo<AutoRecommendTaskEntity> pageInfo = new PageInfo<>(taskList);
        return pageInfo;
    }

    @Override
    public AutoRecommendTaskNatInfoDTO getNatInfo(AutoRecommendTaskGetInfoVO getInfoVO) {
        AutoRecommendTaskNatInfoDTO natInfoDTO = new AutoRecommendTaskNatInfoDTO();
        RecommendTaskEntity recommendTaskEntity = recommendTaskMapper.getById(getInfoVO.getTaskId());

        if (null == recommendTaskEntity || !getInfoVO.getTaskType().equals(recommendTaskEntity.getTaskType())) {
            return null;
        }
        List<CommandTaskEditableEntity> commandTaskEditableEntities = commandTaskEdiableMapper.selectByTaskId(getInfoVO.getTaskId());
        if(CollectionUtils.isEmpty(commandTaskEditableEntities)){
            return natInfoDTO;
        }
        natInfoDTO.setTheme(recommendTaskEntity.getTheme());

        natInfoDTO.setStartTime(recommendTaskEntity.getStartTime());
        natInfoDTO.setEndTime(recommendTaskEntity.getEndTime());
        natInfoDTO.setUserName(recommendTaskEntity.getUserName());
        natInfoDTO.setAction("PERMIT");
        natInfoDTO.setServiceList(recommendTaskEntity.getServiceList());
        natInfoDTO.setPushStatus(commandTaskEditableEntities.get(0).getPushStatus());
        JSONObject object = JSONObject.parseObject(recommendTaskEntity.getAdditionInfo());
        if (getInfoVO.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT)) {
            // 源Nat，内网访问互联网
            if (object != null) {
                SNatAdditionalInfoEntity sNatAdditionalInfoEntity = object.toJavaObject(SNatAdditionalInfoEntity.class);
                natInfoDTO.setSrcZone(formatZoneItfString(sNatAdditionalInfoEntity.getSrcZone(), sNatAdditionalInfoEntity.getSrcItf()));
                natInfoDTO.setDstZone(formatZoneItfString(sNatAdditionalInfoEntity.getDstZone(), sNatAdditionalInfoEntity.getDstItf()));
                natInfoDTO.setPreSrcIp(recommendTaskEntity.getSrcIp());
                natInfoDTO.setPostSrcIp(StringUtils.isNotEmpty(sNatAdditionalInfoEntity.getPostIpAddress()) ? sNatAdditionalInfoEntity.getPostIpAddress() : sNatAdditionalInfoEntity.getDstItf());
                natInfoDTO.setPreDstIp(recommendTaskEntity.getDstIp());
                NodeEntity node = nodeMapper.getTheNodeByUuid(sNatAdditionalInfoEntity.getDeviceUuid());
                natInfoDTO.setDeviceIp(node.getIp());
                natInfoDTO.setDeviceName(node.getDeviceName());
            }
        } else if (getInfoVO.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT)) {
            // 目的Nat，互联网访问内网
            DNatAdditionalInfoEntity dNatAdditionalInfoEntity = object.toJavaObject(DNatAdditionalInfoEntity.class);
            natInfoDTO.setSrcZone(formatZoneItfString(dNatAdditionalInfoEntity.getSrcZone(), dNatAdditionalInfoEntity.getSrcItf()));
            natInfoDTO.setDstZone(formatZoneItfString(dNatAdditionalInfoEntity.getDstZone(), dNatAdditionalInfoEntity.getDstItf()));
            natInfoDTO.setPreSrcIp(recommendTaskEntity.getSrcIp());
            natInfoDTO.setPreDstIp(recommendTaskEntity.getDstIp());
            natInfoDTO.setPostServiceList(dNatAdditionalInfoEntity.getPostServiceList());
            natInfoDTO.setPostDstIp(dNatAdditionalInfoEntity.getPostIpAddress());
            NodeEntity node = nodeMapper.getTheNodeByUuid(dNatAdditionalInfoEntity.getDeviceUuid());
            natInfoDTO.setDeviceIp(node.getIp());
            natInfoDTO.setDeviceName(node.getDeviceName());
        } else {
            // 安全策略，内网互访
            AutoRecommendTaskAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(AutoRecommendTaskAdditionalInfoEntity.class);
            natInfoDTO.setDstZone(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getOutDevItf()));
            natInfoDTO.setSrcZone(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getInDevItf()));
            natInfoDTO.setPreDstIp(recommendTaskEntity.getDstIp());
            natInfoDTO.setPreSrcIp(recommendTaskEntity.getSrcIp());
            NodeEntity node = nodeMapper.getTheNodeByUuid(additionalInfoEntity.getDeviceUuid());
            natInfoDTO.setDeviceName(node.getDeviceName());
            natInfoDTO.setDeviceIp(node.getIp());
        }
        natInfoDTO.setCreateTime(recommendTaskEntity.getCreateTime());
        return natInfoDTO;
    }

    @Override
    public ReturnT autoPush(AutoRecommendTaskVO vo) {
        StringBuilder errMsg = new StringBuilder();
        List<AutoRecommendTaskEntity> autoEntityList = new ArrayList<>();
        boolean taskDelete = true;
        boolean isDevicePush = true;
        //自动开通工单所对应的设备中安全策略和nat策略的taskId
        if(ObjectUtils.isNotEmpty(vo.getTaskIdList())){
            isDevicePush = false;
        }
        Set<Integer> deleteIdSet = new HashSet<>();
        Map<String, List<CommandTaskEditableEntity>> taskMap = new HashMap<>();
        for (Integer id : vo.getIdList()) {
            List<RecommendTaskEntity> entityList = new ArrayList<>();
            AutoRecommendTaskEntity autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(id);
            if (autoTaskEntity == null) {
                logger.warn("根据ID：{} 未查询到自动开通任务工单", id);
                continue;
            }

            if (isDevicePush) {
                // 命令行生成失败、下发等待中、下发中、策略已开通、下发成功不允许下发
                if(autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode()) ||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSH_WAITING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSHING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSH_SUCCESS.getCode())) {
                    logger.error("工单：{} 状态为：{}，不允许下发!", autoTaskEntity.getTheme(), AutoRecommendStatusEnum.getDescByCode(autoTaskEntity.getStatus()));
                    continue;
                }
            }else {
                // 下发等待中、下发中、策略已开通、不允许下发
                if(autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSH_WAITING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSHING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode())) {
                    logger.error("工单：{} 状态为：{}，不允许下发!", autoTaskEntity.getTheme(), AutoRecommendStatusEnum.getDescByCode(autoTaskEntity.getStatus()));
                    continue;
                }
            }
            autoEntityList.add(autoTaskEntity);
            try {
                // 针对未生成完命令行的数据，不加入下发
                boolean isAutoPush = false;
                String relevancyNat = autoTaskEntity.getRelevancyNat();
                JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
                if(isDevicePush){
                    for (int index = 0; index < jsonArray.size(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        Integer natId = jsonObject.getInteger("id");
                        List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(natId);
                        if (CollectionUtils.isEmpty(commandTaskEditableList)) {
                            deleteIdSet.add(id);
                            isAutoPush = true;
                            break;
                        }

                        for (CommandTaskEditableEntity editTaskEntity : commandTaskEditableList) {
                            String command = editTaskEntity.getCommandline();
                            if (StringUtils.isEmpty(command)) {
                                deleteIdSet.add(id);
                                isAutoPush = true;
                                break;
                            }
                        }
                    }
                }
                if (isAutoPush) {
                    logger.info("当前工单：{}存在未生成完命令行的数据，不自动下发", autoTaskEntity.getTheme());
                    continue;
                }

                // 更新状态为下发中
                taskDelete = false;
                autoTaskEntity.setStatus(AutoRecommendStatusEnum.PUSHING.getCode());
                autoRecommendTaskMapper.updateByPrimaryKey(autoTaskEntity);

                CountDownLatch latch = new CountDownLatch(jsonArray.size());
                try {
                    for (int index = 0; index < jsonArray.size(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        Integer natId = jsonObject.getInteger("id");
                        RecommendTaskEntity taskEntity = recommendTaskMapper.getById(natId);
                        entityList.add(taskEntity);
                        if (!isDevicePush && !vo.getTaskIdList().contains(natId)){
                            // 计数器-1
                            latch.countDown();
                            continue;
                        }
                        String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss") + "-" + IdGen.randomBase62(6);
                        autoTaskPushExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, autoTaskEntity.getTheme(), autoTaskEntity.getTheme(), new Date())) {
                            @Override
                            protected void start() throws Exception {


                                List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(natId);
                                boolean ignore = false;
                                for (CommandTaskEditableEntity editTaskEntity : commandTaskEditableList) {
                                    String command = editTaskEntity.getCommandline();
                                    if (StringUtils.isNotBlank(command) && command.startsWith("无法生成该设备的命令行")) {
                                        errMsg.append(String.format("[%s]开始下发失败，存在未生成命令行的设备！", editTaskEntity.getTheme()));
                                        ignore = true;
                                        deleteIdSet.add(id);
                                        break;
                                    }
                                }
                                if (ignore) {
                                    latch.countDown();
                                    return;
                                }

                                CommandTaskDTO taskDTO = new CommandTaskDTO();
                                taskDTO.setRevert(vo.getIsRevert());

                                taskDTO.setList(commandTaskEditableList);

                                taskDTO.setTaskId(taskEntity.getId());
                                taskDTO.setTheme(taskEntity.getTheme());

                                String message = String.format("工单：%s ，开始%s", taskEntity.getTheme(), vo.getIsRevert() ? "回滚" : "下发");
                                logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

                                // 下发命令行
                                pushService.pushCommand(taskDTO);
                                // 计数器-1
                                latch.countDown();
                            }
                        });

                    }
                } catch (Exception e) {
                    logger.error("自动开通工单任务下发异常，异常原因：", e);
                    latch.countDown();
                    throw e;
                }

                try {
                    latch.await();
                } catch (Exception e) {
                    logger.error("自动开通工单任务下发异常", e);
                }
                // 更新下发状态
                List<CommandTaskEditableEntity> taskEditableEntityList = new ArrayList<>();
                for (RecommendTaskEntity recommendTaskEntity : entityList) {
                    List<CommandTaskEditableEntity> partList = commandTaskManager.getCommandTaskByTaskId(recommendTaskEntity.getId());
                    taskEditableEntityList.addAll(partList);
                }
                int pushStatus;
                int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(taskEditableEntityList);
                if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_NOT_START) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_NOT_START.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_PUSHING) {
                    pushStatus = AutoRecommendStatusEnum.PUSHING.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_FINISHED) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_SUCCESS.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_FAILED) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_FAIL.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_INT_PUSH_QUEUED) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_WAITING.getCode();
                } else {
                    pushStatus = AutoRecommendStatusEnum.PUSH_SUCCESS_PARTS.getCode();
                }

                autoTaskEntity.setStatus(pushStatus);
                autoRecommendTaskMapper.updateByPrimaryKey(autoTaskEntity);

                taskMap.put(autoTaskEntity.getTheme(), taskEditableEntityList);
                /*//下发完成后发送邮件
                pushAutoTaskEmailService.startAutoRecommendPushEmail(taskEditableEntityList, autoTaskEntity);*/


            } catch (Exception e) {
                logger.error("自动开通工单下发异常，异常原因：", e);
                autoTaskEntity.setStatus(AutoRecommendStatusEnum.PUSH_FAIL.getCode());
                autoRecommendTaskMapper.updateByPrimaryKey(autoTaskEntity);
                throw e;
            }

        }
        // 处理被删除的工单状态
        if(taskDelete){
            for(Integer id : deleteIdSet){
                AutoRecommendTaskEntity autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(id);
                autoTaskEntity.setStatus(AutoRecommendStatusEnum.PUSH_FAIL.getCode());
                autoRecommendTaskMapper.updateByPrimaryKey(autoTaskEntity);
            }
            return new ReturnT(ReturnT.FAIL_CODE, "策略建议不存在或已删除，下发失败");
        }

        //下发完成后发送邮件-根据主题号批量发送邮件
        pushAutoTaskEmailService.startAutoRecommendPushBatchTaskEmail(taskMap, autoEntityList);
        return ReturnT.SUCCESS;
    }


    @Override
    public ReturnT getResult(AutoRecommendTaskVO vo) {
        AutoRecommendAllResultDTO allResultDTO = new AutoRecommendAllResultDTO();

        List<AutoRecommendOrderStatusDTO> orderStatusDTOList = new ArrayList<>();

        Map<String, Object> resultConflictMap = new HashMap<>();
        AutoRecommendTaskEntity autoTaskEntity;
        if(StringUtils.isNotBlank(vo.getTheme())){
            autoTaskEntity = autoRecommendTaskMapper.getByName(vo.getTheme());
            if(autoTaskEntity == null ){
                logger.info("根据ID：{} 未查询到自动开通任务工单", vo.getId());
                return new ReturnT(allResultDTO);
            }
        } else if(ObjectUtils.isNotEmpty(vo.getId())){
            autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(vo.getId());
            if(autoTaskEntity == null ){
                logger.info("根据ID：{} 未查询到自动开通任务工单", vo.getId());
                return new ReturnT(allResultDTO);
            }
        } else {
            return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
        }

        String url = projectIp + "/appmodules/views/autoOpenTaskDetail/index.html?access_token=" + token + "&flag=zte&theme=" + autoTaskEntity.getTheme();
        allResultDTO.setDetailPageUrl(url);

        if(StringUtils.isNotBlank(autoTaskEntity.getErrorMsg())){
            allResultDTO.setErrorDetailDTOList(JSONArray.parseArray(autoTaskEntity.getErrorMsg(), AutoRecommendErrorDetailDTO.class));
        }

        // 设置 不开通/已开通 工单状态数据
        this.setErrorOrderStatusList(allResultDTO.getErrorDetailDTOList(), orderStatusDTOList);

        List<CommandTaskEditableEntity> allCmdList = new ArrayList<>();
        Map<CommandTaskEditableEntity, RecommendTaskEntity> mappingMap = new HashMap<>();

        String relevancyNat = autoTaskEntity.getRelevancyNat();
        String conflictPolicy = autoTaskEntity.getConflictPolicy();
        if(StringUtils.isNotBlank(conflictPolicy)){
            resultConflictMap = JSON.parseObject(conflictPolicy, HashMap.class);
        }
        if(StringUtils.isNotBlank(relevancyNat)){
            JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
            for (int index = 0; index < jsonArray.size(); index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                Integer entityId = jsonObject.getInteger("id");

                RecommendTaskEntity recommendTaskEntity = recommendTaskMapper.getById(entityId);

                List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(entityId);
                if (CollectionUtils.isEmpty(commandTaskEditableList)){
                    continue;
                }
                for (CommandTaskEditableEntity data : commandTaskEditableList){
                    // 建立映射关系
                    mappingMap.put(data, recommendTaskEntity);
                    allCmdList.add(data);
                }
            }
        }

        if(CollectionUtils.isEmpty(allCmdList)){
            // TODO 临时方案：如果是策略已开通，将状态置为下发成功
            boolean contaisnExistPolicy = false;
            boolean contaisnErrorPolicy = false;
            if(CollectionUtils.isNotEmpty(allResultDTO.getErrorDetailDTOList())){
                for (AutoRecommendErrorDetailDTO errorMsg : allResultDTO.getErrorDetailDTOList()) {
                    if(ObjectUtils.isNotEmpty(errorMsg)){
                        if(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode().equals(errorMsg.getStatus()) ||
                                AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode().equals(errorMsg.getStatus())){
                            contaisnExistPolicy = true;
                        } else {
                            contaisnErrorPolicy = true;
                        }
                    }
                }
            }
            if(contaisnExistPolicy && !contaisnErrorPolicy){
                allResultDTO.setDemandStatus(AutoRecommendStatusEnum.PUSH_SUCCESS.getCode());
            } else {
                allResultDTO.setDemandStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            }
            allResultDTO.setOrderStatusDTOList(orderStatusDTOList);
            return new ReturnT(allResultDTO);
        }

        Map<String, AutoRecommendResultDTO> dataMap = new HashMap<>();
        for (CommandTaskEditableEntity data : allCmdList ) {
            RecommendTaskEntity recommendTaskEntity = mappingMap.get(data);
            // 针对状态特殊处理，详情中展示下发状态，不展示任务状态
            List<CommandTaskEditableEntity> commandTaskEditableEntities = commandTaskEdiableMapper.selectByTaskId(recommendTaskEntity.getId());
            if(CollectionUtils.isEmpty(commandTaskEditableEntities)){
                continue;
            }
            recommendTaskEntity.setStatus(commandTaskEditableEntities.get(0).getPushStatus());

            String deviceUuid = data.getDeviceUuid();
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            String deviceName = nodeEntity.getDeviceName();
            DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
            DeviceDataRO deviceData = device.getData().get(0);
            // 如果是虚墙时需显示虚墙名称
            if (deviceData.getIsVsys() != null && deviceData.getIsVsys()) {
                deviceName = deviceData.getVsysName();
            }

            if(dataMap.containsKey(data.getDeviceUuid())){
                AutoRecommendResultDTO resultDTO = dataMap.get(data.getDeviceUuid());
                Set<RecommendTaskEntity> securityPolicyDTOSet = resultDTO.getSecurityPolicyDTOSet();
                Set<RecommendTaskEntity> natPolicyDTOSet = resultDTO.getNatPolicyDTOSet();
                List<CommandTaskEditableEntity> commandTaskEditableList = resultDTO.getCommandTaskEditableList();
                if(data.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED)){
                    securityPolicyDTOSet.add(recommendTaskEntity);
                } else {
                    natPolicyDTOSet.add(recommendTaskEntity);
                }
                commandTaskEditableList.add(data);

                resultDTO.setDeviceName(deviceName);
                resultDTO.setDeviceIp(nodeEntity.getIp());
                resultDTO.setNatPolicyDTOSet(natPolicyDTOSet);
                resultDTO.setSecurityPolicyDTOSet(securityPolicyDTOSet);
                resultDTO.setCommandTaskEditableList(commandTaskEditableList);

                if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                    String fortinetDnatMipName = this.getDnatMipName(commandTaskEditableList);
                    if(StringUtils.isNotBlank(fortinetDnatMipName)){
                        natPolicyDTOSet.stream().forEach(natPolicyDTO ->{
                            natPolicyDTO.setFortinetDnatMipName(fortinetDnatMipName);
                        });
                    }
                }

                // 设置冲突策略
                if(resultConflictMap.containsKey(data.getDeviceUuid())){
                    String conflictPolicyDTOString = resultConflictMap.get(data.getDeviceUuid()).toString();
                    AutoRecommendConflictPolicyDTO conflictPolicyDTO = JSON.parseObject(conflictPolicyDTOString, AutoRecommendConflictPolicyDTO.class);
                    if(ObjectUtils.isNotEmpty(conflictPolicyDTO)){
                        resultDTO.setSecurityConflictPolicyDTOSet(conflictPolicyDTO.getSecurityConflictPolicyDTOSet());
                        resultDTO.setNatConflictPolicyDTOSet(conflictPolicyDTO.getNatConflictPolicyDTOSet());
                    }
                }
                dataMap.put(data.getDeviceUuid(), resultDTO);
            } else {
                Set<RecommendTaskEntity> securityPolicyDTOSet = new HashSet<>();
                Set<RecommendTaskEntity> natPolicyDTOSet = new HashSet<>();
                List<CommandTaskEditableEntity> cmdList = new ArrayList<>();
                AutoRecommendResultDTO resultDTO = new AutoRecommendResultDTO();
                if(data.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED)){
                    securityPolicyDTOSet.add(recommendTaskEntity);
                } else {
                    natPolicyDTOSet.add(recommendTaskEntity);
                }
                cmdList.add(data);

                resultDTO.setDeviceIp(nodeEntity.getIp());
                resultDTO.setDeviceName(nodeEntity.getDeviceName());
                resultDTO.setCommandTaskEditableList(cmdList);
                resultDTO.setSecurityPolicyDTOSet(securityPolicyDTOSet);
                resultDTO.setNatPolicyDTOSet(natPolicyDTOSet);

                if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                    String fortinetDnatMipName = this.getDnatMipName(cmdList);
                    if(StringUtils.isNotBlank(fortinetDnatMipName)){
                        natPolicyDTOSet.stream().forEach(natPolicyDTO ->{
                            natPolicyDTO.setFortinetDnatMipName(fortinetDnatMipName);
                        });
                    }
                }

                // 设置冲突策略
                if(resultConflictMap.containsKey(data.getDeviceUuid())){
                    String conflictPolicyDTOString = resultConflictMap.get(data.getDeviceUuid()).toString();
                    AutoRecommendConflictPolicyDTO conflictPolicyDTO = JSON.parseObject(conflictPolicyDTOString, AutoRecommendConflictPolicyDTO.class);
                    if(ObjectUtils.isNotEmpty(conflictPolicyDTO)){
                        resultDTO.setNatConflictPolicyDTOSet(conflictPolicyDTO.getNatConflictPolicyDTOSet());
                        resultDTO.setSecurityConflictPolicyDTOSet(conflictPolicyDTO.getSecurityConflictPolicyDTOSet());
                    }
                }
                dataMap.put(data.getDeviceUuid(), resultDTO);
            }
        }

        // 根据策略开通建议生成待开通工单状态数据
        this.setWaitCreateOrderStatusList(dataMap, orderStatusDTOList);
        allResultDTO.setDemandStatus(autoTaskEntity.getStatus());
        allResultDTO.setDataMap(dataMap);
        allResultDTO.setOrderStatusDTOList(orderStatusDTOList);

        return new ReturnT(allResultDTO);
    }


    /**
     * 获取目的Nat时，飞塔设备的Mip名称
     * @param cmdList
     * @return
     */
    private String getDnatMipName(List<CommandTaskEditableEntity> cmdList){
        if(CollectionUtils.isEmpty(cmdList)){
            return null;
        }

        for (CommandTaskEditableEntity editableEntity : cmdList ) {
            String commandline = editableEntity.getCommandline();
            if(StringUtils.isBlank(commandline)){
                return null;
            }
            String[] commandLines = commandline.split("\n");
            for (String comStr : commandLines) {
                Pattern pattern = Pattern.compile(GET_DNAT_MIP_NAME_STR);
                Matcher matcher = pattern.matcher(comStr);
                if (matcher.find()) {
                    String ruleLine = StringUtils.substring(comStr,matcher.end()).trim();
                    String ruleName = ruleLine;
                    if (StringUtils.isNotEmpty(ruleName) && (ruleName.startsWith("mip") || ruleName.startsWith("vip"))) {
                        // 飞塔映射关系名称以mip开头或vip开头
                        return ruleName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 设置 不开通/已开通 的策略工单状态
     * @param errorDetailDTOList
     * @param orderStatusDTOList
     */
    private void setErrorOrderStatusList(List<AutoRecommendErrorDetailDTO> errorDetailDTOList, List<AutoRecommendOrderStatusDTO> orderStatusDTOList){
        // 不开通策略
        AutoRecommendOrderStatusDTO errorOrderStatusDTO = new AutoRecommendOrderStatusDTO();
        AutoRecommendOrderStatusDTO existOrderStatusDTO = new AutoRecommendOrderStatusDTO();
        Set<String> errorSrcIpList = new HashSet<>();
        Set<String> errorDstIpList = new HashSet<>();
        Set<List<ServiceDTO>> errorServiceList = new HashSet<>();
        Set<NodeEntity> errorEntityList = new HashSet<>();

        // 已存在策略
        Set<String> existSrcIpList = new HashSet<>();
        Set<List<ServiceDTO>> existServiceList = new HashSet<>();
        Set<String> existDstIpList = new HashSet<>();
        Set<NodeEntity> existEntityList = new HashSet<>();
        for (AutoRecommendErrorDetailDTO errorDetailDTO : errorDetailDTOList) {
            if(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode().equals(errorDetailDTO.getStatus()) ||
                    AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode().equals(errorDetailDTO.getStatus())){
                this.setOrderStatusData(errorDetailDTO,existSrcIpList, existDstIpList, existServiceList, existEntityList);
            } else {
                this.setOrderStatusData(errorDetailDTO,errorSrcIpList, errorDstIpList, errorServiceList, errorEntityList);
            }
        }
        errorOrderStatusDTO.setSrcIpList(errorSrcIpList);
        errorOrderStatusDTO.setDstIpList(errorDstIpList);
        errorOrderStatusDTO.setServiceList(errorServiceList);
        errorOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_GENERATE_ERROR.getCode());
        errorOrderStatusDTO.setEntityList(errorEntityList);
        orderStatusDTOList.add(errorOrderStatusDTO);

        existOrderStatusDTO.setSrcIpList(existSrcIpList);
        existOrderStatusDTO.setDstIpList(existDstIpList);
        existOrderStatusDTO.setServiceList(existServiceList);
        existOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_HAS_EXIST.getCode());
        existOrderStatusDTO.setEntityList(existEntityList);
        orderStatusDTOList.add(existOrderStatusDTO);
    }

    /**
     * 设置 不开通/已开通 的策略工单状态
     * @param errorDetailDTOList
     * @param securityOrderStatusDTOList
     * @param natOrderStatusDTOList
     */
    private void setErrorOrderStatusListNew(List<AutoRecommendErrorDetailDTO> errorDetailDTOList, List<AutoRecommendOrderStatusNewDTO> securityOrderStatusDTOList,
                                            List<AutoRecommendOrderStatusNewDTO> natOrderStatusDTOList){
        // 不开通策略
        AutoRecommendOrderStatusNewDTO securityOrderStatusDTO = new AutoRecommendOrderStatusNewDTO();
        AutoRecommendOrderStatusNewDTO natOrderStatusDTO = new AutoRecommendOrderStatusNewDTO();
        AutoRecommendOrderStatusNewDTO errorOrderStatusDTO = new AutoRecommendOrderStatusNewDTO();

        Set<AutoRecommendOrderDTO> securityOrderDTOSet = new HashSet<>();
        Set<AutoRecommendOrderDTO> natOrderDTOSet = new HashSet<>();
        Set<AutoRecommendOrderDTO> errorOrderDTOSet = new HashSet<>();
        for (AutoRecommendErrorDetailDTO errorDetailDTO : errorDetailDTOList) {
            if(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode().equals(errorDetailDTO.getStatus())){
                AutoRecommendOrderDTO securityOrderDTO = new AutoRecommendOrderDTO();
                securityOrderDTO.setSrcIp(errorDetailDTO.getSrcIp());
                securityOrderDTO.setDstIp(errorDetailDTO.getDstIp());
                securityOrderDTO.setServiceList(errorDetailDTO.getServiceList());

                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setIp(errorDetailDTO.getDeviceIp());
                nodeEntity.setDeviceName(errorDetailDTO.getDeviceName());
                securityOrderDTO.setEntity(nodeEntity);
                securityOrderDTOSet.add(securityOrderDTO);
            }

            if(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode().equals(errorDetailDTO.getStatus())){
                AutoRecommendOrderDTO natOrderDTO = new AutoRecommendOrderDTO();
                natOrderDTO.setSrcIp(errorDetailDTO.getSrcIp());
                natOrderDTO.setDstIp(errorDetailDTO.getDstIp());
                natOrderDTO.setServiceList(errorDetailDTO.getServiceList());

                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setIp(errorDetailDTO.getDeviceIp());
                nodeEntity.setDeviceName(errorDetailDTO.getDeviceName());
                natOrderDTO.setEntity(nodeEntity);
                natOrderDTOSet.add(natOrderDTO);
            }

            // TODO status为命令行生成失败的，状态未添加
            if(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode().equals(errorDetailDTO.getStatus())){
                AutoRecommendOrderDTO errorOrderDTO = new AutoRecommendOrderDTO();
                errorOrderDTO.setSrcIp(errorDetailDTO.getSrcIp());
                errorOrderDTO.setDstIp(errorDetailDTO.getDstIp());
                errorOrderDTO.setServiceList(errorDetailDTO.getServiceList());

                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setIp(errorDetailDTO.getDeviceIp());
                nodeEntity.setDeviceName(errorDetailDTO.getDeviceName());
                errorOrderDTO.setEntity(nodeEntity);
                errorOrderDTOSet.add(errorOrderDTO);
            }
        }

        // 安全策略已开通
        if(CollectionUtils.isNotEmpty(securityOrderDTOSet)){
            securityOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_HAS_EXIST.getCode());
            securityOrderStatusDTO.setOrderDTOSet(securityOrderDTOSet);
            securityOrderStatusDTOList.add(securityOrderStatusDTO);
        }

        // NAT策略已开通
        if(CollectionUtils.isNotEmpty(natOrderDTOSet)) {
            natOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_HAS_EXIST.getCode());
            natOrderStatusDTO.setOrderDTOSet(natOrderDTOSet);
            natOrderStatusDTOList.add(natOrderStatusDTO);
        }
        // 安全策略不开通
        if(CollectionUtils.isNotEmpty(errorOrderDTOSet)) {
            errorOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_GENERATE_ERROR.getCode());
            errorOrderStatusDTO.setOrderDTOSet(errorOrderDTOSet);
            securityOrderStatusDTOList.add(errorOrderStatusDTO);
        }
    }

    /**
     * 创建待开通的工单状态数据
     * @param dataMap
     * @param orderStatusDTOList
     */
    private void setWaitCreateOrderStatusList(Map<String, AutoRecommendResultDTO> dataMap, List<AutoRecommendOrderStatusDTO> orderStatusDTOList){
        // 待开通工单数据
        Set<String> waitCreateSrcIpList = new HashSet<>();
        Set<String> waitCreateDstIpList = new HashSet<>();
        Set<List<ServiceDTO>> waitCreateServiceList = new HashSet<>();
        Set<NodeEntity> waitCreateEntityList = new HashSet<>();
        AutoRecommendOrderStatusDTO waitCreateOrderStatusDTO = new AutoRecommendOrderStatusDTO();

        Set<AutoRecommendResultDTO> resultDTOSet = new HashSet<>(dataMap.values());
        for (AutoRecommendResultDTO resultDTO : resultDTOSet) {
            Set<RecommendTaskEntity> allRecommendTaskSet = new HashSet<>();
            allRecommendTaskSet.addAll(resultDTO.getSecurityPolicyDTOSet());
            allRecommendTaskSet.addAll(resultDTO.getNatPolicyDTOSet());
            for (RecommendTaskEntity taskEntity : allRecommendTaskSet) {
                String srcIp = StringUtils.isBlank(taskEntity.getSrcAddressObjectName()) ? taskEntity.getSrcIp() : taskEntity.getSrcAddressObjectName();
                String dstIp = StringUtils.isBlank(taskEntity.getDstAddressObjectName()) ? taskEntity.getDstIp() : taskEntity.getDstAddressObjectName();
                waitCreateSrcIpList.addAll(Arrays.asList(srcIp.split(",")));
                waitCreateDstIpList.addAll(Arrays.asList(dstIp.split(",")));
                List<ServiceDTO> serviceList = ServiceDTOUtils.toList(taskEntity.getServiceList());
                if(CollectionUtils.isNotEmpty(serviceList)){
                    waitCreateServiceList.add(serviceList);
                }

            }
            NodeEntity nodeEntity = new NodeEntity();
            nodeEntity.setIp(resultDTO.getDeviceIp());
            nodeEntity.setDeviceName(resultDTO.getDeviceName());
            waitCreateEntityList.add(nodeEntity);
        }
        waitCreateOrderStatusDTO.setSrcIpList(waitCreateSrcIpList);
        waitCreateOrderStatusDTO.setDstIpList(waitCreateDstIpList);
        waitCreateOrderStatusDTO.setServiceList(waitCreateServiceList);
        waitCreateOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_WAIT_CREATE.getCode());
        waitCreateOrderStatusDTO.setEntityList(waitCreateEntityList);
        orderStatusDTOList.add(waitCreateOrderStatusDTO);
    }

    /**
     * 设置工单状态值
     * @param errorDetailDTO
     * @param srcIpList
     * @param dstIpList
     * @param serviceList
     * @param entityList
     */
    private void setOrderStatusData(AutoRecommendErrorDetailDTO errorDetailDTO, Set<String> srcIpList, Set<String> dstIpList,
                                    Set<List<ServiceDTO>> serviceList, Set<NodeEntity> entityList){
        if(StringUtils.containsIgnoreCase(errorDetailDTO.getErrorMsg(), "Nat映射关系已存在")){
            logger.info("Nat映射关系已存在的工单数据不添加到工单状态表中");
            return;
        }
        if(StringUtils.isNotBlank(errorDetailDTO.getSrcIp())){
            String srcIp = errorDetailDTO.getSrcIp();
            srcIpList.addAll(Arrays.asList(srcIp.replaceAll(ANY_IP,"").split(",")));
        }
        if(StringUtils.isNotBlank(errorDetailDTO.getDstIp())){
            String dstIp = errorDetailDTO.getDstIp();
            dstIpList.addAll(Arrays.asList(dstIp.replaceAll(ANY_IP,"").split(",")));
        }
        if(CollectionUtils.isNotEmpty(errorDetailDTO.getServiceList())){
            serviceList.add(errorDetailDTO.getServiceList());
        }
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setIp(errorDetailDTO.getDeviceIp());
        nodeEntity.setDeviceName(errorDetailDTO.getDeviceName());
        entityList.add(nodeEntity);
    }

    /**
     * 设置工单状态值
     * @param errorDetailDTO
     * @param srcIpList
     * @param dstIpList
     * @param serviceList
     * @param entityList
     */
    private void setOrderStatusDataNew(AutoRecommendErrorDetailDTO errorDetailDTO, Set<String> srcIpList, Set<String> dstIpList,
                                       Set<List<ServiceDTO>> serviceList, Set<NodeEntity> entityList){
        if(StringUtils.containsIgnoreCase(errorDetailDTO.getErrorMsg(), "Nat映射关系已存在")){
            logger.info("Nat映射关系已存在的工单数据不添加到工单状态表中");
            return;
        }
        if(StringUtils.isNotBlank(errorDetailDTO.getSrcIp())){
            String srcIp = errorDetailDTO.getSrcIp();
            srcIpList.addAll(Arrays.asList(srcIp.replaceAll(ANY_IP,"").split(",")));
        }
        if(StringUtils.isNotBlank(errorDetailDTO.getDstIp())){
            String dstIp = errorDetailDTO.getDstIp();
            dstIpList.addAll(Arrays.asList(dstIp.replaceAll(ANY_IP,"").split(",")));
        }
        if(CollectionUtils.isNotEmpty(errorDetailDTO.getServiceList())){
            serviceList.add(errorDetailDTO.getServiceList());
        }
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setIp(errorDetailDTO.getDeviceIp());
        nodeEntity.setDeviceName(errorDetailDTO.getDeviceName());
        entityList.add(nodeEntity);
    }


    /**
     * 检查防护网段配置，查询域和接口信息
     *
     * @param vo
     * @param policyInfoMap
     * @param resultMap
     * @return
     */
    private void checkProtectNetworkConfig(AutoRecommendTaskVO vo, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                           Map<String, Set<AddAutoRecommendTaskDTO>> resultMap,
                                           Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet) throws Exception{
        // 1.初步筛选出设备关联的五元组信息
        StringBuilder srcErrorMsg = new StringBuilder();
        boolean srcFindNode = false;
        boolean dstFindNode = false;

        Map<String, String> errorMsgMap = new HashMap<>();
        boolean matchSNatMapping = false;
        List<AutoRecommendErrorDetailDTO> srcErrorDetailList = new ArrayList<>();
        List<AutoRecommendErrorDetailDTO> dstErrorDetailList = new ArrayList<>();
        if (!PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())){
            if (StringUtils.isNotEmpty(vo.getSrcIp())) {
                String[] srcIpList = vo.getSrcIp().split(",");
                Map<String, Boolean> securityExistMap = new HashMap<>();
                Map<String, Boolean> isCleanSecurityPolicyMap = new HashMap<>();
                for (String srcIp : srcIpList) {
                    if(IpUtils.isIPSegment(srcIp) && StringUtils.equalsAnyIgnoreCase(IpUtils.getMaskBitFromIpSegment(srcIp), "0")){
                        policyInfoMap.clear();
                        errorDetailDTOSet.clear();
                        this.setErrorMsg("禁止开通掩码为0的工单 [Creating policy with subnet mask 0 is prohibited]", srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        return;
                    }

                    // 生成安全策略
                    List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(srcIp, vo);
                    if (CollectionUtils.isEmpty(list)) {
                        if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                            String msg = "源IP:" + srcIp + "找不到防护网段数据，请检查防护网段配置 [Source ip is not in protect network table,please check the protect network table]";
                            srcErrorMsg.append(msg);
                            AutoRecommendErrorDetailDTO srcErrorDetailDTO = this.setSrcErrorMsg(msg, srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                    vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),vo.getAccessType());
                            srcErrorDetailList.add(srcErrorDetailDTO);
                            continue;
                        } else {
                            errorMsgMap.put(srcIp, vo.getDstIp());
                            continue;
                        }
                    }
                    srcFindNode = true;
                    for (ProtectNetworkConfigEntity configEntity : list) {
                        Set<AddAutoRecommendTaskDTO> taskDTOSet= new HashSet<>();
                        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                        if (null == nodeEntity) {
                            String msg = configEntity.getDeviceIp() + "防火墙已删除，请更新防护网段配置 [Source ip can't find relevant firewall in protect network table,please check the protect network table]";
                            srcErrorMsg.append(msg);
                            this.setErrorMsg(msg, srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
                            continue;
                        }
                        String deviceUuid = nodeEntity.getUuid();
                        securityExistMap.put(deviceUuid, true);
                        srcIp = StringUtils.isBlank(configEntity.getConvertRangeIp()) ? srcIp : configEntity.getConvertRangeIp();

                        String[] srcIps = srcIp.split(",");

                        this.setSrcSecurityData(srcIps, vo, nodeEntity,policyInfoMap,deviceUuid,taskDTOSet);
                    }
                }

                // 内网访问互联网时检查nat映射关系，同时生成安全策略和源Nat策略
                for (String srcIp : srcIpList) {
                    if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                        List<ProtectNetworkNatMappingEntity> snatMappingEntityList = protectNetworkNatMappingMapper.selectByNatType(PushNatTypeEnum.NAT_TYPE_S.getCode());
                        // 如果是内网访问互联网并且打开映射关系
                        // 匹配原则:当源IP匹配到nat映射关系中的内网IP时，使用当前数据做nat命令
                        for (ProtectNetworkNatMappingEntity natMappingEntity : snatMappingEntityList) {
                            if(IpUtils.checkIpRange(srcIp, natMappingEntity.getOutsideIp())){
                                logger.info("当前IP:{}已匹配到源Nat映射关系", srcIp);
                                matchSNatMapping = true;

                                Set<AddAutoRecommendTaskDTO> taskDTOSet= new HashSet<>();
                                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.selectByPrimaryKey(natMappingEntity.getConfigId());
                                if( null == configEntity ){
                                    logger.error("根据配置id:{} 未查询到防护网段配置", natMappingEntity.getConfigId());
                                    continue;
                                }

                                // 检查设备是否存在
                                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                                if (nodeEntity == null) {
                                    String msg = "防火墙:" + configEntity.getDeviceIp() + "已下线，请检查防护网段配置 [Firewall offline,please check the protect network table]";
                                    srcErrorMsg.append(msg);
                                    this.setErrorMsg(msg, srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                            AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
                                    continue;
                                }

                                srcFindNode = true;
                                // 设置源nat策略建议数据
                                this.setSnatData(natMappingEntity, srcIp,vo, nodeEntity,policyInfoMap,nodeEntity.getUuid(),taskDTOSet, PushNatTypeEnum.NAT_TYPE_S.getCode());

                                // 设置源nat的对应安全策略建议数据
                                if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                                    if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                        logger.info("当前设备的安全策略已生成，不再重复生成");
                                        Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                        Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                        policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                        isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                        continue;

                                    }
                                    logger.info("当前为飞塔设备，不生成安全策略建议命令行，命令行存在于nat");
                                    continue;
                                }

                                // 山石墙特殊处理
                                if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                    logger.info("当前设备的安全策略已生成，兼容山石的目的IP特殊情况，删除后重新生成安全策略");
                                    Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                    Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                    policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                    isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                }

                                this.setSnatData(natMappingEntity, srcIp,vo, nodeEntity,policyInfoMap,nodeEntity.getUuid(),taskDTOSet, PushNatTypeEnum.NAT_TYPE_N.getCode());
                            }
                        }
                    }

                    /*if (!matchSNatMapping && PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                        logger.error("当前IP:{}找不到防护网段Nat映射关系数据，请检查防护网段配置", srcIp);
                        String errorMsg = "当前IP:"+ srcIp + "找不到防护网段源Nat映射关系数据，请检查防护网段配置 [IP:"+ srcIp +" can't find relevant source Nat mapping in project network table,please check the protect network table]";
                        this.setErrorMsg(errorMsg,srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        continue;
                    }*/
                }
            }

            // 如果是内访外并且匹配到源nat，则源nat未找到防护网段不提示
            if(matchSNatMapping && PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())){
                srcErrorDetailList.clear();
            } else {
                errorDetailDTOSet.addAll(srcErrorDetailList);
            }

            // 如果访问类型是内网访问互联网，并且源IP找不到防护地址，则直接提示错误信息
            /*if(PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType()) && !matchSNatMapping){
                policyInfoMap.clear();
                return;
            }*/
        }

        StringBuilder dstErrorMsg = new StringBuilder();
        boolean allMatchDNatMapping = false;
        if (!PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())){
            if (StringUtils.isNotEmpty(vo.getDstIp())) {
                String[] dstIpList = vo.getDstIp().split(",");
                Map<String, Boolean> securityExistMap = new HashMap<>();
                Map<String, Boolean> isCleanSecurityPolicyMap = new HashMap<>();
                boolean currentMatchDNatMapping = false;
                for (String dstIp : dstIpList) {
                    if(IpUtils.isIPSegment(dstIp) && StringUtils.equalsAnyIgnoreCase(IpUtils.getMaskBitFromIpSegment(dstIp), "0")){
                        policyInfoMap.clear();
                        errorDetailDTOSet.clear();
                        this.setErrorMsg("禁止开通掩码为0的工单 [Prohibit create policy with subnet mask 0]", vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        return;
                    }

                    // 内网互访情况下，只用生成安全策略
                    List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(dstIp, vo);
                    if (CollectionUtils.isEmpty(list)) {
                        if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                            String msg = "目的IP:" + dstIp + "找不到防护网段数据，请检查防护网段配置 [Destination ip is not in the protect network table,please check the protect network table]";
                            srcErrorMsg.append(msg);

                            AutoRecommendErrorDetailDTO srcErrorDetailDTO = this.setSrcErrorMsg(msg, vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                    vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),vo.getAccessType());
                            dstErrorDetailList.add(srcErrorDetailDTO);

                            continue;
                        } else {
                            String[] errorIP = srcAndDstAllError(errorMsgMap, vo.getSrcIp(), dstIp);
                            if(ObjectUtils.isNotEmpty(errorIP) && errorIP.length == 2){
                                String msg = "源IP："+ errorIP[0] +"目的IP:" + errorIP[1] + "均找不到防护网段数据，请检查防护网段配置 [Source ip:"+errorIP[0]+" and destination ip:"+ errorIP[1]+" are not in the protect network table,please check the protect network table]";
                                dstErrorMsg.append(msg);
                                this.setErrorMsg(msg, errorIP[0], errorIP[1], vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
                            }
                            continue;
                        }
                    }

                    dstFindNode = true;
                    for (ProtectNetworkConfigEntity configEntity : list) {

                        Set<AddAutoRecommendTaskDTO> taskDTOSet = new HashSet<>();
                        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                        if (null == nodeEntity) {
                            String msg = "防火墙:" + configEntity.getDeviceIp() + "已下线，请检查防护网段配置 [Firewall offline,please check the protect network table]";
                            dstErrorMsg.append(msg);
                            this.setErrorMsg(msg, vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(), null,null,null,null);
                            continue;
                        }
                        String deviceUuid = nodeEntity.getUuid();
                        securityExistMap.put(deviceUuid, true);
                        // 此处用交集的范围IP去匹配Nat映射关系、去查询域和接口信息
                        dstIp = StringUtils.isBlank(configEntity.getConvertRangeIp()) ? dstIp : configEntity.getConvertRangeIp();
                        String[] dstIps = dstIp.split(",");
                        this.setDstSecurityData(vo,nodeEntity,  policyInfoMap, deviceUuid, taskDTOSet, dstIps);
                    }
                }

                // 如果打开映射关系并且查到的nat类型为目的Nat
                // 匹配原则，当目的IP匹配到nat映射关系中的内网IP时，使用当前数据做nat命令
                for (String dstIp : dstIpList) {
                    // 外网访问互联网时检查nat映射关系
                    if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                        List<ProtectNetworkNatMappingEntity> dnatMappingEntityList = protectNetworkNatMappingMapper.selectByNatType(PushNatTypeEnum.NAT_TYPE_D.getCode());
                        for (ProtectNetworkNatMappingEntity natMappingEntity : dnatMappingEntityList) {
                            if(!StringUtils.equalsAnyIgnoreCase(natMappingEntity.getNatType(), PushNatTypeEnum.NAT_TYPE_D.getCode())){
                                continue;
                            }
                            // 当内网IP在映射关系内，并且内网端口在输入的工单协议端口范围内时，做端口映射
                            if(IpUtils.checkIpRange(dstIp, natMappingEntity.getInsideIp()) &&
                                    isContainsPort(natMappingEntity.getInsideProtocol(), natMappingEntity.getInsidePorts(), vo.getServiceList())){

                                logger.info("当前IP:{}已匹配到Nat映射关系", dstIp);
                                currentMatchDNatMapping = true;
                                allMatchDNatMapping = true;

                                Set<AddAutoRecommendTaskDTO> taskDTOSet= new HashSet<>();
                                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.selectByPrimaryKey(natMappingEntity.getConfigId());
                                if( null == configEntity ){
                                    logger.error("根据配置id:{} 未查询到防护网段配置", natMappingEntity.getConfigId());
                                    continue;
                                }
                                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                                if (null == nodeEntity) {
                                    String msg = "防火墙:" + configEntity.getDeviceIp() + "已下线，请检查防护网段配置 [Firewall offline,please check the protect network table]";
                                    dstErrorMsg.append(msg);
                                    this.setErrorMsg(msg, vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                            AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(), null,null,null,null);
                                    continue;
                                }

                                AddAutoRecommendTaskDTO taskDTO = new AddAutoRecommendTaskDTO();

                                taskDTO.setPreProtocol(natMappingEntity.getOutsideProtocol());
                                taskDTO.setPostProtocol(natMappingEntity.getInsideProtocol());
                                // 山石型号特殊处理nat映射关系
                                if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                                    taskDTO.setPrePorts(natMappingEntity.getInsidePorts());
                                    taskDTO.setPostPorts(natMappingEntity.getOutsidePorts());
                                } else {
                                    taskDTO.setPrePorts(natMappingEntity.getOutsidePorts());
                                    taskDTO.setPostPorts(natMappingEntity.getInsidePorts());
                                }

                                dstFindNode = true;
                                taskDTO.setPreDstIp(natMappingEntity.getOutsideIp());
                                taskDTO.setPostDstIp(natMappingEntity.getInsideIp());
                                // 山石型号特殊处理nat映射关系
                                // 注释原因：H3C集团的目的IP填的是转换后的内网IP，与山石不同
                                /*if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                                    taskDTO.setDstIp(getRealIP(dstIp));
                                } else {
                                    taskDTO.setDstIp(natMappingEntity.getInsideIp());
                                }*/
                                if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                                    taskDTO.setDstIp(natMappingEntity.getOutsideIp());
                                } else {
                                    taskDTO.setDstIp(getRealIP(dstIp));
                                }
                                taskDTO.setDstRangeIp(getRealIP(dstIp));
                                taskDTO.setNatType(PushNatTypeEnum.NAT_TYPE_D.getCode());

                                // 添加目的Nat策略，设置公共字段属性
                                this.setData(taskDTO, vo,nodeEntity,  policyInfoMap, nodeEntity.getUuid(), taskDTOSet);
                                // 设置命令行中的安全策略协议端口为转换前端口
                                if(StringUtils.isNotBlank(natMappingEntity.getInsideProtocol())){
                                    List<ServiceDTO> serviceList = new ArrayList<>();
                                    ServiceDTO serviceDTO = new ServiceDTO();
                                    String protocol = ProtocolUtils.getProtocolNumberByName(natMappingEntity.getInsideProtocol());
                                    serviceDTO.setProtocol(protocol);
                                    serviceDTO.setDstPorts(natMappingEntity.getOutsidePorts());
                                    serviceList.add(serviceDTO);
                                    taskDTO.setServiceList(serviceList);
                                }

                                // 添加对应的安全策略
                                // 飞塔墙处理
                                if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                                    if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                        logger.info("当前设备的安全策略已生成，不再重复生成");
                                        Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                        Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                        policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                        isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                        continue;
                                    }
                                    logger.info("当前为飞塔设备，不生成安全策略建议命令行，命令行存在于nat");
                                    continue;
                                }

                                // 添加对应的安全策略
                                // 山石墙特殊处理
                                if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                    logger.info("当前设备的安全策略已生成，兼容山石的目的IP特殊情况，删除后重新生成安全策略");
                                    Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                    Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                    policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                    isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                }

                                AddAutoRecommendTaskDTO securityTaskDTO = new AddAutoRecommendTaskDTO();
                                BeanUtils.copyProperties(taskDTO, securityTaskDTO);
                                securityTaskDTO.setNatType(PushNatTypeEnum.NAT_TYPE_N.getCode());
                                // 设置公共字段属性
                                this.setData(securityTaskDTO, vo,nodeEntity,  policyInfoMap, nodeEntity.getUuid(), taskDTOSet);

                            }
                        }
                    }

                    /*if (!currentMatchDNatMapping && PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                        logger.error("当前IP:{}找不到防护网段目的Nat映射关系数据，请检查防护网段配置", dstIp);
                        String errorMsg = "当前IP:"+ dstIp + "找不到防护网段Nat映射关系数据，请检查防护网段配置 [IP:"+ dstIp + " cant find relevant nat maping information please check the protect network table]";
                        dstErrorMsg.append(errorMsg);
                        this.setErrorMsg(errorMsg,vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        continue;
                    }*/
                }
            }

            if(CollectionUtils.isNotEmpty(dstErrorDetailList)){
                errorDetailDTOSet.addAll(dstErrorDetailList);
            }

            // 如果访问类型是互联网访问内网，并且目的IP找不到映射关系数据，则直接提示错误信息
            // 注释原因：H3C集团如果是外访问内，但是没有配置转换关系，只用生成安全策略即可，不提示错误信息
            /*if(PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType()) && !allMatchDNatMapping){
                policyInfoMap.clear();
                return;
            }*/
        }

        if(!srcFindNode && !dstFindNode){
            policyInfoMap.clear();
            logger.error("源、目的IP均未找到防护网段配置，不生成策略建议;");
            return;
        }


        // 2.查找域和接口信息
        if(srcFindNode || dstFindNode){
            this.getInterfaceAndZone(policyInfoMap, resultMap, vo, errorDetailDTOSet);
        }
    }

    /**
     * 设置源Nat数据信息
     * @param natMappingEntity
     * @param currentSrcIp
     * @param vo
     * @param nodeEntity
     * @param policyInfoMap
     * @param deviceUuid
     * @param taskDTOSet
     * @param natType
     */
    private void setSnatData(ProtectNetworkNatMappingEntity natMappingEntity, String currentSrcIp, AutoRecommendTaskVO vo, NodeEntity nodeEntity,
                             Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap, String deviceUuid, Set<AddAutoRecommendTaskDTO> taskDTOSet, String natType){
        AddAutoRecommendTaskDTO taskDTO = new AddAutoRecommendTaskDTO();
        taskDTO.setPreSrcIp(natMappingEntity.getOutsideIp());
        taskDTO.setPostSrcIp(natMappingEntity.getInsideIp());

        // 此处逻辑：如果源Nat转换后IP为空或者为单IP，则以工单输入做转换，否则按映射关系做转换
        if(StringUtils.isBlank(natMappingEntity.getInsideIp()) || IpUtils.isIP(natMappingEntity.getInsideIp())){
            taskDTO.setSrcIp(getRealIP(currentSrcIp));
        } else {
            taskDTO.setSrcIp(StringUtils.isBlank(natMappingEntity.getOutsideIp()) ? getRealIP(currentSrcIp) : natMappingEntity.getOutsideIp());
        }
        taskDTO.setDstIp(vo.getDstIp());
        taskDTO.setSrcFlag(true);
        taskDTO.setNodeEntity(nodeEntity);
        taskDTO.setSrcRangeIp(getRealIP(currentSrcIp));
        taskDTO.setAccessType(vo.getAccessType());
        taskDTO.setServiceList(vo.getServiceList());
        //设置类型为源nat策略
        taskDTO.setNatType(natType);
        taskDTO.setDeviceUuid(deviceUuid);

        if( policyInfoMap.containsKey(deviceUuid) ){
            // 如果之前已经存在相同防护网段设备了，则直接追加源地址即可
            Set<AddAutoRecommendTaskDTO> existTaskDTOList = policyInfoMap.get(deviceUuid);
            existTaskDTOList.add(taskDTO);
            policyInfoMap.put(deviceUuid, existTaskDTOList);
        } else {
            taskDTOSet.add(taskDTO);
            policyInfoMap.put(deviceUuid, taskDTOSet);
        }
    }

    /**
     * 设置公共字段属性值
     * @param taskDTO
     * @param vo
     * @param nodeEntity
     * @param policyInfoMap
     * @param deviceUuid
     * @param taskDTOSet
     */
    private void setData(AddAutoRecommendTaskDTO taskDTO, AutoRecommendTaskVO vo ,NodeEntity nodeEntity, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                         String deviceUuid, Set<AddAutoRecommendTaskDTO> taskDTOSet){
        taskDTO.setSrcIp(vo.getSrcIp());
        taskDTO.setSrcFlag(false);
        taskDTO.setNodeEntity(nodeEntity);
        taskDTO.setServiceList(vo.getServiceList());
        taskDTO.setAccessType(vo.getAccessType());
        taskDTO.setDeviceUuid(deviceUuid);

        if(policyInfoMap.containsKey(deviceUuid)){
            Set<AddAutoRecommendTaskDTO> existTaskDTOSet = policyInfoMap.get(deviceUuid);
            existTaskDTOSet.add(taskDTO);
            policyInfoMap.put(deviceUuid, existTaskDTOSet);
        } else {
            taskDTOSet.add(taskDTO);
            policyInfoMap.put(deviceUuid, taskDTOSet);
        }
    }

    /**
     * 设置安全策略数据
     * @param vo
     * @param nodeEntity
     * @param policyInfoMap
     * @param deviceUuid
     * @param taskDTOSet
     */
    private void setDstSecurityData(AutoRecommendTaskVO vo ,NodeEntity nodeEntity, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                    String deviceUuid, Set<AddAutoRecommendTaskDTO> taskDTOSet, String[] dstIps){
        for (String currentDstIp : dstIps){
            AddAutoRecommendTaskDTO taskDTO = new AddAutoRecommendTaskDTO();
            taskDTO.setDstIp(getRealIP(currentDstIp));
            taskDTO.setDstRangeIp(getRealIP(currentDstIp));
            taskDTO.setNatType(PushNatTypeEnum.NAT_TYPE_N.getCode());
            // 设置公共字段属性
            this.setData(taskDTO, vo,nodeEntity,  policyInfoMap, deviceUuid, taskDTOSet);
        }
    }

    /**
     * 设置源IP的安全策略
     * @param srcIps
     * @param vo
     * @param nodeEntity
     * @param policyInfoMap
     * @param deviceUuid
     * @param taskDTOSet
     */
    private void setSrcSecurityData(String[] srcIps, AutoRecommendTaskVO vo, NodeEntity nodeEntity, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                    String deviceUuid, Set<AddAutoRecommendTaskDTO> taskDTOSet){
        // 源nat没有映射关系，则生成安全策略
        for (String currentSrcIp : srcIps ) {
            AddAutoRecommendTaskDTO taskDTO = new AddAutoRecommendTaskDTO();
            taskDTO.setDstIp(vo.getDstIp());
            taskDTO.setSrcIp(getRealIP(currentSrcIp));
            taskDTO.setSrcRangeIp(getRealIP(currentSrcIp));
            taskDTO.setSrcFlag(true);
            taskDTO.setAccessType(vo.getAccessType());
            taskDTO.setDeviceUuid(nodeEntity.getUuid());
            taskDTO.setNodeEntity(nodeEntity);
            taskDTO.setServiceList(vo.getServiceList());
            //设置类型为安全策略
            taskDTO.setNatType(PushNatTypeEnum.NAT_TYPE_N.getCode());

            if( policyInfoMap.containsKey(deviceUuid) ){
                // 如果之前已经存在相同防护网段设备了，则直接追加源地址即可
                Set<AddAutoRecommendTaskDTO> existTaskDTOList = policyInfoMap.get(deviceUuid);
                existTaskDTOList.add(taskDTO);
                policyInfoMap.put(deviceUuid, existTaskDTOList);
            } else {
                taskDTOSet.add(taskDTO);
                policyInfoMap.put(deviceUuid, taskDTOSet);
            }
        }
    }

    /**
     * 去除/32掩码
     * @param ip
     * @return
     */
    private String getRealIP(String ip){
        if(StringUtils.isNotBlank(ip) && ip.endsWith(IPV4_MASK)){
            ip = ip.substring(0, ip.lastIndexOf("/32"));
        }
        return ip;
    }

    /**
     * 设置异常数据信息
     * @param errorMsg
     * @param srcIp
     * @param dstIp
     * @param serviceList
     * @param startTime
     * @param endTime
     * @param status
     * @param errorDetailDTOSet
     */
    private void setErrorMsg(String errorMsg, String srcIp, String dstIp, List<ServiceDTO> serviceList, Date startTime, Date endTime,
                             Integer status, Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet, List<PolicyDetailVO> ruleList, Integer accessType,
                             String deviceUuid, String srcZone, String dstZone, String postIpAddress){
        AutoRecommendErrorDetailDTO errorDetailDTO = new AutoRecommendErrorDetailDTO();
        errorDetailDTO.setErrorMsg(errorMsg);

        if(StringUtils.isNotBlank(srcIp) && srcIp.endsWith(IPV4_MASK)){
            srcIp = srcIp.substring(0, srcIp.lastIndexOf("/32"));
        }
        if(StringUtils.isNotBlank(dstIp) && dstIp.endsWith(IPV4_MASK)){
            dstIp = dstIp.substring(0, dstIp.lastIndexOf("/32"));
        }

        errorDetailDTO.setSrcIp(StringUtils.equalsIgnoreCase(srcIp,ANY_IP) ? "" : srcIp);
        errorDetailDTO.setDstIp(StringUtils.equalsIgnoreCase(dstIp,ANY_IP) ? "" : dstIp);
        errorDetailDTO.setSrcZone(srcZone);
        errorDetailDTO.setDstZone(dstZone);
        errorDetailDTO.setServiceList(serviceList);
        errorDetailDTO.setStartTime(startTime);
        errorDetailDTO.setEndTime(endTime);
        errorDetailDTO.setStatus(status);
        errorDetailDTO.setRuleList(ruleList);
        errorDetailDTO.setAccessType(accessType);
        errorDetailDTO.setPostIpAddress(postIpAddress);
        if(StringUtils.isNotBlank(deviceUuid)){
            NodeEntity node = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(node != null ){
                errorDetailDTO.setDeviceIp(node.getIp());
                errorDetailDTO.setDeviceName(node.getDeviceName());
            }
        }
        errorDetailDTOSet.add(errorDetailDTO);
    }

    /**
     * 设置源IP错误信息
     * @param errorMsg
     * @param srcIp
     * @param dstIp
     * @param serviceList
     * @param startTime
     * @param endTime
     * @param status
     * @param accessType
     * @return
     */
    private AutoRecommendErrorDetailDTO setSrcErrorMsg(String errorMsg, String srcIp, String dstIp, List<ServiceDTO> serviceList, Date startTime, Date endTime,
                                                       Integer status, Integer accessType){
        AutoRecommendErrorDetailDTO errorDetailDTO = new AutoRecommendErrorDetailDTO();
        errorDetailDTO.setErrorMsg(errorMsg);

        if(StringUtils.isNotBlank(srcIp) && srcIp.endsWith(IPV4_MASK)){
            srcIp = srcIp.substring(0, srcIp.lastIndexOf("/32"));
        }
        if(StringUtils.isNotBlank(dstIp) && dstIp.endsWith(IPV4_MASK)){
            dstIp = dstIp.substring(0, dstIp.lastIndexOf("/32"));
        }

        errorDetailDTO.setSrcIp(StringUtils.equalsIgnoreCase(srcIp,ANY_IP) ? "" : srcIp);
        errorDetailDTO.setDstIp(StringUtils.equalsIgnoreCase(dstIp,ANY_IP) ? "" : dstIp);
        errorDetailDTO.setServiceList(serviceList);
        errorDetailDTO.setStartTime(startTime);
        errorDetailDTO.setEndTime(endTime);
        errorDetailDTO.setStatus(status);
        errorDetailDTO.setAccessType(accessType);
        return errorDetailDTO;
    }

    /**
     * 判断端口是否在协议范围内
     * @param protocol
     * @param port
     * @param serviceList
     * @return
     */
    private boolean isContainsPort(String protocol, String port, List<ServiceDTO> serviceList){
        if(CollectionUtils.isEmpty(serviceList)){
            return true;
        }
        if (StringUtils.isBlank(protocol) && StringUtils.isBlank(port)){
            return true;
        }
        for (ServiceDTO serviceDTO : serviceList ){
            String serviceProtocol = ProtocolUtils.getProtocolByValue(Integer.parseInt(serviceDTO.getProtocol()));
            if (StringUtils.equalsAnyIgnoreCase(serviceProtocol, protocol)){
                String orderDstPort = serviceDTO.getDstPorts();
                if(StringUtils.isBlank(port)){
                    port = "0-65535";
                }
                if(StringUtils.isBlank(orderDstPort)){
                    orderDstPort = port = "0-65535";
                }

                String currentPort = convert2Range(port);
                String[] dstPorts = orderDstPort.split(",");
                for (String dstPort : dstPorts) {
                    String orderPort = convert2Range(dstPort);
                    int orderStartPort = Integer.parseInt(PortUtils.getStartPort(orderPort));
                    int orderEndPort = Integer.parseInt(PortUtils.getEndPort(orderPort));
                    int currentStartPort = Integer.parseInt(PortUtils.getStartPort(currentPort));
                    int currentEndPort = Integer.parseInt(PortUtils.getEndPort(currentPort));
                    if(orderStartPort <= currentStartPort && orderEndPort >= currentEndPort){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 将端口转换为范围
     * @param port
     * @return
     */
    private String convert2Range(String port){
        if(PortUtils.isPortRange(port)){
            return port;
        }
        if(NumberUtils.isCreatable(port)){
            return port + "-" + port;
        }
        return null;
    }

    /**
     * 获取接口和域信息
     * @param policyInfoMap
     * @param resultMap
     * @return
     */
    private void getInterfaceAndZone(Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap, Map<String, Set<AddAutoRecommendTaskDTO>> resultMap,
                                     AutoRecommendTaskVO vo, Set<AutoRecommendErrorDetailDTO> errorDetailDTOList) throws Exception{
        if (ObjectUtils.isEmpty(policyInfoMap)) {
            logger.info("未生成任何初步策略建议");
            return;
        }
        for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> entry : policyInfoMap.entrySet()) {
            Set<AddAutoRecommendTaskDTO> resultTaskSet = new HashSet<>();
            String deviceUuid = entry.getKey();

            boolean isVsys = false;
            String vsysName = "";
            DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
            if( null == device || CollectionUtils.isEmpty(device.getData())){
                String msg = "查询whale未获取到设备相关信息 [No device can be queried based on the IP address]";
                logger.error(msg + ",设备uuid:{}", deviceUuid);
                this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),deviceUuid,null,null,null);
                continue;
            }
            DeviceDataRO deviceData = device.getData().get(0);
            if (deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                vsysName = deviceData.getVsysName();
            }

            //查询设备导入的路由表基本信息
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);

            ResultRO<List<ImportedRoutingTableRO>> resultRO = whaleDevicePolicyClient.getImportRoutTable(deviceUuid);
            if (resultRO == null || resultRO.getData() == null || resultRO.getData().isEmpty()) {
                logger.error("根据设备uuid从Whale中查询导入的路由表基本信息失败,deviceUuid:{}", deviceUuid);
                String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）未查询到默认路由信息 [No default router information can be queried based on the IP address]";
                this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
                continue;
            }
            String defaultOutItfUuid = this.getDefaultOutItf(resultRO.getData());
            if (StringUtils.isEmpty(defaultOutItfUuid)) {
                logger.error("获取到的默认路由表为空！");
                String msg = nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）默认路由表为空 [Default route is not presented in the routing table]";
                this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
                continue;
            }

            Set<AddAutoRecommendTaskDTO> policyInfoSet = entry.getValue();
            if (CollectionUtils.isEmpty(policyInfoSet)) {
                logger.info("策略uuid为：{} 没有对应的策略建议任务", deviceUuid);
                continue;
            }
            for (AddAutoRecommendTaskDTO policyInfo : policyInfoSet) {
                boolean srcFlag = policyInfo.getSrcFlag();
                policyInfo.setVsys(isVsys);
                policyInfo.setVsysName(vsysName);
                this.handleIpAndGetZoneInfo(srcFlag,policyInfo,nodeEntity, defaultOutItfUuid, deviceData, resultTaskSet, vo, errorDetailDTOList);
            }
            if(CollectionUtils.isNotEmpty(resultTaskSet)){
                resultMap.put(deviceUuid,resultTaskSet);
            }
        }
    }

    /**
     * 抽取源和目的的共同操作
     * 处理IP并获取域信息
     * @param srcFlag
     * @param policyInfo
     * @param nodeEntity
     * @param defaultOutItfUuid
     * @param deviceData
     * @param resultTaskSet
     * @param vo
     * @param errorDetailDTOList
     * @throws Exception
     */
    private void handleIpAndGetZoneInfo(boolean srcFlag, AddAutoRecommendTaskDTO policyInfo, NodeEntity nodeEntity, String defaultOutItfUuid, DeviceDataRO deviceData,
                                        Set<AddAutoRecommendTaskDTO> resultTaskSet, AutoRecommendTaskVO vo, Set<AutoRecommendErrorDetailDTO> errorDetailDTOList) throws Exception{
        String rangeIp;
        String policyInfoIp;
        if (srcFlag) {
            rangeIp = StringUtils.isBlank(policyInfo.getSrcRangeIp()) ? policyInfo.getSrcIp() : policyInfo.getSrcRangeIp();
            policyInfoIp = policyInfo.getDstIp();
        } else{
            policyInfoIp = policyInfo.getSrcIp();
            rangeIp = StringUtils.isBlank(policyInfo.getDstRangeIp()) ? policyInfo.getDstIp() : policyInfo.getDstRangeIp();
        }
        if(StringUtils.equalsAnyIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY, policyInfoIp) || StringUtils.isEmpty(policyInfoIp)){
            // 内网互访时Ip为any则接口和域都为any
            if(PushAccessTypeEnum.INSIDE_TO_INSIDE.getCode().equals(policyInfo.getAccessType())){
                policyInfoIp = PolicyConstants.POLICY_STR_VALUE_ANY;
            } else {
                policyInfoIp = ANY_IP;
            }
        }
        if (StringUtils.isNotEmpty(policyInfoIp)) {
            String[] policyInfoIpList = policyInfoIp.split(",");
            for (String policyIp : policyInfoIpList) {
                // 判断源和目的是否相同，相同则同域，跳过不开通
                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.selectByDeviceUuid(nodeEntity.getUuid());
                if(StringUtils.equals(SameZoneFlagFlagEnum.SAME_ZONE_RECOMMEND_N.getCode(), configEntity.getSameZoneFlag())
                        && IpAddress.isSameIp(rangeIp, policyIp, IpTypeEnum.IPV4.getCode())){
                    logger.info(nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）源IP:{}, 目的IP：{} 相同，不生成策略建议", srcFlag == true ? rangeIp : policyIp, srcFlag == true ? policyIp : rangeIp);

                    if(rangeIp.endsWith(IPV4_MASK)){
                        rangeIp = rangeIp.substring(0, rangeIp.lastIndexOf("/32"));
                    }
                    if(policyIp.endsWith(IPV4_MASK)){
                        policyIp = policyIp.substring(0, policyIp.lastIndexOf("/32"));
                    }
                    String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）源IP（"+ (srcFlag == true ? rangeIp : policyIp) +"）和目的IP（"+ (srcFlag == true ? policyIp : rangeIp) +"）相同 [Source ip and destination ip are the same]";
                    this.setErrorMsg(msg, srcFlag == true ? rangeIp : policyIp, srcFlag == true ? policyIp : rangeIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                            AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);

                    continue;
                }
                if(srcFlag){
                    this.setInterfaceAndZone(nodeEntity, defaultOutItfUuid, rangeIp, policyIp, deviceData, policyInfo, resultTaskSet, vo, errorDetailDTOList, configEntity);
                } else {
                    this.setInterfaceAndZone(nodeEntity, defaultOutItfUuid, policyIp, rangeIp, deviceData, policyInfo, resultTaskSet, vo, errorDetailDTOList, configEntity);
                }
            }
        }
    }

    /**
     * 根据设备uuid和默认路由表策略集uuid获取接口和域信息
     *
     * @param nodeEntity
     * @param defaultOutItfUuid
     * @param srcIp
     * @param dstIp
     * @return
     */
    private void setInterfaceAndZone(NodeEntity nodeEntity, String defaultOutItfUuid, String srcIp, String dstIp, DeviceDataRO deviceData,
                                     AddAutoRecommendTaskDTO policyInfo, Set<AddAutoRecommendTaskDTO> resultTaskSet,
                                     AutoRecommendTaskVO vo, Set<AutoRecommendErrorDetailDTO> errorDetailDTOList,ProtectNetworkConfigEntity configEntity) throws Exception{

        // 获取源域、源接口
        Set<String> srcInterfaceNameSet =  new HashSet<>();
        Set<String> dstInterfaceNameSet =  new HashSet<>();
        ReturnT<List<String>> srcReturnT;

        if(StringUtils.equalsAnyIgnoreCase(srcIp, PolicyConstants.POLICY_STR_VALUE_ANY) && StringUtils.isNotBlank(anyIpFindZoneInfo)){
//            srcIp = anyIpFindZoneInfo;
            srcReturnT = this.remoteGetRoutTableRuleListByInside2Inside(nodeEntity.getUuid(), defaultOutItfUuid, anyIpFindZoneInfo);
        } else {
            srcReturnT = this.remoteGetRoutTableRuleList(nodeEntity.getUuid(), defaultOutItfUuid, srcIp);
        }
        if (srcReturnT.getCode() == ReturnT.FAIL_CODE || CollectionUtils.isEmpty(srcReturnT.getData())) {
            logger.error("根据IP:{} 获取源接口失败，失败原因：{}", srcIp, srcReturnT.getMsg());
            String msg = nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）根据IP:" + srcIp + "获取源接口失败 [According to the IP: "+ srcIp+ " failed to obtain the source interface]";
            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
            return;
        }

        srcInterfaceNameSet.addAll(srcReturnT.getData());

        // 判断源IP对应的接口是否在设备接口列表中（针对飞塔设备）
        srcInterfaceNameSet = this.checkInterface(nodeEntity,srcInterfaceNameSet, srcIp, dstIp, vo, errorDetailDTOList);

        ReturnT<List<String>> dstReturnT;
        if(StringUtils.equalsAnyIgnoreCase(dstIp, PolicyConstants.POLICY_STR_VALUE_ANY) && StringUtils.isNotBlank(anyIpFindZoneInfo)){
//            dstIp = anyIpFindZoneInfo;
            dstReturnT = this.remoteGetRoutTableRuleListByInside2Inside(nodeEntity.getUuid(), defaultOutItfUuid, anyIpFindZoneInfo);
        } else {
            // 1.互联网访问内网时，是根据内网IP去查询接口和域
            // 2.山石的墙，互联网访问内网时，安全策略建议也是根据内网IP去查询接口和域
            if((PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(policyInfo.getAccessType()) &&
                    StringUtils.equalsAnyIgnoreCase(PushNatTypeEnum.NAT_TYPE_D.getCode(), policyInfo.getNatType())) ||
                    (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY) && PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(policyInfo.getAccessType()) &&
                            StringUtils.equalsAnyIgnoreCase(PushNatTypeEnum.NAT_TYPE_N.getCode(), policyInfo.getNatType()))){
                dstReturnT = this.remoteGetRoutTableRuleList(nodeEntity.getUuid(), defaultOutItfUuid, StringUtils.isNotEmpty(policyInfo.getPostDstIp()) ? policyInfo.getPostDstIp() : policyInfo.getDstRangeIp());
            } else{
                dstReturnT = this.remoteGetRoutTableRuleList(nodeEntity.getUuid(), defaultOutItfUuid, dstIp);
            }
        }
        if (dstReturnT.getCode() == ReturnT.FAIL_CODE || CollectionUtils.isEmpty(dstReturnT.getData())) {
            String msg = nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）根据IP:" + dstIp + "获取目的接口失败 [According to the IP: "+ dstIp +" failed to obtain the source interface]";
            logger.error(msg);
            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
            return;
        }
        dstInterfaceNameSet.addAll(dstReturnT.getData());

        // 判断目的IP对应的接口是否在设备接口列表中（针对飞塔设备）
        dstInterfaceNameSet = this.checkInterface(nodeEntity,dstInterfaceNameSet, srcIp, dstIp, vo, errorDetailDTOList);

        // 查源域和目的域
        for (String srcDevIf : srcInterfaceNameSet){
            for (String dstDevIf : dstInterfaceNameSet){
                AddAutoRecommendTaskDTO addAutoRecommendTaskDTO = new AddAutoRecommendTaskDTO();
                BeanUtils.copyProperties(policyInfo, addAutoRecommendTaskDTO);
                addAutoRecommendTaskDTO.setSrcIp(srcIp);
                addAutoRecommendTaskDTO.setDstIp(dstIp);
                if(StringUtils.equalsAnyIgnoreCase(srcIp,ANY_IP) || StringUtils.equalsAnyIgnoreCase(srcIp,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    addAutoRecommendTaskDTO.setSrcIp("");
                }
                if(StringUtils.equalsAnyIgnoreCase(dstIp,ANY_IP) || StringUtils.equalsAnyIgnoreCase(dstIp,PolicyConstants.POLICY_STR_VALUE_ANY)){
                    addAutoRecommendTaskDTO.setDstIp("");
                }

                // 思科需要查接口别名
                String devIfAlias = "";
                if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "CISCO")) {
                    List<DeviceInterfaceRO> deviceInterfaces = deviceData.getDeviceInterfaces();
                    logger.info("思科设备接口数据为：{}" + JSONObject.toJSONString(deviceInterfaces));
                    for (DeviceInterfaceRO deviceInterfaceRO : deviceInterfaces ){
                        if(StringUtils.equals(deviceInterfaceRO.getName(), srcDevIf)){
                            devIfAlias = deviceInterfaceRO.getAlias();
                            break;
                        }
                    }
                    // 思科特殊处理
                    addAutoRecommendTaskDTO.setInDevIf(srcDevIf);
                    addAutoRecommendTaskDTO.setSrcZone(srcDevIf);
                    addAutoRecommendTaskDTO.setInDevItfAlias(devIfAlias);
                    addAutoRecommendTaskDTO.setDstZone(dstDevIf);
                    addAutoRecommendTaskDTO.setOutDevIf(dstDevIf);
                    addAutoRecommendTaskDTO.setOutDevItfAlias(devIfAlias);
                } else {
                    String srcZoneName;
                    String dstZoneName;
                    srcZoneName = whalePolicyClient.getZoneInfoByDeviceUuidAndInterfaceName(nodeEntity.getUuid(), srcDevIf);
                    // 如果是飞塔或者思科设备，没查到域则将接口作为域

                    if (srcZoneName == null || StringUtils.isEmpty(srcZoneName)) {
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY) || StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "CISCO")){
                            srcZoneName = srcDevIf;
                        }else {
                            String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）获取源域失败 [Get source zone failed]";
                            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,null ,null);
                            continue;
                        }

                    }

                    dstZoneName = whalePolicyClient.getZoneInfoByDeviceUuidAndInterfaceName(nodeEntity.getUuid(), dstDevIf);
                    // 如果是飞塔或者思科设备，没查到域则将接口作为域
                    if (dstZoneName == null || StringUtils.isEmpty(dstZoneName)) {
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY) || StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "CISCO")){
                            dstZoneName = dstDevIf;
                        } else {
                            String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）获取目的域失败 [Get destination zone failed]";
                            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName ,null);
                            continue;
                        }
                    }

                    // 如果源接口或者目的接口为any，则界面上不显示
                    if(StringUtils.equalsAnyIgnoreCase(srcDevIf, PolicyConstants.POLICY_STR_VALUE_ANY)){
                        addAutoRecommendTaskDTO.setInDevIf(null);
                    } else {
                        addAutoRecommendTaskDTO.setInDevIf(srcDevIf);
                    }

                    if(StringUtils.equalsAnyIgnoreCase(srcDevIf, PolicyConstants.POLICY_STR_VALUE_ANY)){
                        addAutoRecommendTaskDTO.setOutDevIf(null);
                    } else {
                        addAutoRecommendTaskDTO.setOutDevIf(dstDevIf);
                    }

                    // 当访问类型为互联网访问内网并且设备为山石设备时，不检查源域和目的域是否相同
                    /*
                    if(StringUtils.equals(srcZoneName, dstZoneName)){
                        logger.info(nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）源域：{}和目的域:{} 相同，不生成自动开通建议", srcZoneName, dstZoneName);
                        String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）源域（"+ srcZoneName +"）和目的域（"+ dstZoneName +"）相同 [Source zone and destination zone are the same]";
                        this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName,null);
                        continue;
                    }*/
                    // 如果域相同，并且开启同域开通
                    if(StringUtils.equals(SameZoneFlagFlagEnum.SAME_ZONE_RECOMMEND_N.getCode(), configEntity.getSameZoneFlag())){
                        if(StringUtils.equals(srcZoneName, dstZoneName)){
                            String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）源域（"+ srcZoneName +"）和目的域（"+ dstZoneName +"）相同 [Source zone and destination zone are the same]";
                            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName,null);
                            continue;
                        }
                        // 判断同域开通是否开启
                    }

                    // 开启同域开通时检查高级设置
                    // 判断高级设置配置
                    boolean generatePolicy = true;
                    List<PushZoneLimitConfigEntity> byDeviceUuid = pushZoneLimitConfigMapper.findByDeviceUuid(nodeEntity.getUuid());
                    if(CollectionUtils.isNotEmpty(byDeviceUuid)){
                        for(PushZoneLimitConfigEntity zoneLimitConfigEntity : byDeviceUuid ){
                            if(StringUtils.equals(srcZoneName, zoneLimitConfigEntity.getSrcZone()) && StringUtils.equals(dstZoneName, zoneLimitConfigEntity.getDstZone())){
                                logger.info("当前设备：{}，源域：{}，目的域：{}已开通同域开通，但配置了高级设置，不开通当前源域和目的域的策略建议",nodeEntity.getIp(), srcZoneName, dstZoneName);
                                String msg = nodeEntity.getDeviceName() +"（" + nodeEntity.getIp() + "）已配置源域（"+ srcZoneName +"）到目的域（"+ dstZoneName +"）不开通策略建议 [Source zone to destination zone is not allowed]";
                                this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName,null);
                                generatePolicy = false;
                                break;
                            }
                        }
                    }
                    // 已开通同域开通，但配置了高级设置，不开通当前源域和目的域的策略建议
                    if(!generatePolicy){
                        continue;
                    }

                    if (StringUtils.isNotBlank(srcZoneName)) {
                        addAutoRecommendTaskDTO.setSrcZone(srcZoneName);
                    }
                    if (StringUtils.isNotBlank(dstZoneName)) {
                        addAutoRecommendTaskDTO.setDstZone(dstZoneName);
                    }
                }
                resultTaskSet.add(addAutoRecommendTaskDTO);
            }

        }
    }

    /**
     * 针对飞塔设备，检查接口
     *
     * 检查逻辑：判断是否存在接口列表中：存在则跳过，不存在则去掉最后一个"_"及后面的数字，再次判断去除之后的名字是否在接口列表中
     *           存在，则继续，不存在，则提示错误信息。
     * @param nodeEntity
     * @param interfaceNameSet
     * @param srcIp
     * @param dstIp
     * @param vo
     * @param errorDetailDTOList
     */
    private Set<String> checkInterface(NodeEntity nodeEntity, Set<String> interfaceNameSet, String srcIp, String dstIp, AutoRecommendTaskVO vo,
                                       Set<AutoRecommendErrorDetailDTO> errorDetailDTOList){
        // 有问题的接口名称
        Set<String> errorInterfaceNameSet = new HashSet<>();
        // 截取后正确的接口名称
        Set<String> rightInterfaceNameSet = new HashSet<>();
        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(nodeEntity.getUuid());
            if( null != deviceRO && CollectionUtils.isNotEmpty(deviceRO.getData())){
                for (DeviceDataRO deviceDataRO : deviceRO.getData()){
                    List<DeviceInterfaceRO> deviceInterfaces = deviceDataRO.getDeviceInterfaces();
                    if(CollectionUtils.isNotEmpty(deviceInterfaces)){
                        List<String> deviceInterfaceList = deviceInterfaces.stream().map(DeviceInterfaceRO :: getName).collect(Collectors.toList());
                        for (String interfaceName : interfaceNameSet) {
                            if(!containsInterfaceName(deviceInterfaceList, interfaceName)){
                                if(StringUtils.contains(interfaceName, INTERFACE_NAME_SPLIT_BY)){
                                    String subInterfaceName = interfaceName.substring(0, interfaceName.lastIndexOf(INTERFACE_NAME_SPLIT_BY));
                                    if(!containsInterfaceName(deviceInterfaceList, subInterfaceName)){
                                        errorInterfaceNameSet.add(interfaceName);
                                        logger.error("截取前接口名称：{}, 截取后接口名称：{} 不存在于设备接口列表中", interfaceName, subInterfaceName);
                                        String msg = nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）未查询到接口："+ subInterfaceName +" [Can't find relevant interface in firewall]";
                                        this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                                AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
                                        continue;
                                    } else{
                                        errorInterfaceNameSet.add(interfaceName);
                                        rightInterfaceNameSet.add(subInterfaceName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 去除有问题的接口名称
        if(CollectionUtils.isNotEmpty(errorInterfaceNameSet)){
            interfaceNameSet.removeAll(errorInterfaceNameSet);
        }
        // 添加截取后正确的接口名称
        if(CollectionUtils.isNotEmpty(rightInterfaceNameSet)){
            interfaceNameSet.addAll(rightInterfaceNameSet);
        }
        return interfaceNameSet;
    }

    /**
     * 检查接口名是否在设备接口列表中
     * @param deviceInterfaceList
     * @param interfaceName
     * @return
     */
    private boolean containsInterfaceName(List<String> deviceInterfaceList, String interfaceName){
        if(deviceInterfaceList.contains(interfaceName)){
            return true;
        }
        return false;
    }

    /**
     * 添加自动开通任务
     *
     * @param taskDTO
     * @param samePartDTO
     * @return
     */
    public int addAutoRecommendTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO, Integer taskId, PolicyEnum type, String vipName,
                                    AutoRecommendSpecialDTO autoRecommendSpecialDTO) {
        //添加任务
        CommandTaskEditableEntity entity = EntityUtils.createCommandTask(samePartDTO.getTaskType(),
                taskId, samePartDTO.getUserName(), samePartDTO.getTheme(), taskDTO.getDeviceUuid());
        entity.setBranchLevel("00");

        String postPorts = taskDTO.getPostPorts();

        NodeEntity nodeEntity = taskDTO.getNode();
        if(ObjectUtils.isNotEmpty(nodeEntity)){
            if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                postPorts = taskDTO.getPrePorts();
            }
        }
        String dstIp = taskDTO.getDstIp();
        if(PolicyEnum.DNAT.equals(type)){
            dstIp = taskDTO.getPreDstIp();
        }

        String srcIp = taskDTO.getSrcIp();
        // 如果转换是源nat，并且转换后的源IP为空，则使用出接口
        String postSrcIp = taskDTO.getPostSrcIp();
        List<ServiceDTO> serviceList = taskDTO.getServiceList();
        if(PolicyEnum.SNAT.equals(type)){
            if(StringUtils.isBlank(postSrcIp)){
                if (ObjectUtils.isNotEmpty(nodeEntity) && StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                    postSrcIp = "";
                } else {
                    postSrcIp = taskDTO.getOutDevIf();
                }
            }
            if (ObjectUtils.isNotEmpty(nodeEntity) && StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                dstIp = "";
                serviceList = new ArrayList<>();
            }
        }
        List<ServiceDTO> postServiceList = this.createPostServiceList(taskDTO);
        commandTaskManager.addCommandEditableEntityTask(entity);
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(samePartDTO.getType(), entity.getId(), entity.getTaskId(), taskDTO.getDeviceUuid(), samePartDTO.getTheme(), samePartDTO.getUserName(),
                srcIp, dstIp, postSrcIp, taskDTO.getPostDstIp() == null ? "" : taskDTO.getPostDstIp(), serviceList,
                postServiceList, taskDTO.getSrcZone(), taskDTO.getDstZone(), taskDTO.getInDevIf(), taskDTO.getOutDevIf(), taskDTO.getInDevItfAlias(), taskDTO.getOutDevItfAlias(),
                samePartDTO.getStartTimeString(), samePartDTO.getEndTimeString(), samePartDTO.getDescription(), samePartDTO.getAction(), taskDTO.isVsys(), taskDTO.getVsysName(), samePartDTO.getMoveSeat(), null, null, null,
                null, null, samePartDTO.getIpType(), null, null, null, null, null,postPorts, vipName);
        TaskDTO task = cmdDTO.getTask();
        task.setMergeCheck(samePartDTO.getMergeCheck());
        task.setRangeFilter(samePartDTO.getRangeFilter());
        task.setBeforeConflict(samePartDTO.getBeforeConflict());
        cmdDTO.getPolicy().setPolicyUserNames(samePartDTO.getPolicyUserNames());
        cmdDTO.getPolicy().setPrePort(taskDTO.getPrePorts());
        cmdDTO.getPolicy().setPolicyApplications(samePartDTO.getPolicyApplications());
        cmdDTO.getPolicy().setFortinetDnatSpecialDTO(taskDTO.getFortinetDnatSpecialDTO());
        // 设置自动开通适配地址对象管理命令行特殊参数
        cmdDTO.setAutoRecommendSpecialDTO(autoRecommendSpecialDTO);
        cmdTaskService.getRuleMatchFlow2Generate(cmdDTO, samePartDTO.getUserInfoDTO());
        return entity.getId();
    }

    /**
     * 构建新建防护网段DTO数据
     *
     * @param vo
     * @return
     */
    private AutoRecommendTaskEntity buildAutoRecommendTask(AutoRecommendTaskVO vo, AutoRecommendTaskEntity record,Set<AutoRecommendErrorDetailDTO> errorDetailDTOList) {
        BeanUtils.copyProperties(vo, record);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber = "A" + simpleDateFormat.format(date);
        record.setOrderNumber(orderNumber);
        record.setUuid(IdGen.uuid());
        record.setStatus(AutoRecommendStatusEnum.GENERATING_COMMANDLINE.getCode());
        record.setTaskType(vo.getAccessType());
        record.setBranchLevel("00");
        record.setIpType(vo.getIpType());

        if(vo.getEndTime() != null) {
            record.setEndTime(new Date(vo.getEndTime()));
        }
        if(vo.getStartTime() != null) {
            record.setStartTime(new Date(vo.getStartTime()));
        }

        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(vo.getServiceList())) {
            logger.info("新建策略服务为空");
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTOList.add(serviceDTO);
            record.setServiceList(JSONObject.toJSONString(serviceDTOList));
        } else {
            logger.info("新建策略服务不为空:{}", JSONObject.toJSONString(vo.getServiceList()));
            serviceDTOList = vo.getServiceList();
            for (ServiceDTO service : serviceDTOList) {
                if (!AliStringUtils.isEmpty(service.getDstPorts())) {
                    service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                }
            }
            record.setServiceList(JSONObject.toJSONString(vo.getServiceList()));
        }
        record.setCreateTime(date);

        PushAdditionalInfoEntity additionalInfoEntity = new PushAdditionalInfoEntity();
        record.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        record.setErrorMsg(JSONObject.toJSONString(errorDetailDTOList));
        return record;
    }


    /**
     * 获取路由表的默认策略集名称
     *
     * @param routingTableROList
     * @return
     */
    private String getDefaultOutItf(List<ImportedRoutingTableRO> routingTableROList) {
        for (ImportedRoutingTableRO routingTableRO : routingTableROList) {
            List<RoutingtableRO> baseRoutingTables = routingTableRO.getBaseRoutingTables();
            if (CollectionUtils.isEmpty(baseRoutingTables)) {
                logger.error("导入的路由表基本信息列表为空！");
                continue;
            }
            for (RoutingtableRO baseRoutingTable : baseRoutingTables) {
                if (ObjectUtils.isEmpty(baseRoutingTable.getIsDefault())) {
                    logger.error("未查询到默认策略集名称！");
                    continue;
                }
                if (baseRoutingTable.getIsDefault()) {
                    logger.info("查询到默认策略集名称:{}", baseRoutingTable.getName());
                    return baseRoutingTable.getUuid();
                }
            }
        }
        return null;
    }

    /**
     * 格式化域和接口信息
     *
     * @param zone
     * @param itf
     * @return
     */
    String formatZoneItfString(String zone, String itf) {
        if (AliStringUtils.isEmpty(zone)) {
            return AliStringUtils.isEmpty(itf) ? "" : itf;
        } else {
            return AliStringUtils.isEmpty(itf) ? zone : zone + ", " + itf;
        }
    }

    /**
     * 将自动开通任务DTO转换为PolicyDTO，调用原来的合并逻辑
     *
     * @param taskDTOMap
     * @return
     */
    private Map<String, List<RecommendPolicyDTO>> converntTaskDTO2PolicyDTO(Map<String, Set<AddAutoRecommendTaskDTO>> taskDTOMap) {
        Map<String, List<RecommendPolicyDTO>> policyDTOMap = new HashMap<>();
        for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : taskDTOMap.entrySet()) {
            String deviceUuid = taskMap.getKey();
            Set<AddAutoRecommendTaskDTO> taskDTOList = taskMap.getValue();
            List<RecommendPolicyDTO> policyDTOList = new ArrayList<>();
            for (AddAutoRecommendTaskDTO taskDTO : taskDTOList) {
                RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
                BeanUtils.copyProperties(taskDTO, policyDTO);
                policyDTO.setDeviceUuid(deviceUuid);
                policyDTO.setNode(taskDTO.getNodeEntity());
                policyDTOList.add(policyDTO);
            }
            policyDTOMap.put(deviceUuid, policyDTOList);
        }
        return policyDTOMap;
    }


    /**
     * 检查nat策略数据流
     * @param policyDTO
     * @param deviceUuid
     * @param ipType
     * @param errorDetailDTOSet
     * @param accessType
     * @param natType 1:源nat，2：目的nat
     */
    private RecommendPolicyDTO checkNatDataFlow(RecommendPolicyDTO policyDTO, String deviceUuid, Integer ipType, Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet,
                                                Integer accessType, Integer natType, Map<String, AutoRecommendConflictPolicyDTO> conflictPolicyMap,
                                                AutoRecommendTaskVO vo, AutoRecommendTaskEntity record, List<RecommendPolicyDTO> existVipName,JSONArray taskJsonArray,
                                                List<MergeFortinetPolicyDTO> fortinetPolicyDTOList) throws UnsupportedEncodingException{

        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
        PolicyDTO policy = new PolicyDTO();
        DeviceDTO device = new DeviceDTO();
        device.setDeviceUuid(deviceUuid);
        BeanUtils.copyProperties(policyDTO, policy);
        policy.setIpType(ipType);
        policy.setSrcItf(policyDTO.getInDevIf());
        policy.setDstItf(policyDTO.getOutDevIf());
        if(natType == 2){
            policy.setDstIp(policyDTO.getPreDstIp());
        }
        logger.info("根据nat策略建议：{} 和设备UUID：{}查询nat策略数据流", JSON.toJSONString(policy), device.getDeviceUuid());
        List<NatRuleMatchFlowVO> natRuleMatchFlowVOList = ngRemoteService.getNatRuleMatchFlow(policy, device, natType);
        logger.info("查询数据流响应数据:{}", JSON.toJSONString(natRuleMatchFlowVOList));
        List<PolicyDetailVO> ruleList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(natRuleMatchFlowVOList)) {
            // 添加Nat的冲突策略
            for (NatRuleMatchFlowVO flowVO : natRuleMatchFlowVOList ){
                if(null == flowVO || null == flowVO.getNatRule()){
                    logger.info("当前匹配Nat策略为空，跳过");
                    continue;
                }
                PolicyDetailVO natRule = flowVO.getNatRule();
                // 添加到自动开通冲突策略表
                List<PolicyDetailVO> natRuleList = new ArrayList();
                natRuleList.add(natRule);
                this.batchInsert(natRuleList,record.getId(), record.getTheme(), "1");
                ruleList.add(natRule);
                if (conflictPolicyMap.containsKey(deviceUuid)){
                    AutoRecommendConflictPolicyDTO autoRecommendConflictPolicyDTO = conflictPolicyMap.get(deviceUuid);
                    Set<PolicyDetailVO> natConflictPolicyDTOSet = autoRecommendConflictPolicyDTO.getNatConflictPolicyDTOSet();
                    if(CollectionUtils.isEmpty(natConflictPolicyDTOSet)){
                        natConflictPolicyDTOSet = new HashSet<>();
                    }
                    natConflictPolicyDTOSet.add(flowVO.getNatRule());
                } else {
                    AutoRecommendConflictPolicyDTO autoRecommendConflictPolicyDTO = new AutoRecommendConflictPolicyDTO();
                    Set<PolicyDetailVO> natConflictPolicyDTOSet = new HashSet<>();
                    natConflictPolicyDTOSet.add(flowVO.getNatRule());
                    autoRecommendConflictPolicyDTO.setNatConflictPolicyDTOSet(natConflictPolicyDTOSet);
                    conflictPolicyMap.put(deviceUuid, autoRecommendConflictPolicyDTO);
                }
            }

            // 如果nat策略建议返回数据流中的最后一条的剩余数据流（未匹配）数据为空，则该nat策略建议已存在，不开通
            NatRuleMatchFlowVO natRuleMatchFlowVO = natRuleMatchFlowVOList.get(natRuleMatchFlowVOList.size() - 1);
            if(ObjectUtils.isEmpty(natRuleMatchFlowVO) || CollectionUtils.isEmpty(natRuleMatchFlowVO.getRestFlow())){
                String postIpAddress = natType == 1 ? policyDTO.getPostSrcIp() : policyDTO.getPostDstIp();
                logger.error("Nat：查询到最后一条数据的剩余数据流（未匹配）为空，Nat策略：{} 已开通，不生成自动开通策略建议", JSON.toJSONString(policyDTO));
                this.setErrorMsg(nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）Nat策略已开通 [Nat policy already exists]", policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getServiceList(), policyDTO.getStartTime(), policyDTO.getEndTime(),
                        AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode(),errorDetailDTOSet,ruleList, accessType,nodeEntity.getUuid(),policyDTO.getSrcZone(),policyDTO.getDstZone(), postIpAddress);
                return null;
            }
        }

        if(natType == 2 && StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
            MergeFortinetPolicyDTO fortinetPolicyDTO = new MergeFortinetPolicyDTO();
            fortinetPolicyDTO.setNodeEntity(nodeEntity);
            fortinetPolicyDTO.setPolicyDTO(policyDTO);
            fortinetPolicyDTO.setRecord(record);
            fortinetPolicyDTO.setTaskJsonArray(taskJsonArray);
            fortinetPolicyDTO.setVo(vo);
            fortinetPolicyDTOList.add(fortinetPolicyDTO);
        }
        /*if(natType == 2 && StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
            // 针对飞塔的目的Nat:如果restFlow里面数据不为空，则需要根据转换前目的IP，转换后目的IP去查询一下策略列表，看VIP映射关系时否存在，存在则复用，并生成一条安全策略建议，不存在但不为空，则提示错误信息，为空则走正常流程
            String ipTerm = this.buildIpTerm(policyDTO.getPreDstIp());
            JSONArray jsonArray = remotePolicyService.remotePolicyDetailByIpTerms(ipTerm, null, deviceUuid, SYSTEM__NAT_LIST.getCode());
            if (jsonArray != null && jsonArray.size() > 0) {
                List<PolicyDetailVO> policyDetailVOS = jsonArray.toJavaList(PolicyDetailVO.class);
                // 判断转换后关系是否存在
                boolean existPostDstIp = false;
                String vipName = null;
                for (PolicyDetailVO policyDetailVO : policyDetailVOS ){
                    if(StringUtils.equals(policyDetailVO.getPostDstIp(), policyDTO.getPostDstIp())){
                        existPostDstIp = true;
                        vipName = policyDetailVO.getPolicyName();

                        ruleList.add(policyDetailVO);
                        break;
                    }
                }
                // 如果存在转换后的目的IP并且VIP名称不为空，则复用VIP名称，创建一个目的地址为VIP的安全策略，否则给出错误信息提示
                if(existPostDstIp && StringUtils.isNotBlank(vipName)){
                    existVipName.add(policyDTO);
                    AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);
                    samePartDTO.setType(PolicyEnum.SECURITY);
                    samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                    // 设置目的地址为VIP名称
                    policyDTO.setDstIp(vipName);
                    policyDTO.setDeviceUuid(deviceUuid);
                    RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                    // 关联安全策略
                    this.buildRelevancyNat(securityEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,taskJsonArray);
                    this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY, vipName, null);

                    return null;
                } else {
                    this.setErrorMsg(nodeEntity.getDeviceName() + "（" + nodeEntity.getIp() + "）Nat映射关系已存在 [Nat mapping already exists]", policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getServiceList(), policyDTO.getStartTime(), policyDTO.getEndTime(),
                            AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode(),errorDetailDTOSet,ruleList, accessType,nodeEntity.getUuid(),policyDTO.getSrcZone(),policyDTO.getDstZone(), policyDTO.getPostDstIp());
                    return null;
                }

            }
        }*/

        return policyDTO;

    }

    /**
     * 抽离公共代码块，构建公共参数
     * @param vo
     * @param record
     * @return
     */
    private AutoRecommendTaskSamePartDTO  buildSamePartDTO(AutoRecommendTaskVO vo, AutoRecommendTaskEntity record){
        AutoRecommendTaskSamePartDTO samePartDTO = new AutoRecommendTaskSamePartDTO();
        String userName = vo.getUserName();
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        samePartDTO.setUserInfoDTO(userInfoDTO);
        samePartDTO.setUserName(userName);
        samePartDTO.setTheme(vo.getTheme());
        samePartDTO.setDescription(record.getDescription());

        ActionEnum action = ActionEnum.PERMIT;
        samePartDTO.setAction(action);
        MoveSeatEnum moveSeat = MoveSeatEnum.FIRST;
        samePartDTO.setMoveSeat(moveSeat);

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        String endTimeString = record.getEndTime() == null ? null : sdf.format(record.getEndTime());
        String startTimeString = record.getStartTime() == null ? null : sdf.format(record.getStartTime());
        samePartDTO.setStartTimeString(startTimeString);
        samePartDTO.setEndTimeString(endTimeString);
        samePartDTO.setIpType(vo.getIpType());
        return samePartDTO;
    }

    /**
     * 构建远程调用青提的参数
     * @param dstIp
     * @return
     * @throws UnsupportedEncodingException
     */
    private String buildIpTerm(String dstIp) throws UnsupportedEncodingException {
        IpTermsExtendDTO ipTerms = new IpTermsExtendDTO();
        if (StringUtils.isNotEmpty(dstIp) && !CommonConstants.ANY.equalsIgnoreCase(dstIp)) {
            List<CommonRangeStringDTO> ip4DstAddresses = checkConversionParam(dstIp);
            ipTerms.setIp4DstAddresses(ip4DstAddresses);
            ipTerms.setDstAddressOp(SearchRangeOpEnum.EQUAL);
        }

        ipTerms.setSkipAny(false);
        ipTerms.setPolicyType(SYSTEM__NAT_LIST.getCode());
        JsonQueryDTO jsonQuery = new JsonQueryDTO();
        String whalePolicyType = "SYSTEM__NAT_LIST";
        Map<String, String[]> filterListType = new HashMap<>();
        filterListType.put("$in", whalePolicyType.split(","));
        jsonQuery.setFilterListType(filterListType);
        ipTerms.setJsonQuery(jsonQuery);

        String ipTerm = JSONObject.toJSONString(ipTerms);
        ipTerm = URLEncoder.encode(ipTerm, Charset.defaultCharset().name());
        return ipTerm;
    }

    private List<CommonRangeStringDTO> checkConversionParam(String ip) {
        String[] ip4Addresses = ip.split(",");
        List<CommonRangeStringDTO> ip4AddressList = new LinkedList<>();
        for (String ip4 : ip4Addresses) {

            CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();

            if (IpUtils.isIPRange(ip4)) {
                String[] ipSegment = ip4.trim().split("-");
                commonRangeStringDTO.setStart(ipSegment[0]);
                commonRangeStringDTO.setEnd(ipSegment[1]);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else if (IpUtils.isIPSegment(ip4)) {
                String startIp = IpUtils.getStartIp(ip4);
                String endIp = IpUtils.getEndIp(ip4);
                commonRangeStringDTO.setStart(startIp);
                commonRangeStringDTO.setEnd(endIp);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else if (IpUtils.isIP(ip4)) {
                commonRangeStringDTO.setStart(ip4);
                commonRangeStringDTO.setEnd(ip4);
                ip4AddressList.add(commonRangeStringDTO);
                continue;
            } else {
                logger.error("不支持这种类型{}", ip4);
                throw new IllegalArgumentException("不支持其它类型的ip" + ip4 + "输入");
            }
        }
        return ip4AddressList;
    }

    /**
     * 判断相关策略中的转换后源IP是否与当前相同
     * 判断逻辑：如果转换后IP为IP，则直接判断，不为IP，则与默认出接口比较
     * @param postSrcIp
     * @param natRule
     * @param ipEndFlow
     * @return
     */
    private boolean checkPostSrcIp(String postSrcIp, PolicyDetailVO natRule, List<String> ipEndFlow){
        if(IpUtils.isIP(postSrcIp) || IpUtils.isIPSegment(postSrcIp) || IpUtils.isIPRange(postSrcIp)){
            return ipEndFlow.contains(postSrcIp);
        } else {
            return StringUtils.equals(postSrcIp, natRule.getPostSrcIp());
        }
    }

    /**
     * 获取转换后的ip
     * @param endFlow
     * @return
     */
    private List<String> getIpEndFlow(List<PathFlowRO> endFlow, Integer natType){
        List<String> ipEndFlow =  new ArrayList<>();
        if(CollectionUtils.isNotEmpty(endFlow)){
            for (PathFlowRO pathFlowRO : endFlow) {
                if(natType == 2 ){
                    List<SrcDstStringDTO> ip4DstAddresses = pathFlowRO.getIp4DstAddresses();
                    for(SrcDstStringDTO ipString : ip4DstAddresses){
                        if(StringUtils.equals(ipString.getStart(), ipString.getEnd())){
                            ipEndFlow.add(ipString.getStart());
                        } else {
                            ipEndFlow.add(ipString.getStart() + "-" +ipString.getEnd());
                        }
                    }
                } else if (natType == 1 ){
                    List<SrcDstStringDTO> ip4SrcAddresses = pathFlowRO.getIp4SrcAddresses();
                    for(SrcDstStringDTO ipString : ip4SrcAddresses){
                        if(StringUtils.equals(ipString.getStart(), ipString.getEnd())){
                            ipEndFlow.add(ipString.getStart());
                        } else {
                            ipEndFlow.add(ipString.getStart() + "-" +ipString.getEnd());
                        }
                    }
                }
            }
        }
        return ipEndFlow;
    }

    /**
     * 判断源和目的是否都未找到配置，返回具体错误信息的的源和目的
     * @param errorMsgMap
     * @param srcIp
     * @param dstIp
     * @return
     */
    private String[] srcAndDstAllError(Map<String, String> errorMsgMap, String srcIp, String dstIp){
        if(ObjectUtils.isEmpty(errorMsgMap)){
            return null;
        }
        for (Map.Entry<String, String> entry : errorMsgMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(srcIp.contains(key) && value.contains(dstIp)){
                return new String[]{key, dstIp};
            }
        }
        return null;
    }

    /**
     * 笛卡尔积打开策略建议的源和目的
     * @param resultMap
     */
    private Map<String, Set<AddAutoRecommendTaskDTO>> descartesTask(Map<String, Set<AddAutoRecommendTaskDTO>> resultMap){
        Map<String, Set<AddAutoRecommendTaskDTO>> descartesMap = new HashMap<>();
        for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
            String deviceUuid = taskMap.getKey();
            Set<AddAutoRecommendTaskDTO> taskSet = taskMap.getValue();

            Set<AddAutoRecommendTaskDTO> descartesSet =  new HashSet<>();
            for(AddAutoRecommendTaskDTO taskDTO : taskSet){
                // 如果源为空
                if(StringUtils.isBlank(taskDTO.getSrcIp())){
                    if(StringUtils.isBlank(taskDTO.getDstIp())){
                        AddAutoRecommendTaskDTO descartesDTO = new AddAutoRecommendTaskDTO();
                        BeanUtils.copyProperties(taskDTO, descartesDTO);
                        descartesSet.add(descartesDTO);
                    } else {
                        // 源为空，目的不为空
                        String[] dstIps = taskDTO.getDstIp().split(",");
                        for (String dstIp : dstIps){
                            AddAutoRecommendTaskDTO descartesDTO = new AddAutoRecommendTaskDTO();
                            BeanUtils.copyProperties(taskDTO, descartesDTO);
                            descartesDTO.setDstIp(dstIp);
                            descartesSet.add(descartesDTO);
                        }
                    }
                } else {
                    String[] srcIps = taskDTO.getSrcIp().split(",");
                    for (String srcIp : srcIps){
                        // 源不为空，目的为空
                        if(StringUtils.isBlank(taskDTO.getDstIp())){
                            AddAutoRecommendTaskDTO descartesDTO = new AddAutoRecommendTaskDTO();
                            BeanUtils.copyProperties(taskDTO, descartesDTO);
                            descartesDTO.setSrcIp(srcIp);
                            descartesSet.add(descartesDTO);
                        } else {
                            // 源、目的均不为空
                            String[] dstIps = taskDTO.getDstIp().split(",");
                            for (String dstIp : dstIps){
                                AddAutoRecommendTaskDTO descartesDTO = new AddAutoRecommendTaskDTO();
                                BeanUtils.copyProperties(taskDTO, descartesDTO);
                                descartesDTO.setSrcIp(srcIp);
                                descartesDTO.setDstIp(dstIp);
                                descartesSet.add(descartesDTO);
                            }
                        }
                    }
                }
            }
            descartesMap.put(deviceUuid, descartesSet);
        }
        return descartesMap;
    }

    /**
     * 走自动开通-IP 的命令行生成逻辑
     * @param vo
     * @param policyInfoMap
     * @param resultMap
     * @param errorDetailDTOSet
     * @param record
     * @param conflictPolicyMap
     * @throws Exception
     */
    private void generateAutoRecommendForIp(AutoRecommendTaskVO vo, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                            Map<String, Set<AddAutoRecommendTaskDTO>> resultMap,
                                            Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet,AutoRecommendTaskEntity record,
                                            Map<String, AutoRecommendConflictPolicyDTO> conflictPolicyMap) throws Exception{

        this.checkProtectNetworkConfig(vo, policyInfoMap, resultMap, errorDetailDTOSet);

        String message = String.format("新建自动开通任务%s成功", vo.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        record = autoRecommendTaskManager.getByUuid(record.getUuid());

        if (ObjectUtils.isEmpty(resultMap)) {
            logger.error("新建自动开通工单任务失败，失败详情：{}", JSON.toJSONString(errorDetailDTOSet));
            policyInfoMap.clear();
            resultMap.clear();
            record.setErrorMsg(JSONObject.toJSONString(errorDetailDTOSet));
            if(CollectionUtils.isNotEmpty(errorDetailDTOSet)){
                Set<AutoRecommendErrorDetailDTO> collect = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue() ||
                        AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(collect)){
                    Set<AutoRecommendErrorDetailDTO> collect1 = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(collect1)){
                        record.setStatus(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode());
                    }else {
                        record.setStatus(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode());
                    }
                } else {
                    record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
                }
            }else {
                record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            }
            autoRecommendTaskMapper.updateByPrimaryKey(record);
        } else {
            JSONArray jsonArray = new JSONArray();
            AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);

            boolean createAutoTask = false;
            List<RecommendPolicyDTO> existVipName = new ArrayList();
            // 生成安全策略建议
            if (ObjectUtils.isNotEmpty(resultMap)) {
                // 笛卡尔积打开策略建议的源和目的
                Map<String, Set<AddAutoRecommendTaskDTO>> descartesMap = this.descartesTask(resultMap);
                for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : descartesMap.entrySet()) {
                    String deviceUuid = taskMap.getKey();
                    // 飞塔设备因为nat策略命令行包含了安全策略命令行，因此不用重复生成
                    Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                    // 过滤掉类型，只取安全策略的数据
                    set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_N.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                    if(CollectionUtils.isEmpty(set)){
                        continue;
                    }

                    // 1.查询数据流，过滤已开通策略
                    Map<String, Set<AddAutoRecommendTaskDTO>> mergeMap = new HashMap<>();
                    mergeMap.put(deviceUuid, set);
                    Map<String, List<RecommendPolicyDTO>> map = this.converntTaskDTO2PolicyDTO(mergeMap);
                    List<RecommendPolicyDTO> policyDTOList = map.get(deviceUuid);
                    List<RecommendPolicyDTO> filterPolicyDTOList = this.checkDataFlow(new HashSet<>(policyDTOList), deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet,
                            record, conflictPolicyMap);
                    if(CollectionUtils.isEmpty(filterPolicyDTOList)){
                        logger.info("当前策略已开通：{}", JSON.toJSONString(policyDTOList));
                        continue;
                    }

                    // 2.合并检查
                    map.put(deviceUuid, filterPolicyDTOList);
                    logger.info("开始进行合并检查，合并前策略：{}", JSON.toJSONString(map));
                    List<RecommendPolicyDTO> resultPolicyDTOList = mergeService.accurateMergedPolicyMap(map);
                    logger.info("完成合并检查，合并后策略：{}", JSON.toJSONString(resultPolicyDTOList));

                    // 3.生成命令行
                    for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
                        createAutoTask = true;
                        logger.info("------------新建自动开通安全策略任务：{}------------", JSONObject.toJSONString(policyDTO));

                        samePartDTO.setType(PolicyEnum.SECURITY);
                        samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                        RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                        policyDTO.setDeviceUuid(deviceUuid);
                        // 关联安全策略
                        this.buildRelevancyNat(securityEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,jsonArray);

                        this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY,null, null);
                    }
                }
            }

            // 生成Nat策略建议
            if (ObjectUtils.isNotEmpty(resultMap)) {
                if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                    for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
                        String deviceUuid = taskMap.getKey();
                        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
                        Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                        // 过滤掉类型，只取源NAT策略的数据
                        set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_S.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                        if(CollectionUtils.isEmpty(set)){
                            continue;
                        }

                        // 如果是源nat，并且设备未山石，则将目的IP和服务都置为空，重新过滤一次
                        Set<AddAutoRecommendTaskDTO> filterSet = new HashSet<>();
                        if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                            for(AddAutoRecommendTaskDTO  hillstoneTask : set ){
                                hillstoneTask.setDstIp("");
                                hillstoneTask.setServiceList(new ArrayList<>());
                                filterSet.add(hillstoneTask);
                            }
                        }

                        Set<AddAutoRecommendTaskDTO> currentSet = CollectionUtils.isEmpty(filterSet) ? set : filterSet;

                        // 合并源nat
                        List<RecommendPolicyDTO> filterPolicyDTOList = new ArrayList<>();
                        for (AddAutoRecommendTaskDTO taskDTO : currentSet) {
                            logger.info("------------新建自动开通源Nat任务：{}------------", JSONObject.toJSONString(taskDTO));
                            RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
                            BeanUtils.copyProperties(taskDTO, policyDTO);
                            policyDTO.setDeviceUuid(deviceUuid);
                            policyDTO.setNode(taskDTO.getNodeEntity());
                            // 检查源nat策略是否已存在
                            RecommendPolicyDTO resultPolicyDTO = this.checkNatDataFlow(policyDTO, deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet, vo.getAccessType(), 1, conflictPolicyMap, vo, record, existVipName, jsonArray, null);
                            if (ObjectUtils.isEmpty(resultPolicyDTO)) {
                                logger.info("当前源nat策略已开通：{}", JSON.toJSONString(resultPolicyDTO));
                                continue;
                            }
                            filterPolicyDTOList.add(resultPolicyDTO);
                        }

                        // 2.合并检查,目前只针对飞塔设备
                        List<RecommendPolicyDTO> resultPolicyDTOList = new ArrayList<>();
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                            Map<String, List<RecommendPolicyDTO>> map = new HashMap<>();
                            map.put(deviceUuid, filterPolicyDTOList);
                            logger.info("开始进行飞塔源Nat合并检查，合并前策略：{}", JSON.toJSONString(map));
                            resultPolicyDTOList = mergeService.accurateMergedPolicyMapForFortinetNat(map, PushNatTypeEnum.NAT_TYPE_S.getCode());
                            logger.info("完成飞塔源Nat合并检查，合并后策略：{}", JSON.toJSONString(resultPolicyDTOList));
                        }

                        resultPolicyDTOList = CollectionUtils.isEmpty(resultPolicyDTOList) ? filterPolicyDTOList : resultPolicyDTOList;
                        // 过完数据流、合并完、生成策略建议
                        for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
                            // 内网访问互联网类型
                            // 更新任务类型为源nat
                            createAutoTask = true;
                            samePartDTO.setType(PolicyEnum.SNAT);
                            samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT);
                            RecommendTaskEntity snatEntity = this.createSnatTask(policyDTO, samePartDTO, record);
                            this.addAutoRecommendTask(policyDTO, samePartDTO, snatEntity.getId(),PolicyEnum.SNAT,null, null);
                            // 关联nat
                            this.buildRelevancyNat(snatEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT,jsonArray);
                        }
                    }
                } else if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())){
                    for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
                        String deviceUuid = taskMap.getKey();
                        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
                        Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                        // 过滤掉类型，只取目的NAT策略的数据
                        set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_D.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                        if(CollectionUtils.isEmpty(set)){
                            continue;
                        }

                        // 合并目的nat
                        List<MergeFortinetPolicyDTO> fortinetPolicyDTOList = new ArrayList<>();
                        List<RecommendPolicyDTO> filterPolicyDTOList = new ArrayList<>();
                        List<RecommendPolicyDTO> resultPolicyDTOList = new ArrayList<>();
                        for (AddAutoRecommendTaskDTO taskDTO : set) {
                            logger.info("------------新建自动开通目的Nat任务：{}------------", JSONObject.toJSONString(taskDTO));
                            RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
                            BeanUtils.copyProperties(taskDTO, policyDTO);
                            policyDTO.setDeviceUuid(deviceUuid);
                            policyDTO.setNode(taskDTO.getNodeEntity());

                            // 检查目的nat策略是否已存在
                            RecommendPolicyDTO resultPolicyDTO = this.checkNatDataFlow(policyDTO, deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet, vo.getAccessType(), 2,  conflictPolicyMap,vo, record, existVipName,jsonArray, fortinetPolicyDTOList);
                            if(ObjectUtils.isEmpty(resultPolicyDTO)){
                                logger.info("当前目的nat策略已开通：{}", JSON.toJSONString(resultPolicyDTO));
                                continue;
                            }
                            filterPolicyDTOList.add(resultPolicyDTO);
                        }

                        // 2.合并检查,目前只针对飞塔设备
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                            List<RecommendPolicyDTO> mergeFortinetPolicyList = this.mergeFortinetPolicy(fortinetPolicyDTOList);
                            Map<String, List<RecommendPolicyDTO>> map = new HashMap<>();
                            map.put(deviceUuid, mergeFortinetPolicyList);
                            logger.info("开始进行飞塔目的Nat合并检查，当前合并的是五元组，合并前策略：{}", JSON.toJSONString(map));
                            resultPolicyDTOList = mergeService.accurateMergedPolicyMapForFortinetNat(map, PushNatTypeEnum.NAT_TYPE_D.getCode());

                            Map<String, List<RecommendPolicyDTO>> secondMap = new HashMap<>();
                            secondMap.put(deviceUuid, resultPolicyDTOList);
                            logger.info("开始进行飞塔目的Nat第二次合并检查，当前合并的是VIP，合并前策略：{}", JSON.toJSONString(secondMap));
                            resultPolicyDTOList = mergeService.accurateMergedPolicyMapForFortinetVIP(secondMap);
                            logger.info("完成飞塔源Nat合并检查，合并后策略：{}", JSON.toJSONString(resultPolicyDTOList));
                        }

                        resultPolicyDTOList = CollectionUtils.isEmpty(resultPolicyDTOList) ? filterPolicyDTOList : resultPolicyDTOList;
                        for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
                            createAutoTask = true;
                            samePartDTO.setType(PolicyEnum.DNAT);
                            samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT);
                            RecommendTaskEntity dnatEntity = this.createDnatTask(policyDTO, samePartDTO, record);
                            this.addAutoRecommendTask(policyDTO, samePartDTO, dnatEntity.getId(), PolicyEnum.DNAT,null, null);
                            // 关联nat
                            this.buildRelevancyNat(dnatEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT, jsonArray);

                        }
                    }
                }
            }

            if (!createAutoTask && CollectionUtils.isEmpty(existVipName)){
                if(CollectionUtils.isNotEmpty(errorDetailDTOSet)){
                    Set<AutoRecommendErrorDetailDTO> collect = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue() &&
                            AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(collect)){
                        Set<AutoRecommendErrorDetailDTO> collect1 = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                        if (CollectionUtils.isEmpty(collect1)){
                            record.setStatus(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode());
                        }else {
                            record.setStatus(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode());
                        }
                    }else {
                        record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
                    }
                }else {
                    // 如果没有策略开通建议或禁止数据流为空
                    record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
                }
            } else {
                record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS.getCode());
            }

            record.setRelevancyNat(jsonArray.toJSONString());
            record.setErrorMsg(JSONObject.toJSONString(errorDetailDTOSet));
            record.setConflictPolicy(JSONObject.toJSONString(conflictPolicyMap));
            autoRecommendTaskMapper.updateByPrimaryKey(record);
        }
    }

    /**
     * 走自动开通-地址对象 的命令行生成逻辑
     *         // 1.根据地址对象名称查出对应的IP
     *         // 2.将名称和IP建立对应关系
     *         // 3.根据IP查找防护防火墙
     *         // 4.查接口、查域
     *         // 5.生成初步的策略建议
     *         // 6.去重，生成最终策略建议
     *         // 7.生成命令行
     * @param vo
     * @param policyInfoMap
     * @param resultMap
     * @param errorDetailDTOSet
     * @param record
     * @param conflictPolicyMap
     */
    private void generateAutoRecommendForObject(AutoRecommendTaskVO vo, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                                Map<String, Set<AddAutoRecommendTaskDTO>> resultMap,
                                                Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet,AutoRecommendTaskEntity record,
                                                Map<String, AutoRecommendConflictPolicyDTO> conflictPolicyMap) throws Exception{
        // 1.根据地址对象名称查出对应的IP
        Map<String, String> objectNameAndIpMap = new HashMap<>();
        String srcIps = "";
        String dstIps = "";
        boolean srcFindNode = false;
        boolean dstFindNode = false;

        Map<String, String> errorMsgMap = new HashMap<>();
        List<AutoRecommendErrorDetailDTO> srcErrorDetailList = new ArrayList<>();
        List<AutoRecommendErrorDetailDTO> dstErrorDetailList = new ArrayList<>();
        if(InputTypeEnum.SRC_INPUT_TYPE_OBJECT.getCode().equals(vo.getSrcInputType()) && StringUtils.isNotBlank(vo.getSrcAddressObjectName())){
            String[] srcObjectNames = vo.getSrcAddressObjectName().split(",");
            for (String srcObjectName : srcObjectNames) {
                ReturnT<List<AddressDetailEntryEntity>> addressEntityByName = addressManageDetailService.findAddressEntityByName(srcObjectName);
                if(ReturnT.SUCCESS_CODE == addressEntityByName.getCode()){
                    List<AddressDetailEntryEntity> addressDetailList = addressEntityByName.getData();
                    String addressIps = Joiner.on(",").join(addressDetailList.stream().map(AddressDetailEntryEntity :: getAddressName).collect(Collectors.toList()));
                    objectNameAndIpMap.put(srcObjectName, addressIps);
                    srcIps += addressIps + ",";
                }
            }
            vo.setSrcObjectIp(srcIps.substring(0, srcIps.length() -1 ));
        } else {
            srcIps = vo.getSrcIp();
        }

        if(InputTypeEnum.DST_INPUT_TYPE_OBJECT.getCode().equals(vo.getDstInputType()) && StringUtils.isNotBlank(vo.getDstAddressObjectName())){
            String[] dstObjectNames = vo.getDstAddressObjectName().split(",");
            for (String dstObjectName : dstObjectNames) {
                ReturnT<List<AddressDetailEntryEntity>> addressEntityByName = addressManageDetailService.findAddressEntityByName(dstObjectName);
                if(ReturnT.SUCCESS_CODE == addressEntityByName.getCode()){
                    List<AddressDetailEntryEntity> addressDetailList = addressEntityByName.getData();
                    String addressNames = Joiner.on(",").join(addressDetailList.stream().map(AddressDetailEntryEntity :: getAddressName).collect(Collectors.toList()));
                    // 2.将名称和IP建立对应关系
                    objectNameAndIpMap.put(dstObjectName, addressNames);
                    dstIps += addressNames + ",";
                }
            }
            vo.setDstObjectIp(dstIps.substring(0,dstIps.length() -1 ));
        } else {
            dstIps = vo.getDstIp();
        }

        String srcIpOrName = StringUtils.isEmpty(vo.getSrcObjectIp()) ? vo.getSrcIp() : vo.getSrcObjectIp();
        String dstIpOrName = StringUtils.isEmpty(vo.getDstObjectIp()) ? vo.getDstIp() : vo.getDstObjectIp();

        // 3.根据IP查找防护防火墙
        if(StringUtils.isNotBlank(srcIps)){
            String[] srcIpList = srcIps.split(",");
            for (String srcIp : srcIpList) {
                if(IpUtils.isIPSegment(srcIp) && StringUtils.equalsAnyIgnoreCase(IpUtils.getMaskBitFromIpSegment(srcIp), "0")){
                    policyInfoMap.clear();
                    errorDetailDTOSet.clear();
                    this.setErrorMsg("禁止开通掩码为0的工单 [Creating policy with subnet mask 0 is prohibited]", srcIpOrName, dstIpOrName, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                            vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                            null, vo.getAccessType(),null,null,null,null);
                    return;
                }

                // 生成安全策略
                List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(srcIp, vo);
                if (CollectionUtils.isEmpty(list)) {
                    // TODO 优化
                    if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                        String msg = "源IP:" + srcIp + "找不到防护网段数据，请检查防护网段配置 [Source ip is not in protect network table,please check the protect network table]";
                        AutoRecommendErrorDetailDTO srcErrorDetailDTO = this.setSrcErrorMsg(msg, srcIp, dstIpOrName, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),vo.getAccessType());
                        srcErrorDetailList.add(srcErrorDetailDTO);
                        continue;
                    } else {
                        errorMsgMap.put(srcIp, dstIpOrName);
                        continue;
                    }
                }
                srcFindNode = true;
                for (ProtectNetworkConfigEntity configEntity : list) {
                    Set<AddAutoRecommendTaskDTO> taskDTOSet= new HashSet<>();
                    NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                    if (null == nodeEntity) {
                        String msg = configEntity.getDeviceIp() + "防火墙已删除，请更新防护网段配置 [Source ip can't find relevant firewall in protect network table,please check the protect network table]";
                        this.setErrorMsg(msg, srcIp, dstIpOrName, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
                        continue;
                    }
                    String deviceUuid = nodeEntity.getUuid();
                    srcIp = StringUtils.isBlank(configEntity.getConvertRangeIp()) ? srcIp : configEntity.getConvertRangeIp();

                    String[] srcIpString = srcIp.split(",");

                    vo.setDstIp(dstIpOrName);
                    this.setSrcSecurityData(srcIpString, vo, nodeEntity,policyInfoMap,deviceUuid,taskDTOSet);
                }
            }
        }

        if(StringUtils.isNotBlank(dstIps)){
            String[] dstIpList = dstIps.split(",");
            for (String dstIp : dstIpList) {
                if(IpUtils.isIPSegment(dstIp) && StringUtils.equalsAnyIgnoreCase(IpUtils.getMaskBitFromIpSegment(dstIp), "0")){
                    policyInfoMap.clear();
                    errorDetailDTOSet.clear();
                    this.setErrorMsg("禁止开通掩码为0的工单 [Prohibit create policy with subnet mask 0]", srcIpOrName, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                            vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                            null, vo.getAccessType(),null,null,null,null);
                    return;
                }

                // 内网互访情况下，只用生成安全策略
                List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(dstIp, vo);
                if (CollectionUtils.isEmpty(list)) {
                    if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                        String msg = "目的IP:" + dstIp + "找不到防护网段数据，请检查防护网段配置 [Destination ip is not in the protect network table,please check the protect network table]";
                        AutoRecommendErrorDetailDTO srcErrorDetailDTO = this.setSrcErrorMsg(msg, srcIpOrName, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),vo.getAccessType());
                        dstErrorDetailList.add(srcErrorDetailDTO);

                        continue;
                    } else {
                        String[] errorIP = srcAndDstAllError(errorMsgMap, vo.getSrcIp(), dstIp);
                        if(ObjectUtils.isNotEmpty(errorIP) && errorIP.length == 2){
                            String msg = "源IP："+ errorIP[0] +"目的IP:" + errorIP[1] + "均找不到防护网段数据，请检查防护网段配置 [Source ip:"+errorIP[0]+" and destination ip:"+ errorIP[1]+" are not in the protect network table,please check the protect network table]";
                            this.setErrorMsg(msg, errorIP[0], errorIP[1], vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
                        }
                        continue;
                    }
                }

                dstFindNode = true;
                for (ProtectNetworkConfigEntity configEntity : list) {
                    Set<AddAutoRecommendTaskDTO> taskDTOSet = new HashSet<>();
                    NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                    if (null == nodeEntity) {
                        String msg = configEntity.getDeviceIp() + "防火墙已删除，请更新防护网段配置 [Firewall offline,please check the protect network table]";
                        this.setErrorMsg(msg, srcIpOrName, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(), null,null,null,null);
                        continue;
                    }
                    String deviceUuid = nodeEntity.getUuid();
                    // 此处用交集的范围IP去匹配Nat映射关系、去查询域和接口信息
                    dstIp = StringUtils.isBlank(configEntity.getConvertRangeIp()) ? dstIp : configEntity.getConvertRangeIp();
                    String[] dstIpString = dstIp.split(",");
                    vo.setSrcIp(srcIpOrName);
                    this.setDstSecurityData(vo,nodeEntity,  policyInfoMap, deviceUuid, taskDTOSet, dstIpString);
                }
            }
        }

        record = autoRecommendTaskManager.getByUuid(record.getUuid());
        if(!srcFindNode && !dstFindNode){
            policyInfoMap.clear();
            String msg = "源、目的IP均未找到防护网段配置，不生成策略建议 [Source ip and destination ip are not in the protect network table,please check the protect network table]";
            this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
            // 更新任务状态
            record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            autoRecommendTaskMapper.updateByPrimaryKey(record);
            return;
        }


        // 4.查找域和接口信息
        if(srcFindNode || dstFindNode){
            this.getInterfaceAndZone(policyInfoMap, resultMap, vo, errorDetailDTOSet);
        }

        String message = String.format("新建自动开通任务%s成功", vo.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        if (ObjectUtils.isEmpty(resultMap)) {
            logger.error("新建自动开通工单任务失败，失败详情：{}", JSON.toJSONString(errorDetailDTOSet));
            policyInfoMap.clear();
            resultMap.clear();
            record.setErrorMsg(JSONObject.toJSONString(errorDetailDTOSet));
            if(CollectionUtils.isNotEmpty(errorDetailDTOSet)){
                Set<AutoRecommendErrorDetailDTO> collect = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue() ||
                        AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(collect)){
                    Set<AutoRecommendErrorDetailDTO> collect1 = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(collect1)){
                        record.setStatus(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode());
                    }else {
                        record.setStatus(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode());
                    }
                }else {
                    record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
                }
            }else {
                record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            }
            // 更新任务状态
            autoRecommendTaskMapper.updateByPrimaryKey(record);
        } else {
            JSONArray jsonArray = new JSONArray();
            AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);

            boolean createAutoTask = false;
            List<RecommendPolicyDTO> existVipName = new ArrayList();
            // 生成安全策略建议
            if (ObjectUtils.isNotEmpty(resultMap)) {
                for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
                    String deviceUuid = taskMap.getKey();
                    // 飞塔设备因为nat策略命令行包含了安全策略命令行，因此不用重复生成
                    Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                    // 过滤掉类型，只取安全策略的数据
                    set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_N.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                    if(CollectionUtils.isEmpty(set)){
                        continue;
                    }

                    // 1.查询数据流，过滤已开通策略
                    Map<String, Set<AddAutoRecommendTaskDTO>> mergeMap = new HashMap<>();
                    mergeMap.put(deviceUuid, set);
                    Map<String, List<RecommendPolicyDTO>> map = this.converntTaskDTO2PolicyDTO(mergeMap);
                    /*List<RecommendPolicyDTO> policyDTOList = map.get(deviceUuid);
                    List<RecommendPolicyDTO> filterPolicyDTOList = this.checkDataFlow(new HashSet<>(policyDTOList), deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet,
                            vo.getAccessType(), conflictPolicyMap);
                    if(CollectionUtils.isEmpty(filterPolicyDTOList)){
                        logger.info("当前策略已开通：{}", JSON.toJSONString(policyDTOList));
                        continue;
                    }*/

                    // 2.合并检查
//                    map.put(deviceUuid, filterPolicyDTOList);
                    logger.info("开始进行合并检查，合并前策略：{}", JSON.toJSONString(map));
                    List<RecommendPolicyDTO> resultPolicyDTOList = mergeService.accurateMergedPolicyMap(map);
                    logger.info("完成合并检查，合并后策略：{}", JSON.toJSONString(resultPolicyDTOList));

                    // 3.生成命令行
                    Set<RecommendPolicyDTO> recommendTaskDTOSet = new HashSet<>();
                    for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
//                        RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
//                        BeanUtils.copyProperties(taskDTO, policyDTO);
//                        policyDTO.setDeviceUuid(deviceUuid);
//                        policyDTO.setNode(taskDTO.getNodeEntity());

                        Set<String> srcNameSet = new HashSet<>();
                        Set<String> srcIpSet = new HashSet<>();
                        Set<String> dstNameSet = new HashSet<>();
                        Set<String> dstIpSet = new HashSet<>();

                        if(InputTypeEnum.SRC_INPUT_TYPE_OBJECT.getCode().equals(vo.getSrcInputType()) && StringUtils.isNotBlank(vo.getSrcAddressObjectName())){
                            for (Map.Entry<String, String> nameAndIp : objectNameAndIpMap.entrySet()) {
                                // 如果策略建议中包含名称-IP的对应关系中的某一个IP，则设置对象名称
                                String addreessName = nameAndIp.getKey();
                                String currentIp = nameAndIp.getValue();
                                String[] currentIps = currentIp.split(",");
                                for (String ip : currentIps) {
                                    if(StringUtils.isNotBlank(policyDTO.getSrcIp())){
                                        String[] policyIps = policyDTO.getSrcIp().split(",");
                                        for(String policyIp : policyIps){
                                            if(IpUtils.checkIpRange(policyIp, ip)){
                                                srcNameSet.add(addreessName);
                                                srcIpSet.add(currentIp);
                                            }
                                        }
                                    }
                                }
                            }
                            policyDTO.setSrcIp(String.join(",", srcIpSet));
                        }

                        if(InputTypeEnum.DST_INPUT_TYPE_OBJECT.getCode().equals(vo.getDstInputType()) && StringUtils.isNotBlank(vo.getDstAddressObjectName())){
                            for (Map.Entry<String, String> nameAndIp : objectNameAndIpMap.entrySet()) {
                                // 如果策略建议中包含名称-IP的对应关系中的某一个IP，则设置对象名称
                                String addreessName = nameAndIp.getKey();
                                String currentIp = nameAndIp.getValue();
                                String[] currentIps = currentIp.split(",");
                                for (String ip : currentIps) {
                                    if(StringUtils.isNotBlank(policyDTO.getDstIp())){
                                        String[] policyIps = policyDTO.getDstIp().split(",");
                                        for(String policyIp : policyIps){
                                            if(IpUtils.checkIpRange(policyIp, ip)){
                                                dstNameSet.add(addreessName);
                                                dstIpSet.add(currentIp);
                                            }
                                        }
                                    }
                                }
                            }
                            policyDTO.setDstIp(String.join(",", dstIpSet));
                        }

                        // 根据名称去策略列表上查询是否已存在该名称，存在则复用，不存在则新建
                        AutoRecommendSpecialDTO autoRecommendSpecialDTO = this.buildSpecialDTO(srcNameSet, dstNameSet, objectNameAndIpMap, deviceUuid);

                        createAutoTask = true;
                        logger.info("------------新建自动开通安全策略任务：{}------------", JSONObject.toJSONString(policyDTO));

                        samePartDTO.setType(PolicyEnum.SECURITY);
                        samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                        if(CollectionUtils.isNotEmpty(srcNameSet)){
                            policyDTO.setSrcAddressObjectName(String.join(",", srcNameSet));
                        }
                        if(CollectionUtils.isNotEmpty(dstNameSet)){
                            policyDTO.setDstAddressObjectName(String.join(",", dstNameSet));
                        }
                        if(recommendTaskDTOSet.contains(policyDTO)){
                            logger.info("当前策略建议已存在：{}", JSON.toJSONString(policyDTO));
                            continue;
                        } else {
                            recommendTaskDTOSet.add(policyDTO);
                        }
                        RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                        policyDTO.setDeviceUuid(deviceUuid);
                        // 关联安全策略
                        this.buildRelevancyNat(securityEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,jsonArray);

                        this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY,null, autoRecommendSpecialDTO);
                    }
                    recommendTaskDTOSet.clear();
                }
            }

            if (!createAutoTask && CollectionUtils.isEmpty(existVipName)){
                if(CollectionUtils.isNotEmpty(errorDetailDTOSet)){
                    Set<AutoRecommendErrorDetailDTO> collect = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue() ||
                            AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(collect)){
                        Set<AutoRecommendErrorDetailDTO> collect1 = errorDetailDTOSet.stream().filter(p -> AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode() != p.getStatus().intValue()).collect(Collectors.toSet());
                        if (CollectionUtils.isEmpty(collect1)){
                            record.setStatus(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode());
                        }else {
                            record.setStatus(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode());
                        }
                    }else {
                        record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
                    }
                }else {
                    // 如果没有策略开通建议或禁止数据流为空
                    record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
                }
            } else {
                record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_SUCCESS.getCode());
            }

            record.setRelevancyNat(jsonArray.toJSONString());
            record.setErrorMsg(JSONObject.toJSONString(errorDetailDTOSet));
            record.setConflictPolicy(JSONObject.toJSONString(conflictPolicyMap));
            autoRecommendTaskMapper.updateByPrimaryKey(record);
        }
    }

    //工单中涉及到10个设备，若其中一个设备命令行生成成功， 其他9个设备命令行生成失败，则工单状态为命令行部分成功。
    public boolean isPartSuccess(AutoRecommendTaskEntity entity){


        Map<String,Boolean> map= new HashMap<>();
        if (StringUtils.isNotEmpty(entity.getErrorMsg())){
            List<AutoRecommendErrorDetailDTO> errorDetailDTOList = JSONArray.parseArray(entity.getErrorMsg(), AutoRecommendErrorDetailDTO.class);
            List<String> ips = errorDetailDTOList.stream().map(p -> p.getDeviceIp()).distinct().collect(Collectors.toList());
//            List<NodeEntity> nodeByIpList = nodeMapper.getTheNodeByIpList(ips);
//            List<String> uuids = nodeByIpList.stream().map(p -> p.getUuid()).collect(Collectors.toList());
            List<NodeEntity> nodeByIpList = new ArrayList<>();
            List<String> uuids = new ArrayList<>();
            ips.forEach(p -> {
                NodeEntity theNodeByIp = nodeMapper.getTheNodeByIp(p);
                if(ObjectUtils.isNotEmpty(theNodeByIp) && StringUtils.isNotEmpty(theNodeByIp.getUuid())){
                    uuids.add(theNodeByIp.getUuid());
                }
            });
            uuids.forEach(p ->{
                map.put(p,false);
            });
        }
        if (StringUtils.isNotEmpty(entity.getRelevancyNat())){
            String relevancyNat = entity.getRelevancyNat();
            JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
            if (null != jsonArray && jsonArray.size() > 0){
                for (int index = 0; index < jsonArray.size(); index++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    Integer natId = jsonObject.getInteger("id");
                    List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(natId);
                    if (CollectionUtils.isNotEmpty(commandTaskEditableList)) {
                        for (CommandTaskEditableEntity editTaskEntity : commandTaskEditableList) {
                            String command = editTaskEntity.getCommandline();
                            String deviceUuid = editTaskEntity.getDeviceUuid();
                            if (map.containsKey(deviceUuid) && map.get(deviceUuid)){
                                continue;
                            }else {
                                if (StringUtils.isEmpty(command) || (StringUtils.isNotEmpty(command) && command.startsWith("无法生成该设备的命令行"))){
                                    map.put(deviceUuid,false);
                                }else {
                                    map.put(deviceUuid,true);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!map.isEmpty()){
            Set<String> strings = map.keySet();
            Iterator<String> iterator = strings.iterator();
            if (map.size() == 1){
                if (iterator.hasNext()){
                    String next = iterator.next();
                    if (map.get(next)){
                        return false;
                    }else {
                        return true;
                    }
                }
            }else {
                Set<Boolean> list = new HashSet<>();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    list.add(map.get(next));
                }
                Iterator<Boolean> iterator1 = list.iterator();
                if (list.size() == 1){
                    if (iterator1.hasNext()){
                        if (iterator1.next()){
                            return false;
                        }else {
                            return true;
                        }
                    }
                }else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 组装自动开通生成地址对象命令行特殊参数
     * @param srcNameSet
     * @param dstNameSet
     * @param objectNameAndIpMap
     * @param deviceUuid
     * @return
     */
    private AutoRecommendSpecialDTO buildSpecialDTO(Set<String> srcNameSet, Set<String> dstNameSet, Map<String, String> objectNameAndIpMap,String deviceUuid){

        AutoRecommendSpecialDTO autoRecommendSpecialDTO = new AutoRecommendSpecialDTO();
        List<AddressObjectInfoDTO> existSrcAddressList = new ArrayList<>();
        List<AddressObjectInfoDTO> restSrcAddressList = new ArrayList<>();
        List<AddressObjectInfoDTO> existDstAddressList = new ArrayList<>();
        List<AddressObjectInfoDTO> restDstAddressList = new ArrayList<>();

        // 查询源地址是否已存在
        for (String srcObjectName : srcNameSet ) {
            ResultRO<List<NetWorkGroupObjectShowVO>> dataResultRO = addressManageTaskManager.getDeviceAddressByName(deviceUuid, srcObjectName);
            if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                logger.info("根据设备uuid：{} 查询地址对象名称：{}，未查询到数据", deviceUuid, srcObjectName);
                AddressObjectInfoDTO restSrcInfoDTO = new AddressObjectInfoDTO();
                restSrcInfoDTO.setAddressObjectName(srcObjectName);
                restSrcInfoDTO.setAddressObjectIP(objectNameAndIpMap.get(srcObjectName));
                restSrcAddressList.add(restSrcInfoDTO);
            } else {
                boolean existObjectName = false;
                List<NetWorkGroupObjectShowVO> dataList = dataResultRO.getData();
                for(NetWorkGroupObjectShowVO objectShowVO : dataList ){
                    if(StringUtils.equals(srcObjectName, objectShowVO.getName())){
                        existObjectName = true;
                        break;
                    }
                }
                // 判断是新建还是复用
                if(existObjectName){
                    AddressObjectInfoDTO existSrcInfoDTO = new AddressObjectInfoDTO();
                    existSrcInfoDTO.setAddressObjectIP(objectNameAndIpMap.get(srcObjectName));
                    existSrcInfoDTO.setAddressObjectName(srcObjectName);
                    existSrcAddressList.add(existSrcInfoDTO);
                } else {
                    AddressObjectInfoDTO restSrcInfoDTO = new AddressObjectInfoDTO();
                    restSrcInfoDTO.setAddressObjectIP(objectNameAndIpMap.get(srcObjectName));
                    restSrcInfoDTO.setAddressObjectName(srcObjectName);
                    restSrcAddressList.add(restSrcInfoDTO);
                }
            }
        }

        // 查询目的地址是否已存在
        for (String dstObjectName : dstNameSet ) {
            ResultRO<List<NetWorkGroupObjectShowVO>> dataResultRO = addressManageTaskManager.getDeviceAddressByName(deviceUuid, dstObjectName);
            if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                logger.info("根据设备uuid：{} 查询地址对象名称：{}，未查询到数据",deviceUuid, dstObjectName);
                AddressObjectInfoDTO restDstInfoDTO = new AddressObjectInfoDTO();
                restDstInfoDTO.setAddressObjectIP(objectNameAndIpMap.get(dstObjectName));
                restDstInfoDTO.setAddressObjectName(dstObjectName);
                restDstAddressList.add(restDstInfoDTO);
            } else {
                boolean existObjectName = false;
                List<NetWorkGroupObjectShowVO> dataList = dataResultRO.getData();
                for(NetWorkGroupObjectShowVO objectShowVO : dataList ){
                    if(StringUtils.equals(dstObjectName, objectShowVO.getName())){
                        existObjectName = true;
                        break;
                    }
                }
                // 判断是新建还是复用
                if(existObjectName){
                    AddressObjectInfoDTO existDstInfoDTO = new AddressObjectInfoDTO();
                    existDstInfoDTO.setAddressObjectName(dstObjectName);
                    existDstInfoDTO.setAddressObjectIP(objectNameAndIpMap.get(dstObjectName));
                    existDstAddressList.add(existDstInfoDTO);
                } else {
                    AddressObjectInfoDTO restDstInfoDTO = new AddressObjectInfoDTO();
                    restDstInfoDTO.setAddressObjectName(dstObjectName);
                    restDstInfoDTO.setAddressObjectIP(objectNameAndIpMap.get(dstObjectName));
                    restDstAddressList.add(restDstInfoDTO);
                }
            }
        }
        autoRecommendSpecialDTO.setRestSrcAddressList(restSrcAddressList);
        autoRecommendSpecialDTO.setExistSrcAddressList(existSrcAddressList);
        autoRecommendSpecialDTO.setRestDstAddressList(restDstAddressList);
        autoRecommendSpecialDTO.setExistDstAddressList(existDstAddressList);
        return autoRecommendSpecialDTO;
    }

    @Override
    public ReturnT getByDevice(AutoRecommendConflictVo conflictVo) {
        AutoRecommendGetByDeviceResultDTO allResultDTO = new AutoRecommendGetByDeviceResultDTO();
        Set<RecommendTaskEntity> securityPolicyDTOSet = new HashSet<>();
        Set<RecommendTaskEntity> natPolicyDTOSet = new HashSet<>();
        List<CommandTaskEditableEntity> cmdList = new ArrayList<>();
        AutoRecommendTaskEntity autoTaskEntity;
        if(ObjectUtils.isNotEmpty(conflictVo.getAutoTaskId())){
            autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(conflictVo.getAutoTaskId());
            if(autoTaskEntity == null ){
                logger.info("根据ID：{} 未查询到自动开通任务工单", conflictVo.getAutoTaskId());
                return new ReturnT(allResultDTO);
            }
        } else {
            return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
        }

        String relevancyNat = autoTaskEntity.getRelevancyNat();
        if(StringUtils.isNotBlank(relevancyNat)){
            JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
            for (int index = 0; index < jsonArray.size(); index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                Integer entityId = jsonObject.getInteger("id");

                RecommendTaskEntity recommendTaskEntity = recommendTaskMapper.getById(entityId);
                Map<String, String> params = new HashMap<>();
                params.put("taskId", String.valueOf(entityId));
                params.put("deviceUuid", conflictVo.getDeviceUuid());
                List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskIdAndDeviceUuid(params);
                if (CollectionUtils.isEmpty(commandTaskEditableList)){
                    continue;
                }
                recommendTaskEntity.setStatus(commandTaskEditableList.get(0).getPushStatus());
                cmdList.addAll(commandTaskEditableList);
                for (CommandTaskEditableEntity data : commandTaskEditableList){
                    if(data.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED)){
                        securityPolicyDTOSet.add(recommendTaskEntity);
                    } else {
                        natPolicyDTOSet.add(recommendTaskEntity);
                    }
                }
            }
        }

        allResultDTO.setCommandTaskEditableList(cmdList);
        allResultDTO.setSecurityPolicyDTOSet(securityPolicyDTOSet);
        allResultDTO.setNatPolicyDTOSet(natPolicyDTOSet);
        return new ReturnT(allResultDTO);
    }

    @Override
    public ReturnT getStatusInfo(AutoRecommendConflictVo conflictVo) {
        AutoRecommendAllResultNewDTO allResultDTO = new AutoRecommendAllResultNewDTO();
        List<AutoRecommendOrderStatusNewDTO> securityOrderStatusDTOList = new ArrayList<>();
        List<AutoRecommendOrderStatusNewDTO> natOrderStatusDTOList = new ArrayList<>();
        Set<NodeEntity> nodeEntitySet = new HashSet<>();

        AutoRecommendTaskEntity autoTaskEntity;
        if(StringUtils.isNotBlank(conflictVo.getTheme())){
            autoTaskEntity = autoRecommendTaskMapper.getByName(conflictVo.getTheme());
            if(autoTaskEntity == null ){
                logger.info("根据ID：{} 未查询到自动开通任务工单", conflictVo.getAutoTaskId());
                return new ReturnT(allResultDTO);
            }
        } else if(ObjectUtils.isNotEmpty(conflictVo.getAutoTaskId())){
            autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(conflictVo.getAutoTaskId());
            if(autoTaskEntity == null ){
                logger.info("根据ID：{} 未查询到自动开通任务工单", conflictVo.getAutoTaskId());
                return new ReturnT(allResultDTO);
            }
        } else {
            return new ReturnT(ReturnT.FAIL_CODE,"必要参数缺失");
        }

        // 设置策略日志
        if(StringUtils.isNotBlank(autoTaskEntity.getErrorMsg())){
            allResultDTO.setErrorDetailDTOList(JSONArray.parseArray(autoTaskEntity.getErrorMsg(), AutoRecommendErrorDetailDTO.class));
        }

        // 设置 不开通/已开通 工单状态数据
        this.setErrorOrderStatusListNew(allResultDTO.getErrorDetailDTOList(), securityOrderStatusDTOList, natOrderStatusDTOList);

        List<CommandTaskEditableEntity> allCmdList = new ArrayList<>();
        Map<CommandTaskEditableEntity, RecommendTaskEntity> mappingMap = new HashMap<>();

        String relevancyNat = autoTaskEntity.getRelevancyNat();
        if(StringUtils.isNotBlank(relevancyNat)){
            JSONArray jsonArray = JSONArray.parseArray(relevancyNat);
            for (int index = 0; index < jsonArray.size(); index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                Integer entityId = jsonObject.getInteger("id");

                RecommendTaskEntity recommendTaskEntity = recommendTaskMapper.getById(entityId);

                List<CommandTaskEditableEntity> commandTaskEditableList = commandTaskEdiableMapper.selectByTaskId(entityId);
                if (CollectionUtils.isEmpty(commandTaskEditableList)){
                    continue;
                }
                for (CommandTaskEditableEntity data : commandTaskEditableList){
                    // 建立映射关系
                    mappingMap.put(data, recommendTaskEntity);
                    allCmdList.add(data);
                }
            }
        }

        AutoRecommendOrderStatusNewDTO securityWaitNewDTO = new AutoRecommendOrderStatusNewDTO();
        Set<AutoRecommendOrderDTO> securityWaitDTOSet = new HashSet<>();
        securityWaitNewDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_WAIT_CREATE.getCode());

        AutoRecommendOrderStatusNewDTO natWaitNewDTO = new AutoRecommendOrderStatusNewDTO();
        Set<AutoRecommendOrderDTO> natWaitDTOSet = new HashSet<>();
        natWaitNewDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_WAIT_CREATE.getCode());

        for (CommandTaskEditableEntity data : allCmdList ) {
            RecommendTaskEntity recommendTaskEntity = mappingMap.get(data);
            // 针对状态特殊处理，详情中展示下发状态，不展示任务状态
            List<CommandTaskEditableEntity> commandTaskEditableEntities = commandTaskEdiableMapper.selectByTaskId(recommendTaskEntity.getId());
            if(CollectionUtils.isEmpty(commandTaskEditableEntities)){
                continue;
            }
            String deviceUuid = data.getDeviceUuid();
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            String deviceName = nodeEntity.getDeviceName();
            DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
            DeviceDataRO deviceData = device.getData().get(0);
            // 如果是虚墙时需显示虚墙名称
            if (deviceData.getIsVsys() != null && deviceData.getIsVsys()) {
                deviceName = deviceData.getVsysName();
            }
            nodeEntity.setDeviceName(deviceName);
            nodeEntitySet.add(nodeEntity);
            if(data.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED)){
                AutoRecommendOrderDTO securityWaitDTO = new AutoRecommendOrderDTO();
                securityWaitDTO.setSrcIp(recommendTaskEntity.getSrcIp());
                securityWaitDTO.setDstIp(recommendTaskEntity.getDstIp());

                List<ServiceDTO> serviceList = ServiceDTOUtils.toList(recommendTaskEntity.getServiceList());
                securityWaitDTO.setServiceList(serviceList);
                securityWaitDTO.setEntity(nodeEntity);
                securityWaitDTOSet.add(securityWaitDTO);
            } else {
                AutoRecommendOrderDTO natWaitDTO = new AutoRecommendOrderDTO();
                String additionInfo = recommendTaskEntity.getAdditionInfo();
                JSONObject additionJson = JSONObject.parseObject(additionInfo);
                JSONObject fortinetDnatSpecialDTO = additionJson.getJSONObject("fortinetDnatSpecialDTO");
                if(ObjectUtils.isNotEmpty(fortinetDnatSpecialDTO)){
                    StringBuilder dstSb = new StringBuilder();
                    JSONArray existVipInfos = fortinetDnatSpecialDTO.getJSONArray("existVipList");
                    if(ObjectUtils.isNotEmpty(existVipInfos)){
                        for(int i=0; i< existVipInfos.size(); i++){
                            JSONObject jsonObject = existVipInfos.getJSONObject(i);
                            String vipName = jsonObject.getString("vipName");
                            String preDstIp = jsonObject.getString("preDstIp");
                            String postDstIp = jsonObject.getString("postDstIp");
                            dstSb.append(vipName).append("(").append(preDstIp).append("->").append(postDstIp).append(")").append("\n");
                        }
                    }
                    JSONArray restVipInfos = fortinetDnatSpecialDTO.getJSONArray("restVipList");
                    if(ObjectUtils.isNotEmpty(restVipInfos)){
                        for(int i=0; i< restVipInfos.size(); i++){
                            JSONObject jsonObject = restVipInfos.getJSONObject(i);
                            String vipName = jsonObject.getString("vipName");
                            String preDstIp = jsonObject.getString("preDstIp");
                            String postDstIp = jsonObject.getString("postDstIp");
                            dstSb.append(vipName).append("(").append(preDstIp).append("->").append(postDstIp).append(")").append("\n");
                        }
                    }
                    natWaitDTO.setDstIp(dstSb.toString());
                } else {
                    natWaitDTO.setDstIp(recommendTaskEntity.getDstIp());
                }

                natWaitDTO.setSrcIp(recommendTaskEntity.getSrcIp());

                List<ServiceDTO> serviceList = ServiceDTOUtils.toList(recommendTaskEntity.getServiceList());
                natWaitDTO.setServiceList(serviceList);
                natWaitDTO.setEntity(nodeEntity);
                natWaitDTOSet.add(natWaitDTO);
            }

            if(CollectionUtils.isNotEmpty(securityWaitDTOSet)){
                securityWaitNewDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_WAIT_CREATE.getCode());
                securityWaitNewDTO.setOrderDTOSet(securityWaitDTOSet);
            }

            if(CollectionUtils.isNotEmpty(natWaitDTOSet)){
                natWaitNewDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_WAIT_CREATE.getCode());
                natWaitNewDTO.setOrderDTOSet(natWaitDTOSet);
            }
        }

        // 根据策略开通建议生成待开通工单状态数据
        if(ObjectUtils.isNotEmpty(securityWaitNewDTO)){
            securityOrderStatusDTOList.add(securityWaitNewDTO);
        }
        if(ObjectUtils.isNotEmpty(natWaitNewDTO)){
            natOrderStatusDTOList.add(natWaitNewDTO);
        }
        allResultDTO.setSecurityOrderStatusDTOList(securityOrderStatusDTOList);
        allResultDTO.setNatOrderStatusDTOList(natOrderStatusDTOList);
        allResultDTO.setNodeEntitySet(nodeEntitySet);
        return new ReturnT(allResultDTO);
    }

    /**
     * 批量加入自动开通冲突策略表
     * @param securityConflictPolicyDTOList
     * @param autoTaskId
     * @param theme
     * @param policyType 策略类型，0：安全策略；1：nat策略
     */
    private void batchInsert(List<PolicyDetailVO> securityConflictPolicyDTOList, Integer autoTaskId, String theme, String policyType){
        List<AutoRecommendConflictPolicyVo> voList = new ArrayList<>();
        for(PolicyDetailVO policyDetailVO : securityConflictPolicyDTOList){
            AutoRecommendConflictPolicyVo policyVo = new AutoRecommendConflictPolicyVo();
            policyVo.setAutoTaskId(autoTaskId);
            policyVo.setTheme(theme);
            policyVo.setPolicyType(policyType);
            BeanUtils.copyProperties(policyDetailVO, policyVo);
            voList.add(policyVo);
        }
        pushAutoRecommendConflictPolicyService.batchInsert(voList);
    }

    /**
     * 合并飞塔目的NAT
     * @param fortinetPolicyDTOList
     * @throws Exception
     */
    private List<RecommendPolicyDTO> mergeFortinetPolicy(List<MergeFortinetPolicyDTO> fortinetPolicyDTOList) throws Exception{
        List<RecommendPolicyDTO> mergeFortinetPolicyList = new ArrayList<>();
        for (MergeFortinetPolicyDTO fortinetPolicyDTO : fortinetPolicyDTOList) {
            AutoRecommendFortinetDnatSpecialDTO fortinetDnatSpecialDTO = new AutoRecommendFortinetDnatSpecialDTO();

            NodeEntity nodeEntity = fortinetPolicyDTO.getNodeEntity();
            RecommendPolicyDTO policyDTO = fortinetPolicyDTO.getPolicyDTO();
            AutoRecommendTaskEntity record = fortinetPolicyDTO.getRecord();
            JSONArray taskJsonArray = fortinetPolicyDTO.getTaskJsonArray();
            AutoRecommendTaskVO vo = fortinetPolicyDTO.getVo();
            if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                // 针对飞塔的目的Nat:如果restFlow里面数据不为空，则需要根据转换前目的IP，转换后目的IP去查询一下策略列表，看VIP映射关系时否存在，存在则复用，并生成一条安全策略建议，不存在但不为空，则提示错误信息，为空则走正常流程
                String ipTerm = this.buildIpTerm(policyDTO.getPreDstIp());
                JSONArray jsonArray = remotePolicyService.remotePolicyDetailByIpTerms(ipTerm, null, nodeEntity.getUuid(), SYSTEM__NAT_LIST.getCode());
                if (jsonArray != null && jsonArray.size() > 0) {
                    List<PolicyDetailVO> policyDetailVOS = jsonArray.toJavaList(PolicyDetailVO.class);
                    // 判断转换后关系是否存在
                    boolean existPostDstIp = false;
                    String vipName = null;
                    for (PolicyDetailVO policyDetailVO : policyDetailVOS ){
                        if(StringUtils.equals(policyDetailVO.getPostDstIp(), policyDTO.getPostDstIp())){
                            existPostDstIp = true;
                            vipName = policyDetailVO.getPolicyName();
                            break;
                        }
                    }
                    // 如果存在转换后的目的IP并且VIP名称不为空，则复用VIP名称，创建一个目的地址为VIP的安全策略，否则给出错误信息提示
                    if(existPostDstIp && StringUtils.isNotBlank(vipName)){
                        AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);
                        samePartDTO.setType(PolicyEnum.SECURITY);
                        samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                        // 设置目的地址为VIP名称
                        List<AutoRecommendFortinetDnatInfoDTO> existVipList = new ArrayList<>();
                        AutoRecommendFortinetDnatInfoDTO fortinetDnatInfoDTO = new AutoRecommendFortinetDnatInfoDTO();
                        fortinetDnatInfoDTO.setVipName(vipName);
                        fortinetDnatInfoDTO.setPreDstIp(policyDTO.getPreDstIp());
                        fortinetDnatInfoDTO.setPostDstIp(policyDTO.getPostDstIp());
                        existVipList.add(fortinetDnatInfoDTO);
                        fortinetDnatSpecialDTO.setExistVipList(existVipList);
                        policyDTO.setFortinetDnatSpecialDTO(fortinetDnatSpecialDTO);
                        policyDTO.setVipName(vipName);

                        /*policyDTO.setDstIp(vipName);
                        policyDTO.setDeviceUuid(nodeEntity.getUuid());
                        RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                        // 关联安全策略
                        this.buildRelevancyNat(securityEntity, nodeEntity.getUuid(), record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,taskJsonArray);
                        this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY, vipName);*/

                    }
                } else {
                    // 新建VIP名称
                    String fortinetCreateName = String.format("mip_%s_%s ", record.getTheme(), IdGen.getRandomNumberString());
                    // 设置目的地址为VIP名称
                    List<AutoRecommendFortinetDnatInfoDTO> restVipList = new ArrayList<>();
                    AutoRecommendFortinetDnatInfoDTO fortinetDnatInfoDTO = new AutoRecommendFortinetDnatInfoDTO();
                    fortinetDnatInfoDTO.setVipName(fortinetCreateName);
                    fortinetDnatInfoDTO.setPostDstIp(policyDTO.getPostDstIp());
                    fortinetDnatInfoDTO.setPreDstIp(policyDTO.getPreDstIp());
                    restVipList.add(fortinetDnatInfoDTO);
                    fortinetDnatSpecialDTO.setRestVipList(restVipList);
                    policyDTO.setFortinetDnatSpecialDTO(fortinetDnatSpecialDTO);
                    policyDTO.setVipName(fortinetCreateName);
                }
            }
            mergeFortinetPolicyList.add(policyDTO);
        }
        return mergeFortinetPolicyList;
    }

    @Override
    public List<AutoRecommendTaskEntity> selectList(AutoRecommendTaskSearchVO vo) {
        AutoRecommendTaskEntity recommend = new AutoRecommendTaskEntity();
        BeanUtils.copyProperties(vo, recommend);
        return autoRecommendTaskManager.findList(recommend);
    }

}
