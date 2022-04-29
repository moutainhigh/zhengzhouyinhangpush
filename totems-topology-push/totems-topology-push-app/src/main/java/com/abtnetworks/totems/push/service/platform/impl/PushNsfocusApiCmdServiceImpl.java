package com.abtnetworks.totems.push.service.platform.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratePlatformApiCmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.credential.dao.mysql.CredentialMapper;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateAddressDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreatePolicyDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateServiceDTO;
import com.abtnetworks.totems.push.entity.PushApiResponseEntity;
import com.abtnetworks.totems.push.manager.NsfocusExternalManager;
import com.abtnetworks.totems.push.service.platform.PushNsfocusApiCmdService;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lifei
 * @desc 绿盟API 下发接口实现
 * @date 2021-03-12 16:01
 */
@Service
@Log4j2
public class PushNsfocusApiCmdServiceImpl implements PushNsfocusApiCmdService {

    @Autowired
    private NsfocusExternalManager nsfocusExternalManager;

    @Autowired
    CredentialMapper credentialMapper;

    @Override
    public PushResultDTO PushNsfocusApiCmd(PushCmdDTO pushCmdDTO, NodeEntity nodeEntity) {
        log.info("-------开始执行{}绿盟api工单入参:{}-------", pushCmdDTO.getRevert() ? "回滚" : "下发", JSON.toJSONString(pushCmdDTO));
        PushResultDTO pushResultDTO = new PushResultDTO();
        PushApiResponseEntity responseEntity = new PushApiResponseEntity();

        try {
            if (pushCmdDTO.getRevert()) {
                log.info("如果是回滚命令行，暂时先不考虑");
            }

            // 1.获取到五元组对象数据
            String commandlineDTO = pushCmdDTO.getCommandline();
            GeneratePlatformApiCmdDTO cmdDTO = JSON.parseObject(commandlineDTO, GeneratePlatformApiCmdDTO.class);
            DeviceDTO device = cmdDTO.getDevice();
            PolicyDTO policy = cmdDTO.getPolicy();
            TaskDTO task = cmdDTO.getTask();
            List<String> srcAddress = new ArrayList<>();
            List<String> dstAddress = new ArrayList<>();
            List<String> postSrcAddress = new ArrayList<>();
            List<String> postDstAddress = new ArrayList<>();


            List<String> serviceNames = null;
            // 2.根据凭证id查询凭证的通行证用户名和用户密码
            CredentialEntity credentialEntity = credentialMapper.getByUuid(nodeEntity.getCredentialUuid());
            log.info("查询到凭据信息为：{}", JSON.toJSONString(credentialEntity));
            if (null == credentialEntity) {
                log.error("根据凭证uuid:[{}]查询节点对象为空!", nodeEntity.getCredentialUuid());
                return null;
            }
            String password = Encodes.decodeBase64Key(credentialEntity.getLoginPassword());

            log.info("---------绿盟api开始下发策略,主题为:[{}] ---------", task.getTheme());
            // 构建地址对象
            ManagementPlatformCreateAddressDTO addressDTO = buildAddressDTO(nodeEntity, device, policy, task,
                    credentialEntity.getLoginName(), password);
            // 1.创建地址对象
            PushResultDTO addressObjectResult = createAddressObject(addressDTO, policy, task, srcAddress, dstAddress,postSrcAddress,postDstAddress);
            responseEntity.setSrcNameList(srcAddress);
            responseEntity.setDstNameList(dstAddress);
            responseEntity.setPostSrcNameList(postSrcAddress);
            if (addressObjectResult.getSendErrorEnum() != null) {
                return addressObjectResult;
            }
            Integer taskType = pushCmdDTO.getTaskType();

            // 2.创建服务对象
            // 如果是创建Dnat策略则不需要创建服务对象
            if(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT != taskType){
                ManagementPlatformCreateServiceDTO serviceDTO = buildServiceDTO(policy, task, nodeEntity, pushCmdDTO, credentialEntity.getLoginName(), password);
                ReturnT<List<String>> serviceResult = nsfocusExternalManager.createServiceData(serviceDTO);
                if (null == serviceResult || serviceResult.getCode() != ReturnT.SUCCESS_CODE) {
                    log.error("主题为：[{}] 创建服务对象失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(serviceResult));
                    pushResultDTO.setCmdEcho(null == serviceResult ? "" : serviceResult.getMsg());
                    pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                    pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                    return pushResultDTO;
                } else {
                    // 获取创建的目的地址对象名称
                    serviceNames = new ArrayList<>();
                    serviceNames.addAll(serviceResult.getData());
                    responseEntity.setServiceNames(serviceNames);
                }
            }
            // 3.创建时间对象 TODO 目前没接口，暂且传时间对象为any 所对应的id
            List<String> scheduleNames = new ArrayList<>();
            scheduleNames.add("343001");
            // 4.构建策略参数
            ManagementPlatformCreatePolicyDTO policyDTO = buildPolicyDTO(srcAddress, dstAddress, postSrcAddress,postDstAddress, serviceNames, scheduleNames, nodeEntity, policy, task.getTheme(), policy.getIpType(), credentialEntity.getLoginName(), password);
            // 5.根据类型创建策略
            createPolicyByType(pushResultDTO, responseEntity, task, policyDTO,taskType);
            // 如果创建失败则添加直接返回
            if(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode() == pushResultDTO.getResult()){
                return pushResultDTO;
            }
            // 整体流程成功
            log.info("---------绿盟api成功下发主题为：[{}] 策略,生成策略名称：[{}]---------", task.getTheme(), responseEntity.getPolicyName());
            pushResultDTO.setCmdEcho(JSON.toJSONString(responseEntity));
            return pushResultDTO;
        } catch (Exception e) {
            log.error("绿盟api下发失败，失败原因：", e);
            pushResultDTO.setCmdEcho(e.getMessage());
            pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR);
            return pushResultDTO;
        }
    }

