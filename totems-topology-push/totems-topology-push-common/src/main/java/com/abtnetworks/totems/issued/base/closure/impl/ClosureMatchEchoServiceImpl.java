package com.abtnetworks.totems.issued.base.closure.impl;

import com.abtnetworks.totems.issued.base.closure.ClosureMatchEchoService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Closure;
import expect4j.matches.GlobMatch;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-15
 */
@Service
public class ClosureMatchEchoServiceImpl implements ClosureMatchEchoService {

    /***
     * 日志记录
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClosureMatchEchoServiceImpl.class);


    @Override
    public void closureAndMatch(GlobAndRegexElementDTO globAndRegexElementDTO) throws MalformedPatternException {

        List<Match> lstPattern = new ArrayList<>();
        List<JSONObject> linuxPromptGlobEx = globAndRegexElementDTO.getLinuxPromptGlobEx();
        List<JSONObject> linuxPromptRegEx = globAndRegexElementDTO.getLinuxPromptRegEx();
        Closure closure = globAndRegexElementDTO.getClosure();

        if (CollectionUtils.isNotEmpty(linuxPromptGlobEx)) {

            for (JSONObject jsonObject : linuxPromptGlobEx) {
                String regGlob = jsonObject.getString(SendCommandStaticAndConstants.KEY);
                Match mat = new GlobMatch(regGlob, closure);
                lstPattern.add(mat);
            }
        }
        if (CollectionUtils.isNotEmpty(linuxPromptRegEx)) {
            LOGGER.info("将正则表达式放入Match中{}", JSONObject.toJSONString(linuxPromptRegEx));
            for (JSONObject jsonObject : linuxPromptRegEx) {
                String regGlob = jsonObject.getString(SendCommandStaticAndConstants.KEY);
                Match mat = new RegExpMatch(regGlob, closure);
                lstPattern.add(mat);
            }
        }
        //这里先是顺序的放入了glob，再放入了reg正则类型
        globAndRegexElementDTO.setLstPattern(lstPattern);

    }
}
