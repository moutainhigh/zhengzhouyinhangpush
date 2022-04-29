package com.abtnetworks.totems.recommend.dto.recommend;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.whale.policy.dto.IpTermsDTO;
import com.abtnetworks.totems.whale.policy.dto.JsonQueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/14
 */
@ApiModel("设备五元组信息")
@Data
public class IpTermsExtendDTO extends IpTermsDTO {


    @ApiModelProperty("服务")
    private List<ServiceDTO> services;
    @ApiModelProperty("jsonQuery中域的，时间对象")
    JsonQueryDTO jsonQuery;
    @ApiModelProperty("是否跳过any")
    private Boolean skipAny;
    @ApiModelProperty("策略查询类型，4是acl策略")
    private Integer policyType;
}
