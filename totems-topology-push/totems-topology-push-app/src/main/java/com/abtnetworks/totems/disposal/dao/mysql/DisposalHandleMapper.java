package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.DisposalHandleListDTO;
import com.abtnetworks.totems.disposal.entity.DisposalHandleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:15 2019/11/12
 */
@Mapper
@Repository
public interface DisposalHandleMapper {

    /**
     * 新增
     */
     int insert(DisposalHandleEntity disposalHandle);

    /**
     * 删除
     */
     int delete(@Param("id") int id);

    /**
     * 更新
     */
     int update(DisposalHandleEntity disposalHandle);

    /**
     * get查询 By Id
     */
     DisposalHandleEntity getById(@Param("id") Long id);

    /**
     * get查询
     */
     DisposalHandleEntity get(DisposalHandleEntity disposalHandle);

    /**
     * 查询 List Data
     */
     List<DisposalHandleEntity> findList(DisposalHandleEntity disposalHandle);


    /**
     * 查询Count
     */
     int count();



    /**
     * 查询 List Data
     */
    List<DisposalHandleListDTO> findByCondition(@Param("category") Integer category, @Param("type") Integer type,
                                                @Param("status") Integer status, @Param("content") String content,
                                                @Param("callbackFlag") Boolean callbackFlag);


    /**
     * 查询Count
     */
    int findByConditionCount(@Param("category") Integer category, @Param("type") Integer type,
                             @Param("status") Integer status, @Param("content") String content,
                             @Param("callBackFlag") Boolean callBackFlag);

}

