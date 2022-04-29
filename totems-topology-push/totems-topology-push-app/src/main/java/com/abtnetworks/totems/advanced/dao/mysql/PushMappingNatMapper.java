package com.abtnetworks.totems.advanced.dao.mysql;

import com.abtnetworks.totems.advanced.dto.SearchPushMappingNatDTO;
import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/8
 */
@Mapper
@Repository
public interface PushMappingNatMapper {

    /**
     * 根据获取数据
     *
     * @return 高级设置数据列表
     */
    List<PushMappingNatEntity> listPushMappingNatInfo(Map<String, Object> params);

    /**
     * 更新数据
     *
     * @param entity 设置数据
     * @return
     */
    int updateByPrimaryKey(PushMappingNatEntity entity);

    /**
     * 插入数据
     *
     * @param entity 高级设置数据
     * @return 插入后数据
     */
    int insert(PushMappingNatEntity entity);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    int deletePushMappingNatInfo(@Param("ids") String ids);

}
