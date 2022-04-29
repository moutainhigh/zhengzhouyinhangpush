package com.abtnetworks.totems.push.manager.impl;

import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.manager.DenyPolicyInfoDTO;
import com.abtnetworks.totems.common.dto.manager.IpRangeDTO;
import com.abtnetworks.totems.common.dto.manager.MatchIpDTO;
import com.abtnetworks.totems.common.dto.manager.MatchServiceValueDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TotemsIp4Utils;
import com.abtnetworks.totems.common.utils.TotemsIp6Utils;
import com.abtnetworks.totems.push.manager.PolicyMergeNewTaskService;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.remote.nginz.ComplianceRemoteService;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.policy.dto.RuleCheckServiceDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policy.service.WhalePolicyClient;
import com.abtnetworks.totems.whale.policyoptimize.ro.*;
import com.abtnetworks.totems.whale.policyoptimize.service.WhaleRuleCheckClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.abtnetworks.totems.common.enums.PolicyEnum.EDIT_SECURITY;
import static com.abtnetworks.totems.common.enums.PolicyTypeEnum.SYSTEM__POLICY_1;
import static com.abtnetworks.totems.common.enums.PolicyTypeEnum.SYSTEM__POLICY_2;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/15
 */
@Slf4j
@Service
public class PolicyMergeNewTaskServiceImpl implements PolicyMergeNewTaskService {
    @Resource
    WhaleRuleCheckClient whaleRuleCheckClient;

    @Resource
    WhaleManager whaleManager;




    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    WhalePolicyClient whalePolicyClient;

    @Resource
    ComplianceRemoteService complianceRemoteService;

    @Resource
    CommandTaskEdiableMapper commandTaskEdiableMapper;


    @Override
    public DenyPolicyInfoDTO getPolicyIdByFirstDeny(CmdDTO cmdDTO,boolean advancedSettingOpen) {
        TaskDTO task = cmdDTO.getTask();
        Boolean beforeConflict = task.getBeforeConflict();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        ActionEnum action = policyDTO.getAction();
        if (((beforeConflict != null && beforeConflict) || advancedSettingOpen) && ActionEnum.PERMIT.getKey().equals(action.getKey())) {
            String policyType = PolicyTypeEnum.SYSTEM__POLICY_1 + "," + PolicyTypeEnum.SYSTEM__POLICY_2;
            List<DeviceFilterRuleListRO> resultRO = complianceRemoteService.getPolicyIdByDenyOrPermit(cmdDTO, ActionEnum.DENY.getKey().toUpperCase(),policyType,null);
            log.info("查询到deny策略返回有{}", JSONObject.toJSONString(resultRO));
            DenyPolicyInfoDTO denyPolicyInfoDTO = new DenyPolicyInfoDTO();
            if (CollectionUtils.isNotEmpty(resultRO)) {

                for (DeviceFilterRuleListRO deviceFilterRuleListRO : resultRO) {
                    String policyId = deviceFilterRuleListRO.getRuleId();
                    String policyName = deviceFilterRuleListRO.getName();
                    LinkedHashMap<String,String> miscFields = deviceFilterRuleListRO.getMiscFields();
                    if (StringUtils.isNotBlank(policyId)) {
                        denyPolicyInfoDTO.setPolicyId(policyId);
                        denyPolicyInfoDTO.setRuleName(policyName);
                        denyPolicyInfoDTO.setLineNum(Optional.ofNullable(miscFields).map(u -> u.get("line")).orElseGet(() -> ""));
                        break;
                    } else if (StringUtils.isNotBlank(policyName)) {
                        denyPolicyInfoDTO.setRuleName(policyName);
                        denyPolicyInfoDTO.setLineNum(Optional.ofNullable(miscFields).map(u -> u.get("line")).orElseGet(() -> ""));
                        break;
                    }
                }
            }
            log.info("获取到deny策略一条有{}", JSONObject.toJSONString(denyPolicyInfoDTO));
            return denyPolicyInfoDTO;
        } else {
            return null;
        }
    }


