package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
/**
 * @desc    封禁IP 接口
 * @author liuchanghao
 * @date 2020-09-10 16:52
 */
@Service
public interface PushForbidIpService {

    /**
     * 新增或修改封禁IP
     * @param entity
     * @return
     */
    PushForbidIpEntity addOrUpdate(PushForbidIpEntity entity);

    /**
     * 启用/禁用
     * @param id
     * @param enableStatus
     * @param updateUser
     * @return
     */
    ReturnT<String> enable(int id, String enableStatus,String updateUser);

    /**
     * 分页查询
     * @param entity
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushForbidIpEntity> findList(PushForbidIpEntity entity, int pageNum, int pageSize);


    /**
     * 根据Uuid查询工单
     * @param uuid
     * @return
     */
    PushForbidIpEntity getByUuid(String uuid);


}
