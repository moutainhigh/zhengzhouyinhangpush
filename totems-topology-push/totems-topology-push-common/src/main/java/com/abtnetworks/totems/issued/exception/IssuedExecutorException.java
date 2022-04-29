package com.abtnetworks.totems.issued.exception;

import com.abtnetworks.totems.common.enums.SendErrorEnum;

/**
 * @author zakyoung
 * @Title:
 * @Description: 执行命令行之后业务异常
 * @date 2020-03-17
 */
public class IssuedExecutorException extends RuntimeException {
    private SendErrorEnum sendErrorEnum;

    public IssuedExecutorException() {
        super("运行时异常");
    }

    public IssuedExecutorException(SendErrorEnum sendErrorEnum) {
        super(sendErrorEnum.getMessage());
        this.sendErrorEnum = sendErrorEnum;
    }

    public SendErrorEnum getSendErrorEnum() {
        return sendErrorEnum;
    }
}
