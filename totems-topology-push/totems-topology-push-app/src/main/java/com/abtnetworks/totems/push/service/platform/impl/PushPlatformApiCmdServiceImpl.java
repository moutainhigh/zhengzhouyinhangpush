package com.abtnetworks.totems.push.service.platform.impl;

import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratePlatformApiCmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.dto.platform.dto.*;
import com.abtnetworks.totems.push.entity.PushApiResponseEntity;
import com.abtnetworks.totems.push.manager.FortiManagementPlatformManager;
import com.abtnetworks.totems.push.service.platform.PushPlatformApiCmdService;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @desc    管理平台API对接下发接口
 * @author liuchanghao
 * @date 2021-02-22 10:21
 */
@Slf4j
@Service
public class PushPlatformApiCmdServiceImpl implements PushPlatformApiCmdService {

    private static Logger logger = LoggerFactory.getLogger(PushPlatformApiCmdServiceImpl.class);

    @Autowired
    private FortiManagementPlatformManager fortiManagementPlatformManager;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;


    @Override
    public PushResultDTO PushFortinetApiCmd(PushCmdDTO pushCmdDTO, NodeEntity nodeEntity) {
        logger.info("-------开始执行{}飞塔管理平台工单:{}-------",pushCmdDTO.getRevert() ? "回滚":"下发",JSON.toJSONString(pushCmdDTO) );
        PushResultDTO pushResultDTO = new PushResultDTO();
        PushApiResponseEntity responseEntity = new PushApiResponseEntity();

        try{
            // 判断是否是虚墙，是虚墙，adom则需传虚墙名称
            String vsysName = "root";
            String hostname = null;
            logger.info("请求whale 数据设备uuid：{}", nodeEntity.getUuid());
            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(nodeEntity.getUuid());
            if (deviceRO != null && deviceRO.getSuccess() && deviceRO.getData() != null && deviceRO.getData().size() > 0) {
                logger.info("请求whale 返回数据：{}", JSON.toJSONString(deviceRO));
                DeviceDataRO dataObject = deviceRO.getData().get(0);
                hostname = dataObject.getName();
                if(ObjectUtils.isNotEmpty(dataObject) && dataObject.getIsVsys() != null && dataObject.getIsVsys()){
                    vsysName = dataObject.getVsysName();
                }
            }

            if(pushCmdDTO.getRevert()){
                // 管理平台下发回滚
                String fortinetPlatformEcho = pushCmdDTO.getFortinetPlatformEcho();
                logger.info("fortinetPlatformEcho: {}", fortinetPlatformEcho);
                PushApiResponseEntity revertEntity = JSON.parseObject(fortinetPlatformEcho, PushApiResponseEntity.class);
                logger.info("revertEntity : {}", JSON.toJSONString(revertEntity));
                String rollbackPolicyId = revertEntity.getPolicyId();
                logger.info("---------管理平台开始回滚策略ID为：[{}] 策略---------", rollbackPolicyId);
                ManagementPlatformCreatePolicyDTO policyRevertDTO = buildPolicyDTO(null,null,null,null,rollbackPolicyId, nodeEntity,null,vsysName, hostname,null,revertEntity.getIpType(),null,null);
                ReturnT<String> rollbackResult = fortiManagementPlatformManager.fortinetDeletePolicy(policyRevertDTO);
                if( null == rollbackResult || rollbackResult.getCode() != ReturnT.SUCCESS_CODE ){
                    logger.error("策略ID为：[{}] 回滚策略失败，失败原因：[{}]",rollbackPolicyId, rollbackPolicyId, JSON.toJSONString(rollbackResult));
                    pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_DELETE_POLICY_ERROR.getMessage());
                    pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_DELETE_POLICY_ERROR.getCode());
                    pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_DELETE_POLICY_ERROR);
                    return pushResultDTO;
                } else {
                    logger.info("策略ID为：[{}] 回滚策略成功，响应数据：[{}]", rollbackPolicyId, JSON.toJSONString(rollbackResult));
                    pushResultDTO.setCmdEcho(JSON.toJSONString(rollbackResult));
                    pushResultDTO.setResult(ReturnCode.POLICY_MSG_OK);
                    return pushResultDTO;
                }
            }

            // 组装地址对象数据
            String commandlineDTO = pushCmdDTO.getCommandline();
            GeneratePlatformApiCmdDTO cmdDTO = JSON.parseObject(commandlineDTO, GeneratePlatformApiCmdDTO.class);
            DeviceDTO device = cmdDTO.getDevice();
            PolicyDTO policy = cmdDTO.getPolicy();
            TaskDTO task = cmdDTO.getTask();
            List<String> srcAddress = new ArrayList<>();
            List<String> dstAddress = new ArrayList<>();
            List<String> serviceNames;
            List<String> scheduleNames;
            String policyId;

            logger.info("虚墙名字：{}", vsysName);
            logger.info("---------管理平台开始下发主题为：[{}] 策略---------", task.getTheme());


            ManagementPlatformCreateAddressDTO addressDTO = buildCreateAddressDTO(policy, task, nodeEntity,vsysName,device);
            // 1.创建IPV4地址对象
            PushResultDTO addressObjectResult = createAddressObject(addressDTO,policy, task, srcAddress,dstAddress);
            responseEntity.setSrcNameList(srcAddress);
            responseEntity.setDstNameList(dstAddress);
            if(addressObjectResult.getSendErrorEnum() != null){
                return addressObjectResult;
            }

            // 2.创建服务对象
            ManagementPlatformCreateServiceDTO serviceDTO = buildServiceDTO(policy, task, nodeEntity,vsysName,device,pushCmdDTO);
            ReturnT<List<String>> serviceResult = fortiManagementPlatformManager.fortinetCreateServiceObject(serviceDTO);
            if( null == serviceResult || serviceResult.getCode() != ReturnT.SUCCESS_CODE ){
                logger.error("主题为：[{}] 创建服务对象失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(serviceResult));
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

            // 3.创建时间对象
            ManagementPlatformCreateTimeDTO timeDTO = buildTimeDTO(policy, task, nodeEntity,vsysName);
            ReturnT<String> timeResult = fortiManagementPlatformManager.fortinetCreateTimeObject(timeDTO);
            if( null == timeResult || timeResult.getCode() != ReturnT.SUCCESS_CODE ){
                logger.error("主题为：[{}] 创建时间对象失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(timeResult));
                pushResultDTO.setCmdEcho(null == timeResult ? "" : timeResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的时间对象名称
                scheduleNames = new ArrayList<>();
                scheduleNames.add(timeResult.getData());
                responseEntity.setScheduleNames(scheduleNames);
            }

            // 4.生成策略
            ManagementPlatformCreatePolicyDTO policyDTO = buildPolicyDTO(srcAddress, dstAddress, serviceNames, scheduleNames,null,nodeEntity,policy,vsysName, hostname,task.getTheme(),policy.getIpType(),null,null);
            if(policy != null && policy.getIpType().intValue() == IpTypeEnum.IPV4.getCode()){
                ReturnT<String> policyResult = fortiManagementPlatformManager.fortinetCreateIPV4Policy(policyDTO);
                if( null == policyResult || policyResult.getCode() != ReturnT.SUCCESS_CODE ){
                    logger.error("主题为：[{}] 创建IPV4策略失败，失败原因：[{}]", task.getTheme(), JSON.toJSONString(policyResult));
                    pushResultDTO.setCmdEcho(null == policyResult ? "" :policyResult.getMsg());
                    pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                    pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                    return pushResultDTO;
                } else {
                    logger.info("主题为：[{}] 创建IPV4策略成功，响应数据为：", task.getTheme(), JSON.toJSONString(policyResult.getData()));
                    policyId = policyResult.getData();
                    responseEntity.setPolicyId(policyId);
                    responseEntity.setIpType(IpTypeEnum.IPV4.getCode());
                }
            } else if(policy != null && policy.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
                ReturnT<String> policyIPV6Result = fortiManagementPlatformManager.fortinetCreateIPV6Policy(policyDTO);
                if( null == policyIPV6Result || policyIPV6Result.getCode() != ReturnT.SUCCESS_CODE ){
                    logger.error("主题为：[{}] 创建IPV6策略失败，失败原因：[{}]", task.getTheme(),
                        null == policyIPV6Result ? "" : policyIPV6Result.getMsg());
                    pushResultDTO.setCmdEcho(null == policyIPV6Result ? "" : policyIPV6Result.getMsg());
                    pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                    pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                    return pushResultDTO;
                } else {
                    logger.info("主题为：[{}] 创建IPV4策略成功，响应数据为：", task.getTheme(), JSON.toJSONString(policyIPV6Result.getData()));
                    policyId = policyIPV6Result.getData();
                    responseEntity.setPolicyId(policyId);
                    responseEntity.setIpType(IpTypeEnum.IPV6.getCode());
                }
            } else {
                logger.error("主题为：[{}] 暂不支持创建URL类型的策略", task.getTheme());
                pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR.getMessage());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR);
                return pushResultDTO;
            }
            // 拿到commandTaskEntity后更新回滚命令行
//            CommandTaskEditableEntity commandTaskEntity = pushCmdDTO.getCommandTaskEntity();
//            commandTaskEntity.setCommandlineRevert("回滚policyId: " + policyId);
//            commandTaskEntity.setRevertTime(new Date());
//            logger.info("主题：[{}] 更新回滚命令行为：[{}]", task.getTheme(), commandTaskEntity.getCommandlineRevert());

            // 5.是否移动
            logger.info("当前设备的移动配置为：[{}]", JSON.toJSONString(pushCmdDTO.getMoveParamDTO()));
            if(pushCmdDTO.getMoveParamDTO() != null && StringUtils.isNotEmpty(pushCmdDTO.getMoveParamDTO().getRelatedName())){
                // 当前只考虑ipv4的移动设置，ipv6的不移动,带后面高级设置添加ip类型之后再修改(现在的移动对象里面的ipType直接取得工单里面的，没有实际意义)
                if (IpTypeEnum.IPV6.getCode().equals(pushCmdDTO.getMoveParamDTO().getIpType())) {
                    log.info("当前只考虑ipv4类型的移动策略,ipv6暂不做处理");
                } else {
                    String relatedName = pushCmdDTO.getMoveParamDTO().getRelatedName();
                    String relatedRule = pushCmdDTO.getMoveParamDTO().getRelatedRule();
                    ManagementPlatformCreatePolicyDTO moveDTO = buildPolicyDTO(srcAddress, dstAddress, serviceNames, scheduleNames, responseEntity.getPolicyId(), nodeEntity, policy, vsysName, hostname, task.getTheme(), policy.getIpType(), relatedName, relatedRule);
                    ReturnT<String> moveResult = fortiManagementPlatformManager.fortinetMovePolicy(moveDTO);
                    logger.info("移动策略结束，响应数据：[{}]", JSON.toJSONString(moveResult));
                    if (null == moveResult || moveResult.getCode() != ReturnT.SUCCESS_CODE) {
                        pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_MOVE_POLICY_ERROR.getMessage());
                        pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_MOVE_POLICY_ERROR.getCode());
                        pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_MOVE_POLICY_ERROR);
                        return pushResultDTO;
                    }
                }
            }

            // 6.install安装
            ManagementPlatformInstallDTO installDTO = buildInstallDTO(nodeEntity,vsysName, hostname);
            ReturnT<String> installResult = fortiManagementPlatformManager.fortinetInstall(installDTO);
            logger.info("管理平台安装主题为：[{}]策略完成，响应数据为：[{}]", task.getTheme(),
                null == installResult ? "" : installResult.getData());
            // 6.保存安装响应数据到下发详情中
            if( null == installResult || installResult.getCode() != ReturnT.SUCCESS_CODE ){
                logger.error("主题为：[{}] 管理平台install策略失败", task.getTheme());
                pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_INSTALL_ERROR.getMessage());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_INSTALL_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_INSTALL_ERROR);
                return pushResultDTO;
            } else {
                // 整体流程成功
                logger.info("---------管理平台成功下发主题为：[{}] 策略,响应数据：[{}]---------", task.getTheme(), installResult.getData());
                if(installResult.getData() != null ){
                    responseEntity.setInstallResult(JSON.parseObject(installResult.getData()));
                    pushResultDTO.setResult(ReturnCode.POLICY_MSG_OK);
                } else {
                    // 未获取到安装响应数据
                    JSONObject errMsg = new JSONObject();
                    errMsg.put("errorMsg", SendErrorEnum.PLATFORM_API_RESPONSE_ERROR.getMessage());
                    responseEntity.setInstallResult(errMsg);
                    pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR.getMessage());
                    pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR.getCode());
                    pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                }
                pushResultDTO.setCmdEcho(JSON.toJSONString(responseEntity));
                return pushResultDTO;
            }

        } catch (Exception e) {
            logger.error("飞塔管理平台下发失败，失败原因：", e);
            pushResultDTO.setCmdEcho(e.getMessage());
            pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR);
            return pushResultDTO;
        }
    }

    /**
     * 创建源地址/目的地址对象
     * @param addressDTO
     * @param policy
     * @param task
     * @param srcAddress
     * @param dstAddress
     * @return
     */
    private PushResultDTO createAddressObject(ManagementPlatformCreateAddressDTO addressDTO, PolicyDTO policy, TaskDTO task,
                                              List<String> srcAddress, List<String> dstAddress){
        PushResultDTO pushResultDTO = new PushResultDTO();
        if(policy != null && policy.getIpType().intValue() == IpTypeEnum.IPV4.getCode()){
            // 创建源地址对象
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.SRC);
            ReturnT<List<String>> srcResult = fortiManagementPlatformManager.fortinetCreateIPV4SrcAddress(addressDTO);
            if( null == srcResult || srcResult.getCode() != ReturnT.SUCCESS_CODE ){
                logger.error("主题为：[{}] 创建源地址对象失败，失败原因：[{}]", task.getTheme(), null == srcResult ? "" : srcResult.getMsg());
                pushResultDTO.setCmdEcho(null == srcResult ? "" : srcResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的源地址对象名称
                srcAddress.addAll(srcResult.getData());
            }

            // 创建目的地址对象
            addressDTO.setAddressPropertyEnum(AddressPropertyEnum.DST);
            ReturnT<List<String>> dstResult = fortiManagementPlatformManager.fortinetCreateIPV4DstAddress(addressDTO);
            if( null == dstResult || dstResult.getCode() != ReturnT.SUCCESS_CODE ){
                logger.error("主题为：[{}] 创建目的地址对象失败，失败原因：[{}]", task.getTheme(),
                    null == dstResult ? "" : dstResult.getMsg());
                pushResultDTO.setCmdEcho(null == dstResult ? "" : dstResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的目的地址对象名称
                dstAddress.addAll(dstResult.getData());
            }
        } else if(policy != null && policy.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
            // 创建IPV6地址对象
            ReturnT<List<String>> ipv6SrcResult = fortiManagementPlatformManager.fortinetCreateIPV6SrcAddress(addressDTO);
            if (null == ipv6SrcResult || ipv6SrcResult.getCode() != ReturnT.SUCCESS_CODE) {
                logger.error("主题为：[{}] 创建IPV6源地址对象失败，失败原因：[{}]", task.getTheme(),
                    null == ipv6SrcResult ? "" : ipv6SrcResult.getMsg());
                pushResultDTO.setCmdEcho(null == ipv6SrcResult ? "" : ipv6SrcResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的源地址对象名称
                srcAddress.addAll(ipv6SrcResult.getData());
            }

            ReturnT<List<String>> ipv6DstResult = fortiManagementPlatformManager.fortinetCreateIPV6SrcAddress(addressDTO);
            if (null == ipv6DstResult || ipv6DstResult.getCode() != ReturnT.SUCCESS_CODE) {
                logger.error("主题为：[{}] 创建IPV6目的地址对象失败，失败原因：[{}]", task.getTheme(),
                    null == ipv6DstResult ? "" : ipv6DstResult.getMsg());
                pushResultDTO.setCmdEcho(null == ipv6DstResult ? "" : ipv6DstResult.getMsg());
                pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_SYSTEM_ERROR.getCode());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_RESPONSE_ERROR);
                return pushResultDTO;
            } else {
                // 获取创建的目的地址对象名称
                dstAddress.addAll(ipv6DstResult.getData());
            }
        } else {
            logger.error("主题为：[{}] 暂不支持创建URL类型的地址对象", task.getTheme());
            pushResultDTO.setCmdEcho(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR.getMessage());
            pushResultDTO.setResult(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR.getCode());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PLATFORM_API_UN_SUPPORT_URL_ERROR);
            return pushResultDTO;
        }
        return pushResultDTO;
    }

    /**
     * 组装地址对象数据
     * @param policy
     * @param nodeEntity
     * @return
     */
    private ManagementPlatformCreateAddressDTO buildCreateAddressDTO(PolicyDTO policy, TaskDTO task, NodeEntity nodeEntity, String vsysName,
                                                                     DeviceDTO deviceDTO){
        ManagementPlatformCreateAddressDTO addressDTO = new ManagementPlatformCreateAddressDTO();
        addressDTO.setSrcIp(policy.getSrcIp());
        addressDTO.setDstIp(policy.getDstIp());
        addressDTO.setTicket(task.getTheme());
        addressDTO.setWebUrl(nodeEntity.getWebUrl());
        addressDTO.setDeviceUuid(nodeEntity.getUuid());
        addressDTO.setVsysName(vsysName);
        addressDTO.setDeviceDTO(deviceDTO);
        return addressDTO;
    }

    /**
     * 组装服务对象数据
     * @param policy
     * @param task
     * @param nodeEntity
     * @return
     */
    private ManagementPlatformCreateServiceDTO buildServiceDTO(PolicyDTO policy, TaskDTO task, NodeEntity nodeEntity, String vsysName, DeviceDTO device
                                                                , PushCmdDTO pushCmdDTO){
        ManagementPlatformCreateServiceDTO serviceDTO = new ManagementPlatformCreateServiceDTO();
        serviceDTO.setTicket(task.getTheme());
        serviceDTO.setServiceList(policy.getServiceList());
        serviceDTO.setWebUrl(nodeEntity.getWebUrl());
        serviceDTO.setDeviceUuid(nodeEntity.getUuid());

        DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
        deviceForExistObjDTO.setDeviceUuid(nodeEntity.getUuid());
        deviceForExistObjDTO.setModelNumber(pushCmdDTO.getDeviceModelNumberEnum());
        deviceForExistObjDTO.setPolicyType(policy.getType());
        serviceDTO.setDeviceForExistObjDTO(deviceForExistObjDTO);
        serviceDTO.setIdleTimeout(policy.getIdleTimeout());
        serviceDTO.setVsysName(vsysName);
        return serviceDTO;
    }

    /**
     * 组装时间对象数据
     * @param policy
     * @param task
     * @param nodeEntity
     * @return
     */
    private ManagementPlatformCreateTimeDTO buildTimeDTO(PolicyDTO policy, TaskDTO task, NodeEntity nodeEntity, String vsysName){
        ManagementPlatformCreateTimeDTO timeDTO = new ManagementPlatformCreateTimeDTO();
        timeDTO.setSrcIp(policy.getSrcIp());
        timeDTO.setDstIp(policy.getDstIp());
        timeDTO.setTicket(task.getTheme());
        timeDTO.setStartTime(policy.getStartTime());
        timeDTO.setEndTime(policy.getEndTime());
        timeDTO.setWebUrl(nodeEntity.getWebUrl());
        timeDTO.setDeviceUuid(nodeEntity.getUuid());
        timeDTO.setVsysName(vsysName);
        return timeDTO;
    }

    /**
     * 组装IPV4/IPV6策略数据
     * @param srcAddress
     * @param dstAddress
     * @param serviceNames
     * @param scheduleNames
     * @param policyId
     * @return
     */
    private ManagementPlatformCreatePolicyDTO buildPolicyDTO(List<String> srcAddress, List<String> dstAddress, List<String> serviceNames,
                                                             List<String> scheduleNames, String policyId, NodeEntity nodeEntity, PolicyDTO policy, String vsysName,
                                                             String hostname, String theme, Integer ipType, String relatedName, String relatedRule){
        ManagementPlatformCreatePolicyDTO policyDTO = new ManagementPlatformCreatePolicyDTO();
        policyDTO.setSrcaddrs(srcAddress);
        policyDTO.setDstaddrs(dstAddress);
        policyDTO.setServiceNames(serviceNames);
        policyDTO.setScheduleNames(scheduleNames);
        if(ObjectUtils.isNotEmpty(policy)){
            policyDTO.setSrcItfAlias(StringUtils.isBlank(policy.getSrcItfAlias()) ? "any" : policy.getSrcItfAlias());
            policyDTO.setDstItfAlias(StringUtils.isBlank(policy.getDstItfAlias()) ? "any" : policy.getDstItfAlias());
            policyDTO.setAction(policy.getAction());
            policyDTO.setIpType(policy.getIpType());
        }
        policyDTO.setPolicyId(policyId);
        policyDTO.setDeviceUuid(nodeEntity.getUuid());
        policyDTO.setWebUrl(nodeEntity.getWebUrl());
        policyDTO.setHostName(hostname);
        policyDTO.setVsysName(vsysName);
        policyDTO.setTicket(theme);
        policyDTO.setIpType(ipType);

        // 移动相关属性
        policyDTO.setTargetPolicyId(relatedRule);
        if(StringUtils.equalsAnyIgnoreCase(relatedName, AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE)){
            // 移动到之前
            policyDTO.setMoveSeatEnum(MoveSeatEnum.BEFORE);
        } else if(StringUtils.equalsAnyIgnoreCase(relatedName, AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER)){
            // 移动到之后
            policyDTO.setMoveSeatEnum(MoveSeatEnum.AFTER);
        } else {
            policyDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
        }
        return policyDTO;
    }


    /**
     * 组装install安装参数
     * @param nodeEntity
     * @return
     */
    private ManagementPlatformInstallDTO buildInstallDTO(NodeEntity nodeEntity, String vsysName, String hostname){
        ManagementPlatformInstallDTO installDTO = new ManagementPlatformInstallDTO();
        installDTO.setWebUrl(nodeEntity.getWebUrl());
        installDTO.setHostName(hostname);
        installDTO.setVsysName(vsysName);
        installDTO.setDeviceUuid(nodeEntity.getUuid());
        return installDTO;
    }

}
