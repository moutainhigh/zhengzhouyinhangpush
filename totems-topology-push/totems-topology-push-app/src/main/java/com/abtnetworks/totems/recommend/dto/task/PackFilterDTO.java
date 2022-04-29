package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 路径分析中入参五元组
 * @date 2021/1/6
 */
@Data
public class PackFilterDTO {
    /**源地址数组***/
    private List<String> srcIpList;
    /**目的地址数组***/
    private List<String> dstIpList;
    /**服务数组***/
    private List<ServiceDTO> serviceList;
    /**ip类型  0：ipv4; 1:ipv6; 2:url***/
    private Integer ipType;
}
