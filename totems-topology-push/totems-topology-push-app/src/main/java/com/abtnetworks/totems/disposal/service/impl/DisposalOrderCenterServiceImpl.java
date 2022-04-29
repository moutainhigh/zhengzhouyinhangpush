package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderCenterMapper;
import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import com.abtnetworks.totems.disposal.service.DisposalOrderCenterService;
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
 * @Date 17:48 2019/11/11
 */
@Service
public class DisposalOrderCenterServiceImpl extends BaseService implements DisposalOrderCenterService {

    @Resource
    private DisposalOrderCenterMapper disposalOrderCenterDao;

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalOrderCenterEntity disposalOrderCenter) {

        // valid
        if (disposalOrderCenter == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalOrderCenterDao.insert(disposalOrderCenter);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalOrderCenterDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalOrderCenterEntity disposalOrderCenter) {
        int ret = disposalOrderCenterDao.update(disposalOrderCenter);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新 派发审核类型：0手动审核、1自动
     * @param uuid
     * @param sendType
     * @return
     */
    @Override
    public ReturnT<String> updateSendTypeByUuid(String uuid, Integer sendType) {
        int ret = disposalOrderCenterDao.updateSendTypeByUuid(uuid, sendType);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalOrderCenterEntity getById(int id) {
        return disposalOrderCenterDao.getById(id);
    }

    /**
     * 根据UUID查询工单内容
     * @param uuid 工单内容UUID centerUuid
     * @return
     */
    @Override
    public DisposalOrderCenterEntity getByUuid(String uuid) {
        return disposalOrderCenterDao.getByUuid(uuid);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalOrderCenterEntity> findList(DisposalOrderCenterEntity disposalOrderCenter, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalOrderCenterEntity> list = disposalOrderCenterDao.findList(disposalOrderCenter);
        PageInfo<DisposalOrderCenterEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 查询 List Data All (INNER JOIN disposal_order o ON o.center_uuid = oc.uuid)
     * @param category 分类：0:策略，1:路由
     * @param type 工单类型：1手动、2黑IP、3路径
     * @return
     */
    @Override
    public List<DisposalOrderCenterEntity> findListAll(Integer category, Integer type) {
        return disposalOrderCenterDao.findListAll(category, type);
    }

}

