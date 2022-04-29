package com.abtnetworks.totems.mapping.service;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingRouteEntity;
import com.abtnetworks.totems.mapping.vo.AutoMappingRouteSearchVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @desc    静态路由接口
 * @author liuchanghao
 * @date 2022-01-21 10:34
 */
public interface AutoMappingRouteService {

    /**
     * 添加或修改静态路由
     * @param routeEntity
     * @return
     * @throws Exception
     */
    TotemsReturnT addOrUpdate(PushAutoMappingRouteEntity routeEntity) throws Exception;

    /**
     * 分页查询
     * @param vo
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushAutoMappingRouteEntity> findList(AutoMappingRouteSearchVO vo, int pageNum, int pageSize);

    /**
     * 删除
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
    PushAutoMappingRouteEntity selectById(int id) ;


}
