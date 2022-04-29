package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-18
 */
public interface ClientInExecuteService {

    /***
     * 单行命令
     * @param strCmd
     * @param linuxPromptRegEx
     * @return
     * @throws Exception
     */
    MatchIndexDTO sendAndCheckMatch(String strCmd, GlobAndRegexElementDTO linuxPromptRegEx) throws Exception;

    /***
     * 发送命令的入口
     * @param linuxPromptRegEx
     * @param pushCmdDTO

     * @throws Exception
     */
    void listSendCommands(GlobAndRegexElementDTO linuxPromptRegEx, PushCmdDTO pushCmdDTO) throws Exception;
}
