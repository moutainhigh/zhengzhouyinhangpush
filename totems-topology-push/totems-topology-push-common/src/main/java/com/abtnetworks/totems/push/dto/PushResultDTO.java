package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.common.enums.SendErrorEnum;
import lombok.Data;

@Data
public class PushResultDTO {

    /**
     * 下发结果
     */
    int result;

    /**
     * 命令行回显
     */
    String cmdEcho;
    /**
     * 错误信息提示
     */
    SendErrorEnum sendErrorEnum;
}
