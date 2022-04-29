package com.abtnetworks.totems.push.dao.mysql;

import com.abtnetworks.totems.push.dto.ForbidCommandLineDTO;
import com.abtnetworks.totems.push.entity.PushForbidCommandLineEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PushForbidCommandLineMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(PushForbidCommandLineEntity record);

    int insertSelective(PushForbidCommandLineEntity record);

    PushForbidCommandLineEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushForbidCommandLineEntity record);

    int updateByPrimaryKey(PushForbidCommandLineEntity record);

    PushForbidCommandLineEntity getLastSuccessByUuid(@Param("forBidUuid") String forBidUuid,
                                                           @Param("deviceUuid") String deviceUuid,
                                                           @Param("pushStatus") Integer pushStatus);



    List<ForbidCommandLineDTO> getLastListByUuid(@Param("forBidUuid") String forBidUuid);


    List<PushForbidCommandLineEntity> getWaitSendRecordByUuid(@Param("forBidUuid") String forBidUuid);
}