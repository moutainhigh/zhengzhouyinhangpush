package com.abtnetworks.totems.push.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author luwei
 * @date 2020/9/12
 */
@ApiModel(value = "开始下发-请求入参")
@Data
public class ForbidSendCommandRequest {

    @ApiModelProperty("封禁工单uuid集合")
    private List<String> forbidUuidList;
}
