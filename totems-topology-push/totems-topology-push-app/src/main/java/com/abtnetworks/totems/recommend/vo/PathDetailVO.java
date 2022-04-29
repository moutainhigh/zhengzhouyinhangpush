package com.abtnetworks.totems.recommend.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("路径数据对象")
public class PathDetailVO {

    @ApiModelProperty("路径信息Id")
    private Integer pathInfoId;

    @ApiModelProperty("路径信息数据")
    private JSONObject detailPath;
}
