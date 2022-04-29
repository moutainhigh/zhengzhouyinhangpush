package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.dto.AddressUpdateScenesDTO;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 对象管理详情
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
@Mapper
@Repository
public interface AddressManageDetailMapper {

    int insert(AddressManageDetailEntity addressManageDetailEntity);

    int delete(Integer id);

    int update(AddressManageDetailEntity addressManageDetailEntity);

    AddressManageDetailEntity getById(Integer id);

    List<AddressManageDetailEntity> findList(AddressManageDetailEntity addressManageDetailEntity);

    int count();

    /**
     * 通过父级id删除详情
     */
    int deleteByParentId(@Param("parentId") Integer parentId);

    /**
     * 通过任务id删除详情
     * @param taskId
     * @return
     */
    int deleteByTaskId(@Param("taskId") Integer taskId);

    /**
     * 更新
     */
    int updateByPrimaryKey(AddressManageDetailEntity addressManageDetailEntity);

    /**
     * 更新任务场景
     * @param updateScenesDTO
     * @return
     */
    int updateDetailScenes(AddressUpdateScenesDTO updateScenesDTO);


    /**
     * get查询by parentid
     */
    List<AddressManageDetailEntity> getByParentId(@Param("parentId") Integer parentId);


    /**
     * 通过任务id查询对象详情列表
     */
    List<AddressManageDetailEntity> getByTaskId(Integer taskId);


    List<AddressManageDetailEntity> findAddressByName(@Param("addressName")  String addressName , @Param("addressType")  String addressType);


    List<AddressManageDetailEntity> getAddressByName(@Param("addressName")  String addressName , @Param("addressType")  String addressType);

    int updateByTaskId(AddressManageDetailEntity addressManageDetailEntity);
}
