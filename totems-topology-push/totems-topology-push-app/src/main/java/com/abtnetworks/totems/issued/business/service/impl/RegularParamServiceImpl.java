package com.abtnetworks.totems.issued.business.service.impl;

import com.abtnetworks.totems.issued.business.dao.mysql.CommandRegularParamMapper;
import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.issued.business.service.CommandRegularParamService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.CommandRegularParamPageDTO;
import com.abtnetworks.totems.issued.dto.CommandRegularUpdateDTO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
@Service("commandRegularParamService")
public class RegularParamServiceImpl implements CommandRegularParamService {
    /***/
    @Resource
    CommandRegularParamMapper pushCommandRegularParamMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int addCommandRegularParam(PushCommandRegularParamEntity pushCommandRegularParamEntity) {
        return pushCommandRegularParamMapper.addCommandRegularParam(pushCommandRegularParamEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteCommandRegParamById(String ids) {
        List<Integer> integerList = new LinkedList<>();
        String[] strIds = ids.split(SendCommandStaticAndConstants.COMMA_SPLIT);
        for (int i = 0; i < strIds.length; i++) {
            integerList.add(Integer.valueOf(strIds[i]));
        }
        return pushCommandRegularParamMapper.deleteCommandRegularParam(integerList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateCommandRegularParamById(CommandRegularUpdateDTO commandRegularUpdateDTO) {
        PushCommandRegularParamEntity pushCommandRegularParamEntity = new PushCommandRegularParamEntity();
        BeanUtils.copyProperties(commandRegularUpdateDTO, pushCommandRegularParamEntity);
        return pushCommandRegularParamMapper.updateCommandRegularParamById(pushCommandRegularParamEntity);
    }

    @Override
    public PageInfo<PushCommandRegularParamEntity> getCommandRegularParamList(CommandRegularParamPageDTO pushCommandRegularParamDTO) {

        PageHelper.startPage(pushCommandRegularParamDTO.getCurrentPage(), pushCommandRegularParamDTO.getPageSize());
        List<PushCommandRegularParamEntity> commandRegularParamList = pushCommandRegularParamMapper.getCommandRegularParamList(pushCommandRegularParamDTO);
        PageInfo<PushCommandRegularParamEntity> pageInfo = new PageInfo<>(commandRegularParamList);
        return pageInfo;
    }
}
