package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author luwei
 * @date 2019/11/22
 */
@ApiModel(value = "白名单保存")
@Data
public class DisposalWhiteSaveDTO {

    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 白名单名称
     */
    private String name;

    /**
     * 类型：0策略、1路由
     */
    private Integer type;

    /**
     * 源IP，可填多个，逗号隔开
     */
    private String srcIp;

    /**
     * 目的地址，可填多个，逗号隔开
     */
    private String dstIp;

    /**
     * 路由IP，可填多个，逗号隔开
     */
    private String routingIp;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建人员
     */
    private String createUser;

    private String modifiedUser;

    /**
     * 是否 ipv6
     */
    private boolean ipv6;

    public boolean getIpv6() {
        return ipv6;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    @ApiModelProperty(value = "协议+端口集合")
    private List<ServiceDTO> serviceList;
}
