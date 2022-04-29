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
 * 静态路由生成表实体
 *
 * @author lb
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushRecommendStaticRoutingDTO {

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

    @ApiModelProperty("设备ip")
    private String deviceIp;

    @ApiModelProperty("工单号(名称)")
    @Length(max = 64 ,message = "工单号长度不超过64")
    private String theme;

    @ApiModelProperty("任务状态")
    private Integer taskStatus;

    @ApiModelProperty("任务类型 20:静态路由")
    private Integer taskType;

    @ApiModelProperty("所属虚拟路由器")
    private String srcVirtualRouter;

    @ApiModelProperty("目的虚拟路由器")
    private String dstVirtualRouter;

    @ApiModelProperty("出接口")
    private String outInterface;

    @ApiModelProperty("ip类型 0:ipv4 ,1:ipv6")
    private Integer ipType;

    @ApiModelProperty("目的IP")
    @NotNull(message = "目的地址不能为空")
    private String dstIp;

    @ApiModelProperty("子网掩码")
    @NotNull(message = "子网掩码不能为空")
    private Integer subnetMask;

    @ApiModelProperty("下一跳")
    private String nextHop;

    @ApiModelProperty("优先级")
    private String priority;

    @ApiModelProperty("管理距离")
    private String managementDistance;

    @ApiModelProperty("备注")
    private String mark;

    @ApiModelProperty("批量开通任务id")
    private Integer batchId;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("页面大小")
    private Integer pageSize;

    @ApiModelProperty("当前页")
    private Integer currentPage;

}