    @Override
    public void mergePolicyForGenerateParam(CmdDTO cmdDTO) {


        DeviceDTO deviceDTO = cmdDTO.getDevice();
        // 如果开启编辑策略建议开关，则生成策略编辑建议
        String ruleListUuid = cmdDTO.getDevice().getRuleListUuid();

        DenyPolicyInfoDTO policyIdByFirstDeny = cmdDTO.getPolicyIdByFirstDeny();
        //合并代码
        TaskDTO task = cmdDTO.getTask();
        Boolean mergeCheck = task.getMergeCheck();

        if (mergeCheck != null && mergeCheck) {

            if (StringUtils.isBlank(ruleListUuid)) {
                ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceDTO.getDeviceUuid());
                List<DeviceFilterlistRO> list = resultRO.getData();

                AtomicInteger count = new AtomicInteger();
                for (DeviceFilterlistRO filterlistRO : list) {
                    if (filterlistRO.getRuleListType().equals(SYSTEM__POLICY_1.getRuleListType()) || filterlistRO.getRuleListType().equals(SYSTEM__POLICY_2.getRuleListType())) {
                        switch (deviceDTO.getModelNumber()){
                            //华三V7可再过滤一部分策略
                            case H3CV7:
                                String tmpRuleListUuid = filterMergedPolicyByIp(cmdDTO, count, filterlistRO);
                                if(StringUtils.isNotEmpty(tmpRuleListUuid)){
                                    ruleListUuid = tmpRuleListUuid;
                                }
                                log.info("策略合并查询到策略集，通过ip类型策略集名称再过滤一次，得到数量："+count);
                                break;
                            case H3CV5:
                                String tmpRuleListUuid2 = filterMergedPolicyByZone(cmdDTO, count, filterlistRO);
                                if(StringUtils.isNotEmpty(tmpRuleListUuid2)){
                                    ruleListUuid = tmpRuleListUuid2;
                                }
                                log.info("策略合并查询到策略集，通过源域目的域策略集名称再过滤一次，得到数量："+count);
                                break;
                            default:
                                ruleListUuid = filterlistRO.getUuid();
                                count.addAndGet(1);
                        }
                    }
                }
                if (count.get() > 1) {
                    log.error("策略合并查询到策略集是多个参数为: {}", JSONObject.toJSONString(resultRO));
                    return;
                }
            }

            //组装五元组参数
            String deviceUuid = deviceDTO.getDeviceUuid();
            String modelNumber = deviceDTO.getModelNumber().getKey();
            RuleCheckPolicyRO ruleCheckPolicyVO = new RuleCheckPolicyRO();
            ruleCheckPolicyVO.setRuleListUuid(ruleListUuid);
            SimpleRuleRO simpleRuleRO = new SimpleRuleRO();
            PolicyDTO policy = cmdDTO.getPolicy();
            if (DeviceModelNumberEnum.isRangeFortCode(cmdDTO.getDevice().getModelNumber().getCode())) {
                if (policy.getExistDnat()) {
                    log.info("当前工单:{}存在飞塔目的NAT的转换,不执行策略合并的逻辑", task.getTheme());
                    return;
                }
            }

            Integer ipType = policy.getIpType() != null ? policy.getIpType() : IpTypeEnum.IPV4.getCode();
            String action = policy.getAction().getKey().toUpperCase();
            Integer idleTimeout = policy.getIdleTimeout();
            String startTime = policy.getStartTime();
            String endTime = policy.getEndTime();

            Set<String> ciscoModelSet = new HashSet<>();
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_S.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_ASA_86.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_ASA_99.getKey());
            if (ciscoModelSet.contains(modelNumber)) {
                //策略检查设置参数时先检查思科设备ACL策略是设置在in接口还是out接口。
                if (cmdDTO.getSetting().isOutBound()) {
                    log.debug("思科检查ACL策略在OUT接口");
                    if (StringUtils.isNotEmpty(policy.getDstItf())){
                        simpleRuleRO.setToInterfaceName(policy.getDstItf());
                    }
                } else {
                    log.debug("思科检查ACL策略在IN接口");
                    if (StringUtils.isNotEmpty(policy.getSrcItf())){
                        simpleRuleRO.setFromInterfaceName(policy.getSrcItf());
                    }
                }
            }

            Set<String> itfModelSet = new HashSet<>();
            itfModelSet.add(DeviceModelNumberEnum.ABTNETWORKS.getKey());
            itfModelSet.add(DeviceModelNumberEnum.SDNWARE.getKey());
            itfModelSet.add(DeviceModelNumberEnum.FORTINET.getKey());
            itfModelSet.add(DeviceModelNumberEnum.FORTINET_V5.getKey());
            itfModelSet.add(DeviceModelNumberEnum.FORTINET_V5_2.getKey());


