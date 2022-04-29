package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesNodeMapper;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.entity.DisposalScenesNodeEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.abtnetworks.totems.disposal.BaseService;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author hw
 * @Description
 * @Date 17:28 2019/11/11
 */
@Service
public class DisposalScenesServiceImpl extends BaseService implements DisposalScenesService {

    @Autowired
    private LogClientSimple logClientSimple;

    @Resource
    private DisposalScenesMapper disposalScenesDao;

    @Resource
    private DisposalScenesNodeMapper disposalScenesNodeDao;

    /**
     * 新增，修改
     */
    @Override
    public ReturnT<String> saveOrUpdate(DisposalScenesEntity scenesEntity, List<DisposalScenesNodeEntity> scenesNodeEntityList) {
        String logInfo = "场景管理" + scenesEntity.getCreateUser();
        try {
            //验证非空参数
            if (AliStringUtils.isEmpty(scenesEntity.getName())) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
            }

            DisposalScenesEntity po = disposalScenesDao.getByNameNotId(scenesEntity.getName(), scenesEntity.getId());
            if(po != null){
                return new ReturnT<String>(ReturnT.FAIL_CODE, "场景名称已存在，不可重复填写");
            }

            if (scenesEntity.getId() == null) {
                scenesEntity.setUuid(IdGen.uuid());
                scenesEntity.setCreateTime(new Date());

                disposalScenesDao.insert(scenesEntity);
                disposalScenesNodeDao.deleteByScenesUuid(scenesEntity.getUuid());
                disposalScenesNodeDao.bulkInsert(scenesNodeEntityList, scenesEntity.getUuid());
                logInfo = logInfo + " 新增场景" +scenesEntity.getName() +"：成功";
            } else {
                disposalScenesDao.update(scenesEntity);
                disposalScenesNodeDao.deleteByScenesUuid(scenesEntity.getUuid());
                disposalScenesNodeDao.bulkInsert(scenesNodeEntityList, scenesEntity.getUuid());
                logInfo = logInfo + " 修改场景" +scenesEntity.getName() +"：成功";
            }
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo);
        Map map = new HashMap();
        if (scenesEntity != null) {
            map.put("uuid", scenesEntity.getUuid());
        }
        return new ReturnT(map);
    }

    /**
     * 删除
     */
    @Override
    public ReturnT<String> delete(int id) {
        DisposalScenesEntity scenesEntity = disposalScenesDao.getById(id);
        if (scenesEntity == null) {
            return ReturnT.FAIL;
        }
        int ret1 = disposalScenesDao.delete(id);
        int ret2 = disposalScenesNodeDao.deleteByScenesUuid(scenesEntity.getUuid());
        return ret1+ret2>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 更新
     */
    @Override
    public ReturnT<String> update(DisposalScenesEntity disposalScenes) {
        int ret = disposalScenesDao.update(disposalScenes);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public DisposalScenesEntity getById(int id) {
        return disposalScenesDao.getById(id);
    }

    /**
     * 查询 get By uuid
     */
    @Override
    public DisposalScenesEntity getByUUId(String uuid) {
        return disposalScenesDao.getByUUId(uuid);
    }

    /**
     * 查询场景设备信息 find By scenesUuid
     * @param scenesUuid
     * @return
     */
    @Override
    public List<DisposalScenesDTO> findByScenesUuid(String scenesUuid) {
        return disposalScenesNodeDao.findByScenesUuid(scenesUuid);
    }

    /**
     * 查询 get
     */
    @Override
    public DisposalScenesEntity get(DisposalScenesEntity disposalScenes) {
        return disposalScenesDao.get(disposalScenes);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<DisposalScenesEntity> findList(DisposalScenesEntity disposalScenes, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalScenesEntity> list = disposalScenesDao.findList(disposalScenes);
        PageInfo<DisposalScenesEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public List<DisposalScenesEntity> findListAll() {
        return disposalScenesDao.findList(new DisposalScenesEntity());
    }

    @Override
    public void clearRubbish() {
        disposalScenesNodeDao.clearRubbish();
    }
}

