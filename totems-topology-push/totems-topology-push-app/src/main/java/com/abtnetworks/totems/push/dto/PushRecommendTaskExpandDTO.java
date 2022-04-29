package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.advanced.dto.SceneForFiveBalanceDTO;
import com.abtnetworks.totems.common.dto.commandline.PushPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.PushSnatPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;


/**
 * 工单策略拓展表生成表实体
 *
 * @author lifei
 * @since 2021年08月02日
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushRecommendTaskExpandDTO {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务表id")
    private Integer taskId;

    @ApiModelProperty("设备id")
    @NotNull(message = "设备id不能为空")
    @Length(max = 32 ,message = "设备id长度不超过32")
    private String deviceUuid;

    @ApiModelProperty("设备名称")
    @Length(max = 128 ,message = "设备名称长度不超过128")
    private String deviceName;

    @ApiModelProperty("工单号(Vs名称)")
    @Length(max = 64 ,message = "工单号长度不超过64")
    private String theme;

    @ApiModelProperty("场景uuid")
    private String sceneUuid;

    @ApiModelProperty("场景名称")
    private String sceneName;

    @ApiModelProperty("pool信息")
    private PushPoolInfo poolInfo;

    @ApiModelProperty("snat类型")
    private Integer snatType;

    @ApiModelProperty("snatPool信息")
    private PushSnatPoolInfo snatPoolInfo;

    @ApiModelProperty("源IP")
    private String srcIp;

    @ApiModelProperty("目的IP")
    private String dstIp;

    @ApiModelProperty("任务状态")
    private Integer taskStatus;

    @ApiModelProperty("http_profile")
    private String httpProfile;

    @ApiModelProperty("证书名称")
    private String sslProfile;

    @ApiModelProperty("任务类型 19：botnNat  18：dnat")
    private Integer taskType;

    @ApiModelProperty("服务信息")
    private ServiceDTO serviceInfo;

    @ApiModelProperty("备注")
    private String mark;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("页面大小")
    private Integer pageSize;

    @ApiModelProperty("当前页")
    private Integer currentPage;

    @ApiModelProperty("场景实体")
    private SceneForFiveBalanceDTO sceneForFiveBalanceDTO;
}