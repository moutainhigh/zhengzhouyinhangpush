package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.dto.SpecialParamDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @Title:
 * @Description: checkPoint 交互流程特殊处理
 * @date 2020/8/19
 */
@Service
public class CheckPointPolicyServiceImpl implements SendParticularPolicyService {

    private final Logger LOGGER = LoggerFactory.getLogger(CheckPointPolicyServiceImpl.class);


    @Resource
    ClientInExecuteService expectClientInExecuteService;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        LOGGER.info("CheckPoint交互流程下发package发送特殊处理START");
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        //这里是流程在下发正常规则命令之后， KSH-5129 KSH-5130
        String cmdline = "mgmt login";
        String enableUsername = pushCmdDTO.getEnableUsername();
        String enablePassword = pushCmdDTO.getEnablePassword();
        SpecialParamDTO specialParamDTO = pushCmdDTO.getSpecialParamDTO();

        String mgMtServerIP = "";
        if (specialParamDTO != null) {
            mgMtServerIP = specialParamDTO.getWebUrl();
        }

        if (StringUtils.isNotEmpty(enableUsername) && StringUtils.isNotEmpty(mgMtServerIP)) {
            cmdline = String.format("mgmt login user %s ip-address %s", enableUsername, mgMtServerIP);
        } else if (StringUtils.isNotEmpty(enableUsername) && StringUtils.isEmpty(mgMtServerIP)) {
            cmdline = String.format("mgmt login user %s", enableUsername);
        } else if (StringUtils.isEmpty(enableUsername) && StringUtils.isNotEmpty(mgMtServerIP)) {
            cmdline = String.format("mgmt login ip-address %s", mgMtServerIP);
        }
        //登录
        expectClientInExecuteService.sendAndCheckMatch(cmdline, globAndRegexElementDTO);
        //密码
        expectClientInExecuteService.sendAndCheckMatch(enablePassword, globAndRegexElementDTO);
        //多写这一行是因为在交互密码是有停顿拿不到回显
        Thread.sleep(3000);
        //这里因为登录之后没有回显的流导致需要发送一个空格
        expect.send(" \n");

    }
}
