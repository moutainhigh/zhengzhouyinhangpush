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
 * @desc ?????????????????????????????????
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
     * ???nat mode
     */
    private static final String SNAT_MODE_STRING = "dynamicport";

    /**
     * ?????????????????????
     */
    private static final String HILLSTONE_MODELNUMBER_KEY = "HILLSTONE";

    /**
     * ?????????????????????
     */
    private static final String FORTINET_MODELNUMBER_KEY = "FORTINET";

    /**
     * ?????????????????????__VROUTER_IF_GROUP ?????????????????????
     */
    private static final String HILLSTONE_ZONE_VROUTER = "__VROUTER_IF_GROUP";

    /**
     * ?????????????????????__VSWITCH_IF_GROUP ?????????????????????
     */
    private static final String HILLSTONE_ZONE_VSWITCH = "__VSWITCH_IF_GROUP";

    /**
     * ?????????????????????
     */
    private static final String INTERFACE_NAME_SPLIT_BY = "_";

    /**
     * any???????????????
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
     * IP?????????????????????IP????????????????????????
     */
    private static final String ANY_IP = "114.114.114.114";

    /**
     * IP???????????????????????????
     */
    private static final String IPV4_MASK = "/32";

    /**
     * ????????????????????????????????????????????????IP
     */
    private static final String GET_DEFAULT_INTERFACE_IP = "0.0.0.0";

    /**
     * ????????????mip???????????????????????????
     */
    private static final String GET_DNAT_MIP_NAME_STR = "set dstaddr ";

    /**
     * ????????????????????????????????????????????????
     */
    private static final Integer GET_DEFAULT_INTERFACE_MASK = 0;

    /**
     * IP?????????????????????IP????????????????????????
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
                return new ReturnT(ReturnT.FAIL_CODE, "????????????");
            }
            AutoRecommendTaskEntity getByName = autoRecommendTaskMapper.getByName(vo.getTheme());
            if( null != getByName ){
                logger.error("????????????{} ?????????????????????", vo.getTheme());
                return new ReturnT(ReturnT.FAIL_CODE, "????????????" + vo.getTheme() + "????????????");
            }
            logger.info("------------????????????????????????????????????{}------------", JSONObject.toJSONString(vo));

            // ????????????
            if(vo.getIpType() == null ){
                vo.setIpType(IpTypeEnum.IPV4.getCode());
            }
            int rc;
            if(InputTypeEnum.SRC_INPUT_TYPE_IP.getCode().equals(vo.getSrcInputType())){
                rc = InputValueUtils.checkIp(vo.getSrcIp());
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                    return new ReturnT(ReturnT.FAIL_CODE, "???IP??????????????????");
                }

                //??????IP????????????????????????????????????????????????????????????
                if (rc == ReturnCode.INVALID_IP_RANGE && IpTypeEnum.IPV4.getCode().equals(vo.getIpType())) {
                    vo.setSrcIp(InputValueUtils.autoCorrect(vo.getSrcIp()));
                }
            }

            if(InputTypeEnum.DST_INPUT_TYPE_IP.getCode().equals(vo.getSrcInputType())){
                rc = InputValueUtils.checkIp(vo.getDstIp());
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                    return new ReturnT(ReturnT.FAIL_CODE, "??????IP??????????????????");
                }

                //??????IP????????????????????????????????????????????????????????????
                if (rc == ReturnCode.INVALID_IP_RANGE && IpTypeEnum.IPV4.getCode().equals(vo.getIpType())) {
                    vo.setDstIp(InputValueUtils.autoCorrect(vo.getDstIp()));
                }
            }

            if (StringUtils.isEmpty(vo.getSrcIp()) && StringUtils.isEmpty(vo.getDstIp()) && StringUtils.isEmpty(vo.getSrcAddressObjectName()) && StringUtils.isEmpty(vo.getDstAddressObjectName())) {
                String msg = "????????????IP??????????????????????????????????????????";
                logger.error(msg);
                return new ReturnT(ReturnT.FAIL_CODE, msg);
            }

            // ??????????????????????????????????????????????????????????????? updated on 2021-10-14
            // ????????????????????????????????????????????????updated on 2021-11-10
            /*List<AutoRecommendTaskEntity> allTask = autoRecommendTaskMapper.findCannotCreateTaskByConditions(vo.getAccessType());
            if(CollectionUtils.isNotEmpty(allTask)){
                String currentTaskHashcode = this.getHashcode(vo.getSrcIp(), vo.getDstIp(), vo.getServiceList());
                for (AutoRecommendTaskEntity historyTask : allTask ){
                    List<ServiceDTO> serviceList = ServiceDTOUtils.toList(historyTask.getServiceList());
                    String historyTaskHashcode = this.getHashcode(historyTask.getSrcIp(), historyTask.getDstIp(), serviceList);
                    if(StringUtils.equals(currentTaskHashcode, historyTaskHashcode)){
                        logger.error("????????????????????????????????????????????????????????????????????????????????????{}", JSON.toJSONString(historyTask));
                        String msg = "??????????????????????????????????????????:["+ historyTask.getTheme() +"]??????????????????";
                        return new ReturnT(ReturnT.FAIL_CODE, msg);
                    }
                }
            }*/

            Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap = new HashMap<>();
            Map<String, Set<AddAutoRecommendTaskDTO>> resultMap = new HashMap<>();
            Map<String, AutoRecommendConflictPolicyDTO> conflictPolicyMap = new HashMap<>();
            Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet= new HashSet<>();

            String streamId = com.abtnetworks.totems.common.utils.DateUtils.formatDate(new Date(), "yyyyMMddHHmmss") + "-" + IdGen.randomBase62(6);
            // ??????????????????
            record = this.buildAutoRecommendTask(vo, record, errorDetailDTOSet);
            record.setSrcIp(StringUtils.isBlank(vo.getSrcAddressObjectName()) ? vo.getSrcIp() : null);
            record.setDstIp(StringUtils.isEmpty(vo.getDstAddressObjectName()) ? vo.getDstIp() : null);
            autoRecommendTaskManager.insert(record);
            AutoRecommendTaskEntity finalRecord = record;
            autoTaskGenerateCommandlineExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, finalRecord.getTheme(), finalRecord.getTheme(), new Date())) {
                @Override
                protected void start() throws Exception {
                    if(InputTypeEnum.SRC_INPUT_TYPE_IP.getCode().equals(vo.getSrcInputType()) && InputTypeEnum.DST_INPUT_TYPE_IP.getCode().equals(vo.getDstInputType())){
                        // ??????????????????IP????????????????????????-IP ????????????????????????
                        generateAutoRecommendForIp(vo, policyInfoMap, resultMap, errorDetailDTOSet, finalRecord, conflictPolicyMap);
                    } else {
                        // ???????????????????????????????????????????????????-???????????? ????????????????????????
                        generateAutoRecommendForObject(vo, policyInfoMap, resultMap, errorDetailDTOSet, finalRecord, conflictPolicyMap);
                    }
                }
            });

            logger.info("------------??????????????????????????????------------");
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????????????????", e);
            record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            autoRecommendTaskMapper.updateByPrimaryKey(record);
            throw e;
        }
        return new ReturnT(record.getId());

    }

    /**
     * ??????????????????hashcode???
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
     * ??????serviceList???hashcode???
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
     * ??????????????????????????????????????????????????????Nat?????????Nat???
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
     * ??????????????????
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
     * ?????????Nat??????
     * @param taskDTO
     * @param samePartDTO
     */
    private RecommendTaskEntity createSnatTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO, AutoRecommendTaskEntity record){
        // ?????????NAT??????????????????
        NodeEntity nodeEntity = taskDTO.getNode();
        String dstIp = taskDTO.getDstIp();
        // ??????????????????nat????????????????????????IP???????????????????????????
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
        // ?????????????????????????????????????????????
        recommendTaskMapper.insert(recommendTaskEntity);
        return recommendTaskEntity;
    }

    /**
     *  ??????????????????
     * @param taskDTO
     * @return
     */
    private List<ServiceDTO> createPreServiceList(RecommendPolicyDTO taskDTO){
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        String postProtocol = taskDTO.getPostProtocol();
        // ??????nat????????????????????????????????????
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
     *  ??????????????????
     * @param taskDTO
     * @return
     */
    private List<ServiceDTO> createPostServiceList(RecommendPolicyDTO taskDTO){
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        String postProtocol = taskDTO.getPostProtocol();
        // ??????nat????????????????????????????????????
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
     * ????????????Nat??????
     * @param taskDTO
     * @param samePartDTO
     */
    private RecommendTaskEntity createDnatTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO, AutoRecommendTaskEntity record){
        // ????????????NAT????????????????????????
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
        // ?????????????????????????????????????????????
        recommendTaskMapper.insert(recommendTaskEntity);
        return recommendTaskEntity;
    }

    /**
     * ?????????????????????????????????????????????
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
            logger.info("???????????????{} ?????????UUID???{}???????????????", JSON.toJSONString(policy), device.getDeviceUuid());
            RuleMatchFlowVO ruleMatchFlow = ngRemoteService.getRuleMatchFlow(policy, device);
            logger.info("???????????????????????????:{}", JSON.toJSONString(ruleMatchFlow));
            if (ObjectUtils.isNotEmpty(ruleMatchFlow)) {
                List<PathFlowRO> deny = ruleMatchFlow.getDeny();
                if (CollectionUtils.isEmpty(deny)) {
                    logger.error("????????????????????????????????????????????????{} ?????????????????????????????????????????????", JSON.toJSONString(policyDTO));
                    this.setErrorMsg(nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "???????????????????????? [Security policy already exists]", policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getServiceList(), policyDTO.getStartTime(), policyDTO.getEndTime(),
                            AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode(),errorDetailDTOSet,ruleMatchFlow.getRuleList(), record.getAccessType(),nodeEntity.getUuid(),policyDTO.getSrcZone(),policyDTO.getDstZone(),null);
                    continue;
                }
                // ????????????????????????????????????
                this.batchInsert(ruleMatchFlow.getRuleList(),record.getId(), record.getTheme(), "0");

                BeanUtils.copyProperties(policyDTO, filterPolicyDTO);
                filterPolicyDTOList.add(filterPolicyDTO);
                // ?????????????????????????????????
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
                return new ReturnT(ReturnT.FAIL_CODE, "????????????????????????ip?????????????????????");
            }

            ResultRO<List<RoutingMatchFlowRO>> dataResultRO = whalePathAnalyzeClient.getRoutingMatchFlow(searchDTO);
            if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                return new ReturnT(ReturnT.FAIL_CODE, "?????????????????????????????????");
            }

            List<String> interfaceNameList = new ArrayList<>();
            List<RoutingMatchFlowRO> routData = dataResultRO.getData();
            // ???????????????????????? ?????????????????????
            for (RoutingMatchFlowRO routingMatchFlowRO : routData ){
                RoutingEntriesRO routingEntry = routingMatchFlowRO.getRoutingEntry();
                if ( null == routingEntry ){
                    logger.error("??????????????????????????????????????????????????????");
                    continue;
                }
                NextHopRO nextHop = routingEntry.getNextHop();
                if (null == nextHop ) {
                    logger.error("?????????????????????????????????????????????????????????");
                    continue;
                }
                if (StringUtils.isNotEmpty(nextHop.getInterfaceName())){
                    interfaceNameList.add(nextHop.getInterfaceName());
                }
            }
            logger.info("??????IP???{} ????????????????????????{}", content, interfaceNameList);
            return new ReturnT(interfaceNameList);
        } catch (Exception e ) {
            logger.error("????????????????????????????????????????????????", e);
            throw e;
        }
    }

    /**
     * ????????????????????????????????????IP??????????????????????????????IP??????????????????????????????
     * ??????IP???????????????????????????0.0.0.0/0??????????????????
     * ????????????????????????IPV4
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
                        logger.error("???????????????IP??????????????????:{}???????????????", content);
                        continue;
                    }
                    ResultRO<List<RoutingEntriesRO>> dataResultRO = whaleDevicePolicyClient.searchRout(searchDTO);
                    if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                        logger.error("????????????IP???{} ?????????????????????????????????????????????????????????????????????IP", content);
                        continue;
                    }

                    List<RoutingEntriesRO> routData = dataResultRO.getData();
                    // ???????????????????????? ?????????????????????
                    for (RoutingEntriesRO routingEntriesRO : routData ){
                        NextHopRO nextHop = routingEntriesRO.getNextHop();
                        if (null == nextHop ) {
                            logger.error("?????????????????????????????????????????????????????????");
                            continue;
                        }

                        String ip4Prefix = routingEntriesRO.getIp4Prefix();
                        Integer mask = routingEntriesRO.getMaskLength();
                        if(StringUtils.equals(configIp, ip4Prefix) && Integer.parseInt(configMask) == mask ){
                            if (StringUtils.isNotEmpty(nextHop.getInterfaceName())){
                                logger.info("??????IP???{} ????????????????????????{}", content, firstNameList.toString());
                                this.handleHillstoneZone(nextHop, firstNameList);
                                return new ReturnT(firstNameList);
                            }
                        } else {
                            if(StringUtils.equals(GET_DEFAULT_INTERFACE_IP, ip4Prefix) && GET_DEFAULT_INTERFACE_MASK.equals(mask) ){
                                logger.info("??????IP???{} ??????????????????????????????{}", content, firstNameList.toString());
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
                    return new ReturnT(ReturnT.FAIL_CODE, "????????????????????????????????????????????????");
                }

            } catch (Exception e ) {
                logger.error("????????????????????????????????????????????????", e);
                throw e;
            }
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
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
     * ??????SearchDTO
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
            //?????????IPv4?????????IPv6
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
                        logger.info("????????????????????????????????????{}...", autoRecommendTaskEntity.getTheme());
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        Integer natId = jsonObject.getInteger("id");
                        commandTaskEdiableMapper.deleteByTaskId(natId);
                        recommendTaskMapper.deleteByTaskId(natId);
                    }
                }
                logger.info("????????????????????????????????????:[]...", autoRecommendTaskEntity.getTheme());
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
                                // ??????????????????
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
            // ???Nat????????????????????????
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
            // ??????Nat????????????????????????
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
            // ???????????????????????????
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
        //??????????????????????????????????????????????????????nat?????????taskId
        if(ObjectUtils.isNotEmpty(vo.getTaskIdList())){
            isDevicePush = false;
        }
        Set<Integer> deleteIdSet = new HashSet<>();
        Map<String, List<CommandTaskEditableEntity>> taskMap = new HashMap<>();
        for (Integer id : vo.getIdList()) {
            List<RecommendTaskEntity> entityList = new ArrayList<>();
            AutoRecommendTaskEntity autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(id);
            if (autoTaskEntity == null) {
                logger.warn("??????ID???{} ????????????????????????????????????", id);
                continue;
            }

            if (isDevicePush) {
                // ???????????????????????????????????????????????????????????????????????????????????????????????????
                if(autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode()) ||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSH_WAITING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSHING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSH_SUCCESS.getCode())) {
                    logger.error("?????????{} ????????????{}??????????????????!", autoTaskEntity.getTheme(), AutoRecommendStatusEnum.getDescByCode(autoTaskEntity.getStatus()));
                    continue;
                }
            }else {
                // ???????????????????????????????????????????????????????????????
                if(autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSH_WAITING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.PUSHING.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.SECURITY_POLICY_HAS_EXIST.getCode())||
                        autoTaskEntity.getStatus().equals(AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode())) {
                    logger.error("?????????{} ????????????{}??????????????????!", autoTaskEntity.getTheme(), AutoRecommendStatusEnum.getDescByCode(autoTaskEntity.getStatus()));
                    continue;
                }
            }
            autoEntityList.add(autoTaskEntity);
            try {
                // ??????????????????????????????????????????????????????
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
                    logger.info("???????????????{}??????????????????????????????????????????????????????", autoTaskEntity.getTheme());
                    continue;
                }

                // ????????????????????????
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
                            // ?????????-1
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
                                    if (StringUtils.isNotBlank(command) && command.startsWith("?????????????????????????????????")) {
                                        errMsg.append(String.format("[%s]?????????????????????????????????????????????????????????", editTaskEntity.getTheme()));
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

                                String message = String.format("?????????%s ?????????%s", taskEntity.getTheme(), vo.getIsRevert() ? "??????" : "??????");
                                logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

                                // ???????????????
                                pushService.pushCommand(taskDTO);
                                // ?????????-1
                                latch.countDown();
                            }
                        });

                    }
                } catch (Exception e) {
                    logger.error("??????????????????????????????????????????????????????", e);
                    latch.countDown();
                    throw e;
                }

                try {
                    latch.await();
                } catch (Exception e) {
                    logger.error("????????????????????????????????????", e);
                }
                // ??????????????????
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
                /*//???????????????????????????
                pushAutoTaskEmailService.startAutoRecommendPushEmail(taskEditableEntityList, autoTaskEntity);*/


            } catch (Exception e) {
                logger.error("????????????????????????????????????????????????", e);
                autoTaskEntity.setStatus(AutoRecommendStatusEnum.PUSH_FAIL.getCode());
                autoRecommendTaskMapper.updateByPrimaryKey(autoTaskEntity);
                throw e;
            }

        }
        // ??????????????????????????????
        if(taskDelete){
            for(Integer id : deleteIdSet){
                AutoRecommendTaskEntity autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(id);
                autoTaskEntity.setStatus(AutoRecommendStatusEnum.PUSH_FAIL.getCode());
                autoRecommendTaskMapper.updateByPrimaryKey(autoTaskEntity);
            }
            return new ReturnT(ReturnT.FAIL_CODE, "????????????????????????????????????????????????");
        }

        //???????????????????????????-?????????????????????????????????
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
                logger.info("??????ID???{} ????????????????????????????????????", vo.getId());
                return new ReturnT(allResultDTO);
            }
        } else if(ObjectUtils.isNotEmpty(vo.getId())){
            autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(vo.getId());
            if(autoTaskEntity == null ){
                logger.info("??????ID???{} ????????????????????????????????????", vo.getId());
                return new ReturnT(allResultDTO);
            }
        } else {
            return new ReturnT(ReturnT.FAIL_CODE,"??????????????????");
        }

        String url = projectIp + "/appmodules/views/autoOpenTaskDetail/index.html?access_token=" + token + "&flag=zte&theme=" + autoTaskEntity.getTheme();
        allResultDTO.setDetailPageUrl(url);

        if(StringUtils.isNotBlank(autoTaskEntity.getErrorMsg())){
            allResultDTO.setErrorDetailDTOList(JSONArray.parseArray(autoTaskEntity.getErrorMsg(), AutoRecommendErrorDetailDTO.class));
        }

        // ?????? ?????????/????????? ??????????????????
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
                    // ??????????????????
                    mappingMap.put(data, recommendTaskEntity);
                    allCmdList.add(data);
                }
            }
        }

        if(CollectionUtils.isEmpty(allCmdList)){
            // TODO ?????????????????????????????????????????????????????????????????????
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
            // ??????????????????????????????????????????????????????????????????????????????
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
            // ???????????????????????????????????????
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

                // ??????????????????
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

                // ??????????????????
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

        // ?????????????????????????????????????????????????????????
        this.setWaitCreateOrderStatusList(dataMap, orderStatusDTOList);
        allResultDTO.setDemandStatus(autoTaskEntity.getStatus());
        allResultDTO.setDataMap(dataMap);
        allResultDTO.setOrderStatusDTOList(orderStatusDTOList);

        return new ReturnT(allResultDTO);
    }


    /**
     * ????????????Nat?????????????????????Mip??????
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
                        // ???????????????????????????mip?????????vip??????
                        return ruleName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * ?????? ?????????/????????? ?????????????????????
     * @param errorDetailDTOList
     * @param orderStatusDTOList
     */
    private void setErrorOrderStatusList(List<AutoRecommendErrorDetailDTO> errorDetailDTOList, List<AutoRecommendOrderStatusDTO> orderStatusDTOList){
        // ???????????????
        AutoRecommendOrderStatusDTO errorOrderStatusDTO = new AutoRecommendOrderStatusDTO();
        AutoRecommendOrderStatusDTO existOrderStatusDTO = new AutoRecommendOrderStatusDTO();
        Set<String> errorSrcIpList = new HashSet<>();
        Set<String> errorDstIpList = new HashSet<>();
        Set<List<ServiceDTO>> errorServiceList = new HashSet<>();
        Set<NodeEntity> errorEntityList = new HashSet<>();

        // ???????????????
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
     * ?????? ?????????/????????? ?????????????????????
     * @param errorDetailDTOList
     * @param securityOrderStatusDTOList
     * @param natOrderStatusDTOList
     */
    private void setErrorOrderStatusListNew(List<AutoRecommendErrorDetailDTO> errorDetailDTOList, List<AutoRecommendOrderStatusNewDTO> securityOrderStatusDTOList,
                                            List<AutoRecommendOrderStatusNewDTO> natOrderStatusDTOList){
        // ???????????????
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

            // TODO status?????????????????????????????????????????????
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

        // ?????????????????????
        if(CollectionUtils.isNotEmpty(securityOrderDTOSet)){
            securityOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_HAS_EXIST.getCode());
            securityOrderStatusDTO.setOrderDTOSet(securityOrderDTOSet);
            securityOrderStatusDTOList.add(securityOrderStatusDTO);
        }

        // NAT???????????????
        if(CollectionUtils.isNotEmpty(natOrderDTOSet)) {
            natOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_HAS_EXIST.getCode());
            natOrderStatusDTO.setOrderDTOSet(natOrderDTOSet);
            natOrderStatusDTOList.add(natOrderStatusDTO);
        }
        // ?????????????????????
        if(CollectionUtils.isNotEmpty(errorOrderDTOSet)) {
            errorOrderStatusDTO.setOrderStatus(AutoTaskOrderStatusEnum.POLICY_GENERATE_ERROR.getCode());
            errorOrderStatusDTO.setOrderDTOSet(errorOrderDTOSet);
            securityOrderStatusDTOList.add(errorOrderStatusDTO);
        }
    }

    /**
     * ????????????????????????????????????
     * @param dataMap
     * @param orderStatusDTOList
     */
    private void setWaitCreateOrderStatusList(Map<String, AutoRecommendResultDTO> dataMap, List<AutoRecommendOrderStatusDTO> orderStatusDTOList){
        // ?????????????????????
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
     * ?????????????????????
     * @param errorDetailDTO
     * @param srcIpList
     * @param dstIpList
     * @param serviceList
     * @param entityList
     */
    private void setOrderStatusData(AutoRecommendErrorDetailDTO errorDetailDTO, Set<String> srcIpList, Set<String> dstIpList,
                                    Set<List<ServiceDTO>> serviceList, Set<NodeEntity> entityList){
        if(StringUtils.containsIgnoreCase(errorDetailDTO.getErrorMsg(), "Nat?????????????????????")){
            logger.info("Nat??????????????????????????????????????????????????????????????????");
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
     * ?????????????????????
     * @param errorDetailDTO
     * @param srcIpList
     * @param dstIpList
     * @param serviceList
     * @param entityList
     */
    private void setOrderStatusDataNew(AutoRecommendErrorDetailDTO errorDetailDTO, Set<String> srcIpList, Set<String> dstIpList,
                                       Set<List<ServiceDTO>> serviceList, Set<NodeEntity> entityList){
        if(StringUtils.containsIgnoreCase(errorDetailDTO.getErrorMsg(), "Nat?????????????????????")){
            logger.info("Nat??????????????????????????????????????????????????????????????????");
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
     * ???????????????????????????????????????????????????
     *
     * @param vo
     * @param policyInfoMap
     * @param resultMap
     * @return
     */
    private void checkProtectNetworkConfig(AutoRecommendTaskVO vo, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                           Map<String, Set<AddAutoRecommendTaskDTO>> resultMap,
                                           Set<AutoRecommendErrorDetailDTO> errorDetailDTOSet) throws Exception{
        // 1.?????????????????????????????????????????????
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
                        this.setErrorMsg("?????????????????????0????????? [Creating policy with subnet mask 0 is prohibited]", srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        return;
                    }

                    // ??????????????????
                    List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(srcIp, vo);
                    if (CollectionUtils.isEmpty(list)) {
                        if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                            String msg = "???IP:" + srcIp + "????????????????????????????????????????????????????????? [Source ip is not in protect network table,please check the protect network table]";
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
                            String msg = configEntity.getDeviceIp() + "???????????????????????????????????????????????? [Source ip can't find relevant firewall in protect network table,please check the protect network table]";
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

                // ??????????????????????????????nat?????????????????????????????????????????????Nat??????
                for (String srcIp : srcIpList) {
                    if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                        List<ProtectNetworkNatMappingEntity> snatMappingEntityList = protectNetworkNatMappingMapper.selectByNatType(PushNatTypeEnum.NAT_TYPE_S.getCode());
                        // ??????????????????????????????????????????????????????
                        // ????????????:??????IP?????????nat????????????????????????IP???????????????????????????nat??????
                        for (ProtectNetworkNatMappingEntity natMappingEntity : snatMappingEntityList) {
                            if(IpUtils.checkIpRange(srcIp, natMappingEntity.getOutsideIp())){
                                logger.info("??????IP:{}???????????????Nat????????????", srcIp);
                                matchSNatMapping = true;

                                Set<AddAutoRecommendTaskDTO> taskDTOSet= new HashSet<>();
                                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.selectByPrimaryKey(natMappingEntity.getConfigId());
                                if( null == configEntity ){
                                    logger.error("????????????id:{} ??????????????????????????????", natMappingEntity.getConfigId());
                                    continue;
                                }

                                // ????????????????????????
                                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                                if (nodeEntity == null) {
                                    String msg = "?????????:" + configEntity.getDeviceIp() + "??????????????????????????????????????? [Firewall offline,please check the protect network table]";
                                    srcErrorMsg.append(msg);
                                    this.setErrorMsg(msg, srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                            AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
                                    continue;
                                }

                                srcFindNode = true;
                                // ?????????nat??????????????????
                                this.setSnatData(natMappingEntity, srcIp,vo, nodeEntity,policyInfoMap,nodeEntity.getUuid(),taskDTOSet, PushNatTypeEnum.NAT_TYPE_S.getCode());

                                // ?????????nat?????????????????????????????????
                                if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                                    if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                        logger.info("?????????????????????????????????????????????????????????");
                                        Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                        Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                        policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                        isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                        continue;

                                    }
                                    logger.info("?????????????????????????????????????????????????????????????????????????????????nat");
                                    continue;
                                }

                                // ?????????????????????
                                if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                    logger.info("????????????????????????????????????????????????????????????IP????????????????????????????????????????????????");
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
                        logger.error("??????IP:{}?????????????????????Nat????????????????????????????????????????????????", srcIp);
                        String errorMsg = "??????IP:"+ srcIp + "????????????????????????Nat???????????????????????????????????????????????? [IP:"+ srcIp +" can't find relevant source Nat mapping in project network table,please check the protect network table]";
                        this.setErrorMsg(errorMsg,srcIp, vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        continue;
                    }*/
                }
            }

            // ????????????????????????????????????nat?????????nat??????????????????????????????
            if(matchSNatMapping && PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())){
                srcErrorDetailList.clear();
            } else {
                errorDetailDTOSet.addAll(srcErrorDetailList);
            }

            // ??????????????????????????????????????????????????????IP???????????????????????????????????????????????????
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
                        this.setErrorMsg("?????????????????????0????????? [Prohibit create policy with subnet mask 0]", vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                                null, vo.getAccessType(),null,null,null,null);
                        return;
                    }

                    // ????????????????????????????????????????????????
                    List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(dstIp, vo);
                    if (CollectionUtils.isEmpty(list)) {
                        if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                            String msg = "??????IP:" + dstIp + "????????????????????????????????????????????????????????? [Destination ip is not in the protect network table,please check the protect network table]";
                            srcErrorMsg.append(msg);

                            AutoRecommendErrorDetailDTO srcErrorDetailDTO = this.setSrcErrorMsg(msg, vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                    vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),vo.getAccessType());
                            dstErrorDetailList.add(srcErrorDetailDTO);

                            continue;
                        } else {
                            String[] errorIP = srcAndDstAllError(errorMsgMap, vo.getSrcIp(), dstIp);
                            if(ObjectUtils.isNotEmpty(errorIP) && errorIP.length == 2){
                                String msg = "???IP???"+ errorIP[0] +"??????IP:" + errorIP[1] + "???????????????????????????????????????????????????????????? [Source ip:"+errorIP[0]+" and destination ip:"+ errorIP[1]+" are not in the protect network table,please check the protect network table]";
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
                            String msg = "?????????:" + configEntity.getDeviceIp() + "??????????????????????????????????????? [Firewall offline,please check the protect network table]";
                            dstErrorMsg.append(msg);
                            this.setErrorMsg(msg, vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(), null,null,null,null);
                            continue;
                        }
                        String deviceUuid = nodeEntity.getUuid();
                        securityExistMap.put(deviceUuid, true);
                        // ????????????????????????IP?????????Nat??????????????????????????????????????????
                        dstIp = StringUtils.isBlank(configEntity.getConvertRangeIp()) ? dstIp : configEntity.getConvertRangeIp();
                        String[] dstIps = dstIp.split(",");
                        this.setDstSecurityData(vo,nodeEntity,  policyInfoMap, deviceUuid, taskDTOSet, dstIps);
                    }
                }

                // ???????????????????????????????????????nat???????????????Nat
                // ????????????????????????IP?????????nat????????????????????????IP???????????????????????????nat??????
                for (String dstIp : dstIpList) {
                    // ??????????????????????????????nat????????????
                    if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                        List<ProtectNetworkNatMappingEntity> dnatMappingEntityList = protectNetworkNatMappingMapper.selectByNatType(PushNatTypeEnum.NAT_TYPE_D.getCode());
                        for (ProtectNetworkNatMappingEntity natMappingEntity : dnatMappingEntityList) {
                            if(!StringUtils.equalsAnyIgnoreCase(natMappingEntity.getNatType(), PushNatTypeEnum.NAT_TYPE_D.getCode())){
                                continue;
                            }
                            // ?????????IP???????????????????????????????????????????????????????????????????????????????????????????????????
                            if(IpUtils.checkIpRange(dstIp, natMappingEntity.getInsideIp()) &&
                                    isContainsPort(natMappingEntity.getInsideProtocol(), natMappingEntity.getInsidePorts(), vo.getServiceList())){

                                logger.info("??????IP:{}????????????Nat????????????", dstIp);
                                currentMatchDNatMapping = true;
                                allMatchDNatMapping = true;

                                Set<AddAutoRecommendTaskDTO> taskDTOSet= new HashSet<>();
                                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.selectByPrimaryKey(natMappingEntity.getConfigId());
                                if( null == configEntity ){
                                    logger.error("????????????id:{} ??????????????????????????????", natMappingEntity.getConfigId());
                                    continue;
                                }
                                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(configEntity.getDeviceUuid());
                                if (null == nodeEntity) {
                                    String msg = "?????????:" + configEntity.getDeviceIp() + "??????????????????????????????????????? [Firewall offline,please check the protect network table]";
                                    dstErrorMsg.append(msg);
                                    this.setErrorMsg(msg, vo.getSrcIp(), dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                            AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(), null,null,null,null);
                                    continue;
                                }

                                AddAutoRecommendTaskDTO taskDTO = new AddAutoRecommendTaskDTO();

                                taskDTO.setPreProtocol(natMappingEntity.getOutsideProtocol());
                                taskDTO.setPostProtocol(natMappingEntity.getInsideProtocol());
                                // ????????????????????????nat????????????
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
                                // ????????????????????????nat????????????
                                // ???????????????H3C???????????????IP???????????????????????????IP??????????????????
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

                                // ????????????Nat?????????????????????????????????
                                this.setData(taskDTO, vo,nodeEntity,  policyInfoMap, nodeEntity.getUuid(), taskDTOSet);
                                // ???????????????????????????????????????????????????????????????
                                if(StringUtils.isNotBlank(natMappingEntity.getInsideProtocol())){
                                    List<ServiceDTO> serviceList = new ArrayList<>();
                                    ServiceDTO serviceDTO = new ServiceDTO();
                                    String protocol = ProtocolUtils.getProtocolNumberByName(natMappingEntity.getInsideProtocol());
                                    serviceDTO.setProtocol(protocol);
                                    serviceDTO.setDstPorts(natMappingEntity.getOutsidePorts());
                                    serviceList.add(serviceDTO);
                                    taskDTO.setServiceList(serviceList);
                                }

                                // ???????????????????????????
                                // ???????????????
                                if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                                    if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                        logger.info("?????????????????????????????????????????????????????????");
                                        Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                        Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                        policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                        isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                        continue;
                                    }
                                    logger.info("?????????????????????????????????????????????????????????????????????????????????nat");
                                    continue;
                                }

                                // ???????????????????????????
                                // ?????????????????????
                                if(securityExistMap.containsKey(nodeEntity.getUuid()) && !isCleanSecurityPolicyMap.containsKey(nodeEntity.getUuid())){
                                    logger.info("????????????????????????????????????????????????????????????IP????????????????????????????????????????????????");
                                    Set<AddAutoRecommendTaskDTO> addAutoRecommendTaskDTOSet = policyInfoMap.get(nodeEntity.getUuid());
                                    Set<AddAutoRecommendTaskDTO> newTaskDTOSet = addAutoRecommendTaskDTOSet.stream().filter(taskDTOTemp -> !StringUtils.equalsAnyIgnoreCase(taskDTOTemp.getNatType(), PushNatTypeEnum.NAT_TYPE_N.getCode())).collect(Collectors.toSet());
                                    policyInfoMap.put(nodeEntity.getUuid(), newTaskDTOSet);
                                    isCleanSecurityPolicyMap.put(nodeEntity.getUuid(), true);
                                }

                                AddAutoRecommendTaskDTO securityTaskDTO = new AddAutoRecommendTaskDTO();
                                BeanUtils.copyProperties(taskDTO, securityTaskDTO);
                                securityTaskDTO.setNatType(PushNatTypeEnum.NAT_TYPE_N.getCode());
                                // ????????????????????????
                                this.setData(securityTaskDTO, vo,nodeEntity,  policyInfoMap, nodeEntity.getUuid(), taskDTOSet);

                            }
                        }
                    }

                    /*if (!currentMatchDNatMapping && PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                        logger.error("??????IP:{}???????????????????????????Nat????????????????????????????????????????????????", dstIp);
                        String errorMsg = "??????IP:"+ dstIp + "?????????????????????Nat???????????????????????????????????????????????? [IP:"+ dstIp + " cant find relevant nat maping information please check the protect network table]";
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

            // ?????????????????????????????????????????????????????????IP?????????????????????????????????????????????????????????
            // ???????????????H3C?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            /*if(PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType()) && !allMatchDNatMapping){
                policyInfoMap.clear();
                return;
            }*/
        }

        if(!srcFindNode && !dstFindNode){
            policyInfoMap.clear();
            logger.error("????????????IP??????????????????????????????????????????????????????;");
            return;
        }


        // 2.????????????????????????
        if(srcFindNode || dstFindNode){
            this.getInterfaceAndZone(policyInfoMap, resultMap, vo, errorDetailDTOSet);
        }
    }

    /**
     * ?????????Nat????????????
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

        // ????????????????????????Nat?????????IP??????????????????IP???????????????????????????????????????????????????????????????
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
        //??????????????????nat??????
        taskDTO.setNatType(natType);
        taskDTO.setDeviceUuid(deviceUuid);

        if( policyInfoMap.containsKey(deviceUuid) ){
            // ????????????????????????????????????????????????????????????????????????????????????
            Set<AddAutoRecommendTaskDTO> existTaskDTOList = policyInfoMap.get(deviceUuid);
            existTaskDTOList.add(taskDTO);
            policyInfoMap.put(deviceUuid, existTaskDTOList);
        } else {
            taskDTOSet.add(taskDTO);
            policyInfoMap.put(deviceUuid, taskDTOSet);
        }
    }

    /**
     * ???????????????????????????
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
     * ????????????????????????
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
            // ????????????????????????
            this.setData(taskDTO, vo,nodeEntity,  policyInfoMap, deviceUuid, taskDTOSet);
        }
    }

    /**
     * ?????????IP???????????????
     * @param srcIps
     * @param vo
     * @param nodeEntity
     * @param policyInfoMap
     * @param deviceUuid
     * @param taskDTOSet
     */
    private void setSrcSecurityData(String[] srcIps, AutoRecommendTaskVO vo, NodeEntity nodeEntity, Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap,
                                    String deviceUuid, Set<AddAutoRecommendTaskDTO> taskDTOSet){
        // ???nat??????????????????????????????????????????
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
            //???????????????????????????
            taskDTO.setNatType(PushNatTypeEnum.NAT_TYPE_N.getCode());

            if( policyInfoMap.containsKey(deviceUuid) ){
                // ????????????????????????????????????????????????????????????????????????????????????
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
     * ??????/32??????
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
     * ????????????????????????
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
     * ?????????IP????????????
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
     * ????????????????????????????????????
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
     * ????????????????????????
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
     * ????????????????????????
     * @param policyInfoMap
     * @param resultMap
     * @return
     */
    private void getInterfaceAndZone(Map<String, Set<AddAutoRecommendTaskDTO>> policyInfoMap, Map<String, Set<AddAutoRecommendTaskDTO>> resultMap,
                                     AutoRecommendTaskVO vo, Set<AutoRecommendErrorDetailDTO> errorDetailDTOList) throws Exception{
        if (ObjectUtils.isEmpty(policyInfoMap)) {
            logger.info("?????????????????????????????????");
            return;
        }
        for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> entry : policyInfoMap.entrySet()) {
            Set<AddAutoRecommendTaskDTO> resultTaskSet = new HashSet<>();
            String deviceUuid = entry.getKey();

            boolean isVsys = false;
            String vsysName = "";
            DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
            if( null == device || CollectionUtils.isEmpty(device.getData())){
                String msg = "??????whale?????????????????????????????? [No device can be queried based on the IP address]";
                logger.error(msg + ",??????uuid:{}", deviceUuid);
                this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),deviceUuid,null,null,null);
                continue;
            }
            DeviceDataRO deviceData = device.getData().get(0);
            if (deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                vsysName = deviceData.getVsysName();
            }

            //??????????????????????????????????????????
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);

            ResultRO<List<ImportedRoutingTableRO>> resultRO = whaleDevicePolicyClient.getImportRoutTable(deviceUuid);
            if (resultRO == null || resultRO.getData() == null || resultRO.getData().isEmpty()) {
                logger.error("????????????uuid???Whale?????????????????????????????????????????????,deviceUuid:{}", deviceUuid);
                String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "????????????????????????????????? [No default router information can be queried based on the IP address]";
                this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
                continue;
            }
            String defaultOutItfUuid = this.getDefaultOutItf(resultRO.getData());
            if (StringUtils.isEmpty(defaultOutItfUuid)) {
                logger.error("????????????????????????????????????");
                String msg = nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "???????????????????????? [Default route is not presented in the routing table]";
                this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
                continue;
            }

            Set<AddAutoRecommendTaskDTO> policyInfoSet = entry.getValue();
            if (CollectionUtils.isEmpty(policyInfoSet)) {
                logger.info("??????uuid??????{} ?????????????????????????????????", deviceUuid);
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
     * ?????????????????????????????????
     * ??????IP??????????????????
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
            // ???????????????Ip???any?????????????????????any
            if(PushAccessTypeEnum.INSIDE_TO_INSIDE.getCode().equals(policyInfo.getAccessType())){
                policyInfoIp = PolicyConstants.POLICY_STR_VALUE_ANY;
            } else {
                policyInfoIp = ANY_IP;
            }
        }
        if (StringUtils.isNotEmpty(policyInfoIp)) {
            String[] policyInfoIpList = policyInfoIp.split(",");
            for (String policyIp : policyInfoIpList) {
                // ??????????????????????????????????????????????????????????????????
                ProtectNetworkConfigEntity configEntity = protectNetworkConfigMapper.selectByDeviceUuid(nodeEntity.getUuid());
                if(StringUtils.equals(SameZoneFlagFlagEnum.SAME_ZONE_RECOMMEND_N.getCode(), configEntity.getSameZoneFlag())
                        && IpAddress.isSameIp(rangeIp, policyIp, IpTypeEnum.IPV4.getCode())){
                    logger.info(nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "??????IP:{}, ??????IP???{} ??????????????????????????????", srcFlag == true ? rangeIp : policyIp, srcFlag == true ? policyIp : rangeIp);

                    if(rangeIp.endsWith(IPV4_MASK)){
                        rangeIp = rangeIp.substring(0, rangeIp.lastIndexOf("/32"));
                    }
                    if(policyIp.endsWith(IPV4_MASK)){
                        policyIp = policyIp.substring(0, policyIp.lastIndexOf("/32"));
                    }
                    String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "??????IP???"+ (srcFlag == true ? rangeIp : policyIp) +"????????????IP???"+ (srcFlag == true ? policyIp : rangeIp) +"????????? [Source ip and destination ip are the same]";
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
     * ????????????uuid???????????????????????????uuid????????????????????????
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

        // ????????????????????????
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
            logger.error("??????IP:{} ???????????????????????????????????????{}", srcIp, srcReturnT.getMsg());
            String msg = nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "?????????IP:" + srcIp + "????????????????????? [According to the IP: "+ srcIp+ " failed to obtain the source interface]";
            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
            return;
        }

        srcInterfaceNameSet.addAll(srcReturnT.getData());

        // ?????????IP?????????????????????????????????????????????????????????????????????
        srcInterfaceNameSet = this.checkInterface(nodeEntity,srcInterfaceNameSet, srcIp, dstIp, vo, errorDetailDTOList);

        ReturnT<List<String>> dstReturnT;
        if(StringUtils.equalsAnyIgnoreCase(dstIp, PolicyConstants.POLICY_STR_VALUE_ANY) && StringUtils.isNotBlank(anyIpFindZoneInfo)){
//            dstIp = anyIpFindZoneInfo;
            dstReturnT = this.remoteGetRoutTableRuleListByInside2Inside(nodeEntity.getUuid(), defaultOutItfUuid, anyIpFindZoneInfo);
        } else {
            // 1.??????????????????????????????????????????IP?????????????????????
            // 2.??????????????????????????????????????????????????????????????????????????????IP?????????????????????
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
            String msg = nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "?????????IP:" + dstIp + "???????????????????????? [According to the IP: "+ dstIp +" failed to obtain the source interface]";
            logger.error(msg);
            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),null,null,null);
            return;
        }
        dstInterfaceNameSet.addAll(dstReturnT.getData());

        // ????????????IP?????????????????????????????????????????????????????????????????????
        dstInterfaceNameSet = this.checkInterface(nodeEntity,dstInterfaceNameSet, srcIp, dstIp, vo, errorDetailDTOList);

        // ?????????????????????
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

                // ???????????????????????????
                String devIfAlias = "";
                if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "CISCO")) {
                    List<DeviceInterfaceRO> deviceInterfaces = deviceData.getDeviceInterfaces();
                    logger.info("??????????????????????????????{}" + JSONObject.toJSONString(deviceInterfaces));
                    for (DeviceInterfaceRO deviceInterfaceRO : deviceInterfaces ){
                        if(StringUtils.equals(deviceInterfaceRO.getName(), srcDevIf)){
                            devIfAlias = deviceInterfaceRO.getAlias();
                            break;
                        }
                    }
                    // ??????????????????
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
                    // ?????????????????????????????????????????????????????????????????????

                    if (srcZoneName == null || StringUtils.isEmpty(srcZoneName)) {
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY) || StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "CISCO")){
                            srcZoneName = srcDevIf;
                        }else {
                            String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "????????????????????? [Get source zone failed]";
                            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,null ,null);
                            continue;
                        }

                    }

                    dstZoneName = whalePolicyClient.getZoneInfoByDeviceUuidAndInterfaceName(nodeEntity.getUuid(), dstDevIf);
                    // ?????????????????????????????????????????????????????????????????????
                    if (dstZoneName == null || StringUtils.isEmpty(dstZoneName)) {
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY) || StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), "CISCO")){
                            dstZoneName = dstDevIf;
                        } else {
                            String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "???????????????????????? [Get destination zone failed]";
                            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName ,null);
                            continue;
                        }
                    }

                    // ????????????????????????????????????any????????????????????????
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

                    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    /*
                    if(StringUtils.equals(srcZoneName, dstZoneName)){
                        logger.info(nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "????????????{}????????????:{} ????????????????????????????????????", srcZoneName, dstZoneName);
                        String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "????????????"+ srcZoneName +"??????????????????"+ dstZoneName +"????????? [Source zone and destination zone are the same]";
                        this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName,null);
                        continue;
                    }*/
                    // ??????????????????????????????????????????
                    if(StringUtils.equals(SameZoneFlagFlagEnum.SAME_ZONE_RECOMMEND_N.getCode(), configEntity.getSameZoneFlag())){
                        if(StringUtils.equals(srcZoneName, dstZoneName)){
                            String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "????????????"+ srcZoneName +"??????????????????"+ dstZoneName +"????????? [Source zone and destination zone are the same]";
                            this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName,null);
                            continue;
                        }
                        // ??????????????????????????????
                    }

                    // ???????????????????????????????????????
                    // ????????????????????????
                    boolean generatePolicy = true;
                    List<PushZoneLimitConfigEntity> byDeviceUuid = pushZoneLimitConfigMapper.findByDeviceUuid(nodeEntity.getUuid());
                    if(CollectionUtils.isNotEmpty(byDeviceUuid)){
                        for(PushZoneLimitConfigEntity zoneLimitConfigEntity : byDeviceUuid ){
                            if(StringUtils.equals(srcZoneName, zoneLimitConfigEntity.getSrcZone()) && StringUtils.equals(dstZoneName, zoneLimitConfigEntity.getDstZone())){
                                logger.info("???????????????{}????????????{}???????????????{}???????????????????????????????????????????????????????????????????????????????????????????????????",nodeEntity.getIp(), srcZoneName, dstZoneName);
                                String msg = nodeEntity.getDeviceName() +"???" + nodeEntity.getIp() + "?????????????????????"+ srcZoneName +"??????????????????"+ dstZoneName +"???????????????????????? [Source zone to destination zone is not allowed]";
                                this.setErrorMsg(msg, srcIp, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                        AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOList,null, vo.getAccessType(),nodeEntity.getUuid(),srcZoneName,dstZoneName,null);
                                generatePolicy = false;
                                break;
                            }
                        }
                    }
                    // ???????????????????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????
     *
     * ???????????????????????????????????????????????????????????????????????????????????????????????????"_"??????????????????????????????????????????????????????????????????????????????
     *           ?????????????????????????????????????????????????????????
     * @param nodeEntity
     * @param interfaceNameSet
     * @param srcIp
     * @param dstIp
     * @param vo
     * @param errorDetailDTOList
     */
    private Set<String> checkInterface(NodeEntity nodeEntity, Set<String> interfaceNameSet, String srcIp, String dstIp, AutoRecommendTaskVO vo,
                                       Set<AutoRecommendErrorDetailDTO> errorDetailDTOList){
        // ????????????????????????
        Set<String> errorInterfaceNameSet = new HashSet<>();
        // ??????????????????????????????
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
                                        logger.error("????????????????????????{}, ????????????????????????{} ?????????????????????????????????", interfaceName, subInterfaceName);
                                        String msg = nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "????????????????????????"+ subInterfaceName +" [Can't find relevant interface in firewall]";
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
        // ??????????????????????????????
        if(CollectionUtils.isNotEmpty(errorInterfaceNameSet)){
            interfaceNameSet.removeAll(errorInterfaceNameSet);
        }
        // ????????????????????????????????????
        if(CollectionUtils.isNotEmpty(rightInterfaceNameSet)){
            interfaceNameSet.addAll(rightInterfaceNameSet);
        }
        return interfaceNameSet;
    }

    /**
     * ?????????????????????????????????????????????
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
     * ????????????????????????
     *
     * @param taskDTO
     * @param samePartDTO
     * @return
     */
    public int addAutoRecommendTask(RecommendPolicyDTO taskDTO, AutoRecommendTaskSamePartDTO samePartDTO, Integer taskId, PolicyEnum type, String vipName,
                                    AutoRecommendSpecialDTO autoRecommendSpecialDTO) {
        //????????????
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
        // ??????????????????nat????????????????????????IP???????????????????????????
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
        // ???????????????????????????????????????????????????????????????
        cmdDTO.setAutoRecommendSpecialDTO(autoRecommendSpecialDTO);
        cmdTaskService.getRuleMatchFlow2Generate(cmdDTO, samePartDTO.getUserInfoDTO());
        return entity.getId();
    }

    /**
     * ????????????????????????DTO??????
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
            logger.info("????????????????????????");
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTOList.add(serviceDTO);
            record.setServiceList(JSONObject.toJSONString(serviceDTOList));
        } else {
            logger.info("???????????????????????????:{}", JSONObject.toJSONString(vo.getServiceList()));
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
     * ???????????????????????????????????????
     *
     * @param routingTableROList
     * @return
     */
    private String getDefaultOutItf(List<ImportedRoutingTableRO> routingTableROList) {
        for (ImportedRoutingTableRO routingTableRO : routingTableROList) {
            List<RoutingtableRO> baseRoutingTables = routingTableRO.getBaseRoutingTables();
            if (CollectionUtils.isEmpty(baseRoutingTables)) {
                logger.error("?????????????????????????????????????????????");
                continue;
            }
            for (RoutingtableRO baseRoutingTable : baseRoutingTables) {
                if (ObjectUtils.isEmpty(baseRoutingTable.getIsDefault())) {
                    logger.error("????????????????????????????????????");
                    continue;
                }
                if (baseRoutingTable.getIsDefault()) {
                    logger.info("??????????????????????????????:{}", baseRoutingTable.getName());
                    return baseRoutingTable.getUuid();
                }
            }
        }
        return null;
    }

    /**
     * ???????????????????????????
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
     * ?????????????????????DTO?????????PolicyDTO??????????????????????????????
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
     * ??????nat???????????????
     * @param policyDTO
     * @param deviceUuid
     * @param ipType
     * @param errorDetailDTOSet
     * @param accessType
     * @param natType 1:???nat???2?????????nat
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
        logger.info("??????nat???????????????{} ?????????UUID???{}??????nat???????????????", JSON.toJSONString(policy), device.getDeviceUuid());
        List<NatRuleMatchFlowVO> natRuleMatchFlowVOList = ngRemoteService.getNatRuleMatchFlow(policy, device, natType);
        logger.info("???????????????????????????:{}", JSON.toJSONString(natRuleMatchFlowVOList));
        List<PolicyDetailVO> ruleList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(natRuleMatchFlowVOList)) {
            // ??????Nat???????????????
            for (NatRuleMatchFlowVO flowVO : natRuleMatchFlowVOList ){
                if(null == flowVO || null == flowVO.getNatRule()){
                    logger.info("????????????Nat?????????????????????");
                    continue;
                }
                PolicyDetailVO natRule = flowVO.getNatRule();
                // ????????????????????????????????????
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

            // ??????nat???????????????????????????????????????????????????????????????????????????????????????????????????nat?????????????????????????????????
            NatRuleMatchFlowVO natRuleMatchFlowVO = natRuleMatchFlowVOList.get(natRuleMatchFlowVOList.size() - 1);
            if(ObjectUtils.isEmpty(natRuleMatchFlowVO) || CollectionUtils.isEmpty(natRuleMatchFlowVO.getRestFlow())){
                String postIpAddress = natType == 1 ? policyDTO.getPostSrcIp() : policyDTO.getPostDstIp();
                logger.error("Nat????????????????????????????????????????????????????????????????????????Nat?????????{} ?????????????????????????????????????????????", JSON.toJSONString(policyDTO));
                this.setErrorMsg(nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "???Nat??????????????? [Nat policy already exists]", policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getServiceList(), policyDTO.getStartTime(), policyDTO.getEndTime(),
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
            // ?????????????????????Nat:??????restFlow??????????????????????????????????????????????????????IP??????????????????IP?????????????????????????????????VIP?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            String ipTerm = this.buildIpTerm(policyDTO.getPreDstIp());
            JSONArray jsonArray = remotePolicyService.remotePolicyDetailByIpTerms(ipTerm, null, deviceUuid, SYSTEM__NAT_LIST.getCode());
            if (jsonArray != null && jsonArray.size() > 0) {
                List<PolicyDetailVO> policyDetailVOS = jsonArray.toJavaList(PolicyDetailVO.class);
                // ?????????????????????????????????
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
                // ??????????????????????????????IP??????VIP???????????????????????????VIP????????????????????????????????????VIP????????????????????????????????????????????????
                if(existPostDstIp && StringUtils.isNotBlank(vipName)){
                    existVipName.add(policyDTO);
                    AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);
                    samePartDTO.setType(PolicyEnum.SECURITY);
                    samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                    // ?????????????????????VIP??????
                    policyDTO.setDstIp(vipName);
                    policyDTO.setDeviceUuid(deviceUuid);
                    RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                    // ??????????????????
                    this.buildRelevancyNat(securityEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,taskJsonArray);
                    this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY, vipName, null);

                    return null;
                } else {
                    this.setErrorMsg(nodeEntity.getDeviceName() + "???" + nodeEntity.getIp() + "???Nat????????????????????? [Nat mapping already exists]", policyDTO.getSrcIp(), policyDTO.getDstIp(), policyDTO.getServiceList(), policyDTO.getStartTime(), policyDTO.getEndTime(),
                            AutoRecommendStatusEnum.NAT_POLICY_HAS_EXIST.getCode(),errorDetailDTOSet,ruleList, accessType,nodeEntity.getUuid(),policyDTO.getSrcZone(),policyDTO.getDstZone(), policyDTO.getPostDstIp());
                    return null;
                }

            }
        }*/

        return policyDTO;

    }

    /**
     * ??????????????????????????????????????????
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
     * ?????????????????????????????????
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
                logger.error("?????????????????????{}", ip4);
                throw new IllegalArgumentException("????????????????????????ip" + ip4 + "??????");
            }
        }
        return ip4AddressList;
    }

    /**
     * ????????????????????????????????????IP?????????????????????
     * ??????????????????????????????IP???IP???????????????????????????IP??????????????????????????????
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
     * ??????????????????ip
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
     * ???????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????
     * @param resultMap
     */
    private Map<String, Set<AddAutoRecommendTaskDTO>> descartesTask(Map<String, Set<AddAutoRecommendTaskDTO>> resultMap){
        Map<String, Set<AddAutoRecommendTaskDTO>> descartesMap = new HashMap<>();
        for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
            String deviceUuid = taskMap.getKey();
            Set<AddAutoRecommendTaskDTO> taskSet = taskMap.getValue();

            Set<AddAutoRecommendTaskDTO> descartesSet =  new HashSet<>();
            for(AddAutoRecommendTaskDTO taskDTO : taskSet){
                // ???????????????
                if(StringUtils.isBlank(taskDTO.getSrcIp())){
                    if(StringUtils.isBlank(taskDTO.getDstIp())){
                        AddAutoRecommendTaskDTO descartesDTO = new AddAutoRecommendTaskDTO();
                        BeanUtils.copyProperties(taskDTO, descartesDTO);
                        descartesSet.add(descartesDTO);
                    } else {
                        // ???????????????????????????
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
                        // ???????????????????????????
                        if(StringUtils.isBlank(taskDTO.getDstIp())){
                            AddAutoRecommendTaskDTO descartesDTO = new AddAutoRecommendTaskDTO();
                            BeanUtils.copyProperties(taskDTO, descartesDTO);
                            descartesDTO.setSrcIp(srcIp);
                            descartesSet.add(descartesDTO);
                        } else {
                            // ????????????????????????
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
     * ???????????????-IP ????????????????????????
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

        String message = String.format("????????????????????????%s??????", vo.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        record = autoRecommendTaskManager.getByUuid(record.getUuid());

        if (ObjectUtils.isEmpty(resultMap)) {
            logger.error("??????????????????????????????????????????????????????{}", JSON.toJSONString(errorDetailDTOSet));
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
            // ????????????????????????
            if (ObjectUtils.isNotEmpty(resultMap)) {
                // ?????????????????????????????????????????????
                Map<String, Set<AddAutoRecommendTaskDTO>> descartesMap = this.descartesTask(resultMap);
                for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : descartesMap.entrySet()) {
                    String deviceUuid = taskMap.getKey();
                    // ??????????????????nat????????????????????????????????????????????????????????????????????????
                    Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                    // ?????????????????????????????????????????????
                    set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_N.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                    if(CollectionUtils.isEmpty(set)){
                        continue;
                    }

                    // 1.???????????????????????????????????????
                    Map<String, Set<AddAutoRecommendTaskDTO>> mergeMap = new HashMap<>();
                    mergeMap.put(deviceUuid, set);
                    Map<String, List<RecommendPolicyDTO>> map = this.converntTaskDTO2PolicyDTO(mergeMap);
                    List<RecommendPolicyDTO> policyDTOList = map.get(deviceUuid);
                    List<RecommendPolicyDTO> filterPolicyDTOList = this.checkDataFlow(new HashSet<>(policyDTOList), deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet,
                            record, conflictPolicyMap);
                    if(CollectionUtils.isEmpty(filterPolicyDTOList)){
                        logger.info("????????????????????????{}", JSON.toJSONString(policyDTOList));
                        continue;
                    }

                    // 2.????????????
                    map.put(deviceUuid, filterPolicyDTOList);
                    logger.info("?????????????????????????????????????????????{}", JSON.toJSONString(map));
                    List<RecommendPolicyDTO> resultPolicyDTOList = mergeService.accurateMergedPolicyMap(map);
                    logger.info("???????????????????????????????????????{}", JSON.toJSONString(resultPolicyDTOList));

                    // 3.???????????????
                    for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
                        createAutoTask = true;
                        logger.info("------------???????????????????????????????????????{}------------", JSONObject.toJSONString(policyDTO));

                        samePartDTO.setType(PolicyEnum.SECURITY);
                        samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                        RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                        policyDTO.setDeviceUuid(deviceUuid);
                        // ??????????????????
                        this.buildRelevancyNat(securityEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,jsonArray);

                        this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY,null, null);
                    }
                }
            }

            // ??????Nat????????????
            if (ObjectUtils.isNotEmpty(resultMap)) {
                if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                    for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
                        String deviceUuid = taskMap.getKey();
                        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
                        Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                        // ???????????????????????????NAT???????????????
                        set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_S.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                        if(CollectionUtils.isEmpty(set)){
                            continue;
                        }

                        // ????????????nat???????????????????????????????????????IP??????????????????????????????????????????
                        Set<AddAutoRecommendTaskDTO> filterSet = new HashSet<>();
                        if (StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), HILLSTONE_MODELNUMBER_KEY)) {
                            for(AddAutoRecommendTaskDTO  hillstoneTask : set ){
                                hillstoneTask.setDstIp("");
                                hillstoneTask.setServiceList(new ArrayList<>());
                                filterSet.add(hillstoneTask);
                            }
                        }

                        Set<AddAutoRecommendTaskDTO> currentSet = CollectionUtils.isEmpty(filterSet) ? set : filterSet;

                        // ?????????nat
                        List<RecommendPolicyDTO> filterPolicyDTOList = new ArrayList<>();
                        for (AddAutoRecommendTaskDTO taskDTO : currentSet) {
                            logger.info("------------?????????????????????Nat?????????{}------------", JSONObject.toJSONString(taskDTO));
                            RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
                            BeanUtils.copyProperties(taskDTO, policyDTO);
                            policyDTO.setDeviceUuid(deviceUuid);
                            policyDTO.setNode(taskDTO.getNodeEntity());
                            // ?????????nat?????????????????????
                            RecommendPolicyDTO resultPolicyDTO = this.checkNatDataFlow(policyDTO, deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet, vo.getAccessType(), 1, conflictPolicyMap, vo, record, existVipName, jsonArray, null);
                            if (ObjectUtils.isEmpty(resultPolicyDTO)) {
                                logger.info("?????????nat??????????????????{}", JSON.toJSONString(resultPolicyDTO));
                                continue;
                            }
                            filterPolicyDTOList.add(resultPolicyDTO);
                        }

                        // 2.????????????,???????????????????????????
                        List<RecommendPolicyDTO> resultPolicyDTOList = new ArrayList<>();
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                            Map<String, List<RecommendPolicyDTO>> map = new HashMap<>();
                            map.put(deviceUuid, filterPolicyDTOList);
                            logger.info("?????????????????????Nat?????????????????????????????????{}", JSON.toJSONString(map));
                            resultPolicyDTOList = mergeService.accurateMergedPolicyMapForFortinetNat(map, PushNatTypeEnum.NAT_TYPE_S.getCode());
                            logger.info("???????????????Nat?????????????????????????????????{}", JSON.toJSONString(resultPolicyDTOList));
                        }

                        resultPolicyDTOList = CollectionUtils.isEmpty(resultPolicyDTOList) ? filterPolicyDTOList : resultPolicyDTOList;
                        // ????????????????????????????????????????????????
                        for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
                            // ???????????????????????????
                            // ????????????????????????nat
                            createAutoTask = true;
                            samePartDTO.setType(PolicyEnum.SNAT);
                            samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT);
                            RecommendTaskEntity snatEntity = this.createSnatTask(policyDTO, samePartDTO, record);
                            this.addAutoRecommendTask(policyDTO, samePartDTO, snatEntity.getId(),PolicyEnum.SNAT,null, null);
                            // ??????nat
                            this.buildRelevancyNat(snatEntity, deviceUuid, record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT,jsonArray);
                        }
                    }
                } else if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())){
                    for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
                        String deviceUuid = taskMap.getKey();
                        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
                        Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                        // ??????????????????????????????NAT???????????????
                        set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_D.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                        if(CollectionUtils.isEmpty(set)){
                            continue;
                        }

                        // ????????????nat
                        List<MergeFortinetPolicyDTO> fortinetPolicyDTOList = new ArrayList<>();
                        List<RecommendPolicyDTO> filterPolicyDTOList = new ArrayList<>();
                        List<RecommendPolicyDTO> resultPolicyDTOList = new ArrayList<>();
                        for (AddAutoRecommendTaskDTO taskDTO : set) {
                            logger.info("------------????????????????????????Nat?????????{}------------", JSONObject.toJSONString(taskDTO));
                            RecommendPolicyDTO policyDTO = new RecommendPolicyDTO();
                            BeanUtils.copyProperties(taskDTO, policyDTO);
                            policyDTO.setDeviceUuid(deviceUuid);
                            policyDTO.setNode(taskDTO.getNodeEntity());

                            // ????????????nat?????????????????????
                            RecommendPolicyDTO resultPolicyDTO = this.checkNatDataFlow(policyDTO, deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet, vo.getAccessType(), 2,  conflictPolicyMap,vo, record, existVipName,jsonArray, fortinetPolicyDTOList);
                            if(ObjectUtils.isEmpty(resultPolicyDTO)){
                                logger.info("????????????nat??????????????????{}", JSON.toJSONString(resultPolicyDTO));
                                continue;
                            }
                            filterPolicyDTOList.add(resultPolicyDTO);
                        }

                        // 2.????????????,???????????????????????????
                        if(StringUtils.containsIgnoreCase(nodeEntity.getModelNumber(), FORTINET_MODELNUMBER_KEY)){
                            List<RecommendPolicyDTO> mergeFortinetPolicyList = this.mergeFortinetPolicy(fortinetPolicyDTOList);
                            Map<String, List<RecommendPolicyDTO>> map = new HashMap<>();
                            map.put(deviceUuid, mergeFortinetPolicyList);
                            logger.info("????????????????????????Nat???????????????????????????????????????????????????????????????{}", JSON.toJSONString(map));
                            resultPolicyDTOList = mergeService.accurateMergedPolicyMapForFortinetNat(map, PushNatTypeEnum.NAT_TYPE_D.getCode());

                            Map<String, List<RecommendPolicyDTO>> secondMap = new HashMap<>();
                            secondMap.put(deviceUuid, resultPolicyDTOList);
                            logger.info("????????????????????????Nat??????????????????????????????????????????VIP?????????????????????{}", JSON.toJSONString(secondMap));
                            resultPolicyDTOList = mergeService.accurateMergedPolicyMapForFortinetVIP(secondMap);
                            logger.info("???????????????Nat?????????????????????????????????{}", JSON.toJSONString(resultPolicyDTOList));
                        }

                        resultPolicyDTOList = CollectionUtils.isEmpty(resultPolicyDTOList) ? filterPolicyDTOList : resultPolicyDTOList;
                        for (RecommendPolicyDTO policyDTO : resultPolicyDTOList) {
                            createAutoTask = true;
                            samePartDTO.setType(PolicyEnum.DNAT);
                            samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT);
                            RecommendTaskEntity dnatEntity = this.createDnatTask(policyDTO, samePartDTO, record);
                            this.addAutoRecommendTask(policyDTO, samePartDTO, dnatEntity.getId(), PolicyEnum.DNAT,null, null);
                            // ??????nat
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
                    // ??????????????????????????????????????????????????????
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
     * ???????????????-???????????? ????????????????????????
     *         // 1.???????????????????????????????????????IP
     *         // 2.????????????IP??????????????????
     *         // 3.??????IP?????????????????????
     *         // 4.??????????????????
     *         // 5.???????????????????????????
     *         // 6.?????????????????????????????????
     *         // 7.???????????????
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
        // 1.???????????????????????????????????????IP
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
                    // 2.????????????IP??????????????????
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

        // 3.??????IP?????????????????????
        if(StringUtils.isNotBlank(srcIps)){
            String[] srcIpList = srcIps.split(",");
            for (String srcIp : srcIpList) {
                if(IpUtils.isIPSegment(srcIp) && StringUtils.equalsAnyIgnoreCase(IpUtils.getMaskBitFromIpSegment(srcIp), "0")){
                    policyInfoMap.clear();
                    errorDetailDTOSet.clear();
                    this.setErrorMsg("?????????????????????0????????? [Creating policy with subnet mask 0 is prohibited]", srcIpOrName, dstIpOrName, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                            vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                            null, vo.getAccessType(),null,null,null,null);
                    return;
                }

                // ??????????????????
                List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(srcIp, vo);
                if (CollectionUtils.isEmpty(list)) {
                    // TODO ??????
                    if (PushAccessTypeEnum.INSIDE_TO_OUTSIDE.getCode().equals(vo.getAccessType())) {
                        String msg = "???IP:" + srcIp + "????????????????????????????????????????????????????????? [Source ip is not in protect network table,please check the protect network table]";
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
                        String msg = configEntity.getDeviceIp() + "???????????????????????????????????????????????? [Source ip can't find relevant firewall in protect network table,please check the protect network table]";
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
                    this.setErrorMsg("?????????????????????0????????? [Prohibit create policy with subnet mask 0]", srcIpOrName, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                            vo.getEndTime() == null ? null : new Date(vo.getEndTime()),AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,
                            null, vo.getAccessType(),null,null,null,null);
                    return;
                }

                // ????????????????????????????????????????????????
                List<ProtectNetworkConfigEntity> list = pushProtectNetworkConfigService.findByIp(dstIp, vo);
                if (CollectionUtils.isEmpty(list)) {
                    if (PushAccessTypeEnum.OUTSIDE_TO_INSIDE.getCode().equals(vo.getAccessType())) {
                        String msg = "??????IP:" + dstIp + "????????????????????????????????????????????????????????? [Destination ip is not in the protect network table,please check the protect network table]";
                        AutoRecommendErrorDetailDTO srcErrorDetailDTO = this.setSrcErrorMsg(msg, srcIpOrName, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()),
                                vo.getEndTime() == null ? null : new Date(vo.getEndTime()), AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),vo.getAccessType());
                        dstErrorDetailList.add(srcErrorDetailDTO);

                        continue;
                    } else {
                        String[] errorIP = srcAndDstAllError(errorMsgMap, vo.getSrcIp(), dstIp);
                        if(ObjectUtils.isNotEmpty(errorIP) && errorIP.length == 2){
                            String msg = "???IP???"+ errorIP[0] +"??????IP:" + errorIP[1] + "???????????????????????????????????????????????????????????? [Source ip:"+errorIP[0]+" and destination ip:"+ errorIP[1]+" are not in the protect network table,please check the protect network table]";
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
                        String msg = configEntity.getDeviceIp() + "???????????????????????????????????????????????? [Firewall offline,please check the protect network table]";
                        this.setErrorMsg(msg, srcIpOrName, dstIp, vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                                AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(), null,null,null,null);
                        continue;
                    }
                    String deviceUuid = nodeEntity.getUuid();
                    // ????????????????????????IP?????????Nat??????????????????????????????????????????
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
            String msg = "????????????IP?????????????????????????????????????????????????????? [Source ip and destination ip are not in the protect network table,please check the protect network table]";
            this.setErrorMsg(msg, vo.getSrcIp(), vo.getDstIp(), vo.getServiceList(), vo.getStartTime() == null ? null : new Date(vo.getStartTime()), vo.getEndTime() == null ? null : new Date(vo.getEndTime()),
                    AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode(),errorDetailDTOSet,null, vo.getAccessType(),null,null,null,null);
            // ??????????????????
            record.setStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            autoRecommendTaskMapper.updateByPrimaryKey(record);
            return;
        }


        // 4.????????????????????????
        if(srcFindNode || dstFindNode){
            this.getInterfaceAndZone(policyInfoMap, resultMap, vo, errorDetailDTOSet);
        }

        String message = String.format("????????????????????????%s??????", vo.getTheme());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        if (ObjectUtils.isEmpty(resultMap)) {
            logger.error("??????????????????????????????????????????????????????{}", JSON.toJSONString(errorDetailDTOSet));
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
            // ??????????????????
            autoRecommendTaskMapper.updateByPrimaryKey(record);
        } else {
            JSONArray jsonArray = new JSONArray();
            AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);

            boolean createAutoTask = false;
            List<RecommendPolicyDTO> existVipName = new ArrayList();
            // ????????????????????????
            if (ObjectUtils.isNotEmpty(resultMap)) {
                for (Map.Entry<String, Set<AddAutoRecommendTaskDTO>> taskMap : resultMap.entrySet()) {
                    String deviceUuid = taskMap.getKey();
                    // ??????????????????nat????????????????????????????????????????????????????????????????????????
                    Set<AddAutoRecommendTaskDTO> set = taskMap.getValue();

                    // ?????????????????????????????????????????????
                    set = set.stream().filter(taskDTO -> PushNatTypeEnum.NAT_TYPE_N.getCode().equals(taskDTO.getNatType())).collect(Collectors.toSet());
                    if(CollectionUtils.isEmpty(set)){
                        continue;
                    }

                    // 1.???????????????????????????????????????
                    Map<String, Set<AddAutoRecommendTaskDTO>> mergeMap = new HashMap<>();
                    mergeMap.put(deviceUuid, set);
                    Map<String, List<RecommendPolicyDTO>> map = this.converntTaskDTO2PolicyDTO(mergeMap);
                    /*List<RecommendPolicyDTO> policyDTOList = map.get(deviceUuid);
                    List<RecommendPolicyDTO> filterPolicyDTOList = this.checkDataFlow(new HashSet<>(policyDTOList), deviceUuid, IpTypeEnum.IPV4.getCode(), errorDetailDTOSet,
                            vo.getAccessType(), conflictPolicyMap);
                    if(CollectionUtils.isEmpty(filterPolicyDTOList)){
                        logger.info("????????????????????????{}", JSON.toJSONString(policyDTOList));
                        continue;
                    }*/

                    // 2.????????????
//                    map.put(deviceUuid, filterPolicyDTOList);
                    logger.info("?????????????????????????????????????????????{}", JSON.toJSONString(map));
                    List<RecommendPolicyDTO> resultPolicyDTOList = mergeService.accurateMergedPolicyMap(map);
                    logger.info("???????????????????????????????????????{}", JSON.toJSONString(resultPolicyDTOList));

                    // 3.???????????????
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
                                // ?????????????????????????????????-IP??????????????????????????????IP????????????????????????
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
                                // ?????????????????????????????????-IP??????????????????????????????IP????????????????????????
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

                        // ???????????????????????????????????????????????????????????????????????????????????????????????????
                        AutoRecommendSpecialDTO autoRecommendSpecialDTO = this.buildSpecialDTO(srcNameSet, dstNameSet, objectNameAndIpMap, deviceUuid);

                        createAutoTask = true;
                        logger.info("------------???????????????????????????????????????{}------------", JSONObject.toJSONString(policyDTO));

                        samePartDTO.setType(PolicyEnum.SECURITY);
                        samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                        if(CollectionUtils.isNotEmpty(srcNameSet)){
                            policyDTO.setSrcAddressObjectName(String.join(",", srcNameSet));
                        }
                        if(CollectionUtils.isNotEmpty(dstNameSet)){
                            policyDTO.setDstAddressObjectName(String.join(",", dstNameSet));
                        }
                        if(recommendTaskDTOSet.contains(policyDTO)){
                            logger.info("??????????????????????????????{}", JSON.toJSONString(policyDTO));
                            continue;
                        } else {
                            recommendTaskDTOSet.add(policyDTO);
                        }
                        RecommendTaskEntity securityEntity = this.createSecurityTask(policyDTO, samePartDTO);
                        policyDTO.setDeviceUuid(deviceUuid);
                        // ??????????????????
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
                    // ??????????????????????????????????????????????????????
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

    //??????????????????10????????????????????????????????????????????????????????? ??????9???????????????????????????????????????????????????????????????????????????
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
                                if (StringUtils.isEmpty(command) || (StringUtils.isNotEmpty(command) && command.startsWith("?????????????????????????????????"))){
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
     * ?????????????????????????????????????????????????????????
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

        // ??????????????????????????????
        for (String srcObjectName : srcNameSet ) {
            ResultRO<List<NetWorkGroupObjectShowVO>> dataResultRO = addressManageTaskManager.getDeviceAddressByName(deviceUuid, srcObjectName);
            if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                logger.info("????????????uuid???{} ???????????????????????????{}?????????????????????", deviceUuid, srcObjectName);
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
                // ???????????????????????????
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

        // ?????????????????????????????????
        for (String dstObjectName : dstNameSet ) {
            ResultRO<List<NetWorkGroupObjectShowVO>> dataResultRO = addressManageTaskManager.getDeviceAddressByName(deviceUuid, dstObjectName);
            if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
                logger.info("????????????uuid???{} ???????????????????????????{}?????????????????????",deviceUuid, dstObjectName);
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
                // ???????????????????????????
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
                logger.info("??????ID???{} ????????????????????????????????????", conflictVo.getAutoTaskId());
                return new ReturnT(allResultDTO);
            }
        } else {
            return new ReturnT(ReturnT.FAIL_CODE,"??????????????????");
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
                logger.info("??????ID???{} ????????????????????????????????????", conflictVo.getAutoTaskId());
                return new ReturnT(allResultDTO);
            }
        } else if(ObjectUtils.isNotEmpty(conflictVo.getAutoTaskId())){
            autoTaskEntity = autoRecommendTaskMapper.selectByPrimaryKey(conflictVo.getAutoTaskId());
            if(autoTaskEntity == null ){
                logger.info("??????ID???{} ????????????????????????????????????", conflictVo.getAutoTaskId());
                return new ReturnT(allResultDTO);
            }
        } else {
            return new ReturnT(ReturnT.FAIL_CODE,"??????????????????");
        }

        // ??????????????????
        if(StringUtils.isNotBlank(autoTaskEntity.getErrorMsg())){
            allResultDTO.setErrorDetailDTOList(JSONArray.parseArray(autoTaskEntity.getErrorMsg(), AutoRecommendErrorDetailDTO.class));
        }

        // ?????? ?????????/????????? ??????????????????
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
                    // ??????????????????
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
            // ??????????????????????????????????????????????????????????????????????????????
            List<CommandTaskEditableEntity> commandTaskEditableEntities = commandTaskEdiableMapper.selectByTaskId(recommendTaskEntity.getId());
            if(CollectionUtils.isEmpty(commandTaskEditableEntities)){
                continue;
            }
            String deviceUuid = data.getDeviceUuid();
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            String deviceName = nodeEntity.getDeviceName();
            DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
            DeviceDataRO deviceData = device.getData().get(0);
            // ???????????????????????????????????????
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

        // ?????????????????????????????????????????????????????????
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
     * ???????????????????????????????????????
     * @param securityConflictPolicyDTOList
     * @param autoTaskId
     * @param theme
     * @param policyType ???????????????0??????????????????1???nat??????
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
     * ??????????????????NAT
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
                // ?????????????????????Nat:??????restFlow??????????????????????????????????????????????????????IP??????????????????IP?????????????????????????????????VIP?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                String ipTerm = this.buildIpTerm(policyDTO.getPreDstIp());
                JSONArray jsonArray = remotePolicyService.remotePolicyDetailByIpTerms(ipTerm, null, nodeEntity.getUuid(), SYSTEM__NAT_LIST.getCode());
                if (jsonArray != null && jsonArray.size() > 0) {
                    List<PolicyDetailVO> policyDetailVOS = jsonArray.toJavaList(PolicyDetailVO.class);
                    // ?????????????????????????????????
                    boolean existPostDstIp = false;
                    String vipName = null;
                    for (PolicyDetailVO policyDetailVO : policyDetailVOS ){
                        if(StringUtils.equals(policyDetailVO.getPostDstIp(), policyDTO.getPostDstIp())){
                            existPostDstIp = true;
                            vipName = policyDetailVO.getPolicyName();
                            break;
                        }
                    }
                    // ??????????????????????????????IP??????VIP???????????????????????????VIP????????????????????????????????????VIP????????????????????????????????????????????????
                    if(existPostDstIp && StringUtils.isNotBlank(vipName)){
                        AutoRecommendTaskSamePartDTO samePartDTO = this.buildSamePartDTO(vo, record);
                        samePartDTO.setType(PolicyEnum.SECURITY);
                        samePartDTO.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED);
                        // ?????????????????????VIP??????
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
                        // ??????????????????
                        this.buildRelevancyNat(securityEntity, nodeEntity.getUuid(), record.getId(), PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED,taskJsonArray);
                        this.addAutoRecommendTask(policyDTO, samePartDTO, securityEntity.getId(),PolicyEnum.SECURITY, vipName);*/

                    }
                } else {
                    // ??????VIP??????
                    String fortinetCreateName = String.format("mip_%s_%s ", record.getTheme(), IdGen.getRandomNumberString());
                    // ?????????????????????VIP??????
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
