package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.entity.DisposalDeleteCommandLineRecordEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 14:26 2019/11/26
 */
public interface DisposalDeleteCommandLineRecordService {

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord);

    /**
     * 查询 get By Id
     */
    public DisposalDeleteCommandLineRecordEntity getById(int id);

    /**
     * find list 查询命令行 by 工单号 或 工单UUID
     * @param centerUuid
     * @param orderNo
     * @return
     */
    public PageInfo<DisposalNodeCommandLineRecordDTO> findListByCenterUuidOrOrderNo(String centerUuid, String orderNo, int pageNum, int pageSize);

    /**
     * 分页查询
     */
    public PageInfo<DisposalDeleteCommandLineRecordEntity> findList(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord, int pageNum, int pageSize);

}
