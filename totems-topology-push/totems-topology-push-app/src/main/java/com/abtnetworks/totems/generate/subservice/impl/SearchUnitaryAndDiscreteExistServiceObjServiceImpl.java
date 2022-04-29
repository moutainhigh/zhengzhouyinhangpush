package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description 该类用于先查询整体复用，在查询离散复用对象
 * @Author zy
 */
@Slf4j
@Service
public class SearchUnitaryAndDiscreteExistServiceObjServiceImpl implements CmdService {

    @Autowired
    Map<String, CmdService> cmdServiceMap;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        log.info(String.format("高级设置复用现有服务对象，先整体复用获取，再离散服务对象......"));
        //先查整体复用情况
        CmdService serviceUnitaryFormat = cmdServiceMap.get(NameUtils.getServiceDefaultName(SubServiceEnum.SEARCH_UNITARY_EXIST_SERVICE.getServiceClass()));
        serviceUnitaryFormat.modify(cmdDTO);
        //再离散查复用
        CmdService serviceDiscreteFormat = cmdServiceMap.get(NameUtils.getServiceDefaultName(SubServiceEnum.SEARCH_DISCRETE_EXIST_SERVICE.getServiceClass()));
        serviceDiscreteFormat.modify(cmdDTO);
    }


}
