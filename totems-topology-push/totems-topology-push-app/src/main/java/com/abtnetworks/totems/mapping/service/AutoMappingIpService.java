package com.abtnetworks.totems.mapping.service;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @desc    地址映射自动匹配接口
 * @author liuchanghao
 * @date 2022-01-21 10:34
 */
public interface AutoMappingIpService {

    /**
     * 分页查询列表
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushAutoMappingIpEntity> findList(AutoMappingIpSearchVO vo, int pageNum, int pageSize);

    /**
     * 根据条件查询IP匹配表
     * @param entity
     * @return
     */
    List<PushAutoMappingIpEntity> findIpMappingByEntity(PushAutoMappingIpEntity entity);

    List<PushAutoMappingIpEntity> findIpMappingByEntityAll(PushAutoMappingIpEntity entity);



    /**
     * 根据id批量删除
     * @param idList
     * @return
     * @throws Exception
     */
    int deleteIdList(List<Integer> idList) throws Exception;

    /**
     * 根据id查询
     * @param id
     * @return
     */
    PushAutoMappingIpEntity selectById(int id) ;

    /**
     * 新增或修改
     * @param entity
     * @return
     */
    TotemsReturnT addOrUpdate(PushAutoMappingIpEntity entity);

    /**
     * 批量新增
     * @param entitys
     * @return
     */
    int batchInsert(List<PushAutoMappingIpEntity> entitys);
}
