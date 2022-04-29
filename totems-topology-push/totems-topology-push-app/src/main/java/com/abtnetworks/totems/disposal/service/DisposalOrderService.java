package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.AttackChainDisposalOrderDTO;
import com.abtnetworks.totems.disposal.dto.AttackChainDisposalQueryOrderDTO;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderEntity;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 18:47 2019/11/11
 */
public interface DisposalOrderService {

    /**
     * 生成封堵命令行
     *
     * @param userName   用户名
     * @param centerUuid 工单uuid
     * @return
     */
    int threadGenerateCommand(String userName, String centerUuid);

    /**
     * 开始封堵命令下发
     * @param streamId
     * @param centerUuid
     */
    public void startSendCommandTasks(String streamId, String centerUuid, String userName);

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalOrderEntity disposalOrder);

    /**
     * 保存 or 修改
     */
    public ReturnT<Map<String, String>> saveOrUpdate(DisposalOrderDTO orderDTO);

    /**
     * 保存 or 修改
     */
    public ReturnT<List<String>> batchSave(List<DisposalOrderDTO> list,String userName);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalOrderEntity disposalOrder);

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
    public DisposalOrderEntity getById(int id);

    /**
     * get DisposalOrderEntity By centerUuid
     * @param centerUuid
     * @return
     */
    public DisposalOrderEntity getOrderEntityByCenterUuid(String centerUuid);

    /**
     * 查询 get Dto By centerUuid
     */
    public DisposalOrderDTO getByCenterUuid(String centerUuid);

    /**
     * 分页查询
     */
    public PageInfo<DisposalOrderDTO> findDtoList(DisposalOrderDTO disposalOrderDTO, int pageNum, int pageSize);

    /**
     * 攻击链使用：查询过滤 封堵工单list AttackChainDisposalOrderDTO
     * @param queryOrderDTO
     * @return
     */
    public List<AttackChainDisposalOrderDTO> findAttackChainDtoList(AttackChainDisposalQueryOrderDTO queryOrderDTO);

}
