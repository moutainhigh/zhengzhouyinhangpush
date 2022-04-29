package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.whale.system.dto.FortressDeviceInfoDTO;
import com.abtnetworks.totems.whale.system.ro.FortressDeviceRO;
import com.abtnetworks.totems.whale.system.service.WhaleProxyClient;
import com.alibaba.fastjson.JSONObject;
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
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/9/8
 */
@Aspect
@Component
public class IssuedProxyMachineAspect {

    /**
     * 日志
     **/
    private final Logger LOGGER = LoggerFactory.getLogger(IssuedConnectDisposeAspect.class);
    @Autowired
    WhaleProxyClient whaleProxyClient;

    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.ProxyEnable)")
    public void processConnectPoint() {
        LOGGER.debug("下发流程前切点标记方法");
    }

    @Before(value = "processConnectPoint()")
    public void issuedBefore(JoinPoint joinPoint) {
        //获取注解标注的方法
        Object[] args = joinPoint.getArgs();
        PushCmdDTO pushCmdDTO = (PushCmdDTO) args[0];
        FortressDeviceInfoDTO fortressDeviceInfoDTO = new FortressDeviceInfoDTO();
        fortressDeviceInfoDTO.setHostAddress(pushCmdDTO.getDeviceManagerIp());
        fortressDeviceInfoDTO.setLoginName(pushCmdDTO.getUsername());
        fortressDeviceInfoDTO.setEnableLoginName(pushCmdDTO.getEnableUsername());
        try {
            //佛山齐治堡垒机的特殊性导致把凭据名当成设备名
            fortressDeviceInfoDTO.setDeviceName(pushCmdDTO.getCredentialName());
            LOGGER.info("查询青提的堡垒机的设备信息中参数{}", JSONObject.toJSONString(fortressDeviceInfoDTO));
            FortressDeviceRO fortressDeviceRO = whaleProxyClient.queryDevicePassword(fortressDeviceInfoDTO);
            LOGGER.info("查询青提的堡垒机的设备信息中返回参数{}", JSONObject.toJSONString(fortressDeviceRO));
            if (ObjectUtils.isNotEmpty(fortressDeviceRO) ) {
                if(StringUtils.isNotBlank(fortressDeviceRO.getDevicePassword())){
                    pushCmdDTO.setPassword(fortressDeviceRO.getDevicePassword());
                }
                if(StringUtils.isNotBlank(fortressDeviceRO.getEnablePassword())){
                    pushCmdDTO.setEnablePassword(fortressDeviceRO.getEnablePassword());
                }
            } else {
                LOGGER.info("正常连接，不走堡垒机连接获取密码下发");
            }
        } catch (Exception e) {
            LOGGER.warn("调用堡垒机出现异常", e);
        }
    }
}
