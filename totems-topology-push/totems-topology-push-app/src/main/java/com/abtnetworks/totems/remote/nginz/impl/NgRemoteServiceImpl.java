package com.abtnetworks.totems.remote.nginz.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.external.vo.NatRuleMatchFlowVO;
import com.abtnetworks.totems.external.vo.RuleMatchFlowVO;

import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.push.service.PushTaskBusinessArithmeticService;
import com.abtnetworks.totems.recommend.dto.task.PackFilterDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.remote.nginz.NgRemoteService;
import com.abtnetworks.totems.whale.baseapi.dto.RuleMatchFlowDTO;
import com.abtnetworks.totems.whale.policy.dto.FilterDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstIntegerDTO;
import com.abtnetworks.totems.whale.policy.dto.SrcDstStringDTO;
import com.abtnetworks.totems.whale.policy.ro.PathFlowRO;
import com.abtnetworks.totems.whale.policy.service.WhalePathAnalyzeClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV6;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.ADD_POLICY;
import static com.abtnetworks.totems.common.enums.PolicyMergePropertyEnum.UN_OPEN_GENERATE;

/**
 * @author Administrator
 * @Title:
 * @Description: ???????????????
 * @date 2021/3/29
 */
@Slf4j
@Service
public class NgRemoteServiceImpl implements NgRemoteService {
    @Resource
    WhalePathAnalyzeClient whalePathAnalyzeClient;

    @Resource
    WhaleManager whaleManager;



    @Resource
    PushTaskBusinessArithmeticService pushTaskBusinessArithmeticService;

    @Override
    public List<CmdDTO> getRuleMatchFlow(CmdDTO cmdDTO) {
        List<CmdDTO> cmdDTOList = new ArrayList<>();
        TaskDTO task = cmdDTO.getTask();
        Boolean rangeFilter = task.getRangeFilter();

        if (rangeFilter!=null && rangeFilter) {
            try {
                RuleMatchFlowDTO ruleMatchFlowDTO = new RuleMatchFlowDTO();
                DeviceDTO device = cmdDTO.getDevice();
                String deviceUuid = device.getDeviceUuid();
                ruleMatchFlowDTO.setDeviceUuid(deviceUuid);
                PolicyDTO policy = cmdDTO.getPolicy();
                String srcZone = policy.getSrcZone();
                String dstZone = policy.getDstZone();
                NodeEntity nodeEntity = device.getNodeEntity();
                if(ObjectUtils.isNotEmpty(nodeEntity)) {


                    DeviceModelNumberEnum modelNumber = DeviceModelNumberEnum.fromString(nodeEntity.getModelNumber());
                    boolean isCs = DeviceModelNumberEnum.isRangeCiscoCode(modelNumber.getCode());
                    if (StringUtils.isNotBlank(dstZone) && !isCs) {
                        ruleMatchFlowDTO.setDstZoneName(dstZone);
                    }
                    if (StringUtils.isNotBlank(srcZone) && !isCs) {
                        ruleMatchFlowDTO.setSrcZoneName(srcZone);
                    }
                    String srcItf = policy.getSrcItf();
                    String dstItf = policy.getDstItf();
                    if (StringUtils.isNotBlank(srcItf)) {
                        ruleMatchFlowDTO.setInInterfaceName(srcItf);
                    }
                    if (StringUtils.isNotBlank(dstItf)) {
                        ruleMatchFlowDTO.setOutInterfaceName(dstItf);
                    }
                    Integer idleTimeout = policy.getIdleTimeout();
                    if (ObjectUtils.isNotEmpty(idleTimeout)) {
                        ruleMatchFlowDTO.setIdleTimeout(idleTimeout);
                    }
                    Integer ipType = policy.getIpType();
                    String dstIp = policy.getDstIp();
                    String srcIp = policy.getSrcIp();

                    List<ServiceDTO> serviceList = policy.getServiceList();
                    PackFilterDTO packFilterDTO = new PackFilterDTO();

                    if (StringUtils.isNotBlank(srcIp)) {
                        List<String> srcIpList = Arrays.asList(srcIp.split(PolicyConstants.ADDRESS_SEPERATOR));
                        packFilterDTO.setSrcIpList(srcIpList);
                    }
                    if (StringUtils.isNotBlank(dstIp)) {
                        List<String> dstIpList = Arrays.asList(dstIp.split(PolicyConstants.ADDRESS_SEPERATOR));
                        packFilterDTO.setDstIpList(dstIpList);
                    }
                    if (CollectionUtils.isNotEmpty(serviceList)) {
                        packFilterDTO.setServiceList(serviceList);
                    }
                    packFilterDTO.setIpType(ipType);
                    List<FilterDTO> beginFlow = whaleManager.packFilterDTO(packFilterDTO);
                    ruleMatchFlowDTO.setBeginFlow(beginFlow);
                    log.info("???????????????????????????{}", JSONObject.toJSONString(ruleMatchFlowDTO));
                    RuleMatchFlowVO ruleMatchFlow = whalePathAnalyzeClient.getRuleMatchFlow(ruleMatchFlowDTO);
                    boolean denyGen = getDenyGen(ruleMatchFlow, cmdDTOList, cmdDTO);
                    if (!denyGen) {
                        cmdDTO.getPolicy().setMergeProperty(UN_OPEN_GENERATE.getCode());
                        cmdDTOList.add(cmdDTO);
                    }
                    log.info("??????deny?????????? {}", denyGen);
                }else{
                    log.error("??????????????????????????????{}",JSONObject.toJSONString(cmdDTO));
                }
            } catch (Exception e) {
                log.info("?????????????????????????????????????????????", e);
                cmdDTO.getPolicy().setMergeProperty(ADD_POLICY.getCode());
                cmdDTOList.add(cmdDTO);
            }
        } else {
            log.info("???????????????????????????????????????");
            cmdDTO.getPolicy().setMergeProperty(ADD_POLICY.getCode());
            cmdDTOList.add(cmdDTO);
        }
        return cmdDTOList;
    }


