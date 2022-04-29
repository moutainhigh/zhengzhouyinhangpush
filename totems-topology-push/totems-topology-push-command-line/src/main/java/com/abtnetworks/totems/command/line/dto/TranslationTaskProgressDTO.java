package com.abtnetworks.totems.command.line.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: WangCan
 * @Description 策略转换进度条
 * @Date: 2021/4/20
 */
@Data
@ApiModel("进度条DTO")
public class TranslationTaskProgressDTO implements Serializable {
    private static final long serialVersionUID = -3612812296623759140L;

    /**
     * 任务id
     */
    @ApiModelProperty("任务uuid")
    private String taskUuid;

    /**
     * 策略总数
     */
    @ApiModelProperty("策略总数")
    private int policyTotal;

    /**
     * 已翻译数量
     */
    @ApiModelProperty("已翻译数量")
    private int translatedNum;

    public TranslationTaskProgressDTO(String taskUuid, int policyTotal, int translatedNum) {
        this.taskUuid = taskUuid;
        this.policyTotal = policyTotal;
        this.translatedNum = translatedNum;
    }

    public void increOne(){
        this.translatedNum++;
    }
}
