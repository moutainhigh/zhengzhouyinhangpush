package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.entity.DisposalDeleteCommandLineRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 14:23 2019/11/26
 */
@Mapper
@Repository
public interface DisposalDeleteCommandLineRecordMapper {

    /**
     * 新增
     */
    public int insert(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord);

    /**
     * get查询 By Id
     */
    public DisposalDeleteCommandLineRecordEntity getById(@Param("id") int id);

    /**
     * find list 查询命令行 by 工单号 或 工单UUID
     * @param centerUuid
     * @param orderNo
     * @return
     */
    public List<DisposalNodeCommandLineRecordDTO> findListByCenterUuidOrOrderNo(@Param("centerUuid") String centerUuid, @Param("orderNo") String orderNo);

    /**
     * 查询 List Data
     */
    public List<DisposalDeleteCommandLineRecordEntity> findList(DisposalDeleteCommandLineRecordEntity disposalDeleteCommandLineRecord);

    /**
     * 查询Count
     */
    public int count();

}
