package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendRelevanceSceneMapper;
import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import com.abtnetworks.totems.recommend.entity.NatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendRelevanceSceneEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.RecommendRelevanceSceneService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;

/**
 * @author lifei
 * @desc 策略建议关联场景服务类
 * @date 2021/12/27 9:57
 */
@Service
@Slf4j
public class RecommendRelevanceSceneServiceImpl implements RecommendRelevanceSceneService {

    @Autowired
    RecommendRelevanceSceneMapper recommendRelevanceSceneMapper;
    @Autowired
    NodeMapper policyRecommendNodeMapper;
    @Autowired
    RemoteBranchService remoteBranchService;
    @Autowired
    private LogClientSimple logClientSimple;
    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Override
    public int createRecommendRelevanceScene(RecommendRelevanceSceneDTO sceneDTO) {
        //检测设备对象是否存在
        String deviceUuid = sceneDTO.getDeviceUuid();
        NodeEntity node = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return ReturnCode.DEVICE_NOT_EXIST;
        }

        //获取用户名
        String userName = sceneDTO.getCreateUser();

        //格式化域信息
        sceneDTO.setSrcZone(getZone(sceneDTO.getSrcZone()));
        sceneDTO.setDstZone(getZone(sceneDTO.getDstZone()));

        //创建双向NAT附加信息数据对象
        NatAdditionalInfoEntity additionalInfoEntity = new NatAdditionalInfoEntity(null, sceneDTO.getPostSrcIp(),
                sceneDTO.getPostDstIp(), sceneDTO.getPostPort(), deviceUuid, sceneDTO.getSrcZone(), sceneDTO.getDstZone(),
                sceneDTO.getDstItf(), sceneDTO.getSrcItf(), false, null, null);
        sceneDTO.setIpType(ObjectUtils.isNotEmpty(sceneDTO.getIpType()) ? sceneDTO.getIpType() : IPV4.getCode());

        sceneDTO.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        sceneDTO.setServiceListJson(JSONObject.toJSONString(sceneDTO.getServiceList()));

        RecommendRelevanceSceneEntity recommendRelevanceSceneEntity = RecommendRelevanceSceneEntity.convertEntity(sceneDTO);
        getBranch(userName, recommendRelevanceSceneEntity);

        int count = recommendRelevanceSceneMapper.add(recommendRelevanceSceneEntity);
        sceneDTO.setId(recommendRelevanceSceneEntity.getId());
        sceneDTO.setTaskId(recommendRelevanceSceneEntity.getId().toString());

        String message = String.format("新建飞塔NAT场景:%s%s", sceneDTO.getName(), count > 0 ? "成功" : "失败");
        //添加操作日志
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int updateRecommendRelevanceScene(RecommendRelevanceSceneDTO sceneDTO) {
        //检测设备对象是否存在
        String deviceUuid = sceneDTO.getDeviceUuid();
        NodeEntity node = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
        if (node == null) {
            return ReturnCode.DEVICE_NOT_EXIST;
        }

        //获取用户名
        String userName = sceneDTO.getCreateUser();

        //格式化域信息
        sceneDTO.setSrcZone(getZone(sceneDTO.getSrcZone()));
        sceneDTO.setDstZone(getZone(sceneDTO.getDstZone()));

        //创建双向NAT附加信息数据对象
        NatAdditionalInfoEntity additionalInfoEntity = new NatAdditionalInfoEntity(null, sceneDTO.getPostSrcIp(),
                sceneDTO.getPostDstIp(), sceneDTO.getPostPort(), deviceUuid, sceneDTO.getSrcZone(), sceneDTO.getDstZone(),
                sceneDTO.getDstItf(), sceneDTO.getSrcItf(), false, null, null);
        sceneDTO.setIpType(ObjectUtils.isNotEmpty(sceneDTO.getIpType()) ? sceneDTO.getIpType() : IPV4.getCode());

        sceneDTO.setAdditionInfo(JSONObject.toJSONString(additionalInfoEntity));
        sceneDTO.setServiceListJson(JSONObject.toJSONString(sceneDTO.getServiceList()));

        RecommendRelevanceSceneEntity recommendRelevanceSceneEntity = RecommendRelevanceSceneEntity.convertEntity(sceneDTO);
        getBranch(userName, recommendRelevanceSceneEntity);

        int count = recommendRelevanceSceneMapper.update(recommendRelevanceSceneEntity);
        String message = String.format("编辑飞塔NAT场景:%s%s", sceneDTO.getName(), count > 0 ? "成功" : "失败");
        //添加操作日志
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return ReturnCode.POLICY_MSG_OK;
    }


