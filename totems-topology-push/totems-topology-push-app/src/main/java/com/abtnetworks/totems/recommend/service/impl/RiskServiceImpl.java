package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.recommend.dto.risk.PolicyRecommendRiskDTO;
import com.abtnetworks.totems.recommend.dto.risk.PolicyRecommendRiskResultDTO;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.entity.RiskRuleDetailEntity;
import com.abtnetworks.totems.recommend.entity.RiskRuleInfoEntity;
import com.abtnetworks.totems.recommend.manager.ExternalManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.manager.impl.WhaleManagerImpl;
import com.abtnetworks.totems.recommend.service.RiskService;
import com.abtnetworks.totems.whale.policy.dto.SrcDstIntegerDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policy.ro.AccessQueryDataRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeDataRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import com.abtnetworks.totems.whale.policy.ro.PathInfoRO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RiskServiceImpl implements RiskService {

    private static Logger logger = Logger.getLogger(RiskServiceImpl.class);

    @Autowired
    RecommendTaskManager policyRecommendTaskService;

    @Autowired
    WhaleManager whaleService;

    @Autowired
    ExternalManager externalService;

    private static final String SEPERATOR  = ",";

    private static final int RISK_PORT_RISK = 6;

    private static final String SECURITY_POLICY = "2";

    /**
     * 检查策略风险
     * @param task
     * @return
     */
    @Override
    public int checkPolicyRecommendRiskByPathInfo(PathInfoTaskDTO task){
        if (!policyRecommendTaskService.isCheckRisk()) {
            logger.info(String.format("任务(%d)[%s]路径%d跳过风险分析...", task.getTaskId(), task.getTheme(), task.getId()));
            policyRecommendTaskService.updatePathRiskStatus(task.getId(), PolicyConstants.POLICY_INT_RECOMMEND_RISK_SKIPPED);
            return ReturnCode.POLICY_MSG_OK;
        }
        logger.info(String.format("任务(%d)[%s]路径%d开始风险分析...", task.getId(), task.getTheme(), task.getId()));

        int taskId = task.getId();
        try {


            String srcIp = task.getSrcIp();
            String dstIp = task.getDstIp();
            PathAnalyzeRO pathAnalyzeRO = task.getPathAnalyzeRO();
            Map<String,Set<String>> dstNodeUuidMap = new HashMap<>();
            if(pathAnalyzeRO!= null && CollectionUtils.isNotEmpty(pathAnalyzeRO.getData())){
                List<PathAnalyzeDataRO> data = pathAnalyzeRO.getData();
                data.forEach(d->{
                    List<PathInfoRO> pathList = d.getPathList();
                    if (CollectionUtils.isNotEmpty(pathList)) {
                        pathList.forEach(p->{
                            String dstNodeId = p.getDstNodeId();
                            if(StringUtils.isNotBlank(dstNodeId)){
                                String dstZoneUuid = whaleService.getZoneUuid(dstNodeId);
                                Set<String> dstZoneList = dstNodeUuidMap.get(dstZoneUuid);
                                if(CollectionUtils.isNotEmpty(dstZoneList)){
                                    dstZoneList.add(dstNodeId);
                                    dstNodeUuidMap.put(dstZoneUuid,dstZoneList);
                                }else{
                                    Set<String> dstZoneNewList = new HashSet<>();
                                    dstZoneNewList.add(dstNodeId);
                                    dstNodeUuidMap.put(dstZoneUuid,dstZoneNewList);
                                }

                            }
                        });
                    }

                });
            }
            String srcNodeUuid = task.getSrcNodeUuid();

            String srcZoneUuid = whaleService.getZoneUuid(srcNodeUuid);


            if (AliStringUtils.isEmpty(srcZoneUuid) || MapUtils.isEmpty(dstNodeUuidMap)) {

                List<String> riskList = new ArrayList<>();

                if (task.getServiceList() != null) {
                    for (ServiceDTO service : task.getServiceList()) {
                        List<String> portRiskList = checkPort(service.getDstPorts(), service.getProtocol());
                        riskList.addAll(portRiskList);
                    }
                }

                boolean hasRisk = false;
                for (String riskId : riskList) {
                    logger.debug(String.format("任务[%d]有风险(%s)", taskId, riskId));
                    policyRecommendTaskService.addTaskRisk(taskId, riskId);
                    hasRisk = true;
                }
                if (hasRisk) {
                    policyRecommendTaskService.updatePathRiskStatus(taskId, PolicyConstants.POLICY_INT_RECOMMEND_RISK_HAS_RISK);
                } else {
                    policyRecommendTaskService.updatePathRiskStatus(taskId, PolicyConstants.POLICY_INT_RECOMMEND_RISK_NO_ZONE_UUID);
                }

                return ReturnCode.POLICY_MSG_OK;
            }

            boolean hasRisk = false;
            List<String> riskList = new ArrayList<>();

            if (dstNodeUuidMap.size() == 0) {
                Set<String> dstNodeUuidS = new HashSet<>();
                dstNodeUuidS.add("");
                dstNodeUuidMap.put("",dstNodeUuidS);
            }
            String startSrcIp = IpUtils.getStartIpFromIpAddress(srcIp);
            String endSrcIp = IpUtils.getEndIpFromIpAddress(srcIp);

            String startDstIp = IpUtils.getStartIpFromIpAddress(dstIp);
            String endDstIp = IpUtils.getEndIpFromIpAddress(dstIp);
            dstNodeUuidMap.forEach((k,v)->{
                List<AccessQueryDataRO> list = new ArrayList<>();
                PolicyRecommendRiskDTO policyRecommendRiskDTO = new PolicyRecommendRiskDTO();
                policyRecommendRiskDTO.setTaskId(taskId);
                policyRecommendRiskDTO.setSrcZoneUuid(srcZoneUuid);
                policyRecommendRiskDTO.setDstZoneUuid(k);
                if(CollectionUtils.isNotEmpty(v)){
                    for (String dstNodeUuid:v) {
                        AccessQueryDataRO dataRO = new AccessQueryDataRO();
                        dataRO.setSrcNodeUuid(srcNodeUuid);
                        dataRO.setDstNodeUuid(dstNodeUuid);
                        List<SrcDstStringDTO> srcIpList = new ArrayList<>();
                        srcIpList.add(WhaleDoUtils.getSrcDstStringDTO(startSrcIp, endSrcIp));
                        dataRO.setIp4SrcAddresses(srcIpList);

                        List<SrcDstStringDTO> dstIpList = new ArrayList<>();
                        dstIpList.add(WhaleDoUtils.getSrcDstStringDTO(startDstIp, endDstIp));
                        dataRO.setIp4DstAddresses(dstIpList);

                        if (task.getServiceList() != null) {
                            for (ServiceDTO service : task.getServiceList()) {

                                dataRO.setSrcPorts(WhaleDoUtils.getSrcDstIntegerDTOList(service.getSrcPorts()));
                                dataRO.setDstPorts(WhaleDoUtils.getSrcDstIntegerDTOList(service.getDstPorts()));

                                String protocolStr = service.getProtocol();
                                int protocol = InputValueUtils.getProtocol(protocolStr);

                                List<SrcDstIntegerDTO> protocolList = new ArrayList<>();
                                SrcDstIntegerDTO srcDstIntegerDTO = new SrcDstIntegerDTO();
                                //协议可以为0，因此any为-1
                                if (protocol == -1) {
                                    srcDstIntegerDTO.setStart(1);
                                    srcDstIntegerDTO.setEnd(65535);
                                } else {
                                    srcDstIntegerDTO.setStart(protocol);
                                    srcDstIntegerDTO.setEnd(protocol);
                                }
                                protocolList.add(srcDstIntegerDTO);
                                dataRO.setProtocols(protocolList);
                            }
                        }

                        list.add(dataRO);
                    }

                }

                policyRecommendRiskDTO.setData(list);
                logger.info("查询分险分析的入参{}" + JSONObject.toJSONString(policyRecommendRiskDTO));
                PolicyRecommendRiskResultDTO result = externalService.checkRisk(policyRecommendRiskDTO);
                logger.info("查询分险分析的返回参{}" + JSONObject.toJSONString(result));
                List<String> risk = result.getRiskList();
                if (risk == null) {
                    logger.debug("风险分析已完成！没有风险");
                } else {
                    riskList.addAll(risk);
                }

            });
            
            if (task.getServiceList() != null) {
                for (ServiceDTO service : task.getServiceList()) {
                    List<String> portRiskList = checkPort(service.getDstPorts(), service.getProtocol());
                    riskList.addAll(portRiskList);
                }
            }

            for (String riskId : riskList) {
                logger.debug(String.format("任务[%d]有风险(%s)", taskId, riskId));
                policyRecommendTaskService.addTaskRisk(taskId, riskId);
                hasRisk = true;
            }

            if (hasRisk) {
                policyRecommendTaskService.updatePathRiskStatus(taskId, PolicyConstants.POLICY_INT_RECOMMEND_RISK_HAS_RISK);
            } else {
                policyRecommendTaskService.updatePathRiskStatus(taskId, PolicyConstants.POLICY_INT_RECOMMEND_RISK_NO_RISK);
            }
        } catch ( Exception e ) {
            logger.error(String.format("任务(%d)[%s]路径%d风险分析异常！", task.getTaskId(), task.getTheme(), task.getId()), e);
            policyRecommendTaskService.updatePathRiskStatus(taskId, PolicyConstants.POLICY_INT_RECOMMEND_RISK_FAILED);
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    private List<String> checkPort(String dstPort, String protocol) {
        List<String> riskRuleIdList = new ArrayList<>();

        Set<String> portSet = new HashSet<>();

        if(!AliStringUtils.isEmpty(dstPort)) {
            String[] dstPorts = dstPort.split(SEPERATOR);
            for (String port : dstPorts) {
                portSet.add(port);
            }
        } else {
            portSet.add("0-65535");
        }

        if(portSet.size() == 0) {
            return riskRuleIdList;
        }

        List<RiskRuleInfoEntity> riskInfolist = policyRecommendTaskService.getRiskInfoBySecondSortId(RISK_PORT_RISK);
        for(RiskRuleInfoEntity riskRuleInfoEntity: riskInfolist) {
            logger.debug(String.format("检查风险：%s", riskRuleInfoEntity.getRuleId()));
            RiskRuleDetailEntity ruleDetailEntity = policyRecommendTaskService.getRiskDetailEntityByRuleId(riskRuleInfoEntity.getRuleId());
            if(ruleDetailEntity == null) {
                logger.warn(String.format("风险%s详细数据为空！", riskRuleInfoEntity.getRuleId()));
                continue;
            }
            if(!ruleDetailEntity.getPolicyType().equals(SECURITY_POLICY)) {
                logger.debug(String.format("策略(%s)不是安全策略，跳过检测", riskRuleInfoEntity.getRuleId()));
                continue;
            }
            String protocolString = ProtocolUtils.getProtocolByString(protocol);
            if(!protocolString.equalsIgnoreCase(ruleDetailEntity.getProtocol())) {
                logger.debug(String.format("策略协议不一致，跳过检测[%s:%s]", protocolString, ruleDetailEntity.getProtocol()));
                continue;
            }

            String[] riskPorts = ruleDetailEntity.getPort().split(SEPERATOR);
            boolean hasRisk = false;
            for(String riskPort: riskPorts) {
                for(String port: portSet) {
                    if(checkPortContained(port, riskPort)) {
                        hasRisk = true;
                        break;
                    }
                }
                if(hasRisk) {
                    break;
                }
            }

            if(hasRisk) {
                logger.debug("策略有风险...." + riskRuleInfoEntity.getRuleId());
                riskRuleIdList.add(ruleDetailEntity.getRuleId());
            }
        }

        return riskRuleIdList;
    }

    //检查端口与风险端口是否有包含关系
    boolean checkPortContained(String port, String riskPort) {
        WhaleManagerImpl impl = new WhaleManagerImpl();
        SrcDstIntegerDTO portDto = impl.getSrcDstIntegerDTO(port);
        SrcDstIntegerDTO riskPortDto = impl.getSrcDstIntegerDTO(riskPort);

        if(portDto.getStart() > riskPortDto.getEnd()) {
            return false;
        }

        if(portDto.getEnd() < riskPortDto.getStart()) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        String port1 = "24";
        String port2 = "25";
        RiskServiceImpl impl = new RiskServiceImpl();
        System.out.println(impl.checkPortContained(port1,port2));
    }
}
