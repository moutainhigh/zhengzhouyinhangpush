package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.recommend.dto.recommend.RecommendRelevanceSceneDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;



/**
* 仿真关联场景实体实体
*
* @author Administrator
* @since 2021年12月24日
*/
@Data
@ApiModel("工单特例关联场景对象")
public class RecommendRelevanceSceneEntity{

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务id")
    private String taskId;

    @ApiModelProperty("场景名称")
    private String name;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("分组")
    private String branchLevel;

    @ApiModelProperty("服务组")
    private String serviceList;

    @ApiModelProperty("附加信息")
    private String additionInfo;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date updateTime;


    /**
     * 将dto转entity
     * @param sceneDTO
     * @return
     */
    public static  RecommendRelevanceSceneEntity convertEntity(RecommendRelevanceSceneDTO sceneDTO){
        RecommendRelevanceSceneEntity entity = new RecommendRelevanceSceneEntity();
        entity.setId(sceneDTO.getId());
        entity.setTaskId(sceneDTO.getTaskId());
        entity.setName(sceneDTO.getName());
        entity.setSrcIp(sceneDTO.getSrcIp());
        entity.setDstIp(sceneDTO.getDstIp());
        entity.setIpType(sceneDTO.getIpType());
        entity.setServiceList(sceneDTO.getServiceListJson());
        entity.setAdditionInfo(sceneDTO.getAdditionInfo());
        entity.setCreateUser(sceneDTO.getCreateUser());
        entity.setCreateTime(sceneDTO.getCreateTime());
        entity.setUpdateTime(sceneDTO.getUpdateTime());
        return entity;
    }
}