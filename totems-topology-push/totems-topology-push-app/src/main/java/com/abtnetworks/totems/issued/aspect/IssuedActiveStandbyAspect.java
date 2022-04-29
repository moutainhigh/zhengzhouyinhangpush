package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.manager.impl.PushTaskManagerImpl;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuchanghao
 * @desc 命令下发之后检查主备双活状态命令回显
 * @date 2020-11-02 14:52
 */
@Aspect
@Component
public class IssuedActiveStandbyAspect {

    /***日志打印*/
    private final Logger logger = LoggerFactory.getLogger(IssuedActiveStandbyAspect.class);

    private static final String CHECK_FIREWALL_STATE_COMMANDLINE = "dis hrp state";

    private static final String STANDBY_COMMANDLINE = "STANDBY";

    private static final String ACTIVE_COMMANDLINE = "ACTIVE";

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private PushTaskManagerImpl pushTaskService;

    @Autowired
    private SendCommandService sendCommandService;


    /***
     * 命令行组装的切点
     */
    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.AfterActiceStandby)")
    public void operationPoint() {
        logger.debug("命令行回显处理方法");
    }


    @AfterReturning(returning = "result", value = "operationPoint()")
    public void afterOperation(JoinPoint joinPoint, Object result) {
        Object[] objects = joinPoint.getArgs();
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[1];
        PushResultDTO pushResultDTO = (PushResultDTO) result;
        String cmdEcho = pushResultDTO.getCmdEcho();
        // 如果是standby防火墙
        if (StringUtils.contains(cmdEcho, CHECK_FIREWALL_STATE_COMMANDLINE) && StringUtils.contains(cmdEcho, STANDBY_COMMANDLINE)) {
            // 找到备ip,凭据
            String currentIp = pushCmdDTO.getDeviceManagerIp();
            List<NodeEntity> nodeList = advancedSettingService.getAnotherDeviceByIp(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY, currentIp);
            for (NodeEntity node : nodeList) {
                if (node == null || node.getOrigin() == 1) {
                    logger.error(String.format("设备（%s）已被删除，无法下发命令行...", pushCmdDTO.getDeviceManagerIp()));
                    return;
                }

                // 重新设置命令行
                pushCmdDTO.setDeviceManagerIp(node.getIp());
                pushCmdDTO.setDeviceName(node.getDeviceName());
                Integer port = recommendTaskService.getDeviceGatherPort(node.getUuid());
                // 下发时如果有下发凭据则使用下发凭据，无下发凭据使用采集凭据
                String credentialUuid = StringUtils.isBlank(node.getPushCredentialUuid()) ? node.getCredentialUuid() : node.getPushCredentialUuid();
                CredentialEntity entity = pushTaskService.getCredentialEntity(credentialUuid);
                if (entity == null) {
                    logger.error(String.format("设备（%s）凭据为空，无法下发命令行...", pushCmdDTO.getDeviceManagerIp()));
                    return;
                }
                pushCmdDTO.setPort(port);
                pushCmdDTO.setUsername(entity.getLoginName());
                pushCmdDTO.setPassword(entity.getLoginPassword());
                pushCmdDTO.setEnableUsername(entity.getEnableUserName());
                pushCmdDTO.setEnablePassword(entity.getEnablePassword());
                pushCmdDTO.setCharset(node.getCharset());
                pushCmdDTO.setCredentialName(entity.getName());
                logger.info("检测到当前防火墙为standby，重新下发命令：{}", JSON.toJSONString(pushCmdDTO));
                sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
            }
        } else if (StringUtils.contains(cmdEcho, CHECK_FIREWALL_STATE_COMMANDLINE) && StringUtils.contains(cmdEcho, ACTIVE_COMMANDLINE)) {
            logger.info("检测到当前防火墙为active，继续下发命令：{}", JSON.toJSONString(pushCmdDTO));
            sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        }
    }

}
