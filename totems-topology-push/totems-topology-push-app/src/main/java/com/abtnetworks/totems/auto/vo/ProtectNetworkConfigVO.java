package com.abtnetworks.totems.auto.vo;

import com.abtnetworks.totems.auto.dto.AutoRecommendNatMappingDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-10 15:52
 */
@Data
@ApiModel("防护网段配置实体类")
public class ProtectNetworkConfigVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("防火墙IP")
    @NotNull(message = "防火墙ip不能为空")
    private String deviceIp;

    @ApiModelProperty("设备uuid")
    @NotNull(message = "防火墙uuid不能为空")
    private String deviceUuid;

    @ApiModelProperty("设备名称")
    @NotNull(message = "防火墙名称不能为空")
    private String deviceName;

    @ApiModelProperty("防护网段")
    @NotNull(message = "防护网段不能为空")
    private String protectNetwork;

    @ApiModelProperty("是否打开Nat映射（Y：打开；N：未打开）")
    private String natFlag;

    @ApiModelProperty("Nat映射关系集合")
    private List<AutoRecommendNatMappingDTO> natMappingDTOList;

    @ApiModelProperty("主键id集合")
    private List<Long> idList;

    @ApiModelProperty("是否开启同域不开通逻辑（Y：打开；N：未打开）")
    private String sameZoneFlag;


}