    @Override
    public int updateRelevanceSceneTaskId(RecommendRelevanceSceneDTO sceneDTO) {
        RecommendRelevanceSceneEntity recommendRelevanceSceneEntity = RecommendRelevanceSceneEntity.convertEntity(sceneDTO);
        int count = recommendRelevanceSceneMapper.update(recommendRelevanceSceneEntity);
        String message = String.format("编辑飞塔NAT场景:%s%s", sceneDTO.getName(), count > 0 ? "成功" : "失败");
        //添加操作日志
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);
        return ReturnCode.POLICY_MSG_OK;
    }



    @Override
    public PageInfo<RecommendRelevanceSceneDTO> getRecommendRelevanceScene(int page, int psize, String ids, String name, String deviceUuid, String userName) {
        String branchLevel = remoteBranchService.likeBranch(userName);

        List<RecommendRelevanceSceneEntity> entityList = searchSceneList(ids, name, branchLevel, deviceUuid, page, psize);
        if (CollectionUtils.isEmpty(entityList)) {
            return new PageInfo<>();
        }
        PageInfo<RecommendRelevanceSceneEntity> originalPageInfo = new PageInfo<>(entityList);

        List<RecommendRelevanceSceneDTO> policyList = new ArrayList<>();

        for (RecommendRelevanceSceneEntity entity : entityList) {
            RecommendRelevanceSceneDTO relevanceSceneDTO = new RecommendRelevanceSceneDTO();
            BeanUtils.copyProperties(entity, relevanceSceneDTO);
            JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
            if (object != null) {
                NatAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(NatAdditionalInfoEntity.class);

                relevanceSceneDTO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getSrcItf()));
                relevanceSceneDTO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getDstItf()));

                relevanceSceneDTO.setPreSrcIp(entity.getSrcIp());
                relevanceSceneDTO.setPostSrcIp(additionalInfoEntity.getPostSrcIp());

                relevanceSceneDTO.setPreDstIp(entity.getDstIp());
                relevanceSceneDTO.setPostDstIp(additionalInfoEntity.getPostDstIp());

                String queryDeviceUuid = additionalInfoEntity.getDeviceUuid();
                String deviceIp = String.format("未知设备(%s)", queryDeviceUuid);
                if (queryDeviceUuid != null) {
                    NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(queryDeviceUuid);
                    if (nodeEntity != null) {
                        deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                    }
                }
                relevanceSceneDTO.setDeviceIp(deviceIp);
                relevanceSceneDTO.setPostPort(additionalInfoEntity.getPostPort());
                relevanceSceneDTO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getSrcItf()));
                relevanceSceneDTO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getDstItf()));

                List<ServiceDTO> postServiceList = new ArrayList<>();

                if (StringUtils.isNotBlank(entity.getServiceList())) {
                    JSONArray array = JSONObject.parseArray(entity.getServiceList());
                    List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                    if (serviceList.size() > 0) {
                        relevanceSceneDTO.setServiceList(serviceList);
                        List<ServiceDTO> preServiceList = new ArrayList<>();

                        for (ServiceDTO serviceDTO : serviceList) {
                            ServiceDTO postService = new ServiceDTO();
                            ServiceDTO preService = new ServiceDTO();
                            String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                            postService.setProtocol(protocol);
                            preService.setProtocol(protocol);
                            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                                postService.setDstPorts(additionalInfoEntity.getPostPort());
                            }
                            preService.setDstPorts(serviceDTO.getDstPorts());
                            preServiceList.add(preService);
                            postServiceList.add(postService);
                        }
                        relevanceSceneDTO.setServiceListJson(JSONObject.toJSONString(preServiceList));
                    } else {
                        ServiceDTO postService = new ServiceDTO();
                        postService.setProtocol("any");
                        postService.setDstPorts(additionalInfoEntity.getPostPort());
                        postServiceList.add(postService);
                    }
                    relevanceSceneDTO.setPostService(JSONObject.toJSONString(postServiceList));
                } else {
                    ServiceDTO postService = new ServiceDTO();
                    postService.setProtocol("any");
                    postService.setDstPorts(additionalInfoEntity.getPostPort());
                    postServiceList.add(postService);
                    relevanceSceneDTO.setPostService(JSONObject.toJSONString(postServiceList));
                }

            }
            policyList.add(relevanceSceneDTO);
        }
        PageInfo<RecommendRelevanceSceneDTO> pageInfo = new PageInfo<>(policyList);
        pageInfo.setTotal(originalPageInfo.getTotal());
        pageInfo.setStartRow(originalPageInfo.getStartRow());
        pageInfo.setEndRow(originalPageInfo.getEndRow());
        pageInfo.setPageSize(originalPageInfo.getPageSize());
        pageInfo.setPageNum(originalPageInfo.getPageNum());
        return pageInfo;
    }



    @Override
    public RecommendRelevanceSceneDTO queryById(Integer id) {
        RecommendRelevanceSceneEntity entity = recommendRelevanceSceneMapper.selectSceneById(id);
        if (null == entity) {
            return null;
        }
        RecommendRelevanceSceneDTO relevanceSceneDTO = new RecommendRelevanceSceneDTO();
        BeanUtils.copyProperties(entity, relevanceSceneDTO);

        JSONObject object = JSONObject.parseObject(entity.getAdditionInfo());
        if (object != null) {
            NatAdditionalInfoEntity additionalInfoEntity = object.toJavaObject(NatAdditionalInfoEntity.class);

            relevanceSceneDTO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getSrcItf()));
            relevanceSceneDTO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getDstItf()));

            relevanceSceneDTO.setSrcZone(additionalInfoEntity.getSrcZone());
            relevanceSceneDTO.setDstZone(additionalInfoEntity.getDstZone());
            relevanceSceneDTO.setSrcItf(additionalInfoEntity.getSrcItf());
            relevanceSceneDTO.setDstItf(additionalInfoEntity.getDstItf());


            relevanceSceneDTO.setPreSrcIp(entity.getSrcIp());
            relevanceSceneDTO.setPostSrcIp(additionalInfoEntity.getPostSrcIp());

            relevanceSceneDTO.setPreDstIp(entity.getDstIp());
            relevanceSceneDTO.setPostDstIp(additionalInfoEntity.getPostDstIp());
            String deviceUuid = additionalInfoEntity.getDeviceUuid();
            String deviceIp = String.format("未知设备(%s)", deviceUuid);
            if (deviceUuid != null) {
                NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
                if (nodeEntity != null) {
                    deviceIp = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                }
            }
            relevanceSceneDTO.setDeviceUuid(deviceUuid);
            relevanceSceneDTO.setDeviceIp(deviceIp);
            relevanceSceneDTO.setPostPort(additionalInfoEntity.getPostPort());
            relevanceSceneDTO.setSrcDomain(formatZoneItfString(additionalInfoEntity.getSrcZone(), additionalInfoEntity.getSrcItf()));
            relevanceSceneDTO.setDstDomain(formatZoneItfString(additionalInfoEntity.getDstZone(), additionalInfoEntity.getDstItf()));
            List<ServiceDTO> postServiceList = new ArrayList<>();
            if (StringUtils.isNotBlank(entity.getServiceList())) {
                JSONArray array = JSONObject.parseArray(entity.getServiceList());
                List<ServiceDTO> serviceList = array.toJavaList(ServiceDTO.class);
                if (serviceList.size() > 0) {
                    relevanceSceneDTO.setServiceList(serviceList);
                    List<ServiceDTO> preServiceList = new ArrayList<>();
                    for (ServiceDTO serviceDTO : serviceList) {
                        ServiceDTO postService = new ServiceDTO();
                        ServiceDTO preService = new ServiceDTO();
                        String protocol = ProtocolUtils.getProtocolByString(serviceDTO.getProtocol());
                        preService.setProtocol(protocol);
                        postService.setProtocol(protocol);
                        if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                            postService.setDstPorts(additionalInfoEntity.getPostPort());
                        }
                        preService.setDstPorts(serviceDTO.getDstPorts());
                        preServiceList.add(preService);
                        postServiceList.add(postService);
                    }
                    relevanceSceneDTO.setServiceListJson(JSONObject.toJSONString(preServiceList));
                } else {
                    ServiceDTO postService = new ServiceDTO();
                    postService.setProtocol("any");
                    postService.setDstPorts(additionalInfoEntity.getPostPort());
                    postServiceList.add(postService);
                }
                relevanceSceneDTO.setPostService(JSONObject.toJSONString(postServiceList));
            } else {
                ServiceDTO postService = new ServiceDTO();
                postService.setProtocol("any");
                postService.setDstPorts(additionalInfoEntity.getPostPort());
                postServiceList.add(postService);
                relevanceSceneDTO.setPostService(JSONObject.toJSONString(postServiceList));
            }
        }
        return relevanceSceneDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRecommendRelevanceScene(String ids) {
        List<RecommendRelevanceSceneEntity> relevanceSceneEntities = recommendRelevanceSceneMapper.selectSceneByIds(ids);
        if(CollectionUtils.isEmpty(relevanceSceneEntities)){
            return ReturnCode.POLICY_MSG_OK;
        }
        for (RecommendRelevanceSceneEntity sceneEntity : relevanceSceneEntities) {
            if (StringUtils.isBlank(sceneEntity.getTaskId())) {
                continue;
            }
            String[] taskIds = sceneEntity.getTaskId().split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String taskId : taskIds) {
                RecommendTaskEntity recommendEntity = policyRecommendTaskService.getRecommendTaskByTaskId(Integer.valueOf(taskId));
                String relevancyNat = Optional.ofNullable(recommendEntity).map(u -> u.getRelevancyNat()).orElseGet(() -> "");
                if (StringUtils.isBlank(relevancyNat)) {
                    continue;
                }
                JSONArray whatIfCaseArray = JSONObject.parseArray(relevancyNat);

                Iterator<Object> itemObj = whatIfCaseArray.iterator();
                while (itemObj.hasNext()) {
                    JSONObject itemObject = (JSONObject) itemObj.next();
                    int specialRelevancyNat = itemObject.getIntValue("type");
                    String itemTaskId = null == itemObject.getInteger("taskId") ? "" : itemObject.getInteger("taskId").toString();
                    if (PolicyConstants.POLICY_INT_PUSH_RELEVANCY_SPECIAL_NAT == specialRelevancyNat) {
                        if (ids.contains(itemTaskId)) {
                            itemObj.remove();
                        }
                    }
                }
                RecommendTaskEntity updateTaskEntity = new RecommendTaskEntity();
                updateTaskEntity.setId(recommendEntity.getId());
                updateTaskEntity.setRelevancyNat(whatIfCaseArray.toJSONString());
                policyRecommendTaskService.updateTaskByEntity(updateTaskEntity);
            }
        }
        recommendRelevanceSceneMapper.deleteSceneById(ids);
        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 处理分支
     * @param userName
     * @param entity
     */
    private void getBranch(String userName, RecommendRelevanceSceneEntity entity) {
        UserInfoDTO userInfoDTO = remoteBranchService.findOne(userName);
        if (userInfoDTO != null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())) {
            entity.setBranchLevel(userInfoDTO.getBranchLevel());
        } else {
            entity.setBranchLevel("00");
        }
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
     * 转换域和接口关系
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
     * 组装查询nat场景集合
     *
     * @param taskIds
     * @param branchLevel
     * @param deviceUuid
     * @param page
     * @param psize
     * @return
     */
    private List<RecommendRelevanceSceneEntity> searchSceneList(String taskIds,String name, String branchLevel, String deviceUuid, Integer page, Integer psize) {
        PageHelper.startPage(page, psize);
        Map<String, Object> params = new HashMap<>(10);
        if (StringUtils.isNotBlank(name)) {
            params.put("name", name);
        }
        if (StringUtils.isNotBlank(branchLevel)) {
            params.put("branchLevel", branchLevel);
        }
        if (!AliStringUtils.isEmpty(deviceUuid)) {
            params.put("deviceUuid", deviceUuid);
        }
        if (!AliStringUtils.isEmpty(taskIds)) {
            params.put("taskIds", taskIds);
        }

        List<RecommendRelevanceSceneEntity> list = recommendRelevanceSceneMapper.selectScene(params);
        return list;
    }
}
