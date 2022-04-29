package com.abtnetworks.totems.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;

import java.util.Date;

@Data
@ApiModel("批量管理任务显示对象")
public class BatchTaskVO {

    @ApiModelProperty("批量工单id")
    Integer id;

    @ApiModelProperty("主题")
    String theme;

    @ApiModelProperty("任务数")
    Integer count;

    @ApiModelProperty("任务状态")
    Integer status;

    @ApiModelProperty("申请人")
    String userName;

    @ApiModelProperty("创建时间")
    Date createTime;

    @ApiModelProperty("任务开始时间")
    Date taskStart;

    @ApiModelProperty("任务结束时间")
    Date taskEnd;

    @ApiModelProperty("任务类型")
    Integer type;

    @ApiModelProperty("任务状态")
    String result;

    @ApiModelProperty("任务持续时间")
    String duration;

    @ApiModelProperty("包含工单Id列表")
    String taskIds;

    @ApiModelProperty("未开始工单")
    Integer notStart;

    @ApiModelProperty("等待中")
    Integer waiting;

    @ApiModelProperty("执行中")
    Integer running;

    @ApiModelProperty("仿真已完成")
    Integer analyzed;
}

