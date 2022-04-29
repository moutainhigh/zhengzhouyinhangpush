package com.abtnetworks.totems.issued.send.impl;


import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.issued.annotation.*;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.CHECK_POINT;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.CISCO;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.H3CV5;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.H3CV7;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.H3CV7_OP;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.H3CV7_ZONE_PAIR_ACL;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.HILLSTONE;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.USG6000;
import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.USG6000_NO_TOP;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-18
 */
@Service
public class ClientInExecuteServiceImpl implements ClientInExecuteService {
    /**
     * 日志
     **/
    private final Logger LOGGER = LoggerFactory.getLogger(ClientInExecuteServiceImpl.class);
    /**
     * 对发送的结果进行判断
     **/
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Value("${HillStone.close-command}")
    private String hillstoneCloseCommand;


    @Override
    public MatchIndexDTO sendAndCheckMatch(String strCmd, GlobAndRegexElementDTO linuxPromptRegEx) throws Exception {
        Expect4j expect = linuxPromptRegEx.getExpect4j();
        MatchIndexDTO matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(linuxPromptRegEx);
        expect.send(strCmd + SendCommandStaticAndConstants.LINE_BREAK);
        //为了避开发送时敏感命令打印
        if (matchIndexDTO.getSensitiveWord()) {
            LOGGER.info("发送命令到远程******");
        } else {
            LOGGER.info("发送命令到远程{}", strCmd);
        }
        return matchIndexDTO;
    }

    @AroundCheckActiveStandBy(modelValue = {USG6000})
    @UpdateCommandSend(modelValue = {H3CV7_OP, H3CV5, H3CV7_ZONE_PAIR_ACL})
    @InExecuteMove(modelValue = {H3CV7, USG6000_NO_TOP})
    @InExecuteBuilder(modelValue = {CISCO, HILLSTONE, CHECK_POINT})
    @Override
    public void listSendCommands(GlobAndRegexElementDTO linuxPromptRegEx, PushCmdDTO pushCmdDTO) throws Exception {
        String command = pushCmdDTO.getCommandline();
        String[] commandLines = command.split(SendCommandStaticAndConstants.LINE_BREAK);
        for (String strCmd : commandLines) {
            sendAndCheckMatch(strCmd, linuxPromptRegEx);
        }

        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(linuxPromptRegEx);

        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        boolean isHillStone = DeviceModelNumberEnum.isRangeHillStoneCode(modelNumberEnum.getCode());
        if(isHillStone){
            Expect4j expect = linuxPromptRegEx.getExpect4j();
            LOGGER.info("山石设备:{}发送关闭连接命令{}到远程",pushCmdDTO.getDeviceManagerIp(),hillstoneCloseCommand);
            expect.send(hillstoneCloseCommand);
        }
    }
}
