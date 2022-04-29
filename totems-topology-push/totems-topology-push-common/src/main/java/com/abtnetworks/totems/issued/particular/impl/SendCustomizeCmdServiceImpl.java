package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 自定义命令行下发执行中发送的命令
 * @date 2021/8/5
 */
@Service
@Slf4j
public class SendCustomizeCmdServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info("自定义命令行下发回显匹配到More处理");
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
        // todo 处理more
        log.info( "H3CV7--More处理-3", JSONObject.toJSON( particularDTO.getDeviceModelNumberEnum())  );
        if ( null == particularDTO.getDeviceModelNumberEnum() ){
            commonAddGlobReg(globAndRegexElementDTO, SendCommandStaticAndConstants.HILLESTONE_MORE_REG);
        }else{
            log.info( "H3CV7--More处理-4" );
            commonAddGlobReg(globAndRegexElementDTO, SendCommandStaticAndConstants.H3V7_MORE_REG);

        }
        List<JSONObject> jsonObjectList = globAndRegexElementDTO.getLinuxPromptGlobEx();
        log.info( "H3CV7--More处理-5" );
        for (String commandLine : commandLines) {
            MatchIndexDTO matchIndexDTO = expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
            if (commandLine.contains(SendCommandStaticAndConstants.FORTINET_NAME_FLAG) || commandLine.contains("dis ")    ) {
                matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                int index = matchIndexDTO.getMatchIndex();
                if (index == jsonObjectList.size() - 1) {
                    log.info("自定义命令行下发回显匹配到More处理开始");
                    sendSpaceWhenReturnMore(globAndRegexElementDTO, expect, randomKey);
                } else {
                    expect.send(" \n");
                }
            }
        }

    }

    /**
     * 递归处理more命令回显
     * @param globAndRegexElementDTO
     * @param expect
     * @param randomKey
     * @throws Exception
     */
    private void sendSpaceWhenReturnMore(GlobAndRegexElementDTO globAndRegexElementDTO, Expect4j expect, String randomKey) throws Exception {
        List<JSONObject> jsonObjectList = globAndRegexElementDTO.getLinuxPromptGlobEx();
        expect.send(" ");
        MatchIndexDTO matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
        int index = matchIndexDTO.getMatchIndex();
        if (index == jsonObjectList.size() - 1) {
            sendSpaceWhenReturnMore(globAndRegexElementDTO, expect, randomKey);
            log.info("退出时发送空格匹配");
        } else {
            expect.send(" \n");
        }
    }

}
