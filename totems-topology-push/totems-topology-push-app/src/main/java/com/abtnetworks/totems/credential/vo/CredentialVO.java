package com.abtnetworks.totems.credential.vo;

import com.abtnetworks.totems.common.entity.NodeEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/25
 */
@Data
public class CredentialVO {

    private String id;

    private String uuid;

    private String name;

    private String loginName;

    private String loginPassword;

    private String enableUserName;

    private String enablePassword;

    private String branchLevel;

    private String branchName;

    private List<NodeEntity> nodeList;
}
