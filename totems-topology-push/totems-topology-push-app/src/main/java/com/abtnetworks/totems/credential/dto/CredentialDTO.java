package com.abtnetworks.totems.credential.dto;

import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/8 13:08
 */
@Data
public class CredentialDTO {
    String id;

    String name;

    String uuid;

    String description;

    String loginName;

    String loginPassword;

    String enableUserName;

    String enablePassword;

    String version;

    String userName;
}
