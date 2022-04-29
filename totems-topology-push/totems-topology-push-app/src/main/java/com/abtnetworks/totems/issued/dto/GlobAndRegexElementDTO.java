package com.abtnetworks.totems.issued.dto;

import com.alibaba.fastjson.JSONObject;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.matches.Match;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/12/10
 */
@Data
public class GlobAndRegexElementDTO {
    /**
     * 正则表达式
     */
    private List<JSONObject> linuxPromptRegEx;
    /**
     * 包含匹配
     */
    private List<JSONObject> linuxPromptGlobEx;
    /***闭包流**/
    private Closure closure;
    /***形成的包**/
    private List<Match> lstPattern;
    /**
     * 客户端
     */
    Expect4j expect4j;

}
