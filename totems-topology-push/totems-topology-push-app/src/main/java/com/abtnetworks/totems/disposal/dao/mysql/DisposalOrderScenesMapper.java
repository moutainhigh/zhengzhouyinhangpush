package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.DisposalNodeCredentialDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderScenesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:35 2019/11/12
 */
@Mapper
@Repository
public interface DisposalOrderScenesMapper {

    /**
     * 新增
     */
    public int insert(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * 批量插入，场景与封堵工单关系
     * @param scenesUuidArray
     * @param centerUuid
     * @return
     */
    public int bulkInsert(@Param("scenesUuidArray") String[] scenesUuidArray, @Param("centerUuid") String centerUuid);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 删除 By centerUuid 工单uuid
     */
    public int deleteByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * 更新
     */
    public int update(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * get查询 By Id
     */
    public DisposalOrderScenesEntity getById(@Param("id") int id);

    /**
     * get查询
     */
    public DisposalOrderScenesEntity get(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * 查询 List Data
     */
    public List<DisposalOrderScenesEntity> findList(DisposalOrderScenesEntity disposalOrderScenes);

    /**
     * find List 获取封堵工单场景中设备的凭证
     * @param centerUuidArray
     * @return
     */
    public List<DisposalNodeCredentialDTO> findOrderScenesNodeCredentialDtoList(@Param("centerUuidArray") String[] centerUuidArray);

    /**
     * find List 获取设备的凭证
     * @param deviceUuidArray
     * @return
     */
    public List<DisposalNodeCredentialDTO> findNodeCredentialDtoList(@Param("deviceUuidArray") String[] deviceUuidArray);

    /**
     * 查询 List 工单，场景信息 By centerUuid
     * @param centerUuid
     * @return
     */
    public List<DisposalOrderScenesEntity> getByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * 查询Count
     */
    public int count();

}

