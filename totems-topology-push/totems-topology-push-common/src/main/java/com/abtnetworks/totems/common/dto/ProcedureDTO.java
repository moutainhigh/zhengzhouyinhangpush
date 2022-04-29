package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.enums.PolicyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("流程数据")
public class ProcedureDTO {

    @ApiModelProperty("流程数组对象")
    List<Integer> steps;

    @ApiModelProperty("命令行生成器")
    String generator;

    @ApiModelProperty("回滚命令行生成器")
    String rollbackGenerator;
}
