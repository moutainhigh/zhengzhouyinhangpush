package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.entity.DisposalWhiteListEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 16:29 2019/11/11
 */
@Mapper
@Repository
public interface DisposalWhiteListMapper {
    /**
     * 新增
     */
    public int insert(DisposalWhiteListEntity disposalWhiteList);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalWhiteListEntity disposalWhiteList);

    /**
     * get查询 By Id
     */
    public DisposalWhiteListEntity getById(@Param("id") Long id);

    /**
     * 根据名称查询白名单
     * @param name
     * @return
     */
    DisposalWhiteListEntity getByName(@Param("name") String name);

    DisposalWhiteListEntity getByNameNotId(@Param("name") String name, @Param("id") Long id);

    /**
     * get查询
     */
    List<DisposalWhiteListEntity> get(DisposalWhiteListEntity disposalWhiteList);

    /**
     * 查询 List Data
     */
    public List<DisposalWhiteListEntity> findList(@Param("type") Integer type,
                                                  @Param("name") String name,
                                                  @Param("content") String content);

    /**
     * 查询Count
     */
    public int count();




}
