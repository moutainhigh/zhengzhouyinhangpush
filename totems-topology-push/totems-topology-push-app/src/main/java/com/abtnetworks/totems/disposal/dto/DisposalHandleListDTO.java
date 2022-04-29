package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author luwei
 * @date 2019/11/12
 */
@ApiModel(value = "处置协作列表")
@Data
public class DisposalHandleListDTO extends DisposalOrderCenterEntity {

    @ApiModelProperty(value = "內容uuid")
    String centerUuid;

    @ApiModelProperty(value = "派发人员")
    String createUser;

    @ApiModelProperty(value = "派发时间")
    Date createTime;

    @ApiModelProperty(value = "派发时间-字符串格式")
    String createTimeDesc;

    @ApiModelProperty(value = "业务分类描述")
    String categoryDesc;


    @ApiModelProperty(value = "工单类型描述")
    String typeDesc;

    @ApiModelProperty(value = "状态")
    Integer status;

    @ApiModelProperty(value = "状态描述")
    String statusDesc;

    @ApiModelProperty(value = "审核人")
    String auditUser;

    @ApiModelProperty(value = "是否需要审核")
    String needAuditFlag;

    @ApiModelProperty(value = "回滚标识")
    private Boolean callbackFlag;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核时间-字符串格式")
    private String auditTimeDesc;

}
