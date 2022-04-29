package com.abtnetworks.totems.issued.dto;

import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 匹配时，返回匹配的正则所在游标，并且根据游标找到是否是正则敏感关键字
 * @date 2021/1/12
 */
@Data
public class MatchIndexDTO {
    /**
     * 匹配游标
     */
    private Integer matchIndex;
    /**
     * 是否敏感匹配
     */
    private Boolean sensitiveWord;
}
