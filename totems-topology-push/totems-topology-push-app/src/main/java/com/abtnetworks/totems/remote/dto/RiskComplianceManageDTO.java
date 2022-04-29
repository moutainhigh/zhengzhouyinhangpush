package com.abtnetworks.totems.remote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.Date;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/23
 */
@Data
public class RiskComplianceManageDTO {

    /**
     * 主键
     **/
    @ApiModelProperty("主键")
    private Long id;
    /**
     * 对象名称
     **/
    @ApiModelProperty("对象名称")
    private String objectName;
    /**
     * 对象类型
     **/
    @ApiModelProperty("对象类型")
    private Integer objectType;
    /**
     * 对象信息
     **/
    @ApiModelProperty("对象信息")
    private String objectInfo;
    /**
     * 来源
     **/
    @ApiModelProperty("来源")
    private Integer sourceType;
    /**
     * 备注
     **/
    @ApiModelProperty("备注")
    private String remark;
    /**
     * 创建时间
     **/
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 修改时间
     **/
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("被引用")
    @Transient
    private Integer beAdopted;
}
