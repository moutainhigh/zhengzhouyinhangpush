package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @desc    自动开通任务结果根据设备获取策略建议和命令行数据DTO
 *          包含异常信息提示和相关策略建议、命令行等信息
 * @author liuchanghao
 * @date 2021-12-09 16:04
 */
@Data
public class AutoRecommendGetByDeviceResultDTO {

    @ApiModelProperty("安全策略建议集合")
    private Set<RecommendTaskEntity> securityPolicyDTOSet;

    @ApiModelProperty("nat策略建议集合")
    private Set<RecommendTaskEntity> natPolicyDTOSet;

    @ApiModelProperty("命令行数据集合")
    private List<CommandTaskEditableEntity> commandTaskEditableList;

}
