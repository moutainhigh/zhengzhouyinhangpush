package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.whale.policy.ro.DeviceRulesRO;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/6/29
 */
@Data
public class DeviceInfoDTO {
    String vendor;

    String model;

    String deviceFamily;

    String name;

    List<DeviceRulesRO> deviceRuleList;
}
