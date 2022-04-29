package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.entity.DisposalScenesNodeEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 17:27 2019/11/11
 */
public interface DisposalScenesService {

    /**
     * 新增
     */
    public ReturnT<String> saveOrUpdate(DisposalScenesEntity disposalScenes, List<DisposalScenesNodeEntity> scenesNodeEntityList);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalScenesEntity disposalScenes);

    /**
     * 查询 get By Id
     */
    public DisposalScenesEntity getById(int id);

    /**
     * 查询 get By uuid
     */
    public DisposalScenesEntity getByUUId(String uuid);

    /**
     * 查询场景设备信息 find By scenesUuid
     * @param scenesUuid
     * @return
     */
    public List<DisposalScenesDTO> findByScenesUuid(String scenesUuid);

    /**
     * 查询 get By Id
     */
    public DisposalScenesEntity get(DisposalScenesEntity disposalScenes);

    /**
     * 分页查询
     */
    public PageInfo<DisposalScenesEntity> findList(DisposalScenesEntity disposalScenes, int pageNum, int pageSize);

    /**
     * 查询 List Data
     */
    public List<DisposalScenesEntity> findListAll();

    void clearRubbish();

}
