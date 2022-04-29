package com.abtnetworks.totems.mapping.dto;

import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

/**
 * @desc    规则流程DTO
 * @author liuchanghao
 * @date 2022-01-20 17:19
 */
@Data
public class RuleProcessDTO {

    @ApiModelProperty("当前要执行的规则任务类型")
    private RuleTypeTaskEnum ruleTypeTaskEnum;

    @ApiModelProperty("地址池转换前IP")
    private String ipPoolPreIp;

    @ApiModelProperty("地址池转换后IP")
    private String ipPoolPostIp;

    @ApiModelProperty("当前取的IP")
    private String currentIp;

    @ApiModelProperty("工单源地址")
    private String srcIp;

    @ApiModelProperty("工单目的地址")
    private String dstIp;

    @ApiModelProperty("工单的服务")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("工单主题")
    private String theme;

    @ApiModelProperty("需要创建的nat数据")
    private Map<String,String> natMap;

    @ApiModelProperty("上下文,含用户信息")
    private Authentication auth;

    @ApiModelProperty("地址池信息DTO")
    private AddressPoolDTO addressPoolDTO = new AddressPoolDTO();
}