            if (itfModelSet.contains(modelNumber)) {
                if (StringUtils.isNotEmpty(policy.getDstItf())){
                    simpleRuleRO.setToInterfaceName(policy.getDstItf());
                }
                if (StringUtils.isNotEmpty(policy.getSrcItf())){
                    simpleRuleRO.setFromInterfaceName(policy.getSrcItf());
                }
            }

            if (!StringUtils.isAllBlank(startTime,endTime)) {
                simpleRuleRO.setFromEffectDate(startTime);
                simpleRuleRO.setToEffectDate(endTime);
            }
            if (ObjectUtils.isNotEmpty(idleTimeout)) {
                simpleRuleRO.setIdleTimeout(idleTimeout);
            }

            if (StringUtils.isNotBlank(action)) {
                simpleRuleRO.setAction(action.toUpperCase());
            }
            String srcZone = policy.getSrcZone();

            if (StringUtils.isNotBlank(srcZone)) {
                simpleRuleRO.setFromZone(srcZone);
            }
            String dstZone = policy.getDstZone();

            if (StringUtils.isNotBlank(dstZone)) {
                simpleRuleRO.setToZone(dstZone);
            }

            String srcIp = policy.getSrcIp();
            List<String> srcIpList = new ArrayList<>();
            if (StringUtils.isNotBlank(srcIp)) {
                String[] srcIpStrings = srcIp.split(PolicyConstants.ADDRESS_SEPERATOR);
                for (String srcIpItem : srcIpStrings) {
                    srcIpList.add(srcIpItem);
                }
            }
            String dstIp =  policy.getDstIp();
            List<String> dstIpList = new ArrayList<>();
            if (StringUtils.isNotBlank(dstIp)) {
                String[] dstIpStrings = dstIp.split(PolicyConstants.ADDRESS_SEPERATOR);
                for (String dstIpItem : dstIpStrings) {
                    dstIpList.add(dstIpItem);
                }
            }
            List<SrcDstStringDTO> srcAddress = whaleManager.getSrcDstStringDTO(srcIpList,ipType);
            List<SrcDstStringDTO> dstAddress = whaleManager.getSrcDstStringDTO(dstIpList,ipType);
            if (CollectionUtils.isNotEmpty(srcAddress)) {
                if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
                    simpleRuleRO.setIp6SrcAddresses(srcAddress);
                } else {
                    simpleRuleRO.setIp4SrcAddresses(srcAddress);
                }
            }
            if (CollectionUtils.isNotEmpty(dstAddress)) {
                if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
                    simpleRuleRO.setIp6DstAddresses(dstAddress);
                } else {
                    simpleRuleRO.setIp4DstAddresses(dstAddress);
                }
            }
            ruleCheckPolicyVO.setSimpleRule(simpleRuleRO);
            List<RuleCheckServiceDTO> ruleCheckServiceDTOS = transferService(policy);
            if (CollectionUtils.isNotEmpty(ruleCheckServiceDTOS)) {
                simpleRuleRO.setServices(ruleCheckServiceDTOS);
            }
            ruleCheckPolicyVO.setSimpleRule(simpleRuleRO);
            //策略名字是，标识是我传递的入参，而不是策略（简单五元组）
            if (policyIdByFirstDeny != null) {
                String ruleName = policyIdByFirstDeny.getRuleName();
                String policyId = policyIdByFirstDeny.getPolicyId();
                if (StringUtils.isNotBlank(policyId)) {
                    log.info("当定位到deny的策略id不为空时BEFORE_RULE_ID{}", policyId);
                    ruleCheckPolicyVO.setLocationType("BEFORE_RULE_ID");
                    ruleCheckPolicyVO.setRuleId(policyId);
                } else if (StringUtils.isNotBlank(ruleName)) {
                    log.info("当定位到deny的策略id为空，策略名字不为空时BEFORE_RULE_NAME{}", ruleName);
                    ruleCheckPolicyVO.setRuleName(ruleName);
                    ruleCheckPolicyVO.setLocationType("BEFORE_RULE_NAME");
                } else {
                    log.info("当定位到deny的策略id为空，策略名字为空时,SimpleRule");
                    ruleCheckPolicyVO.setRuleName("SimpleRule");
                    ruleCheckPolicyVO.setLocationType("TOP");
                }

            } else {
                log.info("当没有定位到deny的策略时,SimpleRule");
                ruleCheckPolicyVO.setRuleName("SimpleRule");
                ruleCheckPolicyVO.setLocationType("TOP");
            }

            log.info("策略合并检查参数为:" + JSONObject.toJSONString(ruleCheckPolicyVO));
            RuleCheckResultRO ruleCheckResultRO = new RuleCheckResultRO();
            //KSH-6211【仿真开通】域名仿真生选合并策略时，应始终生成新建策略的命令行，不生成合并命令行。避免地址和域名的相互覆盖。
            if (!isDomain(policy.getDstIp())){
                ruleCheckResultRO = whaleRuleCheckClient.getRuleCheckResult(ruleCheckPolicyVO, deviceUuid, ruleListUuid);
            }
            log.info("策略合并检查返回参数为:" + JSONObject.toJSONString(ruleCheckResultRO));
            filterForGenerateParam(cmdDTO, ruleCheckResultRO, policyIdByFirstDeny);
        } else {
            filterForGenerateParam(cmdDTO, null, policyIdByFirstDeny);
        }

    }

    /**
     * 根据ip类型过滤可被合并的策略,
     * H3V7  IP4可合并security-policy ip策略集中的策略
     * H3V7  IP6可合并security-policy ipv6策略集中的策略
     * @param cmdDTO
     * @param count
     * @param filterlistRO
     */
    private String filterMergedPolicyByIp(CmdDTO cmdDTO, AtomicInteger count, DeviceFilterlistRO filterlistRO) {
        String ruleListUuid = "";
        if(IpTypeEnum.IPV4.getCode().equals(cmdDTO.getPolicy().getIpType()) && CommonConstants.POLICY_NAME_IP4.equals(filterlistRO.getName())){
            ruleListUuid = filterlistRO.getUuid();
            count.addAndGet(1);
        }else if(IpTypeEnum.IPV6.getCode().equals(cmdDTO.getPolicy().getIpType()) && CommonConstants.POLICY_NAME_IP6.equals(filterlistRO.getName())){
            ruleListUuid = filterlistRO.getUuid();
            count.addAndGet(1);
        }
        return ruleListUuid;
    }

    /**
     * 华三v5按源域目的域进行过滤
     * @param cmdDTO
     * @param count
     * @param filterlistRO
     * @return
     */
    private String filterMergedPolicyByZone(CmdDTO cmdDTO, AtomicInteger count, DeviceFilterlistRO filterlistRO) {
        String ruleListUuid = "";
        if(StringUtils.isNotEmpty(cmdDTO.getPolicy().getSrcZone())
            && StringUtils.isNotEmpty(cmdDTO.getPolicy().getDstZone())){
            String zoneName = cmdDTO.getPolicy().getSrcZone()+"-"+cmdDTO.getPolicy().getDstZone();
            if(filterlistRO.getName()!=null && filterlistRO.getName().endsWith(zoneName)){
                ruleListUuid = filterlistRO.getUuid();
                count.addAndGet(1);
            }else if(filterlistRO.getName()==null){
                ruleListUuid = filterlistRO.getUuid();
                count.addAndGet(1);
            }
        }else{
            ruleListUuid = filterlistRO.getUuid();
            count.addAndGet(1);
        }
        return ruleListUuid;
    }


    /**
     * 服务转化参数
     *
     * @param policy
     * @return
     */
    private List<RuleCheckServiceDTO> transferService(PolicyDTO policy) {
        List<ServiceDTO> serviceList = policy.getServiceList();
        List<RuleCheckServiceDTO> services = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(serviceList)) {
            for (ServiceDTO service : serviceList) {
                String protocolByString = ProtocolUtils.getProtocolByString(service.getProtocol());
                if (!protocolByString.equalsIgnoreCase(CommonConstants.ANY)) {

                    RuleCheckServiceDTO ruleCheckServiceDTO = new RuleCheckServiceDTO();

                    String dstPorts = service.getDstPorts();

                    if (StringUtils.isNotBlank(dstPorts) && !dstPorts.equalsIgnoreCase(CommonConstants.ANY)) {
                        ruleCheckServiceDTO.setDstPorts(whaleManager.getSrcDstIntegerDTOList(dstPorts));
                    }
                    ruleCheckServiceDTO.setProtocolName(protocolByString.toUpperCase());
                    services.add(ruleCheckServiceDTO);
                }

            }
        }
        return services;
    }

    @Override
    public void filterForGenerateParam(CmdDTO cmdDTO, RuleCheckResultRO ruleCheckResultRO, DenyPolicyInfoDTO policyIdByFirstDeny) {
        //首先看下是要合并的什么元素
        PolicyDTO policy = cmdDTO.getPolicy();
        Integer ipType = policy.getIpType() != null ? policy.getIpType() : IpTypeEnum.IPV4.getCode();
        if (ObjectUtils.isNotEmpty(ruleCheckResultRO) && CollectionUtils.isNotEmpty(ruleCheckResultRO.getData())) {
            List<RuleCheckResultDataRO> ruleCheckResultDataROS = ruleCheckResultRO.getData();

            boolean isMerge = false;
            for (RuleCheckResultDataRO ruleCheckResultDataRO : ruleCheckResultDataROS) {
                String bpcCode = ruleCheckResultDataRO.getBpcCode();
                if (PolicyCheckTypeEnum.MERGE.getCode().equals(bpcCode)) {
                    log.info("查询到一条可合并策略{}", JSONObject.toJSONString(ruleCheckResultDataRO));
                    CheckRuleRO primaryRule = ruleCheckResultDataRO.getPrimaryRule();

                    if (ObjectUtils.isNotEmpty(primaryRule)) {
                        RuleObjectRO ruleObject = primaryRule.getRuleObject();
                        String name = ruleObject.getName();

                        if (StringUtils.isNotBlank(name) && name.equalsIgnoreCase("SimpleRule")) {
                            List<CheckRuleRO> relatedRules = ruleCheckResultDataRO.getRelatedRules();
                            if (CollectionUtils.isNotEmpty(relatedRules)) {
                                CheckRuleRO checkRuleRO = relatedRules.get(0);
                                RuleObjectRO ruleObject1 = checkRuleRO.getRuleObject();
                                String uuid = ruleObject1.getUuid();
                                RuleObjectRO ruleObject2 = new RuleObjectRO();
                                BeanUtils.copyProperties(ruleObject1, ruleObject2);
                                primaryRule.setRuleObject(ruleObject2);
                                isMerge = filterAfterMerge(cmdDTO, primaryRule, uuid, ipType);
                                if (isMerge) {
                                    break;
                                }
                            }
                        } else {
                            RuleObjectRO ruleObject1 = primaryRule.getRuleObject();
                            if (ObjectUtils.isNotEmpty(ruleObject1)) {
                                String uuid = ruleObject1.getUuid();
                                isMerge = filterAfterMerge(cmdDTO, primaryRule, uuid, ipType);
                                if (isMerge) {
                                    break;
                                }
                            }
                        }


                    }

                } else {

                    log.info("这些属于非合并策略过滤掉{}", JSONObject.toJSONString(ruleCheckResultDataRO));
                }

            }
            if (!isMerge) {
                //所以新建策略
                this.setPolicyId2SwapId(policyIdByFirstDeny, cmdDTO);
                log.error("没有查询到可合并策略，走生成{}", JSONObject.toJSONString(cmdDTO));
            }
        } else {
            this.setPolicyId2SwapId(policyIdByFirstDeny, cmdDTO);
        }
    }

    /**
     * 设置可移动的策略
     *
     * @param policyIdByFirstDeny
     * @param cmdDTO
     */
    @Override
    public void setPolicyId2SwapId(DenyPolicyInfoDTO policyIdByFirstDeny, CmdDTO cmdDTO) {
        //所以新建策略
        if (ObjectUtils.isNotEmpty(policyIdByFirstDeny)) {
            String policyId = policyIdByFirstDeny.getPolicyId();
            String ruleName = policyIdByFirstDeny.getRuleName();
            TaskDTO task = cmdDTO.getTask();

            if (StringUtils.isNotBlank(policyId)) {
                CommandTaskEditableEntity commandTaskEditableEntity = new CommandTaskEditableEntity();
                // 判断是否有行号，目前只有思科支持行号移动
                if (StringUtils.isNotBlank(policyIdByFirstDeny.getLineNum())) {
                    String lineNum = policyIdByFirstDeny.getLineNum();
                    cmdDTO.getSetting().setPolicyId(lineNum);
                    cmdDTO.getSetting().setSwapNameId(lineNum);
                    commandTaskEditableEntity.setMovePosition(lineNum);
                } else {
                    cmdDTO.getSetting().setPolicyId(policyId);
                    cmdDTO.getSetting().setSwapNameId(policyId);
                    cmdDTO.getSetting().setPolicyName(ruleName);
                    commandTaskEditableEntity.setMovePosition(policyId);
                }
                cmdDTO.getSetting().setMoveSeatEnum(MoveSeatEnum.BEFORE);
                Integer id = task.getId();
                commandTaskEditableEntity.setId(id);
                commandTaskEdiableMapper.updateByPrimaryKeySelective(commandTaskEditableEntity);
            } else if (StringUtils.isNotBlank(ruleName)) {
                cmdDTO.getSetting().setPolicyName(ruleName);
                cmdDTO.getSetting().setMoveSeatEnum(MoveSeatEnum.BEFORE);
                cmdDTO.getSetting().setSwapNameId(ruleName);
                CommandTaskEditableEntity commandTaskEditableEntity = new CommandTaskEditableEntity();
                Integer id = task.getId();
                commandTaskEditableEntity.setId(id);
                commandTaskEditableEntity.setMovePosition(ruleName);
                commandTaskEdiableMapper.updateByPrimaryKeySelective(commandTaskEditableEntity);
            }
        }
    }

    /**
     * 获取解析数据
     *
     * @param ip4SrcAddresses
     * @param srcOrDstIp
     */
    private void commonIpParam(JSONArray ip4SrcAddresses, List<MatchIpDTO> srcOrDstIp, Integer ipType) {
        if (ip4SrcAddresses != null) {
            for (int j = 0; j < ip4SrcAddresses.size(); j++) {
                MatchIpDTO matchIpDTO = new MatchIpDTO();
                IpRangeDTO ipRangeDTO = new IpRangeDTO();
                ipRangeDTO.setStart(ip4SrcAddresses.getJSONObject(j).getString("start"));
                ipRangeDTO.setEnd(ip4SrcAddresses.getJSONObject(j).getString("end"));
                matchIpDTO.setIp4Range(ipRangeDTO);
                matchIpDTO.setType(ipType);
                srcOrDstIp.add(matchIpDTO);
            }

        }
    }

    private void commonServiceParam(JSONArray dstPorts, JSONArray protocols, List<MatchServiceValueDTO> services) {
        if (protocols != null) {
            MatchServiceValueDTO matchServiceValueDTO = new MatchServiceValueDTO();

            //协议取一个，比较高危端口，只需端口
            String start = protocols.getJSONObject(0).getString("start");
            matchServiceValueDTO.setProtocolName(start);
            List<String> dstPortValues = new ArrayList<>();
            if (dstPorts != null) {
                List<IpRangeDTO> ipRangeDTOS = dstPorts.toJavaList(IpRangeDTO.class);
                for (IpRangeDTO ipRangeDTO : ipRangeDTOS) {
                    String port = ipRangeDTO.toString();

                    dstPortValues.add(port);
                }
                matchServiceValueDTO.setDstPortValues(dstPortValues);
            }

            services.add(matchServiceValueDTO);
        }
    }

    /**
     * 开始从合并策略中找到对应合并策略
     *
     * @param cmdDTO
     * @param primaryRule
     * @param uuid
     * @param ipType
     * @return
     */
    private boolean filterAfterMerge(CmdDTO cmdDTO, CheckRuleRO primaryRule, String uuid, Integer ipType) {

        boolean isMerge = false;

        log.info("开始获取可合并策略的参数多条任取一条{}", JSONObject.toJSONString(primaryRule));


        String ruleMergeField = primaryRule.getRuleMergeField();

        if (StringUtils.isNotBlank(ruleMergeField)) {
            // 获取新建策略数据的五元组信息
            PolicyDTO policy = cmdDTO.getPolicy();
            if (QuintetEnum.SRC.getCode().equalsIgnoreCase(ruleMergeField)) {
                buildData(PolicyMergePropertyEnum.MERGE_SRC_IP.getCode(),
                        policy.getSrcIp(), cmdDTO, primaryRule);
                isMerge = true;
            } else if (QuintetEnum.DST.getCode().equalsIgnoreCase(ruleMergeField)) {
                // 飞塔特殊处理
                if (DeviceModelNumberEnum.isRangeFortCode(cmdDTO.getDevice().getModelNumber().getCode())) {
                    if (!specialDealForFortinet(primaryRule, PolicyMergePropertyEnum.MERGE_DST_IP.getCode())) {
                        log.info("工单:{},飞塔查询到墙上的可合并策略的目的地址不是地址对象可能是vip，不进行策略合并", cmdDTO.getTask().getTheme());
                        return false;
                    }
                }
                buildData(PolicyMergePropertyEnum.MERGE_DST_IP.getCode(),
                        policy.getDstIp(), cmdDTO, primaryRule);
                isMerge = true;
            } else if (QuintetEnum.SERVICE.getCode().equalsIgnoreCase(ruleMergeField)) {
                String service = JSONObject.toJSONString(policy.getServiceList());
                buildData(PolicyMergePropertyEnum.MERGE_SERVICE.getCode(),
                        service, cmdDTO, primaryRule);
                isMerge = true;
            } else {
                log.info("不支持此{}合并", ruleMergeField);
            }

        } else {
            log.info("返回合并策略中matchClause为空");
        }

        return isMerge;
    }

    /**
     * 构建编辑策略建议数据
     *
     * @param mergeProperty
     * @param mergeValue
     * @param cmdDTO
     * @param checkRuleRO
     * @return
     */
    @Override
    public void buildData(Integer mergeProperty, String mergeValue, CmdDTO cmdDTO,
                          CheckRuleRO checkRuleRO) {


        // 设置合并策略名称
        PolicyDTO newPolicy = cmdDTO.getPolicy();
        if(PolicyEnum.SECURITY.equals(newPolicy.getType())){
            newPolicy.setType(EDIT_SECURITY);
        }

        RuleObjectRO ruleObject = checkRuleRO.getRuleObject();
        String name = ruleObject.getName();
        String ruleId = ruleObject.getRuleId();
        if (StringUtils.isNotBlank(name)) {
            newPolicy.setEditPolicyName(name);
        } else {
            //todo 暂定都为空不存在
            newPolicy.setEditPolicyName(ruleId);
        }
        if(!StringUtils.isAnyEmpty(ruleObject.getDescription(),newPolicy.getDescription())){
            newPolicy.setDescription(ruleObject.getDescription()+","+newPolicy.getDescription());
        }
        newPolicy.setMergeProperty(mergeProperty);
        newPolicy.setMergeValue(mergeValue);
        PolicyRecommendSecurityPolicyDTO securityPolicy = newPolicy.getSecurityPolicy();
        if(ObjectUtils.isEmpty(securityPolicy)){
            securityPolicy = new PolicyRecommendSecurityPolicyDTO();
        }
        BeanUtils.copyProperties(ruleObject,securityPolicy);
        newPolicy.setSecurityPolicy(securityPolicy);
    }

    private boolean isDomain(String ip){
        if(StringUtils.isNotEmpty(ip)){
            String[] ips = ip.split(",");
            for (String apAddress : ips) {
                if (TotemsIp6Utils.isIp6(apAddress) || TotemsIp6Utils.isIp6Mask(apAddress) || TotemsIp6Utils.isIp6Range(apAddress) ||
                        TotemsIp4Utils.isIp4(apAddress) || TotemsIp4Utils.isIp4Mask(apAddress) || TotemsIp4Utils.isIp4Range(apAddress)){
                    continue;
                }else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 飞塔查询到墙上的可合并策略的目的地址是vip的话，不进行策略合并
     * @param primaryRule
     * @param primaryRule
     * @return
     */
    private boolean specialDealForFortinet(CheckRuleRO primaryRule,Integer mergeProperty){
        if (null == primaryRule) {
            return false;
        }
        RuleObjectRO ruleObject = primaryRule.getRuleObject();

        JSONObject matchClause = ruleObject.getMatchClause();
        String theKey = "dstIp";
        switch (mergeProperty){
            case 0:theKey = "srcIp";break;
            case 1:theKey = "dstIp";break;
            case 2:theKey = "services";
        }
        String srcIpString = matchClause.getString(theKey);
        JSONArray srcArray = JSON.parseArray(srcIpString);
        for(int i = 0;i<srcArray.size();i++){
            JSONObject srcObject = srcArray.getJSONObject(i);
            String nameRef = srcObject.getString("type");
            if(!Constants.OBJECT.equals(nameRef)){
                return false;
            }
            log.info("合并时获取到原策略属性为：{},name:{}",theKey,nameRef);
        }
        return true;
    }

}
