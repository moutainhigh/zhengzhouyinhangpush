package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.issued.annotation.AfterExecuteRepair;
import com.abtnetworks.totems.issued.annotation.BeforeCommandBuilder;
import com.abtnetworks.totems.issued.business.dao.mysql.CommandRegularParamMapper;
import com.abtnetworks.totems.issued.business.service.RegularParamForMatchService;
import com.abtnetworks.totems.issued.common.service.impl.CommonPushBussServiceImpl;
import com.abtnetworks.totems.issued.dto.PushCommandRegularParamDTO;
import com.abtnetworks.totems.issued.send.IssuedEntrancePythonService;
import com.abtnetworks.totems.issued.send.IssuedEntranceService;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.push.service.executor.Executor;
import com.abtnetworks.totems.push.service.platform.PushNsfocusApiCmdService;
import com.abtnetworks.totems.push.service.platform.PushPlatformApiCmdService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zakyoung
 * @Title:
 * @Description: 下发业务调用入口类(路由新旧)<p> 调用之前加参数之前先看是否能公共方法中实现 {@link CommonPushBussServiceImpl}</p>
 * @date 2020-03-18
 */
@Service("issuedSendCommandService")
public class SendCommandServiceImpl implements SendCommandService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SendCommandServiceImpl.class);
    /***
     * 命令行正则匹配
     */
    @Autowired
    CommandRegularParamMapper commandRegularParamMapper;
    /***
     * 以前的的下发
     */
    @Resource
    PushService pushService;

    /**
     * 正则表示层服务
     **/
    @Resource
    RegularParamForMatchService regularParamForMatchService;

    @Autowired
    private NodeMapper nodeMapper;


    @Autowired
    private PushPlatformApiCmdService pushPlatformApiCmdService;

    @Autowired
    private PushNsfocusApiCmdService pushNsfocusApiCmdService;

    @Resource
    IssuedEntranceService issuedEntranceService;

    @Resource
    IssuedEntrancePythonService issuedEntrancePythonService;

    @BeforeCommandBuilder
    @AfterExecuteRepair
    @Override
    public PushResultDTO routeNewOrOldExecuteByRegular(PushCmdDTO pushCmdDTO) {

        // 检查是否适配管理平台API下发，适配则走API下发流程，并直接返回
        PushResultDTO apiResultDTO = checkManagementPlatformPush(pushCmdDTO);
        if (apiResultDTO != null) {
            return apiResultDTO;
        }


        PushCommandRegularParamDTO pushCommandRegularParamDTO = regularParamForMatchService.produceRegexParam(pushCmdDTO);
        if (pushCommandRegularParamDTO != null) {
            pushCmdDTO.setInterval(pushCommandRegularParamDTO.getIntervalTime());

            // 判断是否走py下发
            boolean isPushByPython = pushCmdDTO.getPushByPython();
            if(isPushByPython){
                PushResultDTO pushResultDTO = issuedEntrancePythonService.commandExecuteByPython(pushCmdDTO);
                return pushResultDTO;
            }

            // 正常ssh和telnet下发
            PushResultDTO pushResultDTO = issuedEntranceService.routeNewExecuteByRegular(pushCmdDTO, pushCommandRegularParamDTO);
            return pushResultDTO;
        } else {
            // 以前的下发
            LOGGER.info("开始以前的{}下发执行型号{}", pushCmdDTO.getExecutorType(), pushCmdDTO.getDeviceModelNumberEnum().getKey());
            Executor executor = pushService.getExecutor(pushCmdDTO);
            PushResultDTO pushResultDTO = executor.exec(pushCmdDTO);
            if (StringUtils.isEmpty(pushResultDTO.getCmdEcho())) {
                pushResultDTO.setCmdEcho(SendErrorEnum.MATCH_CMD_ERROR.getMessage());
                pushResultDTO.setSendErrorEnum(SendErrorEnum.SYSTEM_ERROR);
            }
            LOGGER.info("以前的下发执行end");
            return pushResultDTO;
        }
    }


    @BeforeCommandBuilder
    @Override
    public PushResultDTO routeNewOrOldExecuteByRegular(PushCmdDTO pushCmdDTO, String key) {
        // 检查是否适配管理平台API下发，适配则走API下发流程，并直接返回
        PushCommandRegularParamDTO pushCommandRegularParamDTO = regularParamForMatchService.produceRegexParam(pushCmdDTO);
        if (pushCommandRegularParamDTO != null) {
            //准备正则匹配项
            PushResultDTO pushResultDTO = issuedEntranceService.routeNewExecuteByRegular(pushCmdDTO, pushCommandRegularParamDTO);
            return pushResultDTO;
        } else {
            //以前的下发
            LOGGER.info("开始以前的{}下发执行型号{}", pushCmdDTO.getExecutorType(), pushCmdDTO.getDeviceModelNumberEnum().getKey());
            Executor executor = pushService.getExecutor(pushCmdDTO);
            PushResultDTO pushResultDTO = executor.exec(pushCmdDTO);
            LOGGER.info("以前的下发执行end");
            return pushResultDTO;
        }
    }

    /**
     * 检查是否适配管理平台下发
     *
     * @param pushCmdDTO
     * @return
     */
    private PushResultDTO checkManagementPlatformPush(PushCmdDTO pushCmdDTO) {
        if (null == pushCmdDTO) {
            return null;
        }
        LOGGER.info("deviceManagerIp :{}，username :{}, 命令行为：{}", pushCmdDTO.getDeviceManagerIp(), pushCmdDTO.getUsername(), pushCmdDTO.getCommandline());
        // 仅支持业务开通和策略生成的安全策略
        if (null == pushCmdDTO.getTaskType()) {
            LOGGER.info("下发策略类型为空的时候跳过飞塔管理平台下发逻辑--");
            return null;
        }
        NodeEntity nodeEntity = nodeMapper.getTheNodeByIp(pushCmdDTO.getDeviceManagerIp());
        LOGGER.info("----查到的设备信息为：{}----", JSON.toJSONString(nodeEntity));
        if (nodeEntity == null || StringUtils.isBlank(nodeEntity.getWebUrl())) {
            return null;
        }
        // 管理平台IP有值，走API下发流程
        switch (pushCmdDTO.getDeviceModelNumberEnum()) {
            case FORTINET:
            case FORTINET_V5_2:
                if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED == pushCmdDTO.getTaskType() ||
                        PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND == pushCmdDTO.getTaskType()) {
                    return pushPlatformApiCmdService.PushFortinetApiCmd(pushCmdDTO, nodeEntity);
                }
                return null;
            case NSFOCUS:
                // 绿盟支持业务开通和策略生成的安全策略,源NAT,目的NAT的策略下发
                if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED == pushCmdDTO.getTaskType() ||
                        PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND == pushCmdDTO.getTaskType() ||
                        PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == pushCmdDTO.getTaskType() ||
                        PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == pushCmdDTO.getTaskType()) {
                    return pushNsfocusApiCmdService.PushNsfocusApiCmd(pushCmdDTO, nodeEntity);
                }
                return null;
            default:
                return null;
        }
    }
}
