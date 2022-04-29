package com.abtnetworks.totems.recommend.dto.task;

import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 测录仿真标签
 * @date 2021/1/20
 */
@Data
public class RecommendStartLabelDTO {
    /**标签名**/
    private String labelName;
    /**标签策略，0：合并，1为交集**/
    private String labelStrategy;
}
