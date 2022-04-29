package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 对象管理地址条目表
 * @Version --
 * @Created by zhoumuhua on '2021-10-28'.
 */
@Mapper
@Repository
public interface AddressDetailEntryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AddressDetailEntryEntity record);

    int insertSelective(AddressDetailEntryEntity record);

    AddressDetailEntryEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AddressDetailEntryEntity record);

    int updateByPrimaryKey(AddressDetailEntryEntity record);

    List<AddressDetailEntryEntity> findList(AddressDetailEntryEntity addressDetailEntryEntity);

    /**
     * get查询by detailid
     */
    List<AddressDetailEntryEntity> getByDetailId(@Param("detailId") Integer detailId);

    /**
     * get查询by detailid
     */
    List<AddressDetailEntryEntity> getByTaskId(@Param("taskId") Integer taskId);

    /**
     * 通过父级id删除详情
     */
    int deleteByDetailId(@Param("detailId") Integer detailId);
}