package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesNodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 11:14 2019/11/14
 */
@Mapper
@Repository
public interface DisposalScenesNodeMapper {

    /**
     * 新增
     */
    public int insert(DisposalScenesNodeEntity disposalScenesNode);

    /**
     * 批量插入
     * @param list
     * @return
     */
    public int bulkInsert(@Param("list") List<DisposalScenesNodeEntity> list, @Param("scenesUuid") String scenesUuid);

    /**
     * 删除 By id
     */
    public int delete(@Param("id") int id);

    /**
     * 删除 By scenesUuid
     */
    public int deleteByScenesUuid(@Param("scenesUuid") String scenesUuid);

    /**
     * 更新
     */
    public int update(DisposalScenesNodeEntity disposalScenesNode);

    /**
     * get查询 By Id
     */
    public DisposalScenesNodeEntity getById(@Param("id") int id);

    /**
     * get查询
     */
    public DisposalScenesNodeEntity get(DisposalScenesNodeEntity disposalScenesNode);

    /**
     * get list by scenesUuid
     * @param scenesUuid
     * @return
     */
    public List<DisposalScenesDTO> findByScenesUuid(@Param("scenesUuid") String scenesUuid);

    /**
     * 查询 List Data
     */
    public List<DisposalScenesNodeEntity> findList(DisposalScenesNodeEntity disposalScenesNode);

    /**
     * 查询 list dto 场景，设备节点 支持DTO 全部参数过滤
     * @param scenesDTO
     * @return
     */
    public List<DisposalScenesDTO> findDtoList(DisposalScenesDTO scenesDTO);

    /**
     * 查询Count
     */
    public int count();

    /**
     * 清理垃圾数据：在节点里面删除了设备，场景里面还存在设备信息，需清理
     */
    void clearRubbish();

    /**
     * 查询源域的信息
     * @param scenesDTO
     * @return
     */
    List<DisposalScenesDTO> findDtoListForIssue(DisposalScenesDTO scenesDTO);

    List<DisposalScenesDTO> findBySceneUuidList(@Param("scenesUuids") List<String> scenesUuids);

}

