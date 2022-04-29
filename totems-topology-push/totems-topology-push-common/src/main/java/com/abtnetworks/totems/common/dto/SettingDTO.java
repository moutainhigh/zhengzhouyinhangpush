package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("高级设置数据对象")
public class SettingDTO {

    @ApiModelProperty("是否创建对象")
    boolean isCreateObject;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("移动相关位置ID")
    String swapNameId;

    @ApiModelProperty("策略名称")
    String policyName;

    @ApiModelProperty("策略ID")
    String policyId;

    @ApiModelProperty("Cisco设备是否在接口上创建策略集")
    boolean isCreateCiscoItfRuleList;

    @ApiModelProperty("Cisco接口上策略集名称")
    String ciscoItfRuleListName;

    @ApiModelProperty("经过业务处理，接口最后挂在out方向")
    boolean outBound = false;

    @ApiModelProperty("是否复用地址对象")
    boolean enableAddressObjectSearch = true;

    @ApiModelProperty("是否复用服务对象")
    boolean enableServiceObjectSearch = true;

    @ApiModelProperty("天融信策略分组名称")
    String groupName;
    @ApiModelProperty("checkPoint网络分层名称")
    String layerName;
    @ApiModelProperty("checkPoint策略包名称")
    String policyPackage;

    @ApiModelProperty("山石回滚命令行根据策略名称还是策略id，true：策略名称，false：策略id")
    Boolean rollbackType = true;

    @ApiModelProperty("juniper生成地址对象是根据安全域还是全局地址，true：安全域，false：全局地址")
    Boolean addressType = true;

    @ApiModelProperty("可用ruleId集合")
    List<Integer> usableRuleList;

    @ApiModelProperty("h3v7策略地址组使用id")
    String h3v7addressGroupId;

    @ApiModelProperty("策略名称+随机数")
    String randomNumberString;

}
