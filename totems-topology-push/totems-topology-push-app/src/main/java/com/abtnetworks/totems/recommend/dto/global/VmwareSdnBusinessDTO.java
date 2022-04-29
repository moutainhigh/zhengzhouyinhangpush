package com.abtnetworks.totems.recommend.dto.global;

import com.abtnetworks.totems.common.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

//东西向工单实体
@ApiModel(value="东西向业务开通任务实体")
@Data
public class VmwareSdnBusinessDTO extends PageDTO {
    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("类别/域组")
    private String category;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("规则集合")
    List<VmwareSdnPlan> vmwareSdnPlanList;
}
