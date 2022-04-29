package com.abtnetworks.totems.advanced.dao.mysql;

import com.abtnetworks.totems.advanced.entity.AdvanceSettingsEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AdvanceSettingsMapper {
    /**
     * 根据参数名称获取数据
     * @param paramName 参数名称
     * @return 高级设置数据列表
     */
    List<AdvanceSettingsEntity> selectByParamName(String paramName);

    /**
     * 更新数据
     * @param entity 高级设置数据
     */
    void updateByPrimaryKey(AdvanceSettingsEntity entity);

    /**
     * 插入数据
     * @param entity 高级设置数据
     * @return 插入后数据
     */
    int insert(AdvanceSettingsEntity entity);
}