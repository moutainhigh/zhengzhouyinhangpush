package com.abtnetworks.totems.credential.dao.mysql;

import com.abtnetworks.totems.credential.dto.SearchCredentialByPageDTO;
import com.abtnetworks.totems.credential.dto.UpdateCredentialDTO;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 11:05
 */
@Mapper
@Repository
public interface CredentialMapper {

    /**
     * 创建认证信息
     *
     * @param entity 认证信息对象
     */
    void create(CredentialEntity entity);

    /**
     * 删除认证信息
     *
     * @param uuid 认证信息uuid
     */
    void delete(String uuid);

    /**
     * 修改认证信息
     *
     * @param entity 认证信息对象
     */
    void modify(CredentialEntity entity);

    /**
     * 获取认证信息对象
     *
     * @param uuid 认证信息对象uuid
     * @return 认证信息对象
     */
    List<CredentialEntity> get(String uuid);

    /**
     * 根据uuid查询凭证信息
     *
     * @param uuid 认证信息对象uuid
     * @return 认证信息对象，返回1个
     */
    CredentialEntity getByUuid(String uuid);

    /**
     * 根据name查询凭证信息
     *
     * @param name 凭据名称
     * @return 认证信息对象，返回1个
     */
    CredentialEntity getByName(String name);

    /**
     * 查询参数
     *
     * @param searchCredentialByPageDTO
     * @return
     */
    List<CredentialEntity> listByParam(SearchCredentialByPageDTO searchCredentialByPageDTO);

    /**
     * 批量修改
     * @param updateCredentialDTOS
     *
     * @return
     */
    int modifyBatchBranch(@Param("updateCredentialDTOS") List<UpdateCredentialDTO> updateCredentialDTOS);

    /**
     * 获取total
     * @param searchCredentialByPageDTO
     * @return
     */
    Integer listByParamCount(SearchCredentialByPageDTO searchCredentialByPageDTO);

    /**
     * 根据id修改
     * @param entity
     * @return
     */
    Integer modifyById(CredentialEntity entity);

}
