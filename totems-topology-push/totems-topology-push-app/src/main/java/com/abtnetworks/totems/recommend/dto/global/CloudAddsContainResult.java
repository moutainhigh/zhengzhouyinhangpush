package com.abtnetworks.totems.recommend.dto.global;


import com.abtnetworks.totems.common.enums.ContainType;
import lombok.Data;

import java.util.List;

//一组IP在云内查询的结果
@Data
public class CloudAddsContainResult {
    //src包含类型
    ContainType srcContainType;
    //dst包含类型
    ContainType dstContainType;
    //云内包含的srcip
    List<String> srcIncludeIps;
    //云内包含的dstip
    List<String> dstIncludeIps;
    //不在云内的srcip
    List<String> srcExcludeIps;
    //不在云内的dstip
    List<String> dstExcludeIps;
    //源/目的是否是多个云
    boolean multipleCloud;
}
