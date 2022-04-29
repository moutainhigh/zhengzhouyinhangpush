package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.ProtectNetworkNatMappingEntity;
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
public interface ProtectNetworkNatMappingMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ProtectNetworkNatMappingEntity record);

    int insertSelective(ProtectNetworkNatMappingEntity record);

    ProtectNetworkNatMappingEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProtectNetworkNatMappingEntity record);

    int updateByPrimaryKey(ProtectNetworkNatMappingEntity record);

    /**
     * 根据配置id删除记录
     * @param configId
     * @return
     */
    int deleteByConfigId(Long configId);

    /**
     * 根据配置id查询nat映射关系
     * @param configId
     * @return
     */
    List<ProtectNetworkNatMappingEntity> selectByConfigId(Long configId);

    /**
     * 根据配置id查询nat映射关系
     * @param natType
     * @return
     */
    List<ProtectNetworkNatMappingEntity> selectByNatType(String natType);

    /**
     * 根据条件查询nat映射关系
     * @param record
     * @return
     */
    List<ProtectNetworkNatMappingEntity> selectConfigList(ProtectNetworkNatMappingEntity record);

}