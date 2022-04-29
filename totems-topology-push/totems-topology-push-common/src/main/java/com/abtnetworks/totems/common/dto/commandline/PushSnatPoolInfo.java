package com.abtnetworks.totems.common.dto.commandline;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author lifei
 * @desc 下发snatpool信息(针对于f5设备)
 * @date 2021/8/8 15:35
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushSnatPoolInfo {

    /**
     * pool名称
     */
    private String name;

    /**
     * snatPoolIp  可能有多个用逗号分隔
     */
    private String snatPoolIp;

    /**
     * 是否被引用（true 表示直接从墙上面选择 snatpool对象名称。 false 则新建）
     */
    private boolean quote;

}
