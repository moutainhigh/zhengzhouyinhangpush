package com.abtnetworks.totems.issued.dto;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/8/13
 */
@Data
public class ParticularDTO {
    private GlobAndRegexElementDTO globAndRegexElementDTO;

    private PushCmdDTO pushCmdDTO;


    private List<String> ruleNames;

    private DeviceModelNumberEnum deviceModelNumberEnum;

    @ApiModelProperty("是否已经执行")
    private Boolean isExecute = true;

    public GlobAndRegexElementDTO getGlobAndRegexElementDTO() {
        return globAndRegexElementDTO;
    }

    public void setGlobAndRegexElementDTO(GlobAndRegexElementDTO globAndRegexElementDTO) {
        this.globAndRegexElementDTO = globAndRegexElementDTO;
    }

    public PushCmdDTO getPushCmdDTO() {
        return pushCmdDTO;
    }

    public void setPushCmdDTO(PushCmdDTO pushCmdDTO) {
        this.pushCmdDTO = pushCmdDTO;
    }


    public List<String> getRuleNames() {
        return ruleNames;
    }

    public void setRuleNames(List<String> ruleNames) {
        this.ruleNames = ruleNames;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
