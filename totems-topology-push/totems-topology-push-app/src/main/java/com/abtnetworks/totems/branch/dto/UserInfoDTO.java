package com.abtnetworks.totems.branch.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/19
 */
@Data
public class UserInfoDTO {

    private Date createdTime;

    private Date modifiedTime;

    private String createdUser;

    private String modifiedUser;

    private String id;

    private String name;

    private String email;

    private String note;

    private String enabled;

    private String orgId;


    private String roleUuid;


    private String branchLevel;





}
