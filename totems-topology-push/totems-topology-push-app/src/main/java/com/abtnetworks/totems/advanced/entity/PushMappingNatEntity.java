package com.abtnetworks.totems.advanced.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.Date;

/**
 * @author Administrator
 * @Title:
 * @Description: Nat自动映射关联实体
 * @date 2021/3/8
 */
@ApiModel("Nat自动映射关联实体")
@Data
public class PushMappingNatEntity {
    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("设备uuid")
    private String deviceUuid;
    @ApiModelProperty("转前规格描述")
    private String preStandardDesc;
    @ApiModelProperty("转前ip")
    private String preIp;
    @ApiModelProperty("转后规格描述")
    private String postStandardDesc;
    @ApiModelProperty("转后地址")
    private String postIp;
    @ApiModelProperty("创建用户")
    private String createUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("修改时间")
    private Date updateTime;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("源域")
    String srcZone;
    @ApiModelProperty("目的域")
    String dstZone;
    @ApiModelProperty("入接口")
    String inDevIf;
    @ApiModelProperty("出接口")
    String outDevIf;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("厂商名称")
    private String deviceVendorName;
}
