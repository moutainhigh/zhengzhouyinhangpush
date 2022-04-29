package com.abtnetworks.totems.auto.vo;

import com.abtnetworks.totems.auto.dto.AddressCommandTaskEditableDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description 对象管理下发任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
@Data
@ApiModel("对象管理任务表")
public class AddressManagePushVO {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty("下发id集合")
    private List<Integer> idList;

    @ApiModelProperty(value = "对象管理任务id")
    private Integer taskId;

    @ApiModelProperty(value = "基线地址名称")
    private String addressName;

    @ApiModelProperty(value = "场景id")
    private String scenesUuid;

    @ApiModelProperty(value = "场景名称")
    private String scenesName;

    @ApiModelProperty(value = "设备id")
    private String deviceUuid;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "删除值")
    private String addressDel;

    @ApiModelProperty(value = "新增值")
    private String addressAdd;

    @ApiModelProperty(value = "立即采集 true:立即采集,false:不立即采集")
    private Boolean gatherNow;

    @ApiModelProperty(value = "命令行脚本对象")
    private AddressCommandTaskEditableDTO commandTaskEditableEntity;
}
