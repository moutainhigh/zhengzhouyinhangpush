package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.whale.baseapi.ro.SearchLabelResultRO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/20
 */
@ApiModel("标签返回结果")
@Data
public class RecommendLabelVO {
    @ApiModelProperty("标签未选内容")
    private List<SearchLabelResultRO> searchLabelResultROList;
}
