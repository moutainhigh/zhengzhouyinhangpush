package com.abtnetworks.totems.issued.base.closure;

import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * @author zakyoung
 * @Title:
 * @Description: 通过闭包中流配置回显
 * @date 2020-03-15
 */
public interface ClosureMatchEchoService {
    /***
     * 桥接了闭包流和正则对回显匹配
     * @param globAndRegexElementDTO 正则
     *
     * @return
     * @throws MalformedPatternException
     */
    void closureAndMatch(GlobAndRegexElementDTO globAndRegexElementDTO) throws MalformedPatternException;
}
