package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.particular.impl.CheckActiveStandServiceImpl;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author liuchanghao
 * @desc 命令下发前检查主备双活状态切面
 * @date 2020-11-02 14:52
 */
@Aspect
@Component
@Order(1)
public class IssuedAroundCheckActiveStandByAspect {

    private final Logger logger = LoggerFactory.getLogger(IssuedAroundCheckActiveStandByAspect.class);

    /***查询思科主备命令行**/
    @Value("${show-state-commandline.cisco}")
    private String checkCiscoCommandline;

    /***查询华为主备命令行**/
    @Value("${show-state-commandline.huawei}")
    private String checkHuaWeiCommandline;

    /***查询juniper-srx主备命令行**/
    @Value("${show-state-commandline.juniper-srx}")
    private String checkJuniperSrxCommandline;

    /***查询juniper-ssg主备命令行**/
    @Value("${show-state-commandline.juniper-ssg}")
    private String checkJuniperSsgCommandline;

    /***查询天融信主备命令行**/
    @Value("${show-state-commandline.topsec}")
    private String checkTopsecCommandline;

    /***查询迪普主备命令行**/
    @Value("${show-state-commandline.dptech}")
    private String checkDptechCommandline;

    /***查询山石主备命令行**/
    @Value("${show-state-commandline.hillstone}")
    private String checkHillstoneCommandline;

    /***查询飞塔主备命令行**/
    @Value("${show-state-commandline.fortinet}")
    private String checkFortinetCommandline;

    @Resource
    Map<String, SendParticularPolicyService> sendMovePolicyServiceMap;

    @Autowired
    private AdvancedSettingService advancedSettingService;

    /***
     * 命令行组装的切点
     */
    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.AroundCheckActiveStandBy)")
    public void operationPoint() {
        logger.debug("检查主备双活标记方法");
    }

    @Around(value = "operationPoint()")
    public Object operationActiveStandBy(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("进入切面,开始识别当前工单主备信息");
        Object[] objects = joinPoint.getArgs();
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[1];
        Object[] objectss = joinPoint.getArgs();
        ParticularDTO particularDTO = paramGenerate(objectss);
        logger.info("该工单是否查询了主备信息标识为:{}", pushCmdDTO.getHaveQueryActive());
        if (null != pushCmdDTO.getHaveQueryActive() && pushCmdDTO.getHaveQueryActive()) {
            logger.info("该工单已经查询了主备信息，已经切换过了主设备信息，无需重复操作");
            return joinPoint.proceed();
        }
        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        String currentIp = pushCmdDTO.getDeviceManagerIp();
        List<NodeEntity> nodeEntityList = advancedSettingService.getAnotherDeviceByIp(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY, currentIp);
        if (CollectionUtils.isNotEmpty(nodeEntityList)) {
            for (NodeEntity nodeEntity : nodeEntityList) {
                if (nodeEntity != null && nodeEntity.getOrigin() != 1) {
                    switch (modelNumberEnum) {
                        case USG6000:
                        case USG6000_NO_TOP:
                            pushCmdDTO.setQueryBeforeCommandLine(checkHuaWeiCommandline);
                            break;
                        case CISCO:
                        case CISCO_ASA_86:
                        case CISCO_ASA_99:
                            pushCmdDTO.setQueryBeforeCommandLine(checkCiscoCommandline);
                            break;
                        case HILLSTONE:
                        case HILLSTONE_V5:
                            pushCmdDTO.setQueryBeforeCommandLine(checkHillstoneCommandline);
                            break;
                        case DPTECHR003:
                        case DPTECHR004:
                            pushCmdDTO.setQueryBeforeCommandLine(checkDptechCommandline);
                            break;
                        case SRX:
                        case SRX_NoCli:
                            pushCmdDTO.setQueryBeforeCommandLine(checkJuniperSrxCommandline);
                            break;
                        case SSG:
                            pushCmdDTO.setQueryBeforeCommandLine(checkJuniperSsgCommandline);
                            break;
                        case TOPSEC_TOS_005:
                        case TOPSEC_TOS_010_020:
                        case TOPSEC_NG:
                        case TOPSEC_NG2:
                            pushCmdDTO.setQueryBeforeCommandLine(checkTopsecCommandline);
                            break;
                        case FORTINET:
                        case FORTINET_V5_2:
                            pushCmdDTO.setQueryBeforeCommandLine(checkFortinetCommandline);
                            break;
                        default:
                            logger.info("当前厂商:{}配置了主备的高级设置,但代码没有适配该厂商,跳出主备设备切换操作!", modelNumberEnum.getKey());
                            return joinPoint.proceed();
                    }
                    // 待测试
                    String serviceName = NameUtils.getServiceDefaultName(CheckActiveStandServiceImpl.class);
                    SendParticularPolicyService sendMovePolicyService = sendMovePolicyServiceMap.get(serviceName);
                    sendMovePolicyService.deviceParticularByRule(particularDTO);
                    if (particularDTO.getPushCmdDTO().getIsMaster()) {
                        logger.info("设备厂商:{}设备ip:{}查询当前设备为主设备/不支持show主备命令，不切换设备继续下发", modelNumberEnum.getKey(), currentIp);
                        return joinPoint.proceed();
                    } else {
                        logger.info("设备厂商:{}设备ip:{}查询当前设备为备设备，切换之后下发完成了，这里不进行原流程的其他切面流程的执行", modelNumberEnum.getKey(), currentIp);
                        return null;
                    }
                } else {
                    logger.info("设备厂商:{}设备ip:{}没有配置主备双活配置,跳过主备切换", modelNumberEnum.getKey(), currentIp);
                    // 其他设备
                    return joinPoint.proceed();
                }
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 公共组装参数
     *
     * @param objects
     * @return
     */
    private ParticularDTO paramGenerate(Object[] objects) {
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[1];
        GlobAndRegexElementDTO globAndRegexElementDTO = (GlobAndRegexElementDTO) objects[0];
        //要让closure中收到信息，需要做一次匹配
        ParticularDTO particularDTO = new ParticularDTO();
        particularDTO.setPushCmdDTO(pushCmdDTO);
        particularDTO.setGlobAndRegexElementDTO(globAndRegexElementDTO);
        return particularDTO;
    }

}
