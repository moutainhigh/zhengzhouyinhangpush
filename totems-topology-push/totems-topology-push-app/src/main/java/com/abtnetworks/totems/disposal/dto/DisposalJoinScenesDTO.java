package com.abtnetworks.totems.disposal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author luwei
 * @date 2019/11/21
 */
@ApiModel(value = "处置协作-选择场景")
@Data
public class DisposalJoinScenesDTO {

    @ApiModelProperty(value = "处置id")
    private Long id;

    @ApiModelProperty(value = "场景uuid")
    private List<String> uuidList;

}
