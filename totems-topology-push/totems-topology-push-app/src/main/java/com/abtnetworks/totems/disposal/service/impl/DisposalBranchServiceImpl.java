package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.UUIDUtil;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalBranchMapper;
import com.abtnetworks.totems.disposal.entity.DisposalBranchEntity;
import com.abtnetworks.totems.disposal.service.DisposalBranchService;
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
 * @Date 16:54 2019/11/11
 */
@Service
public class DisposalBranchServiceImpl extends BaseService implements DisposalBranchService {

    @Resource
    private DisposalBranchMapper disposalBranchDao;

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalBranchEntity disposalBranch) {

        // valid
        if (disposalBranch == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalBranch.setUuid(UUIDUtil.getUuid());
        disposalBranch.setCreateTime(DateUtil.getCurrentTimestamp());

        disposalBranchDao.insert(disposalBranch);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalBranchDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalBranchEntity disposalBranch) {
        int ret = disposalBranchDao.update(disposalBranch);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalBranchEntity getById(int id) {
        return disposalBranchDao.getById(id);
    }

    @Override
    public DisposalBranchEntity getByNameAndIp(String name, String ip) {
        return disposalBranchDao.getByNameAndIp(name, ip);
    }

    /**
     * 查询 get
     */
    @Override
    public DisposalBranchEntity get(DisposalBranchEntity disposalBranch) {
        return disposalBranchDao.get(disposalBranch);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalBranchEntity> findList(DisposalBranchEntity disposalBranch, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalBranchEntity> list = disposalBranchDao.findList(disposalBranch);
        PageInfo<DisposalBranchEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 查询 List Data By uuids UUID的集合
     * @param uuids
     * @return
     */
    @Override
    public List<DisposalBranchEntity> findByUUIDs(String[] uuids) {
        return disposalBranchDao.findByUUIDs(uuids);
    }

}

