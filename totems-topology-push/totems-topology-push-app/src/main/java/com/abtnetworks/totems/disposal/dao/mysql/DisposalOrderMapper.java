package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.AttackChainDisposalOrderDTO;
import com.abtnetworks.totems.disposal.dto.AttackChainDisposalQueryOrderDTO;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @Author hw
 * @Description
 * @Date 17:52 2019/11/11
 */
@Mapper
@Repository
public interface DisposalOrderMapper {

    /**
     * 新增
     */
    public int insert(DisposalOrderEntity disposalOrder);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalOrderEntity disposalOrder);

    /**
     * 更新状态status By 工单内容UUID centerUuid
     * @param centerUuid
     * @param status
     * @return
     */
    public int updateStatusByCenterUuid(@Param("centerUuid") String centerUuid, @Param("status") Integer status, @Param("errorMessage") String errorMessage);

    /**
     * get查询 By Id
     */
    public DisposalOrderEntity getById(@Param("id") int id);

    /**
     * get DisposalOrderEntity By centerUuid
     * @param centerUuid
     * @return
     */
    public DisposalOrderEntity getOrderEntityByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * get Dto 查询 By centerUuid
     */
    public DisposalOrderDTO getByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * 查询 List Data
     */
    public List<DisposalOrderEntity> findList(DisposalOrderEntity disposalOrder);

    /**
     * 查询List DTO Data
     * @param disposalOrderDTO
     * @return
     */
    public List<DisposalOrderDTO> findDtoList(DisposalOrderDTO disposalOrderDTO);

    /**
     * 攻击链使用：查询过滤 封堵工单list AttackChainDisposalOrderDTO
     * @param attackChainDisposalQueryOrderDTO
     * @return
     */
    public List<AttackChainDisposalOrderDTO> findAttackChainDtoList(AttackChainDisposalQueryOrderDTO attackChainDisposalQueryOrderDTO);

    /**
     * 查询Count
     */
    public int count();

}

