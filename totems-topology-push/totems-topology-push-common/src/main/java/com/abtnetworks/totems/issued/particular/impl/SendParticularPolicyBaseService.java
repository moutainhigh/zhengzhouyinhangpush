package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.matches.GlobMatch;
import expect4j.matches.Match;
import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @Title:
 * @Description: 作为特列的基类
 * @date 2020/9/14
 */
@Component
public class SendParticularPolicyBaseService {

    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;


    /***
     * 公共的发送命令模型
     * @return
     */
    protected void commonSendMode(ParticularDTO particularDTO, String command) throws Exception {
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        expectClientInExecuteService.sendAndCheckMatch(command, globAndRegexElementDTO);
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);

    }

    /***
     * 公共添加包含匹配方法
     * @param globAndRegexElementDTO
     * @param addRegInfo
     * @throws MalformedPatternException
     */
    protected void commonAddGlobReg(GlobAndRegexElementDTO globAndRegexElementDTO, String addRegInfo) throws MalformedPatternException {
        List<JSONObject> linuxPromptGlobEx = globAndRegexElementDTO.getLinuxPromptGlobEx();
        List<Match> lstPattern = globAndRegexElementDTO.getLstPattern();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SendCommandStaticAndConstants.KEY, addRegInfo);
        jsonObject.put(SendCommandStaticAndConstants.VALUE, SendCommandStaticAndConstants.DEFAULT_PROMPT);
        linuxPromptGlobEx.add(jsonObject);
        globAndRegexElementDTO.setLinuxPromptGlobEx(linuxPromptGlobEx);
        Match mat = new GlobMatch(addRegInfo, globAndRegexElementDTO.getClosure());
        lstPattern.add(linuxPromptGlobEx.size() - 1, mat);
        globAndRegexElementDTO.setLstPattern(lstPattern);
    }

    /**
     * 移动前正常下发，同时在命令中找到要移动的策略标记
     *
     * @param pushCmdDTO
     * @param moveFlag
     * @return
     * @throws Exception
     */
    protected List<String> commonIssueFindRuleName(PushCmdDTO pushCmdDTO, GlobAndRegexElementDTO globAndRegexElementDTO, String moveFlag) throws Exception {
        List<String> ruleNames = new ArrayList<>();
        String command = pushCmdDTO.getCommandline();
        String[] commandLines = command.split(SendCommandStaticAndConstants.LINE_BREAK);

        Pattern pattern = Pattern.compile(moveFlag);
        for (String strCmd : commandLines) {
            //是华三V7/华为的找到命令行中
            Matcher matcher = pattern.matcher(strCmd);
            if (matcher.find()) {
                String ruleName = StringUtils.substring(strCmd, matcher.end() + 1).trim();
                ruleNames.add(ruleName);
            }
            expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);
        }
        return ruleNames;
    }

}
