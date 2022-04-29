package com.abtnetworks.totems.push.dao.mysql;


import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-09-10 18:48
 */
@Mapper
@Repository
public interface PushForbidIpMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(PushForbidIpEntity record);

    int insertSelective(PushForbidIpEntity record);

    PushForbidIpEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushForbidIpEntity record);

    int updateByPrimaryKey(PushForbidIpEntity record);

    /**
     * 查询封禁IP列表
     * @param record
     * @return
     */
    List<PushForbidIpEntity> findList(PushForbidIpEntity record);

    PushForbidIpEntity getByUuid(String uuid);

    int updateStatusByUuid(@Param("uuid") String uuid, @Param("status") int status);

    /**
     * 根据固定字符串DI+日期查询当天流水记录
     * @param dateString
     * @return
     */
    List<PushForbidIpEntity> findSerialNumber(String dateString);

}