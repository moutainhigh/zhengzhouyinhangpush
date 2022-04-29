package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc    策略开通任务历史数据
 * @author liuchanghao
 * @date 2020-10-26 09:32
 */
@Data
@ApiModel("策略开通任务历史数据")
public class PushRecommendTaskHistoryEntity extends BaseEntity {

    private static final long serialVersionUID = 8838994941005001962L;

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("导入时间")
    private Date importDate;

    @ApiModelProperty("导入用户")
    private String importUser;

    @ApiModelProperty("导入状态（0：成功；1：失败）")
    private Integer importStatus;

    @ApiModelProperty("文件路径")
    private String fileUrl;

    @ApiModelProperty("导入详情")
    private String importDetail;

    @ApiModelProperty("备注")
    private String remark;



}