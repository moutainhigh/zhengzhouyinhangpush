package com.abtnetworks.totems.recommend.dto.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.Authentication;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/20
 */
@ApiModel("仿真搜索条件")
@Data
public class SearchRecommendTaskDTO {

    public SearchRecommendTaskDTO(){};

    public SearchRecommendTaskDTO(String id, String batchId, String theme, String orderNumber, String userName, String description, String remarks, String srcIp, String dstIp, String protocol, String dstPort, String status, Integer page, Integer pSize, Boolean isServiceAny, Authentication authentication, Integer taskType) {
        this.id = id;
        this.batchId = batchId;
        this.theme = theme;
        this.orderNumber = orderNumber;
        this.userName = userName;
        this.description = description;
        this.remarks = remarks;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.protocol = protocol;
        this.dstPort = dstPort;
        this.status = status;
        this.page = page;
        this.pSize = pSize;
        this.isServiceAny = isServiceAny;
        this.authentication = authentication;
        this.taskType = taskType;
    }
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("批量id")
    private String batchId;
    @ApiModelProperty("主题")
    private String theme;
    @ApiModelProperty("工单号")
    private String orderNumber;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("策略描述")
    private String description;
    @ApiModelProperty("工单备注")
    private String remarks;
    @ApiModelProperty("源地址")
    private String srcIp;
    @ApiModelProperty("目的地之")
    private String dstIp;
    @ApiModelProperty("协议")
    private String protocol;
    @ApiModelProperty("目的端口")
    private String dstPort;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("当前页")
    private Integer page;
    @ApiModelProperty("页面大小")
    private Integer pSize;
    @ApiModelProperty("是否服务any")
    private Boolean isServiceAny;

    private Authentication authentication;

    private Integer taskType;
}
