package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalCreateCommandLineRecordMapper;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalCreateCommandLineRecordEntity;
import com.abtnetworks.totems.disposal.service.DisposalCreateCommandLineRecordService;
import com.abtnetworks.totems.disposal.service.DisposalOrderCenterService;
import com.abtnetworks.totems.disposal.service.DisposalOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.abtnetworks.totems.disposal.BaseService;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author hw
 * @Description
 * @Date 14:15 2019/11/26
 */
@Service
public class DisposalCreateCommandLineRecordServiceImpl extends BaseService implements DisposalCreateCommandLineRecordService {

    @Resource
    private DisposalCreateCommandLineRecordMapper disposalCreateCommandLineRecordDao;

    @Autowired
    private DisposalOrderService disposalOrderService;

    /**
     * 新增
     */
    @Override
    public ReturnT<String> insert(DisposalCreateCommandLineRecordEntity disposalCreateCommandLineRecord) {

        // valid
        if (disposalCreateCommandLineRecord == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        disposalCreateCommandLineRecordDao.insert(disposalCreateCommandLineRecord);
        return ReturnT.SUCCESS;
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        int ret = disposalCreateCommandLineRecordDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalCreateCommandLineRecordEntity disposalCreateCommandLineRecord) {
        int ret = disposalCreateCommandLineRecordDao.update(disposalCreateCommandLineRecord);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalCreateCommandLineRecordEntity getById(int id) {
        return disposalCreateCommandLineRecordDao.getById(id);
    }

    @Override
    public PageInfo<DisposalNodeCommandLineRecordDTO> findListByCenterUuidOrOrderNo(String centerUuid, String orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        //查询工单信息，根据状态判断，读取命令行下发记录
        DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(centerUuid);
//        List<DisposalNodeCommandLineRecordDTO>
        List<DisposalNodeCommandLineRecordDTO> list = Collections.emptyList();
        if (orderDTO != null && orderDTO.getStatus().intValue() != PushConstants.PUSH_INT_PUSH_GENERATING_SUCCESS) {
            list = disposalCreateCommandLineRecordDao.findListByCenterUuidOrOrderNo(centerUuid, orderNo);
        } else {
            logger.info("查询下发记录时，状态为：生成命令行成功，仅查询命令行");
            list = disposalCreateCommandLineRecordDao.listByCenterUuid(centerUuid);
        }

        PageInfo<DisposalNodeCommandLineRecordDTO> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalCreateCommandLineRecordEntity> findList(DisposalCreateCommandLineRecordEntity disposalCreateCommandLineRecord, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalCreateCommandLineRecordEntity> list = disposalCreateCommandLineRecordDao.findList(disposalCreateCommandLineRecord);
        PageInfo<DisposalCreateCommandLineRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 查询Count By taskId
     * @param taskId
     * @return
     */
    @Override
    public int findCountByTaskId(Integer taskId) {
        return disposalCreateCommandLineRecordDao.findCountByTaskId(taskId);
    }

}
