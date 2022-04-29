package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.WhaleDoUtils;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.CheckResultEntity;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.CheckService;
import com.abtnetworks.totems.whale.policy.dto.RuleCheckServiceDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstIntegerDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policyoptimize.ro.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/13 17:35
 */
@Service
public class CheckServiceImpl implements CheckService {

    private static Logger logger = LoggerFactory.getLogger(CheckServiceImpl.class);

    @Value("${push.recommend.check-policy-max}")
    private Integer checkPolicyMax;

    @Autowired
    RecommendTaskManager taskService;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    WhaleManager whaleService;


    @Override
    public int checkPolicyByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO) {
        if(!taskService.isCheckRule() || simulationTaskDTO.getTaskType() == PolicyConstants.BIG_INTERNET_RECOMMEND) {
            logger.info(String.format("任务(%d)[%s]路径%d策略检查关闭，跳过策略检查...", task.getId(), task.getTheme(), task.getId()));
            taskService.updatePathCheckStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_CHECK_SKIPEED);
            return ReturnCode.POLICY_MSG_OK;
        }  else {
            logger.info(String.format("任务(%d)[%s]路径%d开始预生效策略检查...", task.getTaskId(), task.getTheme(), task.getId()));
        }

        List<RecommendPolicyDTO> list = task.getPolicyList();
        boolean hasRule = false;
        for (RecommendPolicyDTO recommendPolicyDTO : list) {
            //先检查策略集UUID，若不存在则不进行后续步骤
            String ruleListUuid = recommendPolicyDTO.getRuleListUuid();
            if (AliStringUtils.isEmpty(ruleListUuid)) {
                logger.warn(String.format("任务(%d)[%s]路径%d设备%s策略集UUID为空，不进行策略检查", task.getTaskId(), task.getTheme(), task.getId(), recommendPolicyDTO.getDeviceUuid()));
                continue;
            }

            String deviceUuid = recommendPolicyDTO.getDeviceUuid();
            String modelNumber = taskService.getDeviceModelNumber(deviceUuid);

            List<ServiceDTO> serviceList = recommendPolicyDTO.getServiceList();

            //组成策略检查参数对象
            RuleCheckPolicyRO ruleCheckPolicyVO = new RuleCheckPolicyRO();
            SimpleRuleRO simpleRuleRO = new SimpleRuleRO();

            List<SrcDstStringDTO> srcAddress = whaleService.getSrcDstStringDTO(recommendPolicyDTO.getSrcIp());
            List<SrcDstStringDTO> dstAddress = whaleService.getSrcDstStringDTO(recommendPolicyDTO.getDstIp());


            simpleRuleRO.setIp4SrcAddresses(srcAddress);
            simpleRuleRO.setIp4DstAddresses(dstAddress);
            simpleRuleRO.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
            List<RuleCheckServiceDTO> services = new ArrayList<>();
            if(serviceList != null) {
                for (ServiceDTO service : serviceList) {
                    RuleCheckServiceDTO ruleCheckServiceDTO = new RuleCheckServiceDTO();
                    ruleCheckServiceDTO.setSrcPorts(whaleService.getSrcDstIntegerDTOList(service.getSrcPorts()));
                    ruleCheckServiceDTO.setDstPorts(whaleService.getSrcDstIntegerDTOList(service.getDstPorts()));
                    ruleCheckServiceDTO.setProtocolName(ProtocolUtils.getProtocolByString(service.getProtocol()));
                    services.add(ruleCheckServiceDTO);
                }
            }
            simpleRuleRO.setServices(services);

            //预变更策略检查添加接口/域参数说明
//            1. cisco：思科策略生成时需用到接口，因此预变更策略检查添加接口
//            2. abt：安博通策略生成时需要用到接口，因此预变更策略检查添加接口
//            3. hillstone：生成策略带zone，因此预变更策略检查添加zone
//            4. junipersrx：生成策略带zone，因此预变更策略添加zone
//            5. juniperssg: 生成策略带zone，因此预变更策略添加zone
//            6. topsec：生成策略带zone，因此预变更策略检查添加zone
//            7. usg2100：生成策略带zone，因此预变更策略检查添加zone
//            8. usg6000：生成策略带zone，因此预变更策略检查添加zone

            //2019-05-06 新增：根据高级设置决定策略检查是否带zone

            Set<String> ciscoModelSet = new HashSet<>();
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_S.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_ASA_86.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_ASA_99.getKey());
            if (ciscoModelSet.contains(modelNumber)) {
                //策略检查设置参数时先检查思科设备ACL策略是设置在in接口还是out接口。
                if (recommendPolicyDTO.getAclDirection() == AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION) {
                    logger.debug("思科检查ACL策略在OUT接口");
                    simpleRuleRO.setToInterfaceName(recommendPolicyDTO.getOutDevIf());
                } else {
                    logger.debug("思科检查ACL策略在IN接口");
                    simpleRuleRO.setFromInterfaceName(recommendPolicyDTO.getInDevIf());
                }
            }

            Set<String> itfModelSet = new HashSet<>();
            itfModelSet.add(DeviceModelNumberEnum.ABTNETWORKS.getKey());
            //itfModelSet.add("abtnetworks");
            //itfModelSet.add("sapling");
            //itfModelSet.add("Sapling");
            if (itfModelSet.contains(modelNumber)) {
                simpleRuleRO.setFromInterfaceName(recommendPolicyDTO.getInDevIf());
                simpleRuleRO.setToInterfaceName(recommendPolicyDTO.getOutDevIf());
            }

            Set<String> zoneModelSet = new HashSet<>();
            zoneModelSet.add(DeviceModelNumberEnum.USG6000.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.USG6000_NO_TOP.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.USG2000.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.SRX.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.SRX_NoCli.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.SSG.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.HILLSTONE.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.HILLSTONE_R5.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.TOPSEC_NG.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.TOPSEC_TOS_005.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.TOPSEC_TOS_010_020.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.DPTECHR003.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.DPTECHR004.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.FORTINET.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.FORTINET_V5_2.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.H3CV7.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.H3CV5.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.VENUSTECHVSOS.getKey());
            if (zoneModelSet.contains(modelNumber)) {
                //根据高级设置决定策略检查是否需要设置源域和目的域。
                if (recommendPolicyDTO.getSpecifyZone() == AdvancedSettingsConstants.PARAM_INT_SET_NO_ZONE) {
                    logger.debug(String.format("设备(%s)不指定域信息...策略检查参数不指定域", deviceUuid));
                } else if (recommendPolicyDTO.getSpecifyZone() == AdvancedSettingsConstants.PARAM_INT_SET_SRC_ZONE) {
                    logger.debug(String.format("设备(%s)指定源域信息...策略检查参数指定源域", deviceUuid));
                    simpleRuleRO.setFromZone(recommendPolicyDTO.getSrcZone());
                } else if (recommendPolicyDTO.getSpecifyZone() == AdvancedSettingsConstants.PARAM_INT_SET_DST_ZONE) {
                    logger.debug(String.format("设备(%s)指定目的域信息...策略检查参数指定目的域", deviceUuid));
                    simpleRuleRO.setToZone(recommendPolicyDTO.getDstZone());
                } else {
                    logger.debug(String.format("设备(%s)使用默认方式设置域...策略检查参数指定源域和目的域", deviceUuid));
                    simpleRuleRO.setFromZone(recommendPolicyDTO.getSrcZone());
                    simpleRuleRO.setToZone(recommendPolicyDTO.getDstZone());
                }
            }

