package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.advanced.dao.mysql.PushMappingNatMapper;
import com.abtnetworks.totems.advanced.dto.SearchPushMappingNatDTO;
import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.TwoMemberObject;
import com.abtnetworks.totems.common.dto.commandline.DNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.IpMatchUtil;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.push.dto.PushRecommendStaticRoutingDTO;
import com.abtnetworks.totems.push.service.PushTaskStaticRoutingService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfNatDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfRouteDTO;
import com.abtnetworks.totems.recommend.entity.AddRecommendTaskEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.RecommendBussCommonService;
import com.abtnetworks.totems.recommend.service.RecommendRelevanceSceneService;
import com.abtnetworks.totems.recommend.service.WhatIfService;
import com.abtnetworks.totems.recommend.task.impl.SimulationTaskServiceImpl;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.abtnetworks.totems.common.constants.CommonConstants.HOUR_SECOND;
import static com.abtnetworks.totems.common.constants.PolicyConstants.BIG_INTERNET_RECOMMEND;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV6;

/**
 * @author Administrator
 * @Title:
 * @Description: ?????????????????????????????????????????????
 * @date 2021/1/14
 */
@Slf4j
@Service
public class RecommendBussCommonServiceImpl implements RecommendBussCommonService {
    @Autowired
    WhatIfService whatIfService;

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    ExcelParser excelParser;

    @Resource
    RemoteBranchService remoteBranchService;

    @Resource
    RecommendTaskMapper recommendTaskMapper;

    @Autowired
    PushMappingNatMapper pushMappingNatMapper;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;
    @Qualifier("simulationTaskServiceImpl")
    @Autowired
    SimulationTaskServiceImpl recommendTaskManager;

    @Autowired
    PushTaskStaticRoutingService pushTaskStaticRoutingService;

    @Autowired
    RecommendRelevanceSceneService recommendRelevanceSceneService;

