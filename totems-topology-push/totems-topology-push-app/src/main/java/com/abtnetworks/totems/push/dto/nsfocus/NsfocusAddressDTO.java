package com.abtnetworks.totems.push.dto.nsfocus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/3/4
 **/
@ApiModel("绿盟地址对象DTO")
@Data
public class NsfocusAddressDTO {

    @ApiModelProperty("地址名称")
    String name;

    @ApiModelProperty("子网--(地址对象-子网)")
    String net;

    @ApiModelProperty("节点--(地址对象-节点)")
    String ip;

    @ApiModelProperty("域名--(地址对象-域名)")
    String url;

    @ApiModelProperty("起始ip--(地址对象-ip)")
    String beginIp;

    @ApiModelProperty("结束ip--(地址对象-ip)")
    String endIp;

    @ApiModelProperty("mac--(地址对象-mac)")
    String mac;

    @ApiModelProperty("地址组id(单个地址id逗号分隔),eg：12003,12004,12005 --(地址组对象)")
    String include;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    String ipType;

    @ApiModelProperty("类型 0：子网 ,1:节点(单ip),2:域名,3:网段(ip池),4地址组")
    Integer type;
}
