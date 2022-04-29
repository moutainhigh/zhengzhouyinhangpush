package com.abtnetworks.totems.common.dto.commandline;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 路由命令行生成的请求对象
 * @author zc
 * @date 2019/11/15
 */
@Data
public class RoutingCommandDTO {

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("业务主题名称")
    private String description;

    @ApiModelProperty("是否为虚设备")
    private Boolean isVsys;

    @ApiModelProperty("虚设备名称")
    private String vsysName;

    @ApiModelProperty("ip范围")
    private String ipAddr;

    @ApiModelProperty("路由类型")
    private RoutingType routingType;

    public enum RoutingType {
        /**
         * 路由不可达（黑洞）
         */
        UNREACHABLE,
        /**
         * 静态路由
         */
        STATIC
    }

}
