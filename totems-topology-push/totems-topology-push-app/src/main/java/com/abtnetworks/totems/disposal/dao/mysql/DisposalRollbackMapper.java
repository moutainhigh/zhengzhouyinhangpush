package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.DisposalRollbackOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalRollbackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 17:19 2019/11/15
 */
@Mapper
@Repository
public interface DisposalRollbackMapper {

    /**
     * 新增
     */
    public int insert(DisposalRollbackEntity disposalRollback);

    /**
     * 回滚插入 方式：INSERT INTO SELECT
     * @param disposalRollback
     * @return
     */
    public int insertBySelectOrder(DisposalRollbackEntity disposalRollback);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalRollbackEntity disposalRollback);

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
    public DisposalRollbackEntity getById(@Param("id") int id);

    /**
     * get 获取 DisposalRollbackEntity By pCenterUuid，centerUuid
     * @param pCenterUuid
     * @param centerUuid
     * @return
     */
    public DisposalRollbackEntity getRollbackEntity(@Param("pCenterUuid") String pCenterUuid, @Param("centerUuid") String centerUuid);

    /**
     * get Dto 查询 By centerUuid
     */
    public DisposalRollbackOrderDTO getByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * 查询 List Data
     */
    public List<DisposalRollbackEntity> findList(DisposalRollbackEntity disposalRollback);

    /**
     * 查询 Dto List Data
     */
    public List<DisposalRollbackOrderDTO> findDtoList(DisposalRollbackOrderDTO disposalRollbackOrderDTO);

    /**
     * 查询Count
     */
    public int count();

}

