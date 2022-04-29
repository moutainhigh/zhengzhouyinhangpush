package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.dto.commandline.PushPoolInfo;
import com.abtnetworks.totems.common.dto.commandline.PushSnatPoolInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 负载均衡设备信息命令行所需数据
 * @date 2021/8/3 11:13
 */
@Data
public class CommandLineBalanceInfoDTO {

    @ApiModelProperty("snatPool信息")
    private PushSnatPoolInfo snatPoolInfo;

    @ApiModelProperty("pool信息")
    private PushPoolInfo poolInfo;

    @ApiModelProperty("场景名称")
    private String sceneName;

    @ApiModelProperty("应用发布类型")
    private String applyType;

    @ApiModelProperty("节点负载模式")
    private String loadBlanaceMode;

    @ApiModelProperty("节点回话保持")
    private String persist;

    @ApiModelProperty("健康检查")
    private String monitor;

    @ApiModelProperty("http_profile")
    private String httpProfile;

    @ApiModelProperty("证书名称")
    private String sslProfile;

    @ApiModelProperty("snat类型")
    private String snatType;
}
