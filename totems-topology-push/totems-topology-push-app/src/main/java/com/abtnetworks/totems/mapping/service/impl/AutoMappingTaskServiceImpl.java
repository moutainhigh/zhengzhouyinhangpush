package com.abtnetworks.totems.mapping.service.impl;

import com.abtnetworks.totems.auto.vo.AutoRecommendTaskSearchVO;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.RecommendTypeEnum;
import com.abtnetworks.totems.common.exception.BusinessException;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingRouteMapper;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingSceneRuleMapper;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingTaskMapper;
import com.abtnetworks.totems.mapping.dto.AddressPoolDTO;
import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import com.abtnetworks.totems.mapping.dto.SearchPushAutoMappingPoolDTO;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingRouteEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingTaskEntity;
import com.abtnetworks.totems.mapping.enums.AutoMappingTaskStatusEnum;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import com.abtnetworks.totems.mapping.manager.AutoMappingProcessManager;
import com.abtnetworks.totems.mapping.service.AutoMappingTaskService;
import com.abtnetworks.totems.mapping.service.PushAutoMappingPoolService;
import com.abtnetworks.totems.mapping.utils.MappingUtils;
import com.abtnetworks.totems.mapping.vo.AutoIdVO;
import com.abtnetworks.totems.mapping.vo.OrderCheckVO;
import com.abtnetworks.totems.push.dao.mysql.PushRecommendTaskExpandMapper;
import com.abtnetworks.totems.push.dto.StaticRoutingDTO;
import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.AddRecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.DNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.SNatAdditionalInfoEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.service.RecommendBussCommonService;
import com.abtnetworks.totems.recommend.vo.PolicyTaskDetailVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @desc    地址映射自动匹配接口实现类
 * @author liuchanghao
 * @date 2022-01-21 10:34
 */
@Service
public class AutoMappingTaskServiceImpl implements AutoMappingTaskService {

    private static Logger logger = LoggerFactory.getLogger(AutoMappingTaskServiceImpl.class);

    @Autowired
    private PushAutoMappingTaskMapper pushAutoMappingTaskMapper;

    @Autowired
    private AutoMappingProcessManager autoMappingProcessManager;

    @Autowired
    private PushAutoMappingSceneRuleMapper pushAutoMappingSceneRuleMapper;

    @Autowired
    private PushAutoMappingRouteMapper pushAutoMappingRouteMapper;

    @Autowired
    RecommendBussCommonService recommendBussCommonService;

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Autowired
    private PushRecommendTaskExpandMapper pushRecommendTaskExpandMapper;

    @Autowired
    private PushAutoMappingPoolService pushAutoMappingPoolService;

    @Override
    public TotemsReturnT check(OrderCheckVO checkVO, Authentication auth) throws Exception {
        List<PushAutoMappingTaskEntity> taskEntityList = new ArrayList<>();
        // 1.判断是否勾选指定后源地址
        boolean isAppointPostSrcIp = false;
        if(ObjectUtils.isNotEmpty(checkVO.getIsAppointPostSrcIp()) && ObjectUtils.isNotEmpty(checkVO.getAppointPostSrcIp())){
            isAppointPostSrcIp = true;
        }
        // 2. 匹配所有的规则，确定要执行哪些规则
        TotemsReturnT sceneRuleEntityReturnT = this.matchNatRule(checkVO);
        if(sceneRuleEntityReturnT.getCode() == TotemsReturnT.FAIL_CODE ){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, sceneRuleEntityReturnT.getMsg());
        }

        if(ObjectUtils.isEmpty(sceneRuleEntityReturnT.getData())){
            // 3.如果工单与规则无交集，则添加一条无关联数据的检查工单，任务结束
            PushAutoMappingTaskEntity record = this.insertPushAutoMappingTask(checkVO,null,auth);
            taskEntityList.add(record);
            return new TotemsReturnT(taskEntityList);
        }

