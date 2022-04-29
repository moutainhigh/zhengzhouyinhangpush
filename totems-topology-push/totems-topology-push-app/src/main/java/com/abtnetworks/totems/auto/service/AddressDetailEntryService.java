package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;

/**
 * @Description 对象管理地址条目表
 * @Version --
 * @Created by zhoumuhua on '2021-10-28'.
 */
public interface AddressDetailEntryService {

    /**
     * 新增
     * @param addressDetailEntryEntity 地址详情实体对象
     */
    ReturnT<String> insert(AddressDetailEntryEntity addressDetailEntryEntity);

    /**
     * 删除
     * @param ids 地址详情id,多个用,隔开
     */
    ReturnT<String> deleteById(Integer id);

    /**
     * 更新
     * @param addressDetailEntryEntity 地址详情实体对象
     */
    ReturnT<String> update(AddressDetailEntryEntity addressDetailEntryEntity);

    /**
     * 查询 get By Id
     * @param id 主键
     */
    AddressDetailEntryEntity getById(Integer id);



}
