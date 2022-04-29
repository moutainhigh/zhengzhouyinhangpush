package com.abtnetworks.totems.common.dto;

import lombok.Data;

/**
 * @author lifei
 * @desc 跳板机DTO
 * @date 2021/12/7 11:28
 */
@Data
public class JumpMachineDTO {
    /**
     * 跳板机ip
     */
    private String ip;

    /**
     * 跳板机端口
     */
    private String port;

    /**
     * 跳板机登陆名称
     */
    private String username;

    /**
     * 跳板机登陆密码
     */
    private String password;

    /**
     * 映射的本地端口
     */
    private String iPort;
}

