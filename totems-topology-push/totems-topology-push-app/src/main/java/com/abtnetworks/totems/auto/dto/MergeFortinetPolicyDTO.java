package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    抽离出的飞塔目的NAT合并信息DTO
 * @author liuchanghao
 * @date 2021-12-14 14:48
 */
@Data
public class MergeFortinetPolicyDTO {

    @ApiModelProperty("策略信息")
    private RecommendPolicyDTO policyDTO;

    @ApiModelProperty("设备信息")
    private NodeEntity nodeEntity;

    @ApiModelProperty("自动开通工单信息")
    private AutoRecommendTaskVO vo;

    @ApiModelProperty("自动开通工单实体")
    private AutoRecommendTaskEntity record;

    @ApiModelProperty("关联策略生成里面的数据")
    private JSONArray taskJsonArray;

}
