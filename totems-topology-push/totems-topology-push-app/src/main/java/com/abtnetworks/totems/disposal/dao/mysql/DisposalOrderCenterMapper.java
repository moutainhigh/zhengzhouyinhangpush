package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @Author hw
 * @Description
 * @Date 17:34 2019/11/11
 */
@Mapper
@Repository
public interface DisposalOrderCenterMapper {

    /**
     * 新增
     */
    public int insert(DisposalOrderCenterEntity disposalOrderCenter);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalOrderCenterEntity disposalOrderCenter);

    /**
     * 更新来源分类，是否上级派发
     * @param uuid
     * @param sourceClassification
     * @return
     */
    public int updateSourceClassificationByUuid(@Param("uuid") String uuid, @Param("sourceClassification") Integer sourceClassification);

    /**
     * 更新 派发审核类型：0手动审核、1自动
     * @param sendType
     * @return
     */
    public int updateSendTypeByUuid(@Param("uuid") String uuid, @Param("sendType") Integer sendType);

    /**
     * get查询 By Id
     */
    public DisposalOrderCenterEntity getById(@Param("id") int id);

    /**
     * 根据UUID查询工单内容
     */
    DisposalOrderCenterEntity getByUuid(@Param("uuid") String uuid);

    /**
     * 查询 List Data
     */
    public List<DisposalOrderCenterEntity> findList(DisposalOrderCenterEntity disposalOrderCenter);

    /**
     * 查询 List Data All (INNER JOIN disposal_order o ON o.center_uuid = oc.uuid)
     */
    public List<DisposalOrderCenterEntity> findListAll(@Param("category") Integer category, @Param("type") Integer type);

    /**
     * 查询Count
     */
    public int count();

}

