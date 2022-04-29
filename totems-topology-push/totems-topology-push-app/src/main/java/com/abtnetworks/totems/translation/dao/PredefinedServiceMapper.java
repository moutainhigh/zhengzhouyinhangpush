package com.abtnetworks.totems.translation.dao;

import com.abtnetworks.totems.translation.entity.PredefinedService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: WangCan
 * @Description 预定义服务对象mapper
 * @Date: 2021/8/27
 */
@Mapper
@Repository
public interface PredefinedServiceMapper {

    void insert(PredefinedService predefinedService);

    void update(PredefinedService predefinedService);

    void insertList(List<PredefinedService> predefinedServiceList);

    PredefinedService getById(Integer id);

    List<PredefinedService> findByProtocolAndPort(@Param("protocol") String protocol,@Param("port") String port);

    List<PredefinedService> find(@Param("protocol") String protocol,@Param("sourcePortType") String sourcePortType,@Param("destinationPortCode") String destinationPortCode,@Param("port") String port);

    List<PredefinedService> findAll();

    void updateVenderObjectNameById(@Param("id") Integer id ,@Param("venderObjName") String venderObjName);

}
