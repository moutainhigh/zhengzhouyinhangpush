package com.abtnetworks.totems.issued.send;

/**
 * @author zakyoung
 * @Title:
 * @Description: 鉴别敏感词（命令）
 * @date 2020-03-17
 */
public interface SensitiveWordCommonService {

    /***
     *  检查敏感词（password等）
     * @param regStr
     * @return
     */
    boolean checkSensitiveWord(String regStr);

}
