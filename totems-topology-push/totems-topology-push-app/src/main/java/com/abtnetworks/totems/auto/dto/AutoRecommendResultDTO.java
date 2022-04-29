package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @desc    自动开通任务结果报告页详情DTO
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Data
public class AutoRecommendResultDTO {

    @ApiModelProperty("设备IP")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("任务状态")
    private Integer demandStatus;

    @ApiModelProperty("安全策略-冲突策略集合")
    private Set<PolicyDetailVO> securityConflictPolicyDTOSet;

    @ApiModelProperty("nat策略-冲突策略集合")
    private Set<PolicyDetailVO> natConflictPolicyDTOSet;

    @ApiModelProperty("安全策略建议集合")
    private Set<RecommendTaskEntity> securityPolicyDTOSet;

    @ApiModelProperty("nat策略建议集合")
    private Set<RecommendTaskEntity> natPolicyDTOSet;

    @ApiModelProperty("命令行数据集合")
    private List<CommandTaskEditableEntity> commandTaskEditableList;

}
