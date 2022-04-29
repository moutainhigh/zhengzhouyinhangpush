package com.abtnetworks.totems.auto.dao.mysql;

import com.abtnetworks.totems.auto.dto.AddressUpdateScenesDTO;
import com.abtnetworks.totems.auto.entity.AddressManageTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
@Mapper
@Repository
public interface AddressManageTaskMapper {

    int delete(Integer id);

    int insert(AddressManageTaskEntity record);

    AddressManageTaskEntity getById(Integer id);

    int update(AddressManageTaskEntity record);

    /**
     * 根据uuid获取自动开通任务
     * @param uuid
     * @return
     */
    AddressManageTaskEntity getByUuid(String uuid);

    /**
     * 更新任务场景
     * @param updateScenesDTO
     * @return
     */
    int updateScenes(AddressUpdateScenesDTO updateScenesDTO);

    /**
     * 查询防护网段列表
     * @param record
     * @return
     */
    List<AddressManageTaskEntity> findList(AddressManageTaskEntity record);
}
