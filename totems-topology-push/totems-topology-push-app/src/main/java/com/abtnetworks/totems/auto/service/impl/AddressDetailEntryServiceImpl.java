package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.dao.mysql.AddressDetailEntryMapper;
import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.service.AddressDetailEntryService;
import com.abtnetworks.totems.common.signature.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 对象管理地址条目表
 * @Version --
 * @Created by zhoumuhua on '2021-10-28'.
 */
@Service
@Slf4j
public class AddressDetailEntryServiceImpl implements AddressDetailEntryService {

    @Autowired
    private AddressDetailEntryMapper addressDetailEntryMapper;

    @Override
    public ReturnT<String> insert(AddressDetailEntryEntity addressManageDetailEntity) {

        if (addressManageDetailEntity == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        addressDetailEntryMapper.insert(addressManageDetailEntity);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> deleteById(Integer id) {

        addressDetailEntryMapper.deleteByPrimaryKey(id);

        return ReturnT.SUCCESS;
    }


    @Override
    public ReturnT<String> update(AddressDetailEntryEntity AddressDetailEntryEntity) {
        int ret = addressDetailEntryMapper.updateByPrimaryKey(AddressDetailEntryEntity);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public AddressDetailEntryEntity getById(Integer id) {
        return addressDetailEntryMapper.selectByPrimaryKey(id);
    }



}
