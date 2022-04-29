package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Mapper
@Repository
public interface ProtectNetworkConfigDetailMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ProtectNetworkConfigDetailEntity record);

    int insertSelective(ProtectNetworkConfigDetailEntity record);

    ProtectNetworkConfigDetailEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProtectNetworkConfigDetailEntity record);

    int updateByPrimaryKey(ProtectNetworkConfigDetailEntity record);

    /**
     * 根据防护网段配置ID删除
     * @param configId
     * @return
     */
    int deleteByConfigId(Long configId);

    /**
     * 查询防护网段配置列表
     * @param record
     * @return
     */
    List<ProtectNetworkConfigDetailEntity> findList(ProtectNetworkConfigDetailEntity record);

    /**
     * 根据配置ID查询
     * @param configId
     * @return
     */
    List<ProtectNetworkConfigDetailEntity> findByConfigId(Long configId);
}