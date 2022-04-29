package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCredentialDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderScenesEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:44 2019/11/12
 */
public interface DisposalOrderScenesService {

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * 查询 get By Id
     */
    public DisposalOrderScenesEntity getById(int id);

    /**
     * 查询 get By Id
     */
    public DisposalOrderScenesEntity get(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * 分页查询
     */
    public PageInfo<DisposalOrderScenesEntity> findList(DisposalOrderScenesEntity disposalOrderScenes, int pageNum, int pageSize);

    /**
     * find List 获取封堵工单场景中设备的凭证
     * @param centerUuidArray
     * @return
     */
    public List<DisposalNodeCredentialDTO> findOrderScenesNodeCredentialDtoList(String[] centerUuidArray);

    /**
     * find List 获取设备的凭证
     * @param deviceUuidArray
     * @return
     */
    public List<DisposalNodeCredentialDTO> findNodeCredentialDtoList(String[] deviceUuidArray);

}