    /**
     * ????????????deny??????
     * @param ruleMatchFlow
     * @param cmdDTOList
     * @param cmdDTO
     * @return
     */
    private boolean getDenyGen(RuleMatchFlowVO ruleMatchFlow, List<CmdDTO> cmdDTOList, CmdDTO cmdDTO) throws InvocationTargetException, IllegalAccessException {
        if (ObjectUtils.isNotEmpty(ruleMatchFlow) && CollectionUtils.isNotEmpty(ruleMatchFlow.getDeny())) {
            List<PathFlowRO> ruleMatchFlowDeny = ruleMatchFlow.getDeny();

            log.info("???????????????????????????{}?????????{}?????????", JSONObject.toJSONString(ruleMatchFlowDeny),ruleMatchFlowDeny.size());
            List<PolicyInfoDTO> policyInfoList = new ArrayList<>();
            ruleMatchFlowDeny.forEach(po -> {
                List<ServiceDTO> serviceDTOS = new ArrayList<>();

                PolicyDTO policy = cmdDTO.getPolicy();
                Integer ipType = policy.getIpType();
                String dstIp, srcIp = "";
                if (IPV4.getCode().equals(ipType)) {
                    List<SrcDstStringDTO> ip4DstAddresses = po.getIp4DstAddresses();
                    dstIp = getSrcDstIpByList(ip4DstAddresses);
                    List<SrcDstStringDTO> ip4SrcAddresses = po.getIp4SrcAddresses();
                    srcIp = getSrcDstIpByList(ip4SrcAddresses);
                } else if (IPV6.getCode().equals(ipType)) {
                    List<SrcDstStringDTO> ip6SrcAddresses = po.getIp6SrcAddresses();
                    List<SrcDstStringDTO> ip6DstAddresses = po.getIp6DstAddresses();
                    dstIp = getSrcDstIpByList(ip6DstAddresses);
                    srcIp = getSrcDstIpByList(ip6SrcAddresses);
                } else {
                    //????????????
                    throw new IllegalArgumentException("????????????ip??????");
                }

                List<SrcDstIntegerDTO> protocols = po.getProtocols();

                if (CollectionUtils.isNotEmpty(protocols)) {
                    for (SrcDstIntegerDTO srcDstIntegerDTO : protocols) {
                        String protocol = String.valueOf(srcDstIntegerDTO.getStart());
                        ServiceDTO serviceDTO = new ServiceDTO();
                        if("0".equals(protocol) ){
                            if(CollectionUtils.isNotEmpty(serviceDTOS)){
                                serviceDTOS.clear();
                            }
                            serviceDTO.setProtocol(protocol);
                            serviceDTOS.add(serviceDTO);
                            break;
                        }
                        boolean  isBeyond = ProtocolUtils.isBeyondProtocol(protocol);
                        if(isBeyond ){
                            if(CollectionUtils.isNotEmpty(serviceDTOS)){
                                serviceDTOS.clear();
                            }
                            serviceDTO.setProtocol("0");
                            serviceDTOS.add(serviceDTO);
                            break;

                        }else{
                            serviceDTO.setProtocol(protocol);
                            List<SrcDstIntegerDTO> dstPorts = po.getDstPorts();

                            if (CollectionUtils.isNotEmpty(dstPorts)) {
                                StringBuffer dstPort = new StringBuffer();

                                dstPorts.parallelStream().forEach(d -> {
                                    // ???????????????????????????????????????start:2001 end:2001
                                    String start = String.valueOf(d.getStart());
                                    String end = String.valueOf(d.getEnd());
                                    if(start.equalsIgnoreCase(end)){
                                        dstPort.append(start);
                                    }else {
                                        dstPort.append(start).append("-").append(end);
                                    }
                                    dstPort.append(",");
                                });
                                if (dstPort.lastIndexOf(",") > 0) {
                                    dstPort.deleteCharAt(dstPort.lastIndexOf(","));
                                }
                                serviceDTO.setDstPorts(dstPort.toString());
                            }
                            serviceDTOS.add(serviceDTO);
                        }
                    }
                }
                PolicyInfoDTO policyInfo = new PolicyInfoDTO();
                policyInfo.setDstIp(dstIp);
                policyInfo.setSrcIp(srcIp);
                policyInfo.setIpType(ipType);
                policyInfo.setServiceList(serviceDTOS);
                policyInfoList.add(policyInfo);
            });
            log.info("?????????????????????{}", JSONObject.toJSONString(policyInfoList));
            List<PolicyInfoDTO> policyInfos = pushTaskBusinessArithmeticService.sameTaskMergeQuintuple(policyInfoList);
            log.info("?????????????????????{}", JSONObject.toJSONString(policyInfos));


            for (int i =0 ; i< policyInfos.size() ; i++) {
                //spring BeanUtils ????????????????????????????????????
                CmdDTO cmdDTO1 = new CmdDTO();
                TaskDTO task = new TaskDTO();
                BeanUtils.copyProperties(cmdDTO.getTask(), task);
                if(i>0){
                    String theme = task.getTheme()+"_"+i;
                    task.setTheme(theme);
                }
                cmdDTO1.setTask(task);
                SettingDTO setting = new SettingDTO();
                BeanUtils.copyProperties(cmdDTO.getSetting(), setting);
                cmdDTO1.setSetting(setting);
                PolicyDTO policy = new PolicyDTO();
                BeanUtils.copyProperties(cmdDTO.getPolicy(), policy);
                PolicyInfoDTO policyInfo = policyInfos.get(i);
                policy.setDstIp(policyInfo.getDstIp());
                policy.setSrcIp(policyInfo.getSrcIp());
                policy.setServiceList(policyInfo.getServiceList());
                policy.setMergeProperty(ADD_POLICY.getCode());
                DeviceDTO device = new DeviceDTO();
                BeanUtils.copyProperties(cmdDTO.getDevice(), device);
                device.setIsDisasterDevice(false);
                cmdDTO1.setDevice(device);
                cmdDTO1.setPolicy(policy);
                cmdDTOList.add(cmdDTO1);
            }

            return true;
        } else {
            log.info("??????????????????????????????");
            return false;
        }
    }