    /**
     * 根据类型创建策略
     * @param pushResultDTO
     * @param responseEntity
     * @param task
     * @param policyDTO
     * @param taskType
     */
    private void createPolicyByType(PushResultDTO pushResultDTO, PushApiResponseEntity responseEntity, TaskDTO task, ManagementPlatformCreatePolicyDTO policyDTO, Integer taskType) {
        // 如果是仿真开通或者是新建安全策略  走创建安全策略的逻辑
        if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED == taskType ||
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND == taskType) {
            // 对安全策略做特殊处理如果源域目的域不填则去创建策略的时候用域global去创建
            policyDTO.setSrcZone(StringUtils.isBlank(policyDTO.getSrcZone()) ? "global" : policyDTO.getSrcZone());
            policyDTO.setDstZone(StringUtils.isBlank(policyDTO.getDstZone()) ? "global" : policyDTO.getDstZone());
            ReturnT<String> policyResult = nsfocusExternalManager.createSecurityPolicyData(policyDTO);
            if (null == policyResult || policyResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建安全策略失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(policyResult));
                pushResultDTO.setCmdEcho(null == policyResult ? "" : policyResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            } else {
                log.info("主题为：[{}] 创建安全策略成功，响应数据为：", task.getTheme(), JSON.toJSONString(policyResult.getData()));
                responseEntity.setPolicyName(policyResult.getData());
                responseEntity.setIpType(IpTypeEnum.IPV4.getCode());
            }
            return;
        }
        if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType) {
            // 生成SNAT策略
            ReturnT<String> sNatPolicyResult = nsfocusExternalManager.createSNatPolicyData(policyDTO);
            if (null == sNatPolicyResult || sNatPolicyResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建SNAT策略失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(sNatPolicyResult));
                pushResultDTO.setCmdEcho(null == sNatPolicyResult ? "" : sNatPolicyResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            } else {
                log.info("主题为：[{}] 创建SNAT策略成功，响应数据为：", task.getTheme(), JSON.toJSONString(sNatPolicyResult.getData()));
                responseEntity.setPolicyName(sNatPolicyResult.getData());
            }
            return;
        }

        // 生成DNAT策略
        if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType) {
            ReturnT<String> dNatPolicyResult = nsfocusExternalManager.createDNatPolicyData(policyDTO);
            if (null == dNatPolicyResult || dNatPolicyResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建DNAT策略失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(dNatPolicyResult));
                pushResultDTO.setCmdEcho(null == dNatPolicyResult ? "" : dNatPolicyResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            } else {
                log.info("主题为：[{}] 创建DNAT策略成功，响应数据为：", task.getTheme(), JSON.toJSONString(dNatPolicyResult.getData()));
                responseEntity.setPolicyName(dNatPolicyResult.getData());
            }
        }
    }


    /**
     * 组装IPV4/IPV6策略数据
     *
     * @param srcAddress
     * @param dstAddress
     * @param serviceNames
     * @param scheduleNames
     * @return
     */
    private ManagementPlatformCreatePolicyDTO buildPolicyDTO(List<String> srcAddress, List<String> dstAddress,List<String> postSrcAddress,List<String> postDstAddress, List<String> serviceNames,
                                                             List<String> scheduleNames, NodeEntity nodeEntity, PolicyDTO policy,
                                                             String theme, Integer ipType, String userName, String password) {
        ManagementPlatformCreatePolicyDTO policyDTO = new ManagementPlatformCreatePolicyDTO();
        policyDTO.setSrcaddrs(srcAddress);
        policyDTO.setDstaddrs(dstAddress);
        policyDTO.setPostSrcaddrs(postSrcAddress);
        policyDTO.setPostDstaddrs(postDstAddress);
        policyDTO.setServiceNames(serviceNames);
        policyDTO.setScheduleNames(scheduleNames);
        if (ObjectUtils.isNotEmpty(policy)) {
            policyDTO.setSrcZone(StringUtils.isBlank(policy.getSrcZone()) ? "" : policy.getSrcZone());
            policyDTO.setDstZone(StringUtils.isBlank(policy.getDstZone()) ? "" : policy.getDstZone());
            policyDTO.setDstItf(StringUtils.isBlank(policy.getDstItf()) ? "" : policy.getDstItf()) ;
            policyDTO.setSrcItf(StringUtils.isBlank(policy.getSrcItf()) ? "" : policy.getSrcItf());
            policyDTO.setAction(policy.getAction());
            policyDTO.setIpType(policy.getIpType());
        }
        policyDTO.setDeviceUuid(nodeEntity.getUuid());
        policyDTO.setWebUrl(nodeEntity.getWebUrl());
        policyDTO.setUserName(userName);
        policyDTO.setPassword(password);
        policyDTO.setTicket(theme);
        policyDTO.setIpType(ipType);
        policyDTO.setIdleTimeout(policy.getIdleTimeout());
        policyDTO.setPostServiceList(policy.getPostServiceList());
        policyDTO.setServiceList(policy.getServiceList());
        return policyDTO;
    }


    /**
     * 创建ipv4,ipv6,地址对象
     *
     * @param addressDTO
     * @param policy
     * @param task
     * @param srcAddress
     * @param dstAddress
     * @return
     */
    private PushResultDTO createAddressObject(ManagementPlatformCreateAddressDTO addressDTO, PolicyDTO policy, TaskDTO task, List<String> srcAddress, List<String> dstAddress
                                                ,List<String> postSrcAddress,List<String> postDstAddress) {
        PushResultDTO pushResultDTO = new PushResultDTO();
        if (policy != null && policy.getIpType().intValue() == IpTypeEnum.IPV4.getCode()) {
            // 创建源地址对象
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.SRC);
            addressDTO.setIpType(IpTypeEnum.IPV4.getCode());
            ReturnT<List<String>> srcResult = nsfocusExternalManager.createIPV4SrcAddressData(addressDTO);
            if (null == srcResult || ReturnT.SUCCESS_CODE != srcResult.getCode()) {
                log.error("主题为：[{}] 创建ipv4源地址对象失败，失败原因：[{}]", task.getTheme(), null == srcResult ? "" : srcResult.getMsg());
                pushResultDTO.setCmdEcho(null == srcResult ? "" : srcResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的源地址对象名称
                srcAddress.addAll(srcResult.getData());
            }
            // 存在转换后的源地址对象的创建的情况,这里一并创建
            createPostSrcIp(addressDTO, task, postSrcAddress, pushResultDTO);


            // 创建目的地址对象
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.DST);
            ReturnT<List<String>> dstResult = nsfocusExternalManager.createIPV4DstAddressData(addressDTO);
            if (null == dstResult || dstResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建ipv4目的地址对象失败，失败原因：[{}]", task.getTheme(), null == dstResult ? "" : dstResult.getMsg());
                pushResultDTO.setCmdEcho(null == dstResult ? "" : dstResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的目的地址对象名称
                dstAddress.addAll(dstResult.getData());
            }
            // 存在转换后的目的地址对象的创建的情况,这里一并创建
            createPostDstIp(addressDTO, task, postDstAddress, pushResultDTO,dstAddress);

        } else if (policy != null && policy.getIpType().intValue() == IpTypeEnum.IPV6.getCode()) {
            // 创建源地址对象
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.SRC);
            addressDTO.setIpType(IpTypeEnum.IPV6.getCode());
            ReturnT<List<String>> ipv6SrcResult = nsfocusExternalManager.createIPV6SrcAddressData(addressDTO);
            if (null == ipv6SrcResult || ipv6SrcResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建IPV6源地址对象失败，失败原因：[{}]", task.getTheme(), null == ipv6SrcResult ? "" : ipv6SrcResult.getMsg());
                pushResultDTO.setCmdEcho(null == ipv6SrcResult ? "" : ipv6SrcResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的源地址对象名称
                srcAddress.addAll(ipv6SrcResult.getData());
            }
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.DST);
            ReturnT<List<String>> ipv6DstResult = nsfocusExternalManager.createIPV6DstAddressData(addressDTO);
            if (null == ipv6DstResult || ipv6DstResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建IPV6目的地址对象失败，失败原因：[{}]", task.getTheme(), null == ipv6DstResult ? "" : ipv6DstResult.getMsg());
                pushResultDTO.setCmdEcho(null == ipv6DstResult ? "" : ipv6DstResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的目的地址对象名称
                dstAddress.addAll(ipv6DstResult.getData());
            }
        } else {
            log.error("主题为：[{}] 暂不支持创建URL类型的地址对象", task.getTheme());
            pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR.getMessage());
            pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR.getCode());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR);
            return pushResultDTO;
        }
        return pushResultDTO;
    }

    /**
     * 创建转换后的源地址对象
     * @param addressDTO
     * @param task
     * @param postSrcAddress
     * @param pushResultDTO
     */
    private void createPostSrcIp(ManagementPlatformCreateAddressDTO addressDTO, TaskDTO task,
        List<String> postSrcAddress, PushResultDTO pushResultDTO) {
        // 如果转换后的地址为空，则不进行创建
        if (StringUtils.isBlank(addressDTO.getPostSrcIp())) {
            return;
        }
        addressDTO.setAddressPropertyEnum(AddressPropertyEnum.SRC);
        addressDTO.setIpType(IpTypeEnum.IPV4.getCode());
        addressDTO.setSrcIp(addressDTO.getPostSrcIp());
        addressDTO.setSrcIpSystem(
            StringUtils.isBlank(addressDTO.getPostSrcIpSystem()) ? "" : addressDTO.getPostSrcIpSystem());
        log.info("主题为：[{}] 创建ipv4转换后源地址对象开始---", task.getTheme());
        ReturnT<List<String>> postSrcResult = nsfocusExternalManager.createIPV4SrcAddressData(addressDTO);
        if (null == postSrcResult || postSrcResult.getCode() != ReturnT.SUCCESS_CODE) {
            log.error("主题为：[{}] 创建ipv4转换后源地址对象失败，失败原因：[{}]", task.getTheme(),
                    null == postSrcResult ? "" : postSrcResult.getMsg());
            pushResultDTO.setCmdEcho(null == postSrcResult ? "" : postSrcResult.getMsg());
            pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            return;
        } else {
            // 获取创建的源地址对象名称
            log.info("主题为：[{}] 创建ipv4转换后源地址对象成功，返回参数：[{}]", task.getTheme(),postSrcResult.getData());
            postSrcAddress.addAll(postSrcResult.getData());
            // 如果查询到的复用对象只有一个，也就是整体被复用了 就不用去创建组对象了
            if (postSrcResult.getData().size() == 1) {
                return;
            }
        }
        // 如果存在多个转换后的源地址，则对应的创建地址组对象。
        String[] postSrcIp = addressDTO.getPostSrcIp().split(",");
        if (postSrcIp.length > 1 && ReturnT.SUCCESS_CODE == postSrcResult.getCode()) {
            String addrGroupId = StringUtils.strip(postSrcResult.getData().toString(), "[]");
            addrGroupId = addrGroupId.replace(" ", "");
            addressDTO.setAddrGroupId(addrGroupId);
            ReturnT<String> addressDataGroupResult = nsfocusExternalManager.createAddressDataGroup(addressDTO);
            if (null == addressDataGroupResult || addressDataGroupResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建地址组对象失败，失败原因：[{}]", task.getTheme(),
                    null == addressDataGroupResult ? "" : addressDataGroupResult.getMsg());
                pushResultDTO.setCmdEcho(null == addressDataGroupResult ? "" : addressDataGroupResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            } else {
                // 获取创建的地址组对象名称
                postSrcAddress.clear();
                postSrcAddress.add(addressDataGroupResult.getData());
            }
        }
        return;
    }

    /**
     * 创建转换后的源地址对象
     * 
     * @param addressDTO
     * @param task
     * @param postDstAddress
     * @param pushResultDTO
     */
    private void createPostDstIp(ManagementPlatformCreateAddressDTO addressDTO, TaskDTO task,
        List<String> postDstAddress, PushResultDTO pushResultDTO,List<String> dstAddress) {
        // 如果转换后的地址为空，则不进行创建
        if (StringUtils.isBlank(addressDTO.getPostDstIp())) {
            return;
        }
        String dstIpSystem = addressDTO.getDstIpSystem();
        String postDstIpSystem = addressDTO.getPostDstIpSystem();
        // 如果填写了转换之后的目的地址，且目的地址有多个，则需要把目的地址转换为地址组
        if(dstAddress.size() > 1){
            log.info("主题为：[{}] 创建ipv4目的地址组对象开始---", task.getTheme());
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.DST);
            addressDTO.setIpType(IpTypeEnum.IPV4.getCode());
            addressDTO.setPostDstIpSystem(dstIpSystem);
            String addrGroupId = StringUtils.strip(dstAddress.toString(), "[]");
            addrGroupId = addrGroupId.replace(" ", "");
            addressDTO.setAddrGroupId(addrGroupId);
            ReturnT<String> addressDataGroupResult = nsfocusExternalManager.createAddressDataGroup(addressDTO);
            if (null == addressDataGroupResult || addressDataGroupResult.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建地址组对象失败，失败原因：[{}]", task.getTheme(),
                        null == addressDataGroupResult ? "" : addressDataGroupResult.getMsg());
                pushResultDTO.setCmdEcho(null == addressDataGroupResult ? "" : addressDataGroupResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            } else {
                // 获取创建的地址组对象名称
                dstAddress.clear();
                dstAddress.add(addressDataGroupResult.getData());
            }
        }


        addressDTO.setAddressPropertyEnum(AddressPropertyEnum.DST);
        addressDTO.setDstIp(addressDTO.getPostDstIp());
        addressDTO.setIpType(IpTypeEnum.IPV4.getCode());
        addressDTO.setDstIpSystem(postDstIpSystem);
        log.info("主题为：[{}] 创建ipv4转换后目的地址对象开始---", task.getTheme());
        ReturnT<List<String>> postDstResult = nsfocusExternalManager.createIPV4DstAddressData(addressDTO);
        if (null == postDstResult || postDstResult.getCode() != ReturnT.SUCCESS_CODE) {
            log.error("主题为：[{}] 创建ipv4转换后目的地址对象失败，失败原因：[{}]", task.getTheme(),
                    null == postDstResult ? "" : postDstResult.getMsg());
            pushResultDTO.setCmdEcho(null == postDstResult ? "" : postDstResult.getMsg());
            pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            return;
        } else {
            // 获取创建的源地址对象名称
            postDstAddress.addAll(postDstResult.getData());
            // 如果查询到的复用对象只有一个，也就是整体被复用了 就不用去创建组对象了
            if (postDstResult.getData().size() == 1) {
                return;
            }
        }
        // 如果存在多个转换后的源地址，则对应的创建地址组对象。
        String[] postDstIp = addressDTO.getPostDstIp().split(",");

        if (postDstIp.length > 1 && ReturnT.SUCCESS_CODE == postDstResult.getCode()) {
            log.info("主题为：[{}] 创建ipv4转换后目的地址组开始---", task.getTheme());
            String addrGroupId = StringUtils.strip(postDstResult.getData().toString(), "[]");
            addrGroupId = addrGroupId.replace(" ", "");
            addressDTO.setAddrGroupId(addrGroupId);
            addressDTO.setPostDstIpSystem(postDstIpSystem);
            ReturnT<String> addressDataGroupResultNew = nsfocusExternalManager.createAddressDataGroup(addressDTO);
            if (null == addressDataGroupResultNew || addressDataGroupResultNew.getCode() != ReturnT.SUCCESS_CODE) {
                log.error("主题为：[{}] 创建地址组对象失败，失败原因：[{}]", task.getTheme(),
                    null == addressDataGroupResultNew ? "" : addressDataGroupResultNew.getMsg());
                pushResultDTO.setCmdEcho(null == addressDataGroupResultNew ? "" : addressDataGroupResultNew.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
            } else {
                // 获取创建的地址组对象名称
                postDstAddress.clear();
                postDstAddress.add(addressDataGroupResultNew.getData());
            }
        }

        return;
    }

    /**
     * 组装服务对象数据
     *
     * @param policy
     * @param task
     * @param nodeEntity
     * @return
     */
    private ManagementPlatformCreateServiceDTO buildServiceDTO(PolicyDTO policy, TaskDTO task, NodeEntity nodeEntity,
                                                               PushCmdDTO pushCmdDTO, String userName, String enablePassword) {
        ManagementPlatformCreateServiceDTO serviceDTO = new ManagementPlatformCreateServiceDTO();
        serviceDTO.setTicket(task.getTheme());
        serviceDTO.setServiceList(policy.getServiceList());
        serviceDTO.setWebUrl(nodeEntity.getWebUrl());
        serviceDTO.setDeviceUuid(nodeEntity.getUuid());
        serviceDTO.setUserName(userName);
        serviceDTO.setPassword(enablePassword);
        DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
        deviceForExistObjDTO.setDeviceUuid(nodeEntity.getUuid());
        deviceForExistObjDTO.setModelNumber(pushCmdDTO.getDeviceModelNumberEnum());
        deviceForExistObjDTO.setPolicyType(policy.getType());
        serviceDTO.setDeviceForExistObjDTO(deviceForExistObjDTO);
        serviceDTO.setIdleTimeout(policy.getIdleTimeout());
        return serviceDTO;
    }

    /**
     * 构建地址对象
     *
     * @param nodeEntity
     * @param device
     * @param policy
     * @param task
     * @return
     */
    private ManagementPlatformCreateAddressDTO buildAddressDTO(NodeEntity nodeEntity, DeviceDTO device, PolicyDTO policy,
                                                               TaskDTO task, String userName, String password) {
        ManagementPlatformCreateAddressDTO addressDTO = new ManagementPlatformCreateAddressDTO();
        addressDTO.setSrcIp(policy.getSrcIp());
        addressDTO.setDstIp(policy.getDstIp());
        addressDTO.setTicket(task.getTheme());
        addressDTO.setWebUrl(nodeEntity.getWebUrl());
        addressDTO.setDeviceUuid(nodeEntity.getUuid());
        addressDTO.setUserName(userName);
        addressDTO.setPassword(password);
        addressDTO.setDeviceDTO(device);
        addressDTO.setPostSrcIp(policy.getPostSrcIp());
        addressDTO.setPostDstIp(policy.getPostDstIp());
        addressDTO.setSrcIpSystem(policy.getSrcIpSystem());
        addressDTO.setDstIpSystem(policy.getDstIpSystem());
        addressDTO.setPostSrcIpSystem(policy.getPostSrcIpSystem());
        addressDTO.setPostDstIpSystem(policy.getPostDstIpSystem());
        return addressDTO;
    }
}