//                FIRST(1, "最前面"),
//                        LAST(2,"最后面"),
//                        BEFORE(3, "在之前",  "before"),
//                        AFTER(4, "在之后", "after"),
            int movePolicy = recommendPolicyDTO.getMovePolicy();
            if (movePolicy == AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_FIRST) {
                ruleCheckPolicyVO.setLocationType("TOP");
            } else if (movePolicy == AdvancedSettingsConstants.PARAM_INT_NOT_MOVE_POLICY) {
                ruleCheckPolicyVO.setLocationType("BOTTOM");
            } else if (movePolicy == AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_BEFORE) {
                String policy = recommendPolicyDTO.getSpecificPosition();
                if (AliStringUtils.isNumeric(policy)) {
                    //如果是思科设备的话 思科移动是按照行号进行检查
                    if (DeviceModelNumberEnum.CISCO.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_S.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_86.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_99.getKey().contains(modelNumber)) {
                        ruleCheckPolicyVO.setLocationType("BEFORE_RULE_INDEX");
                        ruleCheckPolicyVO.setRuleIndex(Integer.parseInt(policy));
                    } else {
                        ruleCheckPolicyVO.setLocationType("BEFORE_RULE_ID");
                        ruleCheckPolicyVO.setRuleId(policy);
                    }
                } else {
                    ruleCheckPolicyVO.setLocationType("BEFORE_RULE_NAME");
                    ruleCheckPolicyVO.setRuleName(policy);
                }
            } else if (movePolicy == AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_AFTER) {
                String policy = recommendPolicyDTO.getSpecificPosition();
                if (AliStringUtils.isNumeric(policy)) {
                    if (DeviceModelNumberEnum.CISCO.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_S.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_86.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_99.getKey().contains(modelNumber)) {
                        ruleCheckPolicyVO.setLocationType("AFTER_RULE_INDEX");
                        ruleCheckPolicyVO.setRuleIndex(Integer.parseInt(policy));
                    } else {
                        ruleCheckPolicyVO.setLocationType("AFTER_RULE_ID");
                        ruleCheckPolicyVO.setRuleId(policy);
                    }
                } else {
                    ruleCheckPolicyVO.setLocationType("AFTER_RULE_NAME");
                    ruleCheckPolicyVO.setRuleName(policy);
                }
            }
            ruleCheckPolicyVO.setRuleListUuid(ruleListUuid);
            ruleCheckPolicyVO.setSimpleRule(simpleRuleRO);

            //调用接口进行策略检察
            logger.debug("策略检查参数为:\n" + JSONObject.toJSONString(ruleCheckPolicyVO));
            RuleCheckResultRO ruleCheckResultRO = whaleService.getRuleCheckResult(ruleCheckPolicyVO, deviceUuid, ruleListUuid);
            List<RuleCheckResultDataRO> ruleCheckResultDataVOList = ruleCheckResultRO.getData();
            if (ruleCheckResultDataVOList == null || ruleCheckResultDataVOList.size() == 0) {
                logger.debug("策略检查结果：没有相关策略...");
                continue;
            }

            hasRule = true;
            //保存检查到的相关策略
            int policyId = recommendPolicyDTO.getId();
            CheckResultEntity entity = new CheckResultEntity();
            entity.setPolicyId(policyId);
            String jsonString = JSONObject.toJSONString(ruleCheckResultRO);
            logger.debug("策略检查结果：有相关策略，相关策略为：\n" + jsonString);
            entity.setCheckResult(jsonString);
            taskService.addCheckResult(entity);

            //需要合并策略才进行合并策略检查
            if (recommendPolicyDTO.getCreatePolicy() == AdvancedSettingsConstants.PARAM_INT_MERGE_RULE) {
                logger.debug("检查相关策略中的可合并策略");
                //保存可合并策略信息
                if (recommendPolicyDTO.getCreatePolicy() == 1) {
                    boolean getMergeRuleName = false;
                    for (RuleCheckResultDataRO checkResultDataRO : ruleCheckResultDataVOList) {

                        if (checkResultDataRO.getBpcCode().equalsIgnoreCase("RC_MERGE_RULE")) {
                            //检测相关策略，若为目的合并，则不加入到合并列表
                            List<CheckRuleRO> checkList = checkResultDataRO.getRelatedRules();
                            if (checkList == null || checkList.size() == 0) {
                                logger.debug("相关规则为空！");
                                continue;
                            }
                            for (CheckRuleRO checkRuleRO : checkList) {
                                String mergeField = checkRuleRO.getRuleMergeField();
                                if (!AliStringUtils.isEmpty(mergeField)) {
                                    logger.debug("合并类型：" + mergeField);
                                    if (!mergeField.equalsIgnoreCase("DST")) {
                                        logger.debug("目的地址不合并, 跳过该可合并策略...");
                                        continue;
                                    }
                                    PolicyMergeDTO mergeDTO = new PolicyMergeDTO();
                                    //策略ID
                                    String ruleId = checkRuleRO.getRuleObject().getRuleId();
                                    if (!AliStringUtils.isEmpty(ruleId)) {
                                        mergeDTO.setRuleId(ruleId);
                                    }

                                    //策略名称
                                    String ruleName = checkRuleRO.getRuleObject().getName();
                                    if (!AliStringUtils.isEmpty(ruleName)) {
                                        mergeDTO.setRuleName(ruleName);
                                    }

                                    if (!AliStringUtils.isEmpty(mergeField)) {
                                        mergeDTO.setMergeField(mergeField);
                                    }
                                    recommendPolicyDTO.setMergeDTO(mergeDTO);
                                    getMergeRuleName = true;
                                    break;
                                }
                            }
                            logger.debug("找到可合并策略：" + JSONObject.toJSONString(checkResultDataRO));
                        }

                        if (getMergeRuleName) {
                            break;
                        }
                    }
                }
            } else {
                logger.debug("命令行生成不进行合并策略...");
            }

        }

        //只对路径保存状态
        if(hasRule) {
            taskService.updatePathCheckStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_CHECK_HAS_RULE);
        } else {
            taskService.updatePathCheckStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_CHECK_NO_RULE);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int checkPolicyByPolicyTask(SimulationTaskDTO simulationTaskDTO) {
        //获取当前任务所有路径信息
        List<PathInfoEntity> pathInfoList= taskService.getPathInfoByTaskId(simulationTaskDTO.getId());

        List<RecommendPolicyDTO> list = simulationTaskDTO.getPolicyList();
        Boolean policyMoreThanSettings = false;

        if (list.size() > checkPolicyMax) {
            //如果策略检查中策略建议数量超过设定的值，则跳过策略检查
            policyMoreThanSettings = true;
        }

        if(!taskService.isCheckRule() || simulationTaskDTO.getTaskType() == PolicyConstants.BIG_INTERNET_RECOMMEND || policyMoreThanSettings) {
            logger.info(String.format("任务(%d)[%s]策略检查关闭，跳过策略检查...", simulationTaskDTO.getId(), simulationTaskDTO.getTheme()));
            for (PathInfoEntity task : pathInfoList) {
                taskService.updatePathCheckStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_CHECK_SKIPEED);
            }
            return ReturnCode.POLICY_MSG_OK;
        }  else {
            logger.info(String.format("任务(%d)[%s]路径开始预生效策略检查...", simulationTaskDTO.getId(), simulationTaskDTO.getTheme()));
        }

        boolean hasRule = false;
        for (RecommendPolicyDTO recommendPolicyDTO : list) {
            //先检查策略集UUID，若不存在则不进行后续步骤
            String ruleListUuid = recommendPolicyDTO.getRuleListUuid();
            if (AliStringUtils.isEmpty(ruleListUuid)) {
                logger.warn(String.format("任务(%d)[%s]路径设备%s策略集UUID为空，不进行策略检查", simulationTaskDTO.getId(), simulationTaskDTO.getTheme(), recommendPolicyDTO.getDeviceUuid()));
                continue;
            }

            String deviceUuid = recommendPolicyDTO.getDeviceUuid();
            String modelNumber = taskService.getDeviceModelNumber(deviceUuid);

            List<ServiceDTO> serviceList = recommendPolicyDTO.getServiceList();

            //组成策略检查参数对象
            RuleCheckPolicyRO ruleCheckPolicyVO = new RuleCheckPolicyRO();
            SimpleRuleRO simpleRuleRO = new SimpleRuleRO();

            List<SrcDstStringDTO> srcAddress = whaleService.getSrcDstStringDTO(recommendPolicyDTO.getSrcIp());
            List<SrcDstStringDTO> dstAddress = whaleService.getSrcDstStringDTO(recommendPolicyDTO.getDstIp());


            simpleRuleRO.setIp4SrcAddresses(srcAddress);
            simpleRuleRO.setIp4DstAddresses(dstAddress);
            String srcZone = recommendPolicyDTO.getSrcZone();

            if (StringUtils.isNotBlank(srcZone)) {
                simpleRuleRO.setFromZone(srcZone);
            }
            String dstZone = recommendPolicyDTO.getDstZone();

            if (StringUtils.isNotBlank(dstZone)) {
                simpleRuleRO.setToZone(dstZone);
            }
            simpleRuleRO.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
            List<RuleCheckServiceDTO> services = new ArrayList<>();
            if(serviceList != null) {
                for (ServiceDTO service : serviceList) {
                    String protocolByString = ProtocolUtils.getProtocolByString(service.getProtocol());
                    if (!protocolByString.equalsIgnoreCase(CommonConstants.ANY)) {

                        RuleCheckServiceDTO ruleCheckServiceDTO = new RuleCheckServiceDTO();

                        String dstPorts = service.getDstPorts();

                        if (StringUtils.isNotBlank(dstPorts) && !dstPorts.equalsIgnoreCase(CommonConstants.ANY)) {
                            ruleCheckServiceDTO.setDstPorts(whaleService.getSrcDstIntegerDTOList(dstPorts));
                        }
                        ruleCheckServiceDTO.setProtocolName(protocolByString.toUpperCase());
                        services.add(ruleCheckServiceDTO);
                    }

                }
            }
            simpleRuleRO.setServices(services);
            simpleRuleRO.setIdleTimeout(recommendPolicyDTO.getIdleTimeout());
            //预变更策略检查添加接口/域参数说明
//            1. cisco：思科策略生成时需用到接口，因此预变更策略检查添加接口
//            2. abt：安博通策略生成时需要用到接口，因此预变更策略检查添加接口
//            3. hillstone：生成策略带zone，因此预变更策略检查添加zone
//            4. junipersrx：生成策略带zone，因此预变更策略添加zone
//            5. juniperssg: 生成策略带zone，因此预变更策略添加zone
//            6. topsec：生成策略带zone，因此预变更策略检查添加zone
//            7. usg2100：生成策略带zone，因此预变更策略检查添加zone
//            8. usg6000：生成策略带zone，因此预变更策略检查添加zone

            //2019-05-06 新增：根据高级设置决定策略检查是否带zone

            Set<String> ciscoModelSet = new HashSet<>();
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_S.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_ASA_86.getKey());
            ciscoModelSet.add(DeviceModelNumberEnum.CISCO_ASA_99.getKey());
            if (ciscoModelSet.contains(modelNumber)) {
                //策略检查设置参数时先检查思科设备ACL策略是设置在in接口还是out接口。
                if (recommendPolicyDTO.getAclDirection().equals(AdvancedSettingsConstants.PARAM_INT_CISCO_POLICY_OUT_DIRECTION)) {
                    logger.debug("思科检查ACL策略在OUT接口");
                    simpleRuleRO.setToInterfaceName(recommendPolicyDTO.getOutDevIf());
                } else {
                    logger.debug("思科检查ACL策略在IN接口");
                    simpleRuleRO.setFromInterfaceName(recommendPolicyDTO.getInDevIf());
                }
            }

            Set<String> itfModelSet = new HashSet<>();
            itfModelSet.add(DeviceModelNumberEnum.ABTNETWORKS.getKey());
            itfModelSet.add(DeviceModelNumberEnum.SDNWARE.getKey());

            //itfModelSet.add("abtnetworks");
            //itfModelSet.add("sapling");
            //itfModelSet.add("Sapling");
            if (itfModelSet.contains(modelNumber)) {
                simpleRuleRO.setFromInterfaceName(recommendPolicyDTO.getInDevIf());
                simpleRuleRO.setToInterfaceName(recommendPolicyDTO.getOutDevIf());
            }

            Set<String> zoneModelSet = new HashSet<>();
            zoneModelSet.add(DeviceModelNumberEnum.USG6000.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.USG6000_NO_TOP.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.USG2000.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.SRX.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.SRX_NoCli.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.SSG.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.HILLSTONE.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.HILLSTONE_R5.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.TOPSEC_NG.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.TOPSEC_TOS_005.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.TOPSEC_TOS_010_020.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.DPTECHR003.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.DPTECHR004.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.FORTINET.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.FORTINET_V5_2.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.H3CV7.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.H3CV5.getKey());
            zoneModelSet.add(DeviceModelNumberEnum.VENUSTECHVSOS.getKey());
            if (zoneModelSet.contains(modelNumber)) {
                //根据高级设置决定策略检查是否需要设置源域和目的域。
                if (recommendPolicyDTO.getSpecifyZone().equals(AdvancedSettingsConstants.PARAM_INT_SET_NO_ZONE)) {
                    logger.debug(String.format("设备(%s)不指定域信息...策略检查参数不指定域", deviceUuid));
                } else if (recommendPolicyDTO.getSpecifyZone().equals(AdvancedSettingsConstants.PARAM_INT_SET_SRC_ZONE)) {
                    logger.debug(String.format("设备(%s)指定源域信息...策略检查参数指定源域", deviceUuid));
                    simpleRuleRO.setFromZone(recommendPolicyDTO.getSrcZone());
                } else if (recommendPolicyDTO.getSpecifyZone().equals(AdvancedSettingsConstants.PARAM_INT_SET_DST_ZONE)) {
                    logger.debug(String.format("设备(%s)指定目的域信息...策略检查参数指定目的域", deviceUuid));
                    simpleRuleRO.setToZone(recommendPolicyDTO.getDstZone());
                } else {
                    logger.debug(String.format("设备(%s)使用默认方式设置域...策略检查参数指定源域和目的域", deviceUuid));
                    simpleRuleRO.setFromZone(recommendPolicyDTO.getSrcZone());
                    simpleRuleRO.setToZone(recommendPolicyDTO.getDstZone());
                }
            }

