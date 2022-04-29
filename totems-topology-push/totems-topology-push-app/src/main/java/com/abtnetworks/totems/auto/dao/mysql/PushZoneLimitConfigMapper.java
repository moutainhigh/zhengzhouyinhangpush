package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.PushZoneLimitConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-11-16 11:16
 */
@Mapper
@Repository
public interface PushZoneLimitConfigMapper {

    /**
     * 根据ID删除
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 新增记录
     * @param record
     * @return
     */
    int insert(PushZoneLimitConfigEntity record);

    /**
     * 指定字段新增
     * @param record
     * @return
     */
    int insertSelective(PushZoneLimitConfigEntity record);

    /**
     * 根据id查询记录
     * @param id
     * @return
     */
    PushZoneLimitConfigEntity selectByPrimaryKey(Integer id);

    /**
     * 根据指定字段修改
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(PushZoneLimitConfigEntity record);

    /**
     * 根据id修改
     * @param record
     * @return
     */
    int updateByPrimaryKey(PushZoneLimitConfigEntity record);

    /**
     * 查询列表
     * @return
     */
    List<PushZoneLimitConfigEntity> findList();

    /**
     * 根据uuid查询列表
     * @param deviceUuid
     * @return
     */
    List<PushZoneLimitConfigEntity> findByDeviceUuid(String deviceUuid);
}