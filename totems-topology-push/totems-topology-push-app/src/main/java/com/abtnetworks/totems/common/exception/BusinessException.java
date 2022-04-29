package com.abtnetworks.totems.common.exception;

import com.abtnetworks.totems.common.enums.SendErrorEnum;

/**
 * @author lifei
 * @desc 业务异常定义类
 * @date 2022/2/24 16:54
 */
public class BusinessException extends RuntimeException {
    private SendErrorEnum sendErrorEnum;

    public BusinessException() {
        super("运行时异常");
    }

    public BusinessException(SendErrorEnum sendErrorEnum) {
        super(sendErrorEnum.getMessage());
        this.sendErrorEnum = sendErrorEnum;
    }

    public SendErrorEnum getSendErrorEnum() {
        return sendErrorEnum;
    }
}
