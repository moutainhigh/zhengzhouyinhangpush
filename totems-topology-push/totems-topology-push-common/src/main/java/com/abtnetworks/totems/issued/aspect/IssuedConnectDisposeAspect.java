package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.abtnetworks.totems.whale.system.dto.FortressDeviceInfoDTO;
import com.abtnetworks.totems.whale.system.ro.FortressDeviceRO;
import com.abtnetworks.totems.whale.system.service.WhaleProxyClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zakyoung
 * @Title:
 * @Description: 这里处理下发连接时的切面
 * @date 2020-03-20
 */
@Aspect
@Component
public class IssuedConnectDisposeAspect {
    /**
     * 日志
     **/
    private final Logger LOGGER = LoggerFactory.getLogger(IssuedConnectDisposeAspect.class);
    @Autowired
    WhaleProxyClient whaleProxyClient;

    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.ConnectDispose)")
    public void processConnectPoint() {
        LOGGER.debug("下发连接时切点标记方法");
    }

    @Before(value = "processConnectPoint()")
    public void connectBefore(JoinPoint joinPoint) {
        //获取注解标注的方法
        Object[] args = joinPoint.getArgs();
        RemoteConnectUserDTO remoteConnectUserDTO = (RemoteConnectUserDTO) args[0];
        FortressDeviceInfoDTO fortressDeviceInfoDTO = new FortressDeviceInfoDTO();
        fortressDeviceInfoDTO.setHostAddress(remoteConnectUserDTO.getDeviceManagerIp());
        fortressDeviceInfoDTO.setLoginName(remoteConnectUserDTO.getUsername());
        try {
            FortressDeviceRO fortressDeviceRO = whaleProxyClient.queryDevicePassword(fortressDeviceInfoDTO);
            if (ObjectUtils.isNotEmpty(fortressDeviceRO) && StringUtils.isNotBlank(fortressDeviceRO.getDevicePassword())) {
                remoteConnectUserDTO.setPassword(fortressDeviceRO.getDevicePassword());
            } else {
                LOGGER.info("正常连接，不走堡垒机连接获取密码下发");
            }
        } catch (Exception e) {
            LOGGER.warn("调用堡垒机出现异常", e);
        }
    }


}
