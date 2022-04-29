package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.collect.TotemsListUtils;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.external.vo.AllRuleMatchFlowVO;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.recommend.dto.task.DeviceNatInfoDTO;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.PathDeviceDetailEntity;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.RecommendService;
import com.abtnetworks.totems.whale.baseapi.dto.RuleMatchFlowDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.policy.dto.FilterDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstIntegerDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policy.ro.*;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.abtnetworks.totems.whale.policy.service.impl.WhalePathAnalyzeClientImpl;
import com.abtnetworks.totems.whale.policybasic.ro.FilterListsRO;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS;

@Service
public class RecommendServiceImpl implements RecommendService {
    private static Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);

    @Autowired
    public RecommendTaskManager taskService;

    @Autowired
    public WhaleManager whaleService;

    @Autowired
    public VendorManager vendorManager;

    @Value("${push.whale:false}")
    private Boolean isNginZ;

    @Value("${push.open-acl:false}")
    private Boolean openAcl;

    @Value("${push.reverse-acl:false}")
    private Boolean reverseAcl;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    public WhalePathAnalyzeClientImpl whalePathAnalyzeClient;

    @Autowired
    private RecommendTaskManager recommendTaskManager;

    @Autowired
    WhalePathAnalyzeClient client;

    @Override
    public int recommendPolicyByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO) {
        int pathId = task.getId();
        int taskId = task.getTaskId();
        String theme = task.getTheme();
        logger.info(String.format("任务(%d)[%s]路径%d开始建议策略生成...", taskId, theme, pathId));

        PathAnalyzeRO pathAnalyzeRO = task.getPathAnalyzeRO();
        List<PathAnalyzeDataRO> list = pathAnalyzeRO.getData();

        if(list == null || list.size() == 0) {
            //策略开通不会走到这里，路径分析中已将此情况排除。这里用来以防本步骤连接到其他步骤进行...
            logger.warn(String.format("任务(%d)[%s]路径%d路径数据为空，无策略生成...",taskId, theme, pathId));
            return ReturnCode.FAILED;
        }

        //生成策略实体数据，用于存储数据库
        List<RecommendPolicyEntity> policyEntityList = new ArrayList<>();

        //经过设备的设备详情列表，集中存储在路径分析完成后一次性调用MySQL存储
        List<PathDeviceDetailEntity> deviceDetailList = new ArrayList<>();

        boolean updateAnalyseStatus =false;

        for(PathAnalyzeDataRO pathAnalyzeData: list) {
            List<PathInfoRO> pathInfoList = pathAnalyzeData.getPathList();
            if (pathInfoList == null || pathInfoList.size() == 0) {
                logger.warn(String.format("任务(%d)[%s]路径%d路径信息数据为空...", taskId, theme, pathId));
                continue;
            }
            logger.debug(String.format("任务(%d)[%s]路径%d分析路径经过设备...", taskId, theme, pathId));


//            //生成策略DTO数据，用于后续策略检查和生成命令行使用
//            List<RecommendPolicyDTO> recommendPolicyDTOList = new ArrayList<>();
            for (PathInfoRO pathInfoRO : pathInfoList) {
                logger.debug(String.format("任务(%d)[%s]:路径(%d)分析路径:%s", taskId, theme, pathId, JSONObject.toJSONString(pathInfoRO)));
                List<PathDetailRO> pathDetailList = pathInfoRO.getDeviceDetails();
                if(pathDetailList == null || pathDetailList.size() == 0) {
                    logger.error(String.format("任务(%d)[%s]:路径(%d)路径设备详情列表为空!", taskId, theme, pathId));
                    continue;
                }

                int index = 0;
                for (PathDetailRO pathDetailRO : pathDetailList) {
                    String deviceUuid = pathDetailRO.getDeviceUuid();
                    NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
                    if (node == null) {
                        logger.error(String.format("任务(%d)[%s]:路径(%d)设备信息(%s)不存在，不生成策略...", taskId, theme, pathId, deviceUuid));
                        continue;
                    }
                    logger.debug(String.format("任务(%d)[%s]:路径(%d)分析设备%s(%s)详情数据...", taskId, theme, pathId, node.getDeviceName(), node.getIp()));

                    //获取设备详情
                    DeviceDetailRO deviceDetailRO = null;
                    logger.info(String.format("任务(%d)[%s]:路径(%d)查询设备详情入参:%s",taskId, theme, pathId,JSONObject.toJSONString(pathDetailRO)));
                    try {
                        //区分青提和whale的模拟变更入参
                        if (isNginZ == null || !isNginZ) {
                            //请求的whale
                            deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO, task.getWhatIfCaseUuid());
                        }else{
                            //请求青提的设备详情
                            deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO,null);
                        }
                    } catch(Exception e) {
                        logger.error(String.format("任务(%d)[%s]路径%d设备详情查询结果异常...", taskId, task.getTheme(), task.getId()), e);
                    }

                    if (deviceDetailRO == null) {
                        //设备详情不存在不妨碍生成策略，策略从设备的flow中获取
                        logger.warn(String.format("任务(%d)[%s]路径%d设备%s(%s)详情数据不存在:%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), pathDetailRO));
                    } else {
                        logger.debug(String.format("任务(%d)[%s]路径%d设备%s(%s)详情数据为:%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), deviceDetailRO));
                    }

                    //存储设备详情
                    PathDeviceDetailEntity deviceDetailEntity = saveDeviceDetail(pathId, deviceUuid, index, deviceDetailRO);
                    index++;
                    deviceDetailList.add(deviceDetailEntity);

                    String currentStatus = pathDetailRO.getCurrDevicePathStatus();
                    //若为通路并且不存在时间对象，则该设备上无需生成策略
                    String longLinkPathStatus = pathDetailRO.getLongLinkPathStatus();
                    List<PolicyInfoDTO> policyInfoList = new ArrayList<>();

                    //若为通路，并且长链接状态不存在（短链接）或者长链接状态也放通，则为放通，不生成策略
                    if (null != currentStatus && currentStatus.endsWith("FULLY_OPEN") &&
                            (AliStringUtils.isEmpty(longLinkPathStatus) || longLinkPathStatus.endsWith("FULLY_OPEN"))) {
                        if (isNginZ == null || !isNginZ) {
                            //请求的whale
                            logger.debug(String.format("任务(%d)[%s]路径%d设备%s(%s)为放通状态，不生成策略", taskId, theme, pathId, node.getDeviceName(), node.getIp()));

                            logger.info(String.format("任务(%d)[%s]路径%d设备%s(%s)为放通状态currDevicePathStatus(%s),longLinkePathStatus(%s)，不生成策略",
                                    taskId, theme, pathId, node.getDeviceName(), node.getIp(), currentStatus, longLinkPathStatus));
                            continue;
                        }else{
//                            if (simulationTaskDTO.getTaskType() == POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND || simulationTaskDTO.getTaskType() == BIG_INTERNET_RECOMMEND ) {
                                logger.debug(String.format("任务(%d)[%s]路径%d设备%s(%s)为放通状态，不生成策略", taskId, theme, pathId, node.getDeviceName(), node.getIp()));

                                logger.info(String.format("任务(%d)[%s]路径%d设备%s(%s)为放通状态currDevicePathStatus(%s),longLinkePathStatus(%s)，不生成策略",
                                        taskId, theme, pathId, node.getDeviceName(), node.getIp(), currentStatus, longLinkPathStatus));
                                continue;
//                            }else{
                                //互联网开通  20210203  KSH-4959仿真开通的 都在开通状态时不生成策略建议
//                                List<DeviceFlowsRO> deviceFlowsROList  = pathDetailRO.getDeviceFlows();
//                                policyInfoList = getPolicyList(deviceFlowsROList, node, task);
//                            }
                        }

                    } else if(null != currentStatus && currentStatus.endsWith("FULLY_OPEN")) {
                        //设备路径已放通，而长链接状态为未放通状态
                        List<PathFlowRO> pathFlowROList = pathDetailRO.getLongLinkDenyTraffic();
                        List<PolicyInfoDTO> policyList = getPolicyInfoListFromPathFlow(pathFlowROList, task, pathDetailRO,false);
                        // 构建策略到policyList
                        buildPolicyDtoToList(task, simulationTaskDTO, policyInfoList, policyList);
                    } else {
                        if("NO_PATH".equalsIgnoreCase(pathInfoRO.getPathType())){
                            logger.error(String.format("任务(%d)[%s]:路径(%d)设备%s(%s)路径状态为无路径，无法生成策略。%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), pathDetailRO));
                            continue;
                        }
                        //设备路径未放通状态
                        List<DeviceFlowsRO> deviceFlowsROList = pathDetailRO.getDeviceFlows();
                        if (deviceFlowsROList == null || deviceFlowsROList.size() == 0) {
                            logger.error(String.format("任务(%d)[%s]:路径(%d)设备%s(%s)流为空，无法生成策略。%s", taskId, theme, pathId, node.getDeviceName(), node.getIp(), pathDetailRO));
                            continue;
                        }
                        List<PolicyInfoDTO> policyList = getPolicyList(deviceFlowsROList, node, task,pathDetailRO,deviceDetailRO);
                        // 构建策略到policyList
                        buildPolicyDtoToList(task, simulationTaskDTO, policyInfoList, policyList);
                    }
                    // 是否匹配到nat策略
                    simulationTaskDTO.setMatchNat(task.getMatchNat());

                    //合并相同源地址和目的地址的服务
                    policyInfoList = mergePolicyInfoList(policyInfoList);

                    //根据设备类型获取设备相关策略集UUID
                    String ruleListUuid = getRuleListUuid(deviceDetailRO);
                    ruleListUuid = getRuleListUuid(ruleListUuid, node);


                    //获取设备出接口，入接口UUID
                    String inIfId = pathDetailRO.getInIfId();
                    String outIfId = pathDetailRO.getOutIfId();

                    //获取设备信息
                    DeviceRO device = null;
                    try {
                        device = whaleService.getDeviceByUuid(deviceUuid);
                    } catch (Exception e) {
                        logger.error(String.format("任务(%d)[%s]路径%d获取设备%s信息失败", task.getTaskId(), task.getTheme(), task.getId(), deviceUuid), e);
                    }

                    if(device == null || device.getData() == null || device.getData().size() == 0) {
                        logger.warn(String.format("任务(%d)[%s]路径%d获取设备%s信息为空, 不生成策略...", task.getTaskId(), task.getTheme(), task.getId(), deviceUuid));
                        continue;
                    }



                    //从设备信息中获取设备是否为虚设备信息
                    DeviceDataRO deviceData = device.getData().get(0);
                    boolean isVsys = false;
                    String rootDeviceUuid = "";
                    String vsysName = "";
                    if(deviceData.getIsVsys() != null) {
                        isVsys = deviceData.getIsVsys();
                        rootDeviceUuid = deviceData.getRootDeviceUuid();
                        vsysName = deviceData.getVsysName();
                    }


                    // 策略集名称
                    String inAclRuleListName = null;
                    String outAclRuleListName = null;
                    //根据设备类型获取设备相关策略集UUID
                    String inRuleListUuid = null;
                    String outRuleListUuid = null;
                    // 匹配的策略id
                    String inAclRuleIds = null;
                    String outAclRuleIds = null;

                    // 判断是思科/锐捷路由设备，则去查询对应的策略集名称，策略集uuid，匹配的策略id
                    String modelNumber = node.getModelNumber();
                    PolicyEnum policyType = null;

                    // 添加是否开通acl的开关
                    if (!AliStringUtils.isEmpty(modelNumber) && DeviceTypeEnum.ROUTER.name().equalsIgnoreCase(deviceData.getDeviceType()) &&
                            (modelNumber.equals("Cisco IOS") || modelNumber.equals("Cisco NX-OS") || modelNumber.equals("Ruijie RGOS")
                            || modelNumber.equals(DeviceModelNumberEnum.SRX.getKey()) || modelNumber.equals(DeviceModelNumberEnum.SRX_NoCli.getKey())
                            || modelNumber.equalsIgnoreCase(DeviceModelNumberEnum.JUNIPER_ROUTER.getKey()))) {
                        //根据设备类型获取设备相关策略集UUID
                        if (!openAcl) {
                            logger.info(String.format("当前设备:%s不生成acl策略,跳过策略建议的生成", modelNumber));
                            continue;
                        }
                        Map<String, List<String>> reACLRuleMap = getACLRuleListUuid(deviceDetailRO);
                        if (reACLRuleMap == null || reACLRuleMap.size() < 1) {
                            logger.info("反向查询思科acl策略匹配到的策略集uuid为空,不生成策略");
                            continue;
                        }
                        if (!TotemsListUtils.isEmpty(reACLRuleMap.get("in"))) {
                            List<String> inRulList = reACLRuleMap.get("in");
                            inRuleListUuid = inRulList.get(0);
                            inAclRuleIds = inRulList.get(1);
                            inAclRuleListName = getAclRuleListName(inRuleListUuid);
                        }

                        if (!TotemsListUtils.isEmpty(reACLRuleMap.get("out"))) {
                            List<String> outRulList = reACLRuleMap.get("out");
                            outRuleListUuid = outRulList.get(0);
                            outAclRuleIds = outRulList.get(1);
                            outAclRuleListName = getAclRuleListName(outRuleListUuid);
                        }
                        if (StringUtils.isBlank(inAclRuleListName) && StringUtils.isBlank(outAclRuleListName)) {
                            continue;
                        }
                        policyType = PolicyEnum.ACL;
                    }


                    //从设备信息中获取设备接口信息
                    List<String> srcIntf = InterfaceNameUtils.getInterfaceName(deviceData, inIfId);
                    List<String> dstIntf = InterfaceNameUtils.getInterfaceName(deviceData, outIfId);
                    String srcInterface = srcIntf.get(0);
                    String dstInterface = dstIntf.get(0);

                    //获取设备所有Zone信息
                    ZoneRO zone = null;
                    try {
                        zone = whaleService.getDeviceZone(deviceUuid);
                    } catch(Exception e) {
                        logger.error(String.format("任务(%d)[%s]路径%d获取域信息失败", task.getTaskId(), task.getTheme(), task.getId()), e);
                    }

                    //根据接口和设备Zone信息获取出入接口所在域
                    ZoneDataRO srcZoneData = whaleService.getZoneData(zone, srcInterface);
                    ZoneDataRO dstZoneData = whaleService.getZoneData(zone, dstInterface);

                    // 入接口挂载了acl 则开通入接口策略
                    if (StringUtils.isNotBlank(inAclRuleListName)) {
                        buildPolicyEntity(task, simulationTaskDTO, policyEntityList, node, policyInfoList, inRuleListUuid, isVsys,
                                rootDeviceUuid, vsysName, inAclRuleListName, inAclRuleIds, policyType, srcIntf, dstIntf, srcZoneData, dstZoneData);
                    }

                    // 出接口挂载了acl 则开通入接口策略
                    if (StringUtils.isNotBlank(outAclRuleListName)) {
                        buildPolicyEntity(task, simulationTaskDTO, policyEntityList, node, policyInfoList, outRuleListUuid, isVsys,
                                rootDeviceUuid, vsysName, outAclRuleListName, outAclRuleIds, policyType, srcIntf, dstIntf, srcZoneData, dstZoneData);
                    }

                    //  非思科和锐捷，策略实体save流程(主线流程)
                    if(StringUtils.isBlank(inAclRuleListName) && StringUtils.isBlank(outAclRuleListName)){
                        buildPolicyEntity(task, simulationTaskDTO, policyEntityList, node, policyInfoList, ruleListUuid, isVsys,
                                rootDeviceUuid, vsysName, null, null, policyType, srcIntf, dstIntf, srcZoneData, dstZoneData);
                    }

                }

                if (reverseAcl) {
                    // 思科反向acl 策略开通
                    boolean isReverseAcl = reverseAclPolicyCreate(task, simulationTaskDTO, taskId, policyEntityList, pathDetailList);
                    if (isReverseAcl) {
                        updateAnalyseStatus = true;
                    }
                }
            }
        }

        //  状态为已开通且有回包命令行时，修改状态
        if (StringUtils.isNotBlank(simulationTaskDTO.getPathAnalyzeStatus()) && String.valueOf(POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS).equals(simulationTaskDTO.getPathAnalyzeStatus()) && updateAnalyseStatus) {
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FINISHED);
        }

        //2020-06-19 luwei 基于青提业务修改，bug=4377，进行去重处理
        if (isNginZ != null && isNginZ) {
            logger.info("去重之前，生成的命令建议条数,size:{}", policyEntityList.size());
            policyEntityList = policyEntityList.stream().distinct().collect(Collectors.toList());
            logger.info("去重之后，剩余命令建议调试，size:{}", policyEntityList.size());
        }



        int rc = taskService.addRecommendPolicyList(policyEntityList);
        if(rc != ReturnCode.POLICY_MSG_OK){
            logger.warn(String.format("任务(%d)[%s]路径%d保存策略列表",taskId, theme, pathId) + ReturnCode.getMsg(rc));
        }

        rc = taskService.insertpathDeviceDetailList(deviceDetailList);
        if(rc != ReturnCode.POLICY_MSG_OK){
            logger.warn(String.format("任务(%d)[%s]路径%d保存策略列表",taskId, theme, pathId) + ReturnCode.getMsg(rc));
        }

        //将生成的策略转成PolicyDTO存放到任务中供后续合并使用
        task.setPolicyList(getPolicyDTOList(policyEntityList));

        //更新路径策略建议步骤结果，有策略生成才设置为策略建议已完成
        //因为路径全通的情况下会到此函数中做存储设备详情的动作，有可能不生成策略
        if(task.getPolicyList().size() > 0) {
            taskService.updatePathAdviceStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_ADVICE_FINISHED);
            logger.info(String.format("任务(%d)[%s]路径%d策略建议已完成，有%d条建议策略生成...",taskId, theme, pathId, task.getPolicyList().size()));
            return ReturnCode.POLICY_MSG_OK;
        }

        logger.info(String.format("任务(%d)[%s]路径%d策略建议已完成，无建议策略生成...",taskId, theme, pathId));
        return ReturnCode.NO_RECOMMEND_POLICY;
    }

    private void buildPolicyDtoToList(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO, List<PolicyInfoDTO> policyInfoList,
                                      List<PolicyInfoDTO> policyList) {
        Map<String, String> domainMap = simulationTaskDTO.getDomainConvertIp();
        if (null != domainMap && !domainMap.isEmpty() && StringUtils.isNotEmpty(domainMap.get(task.getDstIp()))) {
            for (PolicyInfoDTO policy : policyList) {
                //域名或者域名解析后的ip策略不参与合并
                policy.setPolicySource(CommonConstants.POLICY_SOURCE_DST_DOMAIN);
                StringBuffer dstIp = new StringBuffer(policy.getDstIp()) ;
                String domain = domainMap.get(task.getDstIp());
                dstIp.append(",").append(domain);
                policy.setDstIp(dstIp.toString());

            }
        }
        policyInfoList.addAll(policyList);
    }

    /**
     * 构建策略实体参数
     * @param task
     * @param simulationTaskDTO
     * @param policyEntityList
     * @param node
     * @param policyInfoList
     * @param ruleListUuid
     * @param isVsys
     * @param rootDeviceUuid
     * @param vsysName
     * @param inAclRuleListName
     * @param inAclRuleIds
     * @param policyType
     * @param srcIntf
     * @param dstIntf
     * @param srcZoneData
     * @param dstZoneData
     */
    private void buildPolicyEntity(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO, List<RecommendPolicyEntity> policyEntityList, NodeEntity node, List<PolicyInfoDTO> policyInfoList, String ruleListUuid, boolean isVsys, String rootDeviceUuid, String vsysName, String inAclRuleListName, String inAclRuleIds, PolicyEnum policyType, List<String> srcIntf, List<String> dstIntf, ZoneDataRO srcZoneData, ZoneDataRO dstZoneData) {
        //保存生成策略数据
        for (PolicyInfoDTO policyInfo : policyInfoList) {

            RecommendPolicyEntity policyEntity = getPolicyEntity(task, policyInfo, node, ruleListUuid,
                    srcZoneData, dstZoneData, srcIntf, dstIntf, isVsys, rootDeviceUuid, vsysName, task.getIdleTimeout(), simulationTaskDTO, policyType, inAclRuleListName, inAclRuleIds);

            //设置高级设置
            taskService.getAdvancedSettings(policyEntity);

            //生成的策略加入到列表中
            policyEntityList.add(policyEntity);
        }
    }

    /**
     * 反向生成cisco acl策略流程
     * @param task
     * @param simulationTaskDTO
     * @param taskId
     * @param policyEntityList
     * @param pathDetailList
     */
    private boolean reverseAclPolicyCreate(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO, int taskId, List<RecommendPolicyEntity> policyEntityList,
                                        List<PathDetailRO> pathDetailList) {
        boolean updateAnalyseStatus = false;
        for (PathDetailRO pathDetailRO : pathDetailList) {
            String deviceUuid = pathDetailRO.getDeviceUuid();

            NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
            //0：防火墙；1：路由/交换；2：负载均衡 3: 模拟网关
            if (node == null || !"1".equals(node.getType())) {
                continue;
            }
            String modelNumber = node.getModelNumber();
            // 目前只对思科ios和思科nx_os做了acl反向策略
            if (!AliStringUtils.isEmpty(modelNumber) && !(modelNumber.equals(DeviceModelNumberEnum.CISCO_IOS.getKey())
                    || modelNumber.equals(DeviceModelNumberEnum.CISCO_NX_OS.getKey()))) {
                continue;
            }
            logger.info("开始cisco 反向acl策略开通...");
            String tempId = pathDetailRO.getInIfId();
            pathDetailRO.setInIfId(pathDetailRO.getOutIfId());
            pathDetailRO.setOutIfId(tempId);
            List<DeviceFlowsRO> deviceFlows = pathDetailRO.getDeviceFlows();
            for (DeviceFlowsRO deviceFlow : deviceFlows) {
                List<FlowListRO> flowList = deviceFlow.getFlowList();
                for (FlowListRO flowListRO : flowList) {
                    List<PathFlowRO> traffic = flowListRO.getTraffic();
                    for (PathFlowRO pathFlowRO : traffic) {
                        List<SrcDstIntegerDTO> srcPorts = pathFlowRO.getSrcPorts();
                        pathFlowRO.setSrcPorts(pathFlowRO.getDstPorts());
                        pathFlowRO.setDstPorts(srcPorts);
                        List<SrcDstStringDTO> ip4SrcAddresses = pathFlowRO.getIp4SrcAddresses();
                        pathFlowRO.setIp4SrcAddresses(pathFlowRO.getIp4DstAddresses());
                        pathFlowRO.setIp4DstAddresses(ip4SrcAddresses);
                        List<SrcDstStringDTO> ip6SrcAddresses = pathFlowRO.getIp6SrcAddresses();
                        pathFlowRO.setIp6SrcAddresses(pathFlowRO.getIp6DstAddresses());
                        pathFlowRO.setIp6DstAddresses(ip6SrcAddresses);
                    }
                }
            }
            //获取设备详情
            DeviceDetailRO deviceDetailRO = null;
            try {
                //区分青提和whale的模拟变更入参
                if (isNginZ == null || !isNginZ) {
                    //请求的whale
                    deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO, task.getWhatIfCaseUuid());
                } else {
                    //请求青提的设备详情
                    deviceDetailRO = whaleService.getDeviceDetail(pathDetailRO, null);
                }
            } catch (Exception e) {
                logger.error(String.format("反向查询任务(%d)[%s]路径%d设备详情查询结果异常...", taskId, task.getTheme(),
                        task.getId()), e);
            }

            if (deviceDetailRO == null) {
                deviceDetailRO = new DeviceDetailRO();
            }

            //获取设备出接口，入接口UUID
            String inIfId = pathDetailRO.getInIfId();
            String outIfId = pathDetailRO.getOutIfId();

            //获取设备信息
            DeviceRO device = null;
            try {
                device = whaleService.getDeviceByUuid(deviceUuid);
            } catch (Exception e) {
                logger.error(String.format("反向查询任务(%d)[%s]路径%d获取设备%s信息失败", task.getTaskId(), task.getTheme(),
                        task.getId(), deviceUuid), e);
            }

            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.warn(String.format("反向查询任务(%d)[%s]路径%d获取设备%s信息为空, 不生成策略...", task.getTaskId(),
                        task.getTheme(), task.getId(), deviceUuid));
                continue;
            }

            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);

            boolean isVsys = false;
            String rootDeviceUuid = "";
            String vsysName = "";
            if (deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                rootDeviceUuid = deviceData.getRootDeviceUuid();
                vsysName = deviceData.getVsysName();
            }

            // 策略集名称
            String inAclRuleListName = null;
            String outAclRuleListName = null;
            //根据设备类型获取设备相关策略集UUID
            String inRuleListUuid = null;
            String outRuleListUuid = null;
            // 匹配的策略id
            String inAclRuleIds = null;
            String outAclRuleIds = null;
            // 判断是思科路由设备，则去查询对应的策略集名称，策略集uuid，匹配的策略id
            PolicyEnum policyType = null;

            //根据设备类型获取设备相关策略集UUID
            Map<String, List<String>> reACLRuleMap = getACLRuleListUuid(deviceDetailRO);
            if (reACLRuleMap == null || reACLRuleMap.size() < 1) {
                logger.info("反向查询思科acl策略匹配到的策略集uuid为空,不生成策略");
                continue;
            }
            if (!TotemsListUtils.isEmpty(reACLRuleMap.get("in"))) {
                List<String> inRulList = reACLRuleMap.get("in");
                inRuleListUuid = inRulList.get(0);
                inAclRuleIds = inRulList.get(1);
                inAclRuleListName = getAclRuleListName(inRuleListUuid);
            }

            if (!TotemsListUtils.isEmpty(reACLRuleMap.get("out") )) {
                List<String> outRulList = reACLRuleMap.get("out");
                outRuleListUuid = outRulList.get(0);
                outAclRuleIds = outRulList.get(1);
                outAclRuleListName = getAclRuleListName(outRuleListUuid);
            }

            if (StringUtils.isBlank(inAclRuleListName) && StringUtils.isBlank(outAclRuleListName)) {
                continue;
            }
            policyType = PolicyEnum.ACL;

            //从设备信息中获取设备接口信息
            List<String> srcIntf = InterfaceNameUtils.getInterfaceName(deviceData, inIfId);
            List<String> dstIntf = InterfaceNameUtils.getInterfaceName(deviceData, outIfId);
            String srcInterface = srcIntf.get(0);
            String dstInterface = dstIntf.get(0);

            RuleMatchFlowDTO ruleMatchFlowDTO = new RuleMatchFlowDTO();

            ruleMatchFlowDTO.setBeginFlow(createBeginFlow(task));
            ruleMatchFlowDTO.setInInterfaceName(srcInterface);
            ruleMatchFlowDTO.setOutInterfaceName(dstInterface);
            ruleMatchFlowDTO.setInIfId(inIfId);
            ruleMatchFlowDTO.setOutIfId(outIfId);
            ruleMatchFlowDTO.setEstablish(true);
            ruleMatchFlowDTO.setDeviceUuid(deviceUuid);

            AllRuleMatchFlowVO allRuleMatchFlowVO = null;
            try {
                //请求的whale
                allRuleMatchFlowVO = whalePathAnalyzeClient.getAllRuleMatchFlow(ruleMatchFlowDTO);
            } catch (Exception e) {
                logger.error(String.format("反向查询任务(%d)[%s]路径%d设备(交换机)获取策略匹配结果异常[%s]...", taskId, task.getTheme(),
                        task.getId(), e.getMessage()));
            }

            logger.info(String.format("反向查询任务(%d)[%s]路径%d设备(交换机)获取策略匹配结果为:[%s]",taskId, task.getTheme(), task.getId(), JSONObject.toJSONString(allRuleMatchFlowVO)));
            if (allRuleMatchFlowVO == null) {
                continue;
            }

            if (TotemsListUtils.isEmpty(allRuleMatchFlowVO.getInDeny())
                    && TotemsListUtils.isEmpty(allRuleMatchFlowVO.getOutDeny())) {
                continue;
            }

            if (StringUtils.isNotBlank(inAclRuleListName) && !TotemsListUtils.isEmpty(allRuleMatchFlowVO.getInDeny())) {
                List<PathFlowRO> inDenyList = allRuleMatchFlowVO.getInDeny();

                getPolicyList(task, simulationTaskDTO, policyEntityList, node, inRuleListUuid, isVsys,
                        rootDeviceUuid, vsysName, inAclRuleListName, inAclRuleIds, policyType, srcIntf,
                        dstIntf, inDenyList);
            }

            if (StringUtils.isNotBlank(outAclRuleListName) && !TotemsListUtils.isEmpty(allRuleMatchFlowVO.getOutDeny())) {
                List<PathFlowRO> outDenyList = allRuleMatchFlowVO.getOutDeny();

                getPolicyList(task, simulationTaskDTO, policyEntityList, node, outRuleListUuid, isVsys,
                        rootDeviceUuid, vsysName, outAclRuleListName, outAclRuleIds, policyType, srcIntf,
                        dstIntf, outDenyList);
            }
            updateAnalyseStatus = true;
            logger.info("结束cisco 反向acl策略开通...");
        }
        return updateAnalyseStatus;
    }


    /**
     * 根据策略集uuid查询策略集名称
     * @param ruleListUuid
     * @return
     */
    private String getAclRuleListName(String ruleListUuid) {
        FilterListsRO dataPO = null;
        try {
            dataPO = whaleService.getFilterListsByUuid(ruleListUuid);
        } catch (Exception e) {
            logger.error(String.format("根据策略集uuid:[%s]查询策略集名称异常...", ruleListUuid), e);
        }
        if (dataPO == null || null == dataPO.getData()) {
            logger.warn(String.format("根据策略集uuid:[%s]获取策略集信息为空, 不生成策略...", ruleListUuid));
            return null;
        }
        String aclRuleListName = dataPO.getData().get(0).getName();
        return aclRuleListName;
    }


    /**
     * 创建begin数据流
     * @param task
     * @return
     */
    private List<FilterDTO> createBeginFlow(PathInfoTaskDTO task) {

        FilterDTO filterDTO = new FilterDTO();
        //设置源ip
        List<SrcDstStringDTO> srcIpList = new ArrayList<>();
        //设置源ip
        String[] srcIpStrs = task.getSrcIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String srcIpStr : srcIpStrs) {
            String startIp = IpUtils.getStartIpFromIpAddress(srcIpStr);
            String endIp = IpUtils.getEndIpFromIpAddress(srcIpStr);
            srcIpList.add(WhaleDoUtils.getSrcDstStringDTO(startIp, endIp));
        }
        filterDTO.setIp4DstAddresses(srcIpList);

        //设置目的ip
        List<SrcDstStringDTO> dstIpList = new ArrayList<>();
        String[] dstIpStrs = task.getDstIp().split(PolicyConstants.ADDRESS_SEPERATOR);
        for (String strIpStr : dstIpStrs) {
            String startIp = IpUtils.getStartIpFromIpAddress(strIpStr);
            String endIp = IpUtils.getEndIpFromIpAddress(strIpStr);
            dstIpList.add(WhaleDoUtils.getSrcDstStringDTO(startIp, endIp));
        }
        filterDTO.setIp4SrcAddresses(dstIpList);

        List<SrcDstIntegerDTO> protocolList = new ArrayList<>();
        StringBuffer srcPorts = new StringBuffer();
        for (ServiceDTO serviceDTO : task.getServiceList()) {

            SrcDstIntegerDTO protocolDTO = new SrcDstIntegerDTO();
            int protocolNum = serviceDTO.getProtocol() == null ? -1 :
                    Integer.parseInt(serviceDTO.getProtocol());
            if (protocolNum == -1) {
                protocolDTO.setStart(0);
                protocolDTO.setEnd(255);
            } else {
                protocolDTO.setStart(protocolNum);
                protocolDTO.setEnd(protocolNum);
            }
            protocolList.add(protocolDTO);

            srcPorts = srcPorts.append(StringUtils.isNotBlank(serviceDTO.getDstPorts()) ? serviceDTO.getDstPorts() + "," : "");
        }
        if (srcPorts.lastIndexOf(",") > 0) {
            srcPorts.deleteCharAt(srcPorts.lastIndexOf(","));
        }
        filterDTO.setProtocols(protocolList);

        //设置端口
        filterDTO.setSrcPorts(WhaleUtils.getSrcDstIntegerDTOList(srcPorts.toString()));
        filterDTO.setDstPorts(WhaleUtils.getSrcDstIntegerDTOList("any"));

        List<FilterDTO> beginFlow = new ArrayList<>();
        beginFlow.add(filterDTO);

        return beginFlow;
    }

    private void getPolicyList(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO,
                               List<RecommendPolicyEntity> policyEntityList, NodeEntity node, String ruleListUuid,
                               boolean isVsys, String rootDeviceUuid, String vsysName, String aclRuleListName,
                               String aclRuleIds, PolicyEnum policyType, List<String> srcIntf, List<String> dstIntf,
                               List<PathFlowRO> denyList) {
        //==
        List<PolicyInfoDTO> policyList = getPolicyInfoListFromPathFlow(denyList, task,null,true);
        Map<String, String> domainMap = simulationTaskDTO.getDomainConvertIp();
        if (null != domainMap && !domainMap.isEmpty() && StringUtils.isNotEmpty(domainMap.get(task.getDstIp()))) {
            //获取域名
            for (PolicyInfoDTO policy : policyList) {
                policy.setPolicySource(CommonConstants.POLICY_SOURCE_DST_DOMAIN);
                StringBuilder dstIp = new StringBuilder(policy.getDstIp());
                String domain = domainMap.get(task.getDstIp());
                dstIp.append(",").append(domain);
                policy.setDstIp(dstIp.toString());
            }
        }
        List<PolicyInfoDTO> policyInfoList = new ArrayList<>(policyList);
        //==

        //合并相同源地址和目的地址的服务
        policyInfoList = mergePolicyInfoList(policyInfoList);

        //保存生成策略数据
        for (PolicyInfoDTO policyInfo : policyInfoList) {

            RecommendPolicyEntity policyEntity = getPolicyEntity(task, policyInfo, node, ruleListUuid,
                    null, null, srcIntf, dstIntf, isVsys, rootDeviceUuid, vsysName,
                    task.getIdleTimeout(), simulationTaskDTO, policyType, aclRuleListName, aclRuleIds);

            //设置高级设置
            taskService.getAdvancedSettings(policyEntity);

            //生成的策略加入到列表中
            policyEntityList.add(policyEntity);
        }
    }

    /**
     * 保存设备详情
     * @param pathId pathId
     * @param deviceUuid 设备UUid
     * @param index 设备序号
     * @param deviceDetailRO 设备详情数据
     */
    private PathDeviceDetailEntity saveDeviceDetail(int pathId, String deviceUuid, int index, DeviceDetailRO deviceDetailRO) {
        PathDeviceDetailEntity entity = new PathDeviceDetailEntity();
        entity.setPathInfoId(pathId);
        entity.setDeviceUuid(deviceUuid);
        entity.setIsVerifyData(PolicyConstants.POLICY_INT_PATH_ANALYZE_DATA);
        entity.setDeviceDetail(JSONObject.toJSONString(deviceDetailRO));
        entity.setPathIndex(index);
        return entity;
    }

    /**
     * 从相关策略中获取策略集RuleListUuid
     * @param deviceDetailRO 相关策略
     * @return 策略集RuleListUuid
     */
    private String getRuleListUuid(DeviceDetailRO deviceDetailRO) {
        List<DeviceDetailDataRO> deviceDetailDataList = deviceDetailRO.getData();
        if(deviceDetailDataList == null || deviceDetailDataList.size() == 0) {
            return null;
        }
        for (DeviceDetailDataRO deviceDetailDataVO : deviceDetailDataList) {

            DeviceRulesRO deviceRules = deviceDetailDataVO.getDeviceRules();
            List<DeviceFilterRuleListRO> deviceRulesList = deviceRules.getSYSTEM__POLICY_1();
            if(deviceRulesList == null) {
                deviceRulesList = deviceRules.getSYSTEM__POLICY_2();
            }

            if(deviceRulesList == null) {
                logger.error("相关策略数据为空...");
                continue;
            }

            //从相关策略中获取策略列表UUID
            for (DeviceFilterRuleListRO deviceRulesVO : deviceRulesList) {
                String ruleListUuid = deviceRulesVO.getRuleListUuid();
                if(!AliStringUtils.isEmpty(ruleListUuid)) {
                    return ruleListUuid;
                }
            }
        }
        return null;
    }


    /**
     * 从相关策略中获取策略集RuleListUuid和匹配到的rule id
     * @param deviceDetailRO 相关策略
     * @return 策略集RuleListUuid
     */
    private Map<String,List<String>> getACLRuleListUuid(DeviceDetailRO deviceDetailRO) {
        logger.info("查询到的acl禁止数据流为:{}",JSONObject.toJSONString(deviceDetailRO));
        Map<String,List<String>> aclRuleMap = new HashMap<>();
        List<DeviceDetailDataRO> deviceDetailDataList = deviceDetailRO.getData();
        if (CollectionUtils.isEmpty(deviceDetailDataList)) {
            return null;
        }
        for (DeviceDetailDataRO deviceDetailDataVO : deviceDetailDataList) {
            DeviceRulesRO deviceRules = deviceDetailDataVO.getDeviceRules();
            List<DeviceFilterRuleListRO>  inDeviceRulesList = deviceRules.getSYSTEM__INBOUND_INTERFACE_ACL();
            List<DeviceFilterRuleListRO>  outDeviceRulesList = deviceRules.getSYSTEM__OUTBOUND_INTERFACE_ACL();

            if(CollectionUtils.isEmpty(inDeviceRulesList) && CollectionUtils.isEmpty(outDeviceRulesList)){
                logger.error("相关ACL策略数据都为空...");
                return null;
            }
            // 优先考虑入接口,判断入接口里面有没有deny策略。如果有直接返回，如果没有再去出接口里面去找deny策略。都没有返回null
            List<String> inRuleList = new ArrayList<>();
            getRuleListUuid(inRuleList, inDeviceRulesList);
            if(CollectionUtils.isNotEmpty(inRuleList)){
                aclRuleMap.put("in",inRuleList);
            }
            List<String> outRuleList = new ArrayList<>();
            getRuleListUuid(outRuleList, outDeviceRulesList);
            if(CollectionUtils.isNotEmpty(outRuleList)){
                aclRuleMap.put("out",outRuleList);
            }
        }
        return aclRuleMap;
    }



    /**
     * 获取策略集uuid
     * @param ruleList
     * @param deviceRulesList
     */
    private void getRuleListUuid(List<String> ruleList, List<DeviceFilterRuleListRO> deviceRulesList) {
        // 其次考虑出接口
        if (CollectionUtils.isNotEmpty(deviceRulesList)) {
            for (DeviceFilterRuleListRO deviceFilterRuleListRO : deviceRulesList) {
                if (ActionEnum.DENY.getKey().equalsIgnoreCase(deviceFilterRuleListRO.getAction())) {
                    String ruleListUuid = deviceFilterRuleListRO.getRuleListUuid();
                    if (!AliStringUtils.isEmpty(ruleListUuid)) {
                        ruleList.add(ruleListUuid);
                        ruleList.add(deviceFilterRuleListRO.getRuleId());
                        break;
                    }
                }
            }
        }
    }

    private List<PolicyInfoDTO> getPolicyList(List<DeviceFlowsRO> deviceFlowsROList, NodeEntity node,PathInfoTaskDTO task,PathDetailRO pathDetailRO,DeviceDetailRO deviceDetailRO) {
        List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
        for(DeviceFlowsRO deviceFlowsRO : deviceFlowsROList) {
            List<FlowListRO> deviceFlowList = deviceFlowsRO.getFlowList();
            if (deviceFlowList == null || deviceFlowList.size() == 0) {
                logger.info("流列表为空...DeviceFlowsRO" + JSONObject.toJSONString(deviceFlowsRO));
                continue;
            }

            //有post用post，没有post用没有ruleListType的。山石设备由于DNAT和SNAT均在安全策略之后做，因此地址取第一条
            FlowListRO effectFlowListRO = deviceFlowList.get(0);
            if (isNginZ != null && isNginZ) {
                // 青提取没有ruleListType的（即第一条）。山石设备由于DNAT和SNAT均在安全策略之后做，因此取SYSTEM__PRE_ROUTING_NAT
                if (!AliStringUtils.isEmpty(node.getModelNumber()) && node.getModelNumber().contains("Hillstone")) {
                    for (FlowListRO flowListRO : deviceFlowList) {
                        if ("SYSTEM__PRE_ROUTING_NAT".equals(flowListRO.getRuleListType())) {
                            effectFlowListRO = flowListRO;
                            break;
                        }
                    }
                }
            }else {
                if (!AliStringUtils.isEmpty(node.getModelNumber())) {
                    if (node.getModelNumber().contains("Hillstone")) {
                        //山石无论有没有NAT，什么种类NAT，都取第一条
                    } else {
                        for (FlowListRO flowListRO : deviceFlowList) {
                            if (flowListRO.getRuleListType() == null) {
                                effectFlowListRO = flowListRO;
                            } else if (flowListRO.getRuleListType().equals("SYSTEM__POST_ROUTING_NAT")) {
                                effectFlowListRO = flowListRO;
                                break;
                            }
                        }
                    }
                } else {
                    for (FlowListRO flowListRO : deviceFlowList) {
                        if (flowListRO.getRuleListType() == null) {
                            effectFlowListRO = flowListRO;
                        } else if (flowListRO.getRuleListType().equals("SYSTEM__POST_ROUTING_NAT")) {
                            effectFlowListRO = flowListRO;
                            break;
                        }
                    }
                }
            }

            //是否匹配到nat策略
            for (FlowListRO flowListRO : deviceFlowList) {
                if (StringUtils.isNotBlank(flowListRO.getRuleListType()) && (flowListRO.getRuleListType().equals("SYSTEM__POST_ROUTING_NAT") || flowListRO.getRuleListType().equals("SYSTEM__PRE_ROUTING_NAT"))) {
                    task.setMatchNat(true);
                    break;
                }
            }

            List<PathFlowRO> pathFlowROList = effectFlowListRO.getTraffic();
            List<PolicyInfoDTO> list = getPolicyInfoListFromPathFlow(pathFlowROList, task,pathDetailRO,false);
            policyInfoList.addAll(list);
        }

        return policyInfoList;
    }

    List<PolicyInfoDTO> getPolicyInfoListFromPathFlow(List<PathFlowRO> pathFlowROList, PathInfoTaskDTO task,PathDetailRO pathDetailRO,boolean isRevert) {
        List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
        Integer ipType = task.getIpType();
        for(PathFlowRO pathFlowRO : pathFlowROList) {
            String srcIp,dstIp;
            if(ipType != null && IpTypeEnum.IPV6.getCode().equals(ipType)) {
                if (isRevert) {
                    srcIp = getIpList(pathFlowRO.getIp6SrcAddresses(), task.getDstIp());
                    dstIp = getIpList(pathFlowRO.getIp6DstAddresses(), task.getSrcIp());
                } else {
                    srcIp = getIpList(pathFlowRO.getIp6SrcAddresses(), task.getSrcIp());
                    dstIp = getIpList(pathFlowRO.getIp6DstAddresses(), task.getDstIp());
                }
            } else {
                if (isRevert) {
                    srcIp = getIpList(pathFlowRO.getIp4SrcAddresses(), task.getDstIp());
                    dstIp = getIpList(pathFlowRO.getIp4DstAddresses(), task.getSrcIp());
                } else {
                    srcIp = getIpList(pathFlowRO.getIp4SrcAddresses(), task.getSrcIp());
                    dstIp = getIpList(pathFlowRO.getIp4DstAddresses(), task.getDstIp());
                }

            }

            String protocolString = pathFlowRO.getProtocols() == null ? "0" : getSrcDstInteger(pathFlowRO.getProtocols());
            String dstPort = pathFlowRO.getDstPorts() == null ? null : getSrcDstInteger(pathFlowRO.getDstPorts());
            String srcPort = pathFlowRO.getSrcPorts() == null ? null : getSrcDstInteger(pathFlowRO.getSrcPorts());
            String[] protocols = protocolString.split(PolicyConstants.ADDRESS_SEPERATOR);

            for(String protocol: protocols) {
                List<ServiceDTO> serviceList = new ArrayList();
                ServiceDTO serviceDTO = new ServiceDTO();
                serviceDTO.setProtocol(protocol);
                serviceDTO.setDstPorts(dstPort);
                serviceDTO.setSrcPorts(srcPort);
                serviceList.add(serviceDTO);
                PolicyInfoDTO policyInfo = new PolicyInfoDTO();
                policyInfo.setSrcIp(srcIp);
                policyInfo.setDstIp(dstIp);
                policyInfo.setServiceList(serviceList);
                if(null != pathDetailRO){
                    policyInfo.setSnatName(pathDetailRO.getWhatIfSNatName());
                    policyInfo.setDnatName(pathDetailRO.getWhatIfDNatName());
                    policyInfo.setMatchType(null == pathDetailRO.getWhatIfMathType() ? null : pathDetailRO.getWhatIfMathType().toString());

                    List<ServiceDTO> matchPostServiceList = new ArrayList();
                    matchPostServiceList.add(serviceDTO);
                    policyInfo.setMatchPostServices(matchPostServiceList);
                }
                policyInfoList.add(policyInfo);
            }
        }
        return policyInfoList;
    }

    /**
     * 获取特殊设备的uuid
     * @param ruleListUuid 当前的RuleListUuid
     * @param node 设备信息
     * @return 策略集RuleListUuid
     */
    private String getRuleListUuid(String ruleListUuid, NodeEntity node) {
        //针对特殊设备获取其RuleListUUID
        String modelNumber = node.getModelNumber();

        //设备类型为空则直接返回当前ruleListUuid
        if (AliStringUtils.isEmpty(modelNumber)) {
            return ruleListUuid;
        }

        if (modelNumber.equals("USG6000")) {
            return  whaleService.getHuaweiRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("JuniperSRX")) {
            return whaleService.getJuniperSrxRuleListUuid(node.getUuid());
        } else if (modelNumber.contains("DPTech Firewall")) {
            return whaleService.getDpTechRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("Cisco ASA")) {
            return whaleService.getCiscoRuleListUuid(node.getUuid(), taskService.getAclDirection(node.getUuid()));
        } else if (modelNumber.contains("Fortinet")) {
            return whaleService.getFortinetTechRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("Topsec TOS Firewall")) {
            return whaleService.getTopsecRuleListUuid(node.getUuid());
        } else if (modelNumber.equals("H3C SecPath V7")) {
            return whaleService.getH3Cv7RuleListUuid(node.getUuid());
        }

        //不为上面所有设备则返回从相关策略中查找到的RuleListUuid
        return ruleListUuid;
    }

    /**
     * 获取域名
     * @param zoneRO 域数据对象
     * @param interfaceName 接口名称
     * @return 域名称
     */
    private String getZoneName(ZoneRO zoneRO, String interfaceName) {
        String zoneName = "";

        if(zoneRO == null) {
            return zoneName;
        }

        if(zoneRO.getData() == null) {
            return zoneName;
        }

        List<ZoneDataRO> zoneList = zoneRO.getData();

        if(zoneList.size() == 0 ) {
            return zoneName;
        }

        for(ZoneDataRO zoneDataRO: zoneList) {
            List<String> interfaceNameList = zoneDataRO.getInterfaceNames();

            if(interfaceNameList == null || interfaceNameList.size() == 0) {
                continue;
            }

            for(String name:interfaceNameList) {
                if(name.equals(interfaceName)) {
                    zoneName = zoneDataRO.getName();
                    return zoneName;
                }
            }
        }
        return zoneName;
    }


    /**
     * 获取设备NAT信息
     * @param deviceDetailRO 设备匹配详情
     * @return 设备NAT信息
     */
    private DeviceNatInfoDTO getDeivceNatInfo(DeviceDetailRO deviceDetailRO, String deviceUuid){
        DeviceNatInfoDTO deviceNatInfoDTO = new DeviceNatInfoDTO();
        return deviceNatInfoDTO;
    }

    /**
     * 将whale返回的IP地址列表（srcDstStringDTO）类型数据转换成字符串
     * @param ipList
     * @param srcIpStr
     * @return
     */
    private String getIpList(List<SrcDstStringDTO> ipList, String srcIpStr) {


        StringBuilder ipSb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(ipList)) {
            for (SrcDstStringDTO ipDTO : ipList) {
                ipSb.append(",");
                String startIp = ipDTO.getStart();
                String endIp = ipDTO.getEnd();
                if (startIp.equals(endIp)) {
                    ipSb.append(startIp);
                } else {
                    String originalIp = null;
                    if (StringUtils.isNotBlank(srcIpStr)) {
                        String[] ips = srcIpStr.split(",");

                        for (String ip : ips) {
                            if (IpUtils.isIPSegment(ip)) {
                                String start = IpUtils.getStartIpFromIpAddress(ip);
                                String end = IpUtils.getEndIpFromIpAddress(ip);
                                if (start.equals(startIp) && end.equals(endIp)) {
                                    originalIp = ip;
                                }
                            }
                        }
                    }

                    if (originalIp == null) {
                        ipSb.append(String.format("%s-%s", startIp, endIp));
                    } else {
                        ipSb.append(originalIp);
                    }
                }
            }
        }
        if (ipSb.length() > 1) {
            ipSb.deleteCharAt(0);
        }

        return ipSb.toString();
    }

    String getSrcDstInteger(List<SrcDstIntegerDTO> list) {
        StringBuilder sb = new StringBuilder();
        for (SrcDstIntegerDTO srcDstIntegerDTO : list) {
            sb.append(",");
            Integer start = srcDstIntegerDTO.getStart();
            Integer end = srcDstIntegerDTO.getEnd();
            if(start.equals(end)) {
                sb.append(String.valueOf(start));
            }else {
                sb.append(String.format("%s-%s", String.valueOf(start), String.valueOf(end)));
            }
        }
        if(sb.length() > 1) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }
    @Override
    public List<PolicyInfoDTO> mergePolicyInfoList(List<PolicyInfoDTO> policyInfoList) {
        List<PolicyInfoDTO> list = new ArrayList<>();
        for(PolicyInfoDTO newPolicyInfo : policyInfoList) {
            boolean canMerge = false;
            for(PolicyInfoDTO policyInfo : list) {
                if(policyInfo.getSrcIp().equals(newPolicyInfo.getSrcIp()) &&
                policyInfo.getDstIp().equals(newPolicyInfo.getDstIp())) {
                    List<ServiceDTO> serviceList = policyInfo.getServiceList();
                    List<ServiceDTO> newServiceList = newPolicyInfo.getServiceList();
                    serviceList = mergeServiceList(serviceList, newServiceList);
                    policyInfo.setServiceList(serviceList);
                    canMerge = true;
                }
            }

            if(!canMerge) {
                list.add(newPolicyInfo);
            }
        }
        return list;
    }


    String mergePort(String portListString, String newPortListString) {
        //若有一个端口为null，则说明端口为any，即包含所有端口
        if(portListString == null || newPortListString == null) {
            return null;
        }

        String[] portStrings = portListString.split(PolicyConstants.ADDRESS_SEPERATOR);
        String[] newPortStrings = newPortListString.split(PolicyConstants.ADDRESS_SEPERATOR);

        List<PortValue> portValueList = new ArrayList<>();
        List<PortValue> newPortValueList = new ArrayList<>();

        for(String portString : portStrings) {
            PortValue portValue = new PortValue(portString);
            portValueList.add(portValue);
        }

        for(String newPortString: newPortStrings) {
            PortValue portValue = new PortValue(newPortString);
            newPortValueList.add(portValue);
        }

        for(PortValue newPortValue : newPortValueList) {
            List<PortValue> mergedPortList = new ArrayList<>();
            for(PortValue portValue: portValueList) {
                if(newPortValue.getEnd() < (portValue.getStart() - 1)) {
                    //新端口的终止值小于源端口的起始值，不合并
                } else if ((newPortValue.getStart() - 1) > portValue.getEnd()) {
                    //新端口的起始值大于源端口的终止值，不合并
                } else {
                    //需要合并

                    //起始值谁小用谁
                    if(portValue.getStart() < newPortValue.getStart()) {
                        newPortValue.setStart(portValue.getStart());
                    }

                    //终止值谁大用谁
                    if(portValue.getEnd() > newPortValue.getEnd()) {
                        newPortValue.setEnd(portValue.getEnd());
                    }

                    //被合并值加入到移除队列
                    mergedPortList.add(portValue);
                }
            }
            //移除被合并过的端口
            portValueList.removeAll(mergedPortList);
            //添加新端口
            portValueList.add(newPortValue);
        }

        StringBuilder sb = new StringBuilder();
        for(PortValue port : portValueList) {
            sb.append(PolicyConstants.ADDRESS_SEPERATOR);
            sb.append(port.toString());
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    @Data
    class PortValue {
        int start;
        int end;

        public String toString() {
            if(start != end) {
                return String.format("%d-%d", start, end);
            } else {
                return String.format("%d", end);
            }
        }

        PortValue(String port) {
            if(PortUtils.isPortRange(port)) {
                String startString = PortUtils.getStartPort(port);
                String endString = PortUtils.getEndPort(port);
                start = Integer.valueOf(startString);
                end = Integer.valueOf(endString);
            } else {
                start = Integer.valueOf(port);
                end = Integer.valueOf(port);
            }
        }
    }

    List<ServiceDTO> mergeServiceList(List<ServiceDTO> serviceList, List<ServiceDTO> newServiceList) {
        for(ServiceDTO newService: newServiceList) {
            boolean hasSameProtocol = false;
            for(ServiceDTO service: serviceList) {
                //找到相同服务则合并端口
                if(service.getProtocol().trim().equals(newService.getProtocol().trim())) {
                    hasSameProtocol = true;
                    //若为icmp协议，则不用合并端口
                    if(service.getProtocol().equals(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                        break;
                    }
                    String dstPorts = service.getDstPorts();
                    String newDstPorts = newService.getDstPorts();
                    dstPorts = mergePort(dstPorts, newDstPorts);
                    service.setDstPorts(dstPorts);
                }
            }
            if(hasSameProtocol== false){
                serviceList.add(newService);
            }
        }

        return serviceList;
    }

    @Data
    class IpValue {
        long start;
        long end;
        String ipString;

        public String toString() {
            if(start != end) {
                return String.format("%s-%s", IpUtils.IPv4NumToString(start), IpUtils.IPv4NumToString(end));
            } else {
                return String.format("%s", IpUtils.IPv4NumToString(start));
            }
        }

        IpValue() {}

        IpValue(String ip) {
            ipString = ip;
            String startIp = IpUtils.getStartIpFromIpAddress(ip);
            String endIp = IpUtils.getEndIpFromIpAddress(ip);
            start = IpUtils.IPv4StringToNum(startIp);
            end = IpUtils.IPv4StringToNum(endIp);
        }
    }

    String getAddresses(String ipString, String originalIpString) {
        String[] ips = ipString.split(",");
        String[] originalIps = originalIpString.split(",");

        List<String> ipList = new ArrayList<>();
        List<String> originalList = new ArrayList<>();

        for(String ip:ips) {
            ipList.add(ip);
        }

        for(String ip:originalIps) {
            originalList.add(ip);
        }

        List<IpValue> originalIpValueList = new ArrayList<>();
        List<IpValue> ipValueList = new ArrayList<>();
        for(String ip: ipList) {
            IpValue ipValue = new IpValue(ip);
            ipValueList.add(ipValue);
        }

        for(String ip: originalList) {
            IpValue ipValue = new IpValue(ip);
            originalIpValueList.add(ipValue);
        }

        List<IpValue> total = new ArrayList<>();
        for(IpValue originalIpValue: originalIpValueList) {
            for(IpValue ipValue : ipValueList ) {
                if(ipValue.start <= originalIpValue.start && originalIpValue.end <= ipValue.end) {
                    List<IpValue> newIpList = new ArrayList<>();
                    if(ipValue.start < originalIpValue.start) {
                        String newIp = String.format("%s-%s", IpUtils.IPv4NumToString(ipValue.start), IpUtils.IPv4NumToString(originalIpValue.start-1));
//                        newIpList.add(newIp);
                        if(originalIpValue.start - 1 == ipValue.start) {
                            newIp = String.format("%s", IpUtils.IPv4NumToString(ipValue.start));
                        }
                        IpValue newIpValue = new IpValue();
                        newIpValue.setStart(ipValue.start);
                        newIpValue.setEnd(originalIpValue.start-1);
                        newIpValue.setIpString(newIp);
                        newIpList.add(newIpValue);
                    }

                    if(ipValue.end > originalIpValue.end) {
                        String newIp = String.format("%s-%s", IpUtils.IPv4NumToString(originalIpValue.end+1), IpUtils.IPv4NumToString(ipValue.end));

                        if(originalIpValue.end+1 == ipValue.end) {
                            newIp = String.format("%s", IpUtils.IPv4NumToString(ipValue.end));
                        }
//                        newIpList.add(newIp);
                        IpValue newIpValue = new IpValue();
                        newIpValue.setStart(originalIpValue.end+1);
                        newIpValue.setEnd(ipValue.end);
                        newIpValue.setIpString(newIp);
                        newIpList.add(newIpValue);
                    }
                    total.add(originalIpValue);

                    //移除当前的，增加切除后的
                    ipValueList.remove(ipValue);
                    ipValueList.addAll(newIpList);
                    break;
                }
            }
        }

        //增加残余的
        total.addAll(ipValueList);

        StringBuilder sb = new StringBuilder();
        for(IpValue ipValue: total) {
            sb.append(",");
            sb.append(ipValue.getIpString());
        }

        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }

        logger.debug("生成IP地址为：" + ipString);
        logger.debug("原始IP地址为：" + originalIpString);
        logger.debug("优化后IP地址为：" + sb.toString());

        return sb.toString();
    }


    public void test() {
        ServiceDTO serviceDTO1= new ServiceDTO();
        serviceDTO1.setDstPorts("30002");
        serviceDTO1.setProtocol("6");
        List<ServiceDTO> serviceList1 = new ArrayList<>();
        serviceList1.add(serviceDTO1);
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setDstPorts("30002");
        serviceDTO2.setProtocol("6");
        List<ServiceDTO> serviceList2 = new ArrayList<>();
        serviceList2.add(serviceDTO2);

        PolicyInfoDTO policyInfo1 = new PolicyInfoDTO();
        policyInfo1.setSrcIp("192.168.1.1");
        policyInfo1.setDstIp("192.168.2.1");
        policyInfo1.setServiceList(serviceList1);
        PolicyInfoDTO policyInfo2 = new PolicyInfoDTO();
        policyInfo2.setSrcIp("192.168.1.1");
        policyInfo2.setDstIp("192.168.2.1");
        policyInfo2.setServiceList(serviceList2);

        List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
        policyInfoList.add(policyInfo1);
        policyInfoList.add(policyInfo2);
        System.out.println(JSONObject.toJSONString(policyInfoList));
        policyInfoList = mergePolicyInfoList(policyInfoList);
        System.out.println(JSONObject.toJSONString(policyInfoList));
    }



    RecommendPolicyEntity getPolicyEntity(PathInfoTaskDTO pathInfo, PolicyInfoDTO policyInfo, NodeEntity node, String ruleListUuid,
                                          ZoneDataRO srcZone, ZoneDataRO dstZone, List<String> srcItf, List<String> dstItf,
                                          boolean isVsys, String rootDeviceUuid, String vsysName, Integer idleTimeout,SimulationTaskDTO simulationTaskDTO,
                                          PolicyEnum policyType,String aclRuleListName,String aclRuleIds) {
        RecommendPolicyEntity policyEntity = new RecommendPolicyEntity();

        //设置策略对应的任务和路径ID，task为PathInfoTaskDTO，因此id为pathInfoId，其taskId为taskId
        policyEntity.setTaskId(pathInfo.getTaskId());
        policyEntity.setPathInfoId(pathInfo.getId());

        //设置相关设备数据，以免后续过程读取
        policyEntity.setNode(node);
        policyEntity.setDeviceUuid(node.getUuid());

        //设置策略五元组
        policyEntity.setSrcIp(policyInfo.getSrcIp());
        String dstIp;
        if (simulationTaskDTO.getTaskType() == PolicyConstants.IN2OUT_INTERNET_RECOMMEND && StringUtils.isBlank(simulationTaskDTO.getDstIp())) {
            dstIp = "";
        } else {
            dstIp = policyInfo.getDstIp();
        }
        policyEntity.setDstIp(dstIp);
        policyEntity.setService(ServiceDTOUtils.toString(policyInfo.getServiceList()));
        policyEntity.setPolicySource(policyInfo.getPolicySource());

        //设置开始时间和结束时间
        policyEntity.setStartTime(pathInfo.getStartTime());
        policyEntity.setEndTime(pathInfo.getEndTime());

        //设置策略集RuleListUuid
        policyEntity.setRuleListUuid(ruleListUuid);
        //设置策略集RuleListName
        policyEntity.setRuleListName(aclRuleListName);
        // 匹配到的ruleId
        policyEntity.setMatchRuleId(aclRuleIds);

        //设置域和接口信息
        policyEntity.setSrcZone(srcZone == null ? "" : srcZone.getName());
        policyEntity.setDstZone(dstZone == null ? "" : dstZone.getName());

        //获取接口信息
        String srcInterface = srcItf.get(0);
        String dstInterface = dstItf.get(0);
        String srcInterfaceAlias = srcItf.get(1);
        String dstInterfaceAlias = dstItf.get(1);

        String modelNumber = node.getModelNumber();
        policyEntity.setInDevIf(srcInterface);
        policyEntity.setOutDevIf(dstInterface);
        //对于思科/思科8.4设备，接口别名存在设置别名，别名不存在设置接口名
        if(!AliStringUtils.isEmpty(modelNumber) && (modelNumber.equals("Cisco ASA") || modelNumber.equals("Cisco ASA 8.4")  || modelNumber.equals("Cisco ASA 9.9"))) {
            policyEntity.setInDevIf(AliStringUtils.isEmpty(srcInterfaceAlias) ? srcInterface : srcInterfaceAlias);
            policyEntity.setOutDevIf(AliStringUtils.isEmpty(dstInterfaceAlias) ? dstInterface : dstInterfaceAlias);
        }

        //设置虚设备信息，是否为徐设备，主设备的UUID和虚设备名称
        policyEntity.setVsys(isVsys);
        policyEntity.setRootDeviceUuid(rootDeviceUuid);
        policyEntity.setVsysName(vsysName);

        policyEntity.setIdleTimeout(idleTimeout);
        policyEntity.setPolicyType(policyType);
        policyEntity.setSnatName(policyInfo.getSnatName());
        policyEntity.setDnatName(policyInfo.getDnatName());
        policyEntity.setMatchType(policyInfo.getMatchType());
        policyEntity.setMatchPreServices(CollectionUtils.isNotEmpty(policyInfo.getMatchPreServices()) ? JSONObject.toJSONString(policyInfo.getMatchPreServices()) : null);
        policyEntity.setMatchPostServices(CollectionUtils.isNotEmpty(policyInfo.getMatchPostServices()) ? JSONObject.toJSONString(policyInfo.getMatchPostServices()) : null);
        return policyEntity;
    }

    /**
     * 将生成策略数据库对象列表转化成生成策略任务数据列表
     * @param entityList 生成策略列表
     * @return 生成策略任务对象列表
     */
    List<RecommendPolicyDTO> getPolicyDTOList(List<RecommendPolicyEntity> entityList) {
        List<RecommendPolicyDTO> dtoList = new ArrayList<>();
        for(RecommendPolicyEntity entity:entityList) {
            RecommendPolicyDTO dto = new RecommendPolicyDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setServiceList(ServiceDTOUtils.toList(entity.getService()));
            dto.setMatchPreServices(ServiceDTOUtils.toList(entity.getMatchPreServices()));
            dto.setMatchPostServices(ServiceDTOUtils.toList(entity.getMatchPostServices()));
            dtoList.add(dto);
        }
        return dtoList;
    }
}
