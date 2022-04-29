package com.abtnetworks.totems.push.dao.mysql;

import com.abtnetworks.totems.push.entity.PushPwdStrategyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * @Description
 * @Author guanduo.su
 * @Date: 2021/4/12 17:20
 **/
@Mapper
@Repository
public interface PushPwdStrategyMapper {

    PushPwdStrategyEntity fingPwdStrategy();

    void insert(PushPwdStrategyEntity pushPwdStrategyEntity);

    void upadtePushDeviceById(PushPwdStrategyEntity pushPwdStrategyEntity);

    void deletPushDeviceById(Integer id);

    PushPwdStrategyEntity fingCmdbDeviceById(Integer id);
}
