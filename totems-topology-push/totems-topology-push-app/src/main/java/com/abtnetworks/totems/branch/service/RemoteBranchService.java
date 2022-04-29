package com.abtnetworks.totems.branch.service;

import com.abtnetworks.totems.branch.dto.Branch;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/16
 */
public interface RemoteBranchService {




    /**
     * 根据用户信息查询
     * @param id
     * @return
     */
    UserInfoDTO findOne(String id);

    /**
     * 模糊查询参数组织机构
     * @param userName
     * @return
     */
    String likeBranch(String userName);

    /**
     * 根据用户级别查询用组信息
     * @param level
     * @return
     */
    List<Branch> getBranchListByLevel( String level);

}
