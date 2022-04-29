package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @Author hw
 * @Description
 * @Date 17:07 2019/11/11
 */
@Mapper
@Repository
public interface DisposalScenesMapper {

    /**
     * 新增
     */
    public int insert(DisposalScenesEntity disposalScenes);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalScenesEntity disposalScenes);

    /**
     * get查询 By Id
     */
    public DisposalScenesEntity getById(@Param("id") int id);


    DisposalScenesEntity getByNameNotId(@Param("name") String name, @Param("id") Long id);

    /**
     * 通过 ScenesName 获取场景信息Entity
     * @param name
     * @return
     */
    DisposalScenesEntity getByScenesName(@Param("name") String name);

    /**
     * get查询 By uuid
     * @param uuid
     * @return
     */
    public DisposalScenesEntity getByUUId(@Param("uuid") String uuid);

    /**
     * get查询
     */
    public DisposalScenesEntity get(DisposalScenesEntity disposalScenes);

    /**
     * 查询 List Data
     */
    public List<DisposalScenesEntity> findList(DisposalScenesEntity disposalScenes);

    /**
     * 查询Count
     */
    public int count();

}