//                FIRST(1, "最前面"),
//                        LAST(2,"最后面"),
//                        BEFORE(3, "在之前",  "before"),
//                        AFTER(4, "在之后", "after"),
            int movePolicy = recommendPolicyDTO.getMovePolicy();
            if (movePolicy == AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_FIRST) {
                ruleCheckPolicyVO.setLocationType("TOP");
            } else if (movePolicy == AdvancedSettingsConstants.PARAM_INT_NOT_MOVE_POLICY) {
                ruleCheckPolicyVO.setLocationType("BOTTOM");
            } else if (movePolicy == AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_BEFORE) {
                String policy = recommendPolicyDTO.getSpecificPosition();
                if (AliStringUtils.isNumeric(policy)) {
                    //如果是思科设备的话 思科移动是按照行号进行检查
                    if (DeviceModelNumberEnum.CISCO.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_S.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_86.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_99.getKey().contains(modelNumber)) {
                        ruleCheckPolicyVO.setLocationType("BEFORE_RULE_INDEX");
                        ruleCheckPolicyVO.setRuleIndex(Integer.parseInt(policy));
                    } else {
                        ruleCheckPolicyVO.setLocationType("BEFORE_RULE_ID");
                        ruleCheckPolicyVO.setRuleId(policy);
                    }
                } else {
                    ruleCheckPolicyVO.setLocationType("BEFORE_RULE_NAME");
                    ruleCheckPolicyVO.setRuleName(policy);
                }
            } else if (movePolicy == AdvancedSettingsConstants.PARAM_INT_MOVE_POLICY_AFTER) {
                String policy = recommendPolicyDTO.getSpecificPosition();
                if (AliStringUtils.isNumeric(policy)) {
                    if (DeviceModelNumberEnum.CISCO.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_S.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_86.getKey().contains(modelNumber) || DeviceModelNumberEnum.CISCO_ASA_99.getKey().contains(modelNumber)) {
                        ruleCheckPolicyVO.setLocationType("AFTER_RULE_INDEX");
                        ruleCheckPolicyVO.setRuleIndex(Integer.parseInt(policy));
                    } else {
                        ruleCheckPolicyVO.setLocationType("AFTER_RULE_ID");
                        ruleCheckPolicyVO.setRuleId(policy);
                    }
                } else {
                    ruleCheckPolicyVO.setLocationType("AFTER_RULE_NAME");
                    ruleCheckPolicyVO.setRuleName(policy);
                }
            }
            ruleCheckPolicyVO.setRuleListUuid(ruleListUuid);
            ruleCheckPolicyVO.setSimpleRule(simpleRuleRO);

            //调用接口进行策略检察
            logger.info("策略检查参数为:\n" + JSONObject.toJSONString(ruleCheckPolicyVO));
            RuleCheckResultRO ruleCheckResultRO = whaleService.getRuleCheckResult(ruleCheckPolicyVO, deviceUuid, ruleListUuid);
            logger.info("策略检查结果为:\n" + JSONObject.toJSONString(ruleCheckResultRO));
            List<RuleCheckResultDataRO> ruleCheckResultDataVOList = ruleCheckResultRO.getData();
            if (ruleCheckResultDataVOList == null || ruleCheckResultDataVOList.size() == 0) {
                logger.debug("策略检查结果：没有相关策略...");
                continue;
            }

            hasRule = true;
            //保存检查到的相关策略
            int policyId = recommendPolicyDTO.getId();
            CheckResultEntity entity = new CheckResultEntity();
            entity.setPolicyId(policyId);
            String jsonString = JSONObject.toJSONString(ruleCheckResultRO);
            logger.debug("策略检查结果：有相关策略，相关策略为：\n" + jsonString);
            entity.setCheckResult(jsonString);
            taskService.addCheckResult(entity);

            //需要合并策略才进行合并策略检查
            if (recommendPolicyDTO.getCreatePolicy().equals(AdvancedSettingsConstants.PARAM_INT_MERGE_RULE)) {
                logger.debug("检查相关策略中的可合并策略");
                //保存可合并策略信息
                if (recommendPolicyDTO.getCreatePolicy() == 1) {
                    boolean getMergeRuleName = false;
                    for (RuleCheckResultDataRO checkResultDataRO : ruleCheckResultDataVOList) {

                        if (checkResultDataRO.getBpcCode().equalsIgnoreCase("RC_MERGE_RULE")) {
                            //检测相关策略，若为目的合并，则不加入到合并列表
                            List<CheckRuleRO> checkList = checkResultDataRO.getRelatedRules();
                            if (checkList == null || checkList.size() == 0) {
                                logger.debug("相关规则为空！");
                                continue;
                            }
                            for (CheckRuleRO checkRuleRO : checkList) {
                                String mergeField = checkRuleRO.getRuleMergeField();
                                if (!AliStringUtils.isEmpty(mergeField)) {
                                    logger.debug("合并类型：" + mergeField);
                                    if (!mergeField.equalsIgnoreCase("DST")) {
                                        logger.debug("目的地址不合并, 跳过该可合并策略...");
                                        continue;
                                    }
                                    PolicyMergeDTO mergeDTO = new PolicyMergeDTO();
                                    //策略ID
                                    String ruleId = checkRuleRO.getRuleObject().getRuleId();
                                    if (!AliStringUtils.isEmpty(ruleId)) {
                                        mergeDTO.setRuleId(ruleId);
                                    }

                                    //策略名称
                                    String ruleName = checkRuleRO.getRuleObject().getName();
                                    if (!AliStringUtils.isEmpty(ruleName)) {
                                        mergeDTO.setRuleName(ruleName);
                                    }

                                    if (!AliStringUtils.isEmpty(mergeField)) {
                                        mergeDTO.setMergeField(mergeField);
                                    }
                                    recommendPolicyDTO.setMergeDTO(mergeDTO);
                                    getMergeRuleName = true;
                                    break;
                                }
                            }
                            logger.debug("找到可合并策略：" + JSONObject.toJSONString(checkResultDataRO));
                        }

                        if (getMergeRuleName) {
                            break;
                        }
                    }
                }
            } else {
                logger.debug("命令行生成不进行合并策略...");
            }

        }

        //只对路径保存状态,因为现在是合并后的策略进行检查，无法保证策略和路径的对应关系正确性，故有一个检查有规则，全为有规则
        for (PathInfoEntity task : pathInfoList) {
            if(hasRule) {
                taskService.updatePathCheckStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_CHECK_HAS_RULE);
            } else {
                taskService.updatePathCheckStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_CHECK_NO_RULE);
            }
        }
        return ReturnCode.POLICY_MSG_OK;
    }


    public static void main(String[] args) {
        List<SrcDstIntegerDTO> portList = WhaleDoUtils.getSrcDstIntegerDTOList("0-5,7-255");
        System.out.println(JSONObject.toJSONString(portList));
    }
}
