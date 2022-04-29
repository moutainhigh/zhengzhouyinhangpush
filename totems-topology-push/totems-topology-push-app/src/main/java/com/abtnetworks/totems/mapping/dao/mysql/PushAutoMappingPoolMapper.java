package com.abtnetworks.totems.mapping.dao.mysql;

import com.abtnetworks.totems.mapping.entity.PushAutoMappingPoolEntity;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface PushAutoMappingPoolMapper {

    /**
     * 根据获取数据
     *
     * @return 高级设置数据列表
     */
    List<PushAutoMappingPoolEntity> listPushAutoMappingPoolInfo(Map<String, Object> params);

    /**
     * 更新数据
     *
     * @param entity 设置数据
     * @return
     */
    int updateByPrimaryKey(PushAutoMappingPoolEntity entity);

    /**
     * 插入数据
     *
     * @param entity 高级设置数据
     * @return 插入后数据
     */
    int insert(PushAutoMappingPoolEntity entity);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    int deletePushAutoMappingPoolInfo(@Param("ids") String ids);

    /**
     * 更新数据，按需更新
     * @param entity
     * @return
     */
    int updateByEntity(PushAutoMappingPoolEntity entity);

}