    /**
     * ????????????????????????
     * <p>
     * //     * @param natExcelList   ??????????????????????????????
     * //     * @param natTaskList    nat??????????????????
     * //     * @param whatIfCaseName ??????????????????
     * //     * @param user           ??????
     *
     * @return ??????????????????UUID
     */
    @Override
    public WhatIfRO createWhatIfCaseUuid(RecommendTaskEntity taskEntity) {
        WhatIfRO whatIf = null;
        String relevancyNat = taskEntity.getRelevancyNat();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(relevancyNat)) {
            String whatIfCaseName = String.format("A%s", String.valueOf(System.currentTimeMillis()));
            JSONArray whatIfCaseArray = JSONObject.parseArray(relevancyNat);
            List<WhatIfNatDTO> whatIfNatDTOList = new ArrayList<>();
            List<WhatIfRouteDTO> whatIfRouteDTOList = new ArrayList<>();
            if (whatIfCaseArray != null && whatIfCaseArray.size() > 0) {
                Integer ipTypeForTask = ObjectUtils.isNotEmpty(taskEntity.getIpType())?taskEntity.getIpType(): IPV4.getCode();

                for (int i = 0; i < whatIfCaseArray.size(); i++) {
                    JSONObject jsonObject = (JSONObject) whatIfCaseArray.get(i);
                    int taskId = jsonObject.getIntValue("taskId");
                    int specialRelevancyNat = jsonObject.getIntValue("type");

                    RecommendTaskEntity recommendTaskByTask = null;
                    RecommendRelevanceSceneDTO recommendRelevanceSceneDTO = null;
                    if (PolicyConstants.POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT != specialRelevancyNat) {
                        recommendTaskByTask = policyRecommendTaskService.getRecommendTaskByTaskId(taskId);
                        if (null == recommendTaskByTask) {
                            continue;
                        }
                    } else {
                        recommendRelevanceSceneDTO = recommendRelevanceSceneService.queryById(taskId);
                        if (null == recommendRelevanceSceneDTO) {
                            continue;
                        }
                        // ????????????????????? ????????????????????? ????????????????????????????????????NAT????????????????????????????????????????????????????????????
                        if(StringUtils.isBlank(recommendRelevanceSceneDTO.getPostSrcIp()) && StringUtils.isBlank(recommendRelevanceSceneDTO.getDstItf())
                                    && StringUtils.isBlank(recommendRelevanceSceneDTO.getPostDstIp())){
                            continue;
                        }
                    }

                    // ????????????????????????
                    if (null != recommendTaskByTask) {
                        buildWhatIfList(whatIfNatDTOList, whatIfRouteDTOList, ipTypeForTask, taskId, recommendTaskByTask);
                    }
                    // ???????????????????????????(??????????????????NAT??????)
                    if (null != recommendRelevanceSceneDTO) {
                        buildSpecialWhatIfNat(whatIfNatDTOList, ipTypeForTask, recommendRelevanceSceneDTO);
                    }
                }
            }
            whatIf = whatIfService.createWhatIfCase(whatIfNatDTOList,whatIfRouteDTOList, whatIfCaseName, whatIfCaseName);
        }
        return whatIf;
    }

    /**
     * ???????????????????????????
     * @param whatIfNatDTOList
     * @param ipTypeForTask
     * @param sceneDTO
     */
    private void buildSpecialWhatIfNat(List<WhatIfNatDTO> whatIfNatDTOList, Integer ipTypeForTask, RecommendRelevanceSceneDTO sceneDTO) {
        WhatIfNatDTO whatIfNatDTO = new WhatIfNatDTO();

        String deviceUuid = sceneDTO.getDeviceUuid();
        if (StringUtils.isBlank(deviceUuid)) {
            log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", deviceUuid));
            return;
        }
        Integer ipType = null != sceneDTO.getIpType() ? sceneDTO.getIpType() : IPV4.getCode();
        if (!ipType.equals(ipTypeForTask)) {
            return;
        }
        String ipTypeParam = IPV4.getCode().equals(ipType) ? PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4 : PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6;

        whatIfNatDTO.setDeviceUuid(deviceUuid);
        whatIfNatDTO.setName(sceneDTO.getName());
        BeanUtils.copyProperties(sceneDTO, whatIfNatDTO);
        whatIfNatDTO.setPreDstAddress(sceneDTO.getPreDstIp());
        whatIfNatDTO.setPreSrcAddress(sceneDTO.getPreSrcIp());

        String preService = sceneDTO.getServiceListJson();
        if (StringUtils.isNotEmpty(preService)) {
            List<ServiceDTO> serviceDTOList = JSONArray.parseArray(preService, ServiceDTO.class);
            whatIfNatDTO.setPreServiceList(serviceDTOList);
        }
        whatIfNatDTO.setDstZone(sceneDTO.getDstZone());

        whatIfNatDTO.setSrcZone(sceneDTO.getSrcZone());
        whatIfNatDTO.setInDevItf(sceneDTO.getSrcItf());
        whatIfNatDTO.setOutDevItf(sceneDTO.getDstItf());
        whatIfNatDTO.setIpType(ipTypeParam);
        whatIfNatDTO.setNatType("DYNAMIC");

        if (StringUtils.isNotBlank(sceneDTO.getPostSrcIp()) && StringUtils.isNotBlank(sceneDTO.getPostDstIp())) {
            // bothNat??????
            whatIfNatDTO.setNatField("BOTH");
            if (IPV6.getCode().equals(ipType)) {
                whatIfNatDTO.setPreIp6DstAddress(sceneDTO.getPreDstIp());
                whatIfNatDTO.setPreIp6SrcAddress(sceneDTO.getPreSrcIp());
                whatIfNatDTO.setPostIp6SrcAddress(sceneDTO.getPostSrcIp());
                whatIfNatDTO.setPostIp6DstAddress(sceneDTO.getPostDstIp());
            } else {
                whatIfNatDTO.setPostSrcAddress(sceneDTO.getPostSrcIp());
                whatIfNatDTO.setPostDstAddress(sceneDTO.getPostDstIp());
            }

            String postService = sceneDTO.getPostService();

            if (StringUtils.isNotBlank(postService)) {
                JSONArray array = JSONObject.parseArray(postService);
                List<ServiceDTO> postServiceList = array.toJavaList(ServiceDTO.class);
                // ???????????????????????????????????????????????????????????????
                if (CollectionUtils.isNotEmpty(postServiceList) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(postServiceList.get(0).getProtocol())) {
                    whatIfNatDTO.setPostServiceList(postServiceList);
                }
            }

        } else if (StringUtils.isNotBlank(sceneDTO.getPostSrcIp())) {
            whatIfNatDTO.setNatField("SRC");
            if (IPV6.getCode().equals(ipType)) {
                whatIfNatDTO.setPreIp6DstAddress(sceneDTO.getDstIp());
                whatIfNatDTO.setPreIp6SrcAddress(sceneDTO.getSrcIp());
                whatIfNatDTO.setPostIp6DstAddress(null);
                whatIfNatDTO.setPostIp6SrcAddress(sceneDTO.getPostSrcIp());
            } else {
                whatIfNatDTO.setPostDstAddress(null);
                whatIfNatDTO.setPostSrcAddress(sceneDTO.getPostSrcIp());
            }
        } else if (StringUtils.isNotBlank(sceneDTO.getPostDstIp())) {
            whatIfNatDTO.setNatField("DST");
            if (IPV6.getCode().equals(ipType)) {
                whatIfNatDTO.setPreIp6DstAddress(sceneDTO.getDstIp());
                whatIfNatDTO.setPreIp6SrcAddress(sceneDTO.getSrcIp());
                whatIfNatDTO.setPostIp6SrcAddress(null);
                whatIfNatDTO.setPostIp6DstAddress(sceneDTO.getPostDstIp());
            } else {
                whatIfNatDTO.setPostSrcAddress(null);
                whatIfNatDTO.setPostDstAddress(sceneDTO.getPostDstIp());
            }
            String postService = sceneDTO.getPostService();

            if (StringUtils.isNotBlank(postService)) {
                JSONArray array = JSONObject.parseArray(postService);
                List<ServiceDTO> postServiceList = array.toJavaList(ServiceDTO.class);
                // ???????????????????????????????????????????????????????????????
                if (CollectionUtils.isNotEmpty(postServiceList) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(postServiceList.get(0).getProtocol())) {
                    whatIfNatDTO.setPostServiceList(postServiceList);
                }
            }
        } else if (StringUtils.isNotBlank(sceneDTO.getSrcIp()) && StringUtils.isNotBlank(sceneDTO.getDstItf())) {
            // ?????????????????????
            whatIfNatDTO.setNatField("SRC");
            if (IPV6.getCode().equals(ipType)) {
                whatIfNatDTO.setPreIp6DstAddress(sceneDTO.getDstIp());
                whatIfNatDTO.setPreIp6SrcAddress(sceneDTO.getSrcIp());
                whatIfNatDTO.setPostIp6DstAddress(null);
                whatIfNatDTO.setPostIp6SrcAddress(sceneDTO.getPostSrcIp());
            } else {
                whatIfNatDTO.setPostDstAddress(null);
                whatIfNatDTO.setPostSrcAddress(null);
            }
        }

        whatIfNatDTOList.add(whatIfNatDTO);
    }

    /**
     * ????????????????????????getRelevanceSceneById
     * @param whatIfNatDTOList
     * @param whatIfRouteDTOList
     * @param ipTypeForTask
     * @param taskId
     * @param recommendTaskByTask
     */
    private void buildWhatIfList(List<WhatIfNatDTO> whatIfNatDTOList, List<WhatIfRouteDTO> whatIfRouteDTOList, Integer ipTypeForTask, int taskId, RecommendTaskEntity recommendTaskByTask) {
        // ?????????????????????????????????
        if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING == recommendTaskByTask.getTaskType()) {
            PushRecommendStaticRoutingDTO routeDto = pushTaskStaticRoutingService.getStaticRoutingByTaskId(taskId);
            if (null == routeDto) {
                return;
            }
            WhatIfRouteDTO whatIfRouteDTO = new WhatIfRouteDTO();
            whatIfRouteDTO.setName(recommendTaskByTask.getTheme());
            whatIfRouteDTO.setDeviceUuid(routeDto.getDeviceUuid());
            whatIfRouteDTO.setRouteType(RoutingEntryTypeEnum.STATIC);
            Integer ipType = ObjectUtils.isNotEmpty(recommendTaskByTask.getIpType()) ? recommendTaskByTask.getIpType() : IPV4.getCode();
            if (!ipType.equals(ipTypeForTask)) {
                return;
            }
            whatIfRouteDTO.setIpType(ipType);
            if (IPV4.getCode().equals(ipType)) {
                whatIfRouteDTO.setIpv4DstIp(recommendTaskByTask.getDstIp());
                whatIfRouteDTO.setIp4Gateway(routeDto.getNextHop());
            } else if (IPV6.getCode().equals(ipType)) {
                whatIfRouteDTO.setIpv6DstIp(recommendTaskByTask.getDstIp());
                whatIfRouteDTO.setIp6Gateway(routeDto.getNextHop());
            }
            whatIfRouteDTO.setMaskLength(routeDto.getSubnetMask());
            whatIfRouteDTO.setInterfaceName(routeDto.getOutInterface());
            whatIfRouteDTO.setRoutingTableUuid(routeDto.getSrcVirtualRouter());
            whatIfRouteDTO.setDstRoutingTableUuid(routeDto.getDstVirtualRouter());
            whatIfRouteDTO.setDistance(StringUtils.isBlank(routeDto.getPriority()) ? 0 : Integer.valueOf(routeDto.getPriority()));
            whatIfRouteDTOList.add(whatIfRouteDTO);
        } else {
            WhatIfNatDTO whatIfNatDTO = new WhatIfNatDTO();
            String additionInfo = recommendTaskByTask.getAdditionInfo();
            if (StringUtils.isEmpty(additionInfo)) {
                //??????nat??????
                return;
            }
            JSONObject additionJson = JSONObject.parseObject(additionInfo);
            String deviceUuid = additionJson.getString("deviceUuid");
            if (deviceUuid == null) {
                log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", deviceUuid));
                return;
            }
            Integer ipType = ObjectUtils.isNotEmpty(recommendTaskByTask.getIpType())?recommendTaskByTask.getIpType(): IPV4.getCode();
            if(!ipType.equals(ipTypeForTask)){
                return;
            }
            String ipTypeParam = IPV4.getCode().equals(ipType) ? PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4 : PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6;

            whatIfNatDTO.setDeviceUuid(deviceUuid);
            whatIfNatDTO.setName(recommendTaskByTask.getTheme());
            BeanUtils.copyProperties(recommendTaskByTask, whatIfNatDTO);
            whatIfNatDTO.setPreDstAddress(recommendTaskByTask.getDstIp());
            whatIfNatDTO.setPreSrcAddress(recommendTaskByTask.getSrcIp());

            String preService = recommendTaskByTask.getServiceList();
            List<ServiceDTO> serviceDTOList = new ArrayList<>();
            if (StringUtils.isNotEmpty(preService)) {
                serviceDTOList = JSONArray.parseArray(preService, ServiceDTO.class);
                whatIfNatDTO.setPreServiceList(serviceDTOList);
            }
            whatIfNatDTO.setDstZone(additionJson.getString("dstZone"));

            whatIfNatDTO.setSrcZone(additionJson.getString("srcZone"));
            whatIfNatDTO.setInDevItf(additionJson.getString("srcItf"));
            whatIfNatDTO.setOutDevItf(additionJson.getString("dstItf"));
            whatIfNatDTO.setIpType(ipTypeParam);
            String natField = NatTypeEnum.getNatByCode(recommendTaskByTask.getTaskType()).getNatField();
            if ("STATIC".equalsIgnoreCase(natField)) {
                whatIfNatDTO.setNatType("STATIC");
                String preDstAddress = additionJson.getString("insideAddress");

                String postDstAddress = additionJson.getString("globalAddress");
                if (IPV6.getCode().equals(ipType)) {

                    whatIfNatDTO.setPreIp6DstAddress(preDstAddress);
                    whatIfNatDTO.setPostIp6DstAddress(postDstAddress);
                    whatIfNatDTO.setPreIp6SrcAddress(recommendTaskByTask.getSrcIp());
                } else {
                    whatIfNatDTO.setPreDstAddress(preDstAddress);
                    whatIfNatDTO.setPostDstAddress(postDstAddress);
                }

            } else if ("SRC".equalsIgnoreCase(natField)) {
                whatIfNatDTO.setNatType("DYNAMIC");
                whatIfNatDTO.setNatField("SRC");
                if (IPV6.getCode().equals(ipType)) {
                    whatIfNatDTO.setPreIp6DstAddress(recommendTaskByTask.getDstIp());
                    whatIfNatDTO.setPreIp6SrcAddress(recommendTaskByTask.getSrcIp());
                    whatIfNatDTO.setPostIp6DstAddress(null);
                    whatIfNatDTO.setPostIp6SrcAddress(additionJson.getString("postIpAddress"));
                } else {
                    whatIfNatDTO.setPostDstAddress(null);
                    whatIfNatDTO.setPostSrcAddress(additionJson.getString("postIpAddress"));
                }
            } else if ("DST".equalsIgnoreCase(natField)) {
                whatIfNatDTO.setNatField("DST");
                whatIfNatDTO.setNatType("DYNAMIC");
                if (IPV6.getCode().equals(ipType)) {
                    whatIfNatDTO.setPreIp6DstAddress(recommendTaskByTask.getDstIp());
                    whatIfNatDTO.setPreIp6SrcAddress(recommendTaskByTask.getSrcIp());
                    whatIfNatDTO.setPostIp6SrcAddress(null);
                    whatIfNatDTO.setPostIp6DstAddress(additionJson.getString("postIpAddress"));
                } else {
                    whatIfNatDTO.setPostSrcAddress(null);
                    whatIfNatDTO.setPostDstAddress(additionJson.getString("postIpAddress"));
                }
            } else if ("BOTH".equalsIgnoreCase(natField)) {
                whatIfNatDTO.setNatField("BOTH");
                whatIfNatDTO.setNatType("DYNAMIC");
                if (IPV6.getCode().equals(ipType)) {
                    whatIfNatDTO.setPreIp6DstAddress(recommendTaskByTask.getDstIp());
                    whatIfNatDTO.setPreIp6SrcAddress(recommendTaskByTask.getSrcIp());
                    whatIfNatDTO.setPostIp6SrcAddress(additionJson.getString("postSrcIp"));
                    whatIfNatDTO.setPostIp6DstAddress(additionJson.getString("postDstIp"));
                } else {
                    whatIfNatDTO.setPostSrcAddress(additionJson.getString("postSrcIp"));
                    whatIfNatDTO.setPostDstAddress(additionJson.getString("postDstIp"));
                }

                String postPort = additionJson.getString("postPort");
                if (CollectionUtils.isNotEmpty(serviceDTOList) && StringUtils.isNotEmpty(postPort)) {
                    List<ServiceDTO> serviceDTOListPost = new ArrayList<>();

                    for (ServiceDTO serviceDTO : serviceDTOList) {
                        ServiceDTO serviceDTO1 = new ServiceDTO();
                        serviceDTO1.setProtocol(serviceDTO.getProtocol());
                        serviceDTO1.setDstPorts(postPort);
                        serviceDTOListPost.add(serviceDTO1);
                    }
                    whatIfNatDTO.setPostServiceList(serviceDTOListPost);
                }
            }
            whatIfNatDTOList.add(whatIfNatDTO);
        }
    }

    /**
     * ????????????????????????
     *
     * @param natExcelList   ??????????????????????????????
     * @param natTaskList    nat??????????????????
     * @param whatIfCaseName ??????????????????
     * @param user           ??????
     * @return ??????????????????UUID
     */
    @Override
    public WhatIfRO createWhatIfCaseUuid(List<ExcelTaskNatEntity> natExcelList, List<RecommendTaskEntity> natTaskList, List<PushRecommendStaticRoutingDTO> routeTaskList, List<RecommendRelevanceSceneDTO> specialNatList, String whatIfCaseName, String user, UserInfoDTO userInfoDTO) {
        WhatIfRO whatIf = null;
        List<WhatIfNatDTO> whatIfNatDTOList = new ArrayList<>();
        List<WhatIfRouteDTO> whatIfRouteDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(natExcelList)) {
            whatIfNatDTOList.addAll(getWhatIfNatDTOList(natExcelList));
        }
        if (CollectionUtils.isNotEmpty(routeTaskList)) {
            whatIfRouteDTOList = getWhatIfRouteDTOList(routeTaskList);
        }
        if (CollectionUtils.isNotEmpty(specialNatList)) {
            whatIfNatDTOList.addAll(getWhatIfSpecialNatDTOList(specialNatList));
        }
        whatIf = whatIfService.createWhatIfCase(whatIfNatDTOList, whatIfRouteDTOList, whatIfCaseName, whatIfCaseName);
        if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
            log.info("????????????????????????UUID???:" + whatIf.getUuid());
            List<RecommendTaskEntity> tmpList = excelParser.getRecommendTaskEntity(natExcelList, whatIfCaseName, user, userInfoDTO);
            natTaskList.addAll(tmpList);
        } else {
            log.error("?????????????????????????????????" + JSONObject.toJSONString(natExcelList));
        }
        return whatIf;
    }

    /**
     * ???Nat????????????Excel???????????????WhatIfNatDTO??????
     *
     * @param natExcelList nat?????????????????????
     * @return WhatIfNatDTO??????
     */
    @Override
    public List<WhatIfNatDTO> getWhatIfNatDTOList(List<ExcelTaskNatEntity> natExcelList) {
        List<WhatIfNatDTO> whatIfNatDTOList = new ArrayList<>();
        for (ExcelTaskNatEntity entity : natExcelList) {
            WhatIfNatDTO whatIfNatDTO = new WhatIfNatDTO();
            NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(entity.getDeviceIp());
            if (nodeEntity == null) {
                log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", entity.getDeviceIp()));
                continue;
            }
            BeanUtils.copyProperties(entity, whatIfNatDTO);
            Integer ipType = IpTypeEnum.covertString2Int(entity.getIpType());
            String ipTypeParam = IPV4.getCode().equals(ipType) ? PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4 : PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6;
            whatIfNatDTO.setIpType(ipTypeParam);
            String deviceUuid = nodeEntity.getUuid();
            whatIfNatDTO.setDeviceUuid(deviceUuid);
            whatIfNatDTO.setName(entity.getTheme() + "-" + entity.getId());

            if (entity.getNatType().equals("STATIC")) {
                whatIfNatDTO.setNatType("STATIC");
                if (!AliStringUtils.isEmpty(entity.getPostSrcAddress())) {
                    whatIfNatDTO.setNatField("BI_DIR_SRC");
                } else if (!AliStringUtils.isEmpty(entity.getPostDstAddress())) {
                    whatIfNatDTO.setNatField("BI_DIR_DST");
                }
                if(IPV6.getCode().equals(ipType)){
                    whatIfNatDTO.setPreIp6DstAddress(entity.getPreDstAddress());
                    whatIfNatDTO.setPostIp6DstAddress(entity.getPostSrcAddress());
                }else{}
            } else if (entity.getNatType().equals("SNAT")) {
                whatIfNatDTO.setNatType("DYNAMIC");
                whatIfNatDTO.setNatField("SRC");
                whatIfNatDTO.setPostDstAddress(null);
                if(IPV6.getCode().equals(ipType)){
                    whatIfNatDTO.setPostIp6DstAddress(null);
                    whatIfNatDTO.setPostIp6SrcAddress(entity.getPostSrcAddress());
                }else {}
            } else if (entity.getNatType().equals("DNAT")) {
                whatIfNatDTO.setNatField("DST");
                whatIfNatDTO.setNatType("DYNAMIC");
                whatIfNatDTO.setPostSrcAddress(null);
                if(IPV6.getCode().equals(ipType)){
                    whatIfNatDTO.setPostIp6SrcAddress(null);
                    whatIfNatDTO.setPostIp6DstAddress(entity.getPostDstAddress());
                }else{}
            } else if (entity.getNatType().equals("BOTH")) {
                whatIfNatDTO.setNatField("BOTH");
                whatIfNatDTO.setNatType("DYNAMIC");
                if(IPV6.getCode().equals(ipType)){
                    whatIfNatDTO.setPostIp6SrcAddress(entity.getPostSrcAddress());
                    whatIfNatDTO.setPostIp6DstAddress(entity.getPostDstAddress());
                }else{}
            }
            whatIfNatDTOList.add(whatIfNatDTO);
        }

        return whatIfNatDTOList;
    }

    /**
     * ???Nat????????????Excel???????????????WhatIfNatDTO??????
     *
     * @param specialNatList nat?????????????????????
     * @return WhatIfNatDTO??????
     */
    @Override
    public List<WhatIfNatDTO> getWhatIfSpecialNatDTOList(List<RecommendRelevanceSceneDTO> specialNatList) {
        List<WhatIfNatDTO> whatIfNatDTOList = new ArrayList<>();
        for (RecommendRelevanceSceneDTO entity : specialNatList) {
            WhatIfNatDTO whatIfNatDTO = new WhatIfNatDTO();
            NodeEntity nodeEntity = policyRecommendTaskService.getDeviceByManageIp(entity.getDeviceIp());
            if (nodeEntity == null) {
                log.error(String.format("??????%s??????????????????????????????UUID???????????????WhatIfNatDTO??????...", entity.getDeviceIp()));
                continue;
            }
            BeanUtils.copyProperties(entity, whatIfNatDTO);
            Integer ipType = entity.getIpType();
            String ipTypeParam = IPV4.getCode().equals(ipType) ? PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4 : PolicyConstants.POLICY_STR_NETWORK_TYPE_IP6;
            whatIfNatDTO.setIpType(ipTypeParam);
            String deviceUuid = nodeEntity.getUuid();
            whatIfNatDTO.setDeviceUuid(deviceUuid);
            whatIfNatDTO.setName(entity.getName() + "-" + entity.getId());

            whatIfNatDTO.setNatField("BOTH");
            whatIfNatDTO.setNatType("DYNAMIC");

            if (IPV6.getCode().equals(ipType)) {
                whatIfNatDTO.setPreIp6DstAddress(entity.getDstIp());
                whatIfNatDTO.setPreIp6SrcAddress(entity.getSrcIp());
                whatIfNatDTO.setPostIp6SrcAddress(entity.getPostSrcIp());
                whatIfNatDTO.setPostIp6DstAddress(entity.getPostDstIp());
            } else {
                whatIfNatDTO.setPostSrcAddress(entity.getPostSrcIp());
                whatIfNatDTO.setPostDstAddress(entity.getPostDstIp());
            }

            String postPort = entity.getPostPort();
            List<ServiceDTO> serviceDTOList = entity.getServiceList();
            if (CollectionUtils.isNotEmpty(serviceDTOList) && StringUtils.isNotBlank(postPort)) {
                List<ServiceDTO> serviceDTOListPost = new ArrayList<>();

                for (ServiceDTO serviceDTO : serviceDTOList) {
                    ServiceDTO serviceDTO1 = new ServiceDTO();
                    serviceDTO1.setProtocol(serviceDTO.getProtocol());
                    serviceDTO1.setDstPorts(postPort);
                    serviceDTOListPost.add(serviceDTO1);
                }
                whatIfNatDTO.setPostServiceList(serviceDTOListPost);
            }

            whatIfNatDTOList.add(whatIfNatDTO);
        }

        return whatIfNatDTOList;
    }


    /**
     * ??????????????????DTO??????
     *
     * @param routeTaskList
     * @return
     */
    private List<WhatIfRouteDTO> getWhatIfRouteDTOList(List<PushRecommendStaticRoutingDTO> routeTaskList) {
        if (CollectionUtils.isEmpty(routeTaskList)) {
            return new ArrayList<>();
        }
        List<WhatIfRouteDTO> resultRouteDTO = new ArrayList<>();
        for (PushRecommendStaticRoutingDTO staticRoutingDTO : routeTaskList) {
            WhatIfRouteDTO routeDTO = new WhatIfRouteDTO();
            routeDTO.setName(staticRoutingDTO.getTheme());
            routeDTO.setDeviceUuid(staticRoutingDTO.getDeviceUuid());
            routeDTO.setRouteType(RoutingEntryTypeEnum.STATIC);
            routeDTO.setIpType(staticRoutingDTO.getIpType());
            routeDTO.setIpv4DstIp(staticRoutingDTO.getDstIp());
            routeDTO.setIpv6DstIp(staticRoutingDTO.getDstIp());
            routeDTO.setMaskLength(staticRoutingDTO.getSubnetMask());
            routeDTO.setInterfaceName(staticRoutingDTO.getOutInterface());
            routeDTO.setIp4Gateway(staticRoutingDTO.getNextHop());
            routeDTO.setIp6Gateway(staticRoutingDTO.getNextHop());
            routeDTO.setRoutingTableUuid(staticRoutingDTO.getSrcVirtualRouter());
            routeDTO.setDstRoutingTableUuid(staticRoutingDTO.getDstVirtualRouter());
            routeDTO.setDistance(StringUtils.isBlank(staticRoutingDTO.getPriority()) ? 0 : Integer.valueOf(staticRoutingDTO.getPriority()));
            resultRouteDTO.add(routeDTO);
        }
        return resultRouteDTO;
    }


    /**
     * ??????????????????  ??????ipv6??????????????????????????????????????????????????????????????????
     *
     * @param entity
     * @return
     */
    @Override
    public int checkParamForDstAddress(AddRecommendTaskEntity entity) {
        Integer ipType = entity.getIpType();
        //????????????????????????????????? ??????????????????
        int rc ;
        if(ipType != null && ipType == 1){
            //ipv6
            rc = InputValueUtils.checkIpV6(entity.getDstIp());
        }else {
            rc = InputValueUtils.checkIp(entity.getDstIp());
            //??????IP????????????????????????????????????????????????????????????
            if (rc == ReturnCode.INVALID_IP_RANGE) {
                entity.setDstIp(InputValueUtils.autoCorrect(entity.getDstIp()));
                rc = ReturnCode.POLICY_MSG_OK;
            }
        }
        if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
            String msg = "?????????????????????" + ReturnCode.getMsg(rc);
            throw new IllegalArgumentException(msg);
        }

        entity.setDstIp(InputValueUtils.formatIpAddress(entity.getDstIp()));

        return rc;
    }

    /**
     * ???????????????//????????????????????????????????? ?????????????????? ??????ipv6??????????????????????????????????????????????????????????????????
     *
     * @param entity
     * @return
     */
    @Override
    public int checkParamForSrcAddress(AddRecommendTaskEntity entity) {
        Integer ipType = entity.getIpType();
        //????????????????????????????????? ??????????????????
        int rc ;
        if(ipType != null && ipType == 1){
            //ipv6
            rc = InputValueUtils.checkIpV6(entity.getSrcIp());
        }else{
            rc = InputValueUtils.checkIp(entity.getSrcIp());
            //??????IP????????????????????????????????????????????????????????????
            if (rc == ReturnCode.INVALID_IP_RANGE) {
                entity.setSrcIp(InputValueUtils.autoCorrect(entity.getSrcIp()));
                rc = ReturnCode.POLICY_MSG_OK;
            }
        }

        if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE) {
            String msg = "??????????????????" + ReturnCode.getMsg(rc);
            throw new IllegalArgumentException(msg);
        }
        //??????IP???????????????????????????
        entity.setSrcIp(InputValueUtils.formatIpAddress(entity.getSrcIp()));

        return rc;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RecommendTaskEntity addAutoNatGenerate(AddRecommendTaskEntity entity, Authentication auth) throws IssuedExecutorException {
        RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
        BeanUtils.copyProperties(entity, recommendTaskEntity);
        recommendTaskEntity.setSrcIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getSrcIpSystem()));
        recommendTaskEntity.setDstIpSystem(TotemsStringUtils.trim2(recommendTaskEntity.getDstIpSystem()));

        //?????????????????????????????????????????????, ????????????????????????any
        recommendTaskEntity.setServiceList(entity.getServiceList() == null ? null : JSONObject.toJSONString(entity.getServiceList()));

        //????????????????????????????????????????????????
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber = "A" + simpleDateFormat.format(date);
        recommendTaskEntity.setCreateTime(date);
        recommendTaskEntity.setOrderNumber(orderNumber);

        UserInfoDTO userInfoDTO = remoteBranchService.findOne(auth.getName());
        if (userInfoDTO != null && org.apache.commons.lang3.StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
            recommendTaskEntity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            recommendTaskEntity.setBranchLevel("00");
        }
        //???????????????
        recommendTaskEntity.setUserName(auth.getName());

        if (entity.getIdleTimeout() != null) {
            recommendTaskEntity.setIdleTimeout(entity.getIdleTimeout() * HOUR_SECOND);
        } else {
            recommendTaskEntity.setIdleTimeout(null);
        }

        //???????????????????????????
        recommendTaskEntity.setTaskType(entity.getTaskType());
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_STATUS_INITIAL);

        // ???????????????????????????????????????????????????????????????????????????
        if(ObjectUtils.isEmpty(entity.getIsAutoMappingTask()) || !entity.getIsAutoMappingTask()){
            // ????????????????????????????????????????????????,?????????????????????nat,???????????????????????????nat??????
            int rc = this.checkPostRelevancyNat(recommendTaskEntity, auth);
            if (ReturnCode.NO_DEVICE_DETAIL == rc) {
                // ??????????????????????????????
                throw new IssuedExecutorException(SendErrorEnum.DEVICE_NOT_EXIST_FAIL);
            } else if (ReturnCode.POLICY_MSG_OK != rc) {
                // ?????????????????????????????? ????????????????????????????????????????????????
                throw new IssuedExecutorException(SendErrorEnum.NOT_FIND_NAT_ADDRESS_MAPPING);
            }
        }
        recommendTaskMapper.insert(recommendTaskEntity);
        // ????????????NAT??????????????????id
        this.updateRelevancyNatTaskId(recommendTaskEntity,false);

        return recommendTaskEntity;
    }


    /**
     * ????????????NAT??????????????????id
     * @param recommendTaskEntity
     */
    @Override
    public void updateRelevancyNatTaskId(RecommendTaskEntity recommendTaskEntity,boolean cleanTaskId) {
        if (StringUtils.isNotBlank(recommendTaskEntity.getRelevancyNat())) {
            JSONArray whatIfCaseArray = JSONObject.parseArray(recommendTaskEntity.getRelevancyNat());
            if (whatIfCaseArray != null && whatIfCaseArray.size() > 0) {
                for (int i = 0; i < whatIfCaseArray.size(); i++) {
                    JSONObject jsonObject = (JSONObject) whatIfCaseArray.get(i);
                    int specialRelevancyNat = jsonObject.getIntValue("type");
                    int id = jsonObject.getIntValue("taskId");

                    if (PolicyConstants.POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT == specialRelevancyNat) {
                        RecommendRelevanceSceneDTO sceneDTO = new RecommendRelevanceSceneDTO();
                        RecommendRelevanceSceneDTO relevanceSceneDTO = recommendRelevanceSceneService.queryById(id);
                        if (null == relevanceSceneDTO) {
                            continue;
                        }

                        String taskIds = relevanceSceneDTO.getTaskId();

                        if (StringUtils.isBlank(taskIds)) {
                            sceneDTO.setTaskId(null == recommendTaskEntity.getId() ? "" : recommendTaskEntity.getId().toString());
                        } else {
                            if (cleanTaskId) {
                                String[] relevanceTaskIds = taskIds.split(PolicyConstants.ADDRESS_SEPERATOR);
                                StringBuffer sb = new StringBuffer();
                                for (String taskId : relevanceTaskIds) {
                                    if (!taskId.equals(recommendTaskEntity.getId().toString())) {
                                        sb.append(taskId).append(PolicyConstants.ADDRESS_SEPERATOR);
                                    }
                                }
                                if (sb.length() > 0) {
                                    sb.deleteCharAt(sb.lastIndexOf(PolicyConstants.ADDRESS_SEPERATOR));
                                }
                                sceneDTO.setTaskId(sb.toString());
                            } else {
                                sceneDTO.setTaskId(taskIds + "," + recommendTaskEntity.getId());
                            }
                        }
                        sceneDTO.setId(id);
                        recommendRelevanceSceneService.updateRelevanceSceneTaskId(sceneDTO);
                    }
                }
            }
        }
    }

    /**
     * ???????????????????????????nat?????????
     * @param recommendTaskEntity
     */
    @Override
    public int checkPostRelevancyNat(RecommendTaskEntity recommendTaskEntity, Authentication auth) {
        Map<String,Object> paramMap = new HashMap<>();
        // ?????????????????? ??????????????????????????????????????????????????????????????????,??????????????????????????????nat?????????
        if (StringUtils.isBlank(recommendTaskEntity.getPostSrcIp())
            && StringUtils.isBlank(recommendTaskEntity.getPostDstIp())) {
            // ???????????????????????????
            log.info("??????:{}??????,???????????????????????????,??????nat????????????", recommendTaskEntity.getTheme());
            return ReturnCode.POLICY_MSG_OK;
        }
        List<PushMappingNatEntity> pushMappingNatEntities =
            pushMappingNatMapper.listPushMappingNatInfo(paramMap);
        String postSrcIp = recommendTaskEntity.getPostSrcIp();
        String srcIp = recommendTaskEntity.getSrcIp();
        String dstIp = recommendTaskEntity.getDstIp();
        String postDstIp = recommendTaskEntity.getPostDstIp();
        if (CollectionUtils.isEmpty(pushMappingNatEntities)) {
            // ?????????????????????????????????????????????nat??????????????????
            log.info("????????????????????????,????????????????????????nat??????,??????????????????nat??????");
            return ReturnCode.NO_NAT_MAPPING_ADDRESS;
        }

        // ?????????????????????nat
        List<TwoMemberObject<Long, Long>> infoSrcIpList = getListMemberByIp(srcIp);
        List<TwoMemberObject<Long, Long>> infoSrcPostList = getListMemberByIp(postSrcIp);
        // ????????????????????????nat
        List<TwoMemberObject<Long, Long>> infoDstIpList = getListMemberByIp(dstIp);
        List<TwoMemberObject<Long, Long>> infoPostDstList = getListMemberByIp(postDstIp);

        // ???????????????snat??????dnat??????bothnat
        String natFlag = null;
        if (StringUtils.isNotBlank(srcIp) && StringUtils.isNotBlank(dstIp) && StringUtils.isNotBlank(postSrcIp)
            && StringUtils.isNotBlank(postDstIp)) {
            natFlag = NatTypeEnum.BOTH.getNatField();
        } else if (StringUtils.isNotBlank(srcIp) && StringUtils.isNotBlank(dstIp)
            && StringUtils.isNotBlank(postSrcIp)) {
            natFlag = NatTypeEnum.SRC.getNatField();
        } else if (StringUtils.isNotBlank(srcIp) && StringUtils.isNotBlank(dstIp)
            && StringUtils.isNotBlank(postDstIp)) {
            natFlag = NatTypeEnum.DST.getNatField();
        }

        boolean srcOpMatch = false;
        boolean dstOpMatch = false;
        Map<String, String> param = new HashMap<>();
        for (PushMappingNatEntity pushMappingNatEntity : pushMappingNatEntities) {
            String preIp = pushMappingNatEntity.getPreIp();
            String postIp = pushMappingNatEntity.getPostIp();

            List<TwoMemberObject<Long, Long>> conditionPostSrcContainList = getListMemberByIp(postIp);
            List<TwoMemberObject<Long, Long>> conditionPreSrcIpContainList = getListMemberByIp(preIp);

            // ??????????????????nat??????
            if (NatTypeEnum.SRC.getNatField().equals(natFlag)) {
                srcOpMatch = IpMatchUtil.rangeOpMatch(conditionPreSrcIpContainList, infoSrcIpList,
                    SearchRangeOpEnum.CONTAINED_BY);
                if (srcOpMatch) {
                    srcOpMatch = IpMatchUtil.rangeOpMatch(conditionPostSrcContainList, infoSrcPostList,
                        SearchRangeOpEnum.CONTAINED_BY);
                }
                // ???????????????
                if (srcOpMatch) {
                    buildParamMap(param, pushMappingNatEntity);
                    break;
                }
            } else if (NatTypeEnum.DST.getNatField().equals(natFlag)) {
                // ?????????????????????nat??????
                dstOpMatch = IpMatchUtil.rangeOpMatch(conditionPreSrcIpContainList, infoDstIpList,
                    SearchRangeOpEnum.CONTAINED_BY);
                if (dstOpMatch) {
                    dstOpMatch = IpMatchUtil.rangeOpMatch(conditionPostSrcContainList, infoPostDstList,
                        SearchRangeOpEnum.CONTAINED_BY);
                }
                // ??????????????????
                if (dstOpMatch) {
                    buildParamMap(param, pushMappingNatEntity);
                    break;
                }
            } else if (NatTypeEnum.BOTH.getNatField().equals(natFlag)) {
                // ?????????????????????nat??????
                srcOpMatch = IpMatchUtil.rangeOpMatch(conditionPreSrcIpContainList, infoSrcIpList,
                    SearchRangeOpEnum.CONTAINED_BY);
                if (srcOpMatch) {
                    srcOpMatch = IpMatchUtil.rangeOpMatch(conditionPostSrcContainList, infoSrcPostList,
                        SearchRangeOpEnum.CONTAINED_BY);
                }
                dstOpMatch = IpMatchUtil.rangeOpMatch(conditionPreSrcIpContainList, infoDstIpList,
                    SearchRangeOpEnum.CONTAINED_BY);
                if (dstOpMatch) {
                    dstOpMatch = IpMatchUtil.rangeOpMatch(conditionPostSrcContainList, infoPostDstList,
                        SearchRangeOpEnum.CONTAINED_BY);
                }
                // ??????????????? ?????????????????????????????????
                if (srcOpMatch && dstOpMatch) {
                    buildParamMap(param, pushMappingNatEntity);
                    break;
                }else{
                    // ?????????????????????????????????bothNat??????????????? ??????????????????????????????????????????????????????false??????????????????????????????nat????????????nat
                    srcOpMatch = false;
                    dstOpMatch = false;
                }
            }
        }

        // ??????????????????,???????????????nat???????????????????????????????????????nat???????????????
        int rc = insertNatPolicyAndBuildRelevancyNat(recommendTaskEntity, auth, srcOpMatch, dstOpMatch, param);
        return rc;
    }

    /**
     * ????????????map
     * @param param
     * @param pushMappingNatEntity
     */
    private void buildParamMap(Map<String, String> param, PushMappingNatEntity pushMappingNatEntity) {
        param.put(CommonConstants.DEVICE_UUID, pushMappingNatEntity.getDeviceUuid());
        param.put(CommonConstants.SRC_ZONE, pushMappingNatEntity.getSrcZone());
        param.put(CommonConstants.SRC_ITF, pushMappingNatEntity.getInDevIf());
        param.put(CommonConstants.DST_ZONE, pushMappingNatEntity.getDstZone());
        param.put(CommonConstants.DST_ITF, pushMappingNatEntity.getOutDevIf());
    }

    /**
     * ??????????????????,???????????????nat???????????????????????????????????????nat???????????????
     * @param recommendTaskEntity
     * @param auth
     * @param srcOpMatch
     * @param dstOpMatch
     * @param param
     */
    private int insertNatPolicyAndBuildRelevancyNat(RecommendTaskEntity recommendTaskEntity, Authentication auth,
        boolean srcOpMatch, boolean dstOpMatch, Map<String,String> param) {
        if (srcOpMatch && dstOpMatch) {
            // ?????????????????????????????????????????????
            NodeEntity node = policyRecommendNodeMapper.getTheNodeByUuid(param.get(CommonConstants.DEVICE_UUID));
            if (node == null) {
                log.info("????????????uuid:{}????????????????????????!", param.get(CommonConstants.DEVICE_UUID));
                return ReturnCode.NO_DEVICE_DETAIL;
            }
            // both nat
            NatPolicyDTO natPolicyDTO = new NatPolicyDTO();
            natPolicyDTO.setDeviceUuid(param.get(CommonConstants.DEVICE_UUID));
            natPolicyDTO.setSrcZone(param.get(CommonConstants.SRC_ZONE));
            natPolicyDTO.setSrcItf(param.get(CommonConstants.SRC_ITF));
            natPolicyDTO.setDstZone(param.get(CommonConstants.DST_ZONE));
            natPolicyDTO.setDstItf(param.get(CommonConstants.DST_ITF));
            natPolicyDTO.setSrcIp(recommendTaskEntity.getSrcIp());
            natPolicyDTO.setDstIp(recommendTaskEntity.getDstIp());
            natPolicyDTO.setPostSrcIp(recommendTaskEntity.getPostSrcIp());
            natPolicyDTO.setPostDstIp(recommendTaskEntity.getPostDstIp());
            natPolicyDTO.setTheme(recommendTaskEntity.getTheme());
            // ?????????null?????????JSON.toJsonString ??????null????????????????????????????????????????????????????????????????????????????????????????????????????????????[]
            natPolicyDTO.setServiceList(new ArrayList<>());
            natPolicyDTO.setPostPort("");
            policyRecommendTaskService.insertBothNatPolicy(natPolicyDTO, auth);
            // ????????????nat????????????
            buildRelevancyNat(recommendTaskEntity, node, natPolicyDTO.getTaskId(), natPolicyDTO.getTaskId(),
                NatTypeEnum.BOTH.getTypeCode());
            return ReturnCode.POLICY_MSG_OK;
        } else if (srcOpMatch) {
            // ???nat
            // ?????????????????????????????????????????????
            NodeEntity node = policyRecommendNodeMapper.getTheNodeByUuid(param.get(CommonConstants.DEVICE_UUID));
            if (node == null) {
                log.info("????????????uuid:{}????????????????????????!", param.get(CommonConstants.DEVICE_UUID));
                return ReturnCode.NO_DEVICE_DETAIL;
            }
            SNatPolicyDTO sNatPolicyDTO = new SNatPolicyDTO();
            sNatPolicyDTO.setDeviceUuid(param.get(CommonConstants.DEVICE_UUID));
            sNatPolicyDTO.setSrcZone(param.get(CommonConstants.SRC_ZONE));
            sNatPolicyDTO.setSrcItf(param.get(CommonConstants.SRC_ITF));
            sNatPolicyDTO.setDstZone(param.get(CommonConstants.DST_ZONE));
            sNatPolicyDTO.setDstItf(param.get(CommonConstants.DST_ITF));
            sNatPolicyDTO.setSrcIp(recommendTaskEntity.getSrcIp());
            sNatPolicyDTO.setDstIp(recommendTaskEntity.getDstIp());
            sNatPolicyDTO.setPostIpAddress(recommendTaskEntity.getPostSrcIp());
            sNatPolicyDTO.setTheme(recommendTaskEntity.getTheme());
            sNatPolicyDTO.setServiceList(new ArrayList<>());
            policyRecommendTaskService.insertSrcNatPolicy(sNatPolicyDTO, auth);
            // ????????????nat????????????
            buildRelevancyNat(recommendTaskEntity, node, sNatPolicyDTO.getTaskId(), sNatPolicyDTO.getTaskId(),
                NatTypeEnum.SRC.getTypeCode());
            return ReturnCode.POLICY_MSG_OK;
        } else if (dstOpMatch) {
            // ??????nat
            // ?????????????????????????????????????????????
            NodeEntity node = policyRecommendNodeMapper.getTheNodeByUuid(param.get(CommonConstants.DEVICE_UUID));
            if (node == null) {
                log.info("????????????uuid:{}????????????????????????!", param.get(CommonConstants.DEVICE_UUID));
                return ReturnCode.NO_DEVICE_DETAIL;
            }
            DNatPolicyDTO dNatPolicyDTO = new DNatPolicyDTO();
            dNatPolicyDTO.setDeviceUuid(param.get(CommonConstants.DEVICE_UUID));
            dNatPolicyDTO.setSrcZone(param.get(CommonConstants.SRC_ZONE));
            dNatPolicyDTO.setSrcItf(param.get(CommonConstants.SRC_ITF));
            dNatPolicyDTO.setDstZone(param.get(CommonConstants.DST_ZONE));
            dNatPolicyDTO.setDstItf(param.get(CommonConstants.DST_ITF));
            dNatPolicyDTO.setSrcIp(recommendTaskEntity.getSrcIp());
            dNatPolicyDTO.setDstIp(recommendTaskEntity.getDstIp());
            dNatPolicyDTO.setPostIpAddress(recommendTaskEntity.getPostDstIp());
            dNatPolicyDTO.setTheme(recommendTaskEntity.getTheme());
            dNatPolicyDTO.setServiceList(new ArrayList<>());
            dNatPolicyDTO.setPostPort("");
            policyRecommendTaskService.insertDstNatPolicy(dNatPolicyDTO, auth);
            // ????????????nat????????????
            buildRelevancyNat(recommendTaskEntity, node, dNatPolicyDTO.getTaskId(), dNatPolicyDTO.getTaskId(),
                NatTypeEnum.DST.getTypeCode());
            return ReturnCode.POLICY_MSG_OK;
        } else {
            // ??????????????????????????????
            log.info("????????????????????????nat??????,??????????????????nat??????");
            return ReturnCode.NO_NAT_MAPPING_ADDRESS;
        }
    }

    /**
     * ????????????nat????????????
     * 
     * @param recommendTaskEntity
     * @param node
     * @param id
     * @param taskId
     * @param typeCode
     */
    private void buildRelevancyNat(RecommendTaskEntity recommendTaskEntity, NodeEntity node, Integer id, Integer taskId,
        Integer typeCode) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        String name = String.format("%s(%s(%s))", recommendTaskEntity.getTheme(), node.getDeviceName(), node.getIp());
        jsonObject.put("name", name);
        jsonObject.put("id", id);
        jsonObject.put("taskId", taskId);
        jsonObject.put("type", typeCode);
        // ??????????????????????????????nat???????????????????????????????????????????????????
        jsonObject.put("flag","auto");
        String indexField = "index";
        // ????????????????????????????????????nat
        if (StringUtils.isBlank(recommendTaskEntity.getRelevancyNat())) {
            jsonObject.put(indexField, 1);
            jsonArray.add(jsonObject);
            recommendTaskEntity.setRelevancyNat(jsonArray.toJSONString());
        } else {
            // ?????????????????????????????????nat??????????????????????????????????????????jsonObject??????
            JSONArray whatIfCaseArray = JSONObject.parseArray(recommendTaskEntity.getRelevancyNat());
            if (0 == whatIfCaseArray.size()) {
                jsonObject.put(indexField, 1);
                jsonArray.add(jsonObject);
                recommendTaskEntity.setRelevancyNat(jsonArray.toJSONString());
            } else {
                int newIndex = whatIfCaseArray.size() + 1;
                jsonObject.put(indexField, newIndex);
                whatIfCaseArray.add(jsonObject);
                recommendTaskEntity.setRelevancyNat(whatIfCaseArray.toJSONString());
            }
        }
    }

    /**
     * ???ip????????????????????????
     * @param ip
     * @return
     */
    private static List<TwoMemberObject<Long,Long>> getListMemberByIp(String ip){
        List<TwoMemberObject<Long,Long>> infoList = new ArrayList<>();
        if(StringUtils.isNotBlank(ip)){
            //?????????nat
            String[] ips = ip.split(",");
            for (String  address : ips) {
                TwoMemberObject<Long, Long>  twoMemberObject = IpMatchUtil.commonConditionList(address);
                infoList.add(twoMemberObject);
            }
        }
        return infoList;
    }

    @Override
    public Boolean autoStartRecommend(Boolean autoStartRecommend, List<RecommendTaskEntity> recommendTaskEntityList, Authentication auth) {
        if(autoStartRecommend != null && autoStartRecommend){
            List<SimulationTaskDTO> taskDtoList = new ArrayList<>();
            for (RecommendTaskEntity taskEntity : recommendTaskEntityList) {
                SimulationTaskDTO taskDTO = new SimulationTaskDTO();
                BeanUtils.copyProperties(taskEntity, taskDTO);
                if (taskEntity.getWhatIfCase() != null) {
                    //2????????????????????????????????????????????????
                    taskDTO.setWhatIfCaseUuid(taskEntity.getWhatIfCase());
                }
                //??????????????????
                if (taskEntity.getServiceList() == null) {
                    taskDTO.setServiceList(null);
                } else {
                    JSONArray array = JSONArray.parseArray(taskEntity.getServiceList());
                    List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                    taskDTO.setServiceList(serviceList);
                }
                // api????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????
                if (taskEntity.getTaskType() != null && BIG_INTERNET_RECOMMEND == taskEntity.getTaskType()) {
                    taskDTO.setDeviceWhatifs(new JSONObject());
                }
                WhatIfRO whatIf = this.createWhatIfCaseUuid(taskEntity);
                if (whatIf != null && !AliStringUtils.isEmpty(whatIf.getUuid())) {
                    log.info("????????????????????????UUID???:" + whatIf.getUuid());
                    taskDTO.setWhatIfCaseUuid(whatIf.getUuid());
                    taskDTO.setDeviceWhatifs(whatIf.getDeviceWhatifs());
                } else {
                    log.error("?????????????????????????????????" + taskEntity.getRelevancyNat());
                }
                taskDtoList.add(taskDTO);
                policyRecommendTaskService.updateTaskStatus(taskDTO.getId(), PolicyConstants.POLICY_INT_STATUS_SIMULATION_QUEUE);
            }

            recommendTaskManager.addSimulationTaskList(taskDtoList,auth);
            return true;
        }
        return false;
    }

    @Override
    public List<PushRecommendStaticRoutingDTO> getRouteExcelDTO(List<ExcelTaskStaticRouteEntity> routeExcelList, String user, UserInfoDTO userInfoDTO) {
        List<PushRecommendStaticRoutingDTO> routeDTOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(routeExcelList)) {
            routeDTOs = excelParser.getRouteTaskEntity(routeExcelList, user, userInfoDTO);
        }
        return routeDTOs;
    }

    @Override
    public List<RecommendRelevanceSceneDTO> getSpecialNatExcelDTO(List<ExcelTaskSpecialNatEntity> specialNatEntityList, String user, UserInfoDTO userInfoDTO) {
        List<RecommendRelevanceSceneDTO> specialNatDTOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(specialNatEntityList)){
            specialNatDTOs = excelParser.getSpecialNatDTO(specialNatEntityList, user, userInfoDTO);
        }
        return specialNatDTOs;
    }

    @Override
    public void updateRelevanceNatTaskId(List<RecommendTaskEntity> tmpList) {
        if (CollectionUtils.isEmpty(tmpList)) {
            return;
        }

        for (RecommendTaskEntity recommendTaskEntity : tmpList) {
            updateRelevancyNatTaskId(recommendTaskEntity,false);
        }
    }
}