        // 查询当前所有的地址池信息
        SearchPushAutoMappingPoolDTO searchPushMappingNatDTO = new SearchPushAutoMappingPoolDTO();
        searchPushMappingNatDTO.setCurrentPage(1);
        searchPushMappingNatDTO.setPageSize(1000);
        PageInfo<PushAutoMappingPoolEntity> pageInfo = pushAutoMappingPoolService.listPushAutoMappingPoolInfo(searchPushMappingNatDTO);
        if (CollectionUtils.isEmpty(pageInfo.getList())){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"当前没有配置任务地址池信息,请检查配置");
        }


        PushAutoMappingSceneRuleEntity sceneRuleEntity = (PushAutoMappingSceneRuleEntity)sceneRuleEntityReturnT.getData();

        // 4. 遍历所有的规则，执行规则里面的逻辑，返回规则数据
        try {
            taskEntityList = autoMappingProcessManager.generateAutoMappingProcessTask(checkVO, pageInfo, sceneRuleEntity, auth, isAppointPostSrcIp);
        } catch (BusinessException e){
            logger.error(String.format("任务:%s工单检查异常,回滚所有之前已经产生的数据和操作", checkVO.getTheme()));
            logger.error(String.format("任务:%s工单检查异常,异常原因:%s", checkVO.getTheme(), e.getMessage()));
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, e.getMessage());
        } catch (Exception e) {
            logger.error(String.format("任务:%s工单检查异常,回滚所有之前已经产生的数据和操作", checkVO.getTheme()));
            logger.error(String.format("任务:%s工单检查异常,异常原因:%s", checkVO.getTheme(), e.getMessage()));
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, ReturnCode.getMsg(ReturnCode.FAILED));
        }
        return new TotemsReturnT(taskEntityList);
    }

    @Override
    public PageInfo<PushAutoMappingTaskEntity> findList(AutoRecommendTaskSearchVO vo, int pageNum, int pageSize) {
        PushAutoMappingTaskEntity record = new PushAutoMappingTaskEntity();
        BeanUtils.copyProperties(vo, record);
        PageHelper.startPage(pageNum, pageSize);

        List<PushAutoMappingTaskEntity> taskEntities = pushAutoMappingTaskMapper.selectByEntity(record);
        PageInfo<PushAutoMappingTaskEntity> pageInfo = new PageInfo<>(taskEntities);
        return pageInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIdList(List<Integer> idList) throws Exception {
        logger.info("开始删除id为：{} 的数据", idList);
        int delete = pushAutoMappingTaskMapper.deleteIdList(idList);
        return delete;
    }

    @Override
    public PushAutoMappingTaskEntity selectById(int id) {
        return pushAutoMappingTaskMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TotemsReturnT addRecommendTask(List<Integer> idList, Authentication auth) {
        for (Integer id : idList){
            PushAutoMappingTaskEntity autoMappingTaskEntity = pushAutoMappingTaskMapper.selectByPrimaryKey(id);
            AddRecommendTaskEntity entity = new AddRecommendTaskEntity();
            BeanUtils.copyProperties(autoMappingTaskEntity, entity);
            entity.setTaskType(RecommendTypeEnum.DETAIL_RECOMMEND.getTypeCode());
            entity.setIpType(IpTypeEnum.IPV4.getCode());
            entity.setIsAutoMappingTask(true);
            String preDstIp = StringUtils.isBlank(autoMappingTaskEntity.getPostDstIp()) ? autoMappingTaskEntity.getDstIp() : autoMappingTaskEntity.getPostDstIp();
            // 加入仿真任务列表的源和目的都为转换前的IP
            entity.setDstIp(preDstIp);
            // 仿真开通不需要转换后的源地址和目的地址
//            entity.setPostDstIp(autoMappingTaskEntity.getDstIp());
            entity.setPostSrcIp(null);
            entity.setPostDstIp(null);
            if (StringUtils.isNotBlank(autoMappingTaskEntity.getServiceList())) {
                JSONArray array = JSONArray.parseArray(autoMappingTaskEntity.getServiceList());
                List<ServiceDTO> services = array.toJavaList(ServiceDTO.class);
                entity.setServiceList(services);
            }
            recommendBussCommonService.addAutoNatGenerate(entity, auth);
            // 更新状态为已加入仿真任务
            autoMappingTaskEntity.setStatus(AutoMappingTaskStatusEnum.ADDED_RECOMMEND.getCode());
            pushAutoMappingTaskMapper.updateByPrimaryKeySelective(autoMappingTaskEntity);
        }
        return TotemsReturnT.SUCCESS;
    }

    @Override
    public TotemsReturnT getById(AutoIdVO autoIdVO) {
        if (ObjectUtils.isEmpty(autoIdVO.getNatOrRouteId())) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "参数为空");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("id", autoIdVO.getNatOrRouteId());
        List<RecommendTaskEntity> list = recommendTaskMapper.searchNatTask(params);
        if (CollectionUtils.isEmpty(list)) {
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "未查询到相关数据");
        }
        RecommendTaskEntity entity = list.get(0);
        PolicyTaskDetailVO policyDetailVO = new PolicyTaskDetailVO();

        policyDetailVO.setPushStatus(entity.getStatus());
        policyDetailVO.setTaskId(entity.getId());
        policyDetailVO.setPolicyName(entity.getTheme());
        policyDetailVO.setUserName(entity.getUserName());

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

        if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT)) {
            BeanUtils.copyProperties(entity, policyDetailVO);

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
        } else if (entity.getTaskType().equals(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING)) {
            BeanUtils.copyProperties(entity, policyDetailVO);
            PushRecommendTaskExpandEntity expandEntity = pushRecommendTaskExpandMapper.getByTaskId(entity.getId());
            if (null == expandEntity) {
                logger.info(String.format("根据任务id:%d查询拓展数据为空", entity.getId()));
                return new TotemsReturnT(policyDetailVO);
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
        return new TotemsReturnT(policyDetailVO);
    }

    String formatZoneItfString(String zone, String itf) {
        if (AliStringUtils.isEmpty(zone)) {
            return AliStringUtils.isEmpty(itf) ? "" : itf;
        } else {
            return AliStringUtils.isEmpty(itf) ? zone : zone + ", " + itf;
        }
    }

    /**
     * 匹配Nat规则
     * @param checkVO
     * @return
     */
    private TotemsReturnT matchNatRule(OrderCheckVO checkVO){
        // 1.查询所有的规则
        PushAutoMappingSceneRuleEntity record = new PushAutoMappingSceneRuleEntity();
        List<PushAutoMappingSceneRuleEntity> sceneRuleList = pushAutoMappingSceneRuleMapper.selectByEntity(record);
        if(CollectionUtils.isEmpty(sceneRuleList)){
            logger.error("当前暂未配置任何规则场景");
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "当前暂未配置任何规则场景");
        }
        // 2.五元组相交匹配
        for (PushAutoMappingSceneRuleEntity currentSceneRule : sceneRuleList) {
            boolean matchResult = this.matchNat(checkVO, currentSceneRule);
            if(matchResult){
                // 3.判断当前规则是否包含路由规则
                if(StringUtils.containsIgnoreCase(currentSceneRule.getRuleType(), String.valueOf(RuleTypeTaskEnum.STATIC_ROUTING.getCode()))){
                    PushAutoMappingRouteEntity currentRoute = this.matchRouteRule(checkVO);
                    if(ObjectUtils.isEmpty(currentRoute)){
                        logger.error("当前规则配置了静态路由，但未匹配到路由规则，请检查规则配置", JSON.toJSONString(checkVO));
                        return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "当前规则配置了静态路由，但未匹配到路由规则，请检查规则配置");
                    }
                }
                return new TotemsReturnT(currentSceneRule);
            } else {
                //  4.判断是否有交集，无交集，生成一条工单检测数据，有交集，给出错误提示信息
                if(IpUtils.isIntersection(checkVO.getSrcIp(),currentSceneRule.getSrcIp()) || IpUtils.isIntersection(checkVO.getDstIp(),currentSceneRule.getDstIp())){
                    logger.error("当前数据：{} 工单填写错误或场景规则配置错误，请检查", JSON.toJSONString(checkVO));
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "工单填写错误或场景规则配置错误，请检查");
                } else {
                    logger.info("当前数据：{} 未匹配到任何规则", JSON.toJSONString(checkVO));
                    continue;
                }
            }
        }
        return TotemsReturnT.SUCCESS;
    }

    /**
     * 工单匹配Nat规则
     * @param checkVO
     * @param currentSceneRule
     * @return
     */
    private boolean matchNat(OrderCheckVO checkVO, PushAutoMappingSceneRuleEntity currentSceneRule){
        logger.info("开始匹配其他规则，当前工单输入目的地址：{}，场景规则目的地址：{}", checkVO.getDstIp(), currentSceneRule.getDstIp());
        if(StringUtils.isBlank(checkVO.getSrcIp()) && StringUtils.isNotBlank(currentSceneRule.getSrcIp()) ||
                StringUtils.isNotBlank(checkVO.getSrcIp()) && StringUtils.isBlank(currentSceneRule.getSrcIp())){
            return false;
        }
        if(StringUtils.isBlank(checkVO.getDstIp()) && StringUtils.isNotBlank(currentSceneRule.getDstIp()) ||
                StringUtils.isNotBlank(checkVO.getDstIp()) && StringUtils.isBlank(currentSceneRule.getDstIp())){
            return false;
        }
        // 只有当检测工单的源和目的同时被某一条规则的源和目的分别包含时，才能匹配上
        boolean srcIpContainsFlag = true;
        boolean dstIpContainsFlag = true;
        String[] srcIps = checkVO.getSrcIp().split(",");
        String[] sceneRuleSrcIps = currentSceneRule.getSrcIp().split(",");
        for (String srcIp : srcIps) {
            boolean currentIpContainsFlag = false;
            for (String sceneRuleSrcIp : sceneRuleSrcIps) {
                if(IpUtils.checkIpRange(srcIp, sceneRuleSrcIp)){
                    currentIpContainsFlag = true;
                    break;
                }
            }
            if (!currentIpContainsFlag){
                srcIpContainsFlag = false;
                break;
            }
        }

        String[] dstIps = checkVO.getDstIp().split(",");
        String[] sceneRuleDstIps = currentSceneRule.getDstIp().split(",");
        for (String dstIp : dstIps) {
            boolean currentIpContainsFlag = false;
            for (String sceneRuleDstIp : sceneRuleDstIps) {
                if(IpUtils.checkIpRange(dstIp, sceneRuleDstIp)){
                    currentIpContainsFlag = true;
                    break;
                }
            }
            if (!currentIpContainsFlag){
                dstIpContainsFlag = false;
                break;
            }
        }

        if(srcIpContainsFlag && dstIpContainsFlag){
            logger.info("匹配规则成功，规则类型：{}，当前工单输入目的地址：{}，场景规则目的地址：{}", currentSceneRule.getRuleType(), checkVO.getDstIp(), currentSceneRule.getDstIp());
            return true;
        }

        return false;
    }

    /**
     * 匹配路由规则
     * @param checkVO
     * @return
     */
    private PushAutoMappingRouteEntity matchRouteRule(OrderCheckVO checkVO){
        // 1.查询所有的规则
        PushAutoMappingRouteEntity record = new PushAutoMappingRouteEntity();
        List<PushAutoMappingRouteEntity> routeList = pushAutoMappingRouteMapper.selectByEntity(record);
        if(CollectionUtils.isEmpty(routeList)){
            logger.error("当前暂未配置任何静态路由规则");
            return null;
        }
        // 2.五元组相交匹配
        for (PushAutoMappingRouteEntity currentRoute : routeList) {
            boolean matchResult = this.matchRoute(checkVO, currentRoute);
            if(matchResult){
                return currentRoute;
            }
        }
        return null;
    }

    /**
     * 工单匹配Nat规则
     * @param checkVO
     * @param currentRoute
     * @return
     */
    private boolean matchRoute(OrderCheckVO checkVO, PushAutoMappingRouteEntity currentRoute){
        logger.info("开始匹配静态路由规则，当前工单输入目的地址：{}，静态路由目的地址：{}", checkVO.getDstIp(), currentRoute.getDstIp());
        if(StringUtils.isBlank(checkVO.getDstIp()) && StringUtils.isNotBlank(currentRoute.getDstIp())){
            return false;
        }
        if(StringUtils.isNotBlank(checkVO.getDstIp()) && StringUtils.isBlank(currentRoute.getDstIp())){
            return false;
        }

        boolean dstIpContainsFlag = true;
        String[] dstIps = checkVO.getDstIp().split(",");
        String[] currentRouteDstIps = currentRoute.getDstIp().split(",");
        for (String dstIp : dstIps) {
            boolean currentIpContainsFlag = false;
            for (String currentRouteDstIp : currentRouteDstIps) {
                if(IpUtils.checkIpRange(dstIp, currentRouteDstIp)){
                    currentIpContainsFlag = true;
                    break;
                }
            }
            if (!currentIpContainsFlag){
                dstIpContainsFlag = false;
                break;
            }
        }

        if(dstIpContainsFlag){
            logger.info("匹配静态路由成功，当前工单输入目的地址：{}，静态路由目的地址：{}", checkVO.getDstIp(), currentRoute.getDstIp());
            return true;
        }
        return false;
    }


    /**
     * 添加到地址映射自动匹配表
     * @param checkVO
     */
    private PushAutoMappingTaskEntity insertPushAutoMappingTask(OrderCheckVO checkVO, AutoMappingTaskResultDTO taskResultDTO, Authentication auth){
        PushAutoMappingTaskEntity record = new PushAutoMappingTaskEntity();
        BeanUtils.copyProperties(checkVO, record);
        record.setUuid(IdGen.uuid());
        if (ObjectUtils.isNotEmpty(taskResultDTO) && ObjectUtils.isNotEmpty(taskResultDTO.getNatId())) {
            JSONArray natArray = new JSONArray();
            JSONObject natJson = new JSONObject();
            natJson.put("index", 1);
            natJson.put("name", taskResultDTO.getNatTheme() +"(" + taskResultDTO.getDeviceName() + "(" + taskResultDTO.getDeviceIp() + "))");
            natJson.put("natTheme", checkVO.getTheme() + "_SNAT");
            natJson.put("taskId", taskResultDTO.getNatId());
            natJson.put("type", 6);
            natJson.put("ruleType", RuleTypeTaskEnum.SNAT_MANT_TO_ONE.getCode());
            natArray.add(natJson);
            record.setRelevancyNat(natArray.toJSONString());
            //转换后源
            record.setPostSrcIp(taskResultDTO.getPostSrcIp());
            //转换前目的
            record.setPostDstIp(taskResultDTO.getPreDstIp());
        }
        record.setStatus(AutoMappingTaskStatusEnum.WAIT_RECOMMEND.getCode());
        record.setUserName(auth.getName());
        record.setServiceList(JSON.toJSONString(checkVO.getServiceList()));
        record.setCreateUser(auth.getName());
        record.setCreateTime(new Date());
        //转换前源
        record.setSrcIp(checkVO.getSrcIp());
        //转换后目的
        record.setPostDstIp(checkVO.getDstIp());
        pushAutoMappingTaskMapper.insert(record);
        record = pushAutoMappingTaskMapper.getByUuid(record.getUuid());
        return record;
    }

    public static void main(String[] args) {
        JSONObject ruleJson = new JSONObject();
        ruleJson.put("ruleType", 2);
        ruleJson.put("ruleId", 1);
        System.out.println(ruleJson.toJSONString());

        JSONObject natJson = new JSONObject();
        natJson.put("natTheme", "test-dnat");
        natJson.put("natId", 13);
        System.out.println(natJson.toJSONString());

        JSONObject routeJson = new JSONObject();
        routeJson.put("routeTheme", "test-dnat");
        routeJson.put("routeId", 13);
        System.out.println(routeJson.toJSONString());
    }
}
