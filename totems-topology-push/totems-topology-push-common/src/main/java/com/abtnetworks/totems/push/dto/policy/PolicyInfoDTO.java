package com.abtnetworks.totems.push.dto.policy;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/30
 */
@Data
public class PolicyInfoDTO {
    private String srcIp;

    private String dstIp;

    List<ServiceDTO> serviceList;

    @ApiModelProperty("策略生成来源，0为Ip,1为域名或域名解析后的ip地址")
    private String policySource;
    @ApiModelProperty("ip类型")
    private Integer ipType;

    @ApiModelProperty("匹配到的DNAT名称")
    private String dnatName;

    @ApiModelProperty("匹配到的SNAT名称")
    private String snatName;

    @ApiModelProperty("whatIf场景Nat匹配类型 0.未匹配到 1.存量NAT匹配 2.whatIf Nat 匹配 3.存量和whatIf都存在，根据name前缀是否是whatIf_进行区分")
    private String matchType;

    @ApiModelProperty("匹配到的Nat策略的转换前的服务")
    private List<ServiceDTO> matchPreServices;

    @ApiModelProperty("匹配到的Nat策略的转换后的服务")
    private List<ServiceDTO> matchPostServices;

}
