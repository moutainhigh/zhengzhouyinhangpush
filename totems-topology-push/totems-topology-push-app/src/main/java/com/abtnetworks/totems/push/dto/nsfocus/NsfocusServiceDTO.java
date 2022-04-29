package com.abtnetworks.totems.push.dto.nsfocus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/3/4
 **/
@ApiModel("绿盟服务对象DTO")
@Data
public class NsfocusServiceDTO {

    @ApiModelProperty("名称")
    String name;

    @ApiModelProperty("协议：tcp,udp,ip")
    String proto;

    @ApiModelProperty("源端口")
    String srcPorts;

    @ApiModelProperty("目的端口")
    String dstPorts;

    @ApiModelProperty("协议为ip时的协议类型")
    String ipProto;

}
