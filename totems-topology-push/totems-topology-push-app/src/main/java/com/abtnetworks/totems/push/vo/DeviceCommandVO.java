package com.abtnetworks.totems.push.vo;

import lombok.Data;

@Data
public class DeviceCommandVO {
        String deviceName;

        String manageIp;

        String command;

        String pushResult;
}
