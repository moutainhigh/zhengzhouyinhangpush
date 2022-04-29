package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-17
 */
public interface DefineSendResultTypeService {
    /***
     * 执行后对应正则<P>lstPattern</P>中找到回显命令匹配游标
     * @param linuxPromptRegEx
     * @return
     * @throws Exception
     */
    MatchIndexDTO expectMatchAndCheckResult(GlobAndRegexElementDTO linuxPromptRegEx) throws Exception;


}
