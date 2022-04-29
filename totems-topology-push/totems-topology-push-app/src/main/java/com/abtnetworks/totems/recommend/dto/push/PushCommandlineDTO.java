package com.abtnetworks.totems.recommend.dto.push;

import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/7 17:52
 */
@Data
public class PushCommandlineDTO {
    int policyId;

    String modelNumber;

    String name;

    String description;

    String deviceName;

    String commandline;

    String username;

    String password;

    String enableUsername;

    String enablePassword;

    String deviceManagerIp;

    int status;
}
