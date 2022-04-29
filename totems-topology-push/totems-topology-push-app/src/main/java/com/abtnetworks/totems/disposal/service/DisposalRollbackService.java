package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalRollbackOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalRollbackEntity;
import com.github.pagehelper.PageInfo;

/**
 * @Author hw
 * @Description
 * @Date 17:21 2019/11/15
 */
public interface DisposalRollbackService {

    /**
     * 开始回滚命令行下发
     * @param streamId
     * @param centerUuid
     */
    public void startSendDeleteCommandTasks(String streamId, String centerUuid, String userName);

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalRollbackEntity disposalRollback);

    /**
     * 回滚插入 方式：INSERT INTO SELECT
     * @param disposalRollback
     * @return
     */
    public ReturnT<String> insertBySelectOrder(DisposalRollbackEntity disposalRollback);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalRollbackEntity disposalRollback);

    /**
     * 更新状态status By 工单内容UUID centerUuid
     * @param centerUuid
     * @param status 0：未执行，1：下发完成；更新为下发完成；2：下发失败；5：下发过程中；6：正在生成命令行；7：生成命令行出错；
     * @param errorMessage 错误异常信息
     * @return
     */
    public ReturnT<String> updateStatusByCenterUuid(String centerUuid, Integer status, String errorMessage);

    /**
     * 查询 get By Id
     */
    public DisposalRollbackEntity getById(int id);

    /**
     * get 获取 DisposalRollbackEntity By pCenterUuid，centerUuid
     * @param pCenterUuid
     * @param centerUuid
     * @return
     */
    public DisposalRollbackEntity getRollbackEntity(String pCenterUuid, String centerUuid);

    /**
     * 查询 get Dto By centerUuid
     */
    public DisposalRollbackOrderDTO getByCenterUuid(String centerUuid);

    /**
     * 分页查询
     */
    public PageInfo<DisposalRollbackOrderDTO> findDtoList(DisposalRollbackOrderDTO disposalRollbackOrderDTO, int pageNum, int pageSize);

}

