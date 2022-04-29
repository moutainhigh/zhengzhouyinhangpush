package com.abtnetworks.totems.mapping.service.impl;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingIpMapper;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import com.abtnetworks.totems.mapping.service.AutoMappingIpService;
import com.abtnetworks.totems.mapping.vo.AutoMappingIpSearchVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutoMappingIpServiceImpl implements AutoMappingIpService {

    private static Logger logger = LoggerFactory.getLogger(AutoMappingIpServiceImpl.class);

    @Autowired
    private PushAutoMappingIpMapper pushAutoMappingIpMapper;

    @Override
    public PageInfo<PushAutoMappingIpEntity> findList(AutoMappingIpSearchVO vo, int pageNum, int pageSize) {
        PushAutoMappingIpEntity record = new PushAutoMappingIpEntity();
        BeanUtils.copyProperties(vo, record);
        PageHelper.startPage(pageNum, pageSize);

        List<PushAutoMappingIpEntity> taskEntities = pushAutoMappingIpMapper.selectByEntity(record);
        PageInfo<PushAutoMappingIpEntity> pageInfo = new PageInfo<>(taskEntities);
        return pageInfo;
    }

    @Override
    public List<PushAutoMappingIpEntity> findIpMappingByEntity(PushAutoMappingIpEntity entity) {
        return pushAutoMappingIpMapper.findIpMappingByEntity(entity);
    }


    @Override
    public List<PushAutoMappingIpEntity> findIpMappingByEntityAll(PushAutoMappingIpEntity entity) {
        return pushAutoMappingIpMapper.findIpMappingByDeviceUuid(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIdList(List<Integer> idList) throws Exception {
        int delete = pushAutoMappingIpMapper.deleteIdList(idList);
        return delete;
    }

    @Override
    public PushAutoMappingIpEntity selectById(int id) {
        return pushAutoMappingIpMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TotemsReturnT addOrUpdate(PushAutoMappingIpEntity entity) {
        if(ObjectUtils.isEmpty(entity.getId())){
            pushAutoMappingIpMapper.insertSelective(entity);
        } else {
            pushAutoMappingIpMapper.updateByPrimaryKeySelective(entity);
        }
        return TotemsReturnT.SUCCESS;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<PushAutoMappingIpEntity> entitys) {
        return pushAutoMappingIpMapper.batchInsert(entitys);
    }
}
