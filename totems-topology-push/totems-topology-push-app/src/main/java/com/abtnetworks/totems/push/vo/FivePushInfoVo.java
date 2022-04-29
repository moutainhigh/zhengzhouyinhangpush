package com.abtnetworks.totems.push.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author lifei
 * @desc F5 负载均衡设备pool名称
 * @date 2021/8/2 15:12
 */
@Data
public class FivePushInfoVo {

    @ApiModelProperty("默认pool池名称")
    private List<String>  poolName;

    @ApiModelProperty("snatPool池名称")
    private Map<String,List<String>>  snatPoolName;

    @ApiModelProperty("httpProfileNames")
    private List<String>  httpProfileNames;

    @ApiModelProperty("sslProfile 证书名称")
    private List<String>  sslProfileNames;

}
