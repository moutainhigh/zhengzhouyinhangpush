package com.abtnetworks.totems.advanced.service.impl;

import com.abtnetworks.totems.advanced.dao.mysql.SceneForFiveBalanceMapper;
import com.abtnetworks.totems.advanced.dto.SceneForFiveBalanceDTO;
import com.abtnetworks.totems.advanced.entity.SceneForFiveBalanceEntity;
import com.abtnetworks.totems.advanced.service.SceneForFiveBalanceService;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.idgen.TotemsIdGen;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author lifei
 * @desc F5负载均衡设备场景实现类
 * @date 2021/7/30 17:18
 */
@Service
@Log4j2
public class SceneForFiveBalanceServiceImpl implements SceneForFiveBalanceService {

    @Autowired
    private SceneForFiveBalanceMapper sceneForFiveBalanceMapper;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;


    @Override
    public int createSceneForFiveBalance(SceneForFiveBalanceDTO dto) throws IssuedExecutorException{
        SceneForFiveBalanceEntity sceneForFiveBalanceEntity = new SceneForFiveBalanceEntity();
        BeanUtils.copyProperties(dto, sceneForFiveBalanceEntity);
        sceneForFiveBalanceEntity.setCreateTime(new Date());
        // 查询名称一样 则不创建
        SceneForFiveBalanceEntity queryNameEntity = new SceneForFiveBalanceEntity();
        queryNameEntity.setSceneName(dto.getSceneName());
        SceneForFiveBalanceEntity resutlentity = sceneForFiveBalanceMapper.getByEntity(queryNameEntity);
        if(null != resutlentity){
            throw new IssuedExecutorException(SendErrorEnum.SCENE_NAME_EXIST);
        }
        sceneForFiveBalanceEntity.setSceneUuid(TotemsIdGen.uuid());
        dto.setSceneUuid(sceneForFiveBalanceEntity.getSceneUuid());
        sceneForFiveBalanceMapper.add(sceneForFiveBalanceEntity);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int deleteById(String ids) {
        return sceneForFiveBalanceMapper.delete(ids);
    }

    @Override
    public int updateSceneForFiveBalance(SceneForFiveBalanceDTO dto) {
        SceneForFiveBalanceEntity sceneForFiveBalanceEntity = new SceneForFiveBalanceEntity();
        BeanUtils.copyProperties(dto, sceneForFiveBalanceEntity);
        sceneForFiveBalanceEntity.setUpdateTime(new Date());
        return sceneForFiveBalanceMapper.update(sceneForFiveBalanceEntity);
    }

    @Override
    public SceneForFiveBalanceDTO getSceneForFiveBalance(SceneForFiveBalanceDTO dto) {
        SceneForFiveBalanceEntity entity = new SceneForFiveBalanceEntity();
        BeanUtils.copyProperties(dto, entity);
        SceneForFiveBalanceEntity resultEntity = sceneForFiveBalanceMapper.getByEntity(entity);
        if (null == resultEntity) {
            log.info("根据id:{}/场景uuid:{}获取场景信息为空", dto.getId(), dto.getSceneUuid());
            return null;
        }
        SceneForFiveBalanceDTO resultDto = new SceneForFiveBalanceDTO();
        BeanUtils.copyProperties(resultEntity, resultDto);
        return resultDto;
    }

    @Override
    public PageInfo<SceneForFiveBalanceEntity> querySceneForFiveBalanceList(SceneForFiveBalanceDTO dto) {
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        SceneForFiveBalanceEntity entity = new SceneForFiveBalanceEntity();
        if (StringUtils.isNotBlank(dto.getSceneName())) {
            entity.setSceneName(dto.getSceneName());
        }
        List<SceneForFiveBalanceEntity> scene = sceneForFiveBalanceMapper.queryList(entity);
        if (CollectionUtils.isEmpty(scene)) {
            log.info("根据场景名称:{}查询场景列表为空");
            return new PageInfo();
        }

        PageInfo<SceneForFiveBalanceEntity> pageInfo = new PageInfo<>(scene);
        return pageInfo;
    }
}
