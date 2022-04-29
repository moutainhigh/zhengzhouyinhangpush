package com.abtnetworks.totems.mapping.service.impl;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingRouteMapper;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingRouteEntity;
import com.abtnetworks.totems.mapping.service.AutoMappingRouteService;
import com.abtnetworks.totems.mapping.vo.AutoMappingRouteSearchVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @desc    地址映射自动匹配接口实现类
 * @author liuchanghao
 * @date 2022-01-21 10:34
 */
@Service
public class AutoMappingRouteServiceImpl implements AutoMappingRouteService {

    private static Logger logger = LoggerFactory.getLogger(AutoMappingRouteServiceImpl.class);

    @Autowired
    private PushAutoMappingRouteMapper pushAutoMappingRouteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TotemsReturnT addOrUpdate(PushAutoMappingRouteEntity routeEntity) throws Exception {
        try{
            PushAutoMappingRouteEntity entity = new PushAutoMappingRouteEntity();
            if(ObjectUtils.isEmpty(routeEntity.getId())){
                BeanUtils.copyProperties(routeEntity,entity);
                pushAutoMappingRouteMapper.insert(entity);
            } else {
                pushAutoMappingRouteMapper.updateByPrimaryKey(entity);
            }
        } catch (Exception e) {
            logger.error("新增或修改路由规则异常，异常原因：", e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "新增或修改路由规则异常");
        }
        return TotemsReturnT.SUCCESS;
    }

    @Override
    public PageInfo<PushAutoMappingRouteEntity> findList(AutoMappingRouteSearchVO vo, int pageNum, int pageSize) {
        PushAutoMappingRouteEntity record = new PushAutoMappingRouteEntity();
        BeanUtils.copyProperties(vo, record);
        PageHelper.startPage(pageNum, pageSize);

        List<PushAutoMappingRouteEntity> taskEntities = pushAutoMappingRouteMapper.selectByEntity(record);
        PageInfo<PushAutoMappingRouteEntity> pageInfo = new PageInfo<>(taskEntities);
        return pageInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIdList(List<Integer> idList) throws Exception {
        int delete = pushAutoMappingRouteMapper.deleteIdList(idList);
        return delete;
    }

    @Override
    public PushAutoMappingRouteEntity selectById(int id) {
        return pushAutoMappingRouteMapper.selectByPrimaryKey(id);
    }
}
