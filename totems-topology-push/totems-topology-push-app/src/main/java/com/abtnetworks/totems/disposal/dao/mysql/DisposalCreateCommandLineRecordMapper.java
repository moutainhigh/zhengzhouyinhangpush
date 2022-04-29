package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.entity.DisposalCreateCommandLineRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 14:08 2019/11/26
 */
@Mapper
@Repository
public interface DisposalCreateCommandLineRecordMapper {

    /**
     * 新增
     */
    public int insert(DisposalCreateCommandLineRecordEntity disposalCreateCommandLineRecord);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalCreateCommandLineRecordEntity disposalCreateCommandLineRecord);

    /**
     * get查询 By Id
     */
    public DisposalCreateCommandLineRecordEntity getById(@Param("id") int id);

    /**
     * find list 查询命令行 by 工单号 或 工单UUID
     * @param centerUuid
     * @param orderNo
     * @return
     */
    public List<DisposalNodeCommandLineRecordDTO> findListByCenterUuidOrOrderNo(@Param("centerUuid") String centerUuid, @Param("orderNo") String orderNo);

    /**
     * 查询命令行信息，命令行未执行，没有执行记录
     * @param centerUuid
     * @return
     */
    List<DisposalNodeCommandLineRecordDTO> listByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * 查询 List Data
     */
    public List<DisposalCreateCommandLineRecordEntity> findList(DisposalCreateCommandLineRecordEntity disposalCreateCommandLineRecord);

    /**
     * 查询Count
     */
    public int count();

    /**
     * 查询设备的历史命令行
     * @param deviceUuid 设备uuid
     * @param routing 策略时为null, 路由时字段非null
     * @return
     */
    List<DisposalCreateCommandLineRecordEntity> findListByDevice(@Param("deviceUuid") String deviceUuid,
                                                                 @Param("routing") String routing,
                                                                 @Param("actionType") Integer actionType);

    /**
     * 查询Count By taskId
     */
    public int findCountByTaskId(@Param("taskId") Integer taskId);
}
