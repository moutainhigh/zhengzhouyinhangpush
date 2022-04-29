package com.abtnetworks.totems.mapping.dto;

import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @desc 地址池信息DTO
 * @date 2022/2/11 14:09
 */
@Data
public class AddressPoolDTO {

    @ApiModelProperty("所有地址池信息")
    private List<PushAutoMappingPoolEntity> ipPoolList;

    @ApiModelProperty("匹配上的地址池信息")
    private List<PushAutoMappingPoolEntity> matchPools;


}
