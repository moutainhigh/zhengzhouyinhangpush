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
 * @Description: ???????????????
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
            log.info("?????????deny???????????????{}", JSONObject.toJSONString(resultRO));
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
            log.info("?????????deny???????????????{}", JSONObject.toJSONString(denyPolicyInfoDTO));
            return denyPolicyInfoDTO;
        } else {
            return null;
        }
    }


    @Override
    public void mergePolicyForGenerateParam(CmdDTO cmdDTO) {


        DeviceDTO deviceDTO = cmdDTO.getDevice();
        // ??????????????????????????????????????????????????????????????????
        String ruleListUuid = cmdDTO.getDevice().getRuleListUuid();

        DenyPolicyInfoDTO policyIdByFirstDeny = cmdDTO.getPolicyIdByFirstDeny();
        //????????????
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
                            //??????V7???????????????????????????
                            case H3CV7:
                                String tmpRuleListUuid = filterMergedPolicyByIp(cmdDTO, count, filterlistRO);
                                if(StringUtils.isNotEmpty(tmpRuleListUuid)){
                                    ruleListUuid = tmpRuleListUuid;
                                }
                                log.info("???????????????????????????????????????ip??????????????????????????????????????????????????????"+count);
                                break;
                            case H3CV5:
                                String tmpRuleListUuid2 = filterMergedPolicyByZone(cmdDTO, count, filterlistRO);
                                if(StringUtils.isNotEmpty(tmpRuleListUuid2)){
                                    ruleListUuid = tmpRuleListUuid2;
                                }
                                log.info("??????????????????????????????????????????????????????????????????????????????????????????????????????"+count);
                                break;
                            default:
                                ruleListUuid = filterlistRO.getUuid();
                                count.addAndGet(1);
                        }
                    }
                }
                if (count.get() > 1) {
                    log.error("????????????????????????????????????????????????: {}", JSONObject.toJSONString(resultRO));
                    return;
                }
            }

            //?????????????????????
            String deviceUuid = deviceDTO.getDeviceUuid();
            String modelNumber = deviceDTO.getModelNumber().getKey();
            RuleCheckPolicyRO ruleCheckPolicyVO = new RuleCheckPolicyRO();
            ruleCheckPolicyVO.setRuleListUuid(ruleListUuid);
            SimpleRuleRO simpleRuleRO = new SimpleRuleRO();
            PolicyDTO policy = cmdDTO.getPolicy();
            if (DeviceModelNumberEnum.isRangeFortCode(cmdDTO.getDevice().getModelNumber().getCode())) {
                if (policy.getExistDnat()) {
                    log.info("????????????:{}??????????????????NAT?????????,??????????????????????????????", task.getTheme());
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
                //????????????????????????????????????????????????ACL??????????????????in????????????out?????????
                if (cmdDTO.getSetting().isOutBound()) {
                    log.debug("????????????ACL?????????OUT??????");
                    if (StringUtils.isNotEmpty(policy.getDstItf())){
                        simpleRuleRO.setToInterfaceName(policy.getDstItf());
                    }
                } else {
                    log.debug("????????????ACL?????????IN??????");
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
            //????????????????????????????????????????????????????????????????????????????????????
            if (policyIdByFirstDeny != null) {
                String ruleName = policyIdByFirstDeny.getRuleName();
                String policyId = policyIdByFirstDeny.getPolicyId();
                if (StringUtils.isNotBlank(policyId)) {
                    log.info("????????????deny?????????id????????????BEFORE_RULE_ID{}", policyId);
                    ruleCheckPolicyVO.setLocationType("BEFORE_RULE_ID");
                    ruleCheckPolicyVO.setRuleId(policyId);
                } else if (StringUtils.isNotBlank(ruleName)) {
                    log.info("????????????deny?????????id?????????????????????????????????BEFORE_RULE_NAME{}", ruleName);
                    ruleCheckPolicyVO.setRuleName(ruleName);
                    ruleCheckPolicyVO.setLocationType("BEFORE_RULE_NAME");
                } else {
                    log.info("????????????deny?????????id??????????????????????????????,SimpleRule");
                    ruleCheckPolicyVO.setRuleName("SimpleRule");
                    ruleCheckPolicyVO.setLocationType("TOP");
                }

            } else {
                log.info("??????????????????deny????????????,SimpleRule");
                ruleCheckPolicyVO.setRuleName("SimpleRule");
                ruleCheckPolicyVO.setLocationType("TOP");
            }

            log.info("???????????????????????????:" + JSONObject.toJSONString(ruleCheckPolicyVO));
            RuleCheckResultRO ruleCheckResultRO = new RuleCheckResultRO();
            //KSH-6211??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (!isDomain(policy.getDstIp())){
                ruleCheckResultRO = whaleRuleCheckClient.getRuleCheckResult(ruleCheckPolicyVO, deviceUuid, ruleListUuid);
            }
            log.info("?????????????????????????????????:" + JSONObject.toJSONString(ruleCheckResultRO));
            filterForGenerateParam(cmdDTO, ruleCheckResultRO, policyIdByFirstDeny);
        } else {
            filterForGenerateParam(cmdDTO, null, policyIdByFirstDeny);
        }

    }

    /**
     * ??????ip?????????????????????????????????,
     * H3V7  IP4?????????security-policy ip?????????????????????
     * H3V7  IP6?????????security-policy ipv6?????????????????????
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
     * ??????v5??????????????????????????????
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
     * ??????????????????
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
        //???????????????????????????????????????
        PolicyDTO policy = cmdDTO.getPolicy();
        Integer ipType = policy.getIpType() != null ? policy.getIpType() : IpTypeEnum.IPV4.getCode();
        if (ObjectUtils.isNotEmpty(ruleCheckResultRO) && CollectionUtils.isNotEmpty(ruleCheckResultRO.getData())) {
            List<RuleCheckResultDataRO> ruleCheckResultDataROS = ruleCheckResultRO.getData();

            boolean isMerge = false;
            for (RuleCheckResultDataRO ruleCheckResultDataRO : ruleCheckResultDataROS) {
                String bpcCode = ruleCheckResultDataRO.getBpcCode();
                if (PolicyCheckTypeEnum.MERGE.getCode().equals(bpcCode)) {
                    log.info("??????????????????????????????{}", JSONObject.toJSONString(ruleCheckResultDataRO));
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

                    log.info("????????????????????????????????????{}", JSONObject.toJSONString(ruleCheckResultDataRO));
                }

            }
            if (!isMerge) {
                //??????????????????
                this.setPolicyId2SwapId(policyIdByFirstDeny, cmdDTO);
                log.error("??????????????????????????????????????????{}", JSONObject.toJSONString(cmdDTO));
            }
        } else {
            this.setPolicyId2SwapId(policyIdByFirstDeny, cmdDTO);
        }
    }

    /**
     * ????????????????????????
     *
     * @param policyIdByFirstDeny
     * @param cmdDTO
     */
    @Override
    public void setPolicyId2SwapId(DenyPolicyInfoDTO policyIdByFirstDeny, CmdDTO cmdDTO) {
        //??????????????????
        if (ObjectUtils.isNotEmpty(policyIdByFirstDeny)) {
            String policyId = policyIdByFirstDeny.getPolicyId();
            String ruleName = policyIdByFirstDeny.getRuleName();
            TaskDTO task = cmdDTO.getTask();

            if (StringUtils.isNotBlank(policyId)) {
                CommandTaskEditableEntity commandTaskEditableEntity = new CommandTaskEditableEntity();
                // ????????????????????????????????????????????????????????????
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
     * ??????????????????
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

            //???????????????????????????????????????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param cmdDTO
     * @param primaryRule
     * @param uuid
     * @param ipType
     * @return
     */
    private boolean filterAfterMerge(CmdDTO cmdDTO, CheckRuleRO primaryRule, String uuid, Integer ipType) {

        boolean isMerge = false;

        log.info("??????????????????????????????????????????????????????{}", JSONObject.toJSONString(primaryRule));


        String ruleMergeField = primaryRule.getRuleMergeField();

        if (StringUtils.isNotBlank(ruleMergeField)) {
            // ??????????????????????????????????????????
            PolicyDTO policy = cmdDTO.getPolicy();
            if (QuintetEnum.SRC.getCode().equalsIgnoreCase(ruleMergeField)) {
                buildData(PolicyMergePropertyEnum.MERGE_SRC_IP.getCode(),
                        policy.getSrcIp(), cmdDTO, primaryRule);
                isMerge = true;
            } else if (QuintetEnum.DST.getCode().equalsIgnoreCase(ruleMergeField)) {
                // ??????????????????
                if (DeviceModelNumberEnum.isRangeFortCode(cmdDTO.getDevice().getModelNumber().getCode())) {
                    if (!specialDealForFortinet(primaryRule, PolicyMergePropertyEnum.MERGE_DST_IP.getCode())) {
                        log.info("??????:{},?????????????????????????????????????????????????????????????????????????????????vip????????????????????????", cmdDTO.getTask().getTheme());
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
                log.info("????????????{}??????", ruleMergeField);
            }

        } else {
            log.info("?????????????????????matchClause??????");
        }

        return isMerge;
    }

    /**
     * ??????????????????????????????
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


        // ????????????????????????
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
            //todo ????????????????????????
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
     * ?????????????????????????????????????????????????????????vip??????????????????????????????
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
            log.info("???????????????????????????????????????{},name:{}",theKey,nameRef);
        }
        return true;
    }

}
