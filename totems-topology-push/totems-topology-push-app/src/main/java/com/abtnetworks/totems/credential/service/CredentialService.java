package com.abtnetworks.totems.credential.service;

import com.abtnetworks.totems.credential.dto.SearchCredentialByPageDTO;
import com.abtnetworks.totems.credential.dto.UpdateCredentialDTO;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.credential.vo.CredentialVO;
import com.abtnetworks.totems.whale.baseapi.ro.CredentialResultRO;
import com.github.pagehelper.PageInfo;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/8 11:26
 */
public interface CredentialService {

    /**
     * 创建认证对象
     *
     * @param name           名称
     * @param description    描述
     * @param loginName      用户名
     * @param loginPassword  密码
     * @param enableUserName 启用用户名
     * @param enablePassword 启用密码
     * @return 创建认证对象结果对象
     */
    int create(String name, String description, String loginName, String loginPassword,
                              String enableUserName, String enablePassword,String userName, Boolean encrypt);


    /**
     * 修改认证对象
     *
     * @param id             认证对象id
     * @param uuid           认证对象uuid
     * @param name           名称
     * @param description    描述
     * @param loginName      用户名
     * @param loginPassword  密码
     * @param enableUserName 启用用户名
     * @param enablePassword 启用密码
     * @return 修改认证对象结果对象
     */
    int modify(String id, String uuid, String name, String description, String loginName,
                              String loginPassword, String enableUserName, String enablePassword, String version,String userName, Boolean encrypt);

    /**
     * 删除认证对象
     *
     * @param uuid 认证对象uuid
     * @return 删除认证对象结果对象
     */
    int delete(String uuid);

    /**
     * 获取认证对象
     * @param uuid 认证对象uuid
     * @return 认证对象
     */
    CredentialResultRO get(String uuid);

    /**
     * 获取认证对象 从数据库获取
     * @param uuid 认证对象uuid
     * @return 认证对象
     */
    CredentialEntity getFromMysql(String uuid);

    /**
     * 获取所有认证对象
     * @return 认证对象列表
     */
    PageInfo<CredentialVO> getAll(SearchCredentialByPageDTO searchCredentialByPageDTO, String userName);

    /**
     * 批量修改
     * @param updateCredentialDTO
     * @param authentication
     * @return
     */
    int batchModify(UpdateCredentialDTO updateCredentialDTO, Authentication authentication) throws IllegalAccessException;


    /**
     * 获取凭据信息List
     * @param userName
     * @return
     */
    List<CredentialEntity> getCredentialList(String userName);
}
