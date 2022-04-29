package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
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
public interface ProtectNetworkConfigMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ProtectNetworkConfigEntity record);

    int insertSelective(ProtectNetworkConfigEntity record);

    ProtectNetworkConfigEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProtectNetworkConfigEntity record);

    int updateByPrimaryKey(ProtectNetworkConfigEntity record);

    /**
     * 查询防护网段列表
     * @param record
     * @return
     */
    List<ProtectNetworkConfigEntity> findList(ProtectNetworkConfigEntity record);

    /**
     * 根据uuid获取防护网段配置
     * @param uuid
     * @return
     */
    ProtectNetworkConfigEntity getByUuid(String uuid);

    /**
     * 根据设备uuid查询是否已存在配置
     * @param deviceUuid
     * @return
     */
    ProtectNetworkConfigEntity selectByDeviceUuid(String deviceUuid);

    /**
     * 查询所有的防护网段配置
     * @return
     */
    List<ProtectNetworkConfigEntity> findAll();

}