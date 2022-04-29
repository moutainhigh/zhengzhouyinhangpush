package com.abtnetworks.totems.retrieval.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxinghui
 * @date 2021/08/18
 */
@ApiModel("全局搜索：修改策略传递后台参数")
@Data
public class RetrievalParamDto {

    List<RetrievalPolicyDto> policy;

    List<RetrievalAddressDto> address;

    List<RetrievalServiceDto> service;

    public void addAddressDto(RetrievalAddressDto address){
        if(this.address==null){
            this.address = new ArrayList<>();
        }
        this.address.add(address);
    }

    public void addServiceDto(RetrievalServiceDto service){
        if(this.service==null){
            this.service = new ArrayList<>();
        }
        this.service.add(service);
    }

    public void addPolicyDto(RetrievalPolicyDto policy){
        if(this.policy==null){
            this.policy = new ArrayList<>();
        }
        this.policy.add(policy);
    }
}
