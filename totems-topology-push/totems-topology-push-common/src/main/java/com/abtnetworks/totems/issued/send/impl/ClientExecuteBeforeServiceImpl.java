package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.issued.base.closure.ClosureMatchEchoService;
import com.abtnetworks.totems.issued.base.connection.ConnectShareBySameIpService;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.abtnetworks.totems.issued.send.ClientExecuteBeforeService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Closure;
import expect4j.Expect4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 执行前的准备
 */
@Service
public class ClientExecuteBeforeServiceImpl implements ClientExecuteBeforeService {
    /***
     * 日志记录
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientExecuteBeforeServiceImpl.class);
    /**
     * 连接层服务
     **/
    @Resource
    ConnectShareBySameIpService connectShareBySameIpService;

    /**
     * 闭包层服务
     **/
    @Resource
    ClosureMatchEchoService closureMatchEchoService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;


    @Override
    public Expect4j expectClientExecute(PushCmdDTO pushCmdDTO, RemoteConnectUserDTO remoteConnectUserDTO, Closure closure, GlobAndRegexElementDTO linuxPromptRegEx) throws Exception {


        Expect4j expect4j = null;
        try {

            //拿到expect4j
            expect4j = connectShareBySameIpService.connectShareBySameIp(remoteConnectUserDTO);
            linuxPromptRegEx.setExpect4j(expect4j);
            //正则与流桥接，无需关心匹配过程，只看结果
            closureMatchEchoService.closureAndMatch(linuxPromptRegEx);
            //开始发送 命令
            expectClientInExecuteService.listSendCommands(linuxPromptRegEx, pushCmdDTO);
        } catch (Exception e) {
            //非正常正常情况下关闭
            if (expect4j != null) {
                expect4j.close();
            }
            LOGGER.error("非正常情况执行中异常处理", e);
            throw e;
        }
        return expect4j;
    }
}
