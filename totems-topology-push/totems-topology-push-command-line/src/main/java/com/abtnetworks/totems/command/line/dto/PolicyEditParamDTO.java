package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.EditTypeEnums;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import lombok.Data;

@Data
public class PolicyEditParamDTO extends PolicyParamDTO{
    /**
     * 编辑对象
     */
    private EditTypeEnums editTypeEnums;

    /**
     * 操作类型
     */
    private StatusTypeEnum statusTypeEnum;

}
