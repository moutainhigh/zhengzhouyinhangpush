package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 17:47 2019/11/11
 */
public interface DisposalOrderCenterService {

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalOrderCenterEntity disposalOrderCenter);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalOrderCenterEntity disposalOrderCenter);

    /**
     * 更新 派发审核类型：0手动审核、1自动
     * @param uuid
     * @param sendType
     * @return
     */
    public ReturnT<String> updateSendTypeByUuid(String uuid, Integer sendType);

    /**
     * 查询 get By Id
     */
    public DisposalOrderCenterEntity getById(int id);

    /**
     * 根据UUID查询工单内容
     * @param uuid 工单内容UUID centerUuid
     * @return
     */
    public DisposalOrderCenterEntity getByUuid(String uuid);

    /**
     * 分页查询
     */
    public PageInfo<DisposalOrderCenterEntity> findList(DisposalOrderCenterEntity disposalOrderCenter, int pageNum, int pageSize);

    /**
     * 查询 List Data ALL 全部
     * @param category 分类：0:策略，1:路由
     * @param type 工单类型：1手动、2黑IP、3路径
     * @return
     */
    public List<DisposalOrderCenterEntity> findListAll(Integer category, Integer type);

}
