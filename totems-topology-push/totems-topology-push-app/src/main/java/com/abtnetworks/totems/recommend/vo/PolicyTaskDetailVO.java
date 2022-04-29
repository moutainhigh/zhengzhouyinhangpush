package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.Date;

@Data
@ApiModel("策略详情显示数据")
public class PolicyTaskDetailVO extends PolicyDetailVO {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务id")
    Integer taskId;

    @ApiModelProperty("设备IP")
    String deviceIp;

    @ApiModelProperty("创建人")
    String userName;

    @ApiModelProperty("创建时间")
    Date createTime;

    @ApiModelProperty("外部地址")
    String publicAddress;

    @ApiModelProperty("内部地址")
    String privateAddress;

    @ApiModelProperty("外部端口")
    String publicPort;

    @ApiModelProperty("内部端口")
    String privatePort;

    @ApiModelProperty("协议")
    String protocol;
    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("源地址所属系统")
    String srcIpSystem;

    @ApiModelProperty("目的地址所属系统")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后目的地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("下发状态")
    private Integer pushStatus;

    @ApiModelProperty("场景名称")
    private String scenesName;

    @ApiModelProperty("场景uuid")
    private String scenesUuid;

    @ApiModelProperty("所属虚拟路由器")
    private String srcVirtualRouter;

    @ApiModelProperty("目的虚拟路由器")
    private String dstVirtualRouter;

    @ApiModelProperty("出接口")
    private String outInterface;

    @ApiModelProperty("优先级")
    private String priority;

    @ApiModelProperty("管理距离")
    private String managementDistance;

    @ApiModelProperty("子网掩码")
    private Integer subnetMask;

    @ApiModelProperty("下一跳")
    private String nextHop;
}
