package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderScenesMapper;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCredentialDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalOrderScenesService;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.abtnetworks.totems.disposal.BaseService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:45 2019/11/12
 */
@Service
public class DisposalOrderScenesServiceImpl extends BaseService implements DisposalOrderScenesService {

    @Resource
    private DisposalOrderScenesMapper disposalOrderScenesDao;

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalOrderScenesEntity disposalOrderScenes) {

        // valid
        if (disposalOrderScenes == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalOrderScenesDao.insert(disposalOrderScenes);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalOrderScenesDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalOrderScenesEntity disposalOrderScenes) {
        int ret = disposalOrderScenesDao.update(disposalOrderScenes);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalOrderScenesEntity getById(int id) {
        return disposalOrderScenesDao.getById(id);
    }

    /**
     * 查询 get
     */
    @Override
    public DisposalOrderScenesEntity get(DisposalOrderScenesEntity disposalOrderScenes) {
        return disposalOrderScenesDao.get(disposalOrderScenes);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalOrderScenesEntity> findList(DisposalOrderScenesEntity disposalOrderScenes, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalOrderScenesEntity> list = disposalOrderScenesDao.findList(disposalOrderScenes);
        PageInfo<DisposalOrderScenesEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public List<DisposalNodeCredentialDTO> findOrderScenesNodeCredentialDtoList(String[] centerUuidArray) {
        return disposalOrderScenesDao.findOrderScenesNodeCredentialDtoList(centerUuidArray);
    }

    @Override
    public List<DisposalNodeCredentialDTO> findNodeCredentialDtoList(String[] deviceUuidArray) {
        return disposalOrderScenesDao.findNodeCredentialDtoList(deviceUuidArray);
    }

}

