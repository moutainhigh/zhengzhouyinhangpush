package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Closure;
import expect4j.Expect4j;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 请写类注释
 */
public interface ClientExecuteBeforeService {
    /**
     * expect组件两种连接方式
     *
     * @param pushCmdDTO
     * @param remoteConnectUserDTO
     * @param closure
     * @param linuxPromptRegEx
     * @return Expect4j
     */
    Expect4j expectClientExecute(PushCmdDTO pushCmdDTO, RemoteConnectUserDTO remoteConnectUserDTO, Closure closure, GlobAndRegexElementDTO linuxPromptRegEx) throws Exception;


}