    /**
     * ??????????????????
     * @param srcDstStringDTOS
     * @return
     */
    public String getSrcDstIpByList(List<SrcDstStringDTO> srcDstStringDTOS) {

        if (CollectionUtils.isNotEmpty(srcDstStringDTOS)) {
            StringBuffer stringBuffer = new StringBuffer();
            srcDstStringDTOS.forEach(s -> {
                String start = s.getStart();
                String end = s.getEnd();
                if (start.equals(end)) {
                    stringBuffer.append(start).append(",");
                } else if(start.equalsIgnoreCase("0.0.0.0")&& end.equalsIgnoreCase("255.255.255.255")){
                    log.info("????????????????????????start{},end{}",start,end);
                }else  {
                    stringBuffer.append(start).append("-").append(end).append(",");
                }
            });
            if (stringBuffer.lastIndexOf(",") > 0) {
                stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(","));
            }
            return stringBuffer.toString();
        } else {
            return "";
        }
    }

    @Override
    public RuleMatchFlowVO getRuleMatchFlow(PolicyDTO policy, DeviceDTO device) {
        try {
            RuleMatchFlowDTO ruleMatchFlowDTO = this.getRuleMatchFlowDTO(policy, device, null);
            log.info("???????????????????????????{}",JSONObject.toJSONString(ruleMatchFlowDTO));
            RuleMatchFlowVO ruleMatchFlow = whalePathAnalyzeClient.getRuleMatchFlow(ruleMatchFlowDTO);
            return ruleMatchFlow;
        } catch (Exception e) {
            log.info("?????????????????????????????????????????????", e);
        }
        return null;
    }

    @Override
    public List<NatRuleMatchFlowVO> getNatRuleMatchFlow(PolicyDTO policy, DeviceDTO device, Integer natType) {

        try {
            RuleMatchFlowDTO ruleMatchFlowDTO = this.getRuleMatchFlowDTO(policy, device, natType);
            log.info("???????????????????????????{}",JSONObject.toJSONString(ruleMatchFlowDTO));
            ResultRO<List<NatRuleMatchFlowVO>> natListResultRO = whalePathAnalyzeClient.getNatRuleMatchFlow(ruleMatchFlowDTO);
            if(natListResultRO != null && CollectionUtils.isNotEmpty(natListResultRO.getData())){
                return natListResultRO.getData();
            }
        } catch (Exception e) {
            log.info("?????????????????????????????????????????????", e);
        }
        return null;

    }

    /**
     * ??????-????????????
     * @param policy
     * @param device
     * @param natType
     * @return
     */
    private RuleMatchFlowDTO getRuleMatchFlowDTO(PolicyDTO policy, DeviceDTO device, Integer natType){
        RuleMatchFlowDTO ruleMatchFlowDTO = new RuleMatchFlowDTO();
        String deviceUuid = device.getDeviceUuid();
        ruleMatchFlowDTO.setDeviceUuid(deviceUuid);

        String srcZone = policy.getSrcZone();
        String dstZone = policy.getDstZone();
        if (StringUtils.isNotBlank(dstZone)) {
            ruleMatchFlowDTO.setDstZoneName(dstZone);
        }
        if (StringUtils.isNotBlank(srcZone)) {
            ruleMatchFlowDTO.setSrcZoneName(srcZone);
        }
        String dstItf = policy.getDstItf();
        String srcItf = policy.getSrcItf();
        if (StringUtils.isNotBlank(srcItf)) {
            ruleMatchFlowDTO.setInInterfaceName(srcItf);
        }
        if (StringUtils.isNotBlank(dstItf)) {
            ruleMatchFlowDTO.setOutInterfaceName(dstItf);
        }
        Integer idleTimeout = policy.getIdleTimeout();
        if (ObjectUtils.isNotEmpty(idleTimeout)) {
            ruleMatchFlowDTO.setIdleTimeout(idleTimeout);
        }
        Integer ipType = policy.getIpType();
        String srcIp = policy.getSrcIp();
        String dstIp = policy.getDstIp();
        List<ServiceDTO> serviceList = policy.getServiceList();
        PackFilterDTO packFilterDTO = new PackFilterDTO();

        if (StringUtils.isNotBlank(srcIp)) {
            List<String> srcIpList = Arrays.asList(srcIp.split(PolicyConstants.ADDRESS_SEPERATOR));
            packFilterDTO.setSrcIpList(srcIpList);
        }
        if (StringUtils.isNotBlank(dstIp)) {
            List<String> dstIpList = Arrays.asList(dstIp.split(PolicyConstants.ADDRESS_SEPERATOR));
            packFilterDTO.setDstIpList(dstIpList);
        }
        if (CollectionUtils.isNotEmpty(serviceList)) {
            packFilterDTO.setServiceList(serviceList);
        }
        packFilterDTO.setIpType(ipType);
        List<FilterDTO> beginFlow = whaleManager.packFilterDTO(packFilterDTO);
        ruleMatchFlowDTO.setBeginFlow(beginFlow);
        if(natType != null ){
            ruleMatchFlowDTO.setNatType(natType);
        }
        return ruleMatchFlowDTO;
    }

}
