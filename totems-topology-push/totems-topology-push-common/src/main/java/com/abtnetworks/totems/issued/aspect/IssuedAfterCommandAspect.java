package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.send.ClientExecuteAfterService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import expect4j.Expect4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/7/2
 */
@Aspect
@Component
public class IssuedAfterCommandAspect {

    /***日志打印*/
    private final Logger LOGGER = LoggerFactory.getLogger(IssuedAfterCommandAspect.class);

    @Resource
    ClientExecuteAfterService clientExecuteAfterService;

    @Value("${HillStone.close-command}")
    private String hillstoneCloseCommand;

    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    /***
     * 命令行组装的切点
     */
    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.AfterExecuteRepair)")
    public void operationPoint() {
        LOGGER.debug("命令行标记方法");
    }


    @AfterReturning(returning = "result", value = "operationPoint()")
    public void afterOperation(JoinPoint joinPoint, Object result) {
        Object[] objects = joinPoint.getArgs();
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[0];
        PushResultDTO pushResultDTO = (PushResultDTO) result;
        //若为telnet，再修补
        String executorType = pushCmdDTO.getExecutorType();
        if (SendCommandStaticAndConstants.TELNET_TYPE.equalsIgnoreCase(executorType)) {
            String commandLine = clientExecuteAfterService.repairEchoCommand(pushCmdDTO, pushResultDTO);
            pushResultDTO.setCmdEcho(commandLine);
        }

    }


    /***
     * 命令行组装的切点
     */
    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.AfterCloseConnect)")
    public void operationClosePoint() {
        LOGGER.info("进入关闭连接切面方法");
    }

    @AfterReturning(value = "operationClosePoint()")
    public void afterCloseOperation(JoinPoint joinPoint) throws Exception{

        Object[] objects = joinPoint.getArgs();
        ParticularDTO particularDTO = paramGenerate(objects);
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();

        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        boolean isHillstone = DeviceModelNumberEnum.isRangeHillStoneCode(modelNumberEnum.getCode());

        if (isHillstone) {
            LOGGER.info("关闭当前山石服务端连接，发送:{}命令",hillstoneCloseCommand);
            expect.send(hillstoneCloseCommand);
        }
    }



    /**
     * 公共组装参数
     *
     * @param objects
     * @return
     */
    private ParticularDTO paramGenerate(Object[] objects) {
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[0];
        GlobAndRegexElementDTO globAndRegexElementDTO = (GlobAndRegexElementDTO) objects[3];
        //要让closure中收到信息，需要做一次匹配
        ParticularDTO particularDTO = new ParticularDTO();
        particularDTO.setPushCmdDTO(pushCmdDTO);
        particularDTO.setGlobAndRegexElementDTO(globAndRegexElementDTO);
        return particularDTO;
    }
}
