package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.entity.DisposalTeamBranchEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:08 2019/11/12
 */
public interface DisposalTeamBranchService {

    /**
     * 批量保存
     */
    public ReturnT<String> batchSave(String centerUuid, List<DisposalTeamBranchEntity> list);

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 更新 派发下级单位处置单状态
     */
    public ReturnT<String> updateHandleStatus(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 更新 派发下级单位回滚处置单状态
     */
    public ReturnT<String> updateCallbackHandleStatus(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 查询 get By Id
     */
    public DisposalTeamBranchEntity getById(int id);

    /**
     * 查询 get By Id
     */
    public List<DisposalTeamBranchEntity> findByCenterUuid(String centerUuid);

    /**
     * 分页查询
     */
    public PageInfo<DisposalTeamBranchEntity> findList(DisposalTeamBranchEntity disposalTeamBranch, int pageNum, int pageSize);

}
