package com.abtnetworks.totems.issued.business.service;

import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.issued.dto.CommandRegularParamPageDTO;
import com.abtnetworks.totems.issued.dto.CommandRegularUpdateDTO;
import com.github.pagehelper.PageInfo;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
public interface CommandRegularParamService {

    /**
     * 添加
     *
     * @param pushCommandRegularParamEntity
     * @return
     */
    int addCommandRegularParam(PushCommandRegularParamEntity pushCommandRegularParamEntity);

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    int deleteCommandRegParamById(String id);

    /**
     * 修改
     *
     * @param commandRegularUpdateDTO
     * @return
     */
    int updateCommandRegularParamById(CommandRegularUpdateDTO commandRegularUpdateDTO);

    /***
     * 查询
     * @param pushCommandRegularParamDTO
     * @return
     */
    PageInfo<PushCommandRegularParamEntity> getCommandRegularParamList(CommandRegularParamPageDTO pushCommandRegularParamDTO);


}
