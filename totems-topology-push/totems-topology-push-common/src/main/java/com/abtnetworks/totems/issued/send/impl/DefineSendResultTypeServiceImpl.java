package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.issued.send.SensitiveWordCommonService;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import expect4j.matches.Match;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zakyoung
 * @Title:
 * @Description: 定义成不同的返回标识
 * @date 2020-03-17
 */
@Service
public class DefineSendResultTypeServiceImpl implements DefineSendResultTypeService {
    /**
     * 日志记录
     */
    private final Logger LOGGER = LoggerFactory.getLogger(DefineSendResultTypeServiceImpl.class);

    /**
     * 对发送的结果进行判断
     **/
    @Resource
    SensitiveWordCommonService sensitiveWordCommonService;

    @Override
    public MatchIndexDTO expectMatchAndCheckResult(GlobAndRegexElementDTO linuxPromptRegEx) throws Exception {
        Expect4j expect = linuxPromptRegEx.getExpect4j();
        List<Match> lstPattern = linuxPromptRegEx.getLstPattern();
        List<JSONObject> linuxPromptGlobEx = linuxPromptRegEx.getLinuxPromptGlobEx();
        List<JSONObject> jsonObjectList = linuxPromptRegEx.getLinuxPromptRegEx();

        int matchKillIndex = expect.expect(lstPattern);
        MatchIndexDTO matchIndexDTO = new MatchIndexDTO();
        matchIndexDTO.setMatchIndex(matchKillIndex);
        //为了打好日志写这行
        if (0 <= matchKillIndex && matchKillIndex < linuxPromptGlobEx.size()) {

            return commonMatchIndex(linuxPromptGlobEx, matchIndexDTO);

        } else if (linuxPromptGlobEx.size() <= matchKillIndex && matchKillIndex < lstPattern.size()) {
            matchIndexDTO.setMatchIndex(matchKillIndex - linuxPromptGlobEx.size());
            return commonMatchIndex(jsonObjectList, matchIndexDTO);
        }

        if (matchKillIndex == SendCommandStaticAndConstants.COMMAND_EXECUTION_SUCCESS_OPCODE) {
            LOGGER.error("检查到命令超时");
            throw new IssuedExecutorException(SendErrorEnum.MATCH_CMD_OUT_TIME);
        }

        if (matchKillIndex == Expect4j.RET_EOF) {
            LOGGER.error("the end of file marker was encountered when accessing the reader stream");
            throw new IssuedExecutorException(SendErrorEnum.RET_EOF);
        }
        return matchIndexDTO;
    }


    /**
     * 匹配反馈管理中的index
     *
     * @param linuxPromptGlobRegEx
     * @param matchIndexDTO
     */
    private MatchIndexDTO commonMatchIndex(List<JSONObject> linuxPromptGlobRegEx, MatchIndexDTO matchIndexDTO) {
        int matchKillIndex = matchIndexDTO.getMatchIndex();
        JSONObject jsonObject = linuxPromptGlobRegEx.get(matchKillIndex);
        String key = jsonObject.getString(SendCommandStaticAndConstants.KEY);
        String value = jsonObject.getString(SendCommandStaticAndConstants.VALUE);
        if (SendCommandStaticAndConstants.DEFAULT_PROMPT.equals(value)) {
            if (sensitiveWordCommonService.checkSensitiveWord(key)) {
                LOGGER.info("匹配到敏感正则{}", key);
                matchIndexDTO.setSensitiveWord(true);
            } else {
                LOGGER.info("匹配到正则{}", key);
                matchIndexDTO.setSensitiveWord(false);
            }
            return matchIndexDTO;
        } else {
            LOGGER.error("检查到错误匹配的关键字位置值是{}", key);
            if (StringUtils.isNotEmpty(value)) {
                SendErrorEnum.MATCH_ECHO_ERROR_VALUE_CMD.setMessage(String.format("%s (%s)", SendErrorEnum.MATCH_ECHO_ERROR_VALUE_CMD.getMessage(), value));
                throw new IssuedExecutorException(SendErrorEnum.MATCH_ECHO_ERROR_VALUE_CMD);
            } else {
                throw new IssuedExecutorException(SendErrorEnum.MATCH_ECHO_ERROR_CMD);
            }
        }
    }
}
