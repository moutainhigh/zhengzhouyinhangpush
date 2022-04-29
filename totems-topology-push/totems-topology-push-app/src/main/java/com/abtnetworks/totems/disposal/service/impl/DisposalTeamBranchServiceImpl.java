package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalTeamBranchMapper;
import com.abtnetworks.totems.disposal.entity.DisposalTeamBranchEntity;
import com.abtnetworks.totems.disposal.service.DisposalTeamBranchService;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.abtnetworks.totems.disposal.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 10:09 2019/11/12
 */
@Service
public class DisposalTeamBranchServiceImpl extends BaseService implements DisposalTeamBranchService {

    @Resource
    private DisposalTeamBranchMapper disposalTeamBranchDao;

    @Override
    public ReturnT<String> batchSave(String centerUuid, List<DisposalTeamBranchEntity> list) {
        disposalTeamBranchDao.bulkInsert(list, centerUuid);
        return ReturnT.SUCCESS;
    }

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalTeamBranchEntity disposalTeamBranch) {

        // valid
        if (disposalTeamBranch == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalTeamBranchDao.insert(disposalTeamBranch);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalTeamBranchDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalTeamBranchEntity disposalTeamBranch) {
        int ret = disposalTeamBranchDao.update(disposalTeamBranch);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新 派发下级单位处置单状态
     * @param disposalTeamBranch
     * @return
     */
    @Override
    public ReturnT<String> updateHandleStatus(DisposalTeamBranchEntity disposalTeamBranch) {
        if (!AliStringUtils.areNotEmpty(disposalTeamBranch.getCenterUuid(), disposalTeamBranch.getBranchName())
                || disposalTeamBranch.getHandleStatus() == null) {
            return ReturnT.FAIL;
        }
        int ret = disposalTeamBranchDao.updateHandleStatus(disposalTeamBranch);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新 派发下级单位回滚处置单状态
     * @param disposalTeamBranch
     * @return
     */
    @Override
    public ReturnT<String> updateCallbackHandleStatus(DisposalTeamBranchEntity disposalTeamBranch) {
        if (!AliStringUtils.areNotEmpty(disposalTeamBranch.getCenterUuid(), disposalTeamBranch.getBranchName())
                || disposalTeamBranch.getCallbackHandleStatus() == null) {
            return ReturnT.FAIL;
        }
        int ret = disposalTeamBranchDao.updateCallbackHandleStatus(disposalTeamBranch);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalTeamBranchEntity getById(int id) {
        return disposalTeamBranchDao.getById(id);
    }

    /**
     * 查询 get
     */
    @Override
    public List<DisposalTeamBranchEntity> findByCenterUuid(String centerUuid) {
        return disposalTeamBranchDao.findByCenterUuid(centerUuid);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalTeamBranchEntity> findList(DisposalTeamBranchEntity disposalTeamBranch, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalTeamBranchEntity> list = disposalTeamBranchDao.findList(disposalTeamBranch);
        PageInfo<DisposalTeamBranchEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

}

