package com.abtnetworks.totems.disposal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author luwei
 * @date 2019/11/15
 */
@ApiModel(value = "处置返回状态")
@Data
public class DisposalHandleCallbackDTO {

    @ApiModelProperty(value = "工单uuid")
    private String centerUuid;

    @ApiModelProperty(value = "协作单位名称")
    private String workName;

    @ApiModelProperty(value = "协作单位IP")
    private String workIp;

    @ApiModelProperty(value = "是否完成")
    private Boolean success = true;

    @ApiModelProperty(value = "是否是回滚标识")
    private Boolean callbackFlag;
}
