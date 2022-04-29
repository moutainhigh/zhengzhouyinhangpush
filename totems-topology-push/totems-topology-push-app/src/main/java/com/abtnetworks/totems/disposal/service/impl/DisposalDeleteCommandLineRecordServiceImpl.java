package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalDeleteCommandLineRecordMapper;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.entity.DisposalDeleteCommandLineRecordEntity;
import com.abtnetworks.totems.disposal.service.DisposalDeleteCommandLineRecordService;
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
 * @Date 14:27 2019/11/26
 */
@Service
public class DisposalDeleteCommandLineRecordServiceImpl extends BaseService implements DisposalDeleteCommandLineRecordService {

    @Resource
    private DisposalDeleteCommandLineRecordMapper disposalDeleteCommandLineRecordDao;

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord) {

        // valid
        if (disposalDeleteCommandLineRecord == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalDeleteCommandLineRecordDao.insert(disposalDeleteCommandLineRecord);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalDeleteCommandLineRecordDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord) {
        int ret = disposalDeleteCommandLineRecordDao.update(disposalDeleteCommandLineRecord);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalDeleteCommandLineRecordEntity getById(int id) {
        return disposalDeleteCommandLineRecordDao.getById(id);
    }

    @Override
    public PageInfo<DisposalNodeCommandLineRecordDTO> findListByCenterUuidOrOrderNo(String centerUuid, String orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalNodeCommandLineRecordDTO> list = disposalDeleteCommandLineRecordDao.findListByCenterUuidOrOrderNo(centerUuid, orderNo);
        PageInfo<DisposalNodeCommandLineRecordDTO> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalDeleteCommandLineRecordEntity> findList(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalDeleteCommandLineRecordEntity> list = disposalDeleteCommandLineRecordDao.findList(disposalDeleteCommandLineRecord);
        PageInfo<DisposalDeleteCommandLineRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

}
