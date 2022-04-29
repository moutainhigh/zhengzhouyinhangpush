package com.abtnetworks.totems.push.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/5 11:20
 */
@Data
public class PushTaskEntity {

    private int id;

    private int policyId;

    private String orderNo;

    private int orderType;

    private String deviceUuid;

    private String deviceName;

    private String manageIp;

    private Date createTime;

    private Date pushTime;

    private int status;

    private String userName;

    private String command;

    private String pushResult;
}
