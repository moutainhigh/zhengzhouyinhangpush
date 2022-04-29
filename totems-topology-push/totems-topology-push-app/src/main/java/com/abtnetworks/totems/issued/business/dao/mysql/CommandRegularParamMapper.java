package com.abtnetworks.totems.issued.business.dao.mysql;

import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.issued.dto.CommandRegularParamPageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
@Mapper
@Repository
public interface CommandRegularParamMapper {

    /**
     * 获取参数命令行列表
     *
     * @param pushCommandRegularParamDTO 参数
     * @return
     */
    List<PushCommandRegularParamEntity> getCommandRegularParamList(CommandRegularParamPageDTO pushCommandRegularParamDTO);

    /**
     * 添加
     *
     * @param pushCommandRegularParamEntity
     * @return
     */
    int addCommandRegularParam(PushCommandRegularParamEntity pushCommandRegularParamEntity);

    /**
     * 根据设备型号获取实体
     *
     * @param modelNumber
     * @return
     */
    PushCommandRegularParamEntity getCommandRegularParamByModelNumber(String modelNumber);

    /**
     * 根据id删除
     *
     * @param ids
     * @return
     */
    int deleteCommandRegularParam(@Param("ids") List<Integer> ids);

    /**
     * 修改
     *
     * @param pushCommandRegularParamEntity
     * @return
     */
    int updateCommandRegularParamById(PushCommandRegularParamEntity pushCommandRegularParamEntity);


}
