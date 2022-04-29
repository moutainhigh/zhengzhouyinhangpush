package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/8
 */
@ApiModel("新增和修改关键信息")
@Data
public class PushMappingNatDTO {
    @ApiModelProperty("主键")
    private Integer id;
    @NotNull(message = "设备uuid不能为空")
    @Length(max = 32 ,message = "设备uuid长度不超过32")
    @ApiModelProperty("设备uuid")
    private String deviceUuid;
    @ApiModelProperty("转前规格描述")
    @Length(max = 225 ,message = "转换前规格描述长度不超过225")
    private String preStandardDesc;
    @Length(max = 225 ,message = "转换前地址长度不超过225")
    @ApiModelProperty("转前ip")
    private String preIp;
    @Length(max = 225 ,message = "转换后规格描述长度不超过225")
    @ApiModelProperty("转后规格描述")
    private String postStandardDesc;
    @Length(max = 225 ,message = "转换后地址长度不超过225")
    @ApiModelProperty("转后地址")
    private String postIp;
    @ApiModelProperty("源域")
    String srcZone;
    @ApiModelProperty("目的域")
    String dstZone;
    @ApiModelProperty("入接口")
    String inDevIf;
    @ApiModelProperty("出接口")
    String outDevIf;
    @Length(max = 225 ,message = "创建用户长度不超过225")
    @ApiModelProperty("创建用户")
    private String createUser;

    @Length(max = 225 ,message = "备注长度不超过225")
    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("厂商名称")
    private String deviceVendorName;





}
