package com.abtnetworks.totems.common.dto.commandline;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/17 12:32
 */
public class PolicyRecommendCommandResultDataDTO {
    /**
     * 策略id
     */
    int id;

    /**
     * 策略命令行文本
     */
    String text;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
