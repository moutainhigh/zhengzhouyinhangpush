package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.send.SendCommandBeforeBuilderService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 对下发命令切面
 */
@Aspect
@Component
public class IssuedBeforeCommandAspect {
    /***日志打印*/
    private final Logger LOGGER = LoggerFactory.getLogger(IssuedBeforeCommandAspect.class);

    @Resource
    SendCommandBeforeBuilderService sendCommandBuilderService;


    /***
     * 命令行组装的切点
     */
    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.BeforeCommandBuilder)")
    public void operationPoint() {
        LOGGER.debug("命令行标记方法");
    }


    @Before(value = "operationPoint()")
    public void beforeOperation(JoinPoint joinPoint) {
        Object[] objects = joinPoint.getArgs();
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[0];
        //对enable需要再次输入密码拼接命令行
        String commandLine = sendCommandBuilderService.getSshBuildCommand(pushCmdDTO);
        pushCmdDTO.setCommandline(commandLine);

        //若为telnet，再拼接telnet密码
        String executorType = pushCmdDTO.getExecutorType();
        if (SendCommandStaticAndConstants.TELNET_TYPE.equalsIgnoreCase(executorType)) {
            commandLine = sendCommandBuilderService.getTelnetBuildCommand(pushCmdDTO);
        }
        pushCmdDTO.setCommandline(commandLine);
    }
}
