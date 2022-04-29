package com.abtnetworks.totems.mapping.service.impl;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.mapping.dao.mysql.PushAutoMappingSceneRuleMapper;
import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import com.abtnetworks.totems.mapping.service.AutoMappingSceneRuleService;
import com.abtnetworks.totems.mapping.vo.AutoMappingSceneRuleVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
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
public class AutoMappingSceneRuleServiceImpl implements AutoMappingSceneRuleService {

    private static Logger logger = LoggerFactory.getLogger(AutoMappingSceneRuleServiceImpl.class);

    @Autowired
    private PushAutoMappingSceneRuleMapper pushAutoMappingSceneRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TotemsReturnT addOrUpdate(AutoMappingSceneRuleVO sceneRuleVO) throws Exception {
        PushAutoMappingSceneRuleEntity entity = new PushAutoMappingSceneRuleEntity();
        List<PushAutoMappingSceneRuleEntity> sceneRuleEntityList = pushAutoMappingSceneRuleMapper.selectAll();
        if(ObjectUtils.isEmpty(sceneRuleVO.getId())){
            // 新增场景规则时，不能与其他规则有交集
            TotemsReturnT intersection = isIntersection(sceneRuleEntityList, null, sceneRuleVO, false);
            if(intersection.getCode() == TotemsReturnT.FAIL_CODE){
                return intersection;
            }

            BeanUtils.copyProperties(sceneRuleVO,entity);
            pushAutoMappingSceneRuleMapper.insert(entity);
        } else {
            TotemsReturnT intersection = isIntersection(sceneRuleEntityList, sceneRuleVO.getId(), sceneRuleVO, true);
            if(intersection.getCode() == TotemsReturnT.FAIL_CODE){
                return intersection;
            }
            BeanUtils.copyProperties(sceneRuleVO,entity);
            pushAutoMappingSceneRuleMapper.updateByPrimaryKeySelective(entity);
        }

        return TotemsReturnT.SUCCESS;
    }

    /**
     * 判断当前规则与其他规则是否有交集
     * @param sceneRuleEntityList
     * @param id
     * @param checkVO
     * @return
     */
    private TotemsReturnT isIntersection(List<PushAutoMappingSceneRuleEntity> sceneRuleEntityList, Integer id, AutoMappingSceneRuleVO checkVO, boolean isUpdate){
        if(CollectionUtils.isNotEmpty(sceneRuleEntityList)){
            for(PushAutoMappingSceneRuleEntity sceneRuleEntity : sceneRuleEntityList){
                // 跳过自身比较
                if(isUpdate && sceneRuleEntity.getId().intValue() == id){
                    continue;
                }
                if(IpUtils.isIntersection(checkVO.getSrcIp(), sceneRuleEntity.getSrcIp()) || IpUtils.isIntersection(checkVO.getDstIp(), sceneRuleEntity.getDstIp())){
                    return new TotemsReturnT(TotemsReturnT.FAIL_CODE,"当前输入工单规则与规则："+ sceneRuleEntity.getRuleName() +"数据存在交集，请确认");
                }
            }
        }
        return TotemsReturnT.SUCCESS;
    }

    @Override
    public PageInfo<PushAutoMappingSceneRuleEntity> findList(AutoMappingSceneRuleVO vo, int pageNum, int pageSize) {
        PushAutoMappingSceneRuleEntity record = new PushAutoMappingSceneRuleEntity();
        BeanUtils.copyProperties(vo, record);
        PageHelper.startPage(pageNum, pageSize);

        List<PushAutoMappingSceneRuleEntity> taskEntities = pushAutoMappingSceneRuleMapper.selectByEntity(record);
        PageInfo<PushAutoMappingSceneRuleEntity> pageInfo = new PageInfo<>(taskEntities);
        return pageInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIdList(List<Integer> idList) throws Exception {
        int delete = pushAutoMappingSceneRuleMapper.deleteIdList(idList);
        return delete;
    }

    @Override
    public PushAutoMappingSceneRuleEntity selectById(int id) {
        return pushAutoMappingSceneRuleMapper.selectByPrimaryKey(id);
    }
}
